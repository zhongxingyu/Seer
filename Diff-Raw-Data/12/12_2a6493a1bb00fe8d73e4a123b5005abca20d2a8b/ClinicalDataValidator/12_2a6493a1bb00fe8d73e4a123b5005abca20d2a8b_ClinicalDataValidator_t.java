 package gov.nih.nci.rembrandt.queryservice.validation;
 
 import gov.nih.nci.caintegrator.dto.critieria.CloneOrProbeIDCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SampleCriteria;
 import gov.nih.nci.caintegrator.dto.de.CloneIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.DatumDE;
 import gov.nih.nci.caintegrator.dto.de.SampleIDDE;
 import gov.nih.nci.caintegrator.dto.query.QueryType;
 import gov.nih.nci.caintegrator.dto.view.ViewFactory;
 import gov.nih.nci.caintegrator.dto.view.ViewType;
 import gov.nih.nci.caintegrator.enumeration.ClinicalFactorType;
 import gov.nih.nci.rembrandt.dto.lookup.LookupManager;
 import gov.nih.nci.rembrandt.dto.lookup.PatientDataLookup;
 import gov.nih.nci.rembrandt.dto.query.ClinicalDataQuery;
 import gov.nih.nci.rembrandt.dto.query.CompoundQuery;
 import gov.nih.nci.rembrandt.dto.query.GeneExpressionQuery;
 import gov.nih.nci.rembrandt.queryservice.QueryManager;
 import gov.nih.nci.rembrandt.queryservice.ResultsetManager;
 import gov.nih.nci.rembrandt.queryservice.resultset.DimensionalViewContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.Resultant;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.GeneExprResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.ReporterResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleViewResultsContainer;
 import gov.nih.nci.rembrandt.web.helper.ListConvertor;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import javax.naming.OperationNotSupportedException;
 
 import org.apache.log4j.Logger;
 
 /**
  * @author sahnih
  *	Allows validatoion of samples based on collection of ClinicalFactorTypes
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
		if(sampleList != null && sampleList.size() > 0  && clinicalFactors != null){
 			//Create a map of sample collections for each ClinicalFactorType that were passed
 			Map<ClinicalFactorType, Collection<SampleIDDE>> clinicalFactorMap = new HashMap<ClinicalFactorType,Collection<SampleIDDE>>();
 			//execute Clinical Query
 			Collection<SampleResultset> sampleResultsets = executeClinicalQuery(sampleList);
 			for(ClinicalFactorType clinicalFactor: clinicalFactors){
 				//samples associated with each clinical factor are stored in a seperate collection
 				Collection<SampleIDDE> samples = new HashSet<SampleIDDE>();
				if(sampleResultsets != null){	
					for(SampleResultset sampleResultset: sampleResultsets) {	
 						switch(clinicalFactor){
 							case AgeAtDx:
 								if( sampleResultset.getAge()!= null &&  sampleResultset.getAge() != null){
 									samples.add(sampleResultset.getSampleIDDE());
 								}
 								break;		 									
 							case Treatment:
 								//if( sampleResultset.()!= null  && sampleResultset.().getValue() != null){
 								//	samples.add(new SampleIDDE(sampleResultset.getBiospecimen().getValue().toString()));
 								//}
 								break;
 							case KarnofskyAssessment:
 								if( sampleResultset.getKarnofskyClinicalEvalDE()!= null && sampleResultset.getKarnofskyClinicalEvalDE().getValue() != null){
 									samples.add(sampleResultset.getSampleIDDE());
 								}
 								break;
 							case SurvivalLength:
 								if( sampleResultset.getSurvivalLength()!= null && sampleResultset.getSurvivalLength() != null){
 									samples.add(sampleResultset.getSampleIDDE());
 								}
 								break;
 							case Censor:
 								if( sampleResultset.getCensor()!= null && sampleResultset.getCensor().getValue() != null){
 									samples.add(sampleResultset.getSampleIDDE());
 								}
 								break;
 							case Gender:
 								if( sampleResultset.getGenderCode()!= null && sampleResultset.getGenderCode().getValue() != null){
 									samples.add(sampleResultset.getSampleIDDE());
 								}
 								break;
 							case Disease:
 								if( sampleResultset.getDisease()!= null && sampleResultset.getDisease().getValue() != null){
 									samples.add(sampleResultset.getSampleIDDE());
 								}
 								break;
 						}//switch
					}//for loop
				}//if
 				clinicalFactorMap.put(clinicalFactor,samples);
 			}//for loop
 			sampleList = getValidSampleSet(sampleList,clinicalFactorMap);
 			
 			   //Map paitentDataLookup = LookupManager.getPatientDataMap();
 			
 			  for(SampleResultset sampleResultset: sampleResultsets) {
 				for(SampleIDDE sampleIDDE: sampleList) {
 					if(sampleIDDE != null  && sampleResultset != null  &&
 							sampleResultset.getSampleIDDE().getValueObject().equals(sampleIDDE.getValueObject())){	
 			    		//PatientDataLookup patient = (PatientDataLookup) paitentDataLookup.get(sampleIDDE.getValueObject());
 			    		//if(patient != null  && patient.getSurvivalLength() != null){
 			    		//	sampleResultset.setSurvivalLength(new DatumDE(DatumDE.SURVIVAL_LENGTH,patient.getSurvivalLength()));
 			    		//}
 						validSampleResultsets.add(sampleResultset);
 					}						
 				}
 			}
 			
 		}
 	  return validSampleResultsets;
 	}
 	@SuppressWarnings("unchecked")
 	public static Map <String,SampleResultset> getClinicalAnnotationsMapForSamples(Collection<String> sampleIDs) throws Exception{
 		return getClinicalAnnotationsMapForSampleIDDEs(ListConvertor.convertToSampleIDDEs(sampleIDs));
 	}
 	public static Map <String,SampleResultset> getClinicalAnnotationsMapForSampleIDDEs(Collection<SampleIDDE> sampleIDs) throws Exception{
 		Map <String,SampleResultset> sampleResultsetMap= new HashMap<String,SampleResultset>();
 		//execute Clinical Query
 		Collection<SampleResultset> sampleResultsets = executeClinicalQuery(sampleIDs);
 		if(sampleIDs != null && sampleResultsets != null){
 
 			for(SampleResultset sampleResultset: sampleResultsets) {
 				if(sampleResultset != null  && sampleResultset.getSampleIDDE()!= null){							
 					sampleResultsetMap.put(sampleResultset.getSampleIDDE().getValueObject(),sampleResultset);
 					}
 				}
 		 	}
 		return sampleResultsetMap;
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
 		public static Collection<SampleResultset> executeClinicalQuery(Collection<SampleIDDE> sampleList) throws Exception{
 			Collection<SampleResultset> sampleResultsets =  Collections.EMPTY_LIST;
 			//create a ClinicalDataQuery to contrain by Insitition group
 			 ClinicalDataQuery clinicalDataQuery = (ClinicalDataQuery) QueryManager.createQuery(QueryType.CLINICAL_DATA_QUERY_TYPE);
 			 clinicalDataQuery.setAssociatedView(ViewFactory.newView(ViewType.CLINICAL_VIEW));
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
 	 						sampleResultsets = svrContainer.getSampleResultsets();
 	 					}
 		 	}
 	  		return sampleResultsets;
 		}
 		public static Collection<SampleResultset> executeClinicalQueryForSampleList(Collection<String> sampleList) throws Exception{
 			return executeClinicalQuery(ListConvertor.convertToSampleIDDEs(sampleList)); 
 		}
 		public static Collection<SampleResultset> getValidatedSampleResultsetsFromSampleIDs(Collection<String> sampleList, Collection<ClinicalFactorType> clinicalFactors) throws Exception  {
 			return getValidatedSampleResultsets(ListConvertor.convertToSampleIDDEs(sampleList), clinicalFactors); 
 		}
 
 }
