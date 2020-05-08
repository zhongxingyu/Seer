 package gui;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.Border;
 
 import sound.MusicConstants;
 import sound.Note;
 
 
 public class SoundVisualizer extends JPanel implements TransitionElement {
 	private ArrayList<SoundPoint> _plotPoints = new ArrayList<SoundPoint>();
 	private int _height, _width;
 	private Timer _tickTimer = new Timer();
 	private Timer _paintTimer = new Timer();
 	private Ticker _ticker = new Ticker(this);
 	private Repainter _repainter;
	private double _amplitude = 1, _frequency = 1;
 	private double _ticks = 0;
 	private int _speed = 10;
 	private double _alpha = 1.0;
 	private int _numNotes, _startNote;
 
 	private int _x, _y, _defaultX, _defaultY;
 
 	public SoundVisualizer(Screen screen, int width, int height, int numNotes, int startNote){
 		_width = width;
 		_height = height;
 		_numNotes = numNotes;
 		_startNote = startNote;
 		
 		_repainter = new Repainter(screen);
 		
 		this.setSize(_width, _height);
 		_paintTimer.schedule(_repainter, 1000, 50);
 		_tickTimer.schedule(_ticker, 1000, 10);
 	}
 	
 	public void setNumNotes(int numNotes) {
 		_numNotes = numNotes;
 	}
 
 	public void setAmplitude(double amplitude){
 		_amplitude = _height / 2 * amplitude;
 	}
 
 	public void setFrequency(double frequency){
 		_frequency = 8 * frequency;
 	}
 
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		Graphics2D g2 = (Graphics2D) g.create(); 
 
 		// draw white rectangle
 		g2.setColor(new Color(255, 255, 255, (int)(255*_alpha)));
 		g2.fillRect(0, 0, _width, _height);
 		
 		// figure out wave color
 		int red = (int)(_amplitude / (_height / 2) * 255);
 		int green = (int)(_frequency / 10 / 4 * 255);
 		
 		red = red > 255 ? 255 : red;
 		green = green > 255 ? 255 : green;
 		red = red < 0 ? 0 : red;
 		green = green > 0 ? 0 : green;
 		
 		// draw wave
 		g2.setColor(new Color(red, green, Math.abs(red - green), (int)(255*_alpha)));
 		g2.setStroke(new BasicStroke(6));
 		for(int i = 0; i < _plotPoints.size(); i++) { 
 			SoundPoint point = _plotPoints.get(i);
 			if(point.getNext() != null){
 				g2.drawLine((int)point.getX(), (int)point.getY(), (int)point.getNext().getX(), (int)point.getNext().getY());
 			}
 		}
 
 		// draw divider lines
 		g2.setColor(new Color(200,200,200,100));
 		g2.setStroke(new BasicStroke(2));
 		for (int i = 0; i < _numNotes; i++) {
 			int lineX = i * getWidth()/_numNotes;
 			g2.drawLine(lineX, 0, lineX, _height);
 		}
 	}   
 
 	public void tock(){
 		for(int i =0; i<_plotPoints.size();i++)
 			this.movePoint(_plotPoints.get(i));
 
 		//  Length of one full wave = panel width divided by frequency
 		final double waveLength = _width / _frequency;
 
 		//Incrementing yBase; capping off at wavelength
 		_ticks = (++_ticks) % waveLength;
 
 		//Normalizing to [0..1]
 		final double normalized = (double)_ticks / (double)waveLength;
 
 		//Full wave at 2*pi, means...
 		final double radians = normalized * Math.PI * 2;
 
 		//Getting the sine
 		final double sine = Math.sin(radians);
 
 		//Multiplying with amplitude, add to center position and we have our y
 		int y = (int)(sine * _amplitude + _height/2) ;
 
 		SoundPoint incomingPoint = new SoundPoint(0, y, _amplitude,_frequency);
 		
 		if (!_plotPoints.isEmpty())
 			incomingPoint.setNext(_plotPoints.get(_plotPoints.size() - 1));
 		_plotPoints.add(incomingPoint);
 	}
 
 	public void movePoint(SoundPoint point) {
 
 		double x = point.getX();
 
 		//Move a pixel to the right; loop over to the left when reaching edge
 		if(x + _speed > _width)
 			_plotPoints.remove(point);
 		else
 			point.setLocation(x + _speed, point.getY()); 
 	}
 
 	private static class Ticker extends TimerTask {
 
 		private SoundVisualizer _panel;
 
 		public Ticker(SoundVisualizer panel) {
 			_panel = panel;
 		}
 		@Override
 		public void run() {
 			_panel.tock();
 		}
 	}
 
 	private static class Repainter extends TimerTask {
 		private Screen _screen;
 		public Repainter(Screen screen) {
 			_screen = screen;
 		}
 
 		@Override
 		public void run() {
 			_screen.repaint();
 		}
 	}
 
 	@Override
 	public void setDefaultLocation(int x, int y) {
 		this.setLocation(x, y);
 		_x = x;
 		_y = y;
 		_defaultX = x;
 		_defaultY = y;
 	}
 
 	@Override
 	public void setLocation(int x, int y) {
 		super.setLocation(x, y);
 		_x = x;
 		_y = y;
 	}
 
 	@Override
 	public void setX(int x) {
 		this.setLocation(x, _y);
 	}
 
 	@Override
 	public void setY(int y) {
 		this.setLocation(_x, y);
 	}
 
 	@Override
 	public int getDefaultX() {
 		return _defaultX;
 	}
 
 	@Override
 	public int getDefaultY() {
 		return _defaultY;
 	}
 
 	@Override
 	public void setAlpha(float newAlpha) {
 		_alpha = newAlpha;
 	}
 
 	public boolean contains(int x, int y) {
 		return x >= _x && x < _x + _width && y >= _y && y < _y + _height;
 	}
 
 	@Override
 	public void inTransition(boolean transitioning) {}
 
 }
