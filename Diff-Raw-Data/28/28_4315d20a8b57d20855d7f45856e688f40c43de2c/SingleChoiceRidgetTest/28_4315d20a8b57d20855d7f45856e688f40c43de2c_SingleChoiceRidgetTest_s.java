 /*******************************************************************************
  * Copyright (c) 2007, 2013 compeople AG and others.
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
 import java.util.List;
 
 import org.eclipse.core.databinding.beans.PojoObservables;
 import org.eclipse.core.databinding.observable.Realm;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Shell;
 
 import org.eclipse.riena.beans.common.Person;
 import org.eclipse.riena.internal.ui.swt.test.UITestHelper;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.ISingleChoiceRidget;
 import org.eclipse.riena.ui.ridgets.IToggleButtonRidget;
 import org.eclipse.riena.ui.ridgets.swt.MarkerSupport;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper;
 import org.eclipse.riena.ui.swt.ChoiceComposite;
 
 /**
  * Tests for the class {@link SingleChoiceRidget}.
  */
 public final class SingleChoiceRidgetTest extends MarkableRidgetTest {
 
 	private OptionProvider optionProvider;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		optionProvider = new OptionProvider();
 
 		getRidget().bindToModel(optionProvider, "options", optionProvider, "selectedOption");
 		getRidget().updateFromModel();
 	}
 
 	@Override
 	protected Control createWidget(final Composite parent) {
 		return new ChoiceComposite(parent, SWT.NONE, false);
 	}
 
 	@Override
 	protected IRidget createRidget() {
 		return new SingleChoiceRidget();
 	}
 
 	@Override
 	protected ChoiceComposite getWidget() {
 		return (ChoiceComposite) super.getWidget();
 	}
 
 	@Override
 	protected ISingleChoiceRidget getRidget() {
 		return (ISingleChoiceRidget) super.getRidget();
 	}
 
 	// testing methods
 	// ////////////////
 
 	public void testHasFocus() throws Exception {
 		final ISingleChoiceRidget choiceRidget = getRidget();
 		assertNotNull(choiceRidget);
 
 		final ChoiceComposite widget = getWidget();
 		assertNotNull(widget);
 
 		assertFalse(choiceRidget.hasFocus());
 		getRidget().setUIControl(widget);
 		widget.getChildren()[0].setFocus();
 		assertTrue(getRidget().hasFocus());
 	}
 
 	/**
 	 * Test that the control is mapped to the expected ridget.
 	 */
 	public void testRidgetMapping() {
 		final SwtControlRidgetMapper mapper = SwtControlRidgetMapper.getInstance();
 		assertSame(SingleChoiceRidget.class, mapper.getRidgetClass(getWidget()));
 	}
 
 	/**
 	 * Test method getObservableList().
 	 */
 	public void testGetObservableList() {
 		final ISingleChoiceRidget ridget = getRidget();
 
 		assertNotNull(ridget.getObservableList());
 	}
 
 	/**
 	 * Test method updateFromModel().
 	 */
 	public void testUpdateFromModel() {
 		final ISingleChoiceRidget ridget = getRidget();
 		final ChoiceComposite control = getWidget();
 
 		ridget.updateFromModel();
 
 		final int oldSize = optionProvider.getOptions().size();
 		assertEquals(oldSize, ridget.getObservableList().size());
 		assertTrue(ridget.getObservableList().containsAll(optionProvider.getOptions()));
 		assertEquals(optionProvider.getSelectedOption(), ridget.getSelection());
 		assertEquals(optionProvider.getSelectedOption(), getSelectedControlValue(control));
 
 		optionProvider.remove(0);
 
 		assertEquals(oldSize, ridget.getObservableList().size());
 
 		ridget.updateFromModel();
 
 		assertEquals(oldSize - 1, ridget.getObservableList().size());
 	}
 
 	/**
 	 * Test method getUIControl().
 	 */
 	public void testGetUIControl() {
 		final ISingleChoiceRidget ridget = getRidget();
 		final Control control = getWidget();
 
 		assertEquals(control, ridget.getUIControl());
 	}
 
 	/**
 	 * Test method setSelection().
 	 */
 	public void testSetSelection() {
 		final ISingleChoiceRidget ridget = getRidget();
 
 		assertEquals(optionProvider.getSelectedOption(), ridget.getSelection());
 
 		ridget.setSelection(optionProvider.getOptions().get(1));
 
 		assertEquals(ridget.getSelection(), optionProvider.getSelectedOption());
 
 		optionProvider.setSelectedOption(optionProvider.getOptions().get(0));
 
 		assertEquals(ridget.getSelection(), optionProvider.getOptions().get(1));
 
 		ridget.updateFromModel();
 
 		assertEquals(ridget.getSelection(), optionProvider.getOptions().get(0));
 	}
 
 	/**
 	 * Test that ridget.setSelection() affects the model and the widget.
 	 */
 	public void testUpdateSelectionFromRidget() {
 		final ISingleChoiceRidget ridget = getRidget();
 		final ChoiceComposite control = getWidget();
 
 		assertNotNull(ridget.getSelection());
 
 		final Button selected1 = getSelectedControl(control);
 
 		assertEquals(optionProvider.getSelectedOption(), selected1.getText());
 		assertSame(optionProvider.getSelectedOption(), selected1.getData());
 
 		final Object option2 = optionProvider.getOptions().get(1);
 		ridget.setSelection(option2);
 
 		final Button selected2 = getSelectedControl(control);
 
 		assertNotSame(selected1, selected2);
 		assertEquals(optionProvider.getSelectedOption(), selected2.getText());
 		assertSame(optionProvider.getSelectedOption(), selected2.getData());
 	}
 
 	/**
 	 * As per Bug 304733
 	 */
 	public void testClearSelectionWhenSelectionIsRemovedFromModel() {
 		final ISingleChoiceRidget ridget = getRidget();
 
 		final String optionA = optionProvider.getOptions().get(0);
 		ridget.setSelection(optionA);
 
 		assertSame(optionA, ridget.getSelection());
 		assertSame(optionA, optionProvider.getSelectedOption());
 
 		optionProvider.getOptions().remove(0);
 		ridget.updateFromModel();
 
 		assertEquals(null, ridget.getSelection());
 		assertEquals(null, optionProvider.getSelectedOption());
 	}
 
 	/**
 	 * Test that control.setSelection() affects the ridget and the widget.
 	 */
 	public void testUpdateSelectionFromControl() {
 		final ISingleChoiceRidget ridget = getRidget();
 		final ChoiceComposite control = getWidget();
 		final Button button0 = (Button) control.getChildrenButtons()[0];
 		final Button button1 = (Button) control.getChildrenButtons()[1];
 
 		assertTrue(button0.getSelection());
 		assertFalse(button1.getSelection());
 
 		button1.setSelection(true);
 		final Event event = new Event();
 		event.type = SWT.Selection;
 		event.widget = button1;
 		button1.notifyListeners(SWT.Selection, event);
 
 		assertFalse(button0.getSelection());
 		assertTrue(button1.getSelection());
 		assertEquals(optionProvider.getOptions().get(1), ridget.getSelection());
 	}
 
 	/**
 	 * Tests that colors from the ChoiceComposite are applied to children.
 	 */
 	public void testColorsAreAppliedToChildren() {
 		final Shell shell = getShell();
 		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);
 		final Color colorRed = shell.getDisplay().getSystemColor(SWT.COLOR_RED);
 		final Color colorGreen = shell.getDisplay().getSystemColor(SWT.COLOR_GREEN);
 		control.setForeground(colorGreen);
 		control.setBackground(colorRed);
 		getRidget().setUIControl(control);
 
 		final Button selected = getSelectedControl(control);
 
 		assertEquals(colorGreen, selected.getForeground());
 		assertEquals(colorRed, selected.getBackground());
 	}
 
 	/**
 	 * Tests that enablement from the ChoiceComposite is applied to children.
 	 */
 	public void testEnablementIsAppliedToChildren() {
 		final Shell shell = getShell();
 		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);
 
 		assertTrue(control.isEnabled());
 
 		getRidget().setEnabled(false);
 		getRidget().setUIControl(control);
 
 		assertFalse(control.isEnabled());
 		assertTrue(control.getChildren().length > 0);
 		for (final Control child : control.getChildrenButtons()) {
 			assertFalse(((Button) child).isEnabled());
 		}
 	}
 
 	/**
 	 * Test the methods addPropertyChangeListener() and removePropertyChangeListener().
 	 */
 	public void testAddRemovePropertyChangeListener() {
 		final ISingleChoiceRidget ridget = getRidget();
 
 		final TestPropertyChangeListener listener = new TestPropertyChangeListener();
 		ridget.updateFromModel();
 		ridget.addPropertyChangeListener(listener);
 
 		assertEquals(optionProvider.getSelectedOption(), ridget.getSelection());
 
 		ridget.setSelection(optionProvider.getOptions().get(1));
 
 		assertEquals(ridget.getSelection(), optionProvider.getSelectedOption());
 		assertEquals(1, listener.eventCounter);
 
 		ridget.removePropertyChangeListener(listener);
 
 		ridget.setSelection(optionProvider.getOptions().get(0));
 
 		assertEquals(ridget.getSelection(), optionProvider.getSelectedOption());
 		assertEquals(1, listener.eventCounter);
 	}
 
 	/**
 	 * Test method bindToModel() using labels.
 	 */
 	public void testBindToModelUsingLabels() {
 		final ISingleChoiceRidget ridget = getRidget();
 		final ChoiceComposite control = getWidget();
 		optionProvider = new OptionProvider();
 
 		ridget.bindToModel(optionProvider.getOptions(), optionProvider.getOptionLabels(), optionProvider, "selectedOption");
 		ridget.updateFromModel();
 
 		final Object[] labels = optionProvider.getOptionLabels().toArray();
 		final Control[] children = control.getChildrenButtons();
 		assertEquals(labels.length, children.length);
 		for (int i = 0; i < labels.length; i++) {
 			final String label = (String) labels[i];
 			final String caption = ((Button) children[i]).getText();
 			assertEquals(label, caption);
 		}
 	}
 
 	/**
 	 * Tests that the mandatory marker gets disabled when we have a selection.
 	 */
 	public void testDisableMandatoryMarkers() {
 		final ISingleChoiceRidget ridget = getRidget();
 
 		final MandatoryMarker mandatoryMarker = new MandatoryMarker();
 		optionProvider.setSelectedOption(null);
 		ridget.updateFromModel();
 		ridget.addMarker(mandatoryMarker);
 
 		assertFalse(mandatoryMarker.isDisabled());
 
 		ridget.setSelection(optionProvider.getOptions().get(1));
 
 		assertTrue(mandatoryMarker.isDisabled());
 
 		ridget.setSelection(null);
 
 		assertFalse(mandatoryMarker.isDisabled());
 	}
 
 	/**
 	 * Tests that the isDisabledMandatoryMarker true when we have a selection.
 	 */
 	@Override
 	public void testIsDisableMandatoryMarker() {
 		final ISingleChoiceRidget ridget = getRidget();
 
 		optionProvider.setSelectedOption(null);
 		ridget.updateFromModel();
 
 		assertFalse(ridget.isDisableMandatoryMarker());
 
 		ridget.setSelection(optionProvider.getOptions().get(1));
 
 		assertTrue(ridget.isDisableMandatoryMarker());
 
 		ridget.setSelection(null);
 
 		assertFalse(ridget.isDisableMandatoryMarker());
 	}
 
 	/**
 	 * Test validation of the bindToModel(...) method.
 	 */
 	public void testBindToModelWithObservables() {
 		final ISingleChoiceRidget ridget = getRidget();
 
 		try {
 			ridget.bindToModel(null, PojoObservables.observeValue(optionProvider, "selectedOption"));
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 		try {
 			ridget.bindToModel(PojoObservables.observeList(Realm.getDefault(), optionProvider, "options"), null);
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 	}
 
 	/**
 	 * Test validation of the bindToModel(...) method.
 	 */
 	public void testBindToModelWithBeans() {
 		final ISingleChoiceRidget ridget = getRidget();
 
 		try {
 			ridget.bindToModel(null, "options", optionProvider, "selectedOption");
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 		try {
 			ridget.bindToModel(optionProvider, null, optionProvider, "selectedOption");
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 		try {
 			ridget.bindToModel(optionProvider, "options", null, "selectedOption");
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 		try {
 			ridget.bindToModel(optionProvider, "options", optionProvider, null);
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 	}
 
 	/**
 	 * Test validation of the bindToModel(...) method.
 	 */
 	public void testBindToModelWithOptionLabelList() {
 		final ISingleChoiceRidget ridget = getRidget();
 
 		try {
 			ridget.bindToModel(null, optionProvider.getOptionLabels(), optionProvider, "selectedOption");
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 		try {
 			ridget.bindToModel(optionProvider.getOptions(), (List<String>) null, optionProvider, "selectedOption");
 		} catch (final RuntimeException rex) {
 			fail();
 		}
 		try {
 			ridget.bindToModel(optionProvider.getOptions(), new ArrayList<String>(), optionProvider, "selectedOption");
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 		try {
 			ridget.bindToModel(optionProvider.getOptions(), optionProvider.getOptionLabels(), null, "selectedOption");
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 		try {
 			ridget.bindToModel(optionProvider.getOptions(), optionProvider.getOptionLabels(), optionProvider, null);
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 	}
 
 	public void testOutputCannotBeChangedFromUI() {
 		final ISingleChoiceRidget ridget = getRidget();
 		final Button button1 = (Button) getWidget().getChildrenButtons()[0];
 		final Button button2 = (Button) getWidget().getChildrenButtons()[1];
 
 		assertTrue(button1.getSelection());
 		assertFalse(button2.getSelection());
 		assertEquals("Option A", ridget.getSelection());
 
 		ridget.setOutputOnly(true);
 		button2.setFocus();
 		UITestHelper.sendString(button2.getDisplay(), " ");
 
 		assertTrue(button1.getSelection());
 		assertFalse(button2.getSelection());
 		assertEquals("Option A", ridget.getSelection());
 
 		ridget.setOutputOnly(false);
 		button2.setFocus();
 		UITestHelper.sendString(button2.getDisplay(), " ");
 
 		assertFalse(button1.getSelection());
 		assertTrue(button2.getSelection());
 		assertEquals("Option B", ridget.getSelection());
 	}
 
 	/**
 	 * Tests that changing the selected state via {@link IToggleButtonRidget#setSelected(boolean) does not select the control, when the ridget is disabled.
 	 */
 	public void testDisabledRidgetDoesNotCheckControlOnRidgetSelection() {
 		final ISingleChoiceRidget ridget = getRidget();
 		final ChoiceComposite control = getWidget();
 
 		ridget.setSelection("Option A");
 
 		assertEquals("Option A", optionProvider.getSelectedOption());
 		assertEquals("Option A", ridget.getSelection());
 		assertEquals("Option A", getSelectedControlValue(control));
 
 		ridget.setEnabled(false);
 
 		assertEquals("Option A", optionProvider.getSelectedOption());
 		assertEquals("Option A", ridget.getSelection());
 		final String expectedA = MarkerSupport.isHideDisabledRidgetContent() ? null : "Option A";
 		assertEquals(expectedA, getSelectedControlValue(control));
 
 		ridget.setSelection("Option B");
 
 		assertEquals("Option B", optionProvider.getSelectedOption());
 		assertEquals("Option B", ridget.getSelection());
 		final String expectedB = MarkerSupport.isHideDisabledRidgetContent() ? null : "Option B";
 		assertEquals(expectedB, getSelectedControlValue(control));
 
 		ridget.setEnabled(true);
 
 		assertEquals("Option B", optionProvider.getSelectedOption());
 		assertEquals("Option B", ridget.getSelection());
 		assertEquals("Option B", getSelectedControlValue(control));
 	}
 
 	/**
 	 * Tests that changing the selected state via a bound model, does not select the control, when the ridget is disabled.
 	 */
 	public void testDisabledRidgetDoesNotCheckControlOnModelSelection() {
 		final ISingleChoiceRidget ridget = getRidget();
 		final ChoiceComposite control = getWidget();
 
 		ridget.setEnabled(false);
 		optionProvider.setSelectedOption("Option A");
 		ridget.updateFromModel();
 
 		assertEquals("Option A", optionProvider.getSelectedOption());
 		assertEquals("Option A", ridget.getSelection());
 		final String expectedA = MarkerSupport.isHideDisabledRidgetContent() ? null : "Option A";
 		assertEquals(expectedA, getSelectedControlValue(control));
 
 		optionProvider.setSelectedOption("Option B");
 		ridget.updateFromModel();
 
 		assertEquals("Option B", optionProvider.getSelectedOption());
 		assertEquals("Option B", ridget.getSelection());
 		final String expectedB = MarkerSupport.isHideDisabledRidgetContent() ? null : "Option B";
 		assertEquals(expectedB, getSelectedControlValue(control));
 
 		ridget.setEnabled(true);
 
 		assertEquals("Option B", optionProvider.getSelectedOption());
 		assertEquals("Option B", ridget.getSelection());
 		assertEquals("Option B", getSelectedControlValue(control));
 	}
 
 	/**
 	 * Tests that disabling the ridget does not fire 'selected' events, even though the control is modified.
 	 */
 	public void testDisabledDoesNotFireSelected() {
 		final ISingleChoiceRidget ridget = getRidget();
 		ridget.setEnabled(true);
 		ridget.setSelection("Option A");
 
 		ridget.addPropertyChangeListener(ISingleChoiceRidget.PROPERTY_SELECTION, new PropertyChangeListener() {
 			public void propertyChange(final PropertyChangeEvent evt) {
 				fail("Unexpected property change event: " + evt);
 			}
 		});
 
 		ridget.setEnabled(false);
 
 		ridget.setEnabled(true);
 	}
 
 	/**
 	 * Tests that the disabled state is applied to a new control when set into the ridget.
 	 */
 	public void testDisableAndClearOnBind() {
 		final ISingleChoiceRidget ridget = getRidget();
 		final ChoiceComposite control = getWidget();
 
 		ridget.setUIControl(null);
 		ridget.setEnabled(false);
 		ridget.setSelection("Option B");
 		ridget.setUIControl(control);
 
 		assertFalse(control.isEnabled());
 		final String expectedB = MarkerSupport.isHideDisabledRidgetContent() ? null : "Option B";
 		assertEquals(expectedB, getSelectedControlValue(control));
 
 		ridget.setEnabled(true);
 
 		assertTrue(control.isEnabled());
 		assertEquals("Option B", getSelectedControlValue(control));
 	}
 
 	/**
 	 * Test that update from model uses equals() instead of comparing references.
 	 */
 	public void testBug255465() {
 		final ISingleChoiceRidget ridget = getRidget();
 		final ChoiceComposite control = getWidget();
 
 		final List<String> values = Arrays.asList("male", "female");
 		final List<String> labels = Arrays.asList("Man", "Woman");
 		final Person personEntity = new Person("Max", "Mustermann");
 		personEntity.setGender("male");
 		ridget.bindToModel(values, labels, personEntity, "gender");
 		ridget.updateFromModel();
 
 		assertEquals("male", getSelectedControlValue(control));
 	}
 
 	public void testAddSelectionListener() {
 		final ISingleChoiceRidget ridget = getRidget();
 
 		try {
 			ridget.addSelectionListener(null);
 			fail();
 		} catch (final RuntimeException npe) {
 			ok();
 		}
 
 		assertEquals(optionProvider.getSelectedOption(), ridget.getSelection());
 
 		final TestSelectionListener selectionListener = new TestSelectionListener();
 		ridget.addSelectionListener(selectionListener);
 
 		ridget.setSelection(optionProvider.getOptions().get(1));
 		assertEquals(ridget.getSelection(), optionProvider.getSelectedOption());
 		assertEquals(1, selectionListener.getCount());
 
 		ridget.setSelection(optionProvider.getOptions().get(2));
 		assertEquals(ridget.getSelection(), optionProvider.getSelectedOption());
 		assertEquals(2, selectionListener.getCount());
 
 		ridget.removeSelectionListener(selectionListener);
 		ridget.setSelection(null);
 		assertEquals(ridget.getSelection(), optionProvider.getSelectedOption());
 		assertEquals(2, selectionListener.getCount());
 	}
 
 	/**
 	 * As per Bug 321927
 	 */
 	public void testToggleDisabledWhenOutputOnly() {
 		final ISingleChoiceRidget ridget = getRidget();
 		final ChoiceComposite control = getWidget();
 		final Button btnFirst = (Button) control.getChildrenButtons()[0];
 		final String first = optionProvider.getOptions().get(0);
 		ridget.setSelection(first);
 
 		assertTrue(btnFirst.getSelection());
 
 		ridget.setOutputOnly(true);
 		ridget.setEnabled(false);
 		ridget.setEnabled(true);
 		for (final Control child : control.getChildrenButtons()) {
 			final Button button = (Button) child;
 			if (button == btnFirst) {
 				assertTrue(btnFirst.isEnabled());
 				assertTrue(btnFirst.getSelection());
 			} else {
 				assertFalse(button.isEnabled());
 				assertFalse(button.getSelection());
 			}
 		}
 	}
 
 	/**
 	 * As per Bug 321927 - test setSelection() and output only := true
 	 */
 	public void testSelectionWithOutputOnly() {
 		final ISingleChoiceRidget ridget = getRidget();
 		final ChoiceComposite control = getWidget();
 		final String first = optionProvider.getOptions().get(0);
 		final String second = optionProvider.getOptions().get(1);
 		ridget.setSelection(first);
 
 		assertEquals(first, ridget.getSelection());
 
 		ridget.setOutputOnly(true);
 		ridget.setSelection(second);
 
 		assertEquals(second, ridget.getSelection());
 		final Button btnSecond = (Button) control.getChildrenButtons()[1];
 		for (final Control child : control.getChildrenButtons()) {
 			final Button button = (Button) child;
 			if (button == btnSecond) {
 				assertTrue(btnSecond.isEnabled());
 				assertTrue(btnSecond.getSelection());
 			} else {
 				assertFalse(button.isEnabled());
 				assertFalse(button.getSelection());
 			}
 		}
 	}
 
 	/**
 	 * Test methods get/setEmptySelectionItem().
 	 */
 	public void testGetSetEmptySelectionItem() {
 		final ISingleChoiceRidget ridget = getRidget();
 		final String first = optionProvider.getOptions().get(0);
 		final String second = optionProvider.getOptions().get(1);
 		ridget.setEmptySelectionItem(first);
 		ridget.addMarker(new MandatoryMarker());
 
 		assertEquals(first, ridget.getSelection());
 		assertEquals(first, ridget.getEmptySelectionItem());
 		assertFalse(ridget.isDisableMandatoryMarker());
 
 		ridget.setSelection(second);
 
 		assertEquals(second, ridget.getSelection());
 		assertTrue(ridget.isDisableMandatoryMarker());
 
 		ridget.setSelection(first);
 
 		assertEquals(first, ridget.getSelection());
 		assertFalse(ridget.isDisableMandatoryMarker());
 
 		ridget.setEmptySelectionItem(null);
 
 		assertTrue(ridget.isDisableMandatoryMarker());
 
 		ridget.setEmptySelectionItem(second);
 
 		assertTrue(ridget.isDisableMandatoryMarker());
 
 		ridget.setSelection(second);
 
 		assertEquals(second, ridget.getSelection());
 		assertFalse(ridget.isDisableMandatoryMarker());
 	}
 
 	/**
 	 * Fix report ruv 375. Test method setEmptySelectionItem() with emptySelectionItem equal null.
 	 */
 	public void testEmptySelectionItemEqualsNull() {
 		final ISingleChoiceRidget ridget = getRidget();
 		final String optionA = "Option A";
 		final String optionNull = null;
 		final String optionLabelA = "Option label A";
 		final String optionLabelNull = "Option label Null";
 		getRidget().bindToModel(
 				new ArrayList<String>(Arrays.asList(optionA, "Option B", "Option C", "Option D", "Option E", "Option F", optionNull)),
 				new ArrayList<String>(Arrays.asList(optionLabelA, "Option label B", "Option label C", "Option label D", "Option label E", "Option label F",
 						optionLabelNull)), optionProvider, "selectedOption");
 		ridget.setEmptySelectionItem(optionNull);
 		optionProvider.setSelectedOption(optionNull);
 		ridget.addMarker(new MandatoryMarker());
 		getRidget().updateFromModel();
 
 		assertEquals(optionNull, optionProvider.getSelectedOption());
 		assertEquals(optionNull, ridget.getSelection());
 		assertTrue(optionLabelNull.equals(getSelectedControl(getWidget()).getText()));
 		assertFalse(ridget.isDisableMandatoryMarker());
 
 		optionProvider.setSelectedOption(optionA);
 		getRidget().updateFromModel();
 
 		assertTrue(optionA.equals(optionProvider.getSelectedOption()));
 		assertTrue(optionA.equals(ridget.getSelection()));
 		assertTrue(optionLabelA.equals(getSelectedControl(getWidget()).getText()));
 		assertTrue(ridget.isDisableMandatoryMarker());
 
 		optionProvider.setSelectedOption(optionNull);
 		getRidget().updateFromModel();
 
 		assertEquals(optionNull, optionProvider.getSelectedOption());
 		assertEquals(optionNull, ridget.getSelection());
 		assertTrue(optionLabelNull.equals(getSelectedControl(getWidget()).getText()));
 		assertFalse(ridget.isDisableMandatoryMarker());
 	}
 
 	// helping methods
 	// ////////////////
 
 	private Button getSelectedControl(final ChoiceComposite control) {
 		Button selected = null;
 		for (final Control child : control.getChildrenButtons()) {
 			if (((Button) child).getSelection()) {
 				assertNull(selected);
 				selected = (Button) child;
 			}
 		}
 		return selected;
 	}
 
 	private String getSelectedControlValue(final ChoiceComposite control) {
 		final Button button = getSelectedControl(control);
 		return button != null ? String.valueOf(button.getData()) : null;
 	}
 
 	// helping classes
 	// ////////////////
 
 	private static final class OptionProvider {
 
 		private final List<String> options;
 		private final List<String> optionLabels;
 		private String selectedOption;
 
 		private OptionProvider() {
 			this(new ArrayList<String>(Arrays.asList("Option A", "Option B", "Option C", "Option D", "Option E", "Option F")), new ArrayList<String>(
 					Arrays.asList("Option label A", "Option label B", "Option label C", "Option label D", "Option label E", "Option label F")));
 		}
 
 		private OptionProvider(final List<String> options, final List<String> optionLabels) {
 			this.options = options;
 			this.optionLabels = optionLabels;
 			selectedOption = options.get(0);
 		}
 
 		public List<String> getOptions() {
 			return options;
 		}
 
 		public String getSelectedOption() {
 			return selectedOption;
 		}
 
 		public void setSelectedOption(final String selectedOption) {
 			this.selectedOption = selectedOption;
 		}
 
 		public List<String> getOptionLabels() {
 			return optionLabels;
 		}
 
 		public void remove(final int index) {
 			options.remove(index);
 			optionLabels.remove(index);
 		}
 	}
 
 	private static final class TestPropertyChangeListener implements PropertyChangeListener {
 		private int eventCounter = 0;
 
 		public void propertyChange(final PropertyChangeEvent evt) {
 			eventCounter++;
 		}
 	};
 
 }
