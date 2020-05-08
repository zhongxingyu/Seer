 package gov.nih.nci.cabig.labviewer.grid;
 
 import gov.nih.nci.cabig.ccts.domain.HealthcareSiteType;
 import gov.nih.nci.cabig.ccts.domain.IdentifierType;
 import gov.nih.nci.cabig.ccts.domain.OrganizationAssignedIdentifierType;
 import gov.nih.nci.cabig.ccts.domain.ParticipantType;
 import gov.nih.nci.cabig.ccts.domain.Registration;
 import gov.nih.nci.cabig.ccts.domain.StudyRefType;
 import gov.nih.nci.cabig.ccts.domain.StudySiteType;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteAuthorizationAccessException;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
 import gov.nih.nci.caxchange.ctom.viewer.util.LabViewerAuthorizationHelper;
 import gov.nih.nci.ccts.grid.common.RegistrationConsumerI;
 import gov.nih.nci.ccts.grid.service.globus.RegistrationConsumerAuthorization;
 import gov.nih.nci.ccts.grid.stubs.types.InvalidRegistrationException;
 import gov.nih.nci.ccts.grid.stubs.types.RegistrationConsumptionException;
 import gov.nih.nci.ctom.ctlab.domain.HealthCareSite;
 import gov.nih.nci.ctom.ctlab.domain.Identifier;
 import gov.nih.nci.ctom.ctlab.domain.Participant;
 import gov.nih.nci.ctom.ctlab.domain.Protocol;
 import gov.nih.nci.ctom.ctlab.domain.StudyParticipantAssignment;
 import gov.nih.nci.ctom.ctlab.handler.ProtocolHandler;
 
 import java.rmi.RemoteException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 /**
  * LabViewerRegistrationConsumer is the implementing class for this grid service
  * for Lab Viewer. It consumes the patient registration message that is sent
  * from the patient registry application via the hub.
  * <P>
  * 
  * @author Anupama Sharma
  */
 public class LabViewerRegistrationConsumer implements RegistrationConsumerI
 {
 	private static final int MILLIS_PER_MINUTE = 60 * 1000;
 	private static final int THRESHOLD_MINUTE = 1;
 	private static final Logger log =
 			Logger.getLogger(LabViewerRegistrationConsumer.class);
 	private ProtocolHandler dao = new ProtocolHandler();
 	private Connection con;
 	private LabViewerAuthorizationHelper authorizationHelper;
 
 	/**
 	 * commit is not currently implemented but is included in the grid service
 	 * to possibly provide a double commit transaction process.
 	 */
 	public void commit(Registration registration) throws RemoteException,
 			InvalidRegistrationException
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	public org.oasis.wsrf.properties.QueryResourcePropertiesResponse queryResourceProperties(
 			org.oasis.wsrf.properties.QueryResourceProperties_Element params)
 			throws RemoteException
 	{
 		return queryResourceProperties(params);
 	}
 
 	public org.oasis.wsrf.properties.GetMultipleResourcePropertiesResponse getMultipleResourceProperties(
 			org.oasis.wsrf.properties.GetMultipleResourceProperties_Element params)
 			throws RemoteException
 	{
 		return getMultipleResourceProperties(params);
 	}
 
 	public org.oasis.wsrf.properties.GetResourcePropertyResponse getResourceProperty(
 			javax.xml.namespace.QName params) throws RemoteException
 	{
 		return getResourceProperty(params);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.ccts.grid.common.RegistrationConsumer#rollback(gov.nih.nci.ccts.grid.Registration)
 	 */
 	public void rollback(Registration registration) throws RemoteException,
 			InvalidRegistrationException
 	{
 		log.debug("Received a rollback for participant");
 		ParticipantType participant = registration.getParticipant();
 		String participantGridId = participant.getGridId();
 		String participantExtension = participant.getIdentifier(0).getValue();
 		// obtain Connection
 		con = dao.getConnection();
 		// method returns the ctominsertdate if the participantGridId is found
 		// in the database
 		try
 		{
 			// check if Participant exists for rollback
 			java.util.Date insertdate =
 					dao.checkParticipantForRollback(con, participantGridId,
 							participantExtension);
 			if (insertdate != null)
 			{
 				java.util.Date currdate = new Date();
 				long milis1 = insertdate.getTime();
 				long milis2 = currdate.getTime();
 				long diffInMin = (milis2 - milis1) / MILLIS_PER_MINUTE;
 
 				if (insertdate.before(currdate) && diffInMin < THRESHOLD_MINUTE)
 				{
 					// issue Participant roll back
 					dao.rollbackParticipant(con, participantGridId,
 							participantExtension);
 					log.info("deleted participant");
 				}
 				else
 				{
 					log
 							.info("There is no participant within the threshold time for rollback");
 				}
 			}
 			else
 			{
 				InvalidRegistrationException ire =
 						new InvalidRegistrationException();
 				ire
 						.setFaultString("Lab Viewer invalid patient rollback message - no patient found with given gridid");
 				log.fatal(ire);
 				throw (ire);
 
 			}
 		}
 		catch (SQLException se)
 		{
 			log.error("Error deleting participant", se);
 			String msg =
 					"Lab Viewer unable to rollback participant"
 							+ se.getMessage();
 			throw new RemoteException(msg);
 		}
 		finally
 		{
 			try
 			{
 				con.close();
 			}
 			catch (SQLException e)
 			{
 				log.error("Error closing connection", e);
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.ccts.grid.common.RegistrationConsumer#register(gov.nih.nci.ccts.grid.Registration)
 	 */
 	public Registration register(Registration registration)
 			throws RemoteException, InvalidRegistrationException,
 			RegistrationConsumptionException
 	{
 		log.debug("Lab Viewer Registration message received");
 		checkAuthorization(RegistrationConsumerAuthorization.getCallerIdentity(), registration);
 		
 		// save the study data
 		Protocol protocol = new Protocol();
 		// populate the protocol and retrieve the MRN from the identifier
 		// type
 		String mrn = populateProtocol(protocol, registration);
 		// obtain database Connection
 		con = dao.getConnection();
 		log.info("Lab Viewer Registration message validated");
 
 		try
 		{
			// Perform the check to see if the patient exists - Demo case
			// where labs are loaded prior to registering a patient.
			Long spaid = dao.checkParticipantExists(con, protocol, mrn);
			if (spaid != null)
			{
			    // upgrade the grid id
			    log.debug(" updating the grid id = " + registration.getGridId() +" mrn = "+ mrn + " spa id = "+spaid);
			    dao.updateParticipantGridId(con, registration.getGridId(),spaid, mrn);
			}
			else
			{
			    log.debug(" Participation is not there so creating ....");
 				// Save Protocol.
 				dao.persist(con, protocol);
			}
 		}
 		catch (SQLException e)
 		{
 			log.error("Error saving participant", e);
 			RegistrationConsumptionException rce =
 					new RegistrationConsumptionException();
 			rce.setFaultString(e.getMessage());
 			throw rce;
 		}
 		catch (Exception e)
 		{
 			log.error("Error saving participant", e);
 			RegistrationConsumptionException rce =
 					new RegistrationConsumptionException();
 			rce.setFaultString(e.getMessage());
 			throw rce;
 		}
 		finally
 		{
 			try
 			{
 				con.close();
 			}
 			catch (SQLException e)
 			{
 				log.error("Error closing connection", e);
 			}
 		}
 		log.info("Lab Viewer Registration message stored");
 
 		return registration;
 	}
 	
 	/**
 	 * @param callerId
 	 * @throws RegistrationConsumptionException
 	 */
 	private void checkAuthorization(String callerId, Registration registration) throws RegistrationConsumptionException
 	{	
 		if (callerId == null)
 		{
 			log.error("Error saving participant: no user credentials provided");
 			throw createRegistrationConsumptionException("No user credentials provided");
 		}
 
 		log.debug("Service called by: " + callerId);		
 		int beginIndex = callerId.lastIndexOf("=") + 1;
 		int endIndex = callerId.length();
 		String username = callerId.substring(beginIndex, endIndex);
 		log.debug("Username = " + username);
 		
 		try
 		{
 		    Map<SuiteRole, SuiteRoleMembership> userRoleMemberships = getAuthorizationHelper().getUserRoleMemberships(username);
 		    SuiteRole registrationConsumerRole = SuiteRole.REGISTRAR;
 		    if (userRoleMemberships.containsKey(registrationConsumerRole))
 		    {
 		    	log.debug("User role memberships contains role: " + registrationConsumerRole.toString());
 		    	if (registrationConsumerRole.isScoped())
 		    	{
 		    		log.debug("Role is scoped: " + registrationConsumerRole.toString());
 		    		SuiteRoleMembership userRoleMembership = userRoleMemberships.get(registrationConsumerRole);
 
 		    		if (registrationConsumerRole.isStudyScoped())
 		    		{
 		    			log.debug("Role is study scoped: " + registrationConsumerRole.toString());
 		    			String studyId = getStudyId(registration);
 		    			if (studyId == null)
 		    			{
 		    				throw new SuiteAuthorizationAccessException("Role %s is study scoped - study identifier is null", registrationConsumerRole.getDisplayName());
 		    			}
 		    			log.debug("StudyId = " + studyId);
 		    			
 		    			// if the user has permission to access specific studies (not all studies), then verify the study
 		    			if (!userRoleMembership.isAllStudies() && !userRoleMembership.getStudyIdentifiers().contains(studyId))
 		    		    {
 		    				throw new SuiteAuthorizationAccessException("Username %s is not authorized for study %s", username, studyId);
 		    		    }
 		    			log.debug("User is authorized for study");
 		    		}
 
 		    		if (registrationConsumerRole.isSiteScoped())
 		    		{
 		    			log.debug("Role is site scoped: " + registrationConsumerRole.toString());
 		    			List<String> siteNciInstituteCodes = getSiteNciInstituteCodes(registration);
 		    			log.debug("Site NCI institute codes = " + siteNciInstituteCodes.toString());
 		    			if (siteNciInstituteCodes.isEmpty())
 		    			{
 		    				throw new SuiteAuthorizationAccessException("Role %s is site scoped - site NCI institute code is null", registrationConsumerRole.getDisplayName());
 		    			}
 		    			
 		    			// if the user has permission to access specific sites (not all sites), then verify the sites
 		    			if (!userRoleMembership.isAllSites())
 		    		    {
 		    				log.debug("User is NOT authorized for all sites");
 		    				boolean userAuthorizedForSite = false;
 		    				
 		    				for (String siteNciInstituteCode : siteNciInstituteCodes)
 		    				{
 		    					log.debug("Checking site NCI institute code: " + siteNciInstituteCode);
 		    					if (userRoleMembership.getSiteIdentifiers().contains(siteNciInstituteCode))
 		    					{
 		    						log.debug("User is authorized for site NCI institute code:" + siteNciInstituteCode);
 		    						userAuthorizedForSite = true;
 		    					}
 		    				}
 		    				
 		    				log.info("userAuthorizedForSite = " + userAuthorizedForSite);
 		    				if (!userAuthorizedForSite)
 	    					{
 		    					throw new SuiteAuthorizationAccessException("Username %s is not authorized for sites %s", username, siteNciInstituteCodes.toString());
 	    					}
 		    		    }
 		    		}
 		    	}
 		    }
 		    else
 		    {
 		    	throw new SuiteAuthorizationAccessException("Username %s is not authorized for role %s", username, registrationConsumerRole.getDisplayName());
 		    }
 		}
 		catch (SuiteAuthorizationAccessException e)
 		{
 			log.error("Error saving participant: ", e);
 			throw createRegistrationConsumptionException(e.getMessage());
 		}
 		catch (Exception e)
         {
             log.error("Error saving participant: ", e);
             throw createRegistrationConsumptionException(e.getMessage());
         }
 	}
 	
 	private RegistrationConsumptionException createRegistrationConsumptionException(String message)
 	{
 		RegistrationConsumptionException exception = new RegistrationConsumptionException();
 		exception.setFaultString(message);
 		return exception;
 	}
 	
 	private synchronized LabViewerAuthorizationHelper getAuthorizationHelper()
 	{
         if (authorizationHelper == null)
         {
             authorizationHelper = new LabViewerAuthorizationHelper();
         }
         
         return authorizationHelper;
     }
 	
 	private String getStudyId(Registration registration)
 	{
 		String studyId = null;
 		
 		IdentifierType identifiers[] = registration.getStudyRef().getIdentifier();
 		for (IdentifierType identifier : identifiers)
 		{
 			if (identifier.getPrimaryIndicator())
 			{
 				studyId = identifier.getValue();
 				break; // since match has been found
 			}
 		}
 		
 		return studyId;
 	}
 	
 	private List<String> getSiteNciInstituteCodes(Registration registration)
 	{
 		List<String> siteNciInstituteCodes = new ArrayList<String>();
 		
 		StudySiteType studySite = registration.getStudySite();
 		if (studySite != null)
 		{
 		    HealthcareSiteType healthCareSiteTypes[] = studySite.getHealthcareSite();
 		    if (healthCareSiteTypes != null)
 		    {
 		    	for (HealthcareSiteType healthCareSiteType : healthCareSiteTypes)
 		        {
 			        if (StringUtils.isNotBlank(healthCareSiteType.getNciInstituteCode()))
 			        {
 			        	if (!siteNciInstituteCodes.contains(healthCareSiteType.getNciInstituteCode()))
 			        	{
 			        	    siteNciInstituteCodes.add(healthCareSiteType.getNciInstituteCode());
 			        	}
 			        }
 		        }
 		    }
 		}
 		
 		return siteNciInstituteCodes;
 	}
 
 	/**
 	 * Populates the protocol object and returns the mrn from the identifierType 
 	 * @param protocol
 	 * @param registration
 	 * @return mrn 
 	 * @throws InvalidRegistrationException
 	 */
 	private String populateProtocol(Protocol protocol, Registration registration)
 			throws InvalidRegistrationException
 	{
 		java.util.Date now = new Date();
 		StudyRefType studyRef = registration.getStudyRef();
 		protocol.setLongTxtTitle(studyRef.getLongTitleText());
 		protocol.setShortTxtTitle(studyRef.getShortTitleText());
 		IdentifierType identifiers[] = studyRef.getIdentifier();
 
 		// save the identifier data
 		for (IdentifierType ident : identifiers)
 		{
 			if (ident.getPrimaryIndicator())
 			{
 				Identifier id = new Identifier();
 				id.setExtension(ident.getValue());
 				id.setSource(ident.getSource());
 				id.setRoot(studyRef.getGridId());
 				protocol.setIdentifier(id);
 				protocol.setNciIdentifier(id.getRoot() + "."
 						+ id.getExtension());
 			}
 		}
 
 		// save the study site data
 		StudySiteType studySite = registration.getStudySite();
 		HealthcareSiteType hcsType = studySite.getHealthcareSite(0);
 		HealthCareSite healthCare = new HealthCareSite();
 		healthCare.setNciInstituteCd(hcsType.getNciInstituteCode());
 		healthCare.setName(hcsType.getName());
 
 		// save participant data
 		ParticipantType participant = registration.getParticipant();
 		Participant part = new Participant();
 		part.setFirstName(participant.getFirstName());
 		part.setLastName(participant.getLastName());
 		char[] initials = new char[2];
 		initials[0] = participant.getFirstName().charAt(0);
 		initials[1] = participant.getLastName().charAt(0);
 		part.setInitials(new String(initials));
 		part.setAdminGenderCode(participant.getAdministrativeGenderCode());
 		part.setBirthDate(participant.getBirthDate());
 		part.setEthnicGroupCode(participant.getEthnicGroupCode());
 		part.setRaceCode(participant.getRaceCode());
 		part.setCtomInsertDate(now);
 		// Assume that only one identifier was sent and use that
 		IdentifierType[] ids = participant.getIdentifier();
 		IdentifierType id = null;
 		Identifier partIdent = new Identifier();
 		String type = "";
 		String assignedBy = "";
 		if (ids != null && ids.length > 0)
 		{
 			id = ids[0];
 			if (id instanceof OrganizationAssignedIdentifierType)
 				assignedBy = "organization";
 			else
 				assignedBy = "system";
 			type = id.getType();
 			partIdent.setExtension(id.getValue());
 			partIdent.setSource(id.getSource());
 			partIdent.setRoot(participant.getGridId());
 			partIdent.setAssigningAuthorityName(assignedBy);
 			part.setIdentifier(partIdent);
 
 		}
 		else
 		{
 			InvalidRegistrationException ire =
 					new InvalidRegistrationException();
 			ire
 					.setFaultString("Invalid patient registration message missing patient identifier");
 			throw ire;
 		}
 
 		StudyParticipantAssignment studyPartAssig =
 				new StudyParticipantAssignment();
 		studyPartAssig.setParticipant(part);
 		String tmp = participant.getGridId() + "." + id.getValue();
 		String mrn = id.getValue();
 		studyPartAssig.setStudyPartIdOrig(tmp);
 
 		// SPA identifier is same as participant but different grid id
 		Identifier regIdent = new Identifier();
 		regIdent.setSource(partIdent.getSource());
 		regIdent.setExtension(partIdent.getExtension());
 		regIdent.setRoot(registration.getGridId());
 		studyPartAssig.setIdentifier(regIdent);
 		studyPartAssig.setType(type);
 
 		healthCare.setStudyParticipantAssignment(studyPartAssig);
 		protocol.setHealthCareSite(healthCare);
 		protocol.setCtomInsertDt(now);
 
 		return mrn;
 	}
 
 }
