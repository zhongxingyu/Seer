 package org.dawnsci.plotting.draw2d.swtxy.util;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.DirectColorModel;
 import java.awt.image.IndexColorModel;
 import java.awt.image.WritableRaster;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Display;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Draw2DUtils {
 	private static Logger logger = LoggerFactory.getLogger(Draw2DUtils.class);
 
 	/**
 	 * Attempts to get the centre of a figure using its bounds.
 	 * @param bx
 	 * @return
 	 */
 	public static Point getCenter(Figure bx) {
 		final Point   location = bx.getLocation();
 		final Rectangle bounds = bx.getBounds();
 		return new Point(location.x+(bounds.width/2), location.y+(bounds.height/2));
 	}
 
 	public static Cursor getRoiControlPointCursor() {
 		return Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND);
 	}
 	public static Cursor getRoiMoveCursor() {
 		return Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZEALL);
 	}
 
 
 	/**
 	 * Call to make SWT Image data for SWT Image using swing BufferedImage
 	 * 
 	 * This allows one to use Java 2d to draw the primitive.
 	 * 
 	 * @param bufferedImage
 	 * @return
 	 */
 	public static ImageData convertToSWT(BufferedImage bufferedImage) {
 		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
 			DirectColorModel colorModel = (DirectColorModel) bufferedImage
 					.getColorModel();
 			PaletteData palette = new PaletteData(colorModel.getRedMask(),
 					colorModel.getGreenMask(), colorModel.getBlueMask());
 			ImageData data = new ImageData(bufferedImage.getWidth(),
 					bufferedImage.getHeight(), colorModel.getPixelSize(),
 					palette);
 			WritableRaster raster = bufferedImage.getRaster();
 			int[] pixelArray = new int[3];
 			for (int y = 0; y < data.height; y++) {
 				for (int x = 0; x < data.width; x++) {
 					raster.getPixel(x, y, pixelArray);
 					int pixel = palette.getPixel(new RGB(pixelArray[0],
 							pixelArray[1], pixelArray[2]));
 					data.setPixel(x, y, pixel);
 				}
 			}
 			return data;
 		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
 			IndexColorModel colorModel = (IndexColorModel) bufferedImage
 					.getColorModel();
 			int size = colorModel.getMapSize();
 			byte[] reds = new byte[size];
 			byte[] greens = new byte[size];
 			byte[] blues = new byte[size];
 			colorModel.getReds(reds);
 			colorModel.getGreens(greens);
 			colorModel.getBlues(blues);
 			RGB[] rgbs = new RGB[size];
 			for (int i = 0; i < rgbs.length; i++) {
 				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
 						blues[i] & 0xFF);
 			}
 			PaletteData palette = new PaletteData(rgbs);
 			ImageData data = new ImageData(bufferedImage.getWidth(),
 					bufferedImage.getHeight(), colorModel.getPixelSize(),
 					palette);
 			data.transparentPixel = colorModel.getTransparentPixel();
 			WritableRaster raster = bufferedImage.getRaster();
 			int[] pixelArray = new int[1];
 			for (int y = 0; y < data.height; y++) {
 				for (int x = 0; x < data.width; x++) {
 					raster.getPixel(x, y, pixelArray);
 					data.setPixel(x, y, pixelArray[0]);
 				}
 			}
 			return data;
 		}
 		return null;
 	}
 
 	/**
 	 * Generate a curve from a parametrised point function
 	 * @param fn
 	 * @param lower
 	 * @param upper
 	 * @param delta
 	 * @return a point list of curve
 	 */
 	public static PointList generateCurve(PointFunction fn, double lower, double upper, double delta) {
 		return generateCurve(fn, lower, upper, delta, 3, Math.toRadians(0.5));
 	}
 
 	/**
 	 * Generate a curve from a parametrised point function
 	 * @param fn
 	 * @param lower
 	 * @param upper
 	 * @param delta
 	 * @param minDistance
 	 * @param maxAngle (in radians)
 	 * @return a point list of curve
 	 */
 	public static PointList generateCurve(PointFunction fn, double lower, double upper, double delta, double minDistance, double maxAngle) {
 		double vd = delta;
 		PointList list = new PointList();
 		Point pp, pc;
 		pp = fn.calculatePoint(lower);
 		list.addPoint(pp);
 		double cos = Math.cos(maxAngle);
 		boolean force = false;
 		double vc = lower + vd;
 		double dxp = Double.NaN; // previous deltas
 		double dyp = 0;
 		double sp = 0;
 		while (vc < upper) {
 			pc = fn.calculatePoint(vc);
 			double dxc = pc.x() - pp.x(); // current deltas
 			double dyc = pc.y() - pp.y();
 			double sc = Math.hypot(dxc, dyc);
 			if (sc >= minDistance) {
 				if (Double.isNaN(dxp)) {
 					vd *= 0.5;
 					vc -= vd;
 					force = true; // prevent bouncing
 					continue;
 				} else {
 					double cc = dxc * dxp + dyc * dyp;
 					if (cc * cc < sc * sp * cos) {
 						// angle too wide so halve step and backtrack
 						vd *= 0.5;
 						vc -= vd;
 						force = true; // prevent bouncing
 						continue;
 					}
 				}
 			} else if (!force) {
 				// point too close so double step and skip forward
 				vd *= 2;
 				vc += vd;
 				continue;
 			}
 			list.addPoint(pc);
 			pp = pc;
 			dxp = dxc;
 			dyp = dyc;
 			sp = sc;
 			vc += vd;
 			force = false;
 		}
 		list.addPoint(fn.calculatePoint(upper));
 		return list;
 	}
 
 	/**
 	 * Draw a curve from a parametrised point function
 	 * @param g
 	 * @param bounds (can be null for no check)
 	 * @param isPolygon if true, join last point to first
 	 * @param fn
 	 * @param lower
 	 * @param upper
 	 * @param delta
 	 * @return true, if drawn
 	 */
 	public static boolean drawCurve(Graphics g, Rectangle bounds, boolean isPolygon, PointFunction fn, double lower, double upper, double delta) {
 		return drawCurve(g, bounds, isPolygon, fn, lower, upper, delta, 3, Math.toRadians(0.5));
 	}
 
 	/**
 	 * Draw a curve from a parametrised point function
 	 * @param g
 	 * @param bounds (can be null for no check)
 	 * @param isPolygon if true, join last point to first
 	 * @param fn
 	 * @param lower
 	 * @param upper
 	 * @param delta
 	 * @param minDistance
 	 * @param maxAngle (in radians)
 	 * @return true, if drawn
 	 */
 	public static boolean drawCurve(Graphics g, Rectangle bounds, boolean isPolygon, PointFunction fn, double lower, double upper, double delta, double minDistance, double maxAngle) {
 		List<Double> parameters = new ArrayList<Double>();
 		double[] angles;
 
 		// find all parameters that intersect edges of bounding box
 		Point ptl = bounds.getTopLeft();
 		Point pbr = bounds.getBottomRight();
 		bounds = bounds.getExpanded(1, 1); // need expanded bounds copy for checks
 		angles = fn.calculateXIntersectionParameters(ptl.x());
 		if (angles != null) {
 			for (double a : angles) {
 				if (bounds.contains(fn.calculatePoint(a)))
 					parameters.add(a);
 			}
 		}
 		angles = fn.calculateYIntersectionParameters(ptl.y());
 		if (angles != null) {
 			for (double a : angles) {
 				if (bounds.contains(fn.calculatePoint(a)))
 					parameters.add(a);
 			}
 		}
 		angles = fn.calculateXIntersectionParameters(pbr.x());
 		if (angles != null) {
 			for (double a : angles) {
 				if (bounds.contains(fn.calculatePoint(a)))
 					parameters.add(a);
 			}
 		}
 		angles = fn.calculateYIntersectionParameters(pbr.y());
 		if (angles != null) {
 			for (double a : angles) {
 				if (bounds.contains(fn.calculatePoint(a)))
 					parameters.add(a);
 			}
 		}
 
 		Collections.sort(parameters);
 		// select subset within given range
 		int size = parameters.size();
 		int beg = 0;
 		for (; beg < size; beg++) {
 			if (parameters.get(beg) > lower)
 				break;
 		}
 		int end = beg;
 		for (; end < size; end++) {
 			if (parameters.get(end) >= upper)
 				break;
 		}
 		List<Double> subset = parameters.subList(beg, end);
 
 		// check if end points are in bounds
 		Point upt = fn.calculatePoint(upper);
 		if (bounds.contains(upt)) {
 			subset.add(upper);
 		}
		Point lpt = fn.calculatePoint(lower);
		Point last = lpt;
		for (int i = 0; i < subset.size(); i++) { // remove same or adjacent points
			Point p = fn.calculatePoint(subset.get(i));
			if (p.getDistance(last) < 2) {
				subset.remove(i--);
				logger.debug("Removed point {} at parameter {}", i, subset.get(i));
			}
			last = p;
		}
 		size = subset.size();
 		boolean inside = false; // is next segment inside bounds?
 		if (bounds.contains(lpt)) {
 			if (size == 0) {
 				logger.debug("Only lower parameter is within bounds!!!");
 				return false;
 			}
 			subset.add(0, lower);
 			size++;
 			inside = true;
 		} else {
 			if (size > 1) {
 				inside = bounds.contains(fn.calculatePoint(0.5*(subset.get(0) + subset.get(1))));
 			} else {
 				if (size == 1) {
 					logger.debug("Only lower parameter is within bounds!!!");
 				} else {
 					logger.debug("Draw nothing!");
 				}
 				return false;
 			}
 		}
 
 		// now draw alternative segments of parameter
 		double b = subset.get(0);
 		for (int i = 1; i < size; i++) {
 			double e = subset.get(i);
 			if (inside) {
 				PointList list = generateCurve(fn, b, e, delta, minDistance, maxAngle);
 				g.drawPolyline(list);
 			}
 			inside = !inside;
 			b = e;
 		}
 		if (isPolygon) {
 			if (bounds.contains(upt) && bounds.contains(lpt) && !upt.equals(lpt)) {
 				g.drawLine(upt, lpt);
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Draw a polyline clipped by given bounds
 	 * @param g
 	 * @param points of polyline
 	 * @param bounds
 	 * @param isPolygon if true, join last point to first
 	 */
 	public static void drawClippedPolyline(Graphics g, PointList points, Rectangle bounds, boolean isPolygon) {
 		final int pts = points.size();
 		if (pts < 2)
 			return;
 
 		double xl = bounds.preciseX();
 		double xh = xl + bounds.preciseWidth();
 		double yl = bounds.preciseY();
 		double yh = yl + bounds.preciseHeight();
 
 		Point p0;
 		Point p1 = points.getPoint(0);
 		PointList list = new PointList();
 		double[] t = new double[2];
 
 		boolean first = true;
 		for (int i = 1; i < pts || (isPolygon && i == pts); i++) {
 			p0 = p1;
 			p1 = points.getPoint(i % pts);
 
 			t[0] = 0;
 			t[1] = 1;
 			double x0 = p0.preciseX();
 			double y0 = p0.preciseY();
 			double dx = p1.preciseX() - x0;
 			double dy = p1.preciseY() - y0;
 
 			if (dx == 0 && dy == 0)
 				continue; // ignore null segment
 
 			if (first) { // find first segment that is (partly) in bounds
 				if (clip(xl - x0, dx, t) && clip(x0 - xh, -dx, t)
 						&& clip(yl - y0, dy, t) && clip(y0 - yh, -dy, t)) {
 					if (t[0] > 0) {
 						p0 = new Point((int) Math.round(x0 + t[0] * dx), (int) Math.round(y0 + t[0] * dy));
 					} else {
 						p0 = new Point((int) Math.round(x0), (int) Math.round(y0));
 					}
 					list.removeAllPoints();
 					list.addPoint(p0);
 					if (t[1] < 1) {
 						Point p = new Point((int) Math.round(x0 + t[1] * dx), (int) Math.round(y0 + t[1] * dy));
 						list.addPoint(p);
 						g.drawPolygon(list);
 					} else {
 						first = false;
 						list.addPoint(new Point((int) Math.round(p1.preciseX()), (int) Math.round(p1.preciseY())));
 					}
 				}
 			} else { // given that p0 is in bounds
 				if (clip2(xl - x0, dx, t) && clip2(x0 - xh, -dx, t) && clip2(yl - y0, dy, t) && clip2(y0 - yh, -dy, t)) {
 					if (t[1] < 1) {
 						first = true;
 						Point p = new Point((int) Math.round(x0 + t[1] * dx), (int) Math.round(y0 + t[1] * dy));
 						list.addPoint(p);
 						g.drawPolyline(list);
 					} else {
 						list.addPoint(new Point((int) Math.round(p1.preciseX()), (int) Math.round(p1.preciseY())));
 					}
 				}
 			}
 		}
 
 		if (list.size() > 0) {
 			g.drawPolyline(list);
 		}
 	}
 
 	private static boolean clip(double n, double d, double[] t) {
 		if (d == 0) {
 			return n <= 0;
 		}
 
 		double v = n/d;
 		if (d > 0) {
 			if (v > t[1])
 				return false;
 			if (v > t[0])
 				t[0] = v;
 		} else {
 			if (v < t[0])
 				return false;
 			if (v < t[1])
 				t[1] = v;
 		}
 
 		return true;
 	}
 
 	private static boolean clip2(double n, double d, double[] t) {
 		if (d == 0) {
 			return n <= 0;
 		}
 
 		double v = n/d;
 		if (d > 0) {
 			if (v > t[1])
 				return false;
 		} else {
 			if (v < t[1])
 				t[1] = v;
 		}
 
 		return true;
 	}
 
 }
