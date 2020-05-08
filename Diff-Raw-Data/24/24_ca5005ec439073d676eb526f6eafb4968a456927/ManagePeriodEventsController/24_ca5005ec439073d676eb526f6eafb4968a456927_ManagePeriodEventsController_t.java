 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
 import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
 import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
 import org.springframework.beans.propertyeditors.CustomNumberEditor;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  * @author Rhett Sutphin
  */
 public class ManagePeriodEventsController extends SimpleFormController {
     private PeriodDao periodDao;
     private ActivityDao activityDao;
 
     public ManagePeriodEventsController() {
         setBindOnNewForm(true);
         setCommandClass(ManagePeriodEventsCommand.class);
         setFormView("managePeriod");
     }
 
     protected Object formBackingObject(HttpServletRequest request) throws Exception {
         int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
         return new ManagePeriodEventsCommand(periodDao.getById(id), activityDao);
     }
 
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
     }
 
     protected Map<String, Object> referenceData(
         HttpServletRequest request, Object oCommand, Errors errors
     ) throws Exception {
         ManagePeriodEventsCommand command = (ManagePeriodEventsCommand) oCommand;
 
         Map<String, Object> refdata = new HashMap<String, Object>();
 
         Integer selectedActivityId = ServletRequestUtils.getIntParameter(request, "selectedActivity");
         if (selectedActivityId != null) {
             refdata.put("selectedActivity", activityDao.getById(selectedActivityId));
         }
 
         refdata.put("activityTypes", ActivityType.values());
         refdata.put("activities", activityDao.getAll());
         refdata.put("activitiesById", DomainObjectTools.byId(activityDao.getAll()));
         ControllerTools.addHierarchyToModel(command.getPeriod(), refdata);
 
         return refdata;
     }
 
     protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
         ManagePeriodEventsCommand command = (ManagePeriodEventsCommand) oCommand;
         command.apply();
         periodDao.save(command.getPeriod());
         return new ModelAndView("redirectToCalendarTemplate", "id", command.getPeriod().getArm().getEpoch().getPlannedCalendar().getStudy().getId());
     }
 
     ////// CONFIGURATION
 
     @Required
     public void setPeriodDao(PeriodDao periodDao) {
         this.periodDao = periodDao;
     }
 
     @Required
     public void setActivityDao(ActivityDao activityDao) {
         this.activityDao = activityDao;
     }
 
 }
