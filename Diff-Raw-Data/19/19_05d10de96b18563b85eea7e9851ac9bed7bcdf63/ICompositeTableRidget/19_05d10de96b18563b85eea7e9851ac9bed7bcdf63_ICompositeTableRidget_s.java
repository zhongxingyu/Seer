 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets;
 
 import java.util.Comparator;
 
 import org.eclipse.core.databinding.observable.list.IObservableList;
 
 import org.eclipse.riena.ui.common.ISortableByColumn;
 
 /**
  * Ridget for a complex table.
  */
 public interface ICompositeTableRidget extends ISelectableIndexedRidget, ISortableByColumn {
 
 	/**
 	 * Bind the composite table to the given model data and specify which
 	 * composite to use for the rows.
 	 * 
 	 * @param rowObservables
 	 *            An observable list of objects (non-null).
 	 * @param rowClass
 	 *            The class of the objects in the list.
 	 * @param rowRidgetClass
	 *            A class (extending Composite) which will be instantiated for
	 *            each row. It must provide a public constructor with these
	 *            parameters: {@code Composite parent, int style}.
 	 */
	// TODO [ev] this javadoc is SWT specific
 	void bindToModel(IObservableList rowObservables, Class<? extends Object> rowClass,
 			Class<? extends Object> rowRidgetClass);
 
 	/**
 	 * Bind the composite table to the given model data and specify which
 	 * composite to use for the rows.
 	 * 
 	 * @param listHolder
 	 *            An object that has a property with a list of objects.
 	 * @param listPropertyName
 	 *            Property for accessing the list of objects.
 	 * @param rowClass
 	 *            The class of the objects in the list.
 	 * @param rowRidgetClass
	 *            A class (extending Composite) which will be instantiated for
	 *            each row. It must provide a public constructor with these
	 *            parameters: {@code Composite parent, int style}.
 	 */
	// TODO [ev] this javadoc is SWT specific
 	void bindToModel(Object listHolder, String listPropertyName, Class<? extends Object> rowClass,
 			Class<? extends Object> rowRidgetClass);
 
 	/**
 	 * Set the {@link Comparator} to be used when sorting column at columnIndex.
 	 * 
 	 * @param columnIndex
 	 *            a columnIndex in the allowed range: ( 0 &lt;= columnIndex &lt;
 	 *            numColumns )
 	 * @param comparator
 	 *            a Comparator instance; may be null
 	 * @throws RuntimeException
 	 *             if columnIndex is out of range
 	 */
 	void setComparator(int columnIndex, Comparator<Object> comparator);
 }
