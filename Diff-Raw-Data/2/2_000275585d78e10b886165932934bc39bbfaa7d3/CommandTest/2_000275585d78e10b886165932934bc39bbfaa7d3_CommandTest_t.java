 package com.fenrissoftwerks.loki;
 
 import com.google.gson.Gson;
 import junit.framework.TestCase;
 
 public class CommandTest extends TestCase {
 
     public void testSetAndGetCommandName() throws Exception {
         Command command = new Command();
         command.setCommandName("foo");
         assertEquals("foo", command.getCommandName());
     }
 
     public void testSetAndGetCommandArgs() throws Exception {
         Command command = new Command();
         Object[] args = new Object[2];
         args[0] = new String("foo");
         args[1] = new Integer(1);
         command.setCommandArgs(args);
         Object[] outArgs = command.getCommandArgs();
         assertEquals("foo", outArgs[0]);
        assertEquals(1, outArgs[1]);
     }
 
     public void testGson() throws Exception {
         Command command = new Command();
         command.setCommandName("createPlayer");
         command.setCommandArgs(new Object[]{"jason","foobar"});
         Gson gson = new Gson();
         String commandAsJSON = gson.toJson(command);
 
     }
 }
