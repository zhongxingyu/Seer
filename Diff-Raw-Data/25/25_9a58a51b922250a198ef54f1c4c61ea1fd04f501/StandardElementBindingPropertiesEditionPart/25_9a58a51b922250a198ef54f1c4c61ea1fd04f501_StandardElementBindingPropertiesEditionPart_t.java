 /**
  *  Copyright (c) 2008 - 2010 Obeo.
  *  All rights reserved. This program and the accompanying materials
  *  are made available under the terms of the Eclipse Public License v1.0
  *  which accompanies this distribution, and is available at
  *  http://www.eclipse.org/legal/epl-v10.html
  *  
  *  Contributors:
  *      Obeo - initial API and implementation
  *
  */
 package org.eclipse.emf.eef.mapping.parts;
 
 // Start of user code for imports
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.eef.runtime.ui.widgets.ButtonsModeEnum;
 import org.eclipse.emf.eef.runtime.ui.widgets.eobjflatcombo.EObjectFlatComboSettings;
 import org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableSettings;
 import org.eclipse.jface.viewers.ViewerFilter;


 
 // End of user code
 
 /**
  * @author <a href="mailto:nathalie.lepine@obeo.fr">Nathalie Lepine</a>
  * 
  */
 public interface StandardElementBindingPropertiesEditionPart {
 
 	/**
 	 * @return the name
 	 * 
 	 */
 	public String getName();
 
 	/**
 	 * Defines a new name
 	 * @param newValue the new name to set
 	 * 
 	 */
 	public void setName(String newValue);
 
 
 	/**
 	 * @return the model
 	 * 
 	 */
 	public EObject getModel();
 
 	/**
 	 * Init the model
 	 * @param settings the combo setting
 	 */
 	public void initModel(EObjectFlatComboSettings settings);
 
 	/**
 	 * Defines a new model
 	 * @param newValue the new model to set
 	 * 
 	 */
 	public void setModel(EObject newValue);
 
 	/**
 	 * Defines the button mode
 	 * @param newValue the new mode to set
 	 * 
 	 */
 	public void setModelButtonMode(ButtonsModeEnum newValue);
 
 	/**
 	 * Adds the given filter to the model edition editor.
 	 * 
 	 * @param filter
 	 *            a viewer filter
 	 * @see org.eclipse.jface.viewers.StructuredViewer#addFilter(ViewerFilter)
 	 * 
 	 */
 	public void addFilterToModel(ViewerFilter filter);
 
 	/**
 	 * Adds the given filter to the model edition editor.
 	 * 
 	 * @param filter
 	 *            a viewer filter
 	 * @see org.eclipse.jface.viewers.StructuredViewer#addFilter(ViewerFilter)
 	 * 
 	 */
 	public void addBusinessFilterToModel(ViewerFilter filter);
 
 
 
 
 	/**
 	 * Init the views
 	 * @param settings settings for the views ReferencesTable 
 	 */
 	public void initViews(ReferencesTableSettings settings);
 
 	/**
 	 * Update the views
 	 * @param newValue the views to update
 	 * 
 	 */
 	public void updateViews();
 
 	/**
 	 * Adds the given filter to the views edition editor.
 	 * 
 	 * @param filter
 	 *            a viewer filter
 	 * @see org.eclipse.jface.viewers.StructuredViewer#addFilter(ViewerFilter)
 	 * 
 	 */
 	public void addFilterToViews(ViewerFilter filter);
 
 	/**
 	 * Adds the given filter to the views edition editor.
 	 * 
 	 * @param filter
 	 *            a viewer filter
 	 * @see org.eclipse.jface.viewers.StructuredViewer#addFilter(ViewerFilter)
 	 * 
 	 */
 	public void addBusinessFilterToViews(ViewerFilter filter);
 
 	/**
 	 * @return true if the given element is contained inside the views table
 	 * 
 	 */
 	public boolean isContainedInViewsTable(EObject element);
 
 
 
 
 
 	/**
 	 * Returns the internationalized title text.
 	 * 
 	 * @return the internationalized title text.
 	 * 
 	 */
 	public String getTitle();
 
 	// Start of user code for additional methods
	
	// End of user code
 
 }
