 package game.gui;
 
 import java.awt.Dimension;
 
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
 		setBorder(UIManager.getBorder("TextField.border"));
 		setPreferredSize(dimension);
		setLayout(null);
 		
 		textPane = new JTextPane();
 		textPane.setEditable(false);
		textPane.setBounds(21, 11, 359, 86);
 		textPane.setText("Welcome, challenger");
 		textPane.setAlignmentX(CENTER_ALIGNMENT);
 		textPane.setAlignmentY(CENTER_ALIGNMENT);
 		textPane.setBackground(null);
 		textPane.setFocusable(false);
		add(textPane);
 	}
 }
