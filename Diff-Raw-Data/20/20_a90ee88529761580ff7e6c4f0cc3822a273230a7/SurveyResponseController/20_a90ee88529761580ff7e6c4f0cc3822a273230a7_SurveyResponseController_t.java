 /*
  * Copyright (c) 2013 Michael Bochenek 
  */
 package com.vantaa3;
 
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 
 @ManagedBean(name = "surveyResponseController")
 @RequestScoped
 public class SurveyResponseController {
 
 	private Logger logger = Logger.getLogger(this.getClass().getName()); 
 	
 	@EJB
 	private SurveyRequestEJB requestEJB;
 
     @EJB
     private SurveyResponseEJB responseEJB;
 
     private SurveyRequest request;
     private SurveyResponse response;
     
     private String comment;
 
     private String question1;
     private String question2;
     private String question3;
     private String question4;
     private String question5;
 
 	private String key;
 	private String hashKeyOnly;
     private long id;
     
     private SurveyRequest getSurveyRequest() {
 		long start = System.currentTimeMillis();
     	
     	if (request != null) {
     		return request;
     	}
     	
     	// example key:  http://localhost:8080/vantaa3/surveyresponse.faces?key=5b4fff6f84d72af9c572c8f7a5874e8b-1
     	// which is "requestKey-id" (where ID is the SurveyRequest.ID, NOT the SurveyResponse.ID ! )
     	if (key == null) {
         	Map<String, String[]> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap();
         	String[] ks = params.get("key");
     		key = (ks != null && ks.length > 0) ? ks[0] : null;
     	}
 
 		if (key != null && key.trim().length() > 0 && key.contains("-")) {
 			String[] keyArray = key.split("-");
 			id = Long.parseLong(keyArray[1]);
 			hashKeyOnly = keyArray[0];
 			request = requestEJB.load(id, hashKeyOnly);
 		}
 
     	//logger.warning("getSurveyRequest() -> id: " + getId() + "  request.getId():" + (request == null ? " null " : request.getId()));
 
 		if (System.currentTimeMillis() - start > 0) {
 			logger.info("getSurveyRequest ms:" + (System.currentTimeMillis() - start));
 		}
 		
 		return request;
     }
     
     private SurveyResponse getSurveyResponse() {
     	if (response != null) {
     		return response;
     	}
     	
     	if (getSurveyRequest() != null && key != null) {
     		for (SurveyResponse r : request.getSurveyResponses()) {
     			String generatedKey = SimpleHash.generate(r.getEmail());
     			
     			//logger.warning(" generatedKey: " + generatedKey);
 
             	if (hashKeyOnly.equals(generatedKey)) {
             		response = r;
             		return response;
             	}
     		}
     		
     		//logger.warning(" getSurveyResponse() ->  we shouldn't get here: " + request.getSurveyResponses().size());
     	}
 
     	
 		return null;
     }
     
     /**
      * Very important - if the request has been submitted, show in ready only mode.
      * @return
      */
     public boolean isReadOnly() {
     	boolean val = getSurveyResponse() == null ? false : getSurveyResponse().getSubmitDate() != null;
    	//logger.info("isreadonly " + val);
     	return val;
     }
     
     public String getIdString() {
     	return "" + (getSurveyResponse() == null ? "getSurveyResponse returned null " : getSurveyResponse().getId());
     }
 
     public String getYou() {
     	return "" + (getSurveyResponse() == null ? "getSurveyResponse returned null " : getSurveyResponse().getEmail());
     }
 
     public String getOwner() {
     	return "" + (getSurveyRequest() == null ? "getSurveyResponse returned null " : getSurveyRequest().getOwner());
     }
     
     public String getSubject() {
     	return "" + (getSurveyRequest() == null ? "getSurveyResponse returned null " : getSurveyRequest().getSubject());
     }
     
 	public String submit() {
 		long start = System.currentTimeMillis();
 
 		if (getSurveyResponse() == null) {
 			return "nothingtosave.faces";
 		}
 		
 		//TODO
 		// MailSender.test(comment); --> works at 2:04pm Oct 7
 		
 		getSurveyResponse().setComment(comment);
 		
 		responseEJB.save(getSurveyResponse());
 		
 		if (System.currentTimeMillis() - start > 0) {
 			logger.info("submit ms:" + (System.currentTimeMillis() - start));
 		}
 		
 		return "surveyresponsecompleted.faces"; // this would return the next page that we would go to...
 	}
 
 	
 	public String getComment() {
 		return comment;
 	}
 
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 	public String getQuestion1() {
 		return question1;
 	}
 
 	public void setQuestion1(String question1) {
 		this.question1 = question1;
 	}
 
 	public String getQuestion2() {
 		return question2;
 	}
 
 	public void setQuestion2(String question2) {
 		this.question2 = question2;
 	}
 
 	public String getQuestion3() {
 		return question3;
 	}
 
 	public void setQuestion3(String question3) {
 		this.question3 = question3;
 	}
 
 	public String getQuestion4() {
 		return question4;
 	}
 
 	public void setQuestion4(String question4) {
 		this.question4 = question4;
 	}
 
 	public String getQuestion5() {
 		return question5;
 	}
 
 	public void setQuestion5(String question5) {
 		this.question5 = question5;
 	}
 
     public String getKey() {
 		return key;
 	}
 
 	public void setKey(String key) {
 		this.key = key;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 	
 	public long getId() {
 		return id;
 	}
 
 }
 
