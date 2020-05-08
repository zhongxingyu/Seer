 package com.rau.evoting.beans;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.context.FacesContext;
 
 import com.rau.evoting.ElGamal.ElGamalHelper;
 import com.rau.evoting.data.ElectionDP;
 import com.rau.evoting.data.ElectionTrusteeDP;
 import com.rau.evoting.data.ElectionVoteDP;
 import com.rau.evoting.data.ElectionAnswerDP;
 import com.rau.evoting.models.Answer;
 import com.rau.evoting.models.CutVote;
 import com.rau.evoting.models.Election;
 import com.rau.evoting.models.Trustee;
 import com.rau.evoting.utils.StringHelper;
 import com.sun.xml.internal.ws.message.StringHeader;
 
 public class DecodeVotes {
 
 	private int electId;
 	private int trId;
 	private String privateKey = "";
 	private boolean validToken;
 
 	public DecodeVotes() {
 		Map<String, String> reqMap = FacesContext.getCurrentInstance()
 				.getExternalContext().getRequestParameterMap();
 		validToken = true;
 		if (reqMap.containsKey("trId")) {
 			electId = Integer.valueOf(reqMap.get("elId"));
 			int trId = Integer.valueOf(reqMap.get("trId"));
 			String token = reqMap.get("token");
 			Trustee tr = ElectionTrusteeDP.getElectionTrustee(trId);
 			if (!tr.getToken().equals(token)) {
 				validToken = false;
 			} else {
 				validToken = true;
 			}
 		}
 	}
 
 	public String decode() {
 		electId = Integer.valueOf(FacesContext.getCurrentInstance()
 				.getExternalContext().getRequestParameterMap().get("elId"));
 		trId = Integer.valueOf(FacesContext.getCurrentInstance()
 				.getExternalContext().getRequestParameterMap().get("trId"));
 		System.out.println("private key is: " + privateKey);
 		System.out.println("election id is: " + electId);
 		Election election = ElectionDP.getElection(electId);
 		ElGamalHelper gamal = new ElGamalHelper(election.getPublicKey(),
 				privateKey);
 
 		ArrayList<CutVote> votes = ElectionVoteDP.getCutVotes(election.getId());
 		for (CutVote vote : votes) {
 			System.out.println("encoded answer sequence: "
 					+ vote.getAnswersSequence());
 			vote.setAnswersSequence(gamal.decodeBigInt(vote
 					.getAnswersSequence()));
 			System.out.println("answer sequence: " + vote.getAnswersSequence());
 		}
 		ElectionVoteDP.updateCutVotes(votes, election.getId());
 		if(ElectionTrusteeDP.setTrusteeDecoded(trId, electId)) {
 			countVotes(votes, electId);
 		}
 		return "ThankYou?faces-redirect=true";
 	}
 	
 	private void countVotes(ArrayList<CutVote> votes, int elId) {
 		ArrayList<Answer> answers = ElectionAnswerDP.getElectionAnswers(elId);
 		List<Integer> answersSequence;
 		for(CutVote vote : votes) {
 			answersSequence = StringHelper.converStringToInttList(vote.getAnswersSequence());
 			int ans = answersSequence.get(vote.getAnswerId()-1);
			answers.get(ans-1).incNumberofVotes();
 		}
 		ElectionAnswerDP.insertAnswers(elId, answers);
 		return;
 	}
 
 	public String getPrivateKey() {
 		return privateKey;
 	}
 
 	public void setPrivateKey(String privateKey) {
 		this.privateKey = privateKey;
 	}
 
 	public boolean isValidToken() {
 		return validToken;
 	}
 
 	public void setValidToken(boolean validToken) {
 		this.validToken = validToken;
 	}
 
 	public int getElectId() {
 		return electId;
 	}
 
 	public void setElectId(int electId) {
 		this.electId = electId;
 	}
 
 	public int getTrId() {
 		return trId;
 	}
 
 	public void setTrId(int trId) {
 		this.trId = trId;
 	}
 	
 }
