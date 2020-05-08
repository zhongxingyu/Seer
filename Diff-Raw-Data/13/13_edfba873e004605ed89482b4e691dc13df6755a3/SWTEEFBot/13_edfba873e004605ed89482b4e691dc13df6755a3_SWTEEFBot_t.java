 /*******************************************************************************
  * Copyright (c) 2008, 2010 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.tests.swtbot.finder;
 
 import static org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable.syncExec;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.ui.EMFEditUIPlugin;
 import org.eclipse.emf.eef.runtime.EEFRuntimePlugin;
 import org.eclipse.emf.eef.runtime.tests.exceptions.WidgetInvalidException;
 import org.eclipse.emf.eef.runtime.tests.swtbot.utils.SWTBotUtils;
 import org.eclipse.emf.eef.runtime.tests.utils.EEFTestsResourceUtils;
 import org.eclipse.emf.eef.runtime.tests.utils.UIConstants;
 import org.eclipse.emf.eef.runtime.ui.utils.EEFRuntimeUIMessages;
 import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
 import org.eclipse.swtbot.swt.finder.SWTBot;
 import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
 import org.eclipse.swtbot.swt.finder.results.VoidResult;
 import org.eclipse.swtbot.swt.finder.waits.Conditions;
 import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class SWTEEFBot extends SWTWorkbenchBot {
 	
 	private SWTEEFBotHelper helper;
 	
 	/**
 	 * Default constructor 
 	 */
 	public SWTEEFBot() {
 		super();
 		this.helper = new SWTEEFBotHelper(this);
 	}
 
 	/*****************************************************************************
 	 * * Bot members * *
 	 *****************************************************************************/
 
 	private Resource activeResource = null;
 
 	/**
 	 * @return the URI of the input model
 	 */
 	public String getInputModelURI() {
 		if (activeResource != null)
 			return activeResource.getURI().toString();
 		return null;
 	}
 
 	/**
 	 * @return the active resource
 	 */
 	public Resource getActiveResource() {
 		return activeResource;
 	}
 
 	/*****************************************************************************
 	 * * Bot settings * *
 	 *****************************************************************************/
 
 	public void defineActiveModel(Resource resource) {
 		this.activeResource = resource;
 	}
 
 	public void unloadActiveModel() {
 		this.activeResource.unload();
 	}
 
 	public void reloadActiveModel() throws IOException {
 		this.activeResource.unload();
 		this.activeResource.load(Collections.EMPTY_MAP);
 	}
 
 	/*****************************************************************************
 	 * * Bot operation * *
 	 *****************************************************************************/
 
 	/**
 	 * Get another literal value of the enumeration
 	 */
 	public Object changeEnumLiteralValue(EEnum enumeration, String text) {
 		for (EEnumLiteral eEnumLiteral : enumeration.getELiterals()) {
 			if (!eEnumLiteral.getLiteral().equals(text.toString())) {
 				return eEnumLiteral.getLiteral();
 			}
 		}
 		return text;
 	}
 
 	/**
 	 * Get another reference value of the references list
 	 */
 	public Object changeReferenceValue(List<EObject> eObjectList, EObject value) {
 		for (EObject eobj : eObjectList) {
 			if (!eobj.equals(value)) {
 				return eobj;
 			}
 		}
 		return value;
 	}
 
 	/**
 	 * Get another reference value of the references list
 	 */
 	public Object changeReferenceValue(List<EObject> eObjectList, List values) {
 		for (EObject eobj : eObjectList) {
 			if (!values.contains(eobj)) {
 				return eobj;
 			}
 		}
 		if (!values.isEmpty()) {
 			return values.get(0);
 		}
 		return null;
 	}
 
 	/**
 	 * Must open the file with EEF Reflexive editor -> SWTBot pb with context menu
 	 * @param file
 	 *            the file to open
 	 * @return the editor editing the file
 	 */
 	public SWTBotEditor openFile(IFile file) {
 		SWTBotTree wizardTree = viewByTitle(UIConstants.PACKAGE_EXPLORER_VIEW_NAME).bot().tree();
 		List<IResource> expansionPath = getExpansionPath(file);
 		Iterator<IResource> iter = expansionPath.iterator();
 		if (iter.hasNext()) {
 			String text = getNodeText(iter.next());
 			SWTBotTreeItem treeItem = wizardTree.expandNode(text).expand();
 			while (iter.hasNext()) {
 				text = getNodeText(iter.next());
 				treeItem = treeItem.getNode(text);
 				treeItem.expand();
 			}
 			treeItem.select();
 			menu(UIConstants.NAVIGATE_MENU).menu(UIConstants.OPEN_MENU).click();
 			SWTBotEditor editor = editorByTitle(activeResource.getURI().lastSegment());
 			return editor;
 		}
 		return null;
 	}
 
 	/**
 	 * Open the editor on the input model
 	 * 
 	 * @return the associated editor
 	 */
 	public SWTBotEditor openActiveModel() {
 		IFile file = EEFTestsResourceUtils.workspaceFile(activeResource);
 		return openFile(file);
 	}
 
 	/**
 	 * Prepare the model editing
 	 * 
 	 * @param elementType
 	 *            The EClass of the edited element
 	 * @param element
 	 *            the element to edit
 	 * @return the shell of the opened wizard
 	 */
 	public SWTBotShell prepareBatchEditing(SWTBotEditor editor, EClass elementType, EObject element, String tabName) {
 		SWTBotTreeItem node2 = selectNode(editor, element);
 		node2.doubleClick();
		if (tabName != null) {
			cTabItem(tabName).activate();
			cTabItem(tabName).setFocus();
		}
 		return shell(elementType.getName());
 	}
 
 	/**
 	 * Prepare the model editing
 	 * 
 	 * @param editor
 	 *            The EClass of the edited element
 	 * @param element
 	 *            the element to edit
 	 * @return the shell of the opened wizard
 	 */
 	public SWTBotView prepareLiveEditing(SWTBotEditor editor, EObject element, String tabName) {
 		SWTBotTreeItem node2 = selectNode(editor, element);
 		node2.select();
 		SWTBotUtils.selectPropertyTabItem(tabName);
 		return viewByTitle(UIConstants.PROPERTIES_VIEW_NAME);
 	}
 
 	/**
 	 * Select the given element in the given editor
 	 * 
 	 * @param editor
 	 *            the editor where the bot must process
 	 * @param element
 	 *            the element to select
 	 * @return the selected node
 	 */
 	public SWTBotTreeItem selectNode(SWTBotEditor editor, EObject element) {
 		List<Object> expansionPath = getExpansionPath(element);
 		Iterator<Object> iterator = expansionPath.iterator();
 		Object next = null;
 		SWTBotTreeItem node2 = editor.bot().tree().getTreeItem(getInputModelURI());
 		while (iterator.hasNext()) {
 			node2.expand();
 			next = iterator.next();
 			node2 = selectSubNode(node2, next);
 		}
 		return node2;
 	}
 
 	/**
 	 * Select the given element in the given editor
 	 * 
 	 * @param editor
 	 *            the editor where the bot must process
 	 * @param element
 	 *            the element to select
 	 * @return the selected node
 	 */
 	public SWTBotTreeItem selectNode(SWTBotTree tree, EObject element) {
 		List<Object> expansionPath = getExpansionPath(element);
 		Iterator<Object> iterator = expansionPath.iterator();
 		Object next = null;
 		SWTBotTreeItem node2 = tree.getTreeItem(getInputModelURI());
 		while (iterator.hasNext()) {
 			node2.expand();
 			next = iterator.next();
 			node2 = selectSubNode(node2, next);
 		}
 		return node2;
 	}
 
 	/**
 	 * This method save the model and close the editor
 	 * 
 	 * @param editor
 	 *            the modelEditor
 	 */
 	public void finalizeEdition(final SWTBotEditor editor) {
 		activateEclipseShell();
 		menu(UIConstants.FILE_MENU).menu(UIConstants.SAVE_MENU).click();
 		this.waitUntil(new DefaultCondition() {
 			
 			public boolean test() throws Exception {
 				return !editor.isDirty();
 			}
 			
 			public String getFailureMessage() {
 				return "the editor does not save in less than one minute";
 			}
 		}, 60000);
 		editor.close();
 	}
 
 	/*****************************************************************************
 	 * * EEF features editing testing * *
 	 *****************************************************************************/
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 */
 	public void editTextFeature(SWTBotShell shell, String feature, Object newValue) {
 		activateShell(shell);
 		editEEFText(feature, newValue);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the new value to set to the feature
 	 */
 	public void editEMFComboViewerFeature(SWTBotShell shell, String viewID, Object newValue) {
 		activateShell(shell);
 		editEMFComboViewer(viewID, newValue);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the new value to set to the feature
 	 */
 	public void editRadioFeature(SWTBotShell shell, String viewID, Object newValue) {
 		activateShell(shell);
 		editRadio(viewID, newValue);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the new value to set to the feature
 	 */
 	public void editEMFComboViewer(String viewID, Object newValue) {
 		comboBoxWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).setSelection(newValue.toString());
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the new value to set to the feature
 	 */
 	public void editRadio(String viewID, Object newValue) {
 		helper.radioWithIdAndMnemonic(viewID, newValue.toString()).click();
 	}
 
 	/**
 	 * @param shell
 	 */
 	public void activateShell(SWTBotShell shell) {
 		shell.activate();
 	}
 
 	/**
 	 * @param shell
 	 */
 	public void closeShellWithFinishButton(SWTBotShell shell) {
 		button(UIConstants.FINISH_BUTTON).click();
 		waitUntil(Conditions.shellCloses(shell));
 	}
 
 	/**
 	 * @param viewId the ID of the element editor
 	 * @param newValue the new value to set
 	 */
 	public void editEEFText(String viewId, Object newValue) {
 		if (newValue != null) {
 			SWTBotText textWithId = textWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewId);
 			textWithId.setText(newValue.toString());
 			/* our listener wait a focus lost to launch the command */
 //			button(UIConstants.FINISH_BUTTON).setFocus();
 			SWTBotUtils.pressEnterKey(textWithId.widget);
 			SWTBotUtils.sendFocusLost(textWithId.widget);
 			syncExec(new VoidResult() {
 				
 				public void run() {
 					synchronizationWithUIThread();
 				}
 			});
 		}
 	}
 	
 	 /**
      * Wait the end of the asynchronous calls waiting in UI thread.
      */
     public void synchronizationWithUIThread() {
         while (PlatformUI.getWorkbench().getDisplay().readAndDispatch()) {
             // Do nothing, just wait
         }
     }
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param propertyView
 	 *            the properties view
 	 * @param viewID
 	 *            the ID of the ElementEditor
 	 * @param selectNode
 	 *            the SWTBotTreeItem in the treeview model
 	 */
 	public void editPropertyEEFText(SWTBotView propertyView, String viewID, Object newValue, SWTBotTreeItem selectNode) {
 		SWTBot propertyBot = propertyView.bot();
 		SWTBotText elementEditor = propertyBot.textWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID);
 		elementEditor.setFocus();
 		elementEditor.setText(newValue.toString());
 		SWTBotUtils.pressEnterKey(propertyView.getWidget());
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param viewID
 	 *            the ID of the ElementEditor to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 */
 	public void editCheckboxFeature(SWTBotShell shell, String viewID) {
 		activateShell(shell);
 		editEEFCheckbox(viewID);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param tableIndex the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void editEObjectFlatComboViewerFeature(SWTBotShell shell, String viewID, int tableIndex) throws WidgetInvalidException {
 		activateShell(shell);
 		editEObjectFlatComboViewer(viewID, tableIndex);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void editFlatReferencesTableFeature(SWTBotShell shell, String viewID, int newValue) throws WidgetInvalidException {
 		activateShell(shell);
 		editFlatReferencesTable(viewID, newValue);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param tableIndex the new value to set to the feature
 	 * @throws WidgetInvalidException
 	 */
 	public void editFlatReferencesTable(String viewID, int tableIndex) throws WidgetInvalidException {
 		buttonWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).click();
 		try {
 			table().select(tableIndex);
 			button(EMFEditUIPlugin.INSTANCE.getString("_UI_Add_label")).click();
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param tableIndex the element to select
 	 * @throws WidgetInvalidException error during selection
 	 */
 	public void editEObjectFlatComboViewer(String viewID, int tableIndex) throws WidgetInvalidException {
 		buttonWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).click();
 		try {
 			table().select(tableIndex);
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the element to select
 	 * @throws WidgetInvalidException error during selection
 	 */
 	public void editReferencesTableFeature(SWTBotShell shell, String viewID, int newValue) throws WidgetInvalidException {
 		activateShell(shell);
 		editReferencesTable(viewID, newValue);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the element to select
 	 * @throws WidgetInvalidException error during selection
 	 */
 	public void editReferencesTable(String viewID, int newValue) throws WidgetInvalidException {
 		helper.addButtonReferencesTable(viewID).click();
 		try {
 			table().select(newValue);
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the element to select
 	 * @throws WidgetInvalidException error during selection
 	 */
 	public void removeReferencesTableFeature(SWTBotShell shell, String viewID, int newValue) throws WidgetInvalidException {
 		activateShell(shell);
 		removeReferencesTable(viewID, newValue);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the element to select
 	 * @throws WidgetInvalidException error during selection
 	 */
 	public void removeReferencesTable(String viewID, int tableIndex) throws WidgetInvalidException {
 		try {
 			tableWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).select(tableIndex);
 			helper.removeButtonReferencesTable(viewID).click();
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 */
 	public void editAdvancedEObjectFlatComboViewerFeature(SWTBotShell shell, String viewID, Object value) {
 		activateShell(shell);
 		editAdvancedEObjectFlatComboViewer(viewID, value);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 */
 	public void removeAdvancedEObjectFlatComboViewerFeature(SWTBotShell shell, String viewID) {
 		activateShell(shell);
 		removeAdvancedEObjectFlatComboViewer(viewID);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param tableIndex the element to select
 	 * @throws WidgetInvalidException error during selection
 	 */
 	public void removeFlatReferencesTableFeature(SWTBotShell shell, String viewID) throws WidgetInvalidException {
 		activateShell(shell);
 		removeFlatReferencesTable(viewID);
 		closeShellWithFinishButton(shell);
 	}
 	
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @throws WidgetInvalidException error during selection
 	 */
 	public void removeFlatReferencesTable(String viewID) throws WidgetInvalidException {
 		buttonWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).click();
 		try {
 			table(1).select(0);
 			button(EMFEditUIPlugin.INSTANCE.getString("_UI_Remove_label")).click();
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @throws WidgetInvalidException 
 	 */
 	public void removeEObjectFlatComboViewerFeature(SWTBotShell shell, String viewID) throws WidgetInvalidException {
 		activateShell(shell);
 		removeEObjectFlatComboViewer(viewID);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @throws WidgetInvalidException
 	 */
 	public void removeEObjectFlatComboViewer(String viewID) throws WidgetInvalidException {
 		buttonWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).click();
 	try {
 		table().select(0);
 	} catch (Exception e) {
 		// empty table
 		throw new WidgetInvalidException();
 	}
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param tableIndex the element to select
 	 * @throws WidgetInvalidException error during selection
 	 */
 	public void removePropertyFlatReferencesTableFeature(SWTBotView propertyView, String viewID, SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		removeFlatReferencesTable(viewID);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @throws WidgetInvalidException 
 	 */
 	public void removePropertyEObjectFlatComboViewerFeature(SWTBotView propertyView, String viewID, SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		removeEObjectFlatComboViewer(viewID);
 		selectNode.select();
 	}
 
 	/**
 	 * @param viewID the edited advEOFCV
 	 * @return the browse button of the given advEOFCV
 	 */
 	public SWTBotButton browseButtonAdvancedEObjectFlatComboViewer(String viewID) {
 		return helper.browseButtonAdvancedEObjectFlatComboViewer(viewID);
 	}
 	
 	/**
 	 * @param viewID the edited advEOFCV
 	 * @return the remove button of the given advEOFCV
 	 */
 	public SWTBotButton removeButtonAdvancedEObjectFlatComboViewer(String viewID) {
 		return helper.removeButtonAdvancedEObjectFlatComboViewer(viewID);
 	}
 	
 	/**
 	 * @param viewID the edited advEOFCV
 	 * @return the remove button of the given advEOFCV
 	 */
 	public void removeAdvancedEObjectFlatComboViewer(String viewID) {
 		removeButtonAdvancedEObjectFlatComboViewer(viewID).click();
 	}
 	
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the newValue to set
 	 */
 	public void editAdvancedEObjectFlatComboViewer(String viewID, Object newValue) {
 		browseButtonAdvancedEObjectFlatComboViewer(viewID).click();
 		cTabItem(EEFRuntimeUIMessages.TabElementTreeSelectionDialog_all_resources_tab_title).activate();
 		cTabItem(EEFRuntimeUIMessages.TabElementTreeSelectionDialog_all_resources_tab_title).setFocus();
 		selectNode(tree(), (EObject)newValue);
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the value to add to the feature
 	 */
 	public void editAdvancedReferencesTableFeature(SWTBotShell shell, String viewID, Object value) {
 		activateShell(shell);
 		editAdvancedReferencesTable(viewID, value);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the value to add to the feature
 	 */
 	public void editMultiValuedEditorFeature(SWTBotShell shell, String viewID, String newValue) {
 		activateShell(shell);
 		editMultiValuedEditor(viewID, newValue);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the value to add to the feature
 	 */
 	public void editMultiValuedEditor(String viewID, List<String> values) {
 		buttonWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).click();
 		for (String string : values) {
 			text().setText(string);
 			button(EMFEditUIPlugin.INSTANCE.getString("_UI_Add_label")).click();
 		}
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the value to add to the feature
 	 */
 	public void editMultiValuedEditor(String viewID, String newValue) {
 		buttonWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).click();
 		text().setText(newValue);
 		button(EMFEditUIPlugin.INSTANCE.getString("_UI_Add_label")).click();
 		button(UIConstants.OK_BUTTON).click();
 	}
 	
 	/**
 	 * @param viewID the edited advEOFCV
 	 * @return the browse button of the given advEOFCV
 	 */
 	public SWTBotButton addButtonAdvancedReferencesTable(String viewID) {
 		return helper.addButtonAdvancedReferencesTable(viewID);
 	}
 	
 	/**
 	 * @param viewID the edited advEOFCV
 	 * @return the remove button of the given advEOFCV
 	 */
 	public SWTBotButton removeButtonAdvancedReferencesTable(String viewID) {
 		return helper.removeButtonAdvancedReferencesTable(viewID);
 	}
 	
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param value the value to add
 	 */
 	public void editAdvancedReferencesTable(String viewID, Object value) {
 		addButtonAdvancedReferencesTable(viewID).click();
 		cTabItem(EEFRuntimeUIMessages.TabElementTreeSelectionDialog_all_resources_tab_title).activate();
 		cTabItem(EEFRuntimeUIMessages.TabElementTreeSelectionDialog_all_resources_tab_title).setFocus();
 		selectNode(tree(), (EObject)value);
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param value the values to add
 	 */
 	public void editAdvancedReferencesTableValues(String viewID, List values) {
 		for (Object value : values) {
 			editAdvancedReferencesTable(viewID, value);
 		}
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void removeAdvancedReferencesTableFeature(SWTBotShell shell, String viewID, Object value) throws WidgetInvalidException {
 		activateShell(shell);
 		removeAdvancedReferencesTable(viewID, value);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException
 	 */
 	public void removeAdvancedTableCompositionFeature(SWTBotShell shell, String viewID)
 			throws WidgetInvalidException {
 		activateShell(shell);
 		removeAdvancedTableComposition(viewID);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * @param viewID the edited advEOFCV
 	 * @return the browse button of the given advEOFCV
 	 */
 	public SWTBotButton addButtonTableComposition(String viewID) {
 		return helper.addButtonTableComposition(viewID);
 	}
 	
 	
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @throws WidgetInvalidException 
 	 */
 	public void removeTableCompositionFeature(SWTBotShell shell, String viewID) throws WidgetInvalidException {
 		activateShell(shell);
 		removeTableComposition(viewID);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @throws WidgetInvalidException 
 	 */
 	public void removeTableComposition(String viewID) throws WidgetInvalidException {
 		try {
 			tableWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).select(0);
 			helper.removeButtonTableComposition(viewID).click();
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 	}
 
 	/**
 	 * @param viewID the edited advEOFCV
 	 * @return the browse button of the given advEOFCV
 	 */
 	public SWTBotButton addButtonAdvancedTableComposition(String viewID) {
 		return helper.addButtonAdvancedTableComposition(viewID);
 	}
 	
 	/**
 	 * @param viewID the edited advEOFCV
 	 * @return the remove button of the given advEOFCV
 	 */
 	public SWTBotButton removeButtonAdvancedTableComposition(String viewID) {
 		return helper.removeButtonAdvancedTableComposition(viewID);
 	}
 	
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void removePropertyAdvancedTableCompositionFeature(SWTBotView propertyView, String viewID,
 			Object value, SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		removeAdvancedTableComposition(viewID, value);
 		selectNode.select();
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param value the value to remove
 	 * @throws WidgetInvalidException error during widget search
 	 */
 	public void removeAdvancedTableComposition(String viewID, Object value) throws WidgetInvalidException {
 		try {
 			tableWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).select(0);
 			removeButtonAdvancedTableComposition(viewID).click();
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 	}
 
 	/**
 	 * Remove a value from a TableComposition
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @throws WidgetInvalidException
 	 */
 	public void removeAdvancedTableComposition(String viewID) throws WidgetInvalidException {
 		try {
 			tableWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).select(0);
 			removeButtonAdvancedTableComposition(viewID).click();
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param value the value to remove
 	 * @throws WidgetInvalidException error during widget search
 	 */
 	public void removeAdvancedReferencesTable(String viewID, Object value) throws WidgetInvalidException {
 		try {
 			tableWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).select(0);
 			removeButtonAdvancedReferencesTable(viewID).click();
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 */
 	private void editEEFCheckbox(String viewID) {
 		checkBoxWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).click();
 	}
 
 	/**
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the new value to set
 	 */
 	public void editCheckBox(String viewID, boolean newValue) {
 		if (newValue) {
 			checkBoxWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).select();
 		} else {
 			checkBoxWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).deselect();
 		}
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param propertyView the properties view
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param selectNode the SWTBotTreeItem in the treeview model
 	 */
 	public void editPropertyEEFCheckbox(SWTBotView propertyView, String viewID, SWTBotTreeItem selectNode) {
 		SWTBot propertyBot = propertyView.bot();
 		propertyBot.checkBoxWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).click();
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param propertyView the properties view
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the new value to set to the feature
 	 * @param selectNode the SWTBotTreeItem in the treeview model
 	 */
 	public void editPropertyEMFComboViewerFeature(SWTBotView propertyView, String viewID, Object newValue, SWTBotTreeItem selectNode) {
 		SWTBot propertyBot = propertyView.bot();
 		propertyBot.comboBoxWithId(org.eclipse.emf.eef.runtime.ui.UIConstants.EEF_WIDGET_ID_KEY, viewID).setSelection(newValue.toString());
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param propertyView the properties view
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the new value to set to the feature
 	 * @param selectNode the SWTBotTreeItem in the treeview model
 	 * @throws WidgetInvalidException 
 	 */
 	public void editPropertyFlatReferencesTableFeature(SWTBotView propertyView, String viewID, int newValue, SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		editFlatReferencesTable(viewID, newValue);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param tableIndex the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void editPropertyEObjectFlatComboViewerFeature(SWTBotView propertyView, String viewID, int tableIndex, SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		editEObjectFlatComboViewer(viewID, tableIndex);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void editPropertyReferencesTableFeature(SWTBotView propertyView, String viewID, int newValue, SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		editReferencesTable(viewID, newValue);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell the shell of the edited wizard
 	 * @param view the ID of the ElementEditor to edit
 	 * @param newValue the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void removePropertyReferencesTableFeature(SWTBotView propertyView, String viewID, int newValue, SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		removeReferencesTable(viewID, newValue);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @throws WidgetInvalidException 
 	 */
 	public void removePropertyTableCompositionFeature(SWTBotView propertyView, String viewID, SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		removeTableComposition(viewID);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the new value to set to the feature
 	 */
 	public void editPropertyAdvancedEObjectFlatComboViewerFeature(SWTBotView propertyView, String viewID, Object value, SWTBotTreeItem selectNode) {
 		editAdvancedEObjectFlatComboViewer(viewID, value);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit     
 	 * @param newValue
 	 *            the new value to set to the feature
 	 */
 	public void removePropertyAdvancedEObjectFlatComboViewerFeature(SWTBotView propertyView, String viewID, SWTBotTreeItem selectNode) {
 		removeAdvancedEObjectFlatComboViewer(viewID);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param value the value to add to the feature
 	 */
 	public void editPropertyAdvancedReferencesTableFeature(SWTBotView propertyView, String viewID, Object value, SWTBotTreeItem selectNode) {
 		editAdvancedReferencesTable(viewID, value);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell the shell of the edited wizard
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void removePropertyAdvancedReferencesTableFeature(SWTBotView propertyView, String viewID, Object value, SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		removeAdvancedReferencesTable(viewID, value);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param propertyView the properties view
 	 * @param viewID the ID of the ElementEditor to edit
 	 * @param newValue the new value to set to the feature
 	 * @param selectNode
 	 *            the SWTBotTreeItem in the treeview model
 	 */
 	public void editPropertyRadioFeature(SWTBotView propertyView, String viewID, Object newValue, SWTBotTreeItem selectNode) {
 		SWTEEFBotHelper propertyHelper = new SWTEEFBotHelper(propertyView.bot());
 		propertyHelper.radioWithIdAndMnemonic(viewID, newValue.toString()).click();
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param propertyView
 	 *            the properties view
 	 * @param feature
 	 *            the feature to edit
 	 * @param selectNode
 	 *            the SWTBotTreeItem in the treeview model
 	 */
 	public void editPropertyMultiValuedEditorFeature(SWTBotView propertyView, String label, String value,
 			SWTBotTreeItem selectNode) {
 		// SWTBot propertyBot = propertyView.bot();
 		editMultiValuedEditor(label, value);
 		selectNode.select();
 	}
 
 	public void openPropertiesView() {
 		menu(UIConstants.WINDOW_MENU).menu(UIConstants.SHOW_VIEW_MENU).menu(UIConstants.OTHER_MENU).click();
 
 		SWTBotShell shell = shell(UIConstants.SHOW_VIEW_MENU);
 		activateShell(shell);
 
 		SWTBotTree viewSelectionTree = tree();
 		viewSelectionTree.expandNode(UIConstants.GENERAL_MENU).select(UIConstants.PROPERTIES_VIEW_NAME);
 		button(UIConstants.OK_BUTTON).click();
 		waitUntil(Conditions.shellCloses(shell));
 	}
 
 	public void openJavaPerspective() {
 		menu(UIConstants.WINDOW_MENU).menu(UIConstants.OPEN_PERSPECTIVE_MENU).menu(UIConstants.OTHER_MENU)
 				.click();
 		SWTBotShell openPerspectiveShell = shell(UIConstants.OPEN_PERSPECTIVE_MENU);
 		activateShell(openPerspectiveShell);
 
 		table().select(UIConstants.JAVA_LABEL);
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	/**
 	 * This method close the welcome page if we use the workspace of test for the first time
 	 */
 	public void closeWelcomePage() {
 		try {
 			viewByTitle(UIConstants.WELCOME_LABEL).close();
 		} catch (WidgetNotFoundException e) {
 			// do nothing
 		}
 	}
 
 	/*****************************************************************************
 	 * * Bot utils * *
 	 *****************************************************************************/
 
 	/**
 	 * Active the main shell (Workbench shell)
 	 */
 	protected void activateEclipseShell() {
 		SWTBotShell[] shells = shells();
 		if (shells.length > 0)
 			// Heuristic : the eclipse shell is the first shell
 			shells[0].activate();
 	}
 
 	/**
 	 * @param modelElement
 	 *            the element to process
 	 * @return a list containing the element to expand
 	 */
 	private List<Object> getExpansionPath(EObject modelElement) {
 		List<Object> result = new ArrayList<Object>();
 		result.add(modelElement);
 		EObject container = modelElement.eContainer();
 		while (container != null) {
 			result.add(0, container);
 			container = container.eContainer();
 		}
 		return result;
 	}
 
 	/**
 	 * @param file
 	 *            the file to process
 	 * @return a list containing the element to expand
 	 */
 	private List<IResource> getExpansionPath(IFile file) {
 		List<IResource> result = new ArrayList<IResource>();
 		result.add(file);
 		IContainer container = file.getParent();
 		while (container != null && !(container instanceof IProject)) {
 			result.add(0, container);
 			container = container.getParent();
 		}
 		if (container instanceof IProject)
 			result.add(0, container);
 		return result;
 	}
 
 	/**
 	 * Return the label of the given IResource
 	 * 
 	 * @param resource
 	 *            the resource to process
 	 * @return the label of the resource
 	 */
 	private String getNodeText(IResource resource) {
 		return resource.getName();
 	}
 
 	/**
 	 * Select the object <code>next</code> as a subnode of the <code>currentNode</code>
 	 * 
 	 * @param currentNode
 	 *            the currentNode
 	 * @param next
 	 *            the subnode to select
 	 * @return the selected node
 	 */
 	private SWTBotTreeItem selectSubNode(SWTBotTreeItem currentNode, Object next) {
 		AdapterFactory adapterFactory = EEFRuntimePlugin.getDefault().getAdapterFactory();
 		IItemLabelProvider labelProvider = (IItemLabelProvider)adapterFactory.adapt(next,
 				IItemLabelProvider.class);
 		String text = labelProvider.getText(next);
 		SWTBotTreeItem node2 = currentNode.getNode(text);
 		node2.select();
 		return node2;
 	}
 }
