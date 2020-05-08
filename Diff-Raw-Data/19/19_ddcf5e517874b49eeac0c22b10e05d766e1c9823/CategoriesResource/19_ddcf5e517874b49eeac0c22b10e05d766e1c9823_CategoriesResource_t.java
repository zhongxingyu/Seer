 package resources;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import entities.grid.Category;
 import entities.grid.Subject;
 import extjs.FormResult;
 
 @Path("/categories")
 @Stateless
 public class CategoriesResource {
 	@PersistenceContext
 	private EntityManager em;
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public JSONObject getCategories() throws JSONException  {
 		Query query = em.createNamedQuery("findAllCategories");
 		
 		List<Category> categories = query.getResultList();
 		
 		JSONObject jsonData = new JSONObject();
 		JSONArray rows = new JSONArray();
 		
 		for(Category category : categories) {
 			JSONObject row = new JSONObject();
 			row.put("id", category.getId());
 			row.put("name", category.getName());
 			rows.put(row);
 		}
 		jsonData.put("rows", rows);
 		
 		return jsonData;
 	}
 	
 	@POST
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	@Produces(MediaType.APPLICATION_JSON)
 	public FormResult createCategory(final MultivaluedMap<String, String> formParameters) {
 		String name = formParameters.get("name").get(0);
 		
 		Category category = new Category(name);
 		
 		em.persist(category);
 		
 		return new FormResult(true);
 	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void createCategory(Category category) {		
		em.persist(category);
	}
 }
