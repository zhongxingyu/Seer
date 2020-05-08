 package com.uwusoft.timesheet.util;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.commands.ParameterizedCommand;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.services.ISourceProviderService;
 
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.TimesheetApp;
 import com.uwusoft.timesheet.commands.SessionSourceProvider;
 import com.uwusoft.timesheet.dialog.AllDayTaskDateDialog;
 import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.model.AllDayTasks;
 import com.uwusoft.timesheet.model.TaskEntry;
 
 public class AutomaticCheckoutCheckinUtil {
 	public static void execute() {
 		LocalStorageService storageService = LocalStorageService.getInstance();
 		
 		Date startDate = TimesheetApp.startDate;
 		Date shutdownDate = TimesheetApp.shutDownDate;
     	Date lastDate = storageService.getLastTaskEntryDate();
     	if (lastDate == null) lastDate = BusinessDayUtil.getPreviousBusinessDay(new Date());
 		if (shutdownDate.before(lastDate))
 			shutdownDate = lastDate;
 	
 		int startDay = getDay(startDate);
 		final int startWeek = getWeek(startDate);
 	
 		int shutdownDay = getDay(shutdownDate);
 		final int shutdownWeek = getWeek(shutdownDate);
 	
 		final IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
 		final ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
 		ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
 		SessionSourceProvider commandStateService = (SessionSourceProvider) sourceProviderService.getSourceProvider(SessionSourceProvider.SESSION_STATE);
 
 		if (startDay != shutdownDay && storageService.getLastTask() != null) { // don't automatically check in/out if program is restarted
 			Map<String, String> parameters = new HashMap<String, String>();
 			parameters.put("Timesheet.commands.shutdownTime", StorageService.formatter.format(shutdownDate));
 			try { // automatic check out
 				handlerService.executeCommand(ParameterizedCommand.generateCommand(commandService.getCommand("Timesheet.checkout"),	parameters), null);
 			} catch (Exception ex) {
 				MessageBox.setError("Automatic check out", ex.getMessage() + "\n(" + parameters + ")");
 			}
 		}		
 		if (storageService.getLastTask() == null) { // automatic check in								 
 			commandStateService.setEnabled(false);
 			
 			Date end = BusinessDayUtil.getNextBusinessDay(shutdownDate,	true); // create missing holidays and handle week change
 			Date start = BusinessDayUtil.getPreviousBusinessDay(startDate);
			while (end.before(start)) { // create missing whole day tasks until last business day
 				AllDayTaskDateDialog dateDialog = new AllDayTaskDateDialog(Display.getDefault(), "Select missing all day task",
 						Activator.getDefault().getPreferenceStore().getString(AllDayTasks.allDayTasks[0]), end);
 				if (dateDialog.open() == Dialog.OK) {
 					do {
 						storageService.createTaskEntry(new TaskEntry(end, TimesheetApp.createTask(dateDialog.getTask()),
 								AllDayTasks.getInstance().getTotal(), true));
 					} while (!(end = BusinessDayUtil.getNextBusinessDay(end, true)).after(dateDialog.getTo()));
 				}
 			}
 	
 			final Map<String, String> parameters = new HashMap<String, String>();
 			parameters.put("Timesheet.commands.startTime", StorageService.formatter.format(startDate));
 			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 				public void run() {
 					try {
 						handlerService.executeCommand(ParameterizedCommand.generateCommand(commandService.getCommand("Timesheet.checkin"), parameters), null);
 						for (int week = shutdownWeek; week < startWeek; week++) {
 							parameters.clear();
 							parameters.put("Timesheet.commands.weekNum", Integer.toString(week));
 							handlerService.executeCommand(ParameterizedCommand.generateCommand(commandService.getCommand("Timesheet.submit"), parameters), null);
 						}
 					} catch (Exception ex) {
 						MessageBox.setError("Automatic check in", ex.getMessage() + "\n(" + parameters + ")");
 					}
 				}					
 			});
 		}
 		else {
 			commandStateService.setEnabled(true);
 			final Map<String, String> parameters = new HashMap<String, String>();
 			parameters.put("Timesheet.commands.changeTime", StorageService.formatter.format(startDate));
 			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 				public void run() {
 					try {
 						handlerService.executeCommand(ParameterizedCommand.generateCommand(commandService.getCommand("Timesheet.changeTask"), parameters), null);
 					} catch (Exception e) {
 						MessageBox.setError("Automatic change task", e.getMessage() + "\n(" + parameters + ")");
 					}
 				}					
 			});
 		}
 		commandStateService.setBreak(false);			
 	}
 
 	private static int getDay(Date date) {
 		Calendar calDay = Calendar.getInstance();
 		calDay.setTime(date);
 		return calDay.get(Calendar.DAY_OF_YEAR);
 	}
 
 	private static int getWeek(Date date) {
 		Calendar calWeek = new GregorianCalendar();
 		calWeek.setTime(date);
 		return calWeek.get(Calendar.WEEK_OF_YEAR);
 	}
 }
