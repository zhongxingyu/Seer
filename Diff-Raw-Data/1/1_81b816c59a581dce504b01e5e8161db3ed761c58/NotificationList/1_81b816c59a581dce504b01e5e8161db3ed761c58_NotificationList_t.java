 package client.gui.panels;
 
 import java.awt.Dimension;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 
 import client.model.NotificationModel;
 
 /**
  * Display NotificationListElem in a vertical list w/ scroll bar
  * 
  * @author Peter Ringset
  *
  */
 public class NotificationList extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2045270300264712032L;
 	private static final int MAX_SIZE = 10;
 	public static final String
 			NOTIFICATION_READ = "notification read",
 			NOTIFICATION_ARRIVED = "notification arrived",
 			NOTIFICATION_OLD_READ = "notification old read";
 	private JScrollPane scrollPane;
 	private JList list;
 	private DefaultListModel listModel;
 	private ArrayList<NotificationModel> unread, read;
 	private PropertyChangeSupport pcs;
 
 	public NotificationList() {
 		this.setBorder(null);
 		pcs = new PropertyChangeSupport(this);
 		unread = new ArrayList<NotificationModel>();
 		read = new ArrayList<NotificationModel>();
 		list = new JList();
 		list.addMouseListener(new ListClickedListener());
 		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		listModel = new DefaultListModel();
 		list.setModel(listModel);
 		list.setCellRenderer(new NotificationListCellRenderer());
 		scrollPane = new JScrollPane(list);
 		scrollPane.setPreferredSize(new Dimension(310, 485));
 		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		this.add(scrollPane);
 	}
 
 	/**
 	 * Initialize the list
 	 * @param models
 	 * 				An ArrayList containing all the NotificationModels
 	 * 				that the user owns at login time
 	 */
 	public void initializeList(ArrayList<NotificationModel> models) {
 		Collections.sort(models);
 		for (NotificationModel notificationModel : models) {
 			listModel.addElement(notificationModel);
 			if (notificationModel.isRead()) read.add(notificationModel);
 			else unread.add(notificationModel);
 		}
 		pcs.firePropertyChange(NOTIFICATION_ARRIVED, null, new Integer(unread.size()));
 	}
 	
 	/**
 	 * Add a new notification to the list
 	 * This routine will see to that the number of elements in the list
 	 * is always less than MAX_SIZE as long as the number of unread notifications
 	 * is less than MAX_SIZE.
 	 * @param newNotification
 	 * 			the notification to be added. It is presumed that this
 	 * 			is an unread notification and that the notification's time stamp
 	 * 			is newer than that of all existing notifications 
 	 */
 	public void addElement(NotificationModel newNotification) {
		if (read.contains(newNotification) || unread.contains(newNotification)) return;
 		if (unread.size() + read.size() >= MAX_SIZE && read.size() > 0) {
 			for (int i = listModel.size() - 1; i >= 0; i--) {
 				NotificationModel extract;
 				extract = (NotificationModel) listModel.getElementAt(i);
 				if (extract.isRead()) {
 					listModel.removeElementAt(i);
 					read.remove(extract);
 				}
 			}
 		}
 		listModel.add(0, newNotification);
 		unread.add(newNotification);
 		pcs.firePropertyChange(NOTIFICATION_ARRIVED, null, new Integer(unread.size()));
 	}
 	
 	public int getUnreadCount() {
 		return unread.size();
 	}
 	
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		pcs.addPropertyChangeListener(listener);
 	}
 	
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		pcs.removePropertyChangeListener(listener);
 	}
 	
 	class ListClickedListener implements MouseListener {
 		long timestamp;
 		boolean secondClick;
 		@Override
 		public void mouseClicked(MouseEvent arg0) {
 			// Clock the click and calculate delta time since last click
 			long now = System.currentTimeMillis();
 			long delta = now - timestamp;
 			timestamp = now;
 			// Only care about double clicks with delta < 500 ms
 			if (delta < 500 && !secondClick) {
 				// Propagate event to all listeners and reorganize notification arrays
 				NotificationModel clickedNotification = (NotificationModel) list.getSelectedValue();
 				if (unread.contains(clickedNotification)) {
 					unread.remove(clickedNotification);
 					clickedNotification.setRead(true);
 					read.add(clickedNotification);
 					pcs.firePropertyChange(NOTIFICATION_READ, null, clickedNotification);
 					try {
 						clickedNotification.store();
 					} catch (IOException e) {
 						System.err.println("ERROR: Exception happened while trying to store a notification as read");
 						e.printStackTrace();
 					}
 				} else {
 					pcs.firePropertyChange(NOTIFICATION_OLD_READ, null, clickedNotification);
 				}
 				secondClick = true;
 			} else {
 				secondClick = false;
 			}
 		}
 
 		/*
 		 * Unused methods
 		 */
 		@Override
 		public void mouseEntered(MouseEvent arg0) {}
 		@Override
 		public void mouseExited(MouseEvent arg0) {}
 		@Override
 		public void mousePressed(MouseEvent arg0) {}
 		@Override
 		public void mouseReleased(MouseEvent arg0) {}
 	}
 }
