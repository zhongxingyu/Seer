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
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Widget;
 
 import org.eclipse.riena.beans.common.AbstractBean;
 import org.eclipse.riena.tests.UITestHelper;
 import org.eclipse.riena.ui.ridgets.IMasterDetailsDelegate;
 import org.eclipse.riena.ui.ridgets.IMasterDetailsRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IRidgetContainer;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper;
 import org.eclipse.riena.ui.ridgets.uibinding.DefaultBindingManager;
 import org.eclipse.riena.ui.ridgets.uibinding.IBindingManager;
 import org.eclipse.riena.ui.swt.MasterDetailsComposite;
 import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Tests for the class {@link MasterDetailsRidget}
  */
 public class MasterDetailsRidgetTest extends AbstractSWTRidgetTest {
 
 	private static final IBindingManager BINDING_MAN = new DefaultBindingManager(SWTBindingPropertyLocator
 			.getInstance(), SwtControlRidgetMapper.getInstance());
 	private final String[] columnProperties = { MDBean.PROPERTY_COLUMN_1, MDBean.PROPERTY_COLUMN_2 };
 	private final String[] columnHeaders = { "TestColumn1Header", "TestColumn2Header" };
 
 	private List<MDBean> input;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		input = createInput(3);
 		MasterDetailsRidget ridget = (MasterDetailsRidget) getRidget();
 		List<Object> uiControls = getWidget().getUIControls();
 		BINDING_MAN.injectRidgets(ridget, uiControls);
 		BINDING_MAN.bind(ridget, uiControls);
 		ridget.setDelegate(new MDDelegate());
 		getShell().setSize(300, 300);
 	}
 
 	@Override
 	protected Widget createWidget(Composite parent) {
 		return new MDWidget(parent, SWT.NONE);
 	}
 
 	@Override
 	protected IRidget createRidget() {
 		return new MasterDetailsRidget();
 	}
 
 	@Override
 	protected MDWidget getWidget() {
 		return (MDWidget) super.getWidget();
 	}
 
 	@Override
 	protected MasterDetailsRidget getRidget() {
 		return (MasterDetailsRidget) super.getRidget();
 	}
 
 	// test methods
 	///////////////
 
 	public void testRidgetMapping() {
 		SwtControlRidgetMapper mapper = SwtControlRidgetMapper.getInstance();
 		assertSame(MasterDetailsRidget.class, mapper.getRidgetClass(getWidget()));
 	}
 
 	public void testBindToModel() {
 		IMasterDetailsRidget ridget = getRidget();
 		MasterDetailsComposite composite = getWidget();
 		Table table = composite.getTable();
 
 		assertEquals(0, table.getItemCount());
 
 		bindToModel(false);
 
		assertEquals(0, table.getItemCount());
 
 		ridget.updateFromModel();
 
 		assertEquals(2, table.getColumnCount());
 		assertEquals(3, table.getItemCount());
 		assertEquals("TestColumn1Header", table.getColumn(0).getText());
 		assertEquals("TestColumn2Header", table.getColumn(1).getText());
 		assertContent(table, 3);
 	}
 
 	public void testSetUIControl() {
 		IMasterDetailsRidget ridget = getRidget();
 		Table table = getWidget().getTable();
 		MasterDetailsComposite mdComposite2 = (MasterDetailsComposite) createWidget(getShell());
 		Table table2 = mdComposite2.getTable();
 
 		assertEquals(0, table.getItemCount());
 
 		bindToModel(true);
 
 		assertEquals(3, table.getItemCount());
 
 		bindUIControl(ridget, mdComposite2);
 		input.remove(0);
 		ridget.updateFromModel();
 
 		assertEquals(3, table.getItemCount());
 		assertEquals(2, table2.getItemCount());
 
 		unbindUIControl(ridget, mdComposite2);
 		input.remove(0);
 		ridget.updateFromModel();
 
 		assertEquals(3, table.getItemCount());
 		assertEquals(2, table2.getItemCount());
 
 		try {
 			ridget.setUIControl(new Table(getShell(), SWT.MULTI));
 			fail();
 		} catch (RuntimeException rex) {
 			ok("does not allow SWT.MULTI");
 		}
 	}
 
 	public void testAddBean() {
 		MasterDetailsRidget ridget = getRidget();
 		MDWidget widget = getWidget();
 
 		bindToModel(true);
 		int oldSize = input.size();
 
 		ridget.setSelection(input.get(0));
 
 		assertEquals("TestR0C1", widget.txtColumn1.getText());
 		assertEquals("TestR0C2", widget.txtColumn2.getText());
 
 		ridget.handleAdd();
 
 		assertTrue(widget.txtColumn1.isFocusControl());
 
 		assertEquals(oldSize, input.size());
 		assertEquals("", widget.txtColumn1.getText());
 		assertEquals("", widget.txtColumn2.getText());
 
 		widget.txtColumn1.setFocus();
 		UITestHelper.sendString(widget.getDisplay(), "A\r");
 		widget.txtColumn2.setFocus();
 		UITestHelper.sendString(widget.getDisplay(), "B\r");
 
 		ridget.handleApply();
 
 		MDBean newEntry = input.get(oldSize);
 		assertEquals(oldSize + 1, input.size());
 		assertEquals("A", newEntry.column1);
 		assertEquals("B", newEntry.column2);
 
 		assertEquals(newEntry, ridget.getSelection());
 	}
 
 	public void testDeleteBean() {
 		MasterDetailsRidget ridget = getRidget();
 		MDWidget widget = getWidget();
 		Table table = widget.getTable();
 
 		bindToModel(true);
 
 		assertContent(table, 3);
 		assertEquals(3, input.size());
 
 		MDBean toDelete = input.get(1);
 		ridget.setSelection(toDelete);
 		ridget.handleRemove();
 
 		assertEquals(2, input.size());
 		assertFalse(input.contains(toDelete));
 	}
 
 	public void testModifyBean() {
 		MasterDetailsRidget ridget = getRidget();
 		MDWidget widget = getWidget();
 		Table table = widget.getTable();
 
 		bindToModel(true);
 
 		assertContent(table, 3);
 		assertEquals(3, input.size());
 
 		ridget.setSelection(input.get(1));
 		widget.txtColumn1.setFocus();
 		UITestHelper.sendString(widget.getDisplay(), "A\r");
 		widget.txtColumn2.setFocus();
 		UITestHelper.sendString(widget.getDisplay(), "B\r");
 		ridget.handleApply();
 
 		assertEquals(3, input.size());
 		assertEquals("A", input.get(1).getColumn1());
 		assertEquals("B", input.get(1).getColumn2());
 	}
 
 	public void testSetSelection() {
 		IMasterDetailsRidget ridget = getRidget();
 		bindToModel(true);
 
 		assertEquals(null, ridget.getSelection());
 
 		ridget.setSelection(input.get(0));
 
 		assertEquals(input.get(0), ridget.getSelection());
 
 		ridget.setSelection(null);
 
 		assertEquals(null, ridget.getSelection());
 	}
 
 	public void testSetSelectionUpdatesUI() {
 		IMasterDetailsRidget ridget = getRidget();
 		MDWidget widget = getWidget();
 
 		bindToModel(true);
 		ridget.setSelection(input.get(1));
 
 		assertEquals(1, widget.getTable().getSelectionCount());
 		assertEquals("TestR1C1", widget.txtColumn1.getText());
 		assertEquals("TestR1C2", widget.txtColumn2.getText());
 
 		ridget.setSelection(null);
 
 		assertEquals(0, widget.getTable().getSelectionCount());
 		assertEquals("", widget.txtColumn1.getText());
 		assertEquals("", widget.txtColumn2.getText());
 	}
 
 	public void testSetSelectionRevealsSelection() {
 		IMasterDetailsRidget ridget = getRidget();
 		MDWidget widget = getWidget();
 
 		input = createInput(42);
 		bindToModel(true);
 
 		assertEquals(0, widget.getTable().getTopIndex());
 
 		ridget.setSelection(input.get(30));
 
 		// topIndex > 0 means we scrolled to reveal row 30
 		assertTrue(widget.getTable().getTopIndex() > 0);
 	}
 
 	public void testUpdateFromModelPreservesSelection() {
 		IMasterDetailsRidget ridget = getRidget();
 		bindToModel(true);
 		MDBean item2 = input.get(2);
 
 		ridget.setSelection(item2);
 
 		assertSame(item2, ridget.getSelection());
 
 		input.remove(input.get(1));
 
 		assertSame(item2, ridget.getSelection());
 
 		ridget.updateFromModel();
 
 		assertSame(item2, ridget.getSelection());
 	}
 
 	public void testUpdateFromModelRemovesSelection() {
 		IMasterDetailsRidget ridget = getRidget();
 		bindToModel(true);
 		MDBean item2 = input.get(2);
 
 		ridget.setSelection(item2);
 
 		assertSame(item2, ridget.getSelection());
 
 		input.remove(input.get(2));
 
 		assertSame(item2, ridget.getSelection());
 
 		ridget.updateFromModel();
 
 		assertNull(ridget.getSelection());
 	}
 
 	public void testUpdateSelectionFromRidgetOnRebind() {
 		IMasterDetailsRidget ridget = getRidget();
 		MDWidget widget = getWidget();
 
 		unbindUIControl(ridget, widget);
 		bindToModel(true);
 		ridget.setSelection(input.get(0));
 
 		assertEquals(0, widget.getTable().getSelectionCount());
 		assertEquals("", widget.txtColumn1.getText());
 		assertEquals("", widget.txtColumn2.getText());
 
 		MDWidget widget2 = (MDWidget) createWidget(getShell());
 		bindUIControl(ridget, widget2);
 
 		assertEquals(1, widget2.getTable().getSelectionCount());
 		assertEquals("TestR0C1", widget2.txtColumn1.getText());
 		assertEquals("TestR0C2", widget2.txtColumn2.getText());
 	}
 
 	// helping methods
 	//////////////////
 
 	private void assertContent(Table table, int items) {
 		for (int i = 0; i < items; i++) {
 			String label0 = String.format("TestR%dC1", i);
 			String label1 = String.format("TestR%dC2", i);
 			assertEquals(label0, table.getItem(i).getText(0));
 			assertEquals(label1, table.getItem(i).getText(1));
 		}
 	}
 
 	private void bindToModel(boolean withUpdate) {
 		WritableList list = new WritableList(input, MDBean.class);
 		getRidget().bindToModel(list, MDBean.class, columnProperties, columnHeaders);
 		if (withUpdate) {
 			getRidget().updateFromModel();
 		}
 	}
 
 	private void bindUIControl(IMasterDetailsRidget ridget, MasterDetailsComposite control) {
 		ridget.setUIControl(control);
 		BINDING_MAN.bind(ridget, control.getUIControls());
 	}
 
 	private List<MDBean> createInput(int numItems) {
 		List<MDBean> result = new ArrayList<MDBean>();
 		for (int i = 0; i < numItems; i++) {
 			String c1 = String.format("TestR%dC1", i);
 			String c2 = String.format("TestR%dC2", i);
 			result.add(new MDBean(c1, c2));
 		}
 		return result;
 	}
 
 	private void unbindUIControl(IMasterDetailsRidget ridget, MasterDetailsComposite control) {
 		ridget.setUIControl(null);
 		BINDING_MAN.unbind(ridget, control.getUIControls());
 	}
 
 	// helping classes
 	//////////////////
 
 	/**
 	 * A bean with two String values; {@code column1} and {@code column2}.
 	 */
 	private static final class MDBean extends AbstractBean {
 
 		private static final String PROPERTY_COLUMN_1 = "column1";
 		private static final String PROPERTY_COLUMN_2 = "column2";
 
 		private String column1;
 		private String column2;
 
 		MDBean(String column1, String column2) {
 			this.column1 = column1;
 			this.column2 = column2;
 		}
 
 		public String getColumn1() {
 			return column1;
 		}
 
 		public String getColumn2() {
 			return column2;
 		}
 
 		public void setColumn1(String column1) {
 			firePropertyChanged("column1", this.column1, this.column1 = column1);
 		}
 
 		public void setColumn2(String column2) {
 			firePropertyChanged("column2", this.column2, this.column2 = column2);
 		}
 
 		@Override
 		public String toString() {
 			return String.format("[%s, %s]", column1, column2);
 		}
 	}
 
 	/**
 	 * A MasterDetailsComposite with a details area containing two text fields.
 	 */
 	private static final class MDWidget extends MasterDetailsComposite {
 
 		private Text txtColumn1;
 		private Text txtColumn2;
 
 		public MDWidget(Composite parent, int style) {
 			super(parent, style, SWT.BOTTOM);
 		}
 
 		@Override
 		protected void createDetails(Composite parent) {
 			GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parent);
 			GridDataFactory hFill = GridDataFactory.fillDefaults().grab(true, false);
 
 			txtColumn1 = UIControlsFactory.createText(parent);
 			hFill.applyTo(txtColumn1);
 			addUIControl(txtColumn1, "txtColumn1");
 
 			txtColumn2 = UIControlsFactory.createText(parent);
 			hFill.applyTo(txtColumn2);
 			addUIControl(txtColumn2, "txtColumn2");
 		}
 	}
 
 	/**
 	 * Implements a delegate with two text ridgets. This class is a companion
 	 * class to {@link MDBean} and {@link MDWidget}.
 	 */
 	private static final class MDDelegate implements IMasterDetailsDelegate {
 
 		private final MDBean workingCopy = createWorkingCopy();
 
 		public void configureRidgets(IRidgetContainer container) {
 			ITextRidget txtColumn1 = (ITextRidget) container.getRidget("txtColumn1");
 			txtColumn1.bindToModel(workingCopy, MDBean.PROPERTY_COLUMN_1);
 			txtColumn1.updateFromModel();
 
 			ITextRidget txtColumn2 = (ITextRidget) container.getRidget("txtColumn2");
 			txtColumn2.bindToModel(workingCopy, MDBean.PROPERTY_COLUMN_2);
 			txtColumn2.updateFromModel();
 		}
 
 		public Object copyBean(Object source, Object target) {
 			MDBean from = source == null ? createWorkingCopy() : (MDBean) source;
 			MDBean to = target == null ? createWorkingCopy() : (MDBean) target;
 			to.setColumn1(from.getColumn1());
 			to.setColumn2(from.getColumn2());
 			return to;
 		}
 
 		public MDBean createWorkingCopy() {
 			return new MDBean("", "");
 		}
 
 		public MDBean getWorkingCopy() {
 			return workingCopy;
 		}
 
 		public boolean isChanged(Object source, Object target) {
 			return true;
 		}
 
 		public String isValid(IRidgetContainer container) {
 			return null;
 		}
 
 		public void updateDetails(IRidgetContainer container) {
 			for (IRidget ridget : container.getRidgets()) {
 				ridget.updateFromModel();
 			}
 		}
 	}
 
 }
