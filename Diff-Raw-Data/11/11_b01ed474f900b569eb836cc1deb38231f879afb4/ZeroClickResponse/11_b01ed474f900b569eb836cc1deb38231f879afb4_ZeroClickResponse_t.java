 /*
  * ZeroClickResponse.java
  * 
  * This program is free software. It comes without any warranty, to
  * the extent permitted by applicable law. You can redistribute it
  * and/or modify it under the terms of the Do What The Fuck You Want
  * To Public License, Version 2, as published by Sam Hocevar. 
  * See http://sam.zoy.org/wtfpl/COPYING for more details. 
  * 
  * @author shaika-dzari
  * @since 2012-09-14
  */ 
 package net.nakama.duckquery.net.response;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import net.nakama.duckquery.net.response.api.RelatedTopic;
 import net.nakama.duckquery.net.response.api.ResponseType;
 import net.nakama.duckquery.net.response.api.Result;
 import net.nakama.duckquery.net.response.api.Topic;
 
 public class ZeroClickResponse {
 
 	// Info
 	private Calendar responseDate;
 	private String userQuery;
 	
 	// Api Fields
 	/**
 	 * @note api name = abstract
 	 */
 	private String abstractHtml = null;
 	private String abstractText = null;
 	private String abstractSource = null;
 	private String abstractURL = null;
 	private String image = null;
 	private String heading = null;
 	private String answer = null;
 	private String redirect = null;
 	private String answerType = null;
 	private String definition = null;
 	private String definitionSource = null;
 	private String definitionURL = null;
 	
 	/*
 	 *  Instead of an array, I use an object to wrap the result and the topic.
 	 */
 	private RelatedTopic relatedTopics;
 	private List<Result> results = null;
 	private ResponseType type = null;
 	
 	public ZeroClickResponse(String userQuery) {
 		this.userQuery = userQuery;
 		this.responseDate = Calendar.getInstance();
 	}
 
 	/**
 	 * @return the responseDate
 	 */
 	public Calendar getResponseDate() {
 		return responseDate;
 	}
 
 	/**
 	 * @param responseDate the responseDate to set
 	 */
 	public void setResponseDate(Calendar responseDate) {
 		this.responseDate = responseDate;
 	}
 
 	/**
 	 * @return the abstractHtml
 	 */
 	public String getAbstractHtml() {
 		return abstractHtml;
 	}
 
 	/**
 	 * @param abstractHtml the abstractHtml to set
 	 */
 	public void setAbstractHtml(String abstractHtml) {
 		this.abstractHtml = abstractHtml;
 	}
 
 	/**
 	 * @return the abstractText
 	 */
 	public String getAbstractText() {
 		return abstractText;
 	}
 
 	/**
 	 * @param abstractText the abstractText to set
 	 */
 	public void setAbstractText(String abstractText) {
 		this.abstractText = abstractText;
 	}
 
 	/**
 	 * @return the abstractSource
 	 */
 	public String getAbstractSource() {
 		return abstractSource;
 	}
 
 	/**
 	 * @param abstractSource the abstractSource to set
 	 */
 	public void setAbstractSource(String abstractSource) {
 		this.abstractSource = abstractSource;
 	}
 
 	/**
 	 * @return the abstractURL
 	 */
 	public String getAbstractURL() {
 		return abstractURL;
 	}
 
 	/**
 	 * @param abstractURL the abstractURL to set
 	 */
 	public void setAbstractURL(String abstractURL) {
 		this.abstractURL = abstractURL;
 	}
 
 	/**
 	 * @return the image
 	 */
 	public String getImage() {
 		return image;
 	}
 
 	/**
 	 * @param image the image to set
 	 */
 	public void setImage(String image) {
 		this.image = image;
 	}
 
 	/**
 	 * @return the heading
 	 */
 	public String getHeading() {
 		return heading;
 	}
 
 	/**
 	 * @param heading the heading to set
 	 */
 	public void setHeading(String heading) {
 		this.heading = heading;
 	}
 
 	/**
 	 * @return the answer
 	 */
 	public String getAnswer() {
 		return answer;
 	}
 
 	/**
 	 * @param answer the answer to set
 	 */
 	public void setAnswer(String answer) {
 		this.answer = answer;
 	}
 
 	/**
 	 * @return the redirect
 	 */
 	public String getRedirect() {
 		return redirect;
 	}
 
 	/**
 	 * @param redirect the redirect to set
 	 */
 	public void setRedirect(String redirect) {
 		this.redirect = redirect;
 	}
 
 	/**
 	 * @return the answerType
 	 */
 	public String getAnswerType() {
 		return answerType;
 	}
 
 	/**
 	 * @param answerType the answerType to set
 	 */
 	public void setAnswerType(String answerType) {
 		this.answerType = answerType;
 	}
 
 	/**
 	 * @return the definition
 	 */
 	public String getDefinition() {
 		return definition;
 	}
 
 	/**
 	 * @param definition the definition to set
 	 */
 	public void setDefinition(String definition) {
 		this.definition = definition;
 	}
 
 	/**
 	 * @return the definitionSource
 	 */
 	public String getDefinitionSource() {
 		return definitionSource;
 	}
 
 	/**
 	 * @param definitionSource the definitionSource to set
 	 */
 	public void setDefinitionSource(String definitionSource) {
 		this.definitionSource = definitionSource;
 	}
 
 	/**
 	 * @return the definitionURL
 	 */
 	public String getDefinitionURL() {
 		return definitionURL;
 	}
 
 	/**
 	 * @param definitionURL the definitionURL to set
 	 */
 	public void setDefinitionURL(String definitionURL) {
 		this.definitionURL = definitionURL;
 	}
 
 	/**
 	 * @return the relatedTopics
 	 */
 	public RelatedTopic getRelatedTopics() {
 		return relatedTopics;
 	}
 
 	/**
 	 * @param relatedTopics the relatedTopics to set
 	 */
 	public void setRelatedTopics(RelatedTopic relatedTopics) {
 		this.relatedTopics = relatedTopics;
 	}
 
 	/**
 	 * @return the type
 	 */
 	public ResponseType getType() {
 		return type;
 	}
 
 	/**
 	 * @param type the type to set
 	 */
 	public void setType(ResponseType type) {
 		this.type = type;
 	}
 
 	/**
 	 * @return the results
 	 */
 	public List<Result> getResults() {
 		return results;
 	}
 
 	/**
 	 * @param results the results to set
 	 */
 	public void setResults(List<Result> results) {
 		this.results = results;
 	}
 	
 	public boolean isBang() {
 		
 		if (this.userQuery.matches(".*![a-zA-Z]+.*")) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public List<Result> getFlatResults() {
 		
 		List<Result> flatResults = new ArrayList<Result>();
 		Result result;
 		
 		// #1 Abstract
		if (!"".equals(this.abstractURL)) {
 			result = new Result();
 			result.setUrl(this.abstractURL);
 			
			if (!"".equals(this.abstractText)) {
 				result.setText(this.abstractText);
			} else if (!"".equals(this.definition)) {
 				result.setText(this.definition);
			} else if (!"".equals(this.heading)) {
 				result.setText(this.heading);
 			}
 			flatResults.add(result);
 		}
 		
 		// #2 Results
 		if (this.results != null && this.results.size() > 0) {
 			flatResults.addAll(this.results);
 		}
 		
 		if (this.relatedTopics != null) {
 			
 			// #3 RelatedTopics => Results
 			if (this.relatedTopics.getResults() != null && this.relatedTopics.getResults().size() > 0) 
 				flatResults.addAll(this.relatedTopics.getResults());
 			
 			// #4 relatedTopics => Topics
 			if (this.relatedTopics.getTopics() != null && this.relatedTopics.getTopics().size() > 0) {
 				for (Topic t : this.relatedTopics.getTopics()) {
 					if (t.getResults() != null && t.getResults().size() > 0)
 						flatResults.addAll(t.getResults());
 				}
 			}
 		}
 		
 		return flatResults;
 	}
 	
 	@Override
 	public String toString() {
 		String s = "ZeroClickInfo[" + this.type + "]";
 		
 		if (this.relatedTopics != null) {
 			s += "[RelatedTopic";
 			if (this.relatedTopics.getResults() != null) 
 				s += "::Result(" + this.relatedTopics.getResults().size() + ")";
 			if (this.relatedTopics.getTopics() != null) 
 				s += "::Topics(" + this.relatedTopics.getTopics().size() + ")";
 			
 			s += "]";
 		}
 		
 		if (this.results != null) {
 			s += "[Results:::Result(" + this.results.size() + ")]"; 
 		}
 		
 		return s;
 	}
 
 	/**
 	 * @return the userQuery
 	 */
 	public String getUserQuery() {
 		return userQuery;
 	}
 }
