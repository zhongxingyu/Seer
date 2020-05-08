 package game.gui;
 
import java.awt.BorderLayout;
 import java.awt.Dimension;
 
import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JTextPane;
 import javax.swing.UIManager;
 
 public class InfoPanel extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	public JTextPane textPane;
 	
 	public InfoPanel(Dimension dimension) {
		setLayout(new BorderLayout());
 		setBorder(UIManager.getBorder("TextField.border"));
 		setPreferredSize(dimension);
 		
 		textPane = new JTextPane();
 		textPane.setEditable(false);
 		textPane.setText("Welcome, challenger");
 		textPane.setAlignmentX(CENTER_ALIGNMENT);
 		textPane.setAlignmentY(CENTER_ALIGNMENT);
 		textPane.setBackground(null);
 		textPane.setFocusable(false);
		add(textPane, BorderLayout.CENTER);
 	}
 }
