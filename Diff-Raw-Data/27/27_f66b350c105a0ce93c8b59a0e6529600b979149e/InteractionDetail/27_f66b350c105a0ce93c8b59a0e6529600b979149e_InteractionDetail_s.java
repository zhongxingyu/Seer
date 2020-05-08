 package org.geworkbench.util.network;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.ArrayList;
 
 
 /**
  * Created by IntelliJ IDEA.
  * User: xiaoqing
  * Date: Jan 5, 2007
  * Time: 4:27:11 PM
  * To change this template use File | Settings | File Templates.
  */
 
 /**
  * The deatil of one interaction.
  */
 public class InteractionDetail implements Serializable {
 
 	private static final long serialVersionUID = -4163326138016520667L;
 	public static final String ENTREZ_GENE = "Entrez Gene";
 
 	private String dSGeneId;
 	private String dSGeneName;
 	private String dbSource;	 
 	private String interactionType;
 	private Short evidenceId;
 	private List<Confidence> confidenceList;
 	
 
 	public InteractionDetail(String dSGeneId, String dSGeneName,
 			String dbSource, String interactionType,
 			String interactionId, Short evidenceId) {
 
 		this.dSGeneId = dSGeneId;
 		this.dSGeneName = dSGeneName;
 		this.dbSource = dbSource;		 
 		this.interactionType = interactionType;
 		this.evidenceId = evidenceId;
 	}
 
 	public String getdSGeneId() {
 		return dSGeneId;
 	}
 
 	public String getInteractionGene() {
 		 
 		if (dbSource.equalsIgnoreCase(ENTREZ_GENE))
 			return dSGeneId;
 		else
 		    return dSGeneName;
 	}
 
 	public void setdSGeneId(String dSGeneId) {
 		this.dSGeneId = dSGeneId;
 	}
 
 	public String getdSGeneName() {
 		return dSGeneName;
 	}
 
 	public void setdSGeneName(String dSGeneName) {
 		this.dSGeneName = dSGeneName;
 	}
 
 	public String getDbSource() {
 		return this.dbSource;
 	}
 
 	public void setDbSource(String dbSource) {
 		this.dbSource = dbSource;
 	}
 
 	public boolean isGeneEntrezId() {
 		return this.dbSource.equalsIgnoreCase(ENTREZ_GENE);
 	}
 
 	public double getConfidenceValue(int usedConfidenceType) {
 		for (int i=0; i<confidenceList.size(); i++)
 			if (confidenceList.get(i).getType() == usedConfidenceType)
 				return confidenceList.get(i).getScore();
 		//if usedConfidenceType is not found, return 0.
 		return 0;
 	}
 	
 	
 	public List<Short> getConfidenceTypes() {
 		List<Short> types = new ArrayList<Short>();
 		for (int i=0; i<confidenceList.size(); i++)
 			types.add(confidenceList.get(i).getType());
 		return types;
 	}
 	 
 	public void addConfidence(double score, short type) {
 		if (confidenceList == null)
 			confidenceList = new ArrayList<Confidence>();
 		confidenceList.add(new Confidence(score, type));
 	}
 
 	public String getInteractionType() {
 		return this.interactionType;
 	}
 
 	public void setInteraactionType(String interactionType) {
 		this.interactionType = interactionType;
 	}
 
 	public Short getEvidenceId() {
 		return this.evidenceId;
 	}
 	
 
 	public boolean equals(Object obj) {
 		if (obj instanceof InteractionDetail) {
 			InteractionDetail mInfo = (InteractionDetail) obj;
 			return dSGeneId.toString()
 					.equals(mInfo.dSGeneId.toString());
 		}
 		return false;
	}	
 	
 	private class Confidence implements Serializable
 	{
 		 
 		private static final long serialVersionUID = 4151510293677929250L;
 		private double score;
 		private short type;
 		
 		Confidence(double score, short type)
 		{
 			this.score = score;
 			this.type = type;
 		}
 		
 		public double getScore()
 		{
 			return score;
 		}
 		
 		public short getType()
 		{
 			return type;
 		}
 		
 	}
 
 
 }
