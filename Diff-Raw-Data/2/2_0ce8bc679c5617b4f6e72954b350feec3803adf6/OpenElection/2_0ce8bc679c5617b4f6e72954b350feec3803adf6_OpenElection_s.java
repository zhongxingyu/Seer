 package com.rau.evoting.beans;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.context.FacesContext;
 import javax.faces.event.AjaxBehaviorEvent;
 import javax.mail.MessagingException;
 
 import com.rau.evoting.ElGamal.ElGamalHelper;
 import com.rau.evoting.data.ElectionDP;
 import com.rau.evoting.data.ElectionTrusteeDP;
 import com.rau.evoting.data.ElectionVoterDP;
 import com.rau.evoting.data.ElectonAnswerDP;
 import com.rau.evoting.data.SqlDataProvider;
 import com.rau.evoting.models.Answer;
 import com.rau.evoting.models.Election;
 import com.rau.evoting.models.Trustee;
 import com.rau.evoting.utils.MailService;
 import com.rau.evoting.utils.Util;
 import com.restfb.types.Group;
 
 public class OpenElection {
 	private Election election;
 	private ArrayList<Answer> answers;
 	private ArrayList<Trustee> trustees;
 	private String trusteeEmail;
 	private String answer;
 	private boolean showRemove;
 	private boolean canOpen;
 	private String openningMessage;
 	private String selectedGroup;
 	private String selectedVoteMode;
 	private List<Group> groups;
 
 	private String accessToken;
 
 	public OpenElection() {
 		// trustees = new ArrayList<Trustee>();
 		// selectedVoteMode = "all";
 		// answers = new ArrayList<Answer>();
 		accessToken = (String) FacesContext.getCurrentInstance()
 				.getExternalContext().getSessionMap().get("accessToken");
 	}
 
 	public String navigateAnswers() {
 		answers = ElectonAnswerDP.getElectionAnswers(election.getId());
 		answer = "";
 		return "Answers?faces-redirect=true";
 	}
 
 	public String navigateTrustees() {
 		return "Trustees?faces-redirect=true";
 	}
 
 	public String createElection(String name, String description) {
 		int userId = (int) FacesContext.getCurrentInstance()
 				.getExternalContext().getSessionMap().get("userId");
 		int elId = ElectionDP
 				.insert(new Election(0, name, description), userId);
 		election = new Election(elId, name, description);
 		answers = new ArrayList<Answer>();
 		trustees = new ArrayList<Trustee>();
 		selectedVoteMode = "all";
 		selectedGroup = null;
 		return "next";
 	}
 
 	public void addAnswer(AjaxBehaviorEvent even) {
 		if (!answer.equals("")) {
 			answers.add(new Answer(answers.size() + 1, answer));
 			answer = "";
 		}
 		return;
 	}
 
 	public void removeAnswer(AjaxBehaviorEvent even) {
 		if (answers.size() > 0) {
 			answers.remove(answers.size() - 1);
 		}
 		answer = "";
 		return;
 	}
 
 	public String cancelAnswers() {
 		return "OpenElection?faces-redirect=true";
 	}
 
 	public String addAnswers() {
 		ElectonAnswerDP.insertAnswers(election.getId(), answers);
 		return "OpenElection?faces-redirect=true";
 	}
 
 	public String addTrustee() {
 
 		if (trusteeEmail.equals(""))
 			return "";
 
 		for (Trustee trustee : trustees) {
 			if (trustee.getEmail().equals(trusteeEmail))
 				return "";
 		}
 
 		String message = "Hello, you are chosen trustee for  "
 				+ election.getName()
 				+ " election\n Please, generate your key: \n";
 		String token = Util.generateRandomToken();
 		int trId = ElectionTrusteeDP.insertTrustee(election.getId(),
 				new Trustee(0, trusteeEmail, false, token));
 		String url = "http://localhost:8080/Evoting/TrusteeHome.xhtml?trId="
 				+ trId + "&token=" + token;
 		String encodedUrl = url;
 		try {
 			encodedUrl = URLEncoder.encode(url, "UTF-8");
 		} catch (UnsupportedEncodingException e1) {
 			e1.printStackTrace();
 		}
 		message += url; // or encoded
 		try {
 			MailService.sendMessage(trusteeEmail,
 					"Trustee for " + election.getName() + " election", message);
 		} catch (MessagingException e) {
 			e.printStackTrace();
 			ElectionTrusteeDP.deleteTrustee(trId);
 		}
 		trusteeEmail = "";
 
		return "";
 	}
 
 	public String setElection(int id) {
 		election = ElectionDP.getElection(id);
 		answers = ElectonAnswerDP.getElectionAnswers(election.getId());
 		trustees = ElectionTrusteeDP.getElectionTrustees(election.getId());
 		selectedGroup = ElectionVoterDP.getElectionVoterByGroup(election
 				.getId());
 		if (selectedGroup == null) {
 			selectedVoteMode = "all";
 		} else {
 			selectedVoteMode = "";
 		}
 		return "OpenElection?faces-redirect=true";
 	}
 
 	public String open() {
 		String pbKey = ElGamalHelper.getElectionPublicKey(ElectionTrusteeDP
 				.getElectionTrusteesPublicKeys(election.getId()));
 		ElectionDP.openElection(election.getId(), pbKey);
 		return "Elections?faces-redirect=true";
 	}
 
 	public String fromVoters() {
 		ElectionVoterDP.deleteElectionVoters(election.getId());
 		if (!selectedVoteMode.equals("all")) {
 			ElectionVoterDP.setElectionVotersByGroup(election.getId(),
 					selectedGroup);
 		}
 		return "OpenElection?faces-redirect=true";
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
 		trustees = ElectionTrusteeDP.getElectionTrustees(election.getId()); // fix
 		return trustees;
 	}
 
 	public void setTrustees(ArrayList<Trustee> trustees) {
 		this.trustees = trustees;
 	}
 
 	public String getTrusteeEmail() {
 		return trusteeEmail;
 	}
 
 	public void setTrusteeEmail(String trusteeEmail) {
 		this.trusteeEmail = trusteeEmail;
 	}
 
 	public String getSelectedGroup() {
 		return selectedGroup;
 	}
 
 	public void setSelectedGroup(String selectedGroup) {
 		this.selectedGroup = selectedGroup;
 	}
 
 	public String getSelectedVoteMode() {
 		return selectedVoteMode;
 	}
 
 	public void setSelectedVoteMode(String selectedVoteMode) {
 		this.selectedVoteMode = selectedVoteMode;
 	}
 
 	public List<Group> getGroups() {
 		// FacebookClient fbClient = new DefaultFacebookClient(accessToken);
 		// Connection<Group> gr = fbClient.fetchConnection("me/groups",
 		// Group.class);
 		// groups = gr.getData();
 		groups = (List<Group>) FacesContext.getCurrentInstance()
 				.getExternalContext().getSessionMap().get("userGroups");
 		return groups;
 	}
 
 	public void setGroups(ArrayList<Group> groups) {
 		this.groups = groups;
 	}
 
 	public boolean isShowRemove() {
 		return answers.size() > 0;
 	}
 
 	public void setShowRemove(boolean showRemove) {
 		this.showRemove = showRemove;
 	}
 
 	public boolean isCanOpen() {
 		boolean allGenerated = true;
 		for (Trustee tr : trustees) {
 			allGenerated &= tr.isGenerated();
 			if (!allGenerated)
 				break;
 		}
 		canOpen = allGenerated & (answers.size() > 0);
 		return canOpen;
 	}
 
 	public void setCanOpen(boolean canOpen) {
 		this.canOpen = canOpen;
 	}
 
 	public String getOpenningMessage() {
 		boolean allGenerated = true;
 		for (Trustee tr : trustees) {
 			allGenerated &= tr.isGenerated();
 			if (!allGenerated)
 				break;
 		}
 		if (answers.size() == 0) {
 			openningMessage = "You have no answers ";
 			if (!allGenerated) {
 				openningMessage += "and not all trustees generate their keys.";
 			}
 		} else if (!allGenerated) {
 			openningMessage = "Not all trustees generated their keys.";
 		}
 		return openningMessage;
 	}
 
 	public void setOpenningMessage(String openningMessage) {
 		this.openningMessage = openningMessage;
 	}
 
 }
