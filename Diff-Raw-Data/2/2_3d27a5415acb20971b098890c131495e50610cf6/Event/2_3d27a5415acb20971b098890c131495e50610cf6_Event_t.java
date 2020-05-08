 package se.cygni.expenses.api;
 
 import com.fasterxml.jackson.annotation.JsonFormat;
 
 import java.util.Date;
 
 public class Event {
 
     private long id;
     private String name;
     @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ssz")
     private Date date;
 
     public Event() {
     }
 
     public Event(long id, String name, Date date) {
         this.id = id;
         this.name = name;
         this.date = date;
     }
 
     public long getId() {
         return id;
     }
 
     public String getName() {
         return name;
     }
 
     public Date getDate() {
         return date;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Event event = (Event) o;
 
         if (id != event.id) return false;
        //if (date != null ? !date.equals(event.date) : event.date != null) return false;
         if (name != null ? !name.equals(event.name) : event.name != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = (int) (id ^ (id >>> 32));
         result = 31 * result + (name != null ? name.hashCode() : 0);
         result = 31 * result + (date != null ? date.hashCode() : 0);
         return result;
     }
 }
