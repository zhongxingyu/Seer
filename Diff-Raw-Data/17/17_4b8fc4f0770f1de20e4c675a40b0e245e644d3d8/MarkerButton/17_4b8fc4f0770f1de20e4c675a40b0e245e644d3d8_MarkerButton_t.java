 /**
  * 
  */
 package org.cotrix.web.manage.client.codelist.codes.marker;
 
 import static org.cotrix.web.manage.client.codelist.codes.marker.MarkersResource.*;
 
 import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Float;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FocusPanel;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class MarkerButton extends Composite implements HasValueChangeHandlers<Boolean> {
 	
 	private FocusPanel focusPanel;
 	private boolean down;
 
 	
 	public MarkerButton(MarkerType markerType) {
 		
 		focusPanel = new FocusPanel();
 		focusPanel.setStyleName(markerType.getHighlightStyleName());
 		focusPanel.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				onPanelClick();
 			}
 		});
 		
 		initWidget(focusPanel);
 		
 		down = false;
 		getElement().getStyle().setProperty("borderRadius", "2px");
		getElement().getStyle().setFloat(Float.LEFT);
 		setTitle(markerType.getName());
 		style();
 	}
 	
 	private void onPanelClick() {
 		down = !down;
 		style();
 		ValueChangeEvent.fire(this, down);
 	}
 	
 	private void style() {
 		Style elementStyle = getElement().getStyle();
 		elementStyle.setProperty("border", down?"2px solid "+style.selectedButtonColor():"none");
 		elementStyle.setWidth(down?12:16, Unit.PX);
 		elementStyle.setHeight(down?12:16, Unit.PX);
 	}
 
 	@Override
 	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
 		return addHandler(handler, ValueChangeEvent.getType());
 	}	
 }
