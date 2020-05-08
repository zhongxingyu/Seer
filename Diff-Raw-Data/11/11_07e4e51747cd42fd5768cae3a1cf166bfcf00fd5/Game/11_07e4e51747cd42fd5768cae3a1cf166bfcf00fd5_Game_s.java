 package game.logic;
 
 import game.ui.FightEvent;
 import game.ui.GameEvent;
 import game.ui.GameInput;
 import game.ui.GameOutput;
 import game.ui.ResultEvent;
 import general_utilities.MazeInput;
 
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
 
 public class Game {
 
 	//Exit states
 	public static final int OPEN = 1;
 	public static final int CLOSED = 0;
 
 	/*** Private Attributes ***/
 
 	//State Attributes
 	private int dragon_type;
 	//private int game_state; //Variable to prevent dragons from moving at the first turn
 	private int exit_state;
 	private int number_of_dragons;
 	private int remaining_dragons;
 
 	//Game elements
 	private Maze maze;
 	private Sword sword;
 	private Hero hero;
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
 
 		Dragon dragon = new Dragon(dragon_row, dragon_column, dragon_type);
 		return dragon;
 	}
 
 	private ArrayList<Dragon> spawnDragons() {
 		ArrayList<Dragon> ds = new ArrayList<Dragon>();
 		for(int i = 0; i < number_of_dragons; i++)
 			ds.add(spawnDragon());
 
 		return ds;
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
 
 	private boolean nextToHero(int row, int column) { //True if the object is adjacent to the hero (horizontally, vertically or on top), false if not
 		return((row == hero.getRow() + 1 && column == hero.getColumn()) ||
 				(row == hero.getRow() - 1  && column == hero.getColumn()) ||
 				(column == hero.getColumn() + 1 && row == hero.getRow()) ||
 				(column == hero.getColumn() - 1 && row == hero.getRow()) ||
 				(column == hero.getColumn() && row == hero.getRow()));
 	}
 
 	private boolean checkDragonEncounters(boolean goOn) { //Processes an encounter between the hero and a dragon
 
 		for(int i = 0; i < dragons.size(); i++)
 			if (hero.getState() != Hero.DEAD && nextToOneDragon(hero.getRow(), hero.getColumn(), dragons.get(i)) && !(hero.getState() != Hero.ARMED && dragons.get(i).getState() == Dragon.ASLEEP)) {
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
 
 	private boolean moveDragons(boolean goOn) { //Executes the dragons' turn
 		for(int i = 0; i < dragons.size(); i++)
 			if(dragons.get(i).getType() != Dragon.STATIC && (dragons.get(i).getState() == Dragon.ALIVE || dragons.get(i).getState() == Dragon.ASLEEP)) {
 				dragons.get(i).moveDragon(this);
 
 				goOn = checkDragonEncounters(goOn);
 			}
 		return goOn;
 	}
 
 	/*** Public Methods ***/
 
 	//Main
 	public static void main(String[] args) {
 		Game game = new Game();
 		GameOutput.printGame(game);
 		game.play();
 	}
 
 	//Constructors
 	public Game() {
 		//game_state = 0;
 		int rows = 0, columns = 0;
 		int size[] = {rows, columns};
 		boolean giveSize = false; //Will indicate if user wants to give a specific size for the maze
 
 		//Get Maze options from user
 		GameOutput.printStartMessage();
 		giveSize = GameInput.receiveMazeOptions(size);
 
 		rows = size[0];
 		columns = size[1];
 
 		//Get Dragon options from user
 		dragon_type = GameInput.receiveDragonOptions();
 
 		// A maze director is in charge of selecting a
 		// building pattern and to order its construction
 		MazeDirector director = new MazeDirector();
 
 		if(giveSize) {
			if(rows < 5 || columns < 5 || rows > columns) { //Currently, the maze only accepts square mazes or with with more columns than rows
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
 
 		//Multiple dragon options
 		if(GameInput.receiveMultipleDragonOptions())
 			number_of_dragons = (maze.getRows() + maze.getColumns()) / 10;
 		else
 			number_of_dragons = 1;
 		
 		remaining_dragons = number_of_dragons;
 
 		hero = spawnHero();
 		dragons = spawnDragons();
 		sword = spawnSword();
 
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
 
 	//Game Methods
 
 	public boolean checkIfSword(int row, int column) { //Checks if an untaken sword is in that place
 		return(row == sword.getRow() && column == sword.getColumn() && !sword.getTaken());
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
 
 	public boolean play() { //Main game loop
 		boolean goOn = true;
 
 		char input;
 
 		while(goOn) {
 
 			try {
 				GameOutput.printAskForMove();
 				input = MazeInput.getChar();
 				if(input == 's')
 					goOn = hero.moveHero(1, 0, this); //tries to move hero the number or rows or columns given
 				else if(input == 'w')
 					goOn = hero.moveHero(-1, 0, this);
 				else if(input == 'a')
 					goOn = hero.moveHero(0, -1, this);
 				else if(input == 'd')
 					goOn = hero.moveHero(0, 1, this);
 				else if(input == 'z') //z shuts down game
 					goOn = false;
 			}
 
 			catch(Exception e) {
 				System.err.println("Problem reading user input!");
 			}
 
 			goOn = checkDragonEncounters(goOn);
 
 			goOn = moveDragons(goOn);
 
 			GameOutput.printGame(this);
 			
 			if(remaining_dragons == 0) {
 				exit_state = OPEN;
 				ResultEvent exitOpen = new ResultEvent(0);
 				events.add(exitOpen);
 			}
 
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
 
 			GameOutput.printEventQueue(events);
 
 		}
 
 		return true;
 	}
 
 }
