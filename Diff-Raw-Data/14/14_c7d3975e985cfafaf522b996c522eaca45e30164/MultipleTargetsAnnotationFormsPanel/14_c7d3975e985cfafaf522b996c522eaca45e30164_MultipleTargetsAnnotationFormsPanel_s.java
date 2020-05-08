 package org.mindinformatics.gwt.domeo.client.ui.annotation.forms.multipletargets;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.mindinformatics.gwt.domeo.client.IDomeo;
 import org.mindinformatics.gwt.domeo.client.ui.annotation.actions.IAnnotationEditListener;
 import org.mindinformatics.gwt.domeo.client.ui.annotation.forms.AFormComponent;
 import org.mindinformatics.gwt.domeo.client.ui.annotation.forms.AFormsManager;
 import org.mindinformatics.gwt.domeo.client.ui.annotation.forms.IAllowsMultipleTargets;
 import org.mindinformatics.gwt.domeo.client.ui.annotation.forms.IFormGenerator;
 import org.mindinformatics.gwt.domeo.client.ui.annotation.tiles.ITileComponent;
 import org.mindinformatics.gwt.domeo.client.ui.east.annotation.AnnotationSummaryTable;
 import org.mindinformatics.gwt.domeo.client.ui.lenses.selectors.TPrefixSuffixTextSelectorTile;
 import org.mindinformatics.gwt.domeo.model.MAnnotation;
 import org.mindinformatics.gwt.domeo.model.selectors.MSelector;
 import org.mindinformatics.gwt.domeo.plugins.annotation.selection.ui.tile.TSelectionTile;
 import org.mindinformatics.gwt.framework.component.IAnnotationRefreshableComponent;
 import org.mindinformatics.gwt.framework.component.IRefreshableComponent;
 import org.mindinformatics.gwt.framework.component.resources.model.MGenericResource;
 import org.mindinformatics.gwt.framework.src.IContainerPanel;
 import org.mindinformatics.gwt.framework.src.IContentPanel;
 import org.mindinformatics.gwt.framework.src.IResizable;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.SpanElement;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TabLayoutPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
  */
 public class MultipleTargetsAnnotationFormsPanel extends AFormsManager implements IContentPanel,
 		IAnnotationEditListener, IRefreshableComponent, IAnnotationRefreshableComponent, IResizable {
 
 	private static final String TITLE = "Multiple Target Annotation Creation";
 	private static final String TITLE_EDIT = "Multiple Target Annotation Editing";
 	
 	interface Binder extends UiBinder<HorizontalPanel, MultipleTargetsAnnotationFormsPanel> { }
 	private static final Binder binder = GWT.create(Binder.class);
 	
 	private IDomeo _domeo;
 	private IContainerPanel _containerPanel;
 	private String _title;
 	private Element _element;
 	
 	private List<AFormComponent> forms = new ArrayList<AFormComponent>();
 	
 	@UiField FlowPanel main;
 	@UiField VerticalPanel targetsPanel;
 	@UiField TabLayoutPanel tabToolsPanel;
 	@UiField SpanElement footerSpan;
 	
 	private MAnnotation _annotation;
 	private ArrayList<MAnnotation> _targets;
 	
 	public MultipleTargetsAnnotationFormsPanel(IDomeo domeo) {
 		_domeo = domeo;
 		
 		initWidget(binder.createAndBindUi(this));
 	}
 	
 
 	// ------------------------------------------------------------------------
 	//  CREATION OF ANNOTATIONS OF VARIOUS KIND
 	// ------------------------------------------------------------------------
 	public MultipleTargetsAnnotationFormsPanel(IDomeo domeo, final ArrayList<MAnnotation> targets) {
 		_domeo = domeo;
 		_title = TITLE;
 		_targets = targets;
 		
 		// Create layout
 		initWidget(binder.createAndBindUi(this)); 
 		this.setWidth((Window.getClientWidth() - 340) + "px");
 		
 		refreshTargets();
 		
 		//tabToolsPanel.setWidth((Window.getClientWidth() - 840) + "px");
 		tabToolsPanel.setHeight("590px");
 		
 		initializeForms();
 	}
 	
 	public MultipleTargetsAnnotationFormsPanel(IDomeo domeo, final MAnnotation annotation) {
 		_domeo = domeo;
 		_title = TITLE_EDIT;
 		_annotation = annotation;
 		
 		// Create layout
 		initWidget(binder.createAndBindUi(this)); 
 		this.setWidth((Window.getClientWidth() - 340) + "px");
 		
 		refreshTargets();
 		
 		tabToolsPanel.setHeight("590px");
 		
 		initializeForms();
 	}
 	
 	public Element getSelectedElement() {
 		return _element;
 	}
 	
 	public int getImageY() {
 		return _element.getAbsoluteTop();
 	}
 	
 	@Override
 	public void refresh() {
 		//initializeForms();
 		refreshTargets();
 	}
 	
 	public void initializeForms() {
 		tabToolsPanel.clear();
 		
 		if(_annotation==null) {
 			Collection<IFormGenerator> formGenerators = _domeo.getAnnotationFormsManager().getAnnotationFormGenerators();
 			Iterator<IFormGenerator> it = formGenerators.iterator();
 			while(it.hasNext()) {
 				AFormComponent form = it.next().getForm(this);
 				if(form instanceof IAllowsMultipleTargets) {
 					form.setWidth("600px");
 					if(form instanceof IResizable) forms.add(form);
 					tabToolsPanel.add(form, form.getTitle());
 				}
 			}
 		} else {
 			IFormGenerator formGenerator = _domeo.getAnnotationFormsManager().getAnnotationForm(_annotation.getClass().getName());
 			if(formGenerator!=null) {
 				AFormComponent form = formGenerator.getForm(this, _annotation);
 				form.setWidth("600px");
 				tabToolsPanel.add(form, form.getTitle());
 			}
 		}
 	}
 	
 	public void refreshTargets() {
 		targetsPanel.clear();
 		
 		if(_annotation == null) {
 			ArrayList<MAnnotation>  annotations = _domeo.getClipboardManager().getBufferedAnnotation();
 			for(MAnnotation annotation: annotations) {
 				ITileComponent c = _domeo.getAnnotationTailsManager().getAnnotationTile(annotation.getClass().getName(), _domeo.getContentPanel().getAnnotationFrameWrapper());
 				if(c==null) {
 					VerticalPanel vp = new VerticalPanel();
 					vp.add(new Label(annotation.getLocalId() + " - " + annotation.getClass().getName() + " - " + annotation.getY()));
 					targetsPanel.add(vp);
 				} else {
 					try {
 						if(c instanceof TSelectionTile) ((TSelectionTile)c).initializeLens(annotation, this);
 						else c.initializeLens(annotation);
 						targetsPanel.add(c.getTile());
 					} catch(Exception e) {
 						// If something goes wrong just display the default tile
 						e.printStackTrace();
 						VerticalPanel vp = new VerticalPanel();
 						vp.add(new Label(annotation.getLocalId() + " - " + annotation.getClass().getName() + " - " + annotation.getY()));
 						targetsPanel.add(vp);
 					}
 				}
 			}
 		} else {
 			for(MSelector selector: _annotation.getSelectors()) {
 				TPrefixSuffixTextSelectorTile tile = new TPrefixSuffixTextSelectorTile(_domeo, this);
 				tile.initializeLens(_annotation, selector);
 				targetsPanel.add(tile);
 			}
 		}
 	}
 	
 	public void refreshAnnotationForImage(MAnnotation annotation) {
 		
 		targetsPanel.clear();
 		
 		List<MAnnotation> annotations = new ArrayList<MAnnotation>();
 		annotations.add(annotation);
 		AnnotationSummaryTable table = new AnnotationSummaryTable(_domeo, this);
 		table.init();
 		table.refreshPanel(annotations);
 		targetsPanel.add(table);
 	}
 	
 	public String getTitle() {
 		return _title;
 	}
 
 	@Override
 	public void setContainer(IContainerPanel containerPanel) {
 		_containerPanel = containerPanel;
 	}
 	@Override
 	public IContainerPanel getContainer() {
 		return _containerPanel;
 	}
 	public void hideContainer() {
 		_domeo.getComponentsManager().removeComponent(this);
 		_containerPanel.hide();
 	}
 	
 	public void displayMessage(String message) {
 		footerSpan.setInnerHTML(message);
 	}
 	
 	public void clearMessage() {
 		footerSpan.setInnerHTML("");
 	}
 
 //	@Override
 	public void resized() {
 		this.setWidth((Window.getClientWidth() - 340) + "px");
 		tabToolsPanel.setWidth((Window.getClientWidth() - 610) + "px");
 		for(AFormComponent form:forms) {
 			if(form instanceof IResizable) ((IResizable)form).resized();
 		}
 	}
 
 	@Override
 	public void editAnnotation(MAnnotation annotation) {
 	}
 
 
 	@Override
 	public MGenericResource getResource() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public ArrayList<MAnnotation> getTargets() {
 		return _targets;
 	}
 	
 	@Override
 	public int getContainerWidth() {
 		return main.getOffsetWidth();
 	}
 }
