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
 package org.eclipse.riena.client.controller.test;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Locale;
 
 import org.eclipse.riena.beans.common.Person;
 import org.eclipse.riena.beans.common.PersonFactory;
 import org.eclipse.riena.example.client.controllers.MarkerSubModuleController;
 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 import org.eclipse.riena.internal.ui.swt.test.TestUtils;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.ui.swt.controllers.AbstractSubModuleControllerTest;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IComboRidget;
 import org.eclipse.riena.ui.ridgets.ICompositeTableRidget;
 import org.eclipse.riena.ui.ridgets.IDateTextRidget;
 import org.eclipse.riena.ui.ridgets.IDateTimeRidget;
 import org.eclipse.riena.ui.ridgets.IDecimalTextRidget;
 import org.eclipse.riena.ui.ridgets.IGroupedTreeTableRidget;
 import org.eclipse.riena.ui.ridgets.IListRidget;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IMultipleChoiceRidget;
 import org.eclipse.riena.ui.ridgets.INumericTextRidget;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ISingleChoiceRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.IToggleButtonRidget;
 import org.eclipse.riena.ui.ridgets.ITreeRidget;
 import org.eclipse.riena.ui.ridgets.tree2.ITreeNode;
 import org.eclipse.riena.ui.ridgets.tree2.TreeNode;
 import org.eclipse.riena.ui.tests.base.TestSingleSelectionBean;
 
 /**
  * Tests for the MarkerSubModuleController.
  */
 @NonUITestCase
 public class MarkerSubModuleControllerTest extends AbstractSubModuleControllerTest<MarkerSubModuleController> {
 
 	private List<Person> personList;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		personList = PersonFactory.createPersonList();
 	}
 
 	@Override
 	protected MarkerSubModuleController createController(ISubModuleNode node) {
 
 		MarkerSubModuleController newInst = new MarkerSubModuleController();
 		node.setNodeId(new NavigationNodeId("org.eclipse.riena.example.marker"));
 		newInst.setNavigationNode(node);
 
 		return newInst;
 	}
 
 	public void testBasicInitialization() {
 
 		IToggleButtonRidget checkMandatory = getController().getRidget(IToggleButtonRidget.class, "checkMandatory"); //$NON-NLS-1$
 		assertFalse(checkMandatory.isSelected());
 		IToggleButtonRidget checkError = getController().getRidget(IToggleButtonRidget.class, "checkError"); //$NON-NLS-1$
 		assertFalse(checkError.isSelected());
 		IToggleButtonRidget checkDisabled = getController().getRidget(IToggleButtonRidget.class, "checkDisabled"); //$NON-NLS-1$
 		assertFalse(checkDisabled.isSelected());
 		IToggleButtonRidget checkOutput = getController().getRidget(IToggleButtonRidget.class, "checkOutput"); //$NON-NLS-1$
 		assertFalse(checkOutput.isSelected());
 
 		IToggleButtonRidget checkHidden = getController().getRidget(IToggleButtonRidget.class, "checkHidden"); //$NON-NLS-1$
 		assertFalse(checkHidden.isSelected());
 		IToggleButtonRidget checkHiddenParent = getController().getRidget(IToggleButtonRidget.class,
 				"checkHiddenParent"); //$NON-NLS-1$
 		assertFalse(checkHiddenParent.isSelected());
 
 		ITextRidget textName = getController().getRidget(ITextRidget.class, "textName"); //$NON-NLS-1$
 		assertEquals("Chateau Schaedelbrummer", textName.getText());
 
 		IDecimalTextRidget textPrice = getController().getRidget(IDecimalTextRidget.class, "textPrice"); //$NON-NLS-1$
 		assertEquals(TestUtils.getLocalizedNumber("-29,99"), textPrice.getText());
 		INumericTextRidget textAmount = getController().getRidget(INumericTextRidget.class, "textAmount"); //$NON-NLS-1$
 		assertEquals(TestUtils.getLocalizedNumber("1.001"), textAmount.getText());
 
 		IDateTextRidget textDate = getController().getRidget(IDateTextRidget.class, "textDate"); //$NON-NLS-1$
 		assertEquals("04.12.2008", textDate.getText());
 
 		IDateTimeRidget dtDate = getController().getRidget(IDateTimeRidget.class, "dtDate"); //$NON-NLS-1$
 		if ("US".equals(Locale.getDefault().getCountry())) {
 			assertEquals("12/4/08 12:00 AM", dtDate.getText());
 		} else {
 			assertEquals("04.12.08 00:00", dtDate.getText());
 		}
 		Calendar cal = Calendar.getInstance();
 		cal.clear();
 		cal.set(2008, Calendar.DECEMBER, 4);
 		assertEquals(cal.getTime(), dtDate.getDate());
 
 		IComboRidget comboAge = getController().getRidget(IComboRidget.class, "comboAge"); //$NON-NLS-1$
 		assertEquals("young", comboAge.getSelection());
 
 		ISingleChoiceRidget choiceType = getController().getRidget(ISingleChoiceRidget.class, "choiceType"); //$NON-NLS-1$
 		assertEquals("red", choiceType.getSelection());
 
 		IMultipleChoiceRidget choiceFlavor = getController().getRidget(IMultipleChoiceRidget.class, "choiceFlavor"); //$NON-NLS-1$
 		assertNotNull(choiceFlavor.getSelection());
 		assertEquals(1, choiceFlavor.getSelection().size());
 		assertEquals("dry", choiceFlavor.getSelection().get(0));
 
 		IToggleButtonRidget buttonToggle = getController().getRidget(IToggleButtonRidget.class, "buttonToggle"); //$NON-NLS-1$
 		assertTrue(buttonToggle.isSelected());
 
 		IToggleButtonRidget buttonRadioA = getController().getRidget(IToggleButtonRidget.class, "buttonRadioA"); //$NON-NLS-1$
 		assertFalse(buttonRadioA.isSelected());
 
 		IToggleButtonRidget buttonRadioB = getController().getRidget(IToggleButtonRidget.class, "buttonRadioB"); //$NON-NLS-1$
 		assertFalse(buttonRadioB.isSelected());
 
 		IToggleButtonRidget buttonCheck = getController().getRidget(IToggleButtonRidget.class, "buttonCheck"); //$NON-NLS-1$
 		assertFalse(buttonCheck.isSelected());
 	}
 
 	private List<IMarkableRidget> getAllMarkableRidgets() {
 		List<IMarkableRidget> markableRidgets = new ArrayList<IMarkableRidget>();
 
 		markableRidgets.add(getController().getRidget(ITextRidget.class, "textName"));
 		markableRidgets.add(getController().getRidget(IDecimalTextRidget.class, "textPrice"));
 		markableRidgets.add(getController().getRidget(INumericTextRidget.class, "textAmount"));
 		markableRidgets.add(getController().getRidget(IDateTextRidget.class, "textDate"));
 		markableRidgets.add(getController().getRidget(IDateTimeRidget.class, "dtDate"));
 		markableRidgets.add(getController().getRidget(IComboRidget.class, "comboAge"));
 		markableRidgets.add(getController().getRidget(ISingleChoiceRidget.class, "choiceType"));
 		markableRidgets.add(getController().getRidget(IMultipleChoiceRidget.class, "choiceFlavor"));
 		markableRidgets.add(getController().getRidget(IToggleButtonRidget.class, "buttonToggle"));
 		markableRidgets.add(getController().getRidget(IToggleButtonRidget.class, "buttonRadioA"));
 		markableRidgets.add(getController().getRidget(IToggleButtonRidget.class, "buttonRadioB"));
 		markableRidgets.add(getController().getRidget(IToggleButtonRidget.class, "buttonCheck"));
 
 		return markableRidgets;
 	}
 
 	// TODO [ev] not working because assertIsBoundToModel() in AbstractSelectableRidget throws an exception when calling setSelection on ITreeRidget.
 	// See 296639: Make getRowObservables() independent of the uiControl https://bugs.eclipse.org/bugs/show_bug.cgi?id=296639
	public void te_xx_stMandatoryMarkerOptions() {
 		ITreeRidget treePersons = getController().getRidget(ITreeRidget.class, "treePersons"); //$NON-NLS-1$
 		treePersons.setSelectionType(ISelectableRidget.SelectionType.SINGLE);
 		TestSingleSelectionBean singleSelectionBean = new TestSingleSelectionBean();
 		treePersons.bindSingleSelectionToModel(singleSelectionBean, TestSingleSelectionBean.PROPERTY_SELECTION);
 		treePersons.updateFromModel();
 
 		IToggleButtonRidget mandatoryToggle = getController().getRidget(IToggleButtonRidget.class, "checkMandatory");
 		assertFalse(mandatoryToggle.isSelected());
 		for (IMarkableRidget ridget : getAllMarkableRidgets()) {
 			assertFalse(ridget.isMandatory());
 		}
 
 		mandatoryToggle.setSelected(true);
 		assertTrue(mandatoryToggle.isSelected());
 		for (IMarkableRidget ridget : getAllMarkableRidgets()) {
 			assertTrue(ridget.isMandatory());
 		}
 	}
 
 	public ITreeNode[] getTreeRoots() {
 
 		ITreeNode rootA = new TreeNode("A"); //$NON-NLS-1$
 		new TreeNode(rootA, new Person("Albinus", "Albert")); //$NON-NLS-1$ //$NON-NLS-2$
 		new TreeNode(rootA, new Person("Aurelius", "Mark")); //$NON-NLS-1$ //$NON-NLS-2$
 		ITreeNode rootB = new TreeNode("B"); //$NON-NLS-1$
 		new TreeNode(rootB, new Person("Barker", "Clyve")); //$NON-NLS-1$ //$NON-NLS-2$
 		new TreeNode(rootB, new Person("Barclay", "Bob")); //$NON-NLS-1$ //$NON-NLS-2$
 		ITreeNode[] treeRoots = new ITreeNode[] { rootA, rootB };
 
 		return treeRoots;
 	}
 
 	public void testErrorMarkerOptions() {
 		IToggleButtonRidget errorToggle = getController().getRidget(IToggleButtonRidget.class, "checkError");
 		assertFalse(errorToggle.isSelected());
 		for (IMarkableRidget ridget : getAllMarkableRidgets()) {
 			assertFalse(ridget.isErrorMarked());
 		}
 
 		errorToggle.setSelected(true);
 		assertTrue(errorToggle.isSelected());
 		for (IMarkableRidget ridget : getAllMarkableRidgets()) {
 			assertTrue(ridget.isErrorMarked());
 		}
 	}
 
 	public void testDisabledMarkerOptions() {
 		IToggleButtonRidget disabledToggle = getController().getRidget(IToggleButtonRidget.class, "checkDisabled");
 		assertFalse(disabledToggle.isSelected());
 		for (IMarkableRidget ridget : getAllMarkableRidgets()) {
 			assertTrue(ridget.isEnabled());
 		}
 
 		disabledToggle.setSelected(true);
 		assertTrue(disabledToggle.isSelected());
 		for (IMarkableRidget ridget : getAllMarkableRidgets()) {
 			assertFalse(ridget.isEnabled());
 		}
 
 		assertFalse(getController().getRidget(IActionRidget.class, "buttonPush").isEnabled());
 	}
 
 	public void testOutputMarkerOptions() {
 		IToggleButtonRidget outputToggle = getController().getRidget(IToggleButtonRidget.class, "checkOutput");
 		assertFalse(outputToggle.isSelected());
 		for (IMarkableRidget ridget : getAllMarkableRidgets()) {
 			assertFalse(ridget.isOutputOnly());
 		}
 
 		outputToggle.setSelected(true);
 		assertTrue(outputToggle.isSelected());
 		for (IMarkableRidget ridget : getAllMarkableRidgets()) {
 			assertTrue(ridget.isOutputOnly());
 		}
 	}
 
 	public void testHiddenMarkerOptions() {
 		IToggleButtonRidget hiddenToggle = getController().getRidget(IToggleButtonRidget.class, "checkHidden");
 		assertFalse(hiddenToggle.isSelected());
 		for (IMarkableRidget ridget : getAllMarkableRidgets()) {
 			assertTrue(ridget.isVisible());
 		}
 
 		hiddenToggle.setSelected(true);
 		assertTrue(hiddenToggle.isSelected());
 		for (IMarkableRidget ridget : getAllMarkableRidgets()) {
 			assertFalse(ridget.isVisible());
 		}
 
 		assertFalse(getController().getRidget(IActionRidget.class, "buttonPush").isVisible());
 	}
 
 	public void testList() {
 
 		Person expected = null;
 		Person actual = null;
 		IListRidget listPersons = getController().getRidget(IListRidget.class, "listPersons"); //$NON-NLS-1$
 		assertEquals(personList.size(), listPersons.getOptionCount());
 		assertEquals(-1, listPersons.getSelectionIndex());
 		assertEquals(0, listPersons.getSelection().size());
 		for (int i = 0; i < listPersons.getOptionCount(); i++) {
 			expected = personList.get(i);
 			actual = (Person) listPersons.getOption(i);
 			assertEquals(expected.getFirstname(), actual.getFirstname());
 			assertEquals(expected.getLastname(), actual.getLastname());
 		}
 		listPersons.setSelection(1);
 		// Fehler: liefert -1
 		// assertEquals(1, listPersons.getSelectionIndex());
 		assertEquals(1, listPersons.getSelection().size());
 		expected = personList.get(1);
 		actual = (Person) listPersons.getSelection().get(0);
 		assertEquals(expected.getFirstname(), actual.getFirstname());
 		assertEquals(expected.getLastname(), actual.getLastname());
 	}
 
 	public void testTable() {
 
 		Person expected = null;
 		Person actual = null;
 		ITableRidget tablePersons = getController().getRidget(ITableRidget.class, "tablePersons"); //$NON-NLS-1$
 		assertEquals(personList.size(), tablePersons.getOptionCount());
 		assertEquals(-1, tablePersons.getSelectionIndex());
 		assertEquals(0, tablePersons.getSelection().size());
 		for (int i = 0; i < tablePersons.getOptionCount(); i++) {
 			expected = personList.get(i);
 			actual = (Person) tablePersons.getOption(i);
 			assertEquals(expected.getFirstname(), actual.getFirstname());
 			assertEquals(expected.getLastname(), actual.getLastname());
 		}
 		tablePersons.setSelection(1);
 		// Fehler: liefert -1
 		// assertEquals(1, tablePersons.getSelectionIndex());
 		assertEquals(1, tablePersons.getSelection().size());
 		expected = personList.get(1);
 		actual = (Person) tablePersons.getSelection().get(0);
 		assertEquals(expected.getFirstname(), actual.getFirstname());
 		assertEquals(expected.getLastname(), actual.getLastname());
 	}
 
 	public void testCompositeTable() {
 
 		Person expected = null;
 		Person actual = null;
 		ICompositeTableRidget compTable = getController().getRidget(ICompositeTableRidget.class, "compTable"); //$NON-NLS-1$
 		assertEquals(personList.size(), compTable.getOptionCount());
 		assertEquals(-1, compTable.getSelectionIndex());
 		assertEquals(0, compTable.getSelection().size());
 		for (int i = 0; i < compTable.getOptionCount(); i++) {
 			expected = personList.get(i);
 			actual = (Person) compTable.getOption(i);
 			assertEquals(expected.getFirstname(), actual.getFirstname());
 			assertEquals(expected.getLastname(), actual.getLastname());
 		}
 		compTable.setSelection(1);
 		assertEquals(1, compTable.getSelectionIndex());
 		assertEquals(1, compTable.getSelection().size());
 		expected = personList.get(1);
 		actual = (Person) compTable.getSelection().get(0);
 		assertEquals(expected.getFirstname(), actual.getFirstname());
 		assertEquals(expected.getLastname(), actual.getLastname());
 	}
 
 	public void testTree() {
 		ITreeRidget treePersons = getController().getRidget(ITreeRidget.class, "treePersons"); //$NON-NLS-1$
 		assertTrue(treePersons.getSelection().isEmpty());
 	}
 
 	public void testTreeTable() {
 		IGroupedTreeTableRidget treeWCols = getController().getRidget(IGroupedTreeTableRidget.class, "treeWCols"); //$NON-NLS-1$
 		assertTrue(treeWCols.getSelection().isEmpty());
 	}
 
 	public void testMarkers() {
 		basicTestMarkers(getController().getRidget(ITextRidget.class, "textName"), "testString"); //$NON-NLS-1$
 		basicTestMarkers(getController().getRidget(IDecimalTextRidget.class, "textPrice"), "123"); //$NON-NLS-1$
 		basicTestMarkers(getController().getRidget(INumericTextRidget.class, "textAmount"), "123"); //$NON-NLS-1$
 		basicTestMarkers(getController().getRidget(IDateTextRidget.class, "textDate"), "01.01.1980"); //$NON-NLS-1$
 		basicTestMarkers(getController().getRidget(IDateTimeRidget.class, "dtDate")); //$NON-NLS-1$
 		basicTestMarkers(getController().getRidget(IComboRidget.class, "comboAge")); //$NON-NLS-1$
 		basicTestMarkers(getController().getRidget(ISingleChoiceRidget.class, "choiceType")); //$NON-NLS-1$
 		basicTestMarkers(getController().getRidget(IMultipleChoiceRidget.class, "choiceFlavor")); //$NON-NLS-1$
 	}
 
 	private void basicTestMarkers(ITextRidget textRidget, String value) {
 		basicTestMarkers(textRidget);
 		textRidget.setText(null);
 		textRidget.setMandatory(true);
 		assertFalse(textRidget.isDisableMandatoryMarker());
 		textRidget.setText(value);
 		assertTrue(textRidget.isDisableMandatoryMarker());
 		textRidget.setText(null);
 		assertFalse(textRidget.isDisableMandatoryMarker());
 		textRidget.setMandatory(false);
 	}
 
 	private void basicTestMarkers(IComboRidget comboRidget) {
 		basicTestMarkers((IMarkableRidget) comboRidget);
 		comboRidget.setMandatory(true);
 		comboRidget.setSelection(null);
 		assertFalse(comboRidget.isDisableMandatoryMarker());
 		if (comboRidget.getEmptySelectionItem() == null) {
 			comboRidget.setSelection(0);
 		} else {
 			comboRidget.setSelection(1);
 		}
 		assertTrue(comboRidget.isDisableMandatoryMarker());
 		comboRidget.setSelection(-1);
 		assertFalse(comboRidget.isDisableMandatoryMarker());
 		if (comboRidget.getEmptySelectionItem() == null) {
 			comboRidget.setSelection(0);
 		} else {
 			comboRidget.setSelection(1);
 		}
 		assertTrue(comboRidget.isDisableMandatoryMarker());
 		comboRidget.setSelection(comboRidget.getEmptySelectionItem());
 		assertFalse(comboRidget.isDisableMandatoryMarker());
 		comboRidget.setMandatory(false);
 	}
 
 	private void basicTestMarkers(IMarkableRidget ridget) {
 		basicTestMandatory(ridget);
 		basicTestErrorMarked(ridget);
 		basicTestOutputOnly(ridget);
 		basicTestEnabled(ridget);
 		basicTestVisible(ridget);
 	}
 
 	private void basicTestMandatory(IMarkableRidget ridget) {
 		assertFalse(ridget.isMandatory());
 		ridget.setMandatory(true);
 		assertTrue(ridget.isMandatory());
 		ridget.setMandatory(false);
 		assertFalse(ridget.isMandatory());
 	}
 
 	private void basicTestErrorMarked(IMarkableRidget ridget) {
 		assertFalse(ridget.isErrorMarked());
 		ridget.setErrorMarked(true);
 		assertTrue(ridget.isErrorMarked());
 		ridget.setErrorMarked(false);
 		assertFalse(ridget.isErrorMarked());
 	}
 
 	private void basicTestOutputOnly(IMarkableRidget ridget) {
 		assertFalse(ridget.isOutputOnly());
 		ridget.setOutputOnly(true);
 		assertTrue(ridget.isOutputOnly());
 		ridget.setOutputOnly(false);
 		assertFalse(ridget.isOutputOnly());
 	}
 
 	private void basicTestEnabled(IMarkableRidget ridget) {
 		assertTrue(ridget.isEnabled());
 		ridget.setEnabled(false);
 		assertFalse(ridget.isEnabled());
 		ridget.setEnabled(true);
 		assertTrue(ridget.isEnabled());
 	}
 
 	private void basicTestVisible(IMarkableRidget ridget) {
 		assertTrue(ridget.isVisible());
 		ridget.setVisible(false);
 		assertFalse(ridget.isVisible());
 		ridget.setVisible(true);
 		assertTrue(ridget.isVisible());
 	}
 }
