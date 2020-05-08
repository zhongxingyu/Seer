 package com.caldabeast.commander;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.TabCompleter;
 
 /**
  * @author CalDaBeast
  */
 public abstract class Commander implements CommandExecutor, TabCompleter {
 
 	private final HashMap<String, Subcommander> subcommands = new HashMap<>();
 
 	public Commander() {
 		registerSubcommand(this);
 	}
	
 	/**
 	 * Registers a subcommand to be called by the Commander
 	 *
 	 * @param command an instance of the class containing subcommands
 	 */
 	public final void registerSubcommand(Object command) {
 		for (Method method : command.getClass().getMethods()) {
 			Subcommand anno = method.getAnnotation(Subcommand.class);
 			if (anno == null) continue;
 			Class[] params = method.getParameterTypes();
 			if (params.length != 3
 					|| !params[0].isAssignableFrom(CommandSender.class)
 					|| !params[1].isAssignableFrom(String.class)
 					|| !params[2].isAssignableFrom(String[].class)) {
 				throw new IllegalArgumentException("Subcommands must follow the method signature of "
 						+ "name(CommandSender cs, String label, String[] args)");
 			}
 			if (anno.name().equals("")) {
 				throw new IllegalArgumentException("Must specify a name for the subcommand.");
 			}
 			if (subcommands.containsKey(anno.name())) {
 				throw new IllegalArgumentException("Subcommand names and aliases must be unique. "
 						+ anno.name() + " has already been registered in this Commander.");
 			}
 			Subcommander sub = new Subcommander(command, method, anno.name(), anno.alias(), anno.permission());
 			subcommands.put(anno.name(), sub);
 			if (anno.alias().length == 0) continue;
 			for (String alias : anno.alias()) {
 				if (subcommands.containsKey(alias)) {
 					throw new IllegalArgumentException("Subcommand names and aliases must be unique. "
 							+ anno.name() + " has already been registered in this Commander.");
 				}
 				subcommands.put(alias, sub);
 			}
 		}
 		for (Method method : command.getClass().getMethods()) {
 			TabComplete anno = method.getAnnotation(TabComplete.class);
 			if (anno == null) continue;
 			Class[] params = method.getParameterTypes();
 			if (params.length != 2
 					|| !params[0].isAssignableFrom(CommandSender.class)
 					|| !params[1].isAssignableFrom(String[].class)) {
 				throw new IllegalArgumentException("Tab Completion Methods must follow the method signature of "
 						+ "name(CommandSender cs, String[] args)");
 			}
 			if (anno.name().equals("")) {
 				throw new IllegalArgumentException("Must specify a subcommand for the Tab Completer to attach to.");
 			}
 			for (Entry<String, Subcommander> entry : subcommands.entrySet()) {
				if (anno.name().equalsIgnoreCase(entry.getValue().getName())) {
					if (entry.getValue().getTabMethod() != null) {
						throw new IllegalArgumentException("The subcommand " + entry
								+ " already has an attached Tab Completer.");
					}
					entry.getValue().setTabMethod(method);
					break;
 				}
 			}
 		}
 	}
 
 	@Override
 	public final boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
 		if (args.length == 0) {
 			ifNotSubcommand(cs, "", args);
 			return true;
 		}
 		String command = args[0];
 		if (!subcommands.containsKey(command)) {
 			ifNotSubcommand(cs, command, args);
 			return true;
 		}
 		Subcommander subcommand = subcommands.get(command);
 		String[] subArgs = new String[args.length - 1];
 		for (int i = 1; i < args.length; i++) {
 			subArgs[i - 1] = args[i];
 		}
 		if (!checkPermission(cs, subcommand.getPermission())) {
 			ifNoPermission(cs, command, subArgs);
 			return true;
 		}
 		Method method = subcommand.getMethod();
 		try {
 			method.invoke(subcommand.getObject(), cs, command, subArgs);
 		} catch (IllegalAccessException | InvocationTargetException ex) {
 			ifNotSubcommand(cs, command, args);
 			throw new IllegalArgumentException("Could not invoke the '" + command + "' subcommand's method."
 					+ "Perhaps its method signature is incorrect? Must be name(CommandSender cs, String label, String[] args)");
 		}
 		return true;
 	}
 
 	@Override
 	public final List<String> onTabComplete(CommandSender cs, Command cmnd, String label, String[] args) {
 		if (args.length == 0 || args.length == 1) {
 			List<String> completions = getCompletionList(cs);
 			if (args.length == 0) return completions;
 			else return getPossibleFromList(completions, args[0]);
 		}
 		String command = args[0];
 		ArrayList<String> empty = new ArrayList<>();
 		if (!subcommands.containsKey(command)) {
 			ifNotSubcommand(cs, command, args);
 			return empty;
 		}
 		Subcommander subcommand = subcommands.get(command);
 		String[] subArgs = new String[args.length - 1];
 		for (int i = 1; i < args.length; i++) {
 			subArgs[i - 1] = args[i];
 		}
 		Method method = subcommand.getTabMethod();
 		try {
 			Object returned = method.invoke(subcommand.getObject(), cs, subArgs);
 			return getPossibleFromList((List<String>) returned, args[args.length - 1]);
 		} catch (NullPointerException | ClassCastException | IllegalAccessException | InvocationTargetException ex) {
 			return empty;
 		}
 	}
 
 	private List<String> getCompletionList(CommandSender cs) {
 		ArrayList<String> tabComplete = new ArrayList<>();
 		for (String subcommand : subcommands.keySet()) {
 			Subcommander sub = subcommands.get(subcommand);
 			if (sub.getName().equals(subcommand) && checkPermission(cs, sub.getPermission())) {
 				tabComplete.add(subcommand);
 			}
 		}
 		return tabComplete;
 	}
 
 	/**
 	 * For use with Tab Completion.
 	 * Automatically calculated based on the Subcommand's list.
 	 *
 	 * @param all All of the possible Tab Completions
 	 * @param current The current input of the user
 	 * @return All possible completions based on the current user's input
 	 */
 	public final List<String> getPossibleFromList(List<String> all, String current) {
 		ArrayList<String> possible = new ArrayList<>();
 		for (String completion : all) {
 			if (completion.startsWith(current)) possible.add(completion);
 		}
 		return possible;
 	}
 
 	private boolean checkPermission(CommandSender cs, String permission) {
 		if (permission.equals("")) return true;
 		return senderHasPermission(cs, permission);
 	}
 
 	/**
 	 * Checks if the Command Sender has permission to use the subcommand.
 	 * Can be overriden to make the permissions system more advanced.
 	 * This implementation will always return true if the Command Sender is a server operator.
 	 *
 	 * @param cs the Command Sender
 	 * @param permission the permission required by the subcommand
 	 * @return whether or not the Command Sender has permission to use the subcommand
 	 */
 	public boolean senderHasPermission(CommandSender cs, String permission) {
 		if (cs.isOp()) return true;
 		else if (cs.hasPermission(permission)) return true;
 		return false;
 	}
 
 	/**
 	 * Will be called if the Command Sender does not have permission to use the subcommand
 	 * as specified by the senderHasPermission() method.
 	 *
 	 * @param cs the Command Sender
 	 * @param label the name/alias of the subcommand called
 	 * @param args the arguments of the subcommand (args[1] through args[length-1] of the original arguments)
 	 */
 	public abstract void ifNoPermission(CommandSender cs, String label, String[] args);
 
 	/**
 	 * Will be called if the Subcommand specified by the user does not exist.
 	 *
 	 * @param cs the Command Sender
 	 * @param label the name of the non-existent subcommand
 	 * @param args the arguments of the subcommand (the full args[] of the original command)
 	 */
 	public abstract void ifNotSubcommand(CommandSender cs, String label, String[] args);
 
 	/**
 	 * Data storage
 	 */
 	private final class Subcommander {
 
 		private final String name;
 		private final String[] alias;
 		private final String permission;
 		private final Object object;
 		private final Method method;
 		private Method tabMethod = null;
 
 		Subcommander(Object object, Method method, String name, String[] alias, String permission) {
 			this.object = object;
 			this.name = name;
 			this.alias = alias;
 			this.permission = permission;
 			this.method = method;
 		}
 
 		String getName() {
 			return name;
 		}
 
 		String[] getAlias() {
 			return alias;
 		}
 
 		String getPermission() {
 			return permission;
 		}
 
 		Object getObject() {
 			return object;
 		}
 
 		Method getMethod() {
 			return method;
 		}
 
 		Method getTabMethod() {
 			return tabMethod;
 		}
 
 		void setTabMethod(Method method) {
 			this.tabMethod = method;
 		}
		
		@Override
		public String toString(){
			return "Subcommander[name=" + name + ",alias=" + Arrays.asList(alias) + "]";
		}
 
 	}
 
 }
