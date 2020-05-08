 package org.creativecoding.fhnw.matrixrenderer.test;
 
import fhnw.matrixrenderer.MatrixRenderer;
 import processing.core.PApplet;
 
 
 public class Main extends PApplet {
 	
 	private MatrixRenderer render;
 	
 	public void setup () {
 		size (1440, 600);
 		
 		render = new MatrixRenderer (this);
 		render.addSpace (new SmallField (0, 0));
 		render.addSpace (new SmallField (1, 1));
 		render.addSpace (new SmallField (2, 0));
 	}
 	
 	public void draw () {
 		background (255);
 		render.draw ();
 	}
 	
 	public void keyReleased () {
 		switch (keyCode) {
 			case 71:
 				if (render.showsGrid ())
 					render.setMode (MatrixRenderer.RENDER_NO_GRID);
 				else
 					render.setMode (MatrixRenderer.RENDER_GRID);
 				break;
 			case 73:
 				if (render.showsIds ())
 					render.setMode (MatrixRenderer.RENDER_NO_IDS);
 				else
 					render.setMode (MatrixRenderer.RENDER_IDS);
 				break;
 		}
 	}
 }
