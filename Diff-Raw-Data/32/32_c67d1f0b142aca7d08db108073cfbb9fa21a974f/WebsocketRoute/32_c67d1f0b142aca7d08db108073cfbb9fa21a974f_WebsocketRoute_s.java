 package org.oobium.app.routing.routes;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.netty.handler.codec.http.HttpMethod;
 import org.oobium.app.controllers.WebsocketController;
 import org.oobium.app.routing.Route;
 
 public class WebsocketRoute extends Route {
 
 	public final Class<? extends WebsocketController> controllerClass;
 	public final String[][] params;
 	public String group;
 	
 	public WebsocketRoute(String rule, Class<? extends WebsocketController> controllerClass, String group) {
 		super(Route.WEBSOCKET, HttpMethod.GET, rule);
 
 		this.controllerClass = controllerClass;
 		this.group = group;
 		
 		List<String[]> params = new ArrayList<String[]>();
 		parseRules(rule, null, params);
 		
 		if(params.isEmpty()) {
 			this.params = null;
 		} else {
 			this.params = params.toArray(new String[params.size()][]);
 		}
 	}
 
 	@Override
 	protected String[][] params() {
 		return params;
 	}
 
 }
