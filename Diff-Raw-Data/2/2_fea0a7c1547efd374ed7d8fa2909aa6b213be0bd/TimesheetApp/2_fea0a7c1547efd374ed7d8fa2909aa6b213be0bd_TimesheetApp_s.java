 package com.uwusoft.timesheet;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.lang.management.ManagementFactory;
 import java.lang.management.RuntimeMXBean;
 import java.net.URISyntaxException;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileLock;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.persistence.config.SystemProperties;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 
 import com.uwusoft.timesheet.extensionpoint.SubmissionService;
 import com.uwusoft.timesheet.model.Project;
 import com.uwusoft.timesheet.model.Task;
 import com.uwusoft.timesheet.util.MessageBox;
 
 /**
  * This class controls all aspects of the application's execution
  */
 public class TimesheetApp implements IApplication {
 	private static final String PERSISTENCE_UNIT_NAME = "timesheet";
 	public static EntityManagerFactory factory;
 
 	public static final String WORKING_HOURS = "weekly.workinghours";
 	public static final String NON_WORKING_DAYS = "weekly.nonworkingdays";
     public static final String HOLIDAY_TASK = "task.holiday";
     public static final String VACATION_TASK = "task.vacation";
     public static final String SICK_TASK = "task.sick";
     public static final String TIL_TASK = "task.til";
     public static final String DEFAULT_TASK = "task.default";
     public static final String DAILY_TASK = "task.daily";
     public static final String DAILY_TASK_TOTAL = "task.daily.total";
     public static final String SYSTEM_SHUTDOWN = "system.shutdown";
     public static final String SYSTEM_START= "system.start";
     public static Date startDate;
     
 	/* (non-Javadoc)
 	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
 	 */
 	public Object start(IApplicationContext context) {
 		String settingsPath = Activator.getDefault().getStateLocation().toPortableString()
 				.replaceFirst(".metadata/.plugins/.*", ".metadata/.plugins/org.eclipse.core.runtime/.settings/"); // how to get path of settings?
 		// see http://stackoverflow.com/a/4194224:
 		try {
 			final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
 			final File currentJar = new File(SystemShutdownTimeCaptureService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
 			final List<String> command = new ArrayList<String>();
 			command.add(javaBin);
 			command.add("-jar");
 			command.add(currentJar.getPath());
 			command.add(settingsPath);
 			command.add(Activator.PLUGIN_ID + ".prefs");
 
 			final ProcessBuilder builder = new ProcessBuilder(command);
 			builder.start();
 		} catch (IOException e) {
 			MessageBox.setError("Couldn't start shutdown service", e.getLocalizedMessage());
 		} catch (URISyntaxException e) {
 			MessageBox.setError("Couldn't start shutdown service", e.getLocalizedMessage());
 		}
 		
 		try {
 			RandomAccessFile lockFile = new RandomAccessFile(new File(settingsPath + "timesheet.lck"), "rw");
 	        FileChannel channel = lockFile.getChannel();
 	        FileLock lock = channel.tryLock();
 	        if (lock == null) {
	        	MessageBox.setError("Error", "Another instance of " + Activator.PLUGIN_ID + " is already running.");
 	        	stop();
 	            System.exit(0);
 	        }
 		} catch (IOException e) {
 		}
 		
         System.setProperty(SystemProperties.ARCHIVE_FACTORY, MyArchiveFactoryImpl.class.getName()); // see http://stackoverflow.com/a/7982008
 		Map<String, Object> configOverrides = new HashMap<String, Object>();
 		configOverrides.put("javax.persistence.jdbc.url",
 				"jdbc:derby:" + System.getProperty("user.home") + "/.eclipse/databases/timesheet;create=true");
 		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, configOverrides);
 		
 		RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
 		startDate = new Date(mx.getStartTime());
 
 		Display display = PlatformUI.createDisplay();
 		
 		int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
 		if (returnCode == PlatformUI.RETURN_RESTART) {
 			return IApplication.EXIT_RESTART;
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
 
 	public static Map<String, String> getSubmissionSystems() {
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();		
 		String[] systems = preferenceStore.getString(SubmissionService.PROPERTY).split(SubmissionService.separator);
 		Map<String,String> submissionSystems = new HashMap<String, String>();
 		for (String system : systems) {
 			if (!StringUtils.isEmpty(system)) {
 				String descriptiveName = Character.toUpperCase(system.toCharArray()[system.lastIndexOf('.') + 1])
 						+ system.substring(system.lastIndexOf('.') + 2, system.indexOf(SubmissionService.SERVICE_NAME));
 				submissionSystems.put(descriptiveName, system);
 			}
 		}
 		return submissionSystems;
 	}
 	
 	public static String getTaskName(String propertyName) {
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();		
 		String[] task = preferenceStore.getString(propertyName).split(SubmissionService.separator);
 		if (task.length > 2) {
 			return task[0] + " (" + task[1] + ")" + "\nSystem: " + task[2]; 
 		}
 		else return preferenceStore.getString(propertyName);
 	}
 	
 	public static Task createTask(String propertyName) {
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();		
 		String[] task = preferenceStore.getString(propertyName).split(SubmissionService.separator);
 		if (task.length < 2) task = propertyName.split(SubmissionService.separator);
 		Project project = new Project();
 		if (task.length > 2) {
 			project.setName(task[1]);
 			project.setSystem(task[2]);
 		}
 		return new Task(task[0], project);
 	}
 }
