 package pipes.view.tools;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
import pipes.sound.TunePlayer;
 
 public class PlaybackControls extends JPanel {
 	private static final long serialVersionUID = 1L;
 	
	public PlaybackControls(final TunePlayer player) {
 		JButton playButton = new JButton("Play");
 		playButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
				player.play();
 			}
 		});
 		add(playButton);
 		
 		JButton pauseButton = new JButton("Pause");
 		pauseButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
				player.pause();
 			}
 		});
 		add(pauseButton);
 		
 		JButton stopButton = new JButton("Stop");
 		stopButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
				player.stop();
 			}
 		});
 		add(stopButton);
 	}
 }
