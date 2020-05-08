 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Justin Chen - development check in
  *******************************************************************************/
 package org.eclipse.jst.jsf.core.internal.jsflibraryconfig;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibrary;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibraryRegistry;
 
 /**
  * 
  * @author Justin Chen - Oracle
  */
 public class JSFLibraryConfigModelAdapter {
 	private IProject project;
 	private JSFLibraryRegistry jsfLibReg;
 	private List colJSFImplLib;
 	private List colJSFCompLib;	
 	private JSFLibraryConfigPersistData data;
 	private JSFLibraryDecorator preSelectedJSFImpl = null;
 	
 	/**
 	 * @param project
 	 */
 	public JSFLibraryConfigModelAdapter(IProject project) {		
 		this.project = project;
 		this.jsfLibReg = JSFCorePlugin.getDefault().getJSFLibraryRegistry();
 		this.data = new JSFLibraryConfigPersistData(this.project);
 		preSelectedJSFImpl = data.getSelectedJSFLibImplementation();
 	}
 	
 	/**
 	 * (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.ui.properties.IJSFLibraryDecoratorProvider#getProject()
 	 */
 	public IProject getProject() {
 		return project;
 	}
 	
 	/**
 	 * (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.ui.properties.IJSFLibraryDecoratorProvider#getJSFLibraryRegistry()
 	 */
 	public JSFLibraryRegistry getJSFLibraryRegistry() {
 		return jsfLibReg;
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * @return List
 	 * @see org.eclipse.jst.jsf.ui.properties.IJSFLibraryDecoratorProvider#getProjectJSFImplementationLibraries()
 	 */
 	public List getProjectJSFImplementationLibraries() {
 		
 		if (colJSFImplLib == null) {
 			List list = new ArrayList(Collections.EMPTY_LIST);
 			if (data.getSelectedJSFLibImplementation() != null) {
 				list.add(data.getSelectedJSFLibImplementation());
 			}
 			colJSFImplLib = buildJSFLibraryDecoratorList(jsfLibReg.getImplJSFLibraries(), list);
 		}
 		return colJSFImplLib;
 		
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * @return List
 	 * @see org.eclipse.jst.jsf.ui.properties.IJSFLibraryDecoratorProvider#getProjectJSFComponentLibraries()
 	 */
 	public List getProjectJSFComponentLibraries() {
 		
 		if (colJSFCompLib == null) {
 			colJSFCompLib = buildJSFLibraryDecoratorList(jsfLibReg.getNonImplJSFLibraries(), 
 					data.getSelectedJSFLibComponent() );			
 		}
 		return colJSFCompLib;
 		
 	}	
 
 	/**
 	 * (non-Javadoc)
 	 * @param selJSFLibImpl 
 	 * @param selJSFLibComp 
 	 * @see org.eclipse.jst.jsf.core.internal.jsflibraryregistry.util.IJSFLibraryDecoratorProvider#saveModel(java.util.List, java.util.List)
 	 */
 	public void saveData(List selJSFLibImpl, List selJSFLibComp) {
 		
 		data.saveData(selJSFLibImpl, selJSFLibComp);
 		
 		// flush current list
 		colJSFImplLib = null;
 		colJSFCompLib = null;
 		
 	}
 	
 	/**
 	 * @return JSFLibraryDecorator
 	 */
 	public JSFLibraryDecorator getSelectedJSFImplementation() {		
 		return data.getSelectedJSFLibImplementation();		
 	}
 	
 	/**
 	 * @return JSFLibraryDecorator
 	 */
 	public JSFLibraryDecorator getPreviousJSFImplementation() {
 		return preSelectedJSFImpl;
 	}
 	
 	/**
 	 * @return List
 	 */
 	public List getSelectedJSFComponent() {		
 		return data.getSelectedJSFLibComponent();
 	}
 
 	
 	private List buildJSFLibraryDecoratorList(EList wsJSFLibs, List pjJSFLibs) {
 		
 		List list = new ArrayList(Collections.EMPTY_LIST);
 		
 		JSFLibraryDecorator newPrjJSFLib;
 		JSFLibraryDecorator prjJSFLib = null;
 		JSFLibrary jsfLib;
 		Iterator it = wsJSFLibs.iterator();
 		while(it.hasNext()) {
 			jsfLib = (JSFLibrary)it.next();
 			
 			Iterator itPrjLibs = pjJSFLibs.iterator();
 			boolean selected = false;
 			while(itPrjLibs.hasNext()) {
 				prjJSFLib = (JSFLibraryDecorator)itPrjLibs.next();
 				selected = prjJSFLib.getID().equals(jsfLib.getID());
 				if (selected) {
 					break;
 				}
 			}	
 			if (selected && prjJSFLib != null) {
 				newPrjJSFLib = prjJSFLib;
 			} else {
				newPrjJSFLib = new JSFLibraryDecorator(jsfLib, false, jsfLib.isDeployed());
 			}
 			list.add(newPrjJSFLib);			
 		}
 		
 		return list;
 	}
 	
 }	
