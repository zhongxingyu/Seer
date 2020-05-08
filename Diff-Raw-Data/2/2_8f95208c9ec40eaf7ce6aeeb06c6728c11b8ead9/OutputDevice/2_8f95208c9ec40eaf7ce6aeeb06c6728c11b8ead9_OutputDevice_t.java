 package io.metacake.core.output.system;
 
 import io.metacake.core.common.window.CakeWindow;
 import io.metacake.core.output.OutputDeviceName;
 import io.metacake.core.output.RenderingInstruction;
 
 import java.util.Collection;
 
 /**
 * An OutputDevice is an interface into any kind of device that produces output. These devices receive {@link io.metacake.core.output.Renderable}s and
  * render these as output.
  * @author florence
  * @author rpless
  */
 public interface OutputDevice {
 
     /**
      * @return The unique name of this device.
      */
     public OutputDeviceName name();
 
     /**
      * Render all of the given RenderingInstructions.
      * <p>
      * If there is a separate rending thread, pass the instructions along to it here.
      * </p>
      * @param r the list of instructions to render.
      */
     public void render(Collection<RenderingInstruction> r);
 
     /**
      * when this method returns any loops needed for this device to run should be launched.
      */
     public void startOutputLoop();
 
     /**
      * Shutdown all loops and free resources. This should leave the device in a state such that
      * it can be restarted
      */
     public void shutdown();
 
     /**
      * Bind this device to the given window.
      * @param w The window for the game
      */
     public void bind(CakeWindow w);
 }
