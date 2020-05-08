 package edu.northwestern.bioinformatics.studycalendar.web.dashboard.subjectcoordinator;
 
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
 import edu.northwestern.bioinformatics.studycalendar.domain.Role;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.domain.User;
 import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
 import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
 import edu.northwestern.bioinformatics.studycalendar.service.SubjectCoordinatorDashboardService;
 import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
 import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
 import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
 import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.beans.propertyeditors.CustomNumberEditor;
 import org.springframework.validation.BindException;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @AccessControl(roles = Role.SUBJECT_COORDINATOR)
 public class ColleagueSubjectCoordinatorController extends PscSimpleFormController {
     private ScheduledActivityDao scheduledActivityDao;
     private StudyDao studyDao;
     private UserDao userDao;
     private SiteService siteService;
     private SubjectCoordinatorDashboardService subjectCoordinatorDashboardService;
     private AuthorizationService authorizationService;
     private ActivityTypeDao activityTypeDao;
 
     private ApplicationSecurityManager applicationSecurityManager;
 
     public ColleagueSubjectCoordinatorController() {
         setCommandClass(ScheduleCommand.class);
         setBindOnNewForm(true);
         setCrumb(new Crumb());
     }
 
     public ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
         setFormView("/colleagueSubjectCoordinatorSchedule");
         return showForm(request, response, errors, null);
     }
 
     protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
         Map<String, Object> model = new HashMap<String, Object>();
         Integer colleagueId = ServletRequestUtils.getIntParameter(httpServletRequest, "id");
 
         Collection<Site> sitesForCollegueUser = siteService.getSitesForSubjectCoordinator(userDao.getById(colleagueId).getName());
         List<Site> sitesToDisplay = getSitesToDisplay(applicationSecurityManager.getUserName(), userDao.getById(colleagueId).getName());
 
         if (sitesToDisplay.size()< sitesForCollegueUser.size()) {
             int numberOfUnauthorizedSites = sitesForCollegueUser.size() - sitesToDisplay.size();
             model.put("extraSites", "** Please note: There are " + numberOfUnauthorizedSites + " site(s) that are not authorized for viewing");
         }
 
         model.put("userName", userDao.getById(colleagueId));
         model.put("ownedStudies", getColleaguesStudies(colleagueId));
         model.put("mapOfUserAndCalendar", getPAService().getMapOfCurrentEvents(getStudySubjectAssignments(colleagueId), 7));
         model.put("pastDueActivities", getPAService().getMapOfOverdueEvents(getStudySubjectAssignments(colleagueId)));
         model.put("activityTypes", activityTypeDao.getAll());
 
         return model;
     }
 
     private List<Site> getSitesToDisplay(String userName, String colleagueName) {
         Collection<Site> sitesForCurrentUser = siteService.getSitesForSubjectCoordinator(userName);
         Collection<Site> sitesForCollegueUser = siteService.getSitesForSubjectCoordinator(colleagueName);
 
         List<Site> sitesToDisplay = new ArrayList<Site>();
         for (Site site: sitesForCurrentUser) {
             if (sitesForCollegueUser.contains(site)) {
                 sitesToDisplay.add(site);
             }
         }
         return sitesToDisplay;
     }
 
     //todo - might want to return List<StudySite> instead
     private List<Study> getColleaguesStudies(Integer colleagueId) throws Exception {
         String userName = applicationSecurityManager.getUserName();
         User user = applicationSecurityManager.getUser();
         List<Study> studies = studyDao.getAll();
         List<Study> ownedStudies = authorizationService.filterStudiesForVisibility(studies, user.getUserRole(Role.SUBJECT_COORDINATOR));
         List<StudySite> filteredStudySites = authorizationService.filterStudySitesForVisibilityFromStudiesList(ownedStudies, user.getUserRole(Role.SUBJECT_COORDINATOR));
 
         User colleagueUser = userDao.getById(colleagueId);
         List<Site> sitesToDisplay = getSitesToDisplay(userName, colleagueUser.getName());
         List<Study> colleaguesStudies = new ArrayList<Study>();
         for (Site site : sitesToDisplay) {
             List<StudySite> studySites= site.getStudySites();
             for (StudySite studySite : studySites) {
                 if (filteredStudySites.contains(studySite)) {
                     colleaguesStudies.add(studySite.getStudy());
                 }
             }
         }
         return colleaguesStudies;
     }
 
     private List<StudySubjectAssignment> getStudySubjectAssignments(Integer colleagueId) throws Exception {
         List<Study> colleaguesStudies = getColleaguesStudies(colleagueId);
 
         List<StudySubjectAssignment> studySubjectAssignments = getUserDao().getAssignments(userDao.getById(colleagueId));
         List<StudySubjectAssignment> colleagueOnlystudySubjectAssignments = new ArrayList<StudySubjectAssignment>();
         for (StudySubjectAssignment studySubjectAssignment: studySubjectAssignments) {
             Study study = studySubjectAssignment.getStudySite().getStudy();
             if (colleaguesStudies.contains(study)) {
                 colleagueOnlystudySubjectAssignments.add(studySubjectAssignment);
             }
         }
 
         return colleagueOnlystudySubjectAssignments;
     }
 
 
     protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
         ScheduleCommand command = new ScheduleCommand();
         command.setToDate(7);
         return command;
     }
 
     protected ModelAndView onSubmit(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Object oCommand, BindException errors) throws Exception {
         ScheduleCommand scheduleCommand = (ScheduleCommand) oCommand;
         Integer colleagueId = ServletRequestUtils.getIntParameter(request, "id");
         User user = userDao.getById(colleagueId);
 
         scheduleCommand.setUser(user);
         scheduleCommand.setUserDao(userDao);
         scheduleCommand.setScheduledActivityDao(scheduledActivityDao);
         Map<String, Object> model = scheduleCommand.execute(getPAService(), getStudySubjectAssignments(colleagueId));
         return new ModelAndView("template/ajax/listOfSubjectsAndEvents", model);
     }
 
     protected void initBinder(HttpServletRequest httpServletRequest,
                               ServletRequestDataBinder servletRequestDataBinder) throws Exception {
         super.initBinder(httpServletRequest, servletRequestDataBinder);
         servletRequestDataBinder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, false));
         servletRequestDataBinder.registerCustomEditor(ActivityType.class, new DaoBasedEditor(activityTypeDao));
     }
 
     ////// CONFIGURATION
 
     @Required
     public void setStudyDao(StudyDao studyDao) {
         this.studyDao = studyDao;
     }
 
     @Required
     public void setUserDao(UserDao userDao) {
         this.userDao = userDao;
     }
 
     public UserDao getUserDao() {
         return userDao;
     }
 
     @Required
     public void setScheduledActivityDao(ScheduledActivityDao scheduledActivityDao) {
         this.scheduledActivityDao = scheduledActivityDao;
     }
 
     public ScheduledActivityDao getScheduledActivityDao() {
         return scheduledActivityDao;
     }
 
     public SubjectCoordinatorDashboardService getPAService() {
         return subjectCoordinatorDashboardService;
     }
 
     @Required
     public void setSubjectCoordinatorDashboardService(SubjectCoordinatorDashboardService subjectCoordinatorDashboardService) {
         this.subjectCoordinatorDashboardService = subjectCoordinatorDashboardService;
     }
 
     @Required
     public void setSiteService(SiteService siteService) {
         this.siteService = siteService;
     }
 
     @Required
     public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
         this.applicationSecurityManager = applicationSecurityManager;
     }
 
     @Required
     public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
         this.activityTypeDao = activityTypeDao;
     }
 
     @Required
     public void setAuthorizationService(AuthorizationService authorizationService) {
         this.authorizationService = authorizationService;
     }
 
     private static class Crumb extends DefaultCrumb {
        @Override
         public String getName(DomainContext context) {
             return "Colleague Dashboard";
         }
 
         @Override
         public Map<String, String> getParameters(DomainContext context) {
             User user = context.getUser();
 
             Map<String, String> params = new HashMap<String, String>();
             if (user != null && user.getId() != null) {
                 params.put("id", user.getId().toString());
             }
             return params;
 
         }
     }
 }
