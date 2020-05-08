 /*******************************************************************************
 
  * Copyright (c) 2013 Pronoia Health LLC.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Pronoia Health LLC - initial API and implementation
  *******************************************************************************/
 package com.pronoiahealth.olhie.server.services;
 
 import java.util.logging.Logger;
 
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.event.Event;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 
 import org.jboss.errai.cdi.server.events.EventConversationContext;
 
 import com.pronoiahealth.olhie.client.shared.events.LogoutRequestEvent;
 import com.pronoiahealth.olhie.client.shared.events.LogoutResponseEvent;
 import com.pronoiahealth.olhie.client.shared.events.ServiceErrorEvent;
 import com.pronoiahealth.olhie.server.security.ServerUserToken;
 
 /**
  * LogoutService.java<br/>
  * Responsibilities:<br/>
  * 1. Sets the ServerUserToken to logged out<br/>
  * 2. Fired the LoggedoutEvent<br/>
  * 
  * @author John DeStefano
  * @version 1.0
  * @since May 26, 2013
  * 
  */
 @RequestScoped
 public class LogoutService {
 
 	@Inject
 	private Logger log;
 
 	@Inject
 	private ServerUserToken userToken;
 
 	@Inject
 	private SessionTracker sessionTracker;
 
 	@Inject
 	private Event<LogoutResponseEvent> logoutResponseEvent;
 
 	@Inject
 	private Event<ServiceErrorEvent> serviceErrorEvent;
 
 	/**
 	 * Constructor
 	 * 
 	 */
 	public LogoutService() {
 	}
 
 	/**
 	 * Sets the ServerUserToken to loggedin = false, stops the SessionTracker
 	 * from tracking the session, and then fires a LogoutResponseEvent
 	 * 
 	 * @param logoutRequestEvent
 	 */
 	// @SecureAccess({ SecurityRoleEnum.ADMIN, SecurityRoleEnum.AUTHOR,
 	// SecurityRoleEnum.REGISTERED })
 	public void observesLogoutRequestEvent(
 			@Observes LogoutRequestEvent logoutRequestEvent) {
 		try {
 			if (userToken.getLoggedIn() == true) {
				userToken.clearToken();
 				logoutResponseEvent.fire(new LogoutResponseEvent());
 			}
 			String erraiSessionId = EventConversationContext.get()
 					.getSessionId();
 			sessionTracker.stopTrackingUserSession(erraiSessionId);
 		} catch (Exception e) {
 			serviceErrorEvent.fire(new ServiceErrorEvent(e.getMessage()));
 		}
 	}
 
 }
