 package client.gui.view;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Label;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JList;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.border.Border;
 
 import chronos.Person;
 import client.gui.GBC;
 import client.gui.MainFrame;
 import client.model.ChronosModel;
 import client.model.EventConfigModel;
 
 public class EventConfigWindow extends ChronosWindow {
 
 	private String[] startTimeArray = { "00:00", "01:00", "02:00", "03:00", "04:00" };
 	private Integer[] durationArray = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
 
 	protected JTextField eventNameField, dateField, roomNumberField;
 	protected JComboBox startTime, duration;
 	protected JTextArea eventDescriptionArea;
 	protected JList<Person> participantList;
 	protected JCheckBox alert;
 	protected JButton addParticipantButton, deleteParticipantButton, bookRoomButton, editButton, deleteButton, applyButton, cancelButton;
 
 	private Dimension bottomButtonDim = new Dimension(80, 20);
 	private Dimension bigButtonDim = new Dimension(170, 20);
 	private EventConfigModel model;
 
 	public EventConfigWindow(ChronosModel model, MainFrame frame) {
 		super(model, frame);
 		setModel(model);
 		setVisible(false);
 
 		setLayout(new GridBagLayout());
 		eventNameField = new JTextField();
 		dateField = new JTextField();
 		startTime = new JComboBox(startTimeArray);
 		duration = new JComboBox(durationArray);
 		eventDescriptionArea = new JTextArea();
 
 		participantList = new JList<Person>();
 		participantList.setEnabled(true);
 
 		alert = new JCheckBox();
 
 		roomNumberField = new JTextField("");
 		roomNumberField.setEditable(false);
 
 		addParticipantButton = new EditConfigButton("Add participant", bigButtonDim);
 		deleteParticipantButton = new EditConfigButton("Delete participant", bigButtonDim);
 		bookRoomButton = new EditConfigButton("Book room", bigButtonDim);
 
 		editButton = new EditConfigButton("Edit", bottomButtonDim);
 		deleteButton = new EditConfigButton("Delete", bottomButtonDim);
 		applyButton = new EditConfigButton("Apply", bottomButtonDim);
 		cancelButton = new EditConfigButton("Cancel", bottomButtonDim);
 
 		eventNameField.setColumns(15);
 		Border border = BorderFactory.createEmptyBorder(0, 3, 0, 3);
 		eventNameField.setBorder(border);
 
 		dateField.setColumns(8);
 		dateField.setBorder(border);
 		roomNumberField.setColumns(5);
 
 		eventDescriptionArea.setPreferredSize(new Dimension(100, 100));
 		eventDescriptionArea.setBorder(border);
 		participantList.setPreferredSize(new Dimension(100, 100));
 
 		startTime.addActionListener(new StartTimeListener());
 		duration.addActionListener(new DurationListener());
 
 		bookRoomButton.addActionListener(new BookRoomAction());
 		addParticipantButton.addActionListener(new AddParticipantAction());
 		deleteParticipantButton.addActionListener(new DeleteParticipantAction());
 		editButton.addActionListener(new EditAction());
 		deleteButton.addActionListener(new DeleteAction());
 		applyButton.addActionListener(new ApplyAction());
 		cancelButton.addActionListener(new CancelAction());
 
 		add(eventNameField, new GBC(0, 0).setAnchor(GridBagConstraints.FIRST_LINE_START).setSpan(2, 1));
 		add(eventDescriptionArea, new GBC(0, 1).setAnchor(GridBagConstraints.FIRST_LINE_START).setSpan(2, 6));
 
 		add(dateField, new GBC(2, 0).setAnchor(GridBagConstraints.FIRST_LINE_START));
 		add(participantList, new GBC(2, 1).setSpan(1, 6));
 
 		add(startTime, new GBC(3, 0).setAnchor(GridBagConstraints.FIRST_LINE_START).setFill(GridBagConstraints.NONE));
 		add(new Label("Enable alert 15 min before"), new GBC(3, 1).setSpan(2, 1).setAnchor(GridBagConstraints.NORTH));
 
 		add(new Label("Alert:"), new GBC(3, 2));
 		add(new Label("Room no."), new GBC(3, 3).setAnchor(GridBagConstraints.FIRST_LINE_START).setFill(GridBagConstraints.NONE));
 
 		add(bookRoomButton, new GBC(3, 4).setSpan(2, 1));
 		add(addParticipantButton, new GBC(3, 5).setSpan(2, 1));
 		add(deleteParticipantButton, new GBC(3, 6).setSpan(2, 1));
 
 		add(editButton, new GBC(0, 7));
 		add(deleteButton, new GBC(1, 7));
 		add(applyButton, new GBC(3, 7));
 		add(cancelButton, new GBC(4, 7).setSpan(2, 1));
 
 		add(duration, new GBC(4, 0));
 		add(alert, new GBC(4, 2));
 		add(roomNumberField, new GBC(4, 3));
 
 		// sets the default model (IE. empty)
 		this.model.setDefaultModel();
 	}
 
 	@Override
 	public void setModel(ChronosModel model) {
 		this.model = (EventConfigModel) model;
 		this.model.setView(this);
 	}
 
 	public void setParticipants(ArrayList<Person> participants) {
 		DefaultListModel<Person> model = new DefaultListModel<Person>();
 		participantList.setModel(model);
 		for (Person person : participants) {
 			model.addElement(person);
 		}
 	}
 
 	public EventConfigModel getModel() {
 		return model;
 	}
 
 	private class StartTimeListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 		}
 	}
 
 	private class DurationListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 		}
 
 	}
 
 	private class BookRoomAction implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			getFrame().getRoomBookingWindow().setVisible(true);
 		}
 	}
 
 	private class AddParticipantAction implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			getFrame().getAddParticipantWindow().setVisible(true);
 		}
 	}
 
 	private class DeleteParticipantAction implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			int selectedParticipant = participantList.getSelectedIndex();
 			if (selectedParticipant != -1) {
 				participantList.remove(selectedParticipant);
 			}
 		}
 	}
 
 	private class EditAction implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 		}
 	}
 
 	private class DeleteAction implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			model.removeEvent();
 			setVisible(false);
 		}
 	}
 
 	private class CancelAction implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			getFrame().getEventConfigWindow().setVisible(false);
 		}
 	}
 
 	private class ApplyAction implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			model.newCalEvent();
			setVisible(false);
 		}
 	}
 
 	/**
 	 * General input info from veiw made available for the model
 	 */
 	public JTextField getEventNameField() {
 		return eventNameField;
 	}
 
 	public JTextField getStartDateField() {
 		return dateField;
 	}
 
 	public String[] getStartTimeArray() {
 		return startTimeArray;
 	}
 
 	public Integer[] getDurationArray() {
 		return durationArray;
 	}
 
 	public JTextField getRoomNumberField() {
 		return roomNumberField;
 	}
 
 	public JTextArea getEventDescriptionArea() {
 		return eventDescriptionArea;
 	}
 
 	public JList getParticipantList() {
 		return participantList;
 	}
 
 	public JCheckBox getAlert() {
 		return alert;
 	}
 
 }
