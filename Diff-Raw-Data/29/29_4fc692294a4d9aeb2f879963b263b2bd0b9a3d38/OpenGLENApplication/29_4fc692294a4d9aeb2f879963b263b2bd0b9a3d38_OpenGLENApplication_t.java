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
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.util.StringTokenizer;
 
 import com.super2k.openglen.OpenGLENException;
 import com.super2k.openglen.RenderSetting;
 import com.super2k.openglen.SurfaceConfiguration;
 import com.super2k.openglen.nibbler.CompatibilityRunner;
 import com.super2k.openglen.utils.ConfigurationParameters;
 import com.super2k.openglen.utils.Log;
 
 /**
  * Base class for a J2SE OpenGLEN application.
  * Call the startOpenGLEN to create a window and start OpenGLEN.
  * @author Richard Sahlin
  *
  */
 public class OpenGLENApplication {
 
     private final String TAG = this.getClass().getSimpleName();
 
     /**
      * Property to set the runnerclass, the runnerclass must be an instance of
      * CompatibilityRunner
      */
     public final static String PROPERTY_RUNNER_CLASS = "runnerClass";
     /**
      * Property to set the resource path.
      */
     public final static String PROPERTY_RESOURCE_PATH = "resourcePath";
     /**
      * Property to set the screen (window) size, same as PROPERTY_WINDOWSIZE
      */
     public final static String PROPERTY_SCREENSIZE = "screensize";
 
     /**
      * Property to set the window (screen) size, same as PROPERTY_SCREEN_SIZE
      */
     public final static String PROPERTY_WINDOWSIZE = "windowsize";
 
     /**
      * Default window width on J2SE, choose same width as screen.
      */
     protected int mWindowWidth = -1;
     /**
      * Default window height on J2SE, choose same height as screen.
      */
     protected int mWindowHeight = -1;
 
     protected String mRunnerClass = null;
     protected String mResourcePath = null;
     protected OpenGLENWindow mGlenWindow;
 
     public OpenGLENApplication() {
         super();
     }
 
     /**
      * Main entry for OpenGLEN application, sublcasses must implement this method
      * in order to create correct class.
      * @param args
      */
     public static void main(String[] args) {
         throw new IllegalArgumentException("Must create subclass of " + OpenGLENApplication.class.getSimpleName());
     }
 
     /**
      * Constructs a new OpenGLENApplication using the specified shader source.
      * A window will be created and a renderer will be attached to it.
      * An OpenGLENThread will be created and started.
      * @param shaderSource Path to where shaders are
      * @param resourcePath Path to resources.
      * @param runnerClass CompatibilityRunner implementation that will be called in the OpenGLENThread
      */
     public void startOpenGLEN(String[] args) {
 
         try {
 
             checkProperties(args);
 
             if (mRunnerClass == null) {
                 Log.d(TAG, "No runner class set, nothing to start. Set argument 'runnerClass'");
                 throw new IllegalArgumentException("No runner class is set");
             }
             if (mResourcePath == null) {
                 Log.d(TAG, "No resource path set. Set argument '" + PROPERTY_RESOURCE_PATH + "'");
                 throw new IllegalArgumentException("No resource path is set");
             }
 
             try {
                 Class<?> rClass = Class.forName(mRunnerClass);
                createWindow((CompatibilityRunner)rClass.newInstance(), mResourcePath);
 
             } catch (ClassNotFoundException cnfe) {
                 Log.d(TAG, "Could not create class " + mRunnerClass);
                 throw new IllegalArgumentException(cnfe);
             } catch (InstantiationException ie) {
                 Log.d(TAG, "Could not instantiate class " + mRunnerClass);
                 throw new IllegalArgumentException(ie);
             } catch (IllegalAccessException ia) {
                 Log.d(TAG, "Could not access class " + mRunnerClass);
                 throw new IllegalArgumentException(ia);
             }
 
         } catch (OpenGLENException glene) {
             //Cannot recover.
             throw new RuntimeException(glene);
 
         }
     }
 
     /**
      * Check for system properties and commandline properteis to set runnerclass,
      * resourcepath and windowsize.
      * @param args
      */
     public void checkProperties(String[] args) {
 
         //Check system properties, commandline properties should override system settings.
         mRunnerClass = System.getProperty(PROPERTY_RUNNER_CLASS);
         mResourcePath = System.getProperty(PROPERTY_RESOURCE_PATH);
         String screensize = System.getProperty(PROPERTY_SCREENSIZE);
         if (screensize == null) {
             screensize = System.getProperty(PROPERTY_WINDOWSIZE);
         }
 
         //Last check the command line arguments
         if (args != null) {
             for (int i = 0; i < args.length; i++) {
 
                 if (args[i].toLowerCase().startsWith(PROPERTY_RUNNER_CLASS.toLowerCase() + "=")) {
                     mRunnerClass = args[i].substring(12);
                 } else if (args[i].toLowerCase().startsWith(
                         PROPERTY_RESOURCE_PATH.toLowerCase() + "=")) {
                     mResourcePath = args[i].substring(13);
                 } else if (args[i].toLowerCase().startsWith(PROPERTY_SCREENSIZE)) {
                     screensize = args[i].substring(PROPERTY_SCREENSIZE.length() + 1);
                 } else if (args[i].toLowerCase().startsWith(PROPERTY_WINDOWSIZE)) {
                     screensize = args[i].substring(PROPERTY_WINDOWSIZE.length() + 1);
                 }
 
             }
         }
         if (screensize != null) {
             StringTokenizer st = new StringTokenizer(screensize, ",");
             mWindowWidth = Integer.parseInt(st.nextToken());
             mWindowHeight = Integer.parseInt(st.nextToken());
             if (mWindowWidth == 0 || mWindowHeight == 0 ||
                 mWindowWidth < -1 || mWindowHeight < -1) {
                 throw new IllegalArgumentException("Width and height must be > 0 or -1 (" +
                         mWindowWidth + ", " + mWindowHeight + ")");
             }
         }
 
     }
 
     /**
      * Creates the window that can be used to attach a Renderer to.
      * @param runner Runner to attach to thread, this is the implementation that will drive
      * updates in the window.
      * @param resourcePath The path to where resources are loaded from.
      * @throws OpenGLENException If renderer cannot be started.
      * @throws IllegalArgumentException If resourcePath or runner is null
      */
    protected void createWindow(CompatibilityRunner runner, String resourcePath) throws OpenGLENException {
         if (resourcePath == null || runner == null) {
             throw new IllegalArgumentException("Null parameter");
         }
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        if (mWindowWidth == - 1) {
            mWindowWidth = (int) size.getWidth();
        }
        if (mWindowHeight == - 1) {
            mWindowHeight = (int) size.getHeight();
        }
         SurfaceConfiguration surfaceConfig = new SurfaceConfiguration();
         RenderSetting renderSetting = new RenderSetting();
         ConfigurationParameters.getSurfaceConfiguration(surfaceConfig);
         ConfigurationParameters.getRenderConfiguration(renderSetting);
        mGlenWindow = new OpenGLENWindow(surfaceConfig, renderSetting, mWindowWidth, mWindowHeight, resourcePath,
                           runner);
 
     }
 
     /**
      * Returns the compatibilityrunner that is used.
      * @return The CompatibilityRunner
      */
     public CompatibilityRunner getCompatibilityRunner() {
         return mGlenWindow.mGlThread.getRunner();
     }
 
 }
