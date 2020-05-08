 import static org.junit.Assert.*;
 import play.test.*;
 import org.junit.*;
 import models.*;
 
 public class TemplateTest extends UnitTest {
 
     @Test
     public void testCreateTemplate() {
        Template template = new Template("Name", 1);
         assertEquals(template.Name, "Name");
     }
     
     @Test
     public void testChangeTemplate() {
    	Template template = new Template("Name", 1);
     	template.Name = "Name2";
         assertEquals(template.Name, "Name2");
     }
    
    @Test
    public void testIDTemplate() {
    	Template template = new Template("Name", 1);
        assertTrue(template.ID == 1);
    }	
 }
