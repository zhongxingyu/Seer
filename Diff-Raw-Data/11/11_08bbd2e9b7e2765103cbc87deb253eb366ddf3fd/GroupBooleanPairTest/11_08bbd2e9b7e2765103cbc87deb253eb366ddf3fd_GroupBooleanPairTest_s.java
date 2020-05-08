 package org.jtalks.poulpe.web.controller.users;
 
import junit.framework.Assert;
 import org.jtalks.common.model.entity.Group;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * @author Leonid Kazancev
  */
 public class GroupBooleanPairTest {
     private GroupBooleanPair pair;
 
     @BeforeMethod
     public void init(){
         pair = new GroupBooleanPair(new Group(), false);
     }
 
     @Test
     public void changeEnableTest(){
         pair.setEnable(!pair.isEnable());
         Assert.assertTrue(pair.isChanged());
     }
 
     @Test
     public void setEnableWithoutChangeTest(){
         pair.setEnable(pair.isEnable());
         Assert.assertFalse(pair.isChanged());
     }
 }
