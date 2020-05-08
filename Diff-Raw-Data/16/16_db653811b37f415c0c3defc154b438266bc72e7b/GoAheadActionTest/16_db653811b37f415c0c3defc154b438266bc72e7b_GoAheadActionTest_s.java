 package com.liqing.action;
 
 import com.liqing.bean.Rover;
 import com.liqing.bean.RoverAspect;
import com.liqing.com.liqing.action.Action;
import com.liqing.com.liqing.action.GoAheadAction;
 import org.junit.Test;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 /**
  * User: Li Qing
  * Date: 13-3-28
  * Time: 下午11:01.
  */
 public class GoAheadActionTest {
 
     @Test
     public void shouldGoAheadWithAspect() throws Exception {
         Rover rover = new Rover(3,4, RoverAspect.NORTH);
         Action command = new GoAheadAction(rover);
         command.excute();
         assertThat(rover.display(),is("3 5 N"));
     }
 }
