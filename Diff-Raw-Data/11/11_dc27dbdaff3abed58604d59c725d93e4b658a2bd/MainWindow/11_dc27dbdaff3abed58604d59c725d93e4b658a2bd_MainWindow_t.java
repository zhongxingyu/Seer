 package chalmers.dax021308.ecosystem.view;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Menu;
 import java.awt.MenuBar;
 import java.awt.MenuItem;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.io.File;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.filechooser.FileFilter;
 
 import chalmers.dax021308.ecosystem.controller.ControlViewController;
 import chalmers.dax021308.ecosystem.controller.LiveSettingsViewController;
 import chalmers.dax021308.ecosystem.controller.NEWSettingsMenuViewController;
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 import chalmers.dax021308.ecosystem.model.util.Log;
 import chalmers.dax021308.ecosystem.view.chart.ChartProvider;
 import chalmers.dax021308.ecosystem.view.chart.IChart;
 
 /**
  * The view that holds the entire application.
  * 
  * @author Hanna
  *
  */
 
 public class MainWindow extends JFrame implements IView {
 	private static final long serialVersionUID = -8023060073777907757L;
 	private JPanel contentPane;
 //	private JPanel simulationPanel = new JPanel();
 	private JPanel left = new JPanel();
 	private JPanel right = new JPanel();
 	private OpenGLSimulationView openGL;
 	private HeatMapView heatMap;
 	private IChart graphView1;
 	private IChart graphView2;
 	
 	public final LiveSettingsViewController parameterViewCtrl; 
 	public final ControlViewController controlViewCtrl;
 	public final NEWSettingsMenuViewController smvc;
 	
 	public final MenuBar menuBar;
 	public final Menu mnFile;
 	public final MenuItem mntmLoad;
 	public final MenuItem mntmSave;
 	public final MenuItem mntmExit;
 	public final Menu mnControls;
 	public final MenuItem mntmStart;
 	public final MenuItem mntmStop;
 	public final MenuItem mntmPause;
 	public final Menu mnSettings;
 	public final Menu mnView;
 	public final MenuItem mntmMapEditor;
 	public final MenuItem mntmSimulationSettings;
 
 	/**
 	 * Create the frame.
 	 */
 	public MainWindow(final EcoWorld model) {
 		setTitle("Simulated Ecosystem");
 		setIconImage(new ImageIcon("res/Simulated ecosystem icon.png").getImage());
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 613, 516);
 		//OpenGL   
 	    Dimension d = model.getSize();
 		openGL = new OpenGLSimulationView(model, d, true);
 		openGL.init();
 		//openGL.setSize(new Dimension(980,700));
 //<<<<<<< HEAD
 		//heatMap = new HeatMapView(model, d, 11, "Deers");
 		controlViewCtrl = new ControlViewController(model);
 		parameterViewCtrl = new LiveSettingsViewController(model);
 //		graphView1 = new PopulationAmountGraph(model, 10);
 //		graphView2 = new IterationTimeGraph(model, 10);
 		smvc = new NEWSettingsMenuViewController(model);
 //=======
 		heatMap = new HeatMapView(model, d, new Dimension(50,50), 3, "Deers");
 //		parameterView = new ParameterView(model);
 		//
 //		controlView = new ControlView(model);
 		graphView1 = ChartProvider.makeChart(ChartProvider.ChartType.POPULATION_AMOUNT_GRAPH, model);
 		graphView2 = ChartProvider.makeChart(ChartProvider.ChartType.GROUPING_PROPORTION_GRAPH, model);
 //>>>>>>> 0a80aae074172894a60ad8cf4ef1d867f0abfd57
 		
 		menuBar = new MenuBar();
 		setMenuBar(menuBar);
 		
 		mnFile = new Menu("File");
 		menuBar.add(mnFile);
 		
 
 		mnView = new Menu("View");
 		mntmMapEditor = new MenuItem("Map Editor");
 		menuBar.add(mnView);
 		mnView.add(mntmMapEditor);
 		
 //<<<<<<< HEAD
 		mntmLoad = new MenuItem("Load simulation");
		setExtendedState(MAXIMIZED_BOTH);
 		
 //=======
 
 		
 //		MenuItem mntmLoad = new MenuItem("Load simulation");
 		//TODO: MOve this to controller.
 		mntmLoad.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JFileChooser  fc = new JFileChooser();
 				fc.setFileFilter(new SimFileFilter());
 				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 				int ret = fc.showOpenDialog(MainWindow.this);
 				if(ret == JFileChooser.APPROVE_OPTION) {
 					File selectedFile = fc.getSelectedFile();
 					if(selectedFile != null) {
 						Log.v(selectedFile.toString());
 						if(!model.loadRecordedSimulation(selectedFile)) {
 							JOptionPane.showMessageDialog(MainWindow.this, "Failed to load simulation file.");
 						} else {
 							model.playRecordedSimulation();
 						}
 					}
 				}
 			}
 		});
 //>>>>>>> 0a80aae074172894a60ad8cf4ef1d867f0abfd57
 		mnFile.add(mntmLoad);
 		
 		mntmSave = new MenuItem("Save simulation");
 		mnFile.add(mntmSave);
 //<<<<<<< HEAD
 	
 		mntmExit = new MenuItem("Exit");
 //=======
 		//TODO: MOve this to controller.
 		mntmSave.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JFileChooser  fc = new JFileChooser();
 				fc.setFileFilter(new SimFileFilter());
 				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 				//File selectedFile = null;//Get file from somewhere.
 				//fc.setSelectedFile(selectedFile);
 				int ret = fc.showSaveDialog(MainWindow.this);
 				if(ret == JFileChooser.APPROVE_OPTION) {
 					File savedFileAs = fc.getSelectedFile();
 					String filePath = savedFileAs.getPath();
 					if(!filePath.toLowerCase().endsWith(".sim")) {
 						savedFileAs = new File(filePath + ".sim");
 					}
 					if(!model.saveRecordingToFile(savedFileAs))  {
 						JOptionPane.showMessageDialog(MainWindow.this, "Failed to save recorded simulation file.");
 					} else {
 						JOptionPane.showMessageDialog(MainWindow.this, "File saved succesfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
 					}
 				}
 			}
 		});
 		
 //		MenuItem mntmExit = new MenuItem("Exit");
 //>>>>>>> 0a80aae074172894a60ad8cf4ef1d867f0abfd57
 
 		
 		mnFile.add(mntmExit);
 		
 		mnControls = new Menu("Controls");
 		menuBar.add(mnControls);
 		
 		mntmStart = new MenuItem("Start");
 		mnControls.add(mntmStart);
 		
 		mntmStop = new MenuItem("Stop");
 		mnControls.add(mntmStop);
 		
 		mntmPause = new MenuItem("Pause");
 		mnControls.add(mntmPause);
 		
 		mnSettings = new Menu("Settings");
 		menuBar.add(mnSettings);
 		
 		mntmSimulationSettings = new MenuItem("Simulation settings");
 		
 		mntmSimulationSettings.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				smvc.init();
 			}
 		});
 		mnSettings.add(mntmSimulationSettings);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		left.setLayout(new BorderLayout(0,0));
 		
 		/*
 		 * Quick fix...
 		 */
 		right.setLayout(new GridLayout(3,1));
 		right.setMinimumSize(new Dimension(500, 400));
 		right.setPreferredSize(new Dimension(500, 400));
 //		GridBagConstraints gbc = new GridBagConstraints();
 //		gbc.gridx = 0;
 //		gbc.gridwidth = gbc.gridheight = 1;
 //		gbc.fill = GridBagConstraints.HORIZONTAL; 
 //		gbc.gridy = GridBagConstraints.RELATIVE; //makes sure every new add is placed beneath the previous
 //		gbc.anchor = GridBagConstraints.PAGE_START;
 		setContentPane(contentPane);
 		
 //		simulationPanel.setSize(d);
 		//simulationPanel.add(openGL);
 //		simulationPanel.setBackground(Color.RED);
 		left.add(parameterViewCtrl.view, BorderLayout.NORTH);
 		left.add(openGL, BorderLayout.CENTER);
 		left.add(controlViewCtrl.view, BorderLayout.SOUTH);  
 		//right.add(parameterView, BorderLayout.CENTER);
 		//graphView1.setMinimumSize(new Dimension(500, 400));
 		//graphView1.setPreferredSize(new Dimension(500, 400));
 		right.add(graphView2.toComponent(), BorderLayout.CENTER); // during development.
 		right.add(graphView1.toComponent(), BorderLayout.CENTER);
 		right.add(heatMap, BorderLayout.CENTER); 
 //		right.setBackground(Color.BLUE);
 //		parameterView.setBackground(Color.GREEN);
 		
 		contentPane.add(left, BorderLayout.CENTER);
 		contentPane.add(right, BorderLayout.EAST);
 //<<<<<<< HEAD
 		addWindowListener(new WindowAdapter() {			
 //=======
 //		addWindowListener(new WindowListener() {
 //			
 //			@Override
 //			public void windowOpened(WindowEvent arg0) {}
 //			@Override
 //			public void windowIconified(WindowEvent arg0) {}
 //			@Override
 //			public void windowDeiconified(WindowEvent arg0) {}
 //			@Override
 //			public void windowDeactivated(WindowEvent arg0) {}
 //>>>>>>> 0a80aae074172894a60ad8cf4ef1d867f0abfd57
 			@Override
 			public void windowClosing(WindowEvent arg0) {
 				//Try to shutdown all worker threads.
 				model.shutdownNow();
 			}
 //<<<<<<< HEAD
 //=======
 //			@Override
 //			public void windowClosed(WindowEvent arg0) {}
 //			@Override
 //			public void windowActivated(WindowEvent arg0) {}
 //>>>>>>> 0a80aae074172894a60ad8cf4ef1d867f0abfd57
 		});
 		//contentPane.add(graphView2);
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		
 	}
 
 	@Override
 	public void init() {
 		
 	}
 
 	@Override
 	public void addController(ActionListener controller) {
 		
 	}
 
 	@Override
 	public void onTick() {
 		
 	}
 
 	@Override
 	public void release() {
 		
 	}
 
 	public void setBtnStartNewSimWindowActionListener(ActionListener a) {
 		controlViewCtrl.view.buttonStartNew.addActionListener(a);		
 	}
 	
 	private class SimFileFilter extends FileFilter{
 		
 		@Override
 		public boolean accept(File f) {
 			  return f.isDirectory() || f.getName().toLowerCase().endsWith(".sim");  
 		}
 		
 
 		@Override
 		public String getDescription() {
 			  return ".sim files"; 
 		}
 	};
 }
