 package uk.ac.cam.sup.controllers;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import org.jboss.resteasy.annotations.Form;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.sup.form.QuestionAdd;
 import uk.ac.cam.sup.form.QuestionEdit;
 import uk.ac.cam.sup.models.Question;
 import uk.ac.cam.sup.models.QuestionSet;
 import uk.ac.cam.sup.models.Tag;
 import uk.ac.cam.sup.models.User;
 import uk.ac.cam.sup.ppdloader.PPDLoader;
 import uk.ac.cam.sup.queries.QuestionQuery;
 import uk.ac.cam.sup.queries.QuestionSetQuery;
 import uk.ac.cam.sup.queries.TagQuery;
 import uk.ac.cam.sup.util.SearchTerm;
 
 import com.google.common.collect.ImmutableMap;
 import com.googlecode.htmleasy.RedirectException;
 
 @Path("/q")
 public class QuestionController extends GeneralController {
 	private static Logger log = LoggerFactory
 			.getLogger(QuestionController.class);
 
 	@GET
 	@Path("/search")
 	// @ViewWith("/soy/search.main")
 	@Produces("application/json")
 	public Map<String, ?> searchQuestionsView(@QueryParam("tags") String tags,
 			@QueryParam("owners") String owners,
 			@QueryParam("star") Boolean star,
 			@QueryParam("supervisor") Boolean supervisor,
 			@QueryParam("after") Long after, @QueryParam("before") Long before,
 			@QueryParam("usagemin") Integer usageMin,
 			@QueryParam("usagemax") Integer usageMax,
 			@QueryParam("parent") Integer parentId,
 			@QueryParam("durmax") Integer durMax,
 			@QueryParam("durmin") Integer durMin) {
 
 		SearchTerm st = new SearchTerm(tags, owners, star, supervisor, after,
 				before, usageMin, usageMax, parentId, durMax, durMin);
 		List<?> filteredQuestions = getFilteredQuestions(st);
 
 		return ImmutableMap.of("questions", filteredQuestions, "st", st);
 		// return ImmutableMap.of("st", st);
 	}
 
 	@GET
 	@Path("/json")
 	@Produces("application/json")
 	public List<?> produceFilteredJSON(@QueryParam("tags") String tags,
 			@QueryParam("owners") String owners,
 			@QueryParam("star") Boolean star,
 			@QueryParam("supervisor") Boolean supervisor,
 			@QueryParam("after") Long after, @QueryParam("before") Long before,
 			@QueryParam("usagemin") Integer usageMin,
 			@QueryParam("usagemax") Integer usageMax,
 			@QueryParam("parent") Integer parentId,
 			@QueryParam("durmax") Integer durMax,
 			@QueryParam("durmin") Integer durMin) {
 
 		SearchTerm st = new SearchTerm(tags, owners, star, supervisor, after,
 				before, usageMin, usageMax, parentId, durMax, durMin);
 
 		return getFilteredQuestions(st);
 	}
 
 	private List<?> getFilteredQuestions(SearchTerm st) {
 		log.debug("Getting new QuestionQuery");
 		QuestionQuery qq = QuestionQuery.all();
 
 		log.debug("Filtering for tags");
 		if (st.getTags() != null && !st.getTags().equals("")) {
 			List<String> lTags = Arrays.asList(st.getTags().split(","));
 			qq.withTagNames(lTags);
 		}
 
 		log.debug("Filtering for owners");
 		if (st.getOwners() != null && !st.getOwners().equals("")) {
 			List<String> lUsers = Arrays.asList(st.getOwners().split(","));
 			qq.withUserIDs(lUsers);
 		}
 
 		log.debug("Filtering for star, role...");
 		if (st.getStar() != null && st.getStar()) {
 			qq.withStar();
 		}
 		if (st.getStar() != null && !st.getStar()) {
 			qq.withoutStar();
 		}
 		if (st.getSupervisor() != null && st.getSupervisor()) {
 			qq.bySupervisor();
 		}
 		if (st.getSupervisor() != null && !st.getSupervisor()) {
 			qq.byStudent();
 		}
 
 		log.debug("Filtering for date...");
 		if (st.getAfter() != null) {
 			qq.after(new Date(st.getAfter()));
 		}
 		if (st.getBefore() != null) {
 			qq.before(new Date(st.getBefore()));
 		}
 
 		log.debug("Filtering for usages");
 		if (st.getUsageMin() != null) {
 			qq.minUsages(st.getUsageMin());
 		}
 		if (st.getUsageMax() != null) {
 			qq.maxUsages(st.getUsageMax());
 		}
 
 		log.debug("Fileting for parentID");
 		if (st.getParentId() != null) {
 			qq.withParent(st.getParentId());
 		}
 
 		log.debug("Filtering for duration");
 		if (st.getDurMax() != null) {
 			qq.maxDuration(st.getDurMax());
 		}
 		if (st.getDurMin() != null) {
 			qq.minDuration(st.getDurMin());
 		}
 
 		// TODO: check whether current user is a supervisor
 		// and shadow the data appropriately
 		return qq.maplist(false);
 	}
 
 	@GET
 	@Path("/{id}/json")
 	@Produces("application/json")
 	public Map<String, Object> produceSingleQuestionJSON(@PathParam("id") int id) {
 		return QuestionQuery.get(id).toMap(false);
 	}
 
 	@GET
 	@Path("/{id}")
 	@Produces("application/json")
 	public Map<String, Map<String, Object>> produceSingleQuestionJSONAsSingleObject(
 			@PathParam("id") int id) {
 		return ImmutableMap.of("question", QuestionQuery.get(id).toMap(false));
 	}
 
 	@POST
 	@Path("/update")
 	public void editQuestion(@Form QuestionEdit qe) {
 		User editor = getCurrentUser();
 
 		try {
 			qe.validate();
 		} catch (Exception e) {
 			throw new RedirectException("/q/error/?msg=" + e.getMessage());
 		}
 
 		Question q = QuestionQuery.get(qe.getId());
 		q = q.edit(editor, qe);
 
 		throw new RedirectException("/app/#sets/" + qe.getSetId());
 	}
 
 	@POST
 	@Path("/save")
 	public void addQuestion(@Form QuestionAdd qa) {
 		User author = getCurrentUser();
 
 		try {
 			qa.validate();
 		} catch (Exception e) {
 			throw new RedirectException("/q/error/?msg=" + e.getMessage());
 		}
 
 		Question q = new Question(author);
 		q.setContent(qa.getContent());
 		q.setNotes(qa.getNotes());
 		q.setExpectedDuration(qa.getExpectedDuration());
 		q.save();
 		QuestionSet qs = QuestionSetQuery.get(qa.getSetId());
 		qs.addQuestion(q);
 		qs.update();
 
 		throw new RedirectException("/app/#sets/" + qa.getSetId());
 	}
 
 	@GET
 	@Path("/{id}/edit/{setid}")
 	@Produces("application/json")
 	public Map<?, ?> showEditForm(@PathParam("id") int id,
 			@PathParam("setid") int setId) {
 		Question q = QuestionQuery.get(id);
 
 		Map<String, Object> r = new HashMap<String, Object>();
 		r.put("id", q.getId());
 		r.put("content", q.getContent().getData());
 		r.put("notes", q.getNotes().getData());
 		r.put("setId", setId);
 		r.put("expectedDuration", q.getExpectedDuration());
 
 		return r;
 	}
 
 	@GET
 	@Path("/add/{setid}")
 	@Produces("application/json")
 	public Map<?, ?> showAddForm(@PathParam("setid") int setId) {
 		Map<String, Object> r = new HashMap<String, Object>();
 		r.put("setId", setId);
 
 		return r;
 	}
 
 	@GET
 	@Path("/pastpapers")
 	@Produces("application/json")
 	public Set<Question> producePastPapers() throws Exception {
 		return PPDLoader.loadAllQuestions();
 	}
 
 	@POST
 	@Path("/tagsnotin")
 	@Produces("application/json")
 	public List<Map<String, String>> getTagsNotInQuestion(String strInput) {
 		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
 		
 		int equPos = strInput.indexOf("=");
 		if (equPos < 0) {
 			return results;
 		}
 		int qid = Integer.parseInt(strInput.substring(0, equPos));
 		String strTagPart = strInput.substring(equPos + 1).replace("+", " ");
 
 		log.debug("Trying to get all tags containing " + strTagPart
 				+ " which are NOT in question " + qid);
 
 		List<Tag> tags = TagQuery.all().notContainedIn(QuestionQuery.get(qid))
 				.contains(strTagPart).list();
 		

 		for (Tag tag : tags) {
 			results.add(ImmutableMap.of("name", tag.getName()));
 		}
 
 		return results;
 	}
 
 	@GET
 	@Path("/addtags")
 	@Produces("application/json")
 	public Map<String,List<Tag>> addTags(@QueryParam("newtags") String newTags, @QueryParam("qid") int qid) {
 		// returns the tags added
 		if (newTags != null && newTags != "") {
 			
 			Question question = QuestionQuery.get(qid);
 			String[] newTagsArray = newTags.split(",");
 			List<Tag> result = new ArrayList<Tag>();
 			Set<Tag> existingTags = question.getTags();
 			Tag tmp;
 			
 			for (int i = 0; i < newTagsArray.length; i++) {
 				tmp = new Tag(newTagsArray[i]);
 				
 				if(!existingTags.contains(tmp) && tmp.getName() != null && tmp.getName() != "") {
 					result.add(tmp);
 				}
 				
 				log.debug("Trying to add tag " + tmp.getName() + " to question " + qid + "...");
 				question.addTag(tmp);
 			}
 			
 			log.debug("Trying to update question in data base...");
 			question.update();
 			
 			return ImmutableMap.of("tags", result);
 		}
 
 		return null;
 	}
 	
 	@GET
 	@Path("/deltag")
 	@Produces("application/json")
 	public boolean delTag(@QueryParam("tag") String tag, @QueryParam("qid") int qid){
 		try{
 			log.debug("Deleting tag '" + tag + "' from question " + qid);
 			Question question = QuestionQuery.get(qid);
 			question.removeTagByString(tag);
 			question.update();
 			return true;
 		} catch(Exception e){
 			return false;
 		}
 		
 	}
 	
 	@GET
 	@Path("/parents")
 	@Produces("application/json")
 	public Map<String,?> getParents(@QueryParam("qid") int qid, @QueryParam("depth") int depth) {
 		boolean exhausted = false;
 		List<Question> historyList = new ArrayList<Question>();
 		Question curChild = QuestionQuery.get(qid);
 		Question curParent = null;
 		
 		for(; depth > 0; depth--) {
 			
 			curParent = curChild.getParent();
 			
 			if(curParent == null){
 				exhausted = true;
 				break;
 			}
 			
 			historyList.add(curParent);
 			curChild = curParent;
 		}
 		
 		if(curParent != null && curParent.getParent() == null) {exhausted = true;}
 		 
 		int lastID = curChild.getId(); 
 		
 		return ImmutableMap.of("questions", historyList, "exhausted", exhausted, "last", lastID);
 	}
 	
 	@GET
 	@Path("/forks")
 	@Produces("application/json")
 	public Map<String,?> getForks(
 			@QueryParam("qid") int qid, 
 			@QueryParam("disp") int alreadyDisplayed, 
 			@QueryParam("amount") int toDisplay){
 		// disp is the number of forks already displayed. Therefore, if 0 forks are displayed, for ex,
 		// the controller will return the 
 		List<Question> forks = QuestionQuery.all().withParent(qid).list();
 		
 		log.debug("There were " + forks.size() + " forks found. There are " + alreadyDisplayed + " already displayed.");
 		if(forks.size() <= alreadyDisplayed) {
 			log.debug("Number of forks <= forks already displayed.");
 			return ImmutableMap.of("questions", new ArrayList<Question>(), "exhausted", true, "disp", alreadyDisplayed);
 		}else if(forks.size() <= alreadyDisplayed + toDisplay){
 			// If the amount of forks still not displayed is less than those requested.
 			log.debug("There are still a few forks to display but the forks are now exhausted.");
 			return ImmutableMap.of(
 					"questions", forks.subList(alreadyDisplayed, forks.size()),
 					"exhausted", true,
 					"disp", forks.size()
 			);
 		} else {
 			log.debug("Not all forks returned.");
 			return ImmutableMap.of(
 					"questions", forks.subList(alreadyDisplayed, alreadyDisplayed + toDisplay),
 					"exhausted", false,
 					"disp", alreadyDisplayed + toDisplay
 			);
 		}
 		
 	}
 	
 	@GET
 	@Path("/{id}/togglestar")
 	@Produces("application/json")
 	public Map<String,?> toggleStar(@PathParam("id") int id) {
 		Question q = QuestionQuery.get(id);
 		q.toggleStarred();
 		q.update();
 		
 		return ImmutableMap.of("id", id, "starred", q.isStarred());
 	}
 	
 }
