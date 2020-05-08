 package gruppe19.client.gui;
 
 import gruppe19.client.gui.CalendarView.AppointmentWidget;
 import gruppe19.client.ktn.ServerAPI;
import gruppe19.client.ktn.ServerAPI.Status;
 import gruppe19.model.Appointment;
 import gruppe19.model.User;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Collection;
 import java.util.List;
 
 import javax.swing.Box;
 import javax.swing.DefaultListModel;
 import javax.swing.DefaultListSelectionModel;
 import javax.swing.JButton;
 import javax.swing.JColorChooser;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 /**
  * A dialog for importing other users' calendars.
  */
 public class CalendarImportDialog extends JDialog implements ActionListener {
 	private GridBagLayout layout;
 	private JButton btnCancel, 
 					btnConfirm, 
 					btnColorChooser, 
 					btnDeleteImportedCalendar, 
 					btnDeleteAllImportedCalendars;
 	private JTextField txtTransparency;
 	private DefaultListModel defaultModel;
 	private DefaultListSelectionModel defaultSelectModel;
 	private GridBagConstraints constraints;
 	private JList listUsers;
 	private Color color = Color.white;
 	private Collection<AppointmentWidget> container;
 
 	/**
 	 * After the dialog closes, the specified container will be filled with
 	 * all the appointment widgets that should be added to the calendar after
 	 * the import. 
 	 * <p>
 	 * If the list of imported calendars is to be cleared, the container will only
 	 * contain <code>null</code> elements after the dialog closes.
 	 * 
 	 *  @throws NullPointerException If container is null.
 	 */
 	public CalendarImportDialog(Collection<AppointmentWidget> container) {
 		if (container == null) throw new NullPointerException();
 		
 		this.container = container;
 		layout = new GridBagLayout();
 		setLayout(layout);
 		constraints = new GridBagConstraints();
 
 		//legg til deltagere
 		constraints.gridx=0;
 		constraints.gridy=0;
 		add(new JLabel("Velg bruker  importere fra"),constraints);
 
 		constraints.gridx=0;
 		constraints.gridy=1;
 		constraints.gridheight = 2;
 		defaultModel = new DefaultListModel();
 		listUsers = new JList();
 		
 
 		listUsers.setModel(defaultModel);
 //		listUsers.setCellRenderer(new UserStatusListRenderer());
 
 		defaultSelectModel = new DefaultListSelectionModel();
 		defaultSelectModel.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
 		
 		listUsers.setSelectionModel(defaultSelectModel);
 		
 		JScrollPane scrollUsers = new JScrollPane(listUsers);
 		scrollUsers.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		scrollUsers.setPreferredSize(new Dimension(210, 100));
 		add(scrollUsers, constraints);
 
 		constraints.gridheight = 1;
 		constraints.anchor = GridBagConstraints.WEST;
 		constraints.gridy=2;
 		constraints.gridx = 1;
 		btnColorChooser = new JButton("Velg farge p importerte avtaler");
 		add(btnColorChooser,constraints);
 		
 		txtTransparency = new JTextField("0", 2);
 		Box horiBox = Box.createHorizontalBox();
 		horiBox.add(new JLabel("Gjennomsiktighet: "));
 		horiBox.add(txtTransparency);
 		horiBox.add(new JLabel("%"));
 		
 		constraints.anchor = GridBagConstraints.SOUTHWEST;
 		constraints.gridy=2;
 		constraints.gridx = 1;
 		
 		add(horiBox, constraints);
 		
 		//knapper for  slette en kalender og for  slette alle kalendre
 		constraints.gridy=3;
 		constraints.gridx=0;
 		btnDeleteImportedCalendar = new JButton("Velg bort");
 //		add(btnDeleteImportedCalendar,constraints);
 		
 		constraints.gridy = 5;
 		constraints.gridx = 0;
 		btnDeleteAllImportedCalendars=new JButton("Fjern alle tidligere importerte kalendere");
 		add(btnDeleteAllImportedCalendars,constraints);
 		
 		//knapper for godta og slett
 		constraints.anchor = GridBagConstraints.EAST;
 		constraints.gridx=0;
 		constraints.gridy = 3;
 		btnConfirm=new JButton("Importer avtaler");
 		add(btnConfirm, constraints);
 		
 		constraints.gridy = 4;
 		add(Box.createVerticalStrut(20), constraints);
 
 		//add actionlisteners
 		btnDeleteImportedCalendar.addActionListener(this);
 		btnConfirm.addActionListener(this);
 		btnDeleteAllImportedCalendars.addActionListener(this);
 		btnColorChooser.addActionListener(this);
 		
 		constraints.gridx = 1;
 		constraints.gridy = 5;
 		constraints.anchor = GridBagConstraints.SOUTHEAST;
 		btnCancel = new JButton("Avbryt");
 		btnCancel.addActionListener(this);
 		add(btnCancel, constraints);
 		
 		List<User> users = ServerAPI.getUsers();
 		
 		for (User u : users) {
 			defaultModel.addElement(u);
 		}
 		
 		//behaviour
 		setModalityType(ModalityType.APPLICATION_MODAL);
 		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		pack();
 		setLocationRelativeTo(null);
 		setVisible(true);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == btnCancel) {
 			container.clear();
 			dispose();
 		}
 		
 		//button add users
 		if (e.getSource() == btnConfirm) {
 			if (listUsers.getSelectedValue() == null) {
 				//No user selected, just close dialog
 				container.clear();
 				dispose();
 				return;
 			}
 			
 			//Calculate alpha
 			int transparencyPercent, alpha;
 			
 			try {
 				transparencyPercent = Integer.parseInt(txtTransparency.getText());
 			}
 			catch (Exception ex) {transparencyPercent = 0;}
 			
 			if (transparencyPercent < 0) transparencyPercent = 0;
 			else if (transparencyPercent > 100) transparencyPercent = 100;
 			
 			alpha = (int)((transparencyPercent / 100f) * 255);
 			alpha = 255 - alpha;
 			
 			//Shift alpha bits into bits 24-31
 			color = new Color(	  (alpha << 24)
 								| (color.getRed() << 16)
 								| (color.getGreen() << 8)
 								|  color.getBlue(), true);
 			
 			for (Appointment a : 
 					ServerAPI.getAppointments((User)listUsers.getSelectedValue())) {
				//We only want appointments that our user has approved
				if (a.getUserList().get(listUsers.getSelectedValue()) != Status.APPROVED) {
					continue;
				}
 				AppointmentWidget appW = new AppointmentWidget(a, true);
 				appW.setBackground(color);
 				container.add(appW);
 			}
 			
 			for (Appointment a : 
 				ServerAPI.getAppointmentsStarted((User)listUsers.getSelectedValue())) {
 				AppointmentWidget appW = new AppointmentWidget(a, true);
 				appW.setBackground(color);
 				container.add(appW);
 			}
 			dispose();
 		}
 
 		if(e.getSource()==btnDeleteAllImportedCalendars){
 			container.clear();
 			container.add(null);
 			dispose();
 		}
 		
 		if(e.getSource()==btnColorChooser){
 			color=JColorChooser.showDialog(this, "Velg farge til importerte avtaler", Color.WHITE);
 			if(color==null)
 				color=Color.WHITE;
 		}
 	}
 }
