 /*******************************************************************************
  * Copyright (c) 2007, 2012 compeople AG and others.
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
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 
 import org.eclipse.core.databinding.observable.ChangeEvent;
 import org.eclipse.core.databinding.observable.IChangeListener;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.viewers.ColumnPixelData;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 import org.eclipse.riena.beans.common.Person;
 import org.eclipse.riena.beans.common.PersonManager;
 import org.eclipse.riena.beans.common.StringPojo;
 import org.eclipse.riena.beans.common.TypedComparator;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.ui.swt.test.UITestHelper;
 import org.eclipse.riena.internal.ui.swt.utils.TestUtils;
 import org.eclipse.riena.ui.common.ISortableByColumn;
 import org.eclipse.riena.ui.core.marker.RowErrorMessageMarker;
 import org.eclipse.riena.ui.ridgets.IColumnFormatter;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget.SelectionType;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.listener.ClickEvent;
 import org.eclipse.riena.ui.ridgets.listener.SelectionEvent;
 import org.eclipse.riena.ui.ridgets.swt.ColumnFormatter;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper;
 
 /**
  * Tests of the class {@link TableRidget}.
  */
 public class TableRidgetTest extends AbstractTableListRidgetTest {
 
 	@Override
 	protected Table createWidget(final Composite parent) {
 		final Table table = new Table(parent, SWT.MULTI);
 		table.setHeaderVisible(true);
 		new TableColumn(table, SWT.NONE);
 		new TableColumn(table, SWT.NONE);
 		return table;
 	}
 
 	@Override
 	protected ITableRidget createRidget() {
 		return new TableRidget();
 	}
 
 	@Override
 	protected Table getWidget() {
 		return (Table) super.getWidget();
 	}
 
 	@Override
 	protected TableRidget getRidget() {
 		return (TableRidget) super.getRidget();
 	}
 
 	@Override
 	protected void bindRidgetToModel() {
 		getRidget().bindToModel(manager, "persons", Person.class, new String[] { "firstname", "lastname" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				new String[] { "First Name", "Last Name" }); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	// test methods
 	// /////////////
 
 	public void testRidgetMapping() {
 		final SwtControlRidgetMapper mapper = SwtControlRidgetMapper.getInstance();
 		assertSame(TableRidget.class, mapper.getRidgetClass(getWidget()));
 	}
 
 	public void testBindToModel() {
 		final Table control = getWidget();
 
 		assertEquals(manager.getPersons().size(), control.getItemCount());
 		assertEquals(person1.getFirstname(), control.getItem(0).getText(0));
 		assertEquals(person2.getFirstname(), control.getItem(1).getText(0));
 		assertEquals(person3.getFirstname(), control.getItem(2).getText(0));
 
 		assertEquals(person1.getLastname(), control.getItem(0).getText(1));
 		assertEquals(person2.getLastname(), control.getItem(1).getText(1));
 		assertEquals(person3.getLastname(), control.getItem(2).getText(1));
 
 		final TableViewer viewer = ReflectionUtils.invokeHidden(getRidget(), "getTableViewer"); //$NON-NLS-1$
 		assertNotNull(viewer);
 		// table viewer reused?
 		bindRidgetToModel();
 		final TableViewer viewer2 = ReflectionUtils.invokeHidden(getRidget(), "getTableViewer"); //$NON-NLS-1$
 		assertSame(viewer, viewer2);
 		// new table viewer created?
 		final Table table = new Table(control.getParent(), SWT.MULTI);
 		getRidget().setUIControl(table);
 		bindRidgetToModel();
 		final TableViewer viewer3 = ReflectionUtils.invokeHidden(getRidget(), "getTableViewer"); //$NON-NLS-1$
 		assertNotSame(viewer, viewer3);
 		assertNotSame(viewer2, viewer3);
 		table.dispose();
 
 	}
 
 	public void testTableColumnsNumAndHeader() {
 		final Table control = getWidget();
 
 		final TableColumn[] columns = control.getColumns();
 		assertEquals(2, columns.length);
 		assertEquals("First Name", columns[0].getText()); //$NON-NLS-1$
 		assertEquals("Last Name", columns[1].getText()); //$NON-NLS-1$
 		assertTrue(control.getHeaderVisible());
 	}
 
 	public void testTableColumnsNumAndHeaderWithMismatch() {
 		final String[] properties1 = new String[] { "firstname", "lastname" }; //$NON-NLS-1$ //$NON-NLS-2$
 		final String[] headers1 = new String[] { "First Name" }; //$NON-NLS-1$
 
 		try {
 			getRidget().bindToModel(manager, "persons", Person.class, properties1, headers1); //$NON-NLS-1$
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 	}
 
 	public void testTableColumnsWithNullHeader() {
 		final ITableRidget ridget = getRidget();
 		final Table control = getWidget();
 
 		control.setHeaderVisible(true);
 		control.getColumn(0).setText("foo"); //$NON-NLS-1$
 		control.getColumn(1).setText("bar"); //$NON-NLS-1$
 
 		final String[] properties1 = new String[] { "firstname", "lastname" }; //$NON-NLS-1$ //$NON-NLS-2$
 		// null should hide the headers
 		ridget.bindToModel(manager, "persons", Person.class, properties1, null); //$NON-NLS-1$
 
 		assertFalse(control.getHeaderVisible());
 	}
 
 	public void testTableColumnsWithNullHeaderEntry() {
 		final ITableRidget ridget = getRidget();
 		final Table control = getWidget();
 
 		control.getColumn(0).setText("foo"); //$NON-NLS-1$
 		control.getColumn(1).setText("bar"); //$NON-NLS-1$
 
 		final String[] properties1 = new String[] { "firstname", "lastname" }; //$NON-NLS-1$ //$NON-NLS-2$
 		final String[] headers = new String[] { "First Name", null }; //$NON-NLS-1$
 		ridget.bindToModel(manager, "persons", Person.class, properties1, headers); //$NON-NLS-1$
 
 		assertEquals("First Name", control.getColumn(0).getText()); //$NON-NLS-1$
 		assertEquals("", control.getColumn(1).getText()); //$NON-NLS-1$
 	}
 
 	public void testUpdateFromModel() {
 		final ITableRidget ridget = getRidget();
 		final Table control = getWidget();
 		final List<ChangeEvent> changeEvents = new ArrayList<ChangeEvent>();
 		final IChangeListener listener = new IChangeListener() {
 			public void handleChange(final ChangeEvent event) {
 				changeEvents.add(event);
 			}
 		};
 		ridget.getObservableList().addChangeListener(listener);
 
 		final int oldCount = manager.getPersons().size();
 
 		assertEquals(oldCount, ridget.getObservableList().size());
 		assertEquals(oldCount, control.getItemCount());
 
 		manager.getPersons().remove(person1);
 
 		final int newCount = oldCount - 1;
 
 		assertEquals(newCount, manager.getPersons().size());
 		assertEquals(oldCount, ridget.getObservableList().size());
 		assertEquals(oldCount, control.getItemCount());
 		assertEquals(0, changeEvents.size());
 
 		ridget.updateFromModel();
 
 		assertEquals(newCount, manager.getPersons().size());
 		assertEquals(newCount, ridget.getObservableList().size());
 		assertEquals(newCount, control.getItemCount());
 		// TODO [ev] discuss with team
 		//		assertEquals(1, changeEvents.size());
 		assertEquals(0, changeEvents.size());
 	}
 
 	public void testUpdateFromModelPreservesSelection() {
 		final ITableRidget ridget = getRidget();
 
 		ridget.setSelection(person2);
 
 		assertSame(person2, ridget.getSelection().get(0));
 
 		manager.getPersons().remove(person1);
 		ridget.updateFromModel();
 
 		assertSame(person2, ridget.getSelection().get(0));
 	}
 
 	public void testUpdateFromModelRemovesSelection() {
 		final ITableRidget ridget = getRidget();
 
 		ridget.setSelection(person2);
 
 		assertSame(person2, ridget.getSelection().get(0));
 
 		manager.getPersons().remove(person2);
 		ridget.updateFromModel();
 
 		assertTrue(ridget.getSelection().isEmpty());
 	}
 
 	public void testContainsOption() {
 		final ITableRidget ridget = getRidget();
 
 		assertTrue(ridget.containsOption(person1));
 		assertTrue(ridget.containsOption(person2));
 		assertTrue(ridget.containsOption(person3));
 
 		assertFalse(ridget.containsOption(null));
 		assertFalse(ridget.containsOption(new Person("", ""))); //$NON-NLS-1$ //$NON-NLS-2$
 
 		final java.util.List<Person> persons = Arrays.asList(new Person[] { person3 });
 		final PersonManager manager = new PersonManager(persons);
 		ridget.bindToModel(manager, "persons", Person.class, new String[] { "firstname", "lastname" }, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		ridget.updateFromModel();
 
 		assertFalse(ridget.containsOption(person1));
 		assertTrue(ridget.containsOption(person3));
 	}
 
 	public void testSetSelectionType() {
 		final ITableRidget ridget = getRidget();
 		final Table control = getWidget();
 
 		assertEquals(SelectionType.SINGLE, ridget.getSelectionType());
 		assertTrue((control.getStyle() & SWT.MULTI) != 0);
 
 		ridget.setSelection(new int[] { 0, 1 });
 
 		// single selection is enforced
 		assertEquals(1, ridget.getSelectionIndices().length);
 		assertEquals(1, control.getSelectionCount());
 
 		// multiple selection is now allowed
 		ridget.setSelectionType(SelectionType.MULTI);
 		ridget.setSelection(new int[] { 0, 1 });
 
 		assertEquals(2, ridget.getSelectionIndices().length);
 		assertEquals(2, control.getSelectionCount());
 	}
 
 	@Override
 	public void testAddClickListener() {
 		final TableRidget ridget = getRidget();
 		ridget.updateFromModel();
 		final Table control = getWidget();
 		control.getColumn(0).setWidth(100);
 		control.getColumn(1).setWidth(100);
 
 		try {
 			ridget.addClickListener(null);
 			fail();
 		} catch (final RuntimeException npe) {
 			ok();
 		}
 
 		final FTClickListener listener1 = new FTClickListener();
 		ridget.addClickListener(listener1);
 
 		final FTClickListener listener2 = new FTClickListener();
 		ridget.addClickListener(listener2);
 		ridget.addClickListener(listener2);
 
 		final Event mdEvent = new Event();
 		mdEvent.widget = control;
 		mdEvent.type = SWT.MouseDown;
 		mdEvent.button = 2;
 		final TableItem row2 = control.getItem(2);
 		mdEvent.item = row2;
 		mdEvent.x = row2.getBounds().x + 1;
 		mdEvent.y = row2.getBounds().y + 1;
 		control.notifyListeners(SWT.MouseDown, mdEvent);
 
 		assertEquals(0, listener1.getCount());
 		assertEquals(0, listener2.getCount());
 
 		mdEvent.type = SWT.MouseUp;
 		control.notifyListeners(SWT.MouseUp, mdEvent);
 
 		assertEquals(1, listener1.getCount());
 		assertEquals(1, listener2.getCount());
 
 		final ClickEvent event = listener2.getEvent();
 		assertEquals(ridget, event.getSource());
 		assertEquals(2, event.getButton());
 		assertEquals(0, event.getColumnIndex());
 		assertEquals(row2.getData(), event.getRow());
 
 		ridget.removeClickListener(listener1);
 
 		mdEvent.type = SWT.MouseDown;
 		control.notifyListeners(SWT.MouseDown, mdEvent);
 		mdEvent.type = SWT.MouseUp;
 		control.notifyListeners(SWT.MouseUp, mdEvent);
 
 		assertEquals(1, listener1.getCount());
 	}
 
 	public void testAddDoubleClickListener() {
 		final TableRidget ridget = getRidget();
 		final Table control = getWidget();
 
 		try {
 			ridget.addDoubleClickListener(null);
 			fail();
 		} catch (final RuntimeException npe) {
 			ok();
 		}
 
 		final FTActionListener listener1 = new FTActionListener();
 		ridget.addDoubleClickListener(listener1);
 
 		final FTActionListener listener2 = new FTActionListener();
 		ridget.addDoubleClickListener(listener2);
 		ridget.addDoubleClickListener(listener2);
 
 		final Event doubleClick = new Event();
 		doubleClick.widget = control;
 		doubleClick.type = SWT.MouseDoubleClick;
 		control.notifyListeners(SWT.MouseDoubleClick, doubleClick);
 
 		assertEquals(1, listener1.getCount());
 		assertEquals(1, listener2.getCount());
 
 		ridget.removeDoubleClickListener(listener1);
 
 		control.notifyListeners(SWT.MouseDoubleClick, doubleClick);
 
 		assertEquals(1, listener1.getCount());
 	}
 
 	public void testSetComparator() {
 		final TableRidget ridget = getRidget();
 		final Table control = getWidget();
 
 		// sorts from a to z
 		final Comparator<Object> comparator = new TypedComparator<String>();
 
 		try {
 			ridget.setComparator(-1, comparator);
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 
 		try {
 			ridget.setComparator(2, comparator);
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 
 		ridget.setSortedAscending(true);
 
 		final int lastItemIndex = control.getItemCount() - 1;
 
 		assertEquals("John", control.getItem(0).getText(0)); //$NON-NLS-1$
 		assertEquals("Frank", control.getItem(lastItemIndex).getText(0)); //$NON-NLS-1$
 
 		ridget.setComparator(0, comparator);
 
 		assertEquals("John", control.getItem(0).getText(0)); //$NON-NLS-1$
 		assertEquals("Frank", control.getItem(lastItemIndex).getText(0)); //$NON-NLS-1$
 
 		ridget.setSortedColumn(0);
 
 		assertEquals("Frank", control.getItem(0).getText(0)); //$NON-NLS-1$
 		assertEquals("John", control.getItem(lastItemIndex).getText(0)); //$NON-NLS-1$
 
 		ridget.setComparator(0, null);
 
 		assertEquals("John", control.getItem(0).getText(0)); //$NON-NLS-1$
 		assertEquals("Frank", control.getItem(lastItemIndex).getText(0)); //$NON-NLS-1$
 
 		ridget.setComparator(1, comparator);
 		ridget.setSortedColumn(1);
 
 		assertEquals("Doe", control.getItem(0).getText(1)); //$NON-NLS-1$
 		assertEquals("Zappa", control.getItem(lastItemIndex).getText(1)); //$NON-NLS-1$
 
 		ridget.setSortedAscending(false);
 
 		assertEquals("Zappa", control.getItem(0).getText(1)); //$NON-NLS-1$
 		assertEquals("Doe", control.getItem(lastItemIndex).getText(1)); //$NON-NLS-1$
 
 	}
 
 	public void testGetSortedColumn() {
 		final TableRidget ridget = getRidget();
 
 		try {
 			ridget.setSortedColumn(2);
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 
 		assertEquals(-1, ridget.getSortedColumn());
 
 		ridget.setComparator(0, new TypedComparator<String>());
 
 		assertEquals(-1, ridget.getSortedColumn());
 
 		ridget.setSortedColumn(0);
 
 		assertEquals(0, ridget.getSortedColumn());
 
 		ridget.setComparator(0, null);
 
 		assertEquals(-1, ridget.getSortedColumn());
 
 		ridget.setComparator(1, new TypedComparator<String>());
 		ridget.setSortedColumn(1);
 
 		assertEquals(1, ridget.getSortedColumn());
 
 		ridget.setSortedColumn(-1);
 
 		assertEquals(-1, ridget.getSortedColumn());
 
 		// no comparator in column 0
 		ridget.setSortedColumn(0);
 
 		assertEquals(-1, ridget.getSortedColumn());
 	}
 
 	public void testIsColumnSortable() {
 		final TableRidget ridget = getRidget();
 
 		try {
 			assertFalse(ridget.isColumnSortable(-1));
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 
 		try {
 			assertFalse(ridget.isColumnSortable(2));
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 
 		for (int i = 0; i < 2; i++) {
 			assertFalse(ridget.isColumnSortable(i));
 
 			// columns are sortable by default, when they have a comparator
 			ridget.setComparator(i, new TypedComparator<String>());
 
 			assertTrue(ridget.isColumnSortable(i));
 
 			ridget.setColumnSortable(i, false);
 
 			assertFalse(ridget.isColumnSortable(i));
 
 			ridget.setColumnSortable(i, true);
 
 			assertTrue(ridget.isColumnSortable(i));
 
 			// columns are not sortable without a comparator
 			ridget.setComparator(i, null);
 
 			assertFalse(ridget.isColumnSortable(i));
 		}
 	}
 
 	public void testSetSortedAscending() {
 		final Table control = getWidget();
 		final TableRidget ridget = getRidget();
 
 		ridget.bindToModel(manager, "persons", Person.class, new String[] { "lastname", "firstname" }, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		ridget.updateFromModel();
 		final int lastItemIndex = control.getItemCount() - 1;
 
 		assertEquals(-1, ridget.getSortedColumn());
 		assertFalse(ridget.isSortedAscending());
 
 		ridget.setComparator(0, new TypedComparator<String>());
 		ridget.setSortedColumn(0);
 
 		assertTrue(ridget.isSortedAscending());
 		assertEquals("Doe", control.getItem(0).getText(0)); //$NON-NLS-1$
 		assertEquals("Zappa", control.getItem(lastItemIndex).getText(0)); //$NON-NLS-1$
 
 		ridget.setSortedAscending(false);
 
 		assertFalse(ridget.isSortedAscending());
 		assertEquals("Zappa", control.getItem(0).getText(0)); //$NON-NLS-1$
 		assertEquals("Doe", control.getItem(lastItemIndex).getText(0)); //$NON-NLS-1$
 
 		ridget.setSortedAscending(true);
 
 		assertTrue(ridget.isSortedAscending());
 		assertEquals("Doe", control.getItem(0).getText(0)); //$NON-NLS-1$
 		assertEquals("Zappa", control.getItem(lastItemIndex).getText(0)); //$NON-NLS-1$
 
 		ridget.setComparator(0, null);
 
 		assertEquals(-1, ridget.getSortedColumn());
 		assertFalse(ridget.isSortedAscending());
 	}
 
 	public void testSetSortedAscendingFiresEvents() {
 		final TableRidget ridget = getRidget();
 
 		ridget.setSortedAscending(true);
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ISortableByColumn.PROPERTY_SORT_ASCENDING,
 				Boolean.TRUE, Boolean.FALSE));
 
 		ridget.setSortedAscending(false);
 
 		verifyPropertyChangeEvents();
 		expectNoPropertyChangeEvent();
 
 		ridget.setSortedAscending(false);
 
 		verifyPropertyChangeEvents();
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ISortableByColumn.PROPERTY_SORT_ASCENDING,
 				Boolean.FALSE, Boolean.TRUE));
 
 		ridget.setSortedAscending(true);
 
 		verifyPropertyChangeEvents();
 		expectNoPropertyChangeEvent();
 
 		ridget.setSortedAscending(true);
 
 		verifyPropertyChangeEvents();
 	}
 
 	public void testSetSortedColumnFiresEvents() {
 		final TableRidget ridget = getRidget();
 
 		assertEquals(-1, ridget.getSortedColumn());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ISortableByColumn.PROPERTY_SORTED_COLUMN,
 				Integer.valueOf(-1), Integer.valueOf(0)));
 
 		ridget.setSortedColumn(0);
 
 		verifyPropertyChangeEvents();
 		expectNoPropertyChangeEvent();
 
 		ridget.setSortedColumn(0);
 
 		verifyPropertyChangeEvents();
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ISortableByColumn.PROPERTY_SORTED_COLUMN,
 				Integer.valueOf(0), Integer.valueOf(1)));
 
 		ridget.setSortedColumn(1);
 
 		verifyPropertyChangeEvents();
 	}
 
 	public void testSetColumnSortabilityFiresEvents() {
 		final TableRidget ridget = getRidget();
 
 		expectNoPropertyChangeEvent();
 
 		ridget.setColumnSortable(0, true);
 
 		verifyPropertyChangeEvents();
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ISortableByColumn.PROPERTY_COLUMN_SORTABILITY, null,
 				Integer.valueOf(0)));
 
 		ridget.setColumnSortable(0, false);
 
 		verifyPropertyChangeEvents();
 		expectNoPropertyChangeEvent();
 
 		ridget.setColumnSortable(0, false);
 
 		verifyPropertyChangeEvents();
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ISortableByColumn.PROPERTY_COLUMN_SORTABILITY, null,
 				Integer.valueOf(0)));
 
 		ridget.setColumnSortable(0, true);
 
 		verifyPropertyChangeEvents();
 		expectNoPropertyChangeEvent();
 
 		ridget.setColumnSortable(0, true);
 
 		verifyPropertyChangeEvents();
 	}
 
 	public void testColumnHeaderChangesSortability() {
 		final TableRidget ridget = getRidget();
 		final Table table = getWidget();
 
 		ridget.setColumnSortable(0, true);
 		ridget.setComparator(0, new TypedComparator<String>());
 		ridget.setColumnSortable(1, true);
 		ridget.setComparator(1, new TypedComparator<String>());
 
 		ridget.setSortedColumn(-1);
 
 		assertEquals(-1, ridget.getSortedColumn());
 		assertFalse(ridget.isSortedAscending());
 
 		final Event e = new Event();
 		e.type = SWT.Selection;
 		e.widget = table.getColumn(0);
 		e.widget.notifyListeners(SWT.Selection, e);
 
 		assertEquals(0, ridget.getSortedColumn());
 		assertTrue(ridget.isSortedAscending());
 
 		e.widget.notifyListeners(SWT.Selection, e);
 
 		assertEquals(0, ridget.getSortedColumn());
 		assertFalse(ridget.isSortedAscending());
 
 		e.widget.notifyListeners(SWT.Selection, e);
 
 		assertEquals(-1, ridget.getSortedColumn());
 		assertFalse(ridget.isSortedAscending());
 
 		e.widget.notifyListeners(SWT.Selection, e);
 
 		assertEquals(0, ridget.getSortedColumn());
 		assertTrue(ridget.isSortedAscending());
 	}
 
 	public void testSetMoveableColumns() {
 		final TableRidget ridget = getRidget();
 		final Table table = getWidget();
 
 		assertFalse(ridget.hasMoveableColumns());
 		assertFalse(table.getColumn(0).getMoveable());
 		assertFalse(table.getColumn(1).getMoveable());
 
 		ridget.setMoveableColumns(true);
 
 		assertTrue(ridget.hasMoveableColumns());
 		assertTrue(table.getColumn(0).getMoveable());
 		assertTrue(table.getColumn(1).getMoveable());
 
 		ridget.setMoveableColumns(false);
 
 		assertFalse(ridget.hasMoveableColumns());
 		assertFalse(table.getColumn(0).getMoveable());
 		assertFalse(table.getColumn(1).getMoveable());
 	}
 
 	/**
 	 * Tests that for single selection, the ridget selection state and the ui
 	 * selection state cannot be changed by the user when ridget is set to
 	 * "output only".
 	 */
 	public void testOutputSingleSelectionCannotBeChangedFromUI() {
 		final TableRidget ridget = getRidget();
 		final Table control = getWidget();
 
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
 	 * Tests that for multiple selection, the ridget selection state and the ui
 	 * selection state cannot be changed by the user when ridget is set to
 	 * "output only".
 	 */
 	public void testOutputMultipleSelectionCannotBeChangedFromUI() {
 		final TableRidget ridget = getRidget();
 		final Table control = getWidget();
 
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
 		final TableRidget ridget = getRidget();
 
 		ridget.setSelection(0);
 
 		assertEquals(0, ridget.getSelectionIndex());
 
 		ridget.setOutputOnly(true);
 
 		assertEquals(0, ridget.getSelectionIndex());
 
 		ridget.setSelection((Object) null);
 
 		assertEquals(-1, ridget.getSelectionIndex());
 
 		ridget.setOutputOnly(false);
 
 		assertEquals(-1, ridget.getSelectionIndex());
 	}
 
 	public void testSetColumnFormatter() {
 		final TableRidget ridget = getRidget();
 		final Table table = getWidget();
 		final IColumnFormatter formatter = new ColumnFormatter() {
 			@Override
 			public String getText(final Object element) {
 				final Person person = (Person) element;
 				return person.getLastname().toUpperCase();
 			}
 		};
 		final String lastName = person1.getLastname();
 		final String lastNameUpperCase = lastName.toUpperCase();
 
 		try {
 			ridget.setColumnFormatter(-1, formatter);
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 
 		try {
 			ridget.setColumnFormatter(99, formatter);
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 
 		ridget.setColumnFormatter(1, formatter);
 
 		assertEquals(lastName, table.getItem(0).getText(1));
 
 		ridget.updateFromModel();
 
 		assertEquals(lastNameUpperCase, table.getItem(0).getText(1));
 
 		ridget.setColumnFormatter(1, null);
 
 		assertEquals(lastNameUpperCase, table.getItem(0).getText(1));
 
 		ridget.updateFromModel();
 
 		assertEquals(lastName, table.getItem(0).getText(1));
 	}
 
 	/**
 	 * Tests the method {@code clearColumnFormatters()}.
 	 */
 	public void testClearColumnFormatters() {
 
 		final TableRidget ridget = getRidget();
 		final IColumnFormatter formatter = new ColumnFormatter() {
 			@Override
 			public String getText(final Object element) {
 				return "dummy"; //$NON-NLS-1$
 			}
 		};
 
 		ridget.setColumnFormatter(1, formatter);
 
 		IColumnFormatter[] retFormatters = ReflectionUtils.invokeHidden(ridget,
 				"getColumnFormatters", ridget.getColumnCount()); //$NON-NLS-1$
 		assertNotNull(retFormatters);
 		assertEquals(ridget.getColumnCount(), retFormatters.length);
 		assertNull(retFormatters[0]);
 		assertNotNull(retFormatters[1]);
 		assertSame(formatter, retFormatters[1]);
 
 		ridget.clearColumnFormatters();
 		retFormatters = ReflectionUtils.invokeHidden(ridget, "getColumnFormatters", ridget.getColumnCount()); //$NON-NLS-1$
 		assertNotNull(retFormatters);
 		assertEquals(ridget.getColumnCount(), retFormatters.length);
 		assertNull(retFormatters[0]);
 		assertNull(retFormatters[1]);
 
 	}
 
 	public void testAddSelectionListener() {
 		final TableRidget ridget = getRidget();
 		final Table control = getWidget();
 
 		try {
 			ridget.addSelectionListener(null);
 			fail();
 		} catch (final RuntimeException npe) {
 			ok();
 		}
 
 		final TestSelectionListener selectionListener = new TestSelectionListener();
 		ridget.addSelectionListener(selectionListener);
 
 		ridget.setSelection(person1);
 		assertEquals(1, selectionListener.getCount());
 		ridget.removeSelectionListener(selectionListener);
 		ridget.setSelection(person2);
 		assertEquals(1, selectionListener.getCount());
 		ridget.clearSelection();
 
 		ridget.addSelectionListener(selectionListener);
 		ridget.setSelectionType(SelectionType.SINGLE);
 		assertEquals(0, ridget.getSelection().size());
 		assertEquals(0, control.getSelectionCount());
 
 		control.setFocus();
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
 
 		assertEquals(1, ridget.getSelection().size());
 		assertEquals(1, control.getSelectionCount());
 		assertEquals(2, selectionListener.getCount());
 		final SelectionEvent selectionEvent = selectionListener.getSelectionEvent();
 		assertEquals(ridget, selectionEvent.getSource());
 		assertTrue(selectionEvent.getOldSelection().isEmpty());
 		assertEquals(ridget.getSelection(), selectionEvent.getNewSelection());
 		// System.out.println("SelectionEvent: " + selectionListener.getSelectionEvent());
 
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
 
 		assertEquals(1, ridget.getSelection().size());
 		assertEquals(1, control.getSelectionCount());
 		assertEquals(3, selectionListener.getCount());
 		final SelectionEvent selectionEvent2 = selectionListener.getSelectionEvent();
 		assertEquals(ridget, selectionEvent.getSource());
 		assertEquals(selectionEvent.getNewSelection(), selectionEvent2.getOldSelection());
 		assertEquals(ridget.getSelection(), selectionEvent2.getNewSelection());
 		// System.out.println("SelectionEvent: " + selectionListener.getSelectionEvent());
 
 		ridget.removeSelectionListener(selectionListener);
 	}
 
 	/**
 	 * As per Bug 295305
 	 */
 	public void testAutoCreateTableColumns() {
 		final ITableRidget ridget = createRidget();
 		final Table control = new Table(getShell(), SWT.FULL_SELECTION | SWT.SINGLE);
 		ridget.setUIControl(control);
 
 		assertEquals(0, control.getColumnCount());
 
 		final String[] columns3 = { Person.PROPERTY_FIRSTNAME, Person.PROPERTY_LASTNAME, Person.PROPERTY_BIRTHDAY };
 		ridget.bindToModel(manager, "persons", Person.class, columns3, null); //$NON-NLS-1$
 
 		assertEquals(3, control.getColumnCount());
 
 		final String[] columns1 = { Person.PROPERTY_FIRSTNAME };
 		ridget.bindToModel(manager, "persons", Person.class, columns1, null); //$NON-NLS-1$
 
 		assertEquals(1, control.getColumnCount());
 	}
 
 	/**
 	 * As per Bug 295305
 	 */
 	public void testAutoCreateColumnsWithNoLayout() {
 		final ITableRidget ridget = createRidget();
 		final Table control = new Table(getShell(), SWT.FULL_SELECTION | SWT.SINGLE);
 		ridget.setUIControl(control);
 
 		getShell().setLayout(null);
 		control.setSize(300, 100);
 		final String[] columns3 = { Person.PROPERTY_FIRSTNAME, Person.PROPERTY_LASTNAME, Person.PROPERTY_BIRTHDAY };
 		ridget.bindToModel(manager, "persons", Person.class, columns3, null); //$NON-NLS-1$
 
 		assertEquals(null, control.getParent().getLayout());
 		assertEquals(null, control.getLayout());
 
 		TestUtils.assertColumnWidths(control, 3);
 	}
 
 	/**
 	 * As per Bug 295305
 	 */
 	public void testAutoCreateColumnsWithTableLayout() {
 		final ITableRidget ridget = createRidget();
 		final Table control = new Table(getShell(), SWT.FULL_SELECTION | SWT.SINGLE);
 		control.setLayout(new TableLayout());
 		ridget.setUIControl(control);
 
 		final String[] columns3 = { Person.PROPERTY_FIRSTNAME, Person.PROPERTY_LASTNAME, Person.PROPERTY_BIRTHDAY };
 		ridget.bindToModel(manager, "persons", Person.class, columns3, null); //$NON-NLS-1$
 
 		final Class<?> shellLayout = getShell().getLayout().getClass();
 		assertSame(shellLayout, control.getParent().getLayout().getClass());
 		assertTrue(control.getLayout() instanceof TableLayout);
 
 		//		int totalWidthOfColumns = 0;
 		//		for (int i = 0; i < 3; i++) {
 		//			totalWidthOfColumns += control.getColumn(i).getWidth();
 		//		}
 		TestUtils.assertColumnWidths(control, 3);
 	}
 
 	/**
 	 * As per Bug 295305
 	 */
 	public void testAutoCreateColumnsWithTableColumnLayout() {
 		final ITableRidget ridget = createRidget();
 		for (final Control control : getShell().getChildren()) {
 			control.dispose();
 		}
 		final Table control = new Table(getShell(), SWT.FULL_SELECTION | SWT.SINGLE);
 		ridget.setUIControl(control);
 		getShell().setLayout(new TableColumnLayout());
 
 		final String[] columns3 = { Person.PROPERTY_FIRSTNAME, Person.PROPERTY_LASTNAME, Person.PROPERTY_BIRTHDAY };
 		ridget.bindToModel(manager, "persons", Person.class, columns3, null); //$NON-NLS-1$
 
 		assertTrue(control.getParent().getLayout() instanceof TableColumnLayout);
 		assertEquals(null, control.getLayout());
 
 		TestUtils.assertColumnWidths(control, 3);
 	}
 
 	/**
 	 * As per Bug 295305
 	 */
 	public void testSetColumnWidths() {
 		final ITableRidget ridget = createRidget();
 		final Table control = new Table(getShell(), SWT.FULL_SELECTION | SWT.SINGLE);
 		ridget.setUIControl(control);
 
 		try {
 			ridget.setColumnWidths(new Object[] { null });
 			fail();
 		} catch (final RuntimeException rex) {
 			assertTrue(rex.getMessage().contains("null")); //$NON-NLS-1$
 		}
 
 		try {
 			ridget.setColumnWidths(new Object[] { new Object() });
 			fail();
 		} catch (final RuntimeException rex) {
 			assertTrue(rex.getMessage().contains("Object")); //$NON-NLS-1$
 		}
 
 		ridget.setColumnWidths(new Object[] { new ColumnPixelData(20), new ColumnPixelData(40), new ColumnPixelData(60) });
 		final String[] columns3 = { Person.PROPERTY_FIRSTNAME, Person.PROPERTY_LASTNAME, Person.PROPERTY_BIRTHDAY };
 		ridget.bindToModel(manager, "persons", Person.class, columns3, null); //$NON-NLS-1$
 
 		final int[] expected = { 20, 40, 60 };
 		for (int i = 0; i < 3; i++) {
 			final int actual = control.getColumn(i).getWidth();
 			final String msg = String.format("col #%d, exp:%d, act:%d", i, expected[i], actual); //$NON-NLS-1$
 			assertEquals(msg, expected[i], actual);
 		}
 	}
 
 	/**
 	 * As per Bug 295305
 	 */
 	public void testPreserveColumnWidths() {
 		final int[] widths = { 50, 100, 150 };
 		final ITableRidget ridget = createRidget();
 		final Table control = new Table(getShell(), SWT.FULL_SELECTION | SWT.SINGLE);
 		for (final int width : widths) {
 			final TableColumn column = new TableColumn(control, SWT.NONE);
 			column.setWidth(width);
 		}
 		ridget.setUIControl(control);
 
 		final String[] columns3 = { Person.PROPERTY_FIRSTNAME, Person.PROPERTY_LASTNAME, Person.PROPERTY_BIRTHDAY };
 		ridget.bindToModel(manager, "persons", Person.class, columns3, null); //$NON-NLS-1$
 		ridget.updateFromModel();
 
 		for (int i = 0; i < 3; i++) {
 			final int actual = control.getColumn(i).getWidth();
 			final String msg = String.format("col #%d, exp:%d, act:%d", i, widths[i], actual); //$NON-NLS-1$
 			assertEquals(msg, widths[i], actual);
 		}
 	}
 
 	/**
 	 * As per bug 301182
 	 */
 	public void testRefreshNull() {
 		final ITableRidget ridget = createRidget();
 		final Table control = createWidget(getShell());
 		ridget.setUIControl(control);
 
 		final StringPojo word1 = new StringPojo("eclipse"); //$NON-NLS-1$
 		final StringPojo word2 = new StringPojo("riena"); //$NON-NLS-1$
 		final WritableList values = new WritableList(Arrays.asList(word1, word2), StringPojo.class);
 		final String[] columns = { "value" }; //$NON-NLS-1$
 		ridget.bindToModel(values, StringPojo.class, columns, null);
 		ridget.updateFromModel();
 
 		assertEquals("eclipse", control.getItem(0).getText()); //$NON-NLS-1$
 		assertEquals("riena", control.getItem(1).getText()); //$NON-NLS-1$
 
 		word1.setValue("alpha"); //$NON-NLS-1$
 		word2.setValue("beta"); //$NON-NLS-1$
 
 		assertEquals("eclipse", control.getItem(0).getText()); //$NON-NLS-1$
 		assertEquals("riena", control.getItem(1).getText()); //$NON-NLS-1$
 
 		ridget.refresh(null);
 
 		assertEquals("alpha", control.getItem(0).getText()); //$NON-NLS-1$
 		assertEquals("beta", control.getItem(1).getText()); //$NON-NLS-1$
 	}
 
 	/**
 	 * As per bug 301182
 	 */
 	public void testRefresh() {
 		final ITableRidget ridget = createRidget();
 		final Table control = createWidget(getShell());
 		ridget.setUIControl(control);
 
 		final StringPojo word1 = new StringPojo("eclipse"); //$NON-NLS-1$
 		final StringPojo word2 = new StringPojo("riena"); //$NON-NLS-1$
 		final WritableList values = new WritableList(Arrays.asList(word1, word2), StringPojo.class);
 		final String[] columns = { "value" }; //$NON-NLS-1$
 		ridget.bindToModel(values, StringPojo.class, columns, null);
 		ridget.updateFromModel();
 
 		assertEquals("eclipse", control.getItem(0).getText()); //$NON-NLS-1$
 		assertEquals("riena", control.getItem(1).getText()); //$NON-NLS-1$
 
 		word1.setValue("alpha"); //$NON-NLS-1$
 		word2.setValue("beta"); //$NON-NLS-1$
 
 		assertEquals("eclipse", control.getItem(0).getText()); //$NON-NLS-1$
 		assertEquals("riena", control.getItem(1).getText()); //$NON-NLS-1$
 
 		ridget.refresh(word1);
 
 		assertEquals("alpha", control.getItem(0).getText()); //$NON-NLS-1$
 		assertEquals("riena", control.getItem(1).getText()); //$NON-NLS-1$
 	}
 
 	public void testRowErrorMessageMarker() {
 		final ITableRidget ridget = getRidget();
 		final RowErrorMessageMarker marker1 = new RowErrorMessageMarker(null, person1);
 		final RowErrorMessageMarker marker2 = new RowErrorMessageMarker(null, person2);
 
 		assertFalse(ridget.isErrorMarked());
 
 		ridget.addMarker(marker1);
 		ridget.addMarker(marker1);
 
 		assertTrue(ridget.isErrorMarked());
 
 		ridget.removeMarker(marker1);
 
 		assertFalse(ridget.isErrorMarked());
 
 		ridget.addMarker(marker1);
 		ridget.addMarker(marker2);
 		ridget.removeMarker(marker1);
 
 		assertTrue(ridget.isErrorMarked());
 
 		ridget.removeMarker(marker2);
 
 		assertFalse(ridget.isErrorMarked());
 	}
 
 	public void testGetOptionWithSorting() {
 		final ITableRidget ridget = getRidget();
 
 		assertEquals(0, ridget.indexOfOption(person1));
 		assertEquals(person1, ridget.getOption(0));
 
 		ridget.setComparator(1, new StringComparator());
 		ridget.setSortedColumn(1); // sort by last name
 		ridget.setSortedAscending(false);
 
 		final int last = ridget.getOptionCount() - 1;
 		assertEquals(last, ridget.indexOfOption(person1));
 		assertEquals(person1, ridget.getOption(last));
 
 		ridget.setSortedAscending(true);
 
 		assertEquals(0, ridget.indexOfOption(person1));
 		assertEquals(person1, ridget.getOption(0));
 	}
 
 	public void testSetSelectionWithSorting() {
 		final ITableRidget ridget = getRidget();
 
 		assertEquals(-1, ridget.getSelectionIndex());
 		assertTrue(ridget.getSelection().isEmpty());
 
 		ridget.setSelection(0);
 
 		assertEquals(0, ridget.getSelectionIndex());
 		assertEquals(person1, ridget.getSelection().get(0));
 
 		ridget.setComparator(1, new StringComparator());
 		ridget.setSortedColumn(1); // sort by last name
 		ridget.setSortedAscending(false);
 
 		final int last = ridget.getOptionCount() - 1;
 		assertEquals(last, ridget.getSelectionIndex());
 		assertEquals(person1, ridget.getSelection().get(0));
 
 		ridget.setSortedAscending(true);
 
 		assertEquals(0, ridget.getSelectionIndex());
 		assertEquals(person1, ridget.getSelection().get(0));
 	}
 
 	// helping methods
 	// ////////////////
 
 	@Override
 	protected void clearUIControlRowSelection() {
 		getWidget().deselectAll();
 		fireSelectionEvent();
 	}
 
 	@Override
 	protected int getUIControlSelectedRowCount() {
 		return getWidget().getSelectionCount();
 	}
 
 	@Override
 	protected int getUIControlSelectedRow() {
 		return getWidget().getSelectionIndex();
 	}
 
 	@Override
 	protected Object getRowValue(final int i) {
 		// return getRidget().getRowObservables().get(i);
 		final IObservableList rowObservables = ReflectionUtils.invokeHidden(getRidget(), "getRowObservables"); //$NON-NLS-1$
 		return rowObservables.get(i);
 	}
 
 	@Override
 	protected int[] getSelectedRows() {
 		// IObservableList rowObservables = getRidget().getRowObservables();
 		final IObservableList rowObservables = ReflectionUtils.invokeHidden(getRidget(), "getRowObservables"); //$NON-NLS-1$
 		final Object[] elements = getRidget().getMultiSelectionObservable().toArray();
 		final int[] result = new int[elements.length];
 		for (int i = 0; i < elements.length; i++) {
 			final Object element = elements[i];
 			result[i] = rowObservables.indexOf(element);
 		}
 		return result;
 	}
 
 	@Override
 	protected int[] getUIControlSelectedRows() {
 		return getWidget().getSelectionIndices();
 	}
 
 	@Override
 	protected void setUIControlRowSelection(final int[] indices) {
 		getWidget().setSelection(indices);
 		fireSelectionEvent();
 	}
 
 	@Override
 	protected void setUIControlRowSelectionInterval(final int start, final int end) {
 		getWidget().setSelection(start, end);
 		fireSelectionEvent();
 	}
 
 	@Override
 	protected boolean supportsMulti() {
 		return true;
 	}
 
 	// helping classes
 	//////////////////
 
 	private static final class StringComparator implements Comparator<Object> {
 		public int compare(final Object o1, final Object o2) {
 			final String s1 = (String) o1;
 			final String s2 = (String) o2;
 			return s1.compareTo(s2);
 		}
 	}
 
 }
