 package game.stepper;
 
 import game.Command;
 import game.Ending;
 import game.State;
 import game.StaticConfig;
 import game.util.Scoring;
 
 import java.util.List;
 
 public class MultiStepper extends SingleStepper {
   public MultiStepper(StaticConfig sconfig) {
     super(sconfig);
   }
   
   public State multistep(State st, List<Command> cmds) {
     State newSt = st.clone();
 
     for (Command cmd : cmds) {
       boolean isValid = moveRobot(newSt, cmd);
 
       if (!isValid)
         break;
       newSt.steps++;
       
       if (st.ending != Ending.Abort) {
         updateBoard(newSt);
         checkEnding(newSt);
       }
      newSt.score = Scoring.totalScore(newSt.steps, newSt.collectedLambdas, newSt.ending == Ending.Abort, newSt.ending == Ending.Win);
       if (!isValid || newSt.ending != Ending.None)
         break;
     }
 
     return newSt.makeFinal();
   }
 
 }
