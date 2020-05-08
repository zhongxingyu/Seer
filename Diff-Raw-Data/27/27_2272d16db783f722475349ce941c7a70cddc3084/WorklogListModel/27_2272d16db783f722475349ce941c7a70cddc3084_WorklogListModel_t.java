 package ui.swing.mainScreen.tasks.worklog;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.commons.lang.time.DateUtils;
 
 import periods.Period;
 import tasks.TaskView;
 import tasks.tasks.TasksView;
 import basic.AlertImpl;
 import basic.HardwareClock;
 import basic.Subscriber;
 
 public class WorklogListModel {
 
     private final TasksView tasks;
     private final AlertImpl changeAlert;
     private int[] selectedWorklogs;
     private Calendar filter;
     private WorklogInterval filterType;
     private List<Worklog> lista;
     private WorklogFactory taskWorklogFactory;
 
     public WorklogListModel(TasksView tasks, WorklogFactory taskWorklogFactory, HardwareClock clock) {
         this.tasks = tasks;
         this.taskWorklogFactory = taskWorklogFactory;
         this.changeAlert = new AlertImpl();
         this.filter = Calendar.getInstance();
         this.selectedWorklogs = new int[0];
         this.lista = new ArrayList<Worklog>();
         setFilter(clock.getTime(), WorklogInterval.Day);
     }
 
     public List<Worklog> getWorklogList() {
         return lista;
     }
 
     void refrescate() {
         lista.clear();
         fillWorklogList();
         Collections.sort(lista);
     }
 
     private void fillWorklogList() {
         for (final TaskView task : tasks.tasks())
             createWorklogsForTask(task);
     }
 
     private void createWorklogsForTask(final TaskView task) {
         for (final Period period : task.periods())
             if (filtraPeriodo(period))
                 lista.add(taskWorklogFactory.newTaskWorklog(task, period, this));
     }
 
     private boolean filtraPeriodo(final Period period) {
 
         switch (filterType) {
         case Day:
             return period.getDay().equals(filter.getTime());
         case Month:
             Calendar periodDay = Calendar.getInstance();
             periodDay.setTime(period.getDay());
             periodDay.set(Calendar.DAY_OF_MONTH, 0);
             return periodDay.equals(filter);
         }
 
         throw new NotImplementedException();
     }
 
     public void setFilter(Date date, WorklogInterval interval) {
         resetFilter(date, interval);
         filterType = interval;
         refrescate();
         changeAlert.fire();
     }
 
     private void resetFilter(Date date, WorklogInterval interval) {
         filter.setTime(date);
         switch (interval) {
         case Day:
             resetDay(date);
             break;
         case Month:
             resetMonth(date);
             break;
         }
     }
 
     private void resetMonth(Date date) {
         filter.set(Calendar.DAY_OF_MONTH, 0);
         resetDay(date);
     }
 
     private void resetDay(Date date) {
         filter.set(Calendar.MILLISECOND, 0);
         filter.set(Calendar.SECOND, 0);
         filter.set(Calendar.MINUTE, 0);
         filter.set(Calendar.HOUR_OF_DAY, 0);
     }
 
     public double getDayTotalHours() {
         double totalHours = 0;
         for (final Worklog worklog : getWorklogList())
             totalHours += worklog.getMiliseconds();
         return totalHours / DateUtils.MILLIS_PER_HOUR;
     }
 
     public void addChangeSubscriber(Subscriber subscriber) {
         changeAlert.subscribe(subscriber);
     }
 
     public void fireChange() {
         changeAlert.fire();
     }
 
     public void setSelectedWorklogs(int[] selectedWorklogs) {
         this.selectedWorklogs = selectedWorklogs;
     }
 
    public synchronized void sendWorklog() {
         final List<Worklog> worklogs = selectedTaskWorklogs();
         for (final Worklog worklog : worklogs) {
             worklog.send();
             changeAlert.fire();
         }
     }
 
     private List<Worklog> selectedTaskWorklogs() {
         final List<Worklog> tasksPeriods = new LinkedList<Worklog>();
         for (final int i : selectedWorklogs) {
             final Worklog worklog = getWorklogList().get(i);
             if (worklog.canSend())
                 tasksPeriods.add(worklog);
         }
         return tasksPeriods;
     }
 
 }
