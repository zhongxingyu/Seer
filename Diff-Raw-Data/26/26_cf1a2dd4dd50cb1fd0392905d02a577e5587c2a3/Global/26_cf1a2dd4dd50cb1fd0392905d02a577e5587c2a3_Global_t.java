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
    long id = 0;
    Contact contact1 = new Contact(id++, "Philip", "Johnson", "123-456-7890", "Home");
    Contact contact2 = new Contact(id++, "Jane", "Doe", "477-456-7890", "Work");
    Contact contact3 = new Contact(id++, "Justin", "Verlander", "999-456-8888", "Home");
    Contact contact4 = new Contact(id++, "Gordie", "Howe", "654-456-2345", "Mobile");
     
     ContactDB.add(new ContactFormData(contact1));
     ContactDB.add(new ContactFormData(contact2));
     ContactDB.add(new ContactFormData(contact3));
     ContactDB.add(new ContactFormData(contact4));
     
     UserInfoDB.addUserInfo("John Smith", "smith@example.com", "password");
   }
 
 }
