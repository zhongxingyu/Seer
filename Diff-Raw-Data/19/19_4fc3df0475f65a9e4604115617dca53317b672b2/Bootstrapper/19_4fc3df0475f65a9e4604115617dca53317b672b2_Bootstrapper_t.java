 package io.metacake.core;
 
 import io.metacake.core.common.window.CakeWindow;
import io.metacake.core.input.InputDeviceName;
 import io.metacake.core.input.InputSystem;
 import io.metacake.core.input.system.InputDevice;
 import io.metacake.core.input.system.InputLayer;
 import io.metacake.core.output.OutputDeviceName;
 import io.metacake.core.output.OutputSystem;
import io.metacake.core.output.system.OutputDevice;
 import io.metacake.core.output.system.OutputLayer;
 import io.metacake.core.process.GameRunner;
 import io.metacake.core.process.GameState;
 
 import java.util.Map;
 
 /**
  * This class handles bootstrapping and launching the game.
  * <p>
  * The Bootstrapper sets up all of the necessary pieces of the game and does any binding that is necessary.
  *
  * @author florence
  * @author rpless
  */
 public class Bootstrapper {
     public static final long DEFAULT_LOOP_MILLIS = 50;
     private CakeWindow window;
    private Map<InputDeviceName,InputDevice> inputs;
     private Map<OutputDeviceName, OutputDevice> outputs;
     private GameState initialState;
     private long loopTime;
 
    public Bootstrapper(CakeWindow window, Map<InputDeviceName,InputDevice> inputs, Map<OutputDeviceName, OutputDevice> outputs,
                         GameState g) {
         this(window, inputs, outputs, g, DEFAULT_LOOP_MILLIS);
     }
 
    public Bootstrapper(CakeWindow window, Map<InputDeviceName,InputDevice> inputs, Map<OutputDeviceName, OutputDevice> outputs,
                         GameState g, long loopTime) {
         this.window = window;
         this.inputs = inputs;
         this.outputs = outputs;
         this.initialState = g;
         this.loopTime = loopTime;
     }
 
     /**
      * Invokes the the binding phases of a game creation and then launches the game.
      */
     public void setupAndLaunchGame() {
         this.bootstrapUserObjects();
         InputSystem i = this.bootstrapInputSystem();
         OutputSystem o = this.bootstrapOutputSystem();
         GameRunner r = this.bootstrapProcessLayer(i, o);
         o.startOutputLoops();
         r.mainLoop(initialState, loopTime);
     }
 
     /**
      * Invokes binding operations for all InputDevices and InputDevices
      */
     private void bootstrapUserObjects() {
        for (InputDevice i : inputs.values()) {
             i.bind(window);
         }
         for (OutputDevice o : outputs.values()) {
             o.bind(window);
         }
     }
 
     /**
      * @return Returns an InputSystem that has been set up and bound.
      */
     private InputSystem bootstrapInputSystem() {
         return new InputLayer(inputs);
     }
 
     /**
      * @return Returns an OutputSystem that has been set up and bound.
      */
     private OutputSystem bootstrapOutputSystem() {
         return new OutputLayer(outputs);
     }
 
     /**
      * @param i the InputSystem for the game
      * @param o the OutputSystem for the game
      * @return a GameRunner that has been bound to the Input and Output Systems and is ready to be launched
      */
     private GameRunner bootstrapProcessLayer(InputSystem i, OutputSystem o) {
         return new GameRunner(i, o);
     }
 }
