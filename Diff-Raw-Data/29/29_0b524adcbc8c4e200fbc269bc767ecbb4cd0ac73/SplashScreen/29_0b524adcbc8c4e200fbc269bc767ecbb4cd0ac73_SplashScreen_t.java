 package gui;
 import static gui.GuiConstants.LARGE_BUTTON;
 import static gui.GuiConstants.LARGE_BUTTON_RADIUS;
 import static gui.GuiConstants.STANDARD_GROWTH_FACTOR;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontFormatException;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.Timer;
 
 import org.pushingpixels.trident.Timeline;
 import org.pushingpixels.trident.Timeline.TimelineState;
 import org.pushingpixels.trident.callback.TimelineCallback;
 import org.pushingpixels.trident.ease.Spline;
 
 import sound.MusicConstants;
 import sound.NotePlayerChannel;
 
 
 public class SplashScreen extends Screen {
 
 
 	private static final long serialVersionUID = 8244019662589461689L;
 	private ContentPanel _contentPanel;
 	private SmartImage[] _virtuoso;
 	private ArrayList<SmartImage> _toDraw;
 	private int _counter;
 	private Timeline _fadeIn, _timeline;
 	private Timer _timer;
 	private NotePlayerChannel _noteChannel;
 	private PushScreenButton _button;
 	private final double _scaleFactor = 1.0;
	private boolean _gone = false;
 	
	@SuppressWarnings("serial")
 	public SplashScreen(ContentPanel contentPanel, Dimension dimension, NotePlayerChannel noteChannel){
 		super(contentPanel, dimension);
 		_contentPanel = contentPanel;
 		_noteChannel = noteChannel;
         setLayout(null);
 
 		_button = new PushScreenButton(this,"img/cyanbutton.png", "img/playtext.png",
				LARGE_BUTTON, STANDARD_GROWTH_FACTOR, contentPanel, ScreenType.MENU) {
			public void modifyButton(boolean grow) {
				if (!_gone)
					super.modifyButton(grow);
			}
		};
         _button.setDefaultLocation(_width*3/5,_height/2-LARGE_BUTTON_RADIUS);
         _button.setAlpha(0.0f);
         this.add(_button);
         
         try {
         	_virtuoso = new SmartImage[8];
             _virtuoso[0] = new SmartImage(ImageIO.read(new File("img/v.png")), _scaleFactor, this);
             _virtuoso[1] = new SmartImage(ImageIO.read(new File("img/i.png")), _scaleFactor, this);
             _virtuoso[2] = new SmartImage(ImageIO.read(new File("img/r.png")), _scaleFactor, this);
             _virtuoso[3] = new SmartImage(ImageIO.read(new File("img/t.png")), _scaleFactor, this);
             _virtuoso[4] = new SmartImage(ImageIO.read(new File("img/u.png")), _scaleFactor, this);
             _virtuoso[5] = new SmartImage(ImageIO.read(new File("img/o1.png")), _scaleFactor, this);
             _virtuoso[6] = new SmartImage(ImageIO.read(new File("img/s.png")), _scaleFactor, this);
             _virtuoso[7] = new SmartImage(ImageIO.read(new File("img/o2.png")), _scaleFactor, this);
         } catch (IOException e) {}
         _toDraw = new ArrayList<SmartImage>();
         int x = _width*1/10;
 		int y = _height/2 - (int) (_virtuoso[0].getHeight()/2.0);
 		for (SmartImage letter : _virtuoso) {
 			letter.setLocation(x, y);
 			x += letter.getWidth()-(5*_scaleFactor);
 		}
 
         this.setOpaque(false);
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 		for (int i = 0; i < _toDraw.size(); i++) {
 			SmartImage letter = _toDraw.get(i);
 			letter.paint(g, this);
 		}
 	}
 	
 	@Override
 	public void transitionIn() {
 		_button.inTransition(true);
 		this.nameAppear();
 	}
 	
 	@Override
 	public void transitionOut(boolean irrelevant) {
		_gone = true;
 		_button.inTransition(true);
 		_timeline = new Timeline(_button);
 		_timeline.addPropertyToInterpolate("x", _button.getX(), LEFT_OFFSCREEN);
 		_timeline.setDuration(2000);
 		_timeline.setEase(new Spline(0.8f));
 		_timeline.addCallback(new OutSplashCallback());
 		_timeline.play();
 		
 	}
 	
 	public void fadeIn() {
 		Timeline timeline = new Timeline(_button);
 		timeline.addPropertyToInterpolate("alpha", 0.0f, 1.0f);
 		timeline.setDuration(1000);
 		timeline.addCallback(new InSplashCallback());
 		timeline.play();
 	}
 	
 	public void nameAppear() {
 		_counter = 0;
 		_timer = new Timer(185, new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				
 				if (_counter == 0) {
 					_toDraw.add(_virtuoso[0]);
 					SplashScreen.this.playNote(_counter);
 					SplashScreen.this.playChord(MusicConstants.MIDDLE_C);
 				} else if (_counter == 1) {
 					// Do nothing, for musical timing
 					_counter++;
 					return;
 				} else if (_counter < 9) {
 					_toDraw.add(_virtuoso[_counter-1]);
 					SplashScreen.this.playNote(_counter - 1);
 				} else {
 					SplashScreen.this._timer.stop();
 					SplashScreen.this.playNote(_counter - 1);
 					SplashScreen.this.fadeIn();
 				}
 				_counter++;
 				SplashScreen.this.repaint();
 			}
 			
 		});
 		_timer.setInitialDelay(1000);
 		_timer.start();
 	}
 	
 	private void playNote(int noteNum) {
 		_noteChannel.setVolume(MusicConstants.DEFAULT_VOLUME);
 		int startNote = MusicConstants.MIDDLE_C;
 		int note = 0;
 		switch (noteNum) {
 		case 0:
 			note = 0;
 			break;
 		case 1:
 			_noteChannel.noteOff(startNote + 0);
 			note = 2;
 			break;
 		case 2:
 			_noteChannel.noteOff(startNote + 2);
 			note = 4;
 			break;
 		case 3:
 			_noteChannel.noteOff(startNote + 4);
 			note = 5;
 			break;
 		case 4:
 			_noteChannel.noteOff(startNote + 5);
 			note = 7;
 			break;
 		case 5:
 			_noteChannel.noteOff(startNote + 7);
 			note = 9;
 			break;
 		case 6:
 			_noteChannel.noteOff(startNote + 9);
 			note = 11;
 			break;
 		case 7:
 			_noteChannel.noteOff(startNote + 11);
 			note = 12;
 			break;
 		case 8:
 			_noteChannel.allNotesOff();
 			return;
 		}
 		_noteChannel.noteOn(startNote + note, Math.min(127, MusicConstants.DEFAULT_VELOCITY + 3*noteNum));	
 	}
 	
 	/**
 	 * Plays a nicely voiced chord. (Designed for a start note of 60)
 	 * @param startNote
 	 */
 	private void playChord(int startNote) {
 		int prevInstr = _noteChannel.getInstrument();
 		int velocity = 50;
 		
 		// bass note
 		_noteChannel.setInstrument(MusicConstants.ELECTRIC_BASS);
 		_noteChannel.noteOn(startNote - 24, 70);
 		
 		// trumpet middle voices
 		_noteChannel.setInstrument(MusicConstants.TRUMPET);
 		_noteChannel.noteOn(startNote - 12, velocity);
 		_noteChannel.noteOn(startNote - 12 + 7, velocity);
 		
 		// violins on top
 		_noteChannel.setInstrument(MusicConstants.VIOLIN);
 		_noteChannel.noteOn(startNote, velocity);
 		_noteChannel.noteOn(startNote + 7, velocity);
 		_noteChannel.noteOn(startNote + 12, velocity);
 		_noteChannel.noteOn(startNote + 12 + 4, velocity);
 		_noteChannel.noteOn(startNote + 12 + 7, velocity);
 		_noteChannel.noteOn(startNote + 24, velocity);
 		_noteChannel.noteOn(startNote + 24 + 4, velocity);
 		
 		// reset note channel
 		_noteChannel.setInstrument(prevInstr);
 	}
 	
 	private class InSplashCallback implements TimelineCallback {
 
 		@Override
 		public void onTimelinePulse(float arg0, float arg1) {}
 
 		@Override
 		public void onTimelineStateChanged(TimelineState arg0,
 				TimelineState arg1, float arg2, float arg3) {
 			if (arg0.equals(TimelineState.PLAYING_FORWARD) && arg1.equals(TimelineState.DONE)) {
 				_button.inTransition(false);
 			}
 		}
 	}
 
 	private class OutSplashCallback implements TimelineCallback {
 
 		@Override
 		public void onTimelinePulse(float arg0, float arg1) {
 			for (SmartImage letter : _virtuoso) {
 				if (_button.getX() + LARGE_BUTTON_RADIUS < letter.getX() + letter.getWidth()) {
 					_toDraw.remove(letter);
 				}
 			}
 		}
 
 		@Override
 		public void onTimelineStateChanged(TimelineState arg0,
 				TimelineState arg1, float arg2, float arg3) {
 			if (arg0.equals(TimelineState.PLAYING_FORWARD) && arg1.equals(TimelineState.DONE)) {
 				_button.inTransition(false);
 				_contentPanel.continueTransition();
 			}
 		}
 	}
 
 	@Override
 	public void screenTap(int x, int y) {
 		// TODO Auto-generated method stub
 		
 	}
 }
