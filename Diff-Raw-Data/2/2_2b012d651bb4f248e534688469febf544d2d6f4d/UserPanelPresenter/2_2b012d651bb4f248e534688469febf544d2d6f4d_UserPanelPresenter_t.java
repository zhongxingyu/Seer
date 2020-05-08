 package devhood.im.sim.ui.presenter;
 
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.SwingUtilities;
 
 import org.springframework.context.ApplicationContext;
 
 import devhood.im.sim.config.SimConfiguration;
 import devhood.im.sim.model.Receiver;
 import devhood.im.sim.model.Room;
 import devhood.im.sim.model.User;
 import devhood.im.sim.service.interfaces.UserChangeObserver;
 import devhood.im.sim.service.interfaces.UserService;
 import devhood.im.sim.ui.action.BarAction;
 import devhood.im.sim.ui.event.EventDispatcher;
 import devhood.im.sim.ui.event.EventObserver;
 import devhood.im.sim.ui.event.Events;
 import devhood.im.sim.ui.view.UserPanelView;
 
 @Named
 public class UserPanelPresenter implements EventObserver, UserChangeObserver {
 
 	/**
 	 * RegistryService zum Zugriff auf Stammdaten, zb User.
 	 */
 	@Inject
 	private UserService userService;
 
 	@Inject
 	private ApplicationContext applicationContext;
 
 	@Inject
 	private SimConfiguration simConfiguration;
 
 	@Inject
 	private UserPanelView view;
 
 	private boolean refreshed;
 
 	public UserPanelPresenter() {
 		EventDispatcher.add(this);
 	}
 
 	@Override
 	public void eventReceived(Events event, Object o) {
 
 	}
 
 	private Receiver currentReceiver;
 
 	@PostConstruct
 	public void init() {
 		startRefreshUsers();
 
 		userService.registerUserChangeObserver(this);
 
 		view.getOutlookBar().setOnBarSelected(new BarAction() {
 			@Override
 			public void execute(String barName) {
 				SwingUtilities.invokeLater(new Runnable() {
 
 					@Override
 					public void run() {
 						Receiver r = new Receiver();
 						r.setName(view.getSelectedRoom());
 						r.setRoom(true);
 						currentReceiver = r;
 						EventDispatcher.fireEvent(Events.RECEIVER_SELECTED, r);
 					}
 				});
 			}
 
 			@Override
 			public void execute() {
 
 			}
 		});
 
 		view.setQuitChatMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				SwingUtilities.invokeLater(new Runnable() {
 
 					@Override
 					public void run() {
 
 						userService.quitRoom(currentReceiver.getName());
 						EventDispatcher.fireEvent(Events.CLOSE_TAB,
 								currentReceiver.getName());
 						refreshUi();
 						view.getOutlookBar().showTabSelection(
 								simConfiguration.getStreamTabName(), true);
 					}
 				});
 			}
 		});
 
 		view.setAddToAutojoinListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					((JCheckBoxMenuItem) e.getSource()).setSelected(true);
 
 					SwingUtilities.invokeLater(new Runnable() {
 
 						@Override
 						public void run() {
 							simConfiguration.addAutojoinRooms(currentReceiver
 									.getName());
 						}
 					});
 				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
 
 					SwingUtilities.invokeLater(new Runnable() {
 
 						@Override
 						public void run() {
 							simConfiguration.removeAutojoinroom(currentReceiver
 									.getName());
 						}
 					});
 				}
 			}
 		});
 
 		view.getOutlookBar().setFirstBarLabel(
 				simConfiguration.getStreamTabName());
 
 		view.setFirstBarLabel(simConfiguration.getStreamTabName());
 		view.setNoQuitPossibleRoom(simConfiguration.getStreamTabName());
 	}
 
 	public Receiver getCurrentReceiver() {
 		if (currentReceiver == null) {
 			String title = view.getOutlookBar().getTitleOfButton(0);
 			currentReceiver = new Receiver();
 			currentReceiver.setRoom(true);
 			currentReceiver.setName(title);
 		}
 		return currentReceiver;
 	}
 
 	public void openRoom(String name) {
 		Room r = new Room();
 		r.setName(name);
 
 		if (!view.hasRoom(r)) {
 			view.addRoom(r);
 		} else {
 			view.getOutlookBar().showTabSelection(name, false);
 		}
 		Receiver receiver = new Receiver();
 		receiver.setRoom(true);
 		receiver.setName(name);
 		currentReceiver = receiver;
 	}
 
 	/**
 	 * Gibt die Labels der aktuellen User aus der DB zurueck.
 	 */
 	public List<User> getCurrentUsers() {
 		Iterable<User> users = userService.getUsers();
 		List<String> userFromDb = new ArrayList<String>();
 		List<User> currentUsers = new ArrayList<User>();
 
 		for (User user : users) {
 			userFromDb.add(user.getName());
 			currentUsers.add(user);
 		}
 
 		return currentUsers;
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
 				try {
 					refreshUi();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		};
 		t.schedule(task, simConfiguration.getUserLoadDelay(),
 				simConfiguration.getUserLoadPeriod());
 	}
 
 	private void refreshUi() {
 		List<Room> rooms = userService.getRooms();
 		view.refreshUi(rooms);
 
 		refreshed = true;
 	}
 
 	@Override
 	public void onUserRemoved(List<User> user) {
 		EventDispatcher.fireEvent(Events.USER_OFFLINE_NOTICE, user);
 	}
 
 	@Override
 	public void onUserAdded(List<User> user) {
 		EventDispatcher.fireEvent(Events.USER_ONLINE_NOTICE, user);
 	}
 
 	public UserPanelView getView() {
 		return view;
 	}
 
 }
