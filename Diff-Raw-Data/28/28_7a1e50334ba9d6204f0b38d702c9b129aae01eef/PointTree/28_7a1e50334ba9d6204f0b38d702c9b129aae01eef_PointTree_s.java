 /*
  * Copyright 2010 Colin Paton - cozzarp@googlemail.com
  * This file is part of rEdBus.
  *
  *  rEdBus is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  rEdBus is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with rEdBus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 // KD-Tree implementation
 // FIXME - Make into a generic KD-Tree class
 // FIXME - currently a bit nasty due to way data is read from resource - e.g. needs nodes[] array
 //       - Android only gives an InputStream to a resource - ideally we need Random access.
 //       - Read data into memory for now.
 
 package org.redbus;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.Math;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.content.Context;
 import android.util.Log;
 
 
 public class PointTree {
 	
 	private static PointTree pointTree = null;
 	
 	public static PointTree getPointTree(Context ctx)
 	{
 		if (pointTree == null) {
 	        InputStream stopsFile = null;
 			try {
 		        stopsFile = ctx.getResources().openRawResource(R.raw.stops);
 				pointTree = new PointTree(stopsFile);
 			} catch (IOException e) {
 				Log.println(Log.ERROR,"redbus","Error reading stops");
 				e.printStackTrace();
 			} finally {
 				try {
 					if (stopsFile != null)
 						stopsFile.close();
 				} catch (Throwable t) {
 				}
 			}
 		}
 		
 		return pointTree;
 	}
 	
 	// Represents a node in the tree
 	public class BusStopTreeNode {
 		private double x;
 		private double y;
 		private int leftNode;
 		private int rightNode;
 		private int stopCode;
 		private String stopName;
 		private long servicesMap;
 		
 		public BusStopTreeNode(int leftNode, int rightNode, double x, double y, int stopCode, String stopName, long servicesMap)
 		{
 			this.leftNode = leftNode;
 			this.rightNode = rightNode;
 			this.x = x;
 			this.y = y;
 			this.stopCode = stopCode;		
 			this.stopName = stopName;
 			this.servicesMap = servicesMap;
 		}
 		
 		public String getStopName() { return this.stopName; }
 		public int getStopCode() { return this.stopCode; }
 		public double getX() { return this.x; }
 		public double getY() { return this.y; }
 		public long getServicesMap() { return this.servicesMap; }
 	}
 	
 	private BusStopTreeNode[] nodes;
 	private Map<Integer, Integer> nodeIdxByStopCode;
 	private String[] services;
 	private int rootRecordNum;
 
 	// Read Data from the Android resource 'stops.dat' into memory
 	
 	private PointTree(InputStream is) throws IOException
 	{
 		// read entire file into temp buffer
 		byte[] b = new byte[200*1024];
 		if (is.read(b) == b.length)
 			throw new RuntimeException("PointTree temp buffer was too small");
 		this.rootRecordNum = readInt(b, 4);
 
 		// Root is always the last record in the file
 		this.nodes = new BusStopTreeNode[rootRecordNum+1];
 		this.nodeIdxByStopCode = new HashMap<Integer, Integer>();
 		
 		int off = 8;
 		for(int i = 0; i <= rootRecordNum; ++i)
 		{
 			int leftNode = readInt(b, off + 0);
 			int rightNode = readInt(b, off + 4);
 			double x = Double.longBitsToDouble(readLong(b, off + 8));
 			double y = Double.longBitsToDouble(readLong(b, off + 16));
 			int stopCode = readInt(b, off + 24);
 			String stopName = new String(b, off + 28, 16, "utf-8").trim();
 			long servicesMap = readLong(b, off + 44);
 
 			BusStopTreeNode node = new BusStopTreeNode(leftNode, rightNode, x, y, stopCode, stopName, servicesMap);
 			nodes[i] = node;
 			nodeIdxByStopCode.put(new Integer(node.getStopCode()), new Integer(i));
 			
 			off += 52;
 		}
 		
 		int servicesCount = readInt(b, off);
 		off += 4;
 		this.services = new String[servicesCount];
 		int startoff = off;
 		for(int i =0; i< servicesCount; i++) {
 			while(b[off] != 0)
 				off++;
 			this.services[i] = new String(b, startoff, off - startoff, "utf-8");
 			off++;
 			startoff = off;
 		}
 	}
 	
 	private int readInt(byte[] b, int off)
 	{
		return ((b[off+0] & 0xff) << 24) |
			   ((b[off+1] & 0xff) << 16) |
			   ((b[off+2] & 0xff) <<  8) |
			    (b[off+3] & 0xff);
 	}
 
 	private long readLong(byte[] b, int off)
 	{
		return ((b[off+0] & 0xff) << 56) |
			   ((b[off+1] & 0xff) << 48) |
			   ((b[off+2] & 0xff) << 40) |
			   ((b[off+3] & 0xff) << 32) |
			   ((b[off+4] & 0xff) << 24) |
			   ((b[off+5] & 0xff) << 16) |
			   ((b[off+6] & 0xff) <<  8) |
			    (b[off+7] & 0xff);
 	}
 
 	// Could use Android location class to do this, but kept in here from prototype.
 	// sqrt isn't technically needed, but in here to possibly do distance conversions later.
 	
 	private double distance(BusStopTreeNode node, double x, double y)
 	{
 		return Math.sqrt(Math.pow(node.getX()-x,2) + Math.pow(node.getY()-y,2));
 	}
 	
 	private BusStopTreeNode lookupNode(int number)
 	{
 		if (number == -1) return null; // Child is a leaf
 		
 		return this.nodes[number];
 	}
 	
 	// Recursive search - use 'findNearest' to start
 	private BusStopTreeNode searchNearest(BusStopTreeNode here, BusStopTreeNode best, double x, double y, int depth)
 	{
 		if (here == null) return best;
 		if (best == null) best = here;
 		
 		double herepos, wantedpos;
 		
 		if (depth % 2 == 0) {
 		    herepos = here.getX();
 		    wantedpos = x;
 		}
 		else {
 			herepos = here.getY();
 			wantedpos = y;
 		}
 		
 		// Which is closer?
 		double disthere = distance(here,x,y);
 		double distbest = distance(best,x,y);
 		
 		if (disthere < distbest) best = here;
 		
 		// Which branch is nearer?
 		BusStopTreeNode nearest, furthest;
 		
 		if (wantedpos < herepos) {
 			nearest = lookupNode(here.leftNode);
 			furthest = lookupNode(here.rightNode);
 		}
 		else{
 			furthest = lookupNode(here.leftNode);
 			nearest = lookupNode(here.rightNode);
 		}
 		
 		best = searchNearest(nearest,best,x,y,depth+1);
 		
 		// Do we still need to search the away branch?
 		distbest = distance(best,x,y);
 		
 		double distaxis = Math.abs(wantedpos - herepos);
 		
 		if (distaxis < distbest)
 			best = searchNearest(furthest,best,x,y,depth+1);
 		
 		return best;
 	}
 	
 	private BusStopTreeNode getRoot()
 	{
 		return this.nodes[this.rootRecordNum];
 	}
 	
 	// Public interface to this class - finds the node nearest to the supplied
 	// co-ords
 	
 	public BusStopTreeNode findNearest(double x, double y)
 	{	
 		return this.searchNearest(this.getRoot(),null,x,y,0);
 	}
 	
 	private ArrayList<BusStopTreeNode> searchRect(double xtl, double ytl,
 			                                         double xbr, double ybr,
 			                                         BusStopTreeNode here,
 			                                         ArrayList<BusStopTreeNode> stops,
 			                                         int depth)
 	{
 		if (here==null) return stops;
 	
 		// Limit number of stops, otherwise the map gets slow
 		if (stops.size() >= 200) return stops;
 		
 		//Log.println(Log.DEBUG, "visiting", here.getStopName());
 		
 		double topleft, bottomright, herepos, herex, herey;
 		
 		herex=here.getX();
 		herey=here.getY();
 		
 		if (depth % 2 == 0) {
 		    herepos = herex;
 		    topleft = xtl;
 		    bottomright = xbr;
 		}
 		else {
 			herepos = herey;
 			topleft = ytl;
 			bottomright = ybr;
 		}
 		
 		if (topleft > bottomright) {
 			Log.println(Log.ERROR,"redbus", "co-ord error!");
 		}
 		
 		if (bottomright > herepos) {
 			stops = searchRect(xtl,ytl,xbr,ybr,lookupNode(here.rightNode),stops, depth+1);
 		}
 		
 		if (topleft < herepos) {
 			stops = searchRect(xtl,ytl,xbr,ybr,lookupNode(here.leftNode),stops, depth+1);
 		}
 		
 		// If this node falls within range, add it
 		if (xtl <= herex && xbr >= herex && ytl <= herey && ybr >= herey) {
 			stops.add(here);
 		}
 		
 		return stops;
 	}
 	
 	// Return nodes within a certain rectangle - top-left/bottom-right
 	public ArrayList<BusStopTreeNode> findRect(double xtl, double ytl,
 	                                             double xbr, double ybr)
 	{
 		ArrayList<BusStopTreeNode> stops = new ArrayList<BusStopTreeNode>();
 		
 		//Log.println(Log.DEBUG, "redbus", "tl: "+ Double.toString(xtl) + "," + Double.toString(ytl));
 		//Log.println(Log.DEBUG, "redbus", "br: "+ Double.toString(xbr) + "," + Double.toString(ybr));
 		
 		return searchRect(xtl,ytl,xbr,ybr,this.getRoot(),stops,0);
 	}
 	
 	// Return nodes within a certain radius
 	public ArrayList<BusStopTreeNode> findRadius(double xcentre, double ycentre, double radiusMetres)
 	{
 		// Convert radius in metres to approximate decimal degrees.
 		// http://en.wikipedia.org/wiki/Decimal_degrees
 		//
 		// 111,319.9 metres = roughly 1 degree
 		
 		double radiusDegrees = radiusMetres / 111319.9;
 		
 	    // A rectangle is *nearly* a circle, right...
 		// Rectangle searching of the tree is easier than radius searching. We should
 		// filter out finds outside the circle, but we're only interested in an approximate
 		// search right now.
 		
 		return findRect(xcentre-radiusDegrees,
 				          ycentre-radiusDegrees,
 				          xcentre+radiusDegrees,
 				          ycentre+radiusDegrees);
 	}
 	
 	public BusStopTreeNode lookupStopByStopCode(int stopCode)
 	{
 		Integer node = nodeIdxByStopCode.get(new Integer(stopCode));
 		if (node == null)
 			return null;
 		if (node.intValue() >= nodes.length)
 			return null;
 		return nodes[node.intValue()];
 	}
 	
 	public ArrayList<String> lookupServices(long servicesMap)
 	{
 		ArrayList<String> result = new ArrayList<String>();
 		for(int i=0; i< 64; i++)
			if ((servicesMap & (1 << i)) != 0)
 				result.add(services[i]);
 		return result;
 	}
 }
