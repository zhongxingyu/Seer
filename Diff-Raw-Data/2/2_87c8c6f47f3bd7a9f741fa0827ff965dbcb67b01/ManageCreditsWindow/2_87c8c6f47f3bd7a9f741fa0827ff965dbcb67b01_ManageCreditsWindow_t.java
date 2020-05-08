 package org.iucn.sis.client.api.ui.users.panels;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.FieldWidgetCache;
 import org.iucn.sis.client.api.caches.RecentlyAccessedCache;
 import org.iucn.sis.client.api.caches.UserCache;
 import org.iucn.sis.client.api.caches.RecentlyAccessedCache.RecentUser;
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.models.ClientUser;
 import org.iucn.sis.client.api.ui.users.panels.UserSearchController.SearchResults;
 import org.iucn.sis.client.api.utils.BasicWindow;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.shared.api.acl.InsufficientRightsException;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.RecentlyAccessed;
 import org.iucn.sis.shared.api.models.fields.ProxyField;
 import org.iucn.sis.shared.api.models.fields.RedListCreditedUserField;
 import org.iucn.sis.shared.api.structures.SISOptionsList;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 
 import com.extjs.gxt.ui.client.GXT;
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.dnd.ListViewDragSource;
 import com.extjs.gxt.ui.client.dnd.ListViewDropTarget;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.DNDEvent;
 import com.extjs.gxt.ui.client.event.DNDListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.store.Store;
 import com.extjs.gxt.ui.client.store.StoreSorter;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.ListView;
 import com.extjs.gxt.ui.client.widget.ListViewSelectionModel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ButtonBar;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.layout.TableData;
 import com.extjs.gxt.ui.client.widget.layout.TableLayout;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.TextBox;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNode;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 import com.solertium.util.portable.PortableAlphanumericComparator;
 
 /**
  * ManageCreditsWindow.java
  * 
  * Allows browsing of the database and find a list of users, and has an abstract
  * callback method when a selection has been made.
  * 
  * @author carl.scott <carl.scott@solertium.com>
  *  
  */
 public class ManageCreditsWindow extends BasicWindow implements DrawsLazily {
 
 	/**
 	 * MCSearchResultsComparator
 	 * 
 	 * Wrapper for the PortableAlphanumericComparators that integrates with the
 	 * StoreSorter.
 	 * 
 	 * @author carl.scott <carl.scott@solertium.com>
 	 * 
 	 */
 	private static class MCSearchResultsComparator extends
 			StoreSorter<SearchResults> {
 		private static final long serialVersionUID = 1L;
 		private final PortableAlphanumericComparator c = new PortableAlphanumericComparator(); 
 		
 		private List<Integer> order;
 		private boolean isOrderStale = false;
 		
 		public MCSearchResultsComparator(String order) {
 			if (order == null || "".equals(order))
 				this.order = null;
 			else {
 				this.order = new ArrayList<Integer>();
 				for (String s : order.split(",")) {
 					try {
 						this.order.add(Integer.valueOf(s));
 					} catch (Exception how) { }
 				}
 			}
 		}
 		
 		public void setOrderStale(boolean isOrderStale) {
 			this.isOrderStale = isOrderStale;
 		}
 		
 		public void moveUp(ListStore<SearchResults> store, SearchResults model) {
 			if (order == null || isOrderStale) {
 				order = new ArrayList<Integer>();
 				for (SearchResults m : store.getModels())
 					order.add(m.getUser().getId());
 				
 				isOrderStale = false;
 			}
 			
 			int index = order.indexOf(model.getUser().getId());
 			if (index > 0) {
 				order.remove(index);
 				order.add(index-1, model.getUser().getId());
 			}
 			
 			store.sort("order", SortDir.ASC);
 		}
 		
 		public void moveDown(ListStore<SearchResults> store, SearchResults model) {
 			if (order == null || isOrderStale) {
 				order = new ArrayList<Integer>();
 				for (SearchResults m : store.getModels())
 					order.add(m.getUser().getId());
 				
 				isOrderStale = false;
 			}
 			
 			int index = order.indexOf(model.getUser().getId());
 			if (index < store.getModels().size() - 1) {
 				order.remove(index);
 				order.add(index + 1, model.getUser().getId());
 			}
 					
 			store.sort("order", SortDir.ASC);
 		}
 		
 		public void sortAZ(ListStore<SearchResults> store) {
 			order = null; //No more explicit order
 			
 			store.sort("name", SortDir.ASC);
 		}
 
 		@Override
 		public int compare(Store<SearchResults> store, SearchResults m1,
 				SearchResults m2, String property) {
 			if ("order".equals(property) && order != null) {
 				int m1Index = order.indexOf(m1.getUser().getId());
 				int m2Index = order.indexOf(m2.getUser().getId());
 				
 				if (m1Index == -1)
 					return 1;
 				else if (m2Index == -1)
 					return -1;
 				else
 					return Integer.valueOf(m1Index).compareTo(Integer.valueOf(m2Index));
 			}
 			else {
 				return sortByName(m1, m2);
 			}
 		}
 		
 		private int sortByName(SearchResults m1, SearchResults m2) {
 			/*
 			 * Requirement: Sort is done "by Last, First in ascending order"
 			 */
 			for (String current : new String[]{ "lastname", "firstname" }) {
 				int value = c.compare(m1.get(current), m2.get(current));
 				if (value != 0)
 					return value;
 			}
 			return 0;
 		}
 	}
 
 	private final ListView<SearchResults> results, recent, assessors, reviewers, contributors, facilitators;
 	private final HTML status;
 
 	public final static int windowWidth = 750;
 	public final static int windowHeight = 550;
 
 	private Html recentUsersHeading = new Html("<b>Recently Used</b>");
 	private Html searchResultHeading = new Html("<b>Search Result</b>");
 	
 	private Html assessorsHeading = new Html("<b>Assessors*</b>");
 	private Html reviewersHeading = new Html("<b>Reviewers*</b>");
 	private Html contributorsHeading = new Html("<b>Contributors</b>");
 	private Html facilitatorsHeading = new Html("<b>Facilitators</b>");
 	
 	private boolean drawn = false;
 	
 	//private ComplexListener<List<ClientUser>> listener;
  
 	public ManageCreditsWindow() {
 		super("Assessment Credits");
 
 		assessors = newListView();
 		
 		reviewers = newListView();
 				
 		contributors = newListView();
 		
 		facilitators = newListView();
 
 		ListViewSelectionModel<SearchResults> sm = new ListViewSelectionModel<SearchResults>();
 		sm.setSelectionMode(SelectionMode.MULTI);
 
 		results = newListView(300);
 		results.setSelectionModel(sm); 
 		/*
 		 * Requirement: "Search results are returned, including email addresses"
 		 */
 		results.setSimpleTemplate("<div style=\"text-align:left;\">{lastname}, {firstname} ({email})</div>");
 
 		sm = new ListViewSelectionModel<SearchResults>();
 		sm.setSelectionMode(SelectionMode.MULTI);
 		
 		recent = newListView(150);
 		recent.setSelectionModel(sm);
 
 		status = new HTML();
 		
 		setSize(windowWidth, windowHeight);
 
 	}
 	
 	private ListView<SearchResults> newListView() {
 		return newListView(160, 150);
 	}
 	
 	private ListView<SearchResults> newListView(int width) {
 		return newListView(width, 150);
 	}
 	
 	private ListView<SearchResults> newListView(int width, int height) {
 		ListViewSelectionModel<SearchResults> sm = new ListViewSelectionModel<SearchResults>();
 		sm.setSelectionMode(SelectionMode.SINGLE);
 		
 		ListStore<SearchResults> store = new ListStore<SearchResults>();
 		store.setStoreSorter(new MCSearchResultsComparator(null));
 		
 		ListView<SearchResults> view = new ListView<SearchResults>();
 		view.setSelectionModel(sm);
 		view.setHeight(height);
 		view.setWidth(width);
 		/*
 		 * Requirement: "All names in all boxes must appear using 
 		 * Last Name, First name format." 
 		 */
 		view.setSimpleTemplate("<div style=\"text-align:left;\">{lastname}, {firstname}</div>");
 		view.setStore(store);
 		
 		return view;
 	}
 
 	public boolean containsSearchResult(ListStore<SearchResults> store,
 			SearchResults result) {
 		for (int i = 0; i < store.getCount(); i++) {
 			if (store.getAt(i).getUser().getId() == result.getUser().getId())
 				return true;
 		}
 		return false;
 	}
 	
 	public void show() {
 		draw(new DrawsLazily.DoneDrawingCallback() {
 			public void isDrawn() {
 				open();
 			}
 		});
 	}
 	
 	private void open() {
 		super.show();
 	}
 	
 	/**
 	 * Draws the UI, only need be called once.
 	 * 
 	 */
 	public void draw(final DoneDrawingCallback callback) {
 		if (drawn) {
 			callback.isDrawn();
 			return;
 		}
 		
 		drawn = true;
 		
 		final TextBox first = new TextBox();
 		first.setName("firstname");
 		final TextBox last = new TextBox();
 		last.setName("lastname");
 		final TextBox nickname = new TextBox();
 		nickname.setName("nickname");
 		final TextBox affiliation = new TextBox();
 		affiliation.setName("affiliation");
 
 		final FlexTable form = new FlexTable();
 		final ButtonBar bar = new ButtonBar();
 		bar.setAlignment(HorizontalAlignment.RIGHT);
 		bar.add(new Button("Search", new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				final Map<String, String> params = new HashMap<String, String>();
 				for (int i = 0; i < form.getRowCount(); i++) {
 					if (form.getCellCount(i) > 1) {
 						TextBox box = (TextBox) form.getWidget(i, 1);
 						if (!box.getText().equals(""))
 							params.put(box.getName(), box.getText());
 					}
 				}
 				if (params.isEmpty())
 					WindowUtils.errorAlert("You must specify at least one criteria");
 				else
 					search(params);
 			}
 		}));
 			
 		form.setHTML(0, 0, "First Name: ");
 		form.setWidget(0, 1, first);
 		form.setHTML(1, 0, "Last Name: ");
 		form.setWidget(1, 1, last);
 		form.setHTML(2, 0, "Nickname: ");
 		form.setWidget(2, 1, nickname);
 		form.setHTML(3, 0, "Affiliation: ");
 		form.setWidget(3, 1, affiliation);
 		form.setWidget(4, 0, bar);
 		form.getFlexCellFormatter().setColSpan(4, 0, 2);
 					
 		/*----------------------------------------------------------------*/
 		final LayoutContainer topContainer = new LayoutContainer();
 		final TableLayout topLayout = new TableLayout(3);
 		topLayout.setWidth("100%");
 		topLayout.setHeight("100%");
 			
 		final TableData header = new TableData();
 		header.setHorizontalAlign(HorizontalAlignment.CENTER);
 		header.setHeight("25px");
 		
 		final TableData body = new TableData();
 		body.setHorizontalAlign(HorizontalAlignment.CENTER);
 		body.setMargin(5);
 		
 		final TableData selectButton = new TableData();
 		selectButton.setHorizontalAlign(HorizontalAlignment.CENTER);
 		selectButton.setHeight("25px");
 			
 		final TableData filler = new TableData();
 		filler.setColspan(2);
 		
 		topContainer.setLayout(topLayout);
 		
 		topContainer.add(recentUsersHeading, header);
 		if (canAddProfiles())
 			topContainer.add(new Button("Add New Profile", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					final ComplexListener<String> listener = new ComplexListener<String>() {
 						public void handleEvent(String eventData) {
 							final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
 							document.get(UriBase.getInstance().getUserBase() + "/users/" + eventData, new GenericCallback<String>() {
 								public void onSuccess(String result) {
 									NativeNodeList nodes = document.getDocumentElement().getChildNodes();
 									for (int i = 0; i < nodes.getLength(); i++) {
 										NativeNode node = nodes.item(i);
 										if ("user".equals(node.getNodeName())) {
 											ClientUser user = ClientUser.fromXML((NativeElement)node);
 											
 											final ListStore<SearchResults> store = new ListStore<SearchResults>();
 											store.setStoreSorter(new MCSearchResultsComparator(null));
 											store.add(new SearchResults(user));
 
 											results.setStore(store);
 
 											status.setHTML("Found " + store.getCount() + " results.");
 											
 											break;
 										}
 									}
 								}
 								public void onFailure(Throwable caught) {
 									// TODO Auto-generated method stub
 									
 								}
 							});
 						}
 					};
 					AddProfileWindow card = new AddProfileWindow(listener);
 					card.show();
 				}
 			}), header);
 		else
 			topContainer.add(new HTML(""), header);
 		topContainer.add(searchResultHeading, header);
 					
 		topContainer.add(recent, body);
 		topContainer.add(form, body);
 		topContainer.add(results, body);
 			
 		topContainer.add(new Button("Select All",
 				new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				recent.getSelectionModel().selectAll();
 			}
 		}), selectButton);
 		topContainer.add(new HTML(""), selectButton);
 		topContainer.add(new HTML(""), selectButton);
 		
 		topContainer.add(new Button("Clear Recent Users",
 				new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				WindowUtils.confirmAlert("Confirm", "Are you sure you want to remove all of your recent users?", new WindowUtils.SimpleMessageBoxListener() {
 					public void onYes() {
 						RecentlyAccessedCache.impl.deleteAll(RecentlyAccessed.USER, new GenericCallback<Object>() {
 							public void onSuccess(Object result) {
 								loadRecentUsers();	
 							}
 							public void onFailure(Throwable caught) {
 								WindowUtils.errorAlert("Failed to clear recent users, please try again later.");
 							}
 						});
 					}
 				});
 			}
 		}), selectButton);
 		topContainer.add(new HTML(""), selectButton);
 		topContainer.add(new HTML(""), selectButton);
 		
 		/*----------------------------------------------------------------*/
 					
 		final LayoutContainer bottomContainer = new LayoutContainer();
 		final TableLayout bottomLayout = new TableLayout(4);
 		bottomLayout.setWidth("100%");
 		bottomLayout.setHeight("100%");
 		
 		final TableData listHeader = new TableData();
 		listHeader.setHorizontalAlign(HorizontalAlignment.CENTER);
 		listHeader.setHeight("25px");
 		
 		final TableData lists = new TableData();
 		lists.setHorizontalAlign(HorizontalAlignment.CENTER);
 		lists.setMargin(3);
 		
 		final TableData filler1 = new TableData();
 		filler1.setColspan(3);
 		
 		final TableData sortButtons = new TableData();
 		sortButtons.setHorizontalAlign(HorizontalAlignment.CENTER);
 		sortButtons.setHeight("25px");
 		
 		bottomContainer.setLayout(bottomLayout);
 		
 		bottomContainer.add(assessorsHeading, listHeader);
 		bottomContainer.add(reviewersHeading, listHeader);
 		bottomContainer.add(contributorsHeading, listHeader);
 		bottomContainer.add(facilitatorsHeading, listHeader);
 		
 		bottomContainer.add(assessors, lists);
 		bottomContainer.add(reviewers, lists);
 		bottomContainer.add(contributors, lists);
 		bottomContainer.add(facilitators, lists);
 				
 		bottomContainer.add(createButtonBar(assessors), sortButtons);
 		bottomContainer.add(createButtonBar(reviewers), sortButtons);
 		bottomContainer.add(createButtonBar(contributors), sortButtons);
 		bottomContainer.add(createButtonBar(facilitators), sortButtons);
 					
 		/*----------------------------------------------------------------*/
 		
 		new ListViewDragSource(results);			
 		new ListViewDragSource(recent);
 		new ListViewDragSource(assessors);
 		new ListViewDragSource(reviewers);
 		new ListViewDragSource(contributors);
 		new ListViewDragSource(facilitators);
 		
 		new ListViewDropTarget(results);
 		
 		allowDropItems(recent,"RECENT");
 		allowDropItems(assessors,"OTHER");
 		allowDropItems(reviewers,"OTHER");
 		allowDropItems(contributors,"OTHER");
 		allowDropItems(facilitators,"OTHER");
 		
 		final LayoutContainer topWrapper = new LayoutContainer();
 		topWrapper.setLayout(new BorderLayout());
 		BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH,
 				110, 200, 110);
 		data.setMargins(new Margins(10));
 		topWrapper.add(topContainer, data);
 
 		final LayoutContainer bottomWrapper = new LayoutContainer();
 		bottomWrapper.setLayout(new BorderLayout());
 		bottomWrapper.add(bottomContainer, data);
 				
 		setLayout(new FillLayout());
 		add(topWrapper);
 		add(bottomWrapper);
 		
 		addButtons();
 		loadRecentUsers();
 
 		loadAssessmentData(callback);
 	}
 	
 	private ButtonBar createButtonBar(final ListView<SearchResults> view) {
 		Button up = new Button();
 		up.setIconStyle("icon-up");
 		up.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				if (view.getSelectionModel().getSelectedItem() != null) {
 					SearchResults r = (SearchResults)view.getSelectionModel().getSelectedItem();
 					
 					ListStore<SearchResults> store = view.getStore();
 					((MCSearchResultsComparator)store.getStoreSorter()).moveUp(store, r);
 				}
 			}
 		});
 		
 		Button down = new Button();
 		down.setIconStyle("icon-down");
 		down.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				if (view.getSelectionModel().getSelectedItem() != null) {
 					SearchResults r = (SearchResults)view.getSelectionModel().getSelectedItem();
 					
 					ListStore<SearchResults> store = view.getStore();
 					((MCSearchResultsComparator)store.getStoreSorter()).moveDown(store, r);
 				}
 			}
 		});
 		
 		ButtonBar bar = new ButtonBar();
 		bar.setAlignment(HorizontalAlignment.CENTER);
 		bar.add(up);
 		bar.add(down);
 		bar.add(new Button("Sort A-Z",
 				new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				ListStore<SearchResults> store = view.getStore();
 				
 				((MCSearchResultsComparator)store.getStoreSorter()).sortAZ(store);
 			}
 		}));
 		return bar;
 	}
 	
 	private boolean canAddProfiles() {
 		return AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.USE_FEATURE, AuthorizableFeature.ADD_PROFILE_FEATURE);
 	}
 	
 	/*
 	 * Finds all the user IDs selected and does an initial search to load 
 	 * up all the users that are not in the UserCache.  Then, adds the users 
 	 * to the appropriate panels.
 	 * 
 	 * Borrowed from SISCompleteListTextArea.java
 	 */
 	protected void loadAssessmentData(final DrawsLazily.DoneDrawingCallback callback) {
 		final Assessment assessment = AssessmentCache.impl.getCurrentAssessment();
 		
 		final String[] fieldNames = new String[] {
 			CanonicalNames.RedListAssessors, CanonicalNames.RedListEvaluators, 
 			CanonicalNames.RedListContributors, CanonicalNames.RedListFacilitators
 		};
 		
 		final Set<String> userids = new HashSet<String>();
 		for (String fieldName : fieldNames) {
 			Field field = assessment.getField(fieldName);
 			if (field == null)
 				continue;
 			
 			ProxyField proxy = new ProxyField(field);
 			List<Integer> values = 
 				proxy.getForeignKeyListPrimitiveField(SISOptionsList.FK_LIST_KEY);
 			
 			if (values != null)
 				for (Integer value : values)				
 					if(value != 0)
 						if (!UserCache.impl.hasUser(value))
 							userids.add(value.toString());
 
 		}
 		
 		if (userids.isEmpty()){
 			loadSavedDetails(CanonicalNames.RedListAssessors,assessors);
 			loadSavedDetails(CanonicalNames.RedListEvaluators,reviewers);
 			loadSavedDetails(CanonicalNames.RedListContributors,contributors);
 			loadSavedDetails(CanonicalNames.RedListFacilitators,facilitators);
 			
 			callback.isDrawn();
 		}else {
 			Map<String, List<String>> map = new HashMap<String, List<String>>();
 			map.put("userid", new ArrayList<String>(userids));
 			
			UserSearchController.search(map, "or", true, new GenericCallback<List<SearchResults>>() {
 				public void onFailure(Throwable caught) {
 					WindowUtils.errorAlert("Error loading existing values, please try again later.");
 					//No callback, no need to draw the window.
 				}
 				@Override
 				public void onSuccess(List<SearchResults> results) {
 					for (SearchResults result : results)
 						UserCache.impl.addUser(result.getUser());
 					
 					loadSavedDetails(CanonicalNames.RedListAssessors,assessors);
 					loadSavedDetails(CanonicalNames.RedListEvaluators,reviewers);
 					loadSavedDetails(CanonicalNames.RedListContributors,contributors);
 					loadSavedDetails(CanonicalNames.RedListFacilitators,facilitators);
 					
 					callback.isDrawn();
 				}
 			});
 		}		
 	}
 	
 	protected void addButtons() {
 		addButton(new Button("Save", new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				onSave();
 			}
 		}));
 		addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				hide();
 			}
 		}));
 	}
 
 	protected void onSave() {
 		try{
 			Assessment assessment = AssessmentCache.impl.getCurrentAssessment();
 		
 			saveField(CanonicalNames.RedListAssessors, assessment, assessors.getStore());
 			saveField(CanonicalNames.RedListEvaluators, assessment, reviewers.getStore());
 			saveField(CanonicalNames.RedListContributors, assessment, contributors.getStore());
 			saveField(CanonicalNames.RedListFacilitators, assessment, facilitators.getStore());
 
 			WindowUtils.showLoadingAlert("Saving Credits...");
 			AssessmentClientSaveUtils.saveAssessment(null,assessment, new GenericCallback<Object>() {
 				public void onFailure(Throwable arg0) {
 					WindowUtils.hideLoadingAlert();
 					WindowUtils.errorAlert("Save Failed", "Failed to save assessment! " + arg0.getMessage());
 				}
 	
 				public void onSuccess(Object arg0) {
 					WindowUtils.hideLoadingAlert();
 					Info.display("Save Complete", "Successfully saved assessment {0}.",
 							AssessmentCache.impl.getCurrentAssessment().getSpeciesName());
 					Debug.println("Explicit save happened at {0}", AssessmentCache.impl.getCurrentAssessment().getLastEdit().getCreatedDate());
 					
 					/*
 					 * Requirement: "Upon clicking Save & Close, the panel would close and
 					 * bring the user back to the Assessment Information tab, where the text 
 					 * strings for each of these data boxes would be updated/refreshed." 
 					 */
 					ManageCreditsWindow.this.hide();
 					FieldWidgetCache.impl.resetWidgetContents(
 						CanonicalNames.RedListAssessors, CanonicalNames.RedListEvaluators, 
 						CanonicalNames.RedListContributors, CanonicalNames.RedListFacilitators
 					);
 				}
 			});
 			
 			
 		}catch(InsufficientRightsException e){
 			WindowUtils.errorAlert("Sorry, but you do not have sufficient rights to perform this action.");
 		}
 		
 	}
 
 	private void saveField(String fieldName, Assessment assessment, ListStore<SearchResults> store) {
 		final Set<Integer> userIDs = new HashSet<Integer>();
 		final ArrayList<ClientUser> selectedUsers = new ArrayList<ClientUser>();
 		final StringBuilder order = new StringBuilder();
 		
 		for (Iterator<SearchResults> iter = store.getModels().listIterator(); iter.hasNext(); ) {
 			SearchResults model = iter.next();
 			userIDs.add(model.getUser().getId());
 			order.append(model.getUser().getId());
 			if (iter.hasNext())
 				order.append(",");
 		}
 			
 		Field field = assessment.getField(fieldName);
 		if (field == null) {
 			field = new Field(fieldName, assessment);
 			assessment.getField().add(field);
 		}
 		
 		/* 
 		 * Add UI Selected User's to the UserCache		
 		 */
 		final Iterator<SearchResults> iterator = store.getModels().iterator();
 		while (iterator.hasNext()) {
 			ClientUser user = iterator.next().getUser();		
 			selectedUsers.add(user);
 		}
 		UserCache.impl.addUsers(selectedUsers);
 		
 		/*
 		 * The SISOptionsList is the field that drives the original 
 		 * UI widget for assessors, evaluatiors, and contributors. 
 		 * So, I am going to use the same field keys from there for 
 		 * this save operation.
 		 */
 		RedListCreditedUserField proxy = new RedListCreditedUserField(field);
 		proxy.setUsers(new ArrayList<Integer>(userIDs));
 		proxy.setOrder(order.toString());
 		if (!userIDs.isEmpty())
 			proxy.setText(null); //Remove any custom text entries.
 		//proxy.setText(RedListCreditedUserField.generateText(selectedUsers, order.toString()));
 	}
 	
 	public void loadRecentUsers() {
 		ListStore<SearchResults> recentUserStore = new ListStore<SearchResults>();
 		recentUserStore.setStoreSorter(new MCSearchResultsComparator(null));
 	
 		List<RecentUser> users = RecentlyAccessedCache.impl.list(RecentlyAccessed.USER); 
 			
 		for (RecentUser user : users)
 			recentUserStore.add(new SearchResults(user.getUser()));
 		
 		recentUserStore.sort("name", SortDir.ASC);
 		
 		recent.setStore(recentUserStore);
 
 	}
 	
 	public void loadSavedDetails(String canonicalName, ListView<SearchResults> list) {
 		Assessment assessment = AssessmentCache.impl.getCurrentAssessment();			
 		Field field = assessment.getField(canonicalName);
 		if (field == null)
 			return;
 
 		RedListCreditedUserField proxy = new RedListCreditedUserField(field);
 		
 		ListStore<SearchResults> store = new ListStore<SearchResults>();
 		store.setStoreSorter(new MCSearchResultsComparator(proxy.getOrder()));
 		
 		for (Integer userID : proxy.getUsers())
 			if (userID != 0 && UserCache.impl.hasUser(userID))
 				store.add(new SearchResults(UserCache.impl.getUser(userID)));
 		
 		store.sort("order", SortDir.NONE);
 		
 		list.setStore(store);
 	}
 
 	/**
 	 * Performs the search and updates the status text. Appropriate parameter
 	 * keys are "lastname", "firstname", and "affiliation", although "email"
 	 * could be added in the future as any column in the profile table is
 	 * applicable at this time. A like query will be performed if parameters are
 	 * supplied, with multiple parameters being OR'd together.
 	 * 
 	 * @param params
 	 *            - search paramteers.
 	 */
 	protected void search(Map<String, String> params) {
 		GenericCallback<List<SearchResults>> callback = new GenericCallback<List<SearchResults>>() {
 			public void onFailure(Throwable caught) {
 				ManageCreditsWindow.this.hide();
 				WindowUtils.errorAlert("Error",
 						"Could not load results, please try again later");
 			}
 			public void onSuccess(List<SearchResults> result) {
 				final ListStore<SearchResults> store = new ListStore<SearchResults>();
 				store.setStoreSorter(new MCSearchResultsComparator(null));
 				for (SearchResults res : result)
 					store.add(res);
 				store.sort("name", SortDir.ASC);
 
 				results.setStore(store);
 
 				String statusText = "Found " + store.getCount() + " results.";
 				status.setHTML(statusText);
 			}
 		};
 
 		Map<String, List<String>> newParams = new HashMap<String, List<String>>();
 		for (Entry<String, String> entry : params.entrySet()) {
 			ArrayList<String> list = new ArrayList<String>();
 			list.add(entry.getValue());
 			newParams.put(entry.getKey(), list);
 		}
 		
 		UserSearchController.search(newParams, "and", callback);
 	}
 
 	public void setSearchResultHeading(String searchResultHeading) {
 		this.searchResultHeading.setHtml("<b>" + searchResultHeading + "</b>");
 	}
 
 	public void setRecentUsersHeading(String recentUsersHeading) {
 		this.recentUsersHeading.setHtml("<b>" + recentUsersHeading + "</b>");
 	}
 
 	public void setAssessorsHeading(String assessorsHeading) {
 		this.assessorsHeading.setHtml("<b>" + assessorsHeading + "</b>");
 	}
 	
 	public void setReviewersHeading(String reviewersHeading) {
 		this.reviewersHeading.setHtml("<b>" + reviewersHeading + "</b>");
 	}
 	
 	public void setContributorsHeading(String contributorsHeading) {
 		this.contributorsHeading.setHtml("<b>" + contributorsHeading + "</b>");
 	}
 	
 	public void setFacilitatorsHeading(String facilitatorsHeading) {
 		this.facilitatorsHeading.setHtml("<b>" + facilitatorsHeading + "</b>");
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void allowDropItems(final ListView<SearchResults> drList,final String id){
 		ListViewDropTarget selectedTarget = new ListViewDropTarget(drList); 
 		selectedTarget.addDNDListener(new DNDListener() {
 			@Override
 			public void dragEnter(DNDEvent e) {
 				try {
 					boolean hasCollision = false;
 					List<SearchResults> data = (List<SearchResults>)(e.getData());
 					for (SearchResults datum : data) {
 						if (hasCollision = detectCollision(datum)) {
 							e.setCancelled(true);
 							e.getStatus().update("Duplicate User Detected");
 							break;
 						}
 					}
 					if (!hasCollision)
 						e.getStatus().update(GXT.MESSAGES.grid_ddText(data.size()));
 				} catch (Throwable e1) {
 					e1.printStackTrace();
 				}
 			}
 			
 			@Override
 			public void dragDrop(DNDEvent e) {
 				if (!id.equals("RECENT")) {
 					MCSearchResultsComparator sorter = 
 						(MCSearchResultsComparator)drList.getStore().getStoreSorter();
 					sorter.setOrderStale(true);
 					
 					List<SearchResults> data = (List<SearchResults>)(e.getData());
 					List<RecentUser> recent = new ArrayList<RecentUser>();
 					for (SearchResults user : data)
 						recent.add(new RecentUser(user.getUser()));
 					
 					RecentlyAccessedCache.impl.add(RecentlyAccessed.USER, recent);
 					
 					DeferredCommand.addPause();
 					DeferredCommand.addCommand(new Command() {
 						public void execute() {
 							loadRecentUsers();
 						}
 					});
 				}
 			}
 			
 			private boolean detectCollision(SearchResults datum) {
 				for( SearchResults cur : drList.getStore().getModels() ) {	
 					if( datum.get("userid").toString().trim().equals(cur.get("userid").toString().trim()))
 						return true;
 				}
 				return false;
 			}
 		});	
 	}
 }
