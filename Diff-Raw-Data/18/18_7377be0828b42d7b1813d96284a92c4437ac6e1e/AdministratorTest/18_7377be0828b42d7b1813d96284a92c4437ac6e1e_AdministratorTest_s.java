 
 package pt.uac.cafeteria.model;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 
 public class AdministratorTest {
 
     @Before
     public void setUp() {
     }
 
     @Test
     public void canCreateAccount() {
         Administrator admin = new Administrator("dummy", "password");
         assertEquals("dummy", admin.getUsername());
        assertEquals("password", admin.getPassword());
     }
 }
