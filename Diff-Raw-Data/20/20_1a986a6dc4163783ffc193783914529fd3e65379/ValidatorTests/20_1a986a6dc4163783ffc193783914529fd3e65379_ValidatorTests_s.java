 package com.github.colorlines;
 
 import com.github.colorlines.consoleplayer.TurnImpl;
 import com.github.colorlines.domain.Position;
 import com.github.colorlines.domainimpl.TurnValidatorImpl;
 import com.google.common.collect.Lists;
 import org.testng.annotations.Test;
 
 import static com.github.colorlines.ConstantArea.oneColoredBalls;
 import static com.github.colorlines.domain.Position.create;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 /**
  * @author Stanislav Kurilin
  */
 public class ValidatorTests {
     public void testCan(String comment, Position what, Position target, Position... init) {
         final TurnValidatorImpl turnValidator = new TurnValidatorImpl();
         final ConstantArea area = oneColoredBalls(Lists.asList(what, init));
         assertTrue(turnValidator.isValid(area, new TurnImpl(area.take(what), target)), comment);
     }
 
     public void testCannot(String comment, Position what, Position target, Position... init) {
         final TurnValidatorImpl turnValidator = new TurnValidatorImpl();
         final ConstantArea area = oneColoredBalls(Lists.asList(what, init));
         assertFalse(turnValidator.isValid(area, new TurnImpl(area.take(what), target)), comment);
     }
 
     @Test
     public void move_on_empty_area() {
         testCan("Single item on field", create(2, 2), create(4, 4));
     }
 
     @Test
     public void move_on_same_position() {
         testCannot("Single item on field", create(2, 2), create(2, 2));
     }
 
     @Test
     public void move_when_other_items_away() {
         testCan("Simple baricader", create(2, 2), create(4, 4), create(7, 7));
     }
 
     @Test
     public void move_throw_other_items() {
         testCan("Simple baricader", create(2, 2), create(6, 6), create(2, 1), create(2, 3), create(3, 3));
     }
 
     @Test
     public void move_when_cannot() {
         testCannot("Simple baricader", create(2, 2), create(6, 6),
                 create(2, 1), create(2, 3), create(3, 3), create(1, 3), create(0, 3), create(3, 0), create(3, 2));
     }
 
 }
