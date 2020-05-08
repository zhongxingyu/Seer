 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.validators.internal.ui;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.internal.ui.util.SWTUtil;
 import org.eclipse.dltk.internal.ui.util.TableLayoutComposite;
 import org.eclipse.dltk.ui.util.PixelConverter;
 import org.eclipse.dltk.validators.core.IValidator;
 import org.eclipse.dltk.validators.core.IValidatorType;
 import org.eclipse.dltk.validators.core.ValidatorRuntime;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 
 /**
  * A composite that displays installed InterpreterEnvironment's in a table.
  * InterpreterEnvironments can be added, removed, edited, and searched for.
  * <p>
  * This block implements ISelectionProvider - it sends selection change events
  * when the checked InterpreterEnvironment in the table changes, or when the
  * "use default" button check state changes.
  * </p>
  */
 public class ValidatorBlock implements ISelectionProvider,
 		IAddValidatorDialogRequestor {
 
 	/**
 	 * This block's control
 	 */
 	private Composite fControl;
 
 	/**
 	 * Interpreters being displayed
 	 */
 	protected List fValidator = new ArrayList();
 
 	/**
 	 * The main list control
 	 */
 	protected CheckboxTableViewer fValidatorList;
 
 	// Action buttons
 	private Button fAddButton;
 	private Button fRemoveButton;
 	private Button fEditButton;
 	private Button fCopyButton;
 	// private Button fSearchButton;
 
 	// index of column used for sorting
 	private int fSortColumn = 0;
 
 	/**
 	 * Selection listeners (checked InterpreterEnvironment changes)
 	 */
 	private ListenerList fSelectionListeners = new ListenerList();
 
 	/**
 	 * Previous selection
 	 */
 	private ISelection fPrevSelection = new StructuredSelection();
 
 	private Table fTable;
 
 	/**
 	 * Content provider to show a list of InterpreterEnvironments
 	 */
 	class ValidatorContentProvider implements IStructuredContentProvider {
 		public Object[] getElements(Object input) {
 			return fValidator.toArray();
 		}
 
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		}
 
 		public void dispose() {
 		}
 	}
 
 	/**
 	 * Label provider for installed InterpreterEnvironments table.
 	 */
 	class ValidatorLabelProvider extends LabelProvider implements
 			ITableLabelProvider {
 
 		/**
 		 * @see ITableLabelProvider#getColumnText(Object, int)
 		 */
 		public String getColumnText(Object element, int columnIndex) {
 			if (element instanceof IValidator) {
 				IValidator validator = (IValidator) element;
 				switch (columnIndex) {
 				case 0:
 					return validator.getName();
 				case 1:
 					return validator.getValidatorType().getName();
 				case 2:
 					String nature = validator.getValidatorType().getNature();
 					if (nature == "#") {
 						return "All";
 					}
 					try {
 						IDLTKLanguageToolkit languageToolkit = DLTKLanguageManager
 								.getLanguageToolkit(nature);
 						if (languageToolkit != null) {
 							return languageToolkit.getLanguageName();
 						}
 					} catch (CoreException e) {
 					}
 					return "Unknown";
 				}
 			}
 			return element.toString();
 		}
 
 		/**
 		 * @see ITableLabelProvider#getColumnImage(Object, int)
 		 */
 		public Image getColumnImage(Object element, int columnIndex) {
 			if (columnIndex == 0) {
 				// TODO: instert interpreter logo here
 			}
 			return null;
 		}
 
 	}
 
 	public ValidatorBlock() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		fSelectionListeners.add(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
 	 */
 	public ISelection getSelection() {
 		return new StructuredSelection(fValidatorList.getCheckedElements());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void removeSelectionChangedListener(
 			ISelectionChangedListener listener) {
 		fSelectionListeners.remove(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
 	 */
 	public void setSelection(ISelection selection) {
 		if (selection instanceof IStructuredSelection) {
 			if (!selection.equals(fPrevSelection)) {
 				fPrevSelection = selection;
				Object interp = ((IStructuredSelection) selection)
						.getFirstElement();
				if (interp == null) {
					fValidatorList.setCheckedElements(new Object[0]);
				} else {
					fValidatorList.setCheckedElements(new Object[] { interp });
					fValidatorList.reveal(interp);
				}
 				fireSelectionChanged();
 			}
 		}
 	}
 
 	/**
 	 * Creates this block's control in the given control.
 	 * 
 	 * @param ancestor
 	 *            containing control
 	 * @param useManageButton
 	 *            whether to present a single 'manage...' button to the user
 	 *            that opens the installed InterpreterEnvironments pref page for
 	 *            InterpreterEnvironment management, or to provide 'add, remove,
 	 *            edit, and search' buttons.
 	 */
 	public void createControl(Composite ancestor) {
 
 		Composite parent = new Composite(ancestor, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 
 		Font font = ancestor.getFont();
 		parent.setFont(font);
 		parent.setLayout(layout);
 
 		fControl = parent;
 
 		GridData data;
 
 		Label tableLabel = new Label(parent, SWT.NONE);
 		tableLabel.setText(ValidatorMessages.InstalledValidatorBlock_15);
 		data = new GridData();
 		data.horizontalSpan = 2;
 		tableLabel.setLayoutData(data);
 		tableLabel.setFont(font);
 
 		PixelConverter conv = new PixelConverter(parent);
 		data = new GridData(GridData.FILL_BOTH);
 		data.widthHint = conv.convertWidthInCharsToPixels(50);
 		TableLayoutComposite tblComposite = new TableLayoutComposite(parent,
 				SWT.NONE);
 		tblComposite.setLayoutData(data);
 		fTable = new Table(tblComposite, SWT.CHECK | SWT.BORDER | SWT.MULTI
 				| SWT.FULL_SELECTION);
 
 		data = new GridData(GridData.FILL_BOTH);
 		data.widthHint = 450;
 		fTable.setLayoutData(data);
 		fTable.setFont(font);
 
 		fTable.setHeaderVisible(true);
 		fTable.setLinesVisible(true);
 
 		TableColumn column1 = new TableColumn(fTable, SWT.NULL);
 		column1.setText(ValidatorMessages.InstalledValidatorBlock_0);
 		column1.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				sortByName();
 			}
 		});
 
 		TableColumn column2 = new TableColumn(fTable, SWT.NULL);
 		column2.setText(ValidatorMessages.InstalledValidatorBlock_2);
 		column2.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				sortByType();
 			}
 		});
 
 		TableColumn column3 = new TableColumn(fTable, SWT.NULL);
 		column3.setText(ValidatorMessages.InstalledValidatorBlock_1);
 		// column3.addSelectionListener(new SelectionAdapter() {
 		// public void widgetSelected(SelectionEvent e) {
 		// sortByLocation();
 		// }
 		// });
 
 		fValidatorList = new CheckboxTableViewer(fTable);
 		fValidatorList.setLabelProvider(new ValidatorLabelProvider());
 		fValidatorList.setContentProvider(new ValidatorContentProvider());
 		fValidatorList.addCheckStateListener(new ICheckStateListener() {
 			public void checkStateChanged(CheckStateChangedEvent event) {
 				IValidator validator = (IValidator)event.getElement();
 				validator.setActive(event.getChecked());
 			}
 		});
 		
 		// by default, sort by name
 		sortByName();
 
 		fValidatorList
 				.addSelectionChangedListener(new ISelectionChangedListener() {
 					public void selectionChanged(SelectionChangedEvent evt) {
 						enableButtons();
 					}
 				});
 
 		// fValidatorList.addCheckStateListener(new ICheckStateListener() {
 		// public void checkStateChanged(CheckStateChangedEvent event) {
 		// if (event.getChecked()) {
 		// setCheckedInterpreter((IValidator)event.getElement());
 		// } else {
 		// setCheckedInterpreter(null);
 		// }
 		// }
 		// });
 
 		fValidatorList.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent e) {
 				if (!fValidatorList.getSelection().isEmpty()) {
 					editValidator();
 				}
 			}
 		});
 		fTable.addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent event) {
 				if (event.character == SWT.DEL && event.stateMask == 0) {
 					if (fRemoveButton.getEnabled())
 						removeValidator();
 				}
 			}
 		});
 
 		Composite buttons = new Composite(parent, SWT.NULL);
 		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
 		layout = new GridLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		buttons.setLayout(layout);
 		buttons.setFont(font);
 
 		fAddButton = createPushButton(buttons,
 				ValidatorMessages.InstalledValidatorBlock_3);
 		fAddButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event evt) {
 				addValidator();
 			}
 		});
 
 		fEditButton = createPushButton(buttons,
 				ValidatorMessages.InstalledValidatorBlock_4);
 		fEditButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event evt) {
 				editValidator();
 			}
 		});
 
 		fCopyButton = createPushButton(buttons,
 				ValidatorMessages.InstalledValidatorBlock_16);
 		fCopyButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event evt) {
 				copyValidator();
 			}
 		});
 
 		fRemoveButton = createPushButton(buttons,
 				ValidatorMessages.InstalledValidatorBlock_5);
 		fRemoveButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event evt) {
 				removeValidator();
 			}
 		});
 
 		// copied from ListDialogField.CreateSeparator()
 		Label separator = new Label(buttons, SWT.NONE);
 		separator.setVisible(false);
 		GridData gd = new GridData();
 		gd.horizontalAlignment = GridData.FILL;
 		gd.verticalAlignment = GridData.BEGINNING;
 		gd.heightHint = 4;
 		separator.setLayoutData(gd);
 
 		// fSearchButton = createPushButton(buttons,
 		// ValidatorMessages.InstalledValidatorBlock_6);
 		// fSearchButton.addListener(SWT.Selection, new Listener() {
 		// public void handleEvent(Event evt) {
 		// search();
 		// }
 		// });
 
 		fillWithWorkspaceValidator();
 		enableButtons();
 		fAddButton.setEnabled(ValidatorRuntime
 				.getValidatorTypes(/* getCurrentNature() */).length > 0);
 	}
 
 	/**
 	 * Fire current selection
 	 */
 	private void fireSelectionChanged() {
 		SelectionChangedEvent event = new SelectionChangedEvent(this,
 				getSelection());
 		Object[] listeners = fSelectionListeners.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			ISelectionChangedListener listener = (ISelectionChangedListener) listeners[i];
 			listener.selectionChanged(event);
 		}
 	}
 
 	/**
 	 * Sorts by Interpreter type, and name within type.
 	 */
 	private void sortByType() {
 		fValidatorList.setSorter(new ViewerSorter() {
 			public int compare(Viewer viewer, Object e1, Object e2) {
 				if ((e1 instanceof IValidator) && (e2 instanceof IValidator)) {
 					IValidator left = (IValidator) e1;
 					IValidator right = (IValidator) e2;
 					String leftType = left.getValidatorType().getName();
 					String rightType = right.getValidatorType().getName();
 					int res = leftType.compareToIgnoreCase(rightType);
 					if (res != 0) {
 						return res;
 					}
 					return left.getName().compareToIgnoreCase(right.getName());
 				}
 				return super.compare(viewer, e1, e2);
 			}
 
 			public boolean isSorterProperty(Object element, String property) {
 				return true;
 			}
 		});
 		fSortColumn = 3;
 	}
 
 	/**
 	 * Sorts by Interpreter name.
 	 */
 	private void sortByName() {
 		fValidatorList.setSorter(new ViewerSorter() {
 			public int compare(Viewer viewer, Object e1, Object e2) {
 				if ((e1 instanceof IValidator) && (e2 instanceof IValidator)) {
 					IValidator left = (IValidator) e1;
 					IValidator right = (IValidator) e2;
 					return left.getName().compareToIgnoreCase(right.getName());
 				}
 				return super.compare(viewer, e1, e2);
 			}
 
 			public boolean isSorterProperty(Object element, String property) {
 				return true;
 			}
 		});
 		fSortColumn = 1;
 	}
 
 	private void enableButtons() {
 		IStructuredSelection selection = (IStructuredSelection) fValidatorList
 				.getSelection();
 		int selectionCount = selection.size();
 		
 		boolean addEnabled = true;
 		boolean editEnabled = selectionCount == 1;
 		boolean removeEnabled = true;
 		boolean copyEnabled = selectionCount > 0;
 		
 		if (selectionCount > 0) {
 			Iterator iterator = selection.iterator();
 			while (iterator.hasNext()) {
 				IValidator install = (IValidator) iterator.next();
 				boolean builtin = install.getValidatorType().isBuiltin();
 				if (isContributed(install) || builtin) {
 					removeEnabled = false;
 					copyEnabled = false;
 				}
 			}
 		} else {
 			removeEnabled = false;
 		}
 		fAddButton.setEnabled(addEnabled);
 		fEditButton.setEnabled(editEnabled);
 		fRemoveButton.setEnabled(removeEnabled);
 		fCopyButton.setEnabled(copyEnabled);
 	}
 
 	protected Button createPushButton(Composite parent, String label) {
 		return SWTUtil.createPushButton(parent, label, null);
 	}
 
 	private boolean isContributed(IValidator install) {
 		return ValidatorRuntime.isContributedValidator(install.getID());
 	}
 
 	/**
 	 * Returns this block's control
 	 * 
 	 * @return control
 	 */
 	public Control getControl() {
 		return fControl;
 	}
 
 	/**
 	 * Sets the InterpreterEnvironments to be displayed in this block
 	 * 
 	 * @param validators
 	 *            InterpreterEnvironments to be displayed
 	 */
 	protected void setValidator(IValidator[] validators) {
 		fValidator.clear();
 		List active = new ArrayList();
 		for (int i = 0; i < validators.length; i++) {
 			if( validators[i].isActive()) {
 				active.add(validators[i]);
 			}
 			fValidator.add(validators[i]);
 		}
 		fValidatorList.setInput(fValidator);
 		fValidatorList.setCheckedElements(active.toArray());
 		fValidatorList.refresh();
 	}
 
 	/**
 	 * Returns the InterpreterEnvironments currently being displayed in this
 	 * block
 	 * 
 	 * @return InterpreterEnvironments currently being displayed in this block
 	 */
 	public IValidator[] getValidator() {
 		return (IValidator[]) fValidator.toArray(new IValidator[fValidator
 				.size()]);
 	}
 
 	/**
 	 * @see IAddValidatorDialogRequestor#isDuplicateName(String)
 	 */
 	public boolean isDuplicateName(String name) {
 		for (int i = 0; i < fValidator.size(); i++) {
 			IValidator Interpreter = (IValidator) fValidator.get(i);
 			if (Interpreter.getName().equals(name)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void removeValidator() {
 		IStructuredSelection selection = (IStructuredSelection) fValidatorList
 				.getSelection();
 		IValidator[] Validator = new IValidator[selection.size()];
 		Iterator iter = selection.iterator();
 		int i = 0;
 		while (iter.hasNext()) {
 			Validator[i] = (IValidator) iter.next();
 			i++;
 		}
 		removeValidator(Validator);
 	}
 
 	/**
 	 * Removes the given Validator from the table.
 	 * 
 	 * @param Validator
 	 */
 	public void removeValidator(IValidator[] Validator) {
 		IStructuredSelection prev = (IStructuredSelection) getSelection();
 		for (int i = 0; i < Validator.length; i++) {
 			fValidator.remove(Validator[i]);
 		}
 		fValidatorList.refresh();
 		IStructuredSelection curr = (IStructuredSelection) getSelection();
 		if (!curr.equals(prev)) {
 			IValidator[] installs = getValidator();
 			if (curr.size() == 0 && installs.length == 1) {
 				// pick a default Interpreter automatically
 				setSelection(new StructuredSelection(installs[0]));
 			} else {
 				fireSelectionChanged();
 			}
 		}
 	}
 
 	protected Shell getShell() {
 		return getControl().getShell();
 	}
 
 	/**
 	 * Persist table settings into the give dialog store, prefixed with the
 	 * given key.
 	 * 
 	 * @param settings
 	 *            dialog store
 	 * @param qualifier
 	 *            key qualifier
 	 */
 	public void saveColumnSettings(IDialogSettings settings, String qualifier) {
 		int columnCount = fTable.getColumnCount();
 		for (int i = 0; i < columnCount; i++) {
 			settings
 					.put(
 							qualifier + ".columnWidth" + i, fTable.getColumn(i).getWidth()); //$NON-NLS-1$
 		}
 		settings.put(qualifier + ".sortColumn", fSortColumn); //$NON-NLS-1$
 	}
 
 	/**
 	 * Restore table settings from the given dialog store using the given key.
 	 * 
 	 * @param settings
 	 *            dialog settings store
 	 * @param qualifier
 	 *            key to restore settings from
 	 */
 	public void restoreColumnSettings(IDialogSettings settings, String qualifier) {
 		fValidatorList.getTable().layout(true);
 		restoreColumnWidths(settings, qualifier);
 		try {
 			fSortColumn = settings.getInt(qualifier + ".sortColumn"); //$NON-NLS-1$
 		} catch (NumberFormatException e) {
 			fSortColumn = 1;
 		}
 		switch (fSortColumn) {
 		case 1:
 			sortByName();
 			break;
 		case 2:
 			// sortByLocation();
 			break;
 		case 3:
 			sortByType();
 			break;
 		}
 	}
 
 	private void restoreColumnWidths(IDialogSettings settings, String qualifier) {
 		int columnCount = fTable.getColumnCount();
 		for (int i = 0; i < columnCount; i++) {
 			int width = -1;
 
 			try {
 				width = settings.getInt(qualifier + ".columnWidth" + i); //$NON-NLS-1$
 			} catch (NumberFormatException e) {
 			}
 
 			if (width <= 0) {
 				fTable.getColumn(i).pack();
 			} else {
 				fTable.getColumn(i).setWidth(width);
 			}
 		}
 	}
 
 	/**
 	 * Populates the InterpreterEnvironment table with existing
 	 * InterpreterEnvironments defined in the workspace.
 	 */
 	protected void fillWithWorkspaceValidator() {
 		// fill with Validator
 		List standins = new ArrayList();
 		IValidatorType[] types = ValidatorRuntime
 				.getValidatorTypes(/* getCurrentNature() */);
 		for (int i = 0; i < types.length; i++) {
 			IValidatorType type = types[i];
 			IValidator[] installs = type.getValidators();
 			if (installs != null)
 				for (int j = 0; j < installs.length; j++) {
 					IValidator validator = installs[j];
 					// standins.add(new Validatortandin(install));
 					standins.add(validator);
 				}
 		}
 		setValidator((IValidator[]) standins.toArray(new IValidator[standins
 				.size()]));
 	}
 
 	public void validatorAdded(IValidator Interpreter) {
 		fValidator.add(Interpreter);
 		fValidatorList.refresh();
 
 		IValidator[] installs = getValidator();
 		if (installs.length == 1) {
 			// pick a default Interpreter automatically
 			setSelection(new StructuredSelection(installs[0]));
 		}
 		fireSelectionChanged();
 	}
 
 	// Make sure that Validatortandin ids are unique if multiple calls to
 	// System.currentTimeMillis()
 	// happen very quickly
 	private static String fgLastUsedID;
 
 	/**
 	 * Find a unique Interpreter id. Check existing 'real' Validator, as well as
 	 * the last id used for a Validatortandin.
 	 */
 	protected String createUniqueId(IValidatorType InterpreterType) {
 		String id = null;
 		do {
 			id = String.valueOf(System.currentTimeMillis());
 		} while (InterpreterType.findValidator(id) != null
 				|| id.equals(fgLastUsedID));
 		fgLastUsedID = id;
 		return id;
 	}
 
 	/**
 	 * Compares the given name against current names and adds the appropriate
 	 * numerical suffix to ensure that it is unique.
 	 * 
 	 * @param name
 	 *            the name with which to ensure uniqueness
 	 * @return the unique version of the given name
 	 * 
 	 */
 	protected String generateName(String name) {
 		if (!isDuplicateName(name)) {
 			return name;
 		}
 
 		if (name.matches(".*\\(\\d*\\)")) { //$NON-NLS-1$
 			int start = name.lastIndexOf('(');
 			int end = name.lastIndexOf(')');
 			String stringInt = name.substring(start + 1, end);
 			int numericValue = Integer.parseInt(stringInt);
 			String newName = name.substring(0, start + 1) + (numericValue + 1)
 					+ ")"; //$NON-NLS-1$
 			return generateName(newName);
 		} else {
 			return generateName(name + " (1)"); //$NON-NLS-1$
 		}
 	}
 
 	// protected String getCurrentNature() {
 	// return this.nature;
 	// }
 
 	// protected abstract AddDLTKInterpreterDialog
 	// createInterpreterDialog(IValidator standin);
 
 	protected void copyValidator() {
 		// IStructuredSelection selection = (IStructuredSelection)
 		// fValidatorList.getSelection();
 		// Iterator it = selection.iterator();
 		//	
 		// ArrayList newEntries = new ArrayList();
 		// while (it.hasNext()) {
 		// IValidator selectedInterpreter = (IValidator) it.next();
 		//	
 		// // duplicate & add Interpreter
 		// IValidator standin = new IValidator(selectedInterpreter,
 		// createUniqueId(selectedInterpreter.getValidatorType()));
 		// standin.setName(generateName(selectedInterpreter.getName()));
 		// // AddDLTKInterpreterDialog dialog =
 		// createInterpreterDialog(standin);
 		// dialog.setTitle(ValidatorMessages.InstalledValidatorBlock_18);
 		// if (dialog.open() != Window.OK) {
 		// return;
 		// }
 		// newEntries.add(standin);
 		// fValidator.add(standin);
 		// }
 		// fValidatorList.refresh();
 		// fValidatorList.setSelection(new
 		// StructuredSelection(newEntries.toArray()));
 	}
 
 	/**
 	 * Bring up a dialog that lets the user create a new Interpreter definition.
 	 */
 	protected void addValidator() {
 		AddValidatorDialog dialog = new AddValidatorDialog(this, getShell(),
 				ValidatorRuntime.getPossibleValidatorTypes(), null);
 		dialog.setTitle(ValidatorMessages.InstalledValidatorBlock_7);
 		if (dialog.open() != Window.OK) {
 			return;
 		}
 		fValidatorList.refresh();
 	}
 
 	protected void editValidator() {
 		IStructuredSelection selection = (IStructuredSelection) fValidatorList
 				.getSelection();
 		IValidator validator = (IValidator) selection.getFirstElement();
 		if (validator == null) {
 			return;
 		}
 
 		AddValidatorDialog dialog = new AddValidatorDialog(this, getShell(),
 				ValidatorRuntime.getValidatorTypes(), validator);
 		dialog.setTitle(ValidatorMessages.InstalledValidatorBlock_8);
 		if (dialog.open() != Window.OK) {
 			return;
 		}
 		fValidatorList.refresh(validator);
 	}
 }
