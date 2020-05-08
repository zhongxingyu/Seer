 package ualberta.g12.adventurecreator.data;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.List;
 
 public class FragmentPart implements Serializable{
     private String type;
     private String data;
     private Choice choice;
     
     public FragmentPart(String type){
         this.type=type;
         this.data = "";
        this.choice = null;
     }
     /**
      * @return the type
      */
     public String getType() {
         return type;
     }
     /**
      * @param type the type to set
      */
     public void setType(String type) {
         this.type = type;
     }
     /**
      * @return the data
      */
     public String getData() {
         return data;
     }
     /**
      * @param data the data to set
      */
     public void setData(String data) {
         this.data = data;
     }
     /**
      * @return the choice
      */
     public Choice getChoice() {
         return choice;
     }
     /**
      * @param choice the choice to set
      */
     public void setChoice(Choice choice) {
         this.choice = choice;
     }
 }
