 //Sam Dickson and Niraj Venkat
 //CS251 Project 5--Map Editor
 
 import javax.swing.*; 
 import javax.swing.event.*; 
 import javax.swing.text.*; 
 import javax.swing.border.*; 
 import javax.swing.colorchooser.*; 
 import javax.swing.filechooser.*; 
 import javax.accessibility.*; 
 import javax.imageio.ImageIO;
 
 import java.awt.*; 
 import java.awt.event.*; 
 import java.awt.geom.Line2D;
 import java.awt.geom.Rectangle2D;
 import java.beans.*; 
 import java.util.*; 
 import java.io.*; 
 import java.io.FileFilter;
 import java.applet.*; 
 import java.net.*;
 
 public class MapEditor extends JFrame implements ActionListener
 {
     //Constants
     public static final int PREFERRED_WIDTH = 680;
     public static final int PREFERRED_HEIGHT = 600;
     public static final double MAX_ZOOM = 50.0;
     public static final double MIN_ZOOM = 11.0;
     public static final double ZOOM_INCREMENT = 3.0;
     public static final int TOLERANCE = 10;
     
     //GUI components
     private JFrame directionsFrame;
     private JScrollPane scrollPane;
     private ZoomPane zoomPane;
     private MapScene map;
     private JPopupMenu popup;
     
     //Menu items for file menu:
     private JMenuItem exitAction;
     private JMenuItem openAction;
     private JMenuItem saveAction;
     private JMenuItem saveAsAction;
     private JMenuItem newAction;
     
     //Menu items for map menu:
     private JMenu mapMenu;
     private JMenuItem zoomInAction;
     private JMenuItem zoomOutAction;
     public JRadioButtonMenuItem insertLocationMode;
     public JRadioButtonMenuItem deleteLocationMode;
     public JRadioButtonMenuItem insertPathMode;
     public JRadioButtonMenuItem deletePathMode;
     public static JCheckBox displayPaths;
     public static JCheckBox displayVertices;
     
     //Menu items for directions menu:
     private JMenu directionsMenu;
     private JMenuItem directionsAction;
     private JMenuItem mstAction;
     
     //Menu items for help menu:
     private JMenuItem aboutAction;
     private JMenuItem helpAction;
     
     //Menu items for the right-click menu:
     private JMenuItem info_rightClick;
     private JMenuItem edit_rightClick;
     private JMenuItem delete_rightClick;
     
     //Items for directions frame
     private JComboBox fromMenu, toMenu;
     private JLabel fromLabel, toLabel;
     private JButton getDirections, directionsCancel;
     
     //DEBUGGING MENU
     private JMenu debugMenu;
     private JMenuItem connectAllVertices;
     private JMenuItem printPaths;
     private JMenuItem printVertices;
     public static JCheckBox printNames;
     
     //Session variables
     public static ArrayList<Vertex> points = new ArrayList<Vertex>();
     public static ArrayList<Path> paths = new ArrayList<Path>();
     
     public static String imagePath = "Resources/purdue-map.jpg"; //Default map image location
     public static String filePath = ""; //Default xml location
     XML mapXML = new XML();
     int vertex_id = 0;
     double zoomValue = 20.00;
    public static double scale_feet_per_pixel = 1.0;
     
     //Temporary variables
     Point p;
     Vertex rightClicked = null;
     
     public static void main(String[] args) 
     { 
     	MapEditor mapEditor = new MapEditor(); 
     	mapEditor.setVisible(true);
     } 
     
     //Handle events for menu objects
     public void actionPerformed(ActionEvent evt)
     {
     	//Actions for file menu:
     	if(evt.getSource().equals(exitAction)) //Exit program
     	{
     		System.exit(0);
     	}
     	else if(evt.getSource().equals(newAction)) //Create new map
     	{
     		String response = null;
     		String tmpPath = imagePath;
     		imagePath = null;
     		double tmp_scale_feet_per_pixel = scale_feet_per_pixel;
     		scale_feet_per_pixel = 0.0;
     		boolean done = false;
     		
     		JFileChooser fileChooser = new JFileChooser();
     		fileChooser.setFileFilter(new FileNameExtensionFilter("Valid Map Files", "jpg", "gif"));
     		int result = fileChooser.showOpenDialog(this);
     			
     		if(result == JFileChooser.APPROVE_OPTION)
     		{
     			 imagePath = fileChooser.getSelectedFile().getAbsolutePath();
 
 	    		done = false;
 	    		if(imagePath != null)
 	    		{
 		    		while(!done)
 		    		{
 		    			response = JOptionPane.showInputDialog(null, "Enter the feet-per-pixel constant: ", "New Map", JOptionPane.OK_CANCEL_OPTION);
 		    			
 		    			if(response == null)
 		    			{
 		    				break;
 		    			}
 		    			
 		    			try
 		    			{
 		    				scale_feet_per_pixel = Double.parseDouble(response);
 		    				done = true;
 		    			}
 		    			catch(Exception e)
 		    			{
 		    				JOptionPane.showMessageDialog(null, "Invalid feet-per-pixel constant.", "Error", JOptionPane.ERROR_MESSAGE);
 		    			}
 		    		}
 	    		}
     		}
     		
     		if(response != null && imagePath != null)
     		{
     			loadImage();
     			//JOptionPane.showMessageDialog(null, "New map successfully created!", "New Map", JOptionPane.PLAIN_MESSAGE);
     		}
     		else
     		{
     			imagePath = tmpPath;
     			scale_feet_per_pixel = tmp_scale_feet_per_pixel;
     		}
     	}
     	else if(evt.getSource().equals(openAction)) //Open existing XML 
     	{
     		JFileChooser fileChooser = new JFileChooser();
     		fileChooser.setFileFilter(new FileNameExtensionFilter("Valid Map Files", "xml"));
     		int result = fileChooser.showOpenDialog(this);
 			
 			if(result == JFileChooser.APPROVE_OPTION)
 			{
 				filePath = fileChooser.getSelectedFile().getAbsolutePath();
 				mapXML.openMap(filePath);
 				loadImage();
 				saveAction.setEnabled(true);
 			}
 			
     	}
     	else if(evt.getSource().equals(saveAction)) //Save current XML
     	{
     		if(filePath != null && imagePath != null)
     		{
     			mapXML.saveMap(filePath, imagePath, scale_feet_per_pixel);
     		}
     	}
     	else if(evt.getSource().equals(saveAsAction)) //Save current XML with different name
     	{
     		JFileChooser fileChooser = new JFileChooser();
     		int result = fileChooser.showSaveDialog(this);
     		
     		if(result == JFileChooser.APPROVE_OPTION)
     		{
     			filePath = fileChooser.getSelectedFile().getAbsolutePath();
     			if(!filePath.contains(".xml"))
     			{
     				filePath += ".xml";
     			}
     			mapXML.saveMap(filePath, imagePath, scale_feet_per_pixel);
     			saveAction.setEnabled(true);
     		}
     		
     	}
     	
     	//Actions for map menu:
     	else if(evt.getSource().equals(zoomInAction)) //Zoom in
     	{
     		if(zoomValue < MAX_ZOOM)
     		{
     			zoomOutAction.setEnabled(true);
     			double scale = (zoomValue + ZOOM_INCREMENT) / 20.0;
     			zoomValue+=ZOOM_INCREMENT;
     			zoomPane.zoom(scale);
     			zoomPane.repaint();
     		}
     		else
     		{
     			zoomInAction.setEnabled(false);
     		}
     	}
     	else if(evt.getSource().equals(zoomOutAction)) //Zoom out
     	{
     		if(zoomValue > MIN_ZOOM)
     		{
     			zoomInAction.setEnabled(true);
     			double scale = (zoomValue - ZOOM_INCREMENT) / 20.0;
     			zoomValue-=ZOOM_INCREMENT;
     			zoomPane.zoom(scale);
     			zoomPane.repaint();
     		}
     		else
     		{
     			zoomOutAction.setEnabled(false);
     		}
     	}
     	else if(evt.getSource().equals(displayVertices))
     	{
     		map.mouseMoved();
     		MenuSelectionManager.defaultManager().clearSelectedPath();  
     	}
     	else if(evt.getSource().equals(displayPaths))
     	{
     		map.mouseMoved();
     		MenuSelectionManager.defaultManager().clearSelectedPath();  
     	}
     	//Actions for directions menu
     	else if(evt.getSource().equals(directionsAction))
     	{
     		fromMenu.removeAllItems();
     		toMenu.removeAllItems();
     		fromMenu.addItem("-----");
     		toMenu.addItem("-----");
     		int max_size = 5;
     		
     		for(Vertex v : points)
     		{
     			fromMenu.addItem(v.getName());
     			
     			if(v.getName().length() > max_size)
     			{
     				max_size = v.getName().length();
     			}
     		}
     		for(Vertex v : points)
     		{
     			toMenu.addItem(v.getName());
     		}
     		
     		directionsFrame.setSize((295 + max_size),195);
     		directionsFrame.setVisible(true);
     		this.setEnabled(false);
     		directionsFrame.toFront();
     	}
     	else if(evt.getSource().equals(mstAction))
     	{
     		
     	}
     	else if(evt.getSource().equals(directionsCancel))
     	{
     		handleClose();
     	}
     	
     	//Actions for right-click menu
     	else if(evt.getSource().equals(info_rightClick))
     	{
     		JOptionPane.showMessageDialog(null, "ID: " + rightClicked.getID() + "\nName: " + rightClicked.getName() + "\nPoint: (" + rightClicked.getX() + "," + rightClicked.getY() + ")", "Location Information", JOptionPane.PLAIN_MESSAGE);
     	}
     	else if(evt.getSource().equals(edit_rightClick))
     	{
     		String newName = JOptionPane.showInputDialog(null, "New name for this location?", "Edit Location", JOptionPane.OK_CANCEL_OPTION);
     		if(newName != null || newName.equals(" "))
     		{
     			for(Vertex v : points)
     			{
     				if(v.equals(rightClicked))
     				{
     					v.setName(newName);
     					break;
     				}
     			}
     			rightClicked = null;
     		}
     	}
     	else if(evt.getSource().equals(delete_rightClick))
     	{
 			ArrayList<Path> toBeRemoved = new ArrayList<Path>();
 			 
 			 for(Path p : paths)
 			 {
 				 if(p.getStart().equals(rightClicked) || p.getEnd().equals(rightClicked))
 				 {
 					 toBeRemoved.add(p);
 				 }
 			 }
 			 
 			 for(Path condemned : toBeRemoved)
 			 {
 				 paths.remove(condemned);
 			 }
 			 
 			 points.remove(rightClicked);
 			 rightClicked = null;
 			 toBeRemoved = null;
 			 map.mouseClicked();
     	}
     	//DEBUGGING MENU
     	else if(evt.getSource().equals(connectAllVertices))
     	{
     		for(Vertex v : points)
     		{
     			for(Vertex other : points)
     			{
     				if(!other.equals(v))
     				{
     					Path tmp = new Path(v, other);
     					boolean okay = true;
     					
     					for(Path p : paths)
     					{
     						if(p.equals(tmp))
     						{
     							okay = false;
     							break;
     						}
     					}
     					if(okay)
     					{
     						paths.add(tmp);
     					}
     				}
     			}
     		}
     		map.mouseMoved();
     	}
     	else if(evt.getSource().equals(printPaths))
     	{
     		for(Path p : paths)
     		{
     			System.out.println(p);
     		}
     	}
     	else if(evt.getSource().equals(printVertices))
     	{
     		for(Vertex v : points)
     		{
     			System.out.println(v);
     		}
     	}
     	else if(evt.getSource().equals(printNames))
     	{
     		map.mouseMoved();
     	}
     	//Actions for help menu:
     	else if(evt.getSource().equals(aboutAction)) //Display about dialog
     	{
     		JOptionPane.showMessageDialog(null, "Map Viewer:\n\nDeveloped by Sam Dickson and Niraj Venkat\nPurdue University, CS 251\nApril 2013", "About Map Viewer", JOptionPane.PLAIN_MESSAGE);
     	}
     	else if(evt.getSource().equals(helpAction)) //Display help dialog
     	{
     		JOptionPane.showMessageDialog(null, "Map Viewer Commands:\n\n" +
     				"-Insert Location Mode: In this mode, you will be able to insert locations in the map by clicking on the map." +
     				"\nWhen you click on the map, a new location will be created without a name. In this mode you will also be able" +
     				"\nto click and drag an existing location to change the position or to select the current location." +
     				"\n\n-Location Properties: If you double-click on an existing position, a dialog will open that will show the" +
     				"\nproperties of the location: name, x, y, and id. You will be able to change the name of the location in the dialog" +
     				"\n\n-Delete Location Mode. In this mode you will be able to delete a location by clicking on top of the location." +
     				"\nAll paths containing that location will be deleted." +
     				"\n\n-Insert Path Mode. In this mode you will be able to insert a new path by clicking on a location and then dragging" +
     				"\nand releasing the mouse in another location." +
     				"\n\n-Delete Path Mode. In this mode you will be able to delete a path by clicking on top of the path." +
     				"\n\n-Save. This will save the graph in a map database with the current name." +
     				"\n\n-Save As. This will save the graph in a XML map file with a name different than the current name." +
     				"\n\n-Open. This opens an exisiting XML map file." +
     				"\n\n-New. This will clear the screen and prompt for two parameters:" +
     				"\n---The name of the image file to be used." +
     				"\n---The feet-per-pixel constant. This will be used by the Map Viewer to report the distance of a path." +
 
     				"\n\n-Zoom In: Zooms in the map by increasing the size of the map. This option will keep the current center of the bitmap." +
     				"\n-Zoom out. Zooms out the map.", "Help", JOptionPane.PLAIN_MESSAGE);
     	}
     }
     
     public boolean verifyFile(String fp)
     {
     	try
     	{
     		File tmp = new File(fp);
     	}
     	catch(Exception e)
     	{
     		return false;
     	}
     	
     	return true;
     }
     
     public void handleClose()
     {
     	directionsFrame.setVisible(false);
 		this.setEnabled(true);
 		this.toFront();
     }
     
     public void resetPaths()
     {
     	for(Path p : paths)
 		{
 			p.isDirectionEnabled = false;
 		}
 		map.mouseMoved();
     }
     
     public void loadImage()
     {
     	Image image = new ImageIcon(imagePath).getImage();
     	getContentPane().remove(zoomPane);
 		map.setImage(image);
 	    zoomPane.setScene(map);
 	    getContentPane().add(zoomPane);
 	    zoomPane.repaint();
     }
     
     public MapEditor() 
     {
 		setTitle("Map Editor");
 		setSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
 		setBackground(Color.gray);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	
 		JPanel panel = new JPanel();
 		panel.setLayout( new BorderLayout()); 
 		getContentPane().add(panel);
 		
 		//Create and set up menu bars:
 		JMenuBar menubar = new JMenuBar();
 		JMenu fileMenu = new JMenu("File");
 		mapMenu = new JMenu("Map");
 		JMenu helpMenu = new JMenu("Help");
 		popup = new JPopupMenu();
 		
 		//Directions Frame Setup
 		directionsFrame = new JFrame("Directions");
 		GridLayout frameLayout = new GridLayout(3,2);
 		JPanel toPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		JPanel fromPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		directionsFrame.setLayout(frameLayout);
 
 		fromMenu = new JComboBox();
 		toMenu = new JComboBox();
 		fromLabel = new JLabel("From: ");
 		toLabel = new JLabel  ("     To: ");
 		getDirections = new JButton("Get Directions");
 		directionsCancel = new JButton("Cancel");
 		directionsCancel.addActionListener(this);
 		getDirections.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent evt)
 			{
 				if(fromMenu.getSelectedIndex() != 0 && toMenu.getSelectedIndex() != 0)
 				{
 					MapViewer dijkstra = new MapViewer();
 					dijkstra.initiateDirections(points.get(fromMenu.getSelectedIndex() - 1));
 					LinkedList<Vertex> vertices = dijkstra.getDirections(points.get(toMenu.getSelectedIndex() - 1));
 					ArrayList<Path> selected = new ArrayList<Path>();
 					Vertex prev = null;
 					
 					for(Vertex v : vertices)
 					{
 						
 						if(prev == null)
 						{
 							prev = v;
 							continue;
 						}
 						
 							//Make path between v and prev green and also make v green
 							for(Path p : paths)
 							{
 								if(p.getStart().equals(prev) && p.getEnd().equals(v))
 								{
 									p.isDirectionEnabled = true;
 								}
 								
 							}
 							
 							prev = v;
 					}
 					
 					
 					handleClose();
 					map.mouseMoved();
 				}
 			}
 		});
 		getDirections.setIcon(new ImageIcon("Resources/directions.gif"));
 		directionsCancel.setIcon(new ImageIcon("Resources/cancel.gif"));
 		fromPanel.add(fromLabel);
 		fromPanel.add(fromMenu);
 		toPanel.add(toLabel);
 		toPanel.add(toMenu);
 		buttonPanel.add(getDirections);
 		buttonPanel.add(directionsCancel);
 		
 		directionsFrame.setSize(295,180);
 		directionsFrame.setResizable(false);
 		directionsFrame.add(fromPanel);
 		directionsFrame.add(toPanel);
 		directionsFrame.add(buttonPanel);
 		directionsFrame.setLocationRelativeTo(null); 
 		directionsFrame.addWindowListener(new WindowAdapter() {
 
 		    public void windowClosing(WindowEvent e) {
 		    	handleClose();
 		    }
 		});
 		
 		
 		//Menu items for file menu:
 		exitAction = new JMenuItem("Exit");
 		exitAction.addActionListener(this);
 		exitAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
 		openAction = new JMenuItem("Open");
 		openAction.addActionListener(this);
 		openAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
 		saveAction = new JMenuItem("Save");
 		saveAction.addActionListener(this);
 		saveAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
 		saveAction.setEnabled(false);
 		newAction = new JMenuItem("New");
 		newAction.addActionListener(this);
 		newAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
 		saveAsAction = new JMenuItem("Save As...");
 		saveAsAction.addActionListener(this);
 		saveAsAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
 		
 		fileMenu.add(newAction);
 		fileMenu.add(openAction);
 		fileMenu.add(saveAction);
 		fileMenu.add(saveAsAction);
 		fileMenu.add(exitAction);
 		
 		//Menu items for map menu:
 		zoomInAction = new JMenuItem("Zoom In");
 		zoomInAction.addActionListener(this);
 		zoomInAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, ActionEvent.CTRL_MASK));
 		zoomOutAction = new JMenuItem("Zoom Out");
 		zoomOutAction.addActionListener(this);
 		zoomOutAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK));
 		ButtonGroup modeOptions = new ButtonGroup();
 		insertLocationMode = new JRadioButtonMenuItem("Insert Location Mode");
 		deleteLocationMode = new JRadioButtonMenuItem("Delete Location Mode");
 		insertPathMode = new JRadioButtonMenuItem("Insert Path Mode");
 		deletePathMode = new JRadioButtonMenuItem("Delete Path Mode");
 		displayPaths = new JCheckBox("Display Paths");
 		displayPaths.setSelected(true);
 		displayPaths.addActionListener(this);
 		displayVertices = new JCheckBox("Display Locations");
 		displayVertices.setSelected(true);
 		displayVertices.addActionListener(this);
 		modeOptions.add(insertLocationMode);
 		modeOptions.add(deleteLocationMode);
 		modeOptions.add(insertPathMode);
 		modeOptions.add(deletePathMode);
 		
 		mapMenu.add(zoomInAction);
 		mapMenu.add(zoomOutAction);
 		mapMenu.addSeparator();
 		mapMenu.add(insertLocationMode);
 		mapMenu.add(deleteLocationMode);
 		mapMenu.add(insertPathMode);
 		mapMenu.add(deletePathMode);
 		mapMenu.addSeparator();
 		mapMenu.add(displayPaths);
 		mapMenu.add(displayVertices);
 		
 		//Menu items for directions menu:
 		directionsMenu = new JMenu("Directions");
 		directionsAction = new JMenuItem("Get Directions");
 		mstAction = new JMenuItem("Calculate MST");
 		directionsMenu.add(directionsAction);
 		directionsAction.addActionListener(this);
 		directionsMenu.add(mstAction);
 		mstAction.addActionListener(this);
 		
 		//Menu items for help menu:
 		aboutAction = new JMenuItem("About");
 		aboutAction.addActionListener(this);
 		helpAction = new JMenuItem("Help");
 		helpAction.addActionListener(this);
 		helpMenu.add(aboutAction);
 		helpMenu.add(helpAction);
 		
 		//Menu items for right-click menu:
 		info_rightClick = new JMenuItem("Info");
 		info_rightClick.addActionListener(this);
 		edit_rightClick = new JMenuItem("Edit");
 		edit_rightClick.addActionListener(this);
 		delete_rightClick = new JMenuItem("Delete");
 		delete_rightClick.addActionListener(this);
 		
 		
 		menubar.add(fileMenu);
 		menubar.add(mapMenu);
 		menubar.add(directionsMenu);
 		menubar.add(helpMenu);
 		
 		
 		//DEBUGGING MENU
 		debugMenu = new JMenu("Debug");
 		connectAllVertices = new JMenuItem("Connect All Vertices");
 		connectAllVertices.addActionListener(this);
 		debugMenu.add(connectAllVertices);
 		printPaths = new JMenuItem("Print Paths");
 		printPaths.addActionListener(this);
 		debugMenu.add(printPaths);
 		printVertices = new JMenuItem("Print Vertices");
 		printVertices.addActionListener(this);
 		debugMenu.add(printVertices);
 		printNames = new JCheckBox("Display Location Names");
 		printNames.addActionListener(this);
 		debugMenu.add(printNames);
 		menubar.add(debugMenu);
 				
 				
 		popup.add(info_rightClick);
 		popup.add(edit_rightClick);
 		popup.add(delete_rightClick);
 		setJMenuBar(menubar);
 		setLocationRelativeTo(null); 
 		
 		Image image = new ImageIcon(imagePath).getImage();
 		map = new MapScene(image);
 	    zoomPane = new ZoomPane(map);
 	    
 	    
 	    MouseAdapter listener = new MouseAdapter() {
 	    	
 	    	public void mouseClicked(MouseEvent e)
 	        {
 	    		resetPaths();
 	    		if(e.isMetaDown())
 	    		{
 	    			Point point = zoomPane.toViewCoordinates(e.getPoint());
 	    			boolean okay = false;
 	    			
 	    			for(Vertex v : points)
 	    			{
 	    				if(v.isThisMe(new Vertex(null, -1, point.x, point.y)))
 	    				{
 	    					rightClicked = v;
 	    					
 	    					okay = true;
 	    					break;
 	    				}
 	    			}
 	    			
 	    			if(okay)
 	    			{
 	    				popup.show(e.getComponent(), e.getX(), e.getY());
 	    				okay = false;
 	    			}
 	    		}
 	    		else
 	    		{
 		    		if(insertLocationMode.isSelected())
 		        	{
 			        		Point point = zoomPane.toViewCoordinates(e.getPoint());
 			        		boolean okay = true;
 			        		for(Vertex v : points)
 			        		{
 			        			if(v.isThisMe(new Vertex(null, -1, point.x, point.y)))
 			        			{
 			        				okay = false;
 			        				break;
 			        			}
 			        		}
 
 			        		if(okay)
 			        		{
 			        			String name = JOptionPane.showInputDialog(null, "Name for this location?", "Add Location", JOptionPane.OK_CANCEL_OPTION);
 					        	if(name != null && !name.equals(""))
 					        	{
 					        		points.add(new Vertex(name, (vertex_id++), (int)point.getX(), (int)point.getY()));
 			        				map.mouseClicked();
 					        	}
 			        		}
 			        	
 			        	
 		        	}
 		    		else if(deleteLocationMode.isSelected())
 		    		{
 		    			Point point = zoomPane.toViewCoordinates(e.getPoint());
 		    			ArrayList<Path> toBeRemoved = new ArrayList<Path>();
 		    			
 		    			 for(Vertex v : points)
 				         {
 		    				 if(v.isThisMe(new Vertex(null, -1, point.x, point.y)))
 		    				 {
 		    				 
 		    					 for(Path p : paths)
 		    					 {
 		    						 if(p.getStart().equals(v) || p.getEnd().equals(v))
 		    						 {
 		    							 toBeRemoved.add(p);
 		    						 }
 		    					 }
 		    					 
 		    					 points.remove(v);
 		    					 break;
 		    				 }
 				         }
 		    			 
 		    			 for(Path condemned : toBeRemoved)
 		    			 {
 		    				 paths.remove(condemned);
 		    			 }
 		    			 toBeRemoved = null;
 		    			 map.mouseClicked();
 		    			 
 		    		}
 		    		else if(deletePathMode.isSelected())
 		    		{
 		    			Path condemned = null;
 		    			for(Path p : paths)
 				        {
 		    				if(p.isSelected)
 		    				{
 		    					condemned = p;
 		    				}
 				        }
 		    			if(condemned != null)
 		    			{
 		    				paths.remove(condemned);
 		    				map.mouseClicked();
 		    			}
 		    		}
 	    		}
 	        }
 	    	public void mousePressed(MouseEvent e) 
 	    	{
 	    		resetPaths();
 	    		if(insertPathMode.isSelected())
 	    		{
 		            Point point = zoomPane.toViewCoordinates(e.getPoint());
 		            if(paths.size() > 0)
 		            {
 		            	try
 		            	{
 			            	if(paths.get(paths.size() - 1).getEnd() == null)
 			            	{
 			            		paths.remove(paths.size() - 1);
 			            	}
 		            	}
 		            	catch(Exception e2){}
 		            }
 		            for(Vertex v : points)
 		            {
 		            	if(v.isThisMe(new Vertex(null, -1, point.x, point.y)))
 		            	{
 		            		paths.add(new Path(v, null));
 		            		break;
 		            	}
 		            }
 		            map.mousePressed(point);
 	    		}
 	    		if(insertLocationMode.isSelected())
 	    		{
 	    			Point point = zoomPane.toViewCoordinates(e.getPoint());
 	    			for(Vertex v : points)
 	    			{
 	    				if(v.isThisMe(new Vertex(null, -1, point.x, point.y)))
 		            	{
 		            		v.beingModified = true;
 		            	}
 	    				else
 	    				{
 	    					v.beingModified = false;
 	    				}
 	    			}
 	    		}
 	        }
 	    	
 	    	public void mouseReleased(MouseEvent e)
 	        {
 	    		if(insertPathMode.isSelected())
 	    		{
 		            Point point = zoomPane.toViewCoordinates(e.getPoint());
 		            for(Vertex v : points)
 		            {
 		            	if(v.isThisMe(new Vertex(null, -1, point.x, point.y)))
 		            	{
 		            		if((paths.get(paths.size() - 1).getStart().getX() != point.x) && (paths.get(paths.size() - 1).getStart().getY() != point.y))
 		            		{
 		            			paths.get(paths.size() - 1).setEnd(v);
 		            		}
 		            		else
 		            		{
 		            			paths.remove(paths.size() - 1);
 		            		}
 		            		break;
 		            	}
 		            }
 		            map.mouseReleased();
 	    		}
 	    		else if(insertLocationMode.isSelected())
 	    		{
 	    			Vertex check = null;
 	    			for(Vertex v : points)
 	        		{
 	        			if(v.beingModified == true)
 	        			{
 	        				check = v;
 	        				v.beingModified = false;
 	        			}
 	        		}
 	    			
 	    			if(check != null)
 	    			{
 			        	boolean okay = true;
 			        	for(Vertex v : points)
 			        	{
 			        		if(v.isThisMe(new Vertex(null, -1, check.getX(), check.getY())) && (!v.equals(check)))
 			        		{
 			        			okay = false;
 			        			break;
 			        		}
 			        	}
 
 			        	if(okay)
 			        	{
 			        		check.update();
 			        		map.mouseReleased();
 			        	}
 			        	else
 			        	{
 			        		check.reset();
 			        		map.mouseReleased();
 			        	}
 		    			
 	    			}
 	    		}
 	        }
 	    	
 	    	 
 	      };
 
 	      MouseMotionAdapter motionListener = new MouseMotionAdapter() {
 	        public void mouseDragged(MouseEvent e) 
 	        {
 	        	if(insertPathMode.isSelected())
 	    		{
 	        		try
 	        		{
 			            if(paths.get(paths.size() - 1).getEnd() == null)
 			            {
 			            	Point point = zoomPane.toViewCoordinates(e.getPoint());
 			  	          	map.mouseDragged(point);
 			            }
 	        		}
 	        		catch(Exception e2){}
 		            
 	    		}
 	        	else if(insertLocationMode.isSelected())
 	        	{
 	        		Point point = zoomPane.toViewCoordinates(e.getPoint());
 	        		
 	        		for(Vertex v : points)
 	        		{
 	        			if(v.beingModified == true)
 	        			{
 	        				v.setX(point.x);
 	        				v.setY(point.y);
 	        				map.mouseMoved();
 	        				break;
 	        			}
 	        		
 	        		}
 	        	}
 	          
 	        }
 	        
 	        public void mouseMoved(MouseEvent e)
 	        {
 	        	if(deletePathMode.isSelected())
 	        	{
 	        		Point point = zoomPane.toViewCoordinates(e.getPoint());
 	        		Rectangle2D.Double tolerance = new Rectangle2D.Double(point.x, point.y, TOLERANCE, TOLERANCE);
 	        		for(Path p : paths)
 	        		{
 	        			Line2D.Double bound = new Line2D.Double(p.getStart().getX(),p.getStart().getY(),p.getEnd().getX(),p.getEnd().getY());
 	        			if(bound.intersects(tolerance))
 	        			{
 	        				p.isSelected = true;
 	        				break;
 	        			}
 	        			else
 	        			{
 	        				p.isSelected = false;
 	        			}
 	        		}
 	        		map.mouseMoved();
 	        	}
 	        	
 	        	if(deleteLocationMode.isSelected())
 	        	{
 	        		Point point = zoomPane.toViewCoordinates(e.getPoint());
 	        		for(Vertex v : points)
 	        		{
 	        			if(v.isThisMe(new Vertex(null, -1, point.x, point.y)))
 	        			{
 	        				v.isSelected = true;
 	        				break;
 	        			}
 	        			else
 	        			{
 	        				v.isSelected = false;
 	        			}
 	        		}
 	        		map.mouseMoved();
 	        	}
 	        	
 	        }
 	        
 	      };
 
 	      zoomPane.getZoomPanel().addMouseListener(listener);
 	      zoomPane.getZoomPanel().addMouseMotionListener(motionListener);
 
 	    getContentPane().add(zoomPane);
 
     }
     
     
 };
