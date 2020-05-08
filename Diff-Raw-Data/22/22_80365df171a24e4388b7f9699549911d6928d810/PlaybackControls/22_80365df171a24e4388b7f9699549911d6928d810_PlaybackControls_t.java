 package pipes.view.tools;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
import pipes.editing.TuneEditController;
 
 public class PlaybackControls extends JPanel {
 	private static final long serialVersionUID = 1L;
 	
	public PlaybackControls(final TuneEditController controller) {
 		JButton playButton = new JButton("Play");
 		playButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
				controller.getPlayer().setTune(controller.getTune());
				controller.getPlayer().play();
 			}
 		});
 		add(playButton);
 		
 		JButton pauseButton = new JButton("Pause");
 		pauseButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
				controller.getPlayer().pause();
 			}
 		});
 		add(pauseButton);
 		
 		JButton stopButton = new JButton("Stop");
 		stopButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
				controller.getPlayer().stop();
 			}
 		});
 		add(stopButton);
 	}
 }
