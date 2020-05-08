 /**
  * @author schroedlni
  * 
 * $Id: SavedQueryManagerImpl.java,v 1.6 2007-07-31 12:02:21 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.5  2007/03/28 18:01:51  pandyas
  * Modified for the following Test Track items:
  * #462 - Customized search for carcinogens for Jackson Lab data
  * #494 - Advanced search for Carcinogens for Jackson Lab data
  *
  * Revision 1.4  2006/10/17 16:14:05  pandyas
  * modified during development of caMOD 2.2 - various
  *
  * Revision 1.3  2006/05/22 20:10:18  schroedn
  * Added ability to save keyword searches
  *
  * Revision 1.2  2006/05/10 16:38:42  schroedn
  * Added save for Transient Instance
  *
  * Revision 1.1  2006/05/10 14:14:33  schroedn
  * New Features - Changes from code review
  *
  * Revision 1.1  2006/04/28 19:20:14  schroedn
  * Defect #238, 261
  * Search Result Columns and Saving Queries manager Impl
  *
  * 
  */
 
 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.domain.SavedQuery;
 import gov.nih.nci.camod.domain.SavedQueryAttribute;
 import gov.nih.nci.camod.service.SavedQueryManager;
 import gov.nih.nci.camod.webapp.form.SearchData;
 import gov.nih.nci.camod.webapp.form.SearchForm;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 /**
  * @arthur schroedn
  * 
  */
 public class SavedQueryManagerImpl extends BaseManager implements
 		SavedQueryManager {
 	/**
 	 * Save a new SavedQuery
 	 * 
 	 * @param inSavedQuery
 	 *            the SavedQuery to save
 	 * 
 	 * @exception Exception
 	 *                when anything goes wrong.
 	 */
 	public void save(SavedQuery inSavedQuery) throws Exception {
		log.debug("Entering QueryStorageManagerImpl.saveQuery");
 
 		// Save to db
 		super.save(inSavedQuery);
 	}
 
 	/**
 	 * Build the from the set of savedQueryAttributes the searchForm to populate
 	 * search fields
 	 * 
 	 * @param inSavedQueryAttribute
 	 *            set of saved query attributes
 	 * @param inSearchForm
 	 *            form used to prepopulate the search form
 	 * 
 	 * @exception Exception
 	 *                when anything goes wrong.
 	 */
 	public void buildSearchData(Set<SavedQueryAttribute> inSavedQueryAttribute,
 			SearchForm inSearchForm) throws Exception {
		log.debug("Entering QueryStorageManagerImpl.buildSearchData");
 
 		ResourceBundle theBundle = ResourceBundle
 				.getBundle("ApplicationResources_en");
 		Iterator<SavedQueryAttribute> sqaIter = inSavedQueryAttribute
 				.iterator();
 		while (sqaIter.hasNext()) {
 			SavedQueryAttribute theSavedQueryAttribute = sqaIter.next();
 			// System.out.println( theSavedQueryAttribute.getAttributeName() + "
 			// = " + theSavedQueryAttribute.getAttributeValue() );
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.Keyword"))) {
 				inSearchForm.setKeyword(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.ModelDescriptor"))) {
 				inSearchForm.setModelDescriptor(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 			
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.PIName"))) {
 				inSearchForm.setPiName(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 			
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.OrganTissueCode"))) {
 				inSearchForm.setOrganTissueCode(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.OrganTissueName"))) {
 				inSearchForm.setOrganTissueName(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.Organ"))) {
 				inSearchForm.setOrgan(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.DiagnosisCode"))) {
 				inSearchForm.setDiagnosisCode(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.DiagnosisName"))) {
 				inSearchForm.setDiagnosisName(theSavedQueryAttribute
 						.getAttributeValue());
 			}	
 			
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.TumorClassification"))) {
 				inSearchForm.setTumorClassification(theSavedQueryAttribute
 						.getAttributeValue());
 			}			
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.Species"))) {
 				inSearchForm.setSpecies(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.Phenotype"))) {
 				inSearchForm.setPhenotype(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.GeneName"))) {
 				inSearchForm.setGeneName(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 			
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.isEngineeredTransgene"))) {
 				if (theSavedQueryAttribute.getAttributeValue().equals("true")) {
 					inSearchForm.setEngineeredTransgene(true);
 				}
 			}			
 			
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.isTargetedModification"))) {
 				if (theSavedQueryAttribute.getAttributeValue().equals("true")) {
 					inSearchForm.setTargetedModification(true);
 				}
 			}			
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.GenomicSegDesignator"))) {
 				inSearchForm.setGenomicSegDesignator(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.InducedMutationAgent"))) {
 				inSearchForm.setInducedMutationAgent(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.CarcinogenicIntervention"))) {
 				inSearchForm.setCarcinogenicIntervention(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 			
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.AgentName"))) {
 				inSearchForm.setAgentName(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.CellLine"))) {
 				inSearchForm.setCellLine(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 			
 			if (theSavedQueryAttribute
 					.getAttributeName().equals(theBundle
 									.getString("criteria.isSearchTherapeuticApproaches"))) {
 				if (theSavedQueryAttribute.getAttributeValue().equals("true")) {
 					inSearchForm.setSearchTherapeuticApproaches(true);
 				}
 			}
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.TherapeuticApproach"))) {
 				inSearchForm.setTherapeuticApproach(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.isSearchHistoMetastasis"))) {
 				if (theSavedQueryAttribute.getAttributeValue().equals("true")) {
 					inSearchForm.setSearchHistoMetastasis(true);
 				}
 			}
 			
 			if (theSavedQueryAttribute
 					.getAttributeName().equals(theBundle
 									.getString("criteria.isSearchTransientInterference"))) {
 				if (theSavedQueryAttribute.getAttributeValue().equals("true")) {
 					inSearchForm.setSearchTransientInterference(true);
 				}						
 			}
 			
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.isSearchMicroArrayData"))) {
 				if (theSavedQueryAttribute.getAttributeValue().equals("true")) {
 					inSearchForm.setSearchMicroArrayData(true);
 				}
 			}
 			
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.isSearchImageData"))) {
 				if (theSavedQueryAttribute.getAttributeValue().equals("true")) {
 					inSearchForm.setSearchImageData(true);
 				}
 			}			
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.isSearchToolStrain"))) {
 				if (theSavedQueryAttribute.getAttributeValue().equals("true")) {
 					inSearchForm.setSearchToolStrain(true);
 				}
 			}			
 
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.ExternalSource"))) {
 				log.info("<SavedQueryManagerImpl> adding criteria.ExternalSource to criteria list");
 				inSearchForm.setSurgery(theSavedQueryAttribute
 						.getAttributeValue());
 			}
 			
 			if (theSavedQueryAttribute.getAttributeName().equals(
 					theBundle.getString("criteria.isSearchGraft"))) {
 				if (theSavedQueryAttribute.getAttributeValue().equals("true")) {
 					inSearchForm.setSearchGraft(true);
 				}
 			}			
 		}
 	}
 
 	/**
 	 * From the search form data build a set of search criteria to save for a
 	 * SavedQuery
 	 * 
 	 * @param inSearchData
 	 *            the search data from a search
 	 * 
 	 * @return the set of criteria used in the search
 	 * @exception Exception
 	 *                when anything goes wrong.
 	 */
 	public Set<SavedQueryAttribute> buildCriteriaList(SearchData inSearchData)
 			throws Exception {
 		ResourceBundle theBundle = ResourceBundle
 				.getBundle("ApplicationResources_en");
 
 		Set<SavedQueryAttribute> criteriaList = new HashSet<SavedQueryAttribute>();
		log.info("SaveQueryAction.save criteriaList.size()"
 				+ criteriaList.size());
 
 		// Keyword search
 		if (inSearchData.getKeyword() != null
 				&& inSearchData.getKeyword().length() > 0) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle.getString("criteria.Keyword"));
 			sqa.setAttributeValue(inSearchData.getKeyword());
 			criteriaList.add(sqa);
 		}
 
 		// PI criteria
 		if (inSearchData.getPiName() != null
 				&& inSearchData.getPiName().length() > 0) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle.getString("criteria.PIName"));
 			sqa.setAttributeValue(inSearchData.getPiName());
 			criteriaList.add(sqa);
 		}
 
 		// Model Secriptor criteria
 		if (inSearchData.getModelDescriptor() != null
 				&& inSearchData.getModelDescriptor().length() > 0) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle
 					.getString("criteria.ModelDescriptor"));
 			sqa.setAttributeValue(inSearchData.getModelDescriptor());
 			criteriaList.add(sqa);
 		}
 
 		// Species
 		if (inSearchData.getSpecies() != null
 				&& inSearchData.getSpecies().length() > 0) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle.getString("criteria.Species"));
 			sqa.setAttributeValue(inSearchData.getSpecies());
 			criteriaList.add(sqa);
 		}
 
 		// Phenotype
 		if (inSearchData.getPhenotype() != null
 				&& inSearchData.getPhenotype().length() > 0) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle.getString("criteria.Phenotype"));
 			sqa.setAttributeValue(inSearchData.getPhenotype());
 			criteriaList.add(sqa);
 		}
 
 		// Cell Line
 		if (inSearchData.getCellLine() != null
 				&& inSearchData.getCellLine().length() > 0) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle.getString("criteria.CellLine"));
 			sqa.setAttributeValue(inSearchData.getCellLine());
 			criteriaList.add(sqa);
 		}
 
 		// Organ (Tissue Name)
 		if (inSearchData.getOrganTissueName() != null
 				&& inSearchData.getOrganTissueName().length() > 0) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle
 					.getString("criteria.OrganTissueName"));
 			sqa.setAttributeValue(inSearchData.getOrganTissueName());
 			criteriaList.add(sqa);
 
 			// Organ (Tissue Code)
 			if (inSearchData.getOrganTissueCode() != null
 					&& inSearchData.getOrganTissueCode().length() > 0) {
 				sqa = new SavedQueryAttribute();
 				sqa.setAttributeName(theBundle
 						.getString("criteria.OrganTissueCode"));
 				sqa.setAttributeValue(inSearchData.getOrganTissueCode());
 				criteriaList.add(sqa);
 			}
 		}
 
 		// Organ
 		if (inSearchData.getOrgan() != null
 				&& inSearchData.getOrgan().length() > 0) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle.getString("criteria.Organ"));
 			sqa.setAttributeValue(inSearchData.getOrgan());
 			criteriaList.add(sqa);
 		}
 
 		// Diagnosis (Name)
 		if (inSearchData.getDiagnosisName() != null
 				&& inSearchData.getDiagnosisName().length() > 0) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle.getString("criteria.DiagnosisName"));
 			sqa.setAttributeValue(inSearchData.getDiagnosisName());
 			criteriaList.add(sqa);
 
 			// Diagnosis (Code)
 			if (inSearchData.getDiagnosisCode() != null
 					&& inSearchData.getDiagnosisCode().length() > 0) {
 				sqa = new SavedQueryAttribute();
 				sqa.setAttributeName(theBundle
 						.getString("criteria.DiagnosisCode"));
 				sqa.setAttributeValue(inSearchData.getDiagnosisCode());
 				criteriaList.add(sqa);
 			}
 		}
 
 		if (inSearchData.getTumorClassification() != null
 				&& inSearchData.getTumorClassification().length() > 0) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle
 					.getString("criteria.TumorClassification"));
 			sqa.setAttributeValue(inSearchData.getTumorClassification());
 			criteriaList.add(sqa);
 		}
 
 		// ///////////////////////////////////////
 		// Carcinogenic interventions
 		// ///////////////////////////////////////
 
 		if (inSearchData.getCarcinogenicIntervention() != null 
 				&& inSearchData.getCarcinogenicIntervention().length() > 0) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle
 					.getString("criteria.CarcinogenicIntervention"));
 			sqa.setAttributeValue(inSearchData.getCarcinogenicIntervention());
 			criteriaList.add(sqa);
 
 			// Agent Name
 			if (inSearchData.getAgentName() != null
 					&& inSearchData.getAgentName().length() > 0) {
 				sqa = new SavedQueryAttribute();
 				sqa.setAttributeName(theBundle
 						.getString("criteria.AgentName"));
 				sqa.setAttributeValue(inSearchData.getAgentName());
 				criteriaList.add(sqa);
 			}
 		}
 		
 		// ///////////////////////////////////////
 		// Genetic Description
 		// ///////////////////////////////////////
 
 		// is it an Engineered Transgene
 		if (inSearchData.isEngineeredTransgene() == true) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle
 					.getString("criteria.isEngineeredTransgene"));
 			sqa.setAttributeValue("true");
 			criteriaList.add(sqa);
 		}
 
 		// is it an Targeted Modification
 		if (inSearchData.isTargetedModification() == true) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle
 					.getString("criteria.isTargetedModification"));
 			sqa.setAttributeValue("true");
 			criteriaList.add(sqa);
 		}
 
 		// Gene Name
 		if (inSearchData.getGeneName() != null
 				&& inSearchData.getGeneName().length() > 0) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle.getString("criteria.GeneName"));
 			sqa.setAttributeValue(inSearchData.getGeneName());
 			criteriaList.add(sqa);
 		}
 
 		// Genomic Desginator
 		if (inSearchData.getGenomicSegDesignator() != null
 				&& inSearchData.getGenomicSegDesignator().length() > 0) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle
 					.getString("criteria.GenomicSegDesignator"));
 			sqa.setAttributeValue(inSearchData.getGenomicSegDesignator());
 			criteriaList.add(sqa);
 		}
 
 		// Induced Mutation Agent
 		if (inSearchData.getInducedMutationAgent() != null
 				&& inSearchData.getInducedMutationAgent().length() > 0) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle
 					.getString("criteria.InducedMutationAgent"));
 			sqa.setAttributeValue(inSearchData.getInducedMutationAgent());
 			criteriaList.add(sqa);
 		}
 
 		// Is it a therapeutic approaches
 		if (inSearchData.isSearchTherapeuticApproaches() == true) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle
 					.getString("criteria.isSearchTherapeuticApproaches"));
 			sqa.setAttributeValue("true");
 			criteriaList.add(sqa);
 
 			// Therapeutic Approach
 			if (inSearchData.getTherapeuticApproach() != null
 					&& inSearchData.getTherapeuticApproach().length() > 0) {
 				sqa = new SavedQueryAttribute();
 				sqa.setAttributeName(theBundle
 						.getString("criteria.TherapeuticApproach"));
 				sqa.setAttributeValue(inSearchData.getTherapeuticApproach());
 				criteriaList.add(sqa);
 			}
 		}
 
 		// Is it an External Source
 		if (inSearchData.getExternalSource() != null
 				&& inSearchData.getExternalSource().length() > 0) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa
 					.setAttributeName(theBundle
 							.getString("criteria.ExternalSource"));
 			sqa.setAttributeValue(inSearchData.getExternalSource());
 			criteriaList.add(sqa);
 		}
 
 		// Is it a metastasis
 		if (inSearchData.isSearchHistoMetastasis() == true) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle
 					.getString("criteria.isSearchHistoMetastasis"));
 			sqa.setAttributeValue("true");
 			criteriaList.add(sqa);
 		}
 
 		// Is it a Transient Interface
 		if (inSearchData.isSearchTransientInterference() == true) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle
 					.getString("criteria.isSearchTransientInterference"));
 			sqa.setAttributeValue("true");
 			criteriaList.add(sqa);
 		}
 
 		// Is it a microarray data
 		if (inSearchData.isSearchMicroArrayData() == true) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle
 					.getString("criteria.isSearchMicroArrayData"));
 			sqa.setAttributeValue("true");
 			criteriaList.add(sqa);
 		}
 		
 		// Is it a image data
 		if (inSearchData.isSearchImageData() == true) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle
 					.getString("criteria.isSearchImageData"));
 			sqa.setAttributeValue("true");
 			criteriaList.add(sqa);
 		}		
 
 		// Is it a tool strain
 		if (inSearchData.isSearchToolStrain() == true) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle
 					.getString("criteria.isSearchToolStrain"));
 			sqa.setAttributeValue("true");
 			criteriaList.add(sqa);
 		}
 
 		// Is it a graft
 		if (inSearchData.isSearchGraft() == true) {
 			SavedQueryAttribute sqa = new SavedQueryAttribute();
 			sqa.setAttributeName(theBundle
 					.getString("criteria.isSearchGraft"));
 			sqa.setAttributeValue("true");
 			criteriaList.add(sqa);
 		}
 
 		return criteriaList;
 	}
 
 	/**
 	 * Get a specific SavedQuery by id
 	 * 
 	 * @param id
 	 *            the unique id for a savedQuery
 	 * 
 	 * @return the matching SavedQuery object, or null if not found.
 	 * @exception Exception
 	 *                when anything goes wrong.
 	 */
 	public SavedQuery get(String id) throws Exception {
		log.debug("In QueryStorageManagerImpl.get");
 		SavedQuery theSavedQuery = (SavedQuery) super.get(id, SavedQuery.class);
 		return theSavedQuery;
 	}
 
 	/**
 	 * Get all SavedQueries with saved value equal to 0 from a given user
 	 * 
 	 * @param userName
 	 *            the username which to return all SavedQueries
 	 * 
 	 * @return the List of matching SavedQuery objects, or null if not found.
 	 * @exception Exception
 	 *                when anything goes wrong.
 	 */
 	public List getAllByUsername(String userName) throws Exception {
		log.debug("Entering QueryStorageManagerImpl.getAllByUsername");
 
 		List sqList = QueryManagerSingleton.instance().getQueriesByParty(
 				userName);
 
 		// Prune the list, any queries > 20
 		for (int i = 0; i < sqList.size(); i++) {
 			if (i >= 20) {
 				SavedQuery sq = (SavedQuery) sqList.get(i);
 				remove(sq.getId().toString());
 			}
 		}
 
 		return QueryManagerSingleton.instance().getQueriesByParty(userName);
 	}
 
 	/**
 	 * Get all SavedQueries from a given user
 	 * 
 	 * @param userName
 	 *            the username which to return all SavedQueries
 	 * 
 	 * @return the List of matching SavedQuery objects, or null if not found.
 	 * @exception Exception
 	 *                when anything goes wrong.
 	 */
 	public List getSavedQueriesByUsername(String userName) throws Exception {
		log.debug("Entering QueryStorageManagerImpl.getSavedQueriesByUsername");
 		return QueryManagerSingleton.instance()
 				.getSavedQueriesByParty(userName);
 	}
 
 	/**
 	 * Update a SavedQuery
 	 * 
 	 * @param inSavedQuery
 	 *            the SavedQuery to update
 	 * 
 	 * @exception Exception
 	 *                when anything goes wrong.
 	 */
 	public void update(SavedQuery inSavedQuery) throws Exception {
 		// Save to db
		log.debug("Entering QueryStorageManagerImpl.update");
 		super.save(inSavedQuery);
 	}
 
 	/**
 	 * Remove a SavedQuery by id
 	 * 
 	 * @param id
 	 *            the unique id for the SavedQueryto delete
 	 * 
 	 * @exception Exception
 	 *                when anything goes wrong.
 	 */
 	public void remove(String id) throws Exception {
 		// delete from db
		log.debug("Entering QueryStorageManagerImpl.remove");
 		super.remove(id, SavedQuery.class);
 	}
 
 }
