 package cz.cvut.fit.dpo.adventure.view;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 
 import cz.cvut.fit.dpo.adventure.controller.IGameController;
 import cz.cvut.fit.dpo.adventure.model.IGameObject;
 import cz.cvut.fit.dpo.adventure.model.ILocation;
 import cz.cvut.fit.dpo.adventure.model.facade.GameModelFacade;
 import cz.cvut.fit.dpo.adventure.model.observer.IGameEvent;
 
 public class ConsoleView implements GameView {
 
 	private GameModelFacade model;
 	private IGameController controller;
 	
 	private BufferedReader reader;
 	private PrintStream out;
 	private boolean exit;
 
 	public ConsoleView(GameModelFacade model, IGameController controller) {
 		this.model = model;
 		this.controller = controller;
 		this.out = System.out;
 		this.reader = new BufferedReader(new InputStreamReader(System.in));
 		this.exit = false;
 	}
 
 	public void run() throws IOException {
 		printCurrentState();
 		String command = null;
 		while (! exit) {
 			command = reader.readLine();
 			parseCommand(command);
 		}
 		out.println("Bye!");
 		reader.close();
 	}
 	
 	@Override
 	public void gameEventOccured(IGameEvent event) {
 		out.println(event.description());
 		out.println();
 		if (event.shouldUpdateState()) {
 			printCurrentState();
 		}
 	}
 
 	@Override
 	public void parseCommand(String command) {
 		String[] words = command.split(" ");
 
 		if (CommandParser.isNullaryCommand(command)) {
 			// check argument count
 			String action = words[0];
 			if (action.startsWith(CommandParser.HELP)) {
				out.println("Commands: examine <item>, take <item>, drop <item>, use <item1> <item2>, help, status, quit.");
 			} else if (action.startsWith(CommandParser.QUIT)) {
 				controller.exitGame();
 			} else if (action.startsWith(CommandParser.LOOK_AROUND)) {
 				controller.lookAround();
 			}
 		} else if (CommandParser.isUnaryCommand(command)) {
 			// check argument count
 			String action = words[0];
 			String param = words[1];
 			if (action.startsWith(CommandParser.EXAMINE)) {
 				controller.examine(param);
 			} else if (action.startsWith(CommandParser.EXIT_TO)) {
 				controller.exitTo(param);
 			} else if (action.startsWith(CommandParser.PICK_UP)) {
 				controller.pickUp(param);
 			} else if (action.startsWith(CommandParser.DROP)) {
 				controller.drop(param);
 			}
 		} else if (CommandParser.isBinaryCommand(command)) {
 			// check argument count
 			String action = words[0];
 			String param1 = words[1];
 			String param2 = words[2];
 			if (action.startsWith(CommandParser.USE_ON)) {
 				controller.useOn(param1, param2);
 			}
 		} else {
 			out.println("Uknown command.");
 		}
 	}
 	
 	void printCurrentState() {
 		printLocation();
 		printObjects();
 		printExits();
 		printInventory();
 	}
 	
 	void printLocation() {
 		out.println(model.currentLocation().describe());
 		out.println();
 	}
 	
 	void printObjects() {
 		if (! model.currentLocation().objectsHere().isEmpty()) {
 			out.print("You see ");
 		}
 		for (IGameObject object : model.currentLocation().objectsHere()) {
 			out.print(object.name());
 			out.print(", ");
 		}
 		out.println();
 	}
 	
 	void printExits() {
 		out.print("From here, you can go to: ");
 		for (ILocation exit : model.currentLocation().exits()) {
 			out.print(exit.name());
 			out.print(", ");
 		}
 		out.println();
 	}
 	
 	void printInventory() {
 		if (model.itemsCarried().isEmpty()) {
 			out.println("You have no items.");
 		} else {
 			out.println("You have:");
 			for (IGameObject item : model.itemsCarried()) {
 				out.println(item.name());
 			}
 		}
 		out.println();
 	}
 
 	@Override
 	public void exitGame() {
 		exit = true;
 	}
 	
 }
