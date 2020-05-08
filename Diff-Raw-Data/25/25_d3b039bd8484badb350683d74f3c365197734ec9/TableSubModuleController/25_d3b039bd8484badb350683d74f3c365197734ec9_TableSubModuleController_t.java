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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.databinding.observable.value.ComputedValue;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.window.Window;
import org.eclipse.riena.beans.common.TypedComparator;
 import org.eclipse.riena.beans.common.WordNode;
 import org.eclipse.riena.example.client.views.TableSubModuleView;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * Controller for the {@link TableSubModuleView} example.
  */
 public class TableSubModuleController extends SubModuleController {
 
 	private IActionRidget buttonRename;
 	private ITableRidget table;
 	private List<WordNode> input;
 
 	public TableSubModuleController() {
 		this(null);
 	}
 
 	public TableSubModuleController(ISubModuleNode navigationNode) {
 		super(navigationNode);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.controllers.SubModuleController#afterBind()
 	 */
 	@Override
 	public void afterBind() {
 		super.afterBind();
 		bindModel();
 	}
 
 	private void bindModel() {
 		input = createInput();
 		String[] columnPropertyNames = { "word", "upperCase", "ACount" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		String[] columnHeaders = { "Word", "Uppercase", "A Count" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		table.bindToModel(new WritableList(input, WordNode.class), WordNode.class, columnPropertyNames, columnHeaders);
		table.setComparator(0, new TypedComparator<String>());
		table.setComparator(1, new TypedComparator<Boolean>());
 		table.setColumnSortable(2, false);
 		table.setSelectionType(ISelectableRidget.SelectionType.SINGLE);
 		table.setSelection(0);
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IRidgetContainer#configureRidgets()
 	 */
 	public void configureRidgets() {
 
 		table = (ITableRidget) getRidget("table"); //$NON-NLS-1$
 		final IActionRidget buttonAddSibling = (IActionRidget) getRidget("buttonAddSibling"); //$NON-NLS-1$
 		buttonRename = (IActionRidget) getRidget("buttonRename"); //$NON-NLS-1$
 		final IActionRidget buttonDelete = (IActionRidget) getRidget("buttonDelete"); //$NON-NLS-1$
 
 		table.addDoubleClickListener(new IActionListener() {
 			public void callback() {
 				WordNode node = (WordNode) table.getSingleSelectionObservable().getValue();
 				if (node != null) {
 					boolean isUpperCase = !node.isUpperCase();
 					node.setUpperCase(isUpperCase);
 				}
 			}
 		});
 
 		buttonAddSibling.setText("&Add"); //$NON-NLS-1$
 		buttonAddSibling.addListener(new IActionListener() {
 			public void callback() {
 				WordNode newNode = new WordNode("A_NEW_SIBLING"); //$NON-NLS-1$
 				input.add(newNode);
 				table.updateFromModel();
 				table.setSelection(newNode);
 			}
 		});
 
 		buttonRename.setText("&Modify"); //$NON-NLS-1$
 		buttonRename.addListener(new IActionListener() {
 			public void callback() {
 				WordNode node = (WordNode) table.getSingleSelectionObservable().getValue();
 				if (node != null) {
 					String newValue = getNewValue(node.getWordIgnoreUppercase());
 					if (newValue != null) {
 						node.setWord(newValue);
 					}
 				}
 			}
 		});
 
 		buttonDelete.setText("&Delete"); //$NON-NLS-1$
 		buttonDelete.addListener(new IActionListener() {
 			public void callback() {
 				WordNode node = (WordNode) table.getSingleSelectionObservable().getValue();
 				input.remove(node);
 				table.updateFromModel();
 			}
 		});
 
 		final IObservableValue viewerSelection = table.getSingleSelectionObservable();
 		IObservableValue hasSelection = new ComputedValue(Boolean.TYPE) {
 			@Override
 			protected Object calculate() {
 				return Boolean.valueOf(viewerSelection.getValue() != null);
 			}
 		};
 		DataBindingContext dbc = new DataBindingContext();
 		bindEnablementToValue(dbc, buttonDelete, hasSelection);
 		bindEnablementToValue(dbc, buttonRename, hasSelection);
 	}
 
 	private void bindEnablementToValue(DataBindingContext dbc, IMarkableRidget ridget, IObservableValue value) {
 		dbc.bindValue(BeansObservables.observeValue(ridget, IMarkableRidget.PROPERTY_ENABLED), value, null, null);
 	}
 
 	private String getNewValue(Object oldValue) {
 		String newValue = null;
 		if (oldValue != null) {
 			Shell shell = ((Button) buttonRename.getUIControl()).getShell();
 			IInputValidator validator = new IInputValidator() {
 				public String isValid(String newText) {
 					boolean isValid = newText.trim().length() > 0;
 					return isValid ? null : "Word cannot be empty!"; //$NON-NLS-1$
 				}
 			};
 			InputDialog dialog = new InputDialog(shell, "Modify", "Enter a new word:", String.valueOf(oldValue), //$NON-NLS-1$ //$NON-NLS-2$
 					validator);
 			int result = dialog.open();
 			if (result == Window.OK) {
 				newValue = dialog.getValue();
 			}
 		}
 		return newValue;
 	}
 
 	private List<WordNode> createInput() {
 		String[] words = { "Adventure", "Acclimatisation", "Aardwark", "Binoculars", "Beverage", "Boredom", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
 				"Ballistics", "Calculation", "Coexistence", "Cinnamon", "Celebration", "Disney", "Dictionary", "Delta", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
 				"Desperate", "Elf", "Electronics", "Elwood", "Enemy" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 		ArrayList<WordNode> result = new ArrayList<WordNode>(words.length);
 		for (int i = 0; i < words.length; i++) {
 			WordNode node = new WordNode(words[i]);
 			result.add(node);
 		}
 		result.get(0).setUpperCase(true);
 		result.get(1).setUpperCase(true);
 		return result;
 	}
 
 }
