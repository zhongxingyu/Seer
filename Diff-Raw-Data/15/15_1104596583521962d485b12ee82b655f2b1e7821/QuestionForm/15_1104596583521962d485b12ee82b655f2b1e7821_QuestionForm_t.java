 package uk.ac.cam.sup.form;
 
 import javax.ws.rs.FormParam;
 
 import uk.ac.cam.sup.models.Data;
 import uk.ac.cam.sup.models.Question;
 import uk.ac.cam.sup.queries.QuestionSetQuery;
 
 public abstract class QuestionForm {
 private boolean validated = false;
 	
 	@FormParam("content")
 	private String content;
 	private Data dcontent;
 	
 	@FormParam("notes")
 	private String notes;
 	private Data dnotes;
 	
 	@FormParam("setId")
 	private Integer setId;
 	
 	@FormParam("expectedDuration")
	private String expectedDuration;
 	
 	public Data getContent() throws RuntimeException {
 		if (!validated) {
 			throw new RuntimeException("Form was not yet validated");
 		}
 		return this.dcontent;
 	}
 	
 	public Data getNotes() throws RuntimeException {
 		if (!validated) {
 			throw new RuntimeException("Form was not yet validated");
 		}
 		return this.dnotes;
 	}
 	
 	public Integer getSetId() throws RuntimeException {
 		if(!validated) {
 			throw new RuntimeException("Form was not yet validated");
 		}
 		return this.setId;
 	}
 	
 	public int getExpectedDuration() throws RuntimeException {
 		if (!validated) {
 			throw new RuntimeException("Form was not yet validated");
 		}
		try {
			return Integer.parseInt(this.expectedDuration); 
		} catch (Exception e) {
			return 0;
		}
 	}
 	
 	public QuestionForm validate() throws Exception {
 		
 		if (setId == null || QuestionSetQuery.get(setId) == null) {
 			throw new Exception("Question you're trying to edit does not belong to any set");
 		}
 		
 		if (content == null || content.equals("")) {
 			dcontent = new Data(false, null);
 		} else {
 			dcontent = new Data(true, content);
 		}
 		
 		if (notes == null || notes.equals("")) {
 			dnotes = new Data(false, null);
 		} else {
 			dnotes = new Data(true, notes);
 		}
 		
 		if (expectedDuration == null) {
			expectedDuration = "0";
 		}
 		
 		validated = true;
 		
 		return this;
 	}
 	
 	public Question store(Question q) {
 		q.setContent(dcontent);
 		q.setNotes(dnotes);
		q.setExpectedDuration(getExpectedDuration());
 		
 		return q;
 	}
 }
