 package chalmers.dax021308.ecosystem.view;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.TrayIcon.MessageType;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.beans.PropertyChangeEvent;
 import java.io.File;
 
 
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.filechooser.FileFilter;  
 
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 import chalmers.dax021308.ecosystem.model.util.Log;
 import chalmers.dax021308.ecosystem.view.chart.AbstractGraph2D;
 import chalmers.dax021308.ecosystem.view.chart.IterationTimeGraph;
 import chalmers.dax021308.ecosystem.view.chart.PopulationAmountGraph;
 
 /**
  * The view that holds the entire application.
  * 
  * @author Hanna
  *
  */
 
 public class MainWindow extends JFrame implements IView {
 	private static final long serialVersionUID = -8023060073777907757L;
 	private JPanel contentPane;
 	private ParameterView parameterView; 
 	private ControlView controlView;
 //	private JPanel simulationPanel = new JPanel();
 	private JPanel left = new JPanel();
 	private JPanel right = new JPanel();
 	private SettingsMenuView smv = new SettingsMenuView(this);
 	private AWTSimulationView awt;
 	private OpenGLSimulationView openGL;
 	private HeatMapView heatMap;
 	private AbstractGraph2D graphView1;
 	private AbstractGraph2D graphView2;
 	
 
 	/**
 	 * Create the frame.
 	 */
 	public MainWindow(final EcoWorld model) {
 		setTitle("Simulated Ecosystem");
 		setIconImage(new ImageIcon("res/Simulated ecosystem icon.png").getImage());
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 613, 516);
 		this.setExtendedState(MAXIMIZED_BOTH);
 		//OpenGL   
 	    Dimension d = model.getSize();
 		openGL = new OpenGLSimulationView(model, d, true);
 		openGL.init();
 		//openGL.setSize(new Dimension(980,700));
 		heatMap = new HeatMapView(model, d, 11, "Deers");
 		parameterView = new ParameterView(model);
 		//
 		controlView = new ControlView(model);
 		graphView1 = new PopulationAmountGraph(model, 10);
 		graphView2 = new IterationTimeGraph(model, 10);
 		
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		
 		JMenu mnFile = new JMenu("File");
 		menuBar.add(mnFile);
 		
 
 		
 		JMenuItem mntmLoad = new JMenuItem("Load simulation");
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
 		
 		JMenuItem mntmSave = new JMenuItem("Save simulation");
 		mnFile.add(mntmSave);
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
 		
 		JMenuItem mntmExit = new JMenuItem("Exit");
 
 		//TODO: MOve this to controller.
 		mntmExit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 			}
 		});
 		mnFile.add(mntmExit);
 		
 		JMenu mnControls = new JMenu("Controls");
 		menuBar.add(mnControls);
 		
 		JMenuItem mntmStart = new JMenuItem("Start");
 		mnControls.add(mntmStart);
 		
 		JMenuItem mntmStop = new JMenuItem("Stop");
 		mnControls.add(mntmStop);
 		
 		JMenuItem mntmPause = new JMenuItem("Pause");
 		mnControls.add(mntmPause);
 		
 		JMenu mnSettings = new JMenu("Settings");
 		menuBar.add(mnSettings);
 		
 		JMenuItem mntmSimulationSettings = new JMenuItem("Simulation settings");
 		//Only this in this class should be in the Controller
 		mntmSimulationSettings.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				smv.setVisible(true);
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
 		left.add(openGL);
 		left.add(controlView, BorderLayout.SOUTH);  
 		//right.add(parameterView, BorderLayout.CENTER);
 		//graphView1.setMinimumSize(new Dimension(500, 400));
 		//graphView1.setPreferredSize(new Dimension(500, 400));
 		right.add(graphView2, BorderLayout.CENTER); // during development.
 		right.add(graphView1, BorderLayout.CENTER);
 		right.add(heatMap, BorderLayout.CENTER); 
 //		right.setBackground(Color.BLUE);
 //		parameterView.setBackground(Color.GREEN);
 		
 		contentPane.add(left, BorderLayout.CENTER);
 		contentPane.add(right, BorderLayout.EAST);
 		addWindowListener(new WindowListener() {
 			
 			@Override
 			public void windowOpened(WindowEvent arg0) {}
 			@Override
 			public void windowIconified(WindowEvent arg0) {}
 			@Override
 			public void windowDeiconified(WindowEvent arg0) {}
 			@Override
 			public void windowDeactivated(WindowEvent arg0) {}
 			@Override
 			public void windowClosing(WindowEvent arg0) {
 				//Try to shutdown all worker threads.
 				model.shutdownNow();
 			}
 			@Override
 			public void windowClosed(WindowEvent arg0) {}
 			@Override
 			public void windowActivated(WindowEvent arg0) {}
 		});
 		//contentPane.add(graphView2);
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void init() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void addController(ActionListener controller) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onTick() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void release() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void setSimulationPanel(int i) {
 		if(i == 0) {
 //			simulationPanel.add(awt);
 		}
 		else if(i == 1) {
 //			simulationPanel.add(openGL);
 		}
 	}
 
 	public void setBtnStartNewSimWindowActionListener(ActionListener a) {
 		controlView.btnStartNew.addActionListener(a);		
 	}
 	
 	private class SimFileFilter extends FileFilter{
 		
 		@Override
 		public boolean accept(File f) {
			  return f.getName().toLowerCase().endsWith(".sim");  
 		}
 		
 
 		@Override
 		public String getDescription() {
 			  return ".sim files"; 
 		}
 	};
 }
