 package org.iucn.sis.client.panels.permissions;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.WorkingSetCache;
 import org.iucn.sis.client.panels.permissions.NewPermissionEditor.PermissionAttributeOptions;
 import org.iucn.sis.client.panels.permissions.NewPermissionEditor.PermissionResource;
 import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
 import org.iucn.sis.shared.api.models.AssessmentType;
 import org.iucn.sis.shared.api.models.Permission;
 import org.iucn.sis.shared.api.models.PermissionGroup;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.WorkingSet;
 import org.iucn.sis.shared.api.utils.CaseInsensitiveAlphanumericComparator;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.data.ModelKeyProvider;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.MenuEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.fx.FxConfig;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.store.StoreSorter;
 import com.extjs.gxt.ui.client.widget.Component;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.HtmlContainer;
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.ListView;
 import com.extjs.gxt.ui.client.widget.ListViewSelectionModel;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ButtonBar;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.extjs.gxt.ui.client.widget.form.FieldSet;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.layout.FormLayout;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.extjs.gxt.ui.client.widget.menu.MenuItem;
 import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
 import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.events.SimpleListener;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 
 public class NewPermissionGroupEditor extends LayoutContainer implements DrawsLazily {
 	
 	private final ListView<BaseModelData> view; 
 	private final ListStore<BaseModelData> store;
 	
 	private final Html fullNameDisplay;
 	
 	private final LayoutContainer center;
 	private final FormPanel editingArea;
 	
 	private List<WorkingSet> workingSets;
 	
 	private TextField<String> name;
 	private NewPermissionEditor defaultPermission;
 	private LayoutContainer scopedPermissions;
 	private ComboBox<ScopeModelData> scope;
 	private Html scopeDisplay;
 	private ComboBox<BaseModelData> parent;
 	private LayoutContainer featurePermissions;
 	
 	private String scopeURI;
 	
 	private LayoutContainer completeEditingArea;
 	
 	private EditablePermissionGroup current;
 	private boolean initializing;
 	
 	public NewPermissionGroupEditor() {
 		super();
 		setLayout(new FillLayout());
 		
 		this.store = new ListStore<BaseModelData>();
 		this.store.setStoreSorter(new StoreSorter<BaseModelData>(new CaseInsensitiveAlphanumericComparator()));
 		this.store.setKeyProvider(new ModelKeyProvider<BaseModelData>() {
 			public String getKey(BaseModelData model) {
 				int id = model.get("id");
 				return Integer.toString(id);
 			}
 		});
 		
 		this.view = new ListView<BaseModelData>(store);
 		
 		this.fullNameDisplay = new Html();
 		this.center = new LayoutContainer(new FillLayout());
 		this.center.setLayoutOnChange(true);
 		
 		
 		FormLayout layout = new FormLayout();
 		layout.setLabelWidth(80);
 		layout.setDefaultWidth(320);
 		layout.setLabelPad(25);
 		
 		this.editingArea = new FormPanel();
 		this.editingArea.setBodyBorder(false);
 		this.editingArea.setBorders(false);
 		this.editingArea.setHeaderVisible(false);
 		this.editingArea.setLayout(layout);
 		this.editingArea.setScrollMode(Scroll.AUTO);
 		
 		name = new TextField<String>();
 		name.setFieldLabel("Name");
 		
 		defaultPermission = new NewPermissionEditor(false);
 		
 		ListStore<ScopeModelData> scopeStore = new ListStore<ScopeModelData>();
 		ScopeModelData defaultScope;
 		scopeStore.add(defaultScope = new ScopeModelData("All Taxonomy", ""));
 		scopeStore.add(new ScopeModelData("Taxon...", "taxon/"));
 		scopeStore.add(new ScopeModelData("All Working Sets", "workingSets/"));
 		scopeStore.add(new ScopeModelData("Working Set...", "workingSet/"));
 		
 		scope = new ComboBox<ScopeModelData>();
 		scope.setAllowBlank(false);
 		scope.setEditable(false);
 		scope.setFieldLabel("Scope");
 		scope.setForceSelection(true);
 		scope.setStore(scopeStore);
 		scope.setTriggerAction(TriggerAction.ALL);
 		scope.setValue(defaultScope);
 		scope.addSelectionChangedListener(new SelectionChangedListener<ScopeModelData>() {
 			public void selectionChanged(SelectionChangedEvent<ScopeModelData> se) {
 				if (!initializing && se.getSelectedItem() != null) {
 					onScopeChanged(se.getSelectedItem());
 				}
 			}
 		});
 		
 		scopeDisplay = new Html();
 		
 		scopedPermissions = new LayoutContainer();
 		featurePermissions = new LayoutContainer();
 		
 		parent = new ComboBox<BaseModelData>();
 		parent.setAllowBlank(true);
 		parent.setEditable(false);
 		parent.setFieldLabel("Parent");
 		parent.setForceSelection(true);
 		parent.setStore(store);
 		parent.setTriggerAction(TriggerAction.ALL);
 	}
 	
 	public void draw(final DoneDrawingCallback callback) {
 		WorkingSetCache.impl.getGrantableWorkingSets(new GenericCallback<List<WorkingSet>>() {
 			public void onSuccess(List<WorkingSet> result) {
 				NewPermissionGroupEditor.this.workingSets = result;
 				
 				center.add(completeEditingArea = createEditingArea());
 				
 				final LayoutContainer container = new LayoutContainer(new BorderLayout());
 				container.add(createListingArea(), new BorderLayoutData(LayoutRegion.WEST, 150, 150, 150));
 				container.add(center, new BorderLayoutData(LayoutRegion.CENTER));
 				
 				add(container);
 				
 				stopEditing();
 				
 				callback.isDrawn();
 			}
 			public void onFailure(Throwable caught) {
 				onSuccess(new ArrayList<WorkingSet>());
 			}
 		});
 	}
 	
 	public void setPermissionGroup(PermissionGroup permissionGroup) {
 		BaseModelData model = store.findModel(Integer.toString(permissionGroup.getId()));
 		if (model != null)
 			view.getSelectionModel().select(model, false);
 	}
 	
 	private void setCurrent(PermissionGroup permissionGroup) {
 		initializing = true;
 		
 		center.removeAll();
 		center.add(completeEditingArea);
 		
 		this.current = new EditablePermissionGroup(permissionGroup);
 		
 		this.fullNameDisplay.setHtml(current.getName());
 		
 		name.setValue(current.getName());
 		
 		scopeURI = current.getScopeURI();
 		
 		if (current.getScopeURI().startsWith("taxon")) {
 			scope.setValue(scope.getStore().getAt(1));
 			scopeDisplay.setHtml(current.getScopeURI());
 		}
 		else if (current.getScopeURI().equals("workingSets")) {
 			scope.setValue(scope.getStore().getAt(2));
 			scopeDisplay.setHtml("Any taxa included in a user's working sets.");
 		}
 		else if (current.getScopeURI().startsWith("workingSet/")) {
 			scope.setValue(scope.getStore().getAt(3));
 			
 			String[] split = current.getScopeURI().split(",");
 			StringBuilder html = new StringBuilder();
 			html.append(split.length);
 			html.append(" Working Set");
 			if (split.length > 1)
 				html.append('s');
 			html.append("<br/>");
 			
 			for (String id : split) {
 				WorkingSet ws;
 				try {
 					ws =  WorkingSetCache.impl.getWorkingSet(Integer.valueOf(id));
 				} catch (Exception e) {
 					continue;
 				}
 				if (ws == null)
 					html.append("Unknown working set (ID#" + id + ")<br/>");
 				else
 					html.append(ws.getName() + "<br/>");
 			}
 			
 			scopeDisplay.setHtml(html.toString());
 		}
 		else {
 			scope.setValue(scope.getStore().getAt(0));
 			scopeDisplay.setHtml("");
 			scopeURI = "";
 		}
 		
 		scopedPermissions.removeAll();
 		featurePermissions.removeAll();
 		defaultPermission.setValue(new Permission(PermissionGroup.DEFAULT_PERMISSION_URI));
 		
 		for (Permission permission : current.getPermissions()) {
 			if (PermissionGroup.DEFAULT_PERMISSION_URI.equals(permission.getUrl()))
 				defaultPermission.setValue(permission);
 			else if (permission.getUrl().startsWith("feature")) {
 				NewPermissionEditor editor = new NewPermissionEditor(true);
 				editor.setValue(permission);
 				
 				featurePermissions.add(editor);
 			}
 			else {
 				PermissionAttributeOptions options = null;
 				if (permission.getUrl().startsWith("resource/assessment"))
 					options = PermissionAttributeOptions.Assessment;
 				
 				NewPermissionEditor editor = new NewPermissionEditor(true, options);
 				editor.setValue(permission);
 				
 				scopedPermissions.add(editor);
 			}
 		}
 		
 		featurePermissions.layout();
 		scopedPermissions.layout();
 		
 		if (current.getParent() == null)
 			parent.setValue(null);
 		else
 			parent.setValue(parent.getStore().findModel(current.getParent().getId()+""));
 		
 		initializing = false;
 	}
 	
 	private void onScopeChanged(ScopeModelData value) {
 		if (value.isAllTaxonomy()) {
 			scopeURI = "";
 			scopeDisplay.setHtml("");
 		}
 		else if (value.isAllWorkingSets()) {
 			scopeURI = "workingSets";
 			scopeDisplay.setHtml("Any taxa included in a user's working sets.");
 		}
 		else if (value.isWorkingSet()) {
 			final Window window = WindowUtils.newWindow("Select Working Set(s)");
 			window.setLayout(new FillLayout());
 			window.setSize(600, 400);
 			window.setModal(true);
 			window.setClosable(false);
 			window.add(new PermissionWorkingSetList(workingSets, new ComplexListener<List<WorkingSet>>() {
 				public void handleEvent(List<WorkingSet> sets) {
 					window.hide();
 					if (sets.isEmpty())
 						scope.setValue(scope.getStore().getAt(0));
 					else {
 						StringBuilder html = new StringBuilder();
 						html.append(sets.size());
 						html.append(" Working Set");
 						if (sets.size() > 1)
 							html.append('s');
 						html.append("<br/>");
 						
 						StringBuilder ids = new StringBuilder();
 						for (Iterator<WorkingSet> iter = sets.listIterator(); iter.hasNext(); ) {
 							WorkingSet ws = iter.next();
 							boolean comma = iter.hasNext();
 							html.append(ws.getName() + "<br/>");
 							ids.append(ws.getId() + (comma ? "," : ""));
 						}
 						
 						scopeURI = "workingSet/" + ids.toString();
 						scopeDisplay.setHtml(html.toString());
 					}
 				}
 			}));
 			window.show();
 		}
 		else if (value.isTaxon()) {
 			final PermissionScopeTaxonomyBrowser browser;
			final Window window = WindowUtils.newWindow("Select Working Set(s)");
 			window.setLayout(new FillLayout());
 			window.setSize(600, 400);
 			window.setModal(true);
 			window.setClosable(false); 
 			window.add(browser = new PermissionScopeTaxonomyBrowser(new ComplexListener<Taxon>() {
 				public void handleEvent(Taxon taxon) {
 					window.hide();
 					if (taxon == null)
 						scope.setValue(scope.getStore().getAt(0));
 					else {
						scopeURI = "taxon/" + taxon.getFullName() + 
							(taxon.getFootprint().length > 0 ? ("/" + taxon.getFootprint()[0]) : "");
 						scopeDisplay.setHtml(scopeURI);
 					}
 				}
 			}));
 			browser.fireEvent(Events.Show);
 			window.show();
 		}
 	}
 	
 	private void stopEditing() {
 		editingArea.reset();
 		
 		center.removeAll();
 		fullNameDisplay.setHtml("");
 		view.getSelectionModel().deselectAll();
 		
 		current = null;
 		
 		showDefaultScreen();
 	}
 	
 	private void showDefaultScreen() {
 		final LayoutContainer container = new LayoutContainer(new CenterLayout());
 		
 		final HtmlContainer instructions = new HtmlContainer();
 		instructions.addStyleName("gwt-background");
 		instructions.setSize(200, 100);
 		instructions.setHtml("<b>Instructions</b>: Select " +
 			"the permission group from the list on the left which you would " +
 			"like to edit, or click \"Add\" to create a new permission group.");
 		instructions.setBorders(true);
 		
 		container.add(instructions);
 		
 		center.removeAll();
 		center.add(container);
 	}
 	
 	private LayoutContainer createEditingArea() {
 		final ToolBar bar = new ToolBar();
 		bar.add(fullNameDisplay);
 		bar.add(new FillToolItem());
 		bar.add(new Button("Summarize...", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				PermissionGroupSummary.summarize(current);
 			}
 		}));
 		
 		final ButtonBar bottom = new ButtonBar();
 		bottom.setAlignment(HorizontalAlignment.CENTER);
 		bottom.setSpacing(10);
 		bottom.add(new Button("Save Changes", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				save();
 			}
 		}));
 		bottom.add(new Button("Cancel Changes", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				current.rejectChanges();
 				stopEditing();
 			}
 		}));
 		
 		FieldSet set = new FieldSet();
 		set.setLayout(new FormLayout());
 		set.setHeading("Permission Group Name");
 		set.add(name);
 		editingArea.add(set);
 		
 		FieldSet defaultPerm = new FieldSet();
 		defaultPerm.setLayout(new FormLayout());
 		defaultPerm.setHeading("Default Permissions");
 		defaultPerm.add(defaultPermission);
 		editingArea.add(defaultPerm);
 		
 		FieldSet inheritance = new FieldSet();
 		inheritance.setLayout(new FormLayout());
 		inheritance.setHeading("Inheritance");
 		inheritance.add(new Html("<p><i>The scoped permissions from the " +
 				"selected group will be inherited and used only within " +
 				"the scope of this group.  Those permission will be added to " +
 				"and will replace any of the scoped permissions below." +
 				"</i></p>"));
 		inheritance.add(parent);
 		editingArea.add(inheritance);
 		
 		FieldSet scopeArea = new FieldSet();
 		scopeArea.setLayout(new FormLayout());
 		scopeArea.setHeading("Permission Group Scope");
 		scopeArea.add(scope);
 		scopeArea.add(scopeDisplay);
 		editingArea.add(scopeArea);
 		
 		ButtonBar scopedPermBar = new ButtonBar();
 		scopedPermBar.setAlignment(HorizontalAlignment.CENTER);
 		scopedPermBar.add(new Button("Add Resource", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				ComplexListener<PermissionResource> callback = new ComplexListener<PermissionResource>() {
 					public void handleEvent(PermissionResource eventData) {
 						if (scopedPermissions.getItemByItemId(eventData.getUrl()) != null)
 							WindowUtils.errorAlert("A permission already exists for this resource.");						
 						else {
 							NewPermissionEditor editor = new NewPermissionEditor(true, eventData.getOption());
 							editor.setValue(new Permission(eventData.getUrl()));
 							
 							scopedPermissions.add(editor);
 							scopedPermissions.layout();
 						}
 					}
 				};
 				getResourceMenu(callback, workingSets).show(ce.getButton());
 			}
 		}));
 		
 		FieldSet scopedPerms = new FieldSet();
 		scopedPerms.setLayout(new FormLayout());
 		scopedPerms.setHeading("Scoped Permissions");
 		scopedPerms.add(scopedPermissions);
 		scopedPerms.add(scopedPermBar);
 		
 		editingArea.add(scopedPerms);
 		
 		ButtonBar featurePermBar = new ButtonBar();
 		featurePermBar.setAlignment(HorizontalAlignment.CENTER);
 		featurePermBar.add(new Button("Add Feature", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				ComplexListener<PermissionResource> callback = new ComplexListener<PermissionResource>() {
 					public void handleEvent(PermissionResource eventData) {
 						if (featurePermissions.getItemByItemId(eventData.getUrl()) != null)
 							WindowUtils.errorAlert("A permission already exists for this feature.");						
 						else {
 							NewPermissionEditor editor = new NewPermissionEditor(true, eventData.getOption());
 							editor.setValue(new Permission(eventData.getUrl()));
 							
 							featurePermissions.add(editor);
 							featurePermissions.layout();
 						}
 					}
 				};
 				getFeatureMenu(callback).show(ce.getButton());
 			}
 		}));
 		
 		FieldSet featurePerms = new FieldSet();
 		featurePerms.setLayout(new FormLayout());
 		featurePerms.setHeading("Feature Permissions");
 		featurePerms.add(featurePermissions);
 		featurePerms.add(featurePermBar);
 		
 		editingArea.add(featurePerms);
 		
 		
 		final LayoutContainer container = new LayoutContainer(new BorderLayout());
 		container.add(bar, new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25));
 		container.add(editingArea, new BorderLayoutData(LayoutRegion.CENTER));
 		container.add(bottom, new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
 		
 		return container;
 	}
 	
 	private BaseModelData newModelData(final PermissionGroup permissionGroup) {
 		final BaseModelData model = new BaseModelData();
 		updateModelData(model, permissionGroup);
 		return model;
 	}
 	
 	private BaseModelData updateModelData(final BaseModelData model, final PermissionGroup permissionGroup) {
 		model.set("model", permissionGroup);
 		model.set("id", permissionGroup.getId());
 		model.set("name", permissionGroup.getName());
 		model.set("text", permissionGroup.getName());
 		
 		return model;
 	}
 	
 	private LayoutContainer createListingArea() {
 		List<PermissionGroup> list = AuthorizationCache.impl.listGroups();
 		for (PermissionGroup permissionGroup : list)
 			if (!permissionGroup.getName().matches("^ws\\d+.*"))
 				store.add(newModelData(permissionGroup));
 		store.sort("name", SortDir.ASC);
 		
 		final ListViewSelectionModel<BaseModelData> sm = 
 			new ListViewSelectionModel<BaseModelData>();
 		sm.setSelectionMode(SelectionMode.SINGLE);
 		sm.addSelectionChangedListener(new SelectionChangedListener<BaseModelData>() {
 			public void selectionChanged(SelectionChangedEvent<BaseModelData> se) {
 				BaseModelData selection = se.getSelectedItem();
 				if (selection != null)
 					setCurrent((PermissionGroup)selection.get("model"));
 			}
 		});
 		
 		view.setSelectionModel(sm);
 		
 		final ButtonBar bar = new ButtonBar();
 		bar.setAlignment(HorizontalAlignment.CENTER);
 		bar.add(new Button("Add", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				add();
 			}
 		}));
 		bar.add(new Button("Remove", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				final BaseModelData selected = sm.getSelectedItem();
 				if (selected != null) {
 					final PermissionGroup permissionGroup = selected.get("model");
 					WindowUtils.confirmAlert("Confirm", "Are you sure you want to delete this permission group?", new WindowUtils.SimpleMessageBoxListener() {
 						public void onYes() {
 							delete(permissionGroup, new SimpleListener() {
 								public void handleEvent() {
 									store.remove(selected);
 								}
 							});
 						}
 					});
 				}
 			}
 		}));
 		
 		final LayoutContainer container = new LayoutContainer(new BorderLayout());
 		container.add(view, new BorderLayoutData(LayoutRegion.CENTER));
 		container.add(bar, new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
 		
 		return container;
 	}
 	
 	public void add() {
 		WindowUtils.SimpleMessageBoxListener callback = new WindowUtils.SimpleMessageBoxListener() {
 			public void onYes() {
 				stopEditing();
 				
 				PermissionGroup permissionGroup = new PermissionGroup();
 				permissionGroup.setScopeURI("");
 				
 				setCurrent(permissionGroup);
 			}
 		};
 		
 		if (current == null)
 			callback.onYes();
 		else {
 			WindowUtils.confirmAlert("Confirm", "Adding a new " +
 					"permission group now will cancel any current changes " +
 					"you have.  Continue?", callback);
 		}
 	}
 	
 	private void delete(final PermissionGroup permissionGroup, final SimpleListener listener) {
 		WindowUtils.showLoadingAlert("Deleting permission group...");
 		AuthorizationCache.impl.removeGroup(permissionGroup.getName(), new GenericCallback<PermissionGroup>() {
 			public void onSuccess(PermissionGroup permissionGroup) {
 				WindowUtils.hideLoadingAlert();
 				WindowUtils.infoAlert("Saved", "Permission Group has been deleted.");
 				listener.handleEvent();
 				stopEditing();
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				WindowUtils.hideLoadingAlert();
 				WindowUtils.errorAlert("Unable to delete the permission group");
 			}
 		});
 	}
 	
 	private void save() {
 		stageChanges();
 		
 		if (validateEntry()) {
 			if (current.getId() == 0) {
 				WindowUtils.showLoadingAlert("Saving Changes...");
 				AuthorizationCache.impl.saveGroup(current, new GenericCallback<String>() {
 					public void onSuccess(String result) {
 						WindowUtils.hideLoadingAlert();
 						Info.display("Success", "Changes saved.");
 						
 						current.acceptChanges();
 						
 						BaseModelData model = newModelData(AuthorizationCache.impl.getGroup(current.getName()));
 						
 						store.add(model);
 						store.sort("name", SortDir.ASC);
 						
 						stopEditing();
 						
 					}
 					public void onFailure(Throwable caught) {
 						WindowUtils.hideLoadingAlert();
 						WindowUtils.errorAlert("Failed to save new group, please try again laer.");
 					}
 				});
 			}
 			else {
 				AuthorizationCache.impl.updateGroup(current, new GenericCallback<PermissionGroup>() {
 					public void onSuccess(PermissionGroup result) {
 						WindowUtils.hideLoadingAlert();
 						Info.display("Success", "Changes saved.");
 						
 						current.acceptChanges();
 						
 						BaseModelData model = view.getSelectionModel().getSelectedItem();
 						
 						updateModelData(model, result);
 						
 						view.refresh();
 						
 						stopEditing();
 					}
 					public void onFailure(Throwable caught) {
 						WindowUtils.hideLoadingAlert();
 						WindowUtils.errorAlert("Failed to save new group, please try again laer.");
 					}
 				});
 			}
 		}
 	}
 	
 	private void stageChanges() {
 		current.sink(new PermissionGroup(), current);
 
 		current.setName(name.getValue());
 		
 		current.setScopeURI(scopeURI);
 		
 		for (Component component : scopedPermissions.getItems())
 			if (component instanceof NewPermissionEditor) {
 				NewPermissionEditor editor = (NewPermissionEditor)component;
 				editor.stageChanges();
 				current.getPermissions().add(editor.getValue());
 			}
 		
 		for (Component component : featurePermissions.getItems())
 			if (component instanceof NewPermissionEditor) {
 				NewPermissionEditor editor = (NewPermissionEditor)component;
 				editor.stageChanges();
 				current.getPermissions().add(editor.getValue());
 			}
 		
 		defaultPermission.stageChanges();
 		current.getPermissions().add(defaultPermission.getValue());
 		
 		BaseModelData inheritsFrom = parent.getValue();
 		if (inheritsFrom != null) {
 			int parentID = inheritsFrom.get("id");
 			current.setParent(AuthorizationCache.impl.getGroup(parentID));
 		}
 		else
 			current.setParent(null);
 	}
 	
 	private boolean validateEntry() {
 		name.clearInvalid();
 		parent.clearInvalid();
 		
 		String message = null;
 		if (isBlank(current.getName())) {
 			message = "Please enter a name for this group.";
 			name.el().blink(FxConfig.NONE);
 			name.forceInvalid(message);
 			name.focus();
 		}
 		else {
 			PermissionGroup existing = AuthorizationCache.impl.getGroup(current.getName());
 			if (existing != null && current.getId() != existing.getId()) {
 				message = "A group with this name already exists.  Please enter a different name";
 				name.el().blink(FxConfig.NONE);
 				name.forceInvalid(message);
 				name.focus();
 			}
 		}
 		
 		if (current.getParent() != null && current.getName().equals(current.getParent().getName())) {
 			message = "Invalid parent group selected.";
 			parent.el().blink(FxConfig.NONE);
 			parent.forceInvalid(message);
 		}
 		
 		if (message == null)
 			return true;
 		else
 			return false;
 			
 	}
 	
 	private boolean isBlank(String value) {
 		return value == null || "".equals(value);
 	}
 	
 	private Menu getFeatureMenu(final ComplexListener<PermissionResource> callback) {
 		final SelectionListener<MenuEvent> listener = new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				MenuItem item = (MenuItem)ce.getItem();
 				
 				String value = item.getData("value");
 				PermissionAttributeOptions option = item.getData("option");
 				
 				callback.handleEvent(new PermissionResource(value, option));
 			}
 		};
 		
 		Menu features = new Menu(); 
 		List<AuthorizableFeature> featureList = Arrays.asList(AuthorizableFeature.features);
 		Collections.sort(featureList);
 		for (AuthorizableFeature feature : featureList)
 			features.add(newMenuItem(feature.getDescription(), feature.getFullURI(), listener));
 		
 		return features;
 	}
 	
 	private Menu getResourceMenu(final ComplexListener<PermissionResource> callback, final List<WorkingSet> workingSets) {
 		final SelectionListener<MenuEvent> listener = new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				MenuItem item = (MenuItem)ce.getItem();
 				
 				String value = item.getData("value");
 				PermissionAttributeOptions option = item.getData("option");
 				
 				callback.handleEvent(new PermissionResource(value, option));
 			}
 		};
 		
 		Menu resources = new Menu();
 		
 		MenuItem assessment = new MenuItem("Assessment"); {
 			Menu typeMenu = new Menu();
 			List<AssessmentType> permittable = new ArrayList<AssessmentType>();
 			for (int id : new int[] { AssessmentType.DRAFT_ASSESSMENT_STATUS_ID, AssessmentType.SUBMITTED_ASSESSMENT_STATUS_ID, 
 					AssessmentType.FOR_PUBLICATION_ASSESSMENT_STATUS_ID, AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID})
 				permittable.add(AssessmentType.getAssessmentType(id));
 			
 			for (AssessmentType type : permittable)
 				typeMenu.add(newMenuItem(type.getDisplayName(true), "resource/assessment/" + type.getName(), PermissionAttributeOptions.Assessment, listener));
 			
 			assessment.setSubMenu(typeMenu);
 		}
 		resources.add(assessment);
 		
 		resources.add(newMenuItem("Reference", "resource/reference", listener));
 		
 		resources.add(newMenuItem("Taxon", "resource/taxon", listener));
 		
 		MenuItem workingSet = new MenuItem("Working Set"); {
 			Menu listWS = new Menu();
 			listWS.setMaxHeight(250);
 			
 			listWS.add(newMenuItem("(All)", "resource/workingSet", listener));
 			if (!workingSets.isEmpty())
 				listWS.add(new SeparatorMenuItem());
 			for (WorkingSet ws : workingSets)
 				listWS.add(newMenuItem(ws.getName(), "resource/workingSet/" + ws.getId(), listener));
 			
 			workingSet.setSubMenu(listWS);
 		}
 		resources.add(workingSet);
 		
 		return resources;
 	}
 	
 	private MenuItem newMenuItem(String text, String value, SelectionListener<MenuEvent> listener) {
 		return newMenuItem(text, value, null, listener);
 	}
 	
 	private MenuItem newMenuItem(String text, String value, PermissionAttributeOptions option, SelectionListener<MenuEvent> listener) {
 		MenuItem item = new MenuItem(text);
 		item.setData("value", value);
 		item.setData("option", option);
 		item.addSelectionListener(listener);
 		
 		return item;
 	}
 	
 	private static class ScopeModelData extends BaseModelData {
 		
 		private static final long serialVersionUID = 1L;
 		
 		public ScopeModelData(String text, String value) {
 			super();
 			set("text", text);
 			set("value", value);
 		}
 		
 		private String getValue() {
 			return get("value");
 		}
 		
 		public boolean isAllTaxonomy() {
 			return "".equals(getValue());
 		}
 		
 		public boolean isAllWorkingSets() {
 			return "workingSets".equals(getValue());
 		}
 		
 		public boolean isWorkingSet() {
 			return "workingSet/".equals(getValue());
 		}
 		
 		public boolean isTaxon() {
 			return "taxon/".equals(getValue());
 		}
 		
 	}
 	
 	@SuppressWarnings("unused")
 	private static class EditablePermissionGroup extends PermissionGroup {
 
 		private static final long serialVersionUID = 1L;
 		
 		private final PermissionGroup permissionGroup;
 		
 		public EditablePermissionGroup(PermissionGroup permissionGroup) {
 			this.permissionGroup = permissionGroup;
 			if (permissionGroup.getId() != 0)
 				setID(permissionGroup.getId());
 			setChildren(permissionGroup.getChildren());
 			setUsers(permissionGroup.getUsers());
 			init();
 		}
 		
 		public PermissionGroup getModel() {
 			return permissionGroup;
 		}
 		
 		public void rejectChanges() {
 			init();
 		}
 		
 		public void acceptChanges() {
 			sink(this, permissionGroup);
 		}
 		
 		private void init() {
 			sink(permissionGroup, this);
 		}
 		
 		public void sink(PermissionGroup source, PermissionGroup target) {
 			target.setName(source.getName());
 			target.setPermissions(source.getPermissions());
 			target.setParent(source.getParent());
 			target.setScopeURI(source.getScopeURI());
 		}	
 	}
 
 }
