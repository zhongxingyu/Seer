 package gui.controller;
 
 import java.io.File;
 
 import gui.SettingsFrame;
 
 import misc.Settings;
 
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 
 /**
  * This class is responsible for controlling the view @see{SettingsFrame}
  * and uses the models @see{Settings}.
  */
 public class SettingsController {
 	/**
 	 * view for displaying saved settings
 	 */
 	private SettingsFrame frame;
 	/**
 	 * model to save settings
 	 */
 	private Settings settings;
 	
 	/**
 	 * Construct a settings controller with the specified model.
 	 * @param settings specified model
 	 */
 	public SettingsController(Settings settings) {
 		this.settings = settings;
 	}
 	
 	/**
 	 * Return the value of the timeout of the model @see{Settings}.
 	 * @return timeout
 	 */
 	public String getTimeOut() {
 		return Integer.toString(this.settings.getTimeout());
 	}
 	
 	/**
 	 * Return the value of the memory limit of the model @see{Settings}.
 	 * @return memory limit
 	 */
 	public String getMemoryLimit() {
 		return Integer.toString(this.settings.getMemoryLimit());
 	}
 	
 	/**
 	 * Returns the absolute path to the verifier.
 	 * 
 	 * @return the absolute path to the verifier
 	 */
 	public String getVerifierPath() {
 		return this.settings.getVerifierPath();
 	}
 	
 	/**
 	 * Add the specified frame as view.
 	 * @param frame specified view
 	 */
 	public void addView(SettingsFrame frame) {
 		this.frame = frame;
 	}
 	
 	/**
 	 * Return a new listener instance to save settings.
 	 * @return listener 
 	 */
 	public SaveSettings getSaveButtonListener() {
 		return new SaveSettings();
 	}
 	
 	/**
 	 * Display error message.
 	 */
 	private void invalidInput() {
 		this.frame.displayMessage(true, "Invalid input.");
 		this.frame.update();
 	}
 	
 	private class SaveSettings implements SelectionListener {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			int timeout, memorylimit;
 			try {
 				timeout = Integer.parseInt(frame.getTimeoutTextField().getText());
 				memorylimit = Integer.parseInt(frame.getMemoryLimitTextField().getText());
 			} catch(NumberFormatException nfe) {
 				invalidInput();
 				return;
 			}
 			if(timeout < 0 || memorylimit < 0) {
 				invalidInput();
 				return;
 			}
 			settings.setTimeout(timeout);
 			settings.setMemoryLimit(memorylimit);
 			String path = frame.getVerifierPathTextField().getText();
 			if(new File(path).exists()) {
 				settings.setVerifierPath(frame.getVerifierPathTextField().getText());
 			}
 			frame.displayMessage(false, "Settings saved.");
 			frame.update();
 		}
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 		}
 	}
 }
