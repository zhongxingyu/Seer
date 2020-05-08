 package de.aidger.utils;
 
 import static de.aidger.utils.Translation._;
 import de.aidger.model.Runtime;
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Employment;
import de.aidger.view.UI;
import de.aidger.view.models.AssistantTableModel;
import de.aidger.view.tabs.Tab;
import de.aidger.view.tabs.ViewerTab;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IAssistant;
import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 /**
  * Anonymizes assistants after a specified period of time.
  *
  * @author aidGer Team
  */
 public class Anonymizer {
 
     /**
      * Anonymizes assistants after a specified peroid of time.
      *
      * @return The count of anonymized assistants
      */
     public static int anonymizeAssistants() {
         int count = 0;
         int time = -1 * Integer.parseInt(Runtime.getInstance().getOption(
                 "anonymize-time"));
 
         Calendar checkCal = new GregorianCalendar();
         checkCal.add(Calendar.DAY_OF_YEAR, time);
         Date checkDate = checkCal.getTime();
 

        // Try to find any existing Assistant Viewer Tabs to update them correctly
        List<AssistantTableModel> models = new ArrayList<AssistantTableModel>();
        for (Tab t : UI.getInstance().getTabs()) {
            if (t instanceof ViewerTab) {
                ViewerTab v = (ViewerTab) t;
                if (v.getTableModel() instanceof AssistantTableModel) {
                    models.add((AssistantTableModel) v.getTableModel());
                }
            }
        }


         try {
             List<IAssistant> assistants = (new Assistant()).getAll();
 
             for (IAssistant a : assistants) {
                 if (a.getFirstName().equals("*****")) {
                     continue;
                 }
 
                 Date latest = new Date(1);
                 List<Employment> employments = (new Employment())
                         .getEmployments(new Assistant(a));
 
                 for (Employment e : employments) {
                     Calendar gc = new GregorianCalendar(e.getYear(), e.getMonth(), 1);
                     if (gc.getTime().after(latest)) {
                         latest = gc.getTime();
                     }
                 }
 
                 if (latest.after(new Date(2)) && latest.before(checkDate)) {
                     count++;
 
                     Assistant ass = new Assistant(a);
                     ass.setFirstName("*****");
                     ass.setLastName("*****");
                     ass.setEmail("ano@nym.com");
                     ass.save();

                    for (AssistantTableModel m : models) {
                        m.update(ass, (Boolean) true);
                    }
                 }
             }
         } catch (AdoHiveException ex) {
             Logger.error(
                     _("Anonymizing Assistants failed because of a database error"));
         }
 
         return count;
     }
 
 }
