 package de.icetruck;
 
 import java.awt.Dimension;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 import javax.swing.ListModel;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 
 public class Application extends JFrame implements WindowListener {
 	private static Application app;
 	private static List<User> userList;
 	private UserListModel userListModel;
 	private List<String> chatList;
 	private ChatListModel chatListModel;
 	private Client client;
 	private User user_;
 	private Thread clientThread;
 	private JList chatListView;
 	private JScrollPane listViewPanel;
 	private boolean autoscroll_ = true;
 	public static final int maxLineCount = 100;
 
 	class User {
 		private int id_;
 		private String name_;
 
 		public User(int id, String name) {
 			id_ = id;
 			name_ = name;
 		}
 		public int getId() {
 			return id_;
 		}
 		public String getName() {
 			return name_;
 		}
 	}
 	class UserListModel implements ListModel {
 		private List<ListDataListener> listeners;
 
 		UserListModel() {
 			listeners = new LinkedList<ListDataListener>();
 		}
 		@Override
 		public void addListDataListener(ListDataListener l) {
 			listeners.add(l);
 		}
 		@Override
 		public Object getElementAt(int index) {
 			return userList.get(index).getName();
 		}
 		@Override
 		public int getSize() {
 			return userList.size();
 		}
 		@Override
 		public void removeListDataListener(ListDataListener l) {
 			listeners.remove(l);
 		}
 		
 		public String findName(int id) {
 			Iterator<User> i = userList.iterator();
 			while (i.hasNext()) {
 				User u = i.next();
 				if (u.getId() == id)
 					return u.getName();
 			}
 			return null;
 		}
 
 		public synchronized void add(User u) {
 			userList.add(u);
 			Iterator<ListDataListener> i = listeners.iterator();
 			while(i.hasNext()) {
 				i.next().intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, userList.size()-1, userList.size()-1));
 			}
 		}
 
 		public void del(int id) {
 			Iterator<User> ui = userList.iterator();
 			int userindex = -1;
 			boolean found = false;
 			while(ui.hasNext()) {
 				++userindex;
 				User u = ui.next();
 				if (u.getId() == id) {
 					found = true;
 					userList.remove(userindex);
 					break;
 				}
 			}
 			if (!found)
 				return;
 			
 			Iterator<ListDataListener> i = listeners.iterator();
 			while(i.hasNext()) {
 				ListDataListener l = i.next();
 				l.intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, userindex, userindex));
 			}
 		}
 	}
 	class ChatListModel implements ListModel {
 		private List<ListDataListener> listeners;
 
 		ChatListModel() {
 			listeners = new LinkedList<ListDataListener>();
 		}
 		@Override
 		public void addListDataListener(ListDataListener l) {
 			listeners.add(l);
 		}
 		@Override
 		public Object getElementAt(int index) {
 			return chatList.get(index);
 		}
 		@Override
 		public int getSize() {
 			return chatList.size();
 		}
 		@Override
 		public void removeListDataListener(ListDataListener l) {
 			listeners.remove(l);
 		}
 		public synchronized void add(String line) {
 			chatList.add(line);
 			Iterator<ListDataListener> i = listeners.iterator();
 			while(i.hasNext()) {
 				i.next().intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, chatList.size()-1, chatList.size()-1));
 			}
 		}
 	}
 
 	class SendView extends JTextField implements KeyListener {
 		public SendView() {
 			setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
 			addKeyListener(this);
 		}
 
 		@Override
 		public void keyPressed(KeyEvent e) {
 			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 				String text = getText();
 				setText("");
				client.sendMessage(text.replace("\\", "\\\\"));
 			}
 		}
 		@Override
 		public void keyReleased(KeyEvent e) {}
 		@Override
 		public void keyTyped(KeyEvent e) {}
 	}
 
 	public void addChatLine(String line) {
 		chatListModel.add(line);
 		chatListView.ensureIndexIsVisible(chatList.size()-1);
 	}
 
 	public void addUser(User u) {
 		userListModel.add(u);
 		addChatLine("== User joined: " + u.getName());
 	}
 
 	public void delUser(int id) {
 		addChatLine("== User left: " + userListModel.findName(id));
 		userListModel.del(id); 
 	}
 
 	public User userFromString(String s) {
 		int splitter = s.indexOf(":".charAt(0));
 		int id = Integer.parseInt(s.substring(0, splitter));
 		String name = s.substring(splitter + 1);
 		return new User(id, name);
 	}
 	
 	public void addCommand(Command c) {
 		String cmd = c.getCommand();
 		if (cmd.equalsIgnoreCase("MSG")) { // TODO user dependent
 			String subcmd = c.getSubCommand();
 			String username = null;
 			if (subcmd != null) {
 				username = userListModel.findName(Integer.parseInt(subcmd));
 			}
 			if (username == null) {
 				username = "System";
 			}
 			addChatLine(username + ": " + c.getParamLine());
 		} else if (cmd.equalsIgnoreCase("HELLO")) {
 			user_ = userFromString(c.getParamLine());
 		} else if (cmd.equalsIgnoreCase("JOIN")) {
 			User u = userFromString(c.getParamLine());
 			System.out.println("User: " + u.getId());
 			if (user_.getId() != u.getId()) {
 				addUser(u);
 			}
 		} else if (cmd.equalsIgnoreCase("LEAVE")) {
 			int uid = Integer.parseInt(c.getParamLine());
 			delUser(uid);
 		} else if (cmd.equalsIgnoreCase("USERLIST")) {
 			String line = c.getParamLine();
 			String[] users = line.split(" ");
 			for(int i = 0; i < users.length; ++i) {
 				addUser(userFromString(users[i]));
 			}
 		} else {
 			addChatLine("UnknownCMD: " + cmd);
 		}
 	}
 
 	public String getHost() {
 		return "icetruck.de";
 	}
 	public int getPort() {
 		return 12321;
 	}
 
 	public static Application getInstance() {
 		return app;
 	}
 
 	Application() {
 		chatList = new LinkedList<String>();
 		setSize(800, 600);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
 		
 		userList = new LinkedList<User>();
 		userListModel = new UserListModel();
 		JList userListView = new JList(userListModel);
 
 		new JScrollPane(userListView);
 
 		JPanel centerView = new JPanel();
 		centerView.setLayout(new BoxLayout(centerView, BoxLayout.Y_AXIS));
 
 		chatListModel = new ChatListModel();
 		chatListView = new JList(chatListModel);
 		listViewPanel = new JScrollPane(chatListView);
 		/* listViewPanel.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
 	        public void adjustmentValueChanged(AdjustmentEvent e) {
 	        	if (autoscroll_)
 	        		e.getAdjustable().setValue(e.getAdjustable().getMaximum());
 	        }
 	    }); */
 		centerView.add(listViewPanel);
 		SendView sendView = new SendView();
 		centerView.add(sendView);
 
 		JSplitPane splitView = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userListView, centerView);
 		splitView.setDividerLocation(150);
 		add(splitView);
 		client = new Client();
 		clientThread = new Thread(client);
 		clientThread.start();
 	}
 	
 	void run() {
 		setVisible(true);
 	}
 	
 	public static void main(String[] args) {
 		app = new Application();
 		app.run();
 	}
 	@Override
 	public void windowActivated(WindowEvent e) { }
 
 	@Override
 	public void windowClosed(WindowEvent e) {
 		client.setRunning(false);
 	}
 
 	@Override
 	public void windowClosing(WindowEvent e) { }
 
 	@Override
 	public void windowDeactivated(WindowEvent e) { }
 
 	@Override
 	public void windowDeiconified(WindowEvent e) { }
 
 	@Override
 	public void windowIconified(WindowEvent e) { }
 
 	@Override
 	public void windowOpened(WindowEvent e) { }
 }
