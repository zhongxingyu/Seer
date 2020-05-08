 package com.willluongo.asbestos.gui;
 
 import java.awt.Dialog;
 import java.beans.Beans;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.TraverseEvent;
 import org.eclipse.swt.events.TraverseListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Text;
 
 import com.madhackerdesigns.jinder.Campfire;
 import com.madhackerdesigns.jinder.Room;
 import com.madhackerdesigns.jinder.models.Message;
 import com.madhackerdesigns.jinder.models.User;
 
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 
 public class AsbestosWindow {
 
 	protected Shell shlAsbestos;
 
 	private static Campfire campfire;
 	private static String SUBDOMAIN = null;
 	private static String TOKEN = null;
 	private static final Logger log = LogManager.getLogger(AsbestosWindow.class
 			.getName());
 	private Text text_messages;
 	private Room room = null;
 	private SortedSet<User> users = null;
 	private SortedSet<Long> userIds = new TreeSet<Long>();
 	private Message lastMessage = new Message();
 	private Hashtable<Long, User> userCache = new Hashtable<Long, User>();
	private Hashtable<Long, RoomTab> tabs = new Hashtable<Long, RoomTab>();
 	private ArrayList<Room> rooms = new ArrayList<Room>();
 
 	// UI Elements
 	private Display display = null;
 	private TabFolder tabFolder;
 
 	private static final int UPDATERATE = 1000;
 
 	/**
 	 * Launch the application.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		initialize();
 
 		try {
 			AsbestosWindow window = new AsbestosWindow();
 			window.open();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void initialize() {
 		log.debug("Loading properties from file...");
 		Properties prop = new Properties();
 		try {
 			prop.load(new FileInputStream("asbestos.properties"));
 			SUBDOMAIN = prop.getProperty("campfire.subdomain");
 			TOKEN = prop.getProperty("campfire.token");
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		log.debug("Campfire subdomain: ".concat(SUBDOMAIN));
 		log.debug("Campfire token: ".concat(TOKEN));
 		campfire = new Campfire(SUBDOMAIN, TOKEN);
 	}
 
 	private void updateMessages() {
 		Set<Long> keys = tabs.keySet();
 		for (long key : keys)
 		{
 			tabs.get(key).update();
 		}
 
 	}
 
 	/**
 	 * Open the window.
 	 */
 	public void open() {
 		Display.setAppName("Asbestos");
 		display = Display.getDefault();
 
 		createContents();
 		shlAsbestos.open();
 		shlAsbestos.layout();
 
 		Runnable timer = new Runnable() {
 
 			@Override
 			public void run() {
 				updateMessages();
 				display.timerExec(UPDATERATE, this);
 
 			}
 
 		};
 		display.timerExec(UPDATERATE, timer);
 		while (!shlAsbestos.isDisposed()) {
 
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	/**
 	 * Create contents of the window.
 	 */
 	protected void createContents() {
 		shlAsbestos = new Shell();
 		shlAsbestos.setSize(450, 300);
 		shlAsbestos.setText("Asbestos");
 		tabFolder = new TabFolder(shlAsbestos, SWT.NONE);
 		tabFolder.setBounds(0, 0, 450, 299);
 
 		try {
 			users = campfire.users();
 			log.debug(users);
 
 			selectRooms();
 
 			for (User user : users) {
 
 				log.debug(user.name);
 				log.debug(user.id);
 				userIds.add(user.id);
 			}
 
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		Menu menu = new Menu(shlAsbestos, SWT.BAR);
 		shlAsbestos.setMenuBar(menu);
 
 		MenuItem mntmRooms_1 = new MenuItem(menu, SWT.CASCADE);
 		mntmRooms_1.setText("Rooms");
 
 		Menu menu_1 = new Menu(mntmRooms_1);
 		mntmRooms_1.setMenu(menu_1);
 
 		MenuItem mntmSelectRooms = new MenuItem(menu_1, SWT.NONE);
 		mntmSelectRooms.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				try {
 					selectRooms();
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		});
 		mntmSelectRooms.setText("Select room...");
 		updateMessages();
 
 	}
 
 	private void selectRooms() throws IOException {
		Set<Long> keys = tabs.keySet();
		for (long key : keys)
		{
			tabs.get(key).dispose();
		}
 		tabs = new Hashtable<Long, RoomTab>();
 		if (Beans.isDesignTime()) {
 			rooms = (ArrayList<Room>) campfire.rooms();
 		} else {
 			RoomSelector select = new RoomSelector(shlAsbestos, 0,
 					campfire.rooms());
 
 			rooms = select.open();
 		}
 		// Create tabs here
 		for (Room room : rooms) {
 			tabs.put(room.id, new RoomTab(room, tabFolder));
 		}
 	}
 }
