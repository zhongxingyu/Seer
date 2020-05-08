 package components;
 
 import java.awt.Dimension;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import service.Common;
 import service.Utils;
 import service.Errorist;
 
 public class PlayerPanel extends JPanel implements ActionObserver {
 	private static final long serialVersionUID = -9051505855046492589L;
 	
 	Label time;
 	JSlider track, volume;
 	ToggleButton pause, play;
 	String def_time = "00:00";
 	boolean lock_track_update = false;
 	
 	public void GUI() {
 		setVisible(false);
     	setBackground(Common.color_background);
 		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 		track.setUI(new SliderUI(track));
 		track.setValue(track.getMinimum());
 		
 		track.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				if (track.getValueIsAdjusting())
 					lock_track_update = true;
 				else if (lock_track_update) {
 					Common.player.seek(track.getValue());
 					lock_track_update = false;
 				}
 			}
 		});
 //		track.setEnabled(false);
 		
 		volume.setUI(new SliderUI(volume));
 		volume.setPreferredSize(new Dimension(60, 20));
 		volume.setMaximumSize(new Dimension(120, 20));
 		
 		volume.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				if (volume.getValueIsAdjusting()) 
 					Common.player.setVolume(track.getValue());
 			}
 		});		
 	}
 	
     public PlayerPanel() {
 		new DropTarget(this, new DropTargetListener() {
 			public void dropActionChanged(DropTargetDragEvent dtde) {}
 			public void drop(DropTargetDropEvent evt) {
 			      int action = evt.getDropAction();
 			      evt.acceptDrop(action);
 			      try {
 			          Transferable data = evt.getTransferable();
 			          if (data.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
 			              List<File> files = (List<File>) data.getTransferData(DataFlavor.javaFileListFlavor);
 			              for(File file : files) {
 			            	  if (!evt.isLocalTransfer())
 			            	  {
 			            		  //parse droped and add to active list
 			            	  }
 			              }
 			          }
 			          evt.dropComplete(true);
 			      }
 			      catch (Exception e) { evt.dropComplete(false); Errorist.printLog(e); }
 			}
 			public void dragOver(DropTargetDragEvent dtde) {}
 			public void dragExit(DropTargetEvent dte) {}
 			public void dragEnter(DropTargetDragEvent dtde) {}
 		});
 		
 		play = new ToggleButton(Utils.GetIcon("player/play.png"),
 				Utils.GetIcon("player/stop.png"),
 				this, ActionObserver.STOP, ActionObserver.PLAY);
 		this.add(play);
 		
 		pause = new ToggleButton(Utils.GetIcon("player/pause.png"),
 				Utils.GetIcon("player/play.png"),
 				this, ActionObserver.PLAY, ActionObserver.PAUSE);
 		pause.setVisible(false);
 		this.add(pause);
 		
		RoundButton curr = new RoundButton(Utils.GetIcon("player/shuffle.png"));
		curr.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent arg0) { Common.tabber.GetCurrentTab().Shuffle(); } });
		this.add(curr);
		
 		this.add((time = new Label(def_time, 10, 0)));
 		this.add(new Label("", 20, 4));
 		this.add((track = new JSlider(0, 1000)));
 		
 		this.add(new Label("Volume", 20, 4));
 		this.add((volume = new JSlider(0, 100)));
 		GUI();
 		Common.player.setPanel(this);
     }
     
     public void reset() {
 		setTrackPosition(0);
 		setDefaultTime();
 		pause.setVisible(false);
     }
     
     public void setTrackMax(long length) { track.setMaximum((int) length); }
     public void setTrackPosition(int curr_pos) {
     	if (!lock_track_update)
     		track.setValue(curr_pos); 
     }
     public void blockTrack(boolean block) { track.setEnabled(block); }
     
     public void setVolumeRange(int min, int max) { volume.setMinimum(min); volume.setMaximum(max); }
     public void setVolumePosition(int curr_pos) { volume.setValue(curr_pos); }
     public void blockVolume(boolean block) { volume.setEnabled(block); }
     
     public void setTime(String current_time) { time.setText(current_time); }
     public void setDefaultTime() { time.setText(def_time); }
     
     public void playProc() {
     	if (play.IsDefaultState())
     		play.Toggle();
 		if (Common.player.isPaused())
 			play.setVisible(true);
 		
 		pause.setVisible(true);
     }
     public void pauseProc() { play.setVisible(true); }    
     
     
 	@Override
 	public void notify(int state) {
 		switch(state) {
 			case ActionObserver.PLAY:
 				if (Common.player.isPaused()) 
 					Common.player.resume();
 				else
 					Common.tabber.PlaySelectedOrFirst();
 				break;
 			case ActionObserver.STOP:
 				Common.player.stop();
 				reset();
 				break;
 			case ActionObserver.PAUSE:
 				Common.player.pause();
 				break;			
 		}
 	}    
 }
