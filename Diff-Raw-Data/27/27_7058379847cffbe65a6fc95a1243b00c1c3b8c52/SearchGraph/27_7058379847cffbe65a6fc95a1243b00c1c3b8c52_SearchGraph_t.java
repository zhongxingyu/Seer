 package de.anycook.graph;
 
 import java.util.HashSet;
 import java.util.List;
 
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import com.google.common.collect.Multimap;
 
 import de.anycook.utils.JsonpBuilder;
 import de.anycook.recipe.Recipe;
 import de.anycook.search.Search;
 import de.anycook.search.SearchResult;
 
 
 @Path("search")
 public class SearchGraph {
 	
 	@SuppressWarnings("unchecked")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 	public Response search(@QueryParam("callback") String callback, 
 			@QueryParam("tags") StringSet tags,
 			@QueryParam("ingredients") StringSet ingredients,
 			@QueryParam("terms") StringSet terms,
 			@QueryParam("category") String category,
 			@QueryParam("calorie") int calorie,
 			@QueryParam("skill") int skill,
 			@QueryParam("time") String time,
			@QueryParam("user") String user,
 			@DefaultValue("0") @QueryParam("start") int start,
 			@DefaultValue("10") @QueryParam("num") int num){
 		Search search = new Search();
 		if(tags!= null && !tags.isEmpty())
 			search.addTags(tags);
 		if(ingredients != null && !ingredients.isEmpty())
 			search.addZutaten(ingredients);
 		if(terms != null && !terms.isEmpty())
 			search.addTerms(terms);
 		search.setKategorie(category);
 		search.setCalorie(calorie);
 		search.setSkill(skill);
 		search.setTime(time);
		search.setUser(user);
 		
 		SearchResult result = search.search(start, num);
 		JSONObject json = new JSONObject();
 		JSONArray recipes = new JSONArray();
 		List<String> results = result.getResults();
 		for(String recipe : results){
 			recipes.add(Recipe.getJSONforSearch(recipe));
 		}
 		
 		json.put("size", result.getResultLength());
 		json.put("recipes", recipes);
 		
 		return Response.ok(JsonpBuilder.build(callback, json)).build();
 	}
 	
 	
 	/**
 	 * Get possible types for a query. Exact query is matched. 
 	 * @param query 
 	 * @param callback
 	 * @return
 	 */
 	@GET
 	@Path("validate")
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 	public Response validateSearch(@QueryParam("q") String query,
 			@QueryParam("callback") String callback){
 		if(query==null)
 			throw new WebApplicationException(400);
 			
 		query=query.toLowerCase();
 		Multimap<String, String> result = Search.validateSearch(query);			
 		return JsonpBuilder.buildResponse(callback,
 				JSONObject.toJSONString(result.asMap()));
 	}
 	
 	public static class StringSet extends HashSet<String>{
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public StringSet(String in) {
 			super();
 			if(in != null){
 				for(String split : in.split(","))
 					add(split);
 			}
 		}
 	}
 }
