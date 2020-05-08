 package controller;
 
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Point;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import ai.AiState;
 import ai.BotAiStateManager;
 
 import java.util.Random;
 
 import model.*;
 import view.*;
 
 @SuppressWarnings("serial")
 public class HomeProtectorFrame extends JFrame implements ActionsPanelDelegate, StageBasedLoopDelegate {
 	private final int defaultWorldWidth = 10;
 	private final int defaultWorldHeight = 10;
 	private final double randomActionProbability = .1;
 	
 	public World worldModel;
 	public WorldPanel worldPanel;
 	public BotAiStateManager aiManager;
 	
 	public JLabel stateLabel;
 	public JLabel stateValue;
 	
 	public JLabel inventoryLabel;
 	public JLabel inventoryValue;
 	
 	public JLabel energyLabel;
 	public EnergyLevelPanel energyValue;
 	
 	public JLabel actionsLabel;
 	public ActionsPanel actionsPanel;
 	
 	private boolean runWorld = false;
 	private boolean randomActionsEnabled = false;
 	
 	private StageBasedLoop loopController;
 	
 	public int preferredWidth() {
 		return 900;
 	}
 	
 	public int preferredHeight() {
 		return 600;
 	}
 	
 	private ArrayList<Model> defaultObjects() {
 		ArrayList<Model> ret = new ArrayList<Model>();
 		
 		//protector bot
 		ProtectorBot bot = new ProtectorBot(9, 5);
 		ret.add(bot);
 		this.worldModel.setBot(bot);
 		
 		//add base station
 		BaseStation baseStation = new BaseStation(0, 5);
 		baseStation.setHasExtinguisher(true);
 		ret.add(baseStation);
 		this.worldModel.setStation(baseStation);
 		
 		ArrayList<Point> openPoints = openPoints();
 		Random generator = new Random();
 		//add dirt
 		for(int i = 0; i < 10 && !openPoints.isEmpty(); i++) {
 			int r = Math.abs(generator.nextInt()) % openPoints.size();
 			Point p = openPoints.get(r);
 			Dirt d = new Dirt(p.x, p.y);
 			ret.add(d);
 			openPoints.remove(r);
 		}
 		
 		//add obstructions
 		for(int i = 0; i < 20 && !openPoints.isEmpty(); i++) {
 			int r = Math.abs(generator.nextInt()) % openPoints.size();
 			Point p = openPoints.get(r);
 			Obstruction o = new Obstruction(p.x, p.y);
 			ret.add(o);
 			openPoints.remove(r);
 		}
 		
 		return ret;
 	}
 	
 	public HomeProtectorFrame(String title) {
 		super(title);
 		
 		this.setLayout(null);
 		
 		worldModel = new World(defaultWorldWidth, defaultWorldHeight);
 		
 		worldPanel = new WorldPanel(worldModel);
 		worldPanel.setBounds(15, 15, 540, 540);
 		this.add(worldPanel);
 		
 		ArrayList<Model> startingObjects = defaultObjects();
 		for(Model m : startingObjects) {
 			worldModel.addObject(m);
 		}
 		
 		aiManager = new BotAiStateManager(worldModel, worldModel.getBot());
 		
 		Font font = new Font(null, Font.BOLD, 20);
 		
 		stateLabel = new JLabel("AI State:", (int)Component.CENTER_ALIGNMENT);
 		stateLabel.setBounds(600, 25, 250, 34);
 		stateLabel.setFont(new Font(null, Font.BOLD, 30));
 		this.add(stateLabel);
 		stateValue = new JLabel("Idle", (int)Component.CENTER_ALIGNMENT);
 		stateValue.setBounds(600, 65, 250, 30);
 		stateValue.setFont(new Font(null, Font.BOLD, 16));
 		this.add(stateValue);
 		
 		energyLabel = new JLabel("Power:", (int)Component.CENTER_ALIGNMENT);
		energyLabel.setBounds(585, 130, 100, 40);
 		energyLabel.setFont(font);
 		this.add(energyLabel);
 		energyValue = new EnergyLevelPanel(worldModel.getBot());
		energyValue.setBounds(585, 170, 100, 40);
 		this.add(energyValue);
 		
 		inventoryLabel = new JLabel("Extinguisher:", (int)Component.CENTER_ALIGNMENT);
		inventoryLabel.setBounds(720, 130, 150, 40);
 		inventoryLabel.setFont(font);
 		this.add(inventoryLabel);
 		inventoryValue = new JLabel("Unequipped", (int)Component.CENTER_ALIGNMENT);
		inventoryValue.setBounds(720, 170, 150, 30);
 		inventoryValue.setFont(font);
 		this.add(inventoryValue);
 		
		actionsLabel = new JLabel("Actions:", (int)Component.CENTER_ALIGNMENT);
 		actionsLabel.setBounds(600, 250, 250, 40);
		actionsLabel.setFont(font);
 		this.add(actionsLabel);
 		
 		actionsPanel = new ActionsPanel();
 		actionsPanel.setBounds(600, 300, 250, 250);
 		actionsPanel.setDelegate(this);
 		this.add(actionsPanel);
 		
 		loopController = new StageBasedLoop(this);
 	}
 	
 	private void updateDisplay() {
 		ProtectorBot b = this.worldModel.getBot();
 		
 		AiState state = this.worldModel.getBot().getAiState();
 		if(state != null) {
 			this.stateValue.setText(state.prettyName());
 		} else {
 			this.stateValue.setText("Idle");
 		}
 		
 		if(b.hasExtinguisher()) {
 			this.inventoryValue.setText("Equipped");
 		} else {
 			this.inventoryValue.setText("Unequipped");
 		}
 		
 		this.energyValue.repaint();
 		this.worldPanel.repaint();
 	}
 	
 	private ArrayList<Point> openPoints() {
 		ArrayList<Point> openPoints = new ArrayList<Point>();
 		for(int r = 0; r < this.worldModel.getHeight(); r++) {
 			for(int c = 0; c < this.worldModel.getWidth(); c++) {
 				Point point = new Point(c, r);
 				if(this.worldModel.objectAtPosition(point) == null) {
 					openPoints.add(point);
 				}
 			}
 		}
 		return openPoints;
 	}
 	
 	private Point getRandomOpenSpace() {
 		ArrayList<Point> openPoints = openPoints();
 		if(openPoints.size() > 0) {
 			Random generator = new Random();
 			return openPoints.get(Math.abs(generator.nextInt()) % openPoints.size());
 		}
 		return null;
 	}
 	
 	//StageBasedLoopDelegate methods
 	public void stage1() {
 		if(randomActionsEnabled) {
 			Random generator = new Random();
 			int r = Math.abs(generator.nextInt()) % 100;
 			double p = randomActionProbability * 100;
 			
 			if(p > r) {
 				Point openPoint = this.getRandomOpenSpace();
 
 				if(openPoint != null) {
 					if(Math.abs(generator.nextInt()) % 5 == 0) {
 						System.out.println("Adding Fire");
 						Fire fire = new Fire(openPoint.x, openPoint.y);
 						worldModel.addObject(fire);
 					} else {
 						System.out.println("Adding Dirt");
 						Dirt dirt = new Dirt(openPoint.x, openPoint.y);
 						worldModel.addObject(dirt);
 					}
 					this.updateDisplay();
 				}
 			}
 		}
 	}
 	
 	public void stage2() {
 		aiManager.updateBotAiState();
 		updateDisplay();
 	}
 
 	public void stage3() {
 		ProtectorBot bot = this.worldModel.getBot();
 		Action action = bot.getAiState().action();
 		this.worldModel.executeAction(action);
 		
 		updateDisplay();
 	}
 	
 	public boolean keepLooping() {
 		return true;
 	}
 	
 	//ActionsPanelDelegate methods
 	public void addDirt() {
 		Point openPoint = this.getRandomOpenSpace();
 		
 		if(openPoint != null) {
 			Dirt dirt = new Dirt(openPoint.x, openPoint.y);
 			worldModel.addObject(dirt);
 
 			this.updateDisplay();
 		}
 		if(runWorld) loopController.restart();
 	}
 	public void addFire() {
 		Point openPoint = this.getRandomOpenSpace();
 
 		if(openPoint != null) {
 			Fire fire = new Fire(openPoint.x, openPoint.y);
 			worldModel.addObject(fire);
 		
 			this.updateDisplay();
 		}
 		if(runWorld) loopController.restart();
 	}
 	public void removeAllDirt() {
 		for(int r = 0; r < this.worldModel.getHeight(); r++) {
 			for(int c = 0; c < this.worldModel.getWidth(); c++) {
 				Point point = new Point(c, r);
 				Model m = this.worldModel.objectAtPosition(point);
 				if(m != null && m instanceof Dirt) {
 					Model stackedObject = ((GroundObject)m).getStackedObject();
 					this.worldModel.removeObjectAtPosition(point);
 					if(stackedObject != null) {
 						this.worldModel.addObject(stackedObject);
 					}
 				}
 			}
 		}
 		this.updateDisplay();
 		
 		if(runWorld) loopController.restart();
 	}
 	public void removeAllFire() {
 		for(int r = 0; r < this.worldModel.getHeight(); r++) {
 			for(int c = 0; c < this.worldModel.getWidth(); c++) {
 				Point point = new Point(c, r);
 				Model m = this.worldModel.objectAtPosition(point);
 				if(m != null && m instanceof Fire) {
 					this.worldModel.removeObjectAtPosition(point);
 				}
 			}
 		}
 		this.updateDisplay();
 		
 		if(runWorld) loopController.restart();
 	}
 	
 	public void aquireExtinguisher() {
 		this.worldModel.getBot().pickupFireExtinguisher();
 		this.worldModel.getStation().setHasExtinguisher(false);
 		
 		this.updateDisplay();
 	}
 	public void replaceExtinguisher() {
 		this.worldModel.getBot().dropFireExtinguisher();
 		this.worldModel.getStation().setHasExtinguisher(true);
 		
 		this.updateDisplay();
 	}
 	
 	public void fillEnergey() {
 		this.worldModel.getBot().chargeBattery();
 		
 		this.updateDisplay();
 	}
 	public void depleteEnergy() {
 		this.worldModel.getBot().drainBattery();
 		
 		this.updateDisplay();
 	}
 	
 	public void enableRandomActions() {
 		randomActionsEnabled = true;
 	}
 	public void disableRandomActions() {
 		randomActionsEnabled = false;
 	}
 	
 	public void start() {
 		runWorld = true;
 		loopController.run();
 	}
 	public void stop() {
 		runWorld = false;
 		loopController.stop();
 	}
 
 	@Override
 	public void addObstruction() {
 		Point openPoint = this.getRandomOpenSpace();
 
 		if(openPoint != null) {
 			Obstruction o = new Obstruction(openPoint.x, openPoint.y);
 			worldModel.addObject(o);
 		
 			this.updateDisplay();
 		}
 		if(runWorld) loopController.restart();
 	}
 	@Override
 	public void removeAllObstructions() {
 		for(int r = 0; r < this.worldModel.getHeight(); r++) {
 			for(int c = 0; c < this.worldModel.getWidth(); c++) {
 				Point point = new Point(c, r);
 				Model m = this.worldModel.objectAtPosition(point);
 				if(m != null && m instanceof Obstruction) {
 					this.worldModel.removeObjectAtPosition(point);
 				}
 			}
 		}
 		this.updateDisplay();
 		
 		if(runWorld) loopController.restart();
 	}
 }
