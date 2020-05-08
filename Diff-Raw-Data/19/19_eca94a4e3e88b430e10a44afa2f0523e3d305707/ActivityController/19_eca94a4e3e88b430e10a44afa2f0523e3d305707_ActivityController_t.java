 package edu.northwestern.bioinformatics.studycalendar.web.activity;
 
 import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
 import edu.northwestern.bioinformatics.studycalendar.dao.*;
 import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.beans.factory.annotation.Required;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.*;
 
 
 @AccessControl(roles = Role.STUDY_ADMIN)
 
 public class ActivityController extends PscAbstractController {
     private ActivityDao activityDao;
     private SourceDao sourceDao;
     private PlannedActivityDao plannedActivityDao;
 
     public ActivityController() {
     }
 
     @Override
     protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
         Map<String, Object> model = new HashMap<String, Object>();
         if ("POST".equals(request.getMethod())) {
             String sourceId = ServletRequestUtils.getRequiredStringParameter(request, "sourceId");
             if (!sourceId.equals("select")) {
                 model = processRequest(model, sourceId);
             }
             return new ModelAndView("template/ajax/activityTableUpdate", model);
         } else {
             if (request.getParameterMap().isEmpty()) {
                 model.put("sources", sourceDao.getAll());
             } else {
                 String sourceId = ServletRequestUtils.getRequiredStringParameter(request, "sourceId");
                 model = processRequest(model, sourceId);
                 model.put("sourceId", new Integer(sourceId));
                 model.put("sources", sourceDao.getAll());
             }
 
             return new ModelAndView("activity", model);
         }
     }
 
     private Map<String, Object> processRequest( Map<String, Object> model, String sourceId) throws Exception{
         List<Activity> activities;
 
         if (sourceId.equals("selectAll")) {
             activities = activityDao.getAll();
         } else {
             activities = activityDao.getBySourceId(new Integer(sourceId));
         }
         Map<Integer, Boolean> enableDelete = new HashMap<Integer, Boolean>();
         for (Activity a : activities) {
             if (plannedActivityDao.getPlannedActivitiesForActivity(a.getId()).size()>0) {
                 enableDelete.put(a.getId(), false);
             } else {
                 enableDelete.put(a.getId(), true);
             }
         }
         model.put("activitiesPerSource", activities);
         model.put("enableDeletes", enableDelete);
         model.put("activityTypes", ActivityType.values());
         if (! (sourceId.equals("select") || sourceId.equals("selectAll"))) {
             model.put("displayCreateNewActivity", Boolean.TRUE);
            model.put("showtable", Boolean.TRUE);
         } else {
             model.put("displayCreateNewActivity", Boolean.FALSE);
         }
 
         return model;
     }
 
 
     //// CONFIGURATION
 
     @Required
     public void setActivityDao(ActivityDao activityDao) {
         this.activityDao = activityDao;
     }
 
     @Required
     public void setSourceDao(SourceDao sourceDao) {
         this.sourceDao = sourceDao;
     }
 
     @Required
     public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
         this.plannedActivityDao = plannedActivityDao;
     }
 }
