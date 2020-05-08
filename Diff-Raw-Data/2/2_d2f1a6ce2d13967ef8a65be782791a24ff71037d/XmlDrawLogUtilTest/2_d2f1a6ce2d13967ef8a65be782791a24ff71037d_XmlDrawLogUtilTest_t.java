 package hsa.awp.scire.procedureLogic.util;
 
 import hsa.awp.campaign.model.Campaign;
 import hsa.awp.campaign.model.ConfirmedRegistration;
 import hsa.awp.campaign.model.DrawProcedure;
 import hsa.awp.campaign.model.PriorityList;
 import hsa.awp.event.facade.EventFacade;
 import hsa.awp.event.model.Event;
 import hsa.awp.event.model.EventBuilder;
 import hsa.awp.event.model.Subject;
 import hsa.awp.event.model.SubjectBuilder;
 import hsa.awp.user.model.SingleUser;
 import hsa.awp.user.model.Student;
 import org.hamcrest.Matchers;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import java.util.*;
 
 import static org.hamcrest.Matchers.any;
 import static org.hamcrest.Matchers.containsString;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.anyLong;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class XmlDrawLogUtilTest {
 
   XmlDrawLogUtil xmlDrawLogUtil;
 
   @Mock EventFacade eventFacade;
 
   @Before
   public void setUp() throws Exception {
     JAXBContext context = JAXBContext.newInstance(XmlDrawLog.class);
     xmlDrawLogUtil = new XmlDrawLogUtil();
     xmlDrawLogUtil.setContext(context);
     xmlDrawLogUtil.setEventFacade(eventFacade);
   }
 
   @Test
   public void testTransformMailContentsToXml() {
 
     SingleUser user = Student.getInstance("test-user", 123456);
     user.setName("Test User");
     user.setMail("test@physalix");
     Campaign campaign = Campaign.getInstance(3L);
     campaign.setName("testCampaign");
     DrawProcedure drawProcedure = DrawProcedure.getInstance(1337L);
     drawProcedure.setName("testDraw");
     campaign.addProcedure(drawProcedure);
     Subject subject = new SubjectBuilder().build();
     Event event = new EventBuilder().withSubject(subject).build();
 
     when(eventFacade.getEventById(anyLong())).thenReturn(event);
 
     List<MailContent> contents = new ArrayList<MailContent>();
     MailContent content = new MailContent(user);
     content.setDrawProcedure(drawProcedure);
     content.setPrioLists(createPriorityListsFromEvent(2, event));
     content.setRegistrations(createConfirmedRegistrationsFromEvent(2, event));
 
     contents.add(content);
     contents.add(content);
 
     String xml = xmlDrawLogUtil.transformMailContentsToXml(contents);
 
     assertThat(xml, containsString("testCampaign"));
     assertThat(xml, containsString("testDraw"));
     assertThat(xml, containsString("Test User"));
     assertThat(xml, containsString("test@physalix"));
     assertThat(xml, containsString("123456"));
     assertThat(xml, containsString("test-user"));
    assertThat(xml, containsString("<drawLog>"));
   }
 
   private List<ConfirmedRegistration> createConfirmedRegistrationsFromEvent(int amount, Event event) {
     List<ConfirmedRegistration> registrations = new ArrayList<ConfirmedRegistration>();
     for (int i = 0; i < amount; i++) {
       ConfirmedRegistration registration = ConfirmedRegistration.getInstance(event.getId(), 3L);
       registrations.add(registration);
     }
     return registrations;
   }
 
   private List<PriorityList> createPriorityListsFromEvent(int amount, Event event) {
     List<PriorityList> lists = new ArrayList<PriorityList>();
     for (int i = 0; i < amount; i++) {
       Long[] eventIds = new Long[amount];
       Arrays.fill(eventIds, event.getId());
       PriorityList priorityList = PriorityList.getInstance(1L, 2L, Arrays.asList(eventIds), 3L);
       lists.add(priorityList);
     }
     return lists;
   }
 }
