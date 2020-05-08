 package edu.psu.iam.cpr.core.api;
 
 import java.text.ParseException;
 import java.util.Map;
 
 
 import javax.jms.JMSException;
 
 import org.apache.log4j.Logger;
 import org.json.JSONException;
 
 import edu.psu.iam.cpr.core.api.helper.ApiHelper;
 import edu.psu.iam.cpr.core.api.returns.ServiceReturn;
 import edu.psu.iam.cpr.core.database.Database;
 import edu.psu.iam.cpr.core.error.CprException;
 import edu.psu.iam.cpr.core.error.ReturnType;
 import edu.psu.iam.cpr.core.messaging.JsonMessage;
 import edu.psu.iam.cpr.core.service.helper.ServiceCoreReturn;
 /**
  * This class provides the abstract base API.
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
  * @package edu.psu.iam.cpr.api
  * @author $Author$
  * @version $Rev$
  * @lastrevision $Date$
  */
 public abstract class BaseApi {
 	
 	/** Contains the log4j Logger instance */
 	private static final Logger LOG4J_LOGGER = Logger.getLogger(BaseApi.class);
 	
 	/** Constants for dump parameters function */
 	public static final String NAME_TYPE_KEY = "name_type";
 	public static final String NAME_DOCUMENT_TYPE_KEY = "name_document_type";
 	public static final String DOCUMENT_TYPE_KEY = "document_type";
 	public static final String FIRST_NAME_KEY = "first_name";
 	public static final String MIDDLE_NAMES_KEY = "middle_names";
 	public static final String LAST_NAME_KEY = "last_name";
 	public static final String SUFFIX_KEY = "suffix";
 	public static final String ADDRESS1_KEY = "addr1";
 	public static final String ADDRESS2_KEY = "addr2";
 	public static final String ADDRESS3_KEY = "addr3";
 	public static final String ADDRESS_DOCUMENT_TYPE_KEY = "address_document_type";
 	public static final String ADDRESS_TYPE_KEY = "address_type";
 	public static final String ADDRESS_GROUP_ID_KEY = "address_group_id";
 	public static final String CITY_KEY = "city";
 	public static final String STATE_KEY = "state";
 	public static final String POSTALCODE_KEY = "postalcode";
 	public static final String PLUS4_KEY = "plus4";
 	public static final String CAMPUS_KEY = "campus";
 	public static final String COUNTRY_KEY = "country";
 	public static final String GENDER_KEY = "gender";
 	public static final String PHONE_TYPE_KEY = "phone_type";
 	public static final String PHONE_NUMBER_KEY = "phone_number";
 	public static final String PHONE_EXTENSION_KEY = "phone_extension";
 	public static final String PHONE_INTERNATIONAL_NUMBER_KEY = "phone_international_number";
 	public static final String PHONE_GROUP_ID_KEY = "phone_group_id";
 	public static final String EMAIL_ADDRESS_KEY = "email";
 	public static final String EMAIL_ADDRESS_TYPE_KEY = "email_type";
 	public static final String DOB_KEY = "dob";
 	public static final String SSN_KEY = "ssn";
 	public static final String PSUID_KEY = "psuid";
 	public static final String USERID_KEY = "userid";
 	public static final String PERSONID_KEY = "personid";
 	public static final String AFFILIATION_KEY = "affiliation";
 	public static final String CONFIDENTIALITY_TYPE_KEY = "confidentiality_type";
 	public static final String CREDENTIAL_TYPE_KEY = "credential_type";
 	public static final String CREDENTIAL_DATA_KEY = "credential_data";
 	public static final String LINKAGE_TYPE_KEY = "linkage_type";
 	public static final String LINKAGE_IDENTIFIER_TYPE_KEY = "linkage_identifier_type";
 	public static final String LINKAGE_IDENTIFIER_KEY = "linkage_identifier";
 	public static final String PHOTO_KEY = "photo";
 	public static final String PHOTO_DATE_TAKEN_KEY = "photo_date_taken";
	public static final String USER_COMMENT_TYPE_KEY = "user_comment";
 	public static final String USER_COMMENT_KEY = "user_comment";
 	public static final String GROUP_ID_KEY = "group_id_key";
 	public static final String RANK_CUTOFF_KEY = "rank_cutoff";
 	public static final String VERIFY_ADDRESS_FLAG_KEY = "verify_address";
 	public static final String MATCH_DATA_TYPE_KEY = "match_data_type";
 	public static final String MATCH_DATA_VALUE_KEY = "match_data_value";
 	public static final String RETURN_HISTORY_KEY = "return_history";
 	public static final String FEDERATION_NAME_KEY = "federation_name";
 	public static final String ASSIGN_PSU_ID_FLAG_KEY = "assign_psu_id_flag";
 	public static final String ASSIGN_USERID_FLAG_KEY = "assign_userid_flag";
 	public static final String ID_CARD_TYPE_KEY = "id_card_type";
 	public static final String ID_CARD_NUMBER_KEY = "id_card_number";
 	public static final String ID_SERIAL_NUMBER_KEY = "id_serial_number";
 	public static final String IDENTIFIER_TYPE_KEY = "identifier_type";
 	public static final String IDENTIFIER_KEY = "identifier";
 	public static final String EVENT_USER_ID_KEY = "event_user_id";
 	public static final String EVENT_IP_ADDRESS_KEY = "event_ip_address";
 	public static final String EVENT_WORKSTATION_KEY = "event_workstation_key";
 	public static final String DO_FIND_PERSON_KEY = "do_find_person";
 
 	
 	/**
 	 * Provides the abstract template for an API.
 	 * @param apiName contains the name of the API.
 	 * @param db contains the database connection.
 	 * @param updatedBy contains the entity that is updating the record.
 	 * @param serviceCoreReturn contains the service core return object.
  	 * @param otherParameters contain the map of parameters to the API.
 	 * @param checkAuthorization contains a flag that determines if AuthZ is to be checked or not.
 	 * @return will return a service return object.
 	 * @throws CprException will be thrown if there are any CPR related problems.
 	 * @throws JSONException will be thrown if there are any JSON related problems.
 	 * @throws ParseException will be thrown if there are any parsing problems.
 	 * @throws JMSException will be thrown if there are any JMS problems.
 	 */
 	public ServiceReturn implementApi(final String apiName, final Database db, final String updatedBy, 
 			final ServiceCoreReturn serviceCoreReturn, final Map<String, Object> otherParameters, 
 			final boolean checkAuthorization) throws CprException, JSONException, ParseException, JMSException {
 		
 		LOG4J_LOGGER.info(apiName + ": Start of api.");
 
 		// dump the parameters.
 		LOG4J_LOGGER.info(apiName + ": Input Parameters = " + ApiHelper.dumpParameters(otherParameters));	
 		
 		// Run the api.
 		final JsonMessage jsonMessage = runApi(apiName, db, serviceCoreReturn, updatedBy, otherParameters, checkAuthorization);
 		
 		// If the message is set, send out the messages to the service provisioners.
 		if (jsonMessage != null) {
 			ApiHelper.sendMessagesToServiceProviders(apiName, db, jsonMessage); 
 		}
 
 		LOG4J_LOGGER.info(apiName + ": End of api.");
 		
 		return new ServiceReturn(ReturnType.SUCCESS.index(), ApiHelper.SUCCESS_MESSAGE);
 		
 	}
 	
 	public abstract JsonMessage runApi(final String apiName, final Database db, final ServiceCoreReturn serviceCoreReturn, 
 			final String updatedBy, final Map<String, Object> otherParameters, final boolean checkAuthorization) 
 				throws CprException, JSONException, ParseException;
 
 }
