 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core;
 
 import java.io.InputStream;
 
 import org.eclipse.core.runtime.Plugin;
 import org.osgi.framework.BundleContext;
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 
 /**
  * @author BREDEX GmbH
  * @created Nov 29, 2010
  */
 public class Activator extends Plugin {
     /** The plug-in ID */
     public static final String PLUGIN_ID = "org.eclipse.jubula.client.core"; //$NON-NLS-1$
     
     /**
      * <code>RESOURCES_DIR</code>
      */
     public static final String RESOURCES_DIR = "resources/"; //$NON-NLS-1$
     
     /** The shared instance */
     private static Activator plugin;
     
     /**
      * Constructor
      */
     public Activator() {
         // empty
     }
     
     /**
      * {@inheritDoc}
      */
     public void start(BundleContext context) throws Exception {
         super.start(context);
         plugin = this;
        // initialize tie logging facility
         LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
         try {
             JoranConfigurator configurator = new JoranConfigurator();
             configurator.setContext(lc);
             // the context was probably already configured by default
             // configuration
             // rules
             lc.reset();
             InputStream is = context.getBundle().getResource("logback.xml") //$NON-NLS-1$
                     .openStream();
             configurator.doConfigure(is);
         } catch (JoranException je) {
             // no logging if logger fails :-(
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     public void stop(BundleContext context) throws Exception {
         plugin = null;
         super.stop(context);
     }
     
 
     /**
      * Returns the shared instance
      * 
      * @return the shared instance
      */
     public static Activator getDefault() {
         return plugin;
     }
 }
