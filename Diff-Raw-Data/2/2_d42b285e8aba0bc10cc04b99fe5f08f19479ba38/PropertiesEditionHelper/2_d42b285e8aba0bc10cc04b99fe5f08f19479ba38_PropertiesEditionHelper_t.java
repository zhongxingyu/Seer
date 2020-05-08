 /*******************************************************************************
  * Copyright (c) 2008, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.modelingBot.helper;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.eef.components.PropertiesEditionElement;
 import org.eclipse.emf.eef.extended.editor.ReferenceableObject;
 import org.eclipse.emf.eef.modelingBot.Action;
 import org.eclipse.emf.eef.modelingBot.SequenceType;
 import org.eclipse.emf.eef.modelingBot.Wizard;
 import org.eclipse.emf.eef.modelingBot.EEFActions.Add;
 import org.eclipse.emf.eef.modelingBot.EEFActions.Cancel;
 import org.eclipse.emf.eef.modelingBot.EEFActions.EditAction;
 import org.eclipse.emf.eef.modelingBot.interpreter.EEFInterpreter;
 import org.eclipse.emf.eef.modelingBot.swtbot.SWTEEFBot;
 import org.eclipse.emf.eef.modelingBot.ui.utils.WrappedSWTBotRadio;
 import org.eclipse.emf.eef.runtime.EEFRuntimePlugin;
 import org.eclipse.emf.eef.views.ElementEditor;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
 
 /**
  * @author <a href="mailto:nathalie.lepine@obeo.fr">Nathalie Lepine</a>
  */
 public class PropertiesEditionHelper {
 
 	/**
 	 * SWT EEF Bot.
 	 */
 	private SWTEEFBot bot;
 
 	/**
 	 * Create a PropertiesEditionHelper.
 	 * 
 	 * @param bot
 	 *            SWTEEFBot
 	 */
 	public PropertiesEditionHelper(SWTEEFBot bot) {
 		this.bot = bot;
 	}
 
 	/**
 	 * Update an attribute.
 	 * 
 	 * @param selectNode
 	 * @param propertiesEditionElement
 	 * @param value
 	 * @param container
 	 * @param containerOfcontainer
 	 * @param sequenceType
 	 */
 	public void updateAttribute(SWTBotTreeItem selectNode,
 			PropertiesEditionElement propertiesEditionElement,
 			EObject referenceableObject, EObject container,
 			Collection<String> values, SequenceType sequenceType) {
 		assertFalse(propertiesEditionElement.getViews().isEmpty());
 		assertFalse(values == null);
 		assertFalse(values.isEmpty());
 
 		final ElementEditor elementEditor = propertiesEditionElement.getViews()
 				.get(0);
 		final String representationName = elementEditor.getRepresentation()
 				.getName();
 		if ("Text".equals(representationName)
 				|| "Textarea".equals(representationName)) {
 			updateText(propertiesEditionElement, referenceableObject,
 					container, values.iterator().next(), sequenceType);
 		} else if ("EMFComboViewer".equals(representationName)) {
 			updateEMFComboViewer(propertiesEditionElement, referenceableObject,
 					container, values.iterator().next(), sequenceType);
 		} else if ("Radio".equals(representationName)) {
 			updateRadio(elementEditor, values.iterator().next(), sequenceType);
 		} else if ("Checkbox".equals(representationName)) {
 			AdapterFactory adapterFactory = EEFRuntimePlugin.getDefault()
 					.getAdapterFactory();
 			EStructuralFeature model = EMFHelper.map(container.eClass()
 					.getEPackage(), propertiesEditionElement.getModel());
 			IItemPropertySource adapt = (IItemPropertySource) adapterFactory
 					.adapt(container, IItemPropertySource.class);
 			String value = null;
 			if (adapt != null) {
 				value = adapt.getPropertyDescriptor(container, model)
 						.getDisplayName(container);
 			}
 			updateCheckbox(elementEditor, value, sequenceType);
 		} else if ("MultiValuedEditor".equals(representationName)) {
 			updateMultiValuedEditor(propertiesEditionElement, values,
 					sequenceType);
 		} else {
 			System.out.println("Case not managed in updateAttribute : "
 					+ representationName);
 		}
 	}
 
 	/**
 	 * 
 	 * @param elementEditor
 	 * @param value
 	 * @param sequenceType
 	 */
 	private void updateRadio(ElementEditor elementEditor, String value,
 			SequenceType sequenceType) {
 		SWTBotHelper.waitAllUiEvents();
 		final SWTBotRadio radio = bot.radioWithIdAndMnemonic(
 				elementEditor.getQualifiedIdentifier(), value);
 		WrappedSWTBotRadio wrappedRadio = new WrappedSWTBotRadio(radio);
 		wrappedRadio.click();
 		SWTBotHelper.sendFocusLost(radio.widget);
 		SWTBotHelper.waitAllUiEvents();
 
 	}
 
 	/**
 	 * Update a feature.
 	 * 
 	 * @param selectNode
 	 * @param propertiesEditionElement
 	 * @param referenceableObject
 	 *            the container of the reference to set
 	 * @param value
 	 *            the value to set
 	 * @param sequenceType
 	 */
 	public void updateFeature(SWTBotTreeItem selectNode,
 			PropertiesEditionElement propertiesEditionElement,
 			ReferenceableObject referenceableObject,
 			Collection<EObject> values, SequenceType sequenceType) {
 		assertFalse(propertiesEditionElement.getViews().isEmpty());
 		final ElementEditor elementEditor = propertiesEditionElement.getViews()
 				.get(0);
 		final String representationName = elementEditor.getRepresentation()
 				.getName();
 		if ("ReferencesTable".equals(representationName)) {
 			updateReferencesTable(propertiesEditionElement, values);
 		} else if ("AdvancedReferencesTable".equals(representationName)) {
 			updateAdvancedReferencesTable(propertiesEditionElement, values);
 		} else if ("FlatReferencesTable".equals(representationName)) {
 			updateFlatReferencesTable(propertiesEditionElement, values);
 		} else if ("EObjectFlatComboViewer".equals(representationName)) {
 			updateEObjectFlatComboViewer(propertiesEditionElement, values);
 		} else if ("AdvancedEObjectFlatComboViewer".equals(representationName)) {
 			updateAdvancedEObjectFlatComboViewer(propertiesEditionElement,
 					values);
 		} else if ("Combo".equals(representationName)) {
 			final EObject container = bot
 					.getEObjectFromReferenceableEObject((ReferenceableObject) referenceableObject);
 			assertNotNull("No container is found to launch add action.",
 					container);
 			updateCombo(selectNode, propertiesEditionElement,
 					referenceableObject, container, sequenceType);
 		} else {
 			System.out.println("Case not managed in updateFeature : "
 					+ representationName);
 		}
 
 	}
 
 	/**
 	 * Update widget Combo.
 	 * 
 	 * @param propertiesEditionElement
 	 * @param value
 	 * @param value
 	 */
 	private void updateCombo(SWTBotTreeItem selectNode,
 			PropertiesEditionElement propertiesEditionElement,
 			EObject referenceableObject, EObject container,
 			SequenceType sequenceType) {
 		SWTBotHelper.waitAllUiEvents();
 		String realLabel = EMFHelper.getEditorLabel(propertiesEditionElement,
 				referenceableObject, container, sequenceType);
 		final SWTBotCombo comboBoxWithLabel = bot.comboBoxWithLabel(realLabel
 				+ ": ");
 		comboBoxWithLabel.setSelection(0);
 		SWTBotHelper.sendFocusLost(comboBoxWithLabel.widget);
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	/**
 	 * Update widget ReferencesTable.
 	 * 
 	 * @param propertiesEditionElement
 	 * @param value
 	 */
 	private void updateReferencesTable(
 			PropertiesEditionElement propertiesEditionElement,
 			Collection<EObject> values) {
 		SWTBotHelper.waitAllUiEvents();
 		String label = ((ElementEditor) propertiesEditionElement.getViews()
 				.get(0)).getQualifiedIdentifier();
 		bot.addButtonReferencesTable(label).click();
 		bot.selectInActiveTable(values);
 		clickOkOrCancel(propertiesEditionElement);
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	/**
 	 * Update widget AdvancedReferencesTable.
 	 * 
 	 * @param propertiesEditionElement
 	 * @param value
 	 */
 	private void updateAdvancedReferencesTable(
 			PropertiesEditionElement propertiesEditionElement,
 			Collection<EObject> values) {
 		SWTBotHelper.waitAllUiEvents();
 		String label = ((ElementEditor) propertiesEditionElement.getViews()
 				.get(0)).getQualifiedIdentifier();
 		// TODO manage this case with selectInActiveTree(Collection<EObject>)
 		// when EEF will allowed to select multiple elements in a tree
 		for (EObject value : values) {
 			bot.addButtonAdvancedReferencesTable(label).click();
 			bot.selectInActiveTree(value);
 			clickOkOrCancel(propertiesEditionElement);
 		}
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	/**
 	 * Update widget FlatReferencesTable.
 	 * 
 	 * @param propertiesEditionElement
 	 * @param value
 	 */
 	private void updateFlatReferencesTable(
 			PropertiesEditionElement propertiesEditionElement,
 			Collection<EObject> values) {
 		SWTBotHelper.waitAllUiEvents();
 		String label = ((ElementEditor) propertiesEditionElement.getViews()
 				.get(0)).getQualifiedIdentifier();
 		bot.browseButtonFlatReferencesTable(label).click();
 		bot.selectInActiveTable(values);
 		SWTBotButton buttonAdd = bot.button(0);
 		buttonAdd.click();
 		clickOkOrCancel(propertiesEditionElement);
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	/**
 	 * Unset widget FlatReferencesTable.
 	 * 
 	 * @param propertiesEditionElement
 	 * @param values
 	 */
 	public void unsetFlatReferencesTable(
 			PropertiesEditionElement propertiesEditionElement,
 			Collection<EObject> values) {
 		SWTBotHelper.waitAllUiEvents();
 		String label = ((ElementEditor) propertiesEditionElement.getViews()
 				.get(0)).getQualifiedIdentifier();
 		bot.browseButtonFlatReferencesTable(label).click();
 		if (values == null || values.isEmpty()) {
 			final SWTBotTable table = bot.table(1);
 			int rowCount = table.rowCount();
 			for (int i = 0; i < rowCount; i++) {
 				SWTBotButton buttonRemove = bot.button(1);
 				buttonRemove.click();
 				SWTBotHelper.waitAllUiEvents();
 			}
 		} else {
 			for (EObject value : values) {
 				bot.selectInRightTableOfActiveEditor(value);
 				SWTBotButton buttonRemove = bot.button(1);
 				buttonRemove.click();
 				SWTBotHelper.waitAllUiEvents();
 			}
 		}
 		clickOkOrCancel(propertiesEditionElement);
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	/**
 	 * Update widget EObjectFlatComboViewer.
 	 * 
 	 * @param propertiesEditionElement
 	 * @param value
 	 */
 	private void updateEObjectFlatComboViewer(
 			PropertiesEditionElement propertiesEditionElement,
 			Collection<EObject> values) {
 		SWTBotHelper.waitAllUiEvents();
 		String label = ((ElementEditor) propertiesEditionElement.getViews()
 				.get(0)).getQualifiedIdentifier();
 		bot.editButtonEObjectFlatComboViewer(label).click();
 		bot.selectInActiveTable(values);
 		clickOkOrCancel(propertiesEditionElement);
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	/**
 	 * Update widget AdvancedEObjectFlatComboViewer.
 	 * 
 	 * @param propertiesEditionElement
 	 * @param value
 	 */
 	private void updateAdvancedEObjectFlatComboViewer(
 			PropertiesEditionElement propertiesEditionElement,
 			Collection<EObject> values) {
 		SWTBotHelper.waitAllUiEvents();
 		String label = ((ElementEditor) propertiesEditionElement.getViews()
 				.get(0)).getQualifiedIdentifier();
 		bot.browseButtonAdvancedEObjectFlatComboViewer(label).click();
 		bot.selectInActiveTree(values);
 		clickOkOrCancel(propertiesEditionElement);
 		SWTBotHelper.waitAllUiEvents();
 
 	}
 
 	/**
 	 * Update widget text.
 	 * 
 	 * @param selectNode
 	 * @param propertiesEditionElement
 	 * @param value
 	 * @param container
 	 * @param sequenceType
 	 */
 	private void updateText(PropertiesEditionElement propertiesEditionElement,
 			EObject referenceableObject, EObject container, String value,
 			SequenceType sequenceType) {
 		SWTBotHelper.waitAllUiEvents();
 		String realLabel = EMFHelper.getEditorLabel(propertiesEditionElement,
 				referenceableObject, container, sequenceType);
 		final SWTBotText textWithLabel = bot.textWithLabel(realLabel + ": ");
 		textWithLabel.setText(value);
 		SWTBotHelper.pressEnterKey(textWithLabel.widget);
 		SWTBotHelper.sendFocusLost(textWithLabel.widget);
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	/**
 	 * Update widget EMFComboViewer.
 	 * 
 	 * @param propertiesEditionElement
 	 * @param value
 	 * @param sequenceType
 	 */
 	private void updateEMFComboViewer(
 			PropertiesEditionElement propertiesEditionElement,
 			EObject referenceableObject, EObject container, String value,
 			SequenceType sequenceType) {
 		SWTBotHelper.waitAllUiEvents();
 		String realLabel = EMFHelper.getEditorLabel(propertiesEditionElement,
 				referenceableObject, container, sequenceType);
 		final SWTBotCombo comboBoxWithLabel = bot.comboBoxWithLabel(realLabel
 				+ ": ");
 		comboBoxWithLabel.setSelection(value);
 		SWTBotHelper.sendFocusLost(comboBoxWithLabel.widget);
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	/**
 	 * Update widget Checkbox.
 	 * 
 	 * @param elementEditor
 	 * @param value
 	 *            the label of the Checkbox
 	 * @param sequenceType
 	 */
 	private void updateCheckbox(ElementEditor elementEditor, String value,
 			SequenceType sequenceType) {
 		SWTBotHelper.waitAllUiEvents();
 		final SWTBotCheckBox checkBox = bot.checkBox(value);
 		checkBox.click();
 		SWTBotHelper.sendFocusLost(checkBox.widget);
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	/**
 	 * Update widget MultiValuedEditor.
 	 * 
 	 * @param propertiesEditionElement
 	 * @param values
 	 * @param sequenceType
 	 */
 	private void updateMultiValuedEditor(
 			PropertiesEditionElement propertiesEditionElement,
 			Collection<String> values, SequenceType sequenceType) {
 		SWTBotHelper.waitAllUiEvents();
 		String label = ((ElementEditor) propertiesEditionElement.getViews()
 				.get(0)).getQualifiedIdentifier();
 		bot.browseButtonMultiValuedEditor(label).click();
 		bot.addValuesInMultiValuedEditor(values);
 		clickOkOrCancel(propertiesEditionElement);
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	/**
 	 * Unset values for widget MultiValuedEditor.
 	 * 
 	 * @param propertiesEditionElement
 	 * @param values
 	 * @param sequenceType
 	 */
 	public void unsetMultiValuedEditor(
 			PropertiesEditionElement propertiesEditionElement,
 			Collection<String> values, SequenceType sequenceType) {
 		SWTBotHelper.waitAllUiEvents();
 		String label = ((ElementEditor) propertiesEditionElement.getViews()
 				.get(0)).getQualifiedIdentifier();
 		bot.browseButtonMultiValuedEditor(label).click();
 		if (values == null || values.isEmpty()) {
 			bot.removeAllValuesInMultiValuedEditor();
 		} else {
 			bot.removeValuesInMultiValuedEditor(values);
 		}
 		clickOkOrCancel(propertiesEditionElement);
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	public void setBot(SWTEEFBot bot) {
 		this.bot = bot;
 	}
 
 	public void addFeature(SWTBotTreeItem selectNode,
 			PropertiesEditionElement propertiesEditionElement,
 			ReferenceableObject referenceableObject, SequenceType sequenceType) {
 		assertFalse(propertiesEditionElement.getViews().isEmpty());
 		final ElementEditor elementEditor = propertiesEditionElement.getViews()
 				.get(0);
 		final String representationName = elementEditor.getRepresentation()
 				.getName();
 		if ("TableComposition".equals(representationName)) {
 			bot.addButtonTableComposition(
 					elementEditor.getQualifiedIdentifier()).click();
 		} else if ("AdvancedTableComposition".equals(representationName)) {
 			bot.addButtonAdvancedTableComposition(
 					elementEditor.getQualifiedIdentifier()).click();
 		}
 		// Execute the Action of each following siblings of the Add Action in
 		// the Wizard of ModelingBot model
 		if (referenceableObject != null) {
 			if (referenceableObject instanceof Add
 					&& referenceableObject.eContainer() instanceof Wizard) {
 				EStructuralFeature feature = ((Add) referenceableObject)
 						.getEContainingFeature();
 				EClassifier type = feature.getEType();
 				if (type != null && type instanceof EClass
 						&& ((EClass) type).isAbstract()) {
 					SWTBotRadio radio = bot.radio(((Add) referenceableObject).getType().getName());
 					WrappedSWTBotRadio wrappedRadio = new WrappedSWTBotRadio(radio);
 					wrappedRadio.click();
 					bot.button(UIConstants.NEXT_BUTTON).click();
 				}
 //				Collection<EObject> settings = EMFHelper
 //						.followingSiblings(referenceableObject);
 //				for (EObject setting : settings) {
 //					if (setting instanceof Action) {
 //						bot.getModelingBotInterpreter().runAction(
 //								(Action) setting);
 //						if (setting instanceof Cancel) {
 //							EditAction prev = EEFModelingBotHelper
 //									.getCancelPrecedingAction((Cancel) setting);
 //							clickCancel(prev.getPropertiesEditionElement());
 //							return;
 //						}
 //					}
 //				}
 			}
 		}
 		//bot.button(UIConstants.FINISH_BUTTON).click();
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	public void removeFeature(EObject remove,
 			PropertiesEditionElement propertiesEditionElement,
 			SequenceType sequenceType) {
 		assertFalse(propertiesEditionElement.getViews().isEmpty());
 		final ElementEditor elementEditor = propertiesEditionElement.getViews()
 				.get(0);
 		removeFeature(remove, elementEditor);
 	}
 
 	private void removeFeature(EObject remove, ElementEditor elementEditor) {
 		bot.selectInTableWithId(
 				org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY,
 				elementEditor.getQualifiedIdentifier(), remove);
 		final String representationName = elementEditor.getRepresentation()
 				.getName();
 		if ("TableComposition".equals(representationName)) {
 			bot.removeButtonTableComposition(
 					elementEditor.getQualifiedIdentifier()).click();
 		} else if ("AdvancedTableComposition".equals(representationName)) {
 			bot.removeButtonAdvancedTableComposition(
 					elementEditor.getQualifiedIdentifier()).click();
 		}
 	}
 
 	public void unsetReference(
 			PropertiesEditionElement propertiesEditionElement,
 			ReferenceableObject referenceableObject,
 			Collection<EObject> objectsToUnset, SequenceType sequenceType) {
 		final ElementEditor elementEditor = propertiesEditionElement.getViews()
 				.get(0);
 		final String representationName = elementEditor.getRepresentation()
 				.getName();
 		if ("ReferencesTable".equals(representationName)) {
 			unsetReferencesTable(elementEditor, objectsToUnset);
 		} else if ("AdvancedReferencesTable".equals(representationName)) {
 			unsetAdvancedReferencesTable(elementEditor, objectsToUnset);
 		} else if ("FlatReferencesTable".equals(representationName)) {
 			unsetFlatReferencesTable(propertiesEditionElement, objectsToUnset);
 		} else if ("EObjectFlatComboViewer".equals(representationName)) {
 			unsetEObjectFlatComboViewer(elementEditor, objectsToUnset);
 		} else if ("AdvancedEObjectFlatComboViewer".equals(representationName)) {
 			unsetAdvancedEObjectFlatComboViewer(elementEditor);
 		} else if ("Combo".equals(representationName)) {
 			final EObject container = bot
 					.getEObjectFromReferenceableEObject((ReferenceableObject) referenceableObject);
 			assertNotNull("No container is found to launch add action.",
 					container);
 			unsetCombo(propertiesEditionElement, referenceableObject,
 					container, sequenceType);
 		} else {
 			System.out.println("Case not managed in unset : "
 					+ representationName);
 		}
 	}
 
 	/**
 	 * Unset widget EOFCV.
 	 * 
 	 * @param elementEditor
 	 */
 	public void unsetEObjectFlatComboViewer(ElementEditor elementEditor,
 			Collection<EObject> values) {
 		bot.editButtonEObjectFlatComboViewer(
 				elementEditor.getQualifiedIdentifier()).click();
		bot.selectInActiveTable("");
 		bot.button(UIConstants.OK_BUTTON).click();
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	/**
 	 * Unset widget Combo.
 	 * 
 	 * @param propertiesEditionElement
 	 * @param referenceableObject
 	 * @param container
 	 * @param sequenceType
 	 */
 	public void unsetCombo(PropertiesEditionElement propertiesEditionElement,
 			EObject referenceableObject, EObject container,
 			SequenceType sequenceType) {
 		SWTBotHelper.waitAllUiEvents();
 		String realLabel = EMFHelper.getEditorLabel(propertiesEditionElement,
 				referenceableObject, container, sequenceType);
 		final SWTBotCombo comboBoxWithLabel = bot.comboBoxWithLabel(realLabel
 				+ ": ");
 		comboBoxWithLabel.setSelection("");
 		SWTBotHelper.sendFocusLost(comboBoxWithLabel.widget);
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	/**
 	 * Unset widget AdvancedEOFCV.
 	 * 
 	 * @param elementEditor
 	 */
 	public void unsetAdvancedEObjectFlatComboViewer(ElementEditor elementEditor) {
 		bot.removeButtonAdvancedEObjectFlatComboViewer(
 				elementEditor.getQualifiedIdentifier()).click();
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	/**
 	 * Unset widget ReferenceTable.
 	 * 
 	 * @param elementEditor
 	 * @param objectsToUnset
 	 */
 	public void unsetReferencesTable(ElementEditor elementEditor,
 			Collection<EObject> objectsToUnset) {
 		bot.selectInTableWithId(
 				org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY,
 				elementEditor.getQualifiedIdentifier(), objectsToUnset);
 		bot.removeButtonReferencesTable(elementEditor.getQualifiedIdentifier())
 				.click();
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	/**
 	 * Unset widget AdvancedRefrencesTable.
 	 * 
 	 * @param elementEditor
 	 * @param objectsToUnset
 	 */
 	public void unsetAdvancedReferencesTable(ElementEditor elementEditor,
 			Collection<EObject> objectsToUnset) {
 		bot.selectInTableWithId(
 				org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY,
 				elementEditor.getQualifiedIdentifier(), objectsToUnset);
 		bot.removeButtonAdvancedReferencesTable(
 				elementEditor.getQualifiedIdentifier()).click();
 		SWTBotHelper.waitAllUiEvents();
 	}
 
 	private void clickOkOrCancel(
 			PropertiesEditionElement propertiesEditionElement) {
 		SWTBotHelper.waitAllUiEvents();
 		// bot.sleep(1000);
 		if (((EEFInterpreter) bot.getModelingBotInterpreter())
 				.getActionsToCancel().contains(propertiesEditionElement)) {
 			bot.cancel(null);
 			((EEFInterpreter) bot.getModelingBotInterpreter())
 					.getActionsToCancel().remove(propertiesEditionElement);
 		} else {
 			bot.button(UIConstants.OK_BUTTON).click();
 		}
 	}
 
 	private void clickCancel(PropertiesEditionElement propertiesEditionElement) {
 		SWTBotHelper.waitAllUiEvents();
 		// bot.sleep(1000);
 		if (((EEFInterpreter) bot.getModelingBotInterpreter())
 				.getActionsToCancel().contains(propertiesEditionElement)) {
 			bot.cancel(null);
 			((EEFInterpreter) bot.getModelingBotInterpreter())
 					.getActionsToCancel().remove(propertiesEditionElement);
 		}
 	}
 
 	public void unsetAttribute(
 			PropertiesEditionElement propertiesEditionElement,
 			ReferenceableObject referenceableObject, EObject container,
 			Collection<String> values, SequenceType sequenceType) {
 		final ElementEditor elementEditor = propertiesEditionElement.getViews()
 				.get(0);
 		final String representationName = elementEditor.getRepresentation()
 				.getName();
 		if ("Text".equals(representationName)
 				|| "Textarea".equals(representationName)) {
 			updateText(propertiesEditionElement, referenceableObject,
 					container, "", sequenceType);
 		} else if ("MultiValuedEditor".equals(representationName)) {
 			unsetMultiValuedEditor(propertiesEditionElement, values,
 					sequenceType);
 		} else {
 			System.out.println("Case not managed in unset : "
 					+ representationName);
 		}
 
 	}
 }
