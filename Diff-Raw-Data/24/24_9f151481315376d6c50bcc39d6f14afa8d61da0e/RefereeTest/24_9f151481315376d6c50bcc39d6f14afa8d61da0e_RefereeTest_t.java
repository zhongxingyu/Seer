 package com.checkers.server.services;
 
 import com.checkers.server.exceptions.CheckersException;
 import com.checkers.server.services.referee.Referee;
 import com.checkers.server.services.referee.RussianGraphRefereeImpl;
 import com.checkers.server.services.referee.graph.FigureColor;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.internal.runners.JUnit4ClassRunner;
 import org.junit.runner.RunWith;
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  *
  *
  *
  * @author Pavel Kuchin
  */
 
 @RunWith(BlockJUnit4ClassRunner.class)
 public class RefereeTest {
 
     @Test
     public void russianGraphRefereeTest() throws CheckersException {
        /*
         Referee referee = new RussianGraphRefereeImpl();
 
         Assert.assertTrue(referee.checkStep("e3-d4", FigureColor.WHITE));
         Assert.assertTrue(referee.checkStep("d6-e5", FigureColor.BLACK));
 
         Assert.assertTrue(referee.checkStep("a3-b4", FigureColor.WHITE));
         Assert.assertTrue(referee.checkStep("h6-g5", FigureColor.BLACK));
 
         Assert.assertTrue(referee.checkStep("b4-c5", FigureColor.WHITE));
         Assert.assertTrue(referee.checkStep("g5-f4", FigureColor.BLACK));
        */
     }
 }
