 package net.baremodels.uat;
 
 import ionic.app.NucleusTestFactory;
 import net.baremodels.apps.Nucleus;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 public class UATTest {
 
     UAT testObject = new UAT();
 
     @Test
     public void screenContains_throws_IllegalStateException_before_show() {
         try {
             testObject.screenContains("anything");
             fail();
         } catch (IllegalStateException e) {
             assertEquals("This method is only valid after showing an object",e.getMessage());
         }
     }
 
     @Test
     public void assertScreenContains_throws_IllegalStateException_before_show() {
         try {
             testObject.assertScreenContains("anything");
             fail();
         } catch (IllegalStateException e) {
             assertEquals("This method is only valid after showing an object",e.getMessage());
         }
     }
 
     @Test
     public void select_throws_IllegalStateException_before_show() {
         try {
             testObject.select("anything");
             fail();
         } catch (IllegalStateException e) {
             assertEquals("This method is only valid after showing an object",e.getMessage());
         }
     }
 
     @Test
     public void select_ok_when_object_is_on_screen() {
         String one = "one";
         List list = Arrays.asList(one);
         testObject.show(list);
         testObject.select(one);
     }
 
     @Test
     public void select_changes_currently_selected_object() {
         String one = "one";
        String two = "two";
        List nested = Arrays.asList(one, two);
         List outer = Arrays.asList(nested);
         testObject.show(outer);
         assertFalse(testObject.screenContains(one));
         testObject.select(nested);
         assertTrue(testObject.state.text,testObject.screenContains(one));
         testObject.assertScreenContains(one);
        testObject.assertScreenContains(two);
     }
 
     @Test
     public void select_fails_when_object_is_not_on_screen() {
         String object = this + "";
         try {
             List list = new ArrayList();
             testObject.show(list);
             testObject.select(object);
             fail();
         } catch (IllegalStateException e) {
             assertEquals(String.format("[%s] is not on screen",object),e.getMessage());
         }
     }
 
     @Test
     public void assertScreenContains_ok_when_value_is_on_screen_for_list_element() {
         List list = new ArrayList();
         list.add("fantastic");
         testObject.show(list);
         assertTrue(testObject.screenContains("fantastic"));
         testObject.assertScreenContains("fantastic");
     }
 
     @Test
     public void assertScreenContains_ok_when_value_is_on_screen_for_Nucleus() {
         Nucleus nucleus = NucleusTestFactory.newNucleus();
         testObject.show(nucleus);
         testObject.assertScreenContains("Nucleus");
     }
 
     @Test
     public void assertScreenContains_fails_when_value_is_not_on_screen() {
         try {
             List list = new ArrayList();
             testObject.show(list);
             testObject.assertScreenContains("fantastic");
             fail();
         } catch (AssertionError e) {
             String message = e.getMessage();
             assertTrue(message,message.startsWith("fantastic not found in"));
         }
     }
 
 }
