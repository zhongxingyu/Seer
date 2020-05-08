 package gui;
 
 import static gui.GuiConstants.SMALL_BUTTON;
 import static sound.MusicConstants.MIDDLE_C;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import sound.MusicConstants;
 import sound.Note;
 
 import com.leapmotion.leap.Controller;
 import com.leapmotion.leap.Finger;
 import com.leapmotion.leap.Frame;
 import com.leapmotion.leap.Hand;
 import com.leapmotion.leap.Tool;
 
 @SuppressWarnings("serial")
 public class ThereminScreen extends PlayingScreen {
 
 	private int _velocity = 100;
 
 	private int _minNote = MIDDLE_C - 12;
 	private int _maxNote = _minNote + 18;
 	private int _currNote = 0;
 
 	private int _minVolume = 0;
 	private int _currVolume;
 
 	private int _minBend = 31;
 	private int _maxBend = 95;
 	private int _currBend;
 
 	private int _minThereminX, _maxThereminX, _minThereminY, _maxThereminY;
 	
 	private int _framesPerPaint = 2;
 	private int _numFrames = 0;
 	
 	private boolean _showCursor = true;
 	
 	private SoundVisualizer _soundVisualizer;
 	private VolumeVisualizer _volVisualizer;
 
 	public ThereminScreen(ContentPanel contentPanel, Dimension dimension, LibraryScreen library){
 		super(contentPanel, dimension, contentPanel.getNoteChannel());
 		this.setLayout(null);
 
 		// record button
 		final RecordButton record = new RecordButton(this, contentPanel, library);
 		record.setDefaultLocation(this.getWidth() - 2*SMALL_BUTTON, 0);
 		this.add(record);
 		this.addElement(record);
 
 		// back button
 		BackButton back = new BackButton(this, contentPanel) {
 			@Override
 			public void performAction() {
 				super.performAction();
 				record.stop();
 			}
 		};
 		back.setDefaultLocation(0,0);
 		this.add(back);
 		this.addElement(back);
 
 		this.setOpaque(false);
 
 		ThereminListener listener = new ThereminListener();
 		this.addMouseMotionListener(listener);
 		this.addMouseListener(listener);
 
 		_minThereminX = 0;
 		_maxThereminX = getWidth()*2/3;
 		_minThereminY = 200;
 		_maxThereminY = getHeight() - 1;
 
 		// add visualizers
 		_soundVisualizer = new SoundVisualizer(this, _maxThereminX - _minThereminX,
 				_maxThereminY - _minThereminY, _maxNote - _minNote, _minNote);
 		_soundVisualizer.setDefaultLocation(0, 200);
 		this.add(_soundVisualizer);	
 		_elements.add(_soundVisualizer);
 
 		
 		_volVisualizer = new VolumeVisualizer(_maxThereminX, _minThereminY,
 				getWidth() - _maxThereminX, getHeight() - _minThereminY);
 		_elements.add(_volVisualizer);
 		System.out.println("VOL WDTHT" + _volVisualizer.getWidth());
 		
 //		// add note labels
 //		_noteLabels = new ArrayList<LabelElement>();
 //		for (int i = 0; i < _maxNote - _minNote; i++) {
 //			int note = _minNote + i;
 //			if (MusicConstants.USE_MAJOR_SCALE)
 //				for (int scaleDegree = 0; scaleDegree < i; scaleDegree++)
 //					note = Note.getNextWhiteKey(note);
 //
 //			LabelElement label  = new LabelElement(new Note(note).getLabel(), 20,
 //					_width / (_maxNote - _minNote), 20);
 //			label.setDefaultLocation(i * label.getWidth(), getHeight() - label.getHeight());
 //			System.out.println("default location: " + label.getDefaultX() + ", " + label.getDefaultY());
 //			_noteLabels.add(label);
 //		}
 		
 		repaint();
 	}
 
 	@Override
 	public void transitionOut(boolean toLeft) {
 		super.transitionOut(toLeft);
 		reset();
 	}
 
 	@Override
 	public void transitionIn() {
 		super.transitionIn();
 		_soundVisualizer.setNumNotes(MusicConstants.THEREMIN_RANGE);
 		reset();
 	}
 
 	private void reset() {
 		_fingerCircles = new ArrayList<SmartCircle>();
 		this.updateVolume(1);
 		this.updatePitchBend((float)0.5);
 		_currNote = 0;
 	}
 
 	private void updateVolume(float percent) {
 		if (!_active)
 			return;
 
 		int volume = Math.round(_minVolume + percent * (MusicConstants.PLAY_VOLUME - _minVolume));
 		if (volume != _currVolume) {
 			_noteChannel.setVolume(volume);
 			_currVolume = volume;
 		}
 
 		_soundVisualizer.setAmplitude(percent);
 		_volVisualizer.setVolume(volume);
 	}
 
 	private void updatePitch(float percent) {
 		if (!_active)
 			return;
 
 		int note = _minNote;
 		if (!MusicConstants.USE_MAJOR_SCALE)
 			note = (int) (_minNote + percent * (_maxNote - _minNote));
 		else
 			for (int i = 0; i < (int)(percent * (_maxNote - _minNote)); i++)
 				note = Note.getNextWhiteKey(note);
 
 		if (note != _currNote) {
 			if (_currNote > 0 && _currNote < 128)
 				_noteChannel.noteOff(_currNote);
 			_noteChannel.noteOn(note, _velocity);
 			_currNote = note;
 		}
 		
 		double freq = Math.pow(2, percent * (_maxNote - _minNote) / 12.0);
 		_soundVisualizer.setFrequency(freq);
 	}
 
 	private void updatePitchBend(float percent) {
 		if (!_active)
 			return;
 
 		int bend = Math.round(_minBend + percent * (_maxBend - _minBend));
 		if (bend != _currBend) {
 			_noteChannel.setPitchBend(bend);
 			_currBend = bend;
 		}	
 	}
 
 	private class ThereminListener implements MouseMotionListener, MouseListener {
 
 		@Override
 		public void mouseDragged(MouseEvent e) {}
 
 		@Override
 		public void mouseMoved(MouseEvent e) {
 			if (_showCursor && e.getY() > _volVisualizer.getY()) {
 				_showCursor = false;
 				Toolkit toolkit = Toolkit.getDefaultToolkit();
 				Cursor cursor = toolkit.createCustomCursor(toolkit.getImage(""), new Point(0,0), "Pointer"); 
 				setCursor(cursor);
 			}
 
 			else if (!_showCursor && e.getY() < _volVisualizer.getY()) {
 				_showCursor = true;
 				Toolkit toolkit = Toolkit.getDefaultToolkit();
 				Image icon = toolkit.getImage("cursor.png");
 				Point hotSpot = new Point(11, 3);
 				Cursor cursor = toolkit.createCustomCursor(icon, hotSpot, "Pointer"); 
 				setCursor(cursor);
 			}
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
 		public void mouseExited(MouseEvent e) {}
 	}
 
 	@Override
 	public void onFrame(Controller controller, Frame frame) {
 		int currentHand = 0;
 		_fingerCircles = new ArrayList<SmartCircle>();
 		_fingerCircles = Collections.synchronizedCollection(_fingerCircles);
 
 		boolean onePitchFinger = false;
 		boolean oneVolumeFinger = false;
 		SmartCircle pitchCircle = null;
 		SmartCircle volumeCircle = null;
 		
 		synchronized(_fingerCircles) {
 			for (Tool tool : frame.tools()) {
 				Point2D.Double inter = LeapListener.screenIntersection(controller, tool);
 
 				if (inter.y < _volVisualizer.getY())
 					continue;
 				
 				SmartCircle circle = new SmartCircle(inter.x, inter.y);
 				circle.setColor(_colors[currentHand % _colors.length]);
 				_fingerCircles.add(circle);
 				currentHand++;
 				
 				if (_volVisualizer.contains((int)inter.x, (int)inter.y)) {
 					if (volumeCircle == null) {
 						oneVolumeFinger = true;
 						volumeCircle = circle;
 					} else oneVolumeFinger = false;
 				} else if (_soundVisualizer.contains((int)inter.x, (int)inter.y)) {
 					if (pitchCircle == null) {
 						onePitchFinger = true;
 						pitchCircle = circle;
 					} else onePitchFinger = false;
 				}
 			}
 
 			for (Hand hand : frame.hands()) {
 				for (Finger finger : hand.fingers()) {
 					Point2D.Double inter = LeapListener.screenIntersection(controller, finger);	
 					
 					if (inter.y < _volVisualizer.getY())
 						continue;
 					
 					SmartCircle circle = new SmartCircle(inter.x, inter.y);
 					circle.setColor(_colors[currentHand % _colors.length]);
 					_fingerCircles.add(circle);
 					
 					if (_volVisualizer.contains((int)inter.x, (int)inter.y)) {
 						if (volumeCircle == null) {
 							oneVolumeFinger = true;
 							volumeCircle = circle;
 						} else oneVolumeFinger = false;
 					} else if (_soundVisualizer.contains((int)inter.x, (int)inter.y)) {
 						if (pitchCircle == null) {
 							onePitchFinger = true;
 							pitchCircle = circle;
 						} else onePitchFinger = false;
 					}
 				}
 				currentHand++;
 			}
 		}
 		
 		if (onePitchFinger) {
 			updatePitch((float) (pitchCircle.getX() - _minThereminX) / (_maxThereminX - _minThereminX));
 			updatePitchBend(1 - (float) (pitchCircle.getY() - _minThereminY) / (_maxThereminY - _minThereminY));
 		}
 		
 		if (oneVolumeFinger)
 			updateVolume(1 - (float) (volumeCircle.getY() - _minThereminY) / (_maxThereminY - _minThereminY));
 
 		_numFrames = (_numFrames+1) % _framesPerPaint;
 		if (_numFrames == 0)
 			this.repaint();
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		super.paint(g);
 		
 		_volVisualizer.paint((Graphics2D) g);
 		synchronized(_fingerCircles) {
 			for (SmartCircle circle : _fingerCircles)
 				circle.paint(g);
 		}
 		
 		// draw divider line
 		Graphics2D g2 = (Graphics2D) g;
 		g2.setColor(Color.BLACK);
 		g2.setStroke(new BasicStroke(5));
 		g2.drawLine(_soundVisualizer.getX(), _volVisualizer.getY(),
				_volVisualizer.getX() + getWidth(), _volVisualizer.getY());
 	}
 
 	@Override
 	public void screenTap(int x, int y) {}
 }
