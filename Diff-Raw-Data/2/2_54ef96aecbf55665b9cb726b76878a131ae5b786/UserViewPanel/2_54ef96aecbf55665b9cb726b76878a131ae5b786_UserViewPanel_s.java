 /**
  *
  */
 package org.iucn.sis.client.panels.users;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.models.ClientUser;
 import org.iucn.sis.client.api.ui.users.panels.ContentManager;
 import org.iucn.sis.client.api.ui.users.panels.HasRefreshableContent;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
 import org.iucn.sis.shared.api.models.User;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FieldEvent;
 import com.extjs.gxt.ui.client.event.GridEvent;
 import com.extjs.gxt.ui.client.event.KeyListener;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.Popup;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.grid.CellEditor;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
 import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
 import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.lwxml.shared.utils.ArrayUtils;
 import com.solertium.util.extjs.client.CheckboxMultiTriggerField;
 import com.solertium.util.extjs.client.GenericPagingLoader;
 import com.solertium.util.extjs.client.PagingLoaderFilter;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.portable.PortableAlphanumericComparator;
 
 /**
  * UserViewPanel.java
  * 
  * Panel that allows for perusing of users to view and in-line edit their
  * profile information.
  * 
  * @author carl.scott <carl.scott@solertium.com>
  * 
  */
 public class UserViewPanel extends LayoutContainer implements HasRefreshableContent {
 
 	private final LayoutContainer center;
 
 	private EditorGrid<UserModelData> userGrid;
 	private ListStore<UserModelData> store;
 	private GenericPagingLoader<UserModelData> loader;
 	private PagingToolBar pagingBar;
 
 	private TextField<String> usernameFilter;
 	private TextField<String> firstFilter;
 	private TextField<String> lastFilter;
 	private TextField<String> nicknameFilter;
 	private TextField<String> affiliationFilter;
 	private SimpleComboBox<String> activeAccountFilter;
 	private FormPanel filterPanel;
 	private Popup filterPopup;
 
 	private final CheckboxMultiTriggerField permissionGroups;
 
 	public UserViewPanel(ContentManager contentManager) {
 		super();
 
 		setScrollMode(Scroll.NONE);
 
 		center = new LayoutContainer();
 		center.setLayout(new FillLayout());
 		center.setScrollMode(Scroll.NONE);
 
 		loader = new GenericPagingLoader<UserModelData>();
 		store = new ListStore<UserModelData>(loader.getPagingLoader());
 
 		ArrayList<String> groups = new ArrayList<String>(AuthorizationCache.impl.getGroups().keySet());
 		ArrayUtils.quicksort(groups, new PortableAlphanumericComparator());
 		permissionGroups = new CheckboxMultiTriggerField(groups);
 		permissionGroups.setDelimiter(",");
 		permissionGroups.setFilterRegex("^ws\\d+.*");
 
 		pagingBar = new PagingToolBar(50);
 		pagingBar.bind(loader.getPagingLoader());
 
 		usernameFilter = new TextField<String>();
 		usernameFilter.setFieldLabel("Username");
 		firstFilter = new TextField<String>();
 		firstFilter.setFieldLabel("First Name");
 		lastFilter = new TextField<String>();
 		lastFilter.setFieldLabel("Last Name");
 		nicknameFilter = new TextField<String>();
 		nicknameFilter.setFieldLabel("Nickname");
 		affiliationFilter = new TextField<String>();
 		affiliationFilter.setFieldLabel("Affiliation");
 		activeAccountFilter = new SimpleComboBox<String>();
 		activeAccountFilter.setFieldLabel("Active Account");
 		activeAccountFilter.add("");
 		activeAccountFilter.add("true");
 		activeAccountFilter.add("false");
 		activeAccountFilter.findModel("").set("text", "Active");
 		activeAccountFilter.findModel("true").set("text", "Active");
 		activeAccountFilter.findModel("false").set("text", "Disabled");
 		activeAccountFilter.setEditable(false);
 
 		loader.setFilter(new PagingLoaderFilter<UserModelData>() {
 			public boolean filter(UserModelData item, String property) {
 				String active = (activeAccountFilter.getValue() == null || 
 					"".equals(activeAccountFilter.getValue().getValue())) ? 
 					null : activeAccountFilter.getValue().getValue();
 				
 				if (active != null & filterOut(active, (String) item.get("sis")))
 					return true;
 				if (filterOut(usernameFilter.getValue(), (String) item.get("username")))
 					return true;
 				if (filterOut(firstFilter.getValue(), (String) item.get("firstName")))
 					return true;
 				if (filterOut(lastFilter.getValue(), (String) item.get("lastName")))
 					return true;
 				if (filterOut(nicknameFilter.getValue(), (String) item.get("nickname")))
 					return true;
 				if (filterOut(affiliationFilter.getValue(), (String) item.get("affiliation")))
 					return true;
 
 				return false;
 			}
 
 			private boolean filterOut(String value, String filterBy) {
 				String text = value == null || "".equals(value) ? null : value.toLowerCase();
 				return text != null && !filterBy.toLowerCase().startsWith(value.toLowerCase());
 			}
 		});
 
 		setLayout(new BorderLayout());
 	}
 
 	private void addUser(String username) {
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.get(UriBase.getInstance().getUserBase() + "/users/" + username, new GenericCallback<String>() {
 
 			@Override
 			public void onSuccess(String result) {
 				addUsersToLoader(ndoc);
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Info.display("Error",
 						"Failed to load new user.  If you want to view the new user, please reopen window.");
 
 			}
 		});
 	}
 
 	private void draw() {
 		removeAll();
 
 		final ToolBar bar = new ToolBar();
 
 		if (AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.USE_FEATURE,
 				AuthorizableFeature.USER_MANAGEMENT_FEATURE)) {
 			Button item = new Button("Add Profile and Create Password", new SelectionListener<ButtonEvent>() {
 				@Override
 				public void componentSelected(ButtonEvent ce) {
 					AddUserWindow win = new AddUserWindow(true, new GenericCallback<String>() {
 
 						@Override
 						public void onSuccess(String result) {
 							addUser(result);
 						}
 
 						@Override
 						public void onFailure(Throwable caught) {
 						}
 					});
 					win.show();
 					win.setSize(410, 325);
 					win.center();
 				}
 			});
 			item.setIconStyle("icon-user-suit");
 			bar.add(item);
 
 			bar.add(new SeparatorToolItem());
 			item = new Button("Add Profile", new SelectionListener<ButtonEvent>() {
 				@Override
 				public void componentSelected(ButtonEvent ce) {
 					AddUserWindow win = new AddUserWindow(false, new GenericCallback<String>() {
 
 						@Override
 						public void onSuccess(String result) {
 							addUser(result);
 						}
 
 						@Override
 						public void onFailure(Throwable caught) {
 						}
 					});
 					win.show();
 					win.setSize(410, 325);
 					win.center();
 				}
 			});
 			item.setIconStyle("icon-user-green");
 			bar.add(item);
 
 			bar.add(new SeparatorToolItem());
 			if (AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.USE_FEATURE,
 					AuthorizableFeature.DELETE_USERS_FEATURE)) {
 				item = new Button("Delete User", new SelectionListener<ButtonEvent>() {
 					@Override
 					public void componentSelected(ButtonEvent ce) {
 						if (userGrid.getSelectionModel().getSelectedItem() != null) {
 							final UserModelData selected = userGrid.getSelectionModel().getSelectedItem();
 
 							WindowUtils.confirmAlert("Delete User", "Are you sure you want to delete the user "
 									+ selected.get("username") + "? This SHOULD NOT be performed on a user that "
 									+ "is an assessor or contributor on an assessment, as that information "
 									+ "will be irretrievably lost.", new Listener<MessageBoxEvent>() {
 								public void handleEvent(MessageBoxEvent be) {
 									if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
 										NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 										ndoc.post(UriBase.getInstance().getSISBase() + "/authn/authn/remove",
 												"<root><u>" + selected.get("username") + "</u></root>",
 												new GenericCallback<String>() {
 
 													public void onFailure(Throwable caught) {
 														String message;
 														if (caught.getMessage().equals("412"))
 															message = "Sorry - you cannot delete yourself.";
 														else if (caught.getMessage().equals("500"))
 															message = "Error deleting this user. Server failure - "
 																	+ "please report this to an SIS administrator, "
 																	+ "along with the user you were attempting to delete.";
 														else
 															message = "Error deleting this user. Please check your connection and try again.";
 
 														WindowUtils.errorAlert("Delete failed!", message);
 													}
 
 													public void onSuccess(String result) {
 
 														// Removing zendesk account
 														NativeDocument zdoc = SISClientBase.getHttpBasicNativeDocument();
 														String xml = "<root><user email=\"" + selected.get("username")
 																+ "\"/></root>";
 														zdoc.post(UriBase.getInstance().getZendeskBase()
 																+ "/zendesk/remove/", xml,
 																new GenericCallback<String>() {
 
 																	public void onSuccess(String result) {}
 
 																	public void onFailure(Throwable caught) {
 																		Info.display("Error", "Failed to delete zen desk account associated with user " + selected.get("username"));
 																	}
 																});
 
 														Info.display("Success", "User {0} removed.", (String) selected
 																.get("username"));
 														loader.getFullList().remove(selected);
 														loader.getPagingLoader().load();
 														
 													}
 
 												});
 									}
 								};
 							});
 						} else
 							WindowUtils.errorAlert("Please select a user to delete.");
 					}
 				});
 				item.setIconStyle("icon-user-delete");
 				bar.add(item);
 			}
 
 			bar.add(new SeparatorToolItem());
 			item = new Button("Reset Password", new SelectionListener<ButtonEvent>() {
 				@Override
 				public void componentSelected(ButtonEvent ce) {
 					if (userGrid.getSelectionModel().getSelectedItem() != null) {
 						final String username = userGrid.getSelectionModel().getSelectedItem().get("username");
 						WindowUtils.confirmAlert("Reset Password", "Are you sure you want to reset " + username
 								+ "'s password? A new password will be supplied via e-mail.",
 								new Listener<MessageBoxEvent>() {
 
 									public void handleEvent(MessageBoxEvent be) {
 
 										if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
 											NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 											ndoc.post(UriBase.getInstance().getSISBase() + "/authn/reset", "<root><u>"
 													+ username + "</u></root>", new GenericCallback<String>() {
 												public void onSuccess(String result) {
 													Info.display("Reset Success!",
 															"A new password for {0} has been sent.", username);
 												}
 
 												public void onFailure(Throwable caught) {
 													WindowUtils
 															.errorAlert(
 																	"Reset failed!",
 																	"Resetting this "
 																			+ "user's password failed. Please check your Internet connection and try again.");
 												}
 											});
 										}
 									};
 								});
 					} else
 						WindowUtils.errorAlert("Please select a user.");
 				}
 			});
 			item.setIconStyle("icon-user-go");
 			bar.add(item);
 
 			bar.add(new SeparatorToolItem());
 			item = new Button("Show Filter(s)", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					filterPopup = new Popup();
 					filterPopup.setStyleName("navigator");
 					filterPopup.setShim(true);
 					// filterPopup.setShadow(true);
 					filterPopup.add(filterPanel);
 					filterPopup.setLayout(new FitLayout());
 					filterPopup.setSize(380, 230);
 					filterPopup.show();
 					filterPopup.setPagePosition(ce.getButton().getAbsoluteLeft() - 160 > 0 ? ce.getButton()
 							.getAbsoluteLeft() - 160 : 0, ce.getButton().getAbsoluteTop() + 30);
 				}
 			});
 			item.setIconStyle("icon-user-comment");
 			bar.add(item);
 		}
 
 		LayoutContainer c = new LayoutContainer(new FitLayout());
 		c.add(pagingBar);
 
 		filterPanel = new FormPanel();
 		filterPanel.setHeaderVisible(false);
 		filterPanel.add(usernameFilter);
 		filterPanel.add(firstFilter);
 		filterPanel.add(lastFilter);
 		filterPanel.add(nicknameFilter);
 		filterPanel.add(affiliationFilter);
 		filterPanel.add(activeAccountFilter);
 		final Button applyFilters = new Button("Apply Filters", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				filterPopup.hide();
 				loader.applyFilter("");
 				pagingBar.setActivePage(1);
 				loader.getPagingLoader().load();
 			}
 		});
 		KeyListener enter = new KeyListener() {
 			public void componentKeyPress(ComponentEvent event) {
 				if (event.getKeyCode() == KeyCodes.KEY_ENTER)
 					applyFilters.fireEvent(Events.Select);
 			}
 		};
 		usernameFilter.addKeyListener(enter);
 		firstFilter.addKeyListener(enter);
 		lastFilter.addKeyListener(enter);
 		nicknameFilter.addKeyListener(enter);
 		affiliationFilter.addKeyListener(enter);
 
 		filterPanel.getButtonBar().add(applyFilters);
 		filterPanel.getButtonBar().add(new Button("Clear Filters", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				filterPopup.hide();
 				usernameFilter.setValue("");
 				firstFilter.setValue("");
 				lastFilter.setValue("");
 				nicknameFilter.setValue("");
 				affiliationFilter.setValue("");
 				activeAccountFilter.setValue(activeAccountFilter.findModel(""));
 				loader.applyFilter("");
 				pagingBar.setActivePage(1);
 				loader.getPagingLoader().load();
 			}
 		}));
 		// filterPanel.setAlignment(HorizontalAlignment.LEFT);
 
 		add(bar, new BorderLayoutData(LayoutRegion.NORTH, 25));
 		add(c, new BorderLayoutData(LayoutRegion.SOUTH, 25));
 		add(center, new BorderLayoutData(LayoutRegion.CENTER));
 
 		populateStore();
 
 		layout();
 	}
 
 	private ColumnModel getColumnModel() {
 		final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
 
 		// ID field, not editable
 		final ColumnConfig id = new ColumnConfig();
 		id.setId("id");
 		id.setHeader("ID");
 		id.setWidth(30);
 		configs.add(id);
 
 		// User name, not editable
 		final ColumnConfig name = new ColumnConfig();
 		name.setId("username");
 		name.setHeader("User Name");
 		name.setWidth(100);
 		configs.add(name);
 
 		// First name, editable
 		final ColumnConfig first = new ColumnConfig();
 		first.setId("firstName");
 		first.setWidth(100);
 		first.setHeader("First Name");
 		{
 			final TextField<String> text = new TextField<String>();
 			text.setAllowBlank(false);
 			text.setAutoValidate(true);
 			text.setMaxLength(255);
 			first.setEditor(new CellEditor(text));
 		}
 		configs.add(first);
 
 		final ColumnConfig last = new ColumnConfig();
 		last.setId("lastName");
 		last.setWidth(100);
 		last.setHeader("Last Name");
 		{
 			final TextField<String> text = new TextField<String>();
 			text.setAllowBlank(false);
 			text.setAutoValidate(true);
 			text.setMaxLength(255);
 			last.setEditor(new CellEditor(text));
 		}
 		configs.add(last);
 		
 		final ColumnConfig nickname = new ColumnConfig();
 		nickname.setId("nickname");
 		nickname.setWidth(100);
 		nickname.setHeader("Nickname");
 		{
 			final TextField<String> text = new TextField<String>();
			text.setAllowBlank(false);
 			text.setAutoValidate(true);
 			text.setMaxLength(255);
 			nickname.setEditor(new CellEditor(text));
 		}
 		configs.add(nickname);
 
 		final ColumnConfig initials = new ColumnConfig();
 		initials.setId("initials");
 		initials.setWidth(60);
 		initials.setHeader("Initials");
 		{
 			final TextField<String> text = new TextField<String>();
 			text.setAllowBlank(false);
 			text.setAutoValidate(true);
 			text.setMaxLength(255);
 			initials.setEditor(new CellEditor(text));
 			initials.getEditor().setToolTip(
 					"If supplied, initials will be used in publications."
 							+ "Otherwise, the first character of your first name will be used.");
 		}
 		configs.add(initials);
 
 		final ColumnConfig affiliation = new ColumnConfig();
 		affiliation.setId("affiliation");
 		affiliation.setWidth(120);
 		affiliation.setHeader("Affiliation");
 		{
 			final TextField<String> text = new TextField<String>();
 			text.setAllowBlank(true);
 			text.setAutoValidate(false);
 			text.setMaxLength(2000);
 			affiliation.setEditor(new CellEditor(text));
 		}
 		configs.add(affiliation);
 
 		final ColumnConfig quickGroup = new ColumnConfig();
 		quickGroup.setId("quickgroup");
 		quickGroup.setWidth(120);
 		quickGroup.setHeader("Permission Groups");
 		CellEditor cellEditor = new CellEditor(permissionGroups) {
 			@Override
 			protected void onBlur(FieldEvent fe) {
 				fe.cancelBubble();
 			}
 		};
 		quickGroup.setEditor(cellEditor);
 		quickGroup.setHidden(!AuthorizationCache.impl.hasRight(SISClientBase.currentUser,
 				AuthorizableObject.USE_FEATURE, AuthorizableFeature.PERMISSION_MANAGEMENT_FEATURE));
 		configs.add(quickGroup);
 
 		final ColumnConfig sis = new ColumnConfig();
 
 		final SimpleComboBox<String> sisUser = new SimpleComboBox<String>();
 		sisUser.setForceSelection(true);
 		sisUser.setTriggerAction(TriggerAction.ALL);
 		sisUser.add("true");
 		sisUser.add("false");
 		CellEditor editor = new CellEditor(sisUser) {
 
 			@Override
 			public Object preProcessValue(Object value) {
 				if (value == null)
 					return value;
 				return sisUser.findModel(value.toString());
 			}
 
 			@Override
 			public Object postProcessValue(Object value) {
 				if (value == null)
 					return value;
 				return ((ModelData) value).get("value");
 			}
 
 		};
 		sis.setEditor(editor);
 		sis.setId("sisUser");
 		sis.setWidth(120);
 		sis.setHeader("SIS Account");
 		sis.setHidden(!AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.USE_FEATURE,
 				AuthorizableFeature.USER_MANAGEMENT_FEATURE));
 		configs.add(sis);
 
 		final ColumnConfig rapidlist = new ColumnConfig();
 		rapidlist.setId("rapidListUser");
 		rapidlist.setWidth(120);
 		rapidlist.setHeader("RapidList Account");
 		final SimpleComboBox<String> rapidListUser = new SimpleComboBox<String>();
 		rapidListUser.setForceSelection(true);
 		rapidListUser.setTriggerAction(TriggerAction.ALL);
 		rapidListUser.add("true");
 		rapidListUser.add("false");
 
 		editor = new CellEditor(rapidListUser) {
 
 			@Override
 			public Object preProcessValue(Object value) {
 				if (value == null)
 					return value;
 				return rapidListUser.findModel(value.toString());
 			}
 
 			@Override
 			public Object postProcessValue(Object value) {
 				if (value == null)
 					return value;
 				return ((ModelData) value).get("value");
 			}
 
 		};
 		rapidlist.setEditor(editor);
 
 		rapidlist.setHidden(!AuthorizationCache.impl.hasRight(SISClientBase.currentUser,
 				AuthorizableObject.USE_FEATURE, AuthorizableFeature.USER_MANAGEMENT_FEATURE));
 		configs.add(rapidlist);
 
 		return new ColumnModel(configs);
 	}
 
 	private void finalizeStorePopulation() {
 
 		loader.getPagingLoader().load(0, 50);
 		pagingBar.setActivePage(1);
 		store.filter("username");
 
 		final GridSelectionModel<UserModelData> sm = new GridSelectionModel<UserModelData>();
 		sm.setSelectionMode(SelectionMode.SINGLE);
 
 		userGrid = new EditorGrid<UserModelData>(store, getColumnModel());
 		userGrid.setSelectionModel(sm);
 		userGrid.setBorders(false);
 		userGrid.addListener(Events.AfterEdit, new Listener<GridEvent<UserModelData>>() {
 			public void handleEvent(final GridEvent<UserModelData> be) {
 				final ModelData model = be.getGrid().getStore().getAt(be.getRowIndex());
 				if (be.getValue() == null) {
 					store.rejectChanges();
 					return;
 				}
 
 				final String value = ((String) be.getValue()).trim();
 				final String col = be.getGrid().getColumnModel().getColumnId(be.getColIndex());
 				final String body = "<root><field name=\"" + col + "\">" + value + "</field></root>";
 				final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
 				document.post(UriBase.getInstance().getUserBase() + "/users/" + model.get("username"), body,
 						new GenericCallback<String>() {
 							public void onFailure(Throwable caught) {
 								Info.display("Error", "Could not save changes, please try again later.");
 								model.set(col, be.getStartValue());
 							}
 
 							public void onSuccess(String result) {
 								Info.display("Success", "Changes saved.");
 								be.getGrid().getStore().commitChanges();
 								model.set(col, be.getValue());
 							}
 						});
 
 			}
 		});
 
 		center.add(userGrid);
 		center.layout();
 	}
 
 	private void populateStore() {
 		center.removeAll();
 
 		final LayoutContainer container = new LayoutContainer();
 		container.setLayout(new BorderLayout());
 		loader.getFullList().clear();
 
 		if (!AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.USE_FEATURE,
 				AuthorizableFeature.USER_MANAGEMENT_FEATURE)) {
 			loader.add(new UserModelData(SISClientBase.currentUser));
 			finalizeStorePopulation();
 
 		} else {
 			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 			ndoc.get(UriBase.getInstance().getUserBase() + "/users", new GenericCallback<String>() {
 
 				@Override
 				public void onSuccess(String result) {
 					addUsersToLoader(ndoc);
 					finalizeStorePopulation();
 
 				}
 
 				@Override
 				public void onFailure(Throwable caught) {
 					WindowUtils.errorAlert("Unable to load users");
 
 				}
 			});
 
 		}
 
 	}
 
 	protected void addUsersToLoader(NativeDocument ndoc) {
 		NativeElement docElement = ndoc.getDocumentElement();
 		NativeNodeList users = docElement.getElementsByTagName(User.ROOT_TAG);
 		for (int i = 0; i < users.getLength(); i++) {
 			ClientUser user = ClientUser.fromXML(users.elementAt(i));
 
 			loader.add(new UserModelData(user));
 		}
 
 		ArrayUtils.quicksort(loader.getFullList(), new Comparator<UserModelData>() {
 			public int compare(UserModelData o1, UserModelData o2) {
 				return ((String) o1.get("username")).compareTo((String) o2.get("username"));
 			}
 		});
 		loader.getPagingLoader().load();
 
 	}
 
 	public void refresh() {
 		ArrayList<String> groups = new ArrayList<String>(AuthorizationCache.impl.getGroups().keySet());
 		ArrayUtils.quicksort(groups, new PortableAlphanumericComparator());
 		permissionGroups.setOptions(groups);
 		draw();
 
 	}
 
 }
