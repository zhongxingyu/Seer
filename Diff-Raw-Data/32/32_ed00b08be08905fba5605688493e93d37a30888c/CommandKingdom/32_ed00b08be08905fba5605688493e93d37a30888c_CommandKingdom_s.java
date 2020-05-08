 package uk.co.quartzcraft.kingdoms.command;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
import com.sun.tools.javac.util.List;
 
 import uk.co.quartzcraft.core.QuartzCore;
 import uk.co.quartzcraft.core.command.QSubCommand;
 import uk.co.quartzcraft.kingdoms.QuartzKingdoms;
 
 public class CommandKingdom implements CommandExecutor {
 	
 	private static HashMap<List<String>, QSubCommand> commands = new HashMap<List<String>, QSubCommand>();
     
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
             if(args.length >= 1) {
                     boolean match = false; 
                     
                     for(List<String> s : commands.keySet())
                     {
                             if(s.contains(args[0]))
                             {
                                     commands.get(s).runCommand(sender, cmd, label, args);
                                     match = true;
                             }
                     }
                     
                     if(!match)
                     {
                            sender.sendMessage("Unknown Command. Visit the QuartzCraft Wiki for help.");
                     }
             }
             else {
                    sender.sendMessage("Please specify a SubCommand. Visit the QuartzCraft Wiki for help.");
             }
             
             return true;
     }
     
     public static void addCommand(List<String> cmds, QSubCommand s) {
             commands.put(cmds, s);
     }
 }
