 /*
  * Copyright 2009 University of Zurich, Switzerland
  * Copyright 2013 Quinten Soetens - Adapted from org.evolizer.core.hibernate
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package hibernate;
 
import hibernate.session.SessionHandler;

 import java.io.IOException;
 import java.io.InputStream;
 
 import org.eclipse.core.runtime.Plugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle.
  * 
  * @author wuersch
  */
 public class HibernatePlugin extends Plugin {
 
     /**
      * The plug-in ID
      */
     public static final String PLUGIN_ID = "hibernate";
 
     // The shared instance
     private static HibernatePlugin sPlugin;
 
     /**
      * The constructor.
      */
     public HibernatePlugin() {
         HibernatePlugin.sPlugin = this;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void start(BundleContext context) throws Exception {
         super.start(context);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void stop(BundleContext context) throws Exception {
         HibernatePlugin.sPlugin = null;
        SessionHandler.getHandler().cleanupHibernateSessions();
         super.stop(context);
     }
 
     /**
      * Returns the shared instance.
      * 
      * @return the shared instance
      */
     public static HibernatePlugin getDefault() {
         return HibernatePlugin.sPlugin;
     }
 
 
     /**
      * Opens a file located within the plugin-bundle.
      * 
      * @param filePath
      *            relative path of the file starting at the root of this plugin
      * @return an InputStream reading the specified file
      * @throws IOException
      *             if file could not be opened
      */
     public static InputStream openBundledFile(String filePath) throws IOException {
         return HibernatePlugin.getDefault().getBundle().getEntry(filePath).openStream();
     }
 
 }
