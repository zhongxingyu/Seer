 /**
  * 
  */
 package fr.iutvalence.java.projet.test;
 
 import fr.iutvalence.java.projet.spaceinvaders.SpaceInvaders;
 
 /**
  * This class tests the main class of SpaceInvaders
  * 
  * @author Gallet Guyon
  */
 public class Test
 {
 
 	/**
 	 * Create a new SpaceInvader's object to test it.
 	 * @param args No arguments..
 	 */
 	public static void main(String[] args)
 	{
 		SpaceInvaders SI = new SpaceInvaders(250,1);
		SI.start();
 		System.out.print(SI);
 	}
 
 }
