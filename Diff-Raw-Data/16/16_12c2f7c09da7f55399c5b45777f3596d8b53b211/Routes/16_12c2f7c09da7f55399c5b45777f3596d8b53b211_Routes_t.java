 /*
  * This file is a component of thundr, a software library from 3wks.
  * Read more: http://www.3wks.com.au/thundr
  * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.threewks.thundr.route;
 
 import static com.atomicleopard.expressive.Expressive.list;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.threewks.thundr.action.Action;
 import com.threewks.thundr.action.ActionException;
 import com.threewks.thundr.action.ActionResolver;
 import com.threewks.thundr.configuration.JsonProperties;
 import com.threewks.thundr.logger.Logger;
 
 public class Routes {
 	private Map<Route, Action> actionsForRoutes = new HashMap<Route, Action>();
 	private Map<RouteType, Map<String, Route>> routes = createRoutesMap();
 
 	private Map<Class<? extends Action>, ActionResolver<?>> actionResolvers = new LinkedHashMap<Class<? extends Action>, ActionResolver<?>>();
 
 	private boolean debug = true;
 
 	public void addRoutes(Route... routes) {
 		for (Route route : routes) {
 			addRoute(route);
 		}
 	}
 
 	public void addRoutes(Collection<Route> routes) {
 		for (Route route : routes) {
 			addRoute(route);
 		}
 	}
 
 	public void addRoute(Route route) {
 		String path = route.getRouteMatchRegex();
 		String actionName = route.getActionName();
 		RouteType routeType = route.getRouteType();
 		Action action = createAction(actionName);
 		this.routes.get(routeType).put(path, route);
 		this.actionsForRoutes.put(route, action);
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T extends Action> Object invoke(String routePath, RouteType routeType, HttpServletRequest req, HttpServletResponse resp) {
 		Logger.debug("Requesting '%s'", routePath);
 		Route route = findMatchingRoute(routePath, routeType);
 		if (route != null) {
 			T action = (T) actionsForRoutes.get(route);
 			return resolveAction(routePath, routeType, req, resp, route, action);
 		}
 		String debugString = debug ? listRoutes() : "";
 		throw new RouteNotFoundException("No route matching the request %s %s\n%s", routeType, routePath, debugString);
 	}
 
 	public Route findMatchingRoute(String routePath, RouteType routeType) {
 		Map<String, Route> routesForRouteType = routes.get(routeType);
 		for (Route route : routesForRouteType.values()) {
 			if (route.matches(routePath)) {
 				return route;
 			}
 		}
 		return null;
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T extends Action> Object resolveAction(final String routePath, final RouteType routeType, final HttpServletRequest req, final HttpServletResponse resp, final Route route, final T action) {
 		Map<String, String> pathVars = route.getPathVars(routePath);
 		ActionResolver<T> actionResolver = (ActionResolver<T>) actionResolvers.get(action.getClass());
 		Object resolve = actionResolver.resolve(action, routeType, req, resp, pathVars);
 		return resolve;
 	}
 
 	private static final String routeDisplayFormat = "%s\n";
 
 	public String listRoutes() {
		Set<String> allRoutes = new HashSet<String>();
 		for (Map<String, Route> routeEntries : routes.values()) {
 			allRoutes.addAll(routeEntries.keySet());
 		}
		List<String> allRouteNames = list(allRoutes);
		Collections.sort(allRouteNames);
 
 		StringBuilder sb = new StringBuilder();
		for (String route : allRouteNames) {
 			for (RouteType routeType : RouteType.all()) {
 				Map<String, Route> routesForType = routes.get(routeType);
 				if (routesForType.containsKey(route)) {
 					sb.append(String.format(routeDisplayFormat, routesForType.get(route)));
 				}
 			}
 		}
 		return sb.toString();
 	}
 
 	public Action createAction(String actionName) {
 		try {
 			for (ActionResolver<?> actionResolver : actionResolvers.values()) {
 				Action action = actionResolver.createActionIfPossible(actionName);
 				if (action != null) {
 					return action;
 				}
 			}
 			throw new ActionException("Failed to create an action for the route %s: No action resolver can resolve this action", actionName);
 		} catch (ActionException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new ActionException(e, "Failed to create an action for the route %s: %s", actionName, e.getMessage());
 		}
 	}
 
 	// TODO - This method should probably be externalised from this class, routes should theoretically be buildable however someone wants
 	public static List<Route> parseJsonRoutes(String source) {
 		try {
 			List<Route> routes = new ArrayList<Route>();
 			JsonProperties properties = new JsonProperties(source);
 			for (String route : properties.getKeys()) {
 				if (properties.is(route, String.class)) {
 					// simple route
 					String actionName = properties.getString(route);
 					routes.add(new Route(route, actionName, RouteType.GET));
 				} else if (properties.is(route, Map.class)) {
 					// complex route
 					Map<String, String> map = properties.getMap(route);
 					for (Map.Entry<String, String> routeEntry : map.entrySet()) {
 						RouteType routeType = RouteType.from(routeEntry.getKey());
 						if (routeType == null) {
 							throw new RouteNotFoundException("Unknown route type %s", routeEntry.getKey());
 						}
 						routes.add(new Route(route, routeEntry.getValue(), routeType));
 					}
 				}
 			}
 
 			return routes;
 		} catch (Exception e) {
 			throw new RouteNotFoundException(e, "Failed to parse routes: %s", e.getMessage());
 		}
 	}
 
 	public <A extends Action> void addActionResolver(Class<A> actionType, ActionResolver<A> actionResolver) {
 		actionResolvers.put(actionType, actionResolver);
 		Logger.debug("Added action resolver %s for actions of type %s", actionResolver.getClass().getSimpleName(), actionType);
 	}
 
 	private Map<RouteType, Map<String, Route>> createRoutesMap() {
 		Map<RouteType, Map<String, Route>> routesMap = new HashMap<RouteType, Map<String, Route>>();
 		for (RouteType type : RouteType.all()) {
 			routesMap.put(type, new LinkedHashMap<String, Route>());
 		}
 		return routesMap;
 	}
 }
