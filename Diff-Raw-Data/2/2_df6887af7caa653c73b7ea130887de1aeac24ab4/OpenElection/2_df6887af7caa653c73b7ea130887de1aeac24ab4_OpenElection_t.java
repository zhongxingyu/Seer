 package com.rau.evoting.beans;
 
 import java.util.ArrayList;
 
 import javax.faces.event.AjaxBehaviorEvent;
 import javax.mail.MessagingException;
 
 import com.rau.evoting.data.SqlDataProvider;
 import com.rau.evoting.models.*;
 import com.rau.evoting.utils.MailService;
 
 public class OpenElection {
 	private String name;
 	private String description;
 	private Election election;
 	private ArrayList<Answer> answers;
 	private ArrayList<Trustee> trustees;
 	private String trusteeName;
 	private String trusteeEmail;
 	private String answer;
 	private int maxId;
 	private boolean disabled;
 	private boolean showRemove;
 	private boolean canOpen;
 	private String openningMessage;
 	
 	public OpenElection() {
 		answers = new ArrayList<Answer>();
 		trustees = new ArrayList<Trustee>();
 		disabled = false;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public Election getElection() {
 		return election;
 	}
 
 	public void setElection(Election election) {
 		this.election = election;
 	}
 	
 	public ArrayList<Answer> getAnswers() {
 		return answers;
 	}
 
 	public void setAnswers(ArrayList<Answer> answers) {
 		this.answers = answers;
 	}
 	
 	public String getAnswer() {
 		return answer;
 	}
 
 	public void setAnswer(String answer) {
 		this.answer = answer;
 	}
 	
 	public ArrayList<Trustee> getTrustees() {
 		trustees = SqlDataProvider.getInstance().getElectionTrustees(election.getId());
 		return trustees;
 	}
 
 	public void setTrustees(ArrayList<Trustee> trustees) {
 		this.trustees = trustees; 
 	}
 
 	public String getTrusteeName() {
 		return trusteeName;
 	}
 
 	public void setTrusteeName(String trusteeName) {
 		this.trusteeName = trusteeName;
 	}
 
 	public String getTrusteeEmail() {
 		return trusteeEmail;
 	}
 
 	public void setTrusteeEmail(String trusteeEmail) {
 		this.trusteeEmail = trusteeEmail;
 	}
 
 	public int getMaxId() {
 		maxId = answers.size()+1;
 		return maxId;
 	}
 
 	public void setMaxId(int maxId) {
 		this.maxId = maxId;
 	}
 	
 	public boolean isDisabled() {
 		return disabled;
 	}
 
 	public void setDisabled(boolean disabled) {
 		this.disabled = disabled;
 	}
 	
 	public boolean isShowRemove() {
 		return answers.size() > 0 ;
 	}
 
 	public void setShowRemove(boolean showRemove) {
 		this.showRemove = showRemove;
 	}
 
 	
 	public boolean isCanOpen() {
 		boolean allGenerated = true;
 		for(Trustee tr: trustees){
 			allGenerated &= tr.isGenerated();
 			if(!allGenerated)
 				break;
 		}
 		canOpen = allGenerated & (answers.size() > 0) ;
 		return canOpen;
 	}
 
 	public void setCanOpen(boolean canOpen) {
 		this.canOpen = canOpen;
 	}
 
 	public String getOpenningMessage() {
 		boolean allGenerated = true;
 		for(Trustee tr: trustees){
 			allGenerated &= tr.isGenerated();
 			if(!allGenerated)
 				break;
 		}
 		if(answers.size() == 0) {
 			openningMessage = "You have no answers ";
 			if(!allGenerated) {
 				openningMessage += "and not all trustees generate their keys.";
 			}
 		}
 		else if(!allGenerated) {
 			openningMessage = "Not all trustees generated their keys.";
 		}
 		return openningMessage;
 	}
 
 	public void setOpenningMessage(String openningMessage) {
 		this.openningMessage = openningMessage;
 	}
 
 	public String navigateAnswers() {
 		answers = SqlDataProvider.getInstance().getElectionAnswers(election.getId());
 		return "Answers";
 	}
 	
 	public String navigateTrustees() {
 		//answers = SqlDataProvider.getInstance().getElectionAnswers(election.getId());
 		return "Trustees";
 	}
 	
 	public String addElection(){
 		int elId = SqlDataProvider.getInstance().insertElecttion(new Election(0, name, description));
 		election = new Election(elId, name, description);
 		return "next";
 	}
 	
 	public String addAnswer() {
 		answers.add(new Answer(maxId, answer));
 		answer = "";
 		return "";
 	}
 	
 	public String removeAnswer() {
 		if (answers.size() > 0) {
 			answers.remove(answers.size() - 1);
 		}
 		return "";
 	}
 	
 	public String addAnswers() {
 		SqlDataProvider.getInstance().insertAnswers(election.getId(), answers);
		return "OpenElection";
 	}
 	
 	public String addTrustee() {
 		//trustees.add(new Trustee(trustees.size()+1, trusteeName, trusteeEmail));
 //		try {
 //			MailService.sendMessage(trusteeEmail, "Trustee for evoting", "Hi Mr. " + trusteeName);
 //		} catch (MessagingException e) {
 //			e.printStackTrace();
 //		}
 		SqlDataProvider.getInstance().insertTrustee(election.getId(), new Trustee(trustees.size()+1, trusteeName, trusteeEmail, false));
 		trusteeName = "";
 		trusteeEmail = "";
 		return "Trustees";
 	}
 	
 	public void ajaxListener(AjaxBehaviorEvent event) {
 		System.out.println("event: " +  event.getSource().toString());
 		disabled = !disabled; 
 	}
 	
 }
