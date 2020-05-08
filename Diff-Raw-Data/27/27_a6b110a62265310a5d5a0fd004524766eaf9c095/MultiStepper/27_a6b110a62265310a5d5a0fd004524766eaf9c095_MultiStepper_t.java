 package game.stepper;
 
 import game.Command;
 import game.Ending;
 import game.Scoring;
 import game.State;
 
import java.util.List;

 public class MultiStepper extends SingleStepper {
   public State multistep(State st, List<Command> cmds) {
     State newSt = new State(st.staticConfig, st.board.clone(), st.score, st.robotCol, st.robotRow, st.lambdasLeft, st.collectedLambdas, st.steps, st.waterLevel, st.stepsUnderwater, st.stepsUntilNextRise);
 
     for (Command cmd : cmds) {
       newSt.steps++;
       moveRobot(newSt, cmd);
       if (st.ending != Ending.Abort) {
        updateBoard(newSt);
         checkEnding(newSt);
       }
       newSt.score = Scoring.totalScore(newSt.steps, newSt.collectedLambdas, newSt.ending == Ending.Abort, newSt.ending == Ending.Win);
       if (newSt.ending != Ending.None)
         break;
     }
 
     return newSt.makeFinal();
   }
 
 }
