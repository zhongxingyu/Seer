 package mainGUI;
 
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.JFrame;
 
 public class ReasonerWindowListener implements WindowListener{
 
 	JFrame frame;
 	
 	public ReasonerWindowListener(JFrame frame){
 		this.frame = frame;
 	}
 	
 	@Override
 	public void windowActivated(WindowEvent e) {}
 
 	@Override
 	public void windowClosed(WindowEvent e) {}
 
 	@Override
 	public void windowClosing(WindowEvent e) {
 		if ( PreferenceReasoner.existChanges() ) {
 			//offer to save
 			if(PreferenceReasoner.showSaveChangesDialog())
 				frame.dispose();
 		}
 		
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent e) {}
 
 	@Override
 	public void windowDeiconified(WindowEvent e) {}
 
 	@Override
 	public void windowIconified(WindowEvent e) {}
 
 	@Override
 	public void windowOpened(WindowEvent e) {}
 
 }
