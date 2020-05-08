 package org.dlw.ai.blackboard;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import org.dlw.ai.blackboard.knowledge.KnowledgeSourcesImpl;
 import org.dlw.ai.blackboard.util.UniversalContext;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * 
  * @author <a href="mailto:dlwhitehurst@gmail.com">David L. Whitehurst</a>
  * @version 1.0.0
  */
 public class BrainTest {
 
     private Brain brain;
 
     @Before
     public void setUp() throws Exception {
        brain = BlackboardContext.getInstance().getBrain();
 
     }
 
     @Test
     public void engageTest() throws AssertionError {
         brain.engage(); // load KnowledgeSources
         assertNotNull(brain.getKnowledgeSources());
 
         KnowledgeSourcesImpl kss = (KnowledgeSourcesImpl) brain
                 .getKnowledgeSources();
         int count = kss.size();
 
         assertTrue(count == 13);
     }
 }
