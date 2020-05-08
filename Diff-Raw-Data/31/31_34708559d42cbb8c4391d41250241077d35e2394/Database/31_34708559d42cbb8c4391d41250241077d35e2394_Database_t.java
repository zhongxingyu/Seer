 /* SVN FILE: $Id: Database.java 5340 2012-09-27 14:48:52Z jvuccolo $ */
 package edu.psu.iam.cpr.core.database;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.jdbc.Work;
 import org.hibernate.type.StandardBasicTypes;
 
 import edu.psu.iam.cpr.core.database.beans.IdentifierType;
 import edu.psu.iam.cpr.core.database.types.AccessType;
 import edu.psu.iam.cpr.core.database.types.AffiliationsType;
 import edu.psu.iam.cpr.core.error.CprException;
 import edu.psu.iam.cpr.core.error.ReturnType;
 
 /**
  * Database is a utility class that will facility the opening and closing of database connections
  * to the database pool that maintained in the broker.  In addition it provides helper methods
  * that are used to obtain person identifiers from the CPR.
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
  * @package edu.psu.iam.cpr.core.database
  * @author $Author: jvuccolo $
  * @version $Rev: 5340 $
  * @lastrevision $Date: 2012-09-27 10:48:52 -0400 (Thu, 27 Sep 2012) $
  */
 public class Database {
 
 	/** Person identifier string */
 	public static final String PERSON_ID_IDENTIFIER = "PERSON_ID";
 	
 	/** Userid identifier string */
 	public static final String USERID_IDENTIFIER 	= "USERID";
 	
 	/** Social security number identifier string */
 	public static final String SSN_IDENTIFIER 		= "SSN";
 	
 	/** PSU ID identifier string */
 	public static final String PSU_ID_IDENTIFIER 	= "PSU_ID";
 	
 	/** ID Card string */
 	public static final String ID_CARD_IDENTIFIER 	= "ID_CARD";
 	
 	/** Instance of logger */
 	private static final Logger LOG4J_LOGGER = Logger.getLogger(Database.class);
 
 	/** Cpr Access Groups Key */
 	private long cprAccessGroupsKey = 0;
 	
 	/** Registration Authority Key. */
 	private long registrationAuthorityKey = 0;
 	
 	private static final int CPR_ACCESS_GROUPS_KEY = 0;
 	private static final int GRP_MBRS_SUSPEND_FLAG = 1;
 	private static final int CPR_GRPS_SUSPEND_FLAG = 2;
 	private static final int WEB_SRV_SUSPEND_FLAG = 3;
 
 	private static final int BUFFER_SIZE = 512;
 	
 	/**
 	 * Value that represents something that is not found.
 	 */
 	private static final long NOT_FOUND_VALUE = -1L;
 
 	/** 
 	 * Contains a table's columns.
 	 */
 	private HashMap<String, TableColumn> tableColumns = null;
 	
 	/**
 	 * Contains an open hibernate session.
 	 */
 	private Session session = null;
 	
 	/**
 	 * Contains the identifier type.
 	 */
 	private IdentifierType identifierType = null;
 	
 	
 	/**
 	 * openSession routine attempts to obtain a database connection from the ESB connection
 	 * pool.
 	 * @param sessionFactory hibernate session factory.
 	 */
 	public void openSession(SessionFactory sessionFactory) {
 		
 		session = sessionFactory.getCurrentSession();
 		session.beginTransaction();
 	}
 	
 	/**
 	 * closeSession routine will attempt to close an open database connection freeing
 	 * up a resource in the connection pool.
 	 * 
 	 */
 	public void closeSession() {
 		try {
 			getSession().getTransaction().commit();
 		}
 		finally {
 			resetSession();
 		}
 	}
 	
 	/**
 	 * This routine is used to rollback a session.
 	 */
 	public void rollbackSession() {
 		try {
 			getSession().getTransaction().rollback();
 		}
 		finally {
 			resetSession();
 		}
 	}
 
 	/**
 	 * This routine will reset a hibernate session value to its initial value of null.
 	 */
 	public void resetSession() {
 		session = null;
 	}
 
 	/**
 	 * This routine will return a boolean flag to indicate whether a hibernate session is open or not.
 	 * @return will return true if the connection is open, otherwise it will return false.
 	 */
 	public boolean isSessionOpen() {
 		return (getSession() != null) ? true : false;
 	}
 	
 	/**
 	 * getSession will return the open database connection.
 	 * @return returns the open database connection.
 	 */
 	public Session getSession() {
 		
 		return session;
 	}
 	
 	/**
 	 * setSession will accept a hibernate session and store it inside of the database object for use at a later time.
 	 * @param session contains the session to be stored.
 	 */
 	public void setSession(Session session) {
 		this.session = session;
 	}
 	
 	/**
 	 * This method is used to find a registration authority based on a server principal.
 	 * @param principalId contains the ra server principal.
 	 * @param serviceName contains the name of the calling service.
 	 * @return will return a list of longs contains the registration authority key and the ra server principal key.
 	 * @throws CprException will be thrown if there are any CPR Related problems.
 	 */
 	private List<Long> findRegistrationAuthority(String principalId, String serviceName) throws CprException {
 		
		Long localRegistrationAuthoritykey 		= NOT_FOUND_VALUE;
 		Long raServerPrincipalKey 				= NOT_FOUND_VALUE;
 		
 		final int RA_KEY_INDEX 					= 0;
 		final int RA_SUSPEND_FLAG 				= 1;
 		final int RA_SERVER_PRINCIPAL_KEY_INDEX = 2;
 		
 		String suspendFlag 						= "Y";
 
 		// Build the query.
 		final StringBuilder sb = new StringBuilder(BUFFER_SIZE);
 		sb.append("SELECT ra.registration_authority_key, ra.suspend_flag, rasrvrprinc.ra_server_principal_key ");
 		sb.append("FROM registration_authority ra JOIN ra_server_principals rasrvrprinc ");
 		sb.append("ON ra.registration_authority_key = rasrvrprinc.registration_authority_key ");
 		sb.append("WHERE rasrvrprinc.ra_server_principal = :ra_server_principal_in ");
 		sb.append("AND ra.end_date IS NULL ");
 		sb.append("AND rasrvrprinc.end_date IS NULL");
 
 		// Create the query, bind the input parameters and determine the output parameters.
 		SQLQuery query = session.createSQLQuery(sb.toString());
 		query.setParameter("ra_server_principal_in", principalId);
 		query.addScalar("registration_authority_key", StandardBasicTypes.LONG);
 		query.addScalar("suspend_flag", StandardBasicTypes.STRING);
 		query.addScalar("ra_server_principal_key", StandardBasicTypes.LONG);
 
 		// See if a record is found, if so get its data.
 		Iterator<?> it = query.list().iterator();
 		if (it.hasNext()) {
 			Object res[] = (Object []) it.next();
			localRegistrationAuthoritykey= (Long) res[RA_KEY_INDEX];
 			suspendFlag 			= (String) res[RA_SUSPEND_FLAG];
 			raServerPrincipalKey 	= (Long) res[RA_SERVER_PRINCIPAL_KEY_INDEX];
 		}
 		
 	
 		// Is the RA suspended?
		if (localRegistrationAuthoritykey.equals(NOT_FOUND_VALUE) ||
 				raServerPrincipalKey.equals(NOT_FOUND_VALUE) ||
 				suspendFlag.equals("Y")) {
 			throw new CprException(ReturnType.NOT_AUTHORIZED_EXCEPTION, serviceName);
 		}
 		
 		List<Long> methodReturn = new ArrayList<Long>();
		methodReturn.add(localRegistrationAuthoritykey);
 		methodReturn.add(raServerPrincipalKey);
 		return methodReturn;
 		
 	}
 	
 	/**
 	 * This method is used to verify that the client's IP address is authorized to call the service for the particular RA.
 	 * @param raServerPrincipalKey contains the ra server principal key associated with the RA.
 	 * @param serviceName contains the name of the service that is being called.
 	 * @param clientIpAddress contains the ip address of the caller.
 	 * @throws CprException will be thrown if there are any CPR related problems.
 	 */
 	private void verifyClientIpAddress(Long raServerPrincipalKey, String serviceName, String clientIpAddress) throws CprException {
 		
 		Long localRaServerPrincipalKey = NOT_FOUND_VALUE;
 		final String WILD_CARD_IP = "*";
 		
 		final StringBuffer sb = new StringBuffer();
 		sb.append("select ra_server_principal_key from server_principal_ip ");
 		sb.append("where ra_server_principal_key = :ra_server_principal_key AND ");
 		sb.append("(ip_address = :wildcard or ip_address = :client_ip_address)");
 		
 		SQLQuery query = session.createSQLQuery(sb.toString());
 		query.addScalar("ra_server_principal_key", StandardBasicTypes.LONG);
 		query.setParameter("ra_server_principal_key", raServerPrincipalKey);
 		query.setParameter("wildcard", WILD_CARD_IP);
 		query.setParameter("client_ip_address", clientIpAddress);
 		
 		for (Iterator<?> it = query.list().iterator(); it.hasNext(); ) {
 			localRaServerPrincipalKey = (Long) it.next();
 		}
 		
 		if (localRaServerPrincipalKey.equals(NOT_FOUND_VALUE)) {
 			throw new CprException(ReturnType.NOT_AUTHORIZED_EXCEPTION, serviceName);
 		}
 	}
 	
 	/**
 	 * This routine will determine if a particular server principal is authorized to call a service.
 	 * @param principalId contains the requestor's principal identifier.
 	 * @param requestor contains the userid of the person requesting access.
 	 * @param serviceName contains the name of the service.
 	 * @param clientIpAddress contains the client ip address.
 	 * @throws CprException 
 	 */
 	public void requestorAuthorized(String principalId, String requestor, String serviceName, String clientIpAddress) throws CprException  {
 
 		String grpMbrsSuspendFlag = "Y";
 		String cprAccGrpsSuspendFlag = "Y";
 		String webSrvAccSuspendFlag = "Y";
		Long localCprAccessGroupsKey = NOT_FOUND_VALUE;
 		
 		// Get the RA information.
 		List<Long> methodReturn = findRegistrationAuthority(principalId, serviceName);
		Long localRegistrationAuthorityKey = methodReturn.get(0);
 		Long raServerPrincipalKey = methodReturn.get(1);
 		
 		// Determine if the client ip address is valid for the particular RA.
 		verifyClientIpAddress(raServerPrincipalKey, serviceName, clientIpAddress);
 
 		// Determine the user's status and group for the particular RA.
 			
 		// Build the query.
 		StringBuffer sb = new StringBuffer();
 		sb.append("SELECT cpr_access_groups_key, grpmbrs_suspend_flag, cpraccgprs_suspend_flag, websrvacc_suspend_flag ");
 		sb.append("FROM v_ra_group_web_service ");
 		sb.append("WHERE registration_authority_key = :l_ra_key ");
 		sb.append("AND ra_server_principal_key = :ra_sp_key ");
 		sb.append("AND web_service = :web_service_in ");
 		sb.append("AND userid = :requested_by_in");
 
 		// Create the query, bind the parameters and determine the returns.
 		SQLQuery query = session.createSQLQuery(sb.toString());
		query.setParameter("l_ra_key", localRegistrationAuthorityKey);
 		query.setParameter("ra_sp_key", raServerPrincipalKey);
 		query.setParameter("web_service_in", serviceName);
 		query.setParameter("requested_by_in", requestor);
 		query.addScalar("cpr_access_groups_key", StandardBasicTypes.LONG);
 		query.addScalar("grpmbrs_suspend_flag", StandardBasicTypes.STRING);
 		query.addScalar("cpraccgprs_suspend_flag", StandardBasicTypes.STRING);
 		query.addScalar("websrvacc_suspend_flag", StandardBasicTypes.STRING);
 
 		// Perform the query.
 		for (Iterator<?> it = query.list().iterator(); it.hasNext();) {
 			Object res[] = (Object []) it.next();
			localCprAccessGroupsKey = (Long) res[CPR_ACCESS_GROUPS_KEY];
 			grpMbrsSuspendFlag = (String) res[GRP_MBRS_SUSPEND_FLAG];
 			cprAccGrpsSuspendFlag = (String) res[CPR_GRPS_SUSPEND_FLAG];
 			webSrvAccSuspendFlag = (String) res[WEB_SRV_SUSPEND_FLAG];
 		}
 
 
 		// If any of the suspend flags is set to Yes, we need to throw an exception.
		if (localCprAccessGroupsKey.equals(NOT_FOUND_VALUE) ||
 				grpMbrsSuspendFlag.equals("Y") ||
 				cprAccGrpsSuspendFlag.equals("Y") ||
 				webSrvAccSuspendFlag.equals("Y")) {
 			throw new CprException(ReturnType.NOT_AUTHORIZED_EXCEPTION, serviceName);
 		}
 
		setCprAccessGroupsKey(localCprAccessGroupsKey);
		setRegistrationAuthorityKey(localRegistrationAuthorityKey);
 				
 	}
 
 	/**
 	 * This routine is used to verify that the requester is allowed to perform an operation on a particular data type.
 	 * This routine will return true if the operation is allowed, otherwise it will throw an exception.
 	 * @param dataResource contains the data source that is being checked.
 	 * @param action contains the action that is being checked.
 	 * @param requestedBy contains the access id of the perform who requested this operation.
 	 * @return will return true if successful.
 	 * @throws CprException will be thrown if the access is denied.
 	 */
 	public boolean isDataActionAuthorized(String dataResource, String action, String requestedBy) 
 		throws CprException {
 
 		// Verify that the operation being checked is against a valid data key.
 		final Long dataTypeKey = AccessType.valueOf(dataResource.toUpperCase().trim()).index();
 		final Long accessOperationKey = AccessType.valueOf(action.toUpperCase().trim()).index();
 		boolean dataKeyValid = false;
 		try {
 			
 			// Build the query.
 			final StringBuilder sb = new StringBuilder(BUFFER_SIZE);
 	        sb.append("SELECT data_types.data_type_key ");
 	        sb.append("FROM data_types ");
 	        sb.append("WHERE data_types.data_type_key = :data_type_key_in ");
 	        sb.append("AND data_types.active_flag = 'Y' ");
 
 	        // Create the query, bind the parameters and set the return type.
 	        final SQLQuery query = session.createSQLQuery(sb.toString());
 	        query.setParameter("data_type_key_in", dataTypeKey);
 			query.addScalar("data_type_key", StandardBasicTypes.LONG);
 			
 			for (final Iterator<?> it = query.list().iterator(); it.hasNext(); ) {
 				it.next();
 				dataKeyValid = true;
 			}
 		}
 		finally {
 			if (! dataKeyValid) {
 				throw new CprException(ReturnType.DATA_CHANGE_EXCEPTION,dataResource);
 			}
 		}
 		
 		// Do the query to determine if they have access.
 		String readFlag = "N";
 		String writeFlag = "N";
 		String archiveFlag ="N";
 		final StringBuilder sb = new StringBuilder(BUFFER_SIZE);
 		sb.append("SELECT v_group_data_type_access.read_flag, v_group_data_type_access.write_flag, ");
 		sb.append("v_group_data_type_access.archive_flag ");
 		sb.append("FROM v_group_data_type_access ");
 		sb.append("WHERE v_group_data_type_access.cpr_access_groups_key = :cpr_access_groups_key_in ");
 		sb.append("AND v_group_data_type_access.data_type_key = :data_type_key_in");
 
 		// Create the query, bind the parameters and set the return type.
 		final SQLQuery query = session.createSQLQuery(sb.toString());
 		query.setParameter("cpr_access_groups_key_in", getCprAccessGroupsKey());
 		query.setParameter("data_type_key_in", dataTypeKey);
 		query.addScalar("read_flag", StandardBasicTypes.STRING);
 		query.addScalar("write_flag", StandardBasicTypes.STRING);
 		query.addScalar("archive_flag", StandardBasicTypes.STRING);
 
 		for (final Iterator<?> it = query.list().iterator(); it.hasNext(); ) {
 			Object res[] = (Object []) it.next();
 			readFlag = (String) res[0];
 			writeFlag = (String) res[1];
 			archiveFlag = (String) res[2];
 		}
 		
 		boolean hasAccess = false;
 		if (accessOperationKey == AccessType.ACCESS_OPERATION_ARCHIVE.index()) {
 			hasAccess = archiveFlag.equals("Y");
 		}
 		else if (accessOperationKey == AccessType.ACCESS_OPERATION_READ.index()) {
 			hasAccess = readFlag.equals("Y");
 		}
 		else if (accessOperationKey == AccessType.ACCESS_OPERATION_WRITE.index()) {
 			hasAccess = writeFlag.equals("Y");
 		}
 		
 		if (! hasAccess) {
 			throw new CprException(ReturnType.DATA_CHANGE_EXCEPTION, AccessType.get(dataTypeKey).toString());
 		}
 		
 		return hasAccess;
 	}
 	
 	/**
 	 * This routine is used to verify that the requester is allowed to perform an operation on a particular data type.
 	 * This routine will return true if the operation is allowed, otherwise it will throw an exception.
 	 * @param iamGroupKey contains the iam group key which indicates which group the user is a member of. 
 	 * @param dataTypeKey contains the data type key associated with the data element.
 	 * @param accessOperationKey contains the access operation key which indicates the type of operation.
 	 * @param requestedBy contains the access id of the perform who requested this operation.
 	 * @return will return true if successful.
 	 * @throws CprException will be thrown if the access is denied.
 	 */
 	public boolean isDataActionAuthorizedOldCode(long iamGroupKey, long dataTypeKey, long accessOperationKey, String requestedBy) 
 		throws CprException {
 
 		// Verify that the operation being checked is against a valid data key.
 		boolean dataKeyValid = false;
 		try {
 			
 			// Build the query.
 			final StringBuilder sb = new StringBuilder(BUFFER_SIZE);
 	        sb.append("SELECT data_types.data_type_key ");
 	        sb.append("FROM data_types ");
 	        sb.append("WHERE data_types.data_type_key = :data_type_key_in ");
 	        sb.append("AND data_types.active_flag = 'Y' ");
 
 	        // Create the query, bind the parameters and set the return type.
 	        final SQLQuery query = session.createSQLQuery(sb.toString());
 			query.setParameter("data_type_key_in", dataTypeKey);
 			query.addScalar("data_type_key", StandardBasicTypes.LONG);
 			
 			final Iterator<?> it = query.list().iterator();
 			if (it.hasNext()) {
 				dataKeyValid = true;
 			}
 		}
 		finally {
 			if (! dataKeyValid) {
 				throw new CprException(ReturnType.DATA_CHANGE_EXCEPTION, AccessType.get(dataTypeKey).toString());
 			}
 		}
 		
 		// Do the query to determine if they have access.
 		String readFlag = "N";
 		String writeFlag = "N";
 		String archiveFlag ="N";
 		final StringBuilder sb = new StringBuilder(BUFFER_SIZE);
 		sb.append("SELECT v_group_data_type_access.read_flag, v_group_data_type_access.write_flag, ");
 		sb.append("v_group_data_type_access.archive_flag ");
 		sb.append("FROM v_group_data_type_access ");
 		sb.append("WHERE v_group_data_type_access.iam_group_key = :iam_group_key_in ");
 		sb.append("AND v_group_data_type_access.data_type_key = :data_type_key_in");
 
 		// Create the query, bind the parameters and set the return type.
 		final SQLQuery query = session.createSQLQuery(sb.toString());
 		query.setParameter("iam_group_key_in", iamGroupKey);
 		query.setParameter("data_type_key_in", dataTypeKey);
 		query.addScalar("read_flag", StandardBasicTypes.STRING);
 		query.addScalar("write_flag", StandardBasicTypes.STRING);
 		query.addScalar("archive_flag", StandardBasicTypes.STRING);
 
 		final Iterator<?> it = query.list().iterator();
 		if (it.hasNext()) {
 			Object res[] = (Object []) it.next();
 			readFlag = (String) res[0];
 			writeFlag = (String) res[1];
 			archiveFlag = (String) res[2];
 		}
 		
 		boolean hasAccess = false;
 		if (accessOperationKey == AccessType.ACCESS_OPERATION_ARCHIVE.index()) {
 			hasAccess = archiveFlag.equals("Y");
 		}
 		else if (accessOperationKey == AccessType.ACCESS_OPERATION_READ.index()) {
 			hasAccess = readFlag.equals("Y");
 		}
 		else if (accessOperationKey == AccessType.ACCESS_OPERATION_WRITE.index()) {
 			hasAccess = writeFlag.equals("Y");
 		}
 		
 		if (! hasAccess) {
 			throw new CprException(ReturnType.DATA_CHANGE_EXCEPTION, AccessType.get(dataTypeKey).toString());
 		}
 		
 		return hasAccess;
 	}
 	/**
 	 * This routine is used to determine if an RA is authorize to assign an affiliation.
 	 * @param affiliationType - contains the affiliation
 	 * @param requestedBy - userid of the requestor
 	 * 
 	 * @return true if ra is authorized for affiliation
 	 * 
 	 * @throws CprException
 	 */
 	public boolean isAffiliationAccessAuthorized(String affiliationType, String requestedBy)  
 		throws CprException {
 
 		final Long affiliationKey = AffiliationsType.valueOf(affiliationType.toUpperCase().trim()).index();
 		boolean affiliationKeyValid = false;
 		final StringBuilder sb = new StringBuilder(BUFFER_SIZE);
 			
 		// Build the query.
 
 		sb.append("SELECT affiliations.affiliation_key ");
 		sb.append("FROM affiliations ");
 		sb.append("WHERE affiliations.affiliation_key = :affiliation_key_in ");
 		sb.append("AND affiliations.active_flag = 'Y' ");
 
 		SQLQuery query = session.createSQLQuery(sb.toString());
 		query.setParameter("affiliation_key_in", affiliationKey);
 		query.addScalar("affiliation_key", StandardBasicTypes.LONG);
 
 		Iterator<?> it = query.list().iterator();
 		if (it.hasNext()) {
 			affiliationKeyValid = true;
 		}
 		if (! affiliationKeyValid) {
 			throw new CprException(ReturnType.DATA_CHANGE_EXCEPTION, AffiliationsType.get(affiliationKey).toString());
 		}
 		sb.setLength(0);
 		sb.append("select * FROM ra_affiliation ");
 		sb.append("WHERE affiliation_key = :affiliation_key_in ");
 		sb.append("AND registration_authority_key= :ra_type_key_in ");
 		sb.append("AND end_date is null ");
 		// Create the query, bind the parameters and set the return type.
 		query = session.createSQLQuery(sb.toString());
 		query.setParameter("affiliation_key_in", affiliationKey);
 		query.setParameter("ra_type_key_in", getRegistrationAuthorityKey());
 
 		it = query.list().iterator();
 		if ( !it.hasNext()) {
 			affiliationKeyValid = false;
 		}		
 		if (! affiliationKeyValid) {
 			throw new CprException(ReturnType.DATA_CHANGE_EXCEPTION, AffiliationsType.get(affiliationKey).toString());
 		}
 		return affiliationKeyValid;
 	}
 	
 	/**
 	 * This routine is used to obtain a person identifier using a psu id number.
 	 * @param psuId contains the psu id number.
 	 * @return person id if the psu id can be found, otherwise it will return a -1 to indicate an error.
 	 * @throws CprException 
 	 */
 	public long getPersonIdUsingPsuId(String psuId) throws CprException {
 		
 		Long personId = NOT_FOUND_VALUE;
 
 		final String sqlQuery = "SELECT person_id FROM psu_id WHERE psu_id = :psuid AND end_date IS NULL";
 		final SQLQuery query = session.createSQLQuery(sqlQuery);
 		query.setParameter("psuid", psuId);
 		query.addScalar("person_id", StandardBasicTypes.LONG);
 		final Iterator<?> it = query.list().iterator();
 		if (it.hasNext()) {
 			personId = (Long) it.next();
 		}
 
 		if (personId.equals(NOT_FOUND_VALUE)) {
 			throw new CprException(ReturnType.PERSON_NOT_FOUND_EXCEPTION);		
 		}
 		
 		return personId;
 		
 	}
 	
 	/**
 	 * This routine is used to obtain a person identifier using a userid.
 	 * @param userid contains the userid to be used in the search.
 	 * @return person id if the userid can be found, otherwise it will return a -1 to indicate an error.
 	 * @throws CprException exception indicates a cpr specific java exception.
 	 */
 	public long getPersonIdUsingUserid(String userid) throws CprException  {
 		
 		Long personId = NOT_FOUND_VALUE;
 
 		final String sqlQuery = "SELECT person_id FROM userid WHERE userid = :userid";
 		final SQLQuery query = session.createSQLQuery(sqlQuery);
 		query.setParameter("userid", userid);
 		query.addScalar("person_id", StandardBasicTypes.LONG);
 		final Iterator<?> it = query.list().iterator();
 		if (it.hasNext()) {
 			personId = (Long) it.next();
 		}
 		
 		if (personId.equals(NOT_FOUND_VALUE)) {
 			throw new CprException(ReturnType.PERSON_NOT_FOUND_EXCEPTION);		
 		}
 		
 		return personId;
 	}
 	
 	/**
 	 * This routine is used to find a person using their id card number.
 	 * @param idCard contains the id card that is used to be search for.
 	 * @return will return the person identifier if a user was found with the correct id.
 	 * @throws CprException will be thrown if there are any CPR specific problems.
 	 */
 	public long getPersonIdUsingIdCard(String idCard) throws CprException  {
 		
 		Long personId = NOT_FOUND_VALUE;
 
 		final String sqlQuery = "SELECT person_id FROM person_id_card WHERE id_card_number = :idcard AND end_date IS NULL";
 		final SQLQuery query = session.createSQLQuery(sqlQuery);
 		query.setParameter("idcard", idCard);
 		query.addScalar("person_id", StandardBasicTypes.LONG);
 		final Iterator<?> it = query.list().iterator();
 		if (it.hasNext()) {
 			personId = (Long) it.next();
 		}
 		
 		if (personId.equals(NOT_FOUND_VALUE)) {
 			throw new CprException(ReturnType.PERSON_NOT_FOUND_EXCEPTION);		
 		}
 		
 		return personId;
 	}
 	
 	/**
 	 * This routine is used to determine whether a userid/person id is valid and whether the userid is still active.
 	 * @param personId contains the person identifier to do the query for.
 	 * @param userid contains the userid to do the query for.
 	 * @return will return true if valid, otherwise it will return false.
 	 */
 	public boolean isValidUserid(Long personId, String userid) {
 
 		boolean found = false;
 
 		final String sqlQuery = "SELECT person_id FROM userid WHERE userid = :userid AND person_id = :person_id AND end_date IS NULL";
 		final SQLQuery query = session.createSQLQuery(sqlQuery);
 		query.setParameter("person_id", personId);
 		query.setParameter("userid", userid);
 		query.addScalar("person_id", StandardBasicTypes.LONG);
 		found =  (query.list().size() == 0) ? false : true;
 
 		return found;
 	}
 	
 	/**
 	 * This routine is used to determine if a person with a person identifier exists in the CPR or not.  
 	 * @param personId contains the person identifier to do a search for.
 	 * @return returns the person identifier found.
 	 * @throws CprException exception indicates a cpr specific java exception.
 	 */
 	public long getPersonIdUsingPersonId(long personId) throws CprException {
 		
 		Long personIdOut = NOT_FOUND_VALUE;
 
 		final String sqlQuery = "SELECT person_id FROM person WHERE person_id = :personid";
 		final SQLQuery query = session.createSQLQuery(sqlQuery);
 		query.setParameter("personid", personId);
 		query.addScalar("person_id", StandardBasicTypes.LONG);
 		final Iterator<?> it = query.list().iterator();
 		if (it.hasNext()) {
 			personIdOut = (Long) it.next();
 		}
 
 		if (personIdOut.equals(NOT_FOUND_VALUE)) {
 			throw new CprException(ReturnType.PERSON_NOT_FOUND_EXCEPTION);		
 		}
 
 		return personId;
 	}
 	
 	/**
 	 * This routine is used to obtain a person identifier using a SOR identifier.
 	 * @param identifierType contains the identifier type.
 	 * @param identifier contains the identifier value.
 	 * @return will return the person identifier or throw an exception.
 	 * @throws CprException will be thrown if there is a CPR specific problem.
 	 */
 	public long getPersonIdUsingPersonIdentifier(IdentifierType identifierType, String identifier) throws CprException {
 		
 		Long personId = NOT_FOUND_VALUE;
 
 		final StringBuilder sb = new StringBuilder(BUFFER_SIZE);
 		sb.append("SELECT person_id FROM person_identifier WHERE type_key = :type_key ");
 		sb.append("AND identifier_value = :identifier_value ");
 		sb.append("AND end_date IS NULL");
 		final SQLQuery query = session.createSQLQuery(sb.toString());
 		query.setParameter("type_key", identifierType.getTypeKey());
 		query.setParameter("identifier_value", identifier);
 		query.addScalar("person_id", StandardBasicTypes.LONG);
 		final Iterator<?> it = query.list().iterator();
 		if (it.hasNext()) {
 			personId = (Long) it.next();
 		}
 			
 		if (personId.equals(NOT_FOUND_VALUE)) {
 			throw new CprException(ReturnType.PERSON_NOT_FOUND_EXCEPTION);		
 		}
 		
 		return personId;
 	}
 	
 	/**
 	 * The purpose of this routine is to accept an identifier and a IdentifierType and obtain from the database the person's
 	 * Person Identifier.
 	 * @param identifierType contains the type of identifier being used to do the query.
 	 * @param identifier contains the identifier.
 	 * @return will either return the Central Person Registry's person identifier corresponding to the identifier or a -1 to indicate an error.
 	 * @throws CprException exception indicates a cpr specific java exception.
 	 */
 	public long getPersonIdUsingIdentifier(IdentifierType identifierType, String identifier) throws CprException {
 		
 		long personId = NOT_FOUND_VALUE;
 		
 		if (identifier == null) {
 			throw new CprException(ReturnType.NOT_SPECIFIED_EXCEPTION, "Identifier");
 		}
 		
 		String typeName = identifierType.getTypeName();
 		if (typeName.equals(PERSON_ID_IDENTIFIER)) {
 			personId = Long.parseLong(identifier.trim());
 			personId = getPersonIdUsingPersonId(personId);
 		}
 		
 		else if (typeName.equals(PSU_ID_IDENTIFIER)) {
 			personId = getPersonIdUsingPsuId(identifier.trim());
 		}
 		
 		else if (typeName.equals(USERID_IDENTIFIER)) {
 			personId = getPersonIdUsingUserid(identifier.trim());
 		}
 		
 		else if (typeName.equals(ID_CARD_IDENTIFIER)) {
 			personId = getPersonIdUsingIdCard(identifier.trim());
 		}
 		
 		else {
 			personId = getPersonIdUsingPersonIdentifier(identifierType, identifier);
 		}
 		
 		return personId;
 	}
 	
 
 	/**
 	 * This routine will called a stored function to determine if a person is active in the CPR or not.
 	 * @param personId person identifier from the Central Person Registry.
 	 * @return true if the person is active.
 	 * @throws CprException exception indicates a cpr specific java exception.
 	 */
 	public boolean isPersonActive(long personId) throws CprException {
 		
 		long personIdOut = NOT_FOUND_VALUE;
 
 		final String sqlQuery = "SELECT person_id FROM person WHERE person_id = :personid AND end_date IS NULL";
 		final SQLQuery query = session.createSQLQuery(sqlQuery);
 		query.setParameter("personid", personId);
 		query.addScalar("person_id", StandardBasicTypes.LONG);
 		final Iterator<?> it = query.list().iterator();
 		if (it.hasNext()) {
 			personIdOut = (Long) it.next();
 		}
 		
 		if (personId != personIdOut) {
 			throw new CprException(ReturnType.PERSON_NOT_ACTIVE_EXCEPTION);			
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * doesPsuIdExist accepts a single parameter the psuId and checks to see if it exists in the CPR.
 	 * @param psuId contains the psu id to check for existence.
 	 * @throws CprException exception indicates a cpr specific java exception.
 	 */
 	public void doesPsuIdExist(String psuId) throws CprException {
 
 		Long personId = NOT_FOUND_VALUE;
 
 		final String sqlQuery = "SELECT person_id FROM psu_id WHERE psu_id = :psuid AND end_date IS NULL";
 		final SQLQuery query = session.createSQLQuery(sqlQuery);
 		query.setParameter("psuid", psuId);
 		query.addScalar("person_id", StandardBasicTypes.LONG);
 		final Iterator<?> it = query.list().iterator();
 		if (it.hasNext()) {
 			personId = (Long) it.next();
 		}
 
 		if (personId.equals(NOT_FOUND_VALUE)) {
 			throw new CprException(ReturnType.PSUID_NOT_FOUND_EXCEPTION);			
 		}
 	}
 	
 	/**
 	 * This routine will verify whether a userid exists in the CPR or not.
 	 * @param userid input userid to be checked for existence.
 	 * @throws CprException exception indicates a cpr specific java exception.
 	 */
 	public void doesUseridExist(String userid) throws CprException {
 		
 		Long personId = NOT_FOUND_VALUE;
 
 		final String sqlQuery = "SELECT person_id FROM userid WHERE userid = :userid";
 		final SQLQuery query = session.createSQLQuery(sqlQuery);
 		query.setParameter("userid", userid);
 		query.addScalar("person_id", StandardBasicTypes.LONG);
 		final Iterator<?> it = query.list().iterator();
 		if (it.hasNext()) {
 			personId = (Long) it.next();
 		}
 
 		if (personId.equals(NOT_FOUND_VALUE)) {
 			throw new CprException(ReturnType.RECORD_NOT_FOUND_EXCEPTION, "userid");
 		}
 	}
 	
 	/**
 	 * For a particular identifier type and identifier, this routine will attempt to retrieve the correct person id from the registry.
 	 * @param idType contains the string representation of the identifier type.
 	 * @param identifier contains the identifier to be searched for.
 	 * @return contains the person identifier if found.
 	 * @throws CprException 
 	 */
 	public long getPersonId(String idType, String identifier) throws CprException {
 		
 		long personId = NOT_FOUND_VALUE;
 
 		LOG4J_LOGGER.info("Start of get Person");
 		if (idType == null) {
 			throw new CprException(ReturnType.NOT_SPECIFIED_EXCEPTION, "Identifier type");
 		}
 		
 		if (identifier == null) {
 			throw new CprException(ReturnType.NOT_SPECIFIED_EXCEPTION, "Identifier");
 		}
 			
 		// Validate the identifierType.
 		final IdentifierType localIdentifierType = isValidIdentifierType(idType);
 		if (localIdentifierType == null) {
 			throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, "Identifier type");
 		}
 		
 		// Save off the identifier type.
 		setIdentifierType(localIdentifierType);
 		
 		// Validate the length of the identifier type.
 		isIdentifierLengthValid(localIdentifierType.getTypeName(), identifier);
 		
 		personId = getPersonIdUsingIdentifier(localIdentifierType, identifier);
 			
 		LOG4J_LOGGER.info("End of get Person");
 		return personId;
 	}
 	
 	/**
 	 * This is a private routine that is used to obtain the tableColumns hashmap
 	 * @return will return the hashmap.
 	 */
 	public  HashMap<String, TableColumn> getTableColumns() {
 		return tableColumns;
 	}
 	
 	/**
 	 * This routine is used to obtain all of the database columns metadata information for a particular table within the CPR.
 	 * @param tableName contains the table name that is useds to obtain data for.
 	 */
 	public void getAllTableColumns(final String tableName) {
 		
 		session.doWork(new Work() {
 			public void execute(Connection conn) throws SQLException {
 				ResultSet rs = null;
 				try {
 					rs = conn.getMetaData().getColumns(null, 
 							SessionFactoryUtil.getDefaultSchema(),
 							tableName.toLowerCase(), "_%");
 					tableColumns = new HashMap<String, TableColumn>();
 					while (rs.next()) {
 						tableColumns.put(rs.getString("column_name").toUpperCase(), 
 								new TableColumn(rs.getString("column_name").toUpperCase(), 
 										rs.getShort("data_type"), rs.getInt("column_size"), 
 										rs.getInt("decimal_digits"), rs.getInt("nullable")));		
 					}
 				}
 				finally {
 					try {
 						rs.close();
 					}
 					catch (Exception e) {
 					}
 				}
 			}
 		});
 	}
 	
 	/**
 	 * This routine is used to obtain column data for a particular column.
 	 * @param columnName contains the column name to obtain data for.
 	 * @return will return a TableColumn object if successful.
 	 * @throws CprException exception indicates a cpr specific java exception.
 	 */
 	public  TableColumn getColumn(String columnName) throws CprException {
 		
 		if (columnName == null || columnName.length() == 0) {
 			throw new CprException(ReturnType.NOT_SPECIFIED_EXCEPTION, "Column name");
 		}
 
 		final TableColumn tableColumn = getTableColumns().get(columnName.toUpperCase());
 		if (tableColumn == null) {
 			throw new CprException(ReturnType.GENERAL_DATABASE_EXCEPTION, "Unable to find database column in metadata.");
 		}
 		return tableColumn;
 	}
 	
 	/**
 	 * This routine is used to determine if a database field already equals another field.
 	 * @param s1 contains the first field to test.
 	 * @param s2 contains the second field to test.
 	 * @return will return true if the field are equal, otherwise it will return false.
 	 */
 	public boolean areStringFieldsEqual(String s1, String s2) {
 		
 		try {
 			final String tempString1 = (s1 == null || s1.trim().length() == 0) ? "NULL" : s1.trim();
 			final String tempString2 = (s2 == null || s2.trim().length() == 0) ? "NULL" : s2.trim();
 
 			return tempString1.equals(tempString2);
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 	
 	/**
 	 * This routine is used to determine if a database field already equals another field.
 	 * @param parm1 contains the first field to test.
 	 * @param parm2 contains the second field to test.
 	 * @return will return true if the field are equal, otherwise it will return false.
 	 */
 	public boolean areLongFieldsEqual(Long parm1, Long parm2) {
 		
 		try {
 			if (parm1 == null && parm2 == null) {
 				return true;
 			}
 			else if ((parm1 == null && parm2 != null) ||
 					 (parm1 != null && parm2 == null)) {
 				return false;
 			}
 			else if (parm1 != null && parm2 == null) {
 				return false;
 			}
 			else {
 				return parm1.equals(parm2);
 			}
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	/**
 	 * @param identifierType the identifierType to set
 	 */
 	public void setIdentifierType(IdentifierType identifierType) {
 		this.identifierType = identifierType;
 	}
 
 	/**
 	 * @return the identifierType
 	 */
 	public IdentifierType getIdentifierType() {
 		return identifierType;
 	}
 
 	/**
 	 * @param cprAccessGroupsKey the cprAccessGroupsKey to set
 	 */
 	public void setCprAccessGroupsKey(long cprAccessGroupsKey) {
 		this.cprAccessGroupsKey = cprAccessGroupsKey;
 	}
 
 	/**
 	 * @return the cprAccessGroupsKey
 	 */
 	public long getCprAccessGroupsKey() {
 		return cprAccessGroupsKey;
 	}
 
 	/**
 	 * @param registrationAuthorityKey the registrationAuthorityKey to set
 	 */
 	public void setRegistrationAuthorityKey(long registrationAuthorityKey) {
 		this.registrationAuthorityKey = registrationAuthorityKey;
 	}
 
 	/**
 	 * @return the registrationAuthorityKey
 	 */
 	public long getRegistrationAuthorityKey() {
 		return registrationAuthorityKey;
 	}
     
     /**
      * This routine is used to validate a string an attempt to establish an enumerated type.
      * @param identifierType contains a string representing an indentifier type to be converted.
      * @return returns either a valid IdentifierType or a null.
      */
     public IdentifierType isValidIdentifierType(String identifierType) {
     	
 		return (IdentifierType) DBTypes.INSTANCE.getTypeMaps(DBTypes.IDENTIFIER_TYPE).get(identifierType.toUpperCase().trim());
     }
     
     /**
      * The purpose of this routine is to validate whether an identifier's value is less than the maximum database field length.
      * @param typeName contains the type's name.
      * @param identifier contains the value of the identifier.
      * @return will return true if the identifier is less than the maximum length.
      * @throws CprExecption will be thrown if there are any CPR specific problems.
      */
     public boolean isIdentifierLengthValid(String typeName, String identifier) 
     throws CprException {
 
     	if (typeName.equals(Database.ID_CARD_IDENTIFIER)) {
     		doIdentifierLengthCheck(identifier, "Id card number", "PERSON_ID_CARD", "ID_CARD_NUMBER");
     	}
     	else if (typeName.equals(Database.PERSON_ID_IDENTIFIER)) {
     		if (identifier.length() == 0) {
     			throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, "Person identifier");
     		}
     	}
     	else if (typeName.equals(Database.PSU_ID_IDENTIFIER)) {
     		doIdentifierLengthCheck(identifier, "PSU Id Number", PSU_ID_IDENTIFIER, PSU_ID_IDENTIFIER);
     	}
     	else if (typeName.equals(Database.SSN_IDENTIFIER)) {
     		doIdentifierLengthCheck(identifier, "Social Security Number", PSU_ID_IDENTIFIER, PSU_ID_IDENTIFIER);
     	}
     	else if (typeName.equals(Database.USERID_IDENTIFIER)) {
     		doIdentifierLengthCheck(identifier, "Userid", USERID_IDENTIFIER, USERID_IDENTIFIER);
     	}
     	else {
     		doIdentifierLengthCheck(identifier, typeName, "PERSON_IDENTIFIER", "IDENTIFIER_VALUE");
     	}
     	return true;
     }
 
 	/**
 	 * This routine is used to perform the bulk of the identifier length check.
 	 * @param identifier contains the value of the identifier.
 	 * @param identifierName contains the english name of the identifier (for error message purposes).
 	 * @param tableName contains the name of the database table.
 	 * @param columnName contains the name of the database column to validate against.
 	 * @throws CprException will be thrown if there are any CPR specific problems.
 	 * @throws InvalidParametersException 
 	 * @throws NotSpecifiedException 
 	 */
     public void doIdentifierLengthCheck(String identifier, 
     		String identifierName, String tableName, String columnName) throws CprException {
 
     	if (identifier.length() == 0) {
     		throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, identifierName);
     	}
 
     	getAllTableColumns(tableName);
     	if (identifier.length() > getColumn(columnName).getColumnSize()) {
     		throw new CprException(ReturnType.PARAMETER_LENGTH_EXCEPTION, identifierName);
     	}
     }
     
 }
