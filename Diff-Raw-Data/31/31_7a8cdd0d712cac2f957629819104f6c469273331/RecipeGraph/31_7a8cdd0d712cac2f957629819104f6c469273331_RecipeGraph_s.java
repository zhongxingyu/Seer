 package de.anycook.graph;
 
 import java.net.URISyntaxException;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.log4j.Logger;
 import org.json.simple.JSONObject;
 import de.anycook.tag.Tag;
 import de.anycook.user.User;
 import de.anycook.utils.JsonpBuilder;
 import de.anycook.utils.enumerations.ImageType;
 import de.anycook.ingredient.Ingredient;
 import de.anycook.newrecipe.NewRecipe;
 import de.anycook.recipe.Recipe;
 import de.anycook.session.Session;
 import de.anycook.step.Step;
 
 
 @Path("/recipe")
 public class RecipeGraph {
 	Logger logger = Logger.getLogger(getClass());
 
 	@SuppressWarnings("unchecked")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 	public Response getAll(@QueryParam("userid") Integer userid,
 			@QueryParam("callback") String callback){
 		JSONObject json = new JSONObject();
 		
 		List<String> recipes;
 		if(userid != null)
 			recipes = Recipe.getRecipenamesfromUser(userid);
 		else
 			recipes = Recipe.getAll();
 		
 		json.put("names", recipes);
 		json.put("total", recipes.size());
 		return Response.ok(JsonpBuilder.build(callback, json)).build();
 	}
 	
 	/**
 	 * Number of recipes
 	 * @param callback
 	 * @return
 	 */
 	@GET
 	@Path("number")
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 	public Response getNum(@QueryParam("callback") String callback){
 		return JsonpBuilder.buildResponse(callback, Recipe.getTotal());
 	}
 	
 	/**
 	 * returns the recipe of the day
 	 * @param callback
 	 * @return
 	 */
 	@GET
 	@Path("oftheday")
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 	public Response getRecipeOfTheDay(@QueryParam("callback") String callback){
 		String recipeOfTheDay = Recipe.getTagesRezept();		
 		recipeOfTheDay = org.codehaus.jettison.json.JSONObject.quote(recipeOfTheDay);		
 		return JsonpBuilder.buildResponse(callback, recipeOfTheDay);
 	}
 	
 	@GET
 	@Path("{recipename}")
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 	public Response getRecipe(@PathParam("recipename") String recipeName, 
 			@QueryParam("callback") String callback){
 		
 		Recipe recipe;
 		if(recipeName.equals("random")){
 			recipeName = Recipe.getRandomRecipe();
 		}
 		
 		recipe = Recipe.init(recipeName);
 		if(recipe == null)
 			throw new WebApplicationException(400);
 		return JsonpBuilder.buildResponse(callback, recipe);
 	}
 	
 	@GET
 	@Path("{recipename}/ingredients")
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 	public Response getRecipeIngredients(@PathParam("recipename") String recipeName,
 			@QueryParam("callback") String callback){
 		List<Ingredient> ingredients = Ingredient.loadByRecipe(recipeName);
 		return JsonpBuilder.buildResponse(callback, ingredients);
 	}
 	
 	@GET
 	@Path("{recipename}/tags")
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 	public Response getRecipeTags(@PathParam("recipename") String recipeName,
 			@QueryParam("callback") String callback){
 		List<String> tags = Tag.loadTagsFromRecipe(recipeName);
 		return JsonpBuilder.buildResponse(callback, tags);
 	}
 	
 	@GET
 	@Path("{recipename}/steps")
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 	public Response getRecipeSteps(@PathParam("recipename") String recipeName,
 			@QueryParam("callback") String callback){
 		List<Step> steps = Step.loadRecipeSteps(recipeName);
 		return JsonpBuilder.buildResponse(callback, steps);
 	}
 	
 	
 	@GET
 	@Path("{recipename}/{versionid}")
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 	public Response getVersion(@PathParam("recipename") String recipeName,
 			@PathParam("versionid") int versionid,
 			@QueryParam("callback") String callback){
 		Recipe recipe = Recipe.init(recipeName, versionid);
 		return JsonpBuilder.buildResponse(callback, recipe);
 	}
 	
 	@GET
 	@Path("{recipename}/image")
 	@Produces("image/png")
 	public Response getImage(@PathParam("recipename") String recipeName,
			@DefaultValue("small") @QueryParam("type") String typeString, 
			@QueryParam("callback") String callback){
 		ImageType type = ImageType.valueOf(typeString.toUpperCase());
 		try {
 			return Response.temporaryRedirect(Recipe.getRecipeImage(recipeName, type)).build();
 		} catch (URISyntaxException e) {
 			logger.error(e);
 			throw new WebApplicationException(400);
 		}
 	}
 	
 	@GET
 	@Path("{recipename}/schmeckt")
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 	public Response checkSchmeckt(@PathParam("recipename") String recipeName,
 			@Context HttpServletRequest request,
 			@QueryParam("callback") String callback){
 		Session session = Session.init(request.getSession());
 		session.checkLogin();
 		boolean schmeckt = session.checkSchmeckt(recipeName);
 		return JsonpBuilder.buildResponse(callback, schmeckt);
 		
 	}
 	
 	@PUT
 	@Path("{recipename}/schmeckt")
 	public void schmeckt(@PathParam("recipename") String recipeName,
 			@Context HttpHeaders hh,
 			@Context HttpServletRequest request){
 		
 		Session session = Session.init(request.getSession());
 		session.checkLogin(hh.getCookies());
 		boolean schmeckt = session.checkSchmeckt(recipeName);
 		if(!schmeckt)
 			session.makeSchmeckt(recipeName);
 		
 	}
 	
 	@DELETE
 	@Path("{recipename}/schmeckt")
 	public void schmecktNicht(@PathParam("recipename") String recipeName,
 			@Context HttpHeaders hh,
 			@Context HttpServletRequest request){
 		Session session = Session.init(request.getSession());
 		session.checkLogin(hh.getCookies());
 		boolean schmeckt = session.checkSchmeckt(recipeName);
 		if(schmeckt)
 			session.removeSchmeckt(recipeName);
 		
 	}
 	
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response saveRecipe(@Context HttpHeaders hh,
 			@Context HttpServletRequest request,			
 			NewRecipe newRecipe){
 		logger.info("want to save recipe");
 		Session  session = Session.init(request.getSession());
 		session.checkLogin(hh.getCookies());
 		
 		User user = session.getUser();
 		
 		if(newRecipe == null)
 			throw new WebApplicationException(400);
 		
 		
 		if(!newRecipe.save(user.getId()))
 			throw new WebApplicationException(400);
 		
 		return Response.ok("true").build();			
 		
 		
 	}
 		
 		
 //		logger.debug("recipe:"+recipeData);
 //		Session session = Session.init(request.getSession());
 //		Map<String, Cookie> cookies = hh.getCookies();
 //		JSONParser parser = new JSONParser();
 //		JSONObject json = null;
 //		
 //		if(userid != -1){
 //			if(session.checkLogin(cookies)){
 //				 User user = session.getUser();
 //				 if(user.getId() != userid)
 //					 throw new WebApplicationException(401);
 //			}
 //		}
 //		
 //		if(recipeData != null){			
 //			try {
 //				json = (JSONObject) parser.parse(recipeData);
 //			}catch (ParseException e) {
 //	//			throw new WebApplicationException(Response.status(400).entity(e.getMessage()).build());
 //			}
 //			
 //			if(json != null){
 //				NewRecipe newRecipe;
 //				try {
 //					newRecipe = NewRecipe.initWithJSON(recipeName,json);
 //				} catch (ParseException | NewRecipeException e) {
 //					throw new WebApplicationException(Response.status(400).entity(e.getMessage()).build());
 //				}
 //				newRecipe.saveNewVersion();
 ////				if(json.containsKey("mongoid"))
 ////					CouchDB.delete(json.get("mongoid").toString(), 
 ////							Integer.parseInt(json.get("userid").toString()));
 //			}
 //		}
 //		
 //		if(tags != null){
 //			try {
 //				JSONArray tagsJSON = (JSONArray)parser.parse(tags);
 //				
 //				for(int i = 0; i<tagsJSON.size(); i++){
 //					Recipe.suggestTag(recipeName, tagsJSON.get(i).toString(), userid);
 //				}
 //			} catch (ParseException e) {
 //				throw new WebApplicationException(Response.status(400).entity(e.getMessage()).build());
 //			}
 //			
 //		}
 //		return Response.ok("true").build();			
 //		
 //		
 //	}
 }
