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
 package org.sola.services.boundary.wsclients;
 
 import java.util.List;
 import javax.xml.namespace.QName;
 import org.sola.services.boundary.wsclients.exception.WebServiceClientException;
 import org.sola.webservices.search.*;
 import org.sola.webservices.transferobjects.search.*;
 
 /**
  * Implementation class for the {@linkplain SearchClient} interface.
  */
 public class SearchClientImpl extends AbstractWSClientImpl implements SearchClient {
 
     private static final String NAMESPACE_URI = "http://webservices.sola.org/search";
     private static final String LOCAL_PART = "search-service";
 
     /**
      * Creates a web service client class for the web service hosted at the
      * specified URL
      *
      * @param url The location of the service WSDL
      */
     public SearchClientImpl(String url) {
         super(url, new QName(NAMESPACE_URI, LOCAL_PART));
     }
 
     protected Search getPort() {
         return getPort(Search.class, SearchService.class);
     }
 
     @Override
     public boolean checkConnection() throws WebServiceClientException {
         boolean result = false;
         final String methodName = SearchClient.CHECK_CONNECTION;
         try {
             beforeWebMethod(methodName);
             result = getPort().checkConnection();
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result);
         }
         return result;
     }
 
     @Override
     public PropertyVerifierTO verifyApplicationProperty(String applicationNumber, String firstPart, String lastPart)
             throws WebServiceClientException {
         PropertyVerifierTO result = null;
         final String methodName = SearchClient.VERIFY_APPLICATION_PROPERTY;
         try {
             beforeWebMethod(methodName, applicationNumber, firstPart, lastPart);
             result = getPort().verifyApplicationProperty(applicationNumber, firstPart, lastPart);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, applicationNumber, firstPart, lastPart);
         }
         return result;
     }
 
     @Override
     public List<ApplicationSearchResultTO> getUnassignedApplications() throws WebServiceClientException {
         List<ApplicationSearchResultTO> result = null;
         final String methodName = SearchClient.GET_UNASSIGNED_APPLICATIONS;
         String languageCode = getLanguageCode();
         try {
             beforeWebMethod(methodName, languageCode);
             result = getPort().getUnassignedApplications(languageCode);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, languageCode);
         }
         return result;
     }
 
     @Override
     public List<ApplicationSearchResultTO> getAssignedApplications() throws WebServiceClientException {
         List<ApplicationSearchResultTO> result = null;
         final String methodName = SearchClient.GET_ASSIGNED_APPLICATIONS;
         String languageCode = getLanguageCode();
         try {
             beforeWebMethod(methodName, languageCode);
             result = getPort().getAssignedApplications(languageCode);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, languageCode);
         }
         return result;
     }
 
     @Override
     public List<ApplicationSearchResultTO> searchApplications(ApplicationSearchParamsTO applicationSearchParamsTO)
             throws WebServiceClientException {
         List<ApplicationSearchResultTO> result = null;
         final String methodName = SearchClient.SEARCH_APPLICATIONS;
         if (applicationSearchParamsTO.getLocale() == null) {
             applicationSearchParamsTO.setLocale(getLanguageCode());
         }
         try {
             beforeWebMethod(methodName, applicationSearchParamsTO);
             result = getPort().searchApplications(applicationSearchParamsTO);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, applicationSearchParamsTO);
         }
         return result;
     }
 
     @Override
     public List<ResultForSelectionInfo> select(List<QueryForSelect> queries)
             throws WebServiceClientException {
         List<ResultForSelectionInfo> result = null;
         final String methodName = SearchClient.SELECT;
         for (QueryForSelect query : queries) {
             query.setLocale(this.getLanguageCode());
         }
         try {
             beforeWebMethod(methodName, queries);
             result = getPort().select(queries);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, queries);
         }
         return result;
     }
 
     @Override
     public List<PartySearchResultTO> searchParties(PartySearchParamsTO searchParams)
             throws WebServiceClientException {
         List<PartySearchResultTO> result = null;
         final String methodName = SearchClient.SEARCH_PARTIES;
         try {
             beforeWebMethod(methodName, searchParams);
             result = getPort().searchParties(searchParams);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, searchParams);
         }
         return result;
     }
 
     @Override
     public List<SourceSearchResultTO> searchSources(SourceSearchParamsTO searchParams) throws WebServiceClientException {
         List<SourceSearchResultTO> result = null;
         final String methodName = SearchClient.SEARCH_SOURCES;
         if (searchParams.getLocale() == null) {
             searchParams.setLocale(getLanguageCode());
         }
         try {
             beforeWebMethod(methodName, searchParams);
             result = getPort().searchSources(searchParams);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, searchParams);
         }
         return result;
     }
 
     @Override
     public List<UserSearchResultTO> getActiveUsers() throws WebServiceClientException {
         List<UserSearchResultTO> result = null;
         final String methodName = SearchClient.GET_ACTIVE_USERS;
         try {
             beforeWebMethod(methodName);
             result = getPort().getActiveUsers();
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result);
         }
         return result;
     }
 
     @Override
     public List<UserSearchAdvancedResultTO> searchUsers(UserSearchParamsTO searchParams)
             throws WebServiceClientException {
         List<UserSearchAdvancedResultTO> result = null;
         final String methodName = SearchClient.SEARCH_USERS;
         try {
             beforeWebMethod(methodName, searchParams);
             result = getPort().searchUsers(searchParams);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, searchParams);
         }
         return result;
     }
 
     @Override
     public List<ApplicationLogResultTO> getApplicationLog(String applicationId)
             throws WebServiceClientException {
         List<ApplicationLogResultTO> result = null;
         final String methodName = SearchClient.GET_APPLICATION_LOG;
         try {
             beforeWebMethod(methodName, applicationId);
             result = getPort().getApplicationLog(applicationId);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, applicationId);
         }
         return result;
     }
 
     @Override
     public List<BrSearchResultTO> searchBr(BrSearchParamsTO searchParams) throws WebServiceClientException {
         List<BrSearchResultTO> result = null;
         final String methodName = SearchClient.SEARCH_BR;
         String languageCode = getLanguageCode();
         try {
             beforeWebMethod(methodName, searchParams, languageCode);
             result = getPort().searchBr(searchParams, languageCode);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, searchParams, languageCode);
         }
         return result;
     }
 
     @Override
     public List<BaUnitSearchResultTO> searchBaUnit(BaUnitSearchParamsTO searchParams) throws WebServiceClientException {
         List<BaUnitSearchResultTO> result = null;
         final String methodName = SearchClient.SEARCH_BA_UNIT;
         try {
             beforeWebMethod(methodName, searchParams);
             result = getPort().searchBaUnit(searchParams);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, searchParams);
         }
         return result;
     }
 
     @Override
     public List<SpatialSearchOptionTO> getSpatialSearchOptions() throws WebServiceClientException {
         List<SpatialSearchOptionTO> result = null;
         final String methodName = SearchClient.GET_SPATIAL_SEARCH_OPTIONS;
         String languageCode = getLanguageCode();
         try {
             beforeWebMethod(methodName, languageCode);
             result = getPort().getSpatialSearchOptions(languageCode);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, languageCode);
         }
         return result;
     }
 
     @Override
     public List<SpatialSearchResultTO> searchSpatialObjects(
             String queryName, String searchString) throws WebServiceClientException {
         List<SpatialSearchResultTO> result = null;
         final String methodName = SearchClient.SEARCH_SPATIAL_OBJECTS;
         try {
             beforeWebMethod(methodName, queryName, searchString);
             result = getPort().searchSpatialObjects(queryName, searchString);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, queryName, searchString);
         }
         return result;
     }
 
     @Override
     public MapDefinitionTO getMapDefinition() throws WebServiceClientException {
         MapDefinitionTO result = null;
         final String methodName = SearchClient.GET_MAP_DEFINITION;
         String languageCode = getLanguageCode();
         try {
             beforeWebMethod(methodName, languageCode);
             result = getPort().getMapDefinition(languageCode);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, languageCode);
         }
         return result;
     }
 
     @Override
     public List<PowerOfAttorneySearchResultTO> searchPowerOfAttorney(PowerOfAttorneySearchParamsTO searchParams)
             throws WebServiceClientException {
         List<PowerOfAttorneySearchResultTO> result = null;
         final String methodName = SearchClient.SEARCH_POWER_OF_ATTORNEY;
         if (searchParams.getLocale() == null) {
             searchParams.setLocale(getLanguageCode());
         }
         try {
             beforeWebMethod(methodName, searchParams);
             result = getPort().searchPowerOfAttorney(searchParams);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, searchParams);
         }
         return result;
     }
 
     @Override
     public List<RightsExportResultTO> searchRightsForExport(RightsExportParamsTO searchParams) {
         List<RightsExportResultTO> result = null;
         final String methodName = SearchClient.SEARCH_RIGHTS_FOR_EXPORT;
         try {
             beforeWebMethod(methodName);
             result = getPort().searchRightsForExport(searchParams);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result);
         }
         return result;
     }
 
     /*
      * LAA Addition thoriso
      */
     @Override
     public List<DisputeSearchResultTO> searchDispute(DisputeSearchParamsTO searchParams) throws WebServiceClientException {
         List<DisputeSearchResultTO> result = null;
         final String methodName = SearchClient.SEARCH_DISPUTE;
         try {
             beforeWebMethod(methodName, searchParams);
             result = getPort().searchDispute(searchParams);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, searchParams);
         }
         return result;
     }
 
     @Override
     public byte[] getExtentOfPublicDisplayMap(String nameLastPart) throws WebServiceClientException {
         byte[] result = null;
         final String methodName = SearchClient.GET_EXTENT_OF_PUBLIC_DISPLAY_MAP;
         try {
             beforeWebMethod(methodName, nameLastPart);
             result = getPort().getExtentOfPublicDisplayMap(nameLastPart);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, nameLastPart);
         }
         return result;
     }
 
     @Override
     public List<CadastreObjectSearchResultTO> searchCadastreObjects(CadastreObjectSearchParamsTO searchParams) throws WebServiceClientException {
         List<CadastreObjectSearchResultTO> result = null;
         final String methodName = SearchClient.SEARCH_CADASTRE_OBJECTS;
         try {
             beforeWebMethod(methodName, searchParams);
             result = getPort().searchCadastreObjects(searchParams);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, searchParams);
         }
         return result;
     }
 }
