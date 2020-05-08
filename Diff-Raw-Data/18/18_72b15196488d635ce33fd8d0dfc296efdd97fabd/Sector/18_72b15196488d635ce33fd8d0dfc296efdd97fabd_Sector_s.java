 /*-
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.dawnsci.plotting.draw2d.swtxy.util;
 
 import org.dawnsci.plotting.api.axis.ICoordinateSystem;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.PrecisionPoint;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 
 /**
  * A Draw2D annular sector. Its location is the centre (not the top-left corner of its bounding box)
  */
 public class Sector extends Shape implements PointFunction {
 	
 	private boolean drawSymmetry = false;
 	private Rectangle box; // bounding box of sector
 	private PrecisionPoint centre;
 	private double[] radius;
 	private double[] angle; // degrees
 	private double[] symAngle; // degrees
 	private PointFunction innerFunction;
 	private PointFunction outerFunction;
 
 	public Sector() {
 		this(0, 0, 100, 200, 0, 360);
 		setOpaque(true);
 	}
 
 	/**
 	 * 
 	 * @param cx centre x
 	 * @param cy centre y
 	 * @param inner radius
 	 * @param outer radius
 	 * @param start angle in degrees (0 is horizontal and +ve is anti-clockwise)
 	 * @param end angle in degrees
 	 */
 	public Sector(double cx, double cy, double inner, double outer, double start, double end) {
 		centre = new PrecisionPoint(cx, cy);
 		radius = new double[] { inner, outer };
 		angle = new double[] { start, end };
 		calcBox();
 		innerFunction = new PointFunction() {
 			
 			@Override
 			public void setCoordinateSystem(ICoordinateSystem system) {
 				Sector.this.setCoordinateSystem(system);
 			}
 			
 			@Override
 			public double getAspectRatio() {
 				return Sector.this.getAspectRatio();
 			}
 			
 			@Override
 			public Point calculatePoint(double... parameter) {
 				return Sector.this.getPoint(parameter[0], 0);
 			}
 		};
 
 		outerFunction = new PointFunction() {
 			
 			@Override
 			public void setCoordinateSystem(ICoordinateSystem system) {
 				Sector.this.setCoordinateSystem(system);
 			}
 			
 			@Override
 			public double getAspectRatio() {
 				return Sector.this.getAspectRatio();
 			}
 			
 			@Override
 			public Point calculatePoint(double... parameter) {
 				return Sector.this.getPoint(parameter[0], 1);
 			}
 		};
 	}
 
 	private ICoordinateSystem cs;
 
 	@Override
 	public void setCoordinateSystem(ICoordinateSystem system) {
 		cs = system;
 	}
 
 	public ICoordinateSystem getCoordinateSystem() {
 		return cs;
 	}
 
 	@Override
 	public double getAspectRatio() {
 		return cs.getAspectRatio();
 	}
 
 	public Point getPoint(double degrees, int i) {
 		double angle = -Math.toRadians(degrees);
 		double x = radius[i] * Math.cos(angle) + centre.preciseX();
 		double y = radius[i] * Math.sin(angle) * cs.getAspectRatio() + centre.preciseY();
 		return new Point((int) Math.round(x), (int) Math.round(y));
 	}
 
 	/**
 	 * This should not be called
 	 */
 	@Override
 	public Point calculatePoint(double... parameter) {
 		return null;
 	}
 
 	@Override
 	public void setLocation(Point p) {
 		centre.setPreciseX(p.preciseX() - radius[1]);
 		centre.setPreciseY(p.preciseY() - radius[1] / cs.getAspectRatio());
 		calcBox();
 	}
 
 	/**
 	 * Set centre
 	 * @param cx
 	 * @param cy
 	 */
 	public void setCentre(double cx, double cy) {
 		centre.setPreciseX(cx);
 		centre.setPreciseY(cy);
 		calcBox();
 	}
 
 	/**
 	 * Set radii
 	 * @param inner
 	 * @param outer
 	 */
 	public void setRadii(double inner, double outer) {
 		radius[0] = inner;
 		radius[1] = outer;
 		calcBox();
 	}
 
 	/**
 	 * Set angles
 	 * @param start
 	 * @param end
 	 */
 	public void setAnglesDegrees(double start, double end) {
 		angle[0] = start;
 		angle[1] = end;
 		calcBox();
 	}
 	
 	/**
 	 * Set angles
 	 * @param start
 	 * @param end
 	 */
 	public void setSymmetryAnglesDegrees(double start, double end) {
 		if (symAngle==null) symAngle = new double[2];
 		symAngle[0] = start;
 		symAngle[1] = end;
 		calcBox();
 	}
 
 	public PrecisionPoint getCentre() {
 		return centre;
 	}
 
 	public double[] getRadii() {
 		return radius;
 	}
 
 	public double[] getAnglesDegrees() {
 		return angle;
 	}
 
 	private void calcBox() {
 		double rx = radius[1];
 		double ry = (cs == null ? 1 : cs.getAspectRatio()) * rx;
 		int lx = (int) Math.ceil(2 * rx + 1);
 		int ly = (int) Math.ceil(2 * ry + 1);
 		box = new Rectangle((int) Math.floor(centre.preciseX() - rx),
 				(int) Math.floor(centre.preciseY() - ry), lx, ly);
 		setBounds(box);
 	}
 
 	@Override
 	public boolean containsPoint(int x, int y) {
 		if (!isOpaque()) return false;
 		if (!super.containsPoint(x, y))
 			return false;
 		double px = x - centre.preciseX();
 		double py = (centre.preciseY() - y) / cs.getAspectRatio();
 		double r = Math.hypot(px, py);
 		if (r < radius[0] || r > radius[1])
 			return false;
 		double a = Math.toDegrees(Math.atan2(py, px));
 		if (a < angle[0]) {
 			a += 360;
 		} else if (a > angle[1]) {
 			a -= 360;
 		}
 		return a >= angle[0] && a <= angle[1];
 	}
 
 	@Override
 	protected void fillShape(Graphics graphics) {
 		graphics.pushState();
 		graphics.setAdvanced(true);
 		graphics.setAntialias(SWT.ON);
 
		fillSector(graphics, radius, angle);
		
		if (isDrawSymmetry() && symAngle!=null) fillSector(graphics, radius, symAngle);
 
 		graphics.popState();
 	}
 
	private void fillSector(Graphics graphics, double[] rad, double[] ang) {
		PointList points = Draw2DUtils.generateCurve(innerFunction, angle[0], angle[1], 1, 3, Math.toRadians(1));
		PointList oPoints = Draw2DUtils.generateCurve(outerFunction, angle[0], angle[1], 1, 3, Math.toRadians(1));
 		oPoints.reverse();
 		points.addAll(oPoints);
 		graphics.fillPolygon(points);
//		Rectangle bnd = new Rectangle();
//		graphics.getClip(bnd);
//		Draw2DUtils.fillClippedPolygon(graphics, points, bnd);
 	}
 
 	@Override
 	protected void outlineShape(Graphics graphics) {
 		graphics.pushState();
 		graphics.setAdvanced(true);
 		graphics.setAntialias(SWT.ON);
 
 		PointList points = Draw2DUtils.generateCurve(innerFunction, angle[0], angle[1], 1, 3, Math.toRadians(1));
 		PointList oPoints = Draw2DUtils.generateCurve(outerFunction, angle[0], angle[1], 1, 3, Math.toRadians(1));
 		oPoints.reverse();
 		points.addAll(oPoints);
 		Rectangle bnd = new Rectangle();
 		graphics.getClip(bnd);
 		Draw2DUtils.drawClippedPolyline(graphics, points, bnd, true);
 
 		graphics.popState();
 	}
 
 	public boolean isDrawSymmetry() {
 		return drawSymmetry;
 	}
 
 	public void setDrawSymmetry(boolean drawSymmetry) {
 		this.drawSymmetry = drawSymmetry;
 	}
 }
