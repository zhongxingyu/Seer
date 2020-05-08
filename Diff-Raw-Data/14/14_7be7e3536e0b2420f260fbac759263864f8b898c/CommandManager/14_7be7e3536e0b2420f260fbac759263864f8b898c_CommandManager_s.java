 package net.catharos.lib.command;
 
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.catharos.lib.util.ArrayUtil;
 import net.catharos.lib.util.MessageUtil;
 
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 
 public class CommandManager implements CommandExecutor {
 	protected final Map<String, Command> cmds;
 	protected final Map<Command, Method> methods;
 	protected final Map<Method, Object> instances;
 	
 	
 	public CommandManager() {
 		this.cmds = new HashMap<String, Command>();
 		this.methods = new HashMap<Command, Method>();
 		this.instances = new HashMap<Method, Object>();
 	}
 	
 	public boolean registerCommands(Class clazz) {
 		boolean success = true;
 		
		for(Method method : clazz.getMethods()) {
 			Command cmd = method.getAnnotation(Command.class);
 			
 			if(method.getGenericReturnType() != Boolean.class) {
 				success = false;
 				continue;
 			}
 			
 			Class<?>[] params = method.getParameterTypes();
 			if(params.length != 2 || (params[0] != CommandSender.class) || !(params[1].isArray())) {
 				success = false;
 				continue;
 			}
 			
 			cmds.put(cmd.name().toLowerCase(), cmd);
 			
 			for(String s : cmd.aliases()) {
 				cmds.put(s.toLowerCase(), cmd);
 			}
 			
 			methods.put(cmd, method);
 		}
 		
 		return success;
 	}
 
 	public boolean onCommand(CommandSender cs, org.bukkit.command.Command cmnd, String label, String[] args) {
 		String str;
 		Command cmd;
 		
 		for (int args_left = args.length; args_left >= 0; args_left--) {
 			// Build command string
 			str = label + ((args_left > 0) ? " " + ArrayUtil.implode( Arrays.copyOfRange( args, 0, args_left ), " " ) : "");
 			
 			cmd = getCommand(str);
 			
 			if(cmd != null) try {
 				// Check for console / player
 				if (!(cs instanceof Player) && !cmd.consoleCmd()) {
 					throw new Exception("The command cannot be executed from console." );
 				}
 				
 				// Check permisisons
 				if (!hasPermission( cs, cmd.permission() )) {
 					throw new Exception("You are not allowed to use that command!" );
 				}
 				
 				boolean help = (args.length > 0 && args[args.length - 1].equalsIgnoreCase( "?" )) ? true : false;
 
 				if (!help) {
 					String[] cmd_args = Arrays.copyOfRange( args, args_left, args.length );
 
 					if (cmd_args.length < cmd.minArgs()|| cmd_args.length > cmd.maxArgs()) {
 						help = true;
 					} else {
 						Method method = methods.get(cmd);
 						
 						if(method == null) throw new Exception("Something went wrong: Method not found. Please call an admin!");
 						
 						help = (Boolean) method.invoke(getInstance(method), cs, args);
 					}
 				}
 
 				if (help) {
 					MessageUtil.sendMessage( cs, "&6----------------=[ &cHelp: &3%0&6]=----------------", cmd.name() );
 
 					String desc = cmd.description();
 					MessageUtil.sendMessage( cs, "&6Description&f: %0", desc.isEmpty() ? "&7(no description available)" : desc );
 
 					String usage = cmd.usage();
 					MessageUtil.sendMessage( cs, "&6Usage: &f%0", usage.isEmpty() ? "/" + cmd.name() : usage );
 					MessageUtil.sendMessage( cs, "&6Aliases: &f/%0", ArrayUtil.implode( cmd.aliases(), ", /" ) );
 				}
 				
 				return true;
 				
 			} catch( Exception ex ) {
 				MessageUtil.sendError( cs, ex.getMessage());
 				return true;
 			}
 		}
 		
 		// Send error message
 		if (args.length > 0) {
 			MessageUtil.sendError( cs, "Command not found: &6%0 &7(/%0 %1)", label, ArrayUtil.implode( args, " " ) );
 		} else {
 			MessageUtil.sendError( cs, "Command not found: &6%0", label );
 		}
 		
 		return false;
 	}
 	
 	public static boolean hasPermission( CommandSender sender, String perm ) {
 		if ((!(sender instanceof Player)) || (perm == null) || (perm.isEmpty())) return true;
 
 		Player player = (Player) sender;
 		return player.isOp() || player.hasPermission(perm);
 	}
 	
 	public Command getCommand( String label ) {
 		return cmds.get(label);
 	}
 	
 	private Object getInstance( Method m ) {
 		return instances.get(m);
 	}
 	
 }
