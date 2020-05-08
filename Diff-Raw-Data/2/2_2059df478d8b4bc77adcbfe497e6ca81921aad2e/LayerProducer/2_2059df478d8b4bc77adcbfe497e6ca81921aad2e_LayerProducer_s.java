 /*
  * Created on May 1, 2006
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.reprap.geometry;
 
 import java.io.IOException;
 
 import org.reprap.Printer;
 import org.reprap.ReprapException;
 import org.reprap.geometry.polygons.*;
 
 public class LayerProducer {
 	private static final double resolution = 1.0e-6; // How close (in mm^2) are two points before they're the same?
 	private static int gapMaterial = 0;
 	private static int solidMaterial = 1;
 	
 
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
 	public LayerProducer(Printer printer, RrPolygonList list, RrLine hatchDirection) {
 		this.printer = printer;
 
 		borderPolygons = list;
 		
 		RrPolygon hatched = list.hatch(hatchDirection, printer.getExtrusionSize(),
 				gapMaterial, solidMaterial);
 
 		hatchedPolygons = new RrPolygonList();
 		hatchedPolygons.add(hatched);
 		
 		//new RrGraphics(p_list, false);
 		
 		csg_p = null;
 		
 		RrBox big = hatchedPolygons.box.scale(1.1);
 		
 		double width = big.x().length();
 		double height = big.y().length();
 	}
 	
 	/**
 	 * @param reprap
 	 * @param list
 	 * @param hatchDirection
 	 */
 	public LayerProducer(Printer printer, RrCSGPolygon csgPol, RrLine hatchDirection) {
 		this.printer = printer;
 		
 		
 		RrCSGPolygon offBorder = csgPol.offset(-0.5*printer.getExtrusionSize());
 		RrCSGPolygon offHatch = csgPol.offset(-1.5*printer.getExtrusionSize());
 		
 		offBorder.divide(resolution, 1);
 		offHatch.divide(resolution, 1);
 				
		borderPolygons = offBorder.megList(solidMaterial, gapMaterial);
 		
 		hatchedPolygons = new RrPolygonList();
 		hatchedPolygons.add(offHatch.hatch_join(hatchDirection, printer.getExtrusionSize(), 
 				solidMaterial, gapMaterial));
 		
 		//new RrGraphics(p_list, false);
 		
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
 		int leng = p.size();
 		if(leng <= 0)
 			return;
 		for(int j = 0; j <= leng; j++)
 		{
 			int i = j%leng;
 			int f = p.flag(i);
 			if(f != 0 && j != 0)
 			{
 				if (printer.isCancelled()) return;
 				plot(p.point(i));
 			} else
 				if (printer.isCancelled()) return;
 				move(p.point(i)); 
 		}
 	}
 	
 	/**
 	 * Plot a section of parametric line
 	 * @throws IOException
 	 * @throws ReprapException
 	 */
 	private void plot(RrLine a, RrInterval i) throws ReprapException, IOException
 	{
 		if(i.empty()) return;
 		if (printer.isCancelled()) return;
 		move(a.point(i.low()));
 		if (printer.isCancelled()) return;
 		plot(a.point(i.high()));
 	}
 	
 	/**
 	 * Plot a set in a box
 	 * @throws IOException
 	 * @throws ReprapException
 	 */
 	private void plotLeaf(RrCSGPolygon p) throws ReprapException, IOException
 	{
 		RrQContents qc = new RrQContents(p);
 		
 		if (printer.isCancelled()) return;		
 		if(qc.l1 != null)
 			plot(qc.l1, qc.i1);
 		
 		if (printer.isCancelled()) return;
 		if(qc.l2 != null)
 			plot(qc.l2, qc.i2);
 
 	}
 	
 	/**
 	 * Plot a divided CSG polygon recursively
 	 * @throws IOException
 	 * @throws ReprapException
 	 */
 	private void plot(RrCSGPolygon p) throws ReprapException, IOException
 	{
 		if(p.c_1() == null)
 		{
 			if (printer.isCancelled()) return;
 			plotLeaf(p);
 		} else
 		{
 			if (printer.isCancelled()) return;
 			plot(p.c_1());
 			if (printer.isCancelled()) return;
 			plot(p.c_2());
 			if (printer.isCancelled()) return;
 			plot(p.c_3());
 			if (printer.isCancelled()) return;
 			plot(p.c_4());
 		}
 	}
 	
 	/**
 	 * Master plot function - draw everything
 	 * @throws IOException
 	 * @throws ReprapException
 	 */
 	public void plot() throws ReprapException, IOException
 	{
 		if (hatchedPolygons == null)
 			plot(csg_p);
 		else {
 			int leng = borderPolygons.size();
 			for(int i = 0; i < leng; i++) {
 				plot(borderPolygons.polygon(i));
 			}			
 			leng = hatchedPolygons.size();
 			for(int i = 0; i < leng; i++) {
 				if (printer.isCancelled())
 					break;
 				plot(hatchedPolygons.polygon(i));
 			}
 		}
 	}
 	
 }
