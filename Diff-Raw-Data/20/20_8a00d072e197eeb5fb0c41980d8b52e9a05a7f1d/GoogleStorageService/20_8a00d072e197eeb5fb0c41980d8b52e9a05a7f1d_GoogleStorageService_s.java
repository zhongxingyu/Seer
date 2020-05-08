 package com.uwusoft.timesheet.googlestorage;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.widgets.Display;
 
 import com.google.gdata.client.spreadsheet.FeedURLFactory;
 import com.google.gdata.client.spreadsheet.ListQuery;
 import com.google.gdata.client.spreadsheet.SpreadsheetService;
 import com.google.gdata.data.PlainTextConstruct;
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
 import com.uwusoft.timesheet.dialog.LoginDialog;
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.extensionpoint.SubmissionService;
 import com.uwusoft.timesheet.extensionpoint.model.DailySubmissionEntry;
 import com.uwusoft.timesheet.model.Project;
 import com.uwusoft.timesheet.model.Task;
 import com.uwusoft.timesheet.util.ExtensionManager;
 import com.uwusoft.timesheet.util.MessageBox;
 import com.uwusoft.timesheet.util.SecurePreferencesManager;
 
 /**
  * storage service for Google Docs spreadsheet
  *
  * @author Uta Wunderlich
  * @version $Revision: $, $Date: Aug 15, 2011
  * @since Aug 15, 2011
  */
 public class GoogleStorageService implements StorageService {
 
     public static final String USERNAME="google.user.name";
     public static final String PASSWORD="google.user.password";
     public static final String SPREADSHEET_KEY="google.spreadsheet.key";
 
     private static final String dateFormat = "MM/dd/yyyy";
     private static final String timeFormat = "HH:mm";
     private String spreadsheetKey;
     private SpreadsheetService service;
     private FeedURLFactory factory;
     private URL listFeedUrl;
 	private Map<String, Integer> headingIndex;
     private List<WorksheetEntry> worksheets = new ArrayList<WorksheetEntry>();
     private WorksheetEntry defaultWorksheet;
     private Map<String,String> submissionSystems;
     private String message;
     private List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
     private String title = "Google Storage Service";
     private ILog logger;
     
     public GoogleStorageService() throws CoreException {
         service = new SpreadsheetService("Timesheet");
         service.setProtocolVersion(SpreadsheetService.Versions.V1);
         boolean lastSuccess = true;
         do lastSuccess = authenticate(lastSuccess);
        	while (!lastSuccess);
 		factory = FeedURLFactory.getDefault();
 		headingIndex = new LinkedHashMap<String, Integer>();
 		submissionSystems = new HashMap<String, String>();
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		String[] systems = preferenceStore.getString(SubmissionService.PROPERTY).split(SubmissionService.separator);
 		for (String system : systems) {
 			if (!StringUtils.isEmpty(system)) {
 				String descriptiveName = Character.toUpperCase(system.toCharArray()[system.lastIndexOf('.') + 1])
 						+ system.substring(system.lastIndexOf('.') + 2, system.indexOf(SubmissionService.SERVICE_NAME));
 				submissionSystems.put(descriptiveName, system);
 				if (getWorksheet(descriptiveName) == null); // TODO create import task dialog
 			}
 		}
         if (!reloadWorksheets()) return;
         logger = Activator.getDefault().getLog();
     }
     
     private boolean authenticate(boolean lastSuccess) throws CoreException {
         try {
 			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 			SecurePreferencesManager secureProps = new SecurePreferencesManager("Google");
 	    	String userName = preferenceStore.getString(USERNAME);
 	    	String password = secureProps.getProperty(PASSWORD);
 	    	if (lastSuccess && !StringUtils.isEmpty(userName) && !StringUtils.isEmpty(password)) {
 	        	service.setUserCredentials(userName, password);
 	            spreadsheetKey = preferenceStore.getString(SPREADSHEET_KEY);
 	        	return true;
 	    	}
 	    	
 	    	Display display = Display.getDefault();
 	    	LoginDialog loginDialog = new LoginDialog(display, "Google Log in", message, userName, password);
 			if (loginDialog.open() == Dialog.OK) {
 	        	service.setUserCredentials(loginDialog.getUser(), loginDialog.getPassword());
 	        	preferenceStore.setValue(USERNAME, loginDialog.getUser());
 	        	if (loginDialog.isStorePassword())
 	        		secureProps.storeProperty(PASSWORD, loginDialog.getPassword());
 	        	else
 	        		secureProps.removeProperty(PASSWORD);
 	            spreadsheetKey = preferenceStore.getString(SPREADSHEET_KEY);
 	        	return true;
 			}
 		} catch (AuthenticationException e) {
 			message = e.getLocalizedMessage();
 			return false;
 		}
         throw new CoreException(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, null));
    }
 
     private boolean reloadSpreadsheetKey() {
     	if (StringUtils.isEmpty(spreadsheetKey)) {
 			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
             spreadsheetKey = preferenceStore.getString(SPREADSHEET_KEY);
     	}
 		try {
 	    	if (StringUtils.isEmpty(spreadsheetKey)) {
 	    		MessageBox.setMessage("Create spreadsheet", "Please manually create a spreadsheet and copy the spreadsheet key to the Google Spreadsheet Preferences!");
 	    		return false;
 	    	}
 			listFeedUrl = factory.getListFeedUrl(spreadsheetKey, "od6", "private", "full");
 			CellFeed cellFeed = service.getFeed(factory.getCellFeedUrl(spreadsheetKey, "od6", "private", "full"), CellFeed.class);
 			for (CellEntry entry : cellFeed.getEntries()) {
 				if (entry.getCell().getRow() == 1)
 					headingIndex.put(entry.getCell().getValue(), entry.getCell().getCol());
 				else
 					break;
 			}
 		} catch (MalformedURLException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		}
     	return true;
     }
 
     private boolean reloadWorksheets() {
     	if (!reloadSpreadsheetKey()) return false;
 		try {
 			WorksheetFeed feed = service.getFeed(factory.getWorksheetFeedUrl(spreadsheetKey, "private", "full"), WorksheetFeed.class);
 	        worksheets = feed.getEntries();
 	        defaultWorksheet = worksheets.get(0);
 	        worksheets.remove(defaultWorksheet); // only task sheets remaining
 		} catch (MalformedURLException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		}
 		return true;
     }
 
     public void addPropertyChangeListener(PropertyChangeListener listener) 
     { 
     	listeners.add(listener); 
     } 
    
     public void removePropertyChangeListener(PropertyChangeListener listener) 
     { 
     	listeners.remove(listener); 
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
 		try {
 			ListFeed feed = service.getFeed(worksheetListFeedUrl, ListFeed.class);
 			for (ListEntry entry : feed.getEntries()) {
 				projects.add(entry.getCustomElements().getValue(PROJECT));
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		}
         return projects;    	
     }
     
     public List<String> findTasksBySystemAndProject(String system, String project) {
     	List<String> tasks = new ArrayList<String>();
 		URL worksheetListFeedUrl = getListFeedUrl(system);
 		ListQuery query = new ListQuery(worksheetListFeedUrl);
 		query.setSpreadsheetQuery(PROJECT.toLowerCase() + " = \"" + project + "\"");
 		try {
 			ListFeed feed = service.query(query, ListFeed.class);
 			List<ListEntry> listEntries = feed.getEntries();
 			for (ListEntry entry : listEntries) {
 				tasks.add(entry.getCustomElements().getValue(TASK));
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		}
     	return tasks;
     }
     
     public List<Task> getTaskEntries(Date date) {
     	List <Task> taskEntries = new ArrayList<Task>();
 		try {
 	        ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
 	        List<ListEntry> listEntries = feed.getEntries();
 	        for (int i = listEntries.size() - 1; i > 0; i--) {
 	            CustomElementCollection elements = listEntries.get(i).getCustomElements();
 	            if (elements.getValue(DATE) == null) continue;
 	            if (!new SimpleDateFormat(dateFormat).format(new SimpleDateFormat(dateFormat).parse(elements.getValue(DATE)))
 	            		.equals(new SimpleDateFormat(dateFormat).format(date))) break;
 	            
 	            String task = elements.getValue(TASK);
 	            if (task == null || elements.getValue(TIME) == null) break;
 	            if (CHECK_IN.equals(task) || BREAK.equals(task))
 	            	taskEntries.add(0, new Task(new Long(i + 2), new SimpleDateFormat(timeFormat).parse(elements.getValue(TIME)),
 	            			task, 0, new Project()));
 	            else
 	            	taskEntries.add(0, new Task(new Long(i + 2), new SimpleDateFormat(timeFormat).parse(elements.getValue(TIME)),
 	            			task, Float.parseFloat(elements.getValue(TOTAL)), new Project(elements.getValue(PROJECT), getSystem(i))));
 	            if (CHECK_IN.equals(task)) break;
 	        }
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ParseException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		}
 		return taskEntries;
     }
     
     public void createTaskEntry(Task task) {
         Calendar cal = new GregorianCalendar();
         cal.setTime(task.getDateTime());
         try {
             if (!reloadWorksheets()) return;
 			ListEntry timeEntry = new ListEntry();
 			timeEntry.getCustomElements().setValueLocal(WEEK, Integer.toString(cal.get(Calendar.WEEK_OF_YEAR) - 1));
 			
 			timeEntry.getCustomElements().setValueLocal(DATE,
 					new SimpleDateFormat(dateFormat).format(task.getDateTime()));
 			
 			if (task.getTotal() == 0)
 				timeEntry.getCustomElements().setValueLocal(TIME, new SimpleDateFormat(timeFormat).format(task.getDateTime()));
 			
 			String taskLink = null;
 			if (CHECK_IN.equals(task.getTask()) || BREAK.equals(task.getTask()))
 				timeEntry.getCustomElements().setValueLocal(TASK, task.getTask());
 			else {
 				if (task.getTotal() != 0) {
 					timeEntry.getCustomElements().setValueLocal(TOTAL, Float.toString(task.getTotal()));
 					if (task.isWholeDay())
 						timeEntry.getCustomElements().setValueLocal(DAILY_TOTAL, Float.toString(task.getTotal()));
 				}
 				taskLink = getTaskLink(task.getTask(), task.getProject().getName(), task.getProject().getSystem());
 			}
 			service.insert(listFeedUrl, timeEntry);
 
             if (!reloadWorksheets()) return;
             
             logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "create task entry: " + task
             		+ (taskLink == null ? "" : " (task link: " + taskLink +")")));
             
             if (taskLink != null) {
             	ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
 				updateTask(taskLink, feed.getEntries().size() + 1);
 				// if no total set: the (temporary) total of the task will be calculated by: end time - end time of the previous task
 				if (task.getTotal() == 0)
 					createUpdateCellEntry(defaultWorksheet,	feed.getEntries().size() + 1, headingIndex.get(TOTAL), "=(R[0]C[-1]-R[-1]C[-1])*24"); // calculate task total
 			}
 			for (PropertyChangeListener listener : listeners)
 				listener.propertyChange(new PropertyChangeEvent(this, "tasks", null, null));
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		}
     }
 
 	private void updateTask(String taskLink, int row) {
 		createUpdateCellEntry(defaultWorksheet,	row,
 				headingIndex.get(TASK), "=" + taskLink);
 		createUpdateCellEntry(defaultWorksheet,	row,
 				headingIndex.get(PROJECT), "=" + taskLink.replace("!A", "!B")); // TODO hardcoded: project must be in the second column
 	}
 
     public void storeLastDailyTotal() {
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
 			MessageBox.setError(title, e.getLocalizedMessage());
         } catch (ServiceException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
         }
     }
 
     public void storeLastWeekTotal(String weeklyWorkingHours) {
         try {
             if (!reloadWorksheets()) return;
             ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
             List<ListEntry> listEntries = feed.getEntries();
             int rowsOfWeek = 0;
             for (int i = listEntries.size() - 2; i > 0; i--, rowsOfWeek++) {
                 CustomElementCollection elements = listEntries.get(i).getCustomElements();
                 if (elements.getValue(WEEKLY_TOTAL) != null) break; // end of last week
                 if (i==1) break;
             }            
             ListEntry timeEntry = new ListEntry();
 			timeEntry.getCustomElements().setValueLocal(WEEKLY_TOTAL, "0");
 			service.insert(listFeedUrl, timeEntry);
             if (!reloadWorksheets()) return;
 			
 			createUpdateCellEntry(defaultWorksheet, listEntries.size() + 1, headingIndex.get(WEEKLY_TOTAL), "=SUM(R[-1]C[-1]:R[-" + ++rowsOfWeek + "]C[-1])");
 			createUpdateCellEntry(defaultWorksheet, listEntries.size() + 1, headingIndex.get(OVERTIME), "=R[0]C["
 						+ (headingIndex.get(WEEKLY_TOTAL) - headingIndex.get(OVERTIME)) + "]-" +weeklyWorkingHours+ "+" +"R[-" + ++rowsOfWeek + "]C[0]");
         } catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
         } catch (ServiceException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
         }        
     }
 
 	public void updateTaskEntry(Date time, Long id) {
 		createUpdateCellEntry(defaultWorksheet, id.intValue(), headingIndex.get(TIME), new SimpleDateFormat(timeFormat).format(time));
 		for (PropertyChangeListener listener : listeners)
 			listener.propertyChange(new PropertyChangeEvent(this, "tasks", null, null));
 	}
 
 	public void updateTaskEntry(Task task, Long id) {
 		if (CHECK_IN.equals(task.getTask()) || BREAK.equals(task.getTask()))
 			createUpdateCellEntry(defaultWorksheet, id.intValue(), headingIndex.get(TASK), task.getTask());
 		else
 			updateTask(getTaskLink(task.getTask(), task.getProject().getName(), task.getProject().getSystem()), id.intValue());
 		for (PropertyChangeListener listener : listeners)
 			listener.propertyChange(new PropertyChangeEvent(this, "tasks", null, null));
 	}
 
 	public void importTasks(String submissionSystem, Map<String, List<String>> projects) {
 		try {
 			URL worksheetListFeedUrl = null;
			int row = 2;
 			if ((worksheetListFeedUrl = getListFeedUrl(submissionSystem)) != null) {
 				for (String project : projects.keySet()) {
 					ListQuery query = new ListQuery(worksheetListFeedUrl);
 					query.setSpreadsheetQuery(PROJECT.toLowerCase() + " = \"" + project + "\"");
 					List<String> availableTasks = new ArrayList<String>();
 					try {
 						ListFeed feed = service.query(query, ListFeed.class);
 						List<ListEntry> listEntries = feed.getEntries();
 						for (ListEntry entry : listEntries) {
 							String availableTask = entry.getCustomElements().getValue(TASK);
 							availableTasks.add(availableTask);
							projects.get(project).remove(availableTask);							
 						}
						row = listEntries.size() - 1;
 					} catch (IOException e) {
 						MessageBox.setError(title, e.getLocalizedMessage());
 					} catch (ServiceException e) {
 						MessageBox.setError(title, e.getLocalizedMessage());
 					}
 				}
 			}
 			else {
 				createWorksheet(submissionSystem);
 				
 	            if (!reloadWorksheets()) return;
 	            WorksheetEntry worksheet = getWorksheet(submissionSystem);
 			    worksheetListFeedUrl = worksheet.getListFeedUrl(); 			
 				// create column headers:
 			    createUpdateCellEntry(worksheet, 1, 1, TASK);
 				createUpdateCellEntry(worksheet, 1, 2, PROJECT);
 			}
 			for (String project : projects.keySet()) {
 				for (String task : projects.get(project)) {
					ListEntry timeEntry = new ListEntry();
					timeEntry.getCustomElements().setValueLocal(TASK, task);
					service.insert(worksheetListFeedUrl, timeEntry);
 					reloadWorksheets();
 					if (!StringUtils.isEmpty(project)) {
 						String projectLink = getProjectLink(submissionSystem, project, true);
 						if (projectLink != null) {
 							WorksheetEntry worksheet = getWorksheet(submissionSystem);
							createUpdateCellEntry(worksheet, row, getHeadingIndex(submissionSystem, PROJECT), "=" + projectLink);
 						}
 					}
 				}
 			}
 		} catch (MalformedURLException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		}
 	}
 
 	private void createWorksheet(String system) throws IOException,
 			ServiceException, MalformedURLException {
 		WorksheetEntry newWorksheet = new WorksheetEntry();
 		newWorksheet.setTitle(new PlainTextConstruct(system));
 		newWorksheet.setColCount(6);
 		newWorksheet.setRowCount(20);
 		service.insert(factory.getWorksheetFeedUrl(spreadsheetKey, "private", "full"), newWorksheet);
 	}
 
 	public void submitEntries() {
         DailySubmissionEntry entry = null;
         try {
             if (!reloadWorksheets()) return;
 	        ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
 	        List<ListEntry> listEntries = feed.getEntries();
 
 	        Date lastDate = null;
 	        int i = listEntries.size() - 1;
 	        for (; i > 0; i--) {
 	            CustomElementCollection elements = listEntries.get(i).getCustomElements();
 	            if ("Submitted".equals(elements.getValue(SUBMISSION_STATUS))) break;
 
 				if (elements.getValue(TIME) == null && elements.getValue(WEEKLY_TOTAL) == null || elements.getValue(DAILY_TOTAL) != null) { // search for last complete day and break
 					lastDate = new SimpleDateFormat(dateFormat).parse(elements.getValue(DATE));
 					entry = new DailySubmissionEntry(lastDate);
 					break;
 	            }
 	        }
 	        for (; i > 0; i--) {
 	            CustomElementCollection elements = listEntries.get(i).getCustomElements();
 	            if ("Submitted".equals(elements.getValue(SUBMISSION_STATUS))) {
 	            	entry.submitEntries();
 	            	break;
 	            }
 
 	            if (elements.getValue(DATE) == null) continue;
 	            Date date = new SimpleDateFormat(dateFormat).parse(elements.getValue(DATE));
 	            if (!date.equals(lastDate)) { // another day
 	                entry.submitEntries();
 	                entry = new DailySubmissionEntry(date);
 	                lastDate = date;
 	            }
 
 	            String task = elements.getValue(TASK);
 	            if (task == null || CHECK_IN.equals(task) || BREAK.equals(task)) continue;
 	            String system = getSystem(i);
 	            String project = elements.getValue(PROJECT);
 	            if (project == null) updateTask(getTaskLink(task, project, system), i + 2);
 				if (submissionSystems.containsKey(system)) {
 			        entry.addSubmissionEntry(task, project, Double.valueOf(elements.getValue(TOTAL)),
 			            new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(submissionSystems.get(system)));
 				}
 	        }
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ParseException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		}
         if (entry != null) entry.submitEntries();
     }
 
 	private String getSystem(int row) throws IOException, ServiceException {
 		String system = getInputValue(row, headingIndex.get(TASK)).split("!")[0];
 		return system.substring(system.indexOf("=") + 1);
 	}
 
 	private String getInputValue(int row, int col) throws IOException, ServiceException,
 			MalformedURLException {
 		CellEntry cellEntry = service.getEntry(new URL(defaultWorksheet.getCellFeedUrl().toString()
 				+ "/" + "R" + (row + 2) + "C" + col), CellEntry.class);
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
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		}
 		return null;
 	}
 
 	private String getProjectLink(String system, String project, boolean createNew) {
 		String systemProjects = system + SubmissionService.PROJECTS;
 		URL worksheetListFeedUrl = getListFeedUrl(systemProjects);
 		try {
 			if (worksheetListFeedUrl == null) {
 				createWorksheet(systemProjects);
 				if (!reloadWorksheets()) return null;
 	            WorksheetEntry worksheet = getWorksheet(systemProjects);
 			    worksheetListFeedUrl = worksheet.getListFeedUrl(); 			
 				// create column headers:
 			    createUpdateCellEntry(worksheet, 1, 1, PROJECT);
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
 				service.insert(worksheetListFeedUrl, listEntry);
 	            if (!reloadWorksheets()) return null;
 				return getProjectLink(system, project, false);
 			}
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
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
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		}
 		return null;
 	}
 	
     private void createUpdateCellEntry(WorksheetEntry worksheet, int row, int col, String value) {
 		try {
 			CellEntry entry = service.getEntry(new URL(worksheet.getCellFeedUrl().toString() + "/" + "R" + row + "C" + col), CellEntry.class);
 	        entry.changeInputValueLocal(value);
 	        entry.update();
 		} catch (MalformedURLException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (IOException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError(title, e.getLocalizedMessage());
 		}
     }
 }
