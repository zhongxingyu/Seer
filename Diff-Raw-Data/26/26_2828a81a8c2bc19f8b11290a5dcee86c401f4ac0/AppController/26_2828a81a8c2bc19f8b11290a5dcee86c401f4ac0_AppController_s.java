 package net.thesocialos.client;
 
 import java.util.Map;
 import net.thesocialos.client.event.AccountAddedEvent;
 import net.thesocialos.client.event.AccountAddedEventHandler;
 import net.thesocialos.client.event.LogoutEvent;
 import net.thesocialos.client.event.LogoutEventHandler;
 import net.thesocialos.client.event.MessageChatAvailableEvent;
 import net.thesocialos.client.event.MessageChatAvailableEventHandler;
 import net.thesocialos.client.helper.RPCXSRF;
 import net.thesocialos.client.presenter.DesktopPresenter;
 import net.thesocialos.client.presenter.Presenter;
 import net.thesocialos.client.presenter.RegisterPresenter;
 import net.thesocialos.client.presenter.UserProfilePresenter;
 import net.thesocialos.client.service.UserService;
 import net.thesocialos.client.service.UserServiceAsync;
 
 import net.thesocialos.client.view.DesktopView;
 import net.thesocialos.client.view.RegisterView;
 import net.thesocialos.client.view.profile.UserProfileView;
 import net.thesocialos.shared.model.Account;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.web.bindery.event.shared.SimpleEventBus;
 import com.googlecode.objectify.Key;
 
 public class AppController implements ValueChangeHandler<String> {
 
 	private final UserServiceAsync userService = GWT.create(UserService.class);
 
 	private final SimpleEventBus eventBus;
 	
 	private SimpleEventBus chatEventBus = new SimpleEventBus();
 	
 	private String lastToken = "";
 	
 	public AppController(SimpleEventBus eventBus) {
 		this.eventBus = eventBus;
		bind(); // Bind the appController to History to control its changes
 	}
 
 	@Override
 	public void onValueChange(ValueChangeEvent<String> event) {
 		String token = event.getValue();
 		if (token != null) {
 			Presenter presenter = null;
 			if(CacheLayer.getUser(true) != null) {
				if (token.equals("desktop") && !lastToken.contains("profile")) {
 					presenter = new DesktopPresenter(new SimpleEventBus[]{eventBus,chatEventBus}, new DesktopView());
 					presenter.go(TheSocialOS.get().root);
 				} else if (token.equals("profile")) {
 					loadProfile(presenter);
 				} else if (token.equals("profile-timeline")) {
 					loadProfileTimeline(presenter);
 				} else if (token.equals("profile-photos")) {
 					loadProfilePhotos(presenter);
 				} else if (token.equals("profile-music")) {
 					loadProfileMusic(presenter);
 				} else if (token.equals("profile-videos")) {
 					loadProfileVideos(presenter);
 				} else if (token.equals("profile-links")) {
 					loadProfileLinks(presenter);
 				} else if (token.equals("account-added")) {
					token = lastToken = "profile";
 					accountAdded();
 					//eventBus.fireEvent(new AccountAddedEvent());
				} else if (token.equals("desktop")) {
 				} else {
 					History.newItem("desktop");
 				}
 			} else {
 				if (token.equals("register")) { 
 					presenter = new RegisterPresenter(eventBus, new RegisterView());
 					presenter.go(TheSocialOS.get().root);
 					return;
 				} else if (token.equals("login")) {
 					TheSocialOS.get().showLoginView();
 					return;
 				} else
 					History.newItem("login");
 			}
			lastToken = token;
 		}
 	}
 	
 	private void loadProfileLinks(Presenter presenter) {
 		checkProfile(presenter);
 		TheSocialOS.profilePresenter.goProfileLinks(); // Finally load the links screen on the profile window.
 	}
 
 	private void loadProfileVideos(Presenter presenter) {
 		checkProfile(presenter);
 		TheSocialOS.profilePresenter.goProfileVideos();
 	}
 
 	private void loadProfileMusic(Presenter presenter) {
 		checkProfile(presenter);
 		TheSocialOS.profilePresenter.goProfileMusic();		
 	}
 
 	private void loadProfilePhotos(Presenter presenter) {
 		checkProfile(presenter);
 		TheSocialOS.profilePresenter.goProfilePhotos();
 	}
 
 	private void loadProfileTimeline(Presenter presenter) {
 		checkProfile(presenter);
 		TheSocialOS.profilePresenter.goProfileTimeline();
 	}
 
 	private void loadProfile(Presenter presenter) {
 		checkProfile(presenter);
 		TheSocialOS.profilePresenter.goProfile(); // Load the main profile screen in the profile window.
 	}
 	
 	private void checkProfile(Presenter presenter){
		if (lastToken.equals("desktop")) { // If on the desktop, create the profile window.
 			TheSocialOS.profilePresenter = new UserProfilePresenter(eventBus, new UserProfileView());
 			TheSocialOS.profilePresenter.go(TheSocialOS.get().getDesktop());
		} else if (!lastToken.contains("profile")) { // If not on desktop or another profile part, create the desktop and the profile window.
 			presenter = new DesktopPresenter(new SimpleEventBus[]{eventBus,chatEventBus}, new DesktopView());
 			presenter.go(TheSocialOS.get().root);
 			TheSocialOS.profilePresenter = new UserProfilePresenter(eventBus, new UserProfileView());
 			TheSocialOS.profilePresenter.go(TheSocialOS.get().getDesktop());
 		}
 	}
 
 	/**
 	 * Binds the History class to this ValueChangeHandler. And adds the eventHandlers to the eventBus.
 	 */
 	private void bind() {
 		History.addValueChangeHandler(this);
 		eventBus.addHandler(LogoutEvent.TYPE, new LogoutEventHandler() {
 			
 			@Override
 			public void onLogout(LogoutEvent event) {
 				doLogout();
 			}
 		});
 		
 		eventBus.addHandler(AccountAddedEvent.TYPE, new AccountAddedEventHandler() {
 			
 			@Override
 			public void onAcountAdd(AccountAddedEvent event) {
 				new RPCXSRF<Map<Key<Account>, Account>>(userService) {
 
 					@Override
 					protected void XSRFcallService(AsyncCallback<Map<Key<Account>, Account>> cb) {
 						userService.getCloudAccounts(cb);
 					}
 					
 					@Override
 					public void onSuccess(Map<Key<Account>, Account> accounts) {
 						TheSocialOS.get().setCurrentUserAccounts(accounts);
 						//Window.alert("Cuenta añadida");
 						Window.alert("" + TheSocialOS.get().getCurrentUserAccounts().size());
 						TheSocialOS.profilePresenter.goProfile();
 					}
 					
 					@Override
 					public void onFailure(Throwable caught) {
 						GWT.log(caught.getMessage());
 						Window.alert(caught.getMessage());
 					}
 					
 				}.retry(3);
 			}
 		});
 		
 		eventBus.addHandler(MessageChatAvailableEvent.TYPE, new MessageChatAvailableEventHandler() {
 			
 			@Override
 			public void onContentAvailable(MessageChatAvailableEvent contentAvailableEvent) {
 				chatEventBus.fireEvent(new MessageChatAvailableEvent(contentAvailableEvent.getMessageChat()));
 			}
 		});
 	}
 	
 	/**
 	 * Deletes the cookies, the session and calls the method to delete the user  
 	 */
 	protected void doLogout() {
 		Cookies.removeCookie("sid");
 		Cookies.removeCookie("uid");
 		CacheLayer.deleteUser();
 		new RPCXSRF<Void>(userService) {
 
 			@Override
 			protected void XSRFcallService(AsyncCallback<Void> cb) {
 				userService.logout(cb);
 			}
 		};
 		History.newItem("login");
 	}
 
 	/**
 	 * Method called from the main class during loading to fire the current state of the history and do some minor fixes to errors that can occur during the first load. 
 	 */
 	public void go() {
 		if (History.getToken().contains("profile")) // This method can only be called when the web is loaded so lastToken is replaced by "" so the profile window is correctly loaded. Just in case there is some garbage in it.  
 			lastToken = "";
 		if (History.getToken().equals("")) { // If there is no History token, then go to the desktop
 			History.newItem("desktop");
 		} else {
 			History.fireCurrentHistoryState();
 		}
 	}
 	
 	private void accountAdded() {
 		new RPCXSRF<Map<Key<Account>, Account>>(userService) {
 
 			@Override
 			protected void XSRFcallService(AsyncCallback<Map<Key<Account>, Account>> cb) {
 				userService.getCloudAccounts(cb);
 			}
 			
 			@Override
 			public void onSuccess(Map<Key<Account>, Account> accounts) {
 				TheSocialOS.get().setCurrentUserAccounts(accounts);
 				//Window.alert("Cuenta añadida");
 				//Window.alert("" + TheSocialOS.get().getCurrentUserAccounts().size());
 				History.newItem("profile");
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				GWT.log(caught.getMessage());
 				Window.alert(caught.getMessage());
 			}
 			
 		}.retry(3);
 	}
 
 	public SimpleEventBus getChatEventBus() {
 		return chatEventBus;
 	}
 	
 	public void setChatEventBus(SimpleEventBus chatEventBus) {
 		this.chatEventBus = chatEventBus;
 	}
 }
