 package com.jtdevelopment.e_eye_o.jackson.serialization;
 
 import com.jtbdevelopment.e_eye_o.DAO.ReadWriteDAO;
 import com.jtbdevelopment.e_eye_o.entities.*;
 import com.jtbdevelopment.e_eye_o.jackson.serialization.JacksonJSONIdObjectSerializer;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import static org.testng.AssertJUnit.assertEquals;
 
 /**
  * Date: 1/26/13
  * Time: 10:59 PM
  */
 @ContextConfiguration("/test-integration-context.xml")
 @Test(groups = {"integration"})
 public class JacksonJSONIdObjectSerializerIntegration extends AbstractTestNGSpringContextTests {
     private final static String newline = System.getProperty("line.separator");
 
     @Autowired
     private JacksonJSONIdObjectSerializer serializer;
 
     @Autowired
     private ReadWriteDAO readWriteDAO;
 
     @Autowired
     private IdObjectFactory factory;
 
     private AppUser appUser1;
     private AppUser appUser2;
     private ClassList classList1For1;
     private ClassList classList2For1;
     private ClassList classList1For2;
     private ObservationCategory oc1For1;
     private ObservationCategory oc2For1;
     private Student student1For1;
     private Student student2For1;
     private Observation o1ForS1;
     private Observation o2ForS1;
     private Photo photo1for1;
     private Photo photo2for1;
     private Photo photo3for1;
     private Map<IdObject, String> jsonValues;
 
     @BeforeMethod
     public synchronized void initialize() {
         if (readWriteDAO == null) {
             return;
         }
         if (serializer == null) {
             return;
         }
         if (factory == null) {
             return;
         }
         if (appUser1 != null) {
             return;
         }
 
         appUser1 = factory.newAppUserBuilder().withEmailAddress("jtest@test.com").withFirstName("Testy")
                 .withLastName("Tester").withLastLogout(new DateTime(2012, 12, 12, 12, 12, 13)).build();
         appUser1 = readWriteDAO.create(appUser1);
         appUser2 = factory.newAppUserBuilder().withEmailAddress("jtest2@test.com").withFirstName("Testier").withLastName("Tester").build();
         appUser2 = readWriteDAO.create(appUser2);
         classList1For1 = factory.newClassListBuilder(appUser1).withDescription("CL1-1").withLastObservationTimestamp(new LocalDateTime(2013, 3, 30, 13, 5, 0)).withAppUser(appUser1).build();
         classList1For1 = readWriteDAO.create(classList1For1);
         classList2For1 = factory.newClassListBuilder(appUser1).withDescription("CL1-2").withLastObservationTimestamp(new LocalDateTime(2012, 11, 3, 15, 0, 0)).withAppUser(appUser1).build();
         classList2For1 = readWriteDAO.create(classList2For1);
         classList1For2 = factory.newClassListBuilder(appUser1).withDescription("CL2-1").withAppUser(appUser1).build();
         classList1For2 = readWriteDAO.create(classList1For2);
         oc1For1 = factory.newObservationCategoryBuilder(appUser1).withDescription("Description").withShortName("OC-1").build();
         oc1For1 = readWriteDAO.create(oc1For1);
         oc2For1 = factory.newObservationCategoryBuilder(appUser1).withDescription("Description").withShortName("OC-2").build();
         oc2For1 = readWriteDAO.create(oc2For1);
         student1For1 = factory.newStudentBuilder(appUser1).withLastName("Last1-1").withFirstName("First1-1").addClassList(classList1For1).withArchiveFlag(true).build();
         student1For1 = readWriteDAO.create(student1For1);
         student2For1 = factory.newStudentBuilder(appUser1).withLastName("Last2-1").withFirstName("First2-1").addClassList(classList2For1).build();
         student2For1 = readWriteDAO.create(student2For1);
         o1ForS1 = factory.newObservationBuilder(appUser1).addCategory(oc1For1).withObservationSubject(student1For1)
                 .withComment("Comment").withObservationTimestamp(new LocalDateTime(2013, 1, 18, 15, 12)).build();
         o1ForS1 = readWriteDAO.create(o1ForS1);
         o2ForS1 = factory.newObservationBuilder(appUser1).addCategory(oc1For1).withObservationSubject(student1For1)
                 .withFollowUpObservation(o1ForS1).withComment("Comment").withObservationTimestamp(new LocalDateTime(2012, 1, 18, 15, 12)).build();
         o2ForS1 = readWriteDAO.create(o2ForS1);
         photo1for1 = factory.newPhotoBuilder(appUser1).withTimestamp(new LocalDateTime(2011, 11, 11, 11, 11, 11)).withPhotoFor(student1For1).withDescription("Photo1").build();
         photo1for1 = readWriteDAO.create(photo1for1);
         photo2for1 = factory.newPhotoBuilder(appUser1).withTimestamp(new LocalDateTime(2012, 12, 12, 12, 12, 12)).withPhotoFor(o2ForS1).withDescription("Photo2").build();
         photo2for1 = readWriteDAO.create(photo2for1);
         photo3for1 = factory.newPhotoBuilder(appUser1).withTimestamp(new LocalDateTime(2013, 1, 1, 15, 12, 45)).withPhotoFor(classList1For1).withDescription("Photo3").build();
         photo3for1 = readWriteDAO.create(photo3for1);
         buildObjectToExpectedJSONMap();
     }
 
     @Test
     //  Easiest way to deep compare is to test read and write in one function
     public void testReadAndWriteSingleEntities() throws Exception {
         for (Map.Entry<IdObject, String> entry : jsonValues.entrySet()) {
             final IdObject object = serializer.read(entry.getValue());
             assertEquals(entry.getKey(), object);
             assertEquals(entry.getValue(), serializer.write(object));
         }
     }
 
     @Test
     //  Easiest way to deep compare is to test read and write in one function
     public void testReadAndWriteCollection() throws Exception {
         final StringBuilder builder = new StringBuilder("[ ");
         final List<IdObject> entities = new LinkedList<>();
         boolean first = true;
         for (Map.Entry<IdObject, String> entry : jsonValues.entrySet()) {
             if (first) {
                 first = false;
             } else {
                 builder.append(", ");
             }
             builder.append(entry.getValue());
             entities.add(entry.getKey());
         }
         builder.append(" ]");
 
         List<? extends IdObject> objects = serializer.read(builder.toString());
         assertEquals(entities.size(), objects.size());
         for (int i = 0; i < entities.size(); ++i) {
             assertEquals(entities.get(i), objects.get(i));
         }
         String jsonOutput = serializer.write(entities);
         assertEquals(builder.toString(), jsonOutput);
     }
 
     private void buildObjectToExpectedJSONMap() {
         jsonValues = new HashMap<IdObject, String>() {{
             put(appUser1, "{" + newline +
                     "  \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.AppUser\"," + newline +
                     "  \"emailAddress\" : \"jtest@test.com\"," + newline +
                     "  \"firstName\" : \"Testy\"," + newline +
                     "  \"id\" : \"" + appUser1.getId() + "\"," + newline +
                     "  \"lastLogout\" : 1355332333000," + newline +
                     "  \"lastName\" : \"Tester\"," + newline +
                     "  \"modificationTimestamp\" : " + appUser1.getModificationTimestamp().getMillis() + newline +
                     "}");
             put(appUser2, "{" + newline +
                     "  \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.AppUser\"," + newline +
                     "  \"emailAddress\" : \"jtest2@test.com\"," + newline +
                     "  \"firstName\" : \"Testier\"," + newline +
                     "  \"id\" : \"" + appUser2.getId() + "\"," + newline +
                     "  \"lastLogout\" : 946702800000," + newline +
                     "  \"lastName\" : \"Tester\"," + newline +
                     "  \"modificationTimestamp\" : " + appUser2.getModificationTimestamp().getMillis() + newline +
                     "}");
             put(classList1For1, "{" + newline +
                     "  \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.ClassList\"," + newline +
                     "  \"appUser\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.AppUser\"," + newline +
                     "    \"id\" : \"" + appUser1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"archived\" : false," + newline +
                     "  \"description\" : \"CL1-1\"," + newline +
                     "  \"id\" : \"" + classList1For1.getId() + "\"," + newline +
                     "  \"lastObservationTimestamp\" : [ 2013, 3, 30, 13, 5, 0, 0 ]," + newline +
                     "  \"modificationTimestamp\" : " + classList1For1.getModificationTimestamp().getMillis() + newline +
                     "}");
             put(classList2For1, "{" + newline +
                     "  \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.ClassList\"," + newline +
                     "  \"appUser\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.AppUser\"," + newline +
                     "    \"id\" : \"" + appUser1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"archived\" : false," + newline +
                     "  \"description\" : \"CL1-2\"," + newline +
                     "  \"id\" : \"" + classList2For1.getId() + "\"," + newline +
                     "  \"lastObservationTimestamp\" : [ 2012, 11, 3, 15, 0, 0, 0 ]," + newline +
                     "  \"modificationTimestamp\" : " + classList2For1.getModificationTimestamp().getMillis() + newline +
                     "}");
             put(classList1For2, "{" + newline +
                     "  \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.ClassList\"," + newline +
                     "  \"appUser\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.AppUser\"," + newline +
                     "    \"id\" : \"" + appUser1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"archived\" : false," + newline +
                     "  \"description\" : \"CL2-1\"," + newline +
                     "  \"id\" : \"" + classList1For2.getId() + "\"," + newline +
                     "  \"lastObservationTimestamp\" : [ 2000, 1, 1, 0, 0, 0, 0 ]," + newline +
                     "  \"modificationTimestamp\" : " + classList1For2.getModificationTimestamp().getMillis() + newline +
                     "}");
             put(oc1For1, "{" + newline +
                     "  \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.ObservationCategory\"," + newline +
                     "  \"appUser\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.AppUser\"," + newline +
                     "    \"id\" : \"" + appUser1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"archived\" : false," + newline +
                     "  \"description\" : \"Description\"," + newline +
                     "  \"id\" : \"" + oc1For1.getId() + "\"," + newline +
                     "  \"modificationTimestamp\" : " + oc1For1.getModificationTimestamp().getMillis() + "," + newline +
                     "  \"shortName\" : \"OC-1\"" + newline +
                     "}");
             put(oc2For1, "{" + newline +
                     "  \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.ObservationCategory\"," + newline +
                     "  \"appUser\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.AppUser\"," + newline +
                     "    \"id\" : \"" + appUser1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"archived\" : false," + newline +
                     "  \"description\" : \"Description\"," + newline +
                     "  \"id\" : \"" + oc2For1.getId() + "\"," + newline +
                     "  \"modificationTimestamp\" : " + oc2For1.getModificationTimestamp().getMillis() + "," + newline +
                     "  \"shortName\" : \"OC-2\"" + newline +
                     "}");
             put(student1For1, "{" + newline +
                     "  \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.Student\"," + newline +
                     "  \"appUser\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.AppUser\"," + newline +
                     "    \"id\" : \"" + appUser1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"archived\" : true," + newline +
                     "  \"classLists\" : [ {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.ClassList\"," + newline +
                     "    \"id\" : \"" + classList1For1.getId() + "\"" + newline +
                     "  } ]," + newline +
                     "  \"firstName\" : \"First1-1\"," + newline +
                     "  \"id\" : \"" + student1For1.getId() + "\"," + newline +
                     "  \"lastName\" : \"Last1-1\"," + newline +
                    "  \"lastObservationTimestamp\" : [ 2013, 1, 18, 15, 12, 0, 0 ]," + newline +
                     "  \"modificationTimestamp\" : " + student1For1.getModificationTimestamp().getMillis() + newline +
                     "}");
             put(student2For1, "{" + newline +
                     "  \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.Student\"," + newline +
                     "  \"appUser\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.AppUser\"," + newline +
                     "    \"id\" : \"" + appUser1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"archived\" : false," + newline +
                     "  \"classLists\" : [ {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.ClassList\"," + newline +
                     "    \"id\" : \"" + classList2For1.getId() + "\"" + newline +
                     "  } ]," + newline +
                     "  \"firstName\" : \"First2-1\"," + newline +
                     "  \"id\" : \"" + student2For1.getId() + "\"," + newline +
                     "  \"lastName\" : \"Last2-1\"," + newline +
                     "  \"lastObservationTimestamp\" : [ 2000, 1, 1, 0, 0, 0, 0 ]," + newline +
                     "  \"modificationTimestamp\" : " + student2For1.getModificationTimestamp().getMillis() + newline +
                     "}");
             put(o1ForS1, "{" + newline +
                     "  \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.Observation\"," + newline +
                     "  \"appUser\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.AppUser\"," + newline +
                     "    \"id\" : \"" + appUser1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"archived\" : false," + newline +
                     "  \"categories\" : [ {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.ObservationCategory\"," + newline +
                     "    \"id\" : \"" + oc1For1.getId() + "\"" + newline +
                     "  } ]," + newline +
                     "  \"comment\" : \"Comment\"," + newline +
                     "  \"followUpNeeded\" : false," + newline +
                     "  \"followUpObservation\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.Observation\"," + newline +
                     "    \"id\" : null" + newline +
                     "  }," + newline +
                     "  \"followUpReminder\" : null," + newline +
                     "  \"id\" : \"" + o1ForS1.getId() + "\"," + newline +
                     "  \"modificationTimestamp\" : " + o1ForS1.getModificationTimestamp().getMillis() + "," + newline +
                     "  \"observationSubject\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.Student\"," + newline +
                     "    \"id\" : \"" + student1For1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"observationTimestamp\" : [ 2013, 1, 18, 15, 12, 0, 0 ]," + newline +
                     "  \"significant\" : false" + newline +
                     "}");
             put(o2ForS1, "{" + newline +
                     "  \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.Observation\"," + newline +
                     "  \"appUser\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.AppUser\"," + newline +
                     "    \"id\" : \"" + appUser1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"archived\" : false," + newline +
                     "  \"categories\" : [ {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.ObservationCategory\"," + newline +
                     "    \"id\" : \"" + oc1For1.getId() + "\"" + newline +
                     "  } ]," + newline +
                     "  \"comment\" : \"Comment\"," + newline +
                     "  \"followUpNeeded\" : false," + newline +
                     "  \"followUpObservation\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.Observation\"," + newline +
                     "    \"id\" : \"" + o1ForS1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"followUpReminder\" : null," + newline +
                     "  \"id\" : \"" + o2ForS1.getId() + "\"," + newline +
                     "  \"modificationTimestamp\" : " + o2ForS1.getModificationTimestamp().getMillis() + "," + newline +
                     "  \"observationSubject\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.Student\"," + newline +
                     "    \"id\" : \"" + student1For1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"observationTimestamp\" : [ 2012, 1, 18, 15, 12, 0, 0 ]," + newline +
                     "  \"significant\" : false" + newline +
                     "}");
             put(photo1for1, "{" + newline +
                     "  \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.Photo\"," + newline +
                     "  \"appUser\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.AppUser\"," + newline +
                     "    \"id\" : \"" + appUser1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"archived\" : false," + newline +
                     "  \"description\" : \"Photo1\"," + newline +
                     "  \"id\" : \"" + photo1for1.getId() + "\"," + newline +
                     "  \"modificationTimestamp\" : " + photo1for1.getModificationTimestamp().getMillis() + "," + newline +
                     "  \"photoFor\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.Student\"," + newline +
                     "    \"id\" : \"" + student1For1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"timestamp\" : [ 2011, 11, 11, 11, 11, 11, 0 ]" + newline +
                     "}");
             put(photo2for1, "{" + newline +
                     "  \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.Photo\"," + newline +
                     "  \"appUser\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.AppUser\"," + newline +
                     "    \"id\" : \"" + appUser1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"archived\" : false," + newline +
                     "  \"description\" : \"Photo2\"," + newline +
                     "  \"id\" : \"" + photo2for1.getId() + "\"," + newline +
                     "  \"modificationTimestamp\" : " + photo2for1.getModificationTimestamp().getMillis() + "," + newline +
                     "  \"photoFor\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.Observation\"," + newline +
                     "    \"id\" : \"" + o2ForS1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"timestamp\" : [ 2012, 12, 12, 12, 12, 12, 0 ]" + newline +
                     "}");
             put(photo3for1, "{" + newline +
                     "  \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.Photo\"," + newline +
                     "  \"appUser\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.AppUser\"," + newline +
                     "    \"id\" : \"" + appUser1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"archived\" : false," + newline +
                     "  \"description\" : \"Photo3\"," + newline +
                     "  \"id\" : \"" + photo3for1.getId() + "\"," + newline +
                     "  \"modificationTimestamp\" : " + photo3for1.getModificationTimestamp().getMillis() + "," + newline +
                     "  \"photoFor\" : {" + newline +
                     "    \"entityType\" : \"com.jtbdevelopment.e_eye_o.entities.ClassList\"," + newline +
                     "    \"id\" : \"" + classList1For1.getId() + "\"" + newline +
                     "  }," + newline +
                     "  \"timestamp\" : [ 2013, 1, 1, 15, 12, 45, 0 ]" + newline +
                     "}");
         }};
     }
 }
