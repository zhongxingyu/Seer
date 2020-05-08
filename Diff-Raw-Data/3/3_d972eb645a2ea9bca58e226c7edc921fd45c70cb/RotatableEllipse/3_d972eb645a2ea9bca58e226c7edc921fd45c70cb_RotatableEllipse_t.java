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
 
 import java.util.Arrays;
 
 import org.dawnsci.plotting.api.axis.ICoordinateSystem;
 import org.dawnsci.plotting.draw2d.Activator;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.PrecisionPoint;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 
 /**
  * A Draw2D ellipse that allows its orientation to be set. Its location is the centre of rotation
  * (not the top-left corner of its bounding box)
  */
 public class RotatableEllipse extends Shape implements PointFunction {
 	private static PrecisionPoint centre = new PrecisionPoint(0.5, 0.5);
 	private AffineTransform affine; // transforms unit square (origin at top-left corner) to transformed rectangle
 	private Rectangle box; // bounding box of ellipse
 	private boolean outlineOnly = false;
 	private boolean showMajorAxis = false;
 
 	/**
 	 * Unit circle centred on origin
 	 */
 	public RotatableEllipse() {
 		affine = new AffineTransform();
 	}
 
 	/**
 	 * 
 	 * @param cx centre
 	 * @param cy centre
 	 * @param major axis length
 	 * @param minor axis length
 	 * @param angle of major axis from horizontal (in degrees, positive for clockwise)
 	 */
 	public RotatableEllipse(double cx, double cy, double major, double minor, double angle) {
 		affine = new AffineTransform();
 		affine.setTranslation(cx - 0.5 * major, cy - 0.5 * minor);
 		affine.setScale(major, minor);
 		setAngleDegrees(angle);
 	}
 
 	private ICoordinateSystem cs;
 
 	@Override
 	public void setCoordinateSystem(ICoordinateSystem system) {
 		cs = system;
 	}
 
 	public ICoordinateSystem getCoordinateSystem() {
 		return cs;
 	}
 
 	/**
 	 * Set centre position
 	 * @param cx
 	 * @param cy
 	 */
 	public void setCentre(double cx, double cy) {
 		affine.setAspectRatio(cs.getAspectRatio());
 		Point oc = affine.getTransformed(centre);
 		affine.setTranslation(affine.getTranslationX() + cx - oc.preciseX(), affine.getTranslationY() + cy - oc.preciseY());
 		calcBox(true);
 	}
 
 	/**
 	 * Set angle of rotated ellipse to given degrees (positive for clockwise)
 	 * @param degrees
 	 */
 	public void setAngleDegrees(double degrees) {
 		affine.setAspectRatio(cs.getAspectRatio());
 		Point oc = affine.getTransformed(centre);
 		affine.setRotationDegrees(degrees);
 		Point nc = affine.getTransformed(centre);
 		affine.setTranslation(affine.getTranslationX() + oc.preciseX() - nc.preciseX(), affine.getTranslationY() + oc.preciseY() - nc.preciseY());
 		calcBox(true);
 	}
 
 	/**
 	 * @return angle of rotation in degrees (positive for anti-clockwise)
 	 */
 	public double getAngleDegrees() {
 		return affine.getRotationDegrees();
 	}
 
 	/**
 	 * @return centre of ellipse
 	 */
 	public Point getCentre() {
 		return affine.getTransformed(centre);
 	}
 
 	/**
 	 * @return major and minor axis lengths
 	 */
 	public double[] getAxes() {
 		return new double[] { affine.getScaleX(), affine.getScaleY()};
 	}
 
 	/**
 	 * Get point on ellipse at given angle
 	 * @param degrees (positive for anti-clockwise)
 	 * @return
 	 */
 	public Point getPoint(double degrees) {
 		double angle = -Math.toRadians(degrees);
 		double c = Math.cos(angle);
 		double s = Math.sin(angle);
 		PrecisionPoint p = new PrecisionPoint(0.5*(c+1), 0.5*(s+1));
 		return affine.getTransformed(p);
 	}
 
 	@Override
 	public Point calculatePoint(double... parameter) {
 		return getPoint(parameter[0]);
 	}
 
 	/**
 	 * @param show if true, show major axis
 	 */
 	public void showMajorAxis(boolean show) {
 		showMajorAxis = show;
 	}
 
 	/**
 	 * @return affine transform (this is a copy)
 	 */
 	public AffineTransform getAffineTransform() {
 		return affine.clone();
 	}
 
 	/**
 	 * @param point
 	 * @return transformed point
 	 */
 	public Point getTransformedPoint(Point point) {
 		return affine.getTransformed(point);
 	}
 
 	/**
 	 * @param point
 	 * @return inverse-transformed point
 	 */
 	public Point getInverseTransformedPoint(Point point) {
 		return affine.getInverseTransformed(point);
 	}
 
 	/**
 	 * Set major and minor axes lengths
 	 * @param major
 	 * @param minor
 	 */
 	public void setAxes(double major, double minor) {
 		affine.setAspectRatio(cs.getAspectRatio());
 		Point oc = affine.getTransformed(centre);
 		affine.setScale(major, minor);
 		Point nc = affine.getTransformed(centre);
 		affine.setTranslation(affine.getTranslationX() + oc.preciseX() - nc.preciseX(), affine.getTranslationY() + oc.preciseY() - nc.preciseY());
 		calcBox(true);
 	}
 
 	/**
 	 * Set aspect ratio for y/x scaling
 	 * @param aspect
 	 */
 	public void setAspectRatio(double aspect) {
 		affine.setAspectRatio(aspect);
 	}
 
 	@Override
 	public double getAspectRatio() {
 		return cs.getAspectRatio();
 	}
 
 	private void calcBox(boolean redraw) {
 		affine.setAspectRatio(cs.getAspectRatio());
 		box = affine.getBounds();
 		if (redraw)
 			setBounds(box.expand(2, 2));
 	}
 
 	@Override
 	public void setLocation(Point p) {
 		affine.setTranslation(p.preciseX(), p.preciseY());
 		calcBox(true);
 	}
 
 	@Override
 	public boolean containsPoint(int x, int y) {
 		if (outlineOnly) {
 			double d = affine.getInverseTransformed(new PrecisionPoint(x, y)).getDistance(centre);
 			return Math.abs(d - 0.5) < 2./Math.max(affine.getScaleX(), affine.getScaleY());
 		}
 
 		if (!super.containsPoint(x, y) || !box.contains(x, y))
 			return false;
 
 		Point p = affine.getInverseTransformed(new PrecisionPoint(x, y));
 		return p.getDistance(centre) <= 0.5;
 	}
 
 	@Override
 	public void setFill(boolean b) {
 		super.setFill(b);
 		outlineOnly  = !b;
 	}
 
 	@Override
 	protected void fillShape(Graphics graphics) {
 		if (!isShapeFriendlySize()) return;
 
 		graphics.pushState();
 		graphics.setAdvanced(true);
 		graphics.setAntialias(SWT.ON);
 		graphics.translate((int) affine.getTranslationX(), (int) affine.getTranslationY());
 		graphics.rotate((float) affine.getRotationDegrees());
 		// NB do not use Graphics#scale and unit shape as there are precision problems
 		calcBox(false);
 		graphics.fillOval(0, 0, (int) affine.getScaleX(), (int) affine.getScaleY());
 		graphics.popState();
 	}
 
 	@Override
 	protected void outlineShape(Graphics graphics) {
 		graphics.pushState();
 		graphics.setAdvanced(true);
 		graphics.setAntialias(SWT.ON);
 
 		calcBox(false);
 		PointList points = Draw2DUtils.generateCurve(this, 0, 360, 1, 3, Math.toRadians(1));
 		Rectangle bnd = new Rectangle();
 		graphics.getClip(bnd);
 		Draw2DUtils.drawClippedPolyline(graphics, points, bnd, true);
 
 		if (showMajorAxis) {
			double offset = cs.getXAxisRotationAngleDegrees();
			graphics.drawLine(getRoundedPoint(offset), getRoundedPoint(offset + 180));
 		}
 		graphics.popState();
 	}
 
 	private boolean isShapeFriendlySize() {
 		IFigure p = getParent();
 		if (p == null)
 			return true;
 
 		int ax = (int)affine.getScaleX();
 		int ay = (int)affine.getScaleY();
 		
 		// If the affine transform is outside the size of the bounds, we
 		// are very likely to be off-screen. 
 		// On linux off screen is bad therefore we do not draw
 		// Fix to http://jira.diamond.ac.uk/browse/DAWNSCI-429
 		if (Activator.isLinuxOS()) {
 			Rectangle bnds = p.getBounds().getExpanded(500, 500); // This is a fudge, very elongated do still not show.
 																  // Better than crashes however...
 			if (ax>bnds.width && ay>bnds.height) return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Axes " + Arrays.toString(getAxes()) + ", centre " + getCentre() + ", angle " + getAngleDegrees();
 	}
 }
