 /* This file is part of LiveCG.
  *
  * Copyright (C) 2013  Sebastian Kuerten
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.topobyte.livecg.core.painting;
 
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Area;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.PathIterator;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.bric.geom.Clipper;
 
 import de.topobyte.livecg.core.geometry.geom.Chain;
 import de.topobyte.livecg.core.geometry.geom.Coordinate;
 import de.topobyte.livecg.core.geometry.geom.GeometryTransformer;
 import de.topobyte.livecg.core.geometry.geom.Polygon;
 import de.topobyte.livecg.core.lina.AffineTransformUtil;
 import de.topobyte.livecg.core.lina.AwtTransformUtil;
 import de.topobyte.livecg.core.lina.Matrix;
 import de.topobyte.livecg.util.CloneUtil;
 
 public class TikzPainter implements Painter
 {
 
 	final static Logger logger = LoggerFactory.getLogger(TikzPainter.class);
 
 	private StringBuilder buffer;
 	private double scale;
 
 	// Scaling to unity square
 	private AffineTransform atUnity;
 	private Matrix mxUnity;
 	private GeometryTransformer trUnity;
 
 	// Applying the user transform
 	private AffineTransform transform = null;
 	private Matrix mxTransform;
 	private GeometryTransformer trTransform;
 
 	// Safety rectangle to clip geometries with, to avoid latex errors
 	private Rectangle2D safetyRect = new Rectangle2D.Double(0, -1, 1, 1);
 	private Area safetyArea = new Area(safetyRect);
 
 	private String newline = "\n";
 
 	private Color color;
 	private double width = 1.0;
 	private float[] dash = null;
 	private float phase = 0;
 
 	public TikzPainter(StringBuilder buffer, double scale)
 	{
 		this.buffer = buffer;
 		this.scale = scale;
 
 		mxUnity = AffineTransformUtil.scale(scale, -scale);
 		trUnity = new GeometryTransformer(mxUnity);
 
 		atUnity = new AffineTransform();
 		atUnity.scale(scale, -scale);
 
 		buffer.append("\\clip (0,0) rectangle (1,-1);");
 		buffer.append(newline);
 	}
 
 	@Override
 	public void setColor(Color color)
 	{
 		this.color = color;
 	}
 
 	@Override
 	public void setStrokeWidth(double width)
 	{
 		this.width = width;
 	}
 
 	@Override
 	public void setStrokeNormal()
 	{
 		dash = null;
 		phase = 0;
 	}
 
 	@Override
 	public void setStrokeDash(float[] dash, float phase)
 	{
 		this.dash = dash;
 		this.phase = phase;
 	}
 
 	private String line()
 	{
 		return String.format("line width=%.5fmm", width / 5.0);
 	}
 
 	private Set<String> definedNames = new HashSet<String>();
 
 	private String appendColorDefine()
 	{
 		int rgb = color.getRGB();
 		String name = String.format("%06X", rgb);
 		if (!definedNames.contains(name) || true) {
 			definedNames.add(name);
 			double r = ((rgb & 0xff0000) >> 16) / 255.0;
 			double g = ((rgb & 0xff00) >> 8) / 255.0;
 			double b = (rgb & 0xff) / 255.0;
 			buffer.append(String.format("\\definecolor{" + name
 					+ "}{rgb}{%.5f,%.5f,%.5f}", r, g, b));
 			buffer.append(newline);
 		}
 		return name;
 	}
 
 	private String colorDefinition()
 	{
 		String c = appendColorDefine();
 		if (color.getAlpha() == 1.0) {
 			return "color=" + c;
 		}
 		return "color=" + c + ", opacity="
 				+ String.format("%.2f", color.getAlpha());
 	}
 
 	private void appendDraw()
 	{
 		String c = colorDefinition();
 		buffer.append("\\draw[" + line() + ", " + c + "] ");
 	}
 
 	private void appendFill()
 	{
 		String c = colorDefinition();
 		buffer.append("\\fill[" + c + "] ");
 	}
 
 	private void appendFillEvenOdd()
 	{
 		String c = colorDefinition();
 		buffer.append("\\fill[" + c + ", even odd rule] ");
 	}
 
 	private void append(Coordinate c)
 	{
 		append(buffer, c);
 	}
 
 	private void append(StringBuilder strb, Coordinate c)
 	{
 		strb.append(String.format("(%.5f,%.5f)", c.getX(), c.getY()));
 	}
 
 	private Coordinate applyTransforms(double x, double y)
 	{
 		return applyTransforms(new Coordinate(x, y));
 	}
 
 	private Coordinate applyTransforms(Coordinate c)
 	{
 		if (transform != null) {
 			c = trTransform.transform(c);
 		}
 		return trUnity.transform(c);
 	}
 
 	private Shape applyTransforms(Shape shape)
 	{
 		Shape s = shape;
 		if (transform != null) {
 			s = transform.createTransformedShape(s);
 		}
 		return atUnity.createTransformedShape(s);
 	}
 
 	private void appendRect(double x, double y, double width, double height)
 	{
 		Coordinate c1 = applyTransforms(x, y);
 		Coordinate c2 = applyTransforms(x + width, y + height);
 		buffer.append(String.format("(%.5f, %.5f) rectangle (%.5f, %.5f);",
 				c1.getX(), c1.getY(), c2.getX(), c2.getY()));
 		buffer.append(newline);
 	}
 
 	private void appendCircle(double x, double y, double radius)
 	{
 		Coordinate c = applyTransforms(x, y);
 		buffer.append(String.format("(%.5f,%.5f) circle (%.5f);", c.getX(),
 				c.getY(), radius * scale));
 		buffer.append(newline);
 	}
 
 	@Override
 	public void drawRect(int x, int y, int width, int height)
 	{
 		drawRect((double) x, (double) y, (double) width, (double) height);
 	}
 
 	@Override
 	public void drawRect(double x, double y, double width, double height)
 	{
 		appendDraw();
 		appendRect(x, y, width, height);
 	}
 
 	@Override
 	public void fillRect(int x, int y, int width, int height)
 	{
 		fillRect((double) x, (double) y, (double) width, (double) height);
 	}
 
 	@Override
 	public void fillRect(double x, double y, double width, double height)
 	{
 		appendFill();
 		appendRect(x, y, width, height);
 	}
 
 	@Override
 	public void drawLine(int x1, int y1, int x2, int y2)
 	{
 		drawLine((double) x1, (double) y1, (double) x2, (double) y2);
 	}
 
 	@Override
 	public void drawLine(double x1, double y1, double x2, double y2)
 	{
 		appendDraw();
 		append(applyTransforms(x1, y1));
 		buffer.append(" -- ");
 		append(applyTransforms(x2, y2));
 		buffer.append(";");
 		buffer.append(newline);
 	}
 
 	@Override
 	public void drawPath(List<Coordinate> points, boolean close)
 	{
 		appendDraw();
 		for (int i = 0; i < points.size(); i++) {
 			append(applyTransforms(points.get(i)));
 			if (i < points.size() - 1) {
 				buffer.append(" -- ");
 			}
 		}
 		if (close) {
 			buffer.append(" -- cycle");
 		}
		buffer.append(";");
		buffer.append(newline);
 	}
 
 	@Override
 	public void drawCircle(double x, double y, double radius)
 	{
 		appendDraw();
 		appendCircle(x, y, radius);
 	}
 
 	@Override
 	public void fillCircle(double x, double y, double radius)
 	{
 		appendFill();
 		appendCircle(x, y, radius);
 	}
 
 	private void appendChain(Chain chain)
 	{
 		for (int i = 0; i < chain.getNumberOfNodes(); i++) {
 			append(applyTransforms(chain.getCoordinate(i)));
 			if (i < chain.getNumberOfNodes() - 1) {
 				buffer.append(" -- ");
 			}
 		}
 		if (chain.isClosed()) {
 			buffer.append(" -- cycle");
 		}
 	}
 
 	@Override
 	public void drawChain(Chain chain)
 	{
 		appendDraw();
 		appendChain(chain);
 		buffer.append(";");
 		buffer.append(newline);
 	}
 
 	@Override
 	public void drawPolygon(Polygon polygon)
 	{
 		appendDraw();
 		Chain shell = polygon.getShell();
 		appendChain(shell);
 		for (Chain hole : polygon.getHoles()) {
 			buffer.append(" ");
 			appendChain(hole);
 		}
 		buffer.append(";");
 		buffer.append(newline);
 	}
 
 	@Override
 	public void fillPolygon(Polygon polygon)
 	{
 		appendFillEvenOdd();
 		Chain shell = polygon.getShell();
 		appendChain(shell);
 		for (Chain hole : polygon.getHoles()) {
 			buffer.append(" ");
 			appendChain(hole);
 		}
 		buffer.append(";");
 		buffer.append(newline);
 	}
 
 	private void appendClipScopeBegin()
 	{
 		if (clipShapes != null && !clipShapes.isEmpty()) {
 			appendScopeBegin();
 			appendClip();
 		}
 	}
 
 	private void appendClipScopeEnd()
 	{
 		if (clipShapes != null && !clipShapes.isEmpty()) {
 			appendScopeEnd();
 		}
 	}
 
 	private void appendScopeBegin()
 	{
 		buffer.append("\\begin{scope}");
 		buffer.append(newline);
 	}
 
 	private void appendScopeEnd()
 	{
 		buffer.append("\\end{scope}");
 		buffer.append(newline);
 	}
 
 	private void appendClip()
 	{
 		if (clipShapes.isEmpty()) {
 			return;
 		}
 		for (int i = 0; i < clipShapes.size(); i++) {
 			Shape shape = clipShapes.get(i);
 			StringBuilder buf = buildPath(atUnity.createTransformedShape(shape));
 			buffer.append("\\clip ");
 			buffer.append(buf.toString());
 			buffer.append(";");
 			buffer.append(newline);
 		}
 	}
 
 	@Override
 	public void draw(Shape shape)
 	{
 		Shape tshape = applyTransforms(shape);
 
 		appendClipScopeBegin();
 
 		appendDraw();
 		StringBuilder path = buildPath(tshape);
 		buffer.append(path.toString());
 		buffer.append(";");
 		buffer.append(newline);
 
 		appendClipScopeEnd();
 	}
 
 	@Override
 	public void fill(Shape shape)
 	{
 		Shape tshape = applyTransforms(shape);
 
 		Area area = new Area(tshape);
 		area.intersect(safetyArea);
 
 		appendClipScopeBegin();
 
 		appendFillEvenOdd();
 		StringBuilder path = buildPath(area);
 		buffer.append(path.toString());
 		buffer.append(";");
 		buffer.append(newline);
 
 		appendClipScopeEnd();
 	}
 
 	/*
 	 * Transformations
 	 */
 
 	@Override
 	public AffineTransform getTransform()
 	{
 		if (transform == null) {
 			return new AffineTransform();
 		}
 		return new AffineTransform(transform);
 	}
 
 	@Override
 	public void setTransform(AffineTransform t)
 	{
 		transform = t;
 		mxTransform = AwtTransformUtil.convert(t);
 		trTransform = new GeometryTransformer(mxTransform);
 	}
 
 	/*
 	 * Clipping
 	 */
 
 	private List<Shape> clipShapes = null;
 
 	@Override
 	public Object getClip()
 	{
 		if (clipShapes == null) {
 			return null;
 		}
 		return CloneUtil.clone(clipShapes);
 	}
 
 	@Override
 	public void setClip(Object clip)
 	{
 		if (clip == null) {
 			clipShapes = null;
 		}
 		clipShapes = CloneUtil.clone((List<Shape>) clip);
 	}
 
 	@Override
 	public void clipRect(double x, double y, double width, double height)
 	{
 		clipArea(new Rectangle2D.Double(x, y, width, height));
 	}
 
 	@Override
 	public void clipArea(Shape shape)
 	{
 		if (clipShapes == null) {
 			clipShapes = new ArrayList<Shape>();
 		}
 		clipShapes.add(shape);
 	}
 
 	/*
 	 * TODO: these are not yet implemented
 	 */
 
 	@Override
 	public void drawString(String text, double x, double y)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void drawImage(BufferedImage image, int x, int y)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * Shapes, paths
 	 */
 
 	private StringBuilder buildPath(Shape shape)
 	{
 		StringBuilder strb = new StringBuilder();
 
 		GeneralPath clipped = Clipper.clipToRect(shape, safetyRect);
 
 		PathIterator pathIterator = clipped
 				.getPathIterator(new AffineTransform());
 		while (!pathIterator.isDone()) {
 			double[] coords = new double[6];
 			int type = pathIterator.currentSegment(coords);
 			pathIterator.next();
 
 			switch (type) {
 			case PathIterator.SEG_MOVETO:
 				double cx = coords[0];
 				double cy = coords[1];
 				pathMoveTo(strb, cx, cy);
 				break;
 			case PathIterator.SEG_LINETO:
 				cx = coords[0];
 				cy = coords[1];
 				pathLineTo(strb, cx, cy);
 				break;
 			case PathIterator.SEG_CLOSE:
 				pathClose(strb);
 				break;
 			case PathIterator.SEG_QUADTO:
 				cx = coords[2];
 				cy = coords[3];
 				double c1x = coords[0];
 				double c1y = coords[1];
 				pathQuadraticTo(strb, c1x, c1y, cx, cy);
 				break;
 			case PathIterator.SEG_CUBICTO:
 				cx = coords[4];
 				cy = coords[5];
 				c1x = coords[0];
 				c1y = coords[1];
 				double c2x = coords[2];
 				double c2y = coords[3];
 				pathCubicTo(strb, c1x, c1y, c2x, c2y, cx, cy);
 				break;
 			default:
 				logger.error("Not implemented! PathIterator type: " + type);
 			}
 		}
 		return strb;
 	}
 
 	private void pathMoveTo(StringBuilder strb, double cx, double cy)
 	{
 		strb.append(" ");
 		append(strb, new Coordinate(cx, cy));
 	}
 
 	private void pathLineTo(StringBuilder strb, double cx, double cy)
 	{
 		strb.append(" -- ");
 		append(strb, new Coordinate(cx, cy));
 	}
 
 	private void pathClose(StringBuilder strb)
 	{
 		strb.append(" -- ");
 		strb.append("cycle");
 	}
 
 	private void pathQuadraticTo(StringBuilder strb, double c1x, double c1y,
 			double cx, double cy)
 	{
 		strb.append(" .. controls ");
 		append(strb, new Coordinate(c1x, c1y));
 		strb.append(" .. ");
 		append(strb, new Coordinate(cx, cy));
 	}
 
 	private void pathCubicTo(StringBuilder strb, double c1x, double c1y,
 			double c2x, double c2y, double cx, double cy)
 	{
 		strb.append(" .. controls ");
 		append(strb, new Coordinate(c1x, c1y));
 		strb.append(" and ");
 		append(strb, new Coordinate(c2x, c2y));
 		strb.append(" .. ");
 		append(strb, new Coordinate(cx, cy));
 	}
 
 }
