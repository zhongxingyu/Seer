 package org.creativecoding.fhnw.matrixrenderer.test;
 
import org.creativecoding.fhnw.matrixrenderer.MatrixField;
 import processing.core.PGraphics;
 
 
 public class SmallField extends MatrixField {
 
 	public SmallField (int theColumn, int theRow) {
 		super (theColumn, theRow);
 	}
 	
 	public void draw (PGraphics theG) {
 		theG.fill (255, 0, 0);
 		theG.rect (x, y, width, height);
 	}
 }
