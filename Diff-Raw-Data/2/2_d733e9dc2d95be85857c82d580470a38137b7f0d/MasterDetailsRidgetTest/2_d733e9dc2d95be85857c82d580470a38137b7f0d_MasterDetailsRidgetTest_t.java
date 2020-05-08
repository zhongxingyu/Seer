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
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Widget;
 
 import org.eclipse.riena.beans.common.AbstractBean;
 import org.eclipse.riena.core.RienaStatus;
 import org.eclipse.riena.core.marker.IMarkable;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.ui.swt.test.UITestHelper;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.core.marker.ValidationTime;
 import org.eclipse.riena.ui.ridgets.AbstractMasterDetailsDelegate;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.ICompositeRidget;
 import org.eclipse.riena.ui.ridgets.IMasterDetailsActionRidgetFacade;
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
 
 	private static final IBindingManager BINDING_MAN = new DefaultBindingManager(
 			SWTBindingPropertyLocator.getInstance(), SwtControlRidgetMapper.getInstance());
 	private final String[] columnProperties = { MDBean.PROPERTY_COLUMN_1, MDBean.PROPERTY_COLUMN_2 };
 	private final String[] columnHeaders = { "TestColumn1Header", "TestColumn2Header" };
 
 	private List<MDBean> input;
 	private MDDelegate delegate;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		input = createInput(3);
 		final IMasterDetailsRidget ridget = getRidget();
 		final List<Object> uiControls = getWidget().getUIControls();
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
 	protected Widget createWidget(final Composite parent) {
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
 		final SwtControlRidgetMapper mapper = SwtControlRidgetMapper.getInstance();
 		assertSame(MasterDetailsRidget.class, mapper.getRidgetClass(getWidget()));
 	}
 
 	public void testBindToModel() {
 		final IMasterDetailsRidget ridget = getRidget();
 		final MasterDetailsComposite composite = getWidget();
 		final Table table = composite.getTable();
 
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
 		final IMasterDetailsRidget ridget = getRidget();
 		final Table table = getWidget().getTable();
 		final MasterDetailsComposite mdComposite2 = (MasterDetailsComposite) createWidget(getShell());
 		final Table table2 = mdComposite2.getTable();
 
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
 		} catch (final RuntimeException rex) {
			ok("does not allow Table-Widget");
 		}
 	}
 
 	public void testAddBean() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		final ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		final ITextRidget txtColumn2 = ridget.getRidget(ITextRidget.class, "txtColumn2");
 
 		ridget.setApplyTriggersNew(false);
 		bindToModel(true);
 		final int oldSize = input.size();
 
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
 
 		final MDBean newEntry = input.get(oldSize);
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
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		final ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		final ITextRidget txtColumn2 = ridget.getRidget(ITextRidget.class, "txtColumn2");
 
 		ridget.setApplyTriggersNew(true);
 		bindToModel(true);
 		final int oldSize = input.size();
 
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
 
 		final MDBean newEntry = input.get(oldSize);
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
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		final Table table = widget.getTable();
 		final ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		final ITextRidget txtColumn2 = ridget.getRidget(ITextRidget.class, "txtColumn2");
 
 		bindToModel(true);
 
 		assertEquals(3, input.size());
 		assertContent(table, 3);
 
 		final MDBean toDelete = input.get(2);
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
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		final Table table = widget.getTable();
 
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
 		final IMasterDetailsRidget ridget = getRidget();
 		bindToModel(true);
 
 		assertEquals(null, ridget.getSelection());
 
 		ridget.setSelection(input.get(0));
 
 		assertEquals(input.get(0), ridget.getSelection());
 
 		ridget.setSelection(null);
 
 		assertEquals(null, ridget.getSelection());
 	}
 
 	public void testSetSelectionUpdatesUI() {
 		final IMasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 
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
 		final IMasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 
 		input = createInput(42);
 		bindToModel(true);
 
 		assertEquals(0, widget.getTable().getTopIndex());
 
 		ridget.setSelection(input.get(30));
 
 		// topIndex > 0 means we scrolled to reveal row 30
 		assertTrue(widget.getTable().getTopIndex() > 0);
 	}
 
 	public void testUpdateFromModelPreservesSelection() {
 		final IMasterDetailsRidget ridget = getRidget();
 		bindToModel(true);
 		final MDBean item2 = input.get(2);
 
 		ridget.setSelection(item2);
 
 		assertSame(item2, ridget.getSelection());
 
 		input.remove(input.get(1));
 
 		assertSame(item2, ridget.getSelection());
 
 		ridget.updateFromModel();
 
 		assertSame(item2, ridget.getSelection());
 	}
 
 	public void testUpdateFromModelRemovesSelection() {
 		final IMasterDetailsRidget ridget = getRidget();
 		bindToModel(true);
 		final MDBean item2 = input.get(2);
 
 		ridget.setSelection(item2);
 
 		assertSame(item2, ridget.getSelection());
 
 		input.remove(input.get(2));
 
 		assertSame(item2, ridget.getSelection());
 
 		ridget.updateFromModel();
 
 		assertNull(ridget.getSelection());
 	}
 
 	public void testUpdateSelectionFromRidgetOnRebind() {
 		final IMasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 
 		unbindUIControl(ridget, widget);
 		bindToModel(true);
 		ridget.setSelection(input.get(0));
 
 		assertEquals(0, widget.getTable().getSelectionCount());
 		assertEquals("", widget.txtColumn1.getText());
 		assertEquals("", widget.txtColumn2.getText());
 
 		final MDWidget widget2 = (MDWidget) createWidget(getShell());
 		bindUIControl(ridget, widget2);
 
 		assertEquals(1, widget2.getTable().getSelectionCount());
 		assertEquals("TestR0C1", widget2.txtColumn1.getText());
 		assertEquals("TestR0C2", widget2.txtColumn2.getText());
 	}
 
 	public void testSetSelectionFiresEvents() {
 		final IMasterDetailsRidget ridget = getRidget();
 		final MDBean item0 = input.get(0);
 		final FTPropertyChangeListener listener = new FTPropertyChangeListener();
 
 		bindToModel(true);
 		ridget.addPropertyChangeListener(ISelectableRidget.PROPERTY_SELECTION, listener);
 
 		ridget.setSelection(item0);
 		java.util.List<?> oldSelection = Collections.EMPTY_LIST;
 		java.util.List<?> newSelection = Arrays.asList(new Object[] { item0 });
 		assertPropertyChangeEvent(1, oldSelection, newSelection, listener);
 
 		ridget.setSelection(item0);
 		assertEquals(1, listener.count);
 
 		final MDBean item1 = input.get(1);
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
 		final IMasterDetailsRidget ridget = getRidget();
 		bindToModel(true);
 		final ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		final ITextRidget txtColumn2 = ridget.getRidget(ITextRidget.class, "txtColumn2");
 
 		final MDBean item0 = input.get(0);
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
 		final MasterDetailsRidget ridget = getRidget();
 		input = createInput(1);
 		bindToModel(true);
 
 		// select the the only row / item
 		final MDBean item0 = input.get(0);
 		ridget.setSelection(item0);
 
 		assertEquals(item0, ridget.getSelection());
 
 		// invoke apply on the only row
 		ridget.handleApply();
 
 		// after apply the row should not be selected
 		assertEquals(null, ridget.getSelection());
 	}
 
 	public void testDirectWritingHidesApply() {
 		final IMasterDetailsRidget ridget = getRidget();
 		final MDWidget control = getWidget();
 
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
 		final IMasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		bindToModel(true);
 
 		ridget.setDirectWriting(true);
 		final MDBean row0 = input.get(0);
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
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		final Table table = widget.getTable();
 		bindToModel(true);
 		ridget.setDirectWriting(true);
 
 		assertEquals(3, input.size());
 		assertEquals(3, table.getItemCount());
 
 		ridget.handleAdd();
 
 		assertEquals(4, input.size());
 		assertEquals(4, table.getItemCount());
 
 		final MDBean row4 = (MDBean) ridget.getSelection();
 
 		assertEquals("", row4.column1);
 		assertEquals("", row4.column2);
 	}
 
 	public void testDirectWritingWithRequiresNoErrors() {
 		final MasterDetailsRidget ridget = getRidget();
 		ridget.setApplyRequiresNoErrors(true);
 		ridget.setDirectWriting(true);
 		delegate.txtColumn1.addValidationRule(new MinLength(3), ValidationTime.ON_UI_CONTROL_EDIT);
 		bindToModel(true);
 
 		ridget.handleAdd();
 		final MDBean bean = (MDBean) ridget.getSelection();
 
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
 		final MasterDetailsRidget ridget = getRidget();
 		ridget.setApplyRequiresNoMandatories(true);
 		ridget.setDirectWriting(true);
 		delegate.txtColumn1.setMandatory(true);
 		delegate.txtColumn2.setMandatory(true);
 		bindToModel(true);
 
 		ridget.handleAdd();
 		final MDBean bean = (MDBean) ridget.getSelection();
 
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
 		final MasterDetailsRidget ridget = getRidget();
 		ridget.setApplyRequiresNoMandatories(true);
 		ridget.setDirectWriting(true);
 		bindToModel(true);
 		delegate.validationResult = "error";
 
 		ridget.handleAdd();
 		final MDBean bean = (MDBean) ridget.getSelection();
 
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
 		final MDWidget widget = getWidget();
 		final Table table = widget.getTable();
 
 		assertEquals(0, table.getColumnCount());
 
 		final WritableList list = new WritableList(input, MDBean.class);
 		final String[] colProps = { "column1" };
 		final String[] colHeaders = { "The Header" };
 		getRidget().bindToModel(list, MDBean.class, colProps, colHeaders);
 
 		assertEquals(1, table.getColumnCount());
 		assertEquals("The Header", table.getColumn(0).getText());
 		assertEquals(table.getClientArea().width, table.getColumn(0).getWidth());
 	}
 
 	/**
 	 * As per Bug 295305
 	 */
 	public void testAutoCreateTableColumns() {
 		final IMasterDetailsRidget ridget = getRidget();
 		final Table table = getWidget().getTable();
 
 		assertEquals(0, table.getColumnCount());
 
 		final WritableList list = new WritableList(input, MDBean.class);
 		final String[] columnProperties3 = { "column1", "column2", "column1" };
 		ridget.bindToModel(list, MDBean.class, columnProperties3, null);
 
 		assertEquals(3, table.getColumnCount());
 		assertTrue(table.getParent().getLayout() instanceof TableColumnLayout);
 
 		final String[] columnProperties1 = { "column2" };
 		ridget.bindToModel(list, MDBean.class, columnProperties1, null);
 
 		assertEquals(1, table.getColumnCount());
 		assertTrue(table.getParent().getLayout() instanceof TableColumnLayout);
 	}
 
 	public void testDelegateItemCreated() {
 		final MasterDetailsRidget ridget = getRidget();
 		bindToModel(true);
 
 		assertEquals(0, delegate.createCount);
 
 		ridget.handleAdd();
 
 		assertEquals(1, delegate.createCount);
 		final MDBean lastItem = (MDBean) delegate.lastItem;
 		assertEquals(delegate.getWorkingCopy().column1, lastItem.column1);
 		assertEquals(delegate.getWorkingCopy().column2, lastItem.column2);
 	}
 
 	public void testDelegateItemRemoved() {
 		final MasterDetailsRidget ridget = getRidget();
 		bindToModel(true);
 
 		assertEquals(0, delegate.removeCount);
 
 		final Object first = input.get(0);
 		ridget.setSelection(first);
 		ridget.handleRemove();
 
 		assertEquals(1, delegate.removeCount);
 		assertEquals(first, delegate.lastItem);
 	}
 
 	public void testDelegateItemApplied() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		bindToModel(true);
 
 		assertFalse(ridget.isDirectWriting());
 
 		assertEquals(0, delegate.prepareAppliedCount);
 		assertEquals(0, delegate.applyCount);
 		assertEquals(0, delegate.prepareSelectionCount);
 		assertEquals(0, delegate.selectionCount);
 
 		final MDBean first = input.get(0);
 		ridget.setSelection(first);
 
 		assertEquals(0, delegate.prepareAppliedCount);
 		assertEquals(0, delegate.applyCount);
 		assertEquals(1, delegate.prepareSelectionCount);
 		assertEquals(1, delegate.selectionCount);
 
 		widget.txtColumn1.setFocus();
 		UITestHelper.sendString(widget.getDisplay(), "A\r");
 		ridget.handleApply();
 
 		assertEquals(1, delegate.prepareAppliedCount);
 		assertEquals(1, delegate.applyCount);
 		assertEquals(1, delegate.prepareSelectionCount);
 		assertEquals(1, delegate.selectionCount);
 	}
 
 	public void testDelegateItemAppliedWithDirectWriting() {
 		final MasterDetailsRidget ridget = getRidget();
 		ridget.setDirectWriting(true);
 		final MDWidget widget = getWidget();
 
 		assertTrue(ridget.isDirectWriting());
 
 		bindToModel(true);
 
 		assertEquals(0, delegate.prepareAppliedCount);
 		assertEquals(0, delegate.applyCount);
 		assertEquals(0, delegate.prepareSelectionCount);
 		assertEquals(0, delegate.selectionCount);
 
 		final MDBean first = input.get(0);
 		ridget.setSelection(first);
 
 		assertEquals(0, delegate.prepareAppliedCount);
 		assertEquals(0, delegate.applyCount);
 		assertEquals(1, delegate.prepareSelectionCount);
 		assertEquals(1, delegate.selectionCount);
 
 		widget.txtColumn1.setFocus();
 		UITestHelper.sendString(widget.getDisplay(), "abc\t");
 
 		assertEquals(1, delegate.prepareAppliedCount);
 		assertEquals(1, delegate.applyCount);
 		assertEquals(1, delegate.prepareSelectionCount);
 		assertEquals(1, delegate.selectionCount);
 	}
 
 	public void testDelegateItemSelected() {
 		bindToModel(true);
 
 		assertEquals(0, delegate.prepareSelectionCount);
 		assertEquals(0, delegate.selectionCount);
 		assertNull(delegate.lastItem);
 
 		final Object first = input.get(0);
 		getRidget().setSelection(first);
 
 		assertEquals(1, delegate.prepareSelectionCount);
 		assertEquals(1, delegate.selectionCount);
 		assertEquals(first, delegate.lastItem);
 	}
 
 	public void testReselectingSelectedRowIsIgnored() {
 		bindToModel(true);
 		final MDWidget widget = getWidget();
 		final Object first = input.get(0);
 
 		assertEquals(0, delegate.prepareSelectionCount);
 		assertEquals(0, delegate.selectionCount);
 		assertNull(delegate.lastItem);
 
 		final Table table = widget.getTable();
 		final SelectionAdapter dirtyDetailsChecker = ReflectionUtils.getHidden(getRidget(), "dirtyDetailsChecker");
 		table.addSelectionListener(dirtyDetailsChecker);
 		table.setSelection(0);
 		final Event event1 = createSelectionEvent(widget, first);
 		table.notifyListeners(SWT.Selection, event1);
 		final Event event2 = createSelectionEvent(widget, first);
 		table.notifyListeners(SWT.Selection, event2);
 
 		assertEquals(1, delegate.prepareSelectionCount);
 		assertEquals(1, delegate.selectionCount);
 		assertEquals(first, delegate.lastItem);
 	}
 
 	public void testSuggestNewEntryWithParameter() {
 		bindToModel(true);
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 
 		assertEquals("", widget.txtColumn1.getText());
 		assertEquals("", widget.txtColumn2.getText());
 		assertFalse(widget.getButtonApply().isEnabled());
 		assertFalse(areDetailsChanged(ridget));
 
 		final MDBean newEntry = new MDBean("col1", "col2");
 		ridget.suggestNewEntry(newEntry);
 
 		assertEquals("col1", widget.txtColumn1.getText());
 		assertEquals("col2", widget.txtColumn2.getText());
 		assertTrue(widget.getButtonApply().isEnabled());
 		assertFalse(input.contains(newEntry));
 		assertTrue(areDetailsChanged(ridget));
 	}
 
 	/**
 	 * As per Bug 326020
 	 */
 	public void testSuggestNewEntryAddsSameInstance() {
 		bindToModel(true);
 		final MasterDetailsRidget ridget = getRidget();
 		final int size = input.size();
 
 		assertFalse(areDetailsChanged(ridget));
 
 		final MDBean newEntry = new MDBean("col1", "col2");
 		ridget.suggestNewEntry(newEntry);
 
 		assertTrue(areDetailsChanged(ridget));
 
 		ridget.handleApply();
 
 		assertFalse(areDetailsChanged(ridget));
 
 		assertEquals(size + 1, input.size());
 		final int last = input.size() - 1;
 		assertSame(newEntry, input.get(last));
 	}
 
 	public void testSuggestNewEntry() {
 		bindToModel(true);
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 
 		assertFalse(widget.txtColumn1.isEnabled());
 		assertFalse(widget.txtColumn2.isEnabled());
 		assertFalse(widget.getButtonApply().isEnabled());
 		assertFalse(areDetailsChanged(ridget));
 
 		ridget.suggestNewEntry();
 
 		assertEquals("", widget.txtColumn1.getText());
 		assertEquals("", widget.txtColumn2.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertTrue(widget.txtColumn2.isEnabled());
 		assertFalse(widget.getButtonApply().isEnabled());
 		assertFalse(areDetailsChanged(ridget));
 	}
 
 	public void testSuggestNewEntryObjectFalse() {
 		bindToModel(true);
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 
 		assertFalse(widget.txtColumn1.isEnabled());
 		assertFalse(widget.txtColumn2.isEnabled());
 		assertFalse(widget.getButtonApply().isEnabled());
 		assertFalse(areDetailsChanged(ridget));
 
 		final MDBean newEntry = new MDBean("col1", "col2");
 		ridget.suggestNewEntry(newEntry, false);
 
 		assertEquals("col1", widget.txtColumn1.getText());
 		assertEquals("col2", widget.txtColumn2.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertTrue(widget.txtColumn2.isEnabled());
 		assertFalse(widget.getButtonApply().isEnabled());
 		assertFalse(areDetailsChanged(ridget));
 	}
 
 	public void testSuggestNewEntryObjectTrue() {
 		bindToModel(true);
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 
 		assertFalse(widget.txtColumn1.isEnabled());
 		assertFalse(widget.txtColumn2.isEnabled());
 		assertFalse(widget.getButtonApply().isEnabled());
 		assertFalse(areDetailsChanged(ridget));
 
 		final MDBean newEntry = new MDBean("col1", "col2");
 		ridget.suggestNewEntry(newEntry, true);
 
 		assertEquals("col1", widget.txtColumn1.getText());
 		assertEquals("col2", widget.txtColumn2.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertTrue(widget.txtColumn2.isEnabled());
 		assertTrue(widget.getButtonApply().isEnabled());
 		assertTrue(areDetailsChanged(ridget));
 	}
 
 	public void testUpdateApplyButton() {
 		bindToModel(true);
 		final IMasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 
 		final MDBean first = input.get(0);
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
 
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 
 		final MDBean first = input.get(0);
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
 
 		final IMasterDetailsRidget ridget = getRidget();
 		final MDBean first = input.get(0);
 		ridget.setSelection(first);
 		final Control applyButton = getWidget().getButtonApply();
 		final ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		txtColumn1.addValidationRule(new MinLength(5), ValidationTime.ON_UPDATE_TO_MODEL);
 		txtColumn1.setText("abc");
 		final ITextRidget txtColumn2 = ridget.getRidget(ITextRidget.class, "txtColumn2");
 		txtColumn2.setText("efg");
 
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
 
 	/**
 	 * As per bug 320962.
 	 */
 	public void testApplyRequiresNoErrorsIgnoresDisabledRidget() {
 		bindToModel(true);
 
 		final IMasterDetailsRidget ridget = getRidget();
 		final MDBean first = input.get(0);
 		ridget.setSelection(first);
 		ridget.setApplyRequiresNoErrors(true);
 
 		final ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		txtColumn1.setText("abc");
 
 		final Control applyButton = getWidget().getButtonApply();
 
 		assertTrue(applyButton.isEnabled());
 
 		txtColumn1.setErrorMarked(true);
 
 		assertFalse(applyButton.isEnabled());
 
 		txtColumn1.setEnabled(false);
 
 		assertTrue(applyButton.isEnabled());
 	}
 
 	public void testApplyRequiresNoMandatories() {
 		bindToModel(true);
 
 		final IMasterDetailsRidget ridget = getRidget();
 		final MDBean first = input.get(0);
 		ridget.setSelection(first);
 		final Control applyButton = getWidget().getButtonApply();
 		final ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		final MandatoryMarker marker = new MandatoryMarker();
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
 
 	/**
 	 * As per bug 320962.
 	 */
 	public void testApplyRequiresNoMandatoriesIgnoresDisabledRidget() {
 		bindToModel(true);
 
 		final IMasterDetailsRidget ridget = getRidget();
 		final MDBean first = input.get(0);
 		ridget.setSelection(first);
 		ridget.setApplyRequiresNoMandatories(true);
 
 		final ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		txtColumn1.setText("");
 
 		final Control applyButton = getWidget().getButtonApply();
 
 		assertTrue(applyButton.isEnabled());
 
 		txtColumn1.setMandatory(true);
 
 		assertFalse(applyButton.isEnabled());
 
 		txtColumn1.setEnabled(false);
 
 		assertTrue(applyButton.isEnabled());
 	}
 
 	/**
 	 * As per Bug 323547
 	 */
 	public void testApplyRevealsSelection() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		final Table mdTable = widget.getTable();
 
 		input = createInput(42);
 		bindToModel(true);
 
 		ridget.setSelection(input.get(0));
 
 		assertEquals(0, mdTable.getTopIndex());
 
 		mdTable.setTopIndex(30);
 
 		assertTrue(mdTable.getTopIndex() > 0);
 
 		final ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		txtColumn1.setText("abc");
 		ridget.handleApply();
 
 		// apply should reveal edited editem
 		assertEquals(0, mdTable.getTopIndex());
 	}
 
 	public void testApplyTriggersNew() {
 		final IMasterDetailsRidget ridget = getRidget();
 
 		assertFalse(ridget.isApplyTriggersNew());
 
 		ridget.setApplyTriggersNew(true);
 
 		assertTrue(ridget.isApplyTriggersNew());
 
 		ridget.setApplyTriggersNew(false);
 
 		assertFalse(ridget.isApplyTriggersNew());
 	}
 
 	public void testApplyTriggersNewWhenNewButtonDoesNotExist() {
 		final boolean isTesting = RienaStatus.isTest();
 		// disable the ridget "auto-creation" for this test
 		System.setProperty(RienaStatus.RIENA_TEST_SYSTEM_PROPERTY, "false");
 		try {
 			final IMasterDetailsRidget ridget = (IMasterDetailsRidget) createRidget();
 			ridget.setUIControl(new MDWidget(getShell(), SWT.NONE) {
 				@Override
 				public Button createButtonNew(final Composite parent) {
 					return null;
 				}
 			});
 			ridget.setApplyTriggersNew(true);
 
 			assertFalse(ridget.isApplyTriggersNew());
 		} finally {
 			System.setProperty(RienaStatus.RIENA_TEST_SYSTEM_PROPERTY, String.valueOf(isTesting));
 		}
 	}
 
 	public void testHasErrors() {
 		final IMasterDetailsRidget ridget = getRidget();
 		final ICompositeRidget details = new CompositeRidget();
 		final ITextRidget textRidget = new TextRidget();
 		details.addRidget("textRidget", textRidget);
 
 		assertEquals(Boolean.FALSE, ReflectionUtils.invokeHidden(ridget, "hasErrors", details));
 
 		textRidget.setErrorMarked(true);
 
 		assertEquals(Boolean.TRUE, ReflectionUtils.invokeHidden(ridget, "hasErrors", details));
 
 		textRidget.setErrorMarked(false);
 
 		assertEquals(Boolean.FALSE, ReflectionUtils.invokeHidden(ridget, "hasErrors", details));
 	}
 
 	public void testHasErrosConsidersEnablement() {
 		final IMasterDetailsRidget ridget = getRidget();
 		final ICompositeRidget details = new CompositeRidget();
 		final ITextRidget textRidget = new TextRidget();
 		details.addRidget("textRidget", textRidget);
 
 		textRidget.setErrorMarked(true);
 		textRidget.setEnabled(false);
 
 		assertEquals(Boolean.FALSE, ReflectionUtils.invokeHidden(ridget, "hasErrors", details));
 
 		textRidget.setEnabled(true);
 
 		assertEquals(Boolean.TRUE, ReflectionUtils.invokeHidden(ridget, "hasErrors", details));
 	}
 
 	/**
 	 * As per Bug 323210
 	 */
 	public void testHasErrorsWithCompositeRidget() {
 		final IMasterDetailsRidget ridget = getRidget();
 		final ICompositeRidget details = new CompositeRidget();
 		final ICompositeRidget cRidget1 = new CompositeRidget();
 		final ICompositeRidget cRidget2 = new CompositeRidget();
 		final TextRidget textRidget = new TextRidget();
 		details.addRidget("cRidget1", cRidget1);
 		details.addRidget("cRidget2", cRidget2);
 		cRidget2.addRidget("textRidget", textRidget);
 
 		assertEquals(Boolean.FALSE, ReflectionUtils.invokeHidden(ridget, "hasErrors", details));
 		assertTrue(textRidget.isEnabled());
 
 		textRidget.setErrorMarked(true);
 
 		assertEquals(Boolean.TRUE, ReflectionUtils.invokeHidden(ridget, "hasErrors", details));
 
 		textRidget.setErrorMarked(false);
 
 		assertEquals(Boolean.FALSE, ReflectionUtils.invokeHidden(ridget, "hasErrors", details));
 
 		textRidget.setErrorMarked(true);
 		textRidget.setEnabled(false);
 
 		assertEquals(Boolean.FALSE, ReflectionUtils.invokeHidden(ridget, "hasErrors", details));
 	}
 
 	public void testHasMandatories() {
 		final IMasterDetailsRidget ridget = getRidget();
 		final ICompositeRidget details = new CompositeRidget();
 		final ITextRidget textRidget = new TextRidget();
 		details.addRidget("textRidget", textRidget);
 
 		assertEquals(Boolean.FALSE, ReflectionUtils.invokeHidden(ridget, "hasMandatories", details));
 
 		textRidget.setMandatory(true);
 
 		assertEquals(Boolean.TRUE, ReflectionUtils.invokeHidden(ridget, "hasMandatories", details));
 
 		textRidget.setText("abc");
 
 		assertEquals(Boolean.FALSE, ReflectionUtils.invokeHidden(ridget, "hasMandatories", details));
 
 		textRidget.setText("");
 
 		assertEquals(Boolean.TRUE, ReflectionUtils.invokeHidden(ridget, "hasMandatories", details));
 
 		textRidget.setMandatory(false);
 
 		assertEquals(Boolean.FALSE, ReflectionUtils.invokeHidden(ridget, "hasMandatories", details));
 	}
 
 	public void testHasMandatoriesConsidersEnablement() {
 		final IMasterDetailsRidget ridget = getRidget();
 		final ICompositeRidget details = new CompositeRidget();
 		final ITextRidget textRidget = new TextRidget();
 		details.addRidget("textRidget", textRidget);
 
 		textRidget.setMandatory(true);
 		textRidget.setEnabled(false);
 
 		assertEquals(Boolean.FALSE, ReflectionUtils.invokeHidden(ridget, "hasMandatories", details));
 
 		textRidget.setEnabled(true);
 
 		assertEquals(Boolean.TRUE, ReflectionUtils.invokeHidden(ridget, "hasMandatories", details));
 	}
 
 	/**
 	 * As per Bug 323210
 	 */
 	public void testHasMandatoriesWithCompositeRidget() {
 		final IMasterDetailsRidget ridget = getRidget();
 		final ICompositeRidget details = new CompositeRidget();
 		final ICompositeRidget cRidget1 = new CompositeRidget();
 		final ICompositeRidget cRidget2 = new CompositeRidget();
 		final TextRidget textRidget = new TextRidget();
 		details.addRidget("cRidget1", cRidget1);
 		details.addRidget("cRidget2", cRidget2);
 		cRidget2.addRidget("textRidget", textRidget);
 
 		assertEquals(Boolean.FALSE, ReflectionUtils.invokeHidden(ridget, "hasMandatories", details));
 		assertTrue(textRidget.isEnabled());
 
 		textRidget.setMandatory(true);
 
 		assertEquals(Boolean.TRUE, ReflectionUtils.invokeHidden(ridget, "hasMandatories", details));
 
 		textRidget.setMandatory(false);
 
 		assertEquals(Boolean.FALSE, ReflectionUtils.invokeHidden(ridget, "hasMandatories", details));
 
 		textRidget.setMandatory(true);
 		textRidget.setEnabled(false);
 
 		assertEquals(Boolean.FALSE, ReflectionUtils.invokeHidden(ridget, "hasMandatories", details));
 	}
 
 	/**
 	 * As per Bug 327286.
 	 */
 	public void testSetRemoveCancelsNew() {
 		final MasterDetailsRidget ridget = getRidget();
 
 		assertFalse(ridget.isRemoveCancelsNew());
 
 		ridget.setRemoveCancelsNew(true);
 
 		assertTrue(ridget.isRemoveCancelsNew());
 
 		ridget.setRemoveCancelsNew(false);
 
 		assertFalse(ridget.isRemoveCancelsNew());
 	}
 
 	/**
 	 * As per Bug 327286.
 	 */
 	public void testRemoveCancelsNewWithPreviousSelection() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		bindToModel(true);
 		ridget.setRemoveCancelsNew(true);
 
 		final MDBean first = input.get(0);
 		ridget.setSelection(first);
 
 		assertSame(first, ridget.getSelection());
 		assertEquals("TestR0C1", widget.txtColumn1.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertTrue(widget.getButtonRemove().isEnabled());
 
 		ridget.handleAdd();
 
 		assertNull(ridget.getSelection());
 		assertEquals("", widget.txtColumn1.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertTrue(widget.getButtonRemove().isEnabled());
 
 		clickRemove(ridget);
 
 		assertSame(first, ridget.getSelection());
 		assertEquals("TestR0C1", widget.txtColumn1.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertTrue(widget.getButtonRemove().isEnabled());
 
 		final int oldSize = input.size();
 		clickRemove(ridget);
 
 		assertEquals(oldSize, input.size() + 1);
 	}
 
 	/**
 	 * As per Bug 327286.
 	 */
 	public void testRemoveCancelsNewWithNoPreviousSelection() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		bindToModel(true);
 		ridget.setRemoveCancelsNew(true);
 
 		ridget.setSelection(null);
 
 		assertNull(ridget.getSelection());
 		assertEquals("", widget.txtColumn1.getText());
 		assertFalse(widget.txtColumn1.isEnabled());
 		assertFalse(widget.getButtonRemove().isEnabled());
 
 		ridget.handleAdd();
 
 		assertNull(ridget.getSelection());
 		assertEquals("", widget.txtColumn1.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertTrue(widget.getButtonRemove().isEnabled());
 
 		clickRemove(ridget);
 
 		assertNull(ridget.getSelection());
 		assertEquals("", widget.txtColumn1.getText());
 		assertFalse(widget.txtColumn1.isEnabled());
 		assertFalse(widget.getButtonRemove().isEnabled());
 	}
 
 	/**
 	 * As per Bug 327286.
 	 */
 	public void testRemoveCancelsSecondNew() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		bindToModel(true);
 		ridget.setRemoveCancelsNew(true);
 
 		final MDBean first = input.get(0);
 		ridget.setSelection(first);
 
 		assertSame(first, ridget.getSelection());
 		assertEquals("TestR0C1", widget.txtColumn1.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertTrue(widget.getButtonRemove().isEnabled());
 
 		ridget.handleAdd();
 		ridget.handleAdd();
 
 		assertNull(ridget.getSelection());
 		assertEquals("", widget.txtColumn1.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertTrue(widget.getButtonRemove().isEnabled());
 
 		clickRemove(ridget);
 
 		assertSame(first, ridget.getSelection());
 		assertEquals("TestR0C1", widget.txtColumn1.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertTrue(widget.getButtonRemove().isEnabled());
 	}
 
 	/**
 	 * As per Bug 327286.
 	 */
 	public void testRemoveCancelsNewIsOffByDefault() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		bindToModel(true);
 
 		ridget.handleAdd();
 
 		assertFalse(ridget.isRemoveCancelsNew());
 		assertFalse(widget.getButtonRemove().isEnabled());
 	}
 
 	/**
 	 * As per Bug 327286.
 	 */
 	public void testRemoveCancelsNewWhenRemoveButtonDoesNotExist() {
 		final boolean isTesting = RienaStatus.isTest();
 		// disable the ridget "auto-creation" for this test
 		System.setProperty(RienaStatus.RIENA_TEST_SYSTEM_PROPERTY, "false");
 		try {
 			final IMasterDetailsRidget ridget = (IMasterDetailsRidget) createRidget();
 			ridget.setUIControl(new MDWidget(getShell(), SWT.NONE) {
 				@Override
 				public Button createButtonRemove(final Composite parent) {
 					return null;
 				}
 			});
 			ridget.setRemoveCancelsNew(true);
 
 			assertFalse(ridget.isRemoveCancelsNew());
 		} finally {
 			System.setProperty(RienaStatus.RIENA_TEST_SYSTEM_PROPERTY, String.valueOf(isTesting));
 		}
 	}
 
 	public void testSetRemoveTriggersNew() {
 		final MasterDetailsRidget ridget = getRidget();
 
 		assertFalse(ridget.isRemoveTriggersNew());
 
 		ridget.setRemoveTriggersNew(true);
 
 		assertTrue(ridget.isRemoveTriggersNew());
 
 		ridget.setRemoveTriggersNew(false);
 
 		assertFalse(ridget.isRemoveTriggersNew());
 	}
 
 	public void testRemoveTriggersNew() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		bindToModel(true);
 		ridget.setRemoveTriggersNew(true);
 
 		final MDBean first = input.get(0);
 		ridget.setSelection(first);
 
 		assertSame(first, ridget.getSelection());
 		assertEquals("TestR0C1", widget.txtColumn1.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertTrue(widget.getButtonRemove().isEnabled());
 
 		final int oldSize = input.size();
 		clickRemove(ridget);
 
 		assertEquals(oldSize, input.size() + 1);
 		assertNull(ridget.getSelection());
 		assertEquals("", widget.txtColumn1.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertFalse(widget.getButtonRemove().isEnabled());
 	}
 
 	public void testRemoveCancelsNewAndThenTriggersNewWithPreviousSelection() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		bindToModel(true);
 		ridget.setRemoveCancelsNew(true);
 		ridget.setRemoveTriggersNew(true);
 
 		final MDBean first = input.get(0);
 		ridget.setSelection(first);
 
 		assertSame(first, ridget.getSelection());
 		assertEquals("TestR0C1", widget.txtColumn1.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertTrue(widget.getButtonRemove().isEnabled());
 
 		ridget.handleAdd();
 
 		assertNull(ridget.getSelection());
 		assertEquals("", widget.txtColumn1.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertTrue(widget.getButtonRemove().isEnabled());
 
 		clickRemove(ridget);
 
 		assertSame(first, ridget.getSelection());
 		assertEquals("TestR0C1", widget.txtColumn1.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertTrue(widget.getButtonRemove().isEnabled());
 
 		final int oldSize = input.size();
 		clickRemove(ridget);
 
 		assertEquals(oldSize, input.size() + 1);
 	}
 
 	public void testRemoveCancelsNewAndThenTriggersNewWithNoPreviousSelection() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		bindToModel(true);
 		ridget.setRemoveCancelsNew(true);
 		ridget.setRemoveTriggersNew(true);
 
 		ridget.setSelection(null);
 
 		assertNull(ridget.getSelection());
 		assertEquals("", widget.txtColumn1.getText());
 		assertFalse(widget.txtColumn1.isEnabled());
 		assertFalse(widget.getButtonRemove().isEnabled());
 
 		ridget.handleAdd();
 
 		assertNull(ridget.getSelection());
 		assertEquals("", widget.txtColumn1.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertTrue(widget.getButtonRemove().isEnabled());
 
 		clickRemove(ridget);
 
 		assertNull(ridget.getSelection());
 		assertEquals("", widget.txtColumn1.getText());
 		assertTrue(widget.txtColumn1.isEnabled());
 		assertTrue(widget.getButtonRemove().isEnabled());
 	}
 
 	public void testRemoveTriggersNewWhenRemoveButtonDoesNotExist() {
 		final boolean isTesting = RienaStatus.isTest();
 		// disable the ridget "auto-creation" for this test
 		System.setProperty(RienaStatus.RIENA_TEST_SYSTEM_PROPERTY, "false");
 		try {
 			final IMasterDetailsRidget ridget = (IMasterDetailsRidget) createRidget();
 			ridget.setUIControl(new MDWidget(getShell(), SWT.NONE) {
 				@Override
 				public Button createButtonRemove(final Composite parent) {
 					return null;
 				}
 			});
 			ridget.setRemoveTriggersNew(true);
 
 			assertFalse(ridget.isRemoveTriggersNew());
 		} finally {
 			System.setProperty(RienaStatus.RIENA_TEST_SYSTEM_PROPERTY, String.valueOf(isTesting));
 		}
 	}
 
 	/**
 	 * As per Bug 327287.
 	 */
 	public void testShowGlobalMandatoryMarkerOnNew() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		final ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		final ITextRidget txtColumn2 = ridget.getRidget(ITextRidget.class, "txtColumn2");
 		txtColumn2.setMandatory(true);
 		delegate.setValidMaster(false);
 		bindToModel(true);
 
 		ridget.handleAdd();
 
 		assertTrue(hasMandatory(txtColumn1));
 		assertTrue(hasMandatory(txtColumn2));
 
 		widget.txtColumn1.setFocus();
 		UITestHelper.sendString(widget.getDisplay(), "abc\t");
 
 		assertFalse(hasMandatory(txtColumn1));
 		assertTrue(hasMandatory(txtColumn2));
 	}
 
 	/**
 	 * As per Bug 327287.
 	 */
 	public void testShowGlobalMandatoryMarkerOnSuggestNewEntry() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		final ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		final ITextRidget txtColumn2 = ridget.getRidget(ITextRidget.class, "txtColumn2");
 		txtColumn2.setMandatory(true);
 		delegate.setValidMaster(false);
 		bindToModel(true);
 
 		ridget.setDirectWriting(false);
 		ridget.suggestNewEntry();
 
 		assertTrue(hasMandatory(txtColumn1));
 		assertTrue(hasMandatory(txtColumn2));
 
 		widget.txtColumn1.setFocus();
 		UITestHelper.sendString(widget.getDisplay(), "abc\t");
 
 		assertFalse(hasMandatory(txtColumn1));
 		assertTrue(hasMandatory(txtColumn2));
 	}
 
 	/**
 	 * As per Bug 327287.
 	 */
 	public void testShowGlobalMandatoryMarkerOnSuggestNewEntryWithDirectWriting() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		final ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		final ITextRidget txtColumn2 = ridget.getRidget(ITextRidget.class, "txtColumn2");
 		txtColumn2.setMandatory(true);
 		delegate.setValidMaster(false);
 		bindToModel(true);
 
 		ridget.setDirectWriting(true);
 		ridget.suggestNewEntry();
 
 		assertTrue(hasMandatory(txtColumn1));
 		assertTrue(hasMandatory(txtColumn2));
 
 		widget.txtColumn1.setFocus();
 		UITestHelper.sendString(widget.getDisplay(), "abc\t");
 
 		assertFalse(hasMandatory(txtColumn1));
 		assertTrue(hasMandatory(txtColumn2));
 	}
 
 	/**
 	 * As per Bug 327287.
 	 */
 	public void testHideGlobalMandatoryMarkerOnSelect() {
 		final MasterDetailsRidget ridget = getRidget();
 		final ITextRidget txtColumn1 = ridget.getRidget(ITextRidget.class, "txtColumn1");
 		final ITextRidget txtColumn2 = ridget.getRidget(ITextRidget.class, "txtColumn2");
 		delegate.setValidMaster(false);
 		bindToModel(true);
 
 		final MDBean first = input.get(0);
 		ridget.setSelection(first);
 
 		assertTrue(txtColumn1.isEnabled());
 		assertTrue(txtColumn2.isEnabled());
 		assertFalse(hasMandatory(txtColumn1));
 		assertFalse(hasMandatory(txtColumn2));
 	}
 
 	/**
 	 * As per Bug 327496.
 	 */
 	public void testSetHideMandatoryAndErrorMarkers() {
 		final MasterDetailsRidget ridget = getRidget();
 
 		assertFalse(ridget.isHideMandatoryAndErrorMarkersOnNewEntries());
 
 		ridget.setHideMandatoryAndErrorMarkersOnNewEntries(true);
 
 		assertTrue(ridget.isHideMandatoryAndErrorMarkersOnNewEntries());
 
 		ridget.setHideMandatoryAndErrorMarkersOnNewEntries(false);
 
 		assertFalse(ridget.isHideMandatoryAndErrorMarkersOnNewEntries());
 	}
 
 	/**
 	 * As per Bug 327496.
 	 */
 	public void testSetHideMandatoryAndErrorMarkersOnNew() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		bindToModel(true);
 
 		ridget.setHideMandatoryAndErrorMarkersOnNewEntries(true);
 		delegate.txtColumn1.setMandatory(true);
 		delegate.txtColumn2.setMandatory(true);
 		ridget.handleAdd();
 
 		final Color mandatoryMarkerBg = new Color(widget.getDisplay(), 255, 255, 175);
 		final Color whiteBg = widget.getDisplay().getSystemColor(SWT.COLOR_WHITE);
 
 		try {
 			assertEquals("", widget.txtColumn1.getText());
 			assertEquals(whiteBg, widget.txtColumn1.getBackground());
 			assertEquals("", widget.txtColumn2.getText());
 			assertEquals(whiteBg, widget.txtColumn2.getBackground());
 
 			widget.txtColumn1.setFocus();
 			UITestHelper.sendString(widget.getDisplay(), "a\t");
 
 			assertEquals(whiteBg, widget.txtColumn1.getBackground());
 			assertEquals(mandatoryMarkerBg, widget.txtColumn2.getBackground());
 		} finally {
 			mandatoryMarkerBg.dispose();
 		}
 	}
 
 	/**
 	 * As per Bug 327496.
 	 */
 	public void testSetHideMandatoryAndErrorMarkersOnSuggestNewEntry() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		bindToModel(true);
 
 		ridget.setHideMandatoryAndErrorMarkersOnNewEntries(true);
 		delegate.txtColumn1.setMandatory(true);
 		delegate.txtColumn2.setMandatory(true);
 		ridget.suggestNewEntry(new MDBean("", ""));
 
 		final Color mandatoryMarkerBg = new Color(widget.getDisplay(), 255, 255, 175);
 		final Color whiteBg = widget.getDisplay().getSystemColor(SWT.COLOR_WHITE);
 
 		try {
 			assertEquals("", widget.txtColumn1.getText());
 			assertEquals(whiteBg, widget.txtColumn1.getBackground());
 			assertEquals("", widget.txtColumn2.getText());
 			assertEquals(whiteBg, widget.txtColumn2.getBackground());
 
 			widget.txtColumn1.setFocus();
 			UITestHelper.sendString(widget.getDisplay(), "b\t");
 
 			assertEquals(whiteBg, widget.txtColumn1.getBackground());
 			assertEquals(mandatoryMarkerBg, widget.txtColumn2.getBackground());
 		} finally {
 			mandatoryMarkerBg.dispose();
 		}
 	}
 
 	/**
 	 * As per Bug 327496.
 	 */
 	public void testResetHiddenMarkersOnSelectionChange() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		bindToModel(true);
 
 		ridget.setHideMandatoryAndErrorMarkersOnNewEntries(true);
 		delegate.txtColumn1.setMandatory(true);
 		delegate.txtColumn2.setMandatory(true);
 		final MDBean first = new MDBean("first", "");
 		input.add(0, first);
 		ridget.updateFromModel();
 
 		final Color mandatoryMarkerBg = new Color(widget.getDisplay(), 255, 255, 175);
 		final Color whiteBg = widget.getDisplay().getSystemColor(SWT.COLOR_WHITE);
 
 		try {
 			ridget.suggestNewEntry(new MDBean("", ""));
 
 			assertEquals("", widget.txtColumn1.getText());
 			assertEquals(whiteBg, widget.txtColumn1.getBackground());
 			assertEquals("", widget.txtColumn2.getText());
 			assertEquals(whiteBg, widget.txtColumn2.getBackground());
 
 			ridget.setSelection(first);
 
 			assertEquals("first", widget.txtColumn1.getText());
 			assertEquals(whiteBg, widget.txtColumn1.getBackground());
 			assertEquals("", widget.txtColumn2.getText());
 			assertEquals(mandatoryMarkerBg, widget.txtColumn2.getBackground());
 		} finally {
 			mandatoryMarkerBg.dispose();
 		}
 	}
 
 	/**
 	 * As per Bug 327496.
 	 */
 	public void testResetHiddenMarkersOnApply() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		bindToModel(true);
 
 		ridget.setHideMandatoryAndErrorMarkersOnNewEntries(true);
 		delegate.txtColumn1.setMandatory(true);
 		delegate.txtColumn2.setMandatory(true);
 
 		final Color mandatoryMarkerBg = new Color(widget.getDisplay(), 255, 255, 175);
 		final Color whiteBg = widget.getDisplay().getSystemColor(SWT.COLOR_WHITE);
 
 		try {
 			final MDBean value = new MDBean("", "");
 			ridget.suggestNewEntry(value);
 
 			assertTrue(ridget.isHideMandatoryAndErrorMarkersOnNewEntries());
 			assertEquals("", widget.txtColumn1.getText());
 			assertEquals(whiteBg, widget.txtColumn1.getBackground());
 			assertEquals("", widget.txtColumn2.getText());
 			assertEquals(whiteBg, widget.txtColumn2.getBackground());
 
 			ridget.handleApply();
 			ridget.setSelection(value);
 
 			assertTrue(ridget.isHideMandatoryAndErrorMarkersOnNewEntries());
 			assertEquals("", widget.txtColumn1.getText());
 			assertEquals(mandatoryMarkerBg, widget.txtColumn1.getBackground());
 			assertEquals("", widget.txtColumn2.getText());
 			assertEquals(mandatoryMarkerBg, widget.txtColumn2.getBackground());
 
 			ridget.suggestNewEntry(value);
 		} finally {
 			mandatoryMarkerBg.dispose();
 		}
 	}
 
 	/**
 	 * As per Bug 327496.
 	 */
 	public void testResetHiddenMarkersOnRemoveCancelsNew() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		bindToModel(true);
 
 		ridget.setRemoveCancelsNew(true);
 		ridget.setHideMandatoryAndErrorMarkersOnNewEntries(true);
 		delegate.txtColumn1.setMandatory(true);
 		delegate.txtColumn2.setMandatory(true);
 		final MDBean first = new MDBean("first", "");
 		input.add(0, first);
 		ridget.updateFromModel();
 
 		final Color mandatoryMarkerBg = new Color(widget.getDisplay(), 255, 255, 175);
 		final Color whiteBg = widget.getDisplay().getSystemColor(SWT.COLOR_WHITE);
 
 		try {
 			ridget.setSelection(first);
 			ridget.handleAdd();
 
 			assertEquals("", widget.txtColumn1.getText());
 			assertEquals(whiteBg, widget.txtColumn1.getBackground());
 			assertEquals("", widget.txtColumn2.getText());
 			assertEquals(whiteBg, widget.txtColumn2.getBackground());
 
 			ridget.handleCancel();
 
 			assertEquals("first", widget.txtColumn1.getText());
 			assertEquals(whiteBg, widget.txtColumn1.getBackground());
 			assertEquals("", widget.txtColumn2.getText());
 			assertEquals(mandatoryMarkerBg, widget.txtColumn2.getBackground());
 		} finally {
 			mandatoryMarkerBg.dispose();
 		}
 	}
 
 	/**
 	 * As per Bug 327496.
 	 */
 	public void testResetHiddenMarkersOnDirectWritingEvent() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		bindToModel(true);
 
 		ridget.setDirectWriting(true);
 		delegate.txtColumn1.setMandatory(true);
 		delegate.txtColumn2.setMandatory(true);
 		ridget.setHideMandatoryAndErrorMarkersOnNewEntries(true);
 
 		final Color mandatoryMarkerBg = new Color(widget.getDisplay(), 255, 255, 175);
 		final Color whiteBg = widget.getDisplay().getSystemColor(SWT.COLOR_WHITE);
 
 		try {
 			ridget.handleAdd();
 
 			assertEquals("", widget.txtColumn1.getText());
 			assertEquals(whiteBg, widget.txtColumn1.getBackground());
 			assertEquals("", widget.txtColumn2.getText());
 			assertEquals(whiteBg, widget.txtColumn2.getBackground());
 
 			widget.txtColumn1.setFocus();
 			UITestHelper.sendString(widget.getDisplay(), "b\t");
 
 			assertEquals("b", widget.txtColumn1.getText());
 			assertEquals(whiteBg, widget.txtColumn1.getBackground());
 			assertEquals("", widget.txtColumn2.getText());
 			assertEquals(mandatoryMarkerBg, widget.txtColumn2.getBackground());
 		} finally {
 			mandatoryMarkerBg.dispose();
 		}
 	}
 
 	/**
 	 * As per Bug 327496.
 	 */
 	public void testShowGlobalMandatoryMarkerEvenWhenSetHideMandatoryAndErrorMarkersIsOn() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDWidget widget = getWidget();
 		bindToModel(true);
 
 		ridget.setHideMandatoryAndErrorMarkersOnNewEntries(true);
 		delegate.setValidMaster(false);
 		ridget.handleAdd();
 
 		final Color mandatoryMarkerBg = new Color(widget.getDisplay(), 255, 255, 175);
 		final Color whiteBg = widget.getDisplay().getSystemColor(SWT.COLOR_WHITE);
 
 		try {
 			assertEquals("", widget.txtColumn1.getText());
 			assertEquals(mandatoryMarkerBg, widget.txtColumn1.getBackground());
 			assertEquals("", widget.txtColumn2.getText());
 			assertEquals(mandatoryMarkerBg, widget.txtColumn2.getBackground());
 
 			widget.txtColumn1.setFocus();
 			UITestHelper.sendString(widget.getDisplay(), "a\t");
 
 			assertEquals(whiteBg, widget.txtColumn1.getBackground());
 			assertEquals(whiteBg, widget.txtColumn2.getBackground());
 		} finally {
 			mandatoryMarkerBg.dispose();
 		}
 	}
 
 	public void testUpdateMasterDetailsActionRidgets() {
 		final MasterDetailsRidget ridget = getRidget();
 		final MDBean first = new MDBean("first", "");
 		input.add(0, first);
 		bindToModel(true);
 		final MDBean second = new MDBean("second", "");
 		input.add(0, second);
 		bindToModel(true);
 		delegate.updateActionsCalled = false;
 		ridget.updateFromModel();
 		assertFalse(delegate.updateActionsCalled);
 		ridget.setSelection(first);
 		assertTrue(delegate.updateActionsCalled);
 		delegate.updateActionsCalled = false;
 		ridget.setSelection(second);
 		assertTrue(delegate.updateActionsCalled);
 		delegate.updateActionsCalled = false;
 		ridget.setSelection(null);
 
 		delegate.updateActionsCalled = false;
 		ridget.setSelection(second);
 		getWidget().txtColumn1.setFocus();
 		UITestHelper.sendString(getWidget().getDisplay(), "dirty\t");
 		assertTrue(delegate.updateActionsCalled);
 		delegate.updateActionsCalled = false;
 	}
 
 	// helping methods
 	//////////////////
 
 	private boolean areDetailsChanged(final MasterDetailsRidget ridget) {
 		final Boolean result = ReflectionUtils.invokeHidden(ridget, "areDetailsChanged", (Object[]) null);
 		return result.booleanValue();
 	}
 
 	private void assertContent(final Table table, final int items) {
 		for (int i = 0; i < items; i++) {
 			final String label0 = String.format("TestR%dC1", i);
 			final String label1 = String.format("TestR%dC2", i);
 			assertEquals(label0, table.getItem(i).getText(0));
 			assertEquals(label1, table.getItem(i).getText(1));
 		}
 		assertEquals(items, table.getItemCount());
 	}
 
 	private void assertPropertyChangeEvent(final int count, final Object oldValue, final Object newValue,
 			final FTPropertyChangeListener listener) {
 		assertEquals(count, listener.count);
 		assertEquals("selection", listener.event.getPropertyName());
 		assertEquals(oldValue, listener.event.getOldValue());
 		assertEquals(newValue, listener.event.getNewValue());
 	}
 
 	private void bindToModel(final boolean withUpdate) {
 		final WritableList list = new WritableList(input, MDBean.class);
 		getRidget().bindToModel(list, MDBean.class, columnProperties, columnHeaders);
 		if (withUpdate) {
 			getRidget().updateFromModel();
 		}
 	}
 
 	private void bindUIControl(final IMasterDetailsRidget ridget, final MasterDetailsComposite control) {
 		ridget.setUIControl(control);
 		BINDING_MAN.bind(ridget, control.getUIControls());
 	}
 
 	private void clickRemove(final MasterDetailsRidget ridget) {
 		ridget.getRidget(IActionRidget.class, MDWidget.BIND_ID_REMOVE).fireAction();
 	}
 
 	private List<MDBean> createInput(final int numItems) {
 		final List<MDBean> result = new ArrayList<MDBean>();
 		for (int i = 0; i < numItems; i++) {
 			final String c1 = String.format("TestR%dC1", i);
 			final String c2 = String.format("TestR%dC2", i);
 			result.add(new MDBean(c1, c2));
 		}
 		return result;
 	}
 
 	private Event createSelectionEvent(final MDWidget widget, final Object first) {
 		final Event result = new Event();
 		result.type = SWT.Selection;
 		// hack; we only care about w.getData() so we use this here instead of the TableItem
 		widget.setData(first);
 		result.item = widget;
 		return result;
 	}
 
 	private boolean hasMandatory(final IMarkable ridget) {
 		boolean result = false;
 		final Collection<MandatoryMarker> markers = ridget.getMarkersOfType(MandatoryMarker.class);
 		for (final MandatoryMarker marker : markers) {
 			if (!marker.isDisabled()) {
 				result = true;
 				break;
 			}
 		}
 		return result;
 	}
 
 	private void unbindUIControl(final IMasterDetailsRidget ridget, final MasterDetailsComposite control) {
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
 
 		MDBean(final String column1, final String column2) {
 			this.column1 = column1;
 			this.column2 = column2;
 		}
 
 		public String getColumn1() {
 			return column1;
 		}
 
 		public String getColumn2() {
 			return column2;
 		}
 
 		public void setColumn1(final String column1) {
 			firePropertyChanged("column1", this.column1, this.column1 = column1);
 		}
 
 		public void setColumn2(final String column2) {
 			firePropertyChanged("column2", this.column2, this.column2 = column2);
 		}
 
 		@Override
 		public String toString() {
 			return String.format("[%s, %s]", column1, column2);
 		}
 
 		@Override
 		public boolean equals(final Object obj) {
 			if (obj == this) {
 				return true;
 			}
 			if (!(obj instanceof MDBean)) {
 				return false;
 			}
 			final MDBean bean = (MDBean) obj;
 			return column1.equals(bean.column1) && column2.equals(bean.column2);
 		}
 	}
 
 	/**
 	 * A MasterDetailsComposite with a details area containing two text fields.
 	 */
 	private static class MDWidget extends MasterDetailsComposite {
 
 		private Text txtColumn1;
 		private Text txtColumn2;
 
 		public MDWidget(final Composite parent, final int style) {
 			super(parent, style, SWT.BOTTOM);
 		}
 
 		@Override
 		protected void createDetails(final Composite parent) {
 			GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parent);
 			final GridDataFactory hFill = GridDataFactory.fillDefaults().grab(true, false);
 
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
 		private boolean isValidMaster = true;
 
 		private int createCount;
 		private int removeCount;
 		private int applyCount;
 		private int prepareAppliedCount;
 		private int prepareSelectionCount;
 		private int selectionCount;
 		private Object lastItem;
 		private ITextRidget txtColumn1;
 		private ITextRidget txtColumn2;
 		private String validationResult;
 		private boolean updateActionsCalled;
 
 		public void configureRidgets(final IRidgetContainer container) {
 			checkContainer(container);
 
 			txtColumn1 = (ITextRidget) container.getRidget("txtColumn1");
 			txtColumn1.bindToModel(workingCopy, MDBean.PROPERTY_COLUMN_1);
 			txtColumn1.updateFromModel();
 
 			txtColumn2 = (ITextRidget) container.getRidget("txtColumn2");
 			txtColumn2.bindToModel(workingCopy, MDBean.PROPERTY_COLUMN_2);
 			txtColumn2.updateFromModel();
 		}
 
 		public Object copyBean(final Object source, final Object target) {
 			final MDBean from = source == null ? createWorkingCopy() : (MDBean) source;
 			final MDBean to = target == null ? createWorkingCopy() : (MDBean) target;
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
 		public String isValid(final IRidgetContainer container) {
 			checkContainer(container);
 			return validationResult;
 		}
 
 		@Override
 		public boolean isValidMaster(final IMasterDetailsRidget mdRidget) {
 			return isValidMaster;
 		}
 
 		@Override
 		public void updateDetails(final IRidgetContainer container) {
 			checkContainer(container);
 
 			final ITextRidget txtCol1 = (ITextRidget) container.getRidget("txtColumn1");
 			txtCol1.setEnabled(isTxtColumn1IsEnabled);
 			for (final IRidget ridget : container.getRidgets()) {
 				ridget.updateFromModel();
 			}
 		}
 
 		@Override
 		public void itemApplied(final Object changedItem) {
 			applyCount++;
 			lastItem = changedItem;
 		}
 
 		@Override
 		public void itemCreated(final Object newItem) {
 			createCount++;
 			lastItem = newItem;
 		}
 
 		@Override
 		public void itemRemoved(final Object oldItem) {
 			removeCount++;
 			lastItem = oldItem;
 		}
 
 		@Override
 		public void prepareItemApplied(final Object selection) {
 			prepareAppliedCount++;
 			lastItem = selection;
 		}
 
 		@Override
 		public void prepareItemSelected(final Object newSelection) {
 			prepareSelectionCount++;
 			lastItem = newSelection;
 		}
 
 		@Override
 		public void itemSelected(final Object newSelection) {
 			selectionCount++;
 			lastItem = newSelection;
 		}
 
 		@Override
 		public boolean isChanged(final Object source, final Object target) {
 			final MDBean sBean = (MDBean) source;
 			final MDBean tBean = (MDBean) target;
 			return !sBean.equals(tBean);
 		}
 
 		void setValidMaster(final boolean validMaster) {
 			isValidMaster = validMaster;
 		}
 
 		private void checkContainer(final IRidgetContainer container) {
 			assertNotNull(container.getRidget("txtColumn1"));
 			assertNotNull(container.getRidget("txtColumn2"));
 			assertEquals(2, container.getRidgets().size());
 		}
 
 		@Override
 		public void updateMasterDetailsActionRidgets(final IMasterDetailsActionRidgetFacade actionRidgetFacade,
 				final Object selection) {
 			updateActionsCalled = true;
 		}
 	}
 
 	/**
 	 * PropertyChangeListener stub used for testing.
 	 */
 	private static final class FTPropertyChangeListener implements PropertyChangeListener {
 
 		private int count;
 		private PropertyChangeEvent event;
 
 		public void propertyChange(final PropertyChangeEvent event) {
 			count++;
 			this.event = event;
 		}
 	}
 
 }
