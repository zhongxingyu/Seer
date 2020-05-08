 package ussr.builder.constructionTools;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 import ussr.builder.BuilderHelper;
 import ussr.description.geometry.RotationDescription;
 import ussr.description.geometry.VectorDescription;
 import ussr.description.setup.ModulePosition;
 import ussr.model.Module;
 import ussr.physics.jme.JMESimulation;
 import ussr.samples.ckbot.CKBotStandard;
 
 /**
  * Supports construction of CKBot modular robot morphology in more abstract module oriented way.
  * In general the main responsibility of this class is to create CKBot modules and add default CKBot
  * construction module. 
  * @author Konstantinas
  */
 public class CKBotOperationsTemplate extends CommonOperationsTemplate {
 
 	/**
 	 * Supports construction of CKBot modular robot morphology in more abstract module oriented way.
 	 * @param simulation, the physical simulation.
 	 */
 	public CKBotOperationsTemplate(JMESimulation simulation) {
 		super(simulation);		
 	}
 	
 	/**
 	 * Adds default (the first) CKBot construction module at specified position.
 	 * This method is so-called "Primitive operation" for  TEMPLATE method, called "addDefaultConstructionModule(String type, VectorDescription modulePosition)".	 
	 * @param type, the type of modular robot. In this cas it is ATRON.
 	 * @param modulePosition, the position of the ATRON module in simulation environment.
 	 * @param moduleRotation, the rotation of the ATRON module.
	 * @param colorsComponents, the colours of components constituting the ATRON module.
	 * @param colorsConectors, the colours of connectors on the ATRON module.
 	 */
 	@Override
 	public void addDefaultModule(String type, VectorDescription modulePosition,RotationDescription moduleRotation, List<Color> colorsComponents,ArrayList<Color> colorsConectors) {
 		colorsComponents.add(Color.RED);colorsComponents.add(Color.BLUE);
 		colorsConectors.add(Color.WHITE);colorsConectors.add(Color.WHITE);
 		colorsConectors.add(Color.WHITE);colorsConectors.add(Color.WHITE);
 		moduleRotation = CKBotStandard.ROTATION_0_MINUS90X_MINUS90Z;
 		addNewModule(new ModulePosition(type+ BuilderHelper.getRandomInt(),"CKBotStandard",modulePosition,moduleRotation),colorsComponents,colorsConectors);
 		
 	}
 
 	/**
 	 * Creates and returns new copy of CKBot module of selected module.
 	 * This method is so-called "Primitive operation" for above TEMPLATE method, called "addNewModuleOnConnector(ConstructionToolSpecification toolSpecification)".	 
 	 * @param selectedModule,the module selected in simulation environment.
 	 * @return module, newly created ATRON module.  
 	 */	
 	@Override
 	public Module createNewModule(Module selectedModule) {
 		Module module = addNewCopyModule(selectedModule);
 		return module;
 	}
 
 	/**
 	 * Returns the lower level construction object for CKBot modular robot morphology.
 	 * The construction is on the level of components.
 	 */
 	@Override
 	public ConstructionTemplate getConstruction() {
 			return construction = new CKBotConstructionTemplate(simulation);
 	}
 
 	/**
 	 * Additional method for implementing unique properties of modular robots. Here the functionality
 	 * is repeated, however in future can be expanded.
 	 * This method is so-called "Primitive operation" for TEMPLATE method, called "variateModule(ConstructionToolSpecification toolSpecification)".	    
 	 * @param selectedModule,the module selected in simulation environment.	
 	 */
 	@Override
 	public void variateSpecificModule(Module selectedModule) {
 		construction.variateModuleProperties(selectedModule);		
 	}
 
 }
