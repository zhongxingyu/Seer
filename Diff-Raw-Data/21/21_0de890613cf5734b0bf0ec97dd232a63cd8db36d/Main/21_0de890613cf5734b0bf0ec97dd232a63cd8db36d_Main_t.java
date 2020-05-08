 package se.chalmers.kangaroo.main;
 
 
 import java.io.IOException;
 
 //import org.apache.log4j.BasicConfigurator;
 import org.keyczar.exceptions.KeyczarException;
 
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
 	 * @throws KeyczarException 
 	 */
 	public static void main(String[] args) throws KeyczarException {
 
 	new GameController(new ChangeView(new GameModel()));
 
 	}
 
 }
