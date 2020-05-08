 package gruppe19.gui;
 
 import gruppe19.client.ktn.ServerAPI;
 import gruppe19.client.ktn.ServerAPI.Status;
 import gruppe19.model.Appointment;
 import gruppe19.model.Room;
 import gruppe19.model.User;
 import gruppe19.server.ktn.Server;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class MyAppointments extends JDialog {
 	private JLabel lblInvitations;
 	private GridBagConstraints c;
 	private MainScreen mainFrame;
 	private static final String []months = {"Januar","Februar","Mars","April","Mai","Juni","Juli","August","September","Oktober","November","Desember"};
 	
 	/**
 	 * Sorts two appointments by status. 
 	 * If statuses are equal, sorts by start date.
 	 */
 	private static class AppSorter implements Comparator<Appointment> {
 		private static final AppSorter singleton = new AppSorter();
 		
 		public static AppSorter getInstance() {
 			return singleton;
 		}
 		
 		private AppSorter() { }
 		
 		@Override
 		public int compare(Appointment a1, Appointment a2) {
 			boolean a1HasRejected = false, 
 					a2HasRejected = false, 
 					a1HasPending = false, 
 					a2HasPending = false;
 			
 			for (Status s : a1.getUserList().values()) {
 				if (s == Status.REJECTED) {
 					a1HasRejected = true;
 					break;
 				}
 				else if (s == Status.PENDING) {
 					a1HasPending = true;
 				}
 			}
 			
 			for (Status s : a2.getUserList().values()) {
 				if (s == Status.REJECTED) {
 					a2HasRejected = true;
 					break;
 				}
 				else if (s == Status.PENDING) {
 					a2HasPending = true;
 				}
 			}
 			
 			if (a1HasRejected && a2HasRejected) {
 				return a1.getDateStart().compareTo(a2.getDateStart());
 			}
 			
 			if (a1HasRejected) {
 				return -1;
 			}
 			
 			if (a2HasRejected) {
 				return 1;
 			}
 			
 			if (a1HasPending && a2HasPending) {
 				return a1.getDateStart().compareTo(a2.getDateStart());
 			}
 			
 			if (a1HasPending) {
 				return -1;
 			}
 			
 			if (a2HasPending) {
 				return 1;
 			}
 			return a1.getDateStart().compareTo(a2.getDateStart());
 		}
 	}
 	
 	class AppoinmentsButton extends JPanel implements ActionListener{
 		private JLabel lblDescription;
 		private JButton btnHyperlink;
 		private Appointment appointment;
 		
 		public AppoinmentsButton(Appointment appointment) {
 			this.appointment = appointment;
 			btnHyperlink = new JButton();
 			btnHyperlink.setText("<html><u>"+appointment.getTitle()+"</u></html>");
 			btnHyperlink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 			btnHyperlink.setBorderPainted(false);
 			btnHyperlink.setForeground(CalendarView.AppointmentWidget.getColor(appointment));
 			btnHyperlink.setContentAreaFilled(false);
 			btnHyperlink.addActionListener(this);
 			add(btnHyperlink);
 			
 			lblDescription=new JLabel();
 			String txt=String.format("%02d. %s, kl. %02d:%02d, %s", 
 					appointment.getDateStart().getDate(),
 					months[appointment.getDateStart().getMonth()],
 					appointment.getDateStart().getHours(),
 					appointment.getDateStart().getMinutes(),
 					appointment.getRoom() != null && appointment.getRoom().getName() != null
 													&& !appointment.getRoom().getName().equals("") ?
 							"rom " +appointment.getRoom().getName() 
 							: appointment.getPlace());
 			lblDescription.setText(txt);
 			add(lblDescription,c);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			final AppointmentDialogGUI appGUI =
 					new AppointmentDialogGUI(appointment, MainScreen.getUser(), false);
 		
 			appGUI.addConfirmButtonListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					if (appGUI.validateModel()) {
 						ServerAPI.updateAppointment(appointment);
 						appGUI.dispose();
 					}
 				}
 			});
 			
 			appGUI.addDeleteButtonListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					if (appointment.getOwner().equals(MainScreen.getUser())) {
 						//Remove appointment for everyone
 						ServerAPI.destroyAppointment(appointment);
 						mainFrame.getCalendar().removeAppointment(appointment.getID());
 					}
 					else {
 						//Remove appointment for you and
 						//change status to rejected
 						appointment.getUserList().put(
 								MainScreen.getUser(), Status.REJECTED);
 						ServerAPI.updateAppointment(appointment);
 					}
 					appGUI.dispose();
 				}
 			});
 			appGUI.setLocationRelativeTo(MyAppointments.this);
 			appGUI.setVisible(true);
 			MyAppointments.this.repaintAppointments();
 		}
 	}
 	
 	private void repaintAppointments() {
 		getContentPane().removeAll();
 		c=new GridBagConstraints();
 		c.gridy=0;
 		c.gridx=0;
 		lblInvitations = new JLabel();
 		lblInvitations.setText("Dine mter:");
 		c.gridx++;
 		c.gridy++;
 		add(lblInvitations,c);
 		
 		int i =1;
 		
 		Appointment[] sortedAppointments = 
 				new Appointment[mainFrame.getCalendar().getAppointments().size()];
 		sortedAppointments = 
 				mainFrame.getCalendar().getAppointments().toArray(sortedAppointments);
 		Arrays.sort(sortedAppointments, AppSorter.getInstance());
 		
 		for (Appointment appointment : sortedAppointments) {
			if (appointment.getUserList().get(MainScreen.getUser()) != Status.APPROVED
					&& !appointment.getOwner().equals(MainScreen.getUser())) {
 				continue;
 			}
 			
 			c.gridx = 0;
 			JLabel index = new JLabel();
 			c.gridy++;
 			index.setText(""+i+".");
 			add(index, c);
 			c.gridx++;
 			add(new AppoinmentsButton(appointment),c);
 			c.gridy++;
 			i++;
 		}
 		
 		if (i - 1 == 0) {
 			lblInvitations.setText("Du har ingen mter i kalenderen din");
 		}
 		pack();
 		setVisible(true);
 	}
 	
 	public MyAppointments(MainScreen mainFrame){
 		this.mainFrame = mainFrame;
 		setLayout(new GridBagLayout());
 		repaintAppointments();
 	}
 }
