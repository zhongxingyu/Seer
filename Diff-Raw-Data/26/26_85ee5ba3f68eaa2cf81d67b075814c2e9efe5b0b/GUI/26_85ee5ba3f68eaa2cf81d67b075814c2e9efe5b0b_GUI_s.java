 package GUI;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import main.User;
 
 // TODO make other buttons to this if needed.
 
 public class GUI extends JPanel {
 
 	protected Font buttonFont = new Font("SansSerif", Font.BOLD, 28);
 	protected Font titleFont = new Font("SansSerif", Font.BOLD, 38);
 	protected Font textFont = new Font("SansSerif", Font.BOLD, 28);
 	protected Font resultsFont = new Font("SansSerif", Font.BOLD, 24);
 	protected Font borderFont = new Font("SansSerif", Font.BOLD, 20);
 	protected Font smallFont = new Font("SansSerif", Font.BOLD, 16);
 	protected Font titleInPanelFont = new Font("SansSerif", Font.BOLD, 30);
	protected static final JPanel cards = new JPanel(new CardLayout());
 
 	protected ActionListener buttonListener;
 
 	protected User currentUser = Driver.getAccessTracker().getCurrentUser();
 	protected GridBagConstraints c = new GridBagConstraints();
 
 	protected Color darkBlue = new Color(33, 49, 77);
 	protected Color orange = new Color(210, 73, 18);
 	private Color coolGray = new Color(178, 180, 179);
 
 	protected void showMessage(String message) {
 		JOptionPane.showMessageDialog(this, message);
 	}
 
 	protected boolean confirm(String message) {
 		if ( JOptionPane.showConfirmDialog(this, message) == JOptionPane.YES_OPTION ) {
 			return true;
 		}
 		return false;
 	}
 
 	public static ImageIcon createImageIcon(String path) {
 
 		java.net.URL imageURL = MainGUI.class.getResource(path);
 
 		if (imageURL == null) {
 			System.err.println("Resource not found: " + path);
 			return null;
 		} else {
 			return new ImageIcon(imageURL);
 		}
 	}
 
 	public static class LogOutListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			Driver.getAccessTracker().processLogOut(Driver.getAccessTracker().getCurrentUser().getCWID());
 			Driver.getMainGui().restart();
 		}
 	}
 
 	public static class DoneListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			Driver.getMainGui().restart();
 		}
 	}
 	
 	public static JPanel getCards() {
 		return cards;
 	}
 
 }
