 /**
  * Author: Andy Pacher
 * Date: May 13th, 2011
  * Assignment: Coin.java
  */
 
 public class Coin {
 	private boolean faceUp;
 	
 	public Coin() {
 		faceUp = false;
 	}
 	/**
 	 * flip t3h coin
 	 * @pre coin exists
 	 * @post coin has been flipped
 	 */
 	public void flipCoin() {
 		double randomNum = Math.random() + 0.5;
 		int random = ((int) randomNum);
 		if (random == 1)
 			faceUp = true;
 		else
 			faceUp = false;
 	}
 	/**
 	 * show face of coin
 	 * @pre coin has been flipped
 	 * @post face has been shown, hypothetically
 	 */
 	public boolean showFace() {
 		return faceUp;
 	}
 }
