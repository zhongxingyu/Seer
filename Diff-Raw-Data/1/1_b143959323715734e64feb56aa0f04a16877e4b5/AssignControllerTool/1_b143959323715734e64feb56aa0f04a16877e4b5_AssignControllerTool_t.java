 package ussr.builder.controllerReassignmentTool;
 
 import java.lang.reflect.Method;
 import com.jme.scene.Geometry;
 
 import ussr.aGui.tabs.view.AssignBehaviorsTab;
 import ussr.model.Module;
 import ussr.physics.jme.JMEModuleComponent;
 import ussr.physics.jme.pickers.CustomizedPicker;
 
 /**
  * The main responsibility of this class is to assign specific controller
  * passed as a string in format:"package + class name" to  module selected in simulation
  * environment. The precondition is that controller has the method called "activate()" implemented.
  * This method is going to be invoked. 
  * @author Konstantinas
  */
 public class AssignControllerTool extends CustomizedPicker  {
 
 	
 	/**
 	 * The path (directory) where the controller can be located. In format:package + class name.
 	 */
 	private String packageClassName;
 	
 	/**
 	 * The method name to execute in the controller
 	 */
 	private final static String methodToAccess = "activate";
 	
 	public AssignControllerTool(String packageClassName){
 		this.packageClassName = packageClassName;
 	}	
 	
 	/*  Method executed when the module is selected with the left side of the mouse in simulation environment. 
 	 *  In this case locates specific class in specific package and invokes method called "activate" in it.
 	 * @see ussr.physics.jme.pickers.CustomizedPicker#pickModuleComponent(ussr.physics.jme.JMEModuleComponent)
 	 */
 	protected void pickModuleComponent(JMEModuleComponent component) {
 		Module selectedModule = component.getModel();	
 			try {				
 				Class clas = Class.forName(packageClassName);
 				Object controller = clas.newInstance();
 				Class[] typs = new Class[]{Module.class}; 
 				Method method =clas.getMethod(methodToAccess, typs);
 				method.invoke(controller, selectedModule);				
 			}catch (Throwable e) {				
 				throw new Error ("For package and class named as: "+packageClassName+", appeared exception called: "+ e.toString() );
 			}
 			/*IS IT A GOOD PLACE FOR YOU?*/
 			
 			AssignBehaviorsTab.getJLabel10005().setVisible(true);
 			AssignBehaviorsTab.getJLabel10005().setText("Controller was assigned successfully to module with ID: "+selectedModule.getID());
			AssignBehaviorsTab.getJLabel1000().setVisible(true);
 	}
 
 	/* Method executed when the module is selected with the left side of the mouse in simulation environment.
 	 * Here not used, because it is enough of pickModuleComponent(JMEModuleComponent component) method (look above).
 	 * @see ussr.physics.jme.pickers.CustomizedPicker#pickTarget(com.jme.scene.Spatial)
 	 */
 	@Override
 	protected void pickTarget(Geometry target) {		
 	}	
 }
