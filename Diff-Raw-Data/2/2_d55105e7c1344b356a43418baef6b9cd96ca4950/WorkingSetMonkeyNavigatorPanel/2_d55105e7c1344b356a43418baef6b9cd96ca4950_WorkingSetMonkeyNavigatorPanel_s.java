 package org.iucn.sis.client.panels;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import org.iucn.sis.client.api.caches.MarkedCache;
 import org.iucn.sis.client.api.caches.WorkingSetCache;
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.container.StateChangeEvent;
 import org.iucn.sis.client.api.container.StateManager;
 import org.iucn.sis.client.api.utils.FormattedDate;
 import org.iucn.sis.client.panels.MonkeyNavigator.NavigationChangeEvent;
 import org.iucn.sis.client.panels.workingsets.WorkingSetNewWSPanel;
 import org.iucn.sis.client.panels.workingsets.WorkingSetSubscriber;
 import org.iucn.sis.client.tabs.WorkingSetPage;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.WorkingSet;
 
 import com.extjs.gxt.ui.client.data.ModelKeyProvider;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.IconButtonEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MenuEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.GroupingStore;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.IconButton;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.extjs.gxt.ui.client.widget.menu.MenuItem;
 import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.events.SimpleListener;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 import com.solertium.util.portable.PortableAlphanumericComparator;
 
 public class WorkingSetMonkeyNavigatorPanel extends GridNonPagingMonkeyNavigatorPanel<WorkingSet> {
 	
 	private WorkingSet curNavWorkingSet;
 	
 	public WorkingSetMonkeyNavigatorPanel() {
 		super();
 		setHeading("Working Sets");
 	}
 	
 	protected ListStore<NavigationModelData<WorkingSet>> getStoreInstance() {
 		GroupingStore<NavigationModelData<WorkingSet>> store = 
 			new GroupingStore<NavigationModelData<WorkingSet>>(getLoader());
 		store.groupBy("ownerid");
 		
 		return store;
 	}
 	
 	protected void onSelectionChanged(NavigationModelData<WorkingSet> model) {
 		MonkeyNavigator.NavigationChangeEvent<WorkingSet> e = 
 			new NavigationChangeEvent<WorkingSet>(model.getModel());
 		
 		fireEvent(Events.SelectionChange, e);
 	}
 	
 	@Override
 	protected void mark(NavigationModelData<WorkingSet> model, String color) {
 		if (model == null || model.getModel() == null)
 			return;
 		
 		final Integer workingSetID = getSelected().getId();
 		
 		MarkedCache.impl.markWorkingSet(workingSetID, color);
 		
 		refreshView();
 	}
 	
 	@Override
 	protected String getEmptyText() {
 		return "No Working Sets";
 	}
 	
 	protected ColumnModel getColumnModel() {
 		final List<ColumnConfig> list = new ArrayList<ColumnConfig>();
 
 		ColumnConfig display = new ColumnConfig("name", "Name", 100);
 		display.setRenderer(new GridCellRenderer<NavigationModelData<WorkingSet>>() {
 			@Override
 			public Object render(NavigationModelData<WorkingSet> model, String property,
 					ColumnData config, int rowIndex, int colIndex,
 					ListStore<NavigationModelData<WorkingSet>> store, Grid<NavigationModelData<WorkingSet>> grid) {
 				Boolean header = model.get("header");
 				if (header == null)
 					header = Boolean.FALSE;
 				WorkingSet ws = model.getModel();
 				String style;
 				String value;
 				if (ws == null) {
 					style = header ? "monkey_navigation_section_header" : MarkedCache.NONE;
 					value = model.get(property);
 				}
 				else {
 					Boolean mine = model.get("mine");
 					if (mine == null)
 						mine = Boolean.FALSE;
 					style = MarkedCache.impl.getWorkingSetStyle(ws.getId());
 					value = ws.getName() + (mine ? "" : " via " + ws.getCreator().getDisplayableName());
 				}
 				return "<div class=\"" + style + "\">" + value + "</div>";
 			}
 		});
 		
 		list.add(display);
 		
 		return new ColumnModel(list);
 	}
 	
 	public void refresh(final WorkingSet curNavWorkingSet) {
 		this.curNavWorkingSet = curNavWorkingSet;
 		
 		refresh(new DrawsLazily.DoneDrawingCallback() {
 			public void isDrawn() {
 				DeferredCommand.addCommand(new Command() {
 					public void execute() {
 						setSelection(curNavWorkingSet);
 					}
 				});
 			}
 		});
 	}
 	
 	@Override
 	public void setSelection(WorkingSet navigationModel) {
 		final NavigationModelData<WorkingSet> selection;
 		if (curNavWorkingSet == null)
 			selection = getProxy().getStore().findModel("-1");
 		else
 			selection = getProxy().getStore().findModel("" + curNavWorkingSet.getId());
 		
 		Debug.println("Selected working set from nav is {0}, found {1}", curNavWorkingSet, selection);
 		if (selection != null) {
 			((NavigationGridSelectionModel<WorkingSet>)grid.getSelectionModel()).
 				highlight(selection);
 			
 			DeferredCommand.addPause();
 			DeferredCommand.addCommand(new Command() {
 				public void execute() {
 					grid.getView().focusRow(grid.getStore().indexOf(selection));
 				}
 			});
 		}
 	}
 	
 	@Override
 	protected void onLoad() {
 		super.onLoad();
 		
 		setSelection(curNavWorkingSet);
 	}
 	
 	@Override
 	protected void getStore(final GenericCallback<ListStore<NavigationModelData<WorkingSet>>> callback) {
 		final ListStore<NavigationModelData<WorkingSet>> store = new ListStore<NavigationModelData<WorkingSet>>();
 		store.setKeyProvider(new ModelKeyProvider<NavigationModelData<WorkingSet>>() {
 			public String getKey(NavigationModelData<WorkingSet> model) {
 				if (model.getModel() == null)
 					if (model.get("none") != null)
 						return "-1";
 					else
 						return null;
 				else
 					return Integer.toString(model.getModel().getId());
 			}
 		});
 		
 		final int myOwnerID = SISClientBase.currentUser.getId();
 		
 		NavigationModelData<WorkingSet> noneModel = new NavigationModelData<WorkingSet>(null);
 		noneModel.set("name", "None");
 		noneModel.set("none", Boolean.TRUE);
 		noneModel.set("header", Boolean.FALSE);
 		noneModel.set("mine", Boolean.FALSE);
 		
 		store.add(noneModel);
 		
 		MonkeyNavigator.getSortedWorkingSets(new ComplexListener<List<WorkingSet>>() {
 			public void handleEvent(final List<WorkingSet> list) {
 				Integer currentOwner = null;
 				int groupCount = 0;
 				boolean mine = true;
 				
 				for (WorkingSet ws : list) {
 					Integer owner = ws.getCreator().getId();
 					if (!owner.equals(currentOwner)) {
 						NavigationModelData<WorkingSet> header = new NavigationModelData<WorkingSet>(null);
						header.set("name", mine ? "My Working Sets" : "Subscribed Working Sets");
 						header.set("header", Boolean.TRUE);
 						
 						if (mine)
 							store.add(header);
 						
 						currentOwner = owner;
 						if (mine)
 							groupCount = 0;
 						
 						mine = owner.intValue() == myOwnerID;
 					}
 					
 					NavigationModelData<WorkingSet> model = new NavigationModelData<WorkingSet>(ws);
 					model.set("name", ws.getName());
 					model.set("header", Boolean.FALSE);
 					model.set("mine", mine);
 					
 					store.add(model);
 					
 					groupCount++;
 				}
 				
 				callback.onSuccess(store);
 			}
 		});
 	}
 	
 	@Override
 	protected void setupToolbar() {
 		final IconButton goToSet = createIconButton("icon-go-jump", "Open Working Set", new SelectionListener<IconButtonEvent>() {
 			public void componentSelected(IconButtonEvent ce) {
 				open(grid.getSelectionModel().getSelectedItem());
 			}
 		});
 		
 		addTool(createIconButton("icon-add", "Add Working Set", new SelectionListener<IconButtonEvent>() {
 			public void componentSelected(IconButtonEvent ce) {
 				final MenuItem newWS = new MenuItem("Create New Working Set");
 				newWS.addSelectionListener(new SelectionListener<MenuEvent>() {
 					public void componentSelected(MenuEvent ce) {
 						final Window window = new Window();
 						window.setSize(700, 700);
 						window.setHeading("Add New Working Set");
 						window.setLayout(new FillLayout());
 						
 						WorkingSetNewWSPanel panel = new WorkingSetNewWSPanel();
 						panel.setAfterSaveListener(new ComplexListener<WorkingSet>() {
 							public void handleEvent(WorkingSet eventData) {
 								window.hide();
 								
 								StateChangeEvent event = new StateChangeEvent(eventData, null, null, null);
 								event.setCanceled(false);
 								event.setUrl(WorkingSetPage.URL_TAXA);
 								
 								StateManager.impl.setState(event);
 							}
 						});
 						panel.setCloseListener(new ComplexListener<WorkingSet>() {
 							public void handleEvent(WorkingSet eventData) {
 								window.hide();
 								StateManager.impl.setWorkingSet(eventData);
 							}
 						});
 						panel.setCancelListener(new SimpleListener() {
 							public void handleEvent() {
 								window.hide();	
 							}
 						});
 						panel.setSaveNewListener(new ComplexListener<WorkingSet>() {
 							public void handleEvent(WorkingSet eventData) {
 								window.hide();
 								
 								StateChangeEvent event = new StateChangeEvent(eventData, null, null, null);
 								event.setCanceled(false);
 								event.setUrl(WorkingSetPage.URL_EDIT);
 								
 								StateManager.impl.setState(event);
 							}
 						});
 						panel.refresh();
 						
 						window.add(panel);
 						window.show();
 					}
 				});
 				
 				final MenuItem subscribe = new MenuItem("Subscribe to Existing Working Set");
 				subscribe.addSelectionListener(new SelectionListener<MenuEvent>() {
 					public void componentSelected(MenuEvent ce) {
 						final WorkingSetSubscriber panel = new WorkingSetSubscriber();
 						
 						Window window = new Window();
 						window.setLayout(new FillLayout());
 						window.setSize(700, 700);
 						window.setHeading("Subscribe to Working Set");
 						window.addListener(Events.Show, new Listener<BaseEvent>() {
 							public void handleEvent(BaseEvent be) {
 								panel.refresh();
 							}
 						});
 						window.add(panel);
 						window.show();
 					}
 				});
 				
 				final Menu menu = new Menu();
 				menu.add(newWS);
 				menu.add(subscribe);
 				
 				menu.show(ce.getIconButton());
 			}
 		}));
 		addTool(new SeparatorToolItem());
 		addTool(createIconButton("icon-information", "Working Set Details", new SelectionListener<IconButtonEvent>() {
 			public void componentSelected(IconButtonEvent ce) {
 				final WorkingSet ws = getSelected();
 				if (ws == null) {
 					WindowUtils.errorAlert("Please select a working set.");
 					return;
 				}
 					
 				final Window s = WindowUtils.getWindow(false, true, ws.getWorkingSetName());
 				s.setLayout(new FillLayout());
 				s.setTitle(ws.getWorkingSetName());
 				s.setSize(600, 400);
 				
 				final FlexTable table = new FlexTable();
 				table.setCellSpacing(4);
 
 				// IF THERE ARE SPECIES TO GET
 				if (ws.getSpeciesIDs().size() > 0) {
 					WorkingSetCache.impl.fetchTaxaForWorkingSet(ws, new GenericCallback<List<Taxon>>() {
 						public void onFailure(Throwable caught) {
 							table.setHTML(0, 0, "<b>Manager: </b>");
 							table.setHTML(0, 1, ws.getCreator().getUsername());
 							table.setHTML(1, 0, "<b>Date: </b>");
 							table.setHTML(1, 1, FormattedDate.impl.getDate(ws.getCreatedDate()));
 							table.setHTML(2, 0, "<b>Number of Species: </b>");
 							table.setHTML(2, 1, "" + ws.getSpeciesIDs().size());
 							table.setHTML(3, 0, "<b>Description: </b>");
 							table.setHTML(3, 1, ws.getDescription());
 							s.layout();
 						};
 
 						public void onSuccess(List<Taxon> arg0) {
 							table.setHTML(0, 0, "<b>Manager: </b>");
 							table.setHTML(0, 1, ws.getCreator().getUsername());
 							table.setHTML(1, 0, "<b>Date: </b>");
 							table.setHTML(1, 1, FormattedDate.impl.getDate(ws.getCreatedDate()));
 							table.setHTML(2, 0, "<b>Number of Species: </b>");
 							table.setHTML(2, 1, "" + ws.getSpeciesIDs().size());
 
 							String species = "";
 							for (Taxon taxon : arg0) {
 								species += taxon.getFullName() + ", ";
 							}
 							if (species.length() > 0) {
 								table.setHTML(3, 0, "<b>Species: </b>");
 								table.setHTML(3, 1, species.substring(0, species.length() - 2));
 								table.setHTML(4, 0, "<b>Description: </b>");
 								table.setHTML(4, 1, ws.getDescription());
 							} else {
 								table.setHTML(3, 0, "<b>Description: </b>");
 								table.setHTML(3, 1, ws.getDescription());
 							}
 							s.add(table);
 							s.show();
 						};
 					});
 				}
 				// ELSE LOAD NO SPECIES
 				else {
 					table.setHTML(0, 0, "There are no species in the " + ws.getWorkingSetName() + " working set.");
 					s.add(table);
 					
 					s.show();
 				}
 				
 			}
 		}));
 		addTool(createIconButton("icon-image", "Download Working Set", new SelectionListener<IconButtonEvent>() {
 			public void componentSelected(IconButtonEvent ce) {
 				// TODO Auto-generated method stub
 				WindowUtils.infoAlert("This feature is not yet available.");
 			}
 		}));
 		addTool(new SeparatorToolItem());
 		addTool(goToSet);
 	}
 	
 	@Override
 	protected void open(NavigationModelData<WorkingSet> model) {
 		if (hasSelection()) {
 			navigate(getSelected(), null, null);
 		}	
 	}
 	
 	public static class WorkingSetNavigationComparator implements Comparator<WorkingSet> {
 		
 		private final PortableAlphanumericComparator comparator;
 		private final Integer userID;
 		
 		public WorkingSetNavigationComparator(Integer userID) {
 			this.userID = userID;
 			this.comparator = new PortableAlphanumericComparator();
 		}
 		
 		@Override
 		public int compare(WorkingSet o1, WorkingSet o2) {
 			if (isMine(o1) && !isMine(o2))
 				return -1;
 			
 			if (!isMine(o1) && isMine(o2))
 				return 1;
 			
 			return comparator.compare(o1.getName(), o2.getName());
 		}
 		
 		private boolean isMine(WorkingSet ws) {
 			return userID.equals(ws.getCreator().getId());
 		}
 	}
 
 }
