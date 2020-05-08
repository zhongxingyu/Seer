 package gui;
 
 import static gui.GuiConstants.SMALL_BUTTON;
 import static gui.GuiConstants.STANDARD_GROWTH_FACTOR;
 
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.io.File;
 import java.io.IOException;
 
 import javax.sound.midi.InvalidMidiDataException;
 
 import sound.Clip;
 import sound.MusicConstants;
 
 @SuppressWarnings("serial")
 public class LibraryScreen  extends Screen {
 
 	private TrackManager _trackManager;
 	private HoverButton _nextPage, _prevPage, _back;
 	private LabelElement _label;
 	private boolean _showOptions = false;
 	private ClipOptionsPane _optionsPane;
 
 	public LibraryScreen(ContentPanel contentPanel, Dimension dimension){
 		super(contentPanel, dimension);
 		this.setLayout(null);
 		
 		_optionsPane = new ClipOptionsPane(this, contentPanel.getNoteChannel());
 		
 		_label = new LabelElement("Page 1 of 1", 20, 200, 40);
 		_label.setDefaultLocation(getWidth()/2 - _label.getWidth()/2, getHeight()*9/10);
 		this.add(_label);
 		_elements.add(_label);
 		
 		_back = new BackButton(this, contentPanel);
 		_back.setDefaultLocation(0,0);
 		this.add(_back);
 		_elements.add(_back);
 		
 		int pageButtonSize = SMALL_BUTTON;
 		_nextPage = new HoverButton(this,"img/bluebutton.png","img/righttext.png",
 				pageButtonSize, STANDARD_GROWTH_FACTOR, contentPanel.getNoteChannel()) {
 			@Override
 			public void performAction() {
 				_trackManager.nextPage();
 			}
 		};
 		_nextPage.setDefaultLocation(getWidth() - pageButtonSize, getHeight() - pageButtonSize);
 		_nextPage.setChord(MusicConstants.MAJOR_TRIAD);
 
 		_prevPage = new HoverButton(this,"img/bluebutton.png","img/lefttext.png",
 				pageButtonSize, STANDARD_GROWTH_FACTOR, contentPanel.getNoteChannel()) {
 			@Override
 			public void performAction() {
 				_trackManager.prevPage();
 			}
 		};        
 		_prevPage.setDefaultLocation(0, getHeight() - pageButtonSize);
 		_prevPage.setChord(MusicConstants.REVERSE_MAJOR_TRIAD);
 
 		_trackManager = new TrackManager(this);
 		
 		// add all the files in the "files/" folder
 		File dir = new File("files/");
 		String[] filenames = dir.list();
 		for (String filename : filenames) {
 			if (filename.endsWith(".mid")) {
 				try {
 					addClip(new Clip("files/" + filename));
 				} catch (InvalidMidiDataException | IOException e) {
 					e.printStackTrace();
 					System.out.println("ERROR: problem reading in MIDI clip");
 				}
 			}
 		}
 			
 		this.setOpaque(false);
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		_trackManager.makeLabelsLookRight();
 		super.paint(g);
 		if (_showOptions) {
 			g.setColor(new Color(0, 0, 0, 100));
 			g.fillRect(0, 0, getWidth(), getHeight());
 			try {
 				_optionsPane.paint(g);
 			} catch (NullPointerException e) {} // oh well
 		}
 	}
 	
 	public void showOptions(Clip clip) {
 		_showOptions = true;
 		_trackManager.hide();
 		_back.inTransition(true);
 		_nextPage.inTransition(true);
 		_prevPage.inTransition(true);
 
 		_optionsPane.setClip(clip);
 		_optionsPane.startPlaying();
 		this.add(_optionsPane);
 		this.repaint();
 	}
 	
 	public void hideOptions() {
		_optionsPane.repaint();
 		_showOptions = false;
 		_trackManager.show();
 		_back.inTransition(false);
 		_nextPage.inTransition(false);
 		_prevPage.inTransition(false);
 		this.remove(_optionsPane);
 		this.repaint();
 	}
 	
 	public void addClip(Clip clip) {
 		_trackManager.addClip(clip);
 		_label.repaint();
 	}
 	
 	public void deleteClip(Clip clip) {
 		_trackManager.removeButton(clip);
 	}
 	
 	public void updatePageLabel(int currentPage, int numPages) {
 		_label.setText("Page " + (currentPage+1) + " of " + numPages);
 	}
 
 	@Override
 	public void transitionOut(boolean toLeft) {
 		super.transitionOut(toLeft);
 		_trackManager.stopPlaying();
 	}
 
 	/**
 	 * Displays the "next page" button
 	 */
 	public void addNextPageButton() {
 		this.add(_nextPage);
 		_elements.add(_nextPage);
 	}
 
 	/**
 	 * Displays the "previous page" button
 	 */
 	public void addPrevPageButton() {
 		this.add(_prevPage);
 		_elements.add(_prevPage);
 	}
 
 	/**
 	 * Removes the "next page" button
 	 */
 	public void removeNextPageButton() {
 		this.remove(_nextPage);
 		_elements.remove(_nextPage);
 	}
 
 	/**
 	 * Removes the "previous page" button
 	 */
 	public void removePrevPageButton() {
 		this.remove(_prevPage);
 		_elements.remove(_prevPage);
 	}
 
 	public boolean hasNextPageButton() {
 		return _elements.contains(_nextPage);
 	}
 
 	public boolean hasPrevPageButton() {
 		return _elements.contains(_prevPage);
 	}
 
 	@Override
 	public void screenTap(int x, int y) {}
 }
