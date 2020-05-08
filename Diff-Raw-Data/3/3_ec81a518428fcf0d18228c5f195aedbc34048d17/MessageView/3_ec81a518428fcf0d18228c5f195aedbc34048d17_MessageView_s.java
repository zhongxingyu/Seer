 package views;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.text.SimpleDateFormat;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import main.Global;
 import main.PMViewer;
 import models.Language;
 import models.Message;
 
 /**
  * @author Robert Heim
  * 
  */
 public class MessageView {
 	private JPanel panel;
 	private JLabel labelSender;
 	private JLabel labelDate;
 	private JLabel labelMessage;
 	private JTextField textSender;
 	private JTextField textDate;
 	private JTextArea textMessage;
 	private Message message;
 
 	/**
 	 * Creates a new view for a message.
 	 * 
 	 * @param message
 	 *            the message to generate the view for
 	 */
 	public MessageView(Message message) {
 		this.message = message;
 
 		Language lang = PMViewer.getInstance().getLanguage();
 
 		labelSender = new JLabel(lang.getTranslation("BASIC", "SENDER")+": ");
 		labelDate = new JLabel(lang.getTranslation("BASIC", "DATE")+": ");
 		labelMessage = new JLabel(lang.getTranslation("BASIC", "MESSAGE")+": ");
 		textSender = new JTextField();
 		textSender.setEditable(false);
 		textDate = new JTextField();
 		textDate.setEditable(false);
 		textMessage = new JTextArea();
 		textMessage.setLineWrap(true);
 		textMessage.setEditable(false);		
 
 		update();
 
 		panel = new JPanel(new GridBagLayout());
 		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
 				.createTitledBorder(lang.getTranslation("BASIC", "MESSAGE")),
 				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
 
 		GridBagConstraints c1 = new GridBagConstraints();
 		c1.gridwidth = GridBagConstraints.REMAINDER; // end row
 		//c1.weightx = 0.8;
 		c1.fill = GridBagConstraints.HORIZONTAL;
 		
 		GridBagConstraints c2 = new GridBagConstraints();
 		c2.anchor = GridBagConstraints.FIRST_LINE_START;
 		//c2.fill = GridBagConstraints.HORIZONTAL;
 		
 		panel.add(labelSender, c2);
 		panel.add(textSender, c1);
 		panel.add(labelDate, c2);
 		panel.add(textDate, c1);
 		panel.add(labelMessage, c2);
 		panel.add(textMessage, c1);
 
 	}
 
 	/**
 	 * Updates the textFields/Area.
 	 */
 	public void update() {
 		textSender.setText(message.getSender());
 		SimpleDateFormat sdf = new SimpleDateFormat(Global.PMVIEWER_DATE_FORMAT);
 		textDate.setText(sdf.format(message.getDate()));
 		textMessage.setText(message.getMessage());
 	}
 
 	/**
 	 * @return the panel
 	 */
 	public JPanel getPanel() {
 		return panel;
 	}
 }
