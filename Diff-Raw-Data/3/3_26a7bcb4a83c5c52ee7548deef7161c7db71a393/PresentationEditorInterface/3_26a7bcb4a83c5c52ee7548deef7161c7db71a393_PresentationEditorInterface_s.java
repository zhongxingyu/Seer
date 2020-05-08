 /*******************************************************************************
  * Copyright (c) 2012 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.pror.editor.presentation.service;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.CellEditor;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.DatatypeDefinition;
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 import org.eclipse.rmf.reqif10.pror.configuration.provider.ProrPresentationConfigurationItemProvider;
 import org.eclipse.rmf.reqif10.pror.util.PresentationEditInterface;
 
 /**
  * Interface that subclasses of the abstract
  * {@link ProrPresentationConfigurationItemProvider} may optionally implement to
  * provide GUID-related functionality for Presentations. This interface is used
  * like {@link PresentationEditInterface}.
  * <p>
  * 
  * @author jastram
  * 
  */
 public interface PresentationEditorInterface {
 
 	/**
 	 * Returns an {@link IProrCellRenderer} appropriate for the
 	 * {@link DatatypeDefinition} associated with this configuration. May return
 	 * null (then the default renderer is used).
 	 */
 	IProrCellRenderer getCellRenderer(AttributeValue av);
 
 	/**
 	 * Returns the {@link CellEditor} appropriate for the
 	 * {@link DatatypeDefinition} associated with this configuration. May return
 	 * null (then the default editor is used).
 	 */
 	CellEditor getCellEditor(AgileGrid agileGrid, EditingDomain editingDomain,
 			AttributeValue av, SpecElementWithAttributes parent,
 			Object affectedObject);
 
 	/**
 	 * If this method returns a non-null value, it is matched against the ReqIF
 	 * data types. If there is no default for the given type yet, it is
 	 * installed as the default. If there is already a default, ProR asks the
 	 * user whether the current default should be replaced.
 	 * <p>
 	 * 
 	 * A default makes sense for Presentations that improve rendering. For
 	 * instance, a renderer for XHTML can replace the default renderer, which
 	 * has no rich text capabilities.
 	 * <p>
 	 * 
	 * Note that the user cannot configure a default!
 	 * <p>
 	 */
 	Class<? extends DatatypeDefinition> suggestAsDefault();
 
 }
