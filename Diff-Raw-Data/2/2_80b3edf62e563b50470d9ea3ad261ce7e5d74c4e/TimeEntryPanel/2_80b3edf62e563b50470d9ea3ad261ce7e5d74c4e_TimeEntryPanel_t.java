 package ch.bli.mez.view.time;
 
 import javax.swing.JOptionPane;
 
 import ch.bli.mez.model.Employee;
 import ch.bli.mez.util.Parser;
 import ch.bli.mez.view.DefaultPanel;
 
 public class TimeEntryPanel extends DefaultPanel {
   
   private static final long serialVersionUID = -1084526692534142942L;
   
   private Employee employee;
   
   public TimeEntryPanel(Employee employee){
     this.employee = employee;
   }
   
   public Employee getEmployee(){
     return this.employee;
   }
 
   public static Boolean showDeleteWarning(TimeEntryForm form) {
     Object[] options = { "Ja", "Nein" };
     int choice = JOptionPane.showOptionDialog(form, "Zeiteintrag wirklich löschen?\n\n Datum: "
        + form.getDate() + "\n Auftrag: " + form.getMissionName() + "\n Position: " + form.getPositionCode() + "\n Zeit: "
         + Parser.parseMinutesIntegerToString(form.getWorktime()), "Löschen bestätigen", JOptionPane.YES_NO_OPTION,
         JOptionPane.WARNING_MESSAGE, null, options, options[1]);
     if (choice == JOptionPane.YES_OPTION) {
       return true;
     } else {
       return false;
     }
   }
 }
