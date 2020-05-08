 package menu;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 
 
 /**
  * The About box and menu item
  */
 public class AboutMenuItem extends JMenuItem implements ActionListener{
 	
 	private static final long serialVersionUID = 4233577023432788830L;
 
 	/**
 	 * Instantiates a new about menu item.
 	 */
 	public AboutMenuItem(){
 		super("About");
 		addActionListener(this);
 	}
 
 	/**
 	 * Shows the about box
 	 */
 	public void actionPerformed(ActionEvent arg0) {
		JOptionPane.showMessageDialog(null, "iidenki developer build\nInteractive language training software suite\nDeveloped by Sebastian Fabian\n\nThis is free and open source software released under GPLv3!\nweb site: iidenki.no-ip.org");
 	}
 }
