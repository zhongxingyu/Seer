 package gui;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.LinearGradientPaint;
 import java.awt.MultipleGradientPaint;
 
 import sound.MusicConstants;
 
 public class VolumeVisualizer implements TransitionElement {
 
 	private int _x, _y, _width, _height, _defaultX, _defaultY, _volume;
 	private Color _backgroundColor = java.awt.Color.BLACK;
 	private Color _barColor = Color.RED;
 	private Color _barColor1 = Color.ORANGE;
 	private Color _barColor2 = Color.YELLOW;
 	private Color _barColor3 = Color.WHITE;
 	private Color[] _colors = {_barColor3, _barColor2,_barColor1,_barColor};
 	private float[] _fractions = {(float) .02, (float) .2, (float).3 ,(float ) .8};
 	private LinearGradientPaint _linearGradient;
	private int _volumeY;
 	
 	
 	public VolumeVisualizer(int x, int y, int width, int height){
 		setDefaultLocation(x, y);
 		_width = width;
 		_height = height;
 		_linearGradient = new LinearGradientPaint(0,_y + _height,0,0,_fractions,_colors,MultipleGradientPaint.CycleMethod.NO_CYCLE);
 	}
 	
 	protected void paint(Graphics2D g2) {
 		
 		double widthPercent = 0.8;
 		int rectangleWidth = (int)(widthPercent * _width);
 		int sideBorder = (_width - rectangleWidth) / 2;
 		
 		double heightPercent = 0.8;
 		int rectangleHeight = (int)(heightPercent * _height);
 		int topBorder = (_height - rectangleHeight) / 2;
 		
 		// background rectangle
 		g2.setColor(_backgroundColor);
 		g2.fillRect(_x + sideBorder, _y + topBorder, rectangleWidth, rectangleHeight*9/10);
 		
 		// volume bar
 		int barHeight = (int)((double)_volume / MusicConstants.PLAY_VOLUME * _height) - topBorder;
 		barHeight = Math.min(barHeight, rectangleHeight);
 		
 		g2.setPaint(_linearGradient);
 		g2.fillRect(_x + sideBorder, _y + topBorder + rectangleHeight - barHeight,
 				rectangleWidth, barHeight*9/10);
 	}
 	
 	public void setVolume(int volume){
 		_volume = volume;
 	}
 
 	@Override
 	public void setDefaultLocation(int x, int y) {
 		setLocation(x, y);
 		_defaultX = x;
 		_defaultY = y;
 	}
 
 	@Override
 	public void setLocation(int x, int y) {
 		_x = x;
 		_y = y;
 	}
 
 	@Override
 	public void setX(int x) {
 		setLocation(x, _y);
 	}
 
 	@Override
 	public void setY(int y) {
 		setLocation(_x, y);
 	}
 
 	@Override
 	public int getX() {
 		return _x;
 	}
 
 	@Override
 	public int getY() {
 		return _y;
 	}
 	
 	public boolean contains(int x, int y) {
 		return x >= _x && x < _x + _width && y >= _y && y < _y + _height;
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
 	public int getWidth() {
 		return _width;
 	}
 
 	@Override
 	public void setAlpha(float newAlpha) {}
 
 	@Override
 	public void inTransition(boolean transitioning) {}
 }
