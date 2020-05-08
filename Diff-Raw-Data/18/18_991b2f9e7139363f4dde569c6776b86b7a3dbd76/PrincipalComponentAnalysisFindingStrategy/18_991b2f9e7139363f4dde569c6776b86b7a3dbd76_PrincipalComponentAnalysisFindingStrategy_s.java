 package gov.nih.nci.rembrandt.service.findings.strategies;
 
 import gov.nih.nci.caintegrator.analysis.messaging.PrincipalComponentAnalysisRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.ReporterGroup;
 import gov.nih.nci.caintegrator.analysis.messaging.SampleGroup;
 import gov.nih.nci.caintegrator.dto.critieria.InstitutionCriteria;
 import gov.nih.nci.caintegrator.dto.de.CloneIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.GeneIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.SampleIDDE;
 import gov.nih.nci.caintegrator.dto.query.ClinicalQueryDTO;
 import gov.nih.nci.caintegrator.dto.query.PrincipalComponentAnalysisQueryDTO;
 import gov.nih.nci.caintegrator.dto.query.QueryDTO;
 import gov.nih.nci.caintegrator.dto.view.ClinicalSampleView;
 import gov.nih.nci.caintegrator.dto.view.ViewFactory;
 import gov.nih.nci.caintegrator.dto.view.ViewType;
 import gov.nih.nci.caintegrator.dto.view.Viewable;
 import gov.nih.nci.caintegrator.enumeration.ArrayPlatformType;
 import gov.nih.nci.caintegrator.enumeration.FindingStatus;
 import gov.nih.nci.caintegrator.exceptions.FindingsAnalysisException;
 import gov.nih.nci.caintegrator.exceptions.FindingsQueryException;
 import gov.nih.nci.caintegrator.exceptions.ValidationException;
 import gov.nih.nci.caintegrator.service.findings.ClassComparisonFinding;
 import gov.nih.nci.caintegrator.service.findings.Finding;
 import gov.nih.nci.caintegrator.service.findings.PrincipalComponentAnalysisFinding;
 import gov.nih.nci.caintegrator.service.findings.strategies.FindingStrategy;
 import gov.nih.nci.caintegrator.util.ValidationUtility;
 import gov.nih.nci.caintegrator.application.analysis.AnalysisServerClientManager;
 import gov.nih.nci.caintegrator.application.cache.BusinessTierCache;
 
 import gov.nih.nci.rembrandt.dto.query.ClinicalDataQuery;
 import gov.nih.nci.rembrandt.dto.query.CompoundQuery;
 import gov.nih.nci.rembrandt.dto.query.GeneExpressionQuery;
 import gov.nih.nci.rembrandt.queryservice.ResultsetManager;
 import gov.nih.nci.rembrandt.queryservice.resultset.Resultant;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.validation.DataValidator;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.jms.JMSException;
 import javax.naming.NamingException;
 import javax.naming.OperationNotSupportedException;
 
 import org.apache.log4j.Logger;
 
 /**
  * @author sahnih
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
 
 public class PrincipalComponentAnalysisFindingStrategy implements FindingStrategy {
 	private static Logger logger = Logger.getLogger(PrincipalComponentAnalysisFindingStrategy.class);	
 	@SuppressWarnings("unused")
 	private PrincipalComponentAnalysisQueryDTO myQueryDTO;
 	private ReporterGroup reporterGroup; 
 	private SampleGroup sampleGroup = new SampleGroup("validatedSamples");
 	private String sessionId;
 	private String taskId;
     private Collection<ClinicalDataQuery> clinicalQueries;
 	private GeneExpressionQuery geneExprQuery;
 	private Collection<CloneIdentifierDE> reportersNotFound;
 	private Collection<GeneIdentifierDE> genesNotFound;
 	private Collection<SampleIDDE> samplesNotFound;
 	private PrincipalComponentAnalysisRequest pcaRequest;
 	private PrincipalComponentAnalysisFinding pcaFinding;
 	private AnalysisServerClientManager analysisServerClientManager;
 	private BusinessTierCache cacheManager = ApplicationFactory.getBusinessTierCache();
 	
 	public PrincipalComponentAnalysisFindingStrategy(String sessionId, String taskId, PrincipalComponentAnalysisQueryDTO queryDTO) throws ValidationException {
 		//Check if the passed query is valid
 		if(validate(queryDTO)){
 			myQueryDTO = queryDTO;
 			this.sessionId = sessionId;
 			this.taskId = taskId;
 			pcaRequest = new PrincipalComponentAnalysisRequest(this.sessionId,this.taskId);
             try {
                 analysisServerClientManager = AnalysisServerClientManager.getInstance();
             } catch (NamingException e) {               
                 logger.error(new IllegalStateException("Error getting an instance of  AnalysisServerClientManager" ));
                 logger.error(e.getMessage());
                 logger.error(e);
             } catch (JMSException e) {                
                 logger.error(new IllegalStateException("Error getting an instance of  AnalysisServerClientManager" ));
                 logger.error(e.getMessage());
                 logger.error(e);
             }
 		}
 		/*
 		 * Set the Finding into the cache! YOU HAVE TO DO THIS!
 		 */
 		
 		FindingStatus currentStatus = FindingStatus.Running;
 		pcaFinding = new PrincipalComponentAnalysisFinding(sessionId, taskId, currentStatus, null);
 		pcaFinding.setQueryDTO(myQueryDTO);
 		cacheManager.addToSessionCache(sessionId, taskId, pcaFinding);
 	}
 
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.caintegrator.service.findings.strategies.FindingStrategy#createQuery()
 	 * This method validates that 2 groups were passed for TTest and Wincoin Test
 	 */
 	public boolean createQuery() throws FindingsQueryException {
 		boolean flag = true;
 
 		if(myQueryDTO.getComparisonGroups() != null){
             Collection<ClinicalQueryDTO> clinicalQueryDTOs = myQueryDTO.getComparisonGroups();
             clinicalQueries = new ArrayList<ClinicalDataQuery>();
             for(ClinicalQueryDTO clinicalQueryDTO: clinicalQueryDTOs) {
                 clinicalQueries.add((ClinicalDataQuery)clinicalQueryDTO);
             }
 		}
 	    
 		return flag;
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.caintegrator.service.findings.strategies.FindingStrategy#executeQuery()
 	 * this methods queries the database to get back sample Ids for the groups
 	 */
 	public boolean executeQuery() throws FindingsQueryException {
 		//Get Sample Ids from DB
 		if(clinicalQueries != null){
 			CompoundQuery compoundQuery;
             for (ClinicalDataQuery clinicalDataQuery: clinicalQueries){
             Resultant resultant;            
         			try {
         				compoundQuery = new CompoundQuery(clinicalDataQuery);
         				compoundQuery.setAssociatedView(ViewFactory
         		                .newView(ViewType.CLINICAL_VIEW));
         				InstitutionCriteria institutionCriteria = new InstitutionCriteria();
         				institutionCriteria.setInstitutions(myQueryDTO.getInstitutionDEs());
         				compoundQuery.setInstitutionCriteria( institutionCriteria);
         				resultant = ResultsetManager.executeCompoundQuery(compoundQuery);
         	  		}
         	  		catch (Throwable t)	{
         	  			logger.error("Error Executing the query/n"+ t.getMessage());
         	  			throw new FindingsQueryException("Error executing clinical query/n"+t.getMessage());
         	  		}
         
         			if(resultant != null) {      
         		 		ResultsContainer  resultsContainer = resultant.getResultsContainer(); 
         		 		Viewable view = resultant.getAssociatedView();
         		 		if(resultsContainer != null)	{
         		 			if(view instanceof ClinicalSampleView){
         		 				try {
         		 					//1. Get the sample Ids from the return Clinical query
         							Collection<SampleIDDE> sampleIDDEs = StrategyHelper.extractSampleIDDEs(resultsContainer);
         							//2. validate samples so that GE data exsists for these samples
         							Collection<SampleIDDE> validSampleIDDEs = DataValidator.validateSampleIds(sampleIDDEs);
         							//3. Extracts sampleIds as Strings
         							Collection<String> sampleIDs = StrategyHelper.extractSamples(validSampleIDDEs);
         							if(sampleIDs != null && sampleIDs.size()> 0) {
         								//3.1 add them to SampleGroup
         								sampleGroup.addAll(sampleIDs);
             							//3.2 Find out any samples that were not processed  
         								Set<SampleIDDE> set = new HashSet<SampleIDDE>();
         								set.addAll(sampleIDDEs); //samples from the original query
         								//3.3 Remove all samples that are validated								set.removeAll(validSampleIDDEs);
         								samplesNotFound = set;
         								setSamplesNotFound(samplesNotFound);
         							}
         							else{ //No samples validated
         								sampleGroup = null;
         								throw new FindingsQueryException("None of the samples within the selected query are valid for PCA Analysis");
         							}
         						} catch (OperationNotSupportedException e) {
         							logger.error(e.getMessage());
         				  			throw new FindingsQueryException(e.getMessage());
         						} catch (Exception e) {
         							e.printStackTrace();
         							logger.error(e.getMessage());
         				  			throw new FindingsQueryException(e.getMessage());
         						}
         
         	 				}	
         		 		}
                     }
 			}
 		}
 		//Get Reporters from DB
 		if(myQueryDTO.getGeneIdentifierDEs() != null ||
 				myQueryDTO.getReporterIdentifierDEs() != null){
 			if(	myQueryDTO.getReporterIdentifierDEs() != null){
 					Collection<CloneIdentifierDE> validCloneDEs;
 					try {
 						validCloneDEs = DataValidator.validateReporters(myQueryDTO.getReporterIdentifierDEs());
 
 					//Create a set of submitted Reporters 
 					Set<CloneIdentifierDE> set = new HashSet<CloneIdentifierDE>();
 					set.addAll(myQueryDTO.getReporterIdentifierDEs());
 					// Find out if any reports were not validated
 					set.removeAll(validCloneDEs);
 					reportersNotFound = set;
 					
 					Collection<String> reporters = StrategyHelper.extractReporters(validCloneDEs);
 					if(reporters != null  && reporters.size() > 0){
 						this.reporterGroup = new ReporterGroup(myQueryDTO.getQueryName(),reporters.size());
 						reporterGroup.addAll(reporters);
 						
 					}
 					else{ //No reporters are valid
 						reporterGroup = null;
 						throw new FindingsQueryException("None of the selected reporters are valid for PCA Analysis");
 					}
 					} catch (Exception e) {
 						e.printStackTrace();
 						logger.error(e.getMessage());
 			  			throw new FindingsQueryException(e.getMessage());
 					}
 				
 				}
 			if(	myQueryDTO.getGeneIdentifierDEs() != null){
 				Collection<GeneIdentifierDE> validGeneDEs;
 				try {
 					validGeneDEs = DataValidator.validateGenes(myQueryDTO.getGeneIdentifierDEs());
 
 				//Create a set of submitted Reporters 
 				Set<GeneIdentifierDE> set = new HashSet<GeneIdentifierDE>();
 				set.addAll(myQueryDTO.getGeneIdentifierDEs());
 				// Find out if any reports were not validated
 				set.removeAll(validGeneDEs);
 				genesNotFound = set;
 				
 				Collection<String> reporters = StrategyHelper.extractGenes(validGeneDEs);
 				if(reporters != null  && reporters.size() > 0){
 					this.reporterGroup = new ReporterGroup(myQueryDTO.getQueryName(),reporters.size());
 					reporterGroup.addAll(reporters);
 					
 				}
 				else{ //No reporters are valid
 					reporterGroup = null;
 					throw new FindingsQueryException("No reporters founds for the selected genes for PCA Analysis");
 				}
 				} catch (Exception e) {
 					e.printStackTrace();
 					logger.error(e.getMessage());
 		  			throw new FindingsQueryException(e.getMessage());
 				}
 			
 			}
 			}
 		
 
 
 
 
 
 			return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.caintegrator.service.findings.strategies.FindingStrategy#analyzeResultSet()
 	 */
 	public boolean analyzeResultSet() throws FindingsAnalysisException {
 		pcaRequest = new PrincipalComponentAnalysisRequest(sessionId, taskId);
 		pcaRequest.setVarianceFilterValue(myQueryDTO.getGeneVectorPercentileDE().getDecimalValue());
 		pcaRequest.setPlatform(myQueryDTO.getArrayPlatformDE().getValueObjectAsArrayPlatformType());
 		if(reporterGroup != null){
 			pcaRequest.setReporterGroup(reporterGroup);
 		}
 		if(sampleGroup != null){
 			pcaRequest.setSampleGroup(sampleGroup);
 		}
 		else{
 			throw new FindingsAnalysisException("PC Analysis requires a set of samples");
 		}
         try {
         	
         	if (pcaRequest.getPlatform() == ArrayPlatformType.AFFY_OLIGO_PLATFORM) {					 
 		      pcaRequest.setDataFileName(System.getProperty("gov.nih.nci.rembrandt.affy_data_matrix"));
 			}
 			else if (pcaRequest.getPlatform() == ArrayPlatformType.CDNA_ARRAY_PLATFORM) {
 			  pcaRequest.setDataFileName(System.getProperty("gov.nih.nci.rembrandt.cdna_data_matrix"));
 			}
 			else {
 			  logger.warn("Unrecognized array platform type for PCARequest");
			  return false;
 			}
         	
 			analysisServerClientManager.sendRequest(pcaRequest);
 		} catch (JMSException e) {
 			logger.error(e.getMessage());
   			throw new FindingsAnalysisException(e.getMessage());
 		} catch(Exception e){
 			logger.error(e.getMessage());
 			throw new FindingsAnalysisException("Error in setting PrincipalComponentAnalysisRequest object");
 		}
         return true;
 	}
 
 	public Finding getFinding() {
 		return pcaFinding;
 	}
 
 	public boolean validate(QueryDTO queryDTO) throws ValidationException {
 		boolean _valid = false;
 		if(queryDTO instanceof PrincipalComponentAnalysisQueryDTO){
 			_valid = true;
 			PrincipalComponentAnalysisQueryDTO pcaQueryDTO = (PrincipalComponentAnalysisQueryDTO)queryDTO;
 				
 			try {
 						ValidationUtility.checkForNull(pcaQueryDTO.getInstitutionDEs());
 						ValidationUtility.checkForNull(pcaQueryDTO.getArrayPlatformDE()) ;
 						ValidationUtility.checkForNull(pcaQueryDTO.getComparisonGroups());
 						ValidationUtility.checkForNull(pcaQueryDTO.getQueryName());
 					
 					if( pcaQueryDTO.getGeneVectorPercentileDE() == null && 
 							pcaQueryDTO.getGeneIdentifierDEs()== null &&
 							pcaQueryDTO.getReporterIdentifierDEs() == null){
 						throw new ValidationException("PCA query should be contrained by atleast one of the following: GeneVector, Genes or Reporters");
 					}
 				} catch (ValidationException ex) {
 
 					logger.error(ex.getMessage());
 					throw ex;
 				}
 		}
 		return _valid;	
 	}
 	private void setSamplesNotFound(Collection<SampleIDDE>  samplesNotFound ){
 		pcaFinding = (PrincipalComponentAnalysisFinding) cacheManager.getSessionFinding(sessionId, taskId);
 		if(pcaFinding != null){
 			pcaFinding.setSamplesNotFound(samplesNotFound);
 			
 		}
 		cacheManager.addToSessionCache(sessionId, taskId, pcaFinding);
 
 	}
 	private void setReportsNotFound(Collection<CloneIdentifierDE> reportersNotFound){
 		pcaFinding = (PrincipalComponentAnalysisFinding) cacheManager.getSessionFinding(sessionId, taskId);
 		if(pcaFinding != null){
 			pcaFinding.setReportersNotFound(reportersNotFound);
 			
 		}
 		cacheManager.addToSessionCache(sessionId, taskId, pcaFinding);
 
 	}
 	private void setGenesNotFound(Collection<GeneIdentifierDE> genesNotFound ){
 		pcaFinding = (PrincipalComponentAnalysisFinding) cacheManager.getSessionFinding(sessionId, taskId);
 		if(pcaFinding != null){
 			pcaFinding.setGenesNotFound(genesNotFound);
 			
 		}
 		cacheManager.addToSessionCache(sessionId, taskId, pcaFinding);
 
 	}
 }
