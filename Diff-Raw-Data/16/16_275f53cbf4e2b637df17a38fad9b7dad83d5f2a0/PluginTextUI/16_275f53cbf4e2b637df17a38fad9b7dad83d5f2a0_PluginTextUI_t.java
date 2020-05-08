 package de.htwg.seapal.common.views.tui;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import de.htwg.seapal.common.plugin.Plugin;
import de.htwg.seapal.common.observer.IObserver;
import de.htwg.seapal.common.observer.Event;
import de.htwg.seapal.common.command.ICommand;
 
public abstract class PluginTextUI implements Plugin, IObserver {
 
 	/**
 	 * The scanner for reading input.
 	 */
 	@SuppressWarnings("resource")
 	private final static Scanner INPUT = new Scanner(System.in);
 	
 	/**
 	 * The menu key.
 	 */
 	private char menuKey;
 	/**
 	 * The menu entry title.
 	 */
 	private String menuEntry;
 	
 	/**
 	 * The commands of the plugin.
 	 */
 	protected List<ICommand> commands = new ArrayList<ICommand>();
 	
 	/**
 	 * Initializes the base text UI of the plugin.
 	 * @param menuKey The menu key.
 	 * @param menuEntry The menu entry title.
 	 */
 	protected PluginTextUI(char menuKey, String menuEntry) {
 		this.menuKey = menuKey;
 		this.menuEntry = menuEntry;
 		this.initializeCommands();
 	}
 	
 	/**
 	 * Initializes the commands of the plugins text UI.
 	 */
 	protected abstract void initializeCommands();
 	
 	/**
 	 * Adds a new command.
 	 * @param command The command to add.
 	 * @throws IllegalArgumentException When the commands key is already defined.
 	 * Example:
 	 * addCommand(new RelayCommand('a', "Set title") {
 	 *		@Override
 	 *		public void execute(String param) {
 	 *			controller.setTitle(id, param);
 	 *		}
 	 *	});
 	 * </p>
 	 */
 	protected void addCommand(ICommand command) throws IllegalArgumentException {
 		if (commands.contains(command))
 		{
 			throw new IllegalArgumentException("The key is already defined!");
 		}
 		
 		commands.add(command);
 	}
 	
 	/**
 	 * Gets the menu entry title.
 	 */
 	@Override
 	public String getMenuEntry() {
 		return this.menuEntry;
 	}
 
 	/**
 	 * Gets the menu entry key.
 	 */
 	@Override
 	public char getMenuKey() {
 		return this.menuKey;
 	}
 
 	/**
 	 * Prints the text UI.
 	 */
 	@Override
 	public void printTUI() {
 		StringBuilder sb = new StringBuilder();
 
 		sb.append(this.menuEntry).append(":\n");
 		
 		for(ICommand command : commands) {
 			sb.append(command.getKey()).append(" - ").append(command.getTitle()).append("\n");
 		}
 		
 		sb.append("\n");
 		sb.append("Command: ");
 
 		System.out.print(sb.toString());
 	}
 
 	/**
 	 * Processes the input line.
 	 */
 	@Override
 	public boolean processInputLine(String line) {
 		char cmd = line.charAt(0);
 		
 		if (cmd == 'q') {
 			return false;
 		}
 		
 		String param = INPUT.next();
 		boolean executed = false;
 		
 		for (ICommand command : commands) {
 			if (command.getKey() == cmd) {
 				command.execute(param);
 				executed = true;
 				break;
 			}
 		}
 		
 		if (!executed) {
 			System.out.println("Unknown Command!");
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Updates the text UI.
 	 */
 	@Override
	public void update(Event event) {
 		printTUI();
 	}
 
 }
