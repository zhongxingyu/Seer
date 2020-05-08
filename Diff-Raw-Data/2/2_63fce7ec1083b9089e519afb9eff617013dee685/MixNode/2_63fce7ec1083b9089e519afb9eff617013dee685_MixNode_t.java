 package com.rau.evoting.beans;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.context.FacesContext;
 import javax.mail.MessagingException;
 
 import com.rau.evoting.ElGamal.ElGamalHelper;
 import com.rau.evoting.data.ElectionDP;
 import com.rau.evoting.data.ElectionTrusteeDP;
 import com.rau.evoting.data.ElectionVoteDP;
 import com.rau.evoting.models.CutVote;
 import com.rau.evoting.models.Election;
 import com.rau.evoting.models.Trustee;
 import com.rau.evoting.utils.MailService;
 import com.rau.evoting.utils.Util;
 
 public class MixNode {
 	
 	private Trustee trustee;
 	private boolean validToken;
 	private ArrayList<CutVote> votes;	
 	private boolean showReEncrypt;
 	private boolean showThankYou;
 	
 	public MixNode() {
 		Map<String, String> reqMap = FacesContext.getCurrentInstance()
 				.getExternalContext().getRequestParameterMap();
 		validToken = true;
 		showReEncrypt = false;
 		showThankYou = false;
 		if (reqMap.containsKey("token")) {
 			int trId = Integer.valueOf(reqMap.get("trId"));
 			String token = reqMap.get("token");
 			trustee = ElectionTrusteeDP.getElectionTrustee(trId);
 			if (!trustee.getToken().equals(token)) {
 				validToken = false;
 			} else {
 				validToken = true;
 				votes = ElectionVoteDP.getCutVotes(trustee.getElectId(), trustee.getMixServer()-1);
 			}
 		}
 	}
 
 	public String shuffle() {
 		Util.shuffle(votes);
 		showReEncrypt = true;
 		return "";
 	}
 	
 	public String reencrypt() {
 		Election election = ElectionDP.getElection(trustee.getElectId());
 		
 		ElGamalHelper gamal = new ElGamalHelper(trustee.getPublicKey());
 		for (CutVote vote : votes) {
			vote.setAnswersSequence(gamal.reEncodeBigInt(vote
 					.getAnswersSequence())); // change to reencrypt
 		}
 		ElectionVoteDP.updateCutVotes(votes, election.getId(), trustee.getMixServer()-1);
 		
 		ElectionDP.setElectionMixStage(election.getId(), trustee.getMixServer());
 		
 		Trustee tr = ElectionTrusteeDP.getTrusteeByMixServer(trustee.getElectId(), trustee.getMixServer()+1);
 		if(tr == null) {
 			ElectionDP.setElectionDecode(election.getId());
 			//send mail to all trustees make async!!!!!
 			List<Trustee> trustees = ElectionTrusteeDP.getElectionTrustees(election.getId());
 			String message = "Please follow this link to upload your private key and decode election votes: \n";
 			String title = "Trustee for " + election.getName() + " election";
 			String url = "http://localhost:8080/Evoting/DecodeVotes.xhtml?elId=" + election.getId();
 			for(Trustee t : trustees) {
 				url += "&trId=" + t.getId() + "&token=" + t.getToken();
 				message += url;
 				try {
 					MailService.sendMessage(t.getEmail(), title, message);
 				} catch (MessagingException e) {
 					e.printStackTrace();
 				}
 			}
 		} else {
 			String title = "Trustee for " + election.getName() + " election";
 			String message = "Please follow this link to open your mix node: \n";
 			String url ="http://localhost:8080/Evoting/MixNode.xhtml?trId=" + tr.getId() + "&token=" + tr.getToken();
 			message += url;
 			try {
 				MailService.sendMessage(tr.getEmail(), title, message);
 			} catch (MessagingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		showThankYou = true;
 		return "";
 	}
 	
 	public boolean isValidToken() {
 		return validToken;
 	}
 
 	public void setValidToken(boolean validToken) {
 		this.validToken = validToken;
 	}
 
 	public boolean isShowReEncrypt() {
 		return showReEncrypt;
 	}
 
 	public void setShowReEncrypt(boolean showReEncrypt) {
 		this.showReEncrypt = showReEncrypt;
 	}
 
 	public boolean isShowThankYou() {
 		return showThankYou;
 	}
 
 	public void setShowThankYou(boolean showThankYou) {
 		this.showThankYou = showThankYou;
 	}
 			
 }
