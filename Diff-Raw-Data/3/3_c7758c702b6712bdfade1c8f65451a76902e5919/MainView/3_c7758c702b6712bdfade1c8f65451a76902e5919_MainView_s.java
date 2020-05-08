 package ms.ui.client;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 
 import ms.application.client.ClientController;
 import ms.domain.Auftrag;
 import ms.domain.AuftragsListe;
 import ms.domain.LaufendeAuftragsListe;
 import ms.ui.LogFrame;
 import ms.ui.SplashScreen;
 import ms.ui.StatusMessage;
 import ms.ui.StatusMessage.StatusType;
 import ms.ui.dialogs.AboutDialog;
 import ms.ui.dialogs.MessageDialog;
 import ms.ui.dialogs.client.ConfigDialog;
 import ms.ui.models.TaskComboBoxModel;
 import ms.ui.tables.client.TaskTable;
 import ms.utils.ConfigHandler;
 import ms.utils.I18NManager;
 import ms.utils.log.client.ClientLog;
 import ms.utils.ui.Button;
 import ms.utils.ui.ComboBox;
 import ms.utils.ui.Frame;
 import ms.utils.ui.MenuItem;
 import ms.utils.ui.Panel;
 import ms.utils.ui.ScrollPane;
 import ms.utils.ui.TextField;
 
 /**
  * main window of mediastopf
  */
 public class MainView extends Frame {
 
 	private static final long serialVersionUID = 1L;
 	private I18NManager manager = I18NManager.getManager();
 	private ConfigHandler config = ConfigHandler.getClientHandler();
 	private AuftragsListe taskList;
 	private LaufendeAuftragsListe sendTaskList;
 	private JComboBox taskComboBox;
 	private JScrollPane tableScrollPane;
 	private JPanel tablePanel;
 	private JTextField statusBarField;
 	private TaskTable taskTable;
 	private HashMap<String, JButton> buttonMap = new HashMap<String, JButton>();
 	private HashMap<String, JPanel> panelMap = new HashMap<String, JPanel>();
 	private String run = manager.getString("Main.run"), reload = manager.getString("Main.reload"),
 	send = manager.getString("send"), runningTask = manager.getString("Main.runtask"), tasks = manager.getString("Main.task"), statusbar = manager.getString("Main.statusbar");
 	private ClientController clientcontroller;
 
 	public MainView() {
 		new SplashScreen(ClientConstants.SPLASH);
 		clientcontroller = ClientController.getClientController();
 
 		initGUI();
 	}
 
 	/**
 	 * init GUI Components
 	 */
 	private void initGUI() {
 		initFrame();
 
 		addStatusBar();
 		addTaskPanel();
 		addRunningTaskPanel();
 		
 		Iterator<JPanel> it = panelMap.values().iterator();
 		while (it.hasNext()) {
 			add((JPanel) it.next());
 		}
 		setVisible(true);
 	}
 
 	private void initFrame() {
 		super.initFrame(ClientConstants.PROGRAM, getClass().getResource(ClientConstants.UIIMAGE + ClientConstants.ICON), new Dimension(600, 550), JFrame.DO_NOTHING_ON_CLOSE);
 		setMinimumSize(new Dimension(400, 450));
 		setJMenuBar(createMenuBar());
 		
 		componentListener();
 		windowListener();
 	}
 
 	private void windowListener() {
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				exit();
 			}
 		});
 	}
 
 	private void componentListener() {
 		addComponentListener(new ComponentAdapter() {
 			private boolean isShown = false;
 			@Override
 			public void componentResized(ComponentEvent e) {
 				if (isShown) {
 					updatePanelBounds();
 				}
 			}
 			@Override
 			public void componentShown(ComponentEvent e) {
 				isShown = true;
 			}
 		});
 	}
 	
 	private void updatePanelBounds() {
 		int width = getWidth();
 		int height = getHeight();
 
 		JPanel runtaskPanel = panelMap.get(runningTask);
 		runtaskPanel.setSize(width - 10, height - 180);
 		JPanel taskPanel = panelMap.get(tasks);
 		taskPanel.setSize(width - 10, taskPanel.getHeight());
 		JPanel statusPanel = panelMap.get(statusbar);
 		statusPanel.setBounds(0, height - 70, width - 10, statusPanel.getHeight());
 		
 		updateComponentBounds(runtaskPanel, taskPanel, statusPanel);
 	}
 
 	private void updateComponentBounds(JPanel runtaskPanel, JPanel taskPanel, JPanel statusPanel) {
 		buttonMap.get(reload).setLocation(taskPanel.getWidth() - 260, taskPanel.getHeight() - 40);
 		buttonMap.get(run).setLocation(taskPanel.getWidth() - 135, taskPanel.getHeight() - 40);
 
 		taskComboBox.setSize(taskPanel.getWidth() - 25, 20);
 		statusBarField.setSize(statusPanel.getWidth(), statusPanel.getHeight());
 
 		int width = runtaskPanel.getWidth() - 10;
 		int height = runtaskPanel.getHeight() - 70;
 		tablePanel.setSize(width, height);
 		tableScrollPane.setSize(tablePanel.getWidth(), tablePanel.getHeight());
 		tableScrollPane.revalidate();
 
 		buttonMap.get(send).setLocation(width - 250, height + 30);
 	}
 
 	private void addStatusBar() {
 		JPanel panel = new Panel(new Rectangle(0, getHeight() - 70, getWidth() - 10, 20));
 		panelMap.put(statusbar, panel);
 
 		statusBarField = new TextField(manager.getString("StatusMessage.copyright"), new Rectangle(0, 0, panel.getWidth(), panel.getHeight()));
 		statusBarField.setFocusable(false);
 		statusBarField.setEditable(false);
 		panel.add(statusBarField);
 	}
 
 	/**
 	 * add task panel
 	 */
 	private void addTaskPanel() {
 		addTaskComboBox();	
 		
 		JPanel panel = new Panel(new Rectangle(0, 5, getWidth() - 10, 90), BorderFactory.createTitledBorder(tasks));
 		panel.add(taskComboBox);
 		panelMap.put(tasks, panel);
 		addTaskButtons(panel);
 	}
 
 	/**
 	 * combobox which show the tasks available
 	 * 
 	 * @return JComboBox
 	 */
 	private void addTaskComboBox() {
 		taskList = clientcontroller.auftragliste;
 		taskComboBox = new ComboBox(new TaskComboBoxModel(taskList), new Rectangle(10, 20, getWidth() - 30, 20));
 		if (0 < taskComboBox.getItemCount())
 			taskComboBox.setSelectedIndex(0);
 	}
 	
 	private void addTaskButtons(JPanel panel) {
 		int x = panel.getWidth() - 260;
 		int y = panel.getHeight() - 40;
 		int width = 115;
 		int height = 25;
 		final String[] buttonText = { reload, run };
 		final URL[] icons = { getClass().getResource(ClientConstants.UIIMAGE + ClientConstants.RELOAD), getClass().getResource(ClientConstants.UIIMAGE + ClientConstants.TICK) };
 		final Rectangle reloadBounds = new Rectangle(x, y, width, height);
 		final Rectangle runBounds = new Rectangle(x + width + 10, y, width, height);
 		final Rectangle[] bounds = { reloadBounds, runBounds };
 		final int reloadMnemonic = KeyEvent.VK_F5;
 		final int runMnemonic = manager.getMnemonic("Main.run");
 		final int[] mnemonic = { reloadMnemonic, runMnemonic };
 		for (int i = 0; i < buttonText.length; i++) {
 			JButton button = new Button(bounds[i], buttonText[i], mnemonic[i], icons[i]);
 			button.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if (e.getActionCommand() == reload) {
 						taskList.updateList();
 						updateStatusBar(StatusType.RELOADMESSAGE);
 					} else if (e.getActionCommand() == run) {
 						processAuftrag();
 					}
 				}
 			});
 			panel.add(button);
 			buttonMap.put(buttonText[i], button);
 		}
 	}
 
 	private void processAuftrag() {
 		int taskID = (Integer) taskComboBox.getSelectedItem();
 		if(taskID < 0) {
 			MessageDialog.noneSelectedDialog();
 			return;
 		}
 		
 		String folder = getValueOf(ClientConstants.DEFAULTFOLDERCFG);
 		if(!new File(folder).exists()) {
 			pathNotSet(manager.getString("Main.choosedefaultfoldertitle"), manager.getString("Main.choosedefaultfolder"));
 			return;
 		}
 		File taskFolder = new File(folder + File.separator + taskID);
 		taskFolder.mkdirs();
 		
 		startApplication();
 		ClientController.getClientController().pollDirForAuftrag(taskFolder, taskID);
 		updateStatusBar(StatusType.RUNMESSAGE);

		taskList.removebyId(taskID);
 		Auftrag task = new Auftrag(Integer.valueOf(taskID), 2);
 		sendTaskList.add(task);
 		checkDirStatus(taskFolder, task.getID());
 	}
 
 	private void startApplication() {
 		String ripper = getValueOf(ClientConstants.AUDIORIPPERCFG);
 		if(!new File(ripper).exists()) {
 			pathNotSet(manager.getString("Main.chooseaudiorippertitle"), manager.getString("Main.chooseaudioripper"));
 			return;
 		}
 		clientcontroller.openApplication(ripper);
 	}
 
 	private void checkDirStatus(final File taskFolder, final int  taskID) {
 		Thread t = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Thread.sleep(30000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				while(true) {
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 					if(taskFolder.listFiles().length <= 0) {
 						sendTaskList.removebyId(taskID);
 						break;
 					}
 				}
 			}
 		});
 		t.start();
 	}
 
 	private void pathNotSet(String title, String message) {
 		MessageDialog.info(title, message + manager.getString("Config.defaultfolder"));
 		openConfigDialog();
 	}
 
 	private String getValueOf(String key) {
 		if(config.containsKey(key)) {
 			return config.getProperty(key).trim();
 		}
 		return "";
 	}
 	
 	private void updateStatusBar(StatusType type) {
 		statusBarField.setForeground(Color.BLACK);
 		statusBarField.setText(StatusMessage.getMessage(type));
 		Thread t = new Thread(new Runnable() {
 			public void run() {
 				try {
 					Thread.sleep(4000);
 					statusBarField.setForeground(Color.GRAY);
 					statusBarField.setText(manager.getString("StatusMessage.copyright"));
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		t.start();
 	}
 
 	/**
 	 * add running task panel
 	 */
 	private void addRunningTaskPanel() {
 		addTaskTable();
 		
 		JPanel panel = new Panel(new Rectangle(0, 100, getWidth() - 10, getHeight() - 180), BorderFactory.createTitledBorder(runningTask));
 		panel.add(tablePanel);
 		panelMap.put(runningTask, panel);
 		
 		addRunningTaskButtons(panel);
 	}
 
 	/**
 	 * add task table
 	 * 
 	 * @return JPanel
 	 */
 	private void addTaskTable() {
 		tablePanel = new Panel(new Rectangle(5, 15, getWidth() - 20, getHeight() - 250));
 		sendTaskList = new LaufendeAuftragsListe();
 		taskTable = new TaskTable(sendTaskList);
 		tableScrollPane = new ScrollPane(taskTable, new Rectangle(0, 0, tablePanel.getWidth(), tablePanel.getHeight()));
 		tablePanel.add(tableScrollPane);
 	}
 
 	/**
 	 * add button for the running tasks
 	 * 
 	 * @param panel
 	 *            JPanel
 	 */
 	private void addRunningTaskButtons(JPanel panel) {
 		Rectangle bounds = new Rectangle(panel.getWidth() - 135, panel.getHeight() - 40, 115, 25);
 		JButton button = new Button(bounds, send, manager.getMnemonic("send"), getClass().getResource(ClientConstants.UIIMAGE + ClientConstants.SEND));
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int idtoremove = taskTable.send();
 				if(0 <= idtoremove && idtoremove < sendTaskList.size() )
 					sendTaskList.remove(idtoremove);
 				updateStatusBar(StatusType.SENDMESSAGE);
 			}
 		});
 		panel.add(button);
 		buttonMap.put(send, button);
 	}
 
 	private void exit() {
 		int result = MessageDialog.yesNoDialog(manager.getString("Main.exittitle"),
 				manager.getString("Main.exitmessage"));
 		try {
 			clientcontroller.mediumsender.disconnect();
 			clientcontroller.auftragreceiver.disconnect();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		switch (result) {
 		case JOptionPane.YES_OPTION:
 			System.exit(0);
 			break;
 		case JOptionPane.NO_OPTION:
 			return;
 		}
 	}
 
 	/**
 	 * MenuBar
 	 * 
 	 * @return JMenuBar
 	 */
 	private JMenuBar createMenuBar() {
 		JMenuBar menuBar = new JMenuBar();
 		final String file = manager.getString("Main.filemenu"), help = manager.getString("Main.helpmenu");
 		final String[] menuItems = { file, help };
 		final int fileMnemonic = manager.getMnemonic("Main.filemenu"), helpMnemonic = manager.getMnemonic("Main.helpmenu");
 		final int[] keyEvent = new int[] { fileMnemonic, helpMnemonic };
 		for (int i = 0; i < menuItems.length; i++) {
 			JMenu menu = new JMenu(menuItems[i]);
 			menu.setMnemonic(keyEvent[i]);
 			if (menuItems[i] == file) {
 				addFileItems(menu);
 			} else {
 				addHelpItems(menu);
 			}
 			menuBar.add(menu);
 		}
 		return menuBar;
 	}
 
 	/**
 	 * help menu items
 	 * 
 	 * @param helpMenu
 	 */
 	private void addHelpItems(JMenu helpMenu) {
 		JMenuItem aboutItem = new MenuItem(manager.getString("Main.aboutitem"), KeyStroke.getKeyStroke("F1"));
 		aboutItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				AboutDialog about = new AboutDialog(ClientConstants.class);
 				about.setVisible(true);
 			}
 		});
 		helpMenu.add(aboutItem);
 	}
 
 	/**
 	 * filemenu items
 	 * 
 	 * @param fileMenu
 	 *            JMenu
 	 */
 	private void addFileItems(JMenu fileMenu) {
 		final String config = manager.getString("Main.configitem"), log = manager.getString("Main.logitem"), exit = manager.getString("exit");
 		final String[] fileTitles = { config, log, exit };
 		final KeyStroke configAccelerator = KeyStroke.getKeyStroke(manager.getMnemonic("Main.configitem"), KeyEvent.CTRL_DOWN_MASK);
 		final KeyStroke logAccelerator = KeyStroke.getKeyStroke(manager.getMnemonic("Main.logitem"), KeyEvent.CTRL_DOWN_MASK);
 		final KeyStroke exitAccelerator = null;
 		final KeyStroke[] keyStrokes = { configAccelerator, logAccelerator, exitAccelerator };
 		for (int i = 0; i < fileTitles.length; i++) {
 			if (i == 2) {
 				fileMenu.addSeparator();
 			}
 			JMenuItem fileItem = new MenuItem(fileTitles[i], keyStrokes[i]);
 			fileItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if (e.getActionCommand() == config) {
 						openConfigDialog();
 					} else if (e.getActionCommand() == log) {
 						openLogFrame();
 					} else if (e.getActionCommand() == exit) {
 						exit();
 					}
 				}
 			});
 			fileMenu.add(fileItem);
 		}
 	}
 
 	private void openConfigDialog() {
 		ConfigDialog cd = new ConfigDialog();
 		cd.setVisible(true);
 	}
 
 	private void openLogFrame() {
 		Thread t = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				LogFrame ld = new LogFrame(ClientConstants.class);
 				ld.setVisible(true);
 				ClientLog.log.addObserver(ld);
 			}
 		});
 		t.start();
 	}
 }
