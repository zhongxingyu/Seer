 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.manager;
 
 import static org.jboss.netty.handler.codec.http.HttpMethod.POST;
 import static org.oobium.app.http.Action.destroy;
 import static org.oobium.app.http.Action.show;
 import static org.oobium.app.http.Action.showAll;
 import static org.oobium.app.http.Action.update;
 
 import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
 import org.oobium.app.AppService;
 import org.oobium.app.persist.MemoryPersistService;
 import org.oobium.app.routing.Router;
 import org.oobium.client.websockets.Websocket;
 import org.oobium.client.websockets.WebsocketListener;
 import org.oobium.client.websockets.Websockets;
 import org.oobium.manager.controllers.BundleController;
 import org.oobium.manager.models.Bundle;
 import org.oobium.utils.Config;
 import org.osgi.framework.BundleContext;
 
 public class ManagerService extends AppService {
 
 	public static BundleContext context() {
 		return getActivator(ManagerService.class).getContext();
 	}
 
 	
 	public ManagerService() {
 		setPersistService(new MemoryPersistService());
 	}
 	
 	@Override
 	public void addRoutes(Config config, Router router) {
 		router.addResources(Bundle.class);
 		
 		router.addResource("{models}/{name:[\\w\\.]+}_{version:[\\w\\d\\.-]+}", Bundle.class, destroy);
 		router.addResource("{models}/{name:[\\w\\.]+}", Bundle.class, destroy);
 		router.addResource("{models}", Bundle.class, destroy);
 
 		router.addResource("{models}/{name:[\\w\\.]+}_{version:[\\w\\d\\.-]+}", Bundle.class, update);
 		router.addResource("{models}/{name:[\\w\\.]+}", Bundle.class, update);
 		router.addResource("{models}", Bundle.class, update);
 
 		router.addResource("{models}/{name:[\\w\\.]+}_{version:[\\w\\d\\.-]+}", Bundle.class, show);
 
 		router.addResource("{models}/{name:[\\w\\.]+}", Bundle.class, showAll);
 		
 		router.add("refresh").asRoute(POST, BundleController.class);
 	}
 
 	private static Websocket websocket;
 	
 	public static void send(String eventName, String message) {
 		if(websocket != null) {
 			websocket.send(eventName + ":" + message);
 		}
 	}
 	
 	@Override
 	public void startWorkers() {
		System.out.println("url: " + System.getProperty("org.oobium.manager.url"));
 		websocket = Websockets.connect(System.getProperty("org.oobium.manager.url"), new WebsocketListener() {
 			@Override
 			public void onMessage(Websocket websocket, WebSocketFrame frame) {
 				// TODO
 			}
 			@Override
 			public void onError(Websocket websocket, Throwable t) {
 				logger.warn(t);
 			}
 			@Override
 			public void onDisconnect(Websocket websocket) {
				logger.warn("manager websocket has disconnected");
 			}
 			@Override
 			public void onConnect(Websocket websocket) {
 				logger.info("manager websocket has disconnected");
 			}
 		});
 	}
 
 	@Override
 	protected void teardown() {
 		websocket.disconnect();
 	}
 	
 }
