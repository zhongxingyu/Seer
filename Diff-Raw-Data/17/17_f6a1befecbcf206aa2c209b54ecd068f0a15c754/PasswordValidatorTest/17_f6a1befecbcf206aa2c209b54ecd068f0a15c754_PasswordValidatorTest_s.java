 package de.flower.rmt.service.validation;
 
 import de.flower.rmt.model.db.entity.User;
 import de.flower.rmt.model.dto.Password;
 import de.flower.rmt.test.AbstractRMTIntegrationTests;
 import org.testng.annotations.Test;
 
 import javax.validation.ConstraintViolation;
 import java.util.Set;
 
 import static org.testng.Assert.*;
 
 /**
  * @author flowerrrr
  */
 public class PasswordValidatorTest extends AbstractRMTIntegrationTests {
 
     @Test
     public void testValidation() {
         User user = testData.createUser();
         Password password = new Password(user.getId());
         String newPassword = "foobar";
         password.setNewPassword(newPassword);
         password.setNewPasswordRepeat(newPassword);
        password.setOldPassword(passwordGenerator.generatePassword());
         Set<ConstraintViolation<Password>> violations = validator.validate(password);
         assertTrue(violations.isEmpty(), violations.toString());
 
         // now validate with invalid password
         password.setOldPassword("klaöjsaf");
         violations = validator.validate(password);
         log.info(violations.toString());
         assertTrue(violations.size() == 1);
     }
 
 }
