 package jk_5.nailed.map.instruction.instructions;
 
 import jk_5.nailed.map.instruction.GameController;
 import jk_5.nailed.map.instruction.IInstruction;
 import jk_5.nailed.map.instruction.TimedInstruction;
 import jk_5.nailed.util.Utils;
 import lombok.AllArgsConstructor;
 import lombok.NoArgsConstructor;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 @NoArgsConstructor
 @AllArgsConstructor
 public class InstructionCountUp extends TimedInstruction {
 
     private String message;
 
     @Override
     public boolean executeTimed(GameController controller, int ticks) {
         controller.broadcastTimeRemaining(Utils.secondsToShortTimeString(ticks));
        return false;
     }
 
     @Override
     public void injectArguments(String args) {
         this.message = args;
     }
 
     @Override
     public IInstruction cloneInstruction() {
         return new InstructionCountUp(this.message);
     }
 }
