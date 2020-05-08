 /**
  * 
  */
 package org.cotrix.web.demo.client.credential;
 
 import org.cotrix.web.common.client.event.CotrixBus;
 import org.cotrix.web.common.client.event.CotrixStartupEvent;
 import org.cotrix.web.common.client.event.ExtensibleComponentReadyEvent;
 import org.cotrix.web.demo.client.resources.CotrixDemoResources;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.user.client.ui.InlineHTML;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 import com.google.web.bindery.event.shared.binder.EventBinder;
 import com.google.web.bindery.event.shared.binder.EventHandler;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class CredentialsPopupController {
 	
 	private static final String COOKIE_NAME = "COTRIX_SHOW_DEMO_CREDENTIAL";
 	
 	interface CredentialPopupControllerEventBinder extends EventBinder<CredentialsPopupController>{};
 	
 	@Inject
 	CredentialsPopup credentialsPopup;
 	
 	@Inject
 	CotrixDemoResources resources;
 	
 	@Inject
 	private void bind(CredentialPopupControllerEventBinder binder, @CotrixBus EventBus eventBus) {
 		binder.bindEventHandlers(this, eventBus);
 	}
 	
 	public void init(){
 	}
 	
 	@EventHandler
 	void onUserBarReady(ExtensibleComponentReadyEvent componentReadyEvent) {
 		if (componentReadyEvent.getComponentName().equals("UserBar")) {
 			Log.trace("UserBar Ready");
 			HasWidgets extensionArea = componentReadyEvent.getHasExtensionArea().getExtensionArea();
 			
			InlineHTML demoLink = new InlineHTML("It's only a demo...");
 			demoLink.setStyleName(resources.css().demoLabel());
 			extensionArea.add(demoLink);
 			demoLink.addClickHandler(new ClickHandler() {
 				
 				@Override
 				public void onClick(ClickEvent event) {
 					credentialsPopup.showCentered();
 				}
 			});
 			
 		}
 	}
 	
 	@EventHandler
 	void onCotrixStartup(CotrixStartupEvent event){
 		Log.trace("Showing demo credentials");
 		boolean showPopup = shouldShowDemoCredential();
 		if (showPopup) {
 			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 				
 				@Override
 				public void execute() {
 					credentialsPopup.showCentered();
 				}
 			});
 			
 		}
 	}
 	
 	private boolean shouldShowDemoCredential() {
 		String value = Cookies.getCookie(COOKIE_NAME);
 		if (value == null) Cookies.setCookie(COOKIE_NAME, "TRUE");
 		return value == null;
 	}
 
 }
