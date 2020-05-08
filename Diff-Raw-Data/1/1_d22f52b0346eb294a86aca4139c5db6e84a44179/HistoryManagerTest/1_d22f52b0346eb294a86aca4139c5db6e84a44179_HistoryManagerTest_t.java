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
 
 package de.aidger.utils.history;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import de.aidger.model.Runtime;
 import java.io.File;
 import org.junit.After;
 
 /**
  * Tests the HistoryManager class.
  *
  * @author aidGer Team
  */
 public class HistoryManagerTest {
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         Runtime.getInstance().initialize();
         Runtime.getInstance().setConfigPath("./");
     }
 
     @Before
     public void setUp() {
         File historyFile = new File(Runtime.getInstance().getConfigPath() + "/history");
         if (historyFile.exists()) {
             historyFile.delete();
         }
     }
 
     @After
     public void cleanUp() {
     	(new File(Runtime.getInstance().getConfigPath() + "/history")).delete();
     }
 
     /**
      * Test of getInstance method, of class HistoryManager.
      */
     @Test
     public void testGetInstance() throws HistoryException {
         System.out.println("getInstance");
 
         assertNotNull(HistoryManager.getInstance());
     }
 
     /**
      * Test of getEvents method, of class HistoryManager.
      */
     @Test
     public void testGetEvents() throws HistoryException {
         System.out.println("getEvents");
 
         HistoryManager.getInstance().loadFromFile();
         assertTrue(HistoryManager.getInstance().getEvents().isEmpty());
 
         HistoryManager.getInstance().addEvent(new HistoryEvent());
 
         assertFalse(HistoryManager.getInstance().getEvents().isEmpty());
         assertTrue(HistoryManager.getInstance().getEvents().size() > 0);
     }
 
     /**
      * Test of addEvents method, of class HistoryManager.
      */
     @Test
     public void testAddEvent() throws HistoryException {
         System.out.println("addEvents");
 
         HistoryEvent evt = new HistoryEvent();
         evt.date = new java.sql.Date(0);
         evt.id = (long) 0;
         evt.type = "Assistant";
         evt.status = HistoryEvent.Status.Added;
 
         HistoryManager.getInstance().addEvent(evt);
 
         assertFalse(HistoryManager.getInstance().getEvents().isEmpty());
     }
 
     /**
      * Test of loadFromFile method, of class HistoryManager.
      */
     @Test
     public void testLoadFromFile() throws HistoryException {
         System.out.println("loadFromFile");
 
         testAddEvent();
 
         HistoryManager.getInstance().loadFromFile();
 
         assertFalse(HistoryManager.getInstance().getEvents().isEmpty());
     }
 
 }
