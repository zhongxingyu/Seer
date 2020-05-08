 /**
  * 
  */
 package fr.iutvalence.java.projet.test;
 
 import fr.iutvalence.java.projet.spaceinvaders.Coordinates;
 import fr.iutvalence.java.projet.spaceinvaders.SpaceInvaders;
 
 /**
  * This class tests the main class of SpaceInvaders
  * 
  * @author Gallet Guyon
  */
 public class TestSpaceInvaders
 {
 
 	/**
 	 * Create a new SpaceInvader's object to test it.
 	 * 
 	 * @param args
 	 *            No arguments..
 	 */
 	public static void main(String[] args)
 	{
 		// TODO More Test
		SpaceInvaders SI = new SpaceInvaders(4,1,new Coordinates(50,50));
 		
 		System.out.println("Etat Initial :" + SI);
 		
 		SI.start();
 
 		System.out.println("Etat Final :" + SI);
 	}
 
 }
