 package com.chuckanutbay.webapp.timeclock.client;
 import static com.chuckanutbay.webapp.common.client.GwtWidgetHelper.H_ALIGN_CENTER;
 import static com.chuckanutbay.webapp.common.client.GwtWidgetHelper.H_ALIGN_LEFT;
 import static com.chuckanutbay.webapp.common.client.GwtWidgetHelper.H_ALIGN_RIGHT;
 import static com.chuckanutbay.webapp.common.client.GwtWidgetHelper.newImage;
 import static com.chuckanutbay.webapp.common.client.GwtWidgetHelper.newLabel;
 import static com.chuckanutbay.webapp.common.client.GwtWidgetHelper.newListBox;
 import static com.chuckanutbay.webapp.common.shared.EmployeeDto.newBlankEmployeeDto;
 import static com.chuckanutbay.webapp.timeclock.client.RpcHelper.createGetEmployeesCallback;
 import static com.chuckanutbay.webapp.timeclock.client.RpcHelper.createGetLastPayPeriodIntervalCallback;
 import static com.chuckanutbay.webapp.timeclock.client.RpcHelper.createGetPayPeriodReportDataCallback;
 import static com.chuckanutbay.webapp.timeclock.client.RpcHelper.timeClockService;
 import static com.google.common.collect.Lists.newArrayList;
 
 import java.util.Date;
 import java.util.List;
 
 import com.chuckanutbay.webapp.common.client.CbDateBox;
 import com.chuckanutbay.webapp.common.client.CbHorizontalPanel;
 import com.chuckanutbay.webapp.common.client.CbListBox;
 import com.chuckanutbay.webapp.common.client.IconUtil;
 import com.chuckanutbay.webapp.common.shared.DayReportData;
 import com.chuckanutbay.webapp.common.shared.EmployeeDto;
 import com.chuckanutbay.webapp.common.shared.EmployeeWorkIntervalDto;
 import com.chuckanutbay.webapp.common.shared.IntervalDto;
 import com.chuckanutbay.webapp.common.shared.PayPeriodReportData;
 import com.chuckanutbay.webapp.common.shared.WeekReportData;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.datepicker.client.DateBox;
 
 public class TimeClockReport implements EntryPoint, TimeClockReportHandler {
 	//Date Formats
 	//shortDayOfMonth - Mon, Sep 13
	private String SHORT_DAY_OF_MONTH_FORMAT = "EEE, MMM d";
 	
 	//UI components
 	private RootPanel rootPanel;
 	private final VerticalPanel mainPanel = new VerticalPanel();
 	private final Image logo = newImage(IconUtil.LOGO, 300);
 	private final Label header = newLabel("Pay Period Report", "header");
 	private final CbHorizontalPanel dateBoxAndShiftListBoxPanel = new CbHorizontalPanel();
 	private final DateBox startDateBox = new CbDateBox(75, SHORT_DAY_OF_MONTH_FORMAT);
 	private final DateBox endDateBox = new CbDateBox(75, SHORT_DAY_OF_MONTH_FORMAT);
 	private final CbHorizontalPanel gernateButtonPanel = new CbHorizontalPanel();
 	private final ListBox shiftListBox = newListBox("All", "1", "2", "3", "Other");
 	private final CbListBox<EmployeeDto> employeeListBox = new CbListBox<EmployeeDto>();
 	private final Button generateButton = new Button("Generate");
 	
 	private List<PayPeriodReportData> payPeriodReportData;
 	
 	private static final EmployeeDto BLANK_EMPLOYEE_DTO = newBlankEmployeeDto(00000);
 	
 	@Override
 	public void onModuleLoad() {
 		
 		//generateSampleData();
 		getEmployees();
 		getLastPayPeriodIntervalFromServer();
 		
 		//Setup shiftListBox
 		//Note that list boxes don't allow extra white space before or after the string
 		employeeListBox.addData("-", BLANK_EMPLOYEE_DTO);
 		
 		//Setup dateBoxAndShiftListBoxPanel
 		dateBoxAndShiftListBoxPanel
 			.setWidth(750)
 			.setCellSpacing(5)
 			.setStyle("dateBoxPanel")
 			.addWidget(newLabel("Start Date:", "widgetLabel"), H_ALIGN_RIGHT)
 			.addWidget(startDateBox, H_ALIGN_LEFT)
 			.addWidget(newLabel("End Date:", "widgetLabel"), H_ALIGN_RIGHT)
 			.addWidget(endDateBox, H_ALIGN_LEFT)
 			.addWidget(newLabel("Shift:", "widgetLabel"), H_ALIGN_RIGHT)
 			.addWidget(shiftListBox, H_ALIGN_LEFT)
 			.addWidget(newLabel(" - or - ", "widgetLabel"), H_ALIGN_CENTER)
 			.addWidget(newLabel("Employee:", "widgetLabel"), H_ALIGN_RIGHT)
 			.addWidget(employeeListBox, H_ALIGN_LEFT);
 		
 		//Setup gernateButtonPanel
 		generateButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
				mainPanel.clear();
 				int shift;
 				if (startDateBox.getValue() == null) {
 					Window.alert("Please enter a start date.");
 				} else if (endDateBox.getValue() == null) {
 					Window.alert("Please enter an end date.");
 				} else {
 					if (employeeListBox.getSelected().equals(BLANK_EMPLOYEE_DTO)){
 					String shiftSelection = shiftListBox.getItemText(shiftListBox.getSelectedIndex());
 					if (shiftSelection.equals("All")){
 						shift = 0;
 					} else if (shiftSelection.equals("Other")){
 						shift = 4;
 					} else {
 						shift = Integer.parseInt(shiftSelection);
 					}
 					getPayPeriodReportData(startDateBox.getValue(), endDateBox.getValue(), shift);
 				} else {
 					getPayPeriodReportData(startDateBox.getValue(), endDateBox.getValue(), employeeListBox.getSelected());
 				}
 			}
 		}});
 		
 		gernateButtonPanel
 			.setWidth(750)
 			.setCellSpacing(5)
 			.addWidget(generateButton, H_ALIGN_CENTER);
 
 		
 		//Setup mainPanel
 		mainPanel.setSpacing(5);
 		mainPanel.add(logo);
 		mainPanel.add(header);
 		mainPanel.setCellHorizontalAlignment(header, HasHorizontalAlignment.ALIGN_CENTER);
 		mainPanel.add(dateBoxAndShiftListBoxPanel);
 		mainPanel.add(gernateButtonPanel);
 		
 		//Setup rootPanel
 		rootPanel = RootPanel.get("TimeClockReport");
 		rootPanel.add(mainPanel);
 	}
 
 	@SuppressWarnings("unused")
 	private void generateSampleData() {
 		EmployeeWorkIntervalDto interval1 = new EmployeeWorkIntervalDto();
 		interval1.setStartDateTime(new Date());
 		interval1.setEndDateTime(new Date());
 		interval1.setHoursWorked(4.42);
 		
 		EmployeeWorkIntervalDto interval2 = new EmployeeWorkIntervalDto();
 		interval2.setStartDateTime(new Date());
 		interval2.setEndDateTime(new Date());
 		interval2.setHoursWorked(4.40);
 		
 		List<EmployeeWorkIntervalDto> intervals1 = newArrayList(interval1, interval2);
 		List<EmployeeWorkIntervalDto> intervals2 = newArrayList(interval2, interval1);
 		
 		DayReportData dayReportData1 = new DayReportData();
 		dayReportData1.setTotalHoursWorked(8.42);
 		dayReportData1.setEmployeeWorkIntervals(intervals1);
 		
 		DayReportData dayReportData2 = new DayReportData();
 		dayReportData2.setTotalHoursWorked(7.42);
 		dayReportData2.setEmployeeWorkIntervals(intervals2);
 		
 		List<DayReportData> DayIntervalsDtos = newArrayList(dayReportData1, dayReportData2);
 		
 		WeekReportData weekReportData1 = new WeekReportData();
 		weekReportData1.setHoursNormalPay(22.3643);
 		weekReportData1.setHoursOvertime(0);
 		weekReportData1.setDayReportData(DayIntervalsDtos);
 		
 		WeekReportData weekReportData2 = new WeekReportData();
 		weekReportData2.setHoursNormalPay(40.00);
 		weekReportData2.setHoursOvertime(6.32);
 		weekReportData2.setDayReportData(DayIntervalsDtos);
 		
 		PayPeriodReportData payPeriodReportData1 = new PayPeriodReportData();
 		payPeriodReportData1.setDate(new Date());
 		payPeriodReportData1.setPayPeriodStart(new Date());
 		payPeriodReportData1.setPayPeriodEnd(new Date());
 		payPeriodReportData1.setName("Steve Jobs");
 		payPeriodReportData1.setWeekReportData(newArrayList(weekReportData1, weekReportData2));
 		payPeriodReportData = newArrayList(payPeriodReportData1);
 	}
 
 	@Override
 	public void getLastPayPeriodIntervalFromServer() {
 		GWT.log("Requesting Start of Pay Period");
 		timeClockService.getLastPayPeriodIntervalFromServer(new Date(), createGetLastPayPeriodIntervalCallback(this));
 	}
 
 	@Override
 	public void onSuccessfulGetLastPayPeriodInterval(IntervalDto interval) {
 		GWT.log("Got Pay Period Interval");
 		startDateBox.setValue(interval.getStart());
 		endDateBox.setValue(interval.getEnd());
 	}
 	
 	@Override
 	public void getPayPeriodReportData(Date start, Date end, Integer shift) {
 		timeClockService.getPayPeriodReportDataFromDatabase(start, end, shift, createGetPayPeriodReportDataCallback(this)); 
 		
 	}
 	
 	@Override
 	public void getPayPeriodReportData(Date start, Date end,
 			EmployeeDto employee) {
 		timeClockService.getPayPeriodReportDataFromDatabase(start, end, employee, createGetPayPeriodReportDataCallback(this));
 	}
 
 	@Override
 	public void getEmployees() {
 		timeClockService.getEmployees(createGetEmployeesCallback(this));
 	}
 
 	@Override
 	public void onSuccessfulGetEmployees(List<EmployeeDto> employees) {
 		for (EmployeeDto employee : employees) {
 			employeeListBox.addData(employee.getFirstName() + " " + employee.getLastName(), employee);
 		}
 	}
 
 	@Override
 	public void onSuccessfulGetPayPeriodReportData(
 			List<PayPeriodReportData> reportData) {
 		payPeriodReportData = reportData;
 		generateReports();
 	}
 
 	private void generateReports() {
 		for (PayPeriodReportData employeeReport : payPeriodReportData) {
 			TimeClockReportPanel panel = new TimeClockReportPanel(employeeReport);
 			mainPanel.add(panel);
 		}
 	}
 
 
 }
