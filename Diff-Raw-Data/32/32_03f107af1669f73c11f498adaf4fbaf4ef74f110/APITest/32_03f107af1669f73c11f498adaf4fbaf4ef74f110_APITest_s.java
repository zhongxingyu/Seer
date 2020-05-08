 package com.gigaspaces.anyapi;
 
import com.gigaspaces.async.AsyncResult;
 import com.gigaspaces.document.SpaceDocument;
 import com.j_spaces.core.client.SQLQuery;
 import org.openspaces.core.GigaSpace;
 import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.executor.DistributedTask;
import org.openspaces.core.executor.TaskGigaSpace;
 import org.openspaces.core.space.UrlSpaceConfigurer;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.TypedQuery;
 import java.sql.*;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 public class APITest {
     final GigaSpace gigaSpace = new GigaSpaceConfigurer(
            new UrlSpaceConfigurer("jini://*/*/mySpace").create()).create();
     Programmer programmer;
 
     @BeforeTest
     public void writeData() {
         gigaSpace.clean();
         gigaSpace.write(new Programmer(123, "Me", "groovy"));
         gigaSpace.write(new Programmer(345, "you", "java"));
     }
 
     @Test
     public void testPOJO() {
         /******************* POJO *******************************/
 
         programmer = gigaSpace.read(new Programmer(123));
         System.out.println(programmer);
     }
 
     @Test(dependsOnMethods = "testPOJO")
     public void testDocument() {
 
         /****************** Document ***************************/
         SpaceDocument document = new SpaceDocument("com.gigaspaces.anyapi.Programmer");
 
         document.setProperty("id", 345);
         SpaceDocument programmerDoc = gigaSpace.read(document);
 
         System.out.println(programmerDoc);
 
         document.setProperty("id", 789);
         document.setProperty("name", "he");
         document.setProperty("age", 21);
         gigaSpace.write(document);
 
     }
 
     @Test(dependsOnMethods = "testDocument")
     public void testSQLQuery() {
         /****************** SQLQuery ***************************/
         programmer = gigaSpace.read(new SQLQuery<Programmer>(Programmer.class,
                 "id=789 AND name='he'"));
         System.out.println(programmer);
     }
 
     @Test(dependsOnMethods = "testSQLQuery")
     public void testJDBC() throws ClassNotFoundException, SQLException {
         /****************** JDBC *******************************/
         Class.forName("com.j_spaces.jdbc.driver.GDriver");
         Connection connection = DriverManager.getConnection("jdbc:gigaspaces:url:jini://*/*/mySpace");
         Statement statement = connection.createStatement();
 
         statement.execute("SELECT * FROM com.gigaspaces.anyapi.Programmer WHERE age=21");
 
         ResultSet resultSet = statement.getResultSet();
         while (resultSet.next())
             System.out.println("JDBC: id=" + resultSet.getInt("id") + " name=" + resultSet.getString("name") + " age=" + resultSet.getInt("age"));
 
     }
 
     @Test(dependsOnMethods = "testJDBC")
     public void testJPA() {
         /****************** JPA ********************************/
         EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("anyapi", null);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        programmer = entityManager.find(Programmer.class, 345);
        System.out.println(programmer);

        TypedQuery<Programmer> query = entityManager.createQuery(
                "SELECT p FROM com.gigaspaces.anyapi.Programmer p WHERE p.id=123", Programmer.class);

        List<Programmer> resultList = query.getResultList();
        System.out.println(resultList.get(0));
     }
 
     @Test(dependsOnMethods = "testJPA")
     public void testMapReduce() throws ExecutionException, InterruptedException {
 
         /******************** Executor (Map-Reduce) *****************/
         Future<Integer> count = gigaSpace.execute(new ProgrammerCountTask());
 
         System.out.println(count.get());
     }
 }
