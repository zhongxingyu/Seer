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
 package org.eclipse.riena.sample.snippets;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.nebula.widgets.compositetable.AbstractNativeHeader;
 import org.eclipse.swt.nebula.widgets.compositetable.CompositeTable;
 import org.eclipse.swt.nebula.widgets.compositetable.ResizableGridRowLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import org.eclipse.riena.beans.common.Person;
 import org.eclipse.riena.beans.common.PersonFactory;
 import org.eclipse.riena.ui.common.IComplexComponent;
 import org.eclipse.riena.ui.ridgets.AbstractCompositeRidget;
 import org.eclipse.riena.ui.ridgets.ICompositeTableRidget;
 import org.eclipse.riena.ui.ridgets.IRowRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.swt.SwtRidgetFactory;
 import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;
 
 /**
  * A composite table ridget with a row of text fields.
  */
 public final class SnippetCompositeTableRidget001 {
 
 	private SnippetCompositeTableRidget001() {
 		// "utility class"
 	}
 
 	/**
 	 * Header for a {@link CompositeTable} widget.
 	 */
 	private static final class Header extends AbstractNativeHeader {
 		/**
 		 * Must have a two-arguments constructor.
 		 * 
 		 * @param parent
 		 *            the parent Composite; non null
 		 * @param style
 		 *            the style bits
 		 */
 		public Header(Composite parent, int style) {
 			super(parent, style);
 			setWeights(new int[] { 100, 100 });
 			setColumnText(new String[] { "First Name", "Last Name" }); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 
 	/**
 	 * Row for a {@link CompositeTable} widget.
 	 */
 	private static final class Row extends Composite implements IComplexComponent {
 		private final List<Object> controls = new ArrayList<Object>();
 
 		/**
 		 * Must have a two-arguments constructor.
 		 * 
 		 * @param parent
 		 *            the parent Composite; non null
 		 * @param style
 		 *            the style bits
 		 */
 		public Row(Composite parent, int style) {
 			super(parent, style);
 			this.setLayout(new ResizableGridRowLayout());
 			Text txtFirst = new Text(this, SWT.BORDER);
 			addUIControl(txtFirst, "first"); //$NON-NLS-1$
 			Text txtLast = new Text(this, SWT.BORDER);
 			addUIControl(txtLast, "last"); //$NON-NLS-1$
 		}
 
 		public List<Object> getUIControls() {
 			return Collections.unmodifiableList(controls);
 		}
 
 		private void addUIControl(Object uiControl, String bindingId) {
 			controls.add(uiControl);
 			// Set's binding property into the widget.
 			// Need this for the widget <-> ridget binding
 			SWTBindingPropertyLocator.getInstance().setBindingProperty(uiControl, bindingId);
 		}
 	}
 
 	/**
 	 * Row for a {@link ICompositeTableRidget}.
 	 * <p>
 	 * Implementation note: class must be public and have a zero-argument public
 	 * constructor. Instances will be created by reflection.
 	 */
 	public static final class RowRidget extends AbstractCompositeRidget implements IRowRidget {
 		private Person rowData;
 
 		public void setData(Object rowData) {
 			this.rowData = (Person) rowData;
 		}
 
 		@Override
 		public void configureRidgets() {
 			ITextRidget txtFirst = (ITextRidget) getRidget("first"); //$NON-NLS-1$
 			txtFirst.bindToModel(rowData, Person.PROPERTY_FIRSTNAME);
 			txtFirst.updateFromModel();
 
 			ITextRidget txtLast = (ITextRidget) getRidget("last"); //$NON-NLS-1$
 			txtLast.bindToModel(rowData, Person.PROPERTY_LASTNAME);
 			txtLast.updateFromModel();
 		}
 	}
 
 	public static void main(String[] args) {
 		Display display = Display.getDefault();
 		Shell shell = new Shell(display);
 		shell.setLayout(new FillLayout());
 
 		CompositeTable table = new CompositeTable(shell, SWT.NONE);
 		new Header(table, SWT.NONE);
 		new Row(table, SWT.NONE);
 		table.setRunTime(true);
 
 		ICompositeTableRidget ridget = (ICompositeTableRidget) SwtRidgetFactory.createRidget(table);
 		WritableList input = new WritableList(PersonFactory.createPersonList(), Person.class);
 		ridget.bindToModel(input, Person.class, RowRidget.class);
 		ridget.updateFromModel();
 
 		shell.setSize(400, 160);
 		shell.open();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 
 		display.dispose();
 	}
 }
