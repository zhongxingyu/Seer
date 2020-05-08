 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jst.j2ee.internal.common;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.internal.core.ClasspathEntry;
 import org.eclipse.jem.util.logger.proxy.Logger;
 
 
 /**
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class UpdateProjectClasspath {
 	
  
 	public UpdateProjectClasspath(String sourceFolder, String componentName, IProject jProject, boolean isProjectMultiComponents){
 		addSrcFolderToProject(sourceFolder, componentName, jProject, isProjectMultiComponents);
 	}
 	
 	private IClasspathEntry[] getClasspathEntries(String sourceFolder, String componentName,
 			IProject jProject, boolean isProjectMultiComponents) {
 	
 		ArrayList list = new ArrayList();
 		list.add(JavaCore.newSourceEntry(jProject.getFullPath().append(sourceFolder)));
 		
 		IClasspathEntry[] classpath = new IClasspathEntry[list.size()];
         //adjust the output path to be bin/ComponentName
 		//boolean isProjectMultiComponents = FlexibleJavaProjectPreferenceUtil.getMultipleModulesPerProjectProp();
         IPath newOutputPath = null;
         for (int i = 0; i < classpath.length; i++) {
 			classpath[i] = (IClasspathEntry) list.get(i);
             if(isProjectMultiComponents)
                 newOutputPath = Path.fromOSString(Path.SEPARATOR +jProject.getName() + "/bin/" + componentName + Path.SEPARATOR);
             else
                 newOutputPath = Path.fromOSString(Path.SEPARATOR +jProject.getName() + "/bin/");
             ((ClasspathEntry)classpath[i]).specificOutputLocation = newOutputPath;
 		}
 		return classpath;		
 	}	
 	
 	private void addSrcFolderToProject(String sourceFolder,String componentName,
 			IProject jProject, boolean isProjectMultiComponents) {
 			
 		IJavaProject javaProject = JavaCore.create( jProject );
 		try {
 	
 			IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
            List oldEntriesList = new ArrayList();
 			IClasspathEntry[] newEntries = getClasspathEntries(sourceFolder, componentName, 
 					jProject, isProjectMultiComponents);
 			
 			int oldSize = oldEntries.length;
 			int newSize = newEntries.length;
 			
			IClasspathEntry[] classpathEnties = new IClasspathEntry[oldSize + newSize];
			int k = 0;
			for (int i = 0; i < oldEntries.length; i++) {
				classpathEnties[k] = oldEntries[i];
                oldEntriesList.add(oldEntries[i]);
				k++;
			}
 			for( int j=0; j< newEntries.length; j++){
                 if(!oldEntriesList.contains(newEntries[j])) {
                    classpathEnties[k] = newEntries[j];
                    k++;
                 }
 			}
			javaProject.setRawClasspath(classpathEnties, null);
 		}
 		catch (JavaModelException e) {
 			Logger.getLogger().logError(e);
 		}
 	}	
 
 }
