 package pl.itcrowd.revu.youtrack.model;
 
 import org.sylfra.idea.plugins.revu.model.Issue;
 
 public class ReVuYoutrackIssue extends Issue {
 // ------------------------------ FIELDS ------------------------------
 
     private String ticket;
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     public ReVuYoutrackIssue(String ticketId)
     {
         setTicket(ticketId);
     }
 
 // --------------------- GETTER / SETTER METHODS ---------------------
 
     public String getTicket()
     {
         return ticket;
     }
 
     public void setTicket(String ticket)
     {
         this.ticket = ticket;
     }
 
 // ------------------------ CANONICAL METHODS ------------------------
 
     @Override
     public boolean equals(Object o)
     {
         if (this == o) {
             return true;
         }
         if (!(o instanceof ReVuYoutrackIssue)) {
             return false;
         }
 
         ReVuYoutrackIssue that = (ReVuYoutrackIssue) o;
 
         //noinspection SimplifiableIfStatement
         if (ticket != null ? !ticket.equals(that.ticket) : that.ticket != null) {
             return false;
         }
         return !(ticket == null && !super.equals(o));
     }
 
     @Override
     public int hashCode()
     {
         return ticket == null ? super.hashCode() : ticket.hashCode();
     }
 }
