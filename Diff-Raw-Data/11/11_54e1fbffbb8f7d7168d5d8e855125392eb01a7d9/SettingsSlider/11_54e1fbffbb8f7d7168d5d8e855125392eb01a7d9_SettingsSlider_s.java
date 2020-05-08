 package gui;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.Border;
 
 import org.pushingpixels.trident.Timeline;
 import org.pushingpixels.trident.Timeline.TimelineState;
 import org.pushingpixels.trident.callback.TimelineCallback;
 import org.pushingpixels.trident.ease.Spline;
 
 public class SettingsSlider extends JPanel implements TransitionElement {
 
 	private static final long serialVersionUID = -7327603656246439313L;
 
 	private int _height;
 	private int _width;
 
 	private int _numTicks = 14;
 	private int _tickCycle = 7;
 	private int _thickness = 10;
 	private int _thicknessTicks = 3;
 	private int _widthTicks = 15;
 	private int _insets = 15;
 	private int _sliderHeight;
 
 	private BasicStroke _tickStroke = new BasicStroke(_thicknessTicks, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND); 
 	private BasicStroke _tickStroke2 = new BasicStroke(_thicknessTicks+2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND); 
 	private BasicStroke _tubeStroke = new BasicStroke(_thickness+6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
 	private BasicStroke _tubeStroke2 = new BasicStroke(_thickness+4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
 	private BasicStroke _tubeFillStroke = new BasicStroke(_thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
 
 	private Color _tickColor = java.awt.Color.BLACK;
 	private Color _tickColor2 = java.awt.Color.LIGHT_GRAY;
 	private Color _tubeColor = java.awt.Color.BLACK;
 	private Color _tubeColor2 = java.awt.Color.LIGHT_GRAY;
 	private Color _tubeFillColor = java.awt.Color.RED;
 
 	private int _sliderValue;
 	private SmartImage _image;
 	private int _imageOff;
 
 	private JPanel _textPanel = new JPanel();
 	private int _textPanelWidth = 100;
 	private int _textPanelHeight = 40;
 
 	private JButton _valueDisplay;
 	private int _valueDisplayWidth = 100;
 	private int _valueDisplayHeight = 40;
 
 	private int _defaultX, _defaultY;
 
 	private int _constant;
 	private int _constantMax;
 	private int _constantStart;
 	private SettingsScreen _parent;
 	private String _settingType;
 	private int _level = 0;
 	private boolean _sliding = false;
 
 	public SettingsSlider(SettingsScreen screen, String settingType, int width, int height, int constant, int constantMax,int constantStart){
 
 		_height = height;
 		_width = width;
 		_parent = screen;
 		_settingType = settingType;
 
 		_constant = constant;
 		_constantMax = constantMax;
 		_constantStart = constantStart;
 
 		BufferedImage image = null;
 		try {
 			image = ImageIO.read(new File("img/knob.png"));
 		} catch (IOException e) {
 			System.out.println("ERROR: Could not find slider knob image");
 		}
 		_image = new SmartImage(image,1,this);
 		_imageOff = _image.getHeight()/2;
 		_sliderHeight = (_height-(2*_insets)-(2*_imageOff)-_textPanelHeight);
 		_sliderValue = ((int) (_sliderHeight* (1- ((double)_constantStart/(double)_constantMax) )));
 		_image.setLocation((_width/2) - _image.getWidth()/2,_insets+_imageOff+ ((int) (_sliderHeight* (1-( (double)_constantStart/(double)_constantMax) )))-_image.getHeight()/2);
 		_level = _image.getY() + _image.getHeight()/2;
 		
 		this.setLayout(null);
 		this.setOpaque(false);
 		this.setSize(_width,_height);
 		SliderListener listener = new SliderListener();
 		this.addMouseMotionListener(listener);
 		this.add(_textPanel);
 
 
 
 		_textPanel.setLayout(null);
 		_textPanel.setOpaque(false);
 		_textPanel.setSize(_textPanelWidth,_textPanelHeight);
		_textPanel.setLocation(_width/2-_textPanelWidth/2,(int) (_height-_textPanelHeight*1.1));
 
 		_valueDisplay = new JButton(Integer.toString(_constantStart));
 		_valueDisplay.setSize(_valueDisplayWidth,_valueDisplayHeight);
 		_textPanel.add(_valueDisplay);
 		_valueDisplay.setBackground(java.awt.Color.LIGHT_GRAY);
 		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
 		_valueDisplay.setBorder(border);
 		_valueDisplay.setHorizontalTextPosition(SwingConstants.CENTER);
 		_valueDisplay.setHorizontalAlignment(SwingConstants.CENTER);
 		Font valueFont = new Font("ComicSans", Font.BOLD, 40);
 		_valueDisplay.setForeground(java.awt.Color.RED);
 		_valueDisplay.setFont(valueFont);
 
 
 
 	}
 
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		Graphics2D g2 = (Graphics2D) g.create();
 
 		for(int i = 0 ; i<= _numTicks; i++){
 
 			int tickHeight = ((_height-(2*_insets)-2*_imageOff-_textPanelHeight)/_numTicks)*(i);
 			int width =0;
 			if(i% _tickCycle<=(_tickCycle/2)){
 				width = (i%_tickCycle)*_widthTicks + i%_tickCycle;
 			}
 			else{
 				width = (_tickCycle-(i%_tickCycle))*_widthTicks + i%_tickCycle;
 			}
 
 			g2.setColor(_tickColor2);
 			g2.setStroke(_tickStroke2);
 			g2.drawLine((_width/2)-width, tickHeight +_imageOff+_insets, (_width/2)+width, tickHeight+_imageOff+_insets);
 
 			g2.setColor(_tickColor);
 			g2.setStroke(_tickStroke);
 			g2.drawLine((_width/2)-width, tickHeight+_imageOff+_insets, (_width/2)+width, tickHeight+_imageOff+_insets);
 
 
 		}
 
 		g2.setColor(_tubeColor);
 		g2.setStroke(_tubeStroke);
 		g2.drawLine(_width/2,0+_insets+_imageOff,_width/2,_height - _insets -_imageOff-_textPanelHeight);
 
 		g2.setColor(_tubeColor2);
 		g2.setStroke(_tubeStroke2);
 		g2.drawLine(_width/2,0+_insets+_imageOff,_width/2,_height - _insets -_imageOff-_textPanelHeight);
 
 		g2.setColor(_tubeColor2);
 		g2.setStroke(_tubeStroke2);
 		g2.drawLine(_width/2,0+_insets+_imageOff,_width/2,_height - _insets -_imageOff-_textPanelHeight);
 
 		g2.setColor(_tubeFillColor);
 		g2.setStroke(_tubeFillStroke);
 		g2.drawLine(_width/2 ,_height-_insets-_imageOff-_textPanelHeight,_width/2,  _insets+ _sliderValue+_imageOff);
 
 		_image.paint(g, this);
 	} 
 
 	private class SliderListener implements MouseMotionListener{
 
 		@Override
 		public void mouseDragged(MouseEvent e) {
 			if(e.getY() <= _height-_insets-_imageOff-_textPanelHeight && e.getY() >= 0 + _imageOff+_insets){
 				if(_image.containsPoint(e.getX(), e.getY())){
 
 					SettingsSlider.this.setLevel(e.getY());
 					setSlider((int) e.getY()-_imageOff-_insets);
 					_image.setLocation( (_width/2) -_image.getWidth()/2,(int) e.getY()-_imageOff-_insets ) ;
 					_valueDisplay.setText(Integer.toString((int)((1- ((double)e.getY()-_imageOff-_insets)/((double) _sliderHeight))*_constantMax)));
 					_constant = ((int)((1- ((double)e.getY()-_imageOff-_insets)/((double) _sliderHeight))*_constantMax));
 					_parent.setPreference(_settingType, _constant);
 					repaint();
 				}
 			}
 		}
 		@Override
 		public void mouseMoved(MouseEvent e) {}
 	}
 
 	public void setSlider(int sliderValue){
 		_sliderValue = sliderValue;
 		this.repaint();
 	}
 
 	public void processPoint(int x, int y){
		if(y <= _height-_insets-_imageOff-_textPanelHeight && y >= 0 + _imageOff+_insets){
 			if(x>= this.getX() && x<= this.getX()+_width){
 				if (!_sliding) {
 					_sliding = true;
 					Timeline timeline = new Timeline(this);
					timeline.addPropertyToInterpolate("level", _level, y);
 					timeline.setDuration(1000);
 					timeline.setEase(new Spline(0.5f));
 					timeline.addCallback(new SliderCallback(timeline));
 					timeline.play();
 					//this.setLevel(y);
 				}
 			}
 		}
 	}
 	
 	public void setLevel(int y) {
 		_level = y;
 		this.setSlider((int) y-_imageOff-_insets);
 		_image.setLocation( (_width/2) -_image.getWidth()/2,(int) y-_imageOff-_insets ) ;
 		_valueDisplay.setText(Integer.toString((int)((1- ((double)y-_imageOff-_insets)/((double) _sliderHeight))*_constantMax)));
		_constant = ((int)((1- ((double)y-_imageOff-_insets)/((double) _sliderHeight))*_constantMax));
 		_parent.setPreference(_settingType, _constant);
 		repaint();
 	}
 
 	public void inTransition(boolean transitioning) {}
 
 	@Override
 	public void setDefaultLocation(int x, int y) {
 		setLocation(x, y);
 		_defaultX = x;
 		_defaultY = y;
 	}
 
 	@Override
 	public void setX(int x) {
 		setLocation(x, getY());
 	}
 
 	@Override
 	public void setY(int y) {
 		setLocation(getX(), y);
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
 	public void setAlpha(float newAlpha) {}
 	
 	private class SliderCallback implements TimelineCallback{
 
 		private Timeline _timeline;
 		public SliderCallback(Timeline timeline) {
 			_timeline = timeline;
 		}
 
 		@Override
 		public void onTimelinePulse(float arg0, float arg1) {}
 
 		@Override
 		public void onTimelineStateChanged(TimelineState arg0,
 				TimelineState arg1, float arg2, float arg3) {
 			if (arg0.equals(TimelineState.PLAYING_FORWARD) && arg1.equals(TimelineState.DONE)) {
 				_sliding = false;
 				_timeline.removeCallback(this);
 			}		
 		}
 	}
 }
