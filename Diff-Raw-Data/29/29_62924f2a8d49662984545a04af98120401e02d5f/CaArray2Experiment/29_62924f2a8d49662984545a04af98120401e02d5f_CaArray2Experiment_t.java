 package org.geworkbench.builtin.projects.remoteresources.carraydata;
 
 import java.io.Serializable;
 import java.util.Map;
 
 /**
  * 
  * @author zji
  * @version $Id$
  */
 public class CaArray2Experiment implements Comparable<CaArray2Experiment>,
 		Serializable {
 	private static final long serialVersionUID = 6474502004023481576L;
 
 	private String name;
 	private Map<String, String> hybridizations;
 	private String description;
 	private String[] QuantitationTypes;
 	private String experimentReferenceId;
 
	private String publicIdentifier;

	public CaArray2Experiment(String experimentReferenceId, String name, String description, String publicIdentifier) {
 		this.experimentReferenceId = experimentReferenceId;
 		this.name = name;
 		this.description = description;
		this.publicIdentifier = publicIdentifier;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public Map<String, String> getHybridizations() {
 		return hybridizations;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public String[] getQuantitationTypes() {
 		return QuantitationTypes;
 	}
 
 	public int compareTo(CaArray2Experiment o) {
 		return name.compareTo(((CaArray2Experiment) o).getName());
 	}
 
 	/**
 	 * @param hybridizations the hybridizations to set
 	 */
 	public void setHybridizations(Map<String, String> hybridizations) {
 		this.hybridizations = hybridizations;
 	}
 
 	/**
 	 * @param quantitationTypes the quantitationTypes to set
 	 */
 	public void setQuantitationTypes(String[] quantitationTypes) {
 		QuantitationTypes = quantitationTypes;
 	}
 
 	/**
 	 * @return the experimentReferenceId
 	 */
 	public String getExperimentReferenceId() {
 		return experimentReferenceId;
 	}
 	
 	@Override
 	public String toString() {
 		return name;
 	}

	/**
	 * @return the publicIdentifier
	 */
	public String getPublicIdentifier() {
		return publicIdentifier;
	}

 }
