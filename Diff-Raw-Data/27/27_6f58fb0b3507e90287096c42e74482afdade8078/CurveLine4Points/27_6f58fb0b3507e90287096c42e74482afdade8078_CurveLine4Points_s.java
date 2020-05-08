 package com.spongeblob.paint.model;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.spongeblob.paint.utils.PointUtil;
 
 public class CurveLine4Points extends AbstractShape{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6959381887395467014L;
 	private static float STEP = 0.1f;
 
 	public CurveLine4Points(){}
 	
 	public CurveLine4Points(int x, int y, Color c){
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
 		if (points.size() > 3){
 			for (int i = 0; i < points.size() - 3; i= i + 3) {
				pathPoints.addAll(getCurveLine4Points(points.get(i).x, points.get(i).y, 
 						points.get(i + 1).x, points.get(i + 1).y, 
 						points.get(i + 2).x, points.get(i + 2).y,
 						points.get(i + 3).x, points.get(i + 3).y));			
 			}
 		}
 		return pathPoints;
 	}
 	
 
 	/* Formula:
 	 * ((1-t)^2 * P1) + (2*(t)*(1-t) * P2) + ((tt) * P3) */
	public List<Point> getCurveLine4Points(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
 
 		List<Point> mPoints = new LinkedList<Point>();
 		
 		for(float t=0; t <= 1; t += STEP){
 
 			final float u = 1 - t;
 			final float tt = t * t;
 			final float uu = u * u;
 			final float uuu = uu * u;
 			final float ttt = tt * t;
 	
 			final float ut3 = 3 * uu * t;
 			final float utt3 = 3 * u * tt;
 	
 			final float x = (uuu * x1) + (ut3 * x2) + (utt3 * x3) + (ttt * x4);
 			final float y = (uuu * y1) + (ut3 * y2) + (utt3 * y3) + (ttt * y4);
 			
 			mPoints.add(new Point((int)x, (int)y));
 		}
 		return mPoints;
 	}
 
 }
