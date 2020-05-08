 /*
 * Copyright (c) 2007 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Dmitry Stadnik (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.examples.taipan.figures;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Insets;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.swt.graphics.Color;
 
 /**
  * @author dstadnik
  */
 public class RoseFigure extends Shape {
 
 	private int bladeWidth;
 
 	private int bladeHeight;
 
 	private Color bladeDarkColour;
 
 	private Color bladeLightColour;
 
 	public RoseFigure() {
 		this(7, 30);
 	}
 
 	public RoseFigure(int bladeWidth, int bladeHeight) {
 		this.bladeWidth = bladeWidth;
 		this.bladeHeight = bladeHeight;
 	}
 
 	public Color getBladeDarkColour() {
 		return bladeDarkColour;
 	}
 
 	public void setBladeDarkColour(Color bladeDarkColour) {
 		this.bladeDarkColour = bladeDarkColour;
 	}
 
 	public Color getBladeLightColour() {
 		return bladeLightColour;
 	}
 
 	public void setBladeLightColour(Color bladeLightColour) {
 		this.bladeLightColour = bladeLightColour;
 	}
 
 	public Dimension getMinimumSize(int wHint, int hHint) {
 		Insets i = getInsets();
 		int s = 2 * (bladeWidth + bladeHeight);
 		return new Dimension(s, s).expand(i.getWidth(), i.getHeight());
 	}
 
 	public Dimension getMaximumSize(int wHint, int hHint) {
 		Insets i = getInsets();
 		int s = 2 * (bladeWidth + bladeHeight);
 		return new Dimension(s, s).expand(i.getWidth(), i.getHeight());
 	}
 
 	public Dimension getPreferredSize(int wHint, int hHint) {
 		Insets i = getInsets();
 		int s = 2 * (bladeWidth + bladeHeight);
 		return new Dimension(s, s).expand(i.getWidth(), i.getHeight());
 	}
 
 	protected void outlineShape(Graphics g) {
 		Point c = getClientArea().getCenter();
 		for (PointList pl : getLeftBlades(c)) {
 			g.drawPolygon(pl);
 		}
 		for (PointList pl : getRightBlades(c)) {
 			g.drawPolygon(pl);
 		}
 	}
 
 	protected void fillShape(Graphics g) {
 		Point c = getClientArea().getCenter();
 		g.setBackgroundColor(bladeDarkColour != null ? bladeDarkColour : ColorConstants.black);
 		for (PointList pl : getLeftBlades(c)) {
 			g.fillPolygon(pl);
 		}
 		g.setBackgroundColor(bladeLightColour != null ? bladeLightColour : ColorConstants.white);
 		for (PointList pl : getRightBlades(c)) {
 			g.fillPolygon(pl);
 		}
 	}
 
 	protected PointList[] getLeftBlades(Point c) {
 		PointList[] blades = new PointList[4];
 		{
 			PointList pl = new PointList(3);
 			pl.addPoint(c);
 			pl.addPoint(c.x - bladeWidth, c.y - bladeWidth);
 			pl.addPoint(c.x, c.y - bladeWidth - bladeHeight);
 			blades[0] = pl;
 		}
 		{
 			PointList pl = new PointList(3);
 			pl.addPoint(c);
 			pl.addPoint(c.x + bladeWidth, c.y - bladeWidth);
 			pl.addPoint(c.x + bladeWidth + bladeHeight, c.y);
 			blades[1] = pl;
 		}
 		{
 			PointList pl = new PointList(3);
 			pl.addPoint(c);
 			pl.addPoint(c.x + bladeWidth, c.y + bladeWidth);
 			pl.addPoint(c.x, c.y + bladeWidth + bladeHeight);
 			blades[2] = pl;
 		}
 		{
 			PointList pl = new PointList(3);
 			pl.addPoint(c);
 			pl.addPoint(c.x - bladeWidth, c.y + bladeWidth);
 			pl.addPoint(c.x - bladeWidth - bladeHeight, c.y);
 			blades[3] = pl;
 		}
 		return blades;
 	}
 
 	protected PointList[] getRightBlades(Point c) {
 		PointList[] blades = new PointList[4];
 		{
 			PointList pl = new PointList(3);
 			pl.addPoint(c);
 			pl.addPoint(c.x + bladeWidth, c.y - bladeWidth);
 			pl.addPoint(c.x, c.y - bladeWidth - bladeHeight);
 			blades[0] = pl;
 		}
 		{
 			PointList pl = new PointList(3);
 			pl.addPoint(c);
 			pl.addPoint(c.x + bladeWidth, c.y + bladeWidth);
 			pl.addPoint(c.x + bladeWidth + bladeHeight, c.y);
 			blades[1] = pl;
 		}
 		{
 			PointList pl = new PointList(3);
 			pl.addPoint(c);
 			pl.addPoint(c.x - bladeWidth, c.y + bladeWidth);
 			pl.addPoint(c.x, c.y + bladeWidth + bladeHeight);
 			blades[2] = pl;
 		}
 		{
 			PointList pl = new PointList(3);
 			pl.addPoint(c);
 			pl.addPoint(c.x - bladeWidth, c.y - bladeWidth);
 			pl.addPoint(c.x - bladeWidth - bladeHeight, c.y);
 			blades[3] = pl;
 		}
 		return blades;
 	}
 }
