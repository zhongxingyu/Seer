 package frontend;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.HashSet;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 
 import math.Vec;
 
 import util.Logger;
 import backend.HeightMap;
 import backend.Simulation;
 import backend.environment.Element;
 import backend.environment.Prey;
 
 public class UserInterface extends JFrame {
 	
 	Logger log = new Logger(UserInterface.class, System.out, System.err);
 	JPanel toolbar;// control, menu, viewPort, viewPortControl;
 	JButton modeSelect, modePrey, modePredator, modeModifier, modeObstacle, modeLoad, modeRandom, startStop;
 	JFileChooser fc;
 	
 	PropertiesPanel properties;
 	
 	JMenuBar menubar;
 	JMenu file, view;
 	JMenuItem fileLoadTerrain, fileGenerateRandomTerrain, fileExit;
 	JCheckBoxMenuItem viewGrid, viewAxes, viewMap;
 	
 	File terrainFile;
 	
 	Simulation sim;
 	Canvas canv;
 	
 	HashSet<Element> selection;
 	
 	StatusBar status;
 	
 	public UserInterface(final Simulation sim) throws Exception	{
 		super("Swarm AI");
 		this.sim = sim;
 		
 		selection = new HashSet<Element>();
 		
 		//set up things
 		setSize(800, 600);
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		
 		canv = new Canvas(this);
 		
 		status = new StatusBar();
 		
 		fc = new JFileChooser("./maps/");
 
 		properties = new PropertiesPanel();
 		
 		
 		modeSelect = new JButton("Select");
 		modeSelect.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae)	{
 				status.setMode("Selecting");
 			}
 		});
 		modePrey = new JButton("Prey");
 		modePrey.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae)	{
 				status.setMode("Placing prey");
 			}
 		});
 		modePredator = new JButton("Predator");
 		modePredator.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae)	{
 				status.setMode("Placing predator");
 			}
 		});
 		modeModifier = new JButton("Modifier");
 		modeModifier.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae)	{
 				status.setMode("Placing modifier");
 			}
 		});
 		modeObstacle = new JButton("Obstacle");
 		modeObstacle.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae)	{
 				status.setMode("Placing obstacle");
 			}
 		});
 		
 		fileLoadTerrain = new JMenuItem("Load Terrain");
 		fileLoadTerrain.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae)	{
 				if (!sim.isRunning)
 				{
 					status.setMode("Select Terrain");
 					int returnVal = fc.showOpenDialog(UserInterface.this);
 					if (returnVal == JFileChooser.APPROVE_OPTION) {
 			            terrainFile = fc.getSelectedFile();
 			            sim.loadHeightMap(terrainFile);
 			            canv.hmc.setHeightMap(sim.hm);
 			            log.info("Opening: " + terrainFile.getName());
 			            
 			        }
 					status.setMode("");
 				}
 				else
 				{
 					status.setMode("Can't change terrain while simulation is running!");					
 				}
 			}
 		});
 		
 		fileGenerateRandomTerrain = new JMenuItem("Generate Random Terrain");
 		fileGenerateRandomTerrain.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae)	{
 				if (!sim.isRunning)
 				{
 					status.setMode("Generating Random Terrain");
 					sim.setHeightMap(new HeightMap());
 					canv.hmc.setHeightMap(sim.hm);
 					status.setMode("");
 				}
 				else
 				{
 					status.setMode("Can't change terrain while simulation is running!");
 				}
 		}});
 		
 		fileExit = new JMenuItem("Exit");
 		fileExit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae)	{
 				System.exit(0);
 			}
 		});
 		
 		viewGrid = new JCheckBoxMenuItem("Show Grid", canv.renderGrid);
 		viewGrid.addActionListener(new ActionListener()	{
 			public void actionPerformed(ActionEvent ae)	{
 				canv.renderGrid = viewGrid.isSelected();
 				canv.repaint();
 			}
 		});
 		
 		viewAxes = new JCheckBoxMenuItem("Show Axes", canv.renderAxes);
 		viewAxes.addActionListener(new ActionListener()	{
 			public void actionPerformed(ActionEvent ae)	{
 				canv.renderAxes = viewAxes.isSelected();
 				canv.repaint();
 			}
 		});
 
 		viewMap = new JCheckBoxMenuItem("Show Map", canv.renderHeightMap);
 		viewMap.addActionListener(new ActionListener()	{
 			public void actionPerformed(ActionEvent ae)	{
 				canv.renderHeightMap = viewMap.isSelected();
 				canv.repaint();
 			}
 		});
 		
 		menubar = new JMenuBar();
 			file = new JMenu("File");
 				file.add(fileLoadTerrain);
 				file.add(fileGenerateRandomTerrain);
 				file.addSeparator();
 				file.add(fileExit);
 			view = new JMenu("View");
 				view.add(viewGrid);
 				view.add(viewAxes);
 				view.add(viewMap);
 		menubar.add(file);
 		menubar.add(view);
 		
 		setJMenuBar(menubar);
 		
 		toolbar = new JPanel();
 		toolbar.setLayout(new FlowLayout());
 		
 		getContentPane().setLayout(new BorderLayout());
 		
 		//add things
 		toolbar.add(modeSelect);
 		toolbar.add(modePrey);
 		toolbar.add(modePredator);
 		toolbar.add(modeModifier);
 		toolbar.add(modeObstacle);
 		
 		properties.targetEntity(sim.elements.get(0));
 		
 		JSplitPane sPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
 		sPane.add(properties);
 		
 		getContentPane().add(toolbar, BorderLayout.PAGE_START);
 		getContentPane().add(status, BorderLayout.PAGE_END);
 		
 		JPanel centerThings = new JPanel();
 		centerThings.setLayout(new BorderLayout());
 		centerThings.add(canv, BorderLayout.CENTER);
 		centerThings.add(new ControlBar(sim), BorderLayout.PAGE_END);
 		
 		sPane.add(centerThings);
 		
 		getContentPane().add(sPane, BorderLayout.CENTER);
 		/*PropertyDialog pd = new PropertyDialog(this);
 		pd.targetEntity(sim.elements.get(0));*/
 		
 		setVisible(true);
 		sim.start();
 	}
 
 	public void selectPrey(Vec point) {
 		//Add prey to selection
 		//Colour it differently (green?)
 		boolean added = false;
 		for (Element e : sim.elements)
 		{
			if (e.getPosition().withinRadius(point, e.getSize()))	
 			{
 				selection.clear();
 				selection.add(e);
 				added = true;
 				System.out.println("Added element " + e.getPosition() + " : " + point);
 			}
 			else
 			{
 				System.out.println("Did not add element " + e.getPosition() + " : " + point);
 			}
 		}
 		if (!added)
 			selection.clear();
 		canv.repaint();
 	}
 	
 	public void addToSelection(Vec point) {
 		//Add prey to selection
 		//Colour it differently (green?)
 		for (Element e : sim.elements)
 		{
			if (e.getPosition().withinRadius(point, e.getSize()))	
 			{
 				selection.add(e);
 				System.out.println("Added element " + e.getPosition() + " : " + point);
 			}
 			else
 			{
 				System.out.println("Did not add element " + e.getPosition() + " : " + point);
 			}
 		}
 		canv.repaint();
 	}
 
 	public void placePrey(Vec mPoint) {
 		//convert mPoint to worldspace
 		//place prey on closest dot
 		double xloc = Math.round((mPoint.x/10))*10;
 		double yloc = Math.round(mPoint.y/10)*10;
 		
 		boolean validLocation = true;
 		
 		for (Element e : sim.elements)
 			if (e.getPosition().equals(new Vec(xloc, yloc)))
 				validLocation = false;
 		
 		if (validLocation)
			sim.elements.add(new Prey(xloc,yloc,0,0,5));
 		canv.repaint();
 	}
 
 	public void setPreyDirection(Vec mPoint) {
 		//set all elements in selection's dir to mPoint
 		
 	}
 }
 
