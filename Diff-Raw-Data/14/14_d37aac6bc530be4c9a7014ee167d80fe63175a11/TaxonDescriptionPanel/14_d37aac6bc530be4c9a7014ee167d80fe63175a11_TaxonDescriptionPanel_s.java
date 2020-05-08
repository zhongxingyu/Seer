 package org.iucn.sis.client.panels.taxa;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.RegionCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.ui.models.image.ManagedImage;
 import org.iucn.sis.client.api.ui.models.taxa.TaxonListElement;
 import org.iucn.sis.client.api.utils.FormattedDate;
 import org.iucn.sis.client.api.utils.TaxonPagingLoader;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.client.panels.ClientUIContainer;
 import org.iucn.sis.client.panels.PanelManager;
 import org.iucn.sis.client.panels.images.ImageManagerPanel;
 import org.iucn.sis.client.panels.notes.NoteAPI;
 import org.iucn.sis.client.panels.notes.NotesWindow;
 import org.iucn.sis.client.panels.taxomatic.CommonNameToolPanel;
 import org.iucn.sis.client.panels.taxomatic.EditCommonNamePanel;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentType;
 import org.iucn.sis.shared.api.models.CommonName;
 import org.iucn.sis.shared.api.models.Notes;
 import org.iucn.sis.shared.api.models.Synonym;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.TaxonLevel;
 import org.iucn.sis.shared.api.utils.AssessmentFormatter;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 import org.iucn.sis.shared.api.utils.CommonNameComparator;
 import org.iucn.sis.shared.api.utils.XMLUtils;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.binder.DataListBinder;
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.GridEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.event.WindowEvent;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.store.StoreSorter;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.DataList;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.InfoConfig;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.lwxml.shared.utils.ArrayUtils;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 import com.solertium.util.gwt.ui.StyledHTML;
 import com.solertium.util.portable.PortableAlphanumericComparator;
 
 public class TaxonDescriptionPanel extends LayoutContainer {
 
 	private final PanelManager panelManager;
 	
 	private Taxon taxon;
 	private HTML headerAssess;
 	private int panelHeight;
 	private Image taxonImage;//, googleMap;
 	private Window imagePopup = null;
 	private ImageManagerPanel imageManager;
 	private ComplexListener<Integer> updateListener;
 
 	public TaxonDescriptionPanel(PanelManager manager, Taxon taxon) {
 		super();
 		setLayoutOnChange(true);
 		addStyleName("padded");
 		
 		this.taxon = taxon;
 		this.panelManager = manager;
 	}
 	
 	public void setUpdateListener(ComplexListener<Integer> updateListener) {
 		this.updateListener = updateListener;
 	}
 	
 	public void updatePanel(final DrawsLazily.DoneDrawingCallback callback) {
 		if (taxon != null) {
 			AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, taxon.getId()),
 					new GenericCallback<String>() {
 				public void onSuccess(String result) {
 					drawPanel(callback);
 				}
 				public void onFailure(Throwable caught) {
 					callback.isDrawn();
 				};
 			});
 		} else
 			drawPanel(callback);
 	}
 	
 	public void update(Integer nodeID) {
 		if (updateListener != null)
 			updateListener.handleEvent(nodeID);
 	}
 
 	private void drawPanel(final DrawsLazily.DoneDrawingCallback callback) {
 		if (taxon == null) {
 			add(new HTML("No summary available."));
 			callback.isDrawn();
 		}
 		else {
 			getGeneralInformationPanel(taxon, new DrawsLazily.DoneDrawingCallbackWithParam<ContentPanel>() {
 				public void isDrawn(final ContentPanel generalInformation) {
 					getChildrenPanel(taxon, new DrawsLazily.DoneDrawingCallbackWithParam<ContentPanel>() {
 						public void isDrawn(final ContentPanel children) {
 							Image prevTaxon = new Image("tango/actions/go-previous.png");
 							prevTaxon.addClickHandler(new ClickHandler() {
 								public void onClick(ClickEvent event) {
 									TaxonomyCache.impl.fetchTaxon(taxon.getParentId(), true, new GenericCallback<Taxon>() {
 										public void onFailure(Throwable caught) {
 											//updatePanel(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
 										}
 										public void onSuccess(Taxon arg0) {
 											/*taxon = arg0;
 											updatePanel(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
 											ClientUIContainer.headerContainer.update();*/
 											//update(taxon.getParentId());
 										}
 									});
 								}
 							});
 					
 							HorizontalPanel hPanel = new HorizontalPanel();
 							hPanel.setStyleName("SIS_taxonSummaryHeader_panel");
 							if (taxon.getParentId() != 0) {
 								hPanel.add(prevTaxon);
 								hPanel.setCellWidth(prevTaxon, "30px");
 								hPanel.setCellVerticalAlignment(prevTaxon, HasVerticalAlignment.ALIGN_MIDDLE);
 							}
 							hPanel.add(new StyledHTML(" <i>" + (taxon.getLevel() >= TaxonLevel.SPECIES ? taxon.getFullName() : taxon.getName()) + "</i>", "SIS_taxonSummaryHeader"));
 							hPanel.add(headerAssess = new StyledHTML("", "SIS_taxonSummaryHeader"));
 					
 					
 							VerticalPanel westPanel = new VerticalPanel();
 							westPanel.add(generalInformation);
 							if (taxon.getLevel() >= TaxonLevel.SPECIES) {
 								westPanel.add(getAssessmentInformationPanel(taxon));
 							} else {
 								AssessmentCache.impl.resetCurrentAssessment();
 							}
 					
 							HorizontalPanel hp = new HorizontalPanel();
 							hp.add(getTaxonomicNotePanel(taxon));
 							hp.add(children);
 					
 							VerticalPanel vp = new VerticalPanel();
 							if (taxon.getLevel() >= TaxonLevel.SPECIES)
 								vp.add(getAssessmentsPanel(taxon));
 							vp.add(hp);
 							
 							final DockPanel wrapper = new DockPanel();
 							wrapper.add(hPanel, DockPanel.NORTH);
 							wrapper.add(westPanel, DockPanel.WEST);
 							wrapper.add(vp, DockPanel.CENTER);
 							wrapper.setSize("100%", "100%");
 							
 							removeAll();
 							add(wrapper);
 							
 							callback.isDrawn();
 						}
 					});
 				}
 			});
 		}
 	}
 
 	private ContentPanel getAssessmentInformationPanel(final Taxon node) {
 		final ContentPanel assessmentInformation = new ContentPanel();
 		assessmentInformation.setStyleName("x-panel");
 		assessmentInformation.setWidth(350);
 		assessmentInformation.setHeight(200);
 		assessmentInformation.setHeading("Most Recent Published Status");
 		assessmentInformation.setLayoutOnChange(true);
 
 		if (!node.getAssessments().isEmpty()) {
 			VerticalPanel assessPanel = new VerticalPanel();
 			
 			Assessment curAssessment = null;
 			Set<Assessment> assess = TaxonomyCache.impl.getTaxon(node.getId()).getAssessments();
 			for (Assessment cur : assess) {
 				if (!cur.getIsHistorical()) {
 					curAssessment = cur;
 					break;
 				}
 			}
 
 			//TODO: TEST THIS
 			
 			if (curAssessment == null) {
 				assessPanel.add(new HTML("This taxon does not have a non-historical assessment."));
 			} else {
 				VerticalPanel assessInfoPanel = new VerticalPanel();
 				assessInfoPanel.setStyleName("SIS_taxonSummaryHeader_assessHeader");
 
 				assessInfoPanel.add(new HTML("&nbsp;&nbsp;Assessment ID: " + curAssessment.getId()));
 
 				assessInfoPanel.add(new HTML("&nbsp;&nbsp;Category: "
 						+ AssessmentFormatter.getProperCategoryAbbreviation(curAssessment)));
 				assessInfoPanel.add(new HTML("&nbsp;&nbsp;Criteria: " + AssessmentFormatter.getProperCriteriaString(curAssessment)));
 				assessInfoPanel.add(new HTML("&nbsp;&nbsp;Assessed: " + curAssessment.getDateAssessed()));
 				assessInfoPanel.add(new HTML("&nbsp;&nbsp;Assessor: " + AssessmentFormatter.getDisplayableAssessors(curAssessment)));
 				assessPanel.add(assessInfoPanel);
 
 				assessPanel.add(new HTML("&nbsp;&nbsp;Major Threats: " + ""));
 				assessPanel.add(new HTML("&nbsp;&nbsp;Population Trend: " + ""));
 				assessPanel.add(new HTML("&nbsp;&nbsp;Major Importance Habitats: " + ""));
 				assessPanel.add(new HTML("&nbsp;&nbsp;Conservation Actions Needed: " + ""));
 				headerAssess.setText(
 						AssessmentFormatter.getProperCategoryAbbreviation(curAssessment)
 						+ curAssessment.getDateAssessed());
 			}
 			assessmentInformation.add(assessPanel);
 		} else
 			assessmentInformation.add(new HTML("There is no published data for this taxon."));
 		
 		return assessmentInformation;
 	}
 
 	private ContentPanel getAssessmentsPanel(final Taxon node) {
 		final ListStore<BaseModelData> store = new ListStore<BaseModelData>();
 		for (Assessment data : AssessmentCache.impl.getPublishedAssessmentsForTaxon(node.getId())) {
 			BaseModelData model = new BaseModelData();
 			model.set("date", data.getDateAssessed() == null ? "(Not set)" : FormattedDate.impl.getDate(data.getDateAssessed()));
 			model.set("category", AssessmentFormatter.getProperCategoryAbbreviation(data));
 			model.set("criteria", AssessmentFormatter.getProperCriteriaString(data));
 			model.set("status", "Published");
 			model.set("edit", "");
 			model.set("trash", "");
 			model.set("id", data.getId());
 
 			store.add(model);
 		}
 
 		for (Assessment data : AssessmentCache.impl.getDraftAssessmentsForTaxon(node.getId())) {
 			BaseModelData model = new BaseModelData();
 
 			if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ, data)) {
 				model.set("date", data.getDateAssessed() == null ? "(Not set)" : FormattedDate.impl.getDate(data.getDateAssessed()));
 				model.set("category", AssessmentFormatter.getProperCategoryAbbreviation(data));
 				model.set("criteria", AssessmentFormatter.getProperCriteriaString(data));
 				if (data.isRegional())
 					model.set("status", "Draft - " + RegionCache.impl.getRegionName(data.getRegionIDs()));
 				else
 					model.set("status", "Draft");
 				model.set("edit", "");
 				model.set("trash", "");
 				model.set("id", data.getId());
 			} else {
 				model.set("date", "Sorry, you");
 				model.set("category", "do not have");
 				model.set("criteria", "permission");
 				model.set("status", "to view this");
 				model.set("edit", "draft assessment.");
 				model.set("trash", "");
 				model.set("id", data.getId());
 			}
 
 			store.add(model);
 		}
 		
 		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
 
 		columns.add(new ColumnConfig("date", "Assessment Date", 150));
 		columns.add(new ColumnConfig("category", "Category", 100));
 		columns.add(new ColumnConfig("criteria", "Category", 100));
 		columns.add(new ColumnConfig("status", "Category", 100));
 		
 		ColumnConfig editView = new ColumnConfig("edit", "Edit/View", 60);
 		editView.setRenderer(new GridCellRenderer<BaseModelData>() {
 			public Object render(BaseModelData model, String property,
 					ColumnData config, int rowIndex, int colIndex,
 					ListStore<BaseModelData> store, Grid<BaseModelData> grid) {
 				return "<img src =\"images/application_form_edit.png\" class=\"SIS_HyperlinkBehavior\"></img> ";
 			}
 		});
 		columns.add(editView);
 		
 		ColumnConfig trash = new ColumnConfig("trash", "Trash", 60);
 		trash.setRenderer(new GridCellRenderer<BaseModelData>() {
 			public Object render(BaseModelData model, String property,
 					ColumnData config, int rowIndex, int colIndex,
 					ListStore<BaseModelData> store, Grid<BaseModelData> grid) {
 				return "<img src =\"tango/places/user-trash.png\" class=\"SIS_HyperlinkBehavior\"></img> ";
 			}
 		});
 		columns.add(trash);
 
 		final Grid<BaseModelData> tbl = new Grid<BaseModelData>(store, new ColumnModel(columns));
 		tbl.setBorders(false);
 		tbl.removeAllListeners();
 		tbl.addListener(Events.RowClick, new Listener<GridEvent<BaseModelData>>() {
 			public void handleEvent(GridEvent<BaseModelData> be) {
 				if (be.getModel() == null)
 					return;
 
 				BaseModelData model = be.getModel();
 				
 				int column = be.getColIndex();
 
 				final Integer id = model.get("id");
 				final String status = model.get("status");
 				final String type = status.equals("Published") ? 
 						AssessmentType.PUBLISHED_ASSESSMENT_TYPE : AssessmentType.DRAFT_ASSESSMENT_TYPE;
 				if (column == 5) {
 					if (type == AssessmentType.PUBLISHED_ASSESSMENT_TYPE
 							&& !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser,
 									AuthorizableObject.DELETE, AssessmentCache.impl.getPublishedAssessment(id,
 											false))) {
 						WindowUtils.errorAlert("Insufficient Permissions", "You do not have permission "
 								+ "to perform this operation.");
 					} else if (type == AssessmentType.DRAFT_ASSESSMENT_TYPE
 							&& !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser,
 									AuthorizableObject.DELETE, AssessmentCache.impl.getDraftAssessment(id, false))) {
 						WindowUtils.errorAlert("Insufficient Permissions", "You do not have permission "
 								+ "to perform this operation.");
 					} else {
 						WindowUtils.confirmAlert("Confirm Delete",
 								"Are you sure you want to delete this assessment?",
 								new WindowUtils.SimpleMessageBoxListener() {
 							public void onYes() {
 								if (AssessmentCache.impl.getCurrentAssessment() != null
 										&& AssessmentCache.impl.getCurrentAssessment().getId() == id
 										.intValue())
 									AssessmentCache.impl.resetCurrentAssessment();
 								NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
 								doc.delete(UriBase.getInstance().getSISBase() + "/assessments/" + type
 										+ "/" + id, new GenericCallback<String>() {
 									public void onFailure(Throwable arg0) {
 										WindowUtils.errorAlert("Could not delete, please try again later.");
 									}
 									public void onSuccess(String arg0) {
 										TaxonomyCache.impl.evict(String.valueOf(node.getId()));
 										TaxonomyCache.impl.fetchTaxon(node.getId(), true,
 												new GenericCallback<Taxon>() {
 											public void onFailure(Throwable caught) {
 											};
 											public void onSuccess(Taxon result) {
 												AssessmentCache.impl.clear();
 												update(node.getId());
 												panelManager.recentAssessmentsPanel.update();
 											};
 										});
 									}
 								});
 							}
 						});
 					}
 				} else if (column == 4) {
 					Assessment fetched = AssessmentCache.impl.getAssessment(id, false);
 					// CHANGE
 					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ,
 							fetched)) {
 						AssessmentCache.impl.setCurrentAssessment(fetched);
 						ClientUIContainer.headerContainer.update();
 						ClientUIContainer.bodyContainer
 								.setSelection(ClientUIContainer.bodyContainer.tabManager.assessmentEditor);
 					} else {
 						WindowUtils.errorAlert("Sorry, you do not have permission to view this assessment.");
 					}
 				}
 			}
 
 		});
 
 		ClientUIContainer.headerContainer.update();
 		
 		tbl.getStore().sort("date", SortDir.DESC);
 
 		final ContentPanel assessments = new ContentPanel(new FillLayout());
 		assessments.setHeading("Assessment List");
 		assessments.setStyleName("x-panel");
 		assessments.setWidth(com.google.gwt.user.client.Window.getClientWidth() - 500);
 		assessments.setHeight(panelHeight);
 		assessments.add(tbl);
 		
 		return assessments;
 	}
 
 	private void getChildrenPanel(final Taxon taxon, final DrawsLazily.DoneDrawingCallbackWithParam<ContentPanel> callback) {
 		final ContentPanel children = new ContentPanel();
 		children.setLayout(new RowLayout(Orientation.VERTICAL));
 		children.setWidth((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2);
 		children.setHeight(200);
 		
 		if (Taxon.getDisplayableLevelCount() > taxon.getLevel() + 1) {
 			children.setHeading(Taxon.getDisplayableLevel(taxon.getLevel() + 1));
 			// children.setLayoutOnChange(true);
 			
 			final TaxonPagingLoader loader = new TaxonPagingLoader();
 			final PagingToolBar bar = new PagingToolBar(30);
 			bar.bind(loader.getPagingLoader());
 			
 			final DataList list = new DataList();
 			list.setSize((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2, 148);
 			list.setScrollMode(Scroll.AUTOY);
 			
 			final ListStore<TaxonListElement> store = new ListStore<TaxonListElement>(loader.getPagingLoader());
 			store.setStoreSorter(new StoreSorter<TaxonListElement>(new PortableAlphanumericComparator()));
 
 			final DataListBinder<TaxonListElement> binder = new DataListBinder<TaxonListElement>(list, store);
 			binder.setDisplayProperty("name");
 			binder.init();
 			binder.addSelectionChangedListener(new SelectionChangedListener<TaxonListElement>() {
 				public void selectionChanged(SelectionChangedEvent<TaxonListElement> se) {
 					if (se.getSelectedItem() != null) {
 						update(se.getSelectedItem().getNode().getId());
 					}
 				}
 			});
 
 			TaxonTreePopup.fetchChildren(taxon, new GenericCallback<List<TaxonListElement>>() {
 				public void onFailure(Throwable caught) {
 					children.add(new HTML("No " + Taxon.getDisplayableLevel(taxon.getLevel() + 1) + "."));
 					
 					callback.isDrawn(children);
 				}
 				public void onSuccess(List<TaxonListElement> result) {
 					loader.getFullList().addAll(result);
 					ArrayUtils.quicksort(loader.getFullList(), new Comparator<TaxonListElement>() {
 						public int compare(TaxonListElement o1, TaxonListElement o2) {
 							return ((String) o1.get("name")).compareTo((String) o2.get("name"));
 						}
 					});
 					children.add(list);
 					children.add(bar);
 
 					loader.getPagingLoader().load(0, loader.getPagingLoader().getLimit());
 
 					children.layout();
 					
 					callback.isDrawn(children);
 				}
 			});
 		} else {
 			children.setHeading("Not available.");
 			
 			callback.isDrawn(children);
 		}
 	}
 
 	private void getDistributionMapPanel(final Taxon node, final DrawsLazily.DoneDrawingCallbackWithParam<ContentPanel> callback) {
 		final LayoutContainer vp = new LayoutContainer();
 		vp.setStyleName("SIS_taxonSummaryHeader_mapPanel");
 		vp.setLayoutOnChange(true);
 		vp.setWidth((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2);
 		vp.setHeight(200);
 		
 		final ContentPanel cp = new ContentPanel();
 		cp.setHeading("Distribution Map");
 		cp.setWidth((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2);
 		cp.setHeight(200);
 		cp.add(vp);
 		
 		NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
 		doc.get(UriBase.getInstance().getSISBase() + "/raw/browse/spatial/" + node.getId() + ".jpg",
 				new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				Image map = new Image(UriBase.getInstance().getSISBase()
 						+ "/raw/browse/spatial/noMapAvailable.jpg");
 				map.setSize((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2 + "", "175");
 				
 				vp.add(map);
 				
 				callback.isDrawn(cp);
 			}
 			public void onSuccess(String result) {
 				final Image map = new Image("/raw/browse/spatial/" + node.getId() + ".jpg");
 				map.addClickHandler(new ClickHandler() {
 					public void onClick(ClickEvent event) {
 						Window s = WindowUtils.getWindow(false, false, "Map Distribution Viewer");
 						LayoutContainer content = s;
 						content.add(ClientUIContainer.bodyContainer.getTabManager().getPanelManager().imageViewerPanel);
 						if (!ClientUIContainer.bodyContainer.getTabManager().getPanelManager().imageViewerPanel
 								.isRendered())
 							ClientUIContainer.bodyContainer.getTabManager().getPanelManager().imageViewerPanel
 							.update(new ManagedImage(map, ManagedImage.IMG_JPEG));
 						s.setHeight(600);
 						s.setWidth(800);
 						s.show();
 						s.center();
 					}
 				});
 
 				vp.add(map);
 				
 				callback.isDrawn(cp);
 			}
 		});
 	}
 	
 	private void addSynonyms(final Taxon node, LayoutContainer data) {
 		data.add(new HTML("<hr><br />"));
 		data.add(new HTML("<b>Synonyms</b>"));
 		int size = node.getSynonyms().size();
 		if (size > 5)
 			size = 5;
 
 		for (final Synonym curSyn : node.getSynonyms()) {
 			size--;
 			if (size < 0)
 				break;
 			
 			HorizontalPanel hp = new HorizontalPanel();
 			
 			if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
 				final Image notesImage = new Image("images/icon-note.png");
 				if (curSyn.getNotes().isEmpty())
 					notesImage.setUrl("images/icon-note-grey.png");
 				notesImage.setTitle("Add/Remove Notes");
 				notesImage.addClickHandler(new ClickHandler() {
 					public void onClick(ClickEvent event) {
 						final NotesWindow window = new NotesWindow(new SynonymNoteAPI(taxon, curSyn));
 						window.show();
 					}
 				});
 				hp.add(notesImage);
 			}
 
 			String value = curSyn.toDisplayableString();
 			if (curSyn.getStatus().equals(Synonym.ADDED) || curSyn.getStatus().equals(Synonym.DELETED))
 				value += "-- " + curSyn.getStatus();
 
 			hp.add(new HTML("&nbsp;&nbsp;" + value));
 
 			data.add(hp);
 		}
 		
 		if (node.getSynonyms().size() > 5) {
 			HTML viewAll = new HTML("View all...");
 			viewAll.setStyleName("SIS_HyperlinkLookAlike");
 			viewAll.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					if (TaxonomyCache.impl.getCurrentTaxon() == null) {
 						Info.display(new InfoConfig("No Taxa Selected", "Please select a taxa first."));
 						return;
 					}
 
 					final Window s = WindowUtils.getWindow(false, false, "Synonyms");
 					s.setSize(400, 400);
 					LayoutContainer data = s;
 					data.setScrollMode(Scroll.AUTO);
 
 					VerticalPanel currentSynPanel = new VerticalPanel();
 					currentSynPanel.setSpacing(3);
 
 					HTML curHTML = new HTML("Current Synonyms");
 					curHTML.addStyleName("bold");
 					currentSynPanel.add(curHTML);
 
 					if (TaxonomyCache.impl.getCurrentTaxon().getSynonyms().size() == 0)
 						currentSynPanel.add(new HTML("There are no synonyms for this taxon."));
 
 					for (Synonym curSyn : TaxonomyCache.impl.getCurrentTaxon().getSynonyms()) {
 						curHTML = new HTML(curSyn.getFriendlyName());
 						currentSynPanel.add(curHTML);
 					}
 
 					data.add(currentSynPanel);
 					s.show();
 
 				}
 			});
 			data.add(viewAll);
 		}
 	}
 	
 	private Widget getCommonNameDisplay(CommonName curName, Taxon node) {
 		HorizontalPanel hp = new HorizontalPanel();
 		
 		String displayString = "&nbsp;&nbsp;" + curName.getName() ;
 		if (curName.getIso() != null) {
 			displayString += " -- " + curName.getLanguage();
 		}
 		
 		HTML html = new HTML(displayString);
 		if (curName.getChangeReason() == CommonName.DELETED) {
 			html.addStyleName("deleted");
 		} else {
 			if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
 				CommonNameToolPanel cntp = new CommonNameToolPanel(curName, node);
 				hp.add(cntp);
 			}
 		}
 		hp.add(html);
 		return hp;
 	}
 	
 	private void addCommonNames(final Taxon node, LayoutContainer data, int loop) {
 		final ArrayList<CommonName> commonNames = new ArrayList<CommonName>(node.getCommonNames());
 		Collections.sort(commonNames, new CommonNameComparator());
 		for (final CommonName curName : commonNames) {
 			loop--;
 			if (loop < 0)
 				break;			
 			data.add(getCommonNameDisplay(curName, node));
 		}
 
 		HTML viewAll = new HTML("View all...");
 		viewAll.setStyleName("SIS_HyperlinkLookAlike");
 		viewAll.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				final Window container = WindowUtils.getWindow(false, false, "Edit Common Names");
 				container.setScrollMode(Scroll.AUTO);
 
 				if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
 					Button item = new Button();
 					item.setText("New Common Name");
 					item.setIconStyle("icon-add");
 					item.addSelectionListener(new SelectionListener<ButtonEvent>() {
 						public void componentSelected(ButtonEvent ce) {
 							container.hide();
 							Window addNameBox = new EditCommonNamePanel(null, taxon, new GenericCallback<CommonName>() {
 								public void onSuccess(CommonName result) {
 									// TODO Auto-generated method stub
 								}
 								public void onFailure(Throwable caught) {
 								// TODO Auto-generated method stub
 								}
 							});
 							addNameBox.setSize(550, 250);
 							addNameBox.show();
 							addNameBox.center();
 						}
 					});
 
 					ToolBar tBar = new ToolBar();
 					tBar.add(item);
 
 					container.add(tBar);
 				}
 				
 				HTML commonNamesHeader = new HTML("<b>Common Name --- Language</b>");
 
 				LayoutContainer commonNamePanel = new LayoutContainer();
 				commonNamePanel.add(commonNamesHeader);
 
 				container.add(new HTML("<hr><br />"));
 				container.add(commonNamePanel);
 
 				if (commonNames.size() != 0) {
 					for (CommonName curName : commonNames) {
 						container.add(getCommonNameDisplay(curName, node));
 					}
 				} else
 					container.add(new HTML("No Common Names."));
 				container.addListener(Events.Hide, new Listener<WindowEvent>() {
 					public void handleEvent(WindowEvent be) {
 						update(taxon.getId());
 						
 					}
 				});
 				container.setSize(350, 550);
 				container.show();
 				container.center();
 
 			}
 		});
 		if (node.getCommonNames().size() > 5)
 			data.add(viewAll);
 	}
 
 	private void getGeneralInformationPanel(final Taxon node, final DrawsLazily.DoneDrawingCallbackWithParam<ContentPanel> callback) {
 		final ContentPanel generalInformation = new ContentPanel(new BorderLayout());
 		generalInformation.setLayoutOnChange(true);
 		generalInformation.setHeading("General Information");
 		generalInformation.setStyleName("x-panel");
 		generalInformation.setWidth(350);
 		// generalLayout.setSpacing(5);
 		
 		panelHeight = 180;
 		
 		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
 		doc.get(UriBase.getInstance().getImageBase() + "/images/" + node.getId(), new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				Debug.println("failed to fetch xml");
 				callback.isDrawn(generalInformation);
 			}
 			public void onSuccess(String result) {
 				taxonImage = null;
 				
 				NativeNodeList list = doc.getDocumentElement().getElementsByTagName("image");
 				for (int i = 0; i < list.getLength(); i++) {
 					boolean primary = ((NativeElement) list.item(i)).getAttribute("primary").equals("true");
 					if (primary) {
 						String ext = "";
 						if (((NativeElement) list.item(i)).getAttribute("encoding").equals("image/jpeg"))
 							ext = "jpg";
 						if (((NativeElement) list.item(i)).getAttribute("encoding").equals("image/gif"))
 							ext = "gif";
 						if (((NativeElement) list.item(i)).getAttribute("encoding").equals("image/png"))
 							ext = "png";
 
 						setImage(new Image(UriBase.getInstance().getSISBase() + "/raw/images/bin/"
 										+ ((NativeElement) list.item(i)).getAttribute("id") + "." + ext));
 					}
 				}
 				if (taxonImage == null) {
 					taxonImage = new Image("images/unavailable.png");
 					setImage(taxonImage);
 				}
 
 				VerticalPanel vp = new VerticalPanel();
 				vp.setSize("100px", "100px");
 
 				taxonImage.setWidth("100px");
 				taxonImage.setHeight("100px");
 				taxonImage.setStyleName("SIS_taxonSummaryHeader_image");
 				taxonImage.setTitle("Click for Image Viewer");
 				vp.add(taxonImage);
 				
 				// ADD GENERAL INFO
 				LayoutContainer data = new LayoutContainer();
 				data.setWidth(240);
 				if (!node.isDeprecated())
 					data.add(new HTML("Name: <i>" + node.getName() + "</i>"));
 				else
 					data.add(new HTML("Name: <s>" + node.getName() + "</s>"));
 				data.add(new HTML("&nbsp;&nbsp;Taxon ID: "
 						+ "<a target='_blank' href='http://www.iucnredlist.org/apps/redlist/details/" + node.getId()
 						+ "'>" + node.getId() + "</a>"));
 
 				if (node.getLevel() >= TaxonLevel.SPECIES) {
 					panelHeight += 10;
 					data.add(new HTML("Full Name:  <i>" + node.getFullName() + "</i>"));
 				}
 				data.add(new HTML("Level: " + node.getDisplayableLevel()));
 				if (node.getParentName() != null) {
 					panelHeight += 10;
 					HTML parentHTML = new HTML("Parent:  <i>" + node.getParentName() + "</i>"
 							+ "<img src=\"images/icon-tree.png\"></img>");
 					parentHTML.addStyleName("clickable");
 					parentHTML.addClickHandler(new ClickHandler() {
 						public void onClick(ClickEvent event) {
 							new TaxonTreePopup(node).show();
 						}
 					});
 					data.add(parentHTML);
 				}
 				if (node.getTaxonomicAuthority() != null && !node.getTaxonomicAuthority().equalsIgnoreCase("")) {
 					panelHeight += 20;
 					data.add(new HTML("Taxonomic Authority: " + node.getTaxonomicAuthority()));
 				}
 
 				data.add(new HTML("Status: " + node.getStatusCode()));
 				data.add(new HTML("Hybrid: " + node.getHybrid()));
 
 				// ADD SYNONYMS
 				if (!node.getSynonyms().isEmpty()) {
 					panelHeight += 150;
 					addSynonyms(node, data);
 				}
 
 				// ADD COMMON NAMES
 				Image addName = new Image("images/add.png");
 				addName.setSize("14px", "14px");
 				addName.setTitle("Add New Common Name");
 				addName.addClickHandler(new ClickHandler() {
 					public void onClick(ClickEvent event) {
 						Window addNameBox = new EditCommonNamePanel(null, taxon, 
 								new GenericCallback<CommonName>() {
 							public void onFailure(Throwable arg0) {
 							}
 							public void onSuccess(CommonName arg0) {
 								update(node.getId());
 							}
 						});
 						addNameBox.setSize(550, 250);
 						addNameBox.show();
 						addNameBox.center();
 					}
 				});
 		//
 				HTML commonNamesHeader = new HTML("<b>Common Name --- Language</b>");
 				
 				LayoutContainer commonNamePanel = new LayoutContainer();
 				if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node))
 					commonNamePanel.add(addName);
 				commonNamePanel.add(commonNamesHeader);
 				data.add(new HTML("<hr><br />"));
 				data.add(commonNamePanel);
 
 				if (!node.getCommonNames().isEmpty()) {
 					int loop = 5;
 					if (node.getCommonNames().size() < 5)
 						loop = node.getCommonNames().size();
 					panelHeight += loop * 15 + 20;
 					
 					addCommonNames(node, data, loop);
 				} else
 					data.add(new HTML("No Common Names."));
 
 				generalInformation.setHeight(panelHeight);
 				generalInformation.add(data, new BorderLayoutData(LayoutRegion.CENTER));
 				generalInformation.add(vp, new BorderLayoutData(LayoutRegion.WEST, 100));
 				
 				callback.isDrawn(generalInformation);
 			}
 		});
 	}
 
 	private ContentPanel getTaxonomicNotePanel(final Taxon node) {
 		final LayoutContainer vp = new LayoutContainer();
 		vp.setLayoutOnChange(true);
 		vp.setWidth((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2);
 		vp.setHeight(200);
 
 		if (node.getAssessments().isEmpty()) {
 			vp.add(new HTML("No Taxonomic Notes Available."));
 		} else {
 			List<Assessment> pubAssessments = new ArrayList<Assessment>(
 					TaxonomyCache.impl.getTaxon(node.getId()).getAssessments());
 			
 			Collections.sort(pubAssessments, new Comparator<Assessment>() {
 				public int compare(Assessment o1, Assessment o2) {
 					Date date2 = o2.getDateAssessed();
 					Date date1 = o1.getDateAssessed();
 
 					if (date2 == null)
 						return -1;
 					else if (date1 == null)
 						return 1;
 
 					int ret = date2.compareTo(date1);
 					if (ret == 0) {
 						if (o2.getIsHistorical())
 							ret = -1;
 						else
 							ret = 1;
 					}
 
 					return ret;
 				}
 			});
 			
 			Assessment curAssessment = pubAssessments.get(0);
 
 			String notes = (String) curAssessment.getPrimitiveValue(CanonicalNames.TaxonomicNotes, "value");
 			if (notes != null)
 				vp.add(new Html(XMLUtils.cleanFromXML(notes.replaceAll("<em>", "<i>").replaceAll("</em>",
 				"</i>"))));
 			else
 				vp.add(new Html("No Taxonomic Notes Available."));
 			vp.layout();
 		
 		}
 
 		ContentPanel cp = new ContentPanel();
 		cp.setHeading("Taxonomic Notes");
 		cp.setWidth((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2);
 		cp.setHeight(200);
 		cp.add(vp);
 		
 		return cp;
 	}
 
 	public void resize(int width, int height) {
 		onResize(width, height);
 	}
 
 	public void setImage(Image image) {
 		taxonImage = image;
 		taxonImage.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				if (imagePopup == null) {
 					imageManager = new ImageManagerPanel(String.valueOf(taxon.getId()));
 
 					imagePopup = WindowUtils.getWindow(false, false, "Photo Station");
 					imagePopup.add(imageManager);
 				}
 
 				imageManager.setTaxonId(String.valueOf(taxon.getId()));
 				imageManager.update();
 				imagePopup.setScrollMode(Scroll.AUTO);
 				imagePopup.show();
 				imagePopup.setSize(600, 330);
 				imagePopup.setPagePosition(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
 			}
 		});
 	}
 	
 	private static class SynonymNoteAPI implements NoteAPI {
 		
 		private final Synonym synonym;
 		private final Taxon taxon;
 		
 		public SynonymNoteAPI(Taxon taxon, Synonym synonym) {
 			this.synonym = synonym;
 			this.taxon = taxon;
 		}
 
 		@Override
 		public void addNote(final Notes note, final GenericCallback<Object> callback) {
 			note.setSynonym(synonym);
 			
 			final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
 			document.put(UriBase.getInstance().getNotesBase() + "/notes/synonym/" + synonym.getId(), note.toXML(), new GenericCallback<String>() {
 				public void onSuccess(String result) {
 					Notes newNote = Notes.fromXML(document.getDocumentElement());
 					
 					note.setEdits(newNote.getEdits());
 					note.setId(newNote.getId());
 					
 					synonym.getNotes().add(note);
 					
 					callback.onSuccess(result);
 				}public void onFailure(Throwable caught) {
 					callback.onFailure(caught);
 				}
 			});
 		}
 		
 		@Override
 		public void deleteNote(final Notes note, final GenericCallback<Object> callback) {
 			final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
 			document.delete(UriBase.getInstance().getNotesBase() + "/notes/note/" + note.getId(), new GenericCallback<String>() {
 				public void onSuccess(String result) {
 					synonym.getNotes().remove(note);
 					callback.onSuccess(result);
 				}
 				public void onFailure(Throwable caught) {
 					callback.onFailure(caught);
 				}
 			});
 		}
 		
 		@Override
 		public void loadNotes(ComplexListener<Collection<Notes>> listener) {
 			listener.handleEvent(synonym.getNotes());
 		}
 		
 		@Override
 		public void onClose() {
			//ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(taxon.getId());			
 		}
 		
 	}
 	
 	
 }
