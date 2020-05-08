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
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.Realm;
 import org.eclipse.riena.tests.UITestHelper;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.ISingleChoiceRidget;
 import org.eclipse.riena.ui.ridgets.IToggleButtonRidget;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.DefaultSwtControlRidgetMapper;
 import org.eclipse.riena.ui.swt.ChoiceComposite;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Shell;
 
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
 	protected Control createWidget(Composite parent) {
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
 
 	/**
 	 * Test that the control is mapped to the expected ridget.
 	 */
 	public void testRidgetMapping() {
 		DefaultSwtControlRidgetMapper mapper = new DefaultSwtControlRidgetMapper();
 		assertSame(SingleChoiceRidget.class, mapper.getRidgetClass(getWidget()));
 	}
 
 	/**
 	 * Test method getObservableList().
 	 */
 	public void testGetObservableList() {
 		ISingleChoiceRidget ridget = getRidget();
 
 		assertNotNull(ridget.getObservableList());
 	}
 
 	/**
 	 * Test method getSelectionObservable().
 	 */
 	public void testGetSelectionObservable() {
 		ISingleChoiceRidget ridget = getRidget();
 
 		assertNotNull(ridget.getSelectionObservable());
 
 		ridget.updateFromModel();
 
 		assertEquals(optionProvider.getSelectedOption(), ridget.getSelectionObservable().getValue());
 	}
 
 	/**
 	 * Test method updateFromModel().
 	 */
 	public void testUpdateFromModel() {
 		ISingleChoiceRidget ridget = getRidget();
 		ChoiceComposite control = getWidget();
 
 		ridget.updateFromModel();
 
 		assertEquals(optionProvider.getOptions().size(), ridget.getObservableList().size());
 		assertTrue(ridget.getObservableList().containsAll(optionProvider.getOptions()));
 		assertEquals(optionProvider.getSelectedOption(), ridget.getSelection());
 		assertEquals(optionProvider.getSelectedOption(), getSelectedControlValue(control));
 	}
 
 	/**
 	 * Test method getUIControl().
 	 */
 	public void testGetUIControl() {
 		ISingleChoiceRidget ridget = getRidget();
 		Control control = getWidget();
 
 		assertEquals(control, ridget.getUIControl());
 	}
 
 	/**
 	 * Test method setSelection().
 	 */
 	public void testSetSelection() {
 		ISingleChoiceRidget ridget = getRidget();
 
 		ridget.updateFromModel();
 
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
 		ISingleChoiceRidget ridget = getRidget();
 		ChoiceComposite control = getWidget();
 
 		assertNotNull(ridget.getSelection());
 
 		Button selected1 = getSelectedControl(control);
 
 		assertEquals(optionProvider.getSelectedOption(), selected1.getText());
 		assertSame(optionProvider.getSelectedOption(), selected1.getData());
 
 		Object option2 = optionProvider.getOptions().get(1);
 		ridget.setSelection(option2);
 
 		Button selected2 = getSelectedControl(control);
 
 		assertNotSame(selected1, selected2);
 		assertEquals(optionProvider.getSelectedOption(), selected2.getText());
 		assertSame(optionProvider.getSelectedOption(), selected2.getData());
 	}
 
 	/**
 	 * Test that control.setSelection() affects the ridget and the widget.
 	 */
 	public void testUpdateSelectionFromControl() {
 		ISingleChoiceRidget ridget = getRidget();
 		ChoiceComposite control = getWidget();
 		Button button0 = (Button) control.getChildren()[0];
 		Button button1 = (Button) control.getChildren()[1];
 
 		assertTrue(button0.getSelection());
 		assertFalse(button1.getSelection());
 
 		button1.setSelection(true);
 		Event event = new Event();
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
 		Shell shell = getShell();
 		ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);
 		Color colorRed = shell.getDisplay().getSystemColor(SWT.COLOR_RED);
 		Color colorGreen = shell.getDisplay().getSystemColor(SWT.COLOR_GREEN);
 		control.setForeground(colorGreen);
 		control.setBackground(colorRed);
 		getRidget().setUIControl(control);
 
 		Button selected = getSelectedControl(control);
 
 		assertEquals(colorGreen, selected.getForeground());
 		assertEquals(colorRed, selected.getBackground());
 	}
 
 	/**
 	 * Tests that enablement from the ChoiceComposite is applied to children.
 	 */
 	public void testEnablementIsAppliedToChildren() {
 		Shell shell = getShell();
 		ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);
 
 		assertTrue(control.isEnabled());
 
 		getRidget().setEnabled(false);
 		getRidget().setUIControl(control);
 
 		assertFalse(control.isEnabled());
 		assertTrue(control.getChildren().length > 0);
 		for (Control child : control.getChildren()) {
 			assertFalse(((Button) child).isEnabled());
 		}
 	}
 
 	/**
 	 * Test the methods addPropertyChangeListener() and
 	 * removePropertyChangeListener().
 	 */
 	public void testAddRemovePropertyChangeListener() {
 		ISingleChoiceRidget ridget = getRidget();
 
 		TestPropertyChangeListener listener = new TestPropertyChangeListener();
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
 		ISingleChoiceRidget ridget = getRidget();
 		Composite control = getWidget();
 		optionProvider = new OptionProvider();
 
 		ridget.bindToModel(optionProvider.getOptions(), optionProvider.getOptionLabels(), optionProvider,
 				"selectedOption");
 		ridget.updateFromModel();
 
 		Object[] labels = optionProvider.getOptionLabels().toArray();
 		Control[] children = control.getChildren();
 		assertEquals(labels.length, children.length);
 		for (int i = 0; i < labels.length; i++) {
 			String label = (String) labels[i];
 			String caption = ((Button) children[i]).getText();
 			assertEquals(label, caption);
 		}
 	}
 
 	/**
 	 * Tests that the mandatory marker gets disabled when we have a selection.
 	 */
 	public void testDisableMandatoryMarkers() {
 		ISingleChoiceRidget ridget = getRidget();
 
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
 		ISingleChoiceRidget ridget = getRidget();
 
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
 		ISingleChoiceRidget ridget = getRidget();
 
 		try {
 			ridget.bindToModel(null, BeansObservables.observeValue(optionProvider, "selectedOption"));
 			fail();
 		} catch (RuntimeException rex) {
 			// expected
 		}
 		try {
 			ridget.bindToModel(BeansObservables.observeList(Realm.getDefault(), optionProvider, "options"), null);
 			fail();
 		} catch (RuntimeException rex) {
 			// expected
 		}
 	}
 
 	/**
 	 * Test validation of the bindToModel(...) method.
 	 */
 	public void testBindToModelWithBeans() {
 		ISingleChoiceRidget ridget = getRidget();
 
 		try {
 			ridget.bindToModel(null, "options", optionProvider, "selectedOption");
 			fail();
 		} catch (RuntimeException rex) {
 			// expected
 		}
 		try {
 			ridget.bindToModel(optionProvider, null, optionProvider, "selectedOption");
 			fail();
 		} catch (RuntimeException rex) {
 			// expected
 		}
 		try {
 			ridget.bindToModel(optionProvider, "options", null, "selectedOption");
 			fail();
 		} catch (RuntimeException rex) {
 			// expected
 		}
 		try {
 			ridget.bindToModel(optionProvider, "options", optionProvider, null);
 			fail();
 		} catch (RuntimeException rex) {
 			// expected
 		}
 	}
 
 	/**
 	 * Test validation of the bindToModel(...) method.
 	 */
 	public void testBindToModelWithOptionLabelList() {
 		ISingleChoiceRidget ridget = getRidget();
 
 		try {
 			ridget.bindToModel(null, optionProvider.getOptionLabels(), optionProvider, "selectedOption");
 			fail();
 		} catch (RuntimeException rex) {
 			// expected
 		}
 		try {
 			ridget.bindToModel(optionProvider.getOptions(), (List<String>) null, optionProvider, "selectedOption");
 		} catch (RuntimeException rex) {
 			fail();
 		}
 		try {
 			ridget.bindToModel(optionProvider.getOptions(), new ArrayList<String>(), optionProvider, "selectedOption");
 			fail();
 		} catch (RuntimeException rex) {
 			// expected
 		}
 		try {
 			ridget.bindToModel(optionProvider.getOptions(), optionProvider.getOptionLabels(), null, "selectedOption");
 			fail();
 		} catch (RuntimeException rex) {
 			// expected
 		}
 		try {
 			ridget.bindToModel(optionProvider.getOptions(), optionProvider.getOptionLabels(), optionProvider, null);
 			fail();
 		} catch (RuntimeException rex) {
 			// expected
 		}
 	}
 
 	public void testOutputCannotBeChangedFromUI() {
 		ISingleChoiceRidget ridget = getRidget();
 		Button button1 = (Button) getWidget().getChildren()[0];
 		Button button2 = (Button) getWidget().getChildren()[1];
 
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
 	 * Tests that changing the selected state via
 	 * {@link IToggleButtonRidget#setSelected(boolean) does not select the
 	 * control, when the ridget is disabled.
 	 */
 	public void testDisabledRidgetDoesNotCheckControlOnRidgetSelection() {
 		ISingleChoiceRidget ridget = getRidget();
 		ChoiceComposite control = getWidget();
 
 		ridget.setSelection("Option A");
 
 		assertEquals("Option A", optionProvider.getSelectedOption());
 		assertEquals("Option A", ridget.getSelection());
 		assertEquals("Option A", getSelectedControlValue(control));
 
 		ridget.setEnabled(false);
 
 		assertEquals("Option A", optionProvider.getSelectedOption());
 		assertEquals("Option A", ridget.getSelection());
 		String expectedA = MarkerSupport.HIDE_DISABLED_RIDGET_CONTENT ? null : "Option A";
 		assertEquals(expectedA, getSelectedControlValue(control));
 
 		ridget.setSelection("Option B");
 
 		assertEquals("Option B", optionProvider.getSelectedOption());
 		assertEquals("Option B", ridget.getSelection());
 		String expectedB = MarkerSupport.HIDE_DISABLED_RIDGET_CONTENT ? null : "Option B";
 		assertEquals(expectedB, getSelectedControlValue(control));
 
 		ridget.setEnabled(true);
 
 		assertEquals("Option B", optionProvider.getSelectedOption());
 		assertEquals("Option B", ridget.getSelection());
 		assertEquals("Option B", getSelectedControlValue(control));
 	}
 
 	/**
 	 * Tests that changing the selected state via a bound model, does not select
 	 * the control, when the ridget is disabled.
 	 */
 	public void testDisabledRidgetDoesNotCheckControlOnModelSelection() {
 		ISingleChoiceRidget ridget = getRidget();
 		ChoiceComposite control = getWidget();
 
 		ridget.setEnabled(false);
 		optionProvider.setSelectedOption("Option A");
 		ridget.updateFromModel();
 
 		assertEquals("Option A", optionProvider.getSelectedOption());
 		assertEquals("Option A", ridget.getSelection());
 		String expectedA = MarkerSupport.HIDE_DISABLED_RIDGET_CONTENT ? null : "Option A";
 		assertEquals(expectedA, getSelectedControlValue(control));
 
 		optionProvider.setSelectedOption("Option B");
 		ridget.updateFromModel();
 
 		assertEquals("Option B", optionProvider.getSelectedOption());
 		assertEquals("Option B", ridget.getSelection());
 		String expectedB = MarkerSupport.HIDE_DISABLED_RIDGET_CONTENT ? null : "Option B";
 		assertEquals(expectedB, getSelectedControlValue(control));
 
 		ridget.setEnabled(true);
 
 		assertEquals("Option B", optionProvider.getSelectedOption());
 		assertEquals("Option B", ridget.getSelection());
 		assertEquals("Option B", getSelectedControlValue(control));
 	}
 
 	/**
 	 * Tests that disabling the ridget does not fire 'selected' events, even
 	 * though the control is modified.
 	 */
 	public void testDisabledDoesNotFireSelected() {
 		ISingleChoiceRidget ridget = getRidget();
 		ridget.setEnabled(true);
 		ridget.setSelection("Option A");
 
 		ridget.addPropertyChangeListener(ISingleChoiceRidget.PROPERTY_SELECTION, new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				fail("Unexpected property change event: " + evt);
 			}
 		});
 
 		ridget.setEnabled(false);
 
 		ridget.setEnabled(true);
 	}
 
 	/**
 	 * Tests that the disabled state is applied to a new control when set into
 	 * the ridget.
 	 */
 	public void testDisableAndClearOnBind() {
 		ISingleChoiceRidget ridget = getRidget();
 		ChoiceComposite control = getWidget();
 
 		ridget.setUIControl(null);
 		ridget.setEnabled(false);
 		ridget.setSelection("Option B");
 		ridget.setUIControl(control);
 
 		assertFalse(control.isEnabled());
 		String expectedB = MarkerSupport.HIDE_DISABLED_RIDGET_CONTENT ? null : "Option B";
 		assertEquals(expectedB, getSelectedControlValue(control));
 
 		ridget.setEnabled(true);
 
 		assertTrue(control.isEnabled());
 		assertEquals("Option B", getSelectedControlValue(control));
 	}
 
	/**
	 * Test that update from model uses equals() instead of comparing
	 * references.
	 */
	public void testBug255465() {
		ISingleChoiceRidget ridget = getRidget();
		ChoiceComposite control = getWidget();

		List<String> values = Arrays.asList(new String("male"), new String("female"));
		List<String> labels = Arrays.asList("Man", "Woman");
		Person personEntity = new Person("Max", "Mustermann");
		personEntity.setGender("male");
		ridget.bindToModel(values, labels, personEntity, "gender");
		ridget.updateFromModel();

		assertEquals("male", getSelectedControlValue(control));
	}

 	// helping methods
 	// ////////////////
 
 	private Button getSelectedControl(ChoiceComposite control) {
 		Button selected = null;
 		for (Control child : control.getChildren()) {
 			if (((Button) child).getSelection()) {
 				assertNull(selected);
 				selected = (Button) child;
 			}
 		}
 		return selected;
 	}
 
 	private String getSelectedControlValue(ChoiceComposite control) {
 		Button button = getSelectedControl(control);
 		return button != null ? String.valueOf(button.getData()) : null;
 	}
 
 	// helping classes
 	// ////////////////
 
 	private static final class OptionProvider {
 
 		private List<String> options = Arrays.asList("Option A", "Option B", "Option C", "Option D", "Option E",
 				"Option F");
 		private List<String> optionLabels = Arrays.asList("Option label A", "Option label B", "Option label C",
 				"Option label D", "Option label E", "Option label F");
 		private String selectedOption = options.get(0);
 
 		public List<String> getOptions() {
 			return options;
 		}
 
 		public String getSelectedOption() {
 			return selectedOption;
 		}
 
 		public void setSelectedOption(String selectedOption) {
 			this.selectedOption = selectedOption;
 		}
 
 		public List<String> getOptionLabels() {
 			return optionLabels;
 		}
 	}
 
 	private static final class TestPropertyChangeListener implements PropertyChangeListener {
 		private int eventCounter = 0;
 
 		public void propertyChange(PropertyChangeEvent evt) {
 			eventCounter++;
 		}
 	};
 
 }
