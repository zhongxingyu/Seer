 package ch.eleveneye.hs485.device.physically;
 
 import java.io.IOException;
 
public interface IndependentConfigurableSensor extends PhysicallySensor {
 	public static enum InputMode {
 		DOWN, TOGGLE, UP
 	}
 
 	InputMode getInputMode() throws IOException;
 
 	void setInputMode(InputMode inputMode) throws IOException;
 }
