 package com.rau.evoting.beans;
 
 import javax.faces.event.AjaxBehaviorEvent;
 
 import com.rau.evoting.data.ElectionVoteDP;
 import com.rau.evoting.models.Vote;
 import com.rau.evoting.utils.StringHelper;
 
 public class Receipt {
 
 	private String id;
 	private Vote vote;
 	private String hash1;
 	private String hash2;
 	private boolean showReceipt;
 	private boolean receiptFound;
 
 	public Receipt() {
 		showReceipt = false;
 		receiptFound = false;
 		vote = null;
 	}
 
 	public void setBallot(AjaxBehaviorEvent event) {
 
 		try {
 			int recId = Integer.parseInt(id);
 			vote = ElectionVoteDP.getVote(recId);
 			if (vote != null) {
 				hash1 = StringHelper.getSHA256hash(vote.getEncoded1());
 				hash2 = StringHelper.getSHA256hash(vote.getEncoded2());
 				showReceipt = true;
 				receiptFound = true;
 			} else {
 				showReceipt = true;
 				receiptFound = false;
 			}
 
 		} catch (Exception e) {
			showReceipt = true;
			receiptFound = false;
 			System.out.println("Receipt -> setBallot -> Parse exception");
 		}
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public Vote getVote() {
 		return vote;
 	}
 
 	public void setVote(Vote vote) {
 		this.vote = vote;
 	}
 
 	public String getHash1() {
 		return hash1;
 	}
 
 	public void setHash1(String hash1) {
 		this.hash1 = hash1;
 	}
 
 	public String getHash2() {
 		return hash2;
 	}
 
 	public void setHash2(String hash2) {
 		this.hash2 = hash2;
 	}
 
 	public boolean isShowReceipt() {
 		return showReceipt;
 	}
 
 	public void setShowReceipt(boolean showReceipt) {
 		this.showReceipt = showReceipt;
 	}
 
 	public boolean isReceiptFound() {
 		return receiptFound;
 	}
 
 	public void setReceiptFound(boolean receiptFound) {
 		this.receiptFound = receiptFound;
 	}
 
 }
