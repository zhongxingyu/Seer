 package com.gwtcraft.client.places;
 
 import java.util.Date;
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.gwtcraft.client.event.CharacterSelectedEvent;
 import com.gwtcraft.client.event.CharacterSelectedEventHandler;
 import com.gwtcraft.client.event.ItemSelectedEvent;
 import com.gwtcraft.client.event.ItemSelectedEventHandler;
 import com.gwtcraft.client.event.RegionChangedEvent;
 import com.gwtcraft.client.event.RegionChangedEventHandler;
 import com.gwtcraft.client.event.SearchInitiatedEvent;
 import com.gwtcraft.client.event.SearchInitiatedEventHandler;
 import com.gwtcraft.client.places.current.CharacterItemsDisplay;
 import com.gwtcraft.client.places.current.CharacterItemsPresenter;
 import com.gwtcraft.client.places.search.SearchPresenter;
 import com.gwtcraft.client.places.search.SearchView;
 import com.gwtcraft.client.places.upgrade.ItemUpgradesDisplay;
 import com.gwtcraft.client.places.upgrade.ItemUpgradesPresenter;
 import com.gwtcraft.client.service.ArmoryServiceAsync;
 
 public class Application implements Presenter, ValueChangeHandler<String> {
 
 	
 	
 	private final ArmoryServiceAsync armoryService;
 	private final HandlerManager eventBus;
 	private HasWidgets container;
 	private static String regionCode = "www";
 	
 	public Application(ArmoryServiceAsync armoryService, HandlerManager eventBus) {
 		this.armoryService = armoryService;
 		this.eventBus = eventBus;
 		
 		String region = Cookies.getCookie("region");
 		
 		if (region != null) {
 			regionCode = region;
 			
 			//refresh the cookie
 			setRegionCookie();
 		}
 		
 		addHandlers();
 	}
 	
 	private void addHandlers() {
 		History.addValueChangeHandler(this);
 		
 		eventBus.addHandler(CharacterSelectedEvent.TYPE, new CharacterSelectedEventHandler() {
 			@Override
 			public void onCharacterSelected(CharacterSelectedEvent event) {
 				History.newItem("c=" + URL.encode(event.getName()) + 
 						        "&r=" + URL.encode(event.getRealm()));
 			}
 		});
 		
 		eventBus.addHandler(SearchInitiatedEvent.TYPE, new SearchInitiatedEventHandler() {
 			@Override
 			public void onSearchInitiated(SearchInitiatedEvent event) {
				
				String searchHistory = "q=" + URL.encode(event.getSearchTerm());
				String oldHistory = History.getToken();
				
				if (searchHistory.equals(oldHistory)) {
					History.fireCurrentHistoryState();
				} else {
					History.newItem("q=" + URL.encode(event.getSearchTerm()));
				}
 			}
 		});
 		
 		eventBus.addHandler(ItemSelectedEvent.TYPE, new ItemSelectedEventHandler() {
 			@Override
 			public void onItemSelected(ItemSelectedEvent event) {
 				History.newItem("i=" + URL.encode(event.getItemId().toString()) + 
 						        "&c=" + URL.encode(event.getCharacterName()) + 
 						        "&r=" + URL.encode(event.getCharacterRealm()));
 			}
 		});
 		
 		eventBus.addHandler(RegionChangedEvent.TYPE, new RegionChangedEventHandler() {
 			@Override
 			public void onRegionChange(RegionChangedEvent event) {
 				regionCode = event.getRegionCode();
 				setRegionCookie();
 			}
 		});
 	}
 
 	private void showCharacter(String name, String realm) {
 		container.clear();
 		CharacterItemsDisplay view = new CharacterItemsDisplay(name, realm);
 		new CharacterItemsPresenter(armoryService, eventBus, view).go(container);
 	}
 	
 	private void setRegionCookie() {
 		Date now = new Date();
 		long twentyDays = 1000 * 60 * 60 * 24 * 20;
 		long later = now.getTime() + twentyDays;
 		Cookies.setCookie("region", regionCode, new Date(later));
 	}
 	
 	private void showUpgrade(Integer itemId, String name, String realm) {
 		container.clear();
 		ItemUpgradesDisplay view = new ItemUpgradesDisplay();
 		new ItemUpgradesPresenter(armoryService, view, name, realm, itemId).go(container);
 	}
 
 	@Override
 	public void go(HasWidgets container) {
 		this.container = container;
 		if ("".equals(History.getToken())) {
 			History.newItem("search");
 		} else {
 			History.fireCurrentHistoryState();
 		}
 	}
 
 	@Override
 	public void onValueChange(ValueChangeEvent<String> event) {
 		String token = event.getValue();
 		container.clear();
 		
 		if (token.equals("search")) {
 			new SearchPresenter(armoryService, eventBus, new SearchView()).go(container);
 		} else if (token.startsWith("q=")) {
 			String searchTerm = URL.decode(token.replace("q=", ""));
 			new SearchPresenter(armoryService, eventBus, new SearchView(), searchTerm).go(container);
 		} else if (token.startsWith("c=")) {
 			String combined = token.replace("c=", "");
 			String[] details = combined.split("&r="); 
 			showCharacter(URL.decode(details[0]), URL.decode(details[1]));
 		} else if (token.startsWith("i=")) {
 			//1=1234&c=Takbok&r=Eonar
 			String[] details = token.replace("i=", "").replace("&c=", "").replace("&r=", "").split("");
 			showUpgrade(Integer.parseInt(details[0]), details[1], details[2]);
 		} else {
 			//default is new blank search page
 			History.newItem("search");
 		}
 	}
 	
 	public static String getRegionCode() {
 		return regionCode;
 	}
 }
