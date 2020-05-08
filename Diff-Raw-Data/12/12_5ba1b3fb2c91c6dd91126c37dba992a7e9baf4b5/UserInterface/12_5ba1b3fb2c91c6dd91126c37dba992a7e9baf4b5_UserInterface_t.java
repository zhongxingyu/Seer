 package frontend;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 
 import util.Logger;
 import backend.HeightMap;
 import backend.Simulation;
 
 public class UserInterface extends JFrame {
 	
 	Logger log = new Logger(UserInterface.class, System.out, System.err);
 	JPanel toolbar;// control, menu, viewPort, viewPortControl;
 	JButton modeSelect, modePrey, modePredator, modeModifier, modeObstacle, modeLoad, modeRandom, startStop;
 	JFileChooser fc;
 	
 	PropertiesPanel properties;
 	
 	JMenuBar menubar;
 	JMenu file, view;
 	JMenuItem fileLoadTerrain, fileGenerateRandomTerrain, fileExit;
 	JCheckBoxMenuItem viewGrid, viewAxes;
 	
 	File terrainFile;
 	
 	Simulation sim;
 	Canvas canv;
 	
 	StatusBar status;
 	
 	public UserInterface(final Simulation sim) throws Exception	{
 		super("Swarm AI");
 		this.sim = sim;
 		
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
 		
 		menubar = new JMenuBar();
 			file = new JMenu("File");
 				file.add(fileLoadTerrain);
 				file.add(fileGenerateRandomTerrain);
 				file.addSeparator();
 				file.add(fileExit);
 			view = new JMenu("View");
 				view.add(viewGrid);
 				view.add(viewAxes);
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
 }
 
