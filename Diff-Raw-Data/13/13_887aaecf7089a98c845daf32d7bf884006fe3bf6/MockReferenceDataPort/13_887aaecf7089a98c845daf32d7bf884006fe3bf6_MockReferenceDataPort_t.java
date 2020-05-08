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
 package org.sola.services.boundary.wsclients.mock;
 
 import java.util.List;
 import org.sola.services.boundary.wsclients.ReferenceDataClient;
 import org.sola.webservices.referencedata.*;
 import org.sola.webservices.transferobjects.AbstractCodeTO;
 import org.sola.webservices.transferobjects.referencedata.*;
 
 /**
  * Provides a mock implementation for the
  * {@linkplain org.sola.webservices.referencedata.ReferenceData} interface. Uses
  * the
  * {@linkplain MockServiceManager} to obtain the appropriate mock response for
  * each web method. <p>Each method mocked by this class has a public constant
  * defined that can be used to reference a mock response object from the {@linkplain MockServiceManager}.
  * To set a response object for a web method, use the {@linkplain MockServiceManager#setResponse(String, Object)}
  * method referencing the appropriate web method constant from {@linkplain org.sola.services.boundary.wsclients.ReferenceDataClient}.</p>
  *
  * @see MockReferenceDataClient
  * @see ReferenceDataClient
  * @see MockServiceManager
  * @see MockResponse
  */
 public class MockReferenceDataPort implements ReferenceData {
 
     /**
      *
      * @return A reference to the MockServiceManager
      */
     private MockServiceManager getManager() {
         return MockServiceManager.getInstance();
     }
 
     /**
      * Processes the mock response exception and throws the appropriate service
      * exception or a MockResponseException if the response exception is not a
      * recognized type.
      *
      * @param ex The Mock response exception to process
      */
     private void processExceptionBasic(Exception ex) throws SOLAFault, UnhandledFault {
         if (SOLAFault.class.isAssignableFrom(ex.getClass())) {
             throw (SOLAFault) ex;
         } else if (UnhandledFault.class.isAssignableFrom(ex.getClass())) {
             throw (UnhandledFault) ex;
         } else if (RuntimeException.class.isAssignableFrom(ex.getClass())) {
             throw (RuntimeException) ex;
         } else {
             // The type of the exception does not match any recognized exception type used by this 
             // web service. Throw a MockResponseException to fail the test. 
             throw new MockResponseException("Unable to convert the mocked response exception to "
                     + "recognized exception type: " + ex.getClass().getName());
         }
     }
 
     /**
      * Processes the mock response exception and throws the appropriate service
      * exception or a MockResponseException if the response exception is not a
      * recognized type. Extends {@linkplain #processExceptionBasic(java.lang.Exception) processExceptionBasic}
      * to include the SOLAAccessFault;
      *
      * @param ex The Mock response exception to process
      */
     private void processExceptionAccess(Exception ex) throws SOLAFault, SOLAAccessFault,
             UnhandledFault, MockResponseException {
         if (SOLAAccessFault.class.isAssignableFrom(ex.getClass())) {
             throw (SOLAAccessFault) ex;
         } else {
             processExceptionBasic(ex);
         }
     }
 
     /**
      * Processes the mock response exception and throws the appropriate service
      * exception or a MockResponseException if the response exception is not a
      * recognized type. Extends {@linkplain #processExceptionBasic(java.lang.Exception) processExceptionBasic}
      * to include the OptimisticLockingFault;
      *
      * @param ex The Mock response exception to process
      */
     private void processExceptionUpdate(Exception ex) throws SOLAFault, SOLAAccessFault,
             UnhandledFault, OptimisticLockingFault, MockResponseException {
         if (OptimisticLockingFault.class.isAssignableFrom(ex.getClass())) {
             throw (OptimisticLockingFault) ex;
         } else {
             processExceptionBasic(ex);
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_ID_TYPES
      *
      * @return default = MockTOFactory.createIdTypes()
      */
     @Override
     public List<IdTypeTO> getIdTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<IdTypeTO> defaultResponse = MockTOFactory.createIdTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_ID_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_RRR_TYPES
      *
      * @return default = MockTOFactory.createRRRTypes()
      */
     @Override
     public List<RrrTypeTO> getRRRTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<RrrTypeTO> defaultResponse = MockTOFactory.createRRRTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_RRR_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.CHECK_CONNECTION
      *
      * @return default = true
      */
     @Override
     public boolean checkConnection() {
         boolean defaultResponse = true;
         try {
             return getManager().getResponse(ReferenceDataClient.CHECK_CONNECTION,
                     Boolean.class, defaultResponse);
         } catch (Exception ex) {
             if (RuntimeException.class.isAssignableFrom(ex.getClass())) {
                 throw (RuntimeException) ex;
             } else {
                 return false;
             }
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_SERVICE_STATUS_TYPES
      *
      * @return default = MockTOFactory.createServiceStatusTypes()
      */
     @Override
     public List<ServiceStatusTypeTO> getServiceStatusTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<ServiceStatusTypeTO> defaultResponse = MockTOFactory.createServiceStatusTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_SERVICE_STATUS_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_PARTY_ROLES
      *
      * @return default = MockTOFactory.createPartyRoles()
      */
     @Override
     public List<PartyRoleTypeTO> getPartyRoles(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<PartyRoleTypeTO> defaultResponse = MockTOFactory.createPartyRoles();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_PARTY_ROLES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_BA_UNIT_TYPES
      *
      * @return default = MockTOFactory.createBaUnitTypes()
      */
     @Override
     public List<BaUnitTypeTO> getBaUnitTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<BaUnitTypeTO> defaultResponse = MockTOFactory.createBaUnitTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_BA_UNIT_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_RRR_GROUP_TYPES
      *
      * @return default = MockTOFactory.createRRRGroupTypes()
      */
     @Override
     public List<RrrGroupTypeTO> getRRRGroupTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<RrrGroupTypeTO> defaultResponse = MockTOFactory.createRRRGroupTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_RRR_GROUP_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_MORTGAGE_TYPES
      *
      * @return default = MockTOFactory.createMortgageTypes()
      */
     @Override
     public List<MortgageTypeTO> getMortgageTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<MortgageTypeTO> defaultResponse = MockTOFactory.createMortgageTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_MORTGAGE_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_CHANGE_STATUS_TYPES
      *
      * @return default = MockTOFactory.createChangeStatusTypes()
      */
     @Override
     public List<ChangeStatusTypeTO> getChangeStatuTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<ChangeStatusTypeTO> defaultResponse = MockTOFactory.createChangeStatusTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_CHANGE_STATUS_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_PARTY_TYPES
      *
      * @return default = MockTOFactory.createPartyTypes()
      */
     @Override
     public List<PartyTypeTO> getPartyTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<PartyTypeTO> defaultResponse = MockTOFactory.createPartyTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_PARTY_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_SERVICE_ACTION_TYPES
      *
      * @return default = MockTOFactory.createServiceActionTypes()
      */
     @Override
     public List<ServiceActionTypeTO> getServiceActionTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<ServiceActionTypeTO> defaultResponse = MockTOFactory.createServiceActionTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_SERVICE_ACTION_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_SOURCE_BA_UNIT_RELATION_TYPES
      *
      * @return default = MockTOFactory.createSourceBaUnitRelationTypes()
      */
     @Override
     public List<SourceBaUnitRelationTypeTO> getSourceBaUnitRelationTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<SourceBaUnitRelationTypeTO> defaultResponse = MockTOFactory.createSourceBaUnitRelationTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_SOURCE_BA_UNIT_RELATION_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_REGISTRATION_STATUS_TYPES
      *
      * @return default = MockTOFactory.createRegistrationStatusTypes()
      */
     @Override
     public List<RegistrationStatusTypeTO> getRegistrationStatusTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<RegistrationStatusTypeTO> defaultResponse = MockTOFactory.createRegistrationStatusTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_REGISTRATION_STATUS_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_CADASTRE_OBJECT_TYPES
      *
      * @return default = MockTOFactory.createCadastreObjectTypes()
      */
     @Override
     public List<CadastreObjectTypeTO> getCadastreObjectTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<CadastreObjectTypeTO> defaultResponse = MockTOFactory.createCadastreObjectTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_CADASTRE_OBJECT_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_LAND_USE_TYPES
      *
      * @return default = MockTOFactory.createLandUseTypes()
      */
     @Override
     public List<LandUseTypeTO> getLandUseTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<LandUseTypeTO> defaultResponse = MockTOFactory.createLandUseTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_LAND_USE_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_TYPE_ACTIONS
      *
      * @return default = MockTOFactory.createRrrTypeActions()
      */
     @Override
     public List<TypeActionTO> getTypeActions(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<TypeActionTO> defaultResponse = MockTOFactory.createRrrTypeActions();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_TYPE_ACTIONS,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_BR_TECHNICAL_TYPES
      *
      * @return default = MockTOFactory.createBrTechnicalTypes()
      */
     @Override
     public List<BrTechnicalTypeTO> getBrTechnicalTypes(String languageCode) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<BrTechnicalTypeTO> defaultResponse = MockTOFactory.createBrTechnicalTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_BR_TECHNICAL_TYPES,
                     List.class, defaultResponse, languageCode);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_BR_VALIDATION_TARGET_TYPES
      *
      * @return default = MockTOFactory.createBrValidationTargetTypes()
      */
     @Override
     public List<BrValidationTargetTypeTO> getBrValidationTargetTypes(String languageCode) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<BrValidationTargetTypeTO> defaultResponse = MockTOFactory.createBrValidationTargetTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_BR_VALIDATION_TARGET_TYPES,
                     List.class, defaultResponse, languageCode);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_BR_SEVERITY_TYPES
      *
      * @return default = MockTOFactory.createBrSeverityTypes()
      */
     @Override
     public List<BrSeverityTypeTO> getBrSeverityTypes(String languageCode) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<BrSeverityTypeTO> defaultResponse = MockTOFactory.createBrSeverityTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_BR_SEVERITY_TYPES,
                     List.class, defaultResponse, languageCode);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_BA_UNIT_REL_TYPES
      *
      * @return default = MockTOFactory.createBaUnitRelTypes()
      */
     @Override
     public List<BaUnitRelTypeTO> getBaUnitRelTypes(String languageCode) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<BaUnitRelTypeTO> defaultResponse = MockTOFactory.createBaUnitRelTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_BA_UNIT_REL_TYPES,
                     List.class, defaultResponse, languageCode);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.SAVE_REFERENCE_DATA
      *
      * @return default = arg0 param
      */
     @Override
     public AbstractCodeTO saveReferenceData(AbstractCodeTO arg0) throws OptimisticLockingFault, SOLAAccessFault, SOLAFault, UnhandledFault {
         AbstractCodeTO defaultResponse = arg0;
         try {
             return getManager().getResponse(ReferenceDataClient.SAVE_REFERENCE_DATA,
                     AbstractCodeTO.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionUpdate(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_APPLICATION_ACTION_TYPES
      *
      * @return default = MockTOFactory.createApplicationActionTypes()
      */
     @Override
     public List<ApplicationActionTypeTO> getApplicationActionTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<ApplicationActionTypeTO> defaultResponse = MockTOFactory.createApplicationActionTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_APPLICATION_ACTION_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_APPLICATION_STATUS_TYPES
      *
      * @return default = MockTOFactory.createApplicationStatusTypes()
      */
     @Override
     public List<ApplicationStatusTypeTO> getApplicationStatusTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<ApplicationStatusTypeTO> defaultResponse = MockTOFactory.createApplicationStatusTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_APPLICATION_STATUS_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_SOURCE_TYPES
      *
      * @return default = MockTOFactory.createSourceTypes()
      */
     @Override
     public List<SourceTypeTO> getSourceTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<SourceTypeTO> defaultResponse = MockTOFactory.createSourceTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_SOURCE_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_REQUEST_CATEGORY_TYPES
      *
      * @return default = MockTOFactory.createRequestCategoryTypes()
      */
     @Override
     public List<RequestCategoryTypeTO> getRequestCategoryTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<RequestCategoryTypeTO> defaultResponse = MockTOFactory.createRequestCategoryTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_REQUEST_CATEGORY_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_REQUEST_TYPES
      *
      * @return default = MockTOFactory.createRequestTypes()
      */
     @Override
     public List<RequestTypeTO> getRequestTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<RequestTypeTO> defaultResponse = MockTOFactory.createRequestTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_REQUEST_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     @Override
     public List<PresentationFormTypeTO> getPresentationFormTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         throw new UnsupportedOperationException("Method not supported");
     }
 
     @Override
     public List<AvailabilityStatusTO> getAvailabilityStatusList(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         throw new UnsupportedOperationException("Method not supported");
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_GENDER_TYPES
      *
      * @return default = MockTOFactory.createGenderTypes()
      */
     @Override
     public List<GenderTypeTO> getGenderTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<GenderTypeTO> defaultResponse = MockTOFactory.createGenderTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_GENDER_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_COMMUNICATION_TYPES
      *
      * @return default = MockTOFactory.createCommunicationTypes()
      */
     @Override
     public List<CommunicationTypeTO> getCommunicationTypes(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         List<CommunicationTypeTO> defaultResponse = MockTOFactory.createCommunicationTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_COMMUNICATION_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionAccess(ex);
             return null;
         }
     }
 
     @Override
     public List<LeaseConditionTO> getLeaseConditions(String arg0) throws SOLAAccessFault, SOLAFault, UnhandledFault {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /**
      * Response Key = ReferenceDataClient.GET_CLIENT_TYPES
      *
      * @return default = MockTOFactory.createApplicationForms()
      */
     @Override
     public List<ApplicationFormTO> getApplicationForms(String arg0) throws SOLAFault, UnhandledFault {
         List<ApplicationFormTO> defaultResponse = MockTOFactory.createApplicationForms();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_APPLICATION_FORMS,
                    List.class, defaultResponse, arg0);
                    }
        catch(Exception ex){
            processExceptionBasic(ex);
            return null;
        }
    }
    
     @Override
     public List<LandGradeTypeTO> getLandGradeTypes(String arg0) throws SOLAFault, UnhandledFault, SOLAAccessFault {
         List<LandGradeTypeTO> defaultResponse = MockTOFactory.createLandGradeTypes();
         try {
             return getManager().getResponse(ReferenceDataClient.GET_LAND_GRADE_TYPES,
                     List.class, defaultResponse, arg0);
         } catch (Exception ex) {
             processExceptionBasic(ex);
             return null;
         }
     }
 }
