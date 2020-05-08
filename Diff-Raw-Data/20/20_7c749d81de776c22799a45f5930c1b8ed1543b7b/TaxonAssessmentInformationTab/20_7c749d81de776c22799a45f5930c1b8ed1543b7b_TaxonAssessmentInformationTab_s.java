 package org.iucn.sis.client.panels.taxa;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.RegionCache;
 import org.iucn.sis.client.api.caches.SchemaCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.caches.WorkingSetCache;
 import org.iucn.sis.client.api.container.StateChangeEvent;
 import org.iucn.sis.client.api.container.StateManager;
 import org.iucn.sis.client.api.utils.FormattedDate;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentType;
 import org.iucn.sis.shared.api.models.Region;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.fields.RegionField;
 import org.iucn.sis.shared.api.utils.AssessmentFormatter;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.GridEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.store.GroupingStore;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.Component;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
 import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
 import com.extjs.gxt.ui.client.widget.grid.GroupingView;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 
 public class TaxonAssessmentInformationTab extends LayoutContainer implements DrawsLazily {
 	
 	public TaxonAssessmentInformationTab() {
 		super(new FillLayout());
 		setLayoutOnChange(true);
 	}
 	
 	@Override
 	public void draw(final DoneDrawingCallback callback) {
 		AssessmentCache.impl.fetchPartialAssessmentsForTaxon(TaxonomyCache.impl.getCurrentTaxon().getId(), new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				callback.isDrawn();
 			}
 			public void onSuccess(String result) {
 				removeAll();
 				
 				add(getAssessmentsPanel(TaxonomyCache.impl.getCurrentTaxon()));
 				
 				callback.isDrawn();
 			}
 		});
 	}
 	
 	private Component getAssessmentsPanel(final Taxon node) {
 		final GroupingStore<BaseModelData> store = new GroupingStore<BaseModelData>();
		store.groupBy("schema");
 		
 		for (Assessment data : AssessmentCache.impl.getPublishedAssessmentsForTaxon(node.getId(), null)) {
 			BaseModelData model = new BaseModelData();
 			model.set("date", data.getDateAssessed() == null ? "(Not set)" : FormattedDate.impl.getDate(data.getDateAssessed()));
 			model.set("category", AssessmentFormatter.getProperCategoryAbbreviation(data));
 			model.set("criteria", AssessmentFormatter.getProperCriteriaString(data));
 			model.set("status", "Published");
 			model.set("region", getRegions(data));
 			model.set("attachments", data.hasAttachments());
 			model.set("edit", "");
 			model.set("trash", "");
 			model.set("schema", data.getSchema(SchemaCache.impl.getDefaultSchema()));
 			model.set("id", data.getId());
 
 			store.add(model);
 		}
 
 		for (Assessment data : AssessmentCache.impl.getDraftAssessmentsForTaxon(node.getId(), null)) {
 			BaseModelData model = new BaseModelData();
 
 			if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ, data)) {
 				model.set("date", data.getDateAssessed() == null ? "(Not set)" : FormattedDate.impl.getDate(data.getDateAssessed()));
 				model.set("category", AssessmentFormatter.getProperCategoryAbbreviation(data));
 				model.set("criteria", AssessmentFormatter.getProperCriteriaString(data));
 				model.set("status", "Draft");
 				model.set("region", getRegions(data));
 				model.set("attachments", data.hasAttachments());
 				model.set("edit", "");
 				model.set("trash", "");
 				model.set("schema", data.getSchema(SchemaCache.impl.getDefaultSchema()));
 				model.set("id", data.getId());
 			}/* else {
 				model.set("date", "Sorry, you");
 				model.set("category", "do not have");
 				model.set("criteria", "permission");
 				model.set("status", "to view this");
 				model.set("region", "draft assessment.");
 				model.set("edit", "");
 				model.set("trash", "");
 				model.set("schema", data.getSchema(SchemaCache.impl.getDefaultSchema()));
 				model.set("id", data.getId());
 			}*/
 
 			store.add(model);
 		}
 		
 		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
 
 		columns.add(new ColumnConfig("date", "Assessment Date", 150));
 		columns.add(new ColumnConfig("category", "Category", 100));
 		columns.add(new ColumnConfig("criteria", "Criteria", 100));
 		columns.add(new ColumnConfig("status", "Status", 100));
 		columns.add(new ColumnConfig("region", "Region(s)", 150));
 		
 		ColumnConfig attachments = new ColumnConfig("attachments", "", 60);
 		attachments.setRenderer(new GridCellRenderer<BaseModelData>() {
 			@Override
 			public Object render(BaseModelData model, String property,
 					ColumnData config, int rowIndex, int colIndex,
 					ListStore<BaseModelData> store, Grid<BaseModelData> grid) {
 				boolean value = model.get(property);
 				if (!value)
 					return "";
 				
 				return "<img src=\"tango/status/mail-attachment.png\" alt=\"This assessment has attachments\" />";
 			}
 		});
 		columns.add(attachments);
 		
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
 		columns.add(new ColumnConfig("schema", "Schema", 100));
 
 		final GroupingView view = new GroupingView();
 		view.setShowGroupedColumn(false);
 		view.setGroupRenderer(new GridGroupRenderer() {
 			public String render(GroupColumnData data) {
				return SchemaCache.impl.getFromCache(data.group).getName();
 			}
 		});
 		
 		final Grid<BaseModelData> tbl = new Grid<BaseModelData>(store, new ColumnModel(columns));
 		tbl.setView(view);
 		tbl.setBorders(false);
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
 				if (column == 7) {
 					if (type == AssessmentType.PUBLISHED_ASSESSMENT_TYPE
 							&& !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser,
 									AuthorizableObject.DELETE, AssessmentCache.impl.getPublishedAssessment(id))) {
 						WindowUtils.errorAlert("Insufficient Permissions", "You do not have permission "
 								+ "to perform this operation.");
 					} else if (type == AssessmentType.DRAFT_ASSESSMENT_TYPE
 							&& !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser,
 									AuthorizableObject.DELETE, AssessmentCache.impl.getDraftAssessment(id))) {
 						WindowUtils.errorAlert("Insufficient Permissions", "You do not have permission "
 								+ "to perform this operation.");
 					} else {
 						WindowUtils.confirmAlert("Confirm Delete",
 								"Are you sure you want to delete this assessment?",
 								new WindowUtils.SimpleMessageBoxListener() {
 							public void onYes() {
 								/*if (AssessmentCache.impl.getCurrentAssessment() != null
 										&& AssessmentCache.impl.getCurrentAssessment().getId() == id
 										.intValue())
 									AssessmentCache.impl.resetCurrentAssessment();*/
 								NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
 								doc.delete(UriBase.getInstance().getSISBase() + "/assessments/" + type
 										+ "/" + id, new GenericCallback<String>() {
 									public void onFailure(Throwable arg0) {
 										WindowUtils.errorAlert("Could not delete, please try again later.");
 									}
 									public void onSuccess(String arg0) {
 										WorkingSetCache.impl.uncacheAssessmentsForWorkingSets();
 										
 										TaxonomyCache.impl.evict(String.valueOf(node.getId()));
 										TaxonomyCache.impl.fetchTaxon(node.getId(), true,
 												new GenericCallback<Taxon>() {
 											public void onFailure(Throwable caught) {
 											};
 											public void onSuccess(Taxon result) {
 												AssessmentCache.impl.clear();
 												//TaxonomyCache.impl.setCurrentTaxon(node);
 												StateChangeEvent event = new StateChangeEvent(StateManager.impl.getWorkingSet(), result, null, null);
 												event.setCanceled(false);
 												
 												StateManager.impl.reset(event);
 												//FIXME: panelManager.recentAssessmentsPanel.update();
 											};
 										});
 									}
 								});
 							}
 						});
 					}
 				} else if (column == 6) {
 					Assessment fetched = AssessmentCache.impl.getAssessment(id);
 					// CHANGE
 					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ,
 							fetched)) {
 						//AssessmentCache.impl.setCurrentAssessment(fetched);
 						StateManager.impl.setAssessment(fetched);
 						//ClientUIContainer.headerContainer.update();
 						/*ClientUIContainer.bodyContainer
 								.setSelection(ClientUIContainer.bodyContainer.tabManager.assessmentEditor);*/
 					} else {
 						WindowUtils.errorAlert("Sorry, you do not have permission to view this assessment.");
 					}
 				}
 			}
 
 		});
 
 		//ClientUIContainer.headerContainer.update();
 		
 		tbl.getStore().sort("date", SortDir.DESC);
 
 		final ContentPanel assessments = new ContentPanel(new FillLayout());
 		assessments.setHeading("Assessment List");
 		assessments.setStyleName("x-panel");
 		//assessments.setWidth(com.google.gwt.user.client.Window.getClientWidth() - 500);
 		//assessments.setHeight(panelHeight);
 		assessments.add(tbl);
 		
 		return tbl;
 	}
 	
 	private String getRegions(Assessment data) {
 		if (data.getField(CanonicalNames.RegionInformation) != null) {
 			RegionField proxy = new RegionField(data.getField(CanonicalNames.RegionInformation));
 			List<Region> regions = new ArrayList<Region>();
 			for (Integer id : proxy.getRegionIDs()) {
 				Region r = RegionCache.impl.getRegionByID(id);
 				if (r != null)
 					regions.add(r);
 			}
 			
 			if (regions.isEmpty())
 				return "Unset";
 			else
 				return RegionCache.impl.getRegionNamesAsReadable(regions);
 		}
 		else
 			return "N/A";
 	}
 
 }
