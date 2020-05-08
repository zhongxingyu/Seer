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
 import chalmers.dax021308.ecosystem.controller.NEWSettingsMenuViewController;
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 import chalmers.dax021308.ecosystem.model.util.Log;
 import chalmers.dax021308.ecosystem.view.chart.BottomChartTabs;
 import chalmers.dax021308.ecosystem.view.chart.ChartProvider;
 import chalmers.dax021308.ecosystem.view.chart.IChart;
 import chalmers.dax021308.ecosystem.view.populationsettings.PopulationSettingsDialog;
 
 /**
  * The view that holds the entire application.
  * 
  * @author Hanna
  *
  */
 
 public class MainWindow extends JFrame implements IView {
 	private static final long serialVersionUID = -8023060073777907757L;
 	private JPanel contentPane;
	private JPanel left = new JPanel();
	private JPanel right = new JPanel();
 	private OpenGLSimulationView openGL;
 	private HeatmapTabHolder heatMap;
 	private BottomChartTabs bottomTabbedGraphs;
 	private IChart graphView2;
 	
 //	public final LiveSettingsViewController parameterViewCtrl; 
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
 	public final MenuItem mntmPopulationSettings;
 
 	/**
 	 * Create the frame.
 	 */
 	public MainWindow(final EcoWorld model) {
 		setTitle("Simulated Ecosystem");
 		setIconImage(new ImageIcon("res/Simulated ecosystem icon.png").getImage());
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 613, 516);
 		//OpenGL   
 		openGL = new OpenGLSimulationView(model);
 		openGL.init();
		controlViewCtrl = new ControlViewController(model);
 //		parameterViewCtrl = new LiveSettingsViewController(model);
 		smvc = new NEWSettingsMenuViewController(model, this);
 		heatMap = new HeatmapTabHolder(model);
 		bottomTabbedGraphs = new BottomChartTabs(model);
 		graphView2 = ChartProvider.makeChart(ChartProvider.ChartType.GROUPING_PROPORTION_GRAPH, model);
 		
 		menuBar = new MenuBar();
 		setMenuBar(menuBar);
 		
 		mnFile = new Menu("File");
 		menuBar.add(mnFile);
 		
 
 		mnView = new Menu("View");
 		mntmMapEditor = new MenuItem("Map Editor");
 		menuBar.add(mnView);
 		mnView.add(mntmMapEditor);
 		
 		mntmSimulationSettings = new MenuItem("New Simulation");
 		mntmSimulationSettings.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				smvc.init();
 			}
 		});
 		
 		mnFile.add(mntmSimulationSettings);
 		
 		mntmLoad = new MenuItem("Load Simulation");
 		setExtendedState(MAXIMIZED_BOTH);
 		
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
 		mnFile.add(mntmLoad);
 		
 		mntmSave = new MenuItem("Save Simulation");
 		mnFile.add(mntmSave);
 	
 		mntmExit = new MenuItem("Exit");
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
 		
 		mntmPopulationSettings = new MenuItem("Population Settings");
 		
 //		mnSettings.add(mntmSimulationSettings);
 		mnSettings.add(mntmPopulationSettings);
 		mntmPopulationSettings.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				new PopulationSettingsDialog(MainWindow.this);
 			}
 		});
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		left.setLayout(new BorderLayout(0,0));
 		
 		right.setLayout(new GridLayout(3,1));
 		right.setMinimumSize(new Dimension(500, 400));
 		right.setPreferredSize(new Dimension(500, 400));
 		setContentPane(contentPane);
 		
 //		left.add(parameterViewCtrl.view, BorderLayout.NORTH);
 		left.add(openGL, BorderLayout.CENTER);
		left.add(controlViewCtrl.view, BorderLayout.SOUTH);  
 		right.add(graphView2.toComponent(), BorderLayout.CENTER); // during development.
 		right.add(bottomTabbedGraphs, BorderLayout.CENTER);
 		right.add(heatMap, BorderLayout.CENTER); 
 		
 		contentPane.add(left, BorderLayout.CENTER);
 		contentPane.add(right, BorderLayout.EAST);
 		addWindowListener(new WindowAdapter() {			
 			@Override
 			public void windowClosing(WindowEvent arg0) {
 				//Try to shutdown all worker threads.
 				model.shutdownNow();
 			}
 		});
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
		controlViewCtrl.view.restartButton.addActionListener(a);		
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
