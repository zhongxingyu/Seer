 package hello1;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 
 
 @ManagedBean
 @RequestScoped
 public class Hello {
     private String name;
 
     public Hello() {
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String user_name) {
         this.name = user_name;
     }
 }
