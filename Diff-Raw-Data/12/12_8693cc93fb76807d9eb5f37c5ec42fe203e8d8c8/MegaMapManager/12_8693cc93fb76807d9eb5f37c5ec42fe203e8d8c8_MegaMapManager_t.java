 package ca.etsmtl.capra.tools.megamapmanager;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Locale;
 import java.util.Vector;
 
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultCellEditor;
 import javax.swing.DropMode;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.TableCellEditor;
 
 import java.util.List;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 import ca.etsmtl.capra.datas.Position;
 import ca.etsmtl.capra.digitizer.datas.GPSData;
 import ca.etsmtl.capra.digitizer.implementations.NovAtelGPS_BestPos;
 import ca.etsmtl.capra.digitizer.sensors.Gps;
 import ca.etsmtl.capra.digitizer.sensors.Gps.CallBack;
 import ca.etsmtl.capra.tools.megamapmanager.MapViewer.EditMode;
 import ca.etsmtl.capra.tools.megamapmanager.Waypoint.WaypointPriority;
 import ca.etsmtl.capra.util.file.GenericFactory;
 import ca.etsmtl.capra.util.saving.FileLoader;
 
 public class MegaMapManager extends JFrame
 {
 	public static final int MEDIAN_FIFO_SIZE = 5;
 	public static final int MEAN_BUFFER_SIZE = 5;
 	
 	private JPanel contentPane;
 	private JTextField txtLat;
 	private JTextField txtLon;
 	private JTextField txtNumber;
 	protected final DefaultCellEditor priorityCell;
 	protected WaypointTableModel tableModel;
 	private final ButtonGroup buttonGroup = new ButtonGroup();
 	protected final MapViewer viewer;
 	protected final JFileChooser fc = new JFileChooser();
 	private JTable waypointsList;
 	private NovAtelGPS_BestPos gps;
 	private LinkedBlockingQueue<GPSData> gpsFIFO, gpsMeanBuffer;
 	private GPSData actualGPSData;
 	private JScrollPane scrollPane;
 	private Position lastMousePosition;
 	private boolean gpsReady = false;
 	
 	/**
 	 * Create the frame.
 	 */
 	public MegaMapManager()
 	{	
 		gpsFIFO = new LinkedBlockingQueue<GPSData>(); 
 		gpsMeanBuffer = new LinkedBlockingQueue<GPSData>(); 
 		actualGPSData = new GPSData(0,0);
 		gps = new NovAtelGPS_BestPos();
 		gps.registerCallBack(new CallBack() {
 			
 			@Override
 			public void gotNewData(Gps source) {	
 				GPSData d = source.getCurrentGpsData();
 				addGPSData(d);
 				System.out.println("RAW => long:" + d.getLongitude() + ", lat: " + d.getLatitude() + " --- FILTERED =>  Long: " + actualGPSData.getLongitude() + ", lat:" + actualGPSData.getLatitude());
 			}
 		});
 		
         priorityCell = new DefaultCellEditor( new JComboBox( WaypointPriority.values() ) );
 
 		viewer = new MapViewer();
 		
 		viewer.getImagePainter().addMouseListener(new MouseAdapter() 
 		{
 			@Override
 			public void mousePressed(MouseEvent e)
 			{
 				Point pt = e.getPoint();
 				Position position = new Position(viewer.toDataX(pt.x), viewer.toDataY(pt.y));
 				viewer.setLastMousePosition(position);
 			}
 			
 			@Override
 			public void mouseClicked(MouseEvent e) 
 			{
 		      Point pt = e.getPoint();
 		      Position position = new Position(viewer.toDataX(pt.x), viewer.toDataY(pt.y));
 
 				for (int i=0; i < tableModel.getRowCount(); i++)
 				{
					float mouseX = position.getX();
					float mouseY = position.getY();
					float pointX = (Float) ((Vector)tableModel.getDataVector().elementAt(i)).elementAt(2);
					float pointY = (Float) ((Vector)tableModel.getDataVector().elementAt(i)).elementAt(3);
					
					if(mouseX >= pointX - 20 / viewer.getZoom() && mouseY >= pointY - 20 / viewer.getZoom() && mouseX <= pointX + 20 / viewer.getZoom() && mouseY <= pointY + 20 / viewer.getZoom())
 					{
 						viewer.setSelectedWaypoint(i);
 						break;
 					}
 					else
 					{
 						viewer.setSelectedWaypoint(-1);
 					}
 				}
 		      
 				if(viewer.getSelectedWaypoint() == -1)
 				{
 					 if(SwingUtilities.isLeftMouseButton(e) && viewer.getMode() == EditMode.Obstacle)
 					 {
 					      double distance = Point2D.distance(position.getX(), position.getY(), viewer.getLastObstacle().getX(), viewer.getLastObstacle().getY());
 					      if (distance >= MapViewer.MIN_OBSTACLE_DISTANCE)
 					      {
 					        //obstacleDetectionServiceSim.addObstacle(newMousePos);
 					        viewer.setLastObstacle(position);
 					    	viewer.addObstacle(position);
 					      }
 					  }
 					  else if(SwingUtilities.isLeftMouseButton(e) && viewer.getMode() == EditMode.Waypoint)
 					  {
 					  	//refreshWaypoints(viewer.getWaypoints().toArray());
 					  	tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, "High", position.getX(), position.getY()});
 					  	viewer.setWaypoints(tableModel.toWaypointList());
 					  }
 				}
 				else
 				{
 			        if (e.isShiftDown())
 			        {
 			          if (SwingUtilities.isLeftMouseButton(e) && viewer.getMode() == EditMode.Waypoint)
 			          {
 			        	 tableModel.removeRow(viewer.getSelectedWaypoint());
 			        	  
 			     		 for(int i = 0; i < tableModel.getRowCount(); i++)
 			    		 {
 			    			 ((Vector) tableModel.getDataVector().elementAt(i)).setElementAt(i + 1, 0);
 			    		 }
 			     		 
 			     		 viewer.setSelectedWaypoint(-1);
 			     		 tableModel.fireTableDataChanged();
 			          }
 			        }
 				}
 				
 				viewer.repaint();
 				repaint();
 			}
 		});
 		viewer.getImagePainter().addMouseMotionListener(new MouseAdapter() {		
 			@Override
 		    public void mouseDragged(MouseEvent e)
 		    {
 		      Point pt = e.getPoint();
 		      Position position = new Position(viewer.toDataX(pt.x), viewer.toDataY(pt.y));
 		      
 	    	  if(SwingUtilities.isLeftMouseButton(e) && viewer.getMode() == EditMode.Obstacle)
 	    	  {
 	              double distance = Point2D.distance(position.getX(), position.getY(), viewer.getLastObstacle().getX(), viewer.getLastObstacle().getY());
 	              if (distance >= MapViewer.MIN_OBSTACLE_DISTANCE)
 	              {
 	                //obstacleDetectionServiceSim.addObstacle(newMousePos);
 	                viewer.setLastObstacle(position);
 	                viewer.addObstacle(position);
 	              }
 	    	  }
 	    	  else if(SwingUtilities.isLeftMouseButton(e) && viewer.getMode() == EditMode.Line)
 	    	  {
 	          	double distance = Point2D.distance(position.getX(), position.getY(), viewer.getLastObstacle().getX(), viewer.getLastObstacle().getY());
 	  			if (distance >= MapViewer.MIN_LINE_DISTANCE)
 	  			{
 	  			  Line2D.Float line = new Line2D.Float(viewer.getLastLineEnd().getX(), viewer.getLastLineEnd().getY(), position.getX(), position.getY());
 	  			  /*if (lineServiceSim != null)
 	  			  {
 	  			    lineServiceSim.addLine(line);
 	  			  }*/
 	  			  viewer.setLastLineEnd(position);
 	  			  viewer.addLine(line);
 	  			}
 	    	  }
 	    	  else if(SwingUtilities.isRightMouseButton(e) && viewer.getMode() == EditMode.Waypoint)
 	    	  {
 				if (viewer.getSelectedWaypoint() != -1)
 				{
 					((Vector)tableModel.getDataVector().elementAt(viewer.getSelectedWaypoint())).setElementAt(position.getX(), 2);
 					((Vector)tableModel.getDataVector().elementAt(viewer.getSelectedWaypoint())).setElementAt(position.getY(), 3);
 					tableModel.fireTableDataChanged();
 				}
 	    	  }
 	    	  else if(SwingUtilities.isLeftMouseButton(e) && viewer.getMode() == EditMode.Measuring)
 	    	  {
 	    		  viewer.getMeasuringLine().setLine(viewer.getLastMousePosition().getX(), viewer.getLastMousePosition().getY(), position.getX(), position.getY());
 	    	  }
 		      
 		      viewer.repaint();
 		      repaint();
 		    }
 		});
 		
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setBounds(100, 100, 800, 500);
 		
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		
 		JMenu mnFile = new JMenu("File");
 		menuBar.add(mnFile);
 		
 		JMenuItem mntmLoadMap = new JMenuItem("Load Map...");
 		mntmLoadMap.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int choice = fc.showOpenDialog(MegaMapManager.this);
 				File file = fc.getSelectedFile();
 				
 				if(choice == JOptionPane.YES_OPTION)
 				{
 					if(file != null)
 					{
 						System.out.println("Loading folder: " + file.getParent());
 						
 						FileLoader loader = new FileLoader();
 						
 						ArrayList<Position> obstacles = null;
 						ArrayList<MappingLine> tempLines = null;
 						ArrayList<Line2D.Float> lines = new ArrayList<Line2D.Float>();
 						
 						try {
 							obstacles = loader.load(file.getParent() + "/obstacles",
 									new GenericFactory<Position>() {
 										@Override
 										public Position createInstance() {
 											return new Position();
 										}
 							});
 							
 							tempLines = loader.load(file.getParent() + "/lines",
 									new GenericFactory<MappingLine>() {
 										@Override
 										public MappingLine createInstance() {
 											return new MappingLine();
 										}
 							});
 							
 							for(MappingLine line : tempLines)
 							{
 								lines.add(new Line2D.Float(line.getX1(), line.getY1(), line.getX2(), line.getY2()));
 							}
 							
 						} catch (FileNotFoundException ex) {
 							ex.printStackTrace();
 						}
 						
 						if(obstacles == null){
 							System.out.println("Error loading map.  No positions found.");
 						}else{
 							viewer.addObstacles(obstacles);
 						}
 						
 						if(lines == null){
 							System.out.println("Error loading map.  No lines found.");
 						}else{
 							viewer.addLines(lines);
 						}
 					}
 					
 					viewer.repaint();
 				}
 			}
 		});
 		mnFile.add(mntmLoadMap);
 		
 		JMenuItem mntmLoadWayPoints = new JMenuItem("Load Waypoints...");
 		mntmLoadWayPoints.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int choice = fc.showOpenDialog(MegaMapManager.this);
 				
 				if(choice == JOptionPane.YES_OPTION)
 				{
 					File file = fc.getSelectedFile();
 				    List<Waypoint> waypoints = viewer.load(file.getAbsolutePath());
 				    initializeDataTable();
 					tableModel.fireTableDataChanged();
 				    
 				    for(Waypoint waypoint : waypoints)
 				    {
 				    	tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, waypoint.getPriority().toString(), waypoint.getPosition().getX(), waypoint.getPosition().getY()});
 				    }
 				}
 			}
 		});
 		mnFile.add(mntmLoadWayPoints);
 		
 		JMenuItem mntmSave = new JMenuItem("Save Waypoints...");
 		mntmSave.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int choice = fc.showSaveDialog(MegaMapManager.this);
 				
 				if(choice == JOptionPane.YES_OPTION)
 				{
 					try 
 					{
 						BufferedWriter outFile;
 						File file = new File(fc.getSelectedFile() + ".wpts");
 						
 						if(!file.exists())
 						{
 							file.createNewFile();
 						}
 						
 						outFile = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
 						viewer.save(outFile);
 						outFile.flush(); 
 						outFile.close();
 					} 
 					catch (IOException e) 
 					{
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 		mnFile.add(mntmSave);
 		
 		JMenuItem mntmExit = new JMenuItem("Exit");
 		mnFile.add(mntmExit);
 		
 		JMenu mnMode = new JMenu("Mode");
 		menuBar.add(mnMode);
 		
 		final JRadioButtonMenuItem rdbtnmntmObstacle = new JRadioButtonMenuItem("Obstacle");
 		buttonGroup.add(rdbtnmntmObstacle);
 		rdbtnmntmObstacle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_MASK));
 		mnMode.add(rdbtnmntmObstacle);
 		
 		final JRadioButtonMenuItem rdbtnmntmLine = new JRadioButtonMenuItem("Line");
 		buttonGroup.add(rdbtnmntmLine);
 		rdbtnmntmLine.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_MASK));
 		mnMode.add(rdbtnmntmLine);
 		
 		final JRadioButtonMenuItem rdbtnmntmWaypoint = new JRadioButtonMenuItem("Waypoint");
 		buttonGroup.add(rdbtnmntmWaypoint);
 		rdbtnmntmWaypoint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.ALT_MASK));
 		mnMode.add(rdbtnmntmWaypoint);
 		
 		final JRadioButtonMenuItem rdbtnmntmDistance = new JRadioButtonMenuItem("Distance");
 		buttonGroup.add(rdbtnmntmDistance);
 		rdbtnmntmDistance.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.ALT_MASK));
 		rdbtnmntmDistance.setSelected(true);
 		mnMode.add(rdbtnmntmDistance);
 		
 		JMenu menu_GPS = new JMenu("GPS");
 		menuBar.add(menu_GPS);
 		
 		JMenuItem menuItem_connect = new JMenuItem("Connect", KeyEvent.VK_T);
 		menuItem_connect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
 		
		JMenuItem menuItem_addGPSWP = new JMenuItem("Add", KeyEvent.VK_A);
		menuItem_addGPSWP.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
 		
 		menu_GPS.add(menuItem_connect);
 		menu_GPS.add(menuItem_addGPSWP);
 		
 		menuItem_addGPSWP.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(gpsReady)
 				{
 				  	tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, "High", actualGPSData.getLatitude(), actualGPSData.getLongitude()});
 				  	viewer.setWaypoints(tableModel.toWaypointList());
 				}
 			}
 		});
 		
 		// GPS -> Connect
 		menuItem_connect.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				showGPSPrompt();
 			}
 		});
 		
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(new BorderLayout(0, 0));
 		
 		ItemListener radioListener = new ItemListener()
 		{
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if( e.getItem().equals(rdbtnmntmLine) && e.getStateChange() == ItemEvent.SELECTED)
 				{
 					viewer.setMode(EditMode.Line);
 				}
 				else if(e.getItem().equals(rdbtnmntmObstacle) && e.getStateChange() == ItemEvent.SELECTED)
 				{
 					viewer.setMode(EditMode.Obstacle);
 				}
 				else if(e.getItem().equals(rdbtnmntmWaypoint) && e.getStateChange() == ItemEvent.SELECTED)
 				{
 					viewer.setMode(EditMode.Waypoint);
 				}
 				else if(e.getItem().equals(rdbtnmntmDistance) && e.getStateChange() == ItemEvent.SELECTED)
 				{
 					viewer.setMode(EditMode.Measuring);
 				}
 			}
 		};
 		rdbtnmntmLine.addItemListener(radioListener);
 		rdbtnmntmObstacle.addItemListener(radioListener);
 		rdbtnmntmWaypoint.addItemListener(radioListener);
 		rdbtnmntmDistance.addItemListener(radioListener);
 		
 		JPanel mapPanel = new JPanel();
 		contentPane.add(mapPanel, BorderLayout.CENTER);
 		mapPanel.setLayout(new BorderLayout(0, 0));
 		mapPanel.setBackground(Color.BLACK);
 		
 		mapPanel.add(viewer, BorderLayout.CENTER);
 		
 		scrollPane = new JScrollPane();
 		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setPreferredSize(new Dimension(200, 100));
 		contentPane.add(scrollPane, BorderLayout.WEST);
 		 
 		initializeDataTable();
 	}
 	
 	private void initializeDataTable()
 	{
 		tableModel = new WaypointTableModel(new Object[] {"#", "Priority", "Lon", "Lat"}, 0);
 		tableModel.addTableModelListener(new TableModelListener() 
 		{
 			@Override
 			public void tableChanged(TableModelEvent e) 
 			{
 				viewer.setWaypoints(tableModel.toWaypointList());
 				viewer.repaint();
 			}
 		});
 		
 		waypointsList = new JTable(tableModel)
 		{
 			@Override
 			public boolean isCellEditable(int row, int column) 
 			{
 				return column != 0;
 			}
 			
 			@Override
             public TableCellEditor getCellEditor(int row, int column)
             {
                 int modelColumn = convertColumnIndexToModel( column );
 
                 if (modelColumn == 1)
                 {
                 	return priorityCell;
                 }
                 else
                 {
                 	return super.getCellEditor(row, column);
                 }
             }
         };
         waypointsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         waypointsList.setDragEnabled(true);
         waypointsList.setDropMode(DropMode.INSERT_ROWS);
         waypointsList.setTransferHandler(new TableRowTransferHandler(waypointsList));
         
         scrollPane.setViewportView(waypointsList);
 	}
 	
 	public void showGPSPrompt()
 	{
 		GPSConnectPrompt prompt = new GPSConnectPrompt(this, gps);
 		prompt.setVisible(true);
 		gpsReady = prompt.isGPSConnected();
 		updateTitle();
 	}
 	
 	public void updateTitle(){
 		String title = "Map manager";
 		
 		if (gpsReady)
 			title += " : GPS connected";
 		else
 			title += " : GPS not connected";
 		
 		this.setTitle(title);
 	}
 	
 	public void addGPSData(GPSData data)
 	{
 		gpsFIFO.add(data);
 		if (gpsFIFO.size() >= MEDIAN_FIFO_SIZE ){
 			gpsFIFO.remove();
 		}
 		
 		gpsMeanBuffer.add(getMedian(gpsFIFO));
 
 		if (gpsMeanBuffer.size() >= MEAN_BUFFER_SIZE){
 			gpsMeanBuffer.remove();	
 		}
 		
 		actualGPSData = getMean(gpsMeanBuffer);
 		roundGPSData(actualGPSData);
 	}
 	
 	private void dumpQueue(LinkedBlockingQueue<GPSData> q){
 		GPSData[] buff = new GPSData[q.size()];
 		q.toArray(buff);
 		for (GPSData d:buff){
 			System.out.println(getGPSString(d));
 		}
 	}
 	
 	private String getGPSString(GPSData d){
 		return "long: " + d.getLongitude() + ", lat:" + d.getLatitude(); 
 	}
 
 	private void roundGPSData(GPSData d){
 
 		d.lla.setLongitude(Double.parseDouble(String.format((Locale) null,"%.7f" ,d.getLongitude())));
 		d.lla.setLatitude(Double.parseDouble(String.format((Locale) null,"%.7f" ,d.getLatitude())));
 	}
 
 	public GPSData getMedian(LinkedBlockingQueue<GPSData> queue){
 		GPSData data = null;
 		GPSData[] buffer = new GPSData[queue.size()];
 		double[] sortedLongitude = new double[queue.size()];
 		double[] sortedLatitude = new double[queue.size()];
 		
 		queue.toArray(buffer);
 		
 		for (int i=0; i<queue.size(); i++)
 		{
 			sortedLongitude[i] 	= buffer[i].getLongitude();
 			sortedLatitude[i] 	= buffer[i].getLatitude();
 		}
 		
 		if (queue.size() > 0){	
 			Arrays.sort(sortedLongitude);
 			Arrays.sort(sortedLatitude);
 			data = new GPSData(sortedLongitude[(queue.size() - 1) / 2], sortedLatitude[(queue.size() - 1) / 2]);
 		}
 		else
 			data = new GPSData(0,0);
 		
 		return data;
 	}
 	
 	public GPSData getMean(LinkedBlockingQueue<GPSData> queue){
 		GPSData data = null;
 		double longitudeMean 	= 0, 
 				latitudeMean 	= 0;
 		GPSData[] buffer = new GPSData[queue.size()];
 		queue.toArray(buffer);
 		
 		for (GPSData d: buffer){
 			longitudeMean 	+= d.getLongitude();
 			latitudeMean 	+= d.getLatitude();
 		}
 
 		
 		if (queue.size() > 0){
 			longitudeMean /= queue.size();
 			latitudeMean /= queue.size();
 		}	
 		
 		data = new GPSData(longitudeMean, latitudeMean);
 			
 		return data;
 	}	
 }
