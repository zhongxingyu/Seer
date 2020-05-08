 import models.Customer;
 import play.Application;
 import play.GlobalSettings;
 
 public class Global extends GlobalSettings {
 
     public void onStart(Application app) {
         InitialData.insert(app);
     }
 
     static class InitialData {
 
         public static void insert(Application app) {
            Customer c1 = new Customer();
            c1.name = "Foo";
            Customer c2 = new Customer();
            c2.name = "Bar";
            c2.referredBy = c1;
            Customer c3 = new Customer();
            c3.name = "baz";
            c3.referredBy = c1;
 
            c1.save();
            c2.save();
            c3.save();
         }
 
     }
 
 }
