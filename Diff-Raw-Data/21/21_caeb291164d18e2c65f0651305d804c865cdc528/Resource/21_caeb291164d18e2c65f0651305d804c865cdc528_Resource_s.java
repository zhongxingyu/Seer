 package replaceme.model;
 
import javax.validation.constraints.NotNull;
 
 public abstract class Resource {
 
    @NotNull
     private String id;
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 }
