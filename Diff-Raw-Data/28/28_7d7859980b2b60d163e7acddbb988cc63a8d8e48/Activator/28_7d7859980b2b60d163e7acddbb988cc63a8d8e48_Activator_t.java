 /**
  * Copyright (c) 2010, 2012 Darmstadt University of Technology.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Tobias Boehm - initial API and implementation.
  */
 
 package org.eclipse.recommenders.internal.codesearch.rcp;
 
 import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.WorkbenchPlugin;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 public class Activator extends AbstractUIPlugin {
 
     // The plug-in ID
     public static final String PLUGIN_ID = "org.eclipselabs.recommenders.codesearch.rcp"; //$NON-NLS-1$
 
     // The shared instance
     private static Activator plugin;
 
     public static Activator getDefault() {
         return plugin;
     }
 
     public static void logError(final Throwable e, final String format, final Object... args) {
         logConsole(e, format, args);
     }
 
     public static void logError(final Throwable e) {
         logConsole(e, null);
     }
 
     public static void logWarning(final Throwable e, final String format, final Object... args) {
         logConsole(e, format, args);
     }
 
     public static void logWarning(final String format, final Object... args) {
         logConsole(null, format, args);
     }
 
     public static void logInfo(final String format, final Object... args) {
         logConsole(null, format, args);
     }
 
     public static void log(final IStatus s) {
         // logConsole(s);
     }
 
    @SuppressWarnings("restriction")
	private static void logConsole(final Throwable e, final String format, final Object... args) {
         try {
        	String msg = "";
             if (format != null) {
            	msg = String.format(format, args);
                System.out.println(msg);
             }
             if (e != null) {
                 e.printStackTrace();
             }

            WorkbenchPlugin.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg, e));
         } catch (final Exception ex) {
             System.out.println(String.format("String [%1$s] cannot be formatted correctly. Stacktrace: %2$s", format,
                     ex.getStackTrace()));
         }
     }
 
     // private static void logConsole(final IStatus s) {
     // System.out.println(s);
     // }
 
     @Override
     public void start(final BundleContext context) throws Exception {
         plugin = this;
         super.start(context);
     }
 
     @Override
     public void stop(final BundleContext context) throws Exception {
         plugin = null;
         super.stop(context);
     }
 
 }
