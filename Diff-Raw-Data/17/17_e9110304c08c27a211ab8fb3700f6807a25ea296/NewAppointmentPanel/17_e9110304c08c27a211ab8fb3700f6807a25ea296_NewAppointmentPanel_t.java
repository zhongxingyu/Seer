 package client.gui.panels;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 import client.ClientMain;
 import client.IServerResponseListener;
 import client.ServerConnection;
 import client.gui.JDefaultTextArea;
 import client.gui.JDefaultTextField;
 import client.gui.JTimePicker;
 import client.gui.VerticalLayout;
 import client.gui.participantstatus.ParticipantStatusList;
 import client.gui.usersearch.FilteredUserList;
 import client.model.FilteredUserListModel;
 import client.model.InvitationModel;
 import client.model.InvitationStatus;
 import client.model.MeetingModel;
 import client.model.MeetingRoomModel;
 import client.model.UserModel;
 
 import com.toedter.calendar.JDateChooser;
 
 public class NewAppointmentPanel extends JPanel 
 	implements IServerResponseListener, PropertyChangeListener {
 	
 	private final MeetingModel 			model;
 	private final InvitationModel 		invitation;
 	private final JTextField 			tittelText;
 	private final JDateChooser 			dateChooser;
 	private final JTimePicker 			fromTime, 
 										toTime;
 	private final JComboBox 			moteromComboBox;
 	private final JTextField 			moteromText;
 	private final JTextArea 			beskrivelseTextArea;
 	private       FilteredUserList 		filteredUserList;
 	private       JButton 				addEmployeeButton, 
 										removeEmployeeButton;
 	private final ParticipantStatusList participantList;
 	private       JButton 				storeButton,
 										deleteButton;
 	private		  JButton				AcceptButton,
 										DeclineButton,
 										DeleteFromCalendarButton;
 	
 	
 	private       FilteredUserListModel filteredUserListModel;
 	
 	private final boolean isOwner;
 	
 	private int meetingRoomReqID;
 	
 	private MeetingRoomModel selectedRoom;
 	
 	public NewAppointmentPanel(MeetingModel meetingModel) {
 		super(new VerticalLayout(5,SwingConstants.LEFT));
 //		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		
 		model = meetingModel;
 		model.addPropertyChangeListener(this);
 		
 		isOwner = meetingModel.getOwner().equals(ClientMain.getActiveUser());
 		
 		// Find invitation if we're not the owner
 		if(!isOwner) {
 			invitation = model.getInvitation(ClientMain.getActiveUser());
 			invitation.addPropertyChangeListener(this);
 		} else {
 			invitation = null;
 		}
 		
 		//Tittel
 		this.add(new JLabel("Tittel:"));
 		tittelText = new JTextField(model.getName(),26);
 		tittelText.setEditable(isOwner);
 		this.add(tittelText);
 		
 		//Tid
 		this.add(new JLabel("Tid"));
 		JPanel tidPanel = new JPanel();
 		tidPanel.setLayout(new BoxLayout(tidPanel, BoxLayout.X_AXIS));
 		dateChooser = new JDateChooser(model.getTimeFrom().getTime(), "dd. MMMM YYYY");
 		
 		dateChooser.setPreferredSize(new Dimension(130,20));
 		dateChooser.setEnabled(isOwner);
 		tidPanel.add(dateChooser);
 		
 		fromTime = new JTimePicker(model.getTimeFrom());
 		toTime = new JTimePicker(model.getTimeTo());
 		
 		if (!isOwner) fromTime.setEditable(false);
 		if (!isOwner) toTime.setEditable(false);
 		
 		tidPanel.add(dateChooser);
 		tidPanel.add(Box.createHorizontalGlue());
 		tidPanel.add(fromTime);
 		tidPanel.add(new JLabel(" - "));
 		tidPanel.add(toTime);
 		
 		this.add(tidPanel);
 		
 		//Moterom
 		this.add(new JLabel("M�terom"));
 		JPanel moteromPanel = new JPanel();
 		moteromComboBox = new JComboBox();
 		selectedRoom = model.getRoom();
 		moteromComboBox.setSelectedItem(selectedRoom);
 		moteromComboBox.setPreferredSize(new Dimension(
 					100,
 					moteromComboBox.getPreferredSize().height
 				));
 		
 		moteromText = new JDefaultTextField("Skriv m�teplass...", 15);
 		moteromText.setText(model.getLocation());
 		
 		moteromComboBox.setEditable(isOwner);
 		moteromText.setEditable(isOwner);
 		
 		moteromPanel.add(moteromComboBox);
 		moteromPanel.add(moteromText);
 		
 		this.add(moteromPanel);
 		
 		//Beskrivelse
 		this.add(new JLabel("Beskrivelse:"));
 		beskrivelseTextArea = new JDefaultTextArea("Skriv inn beskrivelse...", 4, 26);
 		beskrivelseTextArea.setLineWrap(true);
 		beskrivelseTextArea.setText(model.getDescription());
 //		this.add(beskrivelseTextArea);
 		JScrollPane beskrivelseScroll = new JScrollPane(beskrivelseTextArea);
 		beskrivelseScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		
 		//beskrivelseTextArea.setEditable(isOwner);
 		beskrivelseTextArea.setEnabled(isOwner);
 		
 		this.add(beskrivelseScroll);
 		
 		//Ansatte
 		if (isOwner) {
 			this.add(new JLabel("Ansatte:"));
 			filteredUserListModel = new FilteredUserListModel();
 			filteredUserListModel.addUsersToBlacklist(new UserModel[]{ClientMain.getActiveUser()});
 			filteredUserList = new FilteredUserList(filteredUserListModel);
 			filteredUserList.setPreferredSize(new Dimension(
 						this.getPreferredSize().width,
 						150
 					));
 			this.add(filteredUserList);
 			
 			//Legg til fjern knapper
 			addEmployeeButton = new JButton("Legg til");
 			removeEmployeeButton = new JButton("Fjern");
 			
 			JPanel addRemovePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 			
 			addRemovePanel.add(addEmployeeButton);
 			addRemovePanel.add(Box.createHorizontalStrut(40));
 			addRemovePanel.add(removeEmployeeButton);
 			
 			addRemovePanel.setPreferredSize(new Dimension(
 						this.getPreferredSize().width,
 						addRemovePanel.getPreferredSize().height
 					));
 			
 			this.add(addRemovePanel);
 		}
 		
 		//Deltakere
 		participantList = new ParticipantStatusList(meetingModel);
 		participantList.setPreferredSize(new Dimension(
 					this.getPreferredSize().width,
 					150
 				));
 		this.add(participantList);
 		
 		//Lagre / Slett
 		if (isOwner) {
 			JPanel storeDelPane = new JPanel(new FlowLayout(FlowLayout.CENTER,20,0));
 			storeButton = new JButton("Lagre endringer");
 			deleteButton = new JButton("Slett avtale");
 			storeDelPane.add(storeButton);
 			storeDelPane.add(deleteButton);
 			
 			this.add(storeDelPane);
 		} else {
 			// Accept/Decline
 			
 			JPanel buttonPane = new JPanel(new BorderLayout());
 			AcceptButton = new JButton("Godkjenn");
 			DeclineButton = new JButton("Avslå");
 			DeleteFromCalendarButton = new JButton("Slett møte fra min kalender");
 			
 			if(invitation.getStatus() == InvitationStatus.ACCEPTED) {
 				AcceptButton.setEnabled(false);
 			}
 			if(invitation.getStatus() == InvitationStatus.DECLINED) {
 				DeclineButton.setEnabled(false);
 			}
 			
 			JPanel AcceptDeclinePane = new JPanel(new BorderLayout());
 			AcceptDeclinePane.add(AcceptButton, BorderLayout.WEST);
 			AcceptDeclinePane.add(DeclineButton, BorderLayout.EAST);
 			DeleteFromCalendarButton.setPreferredSize(new Dimension(
 						AcceptButton.getPreferredSize().width + DeclineButton.getPreferredSize().width,
 						DeleteFromCalendarButton.getPreferredSize().height
 					));
 			
 			buttonPane.add(AcceptDeclinePane, BorderLayout.CENTER);
 			buttonPane.add(DeleteFromCalendarButton, BorderLayout.SOUTH);
 			
 			buttonPane.setPreferredSize(new Dimension(
 						(int)this.getPreferredSize().getWidth(),
 						(int)buttonPane.getPreferredSize().getHeight()
 					));
 			
 			this.add(buttonPane);
 		}
 		
 		
 		//Listeners
 		timeChangedListener listener = new timeChangedListener();
 		dateChooser.addPropertyChangeListener(listener);
 		fromTime.addActionListener(listener);
 		toTime.addActionListener(listener);
 		
 		if (isOwner) {
 			storeButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {storeMeeting();}});
 			deleteButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {deleteMeeting();}});
 		
 			addEmployeeButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {addEmployee();}});
 			removeEmployeeButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {removeEmployee();}});
 		} else {
 			AcceptButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {acceptMeetingInvitation();}});
 			DeclineButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {declineMeetingInvitation();}});
 		}
 		
 	}
 	
 	/**
 	 * Prepare for garbage collection
 	 */
 	public void close() {
 		participantList.close();
 	}
 	
 	/**
 	 * Checks if the time entered is valid, returns true and colors the font black if it's valid
 	 * colors the font red if it's invalid.
 	 * @return boolean
 	 */
 	private boolean isTimeValid() {
 		if (toTime.compareTo(fromTime) != 1) {
 			fromTime.setForeground(Color.RED);
 			toTime.setForeground(Color.RED);
 			Toolkit.getDefaultToolkit().beep();
 			return false;
 		} else {
 			fromTime.setForeground(Color.BLACK);
 			toTime.setForeground(Color.BLACK);
 			return true;
 		}
 	}
 	
 	private Calendar getFromTime() {
 		Calendar from = Calendar.getInstance();
 		from.setTime(dateChooser.getJCalendar().getDate());
 		from.set(Calendar.HOUR_OF_DAY, fromTime.getHour());
 		from.set(Calendar.MINUTE, fromTime.getMinute());
 		from.set(Calendar.SECOND, 0);
 		return from;
 	}
 	
 	private Calendar getToTime() {
 		Calendar to = Calendar.getInstance();
 		to.setTime(dateChooser.getJCalendar().getDate());
 		to.set(Calendar.HOUR_OF_DAY, toTime.getHour());
 		to.set(Calendar.MINUTE, toTime.getMinute());
 		to.set(Calendar.SECOND, 0);
 		return to;
 	}
 	
 	private void requestMeetingRooms() {
 		if (!isTimeValid()) return;
 		
 		Calendar 	from = getFromTime(), 
 					to = getToTime();
 		
 		meetingRoomReqID = ServerConnection.instance().requestAvailableRooms(this, from, to);
 	}
 	
 	private boolean isDataValid() {
 		//Name
 		if (tittelText.getText().length() == 0) {
 			Toolkit.getDefaultToolkit().beep();
 			System.out.println("Invalid title");
 			return false;
 		}
 		//Time
 		if (!isTimeValid()) {
 			Toolkit.getDefaultToolkit().beep();
 			System.out.println("Invalid time");
 			return false;
 		}
 		
 		//Moteplass
 		if (moteromComboBox.getSelectedItem() != null && moteromText.getText() != "") {
 			Toolkit.getDefaultToolkit().beep();
 			System.out.println("Only use a single meeting room field");
 			return false;
 		}
 			
 		return true;
 	}
 	
 	private void storeMeeting() {
 		if (!isDataValid()) return;
 
 		//Name
 		model.setName(tittelText.getText());
 		//Date+time
 		model.setTimeFrom(this.getFromTime());
 		model.setTimeTo(this.getToTime());
 		//M�teplass
 		model.setRoom((MeetingRoomModel)moteromComboBox.getSelectedItem());
 		model.setLocation(moteromText.getText());
 		//Beskrivelse
 		model.setDescription(beskrivelseTextArea.getText());
 		
 		//Invitasjoner
 		model.commitInvitations();
 		
 		System.out.println("Store!");
 		try {
 			model.store();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	private void deleteMeeting() {
 		if(model.getId() != -1) {
 			try {
 				model.delete();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 		}
 		//throw new UnsupportedOperationException("Delete m�te er ikke laget enda"); //TODO Hva skal denne gj�re dersom m�tet enda ikke er lagret?
 	}
 	
 	/**
 	 * Accept meeting invitation
 	 */
 	private void acceptMeetingInvitation() {
		if(invitation != null) {
			invitation.setStatus(InvitationStatus.ACCEPTED);
 			try {
				invitation.store();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private void declineMeetingInvitation() {
		if(invitation != null) {
			invitation.setStatus(InvitationStatus.DECLINED);
 			try {
				invitation.store();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private void addEmployee() {
 		UserModel[] selUsers = filteredUserList.getSelectedUsers();
 		filteredUserListModel.addUsersToBlacklist(selUsers);
 		
 		for (UserModel user : selUsers)
 			model.addAttendee(user);
 	}
 	
 	private void removeEmployee() {
 		// filteredUserListModel.removeUsersFromBlacklist()
 		throw new UnsupportedOperationException("");
 	}
 	
 	class timeChangedListener implements ActionListener, PropertyChangeListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			requestMeetingRooms();
 		}
 
 		@Override
 		public void propertyChange(PropertyChangeEvent e) {
 			requestMeetingRooms();
 		}
 	}
 
 	@Override
 	public void onServerResponse(int requestId, Object data) {
 		if (requestId == meetingRoomReqID) {
 			List<MeetingRoomModel> rooms = (List<MeetingRoomModel>) data;
 			
 			selectedRoom = (MeetingRoomModel) moteromComboBox.getSelectedItem();
 			
 			moteromComboBox.removeAllItems();
 			
 			moteromComboBox.addItem(null); //Et blankt valg om man �nsker � heller sette lokasjon som tekst
 			for (MeetingRoomModel room : rooms)
 				moteromComboBox.addItem(room);
 			
 			if (selectedRoom == null) return;
 			
 			for (int i = 1; i < moteromComboBox.getItemCount(); i++) {
 				MeetingRoomModel roomi = (MeetingRoomModel)moteromComboBox.getItemAt(i);
 				
 				if (roomi.getRoomNumber() == selectedRoom.getRoomNumber()) {
 					moteromComboBox.setSelectedIndex(i);
 					return;
 				}
 			}
 				
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent e) {
 		if(e.getPropertyName() == InvitationModel.STATUS_CHANGED) {
 			if(invitation.getStatus() == InvitationStatus.ACCEPTED) {
 				AcceptButton.setEnabled(false);
 				DeclineButton.setEnabled(true);
 			}
 			if(invitation.getStatus() == InvitationStatus.DECLINED) {
 				AcceptButton.setEnabled(true);
 				DeclineButton.setEnabled(false);
 			}
 		}
 		
 	}
 
 }
