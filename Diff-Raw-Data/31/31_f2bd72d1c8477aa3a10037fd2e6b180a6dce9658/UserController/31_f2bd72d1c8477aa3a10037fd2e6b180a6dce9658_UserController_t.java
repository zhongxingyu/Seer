 package uk.ac.cam.sup.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import uk.ac.cam.cl.dtg.ldap.LDAPObjectNotFoundException;
 import uk.ac.cam.cl.dtg.ldap.LDAPQueryManager;
 import uk.ac.cam.cl.dtg.ldap.LDAPUser;
 import uk.ac.cam.sup.exceptions.ModelNotFoundException;
 import uk.ac.cam.sup.exceptions.QueryAlreadyOrderedException;
 import uk.ac.cam.sup.models.User;
 import uk.ac.cam.sup.queries.QuestionQuery;
 import uk.ac.cam.sup.queries.QuestionSetQuery;
 import uk.ac.cam.sup.queries.UserQuery;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 
 @Path("/users")
 public class UserController extends GeneralController {
 	
 	//private static Logger log = LoggerFactory.getLogger(UserController.class);
 	
 	@GET
 	@Path("/{crsid}")
 	@Produces("application/json")
	public Map<String,?> produceUserContent(@PathParam("crsid") String crsid){
		Builder<String,Object> builder = ImmutableMap.builder();
		
		try {
			builder.putAll(fetchUserQuestions(crsid, 1, 25))
				.putAll(fetchUserSets(crsid, 1, 25))
				.put("success", true)
				.put("crsid", crsid);
		} catch (QueryAlreadyOrderedException | ModelNotFoundException e) {
			builder.put("success", false)
				.put("error", e.getMessage());
		}
		
		return builder.build();
 	}
 	
 	@GET
 	@Path("/{crsid}/sets")
 	@Produces("application/json")
 	public Map<String,?> produceUserSets(@PathParam("crsid") String crsid,
 			@QueryParam("page") Integer page, @QueryParam("amount") Integer amount) {
 		
 		if(page == null || page < 1){
 			page = 1;
 		}
 		if(amount == null || amount < 1){
 			amount = 1;
 		}
 		
 		Builder<String,Object> builder = ImmutableMap.builder();
 		try {
 			builder.putAll(fetchUserSets(crsid, page, amount))
 				.put("success", true)
 				.put("crsid", crsid);
 		} catch (QueryAlreadyOrderedException | ModelNotFoundException e) {
 			builder.put("success", false)
 				.put("error", e.getMessage());
 		}
 		
 		return builder.build();
 	}
 	
 	@GET
 	@Path("/{crsid}/questions")
 	@Produces("application/json")
 	public Map<String,?> produceUserQuestions(
 			@PathParam("crsid") String crsid,
 			@QueryParam("page") Integer page, @QueryParam("amount") Integer amount
 			){
 		if(page == null || page < 1){
 			page = 1;
 		}
 		if(amount == null || amount < 1){
 			amount = 1;
 		}
 		
 		Builder<String,Object> builder = ImmutableMap.builder();
 		try {
 			builder.putAll(fetchUserQuestions(crsid, page, amount))
 				.put("success", true)
 				.put("crsid", crsid);
 		} catch (QueryAlreadyOrderedException e) {
 			builder.put("success", false)
 				.put("error", e.getMessage());
 		}
 		
 		return builder.build();
 	}
 	
 	@GET
 	@Path("/me")
 	@Produces("application/json")
 	public Map<String,?> produceMyContent() {
		return produceUserContent(getCurrentUserID());
 	}
 	
 	private Map<String,?> fetchUserQuestions(String crsid, int page, int amount) throws QueryAlreadyOrderedException {
 		List<String> users = new ArrayList<String>();
 		users.add(crsid);
 		
 		QuestionQuery qq = QuestionQuery.all().withUserIDs(users);
 		int totalQuestions = qq.size();
 		
 		List<Map<String,?>> questions = qq.maxResults(amount).offset(amount * (page-1)).maplist();
 		return ImmutableMap.of(
 				"questions", questions,
 				"totalQuestions", totalQuestions,
 				"questionsPage", page,
 				"questionsAmount", amount
 		);
 	}
 	
 	private Map<String,?> fetchUserSets(String crsid, int page, int amount) 
 			throws QueryAlreadyOrderedException, ModelNotFoundException {
 		List<String> users = new ArrayList<String>();
 		users.add(crsid);
 		
 		QuestionSetQuery qsq = QuestionSetQuery.all().withUser(UserQuery.get(crsid));
 		int totalSets = qsq.size();
 		
 		List<Map<String,?>> questions = qsq.maxResults(amount).offset(amount * (page-1)).maplist();
 		return ImmutableMap.of(
 				"sets", questions,
 				"totalSets", totalSets,
 				"setsPage", page,
 				"setsAmount", amount
 		);
 	}
 	
 	@POST
 	@Path("/autocomplete/{amount}")
 	@Produces("application/json")
 	public List<Map<String,String>> produceUsersWith(@FormParam("q") String st, @PathParam("amount") Integer amount){
 		if(amount == null){amount = 10;}
 		
 		try{ 
 			List<User> users = UserQuery.all().idStartsWith(st).maxResults(amount).list();
 			List<Map<String,String>> crsidResults = new ArrayList<Map<String,String>>();
 			List<Map<String,String>> surnameResults = new ArrayList<Map<String,String>>();
 
 
 			for(User u : users){
 				try {
 					LDAPUser user = LDAPQueryManager.getUser(u.getId());
 					crsidResults.add(ImmutableMap.of("value", u.getId(), "crsid", u.getId(), "name", user.getcName()));
 				} catch(LDAPObjectNotFoundException e){
 					crsidResults.add(ImmutableMap.of("value", u.getId(), "crsid", u.getId(), "name", "Annonymous"));
 				}
 			}
 
 			int offset = 0;
 			String surname;
 			while(surnameResults.size() + crsidResults.size() < amount && (users.size() > 0 || offset == 0)){
 				users = UserQuery.all().maxResults(4 * amount).offset(offset).list();
 				offset += 40;
 
 				for(User u: users){
 					try {
 						LDAPUser user = LDAPQueryManager.getUser(u.getId());
 						surname = user.getSurname();
 						if(surname.toLowerCase().startsWith(st.toLowerCase()) && !crsidResults.contains(u)){
 							surnameResults.add(ImmutableMap.of("value", u.getId(), "crsid", u.getId(), "name", user.getcName()));
 						}
 					} catch(LDAPObjectNotFoundException e){
 						// Don't do anything if something goes wrong in previous step, as it is unknown if the last name starts with st
 					}
 
 					if(surnameResults.size() + crsidResults.size() >= amount){
 						break;
 					}
 				}
 			}
 			List<Map<String,String>> results = new ArrayList<Map<String,String>>();
 			results.addAll(crsidResults);
 			results.addAll(surnameResults);
 			return results;
 		} catch(Exception e){
 			List<Map<String,String>> results = new ArrayList<Map<String,String>>();
 			results.add(ImmutableMap.of("value", "Error: " + e.getClass() + "\nMessage: " +e.getMessage()));
 			return results;
 		}
 	}
 	
 }
