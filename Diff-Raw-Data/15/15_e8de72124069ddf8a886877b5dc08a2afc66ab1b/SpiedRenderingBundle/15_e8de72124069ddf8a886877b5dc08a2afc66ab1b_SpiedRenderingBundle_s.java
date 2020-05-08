 package io.metacake.core.output;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * A SpiedRenderingBundle inserts instructions into the instruction set
  * in order to determine when all instructions have been rendered.
  *
  * @author rpless
  */
 public class SpiedRenderingBundle extends RenderingInstructionBundle {
 
     /**
      *  An instruction that keeps track of when it is rendered.
      */
     private class SpyInstruction implements RenderingInstruction {
         private boolean isDone = false;
         @Override
         public void render(Object context) {
             isDone = true;
         }
     }
 
     private List<SpyInstruction> spies = new ArrayList<>();
 
     @Override
     public Map<OutputDeviceName, List<RenderingInstruction>> getInstructions() {
         Map<OutputDeviceName, List<RenderingInstruction>> instMap = super.getInstructions();
         for (List<RenderingInstruction> instructions : instMap.values()) {
             SpyInstruction spy = new SpyInstruction();
             spies.add(spy);
             instructions.add(spy);
         }
         return instMap;
     }
 
     /**
      *  Reset this bundle
      */
     public void reset() {
         spies.clear();
     }
 
     /**
      * @return Have all of the RenderingInstructions in this bundle been rendered.
      */
     public boolean isDone() {
        boolean isDone = true;
         for (SpyInstruction spy : spies) {
             isDone = isDone && spy.isDone;
         }
         return isDone;
     }
 }
