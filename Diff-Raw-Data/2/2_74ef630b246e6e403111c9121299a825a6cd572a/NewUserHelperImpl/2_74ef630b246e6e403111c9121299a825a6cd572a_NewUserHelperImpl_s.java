 package com.jtbdevelopment.e_eye_o.DAO.helpers.example;
 
 import com.jtbdevelopment.e_eye_o.DAO.ReadWriteDAO;
 import com.jtbdevelopment.e_eye_o.DAO.helpers.NewUserHelper;
 import com.jtbdevelopment.e_eye_o.DAO.helpers.ObservationCategoryHelper;
 import com.jtbdevelopment.e_eye_o.entities.*;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalDateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.Resource;
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * Date: 6/2/13
  * Time: 3:27 PM
  */
 @Component
 @SuppressWarnings("unused")
 public class NewUserHelperImpl implements NewUserHelper {
     private static final Logger logger = LoggerFactory.getLogger(NewUserHelperImpl.class);
 
     @Autowired
     private ObservationCategoryHelper observationCategoryHelper;
 
     @Autowired
     protected IdObjectFactory objectFactory;
 
     @Autowired
     protected ReadWriteDAO readWriteDAO;
 
     @Autowired
     protected IdObjectFactory idObjectFactory;
 
     @Resource(name = "newUserDefaultObservationCategories")
     Map<String, String> newUserDefaultObservationCategories;
 
     private void createDefaultCategoriesForUser(final AppUser appUser) {
         for (Map.Entry<String, String> entry : newUserDefaultObservationCategories.entrySet()) {
             readWriteDAO.create(objectFactory.newObservationCategoryBuilder(appUser).withShortName(entry.getKey()).withDescription(entry.getValue()).build());
         }
     }
 
     @Override
     public void initializeNewUser(final AppUser newUser) {
         createDefaultCategoriesForUser(newUser);
 
         Map<String, ObservationCategory> map = observationCategoryHelper.getObservationCategoriesAsMap(newUser);
         ClassList cl = readWriteDAO.create(idObjectFactory.newClassListBuilder(newUser).withDescription("Example Class").build());
         Student s1 = readWriteDAO.create(idObjectFactory.newStudentBuilder(newUser).withFirstName("Student").withLastName("A").addClassList(cl).build());
         Student s2 = readWriteDAO.create(idObjectFactory.newStudentBuilder(newUser).withFirstName("Student").withLastName("B").addClassList(cl).build());
         final Iterator<Map.Entry<String, ObservationCategory>> entryIterator = map.entrySet().iterator();
         ObservationCategory c1 = entryIterator.next().getValue();
         ObservationCategory c2 = entryIterator.next().getValue();
         Observation o1 = readWriteDAO.create(idObjectFactory.newObservationBuilder(newUser).withObservationTimestamp(new LocalDateTime().minusDays(7)).withObservationSubject(s1).withComment("Observation 1").addCategory(c1).build());
         Observation o2 = readWriteDAO.create(idObjectFactory.newObservationBuilder(newUser).withObservationTimestamp(new LocalDateTime().minusDays(3)).withObservationSubject(s1).withComment("Observation 2").addCategory(c1).addCategory(c2).build());
         readWriteDAO.create(idObjectFactory.newObservationBuilder(newUser).withObservationTimestamp(new LocalDateTime().minusDays(10)).withObservationSubject(s2).withComment("Observation 3").build());
         readWriteDAO.create(idObjectFactory.newObservationBuilder(newUser).withObservationSubject(cl).withObservationTimestamp(new LocalDateTime().minusDays(1)).addCategory(c2).withComment("You can put general class observations too.").build());
         Semester semester1 = readWriteDAO.create(idObjectFactory.newSemesterBuilder(newUser).withDescription("Semester 1 - Used to group observations by time").withEnd(new LocalDate().minusDays(5)).withStart(new LocalDate().minusDays(60)).build());
         Semester semester2 = readWriteDAO.create(idObjectFactory.newSemesterBuilder(newUser).withDescription("Semester 2 - Used to group observations by time").withEnd(new LocalDate().plusDays(65)).withStart(new LocalDate().minusDays(4)).build());
 
 
         for (String string : Arrays.asList(
                 "class-work-example.jpg",
                 "student-work-example.jpg"
         )) {
             try {
                 final String defaultImage = "newusersamplephotos/" + string;
                URL url = com.jtbdevelopment.e_eye_o.DAO.helpers.UserHelperImpl.class.getClassLoader().getResource(defaultImage);
                 if (url == null) {
                     logger.warn("Unable to locate default image " + defaultImage);
                     continue;
                 }
                 BufferedImage image = ImageIO.read(new File(url.getFile()));
                 final ByteArrayOutputStream imOS = new ByteArrayOutputStream();
                 ImageIO.write(image, "jpg", imOS);
                 imOS.close();
                 image.flush();
                 Photo photo = idObjectFactory.newPhotoBuilder(newUser).withDescription(string).withMimeType("image/jpeg").withImageData(imOS.toByteArray()).withTimestamp(new LocalDateTime()).build();
                 if (url.getFile().contains("student")) {
                     photo.setPhotoFor(o1);
                 } else {
                     photo.setPhotoFor(cl);
                 }
                 readWriteDAO.create(photo);
             } catch (IOException e) {
                 logger.warn("There was an error creating new user sample photos!", e);
             }
         }
     }
 }
