 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Hodaie
  * koegel
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.ui.dialogs.admin;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.emfstore.internal.client.model.AdminBroker;
 import org.eclipse.emf.emfstore.internal.client.ui.Activator;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACGroup;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACOrgUnit;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACUser;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DragSourceListener;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.ElementListSelectionDialog;
 
 /**
  * This is the super class of property composites shown on properties form (right side of OrgUnitManagmentGUI). It
  * contains an attributes group at top, a TableViewer and button to add/remove OrgUnits.
  * 
  * @author Hodaie
  */
 public abstract class PropertiesComposite extends Composite {
 
 	private final AdminBroker adminBroker;
 
 	private Group grpTable;
 	private Group grpAttributes;
 
 	private Label lblName;
 	private Text txtName;
 	private Label lblDescription;
 	private Text txtDescription;
 
 	private TableViewer tableViewer;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param parent parent
 	 * @param style style
 	 * @param adminBroker adminBroker
 	 */
 	public PropertiesComposite(Composite parent, int style, AdminBroker adminBroker) {
 		super(parent, style);
 		this.adminBroker = adminBroker;
 	}
 
 	/**
 	 * This creates attributes, and table group controls.
 	 */
 	protected void createControls() {
 		setLayout(new GridLayout());
 		createSimpleAttributes();
 		createTableGroup(getTabTitle());
 		createButtons(grpTable);
 	}
 
 	/**
 	 * This creates attributes group control.
 	 */
 	protected void createSimpleAttributes() {
 		grpAttributes = new Group(this, SWT.V_SCROLL);
 		grpAttributes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		grpAttributes.setText(Messages.PropertiesComposite_Properties);
 		grpAttributes.setLayout(new GridLayout(2, false));
 
 		lblName = new Label(grpAttributes, SWT.NONE);
 		lblName.setText(Messages.PropertiesComposite_Name);
 		txtName = new Text(grpAttributes, SWT.BORDER);
 
 		txtName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		txtName.addFocusListener(new FocusListener() {
 
 			public void focusGained(FocusEvent e) {
 			}
 
 			public void focusLost(FocusEvent e) {
 				saveOrgUnitAttributes();
 			}
 		});
 
 		lblDescription = new Label(grpAttributes, SWT.NONE);
 		lblDescription.setText(Messages.PropertiesComposite_Description);
 		txtDescription = new Text(grpAttributes, SWT.BORDER);
 		txtDescription.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		txtDescription.addFocusListener(new FocusListener() {
 
 			public void focusGained(FocusEvent e) {
 			}
 
 			public void focusLost(FocusEvent e) {
 				saveOrgUnitAttributes();
 			}
 		});
 	}
 
 	/**
 	 * Returns the title of the tab.
 	 * 
 	 * @return the title of the tab.
 	 */
 	protected abstract String getTabTitle();
 
 	/**
 	 * This saves an OrgUnit when txtName or txtDescription lose focus. GroupComposite and UserComposite Subclasses must
 	 * override this method.
 	 */
 	protected void saveOrgUnitAttributes() {
 	}
 
 	/**
 	 * This creates table viewer group control.
 	 * 
 	 * @param groupName group name
 	 */
 	protected void createTableGroup(String groupName) {
 		grpTable = new Group(this, SWT.NONE);
 		grpTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		grpTable.setText(groupName);
 		grpTable.setLayout(new GridLayout(5, true));
 
 		createTableViewer(grpTable);
 
 	}
 
 	/**
 	 * This creates TableViewer.
 	 * 
 	 * @param parent parent
 	 */
 	protected void createTableViewer(Composite parent) {
 
		final int style = SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION;
 
 		final Table table = new Table(parent, style);
 		final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		gridData.horizontalSpan = 5;
 		table.setLayoutData(gridData);
 		table.setLinesVisible(true);
 		table.setHeaderVisible(true);
 
 		// 1st column with image
 		TableColumn column = new TableColumn(table, SWT.CENTER, 0);
 		column.setText(StringUtils.EMPTY);
 		column.setWidth(20);
 
 		column = new TableColumn(table, SWT.LEFT, 1);
 		column.setText(Messages.PropertiesComposite_Name);
 		column.setWidth(100);
 
 		column = new TableColumn(table, SWT.LEFT, 2);
 		column.setText(Messages.PropertiesComposite_Description);
 		column.setWidth(200);
 
 		tableViewer = new TableViewer(table);
 		tableViewer.setUseHashlookup(true);
 		tableViewer.setContentProvider(new TableContentProvider());
 		tableViewer.setLabelProvider(new TableLabelProvider());
 		addDragNDropSupport();
 
 	}
 
 	/**
 	 * This creates add/remove Buttons underneath TableViewer.
 	 * 
 	 * @param parent parent
 	 */
 	protected void createButtons(Composite parent) {
 		// Create and configure the "Add" button
 		final Button add = new Button(parent, SWT.PUSH | SWT.CENTER);
 		add.setText(Messages.PropertiesComposite_Add);
 
 		GridData gridData = new GridData();
 		gridData.widthHint = 80;
 		gridData.horizontalAlignment = GridData.END;
 		gridData.horizontalSpan = 4;
 		add.setLayoutData(gridData);
 		add.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				addNewOrgUnit();
 			}
 
 		});
 
 		// Create and configure the "Delete" button
 		final Button remove = new Button(parent, SWT.PUSH | SWT.CENTER);
 		remove.setText(Messages.PropertiesComposite_Remove);
 		gridData = new GridData(SWT.END);
 		gridData.widthHint = 80;
 		remove.setLayoutData(gridData);
 
 		remove.addSelectionListener(new SelectionAdapter() {
 
 			// Remove the selection and refresh the view
 			@Override
 			public void widgetSelected(SelectionEvent e) {
				final ACOrgUnit ou = (ACOrgUnit) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
				if (ou != null) {
					removeOrgUnit(ou);
 				}
 			}
 		});
 
 	}
 
 	/**
 	 * This adds DnD support. This method adds drag support, subclasses add drop support by overriding this method.
 	 */
 	protected void addDragNDropSupport() {
 		final int ops = DND.DROP_MOVE;
 		final Transfer[] transfers = new Transfer[] { LocalSelectionTransfer.getTransfer() };
 		final DragSourceListener dragListener = new DragSourceListener() {
 			public void dragFinished(DragSourceEvent event) {
 				getTableViewer().refresh();
 				PropertiesForm.setDragNDropObject(null);
 			}
 
 			public void dragSetData(DragSourceEvent event) {
 				final EObject eObject = getSelectedItem();
 				if (eObject != null) {
 					if (eObject instanceof ACOrgUnit) {
 						final ACOrgUnit orgUnit = (ACOrgUnit) eObject;
 						PropertiesForm.setDragNDropObject(orgUnit);
 					}
 				}
 			}
 
 			public void dragStart(DragSourceEvent event) {
 			}
 		};
 		getTableViewer().addDragSupport(ops, transfers, dragListener);
 	}
 
 	/**
 	 * This will be used to add OrgUnits using add button. It adds an a new OrgUnit using an object selection dialog.
 	 * Subclasses must override this.
 	 */
 	protected void addNewOrgUnit() {
 
 	}
 
 	/**
 	 * This will be used when adding OrgUnits using drag and drop This adds an existing OrgUnit. Subclasses must
 	 * override this.
 	 * 
 	 * @param orgUnit orgUnit
 	 */
 	protected void addExistingOrgUnit(ACOrgUnit orgUnit) {
 
 	}
 
 	/**
 	 * This removes an OrgUnit. Subclasses must override this.
 	 * 
 	 * @param orgUnit OrgUnit
 	 */
 	protected abstract void removeOrgUnit(ACOrgUnit orgUnit);
 
 	/**
 	 * @param input Input
 	 */
 	public abstract void updateControls(EObject input);
 
 	/**
 	 * Shows an element selection dialog with specified initial contents and title, and returns an array of selected
 	 * elements.
 	 * 
 	 * @param content initial contents
 	 * @param title title
 	 * @return selected elements.
 	 */
 	protected Object[] showDialog(Collection<ACOrgUnit> content, String title) {
 		final ElementListSelectionDialog dlg = new ElementListSelectionDialog(getShell(),
 			new ILabelProviderImplementation());
 
 		dlg.setElements(content.toArray(new Object[content.size()]));
 		dlg.setTitle(title);
 		dlg.setBlockOnOpen(true);
 		dlg.setMultipleSelection(true);
 		Object[] result = new Object[0];
 		if (dlg.open() == Window.OK) {
 			result = dlg.getResult();
 		}
 		return result;
 	}
 
 	/**
 	 * Returns selected item in TableViewer.
 	 * 
 	 * @return selected item in table viewer
 	 */
 	protected EObject getSelectedItem() {
 		EObject result = null;
 		final ISelection sel = tableViewer.getSelection();
 		IStructuredSelection ssel = null;
 		if (sel != null) {
 			ssel = (IStructuredSelection) sel;
 		}
 
 		if (ssel != null) {
 			final Object obj = ssel.getFirstElement();
 			result = (ACOrgUnit) obj;
 		}
 		return result;
 	}
 
 	/**
 	 * @return group control containing TabelViewer
 	 */
 	protected Group getTableGroup() {
 		return grpTable;
 	}
 
 	/**
 	 * @return group control containing name, description, and version.
 	 */
 	protected Group getAttributesGroup() {
 		return grpAttributes;
 	}
 
 	/**
 	 * @return adminBroker
 	 */
 	protected AdminBroker getAdminBroker() {
 		return adminBroker;
 	}
 
 	/**
 	 * @return txtName
 	 */
 	protected Text getTxtName() {
 		return txtName;
 	}
 
 	/**
 	 * @return txtDescription
 	 */
 	protected Text getTxtDescription() {
 		return txtDescription;
 	}
 
 	/**
 	 * @return tableViewer
 	 */
 	public TableViewer getTableViewer() {
 		return tableViewer;
 	}
 
 	/**
 	 * Label provider for users and groups.
 	 * 
 	 * @author koegel
 	 */
 	private final class ILabelProviderImplementation implements ILabelProvider {
 		private static final String USER_ICON = "icons/user.png"; //$NON-NLS-1$
 		private static final String GROUP_ICON = "icons/Group.gif"; //$NON-NLS-1$
 
 		public Image getImage(Object element) {
 			if (element instanceof ACGroup) {
 				return Activator.getImageDescriptor(GROUP_ICON).createImage();
 			}
 			return Activator.getImageDescriptor(USER_ICON).createImage();
 		}
 
 		public String getText(Object element) {
 			return ((ACOrgUnit) element).getName();
 		}
 
 		public void addListener(ILabelProviderListener listener) {
 		}
 
 		public void removeListener(ILabelProviderListener listener) {
 		}
 
 		public void dispose() {
 		}
 
 		public boolean isLabelProperty(Object element, String property) {
 			return false;
 		}
 	}
 
 	/**
 	 * This is the LabelProvider for TableViewer.
 	 * 
 	 * @author Hodaie
 	 */
 	private class TableContentProvider implements IStructuredContentProvider {
 
 		public Object[] getElements(Object inputElement) {
 			Object[] result = new Object[0];
 
 			if (inputElement instanceof ACUser) {
 				List<ACGroup> groups;
 
 				try {
 					groups = adminBroker.getGroups(((ACUser) inputElement).getId());
 					result = groups.toArray(new ACOrgUnit[groups.size()]);
 				} catch (final ESException ex) {
 					MessageDialog.openWarning(getShell(),
 						Messages.ProjectComposite_Insufficient_Access_Rights,
 						Messages.PropertiesComposite_Could_Not_Fetch_Groups_Of_User);
 				}
 
 			} else if (inputElement instanceof ACGroup) {
 				List<ACOrgUnit> members;
 				try {
 					members = adminBroker.getMembers(((ACGroup) inputElement).getId());
 					result = members.toArray(new ACOrgUnit[members.size()]);
 				} catch (final ESException ex) {
 					MessageDialog.openWarning(getShell(), Messages.PropertiesComposite_Could_Not_Fetch_Group_Members,
 						ex.getMessage());
 				}
 
 			} else if (inputElement instanceof ProjectInfo) {
 				List<ACOrgUnit> participants;
 				try {
 					participants = adminBroker.getParticipants(((ProjectInfo) inputElement)
 						.getProjectId());
 					result = participants.toArray(new ACOrgUnit[participants.size()]);
 				} catch (final ESException ex) {
 					MessageDialog.openWarning(getShell(), ex.getMessage(),
 						Messages.PropertiesComposite_Could_Not_Fetch_Participants);
 
 				}
 			}
 
 			return result;
 		}
 
 		public void dispose() {
 		}
 
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		}
 
 	}// TableContentProvider
 
 	/**
 	 * This is the ContentProvider for the TableViewer.
 	 * 
 	 * @author Hodaie
 	 */
 	private class TableLabelProvider extends AdapterFactoryLabelProvider {
 
 		public TableLabelProvider() {
 			super(Activator.getAdapterFactory());
 		}
 
 		@Override
 		public Image getColumnImage(Object element, int columnIndex) {
 			if (columnIndex == 0) {
 				return super.getImage(element);
 			}
 			return null;
 		}
 
 		@Override
 		public String getColumnText(Object element, int columnIndex) {
 			final ACOrgUnit orgUnit = (ACOrgUnit) element;
 			String result = StringUtils.EMPTY;
 
 			switch (columnIndex) {
 			case 0:
 				break;
 			case 1:
 				result = orgUnit.getName();
 				break;
 			case 2:
 				result = orgUnit.getDescription();
 				break;
 			default:
 				break;
 			}
 
 			return result;
 		}
 
 	}// TableLabelProvider
 
 }// FormContent
