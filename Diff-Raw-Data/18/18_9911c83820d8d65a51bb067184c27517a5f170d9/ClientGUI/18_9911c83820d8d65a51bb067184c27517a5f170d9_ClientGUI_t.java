 package bt.View;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.List;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.table.DefaultTableModel;
 import bt.Exceptions.UnknownBittorrentException;
 import bt.GUIComponents.FileSelectionDialog;
 import bt.Model.Bittorrent;
 import bt.Model.Peer;
 import bt.Utils.Utilities;
 
 /**
  * Client's graphic user interface.
  * @author Isaac Yochelson, Robert Schomburg and Fernando Geraci.
  *
  */
 
 @SuppressWarnings("serial")
 public class ClientGUI extends JFrame {
 	
 	/**
 	 * Static singleton instance.
 	 */
 	public static ClientGUI instance = null;
 	
 	GridBagLayout gb = new GridBagLayout();
 	GridBagConstraints gc;
 	Bittorrent client;
 	private Container container;
 	private JPanel dataPanel;
 	private JPanel centralPanel;
 	private JMenuBar menuBar;
 	private JMenu fileOptions;
 	private JMenu helpOptions;
 	private JMenuItem exitMenu;
 	private JMenuItem saveLogToFile;
 	private JMenuItem about;
 	
 	private JPanel mainContainer;
 	
 	// data panel members
 	private JLabel labelUserIDTitle = new JLabel(" My Peer ID ");
 	private JLabel labeltorrentFileTitle = new JLabel(" Torrent File ");
 	private JLabel labelTorrentFileSizeTitle = new JLabel(" File Size ");
 	private JLabel labelUserID = new JLabel(" ");
 	private JLabel labelTorrentFileName = new JLabel(" ");
 	private JLabel labelTorrentFileSize = new JLabel(" ");
 	private JLabel labelClientEventTitle = new JLabel(" Current Event ");
 	private JLabel labelClientEvent = new JLabel(" ");
 	private JLabel labelConnectedPeers = new JLabel("Connected Peers");
 	private JPanel panelListContainer;
 	private DefaultListModel<String> peerListModel;
 	
 	// central panel components
 	private JSplitPane centerPanel;
 	private JScrollPane panelListHolder;
 	private JScrollPane panelLogHolder;
 	private JList<String> listPeers;
 	private JTextArea textFieldLog;
 	
 	//bottom console
 	private JPanel bottomContainer;
 	private JScrollPane bottomPanel;
 	private JTable tableConnectios;
 	private DefaultTableModel tableModel;
 	
 	/**
 	 * Retrieves a single instance of ClientGUI
 	 * @return ClientGUI
 	 */
 	public static ClientGUI getInstance() {
 		if(ClientGUI.instance == null) {
 			ClientGUI.instance = new ClientGUI();
 		}
 		return ClientGUI.instance;
 	}
 	
 	/**
 	 * Constructs a ClientGUI object.
 	 */
 	private ClientGUI() {
 		try {
 			// windows size
 			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 			int xPanel = (int)(dim.getWidth()*.8); // 80% of total width
 			int yPanel = (int)(dim.getHeight()*.6); // 90 % of total height
 			this.setMinimumSize(new Dimension((int)(xPanel*.8), (int)(yPanel*.8)));
 			this.container = this.getContentPane();
 			// this.container.setLayout(this.gb);
 			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			
 			// initialize all layout components
 			this.initLayout();
 			this.pack();
 			// initialize components' decorations.
 			this.initDecorations();
 			
 			// initialize generic window behaviors
 			this.addWindowListener(new WindowAdapter() {
 				public void windowClosing(WindowEvent we) {
 					// Utilities.callClose();
 				}
 			});
 		} catch(Exception e) {
 			System.out.println(" << GUI initialization error >> ");
 		}
 		
 	}
 	
 	/**
 	 * Fires GUI up.
 	 */
 	public void startGUI() {
 		this.loadTorrentFile();
 		this.updatePeerModel();
		this.updateDataPanel();
 		this.initBehaviors();
 		this.setLocationRelativeTo(null);
 		this.setVisible(true);
 		try {
			this.publishEvent("Initializing client...");
			Thread.sleep(1000);
 			while(Bittorrent.getInstance() == null) {
 				this.publishEvent("Initializing client...");
 				Thread.sleep(1000);
 			}
 			Bittorrent.getInstance().startExecuting();
 		} catch (UnknownBittorrentException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Created a dialog for prompting for torrent file.
 	 * @return
 	 */
 	private void loadTorrentFile() {
 		// calls a custom file selection dialog before starting main window.
 		new FileSelectionDialog(this,true); 
 	}
 	
 	/**
 	 * Initializes GUI layout.
 	 */
 	private void initLayout() {
 		
 		this.container.setLayout(new BorderLayout());
 		this.mainContainer = new JPanel(this.gb);
 		
 		// menu bar
 		this.initMenuBar();
 		
 		this.container.add(this.menuBar, BorderLayout.NORTH);
 		// data panel
 		this.initDataPanel();
 		this.gc = new GridBagConstraints();
 		this.gc.insets = new Insets(2, 5, 2, 5);
 		this.gc.fill = GridBagConstraints.HORIZONTAL;
 		this.gc.gridwidth = GridBagConstraints.REMAINDER;
 		this.gc.weightx = 1;
 		this.mainContainer.add(this.dataPanel, this.gc);
 		// central panel
 		this.initCentralPanel();
 		this.gc = new GridBagConstraints();
 		this.gc.insets = new Insets(2, 5, 2, 5);
 		this.gc.weighty = .4;
 		this.gc.fill = GridBagConstraints.BOTH;
 		this.gc.gridwidth = GridBagConstraints.REMAINDER;
 		this.mainContainer.add(this.centerPanel, this.gc);
 		//bottom section
 		this.initBottomPanel();
 		this.gc = new GridBagConstraints();
 		this.gc.insets = new Insets(2, 5, 2, 5);
 		this.gc.fill = GridBagConstraints.BOTH;
 		this.gc.weighty = .1;
 		this.gc.weightx = 1;
 		this.mainContainer.add(this.bottomPanel, this.gc);
 		
 		this.container.add(this.mainContainer);
 		
 	}
 	
 	private void initBottomPanel() {
 		Object[] tableColumns = {"Connected to...", "State", "Downloaded", "Uploaded"};
 		this.tableConnectios = new JTable();
 		this.tableConnectios.setDefaultRenderer(String.class, new bt.GUIComponents.TableCellRenderer());
 		this.tableModel = new DefaultTableModel();
 		for(int i = 0 ; i < tableColumns.length; ++i) {
 			this.tableModel.addColumn(tableColumns[i]);
 		}
 		this.tableConnectios.setModel(this.tableModel);
 		this.tableConnectios.setFillsViewportHeight(true);
 		this.bottomPanel = new JScrollPane(this.tableConnectios);
 	}
 	
 	/**
 	 * Initializes the menu bar.
 	 */
 	private void initMenuBar() {
 		this.menuBar = new JMenuBar();
 		this.fileOptions = new JMenu("File");
 		this.helpOptions = new JMenu("Help");
 		this.saveLogToFile = new JMenuItem("Save Log To File");
 		this.exitMenu = new JMenuItem("Exit");
 		this.about = new JMenuItem("About...");
 		this.fileOptions.add(this.saveLogToFile);
 		this.fileOptions.add(this.exitMenu);
 		this.helpOptions.add(this.about);
 		this.menuBar.add(fileOptions);
 		this.menuBar.add(helpOptions);
 	}
 	
 	/**
 	 * Initializes main body
 	 */
 	private void initCentralPanel() {
 		this.panelListContainer = new JPanel(new GridBagLayout());
 		this.listPeers = new JList<String>();
 		this.panelListHolder = new JScrollPane(this.listPeers);
 		this.panelListHolder.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		GridBagConstraints gc = new GridBagConstraints();
 		gc.fill = GridBagConstraints.HORIZONTAL;
 		gc.gridwidth = GridBagConstraints.REMAINDER;
 		this.panelListContainer.add(this.labelConnectedPeers, gc);
 		gc.fill = GridBagConstraints.BOTH;
 		gc.weighty = 1;
 		gc.weightx = 1;
 		this.panelListContainer.add(this.panelListHolder, gc);
 		this.textFieldLog = new JTextArea();
 		this.textFieldLog.setEditable(false);
 		this.panelLogHolder = new JScrollPane(this.textFieldLog);
 		this.panelLogHolder.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		this.centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.panelListContainer, this.panelLogHolder);
 		this.centerPanel.setOneTouchExpandable(true);
 		this.centerPanel.setDividerLocation(175);
 		this.centerPanel.setDividerSize(5);
 	}
 	
 	/**
 	 * Logs and event in the log area.
 	 * @param message
 	 */
 	public void publishEvent(String message) {
 		int currentCaret = this.textFieldLog.getCaretPosition();
 		StringBuilder stringBuilder = new StringBuilder();
 		stringBuilder.append("\n");
 		stringBuilder.append(message);
 		this.textFieldLog.append(stringBuilder.toString());
 		int caretPosition = currentCaret + (stringBuilder.length());
 		this.textFieldLog.setCaretPosition(caretPosition);
 	}
 	
 	/**
 	 * Local panel initializator.
 	 */
 	private void initDataPanel() {
 		this.dataPanel = new JPanel();
 		this.dataPanel.setLayout(new GridBagLayout());
 		GridBagConstraints gcL = new GridBagConstraints();
 		gcL.insets = new Insets(2,5,5,5);
 		gcL.fill = GridBagConstraints.HORIZONTAL;
 		gcL.weightx = 1;
 		this.dataPanel.add(this.labelUserIDTitle);
 		gcL.gridwidth = GridBagConstraints.REMAINDER;
 		this.dataPanel.add(this.labelUserID, gcL);
 		gcL = new GridBagConstraints();
 		gcL.insets = new Insets(2,5,5,5);
 		this.dataPanel.add(this.labeltorrentFileTitle);
 		gcL.gridwidth = GridBagConstraints.REMAINDER;
 		gcL.fill = GridBagConstraints.HORIZONTAL;
 		gcL.weightx = 1;
 		this.dataPanel.add(this.labelTorrentFileName, gcL);
 		gcL = new GridBagConstraints();
 		gcL.insets = new Insets(2,5,5,5);
 		this.dataPanel.add(this.labelTorrentFileSizeTitle);
 		gcL.gridwidth = GridBagConstraints.REMAINDER;
 		gcL.fill = GridBagConstraints.HORIZONTAL;
 		gcL.weightx = 1;
 		this.dataPanel.add(this.labelTorrentFileSize, gcL);
 		gcL = new GridBagConstraints();
 		gcL.insets = new Insets(2,5,5,5);
 		this.dataPanel.add(this.labelClientEventTitle);
 		gcL.gridwidth = GridBagConstraints.REMAINDER;
 		gcL.fill = GridBagConstraints.HORIZONTAL;
 		gcL.weightx = 1;
 		this.dataPanel.add(this.labelClientEvent, gcL);
 	}
 	
 	/**
 	 * Initializes data panel components.
 	 */
 	private void updateDataPanel() {
 		try {
 			if(Bittorrent.getInstance() != null) {
 				Bittorrent bt = Bittorrent.getInstance();
 				this.labelUserID.setText(" "+bt.getPeerId());
 				this.labelTorrentFileName.setText(" "+bt.getFileName());
 				this.labelTorrentFileSize.setText(" "+bt.getFileLength()+" bytes");
 				this.labelClientEvent.setText(" "+bt.getEvent().toUpperCase());
 			}
 		} catch (Exception e) {}
 	}
 	
 	/**
 	 * Updates the status of the current connections.
 	 */
 	public synchronized void updateTableModel() {
 		try {
 			int rows = this.tableModel.getRowCount();
 			for(int i = 0; i < rows; ++i) {
 				this.tableModel.removeRow(i);
 			}
 			List<Peer> currentList = Bittorrent.getInstance().getPeerList();
 			for(Peer p : currentList) {
 				Peer temp = p;
 				Object[] columns = new String[4];
 				columns[0] = p.toString();
 				columns[1] = p.isChoked()+"";
 				columns[2] = p.getDownloaded()+"";
 				columns[3] = p.getUploaded()+"";
 				this.tableModel.addRow(columns);
 			}
 		} catch(Exception e) {
 			System.out.println("Error updating table model.");
 		}
 		
 	}
 	
 	/**
 	 * Updates the peers list. It will also be called by the refresher every 180 seconds.
 	 */
 	public void updatePeerModel() {
 		if(this.peerListModel == null) { // initialize
 			this.peerListModel = new DefaultListModel<String>();
 		} else { // update
 			this.peerListModel.clear();
 		}
 		try {
 			String[] peers = Bittorrent.getInstance().getPeersArray();
 			for(int i = 0; i < peers.length; ++i) {
 				this.peerListModel.add(i, peers[i]);
 			}
 			this.listPeers.setModel(this.peerListModel);
 		} catch (Exception e) { System.out.println(e.getMessage()); }
 	}
 	
 	
 	/**
 	 * Initializes frames and panels decorations.
 	 */
 	private void initDecorations() {
 		this.dataPanel.setBorder(BorderFactory.createTitledBorder("Torrent & Tracker Data"));
 		this.labelTorrentFileSizeTitle.setHorizontalAlignment(JLabel.LEFT);
 		this.labelUserID.setBorder(BorderFactory.createLoweredBevelBorder());
 		this.labelUserID.setFont(new Font("Courrier", Font.ITALIC, 14));
 		this.labelTorrentFileName.setBorder(BorderFactory.createLoweredBevelBorder());
 		this.labelTorrentFileName.setFont(new Font("Courrier", Font.ITALIC, 14));
 		this.labelTorrentFileSize.setBorder(BorderFactory.createLoweredBevelBorder());
 		this.labelTorrentFileSize.setFont(new Font("Courrier", Font.ITALIC, 14));
 		this.labelClientEvent.setBorder(BorderFactory.createLoweredBevelBorder());
 		this.labelClientEvent.setFont(new Font("Courrier", Font.ITALIC, 14));
 		this.centerPanel.setBorder(BorderFactory.createRaisedBevelBorder());
 		this.labelConnectedPeers.setBorder(BorderFactory.createLoweredSoftBevelBorder());
 	}
 	
 	/**
 	 * Initializes components behaviors.
 	 */
 	private void initBehaviors() {
 		this.saveLogToFile.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				File logFile = new File("log.txt");
 				if(logFile.exists()) {
 					try {
 						logFile.delete();
 					} catch (Exception ex) {
 						System.out.println(ex.getMessage());
 					}
 				}
 				String log = textFieldLog.getText();
 				byte[] bytes = log.getBytes();
 				try {
 					FileOutputStream fileOut = new FileOutputStream(logFile);
 					for(int i = 0; i < bytes.length; ++i) {
 						fileOut.write(bytes[i]);
 					}
 					fileOut.close();
 				} catch (Exception ex) { System.out.println(ex.getMessage());}
 				
 			}
 		});
 		this.exitMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Utilities.callClose();
 			}
 		});
 	}
 
 }
