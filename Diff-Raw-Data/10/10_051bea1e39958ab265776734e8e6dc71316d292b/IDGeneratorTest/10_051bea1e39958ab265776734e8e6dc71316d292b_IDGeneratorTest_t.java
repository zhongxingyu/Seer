 package models.user;
 
 import java.util.Date;
 import java.util.Calendar;
 
 import javax.persistence.OptimisticLockException;
 
 import org.junit.Test;
 import org.junit.Assert;
 
 import com.avaje.ebean.Ebean;
 
 import models.user.IDGenerator;
 import models.user.UserType;
 import models.user.Gender;
 import models.dbentities.UserModel;
 
 /**
  * Test class for the Identifier generator.
  * @author Felix Van der Jeugt
  */
 public class IDGeneratorTest extends test.ContextTest {
 
     @Test public void testCleanName() {
         Assert.assertEquals("frederique", IDGenerator.cleanName("Frédérique"));
         Assert.assertEquals("pieter jan", IDGenerator.cleanName("Pieter-Jan"));
         Assert.assertEquals("ea",         IDGenerator.cleanName("ëáα"));
         Assert.assertEquals("",           IDGenerator.cleanName("2#$!)))"));
     }
 
     @Test public void testGenerator() {
        for(int i = 0; i < 100; i++) {
             String name = "Felix Van der Jeugt";
             String id = IDGenerator.generate(name, Calendar.getInstance());
             try {
                 Ebean.save(new UserModel(
                     id, UserType.PUPIL_OR_INDEP, name, new Date(), new Date(),
                     "password", "salt", "email", Gender.Other, "en"
                 ));
             } catch(OptimisticLockException e) {
                 Assert.fail("Couldn't save: " + e.getStackTrace());
             }
         }
 
     }
 
 }
