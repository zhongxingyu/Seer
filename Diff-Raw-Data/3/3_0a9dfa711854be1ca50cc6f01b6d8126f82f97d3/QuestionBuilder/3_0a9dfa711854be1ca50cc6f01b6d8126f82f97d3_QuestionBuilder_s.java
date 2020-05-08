 package com.askcs.dialog.sdk;
 
 
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import com.askcs.dialog.sdk.model.Answer;
 import com.askcs.dialog.sdk.model.Question;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class QuestionBuilder {
 	
 	static final Logger log = Logger.getLogger(QuestionBuilder.class.getName());
 	
 	@Deprecated
 	public static String build(Question question, String url, String responder) {
 		HashMap<String, String> params = new HashMap<String, String>();
		params.put("responder",responder);
 		
 		return build(question, url, params);
 	}
 
 	public static String build(Question question, String url, HashMap<String, String> params) {
 		
 		String res="{}";
 		if(question.getQuestion_id()==null || question.getQuestion_id().equals(""))
 			return res;
 		
 		if(!question.getType().equals(Question.QUESTION_TYPE_REFERRAL) && (question.getQuestion_text()==null || question.getQuestion_text().equals("")))
 			return res;
 		
 		String question_url = url+"/questions/";
 		question.setBase_url(url);
 		question.setQuestion_url(question_url+question.getQuestion_id());
 		if(question.getQuestion_text().equals("") || question.getQuestion_text().endsWith(".wav")) {
 			if(question.getAnswers()!=null) {
 				for(Answer answer : question.getAnswers()) {
 
 					if(answer.getCallback()!=null && !answer.getCallback().equals("")) {
 						answer.setCallback(question_url+answer.getCallback());
 					
 						if(params!=null && !params.isEmpty()) {
 							answer.setCallback(answer.getCallback()+"?"+HashMapToQuery(params));
 						}
 					}
 				}
 			}
 		} else {
 			question.setQuestion_expandedtext(question.getQuestion_text());
 			question.setQuestion_text("text://"+question.getQuestion_text());
 			
 			if(question.getAnswers()!=null) {
 				for(Answer answer : question.getAnswers()) {
 					
 					String answerText = answer.getAnswer_text(); 					
 					answer.setAnswer_expandedtext(answerText);
 					answer.setAnswer_text("text://"+answerText);
 					if(answer.getCallback()!=null) {
 						answer.setCallback(question_url+answer.getCallback());
 						
 						String qs = "?";
 						if(params!=null && !params.isEmpty()) {
 							answer.setCallback(answer.getCallback()+qs+HashMapToQuery(params));
 							qs="&";
 						}
 					
 						if(question.getType().equals("closed")) {
 							answer.setCallback(answer.getCallback()+qs+"value="+answerText);
 						}
 						
 						
 					}
 				}
 			}
 		}
 		
 		ObjectMapper om = new ObjectMapper();
 		try {
 			res = om.writeValueAsString(question);
 		} catch(Exception ex) {
 			log.warning("Failed to parse next question");
 		}
 		return res;
 	}
 	
 	private static String HashMapToQuery(HashMap<String, String> params) {
 		
 		String res="";
 		for(Entry<String, String> entry : params.entrySet()) {
 			res += "&"+entry.getKey()+"="+entry.getValue();
 		}
 		
 		if(!res.isEmpty())
 			res = res.substring(1);
 		
 		return res;
 	}
 }
