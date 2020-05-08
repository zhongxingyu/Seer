 package gov.nih.nci.rembrandt.queryservice.queryprocessing.clinical;
 
 import gov.nih.nci.caintegrator.dto.critieria.AgeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.ChemoAgentCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.DiseaseOrGradeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.GenderCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.KarnofskyClinicalEvalCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.LanskyClinicalEvalCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.MRIClinicalEvalCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.NeuroExamClinicalEvalCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.OnStudyChemoAgentCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.OnStudyRadiationTherapyCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.OnStudySurgeryOutcomeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.OnStudySurgeryTitleCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.PriorSurgeryTitleCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.RadiationTherapyCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SurgeryOutcomeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SurvivalCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.RaceCriteria;
 import gov.nih.nci.caintegrator.dto.de.ChemoAgentDE;
 import gov.nih.nci.caintegrator.dto.de.DiseaseNameDE;
 import gov.nih.nci.caintegrator.dto.de.OnStudyChemoAgentDE;
 import gov.nih.nci.caintegrator.dto.de.OnStudyRadiationTherapyDE;
 import gov.nih.nci.caintegrator.dto.de.OnStudySurgeryOutcomeDE;
 import gov.nih.nci.caintegrator.dto.de.OnStudySurgeryTitleDE;
 import gov.nih.nci.caintegrator.dto.de.PriorSurgeryTitleDE;
 import gov.nih.nci.caintegrator.dto.de.RaceDE;
 import gov.nih.nci.caintegrator.dto.de.RadiationTherapyDE;
 import gov.nih.nci.caintegrator.dto.de.SurgeryOutcomeDE;
 import gov.nih.nci.rembrandt.dbbean.NeuroEvaluation;
 import gov.nih.nci.rembrandt.dbbean.OnStudyChemotherapy;
 import gov.nih.nci.rembrandt.dbbean.OnStudyRadiationtherapy;
 import gov.nih.nci.rembrandt.dbbean.OnStudySurgery;
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
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.apache.ojb.broker.PersistenceBroker;
 import org.apache.ojb.broker.PersistenceBrokerFactory;
 import org.apache.ojb.broker.query.Criteria;
 import org.apache.ojb.broker.query.QueryFactory;
 import org.apache.ojb.broker.query.ReportQueryByCriteria;
 
 /**
  * @author BhattarR
  */
 
 
 /**
 * caIntegrator License
 * 
 * Copyright 2001-2005 Science Applications International Corporation ("SAIC"). 
 * The software subject to this notice and license includes both human readable source code form and machine readable, 
 * binary, object code form ("the caIntegrator Software"). The caIntegrator Software was developed in conjunction with 
 * the National Cancer Institute ("NCI") by NCI employees and employees of SAIC. 
 * To the extent government employees are authors, any rights in such works shall be subject to Title 17 of the United States
 * Code, section 105. 
 * This caIntegrator Software License (the "License") is between NCI and You. "You (or "Your") shall mean a person or an 
 * entity, and all other entities that control, are controlled by, or are under common control with the entity. "Control" 
 * for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity,
 *  whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) 
 * beneficial ownership of such entity. 
 * This License is granted provided that You agree to the conditions described below. NCI grants You a non-exclusive, 
 * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and royalty-free right and license in its rights 
 * in the caIntegrator Software to (i) use, install, access, operate, execute, copy, modify, translate, market, publicly 
 * display, publicly perform, and prepare derivative works of the caIntegrator Software; (ii) distribute and have distributed 
 * to and by third parties the caIntegrator Software and any modifications and derivative works thereof; 
 * and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties, including the right to license such 
 * rights to further third parties. For sake of clarity, and not by way of limitation, NCI shall have no right of accounting
 * or right of payment from You or Your sublicensees for the rights granted under this License. This License is granted at no
 * charge to You. 
 * 1. Your redistributions of the source code for the Software must retain the above copyright notice, this list of conditions
 *    and the disclaimer and limitation of liability of Article 6, below. Your redistributions in object code form must reproduce 
 *    the above copyright notice, this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials
 *    provided with the distribution, if any. 
 * 2. Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This 
 *    product includes software developed by SAIC and the National Cancer Institute." If You do not include such end-user 
 *    documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments 
 *    normally appear.
 * 3. You may not use the names "The National Cancer Institute", "NCI" "Science Applications International Corporation" and 
 *    "SAIC" to endorse or promote products derived from this Software. This License does not authorize You to use any 
 *    trademarks, service marks, trade names, logos or product names of either NCI or SAIC, except as required to comply with
 *    the terms of this License. 
 * 4. For sake of clarity, and not by way of limitation, You may incorporate this Software into Your proprietary programs and 
 *    into any third party proprietary programs. However, if You incorporate the Software into third party proprietary 
 *    programs, You agree that You are solely responsible for obtaining any permission from such third parties required to 
 *    incorporate the Software into such third party proprietary programs and for informing Your sublicensees, including 
 *    without limitation Your end-users, of their obligation to secure any required permissions from such third parties 
 *    before incorporating the Software into such third party proprietary software programs. In the event that You fail 
 *    to obtain such permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to 
 *    the extent prohibited by law, resulting from Your failure to obtain such permissions. 
 * 5. For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and 
 *    to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses 
 *    of modifications of the Software, or any derivative works of the Software as a whole, provided Your use, reproduction, 
 *    and distribution of the Work otherwise complies with the conditions stated in this License.
 * 6. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, 
 *    THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. 
 *    IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 *    GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 *    OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
 
 public class ClinicalQueryHandler extends QueryHandler {
 	private static Logger logger = Logger.getLogger(ClinicalQueryHandler.class);
 	private Collection patientDIDs = new ArrayList();
 	private ResultSet[] clinicalEvalDataResult = null;	
 	private ResultSet[] priorRadiationResult = null;	
 	private ResultSet[] priorChemoResult = null;	
 	private ResultSet[] priorSurgeryResult = null;	
 	
 	private ResultSet[] onStudyRadiationResult = null;	
 	private ResultSet[] onStudyChemoResult = null;	
 	private ResultSet[] onStudySurgeryResult = null;	
    
 
 	/**
 	 * This methos takes query as a parameter, then go to the database to perform retrieval function,
 	 * including processing patient data, prior/onstudy therapy data, and clinical evaluation information.
 	 */
 	public ResultSet[] handle(Query query) throws Exception {
         ClinicalDataQuery clinicalQuery = (ClinicalDataQuery) query;
         Criteria allCriteria = new Criteria();
 
         
         // add suvival range, age range, gender and race info to the OJB where clause criteria
         buildSurvivalRangeCrit(clinicalQuery, allCriteria);
         buildAgeRangeCrit(clinicalQuery, allCriteria);
         buildGenderCrit(clinicalQuery, allCriteria);
         buildRaceCrit(clinicalQuery, allCriteria);     
        
 
         PersistenceBroker _BROKER = PersistenceBrokerFactory.defaultPersistenceBroker();     
         
         Class targetFactClass = PatientData.class;
         
         // build common factors disease types, sample ids, and institution access to the OJB where clause criteria 
         // for the patient_data table
         //CommonFactHandler.addDiseaseCriteria(clinicalQuery, targetFactClass, _BROKER, allCriteria);
         CommonFactHandler.addDiseaseSampleCriteria(clinicalQuery, targetFactClass, allCriteria);
         CommonFactHandler.addAccessCriteria(clinicalQuery, targetFactClass, allCriteria);
         
         // build nested query for any clinical data which is not located in the patient_data table
         // and from the patient_data table to those are 1 to many relationship, and the link from
         // patient_data table to "many" table is based on patient dids
         // in order to build nested queries, OJB requires ReportQueryByCriteria type.
        
         ReportQueryByCriteria clinicalEvalQuery = getClinicalEvalSubQuery(clinicalQuery,_BROKER,NeuroEvaluation.class,NeuroEvaluation.PATIENT_DID);
         ReportQueryByCriteria priorRadiationQuery = getPriorRadiationTherapySubQuery(clinicalQuery,_BROKER,PriorRadiationtherapy.class,PriorRadiationtherapy.PATIENT_DID);
         ReportQueryByCriteria priorChemoQuery = getPriorChemoTherapySubQuery(clinicalQuery,_BROKER,PriorChemotherapy.class,PriorChemotherapy.PATIENT_DID);
         ReportQueryByCriteria priorSurgeryQuery = getPriorSurgeryTherapySubQuery(clinicalQuery,_BROKER,PriorSurgery.class,PriorSurgery.PATIENT_DID);
         ReportQueryByCriteria onStudyRadiationQuery = getOnStudyRadiationTherapySubQuery(clinicalQuery,_BROKER,OnStudyRadiationtherapy.class,OnStudyRadiationtherapy.PATIENT_DID);
         ReportQueryByCriteria onStudyChemoQuery = getOnStudyChemoTherapySubQuery(clinicalQuery,_BROKER,OnStudyChemotherapy.class,OnStudyChemotherapy.PATIENT_DID);
         ReportQueryByCriteria onStudySurgeryQuery = getOnStudySurgeryTherapySubQuery(clinicalQuery,_BROKER,OnStudySurgery.class,OnStudySurgery.PATIENT_DID);
         
         
         Criteria priorTherapy = new Criteria();
         Criteria priorRadiationCrit = new Criteria();
         Criteria priorChemoCrit= new Criteria();
         Criteria priorSurgeryCrit = new Criteria();
         
         
         Criteria   onStudyTherapy = new Criteria();
         Criteria   onStudyRadiationCrit = new Criteria();
         Criteria   onStudyChemoCrit = new Criteria();
         Criteria   onStudySurgeryCrit = new Criteria();
         
         
         
         _BROKER.close();
         
         if(clinicalEvalQuery != null) {
            allCriteria.addIn(PatientData.PATIENT_DID, clinicalEvalQuery);        
         }
         
         if(priorRadiationQuery != null) {
            
         	priorRadiationCrit.addIn(PatientData.PATIENT_DID, priorRadiationQuery); 
            priorTherapy.addOrCriteria(priorRadiationCrit);    
         }
         
         if(priorChemoQuery != null) {
           
         	
         	priorChemoCrit.addIn(PatientData.PATIENT_DID, priorChemoQuery); 
         	priorTherapy.addOrCriteria(priorChemoCrit);   
           
          }
         
         if(priorSurgeryQuery != null) {
          
         	priorSurgeryCrit.addIn(PatientData.PATIENT_DID, priorSurgeryQuery);    
         	priorTherapy.addOrCriteria(priorSurgeryCrit);  
          }
         
         
        
         if(onStudyRadiationQuery != null) {
              
          	onStudyRadiationCrit.addIn(PatientData.PATIENT_DID, onStudyRadiationQuery); 
             onStudyTherapy.addOrCriteria(onStudyRadiationCrit);    
          }
          
          if(onStudyChemoQuery != null) {
            
          	
         	 onStudyChemoCrit.addIn(PatientData.PATIENT_DID, onStudyChemoQuery); 
         	 onStudyTherapy.addOrCriteria(onStudyChemoCrit);   
            
           }
          
          if(onStudySurgeryQuery != null) {
            onStudySurgeryCrit.addIn(PatientData.PATIENT_DID, onStudySurgeryQuery);    
         	 onStudyTherapy.addOrCriteria(onStudySurgeryCrit);  
           }
         
          
       // make sure if the user selects therapy entries or not  
        if(!priorTherapy.isEmpty()){
                allCriteria.addAndCriteria(priorTherapy);
        }
         
        if(!onStudyTherapy.isEmpty()){
            allCriteria.addAndCriteria(onStudyTherapy);
          }
        
        // excute the whole clinical query after the whole query has been constructed, then formatted to 
        // a PatientData[] array for later use, PatientData implements ResultSet interface
         PatientData[] results = executeQuery(allCriteria);
         
         // after data has been retrieved from patient_data table, the next step is to retrieve child tables info 
         // such as NEUROLOGICAL_EVALUATION , PRIOR_CHEMOTHERAPY, PRIOR_RADIATIONTHERAPY, PRIOR_SURGERY,
         // PT_RADIATIONTHERAPY, PT_SURGERY, PT_CHEMOTHERAPY tables which are 1 to many relationship with the
         // PATIENT_DATA table based on patientDIDs
         
         if(clinicalEvalQuery != null || !priorTherapy.isEmpty() || !onStudyTherapy.isEmpty()|| results.length >=1) {
         	
             clinicalEvalDataResult = populateClinicalEval(clinicalQuery,patientDIDs); 
             priorRadiationResult = populatePriorRadiation(clinicalQuery,patientDIDs); 
             priorChemoResult = populatePriorChemo(clinicalQuery,patientDIDs); 
             priorSurgeryResult = populatePriorSurgery(clinicalQuery,patientDIDs); 
             
             onStudyRadiationResult = populateOnStudyRadiation(clinicalQuery,patientDIDs); 
             onStudyChemoResult = populateOnStudyChemo(clinicalQuery,patientDIDs); 
             onStudySurgeryResult = populateOnStudySurgery(clinicalQuery,patientDIDs); 
             
             // after the data has been retrieved, the next step is to format them to the 
             // PatientData[] array for later display
             results = addClinicalEvalToPatientData(results,clinicalEvalDataResult);
             results = addPriorRadiationToPatientData(results,priorRadiationResult);
             results = addPriorChemoToPatientData(results,priorChemoResult);
             results = addPriorSurgeryToPatientData(results,priorSurgeryResult);
             
             results = addOnStudyRadiationToPatientData(results,onStudyRadiationResult);
             results = addOnStudyChemoToPatientData(results,onStudyChemoResult);
             results = addOnStudySurgeryToPatientData(results,onStudySurgeryResult);
             
         }
         
         
         return results;
     }
 
 	/**
 	 * private method to add clinicalEvaluation including Karnofsky, Lansky,Neuro Exam, and MRI results 
 	 * to the patientData array for later display
 	 * 
 	 */
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
 				StringBuffer neuroExamDescs = new StringBuffer();
 				StringBuffer mriCtScoreDescs = new StringBuffer();
 				int followMonthCompare = 0;
 				int count = 0;
 				Vector v = new Vector();
 				v.clear();
 				
 				for (int j=0; j<clinicalEvalDataResult.length;j++) {
 					NeuroEvaluation clinicalEvalData = (NeuroEvaluation)clinicalEvalDataResult[j];
 					Long ptDid = clinicalEvalData.getPatientDid();
 					if(patientDid.toString().equals(ptDid.toString())) {
 						count++;
 					
 					Long followUpMonth = clinicalEvalData.getFollowupMonth();
 					if(followUpMonth != null) {
 						v.add(followUpMonth.intValue());
 						
 					  }
 					}
 				}
 				
 				int[] followUpMonths = new int[count];
 				
 				for(int p=0; p<v.size();p++) {
 					followUpMonths[p]=((Integer)v.elementAt(p)).intValue();					
 				}
 				
 				Arrays.sort(followUpMonths);
 				
 				for (int j=0; j<clinicalEvalDataResult.length;j++) {
 					NeuroEvaluation clinicalEvalData = (NeuroEvaluation)clinicalEvalDataResult[j];		
 					
 					Long ptDid = clinicalEvalData.getPatientDid();
 					Long followUpMonth = clinicalEvalData.getFollowupMonth();
 					
 					if(patientDid.toString().equals(ptDid.toString())) {						
 						
 						if(followUpMonth == null) {
 						if(clinicalEvalData.getTimePoint()!= null) {
 						   timePoints.append(clinicalEvalData.getTimePoint());
 						   timePoints.append("| ");
 						}
 						if(clinicalEvalData.getFollowupDate() != null) {
 						   followUpdates.append(clinicalEvalData.getFollowupDate());	
 						   followUpdates.append("| ");
 						}
 						
 						if(clinicalEvalData.getFollowupMonth() != null) {
 						   followupMonths.append(clinicalEvalData.getFollowupMonth());
 						   followupMonths.append("| ");
 						}
 						
 						if(clinicalEvalData.getNeuroEvaluationDate() != null) {
 						    neuroEvaluationDates.append(clinicalEvalData.getNeuroEvaluationDate());
 						    neuroEvaluationDates.append("| ");
 						}
 						
 						if(clinicalEvalData.getKarnofskyScore() != null) {
 						    karnofskyScores.append(clinicalEvalData.getKarnofskyScore());
						    karnofskyScores.append("| ");
 						}
 						
 						if(clinicalEvalData.getLanskyScore() != null) {
 						   lanskyScores.append(clinicalEvalData.getLanskyScore());
 						   lanskyScores.append("| ");
 						}
 						
 						if(clinicalEvalData.getNeuroExam() != null) {
 						    neuroExams.append(clinicalEvalData.getNeuroExam());
 						    neuroExams.append("| ");
 						}
 						
 						if(clinicalEvalData.getMriCtScore() != null) {
 						   mriCtScores.append(clinicalEvalData.getMriCtScore());
 						   mriCtScores.append("| ");
 						}
 						
 						if(clinicalEvalData.getSteroidDoseStatus() != null) {
 						   steroidDoseStatuses.append(clinicalEvalData.getSteroidDoseStatus());
 						   steroidDoseStatuses.append("| ");
 						}
 						
 						if(clinicalEvalData.getAntiConvulsantStatus() != null) {
 						   antiConvulsantStatuses.append(clinicalEvalData.getAntiConvulsantStatus());
 						   antiConvulsantStatuses.append("| ");		
 						}
 						
 						if(clinicalEvalData.getNeuroExamDesc()!= null) {
 							neuroExamDescs.append(clinicalEvalData.getNeuroExamDesc());
 							neuroExamDescs.append("| ");
 						}
 							
 						if(clinicalEvalData.getMriScoreDesc()!= null) {
 							mriCtScoreDescs.append(clinicalEvalData.getMriScoreDesc());
 							mriCtScoreDescs.append("| ");
 						}						
 						
 					}
 					}	
 				}
 					 for(int q=0;q<followUpMonths.length;q++) {
 							for (int j=0; j<clinicalEvalDataResult.length;j++) {
 								NeuroEvaluation clinicalEvalData = (NeuroEvaluation)clinicalEvalDataResult[j];		
 								
 								Long ptDid = clinicalEvalData.getPatientDid();
 								Long followUpMonth = clinicalEvalData.getFollowupMonth();
 								
 								if(patientDid.toString().equals(ptDid.toString())) {
 						
 						 
 					 
 					 if (followUpMonth != null ) {
 						  boolean status = false;
 						
 						   if(followUpMonth.intValue()==(int)followUpMonths[q]) {
 							if(clinicalEvalData.getTimePoint()!= null) {
 								   timePoints.append(clinicalEvalData.getTimePoint());
 								   timePoints.append("| ");
 								}
 								if(clinicalEvalData.getFollowupDate() != null) {
 								   followUpdates.append(clinicalEvalData.getFollowupDate());	
 								   followUpdates.append("| ");
 								}
 								
 								if(clinicalEvalData.getFollowupMonth() != null) {
 								   followupMonths.append(clinicalEvalData.getFollowupMonth());
 								   followupMonths.append("| ");
 								}
 								
 								if(clinicalEvalData.getNeuroEvaluationDate() != null) {
 								    neuroEvaluationDates.append(clinicalEvalData.getNeuroEvaluationDate());
 								    neuroEvaluationDates.append("| ");
 								}
 								
 								if(clinicalEvalData.getKarnofskyScore() != null) {
 								    karnofskyScores.append(clinicalEvalData.getKarnofskyScore());
 								    karnofskyScores.append("| ");
 								}
 								
 								if(clinicalEvalData.getLanskyScore() != null) {
 								   lanskyScores.append(clinicalEvalData.getLanskyScore());
 								   lanskyScores.append("| ");
 								}
 								
 								if(clinicalEvalData.getNeuroExam() != null) {
 								    neuroExams.append(clinicalEvalData.getNeuroExam());
 								    neuroExams.append("| ");
 								}
 								
 								if(clinicalEvalData.getMriCtScore() != null) {
 								   mriCtScores.append(clinicalEvalData.getMriCtScore());
 								   mriCtScores.append("| ");
 								}
 								
 								if(clinicalEvalData.getSteroidDoseStatus() != null) {
 								   steroidDoseStatuses.append(clinicalEvalData.getSteroidDoseStatus());
 								   steroidDoseStatuses.append("| ");
 								}
 								
 								if(clinicalEvalData.getAntiConvulsantStatus() != null) {
 								   antiConvulsantStatuses.append(clinicalEvalData.getAntiConvulsantStatus());
 								   antiConvulsantStatuses.append("| ");		
 								}
 								
 								if(clinicalEvalData.getNeuroExamDesc()!= null) {
 									neuroExamDescs.append(clinicalEvalData.getNeuroExamDesc());
 									neuroExamDescs.append("| ");
 								}
 									
 								if(clinicalEvalData.getMriScoreDesc()!= null) {
 									mriCtScoreDescs.append(clinicalEvalData.getMriScoreDesc());
 									mriCtScoreDescs.append("| ");
 								}						
 								
 							
 							
 						  }
 							
 							
 							
 						}
 						
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
 				if(neuroExamDescs.length()>0) {	
 					neuroExamDescs.deleteCharAt(neuroExamDescs.length()-2);
 					ptData.setNeuroExamDescs(neuroExamDescs.toString());
 					}
 				if(mriCtScoreDescs.length()>0) {	
 					mriCtScoreDescs.deleteCharAt(mriCtScoreDescs.length()-2);
 					ptData.setMriScoreDescs(mriCtScoreDescs.toString());
 					}		
 				
 			}
 			
 			
 		 }
 	
 		return patientDataResults;
 	
 	}
 	
 	/**
 	 * 
 	 * private method to add prior therapy radiation result
 	 * to the patientData array for later display
 	 * 
 	 */
 	 
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
 						   timePoints.append("| ");
 						}
 						if(priorRadiationData.getRadiationSite() != null) {
 							radiationSites.append(priorRadiationData.getRadiationSite());	
 							radiationSites.append("| ");
 						}
 						
 						if(priorRadiationData.getDoseStartDate() != null) {
 							doseStartDates.append(priorRadiationData.getDoseStartDate());
 							doseStartDates.append("| ");
 						}
 						
 						if(priorRadiationData.getDoseStopDate() != null) {
 							doseStopDates.append(priorRadiationData.getDoseStopDate());
 						    doseStopDates.append("| ");
 						}
 						
 						if(priorRadiationData.getFractionDose() != null) {
 							fractionDoses.append(priorRadiationData.getFractionDose());
 							fractionDoses.append("| ");
 						}
 						
 						if(priorRadiationData.getFractionNumber() != null) {
 							fractionNumbers.append(priorRadiationData.getFractionNumber());
 							fractionNumbers.append("| ");
 						}
 						
 						if(priorRadiationData.getRadiationType() != null) {
 							radiationTypes.append(priorRadiationData.getRadiationType());
 							radiationTypes.append("| ");
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
 
 
 /**
  * 
  * private method to add onstudy therapy radiation result
  * to the patientData array for later display
  */
 private PatientData[] addOnStudyRadiationToPatientData(PatientData[] patientDataResults, ResultSet[] onStudyRadiationDataResult) {
 	
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
 			StringBuffer neurosisStatuses = new StringBuffer();
 			
 			for (int j=0; j<onStudyRadiationDataResult.length;j++) {
 				OnStudyRadiationtherapy onStudyRadiationData = (OnStudyRadiationtherapy)onStudyRadiationDataResult[j];		
 				
 				Long ptDid = onStudyRadiationData.getPatientDid();
 				if(patientDid.toString().equals(ptDid.toString())) {
 					if(onStudyRadiationData.getTimePoint()!= null) {
 					   timePoints.append(onStudyRadiationData.getTimePoint());
 					   timePoints.append("| ");
 					}
 					if(onStudyRadiationData.getRadiationSite() != null) {
 						radiationSites.append(onStudyRadiationData.getRadiationSite());	
 						radiationSites.append("| ");
 					}
 					
 					if(onStudyRadiationData.getDoseStartDate() != null) {
 						doseStartDates.append(onStudyRadiationData.getDoseStartDate());
 						doseStartDates.append("| ");
 					}
 					
 					if(onStudyRadiationData.getDoseStopDate() != null) {
 						doseStopDates.append(onStudyRadiationData.getDoseStopDate());
 					    doseStopDates.append("| ");
 					}
 					
 					if(onStudyRadiationData.getFractionDose() != null) {
 						fractionDoses.append(onStudyRadiationData.getFractionDose());
 						fractionDoses.append("| ");
 					}
 					
 					if(onStudyRadiationData.getFractionNumber() != null) {
 						fractionNumbers.append(onStudyRadiationData.getFractionNumber());
 						fractionNumbers.append("| ");
 					}
 					
 					if(onStudyRadiationData.getRadiationType() != null) {
 						radiationTypes.append(onStudyRadiationData.getRadiationType());
 						radiationTypes.append("| ");
 					}
 					if(onStudyRadiationData.getNeurosisStatus() != null) {
 						neurosisStatuses.append(onStudyRadiationData.getNeurosisStatus());
 						neurosisStatuses.append("| ");
 					}	
 					
 				}					 
 				
 			}
 			
 			if(timePoints.length() >0) {
 			   timePoints.deleteCharAt(timePoints.length()-2);
 			   ptData.setOnStudyRadiationTimePoints(timePoints.toString());
 			}
 			
 			if(radiationSites.length()>0) {
 			   radiationSites.deleteCharAt(radiationSites.length()-2);
 			   ptData.setOnStudyRadiationRadiationSites(radiationSites.toString());
 			}
 			
 			if(doseStartDates.length()>0) {
 				doseStartDates.deleteCharAt(doseStartDates.length()-2);
 			    ptData.setOnStudyRadiationDoseStartDates(doseStartDates.toString());
 			}
 			
 			if(doseStopDates.length()>0) {				
 				doseStopDates.deleteCharAt(doseStopDates.length()-2);
 			   ptData.setOnStudyRadiationDoseStopDates(doseStopDates.toString());
 			}
 			
 			if(fractionDoses.length()>0) {	
 				fractionDoses.deleteCharAt(fractionDoses.length()-2);
 			   ptData.setOnStudyRadiationFractionDoses(fractionDoses.toString());
 			}
 			
 			if(fractionNumbers.length()>0) {					
 				fractionNumbers.deleteCharAt(fractionNumbers.length()-2);
 			  ptData.setOnStudyRadiationFractionNumbers(fractionNumbers.toString());
 			}
 			
 			if(radiationTypes.length()>0) {					
 				radiationTypes.deleteCharAt(radiationTypes.length()-2);
 			    ptData.setOnStudyRadiationRadiationTypes(radiationTypes.toString());
 			}
 			
 			if(neurosisStatuses.length()>0) {					
 				neurosisStatuses.deleteCharAt(neurosisStatuses.length()-2);
 			    ptData.setOnStudyRadiationNeurosisStatuses(neurosisStatuses.toString());
 			}
 			
 			
 		}
 		
 		
 	 }
 	
 	return patientDataResults;
 
 }
 
 
 /**
  * 
  * private method to add prior therapy chemo agent result
  * to the patientData array for later display
  */
 
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
 						   timePoints.append("| ");
 						}
 						if(priorChemoData.getAgentId() != null) {
 							agentIds.append(priorChemoData.getAgentId());	
 							agentIds.append("| ");
 						}
 						
 						if(priorChemoData.getAgentName() != null) {
 							agentNames.append(priorChemoData.getAgentName());
 							agentNames.append("| ");
 						}
 						
 						if(priorChemoData.getCourseCount() != null) {
 							courseCounts.append(priorChemoData.getCourseCount());
 							courseCounts.append("| ");
 						}
 						
 						if(priorChemoData.getDoseStartDate() != null) {
 							doseStartDates.append(priorChemoData.getDoseStartDate());
 							doseStartDates.append("| ");
 						}
 						
 						if(priorChemoData.getDoseStopDate() != null) {
 							doseStopDates.append(priorChemoData.getDoseStopDate());
 							doseStopDates.append("| ");
 						}
 						
 						if(priorChemoData.getStudySource() != null) {
 							studySources.append(priorChemoData.getStudySource());
 							studySources.append("| ");
 						}
 						
 						if(priorChemoData.getProtocolNumber() != null) {
 							protocolNumbers.append(priorChemoData.getProtocolNumber());
 							protocolNumbers.append("| ");
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
 
 /**
  * 
  * private method to add onstudy therapy chemo agent result
  * to the patientData array for later display
  */
 private PatientData[] addOnStudyChemoToPatientData(PatientData[] patientDataResults, ResultSet[] onStudyChemoDataResult) {
 	
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
 			StringBuffer regimenNumbers = new StringBuffer();
 			
 			for (int j=0; j<onStudyChemoDataResult.length;j++) {
 				OnStudyChemotherapy onStudyChemoData = (OnStudyChemotherapy)onStudyChemoDataResult[j];		
 				
 				Long ptDid = onStudyChemoData.getPatientDid();
 				if(patientDid.toString().equals(ptDid.toString())) {
 					if(onStudyChemoData.getTimePoint()!= null) {
 					   timePoints.append(onStudyChemoData.getTimePoint());
 					   timePoints.append("| ");
 					}
 					if(onStudyChemoData.getAgentId() != null) {
 						agentIds.append(onStudyChemoData.getAgentId());	
 						agentIds.append("| ");
 					}
 					
 					if(onStudyChemoData.getAgentName() != null) {
 						agentNames.append(onStudyChemoData.getAgentName());
 						agentNames.append("| ");
 					}
 					
 					if(onStudyChemoData.getCourseCount() != null) {
 						courseCounts.append(onStudyChemoData.getCourseCount());
 						courseCounts.append("| ");
 					}
 					
 					if(onStudyChemoData.getDoseStartDate() != null) {
 						doseStartDates.append(onStudyChemoData.getDoseStartDate());
 						doseStartDates.append("| ");
 					}
 					
 					if(onStudyChemoData.getDoseStopDate() != null) {
 						doseStopDates.append(onStudyChemoData.getDoseStopDate());
 						doseStopDates.append("| ");
 					}
 					
 					if(onStudyChemoData.getStudySource() != null) {
 						studySources.append(onStudyChemoData.getStudySource());
 						studySources.append("| ");
 					}
 					
 					if(onStudyChemoData.getProtocolNumber() != null) {
 						protocolNumbers.append(onStudyChemoData.getProtocolNumber());
 						protocolNumbers.append("| ");
 					}
 						
 					if(onStudyChemoData.getRegimenNumber() != null) {
 						regimenNumbers.append(onStudyChemoData.getRegimenNumber());
 						regimenNumbers.append("| ");
 					}
 					
 				}					 
 				
 			}
 			
 			if(timePoints.length() >0) {
 			   timePoints.deleteCharAt(timePoints.length()-2);
 			   ptData.setOnStudyChemoTimePoints(timePoints.toString());
 			}
 			
 			if(agentIds.length()>0) {
 				agentIds.deleteCharAt(agentIds.length()-2);
 			    ptData.setOnStudyChemoagentIds(agentIds.toString());
 			}
 			
 			if(agentNames.length()>0) {
 				agentNames.deleteCharAt(agentNames.length()-2);
 			    ptData.setOnStudyChemoAgentNames(agentNames.toString());
 			}
 			
 			if(courseCounts.length()>0) {	
 				courseCounts.deleteCharAt(courseCounts.length()-2);
 			   ptData.setOnStudyChemoCourseCounts(courseCounts.toString());
 			}
 			
 			if(doseStartDates.length()>0) {
 				doseStartDates.deleteCharAt(doseStartDates.length()-2);
 			    ptData.setOnStudyChemoDoseStartDates(doseStartDates.toString());
 			}
 			
 			
 			if(doseStopDates.length()>0) {				
 				doseStopDates.deleteCharAt(doseStopDates.length()-2);
 			   ptData.setOnStudyChemoDoseStopDates(doseStopDates.toString());
 			}
 			
 			
 			
 			if(studySources.length()>0) {					
 				studySources.deleteCharAt(studySources.length()-2);
 			    ptData.setOnStudyChemoStudySources(studySources.toString());
 			}
 			
 			if(protocolNumbers.length()>0) {					
 				protocolNumbers.deleteCharAt(protocolNumbers.length()-2);
 			    ptData.setOnStudyChemoProtocolNumbers(protocolNumbers.toString());
 			}	
 			
 			if(regimenNumbers.length()>0) {					
 				regimenNumbers.deleteCharAt(regimenNumbers.length()-2);
 			    ptData.setOnStudyChemoRegimenNumbers(regimenNumbers.toString());
 			}	
 			
 		}
 		
 		
 	 }
 	
 	return patientDataResults;
 
 }
 
 
 
 
 /**
  * 
  * private method to add prior therapy surgery including surgery title and outcome result
  * to the patientData array for later display
  */
 
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
 						   timePoints.append("| ");
 						}
 						if(priorSurgeryData.getProcedureTitle() != null) {
 							procedureTitles.append(priorSurgeryData.getProcedureTitle());	
 							procedureTitles.append("| ");
 						}
 						
 						if(priorSurgeryData.getTumorHistology() != null) {
 							tumorHistologys.append(priorSurgeryData.getTumorHistology());
 							tumorHistologys.append("| ");
 						}
 						
 						if(priorSurgeryData.getSurgeryDate() != null) {
 							surgeryDates.append(priorSurgeryData.getSurgeryDate());
 							surgeryDates.append("| ");
 						}
 						
 						if(priorSurgeryData.getSurgeryOutcome() != null) {
 							surgeryOutcomes.append(priorSurgeryData.getSurgeryOutcome());
 							surgeryOutcomes.append("| ");
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
 
 
 /**
  * 
  * private method to add onstudy therapy surgery including surgery title and outcome result
  * to the patientData array for later display
  */
 
 private PatientData[] addOnStudySurgeryToPatientData(PatientData[] patientDataResults, ResultSet[] onStudySurgeryDataResult) {
 	
 	if(patientDataResults instanceof PatientData[]) {
 		
 		
 		for(int i=0; i< patientDataResults.length;i++) {
 			PatientData ptData = (PatientData)patientDataResults[i];
 			
 			Long patientDid = ptData.getPatientDid();
 			StringBuffer timePoints = new StringBuffer();
 			StringBuffer procedureTitles = new StringBuffer();
 			StringBuffer histoDiagnoses = new StringBuffer();							
 			StringBuffer surgeryDates = new StringBuffer();
 			StringBuffer surgeryOutcomes = new StringBuffer();	
 			StringBuffer indications = new StringBuffer();	
 			
 			for (int j=0; j<onStudySurgeryDataResult.length;j++) {
 				OnStudySurgery onStudySurgeryData = (OnStudySurgery)onStudySurgeryDataResult[j];		
 				
 				Long ptDid = onStudySurgeryData.getPatientDid();
 				if(patientDid.toString().equals(ptDid.toString())) {
 					if(onStudySurgeryData.getTimePoint()!= null) {
 					   timePoints.append(onStudySurgeryData.getTimePoint());
 					   timePoints.append("| ");
 					}
 					if(onStudySurgeryData.getProcedureTitle() != null) {
 						procedureTitles.append(onStudySurgeryData.getProcedureTitle());	
 						procedureTitles.append("| ");
 					}
 					
 					if(onStudySurgeryData.getHistoDiagnosis() != null) {
 						histoDiagnoses.append(onStudySurgeryData.getHistoDiagnosis());
 						histoDiagnoses.append("| ");
 					}
 					
 					if(onStudySurgeryData.getSurgeryDate() != null) {
 						surgeryDates.append(onStudySurgeryData.getSurgeryDate());
 						surgeryDates.append("| ");
 					}
 					
 					if(onStudySurgeryData.getSurgeryOutcome() != null) {
 						surgeryOutcomes.append(onStudySurgeryData.getSurgeryOutcome());
 						surgeryOutcomes.append("| ");
 					}
 					
 					if(onStudySurgeryData.getIndication() != null) {
 						indications.append(onStudySurgeryData.getIndication());
 						indications.append("| ");
 					}
 					
 				}					 
 				
 			}
 			
 			if(timePoints.length() >0) {
 			   timePoints.deleteCharAt(timePoints.length()-2);
 			   ptData.setOnStudySurgeryTimePoints(timePoints.toString());
 			}
 			
 			if(procedureTitles.length()>0) {
 				procedureTitles.deleteCharAt(procedureTitles.length()-2);
 			    ptData.setOnStudySurgeryProcedureTitles(procedureTitles.toString());
 			}
 			
 			if(histoDiagnoses.length()>0) {
 				histoDiagnoses.deleteCharAt(histoDiagnoses.length()-2);
 			    ptData.setOnStudySurgeryHistoDiagnoses(histoDiagnoses.toString());
 			}
 			
 			if(surgeryDates.length()>0) {	
 				surgeryDates.deleteCharAt(surgeryDates.length()-2);
 			    ptData.setOnStudySurgerySurgeryDates(surgeryDates.toString());
 			}
 	
 			if(surgeryOutcomes.length()>0) {
 				surgeryOutcomes.deleteCharAt(surgeryOutcomes.length()-2);
 			    ptData.setOnStudySurgerySurgeryOutcomes(surgeryOutcomes.toString());
 			}
 				
 			if(indications.length()>0) {
 				indications.deleteCharAt(indications.length()-2);
 			    ptData.setOnStudySurgeryIndications(indications.toString());
 			}
 			
 		}
 		
 		
 	 }
 	
 	return patientDataResults;
 
 }
 
 /**
  * 
  * private method to perform clinical query to retrive patient data from Patient_data table
  * then have the result pupolated to the patientData[] array
  * 
  */
 	private PatientData[] executeQuery(Criteria allCriteria) throws Exception {
         final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
         ReportQueryByCriteria sampleQuery = QueryFactory.newReportQuery(PatientData.class, allCriteria, true);
        
         sampleQuery.setAttributes(new String[] {
                        PatientData.BIOSPECIMEN_ID, PatientData.GENDER,
                        PatientData.DISEASE_TYPE, PatientData.AGE_GROUP,
                        PatientData.SAMPLE_ID, PatientData.SURVIVAL_LENGTH_MONTH,
                        PatientData.RACE,PatientData.PATIENT_DID,
                        PatientData.SURVIVAL_LENGTH,PatientData.CENSORING_STATUS,
                        PatientData.AGE,PatientData.WHO_GRADE,
                        PatientData.INSTITUTION_NAME, PatientData.SPECIMEN_NAME
                        } );        
 
    
 
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
   
 	/**
 	 * 
 	 * private method to perform clinicaleval query including Karnofsky, Lansky, Neuro Exam and MRI  to retrive clinicalEval data
 	 * from NEUROLOGICAL_EVALUATION table, and have the result formatted to the NeuroEvaluation[] which impelments 
 	 * ResultSet, then the result will be added to the patientData[] array
 	 * 
 	 */
 	  private NeuroEvaluation[] populateClinicalEval(ClinicalDataQuery clinicalQuery, Collection patientDIDs)throws Exception {
     	 
 		 if(patientDIDs.size()>=1) {
 		  Criteria  clinicalEvalCrit = buildClinicalEvalCriteria(clinicalQuery);   
     	  if(clinicalEvalCrit== null) {
     		  clinicalEvalCrit = new Criteria();
     	  }
     	   		  
     		  clinicalEvalCrit.addIn(NeuroEvaluation.PATIENT_DID, patientDIDs);
     		  final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
     	      ReportQueryByCriteria clinicalEvalQuery = QueryFactory.newReportQuery(NeuroEvaluation.class, clinicalEvalCrit, true);
     	       
     	      clinicalEvalQuery.setAttributes(new String[] {
     	    		  NeuroEvaluation.TIME_POINT, NeuroEvaluation.FOLLOWUP_DATE,
     	    		  NeuroEvaluation.FOLLOWUP_MONTH, NeuroEvaluation.NEURO_EVALUATION_DATE,
     	    		  NeuroEvaluation.KARNOFSKY_SCORE, NeuroEvaluation.LANSKY_SCORE,
     	    		  NeuroEvaluation.NEURO_EXAM,NeuroEvaluation.MRI_CT_SCORE,
     	    		  NeuroEvaluation.STEROID_DOSE_STATUS,NeuroEvaluation.ANTI_CONVULSANT_STATUS,
     	    		  NeuroEvaluation.PATIENT_DID,NeuroEvaluation.NEURO_EXAM_DESC,
     	    		  NeuroEvaluation.MRI_CT_SCORE_DESC} ); 
     	      
     	   
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
 	 
 	  /**
 		 * 
 		 * private method to perform prior therapy radiation type query to retrive prior therapy radtiation data
 		 * from PRIOR_RADIATIONTHERAPY table, and have the result formatted to the PriorRadiationtherapy[] which impelments 
 		 * ResultSet, then the result will be added to the patientData[] array
 		 * 
 		 */
 	  
 	  private PriorRadiationtherapy[] populatePriorRadiation(ClinicalDataQuery clinicalQuery, Collection patientDIDs)throws Exception {
 	    	 
 			 if(patientDIDs.size()>=1) {
 			  Criteria  priorRadiationCrit = buildPriorRadiationCriteria(clinicalQuery);
 	    	  if(priorRadiationCrit== null) {
 	    		  priorRadiationCrit = new Criteria();
 	    	  }
 	    	   		  
 	    	  priorRadiationCrit.addIn(PriorRadiationtherapy.PATIENT_DID, patientDIDs);
 	    		  final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
 	    	      ReportQueryByCriteria priorRadiationQuery = QueryFactory.newReportQuery(PriorRadiationtherapy.class, priorRadiationCrit, true);
 	    	       
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
 	  
 	  /**
 		 * 
 		 * private method to perform onstudy therapy radiation type query to retrive onstudy therapy radtiation data
 		 * from PT_RADIATIONTHERAPY table, and have the result formatted to the OnStudyRadiationtherapy[] which impelments 
 		 * ResultSet, then the result will be added to the patientData[] array
 		 * 
 		 */
 	  
 	  private OnStudyRadiationtherapy[] populateOnStudyRadiation(ClinicalDataQuery clinicalQuery, Collection patientDIDs)throws Exception {
 	    	 
 			 if(patientDIDs.size()>=1) {
 			  Criteria  onStudyRadiationCrit = buildOnStudyRadiationCriteria(clinicalQuery);
 	    	  if(onStudyRadiationCrit== null) {
 	    		  onStudyRadiationCrit = new Criteria();
 	    	  }
 	    	   		  
 	    	      onStudyRadiationCrit.addIn(OnStudyRadiationtherapy.PATIENT_DID, patientDIDs);
 	    		  final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
 	    	      ReportQueryByCriteria onStudyRadiationQuery = QueryFactory.newReportQuery(OnStudyRadiationtherapy.class, onStudyRadiationCrit, true);
 	    	       
 	    	      onStudyRadiationQuery.setAttributes(new String[] {
 	    	    		  OnStudyRadiationtherapy.TIME_POINT, OnStudyRadiationtherapy.RADIATION_SITE,
 	    	    		  OnStudyRadiationtherapy.DOSE_START_DATE, OnStudyRadiationtherapy.DOSE_STOP_DATE,
 	    	    		  OnStudyRadiationtherapy.FRACTION_DOSE,   OnStudyRadiationtherapy.FRACTION_NUMBER,
 	    	    		  OnStudyRadiationtherapy.RADIATION_TYPE, OnStudyRadiationtherapy.PATIENT_DID,
 	    	    		  OnStudyRadiationtherapy.NEUROSIS_STATUS} ); 
 	    	      
 	    	   
 	    	        Iterator onStudyRadiationDataObjects =  pb.getReportQueryIteratorByQuery(onStudyRadiationQuery);
 	    	        
 	    	        ArrayList results = new ArrayList();
 	    	        populateOnStudyRadiationResults(onStudyRadiationDataObjects, results);
 	    	        
 	    	      
 	    	        OnStudyRadiationtherapy[] finalResult = new OnStudyRadiationtherapy[results.size()];
 	    	        for (int i = 0; i < results.size(); i++) {
 	    	        	OnStudyRadiationtherapy onStudyRadiationData = (OnStudyRadiationtherapy) results.get(i);
 	    	            finalResult[i]  = onStudyRadiationData ;
 	    	        }
 	    	        pb.close();
 	    	        
 	    	       return finalResult;  
 	              }
 	             else {
 	               return null;
 	             }
 	  
 	     }
 	
 	
 	  /**
 		 * 
 		 * private method to perform prior therapy chemo agent  type query to retrive prior therapy chmeo agent data
 		 * from PRIOR_CHEMOTHERAPY table, and have the result formatted to the PriorChemotherapy[] which impelments 
 		 * ResultSet, then the result will be added to the patientData[] array
 		 * 
 		 */
 	  private PriorChemotherapy[] populatePriorChemo(ClinicalDataQuery clinicalQuery, Collection patientDIDs)throws Exception {
 	    	 
 			 if(patientDIDs.size()>=1) {
 			  Criteria  priorChemoCrit = buildPriorChemoCriteria(clinicalQuery);
 	    	  if(priorChemoCrit== null) {
 	    		  priorChemoCrit = new Criteria();
 	    	  }
 	    	   		  
 	    	  priorChemoCrit.addIn(PriorChemotherapy.PATIENT_DID, patientDIDs);
 	    		  final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
 	    	      ReportQueryByCriteria priorChemoQuery = QueryFactory.newReportQuery(PriorChemotherapy.class, priorChemoCrit, true);
 	    	       
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
 	  
 	  /**
 		 * 
 		 * private method to perform onstudy therapy chemo agent  type query to retrive onstudy therapy chmeo agent data
 		 * from PT_CHEMOTHERAPY table, and have the result formatted to the OnStudyChemotherapy[] which impelments 
 		 * ResultSet, then the result will be added to the patientData[] array
 		 * 
 		 */
 	  
 	  private OnStudyChemotherapy[] populateOnStudyChemo(ClinicalDataQuery clinicalQuery, Collection patientDIDs)throws Exception {
 	    	 
 			 if(patientDIDs.size()>=1) {
 			  Criteria  onStudyChemoCrit = buildOnStudyChemoCriteria(clinicalQuery);
 	    	  if(onStudyChemoCrit== null) {
 	    		  onStudyChemoCrit = new Criteria();
 	    	  }
 	    	   		  
 	    	  onStudyChemoCrit.addIn(OnStudyChemotherapy.PATIENT_DID, patientDIDs);
 	    		  final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
 	    	      ReportQueryByCriteria onStudyChemoQuery = QueryFactory.newReportQuery(OnStudyChemotherapy.class, onStudyChemoCrit, true);
 	    	       
 	    	      onStudyChemoQuery.setAttributes(new String[] {
 	    	    		  OnStudyChemotherapy.TIME_POINT, OnStudyChemotherapy.AGENT_ID,
 	    	    		  OnStudyChemotherapy.AGENT_NAME, OnStudyChemotherapy.COURSE_COUNT,
 	    	    		  OnStudyChemotherapy.DOSE_START_DATE, OnStudyChemotherapy.DOSE_STOP_DATE,
 	    	    		  OnStudyChemotherapy.STUDY_SOURCE,OnStudyChemotherapy.PROTOCOL_NUMBER,
 	    	    		  OnStudyChemotherapy.PATIENT_DID,OnStudyChemotherapy.REGIMEN_NUMBER} ); 
 	    	      
 	    	   
 	    	        Iterator onStudyRadiationDataObjects =  pb.getReportQueryIteratorByQuery(onStudyChemoQuery);
 	    	        
 	    	        ArrayList results = new ArrayList();
 	    	        populateOnStudyChemoResults(onStudyRadiationDataObjects, results);
 	    	        
 	    	      
 	    	        OnStudyChemotherapy[] finalResult = new OnStudyChemotherapy[results.size()];
 	    	        for (int i = 0; i < results.size(); i++) {
 	    	        	OnStudyChemotherapy onStudyChemoData = (OnStudyChemotherapy) results.get(i);
 	    	            finalResult[i]  = onStudyChemoData ;
 	    	        }
 	    	        pb.close();
 	    	        
 	    	       return finalResult;  
 	              }
 	             else {
 	               return null;
 	             }
 	  
 	     }
 	  
 	
 	  /**
 		 * 
 		 * private method to perform prior therapy surgery title and outcome query to retrive prior therapy chmeo agent data
 		 * from PRIOR_SURGERY table, and have the result formatted to the PriorSurgery[] which impelments 
 		 * ResultSet, then the result will be added to the patientData[] array
 		 * 
 		 */  
 
 	  private PriorSurgery[] populatePriorSurgery(ClinicalDataQuery clinicalQuery, Collection patientDIDs)throws Exception {
 	    	 
 			 if(patientDIDs.size()>=1) {
 			  Criteria  priorSurgeryCrit = buildPriorSurgeryCriteria(clinicalQuery);
 	    	  if(priorSurgeryCrit== null) {
 	    		  priorSurgeryCrit = new Criteria();
 	    	  }
 	    	   		  
 	    	  priorSurgeryCrit.addIn(PriorSurgery.PATIENT_DID, patientDIDs);
 	    		  final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
 	    	      ReportQueryByCriteria priorSurgeryQuery = QueryFactory.newReportQuery(PriorSurgery.class, priorSurgeryCrit, true);
 	    	       
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
 	  
 	  /**
 		 * 
 		 * private method to perform onstudy therapy surgery title and outcome query to retrive onstudy therapy chmeo agent data
 		 * from PT_SURGERY table, and have the result formatted to the OnStudySurgery[] which impelments 
 		 * ResultSet, then the result will be added to the patientData[] array
 		 * 
 		 */  
 
 	  
 	  private OnStudySurgery[] populateOnStudySurgery(ClinicalDataQuery clinicalQuery, Collection patientDIDs)throws Exception {
 	    	 
 			 if(patientDIDs.size()>=1) {
 			  Criteria  onStudySurgeryCrit = buildOnStudySurgeryCriteria(clinicalQuery);
 	    	  if(onStudySurgeryCrit== null) {
 	    		  onStudySurgeryCrit = new Criteria();
 	    	  }
 	    	   		  
 	    	  onStudySurgeryCrit.addIn(OnStudySurgery.PATIENT_DID, patientDIDs);
 	    		  final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
 	    	      ReportQueryByCriteria onStudySurgeryQuery = QueryFactory.newReportQuery(OnStudySurgery.class, onStudySurgeryCrit, true);
 	    	       
 	    	      onStudySurgeryQuery.setAttributes(new String[] {
 	    	    		  OnStudySurgery.TIME_POINT, OnStudySurgery.PROCEDURE_TITLE,
 	    	    		  OnStudySurgery.INDICATION,OnStudySurgery.SURGERY_DATE, 
 	    	    		  OnStudySurgery.SURGERY_OUTCOME,OnStudySurgery.PATIENT_DID,
 	    	    		  OnStudySurgery.HISTO_DIAGNOSIS} ); 
 	    	      
 	    	   
 	    	        Iterator onStudySurgeryDataObjects =  pb.getReportQueryIteratorByQuery(onStudySurgeryQuery);
 	    	        
 	    	        ArrayList results = new ArrayList();
 	    	        populateOnStudySurgeryResults(onStudySurgeryDataObjects, results);
 	    	        
 	    	      
 	    	        OnStudySurgery[] finalResult = new OnStudySurgery[results.size()];
 	    	        for (int i = 0; i < results.size(); i++) {
 	    	        	OnStudySurgery onStudySurgeryData = (OnStudySurgery) results.get(i);
 	    	            finalResult[i]  = onStudySurgeryData ;
 	    	        }
 	    	        pb.close();
 	    	        
 	    	       return finalResult;  
 	              }
 	             else {
 	               return null;
 	             }
 	  
 	     }
 	  
 	  /**
 	   * private method to add individual column data retrieved from patient_data to a arraylist
 	   * 
 	   */
  
     private void populateResults(Iterator patientDataObjects, ArrayList results) {
         while(patientDataObjects.hasNext()) {
             Object[] objs = (Object[]) patientDataObjects.next();
             Long bspID = null;
             if(objs[0]!= null){
             	bspID = new Long(((BigDecimal)objs[0]).longValue());
             }
             String gender = (String)objs[1];
             String diseaseType = (String)objs[2];
             String ageGroup = (String)objs[3];
             String sampleID = (String)objs[4];
             Double survLenMonth = null;
             if(objs[5] != null){
             	survLenMonth = new Double(((BigDecimal)objs[5]).doubleValue());   
             	//String survLenRange = (String)objs[5];
             }
             
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
             String whoGrade = (String)objs[11];
             String institutionName = (String)objs[12];
             String specimenName = (String)objs[13];
             PatientData p = new PatientData();
             p.setBiospecimenId(bspID);
             p.setGender(gender);           
             p.setDiseaseType(diseaseType);
             p.setAgeGroup(ageGroup);
             p.setSampleId(sampleID);
             p.setSurvivalLengthMonth(survLenMonth);
             if(survLenMonth != null){
             	p.setSurvivalLengthRange(survLenMonth.toString());  
             }
 			p.setRace(race);   
 			p.setPatientDid(ptDID);
 			p.setWhoGrade(whoGrade);
 			p.setSurvivalLength(survivalLength);
 			p.setCensoringStatus(censor);
 			p.setAge(age);
 			p.setInstitutionName(institutionName);
 			p.setSpecimenName(specimenName);
 			
 			
 			patientDIDs.add(ptDID);
 
             results.add(p );
         }
     }
     
     
     /**
 	   * private method to add individual column data retrieved from PT_RADIATIONTHERAPY to a arraylist
 	   * 
 	   */
 
     private void populateOnStudyRadiationResults(Iterator onStudyRadiationObjects, ArrayList results) {
 		  while(onStudyRadiationObjects.hasNext()) {
 	            Object[] objs = (Object[]) onStudyRadiationObjects.next();          
 	       
 	            String timePoint = null;
 	            String RadiationSite = null;
 	            Date doseStartDate = null;
 	            Date doseStopDate = null;
 	            Long fractionDose = null;	           
 	            Long fractionNumber = null;	          
 	            String radiationType = null;	          
 	            Long patientDid = null;
 	            String neurosisStatus = null;
 	            
 	            
 	            
 	            OnStudyRadiationtherapy  onStudyRadiation = new OnStudyRadiationtherapy();
 	            
 	            if(objs[0] != null) {
 	               timePoint = (String)objs[0];
 	               onStudyRadiation.setTimePoint(timePoint);
 	            }	
 	            
 	            if(objs[1] != null) {
 	            	RadiationSite = (String)objs[1];
 	            	onStudyRadiation.setRadiationSite(RadiationSite);
 		            }	
 	            
 	            if(objs[2]!=null){
 	            	doseStartDate = new java.sql.Date(((java.util.Date)objs[2]).getTime());	
 	            	onStudyRadiation.setDoseStartDate(doseStartDate);   
 	            }
 	            	           
 	            if(objs[3] != null) {
 	            	 doseStopDate = new java.sql.Date(((java.util.Date)objs[3]).getTime());	
 	            	 onStudyRadiation.setDoseStopDate(doseStopDate);
 	            }
 	            	           
 	                  
 	            if(objs[4] != null) {
 	            	fractionDose = new Long(((BigDecimal)objs[4]).longValue());
 	            	onStudyRadiation.setFractionDose(fractionDose);
 	             }
 	             
 	            if(objs[5] != null) {
 	            	fractionNumber = new Long(((BigDecimal)objs[5]).longValue());
 	            	onStudyRadiation.setFractionNumber(fractionNumber);  
 	             }
 	               
 	            
 	            if(objs[6] != null) {
 	            	radiationType = (String)objs[6];
 	            	onStudyRadiation.setRadiationType(radiationType);  
 	            }
 	           
 	          
 	            if(objs[7] != null) {    	
 	            	
 	               patientDid =  new Long(((BigDecimal)objs[7]).longValue());
 	               onStudyRadiation.setPatientDid(patientDid);
 	            }
 	            
 	            if(objs[8] != null) {    	
 	            	
 	            	neurosisStatus =  (String)objs[8];
 		               onStudyRadiation.setNeurosisStatus(neurosisStatus);
 		            }
 		            
 	            results.add(onStudyRadiation);
 	        }
 		  
 	  }
     
     /**
 	   * private method to add individual column data retrieved from PRIOR_RADIATIONTHERAPY to a arraylist
 	   * 
 	   */
     
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
   
     /**
 	   * private method to add individual column data retrieved from PRIOR_CHEMOTHERAPY to a arraylist
 	   * 
 	   */
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
     
     /**
 	   * private method to add individual column data retrieved from PT_CHEMOTHERAPY to a arraylist
 	   * 
 	   */
   
     private void populateOnStudyChemoResults(Iterator onStudyChemoObjects, ArrayList results) {
 		  while(onStudyChemoObjects.hasNext()) {
 	            Object[] objs = (Object[]) onStudyChemoObjects.next();      
 	       	
 	       
 	            String timePoint = null;
 	            Long agentId = null;
 	            String agentName = null; 
 	            Long courseCount = null;	           
 	            Date doseStartDate = null;
 	            Date doseStopDate = null;
 	            String studySource = null;
 	            String protocolNumber= null;	                 
 	            Long patientDid = null;
 	            Long regimenNumber= null;	
 	            
 	            OnStudyChemotherapy  onStudyChemo = new OnStudyChemotherapy();
 	            
 	            if(objs[0] != null) {
 	               timePoint = (String)objs[0];
 	               onStudyChemo.setTimePoint(timePoint);
 	            }	
 	            
 	            if(objs[1] != null) {
 	            	agentId = new Long(((BigDecimal)objs[1]).longValue());
 	            	onStudyChemo.setAgentId(agentId);
 		            }	
 	            if(objs[2] != null) {
 	            	agentName = (String)objs[2];
 	            	onStudyChemo.setAgentName(agentName);  
 	            }
 	            
 	            if(objs[3] != null) {
 	            	courseCount = new Long(((BigDecimal)objs[3]).longValue());
 	            	onStudyChemo.setCourseCount(courseCount);
 		            }
 	            if(objs[4]!=null){
 	            	doseStartDate = new java.sql.Date(((java.util.Date)objs[4]).getTime());	
 	            	onStudyChemo.setDoseStartDate(doseStartDate);   
 	            }
 	            	           
 	            if(objs[5] != null) {
 	            	 doseStopDate = new java.sql.Date(((java.util.Date)objs[5]).getTime());	
 	            	 onStudyChemo.setDoseStopDate(doseStopDate);
 	            }
 	            	           
 	            if(objs[6] != null) {
 	            	studySource = (String)objs[6];
 	            	onStudyChemo.setStudySource(studySource);  
 	            }    
 	        
 	            if(objs[7] != null) {
 	            	protocolNumber = (String)objs[7];
 	            	onStudyChemo.setProtocolNumber(protocolNumber);  
 	            }  
 	            if(objs[8] != null) {    	
 	            	
 	               patientDid =  new Long(((BigDecimal)objs[8]).longValue());
 	               onStudyChemo.setPatientDid(patientDid);
 	            }
 	            
 	            if(objs[9] != null) {    	
 	            	
 	            	   regimenNumber =  new Long(((BigDecimal)objs[9]).longValue());
 	            	   onStudyChemo.setRegimenNumber(regimenNumber);
 		            }
 		            
 	            
 	            results.add(onStudyChemo);
 	        }
 		 
 	  }
 
   
   
   
     
     /**
 	   * private method to add individual column data retrieved from PRIOR_SURGERY to a arraylist
 	   * 
 	   */
 
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
     
     /**
 	   * private method to add individual column data retrieved from PT_SURGERY to a arraylist
 	   * 
 	   */
     private void populateOnStudySurgeryResults(Iterator onStudySurgeryObjects, ArrayList results) {
 		  while(onStudySurgeryObjects.hasNext()) {
 	            Object[] objs = (Object[]) onStudySurgeryObjects.next();     	
 	       
 	            String timePoint = null;	           
 	            String procedureTitle = null; 
 	            String indication = null; 	                
 	            Date surgeryDate = null;          
 	            String surgeryOutcome= null;	                 
 	            Long patientDid = null;  	
 	            String histoDiagnosis = null;	
 	          
 	            OnStudySurgery  onStudySurgery = new OnStudySurgery();
 	            
 	            if(objs[0] != null) {
 	               timePoint = (String)objs[0];
 	               onStudySurgery.setTimePoint(timePoint);
 	            }	
 	            
 	            if(objs[1] != null) {
 	            	procedureTitle = (String)objs[1];
 	            	onStudySurgery.setProcedureTitle(procedureTitle);
 		            }
 	            
 	            if(objs[2] != null) {
 	            	indication = (String)objs[2];
 	            	onStudySurgery.setIndication(indication);  
 	            }	            
 	          
 	            if(objs[3]!=null){
 	            	surgeryDate = new java.sql.Date(((java.util.Date)objs[3]).getTime());	
 	            	onStudySurgery.setSurgeryDate(surgeryDate);   
 	            }
 	            	            	           
 	            if(objs[4] != null) {
 	            	surgeryOutcome = (String)objs[4];
 	            	onStudySurgery.setSurgeryOutcome(surgeryOutcome);  
 	            }    
 	        	        
 	            if(objs[5] != null) {    	
 	            	
 	               patientDid =  new Long(((BigDecimal)objs[5]).longValue());
 	               onStudySurgery.setPatientDid(patientDid);
 	            }
 	            if(objs[6] != null) {    	
 	            	
 	            	histoDiagnosis =  (String)objs[6];
 		               onStudySurgery.setHistoDiagnosis(histoDiagnosis);
 		            }
 	            
 	            results.add(onStudySurgery);
 	        }
 		  
 	  }
 
 
     /**
 	   * private method to add individual column data retrieved from NEUROLOGICAL_EVALUATION to a arraylist
 	   * 
 	   */
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
 	            String neuroExamDesc = null;
 	            String mriScoreDesc = null;
 	            
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
 	           
 	            if(objs[11] != null) {
 	            	neuroExamDesc = (String)objs[11];
 		               clinicalEval.setNeuroExamDesc(neuroExamDesc); 
 		            } 
 	            
 	            if(objs[12] != null) {
 	            	mriScoreDesc = (String)objs[12];
 		               clinicalEval.setMriScoreDesc(mriScoreDesc);
 		            } 
 	            
 	            results.add(clinicalEval);
 	        }
 		  
 	  }
   /**
    * 
    * private method used to add the survival range to  OJB criteria
    */
     private void buildSurvivalRangeCrit(ClinicalDataQuery cghQuery, Criteria survivalCrit) {
         SurvivalCriteria crit = cghQuery.getSurvivalCriteria();
         
         if (crit != null) {
         	long lowerLmtInMons = 0;
         	long upperLmtInMons = 0;
         	long lowerLmtInDays  = 0;
         	long upperLmtInDays = 0;
         	
         	if(crit.getLowerSurvivalRange() != null && crit.getUpperSurvivalRange() != null) {        	
 	            lowerLmtInMons = crit.getLowerSurvivalRange().getValueObject().longValue();
 	            lowerLmtInDays  = lowerLmtInMons * 30;
 	            upperLmtInMons = crit.getUpperSurvivalRange().getValueObject().longValue();
 	            upperLmtInDays = upperLmtInMons * 30;
         	}
         	if(crit.getLowerSurvivalRange() == null && crit.getUpperSurvivalRange() != null) {    
         		upperLmtInMons = crit.getUpperSurvivalRange().getValueObject().longValue();
 	            upperLmtInDays = upperLmtInMons * 30;        		
         	}
         	if(crit.getLowerSurvivalRange() != null && crit.getUpperSurvivalRange() == null) {  
         		lowerLmtInMons = crit.getLowerSurvivalRange().getValueObject().longValue();
  	            lowerLmtInDays  = lowerLmtInMons * 30; 	           
         		upperLmtInMons = 90;
 	            upperLmtInDays = upperLmtInMons * 30;        		
         	}
             survivalCrit.addBetween(PatientData.SURVIVAL_LENGTH, new Long(lowerLmtInDays), new Long(upperLmtInDays));
         }
     }
     
     /**
      * 
      * private method used to add the age range to  OJB criteria
      */
     private void buildAgeRangeCrit(ClinicalDataQuery cghQuery, Criteria ageCrit ) {
         AgeCriteria crit = cghQuery.getAgeCriteria();
         long lowerLmtInYrs = 0;
         long upperLmtInYrs = 0;
         if (crit != null) {
         	 if(crit.getLowerAgeLimit() != null && crit.getUpperAgeLimit() != null) {
                 lowerLmtInYrs= crit.getLowerAgeLimit().getValueObject().longValue();
                 upperLmtInYrs  = crit.getUpperAgeLimit().getValueObject().longValue();                
         	 }
              if(crit.getLowerAgeLimit() != null && crit.getUpperAgeLimit() == null) {
                  lowerLmtInYrs= crit.getLowerAgeLimit().getValueObject().longValue();
                  upperLmtInYrs  = 90;          
             	 
              }
             if(crit.getLowerAgeLimit() == null && crit.getUpperAgeLimit() != null) {                
             	upperLmtInYrs  = crit.getUpperAgeLimit().getValueObject().longValue();
             }
             ageCrit.addBetween(PatientData.AGE, new Long(lowerLmtInYrs), new Long(upperLmtInYrs));
         }
     }
     
     /**
      * 
      * private method used to add the gender to  OJB criteria
      */
     private void buildGenderCrit(ClinicalDataQuery cghQuery, Criteria genderCrit) {
         GenderCriteria crit = cghQuery.getGenderCriteria();
         if (crit != null) {
             genderCrit.addEqualTo(PatientData.GENDER, crit.getGenderDE().getValueObject());
         }
     }
     
     
   
     /**
      * 
      * private method used to add the race to  OJB criteria
      */
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
     
     /**
      * 
      * private method used to add the Karnofsky scores to  OJB criteria
      */
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
     
     /**
      * 
      * private method used to add the Lansky scores to  OJB criteria
      */
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
     
     /**
      * 
      * private method used to add the MRI scores to  OJB criteria
      */
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
     
     /**
      * 
      * private method used to add the neuro exam scores to  OJB criteria
      */
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
    
     /**
      * private method to build a nested query based on patient DIDs, so the nested query would look like something like this:
      * where patient_did in (select patient_did from buildClinicalEvalCriteria where KARNOFSKY_SCORE =90 or LANSKY_SCORE=0
      * or NEURO_EXAM =-1 or MRI_CT_SCORE=2)
      *
      */
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
     
     /**
      * private method to build ClinicalEvalCriteria for OJB by "OR"ing Karnofsky scores, 
      * lanskyCrit scores, neuro exam scores and MRI scores' criteria together
      * 
      */
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
      
      
      /**
       * 
       * private method used to add prior radiation type query to  OJB criteria
       */
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
      
      /**
       * 
       * private method used to add onstudy radiation type query to  OJB criteria
       */
      
  private Criteria buildOnStudyRadiationCriteria(ClinicalDataQuery clinicalQuery) {
     	 
     	 OnStudyRadiationTherapyCriteria crit = clinicalQuery.getOnStudyRadiationTherapyCriteria(); 	 
          Criteria c = new Criteria();    	
      		 
      	if (crit != null) {     		 
          	ArrayList radiationTypes = new ArrayList();
                  for (Iterator iterator = crit.getRadiations().iterator(); iterator.hasNext();)
                 	 radiationTypes.add(((OnStudyRadiationTherapyDE) iterator.next()).getValueObject());     	    
                  
          		c.addColumnIn(OnStudyRadiationtherapy.RADIATION_TYPE, radiationTypes);
          		return c;  
      	 
      	   }
      	 else {
      		 return null;
      	  }	
     	 
      }
      
  /**
   * 
   * private method used to build prior radiation type query to  OJB ReportQueryByCriteria in order to 
   * construct a nested query based on patient DIDs
   * 
   */
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
      
      /**
       * 
       * private method used to build onstudy radiation type query to  OJB ReportQueryByCriteria in order to 
       * construct a nested query based on patient DIDs
       * 
       */
      private ReportQueryByCriteria getOnStudyRadiationTherapySubQuery(ClinicalDataQuery clinicalQuery,PersistenceBroker _BROKER,Class subQueryClass, String fieldToSelect) throws Exception {
     	 Criteria c = buildOnStudyRadiationCriteria (clinicalQuery);      	 
      	 if(c != null) {     		   
      	    String ptIDCol = getColumnNameForBean(_BROKER, subQueryClass.getName(), fieldToSelect);
      	    org.apache.ojb.broker.query.ReportQueryByCriteria onStudyRadiationTherapyQuery =
             QueryFactory.newReportQuery(subQueryClass, new String[]{ptIDCol}, c , true);    
      	    return onStudyRadiationTherapyQuery;
      	  }     	 
      	 
      	 else {
      		 return  null;
      	 }  
    
    	}
      
      /**
       * 
       * private method used to add prior chemo agent query to OJB criteria
       * 
       */
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
       
       /**
        * 
        * private method used to add onstudy chemo agent query to OJB criteria
        * 
        */
       private Criteria buildOnStudyChemoCriteria (ClinicalDataQuery clinicalQuery) {
      	 
      	 OnStudyChemoAgentCriteria crit = clinicalQuery.getOnStudyChemoAgentCriteria(); 	 
           Criteria c = new Criteria();      
           
       	 if (crit != null) {     		 
       		 ArrayList agentTypes = new ArrayList();
               for (Iterator iterator = crit.getAgents().iterator(); iterator.hasNext();)
              	 agentTypes.add(((OnStudyChemoAgentDE) iterator.next()).getValueObject());     	    
               
       		c.addColumnIn(OnStudyChemotherapy.AGENT_NAME, agentTypes);
       		return c;   
       	   }
       	 else {
       		 return null;
       	  }	
      	 
       }
 
       /**
        * 
        * private method used to build prior chemo agent query to  OJB ReportQueryByCriteria in order to 
        * construct a nested query based on patient DIDs
        * 
        */
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
      
      /**
       * 
       * private method used to build onstudy chemo agent query to  OJB ReportQueryByCriteria in order to 
       * construct a nested query based on patient DIDs
       * 
       */
      private ReportQueryByCriteria getOnStudyChemoTherapySubQuery(ClinicalDataQuery clinicalQuery,PersistenceBroker _BROKER,Class subQueryClass, String fieldToSelect) throws Exception {
      	
      	 Criteria c = buildOnStudyChemoCriteria(clinicalQuery);       	 
      	 if(c != null) {     		   
      	    String ptIDCol = getColumnNameForBean(_BROKER, subQueryClass.getName(), fieldToSelect);
      	    org.apache.ojb.broker.query.ReportQueryByCriteria onStudyChemoTherapyQuery =
             QueryFactory.newReportQuery(subQueryClass, new String[]{ptIDCol}, c , true);    
      	    return onStudyChemoTherapyQuery;
      	  }
      	 
      	 
      	 else {
      		 return  null;
      	 }  
    
      }
      
      /**
       * 
       * private method used to add prior therapy surgery title and outcome query to OJB criteria
       * 
       */
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
  
  /**
   * 
   * private method used to add onstudy therapy surgery title and outcome query to OJB criteria
   * 
   */
  private Criteria buildOnStudySurgeryCriteria (ClinicalDataQuery clinicalQuery) {
 	 
      OnStudySurgeryOutcomeCriteria crit = clinicalQuery.getOnStudySurgeryOutcomeCriteria(); 
      OnStudySurgeryTitleCriteria crit2 = clinicalQuery.getOnStudySurgeryTitleCriteria(); 
      
      Criteria c = new Criteria();    	 
  	 if (crit != null || crit2 != null ) {
  		 
  		 if(crit != null) {
  		 
  		    ArrayList outcomes = new ArrayList();
             for (Iterator iterator = crit.getOutcomes().iterator(); iterator.hasNext();)
         	   outcomes.add(((OnStudySurgeryOutcomeDE) iterator.next()).getValueObject());     	    
      	    c.addColumnIn(OnStudySurgery.SURGERY_OUTCOME, outcomes);
  		 }
  		 
  		 if(crit2 != null) {
  			 
  			 ArrayList titles = new ArrayList();
              for (Iterator iterator = crit2.getTitles().iterator(); iterator.hasNext();)
             	 titles.add(((OnStudySurgeryTitleDE) iterator.next()).getValueObject());     	    
       	     c.addColumnIn(OnStudySurgery.PROCEDURE_TITLE, titles);
   	
  		 }
      	
  		return c;   
  	 
  	   }
  	 else {
  		 return null;
  	  }	
 	 
  }
 
  /**
   * 
   * private method used to build prior therapy surgery title and outcome query to  OJB ReportQueryByCriteria in order to 
   * construct a nested query based on patient DIDs
   * 
   */
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
      
     
      /**
       * 
       * private method used to build onstudy therapy surgery title and outcome query to  OJB ReportQueryByCriteria in order to 
       * construct a nested query based on patient DIDs
       * 
       */
 
 private ReportQueryByCriteria getOnStudySurgeryTherapySubQuery(ClinicalDataQuery clinicalQuery,PersistenceBroker _BROKER,Class subQueryClass, String fieldToSelect) throws Exception {
 	
 	 Criteria c = buildOnStudySurgeryCriteria(clinicalQuery);       	 
 	 if(c != null) {     
 		
 	    String ptIDCol = getColumnNameForBean(_BROKER, subQueryClass.getName(), fieldToSelect);
 	    org.apache.ojb.broker.query.ReportQueryByCriteria onStudyChemoTherapyQuery =
        QueryFactory.newReportQuery(subQueryClass, new String[]{ptIDCol}, c , true);    
 	    return onStudyChemoTherapyQuery;
 	  }
 	 
 	 
 	 else {
 		 return  null;
 	 }  
 
  }
 
 	public Integer getCount(Query query) throws Exception {
 		//faking count query for this release
 		ResultSet[] resultset = handle(query);
 		if(resultset != null){
 			return resultset.length;
 		}
 		return null;
 	}
 
 }
     
     
   
