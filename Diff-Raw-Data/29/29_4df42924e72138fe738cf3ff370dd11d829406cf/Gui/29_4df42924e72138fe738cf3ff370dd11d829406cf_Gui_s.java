 package test;
 
 import java.awt.Component;
 
 import javax.swing.Timer;
 
 import backend.*;
 import frontend.*;
 
 public class Gui {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		MineFrame mF = new MineFrame();
		mF.setDifficulty(1);
 		
		for( Component c : mF.getField().getComponents())
			if(!((MineSpot)c).getSpot().isBomb())
				((MineSpot)c).getSpot().open();
 		
 		System.out.println(mF.getGame().testString());
 	}
 
 }
