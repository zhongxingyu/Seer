 package org.motechproject.care.reporting.processors;
 
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.motechproject.care.reporting.builder.CaseEventBuilder;
 import org.motechproject.care.reporting.builder.ChildCaseBuilder;
 import org.motechproject.care.reporting.builder.FlwBuilder;
 import org.motechproject.care.reporting.builder.MotherCaseBuilder;
 import org.motechproject.care.reporting.domain.dimension.ChildCase;
 import org.motechproject.care.reporting.domain.dimension.Flw;
 import org.motechproject.care.reporting.domain.dimension.FlwGroup;
 import org.motechproject.care.reporting.domain.dimension.MotherCase;
 import org.motechproject.care.reporting.repository.SpringIntegrationTest;
 import org.motechproject.commcare.events.CaseEvent;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
 import static org.motechproject.care.reporting.utils.TestUtils.assertReflectionEqualsWithIgnore;
 
 public class CloseCaseProcessorIT extends SpringIntegrationTest {
     private final String caseId = "97e56523-5820-414a-83c2-bfcb6dcf4db3";
     private final String userId = "89fda0284e008d2e0c980fb13f989136";
     private final String serverModifiedOn = "2013-06-13";
 
     @Autowired
     private CloseCaseProcessor closeCaseProcessor;
 
     @Test
     public void shouldUpdateCloseCaseWhenMotherCaseAlreadyExists() {
         Flw oldFlw = new FlwBuilder()
                 .flwId("oldda0284e008d2e0c980fb13f989136")
                 .build();
         MotherCase mother = new MotherCaseBuilder()
                 .caseId(caseId)
                 .flw(oldFlw)
                 .build();
         template.save(mother);
         Flw flw = new FlwBuilder().flwId(userId).build();
         template.save(flw);
 
         CaseEvent closedCase = new CaseEventBuilder(caseId)
                 .withAction("CLOSE")
                 .withUserId(userId)
                 .withServerModifiedOn(serverModifiedOn)
                 .build();
 
         closeCaseProcessor.process(closedCase);
 
         List<MotherCase> motherCases = template.loadAll(MotherCase.class);
         assertEquals(1, motherCases.size());
 
         MotherCase actualMother = motherCases.get(0);
         assertEquals(Boolean.TRUE, actualMother.getClosed());
         assertEquals((new DateTime(2013, 6, 13, 0, 0, 0)).toDate(), actualMother.getClosedOn());
         assertNotNull(actualMother.getLastModifiedTime());
         assertReflectionEqualsWithIgnore(flw, actualMother.getFlw());
         assertReflectionEqualsWithIgnore(flw, actualMother.getClosedBy());
 
     }
 
     @Test
     public void shouldUpdateCloseCaseWhenChildCaseAlreadyExists() {
         Flw flw = new FlwBuilder().flwId(userId).build();
         template.save(flw);
 
         ChildCase childCase = new ChildCaseBuilder()
                 .caseId(caseId)
                 .flw(new FlwBuilder().flwId("oldda0284e008d2e0c980fb13f989136").build())
                 .build();
         template.save(childCase);
 
         CaseEvent closedCase = new CaseEventBuilder(caseId)
                 .withAction("CLOSE")
                 .withUserId(userId)
                 .withServerModifiedOn(serverModifiedOn)
                 .build();
 
         closeCaseProcessor.process(closedCase);
 
         List<ChildCase> childCases = template.loadAll(ChildCase.class);
         assertEquals(1, childCases.size());
 
         ChildCase actualChild = childCases.get(0);
         assertEquals(Boolean.TRUE, actualChild.getClosed());
         assertEquals((new DateTime(2013, 6, 13, 0, 0, 0)).toDate(), actualChild.getClosedOn());
         assertNotNull(actualChild.getLastModifiedTime());
         assertReflectionEqualsWithIgnore(flw, actualChild.getFlw());
         assertReflectionEqualsWithIgnore(flw, actualChild.getClosedBy());
 
     }
 
    @Test
    public void shouldNotSaveCaseWhenTryingToCloseCaseThatDoesNotExists() {
         closeCaseProcessor.process(new CaseEventBuilder(caseId).withAction("CLOSE").withDateModified("2012-01-01").build());

        List<ChildCase> childCases = template.loadAll(ChildCase.class);
        List<MotherCase> motherCases = template.loadAll(MotherCase.class);
        assertTrue(childCases.isEmpty());
        assertTrue(motherCases.isEmpty());
     }
 
     @Test
     public void shouldCreateOnlyOneFlwForClosedByAndFlw() {
         ChildCase childCase = new ChildCaseBuilder()
                 .caseId(caseId)
                 .flw(null)
                 .build();
         template.save(childCase);
 
         CaseEvent closedCase = new CaseEventBuilder(caseId)
                 .withAction("CLOSE")
                 .withUserId(userId)
                 .withServerModifiedOn(serverModifiedOn)
                 .build();
 
 
         closeCaseProcessor.process(closedCase);
 
         List<Flw> flws = template.loadAll(Flw.class);
         assertEquals(1, flws.size());
         List<ChildCase> childCases = template.loadAll(ChildCase.class);
 
         assertEquals(userId, childCases.get(0).getClosedBy().getFlwId());
         assertEquals(userId, childCases.get(0).getFlw().getFlwId());
     }
 
     @Test
     public void shouldNotUpdateClosedFieldsForMotherOnlyIfServerDateModifiedIsOld() {
         Date serverDateModified = DateTime.parse("2012-01-05").toDate();
         String oldFlwId = "faab798501ee48fa9d557a24e402ea9b";
         String newFlwId = "23ab798501ee48fa9d557a24e402ea9b";
         Flw flw = new FlwBuilder().flwId(oldFlwId).build();
         template.save(flw);
 
         MotherCase mother = new MotherCaseBuilder().caseId(caseId).flw(flw).serverDateModified(serverDateModified).close().build();
         template.save(mother);
 
         CaseEvent closedCase = new CaseEventBuilder(caseId)
                 .withAction("CLOSE")
                 .withUserId(newFlwId)
                 .withServerModifiedOn("2012-01-01")
                 .build();
 
         closeCaseProcessor.process(closedCase);
 
         List<MotherCase> motherCases = template.loadAll(MotherCase.class);
         assertEquals(1, motherCases.size());
         assertEquals(serverDateModified, motherCases.get(0).getClosedOn());
         assertEquals(oldFlwId, motherCases.get(0).getClosedBy().getFlwId());
     }
 
     @Test
     public void shouldUpdateClosedFieldsIfServerDateModifiedIsNew() throws ParseException {
         Date serverDateModified = DateTime.parse("2013-01-05").toDate();
         String oldFlwId = "faab798501ee48fa9d557a24e402ea9b";
         String newFlwId = "23ab798501ee48fa9d557a24e402ea9b";
         Flw flw = new FlwBuilder().flwId(oldFlwId).build();
         template.save(flw);
 
         MotherCase mother = new MotherCaseBuilder().caseId(caseId).flw(flw).serverDateModified(serverDateModified).close().build();
         template.save(mother);
 
         String closeDate = "2013-07-05 01:27:35";
         CaseEvent closedCase = new CaseEventBuilder(caseId)
                 .withAction("CLOSE")
                 .withUserId(newFlwId)
                 .withServerModifiedOn(closeDate)
                 .build();
 
         closeCaseProcessor.process(closedCase);
 
         List<MotherCase> motherCases = template.loadAll(MotherCase.class);
         assertEquals(1, motherCases.size());
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
         assertEquals(dateFormat.parse(closeDate), motherCases.get(0).getClosedOn());
         assertEquals(newFlwId, motherCases.get(0).getClosedBy().getFlwId());
     }
 
     @Test
     public void shouldNotUpdateClosedFieldsForChildOnlyIfSererDateModifiedOld() {
         Date serverDateModified = DateTime.parse("2012-01-05").toDate();
         String oldFlwId = "faab798501ee48fa9d557a24e402ea9b";
         String newFlwId = "23ab798501ee48fa9d557a24e402ea9b";
         Flw flw = new FlwBuilder().flwId(oldFlwId).build();
         template.save(flw);
 
         ChildCase child = new ChildCaseBuilder().caseId(caseId).flw(flw).serverDateModified(serverDateModified).close().build();
         template.save(child);
 
         CaseEvent closedCase = new CaseEventBuilder(caseId)
                 .withAction("CLOSE")
                 .withUserId(newFlwId)
                 .withServerModifiedOn("2012-01-01")
                 .build();
 
         closeCaseProcessor.process(closedCase);
 
         List<ChildCase> childCases = template.loadAll(ChildCase.class);
         assertEquals(1, childCases.size());
         assertEquals(serverDateModified, childCases.get(0).getClosedOn());
         assertEquals(oldFlwId, childCases.get(0).getClosedBy().getFlwId());
     }
 }
