 package se.chalmers.kangaroo.main;
 

import java.io.IOException;

 import se.chalmers.kangaroo.controller.GameController;
import se.chalmers.kangaroo.io.Highscore;
 import se.chalmers.kangaroo.model.GameModel;
 import se.chalmers.kangaroo.view.ChangeView;
 
 /**
  * 
  * @author simonal (?)
  * @modifiedby alburgh
  * @modifiedby arvidk
  * 
  */
 public class Main {
 
 	/**
 	 * Starts the game
 	 * @param args
 	 */
 	public static void main(String[] args) {

	new GameController(new ChangeView(new GameModel()));
	Highscore nh = Highscore.getInstance();
	try {
		nh.setHighscore("hej", 0, 42);
	} catch (IOException e) {
		System.out.println("hej");
		e.printStackTrace();
	}


 	}
 
 }
