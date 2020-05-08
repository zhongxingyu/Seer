 package edu.id.meteordodge;
 
 import java.awt.Color;
 import java.awt.Font;
 
 public class InstructionsMenu {
 	public static void printMenu(){
 		Font font = new Font("Sans Serif", Font.BOLD, 55);
 		Font font2 = new Font("SansSerif", Font.PLAIN, 45);
 		StdDraw.setCanvasSize(1600, 900);
 		StdDraw.clear(Color.BLACK);
 		StdDraw.setXscale(0,1600);
 		StdDraw.setYscale(0,900);
 		StdDraw.setFont(font);
 		StdDraw.text(800, 650, "Instructions");
 		StdDraw.picture(1550, 50, "Back Button.png", 202, 150);
 		StdDraw.setFont(font2);
 		StdDraw.textLeft(0, 520, "Use the left and right arrow keys to move.");
 		StdDraw.textLeft(0, 445, "When the God Tier charge meter fills, press 8 to go GOD TIER!");
 		StdDraw.textLeft(0, 370, "When in God Tier, press space to fire wind bullets.");
 		StdDraw.textLeft(0, 295, "Note: You have a limited number of bullets.");
 		StdDraw.show(1);
 	}
 	public static int clickCheck(){
 		if (StdDraw.mousePressed() == true && StdDraw.mouseX()<1651 && StdDraw.mouseX()>1449 && StdDraw.mouseY()>-25 && StdDraw.mouseY()< 125){
 			return 3;
 		}
 		return 2;
 	}
 }
