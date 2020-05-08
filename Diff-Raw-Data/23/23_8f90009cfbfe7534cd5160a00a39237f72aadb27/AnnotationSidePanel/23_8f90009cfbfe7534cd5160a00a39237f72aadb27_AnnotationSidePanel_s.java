 package org.mindinformatics.gwt.domeo.client.ui.east.annotation;
 
 import java.util.List;
 
 import org.mindinformatics.gwt.domeo.client.IDomeo;
 import org.mindinformatics.gwt.domeo.model.MAnnotation;
 import org.mindinformatics.gwt.framework.component.IAnnotationRefreshableComponent;
 import org.mindinformatics.gwt.framework.component.IInitializableComponent;
 import org.mindinformatics.gwt.framework.component.IRefreshableComponent;
 import org.mindinformatics.gwt.framework.component.ui.east.ASidePanel;
 import org.mindinformatics.gwt.framework.component.ui.east.ASideTab;
 import org.mindinformatics.gwt.framework.component.ui.east.SidePanelsFacade;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
  */
 public class AnnotationSidePanel extends ASidePanel 
 	implements IInitializableComponent, IRefreshableComponent, 
		IAnnotationRefreshableComponent {
 
 	interface Binder extends UiBinder<Widget, AnnotationSidePanel> { }
 	private static final Binder binder = GWT.create(Binder.class);
 	
 	@UiField VerticalPanel body;
 	
 	private AnnotationSidePanelTopbar topbar;
 	private AnnotationSummaryTable table;
 
 	public AnnotationSidePanel(IDomeo domeo, SidePanelsFacade facade, ASideTab tab) {
 		super(domeo, facade, tab);
 		
 		initWidget(binder.createAndBindUi(this));
 		topbar = new AnnotationSidePanelTopbar(domeo, this);
 		body.add(topbar);
 		body.setCellHeight(topbar, "18px");
 		
 		table = new AnnotationSummaryTable(domeo, domeo.getContentPanel().getAnnotationFrameWrapper());
 		table.init();
 		body.setHeight("100%");
 		body.add(table);
 	}
 	
 	@Override
 	public void init() {
 		_application.getLogger().debug(this, "Initializing...");
 		topbar.init();
 		table.init();
 	}
 	
 	@Override
 	public void refresh() {
 		_application.getLogger().debug(this, "Refreshing...");
 		topbar.refresh();
 		table.refresh();
 	}
 	
 	/**
 	 * Performs a refresh with the provided list of annotation items.
 	 * @param annotations	The annotations to display
 	 */
 	public void refresh(List<MAnnotation> annotations) {
 		table.refreshPanel(annotations);
 	}
 }
