 
 package com.mycompany.booking.core;
 
 import com.mycompany.booking.utils.IDAO;
 import java.util.List;
 
 /**
  * Interface to product catalogue
  * @author hajo
  * Modified for our projects
  */
 public interface ITicketCatalogue extends IDAO<Ticket, Long> {
 
    public List<Ticket> getByName(String name);
     
     public Ticket getById(Long id);
     
     public List<Ticket> getByType(String type);
     
    public List<Ticket> getByDestination(String dest);
     
 }
