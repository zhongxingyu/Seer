 package com.hashin.project.bean;
 
 import java.util.List;
 
 public class FormListBean
 {
 
     private List<ElectionsConstsBean> electionList ;
     private List<ElectionsCandidatesBean> candidateList ;
     private List<VotersUserBean> voterList;
     private String customMessage;
    private List<ConstituenciesBean> constsList;
     
     
  
	public List<ConstituenciesBean> getConstsList() {
		return constsList;
	}
	public void setConstsList(List<ConstituenciesBean> constsList) {
		this.constsList = constsList;
	}
	public List<VotersUserBean> getVoterList()
     {
         return voterList;
     }
     public void setVoterList(List<VotersUserBean> voterList)
     {
         this.voterList = voterList;
     }
     public String getCustomMessage()
     {
         return customMessage;
     }
     public void setCustomMessage(String customMessage)
     {
         this.customMessage = customMessage;
     }
     public List<ElectionsConstsBean> getElectionList()
     {
         return electionList;
     }
     public void setElectionList(List<ElectionsConstsBean> electionList)
     {
         this.electionList = electionList;
     }
     public List<ElectionsCandidatesBean> getCandidateList()
     {
         return candidateList;
     }
     public void setCandidateList(List<ElectionsCandidatesBean> candidateList)
     {
         this.candidateList = candidateList;
     }  
     
     
     
 }
