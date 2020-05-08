 package views;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.SpringLayout;
 import javax.swing.SwingWorker;
 import javax.swing.WindowConstants;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 import lib.OSProperties;
 import lib.RoundedBorder;
 import lib.SpringUtilities;
 import models.Bug_Report_Model;
 import controllers.File_System_Controller;
 
 public class Bug_Report_View implements ActionListener, KeyListener
 {
 	private Bug_Report_Model model = null;
 	private JButton closeButton, sendButton;
 	private JCheckBox checkbox;
 	private JDialog dialog = new JDialog();
 	private JLabel feedbackLabel;
 	private JPanel buttonPanel;
 	private JPanel secondLayerRight;
 	private JPasswordField pw;
 	private JTextArea bugContent;
 	private JTextField un;
 	private OSProperties osp = null;
 
 	public Bug_Report_View(Bug_Report_Model m)
 	{
 		this.model = m;
 		
 		closeButton = new JButton("Close");
 		closeButton.setBorder(new RoundedBorder(5));
 		closeButton.setPreferredSize(new Dimension(75, 30));
 		closeButton.setActionCommand("close_button");
 		closeButton.addActionListener(this);
 		closeButton.addKeyListener(this);
 	}
 	
 	public void show()
 	{
 		osp = new OSProperties();
 		Container container = dialog.getContentPane();
 		container.add(dialogPanel());
 
 		dialog.pack();
 		dialog.setModal(true);
 		dialog.setTitle("Bug Report");
 		dialog.setResizable(false);
 		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		dialog.setLocationRelativeTo(null);
 		dialog.setVisible(true);
 	}
 
 	/**
 	 * Creates the About_View dialog window.
 	 * 
 	 * @return The JPanel containing all About_View window components
 	 */
 	private JPanel dialogPanel()
 	{
 		JButton button;
 		JLabel label;
 		JTextPane textPane;
 		JPanel topLayer = new JPanel(new BorderLayout());
 		JPanel secondLayerLeft = new JPanel(new BorderLayout());
 		secondLayerLeft.setBorder(BorderFactory.createTitledBorder("Google Mail (gmail) credentials:"));
 
 		/*
 		 * WEST Panel
 		 */
 		/*
 		 * Username and Passwords
 		 */
 		JPanel leftPanel = new JPanel(new BorderLayout());
 		JPanel nPanel = new JPanel(new GridLayout(1, 1));
 
 		textPane = new JTextPane();
 		Font font = new Font("Arial", Font.PLAIN, 9);
 		textPane.setFont(font);
 		textPane.setForeground(Color.DARK_GRAY);
 		textPane.setText("NOTE: You must have a valid gmail account to use this feature.");
 		textPane.setEditable(false);
 		textPane.setOpaque(false);
 		StyledDocument doc = textPane.getStyledDocument();
 		SimpleAttributeSet center = new SimpleAttributeSet();
 		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
 		doc.setParagraphAttributes(0, doc.getLength(), center, false);
 		nPanel.add(textPane);
 		leftPanel.add(nPanel, BorderLayout.NORTH);
 
 		String[] labels = {"Email: ", "Password: "};
 		int numPairs = labels.length;
 		JPanel cPanel = new JPanel(new SpringLayout());
 
 		label = new JLabel(labels[0], JLabel.TRAILING);
 		cPanel.add(label);
 		un = new JTextField(15);
 		label.setLabelFor(un);
 		cPanel.add(un);
 
 		label = new JLabel(labels[1]);
 		cPanel.add(label);
 		pw = new JPasswordField(15);
 		label.setLabelFor(pw);
 		cPanel.add(pw);
 
 		// Lay out the panel
 		SpringUtilities.makeCompactGrid(cPanel,
 		                                numPairs, 2, //rows, cols
 		                                0, 0,        //initX, initY
 		                                5, 5);     //xPad, yPad
 
 		leftPanel.add(cPanel, BorderLayout.CENTER);
 		secondLayerLeft.add(leftPanel, BorderLayout.WEST);
 
 		/*
 		 * EAST Panel
 		 */
 		/*
 		 * Bug Description area (allows user to describe problem with program)
 		 */
 		secondLayerRight = new JPanel(new BorderLayout());
 		secondLayerRight.setBorder(BorderFactory.createTitledBorder("Issue(s): (20 more characters required)"));
 		bugContent = new JTextArea();
 		bugContent.addKeyListener(this);
 		bugContent.setColumns(20);
 		
 		JScrollPane scrollPane = new JScrollPane(bugContent);
 
 		secondLayerRight.add(scrollPane, BorderLayout.EAST);
 
 		/*
 		 * MIDDLE-SOUTH Panel
 		 */
 		JPanel secondLayerBottom = new JPanel(new GridLayout(2, 1));
 		JPanel temp = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		checkbox = new JCheckBox("Include system log file in bug report?");
 		temp.add(checkbox);
 		secondLayerBottom.add(temp);
 		feedbackLabel = new JLabel(" ", JLabel.CENTER);
 		secondLayerBottom.add(feedbackLabel);
 
 		/*
 		 * SOUTH Panel
 		 */
 		/*
 		 * Send Button
 		 */
 		buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		sendButton = new JButton("Send");
 		sendButton.setBorder(new RoundedBorder(5));
 		sendButton.setPreferredSize(new Dimension(75, 30));
 		sendButton.setActionCommand("send_button");
 		sendButton.addActionListener(this);
 		buttonPanel.add(sendButton);
 		
 		/*
 		 * Send Button
 		 */
 		button = new JButton("Cancel");
 		button.setBorder(new RoundedBorder(5));
 		button.setPreferredSize(new Dimension(75, 30));
 		button.setActionCommand("cancel_button");
 		button.addActionListener(this);
 		buttonPanel.add(button);
 		
 		JPanel intermediate = new JPanel(new BorderLayout());
 		intermediate.add(secondLayerLeft, BorderLayout.WEST);
 		intermediate.add(secondLayerRight, BorderLayout.EAST);
 		intermediate.add(secondLayerBottom, BorderLayout.SOUTH);
 		
 		topLayer.add(intermediate, BorderLayout.CENTER);
 		topLayer.add(buttonPanel, BorderLayout.SOUTH);
 		
 		return topLayer;
 	}
 
 	/**
 	 * <p>Checks to see how many characters the user has entered into
 	 * the Bug Report content textfield and changes the border title
 	 * accordingly.
 	 * 
 	 * @param e The KeyEvent relating to the textfield instance
 	 */
 	public void checkCharacters(KeyEvent e)
 	{
 		if (bugContent.hasFocus())
 		{
 			if ((e.getKeyCode() != 8) || (e.getKeyCode() != 127))
 			{
 				if ((20 - bugContent.getText().length()) > 1)
 					secondLayerRight.setBorder(
 						BorderFactory.createTitledBorder("Issue(s): (" + (20 - bugContent.getText().length()) + " more characters required)"));
 				else if ((20 - bugContent.getText().length()) == 1)
 					secondLayerRight.setBorder(
 							BorderFactory.createTitledBorder("Issue(s): (1 more character required)"));
 				else
 					secondLayerRight.setBorder(
 							BorderFactory.createTitledBorder("Issue(s):"));
 			}
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		if (e.getActionCommand().equals("send_button"))
 		{
 			if (bugContent.getText().length() > 20)
 			{
 				if (un.getText().length() > 0)
 				{
 					if (new String(pw.getPassword()).length() > 0)
 					{
 						feedbackLabel.setText("Sending bug report... Please wait.");
 						feedbackLabel.setForeground(Color.BLACK);
 						checkbox.setEnabled(false);
 						bugContent.setEditable(false);
 						un.setEditable(false);
 						pw.setEditable(false);
 						sendButton.setEnabled(false);
 
 						sw.execute();
 					} else {
 						feedbackLabel.setText("You must provide a password.");
 						feedbackLabel.setForeground(Color.RED);
 					}
 				} else {
 					feedbackLabel.setText("You must provide a gmail username.");
 					feedbackLabel.setForeground(Color.RED);
 				}
 			} else {
 				feedbackLabel.setText("You must write more than 20 characters in the \"Issue(s)\" text area.");
 				feedbackLabel.setForeground(Color.RED);
 			}
 		}
 		else if (e.getActionCommand().equals("cancel_button"))
 		{
 			sw.cancel(true);
 			dialog.dispose();
 		}
 		else if (e.getActionCommand().equals("close_button"))
 		{
 			dialog.dispose();
 		}
 	}
 
 	SwingWorker<Boolean, Void> sw = new SwingWorker<Boolean, Void>()
 	{
 		public boolean sent = false;
 
 		@Override
 		protected Boolean doInBackground() // This is called when you .execute() the SwingWorker instance
 		{ //Runs on its own thread, thus not "freezing" the interface
 		  //let's assume that doMyLongComputation() returns true if OK, false if not OK.
 		  //(I used Boolean, but doInBackground can return whatever you need, an int, a
 		  //string, whatever)
 			sent = model.sendEmail(un.getText(),
 					new String(pw.getPassword()),
 					"BUG REPORT",
 					bugContent.getText(),
 					checkbox.isSelected() ? new File_System_Controller().getModel().findFile("logs" + osp.getSeparator() + "log.txt") : null);
 			return sent;
 		}
 
 		@Override
 		protected void done() //this is called after doInBackground() has finished
 		{ // Runs on the EDT
 		  // Update your swing components here
 			if (sent)
 			{
 				feedbackLabel.setText("Success!");
 				feedbackLabel.setForeground(new Color(0, 139, 0));
 				buttonPanel.removeAll();
 				buttonPanel.add(closeButton);
 				buttonPanel.revalidate();
 				buttonPanel.repaint();
 			} else {
 				checkbox.setEnabled(true);
 				bugContent.setEditable(true);
 				un.setEditable(true);
 				pw.setEditable(true);
 				sendButton.setEnabled(true);
 				feedbackLabel.setText("Error: bug report could not be sent...");
 				feedbackLabel.setForeground(Color.RED);
 			}
 		}
 	};
 
 	@Override
 	public void keyPressed(KeyEvent e)
 	{
 		checkCharacters(e);
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e)
 	{
 		checkCharacters(e);
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e)
 	{
 		
 	}
 }
