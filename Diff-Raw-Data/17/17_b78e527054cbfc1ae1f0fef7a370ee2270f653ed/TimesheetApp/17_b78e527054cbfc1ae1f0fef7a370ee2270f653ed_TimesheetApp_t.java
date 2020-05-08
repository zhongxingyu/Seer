 package com.uwusoft.timesheet;
 
 import java.lang.management.ManagementFactory;
 import java.lang.management.RuntimeMXBean;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 
 import com.uwusoft.timesheet.dialog.TimeDialog;
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.util.ExtensionManager;
 import com.uwusoft.timesheet.util.MessageBox;
 
 /**
  * This class controls all aspects of the application's execution
  */
 public class TimesheetApp implements IApplication {
 
     public static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
     
     public static final String WORKING_HOURS = "weekly.workinghours";
     public static final String DEFAULT_TASK = "task.default";
     public static final String DAILY_TASK = "task.daily";
     public static final String DAILY_TASK_TOTAL = "task.daily.total";
     public static final String LAST_TASK = "task.last";
     private static final String SYSTEM_SHUTDOWN = "system.shutdown";
     
 	/* (non-Javadoc)
 	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
 	 */
 	public Object start(IApplicationContext context) {
 		Display display = PlatformUI.createDisplay();
 		final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 		    public void run() {
 				preferenceStore.setValue(SYSTEM_SHUTDOWN, formatter.format(System.currentTimeMillis()));
 				if (!PlatformUI.isWorkbenchRunning()) return;
 		    	final IWorkbench workbench = PlatformUI.getWorkbench();
 		    	final Display display = workbench.getDisplay();
		    	if (!display.isDisposed()) {
					display.syncExec(new Runnable() {
						public void run() {
							if (!display.isDisposed()) {
								preferenceStore.setValue(SYSTEM_SHUTDOWN, formatter.format(System.currentTimeMillis()));
								workbench.close();
							}
 						}
					});
		    	}
 		    }
 		});
 		try {
 			RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
 			String startTime = formatter.format(mx.getStartTime());
 
 			String shutdownTime = preferenceStore.getString(SYSTEM_SHUTDOWN);
 			if (shutdownTime == "") shutdownTime = startTime;
 			else shutdownTime = formatter.format(formatter.parse(shutdownTime));
 
 			Calendar calDay = Calendar.getInstance();
 			Calendar calWeek = new GregorianCalendar();
 			int shutdownDay = 0;
 			int shutdownWeek = 0;
 			
 			Date startDate = formatter.parse(startTime);
 			calDay.setTime(startDate);
 			calWeek.setTime(startDate);
 			int startDay = calDay.get(Calendar.DAY_OF_YEAR);
 			int startWeek = calWeek.get(Calendar.WEEK_OF_YEAR);
 
 			Date shutdownDate = formatter.parse(shutdownTime);
 			calDay.setTime(shutdownDate);
 			shutdownDay = calDay.get(Calendar.DAY_OF_YEAR);
 			if (startDay != shutdownDay) { // don't automatically check in/out if computer is rebooted
 				StorageService storageService = new ExtensionManager<StorageService>(StorageService.SERVICE_ID)
 						.getService(preferenceStore.getString(StorageService.PROPERTY));
 				calWeek.setTime(shutdownDate);
 				shutdownWeek = calWeek.get(Calendar.WEEK_OF_YEAR);
 				if (preferenceStore.getString(LAST_TASK) != "") { // last task will be set to empty if a manual check out has occurred
 					// automatic check out
 					if (storageService == null) return IApplication.EXIT_OK;
 					TimeDialog timeDialog = new TimeDialog(display, "Check out at " + DateFormat.getDateInstance(DateFormat.SHORT).format(shutdownDate),
 							preferenceStore.getString(LAST_TASK), shutdownDate);
 					if (timeDialog.open() == Dialog.OK) {
 						storageService.createTaskEntry(timeDialog.getTime(), preferenceStore.getString(LAST_TASK));
 						storageService.storeLastDailyTotal();
 						if (preferenceStore.getString(DAILY_TASK) != "")
 							storageService.createTaskEntry(
 									timeDialog.getTime(), preferenceStore.getString(DAILY_TASK), preferenceStore.getString(DAILY_TASK_TOTAL));
 					}
 				}
 				// automatic check in
 				TimeDialog timeDialog = new TimeDialog(display, "Check in at "	+ DateFormat.getDateInstance(DateFormat.SHORT).format(startDate),
 						StorageService.CHECK_IN, startDate);
 				if (timeDialog.open() == Dialog.OK) {
 					if (startWeek != shutdownWeek)
 						storageService.storeLastWeekTotal(preferenceStore.getString(WORKING_HOURS)); // store Week and Overtime
 					storageService.createTaskEntry(timeDialog.getTime(), StorageService.CHECK_IN);
 					preferenceStore.setValue(LAST_TASK, preferenceStore.getString(DEFAULT_TASK));
 				}
 			}
 			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
 			if (returnCode == PlatformUI.RETURN_RESTART) {
 				return IApplication.EXIT_RESTART;
 			}
 		} catch (Exception e) {
 	        MessageBox.setMessage("Timesheet notification", e.getLocalizedMessage());
 		} finally {
 			display.dispose();
 		}
 		return IApplication.EXIT_OK;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.equinox.app.IApplication#stop()
 	 */
 	public void stop() {
 		if (!PlatformUI.isWorkbenchRunning())
 			return;
 		final IWorkbench workbench = PlatformUI.getWorkbench();
 		final Display display = workbench.getDisplay();
 		display.syncExec(new Runnable() {
 			public void run() {
 				if (!display.isDisposed())
 					workbench.close();
 			}
 		});
 	}
 }
