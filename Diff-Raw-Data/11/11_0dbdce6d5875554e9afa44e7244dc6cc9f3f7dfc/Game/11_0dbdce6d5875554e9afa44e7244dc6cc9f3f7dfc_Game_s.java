 package game.logic;
 
 import game.ui.FightEvent;
 import game.ui.GameEvent;
 import game.ui.GameInput;
 import game.ui.GameOutput;
 import game.ui.ResultEvent;
 import general_utilities.MazeInput;
 
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
 
 	public static final int OPEN = 1;
 	public static final int CLOSED = 0;
 
 	/*** Private Attributes ***/
 
 	//State Attributes
 	private int game_state;
 	private int exit_state;
 
 	//Game elements
 	private Maze maze;
 	private Sword sword;
 	private Hero hero;
 	private Dragon dragon;
 	private LinkedList<GameEvent> events = new LinkedList<GameEvent>();
 
 	/*** Private Methods ***/
 
 	//Game Initializers
 	private void spawnHero() { //Creates a hero object on a random valid position in the maze
 		Random random = new Random();
 		int hero_row = 0;
 		int hero_column = 0;
 
 		do {
 			hero_row = random.nextInt(maze.getRows());
 			hero_column = random.nextInt(maze.getColumns());
 		} while (!maze.checkIfEmpty(hero_row, hero_column));
 
 		hero = new Hero(hero_row, hero_column);
 	}
 
 	private void spawnSword() { //Creates a sword object on a random valid position in the maze
 		Random random = new Random();
 		int sword_row = 0;
 		int sword_column = 0;
 
 		do {
 			sword_row = random.nextInt(maze.getRows());
 			sword_column = random.nextInt(maze.getColumns());
 		} while (!maze.checkIfEmpty(sword_row, sword_column) || nextToHero(sword_row, sword_column));
 
 		sword = new Sword(sword_row, sword_column);
 	}
 
 	private void spawnDragon() { //Creates a dragon object on a random valid position in the maze
 		Random random = new Random();
 		int dragon_row = 0;
 		int dragon_column = 0;
 
 		do {
 			dragon_row = random.nextInt(maze.getRows());
 			dragon_column = random.nextInt(maze.getColumns());
 		} while (!maze.checkIfEmpty(dragon_row, dragon_column) || nextToHero(dragon_row, dragon_column));
 
 		dragon = new Dragon(dragon_row, dragon_column);
 	}
 
 	private boolean nextToDragon() { //True if the hero is adjacent to the dragon (horizontally, vertically or on top), false if not
 		return(((dragon.getRow() == hero.getRow() + 1 && dragon.getColumn() == hero.getColumn()) ||
 				(dragon.getRow() == hero.getRow() - 1  && dragon.getColumn() == hero.getColumn()) ||
 				(dragon.getColumn() == hero.getColumn() + 1 && dragon.getRow() == hero.getRow()) ||
 				(dragon.getColumn() == hero.getColumn() - 1 && dragon.getRow() == hero.getRow()) ||
 				(dragon.getColumn() == hero.getColumn() && dragon.getRow() == hero.getRow()))
 				&& (dragon.getState() == Dragon.ALIVE));
 	}
 
 	private boolean nextToHero(int row, int column) { //True if the object is adjacent to the hero (horizontally, vertically or on top), false if not
 		return((row == hero.getRow() + 1 && column == hero.getColumn()) ||
 				(row == hero.getRow() - 1  && column == hero.getColumn()) ||
 				(column == hero.getColumn() + 1 && row == hero.getRow()) ||
 				(column == hero.getColumn() - 1 && row == hero.getRow()));
 	}
 
 	/*** Public Methods ***/
 
 	//Main
 	public static void main(String[] args) {
 		Game game = new Game();
 		//GameOutput.printMaze(game.getMaze());
 		GameOutput.printGame(game);
 		game.play();
 	}
 
 	//Constructors
 	public Game() {
 		game_state = 0;
 		int rows = 0, columns = 0;
 		int size[] = {rows, columns};
 		boolean giveSize = false; //Will indicate if user wants to give a specific size for the maze
 		GameOutput.printStartMessage();
 		giveSize = GameInput.receiveMazeOptions(size);
 		rows = size[0];
 		columns = size[1];
 		
 		// A maze director is in charge of selecting a
 		// building pattern and to order its construction
 		MazeDirector director = new MazeDirector();
 		
 		if(giveSize) {
			if(rows <= 5 || columns <= 5) {
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
 
 		spawnHero();
 		spawnSword();
 		spawnDragon();
 
 	}
 
 	//General Methods
 	public int getDragonState() {
 		return dragon.getState();
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
 
 	public Dragon getDragon() {
 		return dragon;
 	}
 
 	public Sword getSword() {
 		return sword;
 	}
 
 	public void addEvent(GameEvent ev) {
 		events.add(ev);
 	}
 
 	//Game Methods
 	
 	public boolean checkIfSword(int row, int column) { //Checks if a sword is in that place
 		return(row == sword.getRow() && column == sword.getColumn() && !sword.getTaken());
 	}
 	
 	public boolean checkIfDragon(int row, int column) {
 		return(row == dragon.getRow() && column == dragon.getColumn() && dragon.getState() == Dragon.ALIVE);
 	}
 	
 	public boolean fightDragon() { //True if the hero killed the dragon (was carrying sword), false if the hero died
 		if(hero.getState() == Hero.ARMED) {
 			dragon.setState(Dragon.DEAD);
 			exit_state = OPEN;
 			return true;
 		}
 		else if(hero.getState() == Hero.IN_GAME) {
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
 
 			if (nextToDragon()) {
 				if(fightDragon()) {
 					FightEvent wonFight = new FightEvent("wonFight");
 					events.add(wonFight);
 				}
 				else {
 					goOn = false;
 					FightEvent lostFight = new FightEvent("lostFight");
 					events.add(lostFight);
 				}
 			}
 
 			if(game_state == 1 && (dragon.getState() == Dragon.ALIVE)) {
 				dragon.moveDragon(this);
 
 				if (nextToDragon()) {
 					if(fightDragon()) {
 						FightEvent wonFight = new FightEvent("wonFight");
 						events.add(wonFight);
 					}
 					else {
 						goOn = false;
 						FightEvent lostFight = new FightEvent("lostFight");
 						events.add(lostFight);
 					}
 				}
 			}
 			else
 				game_state = 1;
 
 			GameOutput.printGame(this);
 
 			switch(hero.getState()) {
 			case Hero.EXITED_MAZE:
 				ResultEvent won = new ResultEvent(1);
 				events.add(won);
 				break;
 			case Hero.DEAD:
 				ResultEvent lost = new ResultEvent(0);
 				events.add(lost); 
 				break;
 			}
 
 			GameOutput.printEventQueue(events);
 
 		}
 
 		return true;
 	}
 
 
 
 }
