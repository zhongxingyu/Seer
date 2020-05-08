 package org.triple_brain.module.model.graph;
 
 import org.junit.Test;
import org.triple_brain.module.model.FreebaseFriendlyResource;
 import org.triple_brain.module.model.FriendlyResource;
 
 import static org.junit.Assert.assertTrue;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class FreebaseExternalFriendlyResourceTest extends AdaptableGraphComponentTest{
 
     @Test
     public void can_get_images(){
         FriendlyResource timBernersLee = modelTestScenarios.timBernersLeeInFreebase();
        assertTrue(FreebaseFriendlyResource.isFromFreebase(timBernersLee));
     }
 
 }
