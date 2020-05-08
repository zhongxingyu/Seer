 import java.awt.*;
 import javax.swing.*;
 import java.lang.Math.*;
 
 //Represents an interaction skill challenge, the objective is to keep the balance node between -50 < x <= +50. Values outside
 //this range trigger an increasingly higher chance of failure. Once the node hits +-500 the challenge is lost.
 public class BalanceNode {
 	double currentForce;
 	double currentMovement;
 	double currentPosition;
 	int maxVelocity = 3;
 	int maxForce = 5;
 	int maxOffset = 100;
 	int baseFailChance = 40;
 	int easySetting = 2;
 	int hardSetting = 3;
 	
 	public BalanceNode(int startingPoint, boolean hardMode) {
 		if (hardMode) {
 			maxVelocity = hardSetting;
 		}
 		else {
 			maxVelocity = easySetting;
 		}
 		currentMovement = 0;
 		currentPosition = startingPoint;
 	}
 	
 	//provide manual impetus to the node
 	public void nudge(int movementFactor) {
 		currentForce = currentForce + movementFactor;
 	}
 	
 	public void stopMovement() {
 		currentMovement = 0;
 	}
 	
 	//cause the balance node to move under its own weight
 	public void roll() {
 		double slideFactor = getSlideFactor(currentPosition);
 		
 		currentForce = currentForce + (slideFactor/4);
 		
 		if (currentForce > maxForce) currentForce = maxForce;
 		if (currentForce < -maxForce) currentForce = -maxForce;
 
 		currentMovement = currentMovement + (currentForce/20);
 
 		if(currentMovement > maxVelocity) currentMovement = maxVelocity;
 		if(currentMovement < -maxVelocity) currentMovement = -maxVelocity;
 		
 		currentPosition = currentPosition + currentMovement;
 
 		if(currentPosition > maxOffset) currentPosition = maxOffset;
 		if(currentPosition < -maxOffset) currentPosition = -maxOffset;
 	}
 	
 	public int getFailChance() {
 		double failChance = currentPosition;
 		if ((failChance < baseFailChance) && (failChance > (-1 * baseFailChance)))
 			failChance = 0;
 		return (int)Math.round(Math.abs(failChance));
 	}
 	
 	public int getNodePosition() {
 		//System.out.printf("%4d %4d %4d\n",(int)java.lang.Math.round(currentPosition),(int)java.lang.Math.round(currentMovement),(int)java.lang.Math.round(currentForce));
 		return (int)java.lang.Math.round(currentPosition);
 	}
 	
 	//need varying degrees of slipiness to make it harder to get back to center the further out you are.
 	private double getSlideFactor(double currentPosition) {
 		double slide = currentPosition / 400;
 		if ((slide >= 0) && (slide < 2)) slide = 2;
 		if ((slide < 0) && (slide > -2)) slide = -2;
 		return slide;
 	}
 	
 	public void paint(Graphics g, int offsetX, int offsetY) {
 		g.setColor(Color.red);
 		g.drawLine((offsetX-100),(offsetY+5),(offsetX-40),(offsetY+5));
 		g.drawLine((offsetX+40),(offsetY+5),(offsetX+100),(offsetY+5));
 		g.setColor(Color.green);
		g.drawLine((offsetX-40),(offsetY+5),(offsetX+40),(offsetY+5));
 		g.setColor(Color.black);
 		g.drawString("V",(offsetX+getNodePosition()), offsetY);
 	}
 
 }
