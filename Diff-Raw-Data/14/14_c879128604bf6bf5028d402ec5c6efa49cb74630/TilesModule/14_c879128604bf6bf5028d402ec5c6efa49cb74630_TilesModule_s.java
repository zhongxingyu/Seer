 /*
 	Copyright (C) 2009 by Peter H. ("TroY")
 
 	This program is free software; you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation; either version 2 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful, but
 	WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 	General Public License for more details.
 */
 
 package troy.procedural;
 
 import artofillusion.procedural.*;
 import artofillusion.ui.*;
 import java.awt.*;
 
 /**
  * Generate tiles.
  *
  * Based on various class by Peter Eastman, such as NumberModule.java
  * and TransformModule.java
  *
  * @author TroY
  */
 public class TilesModule extends Module
 {
 	protected PointInfo point = null;
 
 	public TilesModule()
 	{
 		this(new Point());
 	}
 
 	public TilesModule(Point position)
 	{
 		super(Translate.text("tilesmodule:caption.module"),
 		new IOPort [] {
 			new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT,
 				new String [] {"X", "(X)"}),
 			new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT,
 				new String [] {"Y", "(Y)"}),
 			new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT,
 				new String [] {"Z", "(Z)"}),
 			new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.BOTTOM,
 				new String [] {
 					Translate.text("tilesmodule:caption.width"),
 					"(0.1)"}),
 			new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.BOTTOM,
 				new String [] {
 					Translate.text("tilesmodule:caption.scale"),
 					"(4.0)"}),
 		},
 		new IOPort [] {
 			new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT,
 				new String [] {
 					Translate.text("tilesmodule:caption.output"),
 					"(0.0)"}),
 		},
 		position);
 	}
 
 	/** New point, so the value will need to be recalculated. */
 	@Override
 	public void init(PointInfo p)
 	{
 		point = p;
 	}
 
 	/** Tiles-Generator. */
 	@Override
 	public double getAverageValue(int which, double blur)
 	{
 		if (point == null)
 			return 0.0;
 
 		// Parameters
        double width = (linkFrom[3] == null) ? 0.1
 			: linkFrom[3].getAverageValue(linkFromIndex[3], blur);
 		width = (width <= 0.0 ? 0.1 : width);
 		width *= 0.5;
 
        double scale = (linkFrom[4] == null) ? 4.0
 			: linkFrom[4].getAverageValue(linkFromIndex[4], blur);
 		scale = (scale <= 0.0 ? 4.0 : scale);
 		scale *= 0.5;
 
 		// Position
        double x = (linkFrom[0] == null) ? point.x
 			: linkFrom[0].getAverageValue(linkFromIndex[0], blur);
        double y = (linkFrom[1] == null) ? point.y
 			: linkFrom[1].getAverageValue(linkFromIndex[1], blur);
        double z = (linkFrom[2] == null) ? point.z
 			: linkFrom[2].getAverageValue(linkFromIndex[2], blur);
 
 		// Scale it (before the shift).
 		x *= scale;
 		y *= scale;
 		z *= scale;
 
 		// Shift it a bit to get a better "default" experience
 		// (otherwise the preview cube would be all white).
 		x += 0.5;
 		y += 0.5;
 		z += 0.5;
 
 		// See if the point is close enough to one plane.
 		if (Math.abs(x - Math.floor(x)) <= width)
 			return 1.0;
 		if (Math.abs(y - Math.floor(y)) <= width)
 			return 1.0;
 		if (Math.abs(z - Math.floor(z)) <= width)
 			return 1.0;
 
 		return 0.0;
 	}
 }
