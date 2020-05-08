 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Gerry Kessler - initial API and implementation
  *******************************************************************************/ 
 package org.eclipse.jst.jsf.core.jsflibraryregistry;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.Messages;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.ArchiveFile;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibrary;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibraryRegistryFactory;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFVersion;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.PluginProvidedJSFLibrary;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Helper class used to create JSF Libraries from the
  * <code>org.eclipse.jst.jsf.core.jsflibraries</code> extension-point.
 * 
 * <p><b>Provisional API - subject to change</b></p>
  * 
  * @deprecated - clients should not use
  */
 public final class PluginProvidedJSFLibraryCreationHelper {
 	private IConfigurationElement config_element;
 
 	/**
 	 * Key of the 'name' attribute of the extension point.
 	 */
 	public final static String NAME 		= "name"; //$NON-NLS-1$
 	/**
 	 * Key of the 'isImplementation' attribute of the extension point.
 	 */
 	public final static String IS_IMPL 		= "isImplementation"; //$NON-NLS-1$
 	/**
 	 * Key of the 'maxVersionSupported' attribute of the extension point.
 	 */
 	public final static String VERSION 		= "maxVersionSupported"; //$NON-NLS-1$
 	/**
 	 * Key of the 'archiveFilesDelegate' attribute of the extension point.
 	 */
 	public final static String DELEGATE 	= "archiveFilesDelegate"; //$NON-NLS-1$
 
 	/**
 	 * Creates an instance with the specified IConfigurationElement instance.
 	 * 
 	 * @param jsfLibrary IConfigurationElement instance
 	 */
 	public PluginProvidedJSFLibraryCreationHelper (IConfigurationElement jsfLibrary){
 		this.config_element = jsfLibrary;
 	}
 
 	/**
 	 * Creates a new PluginProvidedJSFLibrary from the JSFLibrary extension point.
 	 * 
 	 * @return PluginProvidedJSFLibrary instance.
 	 */
 	public JSFLibrary create(){
 		PluginProvidedJSFLibrary newLib = JSFLibraryRegistryFactory.eINSTANCE.createPluginProvidedJSFLibrary();
 //		newLib.setID(getLibID());
 		newLib.setPluginID(getPluginID());
 		newLib.setName(config_element.getAttribute(NAME));
 		newLib.setImplementation(config_element.getAttribute(IS_IMPL).equals("true") ? true : false); //$NON-NLS-1$		
 		newLib.setJSFVersion(JSFVersion.getJSFVersion(config_element.getAttribute(VERSION)));
 		
 		try {
 			addArchives(newLib);			
 			return newLib;
 		} catch (Exception e) {
 			JSFCorePlugin.log(
 					e,
 					NLS.bind(
 							Messages.PluginProvidedJSFLibraryCreationHelper_ErrorCreating,
 							newLib.getName()));
 		}
 		return null;
 	}
 
 	/**
 	 * Adds ArchiveFile instances to the specified JSFLibrary instance.
 	 * 
 	 * @param newLib JSFLibrary instance
 	 * @throws InvalidArchiveFilesCreationException on attempt to create
 	 * multiple instances at same location.
 	 * @throws CoreException on core failure.
 	 */
 	private void addArchives(JSFLibrary newLib) throws InvalidArchiveFilesCreationException, CoreException {
 		JSFLibraryArchiveFilesDelegate jarCol = null;
 		ArchiveFile jar = null;
 
 		jarCol = (JSFLibraryArchiveFilesDelegate)config_element.createExecutableExtension(DELEGATE);
 		if (jarCol != null){
 			jarCol.setConfigurationElement(config_element);
 			Collection jars = jarCol.getArchiveFiles();
 			if (jars == null)//TODO: better validation and error handling
 				return;
 			Iterator it = jars.iterator();
 			while (it.hasNext()){
 				Object aJar = it.next();
 				if (aJar instanceof ArchiveFile){//for now check to see ArchiveFiles were being returned
 					jar = (ArchiveFile)aJar;
 					if (!newLib.containsArchiveFile(jar.getSourceLocation()))
 						newLib.getArchiveFiles().add(jar);
 				}
 				else {
 					throw new InvalidArchiveFilesCreationException(
 							NLS.bind(
 									Messages.PluginProvidedJSFLibraryCreationHelper_ErrorMultipleDefinition,
 									jar.getSourceLocation(),
 									config_element.getName()));
 				}
 					
 			}
 		}
 	}
 	
 	/**
 	 * Returns the plugin's ID.
 	 * 
 	 * @return The plugin's ID
 	 */
 	private String getPluginID() {
 		return config_element.getDeclaringExtension().getContributor().getName();
 	}
 }
