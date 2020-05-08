 package edu.northwestern.bioinformatics.studycalendar.web.activity;
 
 import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.beans.factory.annotation.Required;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
 
 
 public class EditActivityController extends PscAbstractController {
     private ActivityDao activityDao;
     private ActivityTypeDao activityTypeDao;
    private PlannedActivityDao plannedActivityDao;
 
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

        Map<String, Object> model = new HashMap<String, Object>();
        List<Activity> activities = activityDao.getBySourceId(activity.getSource().getId());
        Map<Integer, Boolean> enableDelete = new HashMap<Integer, Boolean>();
        for (Activity activity1 : activities) {
            if (plannedActivityDao.getPlannedActivitiesForActivity(activity1.getId()).size()>0) {
                enableDelete.put(activity1.getId(), false);
            } else {
                enableDelete.put(activity1.getId(), true);
            }
        }
        model.put("enableDeletes", enableDelete);
        model.put("activitiesPerSource", activities);
        model.put("activityTypes", activityTypeDao.getAll());
        model.put("displayCreateNewActivity", Boolean.TRUE);
        model.put("showtable", Boolean.TRUE);
        return new ModelAndView("template/ajax/activityTableUpdate", model);
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

    @Required
    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }
 }
