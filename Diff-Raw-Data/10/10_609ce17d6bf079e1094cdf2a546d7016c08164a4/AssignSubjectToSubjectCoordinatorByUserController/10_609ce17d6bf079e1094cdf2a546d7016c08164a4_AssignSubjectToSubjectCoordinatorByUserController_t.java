 package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;
 
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Role;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
 import edu.northwestern.bioinformatics.studycalendar.domain.User;
 import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
 import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparator;
 import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
 import edu.northwestern.bioinformatics.studycalendar.service.UserService;
 import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
 import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 import org.springframework.validation.BindException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 @AccessControl(roles = {Role.SITE_COORDINATOR})
 public class AssignSubjectToSubjectCoordinatorByUserController extends PscSimpleFormController {
     private UserDao userDao;
     private StudySiteService studySiteService;
     private UserService userService;
     private SubjectDao subjectDao;
     private StudyDao studyDao;
     private SiteDao siteDao;
     private ApplicationSecurityManager applicationSecurityManager;
 
 
 
     public AssignSubjectToSubjectCoordinatorByUserController() {
         setFormView("dashboard/sitecoordinator/assignSubjectToSubjectCoordinator");
         setSuccessView("studyList");
     }
 
     //We have to remember 2 cases to process here: when selected is "unassigned" and when selected is actually the existing user
     protected Map referenceData(HttpServletRequest request) throws Exception {
         Map<String, Object> refData = new HashMap<String, Object>();
         AssignSubjectToSubjectCoordinatorByUserCommand command = new AssignSubjectToSubjectCoordinatorByUserCommand();
         User siteCoordinator = getSiteCoordinator();
         String subjectCoordinatorIdString = ServletRequestUtils.getStringParameter(request, "selected");
         Integer subjectCoordinatorId = null;
 
         Map<Site, Map<Study, List<Subject>>> displayMap = null;
          Map<Study, Map<Site, List<User>>> studySiteParticipCoordMap = null;
 
          if (command.isUnassigned(subjectCoordinatorIdString)) {
             displayMap = buildDisplayMap(siteCoordinator, true);
             studySiteParticipCoordMap = buildStudySiteSubjectCoordinatorMapForUnassigned(siteCoordinator);
          } else {
              subjectCoordinatorId = Integer.parseInt(subjectCoordinatorIdString);
              if (subjectCoordinatorId != null) {
                  User subjectCoordinator = userDao.getById(subjectCoordinatorId);
                  command.setSubjectCoordinator(subjectCoordinator);
                  displayMap = buildDisplayMap(subjectCoordinator, false);
                  studySiteParticipCoordMap = buildStudySiteSubjectCoordinatorMap(subjectCoordinator);
              }
          }
 
          refData.put("displayMap", displayMap);
          refData.put("subjectCoordinatorStudySites", studySiteParticipCoordMap);
          refData.put("assignableUsers", userService.getSiteCoordinatorsAssignableUsers(siteCoordinator));
          if (command.isUnassigned(subjectCoordinatorIdString)) {
              refData.put("selectedId", "unassigned");
          } else {
              refData.put("selectedId", subjectCoordinatorId);
          }
 
          return refData;
      }
 
    //taking care of the case, when selected is null. We defaulting it to "unassigned"
     @Override
     protected ModelAndView showForm(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BindException e) throws Exception {
         String subjectCoordinatorIdString = ServletRequestUtils.getStringParameter(httpServletRequest, "selected");
         if (subjectCoordinatorIdString == null) {
            RedirectView rv = new RedirectView("/pages/dashboard/siteCoordinator/assignSubjectToSubjectCoordinatorByUser?selected=unassigned", true);
             return new ModelAndView(rv);
          } else {
             return super.showForm(httpServletRequest, httpServletResponse, e);
         }
     }
 
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         super.initBinder(request, binder);
 
         binder.registerCustomEditor(User.class, new DaoBasedEditor(userDao));
         binder.registerCustomEditor(Site.class, "site", new DaoBasedEditor(siteDao));
         binder.registerCustomEditor(Study.class, "study", new DaoBasedEditor(studyDao));
         binder.registerCustomEditor(Subject.class, "subjectCoordinator", new DaoBasedEditor(subjectDao));
         binder.registerCustomEditor(Subject.class, "subjects", new DaoBasedEditor(subjectDao));
     }
 
 
     protected ModelAndView onSubmit(Object o) throws Exception {
         AssignSubjectToSubjectCoordinatorByUserCommand command = (AssignSubjectToSubjectCoordinatorByUserCommand) o;
 
         command.assignSubjectsToSubjectCoordinator();
 
         Map<String, Object> model = new HashMap<String, Object>();
         model.put("study", command.getStudy());
         model.put("site", command.getSite());
         if (command.getSelected() != null && !command.isUnassigned(command.getSelected())) {
             Integer subjectCoordinatorId = Integer.parseInt(command.getSelected());
             User subjectCoordinator = userDao.getById(subjectCoordinatorId);
             model.put("subjects", buildSubjects(findStudySite(command.getStudy(), command.getSite()), subjectCoordinator, false));
         } else if (command.getSelected() != null && command.isUnassigned(command.getSelected())) {
             model.put("subjects", buildSubjects(findStudySite(command.getStudy(), command.getSite()), getSiteCoordinator(), true));
         }
         return new ModelAndView("dashboard/sitecoordinator/ajax/displaySubjects", model);
     }
 
 
     protected Object formBackingObject(HttpServletRequest request) throws Exception {
         AssignSubjectToSubjectCoordinatorByUserCommand command = new AssignSubjectToSubjectCoordinatorByUserCommand();
         command.setSubjectDao(subjectDao);
         return command;
     }
 
     protected User getSiteCoordinator() {
         return applicationSecurityManager.getFreshUser();
     }
                                                                                                                 
     protected Map<Site, Map<Study, List<Subject>>> buildDisplayMap(User user, Boolean isForUnassigned) {
         Map<Site, Map<Study, List<Subject>>> displayMap = new TreeMap<Site, Map<Study, List<Subject>>>(new NamedComparator());
         if (user != null) {
             List<StudySite> studySitesForSiteCoordinator = studySiteService.getAllStudySitesForSubjectCoordinator(user);
             for (StudySite studySite: studySitesForSiteCoordinator){
                 Study study= studySite.getStudy();
                 Site site = studySite.getSite();
                 List<Subject> studySubjects = buildSubjects(studySite, user, isForUnassigned);
                 if (studySubjects.size() > 0) {
                     if (!displayMap.containsKey(site)) {
                         displayMap.put(site, new TreeMap<Study, List<Subject>>(new NamedComparator()));
                     }
                     if (!displayMap.get(site).containsKey(study)) {
                         displayMap.get(site).put(study, new ArrayList<Subject>());
                     }
                     displayMap.get(site).get(study).addAll(studySubjects);
                 }
 
             }
         }
         return displayMap;
     }
 
     protected Map<Study, Map<Site, List<User>>> buildStudySiteSubjectCoordinatorMapForUnassigned(User siteCoordinator) {
         Map<Study, Map<Site, List<User>>> studySiteSubjectCoordinatorMap = new HashMap<Study, Map<Site, List<User>>>();
         List<User> otherStudySiteSubjectCoords = new ArrayList<User>();
         List<User> allUsers = userService.getSiteCoordinatorsAssignableUsers(siteCoordinator);
         for (User user: allUsers) {
             List<StudySite> allStudySitesPerUser = studySiteService.getAllStudySitesForSubjectCoordinator(user);
             for (StudySite studySite: allStudySitesPerUser){
                 List<StudySubjectAssignment> allStudySubjectAssignmentsPerUser = studySite.getStudySubjectAssignments();
                 boolean unassigned = false;
                 for (StudySubjectAssignment ssa: allStudySubjectAssignmentsPerUser){
                     User subjCoord = ssa.getSubjectCoordinator();
                     if (subjCoord == null){
                         unassigned= true;
                     }
                 }
                 if (unassigned) {
                     if (! otherStudySiteSubjectCoords.contains(user)){
                         otherStudySiteSubjectCoords.add(user);
                     }
                     Collections.sort(otherStudySiteSubjectCoords, new NamedComparator());
                     if (!studySiteSubjectCoordinatorMap.containsKey(studySite.getStudy())) {
                         studySiteSubjectCoordinatorMap.put(studySite.getStudy(), new HashMap<Site, List<User>>());
                     }
                     studySiteSubjectCoordinatorMap.get(studySite.getStudy()).put(studySite.getSite(), otherStudySiteSubjectCoords);
                 }
             }
         }
         return studySiteSubjectCoordinatorMap;
     }
 
 
     protected Map<Study, Map<Site, List<User>>> buildStudySiteSubjectCoordinatorMap(User subjectCoordinator) {
         Map<Study ,Map<Site, List<User>>> studySiteSubjectCoordinatorMap = new HashMap<Study ,Map<Site, List<User>>>();
 
         if (subjectCoordinator != null ) {
             List<StudySite> studySites = studySiteService.getAllStudySitesForSubjectCoordinator(subjectCoordinator);
             for (StudySite studySite : studySites) {
                 List<User> otherStudySiteSubjectCoords = new ArrayList<User>();
                 List<UserRole> userRoles = studySite.getUserRoles();
                 for (UserRole userRole : userRoles ) {
                     User user = userRole.getUser();
                     if (!user.equals(subjectCoordinator)) {
                         otherStudySiteSubjectCoords.add(user);
                     }
                 }
                 Collections.sort(otherStudySiteSubjectCoords, new NamedComparator());
                 if (!studySiteSubjectCoordinatorMap.containsKey(studySite.getStudy())) {
                     studySiteSubjectCoordinatorMap.put(studySite.getStudy(), new HashMap<Site, List<User>>());
                 }
                 studySiteSubjectCoordinatorMap.get(studySite.getStudy()).put(studySite.getSite(), otherStudySiteSubjectCoords);
             }
         }
         return studySiteSubjectCoordinatorMap;
     }
 
     public List<Subject> buildSubjects(StudySite studySite, User subjectCoordinator, Boolean isForUnassigned) {
         List<Subject> studySubjects = new ArrayList<Subject>();
         if (studySite != null ) {
             for (StudySubjectAssignment assignment : studySite.getStudySubjectAssignments()) {
                 Subject subject = assignment.getSubject();
                 if (assignment.getSubjectCoordinator() != null && !isForUnassigned) {
                     if (assignment.getSubjectCoordinator().equals(subjectCoordinator) && !assignment.isExpired()) {
                         studySubjects.add(subject);
                     }
                 } else if (assignment.getSubjectCoordinator() == null && isForUnassigned){
                     studySubjects.add(subject);
                 }
             }
         }
 
         Collections.sort(studySubjects, new Comparator<Subject>(){
             public int compare(Subject subject, Subject subject1) {
                 return subject.getLastFirst().compareTo(subject1.getLastFirst());
             }
         });
         return studySubjects;
     }
 
     public void setUserDao(UserDao userDao) {
         this.userDao = userDao;
     }
 
     public void setStudySiteService(StudySiteService studySiteService) {
         this.studySiteService = studySiteService;
     }
 
     public void setUserService(UserService userService) {
         this.userService = userService;
     }
 
     public void setSubjectDao(SubjectDao subjectDao) {
         this.subjectDao = subjectDao;
     }
 
     public void setStudyDao(StudyDao studyDao) {
         this.studyDao = studyDao;
     }
 
     public void setSiteDao(SiteDao siteDao) {
         this.siteDao = siteDao;
     }
 
     @Required
     public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
         this.applicationSecurityManager = applicationSecurityManager;
     }
 }
