 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
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
 import java.util.List;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.databinding.observable.value.ComputedValue;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 import org.eclipse.riena.beans.common.TypedComparator;
 import org.eclipse.riena.beans.common.WordNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.IToggleButtonRidget;
 import org.eclipse.riena.ui.ridgets.listener.ISelectionListener;
 import org.eclipse.riena.ui.ridgets.listener.SelectionEvent;
 import org.eclipse.riena.ui.ridgets.swt.ColumnFormatter;
 import org.eclipse.riena.ui.ridgets.swt.NumberColumnFormatter;
 import org.eclipse.riena.ui.swt.StatusMeter;
 
 /**
  * Controller for the {@link TableSubModuleView} example.
  */
 public class TableSubModuleController extends SubModuleController {
 
 	private IActionRidget buttonRename;
 	private ITableRidget table;
 	private List<WordNode> input;
 
 	private Color green;
 	private Color darkGreen;
 
 	public TableSubModuleController() {
 		this(null);
 
 		final Display display = Display.getCurrent();
 		green = display.getSystemColor(SWT.COLOR_GREEN);
 		darkGreen = display.getSystemColor(SWT.COLOR_DARK_GREEN);
 	}
 
 	public TableSubModuleController(final ISubModuleNode navigationNode) {
 		super(navigationNode);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.controllers.SubModuleController#afterBind()
 	 */
 	@Override
 	public void afterBind() {
 		super.afterBind();
 		// bindModel();
 	}
 
 	private void bindModel() {
 		input = createInput();
 		final String[] columnPropertyNames = { "AQuota", "word", "upperCase", "ACount", "AQuota" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 		final String[] columnHeaders = { "S", "Word", "Uppercase", "A Count", "A Quota [%]" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 		table.bindToModel(new WritableList(input, WordNode.class), WordNode.class, columnPropertyNames, columnHeaders);
 		table.updateFromModel();
 		table.setComparator(1, new TypedComparator<String>());
 		table.setComparator(2, new TypedComparator<Boolean>());
 		table.setColumnFormatter(4, new NumberColumnFormatter(Float.class, 2) {
 			@Override
 			protected Number getValue(final Object element) {
 				return ((WordNode) element).getAQuota();
 			}
 		});
 		table.setColumnSortable(3, false);
 		table.setSelectionType(ISelectableRidget.SelectionType.SINGLE);
 		table.setSelection(0);
 
 		table.setColumnFormatter(0, new ColumnFormatter() {
 			@Override
 			public String getText(final Object element) {
 				return ""; //$NON-NLS-1$
 			}
 
 			@Override
 			public Image getImage(final Object element) {
 				if (((WordNode) element).isUpperCase()) {
					return new Image(Display.getCurrent(), StatusMeter.imageFinished().width(16)
							.gradientStartColor(green).gradientEndColor(darkGreen).getImageData());
 				}
				final int value = (int) ((WordNode) element).getAQuota();
				return new Image(Display.getCurrent(), StatusMeter.imageDefault().width(16).value(value).getImageData());

 			}
 		});
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IRidgetContainer#configureRidgets()
 	 */
 	@Override
 	public void configureRidgets() {
 		table = getRidget(ITableRidget.class, "table"); //$NON-NLS-1$
 		final IToggleButtonRidget buttonPrintSelection = getRidget(IToggleButtonRidget.class, "buttonPrintSelection"); //$NON-NLS-1$
 		final IActionRidget buttonAddSibling = getRidget(IActionRidget.class, "buttonAddSibling"); //$NON-NLS-1$
 		buttonRename = getRidget(IActionRidget.class, "buttonRename"); //$NON-NLS-1$
 		final IActionRidget buttonDelete = getRidget(IActionRidget.class, "buttonDelete"); //$NON-NLS-1$
 
 		table.addDoubleClickListener(new IActionListener() {
 			public void callback() {
 				final WordNode node = (WordNode) table.getSingleSelectionObservable().getValue();
 				if (node != null) {
 					final boolean isUpperCase = !node.isUpperCase();
 					node.setUpperCase(isUpperCase);
 				}
 			}
 		});
 
 		table.addSelectionListener(new ISelectionListener() {
 			public void ridgetSelected(final SelectionEvent event) {
 				if (buttonPrintSelection.isSelected()) {
 					System.out.println(event);
 				}
 			}
 		});
 
 		buttonPrintSelection.setText("&Echo Selection"); //$NON-NLS-1$
 		buttonPrintSelection.setSelected(true);
 
 		buttonAddSibling.setText("&Add"); //$NON-NLS-1$
 		buttonAddSibling.addListener(new IActionListener() {
 			public void callback() {
 				final WordNode newNode = new WordNode("A_NEW_SIBLING"); //$NON-NLS-1$
 				input.add(newNode);
 				table.updateFromModel();
 				table.setSelection(newNode);
 			}
 		});
 
 		buttonRename.setText("&Modify"); //$NON-NLS-1$
 		buttonRename.addListener(new IActionListener() {
 			public void callback() {
 				final WordNode node = (WordNode) table.getSingleSelectionObservable().getValue();
 				if (node != null) {
 					final String newValue = getNewValue(node.getWordIgnoreUppercase());
 					if (newValue != null) {
 						node.setWord(newValue);
 					}
 				}
 			}
 		});
 
 		buttonDelete.setText("&Delete"); //$NON-NLS-1$
 		buttonDelete.addListener(new IActionListener() {
 			public void callback() {
 				final WordNode node = (WordNode) table.getSingleSelectionObservable().getValue();
 				input.remove(node);
 				table.updateFromModel();
 			}
 		});
 
 		final IObservableValue viewerSelection = table.getSingleSelectionObservable();
 		final IObservableValue hasSelection = new ComputedValue(Boolean.TYPE) {
 			@Override
 			protected Object calculate() {
 				return Boolean.valueOf(viewerSelection.getValue() != null);
 			}
 		};
 		final DataBindingContext dbc = new DataBindingContext();
 		bindEnablementToValue(dbc, buttonDelete, hasSelection);
 		bindEnablementToValue(dbc, buttonRename, hasSelection);
 
 		bindModel();
 	}
 
 	private void bindEnablementToValue(final DataBindingContext dbc, final IRidget ridget, final IObservableValue value) {
 		dbc.bindValue(BeansObservables.observeValue(ridget, IRidget.PROPERTY_ENABLED), value, null, null);
 	}
 
 	private String getNewValue(final Object oldValue) {
 		String newValue = null;
 		if (oldValue != null) {
 			final Shell shell = ((Button) buttonRename.getUIControl()).getShell();
 			final IInputValidator validator = new IInputValidator() {
 				public String isValid(final String newText) {
 					final boolean isValid = newText.trim().length() > 0;
 					return isValid ? null : "Word cannot be empty!"; //$NON-NLS-1$
 				}
 			};
 			final InputDialog dialog = new InputDialog(shell, "Modify", "Enter a new word:", String.valueOf(oldValue), //$NON-NLS-1$ //$NON-NLS-2$
 					validator);
 			final int result = dialog.open();
 			if (result == Window.OK) {
 				newValue = dialog.getValue();
 			}
 		}
 		return newValue;
 	}
 
 	private List<WordNode> createInput() {
 		final String[] words = { "Adventure", "Acclimatisation", "Aardwark", "Binoculars", "Beverage", "Boredom", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
 				"Ballistics", "Calculation", "Coexistence", "Cinnamon", "Celebration", "Disney", "Dictionary", "Delta", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
 				"Desperate", "Elf", "Electronics", "Elwood", "Enemy" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 		final ArrayList<WordNode> result = new ArrayList<WordNode>(words.length);
 		for (final String word : words) {
 			final WordNode node = new WordNode(word);
 			result.add(node);
 		}
 		result.get(0).setUpperCase(true);
 		result.get(1).setUpperCase(true);
 		return result;
 	}
 
 }
