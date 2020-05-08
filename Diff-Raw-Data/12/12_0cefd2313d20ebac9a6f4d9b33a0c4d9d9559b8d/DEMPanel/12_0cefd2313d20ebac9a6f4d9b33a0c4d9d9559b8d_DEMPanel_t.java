 package org.iucn.sis.client.panels.dem;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.RegionCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.caches.ViewCache;
 import org.iucn.sis.client.api.container.StateManager;
 import org.iucn.sis.client.api.ui.views.ViewDisplay;
 import org.iucn.sis.client.api.ui.views.ViewDisplay.PageChangeRequest;
 import org.iucn.sis.client.api.utils.FormattedDate;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.client.panels.dem.DEMToolbar.EditStatus;
 import org.iucn.sis.client.tabs.FeaturedItemContainer;
 import org.iucn.sis.shared.api.acl.InsufficientRightsException;
 import org.iucn.sis.shared.api.acl.UserPreferences;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.CommonName;
 import org.iucn.sis.shared.api.models.Region;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.utils.CommonNameComparator;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.InfoConfig;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.TabPanel;
 import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.events.SimpleListener;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 import com.solertium.util.gwt.ui.StyledHTML;
 
 /**
  * Shows an assessment in the following steps:
  * 
  * 1) Based on the view and page selected, determines the displays needed by
  * canonical name.
  * 
  * 2) Fetches the Widget from the FieldWidgetCache and the current assessment
  * 
  * 3) Clears the contents of the Widgets and fills them with data from the
  * current assessment, if applicable
  * 
  * @author adam.schwartz
  * 
  */
 public class DEMPanel extends FeaturedItemContainer<Integer> {
 
 	private boolean viewOnly = false;
 	private LayoutContainer scroller;
 
 	private AccordionLayout viewChooser;
 	private ViewDisplay viewWrapper;
 
 	private DEMToolbar toolBar;
 
 	public DEMPanel() {
 		viewChooser = new AccordionLayout();
 		
 		viewWrapper = new ViewDisplay();
 		viewWrapper.setLayout(viewChooser);
 		viewWrapper.setLayoutOnChange(true);
 		viewWrapper.setPageChangelistener(new ComplexListener<PageChangeRequest>() {
 			public void handleEvent(PageChangeRequest eventData) {
 				changePage(eventData);
 			}
 		});
 
 		scroller = new LayoutContainer();
 		scroller.setLayout(new FitLayout());
 		scroller.setScrollMode(Scroll.NONE);
 
 		toolBar = buildToolBar();
 		
 		bodyContainer.addStyleName("page_assessment_body");
 	}
 	
 	@Override
 	protected void drawBody(DoneDrawingCallback callback) {
 		BorderLayoutData toolBarData = new BorderLayoutData(LayoutRegion.NORTH);
 		toolBarData.setSize(25);
 		
 		BorderLayoutData scrollerData = new BorderLayoutData(LayoutRegion.CENTER, .82f, 300, 3000);
 		
 		final LayoutContainer container = new LayoutContainer(new BorderLayout());
 		container.add(toolBar, toolBarData);
 		container.add(scroller, scrollerData);
 		
 		bodyContainer.removeAll();
 		bodyContainer.add(container);
 		
 		/*
 		 * TODO: check to see if a page should be 
 		 * pre-loaded 
 		 */
 		
 		callback.isDrawn();
 	}
 	
 	@Override
 	protected void drawOptions(final DrawsLazily.DoneDrawingCallback callback) {
 		viewWrapper.draw(new DrawsLazily.DoneDrawingCallback() {
 			public void isDrawn() {
 				optionsContainer.removeAll();	
 				optionsContainer.add(viewWrapper);
 				
 				callback.isDrawn();	
 			}
 		});
 	}
 	
 	@Override
 	protected LayoutContainer updateFeature() {
 		final Assessment item = AssessmentCache.impl.getAssessment(getSelectedItem());
 		final Taxon taxon = StateManager.impl.getTaxon();
 		
 		String abbreviation = "";
 		try {
 			abbreviation = " (" + item.getCategoryAbbreviation() + ")";
 		} catch (Exception e) {
 			Debug.println(e);
 		}
 		
 		final StyledHTML speciesName = 
 			new StyledHTML("<center>" + item.getSpeciesName() + abbreviation + "</center>", "page_assessment_featured_header;clickable");
 		speciesName.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				AssessmentClientSaveUtils.saveIfNecessary(new SimpleListener() {
 					public void handleEvent() {
 						StateManager.impl.setAssessment(null);
 					}
 				});
 			}
 		});
 		
 		final LayoutContainer container = new LayoutContainer();
 		container.add(speciesName);
 		
 		CommonName cn = null;
 		final List<CommonName> cns = new ArrayList<CommonName>(taxon.getCommonNames());
 		Collections.sort(cns, new CommonNameComparator());
 		if (!cns.isEmpty())
 			cn = cns.get(0);
 		
 		if (cn != null)
 			container.add(new StyledHTML("(" + cn.getName() + ")", "page_assessment_featured_content_common_name"));
 		
 		List<Region> regions = new ArrayList<Region>();
 		for (Integer id : item.getRegionIDs()) {
 			Region region = RegionCache.impl.getRegionByID(id);
 			if (region != null)
 				regions.add(region);
 		}
 		
 		container.add(createSpacer(20));
 		
 		FlexTable stats = new FlexTable();
 		stats.setCellSpacing(4);
 				
 		int row = 0;
 		
 		stats.setWidget(row, 0, new StyledHTML("Status: ", "page_assessment_featured_prompt"));
 		stats.setWidget(row, 1, new StyledHTML(item.getAssessmentType().getDisplayName(true), "page_assessment_featured_content"));
 		
 		//U/T assessments aren't regional...
 		if (!regions.isEmpty()) {
 			stats.setWidget(++row, 0, new StyledHTML("Region(s): ", "page_assessment_featured_prompt"));
 			stats.setWidget(row, 1, new StyledHTML(RegionCache.impl.getRegionNamesAsReadable(regions), "page_assessment_featured_content"));
 		}
 		
 		if (item.getLastEdit() != null) {
 			stats.setWidget(++row, 0, new StyledHTML("Last Modified: ", "page_assessment_featured_prompt"));
 			stats.setWidget(row, 1, new StyledHTML(FormattedDate.FULL.getDate(item.getLastEdit().getCreatedDate()) + 
 					" by " + item.getLastEdit().getUser().getDisplayableName(), "page_assessment_featured_content"));
 		}
 		
 		container.add(stats);
 		
 		return container;
 	}
 	
 	@Override
 	protected void updateSelection(final Integer selection) {
 		AssessmentClientSaveUtils.saveIfNecessary(new SimpleListener() {
 			public void handleEvent() {
 				AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(selection, null), new GenericCallback<String>() {
 					public void onSuccess(String result) {
						final Assessment assessment = AssessmentCache.impl.getAssessment(selection);
						TaxonomyCache.impl.fetchTaxon(assessment.getTaxon().getId(), new GenericCallback<Taxon>() {
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Failed to load the taxon for this assessment.");
							}
							public void onSuccess(Taxon result) {
								StateManager.impl.setState(result, assessment);
							}
						});
 					}
 					public void onFailure(Throwable caught) {
 						WindowUtils.errorAlert("Error loading next assessment");
 					}
 				});
 			}
 		});
 	}
 
 	private DEMToolbar buildToolBar() {
 		DEMToolbar toolbar = new DEMToolbar();
 		toolbar.setRefreshListener(new ComplexListener<EditStatus>() {
 			public void handleEvent(EditStatus eventData) {
 				viewOnly = EditStatus.EDIT_DATA.equals(eventData);
 				redraw();
 			}
 		});
 		toolbar.setSaveListener(new SimpleListener() {
 			public void handleEvent() {
 				drawFeatureArea();
 			}
 		});
 		toolbar.build();
 		return toolbar;
 	}
 	
 	public void redraw() {
 		if (AssessmentCache.impl.getCurrentAssessment() == null) {
 			Info.display(new InfoConfig("No Assessment", "Please select an assessment first."));
 			return;
 		}
 		
 		viewWrapper.draw(new DrawsLazily.DoneDrawingCallback() {
 			public void isDrawn() {
 				final HTML label = viewWrapper.getLastSelected();
 				if (label != null) {
 					//If the user had a view in play, we want to 
 					//show it again.
 					
 					/*SISView currentView = ViewCache.impl.getCurrentView();
 					SISPageHolder page = currentView.getCurPage();
 					
 					changePage(
 						new PageChangeRequest(currentView, page, label));*/
 				}
 				else
 					clearDEM();
 				
 				layout();
 			}
 		});
 	}
 	
 
 	public void clearDEM() {
 		if (ViewCache.impl.getCurrentView() != null && ViewCache.impl.getCurrentView().getCurPage() != null)
 			ViewCache.impl.getCurrentView().getCurPage().removeMyFields();
 
 		scroller.removeAll();
 	}
 	
 	private void changePage(final ViewDisplay.PageChangeRequest request) {
 		onBeforePageChange(request, new SimpleListener() {
 			public void handleEvent() {
 				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, AssessmentCache.impl
 						.getCurrentAssessment())) {
 					Info.display("Insufficient Permissions", "NOTICE: You do not have "
 							+ "permission to save modifications to this assessment.");
 					viewOnly = true;
 				}
 				
 				clearDEM();
 				
 				toolBar.setViewOnly(viewOnly);
 				toolBar.resetAutosaveTimer();
 				
 				scroller.add(new HTML("Loading..."));
 				
 				updateBoldedPage(request.getLabel());
 				
 				DeferredCommand.addPause();
 				DeferredCommand.addCommand(new Command() {
 					public void execute() {
 						int page = request.getView().getPages().indexOf(request.getPage());
 						
 						ViewCache.impl.showPage(request.getView().getId(), page, viewOnly, new DrawsLazily.DoneDrawingCallbackWithParam<TabPanel>() {
 							public void isDrawn(TabPanel parameter) {
 								scroller.removeAll();
 								scroller.add(parameter);
 								scroller.layout();
 							}	
 						});
 					}
 				});
 			}
 		});
 	}
 
 	private void onBeforePageChange(final PageChangeRequest request, final SimpleListener listener) {
 		if (AssessmentCache.impl.getCurrentAssessment() == null) {
 			Info.display(new InfoConfig("No Assessment", "Please select an assessment first."));
 			return;
 		}
 
 		boolean samePage = false;
 		try {
 			samePage = ViewCache.impl.getCurrentView().getCurPage().equals(request.getPage());
 		} catch (Exception somethingsNull) {
 		}
 		
 		if (samePage)
 			return;
 		
 		boolean hasEditablePageWithFields = 
 			!viewOnly && ViewCache.impl.getCurrentView() != null && ViewCache.impl.getCurrentView().getCurPage() != null;
 		
 		if (hasEditablePageWithFields && AssessmentClientSaveUtils.shouldSaveCurrentAssessment(
 				ViewCache.impl.getCurrentView().getCurPage().getMyFields())) {
 			stopAutosaveTimer();
 
 			String savePreference = 
 				SimpleSISClient.currentUser.getPreference(UserPreferences.AUTO_SAVE, UserPreferences.PROMPT);
 			
 			if (savePreference.equals(UserPreferences.DO_ACTION))
 				doSaveCurrentAssessment(listener);
 			else if (savePreference.equals(UserPreferences.IGNORE)) {
 				listener.handleEvent();
 			}
 			else {
 				WindowUtils.confirmAlert("By the way...", "Navigating away from this page will"
 						+ " revert unsaved changes. Would you like to save?", new WindowUtils.SimpleMessageBoxListener() {
 					public void onYes() {
 						toolBar.startAutosaveTimer();
 
 						WindowUtils.showLoadingAlert("Saving assessment...");
 						doSaveCurrentAssessment(listener);
 					}
 					public void onNo() {
 						listener.handleEvent();		
 					}
 				});
 			}
 		} else {
 			listener.handleEvent();
 		}
 	}
 
 	private void doSaveCurrentAssessment(final SimpleListener callback) {
 		try {
 			AssessmentClientSaveUtils.saveAssessment(ViewCache.impl.getCurrentView().getCurPage().getMyFields(), 
 					AssessmentCache.impl.getCurrentAssessment(), new GenericCallback<Object>() {
 				public void onFailure(Throwable arg0) {
 					WindowUtils.hideLoadingAlert();
 					WindowUtils.errorAlert("Save Failed", "Failed to save assessment! " + arg0.getMessage());
 				}
 
 				public void onSuccess(Object arg0) {
 					WindowUtils.hideLoadingAlert();
 					Info.display("Save Complete", "Successfully saved assessment {0}.", AssessmentCache.impl
 							.getCurrentAssessment().getSpeciesName());
 
 					callback.handleEvent();
 				}
 			});
 		} catch (InsufficientRightsException e) {
 			WindowUtils.errorAlert("Auto-save failed. You do not have sufficient " + "rights to perform this action.");
 		}
 	}
 
 	public void stopAutosaveTimer() {
 		toolBar.stopAutosaveTimer();
 	}
 
 	protected void onDetach() {
 		stopAutosaveTimer();
 		super.onDetach();
 	}
 
 	protected void onHide() {
 		stopAutosaveTimer();
 		super.onHide();
 	}
 
 	private void updateBoldedPage(final HTML curPageLabel) {
 		viewWrapper.selectPage(curPageLabel);
 		viewWrapper.layout();
 	}
 	
 	/*public void updateWorkflowStatus() {
 		try {
 			if (!WorkflowStatus.DRAFT.equals(WorkingSetCache.impl.getCurrentWorkingSet().getWorkflowStatus()))
 				workflowStatus.setText("Submission Status: " + 
 					WorkingSetCache.impl.getCurrentWorkingSet().getWorkflowStatus() 
 				);
 		} catch (Exception e) {
 			workflowStatus.setText("");
 		}
 	}*/
 }
 
