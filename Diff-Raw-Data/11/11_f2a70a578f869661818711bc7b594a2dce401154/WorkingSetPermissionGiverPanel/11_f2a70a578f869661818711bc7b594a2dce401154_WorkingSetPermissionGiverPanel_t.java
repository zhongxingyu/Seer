 package org.iucn.sis.client.panels.permissions;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.WorkingSetCache;
 import org.iucn.sis.client.api.models.ClientUser;
 import org.iucn.sis.client.api.ui.users.panels.BrowseUsersWindow;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.client.panels.permissions.PermissionUserModel.PermissionsModelData;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.models.WorkingSet;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.GridEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.store.Store;
 import com.extjs.gxt.ui.client.store.StoreEvent;
 import com.extjs.gxt.ui.client.store.StoreFilter;
 import com.extjs.gxt.ui.client.store.StoreListener;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ButtonBar;
 import com.extjs.gxt.ui.client.widget.grid.CellEditor;
 import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.HTMLTable.Cell;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.extjs.client.WindowUtils;
 
 public abstract class WorkingSetPermissionGiverPanel extends ContentPanel {
 
 	public static final String read = "read";
 	public static final String write = "write";
 	public static final String create = "create";
 	public static final String assign = "grant";
 	public static final String delete = "delete";
 
 	private String instructions = "";
 	private boolean allowRead;
 	private boolean allowWrite;
 	private boolean allowAssign;
 	private boolean allowCreate;
 	private boolean allowDelete;
 	private int widthOfNameColumn = 200;
 	private int widthOfPermissionColumn = 200;
 	private String defaultPermission = read;
 
 	private PermissionUserModel selectedItem = null;
 	
 	protected ToolBar toolBar;
 	protected Button removeButton;
 	protected ListStore<PermissionUserModel> associatedPermissions;
 
 	public WorkingSetPermissionGiverPanel() {
 		allowRead = true;
 		allowWrite = true;
 		allowAssign = true;
 		allowCreate = false;
 		allowDelete = false;
 		associatedPermissions = new ListStore<PermissionUserModel>();
 		setInstructions("<b>Instructions:</b>Add/Remove users to determine to what extent other users " +
 			"can access this working set. These permissions govern who can subscribe to (read), edit " +
 			"(write) or access the Permission Management panel (grant) for this working set. These permissions " +
 			"<i>do not</i> grant permission to read or write the taxa contained in the working set - " +
 			"that must be performed by an appropriate SIS permissions manager.<br />");
 		toolBar = new ToolBar();
 		setHeaderVisible(false);
 //		setTopComponent(toolBar);
 	}
 
 	public void addAssociatedPermissions(List<PermissionUserModel> permissions) {
 		associatedPermissions.add(permissions);
 	}
 
 	protected void addToolItem(Button item) {
 		toolBar.add(item);
 	}
 
 	private boolean containsModel(int userID) {
 		return associatedPermissions.findModel("id", Integer.valueOf(userID)) != null;
 	}
 	
 	public void drawSimple() {
 		final WorkingSet curWS = WorkingSetCache.impl.getCurrentWorkingSet();
 		final ClientUser curUser = SimpleSISClient.currentUser;
 		setAllowWrite(AuthorizationCache.impl.hasRight(curUser, AuthorizableObject.WRITE, curWS));
 		
 		associatedPermissions.addFilter(new StoreFilter<PermissionUserModel>() {
 			public boolean select(Store<PermissionUserModel> store, PermissionUserModel parent,
 					PermissionUserModel item, String property) {
 				if( property.equals("permission") && item.getPermission() != null && item.getPermission().toCSV().indexOf("write") > -1 )
 					return AuthorizationCache.impl.hasRight(curUser, AuthorizableObject.WRITE, curWS);
 				else
 					return true;
 			}
 		});
 		
 		if (!AuthorizationCache.impl.hasRight(curUser, AuthorizableObject.WRITE, curWS))
 			associatedPermissions.filter("permission");
 		
 		final boolean showAssessorColumn;
 		if (AuthorizationCache.impl.hasRight(curUser, AuthorizableObject.GRANT, curWS) 
 				&& (curUser.getProperty("quickGroup").contains("ws" + curWS.getId() + "assessor")
 						|| curUser.getProperty("quickGroup").contains("rlu")
 						|| curUser.getProperty("quickGroup").contains("sysAdmin"))) {
 			showAssessorColumn = true;
 		} else
 			showAssessorColumn = false;
 		
 		final FlexTable grid = new FlexTable();
 		grid.setSize("100%", "100%");
 		grid.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				Cell cell = grid.getCellForEvent(event);
 				if (cell == null) return;
 				for (int i = 0; i < grid.getRowCount(); i++)
 					if (i == cell.getRowIndex())
 						grid.getRowFormatter().addStyleName(i, "sis_ws_permissionpanel_row_selected");
 					else
 						grid.getRowFormatter().removeStyleName(i, "sis_ws_permissionpanel_row_selected");
 				selectedItem = associatedPermissions.getAt(grid.getRowCount());
 			}
 		});
 		grid.setHTML(0, 0, "<b>Name</b>");
 		grid.setHTML(0, 1, "<b>Basic Permissions</b>");
 		if (showAssessorColumn)
 			grid.setHTML(0, 2, "<b>Assessor</b>");
 		
 		int row = 1;
 		for (PermissionUserModel model : associatedPermissions.getModels())
 			addRowToGrid(grid, row++, showAssessorColumn, model);
 		
 		associatedPermissions.addStoreListener(new StoreListener<PermissionUserModel>() {
 			public void storeAdd(StoreEvent<PermissionUserModel> se) {
 				for (PermissionUserModel model : se.getModels())
 					addRowToGrid(grid, grid.getRowCount(), showAssessorColumn, model);
 			}
 			public void storeRemove(StoreEvent<PermissionUserModel> se) {
 				grid.removeRow(associatedPermissions.indexOf(se.getModel()));
 			}
 			@Override
 			public void storeClear(StoreEvent<PermissionUserModel> se) {
 				grid.removeAllRows();
 			}
 			@Override
 			public void storeDataChanged(StoreEvent<PermissionUserModel> se) {
 				for (PermissionUserModel model : se.getModels())
 					addRowToGrid(grid, associatedPermissions.indexOf(model), showAssessorColumn, model);
 			}
 		});
 		
 		getSaveButtons();
 		
 		RowLayout layout = new RowLayout();
 		setLayout(layout);
 		add(toolBar, new RowData(1, 25));
 		add(new HTML(instructions, true), new RowData(1, -1));
 		add(grid, new RowData(1, 1));
 		add(drawButtons(), new RowData(1, -1));
 		
 	}
 	
 	private void addRowToGrid(final FlexTable table, final int row, boolean showAssessorColumn, final PermissionUserModel model) {
 		table.setHTML(row, 0, model.getName());
 		table.getCellFormatter().setWidth(row, 0, "150px");
 		
 		table.setWidget(row, 1, new CheckBoxPermissionPanel(getOptions(), defaultPermission, model));
 		table.getCellFormatter().setWidth(row, 0, "250px");
 		
 		if (showAssessorColumn) {
 			final CheckBox box = new CheckBox();
 			box.setName("assessor");
 			box.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					model.set("assessor", box.getValue());
 				}
 			});
 			box.setValue(model.isAssessor());
 			
 			final HorizontalPanel panel = new HorizontalPanel();
 			panel.setWidth("100%");
 			panel.add(box);
 			
 			table.setWidget(row, 2, panel);
 			table.getCellFormatter().setWidth(row, 0, "100px");
 		}
 		
 	}
 
 	/**
 	 * Function that must be called when ready to display on the page. Must set
 	 * allowRead, write and assign before this method is called.
 	 */
 	public void draw() {
 		final WorkingSet curWS = WorkingSetCache.impl.getCurrentWorkingSet();
 		final ClientUser curUser = SimpleSISClient.currentUser;
 		
 		setAllowWrite(AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, WorkingSetCache.impl.getCurrentWorkingSet()));
 		
 		ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
 		ColumnConfig nameColumn = new ColumnConfig("name", "Name", widthOfNameColumn);
 		nameColumn.setRenderer(new GridCellRenderer<PermissionUserModel>() {
 			public Object render(PermissionUserModel model, String property, ColumnData config, int rowIndex, int colIndex, com.extjs.gxt.ui.client.store.ListStore<PermissionUserModel> store, com.extjs.gxt.ui.client.widget.grid.Grid<PermissionUserModel> grid) {
 				return model.getName();
 			}
 		});
 		columns.add(nameColumn);
 		
 		ColumnConfig permissionColumn = new ColumnConfig("permission",
 				"Basic Permissions", widthOfPermissionColumn);
 		
 		final PermissionComboBox permissionField = new PermissionComboBox();
 				
 		final ListStore<PermissionUserModel.PermissionsModelData> store = 
 			new ListStore<PermissionsModelData>();
 		for (String perm : new String[] { read, write, assign })
 			store.add(new PermissionsModelData(perm));
 						
 		permissionField.setStore(store);
 		
 		CellEditor editor = new CellEditor(permissionField);
 		editor.setCompleteOnEnter(true);
 		editor.setCancelOnEsc(true);
 		permissionColumn.setEditor(editor);
 		columns.add(permissionColumn);
 
 		CheckColumnConfig assessorColumn = new CheckColumnConfig("assessor", "Assessor Rights", 100);
 		if( AuthorizationCache.impl.hasRight(curUser, AuthorizableObject.GRANT, curWS)
 				 && (curUser.getProperty("quickGroup").contains("ws" + curWS.getId() + "assessor")
 				   || curUser.getProperty("quickGroup").contains("rlu")
 				   || curUser.getProperty("quickGroup").contains("sysAdmin") ) ) {
 			assessorColumn.setHidden(false);
 		} else
 			assessorColumn.setHidden(true);
 		
 		columns.add(assessorColumn);
 
 		ColumnModel cm = new ColumnModel(columns);
 
 		final EditorGrid<PermissionUserModel> permissionGrid = new EditorGrid<PermissionUserModel>(associatedPermissions, cm);
 		permissionGrid.addPlugin(assessorColumn);
 		associatedPermissions.addFilter(new StoreFilter<PermissionUserModel>() {
 			public boolean select(Store<PermissionUserModel> store, PermissionUserModel parent,
 					PermissionUserModel item, String property) {
 				if( property.equals("permission") && item.getPermission() != null && item.getPermission().toCSV().indexOf("write") > -1 )
 					return AuthorizationCache.impl.hasRight(curUser, AuthorizableObject.WRITE, curWS);
 				else
 					return true;
 			}
 		});
 		
 		if( !AuthorizationCache.impl.hasRight(curUser, AuthorizableObject.WRITE, curWS) )
 			associatedPermissions.filter("permission");
 		
 		GridSelectionModel<PermissionUserModel> model = new GridSelectionModel<PermissionUserModel>();
 		model.bindGrid(permissionGrid);
 		model.setFiresEvents(true);
 		model.addSelectionChangedListener(new SelectionChangedListener<PermissionUserModel>() {
 			public void selectionChanged(SelectionChangedEvent<PermissionUserModel> se) {
 				selectedItem = se.getSelectedItem();
 				removeButton.setEnabled(selectedItem != null);
 			}
 		});
 
 		permissionGrid.setSelectionModel(model);
 		permissionGrid.setAutoExpandColumn("permission");
 		permissionGrid.setBorders(true);
 		permissionGrid.addListener(Events.RowClick, new Listener<GridEvent<PermissionUserModel>>() {
 			public void handleEvent(GridEvent<PermissionUserModel> be) {
 				permissionGrid.getView().focusRow(be.getRowIndex());
 			}
 		});
 		
 		getSaveButtons();
 		
 		RowLayout layout = new RowLayout();
 		setLayout(layout);
 		add(toolBar, new RowData(1, 25));
 		add(new HTML(instructions, true), new RowData(1, -1));
 		add(permissionGrid, new RowData(1, 1));
 		add(drawButtons(), new RowData(1, -1));
 	}
 
 	protected abstract void onRemoveUsers(List<PermissionUserModel> removed);
 	
 	protected ButtonBar drawButtons() {
 		ButtonBar buttonBar = new ButtonBar();
 		buttonBar.setAlignment(HorizontalAlignment.RIGHT);
 		removeButton = new Button("Remove User",
 				new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				if (selectedItem != null) {
					//associatedPermissions.remove(selectedItem);
 					List<PermissionUserModel> l = new ArrayList<PermissionUserModel>();
 					l.add(selectedItem);
 					onRemoveUsers(l);
 				} else {
 					WindowUtils.errorAlert("Must first choose user to remove from permission group.");
 				}
 			}
 		});
 
 		Button addButton = new Button("Add User(s)", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				BrowseUsersWindow window = new BrowseUsersWindow();
 				window.setSelectionListener(new ComplexListener<List<ClientUser>>() {
 					public void handleEvent(List<ClientUser> selectedUsers) {
 						for (ClientUser user : selectedUsers) {
 							if (!containsModel(user.getId())) {
 								PermissionUserModel model = new PermissionUserModel(user, defaultPermission, false);
 								associatedPermissions.add(model);
 							}
 						}
 					}
 				});
 				window.setSelectedUsersHeading("Users to Add");
 				window.setPossibleUsersHeading("Search results");
 				window.setInstructions("<b>Add User:</b> Choose a recent user or search for a user and then drag and drop the user to the \"Users to Add\" list.  </br></br>");
 				
 				List<ClientUser> users = new ArrayList<ClientUser>();
 				// for (PermissionUserModel model :
 				// permissionGrid.getStore().getModels())
 				// users.add(model.getUser());
 				window.refresh(users);
 				window.show();
 			}
 		});
 
 		buttonBar.add(removeButton);
 		buttonBar.add(addButton);
 		buttonBar.add(new Button("Remove All Users", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				if (!associatedPermissions.getModels().isEmpty()) {
 					associatedPermissions.removeAll();
 					onRemoveUsers(associatedPermissions.getModels());
 				}
 			}
 		}));
 
 		return buttonBar;
 
 	}
 
 	public ListStore<PermissionUserModel> getAssociatedPermissions() {
 		return associatedPermissions;
 	}
 
 	public String getDefaultPermission() {
 		return defaultPermission;
 	}
 
 	public String getInstructions() {
 		return instructions;
 	}
 
 	protected List<String> getOptions() {
 		List<String> options = new ArrayList<String>();
 		if (allowRead)
 			options.add(read);
 		if (allowWrite)
 			options.add(write);
 		if (allowCreate)
 			options.add(create);
 		if (allowDelete)
 			options.add(delete);
 		if (allowAssign)
 			options.add(assign);
 
 		return options;
 	}
 
 	protected void getSaveButtons() {
 		Button save = new Button("Save", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				onSave();
 			}
 		});
 		save.setIconStyle("icon-save");
 		save.setTitle("Save and Continue Editing");
 
 		addToolItem(save);
 	}
 
 	public boolean isAllowAssign() {
 		return allowAssign;
 	}
 
 	public boolean isAllowCreate() {
 		return allowCreate;
 	}
 
 	public boolean isAllowDelete() {
 		return allowDelete;
 	}
 
 	public boolean isAllowRead() {
 		return allowRead;
 	}
 
 	public boolean isAllowWrite() {
 		return allowWrite;
 	}
 
 	public abstract void onSave();
 
 	public void setAllowAssign(boolean allowAssign) {
 		this.allowAssign = allowAssign;
 	}
 
 	public void setAllowCreate(boolean allowCreate) {
 		this.allowCreate = allowCreate;
 	}
 
 	public void setAllowDelete(boolean allowDelete) {
 		this.allowDelete = allowDelete;
 	}
 
 	public void setAllowRead(boolean allowRead) {
 		this.allowRead = allowRead;
 	}
 
 	public void setAllowWrite(boolean allowWrite) {
 		this.allowWrite = allowWrite;
 	}
 
 	public void setAssociatedPermissions(
 			ListStore<PermissionUserModel> associatedPermissions) {
 		this.associatedPermissions = associatedPermissions;
 	}
 
 	public void setDefaultPermission(String defaultPermission) {
 		this.defaultPermission = defaultPermission;
 	}
 
 	public void setInstructions(String instructions) {
 		this.instructions = instructions;
 	}
 
 	private static class CheckBoxPermissionPanel extends HorizontalPanel {
 		
 		private final PermissionUserModel model;
 		private final String defaultPermission;
 		
 		public CheckBoxPermissionPanel(List<String> options, String defaultPermission, PermissionUserModel model) {
 			super();
 			this.defaultPermission = defaultPermission;
 			this.model = model;
 			PermissionUserModel.PermissionsModelData value = model.getPermission();
 			for (String option : options) {
 				CheckBox box = new CheckBox();
 				box.setName(option);
 				box.addClickHandler(new ClickHandler() {
 					public void onClick(ClickEvent event) {
 						updateModel();
 					}
 				});
 				box.setValue(value.hasPermission(option));
 				add(box);
 				add(new HTML(option));
 			}
 		}
 		
 		private void updateModel() {
 			final List<String> selected = new ArrayList<String>();
 			CheckBox defaultBox = null;
 			for (int i = 0; i < getWidgetCount(); i += 2) {
 				Widget current = getWidget(i);
 				if (current instanceof CheckBox) {
 					CheckBox box = (CheckBox)current;
 					if (defaultPermission.equals(box))
 						defaultBox = box;
 					
 					if (box.getValue())
 						selected.add(box.getName());
 				}
 			}
 			if (selected.isEmpty()) {
 				WindowUtils.errorAlert("Must give user some permission or remove them from permission list.");
 				if (defaultBox != null) {
 					selected.add(defaultBox.getName());
 					defaultBox.setValue(true);
 				}
 			}
 			
 			StringBuilder csv = new StringBuilder();
 			for (Iterator<String> iter = selected.listIterator(); iter.hasNext(); )
 				csv.append(iter.next() + (iter.hasNext() ? "," : ""));
 			
 			model.set("permission", csv.toString());
 		}
 		
 	}
 	
 }
