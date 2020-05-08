 package com.tsupryk.web.service;
 
 import com.tsupryk.api.ITicket;
 import com.tsupryk.api.TicketCategory;
 import com.tsupryk.service.api.ITicketService;
 import com.tsupryk.web.api.ITicketController;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  * The Class JsonController.
  * Date: 06.09.13
  */
 @Controller
 @RequestMapping("/cinema")
 public class TicketController implements ITicketController {
 
     private static final String USER_ID = "userId";
     private static final String ERROR_RESPONSE = "{\"error\":\"Error\"}";
 
     @Autowired
     private ITicketService ticketService;
 
     @Override
     @ResponseBody
     @RequestMapping(value = "/tickets", produces = "application/json", method = RequestMethod.GET)
     public Object getAvailableTickets(@RequestParam(required = false) String filmName,
                                       @RequestParam(required = false) Date filmStartDate,
                                       @RequestParam(required = false) TicketCategory ticketCategory) {
 
         List<ITicket> availableTickets = ticketService.getAvailableTickets(filmName, filmStartDate, ticketCategory);
 
         return availableTickets;
     }
 
     /**
      * Books tickets for user.
      *
      * @param userId the user id, required
      * @param ticketList list of tickets to book, field Id in every ticket is required
      * @return the object
      */
     @Override
     @ResponseBody
     @RequestMapping(value = "/book", produces = "application/json", method = RequestMethod.POST)
     public Object bookTickets(@RequestParam String userId, @RequestBody List<ITicket> ticketList) {
        ticketService.bookTickets(userId, ticketList);
         return null;
     }
 
     @Override
     @ResponseBody
     @RequestMapping(value = "/tickets/booked/{" + USER_ID + "}/", produces = "application/json",
             method = RequestMethod.GET)
     public Object getBookedTickets(@PathVariable(USER_ID) String userId,
                                    @RequestParam(required = false) String filmName,
                                    @RequestParam(required = false) Date filmStartDate,
                                    @RequestParam(required = false) TicketCategory ticketCategory) {
        List<ITicket> tickets = ticketService.getBookedTickets(userId, filmName, filmStartDate, ticketCategory);
        return tickets;
     }
 
 }
