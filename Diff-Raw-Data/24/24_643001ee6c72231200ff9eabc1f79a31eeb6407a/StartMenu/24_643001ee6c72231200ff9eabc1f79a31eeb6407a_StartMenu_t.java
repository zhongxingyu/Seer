 package edu.id.meteordodge;
 
 import java.awt.Color;
 import java.awt.Font;
 
 public class StartMenu {
 	public static void printMenu(){
 		Font font = new Font("Sans Serif", Font.BOLD, 55);
 		StdDraw.setCanvasSize(1600, 900);
 		StdDraw.clear(Color.BLACK);
 		StdDraw.setXscale(0,1600);
 		StdDraw.setYscale(0,900);
 		StdDraw.setFont(font);
 		StdDraw.picture(800, 450, "PCHOOOOO Start Screen.png", 1760, 990);
 		StdDraw.picture(1200, 500, "Start Button.png", 500, 150);
 		StdDraw.picture(1150, 310, "Instructions Button.png", 380, 200);
 		StdDraw.show(1);
 	}
 	public static int clickCheck(){
 		if (StdDraw.mousePressed() == true && StdDraw.mouseX()<1450 && StdDraw.mouseX()>950 && StdDraw.mouseY()<575 && StdDraw.mouseY()>425){
			StdDraw.mousePressed = false;
 			return 1;
 		}
 		if (StdDraw.mousePressed() == true && StdDraw.mouseX()<1340 && StdDraw.mouseX()>960 && StdDraw.mouseY()>210 && StdDraw.mouseY()<410){
			StdDraw.mousePressed = false;
 			return 2;
 		}
 		return 0;
 	}
 }
