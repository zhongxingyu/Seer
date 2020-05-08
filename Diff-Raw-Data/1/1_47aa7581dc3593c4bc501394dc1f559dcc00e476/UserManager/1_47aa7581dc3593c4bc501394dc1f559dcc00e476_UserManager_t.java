 package org.makumba.parade.model.managers;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.model.Parade;
 import org.makumba.parade.model.User;
 
 /**
  * This model manager handles User-s (creation, edition)
  * 
  * @author Manuel Gay
  */
 public class UserManager {
 
     public Object[] createUser(String login, String name, String surname, String nickname, String email) {
         User u = new User(login, name, surname, nickname, email);
 
         boolean success = false;
         Session s = null;
         Transaction tx = null;
         String result = "User account for " + name + " successfully created!";
 
         try {
             s = InitServlet.getSessionFactory().openSession();
             tx = s.beginTransaction();
 
             Parade p = (Parade) s.get(Parade.class, new Long(1));
 
             // check if user with same login already exists
             if (p.getUsers().get(login) != null) {
                 result = "User with login " + login + " already exists!";
                 success = false;
             } else {
                u.setParade(p);
                 p.getUsers().put(u.getLogin(), u);
                 success = true;
             }
 
         } finally {
             tx.commit();
             s.close();
         }
 
         return new Object[] { result, success, u };
     }
 
 }
