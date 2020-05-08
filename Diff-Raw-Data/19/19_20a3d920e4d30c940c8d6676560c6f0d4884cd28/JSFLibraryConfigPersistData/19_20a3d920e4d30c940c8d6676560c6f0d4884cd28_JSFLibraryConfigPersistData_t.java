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
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibrary;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibraryRegistry;
 import org.eclipse.jst.jsf.core.internal.project.facet.JSFUtils;
 
 /**
  * This class is responsible for persist and retore 
  * the configuration data for <b>JSFLibraryConfigControl</b>.  
  *  
  * @author Justin Chen - Oracle
  *
  */
 public class JSFLibraryConfigPersistData {
 	final protected static String SPTR_TUPLE = ":"; //$NON-NLS-1$
 	final protected static String EO_TUPLE = ";"; //$NON-NLS-1$
 	
 	private IProject project;
 	private JSFLibraryRegistry jsfLibReg;
 	private JSFLibraryDecorator selJSFLibImpl = null;
 	private List selJSFLibComp = null;
 	
  	/**
  	 * @param project
  	 */
  	protected JSFLibraryConfigPersistData(IProject project) {
 		this.project = project;	
 		this.jsfLibReg = JSFCorePlugin.getDefault().getJSFLibraryRegistry();
 	}
  	
 	/**
 	 * @return JSFLibraryDecorator
 	 */
 	protected JSFLibraryDecorator getSelectedJSFLibImplementation() {
 		
 		try {			
 			if ( selJSFLibImpl == null ) {
 				String strImplLibs = ((IResource)project).getPersistentProperty(new QualifiedName("", JSFUtils.PP_JSF_IMPLEMENTATION_LIBRARIES));				
 				selJSFLibImpl = getSelectedJSFLibImpl(getTuples(strImplLibs));
 			}
 		} catch (CoreException e) {
 			if (JSFCorePlugin.getDefault().getJSFLibraryRegistry() != null) {
 				JSFLibrary jsfLib = JSFCorePlugin.getDefault().getJSFLibraryRegistry().getDefaultImplementation();
 				if (jsfLib != null) {
					selJSFLibImpl = new JSFLibraryDecorator(jsfLib, true, true);
 				} 
 			} else {
 				JSFCorePlugin.getDefault().getMsgLogger().log(e);
 			}
 		}
 		return selJSFLibImpl;
 		
 	}
 	
 	/**
 	 * @return List
 	 */
 	protected List getSelectedJSFLibComponent() {	
 		
 		try {
 			if ( selJSFLibComp == null ) {
 				selJSFLibComp = new ArrayList(Collections.EMPTY_LIST);
 				String strCompLibs = ((IResource)project).getPersistentProperty(new QualifiedName("", JSFUtils.PP_JSF_COMPONENT_LIBRARIES));				
 				initJSFCompLibs(getTuples(strCompLibs), selJSFLibComp);
 			}		
 		} catch (CoreException e) {
 			JSFCorePlugin.getDefault().getMsgLogger().log(e);
 		}
 		return selJSFLibComp;
 	}
 	
 	/**
 	 * To persist configuration data as a project resource.
 	 * 
 	 * @param selJSFLibImpl
 	 * @param selJSFLibComp
 	 */
 	protected void saveData(List selJSFLibImpl, List selJSFLibComp) {
 		try {
 			((IResource)project).setPersistentProperty(new QualifiedName("", JSFUtils.PP_JSF_IMPLEMENTATION_LIBRARIES), generatePersistString(selJSFLibImpl));
 			((IResource)project).setPersistentProperty(new QualifiedName("", JSFUtils.PP_JSF_COMPONENT_LIBRARIES), generatePersistString(selJSFLibComp));
 			
 			this.selJSFLibImpl = null;
 			this.selJSFLibComp = null;
 			
 		} catch (CoreException e) {
 			JSFCorePlugin.getDefault().getMsgLogger().log(e);
 		}
 	}
 	
 	private JSFLibraryDecorator getSelectedJSFLibImpl(List jsfLibTuples) {
 		Iterator itTuple = jsfLibTuples.iterator();
 		while(itTuple.hasNext()) {
 			Tuple tuple = (Tuple)itTuple.next();
 			
 			if ( jsfLibReg != null ) {			
 				JSFLibrary jsfLib = jsfLibReg.getJSFLibraryByID(tuple.ID);				
 				if (jsfLib != null) {
 					return new JSFLibraryDecorator(jsfLib, tuple.selected, tuple.deploy);
 				}
 			} 
 		}	
 		return null;
 	}
 	
 	private void initJSFCompLibs(List jsfLibTuples, List JSFCompLibs) {
 		Iterator itTuple = jsfLibTuples.iterator();
 		while(itTuple.hasNext()) {
 			Tuple tuple = (Tuple)itTuple.next();
 			
 			if ( jsfLibReg != null ) {				
 				JSFLibrary jsfLib = jsfLibReg.getJSFLibraryByID(tuple.ID);				
 				if (jsfLib != null) {
 					JSFLibraryDecorator newJSFLibDcrt = new JSFLibraryDecorator(jsfLib, tuple.selected, tuple.deploy);
 					JSFCompLibs.add(newJSFLibDcrt);
 				}			
 			} 			
 		}
 	}
 	
 	private String generatePersistString(List list) {
 		JSFLibraryDecorator jsfLibItem;
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < list.size(); i++) {
 			jsfLibItem = (JSFLibraryDecorator)list.get(i);
 			sb = sb.append(jsfLibItem.generatePersistString());
 			sb.append(JSFLibraryConfigPersistData.EO_TUPLE);
 		}
 		return sb.toString();		
 	}	
 	
 	private List getTuples(String strJSFLibs) {
 		List list = new ArrayList(Collections.EMPTY_LIST);
 		
 		if (strJSFLibs != null) {
 			String patternStr = JSFLibraryConfigPersistData.EO_TUPLE;
 			String[] fields = strJSFLibs.split(patternStr);
 			if (strJSFLibs.length() > 0) {
 				Tuple tuple;
 				for (int i = 0; i < fields.length; i++) {
 					tuple = new Tuple(fields[i]);
 					list.add(tuple);
 				}
 			}			
 		}
 		
 		return list;
 	}
 	
 	
 	class Tuple {
 		private String ID;
 		private boolean selected = false;
 		private boolean deploy = true;
 		
 		Tuple(String ID, boolean selected, boolean deploy) {
 			this.ID = ID;
 			this.selected = selected;
 			this.deploy = deploy;
 		}
 		// parse tuple = ID:selected:deploy
 		Tuple(String tuple) {
 			String[] fields = tuple.split(JSFLibraryConfigPersistData.SPTR_TUPLE);
 			this.ID = fields[0];
 			this.selected = Boolean.valueOf(fields[1]).booleanValue();
 			this.deploy = Boolean.valueOf(fields[2]).booleanValue();
 		}
 		
 		String getID() {
 			return ID;
 		}
 		
 		boolean isSelected() {
 			return selected;
 		}
 		
 		boolean needDeploy() {
 			return deploy;
 		}		
 	}
 
 }
