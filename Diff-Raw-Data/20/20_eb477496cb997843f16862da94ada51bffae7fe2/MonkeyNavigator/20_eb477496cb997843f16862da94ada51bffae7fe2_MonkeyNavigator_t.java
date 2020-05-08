 package org.iucn.sis.client.panels;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.caches.WorkingSetCache;
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.container.StateManager;
 import org.iucn.sis.client.panels.AssessmentMonkeyNavigatorPanel.AssessmentGroupedComparator;
 import org.iucn.sis.client.panels.WorkingSetMonkeyNavigatorPanel.WorkingSetNavigationComparator;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.WorkingSet;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 
 public class MonkeyNavigator extends LayoutContainer implements DrawsLazily {
 	
 	private final WorkingSetMonkeyNavigatorPanel workingSetContainer;
 	private final TaxonMonkeyNavigatorPanel taxonContainer;
 	private final AssessmentMonkeyNavigatorPanel assessmentContainer;
 	
 	private WorkingSet curNavWorkingSet;
 	private Taxon curNavTaxon;
 	private Assessment curNavAssessment;
 	
 	private boolean isDrawn;
 	
 	public MonkeyNavigator() {
 		super();
 		setBorders(false);
 		setStyleName("navigator");
 		setLayout(new FillLayout());
 		setLayoutOnChange(true);
 		
 		isDrawn = false;
 		
 		workingSetContainer = new WorkingSetMonkeyNavigatorPanel();
 		taxonContainer = new TaxonMonkeyNavigatorPanel();
 		assessmentContainer = new AssessmentMonkeyNavigatorPanel();
 	}
 	
 	
 	public void draw(final DrawsLazily.DoneDrawingCallback callback) {
 		curNavWorkingSet = WorkingSetCache.impl.getCurrentWorkingSet();
 		curNavTaxon = TaxonomyCache.impl.getCurrentTaxon();
 		curNavAssessment = AssessmentCache.impl.getCurrentAssessment();
 		
 		workingSetContainer.refresh(curNavWorkingSet);
 		taxonContainer.refresh(curNavWorkingSet, curNavTaxon);
 		assessmentContainer.refresh(curNavWorkingSet, curNavTaxon, curNavAssessment);
 		
 		if (!isDrawn) {
 			addListeners();
 						
 			final LayoutContainer container = new LayoutContainer(new BorderLayout());
 			container.add(workingSetContainer, new BorderLayoutData(LayoutRegion.WEST, .30f, 5, 4000));
 			container.add(taxonContainer, new BorderLayoutData(LayoutRegion.CENTER, .37f, 5, 4000));
 			container.add(assessmentContainer, new BorderLayoutData(LayoutRegion.EAST, .33f, 5, 4000));
 						
 			removeAll();
 			add(container);
 		}
 						
 		isDrawn = true;
 						
 		callback.isDrawn();
 	}
 	
 	private void addListeners() {	
 		workingSetContainer.addListener(Events.SelectionChange, new Listener<NavigationChangeEvent<WorkingSet>>() {
 			public void handleEvent(NavigationChangeEvent<WorkingSet> be) {
 				curNavWorkingSet = be.getModel();
 				curNavTaxon = null;
 				curNavAssessment = null;
 				
 				taxonContainer.refresh(curNavWorkingSet, curNavTaxon);
 				assessmentContainer.refresh(curNavWorkingSet, curNavTaxon, curNavAssessment);
 			}
 		});
 		
 		taxonContainer.addListener(Events.SelectionChange, new Listener<NavigationChangeEvent<Taxon>>() {
 			public void handleEvent(NavigationChangeEvent<Taxon> be) {
 				if (be.getModel() == null)
 					return;
 				
 				curNavTaxon = be.getModel();
 				curNavAssessment = null;
 				
 				assessmentContainer.refresh(curNavWorkingSet, curNavTaxon, curNavAssessment);
 			}
 		});	
 	}
 	
 	public void refreshWorkingSetView() {
		workingSetContainer.refreshView(false);
 	}
 	
 	public void refreshTaxonView() {
		taxonContainer.refreshView(false);
 	}
 	
 	public void refreshAssessmentView() {
		assessmentContainer.refreshView(false);
 	}
 	
 	public static void getSortedWorkingSets(ComplexListener<List<WorkingSet>> callback) {
 		final List<WorkingSet> ws = new ArrayList<WorkingSet>(WorkingSetCache.impl.getWorkingSets().values());
 		Collections.sort(ws, new WorkingSetNavigationComparator(SISClientBase.currentUser.getId()));
 		
 		callback.handleEvent(ws);
 	}
 	
 	public static void getSortedWorkingSetIDs(final ComplexListener<List<Integer>> callback) {
 		getSortedWorkingSets(new ComplexListener<List<WorkingSet>>() {
 			public void handleEvent(List<WorkingSet> eventData) {
 				final List<Integer> list = new ArrayList<Integer>();
 				for (WorkingSet set : eventData)
 					list.add(set.getId());
 				
 				callback.handleEvent(list);
 			}
 		});
 	}
 	
 	public static void getSortedTaxa(final WorkingSet ws, final ComplexListener<List<Taxon>> callback) {
 		if (ws == null) {
 			callback.handleEvent(new ArrayList<Taxon>(TaxonomyCache.impl.getRecentlyAccessed()));
 		}
 		else {
 			WorkingSetCache.impl.fetchTaxaForWorkingSet(ws.getId(), new GenericCallback<List<Taxon>>() {
 				public void onSuccess(List<Taxon> result) {
 					Collections.sort(result, new TaxonMonkeyNavigatorPanel.TaxonComparator());
 					
 					callback.handleEvent(result);
 				}
 				public void onFailure(Throwable caught) {
 					WindowUtils.hideLoadingAlert();
 					WindowUtils.errorAlert("Error loading taxa for this working set.");
 					
 					callback.handleEvent(new ArrayList<Taxon>());
 				}
 			});
 		}
 	}
 	
 	public static void getSortedTaxaIDs(final WorkingSet ws, final ComplexListener<List<Integer>> callback) {
 		getSortedTaxa(ws, new ComplexListener<List<Taxon>>() {
 			public void handleEvent(List<Taxon> eventData) {
 				final List<Integer> list = new ArrayList<Integer>();
 				for (Taxon set : eventData)
 					list.add(set.getId());
 				
 				callback.handleEvent(list);
 			}
 		});
 	}
 	
 	public static void getSortedAssessments(final ComplexListener<List<Assessment>> callback) {
 		final WorkingSet workingSet = StateManager.impl.getWorkingSet();
 		final Taxon taxon = StateManager.impl.getTaxon();
 		
 		if (taxon == null)
 			callback.handleEvent(new ArrayList<Assessment>());
 		else if (workingSet == null) {
 			AssessmentCache.impl.fetchPartialAssessmentsForTaxon(taxon.getId(), new GenericCallback<String>() {
 				public void onFailure(Throwable caught) {
 					WindowUtils.errorAlert("Could not load assessments for this taxon.");
 					
 					callback.handleEvent(new ArrayList<Assessment>());
 				}
 				public void onSuccess(String result) {
 					/*
 					 * TODO: this can be improved, but the API needs to get a little better...
 					 */
 					final List<Assessment> list = new ArrayList<Assessment>();
 					final List<Assessment> drafts = 
 						new ArrayList<Assessment>(AssessmentCache.impl.getDraftAssessmentsForTaxon(taxon.getId()));
 					Collections.sort(drafts, new AssessmentGroupedComparator());
 					
 					for (Assessment current : drafts)
 						list.add(current);
 					
 					final List<Assessment> published = 
 						new ArrayList<Assessment>(AssessmentCache.impl.getPublishedAssessmentsForTaxon(taxon.getId()));
 					Collections.sort(published, new AssessmentGroupedComparator());
 					
 					for (Assessment current : published)
 						list.add(current);
 					
 					callback.handleEvent(list);
 				}
 			});
 		}
 		else {
 			WorkingSetCache.impl.getAssessmentsForWorkingSet(workingSet, taxon, new GenericCallback<List<Assessment>>() {
 				public void onSuccess(List<Assessment> result) {
 					Collections.sort(result, new AssessmentGroupedComparator());
 					
 					callback.handleEvent(result);
 				}
 				public void onFailure(Throwable caught) {
 					WindowUtils.errorAlert("Could not load assessments for the taxon in this working set.");
 				}
 			});
 		}
 	}
 	
 	public static void getSortedAssessmentIDs(final ComplexListener<List<Integer>> callback) {
 		getSortedAssessments(new ComplexListener<List<Assessment>>() {
 			public void handleEvent(List<Assessment> eventData) {
 				List<Integer> list = new ArrayList<Integer>();
 				for (Assessment current : eventData)
 					list.add(current.getId());
 				
 				callback.handleEvent(list);
 			}
 		});
 	}
 	
 	public static class NavigationChangeEvent<T> extends BaseEvent {
 	
 		private T model;
 		
 		public NavigationChangeEvent(T model) {
 			super(null);
 			this.model = model;
 		}
 		
 		public T getModel() {
 			return model;
 		}
 		
 	}
 
 }
