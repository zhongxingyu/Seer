 package edu.northwestern.bioinformatics.studycalendar.web;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
 import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
 import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
 import org.easymock.classextension.EasyMock;
 import static org.easymock.classextension.EasyMock.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 /**
  * @author Rhett Sutphin
  */
 public class StudyListControllerTest extends ControllerTestCase {
     private StudyListController controller;
     private StudyDao studyDao;
     private TemplateService templateService;
     private SiteService siteService;
 
     protected void setUp() throws Exception {
         super.setUp();
         controller = new StudyListController();
         studyDao = registerDaoMockFor(StudyDao.class);
         templateService = registerMockFor(TemplateService.class);
         siteService = registerMockFor(SiteService.class);
         controller.setStudyDao(studyDao);
         controller.setTemplateService(templateService);
         controller.setSiteService(siteService);
     }
 
     public void testModelAndView() throws Exception {
         Study complete = Fixtures.createSingleEpochStudy("Complete", "E1");
        complete.setAmended(false);
         complete.getPlannedCalendar().setComplete(true);
         Study incomplete = Fixtures.createSingleEpochStudy("Incomplete", "E1");
        incomplete.setAmended(false);
         incomplete.getPlannedCalendar().setComplete(false);
         List<Study> studies = Arrays.asList(incomplete, complete);
         List<Site> sites = new ArrayList<Site>();
         ApplicationSecurityManager.setUser(request, "jimbo");
 
         expect(studyDao.getAll()).andReturn(studies);
         expect(templateService.checkOwnership("jimbo", studies)).andReturn(studies);
         expect(siteService.getSitesForSiteCd("jimbo")).andReturn(sites);
         replayMocks();
 
         ModelAndView mv = controller.handleRequest(request, response);
         verifyMocks();
 
         assertEquals("Complete studies list missing or wrong", Arrays.asList(complete),
             mv.getModel().get("completeStudies"));
         assertEquals("Incomplete studies list missing or wrong", Arrays.asList(incomplete),
             mv.getModel().get("incompleteStudies"));
         assertSame("Sites list missing or wrong", sites, mv.getModel().get("sites"));
         assertEquals("studyList", mv.getViewName());
     }
 }
