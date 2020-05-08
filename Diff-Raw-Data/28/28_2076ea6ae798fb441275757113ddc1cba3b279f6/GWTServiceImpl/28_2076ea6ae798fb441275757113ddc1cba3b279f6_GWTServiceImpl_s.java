 package org.riviti.org.EnterpriseTest.GWT.server;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import org.riviti.EnterpriseTest.Common.OrchestraRegistration;
 import org.riviti.EnterpriseTest.Common.OrchestraRegistrationHandler;
 import org.riviti.org.EnterpriseTest.GWT.client.GWTService;
 import org.riviti.org.EnterpriseTest.GWT.client.OrchestraRegistrationDTO;
 
 import javax.ejb.EJB;
 import java.util.ArrayList;
 import java.util.List;
 
 public class GWTServiceImpl extends RemoteServiceServlet implements GWTService {
 
     private static final long serialVersionUID = -4154185365272929994L;
    private OrchestraRegistrationHandler orchestraRegistrationHandler;
 
     @EJB
     public void setOrchestraRegistrationHandler(OrchestraRegistrationHandler orchestraRegistrationHandler) {
         this.orchestraRegistrationHandler = orchestraRegistrationHandler;
     }
 
     @Override
     public List<OrchestraRegistrationDTO> getMessages() {
         List<OrchestraRegistration> orchestraRegistrations = orchestraRegistrationHandler.getMessages();
         List<OrchestraRegistrationDTO> retval = new ArrayList<OrchestraRegistrationDTO>();
         for (OrchestraRegistration orchestraRegistration : orchestraRegistrations) {
             OrchestraRegistrationDTO dto = new OrchestraRegistrationDTO();
             dto.setId(orchestraRegistration.getId());
             dto.setFullName(orchestraRegistration.getFullName());
             dto.setShortName(orchestraRegistration.getShortName());
             dto.setShortNameUsageAllowed(orchestraRegistration.isShortNameUsageAllowed());
             dto.setBestOrchestraMemory(orchestraRegistration.getBestOrchestraMemory());
             dto.setConcertRitualDescription(orchestraRegistration.getConcertRitualDescription());
             dto.setLookingForwardToDescription(orchestraRegistration.getLookingForwardToDescription());
             dto.setSofVisitationCount(orchestraRegistration.getSofVisitationCount());
             dto.setThreeDescriptiveWords(orchestraRegistration.getThreeDescriptiveWords());
             dto.setFavouriteMusicDescription(orchestraRegistration.getFavouriteMusicDescription());
             retval.add(dto);
         }
         return retval;
     }
 
     @Override
     public void saveRegistration(OrchestraRegistrationDTO dto) {
         OrchestraRegistration registration = new OrchestraRegistration();
         registration.setId(dto.getId());
         registration.setFullName(dto.getFullName());
         registration.setShortName(dto.getShortName());
         registration.setShortNameUsageAllowed(dto.isShortNameUsageAllowed());
         registration.setBestOrchestraMemory(dto.getBestOrchestraMemory());
         registration.setConcertRitualDescription(dto.getConcertRitualDescription());
         registration.setLookingForwardToDescription(dto.getLookingForwardToDescription());
         registration.setSofVisitationCount(dto.getSofVisitationCount());
         registration.setThreeDescriptiveWords(dto.getThreeDescriptiveWords());
         registration.setFavouriteMusicDescription(dto.getFavouriteMusicDescription());
 
         orchestraRegistrationHandler.createRegistration(registration);
     }
 }
