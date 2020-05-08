 package com.tehbeard.utils.testSuite.syringe;
 
 import static org.junit.Assert.assertEquals;
 
 import java.lang.reflect.Field;
 
import me.tehbeard.utils.syringe.Doctor;
import me.tehbeard.utils.syringe.Inject;
import me.tehbeard.utils.syringe.Injector;
 
 import org.junit.Test;
 
 public class DoctorInjectTest {
 
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
         Doctor<Patient> who = new Doctor<Patient>();
         who.addInjector(inject);
         who.administer(patient);
         assertEquals("jane", patient.toString());
 
     }
 }
