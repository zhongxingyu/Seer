 package org.i52jianr.multidraw;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Pixmap.Filter;
 import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
 
 public class Brush {
 	int size;
 	float[][] brush;
 	
 	public Brush(int size, float[][] brush) {
 		this.size = size;
 		this.brush = brush;
 	}
 	
 	public int getSize() {
 		return size;
 	}
 	
 	public float[][] getBrush() {
 		return brush;
 	}
 	
 	public Pixmap getPixmap() {
 		Pixmap pix = new Pixmap(brush.length, brush.length, Format.RGB888);
 		for (int i = 0; i < brush.length; i++) {
 			for (int j = 0; j < brush.length; j++) {
 				pix.setColor(new Color(brush[i][j], brush[i][j], brush[i][j], brush[i][j]));
 				pix.drawPixel(j, i);
 			}
 		}
 		
 		Pixmap canvas = new Pixmap(32, 32, Format.RGB888);
 		Pixmap.setFilter(Filter.NearestNeighbour);
 		canvas.drawPixmap(pix, 0, 0, brush.length, brush.length, 16 - brush.length, 16 - brush.length, brush.length*2, brush.length*2);
 		
 		return canvas;
 	}
 }
