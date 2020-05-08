 package edu.northwestern.bioinformatics.studycalendar.web.subject;
 
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
 import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
 import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
 import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
 import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
 import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
 import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
 import gov.nih.nci.cabig.ctms.editors.GridIdentifiableDaoBasedEditor;
 import gov.nih.nci.cabig.ctms.lang.NowFactory;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Rhett Sutphin
  */
 public class SubjectCentricScheduleController extends PscAbstractController {
     private SubjectDao subjectDao;
     private AuthorizationService authorizationService;
     private NowFactory nowFactory;
     private ApplicationSecurityManager applicationSecurityManager;
 
     public SubjectCentricScheduleController() {
         setCrumb(new Crumb());
     }
 
     @Override
     protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
         DaoBasedEditor editor = new GridIdentifiableDaoBasedEditor(subjectDao);
         editor.setAsText(ServletRequestUtils.getRequiredStringParameter(request, "subject"));
         Subject subject = (Subject) editor.getValue();
 
         List<StudySubjectAssignment> allAssignments = subject.getAssignments();
         List<StudySubjectAssignment> visibleAssignments
             = authorizationService.filterAssignmentsForVisibility(allAssignments, applicationSecurityManager.getUser());
         Set<StudySubjectAssignment> hiddenAssignments
             = new LinkedHashSet<StudySubjectAssignment>(allAssignments);
         for (StudySubjectAssignment visibleAssignment : visibleAssignments) {
             hiddenAssignments.remove(visibleAssignment);
         }
         SubjectCentricSchedule schedule = new SubjectCentricSchedule(
             visibleAssignments, new ArrayList<StudySubjectAssignment>(hiddenAssignments), nowFactory);
 
         ModelMap model = new ModelMap("schedule", schedule);
         model.addObject(subject);
        model.addAttribute("schedulePreview", false);
         return new ModelAndView("subject/schedule", model);
     }
 
     ////// CONFIGURATION
 
     @Required
     public void setSubjectDao(SubjectDao subjectDao) {
         this.subjectDao = subjectDao;
     }
 
     @Required
     public void setAuthorizationService(AuthorizationService authorizationService) {
         this.authorizationService = authorizationService;
     }
 
     @Required
     public void setNowFactory(NowFactory nowFactory) {
         this.nowFactory = nowFactory;
     }
 
     @Required
     public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
         this.applicationSecurityManager = applicationSecurityManager;
     }
 
     private static class Crumb extends DefaultCrumb {
         @Override
         public String getName(BreadcrumbContext context) {
             return new StringBuilder()
                 .append("Comprehensive schedule for ").append(context.getSubject().getFullName())
                 .toString();
         }
 
         @Override
         public Map<String, String> getParameters(BreadcrumbContext context) {
             return createParameters(
                 "subject", context.getSubject().getId().toString()
             );
         }
     }
 }
