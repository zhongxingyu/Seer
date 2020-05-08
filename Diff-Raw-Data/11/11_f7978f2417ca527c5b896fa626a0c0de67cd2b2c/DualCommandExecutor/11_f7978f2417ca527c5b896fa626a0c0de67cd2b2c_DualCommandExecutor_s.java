 package uk.co.drnaylor.mcmmopartyadmin.dualcommandexecutor;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import org.apache.commons.lang.ArrayUtils;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.TabExecutor;
 
 /**
  * Class that handles commands and dynamically registering sub commands for a command within 
  * plugin. Cannot be instantiated.
  *
  * @author Daniel Naylor
  */
 public abstract class DualCommandExecutor implements TabExecutor {
 
     private Map<Class<? extends DualSubCommandInterface>, DualSubCommandInterface> subCommands;
 
     public DualCommandExecutor() {
         subCommands = new HashMap<Class<? extends DualSubCommandInterface>, DualSubCommandInterface>();
     }
 
     /**
      * Registers a new subcommand for use in a command
      *
      * @param sci SubCommandInterface derived class which contains logic for the
      * subcommand
      * @throws SubCommandException
      */
     public final void RegisterSubCommand(DualSubCommandInterface sci) throws SubCommandException {
         // If the command is registered, then throw an exception.
         if (subCommands.containsKey(sci.getClass())) {
             throw new SubCommandException("This subcommand is already registered.", sci);
         }
 
         // Put the command into the map ready to be used.
         subCommands.put(sci.getClass(), sci);
     }
 
     /**
      * Execute a subcommand.
      *
      * @param subcommand Subcommand to execute
      * @param sender Command sender
      * @param arguments Arguments to subcommand
      * @throws SubCommandNotRegisteredException Thrown if the subcommand is not
      * registered.
      */
     public final void ExecuteSubCommand(String subcommand, CommandSender sender, String[] arguments) throws SubCommandNotRegisteredException, SubCommandNoPermissionsException {
         // Execute the subcommand. Will throw exception if the command is not registered.
         if (!getInstanceFromSubcommand(subcommand).checkPermissions(sender)) {
             throw new SubCommandNoPermissionsException("No permission", getInstanceFromSubcommand(subcommand).getPermissions());
         }
 
         getInstanceFromSubcommand(subcommand).executeSubCommand(sender, arguments);
     }
 
     /**
      * Execute a subcommand.
      *
      * @param subcommand Subcommand to execute
      * @param sender Command sender
      * @param arguments Arguments to subcommand
      * @throws SubCommandNotRegisteredException Thrown if the subcommand is not
      * registered.
      */
     public final void UnregisterSubCommand(String subcommand) throws SubCommandNotRegisteredException {
 
         // Will throw exception if the command is not registered.
         DualSubCommandInterface iface = getInstanceFromSubcommand(subcommand);
 
         // Remove it
         subCommands.remove(iface.getClass());
     }
 
     /**
      * Get the collection of DualSubCommandInterface derived objects that are
      * bound to this command.
      *
      * @return Collection of DualSubCommandInterface objects.
      */
     public final Collection<DualSubCommandInterface> GetSubCommands() {
         return subCommands.values();
     }
 
     /**
      * Executes the command. If there is a subcommand to execute, execute it.
      * Otherwise, pass to the relevant method. Cannot be overridden.
      *
      * @param sender Sender of the command
      * @param command Command requested
      * @param label Label of the command
      * @param args Arguments of the command - args[0] will be taken to be the
      * subcommand, if any.
      * @return Should normally be true.
      */
     @Override
     public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (args == null || args.length == 0) {
             return onNoSubCommand(sender, command, label);
         }
 
         try {
             String[] subargs = args.clone();
             subargs = (String[]) ArrayUtils.remove(subargs, 0);
             ExecuteSubCommand(args[0], sender, subargs);
             return true;
         } catch (SubCommandNotRegisteredException e) {
             return onInvalidSubCommand(sender, command, label, args);
         } catch (SubCommandNoPermissionsException e) {
             return onNoPermissionsSubCommand(sender, command, label, args, e.getRequiredPermissions());
         }
     }
     
     /**
      * Autocompletes the command on TAB. If there is zero or one partially written argument, provide a list of subcommands. 
      * Otherwise, pass through to the "onSubCommandTabComplete" command, which should be overridden by subclasses. 
      * This method cannot be overridden.
      *
      * @param sender Sender of the command
      * @param command Command requested
      * @param label Label of the command
      * @param args Arguments of the command - args[0] will be taken to be the
      * subcommand, if any.
      * @return Should normally be true.
      */
     @Override
     public final List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
         if (args.length == 1) {
            return getAllSubcommands();
         }
         
         // Remove the other arguments before sending to the sub command.
         String[] subargs = args.clone();
         subargs = (String[]) ArrayUtils.remove(subargs, 0);
         try {
             return getInstanceFromSubcommand(args[0]).onSubCommandTabComplete(sender, subargs);
         }
         finally {
             return null;
         }
     }
 
     /**
      * This method runs if no arguments are supplied to the command.
      *
      * @param sender Sender of the command
      * @param cmnd Command that has been sent
      * @param label Command label
      * @return true or false, dependent on what you wish the onCommand method to
      * return. Should normally be true.
      */
     public abstract boolean onNoSubCommand(CommandSender sender, Command command, String label);
 
     /**
      * This method runs if an invalid subcommand is supplied.
      *
      * @param sender Sender of the command
      * @param cmnd Command that has been sent
      * @param label Command label
      * @param args Arguments originally passed to the command
      * @return true or false, dependent on what you wish the onCommand method to
      * return. Should normally be true.
      */
     public abstract boolean onInvalidSubCommand(CommandSender sender, Command command, String label, String[] args);
 
     /**
      * This method runs if subcommand is requested but the sender does not have permission.
      *
      * @param sender Sender of the command
      * @param cmnd Command that has been sent
      * @param label Command label
      * @param args Arguments originally passed to the command
      * @param requiredPerms Permissions that are required for the command just executed
      * @return true or false, dependent on what you wish the onCommand method to
      * return. Should normally be true.
      */
     public abstract boolean onNoPermissionsSubCommand(CommandSender sender, Command command, String label, String[] args, List<String> requiredPerms);
     
     /**
      * Get the instance associated with a subcommand.
      *
      * @param sub Subcommand to check.
      * @return A DualSubCommandInterface instance.
      * @throws SubCommandNotRegisteredException Thrown if no instance is found.
      */
     private DualSubCommandInterface getInstanceFromSubcommand(String sub) throws SubCommandNotRegisteredException {
         for (DualSubCommandInterface sc : subCommands.values()) {
             if (sc.getSubCommands().contains(sub)) {
                 return sc;
             }
         }
 
         // Throws exception if no instance uses that subcommand
         throw new SubCommandNotRegisteredException("The subcommand " + sub + " is not registered.");
     }
     
     private List<String> getAllSubcommands() {
         List<String> s = new LinkedList<String>();
         for (DualSubCommandInterface sc : subCommands.values()) {
             s.addAll(sc.getSubCommands());
         }
         return s;
     }
 }
