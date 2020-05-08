 package edu.northwestern.bioinformatics.studycalendar.web;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.beans.propertyeditors.CustomCollectionEditor;
 import org.springframework.validation.BindException;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
 import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
 import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
 import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
 
 /**
  * @author Padmaja Vedula
  * @author Rhett Sutphin
  * @author Jaron Sampson
  */
 
 @AccessControl(protectionGroups = StudyCalendarProtectionGroup.SITE_COORDINATOR)
 public class ParticipantCoordinatorController extends PscSimpleFormController {
     private static Log log = LogFactory.getLog(ParticipantCoordinatorController.class);
 
 	private TemplateService templateService;
 	private StudyDao studyDao;
 	private SiteDao	siteDao;
 
     public ParticipantCoordinatorController() {
         setCommandClass(ParticipantCoordinatorCommand.class);
         setFormView("assignParticipantCoordinator");
     }
 
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Set.class, "assignedCoordinators", new CustomCollectionEditor(Set.class));
        binder.registerCustomEditor(Set.class, "availableCoordinators", new CustomCollectionEditor(Set.class));
    } 

     protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
     	String userName = ApplicationSecurityManager.getUser(httpServletRequest);
     	
     	Map<String, Object> refdata = new HashMap<String, Object>();
     	Study study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "id"));
         List<Site> siteCoordinatorSites = templateService.getSitesForTemplateSiteCd(userName, study);
         refdata.put("study", study);
         refdata.put("sites", siteCoordinatorSites);
         return refdata;
     }
 
     protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
     	ParticipantCoordinatorCommand assignCommand = (ParticipantCoordinatorCommand) oCommand;
     	Study assignedStudy = studyDao.getById(assignCommand.getStudyId());
     	Site assignedSite = siteDao.getById(assignCommand.getSiteId());
    	log.debug("+$+" + assignCommand.getAssignedCoordinators());
    	templateService.assignTemplateToParticipantCds(assignedStudy, assignedSite, assignCommand.getAssignedCoordinators(), assignCommand.getAvailableCoordinators());
         return ControllerTools.redirectToCalendarTemplate(ServletRequestUtils.getIntParameter(request, "id"));
     }
 
     protected Object formBackingObject(HttpServletRequest request) throws Exception {
     	ParticipantCoordinatorCommand command = new ParticipantCoordinatorCommand();
         return command;
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
