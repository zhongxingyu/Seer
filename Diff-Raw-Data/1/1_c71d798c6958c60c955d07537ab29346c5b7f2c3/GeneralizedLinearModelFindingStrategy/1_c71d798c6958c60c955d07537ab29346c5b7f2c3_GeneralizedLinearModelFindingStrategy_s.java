 package gov.nih.nci.eagle.service.strategies;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.jms.JMSException;
 
 import org.apache.log4j.Logger;
 
 
 
 import gov.nih.nci.caintegrator.analysis.messaging.GLMSampleGroup;
 import gov.nih.nci.caintegrator.analysis.messaging.GeneralizedLinearModelRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.SampleGroup;
 import gov.nih.nci.caintegrator.application.analysis.AnalysisServerClientManager;
 import gov.nih.nci.caintegrator.application.cache.BusinessTierCache;
 import gov.nih.nci.caintegrator.application.service.strategy.AsynchronousFindingStrategy;
 import gov.nih.nci.caintegrator.dto.query.ClassComparisonQueryDTO;
 import gov.nih.nci.caintegrator.dto.query.QueryDTO;
 import gov.nih.nci.caintegrator.enumeration.ArrayPlatformType;
 import gov.nih.nci.caintegrator.enumeration.CoVariateType;
 import gov.nih.nci.caintegrator.enumeration.StatisticalMethodType;
 import gov.nih.nci.caintegrator.exceptions.FindingsAnalysisException;
 import gov.nih.nci.caintegrator.exceptions.FindingsQueryException;
 import gov.nih.nci.caintegrator.exceptions.ValidationException;
 import gov.nih.nci.caintegrator.service.findings.Finding;
 import gov.nih.nci.caintegrator.service.findings.GeneralizedLinearModelFinding;
 import gov.nih.nci.caintegrator.service.task.Task;
 import gov.nih.nci.caintegrator.service.task.TaskResult;
 import gov.nih.nci.caintegrator.util.ValidationUtility;
 import gov.nih.nci.eagle.dto.de.CoVariateDE;
 import gov.nih.nci.eagle.enumeration.SpecimenType;
 import gov.nih.nci.eagle.query.dto.ClassComparisonQueryDTOImpl;
 import gov.nih.nci.eagle.util.PatientGroupManager;
 
 public class GeneralizedLinearModelFindingStrategy extends
 		AsynchronousFindingStrategy {
 	
 	 private static Logger logger = Logger.getLogger(GeneralizedLinearModelFindingStrategy.class);
 	 private Collection<SampleGroup> sampleGroups = new ArrayList<SampleGroup>();
 	 private List<CoVariateType> coVariateTypes = new ArrayList<CoVariateType>();
 	 private GeneralizedLinearModelRequest glmRequest = null;
 	 private AnalysisServerClientManager analysisServerClientManager;
 	 private Map<ArrayPlatformType, String> dataFileMap;
 	 
 	 private PatientGroupManager pgm = new PatientGroupManager();
 	
 	 /*
 	     * (non-Javadoc)
 	     * 
 	     * @see gov.nih.nci.caintegrator.service.findings.strategies.FindingStrategy#createQuery()
 	     *      This method validates that 2 groups were passed for generalized linear model as the statistical method
 	     */
 	    public boolean createQuery() throws FindingsQueryException {
 
 	      
 	        // because each layer is valid I am assured I will be getting a fulling
 	        // populated query object
 	        StatisticalMethodType statisticType = getQueryDTO()
 	                .getStatisticTypeDE().getValueObject();
 
 	        if (getQueryDTO().getComparisonGroups() != null) {
 	        	
 	        	if(statisticType==StatisticalMethodType.GLM) {	           
 	                if (getQueryDTO().getComparisonGroups().size() < 1 && getQueryDTO().getBaselineGroupMap().size()>0) {
 	                    throw new FindingsQueryException(
 	                            "Incorrect Number of queries passed for the Generalized Linear Model statistical type");
 	                }	               
 	        	}
 	            return true;
 	        }
 	        return false;
 	    }
 	    
 	    protected void executeStrategy() {			
 			
 	    	glmRequest = new GeneralizedLinearModelRequest(taskResult
 	                .getTask().getCacheId(), taskResult.getTask().getId());
 			businessCacheManager.addToSessionCache(getTaskResult().getTask()
 	                .getCacheId(), getTaskResult().getTask().getId(),
 	                 getTaskResult());
 		}
 		
 	    
 	    /*
 		 * 
 		 */
 		public boolean analyzeResultSet() throws  FindingsAnalysisException {
 			StatisticalMethodType statisticType = getQueryDTO().getStatisticTypeDE().getValueObject();
 			
 			try {
 			    if(statisticType==StatisticalMethodType.GLM) {	
 			    	
 			    	 // set statistical method
 	                glmRequest.setStatisticalMethod(statisticType);
 			    	
 			    	 // Set sample groups
 	                HashMap<String, List> comparisonGroupsMap = getQueryDTO().getComparisonGroupsMap();
 	                HashMap<String, List> baselineGroupMap = getQueryDTO().getBaselineGroupMap();
 	                GLMSampleGroup baseline  = null;
 	                for(String gname : baselineGroupMap.keySet()) {
 	                	List<String> groups = baselineGroupMap.get(gname);
 	                	 baseline = new GLMSampleGroup(gname);
 	                	 baseline.addAll(baselineGroupMap.get(gname));
 	                	 
 	                	for(String name : groups )	{
 		                   
 		                   	
 		                    //add each patient
 		                    
 		                    HashMap<String, String> annotationMap = new HashMap<String, String>();
 		                    //fetch the data about each patient
 		                    Map pm = pgm.getPatientInfo(name);
 		                    annotationMap.put("sex", pm.get("sex").toString());
 		                    annotationMap.put("age", pm.get("age").toString());
 		                    annotationMap.put("smoking_status", pm.get("smoking_status").toString());
 	
 		                    baseline.addPatientData(name, annotationMap);
 	                	}
 	                }
                     glmRequest.setBaselineGroup(baseline);
 
 	                
 	                List<GLMSampleGroup> glmsgs = new ArrayList<GLMSampleGroup>();
 	                for(String gname : comparisonGroupsMap.keySet()) {
 	                	List<String> groups = comparisonGroupsMap.get(gname);
 	                	GLMSampleGroup comparison = new GLMSampleGroup(gname);
 	                	comparison.addAll(comparisonGroupsMap.get(gname));
 	                	for(String name : groups )	{
 		                    
 		                    HashMap<String, String> annotationMap = new HashMap<String, String>();
 		                    Map pm = pgm.getPatientInfo(name);
 		                    annotationMap.put("sex", pm.get("sex").toString());
 		                    annotationMap.put("age", pm.get("age").toString());
 		                    annotationMap.put("smoking_status", pm.get("smoking_status").toString());
 		                    comparison.addPatientData(name, annotationMap);
 	                	}
 	                }
 	                
                     glmRequest.setComparisonGroups(glmsgs);
 	               
 	                
 				 // set Co-variates
 	                
 	                List<CoVariateDE> coVariateDEs = getQueryDTO().getCoVariateDEs();
 	                Object[] obj = coVariateDEs.toArray();
 	                for(int i=0; i<obj.length;i++) {
 	                	CoVariateDE coVariateDE = (CoVariateDE)obj[i];
 	                	coVariateTypes.add(coVariateDE.getValueObject());	                	
 	                }
 	                glmRequest.setCoVariateTypes(coVariateTypes);              
 				   
 				   
 				
 				    // set Multiple Comparison Adjustment type
 	                glmRequest.setMultiGrpComparisonAdjType(getQueryDTO().getMultiGroupComparisonAdjustmentTypeDE().getValueObject());
 				
 				    // set foldChange
 	                glmRequest.setFoldChangeThreshold(getQueryDTO().getExprFoldChangeDE().getValueObject());
 				
 				    // set pvalue
 	                glmRequest.setPValueThreshold(getQueryDTO().getStatisticalSignificanceDE().getValueObject());
 				
 				    // set arrayplat form, come back to this to figure out how to pass the platform
 	               // glmRequest.setArrayPlatform(getQueryDTO().getArrayPlatformDE().getValueObjectAsArrayPlatformType());
 				
 				    // go the correct matrix to fetch data			
 				
 	                glmRequest.setDataFileName(dataFileMap.get(getQueryDTO().getSpecimenTypeEnum().name()));
 			      
 			
 			   analysisServerClientManager.sendRequest(glmRequest);
 			   return true;	
 			    }
 			}// end of try
 			catch(JMSException ex) {
 				logger.error(ex.getMessage());
 	  			throw new FindingsAnalysisException(ex.getMessage());
 			}
 			catch(Exception ex) {
 				logger.error("erro in glm", ex);
 				throw new FindingsAnalysisException("Error in setting glmRequest object");
 			}
 			return false;
 		}
 		
 
 		public Finding getFinding() {
 	        return (GeneralizedLinearModelFinding) taskResult;
 	    }
 		
 		 public boolean validate(QueryDTO queryDTO) throws ValidationException {
 		        boolean _valid = false;
 		        if (queryDTO instanceof ClassComparisonQueryDTO) {
 		            ClassComparisonQueryDTO classComparisonQueryDTO = (ClassComparisonQueryDTO) queryDTO;
 		            try {
 		                
 		                ValidationUtility.checkForNull(classComparisonQueryDTO
 		                        .getArrayPlatformDE());
 		                ValidationUtility.checkForNull(classComparisonQueryDTO
 		                        .getComparisonGroups());
 		                ValidationUtility.checkForNull(classComparisonQueryDTO
 		                        .getExprFoldChangeDE());
 		                ValidationUtility.checkForNull(classComparisonQueryDTO
 		                        .getMultiGroupComparisonAdjustmentTypeDE());
 		                ValidationUtility.checkForNull(classComparisonQueryDTO
 		                        .getQueryName());
 		                ValidationUtility.checkForNull(classComparisonQueryDTO
 		                        .getStatisticalSignificanceDE());
 		                ValidationUtility.checkForNull(classComparisonQueryDTO
 		                        .getStatisticTypeDE());	                          
 		                
 		                _valid = true;
 		            } catch (ValidationException ex) {
 		                logger.error(ex.getMessage());
 		                throw ex;
 		            }
 		        }
 		        return _valid;
 		    }
 
 	    private ClassComparisonQueryDTOImpl getQueryDTO() {
 	        return (ClassComparisonQueryDTOImpl) taskResult.getTask().getQueryDTO();
 	    }
     
 	    public TaskResult retrieveTaskResult(Task task) {
 	        TaskResult taskResult = (TaskResult) businessCacheManager
 	                .getObjectFromSessionCache(task.getCacheId(), task.getId());
 	        return taskResult;
 	    }
 	    
 	    public boolean canHandle(QueryDTO query) {
 	        if(query instanceof ClassComparisonQueryDTO) {
 	            ClassComparisonQueryDTO dto = (ClassComparisonQueryDTO)query;
 	            return ( dto.getStatisticTypeDE().getValueObject().equals(StatisticalMethodType.GLM));
 	        }
 	        return false;
 	    }
 	    
 	    public AnalysisServerClientManager getAnalysisServerClientManager() {
 	        return analysisServerClientManager;
 	    }
 	    
 	    public void setAnalysisServerClientManager(
 	            AnalysisServerClientManager analysisServerClientManager) {
 	        this.analysisServerClientManager = analysisServerClientManager;
 	    }
 	    
 	    public BusinessTierCache getBusinessCacheManager() {
 	        return businessCacheManager;
 	    }
 	    
 	    public void setBusinessCacheManager(BusinessTierCache cacheManager) {
 	        this.businessCacheManager = cacheManager;
 	    }
 	    
 	    public Map getDataFileMap() {
 	        return dataFileMap;
 	    }
 
 	    public void setDataFileMap(Map dataFileMap) {
 	        this.dataFileMap = dataFileMap;
 	    }
 
 }
