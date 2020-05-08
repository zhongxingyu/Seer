 
 package edu.northwestern.bioinformatics.studycalendar.web;
 
 import org.springframework.web.servlet.mvc.AbstractController;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.web.bind.ServletRequestUtils;
 
 import gov.nih.nci.security.authorization.domainobjects.User;
 
 import org.apache.log4j.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
 import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
 import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
 
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.Map;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 
 /**
  * @author Yufang Wang
  */
 @AccessControl(protectionGroups = StudyCalendarProtectionGroup.SITE_COORDINATOR)
 public class SiteParticipantCoordinatorListController extends AbstractController {
     //private Map<String, List> participantcoordinators;
 	private SiteDao siteDao;
 	private SiteService siteService;
 	private static final Logger log = Logger.getLogger(AssignParticipantCoordinatorsToSiteController.class.getName());
 
     protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
         Map<String, Object> model = new HashMap<String, Object>();
         
         
         Site site= siteDao.getById(ServletRequestUtils.getRequiredIntParameter(request, "id"));
        Map<String, List> participantcoordinatorList = siteService.getParticipantCoordinatorLists(site);
         
         
         log.debug("+++++id=" + site.getId());
         List<User> pclist = new ArrayList<User> ();
         pclist = participantcoordinatorList.get(SiteService.ASSIGNED_USERS);
         if(pclist != null) {
         	Iterator i = pclist.iterator();
         	while(i.hasNext()) {
         		User pc = (User)i.next();
         		log.debug("+++++pcname=" + pc.getName() + "pcid=" + pc.getUserId());
         	} 
         } else {
         	log.debug("+++++ pclist is null");
         }
        
         model.put("participantcoordinators", participantcoordinatorList.get(SiteService.ASSIGNED_USERS));
         
         
         List<User> pclt = (List<User>)model.get("participantcoordinators");
         if(pclt!=null) {
         	Iterator it = pclt.iterator();
         	while(it.hasNext()) {
         		User pc = (User)it.next();
         		log.debug("---+++pcname=" + pc.getName() + "pcid=" + pc.getUserId());
         	}
         } else {
         	log.debug("---+++ cannot get pclt.");
         }
   	    
        
         
         model.put("site", site);
         return new ModelAndView("siteParticipantCoordinatorList", model);
     }
 
     ////// CONFIGURATION
 
     @Required
     public void setSiteDao(SiteDao siteDao) {
         this.siteDao = siteDao;
     }
      
     @Required
     public void setSiteService(SiteService siteService) {
         this.siteService = siteService;
     }
 }
 
