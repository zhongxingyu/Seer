 package net.cyklotron.cms.security.internal;
 
 import java.security.Principal;
 
 import org.objectledge.authentication.UserAlreadyExistsException;
 import org.objectledge.authentication.UserInUseException;
 import org.objectledge.authentication.UserManagementParticipant;
 import org.objectledge.authentication.UserUnknownException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.entity.EntityInUseException;
 import org.objectledge.coral.security.Role;
 import org.objectledge.coral.security.RoleAssignment;
 import org.objectledge.coral.security.SecurityException;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.pipeline.ProcessingException;
 
 public class CyklotronUserManagementParticipant
     implements UserManagementParticipant
 {
 
     private final CoralSessionFactory coralSessionFactory;
 
     public CyklotronUserManagementParticipant(CoralSessionFactory coralSessionFactory)
     {
         super();
         this.coralSessionFactory = coralSessionFactory;
     }
 
     @Override
     public boolean supportsRemoval()
     {
         return true;
     }
 
     @Override
     public void createAccount(Principal user)
         throws UserAlreadyExistsException
     {
 
         try (CoralSession coralSession = coralSessionFactory.getRootSession())
         {
             Subject subject = coralSession.getSecurity().getSubject(user.getName());
             Role role = coralSession.getSecurity().getUniqueRole("cms.registered");
             coralSession.getSecurity().grant(role, subject, false);
         }
         catch(EntityDoesNotExistException | org.objectledge.coral.security.SecurityException e)
         {
             throw new RuntimeException("Granting cms.registerd role failed", e);
         }
     }
 
     @Override
     public void removeAccount(Principal user)
         throws UserUnknownException, UserInUseException
     {
        CoralSession coralSession = coralSessionFactory.getRootSession();
        Subject subject;
        try
         {
            subject = coralSession.getSecurity().getSubject(user.getName());
             RoleAssignment[] roles = subject.getRoleAssignments();
             for(RoleAssignment role : roles)
             {
                 coralSession.getSecurity().getRole(role.getRole().getId());
                 coralSession.getSecurity().revoke(role.getRole(), subject);
             }
            coralSession.getSecurity().deleteSubject(subject);
         }
        catch(EntityDoesNotExistException | IllegalArgumentException | SecurityException | EntityInUseException e)
         {
             throw new UserUnknownException("Failed to lookup coral subject", e);
         }
        
     }
 
 }
