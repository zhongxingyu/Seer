 package org.narwhal.core.postgresql;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.narwhal.bean.Person;
 import org.narwhal.core.DatabaseConnection;
 import org.narwhal.core.DatabaseInformation;
 import org.narwhal.query.PostgreSQLQueryCreator;
 
 import java.sql.SQLException;
import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 
 /**
  * @author Miron Aseev
  */
 @RunWith(JUnit4.class)
 public class DatabaseConnectionTest {
 
     private static final String driver       = "org.postgresql.Driver";
     private static final String url          = "jdbc:postgresql://localhost/test";
     private static final String username     = "postgres";
     private static final String password     = "admin";
    private static final Date   johnBirthday = new GregorianCalendar(1990, 6, 9).getTime();
    private static final Date   doeBirthday  = new GregorianCalendar(1993, 3, 24).getTime();
 
 
     @Test
     public void transactionMethodsTest() throws SQLException {
         DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
         DatabaseConnection connection = null;
         int expectedRowAffected = 3;
         int result = 0;
 
         try {
             connection = new DatabaseConnection(databaseInformation, new PostgreSQLQueryCreator());
 
             try {
                 connection.beginTransaction();
 
                result += connection.executeUpdate("INSERT INTO Person (id, name, birthday) VALUES (?, ?, ?)", null, "Test", new Date());
                 result += connection.executeUpdate("UPDATE Person SET name = ? WHERE name = ?", "TestTest", "Test");
                 result += connection.executeUpdate("DELETE FROM Person WHERE name = ?", "TestTest");
 
                 connection.commit();
             } catch (Exception ex) {
                 connection.rollback();
             }
         } finally {
             if (connection != null) {
                 connection.close();
             }
         }
 
         try {
             Assert.assertEquals(expectedRowAffected, result);
         } finally {
             restoreDatabase();
         }
     }
 
     @Test
     public void createTest() throws SQLException {
         DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
         DatabaseConnection connection = null;
        Person person = new Person(null, "TestPerson", new Date());
         int expectedRowAffected = 1;
         int result;
 
         try {
             connection = new DatabaseConnection(databaseInformation, new PostgreSQLQueryCreator());
             result = connection.persist(person);
         } finally {
             if (connection != null) {
                 connection.close();
             }
         }
 
         try {
             Assert.assertEquals(expectedRowAffected, result);
         } finally {
             restoreDatabase();
         }
     }
 
     @Test
     public void readTest() throws SQLException {
         DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
         DatabaseConnection connection = null;
         Person person;
 
         try {
             connection = new DatabaseConnection(databaseInformation, new PostgreSQLQueryCreator());
             person = connection.read(Person.class, 1);
         } finally {
             if (connection != null) {
                 connection.close();
             }
         }
 
         try {
             Assert.assertEquals("John", person.getName());
             Assert.assertEquals(johnBirthday, person.getBirthday());
         } finally {
             restoreDatabase();
         }
     }
 
     @Test
     public void updateTest() throws SQLException {
         DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
         DatabaseConnection connection = null;
        Person person = new Person(1, "John Doe", new Date());
         int expectedRowAffected = 1;
         int result;
 
         try {
             connection = new DatabaseConnection(databaseInformation, new PostgreSQLQueryCreator());
             result = connection.update(person);
         } finally {
             if (connection != null) {
                 connection.close();
             }
         }
 
         try {
             Assert.assertEquals(expectedRowAffected, result);
         } finally {
             restoreDatabase();
         }
     }
 
     @Test
     public void deleteTest() throws SQLException {
         DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
         DatabaseConnection connection = null;
        Person person = new Person(1, "John", new Date());
         int expectedRowAffected = 1;
         int result;
 
         try {
             connection = new DatabaseConnection(databaseInformation, new PostgreSQLQueryCreator());
             result = connection.delete(person);
         } finally {
             if (connection != null) {
                 connection.close();
             }
         }
 
         try {
             Assert.assertEquals(expectedRowAffected, result);
         } finally {
             restoreDatabase();
         }
     }
 
     @Test
     public void executeUpdateTest() throws SQLException {
         DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
         DatabaseConnection connection = null;
         int doeId = 2;
         int expectedRowAffected = 1;
         int result;
 
         try {
             connection = new DatabaseConnection(databaseInformation, new PostgreSQLQueryCreator());
             result = connection.executeUpdate("UPDATE Person SET name = ? WHERE id = ?", "FunnyName", doeId);
         } finally {
             if (connection != null) {
                 connection.close();
             }
         }
 
         try {
             Assert.assertEquals(expectedRowAffected, result);
         } finally {
             restoreDatabase();
         }
     }
 
     @Test
     public void executeQueryTest() throws SQLException {
         DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
         DatabaseConnection connection = null;
         String expectedName = "John";
         Person person;
         int joeId = 1;
 
         try {
             connection = new DatabaseConnection(databaseInformation, new PostgreSQLQueryCreator());
             person = connection.executeQuery("SELECT * FROM Person WHERE id = ?", Person.class, joeId);
         } finally {
             if (connection != null) {
                 connection.close();
             }
         }
 
         Assert.assertNotNull(person);
         Assert.assertEquals(expectedName, person.getName());
     }
 
     @Test
     public void executeQueryForCollectionTest() throws SQLException {
         DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
         DatabaseConnection connection = null;
         List<Person> persons;
         int expectedSize = 2;
 
         try {
             connection = new DatabaseConnection(databaseInformation, new PostgreSQLQueryCreator());
             persons = connection.executeQueryForCollection("SELECT * FROM Person", Person.class);
         } finally {
             if (connection != null) {
                 connection.close();
             }
         }
 
         Assert.assertEquals(expectedSize, persons.size());
         Assert.assertEquals("John", persons.get(0).getName());
         Assert.assertEquals("Doe", persons.get(1).getName());
     }
 
     private void restoreDatabase() throws SQLException {
         DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
         DatabaseConnection connection = null;
 
         try {
             connection = new DatabaseConnection(databaseInformation, new PostgreSQLQueryCreator());
 
             try {
                 connection.beginTransaction();
 
                 connection.executeUpdate("DELETE FROM Person");
                 connection.executeUpdate("INSERT INTO Person (id, name, birthday) VALUES (?, ?, ?)", 1, "John", johnBirthday);
                 connection.executeUpdate("INSERT INTO Person (id, name, birthday) VALUES (?, ?, ?)", 2,  "Doe", doeBirthday);
 
                 connection.commit();
             } catch (SQLException ex) {
                 connection.rollback();
             }
         } finally {
             if (connection != null) {
                 connection.close();
             }
         }
     }
 }
