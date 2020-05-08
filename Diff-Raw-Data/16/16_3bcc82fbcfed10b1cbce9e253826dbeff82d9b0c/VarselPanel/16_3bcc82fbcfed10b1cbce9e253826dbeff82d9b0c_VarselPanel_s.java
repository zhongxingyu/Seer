 package client.gui.panels;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 
 import javax.swing.*;
 
 import client.ClientMain;
 import client.gui.VerticalLayout;
 import client.model.ActiveUserModel;
 import client.model.NotificationModel;
 
 /**
  * The panel for displaying and handling notifications
  * 
  * @author Peter Ringset
  * @author Susanne Gustavesen
  *
  */
 public class VarselPanel extends JPanel implements PropertyChangeListener {
 	/*
 	 * TODO: Legg til click-listener i lista
 	 */
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4184187489458007088L;
 	public static final String
 		NOTIFICATION_W_MEETING_CLICKED = "notification with meeting clicked",
 		NOTIFICATION_COUNT_CHANGED = "notification count changed";
 
 	private JButton newAppointmentButton;
 	private NotificationList notificationList;
 	private PropertyChangeSupport pcs;
 	
 	@SuppressWarnings("static-access")
 	public VarselPanel(){
 		super(new VerticalLayout(5,SwingConstants.LEFT));		
 
 		pcs = new PropertyChangeSupport(this);
 		
 		// Top panel, the users icon, name and the logout button
 		JPanel topPanel = new JPanel();
 		PersonLabel personLabel = new PersonLabel();
 		personLabel.setPreferredSize(new Dimension(310, 50));
 		topPanel.add(personLabel);
 		
 		// Center content, the notification label and the notification list
 		JPanel centerContent = new JPanel(new VerticalLayout(5, SwingConstants.LEFT));
 		centerContent.setPreferredSize(new Dimension(310, 503));
 		
 		JLabel notifications = new JLabel("Varsler:");
 		centerContent.add(notifications);
 		
 		notificationList = new NotificationList();
 		notificationList.addPropertyChangeListener(this);
		notificationList.setPreferredSize(new Dimension(310, 350));
 		centerContent.add(notificationList);
 	
 		// Bottom panel, the button for creating a new meeting
 		JPanel bottomPanel = new JPanel(new BorderLayout());
 		bottomPanel.setPreferredSize(new Dimension(306,100));
 		newAppointmentButton = new JButton("Opprett en avtale/mte");
 		newAppointmentButton.setOpaque(true);
 		bottomPanel.add(newAppointmentButton);
 		
 		// Add the panels
 		this.add(topPanel);
 		this.add(centerContent);
 		this.add(bottomPanel);
 		
 		ClientMain.client().getActiveUser().addPropertyChangeListener(this);
 	}
 	
 	/**
 	 * Receive notifications, put them in the notifications list
 	 * @param model
 	 */
 	public void initializeList(ArrayList<NotificationModel> models) {
 		notificationList.initializeList(models);
 	}
 	
 	public void receiveNotification(NotificationModel model) {
 		notificationList.addElement(model);
 	}
 	
 	public JButton getNewAppointmentButton() {
 		return newAppointmentButton;
 	}
 	
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		pcs.addPropertyChangeListener(listener);
 	}
 	
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		pcs.removePropertyChangeListener(listener);
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if (evt.getPropertyName() == ActiveUserModel.NOTIFICATIONS_PROPERTY) {
 			receiveNotification((NotificationModel)evt.getNewValue());	
 		}
 		
 		if (evt.getPropertyName() == NotificationList.NOTIFICATION_ARRIVED ||
 				evt.getPropertyName() == NotificationList.NOTIFICATION_READ) {
 			pcs.firePropertyChange(NOTIFICATION_COUNT_CHANGED, null, notificationList.getUnreadCount());
 		}
 		if (evt.getPropertyName() == NotificationList.NOTIFICATION_OLD_READ ||
 				evt.getPropertyName() == NotificationList.NOTIFICATION_READ) {
 			pcs.firePropertyChange(NOTIFICATION_W_MEETING_CLICKED, null, evt.getNewValue());
 		}
 	}
 }
