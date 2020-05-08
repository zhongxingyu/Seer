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
 package org.eclipse.riena.example.client.controllers;
 
 import java.util.Arrays;
 import java.util.List;
 
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
 import org.eclipse.riena.example.client.model.Person;
 import org.eclipse.riena.example.client.model.TestBean;
 import org.eclipse.riena.example.client.views.TextSubModuleView;
 import org.eclipse.riena.internal.example.client.beans.PersonFactory;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.core.marker.ValidationTime;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IComboRidget;
 import org.eclipse.riena.ui.ridgets.IDecimalTextRidget;
 import org.eclipse.riena.ui.ridgets.IGroupedTreeTableRidget;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IMultipleChoiceRidget;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ISingleChoiceRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.IToggleButtonRidget;
 import org.eclipse.riena.ui.ridgets.ITreeRidget;
 import org.eclipse.riena.ui.ridgets.tree2.ITreeNode;
 import org.eclipse.riena.ui.ridgets.tree2.TreeNode;
 import org.eclipse.riena.ui.ridgets.util.beans.WordNode;
 import org.eclipse.riena.ui.ridgets.validation.ValidationRuleStatus;
 
 /**
  * Controller for the {@link TextSubModuleView} example.
  */
 public class MarkerSubModuleController extends SubModuleController {
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IRidgetContainer#configureRidgets()
 	 */
 	@Override
 	public void configureRidgets() {
 		final ITextRidget textName = (ITextRidget) getRidget("textName"); //$NON-NLS-1$
 		textName.setText("Chateau Schaedelbrummer"); //$NON-NLS-1$
 
 		final IDecimalTextRidget textPrice = (IDecimalTextRidget) getRidget("textPrice"); //$NON-NLS-1$
 		textPrice.setSigned(true);
 		textPrice.setText(Double.toString(-29.99));
 
 		final IComboRidget comboAge = (IComboRidget) getRidget("comboAge"); //$NON-NLS-1$
 		List<String> ages = Arrays.asList(new String[] { "<none>", "young", "moderate", "aged", "old" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 		comboAge.bindToModel(new WritableList(ages, String.class), String.class, null, new WritableValue());
 		comboAge.updateFromModel();
 		comboAge.setEmptySelectionItem("<none>"); //$NON-NLS-1$
 		comboAge.setSelection(1);
 
 		final ISingleChoiceRidget choiceType = (ISingleChoiceRidget) getRidget("choiceType"); //$NON-NLS-1$
 		choiceType.bindToModel(Arrays.asList("red", "white", "rose"), (List<String>) null, new TestBean(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				TestBean.PROPERTY);
 		choiceType.updateFromModel();
 		choiceType.setSelection("red"); //$NON-NLS-1$
 
 		final IMultipleChoiceRidget choiceFlavor = (IMultipleChoiceRidget) getRidget("choiceFlavor"); //$NON-NLS-1$
 		choiceFlavor.bindToModel(Arrays.asList("dry", "sweet", "sour", "spicy"), (List<String>) null, new TestBean(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 				TestBean.PROPERTY);
 		choiceFlavor.updateFromModel();
 		choiceFlavor.setSelection(Arrays.asList("dry")); //$NON-NLS-1$
 
 		final ITableRidget listPersons = (ITableRidget) getRidget("listPersons"); //$NON-NLS-1$
 		listPersons.setSelectionType(ISelectableRidget.SelectionType.SINGLE);
 		listPersons.bindToModel(createPersonList(), Person.class, new String[] { "listEntry" }, new String[] { "" }); //$NON-NLS-1$//$NON-NLS-2$
 		listPersons.updateFromModel();
 
 		final ITableRidget tablePersons = (ITableRidget) getRidget("tablePersons"); //$NON-NLS-1$
 		tablePersons.setSelectionType(ISelectableRidget.SelectionType.SINGLE);
 		String[] colValues = new String[] { "lastname", "firstname" }; //$NON-NLS-1$ //$NON-NLS-2$
 		String[] colHeaders = new String[] { "Last Name", "First Name" }; //$NON-NLS-1$ //$NON-NLS-2$
 		tablePersons.bindToModel(createPersonList(), Person.class, colValues, colHeaders);
 		tablePersons.updateFromModel();
 
 		final ITreeRidget treePersons = (ITreeRidget) getRidget("treePersons"); //$NON-NLS-1$
 		treePersons.setSelectionType(ISelectableRidget.SelectionType.SINGLE);
 		treePersons.bindToModel(createTreeRoots(), ITreeNode.class, ITreeNode.PROPERTY_CHILDREN,
 				ITreeNode.PROPERTY_PARENT, ITreeNode.PROPERTY_VALUE);
 		treePersons.updateFromModel();
 
 		final IGroupedTreeTableRidget treeWCols = (IGroupedTreeTableRidget) getRidget("treeWCols"); //$NON-NLS-1$
 		treeWCols.setSelectionType(ISelectableRidget.SelectionType.MULTI);
 		treeWCols.setGroupingEnabled(true);
 		colValues = new String[] { "word", "ACount" }; //$NON-NLS-1$ //$NON-NLS-2$
 		colHeaders = new String[] { "Word", "#A" }; //$NON-NLS-1$ //$NON-NLS-2$
 		treeWCols.bindToModel(createTreeTableRoots(), WordNode.class, ITreeNode.PROPERTY_CHILDREN,
 				ITreeNode.PROPERTY_PARENT, colValues, colHeaders);
 		treeWCols.updateFromModel();
 
 		final IToggleButtonRidget buttonToggle = (IToggleButtonRidget) getRidget("buttonToggle"); //$NON-NLS-1$
 		buttonToggle.setText("Toggle"); //$NON-NLS-1$
 		buttonToggle.setSelected(true);
 		final IActionRidget buttonPush = (IActionRidget) getRidget("buttonPush"); //$NON-NLS-1$
 		final IToggleButtonRidget buttonRadioA = (IToggleButtonRidget) getRidget("buttonRadioA"); //$NON-NLS-1$
 		final IToggleButtonRidget buttonRadioB = (IToggleButtonRidget) getRidget("buttonRadioB"); //$NON-NLS-1$
 		final IToggleButtonRidget buttonCheck = (IToggleButtonRidget) getRidget("buttonCheck"); //$NON-NLS-1$
 
 		final IMarkableRidget[] markables = new IMarkableRidget[] { textName, textPrice, comboAge, choiceType,
 				choiceFlavor, listPersons, tablePersons, treePersons, treeWCols, buttonToggle, buttonPush,
 				buttonRadioA, buttonRadioB, buttonCheck };
 
 		final IToggleButtonRidget checkMandatory = (IToggleButtonRidget) getRidget("checkMandatory"); //$NON-NLS-1$
 		final IToggleButtonRidget checkError = (IToggleButtonRidget) getRidget("checkError"); //$NON-NLS-1$
 		final IToggleButtonRidget checkDisabled = (IToggleButtonRidget) getRidget("checkDisabled"); //$NON-NLS-1$
 		final IToggleButtonRidget checkOutput = (IToggleButtonRidget) getRidget("checkOutput"); //$NON-NLS-1$
 		final IToggleButtonRidget checkHidden = (IToggleButtonRidget) getRidget("checkHidden"); //$NON-NLS-1$
 
 		checkMandatory.setText("&mandatory"); //$NON-NLS-1$
 		checkMandatory.addListener(new IActionListener() {
 			public void callback() {
 				boolean isMandatory = checkMandatory.isSelected();
 				for (IMarkableRidget ridget : markables) {
 					ridget.setMandatory(isMandatory);
 				}
 				if (isMandatory) {
 					textName.setText(""); //$NON-NLS-1$
 					textPrice.setText(""); //$NON-NLS-1$
 					comboAge.setSelection("<none>"); //$NON-NLS-1$
 					choiceType.setSelection(null);
 					choiceFlavor.setSelection(null);
 					listPersons.setSelection((Object) null);
 					tablePersons.setSelection((Object) null);
 					treePersons.setSelection((Object) null);
 					treeWCols.setSelection((Object) null);
 					buttonToggle.setSelected(false);
 					buttonRadioA.setSelected(false);
 					buttonRadioB.setSelected(false);
 					buttonCheck.setSelected(false);
 				}
 			}
 		});
 
 		checkError.setText("&error"); //$NON-NLS-1$
 		checkError.addListener(new IActionListener() {
 			private IValidator alwaysWrong = new AlwaysWrongValidator();
 
 			public void callback() {
 				boolean isError = checkError.isSelected();
 				for (IMarkableRidget ridget : markables) {
 					ridget.setErrorMarked(isError);
 				}
				// using this "always wrong" validator for purposes of this
				// demo. It prevents the error marker being removed from the
				// text field on the next revalidation (i.e. when the user
				// types).
 				if (isError) {
 					textName.addValidationRule(alwaysWrong, ValidationTime.ON_UI_CONTROL_EDIT);
 					textPrice.addValidationRule(alwaysWrong, ValidationTime.ON_UI_CONTROL_EDIT);
 				} else {
 					textName.removeValidationRule(alwaysWrong);
 					textPrice.removeValidationRule(alwaysWrong);
 				}
 			}
 		});
 
 		checkDisabled.setText("&disabled"); //$NON-NLS-1$
 		checkDisabled.addListener(new IActionListener() {
 			public void callback() {
 				boolean isEnabled = !checkDisabled.isSelected();
 				for (IMarkableRidget ridget : markables) {
 					ridget.setEnabled(isEnabled);
 				}
 			}
 		});
 
 		checkOutput.setText("&output"); //$NON-NLS-1$
 		checkOutput.addListener(new IActionListener() {
 			public void callback() {
 				boolean isOutput = checkOutput.isSelected();
 				for (IMarkableRidget ridget : markables) {
 					ridget.setOutputOnly(isOutput);
 				}
 			}
 		});
 
 		checkHidden.setText("&hidden"); //$NON-NLS-1$
 		checkHidden.addListener(new IActionListener() {
 			public void callback() {
 				boolean isVisible = !checkHidden.isSelected();
 				for (IMarkableRidget ridget : markables) {
 					ridget.setVisible(isVisible);
 				}
 			}
 		});
 	}
 
 	// helping methods
 	// ////////////////
 
 	private WritableList createPersonList() {
 		return new WritableList(PersonFactory.createPersonList(), Person.class);
 	}
 
 	private ITreeNode[] createTreeRoots() {
 		ITreeNode rootA = new TreeNode("A"); //$NON-NLS-1$
 		new TreeNode(rootA, new Person("Albinus", "Albert")); //$NON-NLS-1$ //$NON-NLS-2$
 		new TreeNode(rootA, new Person("Aurelius", "Mark")); //$NON-NLS-1$ //$NON-NLS-2$
 		ITreeNode rootB = new TreeNode("B"); //$NON-NLS-1$
 		new TreeNode(rootB, new Person("Barker", "Clyve")); //$NON-NLS-1$ //$NON-NLS-2$
 		new TreeNode(rootB, new Person("Barclay", "Bob")); //$NON-NLS-1$ //$NON-NLS-2$
 		return new ITreeNode[] { rootA, rootB };
 	}
 
 	private WordNode[] createTreeTableRoots() {
 		WordNode rootA = new WordNode("A"); //$NON-NLS-1$
 		WordNode rootB = new WordNode("B"); //$NON-NLS-1$
 		new WordNode(rootA, "Astoria"); //$NON-NLS-1$
 		new WordNode(rootA, "Ashland"); //$NON-NLS-1$
 		new WordNode(rootA, "Aurora"); //$NON-NLS-1$
 		new WordNode(rootA, "Alpine"); //$NON-NLS-1$
 		new WordNode(rootB, "Boring"); //$NON-NLS-1$
 		new WordNode(rootB, "Bend"); //$NON-NLS-1$
 		new WordNode(rootB, "Beaverton"); //$NON-NLS-1$
 		new WordNode(rootB, "Bridgeport"); //$NON-NLS-1$
 		return new WordNode[] { rootA, rootB };
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * Validator that always returns an error status.
 	 */
 	private static final class AlwaysWrongValidator implements IValidator {
 		public IStatus validate(Object value) {
 			return ValidationRuleStatus.error(false, "", this); //$NON-NLS-1$
 		}
 
 	}
 
 }
