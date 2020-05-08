 package org.riviti.EnterpriseTest.GWT.server;
 
 import org.mockito.ArgumentCaptor;
 import org.riviti.EnterpriseTest.Common.OrchestraRegistration;
 import org.riviti.EnterpriseTest.Common.OrchestraRegistrationHandler;
 import org.riviti.EnterpriseTest.GWT.client.OrchestraRegistrationDTO;
import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.Collections;
 import java.util.List;
 
 import static org.mockito.Mockito.*;
 import static org.testng.Assert.assertEquals;
 
 public class GWTServiceImplTest {
 
     private GWTServiceImpl service;
 
     private OrchestraRegistrationHandler registrationHandlerMock;
 
     @BeforeMethod
     public void setUp() throws Exception {
         service = new GWTServiceImpl();
         registrationHandlerMock = mock(OrchestraRegistrationHandler.class);
         service.setOrchestraRegistrationHandler(registrationHandlerMock);
     }
 
    @AfterMethod
    public void tearDown() throws Exception {

    }

     @Test
     public void getMessagesReturnsCorrectData() {
         OrchestraRegistration orchestraRegistration = new OrchestraRegistration();
         orchestraRegistration.setId(2);
         orchestraRegistration.setFullName("FN");
         orchestraRegistration.setShortName("SN");
         orchestraRegistration.setShortNameUsageAllowed(true);
         orchestraRegistration.setBestOrchestraMemory("BOM");
         orchestraRegistration.setConcertRitualDescription("CRD");
         orchestraRegistration.setLookingForwardToDescription("LFT");
         orchestraRegistration.setSofVisitationCount(5);
         orchestraRegistration.setThreeDescriptiveWords("abc");
         orchestraRegistration.setFavouriteMusicDescription("FM");
 
         when(registrationHandlerMock.getRegistrations()).thenReturn(Collections.singletonList(orchestraRegistration));
 
         List<OrchestraRegistrationDTO> registrations = service.getRegistrations();
         assertEquals(registrations.size(), 1, "Incorrect number of elements in result from getRegistrations().");
         OrchestraRegistrationDTO dto = registrations.get(0);
         assertEquals(dto.getId(), orchestraRegistration.getId(), "Incorrect id in the dto.");
         assertEquals(dto.getFullName(), orchestraRegistration.getFullName(), "Incorrect full name in the dto.");
         assertEquals(dto.getShortName(), orchestraRegistration.getShortName(), "Incorrect short name in the dto.");
         assertEquals(dto.isShortNameUsageAllowed(), orchestraRegistration.isShortNameUsageAllowed(),
                 "Incorrect value on the short name usage flag in the dto.");
         assertEquals(dto.getBestOrchestraMemory(), orchestraRegistration.getBestOrchestraMemory(),
                 "Incorrect best memory text in the dto.");
         assertEquals(dto.getConcertRitualDescription(), orchestraRegistration.getConcertRitualDescription(),
                 "Incorrect ritual description in the dto.");
         assertEquals(dto.getLookingForwardToDescription(), orchestraRegistration.getLookingForwardToDescription(),
                 "Incorrect \"Looking forward to\" description in the dto.");
         assertEquals(dto.getSofVisitationCount(), orchestraRegistration.getSofVisitationCount(),
                 "Incorrect visitation count in the dto.");
         assertEquals(dto.getThreeDescriptiveWords(), orchestraRegistration.getThreeDescriptiveWords(),
                 "Incorrect three descriptive words in the dto.");
         assertEquals(dto.getFavouriteMusicDescription(), orchestraRegistration.getFavouriteMusicDescription(),
                 "Incorrect favourite music in the dto.");
     }
 
     @Test
     public void saveRegistrationSavesCorrectData() {
         OrchestraRegistrationDTO dto = new OrchestraRegistrationDTO();
         dto.setId(2);
         dto.setFullName("FN");
         dto.setShortName("SN");
         dto.setShortNameUsageAllowed(true);
         dto.setBestOrchestraMemory("BOM");
         dto.setConcertRitualDescription("CRD");
         dto.setLookingForwardToDescription("LFT");
         dto.setSofVisitationCount(5);
         dto.setThreeDescriptiveWords("abc");
         dto.setFavouriteMusicDescription("FM");
 
         service.saveRegistration(dto);
 
         ArgumentCaptor<OrchestraRegistration> argumentCaptor = ArgumentCaptor.forClass(OrchestraRegistration.class);
         verify(registrationHandlerMock).createRegistration(argumentCaptor.capture());
 
         OrchestraRegistration registration = argumentCaptor.getValue();
 
         assertEquals(registration.getId(), dto.getId(), "Incorrect id in the registration.");
         assertEquals(registration.getFullName(), dto.getFullName(), "Incorrect full name in the registration.");
         assertEquals(registration.getShortName(), dto.getShortName(), "Incorrect short name in the registration.");
         assertEquals(registration.isShortNameUsageAllowed(), dto.isShortNameUsageAllowed(),
                 "Incorrect value on the short name usage flag in the registration.");
         assertEquals(registration.getBestOrchestraMemory(), dto.getBestOrchestraMemory(),
                 "Incorrect best memory text in the registration.");
         assertEquals(registration.getConcertRitualDescription(), dto.getConcertRitualDescription(),
                 "Incorrect ritual description in the registration.");
         assertEquals(registration.getLookingForwardToDescription(), dto.getLookingForwardToDescription(),
                 "Incorrect \"Looking forward to\" description in the registration.");
         assertEquals(registration.getSofVisitationCount(), dto.getSofVisitationCount(),
                 "Incorrect visitation count in the registration.");
         assertEquals(registration.getThreeDescriptiveWords(), dto.getThreeDescriptiveWords(),
                 "Incorrect three descriptive words in the registration.");
         assertEquals(registration.getFavouriteMusicDescription(), dto.getFavouriteMusicDescription(),
                 "Incorrect favourite music in the registration.");
     }
 }
