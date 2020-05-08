 /**
  * 
  */
 package de.findus.cydonia.server;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import de.findus.cydonia.server.GameServer.ServerStateListener;
 
 /**
  * @author Findus
  *
  */
 @SuppressWarnings("serial")
 public class ServerConfigFrame extends JFrame implements ActionListener, ServerStateListener, Console {
 
 	protected static final String MAPFOLDER = "/de/findus/cydonia/level/";
 	
 	private static final FileFilter mfxFilter = new FileFilter() {
 		@Override
 		public boolean accept(File pathname) {
 			if(pathname.isFile() && pathname.getName().endsWith(GameServer.MAPEXTENSION)) {
 				return true;
 			}
 			return false;
 		}
 	};
 	
 	private static final javax.swing.filechooser.FileFilter mfxChooserFilter = new FileNameExtensionFilter("XML Map Files", "mfx");
 	
 	private GameServer server;
 	
 	private JButton mapButton;
 	private JButton saveButton;
 	private JTextArea consoleOutput;
 	private JTextField commandInput;
 	
 	JLabel nameLabel;
 	JLabel mapLabel;
 	JLabel stateLabel;
 	
 	private DefaultListModel<String> listmodel;
 	private JList<String> maplist;
 	
 	public ServerConfigFrame(GameServer server) {
 		this.server = server;
 		
 		this.setTitle("Cydonia Server");
 
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(ServerConfigFrame.this, "Do you really want to shut down the server?", "Shut down?", JOptionPane.YES_NO_OPTION)) {
 					ServerConfigFrame.this.server.stop(true);
 					dispose();
 				}
 			}
 		});
 
 		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		this.setLocationByPlatform(true);
 		initGUI();
 		
 		saveDefaultMaps();
 		loadMapsFromDir();
 		
 		server.registerStateListener(this);
 	}
 	
 	private void initGUI() {
 		JTabbedPane tabbedPane = new JTabbedPane();
 		this.add(tabbedPane, BorderLayout.CENTER);
 		
 		// Main panel
 		JPanel mapPanel = new JPanel(new BorderLayout());
 		tabbedPane.addTab("Map", mapPanel);
 		
 		JPanel buttonPanel = new JPanel();
 		mapPanel.add(buttonPanel, BorderLayout.NORTH);
 		
 		mapButton = new JButton("Load Map");
 		buttonPanel.add(mapButton);
 		mapButton.setActionCommand("loadMap");
 		mapButton.addActionListener(this);
 		
 		saveButton = new JButton("Save Map");
 		buttonPanel.add(saveButton);
 		saveButton.setActionCommand("saveMap");
 		saveButton.addActionListener(this);
 		
 		listmodel = new DefaultListModel<String>();
 		maplist = new JList<String>(listmodel);
 		mapPanel.add(maplist, BorderLayout.CENTER);
 		maplist.setMinimumSize(new Dimension(400, 300));
 		maplist.setPreferredSize(new Dimension(400, 300));
 		
 		// Info panel
 		JPanel infoPanel = new JPanel(new GridBagLayout());
 		tabbedPane.addTab("Info", infoPanel);
 		nameLabel = new JLabel("My Server");
 		mapLabel = new JLabel();
 		stateLabel = new JLabel("Stopped");
 		
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 2;
 		c.anchor = GridBagConstraints.CENTER;
 		infoPanel.add(new JLabel("Server Infos:"), c);
 		c.gridx = 0;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		c.anchor = GridBagConstraints.WEST;
 		infoPanel.add(new JLabel("Servername: "), c);
 		c.gridx = 1;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		c.anchor = GridBagConstraints.WEST;
 		infoPanel.add(nameLabel, c);
 		c.gridx = 0;
 		c.gridy = 2;
 		c.gridwidth = 1;
 		c.anchor = GridBagConstraints.WEST;
 		infoPanel.add(new JLabel("Map: "), c);
 		c.gridx = 1;
 		c.gridy = 2;
 		c.gridwidth = 1;
 		c.anchor = GridBagConstraints.WEST;
 		infoPanel.add(mapLabel, c);
 		c.gridx = 0;
 		c.gridy = 3;
 		c.gridwidth = 1;
 		c.anchor = GridBagConstraints.WEST;
 		infoPanel.add(new JLabel("State: "), c);
 		c.gridx = 1;
 		c.gridy = 3;
 		c.gridwidth = 1;
 		c.anchor = GridBagConstraints.WEST;
 		infoPanel.add(stateLabel, c);
 		
 		// Console panel
 		JPanel consolePanel = new JPanel(new BorderLayout());
 		tabbedPane.addTab("Console", consolePanel);
 		
 		consoleOutput = new JTextArea("Console Output");
 		consolePanel.add(consoleOutput, BorderLayout.CENTER);
 		consoleOutput.setMinimumSize(new Dimension(400, 300));
 		consoleOutput.setPreferredSize(new Dimension(400, 300));
 		consoleOutput.setEditable(false);
 		
 		commandInput = new JTextField();
 		consolePanel.add(commandInput, BorderLayout.SOUTH);
 		commandInput.addActionListener(this);
 		commandInput.setActionCommand("sendCommand");
 		commandInput.setMinimumSize(new Dimension(400, 20));
 	}
 
 	private String[] getDefaultMapNames() {
 		InputStream is = this.getClass().getResourceAsStream("/de/findus/cydonia/level/levels.txt");
 		BufferedReader br = new BufferedReader(new InputStreamReader(is));
 		String line;
 		LinkedList<String> list = new LinkedList<String>();
 		try {
 			while ((line = br.readLine()) != null) {
 				list.add(line);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return list.toArray(new String[list.size()]);
 	}
 	
 	private void saveDefaultMaps() {
 		File userdir = new File(System.getProperty("user.home"));
 		if(userdir.exists() && userdir.isDirectory()) {
 			File dir = new File(System.getProperty("user.home") + "/Cydonia/maps/");
 			if(!dir.exists()) {
 				dir.mkdirs();
 			}
 			server.handleCommand("sv_mapsdir " + dir.getPath());
 			if(dir.exists() && dir.isDirectory()) {
 				for(String s : getDefaultMapNames()) {
 					File f = new File(dir.getPath() + System.getProperty("file.separator") + s + GameServer.MAPEXTENSION);
 					try {
 						if(f.createNewFile()) {
 							BufferedWriter bw = new BufferedWriter(new FileWriter(f));
 
 							InputStream is = this.getClass().getResourceAsStream(MAPFOLDER + s + GameServer.MAPEXTENSION);
 							BufferedReader br = new BufferedReader(new InputStreamReader(is));
 							String line;
 							while ((line = br.readLine()) != null) {
 								bw.write(line);
 							}
 							bw.close();
 							br.close();
 							is.close();
 						}
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 	
 	private void loadMapsFromDir() {
 		listmodel.clear();
 		
 		File userdir = new File(System.getProperty("user.home"));
 		if(userdir.exists() && userdir.isDirectory()) {
 			File file = new File(System.getProperty("user.home") + "/Cydonia/maps/");
 			if(file.exists() && file.isDirectory()) {
 				System.out.println("loading maps from dir: " + file.getPath());
 				File[] maps = file.listFiles(mfxFilter);
 				for(File m : maps) {
 					listmodel.addElement(m.getName().substring(0, m.getName().indexOf(GameServer.MAPEXTENSION)));
 				}
 			}
 		}
 	}
 	
 	private void saveMap() {
 		String mapname = JOptionPane.showInputDialog("Please insert a name for this map:", server.getWorldController().getMap().getName());
 		
 		File dir = new File(System.getProperty("user.home") + "/Cydonia/maps/");
 		if(!dir.exists()) {
 			dir = new File(System.getProperty("user.home"));
 		}
 		JFileChooser chooser = new JFileChooser(dir);
 		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		chooser.setFileFilter(mfxChooserFilter);
 		chooser.setSelectedFile(new File(dir, mapname + GameServer.MAPEXTENSION));
 		boolean accepted = false;
 		File f = null;
 		do {
 			if(JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(this)) {
 				f = chooser.getSelectedFile();
 				if(!f.exists()) {
 					accepted = true;
 				}else if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "File already exits. Do you want to overwrite it?", "Overwrite?", JOptionPane.YES_NO_OPTION)) {
 					accepted = true;
 				}
 			}else {
 				break;
 			}
 		}while (!accepted);
 		
 		if(accepted && f != null) {
 			try {
 				if(f.exists()) f.delete();
 				f.createNewFile();
 				BufferedWriter bw = new BufferedWriter(new FileWriter(f));
 				server.saveCurrentMap(mapname, bw);
 				bw.flush();
 				bw.close();
 				loadMapsFromDir();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 		}
 	}
 
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if("sendCommand".equals(e.getActionCommand())) {
			consoleOutput.append("-> " + commandInput.getText());
 			server.handleCommand(commandInput.getText());
 			commandInput.setText("");
 		}else if("loadMap".equals(e.getActionCommand())) {
 			if(maplist.getSelectedValue() != null) {
 				server.handleCommand("mp_map " + maplist.getSelectedValue());
 			}
 		}else if("saveMap".equals(e.getActionCommand())) {
 			saveMap();
 		}
 	}
 
 	@Override
 	public void stateChanged() {
 		if(server.getWorldController() != null && server.getWorldController().getMap() != null) { 
 			mapLabel.setText(server.getWorldController().getMap().getName());
 		}
 	}
 
 	@Override
 	public void writeLine(String line) {
 		this.consoleOutput.append("\n" + line);
 	}
 }
