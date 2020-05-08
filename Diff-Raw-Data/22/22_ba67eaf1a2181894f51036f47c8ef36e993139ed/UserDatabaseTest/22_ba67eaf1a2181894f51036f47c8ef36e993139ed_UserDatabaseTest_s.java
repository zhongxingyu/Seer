 package test;
 import java.util.Date;
 import java.util.List;
 import models.dbentities.UserModel;
 import models.user.Gender;
 import models.user.Independent;
 import models.user.Organizer;
 import models.user.Teacher;
 import models.user.UserType;
 import models.user.User;
 import org.junit.Test;
 import com.avaje.ebean.Ebean;
 import java.security.SecureRandom;
 import java.math.BigInteger;
 import javax.persistence.PersistenceException;
 import junit.framework.Assert;
 
 public class UserDatabaseTest extends ContextTest {
 
     private SecureRandom random = new SecureRandom();
 
     /*
      * Test IndependentUser database insertion.
      */
     @Test
     public void makeIndependentUser() {
         String id = "id";
         String name = "Bertrand Russell";
         User userFind = null;
         UserModel mdl = new UserModel(id,UserType.PUPIL_OR_INDEP,
                 name,
                 new Date(),
                 new Date(),
                 "password",
                 "salt",
                 "mail@localhost",
                 Gender.Male,"nl");
         User user = new Independent(mdl);
 
         try{
             user.data.save();
         }catch(PersistenceException e){
             Assert.fail("Could not save the user");
         }
         Assert.assertNotNull(Ebean.find(UserModel.class).where().eq("id",user.data.id).where().eq("type", UserType.PUPIL_OR_INDEP.toString()).findUnique());
         UserModel userModel = Ebean.find(UserModel.class).where().eq("id", user.data.id).findUnique();
         Assert.assertEquals(true, userModel.gender == Gender.Male);
         Assert.assertEquals(true, userModel.preflanguage == "nl");
         Assert.assertEquals(true, userModel.email =="mail@localhost");
         Assert.assertEquals(true, userModel.password == "password");
         user.data.delete();
     }
 
     /*
      * Test teacher database insertion.
      */
     @Test
     public void makeTeacher(){
 
         int numberOfTeachers = 10;
         String teacherID = new BigInteger(130,random).toString();
         String name = "Maya Angelou";
 
         for(int i = 0; i < numberOfTeachers; i++){
             try{
                 new Teacher(new UserModel(new BigInteger(130,random).toString(),UserType.TEACHER,
                         new BigInteger(130,random).toString(),
                         new Date(),
                         new Date(),
                         "password",
                         "salt",
                         "mail@localhost",
                         Gender.Female,"nl")).data.save();
             }catch(PersistenceException e){Assert.fail("Could not save the user");}
         }
 
         List<UserModel> teacherList = UserModel.find.where().like("type", "TEACHER").findList();
         Assert.assertTrue(teacherList.size() == numberOfTeachers);
 
         User teacher = new Teacher(new UserModel(teacherID, UserType.TEACHER,
                 name,
                 new Date(),
                 new Date(),
                 "password",
                 "salt",
                 "mail@localhost",
                 Gender.Female,"nl"));
         teacher.data.save();
         
        
 
         Assert.assertNotNull(Ebean.find(UserModel.class).where().eq("id",teacherID).findUnique());
         Assert.assertNotNull(Ebean.find(UserModel.class).where().eq("name", name).findUnique());
         
         UserModel userModel = Ebean.find(UserModel.class).where().eq("id", teacher.data.id).findUnique();
         Assert.assertEquals(true, userModel.gender == Gender.Female);
         Assert.assertEquals(true, userModel.preflanguage == "nl");
         Assert.assertEquals(true, userModel.email =="mail@localhost");
         Assert.assertEquals(true, userModel.password == "password");
 
         teacher.data.delete();
     }
 
     @Test
     public void makeOrganizer(){
 
         int numberOfOrganizer = 10;
         String organizerID = new BigInteger(130,random).toString();
         String name = "Maya Angelou";
 
         for(int i = 0; i < numberOfOrganizer; i++){
             try{
                 new Organizer(new UserModel(new BigInteger(130,random).toString(),UserType.ORGANIZER,
                         new BigInteger(130,random).toString(),
                         new Date(),
                         new Date(),
                         "password",
                         "salt",
                         "mail@localhost",
                         Gender.Female,"en")).data.save();
             }catch(PersistenceException e){Assert.fail("Could not save the user");}
         }
 
         List<UserModel> organizerList = UserModel.find.where().like("type", "ORGANIZER").findList();
         Assert.assertTrue(organizerList.size() == numberOfOrganizer);
 
         User organizer = new Organizer(new UserModel(organizerID,
                 UserType.ORGANIZER,
                 name,
                 new Date(),
                 new Date(),
                 "password",
                 "salt",
                 "mail@localhost",
                 Gender.Female,"en"));
         organizer.data.save();
 
         Assert.assertNotNull(Ebean.find(UserModel.class).where().eq("id",organizerID).findUnique());
         Assert.assertNotNull(Ebean.find(UserModel.class).where().eq("name", name).findUnique());
         UserModel userModel = Ebean.find(UserModel.class).where().eq("id", organizer.data.id).findUnique();
         
         Assert.assertEquals(true, userModel.gender == Gender.Female);
         Assert.assertEquals(true, userModel.preflanguage == "en");
         Assert.assertEquals(true, userModel.email =="mail@localhost");
         Assert.assertEquals(true, userModel.password == "password");
 
         organizer.data.delete();
 
     }
 
     /**
      * Test user finder.
      */
     @Test
     public void findListBasedOnType(){
         int numberOfIndependentUser = 10;
        int numberOfPupils = 5;
 
         //First clear the Users
         for (UserModel um :UserModel.find.all()){
             um.delete();
         }
 
         //generate random IndependentUser and save them.
         for(int i = 0; i < numberOfIndependentUser; i++){
             try{
                 Independent ip = new Independent(new UserModel(new BigInteger(130,random).toString(),UserType.PUPIL_OR_INDEP,
                         new BigInteger(130,random).toString(),
                         new Date(),
                         new Date(),
                         "password",
                         "salt",
                         "mail@localhost",
                         Gender.Female,"nl"));
                 ip.data.save();
 
             }catch(PersistenceException e){Assert.fail("Could not save the user");}
         }
 
         //generate random Pupils.
        for(int i = 0; i < numberOfPupils; i++){
             try{
                Independent pu = new Independent(new UserModel(new BigInteger(130,random).toString(),UserType.PUPIL_OR_INDEP,
                         new BigInteger(130,random).toString(),
                         new Date(),
                         new Date(),
                         "password",
                         "salt",
                         "mail@localhost",
                         Gender.Female,"nl"));
 
                pu.data.save();
             }catch(PersistenceException e){Assert.fail("Could not save the user");}
         }
 
         List<UserModel> allUsers = UserModel.find.all();
         List<UserModel> allIndepententUser = UserModel.find.where().like("type", UserType.PUPIL_OR_INDEP.toString()).findList();
        List<UserModel> allPupils = UserModel.find.where().like("type",UserType.PUPIL_OR_INDEP.toString()).findList();
         System.out.println(allIndepententUser.size());
        System.out.println(allPupils.size());
        Assert.assertTrue(Integer.toString(allUsers.size()),allUsers.size() == numberOfIndependentUser+numberOfPupils);
         Assert.assertTrue("indep",allIndepententUser.size() == numberOfIndependentUser);
        Assert.assertTrue("pup",allPupils.size() == numberOfPupils);
     }
 }
