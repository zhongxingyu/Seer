 package gov.nih.nci.cabig.labviewer.grid;
 
 import gov.nih.nci.cabig.ccts.domain.HealthcareSiteType;
 import gov.nih.nci.cabig.ccts.domain.IdentifierType;
 import gov.nih.nci.cabig.ccts.domain.InvestigatorType;
 import gov.nih.nci.cabig.ccts.domain.OrganizationAssignedIdentifierType;
 import gov.nih.nci.cabig.ccts.domain.Study;
 import gov.nih.nci.cabig.ccts.domain.StudyInvestigatorType;
 import gov.nih.nci.caxchange.ctom.viewer.util.LabViewerAuthorizationHelper;
 import gov.nih.nci.ccts.grid.studyconsumer.common.StudyConsumerI;
 import gov.nih.nci.ccts.grid.studyconsumer.service.globus.StudyConsumerAuthorization;
 import gov.nih.nci.ccts.grid.studyconsumer.stubs.types.InvalidStudyException;
 import gov.nih.nci.ccts.grid.studyconsumer.stubs.types.StudyCreationException;
 import gov.nih.nci.ctom.ctlab.domain.HealthCareSite;
 import gov.nih.nci.ctom.ctlab.domain.Identifier;
 import gov.nih.nci.ctom.ctlab.domain.Investigator;
 import gov.nih.nci.ctom.ctlab.domain.Protocol;
 import gov.nih.nci.ctom.ctlab.domain.ProtocolStatus;
 import gov.nih.nci.ctom.ctlab.handler.ProtocolHandler;
 
 import java.rmi.RemoteException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Date;
 
 import javax.xml.namespace.QName;
 
 import org.apache.log4j.Logger;
 import org.oasis.wsrf.properties.GetMultipleResourcePropertiesResponse;
 import org.oasis.wsrf.properties.GetMultipleResourceProperties_Element;
 import org.oasis.wsrf.properties.GetResourcePropertyResponse;
 import org.oasis.wsrf.properties.QueryResourcePropertiesResponse;
 import org.oasis.wsrf.properties.QueryResourceProperties_Element;
 
 public class LabViewerStudyConsumer implements StudyConsumerI
 {
 	Logger logger = Logger.getLogger(getClass());
 	static final int MILLIS_PER_MINUTE = 60 * 1000;
 	static final int THRESHOLD_MINUTE = 1;
 	private ProtocolHandler dao = new ProtocolHandler();
 	private Connection con;
 
 	public void commit(Study study) throws RemoteException,
 			InvalidStudyException
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	public GetMultipleResourcePropertiesResponse getMultipleResourceProperties(
 			GetMultipleResourceProperties_Element params)
 			throws RemoteException
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public GetResourcePropertyResponse getResourceProperty(QName params)
 			throws RemoteException
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public QueryResourcePropertiesResponse queryResourceProperties(
 			QueryResourceProperties_Element params) throws RemoteException
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void createStudy(Study study) throws RemoteException,
 			InvalidStudyException, StudyCreationException
 	{
 		logger.info("Create Study message received");
 
 		// Authorization code
 		String username = StudyConsumerAuthorization.getCallerIdentity();
 
 		if (!authorized(username))
 		{
 			logger.error("Error saving Study");
 			StudyCreationException rce = new StudyCreationException();
 			rce.setFaultString("User " + username
 					+ " not authorized for this operation");
 			throw rce;
 		}
 		else
 		{
 
 			// save the study data
 			logger.info("payload has Study information");
 			Protocol protocol = new Protocol();
 			// populate the Protocol object with Study message data
 			populateProtocol(protocol, study);
 			// obtain connection
 			con = dao.getConnection();
 			logger.info("Create Study message validated");
 			try
 			{
 				// save Protocol
 				dao.persist(con, protocol);
 				logger.info("Persisted the study");
 			}
 			catch (SQLException e)
 			{
 				logger.error("Error creating study", e);
 				StudyCreationException rce = new StudyCreationException();
 				rce.setFaultString(e.getMessage());
 				throw rce;
 			}
 			catch (Exception e)
 			{
 				logger.error("Error creating study", e);
 				StudyCreationException rce = new StudyCreationException();
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
 					logger.error("Error closing connection", e);
 				}
 			}
 			logger.info("Study created");
 		}// end of else
 	}
 
 	/**
 	 * @param protocol
 	 * @param study
 	 */
 	private void populateProtocol(Protocol protocol, Study study)
 	{
		try
		{
 		java.util.Date now = new Date();
 		protocol.setLongTxtTitle(study.getLongTitleText());
 		protocol.setShortTxtTitle(study.getShortTitleText());
 		protocol.setPhaseCode(study.getPhaseCode());
 		if (study.getCoordinatingCenterStudyStatus() != null)
 		{
 			ProtocolStatus protocolStatus = new ProtocolStatus();
 			protocolStatus.setCtom_insert_date(now);
 			protocolStatus.setStatus_code(study
 					.getCoordinatingCenterStudyStatus().getValue());
 			protocol.setStatus(protocolStatus);
 		}
 		else
 		{
 			protocol.setStatus(null);
 		}
 		protocol.setCtomInsertDt(now);
 		IdentifierType identifiers[] = study.getIdentifier();
 
 		// save the identifier data
 		// IdentifierType ident: identifiers
 		String assignedBy = "";
 		for (int i = 0; i < identifiers.length; i++)
 		{
 			IdentifierType ident = identifiers[i];
			if (ident.getPrimaryIndicator() != null && ident.getPrimaryIndicator().booleanValue())
 			{
 				Identifier id = new Identifier();
 				if (ident instanceof OrganizationAssignedIdentifierType)
 					assignedBy = "organization";
 				else
 					assignedBy = "system";
 				id.setExtension(ident.getValue());
 				id.setSource(ident.getSource());
 				id.setRoot(study.getGridId());
 				id.setAssigningAuthorityName(assignedBy);
 				protocol.setIdentifier(id);
 				protocol.setNciIdentifier(id.getRoot() + "."
 						+ id.getExtension());
 			}
 		}// end of for
 
 		// save the study site data
 		if (study.getStudyOrganization() != null)
 		{
 			//replace code: Perform a search by CTEP Identifier to retrieve the ii for the Organization
 			// and using this ii perform a COPPA service call to retrieve the Health care site details
 			Long ctepIdentifier=1L;
 			//retrieveOrgIi(ctepIdentifier);
 
 			if (study.getStudyOrganization(0).getHealthcareSite() != null)
 			{
 				logger.info("payload has HealthcareSite information");
 				HealthcareSiteType hcsType =
 						study.getStudyOrganization(0).getHealthcareSite(0);
 				HealthCareSite healthCare = new HealthCareSite();
 				healthCare.setNciInstituteCd(hcsType.getNciInstituteCode());
 				healthCare.setName(hcsType.getName());
 				healthCare.setCtomInsertDt(now);
 				protocol.setHealthCareSite(healthCare);
 			}
 			else
 			{
 				logger.info("payload has no HealthcareSite information");
 				protocol.setHealthCareSite(null);
 			}
 			if (study.getStudyOrganization(0).getStudyInvestigator() != null)
 			{
 				StudyInvestigatorType investigator =
 						study.getStudyOrganization(0).getStudyInvestigator(0);
 				Investigator studyInvestigator = new Investigator();
 				if (investigator.getHealthcareSiteInvestigator() != null)
 				{
 					if (investigator.getHealthcareSiteInvestigator()
 							.getInvestigator() != null)
 					{
 						// save the investigator data
 						logger.info("payload has Investigator information");
 						InvestigatorType healthCareSiteInvestigator = investigator.getHealthcareSiteInvestigator().getInvestigator(0);
 						studyInvestigator.setNciId(healthCareSiteInvestigator.getNciIdentifier());
 						studyInvestigator.setFirstName(healthCareSiteInvestigator.getFirstName());
 						studyInvestigator.setLastName(healthCareSiteInvestigator.getLastName());
 						studyInvestigator.setPhone(healthCareSiteInvestigator.getPhoneNumber());
 						protocol.setInvestigator(studyInvestigator);
 					}
 					else
 					{
 						logger.info("payload has no Investigator information");
 						protocol.setInvestigator(null);
 					}
 				}// end if
 			}// end if
 		}
		}
		catch (Exception e)
		{
			logger.error("populateProtocol: Exception occurred: ", e);
		}
 	}
 
 	/**
 	 * @param username
 	 * @return
 	 * @throws StudyCreationException
 	 */
 	private boolean authorized(String username) throws StudyCreationException
 	{
 		boolean userAuthorized = false;
 		String user = "";
 		if (username == null)
 		{
 			logger.error("Error saving Study no username provided");
 			StudyCreationException rce = new StudyCreationException();
 			rce.setFaultString("No user credentials provided");
 			throw rce;
 		}
 		else
 		{
 			logger.debug("User who called the service was " + username);
 			// instantiate Lab Viewer authorization
 			LabViewerAuthorizationHelper lvaHelper =
 					new LabViewerAuthorizationHelper();
 			if (username != null)
 			{
 				int beginIndex = username.lastIndexOf("=");
 				int endIndex = username.length();
 				user = username.substring(beginIndex + 1, endIndex);
 			}
 			// check if authorized
 			userAuthorized = lvaHelper.isAuthorized(user);
 		}
 		return userAuthorized;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see gov.nih.nci.ccts.grid.common.StudyConsumerI#rollback(gov.nih.nci.ccts.grid.Study)
 	 */
 	public void rollback(Study study) throws RemoteException,
 			InvalidStudyException
 	{
 		String studyGridId = study.getGridId();
 		logger.debug("Received a Study Rollback StudyGridId" + studyGridId);
 		// Obtain Connection
 		con = dao.getConnection();
 		try
 		{
 
 			java.util.Date insertdate =
 					dao.checkStudyForRollback(con, studyGridId);
 			if (insertdate != null)
 			{
 				java.util.Date currdate = new Date();
 				long milis1 = insertdate.getTime();
 				long milis2 = currdate.getTime();
 				long diffInMin = (milis2 - milis1) / MILLIS_PER_MINUTE;
 
 				if (insertdate.before(currdate) && diffInMin < THRESHOLD_MINUTE)
 				{
 					// Issue Study rollback
 					dao.rollbackStudy(con, studyGridId);
 				}
 				else
 				{
 					logger
 							.info("There is no study with in the threshold time for rollback");
 				}
 			}
 			else
 			{
 				// throw remote exception
 				StudyCreationException ire = new StudyCreationException();
 				ire
 						.setFaultString("Invalid study rollback message- no study found with given gridid");
 				logger.fatal(ire);
 				throw (ire);
 			}
 		}
 		catch (SQLException se)
 		{
 			logger.error("Error deleting study", se);
 		}
 		finally
 		{
 			try
 			{
 				con.close();
 			}
 			catch (SQLException e)
 			{
 				logger.error("Error closing connection", e);
 				String msg =
 						"Lab Viewer unable to rollback study" + e.getMessage();
 				throw new RemoteException(msg);
 			}
 		}
 		logger.info("deleted study");
 	}
 }
