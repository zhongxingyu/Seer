 package devhood.im.sim.ui;
 
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.BoxLayout;
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 
 import devhood.im.sim.Sim;
 import devhood.im.sim.event.EventDispatcher;
 import devhood.im.sim.event.Events;
 import devhood.im.sim.model.User;
 import devhood.im.sim.service.interfaces.RegistryService;
 
 /**
  * Panel zur Auswahl der User. Aktualisiert sich selbst via
  * {@link RegistryService}.
  * 
  * @author flo
  * 
  */
 public class UserPanel extends JPanel {
 
 	/**
 	 * Aktuelle Usernamen im Panel.
 	 */
 	private List<String> currentUsers = new ArrayList<String>();
 
 	/**
 	 * RegistryService zum Zugriff auf Stammdaten, zb User.
 	 */
 	private RegistryService registryService;
 
 	/**
 	 * Laedt User 1 Sekunden nach dem Start.
 	 */
 	private int userLoadDelay = 1000;
 
 	/**
 	 * Laedt User alle 10 Sekunden.
 	 */
 	private int userLoadPeriod = 10000;
 
 	/**
 	 * true wenn user min. einmal refresht wurden.
 	 */
 	private boolean usersLoaded;
 
 	private boolean userProcessecd;
 
 	public UserPanel(RegistryService registryService) {
 		this.registryService = registryService;
 		init();
 	}
 
 	/**
 	 * Initialisiert, fllt das UserPanel.
 	 */
 	public void init() {
 		BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
 		setLayout(layout);
 
 		addUsers();
 		startUserRefreshingTimer();
 	}
 
 	/**
 	 * Verarbeitet die USer und prft ob sie bereits vorhanden, oder neu, oder
 	 * nicht mehr vorhanden sind.
 	 * 
 	 * @param users
 	 *            aktuelle USer aus der dB.
 	 */
 	public void processNewOrRemovedUsers(List<User> users,
 			List<String> usersFromDb) {
 		if (!usersLoaded) {
 			return;
 		}
 
 		List<String> newUsers = new ArrayList<String>();
 		List<String> offlineUsers = new ArrayList<String>();
 
 		for (User u : users) {
 			if (currentUsers.size() == 0) {
 				currentUsers.add(u.getName());
 				continue;
 			}
 
 			if (!currentUsers.contains(u.getName())) {
 				if (!Sim.username.equals(u.getName())) {
 					newUsers.add(u.getName());
 				}
 				currentUsers.add(u.getName());
 			}
 		}
 
 		Iterator<String> it = currentUsers.iterator();
 		while (it.hasNext()) {
 			String user = it.next();
 			if (!usersFromDb.contains(user)) {
 				if (!Sim.username.equals(user)) {
 					offlineUsers.add(user);
 				}
 				it.remove();
 			}
 		}
 
 		if (offlineUsers.size() > 0) {
 			EventDispatcher.fireEvent(Events.USER_OFFLINE_NOTICE,
 					offlineUsers.toString());
 		}
 		if (newUsers.size() > 0) {
 			EventDispatcher.fireEvent(Events.USER_ONLINE_NOTICE,
 					newUsers.toString());
 
 		}
 	}
 
 	/**
 	 * Fuegt die Users aus registryService in die Liste ein.
 	 */
 	public void addUsers() {
 		List<User> users = registryService.getUsers();
 		List<String> userFromDb = new ArrayList<String>();
 
 		for (User user : users) {
 			JCheckBox userCheckBox = new JCheckBox(user.getName());
 			userCheckBox.setName(user.getName());
 			userCheckBox.addItemListener(new ItemListener() {
 
 				/**
 				 * Jede Checkbox bekommt einen Listener, der anschlgt, wenn Sie
 				 * ausgewhlt wurde. Dabei wird Ein Events.USER_SELECTED
 				 * gefeuert.
 				 */
 				@Override
 				public void itemStateChanged(ItemEvent e) {
 					if (e.getStateChange() == ItemEvent.SELECTED) {
 						JCheckBox box = (JCheckBox) e.getSource();
 						EventDispatcher.fireEvent(Events.USER_SELECTED, box);
 					}
 				}
 			});
 
 			add(userCheckBox);
 
 			userFromDb.add(user.getName());
 
 		}
 
 		processNewOrRemovedUsers(users, userFromDb);
 	}
 
 	/**
 	 * Startet den Thread, der die User liste aktuell haelt.
 	 */
 	public void startUserRefreshingTimer() {
 		Timer t = new Timer("Load Users Timer");
 		TimerTask task = new TimerTask() {
 			@Override
 			public void run() {
 				registryService.purgeOfflineUsers();
 
 				removeAll();
 				addUsers();
 				validate();
 				usersLoaded = true;
 			}
 		};
 
 		t.schedule(task, userLoadDelay, userLoadPeriod);
 	}
 }
