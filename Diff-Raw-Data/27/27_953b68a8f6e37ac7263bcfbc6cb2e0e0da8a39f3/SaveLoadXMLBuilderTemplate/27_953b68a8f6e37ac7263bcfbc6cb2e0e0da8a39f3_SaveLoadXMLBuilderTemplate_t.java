 package ussr.builder.saveLoadXML;
 
 import java.awt.Color;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.transform.sax.TransformerHandler;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import com.jme.math.Quaternion;
 
 import ussr.builder.enumerations.SupportedModularRobots;
 import ussr.builder.enumerations.UssrXmlFileTypes;
 import ussr.builder.enumerations.XMLTagsUsed;
 import ussr.builder.helpers.BuilderHelper;
 import ussr.builder.helpers.FileDirectoryHelper;
 import ussr.builder.helpers.StringProcessingHelper;
 import ussr.description.geometry.RotationDescription;
 import ussr.description.geometry.VectorDescription;
 import ussr.description.setup.BoxDescription;
 import ussr.description.setup.WorldDescription;
 import ussr.model.Module;
 import ussr.physics.PhysicsParameters;
 import ussr.physics.jme.JMESimulation;
 
 /**
  * This class is responsible for current definition of the XML format of saving and loading
  * for builder.
  * @author Konstantinas
  */  
 public abstract class SaveLoadXMLBuilderTemplate extends SaveLoadXMLTemplate {
 
 	/**
 	 * Method for defining the format of XML to print into the xml file. In other words
 	 * what to save in the file about simulation.  
 	 * @param transformerHandler,the content handler used to print out XML format. 
 	 */
 	public void printOutXML(UssrXmlFileTypes ussrXmlFileType, TransformerHandler transformerHandler) {
 		
 		switch(ussrXmlFileType){
 		case ROBOT:
 			printRobotXML(transformerHandler);
 			break;
 		case SIMULATION:
 			printSimulationXML(transformerHandler);
 			break;
 			default: throw new Error("XML file type named as "+ ussrXmlFileType.toString()+ " is not supported yet");
 		}
 	
 	}
 	
 	/**
 	 * Saves the data about the robot in simulation environment in XML file.
 	 * @param transformerHandler,the content handler used to print out XML format.
 	 */
 	private void printRobotXML(TransformerHandler transformerHandler){
 		printFirstStartTag(transformerHandler, XMLTagsUsed.MODULES);
 		int amountModules = numberOfSimulatedModules();
 		/*For each module print out the start and end tags with relevant data*/
 		for (int module=0; module<amountModules;module++){           
 			Module currentModule = getModuleByIndex(module);			
 			try {				
 				transformerHandler.startElement("","",XMLTagsUsed.MODULE.toString(),EMPTY_ATT);				
 				printSubTagsWithValue(transformerHandler, XMLTagsUsed.ID, getID(currentModule));				
 				printSubTagsWithValue(transformerHandler, XMLTagsUsed.TYPE, getType(currentModule));
 				printSubTagsWithValue(transformerHandler, XMLTagsUsed.NAME, getName(currentModule));
 				printSubTagsWithValue(transformerHandler, XMLTagsUsed.CONTROLLER_LOCATION, getControllerLocation(getModuleByIndex(0)));
 				printSubTagsWithValue(transformerHandler, XMLTagsUsed.ROTATION, getRotation(currentModule));
 				printSubTagsWithValue(transformerHandler, XMLTagsUsed.ROTATION_QUATERNION, getRotationQuaternion(currentModule));
 				printSubTagsWithValue(transformerHandler, XMLTagsUsed.POSITION, getPosition(currentModule));
 				//printSubTagsWithValue(transformerHandler, positionVectorTag, getPositionVector(currentModule));
 				printSubTagsWithValue(transformerHandler,  XMLTagsUsed.LABELS_MODULE, getLabelsModule(currentModule));
 				printSubTagsWithValue(transformerHandler,  XMLTagsUsed.COMPONENTS, getAmountComponents(currentModule));
 				printSubTagsWithValue(transformerHandler, XMLTagsUsed.COLORS_COMPONENTS, getColorsComponents(currentModule));
 				printSubTagsWithValue(transformerHandler, XMLTagsUsed.CONNECTORS, getAmountConnectors(currentModule));			
 				printSubTagsWithValue(transformerHandler, XMLTagsUsed.COLORS_CONNECTORS, getColorsConnectors(currentModule));
 				printSubTagsWithValue(transformerHandler,XMLTagsUsed.LABELS_CONNECTORS, getLabelsConnectors(currentModule));
 
 				printInfoConnectors(transformerHandler,getInfoConnectors(currentModule, true), getInfoConnectors(currentModule, false));						
 				transformerHandler.endElement("","",XMLTagsUsed.MODULE.toString());
 			} catch (SAXException e) {
 				throw new Error ("SAX exception appeared and named as: "+ e.toString());
 			}			
 		}
 		printFirstEndTag(transformerHandler, XMLTagsUsed.MODULES);	
 	}
 	
 	/**
 	 * Saves the data about simulation environment(world description and physics parameters) in XML file.
 	 * @param transformerHandler,the content handler used to print out XML format.
 	 */
 	private void printSimulationXML(TransformerHandler transformerHandler){
 		printFirstStartTag(transformerHandler, XMLTagsUsed.SIMULATION);		
 		String robotMorphologyFileLocation ="";
 		String moduleType ="";
 		try {
 
 			transformerHandler.startElement("","",XMLTagsUsed.ROBOT.toString(),EMPTY_ATT);
             
 			
 			moduleType = StringProcessingHelper.covertToString(getType(getModuleByIndex(0)));		
 			
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.TYPE, moduleType.toCharArray());				
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.NUMBER_OF_MODULES, (""+getWorldDescription().getNumberOfModules()).toCharArray());
 			
 			String directory = FileDirectoryHelper.extractDirectory(fileDirectoryName);
 			robotMorphologyFileLocation = directory+DEFAULT_DIRECTORY_ROBOTS+"\\"+SupportedModularRobots.getConsistentMRName(moduleType).toLowerCase()+XML_EXTENSION;
 				
 			//robotMorphologyFileLocation = directory;//+ "morphologies"+"\\"+ BuilderHelper.getRandomInt();
 						
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.MORPHOLOGY_LOCATION, robotMorphologyFileLocation.toCharArray());
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.CONTROLLER_LOCATION, getControllerLocation(getModuleByIndex(0)));
 			
 			transformerHandler.endElement("","",XMLTagsUsed.ROBOT.toString());
 
 			transformerHandler.startElement("","",XMLTagsUsed.WORLD_DESCRIPTION.toString(),EMPTY_ATT);
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.PLANE_SIZE, (""+getWorldDescription().getPlaneSize()).toCharArray());
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.PLANE_TEXTURE,  getWorldDescription().getPlaneTexture().getFileName().toString().toCharArray());			
 		    printSubTagsWithValue(transformerHandler, XMLTagsUsed.CAMERA_POSITION, getWorldDescription().getCameraPosition().toString().toCharArray());			    
 		    printSubTagsWithValue(transformerHandler, XMLTagsUsed.THE_WORLD_IS_FLAT, (""+getWorldDescription().theWorldIsFlat()).toCharArray());
 		    printSubTagsWithValue(transformerHandler, XMLTagsUsed.HAS_BACKGROUND_SCENERY, (""+getWorldDescription().hasBackgroundScenery()).toCharArray());
 		    printSubTagsWithValue(transformerHandler, XMLTagsUsed.HAS_HEAVY_OBSTACLES, (""+getWorldDescription().hasBackgroundScenery()).toCharArray());
 		    printSubTagsWithValue(transformerHandler, XMLTagsUsed.IS_FRAME_GRABBING_ACTIVE, (""+getWorldDescription().getIsFrameGrabbingActive()).toCharArray());
 		    printSubTagsWithValue(transformerHandler, XMLTagsUsed.BIG_OBSTACLES, extractPositionsObstacles(getWorldDescription().getBigObstacles()));
 			transformerHandler.endElement("","",XMLTagsUsed.WORLD_DESCRIPTION.toString());
 
 			transformerHandler.startElement("","",XMLTagsUsed.PHYSICS_PARAMETERS.toString(),EMPTY_ATT);
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.WORLD_DAMPING_LINEAR_VELOCITY, (""+PhysicsParameters.get().getWorldDampingLinearVelocity()).toCharArray());
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.WORLD_DAMPING_ANGULAR_VELOCITY, (""+PhysicsParameters.get().getWorldDampingAngularVelocity()).toCharArray());
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.PHYSICS_SIMULATION_STEP_SIZE, (""+PhysicsParameters.get().getPhysicsSimulationStepSize()).toCharArray());
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.REALISTIC_COLLISION, (""+PhysicsParameters.get().getRealisticCollision()).toCharArray());
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.GRAVITY, (""+PhysicsParameters.get().getGravity()).toCharArray());
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.PLANE_MATERIAL, PhysicsParameters.get().getPlaneMaterial().toString().toCharArray());
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.MAINTAIN_ROTATIONAL_JOINT_POSITIONS, (""+PhysicsParameters.get().getMaintainRotationalJointPositions()).toCharArray());
 		//	printSubTagsWithValue(transformerHandler, TagsUsed.HAS_MECHANICAL_CONNECTOR_SPRINGINESS, (""+PhysicsParameters.get().hasMechanicalConnectorSpringiness()).toCharArray());
 		//	printSubTagsWithValue(transformerHandler, TagsUsed.MECHANICAL_CONNECTOR_CONSTANT, (""+PhysicsParameters.get().getMechanicalConnectorConstant()).toCharArray());
 		//  printSubTagsWithValue(transformerHandler, TagsUsed.MECHANICAL_CONNECTOR_DAMPING, (""+PhysicsParameters.get().getMechanicalConnectorDamping()).toCharArray());
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.CONSTRAINT_FORCE_MIX, (""+PhysicsParameters.get().getConstraintForceMix()).toCharArray());
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.ERROR_REDUCTION_PARAMETER, (""+PhysicsParameters.get().getErrorReductionParameter()).toCharArray());
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.RESOLUTION_FACTOR, (""+PhysicsParameters.get().getResolutionFactor()).toCharArray());
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.USE_MOUSE_EVENT_QUEUE, (""+PhysicsParameters.get().useModuleEventQueue()).toCharArray());
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.SYNC_WITH_CONTROLLERS, (""+PhysicsParameters.get().syncWithControllers()).toCharArray());
 			printSubTagsWithValue(transformerHandler, XMLTagsUsed.PHYSICS_SIMULATION_CONTROLLER_STEP_FACTOR, (""+PhysicsParameters.get().getPhysicsSimulationControllerStepFactor()).toCharArray());
 			transformerHandler.endElement("","",XMLTagsUsed.PHYSICS_PARAMETERS.toString());
 		} catch (SAXException e) {
 			throw new Error ("SAX exception appeared and named as: "+ e.toString());
 		}
 
 		printFirstEndTag(transformerHandler, XMLTagsUsed.SIMULATION);
 	
 		getInstance().saveXMLfile(UssrXmlFileTypes.ROBOT, robotMorphologyFileLocation);
 				
 	}
 	
 	 /**
 	  * Returns instance of XML processing class.
 	 * @return  current instance of XML processing.
 	 */
 	protected abstract InSimulationXMLSerializer getInstance();
 	
 	/**
 	 * Returns module in simulation  environment by its index in the list of modules.
 	 * @param moduleIndex, the index of the module to return,
 	 * @return module, the module in simulation environment.
 	 */
 	protected abstract Module getModuleByIndex(int moduleIndex);
 
 	/**
 	 * Returns the world description object of simulation.
 	 * @return the world description object of simulation.
 	 */
 	protected abstract WorldDescription getWorldDescription();
 
 	/**
 	 * Returns the amount of modules in simulation environment.
 	 * @return the amount of modules in simulation environment.
 	 */
 	protected abstract int numberOfSimulatedModules();
 	
 	/**  
 	 * Loads the data about simulation from chosen XML file into simulation.
 	 * This operation is TEMPLATE method. Operation means that it should be executed on the object.
 	 * @param ussrXmlFileType, the type of XML file supported by USSR.
 	 * @param fileDirectoryName, the name of directory, like for example: "C:/newXMLfile".	 
 	 */
 	public void loadInXML(UssrXmlFileTypes ussrXmlFileType, Document document) {		
 		switch(ussrXmlFileType){
 		case ROBOT:
 			loadRobotXML(document);
 			break;
 		case SIMULATION:
 			loadSimulationXML(document);
 			break;
 			default: throw new Error("XML file type named as "+ ussrXmlFileType.toString()+ " is not supported yet");
 		}
 	}
 	
 	
 	/**
 	 * Loads robot in simulation environment from XML description file.
 	 * @param document, DOM object of document. 
 	 */
 	private void loadRobotXML(Document document){
 		
 		NodeList nodeList = document.getElementsByTagName(XMLTagsUsed.MODULE.toString());
 		
 		for (int node = 0; node < nodeList.getLength(); node++) {
 			Node firstNode = nodeList.item(node);
 
 			if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
 
 				Element firstElmnt = (Element) firstNode;				
 				//String moduleID = extractTagValue(firstElmnt,idTag);
 			    String moduleType = extractTagValue(firstElmnt,XMLTagsUsed.TYPE);
 				String moduleName = extractTagValue(firstElmnt,XMLTagsUsed.NAME);
 				String moduleRotation = extractTagValue(firstElmnt,XMLTagsUsed.ROTATION);		
 				String moduleRotationQuaternion = extractTagValue(firstElmnt,XMLTagsUsed.ROTATION_QUATERNION);
 				String modulePosition = extractTagValue(firstElmnt,XMLTagsUsed.POSITION);
 				//String modulePositionVector = extractTagValue(firstElmnt,positionVectorTag);
 				String labelsModule = extractTagValue(firstElmnt,XMLTagsUsed.LABELS_MODULE);
 				if (labelsModule.contains(BuilderHelper.getTempLabel())){
 					labelsModule = labelsModule.replaceAll(BuilderHelper.getTempLabel(), "");
 				}				
 				
 				int amountComponents = Integer.parseInt(extractTagValue(firstElmnt,XMLTagsUsed.COMPONENTS));
 				String colorsComponents = extractTagValue(firstElmnt,XMLTagsUsed.COLORS_COMPONENTS);				
 				LinkedList<Color> listColorsComponents = extractColorsComponents(amountComponents, colorsComponents);				
 				
 				int amountConnectors = Integer.parseInt(extractTagValue(firstElmnt,XMLTagsUsed.CONNECTORS));
 				String colorsConnectors = extractTagValue(firstElmnt,XMLTagsUsed.COLORS_CONNECTORS);				
 				LinkedList<Color> listColorsConnectors= extractColoursConnectors(amountConnectors,colorsConnectors);
 
 				String labelsConnectors = extractTagValue(firstElmnt,XMLTagsUsed.LABELS_CONNECTORS);
 				String tempLabelsConnectors[] = labelsConnectors.split(",");	
 				
 				RotationDescription rotationDescription = new RotationDescription();
 				rotationDescription.setRotation(new Quaternion(extractFromQuaternion(moduleRotationQuaternion,"X"),extractFromQuaternion(moduleRotationQuaternion,"Y"),extractFromQuaternion(moduleRotationQuaternion,"Z"),extractFromQuaternion(moduleRotationQuaternion,"W")));
 
 				createNewModule(moduleName,moduleType,new VectorDescription(extractFromPosition(modulePosition, "X"),extractFromPosition(modulePosition, "Y"),extractFromPosition(modulePosition, "Z")),rotationDescription ,listColorsComponents,listColorsConnectors,labelsModule,tempLabelsConnectors);
 			
 //FIXME IN CASE THERE IS A NEED TO EXTRACT THE STATE OF CONNECTORS
 					/*NodeList sixthNmElmntLst = fstElmnt.getElementsByTagName("CONNECTOR");
 				int amountConnectorNodes = sixthNmElmntLst.getLength();
 				System.out.println("amountConnectorNodes:"+amountConnectorNodes );
 
 				for(int con=0; con<amountConnectorNodes; con++){
 
 					Element currentElement =(Element)sixthNmElmntLst.item(con);
 					NodeList currentNumber = currentElement.getChildNodes();
 					System.out.println("CONNECTOR NAME=" +currentElement.getAttributes().item(0).getNodeValue()+" state:"+ ((Node) currentNumber.item(0)).getNodeValue());
 				}*/
 			}
 		}	
 	}
 	
 	/**
 	 * Container for keeping simulation values of world description object, physics parameters object and robot desription.
 	 */
 	private Map<XMLTagsUsed, String> simulationWorldValues = new Hashtable<XMLTagsUsed, String>(),
 	                                 simulationPhysicsValues = new Hashtable<XMLTagsUsed, String>(),
                                      robotDescription = new Hashtable<XMLTagsUsed, String>();
 	/**
      * Returns values of physics parameters object taken from xml file describing simulation.
 	 * @return values of physics parameters object taken from xml file describing simulation.
 	 */
 	public Map<XMLTagsUsed, String> getSimulationPhysicsValues() {
 		return simulationPhysicsValues;
 	}
 	
 	 /**
      * Returns values of world description object taken from xml file describing simulation.
      * @return values of world description object taken from xml file describing simulation.
      */
 	public Map<XMLTagsUsed, String> getSimulationWorldDescriptionValues() {
 		return simulationWorldValues;
 	}
 	
 	 /**
      * Returns values of robot description object taken from xml file describing simulation.
      * @return values of robot description object taken from xml file describing simulation.
      */
 	public Map<XMLTagsUsed, String> getRobotDescriptionValues() {
 		return robotDescription;
 	}
 	
 	/**
 	 * Loads simulation environment parameters from XML description file.
 	 * @param document, DOM object of document. 
 	 */
 	private void loadSimulationXML(Document document){		
 		
     NodeList nodeList2 = document.getElementsByTagName(XMLTagsUsed.ROBOT.toString());
 		
 		for (int node = 0; node < nodeList2.getLength(); node++) {
 			Node firstNode = nodeList2.item(node);
 
 			if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
 
 				Element firstElmnt = (Element) firstNode;				
 		
 				robotDescription.put(XMLTagsUsed.MORPHOLOGY_LOCATION, extractTagValue(firstElmnt,XMLTagsUsed.MORPHOLOGY_LOCATION));
 				robotDescription.put(XMLTagsUsed.CONTROLLER_LOCATION, extractTagValue(firstElmnt,XMLTagsUsed.CONTROLLER_LOCATION));					
 			}
 		}		
 		
 		
 		NodeList nodeList = document.getElementsByTagName(XMLTagsUsed.WORLD_DESCRIPTION.toString());
 		
 		for (int node = 0; node < nodeList.getLength(); node++) {
 			Node firstNode = nodeList.item(node);
 
 			if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
 
 				Element firstElmnt = (Element) firstNode;				
 		
 				simulationWorldValues.put(XMLTagsUsed.PLANE_SIZE, extractTagValue(firstElmnt,XMLTagsUsed.PLANE_SIZE));
 				simulationWorldValues.put(XMLTagsUsed.PLANE_TEXTURE, extractTagValue(firstElmnt,XMLTagsUsed.PLANE_TEXTURE));
 				simulationWorldValues.put(XMLTagsUsed.CAMERA_POSITION, extractTagValue(firstElmnt,XMLTagsUsed.CAMERA_POSITION));
 				//number of modules is not relevant
 				simulationWorldValues.put(XMLTagsUsed.THE_WORLD_IS_FLAT, extractTagValue(firstElmnt,XMLTagsUsed.THE_WORLD_IS_FLAT));				
 				simulationWorldValues.put(XMLTagsUsed.HAS_BACKGROUND_SCENERY, extractTagValue(firstElmnt,XMLTagsUsed.HAS_BACKGROUND_SCENERY));
 				simulationWorldValues.put(XMLTagsUsed.HAS_HEAVY_OBSTACLES, extractTagValue(firstElmnt,XMLTagsUsed.HAS_HEAVY_OBSTACLES));				
 				simulationWorldValues.put(XMLTagsUsed.IS_FRAME_GRABBING_ACTIVE, extractTagValue(firstElmnt,XMLTagsUsed.IS_FRAME_GRABBING_ACTIVE));
 				//simulationDescriptionValues.put(TagsUsed.BIG_OBSTACLES, extractTagValue(firstElmnt,TagsUsed.BIG_OBSTACLES));		
 			}
 
 		}	
 		
 		NodeList nodeList1 = document.getElementsByTagName(XMLTagsUsed.PHYSICS_PARAMETERS.toString());
 		for (int node = 0; node < nodeList1.getLength(); node++) {
 			Node firstNode = nodeList1.item(node);
 
 			if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
 
 				Element firstElmnt = (Element) firstNode;				
 		
 				simulationPhysicsValues.put(XMLTagsUsed.WORLD_DAMPING_LINEAR_VELOCITY, extractTagValue(firstElmnt,XMLTagsUsed.WORLD_DAMPING_LINEAR_VELOCITY));
 				simulationPhysicsValues.put(XMLTagsUsed.WORLD_DAMPING_ANGULAR_VELOCITY, extractTagValue(firstElmnt,XMLTagsUsed.WORLD_DAMPING_ANGULAR_VELOCITY));
 				simulationPhysicsValues.put(XMLTagsUsed.PHYSICS_SIMULATION_STEP_SIZE, extractTagValue(firstElmnt,XMLTagsUsed.PHYSICS_SIMULATION_STEP_SIZE));
 				simulationPhysicsValues.put(XMLTagsUsed.REALISTIC_COLLISION, extractTagValue(firstElmnt,XMLTagsUsed.REALISTIC_COLLISION));
 				simulationPhysicsValues.put(XMLTagsUsed.GRAVITY, extractTagValue(firstElmnt,XMLTagsUsed.GRAVITY));
 				simulationPhysicsValues.put(XMLTagsUsed.PLANE_MATERIAL, extractTagValue(firstElmnt,XMLTagsUsed.PLANE_MATERIAL));
 				simulationPhysicsValues.put(XMLTagsUsed.MAINTAIN_ROTATIONAL_JOINT_POSITIONS, extractTagValue(firstElmnt,XMLTagsUsed.MAINTAIN_ROTATIONAL_JOINT_POSITIONS));
 				//simulationPhysicsValues.put(TagsUsed.HAS_MECHANICAL_CONNECTOR_SPRINGINESS, extractTagValue(firstElmnt,TagsUsed.HAS_MECHANICAL_CONNECTOR_SPRINGINESS));
 				//simulationPhysicsValues.put(TagsUsed.MECHANICAL_CONNECTOR_CONSTANT, extractTagValue(firstElmnt,TagsUsed.MECHANICAL_CONNECTOR_CONSTANT));
 				//simulationPhysicsValues.put(TagsUsed.MECHANICAL_CONNECTOR_DAMPING, extractTagValue(firstElmnt,TagsUsed.MECHANICAL_CONNECTOR_CONSTANT));
 				simulationPhysicsValues.put(XMLTagsUsed.CONSTRAINT_FORCE_MIX, extractTagValue(firstElmnt,XMLTagsUsed.CONSTRAINT_FORCE_MIX));
 				simulationPhysicsValues.put(XMLTagsUsed.ERROR_REDUCTION_PARAMETER, extractTagValue(firstElmnt,XMLTagsUsed.ERROR_REDUCTION_PARAMETER));
 				simulationPhysicsValues.put(XMLTagsUsed.RESOLUTION_FACTOR, extractTagValue(firstElmnt,XMLTagsUsed.RESOLUTION_FACTOR));
 				simulationPhysicsValues.put(XMLTagsUsed.USE_MOUSE_EVENT_QUEUE, extractTagValue(firstElmnt,XMLTagsUsed.USE_MOUSE_EVENT_QUEUE));
 				simulationPhysicsValues.put(XMLTagsUsed.SYNC_WITH_CONTROLLERS, extractTagValue(firstElmnt,XMLTagsUsed.SYNC_WITH_CONTROLLERS));
 				simulationPhysicsValues.put(XMLTagsUsed.PHYSICS_SIMULATION_CONTROLLER_STEP_FACTOR, extractTagValue(firstElmnt,XMLTagsUsed.PHYSICS_SIMULATION_CONTROLLER_STEP_FACTOR));
 			}
 		}			
 	}
 	
 	/**
 	 * Creates new module in simulation environment.
 	 * @param moduleName
 	 * @param moduleType
 	 * @param modulePosition
 	 * @param moduleRotation
 	 * @param listColorsComponents
 	 * @param listColorsConnectors
 	 * @param labelsModule
 	 * @param labelsConnectors
 	 */
 	protected abstract void createNewModule(String moduleName, String moduleType, VectorDescription modulePosition, RotationDescription moduleRotation, LinkedList<Color> listColorsComponents,LinkedList<Color> listColorsConnectors, String labelsModule, String[] labelsConnectors);
 
 	
 	private char[] extractPositionsObstacles(List<BoxDescription> bigObstacles ){
 		//FIXME
 		String positions = "";
 		Iterator bigObstaclesIt = bigObstacles.iterator();
 		while(bigObstaclesIt.hasNext()){
 			//BoxDescription
 			BoxDescription boxDescription = (BoxDescription)bigObstaclesIt.next();
 			positions = positions +";"+ boxDescription.getPosition();
 			
 		}
 		return positions.toCharArray();
 		
 	}	
 	
 	/**
 	 * Extracts the value of specific coordinate from the string of VectorDescription.
 	 * @param textString, the string  of VectorDescription. 
 	 * @param coordinate, the coordinate to extract.
 	 * @return the value of the coordinate.
 	 */
 	private float extractFromPosition(String textString, String coordinate){		
 		String cleanedTextString1 =textString.replace("(", "");
 		String cleanedTextString2 =cleanedTextString1.replace(")", "");
 		String[] temporary = cleanedTextString2.split(",");		
 
 		float extractedValue = 100000; 
 		if (coordinate.equalsIgnoreCase("X")){
 			extractedValue = Float.parseFloat(temporary[0]);			
 		}else if (coordinate.equalsIgnoreCase("Y")){
 			extractedValue = Float.parseFloat(temporary[1]);			
 		}else if (coordinate.equalsIgnoreCase("Z")){
 			extractedValue = Float.parseFloat(temporary[2]);
 		}else throw new Error ("There is no such coordinate");
 		return extractedValue; 
 	}	
 
 	/**
 	 * Extracts the value of specific coordinate from the string of Vector3f.
 	 * @param textString, the string  of Vector3f. 
 	 * @param coordinate, the coordinate to extract.
 	 * @return the value of the coordinate.
 	 */
 	public float extractVector(String textString, String coordinate){
 		//String cleanedTextString1 =textString.replace("[", "");
 		textString =textString.replace("]", "");
 		textString =textString.replace("[", "");
 		textString =textString.replace("X", "");
 		textString =textString.replace("Y", "");
 		textString =textString.replace("Z", "");	
 		textString =textString.replace("=", "");
 		textString =textString+",";
 		System.out.println("Text"+textString);	
 
 		String[] temporary = textString.split(",");
 
 		float extractedValue = 100000; 
 		if (coordinate.equalsIgnoreCase("X")){			
 			extractedValue = Float.parseFloat(temporary[0]);
 			//System.out.println(""+extractedValue);		
 		}else if (coordinate.equalsIgnoreCase("Y")){
 			extractedValue = Float.parseFloat(temporary[1]);
 			//System.out.println(""+extractedValue);
 		}else if (coordinate.equalsIgnoreCase("Z")){
 			extractedValue = Float.parseFloat(temporary[2]);
 			//System.out.println(""+extractedValue);
 		}else throw new Error ("There is no such coordinate");
 
 		return extractedValue; 
 	}
 }
