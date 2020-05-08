 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Toolkit;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextPane;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 /**
  * A simple about dialog showing information about the program
  * @author nils
  *
  */
 public class AboutDialog extends JDialog {
 	private static final long serialVersionUID = 3500094512119662325L;
 	private JLabel jLabelIcon =  null;
 	private JPanel jPanelDetails = null;
 	private JLabel jLabelAppName = null;
 	private JPanel jPanelAppName = null;
 	private JLabel jLabelVersion = null;
 	private JPanel jPanelVersion = null;
 	private JTextPane jTextPaneText = null;
 	private JPanel jPanelText = null;
 
 
 	/**
 	 * Constructs a new about dialog
 	 */
 	public AboutDialog() {
 		super();
 		
 		this.setTitle("About " + AppData.APP_NAME);
 		
 		this.setLayout(new BorderLayout());
 		
		this.jLabelIcon = new JLabel("", new ImageIcon(this.getClass().getResource("images/logo.png")), JLabel.CENTER);
 		this.add(jLabelIcon, BorderLayout.CENTER);
 		
 		this.jPanelDetails = new JPanel();
 		this.add(this.jPanelDetails, BorderLayout.SOUTH);
 		
 		this.jPanelDetails.setLayout(new BoxLayout(this.jPanelDetails, BoxLayout.PAGE_AXIS));
 		this.jPanelDetails.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		this.jPanelDetails.add(Box.createHorizontalGlue());
 		
 		this.jLabelAppName = new JLabel(AppData.APP_NAME, JLabel.CENTER);
 		this.jLabelAppName.setFont(this.jLabelAppName.getFont().deriveFont(this.jLabelAppName.getFont().getStyle() | Font.BOLD, this.jLabelAppName.getFont().getSize() + 2));
 		this.jPanelAppName = new JPanel(new BorderLayout());
 		this.jPanelAppName.add(this.jLabelAppName, BorderLayout.CENTER);
 		this.jPanelDetails.add(jPanelAppName);
 		this.jPanelDetails.add(Box.createRigidArea(new Dimension(0, 5)));
 
 		
 		this.jLabelVersion = new JLabel(AppData.APP_VERSION, JLabel.CENTER);
 		this.jPanelVersion = new JPanel(new BorderLayout());
 		this.jPanelVersion.add(this.jLabelVersion, BorderLayout.CENTER);
 		this.jPanelDetails.add(jPanelVersion);
 		this.jPanelDetails.add(Box.createRigidArea(new Dimension(0, 25)));
 
 		
 		this.jTextPaneText = new JTextPane();
 		this.jTextPaneText.setText(AppData.APP_AUTHORS);
 		StyledDocument doc = this.jTextPaneText.getStyledDocument();
 		SimpleAttributeSet center = new SimpleAttributeSet();
 		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
 		doc.setParagraphAttributes(0, doc.getLength(), center, false);
 		this.jTextPaneText.setEditable(false);
 		this.jTextPaneText.setOpaque(false);
 		this.jPanelText = new JPanel(new BorderLayout());
 		this.jPanelText.add(this.jTextPaneText, BorderLayout.CENTER);
 		this.jPanelDetails.add(jPanelText);
 		this.jPanelDetails.add(Box.createRigidArea(new Dimension(0, 20)));
 	
 		
 		this.setSize(new Dimension(400,500));
 		this.setResizable(false);
 		this.centerDialogOnTheScreen();
 	}
 	
 	/**
 	 * Center the dialog window on the screen
 	 */
 	private void centerDialogOnTheScreen() {
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		Dimension dialogSize = this.getSize();
 		int centerPosX = (screenSize.width - dialogSize.width) / 2;
 		int centerPosY = (screenSize.height - dialogSize.height) / 2;
 		setLocation(centerPosX, centerPosY);
 	}
 }
