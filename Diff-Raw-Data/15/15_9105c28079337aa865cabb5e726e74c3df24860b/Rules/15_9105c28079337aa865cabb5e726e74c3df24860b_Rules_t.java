 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Email;
 
 import Persist.PersistentStorage;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  *
  * @author Bargavi
  */
 public class Rules {
 
     static private Rules rules;
     MessageController controller = MessageController.getInstance();
     PersistentStorage persistStore = PersistentStorage.getInstance();
     ArrayList<FilterRule> listOfRules = new ArrayList<FilterRule>();
 
    private Rules() {
         listOfRules = persistStore.loadRulesFromFileSystem();
         //applyRules(controller.getInboxFolderId());
         controller.addObserver(new Observer() {
             @Override
             public void update(Observable o, Object o1) {
                 applyRules(controller.getInboxFolderId());
             }
         });
     }
     
     public ArrayList getListOfRules() {
         return listOfRules;
     }
     
     public static Rules getInstance() {
        if (rules == null){
            rules = new Rules();
        }
         return rules;
     }
 
     public void applyRules(String folderId) {
 
         String[] messages = controller.getEmailList(folderId);
 
         for (String messageid : messages) {
             for (FilterRule rule : listOfRules) {
                 if (rule.matches(messageid)) {
                     controller.moveMessageToFolder(messageid, rule.moveToFolder);
                     break;
                 }
             }
         }
     }
 
     public void addRule(FilterRule rule) {
         listOfRules.add(rule);
     }
     
     public void deleteRule(int ruleId) {
         if(listOfRules.size() > ruleId)
             listOfRules.remove(ruleId);
     }
 
     void saveRules() {
         persistStore.saveRulesToFileSystem(listOfRules);
     }
     
 }
