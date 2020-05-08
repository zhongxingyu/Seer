 package org.mindinformatics.gwt.domeo.client.ui.east.annotation;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.mindinformatics.gwt.domeo.client.IDomeo;
 import org.mindinformatics.gwt.domeo.client.ui.annotation.actions.IAnnotationEditListener;
 import org.mindinformatics.gwt.domeo.client.ui.annotation.tiles.ITileComponent;
 import org.mindinformatics.gwt.domeo.model.MAnnotation;
 import org.mindinformatics.gwt.domeo.model.MAnnotationCitationReference;
 import org.mindinformatics.gwt.domeo.plugins.annotation.comment.model.MCommentAnnotation;
 import org.mindinformatics.gwt.domeo.plugins.annotation.commentaries.linear.model.MLinearCommentAnnotation;
 import org.mindinformatics.gwt.domeo.plugins.annotation.curation.model.MCurationToken;
 import org.mindinformatics.gwt.framework.component.IInitializableComponent;
 import org.mindinformatics.gwt.framework.component.IRefreshableComponent;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
  */
 public class AnnotationSummaryTable extends Composite 
	implements IInitializableComponent, IRefreshableComponent{
 
 	private IDomeo _domeo;
 	private IAnnotationEditListener _listener;
 	
 	@UiField ScrollPanel body;
 	@UiField VerticalPanel content;
 	
 	interface Binder extends UiBinder<Widget, AnnotationSummaryTable> { }
 	private static final Binder binder = GWT.create(Binder.class);
 	
 	public static final AnnotationSummaryTableResources localResources = 
 		GWT.create(AnnotationSummaryTableResources.class);
 	
 	public AnnotationSummaryTable(IDomeo domeo, IAnnotationEditListener listener) {
 		_domeo = domeo;
 		_listener = listener;
 		
 		initWidget(binder.createAndBindUi(this));
 	}
 	
 	@Override
 	public void init() {		
 		content.clear();
 		
 		VerticalPanel vp = new VerticalPanel();
 		vp.add(new HTML("No Annotation has been created yet."));
 		content.add(vp);
 	}
 	
 	@Override
 	public void refresh() {
 		refreshPanel(_domeo.getAnnotationPersistenceManager().getAllAnnotations());
 	}
 	
 	public void refreshPanel(List<MAnnotation> annotations) {
 		long start = System.currentTimeMillis();
 		try {
 			content.clear();
 			if(annotations!=null && annotations.size()>0) {
 				Collections.sort(annotations, new SortByVerticalPostion());
 				for(MAnnotation ann: annotations) {
 					if(ann instanceof MLinearCommentAnnotation || ann instanceof MCommentAnnotation || ann instanceof MCurationToken || ann instanceof MAnnotationCitationReference) continue; 
 					ITileComponent c = _domeo.getAnnotationTailsManager().getAnnotationTile(ann.getClass().getName(), _listener);
 					if(c==null) {
 						VerticalPanel vp = new VerticalPanel();
 						vp.add(new Label(ann.getLocalId() + " - " + ann.getClass().getName() + " - " + ann.getY()));
 						content.add(vp);
 					} else {
 						try {
 							c.initializeLens(ann);
 							content.add(c.getTile());
 						} catch(Exception e) {
 							// If something goes wrong just display the default tile
 							_domeo.getLogger().exception(this, "Refreshing tile: " + e.getMessage());
 							VerticalPanel vp = new VerticalPanel();
 							vp.add(new Label(ann.getLocalId() + " - " + ann.getClass().getName() + " - " + ann.getY()));
 							content.add(vp);
 						}
 					}
 				}
 			}
 		} catch(Exception e) {
 			_domeo.getLogger().exception(this, e.getMessage());
 		}
 		_domeo.getLogger().debug(this, "Annotation summary panel with search term refreshed in (ms):" + (System.currentTimeMillis()-start));
 	}
 
 	public class SortByVerticalPostion implements Comparator<MAnnotation>{
 	    public int compare(MAnnotation o1, MAnnotation o2) {
 	        return o1.getY() - o2.getY();
 	    }
 	}
 }
