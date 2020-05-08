 package game.logic;
 
 import game.ui.CLInterface;
 import game.ui.EagleEvent;
 import game.ui.FightEvent;
 import game.ui.GameEvent;
 import game.ui.GameInput;
 import game.ui.GameOptions;
 import game.ui.GameOutput;
 import game.ui.ResultEvent;
 import general_utilities.MazeInput;
 import general_utilities.WaitTime;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Random;
 
 import maze_objects.Dragon;
 import maze_objects.Hero;
 import maze_objects.Maze;
 import maze_objects.MazeBuilder;
 import maze_objects.MazeDirector;
 import maze_objects.PredefinedMaze;
 import maze_objects.RandomMaze;
 import maze_objects.Sword;
 import maze_objects.Eagle;
 
 
 public class Game {
 
 	//Exit states
 	public static final int OPEN = 1;
 	public static final int CLOSED = 0;
 
 	/*** Private Attributes ***/
 
 	//State Attributes
 	private int dragonType;
 	private int exit_state;
 	private int number_of_dragons;
 	private int remaining_dragons;
 
 	//Game elements
 	private Maze maze;
 	private Sword sword;
 	private Hero hero;
 	private Eagle eagle;
 	//private Dragon dragon;
 	private ArrayList<Dragon> dragons;
 	private LinkedList<GameEvent> events = new LinkedList<GameEvent>();
 
 	/*** Private Methods ***/
 
 	//Game Initializers
 	private Hero spawnHero() { //Creates a hero object on a random valid position in the maze
 		Random random = new Random();
 		int hero_row = 0;
 		int hero_column = 0;
 
 		do {
 			hero_row = random.nextInt(maze.getRows());
 			hero_column = random.nextInt(maze.getColumns());
 		} while (!maze.checkIfEmpty(hero_row, hero_column));
 
 		Hero h = new Hero(hero_row, hero_column);
 		return h;
 	}
 
 	private Sword spawnSword() { //Creates a sword object on a random valid position in the maze
 		Random random = new Random();
 		int sword_row = 0;
 		int sword_column = 0;
 
 		do {
 			sword_row = random.nextInt(maze.getRows());
 			sword_column = random.nextInt(maze.getColumns());
 		} while (!maze.checkIfEmpty(sword_row, sword_column) || nextToHero(sword_row, sword_column) || isOnDragon(sword_row, sword_column));
 
 		Sword sd = new Sword(sword_row, sword_column);
 		return sd;
 	}
 
 	private Dragon spawnDragon() { //Creates a dragon object on a random valid position in the maze
 		Random random = new Random();
 		int dragon_row = 0;
 		int dragon_column = 0;
 
 		do {
 			dragon_row = random.nextInt(maze.getRows());
 			dragon_column = random.nextInt(maze.getColumns());
 		} while (!maze.checkIfEmpty(dragon_row, dragon_column) || nextToHero(dragon_row, dragon_column) || nextToDragons(dragon_row, dragon_column));
 
 		Dragon dragon = new Dragon(dragon_row, dragon_column, dragonType);
 		return dragon;
 	}
 
 	private Eagle spawnEagle(int row, int column) { 
 		Eagle eagle = new Eagle(row, column, true);
 		return eagle;
 	}
 
 	private ArrayList<Dragon> spawnDragons() {
 		ArrayList<Dragon> ds = new ArrayList<Dragon>();
 		for(int i = 0; i < number_of_dragons; i++)
 			ds.add(spawnDragon());
 
 		return ds;
 	}
 
 	private boolean isOnEagle(int row, int column) { //Returns true if the position given is the same as the eagle's and the eagle is alive
 		return(row == eagle.getRow() && column == eagle.getColumn() && eagle.getState() != Eagle.DEAD);
 	}
 
 	private boolean nextToHero(int row, int column) { //True if the object is adjacent to the hero (horizontally, vertically or on top), false if not
 		return((row == hero.getRow() + 1 && column == hero.getColumn()) ||
 				(row == hero.getRow() - 1  && column == hero.getColumn()) ||
 				(column == hero.getColumn() + 1 && row == hero.getRow()) ||
 				(column == hero.getColumn() - 1 && row == hero.getRow()) ||
 				(column == hero.getColumn() && row == hero.getRow()));
 	}
 
 	private boolean checkDragonEncounters(boolean goOn) { //Processes encounters between the hero and a dragon
 
 		for(int i = 0; i < dragons.size(); i++)
			if (hero.getState() != Hero.DEAD 
			        && nextToOneDragon(hero.getRow(), hero.getColumn(), dragons.get(i)) 
			        && !(hero.getState() != Hero.ARMED 
			        && dragons.get(i).getState() == Dragon.ASLEEP)) {
 				if(fightDragon(dragons.get(i))) {
 					FightEvent wonFight = new FightEvent("wonFight");
 					events.add(wonFight);
 				}
 				else {
 					goOn = false;
 					FightEvent lostFight = new FightEvent("lostFight");
 					events.add(lostFight);
 					return goOn;
 				}
 			}
 
 		return goOn;
 	}
 
 	private void checkEagleEncounters() {
 		for(int i = 0; i < dragons.size(); i++)
 			if(eagle.getState() != Eagle.DEAD && nextToOneDragon(eagle.getRow(), eagle.getColumn(), dragons.get(i))) {
 				eagle.killEagle();
 				EagleEvent eagleKilled = new EagleEvent("killed");
 				events.add(eagleKilled);
 			}
 	}
 
 	private boolean moveDragons(boolean goOn) { //Executes the dragons' turn
 		for(int i = 0; i < dragons.size(); i++)
 			if(dragons.get(i).getType() != Dragon.STATIC && (dragons.get(i).getState() == Dragon.ALIVE || dragons.get(i).getState() == Dragon.ASLEEP)) {
 				dragons.get(i).moveDragon(this);
 
				// WARP KILLS
				//goOn = checkDragonEncounters(goOn);
 			}
 		return goOn;
 	}
 
 	private void tryToSendEagle() {
 		if(eagle.getState() == Eagle.DEAD) {
 			EagleEvent ed = new EagleEvent("cantSendDead");
 			events.add(ed);
 		}
 		else if(!eagle.isWithHero()) {
 			EagleEvent or = new EagleEvent("cantSendOnRoute");
 			events.add(or);
 		}
 		else if(sword.isTaken()) {
 			EagleEvent ns = new EagleEvent("noSword");
 			events.add(ns);
 		}
 		else
 			eagle.takeOff(hero.getRow(), hero.getColumn(), sword); // launches eagle
 	}
 
 	private void updateEagle() {
 		if (eagle.getState() != Eagle.DEAD && eagle.isWithHero()){
 			eagle.moveWithHero(hero.getRow(), hero.getColumn());
 		}
 		else if(eagle.getState() != Eagle.DEAD) {
 			eagle.moveEagle();
 			if(eagle.isOnGroundWithSword()) {
 				EagleEvent gs = new EagleEvent("gotSword");
 				events.add(gs);
 			}
 			if(eagle.isWaitingForHero()) {
 				EagleEvent wh = new EagleEvent("isWaiting");
 				events.add(wh);
 			}
 
 			if(eagle.isWaitingForHero() && isOnEagle(hero.getRow(), hero.getColumn())) {
 				eagle.returnToHero();
 				sword.takeSword();
 				hero.armHero(hero.getRow(), hero.getColumn() );
 				EagleEvent er = new EagleEvent("eagleReturned");
 				events.add(er);
 			}
 
 			if((eagle.isOnGroundWithSword() || eagle.isWaitingForHero()))
 				checkEagleEncounters();
 
 		}
 	}
 
 	private void checkHeroState() { //Checks if hero died or exited the maze, creating the necessary events
 		switch(hero.getState()) {
 		case Hero.EXITED_MAZE:
 			ResultEvent won = new ResultEvent(2);
 			events.add(won);
 			break;
 		case Hero.DEAD:
 			ResultEvent lost = new ResultEvent(1);
 			events.add(lost); 
 			break;
 		}
 	}
 
 	private void checkEnemyState() { //Checks if there are any enemies left
 		if(remaining_dragons == 0) {
 			exit_state = OPEN;
 			ResultEvent exitOpen = new ResultEvent(0);
 			events.add(exitOpen);
 		}
 	}
 
 	/*** Public Methods ***/
 
 	//Main
 	public static void main(String[] args) {
 		CLInterface cli = new CLInterface();
 		cli.startGame();
 	}
 
 	//Constructors
 	public Game(GameOptions options) {
 		int rows = options.rows, columns = options.columns;
 
 		boolean isRandom = options.randomMaze; //Will indicate if user wants to give a specific size for the maze
 
 		dragonType = options.dragonType;
 
 		//Multiple dragon options
 		if(options.multipleDragons)
 			number_of_dragons = (rows + columns) / 10;
 		else
 			number_of_dragons = 1;
 
 		remaining_dragons = number_of_dragons;
 
 		// A maze director is in charge of selecting a
 		// building pattern and to order its construction
 		MazeDirector director = new MazeDirector();
 
 		if(isRandom) {
 			if(rows < 5 || columns < 5) {
 				GameOutput.printMazeSizeError();
 				MazeBuilder predefined = new PredefinedMaze();
 				director.setMazeBuilder(predefined);
 			} else {
 				MazeBuilder randomMaze = new RandomMaze();
 				director.setMazeBuilder(randomMaze);
 			}
 		} else {
 			MazeBuilder predefined = new PredefinedMaze();
 			director.setMazeBuilder(predefined);
 		}
 
 		director.constructMaze(rows, columns);
 		maze = director.getMaze();
 
 		if(options.randomSpawns) {
 			hero = spawnHero();
 			dragons = spawnDragons();
 			sword = spawnSword();
 			eagle = spawnEagle(hero.getRow(), hero.getColumn());
 		}
 		else {
 			hero = new Hero(options.heroRow, options.heroColumn);
 			dragons = options.dragons;
 			number_of_dragons = dragons.size();
 			remaining_dragons = number_of_dragons;
 			sword = new Sword(options.swordRow, options.swordColumn);
 			eagle = spawnEagle(hero.getRow(), hero.getColumn());
 		}
 	}
 
 	//General Methods
 	public int getDragonState(int i) {
 		return dragons.get(i).getState();
 	}
 
 	public int getExitState() {
 		return exit_state;
 	}
 
 	public Maze getMaze() {
 		return maze;
 	}
 
 	public Hero getHero() {
 		return hero;
 	}
 
 	public Dragon getDragon(int i) {
 		return dragons.get(i);
 	}
 
 	public ArrayList<Dragon> getDragons() {
 		return dragons;
 	}
 
 	public int getNumberOfDragons() {
 		return number_of_dragons;
 	}
 
 	public Sword getSword() {
 		return sword;
 	}
 
 	public void addEvent(GameEvent ev) {
 		events.add(ev);
 	}
 
 	public LinkedList<GameEvent> getEvents() {
 		return events;
 	}
 
 	public Eagle getEagle(){
 		return eagle;
 	}
 
 	//Game Methods
 
 	public boolean checkIfSword(int row, int column) { //Checks if an untaken sword is in that place
 		return(row == sword.getRow() && column == sword.getColumn() && !sword.isTaken());
 	}
 
 	public boolean checkIfEagle(int row, int column) {
 		return(row == eagle.getRow() && column == eagle.getColumn() && !eagle.isWithHero() );
 	}
 
 	public boolean checkIfDragon(int row, int column) {
 
 		for(int i = 0; i < dragons.size(); i++)
 			if(row == dragons.get(i).getRow() && column == dragons.get(i).getColumn() && (dragons.get(i).getState() == Dragon.ALIVE || dragons.get(i).getState() == Dragon.ASLEEP))
 				return true;
 
 		return false;
 	}
 
 	public boolean fightDragon(Dragon dragon) { //True if the hero killed the dragon (was carrying sword), false if the hero died
 		if(hero.getState() == Hero.ARMED) {
 			dragon.setState(Dragon.DEAD);
 			remaining_dragons--;
 			return true;
 		}
 		else if(hero.getState() == Hero.IN_GAME && dragon.getState() == Dragon.ALIVE) {
 			hero.setState(Hero.DEAD);
 			return false;
 		}
 
 		return false;
 	}
 
 	public boolean nextToDragons(int row, int column) { //True if the hero is adjacent to the dragon (horizontally, vertically or on top), false if not
 
 		if(dragons == null || dragons.isEmpty())
 			return false;
 
 		for(int i = 0; i < dragons.size(); i++) {
 			if(((dragons.get(i).getRow() == row + 1 && dragons.get(i).getColumn() == column) ||
 					(dragons.get(i).getRow() == row - 1  && dragons.get(i).getColumn() == column) ||
 					(dragons.get(i).getColumn() == column + 1 && dragons.get(i).getRow() == row) ||
 					(dragons.get(i).getColumn() == column - 1 && dragons.get(i).getRow() == row) ||
 					(dragons.get(i).getColumn() == column && dragons.get(i).getRow() == row))
 					&& (dragons.get(i).getState() == Dragon.ALIVE || dragons.get(i).getState() == Dragon.ASLEEP))	
 				return true;
 		}
 
 		return false;
 	}
 
 	public boolean nextToOneDragon(int row, int column, Dragon dragon) {
 		return(((dragon.getRow() == row + 1 && dragon.getColumn() == column) ||
 				(dragon.getRow() == row - 1  && dragon.getColumn() == column) ||
 				(dragon.getColumn() == column + 1 && dragon.getRow() == row) ||
 				(dragon.getColumn() == column - 1 && dragon.getRow() == row) ||
 				(dragon.getColumn() == column && dragon.getRow() == row))
 				&& (dragon.getState() == Dragon.ALIVE || dragon.getState() == Dragon.ASLEEP));
 	}
 
 	public boolean isOnDragon(int row, int column) {
 
 		if(dragons == null || dragons.isEmpty())
 			return false;
 
 		for(int i = 0; i < dragons.size(); i++) {
 			if((dragons.get(i).getColumn() == column && dragons.get(i).getRow() == row)
 					&& (dragons.get(i).getState() == Dragon.ALIVE || dragons.get(i).getState() == Dragon.ASLEEP))
 				return true;
 		}
 
 		return false;
 	}
 
 	public boolean heroTurn(char input) { //Main game loop
 		boolean goOn = true;
 
 		switch (input) {
 		case 's':
 			goOn = hero.moveHero(1, 0, this); //tries to move hero the number or rows or columns given
 			break;
 		case 'w':
 			goOn = hero.moveHero(-1, 0, this);
 			break;
 		case 'a':
 			goOn = hero.moveHero(0, -1, this);
 			break;
 		case 'd':
 			goOn = hero.moveHero(0, 1, this);
 			break;
 		case ' ':
 			goOn = hero.moveHero(0,  0,  this);
 			break;
 		case 'e':
 			tryToSendEagle();
 			break;
 		case 'z': //z shuts down game
 			goOn = false;
 			break;
 		default:
 			break;
 		}
 
 		updateEagle();
 
 		return goOn;
 	}
 
 	public boolean dragonTurn(boolean goOn) {
 
 		goOn = checkDragonEncounters(goOn);
 		goOn = moveDragons(goOn);
 
 		return goOn;
 
 	}
 	
	public boolean checkState(boolean goOn) {
		goOn = checkDragonEncounters(goOn);
 		checkEnemyState();
 		checkHeroState();
		return goOn;
 	}
 
 
 }
