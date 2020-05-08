 package de.aidger.view.models;
 
 import static de.aidger.utils.Translation._;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Employment;
 import de.aidger.view.tabs.ViewerTab.DataType;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IAssistant;
 import de.unistuttgart.iste.se.adohive.model.IEmployment;
 
 /**
  * The UI employment is used for prettier rendering of the model.
  * 
  * @author aidGer Team
  */
 public class UIEmployment extends Employment implements UIModel {
 
     /**
      * Initializes the Employment class.
      */
     public UIEmployment() {
     }
 
     /**
      * Initializes the Employment class with the given employment model.
      * 
      * @param e
      *            the employment model
      */
     public UIEmployment(IEmployment e) {
         super(e);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.model.AbstractModel#toString()
      */
     @Override
     public String toString() {
         try {
             IAssistant assistant = (new Assistant()).getById(getAssistantId());
 
             Calendar cal = Calendar.getInstance();
             cal.clear();
             cal.set(Calendar.MONTH, getMonth() - 1);
             cal.set(Calendar.YEAR, getYear());
 
             return _("from") + " " + (new UIAssistant(assistant)).toString()
                     + " " + _("in") + " "
                    + (new SimpleDateFormat("MMMM yy")).format(cal.getTime());
         } catch (AdoHiveException e) {
             return "";
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.view.models.UIModel#getDataType()
      */
     @Override
     public DataType getDataType() {
         return DataType.Employment;
     }
 }
