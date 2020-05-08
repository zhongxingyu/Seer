 package org.mindinformatics.gwt.domeo.plugins.annotation.commentaries.linear.ui.east;
 
 import org.mindinformatics.gwt.domeo.client.IDomeo;
 import org.mindinformatics.gwt.domeo.client.ui.annotation.actions.IAnnotationEditListener;
 import org.mindinformatics.gwt.domeo.client.ui.annotation.tiles.ATileComponent;
 import org.mindinformatics.gwt.domeo.client.ui.annotation.tiles.ITileComponent;
 import org.mindinformatics.gwt.domeo.model.MAnnotation;
 import org.mindinformatics.gwt.domeo.model.MAnnotationSet;
 import org.mindinformatics.gwt.domeo.plugins.annotation.commentaries.linear.model.MLinearCommentAnnotation;
 
 import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Provides the standard lens for the annotation type Highlight.
  * 
  * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
  */
 public class LLinearCommentLens extends ATileComponent implements ITileComponent {
 
 	interface Binder extends UiBinder<Widget, LLinearCommentLens> { }
 	private static final Binder binder = GWT.create(Binder.class);
 	
 	// By contract 
 	private MAnnotationSet _set;
 	private MLinearCommentAnnotation _annotation;
 	
 	@UiField VerticalPanel body;
 	@UiField HorizontalPanel provenance;
 	@UiField FlowPanel content;
 	@UiField Label title;
 	@UiField Label text;
 	
 	public LLinearCommentLens(IDomeo domeo, IAnnotationEditListener listener, MAnnotationSet set) {
 		super(domeo, listener);
 		_set = set;
 		
 		initWidget(binder.createAndBindUi(this));
 		
 		tileResources.css().ensureInjected();
 	}
 	
 	public MAnnotation getAnnotation() {
 		return _annotation;
 	}
 		
 	@Override
 	public void initializeLens(MAnnotation annotation) {
 		_annotation = (MLinearCommentAnnotation) annotation;
 		refresh();
 	}	
 	@Override
 	public Widget getTile() {
 		return this;
 	}
 	@Override
 	public void refresh() {
 		try {
 			createProvenanceBar("", provenance, "", _annotation);
 			
 			/*
 			MAnnotationSet set = _domeo.getAnnotationPersistenceManager().getSetByAnnotationId(_annotation.getLocalId());
 		*/
 			if(_set.getLabel()!=null) 
 				title.setText(_set.getLabel());
 			else 
 				title.setVisible(false);
 			
 			//type.setText("Comment:");
 			text.setText(_annotation.getText());
 			/*
 			text.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					Window.alert("Edit or display?");
 				}
 			});
 			*/
 			
 			injectButtons("", content, _annotation);
 			
 		} catch(Exception e) {
 			_domeo.getLogger().exception(this, e.getMessage());
 		}
 	}
 }
