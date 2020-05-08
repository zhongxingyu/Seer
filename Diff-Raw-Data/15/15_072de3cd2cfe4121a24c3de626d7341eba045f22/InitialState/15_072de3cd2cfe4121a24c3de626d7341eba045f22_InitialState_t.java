 package io.metacake.game;
 
 import io.metacake.core.common.MilliTimer;
 import io.metacake.core.input.ActionTrigger;
 import io.metacake.core.output.OutputDevice;
 import io.metacake.core.output.OutputDeviceName;
 import io.metacake.core.output.RenderingInstruction;
 import io.metacake.core.process.ActionRecognizer;
 import io.metacake.core.process.GameState;
 import io.metacake.engine.*;
 import sun.security.krb5.internal.crypto.KeyUsage;
 
 import java.awt.event.KeyEvent;
 import java.util.*;
 
 /**
  * @author florence
  * @author rpless
  */
 public class InitialState implements GameState {
     int x;
     int y;
     KeyboardRecognizer vert = new MovementRecognizer(KeyEvent.VK_DOWN,KeyEvent.VK_UP);
     KeyboardRecognizer horz = new MovementRecognizer(KeyEvent.VK_RIGHT,KeyEvent.VK_LEFT);
 
     public InitialState(int x, int y) {
         this.x = x;
         this.y = y;
     }
 
     @Override
     public List<ActionRecognizer> getRecognizers() {
         List<ActionRecognizer> r = new LinkedList<>();
         r.add(vert);
         return r;
     }
 
     @Override
     public GameState tick() {
         return new RunningState(x, y, vert,horz);
     }
 
     @Override
     public boolean shouldClearActions() {
         return true;
     }
 
     @Override
     public boolean shouldReplaceActionTriggers() {
         return true;
     }
 
     @Override
    public List<ActionTrigger> replaceActionTriggers() {
         List<ActionTrigger> r = new LinkedList<>();
         KeyboardTrigger t = new KeyboardTrigger(KeyEvent.VK_DOWN,KeyEvent.VK_UP);
         t.bindRecognizer(vert);
         KeyboardTrigger t2 = new KeyboardTrigger(KeyEvent.VK_RIGHT,KeyEvent.VK_LEFT);
         t2.bindRecognizer(horz);
         r.add(t);
         r.add(t2);
         return r;
     }
 
     @Override
     // Concern: List<RenderingInstruction> could be its own class
     public Map<OutputDevice, List<RenderingInstruction>> renderingInstructions(Map<OutputDeviceName, OutputDevice> outputDevices) {
         return Collections.emptyMap();
     }
 }
 
 
