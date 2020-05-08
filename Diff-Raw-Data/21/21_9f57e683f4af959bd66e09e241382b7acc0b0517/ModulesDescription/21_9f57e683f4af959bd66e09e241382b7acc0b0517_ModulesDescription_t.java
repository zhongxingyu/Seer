 package jDistsim.utils.resource;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 1.3.13
  * Time: 21:53
  */
 public class ModulesDescription {
 
     public String resolveTitle(String moduleName) {
         switch (moduleName) {
             case "create":
                return "Create";
             case "dispose":
                return "Dispose";
             case "delay":
                return "Delay";
             case "sender":
                return "Sender";
             case "assign":
                 return "Assign";
             case "receiver":
                 return "Receiver";
             case "outputer":
                 return "Outputer";
             case "condition":
                 return "Condition";
             default:
                 throw new RuntimeException("Title description not found");
         }
     }
 
     public String resolveText(String moduleName) {
         switch (moduleName) {
             case "create":
                 return EventTextResource.CREATE_EVENT_DESCRIPTION_TEXT;
             case "dispose":
                 return EventTextResource.DISPOSE_EVENT_DESCRIPTION_TEXT;
             case "delay":
                 return EventTextResource.DELAY_EVENT_DESCRIPTION_TEXT;
             case "sender":
                 return EventTextResource.SENDER_EVENT_DESCRIPTION_TEXT;
             case "assign":
                 return EventTextResource.DELAY_EVENT_DESCRIPTION_TEXT;
             case "receiver":
                 return EventTextResource.DELAY_EVENT_DESCRIPTION_TEXT;
             case "outputer":
                 return EventTextResource.DELAY_EVENT_DESCRIPTION_TEXT;
             case "condition":
                 return EventTextResource.DELAY_EVENT_DESCRIPTION_TEXT;
             default:
                 throw new RuntimeException("Text description not found");
         }
     }
 }
