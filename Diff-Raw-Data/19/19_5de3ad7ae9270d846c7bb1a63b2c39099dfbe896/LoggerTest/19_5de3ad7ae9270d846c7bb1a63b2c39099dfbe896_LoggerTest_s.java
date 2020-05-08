 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2011 The aidGer Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.aidger.utils;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.util.logging.Level;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import de.aidger.model.Runtime;
 
 /**
  * Tests the Logger class.
  *
  * @author aidGer Team
  */
 public class LoggerTest {
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         Runtime.getInstance().initialize();
     }
 
     /**
      * Test of getInstance method, of class Logger.
      */
     @Test
     public void testGetInstance() {
         System.out.println("getInstance");
 
         Logger result = Logger.getInstance();
         assertNotNull(result);
 
         File file = new File(Runtime.getInstance().getConfigPath().concat(
                 "/aidger.log"));
         assertTrue(file.exists());
     }
 
     /**
      * Test of logMsg method, of class Logger.
      */
     @Test
     public void testLogMsg() {
         System.out.println("logMsg");
 
         File file = new File(Runtime.getInstance().getConfigPath().concat(
                 "/aidger.log"));
         long before = file.length();
         Logger.getInstance().logMsg(Level.SEVERE, "Some logging message");
 
         assertTrue(before < file.length());
     }
 
     /**
      * Test of debug method, of class Logger.
      */
     @Test
     public void testDebug() {
         System.out.println("debug");

         File file = new File(Runtime.getInstance().getConfigPath().concat(
                 "/aidger.log"));
         long before = file.length();
         Logger.debug("Debug logging message");
 
         assertTrue(before < file.length());
     }
 
     /**
      * Test of info method, of class Logger.
      */
     @Test
     public void testInfo() {
         System.out.println("info");
 
         File file = new File(Runtime.getInstance().getConfigPath().concat(
                 "/aidger.log"));
         long before = file.length();
         Logger.info("Info logging message");
 
         assertTrue(before < file.length());
     }
 
     /**
      * Test of error method, of class Logger.
      */
     @Test
     public void testError() {
         System.out.println("error");
 
         File file = new File(Runtime.getInstance().getConfigPath().concat(
                 "/aidger.log"));
         long before = file.length();
         Logger.error("Error logging message");
 
         assertTrue(before < file.length());
     }
 
 }
