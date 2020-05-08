 /*
  * Created on May 1, 2006
  *
  * Changed by Vik to reject polts of less than 0.05mm
  */
 package org.reprap.geometry;
 
 import java.io.IOException;
 import javax.media.j3d.*;
 import org.reprap.Printer;
 import org.reprap.Attributes;
 import org.reprap.Preferences;
 import org.reprap.ReprapException;
 import org.reprap.geometry.polygons.*;
 import org.reprap.devices.pseudo.LinePrinter;
 import org.reprap.utilities.Debug;
 import org.reprap.geometry.Producer;
 
 /**
  *
  */
 class segmentSpeeds
 {
 	/**
 	 * 
 	 */
 	public Rr2Point p1, p2, p3;
 	
 	/**
 	 * 
 	 */
 	public double ca;
 	
 	/**
 	 * 
 	 */
 	public boolean plotMiddle;
 	
 	/**
 	 * 
 	 */
 	public boolean abandon;
 	
 	/**
 	 * @param before
 	 * @param now
 	 * @param after
 	 * @param fastLength
 	 */
 	public segmentSpeeds(Rr2Point before, Rr2Point now, Rr2Point after, double fastLength)
 	{
 		Rr2Point a = Rr2Point.sub(now, before);
 		double amod = a.mod();
 		abandon = amod == 0;
 		if(abandon)
 			return;
 		Rr2Point b = Rr2Point.sub(after, now);
 		if(b.mod() == 0)
 			ca = 0;
 		else
 			ca = Rr2Point.mul(a.norm(), b.norm());
 		plotMiddle = true;
 		if(amod <= 2*fastLength)
 		{
 			fastLength = amod*0.5;
 			plotMiddle = false;
 		}
 		a = a.norm();
 		p1 = Rr2Point.add(before, Rr2Point.mul(a, fastLength));
 		p2 = Rr2Point.add(p1, Rr2Point.mul(a, amod - 2*fastLength));
 		p3 = Rr2Point.add(p2, Rr2Point.mul(a, fastLength));
 	}
 	
 	int speed(int currentSpeed, double angFac)
 	{
 		double fac = (1 - 0.5*(1 + ca)*angFac);
 		return LinePrinter.speedFix(currentSpeed, fac);
 	}
 }
 
 /**
  *
  */
 public class LayerProducer {
 
 	/**
 	 * The shape of the object built so far under the current layer
 	 */
 	private BranchGroup lowerShell;
 
 	/**
 	 * 
 	 */
 	private Printer printer;
 	
 	/**
 	 * The polygons to infill
 	 */
 	private RrPolygonList hatchedPolygons;
 	
 	/**
 	 * The polygons to outline
 	 */
 	private RrPolygonList borderPolygons;
 	
 	/**
 	 * CSG representation of the polygons as input
 	 */
 	private RrCSGPolygon csg_p;
 	
 	/**
 	 * 
 	 */
 	//private double scale;
 	
 	/**
 	 * The height of the current layer
 	 */
 	private double z;
 	
 	
 	/**
 	 * Counters use so that each material is plotted completely before
 	 * moving on to the next.
 	 */
 	private int commonBorder, commonHatch;
 	
 	/**
 	 * The clue is in the name...
 	 */
 	private int currentSpeed;
 	
 	/**
 	 * 
 	 */
 	//private Rr2Point p_0;
 	
 	/**
 	 * 
 	 */
 	//private Rr2Point pos;
 	
 	/**
 	 * Count of how many up we are (the bottom is layer 0)
 	 */
 	private int layerNumber;
 	
 	/**
 	 * Record the end of each polygon as a clue where to start next
 	 */
 	private Rr2Point startNearHere;
 	
 		
 	/**
 	 * @param printer
 	 * @param zValue
 	 * @param csgPol
 	 * @param ls
 	 * @param hatchDirection
 	 */
 	public LayerProducer(Printer printer, double zValue, RrCSGPolygonList csgPols, 
 			BranchGroup ls, RrHalfPlane hatchDirection, int layerNo) {
 		
 		layerNumber = layerNo;
 		startNearHere = null;
 		
 		this.printer = printer;
 		lowerShell = ls;
 
 		z = zValue;
 		
 		RrCSGPolygonList offBorder = csgPols.offset(printer.getExtruders(), true);
 		RrCSGPolygonList offHatch = csgPols.offset(printer.getExtruders(), false);
 		
 		offBorder.divide(Preferences.tiny(), 1.01);
 		offHatch.divide(Preferences.tiny(), 1.01);
 		
 		//RrGraphics g = new RrGraphics(offBorder, true);
 		
 		borderPolygons = offBorder.megList();
 		RrPolygon last = borderPolygons.polygon(borderPolygons.size()-1);
 //		Rr2Point startNearHere = last.point(last.size() - 1);
 		Rr2Point startNearHere = last.point(1);
 		hatchedPolygons = new RrPolygonList();
 		hatchedPolygons.add(offHatch.hatch(hatchDirection, printer.getExtruders(), startNearHere));	
 	
 //		RrPolygonList pllist = new RrPolygonList();
 //		pllist.add(borderPolygons);
 //		pllist.add(hatchedPolygons);
 //		RrGraphics g = new RrGraphics(pllist, false);
 
 		csg_p = null;
 		
 		RrBox big = csgPols.box().scale(1.1);
 		
 		double width = big.x().length();
 		double height = big.y().length();
 	}
 	
 	/**
 	 * @return current X and Y position of the printer
 	 */
 	private Rr2Point posNow()
 	{
 		return new Rr2Point(printer.getX(), printer.getY());
 	}
 	
 	/**
 	 * speed up for short lines
 	 * @param p
 	 * @return
 	 * @throws ReprapException
 	 * @throws IOException
 	 */
 	private boolean shortLine(Rr2Point p) throws ReprapException, IOException
 	{
 		double shortLen = printer.getExtruder().getShortLength();
 		if(shortLen < 0)
 			return false;
 		Rr2Point a = Rr2Point.sub(posNow(), p);
 		double amod = a.mod();
 		if(amod > shortLen) {
 			Debug.d("Long segment.  Current speed is: " + currentSpeed);
 			return false;
 		}
 		printer.setSpeed(LinePrinter.speedFix(printer.getExtruder().getXYSpeed(), 
 				printer.getExtruder().getShortSpeed()));
 		printer.printTo(p.x(), p.y(), z, true);
 		printer.setSpeed(currentSpeed);
 		Debug.d("Short segment at speed " +
 				LinePrinter.speedFix(currentSpeed, printer.getExtruder().getShortSpeed())
 				+" derived from speed: " + currentSpeed);
 		return true;	
 	}
 	
 	/**
 	 * @param first First point, the start of the line segment to be plotted.
 	 * @param second Second point, the end of the line segment to be plotted.
 	 * @param turnOff True if the extruder should be turned off at the end of this segment.
 	 * @throws ReprapException
 	 * @throws IOException
 	 */
 	private void plot(Rr2Point first, Rr2Point second, boolean turnOff) throws ReprapException, IOException
 	{
 		if (printer.isCancelled()) return;
 		
 		if(shortLine(first))
 			return;
 		
 		double speedUpLength = printer.getExtruder().getAngleSpeedUpLength();
 		if(speedUpLength > 0)
 		{
 			segmentSpeeds ss = new segmentSpeeds(posNow(), first, second, 
 					speedUpLength);
 			if(ss.abandon)
 				return;
 
 			printer.printTo(ss.p1.x(), ss.p1.y(), z, false);
 
 			if(ss.plotMiddle)
 			{
 				int straightSpeed = LinePrinter.speedFix(currentSpeed, (1 - 
 						printer.getExtruder().getAngleSpeedFactor()));
 				printer.setSpeed(straightSpeed);
 				printer.printTo(ss.p2.x(), ss.p2.y(), z, false);
 			}
 
 			printer.setSpeed(ss.speed(currentSpeed, printer.getExtruder().getAngleSpeedFactor()));
 			printer.printTo(ss.p3.x(), ss.p3.y(), z, true);
 			//pos = ss.p3;
 		// Leave speed set for the start of the next line.
 		} else
 			printer.printTo(first.x(), first.y(), z, true);
 	}
 
 	/**
 	 * @param first
 	 * @param second
 	 * @param startUp
 	 * @param endUp
 	 * @throws ReprapException
 	 * @throws IOException
 	 */
 	private void move(Rr2Point first, Rr2Point second, boolean startUp, boolean endUp) 
 		throws ReprapException, IOException
 	{
 		if (printer.isCancelled()) return;
 		
 		if(startUp)
 		{
 			printer.setSpeed(printer.getFastSpeed());
 			printer.moveTo(first.x(), first.y(), z, startUp, endUp);
 			return;
 		}
 		
 		double speedUpLength = printer.getExtruder().getAngleSpeedUpLength();
 		if(speedUpLength > 0)
 		{
 			segmentSpeeds ss = new segmentSpeeds(posNow(), first, second, 
 					speedUpLength);
 			if(ss.abandon)
 				return;
 
 			printer.moveTo(ss.p1.x(), ss.p1.y(), z, startUp, startUp);
 
 			if(ss.plotMiddle)
 			{
 				printer.setSpeed(currentSpeed);
 				printer.moveTo(ss.p2.x(), ss.p2.y(), z, startUp, startUp);
 			}
 
 			printer.setSpeed(ss.speed(currentSpeed, printer.getExtruder().getAngleSpeedFactor()));
 			printer.moveTo(ss.p3.x(), ss.p3.y(), z, startUp, endUp);
 			//pos = ss.p3;
 			// Leave speed set for the start of the next movement.
 		} else
 			printer.moveTo(first.x(), first.y(), z, startUp, endUp);
 	}
 
 
 	/**
 	 * Plot a polygon
 	 * @throws IOException
 	 * @throws ReprapException
 	 * @return
 	 */
 	private void plot(RrPolygon p, boolean outline) throws ReprapException, IOException
 	{
 		int leng = p.size();
 		
 		if(leng <= 1)
 		{
 			startNearHere = null;
 			return;
 		}
 		// If the length of the plot is <0.05mm, don't bother with it.
 		// This will not spot an attempt to plot 10,000 points in 1mm.
 		double plotDist=0;
 		Rr2Point lastPoint=p.point(0);
 		for (int i=1; i<leng; i++)
 		{
 			Rr2Point n=p.point(i);
 			plotDist+=Rr2Point.d(lastPoint, n);
 			lastPoint=n;
 		}
 		if (plotDist<Preferences.machineResolution()*0.5) {
 			Debug.d("Rejected line with "+leng+"points, length"+plotDist);
 			startNearHere = null;
 			return;
 		}
 
 		
 		Attributes att = p.getAttributes();
 		
 		int baseSpeed = att.getExtruder(printer.getExtruders()).getXYSpeed();
 		int outlineSpeed = LinePrinter.speedFix(baseSpeed, 
 				att.getExtruder(printer.getExtruders()).getOutlineSpeed());
 		int infillSpeed = LinePrinter.speedFix(baseSpeed, 
 				att.getExtruder(printer.getExtruders()).getInfillSpeed());
 			
 		
 		printer.selectExtruder(att);
 		
 		// Warning: if incrementedStart is activated, this will override the randomStart
 		if(outline && printer.getExtruder().incrementedStart())
 			p = p.incrementedStart(layerNumber);
 		else if(outline && printer.getExtruder().randomStart())
 			p = p.randomStart();
 		
 		// The last point is near where we want to start next
		
		startNearHere = p.point(p.size() - 1);
 		
 		int stopExtruding = leng + 10;
 		double backLength = printer.getExtruder().getExtrusionOverRun();
 		if(backLength > 0)
 			stopExtruding = p.backStep(backLength, outline);
 		
 		if (printer.isCancelled()) return;
 		
 		printer.setSpeed(printer.getFastSpeed());
 		move(p.point(0), p.point(1), true, false);
 
 		plot(p.point(0), p.point(1), false);
 		// Print any lead-in.
 		if (outline) printer.printStartDelay(printer.getExtruder().getExtrusionDelayForBorder());
 		else printer.printStartDelay(printer.getExtruder().getExtrusionDelayForHatch());
 				
 		if(outline)
 		{
 			printer.setSpeed(outlineSpeed);
 			currentSpeed = outlineSpeed;			
 		} else
 		{
 			printer.setSpeed(infillSpeed);
 			currentSpeed = infillSpeed;			
 		}
 		
 		if(outline)
 		{
 			for(int j = 1; j <= p.size(); j++)
 			{
 				int i = j%p.size();
 				Rr2Point next = p.point((j+1)%p.size());
 				
 				if (printer.isCancelled())
 				{
 					printer.stopExtruding();
 					move(posNow(), posNow(), true, true);
 					return;
 				}
 				
 				if(j > stopExtruding)
 				{
 					printer.stopExtruding();
 					move(p.point(i), next, false, false);
 				} else
 					plot(p.point(i), next, false);
 			}
 		} else
 		{
 			for(int i = 1; i < p.size(); i++)
 			{
 				Rr2Point next = p.point((i+1)%p.size());
 				
 				if (printer.isCancelled())
 				{
 					printer.stopExtruding();
 					move(posNow(), posNow(), true, true);
 					return;
 				}
 				
 				if(i > stopExtruding)
 				{
 					printer.stopExtruding();
 					move(p.point(i), next, false, false);
 				} else
 					plot(p.point(i), next, false);
 			}
 		}
 		
 		move(posNow(), posNow(), true, true);
 		
 	}
 	
 
 
 	private int plotOneMaterial(RrPolygonList polygons, int i, boolean outline)
 		throws ReprapException, IOException
 	{
 		String material = polygons.polygon(i).getAttributes().getMaterial();
 		
 		while(i < polygons.size() && polygons.polygon(i).getAttributes().getMaterial().equals(material))
 		{
 			if (printer.isCancelled())
 				return i;
 			plot(polygons.polygon(i), outline);
 			i++;
 		}
 		return i;
 	}
 	
 	private boolean nextCommon(int ib, int ih)
 	{
 		commonBorder = ib;
 		commonHatch = ih;
 		
 		for(int jb = ib; jb < borderPolygons.size(); jb++)
 		{
 			for(int jh = ih; jh < hatchedPolygons.size(); jh++)
 			{
 				if(borderPolygons.polygon(ib).getAttributes().getMaterial().equals(
 						hatchedPolygons.polygon(ih).getAttributes().getMaterial()))
 				{
 					commonBorder = jb;
 					commonHatch = jh;
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 		
 	/**
 	 * Master plot function - draw everything
 	 * @throws IOException
 	 * @throws ReprapException
 	 */
 	public void plot() throws ReprapException, IOException
 	{
 		int ib, jb, ih, jh;
 		
 		//borderPolygons = borderPolygons.filterShorts(Preferences.machineResolution()*2);
 		//hatchedPolygons = hatchedPolygons.filterShorts(Preferences.machineResolution()*2);
 		
 		ib = 0;
 		ih = 0;
 		
 		while(nextCommon(ib, ih)) 
 		{	
 			for(jb = ib; jb < commonBorder; jb++)
 			{
 				if (printer.isCancelled())
 					break;
 				plot(borderPolygons.polygon(jb), true);
 			}
 			ib = commonBorder;
 
 			for(jh = ih; jh < commonHatch; jh++)
 			{
 				if (printer.isCancelled())
 					break;
 				plot(hatchedPolygons.polygon(jh), false);
 			}
 			ih = commonHatch;
 			
 			ib = plotOneMaterial(borderPolygons, ib, true);
 			hatchedPolygons = hatchedPolygons.nearEnds(startNearHere);
 			ih = plotOneMaterial(hatchedPolygons, ih, false);	
 		}
 		
 		for(jb = ib; jb < borderPolygons.size(); jb++)
 		{
 			if (printer.isCancelled())
 				break;
 			plot(borderPolygons.polygon(jb), true);			
 		}
 		
 		for(jh = ih; jh < commonHatch; jh++)
 		{
 			if (printer.isCancelled())
 				break;
 			plot(hatchedPolygons.polygon(jh), false);
 		}
 
 		printer.setLowerShell(lowerShell);
 	}		
 	
 }
