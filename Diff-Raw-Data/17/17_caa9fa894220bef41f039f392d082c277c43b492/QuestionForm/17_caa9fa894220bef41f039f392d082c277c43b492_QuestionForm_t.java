 package uk.ac.cam.sup.form;
 
 import javax.ws.rs.FormParam;
 
 import org.apache.log4j.Logger;
 
 import uk.ac.cam.sup.exceptions.FormValidationException;
 import uk.ac.cam.sup.models.Data;
 import uk.ac.cam.sup.models.Question;
 import uk.ac.cam.sup.models.QuestionSet;
 import uk.ac.cam.sup.queries.QuestionSetQuery;
 import uk.ac.cam.sup.util.DataType;
 
 public abstract class QuestionForm {
 	
 	Logger log = Logger.getLogger(QuestionForm.class);
 	private boolean validated = false;
 	private boolean parsed = false;
 	
 	@FormParam("content_type") private String contentType;
 	@FormParam("content_text") private String contentText;
 	@FormParam("content_file") private byte[] contentFile;
 	@FormParam("content_desc") private String contentDescription;
 	@FormParam("content_ext")  private String contentExtension;
 	private Data contentData;
 	
 	@FormParam("notes_type") private String notesType;
 	@FormParam("notes_text") private String notesText;
 	@FormParam("notes_file") private byte[] notesFile;
 	@FormParam("notes_desc") private String notesDescription;
 	@FormParam("notes_ext")  private String notesExtension;
 	private Data notesData;
 	
 	@FormParam("setId")
 	protected Integer setId;
 	
 	@FormParam("expectedDuration")
 	private String expectedDuration;
 
 	public QuestionForm(){
 	}
 	
 	public QuestionForm(String content, String notes, Integer setID, Integer expectedDuration){
 		this.contentType = DataType.PLAIN_TEXT.toString();
 		this.contentText = content;
 		this.notesType = DataType.PLAIN_TEXT.toString();
 		this.notesText = notes;
 		this.setId = setID;
 		this.expectedDuration = expectedDuration.toString();
 	}
 	
 	public Data getContent() throws FormValidationException {
 		if (!validated) {
 			throw new FormValidationException("Form was not validated");
 		} else if (!parsed) {
 			log.error("Form was not parsed");
 			throw new FormValidationException("Form was not parsed");
 		}
 		return this.contentData;
 	}
 	
 	public Data getNotes() throws FormValidationException {
 		if (!validated) {
 			throw new FormValidationException("Form was not validated");
 		} else if (!parsed) {
 			log.error("Form was not parsed");
 			throw new FormValidationException("Form was not parsed");
 		}
 		return this.notesData;
 	}
 	
 	public Integer getSetId() throws FormValidationException {
 		if (!validated) {
 			throw new FormValidationException("Form was not validated");
 		} else if (!parsed) {
 			log.error("Form was not parsed");
 			throw new FormValidationException("Form was not parsed");
 		}
 		return this.setId;
 	}
 	
 	public int getExpectedDuration() throws FormValidationException {
 		if (!validated) {
 			throw new FormValidationException("Form was not validated");
 		} else if (!parsed) {
 			log.error("Form was not parsed");
 			throw new FormValidationException("Form was not parsed");
 		}
 		try {
 			return Integer.parseInt(this.expectedDuration); 
 		} catch (Exception e) {
 			return 0;
 		}
 	}
 	
 	public QuestionForm validate() throws FormValidationException {
		
		if (setId == null) {
			throw new FormValidationException("This question cannot be edited in this context (setId == null)");
		}
		
 		QuestionSet qs = QuestionSetQuery.get(setId);
 		
 		// Note: setId is -1 if question is edited without a set as context.
		if (setId != -1 && qs == null) {
 			throw new FormValidationException("This question cannot be edited in this context");
 		}
 		
 		if (contentType == null) {
 			throw new FormValidationException("Content type not specified");
 		}
 		
 		if (DataType.valueOf(contentType) == DataType.FILE 
 				&& contentFile != null && contentFile.length > 0
 				&& (contentExtension == null || contentExtension.equals(""))) {
 			throw new FormValidationException("File extension not specified");
 		}
 		
 		if (contentText == null) {
 			contentText = "";
 		}
 		
 		if (notesType == null) {
 			throw new FormValidationException("Notes type not specified");
 		}
 		
 		if (DataType.valueOf(notesType) == DataType.FILE
 				&& notesFile != null && notesFile.length > 0
 				&& (notesExtension == null || notesExtension.equals(""))) {
 			throw new FormValidationException("File extension not specified");
 		}
 		
 		if (notesText == null) {
 			notesText = "";
 		}
 		
 		if (expectedDuration == null) {
 			expectedDuration = "0";
 		}
 		
 		validated = true;
 		
 		return this;
 	}
 	
 	public QuestionForm parse() throws Exception {
 		if (!validated) {
 			throw new FormValidationException("Form was not validated");
 		}
 		
 		this.contentData = new Data (contentType, contentText,
 				contentFile, contentDescription, contentExtension, forceLoad());
 		
 		this.notesData = new Data(notesType, notesText, notesFile,
 				notesDescription, notesExtension, forceLoad());
 		parsed = true;
 		return this;
 	}
 	
 	public Question store(Question q) throws FormValidationException {
 		if (!validated) {
 			throw new FormValidationException("Form was not validated");
 		} else if (!parsed) {
 			log.error("Form was not parsed");
 			throw new FormValidationException("Form was not parsed");
 		}
 		
 		q.getContent().updateWith(contentData);
 		q.getNotes().updateWith(notesData);
 		q.setExpectedDuration(getExpectedDuration());
 		
 		return q;
 	}
 	
 	protected abstract boolean forceLoad();
 }
