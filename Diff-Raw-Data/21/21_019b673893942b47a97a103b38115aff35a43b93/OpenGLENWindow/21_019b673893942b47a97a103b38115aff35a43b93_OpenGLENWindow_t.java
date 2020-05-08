 /* Copyright 2012 Richard Sahlin
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 package com.super2k.openglen.jogamp;
 
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLContext;
 import javax.media.opengl.GLDrawable;
 import javax.media.opengl.GLDrawableFactory;
 import javax.media.opengl.GLProfile;
 
 import com.jogamp.newt.event.KeyEvent;
 import com.jogamp.newt.event.KeyListener;
 import com.jogamp.newt.event.MouseEvent;
 import com.jogamp.newt.event.MouseListener;
 import com.jogamp.newt.event.WindowEvent;
 import com.jogamp.newt.event.WindowListener;
 import com.jogamp.newt.event.WindowUpdateEvent;
 import com.jogamp.newt.opengl.GLWindow;
 import com.super2k.openglen.OpenGLENException;
 import com.super2k.openglen.RenderSetting;
 import com.super2k.openglen.SurfaceConfiguration;
 import com.super2k.openglen.nibbler.CompatibilityRunner;
 import com.super2k.openglen.nibbler.J2SEInputStreamResolver;
 import com.super2k.openglen.nibbler.OpenGLENRunner;
 import com.super2k.openglen.nibbler.UserEvent;
 import com.super2k.openglen.utils.Log;
 
 /**
  * This class has an OpenGLENRunner and renderer. When the setup method is called a new window is created.
  * It has the behavior of EGL where a surface is created with the specific configuration, called capabilities
  * in the JOGAMP implementation.
  * The renderer is initialized with this window and the OpenGLENRunner is started by calling start()
  * @author Richard Sahlin
  *
  */
 public class OpenGLENWindow implements WindowListener, MouseListener, KeyListener {
 
     public final String TAG = this.getClass().getSimpleName();
 
     JOGLGLES20Renderer mRenderer;
     /**
      * Runnable implementation that will
      * drive the compatibility runner and renderloop.
      */
     OpenGLENRunner mGlThread;
 
     /**
      * Creates a window and connects a renderer to that window.
      * @param surfaceConfig The surface configuration, bitdepth, multisampling etc
      * @param renderConfig The render settings.
      * @param windowWidth Width of window in pixels
      * @param windowHeight Height of window in pixels
      * @param resourcePath Path to resources
      * @param runnerClass CompatibilityRunner to use for OpenGLENRunner, this is the implementation
      * that will drive updates in the window.
      * @throws OpenGLENException If the renderer cannot be initialized
      * @throws IllegalArgumentException If one of the parameters is null or if the runnerclass
      * cannot be created.
      */
     public OpenGLENWindow(SurfaceConfiguration surfaceConfig, RenderSetting renderConfig,
             int windowWidth, int windowHeight, String resourcePath, CompatibilityRunner runnerClass)
                                                                     throws OpenGLENException {
         super();
         setup(surfaceConfig, renderConfig, windowWidth, windowHeight, resourcePath, runnerClass);
     }
 
     /**
      * Creates a window and connects a renderer to that window.
      * @param surfaceConfig The surface configuration, bitdepth, multisampling etc
      * @param renderConfig The render settings.
      * @param windowWidth Width of window in pixels
      * @param windowHeight Height of window in pixels
      * @param resourcePath Path to resources
      * @param runnerClass CompatibilityRunner to use for OpenGLENRunner, this is the implementation
      * that will drive updates in the window.
      * @throws OpenGLENException If the renderer cannot be initialized
      * @throws IllegalArgumentException If one of the parameters is null or if the runnerclass
      * cannot be created.
      */
     private void setup(SurfaceConfiguration surfaceConfig, RenderSetting renderConfig,
             int windowWidth,int windowHeight, String resourcePath, CompatibilityRunner runnerClass)
                                                                         throws OpenGLENException {
 
         GLProfile glp = GLProfile.get(GLProfile.GL2ES2);
         GLCapabilities caps = new GLCapabilities(glp);
         if (surfaceConfig.getSamples() > 1) {
             caps.setNumSamples(surfaceConfig.getSamples());
         } else {
             caps.setSampleBuffers(false);
         }
         caps.setRedBits(surfaceConfig.getRedBits());
         caps.setGreenBits(surfaceConfig.getGreenBits());
         caps.setBlueBits(surfaceConfig.getBlueBits());
         caps.setAlphaBits(surfaceConfig.getAlphaBits());
         caps.setDepthBits(surfaceConfig.getDepthBits());
 
         GLWindow glWin = GLWindow.create(caps);
         glWin.setSize(windowWidth, windowHeight);
         glWin.setVisible(true);
         glWin.addWindowListener(this);
         glWin.addMouseListener(this);
         glWin.addKeyListener(this);
 
         /**
          * EGL is not used in JOGAMP - to get swapBuffer the JOGLGLES20 renderer implements EGLRenderer
          * but EGL cannot be created. Do it here instead and pass on relevant objects to renderer.
          */
         GLDrawable glDrawable = GLDrawableFactory.getFactory(glp).createGLDrawable(glWin.getNativeSurface());
         GLContext glContext = glDrawable.createContext(null);
 
         mRenderer = new JOGLGLES20Renderer(glDrawable.getWidth(), glDrawable.getHeight(),
                 glContext, glDrawable, renderConfig);
 
         J2SEInputStreamResolver resolver = new J2SEInputStreamResolver(resourcePath);
         mGlThread = new OpenGLENRunner(mRenderer, resolver, runnerClass, windowWidth,
                                         windowHeight);
 
     }
 
     @Override
     public void windowDestroyNotify(WindowEvent arg0) {
         Log.d(TAG, "windowDestroyNotify");
         mGlThread.destroy();
 
     }
 
     @Override
     public void windowDestroyed(WindowEvent arg0) {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void windowGainedFocus(WindowEvent arg0) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void windowLostFocus(WindowEvent arg0) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void windowMoved(WindowEvent arg0) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void windowRepaint(WindowUpdateEvent arg0) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void windowResized(WindowEvent arg0) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void mouseClicked(MouseEvent arg0) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void mouseDragged(MouseEvent arg0) {
         if (mGlThread != null && mGlThread.getRunner() != null) {
             mGlThread.getRunner().touchMove(arg0.getX(), arg0.getY(), arg0.getWhen());
         }
 
     }
 
     @Override
     public void mouseEntered(MouseEvent arg0) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void mouseExited(MouseEvent arg0) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void mouseMoved(MouseEvent arg0) {}
 
     @Override
     public void mousePressed(MouseEvent arg0) {
         if (mGlThread != null && mGlThread.getRunner() != null) {
             mGlThread.getRunner().touchDown(arg0.getX(), arg0.getY(), arg0.getWhen());
         }
     }
 
     @Override
     public void mouseReleased(MouseEvent arg0) {
         if (mGlThread != null && mGlThread.getRunner() != null) {
             mGlThread.getRunner().touchUp(arg0.getX(), arg0.getY(), arg0.getWhen());
         }
 
     }
 
     @Override
     public void mouseWheelMoved(MouseEvent arg0) {
 
     }
 
     @Override
     public void keyPressed(KeyEvent arg0) {
 
     }
 
     @Override
     public void keyReleased(KeyEvent arg0) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
    public void keyTyped(KeyEvent key) {
         if (mGlThread != null && mGlThread.getRunner() != null) {
            System.out.println("char:" + key.getKeyChar() + ", code:" + key.getKeyCode());
             UserEvent event = null;
            if (key.getKeyChar() == '1') {
                 event = new UserEvent(UserEvent.TYPE_KEY_DOWN,
                         new Integer(UserEvent.BUTTON_1));
            } else if (key.getKeyChar() == '2') {
                 event = new UserEvent(UserEvent.TYPE_KEY_DOWN,
                         new Integer(UserEvent.BUTTON_2));
            } else if (key.getKeyChar() == '3') {
                 event = new UserEvent(UserEvent.TYPE_KEY_DOWN,
                         new Integer(UserEvent.BUTTON_3));
            } else if (key.getKeyChar() == '4') {
                 event = new UserEvent(UserEvent.TYPE_KEY_DOWN,
                         new Integer(UserEvent.BUTTON_4));
            } else if (key.getKeyCode() == KeyEvent.VK_ESCAPE) {
                event = new UserEvent(UserEvent.TYPE_KEY_DOWN,
                        new Integer(UserEvent.BACK_KEY));
             }
             if (event != null) {
                 mGlThread.getRunner().userEvent(event);
             }
 
         }
 
     }
 
 }
