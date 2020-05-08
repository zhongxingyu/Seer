 package com.github.vspiewak.mowitnow.mower.domain;
 
 import static com.github.vspiewak.mowitnow.mower.app.AppFactory.newMower;
 import static com.github.vspiewak.mowitnow.mower.app.AppFactory.newPosition;
 import static com.github.vspiewak.mowitnow.mower.app.AppFactory.init;
 import static org.fest.assertions.Assertions.assertThat;
 
 import com.github.vspiewak.mowitnow.mower.app.AppFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.Test;
 
 import com.github.vspiewak.mowitnow.mower.api.Engine;
 import com.github.vspiewak.mowitnow.mower.api.Vehicule;
 import com.github.vspiewak.mowitnow.mower.base.Direction;
 import com.github.vspiewak.mowitnow.mower.base.Orientation;
 import com.github.vspiewak.mowitnow.mower.base.Position;
 import com.github.vspiewak.mowitnow.mower.base.Rotation;
 
 /**
  * Tests for the <code>XEngine</code> class.
  * 
  * @author Vincent Spiewak
  * @since 1.0
  */
 public class XEngineTest {
 
    private static Logger LOG = LoggerFactory.getLogger(XEngineTest.class);
 
    @Test
    public static void given_the_specification_case_expect_specified_result() {
 
       Engine engine = XEngine.get();
       init(5, 5);
 
       Vehicule mower1 = newMower(1, 2, Orientation.NORTH);
       engine.attach(mower1);
 
       mower1.rotate(Rotation.LEFT);
       mower1.move(Direction.FORWARD);
       mower1.rotate(Rotation.LEFT);
       mower1.move(Direction.FORWARD);
       mower1.rotate(Rotation.LEFT);
       mower1.move(Direction.FORWARD);
       mower1.rotate(Rotation.LEFT);
       mower1.move(Direction.FORWARD);
       mower1.move(Direction.FORWARD);
 
       Vehicule mower2 = newMower(3, 3, Orientation.EAST);
       engine.attach(mower2);
 
       mower2.move(Direction.FORWARD);
       mower2.move(Direction.FORWARD);
       mower2.rotate(Rotation.RIGHT);
       mower2.move(Direction.FORWARD);
       mower2.move(Direction.FORWARD);
       mower2.rotate(Rotation.RIGHT);
       mower2.move(Direction.FORWARD);
       mower2.rotate(Rotation.RIGHT);
       mower2.rotate(Rotation.RIGHT);
       mower2.move(Direction.FORWARD);
 
       LOG.debug("Mower1 - {}", mower1);
       LOG.debug("Mower2 - {}", mower2);
 
       assertThat(mower1.print()).isEqualTo("1 3 N");
       assertThat(mower2.print()).isEqualTo("5 1 E");
 
    }
 
    @Test
    public static void given_a_mower_1_2_N_and_another_mower_1_3_N_move_forward_expect_no_move() {
 
       Position oldPosition = newPosition(1, 2);
       Vehicule mower1 = newMower(oldPosition, Orientation.NORTH);
       Vehicule mower2 = newMower(1, 3, Orientation.NORTH);
       AppFactory.init(5, 5, mower1, mower2);
 
       mower1.move(Direction.FORWARD);
 
       assertThat(mower1.getPosition()).isEqualTo(oldPosition);
 
    }
 
    @Test(expectedExceptions = IllegalArgumentException.class)
    public static void given_a_mower_1_2_N_add_another_at_1_2_N_expect_exception() {
 
       Position position = newPosition(1, 2);
      AppFactory.init(5,
              5,
              new Vehicule[]{newMower(position, Orientation.NORTH),
                      newMower(position, Orientation.NORTH)});
 
    }
 
 }
