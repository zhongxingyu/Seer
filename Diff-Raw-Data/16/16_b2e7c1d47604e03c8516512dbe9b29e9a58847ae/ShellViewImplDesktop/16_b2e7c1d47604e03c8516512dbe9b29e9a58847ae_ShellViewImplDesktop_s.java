 package org.robbins.flashcards.client.ui.desktop;
 
 import org.robbins.flashcards.client.factory.ClientFactory;
 import org.robbins.flashcards.client.mvp.ContentActivityMapper;
 import org.robbins.flashcards.client.mvp.NavigationActivityMapper;
 import org.robbins.flashcards.client.ui.AppConstants;
 import org.robbins.flashcards.client.ui.ShellView;
 import org.robbins.flashcards.model.UserDto;
 
 import com.google.gwt.activity.shared.ActivityManager;
 import com.google.gwt.activity.shared.ActivityMapper;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Overflow;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.InlineHyperlink;
 import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class ShellViewImplDesktop extends Composite implements ShellView {
 
 	private ClientFactory clientFactory;
 	private AppConstants constants;
 	
 	private static MyDockLayoutPanelUiBinder uiBinder = GWT
 			.create(MyDockLayoutPanelUiBinder.class);
 
 	interface MyDockLayoutPanelUiBinder extends
 			UiBinder<Widget, ShellViewImplDesktop> {
 	}
 
 	@UiField
 	SimplePanel navigationPanel;
 	
 	@UiField
 	SimplePanel contentPanel;
 	
 	@UiField
 	InlineLabel loggedInUser;
 	
 	@UiField
 	InlineHyperlink loginLogoutLink;
 	
 	public ShellViewImplDesktop(ClientFactory clientFactory) {
 		GWT.log("Creating 'ShellViewImplDesktop'");
 
 		this.clientFactory = clientFactory;
 		this.constants = clientFactory.getConstants();
 		
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		this.loginLogoutLink.setVisible(false);
 
 		// Start ActivityManager for the main widget with our ActivityMapper
 		ActivityMapper navigationActivityMapper = new NavigationActivityMapper(clientFactory);
 		ActivityManager navigationActivityManager = new ActivityManager(navigationActivityMapper, this.clientFactory.getEventBus());
 		navigationActivityManager.setDisplay(navigationPanel);
 
 		// right side
 		ActivityMapper contentActivityMapper = new ContentActivityMapper(clientFactory);
 		ActivityManager contentActivityManager = new ActivityManager(contentActivityMapper, this.clientFactory.getEventBus());
 	
 		//contentPanel.getElement().getStyle().setOverflow(Overflow.HIDDEN);
 		contentPanel.getElement().getStyle().setOverflow(Overflow.AUTO);
 		contentActivityManager.setDisplay(contentPanel);
 	}
 
 	@Override
 	public void setLogoutToken(String token) {
 		loginLogoutLink.setTargetHistoryToken(token);
 	}
 
 	@Override
 	public void setLoggedInUser(UserDto user) {
 		if (user == null ) {
 			loginLogoutLink.setText(constants.login());
 			loggedInUser.setText("");
 		} else {
 			loginLogoutLink.setText(constants.logout());
 			loggedInUser.setText(user.getEmail() + " | ");
 		}
 	}
 }
