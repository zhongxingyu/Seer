 package jivko.brain.state;
 
 import jivko.brain.Brain;
 import jivko.brain.improvisation.JokeCreator;
 import jivko.brain.speech.sketch.Sketch;
 import org.customsoft.stateless4j.StateMachine;
 import org.customsoft.stateless4j.delegates.Action;
 
 /**
  *
  * @author Sergii Smehov (smehov.com)
  */
 public class StateMachineHolder {
 
   private String actionResult = "";
 
   public enum State {
     INITIAL, DIALOG, PAUSED, SKETCH_CHOICE, SKETCHING, JOKE_CHOICE, JOKING, IMPROVISE_CHOICE, IMPROVISE
   }
   
   Action resetAction = new Action() {
     @Override
     public void doIt() {
       Brain.getInstance().reset();
 
       try {
         stateMachine.Fire(Command.DIALOG);
       } catch (Exception e) {
         e.printStackTrace();
       }
     }
   };
   
   Action sketchChooseAction = new Action() {
     @Override
     public void doIt() {
 
       if (Brain.getInstance().getSketchesManager().getActiveSketch() == null) {
         actionResult = "Какую миниатюру?";        
       }
      
       setNextCommand(Command.START);
     }
   };
   
   Action sketchTellAction = new Action() {
     @Override
     public void doIt() {            
       actionResult = "";
       
       setNextCommand(Command.SKETCH);
               
       if (Brain.getInstance().getSketchesManager().getActiveSketch() == null) {
         
         Sketch sketch = Brain.getInstance().getSketchesManager().findSketch(
                 Brain.getInstance().getQuestion());
         
         if (sketch == null) {
           actionResult = "повтори еще раз, какую миниатюру?";
           return;
         }
                 
         Brain.getInstance().getSketchesManager().setActiveSketch(sketch);
 
         if (sketch.doesHumanStart()) {
           actionResult = "ок! начинай";
           return;
         } else {
           actionResult = "окей. погнали.  ";
         }
       }
 
      String answer = null;
      try {
        Brain.getInstance().getSketchesManager().findAnswer(
               Brain.getInstance().getQuestion());
      } catch (Exception e) {
        e.printStackTrace();
      }
 
       if (answer == null) {
         actionResult = "спасибо за внимание";       
         setNextCommand(Command.RESET);        
       } else {
         actionResult += answer;
       }      
     }    
   };
   
   Action jokeChooseAction = new Action() {
     @Override
     public void doIt() {     
       
       if (Brain.getInstance().getJokesManager().getActiveJoke() == null) {
         actionResult = "Шутку про что?";        
       }
 
       try {
         setNextCommand(Command.START);
       } catch (Exception e) {
         e.printStackTrace();
       }
     }
   };
   
   Action jokeTellAction = new Action() {
     @Override
     public void doIt() {
       try {
         actionResult = Brain.getInstance().getJokesManager().findAnswer(
               Brain.getInstance().getQuestion());
       } catch (Exception e) {
         e.printStackTrace();
       }
 
       if (actionResult == null) {
         actionResult = "повторите, про что шутку?";
       } else {
         actionResult = "слушайте. " + actionResult;        
         setNextCommand(Command.RESET);
       }
     }
   };     
   
   Action improviseTellAction = new Action() {
     @Override
     public void doIt() {
       
       try {
         actionResult = JokeCreator.getInstance().createJokeWHoWhere(Brain.getInstance().getQuestion());
       } catch (Exception e) {
         e.printStackTrace();
       }
 
       if (actionResult == null) {
         actionResult = "повторите, про что шутку?";
       } else {
         actionResult = "слушайте. " + actionResult;        
         setNextCommand(Command.RESET);
       }
     }
   };
       
   Action dialogTellAction = new Action() {
     @Override
    public void doIt() {      
        try {
          actionResult = Brain.getInstance().getUtterancesManager().findAnswer(
                Brain.getInstance().getQuestion());     
        } catch (Exception e) {
          e.printStackTrace();
        }
       }
   };
   
   StateMachine<State, Command> stateMachine = new StateMachine<>(State.DIALOG);
   {
     try {
       stateMachine.Configure(State.INITIAL)
               .Permit(Command.DIALOG, State.DIALOG)
               .OnEntry(resetAction);
 
       stateMachine.Configure(State.DIALOG)
               .PermitReentry(Command.DIALOG)
               .Permit(Command.RESET, State.INITIAL)
               .Permit(Command.PAUSE, State.PAUSED)
               .Permit(Command.SKETCH, State.SKETCH_CHOICE)
               .Permit(Command.JOKE, State.JOKE_CHOICE)
               .Permit(Command.IMPROVISE, State.IMPROVISE_CHOICE)
               .OnEntry(dialogTellAction);
 
       stateMachine.Configure(State.SKETCH_CHOICE)
               .PermitReentry(Command.SKETCH)
               .Permit(Command.RESET, State.INITIAL)
               .Permit(Command.START, State.SKETCHING)
               .OnEntry(sketchChooseAction);
 
       stateMachine.Configure(State.SKETCHING)
               .PermitReentry(Command.SKETCH)
               .Permit(Command.RESET, State.INITIAL)
               .Permit(Command.PAUSE, State.PAUSED)
               .OnEntry(sketchTellAction);
 
       stateMachine.Configure(State.JOKE_CHOICE)
               .Permit(Command.RESET, State.INITIAL)
               .Permit(Command.START, State.JOKING)
               .OnEntry(jokeChooseAction);
 
       stateMachine.Configure(State.JOKING)
               .PermitReentry(Command.START)
               .Permit(Command.RESET, State.INITIAL)
               .Permit(Command.PAUSE, State.PAUSED)
               .OnEntry(jokeTellAction);
       
       stateMachine.Configure(State.IMPROVISE_CHOICE)
               .Permit(Command.RESET, State.INITIAL)
               .Permit(Command.START, State.IMPROVISE)
               .OnEntry(jokeChooseAction);
 
       stateMachine.Configure(State.IMPROVISE)
               .PermitReentry(Command.START)
               .Permit(Command.RESET, State.INITIAL)
               .Permit(Command.PAUSE, State.PAUSED)
               .OnEntry(improviseTellAction);
 
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
   private State prevState = State.INITIAL;
 
   private Command nextCommand = Command.DIALOG;
 
   public void setNextCommand(Command nextCommand) {
     this.nextCommand = nextCommand;
   }  
 
   public Command getNextCommand() {
     return nextCommand;
   }
         
   public String execute(Command command) {
 
     if (command != Command.UNKNOWN_COMMAND) {
       setNextCommand(command);
     }      
     
     try {
       State curState = stateMachine.getState();
       stateMachine.Fire(getNextCommand());
       prevState = curState;
     } catch (Exception e) {
       e.printStackTrace();
     }
 
     return actionResult;
   }
 
   public State getState() {
     return stateMachine.getState();
   }
 
   public State getPrevState() {
     return prevState;
   }
 
   public void setPrevState() {
   }
 }
