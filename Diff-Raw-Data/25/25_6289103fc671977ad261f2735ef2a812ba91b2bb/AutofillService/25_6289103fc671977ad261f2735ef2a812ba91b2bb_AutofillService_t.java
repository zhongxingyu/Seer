 package fr.cg95.cvq.service.request.impl;
 
 import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
import org.apache.log4j.Logger;

 import fr.cg95.cvq.dao.hibernate.PersistentStringEnum;
import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.request.IAutofillService;
 import fr.cg95.cvq.service.request.IAutofillTriggerService;
 import fr.cg95.cvq.service.users.IIndividualService;
 
 /**
  * @author jsb@zenexity.fr
  *
  */
 public class AutofillService implements IAutofillService {
 
    private static Logger logger = Logger.getLogger(AutofillService.class);

     private static IIndividualService individualService;
 
     private enum TriggerType {
         SUBJECTID { public IAutofillTriggerService getService() { return individualService; } },
         REQUESTERID { public IAutofillTriggerService getService() { return individualService; } };
         public abstract IAutofillTriggerService getService();
     }
 
     public Map<String, String> getValues(String triggerName, Long id, Map<String, String> keys)
         throws CvqObjectNotFoundException {
         Object trigger = TriggerType.valueOf(triggerName.toUpperCase()).getService().getById(id);
         Object currentObject;
         Map<String, String> values = new HashMap<String, String>();
         for (Entry<String, String> listener : keys.entrySet()) {
             currentObject = trigger;
             for (String field : listener.getValue().split("\\.")) {
                 if (currentObject == null) {
                     break;
                 }
                 try {
                     currentObject = currentObject.getClass().getMethod("get" + field.substring(0, 1).toUpperCase() + field.substring(1, field.length())).invoke(currentObject);
                 } catch (NoSuchMethodException e) {
                     currentObject = null;
                     break;
                 } catch (IllegalAccessException e) {
                     currentObject = null;
                     break;
                 } catch (InvocationTargetException e) {
                     currentObject = null;
                     break;
                 }
             }
             if (currentObject instanceof PersistentStringEnum) {
                 values.put(listener.getKey(), currentObject.getClass().getName() + "_" + currentObject);
            } else if (currentObject instanceof Date) { 
                try {
                    SimpleDateFormat dateFormat = 
                        new SimpleDateFormat("dd/MM/yyyy", SecurityContext.getCurrentLocale());
                    values.put(listener.getKey(), 
                            currentObject != null ? dateFormat.format(currentObject) : null);
                } catch (CvqException e) {
                    logger.error("getValues() unable to get a localized date (no locale in security context)");
                }
             } else {
                values.put(listener.getKey(), currentObject != null ? currentObject.toString() : null);
             }
         }
         return values;
     }
 
     public void setIndividualService(IIndividualService individualService) {
         AutofillService.individualService = individualService;
     }
 
 }
