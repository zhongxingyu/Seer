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
 
 RrCSGPolygon: 2D polygons as boolean combinations of half-planes,
 together with spatial quad tree and other tools
 
 First version 14 November 2005
 
 */
 
 
 package org.reprap.geometry.polygons;
 
 import java.util.*;
 
 
 /**
  * Small class for containing results of hatch searches
  */
 class RrHSearch
 {
 	public boolean join;
 	public boolean notFinished;
 	public int theLine;
 	public int thePoint;
 	public double dsq;
 }
 
 
 /**
  * Polygons as CSG combinations of half spaces with recursive quad-tree
  * division of their containing boxes.
  */
 public class RrCSGPolygon
 {
 	private RrCSG csg;            ///< The polygon
 	private RrBox box;            ///< Its enclosing box
 	private RrCSGPolygon q1,      ///< Quad tree division - NW
 	q2,           ///< NE 
 	q3,           ///< SE
 	q4;           ///< SW
 	private double resolution_2;  ///< Squared diagonal of the smallest box to go to
 	private boolean visit1, visit2; /// Used by the edge-generation software.
 	private double sFactor;       /// Swell factor for division
 	/**
 	 * Set one up
 	 * @param p
 	 * @param bx
 	 */
 	public RrCSGPolygon(RrCSG p, RrBox bx)
 	{
 		csg = p;
 		box = new RrBox(bx);
 		q1 = null;
 		q2 = null;
 		q3 = null;
 		q4 = null;
 		resolution_2 = box.d_2()*1.0e-8;  // Default - set properly
 		visit1 = false;
 		visit2 = false;
 		sFactor = 1;
 	}
 	
 	/**
 	 * Deep copy
 	 * @param p
 	 */
 	public RrCSGPolygon(RrCSGPolygon p)
 	{
 		csg = new RrCSG(p.csg);
 		box = new RrBox(p.box);
 		if(p.q1 != null)
 		{
 			q1 = new RrCSGPolygon(p.q1);
 			q2 = new RrCSGPolygon(p.q2);
 			q3 = new RrCSGPolygon(p.q3);
 			q4 = new RrCSGPolygon(p.q4);
 			resolution_2 = p.resolution_2;
 		} else
 		{
 			q1 = null;
 			q2 = null;
 			q3 = null;
 			q4 = null;
 			resolution_2 = p.resolution_2;
 		}
 
 		visit1 = p.visit1;
 		visit2 = p.visit2;
 		sFactor = p.sFactor;
 	}
 	
 	// get children etc
 	
 	public RrCSGPolygon c_1() { return q1; }
 	public RrCSGPolygon c_2() { return q2; }
 	public RrCSGPolygon c_3() { return q3; }
 	public RrCSGPolygon c_4() { return q4; }
 	public RrCSG csg() { return csg; }
 	public RrBox box() { return box; }
 	public double resolution() { return resolution_2; }
 	public double swell() { return sFactor; }
 	
 	
 	/**
 	 * Convert to a string - internal recursive call
 	 * @param quad
 	 */
 	private String toString_r(String quad)
 	{
 		if(csg.operator() == RrCSGOp.UNIVERSE)
 			quad = quad + "U";
 		else
 			quad = quad + Integer.toString(csg.complexity());
 		
 		if(q1 == null)
 		{
 			String result = quad + "\n";
 			return result;
 		} else
 		{
 			return(q1.toString_r(quad + ":NW-") + 
 					q2.toString_r(quad + ":NE-") +
 					q3.toString_r(quad + ":SE-") +
 					q4.toString_r(quad + ":SW-"));
 		}      
 	}
 	
 	/**
 	 * Convert to a string
 	 */	
 	public String toString()
 	{
 		return "RrCSGPolygon\n" + toString_r(":-");
 	}
 	
 	/**
 	 * Quad-tree division
 	 * @param res_2
 	 * @param swell
 	 */
 	public void divide(double res_2, double swell)
 	{
 		resolution_2 = res_2;
 		sFactor = swell;
 		
 		// Anything as simple as a single corner, do nothing
 		
 		if(csg.complexity() < 3) 
 			return;
 		
 		// Too small a box?
 		
 		if(box.d_2() < resolution_2)
 			return;
 		
 		// For comlexities of 4 or less, check if regularization throws
 		// some away.
 		
 		if(csg.complexity() < 5)
 		{
 			csg = csg.regularise();
 			if(csg.complexity() < 3)
 				return;
 		}
 		
 		// Set up the quad-tree division
 		
 		Rr2Point sw = box.sw();
 		Rr2Point nw = box.nw();
 		Rr2Point ne = box.ne();
 		Rr2Point se = box.se();
 		Rr2Point cen = box.centre();
 		
 		// Prune the set to the four boxes, and put the results in the children
 		
 		RrBox s = new RrBox(Rr2Point.mul(Rr2Point.add(sw, nw), 0.5), 
 				Rr2Point.mul(Rr2Point.add(nw, ne), 0.5));
 		s = s.scale(sFactor);
 		q1 = new RrCSGPolygon(csg.prune(s), s);
 		
 		s = new RrBox(cen, ne);
 		s = s.scale(sFactor);
 		q2 = new RrCSGPolygon(csg.prune(s), s);
 		
 		s = new RrBox(Rr2Point.mul(Rr2Point.add(sw, se), 0.5), 
 				Rr2Point.mul(Rr2Point.add(se, ne), 0.5));
 		s = s.scale(sFactor);
 		q3 = new RrCSGPolygon(csg.prune(s), s);
 		
 		s = new RrBox(sw, cen);
 		s = s.scale(sFactor);
 		q4 = new RrCSGPolygon(csg.prune(s), s);
 		
 		// Recursively divide the children
 		
 		q1.divide(resolution_2, sFactor);
 		q2.divide(resolution_2, sFactor);
 		q3.divide(resolution_2, sFactor);
 		q4.divide(resolution_2, sFactor);
 	}
 	
 	/**
 	 * Find the quad containing a point
 	 * @param p
 	 * @return
 	 */
 	public RrCSGPolygon quad(Rr2Point p)
 	{
 		if(q1 == null)
 		{
 			if(box.point_relative(p) != 0)
 				System.err.println("find_quad(): point not in the box.");
 		} else
 		{
 			Rr2Point cen = box.centre();
 			if(p.x() >= cen.x())
 			{
 				if(p.y() >= cen.y())
 					return(q2.quad(p));
 				else
 					return(q3.quad(p));
 			} else
 			{
 				if(p.y() >= cen.y())
 					return(q1.quad(p));
 				else
 					return(q4.quad(p));               
 			}
 		}
 		
 		return this;
 	}
 	
 	
 	/**
 	 * Find the RrCSG expression that gives the potentaial at point p.
 	 * Note this does NOT find the closest half-plane unless the point
 	 * is on a surface.
 	 * @param p
 	 * @return
 	 */
 	public RrCSG leaf(Rr2Point p)
 	{
 		RrCSGPolygon q = quad(p);
 		return(q.csg.leaf(p));
 	}
 	
 	/**
 	 * Find the potentaial at point p.
 	 * @param p
 	 * @return
 	 */	
 	public double value(Rr2Point p)
 	{
 		RrCSG c = leaf(p);
 		return c.value(p);
 	}
 	
 	/**
 	 * Offset by a distance; grow or shrink the box by the same amount
 	 * If the old polygon was divided, the new one will be too.
 	 * If we shrink out of existence, a standard null object is returned.
 	 * @param d
 	 * @return
 	 */
 	public RrCSGPolygon offset(double d)
 	{
 		RrBox b;
 		if(-d >= 0.5*box.x().length() || -d >= 0.5*box.y().length())
 		{
 			b = new RrBox(new Rr2Point(0,0), new Rr2Point(1,1));
 			return new RrCSGPolygon(RrCSG.nothing(), b);
 		}
 		Rr2Point p = new Rr2Point(Math.sqrt(2)*d, Math.sqrt(2)*d);
 		b = new RrBox( Rr2Point.sub(box.sw(), p), Rr2Point.add(box.ne(), p) );
 		RrCSGPolygon result = new RrCSGPolygon(csg.offset(d), b);
 		if(q1 != null)
 			result.divide(resolution_2, sFactor);
 		return result;
 	}
 	
     
 	 /**
 	 * Deal with the case where a quad contains a single edge
      * @param l
      * @param i
      * @param r
      * @param here
      * @param d
      * @param st
      * @param fin
      * @param flag
      */   
     private void oneLine(RrLine l, RrInterval i, RrPolygon r, 
     		Rr2Point here, Rr2Point d, Rr2Point st, Rr2Point fin, int flag)
     {
     	if(st != null)
     		r.add(st, 0);
     	
     	if(Rr2Point.mul(d, l.direction()) < 0)
     	{
     		l = l.neg();
     		i = RrInterval.sub(0, i);
     	}
     	d.set(l.direction());
     	
     	if(fin != null)
     	{
     		r.add(fin, flag);
     		here.set(fin);
     	} else
     		here.set(l.point(i.high() + Math.sqrt(resolution_2)));
     }
     
     /**
 	 * Deal with the case where a quad contains two edges
      * @param qc
      * @param r
      * @param here
      * @param d
      * @param st
      * @param fin
      * @param flag
      */   
     private void twoLine(RrQContents qc, RrPolygon r, 
     		Rr2Point here, Rr2Point d, Rr2Point st, Rr2Point fin, int flag)
     {
     	Rr2Point d1 = qc.l1.d_2(here);
     	Rr2Point d2 = qc.l2.d_2(here);
     	
     	// If there's no corner in the quad, find the line we're 
     	// on and go along it
     	
     	if(!qc.corner)
     	{
     		if(d1.x() <= d2.x())
     		{
     			oneLine(qc.l1, qc.i1, r, here, d, st, fin, flag);
     			visit1 = true;
     		} else
     		{
     			oneLine(qc.l2, qc.i2, r, here, d, st, fin, flag);
     			visit2 = true;
     		}
     		return;
     	}
     	
        	RrLine l1, l2;
     	RrInterval i1, i2;
     	
     	if(d1.x() <= d2.x())
     	{
     		l1 = qc.l1;
     		i1 = qc.i1;
     		l2 = qc.l2;
     		i2 = qc.i2;
     	} else
     	{
     		l1 = qc.l2;
     		i1 = qc.i2;
     		l2 = qc.l1;
     		i2 = qc.i1;
     	}
     	
     	if(Rr2Point.mul(l1.direction(), d) < 0)
     	{
     		l1 = l1.neg();
     		i1 = RrInterval.sub(0, i1);
     	}
     	
     	boolean before = true;
     	
     	if(st != null)
     	{
     		if(!Rr2Point.same(qc.vertex, st, resolution_2))
     		{
     			r.add(st, 0);
     			before = Rr2Point.same(qc.vertex, l1.point(i1.high()), 
     					resolution_2);
     		}
     	}
     	
     	
     	if(before)
     	{
    			r.add(qc.vertex, flag);
  
     		if(Rr2Point.same(qc.vertex, l2.point(i2.high()), resolution_2))
     		{
     			l2 = l2.neg();
     			i2 = RrInterval.sub(0, i2);
     		}
     		d.set(l2.direction());
     		here.set(l2.point(i2.high() + Math.sqrt(resolution_2)));
     		visit1 = true;
     		visit2 = true;
     	} else
     	{
     		if(d1.x() <= d2.x())
     			visit1 = true;
     		else
     			visit2 = true;
     		d.set(l1.direction());
     		here.set(l1.point(i1.high() + Math.sqrt(resolution_2)));
     	}
     	
     	if(fin != null)
     	{
     		if(!Rr2Point.same(qc.vertex, fin, resolution_2))
     			r.add(fin, flag);
     		here.set(fin);
     	}
     		
     }
     
 	 /**
 	 * Walk round the edges of a polygon from here to there, trying
      * to start roughly in direction.
      * If here and there coincide, then a full circuit is returned.
      * The MEG algorithm is due to my old chum John Woodwark.
      * @param here
      * @param there
      * @param direction
      * @param flag
      * @return a polygon as the result
      */
     public RrPolygon meg(Rr2Point here, Rr2Point there, 
     		Rr2Point direction, int flag)
     {
             if(q1 == null)
             {
                     System.err.println("meg(): edge finding in an undivided polygon!  Making it up...");
                     double r2 = box.d_2()*1.0e-8;
                     divide(r2, 1);
             }
             
             RrCSGPolygon qh;
             RrCSGPolygon qt = quad(there);
             
             RrPolygon r = new RrPolygon();
             Rr2Point h = new Rr2Point(here);
             
             Rr2Point d;
             if(direction != null)
             	d = new Rr2Point(direction);
             else
             	d = null;
             
             Rr2Point fin, st;
             
             RrQContents qc;
 
             st = here;
             int loop = 0;
             boolean rightRound = Rr2Point.same(here, there, resolution_2);
             if(rightRound)
             	loop = -1;
             qh = quad(h);
             
             do
             {
             	qh = quad(h);
             	qc = new RrQContents(qh);
             	
             	if(qh == qt && loop >= 0)
             		fin = there;
             	else
             		fin = null;
             	
             	switch(qc.count)
            	{	
             	case 1:
             		if(qc.l1 != null)
             		{
             			if(d == null)
             				d = new Rr2Point(qc.l1.direction());
             			qh.oneLine(qc.l1, qc.i1, r, h, d, st, fin, flag);
             			qh.visit1 = true;
             		} else if (qc.l2 != null)
             		{
             			if(d == null)
             				d = new Rr2Point(qc.l2.direction());
             			qh.oneLine(qc.l2, qc.i2, r, h, d, st, fin, flag);
             			qh.visit2 = true;
             		} else
             			System.err.println("meg(): both segments null!");
             		break;
             		
             	case 2:
             		if(d == null)
         				d = new Rr2Point(qc.l1.direction());
             		qh.twoLine(qc, r, h, d, st, fin, flag);
             		break;
             		
             	default:
            		System.err.println("meg(): count not 1 or 2: " + qc.count);	
             	}
             	st = null;
             	loop++;
             } while (qh != qt || loop == 0);
             
             // If looping right round, the first and last
             // points coincide.  Remove the last.
             
             if(rightRound)
             	r.remove(r.size()-1);
             
             return r;
     }
     
 	 /**
 	 * Walk the tree setting all visited flags false
      */
     private void clearVisted()
     {
     	visit1 = false;
     	visit2 = false;
     	if(q1 != null)
     	{
     		q1.clearVisted();
     		q2.clearVisted();
     		q3.clearVisted();
     		q4.clearVisted();    		
     	}
     }
  
 	 /**
 	 * Walk the tree to find an unvisited corner
      */
     private Rr2Point findCorner()
     {
     	Rr2Point result = null;
     	
     	if(csg.complexity() == 2 && !visit1 && !visit2)
     	{
     		RrQContents qc = new RrQContents(this);
     		if(qc.corner)
     			return qc.vertex;
     	}
  
     	if(q1 != null)
     	{
     		result = q1.findCorner();
     		if(result != null)
     			return result;
        		result = q2.findCorner();
     		if(result != null)
     			return result; 
       		result = q3.findCorner();
     		if(result != null)
     			return result; 
      		result = q4.findCorner();
     		if(result != null)
     			return result;   		
     	}
     	
     	return result;
     }
 	 /**
 	 * Find all the polygons represented by a CSG object
 	 * @param fg
 	 * @param fs
      * @return a polygon list as the result
      */
     public RrPolygonList megList(int fg, int fs)
     {
     	clearVisted();
     	RrPolygonList result = new RrPolygonList();
     	RrPolygon m;
     	
     	Rr2Point vertex = findCorner();
     	while(vertex != null)
     	{
     		m = meg(vertex, vertex, null, fg);
     		if(m.size() > 0)
     		{
     			m.flag(0, fs);
     			if(m.size() > 2)
     				result.add(m);
     			else
     				System.err.println("megList(): polygon with < 3 sides!");
     		}
     		vertex = findCorner();
     	}
     	
     	return result;
     }
 	
 	/**
 	 * Intersect a line with a polygon, adding to an existing
 	 * unsorted list of the intersection parameters - recursive
 	 * internal call.
 	 * @param l0
 	 * @param t
 	 * @param big_wipe
 	 * @return
 	 */
 	private List pl_intersect_r(RrLine l0, List t, boolean big_wipe)
 	{
 		RrInterval range;
 		if(big_wipe)
 			range = RrInterval.big_interval();
 		else
 			range = new RrInterval(0, 1);
 		range = box.wipe(l0, range);
 		if(range.empty())
 			return t;
 		
 		if(q1 != null)
 		{
 			t = q1.pl_intersect_r(l0, t, big_wipe);
 			t = q2.pl_intersect_r(l0, t, big_wipe);
 			t = q3.pl_intersect_r(l0, t, big_wipe);
 			t = q4.pl_intersect_r(l0, t, big_wipe);
 		} else
 		{
 			double tx, v;
 			Rr2Point p;
 			
 			switch(csg.complexity())
 			{
 			case 0:
 				break;
 				
 			case 1:
 				try
 				{
 					tx = csg.plane().cross_t(l0);
 					if (range.in(tx))
 						t.add(new Double(tx));
 				}catch (rr_ParallelLineException ple)
 				{}
 				break;
 				
 			case 2:
 				try
 				{
 					tx = csg.c_1().plane().cross_t(l0);
 					if (range.in(tx))
 					{
 						p = l0.point(tx);
 						v = csg.value(p); 
 						if(v*v < resolution_2)
 							t.add(new Double(tx));
 					}
 				}catch (rr_ParallelLineException ple)
 				{}
 				
 				try
 				{
 					tx = csg.c_2().plane().cross_t(l0);
 					if (range.in(tx))
 					{
 						p = l0.point(tx);
 						v = csg.value(p); 
 						if(v*v < resolution_2)
 							t.add(new Double(tx));
 					}
 				}catch (rr_ParallelLineException ple)
 				{}
 				break;
 				
 			default:
 				System.err.println("pl_intersect_r(): complicated quad ignored. Complexity: " +
 						Integer.toString(csg.complexity()));
 			}
 		}
 		
 		return t;
 	}
 	
 	/**
 	 * Take a sorted list of parameter values and a line, and
 	 * make sure they alternate solid/void/solid etc.  Insert
 	 * duplicate parameter values if need be to ensure this. 
 	 * @param t
 	 * @param l0
 	 */
 	private void solidSet(List t, RrLine l0)
 	{
 		
 		double half, v;
 		int i = 0;
 		boolean odd = true;
 		while(i < t.size()-1)
 		{
 			half = 0.5*(((Double)(t.get(i))).doubleValue() + 
 				((Double)(t.get(i+1))).doubleValue());
 			v = value(l0.point(half));
 			if(odd)
 			{
 				if(v > 0)
 					t.add(i, t.get(i));
 			} else
 			{
 				if(v <= 0)
 					t.add(i, t.get(i));
 			}
 			odd = !odd;
 			i++;
 		}
 		if (t.size()%2 != 0)    // Nasty hack that seems to work...
 			t.remove(t.size() - 1);
 	}
 	
 	/**
 	 * Intersect a line with a polygon, giving a list of the 
 	 * intersection parameters.
 	 * @param l0
 	 * @param big_wipe
 	 * @return a sorted list of parameter values.
 	 */
 	public List pl_intersect(RrLine l0, boolean big_wipe)
 	{
 		if(q1 == null)
 		{
 			System.err.println("pl_intersect(): Ray intersection with undivided polygon!  Making it up...");
 			double r2 = box.d_2()*1.0e-8;
 			divide(r2, 1);
 		}
 		List t = new ArrayList();	
 		t = pl_intersect_r(l0, t, big_wipe);
 		java.util.Collections.sort(t);
 		if(big_wipe)
 			solidSet(t, l0);
 		return t;
 	}
 	
 	
 	/**
 	 * Hatch a csg polygon parallel to line l0 with index gap
 	 * @param l0
 	 * @param gap
 	 * @param fg
 	 * @param fs
 	 * @return a polygon list as the result with flag values f
 	 */
 	public RrPolygonList hatch(RrLine l0, double gap, int fg, int fs)
 	{
 		RrBox big = box.scale(1.1);
 		double d = Math.sqrt(big.d_2());
 		RrPolygonList result = new RrPolygonList();
 		Rr2Point orth = new Rr2Point(-l0.direction().y(), l0.direction().x());
 		orth.norm();
 		
 		int quad = (int)(2*Math.atan2(orth.y(), orth.x())/Math.PI);
 		
 		Rr2Point org;
 		
 		switch(quad)
 		{
 		case 0:
 			org = big.sw();
 			break;
 			
 		case 1:
 			org = big.se();
 			break;
 			
 		case 2:
 			org = big.ne(); 
 			break;
 			
 		case 3:
 			org = big.nw();
 			break;
 			
 		default:
 			System.err.println("RrPolygon hatch(): The atan2 function doesn't seem to work...");
 			org = big.sw();
 		}
 		
 		double g = 0;
 		
 		//double proj = (Rr2Point.mul(orth, org) - Rr2Point.mul(orth, l0.origin()))/gap;
 		//proj = (double)((int)proj + 1) - proj;
 		//org = Rr2Point.add(org, Rr2Point.mul(orth, proj));
 		
 		orth = Rr2Point.mul(orth, gap);
 		
 		RrLine hatcher = new RrLine(org, Rr2Point.add(org, l0.direction()));
 		RrPolygon r;
 		
 		while (g < d)
 		{
 			hatcher = hatcher.neg();
 			List t_vals = pl_intersect(hatcher, true);
 			if (t_vals.size() > 0)
 			{
 				r = RrPolygon.rr_t_polygon(t_vals, hatcher, fg, fs);
 				result.add(r);
 			}
 			hatcher = hatcher.add(orth);
 			g = g + gap;
 		}
 		return result;
 	}
 	
 	/**
 	 * Is the line between two points wholely within the polygon?
 	 * @param here
 	 * @param there
 	 * @return 
 	 */
 	private boolean all_inside(Rr2Point here, Rr2Point there)
 	{
 		// The points are on the surface.  Are they on the _same_
 		// surface?  (Suppose there's a gap in it, dummo?...)
 		RrCSG ch = leaf(here);
 		RrCSG ct = leaf(there);
 		if(ch == ct)
 			return true;
 		
 		RrLine line = new RrLine(here, there);
 		if(value(line.point(0.5)) > 0) // Need to go round the corner here
 			return false;
 		
 		List t = new ArrayList();	
 		t = pl_intersect_r(line, t, false);
 		double v;
 		double r = Math.sqrt(resolution_2);
 		for(int i = 0; i < t.size(); i++)
 		{
 			v = ((Double)(t.get(i))).doubleValue();
 			if((v > r) && (v < 1 - r))
 				return false;
 		}
 		
 		return true;
 	}
 	
 	
 	private RrHSearch search_line(RrPolygon pg, Rr2Point here, double ds)
 	{
 		RrHSearch result = new RrHSearch();
 		result.thePoint = -1;
 		result.dsq = ds;
 		Rr2Point there;
 		double d2;
 		
 		for(int i = 0; i < pg.size(); i += 2) 
 		{
 			if(pg.flag(i) >= 0) 
 			{
 				there = pg.point(i);
 				d2 = Rr2Point.d_2(here, there);
 				if(d2 < result.dsq) 
 				{
 					if(all_inside(here, there)) 
 					{
 						result.thePoint = i;
 						result.dsq = d2;
 					} 
 				}
 				there = pg.point(i+1);
 				d2 = Rr2Point.d_2(here, there);
 				if(d2 < result.dsq) 
 				{
 					if(all_inside(here, there)) 
 					{
 						result.thePoint = i + 1;
 						result.dsq = d2;
 						
 					}
 				}
 			}
 		}
 		return result;
 	}
 	
 	private RrHSearch search_join(RrPolygonList p_l, Rr2Point here, int line)
 	{
 		RrHSearch result = new RrHSearch();
 		RrHSearch intermediate;
 		result.notFinished = true;
 		result.join = false;
 		double dsq = Double.POSITIVE_INFINITY;
 		int thePoint = -1;
 		int theLine = -1;
 		RrPolygon pg;
 		
 		if(line > 0) 
 		{
 			pg = p_l.polygon(line - 1);
 			intermediate = search_line(pg, here, dsq);
 			if(intermediate.thePoint >= 0) 
 			{
 				dsq = intermediate.dsq;
 				theLine = line - 1;
 				thePoint = intermediate.thePoint;
 			}
 		}
 		if(line < p_l.size() - 1) 
 		{
 			pg = p_l.polygon(line + 1);
 			intermediate = search_line(pg, here, dsq);
 			if(intermediate.thePoint >= 0) 
 			{
 				dsq = intermediate.dsq;
 				theLine = line + 1;
 				thePoint = intermediate.thePoint;
 			}
 		}
 		
 		if(thePoint >= 0)
 		{
 			result.join = true;
 			result.theLine = theLine;
 			result.thePoint = thePoint;
 		}
 		
 		return result;
 	}
 	
 	private RrHSearch new_start(RrPolygonList p_l, Rr2Point here)
 	{
 		RrHSearch result = new RrHSearch();
 		result.notFinished = false;        
 		result.join = false;
 		result.dsq = Double.POSITIVE_INFINITY;
 		double d2;
 		
 		for(int i = 0; i < p_l.size(); i++)
 		{
 			RrPolygon pg = p_l.polygon(i);
 			for(int j = 0; j < pg.size(); j += 2)
 			{
 				if(pg.flag(j) >= 0) 
 				{
 					Rr2Point there = pg.point(j);
 					d2 = Rr2Point.d_2(here, there);
 					if(d2 < result.dsq) 
 					{
 						result.dsq = d2;
 						result.notFinished = true;
 						result.theLine = i;
 						result.thePoint = j;   
 					}
 					there = pg.point(j + 1);
 					d2 = Rr2Point.d_2(here, there);
 					if(d2 < result.dsq) 
 					{
 						result.dsq = d2;
 						result.notFinished = true;
 						result.theLine = i;
 						result.thePoint = j + 1;   
 					}
 				}
 			}
 		}
 		
 		return result;       
 	}
 	
 	public RrPolygon hatch_join(RrLine l0, double gap, int fg, int fs)
 	{
 		if(fg < 0 || fs < 0)
 			System.err.println("hatch_join(): illegal negative flag value " + 
 					Integer.toString(fg) + " or " + Integer.toString(fs));
 		
 		RrPolygonList p_l = hatch(l0, gap, fg, fs);
 		RrPolygon p1 = new RrPolygon();
 		
 		int leng = p_l.size();
 		if(leng <= 0)
 			return p1;
 
 		int i = 0;
 		RrPolygon here_p = p_l.polygon(i);
 		while(here_p.size() <= 0)
 		{
 			i++;
 			if(i >= leng)
 				break;
 			here_p = p_l.polygon(i);
 		}
 		
 		if(here_p.size() <= 0)
 		{
 			System.err.println("Nothing in the hatch!");
 			return p1;
 		}
 		
 		Rr2Point here = here_p.point(0);
 		
 		Rr2Point pa, pb;
 		
 		RrHSearch next = new_start(p_l, here);
 		int line = next.theLine;
 		boolean notFinished = next.notFinished;
 		next.join = true;
 		int flag = fs;
 		
 		while(notFinished) 
 		{
 			if(next.join) 
 			{
 				if(next.thePoint%2 == 1) 
 				{
 					pa = p_l.polygon(next.theLine).point(next.thePoint);
 					pb = p_l.polygon(next.theLine).point(next.thePoint - 1);
 					p_l.polygon(next.theLine).flag(next.thePoint - 1, -1);
 				} else 
 				{
 					pa = p_l.polygon(next.theLine).point(next.thePoint);
 					p_l.polygon(next.theLine).flag(next.thePoint, -1);
 					pb = p_l.polygon(next.theLine).point(next.thePoint + 1);
 				}
 				p1.add(pa, flag);
 				flag = fg;
 				here = pb;
 				line = next.theLine;
 				p1.add(here, flag);
 			} else 
 			{
 				flag = fs;
 				next = new_start(p_l, here);
 				notFinished = next.notFinished;
 				if(notFinished) 
 				{
 					if(next.thePoint%2 == 1) 
 					{
 						pa = p_l.polygon(next.theLine).point(next.thePoint);
 						pb = p_l.polygon(next.theLine).point(next.thePoint - 1);
 						p_l.polygon(next.theLine).flag(next.thePoint - 1, -1);
 					} else 
 					{
 						pa = p_l.polygon(next.theLine).point(next.thePoint);
 						p_l.polygon(next.theLine).flag(next.thePoint, -1);
 						pb = p_l.polygon(next.theLine).point(next.thePoint + 1);
 					}
 					p1.add(pa, flag);
 					flag = fg;
 					here = pb;
 					line = next.theLine;
 					p1.add(here, flag);
 				}
 			}
 			if(notFinished)
 				next = search_join(p_l, here, line);
 		}
 		
 		return p1;
 	}
 	
 }
