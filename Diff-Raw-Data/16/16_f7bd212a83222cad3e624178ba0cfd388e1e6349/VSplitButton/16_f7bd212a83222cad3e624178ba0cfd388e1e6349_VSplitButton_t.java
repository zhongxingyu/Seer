 package org.vaadin.hene.splitbutton.widgetset.client.ui;
 
 import java.util.Set;
 
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.Widget;
 import com.vaadin.terminal.gwt.client.ApplicationConnection;
 import com.vaadin.terminal.gwt.client.Container;
 import com.vaadin.terminal.gwt.client.Paintable;
 import com.vaadin.terminal.gwt.client.RenderSpace;
 import com.vaadin.terminal.gwt.client.UIDL;
 import com.vaadin.terminal.gwt.client.Util;
 
 public class VSplitButton extends Composite implements Paintable, Container {
 
 	public static final String CLASSNAME = "v-splitbutton";
 
 	private final Panel panel;
 
 	private Widget buttonWidget;
 	private Widget popupButtonWidget;
 
 	private boolean initDone = false;
 
	private RenderSpace renderSpace = new RenderSpace();
 	private String width;
 	private String height;
 
 	public VSplitButton() {
 		panel = new FlowPanel();
 		initWidget(panel);
 
 		setStyleName(CLASSNAME);
 	}
 
 	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
 		if (client.updateComponent(this, uidl, false)) {
 			return;
 		}
 
 		UIDL buttonUidl = uidl.getChildUIDL(0);
 		Paintable buttonPaintable = client.getPaintable(buttonUidl);
 		panel.add((Widget) buttonPaintable);
 		buttonPaintable.updateFromUIDL(buttonUidl, client);
 		buttonWidget = (Widget) buttonPaintable;
 
 		UIDL popupButtonUidl = uidl.getChildUIDL(1);
 		Paintable popupButtonPaintable = client.getPaintable(popupButtonUidl);
 		panel.add((Widget) popupButtonPaintable);
 		popupButtonPaintable.updateFromUIDL(popupButtonUidl, client);
 		popupButtonWidget = (Widget) popupButtonPaintable;
 
 		if (!initDone) {
 			setButtonWidth();
 			setHeight();
 		}
 
 		initDone = true;
 	}
 
 	private void setButtonWidth() {
 		if (width == null) {
 			buttonWidget.setWidth("");
 		} else {
 			int popupButtonWidth = Util.getRequiredWidth(popupButtonWidget
 					.getElement());
 			buttonWidget.setWidth((getOffsetWidth() - popupButtonWidth) + "px");
 		}
		renderSpace.setWidth(getOffsetWidth());
 	}
 
 	@Override
 	public void setWidth(String width) {
 		if (width == null || "".equals(width)) {
 			this.width = null;
 		} else {
 			this.width = width;
 		}
 
 		Util.setWidthExcludingPaddingAndBorder(this, width, 0);
 
 		if (initDone) {
 			setButtonWidth();
 		}
 	}
 
 	private void setHeight() {
 		if (this.height == null) {
 			buttonWidget.setHeight("");
 			popupButtonWidget.setHeight("");
 			int buttonHeight = Util.getRequiredHeight(buttonWidget);
 			int popupButtonHeight = Util.getRequiredHeight(popupButtonWidget);
 			if (buttonHeight > popupButtonHeight) {
 				buttonWidget.setHeight(buttonHeight + "px");
 				popupButtonWidget.setHeight(buttonHeight + "px");
				renderSpace.setHeight(buttonHeight);
 			} else {
 				buttonWidget.setHeight(popupButtonHeight + "px");
 				popupButtonWidget.setHeight(popupButtonHeight + "px");
				renderSpace.setHeight(popupButtonHeight);
 			}
 		} else {
 			buttonWidget.setHeight(this.height);
 			popupButtonWidget.setHeight(this.height);
			renderSpace.setHeight(Integer.parseInt(this.height.substring(0,
					this.height.length() - 2)));
 		}
 	}
 
 	@Override
 	public void setHeight(String height) {
 		super.setHeight(height);
 		if (height == null || "".equals(height)) {
 			this.height = null;
 		} else {
 			this.height = height;
 		}
 		if (initDone) {
 			setHeight();
 		}
 	}
 
 	public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public boolean hasChildComponent(Widget component) {
 		return component.getParent() == panel;
 	}
 
 	public void updateCaption(Paintable component, UIDL uidl) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public boolean requestLayout(Set<Paintable> children) {
 		if (initDone) {
 			setButtonWidth();
 			setHeight();
 		}
 		return false;
 	}
 
 	public RenderSpace getAllocatedSpace(Widget child) {
		return renderSpace;
 	}
 }
