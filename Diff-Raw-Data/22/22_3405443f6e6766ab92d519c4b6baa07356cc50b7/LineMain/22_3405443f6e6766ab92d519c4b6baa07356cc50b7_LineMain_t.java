 package de.fhh.pr2.common.u.a2;
 
 import java.awt.Graphics;
 
 import de.fhh.pr2.common.u.a1.Point;
 import de.fhh.pr2.common.DrawingPanel;
 
 public class LineMain {
 	private static DrawingPanel panel;
 	private static Graphics pen;
 	
 	public static void main(String[] args) {
 		// Aufgabe 2
 		printLine();
 		
 		
 		// Aufgabe 3
 		enablePanel(200, 200);
 		drawLines();
 		
 		// Aufgabe 4 Beispiel 1/2
 		enablePanel(200, 200);
 		drawDashedLine();
 		drawSecondDashedLine();
 		
 		// Aufgabe 4 Beispiel 3
 		enablePanel(100, 100);
 		drawSun();
 		
 		// Aufgabe 6
 		enablePanel(200, 200);
 		drawBigFatLines();
 		
 	}
 	
 	public static void printLine() {
 		Line lineOne = new Line(new Point(1, 2), new Point(2, 3));
 		System.out.println(lineOne);
 	}
 	
 	public static void enablePanel(int width, int height) {
 		panel = new DrawingPanel(width, height);
 		pen = panel.getGraphics();
 	}
 	
 	public static void drawLines() {
 		Line currentLine;
 		for (int xy=1; xy<400; xy += 20) {
 			currentLine = new Line(new Point(xy, 0), new Point(0, xy));
 			currentLine.draw(pen);
 		}
 	}
 	
 	public static void drawDashedLine() {
 		int[] dashes = { 15, 25 };
 		DashedLine dashedLine = new DashedLine(new Point(10, 10),
 											   new Point(190, 10), dashes);
 		dashedLine.draw(pen);
 	}
 	
 	public static void drawSecondDashedLine() {
 		int[] dashes = { 15, 25, 5, 35 };
 		DashedLine dashedLine = new DashedLine(new Point(10, 20),
 											   new Point(190, 20), dashes);
 		dashedLine.draw(pen);
 	}
 	
 	public static void drawSun() {
 		int[] dashes = { 15, 3 };
 		DashedLine dashedLine;
 		Point start = new Point(10, 10);
 		dashedLine = new DashedLine(start, new Point(90, 10), dashes);
 		dashedLine.draw(pen);
 		dashedLine = new DashedLine(start, new Point(90, 37), dashes);
 		dashedLine.draw(pen);
 		dashedLine = new DashedLine(start, new Point(90, 63), dashes);
 		dashedLine.draw(pen);
 		dashedLine = new DashedLine(start, new Point(90, 90), dashes);
 		dashedLine.draw(pen);
 		dashedLine = new DashedLine(start, new Point(63, 90), dashes);
 		dashedLine.draw(pen);
 		dashedLine = new DashedLine(start, new Point(37, 90), dashes);
 		dashedLine.draw(pen);
 		dashedLine = new DashedLine(start, new Point(10, 90), dashes);
 		dashedLine.draw(pen);
 	}
 	
 	public static void drawBigFatLines() {
 		int[] dashes = { 15, 25 };
 		DashedLine dashedLine = new DashedLine(new Point(10, 10),
 											   new Point(190, 190), dashes, 10);
 		dashedLine.draw(pen);
		dashedLine = new DashedLine(new Point(10, 190), new Point(190, 10),
 									dashes, 10);
 		dashedLine.draw(pen);
 	}
 }
