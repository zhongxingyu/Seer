 package jacs;
 
 import java.util.Scanner;
 
import jacs.database.exceed.controller.DatabasesController;
 import jacs.gui.Gui;
 import jacs.server.Server;
 
 /**
  * This class Main for Running Server for eXceedVote.
  * @author GoDParTicle
 <<<<<<< HEAD
  * @author nookskill
 =======
  * @author Nookskill
 >>>>>>> guitest
  */
 
 public class Main {
 	public static Scanner sc = new Scanner(System.in);
 	public static void main(String[] args) {
		//DatabasesController data = new DatabasesController();
		//data.init();
 		Gui gui = new Gui();
 		gui.run();
 		
 	}
 }
