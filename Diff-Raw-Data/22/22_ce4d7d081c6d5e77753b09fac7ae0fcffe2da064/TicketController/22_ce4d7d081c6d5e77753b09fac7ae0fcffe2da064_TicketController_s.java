 package com.ateneo.uaaptickets.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 import java.security.Principal;
 import java.util.Date;
 
 import com.ateneo.uaaptickets.entity.Event;
 import com.ateneo.uaaptickets.entity.Student;
 import com.ateneo.uaaptickets.entity.Ticket;
 import com.ateneo.uaaptickets.repository.*;
 import com.ateneo.uaaptickets.util.*;
 
 @Controller
 public class TicketController 
 {
 	@Autowired
 	private EventRepository eventRepository;
 
 	@Autowired
 	private AccountRepository accountRepository;
 
 	@Autowired
 	private StudentRepository studentRepository;
 
 	@Autowired
 	private TicketRepository ticketRepository;
 
 	@RequestMapping(value={"/event/{id}"})
 	@PreAuthorize("hasRole('USER')")
 	public String event(@PathVariable("id") int id, Model uiModel)
 	{
 		Event e = eventRepository.findOne(id);
 		uiModel.addAttribute("event", e);
 		return "eventPage";
 	}
 
 	@RequestMapping(value={"/event/{id}/buy"})
 	@PreAuthorize("hasRole('USER')")
 	public String buyForm(@PathVariable("id") int id, Principal user, Model uiModel)
 	{
 		// new Ticket and attach event
		uiModel.addAttribute("ticket", new Ticket());
 		Event e = eventRepository.findOne(id);
		ticket.setEvent(e);
 		
 		return "ticketForm"; //redirect to choose the seat type before processing of the ticket
 	}
 
 	@RequestMapping("/save")
 	@PreAuthorize("hasRole('USER')")
 	public String saveTicket(Ticket ticket, Principal principal, RedirectAttributes redirectAttributes)
 	{
 		Student student = studentRepository.findByAccount(accountRepository.findByUsername(principal.getName()));
 		ticket.setStudent(student);
 		ticket.setCreatedAt(new Date());
 
 		ticket = ticketRepository.save(ticket);
 
 		redirectAttributes.addFlashAttribute("SUCCESS_MESSAGE", "You have successfully purchased a ticket. Thank you for using uaaptickets");
 
 		return "redirect:/event";
 	}
 
 }
