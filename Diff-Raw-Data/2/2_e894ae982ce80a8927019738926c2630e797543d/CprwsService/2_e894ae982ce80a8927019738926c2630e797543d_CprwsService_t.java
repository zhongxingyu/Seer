 /* SVN FILE: $Id: CprwsService.java 5343 2012-09-27 14:56:40Z jvuccolo $ */
 package edu.psu.iam.cpr.service;
 
 import static edu.psu.iam.cpr.core.api.BaseApi.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebParam.Mode;
 import javax.jws.WebResult;
 import javax.jws.WebService;
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.ws.WebServiceContext;
 import javax.xml.ws.handler.MessageContext;
 
 import edu.psu.iam.cpr.core.api.returns.NamesServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.ServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.AddressServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.AffiliationServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.ConfidentialityServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.CredentialServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.EmailAddressServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.FindPersonServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.IAPServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.IdCardPrintEventServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.IdCardServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.MatchCodeServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.PersonIdCardNumberServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.PersonIdentifierServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.PersonLinkageServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.PersonServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.PhoneServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.PhotoServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.RulesServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.TransformServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.UserCommentServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.UseridServiceReturn;
 import edu.psu.iam.cpr.core.database.types.CprServiceName;
 import edu.psu.iam.cpr.core.error.ReturnType;
 import edu.psu.iam.cpr.core.rules.engine.RulesEngineHelper;
 import edu.psu.iam.cpr.core.rules.engine.RulesReturn;
 import edu.psu.iam.cpr.core.util.Utility;
 import edu.psu.iam.cpr.service.impl.AddAddressImpl;
 import edu.psu.iam.cpr.service.impl.AddAffiliationImpl;
 import edu.psu.iam.cpr.service.impl.AddPersonImpl;
 import edu.psu.iam.cpr.service.impl.AddConfidentialityHoldImpl;
 import edu.psu.iam.cpr.service.impl.AddCredentialImpl;
 import edu.psu.iam.cpr.service.impl.AddEmailAddressImpl;
 import edu.psu.iam.cpr.service.impl.AddIdCardImpl;
 import edu.psu.iam.cpr.service.impl.AddIdCardPrintEventImpl;
 import edu.psu.iam.cpr.service.impl.AddNameImpl;
 import edu.psu.iam.cpr.service.impl.AddPersonIdentifierImpl;
 import edu.psu.iam.cpr.service.impl.AddPersonLinkageImpl;
 import edu.psu.iam.cpr.service.impl.AddPhoneImpl;
 import edu.psu.iam.cpr.service.impl.AddPhotoImpl;
 import edu.psu.iam.cpr.service.impl.AddSpecialUseridImpl;
 import edu.psu.iam.cpr.service.impl.AddUserCommentImpl;
 import edu.psu.iam.cpr.service.impl.AddUseridImpl;
 import edu.psu.iam.cpr.service.impl.ArchiveAddressImpl;
 import edu.psu.iam.cpr.service.impl.ArchiveAffiliationImpl;
 import edu.psu.iam.cpr.service.impl.ArchiveConfidentialityHoldImpl;
 import edu.psu.iam.cpr.service.impl.ArchiveCredentialImpl;
 import edu.psu.iam.cpr.service.impl.ArchiveEmailAddressImpl;
 import edu.psu.iam.cpr.service.impl.ArchiveIdCardImpl;
 import edu.psu.iam.cpr.service.impl.ArchiveNameImpl;
 import edu.psu.iam.cpr.service.impl.ArchivePersonIdentifierImpl;
 import edu.psu.iam.cpr.service.impl.ArchivePersonImpl;
 import edu.psu.iam.cpr.service.impl.ArchivePersonLinkageImpl;
 import edu.psu.iam.cpr.service.impl.ArchivePhoneImpl;
 import edu.psu.iam.cpr.service.impl.ArchiveUserCommentImpl;
 import edu.psu.iam.cpr.service.impl.ArchiveUseridImpl;
 import edu.psu.iam.cpr.service.impl.GetAddressImpl;
 import edu.psu.iam.cpr.service.impl.GetAffiliationsImpl;
 import edu.psu.iam.cpr.service.impl.GetConfidentialityImpl;
 import edu.psu.iam.cpr.service.impl.GetCredentialImpl;
 import edu.psu.iam.cpr.service.impl.GetEmailAddressImpl;
 import edu.psu.iam.cpr.service.impl.GetExternalAffiliationsImpl;
 import edu.psu.iam.cpr.service.impl.GetExternalIAPImpl;
 import edu.psu.iam.cpr.service.impl.GetIdCardImpl;
 import edu.psu.iam.cpr.service.impl.GetIdCardNumberImpl;
 import edu.psu.iam.cpr.service.impl.GetIdCardPrintEventImpl;
 import edu.psu.iam.cpr.service.impl.GetInternalAffiliationsImpl;
 import edu.psu.iam.cpr.service.impl.GetNameImpl;
 import edu.psu.iam.cpr.service.impl.GetPSUIAPImpl;
 import edu.psu.iam.cpr.service.impl.GetPersonIdentifierImpl;
 import edu.psu.iam.cpr.service.impl.GetPersonImpl;
 import edu.psu.iam.cpr.service.impl.GetPersonLinkageImpl;
 import edu.psu.iam.cpr.service.impl.GetPhoneImpl;
 import edu.psu.iam.cpr.service.impl.GetPhotoImpl;
 import edu.psu.iam.cpr.service.impl.GetUserCommentsImpl;
 import edu.psu.iam.cpr.service.impl.GetUseridImpl;
 import edu.psu.iam.cpr.service.impl.SearchForPersonImpl;
 import edu.psu.iam.cpr.service.impl.SecurityImpl;
 import edu.psu.iam.cpr.service.impl.SetPrimaryAddressByTypeImpl;
 import edu.psu.iam.cpr.service.impl.SetPrimaryAffiliationImpl;
 import edu.psu.iam.cpr.service.impl.SetPrimaryPhoneByTypeImpl;
 import edu.psu.iam.cpr.service.impl.SetPrimaryUseridImpl;
 import edu.psu.iam.cpr.service.impl.UnarchivePersonImpl;
 import edu.psu.iam.cpr.service.impl.UnarchiveUseridImpl;
 import edu.psu.iam.cpr.service.impl.UpdateAddressImpl;
 import edu.psu.iam.cpr.service.impl.UpdateAffiliationImpl;
 import edu.psu.iam.cpr.service.impl.UpdateEmailAddressImpl;
 import edu.psu.iam.cpr.service.impl.UpdatePersonImpl;
 import edu.psu.iam.cpr.service.impl.UpdatePhoneImpl;
 import edu.psu.iam.cpr.service.impl.UpdateUserCommentImpl;
 
 /**
  * The functions contained in this file provide an implementation for the CPR SOAP-based services.
  * 
  * Copyright 2012 The Pennsylvania State University
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * @package edu.psu.iam.cpr.service
  * @author $Author: jvuccolo $
  * @version $Rev: 5343 $
  * @lastrevision $Date: 2012-09-27 10:56:40 -0400 (Thu, 27 Sep 2012) $
  */
 @WebService(serviceName = "cprws", endpointInterface = "edu.psu.iam.cpr.service.CprwsSEI")
 public class CprwsService implements CprwsSEI {
 
 	/** Web service context */
 	@Resource
 	WebServiceContext wsContext;
 
 	/**
 	 * This function provides the implementation for the AddAddress SOAP web service.
 	 * AddAddress  will allow authorized registration authorities to add an address for a person in the
 	 * Central Person Registry.  The RA must specify the type of address along with address.
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service and to add a particular address type.
 	 * 
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is adding the address, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param addressType the type of address to add
 	 * @param documentType the type of document
 	 * @param address1 line 1 of address
 	 * @param address2 line 2 of address
 	 * @param address3 line 3 of address
 	 * @param city the city of the address
 	 * @param stateOrProvince the state of the address, if US address; Province for non-US addresses
 	 * @param postalCode the postal code of the address. For US address, may include plus4 code.
 	 * @param countryCode the three character country code.
 	 * @param campusCode the two character campus code.
 	 * @param verifyAddressFlag flag to control address validation
 	 * 
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="AddAddress")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn AddAddress(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId, 
 			@WebParam(name="password", mode=Mode.IN) String password, 
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) String identifier, 
 			@WebParam(name="addressType", mode=Mode.IN) String addressType, 
 			@WebParam(name="documentType", mode=Mode.IN) String documentType, 
 			@WebParam(name="address1", mode=Mode.IN) String address1, 
 			@WebParam(name="address2", mode=Mode.IN) String address2, 
 			@WebParam(name="address3", mode=Mode.IN) String address3, 
 			@WebParam(name="city", mode=Mode.IN) String city, 
 			@WebParam(name="stateOrProvince", mode=Mode.IN) String stateOrProvince, 
 			@WebParam(name="postalCode", mode=Mode.IN) String postalCode,
 			@WebParam(name="countryCode", mode=Mode.IN) String countryCode, 
 			@WebParam(name="campusCode", mode=Mode.IN) String campusCode,
 			@WebParam(name="verifyAddressFlag", mode=Mode.IN) String verifyAddressFlag) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String,Object>(11);
         otherParameters.put(ADDRESS_TYPE_KEY, addressType);
         otherParameters.put(DOCUMENT_TYPE_KEY, documentType);
         otherParameters.put(ADDRESS1_KEY, address1);
         otherParameters.put(ADDRESS2_KEY, address2);
         otherParameters.put(ADDRESS3_KEY, address3);
         otherParameters.put(CITY_KEY, city);
         otherParameters.put(STATE_KEY, stateOrProvince);
         otherParameters.put(POSTALCODE_KEY, postalCode);
         otherParameters.put(COUNTRY_KEY, countryCode);
         otherParameters.put(CAMPUS_KEY, campusCode);
         otherParameters.put(VERIFY_ADDRESS_FLAG_KEY, verifyAddressFlag);
 
         return (ServiceReturn) new AddAddressImpl().implementService(
                                 CprServiceName.AddAddress.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                                                                 identifierType, identifier, otherParameters);	
 	}
 	
 	/**
 	 * This function provides the implementation for the ArchiveAddress SOAP web service.  ArchiveAddress will allow 
 	 * authorized registration authorities to archive address information for a person in the Central Person Registry.  
 	 * The RA must specify the type of the address to be archived. Authorization checks are made to determine if the RA agent is 
 	 * allowed to call the service and to archive the particular address type. 
 	 *  
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is requesting the addresses, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param addressType  the type of address to delete
 	 * @param documentType the type of document
 	 * @param groupId the groupId of the address record within the address type
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="ArchiveAddress")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn ArchiveAddress(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId, 
 			@WebParam(name="password", mode=Mode.IN) String password, 
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) String identifier, 
 			@WebParam(name="addressType", mode=Mode.IN) String addressType,
 			@WebParam(name="documentType", mode=Mode.IN) String documentType,
 			@WebParam(name="groupId", mode=Mode.IN) Long groupId) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(3);
         otherParameters.put(ADDRESS_TYPE_KEY, addressType);
         otherParameters.put(DOCUMENT_TYPE_KEY, documentType);
         otherParameters.put(GROUP_ID_KEY, groupId);
 
         return (ServiceReturn) new ArchiveAddressImpl().implementService(
                                         CprServiceName.ArchiveAddress.toString(), request.getRemoteAddr(), principalId, password, 
                                         updatedBy, identifierType, identifier, otherParameters);	
 	}
 	
 	/**
 	 * This function provides the implementation for the GetAddress SOAP web service.  GetAddress will allow 
 	 * authorized registration authorities to obtain address information for a person in the Central Person Registry.  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service. 
 	 *  
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param requestedBy the person (userid) who is requesting the addresses, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param addressType if specified will contain the type of address to be returned.
 	 * @param returnHistory a Y/N flag that indicates whether to return history or not.
 	 * @return AddressServiceReturn object that contains the result of executing the service.
 	 *
 	 * @see edu.psu.iam.cpr.core.api.returns.AddressServiceReturn
 	 * 
 	 */
 	@WebMethod(operationName="GetAddress")
 	@WebResult(name="AddressServiceReturn")
 	public AddressServiceReturn GetAddress(
 			@WebParam(name="principalId", mode=Mode.IN) 
 			String principalId, 
 			@WebParam(name="password", mode=Mode.IN) 
 			String password, 
 			@WebParam(name="requestedBy", mode=Mode.IN) 
 			String requestedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) 
 			String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) 
 			String identifier,
 			@WebParam(name="addressType", mode=Mode.IN)
 			String addressType,
 			@WebParam(name="returnHistory", mode=Mode.IN)
 			String returnHistory) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(ADDRESS_TYPE_KEY, addressType);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
 
         return (AddressServiceReturn) new GetAddressImpl().implementService(
                                         CprServiceName.GetAddress.toString(), request.getRemoteAddr(), principalId, password, 
                                         requestedBy, identifierType, identifier, otherParameters);
 	}
 
 	/**
 	 * This function provides the implementation for the SetPrimaryAddressByType SOAP web service.  SetPrimaryAddressByType will allow 
 	 * authorized registration authorities to specify a primary address within a type for a person in the Central Person Registry.  
 	 * The RA must specify the type of the address, document type if necessary and groupId to be primary. Authorization checks 
 	 * are made to determine if the RA agent is 
 	 * allowed to call the service and to set the primary for the particular address type.
 	 *  
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is requesting the addresses, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier
 	 * @param addressType  the type of address to delete
 	 * @param documentType the type of document
 	 * @param groupId the groupId of the address record within the address type
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="SetPrimaryAddressByType")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn SetPrimaryAddressByType(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId, 
 			@WebParam(name="password", mode=Mode.IN) String password, 
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) String identifier, 
 			@WebParam(name="addressType", mode=Mode.IN) String addressType,
 			@WebParam(name="documentType", mode=Mode.IN) String documentType,
 			@WebParam(name="groupId", mode=Mode.IN) Long groupId) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(3);
         otherParameters.put(ADDRESS_TYPE_KEY, addressType);
         otherParameters.put(DOCUMENT_TYPE_KEY, documentType);
         otherParameters.put(GROUP_ID_KEY, groupId);
 
         return (ServiceReturn) new SetPrimaryAddressByTypeImpl().implementService(
                                         CprServiceName.SetPrimaryAddressByType.toString(), request.getRemoteAddr(), principalId, 
                                         password, updatedBy, identifierType,
                                         identifier, otherParameters);		
 	}
 	
 	/**
 	 * This function provides the implementation for the UpdateAddress SOAP web service.
 	 * UpdateAddress  will allow authorized registration authorities to update an address for a person in the
 	 * Central Person Registry.  The RA must specify the type of address along with address.
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service and to update a particular address type.
 	 *
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is adding the address, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param addressType the type of address to update
 	 * @param documentType the type of document
 	 * @param groupId the groupId of the phone record within the phone type
 	 * @param address1 line 1 of address
 	 * @param address2 line 2 of address
 	 * @param address3 line 3 of address
 	 * @param city the city of the address
 	 * @param stateOrProvince the state of the address, if US address; Province for non-US addresses
 	 * @param postalCode the postal code of the address. For US address, may include plus4 code.
 	 * @param countryCode the three character country code.
 	 * @param campusCode the two character campus code.
 	 * @param verifyAddressFlag flag to control address validation
 	 * 
 	 * 
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="UpdateAddress")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn UpdateAddress(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId, 
 			@WebParam(name="password", mode=Mode.IN) String password, 
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) String identifier, 
 			@WebParam(name="addressType", mode=Mode.IN) String addressType, 
 			@WebParam(name="documentType", mode=Mode.IN) String documentType,
 			@WebParam(name="groupId", mode=Mode.IN) Long groupId, 
 			@WebParam(name="address1", mode=Mode.IN) String address1, 
 			@WebParam(name="address2", mode=Mode.IN) String address2, 
 			@WebParam(name="address3", mode=Mode.IN) String address3, 
 			@WebParam(name="city", mode=Mode.IN) String city, 
 			@WebParam(name="stateOrProvince", mode=Mode.IN) String stateOrProvince, 
 			@WebParam(name="postalCode", mode=Mode.IN) String postalCode,
 			@WebParam(name="countryCode", mode=Mode.IN) String countryCode, 
 			@WebParam(name="campusCode", mode=Mode.IN) String campusCode,
 			@WebParam(name="verifyAddressFlag", mode=Mode.IN) String verifyAddressFlag)  {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(12);
         otherParameters.put(ADDRESS_TYPE_KEY, addressType);
         otherParameters.put(DOCUMENT_TYPE_KEY, documentType);
         otherParameters.put(GROUP_ID_KEY, groupId);
         otherParameters.put(ADDRESS1_KEY, address1);
         otherParameters.put(ADDRESS2_KEY, address2);
         otherParameters.put(ADDRESS3_KEY, address3);
         otherParameters.put(CITY_KEY, city);
         otherParameters.put(STATE_KEY, stateOrProvince);
         otherParameters.put(POSTALCODE_KEY, postalCode);
         otherParameters.put(COUNTRY_KEY, countryCode);
         otherParameters.put(CAMPUS_KEY, campusCode);
         otherParameters.put(VERIFY_ADDRESS_FLAG_KEY, verifyAddressFlag);
 
         return (ServiceReturn) new UpdateAddressImpl().implementService(
                                 CprServiceName.UpdateAddress.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                                                                 identifierType, identifier, otherParameters);		
 	}
 	
 	/**
 	 * This function provides the implementation for the AddAffiliation SOAP web service.
 	 * AddAffiliation  will allow authorized registration authorities to add an affiliation for a person in the
 	 * Central Person Registry.  The RA must specify the type of affiliation.
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service and to add a particular affiliation type.
 	 * 
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is adding the address, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param affiliation the affiliation
 	 * 
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="AddAffiliation")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn AddAffiliation(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId, 
 			@WebParam(name="password", mode=Mode.IN) String password, 
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) String identifier, 
 			@WebParam(name="affiliation", mode=Mode.IN) String affiliation) {
 
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(AFFILIATION_KEY, affiliation);
 
         return (ServiceReturn) new AddAffiliationImpl().implementService(
                                 CprServiceName.AddAffiliation.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                                                                 identifierType, identifier, otherParameters);		
 	}
 
 	/**
 	 * This function provides the implementation for the ArchiveAffiliation SOAP web service.
 	 * ArchiveAffiliation  will allow authorized registration authorities to archive an affiliation for a person in the
 	 * Central Person Registry.  The RA must specify the type of affiliation.
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service and to archive the particular 
 	 * affiliation type.
 	 * 
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is adding the address, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param affiliation the affiliation
 	 * 
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="ArchiveAffiliation")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn ArchiveAffiliation(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId, 
 			@WebParam(name="password", mode=Mode.IN) String password, 
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) String identifier, 
 			@WebParam(name="affiliation", mode=Mode.IN) String affiliation) {
 			
 
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(AFFILIATION_KEY, affiliation);
 
         return (ServiceReturn) new ArchiveAffiliationImpl().implementService(
                                         CprServiceName.ArchiveAffiliation.toString(), request.getRemoteAddr(), principalId, password, 
                                         updatedBy, identifierType,
                                         identifier, otherParameters);		
 	}
 	/**
 	 * This function provides the implementation for the GetAffiliations SOAP web service.
 	 * GetAffiliations  will allow authorized registration authorities to get all affiliation information for a person in the
 	 * Central Person Registry.  Authorization checks are made to determine if the RA agent is allowed to call the service.
 	 * 
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param requestedBy the person (userid) who is adding the address, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * 
 	 * @return AffiliationServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.AffiliationServiceReturn
 	 
 	 */
 	@WebMethod(operationName="GetAffiliations")
 	@WebResult(name="AffiliationServiceReturn")
 	public AffiliationServiceReturn GetAffiliations(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId, 
 			@WebParam(name="password", mode=Mode.IN) String password, 
 			@WebParam(name="requestedBy", mode=Mode.IN) String requestedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) String identifier) {
 
 		final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 		return (AffiliationServiceReturn) new GetAffiliationsImpl().implementService(
 						CprServiceName.GetAffiliations.toString(), request.getRemoteAddr(), principalId, password, requestedBy, identifierType, 
 						identifier, null);
 	}
 
 	 /**
 	 * This function provides the implementation for the GetExternalAffiliations SOAP web service.
 	 * GetExternalAffiliations  will allow authorized registration authorities to get all external affiliation information for a person in the
 	 * Central Person Registry.  Authorization checks are made to determine if the RA agent is allowed to call the service.
 	 * 
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param requestedBy the person (userid) who is adding the address, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * 
 	 * @return AffiliationServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.AffiliationServiceReturn
 	 
 	 */
 	@WebMethod(operationName="GetExternalAffiliations")
 	@WebResult(name="AffiliationServiceReturn")
 	public AffiliationServiceReturn GetExternalAffiliations(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId, 
 			@WebParam(name="password", mode=Mode.IN) String password, 
 			@WebParam(name="requestedBy", mode=Mode.IN) String requestedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) String identifier) {
 
 		final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 		return (AffiliationServiceReturn) new GetExternalAffiliationsImpl().implementService(
 						CprServiceName.GetExternalAffiliations.toString(), request.getRemoteAddr(), principalId, password, requestedBy, identifierType, 
 						identifier, null);
 	}
 
 	/**
 	 * This function provides the implementation for the GetInternalAffiliations SOAP web service.
 	 * GetInternalAffiliations  will allow authorized registration authorities to get internal, Penn State,  affiliation information for a person in the
 	 * Central Person Registry.  Authorization checks are made to determine if the RA agent is allowed to call the service.
 	 * 
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param requestedBy the person (userid) who is adding the address, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param returnHistory Y/N flag that will indicate whether to return history or not.
 	 * @return AffiliationServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.AffiliationServiceReturn
 	 
 	 */
 	@WebMethod(operationName="GetInternalAffiliations")
 	@WebResult(name="AffiliationServiceReturn")
 	public AffiliationServiceReturn GetInternalAffiliations(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId, 
 			@WebParam(name="password", mode=Mode.IN) String password, 
 			@WebParam(name="requestedBy", mode=Mode.IN) String requestedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) String identifier,
 			@WebParam(name="returnHistory", mode=Mode.IN) String returnHistory) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
 
         return (AffiliationServiceReturn) new GetInternalAffiliationsImpl().implementService(
                                         CprServiceName.GetInternalAffiliations.toString(), request.getRemoteAddr(), principalId, 
                                         password, requestedBy, identifierType,
                                         identifier, otherParameters);		
 	}
 	
 	/**
 	 * This function provides the implementation for the UpdateAffiliation SOAP web service.
 	 * UpdateAffiliation  will allow authorized registration authorities to update an affiliation for a person in the
 	 * Central Person Registry.  The RA must specify the type of affiliation.
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service and to update a particular affiliation type.
 	 * 
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is adding the address, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param affiliation the affiliation
 	 * 
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="UpdateAffiliation")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn UpdateAffiliation(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId, 
 			@WebParam(name="password", mode=Mode.IN) String password, 
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) String identifier, 
 			@WebParam(name="affiliation", mode=Mode.IN) String affiliation) {
 			
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(AFFILIATION_KEY, affiliation);
 
         return (ServiceReturn) new UpdateAffiliationImpl().implementService(
                                 CprServiceName.UpdateAffiliation.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                                                                 identifierType, identifier, otherParameters);		
 	}
 	/**
 	 * This function provides the implementation for the SetPrimaryAffiliation SOAP web service.
 	 * SetPrimryAffiliation  will allow authorized registration authorities to specify primary affiliation for a person in the
 	 * Central Person Registry.  The RA must specify the type of affiliation.
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service and to update a particular affiliation type.
 	 * 
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is adding the address, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param affiliation the affiliation
 	 * 
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 *
 	 */
 	@WebMethod(operationName="SetPrimaryAffiliation")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn SetPrimaryAffiliation(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId, 
 			@WebParam(name="password", mode=Mode.IN) String password, 
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) String identifier, 
 			@WebParam(name="affiliation", mode=Mode.IN) String affiliation) {
 			
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(AFFILIATION_KEY, affiliation);
 
         return (ServiceReturn) new SetPrimaryAffiliationImpl().implementService(
                                         CprServiceName.SetPrimaryAffiliation.toString(), request.getRemoteAddr(), principalId, 
                                         password, updatedBy, identifierType,
                                         identifier, otherParameters);		
 	}
 	
 	/**
 	 * This function provides the implementation for the AddConfidentialityHold SOAP web service.  AddConfidentialityHold will allow 
 	 * authorized registration authorities to add a confidentiality hold to a person in the Central Person Registry.  
 	 * The RA must specify the confidentiality type and the expiration date.  Authorization checks are made to determine 
 	 * if the RA agent is allowed to call the service and to add the hold.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy contains the system principal and/or userid that requested the confidentiality hold
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @param confidentialityType contains the type of confidentiality hold.
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="AddConfidentialityHold")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn AddConfidentialityHold(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="confidentialityType", mode=Mode.IN)
 			String confidentialityType) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(CONFIDENTIALITY_TYPE_KEY, confidentialityType);
 
         return (ServiceReturn) new AddConfidentialityHoldImpl().implementService(
                                 CprServiceName.AddConfidentialityHold.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                                                                 identifierType, identifier, otherParameters);		
 	}
 
 	/**
 	 * This function provides the implementation for the UpdateConfidentialityHold SOAP web service.  UpdateConfidentialityHold will allow 
 	 * authorized registration authorities to update a confidentiality hold for a person in the Central Person Registry.  
 	 * The RA must specify the confidentiality type and the expiration date.  Authorization checks are made to determine 
 	 * if the RA agent is allowed to call the service and to add the hold.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy contains the system principal and/or userid that requested the confidentiality hold
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @param confidentialityType contains the type of confidentiality hold.
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="UpdateConfidentialityHold")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn UpdateConfidentialityHold(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="confidentialityType", mode=Mode.IN)
 			String confidentialityType) {
 		
 		return AddConfidentialityHold(principalId, password, updatedBy, identifierType, identifier, confidentialityType);
 	}
 
 	/**
 	 * This function provides the implementation for the ArchiveConfidentialityHold SOAP web service.  ArchiveConfidentialityHold will allow 
 	 * authorized registration authorities to archive a confidentiality hold for a person in the Central Person Registry.  
 	 * The RA must specify the confidentiality type to be archived.  Authorization checks are made to determine 
 	 * if the RA agent is allowed to call the service and to perform the archive..
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy contains the system principal and/or userid that requested the confidentiality hold to be archived.
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @param confidentialityType contains the type of confidentiality hold.
 	 * @return ConfidentialityServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="ArchiveConfidentialityHold")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn ArchiveConfidentialityHold(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="confidentialityType", mode=Mode.IN)
 			String confidentialityType) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(CONFIDENTIALITY_TYPE_KEY, confidentialityType);
 
         return (ServiceReturn) new ArchiveConfidentialityHoldImpl().implementService(
                                         CprServiceName.ArchiveConfidentialityHold.toString(), request.getRemoteAddr(), principalId, 
                                         password, updatedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 
 	/**
 	 * This function provides the implementation for the GEtConfidentialityHold SOAP web service.  GetConfidentialityHold will allow 
 	 * authorized registration authorities to obtain a list of confidentiality holds for a person in the Central Person Registry.  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service and to perform the get.  This 
 	 * service will only return the active holds that a user has.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param requestedBy contains the system principal and/or userid that requested the confidentiality hold to be archived.
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @param returnHistory Y/N flag that indicates whether history is to be returned or not.
 	 * @return ConfidentialityServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ConfidentialityServiceReturn
 	 */
 	@WebMethod(operationName="GetConfidentialityHold")
 	@WebResult(name="ConfidentialityServiceReturn")
 	public ConfidentialityServiceReturn GetConfidentialityHold(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="requestedBy", mode=Mode.IN)
 			String requestedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="returnHistory", mode=Mode.IN)
 			String returnHistory) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
 
         return (ConfidentialityServiceReturn) new GetConfidentialityImpl().implementService(
                                         CprServiceName.GetConfidentialityHold.toString(), request.getRemoteAddr(), principalId, 
                                         password, requestedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 
 	/**
 	 * This function provides the implementation for the AddCredential SOAP web service.  AddCredential will allow 
 	 * authorized registration authorities to add a new credential for a person in the Central Person Registry.  
 	 * The RA must specify the type of the credential, along with its associated data.  Authorization checks are made 
 	 * to determine if the RA agent is allowed to call the service and to add a particular credential type.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param credentialType the type of credential that is being added.
 	 * @param credentialData the data associated with the credential.
 	 * @return will return a ServiceReturn instance.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 * 
 	 */
 	@WebMethod(operationName="AddCredential")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn AddCredential(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password,
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="credentialType", mode=Mode.IN)
 			String credentialType, 
 			@WebParam( name="credentialData", mode=Mode.IN)
 			String credentialData) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(CREDENTIAL_TYPE_KEY, credentialType);
         otherParameters.put(CREDENTIAL_DATA_KEY, credentialData);
 
         return (ServiceReturn) new AddCredentialImpl().implementService(
                                 CprServiceName.AddCredential.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                                                                 identifierType, identifier, otherParameters);	
 	}
 
 	/**
 	 * This function provides the implementation for the UpdateCredential SOAP web service.  UpdateCredential will allow 
 	 * authorized registration authorities to update a credential for a person in the Central Person Registry.  
 	 * The RA must specify the type of the credential, along with its associated data.  Authorization checks are made 
 	 * to determine if the RA agent is allowed to call the service and to update the particular credential type.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param credentialType the type of credential that is being updated.
 	 * @param credentialData the data associated with the credential.
 	 * @return ServiceReturn
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 * 
 	 */
 	@WebMethod(operationName="UpdateCredential")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn UpdateCredential(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password,
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="credentialType", mode=Mode.IN)
 			String credentialType, 
 			@WebParam( name="credentialData", mode=Mode.IN)
 			String credentialData) { 
 		
 		return AddCredential(principalId, password, updatedBy, identifierType, identifier, credentialType, credentialData);
 	}
 
 	/**
 	 * This function provides the implementation for the ArchiveCredential SOAP web service.  ArchiveCredential will allow 
 	 * authorized registration authorities to archive a credential for a person in the Central Person Registry.  
 	 * The RA must specify the type of the credential, along with its associated data.  Authorization checks are made 
 	 * to determine if the RA agent is allowed to call the service and to archive the particular credential type.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param credentialType the type of credential that is being archived.
 	 * @return will return a ServiceReturn object upon success.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 * 
 	 */
 	@WebMethod(operationName="ArchiveCredential")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn ArchiveCredential(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password,
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="credentialType", mode=Mode.IN)
 			String credentialType) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(CREDENTIAL_TYPE_KEY, credentialType);
 
         return (ServiceReturn) new ArchiveCredentialImpl().implementService(
                                         CprServiceName.ArchiveCredential.toString(), request.getRemoteAddr(), principalId, 
                                         password, updatedBy, identifierType,
                                         identifier, otherParameters);	
     }
 
 	/**
 	 * This function provides the implementation for the GetCredential SOAP web service.  GetCredential will allow 
 	 * authorized registration authorities to retrieve all of the active credentials for a person in the Central Person Registry.  
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param requestedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param credentialType contains the type of credential to be returned, if specified.
 	 * @param returnHistory Y/N flag that indicates whether history is to be returned or not.
 	 * @return ServiceReturn
 	 * @see edu.psu.iam.cpr.core.api.returns.CredentialServiceReturn
 	 * 
 	 */
 	@WebMethod(operationName="GetCredential")
 	@WebResult(name="CredentialServiceReturn")
 	public CredentialServiceReturn GetCredential(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password,
 			@WebParam( name="requestedBy", mode=Mode.IN)
 			String requestedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="credentialType", mode=Mode.IN)
 			String credentialType,
 			@WebParam( name="returnHistory", mode=Mode.IN)
 			String returnHistory) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(CREDENTIAL_TYPE_KEY, credentialType);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
 
         return (CredentialServiceReturn) new GetCredentialImpl().implementService(
                                         CprServiceName.GetCredential.toString(), request.getRemoteAddr(), principalId, password, 
                                         requestedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 
 	/**
 	 * This function provides the implementation for the AddEmailAddress SOAP web service.
 	 * AddEmailAddress will allow authorized registration authorities to be able to add a
 	 * new e-mail address for a person in the Central Person Registry.
 	 * The RA must specify the type of the e-mail address, along with the e-mail address.
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service 
 	 * and to add a particular e-mail address type.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password contains the password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType contains the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param emailAddressType contains the type of email address that is being added.
 	 * @param emailAddress contains the email address
 	 * 
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="AddEmailAddress")
 	@WebResult(name="ServiceReturn")
 	
 	public ServiceReturn AddEmailAddress(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId,
 			@WebParam(name="password", mode=Mode.IN) String password,
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType,
 			@WebParam(name="identifier", mode=Mode.IN) String identifier,
 			@WebParam(name="emailAddressType", mode=Mode.IN) String emailAddressType,
 			@WebParam(name="emailAddress", mode=Mode.IN) String emailAddress ) {
 
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(EMAIL_ADDRESS_TYPE_KEY, emailAddressType);
         otherParameters.put(EMAIL_ADDRESS_KEY, emailAddress);
 
         return (ServiceReturn) new AddEmailAddressImpl().implementService(
                                 CprServiceName.AddEmailAddress.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                                                                 identifierType, identifier, otherParameters);	
 	}
 
 	/**
 	 * This function provides the implementation for the UpdateEmailAddress SOAP web service.
 	 * UpdateEmailAddress will allow authorized registration authorities to be able to update
 	 * an e-mail address for a person in the Central Person Registry.  The RA must specify the
 	 * type of the e-mail address, along with the e-mail address.  Authorization checks are made
 	 * to determine if the RA agent is allowed to call the service and to update a particular 
 	 * e-mail address type.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password contains the password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType contains the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param emailAddressType contains the type of email address that is being updated.
 	 * @param emailAddress contains the email address
 	 * 
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="UpdateEmailAddress")
 	@WebResult(name="ServiceReturn")
 	
 	public ServiceReturn UpdateEmailAddress(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId,
 			@WebParam(name="password", mode=Mode.IN) String password,
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType,
 			@WebParam(name="identifier", mode=Mode.IN) String identifier,
 			@WebParam(name="emailAddressType", mode=Mode.IN) String emailAddressType,
 			@WebParam(name="emailAddress", mode=Mode.IN) String emailAddress ) {
 
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(EMAIL_ADDRESS_TYPE_KEY, emailAddressType);
         otherParameters.put(EMAIL_ADDRESS_KEY, emailAddress);
 
         return (ServiceReturn) new UpdateEmailAddressImpl().implementService(
                                 CprServiceName.UpdateEmailAddress.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                                                                 identifierType, identifier, otherParameters);
 	}
 
 
 	/**
 	 * This function provides the implementation for the GetEmailAddress SOAP web service.  
 	 * GetEmailAddress will allow authorized registration authorities to be able to obtain 
 	 * an e-mail address for a person in the Central Person Registry.  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password contains the password for the service principal identifier.
 	 * @param requestedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType contains the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param returnHistory Y/N that indicates whether to return history records or not.
 	 * @return EmailAddressServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.EmailAddressServiceReturn
 	 * 
 	 */
 	@WebMethod(operationName="GetEmailAddress")
 	@WebResult(name="EmailAddressServiceReturn")
 
 	public EmailAddressServiceReturn GetEmailAddress(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId,
 			@WebParam(name="password", mode=Mode.IN) String password,
 			@WebParam(name="requestedBy", mode=Mode.IN) String requestedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType,
 			@WebParam(name="identifier", mode=Mode.IN) String identifier,
 			@WebParam(name="returnHistory", mode=Mode.IN) String returnHistory) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
 
         return (EmailAddressServiceReturn) new GetEmailAddressImpl().implementService(
                                         CprServiceName.GetEmailAddress.toString(), request.getRemoteAddr(), principalId, 
                                         password, requestedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 	
 	/**
 	 * This function provides the implementation for the ArchiveEmailAddress SOAP web service.
 	 * ArchiveEmailAddress will allow authorized registration authorities to be able to archive
 	 * an e-mail address for a person in the Central Person Registry.  The RA must specify the
 	 * type of the e-mail address.  Authorization checks are made to determine if the RA agent
 	 * is allowed to call the service and to archive a particular e-mail address type.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password contains the password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType contains the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param emailAddressType contains the type of email address that is being archived.
 	 * 
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="ArchiveEmailAddress")
 	@WebResult(name="ServiceReturn")
 
 	public ServiceReturn ArchiveEmailAddress(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId,
 			@WebParam(name="password", mode=Mode.IN) String password,
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType,
 			@WebParam(name="identifier", mode=Mode.IN) String identifier,
 			@WebParam(name="emailAddressType", mode=Mode.IN) String emailAddressType  ) {
 
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
        otherParameters.put(EMAIL_ADDRESS_TYPE_KEY, emailAddressType);
 
         return (ServiceReturn) new ArchiveEmailAddressImpl().implementService(
                                         CprServiceName.ArchiveEmailAddress.toString(), request.getRemoteAddr(), principalId, 
                                         password, updatedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 	
 	/** 
 	 * 
 	 * Implementation for service SearchForPerson
 	 * 
 	 * @param principalId The principal requesting the service. Mandatory.
 	 * @param password The password for principalId. Mandatory.
 	 * @param requestedBy The user requesting the service. Mandatory.
 	 * @param psuId The Penn State id number of the user to be found. Optional.
 	 * @param userId The userId of the user to be found. Optional.
 	 * @param ssn The SSN of the user to be found. Optional.
 	 * @param firstName The first name of the user to be found. Mandatory.
 	 * @param lastName The last name of the user to be found. Mandatory.
 	 * @param middleName The middle name of the user to be found. Optional.
 	 * @param address1 First line of the street address of the user to be found. Optional.
 	 * @param address2 Second line of the street address of the user to be found. Optional.
 	 * @param address3 Third line of the street address of the user to be found. Optional.
 	 * @param city City of the user to be found. Optional.
 	 * @param state State or province of the user to be found. Optional.
 	 * @param postalCode Zip/Postcode of the user to be found. Optional.
 	 * @param plus4 Extra zipcode information. Optional.
 	 * @param country Country for the street address. Optional. If blank, "USA" is assumed.
 	 * @param dateOfBirth full birth date in format mm/dd/yyyy or partial birth date in format mm/dd
 	 * @param gender Gender for the user to be found. Optional.
 	 * @param rankCutOff The minimum ranking to be considered a positive match. If no value provided, 
 	 * then cut off score will be determined by default cutoff rankings from the standard and international criteria matrices
 	 *  
 	 * @return FindPersonServiceReturn
 	 * 
 	 */
 
 	@WebMethod(operationName="SearchForPerson")
 	@WebResult(name="FindPersonServiceReturn")
 	public FindPersonServiceReturn SearchForPerson(
 		@WebParam(name="principalId", mode=Mode.IN) String principalId,
 		@WebParam(name="password", mode=Mode.IN) String password,
 		@WebParam(name="requestedBy", mode=Mode.IN) String requestedBy,
 		@WebParam(name="psuId", mode=Mode.IN) String psuId, 
 		@WebParam(name="userId", mode=Mode.IN) String userId, 
 		@WebParam(name="ssn", mode=Mode.IN) String ssn, 
 		@WebParam(name="firstName", mode=Mode.IN) String firstName,
 		@WebParam(name="lastName", mode=Mode.IN) String lastName, 
 		@WebParam(name="middleName", mode=Mode.IN) String middleName,
 		@WebParam(name="address1", mode=Mode.IN) String address1,
 		@WebParam(name="address2", mode=Mode.IN) String address2, 
 		@WebParam(name="address3", mode=Mode.IN) String address3,
 		@WebParam(name="city", mode=Mode.IN) String city, 
 		@WebParam(name="state", mode=Mode.IN) String state, 
 		@WebParam(name="postalCode", mode=Mode.IN) String postalCode,
 		@WebParam(name="plus4", mode=Mode.IN) String plus4,
 		@WebParam(name="country", mode=Mode.IN) String country, 
 		@WebParam(name="dateOfBirth", mode=Mode.IN) String dateOfBirth,
 		@WebParam(name="gender", mode=Mode.IN) String gender, 
 		@WebParam(name="rankCutOff", mode=Mode.IN) String rankCutOff) {
 
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String,Object>();
         otherParameters.put(PSUID_KEY, psuId);
         otherParameters.put(USERID_KEY, userId);
         otherParameters.put(SSN_KEY, ssn);
         otherParameters.put(FIRST_NAME_KEY, firstName);
         otherParameters.put(LAST_NAME_KEY, lastName);
         otherParameters.put(MIDDLE_NAMES_KEY, middleName);
         otherParameters.put(ADDRESS1_KEY, address1);
         otherParameters.put(ADDRESS2_KEY, address2);
         otherParameters.put(ADDRESS3_KEY, address3);
         otherParameters.put(CITY_KEY, city);
         otherParameters.put(STATE_KEY, state);
         otherParameters.put(POSTALCODE_KEY, postalCode);
         otherParameters.put(PLUS4_KEY, plus4);
         otherParameters.put(COUNTRY_KEY, country);
         otherParameters.put(DOB_KEY, dateOfBirth);
         otherParameters.put(GENDER_KEY, gender);
         otherParameters.put(RANK_CUTOFF_KEY, rankCutOff);
 
         return (FindPersonServiceReturn) new SearchForPersonImpl().implementService(
                                         CprServiceName.FindPerson.toString(), request.getRemoteAddr(), principalId, 
                                         password, requestedBy, null,
                                         null, otherParameters);
 	}
 
 	/**
 	 * This function implements the GetPSUIAP SOAP Web service.
 	 * GetPSUIAP  will allow authorized registration authorities to get internal, Penn State,
 	 * IAP information for a person in the Central Person Registry.  Authorization checks are made 
 	 * to determine if the RA agent is allowed to call the service.
 	 * 
 	 * @param principalId the service principal's identifier. 
 	 * @param password the service principal's password.
 	 * @param requestedBy the person (userid) who requested the service, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR.
 	 * @param identifier the identifier used to identify the user within the CPR.
 	 * @param userId the userid
 	 * @param returnHistory Y/N flag that indicates whether to return history or not.
 	 * @return IAPServiceReturn  object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.IAPServiceReturn
 	 */
 	@WebMethod(operationName="GetPSUIAP")
 	@WebResult(name="IAPServiceReturn")
 	public IAPServiceReturn GetPSUIAP(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId,
 			@WebParam(name="password", mode=Mode.IN) String password,
 			@WebParam(name="requestedBy", mode=Mode.IN) String requestedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType,
 			@WebParam(name="identifier", mode=Mode.IN) String identifier,
 			@WebParam(name="userId", mode=Mode.IN) String userId,
 			@WebParam(name="returnHistory", mode=Mode.IN) String returnHistory)
 	{
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(USERID_KEY, userId);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
 
         return (IAPServiceReturn) new GetPSUIAPImpl().implementService(
                                         CprServiceName.GetPSUIAP.toString(), request.getRemoteAddr(), principalId, 
                                         password, requestedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 
 	/**
 	 * This function implements the GetExternalIAP SOAP Web service.
 	 * GetExternalIAP  will allow authorized registration authorities to get external IAP information
 	 * for a person in the Central Person Registry.  Authorization checks are made 
 	 * to determine if the RA agent is allowed to call the service.
 	 * @param principalId the service principal's identifier. 
 	 * @param password the service principal's password.
 	 * @param requestedBy the person (userid) who requested the service, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR.
 	 * @param identifier the identifier used to identify the user within the CPR.
 	 * @param userId the userid
 	 * @param federationName the Federation Name
 	 * 
 	 * @return IAPServiceReturn  object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.IAPServiceReturn
 	 */
 	@WebMethod(operationName="GetExternalIAP")
 	@WebResult(name="IAPServiceReturn")
 	public IAPServiceReturn GetExternalIAP(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId,
 			@WebParam(name="password", mode=Mode.IN) String password,
 			@WebParam(name="requestedBy", mode=Mode.IN) String requestedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType,
 			@WebParam(name="identifier", mode=Mode.IN) String identifier,
 			@WebParam(name="userId", mode=Mode.IN) String userId,
 			@WebParam(name="federationName", mode=Mode.IN) String federationName)
 	{
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(USERID_KEY, userId);
         otherParameters.put(FEDERATION_NAME_KEY, federationName);
 
         return (IAPServiceReturn) new GetExternalIAPImpl().implementService(
                                         CprServiceName.GetExternalIAP.toString(), request.getRemoteAddr(), principalId, 
                                         password, requestedBy, identifierType,
                                         identifier, otherParameters);
 	}
 
 	/**
 	 * This function provides the implementation for the AddIdCard SOAP web service.  AddIdCard will allow 
 	 * authorized registration authorities to add Id Card information for a person in the Central Person Registry  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is requesting the information, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param idCardType the type of id card to return
 	 * @param idCardNumber the id card number associated 
 	 * @param idSerialNumber the serial number, if any, associated with the card
 	 * @param photo the photo, if any, on the id card
 	 * @param photoDateTaken date photo was taken
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName = "AddIdCard")
 	@WebResult(name = "ServiceReturn")
 	public ServiceReturn AddIdCard(
 			@WebParam(name = "principalId", mode = Mode.IN) String principalId,
 			@WebParam(name = "password", mode = Mode.IN) String password,
 			@WebParam(name = "updatedBy", mode = Mode.IN) String updatedBy,
 			@WebParam(name = "identifierType", mode = Mode.IN) String identifierType,
 			@WebParam(name = "identifier", mode = Mode.IN) String identifier,
 			@WebParam(name = "IdCardType", mode = Mode.IN) String idCardType,
 			@WebParam(name = "idCardNumber", mode = Mode.IN) String idCardNumber,
 			@WebParam(name = "idSerialNumber", mode = Mode.IN) String idSerialNumber,
 			@WebParam(name = "photo", mode = Mode.IN) byte[] photo,
 			@WebParam(name = "photoDateTaken", mode = Mode.IN) String photoDateTaken) {
 		
 		final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 		
 		final Map<String,Object> otherParameters = new HashMap<String,Object>(5);
 		otherParameters.put(ID_CARD_TYPE_KEY, idCardType);
 		otherParameters.put(ID_CARD_NUMBER_KEY, idCardNumber);
 		otherParameters.put(ID_SERIAL_NUMBER_KEY, idSerialNumber);
 		otherParameters.put(PHOTO_KEY, photo);
 		otherParameters.put(PHOTO_DATE_TAKEN_KEY, photoDateTaken);
 		
 		return (ServiceReturn) new AddIdCardImpl().implementService(
 					CprServiceName.AddIdCard.toString(), request.getRemoteAddr(), principalId, password, updatedBy, 
 									identifierType, identifier, otherParameters);
 	}
 	
 	/**
 	 * This function provides the implementation for the ArchiveIdCard SOAP web service.  ArchiveIdCard will allow 
 	 * authorized registration authorities to archive an active Id Card information for a person in the Central Person Registry  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is requesting the information, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param idCardType the type of id card to return
 	 * @return will return a ServiceReturn object upon successful completion of the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName = "ArchiveIdCard")
 	@WebResult(name = "ServiceReturn")
 	public ServiceReturn ArchiveIdCard(
 			@WebParam(name = "principalId", mode = Mode.IN) String principalId,
 			@WebParam(name = "password", mode = Mode.IN) String password,
 			@WebParam(name = "updatedBy", mode = Mode.IN) String updatedBy,
 			@WebParam(name = "identifierType", mode = Mode.IN) String identifierType,
 			@WebParam(name = "identifier", mode = Mode.IN) String identifier,
 			@WebParam(name = "idCardType", mode = Mode.IN) String idCardType) {
 		
 		final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 		
 		final Map<String,Object> otherParameters = new HashMap<String,Object>(1);
 		otherParameters.put(ID_CARD_TYPE_KEY, idCardType);
 		
 		return (ServiceReturn) new ArchiveIdCardImpl().implementService(
 						CprServiceName.ArchiveIdCard.toString(), request.getRemoteAddr(), principalId, password, updatedBy, identifierType, 
 						identifier, otherParameters);
 	}
 	
 	/**
 	 * This function provides the implementation for the GetIdCard SOAP web service.  GetIdCard will allow 
 	 * authorized registration authorities to obtain id card information information for a person in the Central Person Registry.  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param requestedBy the person (userid) who is requesting the information, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param idCardType contains the id card type to be used for a query, if specified.
 	 * @param returnHistory Y/N flag that indicates whether history is returned or not.
 	 * @return IdCardServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.IdCardServiceReturn
 	 */
 	@WebMethod(operationName="GetIdCard")
 	@WebResult(name="IdCardServiceReturn")
 	public IdCardServiceReturn GetIdCard(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password,
 			@WebParam( name="requestedBy", mode=Mode.IN)
 			String requestedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="idCardType", mode=Mode.IN)
 			String idCardType,
 			@WebParam( name="returnHistory", mode=Mode.IN)
 			String returnHistory) {
 		
 		final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 		
 		final Map<String,Object> otherParameters = new HashMap<String,Object>(2);
 		otherParameters.put(ID_CARD_TYPE_KEY, idCardType);
 		otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
 
 		return (IdCardServiceReturn) new GetIdCardImpl().implementService(
 						CprServiceName.GetIdCard.toString(), request.getRemoteAddr(), principalId, password, requestedBy, identifierType, 
 						identifier, otherParameters);
 	}
 	/**
 	 * This function provides the implementation for the GetIdCardNumber SOAP web service.  GetIdCardNumber will allow 
 	 * authorized registration authorities to obtain Id Card number information for a person in the Central Person Registry.  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param requestedBy the person (userid) who is requesting the information, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 *
 	 * @return IdCardNumberServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.IdCardServiceReturn
 	 */
 	@WebMethod(operationName="GetIdCardNumber")
 	@WebResult(name="IdCardNumberServiceReturn")
 	public PersonIdCardNumberServiceReturn GetIdCardNumber(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password,
 			@WebParam( name="requestedBy", mode=Mode.IN)
 			String requestedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier) {
 		
 		final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 		return (PersonIdCardNumberServiceReturn) new GetIdCardNumberImpl().implementService(
 						CprServiceName.GetIdCardNumber.toString(), request.getRemoteAddr(), principalId, password, requestedBy, identifierType, 
 						identifier,null);
 	}
 	/**
 	 * This function provides the implementation for the UpdateIdCard SOAP web service.  UpdateIdCard will allow 
 	 * authorized registration authorities to update Id Card information for a person in the Central Person Registry  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is requesting the information, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param idCardType the type of id card to return
 	 * @param idCardNumber the id card number associated 
 	 * @param idSerialNumber the serial number, if any, associated with the card
 	 * @param photo the photo, if any, on the id card
 	 * @param photoDateTaken date photo was taken
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName = "UpdateIdCard")
 	@WebResult(name = "ServiceReturn")
 	public ServiceReturn UpdateIdCard(
 			@WebParam(name = "principalId", mode = Mode.IN) String principalId,
 			@WebParam(name = "password", mode = Mode.IN) String password,
 			@WebParam(name = "updatedBy", mode = Mode.IN) String updatedBy,
 			@WebParam(name = "identifierType", mode = Mode.IN) String identifierType,
 			@WebParam(name = "identifier", mode = Mode.IN) String identifier,
 			@WebParam(name = "idCardType", mode = Mode.IN) String idCardType,
 			@WebParam(name = "idCardNumber", mode = Mode.IN) String idCardNumber,
 			@WebParam(name = "idSerialNumber", mode = Mode.IN) String idSerialNumber,
 			@WebParam(name = "photo", mode = Mode.IN) byte[] photo,
 			@WebParam(name = "photoDateTaken", mode = Mode.IN) String photoDateTaken) {
 		return AddIdCard(principalId, password, updatedBy, identifierType, identifier, idCardType, idCardNumber, 
 				idSerialNumber, photo, photoDateTaken);
 	}
 	/**
 	 * This function provides the implementation for the AddIdCardPrintEvent SOAP web service.  AddIdCardPrintEvent will allow 
 	 * authorized registration authorities to add a Id Card Print Log entry information for an id card in the Central Person Registry  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is requesting the information, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR (must be ID_CARD). 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param eventUserId the user that printed the id card
       * @param eventIpAddress the ip address of the workstation where the id card was printed
       * @param eventWorkstation the name of the workstation where the id card was printed.
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName = "AddIdCardPrintEvent")
 	@WebResult(name = "ServiceReturn")
 	public ServiceReturn AddIdCardPrintEvent(
 			@WebParam(name = "principalId", mode = Mode.IN) String principalId,
 			@WebParam(name = "password", mode = Mode.IN) String password,
 			@WebParam(name = "updatedBy", mode = Mode.IN) String updatedBy,
 			@WebParam(name = "identifierType", mode = Mode.IN) String identifierType,
 			@WebParam(name = "identifier", mode = Mode.IN) String identifier,
 			@WebParam(name = "eventUserId", mode = Mode.IN) String eventUserId,
 			@WebParam(name = "eventIpAddress", mode = Mode.IN) String eventIpAddress,
 			@WebParam(name = "eventWorkstation", mode = Mode.IN) String eventWorkstation) {
 		
 		final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 		
 		final Map<String,Object> otherParameters = new HashMap<String,Object>(5);
 		otherParameters.put(IDENTIFIER_TYPE_KEY, identifierType);
 		otherParameters.put(IDENTIFIER_KEY, identifier);
 		otherParameters.put(EVENT_USER_ID_KEY, eventUserId);
 		otherParameters.put(EVENT_IP_ADDRESS_KEY, eventIpAddress);
 		otherParameters.put(EVENT_WORKSTATION_KEY, eventWorkstation);
 
 		return (ServiceReturn) new AddIdCardPrintEventImpl().implementService(
 					CprServiceName.AddIdCardPrintEvent.toString(), request.getRemoteAddr(), principalId, password, updatedBy, 
 									identifierType, identifier, otherParameters);
 	}
 	/**
 	 * This function provides the implementation for the GetIdCardPrintEvent SOAP web service.  GetIdCardPrintEvent will allow 
 	 * authorized registration authorities to get Id Card Print Log entries information for an id card in the Central Person Registry  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param requestedBy the person (userid) who is requesting the information, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR (must be ID_CARD). 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @return GetIdCardPrintEventServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.IdCardPrintEventServiceReturn
 	 */
 	@WebMethod(operationName = "GetIdCardPrintEvent")
 	@WebResult(name = "IdCardPrintEventServiceReturn")
 	public IdCardPrintEventServiceReturn GetIdCardPrintEvent(
 			@WebParam(name = "principalId", mode = Mode.IN) String principalId,
 			@WebParam(name = "password", mode = Mode.IN) String password,
 			@WebParam(name = "requestedBy", mode = Mode.IN) String requestedBy,
 			@WebParam(name = "identifierType", mode = Mode.IN) String identifierType,
 			@WebParam(name = "identifier", mode = Mode.IN) String identifier) {
 		
 
 		final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 		
 		final Map<String,Object> otherParameters = new HashMap<String,Object>(2);
 		otherParameters.put(IDENTIFIER_TYPE_KEY, identifierType);
 		otherParameters.put(IDENTIFIER_KEY, identifier);
 		
 		return (IdCardPrintEventServiceReturn) new GetIdCardPrintEventImpl().implementService(
 						CprServiceName.GetIdCardPrintEvent.toString(), request.getRemoteAddr(), principalId, password, requestedBy, 
 						identifierType, 
 						identifier, otherParameters);
 	}
 	
 	/**
 	 * This function provides the implementation for the AddName SOAP web service.  AddName will allow 
 	 * authorized registration authorities to add a new name for a person in the Central Person Registry.  
 	 * The RA must specify the type of the name, along with the full name.  Authorization checks are made 
 	 * to determine if the RA agent is allowed to call the service and to add a particular name type.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param nameType the type of name that is being added.
 	 * @param documentType the type of documented name that is being added (optional).
 	 * @param firstName the first name that is being added.
 	 * @param middleNames the middle name(s) that are being added.
 	 * @param lastName the last name that is being added.
 	 * @param suffix optionally the suffix that is being added.
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 * 
 	 */
 	@WebMethod(operationName="AddName")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn AddName(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password,
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="nameType", mode=Mode.IN)
 			String nameType, 
 			@WebParam( name="documentType", mode=Mode.IN)
 			String documentType, 
 			@WebParam( name="firstName", mode=Mode.IN)
 			String firstName, 
 			@WebParam( name="middleNames", mode=Mode.IN)
 			String middleNames,
 			@WebParam( name="lastName", mode=Mode.IN)
 			String lastName, 
 			@WebParam( name="suffix", mode=Mode.IN)
 			String suffix) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(6);
         otherParameters.put(NAME_TYPE_KEY, nameType);
         otherParameters.put(DOCUMENT_TYPE_KEY, documentType);
         otherParameters.put(FIRST_NAME_KEY, firstName);
         otherParameters.put(MIDDLE_NAMES_KEY, middleNames);
         otherParameters.put(LAST_NAME_KEY, lastName);
         otherParameters.put(SUFFIX_KEY, suffix);
 
         return (ServiceReturn) new AddNameImpl().implementService(
                                 CprServiceName.AddName.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                                                                 identifierType, identifier, otherParameters);	
 	}
 
 	/**
 	 * This function provides the implementation for the UpdateName SOAP web service.  UpdateName will allow 
 	 * authorized registration authorities to update a name for a person in the Central Person Registry.  
 	 * The RA must specify the type of the name, along with the full name.  Authorization checks are made 
 	 * to determine if the RA agent is allowed to call the service and to update the particular name type.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param nameType the type of name that is being updated.
 	 * @param documentType the type of documented name that is being updated (optional).
 	 * @param firstName the first name that is being added.
 	 * @param middleNames the middle name(s) that are being added.
 	 * @param lastName the last name that is being added.
 	 * @param suffix optionally the suffix that is being added.
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 * 
 	 */
 	@WebMethod(operationName="UpdateName")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn UpdateName(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password,
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="nameType", mode=Mode.IN)
 			String nameType, 
 			@WebParam( name="documentType", mode=Mode.IN)
 			String documentType, 
 			@WebParam( name="firstName", mode=Mode.IN)
 			String firstName, 
 			@WebParam( name="middleNames", mode=Mode.IN)
 			String middleNames,
 			@WebParam( name="lastName", mode=Mode.IN)
 			String lastName, 
 			@WebParam( name="suffix", mode=Mode.IN)
 			String suffix) {
 		
 		return AddName(principalId, password, updatedBy, identifierType, identifier, nameType, documentType, firstName, middleNames,
 				lastName, suffix);
 
 	}
 
 	/**
 	 * This function provides the implementation for the ArchiveName SOAP web service.  ArchiveName will allow 
 	 * authorized registration authorities to archive a name for a person in the Central Person Registry.  
 	 * The RA must specify the type of the name to be archived.  Authorization checks are made 
 	 * to determine if the RA agent is allowed to call the service and to archive the particular name type.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param nameType the type of name that is being archived.
 	 * @param documentType the type of documented name that is being archived (optional).
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="ArchiveName")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn ArchiveName(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password,
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="nameType", mode=Mode.IN)
 			String nameType,
 			@WebParam( name="documentType", mode=Mode.IN)
 			String documentType) {
 
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(NAME_TYPE_KEY, nameType);
         otherParameters.put(DOCUMENT_TYPE_KEY, documentType);
 
         return (ServiceReturn) new ArchiveNameImpl().implementService(
                                         CprServiceName.ArchiveName.toString(), request.getRemoteAddr(), principalId, 
                                         password, updatedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 
 	/**
 	 * This function provides the implementation for the GetName SOAP web service.  GetName will allow 
 	 * authorized registration authorities to obtain name information for a person in the Central Person Registry.  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param requestedBy the person (userid) who is requesting the information, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param nameType contains the type of name to be retrieved, if specified.
 	 * @param returnHistory flag that indicates whether history must be returned or not.
 	 * @return NamesServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.NamesServiceReturn
 	 */
 	@WebMethod(operationName="GetName")
 	@WebResult(name="NamesServiceReturn")
 	public NamesServiceReturn GetName(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password,
 			@WebParam( name="requestedBy", mode=Mode.IN)
 			String requestedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="nameType", mode=Mode.IN)
 			String nameType,
 			@WebParam( name="returnHistory", mode=Mode.IN)
 			String returnHistory) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(NAME_TYPE_KEY, nameType);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
 
         return (NamesServiceReturn) new GetNameImpl().implementService(
                                         CprServiceName.GetName.toString(), request.getRemoteAddr(), principalId, 
                                         password, requestedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 
 	/**
 	 * This function provides the implementation for the AddPersonLinkage SOAP web service.  
 	 * This service will enable an authorized registration authority to be able to add a linkage between two 
 	 * persons in the person registry.  The calling parameters to the service will specify the linked users 
 	 * along with the linkage type.  If the user already has a linkage of the type specified, it will be expired prior 
 	 * to the new linkage being added.  The service will either return an exception (with the reason the 
 	 * add did not happen) or success. 
 	 * 
 	 * @param principalId contains the service principal used to authenticate the service.
 	 * @param password contains the password for the service principal.
 	 * @param updatedBy contains the system principal and/or userid that requested the add.
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @param linkageType contains the type of linkage that is being added.
 	 * @param linkedIdentifierType contains the type of identifier used to find the person being linked.
 	 * @param linkedIdentifier contains the value of the identifier for the person being linked.
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="AddPersonLinkage")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn AddPersonLinkage(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType,
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="linkageType", mode=Mode.IN)
 			String linkageType, 
 			@WebParam( name="linkedIdentifierType", mode=Mode.IN)
 			String linkedIdentifierType,
 			@WebParam( name="linkedIdentifier", mode=Mode.IN)
 			String linkedIdentifier) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(3);
         otherParameters.put(LINKAGE_TYPE_KEY, linkageType);
         otherParameters.put(LINKAGE_IDENTIFIER_TYPE_KEY, linkedIdentifierType);
         otherParameters.put(LINKAGE_IDENTIFIER_KEY, linkedIdentifier);
 
         return (ServiceReturn) new AddPersonLinkageImpl().implementService(
                                 CprServiceName.AddPersonLinkage.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                                                                 identifierType, identifier, otherParameters);	
 	}
 
 	
 	/**
 	 * This function provides the implementation for the UpdatePersonLinkage SOAP web service.  
 	 * This service will enable an authorized registration authority to be able to update a linkage between two 
 	 * persons in the person registry.  The calling parameters to the service will specify the linked users 
 	 * along with the linkage type.  If the user already has a linkage of the type specified, it will be expired prior 
 	 * to the new linkage being added.  The service will either return an exception (with the reason the 
 	 * add did not happen) or success. 
 	 * 
 	 * @param principalId contains the service principal used to authenticate the service.
 	 * @param password contains the password for the service principal.
 	 * @param updatedBy contains the system principal and/or userid that requested the add.
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @param linkageType contains the type of linkage that is being added.
 	 * @param linkedIdentifierType contains the type of identifier used to find the person being linked.
 	 * @param linkedIdentifier contains the value of the identifier for the person being linked.
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="UpdatePersonLinkage")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn UpdatePersonLinkage(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType,
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="linkageType", mode=Mode.IN)
 			String linkageType, 
 			@WebParam( name="linkedIdentifierType", mode=Mode.IN)
 			String linkedIdentifierType,
 			@WebParam( name="linkedIdentifier", mode=Mode.IN)
 			String linkedIdentifier) {
 		
 		return AddPersonLinkage(principalId, password, updatedBy, identifierType, identifier, linkageType, linkedIdentifierType, linkedIdentifier);
 	}
 
 	/**
 	 * This function provides the implementation for the ArchivePersonLinkage SOAP web service.  
 	 * This service will enable an authorized registration authority to be able to archive a linkage between two 
 	 * persons in the person registry.
 	 * 
 	 * @param principalId contains the service principal used to authenticate the service.
 	 * @param password contains the password for the service principal.
 	 * @param updatedBy contains the system principal and/or userid that requested the archive.
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @param linkageType contains the type of linkage that is being archived.
 	 * @param linkedIdentifierType contains the type of identifier used to find the person being linked.
 	 * @param linkedIdentifier contains the value of the identifier for the person being linked.
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="ArchivePersonLinkage")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn ArchivePersonLinkage(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType,
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="linkageType", mode=Mode.IN)
 			String linkageType, 
 			@WebParam( name="linkedIdentifierType", mode=Mode.IN)
 			String linkedIdentifierType,
 			@WebParam( name="linkedIdentifier", mode=Mode.IN)
 			String linkedIdentifier) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(3);
         otherParameters.put(LINKAGE_TYPE_KEY, linkageType);
         otherParameters.put(LINKAGE_IDENTIFIER_TYPE_KEY, linkedIdentifierType);
         otherParameters.put(LINKAGE_IDENTIFIER_KEY, linkedIdentifier);
 
         return (ServiceReturn) new ArchivePersonLinkageImpl().implementService(
                                         CprServiceName.ArchivePersonLinkage.toString(), request.getRemoteAddr(), 
                                         principalId, password, updatedBy, identifierType,
                                         identifier, otherParameters);
 	}
 
 	/**
 	 * This function provides the implementation for the GetPersonLinkage SOAP web service.  
 	 * This service will enable an authorized registration authority to be able to obtain linkage
 	 * information about a person in the registry.  This service will only return the active
 	 * person linkages.
 	 * 
 	 * @param principalId contains the service principal used to authenticate the service.
 	 * @param password contains the password for the service principal.
 	 * @param requestedBy contains the system principal and/or userid that requested the get.
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @param returnHistory Y/N flag that indicates whether history is to be returned or not.
 	 * @return PersonLinkageServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.PersonLinkageServiceReturn
 	 */
 	@WebMethod(operationName="GetPersonLinkage")
 	@WebResult(name="PersonLinkageServiceReturn")
 	public PersonLinkageServiceReturn GetPersonLinkage(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="requestedBy", mode=Mode.IN)
 			String requestedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType,
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="returnHistory", mode=Mode.IN)
 			String returnHistory) {
 
 
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
 
         return (PersonLinkageServiceReturn) new GetPersonLinkageImpl().implementService(
                                         CprServiceName.GetPersonLinkage.toString(), request.getRemoteAddr(), 
                                         principalId, password, requestedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 		
 	/**
 	 * This function provides the implementation for the AddPerson SOAP web service.  AddPerson will allow 
 	 * authorized registration authorities to add a new person to the Central Person Registry.  
 	 * The RA must specify some required information about the new person and whether they want to
 	 * generate a PSU ID and/or userid.  Authorization checks are made to determine if the RA agent is 
 	 * allowed to call the service and to add the person.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param assignPsuIdFlag a y/n flag to indiciate whether a new PSU ID is to be created for the person.
 	 * @param assignUseridFlag a y/n flag to indicate whether a new userid is to be created for the person.
 	 * @param gender a flag to indicate the person's gender.
 	 * @param dob the person's date of birth, it can either be a full DOB or a partial one (mm/dd).
 	 * @param nameType the type of name that is being added.
 	 * @param nameDocumentType the document type associated with the name.
 	 * @param firstName the first name that is being added.
 	 * @param middleNames the middle name(s) that are being added.
 	 * @param lastName the last name that is being added.
 	 * @param suffix optionally the suffix that is being added.
 	 * @param addressType the type of address that is being added.
 	 * @param addressDocumentType the document type of the address that is being added.
 	 * @param address1 line number one of the address.
 	 * @param address2 line number two of the address (if it exists).
 	 * @param address3 line number three of the address (if it exists).
 	 * @param city the city of the address.
 	 * @param stateOrProvince the state (if country is USA), or province (otherwise).
 	 * @param postalCode the postal code.
 	 * @param countryCode the ISO country code abbreviation.
 	 * @param campusCode the Penn State campus code for University addresses only.
 	 * @param phoneType the type of phone number being added.
 	 * @param phoneNumber the phone number.
 	 * @param extension the extension for the phone number if applicable.
 	 * @param internationalNumber a y/n flag to indicate whether the phone number is an international number.
 	 * @param emailType the type of email address being added.
 	 * @param emailAddress the email address.
 	 * @param affiliation the affiliation to be added.
 	 * @param ssn contains the user's SSN.
 	 * @return PersonServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.PersonServiceReturn
 	 * 
 	 */
 	@WebMethod(operationName="AddPerson")
 	@WebResult(name="PersonServiceReturn")
 	public PersonServiceReturn AddPerson(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="assignPsuIdFlag", mode=Mode.IN)
 			String assignPsuIdFlag, 
 			@WebParam( name="assignUseridFlag", mode=Mode.IN)
 			String assignUseridFlag, 
 			@WebParam( name="gender", mode=Mode.IN)
 			String gender, 
 			@WebParam( name="dob", mode=Mode.IN)
 			String dob, 
 			@WebParam( name="nameType", mode=Mode.IN)
 			String nameType, 
 			@WebParam(name = "nameDocumentType", mode = Mode.IN) 
 			String nameDocumentType,
 			@WebParam( name="firstName", mode=Mode.IN)
 			String firstName, 
 			@WebParam( name="middleNames", mode=Mode.IN)
 			String middleNames, 
 			@WebParam( name="lastName", mode=Mode.IN)
 			String lastName, 
 			@WebParam( name="suffix", mode=Mode.IN)
 			String suffix,
 			@WebParam( name="addressType", mode=Mode.IN)
 			String addressType, 
 			@WebParam(name = "addressDocumentType", mode = Mode.IN) 
 			String addressDocumentType,
 			@WebParam( name="address1", mode=Mode.IN)
 			String address1, 
 			@WebParam( name="address2", mode=Mode.IN)
 			String address2, 
 			@WebParam( name="address3", mode=Mode.IN)
 			String address3,  
 			@WebParam( name="city", mode=Mode.IN)
 			String city, 
 			@WebParam( name="stateOrProvince", mode=Mode.IN)
 			String stateOrProvince, 
 			@WebParam( name="postalCode", mode=Mode.IN)
 			String postalCode,
 			@WebParam( name="countryCode", mode=Mode.IN)
 			String countryCode, 
 			@WebParam( name="campusCode", mode=Mode.IN)
 			String campusCode,
 			@WebParam(name="verifyAddressFlag", mode = Mode.IN) 
 			String verifyAddressFlag,
 			@WebParam( name="phoneType", mode=Mode.IN)
 			String phoneType, 
 			@WebParam( name="phoneNumber", mode=Mode.IN)
 			String phoneNumber, 
 			@WebParam( name="extension", mode=Mode.IN)
 			String extension, 
 			@WebParam( name="internationalNumber", mode=Mode.IN)
 			String internationalNumber,
 			@WebParam( name="emailType", mode=Mode.IN)
 			String emailType, 
 			@WebParam( name="emailAddress", mode=Mode.IN)
 			String emailAddress,
 			@WebParam(name="affilation", mode=Mode.IN)
 			String affiliation,
 			@WebParam(name="ssn", mode=Mode.IN) 
 			String ssn) {
 		
 		final String doFindPersonFlag = "Y";
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(30);
         otherParameters.put(DO_FIND_PERSON_KEY, doFindPersonFlag);
         otherParameters.put(ASSIGN_PSU_ID_FLAG_KEY, assignPsuIdFlag);
         otherParameters.put(ASSIGN_USERID_FLAG_KEY, assignUseridFlag);
         otherParameters.put(GENDER_KEY, gender);
         otherParameters.put(DOB_KEY, dob);
         otherParameters.put(NAME_TYPE_KEY, nameType);
         otherParameters.put(NAME_DOCUMENT_TYPE_KEY, nameDocumentType);
         otherParameters.put(FIRST_NAME_KEY, firstName);
         otherParameters.put(MIDDLE_NAMES_KEY, middleNames);
         otherParameters.put(LAST_NAME_KEY, lastName);
         otherParameters.put(SUFFIX_KEY, suffix);
         otherParameters.put(ADDRESS_TYPE_KEY, addressType);
         otherParameters.put(ADDRESS_DOCUMENT_TYPE_KEY, addressDocumentType);
         otherParameters.put(ADDRESS1_KEY, address1);
         otherParameters.put(ADDRESS2_KEY, address2);
         otherParameters.put(ADDRESS3_KEY, address3);
         otherParameters.put(CITY_KEY, city);
         otherParameters.put(STATE_KEY, stateOrProvince);
         otherParameters.put(POSTALCODE_KEY, postalCode);
         otherParameters.put(COUNTRY_KEY, countryCode);
         otherParameters.put(CAMPUS_KEY, campusCode);
         otherParameters.put(VERIFY_ADDRESS_FLAG_KEY, verifyAddressFlag);
         otherParameters.put(PHONE_TYPE_KEY, phoneType);
         otherParameters.put(PHONE_NUMBER_KEY, phoneNumber);
         otherParameters.put(PHONE_EXTENSION_KEY, extension);
         otherParameters.put(PHONE_INTERNATIONAL_NUMBER_KEY, internationalNumber);
         otherParameters.put(EMAIL_ADDRESS_TYPE_KEY, emailType);
         otherParameters.put(EMAIL_ADDRESS_KEY, emailAddress);
         otherParameters.put(AFFILIATION_KEY, affiliation);
         otherParameters.put(SSN_KEY, ssn);
 
         return (PersonServiceReturn) new AddPersonImpl().implementService(
                                         CprServiceName.AddPerson.toString(), request.getRemoteAddr(), principalId, 
                                         password, updatedBy, null,
                                         null, otherParameters);
 		
 		
 	}
 	
 	/**
 	 * This function provides the implementation for the UnarchivePerson SOAP web service.  UnarchivePerson will allow 
 	 * authorized registration authorities to un-archive a person's information in the Central Person Registry.  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service and to un-archive the person.
 	 * 
 	 * @param principalId contains the service principal used to authenticate the service.
 	 * @param password contains the password for the service principal.
 	 * @param updatedBy contains the system principal and/or userid that requested the un-archive.
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="UnarchivePerson")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn UnarchivePerson(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType,
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier) {
 		
 		final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 		return (ServiceReturn) new UnarchivePersonImpl().implementService(
 						CprServiceName.UnarchivePerson.toString(), request.getRemoteAddr(), principalId, password, updatedBy, identifierType, 
 						identifier, null);
 	}
 
 	/**
 	 * This function provides the implementation for the ArchivePerson SOAP web service.  ArchivePerson will allow 
 	 * authorized registration authorities to archive a person's information in the Central Person Registry.  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service and to archive the person.
 	 * 
 	 * @param principalId contains the service principal used to authenticate the service.
 	 * @param password contains the password for the service principal.
 	 * @param updatedBy contains the system principal and/or userid that requested the archive.
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="ArchivePerson")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn ArchivePerson(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType,
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier) {
 		
 		final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 		return (ServiceReturn) new ArchivePersonImpl().implementService(
 						CprServiceName.ArchivePerson.toString(), request.getRemoteAddr(), principalId, password, updatedBy, identifierType, 
 						identifier, null);
 	}
 
 	/**
 	 * This function provides the implementation for the GetPerson SOAP web service.  GetPerson will allow 
 	 * authorized registration authorities to obtain a person's information from the Central Person Registry.  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.
 	 * 
 	 * @param principalId contains the service principal used to authenticate the service.
 	 * @param password contains the password for the service principal.
 	 * @param requestedBy contains the system principal and/or userid that requested the information.
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @param returnHistory Y/N flag that indicates whether to return a complete history or not.
 	 * @return PersonServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.PersonServiceReturn
 	 */
 	@WebMethod(operationName="GetPerson")
 	@WebResult(name="PersonServiceReturn")
 	public PersonServiceReturn GetPerson(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password,
 			@WebParam( name="requestedBy", mode=Mode.IN)
 			String requestedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="returnHistory", mode=Mode.IN)
 			String returnHistory) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
 
         return (PersonServiceReturn) new GetPersonImpl().implementService(
                                         CprServiceName.GetPerson.toString(), request.getRemoteAddr(), principalId, 
                                         password, requestedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 
 	/**
 	 * This function provides the implementation for the UpdatePerson SOAP web service.  UpdatePerson will allow 
 	 * authorized registration authorities to update a person's information in the Central Person Registry.  
 	 * The RA must specify some required information about the person and whether they want to
 	 * generate a PSU ID and/or userid.  Authorization checks are made to determine if the RA agent is 
 	 * allowed to call the service and to add the person.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param assignPsuIdFlag a y/n flag to indiciate whether a new PSU ID is to be created for the person.
 	 * @param assignUseridFlag a y/n flag to indicate whether a new userid is to be created for the person.
 	 * @param gender a flag to indicate the person's gender.
 	 * @param dob the person's date of birth, it can either be a full DOB or a partial one (mm/dd).
 	 * @param nameType the type of name that is being added.
 	 * @param nameDocumentType the document type associated with the name.
 	 * @param firstName the first name that is being added.
 	 * @param middleNames the middle name(s) that are being added.
 	 * @param lastName the last name that is being added.
 	 * @param suffix optionally the suffix that is being added.
 	 * @param addressType the type of address that is being added.
 	 * @param addressDocumentType the document type of the address that is being added.
 	 * @param addressGroupId the group id associated addressType and addressDocumentType being updated.
 	 * @param address1 line number one of the address.
 	 * @param address2 line number two of the address (if it exists).
 	 * @param address3 line number three of the address (if it exists).
 	 * @param city the city of the address.
 	 * @param stateOrProvince the state (if country is USA), or province (otherwise).
 	 * @param postalCode the postal code.
 	 * @param countryCode the ISO country code abbreviation.
 	 * @param campusCode the Penn State campus code for University addresses only.
 	 * @param phoneType the type of phone number being added.
 	 * @param phoneGroupId the group id associated with the record being updated.
 	 * @param phoneNumber the phone number.
 	 * @param extension the extension for the phone number if applicable.
 	 * @param internationalNumber a y/n flag to indicate whether the phone number is an international number.
 	 * @param emailType the type of email address being added.
 	 * @param emailAddress the email address.
 	 * @param affiliation the affiliation to be added.
 	 * @param ssn the user's social security number.
 	 * @return PersonServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.PersonServiceReturn
 	 * 
 	 */
 	@WebMethod(operationName="UpdatePerson")
 	@WebResult(name="PersonServiceReturn")
 	public PersonServiceReturn UpdatePerson(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="assignPsuIdFlag", mode=Mode.IN)
 			String assignPsuIdFlag, 
 			@WebParam( name="assignUseridFlag", mode=Mode.IN)
 			String assignUseridFlag, 
 			@WebParam( name="gender", mode=Mode.IN)
 			String gender, 
 			@WebParam( name="dob", mode=Mode.IN)
 			String dob, 
 			@WebParam( name="nameType", mode=Mode.IN)
 			String nameType, 
 			@WebParam(name = "nameDocumentType", mode = Mode.IN) 
 			String nameDocumentType,
 			@WebParam( name="firstName", mode=Mode.IN)
 			String firstName, 
 			@WebParam( name="middleNames", mode=Mode.IN)
 			String middleNames, 
 			@WebParam( name="lastName", mode=Mode.IN)
 			String lastName, 
 			@WebParam( name="suffix", mode=Mode.IN)
 			String suffix,
 			@WebParam( name="addressType", mode=Mode.IN)
 			String addressType, 
 			@WebParam(name = "addressDocumentType", mode = Mode.IN) 
 			String addressDocumentType,
 			@WebParam(name="addressGroupId", mode=Mode.IN) 
 			Long addressGroupId, 
 			@WebParam( name="address1", mode=Mode.IN)
 			String address1, 
 			@WebParam( name="address2", mode=Mode.IN)
 			String address2, 
 			@WebParam( name="address3", mode=Mode.IN)
 			String address3,  
 			@WebParam( name="city", mode=Mode.IN)
 			String city, 
 			@WebParam( name="stateOrProvince", mode=Mode.IN)
 			String stateOrProvince, 
 			@WebParam( name="postalCode", mode=Mode.IN)
 			String postalCode,
 			@WebParam( name="countryCode", mode=Mode.IN)
 			String countryCode, 
 			@WebParam( name="campusCode", mode=Mode.IN)
 			String campusCode,
 			@WebParam(name = "verifyAddressFlag", mode = Mode.IN) 
 			String verifyAddressFlag,
 			@WebParam( name="phoneType", mode=Mode.IN)
 			String phoneType, 
 			@WebParam( name="phoneGroupId", mode=Mode.IN)
 			Long phoneGroupId, 
 			@WebParam( name="phoneNumber", mode=Mode.IN)
 			String phoneNumber, 
 			@WebParam( name="extension", mode=Mode.IN)
 			String extension, 
 			@WebParam( name="internationalNumber", mode=Mode.IN)
 			String internationalNumber,
 			@WebParam( name="emailType", mode=Mode.IN)
 			String emailType, 
 			@WebParam( name="emailAddress", mode=Mode.IN)
 			String emailAddress,
 			@WebParam(name="affiliation", mode=Mode.IN)
 			String affiliation,
 			@WebParam(name="ssn", mode=Mode.IN) 
 			String ssn) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(29);
         otherParameters.put(ASSIGN_PSU_ID_FLAG_KEY, assignPsuIdFlag);
         otherParameters.put(ASSIGN_USERID_FLAG_KEY, assignUseridFlag);
         otherParameters.put(GENDER_KEY, gender);
         otherParameters.put(DOB_KEY, dob);
         otherParameters.put(NAME_TYPE_KEY, nameType);
         otherParameters.put(NAME_DOCUMENT_TYPE_KEY, nameDocumentType);
         otherParameters.put(FIRST_NAME_KEY, firstName);
         otherParameters.put(MIDDLE_NAMES_KEY, middleNames);
         otherParameters.put(LAST_NAME_KEY, lastName);
         otherParameters.put(SUFFIX_KEY, suffix);
         otherParameters.put(ADDRESS_TYPE_KEY, addressType);
         otherParameters.put(ADDRESS_DOCUMENT_TYPE_KEY, addressDocumentType);
         otherParameters.put(ADDRESS_GROUP_ID_KEY, Utility.safeConvertLongToString(addressGroupId));
         otherParameters.put(ADDRESS1_KEY, address1);
         otherParameters.put(ADDRESS2_KEY, address2);
         otherParameters.put(ADDRESS3_KEY, address3);
         otherParameters.put(CITY_KEY, city);
         otherParameters.put(STATE_KEY, stateOrProvince);
         otherParameters.put(POSTALCODE_KEY, postalCode);
         otherParameters.put(COUNTRY_KEY, countryCode);
         otherParameters.put(CAMPUS_KEY, campusCode);
         otherParameters.put(VERIFY_ADDRESS_FLAG_KEY, verifyAddressFlag);
         otherParameters.put(PHONE_TYPE_KEY, phoneType);
         otherParameters.put(PHONE_GROUP_ID_KEY, Utility.safeConvertLongToString(phoneGroupId));
         otherParameters.put(PHONE_NUMBER_KEY, phoneNumber);
         otherParameters.put(PHONE_EXTENSION_KEY, extension);
         otherParameters.put(PHONE_INTERNATIONAL_NUMBER_KEY, internationalNumber);
         otherParameters.put(EMAIL_ADDRESS_TYPE_KEY, emailType);
         otherParameters.put(EMAIL_ADDRESS_KEY, emailAddress);
         otherParameters.put(AFFILIATION_KEY, affiliation);
         otherParameters.put(SSN_KEY, ssn);
 
 
         return (PersonServiceReturn) new UpdatePersonImpl().implementService(
                                         CprServiceName.UpdatePerson.toString(), request.getRemoteAddr(), principalId, password, 
                                         updatedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 		
 	/**
 	 * This function provides the implementation for the AddPhone SOAP web service.
 	 * AddPhone  will allow authorized registration authorities to add phone information for a person in the
 	 * Central Person Registry.  The RA must specify the type of phone along with the phone number.
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service and to add a particular phone type.
 	 * 
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is adding the address, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param phoneType the type of phone number that is being added
 	 * @param phoneNumber the phone number to added.
 	 * @param extension the extension, if available, that is being added.
 	 * @param internationalNumber a flag indicating if the phone number is an international number
 	 * 
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="AddPhone")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn AddPhone(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId, 
 			@WebParam(name="password", mode=Mode.IN) String password, 
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) String identifier,
 			@WebParam(name="phoneType", mode=Mode.IN) String phoneType, 
 			@WebParam(name="phoneNumber", mode=Mode.IN) String phoneNumber, 
 			@WebParam(name="extension", mode=Mode.IN) String extension,
 			@WebParam(name="internationalNumber", mode=Mode.IN) String internationalNumber) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(4);
         otherParameters.put(PHONE_TYPE_KEY, phoneType);
         otherParameters.put(PHONE_NUMBER_KEY, phoneNumber);
         otherParameters.put(PHONE_EXTENSION_KEY, extension);
         otherParameters.put(PHONE_INTERNATIONAL_NUMBER_KEY, internationalNumber);
 
         return (ServiceReturn) new AddPhoneImpl().implementService(
                                 CprServiceName.AddPhone.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                                                                 identifierType, identifier, otherParameters);	
 	}
 	
 	/**
 	 * This function provides the implementation for the ArchivePhone SOAP web service.
 	 * ArchivePhone  will allow authorized registration authorities to archive phone information for a person in the
 	 * Central Person Registry.  The RA must specify the type of phone and groupId. Authorization checks are made to determine if the RA agent 
 	 * is allowed to call the service and to archive a particular phone type.
 	 *
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is archiving the address, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param phoneType the type of phone number that is being archived.
 	 * @param groupId the groupId of the phone record within the phone type
 	 * 
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="ArchivePhone")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn ArchivePhone(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId, 
 			@WebParam(name="password", mode=Mode.IN) String password, 
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) String identifier,
 			@WebParam(name="phoneType", mode=Mode.IN) String phoneType ,
 			@WebParam(name="groupId", mode=Mode.IN) Long groupId) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(PHONE_TYPE_KEY, phoneType);
         otherParameters.put(GROUP_ID_KEY, groupId);
 
         return (ServiceReturn) new ArchivePhoneImpl().implementService(
                                         CprServiceName.ArchivePhone.toString(), request.getRemoteAddr(), principalId, 
                                         password, updatedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 	
 	/**
 	 * This function provides the implementation for the GetPhone SOAP web service.
 	 * GetPhone  will allow authorized registration authorities to get phone information for a person in the
 	 * Central Person Registry. Authorization checks are made to determine if the RA agent is allowed to call the service.
 	 * 
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param requestedBy the person (userid)  who is requesting the phone information, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param phoneType if specified will perform a query for a specific phone type.
 	 * @param returnHistory Y/N that indicates whether history is to be returned.
 	 * @return PhoneServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.PhoneServiceReturn
 	 * 
 	 */
 	@WebMethod(operationName="GetPhone")
 	@WebResult(name="PhoneServiceReturn")
 	public PhoneServiceReturn GetPhone(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId, 
 			@WebParam(name="password", mode=Mode.IN) String password, 
 			@WebParam(name="requestedBy", mode=Mode.IN) String requestedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) String identifier,
 			@WebParam(name="phoneType", mode=Mode.IN) String phoneType,
 			@WebParam(name="returnHistory", mode=Mode.IN) String returnHistory) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(PHONE_TYPE_KEY, phoneType);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
 
         return (PhoneServiceReturn) new GetPhoneImpl().implementService(
                                         CprServiceName.GetPhone.toString(), request.getRemoteAddr(), principalId, 
                                         password, requestedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 	
 	/**
 	 * This function provides the implementation for the SetPrimaryPhoneByType SOAP web service.
 	 * SetPrimaryPhoneByType  will allow authorized registration authorities to set a primary phone number with a phone type for a person in the
 	 * Central Person Registry.  The RA must specify the type of phone and groupId. Authorization checks are made to determine if the RA agent 
 	 * is allowed to call the service and to archive a particular phone type.
 	 *
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is setting the primary address, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param phoneType the type of phone number that is being set to primary.
 	 * @param groupId the groupId of the phone record within the phone type
 	 * 
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="SetPrimaryPhoneByType")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn SetPrimaryPhoneByType(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId, 
 			@WebParam(name="password", mode=Mode.IN) String password, 
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) String identifier,
 			@WebParam(name="phoneType", mode=Mode.IN) String phoneType ,
 			@WebParam(name="groupId", mode=Mode.IN) Long groupId) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(PHONE_TYPE_KEY, phoneType);
         otherParameters.put(GROUP_ID_KEY, groupId);
 
         return (ServiceReturn) new SetPrimaryPhoneByTypeImpl().implementService(
                                         CprServiceName.SetPrimaryPhoneByType.toString(), request.getRemoteAddr(), principalId, password, updatedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 	
 	/**
 	 * This function provides the implementation for the UpdatePhone SOAP web service.
 	 * UpdatePhone  will allow authorized registration authorities to update phone information for a person in the
 	 * Central Person Registry.  The RA must specify the type of phone and groupId along with the phone number.
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service and to update a particular phone type.
 	 * 
 	 * @param principalId service principal identifier.
 	 * @param password  password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is updating the address, this person will be an RA agent.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param phoneType the type of phone number that is being updated.
 	 * @param groupId the groupId of the phone record within the phone type
 	 * @param phoneNumber the phone number of the update.
 	 * @param extension the extension, if available, of the update.
 	 * @param internationalNumber a flag indicating if the phone number is an international number
 	 * 
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="UpdatePhone")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn UpdatePhone(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId, 
 			@WebParam(name="password", mode=Mode.IN) String password, 
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType, 
 			@WebParam(name="identifier", mode=Mode.IN) String identifier,
 			@WebParam(name="phoneType", mode=Mode.IN) String phoneType, 
 			@WebParam(name="groupId", mode=Mode.IN)Long groupId, 
 			@WebParam(name="phoneNumber", mode=Mode.IN) String phoneNumber, 
 			@WebParam(name="extension", mode=Mode.IN) String extension,
 			@WebParam(name="internationalNumber", mode=Mode.IN) String internationalNumber)  {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(PHONE_TYPE_KEY, phoneType);
         otherParameters.put(GROUP_ID_KEY, groupId);
         otherParameters.put(PHONE_NUMBER_KEY, phoneNumber);
         otherParameters.put(PHONE_EXTENSION_KEY, extension);
         otherParameters.put(PHONE_INTERNATIONAL_NUMBER_KEY, internationalNumber);
 
         return (ServiceReturn) new UpdatePhoneImpl().implementService(
                                 CprServiceName.UpdatePhone.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                                                                 identifierType, identifier, otherParameters);	
 	}
 	
 
 	/**
 	 * This function provides the implementation for the AddPhoto SOAP web service.  
 	 * This service will enable an authorized registration authority to be able to add a photograph  
 	 * of a user to the person registry.  The calling parameters to the service will specify the photograph
 	 * along with the date the photo was taken.  If the user already has a photo, it will be expired prior 
 	 * to the new photo being added.  The service will either return an exception (with the reason the 
 	 * add did not happen) or success. 
 	 * 
 	 * @param principalId contains the service principal used to authenticate the service.
 	 * @param password contains the password for the service principal.
 	 * @param updatedBy contains the system principal and/or userid that requested the add.
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @param photo contains the bytes of the jpeg photo.
 	 * @param photoDateTaken contains the date the photo was taken.
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="AddPhoto")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn AddPhoto(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType,
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="photo", mode=Mode.IN)
 			byte[] photo, 
 			@WebParam( name="photoDateTaken", mode=Mode.IN)
 			String photoDateTaken) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(PHOTO_KEY, photo);
         otherParameters.put(PHOTO_DATE_TAKEN_KEY, photoDateTaken);
 
         return (ServiceReturn) new AddPhotoImpl().implementService(
                                 CprServiceName.AddPhoto.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                                                                 identifierType, identifier, otherParameters);	
 	}
 
 	/**
 	 * This function provides the implementation for the GetPhoto SOAP web service.  
 	 * This service will enable an authorized registration authority to be able to obtain a photograph  
 	 * of a user to the person registry.  The service will either return an exception (with the reason the 
 	 * add did not happen) or success. 
 	 * 
 	 * @param principalId contains the service principal used to authenticate the service.
 	 * @param password contains the password for the service principal.
 	 * @param requestedBy contains the system principal and/or userid that requested the add.
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @return PhotoServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.PhotoServiceReturn
 	 */
 	@WebMethod(operationName="GetPhoto")
 	@WebResult(name="PhotoServiceReturn")
 	public PhotoServiceReturn GetPhoto(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="requestedBy", mode=Mode.IN)
 			String requestedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType,
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
         return (PhotoServiceReturn) new GetPhotoImpl().implementService(
                                         CprServiceName.GetPhoto.toString(), request.getRemoteAddr(), principalId, password, requestedBy, identifierType,
                                         identifier, null);	
 	}
 
 	/**
 	 * This function provides the implementation for the ProcessRules SOAP web service.  ProcessRules will allow 
 	 * the Central Person Registry to interface with a Drools Rules Engine.  The service will pass in the
 	 * ruleset to be processed, along with the known facts and a new fact.  The service will process the
 	 * rules and return the result to the caller.
 	 * 
 	 * @param rulesetName the name of the ruleset to be processed.
 	 * @param knownFacts an array containing the known facts.
 	 * @param newFact the new fact to be asserted.
 	 * @return RulesServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.RulesServiceReturn
 	 * 
 	 */
 	@WebMethod(operationName="ProcessRules")
 	@WebResult(name="RulesServiceReturn")
 	public RulesServiceReturn ProcessRules(
 			@WebParam( name="rulesetName", mode=Mode.IN)
 			String rulesetName, 
 			@WebParam( name="knownFacts", mode=Mode.IN)
 			String[] knownFacts,
 			@WebParam( name="newFact", mode=Mode.IN)
 			String newFact) { 
 		
 		RulesReturn rulesReturn = new RulesEngineHelper().processRules(rulesetName, knownFacts, newFact);
 		return new RulesServiceReturn(rulesReturn.getStatusCode(), rulesReturn.getStatusMessage(), 
 				rulesReturn.getNumberOfFacts(), rulesReturn.getFacts());
 		
 	}	
 
 	/**
 	 * This function provides the implementation for the BlockUser SOAP web service.  BlockUser will allow authorized
 	 * security and helpdesk personnel to block a user's access from wireless as the result of a security action.  Authorization
 	 * checks are made to determine if the the caller is allowed to the call the service and add the block.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy contains the system principal and/or userid that requested the block.
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @param userid contains the credential that is being blocked.
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="BlockUser")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn BlockUser(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="userid", mode=Mode.IN)
 			String userid) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(USERID_KEY, userid);
 
         return (ServiceReturn) new SecurityImpl().implementService(
                         CprServiceName.BlockUser.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                         identifierType, identifier, otherParameters);	
 	}
 
 	/**
 	 * This function provides the implementation for the UnblockUser SOAP web service.  UnblockUser will allow authorized
 	 * security and helpdesk personnel to unblock a user's access from wireless as the result of a security action.  Authorization
 	 * checks are made to determine if the the caller is allowed to the call the service and remove the block.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy contains the system principal and/or userid that requested the unblock.
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @param userid contains the credential that is being unblocked.
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="UnblockUser")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn UnblockUser(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="userid", mode=Mode.IN)
 			String userid) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(USERID_KEY, userid);
 
         return (ServiceReturn) new SecurityImpl().implementService(
                         CprServiceName.UnblockUser.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                         identifierType, identifier, otherParameters);	
 	}
 
 	/**
 	 * This function provides the implementation for the DisableUser SOAP web service.  DisableUser will allow authorized
 	 * security and helpdesk personnel to lock a user's access account as the result of a security action.  Authorization
 	 * checks are made to determine if the the caller is allowed to the call the service and lock the account.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy contains the system principal and/or userid that requested the lock.
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @param userid contains the credential that is being disabled.
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="DisableUser")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn DisableUser(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="userid", mode=Mode.IN)
 			String userid) {
 		
 		final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
 		otherParameters.put(USERID_KEY, userid);
 
 		return (ServiceReturn) new SecurityImpl().implementService(
 				CprServiceName.DisableUser.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
 				identifierType, identifier, otherParameters);	
 	}
 
 	/**
 	 * This function provides the implementation for the EnableUser SOAP web service.  EnableUser will allow authorized
 	 * security and helpdesk personnel to unlock a user's access account as the result of a security action.  Authorization
 	 * checks are made to determine if the the caller is allowed to the call the service and unlock the account.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy contains the system principal and/or userid that requested the enable.
 	 * @param identifierType contains the type of identifier used to find the person.
 	 * @param identifier contains the value of the identifier.
 	 * @param userid contains the credential that is being unlocked.
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="EnableUser")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn EnableUser(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId, 
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType, 
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="userid", mode=Mode.IN)
 			String userid) {
 		
         // Init the service.
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(USERID_KEY, userid);
 
         return (ServiceReturn) new SecurityImpl().implementService(
                         CprServiceName.EnableUser.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                         identifierType, identifier, otherParameters);	
 	}
 		
 	/**
 	 * This function implements the TransformAddress SOAP web service.  TransformAddress 
 	 * places the incoming address into a standardized format according to postal rules and makes an 
 	 * assessment of the address deliverability. TransformAddress is currently only valid for
 	 * addresses from the United State and Canada
 	 * 
 	 * @param principalId service principal identifier (will be a Kerberos principal).
 	 * @param password password for the service principal specified as the first argument.
 	 * @param requestedBy the person (userid) who is making the request
 	 * @param address1 first line of the street address
 	 * @param address2 second line of the street address
 	 * @param address3 third line of the street address
 	 * @param city the city of the address
 	 * @param stateOrProvince for US addresses, a State; for Canadian addresses, a Province, 
 	 * @param postalCode the postal code of the address. For US address, may include plus4 code.
 	 * @param countryCode three character country code as defined in ISO 3166
 	 * @return TransformServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.TransformServiceReturn
 	 * 
 	 */
 	@WebMethod(operationName = "TransformAddress")
 	@WebResult(name = "TransformServiceReturn")
 	public TransformServiceReturn TransformAddress(
 			@WebParam(name = "principalId", mode = Mode.IN) String principalId,
 			@WebParam(name = "password", mode = Mode.IN) String password,
 			@WebParam(name = "requestedBy", mode = Mode.IN) String requestedBy,
 			@WebParam(name = "address1", mode = Mode.IN) String address1,
 			@WebParam(name = "address2", mode = Mode.IN) String address2,
 			@WebParam(name = "address3", mode = Mode.IN) String address3,
 			@WebParam(name = "city", mode = Mode.IN) String city,
 			@WebParam(name = "stateOrProvince", mode = Mode.IN) String stateOrProvince,
 			@WebParam(name = "postalCode", mode = Mode.IN) String postalCode,
 			@WebParam(name = "countryCode", mode = Mode.IN) String countryCode) {
 		return new TransformServiceReturn(ReturnType.NOT_IMPLEMENTED_EXCEPTION.index(), ReturnType.NOT_IMPLEMENTED_EXCEPTION.message());
 	}
 
 	/**
 	 * This function implements the GetMatchCode SOAP web service.  Given a matching data type and data,
 	 * service GetMatchCode will connect to the data quality server and generate a match code.
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service. 
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param requestedBy the person (user id) who is making the request
 	 * @param matchDataType first line of the street address
 	 * @param dataValue second line of the street address
 	 * 
 	 * @return MatchCodeServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.MatchCodeServiceReturn
 	 */
 
 	@WebMethod(operationName = "GetMatchCode")
 	@WebResult(name = "MatchCodeServiceReturn")
 	public MatchCodeServiceReturn GetMatchCode(
 			@WebParam(name = "principalId", mode = Mode.IN) String principalId,
 			@WebParam(name = "password", mode = Mode.IN) String password,
 			@WebParam(name = "requestedBy", mode = Mode.IN) String requestedBy,
 			@WebParam(name = "matchDataType", mode = Mode.IN) String matchDataType,
 			@WebParam(name = "dataValue", mode = Mode.IN) String dataValue) {
 		return new MatchCodeServiceReturn(ReturnType.NOT_IMPLEMENTED_EXCEPTION.index(), ReturnType.NOT_IMPLEMENTED_EXCEPTION.message());
 	}
 
 
 	/**
 	 * This function provides the implementation for the AddUserComment SOAP web service.
 	 * AddUserComment will allow authorized Security Agent to be able to add a
 	 * comment for a userid in the Central Person Registry.
 	 * The Security Agent must specify the type of the comment, along with the comment and a userId that is
 	 * associated with the comment.
 	 * Authorization checks are made to determine if the security agent is allowed to call the service 
 	 * and to add a particular comment type.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password contains the password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType contains the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param userId contains the userId associated with the comment.
 	 * @param userCommentType contains the type of comment that is being added.
 	 * @param comment contains the comment
 	 * 
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="AddUserComment")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn AddUserComment(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId,
 			@WebParam(name="password", mode=Mode.IN) String password,
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType,
 			@WebParam(name="identifier", mode=Mode.IN) String identifier,
 			@WebParam(name="userId", mode=Mode.IN) String userId,
 			@WebParam(name="userCommentType", mode=Mode.IN) String userCommentType,
 			@WebParam(name="comment", mode=Mode.IN) String comment ) {
 
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(3);
         otherParameters.put(USERID_KEY, userId);
         otherParameters.put(USER_COMMENT_TYPE_KEY, userCommentType);
         otherParameters.put(USER_COMMENT_KEY, comment);
 
         return (ServiceReturn) new AddUserCommentImpl().implementService(
                                 CprServiceName.AddUserComment.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                                                                 identifierType, identifier, otherParameters);	
 	}
 
 
 	/**
 	 * This function provides the implementation for the UpdateUserComment SOAP web service.
 	 * UpdateUserComment will allow authorized Security Agent to be able to update a
 	 * comment for a userid in the Central Person Registry.
 	 * The Security Agent must specify the type of the comment, along with the comment and a userId that is
 	 * associated with the comment.
 	 * Authorization checks are made to determine if the Security Agent is allowed to call the service 
 	 * and to update a particular comment type.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password contains the password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType contains the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param userId contains the userId associated with the comment.
 	 * @param userCommentType contains the type of comment that is being added.
 	 * @param comment contains the comment
 	 * 
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="UpdateUserComment")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn UpdateUserComment(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId,
 			@WebParam(name="password", mode=Mode.IN) String password,
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType,
 			@WebParam(name="identifier", mode=Mode.IN) String identifier,
 			@WebParam(name="userId", mode=Mode.IN) String userId,
 			@WebParam(name="userCommentType", mode=Mode.IN) String userCommentType,
 			@WebParam(name="comment", mode=Mode.IN) String comment ) {
 
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(3);
         otherParameters.put(USERID_KEY, userId);
         otherParameters.put(USER_COMMENT_TYPE_KEY, userCommentType);
         otherParameters.put(USER_COMMENT_KEY, comment);
 
         return (ServiceReturn) new UpdateUserCommentImpl().implementService(
                                 CprServiceName.UpdateUserComment.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                                                                 identifierType, identifier, otherParameters);	
 	}
 
 
 	/**
 	 * This function provides the implementation for the GetUserComments SOAP web service.  
 	 * GetUserComments will allow authorized registration authorities to be able to obtain 
 	 * the comments for a person in the Central Person Registry.  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password contains the password for the service principal identifier.
 	 * @param requestedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType contains the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param userId contains the userId associated with the comment.
 	 * @param userCommentType if specified will be the comment type that will be searched for.
 	 * @param returnHistory Y/N flag that indicates whether history is to be returned or not.
 	 * @return UserCommentServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.UserCommentServiceReturn
 	 * 
 	 */
 	@WebMethod(operationName="GetUserComments")
 	@WebResult(name="UserCommentServiceReturn")
 
 	public UserCommentServiceReturn GetUserComments(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId,
 			@WebParam(name="password", mode=Mode.IN) String password,
 			@WebParam(name="requestedBy", mode=Mode.IN) String requestedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType,
 			@WebParam(name="identifier", mode=Mode.IN) String identifier,
 			@WebParam(name="userId", mode=Mode.IN) String userId,
 			@WebParam(name="userCommentType", mode=Mode.IN) String userCommentType,
 			@WebParam(name="returnHistory", mode=Mode.IN) String returnHistory) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(3);
         otherParameters.put(USERID_KEY, userId);
         otherParameters.put(USER_COMMENT_TYPE_KEY, userCommentType);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
 
         return (UserCommentServiceReturn) new GetUserCommentsImpl().implementService(
                                         CprServiceName.GetUserComments.toString(), request.getRemoteAddr(), 
                                         principalId, password, requestedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 
 	/**
 	 * This function provides the implementation for the ArchiveUserComment SOAP web service.
 	 * ArchiveUserComment will allow authorized Security Agent to be able to archive a
 	 * comment for a userid in the Central Person Registry.
 	 * The Security Agent must specify the type of the comment, along with the comment and a userId that is
 	 * associated with the comment.
 	 * Authorization checks are made to determine if the security agent is allowed to call the service 
 	 * and to add a particular comment type.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password contains the password for the service principal identifier.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType contains the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param userId contains the userId associated with the comment.
 	 * @param userCommentType contains the type of comment that is being added.
 	 * 
 	 * @return ServiceReturn object that contains the result of executing the service.
 	 * 
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="ArchiveUserComment")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn ArchiveUserComment(
 			@WebParam(name="principalId", mode=Mode.IN) String principalId,
 			@WebParam(name="password", mode=Mode.IN) String password,
 			@WebParam(name="updatedBy", mode=Mode.IN) String updatedBy, 
 			@WebParam(name="identifierType", mode=Mode.IN) String identifierType,
 			@WebParam(name="identifier", mode=Mode.IN) String identifier,
 			@WebParam(name="userId", mode=Mode.IN) String userId,
 			@WebParam(name="userCommentType", mode=Mode.IN) String userCommentType) {
 
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(USERID_KEY, userId);
         otherParameters.put(USER_COMMENT_TYPE_KEY, userCommentType);
 
         return (ServiceReturn) new ArchiveUserCommentImpl().implementService(
                                         CprServiceName.ArchiveUserComment.toString(), request.getRemoteAddr(), principalId, 
                                         password, updatedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 	
 	/**
 	 * This function provides the implementation for the AddUserid SOAP web service.  AddUserid will allow 
 	 * authorized registration authorities to add a new userid for a person in the Central Person Registry.  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.  NOTE:
 	 * if the user already has an existing userid, calling this service will provision a new secondary
 	 * userid.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param personId the CPR private person identifier who a userid is to be created for.
 	 * @return UseridServiceReturn object contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.UseridServiceReturn
 	 */
 	@WebMethod(operationName="AddUserid")
 	@WebResult(name="UseridServiceReturn")
 	public UseridServiceReturn AddUserid(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="personId", mode=Mode.IN)
 			int personId) {
 		
 		final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 		return (UseridServiceReturn) new AddUseridImpl().implementService(
 					CprServiceName.AddUserid.toString(), request.getRemoteAddr(), principalId, password, updatedBy, 
 									"PERSON_ID", Integer.toString(personId), null);
 	}
 	
 	/**
 	 * This function provides the implementation for the AddSpecialUserid SOAP web service.  AddSpecialUserid will allow 
 	 * authorized registration authorities to add a new userid for a person in the Central Person Registry.  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.  NOTE:
 	 * if the user already has an existing userid, calling this service will provision a new secondary
 	 * userid.  The user will specify what the special userid is.  NOTE: this service will only be 
 	 * executed in certain situations.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param personId the CPR private person identifier who a userid is to be created for.
 	 * @param userid the userid that will be added for the user.
 	 * @return ServiceReturn object contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="AddSpecialUserid")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn AddSpecialUserid(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="personId", mode=Mode.IN)
 			int personId,
 			@WebParam(name="userid", mode=Mode.IN) 
 			String userid) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(USERID_KEY, userid);
 
         return (ServiceReturn) new AddSpecialUseridImpl().implementService(
                                 CprServiceName.AddSpecialUserid.toString(), request.getRemoteAddr(), principalId, password, updatedBy,
                                                                 "PERSON_ID", Integer.toString(personId), otherParameters);	
 	}
 
 	/**
 	 * This function provides the implementation for the GetUserid SOAP web service.  GetUserid will allow 
 	 * authorized registration authorities to obtain userid information for a person in the Central Person Registry.  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.  This service will
 	 * return only the active records.
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param requestedBy the person (userid) who is requesting the information, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param returnHistory Y/N flag that indicates whether history is to be returned.
 	 * @return UseridServiceReturn object that contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.UseridServiceReturn
 	 */
 	@WebMethod(operationName="GetUserid")
 	@WebResult(name="UseridServiceReturn")
 	public UseridServiceReturn GetUserid(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="requestedBy", mode=Mode.IN)
 			String requestedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType,
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam( name="returnHistory", mode=Mode.IN)
 			String returnHistory) {
 
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
 
         return (UseridServiceReturn) new GetUseridImpl().implementService(
                                         CprServiceName.GetUserid.toString(), request.getRemoteAddr(), principalId, password, 
                                         requestedBy, identifierType,
                                         identifier, otherParameters);	
 	}	
 
 	/**
 	 * This function provides the implementation for the SetPrimaryUserid SOAP web service.  SetPrimaryUserid will allow 
 	 * authorized registration authorities to add a indicate a userid as primary in the central person registry.  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.  
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param userid the userid that will be set as primary for the user.
 	 * @return ServiceReturn object contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="SetPrimaryUserid")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn SetPrimaryUserid(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType,
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam(name="userid", mode=Mode.IN) 
 			String userid) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(USERID_KEY, userid);
 
         return (ServiceReturn) new SetPrimaryUseridImpl().implementService(
                                         CprServiceName.SetPrimaryUserid.toString(), request.getRemoteAddr(), principalId, 
                                         password, updatedBy, identifierType,
                                         identifier, otherParameters);
 	}
 	
 	/**
 	 * This function provides the implementation for the ArchiveUserid SOAP web service.  ArchiveUserid will allow 
 	 * authorized registration authorities to add a indicate a userid as archived in the central person registry.  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.  
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param userid the userid that will be archived.
 	 * @return ServiceReturn object contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="ArchiveUserid")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn ArchiveUserid(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType,
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam(name="userid", mode=Mode.IN) 
 			String userid) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(USERID_KEY, userid);
 
         return (ServiceReturn) new ArchiveUseridImpl().implementService(
                                         CprServiceName.ArchiveUserid.toString(), request.getRemoteAddr(), principalId, 
                                         password, updatedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 	
 	/**
 	 * This function provides the implementation for the UnarchiveUserid SOAP web service.  UnarchiveUserid will allow 
 	 * authorized registration authorities to add a indicate a userid as unarchived in the central person registry.  
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.  
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param userid the userid that will be unarchived.
 	 * @return ServiceReturn object contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="UnarchiveUserid")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn UnarchiveUserid(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType,
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam(name="userid", mode=Mode.IN) 
 			String userid) {
 		
         final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(USERID_KEY, userid);
 
         return (ServiceReturn) new UnarchiveUseridImpl().implementService(
                                         CprServiceName.UnarchiveUserid.toString(), request.getRemoteAddr(), principalId, 
                                         password, updatedBy, identifierType,
                                         identifier, otherParameters);	
 	}
 	
 	/**
 	 * This function provides the implementation for the AddPersonIdentifier SOAP web service.  AddPersonIdentifier will allow 
 	 * authorized registration authorities to add a SoR registry identifier to a person.
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.  
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param registryIdentifierType contains the type of SoR identifier that is being added to the user.
 	 * @param registryIdentifierValue contains the value of the identfier that is being added.
 	 * @return ServiceReturn object contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="AddPersonIdentifier")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn AddPersonIdentifier(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType,
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam(name="registryIdentifierType", mode=Mode.IN) 
 			String registryIdentifierType,
 			@WebParam(name="registryIdentifierValue", mode=Mode.IN)
 			String registryIdentifierValue) {
 		final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 		
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(IDENTIFIER_TYPE_KEY, registryIdentifierType);
         otherParameters.put(IDENTIFIER_KEY, registryIdentifierValue);
 
 		return (ServiceReturn) new AddPersonIdentifierImpl().implementService(
 						CprServiceName.AddPersonIdentifier.toString(), request.getRemoteAddr(), principalId, password, updatedBy, identifierType, 
 						identifier, otherParameters);
 	}
 
 	/**
 	 * This function provides the implementation for the UpdatePersonIdentifier SOAP web service.  UpdatePersonIdentifier will allow 
 	 * authorized registration authorities to update a SoR registry identifier to a person.
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.  
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param registryIdentifierType contains the type of SoR identifier that is being added to the user.
 	 * @param registryIdentifierValue contains the value of the identfier that is being added.
 	 * @return ServiceReturn object contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="UpdatePersonIdentifier")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn UpdatePersonIdentifier(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType,
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam(name="registryIdentifierType", mode=Mode.IN) 
 			String registryIdentifierType,
 			@WebParam(name="registryIdentifierValue", mode=Mode.IN)
 			String registryIdentifierValue) {
 		return AddPersonIdentifier(principalId, password, updatedBy, identifierType, identifier, registryIdentifierType, registryIdentifierValue);
 	}
 
 	/**
 	 * This function provides the implementation for the ArchivePersonIdentifier SOAP web service.  ArchivePersonIdentifier will allow 
 	 * authorized registration authorities to archive a SoR registry identifier to a person.
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.  
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param updatedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param registryIdentifierType contains the type of SoR identifier that is being added to the user.
 	 * @return ServiceReturn object contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="ArchivePersonIdentifier")
 	@WebResult(name="ServiceReturn")
 	public ServiceReturn ArchivePersonIdentifier(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="updatedBy", mode=Mode.IN)
 			String updatedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType,
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam(name="registryIdentifierType", mode=Mode.IN) 
 			String registryIdentifierType) {
 		final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 		
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(IDENTIFIER_TYPE_KEY, registryIdentifierType);
 		return (ServiceReturn) new ArchivePersonIdentifierImpl().implementService(
 						CprServiceName.ArchivePersonIdentifier.toString(), request.getRemoteAddr(), principalId, password, 
 						updatedBy, identifierType, 
 						identifier, otherParameters);
 	}
 	
 	/**
 	 * This function provides the implementation for the GetPersonIdentifier SOAP web service.  GetPersonIdentifier will allow 
 	 * authorized registration authorities to query person identifiers for a person in the CPR.
 	 * Authorization checks are made to determine if the RA agent is allowed to call the service.  
 	 * 
 	 * @param principalId server principal identifier (will be a Kerberos principal).
 	 * @param password password for the server principal specified as the first argument.
 	 * @param requestedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param registryIdentifierType contains the type of SoR identifier that is being added to the user.
 	 * @return PersonIdentifierServiceReturn object contains the result of executing the service.
 	 * @see edu.psu.iam.cpr.core.api.returns.ServiceReturn
 	 */
 	@WebMethod(operationName="GetPersonIdentifier")
 	@WebResult(name="PersonIdentifierServiceReturn")
 	public PersonIdentifierServiceReturn GetPersonIdentifier(
 			@WebParam( name="principalId", mode=Mode.IN)
 			String principalId,
 			@WebParam( name="password", mode=Mode.IN)
 			String password, 
 			@WebParam( name="requestedBy", mode=Mode.IN)
 			String requestedBy, 
 			@WebParam( name="identifierType", mode=Mode.IN)
 			String identifierType,
 			@WebParam( name="identifier", mode=Mode.IN)
 			String identifier,
 			@WebParam(name="registryIdentifierType", mode=Mode.IN) 
 			String registryIdentifierType,
 			@WebParam(name="returnHistory", mode=Mode.IN)
 			String returnHistory) {
 		final HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
 		
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(IDENTIFIER_TYPE_KEY, registryIdentifierType);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
 
 		return (PersonIdentifierServiceReturn) new GetPersonIdentifierImpl().implementService(
 						CprServiceName.GetPersonIdentifier.toString(), request.getRemoteAddr(), principalId, password, 
 						requestedBy, identifierType, 
 						identifier, otherParameters);
 	}
 }
