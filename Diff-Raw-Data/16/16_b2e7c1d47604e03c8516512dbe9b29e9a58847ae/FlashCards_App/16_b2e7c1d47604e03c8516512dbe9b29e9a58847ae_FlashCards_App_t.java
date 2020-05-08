 package org.robbins.flashcards.client;
 
 import org.robbins.flashcards.client.factory.ClientFactory;
 import org.robbins.flashcards.client.mvp.AppPlaceHistoryMapper;
 import org.robbins.flashcards.client.mvp.ShellActivityMapper;
 import org.robbins.flashcards.client.place.ShellPlace;
 import org.robbins.flashcards.client.ui.desktop.style.ShellStyleResource;
 import org.robbins.flashcards.client.ui.widgets.SimpleWidgetPanel;
 import org.robbins.flashcards.model.UserDto;
 import org.robbins.flashcards.util.ConstsUtil;
 
 import com.google.gwt.activity.shared.ActivityManager;
 import com.google.gwt.activity.shared.ActivityMapper;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.place.shared.Place;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.place.shared.PlaceHistoryHandler;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 
 
 public class FlashCards_App implements EntryPoint {
 
 	private Place defaultPlace;
 	
 	private SimpleWidgetPanel appWidget = new SimpleWidgetPanel();
 	
 	public void onModuleLoad() {
 		GWT.log("FlashCards_App - onModuleLoad()");
 
 		// set the date format for RestyGwt so it can use Unix timestamp date format
 		org.fusesource.restygwt.client.Defaults.setDateFormat(null);
 		
		Window.enableScrolling(true);
		
 		ShellStyleResource.INSTANCE.shellStyles().ensureInjected();
 		ClientFactory clientFactory = GWT.create(ClientFactory.class);
 		EventBus eventBus = clientFactory.getEventBus();
 		PlaceController placeController = clientFactory.getPlaceController();
 		
 		UserDto loggedInUser = clientFactory.getLoggedInUser();
 /*
 		if (loggedInUser == null) {
 			GWT.log("User is NOT logged in");
 			Cookies.setCookie("desitinationURL", Window.Location.getHref());
 			defaultPlace = new LoginPlace(ConstsUtil.LOGIN);
 		}
 		else {
 			GWT.log("Logged in user: " + loggedInUser.toString());
 			defaultPlace = new ShellPlace(ConstsUtil.SHELL_VIEW);
 		}
 */		Cookies.setCookie("desitinationURL", Window.Location.getHref());
 		defaultPlace = new ShellPlace(ConstsUtil.SHELL_VIEW);
 		
 		// start activity manager for our main widget with our ActivityMapper
 		ActivityMapper activityMapper = new ShellActivityMapper(clientFactory);
 		ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
 		activityManager.setDisplay(appWidget);
 		
 		// start PlaceHistoryHandler with our PlaceHistoryMapper
 		AppPlaceHistoryMapper historyMapper = GWT.create(AppPlaceHistoryMapper.class);
 		PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
 		historyHandler.register(placeController, eventBus, defaultPlace);
 		
 		RootLayoutPanel.get().add(appWidget);
 		
 		historyHandler.handleCurrentHistory();
 		
 	}
 }
