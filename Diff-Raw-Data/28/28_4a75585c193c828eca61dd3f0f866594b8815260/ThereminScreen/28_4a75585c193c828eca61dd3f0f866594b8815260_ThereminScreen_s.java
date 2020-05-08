 package gui;
 
 import static gui.GuiConstants.*;
 import static sound.MusicConstants.DEFAULT_VOLUME;
 import static sound.MusicConstants.MIDDLE_C;
 import static sound.MusicConstants.NO_PITCH_BEND;
 
 import java.awt.Dimension;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import sound.Note;
 import sound.NotePlayerChannel;
 
 public class ThereminScreen extends PlayingScreen {
 	
 	private int _velocity = 100;
 
 	private int _minNote = MIDDLE_C - 12;
 	private int _maxNote = _minNote + 24;
 	private int _currNote = 0;
 
 	private int _minVolume = 0;
 	private int _maxVolume = 127;
 	private int _currVolume = DEFAULT_VOLUME;
 	
 	private int _minBend = 0;
 	private int _maxBend = 127;
 	private int _currBend = NO_PITCH_BEND;
 	
 	private int _minThereminX, _maxThereminX, _minThereminY, _maxThereminY;
	
	private VolumeVisualizer _volumeVisualizer;
 	private SoundVisualizer _soundVisualizer;
 
 	public ThereminScreen(ContentPanel contentPanel, Dimension dimension, LibraryScreen library){
 		super(contentPanel, dimension, contentPanel.getNoteChannel());
 		this.setLayout(null);
 		
 		BackButton back = new BackButton(this, contentPanel);
 		back.setDefaultLocation(0,0);
 		this.add(back);
 		this.addElement(back);
 		
 		
 		RecordButton record = new RecordButton(this, contentPanel, library);
 		record.setDefaultLocation(this.getWidth()/2 - GuiConstants.LARGE_BUTTON_RADIUS, 0);
 		this.add(record);
 		this.addElement(record);
 		
 		this.setOpaque(false);
 		
 		_noteChannel.setVolume(_currVolume);
 		_noteChannel.setPitchBend(_currBend);
 		
 		ThereminListener listener = new ThereminListener();
 		this.addMouseMotionListener(listener);
 		this.addMouseListener(listener);
 		
		_volumeVisualizer = new VolumeVisualizer();
		_volumeVisualizer.setLocation(200,40);
//		this.add(_volumeVisualizer);
		
		
 		_minThereminX = 0;
 		_maxThereminX = getWidth() - 1;
 		_minThereminY = 200;
 		_maxThereminY = getHeight() - 1;
 		
 		_soundVisualizer = new SoundVisualizer(_maxThereminX - _minThereminX, _maxThereminY - _minThereminY);
 		_soundVisualizer.setDefaultLocation(0, 200);
 		this.add(_soundVisualizer);	
 		_elements.add(_soundVisualizer);
 	}
 
 	@Override
 	public void transitionOut(boolean toLeft) {
 		super.transitionOut(toLeft);
 		reset();
 	}
 	
 	@Override
 	public void transitionIn() {
 		super.transitionIn();
 		reset();
 	}
 	
 	private void reset() {
 		_currVolume = DEFAULT_VOLUME;
 		_currBend = NO_PITCH_BEND;
 		_currNote = 0;
 	}
 	
 	private void updateVolume(float percent) {
 		if (!_active)
 			return;
 		
 		int volume = Math.round(_minVolume + percent * (_maxVolume - _minVolume));
 		if (volume != _currVolume) {
 			_noteChannel.setVolume(volume);
 			_currVolume = volume;
			_volumeVisualizer.setVolume( ((double) _currVolume) / ((double) _maxVolume));

 		}
 	}
 
 	private void updatePitch(float percent) {
 		if (!_active)
 			return;
 		
 		int note = Math.round(_minNote + percent * (_maxNote - _minNote));
 //		note = Note.floorToMajorScale(note, MIDDLE_C); // only play notes in C-major scale
 		if (note != _currNote) {
 			if (_currNote > 0 && _currNote < 128)
 				_noteChannel.noteOff(_currNote);
 			_noteChannel.noteOn(note, _velocity);
 			_currNote = note;
 		}
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
 			int x = e.getX();
 			int y = e.getY();
 			if (x < _minThereminX || x > _maxThereminX || y < _minThereminY || y > _maxThereminY) {
 				_noteChannel.allNotesOff();
 				_soundVisualizer.setAmplitude(0);
 				_currNote = -1;
 				return;
 			}
 			
 			x -= _minThereminX;
 			y -= _minThereminY;
 			
 			float yPercent = 1 - (float) y / (_maxThereminY - _minThereminY);
 			updateVolume(yPercent);
 
 			float xPercent = (float) x / (_maxThereminX - _minThereminX);
 			updatePitch(xPercent);
 			
 //			float absNote = _minNote + xPercent * (_maxNote - _minNote);
 //			float diff = absNote - _currNote;
 //			updatePitchBend((float)(diff/4 + 0.5));
 			
 			double freq = Math.pow(2, xPercent * (_maxNote - _minNote) / 12.0);
 			_soundVisualizer.setAmplitude(yPercent);
 			_soundVisualizer.setFrequency(freq);
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
 }
