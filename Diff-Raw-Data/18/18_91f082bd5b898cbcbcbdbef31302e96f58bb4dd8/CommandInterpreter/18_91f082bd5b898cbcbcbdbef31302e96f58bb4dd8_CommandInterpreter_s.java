 package courseProject.controller;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 
 import courseProject.model.Game;
 import courseProject.model.ModelListener;
 import courseProject.view.View;
 import courseProject.view.textD.ViewText;
 import courseProject.view.twoD.View2D;
 
 /**
  * This class is part of the "World of Zuul" application. 
  * "World of Zuul" is a very simple, text based adventure game.  
  * 
  * This parser reads user input and tries to interpret it as an "Adventure"
  * command. Every time it is called it reads a line from the terminal and
  * tries to interpret the line as a two word command. It returns the command
  * as an object of class Command.
  *
  * The parser has a set of known command words. It checks user input against
  * the known commands, and if the input is not one of the known commands, it
  * returns a command object that is marked as an unknown command.
  * 
  * @author Michael Hamon
  * @author Matthew Smith
  * @author Denis Dionne
  * @version 04/11/2012
  */
 public class CommandInterpreter implements InputListener
 {
     private Game game;
     private View view;
     private boolean finished;
     private double previousTime;
     
     /**
      * Create a parser to read from the terminal window.
      * @param view what version of the view using
      * @param game the game you are playing
      */
     public CommandInterpreter(View view, Game game) {
         this.view = view;
         this.game = game;
         view.addInputListener(this);
         previousTime = System.nanoTime();
     }
     
     /**
      * receives an input from the view and acts accordingly
      * @param e is the event coming from the view (whether it be a textview, 2Dview or 3Dview)
      */
     public void input(InputEvent e){
     	//need to implement this
     	if(e.getClass().equals(InputEvent2D.class)){
     		if(e.getCommand() == null){
     			((View2D) view).moveCharacter((InputEvent2D)e);
     			game.addUndo();
     			return;
     		}
     	}
     	
     	finished = game.processCommand(e.getCommand());
     	
     	
     }
 
     /**
      * static method returns all the possible commands the parser can handle.
      * @return a string containing each of the possible commands
      */
     public static String getPossibleCommands(){
         return CommandWord.getPossibleCommands();
     }
     
     
     /**
      * The start of the program
      * has user select the type of view (text/2d/3d)
      * @param args
      */
     public static void main(String[] args){
     	
     	Object[] options = {"Text-Based",
                 "2D Game",
                 "3D Game"};
     	
     	Object viewOption = JOptionPane.showInputDialog(null,
 				"Welcome to the World of Nameless\nWhich version of the game would you like to play? ",
 				"Game Mode Selection",
 				JOptionPane.QUESTION_MESSAGE,
 				null,
 				options,
 				options[1]);
     	
     	
     	if(viewOption!=null) {
         	View view ;
 	    	
 	    	if(viewOption.equals(options[0])) { // Text-Based Option
 	    		view =  new ViewText();
 	    	}
 	    	else if (viewOption.equals(options[1])) { // 2D option
 	    		view = new View2D();
 	    	}
 	    	else {
 	    		view = new ViewText();
 	    		view.displayMessage("No such view exists yet, switching to text view");
 	    	}
 	    	
 	    	Game game = new Game();
 	    	CommandInterpreter c = new CommandInterpreter(view, game);
 	
 	    	game.addModelListeners((ModelListener)view);
 	    	c.play();
 	    	view.dispose();
     	}
     }
 
     
     /**
      * this is the loop used to play the game
      */
     private void play(){
     	game.printWelcome();
     	finished = false;
         while (! finished) {
         	final double delta = System.nanoTime()-previousTime;
        	SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
	        	    view.update(delta);
				}
        	});
         	previousTime = System.nanoTime();
             if(game.getPlayer().isDead()){
             	JOptionPane.showMessageDialog(null, "You died,\nThanks for playing", "You Died", JOptionPane.WARNING_MESSAGE);
             	view.dispose();
             	break;
             }
             try {
 				Thread.sleep(1);
 			} catch (InterruptedException e) {
 			}
         }
     }
 }
