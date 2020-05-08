 package ch.bli.mez.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import ch.bli.mez.model.Employee;
 import ch.bli.mez.model.Mission;
 import ch.bli.mez.model.Position;
 import ch.bli.mez.model.TimeEntry;
 import ch.bli.mez.model.dao.EmployeeDAO;
 import ch.bli.mez.model.dao.MissionDAO;
 import ch.bli.mez.model.dao.PositionDAO;
 import ch.bli.mez.model.dao.TimeEntryDAO;
 import ch.bli.mez.view.time.TimeListEntry;
 import ch.bli.mez.view.time.TimePanel;
 import ch.bli.mez.view.time.TimeView;
 
 public class TimeController {
 
   private EmployeeDAO modelemployee;
   private TimeView view;
   private TimeEntryDAO model;
   private MissionDAO missionModel;
   private PositionDAO positionModel;
   private final SearchController searchController;
   private List<Position> positions;
   private List<Mission> missions; 
 
   public TimeController() {
     this.model = new TimeEntryDAO();
     this.missionModel = new MissionDAO();
     this.positionModel = new PositionDAO();
     this.modelemployee = new EmployeeDAO();
     this.searchController = new SearchController();
     this.positions = positionModel.findAll();
     this.missions = missionModel.findAll();
 
     this.view = new TimeView(searchController.getSearchPanel());
     addTabs();
   }
 
   public TimeView getView() {
     return view;
   }
 
   private void addTabs() {
     for (Employee employee : modelemployee.findAll()) {
       view.addTab(employee.getFirstName() + " " + employee.getLastName(),
           createTimePanel(employee));
     }
   }
   
   public void update(){
     this.positions = positionModel.findAll();
     this.missions = missionModel.findAll();
   }
 
   private TimePanel createTimePanel(Employee employee) {
     TimePanel form = new TimePanel();
     setFormActionListeners(form, employee);
 
     addTimeEntrys(employee, form);
 
     return form;
   }
 
   private void addTimeEntrys(Employee employee, TimePanel timePanel) {
     for (TimeEntry timeEntry : model.findAll(employee)) {
       timePanel.addTimeListEntry(createTimeListEntry(timeEntry));
     }
   }
 
   private TimeListEntry createTimeListEntry(final TimeEntry timeEntry) {
     final TimeListEntry timeListEntry = new TimeListEntry();
 
     timeListEntry.setMission(timeEntry.getMission().getMissionName());
     Calendar calendar = timeEntry.getDate();
     Date date = calendar.getTime();
     SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy");
     String date1 = format1.format(date);  
     timeListEntry.setDate(date1);
     timeListEntry.setPosition(timeEntry.getPosition().getPositionName());
     timeListEntry.setWorktime(timeEntry.getWorktime().toString());
 
     setTimeListEntryActionListeners(timeListEntry, timeEntry);
 
     return timeListEntry;
   }
 
   public void setFormActionListeners(final TimePanel form, final Employee employee) {
     form.setSaveTimeListener(new ActionListener() {
       public void actionPerformed(ActionEvent event) {
 
         if (!validateFields(form)) {
           return;
         }
         TimeEntry timeEntry = new TimeEntry();
         timeEntry.setEmployee(employee);
        if (updateTimeEntry(timeEntry, form) != null){
          model.addTimeEntry(timeEntry);
          form.showConfirmation("Der Zeiteintrag wurde gespeichert");
          form.cleanFields();
        } else {
          form.showErrorMessage("Position oder Auftrag nicht vorhanden");
        }
       }
     });
   }
 
   public void setTimeListEntryActionListeners(
       final TimeListEntry timeListEntry, final TimeEntry timeEntry) {
 
     timeListEntry.setSaveTimeEntryListListener(new ActionListener() {
       public void actionPerformed(ActionEvent event) {
 
         if (!validateFields(timeListEntry)) {
           return;
         }
 
         timeEntry.setDate(timeEntry.getDate());
         timeEntry.setMission(timeEntry.getMission());
         timeEntry.setPosition(timeEntry.getPosition());
         timeEntry.setWorktime(timeEntry.getWorktime());
         timeListEntry.showSuccess();
         model.updateTimeEntry(timeEntry);
       }
     });
   }
   
   private Calendar createDate(String date){
     String splittedDate[] = date.split("\\.");
     Calendar calendar = Calendar.getInstance();
     calendar.set(Calendar.YEAR, Integer.parseInt(splittedDate[2]));
     calendar.set(Calendar.MONTH, Integer.parseInt(splittedDate[1])-1);
     calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(splittedDate[0]));
     return calendar;
   }
   
   private Mission findMissionByName(String missionName){
     for (Mission mission : missions) {
       if(mission.getMissionName().equals(missionName)){
         return mission;
       }
     }
     return null;
   }
   
   private Position findPositionByNumber(String number){
     for (Position position: positions){
       if(position.getNumber().equals(Integer.parseInt(number))){
         return position;
       }
     }
     return null;
   }
 
   public TimeEntry updateTimeEntry(TimeEntry timeEntry, TimePanel form) {
     timeEntry.setWorktime(Integer.parseInt(form.getWorktime()));
     timeEntry.setDate(createDate(form.getDate()));
     Position position = findPositionByNumber(form.getPosition());
     Mission mission = findMissionByName(form.getMission());
     if(position != null){
       timeEntry.setPosition(position);
    } else {
      return null;
     }
     if(mission != null){
       timeEntry.setMission(mission);
    } else {
      return null;
     }
     return timeEntry;
   }
 
   // TODO
   public boolean validateFields(TimeListEntry panel) {
     boolean valid = true;
 
     if (panel.getDate().equals("")) {
       panel.showDateError();
       valid = false;
     }
     if (panel.getMission().equals("")) {
       panel.showMissionError();
       valid = false;
     }
     if (panel.getPosition().equals("")) {
       panel.showPositionError();
       valid = false;
     }
 
     try {
       if (!panel.getWorktime().equals(""))
         Integer.parseInt(panel.getWorktime());
     } catch (NumberFormatException e) {
       panel.showWorktimeError();
       valid = false;
     }
 
     return valid;
   }
 
   public boolean validateFields(TimePanel panel) {
     boolean valid = true;
     String errortext = "Die Felder ";
 
     if (panel.getDate().equals("")) {
       panel.showDateError();
       errortext = errortext + "Datum ";
       valid = false;
     }
     if (panel.getMission().equals("")) {
       panel.showMissionError();
       errortext = errortext + "Auftrag ";
       valid = false;
     }
     if (panel.getPosition().equals("")) {
       panel.showPositionError();
       errortext = errortext + "Position ";
       valid = false;
     }
 
     try {
       if (!panel.getWorktime().equals("")) {
         Integer.parseInt(panel.getWorktime());
       } else {
         panel.showWorktimeError();
         errortext = errortext + "Zeit ";
         valid = false;
       }
     } catch (NumberFormatException e) {
       panel.showWorktimeError();
       errortext = errortext + "Zeit ";
       valid = false;
     }
 
     if (valid == false) {
       errortext = errortext + "müssen gültig ausgefüllt sein ";
       panel.showErrorMessage(errortext);
     }
 
     return valid;
   }
 }
