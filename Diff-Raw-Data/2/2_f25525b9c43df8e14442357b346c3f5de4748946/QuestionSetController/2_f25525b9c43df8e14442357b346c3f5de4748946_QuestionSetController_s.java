 package uk.ac.cam.sup.controllers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import org.hibernate.criterion.Order;
 import org.jboss.resteasy.annotations.Form;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.sup.form.QuestionSetAdd;
 import uk.ac.cam.sup.form.QuestionSetEdit;
 import uk.ac.cam.sup.form.QuestionSetFork;
 import uk.ac.cam.sup.models.Question;
 import uk.ac.cam.sup.models.QuestionSet;
 import uk.ac.cam.sup.models.Tag;
 import uk.ac.cam.sup.models.User;
 import uk.ac.cam.sup.queries.QuestionQuery;
 import uk.ac.cam.sup.queries.QuestionSetQuery;
 
 import com.google.common.collect.ImmutableMap;
 import com.googlecode.htmleasy.RedirectException;
 
 @Path("/sets")
 public class QuestionSetController extends GeneralController {
 	
 	private static Logger log = LoggerFactory.getLogger(QuestionSetController.class);
 	
 	@GET
 	@Path("/")
 	@Produces("application/json")
 	public Map<String,?> produceFilteredJSON (
 			@QueryParam("tags") String tags,
 			@QueryParam("owners") String users,
 			@QueryParam("star") boolean star,
 			@QueryParam("supervisor") Boolean supervisor,
 			@QueryParam("after") Long after,
 			@QueryParam("before") Long before,
 			@QueryParam("durmin") Integer minduration,
 			@QueryParam("durmax") Integer maxduration
 	) {
 		QuestionSetQuery query = QuestionSetQuery.all();
 		
 		if (tags != null) {
 			String[] tagstrings = tags.split(",");
 			List<Tag> tagset = new ArrayList<Tag>();
 			for (String t: tagstrings) {
 				tagset.add(new Tag(t));
 			}
 			query.withTags(tagset);
 		}
 		
 		if (users != null) {
 			String[] userstrings = users.split(",");
 			List<User> userset = new ArrayList<User>();
 			for (String u: userstrings) {
 				userset.add(new User(u));
 			}
 			query.withUsers(userset);
 		}
 		
 		if (star) {	query.withStar(); }
 		if (supervisor != null) {
 			if (supervisor) { query.bySupervisor(); }
 			else { query.byStudent(); }
 		}
 		
 		if (after != null) { query.after(new Date(after)); }
 		if (before != null) { query.before(new Date(before)); }
 		
 		if (minduration != null) { query.minDuration(minduration); }
 		if (maxduration != null) { query.maxDuration(maxduration); }
 		
 		return ImmutableMap.of("sets", query.maplist(false));
 	}
 	
 	@GET
 	@Path("/{id}")
 	@Produces("application/json")
 	public Map<String,Object> showSingleSet(@PathParam("id") int id) {
 		return QuestionSetQuery.get(id).toMap(false);
 	}
 	
 	@GET
 	@Path("/{id}/questions")
 	@Produces("application/json")
 	public Map<String,?> showSetsQuestions(@PathParam("id") int id) {
 		return ImmutableMap.of ("questions", QuestionSetQuery.get(id).getQuestionsAsMaps());
 	}
 	
 	@GET
 	@Path("/{id}/json")
 	@Produces("application/json")
 	public Map<String,Object> produceSingleSetJSON(@PathParam("id") int id) {
 		return QuestionSetQuery.get(id).toMap(false);
 	}
 	
 	@GET
 	@Path("/mysets")
 	@Produces("application/json")
 	public Map<?,?> produceMySets(@QueryParam("contains") Integer questionID){
 		
 		User user = getCurrentUser();
 		List<User> userlist = new ArrayList<User>();
 		userlist.add(user);
 		
 		QuestionSetQuery starredList = QuestionSetQuery.all().withUsers(userlist).withStar();
 		QuestionSetQuery nostarList = QuestionSetQuery.all().withUsers(userlist).withoutStar();
 		starredList.getCriteria().addOrder(Order.asc("name"));
 		nostarList.getCriteria().addOrder(Order.asc("name"));
 		
 		List<QuestionSet> resultSets = new ArrayList<QuestionSet>();
 		resultSets.addAll(starredList.list());
 		resultSets.addAll(nostarList.list());
 		
 		if(questionID == null) {
 			return ImmutableMap.of("sets", resultSets);
 		} else {
 			List<Map<String,?>> maplist = new ArrayList<Map<String,?>>();
 			log.debug("Trying to get all questionSets with those specially marked containing question " + questionID);
 			List<QuestionSet> haveQuestion = QuestionSetQuery.all().have(questionID).list();
 			for(QuestionSet set : resultSets) {
 				maplist.add(ImmutableMap.of("set", set, "containsQuestion", haveQuestion.contains(set)));
 			}
 			return ImmutableMap.of("maplist", maplist);
 		}
 	}
 
 	@GET
 	@Path("/mysets/limited")
 	@Produces("application/json")
 	public Map<String,?> produceSelectionOfMySets(
 			@QueryParam("contains") Integer qid,
 			@QueryParam("disp") Integer alreadyDisplayed,
 			@QueryParam("amount") Integer amount) {
 		
 		@SuppressWarnings("unchecked")
 		List<Map<String,?>> mySets = (List<Map<String,?>>) produceMySets(qid).get("maplist");
 		
 		if(mySets.size() <= alreadyDisplayed) {
			return ImmutableMap.of("maplist", null, "exhausted", true, "disp", alreadyDisplayed);
 		} else if(mySets.size() <= alreadyDisplayed + amount) {
 			return ImmutableMap.of(
 					"maplist", mySets.subList(alreadyDisplayed, mySets.size()),
 					"exhausted", true,
 					"disp", mySets.size()
 			);
 		} else {
 			return ImmutableMap.of(
 					"maplist", mySets.subList(alreadyDisplayed, alreadyDisplayed + amount),
 					"exhausted", false,
 					"disp", alreadyDisplayed + amount
 			);
 		}
 	}
 	
 	@GET
 	@Path("/mysets/qlimited")
 	@Produces("application/json")
 	public Map<String,List<QuestionSet>> produceOnlySetsWithQuestion(@QueryParam("qid") Integer qid) {
 		List<User> userList = new ArrayList<User>();
 		userList.add(getCurrentUser());
 		
 		QuestionSetQuery qsq = QuestionSetQuery.all().withUsers(userList).have(qid);
 		qsq.getCriteria().addOrder(Order.asc("name"));
 		
 		return ImmutableMap.of("sets", qsq.list());
 	}
 	
 	@GET
 	@Path("/remove")
 	@Produces("application/json")
 	public boolean removeQuestionFromSet(@QueryParam("qid") int qid, @QueryParam("sid") int sid) {
 		 try{
 			 QuestionSet qs = QuestionSetQuery.get(sid);
 			 qs.removeQuestion(QuestionQuery.get(qid));
 			 qs.update();
 		 } catch(Exception e) {
 			 return false;
 		 }
 		 return true;
 	}
 	
 	@GET
 	@Path("/add")
 	@Produces("application/json")
 	public boolean addQuestionFromSet(@QueryParam("qid") int qid, @QueryParam("sid") int sid) {
 		try{
 			 QuestionSet qs = QuestionSetQuery.get(sid);
 			 qs.addQuestion(QuestionQuery.get(qid));
 			 qs.update();
 		 } catch(Exception e) {
 			 return false;
 		 }
 		 return true;
 	}
 	
 	@POST
 	@Path("/fork")
 	public void forkSet(@Form QuestionSetFork form) throws Exception {
 		form.validate().parse();
 		for (Question q: form.getQuestions()) {
 			form.getTarget().addQuestion(q);
 		}
 		form.getTarget().update();
 		throw new RedirectException("/app/#sets/"+form.getTarget().getId());
 	}
 	
 	@POST
 	@Path("/save")
 	public void saveSet(@Form QuestionSetAdd form) throws Exception {
 		form.validate().parse();
 		User author = getCurrentUser();
 		QuestionSet qs = new QuestionSet(author);
 		qs.setName(form.getName());
 		qs.setPlan(form.getPlan());
 		qs.save();
 		
 		throw new RedirectException("/app/#sets/"+qs.getId());
 	}
 	
 	@POST
 	@Path("/update")
 	public void updateSet(@Form QuestionSetEdit form) throws Exception {
 		form.validate().parse();
 		
 		QuestionSetQuery.get(form.getSetId()).edit(form);
 		
 		throw new RedirectException("/app/#sets/"+form.getSetId());
 	}
 	
 	@GET
 	@Path("/{id}/togglestar")
 	@Produces("application/json")
 	public Map<String,?> toggleStar(@PathParam("id") int id) {
 		QuestionSet qs = QuestionSetQuery.get(id);
 		qs.toggleStarred();
 		qs.update();
 		
 		return ImmutableMap.of("setid", id, "starred", qs.isStarred());
 	}
 	
 }
