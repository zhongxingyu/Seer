 package jpaoletti.jpm.validator;
 
 import jpaoletti.jpm.core.PMContext;
 import jpaoletti.jpm.core.message.MessageFactory;
 import jpaoletti.jpm.core.operations.OperationCommandSupport;
 import jpaoletti.jpm.security.core.PMSecurityUser;
import jpaoletti.jpm.security.core.UserNotFoundException;
 
 /**
  * Check that the username is unique
  * 
  * @author jpaoletti
  */
 public class UniqueUsername extends ValidatorSupport {
 
     @Override
     public ValidationResult validate(PMContext ctx) {
         final ValidationResult res = new ValidationResult();
         res.setSuccessful(true);
         try {
             final PMSecurityUser user = (PMSecurityUser) ctx.getSelected().getInstance();
             if (ctx.getPresentationManager().getSecurityConnector(ctx).getUser(user.getUsername()) != null) {
                 res.setSuccessful(false);
                 res.getMessages().add(MessageFactory.error(ctx.getEntity(), ctx.getEntity().getFieldById("username"), "username.not.unique"));
             }
        } catch (UserNotFoundException ex) {
         } catch (Exception ex) {
             res.getMessages().add(MessageFactory.error(ctx.getEntity(), ctx.getEntity().getFieldById("username"), OperationCommandSupport.UNESPECTED_ERROR));
             res.setSuccessful(false);
         }
         return res;
     }
 }
