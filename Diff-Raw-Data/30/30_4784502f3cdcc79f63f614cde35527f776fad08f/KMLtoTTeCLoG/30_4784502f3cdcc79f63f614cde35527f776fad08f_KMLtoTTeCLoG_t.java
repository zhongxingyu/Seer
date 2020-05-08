 /**
  * 
  */
 package ecologylab.standalone;
 
 import java.io.File;
 import java.util.ArrayList;
 
import rogue.map.EnhancedGameTerrain;
import rogue.map.GPSField;
import rogue.map.WiFiField;
import ecologylab.generic.Debug;
 import ecologylab.sensor.location.gps.data.GeoCoordinate;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.XMLTranslationException;
 import ecologylab.xml.library.geom.Ellipse2DDoubleState;
 import ecologylab.xml.library.geom.Point2DDoubleState;
 import ecologylab.xml.library.geom.PolygonState;
 import ecologylab.xml.library.geom.Rectangle2DDoubleState;
import ecologylab.xml.library.geom.RectangularShape;
 import ecologylab.xml.library.kml.KMLTranslations;
import ecologylab.xml.library.kml.Kml;
 import ecologylab.xml.library.kml.feature.Placemark;
import ecologylab.xml.library.kml.feature.container.Container;
import ecologylab.xml.library.kml.feature.container.Folder;
 import ecologylab.xml.types.element.ArrayListState;
 
 /**
  * @author Alan Blevins
  * 
  */
 public class KMLtoTTeCLoG
 {
 	/**
 	 * @param args
 	 * @throws XMLTranslationException 
 	 */
 	public static void main(String[] args) throws XMLTranslationException
 	{
 		Kml k =  (Kml) ElementState.translateFromXML(new File("/u/ablev/kml/fulltestmap.kml"), KMLTranslations.get());
 	
 		//k.translateToXML(System.out);
 		ArrayListState<Point2DDoubleState>	   seekerSpawns = new ArrayListState<Point2DDoubleState>();
 		ArrayListState<PolygonState> 		   walls      = new ArrayListState<PolygonState>();
		ArrayListState<RectangularShape>   bases      = new ArrayListState<RectangularShape>();
 		ArrayListState<Ellipse2DDoubleState>   gps  	  = new ArrayListState<Ellipse2DDoubleState>();
 		ArrayListState<Ellipse2DDoubleState>   wiFi 	  = new ArrayListState<Ellipse2DDoubleState>();
 		ArrayListState<GPSField>   gpsFields  = new ArrayListState<GPSField>();
 		ArrayListState<WiFiField>   wiFiFields = new ArrayListState<WiFiField>();
 		Rectangle2DDoubleState                 playfield  = new Rectangle2DDoubleState();
 		Point2DDoubleState	NW = new Point2DDoubleState();
 		Point2DDoubleState SE = new Point2DDoubleState();
 		ArrayListState<Point2DDoubleState> pntlist = new ArrayListState<Point2DDoubleState>();
 		
 		if(k != null) 
 		{
 			System.out.println(k.getDocument().translateToXML());
 			ArrayList<Folder> f = ((Container) k.getDocument()).getFolders();
 			ArrayList<Placemark> p;
 			ArrayList<GeoCoordinate> gco;
 			PolygonState pst;
 			int idbg = 0;
 			for(Folder fi : f) 
 			{
 				p = fi.getPlacemarks();
 				for(Placemark pi : p)
 				{
 					//System.out.println(pi.getName());
 					if(pi.getName().equals("block"))
 					{
 						pst = new PolygonState();
 						pntlist.clear();
 						gco = pi.getPolygon().getCoordinates().getCoordinateList();
 						for(GeoCoordinate gc : gco)
 						{
 							pntlist.add(new Point2DDoubleState(gc.getLon(),gc.getLat()));
 						}
 						walls.add(new PolygonState(pntlist));
 					}
 					else if(pi.getName().equals("base"))
 					{
 						gco = pi.getPoint().getCoordinates().getCoordinateList();
 						for(GeoCoordinate gc : gco)
 						{
 							bases.add(new Ellipse2DDoubleState(gc.getLon(), gc.getLat(), Integer.parseInt(pi.getDescription()), Integer.parseInt(pi.getDescription())));
 						}
 					}
 					else if(pi.getName().equals("gps"))
 					{
 						gco = pi.getPoint().getCoordinates().getCoordinateList();
 						for(GeoCoordinate gc : gco)
 						{
 							gpsFields.add(new GPSField(Integer.parseInt(pi.getDescription()), gc.getLon(), gc.getLat(), 400, 400));
 						}
 					}
 					else if(pi.getName().equals("wifi"))
 					{
 						gco = pi.getPoint().getCoordinates().getCoordinateList();
 						for(GeoCoordinate gc : gco)
 						{
 							wiFi.add(new Ellipse2DDoubleState(gc.getLon(), gc.getLat(), Integer.parseInt(pi.getDescription()), Integer.parseInt(pi.getDescription())));
 						}
 					}
 					else if(pi.getName().equals("spawn"))
 					{
 						gco = pi.getPoint().getCoordinates().getCoordinateList();
 						for(GeoCoordinate gc : gco)
 						{
 							seekerSpawns.add(new Point2DDoubleState(gc.getLon(),gc.getLat()));
 						}
 					}
 					else if(pi.getName().equals("goal"))
 					{
 						// Nothing right now, but surely you'd like to pre-set goals in the future
 					}
 					else if(pi.getName().equals("NW"))
 					{
 						gco = pi.getPoint().getCoordinates().getCoordinateList();
 						for(GeoCoordinate gc : gco)
 						{
 							NW.setLocation(gc.getLon(),gc.getLat());
 						}
 					}
 					else if(pi.getName().equals("SE"))
 					{
 						gco = pi.getPoint().getCoordinates().getCoordinateList();
 						for(GeoCoordinate gc : gco)
 						{
 							SE.setLocation(gc.getLon(),gc.getLat());
 						}
 					}
 				}
 			}
 		}
 		// And so now, everything should be set all nice-like......
 		
 		// A note:
 		// This fits the data to a square, whether the area between
 		// NW and SE is a square or not. This is because TTeCLoG has
 		// issues with drawing non-square areas correctly.
 		//
 		// The moral of this story is, make sure your gameplay area
 		// in Google Earth is squarish in nature.
 		//
 		double WEsplit = SE.x() - NW.x();
 		double NSsplit = NW.y() - SE.y();
 		
 		//System.out.println("W-E split = " + Double.toString(NW.x() - SE.x()));
 		//System.out.println("N-S split = " + Double.toString(NW.y() - SE.y()));
 		
 		Debug.println("W-E split = " + Double.toString(NW.x() - SE.x()));
 		Debug.println("N-S split = " + Double.toString(NW.y() - SE.y()));
 		
 		WEsplit = 1000.0/WEsplit;
 		NSsplit = 1000.0/NSsplit;
 		
 		for(Point2DDoubleState p2dd : seekerSpawns)
 		{
 			p2dd.setLocation(Math.floor((p2dd.x()-NW.x())*WEsplit),Math.floor((NW.y()-p2dd.y())*NSsplit));
 		}
 		for(PolygonState pgst : walls)
 		{
 			pntlist.clear();
 			pntlist.addAll(pgst.getPolygonVerticies());
 			for(Point2DDoubleState p2dd : pntlist)
 			{
 				p2dd.setLocation(Math.floor((p2dd.x()-NW.x())*WEsplit),Math.floor((NW.y()-p2dd.y())*NSsplit));
 			}
 			pgst.definePolygon(pntlist);
 		}
		for(RectangularShape e2dd : bases)
 		{
 			e2dd.setFrame(Math.floor((e2dd.getX()-NW.x())*WEsplit),Math.floor((NW.y()-e2dd.getY())*NSsplit), e2dd.getWidth(), e2dd.getHeight());
 		}
 		for(GPSField gpf : gpsFields)
 		{
 			gpf.setFrame(Math.floor(((gpf.getX()-NW.x())*WEsplit)-200), Math.floor(((NW.y()-gpf.getY())*NSsplit)-200), 400.0, 400.0);
 		}
 		for(Ellipse2DDoubleState e2dd : wiFi)
 		{
 			for(int i = 1; i < 5; i++)
 				wiFiFields.add(new WiFiField(i, Math.floor(((e2dd.getX()-NW.x())*WEsplit)-e2dd.getWidth()/(2*i)), Math.floor(((NW.y()-e2dd.getY())*NSsplit)-e2dd.getHeight()/(2*i)), Math.floor(e2dd.getWidth()/i), Math.floor(e2dd.getHeight()/i)));
 		}
 		
 		playfield.setFrame(0, 0, 1000, 1000);
 		
 		EnhancedGameTerrain toXML = new EnhancedGameTerrain();
 		
 		toXML.setBases(bases);
 		toXML.setPlayfield(playfield);
 		toXML.setSeekerSpawns(seekerSpawns);
 		toXML.setWalls(walls);
 		toXML.setWiFiFields(wiFiFields);
 		toXML.setGPSField(gpsFields);
 		
 		System.out.println(toXML.translateToXML());
 		try
 		{
 		toXML.translateToXML(new File("/u/ablev/kml/fulltestmap.xml"));
 		}
 		catch(Exception e)
 		{
 		}
 		
 	}
 }
