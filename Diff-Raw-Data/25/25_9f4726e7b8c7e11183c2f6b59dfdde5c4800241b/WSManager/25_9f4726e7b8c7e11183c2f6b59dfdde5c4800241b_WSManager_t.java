 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO). All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted
  * provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,this list of conditions
  * and the following disclaimer. 2. Redistributions in binary form must reproduce the above
  * copyright notice,this list of conditions and the following disclaimer in the documentation and/or
  * other materials provided with the distribution. 3. Neither the name of FAO nor the names of its
  * contributors may be used to endorse or promote products derived from this software without
  * specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.boundary.wsclients;
 
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 import org.sola.common.messaging.ServiceMessage;
 import org.sola.services.boundary.wsclients.exception.WebServiceClientException;
 
 /**
  * Encapsulates Web-service clients instances, providing initialization and instances access. <br
  * />This class is singleton.
  */
 public class WSManager {
     
     private CaseManagementClient caseManagementWS;
     private AdminClient adminWS;
     private ReferenceDataClient referenceWS;
     private SearchClient searchWS;
     private DigitalArchiveClient digitalArchiveWS;
     private AdministrativeClient administrativeWS;
     private CadastreClient cadastreWS;
     private SpatialClient spatialWS;
     private FileStreamingClient fileStreamingWS;
     private AbstractWSClient testWS;
     
     private WSManager() {
     }
 
     /**
      * Private class to hold singleton instance.
      */
     private static class WSManagerHolder {
         
         private static final WSManager INSTANCE = new WSManager();
     }
 
     /**
      * Returns a singleton instance of {@link WSManager}.
      */
     public static WSManager getInstance() {
         return WSManagerHolder.INSTANCE;
     }
 
     /**
      * Creates the Web Service Client using reflection. This method does not cache the WSClient on
      * the WSManager. To cache the client, use the appropriate set...WS method.
      *
      * @param <T> Generic type for the Web Service Client class
      * @param wsClientClass The concrete type of the Web Service Client. Must extend
      * AbstractWSClient
      * @param wsdlURL The URL of the WSDL for the web service
      * @param userName
      * @param userPassword
      * @return Instantiated Web Service client or null.
      */
     public <T extends AbstractWSClient> T getWSClient(Class<T> wsClientClass, String wsdlURL,
             String userName, char[] userPassword) {
         T wsClient = null;
         if (wsdlURL != null && !wsdlURL.isEmpty()) {
             try {
                 Constructor<T> constructor = wsClientClass.getConstructor(String.class);
                 wsClient = constructor.newInstance(wsdlURL);
                 if (userName != null) {
                     wsClient.setCredentials(userName, userPassword);
                 }
                 // Catch the client if its valid so that its possible to test the connection later. 
                 testWS = wsClient;
             } catch (Exception ex) {
                 Object[] parms = {wsdlURL, ex.getLocalizedMessage(), ex};
                 throw new WebServiceClientException(ServiceMessage.EXCEPTION_INITALIZE_WSCLIENT, parms);
             }
         }
         return wsClient;
     }
 
     /**
      * Indicates if the last web service client instantiated has a valid connection or not.
      *
      * @return true if the connection is valid, false otherwise.
      */
     public boolean checkConnection() {
         boolean result = false;
         if (testWS != null) {
             result = testWS.checkConnection();
         }
         return result;
     }
 
     /**
      * Initializes Web-service clients instances. All Web-services clients are supposed to be
      * secured and accessed with username and password. While initialization
      * <code>checkConnection()</code> method is called on the Web-services clients to authenticate
      * user.
      *
      * @param userName The login name of the user.
      * @param userPassword The password of the user.
      * @throws WebServiceClientException
      */
     public boolean initWebServices(String userName, char[] userPassword, HashMap<String, String> config)
             throws WebServiceClientException {
                
        boolean result = false;
        if (getSearchService() == null) {
            setSearchWS(getWSClient(SearchClientImpl.class,
                    config.get(WSConfig.SOLA_WS_SEARCH_SERVICE_URL.toString()),
                    userName, userPassword));
        }

        // Ticket #206 - Check the connection immediately in case the login credentials are invalid
        try {
            result = checkConnection();
        } finally {
            if (!result) {
                // Reset the search service so that the invalid login credentials are not cached. 
                setSearchWS(null);
            }
        }
 
         // The spatial service is not secured to improve the performance of spatial navigation. 
         if (getSpatialService() == null) {
             setSpatialWS(getWSClient(SpatialClientImpl.class,
                     config.get(WSConfig.SOLA_WS_SPATIAL_SERVICE_URL.toString()),
                     null, null));
         }
 
         // The file streaming service is not secured to improve the performance of file upload
         //and download and avoid Out of Memory errors. 
         if (getFileStreamingService() == null) {
             setFileStreamingWS(getWSClient(FileStreamingClientImpl.class,
                     config.get(WSConfig.SOLA_WS_FILE_STREAMING_SERVICE_URL.toString()),
                     null, null));
         }
         
         if (getCaseManagementService() == null) {
             setCaseManagementWS(getWSClient(CaseManagementClientImpl.class,
                     config.get(WSConfig.SOLA_WS_CASE_MANAGEMENT_SERVICE_URL.toString()),
                     userName, userPassword));
         }
         
         if (getAdminService() == null) {
             setAdminWS(getWSClient(AdminClientImpl.class,
                     config.get(WSConfig.SOLA_WS_ADMIN_SERVICE_URL.toString()),
                     userName, userPassword));
         }
         
         if (getReferenceDataService() == null) {
             setReferenceWS(getWSClient(ReferenceDataClientImpl.class,
                     config.get(WSConfig.SOLA_WS_REFERENCE_DATA_SERVICE_URL.toString()),
                     userName, userPassword));
         }
         
         if (getDigitalArchive() == null) {
             setDigitalArchiveWS(getWSClient(DigitalArchiveClientImpl.class,
                     config.get(WSConfig.SOLA_WS_DIGITAL_ARCHIVE_URL.toString()),
                     userName, userPassword));
             getDigitalArchive().setFileStreamingService(getFileStreamingService());
         }
         
         if (getAdministrative() == null) {
             setAdministrativeWS(getWSClient(AdministrativeClientImpl.class,
                     config.get(WSConfig.SOLA_WS_ADMINISTRATIVE_SERVICE_URL.toString()),
                     userName, userPassword));
         }
         
         if (getCadastreService() == null) {
             setCadastreWS(getWSClient(CadastreClientImpl.class,
                     config.get(WSConfig.SOLA_WS_CADASTRE_SERVICE_URL.toString()),
                     userName, userPassword));
         }
         
        return result; 
     }
 
     /**
      * Returns administrative Web-service client instance.
      */
     public AdministrativeClient getAdministrative() {
         return administrativeWS;
     }
 
     /**
      * Returns digital archive Web-service client instance.
      */
     public DigitalArchiveClient getDigitalArchive() {
         return digitalArchiveWS;
     }
 
     /**
      * Returns case management Web-service client instance.
      */
     public CaseManagementClient getCaseManagementService() {
         return caseManagementWS;
     }
 
     /**
      * Returns admin Web-service client instance.
      */
     public AdminClient getAdminService() {
         return adminWS;
     }
 
     /**
      * Returns reference data Web-service client instance.
      */
     public ReferenceDataClient getReferenceDataService() {
         return referenceWS;
     }
 
     /**
      * Returns search Web-service client instance.
      */
     public SearchClient getSearchService() {
         return searchWS;
     }
 
     /**
      * Returns search Web-service client instance.
      */
     public CadastreClient getCadastreService() {
         return cadastreWS;
     }
 
     /**
      * Returns the File Streaming Web-service client instance.
      */
     public FileStreamingClient getFileStreamingService() {
         return fileStreamingWS;
     }
 
     /**
      * Returns spatial Web-service client instance.
      */
     public SpatialClient getSpatialService() {
         return spatialWS;
     }
     
     public void setAdminWS(AdminClient adminWS) {
         this.adminWS = adminWS;
     }
     
     public void setAdministrativeWS(AdministrativeClient administrativeWS) {
         this.administrativeWS = administrativeWS;
     }
     
     public void setCadastreWS(CadastreClient cadastreWS) {
         this.cadastreWS = cadastreWS;
     }
     
     public void setCaseManagementWS(CaseManagementClient caseManagementWS) {
         this.caseManagementWS = caseManagementWS;
     }
     
     public void setDigitalArchiveWS(DigitalArchiveClient digitalArchiveWS) {
         this.digitalArchiveWS = digitalArchiveWS;
     }
     
     public void setReferenceWS(ReferenceDataClient referenceWS) {
         this.referenceWS = referenceWS;
     }
     
     public void setSearchWS(SearchClient searchWS) {
         this.searchWS = searchWS;
     }
     
     public void setSpatialWS(SpatialClient spatialWS) {
         this.spatialWS = spatialWS;
     }
     
     public void setFileStreamingWS(FileStreamingClient fileStreamingWS) {
         this.fileStreamingWS = fileStreamingWS;
     }
 }
