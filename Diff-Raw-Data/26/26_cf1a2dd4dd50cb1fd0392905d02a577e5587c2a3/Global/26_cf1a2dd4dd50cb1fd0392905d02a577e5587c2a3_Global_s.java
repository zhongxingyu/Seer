 import models.Contact;
 import models.ContactDB;
 import models.UserInfoDB;
 import play.*;
 import views.formdata.ContactFormData;
 
 /**
  * Global class.  Provides Contacts that are included on startup.
  * @author AJ
  *
  */
 public class Global extends GlobalSettings {
   
   
   /**
    * Adds the four contacts to the database on startup.
    */
   public void onStart(Application app) {
    Logger.info("Application has started.");
    Contact contact1 = new Contact(1, "Philip", "Johnson", "123-456-7890", "Home");
    Contact contact2 = new Contact(2, "Jane", "Doe", "477-456-7890", "Work");
    Contact contact3 = new Contact(3, "Justin", "Verlander", "999-456-8888", "Home");
    Contact contact4 = new Contact(4, "Gordie", "Howe", "654-456-2345", "Mobile");
     
     ContactDB.add(new ContactFormData(contact1));
     ContactDB.add(new ContactFormData(contact2));
     ContactDB.add(new ContactFormData(contact3));
     ContactDB.add(new ContactFormData(contact4));
     
     UserInfoDB.addUserInfo("John Smith", "smith@example.com", "password");
   }
 
 }
