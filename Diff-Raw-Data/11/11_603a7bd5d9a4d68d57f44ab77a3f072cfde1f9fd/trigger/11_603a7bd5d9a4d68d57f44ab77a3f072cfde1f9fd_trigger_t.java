 
 public class trigger {
 
    private String arrayItems[];
     private String triggerName;
     
     //Constructor for the trigger object
    public trigger (String triggerName, String[] arrayItems)
     {
         this.triggerName = triggerName;
         this.arrayItems = arrayItems;
         System.out.println(this.arrayItems);
     }
     
     //Accessor for the array
    public String[] getArrayItems() {
         return arrayItems;
     }
     
     //Mutator for the array
    public void setArrayItems(String[] arrayItems) {
         this.arrayItems = arrayItems;
     }
     
     //Accessor for the trigger name
     public String getTriggerName() {
         return triggerName;
     }
     //Mutator for the trigger name
     public void setTriggerName(String triggerName) {
         this.triggerName = triggerName;
     }
         
 }
