 package gov.nih.nci.cabig.labviewer.grid;
 
 import gov.nih.nci.ccts.grid.HealthcareSiteType;
 import gov.nih.nci.ccts.grid.IdentifierType;
 import gov.nih.nci.ccts.grid.ParticipantType;
 import gov.nih.nci.ccts.grid.Registration;
 import gov.nih.nci.ccts.grid.StudyRefType;
 import gov.nih.nci.ccts.grid.StudySiteType;
 import gov.nih.nci.ccts.grid.common.RegistrationConsumer;
 import gov.nih.nci.ccts.grid.stubs.types.InvalidRegistrationException;
 import gov.nih.nci.ccts.grid.stubs.types.RegistrationConsumptionException;
 import gov.nih.nci.ctom.ctlab.domain.HealthCareSite;
 import gov.nih.nci.ctom.ctlab.domain.Identifier;
 import gov.nih.nci.ctom.ctlab.domain.Participant;
 import gov.nih.nci.ctom.ctlab.domain.Protocol;
 import gov.nih.nci.ctom.ctlab.domain.StudyParticipantAssignment;
 import gov.nih.nci.ctom.ctlab.persistence.CTLabDAO;
 
 import java.rmi.RemoteException;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class LabViewerRegistrationConsumer implements RegistrationConsumer
 {
 	private static final Log logger = LogFactory.getLog(LabViewerRegistrationConsumer.class);
 	
 	public void commit(Registration registration) throws RemoteException, InvalidRegistrationException
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void rollback(Registration registration) throws RemoteException, InvalidRegistrationException
 	{
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public Registration register(Registration registration)
 		throws RemoteException, InvalidRegistrationException, RegistrationConsumptionException
 	{
 		logger.info("Lab Viewer Registration message received");
		System.out.println("Lab registration received");
 		StudyRefType studyRef = registration.getStudyRef();
 	
 		// save the study data
 		Protocol protocol = new Protocol();
 		protocol.setLongTxtTitle(studyRef.getLongTitleText());
 		protocol.setShortTxtTitle(studyRef.getShortTitleText());
 	   	IdentifierType identifiers[] = studyRef.getIdentifier();
         // save the identifier data	
 		for(IdentifierType ident: identifiers)
 		{
 			if (ident.getPrimaryIndicator())
 			{
 				Identifier id = new Identifier();
 				id.setExtension(ident.getValue());
 				id.setSource(ident.getSource());
 				id.setRoot(studyRef.getGridId());
 				protocol.setIdentifier(id);
 				protocol.setNciIdentifier(id.getRoot() + "." + id.getExtension());
 			}
 		}
 		
 		// save the study site data
 		StudySiteType studySite = registration.getStudySite();
 		HealthcareSiteType hcsType = studySite.getHealthcareSite(0);
 		HealthCareSite healthCare = new HealthCareSite();
 		String tmpstr = studySite.getGridId()+"."+hcsType.getNciInstituteCode();
 		healthCare.setNciInstituteCd(tmpstr);
 		
 		//save participant data
 		ParticipantType participant = registration.getParticipant();
 		Participant part = new Participant();
 		part.setFirstName(participant.getFirstName());
 		part.setLastName(participant.getLastName());
 		char[] initials= new char[2];
 		initials[0]=participant.getFirstName().charAt(0);
 		initials[1]=participant.getLastName().charAt(0);
 		part.setInitials(new String(initials));
 		part.setAdminGenderCode(participant.getAdministrativeGenderCode());
 		part.setBirthDate(participant.getBirthDate());
 		part.setEthnicGroupCode(participant.getEthnicGroupCode());
 		part.setRaceCode(participant.getRaceCode());
 		 
 		// Assume that only one identifier was sent and use that
 		IdentifierType id = participant.getIdentifier(0);
 		if (id != null)
 		{
 			Identifier ident = new Identifier();
 			ident.setExtension(id.getValue());
 			ident.setSource(id.getSource());
 			ident.setRoot(participant.getGridId());
 			part.setIdentifier(ident);
 		}
 			
 		else
 		{
			throw new InvalidRegistrationException();
 		}
 		
 		StudyParticipantAssignment studyPartAssig = new StudyParticipantAssignment();
 		studyPartAssig.setParticipant(part);
		String tmp =participant.getGridId()+"."+id.getValue();
		System.out.println(tmp);
 		studyPartAssig.setStudyPartIdOrig(tmp);
 		healthCare.setStudyParticipantAssignment(studyPartAssig);
 		protocol.setHealthCareSite(healthCare);
 		
		System.out.println("Data populated");
 		
 		// Now create the DAO and save
 		CTLabDAO dao = new CTLabDAO();
 		Connection con = dao.getConnection();
 		
 		try
 		{
 			dao.saveProtocol(con, protocol);
 		}
 		catch (SQLException e)
 		{
 			logger.error("Error saving participant", e);
			throw new RegistrationConsumptionException();
 		}
 		catch (Exception e)
 		{
 			logger.error("Error saving participant", e);
			throw new RegistrationConsumptionException();
 		}
 		
 		return registration;
 	}
 	
 }
