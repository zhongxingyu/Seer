 package ms.ui.server;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.HashMap;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.plaf.metal.MetalComboBoxUI;
 
 import ms.application.server.ServerController;
 import ms.domain.AuftragsListe;
 import ms.ui.Constants;
 import ms.ui.LogFrame;
 import ms.ui.SplashScreen;
 import ms.ui.StatusMessage;
 import ms.ui.StatusMessage.StatusType;
 import ms.ui.client.ClientConstants;
 import ms.ui.dialogs.AboutDialog;
 import ms.ui.dialogs.MessageDialog;
 import ms.ui.dialogs.server.ExportDialog;
 import ms.ui.models.TaskComboBoxModel;
 import ms.ui.tables.Table;
 import ms.utils.I18NManager;
 
 /**
  * main window of mediastopf server
  * 
  * @author david
  *
  */
 public class MainView extends JFrame {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private I18NManager manager = I18NManager.getManager();
 	private AuftragsListe taskList, runTaskList;
 	private JComboBox taskComboBox;
 	private JPanel tablePanel;
 	private Table exportTable;
 	private JTextField statusBar;
 	private JScrollPane tableScrollPane;
 	private HashMap<String, JButton> buttonMap = new HashMap<String, JButton>();
 	private HashMap<String, JPanel> panelMap = new HashMap<String, JPanel>();
 	private final String export = manager.getString("export"),
 	runningTask = manager.getString("Main.runtask"), tasks = manager.getString("Main.task"), reload = manager.getString("Main.reload"),
 	statusbar = manager.getString("Main.statusbar");
 	
 	public MainView(boolean debug) {
 		if (debug) {
 			setTitle(ServerConstants.PROGRAM + " - Debug");
 		} else {
 			new SplashScreen(Constants.SPLASH);
 		}
 
 		initGUI();
 	}
 
 	/**
 	 * init GUI Components
 	 */
 	private void initGUI() {
 		initFrame();
 
 		addTrayIcon();
 		addStatusBar();
 		addTaskPanel();
 	}
 
 	private void initFrame() {
 		setTitle(ServerConstants.PROGRAM);
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		setLayout(null);
 		setSize(600, 550);
 		setMinimumSize(new Dimension(400, 450));
 		setIconImage(new ImageIcon(getClass().getResource(Constants.UIIMAGE + Constants.ICON)).getImage());
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);
 		setJMenuBar(createMenuBar());
 		
 		componentListener();
 		windowListener();
 	}
 
 	private void windowListener() {
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				setVisible(false);
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
 		int width = getWidth() - 10;
 		int height = getHeight() - 70;
 		
 		JPanel runtaskPanel = panelMap.get(runningTask);
 		runtaskPanel.setSize(width, height - 110);
 		JPanel taskPanel = panelMap.get(tasks);
 		taskPanel.setSize(width, taskPanel.getHeight());
 		JPanel statusPanel = panelMap.get(statusbar);
 		statusPanel.setBounds(0, height, width, statusPanel.getHeight());
 		
 		updateComponentBounds(runtaskPanel, taskPanel, statusPanel);
 	}
 	
 	private void updateComponentBounds(JPanel runtask, JPanel task, JPanel status) {
 		buttonMap.get(export).setLocation(task.getWidth() - 140,task.getHeight() - 40);
 		
 		tablePanel.setSize(runtask.getWidth() - 10, runtask.getHeight() - 20);
 		
 		taskComboBox.setSize(task.getWidth() - 20, 20);
 
 		tableScrollPane.setSize(tablePanel.getWidth(), tablePanel.getHeight());
 		tableScrollPane.revalidate();
 		
 		statusBar.setSize(status.getWidth(), status.getHeight());
 	}
 	
 	private void addTrayIcon() {
 		new SystemTrayIcon(this);
 	}
 	
 	private void addStatusBar() {
 		JPanel panel = new JPanel();
 		panel.setLayout(null);
 		panel.setBounds(0, getHeight() - 70, getWidth() - 10, 20);
 		panelMap.put(statusbar, panel);
 		
 		statusBar = new JTextField(manager.getString("Main.copyright"));
 		statusBar.setBounds(0, 0, panel.getWidth(), panel.getHeight());
 		statusBar.setEditable(false);
 		statusBar.setFocusable(false);
 		
 		panel.add(statusBar);
 		add(panel);
 	}
 
 	/**
 	 * add task panel
 	 */
 	private void addTaskPanel() {
 		addTaskComboBox();
 		addTaskTable();
 		
 		final int[] y = { 5, 100 };
 		final int[] height = { 90, getHeight() - 180 };
 		final String[] panelLabel = { tasks, runningTask };
 		final JComponent[] comp = { taskComboBox, tablePanel };
 		for(int i=0; i<panelLabel.length; i++) {
 			JPanel panel = new JPanel();
 			panel.setLayout(null);
 			panel.setBounds(0, y[i], getWidth() - 10, height[i]);
 			panel.setBorder(BorderFactory.createTitledBorder(panelLabel[i]));
 			panel.add(comp[i]);
 			add(panel);
 			panelMap.put(panelLabel[i], panel);
 		}
 		
 		addButtons();
 		panelMap.get(tasks).add(buttonMap.get(export));
 	}
 	
 	/**
 	 * combobox which show the tasks available
 	 * 
 	 * @return JComboBox
 	 */
 	private void addTaskComboBox() {
 		taskList = new AuftragsListe(ServerController.class);
 		taskComboBox = new JComboBox(new TaskComboBoxModel(taskList));
 		taskComboBox.setBounds(10, 20, getWidth() - 30, 20);
 		if(0<taskComboBox.getItemCount())
 			taskComboBox.setSelectedIndex(0);
 		taskComboBox.setUI(new MetalComboBoxUI() {
 			public void layoutComboBox(Container parent, MetalComboBoxLayoutManager manager) {
 				super.layoutComboBox(parent, manager);
 				arrowButton.setBounds(0, 0, 0, 0);
 			}
 		});
 	}
 
 	/**
 	 * add buttons
 	 * 
 	 * @return JButton
 	 */
 	private void addButtons() {
 		JPanel panel = panelMap.get(tasks);
 		int x = panel.getWidth() - 260;
 		int y = panel.getHeight() - 40;
 		int width = 115;
 		int height = 25;
 		final String[] buttonText = { reload, export };
 		final String[] icons = { ClientConstants.RELOAD, ClientConstants.EXPORT_S };
 		final Rectangle reloadBounds = new Rectangle(x, y, width, height);
 		final Rectangle exportBounds = new Rectangle(x + width + 10, y, width, height);
 		final Rectangle[] bounds = { reloadBounds, exportBounds };
 		final int reloadMnemonic = KeyEvent.VK_F5;
 		final int exportMnemonic = manager.getMnemonic("export");
 		final int[] mnemonic = { reloadMnemonic, exportMnemonic };
 		for (int i = 0; i < buttonText.length; i++) {
 			JButton button = new JButton();
 			button.setBounds(bounds[i]);
 			button.setText(buttonText[i]);
 			button.setMnemonic(mnemonic[i]);
 			button.setIcon(new ImageIcon(getClass().getResource(ClientConstants.UIIMAGE + icons[i])));
 		    button.setVerticalTextPosition(JButton.CENTER);
 		    button.setHorizontalTextPosition(JButton.RIGHT);
 			button.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if (e.getActionCommand() == reload) {
 						taskList.updateList();
 						updateStatusBar(StatusType.RELOADMESSAGE);
 					} else if (e.getActionCommand() == export) {
 						exportSelectedItem();
 					}
 				}
 			});
 			panel.add(button);
 			buttonMap.put(buttonText[i], button);
 		}
 	}
 	
 	private void exportSelectedItem() {
 		int taskID = (Integer)taskComboBox.getSelectedItem();
 		if(taskID == -1) {
 			MessageDialog.noneSelectedDialog();
 			return;
 		}
 		File file = new File(Integer.toString(taskID));
 		if(!file.isDirectory()) {
 			MessageDialog.info(manager.getString("Main.dirnotfoundtitle"), manager.getString("Main.dirnotfoundmessage") + taskID);
 			return;
 		}
 		ExportDialog ed = new ExportDialog(taskID);
 		ed.setVisible(true);
 	}
 
 	/**
 	 * add task table
 	 * 
 	 * @return JPanel
 	 */
 	private void addTaskTable() {
 		tablePanel = new JPanel();
 		tablePanel.setBounds(5, 15, getWidth() - 20, getHeight() - 200);
 		tablePanel.setLayout(null);
 		
 		runTaskList = new AuftragsListe(ServerController.class);
 		exportTable = new Table(runTaskList);
 		tableScrollPane = new JScrollPane(exportTable);
 		tableScrollPane.setBounds(0, 0, tablePanel.getWidth(), tablePanel.getHeight());
 		tablePanel.add(tableScrollPane);
 	}
 	
 	private void updateStatusBar(StatusType type) {
 		statusBar.setForeground(Color.BLACK);
 		statusBar.setText(StatusMessage.getMessage(type));
 		Thread t = new Thread(new Runnable() {
 			public void run() {
 				try {
 					Thread.sleep(4000);
 					statusBar.setForeground(Color.GRAY);
 					statusBar.setText(manager.getString("StatusMessage.copyright"));
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		t.start();
 	}
 
 	void exit() {
 		int result = MessageDialog.yesNoDialog(manager.getString("Main.exittitle"), manager.getString("Main.exitmessage"));
 		switch(result) {
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
 		JMenuItem aboutItem = new JMenuItem(manager.getString("Main.aboutitem"));
 		aboutItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
 		aboutItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				AboutDialog about = new AboutDialog(ServerConstants.class);
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
 		final String log = manager.getString("Main.logitem"), exit = manager.getString("exit");
 		final String[] fileTitles = { log, exit };
 		final KeyStroke logAccelerator = KeyStroke.getKeyStroke(manager.getMnemonic("Main.logitem"), KeyEvent.CTRL_DOWN_MASK);
 		final KeyStroke exitAccelerator = null;
 		final KeyStroke[] keyStrokes = { logAccelerator, exitAccelerator };
 		for (int i = 0; i < fileTitles.length; i++) {
 			if(i == 1) {
 				fileMenu.addSeparator();
 			}
 			JMenuItem fileItem = new JMenuItem();
 			fileItem.setText(fileTitles[i]);
 			fileItem.setAccelerator(keyStrokes[i]);
 			fileItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if (e.getActionCommand() == log) {
 						LogFrame ld = new LogFrame(ServerConstants.class);
 						ld.setVisible(true);
 					} else if (e.getActionCommand() == exit) {
 						exit();
 					}
 				}
 			});
 			fileMenu.add(fileItem);
 		}
 	}
 }
