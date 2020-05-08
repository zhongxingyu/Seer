 /**
  * 
  */
 package ch.zhaw.mima.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.HeadlessException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Calendar;
 import java.util.Locale;
 
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.border.Border;
 
 import org.freixas.jcalendar.JCalendarCombo;
 
 import ch.zhaw.mima.App;
 import ch.zhaw.mima.addresses.Address;
 import ch.zhaw.mima.message.Message;
 import ch.zhaw.mima.message.reminder.Reminder;
 import ch.zhaw.mima.validator.AddressValidatorException;
 
 /**
  * @author michael
  * 
  */
 public abstract class AbstractMessagingModule<T extends Message<? extends Address>>
 		extends BaseModule {
 
 	/**
 	 * @param app
 	 * @throws HeadlessException
 	 */
 	public AbstractMessagingModule(App app) throws HeadlessException {
 		super(app);
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1833405701153538705L;
 	/**
 	 * content the frame in it self
 	 * 
 	 * @uml.property name="mainFrame"
 	 * @uml.associationEnd
 	 */
 
 	/**
 	 * the full editor is in this panel
 	 * 
 	 * @uml.property name="paEditor"
 	 * @uml.associationEnd
 	 */
 	protected JPanel messagePanel;
 
 	/**
 	 * the title of this message frame
 	 * 
 	 * @uml.property name="frameTitle"
 	 */
 	protected String frameTitle;
 
 	protected JLabel lbRecipient;
 	private JTextArea taRecipient;
 	private JTextArea taText;
 
 	protected JTextField taReminderRecipient;
 
 	private JPanel reminderToggleContainer;
 	private JCheckBox chkRemind;
 	private JCalendarCombo calendar;
 	private JCalendarCombo reminderCalendar;
 
 	private JPanel sendPanel;
 
 
 	public void start() {
 		this.init();
 		createHeader();
 
 		createMessagePanel();
 
 		createSendPanel();
 		showFrame();
 	}
 
 	/**
 	 * Initialize a message frame
 	 * 
 	 * @param framename
 	 *            name of new frame
 	 */
 	protected void init() {
 
 		setTitle(frameTitle);
 
 		// paEditor.setLayout(new FlowLayout());
 	}
 
 	private void createMessagePanel() {
 		messagePanel = new JPanel();
 		messagePanel.setLayout(new BorderLayout());
 		messagePanel.setBorder(BorderFactory.createTitledBorder("Nachricht"));
 
 		add(messagePanel, BorderLayout.CENTER);
 		createMessageInput();
 	}
 
 	/**
 	 * write the header to the gui default is the recipients input as textarea
 	 */
 	protected void createHeader() {
 		// Recipient
 		this.createRecipientInput();
 	}
 
 	/**
 	 * 
 	 */
 
 	private void createReminderInput() {
 
 		JPanel reminderPanel = new JPanel();
 		reminderPanel.setLayout(new BorderLayout());
 		reminderPanel.setBorder(BorderFactory.createTitledBorder("Erinnerung"));
 
 		chkRemind = new JCheckBox("Erinnere mich");
 
 		chkRemind.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				boolean showReminderArea = ((AbstractButton) e.getSource())
 						.isSelected();
 				reminderToggleContainer.setVisible(showReminderArea);
 			}
 		});
 
 		// Toggle area (empfänger details)
 		reminderToggleContainer = new JPanel();
 		reminderToggleContainer.setVisible(false);
 
 		reminderToggleContainer.setLayout(new BorderLayout());
 
 		// empfänger für reminder
 		JLabel lbRecipient = new JLabel("Empfänger (Reminder)");
 		lbRecipient.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		taReminderRecipient = new JTextField();
 		taReminderRecipient.setAlignmentX(Component.LEFT_ALIGNMENT);
 		// make this scrollable
 
 		reminderToggleContainer.add(lbRecipient, BorderLayout.WEST);
 
 		reminderToggleContainer.add(taReminderRecipient, BorderLayout.CENTER);
 		setMaxHeight(reminderToggleContainer, 300);
 		// create date picker
 		JLabel lbTime = new JLabel("Zeit:");
 		lbTime.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		reminderCalendar = createCalendarComponent();
 		reminderToggleContainer.add(reminderCalendar, BorderLayout.SOUTH);
 
 		reminderPanel.add(chkRemind, BorderLayout.NORTH);
 		reminderPanel.add(reminderToggleContainer, BorderLayout.CENTER);
 		sendPanel.add(reminderPanel, BorderLayout.CENTER);
 
 	}
 
 
 
 	protected void showFrame() {
 		// show frame
 		setSize(500, 700);
 		setVisible(true);
 		toFront();
 		// mainFrame.setAlwaysOnTop(true);
 	}
 
 	/**
 	 * write the recipient to the gui
 	 */
 	protected void createRecipientInput() {
 
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout());
 
 		lbRecipient = new JLabel("Empfänger");
 		lbRecipient.setAlignmentX(Component.LEFT_ALIGNMENT);
 		taRecipient = new JTextArea();
 		taRecipient.setAlignmentX(Component.LEFT_ALIGNMENT);
 		// make this scrollable
 		JScrollPane spRecipient = new JScrollPane(taRecipient,
 				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		spRecipient.setAlignmentX(Component.LEFT_ALIGNMENT);
 		panel.add(lbRecipient, BorderLayout.WEST);
 		panel.add(spRecipient, BorderLayout.CENTER);
 
 		add(panel, BorderLayout.NORTH);
 	}
 
 	protected void createMessageInput() {
	
 
 		taText = new JTextArea();
 		taText.setAlignmentX(Component.LEFT_ALIGNMENT);
 		JScrollPane spText = new JScrollPane(taText,
 				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		spText.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		messagePanel.add(spText, BorderLayout.CENTER);
 	}
 
 	protected void createSendPanel() {
 		sendPanel = new JPanel();
 		sendPanel.setLayout(new BorderLayout());
 		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
 		// set message input
		JLabel lbTime = new JLabel("Sendezeit: ");
 		lbTime.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		calendar = createCalendarComponent();
 
 		JButton buSend = new JButton("senden");
 		buSend.setAlignmentX(Component.LEFT_ALIGNMENT);
 		buSend.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				sendMessage();
 			}
 		});
 
		panel.add(lbTime, BorderLayout.WEST);
		panel.add(calendar, BorderLayout.CENTER);
		sendPanel.add(panel, BorderLayout.SOUTH);
		panel.add(buSend, BorderLayout.SOUTH);
 
 		createReminderInput();
 		add(sendPanel, BorderLayout.SOUTH);
 	}
 
 	private JCalendarCombo createCalendarComponent() {
 		JCalendarCombo calendar = new JCalendarCombo(
 				Calendar.getInstance(Locale.GERMAN), Locale.GERMAN,
 				JCalendarCombo.DISPLAY_DATE | JCalendarCombo.DISPLAY_TIME, true);
 		prepareCalendarComponent(calendar);
 		return calendar;
 	}
 
 	private void prepareCalendarComponent(JCalendarCombo calendar) {
 		Border etchedBorder = BorderFactory.createEtchedBorder();
 		Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
 		Border compoundBorder = BorderFactory.createCompoundBorder(
 				etchedBorder, emptyBorder);
 		calendar.setBorder(compoundBorder);
 		// calendar.addDateListener(new MyDateListener());
 		setMaxHeight(calendar, 100);
 	}
 
 	private void setMaxHeight(JComponent component, int height) {
 		Dimension dimension = component.getPreferredSize();
 		dimension.height = height;
 
 		component.setMaximumSize(new Dimension(dimension));
 	}
 
 	/**
 	 * 
 	 */
 
 	protected void sendMessage() {
 		T message = createMessage();
 		message.setText(taText.getText());
 		message.setSendTime(calendar.getDate().getTime());
 
 		try {
 
 			addAddressesToMessage(taRecipient.getText(), message);
 			if (chkRemind.isSelected()) {
 				Reminder<T> messageRemind = createReminderMessage();
 				messageRemind.setOriginalMessage(message);
 
 				addAddressesToMessage(taReminderRecipient.getText(),
 						(T) messageRemind);
 
 
 				putMessageInQueue((T) messageRemind);
 
 			}
 
 			putMessageInQueue(message);
 		} catch (AddressValidatorException e) {
 			showRecipientError(e);
 		}
 
 	}
 
 	private void showRecipientError(AddressValidatorException e) {
 		JOptionPane.showMessageDialog(this,
 				"Please check address:\n" + e.getMessage(), "error",
 				JOptionPane.ERROR_MESSAGE);
 	}
 
 	/**
 	 * @param message
 	 */
 	protected abstract void addAddressesToMessage(String addressString,
 			T message) throws AddressValidatorException;
 
 	/**
 	 * @param message
 	 */
 
 	protected abstract void putMessageInQueue(T message)
 			throws AddressValidatorException;
 
 
 	/**
 	 * 
 	 */
 	protected abstract T createMessage();
 	
 	protected abstract Reminder<T> createReminderMessage();
 
 }
