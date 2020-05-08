 package com.uwusoft.timesheet.commands;
 
import java.util.Calendar;
 import java.util.Date;
 
import org.apache.commons.lang3.time.DateUtils;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.TimesheetApp;
 import com.uwusoft.timesheet.dialog.AllDayTaskListDialog;
 import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
 import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
 import com.uwusoft.timesheet.model.AllDayTaskEntry;
 import com.uwusoft.timesheet.model.AllDayTasks;
 import com.uwusoft.timesheet.model.Project;
 import com.uwusoft.timesheet.model.Task;
 
 public class AllDayTaskHandler extends AbstractHandler {
 
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
		
 		AllDayTasks allDayTasks = AllDayTasks.getInstance();
		Date startDate = allDayTasks.getNextBegin(new Date());
 		String task = event.getParameter("Timesheet.commands.task");
 		AllDayTaskListDialog dateDialog = new AllDayTaskListDialog(new Shell(Display.getDefault(), SWT.NO_TRIM | SWT.ON_TOP),
 				TimesheetApp.createTask(task), startDate);
 		if (dateDialog.open() == Dialog.OK) {
 			String system = TimesheetApp.getDescriptiveName(Activator.getDefault().getPreferenceStore().getString(AllDayTaskService.PROPERTY),
 					AllDayTaskService.SERVICE_NAME);
			AllDayTaskEntry entry = new AllDayTaskEntry(DateUtils.truncate(dateDialog.getFrom(), Calendar.DATE),
					DateUtils.truncate(dateDialog.getTo(), Calendar.DATE),
 					new Task(dateDialog.getTask(), new Project(dateDialog.getProject(), dateDialog.getSystem())));
 			if (!system.equals(dateDialog.getSystem()))
 				entry.setSyncStatus(true);
			LocalStorageService.getInstance().createAllDayTaskEntry(entry, true);
			LocalStorageService.getInstance().synchronizeAllDayTaskEntries();
 		}
 		return null;
 	}
 }
