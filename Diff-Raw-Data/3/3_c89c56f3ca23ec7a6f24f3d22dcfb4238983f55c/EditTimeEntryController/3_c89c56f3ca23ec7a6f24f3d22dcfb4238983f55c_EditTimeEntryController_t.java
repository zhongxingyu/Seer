 package com.globant.katari.sample.time.view;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.globant.katari.sample.time.application.SaveTimeEntryCommand;
 import com.globant.katari.sample.time.domain.TimeEntry;
 import com.globant.katari.sample.time.domain.TimeRepository;
 
 /** Spring MVC Controller to edit a time entry.
  *
  * Subclasses need to override <code>createCommandBean</code> and
  * <code>createViewTimeEntryCommand</code> to retrieve a backing object for
  * the current form. Use method injection to override
  * <code>createCommandBean</code> and <code>createViewTimeEntryCommand</code>.
  *
  * @author nicolas.frontini
  */
 public abstract class EditTimeEntryController extends BaseTimeController {
 
   /** The class logger.
    */
  private static Logger log =
    LoggerFactory.getLogger(EditTimeEntryController.class);
 
   /** Default initialization for the controller.
    *
    * @param theTimeRepository The time entry repository.
    */
   public EditTimeEntryController(final TimeRepository theTimeRepository) {
     super(theTimeRepository);
   }
 
   /** Retrieve a backing object for the current form from the given request.
    *
    * @param request The HTTP request we are processing.
    *
    * @exception Exception if the application logic throws an exception.
    *
    * @return The command bean object.
    */
   @Override
   protected Object formBackingObject(final HttpServletRequest request)
       throws Exception {
     log.trace("Entering formBackingObject");
 
     SaveTimeEntryCommand saveTimeEntryCommand = (SaveTimeEntryCommand)
         createCommandBean();
     TimeEntry timeEntry = getTimeRepository().findTimeEntry(
         Long.valueOf(request.getParameter("timeEntryId")));
 
     saveTimeEntryCommand.setTimeEntryId(timeEntry.getId());
     saveTimeEntryCommand.setActivityId(timeEntry.getActivity().getId());
     saveTimeEntryCommand.setProjectId(timeEntry.getProject().getId());
     saveTimeEntryCommand.setStart(timeEntry.getPeriod().getStartHour() + ":"
         + timeEntry.getPeriod().getStartMinutes());
     saveTimeEntryCommand.setDuration(timeEntry.getPeriod().getDuration());
     saveTimeEntryCommand.setComment(timeEntry.getComment());
     saveTimeEntryCommand.setDate(timeEntry.getEntryDate());
 
     log.trace("Leaving formBackingObject");
     return saveTimeEntryCommand;
   }
 }
