 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.platform;
 
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.graphiti.datatypes.IDimension;
 import org.eclipse.graphiti.datatypes.ILocation;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 
 /**
  * The Interface IDiagramEditor.
  */
 public interface IDiagramEditor {
 
 	/**
 	 * Select all the given pictogram elements in the editor.
 	 * 
 	 * @param pictogramElements
 	 *            the pictogram elements
 	 */
 	void selectPictogramElements(PictogramElement[] pictogramElements);
 
 	/**
 	 * Get all pictogram elements currently selected.
 	 * 
 	 * @return all selected pictogram elements
 	 */
 	PictogramElement[] getSelectedPictogramElements();
 
 	/**
 	 * Sets the pictogram element which should be selected after the editor
 	 * refresh.
 	 * 
 	 * @param pictogramElement
 	 *            the pictogram element
 	 */
 	void setPictogramElementForSelection(PictogramElement pictogramElement);
 
 	/**
 	 * Sets the pictogram elements which should be selected after the editor
 	 * refresh.
 	 * 
 	 * @param pictogramElements
 	 *            the pictogram element
 	 */
 	void setPictogramElementsForSelection(PictogramElement[] pictogramElements);
 
 	/**
 	 * Gets the transactional editing domain.
 	 * 
 	 * @return transactional editing domain which is linked to the editor
 	 */
 	TransactionalEditingDomain getEditingDomain();
 
 	/**
 	 * Gets the resource set.
 	 * 
 	 * @return resource set
 	 */
 	ResourceSet getResourceSet();
 
 	/**
 	 * Gets the diagram type provider.
 	 * 
 	 * @return the diagram type provider
 	 */
 	IDiagramTypeProvider getDiagramTypeProvider();
 
 	/**
 	 * Refresh.
 	 */
 	void refresh();
 
 	/**
	 * In opposite to <code>refresh()</code> where the diagram will globally
	 * refreshed this method only refreshes the graphical representation for the
	 * given pictogramm element.
	 * 
	 * @param pe
	 *            an active pictogram element
	 */
	void refresh(PictogramElement pe);

	/**
 	 * Gets the current size.
 	 * 
 	 * @return the current size of the diagram in the editor
 	 */
 	IDimension getCurrentSize();
 
 	/**
 	 * Checks if is dirty.
 	 * 
 	 * @return true, if editor is dirty
 	 */
 	boolean isDirty();
 
 	/**
 	 * Refreshes the title tool tip text of this part.
 	 */
 	void refreshTitleToolTip();
 
 	/**
 	 * Refreshes all rendering decorators for the given pictogram element. That
 	 * means: 1. delete current decorators 2. ask the toolbehaviour provider for
 	 * decorator data 3. create new decorators with this data and render this
 	 * new decorators
 	 * 
 	 * @param pe
 	 *            pictogram element
 	 */
 	void refreshRenderingDecorators(PictogramElement pe);
 
 	/**
 	 * Refreshes the editor's palette.
 	 */
 	void refreshPalette();
 	
 	/**
 	 * Gets the mouse location.
 	 * 
 	 * @return the mouse location
 	 */
 	ILocation getCurrentMouseLocation();
 }
