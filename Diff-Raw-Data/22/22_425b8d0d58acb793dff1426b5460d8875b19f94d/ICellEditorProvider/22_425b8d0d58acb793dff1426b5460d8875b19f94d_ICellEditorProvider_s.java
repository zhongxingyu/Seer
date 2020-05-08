 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mgorning - Bug 347262 - DirectEditingFeature with TYPE_DIALOG type
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.platform;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.swt.widgets.Composite;
 
 /**
  * The Interface ICellEditorProvider.
  * 
  * Clients can use this interface to provide their own cell editors for direct
  * editing. The direct editing feature must simply implement this interface.
  * 
  * @since 0.9
  */
 public interface ICellEditorProvider {
 
 	/**
 	 * Creates the cell editor on the given parent composite.
 	 * 
 	 * @param parent
 	 *            the parent composite to create the cell editor on
 	 * @return the newly created cell editor
 	 */
 	CellEditor createCellEditor(Composite parent);
 
 	/**
 	 * Relocates a CellEditor.
 	 * 
 	 * @param cellEditor
 	 *            the CellEditor
 	 * @param figure
 	 *            the text figure on which direct editing was initiated
 	 */
 	void relocate(CellEditor cellEditor, IFigure figure);
 }
