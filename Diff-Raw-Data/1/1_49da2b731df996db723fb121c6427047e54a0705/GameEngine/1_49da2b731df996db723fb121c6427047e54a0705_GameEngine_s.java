 package controller;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.awt.EventQueue;
 import java.io.*;
 
 import model.*;
 
 import util.InfoPacket;
 import util.Pair;
 import util.Pair.Label;
 import view.*;
 
 /**
  * Game Engine for the 'Nuclear Power Plant Simulation Game' Links all the
  * technical components of the game together - the 'Controller' in the MVC
  * design
  * 
  * @author Harrison
  */
 public class GameEngine {
 	public ArrayList<Component> powrPlntComponents = null;
 
 	public MainWindow window;
 	String fileName; 
 	public boolean notFailed = true;	
 	/**
 	 * Constructor for the game engine. On creation it creates a list to store
 	 * the components of the power plant and links to a user interface (what
 	 * ever type of user interface that may be)
 	 */
 	public GameEngine() {
 		powrPlntComponents = new ArrayList<Component>();
 
 		window = new MainWindow(this, "Welcome to Fire Blanket, Post Apocalyptic Nuclear power plant simulator. Type new game and you operator name into the console to start playing.");
 
 	}
 	
 
 	
 	/**
 	 * Repairs the given component by calling the repair method of said component.
 	 * Then subtracts the repair cost of the repair from the total amount of electricity generated.
 	 * If there was not enough Electriciy, the component will not get repaired.
 	 * @param component The component to be repaired
 	 */
 	public void repair(Component component) {//Component Repair Costs
 		double pumpCost = 50;
 		double valveCost = 0;
 		double reactorCost = 500;
 		double turbineCost = 100;
 		double generatorCost = 0;
 		double condensorCost = 120;
 		
 		Iterator<Component> cIt = powrPlntComponents.iterator();
 		Component comp = null;
 		Generator generator = null;
 		double totalPower = 0;
 		
 		while(cIt.hasNext()){
 			comp = cIt.next();
 			if(comp instanceof Generator){
 				generator = (Generator) comp;
 				totalPower += generator.getElectrisityGenerated();
 				generator.setElectrisityGenerated(0);
 			}
 		}
 		if(generator == null)
 			totalPower = 10000;
 		
 		String componentName = component.getName();
 		if(componentName.contains("Valve") & totalPower >= valveCost){
 			totalPower -= valveCost;
 			component.repair();
 		}else if(componentName.contains("Pump") & totalPower >= pumpCost){
 			totalPower -= pumpCost;
 			component.repair();
 		}else if(componentName.contains("Reactor") & totalPower >= reactorCost){
 			totalPower -= reactorCost;
 			component.repair();
 		}else if(componentName.contains("Turbine") & totalPower >= turbineCost){
 			totalPower -= turbineCost;
 			component.repair();
 		}else if(componentName.contains("Generator") & totalPower >= generatorCost){
 			totalPower -= generatorCost;
 			component.repair();
 		}else if(componentName.contains("Condenser") & totalPower >= condensorCost){
 			totalPower -= condensorCost;
 			component.repair();
 		}
 		if(generator != null)
 			generator.setElectrisityGenerated(totalPower);
 	}
 
 	/**
 	 * Using a list of Info Packets (generated from loading from file
 	 * or elsewhere). Adds each of the components described in the Info Packet
 	 * list to the list of components in the power plant then sends the info
 	 * packet to that component to initialise all its values. Once all components
 	 * of the power plant are in the list and initialized, they are then all
 	 * connected together in the way described by the info packets.
 	 * 
 	 * @param allPowerPlantInfo
 	 *            A list of info packets containing all the information about
 	 *            all components to be put into the power plant.
 	 */
 	public void setupPowerPlantConfiguration(
 			ArrayList<InfoPacket> allPowerPlantInfo) {
 		
 		Iterator<InfoPacket> infoIt = allPowerPlantInfo.iterator();
 		InfoPacket currentInfo = null;
 		String currentCompName = null;
 		Component currentNewComponent = null;
 
 		
 		// Create component list.
 		while (infoIt.hasNext()) 
 		{
 			currentInfo = infoIt.next();
 			currentCompName = getComponentNameFromInfo(currentInfo);
 
 			// Determine component types we are dealing with. and initialise it.
 			if (currentCompName.contains("Condenser")) {
 				currentNewComponent = new Condenser(currentCompName, currentInfo);
 			} else if (currentCompName.contains("Generator")) {
 				currentNewComponent = new Generator(currentCompName, currentInfo);
 			} else if (currentCompName.contains("Pump")) {
 				currentNewComponent = new Pump(currentCompName, currentInfo);
 			} else if (currentCompName.contains("Reactor")) {
 				currentNewComponent = new Reactor(currentCompName, currentInfo);
 			} else if (currentCompName.contains("Turbine")) {
 				currentNewComponent = new Turbine(currentCompName, currentInfo);
 			} else if (currentCompName.contains("Valve")) {
 				currentNewComponent = new Valve(currentCompName, currentInfo);
 			} else if (currentCompName.contains("Infrastructure")){
 				currentNewComponent = new Infrastructure(currentCompName, currentInfo);
 			}
 			addComponent(currentNewComponent); // add the component to the power plant
 
 		}
 
 		// Connect components together
 		infoIt = allPowerPlantInfo.iterator();
 		
 		ArrayList<String> inputComponents = new ArrayList<String>();
 		ArrayList<String> outputComponents = new ArrayList<String>();
 
 		Iterator<Pair<?>> pairIt = null;
 		Pair<?> currentPair = null;
 		Label currentLabel = null;
 
 		Component currentComponent = null;
 
 		Iterator<String> connectionNameIt = null;
 		Component attachComp = null;
 
 		//Get info for each of the components
 		while (infoIt.hasNext()) {
 			currentInfo = infoIt.next();
 			pairIt = currentInfo.namedValues.iterator();
 
 			//Get the useful information out of the info.
 			while (pairIt.hasNext()) {
 				currentPair = pairIt.next();
 				currentLabel = currentPair.getLabel();
 
 				switch (currentLabel) {
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
 			try {
 				currentComponent = getPowerPlantComponent(currentCompName);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			// Attach each input component to the current component.
 			connectionNameIt = inputComponents.iterator();
 			while (connectionNameIt.hasNext()) {
 				try {
 					attachComp = getPowerPlantComponent(connectionNameIt.next());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				
 				if(!currentComponent.getRecievesInputFrom().contains(attachComp) & !attachComp.getOutputsTo().contains(currentComponent))
 					connectComponentTo(currentComponent, attachComp, true);
 			}
 			
 			// Attach each output component to the current compoennt
 			connectionNameIt = outputComponents.iterator();
 			while (connectionNameIt.hasNext()) {
 				try {
 					attachComp = getPowerPlantComponent(connectionNameIt.next());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				
 				if(!currentComponent.getOutputsTo().contains(attachComp) & !attachComp.getRecievesInputFrom().contains(currentComponent))
 					connectComponentTo(currentComponent, attachComp, false);
 			}
 			inputComponents.clear();
 			outputComponents.clear();
 		}
 		notFailed = true;
 	}
 
 
 	/**
 	 * Using the name of a component in the format of a string, returns the
 	 * actual Component found in the list of components of the Power Plant
 	 * 
 	 * @param currentCompName The name of a component.
 	 * @return The component specified by the given name.
 	 * @throws Exception A component with the given name could not be found
 	 */
 	public Component getPowerPlantComponent(String currentCompName) throws Exception {
 		Component currentComponent = null;
 		Iterator<Component> compIt;
 		compIt = powrPlntComponents.iterator();
 		Component c = null;
 		String cName = null;
 		
 		//Find the component we are looking for.
 		while (compIt.hasNext()) {
 			c = compIt.next();
 			cName = c.getName();
 			if (cName.equals(currentCompName)) {
 				currentComponent = c;
 			}
 		}
 		
 		if(currentComponent == null)
 				throw new Exception("The component: " + currentCompName + " does not exist");
 			
 
 		return currentComponent;
 	}
 
 	/**
 	 * Extracts the first component name out of an info packet.
 	 * @param info An info packet for a component
 	 * @return The component name contained within the given info packet.
 	 */
 	private String getComponentNameFromInfo(InfoPacket info) {
 		Iterator<Pair<?>> pairIt = info.namedValues.iterator();
 		Pair<?> pair = null;
 		String name = null;
 		while (pairIt.hasNext() && name == null) {
 			pair = pairIt.next();
 			if (pair.getLabel() == Label.cNme) {
 				name = (String) pair.second();
 			}
 		}
 		return name;
 
 	}
 
 	/**
 	 * Sends an info packet to a component the components is specified by the
 	 * name of the component in the info packet.
 	 * 
 	 * @param info Info Packet to be sent to a component
 	 */
 	public void assignInfoToComponent(InfoPacket info) throws Exception {
 		String compToSendTo = null;
 		compToSendTo = getComponentNameFromInfo(info);
 		Component com = null;
 		com = getPowerPlantComponent(compToSendTo);
 		if (com == null) {
 			throw new Exception(
 					"The component you were trying to send info to doesn't exit: " + compToSendTo);
 		} else {
 			com.takeInfo(info);
 		}
 	}
 
 	/**
 	 * Goes through the list of components one by one calling its simulate
 	 * method This should be called in a loop to get a continuous simulation.
 	 */
 	public void runSimulation() {
 		Iterator<Component> ci = powrPlntComponents.iterator();
 		Component comp = null;
 		while (ci.hasNext()) {
 			comp = ci.next();
 			comp.calculate();
 		}
 	}
 
 	/**
 	 * Add a component to the list of components
 	 * @param component The component to be added to the list of components
 	 */
 	public void addComponent(Component component) {
 		powrPlntComponents.add(component);
 	}
 
 	/**
 	 * Connect two components together, in the given order.
 	 * 
 	 * @param comp1 The component that we are working with
 	 * @param comp2 The component that will be added to comp1
 	 * @param input_output Denoted whether it is an input or an output; in = true, out = false
 	 */
 	public void connectComponentTo(Component comp1, Component comp2,
 			boolean input_ouput) {
 		if (input_ouput) {
 			comp1.connectToInput(comp2);
 			comp2.connectToOutput(comp1);
 		} else {
 			comp1.connectToOutput(comp2);
 			comp2.connectToInput(comp1);
 
 		}
 	}
 	/**
 	 * Saves the contents of the given infoPacket to the given file. 
 	 * uses '&' internally so it cannon be used in component names or pair labels
 	 * @param packets The information to be saved.
 	 * @param FileName The name of the file the data will be saved to
 	 * @return Returns a string that will be written into the console output
 	 */
 	public String saveGameState(ArrayList<InfoPacket> packets, String FileName) {
 		String output = new String();
 		fileName = FileName;
 		Iterator<InfoPacket> packetIter = packets.iterator();
 		InfoPacket pckt = null;
 		while (packetIter.hasNext()) {
 
 			pckt = packetIter.next();
 			Iterator<Pair<?>> namedValueIter = pckt.namedValues.iterator();
 			while (namedValueIter.hasNext()) {
 				Pair<?> pair = namedValueIter.next();
 				if(pair.second() != null){
 					System.out.println(pair.first() + '&' + pair.second().toString() + '\n');
 					output += pair.first() + '&' + pair.second().toString() + '\n';
 				}
 			}
 
 		}
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter("saves/" + FileName + ".fg"));
 			out.write(output);
 			System.out.println(output);
 			out.close();
 		} catch (IOException e) {
 			return "Saving failed.";
 		}
 		return "File saved";
 
 	}
 
 	/**
 	 * Reads in the contents of a given file to the game. This is done by reading each line and adding it to the appropriate
 	 * infoPacket then using the info packets to setup the state of the game and power plant.
 	 * uses '&' internally so it cannon be used in component names or pair labels
 	 * @param file The filename of the file to be read in.
 	 * @throws FileNotFoundException
 	 */
 	public void readfile(String file) throws FileNotFoundException {
 		
 		String path = new java.io.File("").getAbsolutePath() + "/saves/";
 		FileInputStream fstream = new FileInputStream(path + file + ".fg");
 		// Get the object of DataInputStream
 		DataInputStream in = new DataInputStream(fstream);
 		BufferedReader br = new BufferedReader(new InputStreamReader(in));
 		clearPowerPlant(); 
 		ArrayList<String> data = new ArrayList<String>();
 
 		ArrayList<InfoPacket> infoList = new ArrayList<InfoPacket>();
 		String temp;
 		try {
 			while ((temp = br.readLine()) != null) {
 
 				data.add(temp);	
 
 			}
 			br.close();
 
 			
 			String textData[] = new String[data.size()];	
 
 
 			textData= data.toArray(new String[0]);
 			InfoPacket info = new InfoPacket();
 			
 			int i = 0;
 			while (i < data.size()) {
 
 				String ch = "";
 				String d = "";
 				try{
 				ch = textData[i].substring(0, textData[i].indexOf("&"));
 				d = textData[i].substring(textData[i].indexOf("&") + 1, textData[i].length());
 				}catch(StringIndexOutOfBoundsException e){
 					ch = "";
 					d = "";
 				}
 				if (ch.equals(Label.cNme.toString())) {
 					if(i != 0)
 					{
 						infoList.add(info);
 						info = new InfoPacket();
 					}
 					info.namedValues.add(new Pair<String>(Label.cNme, d));
 					
 				}
 
 				else if (ch.equals(Label.falT.toString())) {
 					Double i1 = Double.parseDouble(d);
 					info.namedValues.add(new Pair<Double>(Label.falT, i1));
 
 				} 
 				else if (ch.equals(Label.OPFL.toString()))
 				{
 					Double i1 = Double.parseDouble(d);
 					info.namedValues.add(new Pair<Double>(Label.OPFL, i1));
 					System.out.println(ch + "=" + i1);
 				} else if (ch.equals(Label.psit.toString())) 
 				{
 					boolean ok = Boolean.parseBoolean(d);
 					info.namedValues.add(new Pair<Boolean>(Label.psit, ok));
 
 				}
 				else if (ch.equals(Label.oPto.toString()))
 				{
 					info.namedValues.add(new Pair<String>(Label.oPto, d));
 
 				}
 				else if (ch.equals(Label.rcIF.toString()))
 				{
 					info.namedValues.add(new Pair<String>(Label.rcIF, d));
 	
 				}
 				else if (ch.equals(Label.pres.toString()))
 				{
 					info.namedValues.add(new Pair<Double>(Label.pres, Double.parseDouble(d)));
 					
 				}
 				else if (ch.equals(Label.Vlme.toString()))
 				{
 
 					info.namedValues.add(new Pair<Double>(Label.Vlme, Double.parseDouble(d)));
 					
 				}
 				else if (ch.equals(Label.pres.toString()))
 				{
 					info.namedValues.add(new Pair<Double>(Label.pres, Double.parseDouble(d)));
 					
 				}
 				else if (ch.equals(Label.RPMs.toString()))
 				{
 
 					info.namedValues.add(new Pair<Double>(Label.RPMs, Double.parseDouble(d)));
 					
 				}
 				else if (ch.equals(Label.temp.toString()))
 				{
 					info.namedValues.add(new Pair<Double>(Label.temp, Double.parseDouble(d)));
 					
 				}
 				else if (ch.equals(Label.wLvl.toString()))
 				{
 
 					info.namedValues.add(new Pair<Double>(Label.wLvl, Double.parseDouble(d)));
 					
 				}
 				else if (ch.equals(Label.Amnt.toString()))
 				{
 
 					info.namedValues.add(new Pair<Double>(Label.Amnt, Double.parseDouble(d)));
 					
 				}
 				
 				else if (ch.equals(Label.coRL.toString()))
 				{
 
 					info.namedValues.add(new Pair<Double>(Label.coRL, Double.parseDouble(d)));
 					
 				}else if(ch.equals(Label.elec.toString())){
 					info.namedValues.add(new Pair<Double>(Label.elec, Double.parseDouble(d)));
 				}
 				i++;
 
 			}
 			infoList.add(info);
 		} catch (IOException e) {
 			System.out.println("Cannot load file");
 		}
 		setupPowerPlantConfiguration(infoList);
 
 	}
 
 	/**
 	 * Finds save files for the game.
 	 * @return Returns all available save games
 	 */
 	public String findAvailableSaves() 
 	{
 		String path = new java.io.File("").getAbsolutePath() + "/saves";
 		String files;
 		String result = new String();
 		File folder = new File(path);
 		File[] listOfFiles = folder.listFiles();
 
 		for (int i = 0; i < listOfFiles.length; i++) {
 			if (listOfFiles[i].isFile()) {
 				files = listOfFiles[i].getName();
 				if (files.endsWith(".fg") || files.endsWith(".FG")) {
 					result += files.substring(0, files.lastIndexOf('.')) + '\n';
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Get all the info from all the components within the power plant. Used for
 	 * saving and displaying info to UI.
 	 * @return List of InfoPackets for ALL components in the power plant.
 	 */
 	public ArrayList<InfoPacket> getAllComponentInfo() {
 		ArrayList<InfoPacket> allInfo = new ArrayList<InfoPacket>();
 		Iterator<Component> ci = powrPlntComponents.iterator();
 		Component comp = null;
 		while (ci.hasNext()) {
 			comp = ci.next();
 			allInfo.add(comp.getInfo());
 		}
 		return allInfo;
 
 	}
 
 	/**
 	 * Resets the components of the power plant to an empty list. Will be needed
 	 * for loading a power plant from file.
 	 */
 	public void clearPowerPlant() {
 		powrPlntComponents = new ArrayList<Component>();
 	}
 	
 	/**
 	 * Loops through the list of components in the power plant and calls the calculate method in each of them.
 	 * Resulting in the power plant moving to its next state.
 	 */
 	public void calculateAllComponents(){
 		Iterator<Component> compIt = powrPlntComponents.iterator();
 		while(compIt.hasNext()){
 			compIt.next().calculate();
 		}
 	}
 
 	/**
 	 * Updates interface with the latest changes to all the components. Sorts all the info
 	 * into modifiable components and non modifiable components
 	 * @param packets Info about all the components in the game
 	 */
 	public void updateInterfaceComponents(ArrayList<InfoPacket> packets) 
 	{
 		String nonmodifiable = new String();
 		String modifiable = new String();
 		Iterator<InfoPacket> packetIter = packets.iterator();
 		InfoPacket pckt = null;
 		// All info gathered about a component
 		String componentName = new String();
 		String componentDescriptionModi = new String();
 		String componentDescriptionNon = new String();
 		DecimalFormat threeSignificant = new DecimalFormat("#.###");
 		while (packetIter.hasNext()) {
 
 			pckt = packetIter.next();
 			Iterator<Pair<?>> namedValueIter = pckt.namedValues.iterator();
 			while (namedValueIter.hasNext()) {
 				Pair<?> pair = namedValueIter.next();
 				Label currentLabel = pair.getLabel();
 
 				switch (currentLabel) {
 				case cNme:
 					componentName = (String) pair.second() + '\n';
 					break;
 					
 				case temp:
 					componentDescriptionNon += "Temperature: "
 							+ Double.valueOf(threeSignificant.format(pair.second())).toString() + '\n';
 					break;
 				case pres:
 					componentDescriptionNon += "Pressure: "
 							+ Double.valueOf(threeSignificant.format(pair.second())).toString() + '\n';
 					break;
 				case coRL:
 					componentDescriptionModi += "Control rod level: "
 							+ Double.valueOf(threeSignificant.format(pair.second())).toString() + '\n';
 					break;
 				case wLvl:
 					componentDescriptionNon += "Water level: "
 							+ Double.valueOf(threeSignificant.format(pair.second())).toString() + '\n';
 					break;
 				case RPMs:
 					componentDescriptionModi += "RPMs: "
 							+ Double.valueOf(threeSignificant.format(pair.second())).toString() + '\n';
 					break;
 				case psit:
 					componentDescriptionModi += "Position: "
 							+ pair.second().toString() + '\n';
 					break;
 				case elec:
 					componentDescriptionNon += "Electricity generated: "
 							+ Double.valueOf(threeSignificant.format(pair.second())).toString() + '\n';
 					break;
 				case OPFL:
 					componentDescriptionNon += "Output flow rate: "
 							+ Double.valueOf(threeSignificant.format(pair.second())).toString() + '\n';
 					break;
 				default:
 					break;
 				}
 
 			}
 			if (componentDescriptionNon.length() != 0
 					&& componentName.length() != 0)
 				nonmodifiable += componentName + componentDescriptionNon + '\n';
 			if (componentDescriptionModi.length() != 0
 					&& componentName.length() != 0)
 				modifiable += componentName + componentDescriptionModi + '\n';
 			componentName = "";
 			componentDescriptionNon = "";
 			componentDescriptionModi = "";
 		}
 		window.updateLeftPanel(nonmodifiable);
 		window.updateRightPanel(modifiable);
 
 	}
 
 	
 	
 	/**
 	 * Checks if a file name is already stored in the system (happens if the game was started using load or if save as command was previously used)
 	 *  and can be used for further save games
 	 * @param packets the state of the power plant that needs to be save
 	 * @return returns what should be out put to the console
 	 */
 	public String save(ArrayList<InfoPacket> packets) {
 
 		if (fileName != null) {
 			saveGameState(packets, fileName);
 			return "File saved";
 		} else {
 			return "Use save as command, because there is no valid filename";
 		}
 
 	}
 	
 	public ArrayList<Component> componentFailed(){
 		Iterator<Component> comIt = powrPlntComponents.iterator();
 		Component com = null;
 		
 		ArrayList<Component> failedComps = new ArrayList<Component>();
 		while(comIt.hasNext()){
 			com = comIt.next();
 			if(com.getFailed())
 				failedComps.add(com);
 		}
 		return failedComps;
 	}
 			
 		
 
 	
 	public static void main(String[] args) {
 		GameEngine gameEngine = new GameEngine();
 		 
 		gameEngine.clearPowerPlant();
 		assert (gameEngine.getAllComponentInfo().isEmpty());
 		int autosaveTime = 0;
 		while(true)
 		{
 			String failedComps = "These components have currently failed: ";			//Gather a list of failed components.
 			String failedComp = "";
 
 			while(gameEngine.notFailed){
 				autosaveTime++;
 				
 				gameEngine.calculateAllComponents(); 										//Calculate new Values.
 				gameEngine.updateInterfaceComponents(gameEngine.getAllComponentInfo());		//Update the Screen with the current Values calculate
 				
 				ArrayList<Component> failedC = gameEngine.componentFailed();
 				if(failedC.size() > 0){
 					Iterator<Component> cIt = failedC.iterator();
 					while(cIt.hasNext()){
 						failedComp = cIt.next().getName();
 						if(failedComp.contains("Reactor")){
 							gameEngine.notFailed = false;
 						}
 						failedComps += failedComp + ", ";
 					}
 					gameEngine.window.console.writeToConsole(failedComps);						//Output the names of components that are currently failed.
 					if(!gameEngine.notFailed){
 						gameEngine.window.console.writeToConsole("GAME OVER");
 					}
 				}
 				if(autosaveTime == 10){
 					if(gameEngine.powrPlntComponents.size() > 0)
 						gameEngine.saveGameState(gameEngine.getAllComponentInfo(), "autosave");	//Save the game state to autosave file
 					autosaveTime = 0;
 				}
 				
 				try {
 					Thread.sleep(700);														//Wait until next iteration.
 				} catch(InterruptedException ex) {
 					Thread.currentThread().interrupt();
 				}
 			}
 		}
 	}
 }
