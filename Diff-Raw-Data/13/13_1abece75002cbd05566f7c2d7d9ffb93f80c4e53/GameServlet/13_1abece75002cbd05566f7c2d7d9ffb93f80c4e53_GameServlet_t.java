 package com.csoft.clinelympics;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.http.*;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.Filter;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.FilterPredicate;
 
 @SuppressWarnings("serial")
 public class GameServlet extends HttpServlet {
     
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 	throws IOException {	
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 		
 		Query query = new Query(Game.entityKind, new Game().getGameKey());
 		Filter isGame = new FilterPredicate(Game.gameNameName, FilterOperator.EQUAL, req.getParameter(Game.gameNameName).trim());
 		query.setFilter(isGame);
 	    List<Entity> games = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
 	    
 	    if (games.size() == 0) {
			int newID = 1;
	    	query = new Query(Game.entityKind, new Game().getGameKey()).addSort(Game.gameIDName, Query.SortDirection.DESCENDING);
 		    games = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
		    if (games.size() > 0) {
		    	newID = ((Long) games.get(0).getProperty(Game.gameIDName)).intValue() + 1;
		    }
 			Game game = new Game(newID, 
 					req.getParameter(Game.gameNameName).trim(), 
 					Boolean.parseBoolean(req.getParameter(Game.scoreTypeName).trim()));
 	
 			datastore.put(game.createEntity());
	    } else {
	    	if (games.size() > 1) System.out.println("Multiple games with name " + req.getParameter(Game.gameNameName));
	    	games.get(0).setProperty(Game.scoreTypeName, Boolean.parseBoolean(req.getParameter(Game.scoreTypeName).trim()));
	    	datastore.put(games.get(0));
 	    }
 		resp.sendRedirect("/games.jsp");
 	}
 }
