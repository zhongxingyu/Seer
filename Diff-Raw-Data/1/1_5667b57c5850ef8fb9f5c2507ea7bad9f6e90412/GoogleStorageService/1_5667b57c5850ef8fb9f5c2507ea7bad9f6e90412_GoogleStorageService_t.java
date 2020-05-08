 package com.uwusoft.timesheet.googlestorage;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.jar.JarFile;
 import java.util.zip.ZipEntry;
 
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.core.commands.ParameterizedCommand;
 import org.eclipse.core.commands.common.EventManager;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.handlers.IHandlerService;
 
 import com.google.gdata.client.docs.DocsService;
 import com.google.gdata.client.spreadsheet.FeedURLFactory;
 import com.google.gdata.client.spreadsheet.ListQuery;
 import com.google.gdata.client.spreadsheet.SpreadsheetService;
 import com.google.gdata.data.PlainTextConstruct;
 import com.google.gdata.data.docs.DocumentListEntry;
 import com.google.gdata.data.media.MediaByteArraySource;
 import com.google.gdata.data.spreadsheet.CellEntry;
 import com.google.gdata.data.spreadsheet.CellFeed;
 import com.google.gdata.data.spreadsheet.CustomElementCollection;
 import com.google.gdata.data.spreadsheet.ListEntry;
 import com.google.gdata.data.spreadsheet.ListFeed;
 import com.google.gdata.data.spreadsheet.WorksheetEntry;
 import com.google.gdata.data.spreadsheet.WorksheetFeed;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.TimesheetApp;
 import com.uwusoft.timesheet.dialog.LoginDialog;
 import com.uwusoft.timesheet.dialog.PreferencesDialog;
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.extensionpoint.SubmissionService;
 import com.uwusoft.timesheet.extensionpoint.model.DailySubmissionEntry;
 import com.uwusoft.timesheet.extensionpoint.model.SubmissionEntry;
 import com.uwusoft.timesheet.model.Task;
 import com.uwusoft.timesheet.model.TaskEntry;
 import com.uwusoft.timesheet.submission.model.SubmissionProject;
 import com.uwusoft.timesheet.submission.model.SubmissionTask;
 import com.uwusoft.timesheet.util.AutomaticCheckoutCheckinUtil;
 import com.uwusoft.timesheet.util.DesktopUtil;
 import com.uwusoft.timesheet.util.ExtensionManager;
 import com.uwusoft.timesheet.util.ImportTasksUtil;
 import com.uwusoft.timesheet.util.MessageBox;
 import com.uwusoft.timesheet.util.SecurePreferencesManager;
 
 /**
  * storage service for Google Docs spreadsheet
  *
  * @author Uta Wunderlich
  * @version $Revision: $, $Date: Aug 15, 2011
  * @since Aug 15, 2011
  */
 public class GoogleStorageService extends EventManager implements StorageService {
 
 	public static final String PREFIX = "google.";
     public static final String SPREADSHEET_KEY="google.spreadsheet.key";
 
     private static final String dateFormat = "MM/dd/yyyy";
     private static final String timeFormat = "HH:mm";
     
     private static final String SUBMISSION_STATUS_TRUE = "Submitted";
     
     private String spreadsheetKey;
     private SpreadsheetService service;
     private DocsService docsService;
     private FeedURLFactory factory;
     private URL listFeedUrl;
 	private Map<String, Integer> headingIndex;
     private List<WorksheetEntry> worksheets = new ArrayList<WorksheetEntry>();
     private WorksheetEntry defaultWorksheet;
     private Map<String,String> submissionSystems;
     private String message;
     private String title = "Google Storage Service";
     private ILog logger;
     
     public GoogleStorageService() throws CoreException {
         logger = Activator.getDefault().getLog();
         service = new SpreadsheetService("Timesheet");
         docsService = new DocsService("Timesheet");
         service.setProtocolVersion(SpreadsheetService.Versions.V1);
         service.useSsl();
         docsService.useSsl();
         if (StringUtils.isEmpty(Activator.getDefault().getPreferenceStore().getString(PREFIX + USERNAME))) {
         	PreferencesDialog preferencesDialog;
         	do
         		preferencesDialog = new PreferencesDialog(Display.getDefault(), "com.uwusoft.timesheet.googlestorage.GooglePreferencePage");
         	while (preferencesDialog.open() != Dialog.OK);
         }
         boolean lastSuccess = true;
         do lastSuccess = authenticate(lastSuccess);
        	while (!lastSuccess);
 		factory = FeedURLFactory.getDefault();
 		headingIndex = new LinkedHashMap<String, Integer>();
 		submissionSystems = TimesheetApp.getSubmissionSystems();
         reload();
     }
     
     private boolean authenticate(boolean lastSuccess) throws CoreException {
         try {
 			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 			SecurePreferencesManager secureProps = new SecurePreferencesManager("Google");
 	    	String userName = preferenceStore.getString(PREFIX + USERNAME);
 	    	String password = secureProps.getProperty(PREFIX + PASSWORD);
 	    	if (lastSuccess && !StringUtils.isEmpty(userName) && !StringUtils.isEmpty(password)) {
 	        	service.setUserCredentials(userName, password);
 	        	docsService.setUserCredentials(userName, password);
 	            spreadsheetKey = preferenceStore.getString(SPREADSHEET_KEY);
 	        	return true;
 	    	}
 	    	
 	    	Display display = Display.getDefault();
 	    	LoginDialog loginDialog = new LoginDialog(display, "Google Log in", message, userName, password);
 			if (loginDialog.open() == Dialog.OK) {
 		    	userName = loginDialog.getUser();
 		    	password = loginDialog.getPassword();
 	        	service.setUserCredentials(userName, password);
 	        	docsService.setUserCredentials(userName, password);
 	        	preferenceStore.setValue(PREFIX + USERNAME, userName);
 	        	if (loginDialog.isStorePassword())
 	        		secureProps.storeProperty(PREFIX + PASSWORD, password);
 	        	else
 	        		secureProps.removeProperty(PREFIX + PASSWORD);
 	            spreadsheetKey = preferenceStore.getString(SPREADSHEET_KEY);
 	        	return true;
 			}
 		} catch (AuthenticationException e) {
 			message = e.getMessage();
 			return false;
 		}
         throw new CoreException(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, null));
    }
 
     public void reload() {
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		String oldSpreadsheetKey = spreadsheetKey;
         spreadsheetKey = preferenceStore.getString(SPREADSHEET_KEY);
 		try {
 	    	if (StringUtils.isEmpty(spreadsheetKey)) handleYearChange(oldSpreadsheetKey, 0);
 	    	else reloadHeadingIndex();
 			if (!reloadWorksheets()) return;
     		if (!StringUtils.isEmpty(oldSpreadsheetKey) && !spreadsheetKey.equals(oldSpreadsheetKey)) {
     			if (!headingIndex.keySet().containsAll(Arrays.asList(new String[] {StorageService.DATE, StorageService.TIME, StorageService.TOTAL,
     					StorageService.DAILY_TOTAL, StorageService.WEEKLY_TOTAL, StorageService.WEEK, StorageService.TASK, StorageService.PROJECT,
     					StorageService.COMMENT, StorageService.OVERTIME, StorageService.SUBMISSION_STATUS, StorageService.ID}))) {
     				MessageBox.setError(title, "Please select valid spreadsheet!");
     				preferenceStore.setValue(SPREADSHEET_KEY, oldSpreadsheetKey);
     				return;
     			}
     			AutomaticCheckoutCheckinUtil.execute();
     	        Date lastTaskEntryDate = getLastTaskEntryDate();
     			Calendar cal = new GregorianCalendar();
     			cal.setTime(new Date());
     	        if (lastTaskEntryDate != null)
     	        	cal.setTime(lastTaskEntryDate);
     			firePropertyChangeEvent(new PropertyChangeEvent(this, PROPERTY_WEEK, null, cal.get(Calendar.WEEK_OF_YEAR)));
     		}
 		} catch (MalformedURLException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
     }
 
 	public void handleYearChange(int lastWeek) {
 		handleYearChange(Activator.getDefault().getPreferenceStore().getString(SPREADSHEET_KEY), lastWeek);
 	}
     
     private void handleYearChange(String copySpreadsheetKey, int lastWeek) {
 		try {
 			if (lastWeek != 0 && !StringUtils.isEmpty(copySpreadsheetKey)) {
 				// First submit rest of last year
 				spreadsheetKey = copySpreadsheetKey;
 				openUrl(StorageService.OPEN_BROWSER_CHANGE_TASK);
 				IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
 				ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
 				Map<String, String> parameters = new HashMap<String, String>();
 				parameters.put("Timesheet.commands.weekNum", Integer.toString(lastWeek));
 				try {
 					handlerService.executeCommand(ParameterizedCommand.generateCommand(commandService.getCommand("Timesheet.submit"), parameters), null);
 				} catch (Exception e) {
 					MessageBox.setError(title, e.getMessage());
 				}
 			}
 			// Then create new timesheet for next year
 			String fileName = "template/TimesheetTemplate.ods";
 			URL fileURL = FileLocator.resolve(Platform.getBundle("com.uwusoft.timesheet.googlestorage").getEntry(fileName));
 			byte[] data = null;
 			InputStream input = null;
 			if ("file".equals(fileURL.getProtocol())) {//$NON-NLS-1$
 				File file = new File(fileURL.getPath());
 				data = new byte[(int) file.length()];
 				input = new FileInputStream(file);
 			}
 			if ("jar".equals(fileURL.getProtocol())) { //$NON-NLS-1$
 				String path = fileURL.getPath();
 				if (path.startsWith("file:")) {
 					// strip off the !/
 					path = path.substring(5, path.indexOf("!"));
 					JarFile file = new JarFile(path);
 					ZipEntry entry = file.getEntry(fileName);
 					data = new byte[(int) entry.getSize()];
 					input = file.getInputStream(entry);
 				}
 			}
 			if (data == null) return;
 			input.read(data);
 			input.close();
 			DocumentListEntry newDocument = new DocumentListEntry();
 			String mimeType = DocumentListEntry.MediaType.fromFileName(fileName).getMimeType();
 			newDocument.setMediaSource(new MediaByteArraySource(data, mimeType));
 			Calendar cal = new GregorianCalendar();
 			cal.setTime(new Date());
 			if (lastWeek != 0) cal.add(Calendar.YEAR, 1);
 			String name = "Timesheet " + cal.get(Calendar.YEAR);
 			newDocument.setTitle(new PlainTextConstruct(name));
 
 			spreadsheetKey = docsService.insert(new URL("https://docs.google.com/feeds/default/private/full/"), newDocument).getDocId();
 			MessageBox.setMessage("Spreadsheet created", "Created \"" + name + "\"");
 			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 			if (StringUtils.isEmpty(copySpreadsheetKey)) {
 				ImportTasksUtil.execute(this);
 				reloadHeadingIndex();
 			}
 			else {
 				List<WorksheetEntry> worksheets = service.getFeed(factory.getWorksheetFeedUrl(copySpreadsheetKey, "private", "full"), WorksheetFeed.class).getEntries();
 				WorksheetEntry timesheet = worksheets.remove(0); // remove timesheet
 			    for (WorksheetEntry worksheet : worksheets) {
 			    	String title = worksheet.getTitle().getPlainText();
 			    	MessageBox.setMessage("Created worksheet", title);
 					WorksheetEntry newWorksheet = createWorksheet(title, title.endsWith(SubmissionService.PROJECTS) ?
 							Arrays.asList(new String[] {PROJECT, ID}) : Arrays.asList(new String[] {TASK, PROJECT, ID, PROJECT + ID}));
 					if (newWorksheet == null) break;
 					for (ListEntry entry : service.getFeed(worksheet.getListFeedUrl(), ListFeed.class).getEntries()) {
 						ListEntry newEntry = new ListEntry();
 						for (String element : entry.getCustomElements().getTags())
 							newEntry.getCustomElements().setValueLocal(element, entry.getCustomElements().getValue(element));
 						MessageBox.setMessage("Created entry", (title.endsWith(SubmissionService.PROJECTS) ?
 								entry.getCustomElements().getValue(PROJECT) : entry.getCustomElements().getValue(TASK)));
 						service.insert(newWorksheet.getListFeedUrl(), newEntry);
 					}
 			        if (!reloadWorksheets()) return;
 				}
 				// get weekly total and overtime to roll over to next year
 				String overtime = "";
 				ListFeed feed = service.getFeed(factory.getListFeedUrl(copySpreadsheetKey, "1", "private", "full"), ListFeed.class);
 				List<ListEntry> listEntries = feed.getEntries();
 				int rowsOfWeek = 0;
 				for (int i = listEntries.size() - 2; i > 0; i--, rowsOfWeek++) {
 					CustomElementCollection elements = listEntries.get(i).getCustomElements();
 					if (elements.getValue(WEEKLY_TOTAL) != null) {
 						overtime = elements.getValue(OVERTIME);
 						break; // end of last week
 					}
 					if (i==1) break;
 				}            
 				ListEntry timeEntry = new ListEntry();
 				timeEntry.getCustomElements().setValueLocal(WEEKLY_TOTAL, "0");
 				service.insert(factory.getListFeedUrl(copySpreadsheetKey, "1", "private", "full"), timeEntry);
 			
 				createUpdateCellEntry(timesheet, listEntries.size() + 2, headingIndex.get(WEEKLY_TOTAL), "=SUM(R[-1]C[-1]:R[-" + ++rowsOfWeek + "]C[-1])");
 				
 				feed = service.getFeed(factory.getListFeedUrl(copySpreadsheetKey, "1", "private", "full"), ListFeed.class);
 				String weeklyTotal = feed.getEntries().get(feed.getEntries().size() - 1).getCustomElements().getValue(WEEKLY_TOTAL);
 			    
 				reloadHeadingIndex();
 				createUpdateCellEntry(defaultWorksheet, 2, headingIndex.get(WEEKLY_TOTAL), weeklyTotal);
 				createUpdateCellEntry(defaultWorksheet, 2, headingIndex.get(OVERTIME), overtime);
 			}
 			preferenceStore.setValue(SPREADSHEET_KEY, spreadsheetKey);
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 	}
 
 	private void reloadHeadingIndex() throws IOException, ServiceException, MalformedURLException {
 		CellFeed cellFeed = service.getFeed(factory.getCellFeedUrl(spreadsheetKey, "1", "private", "full"), CellFeed.class);
 		headingIndex.clear();
 		for (CellEntry entry : cellFeed.getEntries()) {
 			if (entry.getCell().getRow() == 1)
 				headingIndex.put(entry.getCell().getValue(), entry.getCell().getCol());
 			else
 				break;
 		}
 	}
 
     private boolean reloadWorksheets() {
 		try {
 			listFeedUrl = factory.getListFeedUrl(spreadsheetKey, "1", "private", "full");
 			WorksheetFeed feed = service.getFeed(factory.getWorksheetFeedUrl(spreadsheetKey, "private", "full"), WorksheetFeed.class);
 	        worksheets = feed.getEntries();
 	        defaultWorksheet = worksheets.get(0);
 	        worksheets.remove(defaultWorksheet); // only task and project sheets remaining
 		} catch (MalformedURLException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 		return true;
     }
 
     public void addPropertyChangeListener(final PropertyChangeListener listener) { 
 		addListenerObject(listener);
     } 
    
     public void removePropertyChangeListener(final PropertyChangeListener listener) { 
 		removeListenerObject(listener);
     } 
 
     protected void firePropertyChangeEvent(final PropertyChangeEvent event) {
 		if (event == null) {
 			throw new NullPointerException();
 		}
 
         synchronized (getListeners()) {
         	for (Object listener : getListeners()) {
         		((PropertyChangeListener) listener).propertyChange(event);
         	}    
         }
     }
     
     public List<String> getSystems() {
     	List<String> systems = new ArrayList<String>();
     	for (WorksheetEntry worksheet : worksheets) {
             String title = worksheet.getTitle().getPlainText();
     		if(title.endsWith(SubmissionService.PROJECTS)) continue;
 	        systems.add(title); 			
     	}
         return systems;
     }
     
     public List<String> getProjects(String system) {
     	List<String> projects = new ArrayList<String>();
 		URL worksheetListFeedUrl = getListFeedUrl(system + SubmissionService.PROJECTS);
 		if (worksheetListFeedUrl == null) return projects;
 		try {
 			ListFeed feed = service.getFeed(worksheetListFeedUrl, ListFeed.class);
 			for (ListEntry entry : feed.getEntries()) {
 				projects.add(entry.getCustomElements().getValue(PROJECT));
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
         return projects;    	
     }
     
 	public Collection<SubmissionProject> getImportedProjects(String system) {
 		Map<String, SubmissionProject> projects = new LinkedHashMap<String, SubmissionProject>();
 		try {
 			for (ListEntry entry : service.getFeed(getListFeedUrl(system), ListFeed.class).getEntries()) {
 				String project = entry.getCustomElements().getValue(PROJECT);
 				if (projects.get(project) == null)
 					projects.put(project, new SubmissionProject(Long.parseLong(entry.getCustomElements().getValue(PROJECT+ID)), project));
 				projects.get(project).addTask(new SubmissionTask(Long.parseLong(entry.getCustomElements().getValue(ID)),
 						entry.getCustomElements().getValue(TASK)));
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 		return projects.values();
 	}
     
     public List<String> findTasksBySystemAndProject(String system, String project) {
     	List<String> tasks = new ArrayList<String>();
 		URL worksheetListFeedUrl = getListFeedUrl(system);
 		if (worksheetListFeedUrl == null) return tasks;
 		ListQuery query = new ListQuery(worksheetListFeedUrl);
 		query.setSpreadsheetQuery(PROJECT.toLowerCase() + " = \"" + project + "\"");
 		try {
 			ListFeed feed = service.query(query, ListFeed.class);
 			List<ListEntry> listEntries = feed.getEntries();
 			for (ListEntry entry : listEntries) {
 				tasks.add(entry.getCustomElements().getValue(TASK));
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
     	return tasks;
     }
     
     public List<TaskEntry> getTaskEntries(Date startDate, Date endDate) {
     	List <TaskEntry> taskEntries = new ArrayList<TaskEntry>();
 		try {
             ListQuery query = new ListQuery(listFeedUrl);
             query.setSpreadsheetQuery(DATE.toLowerCase() + " >= " + new SimpleDateFormat(dateFormat).format(startDate)
             		+ " and " + DATE.toLowerCase() + " <= " + new SimpleDateFormat(dateFormat).format(endDate));
 	        List<ListEntry> listEntries = service.query(query, ListFeed.class).getEntries();
 	        for (ListEntry listEntry : listEntries) {
 	            CustomElementCollection elements = listEntry.getCustomElements();
 	            Calendar date = Calendar.getInstance();
 	            date.setTime(new SimpleDateFormat(dateFormat).parse(elements.getValue(DATE)));
 	            
 	            if (elements.getValue(TIME) != null) {
 		            Calendar time = Calendar.getInstance();
 		            time.setTime(new SimpleDateFormat(timeFormat).parse(elements.getValue(TIME)));
 		            date.set(Calendar.HOUR, time.get(Calendar.HOUR));
 		            date.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
		            date.set(Calendar.AM_PM, time.get(Calendar.AM_PM));
 	            }
 	            
 	            String task = elements.getValue(TASK);
 	            if (task == null) break;
 	            Long id = Long.parseLong(elements.getValue(ID));
 	            if (CHECK_IN.equals(task) || BREAK.equals(task))
 	            	taskEntries.add(new TaskEntry(id, date.getTime(), task, null, null, 0, elements.getValue(COMMENT), false, SUBMISSION_STATUS_TRUE.equals(elements.getValue(SUBMISSION_STATUS))));
 	            else
 	            	taskEntries.add(new TaskEntry(id, date.getTime(), task, elements.getValue(PROJECT), getSystem(id.intValue()), Float.parseFloat(elements.getValue(TOTAL)),
 	            			elements.getValue(COMMENT), elements.getValue(TIME) == null ? true : false, SUBMISSION_STATUS_TRUE.equals(elements.getValue(SUBMISSION_STATUS))));
 	        }
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		} catch (ParseException e) {
 			MessageBox.setError(title, e.getMessage());
 		}
 		return taskEntries;
     }
     
 	@Override
     public String[] getUsedCommentsForTask(String task, String project, String system) {
 		if (StringUtils.isEmpty(task)) return new String[] {StringUtils.EMPTY};
 		Set<String> comments = new HashSet<String>();
     	ListQuery query = new ListQuery(listFeedUrl);
 		query.setSpreadsheetQuery(TASK.toLowerCase() + " = \"" + task + "\" and "
 				+ PROJECT.toLowerCase() + " = \"" + project + "\"");
 		try {
 			ListFeed feed = service.query(query, ListFeed.class);
 			for (ListEntry entry : feed.getEntries()) {
 				if (getSystem(Integer.parseInt(entry.getCustomElements().getValue(ID))).equals(system))
 					comments.add(entry.getCustomElements().getValue(COMMENT));
 			}
 			return comments.toArray(new String[comments.size()]);
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 		return new String[comments.size()];
     }
     
 	@Override
     public Long createTaskEntry(TaskEntry task) {
         try {
 			listFeedUrl = factory.getListFeedUrl(spreadsheetKey, "1", "private", "full");
 			ListEntry timeEntry = new ListEntry();
 			
 	        Calendar cal = new GregorianCalendar();
 	        cal.setTime(task.getDateTime() == null ? new Date() : task.getDateTime());
 	        //cal.setFirstDayOfWeek(Calendar.MONDAY);
 			if (task.getDateTime() != null) {
 				timeEntry.getCustomElements().setValueLocal(WEEK, Integer.toString(cal.get(Calendar.WEEK_OF_YEAR)));
 				timeEntry.getCustomElements().setValueLocal(DATE,
 						new SimpleDateFormat(dateFormat).format(task.getDateTime()));			
 				if (task.getTotal() == 0)
 					timeEntry.getCustomElements().setValueLocal(TIME, new SimpleDateFormat(timeFormat).format(task.getDateTime()));
 			}
 			else if (!BREAK.equals(task.getTask().getName()))
 				timeEntry.getCustomElements().setValueLocal(TOTAL, Float.toString(task.getTotal()));
 			
 			String taskLink = null;
 			if (CHECK_IN.equals(task.getTask().getName()) || BREAK.equals(task.getTask().getName()))
 				timeEntry.getCustomElements().setValueLocal(TASK, task.getTask().getName());
 			else {
 				if (task.getTotal() != 0) {
 					timeEntry.getCustomElements().setValueLocal(TOTAL, Float.toString(task.getTotal()));
 					if (task.isAllDay())
 						timeEntry.getCustomElements().setValueLocal(DAILY_TOTAL, Float.toString(task.getTotal()));
 				}
 				taskLink = getTaskLink(task.getTask().getName(), task.getTask().getProject().getName(), task.getTask().getProject().getSystem());
 			}
 			if (task.getComment() != null) timeEntry.getCustomElements().setValueLocal(COMMENT, task.getComment());
 			service.insert(listFeedUrl, timeEntry);
 
             if (!reloadWorksheets()) return null;
             
             logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "create task entry: " + task
             		+ (taskLink == null ? "" : " (task link: " + taskLink +")")));
             
         	ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
         	if (feed.getTotalResults() == 1) {
     			MessageBox.setError(title, "Couldn't insert cell (Maybe there's an empty line in the spreadsheet)");
     			return null;
         	}
             createUpdateCellEntry(defaultWorksheet,	feed.getEntries().size() + 1, headingIndex.get(ID), "=ROW()");
             
             if (taskLink != null) {
 				updateTask(taskLink, feed.getEntries().size() + 1);
 				// if no total set: the (temporary) total of the task will be calculated by: end time - end time of the previous task
 				if (task.getTotal() == 0)
 					createUpdateCellEntry(defaultWorksheet,	feed.getEntries().size() + 1, headingIndex.get(TOTAL), "=(R[0]C[-1]-R[-1]C[-1])*24"); // calculate task total
 			}
         	feed = service.getFeed(listFeedUrl, ListFeed.class);
             return Long.parseLong(feed.getEntries().get(feed.getEntries().size() - 1).getCustomElements().getValue(ID));
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 		return null;
     }
 
 	@Override
 	public TaskEntry getLastTask() {
     	try {
 			ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
 			int size = feed.getEntries().size();
 			if (size == 0) return null;
 			CustomElementCollection elements = feed.getEntries().get(size - 1).getCustomElements();
 			if (elements.getValue(DATE) == null && elements.getValue(TIME) == null && elements.getValue(TASK) != null && size > 1) // if date and time isn't set yet this should be the last task
 				return new TaskEntry(Long.parseLong(elements.getValue(ID)), elements.getValue(TASK), elements.getValue(PROJECT), getSystem(size + 1), elements.getValue(COMMENT));
 			return null;
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 		return null;
 	}
 
 	public Date getLastTaskEntryDate() {
     	try {
 			ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
 			for (int i = feed.getEntries().size() - 1; i > 2; i--) {
 				CustomElementCollection elements = feed.getEntries().get(i).getCustomElements();
 				if (elements.getValue(DATE) != null)
 					return new SimpleDateFormat(dateFormat).parse(elements.getValue(DATE));
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		} catch (ParseException e) {
 			MessageBox.setError(title, e.getMessage());
 		}		
 		return null;
 	}
 	
 	private void updateTask(String taskLink, int row) {
 		createUpdateCellEntry(defaultWorksheet,	row,
 				headingIndex.get(TASK), "=" + taskLink);
 		createUpdateCellEntry(defaultWorksheet,	row,
 				headingIndex.get(PROJECT), "=" + taskLink.replace("!A", "!B")); // TODO hardcoded: project must be in the second column
 	}
 
     public void handleDayChange() {
         try {
             if (!reloadWorksheets()) return;
             ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
             List<ListEntry> listEntries = feed.getEntries();			
             int rowsOfDay = 0;
             for (int i = listEntries.size() - 2; i > 0; i--, rowsOfDay++) {
                 CustomElementCollection elements = listEntries.get(i).getCustomElements();
                 if (CHECK_IN.equals(elements.getValue(TASK))) break; // begin of day
             }
             createUpdateCellEntry(defaultWorksheet, listEntries.size() + 1, headingIndex.get(DAILY_TOTAL), "=SUM(R[0]C[-1]:R[-" + rowsOfDay + "]C[-1])");
         } catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
         } catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
         }
     }
 
     public void handleWeekChange() {
         try {
 			listFeedUrl = factory.getListFeedUrl(spreadsheetKey, "1", "private", "full");
     		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
             String weeklyWorkingHours = preferenceStore.getString(TimesheetApp.WORKING_HOURS);
             ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
             List<ListEntry> listEntries = feed.getEntries();
             int rowsOfWeek = 0;
             int i = listEntries.size() - 2;
             for (; i > 1; i--, rowsOfWeek++) {
                 CustomElementCollection elements = listEntries.get(i).getCustomElements();
                 if (elements.getValue(WEEKLY_TOTAL) != null) break; // end of last week
             }            
             ListEntry timeEntry = new ListEntry();
 			timeEntry.getCustomElements().setValueLocal(WEEKLY_TOTAL, "0");
 			service.insert(listFeedUrl, timeEntry);
             if (!reloadWorksheets()) return;
 			
 			createUpdateCellEntry(defaultWorksheet, listEntries.size() + 2, headingIndex.get(WEEKLY_TOTAL), "=SUM(R[-1]C[-1]:R[-"
 						+ (i == 1 ? ++rowsOfWeek + 1 : ++rowsOfWeek) + "]C[-1])"); // add weekly total from roll over on second line
 			createUpdateCellEntry(defaultWorksheet, listEntries.size() + 2, headingIndex.get(OVERTIME), "=R[0]C["
 						+ (headingIndex.get(WEEKLY_TOTAL) - headingIndex.get(OVERTIME)) + "]-" +weeklyWorkingHours+ "+" +"R[-" + ++rowsOfWeek + "]C[0]");
         } catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
         } catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
         }        
     }
 
 	public void updateTaskEntry(TaskEntry entry) {
 		if (entry.isStatus()) {
 			createUpdateCellEntry(defaultWorksheet, entry.getRowNum().intValue(), headingIndex.get(SUBMISSION_STATUS), SUBMISSION_STATUS_TRUE);
 			return;
 		}
 		if (entry.getDateTime() != null) {
 			createUpdateCellEntry(defaultWorksheet, entry.getRowNum().intValue(), headingIndex.get(TIME), new SimpleDateFormat(timeFormat).format(entry.getDateTime()));
 			Calendar cal = new GregorianCalendar();
 			//cal.setFirstDayOfWeek(Calendar.MONDAY);
 			cal.setTime(entry.getDateTime());
 			int weekNum = cal.get(Calendar.WEEK_OF_YEAR);
 			createUpdateCellEntry(defaultWorksheet, entry.getRowNum().intValue(), headingIndex.get(DATE), new SimpleDateFormat(dateFormat).format(entry.getDateTime()));			
 			createUpdateCellEntry(defaultWorksheet, entry.getRowNum().intValue(), headingIndex.get(WEEK), Integer.toString(weekNum));
 		}
 		if (CHECK_IN.equals(entry.getTask().getName()) || BREAK.equals(entry.getTask().getName()))
 			createUpdateCellEntry(defaultWorksheet, entry.getRowNum().intValue(), headingIndex.get(TASK), entry.getTask().getName());
 		else
 			updateTask(getTaskLink(entry.getTask().getName(), entry.getTask().getProject().getName(), entry.getTask().getProject().getSystem()),
 					entry.getRowNum().intValue());
 		createUpdateCellEntry(defaultWorksheet, entry.getRowNum().intValue(), headingIndex.get(COMMENT), entry.getComment());
 	}
 
 	public void importTasks(String submissionSystem, Collection<SubmissionProject> projects) {
 		try {
 			URL worksheetListFeedUrl = null;
 			if ((worksheetListFeedUrl = getListFeedUrl(submissionSystem)) != null) {
 				for (SubmissionProject project : projects) {
 					ListQuery query = new ListQuery(worksheetListFeedUrl);
 					query.setSpreadsheetQuery(PROJECT.toLowerCase() + " = \"" + project.getName() + "\"");
 					try {
 						ListFeed feed = service.query(query, ListFeed.class);
 						List<ListEntry> listEntries = feed.getEntries();
 						List<SubmissionTask> tasks = new ArrayList<SubmissionTask>(project.getTasks());
 						
 						for (ListEntry entry : listEntries) // collect available tasks
 							for (SubmissionTask task : tasks)
 								if (task.getName().equals(entry.getCustomElements().getValue(TASK)))
 									tasks.remove(task);							
 						project.setTasks(tasks);
 					} catch (IOException e) {
 						MessageBox.setError(title, e.getMessage());
 					} catch (ServiceException e) {
 						MessageBox.setError(title, e.getResponseBody());
 					}
 				}
 			}
 			else {
 				WorksheetEntry worksheet = createWorksheet(submissionSystem, Arrays.asList(new String[] {TASK, PROJECT, ID, PROJECT + ID}));				
 			    worksheetListFeedUrl = worksheet.getListFeedUrl(); 			
 			}
 			for (SubmissionProject project : projects) {
 				for (SubmissionTask task : project.getTasks()) {
 					ListEntry taskEntry = new ListEntry();
 					taskEntry.getCustomElements().setValueLocal(TASK, task.getName());
 					taskEntry.getCustomElements().setValueLocal(ID, Long.toString(task.getId()));
 					taskEntry.getCustomElements().setValueLocal(PROJECT + ID, Long.toString(project.getId()));
 		            
 					logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "Import task: " + task.getName() + " id=" + task.getId()
 		            		+ " (" + project.getName() + " id=" + project.getId() + ") "));
 					
 					service.insert(worksheetListFeedUrl, taskEntry);
 					reloadWorksheets();
 					String projectLink = getProjectLink(submissionSystem, project.getName(), project.getId(), true);
 					if (projectLink != null) {
 						WorksheetEntry worksheet = getWorksheet(submissionSystem);
 						ListFeed feed = service.getFeed(worksheet.getListFeedUrl(), ListFeed.class);
 						createUpdateCellEntry(worksheet, feed.getEntries().size() + 1,
 								getHeadingIndex(submissionSystem, PROJECT), "=" + projectLink);
 					}
 				}
 			}
 		} catch (MalformedURLException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 	}
 
 	private WorksheetEntry createWorksheet(String system, List<String> headings) throws IOException,
 			ServiceException, MalformedURLException {
 		WorksheetEntry newWorksheet = new WorksheetEntry();
 		newWorksheet.setTitle(new PlainTextConstruct(system));
 		newWorksheet.setColCount(6);
 		newWorksheet.setRowCount(20);
 		service.insert(factory.getWorksheetFeedUrl(spreadsheetKey, "private", "full"), newWorksheet);
 		if (!reloadWorksheets()) return null;
         WorksheetEntry worksheet = getWorksheet(system);
 		// create column headers:
         for (int i = 0; i < headings.size(); i++) 
         	createUpdateCellEntry(worksheet, 1, i+1, headings.get(i));
 	    return worksheet;
 	}
 
 	public Set<String> submitEntries(Date startDate, Date endDate) {
         Set<String> systems = new HashSet<String>();
 		try {
             if (!reloadWorksheets()) return systems;
     		
             ListQuery query = new ListQuery(listFeedUrl);
     		query.setSpreadsheetQuery(SUBMISSION_STATUS.toLowerCase() + " != \"" + SUBMISSION_STATUS_TRUE + "\" and "
     								+ DATE.toLowerCase() + " >= " + new SimpleDateFormat(dateFormat).format(startDate)
     			            		+ " and " + DATE.toLowerCase() + " <= " + new SimpleDateFormat(dateFormat).format(endDate));
 	        List<ListEntry> listEntries = service.query(query, ListFeed.class).getEntries();
             
             if (listEntries.isEmpty()) return systems;
 	        Date lastDate = new SimpleDateFormat(dateFormat).parse(listEntries.get(0).getCustomElements().getValue(DATE));
             DailySubmissionEntry entry = new DailySubmissionEntry(lastDate);
 
             for (ListEntry listEntry : listEntries) {
             	CustomElementCollection elements = listEntry.getCustomElements();
 	            String task = elements.getValue(TASK);
 	            if (SUBMISSION_STATUS_TRUE.equals(elements.getValue(SUBMISSION_STATUS)) || task == null || CHECK_IN.equals(task) || BREAK.equals(task))
 	            	continue;
 	            if (elements.getValue(DATE) == null) continue;
 	            Date date = new SimpleDateFormat(dateFormat).parse(elements.getValue(DATE));
 	            if (!date.equals(lastDate)) { // another day
 	            	entry.submitEntries();
 	                entry = new DailySubmissionEntry(date);
 	                lastDate = date;
 	            }
 
 	            String system = getSystem(Integer.parseInt(elements.getValue(ID)));
 	            String project = elements.getValue(PROJECT);
 				if (submissionSystems.containsKey(system)) {
 					systems.add(system);
 					SubmissionEntry submissionTask = getSubmissionTask(task, project, system);
 					if (submissionTask != null)
 						entry.addSubmissionEntry(submissionTask, Double.valueOf(elements.getValue(TOTAL)));
 				}
             	createUpdateCellEntry(defaultWorksheet,	Integer.parseInt(elements.getValue(ID)), headingIndex.get(SUBMISSION_STATUS), SUBMISSION_STATUS_TRUE);
     		}
             entry.submitEntries();
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		} catch (ParseException e) {
 			MessageBox.setError(title, e.getMessage());
 		}
 		return systems;
     }
 
 	public void submitFillTask(Date date) {
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		if (!StringUtils.isEmpty(preferenceStore.getString(TimesheetApp.DAILY_TASK))) {
 			Task task = TimesheetApp.createTask(TimesheetApp.DAILY_TASK);
 			if (submissionSystems.containsKey(task.getProject().getSystem())) {
 				SubmissionEntry submissionTask = getSubmissionTask(task.getName(), task.getProject().getName(), task.getProject().getSystem());
 				if (submissionTask != null)
 					new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(submissionSystems.get(task.getProject().getSystem()))
 							.submit(date, submissionTask, Double.parseDouble(preferenceStore.getString(TimesheetApp.DAILY_TASK_TOTAL)));
 			}
 		}
 	}
 	
 	private String getSystem(int row) throws IOException, ServiceException {
 		String system = getInputValue(row, headingIndex.get(TASK)).split("!")[0];
 		return system.substring(system.indexOf("=") + 1);
 	}
 
 	private String getInputValue(int row, int col) throws IOException, ServiceException,
 			MalformedURLException {
 		CellEntry cellEntry = service.getEntry(new URL(defaultWorksheet.getCellFeedUrl().toString()
 				+ "/" + "R" + row + "C" + col), CellEntry.class);
 		return cellEntry.getCell().getInputValue();
 	}
 	
 	private WorksheetEntry getWorksheet(String system) {
 		for (WorksheetEntry worksheet : worksheets) {
 		    if(system.equals(worksheet.getTitle().getPlainText())) {
 		        return worksheet; 			
 		    }
 		}
 		return null;
 	}
 	
 	private URL getListFeedUrl(String system) {
 		WorksheetEntry worksheet = getWorksheet(system);
 		if (worksheet == null) return null;
 		return worksheet.getListFeedUrl();
 	}
 	
 	private Integer getHeadingIndex(String system, String heading) {
 		WorksheetEntry worksheet = getWorksheet(system);
 		if (worksheet == null) return null;		
 		try {
 			CellFeed cellFeed = service.getFeed(worksheet.getCellFeedUrl(), CellFeed.class);
 			for (CellEntry entry : cellFeed.getEntries()) {
 				if (entry.getCell().getRow() == 1) {
 					if (heading.equals(entry.getCell().getValue()))
 						return entry.getCell().getCol();
 				}
 				else
 					break;
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 		return null;
 	}
 
 	private String getProjectLink(String system, String project, Long projectId, boolean createNew) {
 		String systemProjects = system + SubmissionService.PROJECTS;
 		URL worksheetListFeedUrl = getListFeedUrl(systemProjects);
 		try {
 			if (worksheetListFeedUrl == null) {
 				WorksheetEntry worksheet = createWorksheet(systemProjects, Arrays.asList(new String[] {PROJECT, ID}));
 				if (worksheet == null) return null;
 			    worksheetListFeedUrl = worksheet.getListFeedUrl(); 			
 			}
 			ListFeed feed = service.getFeed(worksheetListFeedUrl, ListFeed.class);
 			List<ListEntry> entries = feed.getEntries();
 			for (int i = 0; i < entries.size(); i++) {
 				if(project.equals(entries.get(i).getCustomElements().getValue(PROJECT)))
 					return systemProjects + "!A" + (i + 2); // TODO hardcoded: project must be in the first column					
 			}
 			if (createNew) {
 				ListEntry listEntry = new ListEntry();
 				listEntry.getCustomElements().setValueLocal(PROJECT, project);
 				listEntry.getCustomElements().setValueLocal(ID, Long.toString(projectId));
 				service.insert(worksheetListFeedUrl, listEntry);
 	            if (!reloadWorksheets()) return null;
 				return getProjectLink(system, project, projectId, false);
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 		return null;
 	}
 	
 	private String getTaskLink(String task, String project, String system) {
         if (system == null) { // search for task in all task worksheets
         	for (WorksheetEntry worksheet : worksheets) {
         		String title = worksheet.getTitle().getPlainText();
         		if(title.endsWith(SubmissionService.PROJECTS)) continue;
         		String taskLink = getTaskLink(task, project, worksheet);
         		if (taskLink != null) return taskLink;
         	}
         }
 		return getTaskLink(task, project, getWorksheet(system));
 	}
 
 	private String getTaskLink(String task, String project, WorksheetEntry worksheet) {
 		try {
 			ListFeed feed = service.getFeed(worksheet.getListFeedUrl(),	ListFeed.class);
 			List<ListEntry> entries = feed.getEntries();
 			for (int i = 0; i < entries.size(); i++) {
 				if (project == null && task.equals(entries.get(i).getCustomElements().getValue(TASK))
 					|| task.equals(entries.get(i).getCustomElements().getValue(TASK))
 						&& project.equals(entries.get(i).getCustomElements().getValue(PROJECT)))
 					return worksheet.getTitle().getPlainText() + "!A" + (i + 2); // TODO hardcoded: task must be in the first column
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 		return null;
 	}
 	
 	private SubmissionEntry getSubmissionTask(String task, String project, String system) {
 		ListQuery query = new ListQuery(getWorksheet(system).getListFeedUrl());
 		query.setSpreadsheetQuery(TASK.toLowerCase() + " = \"" + task + "\" and "
 								+ PROJECT.toLowerCase() + " = \"" + project + "\"");
 		try {
 			ListFeed feed = service.query(query, ListFeed.class);
 			for (ListEntry entry : feed.getEntries()) {
 				if (entry.getCustomElements().getValue(PROJECT + ID) == null) return null;
 				return new SubmissionEntry(Long.parseLong(entry.getCustomElements().getValue(PROJECT + ID)),
 						Long.parseLong(entry.getCustomElements().getValue(ID)),
 						entry.getCustomElements().getValue(TASK), entry.getCustomElements().getValue(PROJECT), system);
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
 		return null;
 	}
 	
     private void createUpdateCellEntry(WorksheetEntry worksheet, int row, int col, String value) {
 		try {
 			CellEntry entry = service.getEntry(new URL(worksheet.getCellFeedUrl().toString() + "/" + "R" + row + "C" + col), CellEntry.class);
 	        entry.changeInputValueLocal(value);
 	        entry.update();
 		} catch (MalformedURLException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getResponseBody());
 		}
     }
 
 	@Override
 	public void openUrl(String openBrowser) {
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		if (preferenceStore.getBoolean(PREFIX + openBrowser))
 			DesktopUtil.openUrl("https://docs.google.com/spreadsheet/ccc?key=" + spreadsheetKey + "&pli=1#gid=0");
 	}
 }
