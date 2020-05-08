 package ch.bli.mez.controller;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.bli.mez.view.DefaultPanel;
 import ch.bli.mez.view.management.HolidayForm;
 
 public class HolidayControllerTest {
 
   private HolidayController instance;
   private HolidayForm form;
 
   @Before
   public void setUp() throws Exception {
     this.instance = new HolidayController();
     form = new HolidayForm();
     form.setDefaultPanel(new DefaultPanel());
   }
 
   @After
   public void tearDown() throws Exception {
     this.instance = null;
   }
 
   /*
    * Prüft ob der HolidayController korrekt erstellt wurde
    */
   @Test
   public void test() {
     assertNotNull(instance);
     assertNotNull(instance.getView());
   }
 
   @Test
   public void validateFields() {
 
     // Case 1: Everything ok
     form.setYear("9999");
     form.setPublicHolidays("50");
     form.setPreWorkdays("50");
 
    assertTrue(instance.validateFields(form));
 
     // Case 2: Year too long
     form.setYear("19999");
    assertFalse(instance.validateFields(form));
 
     // Case 3: Year too short
     form.setYear("111");
    assertFalse(instance.validateFields(form));
   }
 
 }
