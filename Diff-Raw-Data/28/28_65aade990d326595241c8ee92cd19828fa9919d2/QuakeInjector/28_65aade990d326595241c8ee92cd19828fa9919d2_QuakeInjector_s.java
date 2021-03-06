 package de.haukerehfeld.quakeinjector;
 
 //import java.awt.*;
 import java.awt.Container;
 import java.awt.Dimension;
 
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.table.TableRowSorter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import java.io.File;
 
 public class QuakeInjector {
 	/**
 	 * Window title
 	 */
 	private static final String applicationName = "Quake Injector";
 
 	private final Configuration config;
 	private final Paths paths;
 
 	private final EngineStarter starter;
 
 	public QuakeInjector() {
 		config = new Configuration();
 
 		paths = new Paths(config.get("repositoryBase"),
 						  config.get("enginePath"));
 		/** @todo 2009-05-04 14:48 hrehfeld    check if the paths still exist at startup */
 		starter = new EngineStarter(new File(config.getEnginePath()),
 									new File(config.getEnginePath()
 											 + File.separator
 											 + config.getEngineExecutable()),
 									config.getEngineCommandline());
 	}
 
 	private void createMenu(final JFrame frame) {
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.setOpaque(true);
 		menuBar.setPreferredSize(new Dimension(200, 20));
 		frame.setJMenuBar(menuBar);
 
 		JMenu menu = new JMenu("File");
 		menuBar.add(menu);
 
 		JMenuItem menuItem = new JMenuItem("Quit",
 										   KeyEvent.VK_T);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
 													   ActionEvent.ALT_MASK));
 		menuItem.getAccessibleContext().setAccessibleDescription(
 			"This doesn't really do anything");
 		menu.add(menuItem);
 
 
 		JMenu configM = new JMenu("Configuration");
 		menuBar.add(configM);
 
 		JMenuItem engine = new JMenuItem("Engine Configuration");
 		configM.add(engine);
 		engine.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					EngineConfigDialog d = new EngineConfigDialog(frame,
 																  config.getEnginePath(),
 																  config.getEngineExecutable(),
 																  config.getEngineCommandline());
 					saveEngineConfig(d.getEnginePath(),
 									 d.getEngineExecutable(),
 									 d.getCommandline());
 					
 				}});
 	}
 
 
 	private void saveEngineConfig(File enginePath,
 								  File engineExecutable,
 								  String commandline) {
 		setEngineConfig(enginePath, engineExecutable, commandline);
 
 		config.setEnginePath(enginePath.getAbsolutePath());
 		config.setEngineExecutable(RelativePath.getRelativePath(enginePath, engineExecutable));
 		config.setEngineCommandline(commandline);
 		
 		config.write();
 	}
 
 	private void setEngineConfig(File enginePath,
 								 File engineExecutable,
 								 String commandline) {
 		starter.setQuakeDirectory(enginePath);
 		starter.setQuakeExecutable(engineExecutable);
 		starter.setQuakeCommandline(commandline);
 	}
 	
 
 	private void addMainPane(Container panel) {
 		JPanel mainPanel = new JPanel();
 		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
 
 		final MapList maplist = new MapList();
 		
 		//create a table
 		final JTable table = new JTable(maplist);
 		final TableRowSorter<MapList> sorter = new TableRowSorter<MapList>(maplist);
 		table.setRowSorter(sorter);
 		
 		table.setPreferredScrollableViewportSize(new Dimension(500, 600));
 		table.setFillsViewportHeight(true);
 		table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
 
 		{
 			JPanel filterPanel = new JPanel(new SpringLayout());
 			filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.LINE_AXIS));
 			JLabel filterText = new JLabel("Filter: ", SwingConstants.TRAILING);
 			filterPanel.add(filterText);
 
 			final JTextField filter = new JTextField();
 			filter.getDocument().addDocumentListener(
                 new DocumentListener() {
                     public void changedUpdate(DocumentEvent e) { filter(); }
                     public void insertUpdate(DocumentEvent e) { filter(); }
                     public void removeUpdate(DocumentEvent e) { filter(); }
 
 					private void filter() {
 						maplist.filter(sorter, filter.getText());
 					}
                 });
 			filterText.setLabelFor(filter);
 			filterPanel.add(filter);
 
 			mainPanel.add(filterPanel);
 
 		}
 
 		//Create the scroll pane and add the table to it.
 		JScrollPane scrollPane = new JScrollPane(table);
 
 
 		mainPanel.add(scrollPane);
 		panel.add(mainPanel);
 
 		final InstalledMaps installedMaps = new InstalledMaps();
 
 		MapInfoPanel interactionPanel = new MapInfoPanel(paths, installedMaps, starter);
 		maplist.addChangeListener(interactionPanel);
 		panel.add(interactionPanel);
		ShowMapInfoSelectionHandler selectionHandler = new ShowMapInfoSelectionHandler(interactionPanel,
																					   maplist);
 		table.getSelectionModel().addListSelectionListener(selectionHandler);
 
 		final MapInfoParser parser = new MapInfoParser();
 		SwingWorker<List<MapInfo>,Void> parse = new SwingWorker<List<MapInfo>, Void>() {
 			@Override
 			public List<MapInfo> doInBackground() {
 				try {
 					installedMaps.read();
 				}
 				catch (java.io.IOException e) {
 					/** @todo 2009-04-28 19:00 hrehfeld    better error reporting? */
 					System.out.println(e.getMessage());
 				}
 				java.util.List<MapInfo> maps = parser.parse();
 				installedMaps.set(maps);
 				return maps;
 			}
 
 			@Override
 			public void done() {
 				try {
 					maplist.setMapList(get());
 				}
 				catch (java.lang.InterruptedException e) {
 					throw new RuntimeException("Couldn't get map list!" + e.getMessage());
 				}
 				catch (java.util.concurrent.ExecutionException e) {
 					throw new RuntimeException("Couldn't get map list!" + e.getMessage());
 				}
 			}
 		};
 		parse.execute();
 
 	}
 
 	
 	private void createAndShowGUI() {
 		//Create and set up the window.
 		JFrame frame = new JFrame(applicationName);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		frame.getContentPane()
 			.setLayout(new BoxLayout(frame.getContentPane(),
 									 BoxLayout.PAGE_AXIS));
 
 		createMenu(frame);
 
 
 		//Add the scroll pane to this panel.
 		addMainPane(frame.getContentPane());
 
 		//Display the window.
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	public static void main(String[] args) {
 		//Schedule a job for the event-dispatching thread:
 		//creating and showing this application's GUI.
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					QuakeInjector qs = new QuakeInjector();
 					qs.createAndShowGUI();
 				}
 			});
 
 	}
 }
