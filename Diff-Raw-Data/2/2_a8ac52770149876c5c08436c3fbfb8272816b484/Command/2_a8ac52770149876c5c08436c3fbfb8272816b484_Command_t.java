 package net.marcuswhybrow.minecraft.law.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 import net.marcuswhybrow.minecraft.law.Law;
 import net.marcuswhybrow.minecraft.law.commands.elements.ArgumentOptional;
 import net.marcuswhybrow.minecraft.law.commands.elements.ArgumentRequired;
 import net.marcuswhybrow.minecraft.law.commands.elements.Element;
 import net.marcuswhybrow.minecraft.law.commands.elements.Text;
 import net.marcuswhybrow.minecraft.law.exceptions.IllegalCommandDefinitionException;
 import net.marcuswhybrow.minecraft.law.utilities.Colorise;
 import net.marcuswhybrow.minecraft.law.utilities.MessageDispatcher;
 
 public abstract class Command {
 	public enum Type {IN_GAME_ONLY, CONSOLE_ONLY, NORMAL};
 	public static final String BASE_PERMISSION_NODE = "law.commands";
 	public static final String PERMISSION_NODE = "";
 	
 	private List<Element> elements;
 	private int minArgs;
 	private int maxArgs;
 	private boolean hasArgumentElement;
 	private boolean hasOptionalElement;
 	private StringBuilder name;
 	private StringBuilder signature;
 	private Type type;
 	private String permissionNode;
 	
 	protected Law law;
 	
 	public Command() {
 		elements = new ArrayList<Element>();
 		law = Law.get();
 		minArgs = 0;
 		maxArgs = 0;
 		hasOptionalElement = false;
 		hasArgumentElement = false;
 		name = null;
 		signature = null;
 		type = Type.NORMAL;
 		permissionNode = null;
 	}
 	
 	/**
 	 * Constructs a Command instance with Element's derived from the
 	 * supplied definitionList string.
 	 * 
 	 * @param definitionList A space separated list of elements, all of which
 	 * must match one concrete Element's doesTextMatch method
 	 * @throws IllegalCommandDefinitionException if a the definitionList cannot be parsed
 	 */
 	public Command(String definitionList) throws IllegalCommandDefinitionException {
 		this();
 		
 		String[] definitions = definitionList.split(" ");
 		for (String definition : definitions) {
 			Element element = Element.createElementFromDefinition(definition);
 			if (element == null) {
 				throw new IllegalCommandDefinitionException("Could not parse element: \"" + definition + "\" in definition \"" + definitionList + "\"");
 			} else {
 				// Will throw an IllegalCommandDefinitionException if the element cannot be added
 				this.appendElement(element);
 			}
 		}
 	}
 	
 	/**
 	 * This method will be called in order to perform the purpose of the command
 	 * and calls either the onSuccess or onFailure methods afterwards.
 	 * 
 	 * @param sender The instigator of this command
 	 * @param args The arguments entered in the console after the forward slash
 	 */
 	public void execute(CommandSender sender, String[] args) {
 		
 		if (sender.hasPermission(this.getPermissioNode()) == false) {
 			MessageDispatcher.consoleInfo(sender.getName() + " tried to use the command \"" + this.getName() + "\" but did not have permission.");
 			return;
 		}
 		
 		switch (this.type) {
 		case CONSOLE_ONLY:
 			if (sender instanceof ConsoleCommandSender == false) {
 				MessageDispatcher.sendMessage(sender, Colorise.info("The ") + Colorise.command("/" + this.getName()) + Colorise.info(" command may only be used in the console, and not in-game."));
 				return;
 			}
 			break;
 		case IN_GAME_ONLY:
 			if (sender instanceof Player == false) {
 				MessageDispatcher.sendMessage(sender, "The \"" + this.getName() + "\" command may only be used in-game, and not from the console.");
 				return;
 			}
 			break;
 		}
 		
 		if (isValid(args) == false) {
 			MessageDispatcher.sendMessage(sender, Colorise.error("Usage: /" + this.getUsagePattern()));
 			return;
 		}
 		
 		int reason = onExecute(sender, args);
 		if (reason == 0) {
 			onSuccess();
 		} else {
 			onFailure(reason);
 		}
 	}
 	
 	/**
 	 * Must be overridden by concrete Command classes to define the logic of the
 	 * command
 	 *  
 	 * @param sender The instigator of this command
 	 * @param args The arguments entered in the console after the forward slash
 	 * @return Returns an integer representing the status of the command 
 	 */
 	public abstract int onExecute(CommandSender sender, String[] args);
 	
 	/**
 	 * Called when the command has complete successfully. Override to provide a
 	 * success message, which is visual confirmation for the user.
 	 */
 	public abstract void onSuccess();
 	
 	/**
 	 * Called when the execution of the command failed. Override to define failure
 	 * messages and logic.
 	 * 
 	 * @param reason An integer representing the reason for failure
 	 */
 	public abstract void onFailure(int reason);
 	
 	/**
 	 * Sets the type of this command
 	 * 
 	 * @param type The type this command should be set as
 	 */
 	public void setType(Type type) {
 		this.type = type;
 	}
 	
 	/**
 	 * Appends an element to this command.
 	 * 
 	 * @param element The element to append
 	 * @throws IllegalCommandDefinitionException If the element cannot be added to the command
 	 */
 	public void appendElement(Element element) throws IllegalCommandDefinitionException {
 		if (hasArgumentElement && element instanceof Text) {
 			throw new IllegalCommandDefinitionException("Definition has this non-argument element after an argument: " + element);
 		}
 		
 		// Prevents non-"optional argument" elements being added
 		// after an "optional argument".
 		if (element.isOptional()) {
 			// adding an optional element
 			hasOptionalElement = true;
 		} else if (hasOptionalElement) {
 			// adding a non-optional element after an optional one
 			throw new IllegalCommandDefinitionException("Definition has this non-optional element after an optional one: " + element);
 		}
 		
 		// adding a non-optional element without any pre-existing optional elements
 		// or adding an optional element
 		elements.add(element);
 		
 		// Update the argument limit 
 		maxArgs = elements.size();
 		if (element.isOptional() == false) {
 			minArgs = maxArgs;
 		}
 		
 		if (element instanceof Text) {			
 			if (name == null) {
 				name = new StringBuilder(element.getUsageText());
 			} else {
 				name.append(" ").append(element.getUsageText());
 			}
 		}
 		
 		if (signature == null) {
 			signature = new StringBuilder(element.getUsageText());
 		} else {
 			signature.append(" ").append(element.getUsageText());
 		}
 	}
 	
 	/**
 	 * Determines if the given arguments could be used for this command.
 	 * 
 	 * @param args The list of arguments
 	 * @return True if the arguments can be used with this command
 	 */
 	public boolean matches(String[] args) {
 		for (int i = 0; i < elements.size(); i++) {
 			Element element = elements.get(i);
 			if (element instanceof Text) {
 				if (!hasArgAtPosition(args, i) || element.getUsageText() != args[i]) {
 					// Fail if it does not match the text
 					return false;
 				}
 			} else if (element instanceof ArgumentOptional) {
 				// do nothing
 			} else if (element instanceof ArgumentRequired) {
 				if (!hasArgAtPosition(args, i)) {
 					return false;
 				}
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Determines if the provided arguments are usable by this command
 	 * which is dependent on the total number of command elements and
 	 * the number of optional elements
 	 *  
 	 * @param args The input arguments for the command
 	 * @return True if the arguments are valid
 	 */
 	public boolean isValid(String[] args) {
 		return args.length >= minArgs && args.length <= maxArgs; 
 	}
 	
 	/**
 	 * Returns the usage pattern which is necessary to use this command.
 	 * 
 	 * @return The string representing the usage pattern
 	 */
 	public String getUsagePattern() {
 		return signature.toString();
 	}
 	
 	/**
 	 * Gets the name of this command which is all elements of the command
 	 * except the arguments.
 	 * 
 	 * @return The name as a string
 	 */
 	public String getName() {
 		return name.toString();
 	}
 	
 	/**
 	 * Sets the permission node required for a player to use this command
 	 * 
 	 * @param permissionNode The permission node string
 	 */
 	public void setPermissionNode(String permissionNode) {
 		this.permissionNode = BASE_PERMISSION_NODE + "." + permissionNode;
 	}
 	
 	/**
 	 * Gets the permission node required for a player to use this command
 	 * 
 	 * @return The permission node string
 	 */
 	public String getPermissioNode() {
 		return this.permissionNode;
 	}
 	
 	/**
 	 * Checks to see if a command can be run by the command sender and returns
 	 * true if the sender has permission.
 	 * 
 	 * @param sender The sender of the command
 	 * @param commandPermissionNode The command permission node (Excluding the commands base permission)
	 * @return True if the sender has permission to execute the command, and false otherwise
 	 */
 	public static boolean checkHasPermission(CommandSender sender, String permissioNode) {
 		return sender.hasPermission(BASE_PERMISSION_NODE + "." + permissioNode);
 	}
 	
 	/**
 	 * Checks to see if there is an argument at a specified postion,
 	 * 
 	 * @param args The arguments to check against
 	 * @param i The position to look for
 	 * @return True if the position exist, false if not
 	 */
 	private boolean hasArgAtPosition(String[] args, int i) {
 		if (args.length < i + 1) {
 			// Fail if there is not an arguments to check against this element
 			return false;
 		}
 		return true;
 	}
 }
