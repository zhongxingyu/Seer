 /*******************************************************************************
  * Copyright (c) 2008, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.context;
 
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.change.util.ChangeRecorder;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettings;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  * use {@link ExtendedPropertiesEditingContext} instead of this interface.
  */
 public interface PropertiesEditingContext {
 
 	/**
 	 * Intialize the ChangeRecorder of the editing session.
 	 */
 	public void initializeRecorder();
 	
 	/**
 	 * @return the changeRecorder of the editing session
 	 */
 	public ChangeRecorder getChangeRecorder();
 
 	/**
 	 * @return the currently edited {@link EObject}.
 	 */
 	public EObject getEObject();
 	
 	/**
 	 * @return the resourceSet where to work.
 	 */
 	public ResourceSet getResourceSet();
 
 	/**
 	 * @return the {@link AdapterFactory} to use during the editing.
 	 */
 	public AdapterFactory getAdapterFactory();
 
 	/**
 	 * dispose the context
 	 */
 	public void dispose();
 	
 	/**
 	 * @return the parentContext
 	 */
 	public PropertiesEditingContext getParentContext();
 
 	/**
 	 * @param eObject
 	 */
	public abstract void seteObject(EObject eObject);
 	
 }
