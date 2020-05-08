 package test;
 
 import lejos.nxt.LCD;
 
 public class DisplayTest {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new DisplayTest();
 	}
 	
 	public DisplayTest() {
 		LCD.drawString("Display-Test", 0, 0);
 		try {
 			Thread.sleep(10000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
