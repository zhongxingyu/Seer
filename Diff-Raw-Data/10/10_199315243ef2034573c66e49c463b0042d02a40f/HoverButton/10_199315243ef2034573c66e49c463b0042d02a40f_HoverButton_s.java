 package gui;
 import static gui.GuiConstants.IMAGE_BUFFER_SIZE;
 import static gui.GuiConstants.STANDARD_IMAGE_SIZE;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Arc2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.Timer;
 import javax.swing.border.Border;
 
 import org.pushingpixels.trident.Timeline;
 import org.pushingpixels.trident.Timeline.TimelineState;
 import org.pushingpixels.trident.callback.TimelineCallback;
 
 import sound.MusicConstants;
 import sound.NotePlayerChannel;
 
 public abstract class HoverButton extends JButton implements TransitionElement {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private boolean _inButton = false;
 	private boolean _inTransition = false;
 	private BufferedImage _bufferBase;
 	private BufferedImage _bufferText;
 	private int _radiusGrowth = 0;
 	private int _buttonSize= 0;
 	private int _timerAngle = 0;
 	protected Timeline _timeline;
 	private boolean _playing = false;
 	private HoverButton _this = this;
 	private int _x, _y, _defaultX, _defaultY;
 	private float _alpha = 1.0f;
 	private Container _pane;
 	private final double _scale1;
 	private final double _scale2;
 	private NotePlayerChannel _noteChannel;
 	protected int[] _chord;
 	private Timer _playTimer, _stopTimer;
 
 	public HoverButton(Container pane, String imageBase,  String imageText, int buttonSize, double growthFactor, NotePlayerChannel noteChannel){
 		_pane = pane;
 		_buttonSize = buttonSize;
 		_noteChannel = noteChannel;
 
 		//		 Insets insets = pane.getInsets();
 		//		 this.setBounds(BUTTON_SIZE + insets.left, BUTTON_SIZE + insets.top, this.getPreferredSize().width, this.getPreferredSize().height);
 
 		this.setBounds(buttonSize + IMAGE_BUFFER_SIZE, buttonSize + IMAGE_BUFFER_SIZE, this.getPreferredSize().width, this.getPreferredSize().height);
 
 		_scale1 = (double) buttonSize/STANDARD_IMAGE_SIZE;
 		_scale2 = (double) buttonSize*growthFactor/STANDARD_IMAGE_SIZE;
 
 		_radiusGrowth = (((int) (buttonSize*growthFactor) - buttonSize) / 2);
 
 		try {
 			_bufferBase = ImageIO.read(new File(imageBase));
 			_bufferText = ImageIO.read(new File(imageText));
 		} catch (IOException e) {
 			System.out.println("Invalid image names");
 		}
 
 		this.addMouseMotionListener(new HoverListener(this));
 		this.addMouseListener(new ExitComponentListener(this));
 		Border emptyBorder = BorderFactory.createEmptyBorder();
 		this.setBorder(emptyBorder);
 
 		_timeline = new Timeline(this);
 		_timeline.addPropertyToInterpolate("timerAngle", 0, 360);
 		_timeline.setDuration(1000);
 		_timeline.addCallback(new MyTimelineCallback());
 
 		/*
 		 * For chime (second inversion major chord)
 		 */
 		_chord = new int[3];
 		_chord[0] = 67;
 		_chord[1] = _chord[0] + 5;
 		_chord[2] = _chord[1] + 4;
 	}
 	
 	public void setImageBase(String imageBase) {
 		try {
 			_bufferBase = ImageIO.read(new File(imageBase));
 		} catch (IOException e) {
 			System.out.println("ERROR: invalid image name");
 		}
 	}
 	
 	public void setImageText(String imageText) {
 		try {
 			_bufferText = ImageIO.read(new File(imageText));
 		} catch (IOException e) {
 			System.out.println("ERROR: invalid image name");
 		}
 	}
 	
 	public void setTimerAngle(int newValue) {
 		_timerAngle = newValue;
 		this.repaint();
 	}
 
 	public void setX(int newX) {
 		this.setLocation(newX, _y);
 		_pane.repaint();
 	}
 
 	public void setY(int newY) {
 		this.setLocation(_x, newY);
 		_pane.repaint();
 	}
 
 	public void setAlpha(float newAlpha) {
 		_alpha = newAlpha;
 		_pane.repaint();
 	}
 
 	public void setDefaultLocation(int x, int y) {
 		this.setLocation(x, y);
 		_x = x;
 		_y = y;
 		_defaultX = x;
 		_defaultY = y;
 	}
 
 	@Override
 	public void setLocation(int x, int y) {
 		_x = x;
 		_y = y;
 		super.setLocation(x, y);
 	}
 
 	public int getX() {
 		return _x;
 	}
 
 	public int getY() {
 		return _y;
 	}
 
 	public int getDefaultX() {
 		return _defaultX;
 	}
 	public int getDefaultY() {
 		return _defaultY;
 	}
 
 	@Override
 	public Dimension getPreferredSize() {
 		Dimension size = super.getPreferredSize();
 		size.setSize(_buttonSize + IMAGE_BUFFER_SIZE, _buttonSize + IMAGE_BUFFER_SIZE);
 		return size;
 	}
 
 	public void enable() {
 		_inTransition = false;
 	}
 
 	public void disable() {
 		_inTransition = true;
 	}
 
 	public void chime() {
 		if (_noteChannel != null) {
 			_playTimer = new Timer(55, new ActionListener() {
 				private int _counter = 0;
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					if(_counter < 3) {
 						_noteChannel.noteOn(_chord[_counter], MusicConstants.DEFAULT_VELOCITY);
 						_counter++;
 					} else {
 						HoverButton.this._playTimer.stop();
 					}
 				}
 			});
 			_stopTimer = new Timer(500, new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					for (int i : _chord) {
 						_noteChannel.noteOff(i);
 					}
 				}
 			});
 			_stopTimer.setRepeats(false);			
 			_playTimer.start();
 			_stopTimer.start();
 		}
 	}
 
 	public abstract void performAction();
 
 	public void modifyButton(boolean grow) {
 		if (!_inTransition) {
 			if (!_inButton && grow) {
 				_inButton = true;
 				this.setSize(this._buttonSize+(2*this._radiusGrowth) + IMAGE_BUFFER_SIZE,this._buttonSize+(2*this._radiusGrowth) + IMAGE_BUFFER_SIZE);
 				this.setLocation(this.getX()-this._radiusGrowth, this.getY()-this._radiusGrowth);
 				if (!_playing) {
 					_timeline.play();
 					_playing = true;
 				}
 				this.repaint();
 			}
 			else if (_inButton && !grow){
 				_inButton = false;
 				this.setSize(this._buttonSize + IMAGE_BUFFER_SIZE,this._buttonSize + IMAGE_BUFFER_SIZE);
 				this.setLocation(this.getX()+this._radiusGrowth, this.getY()+this._radiusGrowth);
 				if (_playing) {
 					_timeline.playReverse();
 					_playing = false;
 				}
 				this.repaint();
 			}
 		}
 	}
 
 	@Override
 	protected void paintComponent(Graphics g) {
 		Graphics2D brush = (Graphics2D) g;
 		Graphics2D unscaled = (Graphics2D) g.create();
 		if (_inButton) {
 			brush.scale(_scale2, _scale2);
 		} else {
 			brush.scale(_scale1, _scale1);
 		}
 		brush.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		brush.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha));
 		brush.drawImage(_bufferBase,0,0,this);
 		if (_timerAngle != 0) {
 			Arc2D.Double arc = new Arc2D.Double(0, 0, this.getWidth() - IMAGE_BUFFER_SIZE, this.getHeight() - IMAGE_BUFFER_SIZE, 90, -1 * _timerAngle, Arc2D.PIE);
 			unscaled.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			unscaled.setColor(new Color(245,245,245));
 			unscaled.draw(arc);
 			unscaled.fill(arc);
 		}
 		brush.drawImage(_bufferText,0,0,this);
 		brush.scale(1/_scale1, 1/_scale1);
 	}
 
 	private class HoverListener implements MouseMotionListener{
 		private HoverButton parent;
 		public HoverListener(HoverButton parent){
 			this.parent = parent;
 		}
 		@Override
 		public void mouseDragged(MouseEvent e) {}
 
 		@Override
 		public void mouseMoved(MouseEvent e) {
 
 			int currentX = e.getX();
 			int currentY = e.getY();
 			if(Math.sqrt((currentX-((parent.getWidth()-IMAGE_BUFFER_SIZE)/2))*(currentX-((parent.getWidth()-IMAGE_BUFFER_SIZE)/2)) + 
 					(currentY-((parent.getHeight()-IMAGE_BUFFER_SIZE)/2))*(currentY-((parent.getHeight()-IMAGE_BUFFER_SIZE)/2)))
 					<=(((parent.getWidth()-IMAGE_BUFFER_SIZE)/2))){
 				parent.modifyButton(true);
 
 			} else {
 				parent.modifyButton(false);
 			}	
 		}
 	}
 
 	private class ExitComponentListener implements MouseListener{
 		private HoverButton parent;
 		public ExitComponentListener(HoverButton parent){
 			this.parent = parent;
 		}
 		@Override
 		public void mouseClicked(MouseEvent e) {}
 
 		@Override
 		public void mousePressed(MouseEvent e) {}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 			parent.modifyButton(false);
 		}
 	}
 
 	private class MyTimelineCallback implements TimelineCallback {
 
 		@Override
 		public void onTimelinePulse(float arg0, float arg1) {}
 
 		@Override
 		public void onTimelineStateChanged(TimelineState arg0,
 				TimelineState arg1, float arg2, float arg3) {
 			if (arg0.equals(TimelineState.PLAYING_FORWARD) && arg1.equals(TimelineState.DONE)) {
 				_timerAngle = 0;
				_timeline.replayReverse();
				_timeline.end();
 				_this.modifyButton(false);
 				_this.chime();
 				_this.performAction();
 			}
 		}
 	}
 
 
 }
