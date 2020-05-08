 import GUI.Game.GameFrame;
 import Logik.Spiel;
 
 /**
  * Hauptklasse
  * @author Daniel Schukies, Sebastian Junger
  *
  */
public class Main {
 
 	/**
 	 * @param args Konsolen-Modus
 	 */
 	public static void main(String[] args) 
 	{
 		if(args.length > 0)
 		{
 			/**
 			 * Preufe, ob Konsolenversion genutzt werden soll.
 			 */
 			if(args[0].equals("--console"))
 			{
 				/**
 				 * Starte Konsolenversion.
 				 */
 				new Spiel(false);
 			}
 			else
 			{
 				/**
 				 * Starte GUI Spiel
 				 */
 				new GameFrame();
 			}
 		}
 		else
 		{
 			/**
 			 * Starte GUI Spiel
 			 */
 			new GameFrame();
 		}
 	}
 }
