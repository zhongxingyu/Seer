 package com.deev.interaction.uav3i.ui;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Area;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 
 import uk.me.jstott.jcoord.LatLng;
 
 public class LineMnvr extends Manoeuver
 {
 	// Points de la zone à regarder
 	private LatLng _A, _B;
 
 	private SymbolMap _smap;
 
 	// Distance entre la zone à regarder et la trajectoire
 	private double _currentRm = 500.;
 	private double _lastRm;
 
 	// Codage de l'orientation de la droite dans le plan ?
 	private Point2D.Double _u, _v;
 
 	static double RPX = 10.;
 
 	private enum LineMnvrMoveStates {NONE, TRANSLATE, FULL};
 	private LineMnvrMoveStates _moveState = LineMnvrMoveStates.NONE;
 
 	private Object _touchOne;
 	private Object _touchTwo;
 
 	private Point2D.Double _uvOne;
 	private Point2D.Double _uvTwo;
 
 	private Point2D.Double _offsetA;
 	private Point2D.Double _offsetB;
 	private Point2D.Double _lastPosOne;
 	private Point2D.Double _lastPosTwo;
 	
 	
 
 	/**
 	 * @param map
 	 * @param xA Point A x-coordinate (screen)
 	 * @param yA Point A y-coordinate (screen)
 	 * @param xB Point B x-coordinate (screen)
 	 * @param yB Point B y-coordinate (screen)
 	 */
 	public LineMnvr(SymbolMap map, double xA, double yA, double xB, double yB)
 	{
 		_A = map.getLatLngForScreen(xA, yA);
 		_B = map.getLatLngForScreen(xB, yB);
 
 		_smap = map;
 
 		double d = Point2D.Double.distance(xA, yA, xB, yB);
 		_u = new Point2D.Double((xB-xA)/d, (yB-yA)/d);
 		_v = new Point2D.Double(-_u.y, _u.x);
 
 	}
 
 	public LineMnvr(SymbolMap map, LatLng A, LatLng B)
 	{
 		Point2D.Double a = map.getScreenForLatLng(A);
 		Point2D.Double b = map.getScreenForLatLng(B);
 
 		_A = A;
 		_B = B;
 
 		_smap = map;
 
 		double d = a.distance(b);
 		_u = new Point2D.Double((b.x-a.x)/d, (b.y-a.y)/d);
 		_v = new Point2D.Double(-_u.y, _u.x);
 	}
 
 	@Override
 	public void paint(Graphics2D g2)
 	{
 		AffineTransform old = g2.getTransform();
 
 		Point2D.Double Apx = _smap.getScreenForLatLng(_A);
 		Point2D.Double Bpx = _smap.getScreenForLatLng(_B);
 
 		Area area = new Area();
 		BasicStroke stroke = new BasicStroke((float) RPX*2.f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
 
 		// Zone à regarder : ligne rouge
 		GeneralPath line;
 		line = new GeneralPath();
 		line.moveTo(Apx.x, Apx.y);
 		line.lineTo(Bpx.x, Bpx.y);
 
 		area.add(new Area(stroke.createStrokedShape(line)));
 
 		g2.setStroke(new BasicStroke(4.f));
 		g2.setPaint(new Color(1.0f, 0.f, 0.f, 1.0f));
 		g2.draw(area);
 		g2.setPaint(new Color(1.0f, 0.1f, 0.1f, 0.2f));
 		g2.fill(area);
 
 		g2.setStroke(new BasicStroke(4.f));
 		g2.setPaint(new Color(0.0f, 0.f, 1.0f, 1.0f));
 
 		double Rpx = _smap.getPPM() * _currentRm;
 
 		Point2D.Double LApx = new Point2D.Double(Apx.x + _v.x * Rpx,
 				Apx.y + _v.y * Rpx);
 
 		Point2D.Double LBpx = new Point2D.Double(Bpx.x + _v.x * Rpx,
 				Bpx.y + _v.y * Rpx);
 
 		// Trajectoire du drone : ligne bleue
 		Line2D.Double l = new Line2D.Double(LApx, LBpx);
 
 		g2.setStroke(new BasicStroke(2.f*(float)GRIP));
 		if (!_adjusting)
 			g2.setPaint(new Color(1.0f, 1.0f, 1.0f, 0.3f));
 		else
 			g2.setPaint(new Color(1.0f, 1.0f, 0.7f, 0.3f));
 		g2.draw(l);
 
 		g2.setStroke(new BasicStroke(4.f));
 		g2.setPaint(new Color(0.0f, 0.f, 1.0f, 1.0f));
 		g2.draw(l);
 
 		g2.setTransform(old);
 	}
 
 	private Point2D.Double getUVforPx(double x, double y)
 	{
 		Point2D.Double Apx = _smap.getScreenForLatLng(_A);
 
 		double X = x-Apx.x;
 		double Y = y-Apx.y;
 
 		return new Point2D.Double(X*_u.x + Y*_u.y, X*_v.x + Y*_v.y);
 	}
 
 	@Override
 	public boolean adjustAtPx(double x, double y)
 	{
 		// x,y : coordonnée du toucher écran
 		// On projette tout en screen
 		// Coordonnées écran des deux points (zone rouge)
 		Point2D.Double Apx = _smap.getScreenForLatLng(_A);
 		Point2D.Double Bpx = _smap.getScreenForLatLng(_B);
 
 		// Parallélisme avec la zone à regarder
 		Point2D.Double p = getUVforPx(x, y);
 		double u = p.x;
 		double v = p.y;
 
 		double currentRpx = _currentRm * _smap.getPPM();
 		double lastRpx = _lastRm * _smap.getPPM();
 
 		if (_adjusting)
 		{
 			currentRpx += v - lastRpx;
 			lastRpx = v;
 
 			_currentRm = currentRpx / _smap.getPPM();
 			_lastRm = lastRpx / _smap.getPPM();
 
 			return true;
 		}
 
 		if (isAdjustmentInterestedAtPx(x, y))
 		{
 			lastRpx = v;
 			_adjusting = true;
 		}
 
 		_currentRm = currentRpx / _smap.getPPM();
 		_lastRm = lastRpx / _smap.getPPM();
 
 		return _adjusting;
 	}
 
 	public boolean isAdjustmentInterestedAtPx(double x, double y)
 	{
 		// On projette tout en screen
 		Point2D.Double Apx = _smap.getScreenForLatLng(_A);
 		Point2D.Double Bpx = _smap.getScreenForLatLng(_B);
 
 		double X = x-Apx.x;
 		double Y = y-Apx.y;
 
 		double u = X*_u.x + Y*_u.y;
 		double v = X*_v.x + Y*_v.y;
 
 		double currentRpx = _currentRm * _smap.getPPM();
 
 		return v > currentRpx-GRIP && v < currentRpx+GRIP && u > -GRIP && u < Apx.distance(Bpx)+GRIP;
 	}
 
 	/**
 	 * Getter pour le point A de la trajectoire du drone (et non de la zone à regarder).
 	 * 
 	 * @return coordonnée en {@link LatLng} du point.
 	 */
 	public LatLng getTrajA()
 	{
 		double Rpx = _smap.getPPM() * _currentRm;
 		Point2D.Double Apx = _smap.getScreenForLatLng(_A);
 
 		Point2D.Double LApx = new Point2D.Double(Apx.x + _v.x * Rpx, Apx.y + _v.y * Rpx);
 		
 		return _smap.getLatLngForScreen(LApx.x, LApx.y);
 	}
 
 	/**
 	 * Getter pour le point B de la trajectoire du drone (et non de la zone à regarder).
 	 * 
 	 * @return coordonnée en {@link LatLng} du point.
 	 */
 	public LatLng getTrajB()
 	{
 		double Rpx = _smap.getPPM() * _currentRm;
 		Point2D.Double Bpx = _smap.getScreenForLatLng(_B);
 
 		Point2D.Double LBpx = new Point2D.Double(Bpx.x + _v.x * Rpx, Bpx.y + _v.y * Rpx);
 		
 		return _smap.getLatLngForScreen(LBpx.x, LBpx.y);
 	}
 
 	@Override
 	public float getInterestForPoint(float x, float y)
 	{
 		if (_moveState == LineMnvrMoveStates.FULL)
 			return -1.f;
 
 		// On projette tout en screen
 		Point2D.Double Apx = _smap.getScreenForLatLng(_A);
 		Point2D.Double Bpx = _smap.getScreenForLatLng(_B);
 
 		double X = x-Apx.x;
 		double Y = y-Apx.y;
 
 		double u = X*_u.x + Y*_u.y;
 		double v = X*_v.x + Y*_v.y;
 
 		if (v > -2*RPX && v < 2*RPX && u > -2*RPX && u < Apx.distance(Bpx)+2*RPX)
 			return Manoeuver.MOVE_INTEREST;
 		else
 			return -1.f;
 	}
 
 	@Override
 	public void addTouch(float x, float y, Object touchref)
 	{
 		switch (_moveState)
 		{
 			case FULL:
 				return;
 			case TRANSLATE:
 				_touchTwo = touchref;
 				_lastPosTwo = new Point2D.Double(x, y);
 				_moveState = LineMnvrMoveStates.FULL;
 				return;
 			case NONE:
 				_touchOne = touchref;
 				Point2D.Double pA = _smap.getScreenForLatLng(_A);
 				_offsetA = new Point2D.Double(x-pA.x, y-pA.y);
 				Point2D.Double pB = _smap.getScreenForLatLng(_B);
 				_offsetB = new Point2D.Double(x-pB.x, y-pB.y);
 				_moveState = LineMnvrMoveStates.TRANSLATE;
 				return;
 			default:
 				return;
 		}
 	}
 
 	@Override
 	public void updateTouch(float x, float y, Object touchref)
 	{
 		if (touchref != _touchOne && touchref != _touchTwo)
 			return;
 		
 		switch (_moveState)
 		{
 			case FULL:
 				return;
 			case TRANSLATE:
 				if (touchref == _touchOne)
 				{
					_A = _smap.getLatLngForScreen(x-_offsetA.x, y-_offsetA.y);
					_B = _smap.getLatLngForScreen(x-_offsetB.x, y-_offsetB.y);
 				}
 				return;
 			case NONE:
 			default:
 				return;
 		}
 	}
 
 	@Override
 	public void removeTouch(float x, float y, Object touchref)
 	{
 		_moveState = LineMnvrMoveStates.NONE;
 	}
 
 	@Override
 	public void cancelTouch(Object touchref)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 }
