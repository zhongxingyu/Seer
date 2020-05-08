 /*******************************************************************************
  * Copyright (c) 2005-2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.eclipse.vjetv;
 
 import java.io.File;
 import java.util.Map;
 
 import org.eclipse.vjet.dsf.jstojava.cml.vjetv.core.HeadLessValidationEntry;
 import org.eclipse.vjet.eclipse.core.sdk.VJetSdkEnvironment;
 import org.eclipse.vjet.eclipse.core.ts.JstLibResolver;
 import org.eclipse.vjet.vjo.lib.IResourceResolver;
 import org.eclipse.vjet.vjo.lib.LibManager;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 
 /**
  * Class/Interface description
  * 
  * @author <a href="mailto:liama@ebay.com">liama</a>
  * @since JDK 1.5
  */
 public class VJETVRunner implements IApplication {
 
     private final static String SEPERATOR = File.separator;
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
      */
     @Override
     public Object start(IApplicationContext context) throws Exception {
 
 
 
         
         //Step3 run VJETV
         try {
 //            URLClassLoader classLoader = new URLClassLoader(fileURL
 //                    .toArray(new URL[] {}));
 //            Thread.currentThread().setContextClassLoader(classLoader);
 //            Class entry = classLoader
 //                    .loadClass("org.ebayopensource.dsf.jstojava.cml.vjetv.core.HeadLessValidationEntry");
            
         	IResourceResolver jstLibResolver = JstLibResolver.getInstance()
     				.setSdkEnvironment(new VJetSdkEnvironment(new String[0], "DefaultSdk"));
         	// TODO exclusion rules yes -- very important
         	// TODO project dependencies?
         	// TODO library dependency .zip format
         	// TODO clean up message Please check verified JS files writtern by VJO syntax. 
         	// TODO had issues with java heap 
         	// TODO look into issues with 
         	
     		LibManager.getInstance().setResourceResolver(jstLibResolver);
         	
         	HeadLessValidationEntry vjetvEntry = new HeadLessValidationEntry();
        
             Map contextArguments = context.getArguments();
             Object o = contextArguments
                     .get(IApplicationContext.APPLICATION_ARGS);
             String[] args = (String[]) o;
             
             if (args.length>0 && args[0].equalsIgnoreCase("-showlocation")) {
                 String[] actualArgs = new String[args.length - 1];
                 System.arraycopy(args, 1, actualArgs, 0, actualArgs.length);
               
                 vjetvEntry.main(actualArgs);
             } else {
             	 vjetvEntry.main(args);
             }
         } catch (Exception e) {
        	e.printStackTrace();
         	 System.exit(1);
         }
         return null;
     }
 
     /**
      * Get VjoJavaLib.jar and VjoJavaLibResource.jar
      * 
      * @param fileName
      *            {@link String}
      * @param folder
      *            {@link File}
      * @return {@link File}
      */
     private static File getFileFromFolder(String fileName, File folder) {
         File[] folders = folder.listFiles();
         for (int i = 0; i < folders.length; i++) {
             File versionFolder = folders[i];
             if (versionFolder.isDirectory()) {
                 File[] files = versionFolder.listFiles();
                 for (int j = 0; j < files.length; j++) {
                     File javaFolder = files[j];
                     if (javaFolder.isDirectory()) {
                         File[] desFiles = javaFolder.listFiles();
                         for (int w = 0; w < desFiles.length; w++) {
                             if (desFiles[w].getName()
                                     .equalsIgnoreCase(fileName)) {
                                 return desFiles[w];
                             }
                         }
                     }
                 }
             }
         }
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.equinox.app.IApplication#stop()
      */
     @Override
     public void stop() {
     }
 }
