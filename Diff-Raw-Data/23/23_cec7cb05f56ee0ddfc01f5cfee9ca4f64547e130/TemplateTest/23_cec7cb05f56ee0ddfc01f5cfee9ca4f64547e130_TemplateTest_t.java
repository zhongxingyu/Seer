 import static org.junit.Assert.*;
 import play.test.*;
 import org.junit.*;
 import models.*;
 
 public class TemplateTest extends UnitTest {
 
     @Test
     public void testCreateTemplate() {
        Template template = new Template("Name");
         assertEquals(template.Name, "Name");
     }
     
     @Test
     public void testChangeTemplate() {
    	Template template = new Template("Name");
     	template.Name = "Name2";
         assertEquals(template.Name, "Name2");
     }
 }
