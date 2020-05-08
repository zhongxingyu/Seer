 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
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
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.core.databinding.beans.PojoObservables;
 import org.eclipse.core.databinding.conversion.Converter;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.databinding.observable.value.IValueChangeListener;
 import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
 import org.eclipse.core.databinding.observable.value.WritableValue;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 
 import org.eclipse.riena.beans.common.Person;
 import org.eclipse.riena.beans.common.PersonManager;
 import org.eclipse.riena.beans.common.StringManager;
 import org.eclipse.riena.beans.common.StringPojo;
 import org.eclipse.riena.core.marker.AbstractMarker;
 import org.eclipse.riena.internal.ui.swt.test.UITestHelper;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.ridgets.IComboRidget;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.listener.ISelectionListener;
 import org.eclipse.riena.ui.ridgets.listener.SelectionEvent;
 import org.eclipse.riena.ui.ridgets.swt.AbstractComboRidget;
 import org.eclipse.riena.ui.ridgets.swt.MarkerSupport;
 import org.eclipse.riena.ui.swt.CompletionCombo;
 
 /**
  * Tests of the class {@link ComboRidget}.
  */
 public abstract class AbstractComboRidgetTest extends AbstractSWTRidgetTest {
 
 	private PersonManager manager;
 	private Person selection1;
 	private Person selection2;
 	private Person selection3;
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
 		manager = new PersonManager(createPersonList());
 		final Iterator<Person> it = manager.getPersons().iterator();
 		selection1 = it.next();
 		selection2 = it.next();
 		selection3 = it.next();
 	}
 
 	@Override
 	protected final Control getWidget() {
 		return (Control) super.getWidget();
 	}
 
 	@Override
 	protected final AbstractComboRidget getRidget() {
 		return (AbstractComboRidget) super.getRidget();
 	}
 
 	protected abstract Control createWidget(Composite parent, int style);
 
 	// test methods
 	///////////////
 
 	public void testBindingWithNullProperty() throws Exception {
 		final AbstractComboRidget ridget = getRidget();
 		ridget.setUIControl(createWidget(getShell()));
 
 		final ProductHolder model = new ProductHolder();
 		final List<Product> products = new ArrayList<Product>();
 		products.add(new Product("one"));
 		products.add(new Product("two"));
 		products.add(new Product(null));
 		products.add(new Product("four"));
 		products.add(new Product("five"));
 		model.setProducts(products);
 		ridget.bindToModel(model, "products", Product.class, null, model, "selectedProducts");
 
 		try {
 			ridget.updateFromModel();
 			fail();
 		} catch (final NullPointerException npe) {
 			ok("expected");
 		}
 
 		// there are only two elements expected, because the third element has a null property 
 		assertEquals(2, getItemCount(ridget.getUIControl()));
 	}
 
 	public void testBindingWithNullElement() throws Exception {
 		final AbstractComboRidget ridget = getRidget();
 		ridget.setUIControl(createWidget(getShell()));
 
 		final ProductHolder model = new ProductHolder();
 		final List<Product> products = new ArrayList<Product>();
 		products.add(new Product("one"));
 		products.add(new Product("two"));
 		products.add(null);
 		products.add(new Product("four"));
 		products.add(new Product("five"));
 		model.setProducts(products);
 		ridget.bindToModel(model, "products", Product.class, null, model, "selectedProducts");
 
 		try {
 			ridget.updateFromModel();
 			fail();
 		} catch (final NullPointerException npe) {
 			ok("expected");
 		}
 
 		// there are only two elements expected, because the third element is null 
 		assertEquals(2, getItemCount(ridget.getUIControl()));
 	}
 
 	public void testSetUIControl() {
 		final AbstractComboRidget ridget = getRidget();
 
 		ridget.setUIControl(null);
 		assertNull(ridget.getUIControl());
 
 		ridget.setUIControl(getWidget());
 		assertSame(getWidget(), ridget.getUIControl());
 	}
 
 	public void testSetUIControlInvalid() {
 		final AbstractComboRidget ridget = getRidget();
 
 		try {
 			ridget.setUIControl(getShell());
 			fail();
 		} catch (final BindingException bex) {
 			ok();
 		}
 	}
 
 	public void testGetEmptySelectionItem() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		final StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		assertNull(ridget.getEmptySelectionItem());
 
 		final Object emptySelectionItem = "A";
 		ridget.setEmptySelectionItem(emptySelectionItem);
 
 		assertSame(emptySelectionItem, ridget.getEmptySelectionItem());
 
 		ridget.setSelection("A");
 
 		final int controlSelectedItemIndex = getSelectionIndex(control);
 		assertEquals(-1, ridget.getSelectionIndex());
 		assertEquals(0, controlSelectedItemIndex);
 		assertEquals(null, ridget.getSelection());
 		assertEquals("A", getItem(control, controlSelectedItemIndex));
 
 	}
 
 	public void testBindToModelWithDomainObjects() {
 		manager.setSelectedPerson(selection1);
 
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 
 		assertEquals(null, getSelectedString(control));
 		assertEquals(0, getItemCount(control));
 
 		ridget.bindToModel(manager, "persons", String.class, null, manager, "selectedPerson");
 
 		assertEquals(null, getSelectedString(control));
 		assertEquals(0, getItemCount(control));
 
 		ridget.updateFromModel();
 
 		assertEquals(selection1.toString(), getSelectedString(control));
 		assertEquals(manager.getPersons().size(), getItemCount(control));
 
 		for (int i = 0; i < getItemCount(control); i++) {
 			final String item = getItem(control, i);
 			if (!find(manager, item)) {
 				fail();
 			}
 		}
 
 		manager.setSelectedPerson(selection2);
 
 		assertEquals(selection1.toString(), getSelectedString(control));
 
 		ridget.updateFromModel();
 
 		assertEquals(selection2.toString(), getSelectedString(control));
 
 		select(control, 2);
 
 		assertEquals(selection3.toString(), getSelectedString(control));
 	}
 
 	public void testBindToModelWithDomainObjectsUsingColumnPropertyName() {
 		manager.setSelectedPerson(selection1);
 
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 
 		assertEquals(null, getSelectedString(control));
 		assertEquals(0, getItemCount(control));
 		ridget.bindToModel(manager, "persons", String.class, "getListEntry", manager, "selectedPerson");
 		assertEquals(null, getSelectedString(control));
 		assertEquals(0, getItemCount(control));
 
 		ridget.updateFromModel();
 
 		assertEquals(selection1.getListEntry(), getSelectedString(control));
 		assertEquals(selection1.getListEntry(), getText(control));
 		checkPersonList(manager);
 		assertEquals(selection1, manager.getSelectedPerson());
 
 		manager.setSelectedPerson(selection2);
 
 		assertEquals(selection1.getListEntry(), getSelectedString(control));
 		assertEquals(selection1.getListEntry(), getText(control));
 		assertEquals(selection2, manager.getSelectedPerson());
 
 		ridget.updateFromModel();
 
 		assertEquals(selection2, manager.getSelectedPerson());
 		assertEquals(selection2.getListEntry(), getSelectedString(control));
 		assertEquals(selection2.getListEntry(), getText(control));
 
 		select(control, 2);
 
 		assertEquals(selection3.getListEntry(), getSelectedString(control));
 		assertEquals(selection3.getListEntry(), getText(control));
 		assertEquals(selection3, manager.getSelectedPerson());
 	}
 
 	public void testBindToModelWithStrings() {
 		final StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		final Iterator<String> it = aManager.getItems().iterator();
 		final String aSelection1 = it.next();
 		final String aSelection2 = it.next();
 		final String aSelection3 = it.next();
 		aManager.setSelectedItem(aSelection1);
 
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 
 		assertEquals(-1, getSelectionIndex(control));
 		assertEquals(0, getItemCount(control));
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		assertEquals(-1, getSelectionIndex(control));
 		assertEquals(0, getItemCount(control));
 
 		ridget.updateFromModel();
 
 		assertEquals(aManager.getItems().size(), getItemCount(control));
 		assertEquals(aSelection1, getSelectedString(control));
 
 		for (int i = 0; i < getItemCount(control); i++) {
 			if (!aManager.getItems().contains(getItem(control, i))) {
 				fail();
 			}
 		}
 
 		aManager.setSelectedItem(aSelection2);
 
 		assertEquals(aSelection1, getSelectedString(control));
 
 		ridget.updateFromModel();
 
 		assertEquals(aSelection2, getSelectedString(control));
 
 		select(control, 2);
 
 		assertEquals(aSelection3, getSelectedString(control));
 		assertEquals(aSelection3, getText(control));
 		assertEquals(aSelection3, aManager.getSelectedItem());
 	}
 
 	public void testBindToModelWithNoControl() {
 		final AbstractComboRidget ridget = (AbstractComboRidget) createRidget();
 		final Control control = (Control) createWidget(getShell());
 
 		ridget.bindToModel(manager, "persons", String.class, "getListEntry", manager, "selectedPerson");
 		ridget.updateFromModel();
 
 		assertEquals(0, getItemCount(control));
 
 		ridget.setUIControl(control);
 
 		assertEquals(manager.getPersons().size(), getItemCount(control));
 	}
 
 	public void testFirePropertyChangeSelection() {
 		manager.setSelectedPerson(selection1);
 
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 
 		assertEquals(null, getSelectedString(control));
 		assertEquals(0, getItemCount(control));
 		ridget.bindToModel(manager, "persons", String.class, "getListEntry", manager, "selectedPerson");
 		assertEquals(null, getSelectedString(control));
 		assertEquals(0, getItemCount(control));
 
 		ridget.updateFromModel();
 
 		expectPropertyChangeEvents(new TextPropertyChangeEvent(selection1, selection2),
 				new SelectionPropertyChangeEvent(selection1, selection2));
 		select(control, 1);
 		verifyPropertyChangeEvents();
 
 		expectNoPropertyChangeEvent();
 		select(control, 1);
 		verifyPropertyChangeEvents();
 
 		expectPropertyChangeEvents(new TextPropertyChangeEvent(selection2, selection3),
 				new SelectionPropertyChangeEvent(selection2, selection3), new TextPropertyChangeEvent(selection3,
 						selection1), new SelectionPropertyChangeEvent(selection3, selection1));
 		select(control, 2);
 		select(control, 0);
 		verifyPropertyChangeEvents();
 	}
 
 	public void testUpdateFromModel() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 
 		ridget.bindToModel(manager, "persons", String.class, null, manager, "selectedPerson");
 
 		assertEquals(0, getItemCount(control));
 
 		ridget.updateFromModel();
 
 		final int oldSize = manager.getPersons().size();
 		assertEquals(oldSize, getItemCount(control));
 
 		// remove 1 person
 		manager.getPersons().remove(manager.getPersons().iterator().next());
 
 		assertEquals(oldSize, getItemCount(control));
 
 		ridget.updateFromModel();
 
 		assertEquals(oldSize - 1, getItemCount(control));
 	}
 
 	public void testUpdateFromModelWithNullValue() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		final StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		aManager.setSelectedItem("A");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		assertEquals("A", ridget.getText());
 		assertEquals("A", getText(control));
 
 		aManager.setSelectedItem(null);
 		ridget.updateFromModel();
 
 		assertEquals("", ridget.getText());
 		assertEquals("", getText(control));
 	}
 
 	public void testUpdateSelection() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		manager.setSelectedPerson(selection1);
 
 		assertEquals(null, getSelectedString(control));
 		assertEquals(0, getItemCount(control));
 
 		ridget.bindToModel(manager, "persons", String.class, null, manager, "selectedPerson");
 
 		assertEquals(null, getSelectedString(control));
 		assertEquals(0, getItemCount(control));
 
 		ridget.updateFromModel();
 
 		assertEquals(selection1.toString(), getSelectedString(control));
 
 		select(control, 2);
 		manager.setSelectedPerson(selection2);
 
 		assertEquals(selection3.toString(), getSelectedString(control));
 		assertEquals(selection2, manager.getSelectedPerson());
 
 		ridget.updateFromModel();
 
 		assertEquals(selection2.toString(), getSelectedString(control));
 		assertEquals(selection2, manager.getSelectedPerson());
 	}
 
 	public void testGetObservableListWithStrings() {
 		final StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 
 		final AbstractComboRidget ridget = getRidget();
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 
 		assertEquals(0, ridget.getObservableList().size());
 
 		ridget.updateFromModel();
 
 		assertEquals(aManager.getItems().size(), ridget.getObservableList().size());
 		for (final String item : new String[] { "A", "B", "C", "D", "E" }) {
 			assertTrue(ridget.getObservableList().contains(item));
 		}
 	}
 
 	public void testGetSelectionIndex() {
 		final StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 
 		final AbstractComboRidget ridget = getRidget();
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		assertEquals(-1, getRidget().getSelectionIndex());
 
 		select(getWidget(), 1);
 		assertEquals(1, getRidget().getSelectionIndex());
 
 		ridget.setUIControl(null);
 		assertEquals(1, getRidget().getSelectionIndex());
 	}
 
 	public void testGetSelection() {
 		final AbstractComboRidget ridget = getRidget();
 		final StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		assertEquals(null, getRidget().getSelection());
 
 		select(getWidget(), 1);
 
 		assertEquals("B", getRidget().getSelection());
 
 		ridget.setUIControl(null);
 
 		assertEquals("B", getRidget().getSelection());
 	}
 
 	public void testSetSelectionInt() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = ridget.getUIControl();
 		final StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		assertEquals(null, ridget.getSelection());
 
 		ridget.setSelection(0);
 
 		assertEquals("A", ridget.getSelection());
 		assertEquals("A", getItem(control, getSelectionIndex(control)));
 
 		ridget.setSelection(1);
 
 		assertEquals("B", ridget.getSelection());
 		assertEquals("B", getItem(control, getSelectionIndex(control)));
 
 		ridget.setUIControl(null);
 		ridget.setSelection(2);
 
 		assertEquals("C", ridget.getSelection());
 		assertEquals("B", getItem(control, getSelectionIndex(control)));
 
 		ridget.setUIControl(control);
 
 		assertEquals("C", ridget.getSelection());
 		assertEquals("C", getItem(control, getSelectionIndex(control)));
 
 		ridget.setSelection(-1);
 
 		assertEquals(null, ridget.getSelection());
 		assertEquals(-1, getSelectionIndex(control));
 
 		try {
 			ridget.setSelection(999);
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 	}
 
 	public void testSetSelectionString() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = ridget.getUIControl();
 		final StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		assertEquals(null, ridget.getSelection());
 
 		ridget.setSelection("A");
 
 		assertEquals("A", ridget.getSelection());
 		assertEquals("A", getItem(control, getSelectionIndex(control)));
 
 		ridget.setSelection("B");
 
 		assertEquals("B", ridget.getSelection());
 		assertEquals("B", getItem(control, getSelectionIndex(control)));
 
 		ridget.setUIControl(null);
 		ridget.setSelection("C");
 
 		assertEquals("C", ridget.getSelection());
 		assertEquals("B", getItem(control, getSelectionIndex(control)));
 
 		ridget.setUIControl(control);
 
 		assertEquals("C", ridget.getSelection());
 		assertEquals("C", getItem(control, getSelectionIndex(control)));
 
 		ridget.setSelection("X");
 
 		assertEquals("X", ridget.getSelection());
 		assertEquals(-1, getSelectionIndex(control));
 
 		ridget.setSelection("A");
 		ridget.setSelection(null);
 
 		assertEquals(null, ridget.getSelection());
 		assertEquals(-1, getSelectionIndex(control));
 	}
 
 	public void testSetSelectionWhenNotBoundToModel() {
 		final AbstractComboRidget ridget = getRidget();
 
 		try {
 			ridget.setSelection(new Object());
 			fail();
 		} catch (final BindingException bex) {
 			ok();
 		}
 	}
 
 	public void testOutputCannotBeChangedFromUI() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		final StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		assertNull(ridget.getSelection());
 		assertEquals(-1, getSelectionIndex(control));
 
 		ridget.setOutputOnly(true);
 		control.setFocus();
 		selectA(control);
 
 		assertNull(ridget.getSelection());
 		assertEquals(-1, getSelectionIndex(control));
 
 		ridget.setOutputOnly(false);
 		control.setFocus();
 		selectA(control);
 
 		assertEquals("A", ridget.getSelection());
 		assertEquals(0, getSelectionIndex(control));
 	}
 
 	public void testOutputControlIsNotEditableAndHasText() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		ridget.bindToModel(manager, "persons", String.class, null, manager, "selectedPerson");
 		ridget.updateFromModel();
 		ridget.setSelection(selection1);
 
 		assertTrue(control.isEnabled());
 		assertEquals(selection1.toString(), getText(control));
 		assertEquals(selection1, ridget.getSelection());
 
 		ridget.setOutputOnly(true);
 
 		assertControlEditable(control, false);
 		assertEquals(selection1.toString(), getText(control));
 		assertEquals(selection1, ridget.getSelection());
 
 		ridget.setEnabled(false);
 		ridget.setEnabled(true);
 
 		assertControlEditable(control, false);
 		assertEquals(selection1.toString(), getText(control));
 		assertEquals(selection1, ridget.getSelection());
 
 		ridget.setOutputOnly(false);
 
 		assertControlEditable(control, true);
 		assertEquals(selection1.toString(), getText(control));
 		assertEquals(selection1, ridget.getSelection());
 	}
 
 	public void testOutputOnlyAndMandatoryControlIsNotEditable() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 
 		assertControlEditable(control, true);
 
 		ridget.setOutputOnly(true);
 
 		assertControlEditable(control, false);
 
 		ridget.setMandatory(true);
 
 		assertControlEditable(control, false);
 
 		ridget.setMandatory(false);
 
 		assertControlEditable(control, false);
 
 		ridget.setOutputOnly(false);
 
 		assertControlEditable(control, true);
 	}
 
 	public void testOutputControlIsUpdatedOnBind() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 
 		ridget.setUIControl(null);
 		ridget.setOutputOnly(true);
 		ridget.bindToModel(manager, "persons", String.class, null, manager, "selectedPerson");
 		ridget.updateFromModel();
 		ridget.setSelection(selection1);
 
 		assertEquals(selection1, ridget.getSelection());
 		assertEquals("", getText(control));
 		assertTrue(control.isEnabled());
 
 		ridget.setUIControl(control);
 
 		assertEquals(selection1, ridget.getSelection());
 		assertEquals(selection1.toString(), getText(control));
 		assertControlEditable(control, false);
 	}
 
 	/**
 	 * Tests that changing the selection in ridget works as expected, even when
 	 * the ridget is disabled.
 	 */
 	public void testDisabledComboIsEmptyFromRidget() {
 		if (!MarkerSupport.isHideDisabledRidgetContent()) {
 			System.out.println("Skipping ComboRidgetTest.testDisabledComboIsEmptyFromRidget()");
 			return;
 		}
 
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		ridget.bindToModel(manager, "persons", String.class, null, manager, "selectedPerson");
 		ridget.updateFromModel();
 
 		ridget.setSelection(selection1);
 
 		assertEquals(selection1.toString(), getText(control));
 		assertEquals(selection1, ridget.getSelection());
 		assertEquals(selection1, manager.getSelectedPerson());
 
 		ridget.setEnabled(false);
 
 		assertEquals("", getText(control));
 		assertEquals(selection1, ridget.getSelection());
 		assertEquals(selection1, manager.getSelectedPerson());
 
 		ridget.setSelection(selection2);
 
 		assertEquals("", getText(control));
 		assertEquals(selection2, ridget.getSelection());
 		assertEquals(selection2, manager.getSelectedPerson());
 
 		ridget.setEnabled(true);
 
 		assertEquals(selection2.toString(), getText(control));
 		assertEquals(selection2, ridget.getSelection());
 		assertEquals(selection2, manager.getSelectedPerson());
 	}
 
 	/**
 	 * Tests that changing the selection in a bound model works as expected,
 	 * even when the ridget is disabled.
 	 */
 	public void testDisabledComboIsEmptyFromModel() {
 		if (!MarkerSupport.isHideDisabledRidgetContent()) {
 			System.out.println("Skipping ComboRidgetTest.testDisabledComboIsEmptyFromModel()");
 			return;
 		}
 
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		ridget.bindToModel(manager, "persons", String.class, null, manager, "selectedPerson");
 
 		manager.setSelectedPerson(selection1);
 		ridget.updateFromModel();
 
 		assertEquals(selection1.toString(), getText(control));
 		assertEquals(selection1, ridget.getSelection());
 		assertEquals(selection1, manager.getSelectedPerson());
 
 		ridget.setEnabled(false);
 
 		assertEquals("", getText(control));
 		assertEquals(selection1, ridget.getSelection());
 		assertEquals(selection1, manager.getSelectedPerson());
 
 		manager.setSelectedPerson(selection2);
 		ridget.updateFromModel();
 
 		assertEquals("", getText(control));
 		assertEquals(selection2, ridget.getSelection());
 		assertEquals(selection2, manager.getSelectedPerson());
 
 		ridget.setEnabled(true);
 
 		assertEquals(selection2.toString(), getText(control));
 		assertEquals(selection2, ridget.getSelection());
 		assertEquals(selection2, manager.getSelectedPerson());
 	}
 
 	/**
 	 * Tests that disabling / enabling the ridget does not fire selection events
 	 * (because the combo is modified internally).
 	 */
 	public void testDisabledDoesNotFireSelection() {
 		final AbstractComboRidget ridget = getRidget();
 		ridget.bindToModel(manager, "persons", String.class, null, manager, "selectedPerson");
 		ridget.updateFromModel();
 
 		final FTPropertyChangeListener pcl = new FTPropertyChangeListener();
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
 	 * As per Bug 327628: Tests that an output-only Combo does not fire
 	 * selection events when modified from the UI (because the combo is modified
 	 * internally).
 	 */
 	public void testOutputOnlyDoesNotFireSelectionFromUI() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		final StringManager stringManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(stringManager, "items", String.class, null, stringManager, "selectedItem");
 		ridget.updateFromModel();
 		ridget.setUIControl(control);
 
 		final FTPropertyChangeListener pcl = new FTPropertyChangeListener();
 		ridget.addPropertyChangeListener(IComboRidget.PROPERTY_SELECTION, pcl);
 
 		assertEquals(0, pcl.getCount());
 		assertEquals(null, ridget.getSelection());
 		assertEquals(null, getSelectedString(control));
 
 		ridget.setSelection("B");
 
 		assertEquals(1, pcl.getCount());
 		assertEquals("B", ridget.getSelection());
 		assertEquals("B", getSelectedString(control));
 
 		ridget.setOutputOnly(true);
 		control.setFocus();
 		selectA(control);
 
 		// A was not selected, B still selected.
 		assertEquals(1, pcl.getCount());
 		assertEquals("B", ridget.getSelection());
 		assertEquals("B", getSelectedString(control));
 	}
 
 	public void testOutputOnlyDoesFireSelectionWhenChangedViaSetSelection() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		final StringManager stringManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(stringManager, "items", String.class, null, stringManager, "selectedItem");
 		ridget.updateFromModel();
 		ridget.setUIControl(control);
 
 		final FTPropertyChangeListener pcl = new FTPropertyChangeListener();
 		ridget.addPropertyChangeListener(IComboRidget.PROPERTY_SELECTION, pcl);
 
 		assertEquals(0, pcl.getCount());
 		assertEquals(null, ridget.getSelection());
 		assertEquals(null, getSelectedString(control));
 
 		ridget.setOutputOnly(true);
 		ridget.setSelection("A");
 
 		assertEquals(1, pcl.getCount());
 		assertEquals("A", ridget.getSelection());
 		assertEquals("A", getSelectedString(control));
 
 		ridget.setSelection(1);
 
 		assertEquals(2, pcl.getCount());
 		assertEquals("B", ridget.getSelection());
 		assertEquals("B", getSelectedString(control));
 
 		ridget.setSelection(1);
 
 		assertEquals(2, pcl.getCount());
 		assertEquals("B", ridget.getSelection());
 		assertEquals("B", getSelectedString(control));
 	}
 
 	public void testOutputOnlyBlocksSelectionChangeFromUIAndAllowsChangeViaSetter() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		final StringManager stringManager = new StringManagerWithUpdateFromModel(ridget, "A", "B", "C", "D", "E");
 		ridget.bindToModel(stringManager, "items", String.class, null, stringManager, "selectedItem");
 		ridget.updateFromModel();
 		ridget.setUIControl(control);
 
 		ridget.setSelection("B");
 		assertEquals("B", ridget.getSelection());
 		assertEquals("B", getSelectedString(control));
 
 		ridget.setOutputOnly(true);
 
 		assertTrue(ridget.isOutputOnly());
 		assertTrue(control.isEnabled());
 
 		// block changes via keyboard  
 		control.setFocus();
 		selectA(control);
 
 		assertEquals("B", ridget.getSelection());
 		assertEquals("B", getSelectedString(control));
 
 		// allow changes via #setSelection
 		ridget.setSelection(2);
 
 		assertEquals("C", ridget.getSelection());
 		assertEquals("C", getSelectedString(control));
 	}
 
 	public void testOutputOnlyAllowsUpdateFromModel() {
 		final AbstractComboRidget ridget = getRidget();
 		final StringManager stringManager = new StringManager("A");
 		stringManager.setSelectedItem("A");
 		ridget.bindToModel(stringManager, "items", String.class, null, stringManager, "selectedItem");
 
 		ridget.setOutputOnly(true);
 		ridget.updateFromModel();
 
 		assertEquals("A", ridget.getSelection());
 		assertEquals(0, ridget.getSelectionIndex());
 
 		stringManager.setItems(Arrays.asList("B"));
 		stringManager.setSelectedItem("B");
 		ridget.updateFromModel();
 
 		assertEquals("B", ridget.getSelection());
 		assertEquals(0, ridget.getSelectionIndex());
 	}
 
 	/**
 	 * As per Bug 336588
 	 */
 	public void testOutputOnlyAndUpdateFromModelNPE() {
 		final AbstractComboRidget ridget = getRidget();
 		final StringManager stringManager = new StringManager("A");
 		stringManager.setSelectedItem("A");
 		ridget.bindToModel(stringManager, "items", String.class, null, stringManager, "selectedItem");
 
 		ridget.setOutputOnly(true);
 		ridget.updateFromModel();
 
 		assertEquals("A", ridget.getSelection());
 		assertEquals(0, ridget.getSelectionIndex());
 
 		stringManager.setItems(Arrays.asList("B"));
 		stringManager.setSelectedItem("B");
 		ridget.setOutputOnly(false);
 		ridget.updateFromModel();
 
 		assertEquals("B", ridget.getSelection());
 		assertEquals(0, ridget.getSelectionIndex());
 	}
 
 	public void testToggleOutputAndEnabledMarkers1() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		final StringManager stringManager = new StringManager("A");
 		stringManager.setSelectedItem("A");
 		ridget.bindToModel(stringManager, "items", String.class, null, stringManager, "selectedItem");
 		ridget.updateFromModel();
 
 		checkSelection(ridget, control, true);
 
 		ridget.setOutputOnly(true);
 
 		checkSelection(ridget, control, true);
 
 		ridget.setEnabled(false);
 
 		checkSelection(ridget, control, false);
 
 		ridget.setEnabled(true);
 
 		checkSelection(ridget, control, true);
 
 		ridget.setOutputOnly(false);
 
 		checkSelection(ridget, control, true);
 	}
 
 	public void testToggleOutputAndEnabledMarkers2() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		final StringManager stringManager = new StringManager("A");
 		stringManager.setSelectedItem("A");
 		ridget.bindToModel(stringManager, "items", String.class, null, stringManager, "selectedItem");
 		ridget.updateFromModel();
 
 		checkSelection(ridget, control, true);
 
 		ridget.setEnabled(false);
 
 		checkSelection(ridget, control, false);
 
 		ridget.setOutputOnly(true);
 
 		checkSelection(ridget, control, false);
 
 		ridget.setOutputOnly(false);
 
 		checkSelection(ridget, control, false);
 
 		ridget.setEnabled(true);
 
 		checkSelection(ridget, control, true);
 	}
 
 	public void testToggleOutputAndEnabledMarkers3() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		final StringManager stringManager = new StringManager("A");
 		stringManager.setSelectedItem("A");
 		ridget.bindToModel(stringManager, "items", String.class, null, stringManager, "selectedItem");
 		ridget.updateFromModel();
 
 		checkSelection(ridget, control, true);
 
 		ridget.setEnabled(false);
 
 		checkSelection(ridget, control, false);
 
 		ridget.setOutputOnly(true);
 
 		checkSelection(ridget, control, false);
 
 		ridget.setEnabled(true);
 
 		checkSelection(ridget, control, true);
 
 		ridget.setOutputOnly(false);
 
 		checkSelection(ridget, control, true);
 	}
 
 	public void testToggleOutputAndEnabledMarkers4() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		final StringManager stringManager = new StringManager("A");
 		stringManager.setSelectedItem("A");
 		ridget.bindToModel(stringManager, "items", String.class, null, stringManager, "selectedItem");
 		ridget.updateFromModel();
 
 		checkSelection(ridget, control, true);
 
 		ridget.setOutputOnly(true);
 
 		checkSelection(ridget, control, true);
 
 		ridget.setEnabled(false);
 
 		checkSelection(ridget, control, false);
 
 		ridget.setOutputOnly(false);
 
 		checkSelection(ridget, control, false);
 
 		ridget.setEnabled(true);
 
 		checkSelection(ridget, control, true);
 	}
 
 	public void testSetSelectionIntFiresEvents() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		final StringManager stringManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(stringManager, "items", String.class, null, stringManager, "selectedItem");
 		ridget.updateFromModel();
 		ridget.setUIControl(control);
 
 		final FTPropertyChangeListener pcl = new FTPropertyChangeListener();
 		ridget.addPropertyChangeListener(IComboRidget.PROPERTY_SELECTION, pcl);
 
 		assertEquals(0, pcl.getCount());
 		assertEquals(null, ridget.getSelection());
 		assertEquals(null, getSelectedString(control));
 
 		ridget.setSelection(0);
 
 		assertEquals(1, pcl.getCount());
 		assertEquals("A", ridget.getSelection());
 		assertEquals("A", getSelectedString(control));
 
 		ridget.setSelection(1);
 
 		assertEquals(2, pcl.getCount());
 		assertEquals("B", ridget.getSelection());
 		assertEquals("B", getSelectedString(control));
 
 		ridget.setSelection(1);
 
 		assertEquals(2, pcl.getCount());
 		assertEquals("B", ridget.getSelection());
 		assertEquals("B", getSelectedString(control));
 
 		ridget.setSelection(-1);
 
		assertEquals(3, pcl.getCount());
 		assertEquals(null, ridget.getSelection());
 		assertEquals(null, getSelectedString(control));
 
 		try {
 			ridget.setSelection(99);
 			fail();
 		} catch (final IndexOutOfBoundsException ioobe) {
 			// expected
 		}
 	}
 
 	public void testSetSelectionObjFiresEvents() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		final StringManager stringManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(stringManager, "items", String.class, null, stringManager, "selectedItem");
 		ridget.updateFromModel();
 		ridget.setUIControl(control);
 
 		final FTPropertyChangeListener pcl = new FTPropertyChangeListener();
 		ridget.addPropertyChangeListener(IComboRidget.PROPERTY_SELECTION, pcl);
 
 		assertEquals(0, pcl.getCount());
 		assertEquals(null, ridget.getSelection());
 		assertEquals(null, getSelectedString(control));
 
 		ridget.setSelection("A");
 
 		assertEquals(1, pcl.getCount());
 		assertEquals("A", ridget.getSelection());
 		assertEquals("A", getSelectedString(control));
 
 		ridget.setSelection("B");
 
 		assertEquals(2, pcl.getCount());
 		assertEquals("B", ridget.getSelection());
 		assertEquals("B", getSelectedString(control));
 
 		ridget.setSelection("B");
 
 		assertEquals(2, pcl.getCount());
 		assertEquals("B", ridget.getSelection());
 		assertEquals("B", getSelectedString(control));
 
 		ridget.setSelection("this does not exist");
 
		assertEquals(3, pcl.getCount());
 		assertEquals("this does not exist", ridget.getSelection());
 		assertEquals(null, getSelectedString(control));
 	}
 
 	/**
 	 * Check that disabling / enabling works when we don't have a bound model.
 	 */
 	public void testDisableWithoutBoundModel() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 
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
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 
 		ridget.setUIControl(null);
 		ridget.setEnabled(false);
 		manager.setSelectedPerson(selection1);
 		ridget.bindToModel(manager, "persons", String.class, null, manager, "selectedPerson");
 		ridget.updateFromModel();
 		ridget.setUIControl(control);
 
 		assertFalse(control.isEnabled());
 		assertEquals("", getText(control));
 		assertEquals(selection1, ridget.getSelection());
 
 		ridget.setEnabled(true);
 
 		assertTrue(control.isEnabled());
 		assertEquals(selection1.toString(), getText(control));
 		assertEquals(selection1, ridget.getSelection());
 	}
 
 	/**
 	 * Tests that selection has been updated before the listener is notified
 	 * (Bug 287440).
 	 */
 	public void testSelectionListenerHasLatestValues() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		final StringManager stringManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(stringManager, "items", String.class, null, stringManager, "selectedItem");
 		ridget.updateFromModel();
 		ridget.setUIControl(control);
 
 		assertEquals(-1, getSelectionIndex(control));
 		assertEquals(null, getSelectedString(control));
 		assertEquals(null, ridget.getSelection());
 		assertEquals(null, stringManager.getSelectedItem());
 
 		final FTPropertyChangeListener listener = new FTPropertyChangeListener();
 		listener.setRunnable(new Runnable() {
 			public void run() {
 				if (!(control instanceof CCombo) && !(control instanceof CompletionCombo)) {
 					// CCombo and CompletionCombo updates asynchronously by design, so we skip these:
 					assertEquals(0, getSelectionIndex(control));
 					assertEquals("A", getSelectedString(control));
 				}
 				assertEquals("A", ridget.getSelection());
 				assertEquals("A", stringManager.getSelectedItem());
 			}
 		});
 		ridget.addPropertyChangeListener(IComboRidget.PROPERTY_SELECTION, listener);
 
 		control.setFocus();
 		selectA(control);
 
 		assertEquals(1, listener.getCount()); // check that listener was called once
 		assertEquals(0, getSelectionIndex(control));
 		assertEquals("A", getSelectedString(control));
 		assertEquals("A", ridget.getSelection());
 		assertEquals("A", stringManager.getSelectedItem());
 	}
 
 	/**
 	 * Tests converting a model value into a UI value (String) and back (Bug
 	 * 290463).
 	 */
 	public void testValueConversion() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 
 		final Person[] persons = new Person[] { new Person("Einstein", "Albert"), new Person("Da Vinci", "Leonardo"),
 				new Person("Curie", "Marie") };
 		ridget.setModelToUIControlConverter(new Converter(Person.class, String.class) {
 			public Object convert(final Object fromObject) {
 				return getInitials((Person) fromObject);
 			}
 		});
 		ridget.setUIControlToModelConverter(new Converter(String.class, Person.class) {
 			public Object convert(final Object fromObject) {
 				for (final Person person : persons) {
 					if (fromObject.equals(getInitials(person))) {
 						return person;
 					}
 				}
 				return null;
 			}
 		});
 		final WritableList options = new WritableList(Arrays.asList(persons), Person.class);
 		final WritableValue selection = new WritableValue(persons[0], Person.class);
 		ridget.bindToModel(options, Person.class, null, selection);
 		ridget.updateFromModel();
 
 		assertEquals("AE", getItem(control, 0));
 		assertEquals("LD", getItem(control, 1));
 		assertEquals("MC", getItem(control, 2));
 
 		assertEquals(persons[0], ridget.getSelection());
 
 		select(control, 2); // select "MC"
 
 		assertEquals(persons[2], ridget.getSelection()); // result: Marie Curie
 	}
 
 	public void testAddSelectionListener() throws InterruptedException {
 		final IComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		final StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		try {
 			ridget.addSelectionListener(null);
 			fail();
 		} catch (final RuntimeException npe) {
 			ok();
 		}
 
 		final TestSelectionListener selectionListener = new TestSelectionListener();
 
 		ridget.addSelectionListener(selectionListener);
 		assertEquals(null, ridget.getSelection());
 		assertEquals(-1, getSelectionIndex(control));
 
 		control.setFocus();
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
 		UITestHelper.readAndDispatch(control);
 
 		assertNotNull(ridget.getSelection());
 		assertEquals(0, getSelectionIndex(control));
 		assertEquals(1, selectionListener.getCount());
 		final SelectionEvent selectionEvent = selectionListener.getSelectionEvent();
 		assertEquals(ridget, selectionEvent.getSource());
 		assertTrue(selectionEvent.getOldSelection().isEmpty());
 		assertEquals(ridget.getSelection(), selectionEvent.getNewSelection().get(0));
 
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
 		UITestHelper.readAndDispatch(control);
 
 		assertNotNull(ridget.getSelection());
 		assertEquals(1, getSelectionIndex(control));
 		assertEquals(2, selectionListener.getCount());
 		final SelectionEvent selectionEvent2 = selectionListener.getSelectionEvent();
 		assertEquals(ridget, selectionEvent.getSource());
 		assertEquals(selectionEvent.getNewSelection(), selectionEvent2.getOldSelection());
 		assertEquals(ridget.getSelection(), selectionEvent2.getNewSelection().get(0));
 
 		ridget.removeSelectionListener(selectionListener);
 
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
 		UITestHelper.readAndDispatch(control);
 
 		assertEquals(2, selectionListener.getCount()); // no inc -> lsnr removed
 	}
 
 	/**
 	 * As per Bug 293000
 	 */
 	public void testSelectionListenerWithObjectsOtherThanString() {
 		final AbstractComboRidget ridget = getRidget();
 		final WritableList options = new WritableList();
 		options.add(selection1);
 		options.add(selection2);
 		final WritableValue selection = new WritableValue(null, Person.class);
 		ridget.bindToModel(options, Person.class, null, selection);
 		ridget.updateFromModel();
 
 		final FTValueChangeListener valueListener = new FTValueChangeListener();
 		selection.addValueChangeListener(valueListener);
 
 		final FTSelectionListener selectionListener = new FTSelectionListener();
 		ridget.addSelectionListener(selectionListener);
 
 		assertEquals(0, valueListener.getCount());
 		assertEquals(0, selectionListener.getCount());
 
 		ridget.setSelection(selection1);
 
 		assertEquals(1, valueListener.getCount());
 		assertEquals(selection1, selection.getValue());
 		assertEquals(1, selectionListener.getCount());
 		assertEquals(selection1, ridget.getSelection());
 	}
 
 	/**
 	 * As per Bug 292679.
 	 */
 	public void testChangeSelectionViaAPIWhenRidgetIsOutputOnly() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		final WritableList options = new WritableList();
 		options.add(selection1);
 		options.add(selection2);
 		final WritableValue selection = new WritableValue(null, Person.class);
 		ridget.setUIControl(control);
 		ridget.bindToModel(options, Person.class, null, selection);
 		ridget.updateFromModel();
 
 		ridget.setOutputOnly(true);
 
 		assertEquals("", getText(control));
 		assertEquals(null, ridget.getSelection());
 		assertEquals(null, selection.getValue());
 
 		ridget.setSelection(selection1);
 
 		assertEquals(selection1.toString(), getText(control));
 		assertEquals(selection1, ridget.getSelection());
 		assertEquals(selection1, selection.getValue());
 	}
 
 	public void testChangeSelectionViaBeanWhenRidgetIsOutputOnly() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		final WritableList options = new WritableList();
 		options.add(selection1);
 		options.add(selection2);
 		final WritableValue selection = new WritableValue(null, Person.class);
 		ridget.setUIControl(control);
 		ridget.bindToModel(options, Person.class, null, selection);
 		ridget.updateFromModel();
 
 		ridget.setOutputOnly(true);
 
 		assertEquals("", getText(control));
 		assertEquals(null, ridget.getSelection());
 		assertEquals(null, selection.getValue());
 
 		selection.setValue(selection1);
 		ridget.updateFromModel();
 
 		assertEquals(selection1.toString(), getText(control));
 		assertEquals(selection1, ridget.getSelection());
 		assertEquals(selection1, selection.getValue());
 	}
 
 	/**
 	 * As per Bug 304733
 	 */
 	public void testSetMarkSelectionMismatch() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		ridget.bindToModel(manager, "persons", Person.class, null, manager, "selectedPerson");
 		ridget.updateFromModel();
 
 		// select and then remove 'selection2'
 		ridget.setSelection(selection2);
 		manager.getPersons().remove(selection2);
 		ridget.updateFromModel();
 
 		assertEquals(null, getSelectedString(control));
 		assertEquals(selection2, ridget.getSelection());
 		assertEquals(selection2, manager.getSelectedPerson());
 
 		assertFalse(ridget.isMarkSelectionMismatch());
 		assertFalse(ridget.isErrorMarked());
 
 		ridget.setMarkSelectionMismatch(true);
 
 		assertTrue(ridget.isMarkSelectionMismatch());
 		assertTrue(ridget.isErrorMarked());
 
 		ridget.setMarkSelectionMismatch(false);
 
 		assertFalse(ridget.isMarkSelectionMismatch());
 		assertFalse(ridget.isErrorMarked());
 	}
 
 	/**
 	 * As per Bug 304733
 	 */
 	public void testHideSelectionMismatchViaSetSelection() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		ridget.bindToModel(manager, "persons", Person.class, null, manager, "selectedPerson");
 		ridget.updateFromModel();
 
 		ridget.setMarkSelectionMismatch(true);
 		ridget.setSelection(selection2);
 
 		assertEquals(selection2.toString(), getSelectedString(control));
 		assertSame(selection2, ridget.getSelection());
 		assertSame(selection2, manager.getSelectedPerson());
 		assertFalse(ridget.isErrorMarked());
 
 		manager.getPersons().remove(selection2);
 		ridget.updateFromModel();
 
 		assertEquals(null, getSelectedString(control));
 		assertEquals(selection2, ridget.getSelection());
 		assertEquals(selection2, manager.getSelectedPerson());
 		assertTrue(ridget.isErrorMarked());
 
 		// remove error marker on valid selection via java API
 		ridget.setSelection(selection1);
 
 		assertEquals(selection1.toString(), getSelectedString(control));
 		assertEquals(selection1, ridget.getSelection());
 		assertEquals(selection1, manager.getSelectedPerson());
 		assertFalse(ridget.isErrorMarked());
 	}
 
 	/**
 	 * As per Bug 304733
 	 */
 	public void testHideSelectionMismatchViaWidgetSelection() {
 		final AbstractComboRidget ridget = getRidget();
 		final Control control = getWidget();
 		ridget.bindToModel(manager, "persons", Person.class, null, manager, "selectedPerson");
 		ridget.updateFromModel();
 
 		ridget.setMarkSelectionMismatch(true);
 		ridget.setSelection(selection2);
 
 		assertEquals(selection2.toString(), getSelectedString(control));
 		assertSame(selection2, ridget.getSelection());
 		assertSame(selection2, manager.getSelectedPerson());
 		assertFalse(ridget.isErrorMarked());
 
 		manager.getPersons().remove(selection2);
 		ridget.updateFromModel();
 
 		assertEquals(null, getSelectedString(control));
 		assertEquals(selection2, ridget.getSelection());
 		assertEquals(selection2, manager.getSelectedPerson());
 		assertTrue(ridget.isErrorMarked());
 
 		// remove error marker on valid selection via java API
 		select(control, 0);
 
 		assertEquals(selection1.toString(), getSelectedString(control));
 		assertEquals(selection1, ridget.getSelection());
 		assertEquals(selection1, manager.getSelectedPerson());
 		assertFalse(ridget.isErrorMarked());
 	}
 
 	/**
 	 * As per Bug 307592
 	 */
 	public void testUpdateMandatoryMarkerOnUpdateFromModelWithPojo() {
 		final AbstractComboRidget ridget = getRidget();
 		ridget.setMandatory(true);
 
 		assertMandatory(ridget, 1, false);
 
 		ridget.addSelectionListener(new ISelectionListener() {
 			public void ridgetSelected(final SelectionEvent event) {
 				if (event.getNewSelection().isEmpty()) {
 					throw new NullPointerException("im a bad listener");
 				}
 			}
 		});
 
 		final StringPojo selection = new StringPojo("a");
 		final WritableList values = new WritableList(Arrays.asList("a", "b", "c"), String.class);
 		ridget.bindToModel(values, String.class, null, PojoObservables.observeValue(selection, "value"));
 		ridget.updateFromModel();
 
 		assertMandatory(ridget, 1, true);
 
 		selection.setValue(null);
 
 		assertMandatory(ridget, 1, true);
 
 		ridget.updateFromModel();
 
 		assertMandatory(ridget, 1, false);
 	}
 
 	public void testSetOutputOnly() {
 		final IMarkableRidget ridget = (IMarkableRidget) createRidget();
 		final Control control = (Control) createWidget(getShell());
 
 		ridget.setOutputOnly(true);
 
 		assertTrue(ridget.isOutputOnly());
 		assertTrue(control.isEnabled());
 
 		ridget.setUIControl(control);
 		ridget.addMarker(new AbstractMarker() {
 		});
 
 		assertTrue(ridget.isOutputOnly());
 		if (control instanceof CompletionCombo) {
 			final CompletionCombo completionCombo = (CompletionCombo) control;
 			assertTrue(completionCombo.isEnabled());
 			assertFalse(completionCombo.getEditable());
 		} else {
 			//			assertFalse(control.isEnabled());
 		}
 	}
 
 	// helping methods
 	// ////////////////
 
 	private void assertMandatory(final IMarkableRidget ridget, final int count, final boolean isDisabled) {
 		final Collection<MandatoryMarker> markers = ridget.getMarkersOfType(MandatoryMarker.class);
 		assertEquals(count, markers.size());
 		for (final MandatoryMarker marker : markers) {
 			assertEquals(isDisabled, marker.isDisabled());
 		}
 	}
 
 	private void assertControlEditable(final Control control, final boolean editable) {
 		if (control instanceof CompletionCombo) {
 			assertEquals(editable, ((CompletionCombo) control).getEditable());
 			assertTrue(control.isEnabled());
 		} else {
 			assertTrue(control.isEnabled());
 		}
 	}
 
 	private void checkPersonList(final PersonManager manager) {
 		final Control control = getWidget();
 
 		assertEquals(manager.getPersons().size(), getItemCount(control));
 
 		final Collection<String> listEntries = new ArrayList<String>();
 		for (final Person person : manager.getPersons()) {
 			listEntries.add(person.getListEntry());
 		}
 		for (int i = 0; i < getItemCount(control); i++) {
 			final String item = getItem(control, i);
 			if (!listEntries.contains(item)) {
 				fail();
 			}
 		}
 	}
 
 	private void checkSelection(final AbstractComboRidget ridget, final Control control, final boolean isEnabled) {
 		assertEquals("A", ridget.getSelection());
 		assertEquals(0, ridget.getSelectionIndex());
 		if (isEnabled) {
 			assertEquals("A", getText(control));
 			assertEquals(0, getSelectionIndex(control));
 		} else {
 			assertEquals("", getText(control));
 			assertEquals(-1, getSelectionIndex(control));
 		}
 	}
 
 	private Collection<Person> createPersonList() {
 		final Collection<Person> newList = new ArrayList<Person>();
 
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
 
 	private boolean find(final PersonManager manager, final String item) {
 		boolean result = false;
 		final Iterator<Person> iter = manager.getPersons().iterator();
 		while (!result && iter.hasNext()) {
 			result = iter.next().toString().equals(item);
 		}
 		return result;
 	}
 
 	private String getInitials(final Person person) {
 		if (person == null) {
 			return null;
 		}
 		final String first = person.getFirstname();
 		final String last = person.getLastname();
 		final String f = first.length() > 0 ? String.valueOf(first.charAt(0)) : "?";
 		final String l = last.length() > 0 ? String.valueOf(last.charAt(0)) : "?";
 		return f + l;
 	}
 
 	private String getItem(final Control control, final int index) {
 		if (control instanceof Combo) {
 			return ((Combo) control).getItem(index);
 		}
 		if (control instanceof CCombo) {
 			return ((CCombo) control).getItem(index);
 		}
 		if (control instanceof CompletionCombo) {
 			return ((CompletionCombo) control).getItem(index);
 		}
 		throw new IllegalArgumentException("unknown widget type: " + control);
 	}
 
 	private int getItemCount(final Control control) {
 		if (control instanceof Combo) {
 			return ((Combo) control).getItemCount();
 		}
 		if (control instanceof CCombo) {
 			return ((CCombo) control).getItemCount();
 		}
 		if (control instanceof CompletionCombo) {
 			return ((CompletionCombo) control).getItemCount();
 		}
 		throw new IllegalArgumentException("unknown widget type: " + control);
 	}
 
 	private int getSelectionIndex(final Control control) {
 		if (control instanceof Combo) {
 			return ((Combo) control).getSelectionIndex();
 		}
 		if (control instanceof CCombo) {
 			return ((CCombo) control).getSelectionIndex();
 		}
 		if (control instanceof CompletionCombo) {
 			return ((CompletionCombo) control).getSelectionIndex();
 		}
 		throw new IllegalArgumentException("unknown widget type: " + control);
 	}
 
 	private String getSelectedString(final Control control) {
 		final int index = getSelectionIndex(control);
 		return index == -1 ? null : getItem(control, index);
 	}
 
 	private String getText(final Control control) {
 		if (control instanceof Combo) {
 			return ((Combo) control).getText();
 		}
 		if (control instanceof CCombo) {
 			return ((CCombo) control).getText();
 		}
 		if (control instanceof CompletionCombo) {
 			return ((CompletionCombo) control).getText();
 		}
 		throw new IllegalArgumentException("unknown widget type: " + control);
 	}
 
 	private void select(final Control control, final int index) {
 		if (control instanceof Combo) {
 			((Combo) control).select(index);
 		} else if (control instanceof CCombo) {
 			((CCombo) control).select(index);
 		} else if (control instanceof CompletionCombo) {
 			((CompletionCombo) control).select(index);
 		} else {
 			throw new IllegalArgumentException("unknown widget type: " + control);
 		}
 	}
 
 	private void selectA(final Control control) {
 		final Display display = control.getDisplay();
 		if (control instanceof Combo) {
 			UITestHelper.sendString(display, "A");
 		} else if (control instanceof CCombo) {
 			UITestHelper.sendKeyAction(display, UITestHelper.KC_ARROW_DOWN);
 		} else if (control instanceof CompletionCombo) {
 			UITestHelper.sendString(display, "A");
 		} else {
 			throw new IllegalArgumentException("unknown widget type: " + control);
 		}
 		UITestHelper.readAndDispatch(control);
 	}
 
 	// helping classes
 	//////////////////
 
 	private final class SelectionPropertyChangeEvent extends PropertyChangeEvent {
 
 		private static final long serialVersionUID = 4711L;
 
 		public SelectionPropertyChangeEvent(final Object oldValue, final Object newValue) {
 			super(getRidget(), IComboRidget.PROPERTY_SELECTION, oldValue, newValue);
 		}
 	}
 
 	private final class TextPropertyChangeEvent extends PropertyChangeEvent {
 
 		private static final long serialVersionUID = 4711L;
 
 		public TextPropertyChangeEvent(final Object oldValue, final Object newValue) {
 			super(getRidget(), IComboRidget.PROPERTY_TEXT, ((Person) oldValue).getListEntry(), ((Person) newValue)
 					.getListEntry());
 		}
 	}
 
 	private static final class FTPropertyChangeListener implements PropertyChangeListener {
 
 		private int count;
 		private Runnable runnable;
 
 		public void propertyChange(final PropertyChangeEvent evt) {
 			count++;
 			if (runnable != null) {
 				runnable.run();
 			}
 		}
 
 		int getCount() {
 			return count;
 		}
 
 		void setRunnable(final Runnable runnable) {
 			this.runnable = runnable;
 		}
 	}
 
 	private static class FTValueChangeListener implements IValueChangeListener {
 
 		private int count;
 
 		public void handleValueChange(final ValueChangeEvent event) {
 			count++;
 		}
 
 		int getCount() {
 			return count;
 		}
 	}
 
 	private static class FTSelectionListener implements ISelectionListener {
 
 		private int count;
 
 		public void ridgetSelected(final SelectionEvent event) {
 			count++;
 		}
 
 		int getCount() {
 			return count;
 		}
 	}
 
 	private static final class ProductHolder {
 
 		private List<Product> products;
 		private Product selectedProducts;
 
 		public void setProducts(final List<Product> policenProdukte) {
 			this.products = policenProdukte;
 		}
 
 		@SuppressWarnings("unused")
 		public List<Product> getProducts() {
 			return products;
 		}
 
 		@SuppressWarnings("unused")
 		public void setSelectedProducts(final Product selectedPolicenProdukt) {
 			this.selectedProducts = selectedPolicenProdukt;
 		}
 
 		@SuppressWarnings("unused")
 		public Product getSelectedProducts() {
 			return selectedProducts;
 		}
 	}
 
 	private static final class Product {
 
 		private final String name;
 
 		public Product(final String name) {
 			this.name = name;
 		}
 
 		@Override
 		public String toString() {
 			return name;
 		}
 	}
 
 	private static final class StringManagerWithUpdateFromModel extends StringManager {
 
 		private final IRidget ridget;
 
 		public StringManagerWithUpdateFromModel(final IRidget ridget, final String... items) {
 			super(items);
 			this.ridget = ridget;
 		}
 
 		@Override
 		public void setSelectedItem(final String selectedItem) {
 			super.setSelectedItem(selectedItem);
 			ridget.updateFromModel();
 		}
 	}
 }
