 package com.deev.interaction.uav3i.ui;
 
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Path2D;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import org.openstreetmap.gui.jmapviewer.Coordinate;
 
 import com.deev.interaction.common.ui.Touchable;
 import com.deev.interaction.uav3i.model.UAVDataPoint;
 import com.deev.interaction.uav3i.model.UAVDataStore;
 
 import eu.telecom_bretagne.uav3i.UAV3iSettings;
 import eu.telecom_bretagne.uav3i.UAV3iSettings.Mode;
 import uk.me.jstott.jcoord.LatLng;
 
 @SuppressWarnings("serial")
 public class SymbolMap extends Map implements Touchable
 {		
 	private ArrayList<Manoeuver> _manoeuvers = null;
 	private Manoeuver _adjustingMnvr = null;
 	private Object _adjustingTouch = null;
 	
 	private ArrayList<Touchable> _touchsymbols;
 	private HashMap<Object, Touchable> _touchedsymbols;
 
 	private Trajectory _trajectory;
 	private long _lastTrajectoryUpdate = 0;
 
 
 	// Dessin de UAV
 	protected Path2D.Double _tri;
 	protected static double D = 10.;
 
 	public SymbolMap()
 	{
 		super();
 
 		setVisible(true);
 		setOpaque(false);
 		Color back = new Color(0.f, 0.f, 0.f, .0f);
 		setBackground(back);	
 
 		_manoeuvers = new ArrayList<Manoeuver>();
 		_touchsymbols = new ArrayList<Touchable>();
 		_touchedsymbols = new HashMap<Object, Touchable>();
 		
 		_trajectory = new Trajectory();
 
 		// Dessin UAV
 		_tri = new Path2D.Double();
 		_tri.moveTo(D/2., 0.);
 		_tri.lineTo(-D/2., D/2.);
 		_tri.lineTo(-D/3., 0.);
 		_tri.lineTo(-D/2., -D/2.);
 		_tri.closePath();
 	}
 	
 	public void addTouchSymbol(Touchable t)
 	{
 		synchronized (_touchsymbols)
 		{
 			_touchsymbols.add(t);
 		}
 	}
 	
 	public void removeTouchSymbol(Touchable t)
 	{
 		synchronized (_touchsymbols)
 		{
 			_touchsymbols.remove(t);
 			for (Entry<Object,Touchable> e : _touchedsymbols.entrySet())
 				if (e.getValue() == t)
 					_touchedsymbols.entrySet().remove(e);
 		}
 	}
 
 	public void paintComponent(Graphics g)
 	{
 		Graphics2D g2 = (Graphics2D) g;
 		paint(g2);
 	}
 
 	public synchronized void paint(Graphics2D g2)
 	{	
 		long currentTime = System.currentTimeMillis();
 
 		RenderingHints rh;
 		rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 
 		g2.setRenderingHints(rh);
 
 		// Update de trajectoire
 		if (currentTime - _lastTrajectoryUpdate > 500)
 		{
 			_trajectory.update();
 			_lastTrajectoryUpdate = currentTime;
 		}
 
 		// Tracé de trajectoire
 		GeneralPath fullTrajectory = _trajectory.getFullPath(this);	
 
 		if (fullTrajectory != null)
 		{
 			g2.setPaint(new Color(1.f, 1.f, 1.f, .5f));
 			// g2.setPaint(new Color(0.f, 0.f, 0.f, .3f));
 			g2.setStroke(new BasicStroke(5.f, BasicStroke.CAP_BUTT,
 					BasicStroke.JOIN_ROUND));
 			g2.draw(fullTrajectory);
 			g2.setPaint(Color.RED);
 			g2.setStroke(new BasicStroke(2.f, BasicStroke.CAP_BUTT,
 					BasicStroke.JOIN_ROUND));
 			g2.draw(fullTrajectory);
 		}
 
 		// Dessin UAV
 		AffineTransform old = g2.getTransform();	
 
 		UAVDataPoint uavpoint = UAVDataStore.getDataPointAtTime(System.currentTimeMillis());
 		if (uavpoint != null)
 		{
 			Point2D.Double uav = getScreenForLatLng(uavpoint.latlng);
 			double course = Math.PI/2. - uavpoint.course/180.*Math.PI;
 			g2.translate(uav.x, uav.y);
 			g2.rotate(-course);
 			g2.setPaint(new Color(0.f, 0.f, 0.f, .35f));
 			g2.setStroke(new BasicStroke(5.f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
 			g2.draw(_tri);
 			g2.setPaint(new Color(.3f, .6f, 1.f, 1.f));
 			g2.fill(_tri);
 			g2.setPaint(Color.WHITE);
 			g2.setStroke(new BasicStroke(1.f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
 			g2.draw(_tri);
 		}
 		g2.setTransform(old);
 
 		// --------- Manoeuvers --------------------------------------------------
 		synchronized(this)
 		{
 			for (Manoeuver m : _manoeuvers)
 				m.paint(g2);
 		}
 
 	}
 
 	@Override
 	public double getPPM()
 	{
 		return 1. / MainFrame.OSMMap.getMapViewer().getMeterPerPixel();
 	}
 
 	public Point2D.Double getScreenForLatLng(LatLng latlng)
 	{	
 		Point a = MainFrame.OSMMap.getMapViewer().getMapPosition(latlng.getLat(), latlng.getLng(), false);
 		Point b = new Point(a.x+1, a.y+1);
 		
 		LatLng A = getLatLngForScreen(a.x, a.y);
 		LatLng B = getLatLngForScreen(b.x, b.y);
 		
 		double X = (double) a.x + (latlng.getLng()-A.getLng()) / (B.getLng()-A.getLng());
 		double Y = (double) a.y + (latlng.getLat()-A.getLat()) / (B.getLat()-A.getLat());
 		
 		return new Point2D.Double(X, Y);
 	}
 
 	public LatLng getLatLngForScreen(double x, double y)
 	{		
 		int X = (int) x;
 		int Y = (int) y;
 
 		Coordinate A = MainFrame.OSMMap.getMapViewer().getPosition(X, Y);
 		Coordinate B = MainFrame.OSMMap.getMapViewer().getPosition(X+1, Y+1);
 
 		double lat = A.getLat() + (y-(double)Y) * (B.getLat()-A.getLat());
 		double lon = A.getLon() + (x-(double)X) * (B.getLon()-A.getLon());
 		
 		return new LatLng(lat, lon);
 	}
 
	/*
	public double getPixelPerDegree()
	{
		LatLng a_ll = getLatLngForScreen(0, 0);
		LatLng b_ll = getLatLngForScreen(1000, 0);

		return 1000./(b_ll.getLng() - a_ll.getLng());
	}
	 */
 
 	public boolean adjustAtPx(double x, double y)
 	{
 		if (_adjustingMnvr == null)
 			synchronized(this)
 			{
 				for (Manoeuver m : _manoeuvers)
 					if (m.isAdjustmentInterestedAtPx(x, y))
 						_adjustingMnvr = m;
 						
 			}
 		
 		// On demande au manoeuver de s'ajuster et on récupère la valeur booléenne
 		// du résultat pour la renvoyer ensuite même si elle n'est pas utilisée...
 		boolean result = _adjustingMnvr.adjustAtPx(x, y);
 
 		// Si on est connecté à Paparazzi...
 		if(UAV3iSettings.getMode() == Mode.IVY)
 		{
 			switch (_adjustingMnvr.getClass().getSimpleName())
 			{
 				case "CircleMnvr":
 					// Signalement à Paparazzi de la modification du rayon.
 					// TODO utilité de la transmission à chaque modification ? Attendre une à 2 secondes que le rayon soit stabilisé ?
 					UAVDataStore.getIvyCommunication().setNavRadius(((CircleMnvr)_adjustingMnvr).getCurrentRadius());
 					break;
 				case "LineMnvr":
 					LineMnvr lineMnvr = (LineMnvr)_adjustingMnvr;
 					LatLng A = lineMnvr.getTrajA();
 					LatLng B = lineMnvr.getTrajB();
 					UAVDataStore.getIvyCommunication().moveWayPoint("1", lineMnvr.getTrajA());
 					UAVDataStore.getIvyCommunication().moveWayPoint("2", lineMnvr.getTrajB());
 					break;
 				default:
 					break;
 			}
 		}
 
 
 		return result;
 	}
 
 	public boolean isAdjusting()
 	{
 		if (_adjustingMnvr == null)
 			return false;
 
 		return _adjustingMnvr.isAdjusting();
 	}
 
 	public void stopAdjusting()
 	{
 		if (_adjustingMnvr == null)
 			return;
 
 		_adjustingMnvr.stopAdjusting();
 		_adjustingMnvr = null;
 	}
 
 
 	@Override
 	public void alignWith(Map map)
 	{
 		super.alignWith(map);
 		//		if (_zoneSTracker != null)
 		//			_zoneSTracker.align();
 	}
 
 	@Override
 	public float getInterestForPoint(float x, float y)
 	{
 		synchronized(this)
 		{
 			for (Manoeuver m : _manoeuvers)
 				if (m.isAdjustmentInterestedAtPx(x, y))
 					return Manoeuver.ADJUST_INTEREST;			
 		}
 
 		synchronized (_touchsymbols)
 		{
 			float interest = -1.f;;
 			
 			Iterator<Touchable> itr = _touchsymbols.iterator();
 			while(itr.hasNext())
 			{
 				Touchable t = itr.next();
 				float i = t.getInterestForPoint(x, y);
 				if (i > interest)
 				{
 					interest = i;
 				}
 			}
 			
 			return interest;
 		}
 	}
 
 	@Override
 	public void addTouch(float x, float y, Object touchref)
 	{
 		synchronized(this)
 		{
 			for (Manoeuver m : _manoeuvers)
 				if (m.isAdjustmentInterestedAtPx(x, y))
 				{
 					_adjustingMnvr = m;
 					_adjustingTouch = touchref;
 					adjustAtPx(x, y);
 					return;
 				}
 		}
 
 		synchronized (_touchsymbols)
 		{
 			float interest = Float.NEGATIVE_INFINITY;
 			Touchable T = null;
 			
 			Iterator<Touchable> itr = _touchsymbols.iterator();
 			while(itr.hasNext())
 			{
 				Touchable t = itr.next();
 				float i = t.getInterestForPoint(x, y);
 				if (i > interest)
 				{
 					T = t;
 					interest = i;
 				}
 			}
 			
 			T.addTouch(x, y, touchref);
 			_touchedsymbols.put(touchref, T);
 		}
 	}
 
 	@Override
 	public void updateTouch(float x, float y, Object touchref)
 	{
 		if (_adjustingMnvr != null && _adjustingTouch == touchref)
 		{
 			adjustAtPx(x, y);
 			return;
 		}
 		
 		synchronized (_touchedsymbols)
 		{
 			Touchable T = _touchedsymbols.get(touchref);
 			
 			T.updateTouch(x, y, touchref);
 		}
 	}
 
 	@Override
 	public void removeTouch(float x, float y, Object touchref)
 	{
 		if (_adjustingMnvr != null && _adjustingTouch == touchref)
 		{
 			stopAdjusting();
 			return;
 		}
 		
 		synchronized (_touchedsymbols)
 		{
 			Touchable T = _touchedsymbols.get(touchref);
 			
 			T.removeTouch(x, y, touchref);
 		}
 	}
 
 	@Override
 	public void cancelTouch(Object touchref)
 	{
 		if (_adjustingMnvr != null && _adjustingTouch == touchref)
 			stopAdjusting();
 		
 		synchronized (_touchedsymbols)
 		{
 			Touchable T = _touchedsymbols.get(touchref);
 			
 			T.cancelTouch(touchref);
 		}
 	}
 
 	public void addManoeuver(Manoeuver mnvr)
 	{
 		synchronized (this)
 		{
 			_manoeuvers.add(mnvr);
 			addTouchSymbol(mnvr);
 		}
 	}
 
 }
