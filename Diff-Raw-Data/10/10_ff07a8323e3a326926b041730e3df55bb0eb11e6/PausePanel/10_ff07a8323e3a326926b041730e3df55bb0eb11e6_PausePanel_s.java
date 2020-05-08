 package com.cannon.basegame;
 
 import de.matthiasmann.twl.Button;
 import de.matthiasmann.twl.Widget;
 
 public class PausePanel extends Widget {
 
 	private PauseState pauseState;
 	private Button resumeButton;
 	private Button optionsButton;
 	private Button quitButton;
 	private final short SPACES = 15;
 	
 	private String addSpaces(String string){ //makes all text roughly the same length, is there a better way to do this?
 			if(string.length() < SPACES){
 				return addSpaces(" " + string + " ");
 			} else {
 				return string;
 			}
 		}
 	
 	public PausePanel(PauseState pauseState) {
 	
 		this.pauseState = pauseState;
 		resumeButton = new Button();
 		resumeButton.setText(addSpaces("Resume"));
 		resumeButton.addCallback(new Runnable(){
 
 			@Override
 			public void run() {
 				getPauseState().unpause();
 				
 			}
 			
 		});
 		add(resumeButton);
 		optionsButton = new Button();
 		optionsButton.setText(addSpaces("Options"));
 		optionsButton.addCallback(new Runnable(){
 
 			@Override
 			public void run() {
 				//we need options
 			}
 			
 		});
 		add(optionsButton);
 		quitButton = new Button();
		quitButton.setText(addSpaces("Quit"));
 		quitButton.addCallback(new Runnable(){
 
 			@Override
 			public void run() {
 				getPauseState().exit();
 			}
 			
 		});
 		add(quitButton);
 	}
 	
 	protected void layout(){
 		resumeButton.adjustSize();
 		optionsButton.adjustSize();
 		quitButton.adjustSize();
 		resumeButton.setPosition(getX() + getWidth() / 2 - resumeButton.getWidth() / 2,
 				getY() + getHeight() * 2/5 - resumeButton.getWidth() / 2);
 		optionsButton.setPosition(getX() + getWidth() / 2 - optionsButton.getWidth() / 2,
 				getY() + getHeight() * 3/5 - optionsButton.getWidth() / 2);
 		quitButton.setPosition(getX() + getWidth() / 2 - quitButton.getWidth() / 2,
 				getY() + getHeight() * 4/5 - quitButton.getWidth() / 2);
 	}
 	
 	public PauseState getPauseState(){
 		return pauseState;
 	}
 	
 
 }
