 package view;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.TreeMap;
 import java.util.logging.Level;
 
 import javax.swing.BorderFactory;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.EtchedBorder;
 
 import logger.MabisLogger;
 import model.entity.User;
 import utilities.Hasher;
 import view.imagePanel.ChoosableImagePanel;
 import view.mainwindow.MainWindow;
 import view.passwordDialog.PasswordDialog;
 import controller.SQLStamentType;
 import controller.entity.UserSQLFactory;
 import database.MySQLAccess;
 
 /**
  * Klasa dziedzicz�c z JDialog jest oknem typu dialogowego (modalnego) kt�re
  * pozwala na wyb�r u�ytkownika, kt�ry b�dzie korzysta� z programu. </br>
  * U�ytkowniky pobierani s� z lokalnie zlokalizowanej bazy danych. Klasa
  * implementuje interfejs {@link PropertyChangeListener} aby umo�liwi�
  * dynamiczne pod�wietlanie wybranego u�ytkownika. Opisana funkcjonalno�� dzia�a
  * w po��czeniu z {@link ChoosableImagePanel}.
  * 
  * @author kornicameister
  * @version 0.2
  * @see java.beans.PropertyChangeListener
  */
 public class UserSelectionPanel extends JFrame implements
 		PropertyChangeListener {
 
 	private static final long serialVersionUID = -3642888588569732458L;
 	private static final Dimension avatarSize = new Dimension(100, 100);
 	private MainWindow mw = null;
 	private JButton connectButton = null;
 	private JButton cancelButton = null;
 	private JPanel userListPanel;
 	private HashMap<Integer, User> users = null;
 	private JPanel rootListPanel = null;
 	private JScrollPane userScrollPanel = null;
 	private short selectedUserIndex = -1;
 
 	private final TreeMap<User, ChoosableImagePanel> thumbails = new TreeMap<User, ChoosableImagePanel>();
 	private final UserSelectionPanelListener listener = new UserSelectionPanelListener(
 			this);
 	private final UserSQLFactory userFactory = new UserSQLFactory(
 			SQLStamentType.SELECT, new User());
 
 	/**
 	 * Tworzy okno dialogowe z rodzicem.
 	 */
 	public UserSelectionPanel(MainWindow owner) {
 		super();
 		this.mw = (MainWindow) owner;
 		this.init();
 
 		this.obtainUsers();
 		this.initThumbailList();
 
 	}
 
 	public UserSelectionPanel(HashMap<Integer, User> users, Frame owner) {
 		super();
 		this.mw = (MainWindow) owner;
 		this.users = users;
 		this.init();
 
 		this.parseUsers();
 		this.initThumbailList();
 	}
 
 	protected void init() {
 		super.frameInit();
 		this.initComponents();
 		this.layoutComponents();
 		this.initMeta();
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		super.finalize();
 		this.users.clear();
 		this.thumbails.clear();
 	}
 
 	private void parseUsers() {
 		for (User u : this.users.values()) {
 			ChoosableImagePanel p = new ChoosableImagePanel(u.getPictureFile()
 					.getImageFile());
 			p.addPropertyChangeListener(this);
 			thumbails.put(u, p);
 		}
 	}
 
 	private void initThumbailList() {
 		for (User u : this.users.values()) {
 			// we have user u, now lets get it's panel
 			ChoosableImagePanel i = this.thumbails.get(u);
 			i.setPreferredSize(avatarSize);
 			i.setBorder(BorderFactory.createTitledBorder(
 					BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
 					u.getLogin()));
 			this.userListPanel.add(i);
 		}
 	}
 
 	private void initMeta() {
 		this.setSize(new Dimension(500, 280));
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		this.setTitle("Connect as...");
 		this.setLocation(this.mw.getX() + this.mw.getWidth() / 4,
 				this.mw.getY() + 25);
 		this.setAlwaysOnTop(false);
 	}
 
 	/**
 	 * Metoda zadeklarowana jako final static dlatego</br> Dostępna w całym
 	 * pakiecie ponieważ wykonuje działanie niezależne od klasy w której jest
 	 * zdefiniowane. </br> Metoda korzystając z UserSQLFactory, łączy się z bazą
 	 * danych online i pobiera stamtąd wszystkich użytkowników
 	 */
 	// TODO dodać link do UserSQLFactory
 	// TODO przenieść metody typu util do oddzielnego pakietu
 	private void obtainUsers() {
 		try {
			this.userFactory.setStatementType(SQLStamentType.SELECT);
 			this.userFactory.executeSQL(true);
 			this.users = this.userFactory.getUsers();
 			this.parseUsers();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private final void initComponents() {
 		this.rootListPanel = new JPanel(true);
 
 		this.userScrollPanel = new JScrollPane(this.rootListPanel,
 				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		this.userScrollPanel
 				.setBorder(BorderFactory.createTitledBorder(
 						BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
 						"Users"));
 
 		this.userListPanel = new JPanel(true);
 		this.userListPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 
 		this.rootListPanel.add(this.userListPanel);
 
 		this.connectButton = new JButton("Connect");
 		this.connectButton.addActionListener(this.listener);
 		this.cancelButton = new JButton("Cancel");
 		this.cancelButton.addActionListener(this.listener);
 
 	}
 
 	public final void layoutComponents() {
 		GroupLayout layout = new GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 
 		layout.setAutoCreateGaps(true);
 		layout.setAutoCreateContainerGaps(true);
 
 		layout.setHorizontalGroup(layout
 				.createParallelGroup()
 				.addComponent(this.userScrollPanel)
 				.addGroup(
 						layout.createSequentialGroup()
 								.addComponent(this.connectButton,
 										GroupLayout.DEFAULT_SIZE,
 										GroupLayout.PREFERRED_SIZE,
 										Short.MAX_VALUE)
 								.addComponent(this.cancelButton,
 										GroupLayout.DEFAULT_SIZE,
 										GroupLayout.PREFERRED_SIZE,
 										Short.MAX_VALUE)));
 		layout.setVerticalGroup(layout
 				.createSequentialGroup()
 				.addComponent(this.userScrollPanel)
 				.addGroup(
 						layout.createParallelGroup()
 								.addComponent(this.connectButton)
 								.addComponent(this.cancelButton)));
 
 		this.pack();
 		this.repaint();
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent e) {
 		if ((Boolean) e.getNewValue() == true) {
 			ChoosableImagePanel p = (ChoosableImagePanel) e.getSource();
 			this.selectedUserIndex = -1;
 			boolean panelLocated = false;
 			for (ChoosableImagePanel pp : this.thumbails.values()) {
 				pp.demark();
 				if (!panelLocated) {
 					if (pp.equals(p)) {
 						panelLocated = true;
 						pp.mark();
 						this.selectedUserIndex += 1;
 					} else {
 						this.selectedUserIndex += 1;
 					}
 				}
 			}
 		}
 	}
 
 	class UserSelectionPanelListener implements ActionListener {
 		UserSelectionPanel usp = null;
 		private PasswordDialog pd;
 
 		public UserSelectionPanelListener(UserSelectionPanel userSelectionPanel) {
 			this.usp = userSelectionPanel;
 			this.pd = new PasswordDialog(userSelectionPanel);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			JButton source = (JButton) e.getSource();
 			if (source.equals(connectButton)) {
 				User u = (User) users.values().toArray()[selectedUserIndex];
 
 				pd.setVisible(true);
 				// while(pd.isVisible());
 				String password = pd.getPassword();
 
 				if (!u.getPassword().equals(Hasher.hashPassword(password))) {
 					JOptionPane.showMessageDialog(null,
 							"This is not valid password of " + u.getLogin()
 									+ " user", "Wrong password",
 							JOptionPane.ERROR_MESSAGE);
 					MabisLogger.getLogger().log(Level.INFO,
 							"Wrong credentials provided, password differs");
 				} else {
 					JOptionPane.showMessageDialog(null,
 							"Connected as " + u.getLogin(),
 							"God In Da House ;-)",
 							JOptionPane.INFORMATION_MESSAGE);
 					connectWithUser(u);
 					setVisible(false);
 					dispose();
 				}
 			} else if (source.equals(cancelButton)) {
 				if (isDisplayable()) {
 					setVisible(false);
 					dispose();
 				}
 			}
 			MabisLogger.getLogger().log(Level.INFO, source.getActionCommand());
 		}
 
 		private void connectWithUser(User u) {
 			mw.getBottomPanel()
 					.getStatusBar()
 					.setMessage(
 							"Connected as " + u.getLogin() + " at "
 									+ MySQLAccess.getLocalhost());
 			mw.setConnectedUser(u);
 		}
 	}
 }
