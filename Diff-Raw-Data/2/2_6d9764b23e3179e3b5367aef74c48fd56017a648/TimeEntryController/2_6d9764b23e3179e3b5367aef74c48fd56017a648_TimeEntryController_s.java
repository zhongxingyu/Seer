 package ch.bli.mez.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.List;
 
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import ch.bli.mez.model.Employee;
 import ch.bli.mez.model.Mission;
 import ch.bli.mez.model.Position;
 import ch.bli.mez.model.TimeEntry;
 import ch.bli.mez.model.dao.EmployeeDAO;
 import ch.bli.mez.model.dao.MissionDAO;
 import ch.bli.mez.model.dao.PositionDAO;
 import ch.bli.mez.model.dao.TimeEntryDAO;
 import ch.bli.mez.util.Parser;
 import ch.bli.mez.view.EmployeeTabbedView;
 import ch.bli.mez.view.employee.EmployeeSearchPanel;
 import ch.bli.mez.view.time.TimeEntryForm;
 import ch.bli.mez.view.time.TimeEntryPanel;
 import ch.bli.mez.view.time.TimeEntrySearchPanel;
 import ch.bli.mez.view.time.TimeEntryTitlePanel;
 
 public class TimeEntryController {
 
   private EmployeeDAO employeeModel;
   private EmployeeTabbedView view;
   private TimeEntryDAO model;
   private MissionDAO missionModel;
   private PositionDAO positionModel;
   private EmployeeSearchPanel employeeSearchPanel;
   private TimeEntryWeekSummaryController weekSummaryController;
 
   public TimeEntryController() {
     this.model = new TimeEntryDAO();
     this.missionModel = new MissionDAO();
     this.positionModel = new PositionDAO();
     this.employeeModel = new EmployeeDAO();
     setView();
     addTabs();
     setTabListener();
   }
 
   public EmployeeTabbedView getView() {
     return view;
   }
 
   private void setView() {
     this.view = new EmployeeTabbedView();
     this.employeeSearchPanel = new EmployeeSearchPanel();
     this.view.setEmployeeSearchPanel(employeeSearchPanel);
     employeeSearchPanel.setKeyListener(createEmployeeSearchKeyListener());
   }
 
   private KeyListener createTimeEntrySearchKeyListener(final TimeEntrySearchPanel timeEntrySearchPanel) {
     return new KeyListener() {
       public void keyPressed(KeyEvent arg0) {
       }
 
       public void keyReleased(KeyEvent arg0) {
     	  timeEntrySearch(timeEntrySearchPanel);
       }
 
       public void keyTyped(KeyEvent arg0) {
       }
     };
   }
 
   private KeyListener createEmployeeSearchKeyListener() {
     return new KeyListener() {
       public void keyTyped(KeyEvent e) {
       }
 
       public void keyReleased(KeyEvent e) {
         addTabs(employeeSearchPanel.getSearchText());
       }
 
       public void keyPressed(KeyEvent e) {
       }
     };
   }
 
   private void addTabs(String employeeName) {
     addEmployeeTabs(employeeModel.findByKeywords(employeeName));
   }
 
   private void addTabs() {
     addEmployeeTabs(employeeModel.findActive());
     TimeEntryPanel panel = (TimeEntryPanel) view.getSelectedComponent();
     prepareViewOfTimeEntryPanel(panel.getEmployee(), panel);
   }
 
   private void addEmployeeTabs(List<Employee> employeeList) {
     view.removeAllTabs();
     for (Employee employee : employeeList) {
       addEmployeeTab(employee);
     }
   }
 
   private void addEmployeeTab(Employee employee) {
     TimeEntryPanel panel = createEmptyTimeEntryPanel(employee);
     view.addTab(employee.getLastName() + " " + employee.getFirstName(), panel);
   }
 
   public void updateTimeView() {
     int tabAmount = view.getTabCount();
     int activeEmployees = employeeModel.findActive().size();
     if (tabAmount < activeEmployees){
       for (int i = tabAmount; i < activeEmployees; i++) {
         addEmployeeTab(employeeModel.getEmployee(i + 1));
       }
     }
   }
 
   private TimeEntryPanel createEmptyTimeEntryPanel(Employee employee) {
     TimeEntryPanel panel = new TimeEntryPanel(employee);
     return panel;
   }
   
   private void prepareViewOfTimeEntryPanel(Employee employee, TimeEntryPanel panel){
     weekSummaryController = new TimeEntryWeekSummaryController(employee);
     panel.setWeekSummaryPanel(weekSummaryController.getView());
     panel.setCreateNewForm(createTimeEntryForm(null, employee));
     TimeEntrySearchPanel timeEntrySearchPanel = new TimeEntrySearchPanel();
     timeEntrySearchPanel.setKeyListener(createTimeEntrySearchKeyListener(timeEntrySearchPanel));
     timeEntrySearchPanel.setParentPanel(panel);
     panel.setListSearchPanel(timeEntrySearchPanel);
     panel.setListTitlePanel(new TimeEntryTitlePanel());
     addForms(panel, employee);
     panel.setIsPrepared(true);
     weekSummaryController.updateWeekSummary();
   }
 
   private void addForms(TimeEntryPanel panel, Employee employee) {
     for (TimeEntry timeEntry : model.findAll(employee)) {
       panel.addForm(createTimeEntryForm(timeEntry, employee));
     }
   }
 
   private void addForms(String searchString, TimeEntryPanel panel, Employee employee) {
     panel.removeAllForms();
     for (TimeEntry timeEntry : model.findByKeywords(searchString + "&employeeID=" + employee.getId())) {
       panel.addForm(createTimeEntryForm(timeEntry, employee));
     }
   }
 
   private TimeEntryForm createTimeEntryForm(TimeEntry timeEntry, Employee employee) {
     TimeEntryForm form = new TimeEntryForm();
     if (timeEntry != null) {
       form.setMission(timeEntry.getMission().getMissionName());
       form.setDate(timeEntry.getDate());
       form.setPosition(timeEntry.getPosition().getCode());
       form.setWorktime(timeEntry.getWorktime());
     }
     setTimeEntryFormActionListeners(form, timeEntry, employee);
     return form;
   }
   
   private void setTabListener(){
     view.setTabListener(new ChangeListener() {
       public void stateChanged(ChangeEvent e) {
         TimeEntryPanel panel = (TimeEntryPanel) ((JTabbedPane) e.getSource()).getSelectedComponent();
        if (panel.getIsPrepared()){
           return;
         }
         Employee employee = panel.getEmployee();
         prepareViewOfTimeEntryPanel(employee, panel);
       }
     });
   }
 
   private void setTimeEntryFormActionListeners(final TimeEntryForm form, final TimeEntry timeEntry,
       final Employee employee) {
     final TimeEntryWeekSummaryController controller = weekSummaryController;
     form.setSaveListener((new ActionListener() {
       public void actionPerformed(ActionEvent event) {
         updateTimeEntry(timeEntry, form, employee);
         controller.updateWeekSummary();
       }
     }));
 
     form.setDeleteListener((new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         if ((TimeEntryPanel.showDeleteWarning(form))) {
           model.deleteTimeEntry(timeEntry.getId());
           form.getParent().remove(form);
           controller.updateWeekSummary();
         }
       }
     }));
   }
 
   public void updateTimeEntry(TimeEntry timeEntry, TimeEntryForm form, Employee employee) {
     if (validateFields(form)) {
       if (timeEntry == null) {
         timeEntry = new TimeEntry();
         timeEntry.setDate(Parser.parseDateStringToCalendar(form.getDate()));
         timeEntry.setMission(findMissionByName(form.getMissionName()));
         timeEntry.setPosition(findPositionByCode(form.getPositionCode()));
         timeEntry.setWorktime(form.getWorktime());
         timeEntry.setEmployee(employee);
         model.addTimeEntry(timeEntry);
         form.getParentPanel().addForm(createTimeEntryForm(timeEntry, employee));
         form.cleanFields();
       } else {
         timeEntry.setDate(Parser.parseDateStringToCalendar(form.getDate()));
         timeEntry.setMission(findMissionByName(form.getMissionName()));
         timeEntry.setPosition(findPositionByCode(form.getPositionCode()));
         timeEntry.setWorktime(form.getWorktime());
         timeEntry.setEmployee(employee);
         model.updateTimeEntry(timeEntry);
       }
     }
   }
 
   private Mission findMissionByName(String missionName) {
     return missionModel.findByMissionName(missionName);
   }
 
   private Position findPositionByCode(String code) {
     return positionModel.findByCode(code);
   }
 
   public boolean validateFields(TimeEntryForm form) {
     if (form.validateFields()) {
       Position position = findPositionByCode(form.getPositionCode());
       Mission mission = findMissionByName(form.getMissionName());
       if (mission == null ) {
         form.getParentPanel().showError("Der eingegebene Auftrag existiert nicht.");
         return false;
       }
       if (!mission.getIsActive()){
         form.getParentPanel().showError("Der eingegebene Auftrag ist nicht aktiv.");
         return false;
       }
       if (position == null) {
         form.getParentPanel().showError("Die eingegebene Position existiert nicht.");
         return false;
       }
       if (!position.getIsActive()){
         form.getParentPanel().showError("Die eingegebene Position ist nicht aktiv.");
         return false;
       }
       if (!position.getMissions().contains(mission)) {
         form.getParentPanel().showError(
             "Der Auftrag " + mission.getMissionName() + " hat keine Position mit Code " + position.getCode() + ".");
         return false;
       }
       form.getParentPanel().showConfirmation("Der Eintrag wurde gespeichert.");
       return true;
     }
     return false;
   }
   
  public void timeEntrySearch(TimeEntrySearchPanel timeEntrySearchPanel){
   TimeEntryPanel timeEntryPanel = (TimeEntryPanel) timeEntrySearchPanel.getParentPanel();
 //  
 //  String searchString = "";
 //  
 //  try {
 //      if(Parser.parseDateStringToCalendar(timeEntrySearchPanel.getDate()) != null){
 //    	  searchString += "date=" + timeEntrySearchPanel.getDate();
 //      }
 //    } catch (NumberFormatException e){
 //    }
 //
 //  Mission mission = findMissionByName(timeEntrySearchPanel.getMissionName());
 //  if(mission != null){
 //	  
 //	  if(searchString.length()>1){searchString += "&";}
 //	  searchString += "missionName=" + timeEntrySearchPanel.getMissionName();
 //  }
 //  
 //  Position position = findPositionByCode(timeEntrySearchPanel.getPositionCode());
 //  if(position != null){
 //	  if(searchString.length()>1){searchString += "&";}
 //	  searchString += "positionCode=" + timeEntrySearchPanel.getPositionCode();
 //  }
 //  
 //  if( timeEntrySearchPanel.getWorktime().length() > 0 && (timeEntrySearchPanel.getWorktime().matches("[0-9]*[:,.]{1}[0-9]{2}") || timeEntrySearchPanel.getWorktime().matches("[0-9]*"))){
 //	  if(searchString.length()>1){searchString += "&";}
 //	  searchString += "worktime=" + timeEntrySearchPanel.getWorktime();
 //  }
 //  
   //TODO
   addForms(timeEntrySearchPanel.getSearchText(), timeEntryPanel, timeEntryPanel.getEmployee());
   
  }
  
 }
