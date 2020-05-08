 package factory;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 
 import agent.Agent;
 import java.util.*;
 
 //import factory.KitConfig.MyPart;
 import factory.interfaces.*;
 import factory.masterControl.MasterControl;
 import factory.test.mock.MockGantry;
 
 //test commit/push
 public class FCSAgent extends Agent implements FCS{
 	public Queue<KitConfig> orders = new LinkedList<KitConfig>(); //added this queue for kits
 	public PartsRobot partsRobot;
 	Gantry gantry;
 	BinConfig binConfig; //no need for this the way CS200 is designing the bins?
 	public boolean passBinConfigurationToGantry = false;
 	public boolean freeToMakeKit = true;
 	public Map<String, Part> partsList = new HashMap<String, Part>();
 	public Map<String, KitConfig> kitRecipes = new HashMap<String, KitConfig>();
 
 	public enum KitProductionState { PENDING, PRODUCING, FINISHED, PURGING }
 	public KitProductionState state = KitProductionState.PENDING;
 
 	public int kitsExportedCount = 0;
 
 	public MasterControl masterControl;
 	//this is temporarily used for testing purposes.  Constructor will likely change.
 	public FCSAgent(Gantry gantry, PartsRobot partsRobot, MasterControl mc) {
 		super(mc);
 		this.masterControl = mc; // for testing purposes
 		this.gantry = gantry;
 		this.partsRobot = partsRobot;
 		loadData();
 		testImport();
 	}
 	public FCSAgent(MasterControl mc){
 		super (mc);
 		this.masterControl = mc;
 		loadData();
 		testImport();

 	}
 


 	// *** MESSAGES ***
 
 	public void msgInitialize(BinConfig binConfig) {
 		this.binConfig = binConfig;
 		this.passBinConfigurationToGantry = true;
 		stateChanged();
 	}
 
 	//msg from Panel to produce a kit
 	public void msgProduceKit(int quantity, String kitName) {
 		System.out.println("IT WORKS!");
 		KitConfig recipe = new KitConfig();
 		recipe = kitRecipes.get(kitName);
 		recipe.quantity = quantity;
 		recipe.kitName = kitName;
 		orders.add(recipe);
 		stateChanged();
 	}
 
 	//msg from KitRobot that says kit is finished. ADDED THIS
 	public void kitIsFinished(){
 		this.state = KitProductionState.FINISHED;
 		stateChanged();
 	}
 
 	public void msgKitIsExported(Kit kit){
 		kitsExportedCount++;
 		if(kitsExportedCount >= orders.peek().quantity){
 			this.state = KitProductionState.FINISHED;
 		}
 		//send message to manager to say that kit is exported
 		stateChanged();
 	}
 
 
 	// *** SCHEDULER ***
 	public boolean pickAndExecuteAnAction() {
 		if (this.passBinConfigurationToGantry == true){
 			changeGantryBinConfig();
 			return true;
 		}
 
 		//added this in the scheduler
 		if (this.state == KitProductionState.FINISHED){
 			startProducingNextKit();
 			return true;
 		}
 
 		if (this.state == KitProductionState.PENDING && !orders.isEmpty()){ //added freeToMakeKit
 			sendKitConfigToPartRobot();
 			return true;
 		}
 		return false;
 	}
 
 	// *** ACTIONS ***
 	/**
 	 * Passes down the new configuration to the Gantry
 	 */
 	private void changeGantryBinConfig(){
 		gantry.msgChangeGantryBinConfig(this.binConfig);
 		this.passBinConfigurationToGantry = false;
 		stateChanged();
 	}
 
 	/**
 	 * Passes down the new Kit Configuration to the PartsRobot Agent
 	 */
 	private void sendKitConfigToPartRobot() { 
 		
 		/**THIS IS TEMPORARY, //TODO for now send message to gantry instead of parts robot to test swing->agent->animation**/
 		this.gantry.msgFeederNeedsPart(partsList.get("Shoe"), this.masterControl.f0);
 //		this.partsRobot.msgMakeKit(orders.peek()); //TODO uncomment this for v1 implementation
 		this.state = KitProductionState.PRODUCING;
 		kitsExportedCount = 0;
 		stateChanged();
 	}
 
 	//added this action
 	private void startProducingNextKit(){
 		orders.remove();
 		this.state = KitProductionState.PENDING;
 		stateChanged();
 	}
 
 
 	// *** OTHER METHODS ***
 	public void addPartType(String name, double nestStabilizationTime, String description, int id, String imagePath){
 		Part part = new Part();
 		part.name = name;
 		part.nestStabilizationTime = nestStabilizationTime;
 		part.description = description;
 		part.id = id;
 		part.imagePath = imagePath;
 		partsList.put(name, part);
 	}
 
 	public void addKitRecipe(String name, List<String> partsRequired){
 		KitConfig recipe = new KitConfig();
 		if (partsRequired.size() != 0){
 			for (String p: partsRequired){
 				Part part = new Part();
 				part = partsList.get(p);
 				recipe.listOfParts.add(part);
 				recipe.kitName = name;
 			}
 		}
 		kitRecipes.put(name, recipe);
 	}
 
 	public void testImport() {
 		System.out.println("Recipes" + kitRecipes.keySet());
 		System.out.println("Parts" + partsList.keySet());
 		System.out.println("LIST OF PARTS FOR RECIPE KIT2");
 		for (int i=0; i < kitRecipes.get("Kit2").listOfParts.size(); i++){
 			System.out.println(kitRecipes.get("Kit2").listOfParts.get(i).name);
 		}
 	}
 	// Load Data - remember to import the file - FOR EVERYONE
 	public void loadData(){
 		FileInputStream f;
 		ObjectInputStream o;
 		try{    // loads previously saved player data
 			f = new FileInputStream("InitialData/initialParts.ser");
 			o = new ObjectInputStream(f);
 			partsList = (HashMap<String,Part>) o.readObject();
 			System.out.println("Good");
 			o.close();
 		}catch(IOException e){
 			e.printStackTrace();
 		} catch(ClassNotFoundException c){
 			c.printStackTrace();
 		}
 		try{    // loads previously saved player data
 			f = new FileInputStream("InitialData/initialKitConfigs.ser");
 			o = new ObjectInputStream(f);
 			kitRecipes = (HashMap<String,KitConfig>) o.readObject();
 			System.out.println("Good");
 			o.close();
 		}catch(IOException e){
 			e.printStackTrace();
 		}catch(ClassNotFoundException c){
 			c.printStackTrace();
 		}
 	}
 
 }
