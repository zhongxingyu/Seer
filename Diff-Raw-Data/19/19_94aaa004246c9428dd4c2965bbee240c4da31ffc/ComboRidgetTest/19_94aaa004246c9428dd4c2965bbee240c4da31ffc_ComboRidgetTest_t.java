 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
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
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.riena.beans.common.Person;
 import org.eclipse.riena.beans.common.PersonManager;
 import org.eclipse.riena.beans.common.StringManager;
 import org.eclipse.riena.tests.UITestHelper;
 import org.eclipse.riena.ui.ridgets.IComboRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.DefaultSwtControlRidgetMapper;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 /**
  * Tests of the class {@link ComboRidget}.
  */
 public class ComboRidgetTest extends AbstractSWTRidgetTest {
 
 	private PersonManager manager;
 	private Person selection1;
 	private Person selection2;
 	private Person selection3;
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
 		manager = new PersonManager(createPersonList());
 		Iterator<Person> it = manager.getPersons().iterator();
 		selection1 = it.next();
 		selection2 = it.next();
 		selection3 = it.next();
 	}
 
 	@Override
 	protected Control createWidget(Composite parent) {
 		return new Combo(parent, SWT.READ_ONLY);
 	}
 
 	@Override
 	protected IRidget createRidget() {
 		return new ComboRidget();
 	}
 
 	@Override
 	protected Combo getWidget() {
 		return (Combo) super.getWidget();
 	}
 
 	@Override
 	protected ComboRidget getRidget() {
 		return (ComboRidget) super.getRidget();
 	}
 
 	public void testRidgetMapping() {
 		DefaultSwtControlRidgetMapper mapper = new DefaultSwtControlRidgetMapper();
 		assertSame(ComboRidget.class, mapper.getRidgetClass(getWidget()));
 	}
 
 	public void testSetUIControl() {
 		ComboRidget ridget = getRidget();
 
 		ridget.setUIControl(null);
 		assertNull(ridget.getUIControl());
 
 		ridget.setUIControl(getWidget());
 		assertSame(getWidget(), ridget.getUIControl());
 	}
 
 	public void testSetUIControlInvalid() {
 		ComboRidget ridget = getRidget();
 
 		try {
 			ridget.setUIControl(getShell());
 			fail();
 		} catch (BindingException bex) {
 			ok();
 		}
 
 		try {
 			ridget.setUIControl(new Combo(getShell(), SWT.NONE));
 			fail();
 		} catch (BindingException bex) {
 			ok();
 		}
 	}
 
 	public void testGetEmptySelectionItem() {
 		ComboRidget ridget = getRidget();
 		Combo control = getWidget();
 		StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		assertNull(ridget.getEmptySelectionItem());
 
 		Object emptySelectionItem = "A";
 		ridget.setEmptySelectionItem(emptySelectionItem);
 
 		assertSame(emptySelectionItem, ridget.getEmptySelectionItem());
 
 		ridget.setSelection("A");
 
 		int controlSelectedItem = control.getSelectionIndex();
 		assertEquals(-1, ridget.getSelectionIndex());
 		assertEquals(0, controlSelectedItem);
 
 		assertEquals(null, ridget.getSelection());
 		assertEquals("A", control.getItem(controlSelectedItem));
 
 	}
 
 	public void testBindToModelWithDomainObjects() {
 		manager.setSelectedPerson(selection1);
 
 		ComboRidget ridget = getRidget();
 		Combo control = getWidget();
 
 		assertEquals(null, getSelectedString(control));
 		assertEquals(0, control.getItemCount());
 		ridget.bindToModel(manager, "persons", String.class, null, manager, "selectedPerson");
 		assertEquals(null, getSelectedString(control));
 		assertEquals(0, control.getItemCount());
 
 		ridget.updateFromModel();
 
 		assertEquals(selection1.toString(), getSelectedString(control));
 		assertEquals(manager.getPersons().size(), control.getItemCount());
 
 		for (int i = 0; i < control.getItemCount(); i++) {
 			String item = control.getItem(i);
 			if (!find(manager, item)) {
 				fail();
 			}
 		}
 
 		manager.setSelectedPerson(selection2);
 
 		assertEquals(selection1.toString(), getSelectedString(control));
 
 		ridget.updateFromModel();
 
 		assertEquals(selection2.toString(), getSelectedString(control));
 
 		control.select(2);
 
 		assertEquals(selection3.toString(), getSelectedString(control));
 	}
 
 	public void testBindToModelWithDomainObjectsUsingColumnPropertyName() {
 		manager.setSelectedPerson(selection1);
 
 		ComboRidget ridget = getRidget();
 		Combo control = getWidget();
 
 		assertEquals(null, getSelectedString(control));
 		assertEquals(0, control.getItemCount());
 		ridget.bindToModel(manager, "persons", String.class, "getListEntry", manager, "selectedPerson");
 		assertEquals(null, getSelectedString(control));
 		assertEquals(0, control.getItemCount());
 
 		ridget.updateFromModel();
 
 		assertEquals(selection1.getListEntry(), getSelectedString(control));
 		assertEquals(selection1.getListEntry(), control.getText());
 		checkPersonList(manager);
 		assertEquals(selection1, manager.getSelectedPerson());
 
 		manager.setSelectedPerson(selection2);
 
 		assertEquals(selection1.getListEntry(), getSelectedString(control));
 		assertEquals(selection1.getListEntry(), control.getText());
 		assertEquals(selection2, manager.getSelectedPerson());
 
 		ridget.updateFromModel();
 
 		assertEquals(selection2, manager.getSelectedPerson());
 		assertEquals(selection2.getListEntry(), getSelectedString(control));
 		assertEquals(selection2.getListEntry(), control.getText());
 
 		control.select(2);
 
 		assertEquals(selection3.getListEntry(), getSelectedString(control));
 		assertEquals(selection3.getListEntry(), control.getText());
 		assertEquals(selection3, manager.getSelectedPerson());
 	}
 
 	public void testBindToModelWithStrings() {
 		final StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		Iterator<String> it = aManager.getItems().iterator();
 		final String aSelection1 = it.next();
 		final String aSelection2 = it.next();
 		final String aSelection3 = it.next();
 		aManager.setSelectedItem(aSelection1);
 
 		ComboRidget ridget = getRidget();
 		Combo control = getWidget();
 
 		assertEquals(-1, control.getSelectionIndex());
 		assertEquals(0, control.getItemCount());
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		assertEquals(-1, control.getSelectionIndex());
 		assertEquals(0, control.getItemCount());
 
 		ridget.updateFromModel();
 
 		assertEquals(aManager.getItems().size(), control.getItemCount());
 		assertEquals(aSelection1, getSelectedString(control));
 
 		for (int i = 0; i < control.getItemCount(); i++) {
 			if (!aManager.getItems().contains(control.getItem(i))) {
 				fail();
 			}
 		}
 
 		aManager.setSelectedItem(aSelection2);
 
 		assertEquals(aSelection1, getSelectedString(control));
 
 		ridget.updateFromModel();
 
 		assertEquals(aSelection2, getSelectedString(control));
 
 		control.select(2);
 
 		assertEquals(aSelection3, getSelectedString(control));
 		assertEquals(aSelection3, control.getText());
 		assertEquals(aSelection3, aManager.getSelectedItem());
 	}
 
 	public void testBindToModelWithNoControl() {
 		ComboRidget ridget = new ComboRidget();
 		Combo control = new Combo(getShell(), SWT.READ_ONLY);
 
 		ridget.bindToModel(manager, "persons", String.class, "getListEntry", manager, "selectedPerson");
 		ridget.updateFromModel();
 
 		assertEquals(0, control.getItemCount());
 
 		ridget.setUIControl(control);
 
 		assertEquals(manager.getPersons().size(), control.getItemCount());
 	}
 
 	public void testFirePropertyChangeSelection() {
 		manager.setSelectedPerson(selection1);
 
 		ComboRidget ridget = getRidget();
 		Combo control = getWidget();
 
 		assertEquals(null, getSelectedString(control));
 		assertEquals(0, control.getItemCount());
 		ridget.bindToModel(manager, "persons", String.class, "getListEntry", manager, "selectedPerson");
 		assertEquals(null, getSelectedString(control));
 		assertEquals(0, control.getItemCount());
 
 		ridget.updateFromModel();
 
 		expectPropertyChangeEvents(new SelectionPropertyChangeEvent(selection1, selection2));
 		control.select(1);
 		verifyPropertyChangeEvents();
 
 		expectNoPropertyChangeEvent();
 		control.select(1);
 		verifyPropertyChangeEvents();
 
 		expectPropertyChangeEvents(new SelectionPropertyChangeEvent(selection2, selection3),
 				new SelectionPropertyChangeEvent(selection3, selection1));
 		control.select(2);
 		control.select(0);
 		verifyPropertyChangeEvents();
 	}
 
 	public void testUpdateSelection() {
 		manager.setSelectedPerson(selection1);
 
 		ComboRidget ridget = getRidget();
 		Combo control = getWidget();
 
 		assertEquals(null, getSelectedString(control));
 		assertEquals(0, control.getItemCount());
 		ridget.bindToModel(manager, "persons", String.class, null, manager, "selectedPerson");
 		assertEquals(null, getSelectedString(control));
 		assertEquals(0, control.getItemCount());
 
 		ridget.updateFromModel();
 
 		assertEquals(selection1.toString(), getSelectedString(control));
 
 		// ComboBoxSwingRidget.RidgetBasedComboBoxModel.temporaryItem
 		// will be set
 		control.select(2);
 		manager.setSelectedPerson(selection2);
 
 		assertEquals(selection3.toString(), getSelectedString(control));
 		assertEquals(selection2, manager.getSelectedPerson());
 
 		ridget.updateFromModel();
 
 		assertEquals(selection2.toString(), getSelectedString(control));
 		assertEquals(selection2, manager.getSelectedPerson());
 	}
 
 	public void testGetObservableListWithStrings() {
 		StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 
 		ComboRidget ridget = getRidget();
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 
 		assertEquals(0, ridget.getObservableList().size());
 
 		ridget.updateFromModel();
 
 		assertEquals(aManager.getItems().size(), ridget.getObservableList().size());
 		for (String item : new String[] { "A", "B", "C", "D", "E" }) {
 			assertTrue(ridget.getObservableList().contains(item));
 		}
 	}
 
 	public void testGetSelectionIndex() {
 		StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 
 		ComboRidget ridget = getRidget();
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		assertEquals(-1, getRidget().getSelectionIndex());
 
 		getWidget().select(1);
 		assertEquals(1, getRidget().getSelectionIndex());
 
 		ridget.setUIControl(null);
 		assertEquals(1, getRidget().getSelectionIndex());
 	}
 
 	public void testGetSelection() {
 		ComboRidget ridget = getRidget();
 		StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		assertEquals(null, getRidget().getSelection());
 
 		getWidget().select(1);
 
 		assertEquals("B", getRidget().getSelection());
 
 		ridget.setUIControl(null);
 
 		assertEquals("B", getRidget().getSelection());
 	}
 
 	/**
 	 * Should be deleted when the deprecated method getSelectionObservable() is
 	 * removed.
 	 */
	@SuppressWarnings("deprecation")
 	public void testGetSelectionObservable() {
 		ComboRidget ridget = getRidget();
 		StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		IObservableValue selectionObservable = getRidget().getSelectionObservable();
 
 		assertEquals(null, selectionObservable.getValue());
 
 		getWidget().select(1);
 
 		assertEquals("B", selectionObservable.getValue());
 
 		ridget.setUIControl(null);
 
 		assertEquals("B", selectionObservable.getValue());
 	}
 
 	public void testSetSelectionInt() {
 		ComboRidget ridget = getRidget();
 		Combo control = ridget.getUIControl();
 		StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		assertEquals(null, ridget.getSelection());
 
 		ridget.setSelection(0);
 
 		assertEquals("A", ridget.getSelection());
 		assertEquals("A", control.getItem(control.getSelectionIndex()));
 
 		ridget.setSelection(1);
 
 		assertEquals("B", ridget.getSelection());
 		assertEquals("B", control.getItem(control.getSelectionIndex()));
 
 		ridget.setUIControl(null);
 		ridget.setSelection(2);
 
 		assertEquals("C", ridget.getSelection());
 		assertEquals("B", control.getItem(control.getSelectionIndex()));
 
 		ridget.setUIControl(control);
 
 		assertEquals("C", ridget.getSelection());
 		assertEquals("C", control.getItem(control.getSelectionIndex()));
 
 		ridget.setSelection(-1);
 
 		assertEquals(null, ridget.getSelection());
 		assertEquals(-1, control.getSelectionIndex());
 
 		try {
 			ridget.setSelection(999);
 			fail();
 		} catch (RuntimeException rex) {
 			ok();
 		}
 	}
 
 	public void testSetSelectionString() {
 		ComboRidget ridget = getRidget();
 		Combo control = ridget.getUIControl();
 		StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		assertEquals(null, ridget.getSelection());
 
 		ridget.setSelection("A");
 
 		assertEquals("A", ridget.getSelection());
 		assertEquals("A", control.getItem(control.getSelectionIndex()));
 
 		ridget.setSelection("B");
 
 		assertEquals("B", ridget.getSelection());
 		assertEquals("B", control.getItem(control.getSelectionIndex()));
 
 		ridget.setUIControl(null);
 		ridget.setSelection("C");
 
 		assertEquals("C", ridget.getSelection());
 		assertEquals("B", control.getItem(control.getSelectionIndex()));
 
 		ridget.setUIControl(control);
 
 		assertEquals("C", ridget.getSelection());
 		assertEquals("C", control.getItem(control.getSelectionIndex()));
 
 		ridget.setSelection("X");
 
 		assertEquals(null, ridget.getSelection());
 		assertEquals(-1, control.getSelectionIndex());
 
 		ridget.setSelection("A");
 		ridget.setSelection(null);
 
 		assertEquals(null, ridget.getSelection());
 		assertEquals(-1, control.getSelectionIndex());
 	}
 
 	public void testSetSelectionWhenNotBoundToModel() {
 		ComboRidget ridget = getRidget();
 
 		try {
 			ridget.setSelection(new Object());
 			fail();
 		} catch (BindingException bex) {
 			ok();
 		}
 	}
 
 	public void testOutputCannotBeChangedFromUI() {
 		ComboRidget ridget = getRidget();
 		Combo control = getWidget();
 		StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		assertNull(ridget.getSelection());
 		assertEquals(-1, control.getSelectionIndex());
 
 		ridget.setOutputOnly(true);
 		control.setFocus();
 		UITestHelper.sendString(control.getDisplay(), "A");
 
 		assertNull(ridget.getSelection());
 		assertEquals(-1, control.getSelectionIndex());
 
 		ridget.setOutputOnly(false);
 		control.setFocus();
 		UITestHelper.sendString(control.getDisplay(), "A");
 
 		assertEquals("A", ridget.getSelection());
 		assertEquals(0, control.getSelectionIndex());
 	}
 
 	/**
 	 * Tests that changing the selection in ridget works as expected, even when
 	 * the ridget is disabled.
 	 */
 	public void testDisabledComboIsEmptyFromRidget() {
 		if (!MarkerSupport.HIDE_DISABLED_RIDGET_CONTENT) {
 			System.out.println("Skipping ComboRidgetTest.testDisabledComboIsEmptyFromRidget()");
 			return;
 		}
 
 		ComboRidget ridget = getRidget();
 		Combo control = getWidget();
 		ridget.bindToModel(manager, "persons", String.class, null, manager, "selectedPerson");
 		ridget.updateFromModel();
 
 		ridget.setSelection(selection1);
 
 		assertEquals(selection1.toString(), control.getText());
 		assertEquals(selection1, ridget.getSelection());
 		assertEquals(selection1, manager.getSelectedPerson());
 
 		ridget.setEnabled(false);
 
 		assertEquals("", control.getText());
 		assertEquals(selection1, ridget.getSelection());
 		assertEquals(selection1, manager.getSelectedPerson());
 
 		ridget.setSelection(selection2);
 
 		assertEquals("", control.getText());
 		assertEquals(selection2, ridget.getSelection());
 		assertEquals(selection2, manager.getSelectedPerson());
 
 		ridget.setEnabled(true);
 
 		assertEquals(selection2.toString(), control.getText());
 		assertEquals(selection2, ridget.getSelection());
 		assertEquals(selection2, manager.getSelectedPerson());
 	}
 
 	/**
 	 * Tests that changing the selection in a bound model works as expected,
 	 * even when the ridget is disabled.
 	 */
 	public void testDisabledComboIsEmptyFromModel() {
 		if (!MarkerSupport.HIDE_DISABLED_RIDGET_CONTENT) {
 			System.out.println("Skipping ComboRidgetTest.testDisabledComboIsEmptyFromModel()");
 			return;
 		}
 
 		ComboRidget ridget = getRidget();
 		Combo control = getWidget();
 		ridget.bindToModel(manager, "persons", String.class, null, manager, "selectedPerson");
 
 		manager.setSelectedPerson(selection1);
 		ridget.updateFromModel();
 
 		assertEquals(selection1.toString(), control.getText());
 		assertEquals(selection1, ridget.getSelection());
 		assertEquals(selection1, manager.getSelectedPerson());
 
 		ridget.setEnabled(false);
 
 		assertEquals("", control.getText());
 		assertEquals(selection1, ridget.getSelection());
 		assertEquals(selection1, manager.getSelectedPerson());
 
 		manager.setSelectedPerson(selection2);
 		ridget.updateFromModel();
 
 		assertEquals("", control.getText());
 		assertEquals(selection2, ridget.getSelection());
 		assertEquals(selection2, manager.getSelectedPerson());
 
 		ridget.setEnabled(true);
 
 		assertEquals(selection2.toString(), control.getText());
 		assertEquals(selection2, ridget.getSelection());
 		assertEquals(selection2, manager.getSelectedPerson());
 	}
 
 	/**
 	 * Tests that disabling / enabling the ridget does not fire selection events
 	 * (because the combo is modified internally).
 	 */
 	public void testDisabledDoesNotFireSelection() {
 		ComboRidget ridget = getRidget();
 		ridget.bindToModel(manager, "persons", String.class, null, manager, "selectedPerson");
 		ridget.updateFromModel();
 
 		FTPropertyChangeListener pcl = new FTPropertyChangeListener();
 		ridget.addPropertyChangeListener(IComboRidget.PROPERTY_SELECTION, pcl);
 
 		ridget.setSelection(selection1);
 
 		assertEquals(1, pcl.getCount());
 
 		ridget.setEnabled(false);
 
 		assertEquals(1, pcl.getCount());
 
 		ridget.setSelection(selection2);
 
 		assertEquals(2, pcl.getCount());
 
 		ridget.setEnabled(true);
 
 		assertEquals(2, pcl.getCount());
 	}
 
 	/**
 	 * Check that disabling / enabling works when we don't have a bound model.
 	 */
 	public void testDisableWithoutBoundModel() {
 		ComboRidget ridget = getRidget();
 		Combo control = getWidget();
 
 		assertTrue(ridget.getObservableList().isEmpty());
 
 		ridget.setEnabled(false);
 
 		assertFalse(ridget.isEnabled());
 		assertFalse(control.isEnabled());
 
 		ridget.setEnabled(true);
 
 		assertTrue(ridget.isEnabled());
 		assertTrue(control.isEnabled());
 	}
 
 	/**
 	 * Tests that the disabled state is applied to a new control when set into
 	 * the ridget.
 	 */
 	public void testDisableAndClearOnBind() {
 		ComboRidget ridget = getRidget();
 		Combo control = getWidget();
 
 		ridget.setUIControl(null);
 		ridget.setEnabled(false);
 		manager.setSelectedPerson(selection1);
 		ridget.bindToModel(manager, "persons", String.class, null, manager, "selectedPerson");
 		ridget.updateFromModel();
 		ridget.setUIControl(control);
 
 		assertFalse(control.isEnabled());
 		assertEquals("", control.getText());
 		assertEquals(selection1, ridget.getSelection());
 
 		ridget.setEnabled(true);
 
 		assertTrue(control.isEnabled());
 		assertEquals(selection1.toString(), control.getText());
 		assertEquals(selection1, ridget.getSelection());
 	}
 
 	// helping methods
 	// ////////////////
 
 	private String getSelectedString(Combo combo) {
 		int index = combo.getSelectionIndex();
 		return index == -1 ? null : combo.getItem(index);
 	}
 
 	private boolean find(PersonManager manager, String item) {
 		boolean result = false;
 		Iterator<Person> iter = manager.getPersons().iterator();
 		while (!result && iter.hasNext()) {
 			result = iter.next().toString().equals(item);
 		}
 		return result;
 	}
 
 	private void checkPersonList(PersonManager manager) {
 		Combo control = getWidget();
 
 		assertEquals(manager.getPersons().size(), control.getItemCount());
 
 		Collection<String> listEntries = new ArrayList<String>();
 		for (Person person : manager.getPersons()) {
 			listEntries.add(person.getListEntry());
 		}
 		for (int i = 0; i < control.getItemCount(); i++) {
 			String item = control.getItem(i);
 			if (!listEntries.contains(item)) {
 				fail();
 			}
 		}
 	}
 
 	private Collection<Person> createPersonList() {
 		Collection<Person> newList = new ArrayList<Person>();
 
 		Person person = new Person("Doe", "John");
 		person.setEyeColor(1);
 		newList.add(person);
 
 		person = new Person("Jackson", "Janet");
 		person.setEyeColor(1);
 		newList.add(person);
 
 		person = new Person("Jackson", "Jermaine");
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
 
 	// helping classes
 	//////////////////
 
 	private final class SelectionPropertyChangeEvent extends PropertyChangeEvent {
 
 		private static final long serialVersionUID = 4711L;
 
 		public SelectionPropertyChangeEvent(Object oldValue, Object newValue) {
 			super(getRidget(), IComboRidget.PROPERTY_SELECTION, oldValue, newValue);
 		}
 	}
 
 	private static final class FTPropertyChangeListener implements PropertyChangeListener {
 
 		private int count;
 
 		public int getCount() {
 			return count;
 		}
 
 		public void propertyChange(PropertyChangeEvent evt) {
 			count++;
 		}
 	}
 }
