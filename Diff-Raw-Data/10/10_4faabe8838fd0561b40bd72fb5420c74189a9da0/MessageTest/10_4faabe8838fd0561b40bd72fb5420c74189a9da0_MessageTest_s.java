 /**
  * Test of Message persistence.
  */
 package fr.lalourche.model;
 
 import java.io.File;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * @author Lalourche
  */
 public class MessageTest
 {
 
   /**
    * @throws java.lang.Exception
    *           if anything bad occurs.
    */
   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
     // Start from default database
    File emptyDatabaseDir = new File("./database/default");
    File currentDatabaseDir = new File("./database");
     FileUtils.copyDirectory(emptyDatabaseDir, currentDatabaseDir);
   }
 
   /**
    * @throws java.lang.Exception
    *           if anything bad occurs.
    */
   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
     // Add a small tempo in order for the JVM not to close too soon
     // and allow the complete writing of data into the database
     try {
       Thread.sleep(1000);
     }
     catch (InterruptedException e) {
       e.printStackTrace();
     }
   }
 
   /**
    * @throws java.lang.Exception
    *           if anything bad occurs.
    */
   @Before
   public void setUp() throws Exception
   {
   }
 
   /**
    * @throws java.lang.Exception
    *           if anything bad occurs.
    */
   @After
   public void tearDown() throws Exception
   {
   }
 
   /**
    * CRUD test.
    */
   @Test
   public final void testCRUD()
   {
     System.out.println("******* CREATE *******");
     String s = "Toto";
     Message m = new Message(s);
     m.save();
     Long id = m.getId();
     Assert.assertTrue(id > 0);
 
     System.out.println("******* READ *******");
     Entity.read(id, Message.class);
     System.out.println(m);
     Assert.assertEquals(m.getValue(), s);
 
     System.out.println("******* UPDATE *******");
     String newValue = "Titi";
     m.setValue(newValue);
     m.update();
     Assert.assertEquals(m.getId(), id);
     m = (Message) Entity.read(id, Message.class);
     System.out.println(m);
     Assert.assertEquals(m.getValue(), newValue);
 
     System.out.println("******* LIST *******");
     List<Message> allMessages = (List<Message>) Entity.list(Message.class);
     Assert.assertEquals(allMessages.size(), 1);
 
     System.out.println("******* DELETE *******");
     m = (Message) Entity.read(id, Message.class);
     m.delete();
     m = (Message) Entity.read(id, Message.class);
     Assert.assertNull(m);
   }
 
   /**
    * Test multiple entities handling.
    * Tests list and deleteAll
    */
   @Test
   public final void testMultiple()
   {
     // Create multiple messages
     String s = "Toto";
     final int max = 100;
     for (int i = 0; i < max; i++) {
       Message m = new Message(s);
       m.save();
     }
 
     System.out.println("******* LIST *******");
     List<Message> allMessages = (List<Message>) Entity.list(Message.class);
     Assert.assertEquals(max, allMessages.size());
 
     System.out.println("******* DELETE ALL *******");
     Entity.deleteAll(Message.class);
     allMessages = (List<Message>) Entity.list(Message.class);
     Assert.assertEquals(0, allMessages.size());
   }
 }
