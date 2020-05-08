 /**
  * 
  */
 package org.eclipse.stem.internal.data.utility;
 
 /*******************************************************************************
  * Copyright (c) 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.eclipse.swt.graphics.Point;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 
 
 
 /**
  * This class finds the centers or central interior points for all 
  * STEM regions (by ID). It uses the largest polygon
  * if their is more than one per region
  * 
  **/
 public class PolygonCenterGenerator {
 	
 	private static final String GML_ID     = "gml:id";
 	private static final String POLYGON    = "gml:Polygon";
 	private static final String BOUNDARY   = "gml:outerBoundaryIs";
 	private static final String LINEARRING = "gml:LinearRing";
 	private static final String POSLIST    = "gml:posList";
 	private static final String MAP_FILE_SUFFIX = "_MAP.xml";
 	private static final double RESCALE = 1000000.0;
 	
 	private static boolean DO_SINGLE_COUNTRY = false;
 	private static final String SINGLE_COUNTRY = "ITA/";
 	
 	
 	/**
 	 * Hard coded File reference to the above URI Needed because we don;t know
 	 * how to convert a URI to a file path
 	 */
 	public static final String GEO_FOLDER = "../"
 			+ "org.eclipse.stem.data.geography/" 
 			+ "resources/data/geo/country/";
 	
 	/**
 	 * Hard coded File reference to the above URI Needed because we don;t know
 	 * how to convert a URI to a file path
 	 */
 	public static final String OUTPUT_FOLDER =  "./resources/data/country/";
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		discoverCenters();
        	  
 		 
 	}
 
     /**
      * reads ALL gml data at full resolution and gets the centers for each node
      */
     public static final void discoverCenters() {
     
          try { 
        	  File f = new File(GEO_FOLDER);
        	  if (f.isDirectory()) {
        		
        		File[] ccountries = f.listFiles();
        		if (DO_SINGLE_COUNTRY) {
        			ccountries = new File[1];
        			ccountries[0] = new File(GEO_FOLDER+SINGLE_COUNTRY);
        		}
        		  
        		
        		 
        		 
        		 for (int i = 0; i < ccountries.length; i ++) {
        			 File countryFolder = ccountries[i];
        			 String country = countryFolder.getName();
        			 Map<String, Point> centerMap = new HashMap<String,Point>();
        			 if ((!country.equalsIgnoreCase("ZZZ"))||(!country.equalsIgnoreCase("CVS")) ) {
        				
        				 if (countryFolder.isDirectory()) {
        					 File[] files = countryFolder.listFiles();
        					 // we may have multiple gml files at various resolutions
        					 for (int j = 0; j < files.length; j++) {
 								File file = files[j];
 								String name = file.getName();
 								int idx = name.indexOf(MAP_FILE_SUFFIX);
 								if(idx>=1) {
 								// String mapLevel = name.substring(0, idx);
 								addPolygonCentersGML(file, centerMap);
 								}
 
 							}// for j xml files at different resolutions
        					 writeCenters(centerMap, country);
        				 }// if dir
        			 }// if real country
        			
        		 }// for countries
        	  }// if dir
        		 
        	 } catch (Exception e) { 
              // catch io errors from FileInputStream or readLine() 
              System.out.println(" IOException error!" + e.getMessage()); 
              e.printStackTrace();
           }
     }
 	
 	/**
 	 * reads the GML Map file of polygons keyed by regions.
 	 * 
 	 * @param file the gml file to read
 	 * @param centerMap 
 	 */
 	@SuppressWarnings("null")
 	public static void addPolygonCentersGML(File file, Map<String, Point> centerMap) {
 		
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		Document doc = null;
 
 		try {
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			doc = builder.parse(file);
 		} catch (Exception e) {
 			System.out.println("Error parsing file " + e.getMessage());
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		//
 		// MEMBER could have in it Road, Rail, etc...
 		//
 		Element route = doc.getDocumentElement();
 		NodeList allNodes = route.getElementsByTagName(POLYGON);
 
 		for (int i = 0; i < allNodes.getLength(); i++) {
 			Element polygonElt = (Element) allNodes.item(i);
 			String id = polygonElt.getAttribute(GML_ID);
 			Polygon poly = null;
 			
 			double largestArea = -1.0;
 			/*
 			 * <gml:Polygon gml:id="MX-TAB"> <gml:outerBoundaryIs>
 			 * <gml:LinearRing> <gml:posList>
 			 */
 			// default
 			NodeList boundaryList = polygonElt.getElementsByTagName(BOUNDARY);
 			for (int ii = 0; ii < boundaryList.getLength(); ii++) {
 				Element boundary = (Element) boundaryList.item(ii);
 				NodeList ringList = boundary.getElementsByTagName(LINEARRING);
 				for (int j = 0; j < ringList.getLength(); j++) {
 					Element ring = (Element) ringList.item(j);
 					NodeList posList = ring.getElementsByTagName(POSLIST);
 					for (int jj = 0; jj < posList.getLength(); jj++) {
 						Element points = (Element) posList.item(jj);
 						if (points != null) {
 							Node tn = points.getFirstChild();
 							String latlong = tn.getTextContent();
                             // add the polygon to the map
 							Polygon testPoly = getPolygon(latlong);
 							
 							double area = Math.abs(getArea(testPoly));
 							if(area >= largestArea) {
 								largestArea = area;
 								poly = new Polygon(testPoly.xpoints, testPoly.ypoints, testPoly.npoints);
 								//System.out.println("Found "+id+"  area "+area);
 							}// if largest polygon
 						}// if have more polygons
 					}// all polygons
 				}// all boundaries
 			}// all rings
 			//double finalArea = Math.abs(getArea(poly));
 			//System.out.println("Picked "+id+"  area "+finalArea);
 			//System.out.println(" ");
 			Point p = getInteriorCentroid(poly);
 			centerMap.put(id,p);
 		}// all nodes
 
 		System.out.println("Read " + centerMap.size() + " polygons in file ");
 
 		return;
 	}// readMapPolygonsGML()
 	
 	/**
 	 * create a polygon
 	 * lat lng units for paths and polygons are (int) degrees x 1000000
 	 * @param data
 	 * @return the polygon as a string of lat long data
 	 */
 	public static Polygon getPolygon(String data) {
 		
 		StringTokenizer st = new StringTokenizer(data);
 		ArrayList<Double> latList = new ArrayList<Double>();
 		ArrayList<Double> lngList = new ArrayList<Double>();
 		while (st.hasMoreTokens()) {
 			String lat = st.nextToken();
 			String lng = st.nextToken();
 			latList.add(new Double(lat));
 			lngList.add(new Double(lng));
 		}
 		// make sure the polygon is closed
 		if ((latList.get(latList.size()-1)!= latList.get(0))
 			||
 			(lngList.get(lngList.size()-1)!= lngList.get(0))){
 			latList.add(latList.get(0));
 			lngList.add(lngList.get(0));
 		}
 		
 		int size = latList.size();
 		int[] lt = new int[size];
 		int[] lg = new int[size];
 		for (int i = 0; i < size; i ++) {
 			
 			
 			lt[i] = (int) (RESCALE * latList.get(i).doubleValue());
 			lg[i] = (int) (RESCALE * lngList.get(i).doubleValue());
 		}
 		
 		return new Polygon(lt, lg, size);
 		
 	}// getPolygon
 	
 	/**
 	 * finds the area of a polgon - exact
 	 * @param p
 	 * @return the area
 	 */
 	public static double getArea(Polygon p) {
 		double area = 0;
 		int[] x = p.xpoints;
 		int[] y = p.ypoints;
 		
 	
 		for (int i = 0; i < x.length-1; i ++) {
 			area += (((double)x[i]*(double)y[i+1]) - ((double)x[i+1]*(double)y[i]));
 		}
 		area /= 2.0;
 		return area;
 	}
 	
 	
 	
 	/**
 	 * finds and returns an internal "central" point
 	 * in the polygon. Not the centriod which could be outside the boundary
 	 * for u shaped regions
 	 * @param p
 	 * @return the "center"
 	 */
 	public static Point getInteriorCentroid(Polygon p) {
 		double maxRange = -1.0;
 		double cx = 0;
 		double cy = 0;
 		Rectangle r = p.getBounds();
 		final double STEP = 100.0;
 		
 		double deltax = Math.abs((double) r.width);
 		double deltay = Math.abs((double) r.height);
 		
 		deltax /= STEP;
 		deltay /= STEP;
 		//System.out.println("Dwidth,Dheight  =  "+deltax+", "+deltay);
 		
 		double x = r.getMinX();
 		double y = r.getMinY();
 	
 		for (int i = 0; i <= (int) STEP; i ++) {
 			y = r.getMinY();
 			for(int j = 0; j <= (int)STEP; j ++) {
 				if(p.contains(x, y)) {
 					double range = getSqrdEdgeRange(x,y,p);
 					
 					if (range >= maxRange) {
 						maxRange = range;
 						cx = x;
 						cy = y;
 						
 					}
 				}
 				y += deltay;
 			}
 			x += deltax;
 		}
 		
 		//System.out.println("initial estimate "+cx+", "+cy);
 		// now step back on step and do 200 more steps
 		// each 100x smaller for a scan resolution of 1/10000
 		x = cx-deltax;
 		y = cy-deltay;
 		
 		double y0 = y;
 		deltax /= STEP;
 		deltay /= STEP;
 		for (int i = 0; i <= (int)(2.0* STEP); i ++) {
 			y = y0;
 			for(int j = 0; j <= (int)(2.0*STEP); j ++) {
 				if(p.contains(x, y)) {
 					double range = getSqrdEdgeRange(x,y,p);
 					
 					if (range >= maxRange) {
 						maxRange = range;
 						cx = x;
 						cy = y;
 						
 					}
 				}
 				y += deltay;
 			}
 			x += deltax;
 		}
 		
 		int ix = (int)Math.round(cx);
 		int iy = (int)Math.round(cy);
 		Point center = new Point(ix,iy);
 		
 		return center;
 	}// get inner centroid
 	
 	/**
 	 * for testing
 	 * @param map
 	 */
 	public static void printMap(Map<String, Point> map) {
		Iterator<String> iter = map.keySet().iterator();
		while((iter!=null)&&(iter.hasNext())) {
			String id = iter.next();
			Point p = map.get(id);
 			System.out.println("id: "+id+" "+p.x+", "+p.y);
 		}
 	}
 	
 	/**
 	 * Create the .properties file
 	 * @param map
 	 * @param country 
 	 */
 	public static void writeCenters(Map<String, Point> map, String country) {
 		//1. make the direc
 		File outFile = new File(OUTPUT_FOLDER+country+"/"+country+"_centers.properties");
 		try {
             System.out.println("writing centers "+outFile.getName()+" = "+outFile.getAbsolutePath());
 			FileWriter fw = new FileWriter(outFile);
 			fw.write("# "+ country+"_centers.properties \n");
 			fw.write("# all regions centers (id, lat, long) \n");
 			fw.write("\n");
 
 			ArrayList<String> lvl0Keys = new ArrayList<String>();
 			ArrayList<String> lvl1Keys = new ArrayList<String>();
 			ArrayList<String> lvl2Keys = new ArrayList<String>();
 			
 			Iterator<String> iter = map.keySet().iterator();
 			int icount = 0;
 			// sort the keys by admin level
 			while((iter!=null)&&(iter.hasNext())) {
 				icount ++;
 				String id = iter.next();
 				if(id.indexOf("-")==-1) {
 					lvl0Keys.add(id);
 				} else {
 					int idx = id.indexOf("-");
 					String test = id.substring(idx+1,id.length());
 					if(test.indexOf("-")==-1) {
 						lvl1Keys.add(id);
 					} else {
 						lvl2Keys.add(id);
 					}
 				}
 			}
 			//
 			fw.write("# level 0 \n");
 			if (lvl0Keys.size() >=1) {
 				writeLevelData(lvl0Keys, map, fw);
 			}// level 0
 		
 			//
 			fw.write("\n");
 			fw.write("# level 1 \n");
 			if (lvl1Keys.size() >=1) {
 				writeLevelData(lvl1Keys, map, fw);
 			}// level 1
 			
 			fw.write("\n");
 			fw.write("# level 2 \n");
 			if (lvl2Keys.size() >=1) {
 				writeLevelData(lvl2Keys, map, fw);
 			}// level 2
 				
 			fw.flush();
 			fw.close();
 		} catch(IOException ioe) {
 			System.out.println("error writing file"+ioe.getMessage());
 			System.exit(1);
 		}
 		
 	}// writeCenters
 	
 	/**
 	 * 
 	 * @param lvlKeys
 	 * @param map
 	 * @param fw
 	 */
 	public static void writeLevelData(List<String> lvlKeys, Map<String, Point> map, FileWriter fw ) {
 		String[] keys = new String[lvlKeys.size()];
 		for (int i = 0; i < lvlKeys.size(); i ++) {
 			keys[i] = lvlKeys.get(i);
 		}
 		Arrays.sort(keys);
 		try {
 			for (int i = 0; i < lvlKeys.size(); i ++) {
 				Point p = map.get(keys[i]);
 				double x = p.x;
 				double y = p.y;
 				x/= RESCALE;
 				y/= RESCALE;
 				fw.write(keys[i]+" = "+x+", "+y+"\n");
 			}
 		} catch(IOException ioe) {
 			System.out.println("error writing file"+ioe.getMessage());
 			System.exit(1);
 		}
 	}// writeLevelData
 	
 	
 	
 	/**
 	 * finds the range to the nearest edge point
 	 * @param qx
 	 * @param qy
 	 * @param p
 	 * @return range
 	 */
 	public static double getSqrdEdgeRange(double qx, double qy, Polygon p) {
 		double range = Double.MAX_VALUE;
 		int[] ix = p.xpoints;
 		int[] iy = p.ypoints;
 				
 		for (int i=0;i<ix.length;i++) {
 			double x = ix[i];
 			double y = iy[i];
 			double dx = qx-x;
 			double dy = qy-y;
 			double r = (dx*dx) + (dy*dy);
 			if (r <= range) range = r;
 		}
 		return range;
 		
 	}
 	
 	
 }
