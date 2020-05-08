 package gui;
 
 import static gui.GuiConstants.LARGE_BUTTON;
 import static gui.GuiConstants.LARGE_BUTTON_RADIUS;
 import static gui.GuiConstants.SMALL_BUTTON;
 import static gui.GuiConstants.STANDARD_GROWTH_FACTOR;
 import static sound.MusicConstants.REVERSE_MAJOR_CHORD;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiUnavailableException;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 import sound.Clip;
 import sound.MidiPlayer;
 import sound.NotePlayerChannel;
 
 @SuppressWarnings("serial")
 public class ClipOptionsPane extends JPanel {
 
 	private ArrayList<ClipOption> _options = new ArrayList<ClipOption>();
 	private ArrayList<Component> _deleteConfirmation = new ArrayList<Component>();
 	private PlayPauseButton _playPause;
 	
 	private ClipOption _deleteForReals;
 	public ClipOption _justKidding;
 	
 	private boolean _showDeleteConfirmation = false;
 	
 	private Timer _playTimer;
 	private double _percentPlayed = 0;
 	
 	private LibraryScreen _library;
 	private MidiPlayer _player;
 	
 	private LabelElement _clipName;
 
 	
 	public ClipOptionsPane(LibraryScreen library, NotePlayerChannel noteChannel) {
 		super();
 		this.setSize(library.getSize());
 		this.setBackground(new Color(0, 0, 0, 0));
 		this.setLocation(0, 0);
 		
 		_clipName = new LabelElement("", 32, library.getWidth()*4/5, 32);
 		_clipName.setLocation(library.getWidth()/10, library.getHeight()/5);
 		add(_clipName);
 
 		_library = library;
 		try {
 			_player = new MidiPlayer();
 		} catch (MidiUnavailableException e1) {
 			System.out.println("ERROR: problem making midi player");
 		}
 
     
 		// PLAY/PAUSE BUTTON
 		_playPause = new PlayPauseButton(_library);
 		_options.add(_playPause);
 
 		// DELETE BUTTON
 		_options.add(new ClipOption(_library, LARGE_BUTTON, "img/greenbutton.png", "img/deletetext.png", noteChannel) {
 			@Override
 			public void performAction() {
 				_player.stop();
 				_playTimer.stop();
 				changePlayPercentage(0);
 				_playPause.setImageText("img/playcliptext.png");
 				showDeleteConfirmation();
 			}
 		});
 
 		int columnWidth = 50 + LARGE_BUTTON;
 		
 		// position clip options
 		int startX = library.getWidth() / 2 - LARGE_BUTTON - 25;
 		for (int i = 0; i < _options.size(); i++) {
 			_options.get(i).setDefaultLocation(startX + i * columnWidth,
 					library.getHeight()/2 - LARGE_BUTTON_RADIUS);
 			this.add(_options.get(i));
 		}
 		
 		
 		// CANCEL BUTTON ('X' in upper right)
 		ClipOption cancel = new ClipOption(_library, SMALL_BUTTON, "img/redbutton.png", "img/exittext.png", noteChannel) {
 			@Override
 			public void performAction() {
 				_player.stop();
 				_playTimer.stop();
 				changePlayPercentage(0);
 				_playPause.setImageText("img/playcliptext.png");
 				_library.hideOptions();
 			}
 		};
 		this.add(cancel);
 		int distFromBorder = 17;
 		cancel.setDefaultLocation(getWidth()*9/10 - SMALL_BUTTON - distFromBorder, getHeight()/10 + distFromBorder);
 		
 		
 		
 		// DELETE? NO
 		_justKidding = new ClipOption(_library, LARGE_BUTTON, "img/redbutton.png", "img/backtext.png", noteChannel) {
 			@Override
 			public void performAction() {
 				hideDeleteConfirmation();
 			}
 		};
 		_deleteConfirmation.add(_justKidding);
 		
 		// DELETE? YES
 		_deleteForReals = new ClipOption(_library, LARGE_BUTTON, "img/greenbutton.png", "img/deletetext.png", noteChannel) {
 			@Override
 			public void performAction() {
				this.setLocation(_library.getWidth()/2 - LARGE_BUTTON - 25,
						_library.getHeight()/2 - LARGE_BUTTON_RADIUS);
 				_player.stop();
 				_playTimer.stop();
 				changePlayPercentage(0);
 				_playPause.setImageText("img/playcliptext.png");
 				if (_clip != null)
 					_library.deleteClip(_clip);
 				hideDeleteConfirmation();
				_library.hideOptions();
 			}
 		};
 		_deleteConfirmation.add(_deleteForReals);
 		
 		LabelElement label = new LabelElement("Delete for reals?", 32, library.getWidth()*4/5, 32);
 		label.setLocation(library.getWidth()/10, library.getHeight()/5 + _clipName.getHeight() + 32);
 		label.setForeground(Color.RED);
 		_deleteConfirmation.add(label);
 	}
 	
 	public void startPlaying() {
 		_playPause.performAction();
 	}
 
 	public void setClip(Clip clip) {
 		_clipName.setText(clip.getFilename().replace("files/", ""));
		_clipName.setLocation(_library.getWidth()/10, _library.getHeight()/5);
 		
 		int tickLength = 50;
 		_playTimer = new Timer(tickLength, new PlayTimerListener((int)clip.getLength()/tickLength));
 		
 		_deleteForReals.setClip(clip);
 		for (ClipOption option : _options)
 			option.setClip(clip);
 		hideDeleteConfirmation();
 	}
 	
 	private void showDeleteConfirmation() {
 		_showDeleteConfirmation = true;
 		for (ClipOption option : _options)
 			this.remove(option);
 		for (Component c : _deleteConfirmation)
 			this.add(c);
 		
 		_deleteForReals.setLocation(_library.getWidth()/2 - LARGE_BUTTON - 25,
 				_library.getHeight()/2 - LARGE_BUTTON_RADIUS);
 		_justKidding.setLocation(_library.getWidth()/2 + 25, 
 				_library.getHeight()/2 - LARGE_BUTTON_RADIUS);
 				
 		_library.repaint();
 	}
 	
 	private void hideDeleteConfirmation() {
 		_showDeleteConfirmation = false;
 		for (Component c : _deleteConfirmation)
 			this.remove(c);
 		for (ClipOption option : _options)
 			this.add(option);
 		
 		_options.get(0).setLocation(_library.getWidth()/2 - LARGE_BUTTON - 25,
 				_library.getHeight()/2 - LARGE_BUTTON_RADIUS);
 		_options.get(1).setLocation(_library.getWidth()/2 + 25, 
 				_library.getHeight()/2 - LARGE_BUTTON_RADIUS);
 		
 		_library.repaint();
 	}
 
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 
 		Graphics2D g2 = (Graphics2D) g;
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 
 		int rectX = getWidth()/10;
 		int rectY = getHeight()/10;
 		int rectWidth = getWidth() * 4/5;
 		int rectHeight = getHeight() * 4/5;
 		int arcWidth = 50;
 
 		// white rectangle
 		g2.setColor(Color.WHITE);
 		g2.fillRoundRect(rectX, rectY, rectWidth, rectHeight, arcWidth, arcWidth);
 
 		// white rectangle outline
 		g2.setColor(Color.BLACK);
 		g2.setStroke(new BasicStroke(5));
 		g2.drawRoundRect(rectX, rectY, rectWidth, rectHeight, arcWidth, arcWidth);
 
 		if (!_showDeleteConfirmation) {
 			// play-time bar
 			g2.setColor(Color.RED);
 			g2.fillRoundRect(rectX + 50, rectY + rectHeight * 4/5, (int)(_percentPlayed * (rectWidth - 100)), 15, 2, 2);
 
 			// play-time bar outline
 			g2.setColor(Color.GRAY);
 			g2.drawRoundRect(rectX + 50, rectY + rectHeight * 4/5, rectWidth - 100, 15, 2, 2);	
 		}
 	}
 
 	private void changePlayPercentage(double percent) {
 		_percentPlayed = percent;
 		_library.repaint();
 	}
 
 	private abstract class ClipOption extends HoverButton {	
 		protected Clip _clip;
 		public ClipOption(Container pane, int size, String imageBase, String imageText, NotePlayerChannel noteChannel) {
 			super(pane, imageBase, imageText, size, STANDARD_GROWTH_FACTOR, noteChannel);
 			_chord = REVERSE_MAJOR_CHORD;
 		}
 		public void setClip(Clip clip) {
 			_clip = clip;
 		}
 	}
 
 	private class PlayPauseButton extends ClipOption {
 		
 		public PlayPauseButton(LibraryScreen library) {
 			super(library, LARGE_BUTTON, "img/cyanbutton.png", "img/playcliptext.png", null);
 		}
 
 		@Override
 		public void performAction() {
 			if (_clip != null) {
 				if (!_playTimer.isRunning() && _percentPlayed == 0 || _percentPlayed == 1) {
 					setImageText("img/pausecliptext.png");
 					setImageBase("img/bluebutton.png");
 					_player.stop();
 					PlayTimerListener listener = (PlayTimerListener)_playTimer.getActionListeners()[0];
 					listener.restart();
 					_playTimer.start();
 					try {
 						_clip.play(_player);
 					} catch (MidiUnavailableException | InvalidMidiDataException e) {
 						System.out.println("ERROR: problem playing clip");
 					}
 				} else if (!_playTimer.isRunning()) {
 					setImageText("img/pausecliptext.png");
 					setImageBase("img/bluebutton.png");
 					_playTimer.start();
 					_player.continuePlaying();
 				} else {
 					setImageText("img/playcliptext.png");
 					setImageBase("img/cyanbutton.png");
 					_playTimer.stop();
 					_player.pause();
 				}
 			}
 		}
 	}
 
 	private class PlayTimerListener implements ActionListener {
 		private int _maxTicks, _ticks;
 		public PlayTimerListener(int maxTicks) {
 			_ticks = 0;
 			_maxTicks = maxTicks;
 		}
 		public void restart() {
 			_ticks = 0;
 		}
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			_ticks++;
 			changePlayPercentage((double)_ticks/_maxTicks);
 			if (_ticks == _maxTicks) {
 				_playPause.setImageText("img/playcliptext.png");
 				_playTimer.stop();
 				_ticks = 0;
 			}
 		}
 	}
 }
