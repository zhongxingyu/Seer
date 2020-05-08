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
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 
 import javax.imageio.ImageIO;
 
 import org.openstreetmap.gui.jmapviewer.Coordinate;
 
 import com.deev.interaction.touch.Touchable;
 import com.deev.interaction.uav3i.model.UAVDataPoint;
 import com.deev.interaction.uav3i.model.UAVModel;
 
 import eu.telecom_bretagne.uav3i.UAV3iSettings;
 import eu.telecom_bretagne.uav3i.UAV3iSettings.Mode;
 import eu.telecom_bretagne.uav3i.util.log.LoggerUtil;
 import uk.me.jstott.jcoord.LatLng;
 
 @SuppressWarnings("serial")
 public class SymbolMap extends Map implements Touchable
 {		
 	private ArrayList<Manoeuver> _manoeuvers = null;
 	private Manoeuver _adjustingMnvr = null;
 	private Object _adjustingTouch = null;
 	
 	private ArrayList<Touchable> _touchSymbols;
 	private HashMap<Object, Touchable> _touchedSymbols;
 
 	private Trajectory _trajectory;
 	private long _lastTrajectoryUpdate = 0;
 
 	protected static BufferedImage _uavImage = null;
 	protected static BufferedImage _uavGrayImage = null;
 
 	public SymbolMap()
 	{
 		super();
 
 		setVisible(true);
 		setOpaque(false);
 		Color back = new Color(0.f, 0.f, 0.f, .0f);
 		setBackground(back);	
 
 		_manoeuvers = new ArrayList<Manoeuver>();
 		_touchSymbols = new ArrayList<Touchable>();
 		_touchedSymbols = new HashMap<Object, Touchable>();
 		
 		_trajectory = new Trajectory();
 
 		// Dessin UAV
 		try
 		{
 			_uavImage = ImageIO.read(this.getClass().getResource("img/uav.png"));
 			_uavGrayImage = ImageIO.read(this.getClass().getResource("img/uavGray.png"));
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void addTouchSymbol(Touchable t)
 	{
 		synchronized (_touchSymbols)
 		{
 			_touchSymbols.add(t);
 		}
 	}
 	
 	public void removeTouchSymbol(Touchable t)
 	{
 		synchronized (_touchSymbols)
 		{
 			_touchSymbols.remove(t);
 			
 			for(Iterator<Entry<Object,Touchable>> it = _touchedSymbols.entrySet().iterator(); it.hasNext();)
 			{
 				Entry<Object,Touchable> entry = it.next();
 			      if(entry.getValue() == t)
 			      {
 			        it.remove();
 			      }
 			}
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
 
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 
 
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
 			g2.setStroke(new BasicStroke(2.f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
 			g2.draw(fullTrajectory);
 			g2.setPaint(Color.RED);
 			g2.setStroke(new BasicStroke(1.f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
 			g2.draw(fullTrajectory);
 		}
 		
 		if (MainFrame.SWITCHER.getMode() == Switcher3Buttons.Mode.REPLAY)
 		{
 			// Tracé de séquence
 			GeneralPath seqTrajectory = _trajectory.getTrajectorySequence(this,
 					MainFrame.TIMELINE.getTimeSeqStart(),
 					MainFrame.TIMELINE.getTimeSeqend());
 			
 			if (seqTrajectory != null)
 			{
 				g2.setPaint(new Color(1.f, 1.f, 1.f, .7f));
 				g2.setStroke(new BasicStroke(10.f, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
 				g2.draw(seqTrajectory);
 			}
 		}
 		// --------- Manoeuvers --------------------------------------------------
 		synchronized(this)
 		{
 			for (Manoeuver m : _manoeuvers)
 				m.paint(g2);
 		}
 
 		// Dessin UAV
 		AffineTransform old = g2.getTransform();	
 		BufferedImage uavImg;
 		UAVDataPoint uavpoint;
 		
 		if (MainFrame.SWITCHER.getMode() == Switcher3Buttons.Mode.REPLAY)
 		{
 			uavImg = _uavGrayImage;
 		}
 		else
 		{
 			uavImg = _uavImage;
 		}
 
 		uavpoint = UAVModel.getDataPointAtTime(System.currentTimeMillis());
 		if (uavpoint != null)
 		{
 			Point2D.Double uav = getScreenForLatLng(uavpoint.latlng);
 			double course = Math.PI/2. - uavpoint.course/180.*Math.PI;
 			g2.translate(uav.x, uav.y);
 
 			g2.rotate(Math.PI/2.-course);
 			g2.drawImage(uavImg, -uavImg.getWidth()/2, -uavImg.getHeight()/2, null);
 
 		}
 		g2.setTransform(old);
 		
 		
 		// Dessin UAV
 		old = g2.getTransform();	
 		if (MainFrame.SWITCHER.getMode() == Switcher3Buttons.Mode.REPLAY)
 		{
 			uavImg = _uavImage;
 			
 			uavpoint = UAVModel.getDataPointAtTime(MainFrame.TIMELINE.getTimeCursorPosition());
 			if (uavpoint != null)
 			{
 				Point2D.Double uav = getScreenForLatLng(uavpoint.latlng);
 				double course = Math.PI/2. - uavpoint.course/180.*Math.PI;
 				g2.translate(uav.x, uav.y);
 
 				g2.rotate(Math.PI/2.-course);
 				g2.drawImage(uavImg, -uavImg.getWidth()/2, -uavImg.getHeight()/2, null);
 
 			}
 		}
 		g2.setTransform(old);
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
 
 		_adjustingMnvr.positionButtons();
 		
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
 		
 		_adjustingMnvr.positionButtons();
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
 					return m.getAdjustInterest();			
 		}
 
 		synchronized (_touchSymbols)
 		{
 			float interest = -1.f;;
 			
 			Iterator<Touchable> itr = _touchSymbols.iterator();
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
 
 		synchronized (_touchSymbols)
 		{
 			float interest = Float.NEGATIVE_INFINITY;
 			Touchable T = null;
 			
 			Iterator<Touchable> itr = _touchSymbols.iterator();
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
 			_touchedSymbols.put(touchref, T);
 		}
 	}
 
 	@Override
 	public void updateTouch(float x, float y, Object touchref)
 	{
 		if (_adjustingMnvr != null && _adjustingTouch == touchref)
 		{
 			if (_adjustingMnvr.isModifiable())
 			{
 				adjustAtPx(x, y);
 				return;
 			}
 			else
 			{
 				cancelTouch(touchref);
 				return;
 			}
 		}
 		
 		synchronized (_touchedSymbols)
 		{
 			Touchable T = _touchedSymbols.get(touchref);
 			
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
 		
 		synchronized (_touchedSymbols)
 		{
 			Touchable T = _touchedSymbols.get(touchref);
 			
 			T.removeTouch(x, y, touchref);
 		}
 	}
 
 	@Override
 	public void cancelTouch(Object touchref)
 	{
 		if (_adjustingMnvr != null && _adjustingTouch == touchref)
 			stopAdjusting();
 		
 		synchronized (_touchedSymbols)
 		{
 			Touchable T = _touchedSymbols.get(touchref);
 			
 			T.cancelTouch(touchref);
 		}
 	}
 
 	public void addManoeuver(Manoeuver mnvr)
	{
		LoggerUtil.LOG.log(Level.INFO, "adding manoeuver");
	
 		synchronized (this)
 		{
 			_manoeuvers.add(mnvr);
 			addTouchSymbol(mnvr);
 		}
 	}
 	
 	public void hideManoeuverButtons()
 	{
 		synchronized(this)
 		{
 			for (Manoeuver m : _manoeuvers)
 				m.hidebuttons();
 		}
 	}
 	
 	public void deleteManoeuver(Manoeuver mnvr)
 	{
 		synchronized (this)
 		{
 			_manoeuvers.remove(mnvr);
 			removeTouchSymbol(mnvr);
 		}
 	}
 
 }
