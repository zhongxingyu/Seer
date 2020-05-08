 package org.vpac.grisu.client.view.swing.mainPanel;
 
 import java.awt.AWTEvent;
 import java.awt.BorderLayout;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.Arrays;
 import java.util.Date;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.vpac.grisu.client.control.EnvironmentManager;
 import org.vpac.grisu.client.control.files.FileManagerDeleteHelpers;
 import org.vpac.grisu.client.control.files.FileManagerTransferHelpers;
 import org.vpac.grisu.client.control.status.ApplicationStatusManager;
 import org.vpac.grisu.client.control.utils.progress.ProgressDisplay;
 import org.vpac.grisu.client.control.utils.progress.swing.SwingProgressDisplay;
 import org.vpac.grisu.client.view.swing.fileTransfers.FileTransferPanel;
 import org.vpac.grisu.client.view.swing.filemanager.GrisuFilePanel;
 import org.vpac.grisu.client.view.swing.jobs.GlazedJobMonitorPanel;
 import org.vpac.grisu.client.view.swing.login.LoginDialog;
 import org.vpac.grisu.client.view.swing.login.LoginSplashScreen;
 import org.vpac.grisu.client.view.swing.mountpoints.MountPointsManagementDialog;
 import org.vpac.grisu.client.view.swing.template.SubmissionPanel;
 import org.vpac.grisu.client.view.swing.utils.Utils;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.model.GrisuRegistry;
 import org.vpac.grisu.model.GrisuRegistryManager;
 import org.vpac.grisu.settings.ClientPropertiesManager;
 import org.vpac.grisu.settings.Environment;
 import org.vpac.grisu.utils.GrisuPluginFilenameFilter;
 import org.vpac.helpDesk.control.HelpDeskManager;
 import org.vpac.helpDesk.model.HelpDesk;
 import org.vpac.helpDesk.model.HelpDeskNotAvailableException;
 import org.vpac.helpDesk.view.TicketSubmissionDialogMultipleHelpdesks;
 import org.vpac.security.light.control.CertificateFiles;
 
 import au.org.arcs.auth.shibboleth.Shibboleth;
 import au.org.arcs.jcommons.dependencies.ClasspathHacker;
 
 public class Grisu implements WindowListener {
 
 	static final Logger myLogger = Logger.getLogger(Grisu.class.getName());
 
 	public static final String apache2License = "http://www.apache.org/licenses/LICENSE-2.0";
 
 	public static final String GRISU_VERSION = "v0.3-SNAPSHOT";
 
 	/**
 	 * Launches this application
 	 */
 	public static void main(String[] args) {
 
 		Shibboleth.initDefaultSecurityProvider();
 
 		if ((args.length > 0) && (Arrays.binarySearch(args, "--debug") >= 0)) {
 			Level lvl = Level.toLevel("debug");
 			Logger.getRootLogger().setLevel(lvl);
 		}
 
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 
 				final Grisu application = new Grisu();
 
 				Toolkit tk = Toolkit.getDefaultToolkit();
 				tk.addAWTEventListener(WindowSaver.getInstance(),
 						AWTEvent.WINDOW_EVENT_MASK);
 
 				try {
 					UIManager.setLookAndFeel(UIManager
 							.getSystemLookAndFeelClassName());
 				} catch (Exception e) {
 					myLogger.debug("Could not set OS look & feel.");
 				}
 
 				try {
					CertificateFiles.copyCACerts(true);
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					// e1.printStackTrace();
 				}
 				myLogger.debug("Starting login dialog.");
 				LoginDialog ld = new LoginDialog();
 				ld.addWindowListener(new WindowAdapter() {
 					@Override
 					public void windowClosing(WindowEvent e) {
 						System.exit(0);
 					}
 				});
 				ld.setVisible(true);
 
 				if (ld.userCancelledLogin()) {
 					myLogger.debug("User cancelled login dialog.");
 					System.exit(0);
 				}
 
 				application.serviceInterface = ld.getServiceInterface();
 				myLogger.debug("Removing login dialog.");
 				ld.dispose();
 
 				myLogger.debug("Creating splash screen.");
 				final LoginSplashScreen lss = new LoginSplashScreen();
 				lss.addWindowListener(new WindowAdapter() {
 					@Override
 					public void windowClosing(WindowEvent e) {
 						System.exit(0);
 					}
 				});
 				ApplicationStatusManager.getDefaultManager().addStatusListener(
 						lss);
 				lss.setVisible(true);
 
 				new Thread() {
 					@Override
 					public void run() {
 						myLogger.debug("Creating progress bars.");
 
 						try {
 							application.em = new EnvironmentManager(
 									application.serviceInterface);
 
 							// GrisuRegistry defaultRegistry;
 							// try {
 							// // throw new RuntimeException();
 							// defaultRegistry = new
 							// ClientSideGrisuRegistry(application.serviceInterface);
 							// myLogger.info("Using client side mds library.");
 							// } catch (Exception e) {
 							// myLogger.info("Couldn't use client side mds library: "+e.getLocalizedMessage());
 							// myLogger.info("Using grisu service interface to calculate mds information...");
 							// defaultRegistry = new
 							// GrisuRegistryImpl(application.serviceInterface);
 							// }
 							//
 							// GrisuRegistryManager.setDefault(application.serviceInterface,
 							// defaultRegistry);
 
 							GrisuRegistry temp = GrisuRegistryManager
 							.getDefault(application.serviceInterface);
 							temp.setUserEnvironmentManager(application.em);
 							application.em.initializeHistoryManager();
 							if (application.serviceInterface == null) {
 								myLogger
 								.debug("Could not create/find service interface. Exiting.");
 								Utils.showErrorMessage(application.em, null,
 										"startupError", null);
 								System.exit(1);
 							}
 						} catch (Exception e) {
 							// TODO Auto-generated catch block
 							Utils.showErrorMessage(application.em, null,
 									"startupError", e);
 							e.printStackTrace();
 							System.exit(1);
 						}
 
 						// application.em.getFileManager().initAllFileSystemsInBackground();
 						// application.em.buildInfoCacheInBackground();
 						// application.em.getGlazedJobManagement().loadAllJobsInBackground();
 
 						// ProgressDisplay pg_environment = new
 						// SwingProgressDisplay(application.getJFrame());
 						// ProgressDisplay pg_submission = new
 						// SwingProgressDisplay(application.getJFrame());
 						ProgressDisplay pg_file_managementTransfer = new SwingProgressDisplay(
 								application.getJFrame());
 						ProgressDisplay pg_file_deletion = new SwingProgressDisplay(
 								application.getJFrame());
 
 						// EnvironmentManager.progressDisplay = pg_environment;
 						FileManagerTransferHelpers.progressDisplay = pg_file_managementTransfer;
 						FileManagerDeleteHelpers.progressDisplay = pg_file_deletion;
 
 						myLogger.debug("Setting application window visible.");
 						application.getJFrame().setVisible(true);
 
 						Thread
 						.setDefaultUncaughtExceptionHandler(new GrisuRuntimeExceptionHandler(
 								application.getJFrame()));
 
 						ApplicationStatusManager.getDefaultManager()
 						.removeStatusListener(lss);
 						myLogger.debug("Removing splash screen.");
 						lss.dispose();
 
 						// now test whether there is a VO available
 						int availFqans = application.em.getAvailableFqans().length;
 						myLogger.debug("Number of avail Fqans: " + availFqans);
 						int usedFqans = application.em.getAllUsedFqans().size();
 						myLogger.debug("Number of used Fqans: " + usedFqans);
 						if (availFqans == 0) {
 							Utils.showErrorMessage(application.em, application
 									.getJFrame(), "noVOs", null);
 						} else if (usedFqans == 0) {
 							Utils.showErrorMessage(application.em, application
 									.getJFrame(), "noUsableVOs", null);
 						}
 
 					}
 				}.start();
 
 			}
 		});
 	}
 
 	private JFrame jFrame = null; // @jve:decl-index=0:visual-constraint="10,10"
 
 	private JPanel jContentPane = null;
 
 	private JMenuBar jJMenuBar = null;
 
 	private JMenu fileMenu = null;
 
 	private JMenu toolsMenu = null;
 
 	private JMenu helpMenu = null;
 
 	private JMenuItem exitMenuItem = null;
 
 	private JMenuItem addLocalTemplateItem = null;
 
 	private JMenuItem aboutMenuItem = null;
 
 	private JMenuItem requestHelpMenuItem = null;
 
 	private JMenuItem proxyEndTimeMenuItem = null;
 
 	private JMenuItem mountsMenuItem = null;
 
 	private JDialog aboutDialog = null; // @jve:decl-index=0:visual-constraint="739,169"
 
 	private MountPointsManagementDialog mountsDialog = null;
 
 	private JPanel aboutContentPane = null;
 
 	private JLabel aboutVersionLabel = null;
 	private ServiceInterface serviceInterface = null;
 
 	// private JPanel jobSubmissionPanel = null;
 
 	private EnvironmentManager em = null;
 
 	private SubmissionPanel submissionPanel = null;
 
 	private GlazedJobMonitorPanel jobMonitorPanel = null;
 
 	private FileTransferPanel fileTransferPanel = null;
 
 	private JTabbedPane jTabbedPane = null;
 
 	public Grisu() {
 		addPluginsToClasspath();
 	}
 
 	private void addPluginsToClasspath() {
 
 		ClasspathHacker.initFolder(Environment.getGrisuPluginDirectory(),
 				new GrisuPluginFilenameFilter());
 
 	}
 
 	private void exit() {
 		try {
 			serviceInterface.logout();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			WindowSaver.saveSettings();
 			System.exit(0);
 		}
 	}
 
 	/**
 	 * This method initializes aboutContentPane
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getAboutContentPane() {
 		if (aboutContentPane == null) {
 			aboutContentPane = new JPanel();
 			aboutContentPane.setLayout(new BorderLayout());
 			aboutContentPane.add(getAboutVersionLabel(), BorderLayout.CENTER);
 		}
 		return aboutContentPane;
 	}
 
 	/**
 	 * This method initializes aboutDialog
 	 * 
 	 * @return javax.swing.JDialog
 	 */
 	private JDialog getAboutDialog() {
 		if (aboutDialog == null) {
 
 			// List<String> contributors = new LinkedList<String>();
 			// contributors.add("Markus Binsteiner");
 			//
 			// URL picURL =
 			// getClass().getResource("/images/ARCS_LogoTag_even_smaller.jpg");
 			// ImageIcon grisu = new ImageIcon(picURL);
 			//
 			// ProjectInfo info = new ProjectInfo("Grisu", GRISU_VERSION,
 			// "The Grisu Swing client.",
 			// grisu.getImage(), "ARCS", "Apache2", apache2License);
 
 			// aboutDialog = new AboutDialog(getJFrame(), "Grisu", info);
 			aboutDialog = new GrisuAboutDialog();
 
 		}
 		return aboutDialog;
 	}
 
 	/**
 	 * This method initializes jMenuItem
 	 * 
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getAboutMenuItem() {
 		if (aboutMenuItem == null) {
 			aboutMenuItem = new JMenuItem();
 			aboutMenuItem.setText("About");
 			aboutMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					JDialog aboutDialog = getAboutDialog();
 					aboutDialog.pack();
 					Point loc = getJFrame().getLocation();
 					loc.translate(20, 20);
 					aboutDialog.setLocation(loc);
 					aboutDialog.setVisible(true);
 				}
 			});
 		}
 		return aboutMenuItem;
 	}
 
 	/**
 	 * This method initializes aboutVersionLabel
 	 * 
 	 * @return javax.swing.JLabel
 	 */
 	private JLabel getAboutVersionLabel() {
 		if (aboutVersionLabel == null) {
 			aboutVersionLabel = new JLabel();
 			aboutVersionLabel.setText(GRISU_VERSION);
 			aboutVersionLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		}
 		return aboutVersionLabel;
 	}
 
 	/**
 	 * This method initializes jMenuItem
 	 * 
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getAddLocalMenuItem() {
 		if (addLocalTemplateItem == null) {
 			addLocalTemplateItem = new JMenuItem();
 			addLocalTemplateItem
 			.setText("Add template to local template store");
 			addLocalTemplateItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					getSubmissionPanel().addLocalTemplate();
 				}
 			});
 		}
 		return addLocalTemplateItem;
 	}
 
 	/**
 	 * This method initializes jMenuItem
 	 * 
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getExitMenuItem() {
 		if (exitMenuItem == null) {
 			exitMenuItem = new JMenuItem();
 			exitMenuItem.setText("Exit");
 			exitMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					exit();
 				}
 			});
 		}
 		return exitMenuItem;
 	}
 
 	/**
 	 * This method initializes jMenu
 	 * 
 	 * @return javax.swing.JMenu
 	 */
 	private JMenu getFileMenu() {
 		if (fileMenu == null) {
 			fileMenu = new JMenu();
 			fileMenu.setText("File");
 			fileMenu.add(getAddLocalMenuItem());
 			fileMenu.add(getExitMenuItem());
 		}
 		return fileMenu;
 	}
 
 	private FileTransferPanel getFileTransferPanel() {
 		if (fileTransferPanel == null) {
 			fileTransferPanel = new FileTransferPanel();
 			fileTransferPanel.initialize(em.getFileTransferManager());
 		}
 		return fileTransferPanel;
 	}
 
 	/**
 	 * This method initializes jMenu
 	 * 
 	 * @return javax.swing.JMenu
 	 */
 	private JMenu getHelpMenu() {
 		if (helpMenu == null) {
 			helpMenu = new JMenu();
 			helpMenu.setText("Help");
 			helpMenu.add(getRequestSupportMenuItem());
 			helpMenu.add(getProxyEndTimeMenuItem());
 			helpMenu.add(getAboutMenuItem());
 		}
 		return helpMenu;
 	}
 
 	/**
 	 * This method initializes jContentPane
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJContentPane() {
 		if (jContentPane == null) {
 			jContentPane = new JPanel();
 			jContentPane.setLayout(new BorderLayout());
 			try {
 				jContentPane.add(getJTabbedPane(), BorderLayout.CENTER);
 			} catch (RuntimeException e) {
 				// something's gone wrong with something. Exiting...
 				e.printStackTrace();
 				Utils.showErrorMessage(jFrame, em.getUser(), "severeError",
 						Utils.getStackTrace(e), e);
 				System.exit(1);
 			}
 
 		}
 		return jContentPane;
 	}
 
 	/**
 	 * This method initializes jFrame
 	 * 
 	 * @return javax.swing.JFrame
 	 */
 	private JFrame getJFrame() {
 		if (jFrame == null) {
 			jFrame = new JFrame();
 			jFrame.setName("GrisuMainWindow");
 			jFrame.setSize(740, 640);
 			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			jFrame.setJMenuBar(getJJMenuBar());
 			jFrame.setContentPane(getJContentPane());
 			jFrame.setTitle("Grisu client");
 			jFrame.setLocationByPlatform(true);
 			jFrame.addWindowListener(this);
 		}
 		return jFrame;
 	}
 
 	// private JDialog getMountsDialog() {
 	// if (mountsDialog == null) {
 	// mountsDialog = new MountsDialog(em, this.getJFrame());
 	// }
 	// return mountsDialog;
 	// }
 
 	/**
 	 * This method initializes jJMenuBar
 	 * 
 	 * @return javax.swing.JMenuBar
 	 */
 	private JMenuBar getJJMenuBar() {
 		if (jJMenuBar == null) {
 			jJMenuBar = new JMenuBar();
 			jJMenuBar.add(getFileMenu());
 			jJMenuBar.add(getToolsMenu());
 			jJMenuBar.add(getHelpMenu());
 		}
 		return jJMenuBar;
 	}
 
 	private GlazedJobMonitorPanel getJobMonitorPanel() {
 		if (jobMonitorPanel == null) {
 			// jobMonitorPanel = new JobMonitorPanel(em);
 			jobMonitorPanel = new GlazedJobMonitorPanel(em);
 		}
 		return jobMonitorPanel;
 	}
 
 	/**
 	 * This method initializes jTabbedPane
 	 * 
 	 * @return javax.swing.JTabbedPane
 	 */
 	private JTabbedPane getJTabbedPane() {
 		if (jTabbedPane == null) {
 			jTabbedPane = new JTabbedPane();
 			jTabbedPane.addTab("Job submission", getSubmissionPanel());
 			jTabbedPane.addTab("Monitoring", getJobMonitorPanel());
 			// jTabbedPane.addTab("File Management", new
 			// GrisuFileCommanderPanel());
 			jTabbedPane.addTab("File management", new GrisuFilePanel(em));
 			jTabbedPane.addTab("File transfers", getFileTransferPanel());
 		}
 		return jTabbedPane;
 	}
 
 	// /**
 	// * This method initializes jobPreparationPanel
 	// *
 	// * @return javax.swing.JPanel
 	// */
 	// private JPanel getJobPreparationPanel() {
 	// if (jobPreparationPanel == null) {
 	// jobPreparationPanel = new JobPreparationPanel(new
 	// InputFile("/home/markus/workspace/nw-core/simpleTemplateJob.xml"),
 	// serviceInterface);
 	// }
 	// return jobPreparationPanel;
 	// }
 
 	// private JPanel getJobSubmissionPanel() {
 	// if (jobSubmissionPanel == null) {
 	// jobSubmissionPanel = new JobSubmissionPanel(serviceInterface);
 	// }
 	// return jobSubmissionPanel;
 	// }
 
 	private JDialog getMountPointsManagementDialog() {
 		if (mountsDialog == null) {
 			mountsDialog = new MountPointsManagementDialog();
 			mountsDialog.initialize(em);
 		}
 		return mountsDialog;
 	}
 
 	/**
 	 * This method initializes jMenuItem
 	 * 
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getMountsMenuItem() {
 		if (mountsMenuItem == null) {
 			mountsMenuItem = new JMenuItem();
 			mountsMenuItem.setText("Fileshares");
 			mountsMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					// JDialog mountsDialog = getMountsDialog();
 					getMountPointsManagementDialog().pack();
 					Point loc = getJFrame().getLocation();
 					loc.translate(20, 20);
 					getMountPointsManagementDialog().setLocation(loc);
 					getMountPointsManagementDialog().setVisible(true);
 
 				}
 			});
 		}
 		return mountsMenuItem;
 	}
 
 	private JMenuItem getProxyEndTimeMenuItem() {
 		if (proxyEndTimeMenuItem == null) {
 			proxyEndTimeMenuItem = new JMenuItem();
 			proxyEndTimeMenuItem.setText("Proxy time left");
 			proxyEndTimeMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					new Thread() {
 						@Override
 						public void run() {
 							JOptionPane.showMessageDialog(null,
 									"Proxy endtime: "
 									+ new Date(serviceInterface
 											.getCredentialEndTime()),
 											"Proxy endtime",
 											JOptionPane.INFORMATION_MESSAGE);
 						}
 					}.start();
 
 				}
 			});
 		}
 		return proxyEndTimeMenuItem;
 	}
 
 	/**
 	 * This method initializes jMenuItem
 	 * 
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getRequestSupportMenuItem() {
 		if (requestHelpMenuItem == null) {
 			requestHelpMenuItem = new JMenuItem();
 			requestHelpMenuItem.setText("Request support");
 			requestHelpMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 
 					new Runnable() {
 						public void run() {
 							HelpDesk[] helpDesks = new HelpDesk[ClientPropertiesManager
 							                                    .getDefaultHelpDesks().length];
 							Configuration config = null;
 							try {
 								try {
 									config = new PropertiesConfiguration(
 											ClientPropertiesManager
 											.getHelpDeskConfig());
 								} catch (ConfigurationException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 									throw new HelpDeskNotAvailableException(
 									"Could not init helpdesks because of misconfiguration.");
 								}
 
 								for (int i = 0; i < ClientPropertiesManager
 								.getDefaultHelpDesks().length; i++) {
 									String helpDeskClass = ClientPropertiesManager
 									.getDefaultHelpDesks()[i];
 									HelpDesk hd = HelpDeskManager
 									.createHelpDesk(helpDeskClass,
 											config);
 									if (hd != null) {
 										helpDesks[i] = hd;
 									} else {
 										throw new HelpDeskNotAvailableException(
 												"Could not create helpdesk for class: "
 												+ helpDeskClass);
 									}
 								}
 							} catch (HelpDeskNotAvailableException hdnae) {
 
 								JOptionPane.showMessageDialog(null,
 										"Could not create help desk dialog:\n"
 										+ hdnae.getLocalizedMessage(),
 										"Connection error",
 										JOptionPane.ERROR_MESSAGE);
 
 							}
 
 							TicketSubmissionDialogMultipleHelpdesks tsd = new TicketSubmissionDialogMultipleHelpdesks();
 
 							tsd.initialize(helpDesks, null,
 									"Generic help request",
 									"[Please insert your support request]",
 									null);
 
 							tsd.setVisible(true);
 
 							// Configuration config = null;
 							// try {
 							// config = new
 							// PropertiesConfiguration("support.properties");
 							// } catch (ConfigurationException e) {
 							// myLogger.error("Could not initialize irc helpdesk: "+e.getLocalizedMessage());
 							// JOptionPane.showMessageDialog(null,
 							// "Could not connect to irc help desk: "+e.getLocalizedMessage(),
 							// "Connection error",
 							// JOptionPane.ERROR_MESSAGE);
 							// }
 							//
 							//
 							// Utils.showErrorMessage(em, getJFrame(),
 							// "genericRequest", null);
 							// HelpDesk hd =
 							// HelpDeskManager.createHelpDesk("org.vpac.helpDesk.model.irc.IrcHelpDesk",
 							// config);
 							//
 							// try {
 							// hd.initiate(EnvironmentManager.getDefaultManager().getUser());
 							// hd.submitTicket("Grisu",
 							// "General Grisu support request.", new
 							// Object[]{});
 							// } catch (HelpDeskNotAvailableException e) {
 							// myLogger.error("Could not connect to irc support.");
 							// JOptionPane.showMessageDialog(null,
 							// "Could not connect to irc help desk: "+e.getLocalizedMessage(),
 							// "Connection error",
 							// JOptionPane.ERROR_MESSAGE);
 							// }
 						}
 					}.run();
 
 					// JDialog supportDialog = getRequestSupportDialog();
 					// supportDialog.pack();
 					// Point loc = getJFrame().getLocation();
 					// loc.translate(20, 20);
 					// supportDialog.setLocation(loc);
 					// supportDialog.setVisible(true);
 				}
 			});
 		}
 		return requestHelpMenuItem;
 	}
 
 	private SubmissionPanel getSubmissionPanel() {
 		if (submissionPanel == null) {
 			submissionPanel = new SubmissionPanel(em);
 			submissionPanel.setTemplateManager(em.getTemplateManager());
 
 			String application = System.getProperty("grisu.defaultApplication");
 			if ( StringUtils.isNotBlank(application) ) {
 				submissionPanel.setRemoteApplication(application);
 			}
 
 		}
 		return submissionPanel;
 	}
 
 	/**
 	 * This method initializes jMenu
 	 * 
 	 * @return javax.swing.JMenu
 	 */
 	private JMenu getToolsMenu() {
 		if (toolsMenu == null) {
 			toolsMenu = new JMenu();
 			toolsMenu.setText("Settings");
 			// toolsMenu.add(getCutMenuItem());
 			// toolsMenu.add(getCopyMenuItem());
 			// toolsMenu.add(getPasteMenuItem());
 			toolsMenu.add(getMountsMenuItem());
 		}
 		return toolsMenu;
 	}
 
 	public void windowActivated(WindowEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void windowClosed(WindowEvent e) {
 	}
 
 	public void windowClosing(WindowEvent e) {
 		exit();
 	}
 
 	public void windowDeactivated(WindowEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void windowDeiconified(WindowEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void windowIconified(WindowEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void windowOpened(WindowEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
