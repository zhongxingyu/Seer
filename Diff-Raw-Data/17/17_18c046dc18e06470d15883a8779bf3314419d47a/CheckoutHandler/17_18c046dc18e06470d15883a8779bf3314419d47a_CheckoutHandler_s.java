 package com.uwusoft.timesheet.commands;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.Date;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.widgets.Display;
 
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.TimesheetApp;
 import com.uwusoft.timesheet.dialog.TimeDialog;
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.model.TaskEntry;
 import com.uwusoft.timesheet.model.WholeDayTasks;
 import com.uwusoft.timesheet.util.ExtensionManager;
 import com.uwusoft.timesheet.util.MessageBox;
 
 public class CheckoutHandler extends AbstractHandler {
 
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
         String shutdownTime = event.getParameter("Timesheet.commands.shutdownTime");
         if (shutdownTime == null) return null;
 		try {
 			Date shutdownDate = StorageService.formatter.parse(shutdownTime);
 			StorageService storageService = new ExtensionManager<StorageService>(StorageService.SERVICE_ID)
 					.getService(preferenceStore.getString(StorageService.PROPERTY));
 			TaskEntry lastTask = storageService.getLastTask();
 			TimeDialog timeDialog = new TimeDialog(Display.getDefault(), "Check out at " + DateFormat.getDateInstance(DateFormat.SHORT).format(shutdownDate),
 					lastTask.display(), shutdownDate);
 			if (timeDialog.open() == Dialog.OK) {
 				storageService.updateTaskEntry(timeDialog.getTime(), lastTask.getId(), true);
 				storageService.storeLastDailyTotal();
				WholeDayTasks.getInstance().createTaskEntries();
 				preferenceStore.setValue(TimesheetApp.SYSTEM_SHUTDOWN, preferenceStore.getString(TimesheetApp.SYSTEM_START));
 			}
 		} catch (ParseException e) {
 			MessageBox.setError("Check out", e.getLocalizedMessage());
 		}
 		return null;
 	}
 
 }
