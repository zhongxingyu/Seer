 
 package ee.lutsu.alpha.mc.aperf.commands;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.minecraft.command.CommandBase;
 import net.minecraft.command.ICommandSender;
 import net.minecraft.entity.player.EntityPlayer;
 
 import com.google.common.base.Joiner;
 
 import ee.lutsu.alpha.mc.aperf.ChatColor;
 import ee.lutsu.alpha.mc.aperf.Log;
 import ee.lutsu.alpha.mc.aperf.Permissions;
 
 public class CommandsManager 
 {
 	protected Map<String, Map<CommandSyntax, CommandBinding>> listeners = new LinkedHashMap<String, Map<CommandSyntax, CommandBinding>>();
 	protected Object plugin;
 	protected List<Object> helpObjects = new LinkedList<Object>();
 
 	public CommandsManager(Object plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	public void register(BaseCommand listener) 
 	{
 		for (Method method : listener.getClass().getMethods()) 
 		{
 			if (!method.isAnnotationPresent(Command.class)) 
 				continue;
 
 			Command cmdAnnotation = method.getAnnotation(Command.class);
 
 			Map<CommandSyntax, CommandBinding> commandListeners = listeners.get(cmdAnnotation.name());
 			if (commandListeners == null)
 			{
 				commandListeners = new LinkedHashMap<CommandSyntax, CommandBinding>();
 				listeners.put(cmdAnnotation.name(), commandListeners);
 			}
 
 			commandListeners.put(new CommandSyntax(cmdAnnotation.syntax(), cmdAnnotation.isPlayerOnly()), new CommandBinding(listener, method));
 		}
 
 		listener.onRegistered(this);
 	}
 
 	public boolean execute(ICommandSender sender, CommandBase command, String[] args) 
 	{
 		Map<CommandSyntax, CommandBinding> callMap = this.listeners.get(command.getCommandName());
 
 		if (callMap == null) // No commands registered
 			return false;
 
 		CommandBinding selectedBinding = null;
 		int argumentsLength = args.length;
 		String arguments = Joiner.on(" ").join(args);
 
 		for (Entry<CommandSyntax, CommandBinding> entry : callMap.entrySet()) 
 		{
 			CommandSyntax syntax = entry.getKey();
 			if (syntax.playerNeeded && !(sender instanceof EntityPlayer))
 				continue;
 			
 			if (!syntax.isMatch(arguments)) 
 				continue;
 
 			if (selectedBinding != null && syntax.getRegexp().length() < argumentsLength) // match, but there already more fitted variant
 				continue;
 
 			CommandBinding binding = entry.getValue();
 			binding.setParams(syntax.getMatchedArguments(arguments));
 			selectedBinding = binding;
 		}
 
 		if (selectedBinding == null) // there is fitting handler
 		{ 
 			sender.sendChatToPlayer(ChatColor.RED + "Error in command syntax. Check command help.");
 			return true;
 		}
 
 		// Check permission
 		if (sender instanceof EntityPlayer) // this method are not public and required permission
 		{ 
 			if (!selectedBinding.checkPermissions((EntityPlayer) sender)) {
 				Log.warning("User §4" + ((EntityPlayer) sender).username + " §7tried to access chat command \"" 
 						+ command.getCommandName() + " " + arguments
 						+ "\", but §4doesn't have permission §7to do this.");
 				sender.sendChatToPlayer(ChatColor.RED + "Sorry, you don't have enough permissions.");
 				return true;
 			}
 		}
 
 		try 
 		{
 			selectedBinding.call(this.plugin, sender, selectedBinding.getParams());
 		}
 		catch (CommandException e)
 		{
 			sender.sendChatToPlayer(ChatColor.RED + "Command error: " + e.getMessage());
 		}
 		catch (Throwable e) 
 		{
			Log.severe("There is bogus command handler for " + command.getCommandName() + " command.", e);
 			sender.sendChatToPlayer(ChatColor.RED + "Command exception: " + (e.getMessage() == null ? "unknown" : e.getMessage()));
 		}
 
 		return true;
 	}
 
 	public List<CommandBinding> getCommands() 
 	{
 		List<CommandBinding> commands = new LinkedList<CommandBinding>();
 
 		for (Map<CommandSyntax, CommandBinding> map : this.listeners.values()) {
 			commands.addAll(map.values());
 		}
 
 		return commands;
 	}
 
 	protected class CommandSyntax 
 	{
 		protected String originalSyntax;
 		protected String regexp;
 		protected List<String> arguments = new LinkedList<String>();
 		public boolean playerNeeded = false;
 
 		public CommandSyntax(String syntax, boolean playerNeeded) 
 		{
 			this.originalSyntax = syntax;
 			this.playerNeeded = playerNeeded;
 
 			this.regexp = this.prepareSyntaxRegexp(syntax);
 		}
 
 		public String getRegexp() 
 		{
 			return regexp;
 		}
 
 		private String prepareSyntaxRegexp(String syntax) 
 		{
 			String expression = syntax;
 
 			Matcher argMatcher = Pattern.compile("(?:[\\s]+)((\\<|\\[)([^\\>\\]]+)(?:\\>|\\]))").matcher(expression);
 
 			int index = 0;
 			while (argMatcher.find()) {
 				if (argMatcher.group(2).equals("[")) {
 					expression = expression.replace(argMatcher.group(0), "(?:(?:[\\s]+)(\"[^\"]+\"|[^\\s]+))?");
 				} else {
 					expression = expression.replace(argMatcher.group(1), "(\"[^\"]+\"|[\\S]+)");
 				}
 
 				arguments.add(index++, argMatcher.group(3));
 			}
 
 			return expression;
 		}
 
 		public boolean isMatch(String str) 
 		{
 			return str.matches(this.regexp);
 		}
 
 		public Map<String, String> getMatchedArguments(String str)
 		{
 			Map<String, String> matchedArguments = new HashMap<String, String>(this.arguments.size());
 
 			if (this.arguments.size() > 0) {
 				Matcher argMatcher = Pattern.compile(this.regexp).matcher(str);
 
 				if (argMatcher.find()) {
 					for (int index = 1; index <= argMatcher.groupCount(); index++) {
 						String argumentValue = argMatcher.group(index);
 						if (argumentValue == null || argumentValue.isEmpty()) {
 							continue;
 						}
 
 						if (argumentValue.startsWith("\"") && argumentValue.endsWith("\"")) { // Trim boundary colons
 							argumentValue = argumentValue.substring(1, argumentValue.length() - 1);
 						}
 
 						matchedArguments.put(this.arguments.get(index - 1), argumentValue);
 					}
 				}
 			}
 			return matchedArguments;
 		}
 	}
 
 	public class CommandBinding
 	{
 		protected Object object;
 		protected Method method;
 		protected Map<String, String> params = new HashMap<String, String>();
 
 		public CommandBinding(Object object, Method method)
 		{
 			this.object = object;
 			this.method = method;
 		}
 
 		public Command getMethodAnnotation()
 		{
 			return this.method.getAnnotation(Command.class);
 		}
 
 		public Map<String, String> getParams()
 		{
 			return params;
 		}
 
 		public void setParams(Map<String, String> params) 
 		{
 			this.params = params;
 		}
 
 		public boolean checkPermissions(EntityPlayer player) 
 		{
 			String permission = this.getMethodAnnotation().permission();
 
 			if (permission.contains("<")) {
 				for (Entry<String, String> entry : this.getParams().entrySet()) {
 					if (entry.getValue() != null) {
 						permission = permission.replace("<" + entry.getKey() + ">", entry.getValue().toLowerCase());
 					}
 				}
 			}
 
 			return Permissions.canAccess(player, permission);
 		}
 
 		public void call(Object... args) throws Exception
 		{
 			this.method.invoke(object, args);
 		}
 	}
 }
