 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.user.application;
 
 import java.util.List;
 
 import org.apache.commons.lang.Validate;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.globant.katari.hibernate.coreuser.SecurityUtils;
 
 import com.globant.katari.core.application.Command;
 import com.globant.katari.core.application.Validatable;
 import com.globant.katari.core.application.Initializable;
 
 import org.springframework.validation.Errors;
 
 import org.apache.camel.ProducerTemplate;
 import org.apache.camel.CamelContext;
 
 import com.globant.katari.hibernate.coreuser.DeleteMessage;
 
 import com.globant.katari.user.domain.User;
 import com.globant.katari.user.domain.UserRepository;
 
 /** Command to delete a user.
  *
  * This command extends UserFilterCommand. That is a hack to return to the user
  * list keeping the filtering conditions.
  *
  * This hack should be unnecessary when katari implements a crud controller.
  */
 public class DeleteUserCommand extends UserFilterCommand
   implements Command<List<User>>, Validatable, Initializable {
 
   /** The class logger.
    */
   private static Logger log = LoggerFactory.getLogger(DeleteUserCommand.class);
 
   /** The katari event bus, never null.
    */
   private CamelContext eventBus;
 
   /** The user repository, never null.
    */
   private UserRepository userRepository;
 
   /** The id of the user.
    */
   private long userId;
 
   /** The list of users matching the search criteria.
    *
    * This not null after init.
    */
   private List<User> users;
 
   /** Constructor to dynamically proxy this type of command (mainly used by
    * wicket).
   */
   protected DeleteUserCommand() {
   }
 
   /** The contructor with the user repository.
    *
    * @param theEventBus The camel context to send the delete event to. It
    * cannot be null.
    *
    * @param theUserRepository The user repository. It cannot be null.
    */
   public DeleteUserCommand(final CamelContext theEventBus,
       final UserRepository theUserRepository) {
     super(theUserRepository);
     Validate.notNull(theEventBus, "The event bus cannot be null");
     Validate.notNull(theUserRepository, "The user repository cannot be null");
     eventBus = theEventBus;
     userRepository = theUserRepository;
   }
 
   /** Returns the id of the user.
    *
    * @return Returns the user id.
    */
   public long getUserId() {
     return userId;
   }
 
   /** Sets the id of the user.
    *
    * @param theUserId The id of the user.
    */
   public void setUserId(final long theUserId) {
     userId = theUserId;
   }
 
   /** Validates that the user can be deleted.
    *
    * This operation also considers the veto of the delete event.
    *
    * @param errors Contextual state about the validation process. It can not be
    * null.
    */
   public void validate(final Errors errors) {
     log.trace("Entering validate");
     ProducerTemplate producer = eventBus.createProducerTemplate();
     DeleteMessage response = (DeleteMessage) producer.requestBody(
         "direct:katari.user.vetoDeleteUser", new DeleteMessage(userId));
     if (!response.canDelete()) {
       errors.rejectValue("", "unknownError", response.getMessage("", "\n"));
     }
     log.trace("Leaving validate");
   }
 
   public void init() {
     users = super.execute();
   }
 
   /** Removes the user with the id passed in setUserId.
    *
    * This operation will fail if the user being deleted is the same as the
    * logged on user (the user tried to delete himself).
    *
    * @return Always return null.
    */
   public List<User> execute() {
     log.trace("Entering execute");
     if (SecurityUtils.getCurrentUser().getId() == Long.valueOf(getUserId())) {
       throw new RuntimeException("You cannot delete yourself.");
     }
     User user = userRepository.findUser(Long.valueOf(getUserId()));
 
     // Notify all interested parties that this user is being deleted.
     ProducerTemplate producer = eventBus.createProducerTemplate();
     producer.requestBody("direct:katari.user.deleteUser",
         new DeleteMessage(userId));
 
     userRepository.remove(user);
     log.trace("Leaving execute");
     users = super.execute();
 
     return users;
   }
 
   /** Gets the list of users matching the search criteria.
    *
    * @return the list of users, never null after calling init.
    */
   public List<User> getUsers() {
     return users;
   }
 }
 
