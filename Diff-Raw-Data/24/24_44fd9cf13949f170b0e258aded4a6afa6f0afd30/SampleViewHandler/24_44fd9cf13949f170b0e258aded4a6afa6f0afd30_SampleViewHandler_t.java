 package gov.nih.nci.rembrandt.queryservice.resultset.sample;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import gov.nih.nci.caintegrator.dto.de.BioSpecimenIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.DatumDE;
 import gov.nih.nci.caintegrator.dto.de.DiseaseNameDE;
 import gov.nih.nci.caintegrator.dto.de.GenderDE;
 import gov.nih.nci.caintegrator.dto.de.KarnofskyClinicalEvalDE;
 import gov.nih.nci.caintegrator.dto.de.LanskyClinicalEvalDE;
 import gov.nih.nci.caintegrator.dto.de.MRIClinicalEvalDE;
 import gov.nih.nci.caintegrator.dto.de.NeuroExamClinicalEvalDE;
 import gov.nih.nci.caintegrator.dto.de.RaceDE;
 import gov.nih.nci.caintegrator.dto.de.SampleIDDE;
 import gov.nih.nci.caintegrator.dto.view.GroupType;
 import gov.nih.nci.rembrandt.dbbean.PatientData;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ge.GeneExpr;
 import gov.nih.nci.rembrandt.queryservice.resultset.ClinicalResultSet;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultSet;
 import gov.nih.nci.rembrandt.queryservice.resultset.copynumber.CopyNumberSingleViewHandler;
 import gov.nih.nci.rembrandt.queryservice.resultset.copynumber.CopyNumberSingleViewResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.GeneExprSingleViewHandler;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.GeneExprSingleViewResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.validation.ClinicalDataValidator;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.cgh.CopyNumber;
 
 /**
  * @author SahniH
  * Date: Oct 22, 2004
  * 
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
 
 public class SampleViewHandler {
 	private static Logger logger = Logger.getLogger(SampleViewHandler.class);
     public static SampleViewResultsContainer handleSampleView(SampleViewResultsContainer sampleViewContainer, ResultSet resultObj, GroupType groupType){
     	SampleResultset sampleResultset = null;
     	if (sampleViewContainer != null && resultObj instanceof GeneExpr.GeneExprSingle){
     		GeneExpr.GeneExprSingle geneExprObj = (GeneExpr.GeneExprSingle)resultObj;
       		//sampleResultset = handleBioSpecimenResultset(sampleViewContainer,geneExprObj);
     	   	sampleResultset = (SampleResultset) sampleViewContainer.getSampleResultset(geneExprObj.getSampleId());
       		if(sampleResultset == null){ // no record found
       			sampleResultset = new SampleResultset();
       		}
           	//Propulate the GeneExprSingleResultsContainer
       		GeneExprSingleViewResultsContainer geneExprSingleViewContainer = sampleResultset.getGeneExprSingleViewResultsContainer();
         	if(geneExprSingleViewContainer == null){
         		geneExprSingleViewContainer = new GeneExprSingleViewResultsContainer();
         	}
         	geneExprSingleViewContainer = GeneExprSingleViewHandler.handleGeneExprSingleView(geneExprSingleViewContainer,geneExprObj, groupType);
       		sampleResultset.setGeneExprSingleViewResultsContainer(geneExprSingleViewContainer);
            	//Populate the SampleViewResultsContainer
       		sampleViewContainer.addSampleResultset(sampleResultset);
     	}
       	else if(sampleViewContainer != null && resultObj instanceof CopyNumber){
       		CopyNumber copyNumberObj = (CopyNumber)resultObj;
       		//sampleResultset = handleBioSpecimenResultset(sampleViewContainer,copyNumberObj);
     	   	sampleResultset = (SampleResultset) sampleViewContainer.getSampleResultset(copyNumberObj.getSampleId());
       		if(sampleResultset == null){ // no record found
       			sampleResultset = new SampleResultset();
       		}
           	//Propulate the GeneExprSingleResultsContainer
    
           	//Propulate the GeneExprSingleResultsContainer
       		CopyNumberSingleViewResultsContainer copyNumberSingleViewResultsContainer = sampleResultset.getCopyNumberSingleViewResultsContainer();
         	if(copyNumberSingleViewResultsContainer == null){
         		copyNumberSingleViewResultsContainer = new CopyNumberSingleViewResultsContainer();
         	}
         	copyNumberSingleViewResultsContainer = CopyNumberSingleViewHandler.handleCopyNumberSingleView(copyNumberSingleViewResultsContainer,copyNumberObj, groupType);
       		sampleResultset.setCopyNumberSingleViewResultsContainer(copyNumberSingleViewResultsContainer);
            	//Populate the SampleViewResultsContainer
       		sampleViewContainer.addSampleResultset(sampleResultset);
     	}
 
       	return sampleViewContainer;
     }
     public static SampleResultset handleBioSpecimenResultset(SampleViewResultsContainer sampleViewContainer, ClinicalResultSet clinicalObj){
  		//get the gene accessesion number for this record
   		//check if the gene exsists in the GeneExprSingleViewResultsContainer, otherwise add a new one.
     	SampleResultset sampleResultset = (SampleResultset) sampleViewContainer.getSampleResultset(clinicalObj.getSampleId());
   		if(sampleResultset == null){ // no record found
   			sampleResultset = new SampleResultset();
   		}
 		//find out the biospecimenID associated with the GeneExpr.GeneExprSingle
 		//populate the BiospecimenResuluset
 		BioSpecimenIdentifierDE bioSpecimenIdentifierDE = new BioSpecimenIdentifierDE(clinicalObj.getBiospecimenId());
 		bioSpecimenIdentifierDE.setSampleId(clinicalObj.getSampleId());
 		bioSpecimenIdentifierDE.setSpecimenName(clinicalObj.getSpecimenName());
 		sampleResultset.setBiospecimen(bioSpecimenIdentifierDE);
 		sampleResultset.setSampleIDDE(new SampleIDDE(clinicalObj.getSampleId().toString()));
 		sampleResultset.setAgeGroup(new DatumDE(DatumDE.AGE_GROUP,clinicalObj.getAgeGroup()));
 		sampleResultset.setSurvivalLengthRange(new DatumDE(DatumDE.SURVIVAL_LENGTH_RANGE,clinicalObj.getSurvivalLengthRange()));
 		sampleResultset.setGenderCode(new GenderDE(clinicalObj.getGenderCode()));
 		sampleResultset.setDisease(new DiseaseNameDE(clinicalObj.getDiseaseType()));		
 		sampleResultset.setInstitutionName(clinicalObj.getInstitutionName());
 		return sampleResultset;
     }
     public static SampleResultset handleBioSpecimenResultset(SampleViewResultsContainer sampleViewContainer, PatientData clinicalObj){
  		//get the gene accessesion number for this record
   		//check if the gene exsists in the GeneExprSingleViewResultsContainer, otherwise add a new one.
     	SampleResultset sampleResultset = (SampleResultset) sampleViewContainer.getSampleResultset(clinicalObj.getSampleId());
   		if(sampleResultset == null){ // no record found
   			sampleResultset = new SampleResultset();
   		}
 		//find out the biospecimenID associated with the GeneExpr.GeneExprSingle
 		//populate the BiospecimenResuluset
 		BioSpecimenIdentifierDE bioSpecimenIdentifierDE = new BioSpecimenIdentifierDE(clinicalObj.getBiospecimenId());
 		bioSpecimenIdentifierDE.setSampleId(clinicalObj.getSampleId());
		if(clinicalObj.getSpecimenName() != null){
			bioSpecimenIdentifierDE.setSpecimenName(clinicalObj.getSpecimenName());
		}
 		sampleResultset.setBiospecimen(bioSpecimenIdentifierDE);
 		sampleResultset.setSampleIDDE(new SampleIDDE(clinicalObj.getSampleId().toString()));
 		sampleResultset.setAgeGroup(new DatumDE(DatumDE.AGE_GROUP,clinicalObj.getAgeGroup()));
 		sampleResultset.setSurvivalLengthRange(new DatumDE(DatumDE.SURVIVAL_LENGTH_RANGE,clinicalObj.getSurvivalLengthRange()));
 		sampleResultset.setGenderCode(new GenderDE(clinicalObj.getGenderCode()));
 		sampleResultset.setDisease(new DiseaseNameDE(clinicalObj.getDiseaseType()));			
 	    sampleResultset.setWhoGrade(clinicalObj.getWhoGrade());	
 		sampleResultset.setRaceDE(new RaceDE(clinicalObj.getRace()));
 		sampleResultset.setInstitutionName(clinicalObj.getInstitutionName());
 		if(clinicalObj.getAge() != null){
 			sampleResultset.setAge(clinicalObj.getAge());
 		}
 		sampleResultset.setRaceDE(new RaceDE(clinicalObj.getRace()));
 		if(clinicalObj.getSurvivalLength() != null){
 			sampleResultset.setSurvivalLength(clinicalObj.getSurvivalLength());
 		}
 		if(clinicalObj.getCensoringStatus() != null){
 			sampleResultset.setCensor(new DatumDE(DatumDE.CENSOR,clinicalObj.getCensoringStatus()));
 		}
 
 		if(clinicalObj.getKarnofskyScore() != null) {
 		   sampleResultset.setKarnofskyClinicalEvalDE(new KarnofskyClinicalEvalDE(clinicalObj.getKarnofskyScore().toString()));
 		}
 		
 		if(clinicalObj.getKarnofskyScores() != null) {
 			   
 			   sampleResultset.setKarnofskyClinicalEvalDE(new KarnofskyClinicalEvalDE(clinicalObj.getKarnofskyScores().toString()));
 				
 		}
 		
 		if(clinicalObj.getLanskyScore() != null) {
 			   sampleResultset.setLanskyClinicalEvalDE(new LanskyClinicalEvalDE(clinicalObj.getLanskyScore().toString()));
 			}
 			   
 	    if(clinicalObj.getLanskyScores() != null) {
 				
 				 sampleResultset.setLanskyClinicalEvalDE(new LanskyClinicalEvalDE(clinicalObj.getLanskyScores().toString()));
 					
 			}
 		if(clinicalObj.getNeuroExam() != null) {
 			   sampleResultset.setNeuroExamClinicalEvalDE(new NeuroExamClinicalEvalDE(clinicalObj.getNeuroExam().toString()));
 			}
 		
 		if(clinicalObj.getNeuroExams() != null) {
 			  
 			   sampleResultset.setNeuroExamClinicalEvalDE(new NeuroExamClinicalEvalDE(clinicalObj.getNeuroExams().toString()));
 				
 			}
 		
 		if(clinicalObj.getNeuroExamDescs() != null) {
 			   sampleResultset.setNeuroExamDescs(new String(clinicalObj.getNeuroExamDescs()));	
 			}
 		
 		if(clinicalObj.getMriCtScore() != null) {
 			   sampleResultset.setMriClinicalEvalDE(new MRIClinicalEvalDE(clinicalObj.getMriCtScores().toString()));
 			}
 			
 		
 		if(clinicalObj.getMriCtScores() != null) {
 			  
 			   sampleResultset.setMriClinicalEvalDE(new MRIClinicalEvalDE(clinicalObj.getMriCtScores().toString()));
 				
 			}
 		
 		if(clinicalObj.getMriScoreDescs() != null) {
 			   sampleResultset.setMriScoreDescs(new String(clinicalObj.getMriScoreDescs()));	
 			}
 		
 		if(clinicalObj.getTimePoint() != null) {
 			   sampleResultset.setTimePoint(new String(clinicalObj.getTimePoint()));
 			}
 			
 		if(clinicalObj.getTimePoints() != null) {
 			   sampleResultset.setTimePoints(new String(clinicalObj.getTimePoints()));
 			}
 		
 		    if(clinicalObj.getFollowupDate() != null) {
 			   sampleResultset.setFollowupDate(clinicalObj.getFollowupDate());
 			}
 			
 		if(clinicalObj.getFollowupDates() != null) {
 			   sampleResultset.setFollowupDates(clinicalObj.getFollowupDates());
 			}
 		if(clinicalObj.getFollowupMonth() != null) {
 			   sampleResultset.setFollowupMonth(clinicalObj.getFollowupMonth());
 			}
 		
 		if(clinicalObj.getFollowupMonths() != null) {
 			   sampleResultset.setFollowupMonths(clinicalObj.getFollowupMonths());
 			}
 		if(clinicalObj.getNeuroEvaluationDate() != null) {
 			   sampleResultset.setNeuroEvaluationDate(clinicalObj.getNeuroEvaluationDate());
 			}
 		
 		if(clinicalObj.getNeuroEvaluationDates() != null) {
 			   sampleResultset.setNeuroEvaluationDates(clinicalObj.getNeuroEvaluationDates());
 			}
 		if(clinicalObj.getSteroidDoseStatus() != null) {
 			   sampleResultset.setSteroidDoseStatus(clinicalObj.getSteroidDoseStatus());
 			}
 		
 		if(clinicalObj.getSteroidDoseStatuses() != null) {
 			   sampleResultset.setSteroidDoseStatuses(clinicalObj.getSteroidDoseStatuses());
 			}
 		if(clinicalObj.getAntiConvulsantStatus() != null) {
 			   sampleResultset.setAntiConvulsantStatus(clinicalObj.getAntiConvulsantStatus());
 			}
 			
 		if(clinicalObj.getAntiConvulsantStatuses() != null) {
 			   sampleResultset.setAntiConvulsantStatuses(clinicalObj.getAntiConvulsantStatuses());
 			}
 		if(clinicalObj.getPriorRadiationTimePoints() != null) {
 			   sampleResultset.setPriorRadiationTimePoints(clinicalObj.getPriorRadiationTimePoints());
 			}
 		
 		if(clinicalObj.getPriorRadiationRadiationSites() != null) {
 			   sampleResultset.setPriorRadiationRadiationSites(clinicalObj.getPriorRadiationRadiationSites());
 			}
 		
 		if(clinicalObj.getPriorRadiationDoseStartDates() != null) {
 			   sampleResultset.setPriorRadiationDoseStartDates(clinicalObj.getPriorRadiationDoseStartDates());
 			}
 		
 		if(clinicalObj.getPriorRadiationDoseStopDates() != null) {
 			   sampleResultset.setPriorRadiationDoseStopDates(clinicalObj.getPriorRadiationDoseStopDates());
 			}
 		
 		if(clinicalObj.getPriorRadiationFractionDoses() != null) {
 			   sampleResultset.setPriorRadiationFractionDoses(clinicalObj.getPriorRadiationFractionDoses());
 			}
 		
 		if(clinicalObj.getPriorRadiationFractionNumbers() != null) {
 			   sampleResultset.setPriorRadiationFractionNumbers(clinicalObj.getPriorRadiationFractionNumbers());
 			}
 		
 		if(clinicalObj.getPriorRadiationRadiationTypes() != null) {
 			   sampleResultset.setPriorRadiationRadiationTypes(clinicalObj.getPriorRadiationRadiationTypes());
 			}
 		
 		if(clinicalObj.getPriorChemoTimePoints() != null) {
 			   sampleResultset.setPriorChemoTimePoints(clinicalObj.getPriorChemoTimePoints());
 			}
 		
 		if(clinicalObj.getPriorChemoagentIds() != null) {
 			   sampleResultset.setPriorChemoagentIds(clinicalObj.getPriorChemoagentIds());
 			}
 		
 		if(clinicalObj.getPriorChemoAgentNames() != null) {
 			   sampleResultset.setPriorChemoAgentNames(clinicalObj.getPriorChemoAgentNames());
 			}
 		
 		if(clinicalObj.getPriorChemoCourseCounts() != null) {
 			   sampleResultset.setPriorChemoCourseCounts(clinicalObj.getPriorChemoCourseCounts());
 			}
 		
 		if(clinicalObj.getPriorChemoDoseStartDates() != null) {
 			   sampleResultset.setPriorChemoDoseStartDates(clinicalObj.getPriorChemoDoseStartDates());
 			}
 		
 		if(clinicalObj.getPriorChemoDoseStopDates() != null) {
 			   sampleResultset.setPriorChemoDoseStopDates(clinicalObj.getPriorChemoDoseStopDates());
 			}
 		
 		if(clinicalObj.getPriorChemoStudySources() != null) {
 			   sampleResultset.setPriorChemoStudySources(clinicalObj.getPriorChemoStudySources());
 			}
 		
 		if(clinicalObj.getPriorChemoProtocolNumbers() != null) {
 			   sampleResultset.setPriorChemoProtocolNumbers(clinicalObj.getPriorChemoProtocolNumbers());
 			}
 		
 		if(clinicalObj.getPriorSurgeryTimePoints() != null) {
 			   sampleResultset.setPriorSurgeryTimePoints(clinicalObj.getPriorSurgeryTimePoints());
 			}
 		
 		if(clinicalObj.getPriorSurgeryProcedureTitles() != null) {
 			   sampleResultset.setPriorSurgeryProcedureTitles(clinicalObj.getPriorSurgeryProcedureTitles());
 			}
 		
 		if(clinicalObj.getPriorSurgeryTumorHistologys() != null) {
 			   sampleResultset.setPriorSurgeryTumorHistologys(clinicalObj.getPriorSurgeryTumorHistologys());
 			}
 		
 		if(clinicalObj.getPriorSurgerySurgeryDates() != null) {
 			   sampleResultset.setPriorSurgerySurgeryDates(clinicalObj.getPriorSurgerySurgeryDates());
 			}
 		
 		if(clinicalObj.getPriorSurgerySurgeryOutcomes() != null) {
 			   sampleResultset.setPriorSurgerySurgeryOutcomes(clinicalObj.getPriorSurgerySurgeryOutcomes());
 			}
 		
 		
 		if(clinicalObj.getOnStudyRadiationTimePoints() != null) {
 			   sampleResultset.setOnStudyRadiationTimePoints(clinicalObj.getOnStudyRadiationTimePoints());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationRadiationSites() != null) {
 			   sampleResultset.setOnStudyRadiationRadiationSites(clinicalObj.getOnStudyRadiationRadiationSites());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationDoseStartDates() != null) {
 			   sampleResultset.setOnStudyRadiationDoseStartDates(clinicalObj.getOnStudyRadiationDoseStartDates());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationDoseStopDates() != null) {
 			   sampleResultset.setOnStudyRadiationDoseStopDates(clinicalObj.getOnStudyRadiationDoseStopDates());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationFractionDoses() != null) {
 			   sampleResultset.setOnStudyRadiationFractionDoses(clinicalObj.getOnStudyRadiationFractionDoses());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationFractionNumbers() != null) {
 			   sampleResultset.setOnStudyRadiationFractionNumbers(clinicalObj.getOnStudyRadiationFractionNumbers());
 			}
 		
 		if(clinicalObj.getOnStudyRadiationNeurosisStatuses() != null) {
 			   sampleResultset.setOnStudyRadiationNeurosisStatuses(clinicalObj.getOnStudyRadiationNeurosisStatuses());
 			}
 		if(clinicalObj.getOnStudyRadiationRadiationTypes() != null) {
 			   sampleResultset.setOnStudyRadiationRadiationTypes(clinicalObj.getOnStudyRadiationRadiationTypes());
 			}
 		
 		//starts onstudy chemo
 		if(clinicalObj.getOnStudyChemoTimePoints() != null) {
 			   sampleResultset.setOnStudyChemoTimePoints(clinicalObj.getOnStudyChemoTimePoints());
 			}
 		
 		if(clinicalObj.getOnStudyChemoagentIds() != null) {
 			   sampleResultset.setOnStudyChemoagentIds(clinicalObj.getOnStudyChemoagentIds());
 			}
 		
 		if(clinicalObj.getOnStudyChemoAgentNames() != null) {
 			   sampleResultset.setOnStudyChemoAgentNames(clinicalObj.getOnStudyChemoAgentNames());
 			}
 		
 		if(clinicalObj.getOnStudyChemoRegimenNumbers() != null) {
 			   sampleResultset.setOnStudyChemoRegimenNumbers(clinicalObj.getOnStudyChemoRegimenNumbers());
 			}
 		
 		if(clinicalObj.getOnStudyChemoCourseCounts() != null) {
 			   sampleResultset.setOnStudyChemoCourseCounts(clinicalObj.getOnStudyChemoCourseCounts());
 			}
 		
 		if(clinicalObj.getOnStudyChemoDoseStartDates() != null) {
 			   sampleResultset.setOnStudyChemoDoseStartDates(clinicalObj.getOnStudyChemoDoseStartDates());
 			}
 		
 		if(clinicalObj.getOnStudyChemoDoseStopDates() != null) {
 			   sampleResultset.setOnStudyChemoDoseStopDates(clinicalObj.getOnStudyChemoDoseStopDates());
 			}
 		
 		if(clinicalObj.getOnStudyChemoStudySources() != null) {
 			   sampleResultset.setOnStudyChemoStudySources(clinicalObj.getOnStudyChemoStudySources());
 			}
 		
 		if(clinicalObj.getOnStudyChemoProtocolNumbers() != null) {
 			   sampleResultset.setOnStudyChemoProtocolNumbers(clinicalObj.getOnStudyChemoProtocolNumbers());
 			}
 		
 		
 		
 	 // starts onstudy surgery
 		
 		
 		if(clinicalObj.getOnStudySurgeryTimePoints() != null) {
 			   sampleResultset.setOnStudySurgeryTimePoints(clinicalObj.getOnStudySurgeryTimePoints());
 			}
 		
 		if(clinicalObj.getOnStudySurgeryProcedureTitles() != null) {
 			   sampleResultset.setOnStudySurgeryProcedureTitles(clinicalObj.getOnStudySurgeryProcedureTitles());
 			}
 		
 		if(clinicalObj.getOnStudySurgeryHistoDiagnoses() != null) {
 			   sampleResultset.setOnStudySurgeryHistoDiagnoses(clinicalObj.getOnStudySurgeryHistoDiagnoses());
 			}
 		
 		if(clinicalObj.getOnStudySurgerySurgeryDates() != null) {
 			   sampleResultset.setOnStudySurgerySurgeryDates(clinicalObj.getOnStudySurgerySurgeryDates());
 			}
 		
 		if(clinicalObj.getOnStudySurgerySurgeryOutcomes() != null) {
 			   sampleResultset.setOnStudySurgerySurgeryOutcomes(clinicalObj.getOnStudySurgerySurgeryOutcomes());
 			}
 		
 		if(clinicalObj.getOnStudySurgeryIndications() != null) {
 			   sampleResultset.setOnStudySurgeryIndications(clinicalObj.getOnStudySurgeryIndications());
 			}
 		
 		
 		return sampleResultset;
     }
 	/**
 	 * @param sampleViewResultsContainer
 	 * @param patientDataObj
 	 * @return
 	 */
 	public static SampleViewResultsContainer handleSampleView(SampleViewResultsContainer sampleViewResultsContainer, PatientData patientDataObj) {
     	SampleResultset sampleResultset = null;
     	if (sampleViewResultsContainer != null && patientDataObj != null){
       		sampleResultset = handleBioSpecimenResultset(sampleViewResultsContainer,patientDataObj);
            	//Populate the SampleViewResultsContainer
       		sampleViewResultsContainer.addSampleResultset(sampleResultset);
     	}
     	return sampleViewResultsContainer;
 	}
 	public static SampleViewResultsContainer populateWithClinicalData(SampleViewResultsContainer sampleViewResultsContainer, ClinicalResultSet[] clinicalObjs ) throws Exception {
 		//SampleViewResultsContainer sampleViewResultsContainer = new SampleViewResultsContainer();
 		if(clinicalObjs !=null){
 			Set<String> specimenNameSet = new HashSet<String>();			
 			for (int i = 0; i < clinicalObjs.length; i++) {
 				if(clinicalObjs[i] != null  && clinicalObjs[i].getSpecimenName()!= null){
 					specimenNameSet.add(clinicalObjs[i].getSpecimenName());
 				}
 			}
 			try {
 				if(specimenNameSet != null  && specimenNameSet.size() > 0){
 					Collection <SampleResultset> sampleResultsets = ClinicalDataValidator.getSampleResultsetForSpecimens(specimenNameSet);
 					for(SampleResultset sampleResultset:sampleResultsets){
 						sampleViewResultsContainer.addSampleResultset(sampleResultset);
 					}
 				}
 				
 			} catch (Exception e) {
 				logger.error(e.getMessage());
 				throw e;
 			}
 		}
 		return sampleViewResultsContainer;
 	}
 }
