 package uk.ac.cam.sup.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.jboss.resteasy.annotations.Form;
 import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.sup.exceptions.FormValidationException;
 import uk.ac.cam.sup.exceptions.InvalidInputException;
 import uk.ac.cam.sup.form.QuestionAdd;
 import uk.ac.cam.sup.form.QuestionEdit;
 import uk.ac.cam.sup.form.QuestionTagAdd;
 import uk.ac.cam.sup.form.TagDel;
 import uk.ac.cam.sup.models.Question;
 import uk.ac.cam.sup.models.QuestionSet;
 import uk.ac.cam.sup.models.Tag;
 import uk.ac.cam.sup.models.User;
 import uk.ac.cam.sup.ppdloader.PPDLoader;
 import uk.ac.cam.sup.queries.QuestionQuery;
 import uk.ac.cam.sup.queries.QuestionSetQuery;
 
 import com.google.common.collect.ImmutableMap;
 
 @Path("/q")
 public class QuestionEditController extends GeneralController{
 	
 	Logger log = LoggerFactory.getLogger(QuestionEditController.class);
 	
 	
 	/**
 	 * Update an existing question
 	 * 
 	 * @param qe
 	 * @return
 	 */
 	@POST
 	@Path("/update")
 	@Produces("application/json")
 	@Consumes(MediaType.MULTIPART_FORM_DATA)
 	public Map<String,?> updateQuestion(@MultipartForm QuestionEdit qe) {
 		User editor = getCurrentUser();
 		Question q;
 		QuestionSet qs;
 		
 		try {
 			qe.validate().parse();
 
 			log.debug("Trying to update question " + qe.getId() + " in set " + qe.getSetId() + ".");
 			
 			q = QuestionQuery.get(qe.getId());
 			qs = QuestionSetQuery.get(qe.getSetId());
 			if (qs != null) {
 				if (!qs.getOwner().getId().equals(getCurrentUserID())) {
 					throw new FormValidationException("You're not the owner of the target set: "+qs.getName());
 				}
 			}
 			
 			q = q.edit(editor, qe);
 			
 			if(qe.getSetId() == -1) {
 				return ImmutableMap.of("success", true, "question", q);
 			}else{
 				return ImmutableMap.of("success", true, "question", q, "set", qs);
 			}
 		} catch (FormValidationException e) {
 			log.debug("There was a FormValidationException: " + e.getMessage());
 			return ImmutableMap.of("success", false, "error", e.getMessage());
 		} catch (InvalidInputException e){
 			log.debug("There was an InvalidInputException: " + e.getMessage());
 			return ImmutableMap.of("success", false, "error", e.getMessage());
		} catch (NullPointerException e){
			log.debug("There was a NullPointerException");
			e.printStackTrace();
			return ImmutableMap.of("success", false, "error", "NullPointerException");
 		} catch (Exception e){
 			log.debug("There was an exception: " + e.getMessage());
 			return ImmutableMap.of("success", false, "error", e.getMessage());
 		}
 		
 	}
 
 	@POST
 	@Path("/save")
 	@Produces("application/json")
 	@Consumes(MediaType.MULTIPART_FORM_DATA)
 	public Map<String,?> addQuestion(@MultipartForm QuestionAdd qa) {
 		User author = getCurrentUser();
 		Question q;
 		QuestionSet qs;
 
 		try {
 			qa.validate().parse();
 			qs = QuestionSetQuery.get(qa.getSetId());
 			if (!qs.getOwner().getId().equals(getCurrentUserID())) {
 				throw new Exception("You're not the owner of this set");
 			}
 			
 			q = new Question(author);
 			q.setContent(qa.getContent());
 			q.setNotes(qa.getNotes());
 			q.setExpectedDuration(qa.getExpectedDuration());
 			q.save();
 			
 			
 			qs.addQuestion(q);
 			qs.update();
 		} catch (Exception e) {
 			return ImmutableMap.of("success", false, "error", e.getMessage());
 		}
 
 		return ImmutableMap.of("success", true, "question", q, "set", qs);
 	}
 
 	@GET
 	@Path("/pastpapers")
 	@Produces("application/json")
 	public Set<Question> producePastPapers() throws Exception {
 		return PPDLoader.loadAllQuestions();
 	}
 	
 	@POST
 	@Path("/addtags")
 	@Produces("application/json")
 	public Map<String,?> addTags(@Form QuestionTagAdd addForm) {
 		// returns the tags added
 		
 		try {
 			int qid = addForm.validate().getQid();
 			String[] newTagsArray = addForm.getNewTags();
 			
 			if (newTagsArray != null && newTagsArray.length > 0) {
 				
 				Question question = QuestionQuery.get(qid);
 				List<Tag> result = new ArrayList<Tag>();
 				Set<Tag> existingTags = question.getTags();
 				Tag tmp;
 				
 				for (int i = 0; i < newTagsArray.length; i++) {
 					tmp = new Tag(newTagsArray[i].trim());
 					
 					if(!existingTags.contains(tmp) && tmp.getName() != null && tmp.getName() != "") {
 						result.add(tmp);
 					}
 					
 					log.debug("Trying to add tag " + tmp.getName() + " to question " + qid + "...");
 					question.addTag(tmp);
 				}
 				
 				log.debug("Trying to update question in data base...");
 				question.update();
 				
 				return ImmutableMap.of("success", true, "tags", result, "amount", result.size());
 			}
 		} catch (Exception e) {
 			return ImmutableMap.of("success", false, "error", e.getMessage());
 		}
 		return ImmutableMap.of("success", false, "error", "Tag array was null");
 		
 	}
 	
 	@POST
 	@Path("/deltag")
 	@Produces("application/json")
 	public Map<String,?> delTag(@Form TagDel delForm){
 		String tag = delForm.getTag();
 		int qid = delForm.getQid();
 		
 		try{
 			log.debug("Deleting tag '" + tag + "' from question " + qid);
 			Question question = QuestionQuery.get(qid);
 			question.removeTagByString(tag);
 			question.update();
 			return ImmutableMap.of("success", true, "question", question);
 		} catch(Exception e){
 			return ImmutableMap.of("success", false, "error", e.getMessage());
 		}
 		
 	}
 	
 	@POST
 	@Path("/togglestar")
 	@Produces("application/json")
 	public Map<String,?> toggleStar(@FormParam("id") int id) {
 		Question q;
 		try{
 			q = QuestionQuery.get(id);
 			if (!q.getOwner().getId().equals(getCurrentUserID())) {
 				throw new Exception("You're not the owner of this question");
 			}
 			q.toggleStarred();
 			q.update();
 		} catch(Exception e) {
 			return ImmutableMap.of("success", false, "error", e.getMessage());
 		}
 		return ImmutableMap.of("success", true, "id", id, "starred", q.isStarred());
 	}
 	
 }
