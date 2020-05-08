 package dk.statsbiblioteket.doms.authchecker.ticketissuer;
 
//import dk.statsbiblioteket.doms.authchecker.TimeSensitiveCache;
import dk.statsbiblioteket.util.caching.TimeSensitiveCache;
 
 import java.util.Random;
 
 /**
  * Created by IntelliJ IDEA.
 * User: abr + mar
  * Date: Oct 7, 2010
  * Time: 3:19:56 PM
  * To change this template use File | Settings | File Templates.
  */
 public class TicketSystem {
 
 
    private TimeSensitiveCache<String, Ticket> tickets;
 
     private static final Random random = new Random();
 
     public TicketSystem(long timeToLive) {
        tickets = new TimeSensitiveCache<String, Ticket>(timeToLive, true);//30 sec
     }
 
 
 
     /**
      * Get the ticket from the system
      * @param id the id of the ticket
      * @return the ticket, or null of the ticket is not found
      */
     public Ticket getTicketFromID(String id){
        return tickets.get(id);
     }
 
 
     /**
      * Issue a new ticket for the given username and url
      * @param username the username to associate
      * @param url the url to associate
      * @return a new ticket with a unique id
      */
     public Ticket issueTicket(String username, String url){
         String id = generateID(username,url);
         Ticket ticket = new Ticket(id, url, username);
        tickets.put(id,ticket);
         return ticket;
     }
 
     private String generateID(String username, String url) {
        return username+"@"+url+"@"+random.nextInt();
     }
 
 
 }
