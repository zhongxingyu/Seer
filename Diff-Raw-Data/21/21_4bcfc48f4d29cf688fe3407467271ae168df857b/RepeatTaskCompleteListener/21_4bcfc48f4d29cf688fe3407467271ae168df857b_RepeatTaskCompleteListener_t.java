 package com.todoroo.astrid.repeats;
 
 import java.text.ParseException;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 
 import com.google.ical.iter.RecurrenceIterator;
 import com.google.ical.iter.RecurrenceIteratorFactory;
 import com.google.ical.values.DateTimeValueImpl;
 import com.google.ical.values.DateValue;
 import com.google.ical.values.DateValueImpl;
 import com.google.ical.values.Frequency;
 import com.google.ical.values.RRule;
 import com.google.ical.values.WeekdayNum;
 import com.todoroo.andlib.service.Autowired;
 import com.todoroo.andlib.service.DependencyInjectionService;
 import com.todoroo.andlib.service.ExceptionService;
 import com.todoroo.andlib.utility.DateUtilities;
 import com.todoroo.astrid.api.AstridApiConstants;
 import com.todoroo.astrid.model.Task;
 import com.todoroo.astrid.service.TaskService;
 
 public class RepeatTaskCompleteListener extends BroadcastReceiver {
 
     @Autowired
     private TaskService taskService;
 
     @Autowired
     private ExceptionService exceptionService;
 
     @Override
     public void onReceive(Context context, Intent intent) {
         long taskId = intent.getLongExtra(AstridApiConstants.EXTRAS_TASK_ID, -1);
         if(taskId == -1)
             return;
 
         DependencyInjectionService.getInstance().inject(this);
 
         Task task = taskService.fetchById(taskId, Task.ID, Task.RECURRENCE,
                 Task.DUE_DATE, Task.FLAGS, Task.HIDE_UNTIL);
         if(task == null)
             return;
 
         String recurrence = task.getValue(Task.RECURRENCE);
         if(recurrence != null && recurrence.length() > 0) {
             long newDueDate;
             try {
                 newDueDate = computeNextDueDate(task, recurrence);
                 if(newDueDate == -1)
                     return;
             } catch (ParseException e) {
                 exceptionService.reportError("repeat-parse", e); //$NON-NLS-1$
                 return;
             }
 
             long hideUntil = task.getValue(Task.HIDE_UNTIL);
             if(hideUntil > 0 && task.getValue(Task.DUE_DATE) > 0) {
                 hideUntil += newDueDate - task.getValue(Task.DUE_DATE);
             }
 
             // clone to create new task
            Task clone = taskService.clone(task);
            clone.setValue(Task.DUE_DATE, newDueDate);
            clone.setValue(Task.HIDE_UNTIL, hideUntil);
            clone.setValue(Task.COMPLETION_DATE, 0L);
            clone.setValue(Task.TIMER_START, 0L);
            clone.setValue(Task.ELAPSED_SECONDS, 0);
            taskService.save(clone, false);

            // clear recurrence from completed task so it can be re-completed
            task.setValue(Task.RECURRENCE, ""); //$NON-NLS-1$
             taskService.save(task, false);
         }
     }
 
     public static long computeNextDueDate(Task task, String recurrence) throws ParseException {
         DateValue repeatFrom;
         Date repeatFromDate = new Date();
 
         DateValue today = new DateValueImpl(repeatFromDate.getYear() + 1900,
                 repeatFromDate.getMonth() + 1, repeatFromDate.getDate());
         if(task.hasDueDate() && !task.getFlag(Task.FLAGS, Task.FLAG_REPEAT_AFTER_COMPLETION)) {
             repeatFromDate = new Date(task.getValue(Task.DUE_DATE));
             if(task.hasDueTime()) {
                 repeatFrom = new DateTimeValueImpl(repeatFromDate.getYear() + 1900,
                         repeatFromDate.getMonth() + 1, repeatFromDate.getDate(),
                         repeatFromDate.getHours(), repeatFromDate.getMinutes(), repeatFromDate.getSeconds());
             } else {
                 repeatFrom = new DateValueImpl(repeatFromDate.getYear() + 1900,
                         repeatFromDate.getMonth() + 1, repeatFromDate.getDate());
             }
         } else {
             repeatFrom = today;
         }
 
         // invoke the recurrence iterator
         long newDueDate;
         RRule rrule = new RRule(recurrence);
 
         // handle the iCalendar "byDay" field differently depending on if
         // we are weekly or otherwise
 
         List<WeekdayNum> byDay = null;
         if(rrule.getFreq() != Frequency.WEEKLY) {
             byDay = rrule.getByDay();
             rrule.setByDay(Collections.EMPTY_LIST);
         }
 
         if(rrule.getFreq() == Frequency.HOURLY) {
             newDueDate = task.createDueDate(Task.URGENCY_SPECIFIC_DAY_TIME,
                     repeatFromDate.getTime() + DateUtilities.ONE_HOUR * rrule.getInterval());
         } else {
             RecurrenceIterator iterator = RecurrenceIteratorFactory.createRecurrenceIterator(rrule,
                     repeatFrom, TimeZone.getDefault());
             DateValue nextDate = repeatFrom;
             if(repeatFrom.compareTo(today) < 0)
                 iterator.advanceTo(today);
 
             for(int i = 0; i < 10; i++) { // ten tries then we give up
                 if(!iterator.hasNext())
                     return -1;
                 nextDate = iterator.next();
                 if(nextDate.compareTo(repeatFrom) != 0)
                     break;
             }
             System.err.println("REPEAT started " + repeatFrom + ", ended " + nextDate); //$NON-NLS-1$ //$NON-NLS-2$
 
             if(nextDate instanceof DateTimeValueImpl) {
                 DateTimeValueImpl newDateTime = (DateTimeValueImpl)nextDate;
                 newDueDate = task.createDueDate(Task.URGENCY_SPECIFIC_DAY_TIME,
                         Date.UTC(newDateTime.year() - 1900, newDateTime.month() - 1,
                                 newDateTime.day(), newDateTime.hour(),
                                 newDateTime.minute(), newDateTime.second()));
             } else {
                 newDueDate = task.createDueDate(Task.URGENCY_SPECIFIC_DAY,
                         new Date(nextDate.year() - 1900, nextDate.month() - 1,
                                 nextDate.day()).getTime());
             }
         }
 
         // what we do with the by day information is to add days until
         // weekday equals one of this list
         if(byDay != null && byDay.size() > 0) {
             Date newDueDateDate = new Date(newDueDate);
             outer: for(int i = 0; i < 7; i++) {
                 int weekday = newDueDateDate.getDay();
                 for(WeekdayNum wdn : byDay)
                     if(wdn.wday.jsDayNum == weekday)
                         break outer;
                 newDueDateDate.setDate(newDueDateDate.getDate() + 1);
             }
             newDueDate = newDueDateDate.getTime();
         }
 
         return newDueDate;
     }
 
 }
