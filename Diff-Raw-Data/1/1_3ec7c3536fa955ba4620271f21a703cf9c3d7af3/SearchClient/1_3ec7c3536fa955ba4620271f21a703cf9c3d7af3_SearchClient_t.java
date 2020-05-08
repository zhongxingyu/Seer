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
 
 import org.sola.services.boundary.wsclients.exception.WebServiceClientException;
 import org.sola.webservices.search.QueryForSelect;
 import org.sola.webservices.search.ResultForSelectionInfo;
 import org.sola.webservices.search.MapDefinitionTO;
 import org.sola.webservices.transferobjects.search.*;
 //import org.sola.webservices.transferobjects.search.CadastreObjectSearchParamsTO;
 
 /**
  * Interface for the Search Service. Implemented by {@linkplain SearchClientImpl}.
  * To obtain a reference to the Digital Archive Service, use {@linkplain WSManager#getSearchService()}
  *
  * @see SearchClientImpl
  * @see WSManager#getSearchService()
  */
 public interface SearchClient extends AbstractWSClient {
 
     /**
      * Search. - Service name prefix for the Search Web Service
      */
     public static final String SERVICE_NAME = "Search.";
     /**
      * Search.checkConnection - Identifier for the checkConnection method
      */
     public static final String CHECK_CONNECTION = SERVICE_NAME + "checkConnection";
     /**
      * Search.getAssignedApplications - Identifier for the
      * getAssignedApplications method
      */
     public static final String GET_ASSIGNED_APPLICATIONS = SERVICE_NAME + "getAssignedApplications";
     /**
      * Search.getUnassignedApplications - Identifier for the
      * getUnassignedApplications method
      */
     public static final String GET_UNASSIGNED_APPLICATIONS = SERVICE_NAME + "getUnassignedApplications";
     /**
      * Search.searchApplications - Identifier for the searchApplications method
      */
     public static final String SEARCH_APPLICATIONS = SERVICE_NAME + "searchApplications";
     /**
      * Search.verifyApplicationProperty - Identifier for the
      * verifyApplicationProperty method
      */
     public static final String VERIFY_APPLICATION_PROPERTY = SERVICE_NAME + "verifyApplicationProperty";
     /**
      * Search.select - Identifier for the select method
      */
     public static final String SELECT = SERVICE_NAME + "select";
     /**
      * Search.searchParties - Identifier for the searchParties method
      */
     public static final String SEARCH_PARTIES = SERVICE_NAME + "searchParties";
     /**
      * Search.searchSources - Identifier for the searchSources method
      */
     public static final String SEARCH_SOURCES = SERVICE_NAME + "searchSources";
     /**
      * Search.searchPowerOfAttorney - Identifier for the searchPowerOfAttorney
      * method
      */
     public static final String SEARCH_POWER_OF_ATTORNEY = SERVICE_NAME + "searchPowerOfAttorney";
     /**
      * Search.getActiveUsers - Identifier for the getActiveUsers method
      */
     public static final String GET_ACTIVE_USERS = SERVICE_NAME + "getActiveUsers";
     /**
      * Search.searchUsers - Identifier for the searchUsers method
      */
     public static final String SEARCH_USERS = SERVICE_NAME + "searchUsers";
     /**
      * Search.getApplicationLog - Identifier for the getApplicationLog method
      */
     public static final String GET_APPLICATION_LOG = SERVICE_NAME + "getApplicationLog";
     /**
      * Search.searchBr - Identifier for the searchBr method
      */
     public static final String SEARCH_BR = SERVICE_NAME + "searchBr";
     /**
      * Search.searchBaUnit - Identifier for the searchBaUnit method
      */
     public static final String SEARCH_BA_UNIT = SERVICE_NAME + "searchBaUnit";
     /**
      * Search.getSpatialSearchOptions - Identifier for the
      * getSpatialSearchOptions method
      */
     public static final String GET_SPATIAL_SEARCH_OPTIONS = SERVICE_NAME + "getSpatialSearchOptions";
     /**
      * Search.searchSpatialObjects - Identifier for the searchSpatialObjects
      * method
      */
     public static final String SEARCH_SPATIAL_OBJECTS = SERVICE_NAME + "searchSpatialObjects";
     /**
      * Search.getMapDefinition - Identifier for the getMapDefinition method
      */
     public static final String GET_MAP_DEFINITION = SERVICE_NAME + "getMapDefinition";
     public static final String SEARCH_RIGHTS_FOR_EXPORT = SERVICE_NAME + "searchRightsForExport";
     /*
      * LAA addition thoriso
      */
     /**
      * Search.searchDispute - Identifier for the searchDispute method
      */
     public static final String SEARCH_DISPUTE = SERVICE_NAME + "searchDispute";
 
         /**
      * Search.getExtentOfPublicDisplayMap - Identifier for the
      * getExtentOfPublicDisplayMap method
      */
     public static final String GET_EXTENT_OF_PUBLIC_DISPLAY_MAP =
             SERVICE_NAME + "getExtentOfPublicDisplayMap";
 
     /**
      * Search.searchBaUnit - Identifier for the searchBaUnit method
      */
     public static final String SEARCH_CADASTRE_OBJECTS = SERVICE_NAME + "searchCadastreObjects";
     
     /**
      * Returns applications that have a lodged or approved status and are
      * assigned to the currently logged in user.
      *
      * <p>If the currently logged in user has the {@linkplain RolesConstants#APPLICATION_UNASSIGN_FROM_OTHERS}
      * then all lodged or approved applications assigned to any user are
      * returned. </p>
      *
      * <p>Requires the {@linkplain RolesConstants#APPLICATION_VIEW_APPS}
      * role.</p>
      *
      * @return A maximum of 100 applications that match the search criteria,
      * sorted by lodgement date DESC.
      * @throws WebServiceClientException
      */
     List<ApplicationSearchResultTO> getAssignedApplications() throws WebServiceClientException;
 
     /**
      * Returns applications that have a lodged or approve status and are not
      * assigned to any user.
      *
      * <p>Requires the {@linkplain RolesConstants#APPLICATION_VIEW_APPS}
      * role.</p>
      *
      * @return A maximum of 100 applications that match the search criteria,
      * sorted by lodgement date DESC.
      * @throws WebServiceClientException
      */
     List<ApplicationSearchResultTO> getUnassignedApplications() throws WebServiceClientException;
 
     /**
      * Executes a search across all applications using the search criteria
      * provided. Partial, case insensitive matches are supported for the contact
      * person name, agent name, application number, document number and the
      * document reference number criteria.
      *
      * <p>Requires the {@linkplain RolesConstants#APPLICATION_VIEW_APPS}
      * role.</p>
      *
      * @param applicationSearchParamsTO The criteria to use for the search.
      * @return A maximum of 100 applications that match the search criteria,
      * sorted by lodgement date DESC.
      * @throws WebServiceClientException
      */
     List<ApplicationSearchResultTO> searchApplications(ApplicationSearchParamsTO applicationSearchParamsTO)
             throws WebServiceClientException;
 
     /**
      * Determines if the property details provided already exist in the SOLA
      * database or not. Can be used to help determine if the property being
      * added to a new application is valid or not.
      *
      * @param applicationNumber The number of the application the property is
      * being added/associated to. Used to exclude the current application from
      * the test and avoid a false positive match.
      * @param firstPart The first part of the property name.
      * @param lastPart The last part of the property name.
      * @throws WebServiceClientException
      */
     PropertyVerifierTO verifyApplicationProperty(String applicationNumber, String firstPart, String lastPart)
             throws WebServiceClientException;
 
     /**
      * Executes a group of dynamic spatial queries using a filtering geometry.
      * Primarily used to obtain results for the Object Information Tool. Each
      * dynamic query must have a set of query fields configured in the
      * system.query_field table.
      *
      * @param queries The list of dynamic spatial queries to execute using the
      * filtering geometry as a parameter.
      * @throws WebServiceClientException
      */
     public List<ResultForSelectionInfo> select(List<QueryForSelect> queries)
             throws WebServiceClientException;
 
     /**
      * Executes a search across all parties using the search criteria provided.
      * Partial matches are supported for the party name criteria.
      *
      * @param searchParams The criteria to use for the search.
      * @return A maximum of 101 parties that match the search criteria.
      * @throws WebServiceClientException
      */
     List<PartySearchResultTO> searchParties(PartySearchParamsTO searchParams)
             throws WebServiceClientException;
 
     /**
      * Executes a search across all sources using the search criteria provided.
      * Partial matches are supported for the document number and the document
      * reference number criteria.
      *
      * <p>Requires the {@linkplain RolesConstants#SOURCE_SEARCH} role.</p>
      *
      * @param params The criteria to use for the search.
      * @return A maximum of 101 sources that match the search criteria.
      * @throws WebServiceClientException
      */
     List<SourceSearchResultTO> searchSources(SourceSearchParamsTO searchParams)
             throws WebServiceClientException;
 
     /**
      * Returns details for all users marked as active in the SOLA database.
      *
      * @throws WebServiceClientException
      */
     List<UserSearchResultTO> getActiveUsers() throws WebServiceClientException;
 
     /**
      * Executes a search across all users using the search criteria provided.
      * Partial matches are supported for the username, first name and last name
      * criteria.
      *
      * <p>Requires the {@linkplain RolesConstants#ADMIN_MANAGE_SECURITY}
      * role.</p>
      *
      * @param searchParams The criteria to use for the search.
      * @return The users that match the search criteria.
      * @throws WebServiceClientException
      */
     List<UserSearchAdvancedResultTO> searchUsers(UserSearchParamsTO searchParams)
             throws WebServiceClientException;
 
     /**
      * Retrieves the history of changes and actions that have been applied to
      * the application. <p>Requires the {@linkplain RolesConstants#APPLICATION_VIEW_APPS}
      * role.</p>
      *
      * @param applicationId The application to retrieve the log for
      */
     List<ApplicationLogResultTO> getApplicationLog(String applicationId);
 
     /**
      * Executes a search across all Business Rules. Partial matches of the br
      * display name are supported. <p>Requires the {@linkplain RolesConstants#ADMIN_MANAGE_BR}
      * role.</p>
      *
      * @param searchParams The parameters to use for the search.
      * @throws WebServiceClientException
      */
     List<BrSearchResultTO> searchBr(BrSearchParamsTO searchParams) throws WebServiceClientException;
 
     /**
      * Executes a search across all BA Units. Partial, case insensitive matches
      * of the name first part, name last part and owner name are supported.
      *
      * <p>Requires the {@linkplain RolesConstants#ADMINISTRATIVE_BA_UNIT_SEARCH}
      * role.</p>
      *
      * @param searchParams The search criteria to use.
      * @return A maximum of 101 BA Units matching the search criteria.
      * @throws WebServiceClientException
      */
     List<BaUnitSearchResultTO> searchBaUnit(BaUnitSearchParamsTO searchParams)
             throws WebServiceClientException;
 
     /**
      * Retrieves the list of active spatial search options from the
      * system.map_search_option table.
      *
      * @throws WebServiceClientException
      */
     List<SpatialSearchOptionTO> getSpatialSearchOptions() throws WebServiceClientException;
 
     /**
      * Executes a spatial search using the specified query name and search
      * string
      *
      * @param queryName The name of the dynamic query to execute for the search
      * @param searchString The search string (e.g. the label of the spatial
      * object to find)
      * @return The results of the search
      */
     List<SpatialSearchResultTO> searchSpatialObjects(
             String queryName, String searchString) throws WebServiceClientException;
 
     /**
      * Returns the map layer config details from system.config_map_layer table.
      * Also retrieves the default map settings (i.e. default extent for the map
      * and srid) from the system.settings table. Uses the default language code
      * of the client locale to localize display values.
      *
      * @throws WebServiceClientException
      */
     MapDefinitionTO getMapDefinition() throws WebServiceClientException;
 
     /**
      * Executes a search across all Power of attorney using the search criteria
      * provided. Partial matches are supported for the document number and the
      * document reference number criteria.
      *
      * @param searchParams The criteria to use for the search.
      * @return A maximum of 101 records that match the search criteria.
      * @throws WebServiceClientException
      */
     List<PowerOfAttorneySearchResultTO> searchPowerOfAttorney(PowerOfAttorneySearchParamsTO searchParams) throws WebServiceClientException;
 
     List<RightsExportResultTO> searchRightsForExport(RightsExportParamsTO searchParams);
     
     /*
      * LAA Addition thoriso
      */
     
       /**
      * Executes a search across all Disputes. Partial, case insensitive matches
      * of the name  are supported.
      *
      * <p>Requires the {@linkplain RolesConstants#ADMINISTRATIVE_SEARCH_DISPUTE_SEARCH}
      * role.</p>
      *
      * @param searchParams The search criteria to use.
      * @return All Disputes matching the search criteria.
      * @throws WebServiceClientException
      */
     List<DisputeSearchResultTO> searchDispute(DisputeSearchParamsTO searchParams) throws WebServiceClientException;
 
     /**
      * It retrieves the extent of the public display map
      *
      * @param nameLastPart The filter of the cadastre objects in the public
      * display map
      * @return
      */
     public byte[] getExtentOfPublicDisplayMap(String nameLastPart) throws WebServiceClientException;
     
     public List<CadastreObjectSearchResultTO> searchCadastreObjects(CadastreObjectSearchParamsTO searchParams) throws WebServiceClientException;
 }
