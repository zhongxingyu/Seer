 package org.mindinformatics.gwt.domeo.client.ui.east.sets;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.mindinformatics.gwt.domeo.client.IDomeo;
 import org.mindinformatics.gwt.domeo.client.ui.sets.AnnotationSetLens;
 import org.mindinformatics.gwt.domeo.client.ui.sets.AnnotationSetSummaryListLens;
 import org.mindinformatics.gwt.domeo.model.MAnnotationSet;
 import org.mindinformatics.gwt.framework.component.IAnnotationRefreshableComponent;
 import org.mindinformatics.gwt.framework.component.IInitializableComponent;
 import org.mindinformatics.gwt.framework.component.IRefreshableComponent;
 import org.mindinformatics.gwt.framework.component.ui.east.ASidePanel;
 import org.mindinformatics.gwt.framework.component.ui.east.ASideTab;
 import org.mindinformatics.gwt.framework.component.ui.east.SidePanelsFacade;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * This panel displays the list of immediately available sets for the current
  * resource. Additional sets can be retrieved through an appropriate action.
  * 
  * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
  */
 public class AnnotationSetsSidePanel extends ASidePanel implements IInitializableComponent, 
 		IRefreshableComponent, IAnnotationRefreshableComponent{
 
 	interface Binder extends UiBinder<Widget, AnnotationSetsSidePanel> { }
 	private static final Binder binder = GWT.create(Binder.class);
 	
 	@UiField VerticalPanel body;
 	@UiField VerticalPanel toolbar;
 	@UiField ScrollPanel setsListScroller;
 	@UiField VerticalPanel setsList;
 	@UiField VerticalPanel info;
 	
 	private IDomeo _domeo;
 	private MAnnotationSet _setInfo;
 	private AnnotationSetLens _lensInfo;
 	private AnnotationSetsSidePanelTopbar topbar;
 	
 	private List<AnnotationSetSummaryListLens> lenses;
 	
 	public AnnotationSetsSidePanel(IDomeo domeo, SidePanelsFacade facade, ASideTab tab) {
 		super(domeo, facade, tab);
 		_domeo = domeo;
 		
 		initWidget(binder.createAndBindUi(this));
 		
 		topbar = new AnnotationSetsSidePanelTopbar(domeo, this);
 		toolbar.add(topbar);
 		toolbar.setCellHeight(topbar, "18px");
 		
 		body.setCellVerticalAlignment(info, HasVerticalAlignment.ALIGN_TOP);
 		body.setHeight("100%");
 	}
 	
 	@Override
 	public void init() {
 		_setInfo = null;
 		lenses = new ArrayList<AnnotationSetSummaryListLens>();
 		
 		info.clear();
 		topbar.init();
 	}
 	
 	@Override
 	public void refresh() {
 		refresh( _domeo.getAnnotationPersistenceManager().getAllUserSets());
 		topbar.refresh();
 	}
 	
 	/**
 	 * Summary panel refresh with a given list of annotation sets
 	 * @param sets	The list of sets to be displayed
 	 */
 	public void refresh(List<MAnnotationSet> sets) {
 		long start = System.currentTimeMillis();
 		try {
 			//if(_setInfo!=null && sets.contains(_setInfo)) {
 			//	displayAannotationSetInfo(_setInfo);
 			//} else {
 			//	info.clear();
 			//}
 	
 			// The following code, instead of rebuilding the panel every time,
 			// is refreshing the already existing items reducing dramatically
 			// the panel refresh time
 			int counter=0;
 			setsList.clear();
 			for(MAnnotationSet set: sets) {
 				AnnotationSetSummaryListLens lens = isLensAlreadyDisplayed(set);
 				if(lens==null) {
 					_application.getLogger().debug(this, "Creating lens for set " + set.getLocalId());
 					AnnotationSetSummaryListLens newLens = new AnnotationSetSummaryListLens((IDomeo) _application, this, set);
 					_application.getComponentsManager().registerObjectLens(set, newLens);
 					setsList.add(newLens);
 					lenses.add(newLens);
 				} else {
 					_application.getLogger().debug(this, "Refreshing lens of set " + set.getLocalId());
 					setsList.add(lens);
 					lens.refreshLens();
 				}
 				
 				counter++;
 			}
 			body.setCellHeight(setsListScroller, (counter*30)+"px");
 		} catch(Exception e) {
 			_application.getLogger().exception(this, "Refresh of the annotation sets side panel failed " + e.getMessage());
 		}
 		_application.getLogger().debug(this, "Annotation sets summary panel refreshed in (ms):" + (System.currentTimeMillis()-start));
 	}
 	
 	/**
 	 * Selects an annotation set
 	 * @param set The annotation set to be selected
 	 */
 	public void setSelectedAnnotaitonSet(MAnnotationSet set) {
 		((IDomeo) _application).getAnnotationPersistenceManager().setCurrentAnnotationSet(set);
 	}
 	
 	/**
 	 * Verifies if the set is selected
 	 * @param set	Anntoation set
 	 * @return Returns true if the set is selected
 	 */
 	public boolean isAnnotationSetSelected(MAnnotationSet set) {
 		if(_domeo.getAnnotationPersistenceManager().isCurrentSet()==false) return false;
 		return _domeo.getAnnotationPersistenceManager().getCurrentSet().getLocalId()==set.getLocalId();
 	}
 	
 	/**
 	 * Displays the info lens for the specified annotation set
 	 * @param set	The annotation set
 	 */
 	public void displayAannotationSetInfo(MAnnotationSet set) {
 		
 		// Unregister previous info lens
 		if(_setInfo!=null && _setInfo!=set) {
 			_application.getLogger().debug(this, "Unregistering lens for set " + set.getLocalId());
 			_application.getComponentsManager().unregisterObjectLens(_setInfo, _lensInfo);
 		}
 		
 		_setInfo = set;
 		info.clear();
 		
 		_lensInfo = new AnnotationSetLens((IDomeo) _application, this);
 		_application.getComponentsManager().registerObjectLens(set, _lensInfo);
 		_lensInfo.initialize(set);
 		info.add(_lensInfo);
 	}
 	
	public void resetAnnotationSetInfo() {
		info.clear();
	}
	
 	/**
 	 * Checks if the lens has been already cached.
 	 * @param set	The annotation set
 	 * @return	The lens for the set if one exists.
 	 */
 	private AnnotationSetSummaryListLens isLensAlreadyDisplayed(MAnnotationSet set) {
 		for(AnnotationSetSummaryListLens lens:lenses) {
 			if(lens.getSet().getLocalId()==set.getLocalId()) return lens;
 		}
 		return null;
 	}
 }
