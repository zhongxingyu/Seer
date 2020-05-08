 /*
  
  RepRap
  ------
  
  The Replicating Rapid Prototyper Project
  
  
  Copyright (C) 2005
  Adrian Bowyer & The University of Bath
  
  http://reprap.org
  
  Principal author:
  
  Adrian Bowyer
  Department of Mechanical Engineering
  Faculty of Engineering and Design
  University of Bath
  Bath BA2 7AY
  U.K.
  
  e-mail: A.Bowyer@bath.ac.uk
  
  RepRap is free; you can redistribute it and/or
  modify it under the terms of the GNU Library General Public
  Licence as published by the Free Software Foundation; either
  version 2 of the Licence, or (at your option) any later version.
  
  RepRap is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Library General Public Licence for more details.
  
  For this purpose the words "software" and "library" in the GNU Library
  General Public Licence are taken to mean any and all computer programs
  computer files data results documents and other copyright information
  available from the RepRap project.
  
  You should have received a copy of the GNU Library General Public
  Licence along with RepRap; if not, write to the Free
  Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA,
  or see
  
  http://www.gnu.org/
  
  =====================================================================
  
  
  RrPolygon: 2D polygons
  
  First version 20 May 2005
  This version: 1 May 2006 (Now in CVS - no more comments here)
  
  A polygon is an auto-extending list of Rr2Points.  Its end is 
  sometimes considered to join back to its beginning, depending
  on context.
  
  It also keeps its enclosing box.  
  
  Each point is stored with a flag value.  This can be used to flag the
  point as visited, or to indicate if the subsequent line segment is to
  be plotted etc.
  
  java.awt.Polygon is no use for this because it has integer coordinates.
  
  */
 
 package org.reprap.geometry.polygons;
 
 import java.io.*;
 import java.util.*;
 import org.reprap.geometry.LayerProducer;
 
 /**
  * The main boundary-representation polygon class
  */
 public class RrPolygon
 {
 	public List points;
 	public List flags;
 	public RrBox box;
 	
 	/**
 	 * Empty polygon
 	 */
 	public RrPolygon()
 	{
 		points = new ArrayList();
 		flags = new ArrayList();
 		box = new RrBox();
 	}
 	
 	// Get the data
 	
 	public Rr2Point point(int i)
 	{
 		return new Rr2Point((Rr2Point)points.get(i));
 	}
 	public int flag(int i)
 	{
 		return ((Integer)flags.get(i)).intValue();
 	}
 	
 	/**
 	 * As a string
 	 * @return
 	 */
 	public String toString()
 	{
 		String result = " Polygon -  vertices: ";
 		result += size() + ", enclosing box: ";
 		result += box.toString();
 		result += "\n";
 		for(int i = 0; i < size(); i++)
 			result += point(i).toString();
 		
 		return result;
 	}
 	
 	/**
 	 * Change a flag value
 	 * @param i
 	 * @param f
 	 */
 	public void flag(int i, int f)
 	{
 		flags.set(i, new Integer(f));
 	}
 	
 	/**
 	 * Length
 	 * @return
 	 */
 	public int size()
 	{
 		return points.size();
 	}
 	
 	/**
 	 * Deep copy
 	 * @param p
 	 */
 	public RrPolygon(RrPolygon p)
 	{
 		points = new ArrayList();
 		flags = new ArrayList();
 		box = new RrBox(p.box);
 		for(int i = 0; i < p.size(); i++)
 		{
 			points.add(new Rr2Point(p.point(i)));
 			flags.add(new Integer((p.flag(i)))); 
 		}		
 	}
 	
 	
 	/**
 	 * Add a new point and its flag value to the polygon
 	 * @param p
 	 * @param f
 	 */
 	public void add(Rr2Point p, int f)
 	{
 		points.add(new Rr2Point(p));
 		flags.add(new Integer(f));
 		box.expand(p);
 	}
 	
 	/**
 	 * Put a new polygon and its flag values on the end
 	 * @param p
 	 */
 	public void add(RrPolygon p)
 	{
 		if(p.size() == 0)
 			return;
 		for(int i = 0; i < p.size(); i++)
 		{
 			points.add(new Rr2Point(p.point(i)));
 			flags.add(new Integer(p.flag(i))); 
 		}
 		box.expand(p.box);
 	}
 	
 	
 	/**
 	 * Remove a point.
 	 * N.B. This does not ammend the enclosing box
 	 * @param i
 	 */
 	public void remove(int i)
 	{
 		points.remove(i);
 		flags.remove(i);
 	}
 	
 	/**
 	 * Recompute the box (sometimes useful if points have been deleted) 
 	 */
 	public void re_box()
 	{
 		box = new RrBox();
 		int leng = size();
 		for(int i = 0; i < leng; i++)
 		{
 			box.expand((Rr2Point)points.get(i)); 
 		}
 	}
 	
 	
 	/**
 	 * Output the polygon in SVG XML format
 	 * @param opf
 	 */
 	public void svg(PrintStream opf)
 	{
 		opf.println("<polygon points=\"");
 		int leng = size();
 		for(int i = 0; i < leng; i++)
 			opf.println(Double.toString((point(i)).x()) + "," 
 					+ Double.toString((point(i)).y()));
 		opf.println("\" />");
 	}
 		
 	/**
 	 * Negate (i.e. reverse cyclic order)
 	 * @return
 	 */
 	public RrPolygon negate()
 	{
 		RrPolygon result = new RrPolygon();
 		for(int i = size() - 1; i >= 0; i--)
 			result.add(point(i), flag(i));
 		return result;
 	}
 	
 	/**
 	 * Signed area (-ve result means polygon goes anti-clockwise)
 	 * @return
 	 */
 	public double area()
 	{
 		double a = 0;
 		Rr2Point p, q;
 		int j;
 		for(int i = 1; i < size() - 1; i++)
 		{
 			j = i + 1;
 			p = Rr2Point.sub(point(i), point(0));
 			q = Rr2Point.sub(point(j), point(0));
 			a += Rr2Point.op(q, p);
 		} 
 		return a*0.5;
 	}
 	
 	/**
 	 * Backtrack a given distance, inserting a new point there and returning its index
 	 * @param d
 	 * @return
 	 */
 	public int backStep(double d)
 	{
 		Rr2Point last, p;
 		int start = size() - 1;
 		if(flag(0) != LayerProducer.gapMaterial())
 			last = point(0);
 		else
 		{
 			last = point(start);
 			start--;
 		}
 		double sum = 0;
 		for(int i = start; i >= 0; i--)
 		{
 			p = point(i);
 			sum += Math.sqrt(Rr2Point.d_2(p, last));
 			if(sum > d)
 			{
 				sum = sum - d;
 				p = Rr2Point.sub(last, p);
 				sum = sum/p.mod();
 				p = Rr2Point.add(point(i), Rr2Point.mul(sum, p));
 				int j = i + 1;
 				if(j < size())
 				{
 					points.add(j, p);
 					flags.add(j, flags.get(i));
 				} else
 				{
 					points.add(p);
 					flags.add(flags.get(i));					
 				}
 				return(j);
 			}
 			last = p;
 		}
 		return 0;
 	}
 	
 	private int findAngleStart(int v1, double d2)
 	{
 		int leng = size();
 		Rr2Point p1 = point(v1%leng);
 		int v2 = v1;
 		for(int i = 0; i <= leng; i++)
 		{
 			v2++;
 			RrLine line = new RrLine(p1, point(v2%leng));
 			for (int j = v1+1; j < v2; j++)
 			{
 				if (line.d_2(point(j%leng)).x() > d2)
 					return v2 - 1;
 			}	
 		}
 		System.err.println("RrPolygon.findAngleStart(): polygon is all one straight line!");
 		return 0;
 	}
 	
 	/**
 	 * Simplify a polygon by deleting points from it that
 	 * are closer than d to lines joining other points
 	 * @param d
 	 * @return
 	 */
 	public RrPolygon simplify(double d)
 	{
 		int leng = size();
 		if(leng <= 3)
 			return new RrPolygon(this);
 		RrPolygon r = new RrPolygon();
 		double d2 = d*d;
 		int v1 = findAngleStart(0, d2);
 		r.add(point(v1%leng), flag(v1%leng));
 		int v2 = v1;
 		while(true)
 		{
 			v2 = findAngleStart(v2, d2);
 			if(v2 > leng)
 				return(r);
 			r.add(point(v2%leng), flag(v2%leng));
 		}
 	}
 	
 	/**
	 * Remove solitary edges that are shorter than tiny from the
	 *   polygon if they are preceeded and followed by gap material.
 	 * @param tiny
 	 * @param fg
 	 * @return
 	 */
 	public RrPolygon filterShort(double tiny)
 	{
 		RrPolygon r = new RrPolygon();
 		int oldEdgeFlag = flag(size()-1);
		int i, ii;
 		
 		for(i = 1; i <= size(); i++)
 		{
			ii = i%size();
			if(oldEdgeFlag == LayerProducer.gapMaterial() && flag(ii) == LayerProducer.gapMaterial())
 			{
				double d = Rr2Point.sub(point(ii), point(i - 1)).mod();
 				if(d > tiny)
 					r.add(point(i - 1), flag(i - 1));
 				else
 					System.out.println("Tiny edge removed.");
 			} else
 				r.add(point(i - 1), flag(i - 1));
 			oldEdgeFlag = flag(i - 1);
 		}
 		
 		// Anything left?
 		
 		for(i = 0; i < r.size(); i++)
 		{
 			if(r.flag(i) != LayerProducer.gapMaterial())
 				return r;
 		}
 		
 		// Nothing left
 		
 		return new RrPolygon();
 	}
 }
 
 
