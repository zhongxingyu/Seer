 /*******************************************************************************
  * Copyright (c) 2001, 2007 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.common.metadata.internal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jst.jsf.common.JSFCommonPlugin;
 
 /**
  * Creates instances of IMetaDataLocators and caches them so that there is only one instance of a particular locator
  * when client requests one. 
  */
 public class MetaDataLocatorFactory  
 	implements IResourceChangeListener {
 	
 	private static MetaDataLocatorFactory INSTANCE = null;
 	private Map<String, IMetaDataLocator> _locators;
 	
 	private static final boolean DEBUG = false;
 	/**
 	 * @return singleton instance of the MetaDataLocatorFactory
 	 */
 	public synchronized static MetaDataLocatorFactory getInstance(){
 		if (INSTANCE == null){
 			INSTANCE = new MetaDataLocatorFactory();
			ResourcesPlugin.getWorkspace().addResourceChangeListener(INSTANCE,
			        IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE);
 		}
 		return INSTANCE;
 	}
 
 
 
 	private Map<String, IMetaDataLocator> getLocators() {
 		if (_locators == null){
 			_locators = new HashMap<String, IMetaDataLocator>();
 		}
 		return _locators;
 	}
 	
 	/**
 	 * @param locatorClassName - may NOT be null
 	 * @param bundleId - may NOT be null
 	 * @param project - may be null
 	 * @return shared instance of IMetaDataLocator 
 	 * 			may return null if is IPathSensitiveMetaDataLocator and there is no project context 
 	 */
 	public IMetaDataLocator getLocator(final String locatorClassName, final String bundleId, final IProject project){
 		final Class klass = JSFCommonPlugin.loadClass(locatorClassName, bundleId);
 		String key = getKey(locatorClassName, bundleId);
 		IMetaDataLocator locator = null;
 		try {
 			IMetaDataLocator tempLocator = (IMetaDataLocator)klass.newInstance();
 			if (tempLocator != null) {				
 				if (tempLocator instanceof IPathSensitiveMetaDataLocator) {
 					if (project == null)
 						return null;
 					
 					key = getKey(locatorClassName, project.getName());
 				}
 				
 				locator = getLocators().get(key);
 				if (locator == null) {
 					locator = tempLocator;					
 					if (locator instanceof IPathSensitiveMetaDataLocator)
 						((IPathSensitiveMetaDataLocator)locator).setProjectContext(project);					
 					
 					if (DEBUG)
 						System.out.println("Created locator: "+locator.toString()); //$NON-NLS-1$
 					
 					getLocators().put(key, locator);
 					locator.startLocating();
 				}
 			}
 		} catch (InstantiationException e) {
 			JSFCommonPlugin.log(IStatus.ERROR, "Could not instantiate IMetaDataLocator: "+key, e); //$NON-NLS-1$
 		} catch (IllegalAccessException e) {
 			JSFCommonPlugin.log(IStatus.ERROR, "IllegalAccessException while creating IMetaDataLocator: "+key, e); //$NON-NLS-1$
 		}
 		
 		return locator;
 	}
 
 	/**
 	 * @param locatorClassName
 	 * @param contextId - this may be the bundleID or the projectName if it is a path sensitive locator
 	 * @return key 
 	 */
 	private String getKey(final String locatorClassName, final String contextId) {
 		StringBuffer buf = new StringBuffer(contextId);
 		buf.append(":"); //$NON-NLS-1$
 		buf.append(locatorClassName);
 		return buf.toString();
 	}
 	
 	/**
 	 * Stops and disposes all locators
 	 */
 	public void dispose(){
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 		for (Iterator it=getLocators().values().iterator();it.hasNext();){
 			IMetaDataLocator locator = (IMetaDataLocator)it.next();
 			locator.stopLocating();			
 		}
 		getLocators().clear();
 	}
 	
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org
      * .eclipse.core.resources.IResourceChangeEvent)
      */
     public void resourceChanged(final IResourceChangeEvent event) {
         if (event.getType() == IResourceChangeEvent.PRE_CLOSE
                 || event.getType() == IResourceChangeEvent.PRE_DELETE) {
             // a project is closing - release and cleanup
             final IProject aProject = (IProject) event.getResource();
             
             if (aProject != null) {
             	List<String> locatorsToRemove = new ArrayList<String>();
 	    		for (Iterator it=getLocators().keySet().iterator();it.hasNext();){
 	    			String key = (String)it.next();	    			
 	    			if (locatorIsForProject(key, aProject.getName())) {
 	    				locatorsToRemove.add(key);
 	    			}
 	    		}
 
 	    		if (! locatorsToRemove.isEmpty()) {
 	    			for (String key : locatorsToRemove) {
 	    				IMetaDataLocator locator = getLocators().get(key);
 						
 						if (DEBUG)
 							System.out.println("Removed locator: "+locator.toString()); //$NON-NLS-1$
 	    				
 	    				locator.stopLocating();
 	    				getLocators().remove(key);
 	    			}
 	    		}
             }
         }
     }
 
 	private boolean locatorIsForProject(final String key, final String projectName) {
 		StringTokenizer t = new StringTokenizer(key, ":"); //$NON-NLS-1$
 		String contextId = t.nextToken();
 		if (contextId.equals(projectName))
 			return true;
 		return false;
 	}
 }
