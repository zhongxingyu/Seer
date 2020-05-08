 /*******************************************************************************
  * Copyright (c) 2004, 2005 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.facesconfig.ui.section;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CompoundCommand;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.edit.command.RemoveCommand;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jst.jsf.facesconfig.common.dialogfield.ClassButtonDialogField;
 import org.eclipse.jst.jsf.facesconfig.common.dialogfield.DialogField;
 import org.eclipse.jst.jsf.facesconfig.common.dialogfield.DialogFieldBase;
 import org.eclipse.jst.jsf.facesconfig.common.dialogfield.DialogFieldGroup;
 import org.eclipse.jst.jsf.facesconfig.common.dialogfield.IDialogFieldApplyListener;
 import org.eclipse.jst.jsf.facesconfig.common.dialogfield.LayoutUtil;
 import org.eclipse.jst.jsf.facesconfig.common.guiutils.SWTUtils;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigFactory;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigPackage;
 import org.eclipse.jst.jsf.facesconfig.emf.KeyClassType;
 import org.eclipse.jst.jsf.facesconfig.emf.KeyType;
 import org.eclipse.jst.jsf.facesconfig.emf.ManagedBeanType;
 import org.eclipse.jst.jsf.facesconfig.emf.MapEntriesType;
 import org.eclipse.jst.jsf.facesconfig.emf.MapEntryType;
 import org.eclipse.jst.jsf.facesconfig.emf.NullValueType;
 import org.eclipse.jst.jsf.facesconfig.emf.ValueClassType;
 import org.eclipse.jst.jsf.facesconfig.emf.ValueType;
 import org.eclipse.jst.jsf.facesconfig.ui.EditorPlugin;
 import org.eclipse.jst.jsf.facesconfig.ui.EditorMessages;
 import org.eclipse.jst.jsf.facesconfig.ui.dialog.AddEditMapEntryDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * MapEntries Editing DialogFieldGroup
  * 
  * @author sfshi
  * @version
  */
 public class MapEntriesEditGroup extends DialogFieldGroup implements
 		IDialogFieldGroup, ISelectionProvider, ISelectionChangedListener {
 	private ClassButtonDialogField keyClassField;
 
 	private ClassButtonDialogField valueClassField;
 
 	private TableViewer tableViewer;
 
 	private static final int TABLE_DEFAULT_HEIGHT = 160;
 
 	private IProject currentProject;
 
 	private Button removeButton;
 
 	private Button editButton;
 
 	private ManagedBeanType managedBean;
 
 	private AbstractFacesConfigSection section;
 
 	private List selectionChangedListeners = new ArrayList();
 
 	/**
 	 */
 	public MapEntriesEditGroup(AbstractFacesConfigSection section) {
 		super();
 		this.section = section;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.facesconfig.ui.common.dialogfield.DialogFieldGroup#initialize()
 	 */
 	public void initialize() {
 	}
 
 	public void updateButtons() {
 		if (((IStructuredSelection) tableViewer.getSelection()).size() > 0) {
 			editButton.setEnabled(true);
 			removeButton.setEnabled(true);
 		} else {
 			editButton.setEnabled(false);
 			removeButton.setEnabled(false);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.facesconfig.ui.common.dialogfield.DialogFieldGroup#layoutDialogFields(org.eclipse.ui.forms.widgets.FormToolkit,
 	 *      org.eclipse.swt.widgets.Composite)
 	 */
 	public void layoutDialogFields(FormToolkit toolkit, Composite parent) {
 		GridData data = new GridData(GridData.FILL_BOTH);
 		parent.setLayoutData(data);
 
 		GridLayout gl = new GridLayout();
 		gl.verticalSpacing = 0;
 		gl.marginHeight = 0;
 		parent.setLayout(gl);
 
 		layoutMapTypeSelectionSection(toolkit, parent);
 
 		createAndLayoutMapValueSection(toolkit, parent);
 	}
 
 	/**
 	 * 
 	 */
 	private void layoutMapTypeSelectionSection(FormToolkit toolkit,
 			Composite container) {
 
 		Composite typeSelectionSection = null;
 		if (toolkit != null) {
 			typeSelectionSection = toolkit.createComposite(container);
 			toolkit.paintBordersFor(typeSelectionSection);
 		} else {
 			typeSelectionSection = SWTUtils
 					.createComposite(container, SWT.NONE);
 		}
 
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		typeSelectionSection.setLayoutData(gd);
 
 		int numberOfColumns = 4;
 		GridLayout gl = new GridLayout(numberOfColumns, false);
 		typeSelectionSection.setLayout(gl);
 		keyClassField = new ClassButtonDialogField(getProject());
 		keyClassField
 				.setLabelText(EditorMessages.InitializationSection_MapType_KeyClass);
 		keyClassField
 				.setDialogFieldApplyListener(new IDialogFieldApplyListener() {
 
 					public void dialogFieldApplied(DialogField field) {
 						ManagedBeanType managedBean = (ManagedBeanType) getInput();
 						String className = ((ClassButtonDialogField) field)
 								.getText();
 						KeyClassType keyClass = FacesConfigFactory.eINSTANCE
 								.createKeyClassType();
 						keyClass.setTextContent(className);
 						EditingDomain editingDomain = section
 								.getEditingDomain();
 						Command cmd;
 						boolean isNew;
 						if (managedBean.getMapEntries() == null) {
 							isNew = true;
 							MapEntriesType mapEntries = FacesConfigFactory.eINSTANCE
 									.createMapEntriesType();
 							mapEntries.setKeyClass(keyClass);
 							cmd = SetCommand.create(editingDomain, managedBean,
 									FacesConfigPackage.eINSTANCE
 											.getManagedBeanType_MapEntries(),
 									mapEntries);
 						} else {
 							isNew = false;
 							cmd = SetCommand.create(editingDomain, managedBean
 									.getMapEntries(),
 									FacesConfigPackage.eINSTANCE
 											.getMapEntriesType_KeyClass(),
 									keyClass);
 						}
 
 						if (cmd.canExecute()) {
 							editingDomain.getCommandStack().execute(cmd);
 							if (isNew)
 								refreshAll();
 						}
 
 					}
 				});
 
 		valueClassField = new ClassButtonDialogField(getProject());
 
 		valueClassField
 				.setLabelText(EditorMessages.InitializationSection_MapType_ValueClass);
 		valueClassField
 				.setDialogFieldApplyListener(new IDialogFieldApplyListener() {
 
 					public void dialogFieldApplied(DialogField field) {
 						ManagedBeanType managedBean = (ManagedBeanType) getInput();
 						String className = ((ClassButtonDialogField) field)
 								.getText();
 						ValueClassType valueClass = FacesConfigFactory.eINSTANCE
 								.createValueClassType();
 						valueClass.setTextContent(className);
 						EditingDomain editingDomain = section
 								.getEditingDomain();
 						Command cmd;
 						boolean isNew;
 						if (managedBean.getMapEntries() == null) {
 							isNew = true;
 							MapEntriesType mapEntries = FacesConfigFactory.eINSTANCE
 									.createMapEntriesType();
 							mapEntries.setValueClass(valueClass);
 							cmd = SetCommand.create(editingDomain, managedBean,
 									FacesConfigPackage.eINSTANCE
 											.getManagedBeanType_MapEntries(),
 									mapEntries);
 						} else {
 							isNew = false;
 							cmd = SetCommand.create(editingDomain, managedBean
 									.getMapEntries(),
 									FacesConfigPackage.eINSTANCE
 											.getMapEntriesType_ValueClass(),
 									valueClass);
 						}
 
 						if (cmd.canExecute()) {
 							editingDomain.getCommandStack().execute(cmd);
 							if (isNew)
 								refreshAll();
 						}
 
 					}
 				});
 
 		keyClassField.doFillIntoGrid(toolkit, typeSelectionSection,
 				numberOfColumns);
 		LayoutUtil.setHorizontalGrabbing(keyClassField.getTextControl(toolkit,
 				container));
 
 		valueClassField.doFillIntoGrid(toolkit, typeSelectionSection,
 				numberOfColumns);
 	}
 
 	/**
 	 * @param toolkit
 	 * @param parent
 	 */
 	private void createAndLayoutMapValueSection(FormToolkit toolkit,
 			Composite parent) {
 		Composite mapValueSection = null;
 		if (toolkit != null) {
 			mapValueSection = toolkit.createComposite(parent);
 			toolkit.paintBordersFor(mapValueSection);
 		} else {
 			mapValueSection = SWTUtils.createComposite(parent, SWT.NONE);
 		}
 
 		GridData gd = new GridData(GridData.FILL_BOTH);
 		mapValueSection.setLayoutData(gd);
 
 		int numberOfColumns = 3;
 		GridLayout gl = new GridLayout(numberOfColumns, false);
 		mapValueSection.setLayout(gl);
 		DialogField valuesTitle = new DialogFieldBase();
 		valuesTitle
 				.setLabelText(EditorMessages.InitializationSection_MapTable_Title);//$NON-NLS-1$    
 
 		valuesTitle.doFillIntoGrid(toolkit, mapValueSection, numberOfColumns);
 		Table table;
 		if (toolkit == null) {
 			table = new Table(mapValueSection, SWT.FULL_SELECTION
 					| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
 		} else {
 			table = new Table(mapValueSection, SWT.H_SCROLL | SWT.V_SCROLL
 					| SWT.FULL_SELECTION | SWT.BORDER);
 		}
 
 		table.addSelectionListener(new SelectionAdapter() {
 
 			public void widgetSelected(SelectionEvent e) {
 				updateButtons();
 
 			}
 		});
 		gd = new GridData(GridData.FILL_BOTH);
 		gd.horizontalSpan = 1;
 		gd.heightHint = TABLE_DEFAULT_HEIGHT;
 		table.setLayoutData(gd);
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		TableLayout layout = new TableLayout();
 		table.setLayout(layout);
 
 		TableColumn keyCol = new TableColumn(table, SWT.NONE);
		keyCol
				.setText(EditorMessages.InitializationSection_MapTable_Key);//$NON-NLS-1$
 		layout.addColumnData(new ColumnWeightData(1, true));
 		keyCol.setResizable(true);
 
 		TableColumn valueCol = new TableColumn(table, SWT.NONE);
 		layout.addColumnData(new ColumnWeightData(1, true));
		valueCol
				.setText(EditorMessages.InitializationSection_MapTable_Value);//$NON-NLS-1$
 		valueCol.setResizable(true);
 		table.addMouseListener(new MouseAdapter() {
 			public void mouseDoubleClick(MouseEvent e) {
 				if (((IStructuredSelection) tableViewer.getSelection()).size() > 0)
 					editButtonSelected();
 			}
 		});
 		tableViewer = new TableViewer(table);
 		tableViewer.setContentProvider(new AdapterFactoryContentProvider(
 				section.getAdapterFactory()));
 		tableViewer.setLabelProvider(new AdapterFactoryLabelProvider(section
 				.getAdapterFactory()));
 
 		tableViewer.addFilter(new ViewerFilter() {
 			public boolean select(Viewer viewer, Object parentElement,
 					Object element) {
 				return FacesConfigPackage.eINSTANCE.getMapEntryType()
 						.isInstance(element);
 			}
 		});
 
 		tableViewer.addSelectionChangedListener(this);
 
 		Composite operationContainer = null;
 		if (toolkit != null) {
 			operationContainer = toolkit.createComposite(mapValueSection);
 		} else {
 			operationContainer = SWTUtils.createComposite(mapValueSection,
 					SWT.NONE);
 		}
 		gd = new GridData(GridData.FILL_VERTICAL);
 		operationContainer.setLayoutData(gd);
 		gl = new GridLayout();
 		operationContainer.setLayout(gl);
 
 		Button addButton = null;
 		if (toolkit != null) {
 			addButton = toolkit.createButton(operationContainer,
 					EditorMessages.UI_Button_Add_more, //$NON-NLS-1$
 					SWT.PUSH);
 		} else {
 			addButton = SWTUtils.createPushButton(operationContainer,
 					EditorMessages.UI_Button_Add_more); //$NON-NLS-1$
 		}
 		gd = new GridData(GridData.FILL_HORIZONTAL
 				| GridData.VERTICAL_ALIGN_BEGINNING);
 		gd.grabExcessHorizontalSpace = false;
 		addButton.setLayoutData(gd);
 
 		if (toolkit != null) {
 			editButton = toolkit.createButton(operationContainer,
 					EditorMessages.UI_Button_Edit_more, SWT.PUSH);
 		} else {
 			editButton = SWTUtils.createPushButton(operationContainer,
 					EditorMessages.UI_Button_Edit_more); //$NON-NLS-1$
 		}
 		gd = new GridData(GridData.FILL_HORIZONTAL
 				| GridData.VERTICAL_ALIGN_BEGINNING);
 		gd.grabExcessHorizontalSpace = false;
 		editButton.setLayoutData(gd);
 
 		if (toolkit != null) {
 			removeButton = toolkit.createButton(operationContainer,
 					EditorMessages.UI_Button_Remove, //$NON-NLS-1$
 					SWT.PUSH);
 		} else {
 			removeButton = SWTUtils.createPushButton(operationContainer,
 					EditorMessages.UI_Button_Remove); //$NON-NLS-1$
 		}
 		gd = new GridData(GridData.FILL_HORIZONTAL
 				| GridData.VERTICAL_ALIGN_BEGINNING);
 		gd.grabExcessHorizontalSpace = false;
 		removeButton.setLayoutData(gd);
 		addButton.addSelectionListener(new SelectionAdapter() {
 
 			public void widgetSelected(SelectionEvent e) {
 				addButtonSelected();
 			}
 
 		});
 
 		editButton.addSelectionListener(new SelectionAdapter() {
 
 			public void widgetSelected(SelectionEvent e) {
 				editButtonSelected();
 			}
 		});
 		removeButton.addSelectionListener(new SelectionAdapter() {
 
 			public void widgetSelected(SelectionEvent e) {
 				removeButtonSelected();
 			}
 		});
 	}
 
 	protected void addButtonSelected() {
 		AddEditMapEntryDialog dialog = new AddEditMapEntryDialog(EditorPlugin
 				.getActiveShell(), true);
 		if (dialog.open() == Dialog.OK) {
 			MapEntryType mapEntry = FacesConfigFactory.eINSTANCE
 					.createMapEntryType();
 			if (dialog.getKey() != null) {
 				KeyType key = FacesConfigFactory.eINSTANCE.createKeyType();
 				key.setTextContent(dialog.getKey());
 				mapEntry.setKey(key);
 			}
 			if (dialog.isNullValue()) {
 				NullValueType nullValue = FacesConfigFactory.eINSTANCE
 						.createNullValueType();
 				mapEntry.setNullValue(nullValue);
 			} else {
 				ValueType value = FacesConfigFactory.eINSTANCE
 						.createValueType();
 				value.setTextContent(dialog.getValue());
 				mapEntry.setValue(value);
 			}
 
 			EditingDomain editingDomain = section.getEditingDomain();
 			Command command;
 			boolean isNewMapEntries = false;
 			if (managedBean.getMapEntries() == null) {
 				MapEntriesType mapEntriesType = FacesConfigFactory.eINSTANCE
 						.createMapEntriesType();
 				mapEntriesType.getMapEntry().add(mapEntry);
 				command = SetCommand.create(editingDomain, managedBean,
 						FacesConfigPackage.eINSTANCE
 								.getManagedBeanType_MapEntries(),
 						mapEntriesType);
 				isNewMapEntries = true;
 			} else {
 				MapEntriesType mapEntries = managedBean.getMapEntries();
 
 				command = AddCommand.create(editingDomain, mapEntries,
 						FacesConfigPackage.eINSTANCE
 								.getMapEntriesType_MapEntry(), mapEntry);
 			}
 
 			if (command.canExecute()) {
 				editingDomain.getCommandStack().execute(command);
 				if (isNewMapEntries)
 					refreshAll();
 			}
 
 		}
 	}
 
 	protected void editButtonSelected() {
 		MapEntryType mapEntry = (MapEntryType) ((IStructuredSelection) tableViewer
 				.getSelection()).getFirstElement();
 		AddEditMapEntryDialog dialog = new AddEditMapEntryDialog(EditorPlugin
 				.getActiveShell(), false);
 		if (mapEntry.getKey() != null)
 			dialog.setKey(mapEntry.getKey().getTextContent());
 		if (mapEntry.getNullValue() != null)
 			dialog.setNullValue(true);
 		else if (mapEntry.getValue() != null)
 			dialog.setValue(mapEntry.getValue().getTextContent());
 
 		if (dialog.open() == Dialog.OK) {
 			EditingDomain editingDomain = section.getEditingDomain();
 			List commands = new ArrayList();
 
 			if (mapEntry.getKey() != null) {
 				Command cmd1 = SetCommand.create(editingDomain, mapEntry
 						.getKey(), FacesConfigPackage.eINSTANCE
 						.getKeyType_TextContent(), dialog.getKey());
 				commands.add(cmd1);
 
 			} else {
 				KeyType keyType = FacesConfigFactory.eINSTANCE.createKeyType();
 				keyType.setTextContent(dialog.getKey());
 				Command cmd2 = SetCommand.create(editingDomain, mapEntry,
 						FacesConfigPackage.eINSTANCE.getMapEntryType_Key(),
 						keyType);
 				commands.add(cmd2);
 			}
 
 			if (dialog.isNullValue()) {
 				if (mapEntry.getValue() != null) {
 					Command cmd3 = SetCommand.create(editingDomain, mapEntry,
 							FacesConfigPackage.eINSTANCE
 									.getMapEntryType_Value(),
 							SetCommand.UNSET_VALUE);
 					commands.add(cmd3);
 				}
 				Command cmd4 = SetCommand.create(editingDomain, mapEntry,
 						FacesConfigPackage.eINSTANCE
 								.getMapEntryType_NullValue(),
 						FacesConfigFactory.eINSTANCE.createNullValueType());
 
 				commands.add(cmd4);
 			} else {
 				if (mapEntry.getNullValue() != null) {
 					Command cmd5 = SetCommand.create(editingDomain, mapEntry,
 							FacesConfigPackage.eINSTANCE
 									.getMapEntryType_NullValue(),
 							SetCommand.UNSET_VALUE);
 					commands.add(cmd5);
 				}
 
 				if (mapEntry.getValue() != null) {
 					Command cmd6 = SetCommand.create(editingDomain, mapEntry
 							.getValue(), FacesConfigPackage.eINSTANCE
 							.getValueType_TextContent(), dialog.getValue());
 					commands.add(cmd6);
 				} else {
 					ValueType value = FacesConfigFactory.eINSTANCE
 							.createValueType();
 					value.setTextContent(dialog.getValue());
 					Command cmd7 = SetCommand.create(editingDomain, mapEntry,
 							FacesConfigPackage.eINSTANCE
 									.getMapEntryType_Value(), value);
 					commands.add(cmd7);
 				}
 
 			}
 
 			Command command = new CompoundCommand(commands);
 			if (command.canExecute()) {
 				editingDomain.getCommandStack().execute(command);
 				tableViewer.refresh(mapEntry);
 			}
 
 		}
 
 	}
 
 	protected void removeButtonSelected() {
 
 		MapEntryType mapEntry = (MapEntryType) ((IStructuredSelection) tableViewer
 				.getSelection()).getFirstElement();
 		EditingDomain editingDomain = section.getEditingDomain();
 		Command cmd = RemoveCommand.create(editingDomain, mapEntry);
 		if (cmd.canExecute()) {
 			editingDomain.getCommandStack().execute(cmd);
 			refresh();
 		}
 	}
 
 	/**
 	 * get the project according to current xml node.
 	 * 
 	 * @return
 	 */
 	private IProject getProject() {
 		if (currentProject == null) {
 			currentProject = (IProject) section.getPage().getEditor()
 					.getAdapter(IProject.class);
 		}
 		return currentProject;
 	}
 
 	public void setProject(IProject project) {
 		currentProject = project;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.facesconfig.ui.common.dialogfield.DialogFieldGroup#validateDialogFields()
 	 */
 	public IStatus[] validateDialogFields() {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.facesconfig.ui.section.IDialogFieldGroup#setInput(java.lang.Object)
 	 */
 	public void setInput(Object newInput) {
 		if (newInput != null && newInput instanceof ManagedBeanType) {
 			managedBean = (ManagedBeanType) newInput;
 			if (managedBean.getMapEntries() != null)
 				refreshAll();
 		}
 	}
 
 	public void refreshAll() {
 		if (managedBean.getMapEntries() != null
 				&& managedBean.getMapEntries().getKeyClass() != null) {
 			this.keyClassField.setTextWithoutUpdate(managedBean.getMapEntries()
 					.getKeyClass().getTextContent());
 		} else {
 			this.keyClassField.setTextWithoutUpdate(null);
 		}
 
 		if (managedBean.getMapEntries() != null
 				&& managedBean.getMapEntries().getValueClass() != null) {
 			this.valueClassField.setTextWithoutUpdate(managedBean
 					.getMapEntries().getValueClass().getTextContent());
 		} else {
 			this.valueClassField.setTextWithoutUpdate(null);
 		}
 
 		tableViewer.setInput(((ManagedBeanType) getInput()).getMapEntries());
 		updateButtons();
 	}
 
 	public Object getInput() {
 		return this.managedBean;
 	}
 
 	public void refreshData() {
 		refresh();
 
 	}
 
 	/**
 	 * 
 	 */
 	public void refresh() {
 		tableViewer.refresh();
 		updateButtons();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		// TODO Auto-generated method stub
 		selectionChangedListeners.add(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
 	 */
 	public ISelection getSelection() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void removeSelectionChangedListener(
 			ISelectionChangedListener listener) {
 		// TODO Auto-generated method stub
 		selectionChangedListeners.remove(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
 	 */
 	public void setSelection(ISelection selection) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
 	 */
 	public void selectionChanged(SelectionChangedEvent event) {
 		// TODO Auto-generated method stub
 		for (Iterator listeners = selectionChangedListeners.iterator(); listeners
 				.hasNext();) {
 			ISelectionChangedListener listener = (ISelectionChangedListener) listeners
 					.next();
 			listener.selectionChanged(new SelectionChangedEvent(this, event
 					.getSelection()));
 		}
 	}
 }
