 package io.metacake.game;
 
 import io.metacake.core.input.ActionTrigger;
 import io.metacake.core.output.OutputDevice;
 import io.metacake.core.output.OutputDeviceName;
 import io.metacake.core.output.RenderingInstruction;
 import io.metacake.core.process.ActionRecognizer;
 import io.metacake.core.process.GameState;
 import io.metacake.engine.DrawInstruction;
 import io.metacake.engine.DrawingDevice;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author florence
  * @author rpless
  */
 class RunningState implements GameState {
     int x;
     int y;
     ActionRecognizer vert;
     ActionRecognizer horz;
 
     public RunningState(int x, int y, ActionRecognizer v, ActionRecognizer h) {
         this.x = x;
         this.y = y;
         this.vert = v;
         this.horz = h;
     }
 
     @Override
     public List<ActionRecognizer> getRecognizers() {
         List<ActionRecognizer> r = new LinkedList<>();
         r.add(vert);
         return r;
     }
 
     @Override
     public GameState tick() {
         if (vert.wasTriggered()) {
             y += vert.triggerWeight();
         }
         if(horz.wasTriggered()) {
             x += horz.triggerWeight();
         }
         return new RunningState(x, y, vert, horz);
     }
 
     @Override
     public boolean shouldClearActions() {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean shouldReplaceActionTriggers() {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
    public List<ActionTrigger> getNewActionTriggers() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public Map<OutputDevice, List<RenderingInstruction>> renderingInstructions(Map<OutputDeviceName, OutputDevice> outputDevices) {
         HashMap<OutputDevice, List<RenderingInstruction>> ret = new HashMap<>();
         LinkedList<RenderingInstruction> l = new LinkedList<>();
         l.add(new DrawInstruction(x,y));
         ret.put(outputDevices.get(DrawingDevice.NAME), l);
         return ret;
     }
 }
