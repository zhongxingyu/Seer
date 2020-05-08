 package nl.astraeus.persistence.example;
 
 import nl.astraeus.persistence.PersistentManager;
 import nl.astraeus.persistence.SimplePersistent;
 import nl.astraeus.persistence.SimplePersistentDao;
 import nl.astraeus.persistence.Transaction;
 
 /**
  * User: rnentjes
  * Date: 12/16/12
  * Time: 12:45 PM
  */
 public class MinimalExample {
 
     public static class User extends SimplePersistent {
         private final static long serialVersionUID = 1L;
 
         private String name;
         private String title;
 
         public User(String name, String title) {
             this.name = name;
             this.title = title;
         }
 
         public String getName() {
             return name;
         }
 
         public String getTitle() {
             return title;
         }
     }
 
     public static class UserDao extends SimplePersistentDao<User> {
         public User findUserByName(String name) {
             return createQuery().where("name", name).getSingleResult();
         }
     }
 
     public MinimalExample() {
         System.setProperty(PersistentManager.DATA_DIRECTORY, "minimalexample");
 
         createUser();
         showUsers();
         findUser("User-1");
     }
 
     public void createUser() {
         new Transaction() {
 
             @Override
             public void execute() {
                 UserDao dao = new UserDao();
 
                 User user = new User("User-" + (dao.size() + 1), "More date here");
 
                 dao.store(user);
             }
         };
     }
 
     public void showUsers() {
         UserDao dao = new UserDao();
 
         for (User user : dao.findAll()) {
             System.out.println("User :" + user.getName() + ", " + user.getTitle());
         }
     }
 
     public void findUser(String name) {
         UserDao dao = new UserDao();
 
         User user = dao.findUserByName(name);
 
         System.out.println("Found user :" + user.getName() + ", " + user.getTitle());
     }
 
     public static void main(String[] args) {
         new MinimalExample();
     }
 }
