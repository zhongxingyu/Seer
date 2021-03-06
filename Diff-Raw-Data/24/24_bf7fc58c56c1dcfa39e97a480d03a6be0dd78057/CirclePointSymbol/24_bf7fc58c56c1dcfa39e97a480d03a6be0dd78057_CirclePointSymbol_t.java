 /*
  * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
  * This cross-platform GIS is developed at French IRSTV institute and is able
  * to manipulate and create vector and raster spatial information. OrbisGIS
  * is distributed under GPL 3 license. It is produced  by the geo-informatic team of
  * the IRSTV Institute <http://www.irstv.cnrs.fr/>, CNRS FR 2488:
  *    Erwan BOCHER, scientific researcher,
  *    Thomas LEDUC, scientific researcher,
  *    Fernando GONZALEZ CORTES, computer engineer.
  *
  * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
  *
  * This file is part of OrbisGIS.
  *
  * OrbisGIS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * OrbisGIS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
  *
  * For more information, please consult:
  *    <http://orbisgis.cerma.archi.fr/>
  *    <http://sourcesup.cru.fr/projects/orbisgis/>
  *
  * or contact directly:
  *    erwan.bocher _at_ ec-nantes.fr
  *    fergonco _at_ gmail.com
  *    thomas.leduc _at_ cerma.archi.fr
  */
 package org.orbisgis.renderer.symbol;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.NoninvertibleTransformException;
 import java.awt.geom.Point2D;
 
 import org.gdms.driver.DriverException;
 import org.orbisgis.renderer.RenderPermission;
 
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.Point;
 
 public class CirclePointSymbol extends AbstractPointSymbol {
 
 	CirclePointSymbol(Color outline, int lineWidth, Color fillColor, int size,
 			boolean mapUnits) {
 		super(outline, lineWidth, fillColor, size, mapUnits);
 	}
 
 	public Symbol cloneSymbol() {
 		return new CirclePointSymbol(outline, lineWidth, fillColor, size,
 				mapUnits);
 	}
 
	protected void paintCircle(Graphics2D g, double x, double y, double size) {
 		x = x - size / 2;
 		y = y - size / 2;
 		if (fillColor != null) {
 			g.setPaint(fillColor);
			g.fillOval((int) x, (int) y, (int) size, (int) size);
 		}
 		if (outline != null) {
 			g.setStroke(new BasicStroke(lineWidth));
 			g.setColor(outline);
			g.drawOval((int) x, (int) y, (int) size, (int) size);
 		}
 	}
 
 	public Envelope draw(Graphics2D g, Geometry geom, AffineTransform at,
 			RenderPermission permission) throws DriverException {
 
 		Point point = geom.getCentroid();
 		Point2D p = new Point2D.Double(point.getX(), point.getY());
 		p = at.transform(p, null);
		double drawingSize = size;
 		if (mapUnits) {
 			try {
 				drawingSize = (int) toPixelUnits(size, at);
 			} catch (NoninvertibleTransformException e) {
 				throw new DriverException("Cannot convert to map units", e);
 			}
 		}
		paintCircle(g, p.getX(), p.getY(), drawingSize);
 
 		return null;
 	}
 
 	public String getClassName() {
 		return "Circle in point";
 	}
 
 	public String getId() {
 		return "org.orbisgis.symbol.point.Circle";
 	}
 
 }
