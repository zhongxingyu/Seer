 package gui;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.FocusTraversalPolicy;
 import java.awt.KeyboardFocusManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.KeyStroke;
 import javax.swing.SpinnerDateModel;
 import javax.swing.SpinnerModel;
 import javax.swing.SwingConstants;
 
 import messageTypes.AllMessageTypes;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 import com.toedter.calendar.JDateChooser;
 
 import core.IGUIHandler;
 import core.MessageInfo;
 
 public class MultiChannelGUI {
 
 	/**
 	 * Instanzvariablen
 	 */
 	private JFrame frame;
 	private JPanel schedulerPanel;
 	private JPanel calendarPanel;
 	private JPanel reminderPanel;
 	private IGUIHandler guiHandler;
 	private JComboBox comboBox;
 	private JTextField tFRecipient;
 	private JTextField tFSubject;
 	private JTextPane messageBody;
 	private JCheckBox chckbxScheduler;
 	private String selectedItem;
 	private JPanel reminderTimePanel;
 	private JTextField tFReminderTime;
 	private JCheckBox chckbxReminder;
 	private JSpinner timespinner;
 	private JDateChooser dateChooser;
 	private SpinnerModel model;
 	private JComponent editor;
 	private JTextField tFAttachment;
 	private JLabel lblAttachment;
 	private JButton btnDurchsuchen;
 	private File file;
 	private JLabel filePathLabel;
 	static TabFocusPolicy tabPolicy;
 
 	/**
 	 * Konstruktor
 	 */
 	public MultiChannelGUI(IGUIHandler pGuiHandler) {
 
 		guiHandler = pGuiHandler;
 
 		initialize();
 		
 		frame.setVisible(true);
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 526, 350);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(
 				new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.MIN_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("min:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.MIN_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.MIN_COLSPEC,},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.NARROW_LINE_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("fill:max(46dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("bottom:max(22dlu;default):grow"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("fill:default"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.PREF_ROWSPEC,}));
 
 		// Empfänger
 		JLabel lblRecipient = new JLabel("Empfänger:");
 		tFRecipient = new JTextField();
 		tFRecipient.setColumns(10);
 
 		// Betrefft
 		JLabel lblSubject = new JLabel("Betreff:");
 		tFSubject = new JTextField();
 		tFSubject.setColumns(10);
 
 		// Nachrichtfeld
 		JLabel lblMessage = new JLabel("Nachricht:");
 		messageBody = new JTextPane();
 		patch(messageBody);
 
 		// Combobox zur Auswahl des Typs
 		comboBox = new JComboBox();
 		comboBox.addActionListener(new ComboboxActionLister());
 		comboBox.setModel(new DefaultComboBoxModel(AllMessageTypes.values()));
 		
 		// Scheduler
 		chckbxScheduler = new JCheckBox("Scheduler: ");
 		chckbxScheduler
 				.setToolTipText("Select this if you want to schedule this message.");
 		chckbxScheduler.setHorizontalTextPosition(SwingConstants.LEADING);
 		chckbxScheduler.addActionListener(new SchedulerActionListener());
 
 		schedulerPanel = new JPanel();
 		schedulerPanel.add(chckbxScheduler);
 		schedulerPanel.setVisible(true);
 		
 		// Kalender anzeige
 		calendarPanel = datePicker(null, new Date());
 		calendarPanel.setVisible(false); // Nur Anzeigen wenn Schedule ausgewählt wird
 		
 		// Reminder
 		chckbxReminder = new JCheckBox("Reminder: ");
 		chckbxReminder
 				.setToolTipText("Select this if you want to recieve a reminder before this message is sent.");
 		chckbxReminder.setHorizontalTextPosition(SwingConstants.LEADING);
 		chckbxReminder.addActionListener(new ReminderActionListener());
 
 		reminderPanel = new JPanel();
 		reminderPanel.add(chckbxReminder);
 		reminderPanel.setVisible(false); // Nur Anzeigen wenn Schedule ausgewählt wird
 		
 		// Das Panel zur Reminder Zeit eingabe
 		reminderTimePanel = new JPanel();
 		reminderTimePanel.setLayout(null);
 		reminderTimePanel.setVisible(false);
 		tFReminderTime = new JTextField();
 		tFReminderTime.setBounds(64, 6, 52, 28);
 		reminderTimePanel.add(tFReminderTime);
 		tFReminderTime.setColumns(10);
 		
 		JLabel lblMinuten = new JLabel("Minuten:");
 		lblMinuten.setBounds(6, 12, 61, 16);
 		reminderTimePanel.add(lblMinuten);
 		
 		// Absenden Button
 		JButton btnSend = new JButton("Abschicken");
 		btnSend.addActionListener(new SendActionListener());
 		
 		// Alles dem frame hinzfügen
 		frame.getContentPane().add(lblRecipient, "2, 2");
 		frame.getContentPane().add(tFRecipient, "4, 2, 25, 1, fill, default");
 		frame.getContentPane().add(lblSubject, "2, 4");
 		frame.getContentPane().add(tFSubject, "4, 4, 25, 1, fill, default");
 		frame.getContentPane().add(lblMessage, "2, 6");
 		frame.getContentPane().add(messageBody, "2, 8, 27, 7, fill, fill");
 		frame.getContentPane().add(comboBox, "30, 6, fill, default");
 		frame.getContentPane().add(schedulerPanel, "30, 2, left, fill");
 		frame.getContentPane().add(reminderPanel, "30, 4, left, fill");
 		frame.getContentPane().add(calendarPanel, "30, 10, fill, default");
 		frame.getContentPane().add(reminderTimePanel, "30, 12, fill, fill");
 		frame.getContentPane().add(btnSend, "30, 14, fill, fill");
 		
 		createAttachmentPanel();
 		
 		// Tab Reihenfolge festlegen
 		Vector<Component> order = new Vector<Component>(7);
         order.add(tFRecipient);
         order.add(tFSubject);
         order.add(messageBody);
         order.add(chckbxScheduler);
         order.add(btnSend);
         tabPolicy = new TabFocusPolicy(order);
         frame.setFocusTraversalPolicy(tabPolicy);
 		
 	}
 	
 	public void createAttachmentPanel() {
 		lblAttachment = new JLabel("Attachment:");
 		frame.getContentPane().add(lblAttachment, "2, 16, right, default");
 		
 		tFAttachment = new JTextField();
 		tFAttachment.setEditable(false);
 		frame.getContentPane().add(tFAttachment, "4, 16, 25, 1, fill, default");
 		tFAttachment.setColumns(10);
 		
 		btnDurchsuchen = new JButton("Durchsuchen");
 		btnDurchsuchen.addActionListener(new AttachmentActionListener());
 		frame.getContentPane().add(btnDurchsuchen, "30, 16");
 		//frame.pack();
 	}
 
 	/*
 	 * Date Picker
 	 */
 	private JPanel datePicker(String label, Date value) {
 		JPanel datePanel = new JPanel();
 		dateChooser = new JDateChooser();
 		if (value != null) {
 			dateChooser.setDate(value);
 		}
 		for (Component comp : dateChooser.getComponents()) {
 			if (comp instanceof JTextField) {
 				((JTextField) comp).setColumns(50);
 				((JTextField) comp).setEditable(false);
 			}
 		}
 
 		model = new SpinnerDateModel();
 		timespinner = new JSpinner(model);
 		editor = new JSpinner.DateEditor(timespinner, "HH:mm");
 		timespinner.setEditor(editor);
 		if (value != null) {
 			timespinner.setValue(value);
 		}
 
 		datePanel.add(timespinner);
 		datePanel.add(dateChooser);
 
 		return datePanel;
 	}
 	
 	/**
 	 * Patch the JTextArea so we can leave the TextArea with Tab
 	 * @author bbu
 	 *
 	 */
 	public static void patch(Component c) {
         Set<KeyStroke> 
         strokes = new HashSet<KeyStroke>(Arrays.asList(KeyStroke.getKeyStroke("pressed TAB")));
         c.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, strokes);
         strokes = new HashSet<KeyStroke>(Arrays.asList(KeyStroke.getKeyStroke("shift pressed TAB")));
         c.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, strokes);
     }
 
 	/*
 	 * ActionListeners
 	 */
 	private class SendActionListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			Date scheduleDate = null;
 			Date scheduleHour = null;
 			Date reminderDate = null;
 			
 			//selectedItem = (String) comboBox.getSelectedItem();
 			AllMessageTypes enums = (AllMessageTypes) comboBox.getSelectedItem();
 			selectedItem = enums.toString();
 			// Der Methode muss eine Liste von Recipients übergeben werden
 			List<String> recipients = new ArrayList<String>(); // Nur zum
 																// testen
 			// Split the address input field into single addresses
 			String[] addresses = tFRecipient.getText().split("[,;]+");
 			
 			for (int i = 0; i < addresses.length; i++) {
 				recipients.add(addresses[i]);
 			}
 
 			if (chckbxScheduler.isSelected()) {
 				scheduleHour = (Date) timespinner.getValue();
 				scheduleDate = dateChooser.getDate();
 
 				// Convert Date to String
 				DateFormat df = new SimpleDateFormat("dd MMMM yyyy");
 				String convertedDate = df.format(scheduleDate);
 				df = new SimpleDateFormat("HH");
 				int convertedHour = Integer.parseInt(df.format(scheduleHour));
 				df = new SimpleDateFormat("mm");
 				int convertedMin = Integer.parseInt(df.format(scheduleHour));
 
 				int schedulerTime = convertedMin;
 
 				// Scheduler Zeit zusammenstellen
 				String convertedTime = "" + convertedHour + ":" + schedulerTime;
 
 				df = new SimpleDateFormat("dd MMMM yyyy HH:mm");
 				try {
 					scheduleDate = df
 							.parse(convertedDate + " " + convertedTime);
 				} catch (ParseException e) {
 					// TODO Handle this error
 					e.printStackTrace();
 				}
 
 				// TODO: What happens when time is set invalid? ben?
 				// Reminder Zeit zusammenstellen
				// TODO: Wenn kei Reminder eingegeben wird -> CRASH!!!!
 				if (chckbxReminder.isSelected()) {
 					reminderDate = (Date) timespinner.getValue();
					int reminderTime = convertedMin
							- Integer.parseInt(tFReminderTime.getText());
 					convertedTime = "" + convertedHour + ":" + reminderTime;
 
 					df = new SimpleDateFormat("dd MMMM yyyy HH:mm");
 					try {
 						reminderDate = df.parse(convertedDate + " "
 								+ convertedTime);
 					} catch (ParseException e) {
 						// TODO Write to Log window!
 						e.printStackTrace();
 					}
 				}
 			}
 			// Nachricht Abschicken
 			try {
 				MessageInfo newInfo = new MessageInfo(recipients, tFSubject.getText(),messageBody.getText(), selectedItem, scheduleDate, reminderDate, file);
 				ArrayList<String> errorList = guiHandler.sendMessage(newInfo);
 				if(!errorList.isEmpty()){
 					//TODO: Create Dialog with all error Messages!
 					JOptionPane.showMessageDialog(frame, "Message versenden fehlgeschlagen! Mehr Informationen im Log-Window");
 				}else{
 					tFRecipient.setText("");
 					tFSubject.setText("");
 					messageBody.setText("");
 					tFAttachment.setText("");
 				}
 			} catch (Exception e) {
 				JOptionPane.showMessageDialog(frame, e.getMessage());
 			}
 		}
 	}
 
 	private class SchedulerActionListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			if (chckbxScheduler.isSelected()) {
 				calendarPanel.setVisible(true);
 				reminderPanel.setVisible(true);
 			} else {
 				reminderTimePanel.setVisible(false);
 				calendarPanel.setVisible(false);
 				// Also remove the reminder panel
 				reminderPanel.setVisible(false);
 				// and reset the reminder checkbox
 				chckbxReminder.setSelected(false);
 			}
 		}
 	}
 
 	private class ReminderActionListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			if (chckbxReminder.isSelected()) {
 				reminderTimePanel.setVisible(true);
 			} else {
 				reminderTimePanel.setVisible(false);
 			}
 
 		}
 	}
 	
 	private class ComboboxActionLister implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			AllMessageTypes type = (AllMessageTypes) comboBox.getSelectedItem();
 			
 			JComboBox cb = (JComboBox)e.getSource();
 
 			switch (type){
 				case Email:
 					tFAttachment.setVisible(true);
 					lblAttachment.setVisible(true);
 					btnDurchsuchen.setVisible(true);
 					break;
 				case Mms:
 					tFAttachment.setVisible(true);
 					lblAttachment.setVisible(true);
 					btnDurchsuchen.setVisible(true);
 					break;
 				case Sms:
 					tFAttachment.setVisible(false);
 					lblAttachment.setVisible(false);
 					btnDurchsuchen.setVisible(false);
 					break;
 				case Print:
 					tFAttachment.setVisible(false);
 					lblAttachment.setVisible(false);
 					btnDurchsuchen.setVisible(false);
 					break;
 			}
 		}
 	}
 	
 	private class AttachmentActionListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			// Create Load Dialog
 			JFileChooser loadDialog = new JFileChooser();
 
 			// open load dialog
 			int returnVal = loadDialog.showOpenDialog(frame);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				try {
 					file = loadDialog.getSelectedFile();
 					tFAttachment.setText(file.getCanonicalPath());
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Anonyme innere Klasse zur Tab-Reihenfolge
 	 * @author bbu
 	 *
 	 */
 	public static class TabFocusPolicy extends FocusTraversalPolicy {
 		Vector<Component> order;
 
 		public TabFocusPolicy(Vector<Component> order) {
 			this.order = new Vector<Component>(order.size());
 			this.order.addAll(order);
 		}
 
 		public Component getComponentAfter(Container focusCycleRoot,
 				Component aComponent) {
 			int idx = (order.indexOf(aComponent) + 1) % order.size();
 			return order.get(idx);
 		}
 
 		public Component getComponentBefore(Container focusCycleRoot,
 				Component aComponent) {
 			int idx = order.indexOf(aComponent) - 1;
 			if (idx < 0) {
 				idx = order.size() - 1;
 			}
 			return order.get(idx);
 		}
 
 		public Component getDefaultComponent(Container focusCycleRoot) {
 			return order.get(0);
 		}
 
 		public Component getLastComponent(Container focusCycleRoot) {
 			return order.lastElement();
 		}
 
 		public Component getFirstComponent(Container focusCycleRoot) {
 			return order.get(0);
 		}
 }
 
 }
