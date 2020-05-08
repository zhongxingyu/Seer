 package devhood.im.sim.ui;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.swing.BoxLayout;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 
 import org.springframework.context.ApplicationContext;
 
 import devhood.im.sim.config.SimConfiguration;
 import devhood.im.sim.controller.SimControl;
 import devhood.im.sim.event.EventDispatcher;
 import devhood.im.sim.event.Events;
 import devhood.im.sim.model.User;
 import devhood.im.sim.model.UserStatus;
 import devhood.im.sim.service.interfaces.MessageSender;
 import devhood.im.sim.service.interfaces.UserChangeListener;
 import devhood.im.sim.service.interfaces.UserService;
 import devhood.im.sim.ui.util.UiUtil;
 
 /**
  * Panel zur Auswahl der User. Aktualisiert sich selbst via {@link UserService}.
  * 
  * @author flo
  * 
  */
 @Named("userPanel")
 public class UserPanel extends JPanel {
 
 	/**
 	 * RegistryService zum Zugriff auf Stammdaten, zb User.
 	 */
 	@Inject
 	private UserService userService;
 
 	@Inject
 	private SimConfiguration simConfiguration;
 
 	@Inject
 	private SimControl simControl;
 
 	@Inject
 	private ApplicationContext applicationContext;
 
 	/**
 	 * Initialisiert, füllt das UserPanel.
 	 */
 	public void init() {
 		BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
 		setLayout(layout);
 
		for (JLabel userLabel : getCurrentUsers()) {
			add(userLabel);
		}

 		setUserChangeListener();
 		startRefreshUsers();
 	}
 
 	/**
	 * Gibt die Labels der aktuellen User aus der DB zurueck.
 	 */
	public List<JLabel> getCurrentUsers() {
 		List<User> users = userService.getUsers();
 		List<String> userFromDb = new ArrayList<String>();
		List<JLabel> currentUsers = new ArrayList<JLabel>();
 
 		for (User user : users) {
 			userFromDb.add(user.getName());
 			final JLabel userLabel = createUserLabel(user);
			currentUsers.add(userLabel);
 		}

		return currentUsers;
 	}
 
 	/**
 	 * Erzeugt das Label zur Anzeige des Benutzers im UserPanel.
 	 * 
 	 * @param user
 	 *            User.
 	 * @return Label
 	 */
 	public JLabel createUserLabel(final User user) {
 		final JLabel userLabel = new JLabel(user.getName());
 		final JPopupMenu popupMenu = new JPopupMenu();
 		final JMenuItem sendFileItem = new JMenuItem("Send File");
 		sendFileItem.addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				if (e.getSource() == sendFileItem) {
 					JFileChooser fc = new JFileChooser();
 					int returnVal = fc.showOpenDialog(userLabel);
 
 					if (returnVal == JFileChooser.APPROVE_OPTION) {
 						File file = fc.getSelectedFile();
 						if (file.length() == 0) {
 							JOptionPane.showMessageDialog(null,
 									"Die Datei hat eine Größe von 0!");
 							return;
 						}
 
 						SendFileFrame sendFileFrame = new SendFileFrame();
 						sendFileFrame.init();
 						// sendFileFrame.
 
 						sendFileFrame.setMessageSender(applicationContext
 								.getBean(MessageSender.class));
 						sendFileFrame.setFileToSend(file);
 						sendFileFrame.setToUser(user.getName());
 						sendFileFrame.showFrame();
 					} else {
 
 					}
 
 				}
 			}
 		});
 		popupMenu.add(sendFileItem);
 
 		userLabel.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 
 				EventDispatcher.fireEvent(Events.USER_SELECTED,
 						userLabel.getText());
 
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				if (e.isPopupTrigger()) {
 					popupMenu.show(e.getComponent(), e.getX(), e.getY());
 				}
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				JLabel l = (JLabel) e.getSource();
 				l.setForeground(Color.RED);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 				JLabel l = (JLabel) e.getSource();
 				l.setForeground(Color.BLACK);
 			}
 
 		});
 		userLabel.setToolTipText(user.getStatusType().getText());
 		userLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		if (UserStatus.AVAILABLE.equals(user.getStatusType())) {
 			userLabel.setIcon(UiUtil.createImageIcon(
 					"/images/bullet_ball_glass_green.png", ""));
 		} else if (UserStatus.BUSY.equals(user.getStatusType())) {
 			userLabel.setIcon(UiUtil.createImageIcon(
 					"/images/bullet_ball_glass_yellow.png", ""));
 		} else if (UserStatus.NOT_AVAILABLE.equals(user.getStatusType())) {
 			userLabel.setIcon(UiUtil.createImageIcon(
 					"/images/bullet_ball_glass_red.png", ""));
 		}
 
 		userLabel.setMinimumSize(new Dimension(100, 20));
 
 		return userLabel;
 	}
 
 	/**
 	 * Fuegt den {@link UserChangeListener} an, der auf hinzufuegen/entfernen
 	 * von Usern reagiert.
 	 */
 	public void setUserChangeListener() {
 
 		userService.addUserChangeListener(new UserChangeListener() {
 
 			@Override
 			public void userRemoved(List<User> user) {
 				EventDispatcher.fireEvent(Events.USER_OFFLINE_NOTICE, user);
 				refreshUi();
 			}
 
 			@Override
 			public void userAdded(List<User> user) {
 				EventDispatcher.fireEvent(Events.USER_ONLINE_NOTICE, user);
 				refreshUi();
 			}
 		});
 	}
 
 	/**
 	 * Aktualisiert regelmaessig das UserPanel mit den aktuellen Daten aus dem
 	 * {@link UserService}.
 	 */
 	public void startRefreshUsers() {
 		Timer t = new Timer("UI: Refresh UserPanel Timer");
 		TimerTask task = new TimerTask() {
 
 			@Override
 			public void run() {
 				refreshUi();
 			}
 		};
 		t.schedule(task, simConfiguration.getUserLoadDelay(),
 				simConfiguration.getUserLoadPeriod());
 	}
 
 	/**
 	 * Aktualisiert das UserPanel.
 	 */
 	public void refreshUi() {
		List<JLabel> userLabels = getCurrentUsers();

 		removeAll();

		for (JLabel userLabel : userLabels) {
			add(userLabel);
		}

 		validate();
 		repaint();
 	}

 }
