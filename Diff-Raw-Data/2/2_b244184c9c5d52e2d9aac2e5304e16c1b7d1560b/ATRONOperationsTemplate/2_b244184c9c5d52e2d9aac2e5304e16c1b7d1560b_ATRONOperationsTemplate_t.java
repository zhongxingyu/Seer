 package ussr.builder.constructionTools;
 
 import java.awt.Color;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
import ussr.aGui.controllers.MainFrameSeparateController;
 import ussr.builder.enumerations.ATRONTypesModules;
 import ussr.builder.enumerations.OdinTypesModules;
 import ussr.builder.helpers.BuilderHelper;
 import ussr.description.geometry.RotationDescription;
 import ussr.description.geometry.VectorDescription;
 import ussr.description.setup.ModulePosition;
 import ussr.model.Module;
 import ussr.physics.jme.JMESimulation;
 import ussr.remote.facade.RemotePhysicsSimulationImpl;
 import ussr.samples.atron.ATRON;
 
 /**
  * Supports construction of ATRON modular robot morphology in more abstract module oriented way.
  * In general the main responsibility of this class is to create ATRON modules and add default ATRON
  * construction module. 
  * @author Konstantinas
  */
 public class ATRONOperationsTemplate extends CommonOperationsTemplate{
 
 	/**
 	 * Supports construction of ATRON modular robot morphology in more abstract module oriented way.
 	 * @param simulation, the physical simulation.
 	 */
 	public ATRONOperationsTemplate(JMESimulation simulation) {
 		super(simulation);		
 	}
 
 	/**
 	 * Adds default (the first) ATRON construction module at specified position.
 	 * This method is so-called "Primitive operation" for  TEMPLATE method, called "addDefaultConstructionModule(String type, VectorDescription modulePosition)".	 
 	 * @param type, the type of modular robot. In this case it is ATRON.
 	 * @param modulePosition, the position of the ATRON module in simulation environment.
 	 * @param moduleRotation, the rotation of the ATRON module.
 	 * @param colorsComponents, the colors of components constituting the ATRON module.
 	 * @param colorsConectors, the colors of connectors on the ATRON module.
 	 */
 	@Override
 	public void addDefaultModule(String type, VectorDescription modulePosition,	RotationDescription moduleRotation, List<Color> colorsComponents, ArrayList<Color> colorsConectors) {
 		colorsComponents.add(Color.BLUE);colorsComponents.add(Color.RED);
 		colorsConectors.add(Color.BLACK);colorsConectors.add(Color.WHITE);
 		colorsConectors.add(Color.BLACK);colorsConectors.add(Color.WHITE);
 		colorsConectors.add(Color.BLACK);colorsConectors.add(Color.WHITE);
 		colorsConectors.add(Color.BLACK);colorsConectors.add(Color.WHITE);
 		moduleRotation = ATRON.ROTATION_EW;
 		addNewModule(new ModulePosition(type+ BuilderHelper.getRandomInt(),type,modulePosition,moduleRotation),colorsComponents,colorsConectors);		
 	}
 
 	/**
 	 * Creates and returns new copy of ATRON module from the selected module.
 	 * This method is so-called "Primitive operation" for above TEMPLATE method, called "addNewModuleOnConnector(ConstructionToolSpecification toolSpecification)".	 
 	 * @param selectedModule,the module selected in simulation environment.
 	 * @return module, newly created ATRON module.  
 	 */	
 	@Override
 	public Module createNewModule(Module selectedModule) {
 		String userFriendlyModuleType ="";
 		try {
 			userFriendlyModuleType = RemotePhysicsSimulationImpl.getGUICallbackControl().getDefaultConstructionModuleType();
 		} catch (RemoteException e) {
 			throw new Error("Failed  to receive module type, due to remote exception.");
 		}
 		String moduleType = ATRONTypesModules.getModuleTypeFromUserFriendly(userFriendlyModuleType);
 
 		VectorDescription position = selectedModule.getPhysics().get(0).getPosition(); 
 		RotationDescription rotation = selectedModule.getPhysics().get(0).getRotation();	
 
 		List<Color> colorsComponents = selectedModule.getColorList();			
 		ArrayList<Color> colorsConnectors = BuilderHelper.getColorsConnectors(selectedModule);
 		String selectedModuleName = selectedModule.getProperty(BuilderHelper.getModuleNameKey());
 		ModulePosition modulePosition = new ModulePosition(selectedModuleName+ BuilderHelper.getRandomInt(),moduleType,position,rotation);	
 		Module newModule = addNewModule(modulePosition,colorsComponents,colorsConnectors);	
 	
 		return newModule;
 	}
 
 	/**
 	 * Additional method for implementing unique properties of modular robots. Here the functionality
 	 * is repeated, however in future can be expanded.
 	 * This method is so-called "Primitive operation" for TEMPLATE method, called "variateModule(ConstructionToolSpecification toolSpecification)".	    
 	 * @param selectedModule,the module selected in simulation environment.	
 	 */
 	@Override
 	public void variateSpecificModule(Module selectedModule) {		
 		//construction.variateModuleProperties(selectedModule);
 
 		String selectedModuleType =	selectedModule.getProperty(BuilderHelper.getModuleTypeKey());		
 		VectorDescription modulePosition = selectedModule.getPhysics().get(0).getPosition();
 		RotationDescription moduleRotation = selectedModule.getPhysics().get(0).getRotation();
 		List<Color> colorsComponents = selectedModule.getColorList();			
 		ArrayList<Color> colorsConnectors = BuilderHelper.getColorsConnectors(selectedModule);
 
 		String currentModuleType = selectedModule.getProperty(BuilderHelper.getModuleTypeKey());
 
 		ATRONTypesModules newModuleType = null ;
 		if (currentModuleType.equalsIgnoreCase(ATRONTypesModules.ATRON_GENTLE.getModuleType())){
 			newModuleType = ATRONTypesModules.ATRON_RUBBER_RING;
 		}else if (currentModuleType.equalsIgnoreCase(ATRONTypesModules.ATRON_RUBBER_RING.getModuleType())){
 			newModuleType = ATRONTypesModules.ATRON_REALISTIC;
 		}else if (currentModuleType.equalsIgnoreCase(ATRONTypesModules.ATRON_REALISTIC.getModuleType())){
 			newModuleType = ATRONTypesModules.ATRON_SMOOTH;
 		}else if (currentModuleType.equalsIgnoreCase(ATRONTypesModules.ATRON_SMOOTH.getModuleType())){
 			newModuleType = ATRONTypesModules.ATRON_SUPER;
 		}else if (currentModuleType.equalsIgnoreCase(ATRONTypesModules.ATRON_SUPER.getModuleType())){
 			newModuleType = ATRONTypesModules.ATRON_RADIO;
 		}else if (currentModuleType.equalsIgnoreCase(ATRONTypesModules.ATRON_RADIO.getModuleType())){
 			newModuleType = ATRONTypesModules.ATRON_HALF_DUPLEX;
 		}else if (currentModuleType.equalsIgnoreCase(ATRONTypesModules.ATRON_HALF_DUPLEX.getModuleType())){
 			newModuleType = ATRONTypesModules.ATRON_GENTLE;
 		}
 		BuilderHelper.deleteModule(selectedModule,true);
 		addNewModule(new ModulePosition(newModuleType.getModuleType()+BuilderHelper.getRandomInt(),newModuleType.getModuleType(),modulePosition,moduleRotation),colorsComponents,colorsConnectors);
 		try {
 			RemotePhysicsSimulationImpl.getGUICallbackControl().setSelectedDefaultConstructionModule(newModuleType.getUserFriendlyName());
 		} catch (RemoteException e) {
 			throw new Error("Failed to set default construction module named as" +newModuleType.getUserFriendlyName()+ ", due to remote exception");
 		}
 	}
 
 	/**
 	 * Returns the lower level construction object for ATRON modular robot morphology.
 	 * The construction is on the level of components.
 	 */
 	@Override
 	public  ConstructionTemplate getConstruction() {
 		return construction = new ATRONConstructionTemplate(simulation);		
 	}
 
 
 
 }
