 package com.deev.interaction.uav3i.ui;
 
 import java.awt.BasicStroke;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.font.FontRenderContext;
 import java.awt.font.TextLayout;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Path2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.swing.JComponent;
 
 import com.deev.interaction.touch.Animation;
 import com.deev.interaction.touch.Touchable;
 import com.deev.interaction.uav3i.model.VideoModel;
 import com.deev.interaction.uav3i.model.VideoSegment;
 
 public class TimeLine extends JComponent implements Touchable, Animation
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -923278315604054575L;
 		
 	private enum TimeLineState {HIDDEN, SHOWING, ACTIVE, HIDING};
 	private TimeLineState _state;
 	private double _vOffset;
 	
 	protected static final double _Y = 102;
 	protected static final double _HEIGHT = 80;
 	protected static final double _MEDIA_HEIGHT = 16;
 	protected static final double _DOT_SIZE = 28;
 	
 	protected static final int _TEXT_BGD_SIZE = 18;
 	protected static final double _TEXT_BGD_X_OFFSET = 4;
 	protected static final double _TEXT_BGD_Y_OFFSET = 10;
 	
 	protected static final int _TEXT_VID_SIZE = 12;
 	protected static final double _TEXT_VID_X_OFFSET = 2;
 	protected static final double _TEXT_VID_Y_OFFSET = 0;
 	
 	private double _timeOrigin; // Screen Left in milliseconds
 	private double _timeScale;  // milliseconds / pixel
 	private double _timeOriginTgt; // for animations
 	private double _timeScaleTgt;  // for animations
 	
 	private SimpleDateFormat _HHmm = new SimpleDateFormat("HH:mm");
 	private SimpleDateFormat _HHmmss = new SimpleDateFormat("HH:mm:ss");
 	
 	private static double _MIN_TIME_SEG_WIDTH = 300;
 	
 	// Interaction
 	private enum TimeLineScrubStates {NONE, TRANSLATE, FULL};
 	private TimeLineScrubStates _scrubState;
 	private Object _touchOne;
 	private double _touchTimeOne;
 	private Object _touchTwo;
 	private double _touchTimeTwo;
 	private double _lastPosOne;
 	private double _currentPosOne;
 	private double _currentPosTwo;
 	
 	private double _speed; // pixels/second
 	private long _lastTimeOne;
 	private long _deltaTime;
 	private static double _SPEED_RATE = .9;
 	
 
 	public TimeLine(int screenWidth, int screenHeight)
 	{
 		super();
 
 		_state = TimeLineState.HIDDEN;
 		_vOffset = _Y;
 		_scrubState = TimeLineScrubStates.NONE;
 		
 		setBounds(0, 0, screenWidth, screenHeight);
 		
 		_timeOriginTgt = _timeOrigin = System.currentTimeMillis();
 		_timeScaleTgt = _timeScale = 10*60*1000 / 360.;
 	}
 
 	public long getTimeCursorPosition()
 	{
 		if (VideoModel.video.isPlaying())
 			return VideoModel.video.getCursorPosition();
 		else
 			return (long) _timeOrigin;
 	}
 	
 	public long getTimeSeqStart()
 	{
 		return (long) _timeOrigin;
 	}
 	
 	public long getTimeSeqend()
 	{
 		return (long) (_timeOrigin + this.getBounds().width * _timeScale);
 	}
 	
 	@Override
 	public void paintComponent(Graphics g)
 	{
 		if (_state == TimeLineState.HIDDEN)
 			return;
 		
 		Graphics2D g2 = (Graphics2D) g;
 
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 				
 		/** OLD **/
 		AffineTransform old = g2.getTransform(); 
 		
 		g2.translate(0, this.getBounds().height-_Y+_vOffset);
 
 		// -1-
 		paintBackgroundSegments(g2);
 		
 		// -2-
 		paintVideoSegments(g2);
 		
 		// -3-
 		paintCursor(g2, getTimeCursorPosition(), false);
 		
 		// -4-
 		paintDot(g2);
 		
 		/** OLD **/
 		g2.setTransform(old); 
 	}
 
 	private void paintBackgroundSegments(Graphics2D g2)
 	{
 		float width = this.getBounds().width;	
 
 		final double timeSegmentsDurations[] = {2*10*1000, 2*30*1000, 2*60*1000, 2*5*60*1000, 2*10*60*1000, 2*60*60*1000};
 		int timeSegmentIndex = 0;
 		
		while (timeSegmentIndex < timeSegmentsDurations.length-1 && timeSegmentsDurations[timeSegmentIndex] / _timeScale < _MIN_TIME_SEG_WIDTH)
 		{
 			timeSegmentIndex++;
 		}
 
 		double xSegstart = pixelForTime(_timeOrigin - (_timeOrigin  % timeSegmentsDurations[timeSegmentIndex]));
 		double segWidth = timeSegmentsDurations[timeSegmentIndex] / _timeScale;
 
 		FontRenderContext frc = g2.getFontRenderContext();
 	    Font f = new Font("HelveticaNeue-UltraLight", Font.PLAIN, _TEXT_BGD_SIZE);
 	    TextLayout textTl;
 	    Shape outline;
 						
 		for (double x=xSegstart; x<=width; x+=segWidth)
 		{
 				g2.setPaint(Palette3i.getPaint(Palette3i.TIME_LIGHT));
 				g2.fill(new Rectangle2D.Double(x, 0, segWidth / 2., _HEIGHT));
 				g2.setPaint(Palette3i.getPaint(Palette3i.TIME_LIGHTER));
 				g2.fill(new Rectangle2D.Double(x + segWidth / 2., 0, segWidth / 2., _HEIGHT));
 
 				AffineTransform o = g2.getTransform();
 				
 				g2.setPaint(Palette3i.getPaint(Palette3i.TIME_LIGHT_TEXT));
 				g2.translate(x + _TEXT_BGD_X_OFFSET, _HEIGHT - _TEXT_BGD_Y_OFFSET);
 				if (timeSegmentsDurations[timeSegmentIndex]/2. < 60*1000+1)
 					textTl = new TextLayout(_HHmmss.format(new Date((long) timeForPixel(x))), f, frc);
 				else
 					textTl = new TextLayout(_HHmm.format(new Date((long) timeForPixel(x))), f, frc);
 				outline = textTl.getOutline(null);
 				g2.fill(outline);
 				
 				g2.translate(segWidth / 2., 0);
 				if (timeSegmentsDurations[timeSegmentIndex]/2. < 60*1000+1)
 					textTl = new TextLayout(_HHmmss.format(new Date((long) timeForPixel(x+segWidth/2.))), f, frc);
 				else
 					textTl = new TextLayout(_HHmm.format(new Date((long) timeForPixel(x+segWidth/2.))), f, frc);
 				outline = textTl.getOutline(null);
 				g2.fill(outline);
 				
 				g2.setTransform(o);
 		}
 
 	}
 	
 	private void paintVideoSegments(Graphics2D g2)
 	{
 		ArrayList<VideoSegment> list = VideoModel.video.getVideoSegments();
 		
 		VideoSegment segment;
 		double start, end;
 		
 		FontRenderContext frc = g2.getFontRenderContext();
 	    Font f = new Font("HelveticaNeue-UltraLight", Font.PLAIN, _TEXT_VID_SIZE);
 	    TextLayout textTl;
 	    Shape outline;
 		
 		for (int i=0; i<list.size(); i++)
 		{
 			segment = list.get(i);
 			start = pixelForTime(segment.getStart());
 			end = pixelForTime(segment.getEnd());
 			
 			if (i%2 == 0)
 				g2.setPaint(Palette3i.getPaint(Palette3i.TIME_DARK));
 			else
 				g2.setPaint(Palette3i.getPaint(Palette3i.TIME_DARKER));
 			
 			AffineTransform o = g2.getTransform();
 			
 			g2.translate(start, _HEIGHT/2.-_MEDIA_HEIGHT/2.);
 			
 			g2.fill(new Rectangle2D.Double(0, 0, end-start, _MEDIA_HEIGHT));
 			
 			g2.translate(_TEXT_VID_X_OFFSET, _TEXT_VID_SIZE+_TEXT_VID_Y_OFFSET);
 			
 			g2.setPaint(Palette3i.getPaint(Palette3i.TIME_DARK_TEXT));
 			
 			textTl = new TextLayout(segment.getName(), f, frc);
 			outline = textTl.getOutline(null);
 			g2.fill(outline);
 			
 			g2.setTransform(o);
 		}
 	}
 	
 	private void paintCursor(Graphics2D g2, double time, boolean full)
 	{
 		final BasicStroke plain =
 		        new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
 		
 		g2.setStroke(plain);
 		
 		AffineTransform o = g2.getTransform();
 		Path2D.Double line = new Path2D.Double();
 		g2.translate(pixelForTime(time), 0.);
 		line.moveTo(0., 0.);
 		line.lineTo(0., _HEIGHT/2.-_DOT_SIZE/2.);
 		line.lineTo(_DOT_SIZE/2., _HEIGHT/2.);
 		line.lineTo(0., _HEIGHT/2.+_DOT_SIZE/2.);
 		line.lineTo(0., _HEIGHT);
 		
 		g2.setPaint(Palette3i.getPaint(Palette3i.TIME_CURSOR_FILL));
 		if (full)
 			g2.fill(line);
 		
 		g2.setPaint(Palette3i.getPaint(Palette3i.TIME_CURSOR));
 		g2.fill(plain.createStrokedShape(line));
 		
 		// Time
 		FontRenderContext frc = g2.getFontRenderContext();
 	    Font f = new Font("HelveticaNeue-UltraLight", Font.PLAIN, _TEXT_BGD_SIZE);
 	    TextLayout textTl;
 	    Shape outline;
 		
 		g2.setPaint(Palette3i.getPaint(Palette3i.TIME_CURSOR));
 		g2.translate(_TEXT_BGD_X_OFFSET, _HEIGHT/2.-_MEDIA_HEIGHT/2.-_TEXT_BGD_Y_OFFSET);
 		textTl = new TextLayout(_HHmmss.format(new Date((long) time)), f, frc);
 		outline = textTl.getOutline(null);
 		g2.fill(outline);
 		
 		g2.setTransform(o);
 	}
 	
 	private void paintDot(Graphics2D g2)
 	{
 		g2.setPaint(Palette3i.getPaint(Palette3i.TIME_DOT));
 		AffineTransform o = g2.getTransform();
 		g2.translate(pixelForTime(System.currentTimeMillis()), _HEIGHT/2.);
 		g2.rotate(Math.PI/4);
 		g2.fill(new Rectangle2D.Double(-_DOT_SIZE/2, -_DOT_SIZE/2, _DOT_SIZE, _DOT_SIZE));
 		g2.setTransform(o);
 	}
 	
 	private double timeForPixel(double x)
 	{
 		return _timeOrigin + x * _timeScale;
 	}
 	
 	private double pixelForTime(double t)
 	{
 		return (t - _timeOrigin) / _timeScale;
 	}
 	
 	@Override
 	public float getInterestForPoint(float x, float y)
 	{
 		if (_state != TimeLineState.ACTIVE)
 			return -1.f;
 		
 		float interest = -1.f;
 
 		if (y > this.getBounds().height - _Y)
 			interest = 30.f;
 		
 		return interest;
 	}
 
 	@Override
 	public void addTouch(float x, float y, Object touchref)
 	{
 		switch (_scrubState)
 		{
 			case FULL:
 				return;
 			case TRANSLATE:
 				_touchTwo = touchref;
 				_touchTimeOne = timeForPixel(_currentPosOne);
 				_touchTimeTwo = timeForPixel(x);
 				_currentPosTwo = x;
 				_scrubState = TimeLineScrubStates.FULL;
 				return;
 			case NONE:
 				_touchOne = touchref;
 				_currentPosOne = _lastPosOne = x;
 				_touchTimeOne = timeForPixel(x);
 				_lastTimeOne = System.currentTimeMillis();
 				_scrubState = TimeLineScrubStates.TRANSLATE;
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
 		
 		switch (_scrubState)
 		{
 			case FULL:
 				if (touchref == _touchOne)
 					_currentPosOne = x;
 				else
 					_currentPosTwo = x;
 				updateScrub();
 				break;
 			
 			case TRANSLATE:
 				if (touchref == _touchOne)
 				{
 					_lastPosOne = _currentPosOne;
 					_currentPosOne = x;
 					long time = System.currentTimeMillis();
 					_deltaTime = time - _lastTimeOne;
 					_lastTimeOne = time;
 					
 					updateScrub();
 				}
 				break;
 			
 			case NONE:
 			default:
 				break;
 		}
 	}
 
 	private void updateScrub()
 	{
 		switch (_scrubState)
 		{
 			case FULL:
 				if (_currentPosOne-_currentPosTwo != 0.)
 				{
 					_timeScale = Math.abs((_touchTimeOne-_touchTimeTwo) / (_currentPosOne-_currentPosTwo));
 					_timeOrigin = _touchTimeOne - _currentPosOne * _timeScale;
 				}
 				break;
 			
 			case TRANSLATE:
 				_timeOrigin -= timeForPixel(_currentPosOne)-timeForPixel(_lastPosOne);
 				break;
 			
 			case NONE:
 			default:
 				break;
 		}
 		
 		VideoModel.video.setPlaySequence((long) _timeOrigin, (long) (_timeOrigin + this.getBounds().width * _timeScale));
 		MainFrame.SWITCHER.resetPlay();
 	}
 	
 	@Override
 	public void removeTouch(float x, float y, Object touchref)
 	{
 		switch (_scrubState)
 		{
 			case FULL:
 				if (touchref == _touchOne)
 				{
 					_scrubState = TimeLineScrubStates.NONE;
 					Object ref = _touchTwo;
 					_touchOne = null;
 					_touchTwo = null;
 					addTouch((float) _currentPosTwo, 0, ref);
 				}
 				else
 				{
 					_scrubState = TimeLineScrubStates.NONE;
 					Object ref = _touchOne;
 					_touchOne = null;
 					_touchTwo = null;
 					addTouch((float) _currentPosOne, 0, ref);
 				}
 				break;
 			
 			case TRANSLATE:
 				if (_deltaTime != 0)
 					_speed = (_currentPosOne - _lastPosOne) / _deltaTime;
 				_scrubState = TimeLineScrubStates.NONE;
 				break;
 			
 			case NONE:
 			default:
 				break;
 		}
 		
 		
 	}
 
 	@Override
 	public void cancelTouch(Object touchref)
 	{
 		_scrubState = TimeLineScrubStates.NONE;
 	}
 
 	@Override
 	public int tick(int time)
 	{
 		if (_scrubState == TimeLineScrubStates.NONE)
 		{
 			_speed *= _SPEED_RATE;
 			if (_speed < .05)
 				_speed = 0.;
 			double d = (double) time * _speed * _timeScale;
 			_timeOrigin -= d;
 		}
 		
 		switch (_state)
 		{
 			case HIDDEN : 
 				_vOffset = _Y;
 				break;
 			case SHOWING : 
 				_vOffset /= 2.;
 				if (_vOffset < 1.)
 					_state = TimeLineState.ACTIVE;
 				break;
 			case ACTIVE : 
 				_vOffset = 0.;
 				break;
 			case HIDING : 
 				_vOffset += (_Y-_vOffset)/2.;
 				if (_vOffset > _Y-1.)
 					_state = TimeLineState.HIDDEN;
 				break;
 			default:
 		}
 		
 		return 1;
 	}
 
 	@Override
 	public int life()
 	{
 		return 1;
 	}
 	
 	public void hide()
 	{
 		if (_state != TimeLineState.HIDDEN)
 			_state = TimeLineState.HIDING;
 	}
 	
 	public void show()
 	{
 		if (_state != TimeLineState.ACTIVE)
 			_state = TimeLineState.SHOWING;
 	}
 }
