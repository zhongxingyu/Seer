 package org.creativecoding.fhnw.matrixrenderer.test;
 
 import processing.core.PGraphics;
import fhnw.matrixrenderer.MatrixField;
 
 
 public class SmallField extends MatrixField {
 
 	public SmallField (int theColumn, int theRow) {
 		super (theColumn, theRow);
 	}
 	
 	public void draw (PGraphics theG) {
 		theG.fill (255, 0, 0);
 		theG.rect (x, y, width, height);
 	}
 }
