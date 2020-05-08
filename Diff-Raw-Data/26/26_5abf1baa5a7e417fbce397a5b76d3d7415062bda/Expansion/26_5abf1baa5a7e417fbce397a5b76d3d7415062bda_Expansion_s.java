 package no.ovitas.compass2.config;
 
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 /**
  * @author csanyi
  * 
  */
 public class Expansion {
 	
 	// Attributes
 
 	private Logger logger = Logger.getLogger(this.getClass());
 	private boolean useRandomWeight;
 	private boolean prefixMatch;
	private String exansionThreshold;
 	private String maxNumOfTopicToExpand;
 	private AssociationTypes associationTypes;
 
 	// Getter / setter methods
 
 	public boolean getUseRandomWeight() {
 		return useRandomWeight;
 	}
 
 	public void setUseRandomWeight(String useRandomWeight) {
 		if(useRandomWeight != null && useRandomWeight.equals("yes")) {
 			this.useRandomWeight = true;
 		} else {
 			this.useRandomWeight = false;
 		}
 	}
 
 	public boolean getPrefixMatch() {
 		return prefixMatch;
 	}
 
 	public void setPrefixMatch(String prefixMatch) {
 		if(prefixMatch != null && prefixMatch.equals("yes")) {
 			this.prefixMatch = true;
 		} else {
 			this.prefixMatch = false;
 		}
 	}
 
	public String getExansionThreshold() {
		return exansionThreshold;
 	}
 
	public void setExansionThreshold(String exansionThreshold) {
		this.exansionThreshold = exansionThreshold;
 	}
 
 	public String getMaxNumOfTopicToExpand() {
 		return maxNumOfTopicToExpand;
 	}
 
 	public void setMaxNumOfTopicToExpand(String maxNumOfTopicToExpand) {
 		this.maxNumOfTopicToExpand = maxNumOfTopicToExpand;
 	}
 	
 	public AssociationTypes getAssociationTypes() {
 		return associationTypes;
 	}
 
 	public void setAssociationTypes(AssociationTypes associationTypes) {
 		this.associationTypes = associationTypes;
 	}
 	
 	// Constructors
 
 	public Expansion() {
 		associationTypes = new AssociationTypes();
 	}
 
 	// Methods
 }
