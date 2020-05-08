 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.views.interfaces.categories;
 
 import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
 
 /**
  * Interface to be implemented by categorizable element adapters.
  */
 public interface ICategorizable {
 
 	/**
 	 * Defines the possible category operations.
 	 */
 	public enum OPERATION { ADD, REMOVE }
 
 	/**
 	 * Returns the unique categorizable id for the adapted element. The id
 	 * must not change once queried and must be the same across session.
 	 *
 	 * @return The unique categorizable element id, or <code>null</code>.
 	 */
 	public String getId();
 
 	/**
 	 * Returns if or if not the given operation is valid for the given parent
 	 * category and destination category.
 	 *
 	 * @param operation The operation. Must not be <code>null</code>.
	 * @param parentCategory The parent category or <code>null</code>.
 	 * @param category The destination category. Must not be <code>null</code>.
 	 *
 	 * @return <code>True</code> if the operation is valid, <code>false</code> otherwise.
 	 */
 	public boolean isValid(OPERATION operation, ICategory parentCategory, ICategory category);
 
 	/**
 	 * Returns if or if not the given operation is enabled for the given
 	 * destination category.
 	 *
 	 * @param operation The operation. Must not be <code>null</code>.
 	 * @param category The destination category. Must not be <code>null</code>.
 	 *
 	 * @return <code>True</code> if the operation is valid, <code>false</code> otherwise.
 	 */
 	public boolean isEnabled(OPERATION operation, ICategory category);
 }
