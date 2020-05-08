 package com.epam.cdp.oleshchuk.cinema.controller;
 
 import com.epam.cdp.oleshchuk.cinema.model.Ticket;
 import com.epam.cdp.oleshchuk.cinema.model.TicketIdsJson;
 import com.epam.cdp.oleshchuk.cinema.model.User;
 import com.epam.cdp.oleshchuk.cinema.service.TicketService;
 import com.epam.cdp.oleshchuk.cinema.service.UserService;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.HandlerMapping;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @Controller
 public class TicketController {
 
     @Autowired
     private TicketService ticketService;
     @Autowired
     private UserService userService;
     private static final Logger log = Logger.getLogger(TicketController.class);
 
     @RequestMapping(method = RequestMethod.GET, value = "/tickets/**")
     public Map<String, Object> getAllTickets(HttpServletRequest request, HttpServletResponse response) {
         Map<String, Object> returnParams = new HashMap<String, Object>();
         List<Ticket> availableTicket = null;
         String remainingPaths = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
         String enotherPath = remainingPaths.substring("/tickets".length());
         try {
             availableTicket = ticketService.getAvailableTickets();
             returnParams.put("ticketsList", availableTicket);
             if (enotherPath.length() > 6) {
                 RequestDispatcher rd = request.getRequestDispatcher(enotherPath);
                 request.setAttribute("returnParams", returnParams);
                 rd.forward(request, response);
             }
         } catch (Exception e) {
             returnParams.put("error", e.getMessage());
             log.error(e.getMessage(), e);
         }
         return returnParams;
     }
 
     @RequestMapping(method = RequestMethod.POST, value = "/users/{userId}/tickets/book")
     @ResponseBody
     public Map<String, Object> bookTicketsByJson(@PathVariable String userId, @RequestBody TicketIdsJson ticketIdsJson) {
         Map<String, Object> response = new HashMap<String, Object>();
         List<Long> bookedTicketIds = new ArrayList<Long>();
         String message = null;
         try {
 
             Long longId = Long.parseLong(userId);
             User user = userService.getUserById(longId);
             List<Ticket> bookedTickets = ticketService.getBookedTicketsByTicketsIds(ticketIdsJson.getTicketIds());
             if (bookedTickets.size() == 0) {
                 Ticket ticket = null;
                 for (Long ticketId : ticketIdsJson.getTicketIds()) {
                     ticket = ticketService.getTicketById(ticketId);
                     ticketService.bookTicket(ticket, user);
                     bookedTicketIds.add(ticketId);
                 }
                 message = "Tickets are booked";
 
             } else {
 
                 for (Ticket bookedTicket : bookedTickets) {
                     bookedTicketIds.add(bookedTicket.getId());
                 }
                 message = "Sorry, tickets :\r\n " + bookedTickets + " \r\nare already booked. Unchecked it.";
             }
         } catch (Exception e) {
             response.put("message", e.getMessage());
             log.error(e.getMessage(), e);
         }
         response.put("message", message);
         return response;
     }
 
     @RequestMapping(method = RequestMethod.GET, value = "/users/{userId}/tickets/**")
     public Map<String, Object> getUserTickets(@PathVariable String userId, HttpServletRequest request, HttpServletResponse response) {
         Map<String, Object> returnParams = new HashMap<String, Object>();
         String remainingPaths = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
         String endOfThePath= "tickets";
         String enotherPath = remainingPaths.substring(remainingPaths.indexOf(endOfThePath) + endOfThePath.length());
         try {
             Long longId = Long.parseLong(userId);
             User user = userService.getUserById(longId);
             List<Ticket> myTickets = ticketService.getTicketsByUser(user);
             returnParams.put("ticketsList", myTickets);
             returnParams.put("user", user);
             if (enotherPath.length() > 6) {
                 RequestDispatcher rd = request.getRequestDispatcher(enotherPath);
                 request.setAttribute("returnParams", returnParams);
                 rd.forward(request, response);
             }
         } catch (Exception e) {
             returnParams.put("error", e.getMessage());
             log.error(e.getMessage(), e);
         }
 
         return returnParams;
     }
 
 
 
 
     @RequestMapping(method = RequestMethod.GET, value = "/dateFrom/{dateFrom}/dateTo/{dateTo}/**")
     public Map<String, Object> filterTicketsByDate(HttpServletRequest request, @PathVariable String dateFrom, @PathVariable String dateTo) {
         Map<String, Object> returnParams = (Map<String, Object>) request.getAttribute("returnParams");
         List<Ticket> availableTicket = (List<Ticket>) returnParams.get("ticketsList");
         try {
             availableTicket = ticketService.filterTicketsByDate(availableTicket, dateFrom, dateTo);
             returnParams.put("ticketsList", availableTicket);
         } catch (Exception e) {
             returnParams.put("error", e.getMessage());
             log.error(e.getMessage(), e);
         }
         return returnParams;
     }
 
     @RequestMapping(method = RequestMethod.GET, value = "/title/{title}/**")
     public Map<String, Object> filterTicketsByTitle(HttpServletRequest request, @PathVariable String title) {
         Map<String, Object> returnParams = (Map<String, Object>) request.getAttribute("returnParams");
         List<Ticket> availableTicket = (List<Ticket>) returnParams.get("ticketsList");
         try {
             availableTicket = ticketService.filterTicketsByTitle(availableTicket, title);
             returnParams.put("ticketsList", availableTicket);
         } catch (Exception e) {
             returnParams.put("error", e.getMessage());
             log.error(e.getMessage(), e);
         }
         return returnParams;
     }
 
     @RequestMapping(method = RequestMethod.GET, value = "/category/{category}/**")
     public Map<String, Object> filterTicketsByCategory(HttpServletRequest request, @PathVariable String category) {
         Map<String, Object> returnParams = (Map<String, Object>) request.getAttribute("returnParams");
         List<Ticket> availableTicket = (List<Ticket>) returnParams.get("ticketsList");
         try {
             availableTicket = ticketService.filterTicketsByCategory(availableTicket, category);
             returnParams.put("ticketsList", availableTicket);
         } catch (Exception e) {
             returnParams.put("error", e.getMessage());
             log.error(e.getMessage(), e);
         }
         return returnParams;
     }
 }
