 package com.badlogic.gdx.backends.lwjgl;
 
 /**
  * *****************************************************************************
  * Copyright 2011 See AUTHORS file.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
  * License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
  * ****************************************************************************
  */
 import com.badlogic.gdx.*;
 import com.badlogic.gdx.backends.openal.OpenALAudio;
 import java.util.*;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.AWTGLCanvas2;
 import org.lwjgl.opengl.GLContext;
 
 /**
  * An OpenGL surface on an AWT Canvas, allowing OpenGL to be embedded in a Swing application. All OpenGL calls are done on the EDT. This is slightly less
  * efficient then a dedicated thread, but greatly simplifies synchronization. Note that you may need to call {@link #stop()} or a Swing application may deadlock
  * on System.exit due to how LWJGL and/or Swing deal with shutdown hooks.
  *
  * @author Nathan Sweet
  */
 public class LwjglMultiCanvas implements Application {
 
     final LwjglGraphics2 graphics;
     final OpenALAudio audio;
     final LwjglFilesPro files;
     final LwjglMultiCanvasInput input;
     final List<Runnable> runnables = new ArrayList<Runnable>();
     final List<LwjglMultiCanvas.AWTCanvasContext> canvases = new ArrayList<LwjglMultiCanvas.AWTCanvasContext>();
     boolean running = true;
     int logLevel = LOG_INFO;
     Map<String, Preferences> preferences = new HashMap<String, Preferences>();
 
     private class AWTCanvasContext {
 
         int width;
         int height;
         boolean initialized = false;
         boolean resize = false;
         boolean destroy = false;
         AWTGLCanvas2 canvas;
         ApplicationListener listener;
     }
 
     public LwjglMultiCanvas(ApplicationListener listener, LwjglApplicationConfiguration config) {
         LwjglNativesLoader.load();
 
         graphics = new LwjglGraphics2(config);
         audio = new OpenALAudio();
         files = new LwjglFilesPro();
         input = new LwjglMultiCanvasInput();
 
         Gdx.app = this;
         Gdx.graphics = graphics;
         Gdx.audio = audio;
         Gdx.files = files;
         Gdx.input = input;
 
         if (listener != null) {
             LwjglMultiCanvas.AWTCanvasContext context = new LwjglMultiCanvas.AWTCanvasContext();
             context.width = config.width;
             context.height = config.height;
             context.canvas = null;
             context.listener = listener;
             synchronized (canvases) {
                 canvases.add(context);
             }
         }
 
         mainLoop();
     }
 
     private void mainLoop() {
         graphics.lastTime = System.nanoTime();
         running = true;
 
         new Timer().scheduleAtFixedRate(new TimerTask() {
 
             @Override
             public void run() {
                 synchronized (runnables) {
                     for (int i = 0; i < runnables.size(); i++) {
                         runnables.get(i).run();
                     }
                     runnables.clear();
                 }
 
                 if (running) {
                     graphics.updateTime();
                    
                     synchronized (canvases) {
                        for (LwjglMultiCanvas.AWTCanvasContext context : canvases) {
                            updateCanvas(context);
                         }
                     }
                     audio.update();
                 } else {
 //					display.dispose();
                     if (graphics.config.forceExit) {
                         System.exit(-1);
                     }
                 }
             }
         }, 0, 100);
     }
 
     private void updateCanvas(LwjglMultiCanvas.AWTCanvasContext context) {
         // makes OpenGL context the one from current canvas
         if (context.canvas != null) {
             if (!graphics.setupCanvas(context.canvas)) {
                 return;
             }
             input.setCurrentCanvas(context.canvas);
         }
 
         input.processEvents();
 
         if (!context.initialized) {
             // initialize canvas sub-App
             context.listener.create();
             context.resize = true;
             context.initialized = true;
             if (context.canvas != null) {
                 context.canvas.setCursor(null);
             }
         }
 
         if (context.resize) {
             context.resize = false;
             context.width = graphics.getWidth();
             context.height = graphics.getHeight();
             context.listener.resize(graphics.getWidth(), graphics.getHeight());
         }
 
         if (context.canvas != null) {
             try {
                 context.canvas.lockPeer();
 
                 context.canvas.makeCurrent();
 
                 if (context.destroy) {
                     context.canvas.destroy();
                 } else {
                     try {
                         GLContext.useContext(context.canvas);
                     } catch (LWJGLException e) {
                         e.printStackTrace();
                     }
 
                     context.canvas.updateContext();
 
                     int width = context.canvas.getWidth();
                     int height = context.canvas.getHeight();
                     if (context.width != width || context.height != height) {
                         context.width = width;
                         context.height = height;
                         context.listener.resize(context.width, context.height);
                     }
 
                     context.listener.render();
 
                     context.canvas.swapBuffers();
                     context.canvas.releaseContext();
                 }
             } catch (LWJGLException ex) {
                 ex.printStackTrace();
             } finally {
                 try {
                     context.canvas.unlockPeer();
                 } catch (LWJGLException ex) {
                     ex.printStackTrace();
                 }
             }
         }
 
         input.resetValues();
     }
 
     public void addCanvas(AWTGLCanvas2 canvas, ApplicationListener listener) {
         LwjglMultiCanvas.AWTCanvasContext context = new LwjglMultiCanvas.AWTCanvasContext();
         context.canvas = canvas;
         context.listener = listener;
         synchronized (canvases) {
             canvases.add(context);
         }
         input.initContext(canvas);
     }
 
     public void removeCanvas(AWTGLCanvas2 canvas) {
         LwjglMultiCanvas.AWTCanvasContext context = null;
         synchronized (canvases) {
             for (int i = 0; i < canvases.size(); i++) {
                 LwjglMultiCanvas.AWTCanvasContext ctx = canvases.get(i);
                 if (ctx.canvas == canvas) {
                     context = canvases.remove(i);
                     break;
                 }
             }
         }
 
         if (context != null) {
             if (context.initialized) {
                 context.destroy = true;
                 context.listener.pause();
                 context.listener.dispose();
             }
         }
         input.removeContext(canvas);
     }
 
     @Override
     public Audio getAudio() {
         return audio;
     }
 
     @Override
     public Files getFiles() {
         return files;
     }
 
     @Override
     public Graphics getGraphics() {
         return graphics;
     }
 
     @Override
     public Input getInput() {
         return input;
     }
 
     @Override
     public Application.ApplicationType getType() {
         return Application.ApplicationType.Desktop;
     }
 
     @Override
     public int getVersion() {
         return 0;
     }
 
     @Override
     public long getJavaHeap() {
         return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
     }
 
     @Override
     public long getNativeHeap() {
         return getJavaHeap();
     }
 
     @Override
     public Preferences getPreferences(String name) {
         if (preferences.containsKey(name)) {
             return preferences.get(name);
         } else {
             Preferences prefs = new LwjglPreferences(name);
             preferences.put(name, prefs);
             return prefs;
         }
     }
 
     @Override
     public void postRunnable(Runnable runnable) {
         synchronized (runnables) {
             runnables.add(runnable);
         }
     }
 
     @Override
     public void debug(String tag, String message) {
         if (logLevel >= LOG_DEBUG) {
             System.out.println(tag + ": " + message);
         }
     }
 
     @Override
     public void debug(String tag, String message, Throwable exception) {
         if (logLevel >= LOG_DEBUG) {
             System.out.println(tag + ": " + message);
             exception.printStackTrace(System.out);
         }
     }
 
     @Override
     public void log(String tag, String message) {
         if (logLevel >= LOG_INFO) {
             System.out.println(tag + ": " + message);
         }
     }
 
     @Override
     public void log(String tag, String message, Exception exception) {
         if (logLevel >= LOG_INFO) {
             System.out.println(tag + ": " + message);
             exception.printStackTrace(System.out);
         }
     }
 
     @Override
     public void error(String tag, String message) {
         if (logLevel >= LOG_ERROR) {
             System.err.println(tag + ": " + message);
         }
     }
 
     @Override
     public void error(String tag, String message, Throwable exception) {
         if (logLevel >= LOG_ERROR) {
             System.err.println(tag + ": " + message);
             exception.printStackTrace(System.err);
         }
     }
 
     @Override
     public void setLogLevel(int logLevel) {
         this.logLevel = logLevel;
     }
 
     @Override
     public void exit() {
         postRunnable(new Runnable() {
 
             @Override
             public void run() {
                 // shutdown all applications
                 for (LwjglMultiCanvas.AWTCanvasContext context : canvases) {
                     if (context.initialized) {
                         context.listener.pause();
                         context.listener.dispose();
                     }
                 }
                 audio.dispose();
                 running = false;
             }
         });
     }
 }
