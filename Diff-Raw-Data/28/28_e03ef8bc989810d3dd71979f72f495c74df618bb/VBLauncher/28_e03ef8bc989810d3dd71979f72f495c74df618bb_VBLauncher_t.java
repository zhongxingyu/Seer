 package net.gerritk.vehiclebuilder;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Toolkit;
 
 import javax.swing.JFrame;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import net.gerritk.vehiclebuilder.controllers.VehicleBuilderController;
 import net.gerritk.vehiclebuilder.controllers.VehicleChildController;
 import net.gerritk.vehiclebuilder.controllers.VehicleOutputController;
 import net.gerritk.vehiclebuilder.controllers.VehicleSetupController;
 import net.gerritk.vehiclebuilder.models.*;
 import net.gerritk.vehiclebuilder.resources.ResourceLoader;
 
 public class VBLauncher {
 	private static VBLauncher instance;
 	
 	private JFrame frame;
 	private ResourceLoader resourceLoader;
 	
 	private VBLauncher() {
 		instance = this;
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException | InstantiationException
 				| IllegalAccessException | UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		}
 		
 		resourceLoader = new ResourceLoader("resources/items");		
 		
 		frame = new JFrame("Vehicle Builder fr Leitstellenspiel.de");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		// TODO add Controllers, Models & Views
 		CabinModel cabinModel = new CabinModel();
 		StructureModel structureModel = new StructureModel();
 		TemplateModel templateModel = new TemplateModel();
 		ChildModel childModel = new ChildModel();
 		VehicleModel vehicleModel = new VehicleModel(cabinModel, structureModel, templateModel);
 		OutputModel outputModel = new OutputModel();
 		
 		VehicleSetupController vsetupController = new VehicleSetupController(cabinModel, structureModel, 
 				templateModel, childModel, vehicleModel);
 		
 		VehicleChildController vchildController = new VehicleChildController(vehicleModel, outputModel);
 		
 		VehicleOutputController voutputController = new VehicleOutputController(vehicleModel, outputModel);
 		
 		VehicleBuilderController vbuilderController = new VehicleBuilderController(vehicleModel,
 				vsetupController.getVehicleSetupView(), vchildController.getVehicleChildView(),
 				voutputController.getVehicleOutputView());
 		
 		frame.add(vbuilderController.getVehicleBuilderView());
 		
 		outputModel.setScale(10);
 		
 		// Notify ALL
 		cabinModel.notifyObservers();
 		structureModel.notifyObservers();
 		templateModel.notifyObservers();
 		childModel.notifyObservers();
 		vehicleModel.notifyObservers();
 		outputModel.notifyObservers();
 		
 		frame.pack();
 		frame.setMinimumSize(frame.getSize());
 
		outputModel.setScale(5);
		outputModel.notifyObservers();

 		setFrameToScreenCenter();
 		
 		frame.setVisible(true);
 	}
 	
 	public void setFrameToScreenCenter() {
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		Point wp = new Point((int) (screenSize.getWidth() - frame.getWidth()) / 2, (int) (screenSize.getHeight() - frame.getHeight()) / 2);
 		frame.setLocation(wp);
 	}
 	
 	/*
 	 * Getter & Setter
 	 */
 	public JFrame getFrame() {
 		return frame;
 	}
 	
 	public ResourceLoader getResourceLoader() {
 		return resourceLoader;
 	}
 	/*
 	 * Static
 	 */
 	public static VBLauncher getInstance() {
 		return instance;
 	}
 	
 	public static void main(String[] args) {
 		new VBLauncher();
 	}
 }
