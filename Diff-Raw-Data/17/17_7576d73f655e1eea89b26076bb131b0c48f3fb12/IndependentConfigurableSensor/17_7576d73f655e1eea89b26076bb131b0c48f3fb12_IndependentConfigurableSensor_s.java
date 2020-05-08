 package ch.eleveneye.hs485.device.physically;
 
 import java.io.IOException;
 
public interface IndependentConfigurableSensor {
 	public static enum InputMode {
 		DOWN, TOGGLE, UP
 	}
 
 	InputMode getInputMode() throws IOException;
 
 	void setInputMode(InputMode inputMode) throws IOException;
 }
