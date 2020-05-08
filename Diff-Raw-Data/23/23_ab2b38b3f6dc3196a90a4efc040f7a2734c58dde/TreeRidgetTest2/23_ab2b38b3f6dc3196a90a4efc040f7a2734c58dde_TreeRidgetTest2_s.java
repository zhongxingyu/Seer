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
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateListStrategy;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.databinding.observable.value.WritableValue;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 import org.eclipse.riena.beans.common.Person;
 import org.eclipse.riena.internal.ui.swt.test.UITestHelper;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.ITreeRidget;
import org.eclipse.riena.ui.ridgets.ISelectableRidget.SelectionType;
 import org.eclipse.riena.ui.ridgets.tree2.ITreeNode;
 import org.eclipse.riena.ui.ridgets.tree2.TreeNode;
 import org.eclipse.riena.ui.tests.base.TestMultiSelectionBean;
 import org.eclipse.riena.ui.tests.base.TestSingleSelectionBean;
 
 /**
  * Tests for the class {@link TreeRidget}.
  * <p>
  * These tests use a TreeNode that wraps a {@link Person} and focus on methods
  * inherited from the {@link ISelectableRidget}.
  * 
  * @see TableRidgetTest
  */
 public class TreeRidgetTest2 extends AbstractSWTRidgetTest {
 
 	protected ITreeNode root;
 	protected ITreeNode rootChild1;
 	protected ITreeNode rootChild1Child1;
 	protected ITreeNode rootChild2;
 
 	private TestSingleSelectionBean singleSelectionBean;
 	private TestMultiSelectionBean multiSelectionBean;
 
 	@Override
 	protected Control createWidget(Composite parent) {
 		return new Tree(parent, SWT.MULTI);
 	}
 
 	@Override
 	protected IRidget createRidget() {
 		return new TreeRidget();
 	}
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
 		ITreeNode[] nodes = bindRidgetToModel();
 		root = nodes[0];
 		rootChild1 = nodes[1];
 		rootChild1Child1 = nodes[2];
 		rootChild2 = nodes[3];
 		singleSelectionBean = new TestSingleSelectionBean();
 		getRidget().bindSingleSelectionToModel(singleSelectionBean, TestSingleSelectionBean.PROPERTY_SELECTION);
 		multiSelectionBean = new TestMultiSelectionBean();
 		getRidget().bindMultiSelectionToModel(multiSelectionBean, TestMultiSelectionBean.PROPERTY_SELECTION);
 		getRidget().updateFromModel();
 		UITestHelper.readAndDispatch(getWidget());
 	}
 
 	@Override
 	protected ITreeRidget getRidget() {
 		return (ITreeRidget) super.getRidget();
 	}
 
 	@Override
 	protected final Tree getWidget() {
 		return (Tree) super.getWidget();
 	}
 
 	/**
 	 * fires a selection event manually - control.setXXX does not fire events
 	 */
 	protected final void fireSelectionEvent() {
 		Event event = new Event();
 		event.widget = getWidget();
 		event.type = SWT.Selection;
 		getWidget().notifyListeners(SWT.Selection, event);
 	}
 
 	// test methods
 	// /////////////
 
 	public void testClearSelection() {
 		ISelectableRidget ridget = getRidget();
 
 		ridget.setSelectionType(SelectionType.SINGLE);
 		ridget.setSelection(root);
 
 		assertTrue(ridget.getSelection().size() > 0);
 		assertTrue(getUIControlSelectedRowCount() > 0);
 
 		ridget.clearSelection();
 
 		assertEquals(0, ridget.getSelection().size());
 		assertEquals(0, getUIControlSelectedRowCount());
 
 		ridget.setSelectionType(SelectionType.MULTI);
 		ridget.setSelection(Arrays.asList(new Object[] { root, rootChild1 }));
 
 		assertTrue(ridget.getSelection().size() > 0);
 		assertTrue(getUIControlSelectedRowCount() > 0);
 
 		ridget.clearSelection();
 
 		assertEquals(0, ridget.getSelection().size());
 		assertEquals(0, getUIControlSelectedRowCount());
 	}
 
 	public void testGetSelection() {
 		ISelectableRidget ridget = getRidget();
 
 		assertNotNull(ridget.getSelection());
 		assertTrue(ridget.getSelection().isEmpty());
 
 		ridget.setSelectionType(ISelectableRidget.SelectionType.MULTI);
 		ridget.setSelection(Arrays.asList(new Object[] { rootChild1, rootChild1Child1 }));
 
 		assertEquals(2, ridget.getSelection().size());
 		assertEquals(getRowValue(1), ridget.getSelection().get(0));
 		assertEquals(getRowValue(2), ridget.getSelection().get(1));
 	}
 
 	public void testSetSelectionList() {
 		ISelectableRidget ridget = getRidget();
 
 		assertNull(singleSelectionBean.getSelection());
 		assertTrue(multiSelectionBean.getSelectionList().isEmpty());
 		assertEquals(0, getUIControlSelectedRowCount());
 
 		java.util.List<Object> selBeans1 = new ArrayList<Object>(2);
 		selBeans1.add(getRowValue(0));
 		selBeans1.add(getRowValue(1));
 		ridget.setSelection(selBeans1);
 
 		assertEquals(1, getUIControlSelectedRowCount());
 		assertUIControlSelectionContains(0);
 		assertEquals(getRowValue(0), singleSelectionBean.getSelection());
 		assertEquals(1, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(0), multiSelectionBean.getSelectionList().get(0));
 
 		ridget.setSelectionType(ITableRidget.SelectionType.MULTI);
 		java.util.List<Object> selBeans2 = new ArrayList<Object>(2);
 		selBeans2.add(getRowValue(0));
 		selBeans2.add(getRowValue(1));
 		ridget.setSelection(selBeans2);
 
 		assertEquals(2, getUIControlSelectedRowCount());
 		assertUIControlSelectionContains(0);
 		assertUIControlSelectionContains(1);
 		assertEquals(getRowValue(0), singleSelectionBean.getSelection());
 		assertEquals(2, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(0), multiSelectionBean.getSelectionList().get(0));
 		assertEquals(getRowValue(1), multiSelectionBean.getSelectionList().get(1));
 
 		ridget.setSelectionType(ITableRidget.SelectionType.MULTI);
 		java.util.List<Object> selBeans3 = new ArrayList<Object>(1);
 		selBeans3.add(new Object());
 		ridget.setSelection(selBeans3);
 
 		assertEquals(0, getUIControlSelectedRowCount());
 		assertNull(singleSelectionBean.getSelection());
 		assertTrue(multiSelectionBean.getSelectionList().isEmpty());
 
 		try {
 			ridget.setSelection((List<?>) null);
 			fail();
 		} catch (RuntimeException e) {
 			ok();
 		}
 	}
 
 	public void testSetSelectionObject() {
 		ISelectableRidget ridget = getRidget();
 
 		assertNull(singleSelectionBean.getSelection());
 		assertTrue(multiSelectionBean.getSelectionList().isEmpty());
 		assertEquals(0, getUIControlSelectedRowCount());
 
 		ridget.setSelection(getRowValue(0));
 
 		assertEquals(1, getUIControlSelectedRowCount());
 		assertUIControlSelectionContains(0);
 		assertEquals(getRowValue(0), singleSelectionBean.getSelection());
 		assertEquals(1, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(0), multiSelectionBean.getSelectionList().get(0));
 
 		ridget.setSelection(new Object());
 
 		assertEquals(0, getUIControlSelectedRowCount());
 		assertNull(singleSelectionBean.getSelection());
 		assertEquals(0, multiSelectionBean.getSelectionList().size());
 
 		ridget.setSelection((Object) null);
 
 		assertEquals(0, getUIControlSelectedRowCount());
 		assertNull(singleSelectionBean.getSelection());
 		assertEquals(0, multiSelectionBean.getSelectionList().size());
 	}
 
 	public void testSetSelectionWithNoModel() {
 		ISelectableRidget ridget = (ISelectableRidget) createRidget();
 
 		try {
 			ridget.setSelection((Object) null);
 			fail();
 		} catch (BindingException bex) {
 			ok();
 		}
 
 		try {
 			ridget.setSelection(Collections.EMPTY_LIST);
 			fail();
 		} catch (BindingException bex) {
 			ok();
 		}
 	}
 
 	public void testUpdateMultiSelectionCustomBinding() {
 		ISelectableRidget ridget = getRidget();
 
 		ridget.setSelectionType(ITableRidget.SelectionType.MULTI);
 		WritableList customMultiSelectionObservable = new WritableList();
 		DataBindingContext dbc = new DataBindingContext();
 		dbc.bindList(ridget.getMultiSelectionObservable(), customMultiSelectionObservable, new UpdateListStrategy(
 				UpdateListStrategy.POLICY_UPDATE), new UpdateListStrategy(UpdateListStrategy.POLICY_UPDATE));
 
 		setUIControlRowSelectionInterval(0, 2);
 
 		assertEquals(3, getUIControlSelectedRowCount());
 		assertEquals(0, getUIControlSelectedRows()[0]);
 		assertEquals(1, getUIControlSelectedRows()[1]);
 		assertEquals(2, getUIControlSelectedRows()[2]);
 		assertEquals(3, customMultiSelectionObservable.size());
 		assertEquals(getRowValue(0), customMultiSelectionObservable.get(0));
 		assertEquals(getRowValue(1), customMultiSelectionObservable.get(1));
 		assertEquals(getRowValue(2), customMultiSelectionObservable.get(2));
 
 		customMultiSelectionObservable.add(getRowValue(0));
 		customMultiSelectionObservable.clear();
 		customMultiSelectionObservable.add(getRowValue(0));
 		customMultiSelectionObservable.add(getRowValue(2));
 
 		assertEquals(2, getUIControlSelectedRowCount());
 		assertEquals(0, getUIControlSelectedRows()[0]);
 		assertEquals(2, getUIControlSelectedRows()[1]);
 		assertEquals(2, customMultiSelectionObservable.size());
 		assertEquals(getRowValue(0), customMultiSelectionObservable.get(0));
 		assertEquals(getRowValue(2), customMultiSelectionObservable.get(1));
 	}
 
 	public void testUpdateMultiSelectionFromControl() {
 		ISelectableRidget ridget = getRidget();
 
 		ridget.setSelectionType(ITableRidget.SelectionType.MULTI);
 
 		assertNull(singleSelectionBean.getSelection());
 		assertTrue(multiSelectionBean.getSelectionList().isEmpty());
 
 		setUIControlRowSelectionInterval(1, 2);
 
 		assertEquals(getRowValue(1), singleSelectionBean.getSelection());
 		assertEquals(2, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(1), multiSelectionBean.getSelectionList().get(0));
 		assertEquals(getRowValue(2), multiSelectionBean.getSelectionList().get(1));
 
 		TreeItem[] items = { getUIControlItem(0), getUIControlItem(2) };
 		getWidget().setSelection(items);
 		fireSelectionEvent();
 
 		assertEquals(getRowValue(0), singleSelectionBean.getSelection());
 		assertEquals(2, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(0), multiSelectionBean.getSelectionList().get(0));
 		assertEquals(getRowValue(2), multiSelectionBean.getSelectionList().get(1));
 	}
 
 	public void testUpdateMultiSelectionFromModel() {
 		ISelectableRidget ridget = getRidget();
 
 		ridget.setSelectionType(ITableRidget.SelectionType.MULTI);
 		setUIControlRowSelectionInterval(0, 2);
 
		multiSelectionBean.getSelectionList().clear();
 		multiSelectionBean.getSelectionList().add(getRowValue(1));
 		multiSelectionBean.getSelectionList().add(getRowValue(2));
 
 		assertEquals(3, getUIControlSelectedRowCount());
 		assertEquals(0, getUIControlSelectedRows()[0]);
 		assertEquals(1, getUIControlSelectedRows()[1]);
 		assertEquals(2, getUIControlSelectedRows()[2]);
 		assertEquals(getRowValue(0), singleSelectionBean.getSelection());
 		assertEquals(2, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(1), multiSelectionBean.getSelectionList().get(0));
 		assertEquals(getRowValue(2), multiSelectionBean.getSelectionList().get(1));
 
 		ridget.updateMultiSelectionFromModel();
 
 		assertEquals(2, getUIControlSelectedRowCount());
 		assertEquals(1, getUIControlSelectedRows()[0]);
 		assertEquals(2, getUIControlSelectedRows()[1]);
 		assertEquals(getRowValue(1), singleSelectionBean.getSelection());
 		assertEquals(2, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(1), multiSelectionBean.getSelectionList().get(0));
 		assertEquals(getRowValue(2), multiSelectionBean.getSelectionList().get(1));
 	}
 
 	public void testUpdateMultiSelectionFromModelWhenUnbound() {
 		ISelectableRidget ridget = getRidget();
 
 		ridget.setSelectionType(ITableRidget.SelectionType.MULTI);
 		setUIControlRowSelectionInterval(0, 2);
 
		multiSelectionBean.getSelectionList().clear();
 		multiSelectionBean.getSelectionList().add(getRowValue(1));
 		multiSelectionBean.getSelectionList().add(getRowValue(2));
 
 		assertEquals(3, getUIControlSelectedRowCount());
 		assertEquals(0, getUIControlSelectedRows()[0]);
 		assertEquals(1, getUIControlSelectedRows()[1]);
 		assertEquals(2, getUIControlSelectedRows()[2]);
 		assertEquals(getRowValue(0), singleSelectionBean.getSelection());
 		assertEquals(2, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(1), multiSelectionBean.getSelectionList().get(0));
 		assertEquals(getRowValue(2), multiSelectionBean.getSelectionList().get(1));
 
 		ridget.setUIControl(null);
 		ridget.updateMultiSelectionFromModel();
 		ridget.setUIControl(getWidget());
 
 		assertEquals(getRowValue(1), singleSelectionBean.getSelection());
 		assertEquals(2, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(1), multiSelectionBean.getSelectionList().get(0));
 		assertEquals(getRowValue(2), multiSelectionBean.getSelectionList().get(1));
 	}
 
 	public void testUpdateMultiSelectionFromRidgetOnRebind() {
 		ISelectableRidget ridget = getRidget();
 
 		ridget.setSelectionType(ITableRidget.SelectionType.MULTI);
 		setUIControlRowSelectionInterval(1, 2);
 		ridget.setUIControl(null); // unbind
 		clearUIControlRowSelection();
 
 		assertEquals(0, getUIControlSelectedRowCount());
 		assertEquals(getRowValue(1), singleSelectionBean.getSelection());
 		assertEquals(2, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(1), multiSelectionBean.getSelectionList().get(0));
 		assertEquals(getRowValue(2), multiSelectionBean.getSelectionList().get(1));
 
 		ridget.setUIControl(getWidget()); // rebind
 
 		assertEquals(2, getUIControlSelectedRowCount());
 		assertEquals(1, getUIControlSelectedRows()[0]);
 		assertEquals(2, getUIControlSelectedRows()[1]);
 		assertEquals(getRowValue(1), singleSelectionBean.getSelection());
 		assertEquals(2, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(1), multiSelectionBean.getSelectionList().get(0));
 		assertEquals(getRowValue(2), multiSelectionBean.getSelectionList().get(1));
 	}
 
 	public void testUpdateSingleSelectionCustomBinding() {
 		ISelectableRidget ridget = getRidget();
 
 		WritableValue customSingleSelectionObservable = new WritableValue();
 		DataBindingContext dbc = new DataBindingContext();
 		dbc.bindValue(ridget.getSingleSelectionObservable(), customSingleSelectionObservable, new UpdateValueStrategy(
 				UpdateValueStrategy.POLICY_NEVER), new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));
 
 		setUIControlRowSelectionInterval(1, 1);
 
 		assertEquals(1, getUIControlSelectedRowCount());
 		assertUIControlSelectionContains(1);
 		assertNull(customSingleSelectionObservable.getValue());
 
 		customSingleSelectionObservable.setValue(getRowValue(0));
 
 		assertEquals(1, getUIControlSelectedRowCount());
 		assertUIControlSelectionContains(0);
 		assertEquals(getRowValue(0), customSingleSelectionObservable.getValue());
 	}
 
 	public void testUpdateSingleSelectionFromControl() {
 		assertNull(singleSelectionBean.getSelection());
 		assertTrue(multiSelectionBean.getSelectionList().isEmpty());
 
 		setUIControlRowSelectionInterval(1, 1);
 
 		assertEquals(getRowValue(1), singleSelectionBean.getSelection());
 		assertEquals(1, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(1), multiSelectionBean.getSelectionList().get(0));
 	}
 
 	public void testUpdateSingleSelectionFromModel() {
 		ISelectableRidget ridget = getRidget();
 
 		setUIControlRowSelectionInterval(1, 1);
 		singleSelectionBean.setSelection(getRowValue(0));
 
 		assertEquals(1, getUIControlSelectedRowCount());
 		assertUIControlSelectionContains(1);
 		assertEquals(getRowValue(0), singleSelectionBean.getSelection());
 		assertEquals(1, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(1), multiSelectionBean.getSelectionList().get(0));
 
 		ridget.updateSingleSelectionFromModel();
 
 		assertEquals(1, getUIControlSelectedRowCount());
 		assertUIControlSelectionContains(0);
 		assertEquals(getRowValue(0), singleSelectionBean.getSelection());
 		assertEquals(1, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(0), multiSelectionBean.getSelectionList().get(0));
 	}
 
 	public void testUpdateSingleSelectionFromModelWhenUnbound() {
 		ISelectableRidget ridget = getRidget();
 
 		setUIControlRowSelectionInterval(1, 1);
 		singleSelectionBean.setSelection(getRowValue(0));
 
 		assertEquals(1, getUIControlSelectedRowCount());
 		assertUIControlSelectionContains(1);
 		assertEquals(getRowValue(0), singleSelectionBean.getSelection());
 		assertEquals(1, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(1), multiSelectionBean.getSelectionList().get(0));
 
 		ridget.setUIControl(null); // unbind
 		ridget.updateSingleSelectionFromModel();
 		ridget.setUIControl(getWidget()); // rebind
 
 		assertEquals(getRowValue(0), singleSelectionBean.getSelection());
 		assertEquals(1, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(0), multiSelectionBean.getSelectionList().get(0));
 	}
 
 	public void testUpdateSingleSelectionFromRidgetOnRebind() {
 		ISelectableRidget ridget = getRidget();
 
 		setUIControlRowSelectionInterval(2, 2);
 		ridget.setUIControl(null); // unbind
 		clearUIControlRowSelection();
 
 		assertEquals(getRowValue(2), singleSelectionBean.getSelection());
 		assertEquals(1, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(2), multiSelectionBean.getSelectionList().get(0));
 
 		ridget.setUIControl(getWidget()); // rebind
 
 		assertEquals(1, getUIControlSelectedRowCount());
 		assertUIControlSelectionContains(2);
 		assertEquals(getRowValue(2), singleSelectionBean.getSelection());
 		assertEquals(1, multiSelectionBean.getSelectionList().size());
 		assertEquals(getRowValue(2), multiSelectionBean.getSelectionList().get(0));
 	}
 
 	public void testSelectionEventsSelectionTypeSingle() {
 		ISelectableRidget ridget = getRidget();
 		ridget.setSelectionType(SelectionType.SINGLE);
 
 		assertNull(singleSelectionBean.getSelection());
 		assertTrue(multiSelectionBean.getSelectionList().isEmpty());
 		assertEquals(0, getUIControlSelectedRowCount());
 
 		// ridget.addPropertyChangeListener(new PropertyChangeListener() {
 		// public void propertyChange(PropertyChangeEvent evt) {
 		// String name = evt.getPropertyName();
 		// Object old = evt.getOldValue();
 		// Object newv = evt.getNewValue();
 		// System.out.println(name + ", " + old + " -> " + newv);
 		// }
 		// });
 
 		java.util.List<?> oldSelection = Collections.EMPTY_LIST;
 		java.util.List<?> newSelection = Arrays.asList(new Object[] { root });
 		expectPropertyChangeEvent(ISelectableRidget.PROPERTY_SELECTION, oldSelection, newSelection);
 
 		ridget.setSelection(root);
 
 		verifyPropertyChangeEvents();
 
 		expectNoPropertyChangeEvent();
 
 		ridget.setSelection(root);
 
 		verifyPropertyChangeEvents();
 
 		oldSelection = newSelection;
 		newSelection = Collections.EMPTY_LIST;
 		expectPropertyChangeEvent(ISelectableRidget.PROPERTY_SELECTION, oldSelection, newSelection);
 
 		ridget.setSelection(Collections.EMPTY_LIST);
 
 		verifyPropertyChangeEvents();
 
 		oldSelection = Collections.EMPTY_LIST;
 		newSelection = Arrays.asList(new Object[] { rootChild1 });
 		expectPropertyChangeEvent(ISelectableRidget.PROPERTY_SELECTION, oldSelection, newSelection);
 
 		ridget.setSelection(Arrays.asList(new Object[] { rootChild1, rootChild1Child1 }));
 
 		verifyPropertyChangeEvents();
 	}
 
 	public void testSelectionEventsSelectionTypeMulti() {
 		ISelectableRidget ridget = getRidget();
 		ridget.setSelectionType(SelectionType.MULTI);
 
 		assertNull(singleSelectionBean.getSelection());
 		assertTrue(multiSelectionBean.getSelectionList().isEmpty());
 		assertEquals(0, getUIControlSelectedRowCount());
 
 		// ridget.addPropertyChangeListener(new PropertyChangeListener() {
 		// public void propertyChange(PropertyChangeEvent evt) {
 		// String name = evt.getPropertyName();
 		// Object old = evt.getOldValue();
 		// Object newv = evt.getNewValue();
 		// System.out.println(name + ", " + old + " -> " + newv);
 		// }
 		// });
 
 		java.util.List<?> oldSelection = Collections.EMPTY_LIST;
 		java.util.List<?> newSelection = Arrays.asList(new Object[] { root });
 		expectPropertyChangeEvent(ISelectableRidget.PROPERTY_SELECTION, oldSelection, newSelection);
 
 		ridget.setSelection(root);
 
 		verifyPropertyChangeEvents();
 
 		expectNoPropertyChangeEvent();
 
 		ridget.setSelection(root);
 
 		verifyPropertyChangeEvents();
 
 		oldSelection = newSelection;
 		newSelection = Collections.EMPTY_LIST;
 		expectPropertyChangeEvent(ISelectableRidget.PROPERTY_SELECTION, oldSelection, newSelection);
 
 		ridget.setSelection(Collections.EMPTY_LIST);
 
 		verifyPropertyChangeEvents();
 
 		oldSelection = Collections.EMPTY_LIST;
 		newSelection = Arrays.asList(new Object[] { rootChild1, rootChild1Child1 });
 		expectPropertyChangeEvent(ISelectableRidget.PROPERTY_SELECTION, oldSelection, newSelection);
 
 		ridget.setSelection(Arrays.asList(new Object[] { rootChild1, rootChild1Child1 }));
 
 		verifyPropertyChangeEvents();
 	}
 
 	public void testSetSelectionTypeNull() {
 		ISelectableRidget ridget = getRidget();
 
 		try {
 			ridget.setSelectionType(null);
 			fail();
 		} catch (RuntimeException npe) {
 			ok();
 		}
 	}
 
 	public void testSetSelectionTypeNONE() {
 		ISelectableRidget ridget = getRidget();
 
 		try {
 			ridget.setSelectionType(ISelectableRidget.SelectionType.NONE);
 			fail();
 		} catch (RuntimeException iae) {
 			ok();
 		}
 	}
 
 	public void testGetUIControl() throws Exception {
 		Tree control = getWidget();
 		assertEquals(control, getRidget().getUIControl());
 	}
 
 	public void testUpdateFromModelPreservesSelection() throws Exception {
 		ITreeRidget ridget = getRidget();
 
 		ridget.setSelection(rootChild2);
 
 		assertSame(rootChild2, ridget.getSelection().get(0));
 
 		List<ITreeNode> children = root.getChildren();
 		children.remove(rootChild1);
 		root.setChildren(children);
 		ridget.updateFromModel();
 
 		assertSame(rootChild2, ridget.getSelection().get(0));
 	}
 
 	public void testUpdateFromModelRemovesSelection() {
 		ITreeRidget ridget = getRidget();
 
 		ridget.setSelection(rootChild1Child1);
 
 		assertSame(rootChild1Child1, ridget.getSelection().get(0));
 
 		List<ITreeNode> children = root.getChildren();
 		children.remove(rootChild1);
 		root.setChildren(children);
 		ridget.updateFromModel();
 
 		assertTrue(ridget.getSelection().isEmpty());
 	}
 
 	public void testUpdateRootFromModelPreservesSelection() {
 		ITreeRidget ridget = getRidget();
 		ITreeNode node1 = new TreeNode(new Person("Doe", "John"));
 		ITreeNode node2 = new TreeNode(new Person("Doe", "Jane"));
 		ITreeNode[] roots = { node1, node2 };
 		getRidget().bindToModel(roots, ITreeNode.class, ITreeNode.PROPERTY_CHILDREN, ITreeNode.PROPERTY_PARENT,
 				ITreeNode.PROPERTY_VALUE);
 
 		ridget.setSelection(node1);
 
 		assertSame(node1, ridget.getSelection().get(0));
 
 		ITreeNode swap = roots[0];
 		roots[0] = roots[1];
 		roots[1] = swap;
 		ridget.updateFromModel();
 
 		assertSame(node1, ridget.getSelection().get(0));
 	}
 
 	public void testUpdateRootFromModelRemovesSelection() {
 		ITreeRidget ridget = getRidget();
 		ITreeNode node1 = new TreeNode(new Person("Doe", "John"));
 		ITreeNode node2 = new TreeNode(new Person("Doe", "Jane"));
 		ITreeNode[] roots = { node1, node2 };
 		getRidget().bindToModel(roots, ITreeNode.class, ITreeNode.PROPERTY_CHILDREN, ITreeNode.PROPERTY_PARENT,
 				ITreeNode.PROPERTY_VALUE);
 
 		ridget.setSelection(node1);
 
 		assertSame(node1, ridget.getSelection().get(0));
 
 		roots[0] = new TreeNode(new Person("New", "Person 1"));
 		roots[1] = new TreeNode(new Person("New", "Person 2"));
 		ridget.updateFromModel();
 
 		assertTrue(ridget.getSelection().isEmpty());
 	}
 
 	public void testContainsOption() {
 		ITreeRidget ridget = getRidget();
 
 		assertTrue(ridget.containsOption(root));
 		assertTrue(ridget.containsOption(rootChild1));
 		assertTrue(ridget.containsOption(rootChild1Child1));
 		assertTrue(ridget.containsOption(rootChild2));
 
 		TreeNode[] newRoots = { new TreeNode("newRoot") };
 
 		assertFalse(ridget.containsOption(null));
 		assertFalse(ridget.containsOption(newRoots[0]));
 
 		getRidget().bindToModel(newRoots, ITreeNode.class, ITreeNode.PROPERTY_CHILDREN, ITreeNode.PROPERTY_PARENT,
 				ITreeNode.PROPERTY_VALUE);
 
 		assertFalse(ridget.containsOption(root));
 		assertTrue(ridget.containsOption(newRoots[0]));
 	}
 
 	public void testSelectionType() throws Exception {
 		ITreeRidget ridget = getRidget();
 		Tree control = getWidget();
 
 		assertEquals(ITreeRidget.SelectionType.SINGLE, ridget.getSelectionType());
 
 		ridget.setSelectionType(ITreeRidget.SelectionType.MULTI);
 
 		assertEquals(ITreeRidget.SelectionType.MULTI, ridget.getSelectionType());
 
 		try {
 			ridget.setSelectionType(ITreeRidget.SelectionType.NONE);
 			fail("IllegalArgumentException expected");
 		} catch (IllegalArgumentException expected) {
 			ok("IllegalArgumentException expected");
 		}
 
 		assertEquals(ITreeRidget.SelectionType.MULTI, ridget.getSelectionType());
 
 		ridget.setUIControl(null); // unbind
 		ridget.setSelectionType(ITreeRidget.SelectionType.SINGLE);
 
 		assertEquals(ITreeRidget.SelectionType.SINGLE, ridget.getSelectionType());
 
 		ridget.setUIControl(control); // rebind
 
 		assertEquals(ITreeRidget.SelectionType.SINGLE, ridget.getSelectionType());
 	}
 
 	public void testSetSelectionTypeIsEnforced() {
 		ITreeRidget ridget = getRidget();
 		Tree control = getWidget();
 
 		assertEquals(ISelectableRidget.SelectionType.SINGLE, ridget.getSelectionType());
 		assertTrue((control.getStyle() & SWT.MULTI) != 0);
 
 		ridget.setSelection(Arrays.asList(root, rootChild1));
 
 		// single selection is enforced
 		assertEquals(1, ridget.getSelection().size());
 		assertEquals(1, control.getSelectionCount());
 
 		// multiple selection is now allowed
 		ridget.setSelectionType(ISelectableRidget.SelectionType.MULTI);
 		ridget.setSelection(Arrays.asList(root, rootChild1));
 
 		assertEquals(2, ridget.getSelection().size());
 		assertEquals(2, control.getSelectionCount());
 	}
 
 	public void testDisabledItemNotSelectableByAPI() {
 		ITreeRidget ridget = getRidget();
 		Tree control = getWidget();
 
 		TreeNode node1 = new TreeNode("node1");
 		TreeNode node2 = new TreeNode("node2");
 		node2.setEnabled(false);
 		TreeNode[] roots = { node1, node2 };
 		getRidget().bindToModel(roots, TreeNode.class, TreeNode.PROPERTY_CHILDREN, TreeNode.PROPERTY_PARENT,
 				TreeNode.PROPERTY_VALUE, TreeNode.PROPERTY_ENABLED, null);
 
 		ridget.setSelection(node1);
 
 		assertEquals(1, control.getSelectionCount());
 		assertEquals(1, ridget.getSelection().size());
 		assertSame(node1, ridget.getSelection().get(0));
 
 		ridget.setSelection(node2);
 
 		assertEquals(1, control.getSelectionCount());
 		assertEquals(1, ridget.getSelection().size());
 		assertSame(node1, ridget.getSelection().get(0));
 
 		ridget.setSelection(Arrays.asList(new Object[] { node1, node2 }));
 
 		assertEquals(1, control.getSelectionCount());
 		assertEquals(1, ridget.getSelection().size());
 		assertSame(node1, ridget.getSelection().get(0));
 	}
 
 	public void testDisabledItemNotSelectableByUser() {
 		ITreeRidget ridget = getRidget();
 		Tree control = getWidget();
 
 		TreeNode node1 = new TreeNode("node1");
 		TreeNode node2 = new TreeNode("node2");
 		node2.setEnabled(false);
 		TreeNode[] roots = { node1, node2 };
 		ridget.bindToModel(roots, TreeNode.class, TreeNode.PROPERTY_CHILDREN, TreeNode.PROPERTY_PARENT,
 				TreeNode.PROPERTY_VALUE, TreeNode.PROPERTY_ENABLED, null);
 
 		ridget.setSelection(node1);
 
 		assertEquals(1, control.getSelectionCount());
 		assertEquals(1, ridget.getSelection().size());
 		assertSame(node1, ridget.getSelection().get(0));
 
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
 
 		assertEquals(1, control.getSelectionCount());
 		assertEquals(1, ridget.getSelection().size());
 		assertSame(node1, ridget.getSelection().get(0));
 
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_UP);
 
 		assertEquals(1, control.getSelectionCount());
 		assertEquals(1, ridget.getSelection().size());
 		assertSame(node1, ridget.getSelection().get(0));
 	}
 
 	public void testInvisibleItemsAreNotShown() {
 		ITreeRidget ridget = getRidget();
 		Tree control = getWidget();
 
 		TreeNode node1 = new TreeNode("node1");
 		TreeNode node2 = new TreeNode("node2");
 		node2.setEnabled(false);
 		TreeNode[] roots = { node1, node2 };
 		ridget.bindToModel(roots, TreeNode.class, TreeNode.PROPERTY_CHILDREN, TreeNode.PROPERTY_PARENT,
 				TreeNode.PROPERTY_VALUE, null, TreeNode.PROPERTY_VISIBLE);
 
 		assertEquals(2, control.getItemCount());
 
 		node2.setVisible(false);
 
 		assertEquals(1, control.getItemCount());
 
 		node2.setVisible(true);
 
 		assertEquals(2, control.getItemCount());
 	}
 
 	public void testInvisibleItemsAreNotShownWithHiddenRoot() {
 		ITreeRidget ridget = getRidget();
 		Tree control = getWidget();
 
 		TreeNode node1 = new TreeNode("node1");
 		TreeNode node1child1 = new TreeNode(node1, "child 1");
 		TreeNode[] roots = { node1 };
 		ridget.setRootsVisible(false);
 		ridget.bindToModel(roots, TreeNode.class, TreeNode.PROPERTY_CHILDREN, TreeNode.PROPERTY_PARENT,
 				TreeNode.PROPERTY_VALUE, null, TreeNode.PROPERTY_VISIBLE);
 
 		assertEquals(1, control.getItemCount());
 
 		node1child1.setVisible(false);
 
 		assertEquals(0, control.getItemCount());
 
 		node1child1.setVisible(true);
 
 		assertEquals(1, control.getItemCount());
 	}
 
 	public void testSetRootsVisible() {
 		ITreeRidget ridget = getRidget();
 
 		assertTrue(ridget.getRootsVisible());
 
 		ridget.setRootsVisible(false);
 
 		assertFalse(ridget.getRootsVisible());
 
 		ridget.setRootsVisible(true);
 
 		assertTrue(ridget.getRootsVisible());
 	}
 
 	public void testSetRootsVisibleWithModel() {
 		ITreeRidget ridget = getRidget();
 		Tree control = getWidget();
 
 		ridget.setRootsVisible(false);
 		Object[] roots = { root };
 		getRidget().bindToModel(roots, ITreeNode.class, ITreeNode.PROPERTY_CHILDREN, ITreeNode.PROPERTY_PARENT,
 				ITreeNode.PROPERTY_VALUE);
 		ridget.expandAll();
 
 		assertEquals(3, TreeUtils.getItemCount(control));
 
 		ridget.updateFromModel();
 
 		assertEquals(3, TreeUtils.getItemCount(control));
 
 		ridget.setRootsVisible(true);
 
 		ridget.updateFromModel();
 		ridget.expandAll();
 
 		assertEquals(4, TreeUtils.getItemCount(control));
 	}
 
 	public void testSetRootsFalseDoesRefresh() {
 		ITreeRidget ridget = getRidget();
 		Tree control = getWidget();
 
 		ridget.setRootsVisible(false);
 		ridget.updateFromModel();
 
 		ridget.expandAll();
 
 		assertEquals(3, TreeUtils.getItemCount(control));
 
 		ITreeNode sibling = new TreeNode(root, "sibling");
 
 		assertEquals(4, TreeUtils.getItemCount(control));
 
 		List<ITreeNode> children = root.getChildren();
 		children.remove(sibling);
 		root.setChildren(children);
 
 		assertEquals(3, TreeUtils.getItemCount(control));
 
 		new TreeNode(rootChild1, "child");
 
 		assertEquals(4, TreeUtils.getItemCount(control));
 	}
 
 	/**
 	 * Tests that for single selection, the ridget selection state and the ui
 	 * selection state cannot be changed by the user when ridget is set to
 	 * "output only".
 	 */
 	public void testOutputSingleSelectionCannotBeChangedFromUI() {
 		ITreeRidget ridget = getRidget();
 		Tree control = getWidget();
 
 		ridget.setSelectionType(SelectionType.SINGLE);
 
 		assertEquals(0, ridget.getSelection().size());
 		assertEquals(0, control.getSelectionCount());
 
 		ridget.setOutputOnly(true);
 		control.setFocus();
 		// move down and up to select row 0; space does not select in tables
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_UP);
 
 		assertEquals(0, ridget.getSelection().size());
 		assertEquals(0, control.getSelectionCount());
 
 		ridget.setOutputOnly(false);
 		control.setFocus();
 		// move down and up to select row 0; space does not select in tables
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_UP);
 
 		assertEquals(1, ridget.getSelection().size());
 		assertEquals(1, control.getSelectionCount());
 	}
 
 	/**
 	 * Ensure tree does not jump back to the current selection when in 'output
 	 * only' mode. See {@link https://bugs.eclipse.org/245632}.
 	 */
 	public void testBug245632() {
 		ITreeRidget ridget = getRidget();
 		Tree control = getWidget();
 
 		ridget.expandAll();
 		ridget.setSelection(root);
 		ridget.setOutputOnly(true);
 
 		final TreeItem topItem = control.getTopItem();
 		assertSame(root, ridget.getSelection().get(0));
 
 		control.setFocus();
 		// press end to select the last element
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_END);
 
 		// read only -> selection unchanged
 		assertSame(root, ridget.getSelection().get(0));
 		// if we didn't scroll up, we should have DIFFERENT top item
 		assertNotSame(topItem, control.getTopItem());
 	}
 
 	/**
 	 * Tests that for multiple selection, the ridget selection state and the ui
 	 * selection state cannot be changed by the user when ridget is set to
 	 * "output only".
 	 */
 	public void testOutputMultipleSelectionCannotBeChangedFromUI() {
 		ITreeRidget ridget = getRidget();
 		Tree control = getWidget();
 
 		ridget.setSelectionType(SelectionType.MULTI);
 
 		assertEquals(0, ridget.getSelection().size());
 		assertEquals(0, control.getSelectionCount());
 
 		ridget.setOutputOnly(true);
 		control.setFocus();
 		// move down and up to select row 0; space does not select in tables
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_UP);
 
 		assertEquals(0, ridget.getSelection().size());
 		assertEquals(0, control.getSelectionCount());
 
 		ridget.setOutputOnly(false);
 		control.setFocus();
 		// move down and up to select row 0; space does not select in tables
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_UP);
 
 		assertEquals(1, ridget.getSelection().size());
 		assertEquals(1, control.getSelectionCount());
 	}
 
 	/**
 	 * Tests that toggling output state on/off does not change the selection.
 	 */
 	public void testTogglingOutputDoesNotChangeSelection() {
 		ITreeRidget ridget = getRidget();
 
 		ridget.setSelection(root);
 
 		assertEquals(1, ridget.getSelection().size());
 
 		ridget.setOutputOnly(true);
 
 		assertEquals(1, ridget.getSelection().size());
 
 		ridget.setSelection((Object) null);
 
 		assertEquals(0, ridget.getSelection().size());
 
 		ridget.setOutputOnly(false);
 
 		assertEquals(0, ridget.getSelection().size());
 	}
 
 	/**
 	 * As per Bug 296639
 	 */
 	public void testSetSelectionWithNoBoundControl() {
 		ITreeRidget ridget = getRidget();
 
 		ridget.setSelection(rootChild1);
 
 		assertNotNull(ridget.getUIControl());
 		assertEquals(rootChild1, ridget.getSelection().get(0));
 
 		ridget.setUIControl(null);
 		ridget.setSelection(rootChild2);
 
 		assertNull(ridget.getUIControl());
 		assertEquals(rootChild2, ridget.getSelection().get(0));
 	}
 
 	/**
 	 * As per Bug 298033 comment #1
 	 */
 	public void testUpdateSingleSelectionFromModelWithNoBoundControl() {
 		ITreeRidget ridget = getRidget();
 
 		ridget.setSelectionType(SelectionType.SINGLE);
 		ridget.setSelection(rootChild1);
 
 		assertEquals(rootChild1, ridget.getSelection().get(0));
 
 		// remove selected element while not bound to a control
 		ridget.setUIControl(null);
 		removeNode(root, rootChild1);
 		ridget.updateFromModel();
 
 		assertNull(ridget.getUIControl());
 		assertTrue(ridget.getSelection().isEmpty());
 	}
 
 	/**
 	 * As per Bug 298033 comment #1
 	 */
 	public void testUpdateMultiSelectionFromModelWithNoBoundControl() {
 		ITreeRidget ridget = getRidget();
 
 		ridget.setSelectionType(SelectionType.MULTI);
 		ridget.setSelection(Arrays.asList(rootChild1, rootChild2));
 
 		assertEquals(2, ridget.getSelection().size());
 		assertEquals(rootChild1, ridget.getSelection().get(0));
 		assertEquals(rootChild2, ridget.getSelection().get(1));
 
 		// remove selected element while not bound to a control
 		ridget.setUIControl(null);
 		removeNode(root, rootChild1);
 		ridget.updateFromModel();
 
 		assertNull(ridget.getUIControl());
 		assertEquals(1, ridget.getSelection().size());
 		assertEquals(rootChild2, ridget.getSelection().get(0));
 	}
 
 	/**
 	 * As per Bug 298033
 	 */
 	public void testUpdateSingleSelectionFromModelWithBoundControl() {
 		ITreeRidget ridget = getRidget();
 
 		ridget.setSelectionType(SelectionType.SINGLE);
 		ridget.setSelection(rootChild1);
 
 		assertEquals(rootChild1, ridget.getSelection().get(0));
 
 		// remove selected element while bound to a control
 		removeNode(root, rootChild1);
 		ridget.updateFromModel();
 
 		assertNotNull(ridget.getUIControl());
 		assertTrue(ridget.getSelection().isEmpty());
 	}
 
 	/**
 	 * As per Bug 298033
 	 */
 	public void testUpdateMultiSelectionFromModelWithBoundControl() {
 		ITreeRidget ridget = getRidget();
 
 		ridget.setSelectionType(SelectionType.MULTI);
 		ridget.setSelection(Arrays.asList(rootChild1, rootChild2));
 
 		assertEquals(2, ridget.getSelection().size());
 		assertEquals(rootChild1, ridget.getSelection().get(0));
 		assertEquals(rootChild2, ridget.getSelection().get(1));
 
 		// remove selected element while bound to a control
 		removeNode(root, rootChild1);
 		ridget.updateFromModel();
 
 		assertNotNull(ridget.getUIControl());
 		assertEquals(1, ridget.getSelection().size());
 		assertEquals(rootChild2, ridget.getSelection().get(0));
 	}
 
 	// helping methods
 	// ////////////////
 
 	/**
 	 * Returns true if the control's selection contains the TreeItem
 	 * corresponding to the given mock "index" number (0: item for node1, 1:
 	 * item for node2, 2: item for node3).
 	 */
 	private void assertUIControlSelectionContains(int itemIndex) {
 		boolean result = false;
 		TreeItem item = getUIControlItem(itemIndex);
 		TreeItem[] selections = getWidget().getSelection();
 		for (int i = 0; !result && i < selections.length; i++) {
 			result = (item == selections[i]);
 		}
 		assertTrue("not selected in ui control: " + item, result);
 	}
 
 	private ITreeNode[] bindRidgetToModel() {
 		Collection<Person> persons = createPersonList();
 		Iterator<Person> iter = persons.iterator();
 		Person person1 = iter.next();
 		Person person2 = iter.next();
 		Person person3 = iter.next();
 		Person person4 = iter.next();
 
 		// node1 is the root
 		ITreeNode node1 = new TreeNode(person1);
 		// node2 and node4 will be visible by default because node1 gets
 		// autoexpanded
 		ITreeNode node2 = new TreeNode(node1, person2);
 		ITreeNode node4 = new TreeNode(node1, person4);
 		// node3 is on level-2, so it does not get autoexpanded
 		ITreeNode node3 = new TreeNode(node2, person3);
 
 		ITreeNode[] roots = { node1 };
 		getRidget().bindToModel(roots, ITreeNode.class, ITreeNode.PROPERTY_CHILDREN, ITreeNode.PROPERTY_PARENT,
 				ITreeNode.PROPERTY_VALUE);
 		return new ITreeNode[] { node1, node2, node3, node4 };
 	}
 
 	/**
 	 * Clears the selection in the control
 	 */
 	private void clearUIControlRowSelection() {
 		getWidget().deselectAll();
 		fireSelectionEvent();
 	}
 
 	private Collection<Person> createPersonList() {
 		Collection<Person> newList = new ArrayList<Person>();
 
 		Person person = new Person("Doe", "One");
 		person.setEyeColor(1);
 		newList.add(person);
 
 		person = new Person("Jackson", "Two");
 		person.setEyeColor(1);
 		newList.add(person);
 
 		person = new Person("Jackson", "Three");
 		person.setEyeColor(1);
 		newList.add(person);
 
 		person = new Person("Jackson", "John");
 		person.setEyeColor(3);
 		newList.add(person);
 
 		person = new Person("JJ Jr. Shabadoo", "Joey");
 		person.setEyeColor(3);
 		newList.add(person);
 
 		person = new Person("Johnson", "Jack");
 		person.setEyeColor(2);
 		newList.add(person);
 
 		person = new Person("Johnson", "Jane");
 		person.setEyeColor(3);
 		newList.add(person);
 
 		person = new Person("Zappa", "Frank");
 		person.setEyeColor(2);
 		newList.add(person);
 
 		return newList;
 	}
 
 	/**
 	 * Returns the number of selected rows
 	 */
 	private int getUIControlSelectedRowCount() {
 		return getWidget().getSelectionCount();
 	}
 
 	/**
 	 * Returns the "indices" of the selected tree items based on a mock "index"
 	 * scheme (0: item for node1, 1: item for node2, 2: item for node3).
 	 */
 	private int[] getUIControlSelectedRows() {
 		TreeItem[] selection = getWidget().getSelection();
 		int[] result = new int[selection.length];
 		for (int i = 0; i < result.length; i++) {
 			if (selection[i] == getUIControlItem(0)) {
 				result[i] = 0;
 			} else if (selection[i] == getUIControlItem(1)) {
 				result[i] = 1;
 			} else if (selection[i] == getUIControlItem(2)) {
 				result[i] = 2;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Return the TreeItem corresponding to the following mock "index" scheme:
 	 * 0: item for node1, 1: item for node2, 2: item for node3.
 	 * <p>
 	 * This method will fully expand the tree to ensure all tree items are
 	 * created.
 	 */
 	private TreeItem getUIControlItem(int index) {
 		getRidget().expandAll();
 		Tree control = getWidget();
 		switch (index) {
 		case 0:
 			return control.getItem(0);
 		case 1:
 			return control.getItem(0).getItem(0);
 		case 2:
 			return control.getItem(0).getItem(0).getItem(0);
 		case 3:
 			return control.getItem(0).getItem(1);
 		default:
 			throw new IndexOutOfBoundsException("index= " + index);
 		}
 	}
 
 	/**
 	 * Return the tree node corresponding to the following mock "index" scheme:
 	 * 0: item for node1, 1: item for node2, 2: item for node3.
 	 */
 	private ITreeNode getRowValue(int index) {
 		switch (index) {
 		case 0:
 			return root;
 		case 1:
 			return rootChild1;
 		case 2:
 			return rootChild1Child1;
 		default:
 			throw new IndexOutOfBoundsException("index= " + index);
 		}
 	}
 
 	private void removeNode(ITreeNode parent, ITreeNode toRemove) {
 		List<ITreeNode> children = parent.getChildren();
 		boolean removed = children.remove(toRemove);
 		assertTrue("failed to remove " + toRemove, removed);
 		parent.setChildren(children);
 	}
 
 	/**
 	 * Select one or more TreeItems based on a mock "index" scheme (0: item for
 	 * node1, 1: item for node2, 2: item for node3).
 	 * 
 	 * @param start
 	 *            the start index (>=0)
 	 * @param end
 	 *            the end index (>= start index; and < 3)
 	 */
 	private void setUIControlRowSelectionInterval(int start, int end) {
 		Assert.isLegal(0 <= start);
 		Assert.isLegal(start <= end);
 		Tree control = getWidget();
 		int length = (end - start) + 1;
 		TreeItem[] items = new TreeItem[length];
 		for (int i = 0; i < items.length; i++) {
 			items[i] = getUIControlItem(i + start);
 		}
 		control.setSelection(items);
 		fireSelectionEvent();
 	}
 
 }
