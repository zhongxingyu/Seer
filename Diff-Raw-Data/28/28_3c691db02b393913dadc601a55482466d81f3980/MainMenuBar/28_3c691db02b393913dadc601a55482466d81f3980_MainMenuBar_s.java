 package jmbs.client.Graphics;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JSeparator;
 
 import jmbs.client.ClientRequests;
 import jmbs.client.CurrentUser;
 import jmbs.client.SysConf;
 import jmbs.common.Message;
 import jmbs.common.User;
 
 public class MainMenuBar extends JMenuBar {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 2765541073906585914L;
 	private static TimeLinePanel timelinepanel;
 	private NewMessageFrame nmFrame;
 	private AboutFrame about;
 	private UsersFrame uFrame;
 	private ArrayList<Message> msgListTL;
 	private JFrame frmJmbsClient;
 	private User currentUser = new CurrentUser().get();
 	private MyApplicationListener listener;
	private Preferences prfrm;
 	private boolean isMac = new SysConf().isMac() ? true : false;
 
 	/**
 	 * Create the panel.
 	 */
 	public MainMenuBar(MainWindow mw) {
 
 		if (isMac) {
 
 			listener = new MyApplicationListener();
 
 		}
 
 		frmJmbsClient = mw.getFrame();
 		timelinepanel = mw.getTLPanel();
 		nmFrame = mw.getNmFrame();
 		about = mw.getAbout();
 		uFrame = mw.getuFrame();
 		msgListTL = mw.getMsgListTL();
 
 		setBackground(Color.LIGHT_GRAY);
 		// frmJmbsClient.setJMenuBar(this);
 		// frmJmbsClient.getContentPane().add(menuBar, BorderLayout.NORTH);
 
 		JMenu mnFile = new JMenu("File");
 		mnFile.setBackground(Color.LIGHT_GRAY);
 		add(mnFile);
 
 		JMenuItem mntmNewMessage = new JMenuItem("New Message");
 		mntmNewMessage.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				// Open the new Frame to write a new message
 				nmFrame.setVisible(true);
 			}
 		});
 		mnFile.add(mntmNewMessage);
 
 		JMenuItem mntmRefresh = new JMenuItem("Refresh");
 		mntmRefresh.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
					msgListTL = new ClientRequests().getConnection().
							getLatestTL(currentUser.getId(), timelinepanel.getLastIdMsg(),ClientRequests.maxReceivedMsgs);
 					timelinepanel.putList(msgListTL);
 				} catch (RemoteException e1) {
 					// TODO Auto-generated catch block
 					// e1.printStackTrace();
 					System.out.println("Can't get last timeLine from server ");
 				}
 			}
 		});
 		mnFile.add(mntmRefresh);
 
 		JSeparator separator_4 = new JSeparator();
 		mnFile.add(separator_4);
 
 		JMenuItem mntmAboutJmbs = new JMenuItem("About JMBS");
 		mntmAboutJmbs.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				about.setVisible(true);
 			}
 		});
 		mnFile.add(mntmAboutJmbs);
 
 		JSeparator separator = new JSeparator();
 		mnFile.add(separator);
 
 		JMenuItem mntmPreferences = new JMenuItem("Preferences");
 		mntmPreferences.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
				prfrm = new Preferences();
 				prfrm.setVisible(true);
 			}
 		});
 		mnFile.add(mntmPreferences);
 
 		JMenuItem mntmEmptyCache = new JMenuItem("Empty cache");
 		mntmEmptyCache.setEnabled(false);
 		mnFile.add(mntmEmptyCache);
 
 		JSeparator separator_2 = new JSeparator();
 		mnFile.add(separator_2);
 
 		JMenuItem mntmHide = new JMenuItem("Hide");
 		mntmHide.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frmJmbsClient.setState(JFrame.ICONIFIED);
 			}
 		});
 		mnFile.add(mntmHide);
 
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
				if(prfrm.isVisible())
 					prfrm.dispose();
 
 				frmJmbsClient.dispose();
 				new CurrentUser().disconnect();
 				ConnectionFrame cf = new ConnectionFrame(new MainWindow());
 				cf.setVisible(true);
 			}
 		});
 		mnFile.add(mntmDisconnect);
 
 		JSeparator separator_1 = new JSeparator();
 		mnFile.add(separator_1);
 
 		JMenuItem mntmQuit = new JMenuItem("Quit");
 		mntmQuit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		mnFile.add(mntmQuit);
 
 		JMenu mnActivities = new JMenu("Activities");
 		mnActivities.setBackground(Color.LIGHT_GRAY);
 		add(mnActivities);
 
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
 	}
 
 	public MyApplicationListener getApplicationListener() {
 		return listener;
 	}
 
 }
