 package com.tehbeard.utils.testSuite.syringe;
 
 import static org.junit.Assert.assertEquals;
 
 import java.lang.reflect.Field;
 
import com.tehbeard.utils.syringe.Inject;
import com.tehbeard.utils.syringe.Injector;
 
 import org.junit.Test;
 
 public class InjectTest {
 
     @Test
     public void test() {
         Patient patient = new Patient();
 
         assertEquals(patient.toString(), "john");
 
         Injector<Patient, Inject> inject = new Injector<Patient, Inject>(Inject.class) {
 
             @Override
             protected void doInject(Inject annotation, Patient object, Field field) throws IllegalArgumentException,
                     IllegalAccessException {
 
                 field.set(object, "jane");
 
             }
 
         };
         inject.inject(patient);
         assertEquals("jane", patient.toString());
 
     }
 }
