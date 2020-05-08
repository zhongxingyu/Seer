 package ussr.builder.constructionTools;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import ussr.builder.BuilderHelper;
 import ussr.description.geometry.RotationDescription;
 import ussr.description.geometry.VectorDescription;
 import ussr.description.setup.ModulePosition;
 import ussr.model.Module;
 import ussr.physics.jme.JMESimulation;
 
 /**
  * The main responsibility of this class is to act as abstract class in TEMPLATE METHOD pattern.
  * Moreover define the methods common for construction of modular robots morphologies in more 
  * abstract-module oriented way, rather than component oriented way. It is more abstract due to the 
  * fact that modules are created and moved with respect to each other here. 
  * @author Konstantinas
  *
  */
 public abstract class CommonOperationsTemplate implements  SelectOperationsTemplate{
 
 	/** 
 	 * The physical simulation
 	 */
 	public JMESimulation simulation;	
 
 	/**
 	 * The interface to construction of modular robot morphology. This one is on the level of components of the module.  
 	 */
 	protected ConstructionTemplate construction;
 
 	/**
 	 * The module selected in simulation environment. 
 	 */
 	private Module selectedModule;
 
 	/**
 	 *  Newly created movable module.
 	 */
 	private Module newMovableModule;
 	
 	/**
 	 * The zero component of the module.
 	 */
 	private final static int zeroComponent = 0;
 
 	/**
 	 *  Supports construction of modular robot morphology in more abstract module oriented way.
 	 * @param simulation,the physical simulation
 	 */
 	public  CommonOperationsTemplate(JMESimulation simulation){
 		this.simulation = simulation;
 	}	
 
 	/**
 	 * Returns the lower level construction object for modular robot morphology.
 	 * The construction is on the level of components.
 	 */
 	public abstract ConstructionTemplate getConstruction();
 	
 	/**
 	 * Adds default (the first) construction module at specified position.
 	 * This operation is TEMPLATE method. Operation means that it should be executed on the object.
 	 * @param type, the type of modular robot. For example: ATRON,MTRAN or OdinBall.
 	 * @param modulePosition, the position of module in simulation environment.
 	 */
 	public void addDefaultConstructionModule(String type, VectorDescription modulePosition ){
 		List<Color> colorsComponents = new LinkedList<Color>();
 		ArrayList<Color> colorsConectors = new ArrayList<Color>();
 		RotationDescription moduleRotation = null;
 		addDefaultModule(type,modulePosition,moduleRotation, colorsComponents,colorsConectors);	
 	}
 
 	/**
 	 * Adds default (the first) construction module at specified position.
 	 * This method is so-called "Primitive operation" for above TEMPLATE method, called "addDefaultConstructionModule(String type, VectorDescription modulePosition)".	 
 	 * @param type, the type of modular robot. For example: ATRON,MTRAN or OdinBall.
 	 * @param modulePosition, the position of the module in simulation environment.
 	 * @param moduleRotation, the rotation of the module.
 	 * @param colorsComponents, the colours of components constituting the module.
 	 * @param colorsConectors, the colours of connectors on the module.
 	 */
 	public abstract void addDefaultModule(String type,VectorDescription modulePosition,RotationDescription moduleRotation, List<Color> colorsComponents,ArrayList<Color> colorsConectors);
 
 	/**
 	 * Adds and returns newModule at specific position and with specific rotation.
 	 * This method is common to children classes. 
 	 * @param modulePosition, global starting position of the module and annotations.
 	 * @param colorsComponents, the colours of module components.
 	 * @param colorsConnectors, the colours of module connectors.
 	 * @return newModule,newly added module with specific position and rotation. 
 	 */
 	public Module addNewModule(ModulePosition modulePosition, List<Color> colorsComponents,ArrayList<Color> colorsConnectors){
 		Module newModule = this.simulation.createModule(modulePosition,true);			
 		newModule.setColorList(colorsComponents);		
 		setColorsConnectors(newModule,colorsConnectors);
 		return newModule;
 	}
 
 	/**
 	 * Adds the new module on connector. This is for both: selected connector on the module in the simulation environment
 	 * and connector passed as a variable and later selected module in simulation environment.
 	 * This operation is TEMPLATE method. Operation means that it should be executed on the object.
 	 * @param toolSpecification,object containing information about modular robot, selected or chosen connector
 	 * number,selected module, simulation and so on.  
 	 */
 	public void addNewModuleOnConnector(ConstructionToolSpecification toolSpecification){		
 		this.construction = toolSpecification.getConstruction();		
 		this.selectedModule = toolSpecification.getSelectedModule();				
 //		FIXME HERE THE PROBLEM EXISTS WITH "default" thing of ATRON 
 		this.newMovableModule = createNewModule(this.selectedModule);		
 		this.construction.moveModuleAccording(toolSpecification.getSelectedConnectorNr(), this.selectedModule, this.newMovableModule, false);		
 	}
 
 	/**
 	 * Creates and returns new module, depending on which is needed: the copy module or different one. 	 
 	 * This method is so-called "Primitive operation" for above TEMPLATE method, called "addNewModuleOnConnector(ConstructionToolSpecification toolSpecification)".	 
 	 * @param selectedModule,the module selected in simulation environment.
 	 * @return module, newly created module.  
 	 */
 	public abstract Module createNewModule(Module selectedModule);
 
 	/**
 	 * Adds and returns newModule in simulation environment, which is the copy of selected module.
 	 * Meaning that the module will be added at the same position, with the same rotation  and with the same colours, as selected module.
 	 * This method is common to children classes. 	 
 	 * @param selectedModule,the module selected in simulation environment.	
 	 * @return newModule,the module which is the copy of selected module
 	 */
 	public Module addNewCopyModule(Module selectedModule){		
 		VectorDescription position = selectedModule.getPhysics().get(zeroComponent).getPosition();       
 		RotationDescription rotation = selectedModule.getPhysics().get(zeroComponent).getRotation();
 		List<Color> colorsComponents = selectedModule.getColorList();			
 		ArrayList<Color> colorsConnectors = getColorsConnectors(selectedModule);
 		String selectedModuleName = selectedModule.getProperty(BuilderHelper.getModuleNameKey());
 		String moduleType;
 		moduleType = selectedModule.getProperty(BuilderHelper.getModuleTypeKey());
 		if (moduleType.contains("ATRON")||moduleType.contains("default")){
 			moduleType = "default";
 		}
		System.out.println("sss:"+moduleType);
 		ModulePosition modulePosition = new ModulePosition(selectedModuleName+ BuilderHelper.getRandomInt(),moduleType,position,rotation);	
 		Module newModule = this.simulation.createModule(modulePosition,true);				
 		newModule.setColorList(colorsComponents);		
 		setColorsConnectors(newModule,colorsConnectors);
 		return newModule;
 	}
 
 	/**
 	 * Adds the new modules on all connectors of the module selected in simulation environment
 	 * This operation is common to children classes. Operation means that it should be executed on the object.
 	 * @param toolSpecification,object containing information about modular robot, selected module, simulation
 	 * and so on.
 	 */
 	public void addModulesOnAllConnectors(ConstructionToolSpecification toolSpecification) {
 		/*Define the information, which will be manipulated*/
 		this.selectedModule = toolSpecification.getSelectedModule();
 		int amountConnectors = this.selectedModule.getConnectors().size();
 		/*For each connector of selected module set it as selected connector and place new module on it*/
 		for (int connectorNr=0; connectorNr<amountConnectors;connectorNr++){			
 			toolSpecification.setSelectedConnectorNr(connectorNr);			
 			addNewModuleOnConnector(toolSpecification);				
 		}	
 	}	
 
 	/**
 	 * Rotates the module selected in simulation environment with opposite rotation.
 	 * This operation is common to children classes. Operation means that it should be executed on the object.
 	 * @param toolSpecification, object containing information about modular robot, selected module, simulation
 	 * and so on.
 	 */
 	public void rotateModuleWithOppositeRotation(ConstructionToolSpecification toolSpecification){
 		this.selectedModule = toolSpecification.getSelectedModule();
 		this.construction = toolSpecification.getConstruction();
 		this.construction.rotateModuleOpposite(this.selectedModule);	
 	}
 
 	/**
 	 * Rotates the module selected in simulation environment with standard rotation passed as a String
 	 * This operation is common to children classes. Operation means that it should be executed on the object.
 	 * @param toolSpecification, object containing information about modular robot, selected module, simulation
 	 * and so on.
 	 */
 	public void rotateModuleStandardRotation(ConstructionToolSpecification toolSpecification, String standardRotationName){
 		this.selectedModule = toolSpecification.getSelectedModule();
 		this.construction = toolSpecification.getConstruction();
 		this.construction.rotateModuleSpecifically(this.selectedModule, standardRotationName);	
 	}
 
 	/**
 	 * Additional method for implementing unique properties of modular robots. Like for example
 	 * MTRAN has several more specific rotations, which are implemented to make the construction
 	 * more flexible.In Odin case this is replacement of modules by just selecting them in simulation
 	 * environment. 
 	 * This operation is TEMPLATE method. Operation means that it should be executed on the object.   
 	 * @param toolSpecification,object containing information about modular robot, selected module, simulation
 	 * and so on.
 	 */
 	public void variateModule(ConstructionToolSpecification toolSpecification){		
 		this.selectedModule = toolSpecification.getSelectedModule();
 		this.construction = toolSpecification.getConstruction();
 		variateSpecificModule(this.selectedModule);
 	}
 	
 	/**
 	 * Additional method for implementing unique properties of modular robots. Like for example
 	 * MTRAN has several more specific rotations, which are implemented to make the construction
 	 * more flexible.In Odin case this is replacement of modules by just selecting them in simulation
 	 * environment. 
 	 * This method is so-called "Primitive operation" for above TEMPLATE method, called "variateModule(ConstructionToolSpecification toolSpecification)".	    
 	 * @param selectedModule,the module selected in simulation environment.	
 	 */
 	public abstract void variateSpecificModule(Module selectedModule);	
 	
 
 //	TODO MOVE THE FUNCTIONALITY
 	public void moveModuleOnNextConnector(ConstructionToolSpecification toolSpecification){
 /*		this.construction = toolSpecification.getConstruction();		
 		this.selectedModule = toolSpecification.getSelectedModule();
 		this.construction.moveModuleAccording(toolSpecification.getChosenConnectorNr(), this.selectedModule, this.newMovableModule);*/
 	}
 
 	/**
 	 * Returns the ArrayList, containing the colours of connectors of selected module.
 	 * @param selectedModule, the module selected in simulation environment.
 	 * @return the ArrayList of colours of connectors on the module
 	 */
 	private ArrayList<Color> getColorsConnectors(Module selectedModule){
 		ArrayList<Color> colorsConnectors = new ArrayList<Color>();
 		int nrConnectors = selectedModule.getConnectors().size();	        
 		for (int connector=0;connector<nrConnectors; connector++){        	
 			Color connectorColor = selectedModule.getConnectors().get(connector).getColor();
 			colorsConnectors.add(connectorColor);
 		}
 		return colorsConnectors; 
 	}
 
 	/**
 	 * Colors the connectors of the module, according to the colors in ArrayList. Precondition should be that the number of connectors equals to the number of colours in ArrayList.  
 	 * @param newModule, the module in simulation environment.
 	 * @param colorsConnectors, the colors to assign to connectors. Each index in ArrayList is color and at the same time equals to connector number
 	 * @throws Error, if the number of connectors on module is not matching the number of colors in ArrayList. 
 	 */
 	private void setColorsConnectors(Module newModule, ArrayList<Color> colorsConnectors){
 		int nrConnectors = newModule.getConnectors().size();
 		if (nrConnectors!=colorsConnectors.size()){	
 			throw new Error("The number of connectors on module is not matching the number of colors in ArrayList!");			
 		}else{
 			for (int connector=0;connector<nrConnectors; connector++){
 				newModule.getConnectors().get(connector).setColor(colorsConnectors.get(connector));	        	
 			}
 		}
 	}
 }
