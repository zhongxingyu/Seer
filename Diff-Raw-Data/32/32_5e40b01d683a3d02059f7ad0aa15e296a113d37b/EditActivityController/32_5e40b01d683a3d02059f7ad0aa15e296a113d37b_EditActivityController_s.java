 package edu.northwestern.bioinformatics.studycalendar.web.activity;
 
 import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.beans.factory.annotation.Required;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 public class EditActivityController extends PscAbstractController {
     private ActivityDao activityDao;
     private ActivityTypeDao activityTypeDao;
 
     public EditActivityController() {
     }
 
     @Override
     protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
 
         Integer activityId = ServletRequestUtils.getRequiredIntParameter(request, "activityId");
         String activityName = ServletRequestUtils.getRequiredStringParameter(request, "activityName");
         String activityCode = ServletRequestUtils.getRequiredStringParameter(request, "activityCode");
         Integer activityType = ServletRequestUtils.getRequiredIntParameter(request, "activityType");
         String activityDescription = ServletRequestUtils.getRequiredStringParameter(request, "activityDescription");
 
         Activity activity = activityDao.getById(activityId);
         activity.setName(activityName);
         activity.setType(activityTypeDao.getById(activityType));
         activity.setCode(activityCode);
         activity.setDescription(activityDescription);
         activityDao.save(activity);
        return null;
     }
 
 
     //// CONFIGURATION
 
     @Required
     public void setActivityDao(ActivityDao activityDao) {
         this.activityDao = activityDao;
     }
 
     @Required
     public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
         this.activityTypeDao = activityTypeDao;
     }
 }
