 package org.fhw.asta.kasse.client.controller;
 
 import org.fhw.asta.kasse.client.event.LoginEvent;
 import org.fhw.asta.kasse.client.event.LoginEventHandler;
 import org.fhw.asta.kasse.client.widget.topbar.TopBarWidget;
 import org.fhw.asta.kasse.client.widget.topbar.ready.ReadyTopBarWidget;
 
 import com.google.inject.Inject;
import com.google.web.bindery.event.shared.SimpleEventBus;
 
 public class TopbarController {
 
 	@Inject
 	private ReadyTopBarWidget readTopbarWidget;
 	
 	@Inject
 	private TopBarWidget topbarWidget;
 	
 	@Inject
	public void init(SimpleEventBus eventBus) {
 		eventBus.addHandler(LoginEvent.TYPE, new LoginHandler());
 	}
 	
 	private void onLogin() {
 		topbarWidget.setWidget(readTopbarWidget);
 	}
 	
 	private class LoginHandler implements LoginEventHandler {
 
 		@Override
 		public void onLogin(LoginEvent loginEvent) {
 			TopbarController.this.onLogin();
 		}
 		
 	}
 	
 	
 }
