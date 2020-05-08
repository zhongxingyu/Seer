 package com.wtf.client;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class StatusBar {
 	private static StatusBarWidget _status_bar = null;
 	private static Selector _selector_manager;
 
 
 	public static void init(Selector selector_manager) {
 		_selector_manager = selector_manager;
 		//default value TODO: read from config
 		String horizontal = "left";
 		_status_bar = new StatusBarWidget(horizontal);
 		RootPanel.get().add(_status_bar);
 	}
 
 	public static void setStatus(String s) {
 		_status_bar.setStatus(s);
 	}
 
 	/*
 	 *  Status Bar Widget
 	 */
 	private static class StatusBarWidget extends Composite implements ClickHandler {
 
 		private Label _status = new Label();
 		//menu buttons
 		private Button _b_start_selection;
 		private Button _b_show_discussions;
 
 		public StatusBarWidget(String orientation) {		
 			HorizontalPanel menu_panel = new HorizontalPanel();
 			HorizontalPanel status_panel = new HorizontalPanel();
 			VerticalPanel v_panel = new VerticalPanel();
 			// All composites must call initWidget() in their constructors.
 			initWidget(v_panel);	
 
 			//set styles
 			getElement().setId("wtf_status_bar");
 			addStyleName("wtf_ignore");
 			if(orientation.equals("left")) {
 				DOM.setStyleAttribute(getElement(), "left", "0px");
 				menu_panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 			} else {
 				DOM.setStyleAttribute(getElement(), "right", "0px");
 				menu_panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 			}
 			menu_panel.getElement().setId("wtf_menu_panel");
 			status_panel.getElement().setId("wtf_status_panel");
			v_panel.setSpacing(0);			
 
 			//compose all
 			v_panel.add(menu_panel);
 			v_panel.add(status_panel);
 			status_panel.add(_status);
 
 			//menu panel	
 			_b_start_selection = new Button("zaznacz", new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					_selector_manager.newSelection();
 				}
 			});
 
 			_b_show_discussions = new Button("pokaż dyskusje", new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					Debug.log("show discussions pressed");
 				}
 			});
 
 			menu_panel.add(_b_start_selection);
 			menu_panel.add(_b_show_discussions);
 		}
 
 		public void setStatus(String s) {
 			_status.setText(s);
 		}
 
 		public void onClick(ClickEvent event) {
 			/*	Object sender = event.getSource();
 			if (sender == )*/
 		}
 	}
 
 }
