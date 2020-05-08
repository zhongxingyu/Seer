 package view;
 
 import java.lang.Thread.UncaughtExceptionHandler;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTabbedPane;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import model.SimulationEditorModelException;
 import model.entity.FleetEditorModel;
 import model.entity.MapEditorModel;
 import model.entity.SimulationEditorModel;
 import control.FleetEditorController;
 import control.MapEditorController;
 import control.SimulationEditorController;
 
 public class MasterGui extends JFrame {
 	
 	public MasterGui() {
 		this.setSize(1200, 900);
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		this.setLocationRelativeTo(null);
 		
		String lookandFeelerror = "Error initializing system Look and Feel. Fall back to standard.";
		
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException e) {
			System.err.println(lookandFeelerror);
 		} catch (InstantiationException e) {
			System.err.println(lookandFeelerror);
 		} catch (IllegalAccessException e) {
			System.err.println(lookandFeelerror);
 		} catch (UnsupportedLookAndFeelException e) {
			System.err.println(lookandFeelerror);
 		}
 		
 		
 		MapEditorModel mapEditorModel = new MapEditorModel();
 		MapEditorController mapEditorController = new MapEditorController(new MapEditorView(), mapEditorModel);
 		
 		FleetEditorModel fleetEditorModel = new FleetEditorModel();
 		FleetEditorController fleetEditorController = new FleetEditorController(new FleetEditorView(), fleetEditorModel);
 		
 		
 		SimulationEditorModel simulationEditorModel = SimulationEditorModel.getInstance();
 		simulationEditorModel.setMapEditorModel(mapEditorModel);
 		simulationEditorModel.setFleetEditorModel(fleetEditorModel);
 		SimulationEditorController simulationEditorController = new SimulationEditorController(new SimulationEditorView(), simulationEditorModel);
 		
		final JTabbedPane tabPane = new JTabbedPane();

 		tabPane.addTab("Map Editor", mapEditorController.showView());
 		tabPane.addTab("Fleet Editor", fleetEditorController.showView());
 		tabPane.addTab("Simulation Editor", simulationEditorController.showView());
 		
 		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
 
 			public void uncaughtException(Thread t, Throwable e) {
 				if(e instanceof SimulationEditorModelException) {
 					JOptionPane.showMessageDialog(tabPane.getSelectedComponent(), e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
 				} else {
 					JOptionPane.showMessageDialog(tabPane, e.getMessage(), "unexpexted error", JOptionPane.ERROR_MESSAGE);
 					e.printStackTrace();
 				}
 			}
 		});
 
 		
 		this.setContentPane(tabPane);
 	}
 
 }
