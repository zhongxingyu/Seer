 package ch.bli.mez.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import ch.bli.mez.model.Holiday;
 import ch.bli.mez.model.dao.HolidayDAO;
 import ch.bli.mez.view.DefaultPanel;
 import ch.bli.mez.view.management.HolidayForm;
 
 /**
  * 
  * @author dave
  * @version 1.0
  */
 public class HolidayController {
 
   private DefaultPanel view;
   private HolidayDAO model;
 
   public HolidayController() {
     this.view = new DefaultPanel();
     this.model = new HolidayDAO();
 
     addForms();
   }
 
   public DefaultPanel getView() {
     return view;
   }
 
   private void addForms() {
     view.setCreateNewForm(createHolidayForm(null));
     for (Holiday holiday : model.getGlobalHoliday()) {
       view.addForm(createHolidayForm(holiday));
     }
   }
 
   private HolidayForm createHolidayForm(Holiday holiday) {
     final HolidayForm form = new HolidayForm();
     form.setDefaultPanel(view);
 
     if (holiday == null) {
       form.showAsCreateNew();
     } else {
       form.setYear(String.valueOf(holiday.getYear()));
       form.setPublicHolidays(String.valueOf(holiday.getPublicHolidays()));
       form.setPreWorkdays(String.valueOf(holiday.getPreworkdays()));
     }
 
     setHolidayFormActionListeners(form, holiday);
 
     return form;
   }
 
   private void setHolidayFormActionListeners(final HolidayForm form, final Holiday holiday) {
     form.setSaveListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         updateHoliday(holiday, form);
       }
     });
   }
 
  public boolean validateFields(HolidayForm form) {
     if (form.validateFields()) {
      if (model.getGlobalHolidayByYear(Integer.parseInt(form.getYear())) != null) {
         form.getDefaultPanel().showError("Das eingegebene Jahr existiert bereits");
         return false;
       }
       if (form.getYear().length() != 4) {
         form.getDefaultPanel().showError("Das Jahr muss vier Stellen umfassen z.B 2014");
         return false;
       }
       form.getDefaultPanel().showConfirmation("Der Eintrag wurde gespeichert.");
       return true;
     }
     return false;
   }
 
   public void updateHoliday(Holiday holiday, HolidayForm form) {
     boolean newholiday = false;
 
     if (holiday == null) {
       newholiday = true;
       holiday = makeHoliday();
     }
 
    if (validateFields(form)) {
       holiday.setYear(Integer.parseInt(form.getYear()));
       holiday.setPublicHolidays(Integer.parseInt(form.getPublicHolidays()));
       holiday.setPreworkdays(Integer.parseInt(form.getPreWorkdays()));
 
       if (newholiday) {
         model.addHoliday(holiday);
         view.addForm(createHolidayForm(holiday));
         form.cleanFields();
       } else {
         model.updateHoliday(holiday);
       }
     }
   }
 
   public Holiday makeHoliday() {
     return new Holiday();
   }
 
 }
