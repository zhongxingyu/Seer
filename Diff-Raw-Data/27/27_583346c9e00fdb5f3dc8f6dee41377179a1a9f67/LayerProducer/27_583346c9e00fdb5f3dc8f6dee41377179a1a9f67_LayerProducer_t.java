 /*
  * Created on May 1, 2006
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.reprap.geometry;
 
 import java.io.IOException;
 import javax.media.j3d.*;
 import org.reprap.Printer;
 import org.reprap.Preferences;
 import org.reprap.ReprapException;
 import org.reprap.geometry.polygons.*;
 
 public class LayerProducer {
 	private static int gapMaterial = 0;
 	private static int solidMaterial = 1;
 	public static int gapMaterial() { return gapMaterial; }
 	public static int solidMaterial() { return solidMaterial; }
 	private Shape3D lowerShell;
 
 	private Printer printer;
 	private RrPolygonList hatchedPolygons;
 	private RrPolygonList borderPolygons;
 	
 	private RrCSGPolygon csg_p;
 	private double scale;
 	private Rr2Point p_0;
 	private Rr2Point pos;
 		
 	/**
 	 * @param reprap
 	 * @param list 
 	 * @param hatchDirection
 	 */
 	public LayerProducer(Printer printer, RrCSGPolygon csgPol, Shape3D ls, RrHalfPlane hatchDirection) {
 		this.printer = printer;
		
		// Uncomment the next line to replace lower layers with shell triangles.
 		//printer.setLowerShell(ls);
 		
 		RrCSGPolygon offBorder = csgPol.offset(-0.5*printer.getExtrusionSize());
 		RrCSGPolygon offHatch = csgPol.offset(-1.5*printer.getExtrusionSize());
 		
 		offBorder.divide(Preferences.tiny(), 1.01);
 		offHatch.divide(Preferences.tiny(), 1.01);
 		
 		//RrGraphics g = new RrGraphics(offBorder, true);
 		
 		borderPolygons = offBorder.megList(solidMaterial, solidMaterial);
 		
 		hatchedPolygons = new RrPolygonList();
 		hatchedPolygons.add(offHatch.hatch(hatchDirection, printer.getInfillWidth(), 
 				solidMaterial, gapMaterial));	
 	
 //		RrPolygonList pllist = new RrPolygonList();
 //		pllist.add(borderPolygons);
 //		pllist.add(hatchedPolygons);
 //		RrGraphics g = new RrGraphics(pllist, false);
 
 		csg_p = null;
 		
 		RrBox big = csgPol.box().scale(1.1);
 		
 		double width = big.x().length();
 		double height = big.y().length();
 	}
 	
 	private void plot(Rr2Point a) throws ReprapException, IOException
 	{
 		if (printer.isCancelled()) return;
 		printer.printTo(a.x(), a.y(), printer.getZ());
 		pos = a;
 	}
 
 	private void move(Rr2Point a) throws ReprapException, IOException
 	{
 		if (printer.isCancelled()) return;
 		printer.moveTo(a.x(), a.y(), printer.getZ());
 		pos = a;
 	}
 
 
 	/**
 	 * Plot a polygon
 	 * @throws IOException
 	 * @throws ReprapException
 	 */
 	private void plot(RrPolygon p) throws ReprapException, IOException
 	{
 		if(p.size() <= 0)
 			return;
 		
 		int stopExtruding = p.backStep(printer.getOverRun());
 		
 		int leng = p.size();
 		
 		if (printer.isCancelled()) return;
 		move(p.point(0));
 		plot(p.point(0));
 		// Print any lead-in.
 		printer.printStartDelay(printer.getDelay());
 		
 		for(int j = 1; j <= leng; j++)
 		{
 			if (printer.isCancelled()) return;
 			
 			int i = j%leng;
 			int f = p.flag(i);
 			
 			if(f != gapMaterial && j <= stopExtruding)
 				plot(p.point(i));
 			else
 				move(p.point(i));
 		}
 	}
 		
 	/**
 	 * Master plot function - draw everything
 	 * @throws IOException
 	 * @throws ReprapException
 	 */
 	public void plot() throws ReprapException, IOException
 	{
 		printer.setLowerShell(lowerShell);
 		int i;
 		
		borderPolygons = borderPolygons.filterShorts(Preferences.machineResolution()*2);
 		for(i = 0; i < borderPolygons.size(); i++) 
 		{
 			if (printer.isCancelled())
 				break;
 			plot(borderPolygons.polygon(i));
 		}
 		
		hatchedPolygons = hatchedPolygons.filterShorts(Preferences.machineResolution()*2);
 		for(i = 0; i < hatchedPolygons.size(); i++) 
 		{
 			if (printer.isCancelled())
 				break;
 			plot(hatchedPolygons.polygon(i));
 		}
 	}
 	
 }
