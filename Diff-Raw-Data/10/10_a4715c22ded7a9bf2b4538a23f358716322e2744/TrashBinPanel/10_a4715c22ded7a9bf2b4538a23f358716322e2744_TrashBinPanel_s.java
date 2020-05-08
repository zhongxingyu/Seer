 package org.iucn.sis.client.panels.header;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.client.panels.ClientUIContainer;
 import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Taxon;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.DataListEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.store.StoreEvent;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.DataList;
 import com.extjs.gxt.ui.client.widget.DataListItem;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.WindowManager;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.user.client.ui.HTML;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.extjs.client.WindowUtils.MessageBoxListener;
 
 public class TrashBinPanel extends LayoutContainer {
 
 	private LayoutContainer centerPanel;
 	private LayoutContainer westPanel;
 
 	private BorderLayoutData centerData;
 	private BorderLayoutData westData;
 
 	private ContentPanel status;
 	private Grid<TrashedObject> trashTable;
 	private ListStore<TrashedObject> store;
 	private Map<String, List<TrashedObject>> trashedObjects;
 	private DataList folders;
 
 	private int total = 0;
 	private HashMap<String, Integer> folderCount;
 
 	public TrashBinPanel() {
 		setLayout(new BorderLayout());
 		setLayoutOnChange(true);
 		folderCount = new HashMap<String, Integer>();
 		trashedObjects = new HashMap<String, List<TrashedObject>>();
 		store = new ListStore<TrashedObject>();
 		build();
 	}
 
 	public void build() {
 
 		centerPanel = new LayoutContainer();
 		westPanel = new LayoutContainer();
 		centerData = new BorderLayoutData(LayoutRegion.CENTER, .75f);
 		westData = new BorderLayoutData(LayoutRegion.WEST, .25f);
 
 		folders = new DataList();
 		folders.addStyleName("gwt-background");
 		folders.setBorders(true);
 
 		status = new ContentPanel();
 		status.setLayoutOnChange(true);
 
 		buildWestPanel();
 		buildCenterPanel();
 
 		fillTrash();
 
 		add(centerPanel, centerData);
 		add(westPanel, westData);
 	}
 
 	private void buildCenterPanel() {
 		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
 
 		ColumnConfig column = new ColumnConfig();
 		column.setId("date");
 		column.setHeader("Date Removed");
 		column.setWidth(100);
 		configs.add(column);
 
 		column = new ColumnConfig();
 		column.setId("type");
 		column.setHeader("Type");
 		column.setWidth(75);
 		configs.add(column);
 
 		column = new ColumnConfig();
 		column.setId("id");
 		column.setHeader("ID");
 		column.setWidth(75);
 		configs.add(column);
 
 		column = new ColumnConfig();
 		column.setId("taxon");
 		column.setHeader("Taxon");
 		column.setWidth(200);
 		column.setAlignment(HorizontalAlignment.LEFT);
 		configs.add(column);
 
 		column = new ColumnConfig();
 		column.setId("status");
 		column.setHeader("Status");
 		column.setWidth(50);
 		configs.add(column);
 
 		column = new ColumnConfig();
 		column.setId("user");
 		column.setHeader("Removed By");
 		column.setWidth(150);
 		configs.add(column);
 
 		trashTable = new Grid<TrashedObject>(store, new ColumnModel(configs));
 		trashTable.setWidth(585);
 		trashTable.setHeight(475);
 
 		centerPanel.add(buildToolBar());
 		centerPanel.add(trashTable);
 	}
 
 	private ToolBar buildToolBar() {
 		ToolBar bar = new ToolBar();
 
 		Button tItem = new Button();
 		tItem.setText("Restore");
 		tItem.addListener(Events.Select, new Listener() {
 			public void handleEvent(BaseEvent be) {
 				final TrashedObject trashed = ((TrashedObject) trashTable.getSelectionModel().getSelectedItem());
 				String id = trashed.getID();
 				String type = trashed.getType();

 				// **************************
 				// check is assessments exist to restore with taxa
 				boolean recurse = false;
 				if (type.equalsIgnoreCase("TAXON")) {
 					// Iterator<TableItem> iter = trashTable.iterator();
 					for (TrashedObject obj : store.getModels()) {
 						if (obj.getNodeID().equals(trashed.getNodeID()) && obj.getType().equalsIgnoreCase("ASSESSMENT")) {
 							recurse = true;
 						}
 					}
 
 					if (recurse) {
 						WindowUtils.confirmAlert("Restore Assessments",
 								"This taxa has related assessments in the trash bin. Do you wish to restore these?",
 								new MessageBoxListener() {
 									@Override
 									public void onNo() {
 										restore(false, trashed);
 
 									}
 
 									@Override
 									public void onYes() {
 										restore(true, trashed);
 
 									}
 								});
 					}
 				} else {
 					restore(false, trashed);
 				}
 
 				// ***************************
 
 			}
 		});
 		tItem.setIconStyle("icon-undo");
 		bar.add(tItem);
 
 		tItem = new Button();
 		tItem.setText("Delete");
 		tItem.setIconStyle("icon-remove");
 		tItem.addListener(Events.Select, new Listener() {
 			public void handleEvent(BaseEvent be) {
 				final TrashedObject trashed = ((TrashedObject) trashTable.getSelectionModel().getSelectedItem());
 				trashed.delete(new GenericCallback<String>() {
 					public void onFailure(Throwable arg0) {
 					};
 
 					public void onSuccess(String arg0) {
 						trashedObjects.get(trashed.getIdentifier()).remove(trashed);
 						store.remove(trashed);
 						refresh();
 					}
 				});
 			}
 		});
 		bar.add(tItem);
 
 		tItem = new Button();
 		tItem.setText("Empty Trash");
 		tItem.setIconStyle("icon-bomb");
 		tItem.addListener(Events.Select, new Listener() {
 			public void handleEvent(BaseEvent be) {
 				final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
 				doc.post(UriBase.getInstance().getSISBase() + "/trash/deleteall", "", new GenericCallback<String>() {
 					public void onFailure(Throwable arg0) {
 
 					}
 
 					public void onSuccess(String arg0) {
 						trashedObjects.clear();
 						store.removeAll();
 						refresh();
 
 					}
 				});
 			}
 		});
 		bar.add(tItem);
 
 		return bar;
 	}
 
 	private void buildWestPanel() {
 		DataListItem all = new DataListItem("All");
 		all.setIconStyle("tree-folder");
 		all.setItemId("all");
 		folders.add(all);
 
 		DataListItem published = new DataListItem("Published Assessments");
 		published.setIconStyle("tree-folder");
 		published.setItemId("assessment:published");
 		folders.add(published);
 
 		DataListItem draft = new DataListItem("Draft Assessments");
 		draft.setIconStyle("tree-folder");
 		draft.setItemId("assessment:draft");
 		folders.add(draft);
 
 		DataListItem taxon = new DataListItem("Taxa");
 		taxon.setIconStyle("tree-folder");
 		taxon.setItemId("taxon:");
 		folders.add(taxon);
 
 		folders.addListener(Events.SelectionChange, new Listener() {
 			public void handleEvent(BaseEvent be) {
 				refreshStore();
 			}
 		});
 		folders.setHeight(300);
 
 		westPanel.add(folders);
 		refreshStatus();
 		westPanel.add(status);
 		// tree.getItem(i).setIconStyle("tree-folder-open");
 	}
 
 	private void fillTrash() {
 		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
 
 		doc.get(UriBase.getInstance().getSISBase() + "/trash/list", new GenericCallback<String>() {
 			public void onFailure(Throwable arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 			public void onSuccess(String arg0) {
 				// TODO Auto-generated method stub
 
 				trashedObjects.clear();
 				store.removeAll();
 
 				NativeNodeList list = doc.getDocumentElement().getElementsByTagName("data");
 				total = list.getLength();
 				folderCount.put("assessment", 0);
 				folderCount.put("taxon", 0);
 
 				for (int i = 0; i < list.getLength(); i++) {
 					TrashedObject ti = new TrashedObject((NativeElement) list.item(i));
 					folderCount.put(ti.getType(), folderCount.get(ti.getType()) + 1);
 					Debug.println("looking for identifier " + ti.getIdentifier());
 					if (!trashedObjects.containsKey(ti.getIdentifier()))
 						trashedObjects.put(ti.getIdentifier(), new ArrayList<TrashedObject>());
 					trashedObjects.get(ti.getIdentifier()).add(ti);
 
 				}
 
 				if (!trashedObjects.containsKey("assessment:published")) {
 					trashedObjects.put("assessment:published", new ArrayList<TrashedObject>());
 				}
 				if (!trashedObjects.containsKey("assessment:draft")) {
 					trashedObjects.put("assessment:draft", new ArrayList<TrashedObject>());
 				}
 				if (!trashedObjects.containsKey("taxon:")) {
 					trashedObjects.put("taxon:", new ArrayList<TrashedObject>());
 				}
 
 				refreshStore();
 				refreshStatus();
 
 			}
 		});
 	}
 
 	protected void refreshStore() {
 		store.removeAll();
 		if (folders.getSelectedItem() != null) {
 			Debug.println("looking for " + folders.getSelectedItem().getItemId());
 			if (trashedObjects.containsKey(folders.getSelectedItem().getItemId())) {
 				Debug.println("adding obj " + trashedObjects.get(folders.getSelectedItem().getItemId()));
 				store.add(trashedObjects.get(folders.getSelectedItem().getItemId()));
 			} else {
 				Debug.println("adding all");
 				for (Entry<String, List<TrashedObject>> entry : trashedObjects.entrySet())
 					store.add(entry.getValue());
 			}
 		}
 
 	}
 
 	@Override
 	protected void onAttach() {
 		super.onAttach();
 		folders.getSelectionModel().select(0, false);
 	}
 
 	public void refresh() {
 		fillTrash();
 		refreshStatus();
 		// build();
 
 		// layout();
 	}
 
 	private void refreshStatus() {
 		status.removeAll();
 		status.setHeading("Statistics");
 		status.setHeight(200);
 
 		status.add(new HTML("Total Items: " + total));
 		status.add(new HTML("Total Assesments: " + folderCount.get("assessment")));
 		status.add(new HTML("Total Taxa: " + folderCount.get("taxon")));
 		status.add(new HTML("Current View:" + store.getCount()));
 	}
 
 	private void restore(final boolean recurse, final TrashedObject trashed) {
 		trashed.restore(recurse, new GenericCallback<String>() {
 			public void onFailure(Throwable arg0) {
 				WindowUtils.errorAlert("Unable to restore", "Unable to restore this object."
 						+ (trashed.getType().startsWith("draft") ? " Ensure a draft assessment with "
 								+ "the same regions does not already exist for this taxon." : ""));
 			}
 
 			public void onSuccess(String arg0) {
 
 				int id = 0;
 				if (TaxonomyCache.impl.getCurrentTaxon() != null)
 					id = TaxonomyCache.impl.getCurrentTaxon().getId();
 				TaxonomyCache.impl.clear();
 				AssessmentCache.impl.clear();
 				if (id != 0
 						&& ClientUIContainer.bodyContainer.getSelectedItem().equals(
 								ClientUIContainer.bodyContainer.tabManager.taxonHomePage)) {
 					ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel
 							.update(new Integer(id));
 				}
 				fillTrash();
 
 //				store.remove(trashed);
 //				trashedObjects.get(trashed.getIdentifier()).remove(trashed);
 //
 //				if (trashed.getType().equalsIgnoreCase("taxon")) {
 //					if (recurse) {
 //						for (Entry<String, List<TrashedObject>> entry : trashedObjects.entrySet()) {
 //							if (!entry.getKey().equalsIgnoreCase(trashed.getIdentifier())) {
 //								List<TrashedObject> objs = new ArrayList<TrashedObject>();
 //								for (TrashedObject obj : entry.getValue()) {
 //									if (obj.getNodeID().equalsIgnoreCase(trashed.getNodeID())) {
 //										store.remove(obj);
 //										objs.add(obj);
 //									}
 //								}
 //								entry.getValue().removeAll(objs);
 //							}
 //						}
 //					}
 //				
 //
 			}
 		});
 
 	}
 }
