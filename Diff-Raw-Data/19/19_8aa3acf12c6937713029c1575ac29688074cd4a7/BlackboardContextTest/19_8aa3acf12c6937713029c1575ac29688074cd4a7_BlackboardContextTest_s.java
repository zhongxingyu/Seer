 package org.dlw.ai.blackboard;
 
import static org.junit.Assert.*;
 
import java.util.concurrent.ConcurrentLinkedQueue;

import org.dlw.ai.blackboard.domain.Assumption;
import org.dlw.ai.blackboard.util.UniversalContext;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
import org.dlw.ai.blackboard.BlackboardContext;
import org.dlw.ai.blackboard.Blackboard;
 
 /**
  * @author dlwhitehurst
  * 
  */
 public class BlackboardContextTest {
 
     private Blackboard blackboard;
     private Controller controller;
    private BlackboardContext blackboardContext;
    private ConcurrentLinkedQueue<Assumption> pastAssumptions;
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
 
         blackboard = BlackboardContext.getInstance().getBlackboard();

         controller = BlackboardContext.getInstance().getController();
     }
 
     @Test
     public void testGetBlackboard() throws AssertionError {
        blackboard = blackboardContext.getBlackboard();
         assertNotNull(blackboard);
     }
 
 
     @Test
     public void testGetController() throws AssertionError {
        controller = blackboardContext.getController();
         assertNotNull(controller);
     }
 
     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown() throws Exception {
     }
 
 }
