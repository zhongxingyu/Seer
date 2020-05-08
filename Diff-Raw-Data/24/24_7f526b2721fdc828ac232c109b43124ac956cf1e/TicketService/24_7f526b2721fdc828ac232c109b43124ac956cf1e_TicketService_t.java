 package org.einherjer.twitter.tickets.service;
 
 import org.einherjer.twitter.tickets.model.Attachment;
 import org.einherjer.twitter.tickets.model.Project;
 import org.einherjer.twitter.tickets.model.Ticket;
 import org.einherjer.twitter.tickets.model.Ticket.TicketPriority;
 import org.einherjer.twitter.tickets.model.Ticket.TicketStatus;
 import org.einherjer.twitter.tickets.model.User;
 import org.einherjer.twitter.tickets.model.User.Role;
 import org.einherjer.twitter.tickets.repository.ProjectRepository;
 import org.einherjer.twitter.tickets.repository.TicketRepository;
 import org.einherjer.twitter.tickets.repository.UserRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.Assert;
 
 @Service
 public class TicketService {
 
     @Autowired
     private LoginService loginService;
     @Autowired
     private TicketRepository ticketRepository;
     @Autowired
     private ProjectRepository projectRepository;
     @Autowired
     private UserRepository userRepository;
 
     public Iterable<Ticket> findAll() {
         return ticketRepository.findAll();
     }
 
     public Iterable<Ticket> findAllForRole() {
         User loggedUser = loginService.getLoggedUser();
         Iterable<Ticket> tickets;
         if (loggedUser.getRole() == User.Role.REQUESTOR) {
             tickets = ticketRepository.findByCreator(loginService.getLoggedUser());
         }
         else if (loggedUser.getRole() == User.Role.APPROVER) {
             tickets = ticketRepository.findByStatus(TicketStatus.NEW);
         }
         else if (loginService.getLoggedUser().getRole() == User.Role.EXECUTOR) {
             tickets = ticketRepository.findByStatus(TicketStatus.APPROVED);
         }
         else {
             throw new RuntimeException("Unsupported user role");
         }
         for (Ticket t : tickets) {
             if (loggedUser.getUnread().contains(t)) {
                 t.setUnread(true);
             }
         }
         return tickets;
     }
 
     public Ticket find(String projectPrefix, Integer ticketNumber) {
         return this.find(projectPrefix, ticketNumber, false);
     }
 
     private Ticket find(String projectPrefix, Integer ticketNumber, boolean allowNull) {
         Project project = projectRepository.findByPrefix(projectPrefix);
         Assert.notNull(project, "No project matches the specified prefix");
         Ticket ticket = ticketRepository.findByProjectAndNumber(project, ticketNumber);
         if (!allowNull) {
             Assert.notNull(ticket, "No ticket matches the specified project and ticket number");
         }
         return ticket;
     }
 
     @Transactional
     public Ticket save(String projectPrefix, Integer ticketNumber, Ticket data) throws TicketNotFoundException {
         return internalSaveOrPatch(projectPrefix, ticketNumber, data, false);
     }
 
     @Transactional
     public Ticket patch(String projectPrefix, Integer ticketNumber, Ticket data) throws TicketNotFoundException {
         return internalSaveOrPatch(projectPrefix, ticketNumber, data, true);
     }
 
     private Ticket internalSaveOrPatch(String projectPrefix, Integer ticketNumber, Ticket data, boolean patch) throws TicketNotFoundException {
         if (!patch) {
             data.setAssignee(userRepository.findByUsername(data.getAssignee().getUsername()));
             Assert.notNull(data.getAssignee(), "No user matches the specified username");
         }
         Ticket ticket = this.find(projectPrefix, ticketNumber, true);
         if (ticket == null) {
             if (patch) {
                 throw new TicketNotFoundException(projectPrefix, ticketNumber);
             }
             else{
                 Project project = projectRepository.findByPrefix(projectPrefix);
                 Assert.notNull(project, "No project matches the specified prefix");
                 ticket = new Ticket(project, data);
                 Ticket newTicket = ticketRepository.save(ticket);
                 this.broadcastUnread(newTicket, Role.APPROVER);
                 return newTicket;
             }
         }
         else {
             validateUpdate(ticket);
             ticket.set(data);
             ticket.logUpdate();
             return ticket;
         }
     }
 
    private void broadcastRead(Ticket ticket) {
        //TODO: this is very ineffcient, read all users every time a ticket is cancelled, rejected, etc; we should have an object cache or some pre populated structure, updated by the user CRUD that should be infrequent
        for (User user : userRepository.findAll()) {
            user.read(ticket);
        }
    }

     private void broadcastUnread(Ticket ticket, Role role) {
        //TODO: this is very ineffcient, read all users (by role) every time a ticket is created, we should have an object cache or some pre populated structure, updated by the user CRUD that should be infrequent
         for (User user : userRepository.findByRole(role)) {
             user.addUnread(ticket);
         }
     }
 
     @Transactional
     public void changePriority(String projectPrefix, Integer ticketNumber, TicketPriority newPriority, String comment) throws TicketNotFoundException {
         Ticket ticket = this.find(projectPrefix, ticketNumber, true);
 
         if (loginService.getLoggedUser().getRole() != User.Role.APPROVER) {
             throw new SecurityException("Only Approvers can perform this operation");
         }
         if (ticket.getStatus() != TicketStatus.NEW && ticket.getStatus() != TicketStatus.APPROVED) {
             throw new UnsupportedOperationException("Change Priority is not a valid operation for tickets in status " + ticket.getStatus());
         }
 
         if (ticket.getPriority() != newPriority) {
             ticket.changePriority(newPriority, comment);
         }
     }
 
     @Transactional
     public void addComment(String projectPrefix, Integer ticketNumber, String comment) {
         Ticket ticket = this.find(projectPrefix, ticketNumber);
         validateUpdate(ticket);
         ticket.addComment(comment);
         ticket.logUpdate();
     }
 
     @Transactional
     public void cancel(String projectPrefix, Integer ticketNumber, String comment) {
         Ticket ticket = this.find(projectPrefix, ticketNumber);
         if (loginService.getLoggedUser().getRole() == User.Role.REQUESTOR) {
             if (!ticket.createdBy().equals(loginService.getLoggedUser())) {
                 throw new SecurityException("Requestors can only cancel tickets they created");
             }
             if (ticket.getStatus() == TicketStatus.NEW) {
                this.broadcastRead(ticket);
                 this.ticketRepository.delete(ticket);
             }
             else if (ticket.getStatus() == TicketStatus.APPROVED) {
                 if (comment == null || comment.trim().isEmpty()) {
                     throw new IllegalArgumentException("Comment is required in order to cancel an approved ticket");
                 }
                this.broadcastRead(ticket);
                 ticket.changeStatus(TicketStatus.CANCELLED, comment);
             }
             else {
                 throw new UnsupportedOperationException("For Requestors, Cancel is not a valid operation for tickets in status " + ticket.getStatus());
             }
         }
         else if (loginService.getLoggedUser().getRole() == User.Role.APPROVER) {
             if (ticket.getStatus() == TicketStatus.NEW) {
                 if (comment == null || comment.trim().isEmpty()) {
                     throw new IllegalArgumentException("Comment is required in order to cancel an approved ticket");
                 }
                this.broadcastRead(ticket);
                 ticket.changeStatus(TicketStatus.CANCELLED, comment);
             }
             else {
                 throw new UnsupportedOperationException("For Approvers, Cancel is not a valid operation for tickets in status " + ticket.getStatus());
             }
         }
         else {
             throw new SecurityException("Only Requestor or Approver users can perform this operation");
         }
     }
 
     @Transactional
     public void reject(String projectPrefix, Integer ticketNumber, String comment) {
         Ticket ticket = this.find(projectPrefix, ticketNumber);
         if (loginService.getLoggedUser().getRole() != User.Role.APPROVER) {
             throw new SecurityException("Only Approver users can perform this operation");
         }
         if (ticket.getStatus() != TicketStatus.NEW) {
             throw new UnsupportedOperationException("Reject is not a valid operation for tickets in status " + ticket.getStatus());
         }
         if (comment == null || comment.trim().isEmpty()) {
             throw new IllegalArgumentException("Comment is required in order to cancel an approved ticket");
         }
        this.broadcastRead(ticket);
         ticket.changeStatus(TicketStatus.REJECTED, comment);
     }
     
     @Transactional
     public void approve(String projectPrefix, Integer ticketNumber, String comment) {
         Ticket ticket = this.find(projectPrefix, ticketNumber);
         if (loginService.getLoggedUser().getRole() != User.Role.APPROVER) {
             throw new SecurityException("Only Approver users can perform this operation");
         }
         if (ticket.getStatus() != TicketStatus.NEW) {
             throw new UnsupportedOperationException("Approve is not a valid operation for tickets in status " + ticket.getStatus());
         }
         ticket.changeStatus(TicketStatus.APPROVED, comment);
        this.broadcastRead(ticket); //remove from the approvers
        this.broadcastUnread(ticket, Role.EXECUTOR); //add to the executors
     }
 
     @Transactional
     public void done(String projectPrefix, Integer ticketNumber) {
         Ticket ticket = this.find(projectPrefix, ticketNumber);
         if (loginService.getLoggedUser().getRole() != User.Role.EXECUTOR) {
             throw new SecurityException("Only Executor users can perform this operation");
         }
         if (ticket.getStatus() != TicketStatus.APPROVED) {
             throw new UnsupportedOperationException("Mark as Done is not a valid operation for tickets in status " + ticket.getStatus());
         }
        this.broadcastRead(ticket); //remove from executors
         ticket.changeStatus(TicketStatus.DONE, null);
     }
 
     @Transactional
     public void read(String projectPrefix, Integer ticketNumber) {
         Ticket ticket = this.find(projectPrefix, ticketNumber);
         loginService.getLoggedUser().read(ticket);
     }
 
     public Attachment getAttachment(String projectPrefix, Integer ticketNumber, Long attachmentId) {
         Ticket ticket = this.find(projectPrefix, ticketNumber);
         return ticket.findAttachmentById(attachmentId);
     }
 
     @Transactional
     public Attachment addAttachment(String projectPrefix, Integer ticketNumber, String fileName, String fileSize, String fileType, byte[] bytes) {
         Ticket ticket = this.find(projectPrefix, ticketNumber);
         validateUpdate(ticket);
         Attachment attachment = ticket.addAttachment(fileName, fileSize, fileType, bytes);
         ticket.logUpdate();
         return attachment;
     }
 
     @Transactional
     public void deleteAttachment(String projectPrefix, Integer ticketNumber, Long attachmentId) {
         Ticket ticket = this.find(projectPrefix, ticketNumber);
         validateUpdate(ticket);
         ticket.removeAttachment(attachmentId);
         ticket.logUpdate();
     }
 
     private void validateUpdate(Ticket ticket) {
         if (!ticket.createdBy().equals(loginService.getLoggedUser())) {
             throw new SecurityException("Only the creator can edit a ticket");
         }
         if (ticket.getStatus() != TicketStatus.NEW) {
             throw new UnsupportedOperationException("Edit is not a valid operation for tickets in status " + ticket.getStatus());
         }
     }
 
 }
