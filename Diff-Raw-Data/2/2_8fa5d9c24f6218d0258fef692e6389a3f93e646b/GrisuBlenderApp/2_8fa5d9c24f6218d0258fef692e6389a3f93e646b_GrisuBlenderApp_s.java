 package org.vpac.grisu.clients.blender.swing;
 
 import java.awt.AWTEvent;
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 import java.awt.Toolkit;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.JFrame;
 import javax.swing.UIManager;
 
 import org.jdesktop.swingx.JXFrame;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.frontend.control.login.LoginManager;
 import org.vpac.grisu.frontend.model.events.ApplicationEventListener;
 import org.vpac.grisu.frontend.view.swing.GrisuMainPanel;
 import org.vpac.grisu.frontend.view.swing.WindowSaver;
 import org.vpac.grisu.frontend.view.swing.login.LoginPanel;
 
 public class GrisuBlenderApp implements WindowListener {
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 
 		LoginManager.initEnvironment();
 
 		final Toolkit tk = Toolkit.getDefaultToolkit();
 		tk.addAWTEventListener(WindowSaver.getInstance(),
 				AWTEvent.WINDOW_EVENT_MASK);
 
 		new ApplicationEventListener();
 
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (final Exception e) {
 
 		}
 
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					final GrisuBlenderApp window = new GrisuBlenderApp();
 					window.frame.setVisible(true);
 				} catch (final Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	private ServiceInterface si;
 
 	private GrisuMainPanel mainPanel;
 
 	private LoginPanel lp;
 
 	private JXFrame frame;
 
 	/**
 	 * Create the application.
 	 */
 	public GrisuBlenderApp() {
 		initialize();
 	}
 
 	private void exit() {
 		try {
			System.out.println("Exiting...");
 
 			if (si != null) {
 				si.logout();
 			}
 
 		} finally {
 			WindowSaver.saveSettings();
 			System.exit(0);
 		}
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JXFrame();
 		frame.addWindowListener(this);
 		// frame.setBounds(100, 100, 450, 300);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		frame.getContentPane().setLayout(new BorderLayout());
 		final Set<String> apps = new HashSet<String>();
 		apps.add("blender");
 		mainPanel = new GrisuMainPanel(false, apps);
 		mainPanel.addJobCreationPanel(new BlenderJobCreationPanel());
 		// LoginPanel lp = new LoginPanel(mainPanel, true);
 		final LoginPanel lp = new LoginPanel(mainPanel);
 		frame.getContentPane().add(lp, BorderLayout.CENTER);
 	}
 
 	public void setServiceInterface(ServiceInterface si) {
 
 		if (lp == null) {
 			throw new IllegalStateException("LoginPanel not initialized.");
 		}
 
 		if (si == null) {
 			throw new NullPointerException("ServiceInterface can't be null");
 		}
 
 		lp.setServiceInterface(si);
 
 	}
 
 	@Override
 	public void windowActivated(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void windowClosed(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void windowClosing(WindowEvent arg0) {
 		exit();
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void windowIconified(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void windowOpened(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
