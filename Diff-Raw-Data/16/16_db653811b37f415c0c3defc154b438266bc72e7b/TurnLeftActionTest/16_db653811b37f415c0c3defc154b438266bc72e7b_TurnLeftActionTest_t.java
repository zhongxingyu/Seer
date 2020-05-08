 package com.liqing.action;
 
 import com.liqing.bean.Rover;
 import com.liqing.bean.RoverAspect;
 import org.junit.Test;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 /**
  * User: Li Qing
  * Date: 13-3-28
  * Time: 下午11:05.
  */
 public class TurnLeftActionTest {
 
     @Test
     public void shouldTurnLeftWithAspect() throws Exception {
         Rover rover = new Rover(3,4, RoverAspect.NORTH);
         Action command = new TurnLeftAction(rover);
         command.excute();
         assertThat(rover.display(),is("3 4 W"));
     }
 }
