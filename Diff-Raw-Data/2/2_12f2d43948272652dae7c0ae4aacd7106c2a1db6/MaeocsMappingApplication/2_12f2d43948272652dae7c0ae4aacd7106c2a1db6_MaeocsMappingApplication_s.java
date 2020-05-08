 package view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JColorChooser;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
import com.sun.medialib.mlib.Image;

 import model.SectionType;
 import model.State;
 
 public class MaeocsMappingApplication extends JFrame {
  
 	private JFrame principal = this;
 	
 	private JFrame mapa;
 	
 	private JFrame tool;
 	
 	private JFrame create;
 	
 	private JFrame properties;
 		
 	private MapGraphicsPanel grid;
 	 
 	private TypeCreatorGraphicsPanel createPane;
 	
 	private ToolsGraphicsPanel toolsPane;
 	
 	private PropertyGraphicsPanel propertiesPane; 
 	 
 	private JButton processMap;
 	
 	private Boolean pressed = false;
 	
 	private Color gridColor;
 	
 	private String path = File.separator+"tmp";
 	private JFileChooser loadFile = new JFileChooser(new File(path));
 	private File imgFile;
 	 
 	
 	/*
 	 * Colors
 	 * */
 	private final Color black = new Color(0, 0, 0);
 	private final Color grayblack = new Color(50, 50, 50);
 	private final Color white = new Color(255, 255, 255);
 	private final Color blue = new Color(114, 159, 207);
 	private final Color green = new Color(138, 226, 52);
 	private final Color orange = new Color(252, 175, 62);
 	private final Color purple = new Color(173, 127, 168);
 	private final Color yellow = new Color(252, 233, 79);
 	
 	/*
 	 * Dimensiones
 	 * 
 	 * */
 	
 	Dimension principalDim = new Dimension (1200,50);
 	Dimension createDim = new Dimension (250,200);
 	Dimension propertiesDim = new Dimension (250,250);
 	Dimension toolsDim = new Dimension (1200,200);
 	Dimension mapDim;
 
 	private State state;
 	
 	private MaeocsMappingApplication (){
 		super("MAEOCS Mapping Application");
 		this.setSize(principalDim);
 		this.setLocation(10, 10);
 		this.setBackground(black);
 		this.setForeground(white);
 		this.setMaximumSize(principalDim);
 		this.setMinimumSize(principalDim);
 		this.interfaceGenerator();
 		this.setResizable(false);
 		this.setVisible(true);
 	}
 	
 	public void interfaceGenerator (){
 		
 		/*
 		 * create the menu
 		 * 
 		 * */
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         // Creates a menubar for a JFrame
         JMenuBar menuBar = new JMenuBar();
         menuBar.setBackground(white);
         menuBar.repaint();
         // Add the menubar to the frame
         setJMenuBar(menuBar);
         
         // Define and add two drop down menu to the menubar
         JMenu fileMenu = new JMenu("File");
         fileMenu.setForeground(black);
         fileMenu.setBackground(white);
         
         JMenu editMenu = new JMenu("Edit");
         editMenu.setForeground(black);
         editMenu.setBackground(white);
         
         JMenu createMenu = new JMenu("Create");
         editMenu.setForeground(black);
         editMenu.setBackground(white);
         
         JMenu viewMenu = new JMenu("View");
         editMenu.setForeground(black);
         editMenu.setBackground(white);
         
         menuBar.add(fileMenu);
         menuBar.add(editMenu);
         menuBar.add(createMenu);
         menuBar.add(viewMenu);
         
         // Create and add simple menu item to one of the drop down menu
         JMenuItem newAction = new JMenuItem("New");
         newAction.setForeground(black);
         newAction.setBackground(white);
         JMenuItem openAction = new JMenuItem("OpenImage");
         openAction.setForeground(black);
         openAction.setBackground(white);
         JMenuItem exitAction = new JMenuItem("Exit");
         exitAction.setForeground(black);
         exitAction.setBackground(white);
         JMenuItem cutAction = new JMenuItem("Cut");
         cutAction.setForeground(black);
         cutAction.setBackground(white);
         JMenuItem copyAction = new JMenuItem("Copy");
         copyAction.setForeground(black);
         copyAction.setBackground(white);
         JMenuItem pasteAction = new JMenuItem("Paste");
         pasteAction.setForeground(black);
         pasteAction.setBackground(white);
         
         //add the items to the menu item
         
         fileMenu.add(newAction);
         fileMenu.add(openAction);
         fileMenu.addSeparator();
         fileMenu.add(exitAction);
         editMenu.add(cutAction);
         editMenu.add(copyAction);
         editMenu.add(pasteAction);
 		
         this.setJMenuBar(menuBar);
         
         //creating the panels
         this.state = new State();
 
         createPane = new TypeCreatorGraphicsPanel();
         createPane.setBackground(white);
         createPane.setMaximumSize(createDim);
         createPane.setMinimumSize(createDim);
         createPane.setLayout(new GridLayout(1,4));
         
         propertiesPane = new PropertyGraphicsPanel();
         
         toolsPane = new ToolsGraphicsPanel();
         toolsPane.setSize(createDim);        
         
         create = new JFrame("CREATE");
         
         mapa = new JFrame ("MAPA");
         
         tool = new JFrame ("TOOLS");
         
         properties = new JFrame ("PROPERTIES");
 
         //set action listeners
         
         openAction.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				loadFile.showOpenDialog(principal);
 				imgFile = loadFile.getSelectedFile();
 				
 			}
 		});
 
         newAction.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				final newWindow sizeWindow =new newWindow(principal);
 				
 				sizeWindow.setOkActionListener(new ActionListener() {
 					
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						
 						
 						if(sizeWindow.getWidhtSize()>100&& sizeWindow.getHeightSize()>100&&sizeWindow.getGrind()>10){
 							
 							grid = MapGraphicsPanel.getInstance(sizeWindow.getWidhtSize(),
 							sizeWindow.getHeightSize(), sizeWindow.getGrind(), state);
 							
 							mapDim = new Dimension (sizeWindow.getWidhtSize(),sizeWindow.getHeightSize());
 							
 							create.setLocation(660, 60);
 							create.getContentPane().setBackground(white);
 							create.setForeground(black);
 							create.setMaximumSize(createDim);
 							create.setMinimumSize(createDim);
 							create.setResizable(false);
 							create.setBackground(white);
 							create.getContentPane().add(createPane);
 							create.setEnabled(true);
 					        
 							
 					        mapa.setLocation(10, 100);
 					        mapa.getContentPane().setBackground(white);
 					        mapa.setForeground(black);
 					        mapa.setMaximumSize(new Dimension(sizeWindow.getWidhtSize(),sizeWindow.getHeightSize()));
 					        mapa.setMinimumSize(new Dimension(sizeWindow.getWidhtSize(),sizeWindow.getHeightSize()));
 					        mapa.setResizable(true);
 					        mapa.setBackground(white);
 					        mapa.getContentPane().add(grid);
 					        mapa.setEnabled(true);
 							
 					        tool.setLocation(665, 100);
 					        tool.setBackground(white);
 					        tool.setForeground(black);
 					        tool.setMaximumSize(toolsDim);
 					        tool.setMinimumSize(toolsDim);
 					        tool.setResizable(false);
 					        tool.setBackground(white);
 					        tool.getContentPane().add(toolsPane);
 					        
 					        properties.setLocation(10, 665);
 					        properties.setBackground(white);
 					        properties.setForeground(black);
 					        properties.setMaximumSize(propertiesDim);
 					        properties.setMinimumSize(propertiesDim);
 					        properties.setResizable(false);
 					        properties.setBackground(white);
 					        properties.getContentPane().add(propertiesPane);
 					        
 					        createPane.setRoadActionListener(new ActionListener() {
 								
 								@Override
 								public void actionPerformed(ActionEvent arg0) {
 									state.setStateType(SectionType.ROAD);									
 								}
 							});
 					        
 					        createPane.setNuloActionListener(new ActionListener() {
 								
 								@Override
 								public void actionPerformed(ActionEvent arg0) {
 									state.setStateType(SectionType.NULL);									
 								}
 							});
 
 							createPane.setPointActionListener(new ActionListener() {
 								
 								@Override
 								public void actionPerformed(ActionEvent arg0) {
 									state.setStateType(SectionType.POINT);									
 								}
 							});
 					        
 					        propertiesPane.setColorBtAction(new ActionListener() {
 								
 								@Override
 								public void actionPerformed(ActionEvent arg0) {
 								    gridColor
 								      = JColorChooser.showDialog(propertiesPane,
 								                                 "Choose Section Map Color",
 								                                 getBackground());
 								    if (gridColor != null){
 								    	propertiesPane.setLabelColor(gridColor);
 								    	state.setActualColor(gridColor);
 								    }
 									
 								}
 							});
 					        
 					        tool.setVisible(true);
 					        mapa.setVisible(true);
 					        create.setVisible(true);
 					        properties.setVisible(true);
 					        
 					        
 							java.awt.Image img = new ImageIcon(imgFile.getAbsolutePath()).getImage();
 							Graphics g=grid.getGraphics();
 							g.drawImage(img, 0, 0, null);
 					        
 					        
 					        sizeWindow.dispose();
 						}
 						
 					}
 				});
 			}
 		});
 	}
 	
 	
 	public static void main(String[] args) {
 		new MaeocsMappingApplication();
 	}
 
 	public void setMapEnabled(boolean b) {
 		this.mapa.setEnabled(b);
 	}
 	
 }
