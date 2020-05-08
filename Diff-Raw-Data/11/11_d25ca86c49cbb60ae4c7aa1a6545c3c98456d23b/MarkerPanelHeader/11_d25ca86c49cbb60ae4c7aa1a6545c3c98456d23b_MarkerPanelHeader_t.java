 /**
  * 
  */
 package org.cotrix.web.manage.client.codelist.codes.marker;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FocusPanel;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.SimpleCheckBox;
 import com.google.gwt.user.client.ui.ToggleButton;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class MarkerPanelHeader extends Composite implements HasClickHandlers {
 
 	private static LinkTypeHeaderUiBinder uiBinder = GWT.create(LinkTypeHeaderUiBinder.class);
 
 	interface LinkTypeHeaderUiBinder extends UiBinder<Widget, MarkerPanelHeader> {
 	}
 	
 	public interface HeaderListener {
 		public void onHeaderClicked();
 		public void onSwitchChange(boolean isDown);
 	}
 	
 	@UiField
 	ToggleButton switchButton;
 	
 	@UiField
 	FocusPanel headerBox;
 	
 	@UiField
 	InlineLabel headerLabel;
 	
 	@UiField
 	SimpleCheckBox activationCheck;
 	
 	private HeaderListener listener; 
 	
 	private boolean clickEnabled = true;
 	
 	public MarkerPanelHeader() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 	
 	public void setSwitchDown(boolean down) {
 		switchButton.setDown(down);
 	}
 	
 	public void setSwitchEnabled(boolean enabled) {
 		switchButton.setEnabled(enabled);
 	}
 	
 	public void setActivationCheck(boolean checked) {
 		activationCheck.setValue(checked);
 	}
 	
 	public void setActivationCheckEnabled(boolean enabled) {
 		activationCheck.setEnabled(enabled);
 	}
 	
 	public void setClickEnabled(boolean enabled) {
 		this.clickEnabled = enabled;
 	}
 	
 
 	public void setListener(HeaderListener listener) {
 		this.listener = listener;
 	}
 	
 	public void setHeaderLabel(String label) {
 		this.headerLabel.setText(label);
 		this.headerLabel.setTitle(label);
 	}
 	
 	public void setBackgroundColor(String color) {
 		getElement().getStyle().setBackgroundColor(color);
 	}
 	
 	public void setLabelColor(String color) {
 		headerLabel.getElement().getStyle().setColor(color);
 	}
 	
 	public void setHeaderStyle(String style) {
 		this.headerLabel.setStyleName(style);
 	}
 	
 	public void addHeaderStyle(String style) {
 		this.headerLabel.addStyleName(style);
 	}
 
 	@UiHandler("activationCheck")
 	void onHeaderLabel(ClickEvent event) {
 		if (listener!=null) listener.onHeaderClicked();
 	}
 	
 	@UiHandler("switchButton")
 	void onSwitch(ClickEvent event) {
 		if (listener!=null) listener.onSwitchChange(switchButton.isDown());
 	}
 
 	@Override
 	public HandlerRegistration addClickHandler(final ClickHandler handler) {
 		return headerBox.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				if (clickEnabled) handler.onClick(event);
 			}
 		});
 	}
 }
