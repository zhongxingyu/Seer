 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JRadioButton;
 import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;
 
 
 public class MyMenuBarListener implements ActionListener {
 	
 	Playfield playfield;
 	
 	public MyMenuBarListener(Playfield _playfield) {
 		this.playfield = _playfield;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		
 		System.out.println("JMenuItem: " + e.getActionCommand());
 		
 		String cmd = e.getActionCommand();
 		
		if(e.getSource() instanceof JRadioButton && cmd == "Status"){ //TODO: WHY i get an NPE if another JRadioButton from L&F get casted.
 			JRadioButton source = (JRadioButton) e.getSource();
 			JPopupMenu popupMenu = (JPopupMenu) source.getParent();
			JMenu menu = (JMenu) popupMenu.getInvoker();
			JMenuBar bar = (JMenuBar) menu.getParent();
 			MainWindow main = (MainWindow) bar.getTopLevelAncestor(); 
 			
 				if(cmd == "Status"){
 				System.out.println("STATUS program");
 				main.statusWindow.changeVisibility();
 				}
 		}else{
 			System.out.println("Unknown Object. Dont get cast for Toplevel");
 		}
 		if(cmd == "Exit"){
 			System.out.println("EXIT program");
 			System.exit(0);
 		}
 		if(cmd == "Info"){
 			System.out.println("INFO pressed");
 		}
 		
 		//Signal - Third menu element
 		if(cmd == "JOption"){
 			System.out.println("JOption pressed");
 			this.playfield.showJOption();
 		}
 		if(cmd == "JDialog"){
 			System.out.println("JDialog pressed");
 			this.playfield.showJDialog();
 		}
 		
 		if(cmd == "JFileChooser"){
 			System.out.println("JFileChooser pressed");
 			this.playfield.showJFileChooser();
 		}
 		
 		//Look and Feels
 		if(cmd == "Metal"){
 			this.playfield.setLookAndFeel(this.playfield.lookAndFeelInfo[0].getClassName());
 		}
 		if(cmd == "Nimbus"){
 			this.playfield.setLookAndFeel(this.playfield.lookAndFeelInfo[1].getClassName());
 		}
 		if(cmd == "Motif"){
 			this.playfield.setLookAndFeel(this.playfield.lookAndFeelInfo[2].getClassName());
 		}
 		if(cmd == "Gtk"){
 			this.playfield.setLookAndFeel(this.playfield.lookAndFeelInfo[3].getClassName());
 		}
 	}
 
 }
