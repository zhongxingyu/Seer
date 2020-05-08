 package edu.psu.iam.cpr.rest;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 /**
  * This is the interface for all of the CPR's RESTful services.
  *  
  * Copyright 2013 The Pennsylvania State University
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
  * @package edu.psu.iam.cpr.rest
  * @author $Author: jvuccolo $
  */
 
 @Path("/v1")
 public interface CprApiInterface {
 	
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
 	 * @param rankCutOff The minimum ranking to be considered a positive match. If no value provided, then cut off score will be determined by default cutoff rankings from the standard and international criteria matrices
 	 *  
 	 * @return will return an instance of the HTTP response object.
 	 * 
 	 */
 	@GET
 	@Path("/people")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response searchForPerson(
 			@QueryParam("requestedBy") final String requestedBy,
 			@QueryParam("psuId") final String psuId,
 			@QueryParam("userId") final String userId,
 			@QueryParam("ssn") final String ssn,
 			@QueryParam("firstName") final String firstName,
 			@QueryParam("lastName") final String lastName,
 			@QueryParam("middleName") final String middleName,
 			@QueryParam("address1") final String address1,
 			@QueryParam("address2") final String address2,
 			@QueryParam("address3") final String address3,
 			@QueryParam("city") final String city,
 			@QueryParam("state") final String state,
 			@QueryParam("postalCode") final String postalCode,
 			@QueryParam("plus4") final String plus4,
 			@QueryParam("country") final String country,
 			@QueryParam("dateOfBirth") final String dateOfBirth,
 			@QueryParam("gender") final String gender,
 			@QueryParam("rankCutOff") final String rankCutOff);
 
 	/**
 	 * Archive Addresses RESTful API.
 	 *   
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param addressType  the type of address to delete
 	 * @param documentType the type of document
 	 * @param groupId the groupId of the address record within the address type
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * 
 	 * @return will return an instance of a REST response object.
 	 * 
 	 */
 	@DELETE
 	@Path("/people/{identifier_type}:{identifier}/addresses")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response archiveAddress(
 			@PathParam("identifier_type") final String identifierType,
 			@PathParam("identifier") final String identifier,
 			@QueryParam("requestedBy") final String requestedBy,
 			@QueryParam("addressType") final String addressType,
 			@QueryParam("documentType") final String documentType,
 			@QueryParam("groupId") final String groupId);
 	
 	/**
 	 * Archive Addresses RESTful service.
 	 *   
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param addressKey contains the address key to be archived.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * 
 	 * @return will return an instance of a REST response object.
 	 * 
 	 */
 	@DELETE
 	@Path("/people/{identifier_type}:{identifier}/addresses/{addressKey}")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response archiveAddressUsingKey(
 			@PathParam("identifier_type") final String identifierType,
 			@PathParam("identifier") final String identifier,
 			@QueryParam("requestedBy") final String requestedBy,
 			@QueryParam("addressKey") final String addressKey);
 	
 	/**
 	 * Archive Person RESTful API.
 	 * 
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * 
 	 * @return will return a REST response object.
 	 */
 	@DELETE
 	@Path("/people/{identifier_type}:{identifier}")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response archivePerson(
 			@PathParam("identifier_type") final String identifierType,
 			@PathParam("identifier") final String identifier,
 			@QueryParam("requestedBy") final String requestedBy);
 	
 	/**
 	 * Archive Userid RESTful Service
 	 *  
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param userid the userid that will be archived.
 	 * 
 	 * @return will return a REST response object.
 	 */
 	@DELETE
 	@Path("/people/{identifier_type}:{identifier}/userids")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response archiveUserid(
 			@PathParam("identifier_type") final String identifierType,
 			@PathParam("identifier") final String identifier,
 			@QueryParam("userid") final String userid,
 			@QueryParam("requestedBy") final String requestedBy);
 
 
 	/**
 	 * Archive User Comment RESTful API.
 	 *  
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param commentKey contains the comment key.
 	 * 
 	 * @return will return a REST response object.
 	 */
 	@DELETE
 	@Path("/people/{identifier_type}:{identifier}/comments/{commentKey}")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response archiveUserCommentUsingKey(
 			@PathParam("identifier_type") final String identifierType,
 			@PathParam("identifier") final String identifier,
 			@QueryParam("commentKey") final String commentKey,
 			@QueryParam("requestedBy") final String requestedBy);
 	
 	/**
 	 * Archive User Comment RESTful API.
 	 *  
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param userId contains the userId associated with the comment.
 	 * @param userCommentType contains the type of comment that is being added.
 	 * 
 	 * @return will return a REST response object.
 	 */
 	@DELETE
 	@Path("/people/{identifier_type}:{identifier}/comments")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response archiveUserComment(
 			@PathParam("identifier_type") final String identifierType,
 			@PathParam("identifier") final String identifier,
 			@QueryParam("userId") final String userId,
 			@QueryParam("userCommentType") final String userCommentType,
 			@QueryParam("requestedBy") final String requestedBy);
 
 	/**
 	 * Archive Email Address RESTful API.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param emailAddressType contains the type of email address that is being archived.
 	 * 
 	 * @return will return a REST response object.
 	 */
 	@DELETE
 	@Path("/people/{identifier_type}:{identifier}/emails")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response archiveEmail(
 			@PathParam("identifier_type") final String identifierType,
 			@PathParam("identifier") final String identifier,
 			@QueryParam("emailAddressType") final String emailAddressType,
 			@QueryParam("requestedBy") final String requestedBy);
 
 	/**
 	 * Archive Email Address RESTful API.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param emailAddressKey contains the email address key to be archived.
 	 * 
 	 * @return will return a REST response object.
 	 */
 	@DELETE
 	@Path("/people/{identifier_type}:{identifier}/emails/{emailAddressKey}")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response archiveEmailUsingKey(
 			@PathParam("identifier_type") final String identifierType,
 			@PathParam("identifier") final String identifier,
 			@QueryParam("emailAddressKey") final String emailAddressKey,
 			@QueryParam("requestedBy") final String requestedBy);
 
 	/**
 	 * Archive Phone RESTful service.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param phoneType the type of phone number that is being archived.
 	 * @param groupId the groupId of the phone record within the phone type
 	 * 
 	 * @return will return a REST response object.
 	 */
 	@DELETE
 	@Path("/people/{identifier_type}:{identifier}/phones")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response archivePhone(
 			@PathParam("identifier_type") final String identifierType,
 			@PathParam("identifier") final String identifier,
 			@QueryParam("phoneType") final String phoneType,
 			@QueryParam("groupId") final String groupId,
 			@QueryParam("requestedBy") final String requestedBy);
 	
 	/**
 	 * Archive Phone RESTful service.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param phoneKey contains the phone key to be archived.
 	 * 
 	 * @return will return a REST response object.
 	 */
 	@DELETE
 	@Path("/people/{identifier_type}:{identifier}/phones/{phoneKey}")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response archivePhoneUsingKey(
 			@PathParam("identifier_type") final String identifierType,
 			@PathParam("identifier") final String identifier,
 			@QueryParam("phoneKey") final String phoneKey,
 			@QueryParam("requestedBy") final String requestedBy);
 	
 	/**
 	 * This method is used to delete (archive) a name record.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param nameType contains the type of name to be archived.
 	 * @param documentType will contain the document type if the name type is "DOCUMENT".
 	 * @return will return a standard HTTP response.
 	 */
 	@DELETE
 	@Path("/people/{identifier_type}:{identifier}/names")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response deleteName(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @QueryParam("nameType") final String nameType,
 			  @QueryParam("documentType") final String documentType,
 			  @QueryParam("requestedBy") final String requestedBy);
 	
 	/**
 	 * This method is used to delete (archive) a name record.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param nameKey contains the name key that identifies the record.
 	 * @return will return a standard HTTP response.
 	 */
 	@DELETE
 	@Path("/people/{identifier_type}:{identifier}/names/{nameKey}")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response deleteNameUsingKey(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @PathParam("nameKey") final String nameKey,
 			  @QueryParam("requestedBy") final String requestedBy);
 
 		
 	/**
 	 * This function provides the implementation for the Add Person RESTful API.
 	 * 
 	 * @param requestedBy the person (userid) who is making the change, this person will be an RA agent.
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
 	 * @param nickname optionally the nickname that is being added.
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
 	 * @param verifyAddressFlag contains a Y/N flag that indicates whether an address should be validated or not.
 	 * @param phoneType the type of phone number being added.
 	 * @param phoneNumber the phone number.
 	 * @param extension the extension for the phone number if applicable.
 	 * @param internationalNumber a y/n flag to indicate whether the phone number is an international number.
 	 * @param emailType the type of email address being added.
 	 * @param emailAddress the email address.
 	 * @param affiliation the affiliation to be added.
 	 * @param ssn contains the user's SSN.
 	 * 
 	 * @return HTTP response object.
 	 */
 	@POST
 	@Path("/people")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response addPerson(
 				@FormParam("requestedBy") final String requestedBy,
 				@FormParam("assignPsuIdFlag") final String assignPsuIdFlag,
 				@FormParam("assignUseridFlag") final String assignUseridFlag,
 				@FormParam("gender") final String gender,
 				@FormParam("dob") final String dob,
 				@FormParam("nameType") final String nameType,
 				@FormParam("nameDocumentType") final String nameDocumentType,
 				@FormParam("firstName") final String firstName,
 				@FormParam("middleNames") final String middleNames,
 				@FormParam("lastName") final String lastName,
 				@FormParam("suffix") final String suffix,
 				@FormParam("nickname") final String nickname, 
 				@FormParam("addressType") final String addressType,
 				@FormParam("addressDocumentType") final String addressDocumentType,
 				@FormParam("address1") final String address1,
 				@FormParam("address2") final String address2,
 				@FormParam("address3") final String address3,
 				@FormParam("city") final String city,
 				@FormParam("stateOrProvince") final String stateOrProvince,
 				@FormParam("postalCode") final String postalCode,
 				@FormParam("countryCode") final String countryCode,
 				@FormParam("campusCode") final String campusCode,
 				@FormParam("verifyAddressFlag") final String verifyAddressFlag,
 				@FormParam("phoneType") final String phoneType,
 				@FormParam("phoneNumber") final String phoneNumber,
 				@FormParam("extension") final String extension,
 				@FormParam("internationalNumber") final String internationalNumber,
 				@FormParam("emailType") final String emailType,
 				@FormParam("emailAddress") final String emailAddress,
 				@FormParam("affilation") final String affiliation,
 				@FormParam("ssn") final String ssn);
 
 	/**
 	 * This function provides the implementation for the Update Person RESTful API.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is updating the person, this person will be an RA agent.
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
 	 * @param nickname optionally the nickname that is being added.
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
 	 * @param verifyAddressFlag contains a Y/N flag that indicates whether an address should be verified or not.
 	 * @param phoneType the type of phone number being added.
 	 * @param phoneGroupId the group id associated with the record being updated.
 	 * @param phoneNumber the phone number.
 	 * @param extension the extension for the phone number if applicable.
 	 * @param internationalNumber a y/n flag to indicate whether the phone number is an international number.
 	 * @param emailType the type of email address being added.
 	 * @param emailAddress the email address.
 	 * @param affiliation the affiliation to be added.
 	 * @param ssn the user's social security number.
 	 * 
 	 * @return HTTP response object. 
 	 */
 	@PUT
 	@Path("/people/personId:{identifier}")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response updatePerson(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @FormParam("requestedBy") final String requestedBy,
 			  @FormParam("assignPsuIdFlag") final String assignPsuIdFlag,
 			  @FormParam("assignUseridFlag") final String assignUseridFlag,
 			  @FormParam("gender") final String gender,
 			  @FormParam("dob") final String dob,
 			  @FormParam("nameType") final String nameType,
 			  @FormParam("nameDocumentType") final String nameDocumentType,
 			  @FormParam("firstName") final String firstName,
 			  @FormParam("middleNames") final String middleNames,
 			  @FormParam("lastName") final String lastName,
 			  @FormParam("suffix") final String suffix,
 			  @FormParam("nickname") final String nickname,  
 			  @FormParam("addressType") final String addressType,
 			  @FormParam("addressDocumentType") final String addressDocumentType,
 			  @FormParam("addressGroupId") final String addressGroupId,
 			  @FormParam("address1") final String address1,
 			  @FormParam("address2") final String address2,
 			  @FormParam("address3") final String address3,
 			  @FormParam("city") final String city,
 			  @FormParam("stateOrProvince") final String stateOrProvince,
 			  @FormParam("postalCode") final String postalCode,
 			  @FormParam("countryCode") final String countryCode,
 			  @FormParam("campusCode") final String campusCode,
 			  @FormParam("verifyAddressFlag") final String verifyAddressFlag,
 			  @FormParam("phoneType") final String phoneType,
 			  @FormParam("phoneGroupId") final String phoneGroupId,
 			  @FormParam("phoneNumber") final String phoneNumber,
 			  @FormParam("extension") final String extension,
 			  @FormParam("internationalNumber") final String internationalNumber,
 			  @FormParam("emailType") final String emailType,
 			  @FormParam("emailAddress") final String emailAddress,
 			  @FormParam("affiliation") final String affiliation,
 			  @FormParam("ssn") final String ssn);
 	
 	/**
 	 * Add Userid RESTful service.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is adding the userid, this person will be an RA agent.
 	 * 
 	 * @return will return a response object.
 	 */
 	@POST
 	@Path("/people/person_id:{identifier}/userids")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response addUserid(
 			  @PathParam("identifier") final String identifier,
 			  @FormParam("requestedBy") final String requestedBy);
 	
 	/**
 	 * Add Address RESTful API.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is adding the address, this person will be an RA agent.
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
 	 * @return will return a response object.
 	 */
 	@POST
 	@Path("/people/{identifier_type}:{identifier}/addresses")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response addAddress(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @FormParam("requestedBy") final String requestedBy,
 			  @FormParam("addressType") final String addressType,
 			  @FormParam("documentType") final String documentType,
 			  @FormParam("address1") final String address1,
 			  @FormParam("address2") final String address2,
 			  @FormParam("address3") final String address3,
 			  @FormParam("city") final String city,
 			  @FormParam("stateOrProvince") final String stateOrProvince,
 			  @FormParam("postalCode") final String postalCode,
 			  @FormParam("countryCode") final String countryCode,
 			  @FormParam("campusCode") final String campusCode,
 			  @FormParam("verifyAddressFlag") final String verifyAddressFlag );
 
 	/**
 	 * Update Address RESTful API.
 	 *
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is adding the address, this person will be an RA agent.
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
 	 * @return will return a response object.
 	 */
 	@PUT
 	@Path("/people/{identifier_type}:{identifier}/addresses")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response updateAddress(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @FormParam("requestedBy") final String requestedBy,
 			  @FormParam("addressType") final String addressType,
 			  @FormParam("documentType") final String documentType,
 			  @FormParam("groupId") final String groupId,
 			  @FormParam("address1") final String address1,
 			  @FormParam("address2") final String address2,
 			  @FormParam("address3") final String address3,
 			  @FormParam("city") final String city,
 			  @FormParam("stateOrProvince") final String stateOrProvince,
 			  @FormParam("postalCode") final String postalCode,
 			  @FormParam("countryCode") final String countryCode,
 			  @FormParam("campusCode") final String campusCode,
 			  @FormParam("verifyAddressFlag") final String verifyAddressFlag);
 
 	/**
 	 * Add Names RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param requestedBy contains the requestor.
 	 * @param nameType contains the name type to be added.
 	 * @param documentType contains the document type if the name type is DOCUMENTED.
 	 * @param firstName contains the first name.
 	 * @param middleNames contains the middle names.
 	 * @param lastName contains the last name.
 	 * @param suffix contains the suffix.
 	 * @param nickname contains the person's nickname.
 	 * @return will return the results to the caller.
 	 */
 	@POST
 	@Path("/people/{identifier_type}:{identifier}/names")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response addName(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @FormParam("requestedBy") final String requestedBy,
 			  @FormParam("nameType") final String nameType,
 			  @FormParam("documentType") final String documentType,
 			  @FormParam("firstName") final String firstName,
 			  @FormParam("middleNames") final String middleNames,
 			  @FormParam("lastName") final String lastName,
 			  @FormParam("suffix") final String suffix,
 			  @FormParam("nickname") final String nickname);
 
 	/**
 	 * Update Names RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param requestedBy contains the requestor.
 	 * @param nameType contains the name type to be added.
 	 * @param documentType contains the document type if the name type is DOCUMENTED.
 	 * @param firstName contains the first name.
 	 * @param middleNames contains the middle names.
 	 * @param lastName contains the last name.
 	 * @param suffix contains the suffix.
 	 * @param nickname contains the person's nickname.
 	 * @return will return the results to the caller.
 	 */
 	@PUT
 	@Path("/people/{identifier_type}:{identifier}/names")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response updateName(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @FormParam("requestedBy") final String requestedBy,
 			  @FormParam("nameType") final String nameType,
 			  @FormParam("documentType") final String documentType,
 			  @FormParam("firstName") final String firstName,
 			  @FormParam("middleNames") final String middleNames,
 			  @FormParam("lastName") final String lastName,
 			  @FormParam("suffix") final String suffix,
 			  @FormParam("nickname") final String nickname);
 
 	/**
 	 * Add Phone RESTful service.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is updating the phone, this person will be an RA agent.
 	 * @param phoneType the type of phone number that is being updated.
 	 * @param phoneNumber the phone number of the update.
 	 * @param extension the extension, if available, of the update.
 	 * @param internationalNumber a flag indicating if the phone number is an international number
 	 * 
 	 * @return will return an HTTP response object.
 	 */
 	@POST
 	@Path("/people/{identifier_type}:{identifier}/phones")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response addPhone(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @FormParam("requestedBy") final String requestedBy,
 			  @FormParam("phoneType") final String phoneType,
 			  @FormParam("phoneNumber") final String phoneNumber,
 			  @FormParam("extension") final String extension,
 			  @FormParam("internationalNumber") final String internationalNumber);
 	
 	/**
 	 * Update Phone RESTful service.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is updating the phone, this person will be an RA agent.
 	 * @param phoneType the type of phone number that is being updated.
 	 * @param groupId the groupId of the phone record within the phone type
 	 * @param phoneNumber the phone number of the update.
 	 * @param extension the extension, if available, of the update.
 	 * @param internationalNumber a flag indicating if the phone number is an international number
 	 * 	 
 	 * @return will return an HTTP response object.
 	 */
 	@PUT
 	@Path("/people/{identifier_type}:{identifier}/phones")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response updatePhone(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @FormParam("requestedBy") final String requestedBy,
 			  @FormParam("phoneType") final String phoneType,
 			  @FormParam("groupId") final String groupId,
 			  @FormParam("phoneNumber") final String phoneNumber,
 			  @FormParam("extension") final String extension,
 			  @FormParam("internationalNumber") final String internationalNumber);
 
 	/**
 	 * Add User Comment RESTful Service
 	 *  
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is updating the comment, this person will be an RA agent.
 	 * @param userId contains the userId associated with the comment.
 	 * @param userCommentType contains the type of comment that is being added.
 	 * @param comment contains the comment
 	 * 
 	 * @return will return an HTTP response object.
 	 */
 	@POST
 	@Path("/people/{identifier_type}:{identifier}/comments")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response addComment(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @FormParam("requestedBy") final String requestedBy,
 			  @FormParam("userId") final String userId,
 			  @FormParam("userCommentType") final String userCommentType,
 			  @FormParam("comment") final String comment);
 
 	/**
 	 * Update User Comment RESTful Service
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is updating the comment, this person will be an RA agent.
 	 * @param userId contains the userId associated with the comment.
 	 * @param userCommentType contains the type of comment that is being added.
 	 * @param comment contains the comment
 	 * 
 	 * @return will return an HTTP response object.
 	 */
 	@PUT
 	@Path("/people/{identifier_type}:{identifier}/comments")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response updateComment(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @FormParam("requestedBy") final String requestedBy,
 			  @FormParam("userId") final String userId,
 			  @FormParam("userCommentType") final String userCommentType,
 			  @FormParam("comment") final String comment);
 
 	/**
 	 * Add Email RESTful service.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is updating the email address, this person will be an RA agent.
 	 * @param emailAddressType contains the type of email address that is being added.
 	 * @param emailAddress contains the email address
 	 * 
 	 * @return will return an HTTP response object.
 	 */
 	@POST
 	@Path("/people/{identifier_type}:{identifier}/emails")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response addEmail(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @FormParam("requestedBy") final String requestedBy,
 			  @FormParam("emailAddressType") final String emailAddressType,
 			  @FormParam("emailAddress") final String emailAddress);
 
 	/**
 	 * Update Email RESTful service.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is updating the email address, this person will be an RA agent.
 	 * @param emailAddressType contains the type of email address that is being added.
 	 * @param emailAddress contains the email address
 	 * 
 	 * @return will return an HTTP response object.
 	 */
 	@PUT
 	@Path("/people/{identifier_type}:{identifier}/emails")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response updateEmail(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @FormParam("requestedBy") final String requestedBy,
 			  @FormParam("emailAddressType") final String emailAddressType,
 			  @FormParam("emailAddress") final String emailAddress);
 
 
 	/**
 	 * Get Address RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param returnHistory contains a Y/N flag that indicates whether to return history or not.
 	 * @param addressType contains the type of address to query.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	@GET
 	@Path("/people/{identifier_type}:{identifier}/addresses")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response getAddress(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @DefaultValue("N") @QueryParam("history") final String returnHistory,
 			  @QueryParam("type") final String addressType,
 			  @QueryParam("requestedBy") final String requestedBy);
 	
 	/**
 	 * Get Address RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param addressKey contains the specific address record to be retrieved.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	@GET
 	@Path("/people/{identifier_type}:{identifier}/addresses/{address_key}")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response getAddressUsingKey(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @PathParam("address_key") final String addressKey,
 			  @QueryParam("requestedBy") final String requestedBy);
 	
 	/**
 	 * Get Email Address RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param returnHistory contains a Y/N flag that indicates whether to return history or not.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	@GET
 	@Path("/people/{identifier_type}:{identifier}/emails")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response getEmail(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @DefaultValue("N") @QueryParam("history") final String returnHistory,
 			  @QueryParam("requestedBy") final String requestedBy);
 	
 	/**
 	 * Get Email Address RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param emailKey contains the type of address to query.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	@GET
 	@Path("/people/{identifier_type}:{identifier}/emails/{email_key}")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response getEmailUsingKey(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @PathParam("email_key") final String emailKey,
 			  @QueryParam("requestedBy") final String requestedBy);
 	
 	/**
 	 * Get name RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param returnHistory contains a Y/N flag that indicates whether to return history or not.
 	 * @param nameType contains the type of name to query.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	@GET
 	@Path("/people/{identifier_type}:{identifier}/names")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response getName(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @DefaultValue("N") @QueryParam("history") final String returnHistory,
 			  @QueryParam("type") final String nameType,
 			  @QueryParam("requestedBy") final String requestedBy);
 	
 	/**
 	 * Get name RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param nameKey contains the specific name key to be returned.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	@GET
 	@Path("/people/{identifier_type}:{identifier}/names/{name_key}")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response getNameUsingKey(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @PathParam("name_key") final String nameKey,
 			  @QueryParam("requestedBy") final String requestedBy);
 
 	/**
 	 * Get phone RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param returnHistory contains a Y/N flag that indicates whether to return history or not.
 	 * @param phoneType contains the type of name to query.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	@GET
 	@Path("/people/{identifier_type}:{identifier}/phones")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response getPhone(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @DefaultValue("N") @QueryParam("history") final String returnHistory,
 			  @QueryParam("type") final String phoneType,
 			  @QueryParam("requestedBy") final String requestedBy);
 	
 	/**
 	 * Get phone RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param phoneKey contains the specific phone key to be returned.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	@GET
 	@Path("/people/{identifier_type}:{identifier}/phones/{phone_key}")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response getPhoneUsingKey(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @PathParam("phone_key") final String phoneKey,
 			  @QueryParam("requestedBy") final String requestedBy);
 	
 	/**
 	 * Get comment RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param userid contains the specific userid to query.
 	 * @param returnHistory contains a Y/N flag that indicates whether to return history or not.
 	 * @param commentType contains the type of comment to query..
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	@GET
 	@Path("/people/{identifier_type}:{identifier}/comments/{userid}")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response getComment(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @PathParam("userid") final String userid,
 			  @DefaultValue("N") @QueryParam("history") final String returnHistory,
 			  @QueryParam("type") final String commentType,
 			  @QueryParam("requestedBy") final String requestedBy);
 	
 	/**
 	 * Get comment RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param userid contains the specific userid to query.
 	 * @param commentKey contains the specific comment key to be returned.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	@GET
 	@Path("/people/{identifier_type}:{identifier}/comments/{userid}/{comment_key}")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response getCommentUsingKey(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @PathParam("userid") final String userid,
 			  @PathParam("comment_key") final String commentKey,
 			  @QueryParam("requestedBy") final String requestedBy);
 
 	/**
 	 * Get userid RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param returnHistory contains a Y/N flag that indicates whether to return history or not.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	@GET
 	@Path("/people/{identifier_type}:{identifier}/userids")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response getUserid(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @DefaultValue("N") @QueryParam("history") final String returnHistory,
 			  @QueryParam("requestedBy") final String requestedBy);
 	
 	/**
 	 * Get userid RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param userid contains the specific userid to query.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	@GET
 	@Path("/people/{identifier_type}:{identifier}/userids/{userid}")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response getUseridUsingUserid(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @PathParam("userid") final String userid,
 			  @QueryParam("requestedBy") final String requestedBy);
 
 	/**
 	 * Get person RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param returnHistory contains a Y/N flag that indicates whether to return history or not.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	@GET
 	@Path("/people/{identifier_type}:{identifier}")
 	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	Response getPerson(
 			  @PathParam("identifier_type") final String identifierType,
 			  @PathParam("identifier") final String identifier,
 			  @DefaultValue("N") @QueryParam("history") final String returnHistory,
 			  @QueryParam("requestedBy") final String requestedBy);
 
 }
