 package gov.nih.nci.rembrandt.queryservice.queryprocessing.clinical;
 
 import gov.nih.nci.caintegrator.dto.critieria.AgeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.ChemoAgentCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.DiseaseOrGradeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.GenderCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.KarnofskyClinicalEvalCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.LanskyClinicalEvalCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.MRIClinicalEvalCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.NeuroExamClinicalEvalCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.PriorSurgeryTitleCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.RadiationTherapyCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SurgeryOutcomeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SurvivalCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.RaceCriteria;
 import gov.nih.nci.caintegrator.dto.de.ChemoAgentDE;
 import gov.nih.nci.caintegrator.dto.de.DiseaseNameDE;
 import gov.nih.nci.caintegrator.dto.de.PriorSurgeryTitleDE;
 import gov.nih.nci.caintegrator.dto.de.RaceDE;
 import gov.nih.nci.caintegrator.dto.de.RadiationTherapyDE;
 import gov.nih.nci.caintegrator.dto.de.SurgeryOutcomeDE;
 import gov.nih.nci.rembrandt.dbbean.NeuroEvaluation;
 import gov.nih.nci.rembrandt.dbbean.PatientData;
 import gov.nih.nci.rembrandt.dbbean.PriorChemotherapy;
 import gov.nih.nci.rembrandt.dbbean.PriorRadiationtherapy;
 import gov.nih.nci.rembrandt.dbbean.PriorSurgery;
 import gov.nih.nci.rembrandt.dto.query.ClinicalDataQuery;
 import gov.nih.nci.rembrandt.dto.query.Query;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.CommonFactHandler;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.QueryHandler;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultSet;
 
 import java.math.BigDecimal;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 import org.apache.ojb.broker.PersistenceBroker;
 import org.apache.ojb.broker.PersistenceBrokerFactory;
 import org.apache.ojb.broker.query.Criteria;
 import org.apache.ojb.broker.query.QueryFactory;
 import org.apache.ojb.broker.query.ReportQueryByCriteria;
 
 /**
  * @author BhattarR
  */
 public class ClinicalQueryHandler extends QueryHandler {
 	private static Logger logger = Logger.getLogger(ClinicalQueryHandler.class);
 	private Collection patientDIDs = new ArrayList();
 	private ResultSet[] clinicalEvalDataResult = null;	
 	private ResultSet[] priorRadiationResult = null;	
 	private ResultSet[] priorChemoResult = null;	
 	private ResultSet[] priorSurgeryResult = null;	
    
 
 	public ResultSet[] handle(Query query) throws Exception {
         ClinicalDataQuery clinicalQuery = (ClinicalDataQuery) query;
         Criteria allCriteria = new Criteria();
 
         buildSurvivalRangeCrit(clinicalQuery, allCriteria);
         buildAgeRangeCrit(clinicalQuery, allCriteria);
         buildGenderCrit(clinicalQuery, allCriteria);
         buildRaceCrit(clinicalQuery, allCriteria);     
        
 
         PersistenceBroker _BROKER = PersistenceBrokerFactory.defaultPersistenceBroker();     
         
         Class targetFactClass = PatientData.class;
         CommonFactHandler.addDiseaseCriteria(clinicalQuery, targetFactClass, _BROKER, allCriteria);
         CommonFactHandler.addSampleIDCriteria(clinicalQuery, targetFactClass, allCriteria);
         CommonFactHandler.addAccessCriteria(clinicalQuery, targetFactClass, allCriteria);
         
         ReportQueryByCriteria clinicalEvalQuery = getClinicalEvalSubQuery(clinicalQuery,_BROKER,NeuroEvaluation.class,NeuroEvaluation.PATIENT_DID);
         ReportQueryByCriteria priorRadiationQuery = getPriorRadiationTherapySubQuery(clinicalQuery,_BROKER,PriorRadiationtherapy.class,PriorRadiationtherapy.PATIENT_DID);
         ReportQueryByCriteria priorChemoQuery = getPriorChemoTherapySubQuery(clinicalQuery,_BROKER,PriorChemotherapy.class,PriorChemotherapy.PATIENT_DID);
         ReportQueryByCriteria priorSurgeryQuery = getPriorSurgeryTherapySubQuery(clinicalQuery,_BROKER,PriorSurgery.class,PriorSurgery.PATIENT_DID);
         
         Criteria priorTherapy = new Criteria();
         Criteria c1 = new Criteria();
         Criteria c2= new Criteria();
         Criteria c3 = new Criteria();
         
         _BROKER.close();
         
         if(clinicalEvalQuery != null) {
            allCriteria.addIn(PatientData.PATIENT_DID, clinicalEvalQuery);        
         }
         
         if(priorRadiationQuery != null) {
            // allCriteria.addIn(PatientData.PATIENT_DID, priorRadiationQuery); //this is for "AND"  
         	c1.addIn(PatientData.PATIENT_DID, priorRadiationQuery); 
            priorTherapy.addOrCriteria(c1);    
         }
         
         if(priorChemoQuery != null) {
            // allCriteria.addIn(PatientData.PATIENT_DID, priorChemoQuery);  // this is for "AND"
         	
         	c2.addIn(PatientData.PATIENT_DID, priorChemoQuery); 
         	priorTherapy.addOrCriteria(c2);   
           
          }
         
         if(priorSurgeryQuery != null) {
            //allCriteria.addIn(PatientData.PATIENT_DID, priorSurgeryQuery);   // this is for "AND" 
         	c3.addIn(PatientData.PATIENT_DID, priorSurgeryQuery);    
         	priorTherapy.addOrCriteria(c3);  
          }
        
         
        if(!priorTherapy.isEmpty()){
                allCriteria.addAndCriteria(priorTherapy);
        }
         
         PatientData[] results = executeQuery(allCriteria);
         
         if(clinicalEvalQuery != null || !priorTherapy.isEmpty() || results.length >=1) {
         	
             clinicalEvalDataResult = populateClinicalEval(clinicalQuery,patientDIDs); 
             priorRadiationResult = populatePriorRadiation(clinicalQuery,patientDIDs); 
             priorChemoResult = populatePriorChemo(clinicalQuery,patientDIDs); 
             priorSurgeryResult = populatePriorSurgery(clinicalQuery,patientDIDs); 
             
             
             results = addClinicalEvalToPatientData(results,clinicalEvalDataResult);
             results = addPriorRadiationToPatientData(results,priorRadiationResult);
             results = addPriorChemoToPatientData(results,priorChemoResult);
             results = addPriorSurgeryToPatientData(results,priorSurgeryResult);
             
         }
         
         
         return results;
     }
 
 	
 	private PatientData[] addClinicalEvalToPatientData(PatientData[] patientDataResults, ResultSet[] clinicalEvalDataResult) {
 		
 		if(patientDataResults instanceof PatientData[]) {
 			
 			
 			for(int i=0; i< patientDataResults.length;i++) {
 				PatientData ptData = (PatientData)patientDataResults[i];
 				Long patientDid = ptData.getPatientDid();
 				StringBuffer timePoints = new StringBuffer();
 				StringBuffer followUpdates = new StringBuffer();
 				StringBuffer followupMonths = new StringBuffer();							
 				StringBuffer neuroEvaluationDates = new StringBuffer();
 				StringBuffer karnofskyScores = new StringBuffer();	
 				StringBuffer lanskyScores = new StringBuffer();
 				StringBuffer neuroExams = new StringBuffer();
 				StringBuffer mriCtScores = new StringBuffer();
 				StringBuffer steroidDoseStatuses = new StringBuffer();
 				StringBuffer antiConvulsantStatuses = new StringBuffer();
 				for (int j=0; j<clinicalEvalDataResult.length;j++) {
 					NeuroEvaluation clinicalEvalData = (NeuroEvaluation)clinicalEvalDataResult[j];		
 					
 					Long ptDid = clinicalEvalData.getPatientDid();
 					if(patientDid.toString().equals(ptDid.toString())) {
 						
 						/** this commented out code we will need if we decide to create a view to contain 
 						 *  all the clinical data, then we would not have to combine the result sets
 						 *  any more						 
 						 */
 						//ptData.setTimePoint(clinicalEvalData.getTimePoint());
 						//ptData.setFollowupDate(clinicalEvalData.getFollowupDate());
 						//ptData.setFollowupMonth(clinicalEvalData.getFollowupMonth());
 						//ptData.setNeuroEvaluationDate(clinicalEvalData.getNeuroEvaluationDate());
 						//ptData.setKarnofskyScore(clinicalEvalData.getKarnofskyScore());
 						//ptData.setLanskyScore(clinicalEvalData.getLanskyScore());
 						//ptData.setNeuroExam(clinicalEvalData.getNeuroExam());
 						//ptData.setMriCtScore(clinicalEvalData.getMriCtScore());
 						//ptData.setSteroidDoseStatus(clinicalEvalData.getSteroidDoseStatus());
 						//ptData.setAntiConvulsantStatus(clinicalEvalData.getAntiConvulsantStatus());	
 						if(clinicalEvalData.getTimePoint()!= null) {
 						   timePoints.append(clinicalEvalData.getTimePoint());
 						   timePoints.append(", ");
 						}
 						if(clinicalEvalData.getFollowupDate() != null) {
 						   followUpdates.append(clinicalEvalData.getFollowupDate());	
 						   followUpdates.append(", ");
 						}
 						
 						if(clinicalEvalData.getFollowupMonth() != null) {
 						   followupMonths.append(clinicalEvalData.getFollowupMonth());
 						   followupMonths.append(", ");
 						}
 						
 						if(clinicalEvalData.getNeuroEvaluationDate() != null) {
 						    neuroEvaluationDates.append(clinicalEvalData.getNeuroEvaluationDate());
 						    neuroEvaluationDates.append(", ");
 						}
 						
 						if(clinicalEvalData.getKarnofskyScore() != null) {
 						    karnofskyScores.append(clinicalEvalData.getKarnofskyScore());
 						    karnofskyScores.append(", ");
 						}
 						
 						if(clinicalEvalData.getLanskyScore() != null) {
 						   lanskyScores.append(clinicalEvalData.getLanskyScore());
 						   lanskyScores.append(", ");
 						}
 						
 						if(clinicalEvalData.getNeuroExam() != null) {
 						    neuroExams.append(clinicalEvalData.getNeuroExam());
 						    neuroExams.append(", ");
 						}
 						
 						if(clinicalEvalData.getMriCtScore() != null) {
 						   mriCtScores.append(clinicalEvalData.getMriCtScore());
 						   mriCtScores.append(", ");
 						}
 						
 						if(clinicalEvalData.getSteroidDoseStatus() != null) {
 						   steroidDoseStatuses.append(clinicalEvalData.getSteroidDoseStatus());
 						   steroidDoseStatuses.append(", ");
 						}
 						
 						if(clinicalEvalData.getAntiConvulsantStatus() != null) {
 						   antiConvulsantStatuses.append(clinicalEvalData.getAntiConvulsantStatus());
 						   antiConvulsantStatuses.append(", ");		
 						}
 						
 					}					 
 					
 				}
 				
 				if(timePoints.length() >0) {
 				   timePoints.deleteCharAt(timePoints.length()-2);
 				   ptData.setTimePoints(timePoints.toString());
 				}
 				
 				if(followUpdates.length()>0) {
 				   followUpdates.deleteCharAt(followUpdates.length()-2);
 				   ptData.setFollowupDates(followUpdates.toString());
 				}
 				
 				if(followupMonths.length()>0) {
 				   followupMonths.deleteCharAt(followupMonths.length()-2);
 				   ptData.setFollowupMonths(followupMonths.toString());
 				}
 				
 				if(neuroEvaluationDates.length()>0) {				
 				   neuroEvaluationDates.deleteCharAt(neuroEvaluationDates.length()-2);
 				   ptData.setNeuroEvaluationDates(neuroEvaluationDates.toString());
 				}
 				
 				if(karnofskyScores.length()>0) {	
 				   karnofskyScores.deleteCharAt(karnofskyScores.length()-2);
 				   ptData.setKarnofskyScores(karnofskyScores.toString());
 				}
 				
 				if(lanskyScores.length()>0) {					
 				  lanskyScores.deleteCharAt(lanskyScores.length()-2);
 				  ptData.setLanskyScores(lanskyScores.toString());
 				}
 				
 				if(neuroExams.length()>0) {					
 				  neuroExams.deleteCharAt(neuroExams.length()-2);
 				  ptData.setNeuroExams(neuroExams.toString());
 				}
 				
 				if(mriCtScores.length()>0) {	
 				  mriCtScores.deleteCharAt(mriCtScores.length()-2);
 				  ptData.setMriCtScores(mriCtScores.toString());				 
 				}
 				
 				if(steroidDoseStatuses.length()>0) {	
 				  steroidDoseStatuses.deleteCharAt(steroidDoseStatuses.length()-2);
 				  ptData.setSteroidDoseStatuses(steroidDoseStatuses.toString());
 				}
 				
 				if(antiConvulsantStatuses.length()>0) {	
 				  antiConvulsantStatuses.deleteCharAt(antiConvulsantStatuses.length()-2);
 				  ptData.setAntiConvulsantStatuses(antiConvulsantStatuses.toString());
 				}
 				
 			}
 			
 			
 		 }
 		
 		return patientDataResults;
 	
 	}
 	
 	
 private PatientData[] addPriorRadiationToPatientData(PatientData[] patientDataResults, ResultSet[] priorRadiationDataResult) {
 		
 		if(patientDataResults instanceof PatientData[]) {
 			
 			
 			for(int i=0; i< patientDataResults.length;i++) {
 				PatientData ptData = (PatientData)patientDataResults[i];
 				
 				Long patientDid = ptData.getPatientDid();
 				StringBuffer timePoints = new StringBuffer();
 				StringBuffer radiationSites = new StringBuffer();
 				StringBuffer doseStartDates = new StringBuffer();							
 				StringBuffer doseStopDates = new StringBuffer();
 				StringBuffer fractionDoses = new StringBuffer();	
 				StringBuffer fractionNumbers = new StringBuffer();
 				StringBuffer radiationTypes = new StringBuffer();
 				
 				for (int j=0; j<priorRadiationDataResult.length;j++) {
 					PriorRadiationtherapy priorRadiationData = (PriorRadiationtherapy)priorRadiationDataResult[j];		
 					
 					Long ptDid = priorRadiationData.getPatientDid();
 					if(patientDid.toString().equals(ptDid.toString())) {
 						if(priorRadiationData.getTimePoint()!= null) {
 						   timePoints.append(priorRadiationData.getTimePoint());
 						   timePoints.append(", ");
 						}
 						if(priorRadiationData.getRadiationSite() != null) {
 							radiationSites.append(priorRadiationData.getRadiationSite());	
 							radiationSites.append(", ");
 						}
 						
 						if(priorRadiationData.getDoseStartDate() != null) {
 							doseStartDates.append(priorRadiationData.getDoseStartDate());
 							doseStartDates.append(", ");
 						}
 						
 						if(priorRadiationData.getDoseStopDate() != null) {
 							doseStopDates.append(priorRadiationData.getDoseStopDate());
 						    doseStopDates.append(", ");
 						}
 						
 						if(priorRadiationData.getFractionDose() != null) {
 							fractionDoses.append(priorRadiationData.getFractionDose());
 							fractionDoses.append(", ");
 						}
 						
 						if(priorRadiationData.getFractionNumber() != null) {
 							fractionNumbers.append(priorRadiationData.getFractionNumber());
 							fractionNumbers.append(", ");
 						}
 						
 						if(priorRadiationData.getRadiationType() != null) {
 							radiationTypes.append(priorRadiationData.getRadiationType());
 							radiationTypes.append(", ");
 						}
 							
 						
 					}					 
 					
 				}
 				
 				if(timePoints.length() >0) {
 				   timePoints.deleteCharAt(timePoints.length()-2);
 				   ptData.setPriorRadiationTimePoints(timePoints.toString());
 				}
 				
 				if(radiationSites.length()>0) {
 				   radiationSites.deleteCharAt(radiationSites.length()-2);
 				   ptData.setPriorRadiationRadiationSites(radiationSites.toString());
 				}
 				
 				if(doseStartDates.length()>0) {
 					doseStartDates.deleteCharAt(doseStartDates.length()-2);
 				    ptData.setPriorRadiationDoseStartDates(doseStartDates.toString());
 				}
 				
 				if(doseStopDates.length()>0) {				
 					doseStopDates.deleteCharAt(doseStopDates.length()-2);
 				   ptData.setPriorRadiationDoseStopDates(doseStopDates.toString());
 				}
 				
 				if(fractionDoses.length()>0) {	
 					fractionDoses.deleteCharAt(fractionDoses.length()-2);
 				   ptData.setPriorRadiationFractionDoses(fractionDoses.toString());
 				}
 				
 				if(fractionNumbers.length()>0) {					
 					fractionNumbers.deleteCharAt(fractionNumbers.length()-2);
 				  ptData.setPriorRadiationFractionNumbers(fractionNumbers.toString());
 				}
 				
 				if(radiationTypes.length()>0) {					
 					radiationTypes.deleteCharAt(radiationTypes.length()-2);
 				    ptData.setPriorRadiationRadiationTypes(radiationTypes.toString());
 				}			
 				
 			}
 			
 			
 		 }
 		
 		return patientDataResults;
 	
 	}
 
 
 
 private PatientData[] addPriorChemoToPatientData(PatientData[] patientDataResults, ResultSet[] priorChemoDataResult) {
 		
 		if(patientDataResults instanceof PatientData[]) {
 			
 			
 			for(int i=0; i< patientDataResults.length;i++) {
 				PatientData ptData = (PatientData)patientDataResults[i];
 				
 				Long patientDid = ptData.getPatientDid();
 				StringBuffer timePoints = new StringBuffer();
 				StringBuffer agentIds = new StringBuffer();
 				StringBuffer agentNames = new StringBuffer();							
 				StringBuffer courseCounts = new StringBuffer();
 				StringBuffer doseStartDates = new StringBuffer();	
 				StringBuffer doseStopDates = new StringBuffer();
 				StringBuffer studySources = new StringBuffer();
 				StringBuffer protocolNumbers = new StringBuffer();
 				
 				for (int j=0; j<priorChemoDataResult.length;j++) {
 					PriorChemotherapy priorChemoData = (PriorChemotherapy)priorChemoDataResult[j];		
 					
 					Long ptDid = priorChemoData.getPatientDid();
 					if(patientDid.toString().equals(ptDid.toString())) {
 						if(priorChemoData.getTimePoint()!= null) {
 						   timePoints.append(priorChemoData.getTimePoint());
 						   timePoints.append(", ");
 						}
 						if(priorChemoData.getAgentId() != null) {
 							agentIds.append(priorChemoData.getAgentId());	
 							agentIds.append(", ");
 						}
 						
 						if(priorChemoData.getAgentName() != null) {
 							agentNames.append(priorChemoData.getAgentName());
 							agentNames.append(", ");
 						}
 						
 						if(priorChemoData.getCourseCount() != null) {
 							courseCounts.append(priorChemoData.getCourseCount());
 							courseCounts.append(", ");
 						}
 						
 						if(priorChemoData.getDoseStartDate() != null) {
 							doseStartDates.append(priorChemoData.getDoseStartDate());
 							doseStartDates.append(", ");
 						}
 						
 						if(priorChemoData.getDoseStopDate() != null) {
 							doseStopDates.append(priorChemoData.getDoseStopDate());
 							doseStopDates.append(", ");
 						}
 						
 						if(priorChemoData.getStudySource() != null) {
 							studySources.append(priorChemoData.getStudySource());
 							studySources.append(", ");
 						}
 						
 						if(priorChemoData.getProtocolNumber() != null) {
 							protocolNumbers.append(priorChemoData.getProtocolNumber());
 							protocolNumbers.append(", ");
 						}
 							
 						
 					}					 
 					
 				}
 				
 				if(timePoints.length() >0) {
 				   timePoints.deleteCharAt(timePoints.length()-2);
 				   ptData.setPriorChemoTimePoints(timePoints.toString());
 				}
 				
 				if(agentIds.length()>0) {
 					agentIds.deleteCharAt(agentIds.length()-2);
 				    ptData.setPriorChemoagentIds(agentIds.toString());
 				}
 				
 				if(agentNames.length()>0) {
 					agentNames.deleteCharAt(agentNames.length()-2);
 				    ptData.setPriorChemoAgentNames(agentNames.toString());
 				}
 				
 				if(courseCounts.length()>0) {	
 					courseCounts.deleteCharAt(courseCounts.length()-2);
 				   ptData.setPriorChemoCourseCounts(courseCounts.toString());
 				}
 				
 				if(doseStartDates.length()>0) {
 					doseStartDates.deleteCharAt(doseStartDates.length()-2);
 				    ptData.setPriorChemoDoseStartDates(doseStartDates.toString());
 				}
 				
 				
 				if(doseStopDates.length()>0) {				
 					doseStopDates.deleteCharAt(doseStopDates.length()-2);
 				   ptData.setPriorChemoDoseStopDates(doseStopDates.toString());
 				}
 				
 				
 				
 				if(studySources.length()>0) {					
 					studySources.deleteCharAt(studySources.length()-2);
 				    ptData.setPriorChemoStudySources(studySources.toString());
 				}
 				
 				if(protocolNumbers.length()>0) {					
 					protocolNumbers.deleteCharAt(protocolNumbers.length()-2);
 				    ptData.setPriorChemoProtocolNumbers(protocolNumbers.toString());
 				}			
 				
 			}
 			
 			
 		 }
 		
 		return patientDataResults;
 	
 	}
 
 
 
 
 private PatientData[] addPriorSurgeryToPatientData(PatientData[] patientDataResults, ResultSet[] priorSurgeryDataResult) {
 		
 		if(patientDataResults instanceof PatientData[]) {
 			
 			
 			for(int i=0; i< patientDataResults.length;i++) {
 				PatientData ptData = (PatientData)patientDataResults[i];
 				
 				Long patientDid = ptData.getPatientDid();
 				StringBuffer timePoints = new StringBuffer();
 				StringBuffer procedureTitles = new StringBuffer();
 				StringBuffer tumorHistologys = new StringBuffer();							
 				StringBuffer surgeryDates = new StringBuffer();
 				StringBuffer surgeryOutcomes = new StringBuffer();	
 				
 				for (int j=0; j<priorSurgeryDataResult.length;j++) {
 					PriorSurgery priorSurgeryData = (PriorSurgery)priorSurgeryDataResult[j];		
 					
 					Long ptDid = priorSurgeryData.getPatientDid();
 					if(patientDid.toString().equals(ptDid.toString())) {
 						if(priorSurgeryData.getTimePoint()!= null) {
 						   timePoints.append(priorSurgeryData.getTimePoint());
 						   timePoints.append(", ");
 						}
 						if(priorSurgeryData.getProcedureTitle() != null) {
 							procedureTitles.append(priorSurgeryData.getProcedureTitle());	
 							procedureTitles.append(", ");
 						}
 						
 						if(priorSurgeryData.getTumorHistology() != null) {
 							tumorHistologys.append(priorSurgeryData.getTumorHistology());
 							tumorHistologys.append(", ");
 						}
 						
 						if(priorSurgeryData.getSurgeryDate() != null) {
 							surgeryDates.append(priorSurgeryData.getSurgeryDate());
 							surgeryDates.append(", ");
 						}
 						
 						if(priorSurgeryData.getSurgeryOutcome() != null) {
 							surgeryOutcomes.append(priorSurgeryData.getSurgeryOutcome());
 							surgeryOutcomes.append(", ");
 						}
 						
 					}					 
 					
 				}
 				
 				if(timePoints.length() >0) {
 				   timePoints.deleteCharAt(timePoints.length()-2);
 				   ptData.setPriorSurgeryTimePoints(timePoints.toString());
 				}
 				
 				if(procedureTitles.length()>0) {
 					procedureTitles.deleteCharAt(procedureTitles.length()-2);
 				    ptData.setPriorSurgeryProcedureTitles(procedureTitles.toString());
 				}
 				
 				if(tumorHistologys.length()>0) {
 					tumorHistologys.deleteCharAt(tumorHistologys.length()-2);
 				    ptData.setPriorSurgeryTumorHistologys(tumorHistologys.toString());
 				}
 				
 				if(surgeryDates.length()>0) {	
 					surgeryDates.deleteCharAt(surgeryDates.length()-2);
 				    ptData.setPriorSurgerySurgeryDates(surgeryDates.toString());
 				}
 				
 				if(surgeryOutcomes.length()>0) {
 					surgeryOutcomes.deleteCharAt(surgeryOutcomes.length()-2);
 				    ptData.setPriorSurgerySurgeryOutcomes(surgeryOutcomes.toString());
 				}
 								
 				
 			}
 			
 			
 		 }
 		
 		return patientDataResults;
 	
 	}
 	private PatientData[] executeQuery(Criteria allCriteria) throws Exception {
         final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
         ReportQueryByCriteria sampleQuery = QueryFactory.newReportQuery(PatientData.class, allCriteria, true);
        
         sampleQuery.setAttributes(new String[] {
                        PatientData.BIOSPECIMEN_ID, PatientData.GENDER,
                        PatientData.DISEASE_TYPE, PatientData.AGE_GROUP,
                        PatientData.SAMPLE_ID, PatientData.SURVIVAL_LENGTH_RANGE,
                        PatientData.RACE,PatientData.PATIENT_DID,
                        PatientData.SURVIVAL_LENGTH,PatientData.CENSORING_STATUS,
                        PatientData.AGE} );        
    
 
         Iterator patientDataObjects =  pb.getReportQueryIteratorByQuery(sampleQuery);
         
         ArrayList results = new ArrayList();
         populateResults(patientDataObjects, results);
         
       
         PatientData[] finalResult = new PatientData[results.size()];
         for (int i = 0; i < results.size(); i++) {
             PatientData patientData = (PatientData) results.get(i);
             finalResult[i]  = patientData ;
         }
         pb.close();
         return finalResult;
     }
 
 	  private NeuroEvaluation[] populateClinicalEval(ClinicalDataQuery clinicalQuery, Collection patientDIDs)throws Exception {
     	 
 		 if(patientDIDs.size()>=1) {
 		  Criteria  clinicalEvalCrit = buildClinicalEvalCriteria(clinicalQuery);   
     	  if(clinicalEvalCrit== null) {
     		  clinicalEvalCrit = new Criteria();
     	  }
     	   		  
     		  clinicalEvalCrit.addIn(NeuroEvaluation.PATIENT_DID, patientDIDs);
     		  final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
     	      ReportQueryByCriteria clinicalEvalQuery = QueryFactory.newReportQuery(NeuroEvaluation.class, clinicalEvalCrit, false);
     	       
     	      clinicalEvalQuery.setAttributes(new String[] {
     	    		  NeuroEvaluation.TIME_POINT, NeuroEvaluation.FOLLOWUP_DATE,
     	    		  NeuroEvaluation.FOLLOWUP_MONTH, NeuroEvaluation.NEURO_EVALUATION_DATE,
     	    		  NeuroEvaluation.KARNOFSKY_SCORE, NeuroEvaluation.LANSKY_SCORE,
     	    		  NeuroEvaluation.NEURO_EXAM,NeuroEvaluation.MRI_CT_SCORE,
     	    		  NeuroEvaluation.STEROID_DOSE_STATUS,NeuroEvaluation.ANTI_CONVULSANT_STATUS,
     	    		  NeuroEvaluation.PATIENT_DID} ); 
     	      
     	   
     	        Iterator clinicalEvalDataObjects =  pb.getReportQueryIteratorByQuery(clinicalEvalQuery);
     	        
     	        ArrayList results = new ArrayList();
     	        populateClinicalEvalResults(clinicalEvalDataObjects, results);
     	        
     	      
     	       NeuroEvaluation[] finalResult = new NeuroEvaluation[results.size()];
     	        for (int i = 0; i < results.size(); i++) {
     	        	NeuroEvaluation clinicalEvalData = (NeuroEvaluation) results.get(i);
     	            finalResult[i]  = clinicalEvalData ;
     	        }
     	        pb.close();
     	        
     	       return finalResult;  
               }
              else {
                return null;
              }
   
      }
 	  
 	  
 	  private PriorRadiationtherapy[] populatePriorRadiation(ClinicalDataQuery clinicalQuery, Collection patientDIDs)throws Exception {
 	    	 
 			 if(patientDIDs.size()>=1) {
 			  Criteria  priorRadiationCrit = buildPriorRadiationCriteria(clinicalQuery);
 	    	  if(priorRadiationCrit== null) {
 	    		  priorRadiationCrit = new Criteria();
 	    	  }
 	    	   		  
 	    	  priorRadiationCrit.addIn(PriorRadiationtherapy.PATIENT_DID, patientDIDs);
 	    		  final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
 	    	      ReportQueryByCriteria priorRadiationQuery = QueryFactory.newReportQuery(PriorRadiationtherapy.class, priorRadiationCrit, false);
 	    	       
 	    	      priorRadiationQuery.setAttributes(new String[] {
 	    	    		  PriorRadiationtherapy.TIME_POINT, PriorRadiationtherapy.RADIATION_SITE,
 	    	    		  PriorRadiationtherapy.DOSE_START_DATE, PriorRadiationtherapy.DOSE_STOP_DATE,
 	    	    		  PriorRadiationtherapy.FRACTION_DOSE,   PriorRadiationtherapy.FRACTION_NUMBER,
 	    	    		  PriorRadiationtherapy.RADIATION_TYPE, PriorRadiationtherapy.PATIENT_DID} ); 
 	    	      
 	    	   
 	    	        Iterator priorRadiationDataObjects =  pb.getReportQueryIteratorByQuery(priorRadiationQuery);
 	    	        
 	    	        ArrayList results = new ArrayList();
 	    	        populatePriorRadiationResults(priorRadiationDataObjects, results);
 	    	        
 	    	      
 	    	        PriorRadiationtherapy[] finalResult = new PriorRadiationtherapy[results.size()];
 	    	        for (int i = 0; i < results.size(); i++) {
 	    	        	PriorRadiationtherapy priorRadiationData = (PriorRadiationtherapy) results.get(i);
 	    	            finalResult[i]  = priorRadiationData ;
 	    	        }
 	    	        pb.close();
 	    	        
 	    	       return finalResult;  
 	              }
 	             else {
 	               return null;
 	             }
 	  
 	     }
 	
 	  private PriorChemotherapy[] populatePriorChemo(ClinicalDataQuery clinicalQuery, Collection patientDIDs)throws Exception {
 	    	 
 			 if(patientDIDs.size()>=1) {
 			  Criteria  priorChemoCrit = buildPriorChemoCriteria(clinicalQuery);
 	    	  if(priorChemoCrit== null) {
 	    		  priorChemoCrit = new Criteria();
 	    	  }
 	    	   		  
 	    	  priorChemoCrit.addIn(PriorChemotherapy.PATIENT_DID, patientDIDs);
 	    		  final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
 	    	      ReportQueryByCriteria priorChemoQuery = QueryFactory.newReportQuery(PriorChemotherapy.class, priorChemoCrit, false);
 	    	       
 	    	      priorChemoQuery.setAttributes(new String[] {
 	    	    		  PriorChemotherapy.TIME_POINT, PriorChemotherapy.AGENT_ID,
 	    	    		  PriorChemotherapy.AGENT_NAME, PriorChemotherapy.COURSE_COUNT,
 	    	    		  PriorChemotherapy.DOSE_START_DATE, PriorChemotherapy.DOSE_STOP_DATE,
 	    	    		  PriorChemotherapy.STUDY_SOURCE,PriorChemotherapy.PROTOCOL_NUMBER,
 	    	    		  PriorChemotherapy.PATIENT_DID} ); 
 	    	      
 	    	   
 	    	        Iterator priorRadiationDataObjects =  pb.getReportQueryIteratorByQuery(priorChemoQuery);
 	    	        
 	    	        ArrayList results = new ArrayList();
 	    	        populatePriorChemoResults(priorRadiationDataObjects, results);
 	    	        
 	    	      
 	    	        PriorChemotherapy[] finalResult = new PriorChemotherapy[results.size()];
 	    	        for (int i = 0; i < results.size(); i++) {
 	    	        	PriorChemotherapy priorChemoData = (PriorChemotherapy) results.get(i);
 	    	            finalResult[i]  = priorChemoData ;
 	    	        }
 	    	        pb.close();
 	    	        
 	    	       return finalResult;  
 	              }
 	             else {
 	               return null;
 	             }
 	  
 	     }
 	  
 	  
 	  
 
 	  private PriorSurgery[] populatePriorSurgery(ClinicalDataQuery clinicalQuery, Collection patientDIDs)throws Exception {
 	    	 
 			 if(patientDIDs.size()>=1) {
 			  Criteria  priorSurgeryCrit = buildPriorSurgeryCriteria(clinicalQuery);
 	    	  if(priorSurgeryCrit== null) {
 	    		  priorSurgeryCrit = new Criteria();
 	    	  }
 	    	   		  
 	    	  priorSurgeryCrit.addIn(PriorSurgery.PATIENT_DID, patientDIDs);
 	    		  final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
 	    	      ReportQueryByCriteria priorSurgeryQuery = QueryFactory.newReportQuery(PriorSurgery.class, priorSurgeryCrit, false);
 	    	       
 	    	      priorSurgeryQuery.setAttributes(new String[] {
 	    	    		  PriorSurgery.TIME_POINT, PriorSurgery.PROCEDURE_TITLE,
 	    	    		  PriorSurgery.TUMOR_HISTOLOGY,PriorSurgery.SURGERY_DATE, 
 	    	    		  PriorSurgery.SURGERY_OUTCOME,PriorSurgery.PATIENT_DID} ); 
 	    	      
 	    	   
 	    	        Iterator priorSurgeryDataObjects =  pb.getReportQueryIteratorByQuery(priorSurgeryQuery);
 	    	        
 	    	        ArrayList results = new ArrayList();
 	    	        populatePriorSurgeryResults(priorSurgeryDataObjects, results);
 	    	        
 	    	      
 	    	        PriorSurgery[] finalResult = new PriorSurgery[results.size()];
 	    	        for (int i = 0; i < results.size(); i++) {
 	    	        	PriorSurgery priorSurgeryData = (PriorSurgery) results.get(i);
 	    	            finalResult[i]  = priorSurgeryData ;
 	    	        }
 	    	        pb.close();
 	    	        
 	    	       return finalResult;  
 	              }
 	             else {
 	               return null;
 	             }
 	  
 	     }
     private void populateResults(Iterator patientDataObjects, ArrayList results) {
         while(patientDataObjects.hasNext()) {
             Object[] objs = (Object[]) patientDataObjects.next();
             Long bspID = new Long(((BigDecimal)objs[0]).longValue());
             String gender = (String)objs[1];
             String diseaseType = (String)objs[2];
             String ageGroup = (String)objs[3];
             String sampleID = (String)objs[4];
             String survLenRange = (String)objs[5];
             String race = (String)objs[6];
             Long ptDID = new Long(((BigDecimal)objs[7]).longValue());
             Long survivalLength = null;
             if(objs[8] != null){
             	survivalLength = new Long(((BigDecimal)objs[8]).longValue());
             }
             Long age = null;
             String censor = (String)objs[9];
            if(objs[10] != null){
            	age = new Long(((BigDecimal)objs[10]).longValue());
            }
             PatientData p = new PatientData();
             p.setBiospecimenId(bspID);
             p.setGender(gender);           
             p.setDiseaseType(diseaseType);
             p.setAgeGroup(ageGroup);
             p.setSampleId(sampleID);
             p.setSurvivalLengthRange(survLenRange);  
 			p.setRace(race);   
 			p.setPatientDid(ptDID);
 			p.setSurvivalLength(survivalLength);
 			p.setCensoringStatus(censor);
			p.setAge(age);
 			patientDIDs.add(ptDID);
 
             results.add(p );
         }
     }
     
     
 
     private void populatePriorRadiationResults(Iterator priorRadiationObjects, ArrayList results) {
 		  while(priorRadiationObjects.hasNext()) {
 	            Object[] objs = (Object[]) priorRadiationObjects.next();          
 	       
 	            String timePoint = null;
 	            String RadiationSite = null;
 	            Date doseStartDate = null;
 	            Date doseStopDate = null;
 	            Long fractionDose = null;	           
 	            Long fractionNumber = null;	          
 	            String radiationType = null;	          
 	            Long patientDid = null;
 	            
 	            
 	            
 	            PriorRadiationtherapy  priorRadiation = new PriorRadiationtherapy();
 	            
 	            if(objs[0] != null) {
 	               timePoint = (String)objs[0];
 	               priorRadiation.setTimePoint(timePoint);
 	            }	
 	            
 	            if(objs[1] != null) {
 	            	RadiationSite = (String)objs[1];
 		            priorRadiation.setRadiationSite(RadiationSite);
 		            }	
 	            
 	            if(objs[2]!=null){
 	            	doseStartDate = new java.sql.Date(((java.util.Date)objs[2]).getTime());	
 	            	priorRadiation.setDoseStartDate(doseStartDate);   
 	            }
 	            	           
 	            if(objs[3] != null) {
 	            	 doseStopDate = new java.sql.Date(((java.util.Date)objs[3]).getTime());	
 	            	 priorRadiation.setDoseStopDate(doseStopDate);
 	            }
 	            	           
 	                  
 	            if(objs[4] != null) {
 	            	fractionDose = new Long(((BigDecimal)objs[4]).longValue());
 	            	priorRadiation.setFractionDose(fractionDose);
 	             }
 	             
 	            if(objs[5] != null) {
 	            	fractionNumber = new Long(((BigDecimal)objs[5]).longValue());
 	            	priorRadiation.setFractionNumber(fractionNumber);  
 	             }
 	               
 	            
 	            if(objs[6] != null) {
 	            	radiationType = (String)objs[6];
 	            	priorRadiation.setRadiationType(radiationType);  
 	            }
 	           
 	          
 	            if(objs[7] != null) {    	
 	            	
 	               patientDid =  new Long(((BigDecimal)objs[7]).longValue());
 	               priorRadiation.setPatientDid(patientDid);
 	            }
 	            
 	            results.add(priorRadiation);
 	        }
 		  
 	  }
     
     private void populatePriorChemoResults(Iterator priorChemoObjects, ArrayList results) {
 		  while(priorChemoObjects.hasNext()) {
 	            Object[] objs = (Object[]) priorChemoObjects.next();      
 	       	
 	       
 	            String timePoint = null;
 	            Long agentId = null;
 	            String agentName = null; 
 	            Long courseCount = null;	           
 	            Date doseStartDate = null;
 	            Date doseStopDate = null;
 	            String studySource = null;
 	            String protocolNumber= null;	                 
 	            Long patientDid = null;
 	            
 	            PriorChemotherapy  priorChemo = new PriorChemotherapy();
 	            
 	            if(objs[0] != null) {
 	               timePoint = (String)objs[0];
 	               priorChemo.setTimePoint(timePoint);
 	            }	
 	            
 	            if(objs[1] != null) {
 	            	agentId = new Long(((BigDecimal)objs[1]).longValue());
 	            	priorChemo.setAgentId(agentId);
 		            }	
 	            if(objs[2] != null) {
 	            	agentName = (String)objs[2];
 	            	priorChemo.setAgentName(agentName);  
 	            }
 	            
 	            if(objs[3] != null) {
 	            	courseCount = new Long(((BigDecimal)objs[3]).longValue());
 	            	priorChemo.setCourseCount(courseCount);
 		            }
 	            if(objs[4]!=null){
 	            	doseStartDate = new java.sql.Date(((java.util.Date)objs[4]).getTime());	
 	            	priorChemo.setDoseStartDate(doseStartDate);   
 	            }
 	            	           
 	            if(objs[5] != null) {
 	            	 doseStopDate = new java.sql.Date(((java.util.Date)objs[5]).getTime());	
 	            	 priorChemo.setDoseStopDate(doseStopDate);
 	            }
 	            	           
 	            if(objs[6] != null) {
 	            	studySource = (String)objs[6];
 	            	priorChemo.setStudySource(studySource);  
 	            }    
 	        
 	            if(objs[7] != null) {
 	            	protocolNumber = (String)objs[7];
 	            	priorChemo.setProtocolNumber(protocolNumber);  
 	            }  
 	            if(objs[8] != null) {    	
 	            	
 	               patientDid =  new Long(((BigDecimal)objs[8]).longValue());
 	               priorChemo.setPatientDid(patientDid);
 	            }
 	            
 	            results.add(priorChemo);
 	        }
 		  
 	  }
   
     
     
     
     private void populatePriorSurgeryResults(Iterator priorSurgeryObjects, ArrayList results) {
 		  while(priorSurgeryObjects.hasNext()) {
 	            Object[] objs = (Object[]) priorSurgeryObjects.next();     	
 	       
 	            String timePoint = null;	           
 	            String procedureTitle = null; 
 	            String tumorHistology = null; 	                
 	            Date surgeryDate = null;          
 	            String surgeryOutcome= null;	                 
 	            Long patientDid = null;  	       
 	          
 	            PriorSurgery  priorSurgery = new PriorSurgery();
 	            
 	            if(objs[0] != null) {
 	               timePoint = (String)objs[0];
 	               priorSurgery.setTimePoint(timePoint);
 	            }	
 	            
 	            if(objs[1] != null) {
 	            	procedureTitle = (String)objs[1];
 	            	priorSurgery.setProcedureTitle(procedureTitle);
 		            }
 	            
 	            if(objs[2] != null) {
 	            	tumorHistology = (String)objs[2];
 	            	priorSurgery.setTumorHistology(tumorHistology);  
 	            }	            
 	          
 	            if(objs[3]!=null){
 	            	surgeryDate = new java.sql.Date(((java.util.Date)objs[3]).getTime());	
 	            	priorSurgery.setSurgeryDate(surgeryDate);   
 	            }
 	            	            	           
 	            if(objs[4] != null) {
 	            	surgeryOutcome = (String)objs[4];
 	            	priorSurgery.setSurgeryOutcome(surgeryOutcome);  
 	            }    
 	        	        
 	            if(objs[5] != null) {    	
 	            	
 	               patientDid =  new Long(((BigDecimal)objs[5]).longValue());
 	               priorSurgery.setPatientDid(patientDid);
 	            }
 	            
 	            results.add(priorSurgery);
 	        }
 		  
 	  }
 
     private void populateClinicalEvalResults(Iterator clinicalEvalDataObjects, ArrayList results) {
 		  while(clinicalEvalDataObjects.hasNext()) {
 	            Object[] objs = (Object[]) clinicalEvalDataObjects.next(); 
 	            String timePoint = null;
 	            Date followupDate = null;
 	            Long followupMonth = null;
 	            Date neuroEvaluationDate = null;
 	            Long karnofskyScore = null;
 	            Long lanskyScore = null;
 	            Long neuroExam = null;
 	            Long mriCtScore = null;
 	            String steroidDoseStatus = null;
 	            String antiConvulsantStatus = null;
 	            Long patientDid = null;
 	            
 	            NeuroEvaluation clinicalEval = new NeuroEvaluation();
 	            
 	            if(objs[0] != null) {
 	               timePoint = (String)objs[0];
 	               clinicalEval.setTimePoint(timePoint);
 	            }	           
 	            if(objs[1]!=null){
 	                followupDate = new java.sql.Date(((java.util.Date)objs[1]).getTime());	
 	                clinicalEval.setFollowupDate(followupDate);   
 	            }
 	            	           
 	            if(objs[2] != null) {
 	            	followupMonth = new Long(((BigDecimal)objs[2]).longValue());
 	            	 clinicalEval.setFollowupMonth(followupMonth);
 	            }
 	            	           
 	            if(objs[3] != null) {
 	               neuroEvaluationDate =  new java.sql.Date(((java.util.Date)objs[3]).getTime());
 	               clinicalEval.setNeuroEvaluationDate(neuroEvaluationDate);
 	            }
 	           	            
 	            if(objs[4] != null) {
 	                karnofskyScore = new Long(((BigDecimal)objs[4]).longValue());
 	                clinicalEval.setKarnofskyScore(karnofskyScore);
 	             }
 	             
 	            if(objs[5] != null) {
 	            	 lanskyScore = new Long(((BigDecimal)objs[5]).longValue());
 	            	 clinicalEval.setLanskyScore(lanskyScore);  
 	             }
 	           
 	            if(objs[6] != null) {
 	               neuroExam = new Long(((BigDecimal)objs[6]).longValue());
 	               clinicalEval.setNeuroExam(neuroExam);    
 	              }
 	            if(objs[7] != null) {
 	                mriCtScore = new Long(((BigDecimal)objs[7]).longValue());
 	                clinicalEval.setMriCtScore(mriCtScore);  
 	            }
 	            
 	            if(objs[8] != null) {
 	                steroidDoseStatus = (String)objs[8];
 	                clinicalEval.setSteroidDoseStatus(steroidDoseStatus);  
 	            }
 	            
 	            if(objs[9] != null) {
 	               antiConvulsantStatus = (String)objs[9];
 	               clinicalEval.setAntiConvulsantStatus(antiConvulsantStatus); 
 	            }     
 	          
 	            if(objs[10] != null) {
 	            	
 	               patientDid =  new Long(((BigDecimal)objs[10]).longValue());
 		           clinicalEval.setPatientDid(patientDid);
 	            }
 	            
 	            results.add(clinicalEval);
 	        }
 		  
 	  }
   
     private void buildSurvivalRangeCrit(ClinicalDataQuery cghQuery, Criteria survivalCrit) {
         SurvivalCriteria crit = cghQuery.getSurvivalCriteria();
         if (crit != null) {
             long lowerLmtInMons = crit.getLowerSurvivalRange().getValueObject().longValue();
             long lowerLmtInDays  = lowerLmtInMons * 30;
             long upperLmtInMons = crit.getUpperSurvivalRange().getValueObject().longValue();
             long upperLmtInDays = upperLmtInMons * 30;
             survivalCrit.addBetween(PatientData.SURVIVAL_LENGTH, new Long(lowerLmtInDays), new Long(upperLmtInDays));
         }
     }
     private void buildAgeRangeCrit(ClinicalDataQuery cghQuery, Criteria ageCrit ) {
         AgeCriteria crit = cghQuery.getAgeCriteria();
         if (crit != null) {
             long lowerLmtInYrs= crit.getLowerAgeLimit().getValueObject().longValue();
             long upperLmtInYrs  = crit.getUpperAgeLimit().getValueObject().longValue();
             ageCrit.addBetween(PatientData.AGE, new Long(lowerLmtInYrs), new Long(upperLmtInYrs));
         }
     }
     private void buildGenderCrit(ClinicalDataQuery cghQuery, Criteria genderCrit) {
         GenderCriteria crit = cghQuery.getGenderCriteria();
         if (crit != null) {
             genderCrit.addEqualTo(PatientData.GENDER, crit.getGenderDE().getValueObject());
         }
     }
     
     private void buildPTDIDs(ClinicalDataQuery cghQuery, Criteria genderCrit) {
          GenderCriteria crit = cghQuery.getGenderCriteria();
         if (crit != null) {
             genderCrit.addEqualTo(PatientData.GENDER, crit.getGenderDE().getValueObject());
         }
     }
     private void buildRaceCrit(ClinicalDataQuery clinicalQuery, Criteria raceCrit){
     	  RaceCriteria crit = clinicalQuery.getRaceCriteria();   
     	  
           if (crit != null) {
         	  ArrayList raceTypes = new ArrayList();
         	  for (Iterator iterator = crit.getRaces().iterator(); iterator.hasNext();) {
         		  raceTypes.add(((RaceDE) iterator.next()).getValueObject());
         	  }
         	 raceCrit.addIn(PatientData.RACE, raceTypes);
           }          
          
        }
     
     
     private Criteria buildKarnofskyClinicalEvalCrit(ClinicalDataQuery clinicalQuery) {    	 
 
     	KarnofskyClinicalEvalCriteria crit = clinicalQuery.getKarnofskyCriteria();    	 
         Criteria c = new Criteria();    	 
     	 if (crit != null) {
   	  	    c.addEqualTo(NeuroEvaluation.KARNOFSKY_SCORE, Integer.parseInt(crit.getKarnofskyClinicalEvalDE().getValueObject()));               	 
     	    return c;   
     	   }
     	 else {
     		 return null;
     	  }    	   
         }
     
     private Criteria  buildLanskyClinicalEvalCrit(ClinicalDataQuery clinicalQuery) {    	 
 
     	LanskyClinicalEvalCriteria crit = clinicalQuery.getLanskyCriteria();
     	Criteria c = new Criteria();     	 
    	    if (crit != null) {
    		   c.addEqualTo(NeuroEvaluation.LANSKY_SCORE, Integer.parseInt(crit.getLanskyClinicalEvalDE().getValueObject()));             	
    	       return c;
    	      }
    	    else {
    	    	return null;
    	     }
        }
     
     private Criteria  buildMRIClinicalEvalCrit(ClinicalDataQuery clinicalQuery) {    	 
 
     	MRIClinicalEvalCriteria crit = clinicalQuery.getMriCriteria();
     	Criteria c = new Criteria();    
    	 
    	   if (crit != null) {
    		 c.addEqualTo(NeuroEvaluation.MRI_CT_SCORE, Integer.parseInt(crit.getMRIClinicalEvalDE().getValueObject()));            	
    	     return c;
    	      }
    	   
    	   else {
    		    return null;
    	       }   	   
        }
     
     private Criteria  buildNeuroExamClinicalEvalCrit(ClinicalDataQuery clinicalQuery) {    	 
 
     	NeuroExamClinicalEvalCriteria crit = clinicalQuery.getNeuroExamCriteria();   
     	Criteria c = new Criteria();    
    	    if (crit != null) {
    		  c.addEqualTo(NeuroEvaluation.NEURO_EXAM, Integer.parseInt(crit.getNeuroExamClinicalEvalDE().getValueObject()));
    		 return c;
          }    	
    	    else {
    	    	return null;
    	     }
    	    
        }    
    
     
     private ReportQueryByCriteria getClinicalEvalSubQuery(ClinicalDataQuery clinicalQuery,PersistenceBroker _BROKER,Class subQueryClass, String fieldToSelect) throws Exception {
     	  Criteria clinalEvalCrit = buildClinicalEvalCriteria(clinicalQuery);
     	  if(clinalEvalCrit != null) {
     		  String ptIDCol = getColumnNameForBean(_BROKER, subQueryClass.getName(), fieldToSelect);
     		  org.apache.ojb.broker.query.ReportQueryByCriteria clinicalEvalQuery =
                   QueryFactory.newReportQuery(subQueryClass, new String[] {ptIDCol}, clinalEvalCrit , true);       
               return clinicalEvalQuery;  
     		  
     	    }
     	  else {
     		  return null;    
     		  
     	      }    	  
     
     	}
     
      private Criteria buildClinicalEvalCriteria(ClinicalDataQuery clinicalQuery){
     	 
     	 Criteria karnofskyCrit = buildKarnofskyClinicalEvalCrit(clinicalQuery);
          Criteria lanskyCrit  = buildLanskyClinicalEvalCrit(clinicalQuery);
          Criteria neuroExamCrit  = buildNeuroExamClinicalEvalCrit(clinicalQuery);
          Criteria mriCrit  = buildMRIClinicalEvalCrit(clinicalQuery);
          
          if(karnofskyCrit ==null && lanskyCrit == null && neuroExamCrit==null && mriCrit==null) {
               return null;
           }
          else {      	
          
 	         Criteria clinalEvalCrit = new Criteria();
 	         
 	         if(karnofskyCrit != null) {
 	            clinalEvalCrit.addOrCriteria(karnofskyCrit);
 	         }
 	         
 	         if(lanskyCrit != null) {
 	            clinalEvalCrit.addOrCriteria(lanskyCrit);
 	         }
 	         
 	         if(neuroExamCrit != null) {
 	            clinalEvalCrit.addOrCriteria(neuroExamCrit);
 	         }
 	         
 	         if(mriCrit != null) {
 	            clinalEvalCrit.addOrCriteria(mriCrit);
 	         }
 	         return clinalEvalCrit;
 	       }
   
      }
      private Criteria buildPriorRadiationCriteria(ClinicalDataQuery clinicalQuery) {
     	 
     	 RadiationTherapyCriteria crit = clinicalQuery.getRadiationTherapyCriteria(); 	 
          Criteria c = new Criteria();    	
      		 
      	if (crit != null) {     		 
          	ArrayList radiationTypes = new ArrayList();
                  for (Iterator iterator = crit.getRadiations().iterator(); iterator.hasNext();)
                 	 radiationTypes.add(((RadiationTherapyDE) iterator.next()).getValueObject());     	    
                  
          		c.addColumnIn(PriorRadiationtherapy.RADIATION_TYPE, radiationTypes);
          		return c;  
      	 
      	   }
      	 else {
      		 return null;
      	  }	
     	 
      }
      
      private ReportQueryByCriteria getPriorRadiationTherapySubQuery(ClinicalDataQuery clinicalQuery,PersistenceBroker _BROKER,Class subQueryClass, String fieldToSelect) throws Exception {
     	 Criteria c = buildPriorRadiationCriteria (clinicalQuery);      	 
      	 if(c != null) {     		   
      	    String ptIDCol = getColumnNameForBean(_BROKER, subQueryClass.getName(), fieldToSelect);
      	    org.apache.ojb.broker.query.ReportQueryByCriteria priorRadiationTherapyQuery =
             QueryFactory.newReportQuery(subQueryClass, new String[]{ptIDCol}, c , true);    
      	    return priorRadiationTherapyQuery;
      	  }     	 
      	 
      	 else {
      		 return  null;
      	 }  
    
    	}
      
      
       private Criteria buildPriorChemoCriteria (ClinicalDataQuery clinicalQuery) {
     	 
     	 ChemoAgentCriteria crit = clinicalQuery.getChemoAgentCriteria(); 	 
          Criteria c = new Criteria();      
          
      	 if (crit != null) {     		 
      		 ArrayList agentTypes = new ArrayList();
              for (Iterator iterator = crit.getAgents().iterator(); iterator.hasNext();)
             	 agentTypes.add(((ChemoAgentDE) iterator.next()).getValueObject());     	    
              
      		c.addColumnIn(PriorChemotherapy.AGENT_NAME, agentTypes);
      		return c;   
      	   }
      	 else {
      		 return null;
      	  }	
     	 
      }
      
      private ReportQueryByCriteria getPriorChemoTherapySubQuery(ClinicalDataQuery clinicalQuery,PersistenceBroker _BROKER,Class subQueryClass, String fieldToSelect) throws Exception {
     	
      	 Criteria c = buildPriorChemoCriteria(clinicalQuery);       	 
      	 if(c != null) {     		   
      	    String ptIDCol = getColumnNameForBean(_BROKER, subQueryClass.getName(), fieldToSelect);
      	    org.apache.ojb.broker.query.ReportQueryByCriteria priorChemoTherapyQuery =
             QueryFactory.newReportQuery(subQueryClass, new String[]{ptIDCol}, c , true);    
      	    return priorChemoTherapyQuery;
      	  }
      	 
      	 
      	 else {
      		 return  null;
      	 }  
    
      }
      
      
  private Criteria buildPriorSurgeryCriteria (ClinicalDataQuery clinicalQuery) {
     	 
 	     SurgeryOutcomeCriteria crit = clinicalQuery.getSurgeryOutcomeCriteria(); 
 	     PriorSurgeryTitleCriteria crit2 = clinicalQuery.getPriorSurgeryTitleCriteria(); 
 	     
          Criteria c = new Criteria();    	 
      	 if (crit != null || crit2 != null ) {
      		 
      		 if(crit != null) {
      		 
      		    ArrayList outcomes = new ArrayList();
                 for (Iterator iterator = crit.getOutcomes().iterator(); iterator.hasNext();)
             	   outcomes.add(((SurgeryOutcomeDE) iterator.next()).getValueObject());     	    
          	    c.addColumnIn(PriorSurgery.SURGERY_OUTCOME, outcomes);
      		 }
      		 
      		 if(crit2 != null) {
      			 
      			 ArrayList titles = new ArrayList();
                  for (Iterator iterator = crit2.getTitles().iterator(); iterator.hasNext();)
                 	 titles.add(((PriorSurgeryTitleDE) iterator.next()).getValueObject());     	    
           	     c.addColumnIn(PriorSurgery.PROCEDURE_TITLE, titles);
       	
      		 }
          	
      		return c;   
      	 
      	   }
      	 else {
      		 return null;
      	  }	
     	 
      }
      private ReportQueryByCriteria getPriorSurgeryTherapySubQuery(ClinicalDataQuery clinicalQuery,PersistenceBroker _BROKER,Class subQueryClass, String fieldToSelect) throws Exception {
     	
      	 Criteria c = buildPriorSurgeryCriteria(clinicalQuery);       	 
      	 if(c != null) {     
      		
      	    String ptIDCol = getColumnNameForBean(_BROKER, subQueryClass.getName(), fieldToSelect);
      	    org.apache.ojb.broker.query.ReportQueryByCriteria priorChemoTherapyQuery =
             QueryFactory.newReportQuery(subQueryClass, new String[]{ptIDCol}, c , true);    
      	    return priorChemoTherapyQuery;
      	  }
      	 
      	 
      	 else {
      		 return  null;
      	 }  
    
      }
      
     }
     
     
     
   
