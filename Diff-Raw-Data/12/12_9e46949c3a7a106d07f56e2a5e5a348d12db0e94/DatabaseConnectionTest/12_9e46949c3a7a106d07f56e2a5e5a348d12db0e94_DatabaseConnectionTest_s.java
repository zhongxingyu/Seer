 package org.narwhal.core;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.narwhal.bean.Person;
 
 import java.lang.reflect.InvocationTargetException;
 import java.sql.SQLException;
 import java.util.List;
 
 /**
  * @author Miron Aseev
  */
 @RunWith(JUnit4.class)
 public class DatabaseConnectionTest {
 
     private DatabaseConnection connection;
 
 
     public DatabaseConnectionTest() throws SQLException, ClassNotFoundException {
         String driver = "com.mysql.jdbc.Driver";
         String url = "jdbc:mysql://localhost/bank";
         String username = "lrngsql";
         String password = "lrngsql";
         DatabaseInformation information = new DatabaseInformation(driver, url, username, password);
         connection  = new DatabaseConnection(information);
     }
 
     @Test
     public void transactionMethodsTest() throws SQLException {
         int expectedRowAffected = 3;
         int result = 0;
 
         try {
             connection.beginTransaction();
 
             result += connection.executeUpdate("INSERT INTO Person (id, name) VALUES (?, ?)", null, "Test");
             result += connection.executeUpdate("UPDATE Person SET name = ? WHERE name = ?", "TestTest", "Test");
             result += connection.executeUpdate("DELETE FROM Person WHERE name = ?", "TestTest");
 
             connection.commit();
        } finally {
             connection.rollback();
         }
 
         try {
             Assert.assertEquals(expectedRowAffected, result);
         } finally {
             restoreDatabase();
         }
     }
 
     @Test
     public void createTest() throws InvocationTargetException, NoSuchMethodException, SQLException, IllegalAccessException {
         Person person = new Person(null, "TestPerson");
         int expectedRowAffected = 1;
         int result = connection.create(person);
 
         try {
             Assert.assertEquals(expectedRowAffected, result);
         } finally {
             restoreDatabase();
         }
     }
 
     @Test
     public void readTest() throws InvocationTargetException, NoSuchMethodException,
             InstantiationException, SQLException, IllegalAccessException {
         Person person = connection.read(Person.class, 1);
 
         try {
             Assert.assertEquals("John", person.getName());
         } finally {
             restoreDatabase();
         }
     }
 
     @Test
     public void updateTest() throws SQLException, NoSuchMethodException,
             IllegalAccessException, InvocationTargetException {
         Person person = new Person(1, "John Doe");
         int expectedRowAffected = 1;
         int result = connection.update(person);
 
         try {
             connection.update(person);
         } finally {
             restoreDatabase();
         }
     }
 
     @Test
     public void deleteTest() throws SQLException, NoSuchMethodException,
             IllegalAccessException, InvocationTargetException {
         Person person = new Person(1, "John");
         int expectedRowAffected = 1;
         int result = connection.delete(person);
 
         try {
             Assert.assertEquals(expectedRowAffected, result);
         } finally {
             restoreDatabase();
         }
     }
 
     @Test
     public void executeUpdateTest() throws SQLException {
         int doeId = 2;
         int expectedRowAffected = 1;
         int result = connection.executeUpdate("UPDATE Person SET name = ? WHERE id = ?", "FunnyName", doeId);
 
         try {
             Assert.assertEquals(expectedRowAffected, result);
         } finally {
             restoreDatabase();
         }
     }
 
     @Test
     public void executeQueryTest() throws SQLException, IllegalAccessException,
             InstantiationException, NoSuchMethodException, InvocationTargetException {
         String expectedName = "John";
         int joeId = 1;
         Person person = connection.executeQuery("SELECT * FROM Person WHERE id = ?", Person.class, joeId);
 
         Assert.assertNotNull(person);
         Assert.assertEquals(expectedName, person.getName());
     }
 
     @Test
     public void executeQueryForCollectionTest() throws SQLException, ClassNotFoundException,
             NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
         List<Person> persons = connection.executeQueryForCollection("SELECT * FROM Person", Person.class);
         int expectedSize = 2;
 
         Assert.assertNotNull(persons);
         Assert.assertEquals(expectedSize, persons.size());
         Assert.assertEquals("John", persons.get(0).getName());
         Assert.assertEquals("Doe", persons.get(1).getName());
     }
 
     private void restoreDatabase() throws SQLException {
         try {
             connection.beginTransaction();
 
             connection.executeUpdate("DELETE FROM Person");
             connection.executeUpdate("INSERT INTO Person (id, name) VALUES (?, ?)", 1, "John");
             connection.executeUpdate("INSERT INTO Person (id, name) VALUES (?, ?)", 2,  "Doe");
 
             connection.commit();
         } finally {
             connection.rollback();
         }
     }
 }
