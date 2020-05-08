 package de.flower.rmt.service;
 
 import de.flower.common.util.Check;
 import de.flower.rmt.model.AbstractClubRelatedEntity;
 import de.flower.rmt.model.Club;
 import de.flower.rmt.model.User;
 import de.flower.rmt.service.security.ISecurityService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.ConstraintViolationException;
 import javax.validation.Validator;
 import java.util.Set;
 
 import static de.flower.common.util.Check.*;
 
 
 /**
  * @author flowerrrr
  */
 @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
 public abstract class AbstractService {
 
     @Autowired
     protected ISecurityService securityService;
 
     @Autowired
     private Validator validator;
 
 
     /**
      * Returns club of currently logged in user.
      *
      * @return
      */
     protected Club getClub() {
         User currentUser = securityService.getUser();
         notNull(currentUser, "Security Context must be filled with a user");
         return currentUser.getClub();
     }
 
     protected void assertClub(AbstractClubRelatedEntity entity) {
         if (entity != null) {
             Check.isEqual(entity.getClub(), getClub(), null);
         }
     }
 
     protected void validate(Object entity) {
 
         final Set<?> violations = validator.validate(entity);
         if (!violations.isEmpty()) {
            String message = violations.toString();
            throw new ConstraintViolationException(message, (Set<ConstraintViolation<?>>) violations);
         }
     }
 
 }
