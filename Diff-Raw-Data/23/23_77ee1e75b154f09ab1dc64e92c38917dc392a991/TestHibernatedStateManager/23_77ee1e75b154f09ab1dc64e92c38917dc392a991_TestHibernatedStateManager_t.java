 /*
  * #%L
  * Workflow state monitor
  * %%
  * Copyright (C) 2012 The State and University Library, Denmark
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 package dk.statsbiblioteket.medieplatform.workflowstatemonitor;
 
 import junit.framework.TestCase;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 public class TestHibernatedStateManager extends TestCase {
     @Before
     public void setUp() throws Exception {
         fillTestDB();
     }
 
     @After
     public void tearDown() throws Exception {
         clearTestDB();
     }
 
     @Test
     public void testAddState() throws Exception {
         // Clean database
         clearTestDB();
 
         //Insert an element
         State state = new State();
         state.setComponent("comp");
         state.setMessage("message");
         state.setDate(new Date(1000));
         state.setStateName("stat");
         HibernatedStateManager hibernatedStateManager = new HibernatedStateManager();
         List<State> result = hibernatedStateManager.addState("test", state, null);
 
         //Check the result is as expected
         assertEquals(1, result.size());
         assertEquals("stat", result.get(0).getStateName());
         assertEquals("message", result.get(0).getMessage());
         assertEquals("comp", result.get(0).getComponent());
         assertEquals(new Date(1000), result.get(0).getDate());
 
         //Check element is inserted as expected
         List<Entity> entities = hibernatedStateManager.listEntities();
         List<State> states = hibernatedStateManager.listStates(false, null, null, null, null);
         assertEquals(1, entities.size());
         assertTrue(contains(entities, "test"));
         assertEquals(1, states.size());
         assertEquals("stat", states.get(0).getStateName());
         assertEquals("message", states.get(0).getMessage());
         assertEquals("comp", states.get(0).getComponent());
         assertEquals(new Date(1000), states.get(0).getDate());
 
 
         //Insert another element preserving the state "ha" (not found)
         state = new State();
         state.setComponent("comp");
         state.setMessage("message");
         state.setDate(new Date(2000));
         state.setStateName("stat");
         result = hibernatedStateManager.addState("test", state, Arrays.asList("ha"));
 
         //Check the result is as expected
         assertEquals(1, result.size());
         assertEquals("stat", result.get(0).getStateName());
         assertEquals("message", result.get(0).getMessage());
         assertEquals("comp", result.get(0).getComponent());
         assertEquals(new Date(2000), result.get(0).getDate());
 
         //Check element is inserted as expected
         entities = hibernatedStateManager.listEntities();
         states = hibernatedStateManager.listStates(false, null, null, null, null);
         assertEquals(1, entities.size());
         assertTrue(contains(entities, "test"));
         assertEquals(2, states.size());
         assertEquals("stat", states.get(0).getStateName());
         assertEquals("message", states.get(0).getMessage());
         assertEquals("comp", states.get(0).getComponent());
         assertEquals(new Date(2000), states.get(0).getDate());
         assertEquals("stat", states.get(1).getStateName());
         assertEquals("message", states.get(1).getMessage());
         assertEquals("comp", states.get(1).getComponent());
         assertEquals(new Date(1000), states.get(1).getDate());
 
 
         //Insert another element preserving the state "stat" (this time found!!)
         state = new State();
         state.setComponent("compp");
         state.setMessage("message1");
         state.setDate(new Date(3000));
         state.setStateName("ha");
         result = hibernatedStateManager.addState("test", state, Arrays.asList("stat"));
 
         //Check the result is as expected
         assertEquals(2, result.size());
         assertEquals("ha", result.get(0).getStateName());
         assertEquals("message1", result.get(0).getMessage());
         assertEquals("compp", result.get(0).getComponent());
         assertEquals(new Date(3000), result.get(0).getDate());
         assertEquals("stat", result.get(1).getStateName());
         assertEquals("message", result.get(1).getMessage());
         assertEquals("comp", result.get(1).getComponent());
         assertTrue(result.get(1).getDate().getTime()+1000 > System.currentTimeMillis());
         assertTrue(result.get(1).getDate().getTime() <= System.currentTimeMillis());
 
         //Check element is inserted as expected
         entities = hibernatedStateManager.listEntities();
         states = hibernatedStateManager.listStates(false, null, null, null, null);
         assertEquals(1, entities.size());
         assertTrue(contains(entities, "test"));
         assertEquals(4, states.size());
         assertEquals("stat", states.get(0).getStateName());
         assertEquals("message", states.get(0).getMessage());
         assertEquals("comp", states.get(0).getComponent());
         assertTrue(states.get(0).getDate().getTime()+1000 > System.currentTimeMillis());
         assertTrue(states.get(0).getDate().getTime() <= System.currentTimeMillis());
         assertEquals("ha", states.get(1).getStateName());
         assertEquals("message1", states.get(1).getMessage());
         assertEquals("compp", states.get(1).getComponent());
         assertEquals(new Date(3000), states.get(1).getDate());
         assertEquals("stat", states.get(2).getStateName());
         assertEquals("message", states.get(2).getMessage());
         assertEquals("comp", states.get(2).getComponent());
         assertEquals(new Date(2000), states.get(2).getDate());
         assertEquals("stat", states.get(3).getStateName());
         assertEquals("message", states.get(3).getMessage());
         assertEquals("comp", states.get(3).getComponent());
         assertEquals(new Date(1000), states.get(3).getDate());
 
        //Insert another element for a different entity preserving the state "stat" (not found!!)
        state = new State();
        state.setComponent("compp");
        state.setMessage("message1");
        state.setDate(new Date(4000));
        state.setStateName("ha");
        result = hibernatedStateManager.addState("test2", state, Arrays.asList("stat"));
 
        //Check the result is as expected
        assertEquals(1, result.size());
        assertEquals("ha", result.get(0).getStateName());
        assertEquals("message1", result.get(0).getMessage());
        assertEquals("compp", result.get(0).getComponent());
        assertEquals(new Date(4000), result.get(0).getDate());
     }
 
     @Test
     public void testListEntities() {
         // List entities
         List<Entity> list = new HibernatedStateManager().listEntities();
 
         // Check them
         assertEquals(2, list.size());
         assertTrue(contains(list, "file1"));
         assertTrue(contains(list, "file2"));
     }
 
     private boolean contains(List<Entity> entities, String name) {
         for (Entity entity : entities) {
             if (entity.getName().equals(name)) {
                 return true;
             }
         }
         return false;
     }
 
     @Test
     public void testListStates() {
         HibernatedStateManager hibernatedStateManager = new HibernatedStateManager();
 
         // List all states
         List<State> states = hibernatedStateManager.listStates(false, null, null, null, null);
 
         // Check them
         assertEquals(7, states.size());
         assertTrue(contains(states, "comp1", new Date(0), "state1", "file1"));
         assertTrue(contains(states, "comp1", new Date(1000), "state2", "file1"));
         assertTrue(contains(states, "comp2", new Date(2000), "state1", "file1"));
         assertTrue(contains(states, "comp2", new Date(3000), "state2", "file1"));
         assertTrue(contains(states, "comp1", new Date(4000), "state1", "file2"));
         assertTrue(contains(states, "comp1", new Date(5000), "state2", "file2"));
         assertTrue(contains(states, "comp2", new Date(6000), "state1", "file2"));
 
         // List states for file1
         states = hibernatedStateManager.listStates("file1", false, null, null, null, null);
 
         // Check them
         assertEquals(4, states.size());
         assertTrue(contains(states, "comp1", new Date(0), "state1", "file1"));
         assertTrue(contains(states, "comp1", new Date(1000), "state2", "file1"));
         assertTrue(contains(states, "comp2", new Date(2000), "state1", "file1"));
         assertTrue(contains(states, "comp2", new Date(3000), "state2", "file1"));
         assertFalse(contains(states, "comp1", new Date(4000), "state1", "file2"));
         assertFalse(contains(states, "comp1", new Date(5000), "state2", "file2"));
         assertFalse(contains(states, "comp2", new Date(6000), "state1", "file2"));
 
         // List all state1 states
         states = hibernatedStateManager.listStates(false, Arrays.asList(new String[]{"state1"}), null, null, null);
 
         // Check them
         assertEquals(4, states.size());
         assertTrue(contains(states, "comp1", new Date(0), "state1", "file1"));
         assertFalse(contains(states, "comp1", new Date(1000), "state2", "file1"));
         assertTrue(contains(states, "comp2", new Date(2000), "state1", "file1"));
         assertFalse(contains(states, "comp2", new Date(3000), "state2", "file1"));
         assertTrue(contains(states, "comp1", new Date(4000), "state1", "file2"));
         assertFalse(contains(states, "comp1", new Date(5000), "state2", "file2"));
         assertTrue(contains(states, "comp2", new Date(6000), "state1", "file2"));
 
         // List all NOT state2 states
         states = hibernatedStateManager.listStates(false, null, Arrays.asList(new String[]{"state2"}), null, null);
 
         // Check them
         assertEquals(4, states.size());
         assertTrue(contains(states, "comp1", new Date(0), "state1", "file1"));
         assertFalse(contains(states, "comp1", new Date(1000), "state2", "file1"));
         assertTrue(contains(states, "comp2", new Date(2000), "state1", "file1"));
         assertFalse(contains(states, "comp2", new Date(3000), "state2", "file1"));
         assertTrue(contains(states, "comp1", new Date(4000), "state1", "file2"));
         assertFalse(contains(states, "comp1", new Date(5000), "state2", "file2"));
         assertTrue(contains(states, "comp2", new Date(6000), "state1", "file2"));
 
         // List all state1 and state2 states and NOT state1 (i.e. state2)
         states = hibernatedStateManager.listStates(false, Arrays.asList(new String[]{"state1", "state2"}),
                                                              Arrays.asList(new String[]{"state1"}), null, null);
 
         //Check them
         assertEquals(3, states.size());
         assertFalse(contains(states, "comp1", new Date(0), "state1", "file1"));
         assertTrue(contains(states, "comp1", new Date(1000), "state2", "file1"));
         assertFalse(contains(states, "comp2", new Date(2000), "state1", "file1"));
         assertTrue(contains(states, "comp2", new Date(3000), "state2", "file1"));
         assertFalse(contains(states, "comp1", new Date(4000), "state1", "file2"));
         assertTrue(contains(states, "comp1", new Date(5000), "state2", "file2"));
         assertFalse(contains(states, "comp2", new Date(6000), "state1", "file2"));
 
         // List last states of all entities
         states = hibernatedStateManager.listStates(true, null, null, null, null);
 
         //Check them
         assertEquals(2, states.size());
         assertFalse(contains(states, "comp1", new Date(0), "state1", "file1"));
         assertFalse(contains(states, "comp1", new Date(1000), "state2", "file1"));
         assertFalse(contains(states, "comp2", new Date(2000), "state1", "file1"));
         assertTrue(contains(states, "comp2", new Date(3000), "state2", "file1"));
         assertFalse(contains(states, "comp1", new Date(4000), "state1", "file2"));
         assertFalse(contains(states, "comp1", new Date(5000), "state2", "file2"));
         assertTrue(contains(states, "comp2", new Date(6000), "state1", "file2"));
 
         // List last state of file1
         states = hibernatedStateManager.listStates("file1", true, null, null, null, null);
 
         //Check them
         assertEquals(1, states.size());
         assertFalse(contains(states, "comp1", new Date(0), "state1", "file1"));
         assertFalse(contains(states, "comp1", new Date(1000), "state2", "file1"));
         assertFalse(contains(states, "comp2", new Date(2000), "state1", "file1"));
         assertTrue(contains(states, "comp2", new Date(3000), "state2", "file1"));
         assertFalse(contains(states, "comp1", new Date(4000), "state1", "file2"));
         assertFalse(contains(states, "comp1", new Date(5000), "state2", "file2"));
         assertFalse(contains(states, "comp2", new Date(6000), "state1", "file2"));
 
         // List states between 999 and 2001 seconds after epoch
         states = hibernatedStateManager.listStates(false, null, null, new Date(999), new Date(2001));
 
         //Check them
         assertEquals(2, states.size());
         assertFalse(contains(states, "comp1", new Date(0), "state1", "file1"));
         assertTrue(contains(states, "comp1", new Date(1000), "state2", "file1"));
         assertTrue(contains(states, "comp2", new Date(2000), "state1", "file1"));
         assertFalse(contains(states, "comp2", new Date(3000), "state2", "file1"));
         assertFalse(contains(states, "comp1", new Date(4000), "state1", "file2"));
         assertFalse(contains(states, "comp1", new Date(5000), "state2", "file2"));
         assertFalse(contains(states, "comp2", new Date(6000), "state1", "file2"));
 
         // List states after 3001 seconds after epoch
         states = hibernatedStateManager.listStates(false, null, null, new Date(3001), null);
 
         //Check them
         assertEquals(3, states.size());
         assertFalse(contains(states, "comp1", new Date(0), "state1", "file1"));
         assertFalse(contains(states, "comp1", new Date(1000), "state2", "file1"));
         assertFalse(contains(states, "comp2", new Date(2000), "state1", "file1"));
         assertFalse(contains(states, "comp2", new Date(3000), "state2", "file1"));
         assertTrue(contains(states, "comp1", new Date(4000), "state1", "file2"));
         assertTrue(contains(states, "comp1", new Date(5000), "state2", "file2"));
         assertTrue(contains(states, "comp2", new Date(6000), "state1", "file2"));
     }
 
     private boolean contains(List<State> states, String component, Date date, String state1, String file) {
         for (State state : states) {
             if (state.getComponent().equals(component) && state.getDate().getTime() == date.getTime()
                     && state.getStateName().equals(state1) && state.getEntity().getName()
                     .equals(file)) {
                 return true;
             }
         }
         return false;
     }
 
     private void fillTestDB() {
         HibernatedStateManager hibernatedStateManager = new HibernatedStateManager();
         State state1 = new State();
         state1.setComponent("comp1");
         state1.setDate(new Date(0));
         state1.setStateName("state1");
         hibernatedStateManager.addState("file1", state1, null);
         State state2 = new State();
         state2.setComponent("comp1");
         state2.setDate(new Date(1000));
         state2.setStateName("state2");
         hibernatedStateManager.addState("file1", state2, null);
         State state3 = new State();
         state3.setComponent("comp2");
         state3.setDate(new Date(2000));
         state3.setStateName("state1");
         hibernatedStateManager.addState("file1", state3, null);
         State state4 = new State();
         state4.setComponent("comp2");
         state4.setDate(new Date(3000));
         state4.setStateName("state2");
         hibernatedStateManager.addState("file1", state4, null);
         State state5 = new State();
         state5.setComponent("comp1");
         state5.setDate(new Date(4000));
         state5.setStateName("state1");
         hibernatedStateManager.addState("file2", state5, null);
         State state6 = new State();
         state6.setComponent("comp1");
         state6.setDate(new Date(5000));
         state6.setStateName("state2");
         hibernatedStateManager.addState("file2", state6, null);
         State state7 = new State();
         state7.setComponent("comp2");
         state7.setDate(new Date(6000));
         state7.setStateName("state1");
         hibernatedStateManager.addState("file2", state7, null);
     }
 
     public void clearTestDB() throws Exception {
         Connection connection = null;
         try {
             connection = DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "SA", "");
             try {
                 Statement stmt = connection.createStatement();
                 try {
                     stmt.execute("TRUNCATE SCHEMA PUBLIC RESTART IDENTITY AND COMMIT NO CHECK");
                     connection.commit();
                 } finally {
                     stmt.close();
                 }
             } catch (SQLException e) {
                 connection.rollback();
                 throw new Exception(e);
             }
         } catch (SQLException e) {
             throw new Exception(e);
         } finally {
             if (connection != null) {
                 connection.close();
             }
         }
     }
 }
