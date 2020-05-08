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
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Widget;
 
 import org.eclipse.riena.beans.common.AbstractBean;
import org.eclipse.riena.core.RienaStatus;
 import org.eclipse.riena.internal.ui.swt.test.UITestHelper;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.core.marker.ValidationTime;
 import org.eclipse.riena.ui.ridgets.AbstractMasterDetailsDelegate;
 import org.eclipse.riena.ui.ridgets.IMasterDetailsRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IRidgetContainer;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.swt.AbstractMasterDetailsRidget;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper;
 import org.eclipse.riena.ui.ridgets.uibinding.DefaultBindingManager;
 import org.eclipse.riena.ui.ridgets.uibinding.IBindingManager;
 import org.eclipse.riena.ui.ridgets.validation.MinLength;
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
 	private MDDelegate delegate;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		input = createInput(3);
 		IMasterDetailsRidget ridget = getRidget();
 		List<Object> uiControls = getWidget().getUIControls();
 		BINDING_MAN.injectRidgets(ridget, uiControls);
 		BINDING_MAN.bind(ridget, uiControls);
 		delegate = new MDDelegate();
 		ridget.setDelegate(delegate);
 		getShell().setSize(300, 300);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		delegate = null;
 		input = null;
 		super.tearDown();
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
 		ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		ITextRidget txtColumn2 = ridget.getRidget(ITextRidget.class, "txtColumn2");
 
 		ridget.setApplyTriggersNew(false);
 		bindToModel(true);
 		int oldSize = input.size();
 
 		ridget.setSelection(input.get(0));
 
 		assertEquals("TestR0C1", widget.txtColumn1.getText());
 		assertEquals("TestR0C2", widget.txtColumn2.getText());
 
 		ridget.handleAdd();
 		assertTrue(txtColumn1.isEnabled());
 		assertTrue(txtColumn2.isEnabled());
 
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
 
 		// apply triggers 'New' = false
 		assertEquals(newEntry, ridget.getSelection());
 		assertFalse(txtColumn1.isEnabled());
 		assertFalse(txtColumn2.isEnabled());
 		assertEquals("", widget.txtColumn1.getText());
 		assertEquals("", widget.txtColumn2.getText());
 	}
 
 	public void testAddBeanAndApplyTriggersNew() {
 		MasterDetailsRidget ridget = getRidget();
 		MDWidget widget = getWidget();
 		ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		ITextRidget txtColumn2 = ridget.getRidget(ITextRidget.class, "txtColumn2");
 
 		ridget.setApplyTriggersNew(true);
 		bindToModel(true);
 		int oldSize = input.size();
 
 		ridget.setSelection(input.get(0));
 
 		assertEquals("TestR0C1", widget.txtColumn1.getText());
 		assertEquals("TestR0C2", widget.txtColumn2.getText());
 
 		ridget.handleAdd();
 		assertTrue(txtColumn1.isEnabled());
 		assertTrue(txtColumn2.isEnabled());
 
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
 
 		// apply triggers 'New' = true
 		assertNull(ridget.getSelection());
 		assertTrue(txtColumn1.isEnabled());
 		assertTrue(txtColumn2.isEnabled());
 		assertEquals("", widget.txtColumn1.getText());
 		assertEquals("", widget.txtColumn2.getText());
 	}
 
 	public void testDeleteBean() {
 		MasterDetailsRidget ridget = getRidget();
 		MDWidget widget = getWidget();
 		Table table = widget.getTable();
 		ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		ITextRidget txtColumn2 = ridget.getRidget(ITextRidget.class, "txtColumn2");
 
 		bindToModel(true);
 
 		assertEquals(3, input.size());
 		assertContent(table, 3);
 
 		MDBean toDelete = input.get(2);
 		ridget.setSelection(toDelete);
 		assertTrue(txtColumn1.isEnabled());
 		assertTrue(txtColumn2.isEnabled());
 		ridget.handleRemove();
 
 		assertEquals(2, input.size());
 		assertContent(table, 2);
 		assertFalse(input.contains(toDelete));
 		assertFalse(txtColumn1.isEnabled());
 		assertFalse(txtColumn2.isEnabled());
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
 
 	public void testSetSelectionFiresEvents() {
 		IMasterDetailsRidget ridget = getRidget();
 		MDBean item0 = input.get(0);
 		FTPropertyChangeListener listener = new FTPropertyChangeListener();
 
 		bindToModel(true);
 		ridget.addPropertyChangeListener(ISelectableRidget.PROPERTY_SELECTION, listener);
 
 		ridget.setSelection(item0);
 		java.util.List<?> oldSelection = Collections.EMPTY_LIST;
 		java.util.List<?> newSelection = Arrays.asList(new Object[] { item0 });
 		assertPropertyChangeEvent(1, oldSelection, newSelection, listener);
 
 		ridget.setSelection(item0);
 		assertEquals(1, listener.count);
 
 		MDBean item1 = input.get(1);
 		ridget.setSelection(item1);
 		oldSelection = newSelection;
 		newSelection = Arrays.asList(new Object[] { item1 });
 		assertPropertyChangeEvent(2, oldSelection, newSelection, listener);
 
 		ridget.setSelection(null);
 		oldSelection = newSelection;
 		newSelection = Collections.EMPTY_LIST;
 		assertPropertyChangeEvent(3, oldSelection, newSelection, listener);
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code handleSelectionChange(Object)}.
 	 */
 	public void testHandleSelectionChange() {
 		IMasterDetailsRidget ridget = getRidget();
 		bindToModel(true);
 		ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		ITextRidget txtColumn2 = ridget.getRidget(ITextRidget.class, "txtColumn2");
 
 		MDBean item0 = input.get(0);
 		ridget.setSelection(item0);
 
 		assertTrue(txtColumn1.isEnabled());
 		assertTrue(txtColumn2.isEnabled());
 
 		ridget.setSelection(null);
 
 		assertFalse(txtColumn1.isEnabled());
 		assertFalse(txtColumn2.isEnabled());
 
 		delegate.isTxtColumn1IsEnabled = false;
 		ridget.setSelection(item0);
 
 		assertFalse(txtColumn1.isEnabled());
 		assertTrue(txtColumn2.isEnabled());
 
 		ridget.setSelection(null);
 
 		assertFalse(txtColumn1.isEnabled());
 		assertFalse(txtColumn2.isEnabled());
 	}
 
 	/**
 	 * Test for <a href="http://bugs.eclipse.org/283694">Bug 283694</a>.
 	 */
 	public void testDeselectOnApplyWithOneItem() {
 		MasterDetailsRidget ridget = getRidget();
 		input = createInput(1);
 		bindToModel(true);
 
 		// select the the only row / item
 		MDBean item0 = input.get(0);
 		ridget.setSelection(item0);
 
 		assertEquals(item0, ridget.getSelection());
 
 		// invoke apply on the only row
 		ridget.handleApply();
 
 		// after apply the row should not be selected
 		assertEquals(null, ridget.getSelection());
 	}
 
 	public void testDirectWritingHidesApply() {
 		IMasterDetailsRidget ridget = getRidget();
 		MDWidget control = getWidget();
 
 		assertFalse(ridget.isDirectWriting());
 		assertTrue(control.getButtonApply().isVisible());
 
 		ridget.setDirectWriting(true);
 
 		assertTrue(ridget.isDirectWriting());
 		assertFalse(control.getButtonApply().isVisible());
 
 		ridget.setDirectWriting(false);
 
 		assertFalse(ridget.isDirectWriting());
 		assertTrue(control.getButtonApply().isVisible());
 	}
 
 	public void testDirectWritingUpdatesTableWithoutApply() {
 		IMasterDetailsRidget ridget = getRidget();
 		MDWidget widget = getWidget();
 		bindToModel(true);
 
 		ridget.setDirectWriting(true);
 		MDBean row0 = input.get(0);
 		ridget.setSelection(row0);
 
 		assertEquals("TestR0C1", row0.column1);
 		assertEquals("TestR0C2", row0.column2);
 		assertEquals("TestR0C1", widget.txtColumn1.getText());
 		assertEquals("TestR0C2", widget.txtColumn2.getText());
 
 		widget.txtColumn1.setFocus();
 		UITestHelper.sendString(widget.getDisplay(), "A\r");
 		widget.txtColumn2.setFocus();
 		UITestHelper.sendString(widget.getDisplay(), "B\r");
 
 		assertEquals("A", row0.column1);
 		assertEquals("B", row0.column2);
 	}
 
 	public void testDirectWritingAddsToTableWithoutApply() {
 		MasterDetailsRidget ridget = getRidget();
 		MDWidget widget = getWidget();
 		Table table = widget.getTable();
 		bindToModel(true);
 		ridget.setDirectWriting(true);
 
 		assertEquals(3, input.size());
 		assertEquals(3, table.getItemCount());
 
 		ridget.handleAdd();
 
 		assertEquals(4, input.size());
 		assertEquals(4, table.getItemCount());
 
 		MDBean row4 = (MDBean) ridget.getSelection();
 
 		assertEquals("", row4.column1);
 		assertEquals("", row4.column2);
 	}
 
 	public void testDirectWritingWithRequiresNoErrors() {
 		MasterDetailsRidget ridget = getRidget();
 		ridget.setApplyRequiresNoErrors(true);
 		ridget.setDirectWriting(true);
 		delegate.txtColumn1.addValidationRule(new MinLength(3), ValidationTime.ON_UI_CONTROL_EDIT);
 		bindToModel(true);
 
 		ridget.handleAdd();
 		MDBean bean = (MDBean) ridget.getSelection();
 
 		assertEquals("", bean.column1);
 		assertEquals("", bean.column2);
 
 		// should not update bean, col1 is not valid
 		delegate.txtColumn2.setText("beta");
 
 		assertEquals("", bean.column1);
 		assertEquals("", bean.column2);
 
 		delegate.txtColumn1.setText("alpha");
 
 		assertEquals("alpha", bean.column1);
 		assertEquals("beta", bean.column2);
 	}
 
 	public void testDirectWritingWithRequiresNoMandatories() {
 		MasterDetailsRidget ridget = getRidget();
 		ridget.setApplyRequiresNoMandatories(true);
 		ridget.setDirectWriting(true);
 		delegate.txtColumn1.setMandatory(true);
 		delegate.txtColumn2.setMandatory(true);
 		bindToModel(true);
 
 		ridget.handleAdd();
 		MDBean bean = (MDBean) ridget.getSelection();
 
 		assertEquals("", bean.column1);
 		assertEquals("", bean.column2);
 
 		delegate.txtColumn1.setText("alpha");
 
 		assertEquals("", bean.column1);
 		assertEquals("", bean.column2);
 
 		delegate.txtColumn2.setText("beta");
 
 		assertEquals("alpha", bean.column1);
 		assertEquals("beta", bean.column2);
 	}
 
 	public void testDirectWritingValidationCheck() {
 		MasterDetailsRidget ridget = getRidget();
 		ridget.setApplyRequiresNoMandatories(true);
 		ridget.setDirectWriting(true);
 		bindToModel(true);
 		delegate.validationResult = "error";
 
 		ridget.handleAdd();
 		MDBean bean = (MDBean) ridget.getSelection();
 
 		assertEquals("", bean.column1);
 		assertEquals("", bean.column2);
 
 		delegate.txtColumn1.setText("alpha");
 
 		assertEquals("", bean.column1);
 		assertEquals("", bean.column2);
 
 		delegate.validationResult = null;
 		delegate.txtColumn2.setText("beta");
 
 		assertEquals("alpha", bean.column1);
 		assertEquals("beta", bean.column2);
 	}
 
 	/**
 	 * As per Bug 293642
 	 */
 	public void testOneColumnFullWidget() {
 		MDWidget widget = getWidget();
 		Table table = widget.getTable();
 
 		assertEquals(0, table.getColumnCount());
 
 		WritableList list = new WritableList(input, MDBean.class);
 		String[] colProps = { "column1" };
 		String[] colHeaders = { "The Header" };
 		getRidget().bindToModel(list, MDBean.class, colProps, colHeaders);
 
 		assertEquals(1, table.getColumnCount());
 		assertEquals("The Header", table.getColumn(0).getText());
 		assertEquals(table.getClientArea().width, table.getColumn(0).getWidth());
 	}
 
 	/**
 	 * As per Bug 295305
 	 */
 	public void testAutoCreateTableColumns() {
 		IMasterDetailsRidget ridget = getRidget();
 		Table table = getWidget().getTable();
 
 		assertEquals(0, table.getColumnCount());
 
 		WritableList list = new WritableList(input, MDBean.class);
 		String[] columnProperties3 = { "column1", "column2", "column1" };
 		ridget.bindToModel(list, MDBean.class, columnProperties3, null);
 
 		assertEquals(3, table.getColumnCount());
 		assertTrue(table.getParent().getLayout() instanceof TableColumnLayout);
 
 		String[] columnProperties1 = { "column2" };
 		ridget.bindToModel(list, MDBean.class, columnProperties1, null);
 
 		assertEquals(1, table.getColumnCount());
 		assertTrue(table.getParent().getLayout() instanceof TableColumnLayout);
 	}
 
 	public void testDelegateItemCreated() {
 		MasterDetailsRidget ridget = getRidget();
 		bindToModel(true);
 
 		assertEquals(0, delegate.createCount);
 
 		ridget.handleAdd();
 
 		assertEquals(1, delegate.createCount);
 		MDBean lastItem = (MDBean) delegate.lastItem;
 		assertEquals(delegate.getWorkingCopy().column1, lastItem.column1);
 		assertEquals(delegate.getWorkingCopy().column2, lastItem.column2);
 	}
 
 	public void testDelegateItemRemoved() {
 		MasterDetailsRidget ridget = getRidget();
 		bindToModel(true);
 
 		assertEquals(0, delegate.removeCount);
 
 		Object first = input.get(0);
 		ridget.setSelection(first);
 		ridget.handleRemove();
 
 		assertEquals(1, delegate.removeCount);
 		assertEquals(first, delegate.lastItem);
 	}
 
 	public void testDelegateItemApplied() {
 		MasterDetailsRidget ridget = getRidget();
 		MDWidget widget = getWidget();
 		bindToModel(true);
 
 		assertEquals(0, delegate.applyCount);
 		assertEquals(0, delegate.prepareCount);
 		assertEquals(0, delegate.selectionCount);
 
 		MDBean first = input.get(0);
 		ridget.setSelection(first);
 
 		assertEquals(0, delegate.applyCount);
 		assertEquals(1, delegate.prepareCount);
 		assertEquals(1, delegate.selectionCount);
 
 		widget.txtColumn1.setFocus();
 		UITestHelper.sendString(widget.getDisplay(), "A\r");
 		ridget.handleApply();
 
 		assertEquals(1, delegate.applyCount);
 		assertEquals(1, delegate.prepareCount);
 		assertEquals(1, delegate.selectionCount);
 	}
 
 	public void testDelegateItemSelected() {
 		bindToModel(true);
 
 		assertEquals(0, delegate.prepareCount);
 		assertEquals(0, delegate.selectionCount);
 		assertNull(delegate.lastItem);
 
 		Object first = input.get(0);
 		getRidget().setSelection(first);
 
 		assertEquals(1, delegate.prepareCount);
 		assertEquals(1, delegate.selectionCount);
 		assertEquals(first, delegate.lastItem);
 	}
 
 	public void testReselectingSelectedRowIsIgnored() {
 		bindToModel(true);
 		MDWidget widget = getWidget();
 		Object first = input.get(0);
 
 		assertEquals(0, delegate.prepareCount);
 		assertEquals(0, delegate.selectionCount);
 		assertNull(delegate.lastItem);
 
 		Table table = widget.getTable();
 		table.setSelection(0);
 		Event event1 = createSelectionEvent(widget, first);
 		table.notifyListeners(SWT.Selection, event1);
 		Event event2 = createSelectionEvent(widget, first);
 		table.notifyListeners(SWT.Selection, event2);
 
 		assertEquals(1, delegate.prepareCount);
 		assertEquals(1, delegate.selectionCount);
 		assertEquals(first, delegate.lastItem);
 	}
 
 	public void testSuggestNewEntry() {
 		bindToModel(true);
 		IMasterDetailsRidget ridget = getRidget();
 		MDWidget widget = getWidget();
 
 		assertEquals("", widget.txtColumn1.getText());
 		assertEquals("", widget.txtColumn2.getText());
 		assertEquals(false, widget.getButtonApply().isEnabled());
 
 		MDBean newEntry = new MDBean("col1", "col2");
 		ridget.suggestNewEntry(newEntry);
 
 		assertEquals("col1", widget.txtColumn1.getText());
 		assertEquals("col2", widget.txtColumn2.getText());
 		assertTrue(widget.getButtonApply().isEnabled());
 		assertFalse(input.contains(newEntry));
 	}
 
 	public void testUpdateApplyButton() {
 		bindToModel(true);
 		IMasterDetailsRidget ridget = getRidget();
 		MDWidget widget = getWidget();
 
 		MDBean first = input.get(0);
 		ridget.setSelection(first);
 
 		assertFalse(widget.getButtonApply().isEnabled());
 
 		first.column1 = "col1";
 		ridget.updateApplyButton();
 
 		assertTrue(widget.getButtonApply().isEnabled());
 	}
 
 	/**
 	 * Tests the method {@code updateEnabled()} of the class
 	 * {@link AbstractMasterDetailsRidget}.
 	 */
 	public void testUpdateEnabled() {
 		bindToModel(true);
 
 		MasterDetailsRidget ridget = getRidget();
 		MDWidget widget = getWidget();
 
 		MDBean first = input.get(0);
 		ridget.setSelection(first);
 		assertTrue(widget.getButtonNew().isEnabled());
 		assertTrue(widget.getButtonRemove().isEnabled());
 		assertFalse(widget.getButtonApply().isEnabled());
 		assertNotNull(ridget.getSelection());
 		assertTrue(delegate.txtColumn1.isEnabled());
 		assertTrue(delegate.txtColumn2.isEnabled());
 
 		ridget.setEnabled(false);
 		assertFalse(widget.getButtonNew().isEnabled());
 		assertFalse(widget.getButtonRemove().isEnabled());
 		assertFalse(widget.getButtonApply().isEnabled());
 		assertNull(ridget.getSelection());
 		assertFalse(delegate.txtColumn1.isEnabled());
 		assertFalse(delegate.txtColumn2.isEnabled());
 
 		ridget.setEnabled(true);
 		assertTrue(widget.getButtonNew().isEnabled());
 		assertFalse(widget.getButtonRemove().isEnabled());
 		assertFalse(widget.getButtonApply().isEnabled());
 		assertNull(ridget.getSelection());
 		assertFalse(delegate.txtColumn1.isEnabled());
 		assertFalse(delegate.txtColumn2.isEnabled());
 
 	}
 
 	public void testApplyRequiresNoErrors() {
 		bindToModel(true);
 
 		IMasterDetailsRidget ridget = getRidget();
 		MDBean first = input.get(0);
 		ridget.setSelection(first);
 		Control applyButton = getWidget().getButtonApply();
 		ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		txtColumn1.addValidationRule(new MinLength(5), ValidationTime.ON_UPDATE_TO_MODEL);
 		txtColumn1.setText("abc");
 
 		assertTrue(txtColumn1.isErrorMarked());
 		assertFalse(ridget.isApplyRequiresNoErrors());
 		assertTrue(applyButton.isEnabled());
 
 		ridget.setApplyRequiresNoErrors(true);
 
 		assertTrue(txtColumn1.isErrorMarked());
 		assertTrue(ridget.isApplyRequiresNoErrors());
 		assertFalse(applyButton.isEnabled());
 
 		txtColumn1.setText("abcdef");
 
 		assertFalse(txtColumn1.isErrorMarked());
 		assertTrue(ridget.isApplyRequiresNoErrors());
 		assertTrue(applyButton.isEnabled());
 
 		txtColumn1.setText("abc");
 		ridget.setApplyRequiresNoErrors(false);
 
 		assertTrue(txtColumn1.isErrorMarked());
 		assertFalse(ridget.isApplyRequiresNoErrors());
 		assertTrue(applyButton.isEnabled());
 	}
 
 	public void testApplyRequiresNoMandatories() {
 		bindToModel(true);
 
 		IMasterDetailsRidget ridget = getRidget();
 		MDBean first = input.get(0);
 		ridget.setSelection(first);
 		Control applyButton = getWidget().getButtonApply();
 		ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		MandatoryMarker marker = new MandatoryMarker();
 		txtColumn1.addMarker(marker);
 		txtColumn1.setText("");
 
 		assertTrue(txtColumn1.isMandatory());
 		assertFalse(marker.isDisabled());
 		assertFalse(ridget.isApplyRequiresNoMandatories());
 		assertTrue(applyButton.isEnabled());
 
 		ridget.setApplyRequiresNoMandatories(true);
 
 		assertTrue(txtColumn1.isMandatory());
 		assertFalse(marker.isDisabled());
 		assertTrue(ridget.isApplyRequiresNoMandatories());
 		assertFalse(applyButton.isEnabled());
 
 		txtColumn1.setText("abc");
 
 		assertTrue(txtColumn1.isMandatory());
 		assertTrue(marker.isDisabled());
 		assertTrue(ridget.isApplyRequiresNoMandatories());
 		assertTrue(applyButton.isEnabled());
 
 		txtColumn1.setText("");
 		ridget.setApplyRequiresNoMandatories(false);
 
 		assertTrue(txtColumn1.isMandatory());
 		assertFalse(marker.isDisabled());
 		assertFalse(ridget.isApplyRequiresNoMandatories());
 		assertTrue(applyButton.isEnabled());
 	}
 
 	public void testApplyTriggersNew() {
 		IMasterDetailsRidget ridget = getRidget();
 
 		assertFalse(ridget.isApplyTriggersNew());
 
 		ridget.setApplyTriggersNew(true);
 
 		assertTrue(ridget.isApplyTriggersNew());
 
 		ridget.setApplyTriggersNew(false);
 
 		assertFalse(ridget.isApplyTriggersNew());
	}
 
	public void testApplyTriggersNewWhenNewButtonDoesNotExist() {
		boolean isTesting = RienaStatus.isTest();
		// disable the ridget "auto-creation" for this test
		System.setProperty(RienaStatus.RIENA_TEST_SYSTEM_PROPERTY, "false");
		try {
			IMasterDetailsRidget ridget = (IMasterDetailsRidget) createRidget();
			ridget.setUIControl(new MDWidget(getShell(), SWT.NONE) {
				@Override
				public Button createButtonNew(Composite parent) {
					return null;
				}
			});
			ridget.setApplyTriggersNew(true);

			assertFalse(ridget.isApplyTriggersNew());
		} finally {
			System.setProperty(RienaStatus.RIENA_TEST_SYSTEM_PROPERTY, String.valueOf(isTesting));
		}
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
 		assertEquals(items, table.getItemCount());
 	}
 
 	private void assertPropertyChangeEvent(int count, Object oldValue, Object newValue,
 			FTPropertyChangeListener listener) {
 		assertEquals(count, listener.count);
 		assertEquals("selection", listener.event.getPropertyName());
 		assertEquals(oldValue, listener.event.getOldValue());
 		assertEquals(newValue, listener.event.getNewValue());
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
 
 	private Event createSelectionEvent(MDWidget widget, Object first) {
 		Event result = new Event();
 		result.type = SWT.Selection;
 		// hack; we only care about w.getData() so we use this here instead of the TableItem
 		widget.setData(first);
 		result.item = widget;
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
 	private static class MDWidget extends MasterDetailsComposite {
 
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
 
 		@Override
 		public boolean confirmDiscardChanges() {
 			return true; // always accept, don't want a modal dialog in the test
 		}
 	}
 
 	/**
 	 * Implements a delegate with two text ridgets. This class is a companion
 	 * class to {@link MDBean} and {@link MDWidget}.
 	 */
 	private static final class MDDelegate extends AbstractMasterDetailsDelegate {
 
 		private final MDBean workingCopy = createWorkingCopy();
 		private boolean isTxtColumn1IsEnabled = true;
 
 		private int createCount;
 		private int removeCount;
 		private int applyCount;
 		private int prepareCount;
 		private int selectionCount;
 		private Object lastItem;
 		private ITextRidget txtColumn1;
 		private ITextRidget txtColumn2;
 		private String validationResult;
 
 		public void configureRidgets(IRidgetContainer container) {
 			checkContainer(container);
 
 			txtColumn1 = (ITextRidget) container.getRidget("txtColumn1");
 			txtColumn1.bindToModel(workingCopy, MDBean.PROPERTY_COLUMN_1);
 			txtColumn1.updateFromModel();
 
 			txtColumn2 = (ITextRidget) container.getRidget("txtColumn2");
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
 
 		@Override
 		public String isValid(IRidgetContainer container) {
 			checkContainer(container);
 			return validationResult;
 		}
 
 		@Override
 		public void updateDetails(IRidgetContainer container) {
 			checkContainer(container);
 
 			ITextRidget txtCol1 = (ITextRidget) container.getRidget("txtColumn1");
 			txtCol1.setEnabled(isTxtColumn1IsEnabled);
 			for (IRidget ridget : container.getRidgets()) {
 				ridget.updateFromModel();
 			}
 		}
 
 		@Override
 		public void itemApplied(Object changedItem) {
 			applyCount++;
 			lastItem = changedItem;
 		}
 
 		@Override
 		public void itemCreated(Object newItem) {
 			createCount++;
 			lastItem = newItem;
 		}
 
 		@Override
 		public void itemRemoved(Object oldItem) {
 			removeCount++;
 			lastItem = oldItem;
 		}
 
 		@Override
 		public void prepareItemSelected(Object newSelection) {
 			prepareCount++;
 			lastItem = newSelection;
 		}
 
 		@Override
 		public void itemSelected(Object newSelection) {
 			selectionCount++;
 			lastItem = newSelection;
 		}
 
 		private void checkContainer(IRidgetContainer container) {
 			assertNotNull(container.getRidget("txtColumn1"));
 			assertNotNull(container.getRidget("txtColumn2"));
 			assertEquals(2, container.getRidgets().size());
 		}
 	}
 
 	/**
 	 * PropertyChangeListener stub used for testing.
 	 */
 	private static final class FTPropertyChangeListener implements PropertyChangeListener {
 
 		private int count;
 		private PropertyChangeEvent event;
 
 		public void propertyChange(PropertyChangeEvent event) {
 			count++;
 			this.event = event;
 		}
 	}
 
 }
