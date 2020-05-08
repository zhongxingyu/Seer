 package com.deev.interaction.uav3i.ui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.logging.Level;
 
 import javax.swing.JComponent;
 
 import uk.me.jstott.jcoord.LatLng;
 
 import com.deev.interaction.touch.Animator;
 import com.deev.interaction.touch.BoundingRectangle;
 import com.deev.interaction.touch.Gesture;
 import com.deev.interaction.touch.Touchable;
 import com.deev.interaction.uav3i.model.UAVModel;
 import com.deev.interaction.uav3i.ui.LineMnvr;
 import com.deev.interaction.uav3i.ui.MainFrame.MainFrameState;
 
 import eu.telecom_bretagne.uav3i.UAV3iSettings;
 import eu.telecom_bretagne.uav3i.UAV3iSettings.Mode;
 import eu.telecom_bretagne.uav3i.paparazzi_settings.airframe.AirframeFacade;
 import eu.telecom_bretagne.uav3i.util.log.LoggerUtil;
 
 @SuppressWarnings("serial")
 public class FingerPane extends JComponent implements Touchable
 {	
 	private SymbolMap _smap;
 	private HashMap<Object, FingerDrawing> _drawings;
 
 	public FingerPane()
 	{
 		super();
 
 		_drawings = new HashMap<Object, FingerDrawing>();
 
 		setVisible(true);
 		setOpaque(false);
 		Color back = new Color(0.f, 0.f, 0.f, .0f);
 		setBackground(back);
 	}
 
 	@Override
 	public void paintComponent(Graphics g)
 	{	
 		Graphics2D g2 = (Graphics2D) g;
 
 		synchronized (_drawings)
 		{
 			Iterator<FingerDrawing> itr = _drawings.values().iterator();
 
 			while(itr.hasNext())
 				itr.next().paintAnimation(g2);
 		}
 	}
 
 
 	public void interpret(Gesture gesture)
 	{		
 		BoundingRectangle rectangle = gesture.getSmallestEnclosingRectangle();
 
 		double delta = 40.;
 		
 		if (rectangle.width < delta && rectangle.height < delta)
 		{
 			LatLng p = _smap.getLatLngForScreen((float) rectangle.x, (float) rectangle.y);
 		
 			CircleMnvr circleMnvr = new CircleMnvr(_smap, p);
 			_smap.addManoeuver(circleMnvr);
 			Animator.addAnimation(circleMnvr);
 
 			return;
 		}
 
 		if (!gesture.isOpen())
 		{
 			Rectangle2D.Double box = gesture.getBox();
 			
 			LatLng A = _smap.getLatLngForScreen(box.x, box.y);
 
 			LatLng B = _smap.getLatLngForScreen(box.x+box.width, box.y+box.height);
 
 			BoxMnvr boxMnvr = new BoxMnvr(_smap, A, B);
 			_smap.addManoeuver(boxMnvr);
 			Animator.addAnimation(boxMnvr);
 			
 		}
 		
 		if (rectangle.height < delta)
 		{
 			LatLng A = _smap.getLatLngForScreen(rectangle.x + rectangle.width/2.*Math.sin(-rectangle.theta),
 					rectangle.y + rectangle.width/2.*Math.cos(-rectangle.theta));
 
 			LatLng B = _smap.getLatLngForScreen(rectangle.x - rectangle.width/2.*Math.sin(-rectangle.theta),
 					rectangle.y - rectangle.width/2.*Math.cos(-rectangle.theta));
 
 			LineMnvr line = new LineMnvr(_smap, A, B);
 			_smap.addManoeuver(line);
 			Animator.addAnimation(line);
 
 			return;
 		}
 
 		if (rectangle.width < delta)
 		{
 			LatLng A = _smap.getLatLngForScreen(rectangle.x + rectangle.height/2.*Math.sin(-rectangle.theta),
 					rectangle.y + rectangle.height/2.*Math.cos(-rectangle.theta));
 
 			LatLng B = _smap.getLatLngForScreen(rectangle.x - rectangle.height/2.*Math.sin(-rectangle.theta),
 					rectangle.y - rectangle.height/2.*Math.cos(-rectangle.theta));
 
 			LineMnvr lineMnvr = new LineMnvr(_smap, A, B);
 			_smap.addManoeuver(lineMnvr);
 			Animator.addAnimation(lineMnvr);
 
 
 			return;
 		}
 	}
 
 	public void setTopMap(SymbolMap map)
 	{
 		_smap = map;
 	}
 
 	@Override
 	public float getInterestForPoint(float x, float y)
 	{
 		if (MainFrame.getAppState() == MainFrameState.COMMAND)
 			return 10.f;
 		else
 			return -1.f;
 	}
 
 	@Override
 	public void addTouch(float x, float y, Object touchref)
 	{
 		synchronized (_drawings)
 		{
 			_drawings.put(touchref, new FingerDrawing(x, y));
 		}
 	}
 
 	@Override
 	public void updateTouch(float x, float y, Object touchref)
 	{
 		synchronized (_drawings)
 		{
 			_drawings.get(touchref).addPoint(x, y);
 		}
 	}
 
 	@Override
 	public void removeTouch(float x, float y, Object touchref)
 	{
 		synchronized (_drawings)
 		{
 			FingerDrawing fd = _drawings.get(touchref);
 			interpret(new Gesture(fd.getPoints()));
 			fd.release();	
 			Animator.addAnimation(fd);
 		}
 	}
 
 	@Override
 	public void cancelTouch(Object touchref)
 	{
 		Animator.removeAnimation(_drawings.get(touchref));
 		_drawings.remove(touchref);
 	}
 
 
 }
