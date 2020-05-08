 package no.ntnu.fp.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Set;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpringLayout;
 import javax.swing.border.Border;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import no.ntnu.fp.gui.timepicker.*;
 import no.ntnu.fp.model.CalendarEntry;
 import no.ntnu.fp.model.Location;
 import no.ntnu.fp.model.Meeting;
 import no.ntnu.fp.model.Place;
 import no.ntnu.fp.model.User;
 import no.ntnu.fp.model.Meeting.State;
 import no.ntnu.fp.net.network.client.CommunicationController;
 import no.ntnu.fp.util.TimeLord;
 
 
 public class MeetingFrame extends JFrame implements PropertyChangeListener {
 	
 	private static final long serialVersionUID = 1L;
     private static final int SPACING = 5;
     private static final Dimension LABEL_SIZE = new Dimension(100, 20);
     private static final Dimension TEXT_INPUT_SIZE = new Dimension(260, 20);
     
     public static final String TITLE_LABEL = "M�te";
     public static final String DESCRIPTION_LABEL = "Beskrivelse: ";
     public static final String START_LABEL = "Start:";
     public static final String END_LABEL = "Slutt:";
     public static final String PARTICIPANT_LABEL = "Deltakere:";
     public static final String PLACE_LABEL = "Sted:";
 	
 	public static final String MEETING_ACCEPTED = "Akseptert";
 	public static final String MEETING_REJECTED = "Avvist";
 	public static final String MEETING_PENDING = "Venter på svar";
     
     private JTextField description = new JTextField(20);
     private JTextField startField = new JTextField(10);
     private JTextField endField = new JTextField(10);
     private JEditableList participantList;
     private JButton addParticipantButton = new JButton(new addParticipantAction("Legg til Deltaker"));
     private JTextField locationField = new JTextField(20);
     private PlacePickerPanel placePickerPanel = new PlacePickerPanel();
     private JButton saveButton = new JButton(new SaveAction("Lagre"));
     private JButton cancelButton = new JButton(new CancelAction("Avbryt"));
     private JButton deleteButton = new JButton(new DeleteAction("Slett"));
     
    private TimePickableFieldListener startListener = new TimePickableFieldListener(startField, this);
    private TimePickableFieldListener endListener = new TimePickableFieldListener(endField, this);
     
     private Meeting model;
     private ParticipantListModel listModel;
     
     public MeetingFrame() {    	
     	this(new Meeting());
     }
     
     public MeetingFrame(Meeting model) {
 
         setModel(model);
         placePickerPanel.setModel(model);
     	JPanel panel = new JPanel();
     	
     	panel.setLayout(new BorderLayout());
     	
     	listModel = new ParticipantListModel(model);
     	participantList = new JEditableList(listModel, new ParticipantListCellRenderer());
     	participantList.setPreferredSize(new Dimension(410, 150));
     	
     	JLabel labelMeeting = new JLabel(TITLE_LABEL);
 		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
     	labelMeeting.setBorder(padding);
     	labelMeeting.setFont(StylingDefinition.FRAME_TITLE_FONT);
         panel.add(labelMeeting, BorderLayout.NORTH);
     	
         JPanel center = new JPanel();
         
         center.setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         
         addGridBagLabel(center, DESCRIPTION_LABEL, 0, c);
         description.addKeyListener(new KeyAdapter() {
         	@Override
         	public void keyReleased(KeyEvent e) {
         		getModel().setDescription(description.getText());
         	}
 		});
         addGridBagComponent(center, description, 0, c);
         
         addGridBagLabel(center, START_LABEL, 1, c);
         startField.getDocument().addDocumentListener(new DocumentListener() {
 			
         	public void changedUpdate(DocumentEvent e) {}
 			public void removeUpdate(DocumentEvent e) {}
 			public void insertUpdate(DocumentEvent e) {
 				System.out.println(startField.getText());
 				if (getModel() != null) {
 					getModel().setStartDate(TimeLord.parseDate(startField.getText()));
 				}
 			}
 		});
         addGridBagComponent(center, startField, 1, c);
         
         addGridBagLabel(center, END_LABEL, 2, c);
         endField.getDocument().addDocumentListener(new DocumentListener() {
         	
         	public void changedUpdate(DocumentEvent e) {}
         	public void removeUpdate(DocumentEvent e) {}
 			public void insertUpdate(DocumentEvent e) {
 				if (getModel() != null) {
 					getModel().setEndDate(TimeLord.parseDate(endField.getText()));
 				}
 			}
         });
         addGridBagComponent(center, endField, 2, c);
         
         addGridBagComponent(center, new JLabel(PARTICIPANT_LABEL), 3, 0, c, 2);
         JScrollPane scrollPanel = new JScrollPane(participantList);
         scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         scrollPanel.setPreferredSize(new Dimension(410, 200));
         addGridBagComponent(center, scrollPanel, 4, 0, c, 2);
         addGridBagComponent(center, addParticipantButton, 5, 0, c, 2);
         
         //addGridBagLabel(center, PLACE_LABEL, 6, c);
         //addGridBagComponent(center, locationField, 6, c);
         
         addGridBagComponent(center, placePickerPanel, 6, 0, c, 2);
         placePickerPanel.addPropertyChangeListener(this);
         
         
         panel.add(center, BorderLayout.CENTER);
                 
         JPanel buttons = new JPanel();
         
         buttons.add(saveButton);
         buttons.add(cancelButton);
         buttons.add(deleteButton);
         
         panel.add(buttons, BorderLayout.SOUTH);
 
         model.setOwner(CommunicationController.getInstance().getUser());
         
         panel.setPreferredSize(new Dimension(500, 700));
         setPreferredSize(new Dimension(500, 700));
         
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         setContentPane(panel);
         pack();
         setLocationRelativeTo(null);
         setResizable(false);
         setVisible(true);
        
         startField.addFocusListener(startListener);
         startListener.setDate(model.getStartDate());
         endField.addFocusListener(endListener);
        endListener.setDate(model.getEndDate());        
     }
 	
 	private void addGridBagLabel(JPanel panel, String s, int row, GridBagConstraints c) {
 		c.gridx = 0;
 		c.gridy = row;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.fill = GridBagConstraints.NONE;
 		c.weightx = 0.0;
 		c.anchor = GridBagConstraints.LINE_START;
 		JLabel label = new JLabel(s);
 		label.setFont(StylingDefinition.DIALOG_LABEL_FONT);
 		panel.add(label, c);
 	}
 	
 	private void addGridBagComponent(JPanel panel, Component comp, int row, GridBagConstraints c) {
 		addGridBagComponent(panel, comp, row, 1, c, 1);
 	}
 	
 	private void addGridBagComponent(JPanel panel, Component comp, int row, int col, GridBagConstraints c, int width) {
 		c.gridx = col;
 		c.gridy = row;
 		c.gridheight = 1;
 		c.gridwidth = width;
 		c.fill = GridBagConstraints.NONE;
 		c.weightx = 0.0;
 		c.anchor = GridBagConstraints.LINE_START;
 		panel.add(comp, c);	
 	}
     
     public Meeting getModel() {
     	return model;
     }
     
     //Action for Legg til deltaker
     private class addParticipantAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
 
         public addParticipantAction(String text) {
         	super(text, null); 
         }
 
         @Override
         public void actionPerformed(ActionEvent arg0) {
         	new ListRenderingFrame(model);
         }
     }
     
     //Action for Rom reservering
     private class RoomListAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
 
         public RoomListAction(String text) {
         	super(text, null);
         }
 
         @Override
         public void actionPerformed(ActionEvent arg0) {
         	System.out.println("Reserver rom");
         }
     }
     
     //Action for lagring av skjema
     private class SaveAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
 
         public SaveAction(String text) {
         	super(text, null);
         }
 
         @Override
         public void actionPerformed(ActionEvent arg0) {
         	CommunicationController c = CommunicationController.getInstance();
         	int id = c.saveMeeting(model.shallowCopy());
         	model.setID(id);
         	c.getUser().getCalendar().addMeeting(model);
         }
     }
     
     //Action for � avbryte skjema
     private class CancelAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
 
         public CancelAction(String text) {
         	super(text, null);
         }
 
         @Override
         public void actionPerformed(ActionEvent arg0) {
         	dispose();
         }
     }
     
     private class DeleteAction extends AbstractAction {
     	
     	public DeleteAction(String text) {
     		super(text);
     	}	
     	
     	@Override
     	public void actionPerformed(ActionEvent e) {
     		int id = model.getID();
     		if (id != -1) {
     			CommunicationController c = CommunicationController.getInstance();
     			c.deleteMeeting(model);
     			model.setID(-1);
     		}
     	}
     	
     }
     
     private class RemoveParticipant extends AbstractAction {
     	
     	private User user;
     	
     	public RemoveParticipant(User user) {
     		this.user = user;
     	}
     	
     	@Override
     	public void actionPerformed(ActionEvent e) {
     		model.removeParticipant(user);
     	}
     	
     }
 
     public void setModel(Meeting model)  {
     	if (model != null) {
     		if (this.model != null) {
     			this.model.removePropertyChangeListener(this);
     		}
     		
     		this.model = model;
     		this.model.addPropertyChangeListener(this);
     		updatePanel();
     	}
     }
     
 	public void updatePanel() {
     	description.setText(model.getDescription());
     	startField.setText(TimeLord.formatDate(model.getStartDate()));
     	startListener.setDate(model.getStartDate());
     	endField.setText(TimeLord.formatDate(model.getEndDate()));
     	endListener.setDate(model.getEndDate());
     	//participantList.setListData(model.getParticipants());
     	updateLocation();
     }
     
     private void updateLocation() {
     	Location location = model.getLocation(); 
     	if (location != null) {
     		//placePickerPanel.setLocation(location);
     		locationField.setText(model.getLocation().toString());
     	} else {
     		locationField.setText("");
     	}
     }
     
     
     @Override
 	public void propertyChange(PropertyChangeEvent event) {
 		// TODO Auto-generated method stub
         final String name = event.getPropertyName();
         if (name == CalendarEntry.DESC_PROPERTY) {
         	description.setText(model.getDescription());
         } else if (name == CalendarEntry.START_PROPERTY) {
         	startListener.setDate(model.getStartDate());
         } else if (name == CalendarEntry.END_PROPERTY) {
         	endListener.setDate(model.getEndDate());
         } else if (name == Meeting.PARTICIPANTS_PROPERTY) {
         	
         } else if (name == CalendarEntry.LOC_PROPERTY) {
         	locationField.setText(model.getLocation().toString());
         } else if (name == PlacePickerPanel.LOCATIONC_PROPERTY) {
         	Location l = (Location)event.getNewValue();
         	model.setLocation(l);
         }
 	}
 	
 	class ParticipantListCellRenderer implements ListCellRenderer {
 
 		@Override
 		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
 			JPanel panel = new JPanel();
 			
 			User participant = (User)listModel.getElementAt(index);
 			
 			panel.add(new JLabel(participant.getName()));
 			
 			String status = "";
 		//	if (model != null) {
 				switch(model.getState(participant)) {
 					case Accepted: status = MEETING_ACCEPTED; break;
 					case Rejected: status = MEETING_REJECTED; break;
 					case Pending: status = MEETING_PENDING; break;
 				}
 			//}
 			
 			panel.add(new JLabel(status));
 			JButton button = new JButton("Fjern");
 			button.addActionListener(new RemoveParticipant(participant));
 			panel.add(button);
 			
 			return panel;
 		}
 	}
     
     public static void main(String[] args) {
     	
     	User user = new User("havard");
 		user.setName("Håvard Wormdal Høiby");
 		
 		User p1 = new User("bjorn");
 		p1.setName("Bjørn Åge Tungesvik");
 		
 		User p2 = new User("odd");
 		p2.setName("Odd Magnus Trondrud");
 		
 		User p3 = new User("andy");
 		p3.setName("Andre Philipp");
 		
 		User p4 = new User("eivind");
 		p4.setName("Eivind Kvissel");
 		
 		User p5 = new User("tina");
 		p5.setName("Tina Syversen");
 		
 		Meeting model = new Meeting("møte møte møte møte");
 		model.setDate(new Date(112, 2, 1, 12, 0, 0), new Date(112, 2, 1, 13, 0, 0));
 		model.setLocation(new Place(1, "Kiosken"));
 		model.setOwner(user);
 		model.addParticipant(p1, State.Pending);
 		model.addParticipant(p2, State.Pending);
 		model.addParticipant(p3, State.Pending);
 		model.addParticipant(p4, State.Accepted);
 		model.addParticipant(p5, State.Rejected);
     	
     	
     	
 		new MeetingFrame(model);
     }
 }
