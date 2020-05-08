 package hdm.stuttgart.esell.router;
 
 import static spark.Spark.delete;
 import static spark.Spark.get;
 import static spark.Spark.post;
 import static spark.Spark.put;
 import hdm.stuttgart.esell.Model.Petition;
 
 import java.io.File;
import java.io.FileInputStream;
 import java.net.URL;
 import java.sql.Date;
 
 import spark.Request;
 import spark.Response;
 import spark.Route;
import spark.utils.IOUtils;
 
 import com.google.gson.Gson;
 
 /**
  * CRUD Routen fr Kaufgesuche (engl. Petition)
  * (GET /petition/:id, POST /petition/, PUT /petition/:id, DELETE /petition/:id)
  *
  */
 
 public class PetitionRoutes {
 	public PetitionRoutes() {
 		
 		// Gibt Informationen eines Kaufgesuchs zurck
 		get(new Route("/petition/:id") {
 			@Override
 			public Object handle(Request req, Response res) {
 				res.type( "application/json" );
 				
 				try {
 					int id = Integer.parseInt(req.params("id"));
 					Petition petition = Petition.getPetition(id);
 
 					res.status(Router.HTTP_OKAY);
 					return petition.getJson();
 				} catch (Exception e) {
 					res.status(Router.HTTP_SERVER_ERROR);
 					return e.getMessage();
 				}
 				
 			}
 		});
 		
 		// Speichert eine neues Kaufgesuch
 		post(new Route("/petition") {
 			@Override
 			public Object handle(Request req, Response res) {
 				res.type( "application/json" );
 				
 				try {
 					Gson gson = new Gson();
 					Petition petition = gson.fromJson(req.body(), Petition.class);
 					
 					if(petition.getState() == null) {
 						petition.setState("Searching");
 					}
 					if(petition.getCreation() == null)
 					{
 						petition.setCreation(new Date(new java.util.Date().getTime()));
 					}
 					
 					petition.insert();
 					res.status(Router.HTTP_CREATED);
 					return petition.getJson();
 				}
 				catch(Exception e) {
 					res.status(Router.HTTP_SERVER_ERROR);
 					return e.getMessage();
 				}
 			}
 		});
 		
 		// Gibt das Bild eines Kaufgesuch zurck
 		get(new Route("/petition/:id/image") {
 			@Override
 			public Object handle(Request req, Response res) {
 				try {
 					int petitionId = Integer.parseInt(req.params("id"));
 					
 					FileHandler fileHandler = new FileHandler(petitionId);
 					File image = fileHandler.getImage();

 					res.status(Router.HTTP_OKAY);
 					res.type("image/jpeg");
					return IOUtils.toString(new FileInputStream(image));
 				} catch (Exception e) {
 					res.type( "application/json" );
 					res.status(Router.HTTP_SERVER_ERROR);
 					return e.getMessage();
 				}
 			}
 		});
 		
 		// Speichert das Bild zu einem Kaufgesuch
 		post(new Route("/petition/:id/image") {
 			@Override
 			public Object handle(Request req, Response res) {
 				res.type( "application/json" );
 				try {
 					int petitionId = Integer.parseInt(req.params("id"));
 					Petition petition = Petition.getPetition(petitionId);
 					
 					FileHandler fileHandler = new FileHandler(petitionId);
 					fileHandler.saveImgFromRequest(req);
 					petition.setImageURL(new URL("HTTP","localhost/",fileHandler.getFullPath()));
 					petition.update();
 
 					res.status(Router.HTTP_OKAY);
 					return petition.getJson();
 					//return "okay";
 				}
 				catch(Exception e) {
 					res.status(Router.HTTP_SERVER_ERROR);
 					return e.getMessage();
 				}
 			}
 		});
 		
 		// ndert das Bild zu einem Kaufgesuch
 		put(new Route("/petition/:id/image") {
 			@Override
 			public Object handle(Request req, Response res) {
 				res.type( "application/json" );
 				
 				try {
 					int petitionId = Integer.parseInt(req.params("id"));
 					Petition petition = Petition.getPetition(petitionId);
 					
 					FileHandler fileHandler = new FileHandler(petitionId);
 					fileHandler.saveImgFromRequest(req);
 
 					res.status(Router.HTTP_OKAY);
 					return petition.getJson();
 					//return "okay";
 				}
 				catch(Exception e) {
 					res.status(Router.HTTP_SERVER_ERROR);
 					return e.getMessage();
 				}
 			}
 		});
 		
 		// ndert ein Kaufgesuch
 		put(new Route("/petition/:id") {
 			@Override
 			public Object handle(Request req, Response res) {
 				res.type( "application/json" );
 				
 				try {
 					int id = Integer.parseInt(req.params("id"));
 					Petition petition = Petition.getPetition(id);
 					
 					Gson gson = new Gson();
 					Petition updatePetition = gson.fromJson(req.body(), Petition.class);
 					
 					if((Integer) updatePetition.getAmount() != null) petition.setAmount(updatePetition.getAmount());
 					if((Integer) updatePetition.getPrice() != null) petition.setPrice(updatePetition.getPrice());
 					if((Integer) updatePetition.getUserID() != null) petition.setUserID(updatePetition.getUserID());
 					if(updatePetition.getTitle() != null) petition.setTitle(updatePetition.getTitle());
 					if(updatePetition.getDescription() != null) petition.setDescription(updatePetition.getDescription());
 					if((Integer) updatePetition.getCategoryID() != null) petition.setCategoryID(updatePetition.getCategoryID());
 					if(updatePetition.getState() != null) petition.setState(updatePetition.getState());
 					if(updatePetition.getCreation() != null) petition.setCreation(updatePetition.getCreation());
 					
 					petition.update();
 					res.status(Router.HTTP_CREATED);
 					return petition.getJson();
 				}
 				catch(Exception e) {
 					res.status(Router.HTTP_SERVER_ERROR);
 					return e.getMessage();
 				}
 			}
 		});
 		
 		// Lscht ein Kaufgesuch
 		delete(new Route("/petition/:id") {
 			@Override
 			public Object handle(Request req, Response res) {
 				res.type( "application/json" );
 				
 				try {
 					int id = Integer.parseInt(req.params("id"));
 					Petition petition = Petition.getPetition(id);
 					petition.delete();
 					
 					res.status(Router.HTTP_OKAY);
 					return petition.getJson();
 				} catch (Exception e) {
 					res.status(Router.HTTP_SERVER_ERROR);
 					return e.getMessage();
 				}
 			}
 		});
 		
 	}
 }
