 package com.blogspot.jmelon.portlet.quiz.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  * See the file "LICENSE" for the full license governing this code.
  * @author <a href="jmelon.blogspot.com">Michał Mela</a>
  */
 public class QuizPrefs {
 
 	private List<QuizTab> tabs;
 	private List<QuizResult> results;
 	private boolean randomizeAnswerOrder;
 	
 	private String summaryTabTitle;
	private String resetLabel;
 	private String prevLabel;
	private String nextLabel;
 	private String submitLabel;
 	
 	@JsonIgnore
 	public List<QuizQuestion> getAllQuestions() {
 		List<QuizQuestion> questions = new ArrayList<QuizQuestion>();
 		for (QuizTab tab : tabs) {
 	        questions.addAll(tab.getQuestions());
         }
 		return questions;
 	}
 
 	public List<QuizTab> getTabs() {
 	    return tabs;
     }
 
 	public void setTabs(List<QuizTab> questions) {
 	    this.tabs = questions;
     }
 
 	public boolean isRandomizeAnswerOrder() {
 	    return randomizeAnswerOrder;
     }
 
 	public void setRandomizeAnswerOrder(boolean randomizeAnswerOrder) {
 	    this.randomizeAnswerOrder = randomizeAnswerOrder;
     }
 
 	public List<QuizResult> getResults() {
 	    return results;
     }
 
 	public void setResults(List<QuizResult> results) {
 	    this.results = results;
     }
 
 	public String getSummaryTabTitle() {
 	    return summaryTabTitle;
     }
 
 	public void setSummaryTabTitle(String summaryTabTitle) {
 	    this.summaryTabTitle = summaryTabTitle;
     }
 
 	public String getNextLabel() {
     	return nextLabel;
     }
 
 	public void setNextLabel(String nextLabel) {
     	this.nextLabel = nextLabel;
     }
 
 	public String getPrevLabel() {
     	return prevLabel;
     }
 
 	public void setPrevLabel(String prevLabel) {
     	this.prevLabel = prevLabel;
     }
 
 	public String getSubmitLabel() {
     	return submitLabel;
     }
 
 	public void setSubmitLabel(String submitLabel) {
     	this.submitLabel = submitLabel;
     }
 
	public String getResetLabel() {
	    return resetLabel;
    }

	public void setResetLabel(String resetLabel) {
	    this.resetLabel = resetLabel;
    }

 	@Override
     public String toString() {
 	    StringBuilder builder = new StringBuilder();
 	    builder.append("QuizPrefs [tabs=").append(tabs).append(", results=").append(results)
 	            .append(", randomizeAnswerOrder=").append(randomizeAnswerOrder).append(", summaryTabTitle=")
	            .append(summaryTabTitle).append(", resetLabel=").append(resetLabel).append(", prevLabel=")
	            .append(prevLabel).append(", nextLabel=").append(nextLabel).append(", submitLabel=")
	            .append(submitLabel).append("]");
 	    return builder.toString();
     }
 	
 }
