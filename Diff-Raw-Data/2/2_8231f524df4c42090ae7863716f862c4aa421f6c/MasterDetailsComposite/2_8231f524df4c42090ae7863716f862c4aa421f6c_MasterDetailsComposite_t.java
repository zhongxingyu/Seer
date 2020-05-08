 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 
 import org.eclipse.riena.ui.ridgets.IMasterDetailsRidget;
 
 /**
  * This composite contains a Table widget (the "master") of n columns, as well
 * as new, remove and update buttons. It also contains an arbitrary composite
  * (the "details"), which is updated automatically when the selected row in the
  * table changes.
  * <p>
  * See {@link AbstractMasterDetailsComposite} for details.
  * 
  * @see IMasterDetailsRidget
  * 
  * @since 1.2
  */
 public class MasterDetailsComposite extends AbstractMasterDetailsComposite {
 
 	/**
 	 * Creates a master detail composite with the given style and SWT.BOTTOM
 	 * orientation. See parent class for details.
 	 * 
 	 * @see AbstractMasterDetailsComposite#AbstractMasterDetailsComposite(Composite,
 	 *      int, int)
 	 */
 	public MasterDetailsComposite(final Composite parent, final int style) {
 		this(parent, style, SWT.BOTTOM);
 	}
 
 	/**
 	 * Creates a master detail composite with the given style and SWT.BOTTOM
 	 * orientation. See parent class for details.
 	 * 
 	 * @see AbstractMasterDetailsComposite#AbstractMasterDetailsComposite(Composite,
 	 *      int, int)
 	 */
 	public MasterDetailsComposite(final Composite parent, final int style, final int orientation) {
 		super(parent, style, orientation);
 	}
 
 	/**
 	 * Returns the Table control of the 'master' area/
 	 * 
 	 * @return a Table; never null
 	 */
 	@Override
 	public final Table getTable() {
 		return (Table) super.getTable();
 	}
 
 	// protected methods
 	////////////////////
 
 	/**
 	 * The default implementation will creates a table with zero columns.
 	 * <p>
 	 * Subclasses may override, call super to obtain the {@link Table} and
 	 * create {@link TableColumn}s as needed.
 	 * <p>
 	 * Note: if the number of columns in the Table does not match the number of
 	 * Columns in the Ridget, the MasterDetailsRidget will create the
 	 * appropriate number of columns. The automatically created columns will
 	 * share the full width of the table.
 	 */
 	@Override
 	protected Table createTable(final Composite tableComposite, final TableColumnLayout layout) {
 		final Table table = new Table(tableComposite, getTableStyle());
 		// Check that table does not allow multiple selection
 		Assert.isLegal((table.getStyle() & SWT.MULTI) == 0);
 		// Do not create columns here or change the TableColumnLayout.
 		// Clients assume both are pristine so they can configure it to their liking
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		return table;
 	}
 
 	/**
 	 * Returns the style bits for the 'table'. Subclasses may override, but has
 	 * to return a value that is supported by the {@link Table}.
 	 * <p>
 	 * Never return SWT.MULTI. By design, this widget does not support multiple
 	 * selection.
 	 * 
 	 * @return SWT.SINGLE | SWT.FULL_SELECTION
 	 * @since 2.0
 	 */
 	protected int getTableStyle() {
 		return SWT.SINGLE | SWT.FULL_SELECTION;
 	}
 
 }
