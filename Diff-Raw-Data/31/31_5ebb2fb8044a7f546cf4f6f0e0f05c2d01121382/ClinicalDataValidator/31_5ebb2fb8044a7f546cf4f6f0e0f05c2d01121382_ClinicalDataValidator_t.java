 package gov.nih.nci.rembrandt.queryservice.validation;
 
 import gov.nih.nci.caintegrator.dto.critieria.SampleCriteria;
 import gov.nih.nci.caintegrator.dto.de.SampleIDDE;
 import gov.nih.nci.caintegrator.dto.query.QueryType;
 import gov.nih.nci.caintegrator.dto.view.ViewFactory;
 import gov.nih.nci.caintegrator.dto.view.ViewType;
 import gov.nih.nci.caintegrator.enumeration.ClinicalFactorType;
 import gov.nih.nci.rembrandt.dto.query.ClinicalDataQuery;
 import gov.nih.nci.rembrandt.dto.query.CompoundQuery;
 import gov.nih.nci.rembrandt.queryservice.QueryManager;
 import gov.nih.nci.rembrandt.queryservice.ResultsetManager;
 import gov.nih.nci.rembrandt.queryservice.resultset.DimensionalViewContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.Resultant;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.BioSpecimenResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleViewResultsContainer;
 import gov.nih.nci.rembrandt.service.findings.strategies.StrategyHelper;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.naming.OperationNotSupportedException;
 
 import org.apache.log4j.Logger;
 
 /**
  * @author sahnih
  *	Allows validatoion of samples based on collection of ClinicalFactorTypes
  */
 
 public class ClinicalDataValidator {
 	private static Logger logger = Logger.getLogger(ClinicalDataValidator.class);	
 
 	/**
 	 * In order to return the valid samples that meet every condition in the ClinicalFactorType collection
 	 * we need to save returns for each condition is each seperate list
 	 * and then create a set based on all the conditions
 	 * @throws Exception 
 	 *
 	 */
	public static Collection<SampleResultset> getValidatedSampleResultsets(Collection<SampleIDDE> sampleList, Collection<ClinicalFactorType> clinicalFactors) throws Exception  {
 		Collection<SampleResultset> validSampleResultsets = new ArrayList<SampleResultset>();
 		if(sampleList != null && sampleList.size() > 0){
 			//Create a map of sample collections for each ClinicalFactorType that were passed
 			Map<ClinicalFactorType, Collection<SampleIDDE>> clinicalFactorMap = new HashMap<ClinicalFactorType,Collection<SampleIDDE>>();
 			//execute Clinical Query
 			Collection<SampleResultset> sampleResultsets = executeQuery(sampleList);
 			for(ClinicalFactorType clinicalFactor: clinicalFactors){
 				//samples associated with each clinical factor are stored in a seperate collection
 				Collection<SampleIDDE> samples = new HashSet<SampleIDDE>();
 				for(SampleResultset sampleResultset: sampleResultsets) {
 					if(sampleResultset != null){							
 						switch(clinicalFactor){
 							case AgeAtDx:
 								if( sampleResultset.getAgeGroup()!= null &&  sampleResultset.getAgeGroup().getValue() != null){
 									samples.add(new SampleIDDE(sampleResultset.getBiospecimen().getValue().toString()));
 								}
 								break;		 									
 							case Treatment:
 								//if( sampleResultset.()!= null  && sampleResultset.().getValue() != null){
 								//	samples.add(new SampleIDDE(sampleResultset.getBiospecimen().getValue().toString()));
 								//}
 								break;
 							case KarnofskyAssessment:
 								if( sampleResultset.getKarnofskyClinicalEvalDE()!= null && sampleResultset.getKarnofskyClinicalEvalDE().getValue() != null){
 									samples.add(new SampleIDDE(sampleResultset.getBiospecimen().getValue().toString()));
 								}
 								break;
 							case Survival:
 								if( sampleResultset.getSurvivalLengthRange()!= null && sampleResultset.getSurvivalLengthRange().getValue() != null){
 									samples.add(new SampleIDDE(sampleResultset.getBiospecimen().getValue().toString()));
 								}
 								break;
 							case Gender:
 								if( sampleResultset.getGenderCode()!= null && sampleResultset.getGenderCode().getValue() != null){
 									samples.add(new SampleIDDE(sampleResultset.getBiospecimen().getValue().toString()));
 								}
 								break;
 							case Disease:
 								if( sampleResultset.getDisease()!= null && sampleResultset.getDisease().getValue() != null){
 									samples.add(new SampleIDDE(sampleResultset.getBiospecimen().getValue().toString()));
 								}
 								break;
 						}//switch
 					}//if
 				}//for loop
 				clinicalFactorMap.put(clinicalFactor,samples);
 			}//for loop
 			sampleList = getValidSampleSet(sampleList,clinicalFactorMap);
 			
 			for(SampleResultset sampleResultset: sampleResultsets) {
 				for(SampleIDDE sampleIDDE: sampleList) {
 					if(sampleIDDE != null  && sampleResultset != null  &&
 							sampleResultset.getBiospecimen().getValueObject().equals(sampleIDDE.getValueObject())){	
 						validSampleResultsets.add(sampleResultset);
 					}						
 				}
 			}
 			
 		}
 	  return validSampleResultsets;
 	}
 		private static Collection<SampleIDDE> getValidSampleSet(Collection<SampleIDDE> sampleList, Map<ClinicalFactorType, Collection<SampleIDDE>> clinicalFactorMap) throws OperationNotSupportedException {
 			Collection<SampleIDDE> validSampleSet = new HashSet<SampleIDDE>();
 			validSampleSet.addAll(sampleList);
 			if(clinicalFactorMap != null){
 				//Create one large set of all valid samples that meet every ClinicalFactorType
 				Collection<ClinicalFactorType> keys = clinicalFactorMap.keySet();
 				for(ClinicalFactorType key: keys){
 					Collection<SampleIDDE> set= clinicalFactorMap.get(key);
 					validSampleSet.retainAll(set);
 				}
 
 			}
 		return validSampleSet;
 	}
 		@SuppressWarnings("unchecked")
 		private static Collection<SampleResultset> executeQuery(Collection<SampleIDDE> sampleList) throws Exception{
 			Collection<SampleResultset> sampleResultsets =  Collections.EMPTY_LIST;
 			//create a ClinicalDataQuery to contrain by Insitition group
 			 ClinicalDataQuery clinicalDataQuery = (ClinicalDataQuery) QueryManager.createQuery(QueryType.CLINICAL_DATA_QUERY_TYPE);
 			SampleCriteria sampleCriteria = new SampleCriteria();
 			sampleCriteria.setSampleIDs(sampleList);
 			clinicalDataQuery.setSampleIDCrit( sampleCriteria);
 			
 			Resultant resultant;
 			try {
 				CompoundQuery compoundQuery = new CompoundQuery(clinicalDataQuery);
 				compoundQuery.setAssociatedView(ViewFactory
 	                .newView(ViewType.CLINICAL_VIEW));
 				resultant = ResultsetManager.executeCompoundQuery(compoundQuery);
 	  		}
 	  		catch (Throwable t)	{
 	  			logger.error("Error Executing the query/n"+ t.getMessage());
 	  			throw new Exception("Error executing clinical query/n"+t.getMessage());
 	  		}
 	  		if(resultant != null) {      
 	  			SampleViewResultsContainer svrContainer = null;
 	  			/*
 	  			 * These are currently the only two results containers that we have to
 	  			 * worry about at this time, I believe.
 	  			 */
 	  			if(resultant.getResultsContainer() instanceof DimensionalViewContainer) {
 	  				//Get the SampleViewResultsContainer from the DimensionalViewContainer
 	  				DimensionalViewContainer dvContainer = (DimensionalViewContainer)resultant.getResultsContainer();
 	  				svrContainer = dvContainer.getSampleViewResultsContainer();
 	  			}else if(resultant.getResultsContainer() instanceof SampleViewResultsContainer) {
 	  				svrContainer = (SampleViewResultsContainer)resultant.getResultsContainer();
 	  			}						//Handle the SampleViewResultsContainers if that is what we got
 	 					if(svrContainer!=null) {
 	 						sampleResultsets = svrContainer.getBioSpecimenResultsets();
 	 					}
 		 	}
 	  		return sampleResultsets;
 		}
 }
