 ﻿package Untouchables;
 
 import java.io.IOException;
 import StarTrek.Game;
 import StarTrek.Klingon;
 
 /**
  * 
  * Note: SampleClient is UNTOUCHABLE! It represents one of hundreds of Game
  * clients, and should not have to change.
  * 
  */
 public class SampleClient {
 
 	public static void main(String args[]) {
 		System.out.println("Simple Star Trek");
 		WebGadget wg = new WebGadget("phaser", "1000", new Klingon());
 		Game game = new Game();
 		game.fireWeapon(wg);
 		waitForUserToEndGame();
 	}
 
 	private static void waitForUserToEndGame() {
 		try {
 			System.in.read();
 		} catch (IOException e) {
 		}
 	}
}
