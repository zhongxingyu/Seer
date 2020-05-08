 package org.openengsb.openticket.model;
 
 import java.io.Serializable;
 
 public class TicketFilter implements Serializable {
     String id;
     String type;
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public boolean match(Ticket ticket) {
         boolean ret = true;
 
         if (id != null) {
            if (!ticket.getId().startsWith(id))
                 ret = false;
         }
 
         if (type != null) {
            if (!ticket.getType().startsWith(type))
                 ret = false;
         }
         return ret;
     }
 }
