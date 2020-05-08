 package com.reflect7.plansation.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 
 public class MainView extends Composite {
 
 	private static MainViewUiBinder uiBinder = GWT
 			.create(MainViewUiBinder.class);
 
 	interface MainViewUiBinder extends UiBinder<Widget, MainView> {}
 
 
 	public MainView() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 }
