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
 import artofillusion.math.*;
 import java.util.Random;
 import java.awt.*;
 import java.security.*;
 
 /**
  * Generate one random value per unit cube.
  *
  * Based on various class by Peter Eastman, such as NumberModule.java
  * and TransformModule.java
  *
  * @author TroY
  */
 public class PixelNoiseModule extends Module
 {
 	protected PointInfo point = null;
 	protected MessageDigest md = null;
 	protected byte[] sha1hash = new byte[40];
 
 	public PixelNoiseModule()
 	{
 		this(new Point());
 	}
 
 	public PixelNoiseModule(Point position)
 	{
 		super(Translate.text("pixelnoisemodule:caption.module"),
 		new IOPort [] {
 			new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT,
 				new String [] {"X", "(X)"}),
 			new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT,
 				new String [] {"Y", "(Y)"}),
 			new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT,
 				new String [] {"Z", "(Z)"}),
 			new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.BOTTOM,
 				new String [] {
 					Translate.text("pixelnoisemodule:caption.scale"),
 					"(4.0)"}),
 		},
 		new IOPort [] {
 			new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT,
 				new String [] {
 					Translate.text("pixelnoisemodule:caption.output"),
 					"(0.0)"}),
 		},
 		position);
 
 		// Init hash function
 		try
 		{
 			md = MessageDigest.getInstance("SHA-1");
 		}
 		catch (Exception e)
 		{
 			md = null;
 		}
 	}
 
 	/** New point, so the value will need to be recalculated. */
 	@Override
 	public void init(PointInfo p)
 	{
 		point = p;
 	}
 
 	/** Noise-Generator. */
 	@Override
 	public double getAverageValue(int which, double blur)
 	{
 		if (point == null)
 			return 0.0;
 
 		// Parameters
		double scale = (linkFrom[3] == null) ? 4.0
 			: linkFrom[3].getAverageValue(linkFromIndex[3], blur);
 		scale = (scale <= 0.0 ? 4.0 : scale);
		scale *= 0.5;

		// Decide in which unit cube we are, this is done via Math.ceil.
		double dx = (linkFrom[0] == null) ? point.x
			: linkFrom[0].getAverageValue(linkFromIndex[0], blur);
		double dy = (linkFrom[1] == null) ? point.y
			: linkFrom[1].getAverageValue(linkFromIndex[1], blur);
		double dz = (linkFrom[2] == null) ? point.z
			: linkFrom[2].getAverageValue(linkFromIndex[2], blur);

		dx *= scale;
		dy *= scale;
		dz *= scale;

		long x = (long)Math.ceil(dx);
		long y = (long)Math.ceil(dy);
		long z = (long)Math.ceil(dz);
 
 		// So, how do we get a random value that doesn't change for
 		// every run? --> Use the position as a random seed.
 
 		// However, java.util.Random generates very similar numbers if
 		// the seed doesn't differ too much. So we actually need a hash
 		// function here: Different results for slightly different input
 		// while always generating the same result for the same input.
 
 		// I'm very sure there's a better method to do this. Feel free
 		// to tell me! :)
 
 		// Concat'ing all the positions as strings will yield a unique
 		// string for every pixel.
 		return doubleHash(
 				Long.toString(x)
 				+ Long.toString(y)
 				+ Long.toString(z));
 	}
 
 	/** Hash a string and return some of the hash's bytes. */
 	protected double doubleHash(String str)
 	{
 		if (md == null)
 			return 0.0;
 
 		// Hash it!
 		md.update(str.getBytes(), 0, str.length());
 		sha1hash = md.digest();
 
 		// Get some bytes from the hash as a double value.
 		double out = 0.0;
 		double divbase = 256;
 		double maxval = 1;
 		for (int i = 0; i < 4; i++)
 		{
 			// Get the proper integer value for this byte.
 			int intval = (int)sha1hash[i];
 			intval = (intval < 0 ? 256 + intval : intval);
 			intval <<= (i * 8);
 
 			// Add it and keep track of what's the maximum possible
 			// value.
 			out += intval;
 			maxval *= divbase;
 		}
 
 		// Only do positive numbers and break them down to the interval
 		// (0, 1].
 		out = (out < 0.0 ? -out : out);
 		out /= maxval;
 		return out;
 	}
 }
