 package hdm.stuttgart.esell.router;
 
 import static spark.Spark.get;
 import hdm.stuttgart.esell.Model.Category;
 import hdm.stuttgart.esell.Model.Petition;
 import spark.Request;
 import spark.Response;
 import spark.Route;
 
 /**
  * Routen fr Suchen
  * 		
  * Alle Kaufgesuche eines Nutzers: GET /user/:id/petitions
  * Alle Kaufgesuche einer Kategorie: GET /category/:catId/petitions
  * Alle Kaufgesuche: GET /petitions
  * Alle Kategorien: GET /categories
  * (Alle Nutzer: GET /users)
  */
 
 public class SearchRoutes {
 	public SearchRoutes() {
 		
 		// Alle Kaufgesuche eines Nutzers
 		get(new Route("/user/:id/petitions") {
 			@Override
 			public Object handle(Request req, Response res) {
 				res.type( "application/json" );
 				
 				try {
 					int userId = Integer.parseInt(req.params("id"));
 					String order = req.queryParams("order");
 					String start = req.queryParams("start");
 					String limit = req.queryParams("limit");
 					
 					if(order == null) order = "id";
 					if(start == null) start = "0";
 					if(limit == null) limit = "100";
 					
 					Petition.PetitionList list = Petition.getPetitionListByUser(userId, order, Integer.parseInt(start), Integer.parseInt(limit));
 					return list.getJson();
 				} catch (Exception e) {
 					res.status(Router.HTTP_SERVER_ERROR);
 					return e.getMessage();
 				}
 			}
 		});
 		
 		// Alle Kaufgesuche einer Kategorie
 		get(new Route("/category/:catId/petitions") {
 			@Override
 			public Object handle(Request req, Response res) {
 				res.type( "application/json" );
 				
 				try {
					
					int catId = Integer.parseInt(req.params("catid"));
 					String order = req.queryParams("order");
 					String start = req.queryParams("start");
 					String limit = req.queryParams("limit");
 					
 					if(order == null) order = "id";
 					if(start == null) start = "0";
 					if(limit == null) limit = "100";
 					
 					Petition.PetitionList list = Petition.getPetitionListByCategory(catId, order, Integer.parseInt(start), Integer.parseInt(limit));
 					return list.getJson();
 				} catch (Exception e) {
					e.printStackTrace();
 					res.status(Router.HTTP_SERVER_ERROR);
 					return e.getMessage();
 				}
 			}
 		});
 		
 		// Alle Kaufgesuche
 		get(new Route("/petitions") {
 			@Override
 			public Object handle(Request req, Response res) {
 				res.type( "application/json" );
 				
 				try {
 					String order = req.queryParams("order");
 					String start = req.queryParams("start");
 					String limit = req.queryParams("limit");
 					
 					if(order == null) order = "id";
 					if(start == null) start = "0";
 					if(limit == null) limit = "100";
 					
 					Petition.PetitionList list = Petition.getPetitionList(order, Integer.parseInt(start), Integer.parseInt(limit));
 					return list.getJson();
 				} catch (Exception e) {
 					res.status(Router.HTTP_SERVER_ERROR);
 					return e.getMessage();
 				}
 			}
 		});
 		
 		// Alle Kategorien
 		get(new Route("/categories") {
 			@Override
 			public Object handle(Request req, Response res) {
 				res.type( "application/json" );
 				
 				try {
 					String order = req.queryParams("order");
 					String start = req.queryParams("start");
 					String limit = req.queryParams("limit");
 					
 					if(order == null) order = "id";
 					if(start == null) start = "0";
 					if(limit == null) limit = "100";
 					
 					Category.CategoryList list = Category.getCategoryList();
 				
 					return list.getJson();
 				} catch (Exception e) {
 					res.status(Router.HTTP_SERVER_ERROR);
 					return e.getMessage();
 				}
 			}
 		});
 		
 	}
 }
