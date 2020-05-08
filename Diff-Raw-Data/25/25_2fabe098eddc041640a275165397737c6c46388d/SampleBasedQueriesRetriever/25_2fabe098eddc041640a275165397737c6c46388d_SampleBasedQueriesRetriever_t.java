 /**
  * 
  */
 package gov.nih.nci.rembrandt.web.helper;
 
 import gov.nih.nci.caintegrator.dto.critieria.DiseaseOrGradeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SampleCriteria;
 import gov.nih.nci.caintegrator.dto.de.DiseaseNameDE;
 import gov.nih.nci.caintegrator.dto.query.QueryType;
 import gov.nih.nci.rembrandt.cache.PresentationTierCache;
 import gov.nih.nci.rembrandt.dto.lookup.DiseaseTypeLookup;
 import gov.nih.nci.rembrandt.dto.lookup.LookupManager;
 import gov.nih.nci.rembrandt.dto.query.ClinicalDataQuery;
 import gov.nih.nci.rembrandt.dto.query.CompoundQuery;
 import gov.nih.nci.rembrandt.dto.query.Queriable;
 import gov.nih.nci.rembrandt.dto.query.Query;
 import gov.nih.nci.rembrandt.queryservice.QueryManager;
import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.web.bean.SessionCriteriaBag;
 import gov.nih.nci.rembrandt.web.bean.SessionCriteriaBag.ListType;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 
 import java.io.Serializable;
 import java.util.ArrayList;
import java.util.Collection;
 import java.util.List;
 import java.util.Map;
import java.util.Properties;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.util.LabelValueBean;
 
 /**
  * @author sahnih
  *
  */
 public class SampleBasedQueriesRetriever implements Serializable {
 
 	/**
 	 * 
 	 */
     private static Logger logger = Logger.getLogger(SampleBasedQueriesRetriever.class);
     private PresentationTierCache cacheManager = ApplicationFactory.getPresentationTierCache();
 	private static final long serialVersionUID = 1L;
 	@SuppressWarnings("unchecked")
 	private Map<String,ClinicalDataQuery> predefinedQueryMap = new TreeMap();
 	private List allPredefinedAndSampleSetNames;
     private LabelValueBean lvb;
     private SessionCriteriaBag sessionCriteriaBag;
     
 	/**
 	 *  Constructor creates the PredefinedQueries Disease Queries
 	 */
 	public SampleBasedQueriesRetriever() {
 		createPredefinedDiseaseQueries();
 	}
 	private void createPredefinedDiseaseQueries(){		
 		try {
 			DiseaseTypeLookup[] myDiseaseTypes =LookupManager.getDiseaseType();
 			if(myDiseaseTypes != null){
 				for (DiseaseTypeLookup diseaseTypeLookup : myDiseaseTypes){
 					DiseaseOrGradeCriteria diseaseCrit = new DiseaseOrGradeCriteria();
 			        diseaseCrit.setDisease(new DiseaseNameDE(diseaseTypeLookup.getDiseaseType()));
 			        ClinicalDataQuery clinicalDataQuery = (ClinicalDataQuery) QueryManager.createQuery(QueryType.CLINICAL_DATA_QUERY_TYPE);
 			        clinicalDataQuery.setQueryName(diseaseTypeLookup.getDiseaseDesc());
 			        clinicalDataQuery.setDiseaseOrGradeCrit(diseaseCrit);
 			        predefinedQueryMap.put(diseaseTypeLookup.getDiseaseDesc(),clinicalDataQuery);
 				}
				//Logic to handle All Disease!
		        
				ClinicalDataQuery allDiseaseQuery = predefinedQueryMap.get(RembrandtConstants.ALL_GLIOMA);
				if(allDiseaseQuery != null){
				Collection<DiseaseNameDE> allDiseases = new ArrayList<DiseaseNameDE>();
					for (DiseaseTypeLookup diseaseTypeLookup : myDiseaseTypes){
						if((!(diseaseTypeLookup.getDiseaseType().compareToIgnoreCase(RembrandtConstants.NON_TUMOR)== 0) &&
								!(diseaseTypeLookup.getDiseaseType().compareToIgnoreCase(RembrandtConstants.UNKNOWN) == 0) &&
								!(diseaseTypeLookup.getDiseaseType().compareToIgnoreCase(RembrandtConstants.ALL )== 0))){
							allDiseases.add(new DiseaseNameDE(diseaseTypeLookup.getDiseaseType()));		
						}
						
					}
						DiseaseOrGradeCriteria diseaseCrit = new DiseaseOrGradeCriteria();
				        diseaseCrit.setDiseases(allDiseases);	
				        allDiseaseQuery.setDiseaseOrGradeCrit(diseaseCrit);
				        predefinedQueryMap.put(RembrandtConstants.ALL_GLIOMA,allDiseaseQuery);
				}

 			}
 		} catch (Exception e) {
 			logger.error("Error in SampleSetBag when calling createPredefinedDiseaseQueries method");
 			logger.error(e.getMessage());
 		}
 	}
 	@SuppressWarnings("unchecked")
 	/**
 	 * This is a list of defined Disease query names. Typically used in Class Comparision Analysis queries  
 	 * 
 	 */
 	public List getAllPredefinedDiseaseQueryNames(){
 		return new ArrayList( predefinedQueryMap.keySet());
 	}
     
        
 	/**
 	 * This is a list of samples selected from a Resultset.
 	 * It is used to create a SampleCriteria that is placed in a ClinicalDataQuery that
 	 * has a ClinicalView.  This gives us the ability to extract or look at these
 	 * "Samples of Interest" at any time, and applying them to other queries if
 	 * the user desires.    
 	 * 
 	 * @param sessionId --identifies the sessionCache that you want a complete
 	 * list of SampleSetNames stored in.
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> getAllSampleSetNames(String sessionID){
 		List<String> sampleSetList = new ArrayList<String>(); 
 		sampleSetList.addAll(cacheManager.getSampleSetNames(sessionID));
 		SessionCriteriaBag mySessionCriteriaBag = cacheManager.getSessionCriteriaBag(sessionID);
 		if(mySessionCriteriaBag != null){
 			
 			sampleSetList.addAll(mySessionCriteriaBag.getUsetListNames(SessionCriteriaBag.ListType.SampleIdentifierSet));
 		}
 		return  sampleSetList;
 	}
     
     /**This is the list of names of all the predefined queries and sample sets
      * that are in the session. There is a list of defined Disease query names that are
      * retrieved as well as samples the user has selected and saved. After all these 
      * names have been retieved, they are stored as LabelValueBeans in an ArrayList.
      * 
      * NOTE: may want only call predefined queries once. 
      * 
      @param sessionId --identifies the sessionCache that you want a complete
      * list of SampleSetNames stored in. -KR
      */
     public List getAllPredefinedAndSampleSetNames(String sessionID){
         sessionCriteriaBag = cacheManager.getSessionCriteriaBag(sessionID);
         
          List predefined = new ArrayList(getAllPredefinedDiseaseQueryNames());
          List sampleSet = new ArrayList(getAllSampleSetNames(sessionID));
          /*
           * TODO:Change --- For now...we are using the "old way" of retrieving sampleSet names by calling
           * the method getAllSampleSetNames(String) above. In the near future,
           * sampleSets (lists) will be saved as a "type" and stored as a userList like so: 
           * List sampleSet = new ArrayList(sessionCriteriaBag.getUsetListNames(ListType.SampleIdentifierSet));
           */
                    allPredefinedAndSampleSetNames = new ArrayList();
                        for(int i =0; i < predefined.size(); i++){
                            lvb = new LabelValueBean((String)predefined.get(i),(String)predefined.get(i));
                            allPredefinedAndSampleSetNames.add(lvb);
                        }
                        for(int i =0; i < sampleSet.size(); i++){
                            lvb = new LabelValueBean((String)sampleSet.get(i),(String)sampleSet.get(i));
                            allPredefinedAndSampleSetNames.add(lvb);
                        }
         
         return allPredefinedAndSampleSetNames;
         }
     
 	/**
 	 * This method, when given a sessionId and a queryName, will check the 
 	 * session cache for the stored ClinicalDataQuery.  If it is not stored or 
 	 * causes an exception it will return Null.  If you are getting Null values,
 	 * when you know that you have stored the query in the sessionCache, check
 	 * the log files as any exceptions will be written there.
 	 *   
 	 * @param sessionId --the session that should have the query stored
 	 * @param queryName --the query that is desired
 	 * @return ClinicalDataQuery
 	 */
 	public ClinicalDataQuery getQuery(String sessionID, String queryName){
 		if( sessionID != null &&  queryName != null){
             sessionCriteriaBag = cacheManager.getSessionCriteriaBag(sessionID);
             
 			if(predefinedQueryMap.containsKey(queryName)){
 				return predefinedQueryMap.get(queryName);
 			}
 			else if(sessionCriteriaBag.getUserList(ListType.SampleIdentifierSet,queryName)!= null){
                 ClinicalDataQuery clinicalDataQuery = (ClinicalDataQuery) QueryManager.createQuery(QueryType.CLINICAL_DATA_QUERY_TYPE);
 		        clinicalDataQuery.setQueryName(queryName);
 		        clinicalDataQuery.setSampleIDCrit(sessionCriteriaBag.getSampleCriteria(queryName));
 
 				return clinicalDataQuery;
 			}
 			else {
 				/*
 				 * This is where we will retrieve a result set of sample ids that was created 
 				 * when the report waas saved.
 
 				 */
 				CompoundQuery resultSetCompoundQuery = (CompoundQuery) cacheManager.getQuery(sessionID, queryName);
 	        	/*
 	    		 * At this time there is only a single query in any result set.
 	    		 * So let me grab that single query out of the compound query 
 	    		 * and extract it's sampleCriteria to apply to the compoundQuery
 	    		 */
 		    		Queriable query1 = resultSetCompoundQuery.getAssociatiedQueries()[0];
 		    		SampleCriteria sampleCrit = null;
 		    		if(query1 instanceof Query){
 		    			sampleCrit = ((Query)query1).getSampleIDCrit();
 				        ClinicalDataQuery clinicalDataQuery = (ClinicalDataQuery) QueryManager.createQuery(QueryType.CLINICAL_DATA_QUERY_TYPE);
 				        clinicalDataQuery.setQueryName(queryName);
 				        clinicalDataQuery.setSampleIDCrit(sampleCrit);
 
 						return clinicalDataQuery;
 		    		}
 
 				}
 		}
 		return null;
 	}
 }
