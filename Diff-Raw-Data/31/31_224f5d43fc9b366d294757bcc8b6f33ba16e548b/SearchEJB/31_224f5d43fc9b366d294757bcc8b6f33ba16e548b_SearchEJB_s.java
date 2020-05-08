 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations
  * (FAO). All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,this
  * list of conditions and the following disclaimer. 2. Redistributions in binary
  * form must reproduce the above copyright notice,this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. 3. Neither the name of FAO nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
  * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.ejb.search.businesslogic;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import javax.annotation.security.RolesAllowed;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import org.sola.common.RolesConstants;
 import org.sola.common.SOLAException;
 import org.sola.common.messaging.ServiceMessage;
 import org.sola.services.common.ejbs.AbstractEJB;
 import org.sola.services.common.logging.LogUtility;
 import org.sola.services.common.repository.CommonSqlProvider;
 import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;
 import org.sola.services.ejb.search.repository.entities.ApplicationLogResult;
 import org.sola.services.ejb.search.repository.entities.ApplicationSearchParams;
 import org.sola.services.ejb.search.repository.entities.ApplicationSearchResult;
 import org.sola.services.ejb.search.repository.entities.BaUnitSearchParams;
 import org.sola.services.ejb.search.repository.entities.BaUnitSearchResult;
 import org.sola.services.ejb.search.repository.entities.BrSearchParams;
 import org.sola.services.ejb.search.repository.entities.BrSearchResult;
 import org.sola.services.ejb.search.repository.entities.CadastreObjectSearchResult;
 import org.sola.services.ejb.search.repository.entities.ConfigMapLayer;
 import org.sola.services.ejb.search.repository.entities.GenericResult;
 import org.sola.services.ejb.search.repository.entities.PartySearchParams;
 import org.sola.services.ejb.search.repository.entities.PartySearchResult;
 import org.sola.services.ejb.search.repository.entities.PropertyVerifier;
 import org.sola.services.ejb.search.repository.SearchSqlProvider;
 import org.sola.services.ejb.search.repository.entities.Setting;
 import org.sola.services.ejb.search.repository.entities.SourceSearchParams;
 import org.sola.services.ejb.search.repository.entities.SourceSearchResult;
 import org.sola.services.ejb.search.repository.entities.SpatialResult;
 import org.sola.services.ejb.search.repository.entities.UserSearchParams;
 import org.sola.services.ejb.search.repository.entities.UserSearchResult;
 import org.sola.services.ejb.search.repository.entities.DynamicQuery;
 import org.sola.services.ejb.search.repository.entities.DynamicQueryField;
 import org.sola.services.ejb.search.spatial.QueryForNavigation;
 import org.sola.services.ejb.search.spatial.QueryForSelect;
 import org.sola.services.ejb.search.spatial.ResultForNavigationInfo;
 import org.sola.services.ejb.search.spatial.ResultForSelectionInfo;
 
 @Stateless
 @EJB(name = "java:global/SOLA/SearchEJBLocal", beanInterface = SearchEJBLocal.class)
 public class SearchEJB extends AbstractEJB implements SearchEJBLocal {
 
     private DynamicQuery getDynamicQuery(String queryName, Map params) {
         DynamicQuery query = null;
         // Retrieve the dynamic query from the database. Use localization if it is provided
         // as a query parameter. 
         if (params != null && params.containsKey(CommonSqlProvider.PARAM_LANGUAGE_CODE)) {
             query = getRepository().getEntity(DynamicQuery.class, queryName,
                     params.get(CommonSqlProvider.PARAM_LANGUAGE_CODE).toString());
         } else {
             query = getRepository().getEntity(DynamicQuery.class, queryName);
         }
         if (query == null) {
             // Raise an error to indicate the dynamic query does not exist
             throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED,
                     new Object[]{"Dynamic query " + queryName + " does not exist."});
         }
         return query;
     }
 
     // Returns a generic result from the dynamic query
     private ArrayList<HashMap> executeDynamicQuery(DynamicQuery query, Map params) {
         params = params == null ? new HashMap<String, Object>() : params;
         params.put(CommonSqlProvider.PARAM_QUERY, query.getSql());
         return getRepository().executeSql(params);
     }
 
     // Overloaded version of executeDynamicQuery that returns a list of entities from the query
     private <T extends AbstractReadOnlyEntity> List<T> executeDynamicQuery(Class<T> entityClass,
             String queryName, Map params) {
         params = params == null ? new HashMap<String, Object>() : params;
         DynamicQuery query = getDynamicQuery(queryName, params);
         params.put(CommonSqlProvider.PARAM_QUERY, query.getSql());
         return getRepository().getEntityList(entityClass, params);
     }
 
     @Override
     public GenericResult getGenericResultList(
             String queryName, Map params) {
 
         GenericResult result = new GenericResult();
         DynamicQuery query = getDynamicQuery(queryName, params);
         if (query.getFieldList() == null || query.getFieldList().isEmpty()) {
             throw new RuntimeException("The field list is missing. If there is a query to be used to"
                     + " return dynamic result, you have to define a field list.");
         }
         ArrayList<HashMap> queryResult = executeDynamicQuery(query, params);
 
         // Create the generic result from the query result. 
         if (queryResult != null && !queryResult.isEmpty()) {
 
             String[] fieldNames = null;
             List<String> queryFields = new ArrayList<String>();
             List<String> displayNames = new ArrayList<String>();
 
             // Get any query fields and display names from the dynamic query configuration. 
             //if (query.getQueryFieldNames() != null) {
             // asList returns a fixed lenght list backed by the array so need to create a 
             // new list based on the array instead. 
             queryFields = new ArrayList<String>(Arrays.asList(query.getQueryFieldNames()));
             displayNames = new ArrayList<String>(Arrays.asList(query.getFieldDisplayNames()));
             //}
 
             // Need to cycle through a few of the results to get the remaining query fields  
             // (i.e. those not identifeid in the configuration) because any null values from the 
             // dynamic query are completely omitted from the hashmap for that row.  This issue may 
             // be fixed in later versions of Mybatis (i.e. later than 3.0.6). 
 //            int count = 0;
 //            for (Map rowMap : queryResult) {
 //                for (Object field : rowMap.keySet()) {
 //                    if (!queryFields.contains(field.toString())) {
 //                        queryFields.add(field.toString());
 //                        displayNames.add(field.toString());
 //                    }
 //                }
 //                count++;
 //                if (count > 5) {
 //                    // Have cycled through at least 5 results, so exit the for loop. 
 //                    break;
 //                }
 //            }
             fieldNames = queryFields.toArray(new String[0]);
             result.setFieldNames(displayNames.toArray(new String[0]));
 
             for (HashMap map : queryResult) {
                 Object[] values = new Object[fieldNames.length];
                 for (int i = 0; i < fieldNames.length; i++) {
                     values[i] = map.get(fieldNames[i]);
                 }
                 result.addRow(values);
             }
         }
         return result;
     }
 
     /**
      * It returns the first row of the result set. It is used especially from
      * business rules.
      *
      * @param sqlStatement
      * @param params
      * @return
      */
     @Override
     public HashMap getResultObjectFromStatement(String sqlStatement, Map params) {
         params = params == null ? new HashMap<String, Object>() : params;
         params.put(CommonSqlProvider.PARAM_QUERY, sqlStatement);
         // Returns a single result
         //return getRepository().getScalar(Object.class, params); 
         // To use if more than one result is required. 
         List<HashMap> resultList = getRepository().executeSql(params);
         HashMap result = null;
         if (!resultList.isEmpty()) {
             result = resultList.get(0);
         }
         return result;
     }
 
     @Override
     public PropertyVerifier getPropertyVerifier(String firstPart, String lastPart) {
         Map params = new HashMap<String, Object>();
         params.put(CommonSqlProvider.PARAM_QUERY, PropertyVerifier.QUERY_VERIFY_SQL);
         params.put(PropertyVerifier.QUERY_PARAM_FIRST_PART, firstPart);
         params.put(PropertyVerifier.QUERY_PARAM_LAST_PART, lastPart);
         return getRepository().getEntity(PropertyVerifier.class, params);
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_VIEW_APPS)
     public List<ApplicationSearchResult> searchApplications(ApplicationSearchParams params) {
         // Process params
 
         Map queryParams = new HashMap<String, Object>();
         queryParams.put(CommonSqlProvider.PARAM_FROM_PART, ApplicationSearchResult.QUERY_FROM);
 
         queryParams.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, params.getLocale());
         queryParams.put(ApplicationSearchResult.QUERY_PARAM_CONTACT_NAME,
                 params.getContactPerson() == null ? "%" : params.getContactPerson().trim() + "%");
         queryParams.put(ApplicationSearchResult.QUERY_PARAM_AGENT_NAME,
                 params.getAgent() == null ? "%" : params.getAgent().trim() + "%");
         queryParams.put(ApplicationSearchResult.QUERY_PARAM_APP_NR,
                 params.getNr() == null ? "%" : params.getNr().trim() + "%");
         queryParams.put(ApplicationSearchResult.QUERY_PARAM_FROM_LODGE_DATE,
                 params.getFromDate() == null ? new GregorianCalendar(1, 1, 1).getTime() : params.getFromDate());
         queryParams.put(ApplicationSearchResult.QUERY_PARAM_TO_LODGE_DATE,
                 params.getToDate() == null ? new GregorianCalendar(2500, 1, 1).getTime() : params.getToDate());
 
         queryParams.put(CommonSqlProvider.PARAM_WHERE_PART, ApplicationSearchResult.QUERY_WHERE_SEARCH_APPLICATIONS);
         queryParams.put(CommonSqlProvider.PARAM_ORDER_BY_PART, ApplicationSearchResult.QUERY_ORDER_BY);
         queryParams.put(CommonSqlProvider.PARAM_LIMIT_PART, "100");
 
         return getRepository().getEntityList(ApplicationSearchResult.class, queryParams);
     }
 
     @Override
     @RolesAllowed(RolesConstants.SOURCE_SEARCH)
     public List<SourceSearchResult> searchSources(SourceSearchParams searchParams) {
         Map params = new HashMap<String, Object>();
 
         params.put(SourceSearchResult.QUERY_PARAM_FROM_RECORDATION_DATE,
                 searchParams.getFromRecordationDate() == null
                 ? new GregorianCalendar(1, 1, 1).getTime()
                 : searchParams.getFromRecordationDate());
         params.put(SourceSearchResult.QUERY_PARAM_TO_RECORDATION_DATE,
                 searchParams.getToRecordationDate() == null
                 ? new GregorianCalendar(2500, 1, 1).getTime()
                 : searchParams.getToRecordationDate());
         params.put(SourceSearchResult.QUERY_PARAM_FROM_SUBMISSION_DATE,
                 searchParams.getFromSubmissionDate() == null
                 ? new GregorianCalendar(1, 1, 1).getTime()
                 : searchParams.getFromSubmissionDate());
         params.put(SourceSearchResult.QUERY_PARAM_TO_SUBMISSION_DATE,
                 searchParams.getToSubmissionDate() == null
                 ? new GregorianCalendar(2500, 1, 1).getTime()
                 : searchParams.getToSubmissionDate());
         params.put(SourceSearchResult.QUERY_PARAM_TYPE_CODE,
                 searchParams.getTypeCode() == null ? "" : searchParams.getTypeCode());
         params.put(SourceSearchResult.QUERY_PARAM_REF_NUMBER,
                 searchParams.getRefNumber() == null ? "" : searchParams.getRefNumber());
         params.put(SourceSearchResult.QUERY_PARAM_LA_NUMBER,
                 searchParams.getLaNumber() == null ? "" : searchParams.getLaNumber());
         params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE,
                 searchParams.getLocale() == null ? "en" : searchParams.getLocale());
 
         params.put(CommonSqlProvider.PARAM_QUERY, SourceSearchResult.SEARCH_QUERY);
         return getRepository().getEntityList(SourceSearchResult.class, params);
     }
 
     @Override
     @RolesAllowed(RolesConstants.ADMIN_MANAGE_SECURITY)
     public List<UserSearchResult> searchUsers(UserSearchParams searchParams) {
         if (searchParams.getGroupId() == null) {
             searchParams.setGroupId("");
         }
 
         if (searchParams.getUserName() == null) {
             searchParams.setUserName("");
         }
 
         if (searchParams.getFirstName() == null) {
             searchParams.setFirstName("");
         }
 
         if (searchParams.getLastName() == null) {
             searchParams.setLastName("");
         }
 
         Map params = new HashMap<String, Object>();
         params.put(CommonSqlProvider.PARAM_QUERY, UserSearchResult.QUERY_ADVANCED_USER_SEARCH);
         params.put("userName", searchParams.getUserName());
         params.put("firstName", searchParams.getFirstName());
         params.put("lastName", searchParams.getLastName());
         params.put("groupId", searchParams.getGroupId());
         return getRepository().getEntityList(UserSearchResult.class, params);
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_VIEW_APPS)
     public List<ApplicationSearchResult> getUnassignedApplications(String locale) {
 
         Map params = new HashMap<String, Object>();
         params.put(CommonSqlProvider.PARAM_FROM_PART, ApplicationSearchResult.QUERY_FROM);
         params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, locale);
         params.put(CommonSqlProvider.PARAM_WHERE_PART, ApplicationSearchResult.QUERY_WHERE_GET_UNASSIGNED);
         params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, ApplicationSearchResult.QUERY_ORDER_BY);
         params.put(CommonSqlProvider.PARAM_LIMIT_PART, "100");
 
         return getRepository().getEntityList(ApplicationSearchResult.class, params);
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_VIEW_APPS)
     public List<ApplicationSearchResult> getAssignedApplications(String locale) {
         Map params = new HashMap<String, Object>();
         params.put(CommonSqlProvider.PARAM_FROM_PART, ApplicationSearchResult.QUERY_FROM);
         params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, locale);
         
         if (isInRole(RolesConstants.APPLICATION_UNASSIGN_FROM_OTHERS)) {
             params.put(CommonSqlProvider.PARAM_WHERE_PART, ApplicationSearchResult.QUERY_WHERE_GET_ASSIGNED_ALL);
         } else {
             params.put(ApplicationSearchResult.QUERY_PARAM_USER_NAME, getUserName());
             params.put(CommonSqlProvider.PARAM_WHERE_PART, ApplicationSearchResult.QUERY_WHERE_GET_ASSIGNED);
         }
         
         params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, ApplicationSearchResult.QUERY_ORDER_BY);
         params.put(CommonSqlProvider.PARAM_LIMIT_PART, "100");
 
         return getRepository().getEntityList(ApplicationSearchResult.class, params);
     }
 
     @Override
     public List<UserSearchResult> getActiveUsers() {
         Map params = new HashMap<String, Object>();
         params.put(CommonSqlProvider.PARAM_QUERY, UserSearchResult.QUERY_ACTIVE_USERS);
         return getRepository().getEntityList(UserSearchResult.class, params);
     }
 
     @Override
     public List<PartySearchResult> searchParties(PartySearchParams searchParams) {
         if (searchParams.getName() == null) {
             searchParams.setName("");
         }
         if (searchParams.getTypeCode() == null) {
             searchParams.setTypeCode("");
         }
         if (searchParams.getRoleTypeCode() == null) {
             searchParams.setRoleTypeCode("");
         }
 
         searchParams.setName(searchParams.getName().trim());
 
         Map params = new HashMap<String, Object>();
         params.put(CommonSqlProvider.PARAM_QUERY, PartySearchResult.SEARCH_QUERY);
         params.put("name", searchParams.getName());
         params.put("typeCode", searchParams.getTypeCode());
         params.put("roleTypeCode", searchParams.getRoleTypeCode());
         return getRepository().getEntityList(PartySearchResult.class, params);
     }
 
     @Override
     public ResultForNavigationInfo getSpatialResult(
             QueryForNavigation spatialQuery) {
         Map params = new HashMap<String, Object>();
         params.put("minx", spatialQuery.getWest());
         params.put("miny", spatialQuery.getSouth());
         params.put("maxx", spatialQuery.getEast());
         params.put("maxy", spatialQuery.getNorth());
         params.put("srid", spatialQuery.getSrid());
         ResultForNavigationInfo spatialResultInfo = new ResultForNavigationInfo();
         getRepository().setLoadInhibitors(new Class[]{DynamicQueryField.class});
         List<SpatialResult> result = executeDynamicQuery(SpatialResult.class,
                 spatialQuery.getQueryName(), params);
         getRepository().clearLoadInhibitors();
         spatialResultInfo.setToAdd(result);
         return spatialResultInfo;
     }
 
     @Override
     public List<ConfigMapLayer> getConfigMapLayerList(String languageCode) {
         Map params = new HashMap<String, Object>();
         params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, languageCode);
         params.put(CommonSqlProvider.PARAM_QUERY, ConfigMapLayer.QUERY_SQL);
         return getRepository().getEntityList(ConfigMapLayer.class, params);
     }
 
     @Override
     public List<ResultForSelectionInfo> getSpatialResultFromSelection(
             List<QueryForSelect> queriesForSelection) {
         List<ResultForSelectionInfo> results = new ArrayList<ResultForSelectionInfo>();
         for (QueryForSelect queryInfo : queriesForSelection) {
             Map params = new HashMap<String, Object>();
             params.put(ResultForSelectionInfo.PARAM_GEOMETRY, queryInfo.getFilteringGeometry());
             params.put(ResultForSelectionInfo.PARAM_SRID, queryInfo.getSrid());
             if (queryInfo.getLocale() != null) {
                 params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, queryInfo.getLocale());
             } else {
                 params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, "en");
             }
             ResultForSelectionInfo resultInfo = new ResultForSelectionInfo();
             resultInfo.setId(queryInfo.getId());
             resultInfo.setResult(this.getGenericResultList(queryInfo.getQueryName(), params));
             results.add(resultInfo);
         }
         return results;
     }
 
     @Override
     public HashMap<String, String> getMapSettingList() {
         return this.getSettingList(Setting.QUERY_SQL_FOR_MAP_SETTINGS);
     }
 
     private HashMap<String, String> getSettingList(String queryBody) {
         HashMap settingMap = new HashMap();
         Map params = new HashMap<String, Object>();
         params.put(CommonSqlProvider.PARAM_QUERY, queryBody);
         List<Setting> settings = getRepository().getEntityList(Setting.class, params);
         if (settings != null && !settings.isEmpty()) {
             for (Setting setting : settings) {
                 settingMap.put(setting.getId(), setting.getVl());
             }
         }
         return settingMap;
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_VIEW_APPS)
     public List<ApplicationLogResult> getApplicationLog(String applicationId) {
         Map params = new HashMap<String, Object>();
         params.put(CommonSqlProvider.PARAM_QUERY, SearchSqlProvider.buildApplicationLogSql());
         params.put(SearchSqlProvider.PARAM_APPLICATION_ID, applicationId);
         return getRepository().getEntityList(ApplicationLogResult.class, params);
 
     }
 
     @RolesAllowed(RolesConstants.ADMIN_MANAGE_BR)
     @Override
     public List<BrSearchResult> searchBr(BrSearchParams searchParams, String lang) {
         Map params = new HashMap<String, Object>();
 
         if (searchParams.getDisplayName() == null) {
             searchParams.setDisplayName("");
         }
         if (searchParams.getTargetCode() == null) {
             searchParams.setTargetCode("");
         }
         if (searchParams.getTechnicalTypeCode() == null) {
             searchParams.setTechnicalTypeCode("");
         }
 
         searchParams.setDisplayName(searchParams.getDisplayName().trim());
 
         params.put(CommonSqlProvider.PARAM_QUERY, BrSearchResult.SELECT_QUERY);
         params.put("lang", lang);
         params.put("displayName", searchParams.getDisplayName());
         params.put("technicalTypeCode", searchParams.getTechnicalTypeCode());
         params.put("targetCode", searchParams.getTargetCode());
         return getRepository().getEntityList(BrSearchResult.class, params);
     }
 
     @Override
     public List<DynamicQuery> getQueryListAll() {
         return this.getRepository().getEntityList(DynamicQuery.class);
     }
 
     @Override
     @RolesAllowed(RolesConstants.ADMINISTRATIVE_BA_UNIT_SEARCH)
     public List<BaUnitSearchResult> searchBaUnits(BaUnitSearchParams searchParams) {
         Map params = new HashMap<String, Object>();
 
         if (searchParams.getNameFirstPart() == null) {
             searchParams.setNameFirstPart("");
         }
         if (searchParams.getNameLastPart() == null) {
             searchParams.setNameLastPart("");
         }
         if (searchParams.getOwnerName() == null) {
             searchParams.setOwnerName("");
         }
 
         params.put(CommonSqlProvider.PARAM_QUERY, BaUnitSearchResult.SEARCH_QUERY);
         params.put("ownerName", searchParams.getOwnerName());
         params.put("nameFirstPart", searchParams.getNameFirstPart());
         params.put("nameLastPart", searchParams.getNameLastPart());
         return getRepository().getEntityList(BaUnitSearchResult.class, params);
     }
 
     @Override
     public List<CadastreObjectSearchResult> searchCadastreObjects(
             String searchBy, String searchString) {
         String wherePart = null;
         String selectPart = null;
         String fromPart = null;
        Integer numberOfMaxRecordsReturned = 10;
 
         if (searchBy.equals(CadastreObjectSearchResult.SEARCH_BY_NUMBER)) {
             wherePart = CadastreObjectSearchResult.QUERY_WHERE_SEARCHBY_NUMBER;
         } else if (searchBy.equals(CadastreObjectSearchResult.SEARCH_BY_BAUNIT)) {
             selectPart = CadastreObjectSearchResult.QUERY_SELECT_SEARCHBY_BAUNIT;
             fromPart = CadastreObjectSearchResult.QUERY_FROM_SEARCHBY_BAUNIT;
             wherePart = CadastreObjectSearchResult.QUERY_WHERE_SEARCHBY_BAUNIT;
         } else if (searchBy.equals(CadastreObjectSearchResult.SEARCH_BY_OWNER_OF_BAUNIT)) {
             selectPart = CadastreObjectSearchResult.QUERY_SELECT_SEARCHBY_OWNER_OF_BAUNIT;
             fromPart = CadastreObjectSearchResult.QUERY_FROM_SEARCHBY_OWNER_OF_BAUNIT;
             wherePart = CadastreObjectSearchResult.QUERY_WHERE_SEARCHBY_OWNER_OF_BAUNIT;
         } else if (searchBy.equals(CadastreObjectSearchResult.SEARCH_BY_BAUNIT_ID)) {
             wherePart = CadastreObjectSearchResult.QUERY_WHERE_GET_NEW_PARCELS;
             numberOfMaxRecordsReturned = 0;
         }
         List<CadastreObjectSearchResult> result = new ArrayList<CadastreObjectSearchResult>();
         if (wherePart != null) {
             Map params = new HashMap<String, Object>();
             if (numberOfMaxRecordsReturned > 0) {
                 params.put(CommonSqlProvider.PARAM_LIMIT_PART, numberOfMaxRecordsReturned);
             }
             if (fromPart != null) {
                 params.put(CommonSqlProvider.PARAM_FROM_PART, fromPart);
             }
             if (selectPart != null) {
                 params.put(CommonSqlProvider.PARAM_SELECT_PART, selectPart);
             }
             params.put(CommonSqlProvider.PARAM_WHERE_PART, wherePart);
             params.put(CadastreObjectSearchResult.SEARCH_STRING_PARAM, searchString);
             result = this.getRepository().getEntityList(CadastreObjectSearchResult.class, params);
         }
         return result;
     }
 }
