 package com.example.gamedice;
 
 import java.util.Random;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.RectF;
 
 
 public class TaoDie {
 	RectF rect, selRect;
 	TaoColor color = TaoColor.randomColor();
 	Paint diePaint = new Paint();
 	Paint extraDiePaint = new Paint();
 	Paint dotPaint = new Paint();
 	Paint selectedPaint = new Paint();
 	int width, height;					// Screen dimensions
 	int winMinX, winMaxX, winMinY, winMaxY;		// Die drawing window (center of die must be within window)
 	int x, y;							// Die Location
 	Random dieRand = new Random();		// Random generator for die location and rotation
 	float rotation;						// Die rotation in degrees
 	double length;						// Die length
 	float dotRadius;					// Dot radius
 	float rectRadius;					// Rect corner radius
 	double dieRadius;					// Radius of bounding circle
 	boolean selected = false;			// Is the die currently selected
 	
 	public TaoDie(int width, int height) {
 		float length = Math.min(width, height) / 10;
 		rect = new RectF(-length, -length, length, length);
 		float selLength = length * (float) 1.1;
 		selRect = new RectF(-selLength, -selLength, selLength, selLength);
 		this.width = width;
 		this.height = height;
 		
 		dieRadius = Math.sqrt(2 * Math.pow(length, 2)) * 1.1;
 		dotRadius = (float) (length * 0.7);
 		rectRadius = (float) (length * 0.3);
 		winMinX = (int) dieRadius;
 		winMaxX = width - (int) dieRadius;
 		winMinY = (int) dieRadius;
 		winMaxY = height - (int) dieRadius;
 		
 		dotPaint.setStyle(Style.FILL);
 		diePaint.setStyle(Style.FILL);
 		diePaint.setColor(Color.WHITE);
 		extraDiePaint.setStyle(Style.FILL);
 		extraDiePaint.setColor(Color.GRAY);
 		selectedPaint.setStyle(Style.FILL);
 		selectedPaint.setColor(Color.RED);
 	}
 	
 	public void roll() {
 		color = TaoColor.randomColor();
 		dotPaint.setColor(color.convertColor());
 		changeLocation();
 	}
 	
 	/*public TaoColor getRoll() {
 		color = TaoColor.randomColor();
 		changeLocation();
 		return getColor();
 	}
 	
 	public TaoColor getColor() {
 		return color;
 	}*/
 	
 	public void changeLocation() {
 		x = dieRand.nextInt(winMaxX - winMinX + 1) + winMinX;
 		y = dieRand.nextInt(winMaxY - winMinY + 1) + winMinY;
 		rotation = dieRand.nextFloat() * 90;
 	}
 	
 	// Die selection methods
 	public void select() {
 		selected = true;
 	}
 	
 	public void unselect() {
 		selected = false;
 	}
 	
 	public void toggleSelect() {
 		selected = ! selected;
 	}
 	
 	public void draw(Canvas canvas) {
		draw(canvas, false);
	}
	
	public void draw(Canvas canvas, boolean extra) {
 		canvas.save();
 		canvas.translate(x, y);
 		canvas.rotate(rotation, 0, 0);
 		
 		// If selected first paint the selection box;
 		if (selected) {
 			canvas.drawRoundRect(selRect, rectRadius, rectRadius, selectedPaint);
 		}
		Paint paint = extra ? extraDiePaint : diePaint;
 		canvas.drawRoundRect(rect, rectRadius, rectRadius, paint);
 		canvas.drawCircle(0,  0,  dotRadius,  dotPaint);
 		canvas.restore();
 	}
 	
 	public int getX() {
 		return x;
 	}
 	
 	public int getY() {
 		return y;
 	}
 	
 	public double getRadius() {
 		return dieRadius;
 	}
 	
 	public boolean isOverlapping(TaoDie otherDie) {
 		double dist = Math.sqrt(Math.pow(otherDie.getX() -  x,2) + Math.pow(otherDie.getY() - y,2));
 		double minRequiredDist = otherDie.getRadius() + dieRadius;
 		return dist < minRequiredDist;
 	}
 	
 	public boolean isIn(float x, float y) {
 		double dist = Math.sqrt(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2));
 		return dist < dieRadius;
 	}
 	
 	public boolean isSelected() {
 		return selected;
 	}
 }
