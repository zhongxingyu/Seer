 /**
  * Copyright 2010 The University of Nottingham
  * 
  * This file is part of automicslogviz.
  *
  *  automicslogviz is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  automicslogviz is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with automicslogviz.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package automicslogviz;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.UnsupportedEncodingException;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author cmg
  *
  */
 public class DrawTrails {
 	static Logger logger = Logger.getLogger(DrawTrails.class.getName());
 	public static double BORDER_M = 100;
 	public static double SCALE = 1000;
 	private static double minX=Double.MAX_VALUE, minY=Double.MAX_VALUE, maxX=-Double.MAX_VALUE, maxY=-Double.MAX_VALUE;
 	private static double minLat=Double.MAX_VALUE, minLon=Double.MAX_VALUE, maxLat=-Double.MAX_VALUE, maxLon=-Double.MAX_VALUE;
 	private static String colors [] = new String [] { "#f00", "#ff0", "#0f0", "#0ff", "#00f", "#f0f", "#f80", "#f08", "#0f8", "#8f0", "#80f", "#08f","#800", "#880", "#080", "#088", "#008", "#808" };
 	private static double width;
 	private static double cX, cY;
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if (args.length!=2) {
 			logger.log(Level.SEVERE, "usage: <logdir> <outfile>");
 			System.exit(-1);			
 		}
 		try {
 			double cLat=52.987253, cLon=-1.8895595;
 			cX = Mercator.mercX(cLon);
 			cY = Mercator.mercY(cLat);
 			//http://maps.googleapis.com/maps/api/staticmap?center=40.714728,-73.998672&zoom=12&size=400x400&maptype=satellite&sensor=false
 			//INFO: lon: -1.896922 to -1.882197, lat: 52.984308 to 52.990198
 			//http://maps.googleapis.com/maps/api/staticmap?center=52.987253,-1.8895595&zoom=14&size=640x640&maptype=satellite&sensor=false
 			List<UserData> users = LogReader.readLogs(new File(args[0]));
 			
 			// zoom level - 2^zoom level tiles in x & y
 			int zoom = 15;
 			double imageWidth = 2*Mercator.mercX(180)/Math.pow(2, zoom)*640/256;
 			width = 1750;
 			
 			SvgFile svg = new SvgFile(new File(args[1]));
 			svg.desc("DrawTrails of automics data, "+new Date());
 			svg.print("<filter id=\"desaturate\"  primitiveUnits=\"objectBoundingBox\">");
 //			svg.print("<feImage x=\""+(500-width/2)+"\" y=\""+(500-width/2)+"\" width=\""+width+"\" height=\""+width+"\" xlink:href=\"altontowers-zoom15.png\"/>");
 			svg.print("<feImage x=\"0%\" y=\"0%\" width=\"100%\" height=\"100%\" xlink:href=\"altontowers-zoom15.png\"/>");
 			svg.print("<feColorMatrix type=\"matrix\" values=\"0.3333 0.3333 0.3333 0 0 "+
                     "0.3333 0.3333 0.3333 0 0 "+
                   "0.3333 0.3333 0.3333 0 0 "+
                 "0      0      0      1 0\"/>");
 			svg.print("<feColorMatrix type=\"matrix\" values=\"0.2 0 0 0 0 "+
                     "0 0.2 0 0 0 "+
                   "0 0 0.2 0 0 "+
                 "0      0      0      1 0\"/>");
 			svg.print("</filter>");
 			svg.print("<rect x=\""+SCALE*0.5*(1-imageWidth/width)+"\" y=\""+SCALE*0.5*(1-imageWidth/width)+"\" width=\""+SCALE*(imageWidth/width)+"\" height=\""+SCALE*(imageWidth/width)+"\" filter=\"url(#desaturate)\"/>");
 			for (UserData user : users) {
 				for (Position p : user.getPositions()) {
 					if (p.isTruncated())
 						continue;
 					
 					double x= p.getX();
 					double y= p.getY();
 					if (x<minX)
 						minX = x;
 					if (x>maxX)
 						maxX = x;
 					if (y<minY)
 						minY = y;
 					if (y>maxY)
 						maxY = y;
 					double lat= p.getLat();
 					double lon= p.getLon();
 					if (lat<minLat)
 						minLat = lat;
 					if (lat>maxLat)
 						maxLat = lat;
 					if (lon<minLon)
 						minLon = lon;
 					if (lon>maxLon)
 						maxLon = lon;
 				}
 			}
 			logger.log(Level.INFO,"Ranges, x: "+minX+" to "+maxX+", y: "+minY+" to "+maxY);
 			logger.log(Level.INFO,"Ranges, lon: "+minLon+" to "+maxLon+", lat: "+minLat+" to "+maxLat);
 			minX -= BORDER_M;
 			minY -= BORDER_M;
 			maxX += BORDER_M;
 			maxY += BORDER_M;
 			//hack!!
 			//maxX = minX+(maxY-minY);
 			//String stroke = "#ff0000";
 			float strokeWidth = 0.5f;
 			int ci = 0;
 			for (UserData user : users) {
 				ci = ci+1 % colors.length;
 				String stroke = colors[ci];
 				Position lp = null;
 				for (Position p : user.getPositions()) {
 					if (lp!=null && !lp.isTruncated() && !p.isTruncated()) {
 						svg.line(scaleX(lp.getX()), scaleY(lp.getY()), scaleX(p.getX()), scaleY(p.getY()), stroke, strokeWidth);
 					}
 					lp = p;
 					svg.circle(scaleX(p.getX()), scaleY(p.getY()), 3, stroke, 0.4f, null);
 				}
 				for (Event e : user.getEvents()) {
 //					e.get
 				}
 			}
 			svg.close();
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "Error creating "+args[1], e);
 		}		
 	}
 	private static float scaleX(double x) {
 		return (float)(SCALE*(0.5+(x-cX)/width));
 //		return (float)(SCALE*(x-minX)/(maxX-minX));
 	}
 	private static float scaleY(double y) {
		return (float)(SCALE*(0.5-(y-cY)/width));
 //		return (float)(SCALE*(y-minY)/(maxY-minY));
 	}
 
 }
