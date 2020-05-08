 package jmbs.client.Graphics;
 
 import javax.swing.JFrame;
 import javax.swing.JMenuBar;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.TitledBorder;
 import javax.swing.JScrollPane;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 
 import jmbs.client.ClientRequests;
 import jmbs.client.CurrentUser;
 import jmbs.common.Message;
 import jmbs.common.User;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 
 public class MainWindow {
 
 	private static JFrame frmJmbsClient = null;
 	private TimeLinePanel timelinepanel;
 	private ProfilePanel ppanel;
 	private NewMessageFrame nmFrame;
 	private AboutFrame about;
 	private UsersFrame uFrame;
 	private ArrayList<Message> msgListTL;
 	private User currentUser = new CurrentUser().get();
 
 	/**
 	 * Create the application. TODO: create a new class for the menubar
 	 */
 	public MainWindow() {
 		if (frmJmbsClient == null) {
 
 			initialize();
 		}
 	}
 
 	public JFrame getFrame() {
 		return frmJmbsClient;
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 * 
 	 * @wbp.parser.entryPoint
 	 */
 	private void initialize() {
 		
 		frmJmbsClient = new JFrame();
 		timelinepanel = new TimeLinePanel();
 		ppanel = new ProfilePanel();
 		nmFrame = new NewMessageFrame(timelinepanel);
 		about = new AboutFrame();
 		uFrame = new UsersFrame();
 
 		try {
 			msgListTL = new ClientRequests().getConnection().getLatestTL(
 					currentUser.getId(), 0);
 			timelinepanel.putList(msgListTL);
 		} catch (RemoteException e1) {
 			// TODO Auto-generated catch block
 			// e1.printStackTrace();
 			System.out.println("Can't get last timeLine from server ");
 		}
 
 		frmJmbsClient.setTitle("JMBS Client");
 		// frmJmbsClient.setBounds(100, 100, 365, 600);
 		frmJmbsClient.setSize(440, 600);
 		frmJmbsClient.setMinimumSize(new Dimension(440, 560));
 		frmJmbsClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmJmbsClient.setLocationRelativeTo(null);
 		// frmJmbsClient.setVisible(true);
 
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.setBackground(Color.LIGHT_GRAY);
 		frmJmbsClient.setJMenuBar(menuBar);
 		;
 		// frmJmbsClient.getContentPane().add(menuBar, BorderLayout.NORTH);
 
 		JMenu mnJmbs = new JMenu("JMBS");
 		mnJmbs.setBackground(Color.LIGHT_GRAY);
 		menuBar.add(mnJmbs);
 
 		JMenuItem mntmNewMessage = new JMenuItem("New Message");
 		mntmNewMessage.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				// Open the new Frame to write a new message
 				nmFrame.setVisible(true);
 			}
 		});
 		mnJmbs.add(mntmNewMessage);
 
 		JMenuItem mntmRefresh = new JMenuItem("Refresh");
 		mntmRefresh.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					msgListTL = new ClientRequests().getConnection()
 							.getLatestTL(currentUser.getId(),
 									timelinepanel.getLastIdMsg());
 					timelinepanel.putList(msgListTL);
 				} catch (RemoteException e1) {
 					// TODO Auto-generated catch block
 					// e1.printStackTrace();
 					System.out.println("Can't get last timeLine from server ");
 				}
 			}
 		});
 		mnJmbs.add(mntmRefresh);
 
 		JSeparator separator_4 = new JSeparator();
 		mnJmbs.add(separator_4);
 
 		JMenuItem mntmAboutJmbs = new JMenuItem("About JMBS");
 		mntmAboutJmbs.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				about.setVisible(true);
 			}
 		});
 		mnJmbs.add(mntmAboutJmbs);
 
 		JSeparator separator = new JSeparator();
 		mnJmbs.add(separator);
 
 		JMenuItem mntmPreferences = new JMenuItem("Preferences");
 		mnJmbs.add(mntmPreferences);
 
 		JMenuItem mntmEmptyCache = new JMenuItem("Empty cache");
 		mntmEmptyCache.setEnabled(false);
 		mnJmbs.add(mntmEmptyCache);
 
 		JSeparator separator_2 = new JSeparator();
 		mnJmbs.add(separator_2);
 
 		JMenuItem mntmHide = new JMenuItem("Hide");
 		mntmHide.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frmJmbsClient.setState(JFrame.ICONIFIED);
 			}
 		});
 		mnJmbs.add(mntmHide);
 
 		JMenuItem mntmDisconnect = new JMenuItem("Disconnect");
 		mntmDisconnect.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				// Close all other jmbs frames
 
 				if (nmFrame.isVisible())
 					nmFrame.dispose();
 				if (uFrame.isVisible())
 					uFrame.dispose();
 				if (about.isVisible())
 					about.dispose();
 
 				frmJmbsClient.dispose();
 				new CurrentUser().disconnect();
 				ConnectionFrame cf = new ConnectionFrame(new MainWindow());
 				cf.setVisible(true);
 			}
 		});
 		mnJmbs.add(mntmDisconnect);
 
 		JSeparator separator_1 = new JSeparator();
 		mnJmbs.add(separator_1);
 
 		JMenuItem mntmQuit = new JMenuItem("Quit");
 		mntmQuit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		mnJmbs.add(mntmQuit);
 
 		JMenu mnActivities = new JMenu("Activities");
 		mnActivities.setBackground(Color.LIGHT_GRAY);
 		menuBar.add(mnActivities);
 
 		JMenuItem mntmUsers = new JMenuItem("Users");
 		mntmUsers.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				// uFrame = new UsersFrame();
 				uFrame.setVisible(true);
 			}
 		});
 		mnActivities.add(mntmUsers);
 
 		JMenuItem mntmProjects = new JMenuItem("Projects");
 		mntmProjects.setEnabled(false);
 		mnActivities.add(mntmProjects);
 
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
 		tabbedPane.setBorder(new TitledBorder(null, "", TitledBorder.LEADING,
 				TitledBorder.BELOW_BOTTOM, null, null));
 		tabbedPane.setToolTipText("JMBS");
 		frmJmbsClient.getContentPane().add(tabbedPane, BorderLayout.CENTER);
 
 		JPanel tlpanel = new JPanel();
 		tlpanel.setToolTipText("TimeLine");
 		tabbedPane.addTab("TimeLine", null, tlpanel, null);
 
 		JScrollPane tlscrollPane = new JScrollPane();
 		tlscrollPane.setViewportBorder(UIManager
 				.getBorder("InsetBorder.aquaVariant"));
 		tlscrollPane
 				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		tlscrollPane.setViewportView(timelinepanel);
 		GroupLayout gl_tlpanel = new GroupLayout(tlpanel);
 		gl_tlpanel.setHorizontalGroup(
 			gl_tlpanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_tlpanel.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(tlscrollPane, GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
 					.addContainerGap())
 		);
 		gl_tlpanel.setVerticalGroup(
 			gl_tlpanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_tlpanel.createSequentialGroup()
 					.addComponent(tlscrollPane, GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
 					.addContainerGap())
 		);
 		tlpanel.setLayout(gl_tlpanel);
 
 		JPanel profpanel = new JPanel();
 		tabbedPane.addTab("Profile", null, profpanel, null);
 		profpanel.setLayout(new BorderLayout(0, 0));
 
 		JScrollPane profilescrollPane = new JScrollPane();
 		profilescrollPane.setViewportBorder(UIManager
 				.getBorder("InsetBorder.aquaVariant"));
 		profilescrollPane
 				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		profilescrollPane.setViewportView(ppanel);
 
 		profpanel.add(profilescrollPane);
 	}
 
 	/**
 	 * used to update the timelinepanel from the other windows as
 	 * NewMessageFrame
 	 * 
 	 * @return the default timeline Panel
 	 */
 	public TimeLinePanel getTLPanel() {
 		return this.timelinepanel;
 	}
 }
