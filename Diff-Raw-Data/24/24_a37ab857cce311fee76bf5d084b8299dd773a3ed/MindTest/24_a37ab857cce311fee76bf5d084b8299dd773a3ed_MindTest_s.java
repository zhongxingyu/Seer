 /**
  * Project "com.quui.chat.core" (C) 2006 Fabian Steeg This library is free
  * software; you can redistribute it and/or modify it under the terms of the GNU
  * Lesser General Public License as published by the Free Software Foundation;
  * either version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details. You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package com.quui.chat;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 import junit.framework.TestCase;
 
 /**
  * Tests for the mind
  * @author Fabian Steeg (fsteeg)
  */
 public class MindTest extends TestCase {
     /* Test inputs: */
     private static final String[] questions = new String[] { "hi", "ok fine",
             "jackie", "how are things", "how are you", "wie geht's?",
             "was geht?", "hallo mein lieber", "do you like opera?",
            "i like turtles", "who is god?" };
     private String wn;
     private String log;
 
     protected void setUp() throws Exception {
         super.setUp();
         Properties p = new Properties();
         try {
             p.load(new FileInputStream("config/global.properties"));
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         wn = p.getProperty("wn");
         log = p.getProperty("log");
 
     }
 
     public void testNoWnNoBase() {
         process(new Talk("config", "empty.xml", null, log), questions);
         new File("config/empty.xml").delete();
     }
 
     public void testWithWnNoBase() {
         process(new Talk("config", "empty.xml", wn, log), questions);
         new File("config/empty.xml").delete();
     }
 
     public void testNoWnBigBase() {
         process(new Talk("config", "topics-hollala.xml", null, log), questions);
     }
 
     public void testWithWnBigBase() {
         process(new Talk("config", "topics-hollala.xml", wn, log), questions);
     }
 
     private void process(Talk t, String... strings) {
         System.out
                 .println("----------------------------------------------------------------------------------------------------");
         System.out.println(String.format("Testing: %s", t));
         System.out
                 .println("----------------------------------------------------------------------------------------------------");
         for (String string : strings) {
            System.out.println(String.format("'%s' --> '%s'", string, t
                    .process(string, true)));
         }
     }
 
 }
