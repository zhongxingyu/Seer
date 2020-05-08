 package no.niths.domain;
 
 import static org.junit.Assert.*;
 
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 
 import org.hibernate.validator.HibernateValidator;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
 
 public class RoomTest {
 
 private LocalValidatorFactoryBean localValidatorFactory;
     
     @Before
     public void setup() {
         localValidatorFactory = new LocalValidatorFactoryBean();
         localValidatorFactory.setProviderClass(HibernateValidator.class);
         localValidatorFactory.afterPropertiesSet();
     }

    @Ignore
     @Test
     public void testCreateRoomWithInvalidName() {
         Room r = new Room();
         r.setRoomName("baz");
         Set<ConstraintViolation<Room>> v = localValidatorFactory.validate(r);
         assertEquals(1, v.size());
     }
 }
