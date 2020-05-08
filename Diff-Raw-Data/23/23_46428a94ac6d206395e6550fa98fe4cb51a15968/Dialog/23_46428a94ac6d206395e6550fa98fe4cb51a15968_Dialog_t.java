 package com.soc.game.map;
 
 public class Dialog {
 	public String text;
 	public boolean popped;
 	
 	private Dialog(String text){
 		this.text = text;
 		popped = false;
 	}
 	
 	static Dialog[] dialogs = new Dialog[]{
 		new Dialog("Press H to open the instructions."),
		new Dialog("\nBe careful, quicksaving a game (F5) will override the previous quick save."),
 		new Dialog("\nTip:\n\n"
				+ "You can destroy some enemy attacks, attacking them, or shielding yourself (SHIFT LEFT)."),
 		new Dialog("\nTip:\n\n"
 				+ "Save your game ofter, you never know what may lie ahead!")
 	};
 }
