 package models;
 
 import java.util.*;
 import play.db.ebean.*;
 import play.db.ebean.Model.*;
 import play.data.validation.Constraints.*;
 import play.data.format.Formats;
 
 import javax.persistence.*;
 import play.libs.Json;
 
 
 @Entity
 public class Task extends Model{
     
     @Id
     public Long id;
    @Required
     public String label;
     public Boolean done = false;
     public Integer priority;
 
     @Formats.DateTime(pattern = "yyyy-MM-dd")
     public Date due;
 
     @Formats.DateTime(pattern = "yyyy-MM-dd")
     public Date created;
 
     @Formats.DateTime(pattern = "yyyy-MM-dd")
     public Date modified;
         
 
     public static Finder<Long,Task> find = 
         new Finder(Long.class, Task.class);
 
     public static List<Task> all() {
         return find.all();
     }
   
     public static void create(Task task) {
         if(task.due == null){
             task.due = new Date();
         }
         task.save();
     }
   
     public static void delete(Long id) {
         find.ref(id).delete();
     }
 
     public static void update(Task task, Long id){
         task.modified = new Date();
         task.update(id);
     }
 
     public static List<Task> allDone(String orderBy){
         if(orderBy == null){
             return find.where()
                 .eq("done", true)
                 .orderBy("due asc").findList();
         } else {
             return find.where()
                 .eq("done", true)
                 .orderBy(orderBy).findList();
         }
     }
 
     public static List<Task> allOpen(String orderBy){
         if(orderBy == null){
             return find.where()
                 .eq("done", false)
                 .orderBy("due asc").findList();
         } else {
             return find.where()
                 .eq("done", false)
                 .orderBy(orderBy).findList();
         }
     }
 
     @Override
         public void save() {
         created();
         super.save();
     }
     
     
     @PrePersist
         void created() {
         this.created = this.modified = new Date();
     }
     
 }
