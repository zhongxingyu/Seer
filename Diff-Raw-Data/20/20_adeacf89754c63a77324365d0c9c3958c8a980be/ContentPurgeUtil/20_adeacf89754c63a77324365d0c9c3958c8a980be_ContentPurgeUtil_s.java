 package com.weather.percussion.content;
 
 import java.text.DateFormat;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import javax.jcr.Property;
 import javax.jcr.PropertyIterator;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryResult;
 import javax.jcr.query.Row;
 import javax.jcr.query.RowIterator;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.percussion.cms.objectstore.PSComponentSummary;
 import com.percussion.design.objectstore.PSLocator;
 import com.percussion.server.PSRequest;
 import com.percussion.server.PSRequestContext;
 import com.percussion.services.content.data.PSItemStatus;
 import com.percussion.services.contentmgr.IPSContentMgr;
 import com.percussion.services.contentmgr.PSContentMgrLocator;
 import com.percussion.services.contentmgr.data.PSContentNode;
 import com.percussion.services.guidmgr.IPSGuidManager;
 import com.percussion.services.guidmgr.PSGuidManagerLocator;
 import com.percussion.services.legacy.IPSCmsContentSummaries;
 import com.percussion.services.legacy.PSCmsContentSummariesLocator;
 import com.percussion.utils.guid.IPSGuid;
 import com.percussion.utils.request.PSRequestInfo;
 import com.percussion.webservices.content.IPSContentWs;
 import com.percussion.webservices.content.PSContentWsLocator;
 import com.weather.percussion.extensions.schedule.TWCPurgeContentTask;
 
 public class ContentPurgeUtil {
     private List<String> userIds; //Optional Field
     private List<String> folderPaths; //Required Field
     private List<String> contentTypes; //Optional Field
     private List<Integer> contentStateIds; //Optional Field
     private Calendar dateFrom; //Required Field
     protected IPSContentWs contentWebService;
     private IPSContentMgr contentManager;
     private IPSCmsContentSummaries contentSummariesService;
     private IPSGuidManager guidManager;
     
     private static final Log log = LogFactory.getLog(com.weather.percussion.content.ContentPurgeUtil.class);
     
     private static final String COMMUNITY_ID_FIELD = "rx:sys_communityid";
     
     public ContentPurgeUtil(Map<String, String> params) throws IllegalArgumentException {
         setUserIds(params);
         setFolderPaths(params);
         setContentTypes(params);
         setDateFrom(params);
         setContentStateIds(params);
         contentWebService = PSContentWsLocator.getContentWebservice();
         contentManager = PSContentMgrLocator.getContentMgr();
         contentSummariesService = PSCmsContentSummariesLocator.getObjectManager();
         guidManager = PSGuidManagerLocator.getGuidMgr();
     }
     
     public void purge() throws Exception {
         log.debug("ContentPurgeUtil.purge()");
         List<IPSGuid> guidsToPurge = getGuidsToPurge();
         purgeGuids(guidsToPurge);        
     }
     
     private void purgeGuids(List<IPSGuid> guidList) {
         boolean hasErrors = false;
         /**
          * Iterate through to call 1 by 1 so can continue processing even if individual file errors out.
          * Calling purge on whole list will stop processing on error.  
          */
         for (IPSGuid guid : guidList) {
             try {
                 log.debug("Purging item: " + guid.getUUID());
                 prepareGuidForEdit(guid);
                 purgeGuid(guid);
             }
             catch(Exception e) {
                 hasErrors = true;
                 log.debug(new StringBuilder("Error purging content: ").append(guid.getUUID()));
                 log.debug("Exception", e);
             }
         }
         
         if (hasErrors) {
             log.error("Errors found purging some items.  Please enable debug logging to see individual items.  All items being purged must be checked in");
         }
     }
     
     private void prepareGuidForEdit(IPSGuid guidToEdit) throws Exception {
         List<IPSGuid> singleGuidList = new ArrayList<IPSGuid>();
         singleGuidList.add(guidToEdit);
         PSContentNode contentNode = getNodeFromGuid(guidToEdit);
         initRequestInfo(contentNode.getProperty(COMMUNITY_ID_FIELD).getString());
         List<PSItemStatus> itemStatusList = contentWebService.prepareForEdit(singleGuidList);        
     }
     
     private void purgeGuid(IPSGuid guidToPurge) throws Exception {
         List<IPSGuid> singleGuidList = new ArrayList<IPSGuid>();
         singleGuidList.add(getEditGuid(guidToPurge.getUUID()));
         contentWebService.deleteItems(singleGuidList);        
     }
     
     private void setUserIds(Map<String, String> params) {
         String userIdsParam = params.get(TWCPurgeContentTask.USER_IDS_PARAM);
         if (!StringUtils.isBlank(userIdsParam)) {
             userIds = Arrays.asList(userIdsParam.split("\\s*,\\s*"));
         }
         else {
             userIds = new ArrayList<String>();
         }
     }
     
     private void setFolderPaths(Map<String, String> params) throws IllegalArgumentException {
         String folderPathsParam = params.get(TWCPurgeContentTask.FOLDER_PATHS_PARAM);
         if (StringUtils.isBlank(folderPathsParam)) {
             throw new IllegalArgumentException(
                     new StringBuilder(TWCPurgeContentTask.FOLDER_PATHS_PARAM).append(
                             " is a required field").toString());            
         }
         folderPaths = Arrays.asList(folderPathsParam.split("\\s*,\\s*"));
     }
     
     private void setContentTypes(Map<String, String> params) {
         String contentTypesParam = params.get(TWCPurgeContentTask.CONTENT_TYPES_PARAM);
         if (!StringUtils.isBlank(contentTypesParam)) {
             contentTypes = Arrays.asList(contentTypesParam.split("\\s*,\\s*"));
         }
         else {
             contentTypes = new ArrayList<String>();
         }
     }
     
     private void setContentStateIds(Map<String, String> params) throws IllegalArgumentException {
         String contentStateIdsParam = params.get(TWCPurgeContentTask.CONTENT_STATE_IDS_PARAM);
         contentStateIds = new ArrayList<Integer>();
         if (!StringUtils.isBlank(contentStateIdsParam)) {
             List<String> contentStateIdsAsStrings = Arrays.asList(contentStateIdsParam.split("\\s*,\\s*"));
             try {
                 for (String stateIdAsString : contentStateIdsAsStrings) {
                     contentStateIds.add(Integer.parseInt(stateIdAsString));
                 }
             }
             catch(NumberFormatException nfe) {
                 throw new IllegalArgumentException(
                         new StringBuilder(TWCPurgeContentTask.CONTENT_STATE_IDS_PARAM).append(
                                 " must contain valid integers in a comma separated list").toString());
             }
             
         }
     }
     
     private void setDateFrom(Map<String, String> params) throws IllegalArgumentException {
         String numberOfDaysParam = params.get(TWCPurgeContentTask.NUMBER_OF_DAYS);
         if (StringUtils.isBlank(numberOfDaysParam)) {
             throw new IllegalArgumentException(
                     new StringBuilder(TWCPurgeContentTask.NUMBER_OF_DAYS).append(
                             " is a required field").toString());
         }
         int daysInThePast = 0;
         try {
             daysInThePast = Integer.parseInt(numberOfDaysParam);
         }
         catch(NumberFormatException nfe) {
             throw new IllegalArgumentException(
             new StringBuilder(TWCPurgeContentTask.NUMBER_OF_DAYS).append(
                     " must be a valid number").toString());            
         }
         dateFrom = Calendar.getInstance();
         dateFrom.add(Calendar.DAY_OF_MONTH, -daysInThePast);                
     }
     
     private List<IPSGuid> getGuidsToPurge() throws Exception {
         String queryString = buildContentSearchJcrQuery();
         log.debug("Query: " + queryString);        
         Query jcrQuery = contentManager.createQuery(queryString, "sql");
         QueryResult results = contentManager.executeQuery(jcrQuery, -1, null, null);
         List<IPSGuid> retVal = processContentQueryResults(results);
         retVal = filterGuidsBasedOnContentStates(retVal);
         return retVal;
     }
     
     private List<IPSGuid> processContentQueryResults(QueryResult queryResult) {
         List<IPSGuid> retVal = new ArrayList<IPSGuid>();
         if (queryResult == null) {
             return retVal;
         }
         try {
             RowIterator rowIterator = queryResult.getRows();
             while (rowIterator.hasNext()) {
                 Row row = rowIterator.nextRow();
                 String contentIdString = row.getValue("rx:sys_contentid").getString();
                 retVal.add(getCurrentGuid(Integer.parseInt(contentIdString)));
             }
         }
         catch(Exception e) {
             log.error("Error attempting to process results of query for items to purge", e);
         }
         return retVal;
     }
     
     private List<IPSGuid> filterGuidsBasedOnContentStates(List<IPSGuid> guidListToFilter) throws Exception {
         List<IPSGuid> filteredGuidList = new ArrayList<IPSGuid>();
         for (IPSGuid guid : guidListToFilter) {
             PSContentNode contentNode = getNodeFromGuid(guid);
             if (contentStateIds.contains(Integer.valueOf(contentNode.getSummary().getContentStateId()))) {
                 filteredGuidList.add(guid);
             }
         }
         
         return filteredGuidList;
     }
     
     private IPSGuid getEditGuid(int id) {
         PSComponentSummary summary = contentSummariesService.loadComponentSummary(id);
         PSLocator editLocator = summary.getEditLocator();
         return guidManager.makeGuid(editLocator);
     }   
     
     private IPSGuid getCurrentGuid(int id) {
         PSComponentSummary summary = contentSummariesService.loadComponentSummary(id);
         PSLocator currentLocator = summary.getCurrentLocator();
         return guidManager.makeGuid(currentLocator);
     }     
     
     private String buildContentSearchJcrQuery() {
         StringBuilder queryString = new StringBuilder("select rx:sys_contentid from {0} where rx:sys_contentcreateddate <= ''{1}'' ");        
         queryString.append(buildCreatedByUserClause());
         queryString.append(buildJcrPathClause());                
         return MessageFormat.format(queryString.toString(), getContentTypesForQuery(), getCreatedDateForQuery());
     }
     
     private String buildCreatedByUserClause() {        
         if (userIds.size() < 1) {
             return "";
         }
         StringBuilder retVal = new StringBuilder(" and (");
         boolean isFirst = true;
         for (String userId : userIds) {
             if (!isFirst) {
                 retVal.append(" or ");
             }
             retVal.append("rx:sys_contentcreatedby=''");
             retVal.append(userId);
             retVal.append("''");
             isFirst = false;
         }                
         retVal.append(")");    
         return retVal.toString();
     }
     
     private String buildJcrPathClause() {
         StringBuilder retVal = new StringBuilder(" and (");
         boolean isFirst = true;
         for (String folderPath : folderPaths) {
             if (!isFirst) {
                 retVal.append(" or ");
             }
             retVal.append(" jcr:path like ''");
             retVal.append(folderPath);
             retVal.append("''");
             isFirst = false;
         }
         retVal.append(")");
         return retVal.toString();
     }
     
     private String getContentTypesForQuery() {
         if (contentTypes.size() < 1) {
             return "*";
         }
         StringBuilder retVal = new StringBuilder();
         boolean isFirst = true;
         for (String contentType : contentTypes) {
             if (!isFirst) {
                 retVal.append(", ");
             }
             retVal.append(contentType);
             isFirst = false;
         }
         return retVal.toString();
     }
     
     private String getCreatedDateForQuery() {
         DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         return dateFormat.format(dateFrom.getTime());        
     }
     
     private void initRequestInfo(String communityId) {
         PSRequest psRequest = (PSRequest)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
         if(psRequest == null)
         {
             psRequest = com.percussion.server.PSRequest.getContextForRequest();                                 
             PSRequestInfo.initRequestInfo((java.util.Map)null);
             PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, psRequest);
             PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_USER, "rxserver");
         }
         PSRequestContext psRequestContext = new PSRequestContext(psRequest); 
         psRequestContext.setSessionPrivateObject("sys_community", communityId);
     }
     
     private PSContentNode getNodeFromGuid(IPSGuid guid) throws Exception {
         if (guid == null) {
             throw new Exception("Guid is null, cannot find node for a null guid");
         }
         List nodes = contentManager.findItemsByGUID(Collections.singletonList(guid), null);
         if (nodes.size() > 0) {
             return (PSContentNode) nodes.get(0);    
         }
         else
             throw new Exception("Could not find PSContentNode for guid: " + guid.toString());
     }    
 
 }
