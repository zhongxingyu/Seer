 package com.od.jtimeseries.ui.download.panel;
 
 import com.od.jtimeseries.context.TimeSeriesContext;
 import com.od.jtimeseries.net.udp.TimeSeriesServer;
 import com.od.jtimeseries.util.logging.LogMethods;
 import com.od.jtimeseries.util.logging.LogUtils;
 import swingcommand.BackgroundTask;
 import swingcommand.SwingCommand;
 import swingcommand.Task;
 import swingcommand.TaskListenerAdapter;
 
 /**
 * Created by IntelliJ IDEA.
 * User: Nick
 * Date: 28/12/10
 * Time: 10:47
 * To change this template use File | Settings | File Templates.
 */
 public class LoadSeriesFromServerCommand extends SwingCommand<TimeSeriesServer, String> {
 
     private static LogMethods logMethods = LogUtils.getLogMethods(LoadSeriesFromServerCommand.class);
     private TimeSeriesContext destinationContext;
 
     public LoadSeriesFromServerCommand(TimeSeriesContext destinationContext) {
         this.destinationContext = destinationContext;
         addTaskListener(
             new TaskListenerAdapter<String>() {
                 public void error(Task task, Throwable error) {
                     TimeSeriesServer s = (TimeSeriesServer)task.getParameters();
                     s.setConnectionFailed(true);
                    logMethods.logError("Failed to load series from server " + task.getParameters(), error);
                 }
 
                 public void success(Task task) {
                     TimeSeriesServer s = (TimeSeriesServer)task.getParameters();
                     s.setConnectionFailed(false);
                     logMethods.logInfo("Loaded series from server " + task.getParameters());
                 }
             }
         );
     }
 
     protected Task<TimeSeriesServer, String> createTask() {
         return new BackgroundTask<TimeSeriesServer, String>() {
 
             protected void doInBackground() throws Exception {
                 TimeSeriesServer server = getParameters();
 
                 AddSeriesFromServerTask t = new AddSeriesFromServerTask(
                     destinationContext,
                     server
                 );
                 t.call();
             }
 
             protected void doInEventThread() {
             }
         };
     }
 }
