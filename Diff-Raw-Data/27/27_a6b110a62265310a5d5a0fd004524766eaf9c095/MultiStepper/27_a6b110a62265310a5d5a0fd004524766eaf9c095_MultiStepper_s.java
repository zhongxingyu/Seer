 package game.stepper;
 
import java.util.List;

import game.Board;
 import game.Command;
 import game.Ending;
 import game.Scoring;
 import game.State;
 
 public class MultiStepper extends SingleStepper {
   public State multistep(State st, List<Command> cmds) {
     State newSt = new State(st.staticConfig, st.board.clone(), st.score, st.robotCol, st.robotRow, st.lambdasLeft, st.collectedLambdas, st.steps, st.waterLevel, st.stepsUnderwater, st.stepsUntilNextRise);
 
     for (Command cmd : cmds) {
       newSt.steps++;
      Board board = newSt.board.clone();
       moveRobot(newSt, cmd);
       if (st.ending != Ending.Abort) {
        updateBoard(board, newSt);
         checkEnding(newSt);
       }
       newSt.score = Scoring.totalScore(newSt.steps, newSt.collectedLambdas, newSt.ending == Ending.Abort, newSt.ending == Ending.Win);
       if (newSt.ending != Ending.None)
         break;
     }
 
     return newSt.makeFinal();
   }
 
 }
