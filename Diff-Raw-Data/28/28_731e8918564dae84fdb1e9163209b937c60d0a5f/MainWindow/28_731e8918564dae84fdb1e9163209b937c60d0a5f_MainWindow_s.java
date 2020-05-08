 package com.gzlabs.gzroster.gui;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.Properties;
 
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.wb.swt.SWTResourceManager;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.jface.dialogs.MessageDialog;
 
 import com.gzlabs.drosterheper.DRosterHelper;
 import com.gzlabs.drosterheper.IDisplayStatus;
 import com.gzlabs.gzroster.data.DataManager;
 import com.gzlabs.gzroster.data.Tables;
 
 import org.eclipse.swt.widgets.MenuItem;
 
 /**
  * Main window for the application
  * 
  * @author apavlune
  * 
  */
 public class MainWindow implements IDisplayStatus, IDutyUpdater,
 		IEHoursHandler, IShiftAdder, IPositionsManager, IDetailsManager,
 		IEmployeeManager, ITimeOffManager {
 
 	protected Shell shell;
 	private final FormToolkit formToolkit = new FormToolkit(
 			Display.getDefault());
 	private DataManager dman;
 	private DRosterHelper drh;
 
 	/************************************************************/
 	// Main Window Container
 	private TabFolder tabFolder;
 	private Label lblStatus;
 	private EmployeeHoursComposite employeeHoursComposite;
 	private EmployeePositionComposite employeePositionComposite;
 	private AddShiftComposite addShiftComposite;
 	/************************************************************/
 
 	/************************************************************/
 	// Employee Tab
 	private EmployeesWidget employeesWidget;
 	private EmployeeTimeOffComposite employeeTimeOffComposite;
 	/************************************************************/
 
 	/************************************************************/
 	// Positions Tab
 	private TabItem tbtmDetails;
 	private PositionsWidget positionsWidget;
 
 	/************************************************************/
 
 	/************************************************************/
 	// Details Tab
 	private DetailsWidget detailsWidget;
 	/************************************************************/
 
 	/************************************************************/
 	private Label lblAllowedPositions;
 
 	/************************************************************/
 
 	/**
 	 * Launch the application.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			Display.setAppName("GZRoster");
 			MainWindow window = new MainWindow();
 			window.open();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Open the window.
 	 */
 	public void open() {
 		Display display = Display.getDefault();
 		createContents();
 		shell.open();
 		shell.layout();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	/**
 	 * Create contents of the window.
 	 */
 	protected void createContents() {
 		shell = new Shell();
 		shell.setImage(SWTResourceManager.getImage(MainWindow.class,
 				"/com/gzlabs/gzroster/gui/1370647295_60814.ico"));
 		shell.setSize(1420, 928);
 		shell.setText("GZ Roster");
 		shell.setLayout(null);
 
 		Menu menu = new Menu(shell, SWT.BAR);
 		shell.setMenuBar(menu);
 
 		MenuItem fileMenu = new MenuItem(menu, SWT.CASCADE);
 		fileMenu.setText("File");
 
 		Menu menu_2 = new Menu(fileMenu);
 		fileMenu.setMenu(menu_2);
 
 		MenuItem mntmUpload = new MenuItem(menu_2, SWT.NONE);
 
 		mntmUpload.setText("Upload");
 
 		MenuItem quitMenuItem = new MenuItem(menu_2, SWT.NONE);
 		quitMenuItem.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				System.exit(0);
 			}
 		});
 		quitMenuItem.setText("Quit");
 
 		MenuItem editMenu = new MenuItem(menu, SWT.CASCADE);
 		editMenu.setText("Edit");
 
 		Menu menu_3 = new Menu(editMenu);
 		editMenu.setMenu(menu_3);
 
 		MenuItem configurationMenuItem = new MenuItem(menu_3, SWT.NONE);
 
 		configurationMenuItem.setText("Configuration");
 
 		MenuItem mntmPurge = new MenuItem(menu_3, SWT.NONE);
 
 		MenuItem helpMenu = new MenuItem(menu, SWT.CASCADE);
 		helpMenu.setText("Help");
 
 		Menu menu_1 = new Menu(helpMenu);
 		helpMenu.setMenu(menu_1);
 
 		MenuItem helpAboutMenuItem = new MenuItem(menu_1, SWT.NONE);
 		helpAboutMenuItem.setText("About");
 
 		tabFolder = new TabFolder(shell, SWT.NONE);
 		tabFolder.setBounds(10, 10, 900, 800);
 
 		addShiftComposite = new AddShiftComposite(shell, SWT.NONE, this);
 		addShiftComposite.setBounds(916, 38, 169, 336);
 		formToolkit.adapt(addShiftComposite);
 		formToolkit.paintBordersFor(addShiftComposite);
 
 		createEmployeesTab();
 		createPositionsTab();
 
 		employeeHoursComposite = new EmployeeHoursComposite(shell, SWT.NONE,
 				this);
 		employeeHoursComposite.setBounds(1091, 38, 227, 529);
 		formToolkit.adapt(employeeHoursComposite);
 		formToolkit.paintBordersFor(employeeHoursComposite);
 
 		lblStatus = new Label(shell, SWT.NONE);
 		lblStatus.setBounds(10, 816, 1398, 18);
 		formToolkit.adapt(lblStatus, true, true);
 		lblStatus.setText("Status");
 		dman = new DataManager(this);
 		drh = new DRosterHelper(dman.getProp(), this);
 		createDetailsTab();
 		populateData();
 
 		final PurgeDataDialog pdd = new PurgeDataDialog(shell, SWT.NONE, drh,
 				this);
 
 		mntmPurge.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (pdd != null) {
 					pdd.open();
 				}
 			}
 		});
 
 		final ConfigurationDialog conf_dialog = new ConfigurationDialog(shell,
 				SWT.NONE, dman.getProp(), this);
 
 		configurationMenuItem.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (conf_dialog != null) {
 					conf_dialog.open();
 				}
 			}
 		});
 		mntmPurge.setText("Purge");
 
 		mntmUpload.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 
 				if (dman != null) {
 					SSHStatusShell.launch(dman.getProp());
 				}
 			}
 		});
 	}
 
 	/**
 	 * Resets add shift controls
 	 */
 	protected void resetScheduleControls() {
 		if (addShiftComposite != null) {
 			addShiftComposite.resetControls();
 		}
 	}
 
 	/*************************************************************************/
 	/**
 	 * Separated for readability.
 	 */
 
 	private void createDetailsTab() {
 		tbtmDetails = new TabItem(tabFolder, SWT.NONE);
 		tbtmDetails.setText("Details");
 
 		detailsWidget = new DetailsWidget(tabFolder, SWT.NONE, this, this);
 		detailsWidget.setLayout(new GridLayout(1, false));
 		tbtmDetails.setControl(detailsWidget);
 	}
 
 	/**
 	 * Initiates details data
 	 * 
 	 * @param data
 	 *            List of duties
 	 */
 	protected void updateDetailsData(ArrayList<String> data) {
 		if (detailsWidget != null && data != null) {
 			detailsWidget.initiateData(data);
 		}
 	}
 
 	/**
 	 * Creates employees tab.
 	 */
 	protected void createEmployeesTab() {
 
 		TabItem tbtmEmployees = new TabItem(tabFolder, SWT.NONE);
 		tbtmEmployees.setText("Employees");
 
 		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
 		tbtmEmployees.setControl(composite_1);
 		composite_1.setBounds(0, 27, 900, 800);
 		formToolkit.adapt(composite_1);
 		formToolkit.paintBordersFor(composite_1);
 
 		employeesWidget = new EmployeesWidget(composite_1, SWT.NONE, this);
 		employeesWidget.setBounds(10, 10, 640, 376);
 		formToolkit.adapt(employeesWidget);
 		formToolkit.paintBordersFor(employeesWidget);
 
 		employeePositionComposite = new EmployeePositionComposite(composite_1,
 				SWT.NONE);
 		employeePositionComposite.setBounds(680, 32, 182, 390);
 		formToolkit.adapt(employeePositionComposite);
 		formToolkit.paintBordersFor(employeePositionComposite);
 
 		lblAllowedPositions = new Label(composite_1, SWT.CENTER);
 		lblAllowedPositions.setBounds(701, 10, 130, 18);
 		formToolkit.adapt(lblAllowedPositions, true, true);
 		lblAllowedPositions.setText("Allowed Positions");
 
 		employeeTimeOffComposite = new EmployeeTimeOffComposite(composite_1,
 				SWT.NONE, this);
 		employeeTimeOffComposite.setBounds(10, 428, 418, 303);
 		formToolkit.adapt(employeeTimeOffComposite);
 		formToolkit.paintBordersFor(employeeTimeOffComposite);
 
 		Label lblTimeOff = new Label(composite_1, SWT.CENTER);
 		lblTimeOff.setBounds(168, 404, 69, 18);
 		formToolkit.adapt(lblTimeOff, true, true);
 		lblTimeOff.setText("Time Off");
 	}
 
 	/**
 	 * Creates positions tab
 	 */
 	protected void createPositionsTab() {
 		TabItem tbtmPositions = new TabItem(tabFolder, SWT.NONE);
 		tbtmPositions.setText("Positions");
 		positionsWidget = new PositionsWidget(tabFolder, SWT.NONE, this);
 		tbtmPositions.setControl(positionsWidget);
 		positionsWidget.setBounds(10, 10, 431, 341);
 		formToolkit.adapt(positionsWidget);
 		formToolkit.paintBordersFor(positionsWidget);
 	}
 
 	/*************************************************************************/
 
 	/**
 	 * Populates objects with data
 	 */
 	public void populateData() {
 		ArrayList<String> employees = dman.getEmployees();
 
 		if (addShiftComposite != null && employeesWidget != null
 				&& employees != null) {
 			addShiftComposite.clearControls();
 			addShiftComposite.addEmployee(employees);
 
 			employeesWidget.clearEmployeesList();
 			employeesWidget.addEmployee(employees);
 
 			Collections.sort(employees);
 			for (String s : employees) {
 
 				if (employeeHoursComposite != null) {
 					employeeHoursComposite.updateItem(s, dman
 							.getTotalEmpoloyeeHours(s,
 									employeeHoursComposite.getStartDate(),
 									employeeHoursComposite.getEndDate()));
 				}
 			}
 
 			ArrayList<String> positions = dman.getPositions();
 
 			if (positionsWidget != null && positions!=null) {
 				positionsWidget.clearPositionsList();
 				Collections.sort(positions);
 				for (String s : positions) {
 					positionsWidget.addPosition(s);
 					addShiftComposite.addPosition(s);
 
 					if (employeePositionComposite != null)
 						employeePositionComposite.addButton(s);
 				}
 			}
 
 			ArrayList<String> timespans = dman.getTimeSpan();
 			
 			if(timespans!=null)
 			{
 				for (String s : timespans) {
 					addShiftComposite.addStart(s);
 				}
 			}
 			updateDetailsData(timespans);
 		}
 
 	}
 
 	/**
 	 * Displays current status in a text area window.
 	 */
 	@Override
 	public void DisplayStatus(String status) {		
 		WidgetUtilities.safeLabelSet(lblStatus, status);
 		System.out.println(status);
 	}
 
 	/**
 	 * Displays error dialog
 	 */
 	@Override
 	public void ShowErrorBox(String error) {
 		MessageDialog.openError(shell, "Error", error);
 	}
 
 	/**
 	 * Sets employee details to the specified HashMap values
 	 * 
 	 * @param details
 	 *            HashMap with values.
 	 */
 	public void setEmployeeDetails(String string) {
 
 		ArrayList<String> details = null;
 		if (dman != null) {
 			details = dman.getEmployeeDetails(string);
 			if (details != null && employeesWidget != null) {
 				String value = details.get(Tables.PERSON_NAME_INDEX);
 				if (value != null) {
 					employeesWidget.setNameText(value);
 				}
 
 				value = details.get(Tables.PERSON_ADDRESS_INDEX);
 				if (value != null) {
 					employeesWidget.setAddressText(value);
 				}
 
 				value = details.get(Tables.PERSON_HPHONE_INDEX);
 				if (value != null) {
 					employeesWidget.setHomephoneText(value);
 				}
 
 				value = details.get(Tables.PERSON_MPHONE_INDEX);
 				if (value != null) {
 					employeesWidget.setMobilePhoneText(value);
 				}
 
 				value = details.get(Tables.PERSON_ACTIVE_INDEX);
 				if (value != null && value.equals("1")) {
 					employeesWidget.setActiveCheck(true);
 				} else {
 					employeesWidget.setActiveCheck(false);
 				}
 
 				value = details.get(Tables.PERSON_EMAIL_INDEX);
 				if (value != null) {
 					employeesWidget.setEmailText(value);
 				}
 
 				if (employeePositionComposite != null && dman != null) {
 					employeePositionComposite.checkBoxes(dman
 							.getPersonToPosMapping(employeesWidget
 									.getNameText()));
 				}
 
 				if (employeeTimeOffComposite != null) {
 					employeeTimeOffComposite.toggleButtonVisibility(true);
 					employeeTimeOffComposite.showTimeOff(dman
 							.getTimeOff(string));
 				}
 			}
 
 		}
 	}
 
 	/**
 	 * Sets position details to the specified HashMap values
 	 * 
 	 * @param details
 	 *            HashMap with values.
 	 */
 	public void setPositionDetails(String string) {
 
 		if (dman != null) {
 			ArrayList<String> details = dman.getPositionDetailsByName(string);
 
 			if (details != null && positionsWidget != null) {
 				String value = details.get(Tables.PLACE_NAME_INDEX);
 				if (value != null) {
 					positionsWidget.setPositionNameText(value);
 				}
 
 				value = details.get(Tables.PLACE_NOTE_INDEX);
 				if (value != null) {
 					positionsWidget.setPositionNoteText(value);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Updates existing employee or inserts new one.
 	 */
 	public void processEmployeeData() {
 		if (employeesWidget != null) {
 			ArrayList<String> new_details = new ArrayList<String>();
 			WidgetUtilities.safeArrayStringListAdd(new_details, "", true);
 			WidgetUtilities.safeArrayStringListAdd(new_details, employeesWidget.getNameText(), true);
 			WidgetUtilities.safeArrayStringListAdd(new_details, employeesWidget.getAddressText(), true);
 			WidgetUtilities.safeArrayStringListAdd(new_details, employeesWidget.getHomephoneText(), true);
 			WidgetUtilities.safeArrayStringListAdd(new_details, employeesWidget.getMobilePhoneText(), true);
 			WidgetUtilities.safeArrayStringListAdd(new_details, "", true);
 			WidgetUtilities.safeArrayStringListAdd(new_details, employeesWidget.getActiveCheck() ? "1" : "0", true);
 			WidgetUtilities.safeArrayStringListAdd(new_details, "", true);
 			WidgetUtilities.safeArrayStringListAdd(new_details, employeesWidget.getEmailText(), true);
 
 			ArrayList<String> old_details = new ArrayList<String>(
 					Tables.PLACE_MAX_COLS);
 			WidgetUtilities.safeArrayStringListAdd(old_details, "", true);
 			WidgetUtilities.safeArrayStringListAdd(old_details, employeesWidget.getOld_name(), true);
 			WidgetUtilities.safeArrayStringListAdd(old_details, employeesWidget.getOld_address(), true);
 			WidgetUtilities.safeArrayStringListAdd(old_details, employeesWidget.getOld_hphone(), true);
 			WidgetUtilities.safeArrayStringListAdd(old_details, employeesWidget.getOld_mphone(), true);
 			WidgetUtilities.safeArrayStringListAdd(old_details, "", true);
 			WidgetUtilities.safeArrayStringListAdd(old_details, employeesWidget.getOld_active(), true);
 			WidgetUtilities.safeArrayStringListAdd(old_details, employeesWidget.getOld_email(), true);
 			WidgetUtilities.safeArrayStringListAdd(old_details, "", true);
 
 			ArrayList<String> position_boxes = null;
 			if (employeePositionComposite != null) {
 				position_boxes = employeePositionComposite.getBoxes();
 			}
 
 			if (employeesWidget.getSelectionIndex() >= 0 && dman!=null) {
 				dman.updateEmployee(old_details, new_details, position_boxes);
 			} else {
 				dman.addEmployee(new_details, position_boxes);
 			}
 			employeesWidget.resetControls();
 			populateData();
 		}
 	}
 
 	/**
 	 * Updates existing duty or inserts new one.
 	 */
 	@Override
 	public void processDutyData() {
 		if (addShiftComposite != null && addShiftComposite.checkTextLength() && dman!=null) {
 
 			String start_date = addShiftComposite.getSelectedDate() + " "
 					+ addShiftComposite.getSelectedStart() + ":00.0";
 
 			String end_date = addShiftComposite.getSelectedDate() + " "
 					+ addShiftComposite.getSelectedEnd() + ":00.0";
 
 			ArrayList<String> details = new ArrayList<String>();
 			WidgetUtilities.safeArrayStringListAdd(details, start_date, true);
 			WidgetUtilities.safeArrayStringListAdd(details, addShiftComposite.getSelectedPosition(), true);
 			WidgetUtilities.safeArrayStringListAdd(details, addShiftComposite.getSelectedEmployee(), true);
 			WidgetUtilities.safeArrayStringListAdd(details, end_date, true);
 			WidgetUtilities.safeArrayStringListAdd(details, "", true);	
 
 			if (dman.checkDutyConflict(addShiftComposite.getSelectedEmployee(),
 					start_date, end_date)) {
 				DisplayStatus("Scheduling conflict!");
 				if (!MessageDialog
 						.openConfirm(shell, "Schedule Conflict",
 								"This employee is already scheduled during this time period.  Add anyway?")) {
 					DisplayStatus("Cancelled!");
 					return;
 				}
 			}
 			dman.addDuty(details);
 			populateData();
 		} else {
 			DisplayStatus("Both Employee and Position need to be selected!  Both Start and End times are required!");
 		}
 	}
 
 	/**
 	 * Updates existing position or inserts new one.
 	 */
 	public void processPositionData() {
 
 		if (positionsWidget != null) {
 			ArrayList<String> new_details = new ArrayList<String>();
 			WidgetUtilities.safeArrayStringListAdd(new_details, "", true);	
 			WidgetUtilities.safeArrayStringListAdd(new_details, positionsWidget.getPositionNameText(), true);	
 			WidgetUtilities.safeArrayStringListAdd(new_details, positionsWidget.getPositionNoteText(), true);	
 	
 			ArrayList<String> old_details = new ArrayList<String>(
 					Tables.PLACE_MAX_COLS);
 			WidgetUtilities.safeArrayStringListAdd(old_details, "", true);	
 			WidgetUtilities.safeArrayStringListAdd(old_details, positionsWidget.getOld_name(), true);	
 			WidgetUtilities.safeArrayStringListAdd(old_details, positionsWidget.getOld_note(), true);	
 
 			if (positionsWidget.getSelectionIndex() >= 0 && dman!=null) {
 				dman.updatePosition(old_details, new_details);
 			} else {
 				dman.addPosition(new_details);
 			}
 		}
 	}
 
 	@Override
 	public void dutyUpdateRequest(String label, String col_label,
 			String row_label) {
 
 		if (addShiftComposite != null && dman != null) {
 			addShiftComposite.setUpd_person(label);
 			addShiftComposite.setUpd_position(col_label);
 			addShiftComposite.selectEmployee(label);
 			addShiftComposite.selectPosition(col_label);
 
 			String start_time = dman.getDutyStart(label, col_label,
 					addShiftComposite.getSelectedDate() + " " + row_label
 							+ ":00.0");
 
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
 			Calendar time_cal = new GregorianCalendar();
 
 			String end_time = dman.getDutyEnd(label, col_label,
 					addShiftComposite.getSelectedDate() + " " + row_label
 							+ ":00.0");
 			try {
 				String zminute = "00";
 				time_cal.setTime(sdf.parse(start_time));
 				start_time = time_cal.get(Calendar.HOUR_OF_DAY)
 						+ ":"
 						+ (time_cal.get(Calendar.MINUTE) == 0 ? zminute
 								: time_cal.get(Calendar.MINUTE));
 				time_cal.setTime(sdf.parse(end_time));
 				end_time = time_cal.get(Calendar.HOUR_OF_DAY)
 						+ ":"
 						+ (time_cal.get(Calendar.MINUTE) == 0 ? zminute
 								: time_cal.get(Calendar.MINUTE));
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 
 			addShiftComposite.setEndEnabled(true);
 			addShiftComposite.setUpd_start(start_time);
 			addShiftComposite.selectStart(start_time);
 
 			addShiftComposite.correlateEndTimeCombo();
 			addShiftComposite.selectEnd(end_time);
 			addShiftComposite.setButtonsToUpdate();
 			addShiftComposite.enableEmployeePicker();
 		}
 	}
 
 	@Override
 	public void dutyDeleteRequest(String label, String col_label,
 			String row_label) {
 		if (dman != null && addShiftComposite!=null) {
 			String start_date = dman.getDutyStart(label, col_label,
 					addShiftComposite.getSelectedDate() + " " + row_label
 							+ ":00.0");
 
 			String end_date = dman.getDutyEnd(label, col_label,
 					addShiftComposite.getSelectedDate() + " " + row_label
 							+ ":00.0");
 
 			ArrayList<String> details = new ArrayList<String>();
 			WidgetUtilities.safeArrayStringListAdd(details, start_date, true);	
 			WidgetUtilities.safeArrayStringListAdd(details, col_label, true);	
 			WidgetUtilities.safeArrayStringListAdd(details, label, true);	
 			WidgetUtilities.safeArrayStringListAdd(details, end_date, true);	
 			WidgetUtilities.safeArrayStringListAdd(details, "", true);	
 
 			dman.deleteDuty(details);
 		}
 
 	}
 
 	@Override
 	public void dutyNewRequest(String col_label, String row_label) {
 
		if (addShiftComposite != null && dman != null) {
 			addShiftComposite.selectPosition(col_label);
 			addShiftComposite.selectStart(row_label);
 			addShiftComposite.correlateEndTimeCombo();
 			isa_updateEmployeeList();
 		}
 
 	}
 
 	@Override
 	public void rangeChanged(String dateStringFromWidget,
 			String dateStringFromWidget2) {
 		if (dman != null) {
 			ArrayList<String> employees = dman.getEmployees();
 			
 			if(employees!=null && employeeHoursComposite!=null )
 			{
 				for (String s : employees) {
 					employeeHoursComposite.updateItem(s, dman
 							.getTotalEmpoloyeeHours(s,
 									employeeHoursComposite.getStartDate(),
 									employeeHoursComposite.getEndDate()));
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public void isa_DateChaged(String new_date) {
 		if (dman != null && employeeHoursComposite != null
 				&& addShiftComposite != null) {
 			updateDetailsData(dman.getTimeSpan());
 			employeeHoursComposite.updateDates(new_date);
 			isa_updateEmployeeList();
 		}
 
 	}
 
 	@Override
 	public ArrayList<String> getTimeSpan() {
 		if (dman != null) {
 			return dman.getTimeSpan();
 		}
 		return null;
 
 	}
 
 	@Override
 	public void deletePosition(String[] selection) {
		if (dman != null && tbtmDetails!=null) {
 			dman.deletePosition(selection);
 			detailsWidget = new DetailsWidget(tabFolder, SWT.NONE, this, this);
 			detailsWidget.setLayout(new GridLayout(1, false));
 			tbtmDetails.setControl(detailsWidget);
			populateData();
 		}

 	}
 
 	@Override
 	public ArrayList<String> getPositions() {
 		if (dman != null) {
 			return dman.getPositions();
 		}
 		return null;
 	}
 
 	@Override
 	public String getCellLabelString(String element, String column_label) {
 		if (dman != null && addShiftComposite != null) {
 			return dman.getCellLabelString((String) element, column_label,
 					addShiftComposite.getSelectedDate());
 		}
 		return null;
 	}
 
 	@Override
 	public void deleteEmployee(String[] selection) {
 		if (dman != null) {
 			dman.deleteEmployee(selection);
 		}
 
 	}
 
 	@Override
 	public void newTimeOffRequest(String start, String end) {
 		if (employeesWidget != null && dman != null) {
 			dman.newTimeOffRequest(start, end, employeesWidget.getNameText());
 
 			if (employeeTimeOffComposite != null)
 				employeeTimeOffComposite.showTimeOff(dman
 						.getTimeOff(employeesWidget.getNameText()));
 		}
 
 	}
 
 	@Override
 	public boolean deleteTimeOffRequest(String start, String end) {
 		if (employeesWidget != null && dman != null) {
 			return dman.deleteTimeOffRequest(start, end,
 					employeesWidget.getNameText());
 		}
 		return false;
 
 	}
 
 	@Override
 	public void isa_updateEmployeeList() {
 		if (addShiftComposite != null && dman != null) {
 			// This is where we determine if an employee is allowed to work
 			// this position
 			String position = addShiftComposite.getSelectedPosition();
 			String start = addShiftComposite.getSelectedStart();
 			if (start!=null && start.length() == 0) {
 				start = "00:00";
 			}
 			String end = addShiftComposite.getSelectedEnd();
 			if (end!=null && end.length() == 0) {
 				end = "00:00";
 			}
 
 			String start_dt = addShiftComposite.getSelectedDate() + " " + start
 					+ ":00.0";
 			String end_dt = addShiftComposite.getSelectedDate() + " " + end
 					+ ":00.0";
 
 			ArrayList<String> allowed_empl = dman.getAllowedEmployees(position,
 					start_dt, end_dt);
 			addShiftComposite.clearEmployees();
 			addShiftComposite.addEmployee(allowed_empl);
 			addShiftComposite.enableEmployeePicker();
 			addShiftComposite.selectEmployee(addShiftComposite.getUpd_person());
 
 		}
 	}
 
 	@Override
 	public void refreshData() {
 		if (dman != null) {
 			dman.refreshDutyList();
 		}
 		populateData();
 
 	}
 
 	@Override
 	public void updateProperties(Properties prop) {
 		if (dman != null && prop!=null) {
 			dman.saveProp(prop);
 			refreshData();
 		}
 
 	}
 }
