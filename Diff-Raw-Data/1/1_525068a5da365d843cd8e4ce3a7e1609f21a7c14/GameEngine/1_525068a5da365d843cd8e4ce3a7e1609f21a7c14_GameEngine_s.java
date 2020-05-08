 package anchovy;
 
 
 import java.awt.EventQueue;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.io.*;
 //import java.util.*;
 import anchovy.Components.*;
 import anchovy.Pair.Label;
 import anchovy.io.*;
 //import anchovy.io.Parser;
 /**
  * Game Engine for the 'Nuclear Power Plant Simulation Game'
  * Links all the technical components of the game together - the 'Controller' in the MVC design
  * 
  * @author Harrison
  */
 public class GameEngine {
 	public ArrayList<Component> powrPlntComponents = null;
 	//Parser parser;
 	//UI ui = null; Commented out for now
 	MainWindow window;
 	/**
 	 * Constructor for the game engine
 	 * On creation it creates a list to store the components of the power plant
 	 * and links to a user interface (what ever type of user interface that may be)
 	 */
 	public GameEngine(){
 		powrPlntComponents = new ArrayList<Component>();
 		//ui = new UI(this); Commented out for now
 		//parser = new Parser(this);
 		
 		window = new MainWindow();
 	
 		
 		/*
 		final Timer gameLoop = new Timer();
 		gameLoop.scheduleAtFixedRate(new TimerTask(){
 			boolean stop = false;
 			long timesRoundLoop = 0;
 			
 			public void run(){
 				timesRoundLoop++;
 				if(timesRoundLoop > 10){
 					stop = true;
 				}
 				if(!stop){
 					System.out.println("Hello");
 					runSimulation();
 				}else{
 					gameLoop.cancel();
 				}
 				
 			}
 		}, 0, 1000);
 		*/
 	}
 	public void parsercommand(String componentType,String componentName, String command)
 	{
 		Component component= getPowerPlantComponent(componentName);
 		InfoPacket i = new InfoPacket();
 
 					if(component.getName() == componentName)
 					{
 						i.namedValues.add(new Pair<String>(Pair.Label.cNme, component.getName()));
 							if(command=="open")
 							{
 								i.namedValues.add(new Pair<Boolean>(Pair.Label.psit, true));
 							}
 							else if(command=="close")
 							{
 								i.namedValues.add(new Pair<Boolean>(Pair.Label.psit, false));
 							}
 					}
 					else if(component.getName() == componentName)
 					{
 						
 						i.namedValues.add(new Pair<String>(Pair.Label.cNme, component.getName()));
 						if(command=="on")
 						{
 							i.namedValues.add(new Pair<Boolean>(Pair.Label.psit, true));
 						}
 						else if(command=="off")
 						{
 							i.namedValues.add(new Pair<Boolean>(Pair.Label.psit, false));
 						}
 					}
 					else if(component.getName() == componentName)
 					{
 							
 							i.namedValues.add(new Pair<String>(Pair.Label.cNme, component.getName()));
 							if(command=="lower")
 							{
 								i.namedValues.add(new Pair<Boolean>(Pair.Label.psit, true));
 							}
 							else if(command=="raise")
 							{
 								i.namedValues.add(new Pair<Boolean>(Pair.Label.psit, false));
 							}
 					}
 					else
 					{
 						System.out.println("wrong command entered");
 					}
 					try{
 						component.takeInfo(i);
 					}
 					catch(Exception e) { e.printStackTrace(); }
 			}
 			
 		
 	/**
 	 * Using a list of Info Packets (generated from loading the same from file or elsewhere)
 	 * Adds each of the components described in the Info Packet list to the list of components in the power plant
 	 * Then sends the info packet to that component to initialize all its values
 	 * Once all components of the power plant are in the list and initialized, they are then all connected together in the way described by the info packets.
 	 * 
 	 * @param allPowerPlantInfo A list of info packets containing all the information about all components to be put into the power plant.
 	 */
 	public void setupPowerPlantConfigureation(ArrayList<InfoPacket> allPowerPlantInfo){
 		Iterator<InfoPacket> infoIt = allPowerPlantInfo.iterator();
 		InfoPacket currentInfo = null;
 		String currentCompName = null;
 		Component currentNewComponent = null;
 		
 		//Create component list.
 		while(infoIt.hasNext()){
 			currentInfo = infoIt.next();
 			currentCompName = getComponentNameFromInfo(currentInfo);
 			
 			//Determine component types we are dealing with.
 			if(currentCompName.contains("Consenser")){
 				currentNewComponent = new Condenser(currentCompName);
 			}else if(currentCompName.contains("Generator")){
 				currentNewComponent = new Generator(currentCompName);
 			}else if(currentCompName.contains("Pump")){
 				currentNewComponent = new Pump(currentCompName);
 			}else if(currentCompName.contains("Reactor")){
 				currentNewComponent = new Reactor(currentCompName);
 			}else if(currentCompName.contains("Turbine")){
 				currentNewComponent = new Turbine(currentCompName);
 			}else if(currentCompName.contains("Valve")){
 				currentNewComponent = new Valve(currentCompName);
 			}
 			addComponent(currentNewComponent); //add the component to the power plant
 			
 			try {
 				assignInfoToComponent(currentInfo); //send the just added component its info.
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		//Connect components together
 		infoIt = allPowerPlantInfo.iterator();// reset the iterator TODO i think this works.
 		ArrayList<String> inputComponents = new ArrayList<String>();
 		ArrayList<String> outputComponents = new ArrayList<String>();
 		
 		Iterator<Pair<?>> pairIt = null;
 		Pair currentPair = null;
 		Label currentLabel = null;
 		
 		Component currentComponent = null;
 		Iterator<Component> compIt = null;
 		
 		Iterator<String> connectionNameIt = null;
 		Component attachComp = null;
 		
 		//get info for each components
 		while(infoIt.hasNext()){
 			currentInfo = infoIt.next();
 			pairIt = currentInfo.namedValues.iterator();
 			
 			//get the useful information out of the info.
 			while(pairIt.hasNext()){
 				currentPair = pairIt.next();
 				currentLabel = currentPair.getLabel();
 		
 				switch (currentLabel){
 				case cNme:
 					currentCompName = (String) currentPair.second();
 					break;
 				case rcIF:
 					inputComponents.add((String) currentPair.second());
 					break;
 				case oPto:
 					outputComponents.add((String) currentPair.second());
 					break;
 				default:
 					break;
 				}
 			}
 			
 			//Get the component that we are going to connect other components to.
 			currentComponent = getPowerPlantComponent(currentCompName);
 			
 			//Attach each input component to the current component.
 			connectionNameIt = inputComponents.iterator();
 			while(connectionNameIt.hasNext()){
 				attachComp = getPowerPlantComponent(connectionNameIt.next());
 				connectComponentTo(currentComponent, attachComp, true);
 			}
 			//Attach each output component to the current compoennt
 			connectionNameIt = outputComponents.iterator();
 			while(connectionNameIt.hasNext()){
 				attachComp =  getPowerPlantComponent(connectionNameIt.next());
 				connectComponentTo(currentComponent,attachComp, false);
 			}
 			
 			
 		}
 	}
 	/**
 	 * Using the name of a component in the format of a string returns the actual Component found in the list of components of the Power Plant
 	 * 
 	 * @param The name of a component.
 	 * @return The component specified by the given name.
 	 */
 	public Component getPowerPlantComponent(String currentCompName) {
 		Component currentComponent = null;
 		Iterator<Component> compIt;
 		compIt = powrPlntComponents.iterator();
 		Component c = null;
 		String cName = null;
 		while(compIt.hasNext()){
 			c = compIt.next();
 			cName = c.getName();
 			if(cName.equals(currentCompName)){
 				currentComponent = c;
 			}
 		}
 		return currentComponent;
 	}
 	/**
 	 * Extracts the first component name out of an info packet.
 	 * 
 	 * @param info An info packet for a component
 	 * @return The component name contained within the given info packet.
 	 */
 	private String getComponentNameFromInfo(InfoPacket info){
 		Iterator<Pair<?>> pairIt = info.namedValues.iterator();
 		Pair<?> pair = null;
 		String name = null;
 		while(pairIt.hasNext() && name==null){
 			pair = pairIt.next();
 			if(pair.getLabel() == Label.cNme){
 				name = (String) pair.second();
 			}
 		}
 		return name;
 		
 	}
 	
 	/**
 	 * Sends an info packet to a component 
 	 * the components is specified by the name of the component in the info packet.
 	 * 
 	 * @param info Info Packet to be sent to a component
 	 */
 	public void assignInfoToComponent(InfoPacket info) throws Exception{
 		String compToSendTo = null;
 		
 //		Pair<?> pair = null;
 //		Iterator<Pair<?>> pi = info.namedValues.iterator();
 //		Label label = null;
 //		while(pi.hasNext() && compToSendTo == null){
 //			pair = pi.next();
 //			label = pair.getLabel();
 //			switch (label){
 //			case cNme:
 //				compToSendTo = (String) pair.second();
 //			default:
 //				break;
 //			}
 //		}
 		
 		compToSendTo = getComponentNameFromInfo(info);
 		
 //		Iterator<Component> ci = powrPlntComponents.iterator();
 //		boolean comNotFound = true;
 		Component com = null;
 //		while(ci.hasNext() && comNotFound){
 //			comNotFound = true;
 //			com = ci.next();
 //			if(com.getName() == compToSendTo){
 //				comNotFound = false;
 //				
 //			}
 //		}
 		
 		com = getPowerPlantComponent(compToSendTo);
 		/*
 		 * if the component wasn't found throw an exception stating this
 		 */
 		if(com == null){
 			throw new Exception("The component you were trying to send info to doesn't exit");
 		}else{
 			com.takeInfo(info);
 		}
 	}
 	
 	/**
 	 * Goes through the list of components one by one calling its simulate method
 	 * This should be called in a loop to get a continuous simulation. 
 	 */
 	public void runSimulation(){
 		Iterator<Component> ci = powrPlntComponents.iterator();
 		Component comp = null;
 		while(ci.hasNext()){
 			comp = ci.next();
 			comp.calucalte();
 		}
 	}
 	
 	/**
 	 * Add a component to the list of components
 	 * 
 	 * @param component the component to be added to the list of components
 	 */
 	public void addComponent(Component component){
 		powrPlntComponents.add(component);
 	}
 	
 	
 	/**
 	 * Connect two components together.
 	 * 
 	 * @param comp1 the component that we are working with
 	 * @param comp2 the component that will be added to comp1
 	 * @param input_output denoted whether it is an input or an output; in = true, out = false
 	 */
 	public void connectComponentTo(Component comp1, Component comp2, boolean input_ouput){
 		if(input_ouput){
 			comp1.connectToInput(comp2);
 			comp2.connectToOutput(comp1);
 		}else{
 			comp1.connectToOutput(comp2);
 			comp2.connectToInput(comp1);
 			
 		}
 	}
 	
 	/**
 	 * Get all the info from all the components within the power plant.
 	 * Used for saving and displaying info to UI.
 	 * @return List of InfoPackets for ALL components in the power plant.
 	 */
 	public ArrayList<InfoPacket> getAllComponentInfo(){
 		ArrayList<InfoPacket> allInfo = new ArrayList<InfoPacket>();
 		Iterator<Component> ci = powrPlntComponents.iterator();
 		Component comp = null;
 		while(ci.hasNext()){
 			comp = ci.next();
 			allInfo.add(comp.getInfo());
 		}
 		return allInfo;
 		
 	}
 	
 	/**
 	 * Resets the components of the power plant to am empty list.
 	 * Will be needed for loading a power plant from file.
 	 */
 	public void clearPowerPlant(){
 		powrPlntComponents = new ArrayList<Component>();
 	}
 	
 	/**
 	 * Updates interface with the latest changes to all the components.
 	 * @param packets Info about all the components in the game
 	 */
 	public void updateInterfaceComponents(ArrayList<InfoPacket> packets)
 	{
 		String nonmodifiable = new String();
 		String modifiable = new String();
 		Iterator<InfoPacket> packetIter = packets.iterator();
 		InfoPacket pckt = null;
 		//All info gathered about a component
 		String componentName = new String();
 		String componentDescriptionModi = new String();
 		String componentDescriptionNon = new String();
 		while(packetIter.hasNext())
 		{
 			
 			pckt = packetIter.next();
 			Iterator<Pair<?>> namedValueIter = pckt.namedValues.iterator();
 			while(namedValueIter.hasNext())
 			{
 				Pair<?> pair = namedValueIter.next();
 				Label currentLabel = pair.getLabel();
 				
 				switch (currentLabel){
 				case cNme:
 					componentName = (String) pair.second() + '\n';
 					break;
 				case rcIF:
 					componentDescriptionNon += "Gets inputs from: " + pair.second().toString() + '\n';
 					break;
 				case oPto:
 					componentDescriptionNon += "Outputs to: " + pair.second().toString() + '\n';
 					break;
 				case temp:
 					componentDescriptionNon += "Temperature: " + pair.second().toString() + '\n';
 					break;
 				case pres:
 					componentDescriptionNon += "Pressure: " + pair.second().toString() + '\n';
 					break;
 				case coRL:
 					componentDescriptionModi += "Control rod level: " + pair.second().toString() + '\n';
 					break;
 				case wLvl:
 					componentDescriptionNon += "Water level: " + pair.second().toString() + '\n';
 					break;
 				case RPMs:
 					componentDescriptionModi += "RPMs: " + pair.second().toString() + '\n';
 					break;
 				case psit:
 					componentDescriptionModi += "Position: " + pair.second().toString() + '\n';
 					break;
 				case elec:
 					componentDescriptionNon += "Electricity generated: " + pair.second().toString() + '\n';
 					break;
 				case OPFL:
 					componentDescriptionNon += "Output flow rate: " + pair.second().toString() + '\n';
 					break;
 				default:
 					break;
 				}
 				
 			}
 			if(componentDescriptionNon.length() != 0 && componentName.length() != 0)
 				nonmodifiable += componentName + componentDescriptionNon;
 			if(componentDescriptionModi.length() != 0 && componentName.length() != 0)
 				modifiable += componentName + componentDescriptionModi;
 			componentName = "";
 			componentDescriptionNon = "";
 			componentDescriptionModi = "";
 		}
 		window.updateLeftPanel(nonmodifiable);
 		window.updateRightPanel(modifiable);
 		
 	}
 	
 	public void saveGameState(ArrayList<InfoPacket> packets, String fileName)
 	{
 		String output = new String();
 		
 		Iterator<InfoPacket> packetIter = packets.iterator();
 		InfoPacket pckt = null;
 		while(packetIter.hasNext())
 		{
 			
 			pckt = packetIter.next();
 			Iterator<Pair<?>> namedValueIter = pckt.namedValues.iterator();
 			while(namedValueIter.hasNext())
 			{
 				Pair<?> pair = namedValueIter.next();
 				
 				output += pair.first() + '/' + pair.second().toString() + '\n';
 			}
 		}
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter("test.txt"));
 			out.write(output);
 			System.out.println(output);
 			out.close();
 		}
 		catch (IOException e)
 		{
 			System.out.println("Exception ");		
 		}
 		
 	}
 	/**
 	 * The main method for the game
 	 */
 	public static void main(String[] args){
 		// TODO create the main game loop
 		GameEngine gameEngine = new GameEngine();
 		ArrayList<InfoPacket> infoList = new ArrayList<InfoPacket>();
 		
 		InfoPacket info = new InfoPacket();
 		info.namedValues.add(new Pair<String>(Label.cNme, "Valve 1"));
 		info.namedValues.add(new Pair<Boolean>(Label.psit, true));
 		info.namedValues.add(new Pair<Double>(Label.OPFL, 12.34));
 		info.namedValues.add(new Pair<String>(Label.rcIF, "Valve 2"));
 		info.namedValues.add(new Pair<String>(Label.oPto, "Valve 2"));
 		infoList.add(info);
 		
 		info = new InfoPacket();
 		info.namedValues.add(new Pair<String>(Label.cNme, "Valve 2"));
 		info.namedValues.add(new Pair<Boolean>(Label.psit, false));
 		info.namedValues.add(new Pair<Double>(Label.OPFL, 12.34));
 		info.namedValues.add(new Pair<String>(Label.oPto, "Valve 3"));
 		info.namedValues.add(new Pair<String>(Label.rcIF, "Valve 3"));
 		infoList.add(info);
 		
 		info = new InfoPacket();
 		info.namedValues.add(new Pair<String>(Label.cNme, "Valve 3"));
 		info.namedValues.add(new Pair<Boolean>(Label.psit, false));
 		info.namedValues.add(new Pair<Double>(Label.OPFL, 12.34));
 		info.namedValues.add(new Pair<String>(Label.oPto, "Valve 2"));
 		info.namedValues.add(new Pair<String>(Label.rcIF, "Valve 2"));
 		infoList.add(info);
 		
 		gameEngine.clearPowerPlant();
 		assert(gameEngine.getAllComponentInfo().isEmpty());
 		
 		gameEngine.setupPowerPlantConfigureation(infoList);
 		gameEngine.parsercommand("Valve", "Valve 1", "close");
 		gameEngine.parsercommand("Valve", "Valve 2", "open");
 		gameEngine.parsercommand("Valve", "Valve 3", "open");
 		//gameEngine.parsercommand("Pump", "Pump 1", "on");
 		
 		gameEngine.updateInterfaceComponents(gameEngine.getAllComponentInfo());
 		gameEngine.saveGameState(gameEngine.getAllComponentInfo(), "Test.txt");
 		System.out.println("HellO");
 	}
 }
