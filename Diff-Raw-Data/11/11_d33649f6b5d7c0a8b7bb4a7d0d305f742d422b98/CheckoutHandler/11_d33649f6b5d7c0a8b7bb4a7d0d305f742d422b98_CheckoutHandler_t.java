 package com.uwusoft.timesheet.commands;
 
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.util.Date;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.services.ISourceProviderService;
 
 import com.uwusoft.timesheet.dialog.TimeDialog;
 import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.model.AllDayTasks;
 import com.uwusoft.timesheet.model.TaskEntry;
 import com.uwusoft.timesheet.util.MessageBox;
 
 public class CheckoutHandler extends AbstractHandler {
 
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		String shutdownTime = event.getParameter("Timesheet.commands.shutdownTime");
 		Date shutdownDate;
 		if (shutdownTime == null) shutdownDate = new Date();
 		else
 			try {
 				shutdownDate = StorageService.formatter.parse(shutdownTime);
 			} catch (ParseException e) {
 				MessageBox.setError("Check out", e.getLocalizedMessage());
 				return null;
 			}
 		LocalStorageService storageService = LocalStorageService.getInstance();
 		TaskEntry lastTask = storageService.getLastTask();
 		TimeDialog timeDialog = new TimeDialog(Display.getDefault(), "Check out", lastTask.getTask().display(), shutdownDate);
 		if (timeDialog.open() == Dialog.OK) {
			lastTask.setDateTime(new Timestamp(timeDialog.getTime().getTime()));
 			storageService.updateTaskEntry(lastTask);
			storageService.synchronize();
 			AllDayTasks.getInstance().createTaskEntries(timeDialog.getTime());
			ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
 			SessionSourceProvider commandStateService = (SessionSourceProvider) sourceProviderService.getSourceProvider(SessionSourceProvider.SESSION_STATE);
 			commandStateService.setEnabled(false);
			commandStateService.setBreak(false);
 		}
 		return null;
 	}
 }
