 package com.kk_electronic.kkportal.core.security;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.kk_electronic.kkportal.core.ui.InputDialog;
 import com.kk_electronic.kkportal.core.ui.InputDialog.Handler;
 import com.kk_electronic.kkportal.examples.modules.MotDService;
 
 @Singleton
 public class IdentityProvider implements Handler{
 	private final InputDialog display;
 	private final EventBus eventBus;
 	private final String dialogtext = "Username:";
 	private String motd;
	private String errortext = "";
 	
 	public static interface Display {
 		void show();
 		void isShowing();
 		void setPresenter(IdentityProvider identityProvider);
 	}
 
 	@Inject
 	public IdentityProvider(final InputDialog display,EventBus eventBus,MotDService motDService) {
 		this.display = display;
 		this.eventBus = eventBus;
 		motDService.getMessageOfTheDay(new AsyncCallback<String>() {
 			
 			@Override
 			public void onSuccess(String result) {
 				setMotd(result);
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				setMotd(null);
 			}
 		});
 		display.setHandler(this);
 	}
 	
 	protected void setMotd(String newmotd) {
 		if(newmotd == null || newmotd.endsWith("\n")){
 			motd = newmotd;			
 		} else {
 			motd = newmotd + "\n";
 		}
 		updateText();
 	}
 
 	private void updateText() {
 		if(display.isShowing()){
 			display.setText(getDialogText());
 		}
 	}
 
 	private String getDialogText() {
 		if(motd == null){
 			return dialogtext;
 		} else {
			return motd + errortext + dialogtext;
 		}
 	}
 
 	List<Identity> identities = new LinkedList<Identity>();
 	public void invalidate(Identity identity){
 		identities.remove(identity);
 		if(identities.size() == 0){
 			eventBus.fireEventFromSource(new NewPrimaryIdentityEvent(null),this);
 		}
 	}
 	public Identity getPrimaryIdentity(){
 		if(identities.size() > 0) return identities.get(0);
 		display.show();
 		display.setText(getDialogText());
 		return null;
 	}
 
 	public void addIdentity(Identity identity) {
 		identities.add(identity);
 		if(identities.size() == 1){
 			eventBus.fireEventFromSource(new NewPrimaryIdentityEvent(identity),this);
 		}
 	}
 	
 	public HandlerRegistration addNewPrimaryIdentityEventHandler(NewPrimaryIdentityEvent.Handler handler){
 		return eventBus.addHandler(NewPrimaryIdentityEvent.TYPE, handler);
 	}
 	@Override
 	public void onInput(String input) {
 		String[] c = input.split("@",2);
		errortext = "";
 		addIdentity(new Identity(c[0],c[1]));
 	}

	public void invalidate(Identity identity, String reason) {
		invalidate(identity);
		errortext = "Password is rejected\n\n";
		display.show();
		display.setText(getDialogText());
	}
 }
