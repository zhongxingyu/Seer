 package views.gui;
 
 import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
 
 public class ImportManager extends JFrame {
	public ImportManager() {
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);
		
		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.SOUTH);
	}
 
 }
