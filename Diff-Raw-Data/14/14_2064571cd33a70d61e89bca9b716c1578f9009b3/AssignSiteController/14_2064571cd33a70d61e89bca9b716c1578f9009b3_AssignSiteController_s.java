 package edu.northwestern.bioinformatics.studycalendar.web;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.validation.BindException;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.Role;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
 import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
 import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 
 /**
  * @author Padmaja Vedula
  */
 @AccessControl(roles = Role.STUDY_ADMIN)
 public class AssignSiteController extends PscSimpleFormController {
     private TemplateService templateService;
     private StudyDao studyDao;
     private SiteDao siteDao;
 
     public AssignSiteController() {
         setCommandClass(AssignSiteCommand.class);
         setFormView("assignSite");
     }
 
     @Override
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         getControllerTools().registerDomainObjectEditor(binder, "assignedSites", siteDao);
         getControllerTools().registerDomainObjectEditor(binder, "availableSites", siteDao);
     }
 
     @Override
     protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
         Map<String, Object> refdata = new HashMap<String, Object>();
         Study study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "id"));
         Map<String, List<Site>> userLists = templateService.getSiteLists(study);
         refdata.put("study", study);
         refdata.put("assignedSites", userLists.get(StudyCalendarAuthorizationManager.ASSIGNED_PGS));
         refdata.put("availableSites", userLists.get(StudyCalendarAuthorizationManager.AVAILABLE_PGS));
         refdata.put("action", "Assign");
         return refdata;
     }
 
     @Override
     protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
         AssignSiteCommand assignCommand = (AssignSiteCommand) oCommand;
         Study assignedStudy = studyDao.getById(assignCommand.getStudyId());
         if ("true".equals(assignCommand.getAssign())) {
             templateService.assignTemplateToSites(assignedStudy, assignCommand.getAvailableSites());
         } else {
             try {
                 templateService.removeTemplateFromSites(assignedStudy, assignCommand.getAssignedSites());
             } catch (StudyCalendarValidationException scve) {
                errors.reject("error.literal", scve.getMessage());
             }
         }
         if (errors.hasErrors()) {
             Map<String, Object> model = referenceData(request);
             model.putAll(errors.getModel());
             return new ModelAndView(getFormView(), model);
         } else {
             return getControllerTools().redirectToCalendarTemplate(ServletRequestUtils.getIntParameter(request, "id"));
         }
     }
 
     ////// CONFIGURATION
 
     @Required
     public void setStudyDao(StudyDao studyDao) {
         this.studyDao = studyDao;
     }
 
     @Required
     public void setSiteDao(SiteDao siteDao) {
         this.siteDao = siteDao;
     }
 
     @Required
     public void setTemplateService(TemplateService templateService) {
         this.templateService = templateService;
     }
 
 }
