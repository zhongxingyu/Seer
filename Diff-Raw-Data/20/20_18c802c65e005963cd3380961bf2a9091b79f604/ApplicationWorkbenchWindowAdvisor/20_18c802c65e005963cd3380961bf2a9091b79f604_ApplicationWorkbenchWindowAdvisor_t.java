 package com.uwusoft.timesheet;
 
 import java.text.ParseException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.NotEnabledException;
 import org.eclipse.core.commands.NotHandledException;
 import org.eclipse.core.commands.ParameterizedCommand;
 import org.eclipse.core.commands.common.NotDefinedException;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Tray;
 import org.eclipse.swt.widgets.TrayItem;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.application.ActionBarAdvisor;
 import org.eclipse.ui.application.IActionBarConfigurer;
 import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
 import org.eclipse.ui.application.WorkbenchWindowAdvisor;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.menus.CommandContributionItem;
 import org.eclipse.ui.menus.CommandContributionItemParameter;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.services.ISourceProviderService;
 
 import com.uwusoft.timesheet.commands.SessionSourceProvider;
 import com.uwusoft.timesheet.commands.WholeDayTaskFactory;
 import com.uwusoft.timesheet.dialog.DateDialog;
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.model.TaskEntry;
 import com.uwusoft.timesheet.model.WholeDayTasks;
 import com.uwusoft.timesheet.util.BusinessDayUtil;
 import com.uwusoft.timesheet.util.ExtensionManager;
 import com.uwusoft.timesheet.util.MessageBox;
 
 public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
 
 	private IWorkbenchWindow window;
 	private TrayItem trayItem;
 	private Image trayImage;
 	private ApplicationActionBarAdvisor actionBarAdvisor;
 	private IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 
 	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
 		super(configurer);
 	}
 
 	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
 		actionBarAdvisor = new ApplicationActionBarAdvisor(configurer);
 		return actionBarAdvisor;
 	}
 
 	public void preWindowOpen() {
 		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
 		configurer.setInitialSize(new Point(400, 300));
 		configurer.setShowCoolBar(true);
 		configurer.setShowProgressIndicator(true);
 		configurer.setTitle("Timesheet");
 	}
 	
 	public void postWindowOpen() {
 		super.postWindowOpen();
 		window = getWindowConfigurer().getWindow();
 		window.getShell().addShellListener(new ShellAdapter() {
 			public void shellIconified(ShellEvent e) { // If the window is minimized hide the window
 				preWindowShellClose();
 			}
 		});
		trayItem = initTaskItem();
		// Some OS might not support tray items
		if (trayItem != null) {
			window.getShell().setVisible(false);
			hookPopupMenu();
		}
 		StorageService storageService = new ExtensionManager<StorageService>(
 				StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
 		
 		Date startDate = TimesheetApp.startDate;
 		Date shutdownDate = startDate;
 		try {
 			String startTime = preferenceStore.getString(TimesheetApp.SYSTEM_START);
 			if (!StringUtils.isEmpty(startTime)) {
 				preferenceStore.setToDefault(TimesheetApp.SYSTEM_START);
 				startDate = StorageService.formatter.parse(startTime);				
 			}
 
 			String shutdownTime = preferenceStore.getString(TimesheetApp.SYSTEM_SHUTDOWN);
 			if (StringUtils.isEmpty(shutdownTime) || StorageService.formatter.parse(shutdownTime).before(storageService.getLastTaskEntryDate()))
 				shutdownDate = storageService.getLastTaskEntryDate();
 			else
 				shutdownDate = StorageService.formatter.parse(shutdownTime);				
 		} catch (ParseException e) {
 			MessageBox.setError("Shutdown date", e.getMessage());
 		}
 	
 		int startDay = getDay(startDate);
 		int startWeek = getWeek(startDate);
 
 		int shutdownDay = getDay(shutdownDate);
 		int shutdownWeek = getWeek(shutdownDate);
 
 		IHandlerService handlerService = (IHandlerService) window.getService(IHandlerService.class);
 		ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
 				
 		if (startDay != shutdownDay && storageService.getLastTask() != null) { // don't automatically check in/out if program is restarted
 			Map<String, String> parameters = new HashMap<String, String>();
 			parameters.put("Timesheet.commands.shutdownTime", StorageService.formatter.format(shutdownDate));
 			try { // automatic check out
 				handlerService.executeCommand(ParameterizedCommand.generateCommand(commandService.getCommand("Timesheet.checkout"),	parameters), null);
 			} catch (ExecutionException ex) {
 				MessageBox.setError("Automatic check out", ex.getMessage() + "\n(" + parameters + ")");
 			} catch (NotDefinedException ex) {
 				MessageBox.setError("Automatic check out", ex.getMessage() + "\n(" + parameters + ")");
 			} catch (NotEnabledException ex) {
 				MessageBox.setError("Automatic check out", ex.getMessage() + "\n(" + parameters + ")");
 			} catch (NotHandledException ex) {
 				MessageBox.setError("Automatic check out", ex.getMessage() + "\n(" + parameters + ")");
 			}
 		}
 		if (storageService.getLastTask() == null) { // automatic check in								 
 			Date end = BusinessDayUtil.getNextBusinessDay(shutdownDate,	true); // create missing holidays and handle week change
 			Date start = BusinessDayUtil.getLastBusinessDay(startDate);
 			while (end.before(start)) { // create missing whole day tasks until last business day
 				DateDialog dateDialog = new DateDialog(Display.getDefault(), "Select missing whole day task",
 						preferenceStore.getString(WholeDayTasks.wholeDayTasks[0]), end);
 				if (dateDialog.open() == Dialog.OK) {
 					do {
 						storageService.createTaskEntry(new TaskEntry(end, TimesheetApp.createTask(dateDialog.getTask()),
 								WholeDayTasks.getInstance().getTotal(), true));
 					} while (!(end = BusinessDayUtil.getNextBusinessDay(end, true)).after(dateDialog.getTime()));
 				}
 			}
 
 			Map<String, String> parameters = new HashMap<String, String>();
 			parameters.put("Timesheet.commands.startTime", StorageService.formatter.format(startDate));
 			// parameters.put("Timesheet.commands.storeWeekTotal", Boolean.toString(startWeek != shutdownWeek));
 			try {
 				handlerService.executeCommand(ParameterizedCommand.generateCommand(commandService.getCommand("Timesheet.checkin"), parameters), null);
 				for (int week = shutdownWeek; week < startWeek; week++) {
 					parameters.clear();
 					parameters.put("Timesheet.commands.weekNum", Integer.toString(week));
 					handlerService.executeCommand(ParameterizedCommand.generateCommand(commandService.getCommand("Timesheet.submit"), parameters), null);
 				}
 			} catch (ExecutionException ex) {
 				MessageBox.setError("Automatic check in", ex.getMessage() + "\n(" + parameters + ")");
 			} catch (NotDefinedException ex) {
 				MessageBox.setError("Automatic check in", ex.getMessage() + "\n(" + parameters + ")");
 			} catch (NotEnabledException ex) {
 				MessageBox.setError("Automatic check in", ex.getMessage() + "\n(" + parameters + ")");
 			} catch (NotHandledException ex) {
 				MessageBox.setError("Automatic check in", ex.getMessage() + "\n(" + parameters + ")");
 			}
 		}
 	}
 
 	private int getDay(Date date) {
 		Calendar calDay = Calendar.getInstance();
 		calDay.setTime(date);
 		return calDay.get(Calendar.DAY_OF_YEAR);
 	}
 
 	private int getWeek(Date date) {
 		Calendar calWeek = new GregorianCalendar();
 		calWeek.setTime(date);
 		return calWeek.get(Calendar.WEEK_OF_YEAR);
 	}
 
 	public boolean preWindowShellClose() {
 		window.getShell().setVisible(false);
 		return false; // if window close button pressed only minimize to system tray (don't exit the program)
 	}
 	
 	private void hookPopupMenu() {
 		trayItem.addListener(SWT.MenuDetect, new Listener() {
 			public void handleEvent(Event event) {
 				MenuManager trayMenu = new MenuManager();
                 
 				ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(ISourceProviderService.class);
 				SessionSourceProvider commandStateService = (SessionSourceProvider) sourceProviderService.getSourceProvider(SessionSourceProvider.SESSION_STATE);
 				String sessionState = (String) commandStateService.getCurrentState().get(SessionSourceProvider.SESSION_STATE);
 				String breakState = (String) commandStateService.getCurrentState().get(SessionSourceProvider.BREAK_STATE);
 				
 				if (SessionSourceProvider.DISABLED.equals(sessionState)) {					
                     CommandContributionItemParameter p = new CommandContributionItemParameter(window, null, "Timesheet.checkin", CommandContributionItem.STYLE_PUSH);
                     p.icon = AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/check_in_16.png");
                     trayMenu.add(new CommandContributionItem(p));
                 }
                 else {
                 	CommandContributionItemParameter p = new CommandContributionItemParameter(window, null, "Timesheet.changeTask", CommandContributionItem.STYLE_PUSH);
                     p.icon = AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/task_16.png");
                     trayMenu.add(new CommandContributionItem(p));
             				
                     if (SessionSourceProvider.DISABLED.equals(breakState)) {
                         p = new CommandContributionItemParameter(window, null, "Timesheet.setBreak", CommandContributionItem.STYLE_PUSH);
                         p.icon = AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/pause_16.png");
                         trayMenu.add(new CommandContributionItem(p));
                     }
 
                     MenuManager wholeDayTask = new MenuManager("Set whole day task",
                     		AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/day_16.png"), "Timesheet.wholeDayTask");
                     
                     new WholeDayTaskFactory(wholeDayTask).createContributionItems(window, null);
 
                     trayMenu.add(wholeDayTask);
 
                     p = new CommandContributionItemParameter(window, null, "Timesheet.checkout", CommandContributionItem.STYLE_PUSH);
                     p.icon = AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/check_out_16.png");
                     trayMenu.add(new CommandContributionItem(p));
                 }
                 trayMenu.add(new Separator());
                 
                 /*trayMenu.add(new CommandContributionItem(
         				new CommandContributionItemParameter(window, null, "Timesheet.importTasks", CommandContributionItem.STYLE_PUSH)));*/
 
 				CommandContributionItemParameter p = new CommandContributionItemParameter(window, null, "Timesheet.submit", CommandContributionItem.STYLE_PUSH);
                 p.icon = AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/signs_16.png");
                 trayMenu.add(new CommandContributionItem(p));
                 
                 trayMenu.add(new Separator());
 			    
                 Menu menu = trayMenu.createContextMenu(window.getShell());
 			    actionBarAdvisor.fillTrayItem(trayMenu);
 			    menu.setVisible(true);
 			}
 		});
 	}
 
 	private TrayItem initTaskItem() {
 		final Tray tray = window.getShell().getDisplay().getSystemTray();
 		final TrayItem trayItem = new TrayItem(tray, SWT.NONE);
 		ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/clock.png");
 		if (descriptor != null) {
 			trayImage = descriptor.createImage();
 			trayItem.setImage(trayImage);
 		}
 		trayItem.setToolTipText("Timesheet");
 		trayItem.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				Shell shell = window.getShell();
 				shell.setVisible(true);
 				shell.setActive();
 				shell.setFocus();
 				shell.setMinimized(false);
 				//trayItem.dispose();
 			}
 		});
 		return trayItem;
 	}
 
 	// We need to clean-up after ourself
 	@Override
 	public void dispose() {
 		if (trayImage != null) {
 			trayImage.dispose();
 		}
 		if (trayItem != null) {
 			trayItem.dispose();
 		}
 	}
 }
