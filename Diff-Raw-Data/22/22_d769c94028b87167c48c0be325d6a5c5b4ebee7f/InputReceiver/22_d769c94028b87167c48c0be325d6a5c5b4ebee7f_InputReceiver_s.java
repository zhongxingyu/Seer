 /**
  * 
  * @author Stephen Conway
  * 
  * Created for:		M5		9/30/13
  * Last modified:	M5		10/2/13		Stephen Conway
  * 
  * 
  * 
  * Purpose: Interface implemented by anything that would use
  * 			keyboard/AI/network input. Notably planned for GUIs, Auction,
  * 			LandGranter, Sprite, etc.
  */
 public interface InputReceiver {
	private boolean hasFocus;
 	
 	/**
 	 * @author Stephen Conway #M5
 	 * Called by KeyboardAdapter or other input channels when focus comes onto object
 	 */
	public void gainFocus() {
		hasFocus = true;
	}
 	
 	/**
 	 * @author Stephen Conway #M5
 	 * Called by KeyboardAdapter or other input channels when focus moves away from object
 	 */
	public void loseFocus() {
		hasFocus = false;
	}
 	
 	/**
 	 * @author #M5
 	 * Method to interpret input from keyboard, AI, or network.
 	 * @param input String representation of input. Either single 
 	 * 					char or a word for special keys (enter)
 	 */
 	public abstract void receiveInput(String input);
 }
 
