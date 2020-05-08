 package com.kuxhausen.colorcompete;
 
 import java.util.ArrayList;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.DashPathEffect;
 import android.graphics.Paint;
 import android.graphics.Path;
 
 public class Route {
 
 	public ArrayList<Pair> points;
 	private Path visualPath;
 	private Paint p;
 	public boolean loopMode;
 
 	public Route(Paint paint) {
 
 		points = new ArrayList<Pair>();
 
 		visualPath = new Path();
 
 		p = paint;
 
 	}
 
 	public void addPoint(float x, float y) {
		if (points.size() < 1)
 			visualPath.moveTo(x, y);
 		else
 			visualPath.lineTo(x, y);
 
 		Pair point = new Pair(x,y);
 		points.add(point);
 	}
 	
 	public void moveAlongRoute(float xc, float yc, float speed, boolean foward, GamePiece gp){
 
 		Pair target = foward ? points.get((gp.routePosition+1+points.size())%points.size()): points.get((gp.routePosition-1+points.size())%points.size());
 		Pair delta = new Pair(target.x-xc,target.y-yc);
 		
 		
 		//normalize and scale to speed
 		float scale = speed/delta.getMagnitude();
 		if(Float.isNaN(scale) || Float.isInfinite(scale))
 			scale =0;
 		Pair movement = new Pair(delta.x*scale,delta.y*scale);
 		
 		
 		int nextPair=gp.routePosition;
 		// if arrived at the target
 		if(movement.getMagnitude()>=delta.getMagnitude()){
 			movement=delta;
 			nextPair = foward ? (nextPair + 1 + points.size()) % points.size() : (nextPair - 1 + points.size()) % points.size();
 		}
 		
 		if(gp.gb.move(gp, movement))
 			gp.routePosition=nextPair;
 		else
 			foward = !foward;
 			//gp.gb.move(gp, new Pair(-movement.x,-movement.y));
 		if(gp.routePosition==0)
 			foward = false;
 		if(gp.routePosition==points.size()-1)
 			foward = true;
 		//TODO fix foward
 	}
 
 	public void clear() {
 		points.clear();
 		visualPath.reset();
 	}
 
 	/**
 	 * @param c
 	 *            Canvas onto which GamePiece will draw itself
 	 */
 	public void draw(Canvas c, float xOffset) {
 		visualPath.offset(xOffset, 0);
 		c.drawPath(visualPath, p);
 		visualPath.offset(-xOffset, 0);
 	}
 }
