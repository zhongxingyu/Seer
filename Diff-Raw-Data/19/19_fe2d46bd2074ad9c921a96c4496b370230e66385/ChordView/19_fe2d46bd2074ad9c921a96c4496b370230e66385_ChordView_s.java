 /*
  * Created on 12.06.2010
  *
  */
 package de.wertarbyte.chorddroid;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.view.View;
 
 public class ChordView extends View {
 	
 	private Shape chordshape;
 	
 	public ChordView(Context context, AttributeSet as) {
 		super(context, as);
 		chordshape = null;
 	}
 	
 	public void setShape(Shape chordshape) {
 		this.chordshape = chordshape; 
 		invalidate();
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
         int width = getWidth();
         int height = getHeight();
         
         // we use a square playing field
         int size = Math.min(width, height);
 
 		if (chordshape != null) {
 			// draw the chord shape
 			drawLines(canvas, size);
 		}
 	}
 	
 	private void drawLines(Canvas c, int size) {
 		int[] pos = chordshape.getPositions();
 		int n = pos.length;
 		
 		// number of frets we need
 		int frets = Math.max(4, (chordshape.getMaxPos()-Math.max(chordshape.getMinPos(), 1)) );
 		
 		float d_w = size/(frets+1);
 		float d_h = size/(n+1);
 		
 		Paint string_paint = new Paint();
 		string_paint.setColor(Color.WHITE);
 		string_paint.setStrokeWidth(4);
 		
 		Paint fret_paint = new Paint();
 		fret_paint.setColor(Color.WHITE);
 		string_paint.setStrokeWidth(2);
 
 		fret_paint.setTextSize(24);
 		c.drawText(Math.max(chordshape.getMinPos(), 1)+"", d_w*0.5f, d_h*0.5f , fret_paint);
 
 		for (int i = n; i>0;  i--) {
 			c.drawLine(0, d_h*i, size, d_h*i, string_paint);
 		}
 		
 		for (int i = frets; i>0; i--) {
 			c.drawLine(d_w*i, d_h*0.75f, d_w*i, d_h*(pos.length+0.25f), string_paint);
 		}
 		
 		for (int string = 0; string < pos.length; string++) {
 			int fret = pos[pos.length-string-1];
 			float r = d_w/4f;
 			if (fret > 0) {
 				float c_x = d_w * ((float)fret+0.5f-1f);
 				float c_y = d_h * (1f+(float)string);
 				c.drawCircle( c_x, c_y, r, fret_paint);
 			} else if (fret <0) {
 				// paint markers on muted strings 
 				float c_x = d_w * 0.5f;
 				float c_y = d_h * (1f+(float)string);
 				c.drawLine(c_x-r, c_y-r, c_x+r, c_y+r, string_paint);
 				c.drawLine(c_x-r, c_y+r, c_x+r, c_y-r, string_paint);
 			}
 		}
 		
 	}
 }
