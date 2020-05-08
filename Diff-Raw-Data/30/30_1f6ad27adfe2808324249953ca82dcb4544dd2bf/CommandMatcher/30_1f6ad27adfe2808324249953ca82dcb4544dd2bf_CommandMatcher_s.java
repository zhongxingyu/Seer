 package org.shininet.bukkit.itemrenamer;
 
 import java.util.Deque;
 import java.util.Map;
 import java.util.Set;
 
 import com.google.common.collect.Maps;
 
 class CommandMatcher<TType extends Enum<TType>> {
 	/**
 	 * Represents a possible command.
 	 * 
 	 * @author Kristian
 	 */
 	public class CommandNode {
 		// NULL for no command
 		private TType command;
 		private String permission;
 		
 		private String name;
 		private String path = "";
 		
 		private Map<String, CommandNode> children = Maps.newHashMap();
 		
 		private CommandNode(String permission, String name, TType command) {
 			this.name = name;
 			this.command = command;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public TType getCommand() {
 			return command;
 		}
 
 		public CommandNode getChild(String name) {
 			return children.get(name);
 		}
 		
 		public CommandNode addChild(CommandNode node) {
 			if (node == null)
 				throw new IllegalArgumentException("Node cannot be NULL.");
 			
 			node.path = (path.length() > 0 ? (path + " ") : "") + node.getName();
 			children.put(node.getName(), node);
 			return node;
 		}
 		
 		public CommandNode addOrCreate(String permission, String name, TType command) {
 			if (children.containsKey(name)) 
 				return children.get(name);
 			return addChild(new CommandNode(permission, name, command));
 		}
 		
 		public String getPath() {
 			return path;
 		}
 		
 		public boolean isCommand() {
 			return command != null;
 		}
 		
 		public String getPermission() {
 			return permission;
 		}
 
 		public Set<String> getChildren() {
 			return children.keySet();
 		}
 	}
 	
 	/**
 	 * Represents anything.
 	 */
 	private CommandNode root = new CommandNode("", "", null);
 	
 	public CommandNode matchClosest(Deque<String> arguments) {
 		CommandNode current = root;
 		
 		while (!arguments.isEmpty()) {
 			CommandNode next = current.getChild(arguments.peekFirst());
 			
 			// No match
 			if (next == null)
 				return current;
 			arguments.poll();
 
 			// We found the command
 			if (next.isCommand())
 				return next;
 			
 			// Next argument
 			current = next;
 		}
 		return current;
 	}
 	
 	public void registerCommand(TType command, String permission, String... arguments) {
 		CommandNode current = root;
 		
 		// Create the tree structure
 		for (int i = 0; i < arguments.length; i++) {
 			boolean isLast = i == arguments.length - 1;
 			current = current.addOrCreate(permission, arguments[i], isLast ? command : null);
 		}
 	}
 }
