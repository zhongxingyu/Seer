 package gov.nih.nci.eagle.query.dto;
 
 import gov.nih.nci.caintegrator.application.dtobuilder.QueryDTOBuilder;
 import gov.nih.nci.caintegrator.dto.query.QueryDTO;
 import gov.nih.nci.caintegrator.enumeration.CoVariateType;
 import gov.nih.nci.caintegrator.enumeration.MultiGroupComparisonAdjustmentType;
 import gov.nih.nci.caintegrator.enumeration.Operator;
 import gov.nih.nci.caintegrator.enumeration.StatisticalMethodType;
 import gov.nih.nci.caintegrator.enumeration.StatisticalSignificanceType;
 import gov.nih.nci.caintegrator.application.lists.UserListBeanHelper;
 import gov.nih.nci.caintegrator.dto.de.ArrayPlatformDE;
 import gov.nih.nci.caintegrator.dto.de.MultiGroupComparisonAdjustmentTypeDE;
 import gov.nih.nci.caintegrator.dto.de.StatisticTypeDE;
 import gov.nih.nci.caintegrator.dto.de.StatisticalSignificanceDE;
 import gov.nih.nci.caintegrator.dto.de.ExprFoldChangeDE.UpRegulation;
 import gov.nih.nci.caintegrator.dto.query.ClinicalQueryDTO;
 import gov.nih.nci.caintegrator.dto.query.QueryDTO;
 import gov.nih.nci.caintegrator.enumeration.CoVariateType;
 import gov.nih.nci.caintegrator.enumeration.MultiGroupComparisonAdjustmentType;
 import gov.nih.nci.caintegrator.enumeration.Operator;
 import gov.nih.nci.caintegrator.enumeration.StatisticalMethodType;
 import gov.nih.nci.caintegrator.enumeration.StatisticalSignificanceType;
 import gov.nih.nci.eagle.dto.de.CoVariateDE;
 import gov.nih.nci.eagle.enumeration.SpecimenType;
 import gov.nih.nci.eagle.service.validation.ListValidationService;
 import gov.nih.nci.eagle.web.helper.EnumCaseChecker;
 import gov.nih.nci.eagle.web.struts.ClassComparisonForm;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 
 
 /**
  * ClassComparisonQueryDTOBuilder builds the ClassComparisonQueryDTO to be used
  * by the ClassComparisonStrategy. It is called from the struts 
  * action and uses all values populated in the UI form.
  * @author zhangd
  *
  */
 
 public class ClassComparisonQueryDTOBuilder implements QueryDTOBuilder{
 	
 	private static Logger logger = Logger.getLogger(ClassComparisonQueryDTOBuilder.class);
     private ListValidationService listValidationService;
 
 	public ClassComparisonQueryDTOBuilder() {}
 	
 	
 	  /***
      * These are the default error values used when an invalid enum type
      * parameter has been passed to the action.  These default values should
      * be verified as useful in all cases.
      */
      private StatisticalMethodType ERROR_STATISTICAL_METHOD_TYPE = StatisticalMethodType.TTest;
      private CoVariateType ERROR_COVARIATE_TYPE = CoVariateType.Age;
      
     public QueryDTO buildQueryDTO(Object form){
     	//dont use
     	throw(new UnsupportedOperationException());
     }
      
 	// in the QueryDTOBuilder interface, there is only one method : QueryDTO buildQueryDTO(Object form){}
 	public QueryDTO buildQueryDTO(Object form, String cacheId) {
 		
 		ClassComparisonForm classComparisonForm = (ClassComparisonForm) form;
 		ClassComparisonQueryDTOImpl  classComparisondto = new ClassComparisonQueryDTOImpl();
 		
 		//set taskId/analysis name
        if(classComparisonForm.getAnalysisName().trim().length()>0){
         	classComparisondto.setQueryName(classComparisonForm.getAnalysisName());
         }
         
         
        //set statistical method
         if(classComparisonForm.getStatisticalMethod().trim().length()>1){
         	/*
              * This following code is here to deal with an observed problem with the changing 
              * of case in request parameters.  See the class EnumChecker for 
              * enlightenment.
              */
  		   
  		   StatisticalMethodType statisticalMethodType; 
  		   String statisticalMethodTypeString= EnumCaseChecker.getEnumTypeName(classComparisonForm.getStatisticalMethod(),StatisticalMethodType.values());
             if(statisticalMethodTypeString!=null) {
            	 statisticalMethodType = StatisticalMethodType.valueOf(statisticalMethodTypeString);
             }
             
             else {
          	   	logger.error("Invalid StatisticalMethodType value given in request");
         		logger.error("Selected StatisticalMethodType value = "+classComparisonForm.getStatisticalMethod());
         		logger.error("Using the default StatisticalMethodType type of :"+ERROR_STATISTICAL_METHOD_TYPE);
         		statisticalMethodType = ERROR_STATISTICAL_METHOD_TYPE;            
                 }
             
            StatisticTypeDE statisticTypeDE = new StatisticTypeDE(statisticalMethodType);
            classComparisondto.setStatisticTypeDE(statisticTypeDE);
        
  	        }
         
         // set up MultiGroupComparisonAdjustmentType
         classComparisondto.setMultiGroupComparisonAdjustmentTypeDE(new MultiGroupComparisonAdjustmentTypeDE(MultiGroupComparisonAdjustmentType.NONE));
         
         
         
         // set up co-variates
         
         if(classComparisonForm.getSelectedCovariates() != null && classComparisonForm.getSelectedCovariates().length >=1) {
         	
         	   CoVariateType coVariateType ;     	   
         	   List <CoVariateDE> coVariateDEs = new ArrayList<CoVariateDE>();       	   
         	   
         	   for(int i=0; i<classComparisonForm.getSelectedCovariates().length; i++){	
         		   String myCovariateName = (String)classComparisonForm.getSelectedCovariates()[i];
         		   String covariateString= EnumCaseChecker.getEnumTypeName(myCovariateName,CoVariateType.values());
         		   if(covariateString!=null) {
                    	   coVariateType = CoVariateType.valueOf(covariateString);
                    }
         		   else {
                 	logger.error("Invalid covariateType value given in request");
                		logger.error("Selected covariateType value = "+classComparisonForm.getExistingCovariates());
                		logger.error("Using the default covariateType type of :"+ERROR_COVARIATE_TYPE);
                		coVariateType = ERROR_COVARIATE_TYPE;            
                        }   
         	          
         		   CoVariateDE coVariateDE = new CoVariateDE(coVariateType);
         		   coVariateDEs.add(coVariateDE);       		   
         		   
         	   }
         	
         	   classComparisondto.setCoVariateDEs(coVariateDEs);
         
          }
     		
         
        //look at the classComparisonForm.getSpecimenType() and then reconstruct the enum, then setSpecimenType(enum) in DTO
        if(classComparisonForm.getSpecimenType()!= "" || classComparisonForm.getSpecimenType().length() != 0){       
             classComparisondto.setSpecimenTypeEnum(SpecimenType.valueOf(classComparisonForm.getSpecimenType()));
         }
      
         
 	 
 	 // set comparison groups
        UserListBeanHelper ulbh = new UserListBeanHelper(cacheId);
     
 	   if(classComparisonForm.getSelectedGroups() != null && classComparisonForm.getSelectedGroups().length >=1) {
 		   	   
 		   HashMap<String, List> compGroups = new HashMap();
 		   
 		   for(int i=0; i<classComparisonForm.getSelectedGroups().length; i++){			   
 			  String myGroupName = classComparisonForm.getSelectedGroups()[i];
 			  List<String> myGroupValues = new ArrayList();
 			  myGroupValues = ulbh.getItemsFromList(myGroupName);
               List<String> validList = listValidationService.validateList(myGroupValues, classComparisondto.getSpecimenTypeEnum());
 			  compGroups.put(myGroupName, validList);
 		   }
 		   
 		   classComparisondto.setComparisonGroupsMap(compGroups);
 	   }
         
 	   //set the baseline group in a similar manner as above
 	   if(classComparisonForm.getBaseline()!=null){
 		   HashMap baselineMap = new HashMap();
 		   List<String> baselineValues = ulbh.getItemsFromList(classComparisonForm.getBaseline());
            List<String> validList = listValidationService.validateList(baselineValues, classComparisondto.getSpecimenTypeEnum());
 		   baselineMap.put(classComparisonForm.getBaseline(), validList);
 		   classComparisondto.setBaselineGroupMap(baselineMap);
 	   }
 	   //set up fold change
 	   
 	   if(classComparisonForm.getFoldChange()!= null && classComparisonForm.getFoldChange()!= "") {
 		   UpRegulation exprFoldChangeDE = new UpRegulation(new Float(classComparisonForm.getFoldChange()));           
 		   classComparisondto.setExprFoldChangeDE(exprFoldChangeDE);
 	   }
        
 	   // set up p value
 	   
 	   if(classComparisonForm.getPvalue()!= null && classComparisonForm.getPvalue()!="") {		   
 		   StatisticalSignificanceDE statisticalSignificanceDE = new StatisticalSignificanceDE(new Double(classComparisonForm.getPvalue()),Operator.LE,StatisticalSignificanceType.pValue);  
 		   classComparisondto.setStatisticalSignificanceDE(statisticalSignificanceDE);	   
 	   }
 	   
 	   // set up platform
 	   /*
 	   if(classComparisonForm.getPlatform()!= "" || classComparisonForm.getPlatform().length() != 0){       
            ArrayPlatformDE arrayPlatformDE = new ArrayPlatformDE(classComparisonForm.getPlatform());
            //classComparisondto.setArrayPlatformDE(arrayPlatformDE);
            classComparisondto.setArrayPlatformDE(new ArrayPlatformDE("ALL_PLATFORM"));
            
        }
        */
 	   // Set array platform so that the reporters can be annotated for the report.
        classComparisondto.setArrayPlatformDE(new ArrayPlatformDE("ALL_PLATFORM"));
 	   
 	   return classComparisondto;
 		
 	}
 
     public ListValidationService getListValidationService() {
         return listValidationService;
     }
 
     public void setListValidationService(ListValidationService listValidationService) {
         this.listValidationService = listValidationService;
     }	
 
 }
