 package project1;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import uchicago.src.sim.engine.BasicAction;
 import uchicago.src.sim.engine.Schedule;
 import uchicago.src.sim.engine.SimInit;
 import uchicago.src.sim.engine.SimModelImpl;
 import uchicago.src.sim.gui.DisplaySurface;
 import uchicago.src.sim.gui.ColorMap;
 import uchicago.src.sim.gui.Value2DDisplay;
 import uchicago.src.sim.util.SimUtilities;
 
 public class Model extends SimModelImpl{
 
 	private Schedule schedule;
 
 	private World worldSpace;
 	private DisplaySurface displaySurf;
 	private ArrayList<Rabbit> rabbitList;
 
 
 	// Default Values
 	private static final int NUMRABBITS = 20;
 	private static final int WORLDXSIZE = 20;
 	private static final int WORLDYSIZE = 20;
 	private static final int REPRODUCTIONCOST = 5;
 	private static final int INITIALGRASS = 30;
 	private static final int GRASSGROWTHRATE = 10;
 	private static final int INITIALENERGY = 10;
 
 
 	private int numRabbits = NUMRABBITS;
 	private int worldXSize = WORLDXSIZE;
 	private int worldYSize = WORLDYSIZE;
 	private int reproductionCost = REPRODUCTIONCOST;
 	private int initialGrass = INITIALGRASS;
 	private int GrassGrowthRate = GRASSGROWTHRATE;
 	private int initialEnergy = INITIALENERGY;
 
 
 	public String getName(){
 		return "Rabbits World Simulator";
 	}
 
 	public void setup(){
 		System.out.println("System Setup");
 		schedule = new Schedule(1);
 		worldSpace = null;
 		rabbitList = new ArrayList<Rabbit>();
 
 		if (displaySurf != null){
 			displaySurf.dispose();
 		}
 		displaySurf = null;
 
 		displaySurf = new DisplaySurface(this, "Rabbit World Simulator");
 
 		registerDisplaySurface("Rabbit World Simulator", displaySurf);
 	}
 
 	public void begin(){
 		buildModel();
 		buildSchedule();
 		buildDisplay();
 
 		displaySurf.display();
 	}
 
 	public void buildModel(){
 		System.out.println("Running BuildModel");
 
 		worldSpace = new World(worldXSize, worldYSize);
 		worldSpace.growGrass(initialGrass);
 
 		for(int i = 0; i < numRabbits; i++) {
 			addNewRabbit();
 		}
 	}
 
 	public void buildSchedule(){
 		System.out.println("Running BuildSchedule");
 
 		class worldStep extends BasicAction {
 			public void execute() {
 				worldSpace.growGrass(GrassGrowthRate);
 				deleteDeadrabbits();
 
 				SimUtilities.shuffle(rabbitList);
				for(int i = 0; i < rabbitList.size(); i++){
					Rabbit rabbit = rabbitList.get(i);
 					rabbit.moveRabbit();
 					rabbit.eatGrass();
 					rabbit.report();
 				}
 				makeRabbitReproduction();
 				displaySurf.updateDisplay();
 			}
 		}
 
 		schedule.scheduleActionBeginning(0, new worldStep());		
 
 		class LivingRabbit extends BasicAction {
 			public void execute(){
 				livingRabbits();
 				displaySurf.updateDisplay();
 			}
 		}
 
 		schedule.scheduleActionAtInterval(10, new LivingRabbit());
 	}
 	private int deleteDeadrabbits(){
 		int deadRabbits = 0;		
 		for(int i = (rabbitList.size() - 1); i >= 0 ; i--){
 			Rabbit rabbit = (Rabbit)rabbitList.get(i);
 			if(rabbit.getEnergy() ==0){
 				worldSpace.removeRabbitAt(rabbit.getPositionX(), rabbit.getPositionY());
 				rabbitList.remove(i);
 				deadRabbits++;
 			}
 		}
 		System.out.println("Number of dead rabbits is: " + deadRabbits);
 		return deadRabbits;
 	}
 	private int makeRabbitReproduction(){
 		int newRabbits = 0;
 		for(int i = rabbitList.size()-1; i >= 0; i--){
 			Rabbit rabbit = (Rabbit)rabbitList.get(i);
 			if(rabbit.getEnergy() >= 6 ){
				Rabbit newRabbit= new Rabbit(initialEnergy, worldSpace);
 				if(worldSpace.placeRabbit(newRabbit)){
 					rabbit.setEnergy(rabbit.getEnergy()- reproductionCost);
 					rabbitList.add(newRabbit);
 					newRabbits++;
 				}
 				else{
 					//grid is full, can not generate new rabbits
 					newRabbit= null;
 				}
 			}
 		}
 		System.out.println("Number of new rabbits is: " + newRabbits);
 		return newRabbits;
 	}
 
 
 	private int livingRabbits(){
 		int livingRabbits = 0;
 		for(int i = 0; i < rabbitList.size(); i++){
 			Rabbit rabbit = (Rabbit)rabbitList.get(i);
 			if(rabbit.getEnergy() > 0) livingRabbits++;
 		}
 		System.out.println("Number of living rabbits is: " + livingRabbits);
 
 		return livingRabbits;
 	}
 
 	public void buildDisplay(){
 		System.out.println("Running BuildDisplay");
 
 		ColorMap map = new ColorMap();
 
 		for(int i = 0; i < 16; i++) {
 			map.mapColor(i, new Color((int)(i*8 + 127), 0, 0));
 		}
 		map.mapColor(0, Color.white);
 
 		Value2DDisplay displayGrass = new Value2DDisplay(worldSpace.getCurrentGrassSpace(), map);
 
 		displaySurf.addDisplayable(displayGrass, "Grass");
 
 	}
 
 	public Schedule getSchedule(){
 		return schedule;
 	}
 
 	public String[] getInitParam(){
 		System.out.println("Init parameters");
 		String[] initParams = { "NumRabbits", "InitialEnergy", "WorldXSize", "WorldYSize", "ReproductionCost", "initialGrass", "GrassGrowthRate" };
 		return initParams;
 	}
 
 	public int getNumRabbits(){
 		return numRabbits;
 	}
 
 	public void setNumRabbits(int na){
 		numRabbits = na;
 	}
 	
 	public int getInitialEnergy(){
 		return initialEnergy;
 	}
 
 	public void setInitialEnergy(int en){
 		initialEnergy = en;
 	}
 
 	public int getWorldXSize() {
 		return worldXSize;
 	}
 
 	public void setWorldXSize(int x) {
 		worldXSize = x;
 	}
 
 	public int getWorldYSize() {
 		return worldYSize;
 	}
 
 	public void setWorldYSize(int y) {
 		worldXSize = y;
 	}
 
 	public int getReproductionCost() {
 		return reproductionCost;
 	}
 
 	public void setReproductionCost(int rc) {
 		reproductionCost = rc;
 	}
 
 	public int getGrassGrowthRate() {
 		return GrassGrowthRate;
 	}
 
 	public void setGrassGrowthRate(int grassGrowthRate) {
 		GrassGrowthRate = grassGrowthRate;
 	}
 	public int getInitialGrass() {
 		return initialGrass;
 	}
 
 	public void setInitialGrass(int initialGrass) {
 		this.initialGrass = initialGrass;
 	}
 
 	public void addNewRabbit() {
		Rabbit r = new Rabbit(initialEnergy, worldSpace);
 		rabbitList.add(r);
 		worldSpace.placeRabbit(r);
 		r.report();
 	}
 
 	public static void main(String[] args) {
 		SimInit init = new SimInit();
 		Model model = new Model();
 		init.loadModel(model, null, false);
 	}
 
 
 }
