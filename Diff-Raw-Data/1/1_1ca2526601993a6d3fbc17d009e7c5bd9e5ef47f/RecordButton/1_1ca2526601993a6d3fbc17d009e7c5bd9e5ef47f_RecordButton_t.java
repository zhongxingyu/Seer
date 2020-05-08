 package gui;
 
 import static gui.GuiConstants.SMALL_BUTTON;
 import static gui.GuiConstants.STANDARD_GROWTH_FACTOR;
 
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.Timer;
 
 import sound.Clip;
 import sound.NotePlayerChannel;
 
 public class RecordButton extends HoverButton {
 	
 	private NotePlayerChannel _noteChannel;
 	private ContentPanel _contentPanel;
 	private LibraryScreen _library;
 	private Timer _blinkTimer;
 
 	public RecordButton(Container pane, ContentPanel contentPanel, LibraryScreen library) {
 		super(pane, "img/redbutton.png","img/rectext.png", SMALL_BUTTON, STANDARD_GROWTH_FACTOR, null);
 		_noteChannel = contentPanel.getNoteChannel();
 		_contentPanel = contentPanel;
 		_library = library;
 		_blinkTimer = new Timer(500, new BlinkListener());
 	}
 
 	@Override
 	public void performAction() {
 		if (!_noteChannel.isRecording()) {
 			_blinkTimer.start();
 			_noteChannel.startRecording();
 		} else {
			this.modifyButton(false);
 			_blinkTimer.stop();
 			setImageBase("img/redbutton.png");
 			_noteChannel.stopRecording();
 			Clip clip = _noteChannel.getRecording();
 			if (!clip.containsNotes())
 				return; // ignore empty clip
 			_library.addClip(clip);
 			_contentPanel.pushScreen(ScreenType.LIBRARY);
 			try {
 				clip.writeToFile();
 			} catch (IOException e) {
 				System.out.println("ERROR: problem writing clip to file");
 			}
 		}
 	}
 
 	private class BlinkListener implements ActionListener {
 		private boolean on = true;
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (on)
 				setImageBase("img/pinkbutton.png");
 			else
 				setImageBase("img/redbutton.png");
 			repaint();
 			on = !on;
 		}
 	}
 }
