 /**
  * Copyright (C) 2013 Tokanagrammar Team
  *
  * This is a jigsaw-like puzzle game,
  * except each piece is token from a source file,
  * and the 'complete picture' is the program.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package edu.umb.cs.api.service;
 
 import edu.umb.cs.entity.Hint;
 import edu.umb.cs.entity.Puzzle;
 import edu.umb.cs.entity.User;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 /**
  * @author              Vy Thuy Nguyen
  * @version             1.0 Mar 23, 2013
  * Last modified:       
  */
 public class DatabaseService 
 { 
     private static Map<String, String> properties = new HashMap<String, String>();
     private static String dbName = "Tokanagrammar.odb";
     private static String partialURL = "$objectdb/db/";
     private static EntityManagerFactory emf;
     private static EntityManager em;
     
     /**
      * Start database connection with default settings
      * Call this when program starts
      * 
      * dbName = Tokanagrammar
      * partialURL = "$objectdb/db/"
      * 
      * @param String newName
      */
     public static void openConnection(String newName)
     {
         //dbName = "Tokanagrammar.odb";
         dbName = newName + ".odb";
         partialURL = "$objectdb/db/";
         emf = Persistence.createEntityManagerFactory(partialURL + dbName, properties);
         em = emf.createEntityManager();
         em.getTransaction().begin();
     }
 
     /**
      * Start database connection with given settings
      * Call this when program starts.
      * 
      * @param newDbName
      * @param newPartialURL
      * @param user
      * @param password 
      */
     public static void openConnection(String newDbName, String newPartialURL, String user, String password)
     {
         dbName = newDbName;
         partialURL = newPartialURL;
         properties.put("javax.persistence.jdbc.user", user);
         properties.put("javax.persistence.jdbc.password", password);
         emf = Persistence.createEntityManagerFactory(partialURL + dbName, properties);
         em = emf.createEntityManager();
         em.getTransaction().begin();
     }
     
     /**
      * Commit all everything and close connection
      * Call this upon exiting the program
      */
     public static void closeConnection()
     {   
        em.getTransaction().commit();
         em.close();
         emf.close();
     }
     
     public static void deleteAll()
     {
         em.createQuery("DELETE FROM User u").executeUpdate();
         em.createQuery("DELETE FROM Puzzle p").executeUpdate();
         em.createQuery("DELETE FROM Game g").executeUpdate();
     }
 
     /**
      * 
      * @return a list of all puzzles in the database
      */
     public static List<Puzzle> getAllPuzzles()
     {
         return em.createQuery("SELECT p FROM Puzzle p", Puzzle.class).getResultList();
     }
     
     //List of users
     public static List<User> getAllUsers()
     {
         return em.createQuery("SELECT u FROM User u", User.class).getResultList();
     }
     
     /**
      * Add a new puzzle to the database
      * 
      * @param filePath the path to the source file (relative to execution directory)
      * @param expResult expected result of the program
      * @param metaData meta data
      * @return true if the source file can be found and added correctly
      */
     public static boolean addPuzzle(String filePath, String expResult, String metaData, Hint... hints)
     {
         try
         {
             Puzzle p = new Puzzle(filePath, expResult, metaData);
             for (Hint h : hints)
                 p.addHint(h);
             em.persist(p);
             em.getTransaction().commit();
         }
         catch (IOException exc)
         {
             return false;
         }
         
         return true;
     }
     
     /**
      * Remove the puzzle with given name from db
      * 
      * @param filePath
      * @return true if there is such puzzle, false otherwise; 
      */
     public static boolean removePuzzle(String filePath)
     {
         int count = em.createQuery("DELETE FROM Puzzle p WHERE p.filePath = :filePath", Puzzle.class)
                      .setParameter("filePath", filePath)
                      .executeUpdate();
         return (count == 0 ? false : true);
     }
     
     /**
      * 
      * @deprecated
      */
     private static void persistPuzzle(Puzzle p)
     {
         em.persist(p);
         em.getTransaction().commit();
     }
     
     /**
      * 
      * @param username
      * @return true if the username has already existed
      */
     public static boolean usernameExists(String username)
     {
         List<User> exist = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                        .setParameter("username", username)
                        .getResultList();
         
         if (exist.size() > 0)
             return true;
         else 
             return false;
     }
     
     /**
      * Add a new user to the database
      * Will throw exception if user
      * 
      * @param username 
      * @return the user just got created
      * @throws Exception if username is not available
      */
     public static User addUser(String username) throws Exception
     {
         if (usernameExists(username))
             throw new Exception("Username has already existed");
         
         User u = new User(username);
         em.persist(u);
         return u;
     }
     
     public static void removeUser(User u)
     {
         
     }
     
     public static void persistUser(User u)
     {
         em.persist(u);
         em.getTransaction().commit();
     }
     
     public static void dropDatabase(String name)
     {
         
     }
 }
