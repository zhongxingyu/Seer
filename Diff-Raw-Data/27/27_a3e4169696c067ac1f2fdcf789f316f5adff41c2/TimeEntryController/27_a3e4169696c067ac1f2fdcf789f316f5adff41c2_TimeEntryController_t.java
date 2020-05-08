 package ch.bli.mez.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.List;
 
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
 
   public TimeEntryController() {
     this.model = new TimeEntryDAO();
     this.missionModel = new MissionDAO();
     this.positionModel = new PositionDAO();
     this.employeeModel = new EmployeeDAO();
     setView();
     addTabs();
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
         TimeEntryPanel timeEntryPanel = (TimeEntryPanel) timeEntrySearchPanel.getParentPanel();
         addForms(timeEntrySearchPanel.getSearchText(), timeEntryPanel, timeEntryPanel.getEmployee());
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
     addEmployeeTabs(employeeModel.findAll());
   }
 
   private void addEmployeeTabs(List<Employee> employeeList) {
     view.removeAllTabs();
     for (Employee employee : employeeList) {
       addEmployeeTab(employee);
     }
   }
 
   private void addEmployeeTab(Employee employee) {
     view.addTab(employee.getFirstName() + " " + employee.getLastName(), createTimeEntryPanel(employee));
   }
 
   public void updateTimeView() {
     addTabs();
   }
 
   private TimeEntryPanel createTimeEntryPanel(Employee employee) {
     TimeEntryPanel panel = new TimeEntryPanel(employee);
     panel.setCreateNewForm(createTimeEntryForm(null, employee));
     TimeEntrySearchPanel timeEntrySearchPanel = new TimeEntrySearchPanel();
     timeEntrySearchPanel.setKeyListener(createTimeEntrySearchKeyListener(timeEntrySearchPanel));
     timeEntrySearchPanel.setParentPanel(panel);
     panel.setListSearchPanel(timeEntrySearchPanel);
     panel.setListTitlePanel(new TimeEntryTitlePanel());
     addForms(panel, employee);
     return panel;
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
 
   private void setTimeEntryFormActionListeners(final TimeEntryForm form, final TimeEntry timeEntry,
       final Employee employee) {
     form.setSaveListener((new ActionListener() {
       public void actionPerformed(ActionEvent event) {
         updateTimeEntry(timeEntry, form, employee);
       }
     }));
 
     form.setDeleteListener((new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         if ((TimeEntryPanel.showDeleteWarning(form))) {
           model.deleteTimeEntry(timeEntry.getId());
           form.getParent().remove(form);
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
       if (mission == null) {
         form.getParentPanel().showError("Der eingegebene Auftrag existiert nicht.");
         return false;
       }
       if (position == null) {
         form.getParentPanel().showError("Die eingegebene Position existiert nicht.");
         return false;
       }
       if (!mission.getPositions().contains(position)) {
         form.getParentPanel().showError(
             "Der Auftrag " + mission.getMissionName() + " hat keine Position mit Code " + position.getCode() + ".");
         return false;
       }
       form.getParentPanel().showConfirmation("Der Eintrag wurde gespeichert.");
       return true;
     }
     return false;
   }
 }
