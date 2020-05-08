 package com.spongeblob.paint.model;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.spongeblob.paint.utils.PointUtil;
 
 public class CurveLine3Points extends AbstractShape{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3456072526729463848L;
 	private static float STEP = 0.1f;
 	
 	public CurveLine3Points(){}
 	
 	public CurveLine3Points(int x, int y, Color c){
 		points = new LinkedList<Point>();
 		points.add(new Point(x, y));
 		color = c;
 	}
 	
 	public void drawPathPoints(Graphics g) {
 		g.setColor(getColor());
 		g.drawPolyline(PointUtil.getXs(points), PointUtil.getYs(points), points.size());
 		for (Point point : points) {
 			PointUtil.paintCircleAroundPoint(g, point);
 		}
 	}
 	
 	public void draw(Graphics g) {
 		g.setColor(getColor());
 		List<Point> curvePoints = getCurvePoints();
 		g.drawPolyline(PointUtil.getXs(curvePoints), PointUtil.getYs(curvePoints), curvePoints.size());
 	}
 
	
 	public List<Point> getCurvePoints() {
 		List<Point> pathPoints = new LinkedList<Point>();
 		if (points.size() > 2){
 			for (int i = 0; i < points.size() - 2; i= i + 2) {
				pathPoints.addAll(getCurveLine3Points(points.get(i).x, points.get(i).y, 
 						points.get(i + 1).x, points.get(i + 1).y, 
 						points.get(i + 2).x, points.get(i + 2).y));			
 			}
 		}
 		return pathPoints;
 	}
 	
 
 	/* Formula:
 	 * ((1-t)^2 * P1) + (2*(t)*(1-t) * P2) + ((tt) * P3) */
	public List<Point> getCurveLine3Points(float x1, float y1, float x2, float y2, float x3, float y3) {
 
 		List<Point> mPoints = new LinkedList<Point>();
 		
 		for(float t=0; t <= 1; t += STEP){
 			final float u = 1 - t;
 			final float tt = t*t;
 			final float uu = u*u;
 	
 			final float ut2 = 2 * u * t;
 
 			final float x = (uu * x1) + (ut2 * x2) + (tt * x3);
 			final float y = (uu * y1) + (ut2 * y2) + (tt * y3);
 			
 			mPoints.add(new Point((int)x, (int)y));
 		}
 		return mPoints;
 	}
 
 }
