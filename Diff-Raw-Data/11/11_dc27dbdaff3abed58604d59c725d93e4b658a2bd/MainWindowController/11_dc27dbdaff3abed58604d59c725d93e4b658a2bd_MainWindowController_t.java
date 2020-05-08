 package chalmers.dax021308.ecosystem.controller;
 
 
 import java.awt.Dimension;
import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.naming.event.NamingExceptionEvent;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 import chalmers.dax021308.ecosystem.controller.mapeditor.MapEditorController;
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 import chalmers.dax021308.ecosystem.model.environment.IModel;
 import chalmers.dax021308.ecosystem.model.environment.SimulationSettings;
 import chalmers.dax021308.ecosystem.model.util.Log;
 import chalmers.dax021308.ecosystem.view.MainWindow;
 import chalmers.dax021308.ecosystem.view.chart.ChartProvider;
 
 /**
  * Controller class
  * 
  * @author Henrik Ernstsson
  */
 public class MainWindowController implements IController {
 
 	private EcoWorld model;
 	private MainWindow window;
 	//private NewSimulationController newSim;
 	
 	private ActionListener listenerStartNewSimButton = new ActionListener() {
 		public void actionPerformed(ActionEvent arg0) {
 			try {
 				Log.v("Starting new simulation. ");
 				//showNewSimulationWindow();
 				startSimulation();
 				
 			} catch (IllegalStateException ex) {
 				Log.v("EcoWorld already stopped");
 			}
 		}
 	};
 	public MainWindowController() {
 		init();
 		window.smvc.init();
		window.setExtendedState(Frame.MAXIMIZED_BOTH);
 	}
 
 	@Override
 	public void init() {
 		Dimension d = new Dimension(1300, 1300);
 		d.height = d.height - 40;
 		this.model = new EcoWorld();
 		this.window = new MainWindow(model);
 		window.setVisible(true);
 		window.setBtnStartNewSimWindowActionListener(listenerStartNewSimButton);
 		addActionListeners();
 		//showNewSimulationWindow();	
 		window.mntmMapEditor.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				new MapEditorController();
 			}
 		});
 	}
 	
 	private void addActionListeners() {
 		window.mntmLoad.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JFileChooser  fc = new JFileChooser();
 				int ret = fc.showOpenDialog(window);
 				if(ret == JFileChooser.APPROVE_OPTION) {
 					File selectedFile = fc.getSelectedFile();
 					if(selectedFile != null) {
 						Log.v(selectedFile.toString());
 						if(!model.loadRecordedSimulation(selectedFile)) {
 							JOptionPane.showMessageDialog(window, "Failed to load simulation file.");
 						} else {
 							model.playRecordedSimulation();
 						}
 					}
 				}
 			}
 		});
 		
 		window.mntmSave.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JFileChooser  fc = new JFileChooser();
 				//File selectedFile = null;//Get file from somewhere.
 				//fc.setSelectedFile(selectedFile);
 				int ret = fc.showSaveDialog(window);
 				if(ret == JFileChooser.APPROVE_OPTION) {
 					File savedFileAs = fc.getSelectedFile();
 					if(!model.saveRecordingToFile(savedFileAs))  {
 						JOptionPane.showMessageDialog(window, "Failed to save recorded simulation file.");
 					} 
 				}
 			}
 		});
 		
 		window.mntmExit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 			}
 		});
 	}
 
 	@Override
 	public void release() {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void setModel(IModel m) {
 		// TODO Auto-generated method stub
 	}
 	
 	/*
 	public void showNewSimulationWindow() {
 		if(newSim == null) {
 			newSim = new NewSimulationController();
 			newSim.setModel(model);
 			newSim.init();
 		} else {
 			newSim.init();
 		}
 	}
 	*/
 	
 	private void startSimulation() {
 		try {
 			try {
 				model.stop();
 			} catch (IllegalStateException e) {
 			}
 			SimulationSettings s = window.smvc.getSimSettings();
 			s.saveToFile();
 			model.loadSimulationSettings(s);
 			// TODO: Uppdatera LiveSettingsgrejen?
 			try {
 				model.start();
 			} catch (IllegalStateException e) {
 				Log.v(e.toString());
 			}
 		} catch (Exception e) {
 			//view.showErrorMessage();
 			System.out.println("fel gick det");
 			e.printStackTrace();
 		}
 	}
 
 }
