 package com.maestrodev;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import org.json.simple.JSONObject;
 
 /**
  * Unit test for simple App.
  */
 public class IrcWorkerTest 
     extends TestCase
 {
     /**
      * Create the test case
      *
      * @param testName name of the test case
      */
     public IrcWorkerTest( String testName )
     {
         super( testName );
     }
 
     /**
      * @return the suite of tests being tested
      */
     public static Test suite()
     {
         return new TestSuite( IrcWorkerTest.class );
     }
     
     /**x
      * Test IrcWorker
      */
     public void testPostMessage() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
     {
         IrcWorker ircWorker = new IrcWorker();
         JSONObject fields = new JSONObject();
         fields.put("body", "Hello From Maestro 4!\rGoodbye from Maestro4\nI am lost...");
         fields.put("nickname", "irc-plugin-test");        
         fields.put("server", "irc.freenode.net");
         fields.put("password", "");
         fields.put("ssl", "false");
         fields.put("port", "6667");
         fields.put("channel", "#kittest");
         fields.put("ignore_failure", "false");
         
         JSONObject workitem = new JSONObject();
         workitem.put("fields", fields);
         ircWorker.setWorkitem(workitem);
                
         Method method = ircWorker.getClass().getMethod("postMessage");
         method.invoke(ircWorker);
         
         assertNull(ircWorker.getField("__error__"));
     }
     
    
          /**
      * Test IrcWorker
      */
     public void testIgnoreFailureOnFailure() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
     {
         
         IrcWorker ircWorker = new IrcWorker();
         JSONObject fields = new JSONObject();
         fields.put("body", "Hello From Maestro 4!\rGoodbye from Maestro4\nI am lost...");
         fields.put("nickname", "irc-plugin-test");        
         fields.put("server", "not.real.com");
         fields.put("password", "");
         fields.put("ssl", "false");
         fields.put("port", "6667");
         fields.put("channel", "#kittest");
         fields.put("ignore_failure", "true");
         
         JSONObject workitem = new JSONObject();
         workitem.put("fields", fields);
         ircWorker.setWorkitem(workitem);
                
         Method method = ircWorker.getClass().getMethod("postMessage");
         method.invoke(ircWorker);
         
         assertNull(ircWorker.getField("__error__"));
     }
     
     
     public void testDontIgnoreFailureOnFailure() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
     {
         
         IrcWorker ircWorker = new IrcWorker();
         JSONObject fields = new JSONObject();
         fields.put("body", "Hello From Maestro 4!\rGoodbye from Maestro4\nI am lost...");
         fields.put("nickname", "irc-plugin-test");        
         fields.put("server", "not.real.com");
         fields.put("password", "");
         fields.put("ssl", "false");
         fields.put("port", "6667");
         fields.put("channel", "#kittest");
         fields.put("ignore_failure", "false");
         
         JSONObject workitem = new JSONObject();
         workitem.put("fields", fields);
         ircWorker.setWorkitem(workitem);
                
         Method method = ircWorker.getClass().getMethod("postMessage");
         method.invoke(ircWorker);
         
         assertNotNull(ircWorker.getField("__error__"));
     }
      /**
      * Test IrcWorker
      */
     public void testPostMessageAndWaitForConfirmation() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
     {
         IrcWorker ircWorker = new IrcWorker();
         JSONObject fields = new JSONObject();
         fields.put("body", "Something Happened Would You like to proceed?");
         fields.put("nickname", "irc-plugin-test2");        
         fields.put("server", "irc.freenode.net");
         fields.put("password", null);
         fields.put("ssl", "false");
         fields.put("port", "6667");
         fields.put("channel", "#kittest");
         fields.put("ignore_failure", "false");
         
         
         JSONObject workitem = new JSONObject();
         workitem.put("fields", fields);
         ircWorker.setWorkitem(workitem);
                
         
         Method method = ircWorker.getClass().getMethod("postMessageAndWaitForConfirmation");
         assertNotNull(method);
         
     }
 }
