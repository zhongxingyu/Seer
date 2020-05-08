 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 
 package org.eclipse.dltk.ui.tests.navigator.scriptexplorer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.internal.ui.scriptview.ScriptExplorerPart;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.PreferenceConstants;
 import org.eclipse.dltk.ui.viewsupport.ProblemTreeViewer;
 import org.eclipse.jface.viewers.IContentProvider;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 
 
 /**
  * Helper to test the PackageExplorerContentProvider.
  * 
 	 *
  */
 public class MockPluginView extends ScriptExplorerPart {
 
 	TreeViewer fViewer;
 	private ITreeContentProvider contentProvider;
 	private boolean fRefreshHappened;
 	public List fRefreshedObjects;
 	private boolean fRemoveHappened;
 	private boolean fAddHappened;
 	
 	private List fRemovedObject;
 	private Object fAddedObject;
 	private Object fAddedParentObject;
 
 	/**
 	 * Constructor for MockPluginView.
 	 */
 	public MockPluginView() {
 		super();
 		fRefreshedObjects= new ArrayList();
 		fRemovedObject= new ArrayList();
 	}
 	
 	/**
 	 * Creates only the viewer and the content provider.
 	 * 
 	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
 	 */
 	public void createPartControl(Composite parent) {
 	
 		//create viewer
 		fViewer= createViewer(parent);
 		
 		//create my contentProvider
 		contentProvider= createContentProvider();
 		contentProvider.inputChanged(fViewer, null, null);
 		
 		//set content provider
 		fViewer.setContentProvider(contentProvider);
 		
 	}
 	
	private TreeViewer createViewer(Composite parent) {
 		return new TestProblemTreeViewer(parent, SWT.MULTI);
 	}
 
 	public void dispose() {
 		if (fViewer != null) {
 			IContentProvider p = fViewer.getContentProvider();
 			if(p!=null)	
 				p.dispose();
 		}
 		
 		super.dispose();
 	}
 
 	/*
 	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
 	 */
 	public void setFocus() {
 	}
 	
 	public TreeViewer getTreeViewer(){
 		return fViewer;
 	}
 	
 
 	protected IModelElement findElementToSelect(IModelElement je) {
 		return null;
 	}
 
 	protected String getHelpContextId() {
 		return null;
 	}
 
 	protected boolean isValidInput(Object element) {
 		return false;
 	}
 	
 	private class TestProblemTreeViewer extends ProblemTreeViewer{
 		
 		public TestProblemTreeViewer(Composite parent, int flag){
 			super(parent,flag);
 		}
 		
 		public void refresh(Object object){
 			fRefreshHappened= true;
 			fRefreshedObjects.add(object);
 		}
 		
 		public void refresh(final Object element, final boolean updateLabels) {
 			fRefreshHappened= true;
 			fRefreshedObjects.add(element);
 		}
 		
 		public void remove(Object object) {
 			fRemoveHappened= true;
 			fRemovedObject.add(object);
 		}
 		
 		public void add(Object parentObject, Object object){
 			fAddHappened= true;
 			fAddedObject= object;
 			fAddedParentObject= parentObject;
 		}
 	}
 
 	/**
 	 * Returns the refreshed object.
 	 * @return Object
 	 */
 	public boolean wasObjectRefreshed(Object c) {
 		return fRefreshedObjects.contains(c);
 	}
 	
 	public List getRefreshedObject(){
 		return fRefreshedObjects;	
 	}
 
 	/**
 	 * Returns the object added to the tree viewer
 	 * @return Object
 	 */
 	public Object getParentOfAddedObject() {
 		return fAddedParentObject;
 	}
 
 	/**
 	 * Returns true if something was added to the viewer
 	 * @return boolean
 	 */
 	public boolean hasAddHappened() {
 		return fAddHappened;
 	}
 
 	/**
 	 * Returns true if an object was removed from the viewer
 	 * @return boolean
 	 */
 	public boolean hasRemoveHappened() {
 		return fRemoveHappened;
 	}	
 	/**
 	 * Returns the object removed from the viewer
 	 * @return Object
 	 */
 	public List getRemovedObject() {
 		return fRemovedObject;
 	}
 
 	/**
 	 * Returns the object added to the viewer
 	 * @return Object
 	 */
 	public Object getAddedObject() {
 		return fAddedObject;
 	}
 	
 	/**
 	 * Returns true if a refresh happened
 	 * @return boolean
 	 */
 	public boolean hasRefreshHappened() {
 		return fRefreshHappened;
 	}
 	
 	/**
 	 * Sets the folding preference.
 	 * @param fold
 	 */
 	public void setFolding(boolean fold) {
 		DLTKUIPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.APPEARANCE_FOLD_PACKAGES_IN_PACKAGE_EXPLORER, fold);
 	}		
 }
