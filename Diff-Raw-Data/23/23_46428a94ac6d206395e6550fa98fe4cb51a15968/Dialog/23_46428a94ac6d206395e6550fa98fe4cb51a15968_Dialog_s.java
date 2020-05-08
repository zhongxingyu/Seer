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
		new Dialog("\nTo loot items, just walk over them."),
 		new Dialog("\nTip:\n\n"
				+ "You can destroy some enemy attacks"),
 		new Dialog("\nTip:\n\n"
 				+ "Save your game ofter, you never know what may lie ahead!")
 	};
 }
