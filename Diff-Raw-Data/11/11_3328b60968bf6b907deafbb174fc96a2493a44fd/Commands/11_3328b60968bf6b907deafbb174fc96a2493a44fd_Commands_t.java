 package io.github.totemo.doppelganger;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.BlockCommandSender;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 
 import com.amoebaman.kitmaster.utilities.CommandController.CommandHandler;
 import com.amoebaman.kitmaster.utilities.CommandController.SubCommandHandler;
 
 // --------------------------------------------------------------------------
 /**
  * Handles the command line.
  */
 public class Commands
 {
   // --------------------------------------------------------------------------
   /**
    * Constructor.
    * 
    * @param plugin the reference to the Doppelganger plugin.
    */
   public Commands(Doppelganger plugin)
   {
     _plugin = plugin;
     _help = new Help(plugin);
   }
 
   // --------------------------------------------------------------------------
   /**
    * This method is called if none of the subcommands matches exactly.
    * 
    * We use it to print the help messages for all commands.
    */
   @CommandHandler(name = "doppel", permission = "doppel.help")
   public void onCommand(CommandSender sender, String[] args)
   {
     if (args.length > 0)
     {
       sender.sendMessage(_failureColour + "\"" + args[0] + "\"" + " is not a valid /doppel subcommand.");
     }
 
     sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
       "&eUsage summary:"));
     for (String topic : _help.getTopics())
     {
       _help.showHelp(sender, topic, false);
     }
     sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
       "&eType /doppel help &d&osubcommand &efor detailed descriptions of subcommands."));
   }
 
   // --------------------------------------------------------------------------
   /**
    * Handle the /doppel info [list | shape &lt;name&gt; | creature &lt;name&gt;
    * | player &lt;name&gt;] command.
    */
   @SubCommandHandler(parent = "doppel", name = "help", permission = "doppel.help")
   public void onCommandDoppeHelp(CommandSender sender, String[] args)
   {
     if (args.length == 2)
     {
       String topic = args[1];
       if (_help.getTopics().contains(topic))
       {
         _help.showHelp(sender, topic, true);
         return;
       }
       else
       {
         sender.sendMessage(_failureColour + "\"" + topic + "\" is not a valid help topic.");
         // Fall through.
       }
     }
 
     // In all other cases, including failure:
     _help.showHelp(sender, "help", true);
   }
 
   // --------------------------------------------------------------------------
   /**
    * Handle the /doppel reload command.
    */
   @SubCommandHandler(parent = "doppel", name = "reload", permission = "doppel.reload")
   public void onCommandDoppeReload(CommandSender sender, String[] args)
   {
     if (args.length == 1)
     {
       _plugin.reloadConfig();
       sender.sendMessage(_successColour + "Doppelganger configuration reloaded.");
     }
     else
     {
       showUsage(sender, "reload");
     }
   }
 
   // --------------------------------------------------------------------------
   /**
    * Handle the /doppel info [list | shape &lt;name&gt; | creature &lt;name&gt;
    * | player &lt;name&gt;] command.
    */
   @SubCommandHandler(parent = "doppel", name = "info", permission = "doppel.info")
   public void onCommandDoppelInfo(CommandSender sender, String[] args)
   {
     if (args.length == 2)
     {
       if (args[1].equals("list"))
       {
         _plugin.getCreatureFactory().listConfiguration(sender);
         return;
       }
     }
     else if (args.length == 3)
     {
       if (args[1].equals("shape"))
       {
         CreatureShape shape = _plugin.getCreatureFactory().getCreatureShape(args[2]);
         if (shape == null)
         {
           sender.sendMessage(_failureColour + "There is no shape by that name.");
         }
         else
         {
           shape.describe(sender);
         }
         return;
       }
       else if (args[1].equals("creature"))
       {
         CreatureType type = _plugin.getCreatureFactory().getCreatureType(args[2]);
         if (type == null)
         {
           sender.sendMessage(_failureColour + "There is no creature type by that name.");
         }
         else
         {
           type.describe(sender);
         }
         return;
       }
       else if (args[1].equals("player"))
       {
         String name = args[2];
         String creature = _plugin.getCreatureFactory().getPlayerCreature(name);
         if (creature == null)
         {
           sender.sendMessage(_failureColour + "No specific creature type is defined for that player name.");
         }
         else
         {
           sender.sendMessage(_successColour + "Player " + name +
                              " will spawn a creature of type: " + ChatColor.YELLOW + creature);
           ArrayList<CreatureShape> shapes = _plugin.getCreatureFactory().getPlayerShapes(name);
           if (shapes.size() == 0)
           {
             sender.sendMessage(_successColour + name
                                + "can only be spawned by command because no summoning shapes are defined.");
           }
           else
           {
             StringBuilder message = new StringBuilder();
             message.append(_successColour);
             message.append(name);
             message.append(" can be spawned by the following shapes:");
             message.append(ChatColor.YELLOW);
             for (CreatureShape shape : shapes)
             {
               message.append(' ');
               message.append(shape.getName());
             }
             sender.sendMessage(message.toString());
           }
         }
         return;
       }
     }
 
     showUsage(sender, "info");
   } // onCommandDoppelInfo
 
   // --------------------------------------------------------------------------
   /**
    * Handle /doppel coords:
    * <ul>
    * <li>/doppel coords sphere here radius [name]</li>
    * <li>/doppel coords sphere [world] x y z radius [name]</li>
    * <li>/doppel coords box [world] x1 y1 z1 x2 y2 z2 [name]</li>
    * </ul>
    * If the name is not specified, any name will match. The CommandSender can be
    * a player or a command block.
    */
   @SubCommandHandler(parent = "doppel", name = "coords", permission = "doppel.coords")
   public void onCommandDoppelCoords(CommandSender sender, String[] args)
   {
     ArrayList<String> tail = tail(args, 1);
     Volume volume = parseVolume(tail, sender);
     String name = (tail.size() > 0) ? tail.remove(0) : null;
 
     // By this point we should have consumed all the arguments.
     if (volume == null || tail.size() > 0)
     {
       showUsage(sender, "coords");
     }
     else
     {
       // Arguments are valid. Do the work.
       ArrayList<LivingEntity> doppelgangers = findDoppelgangers(name, volume);
       String message = formatVolumeMessage(volume, "There are", doppelgangers.size());
 
       // Command blocks output a redstone signal if result message is not red.
       // Well... ideally, if the Minecraft wiki wasn't full of lies.
       ChatColor colour = (doppelgangers.size() == 0) ? _failureColour : _successColour;
       sender.sendMessage(colour + message);
 
       for (int i = 0; i < doppelgangers.size(); ++i)
       {
         LivingEntity living = doppelgangers.get(i);
         Location loc = living.getLocation();
         sender.sendMessage(String.format("%s(%d) %s %s (%d, %d, %d)",
           _successColour, i + 1, living.getCustomName(),
           CreatureFactory.getLivingEntityType(living),
           loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
       }
     }
   } // onCommandDoppelCoords
 
   // --------------------------------------------------------------------------
   /**
    * Handle /doppel:
    * 
    * <pre>
    * /doppel kill sphere here radius name"
    * /doppel kill sphere [world] x y z radius name"
    * /doppel kill box [world] x1 y1 z1 x2 y2 z2 name"
    * </pre>
    * 
    * @param sender the issuer of the command.
    * @param args command arguments after the initial /doppel.
    */
   @SubCommandHandler(parent = "doppel", name = "kill", permission = "doppel.kill")
   public void onCommandDoppelKill(CommandSender sender, String[] args)
   {
     ArrayList<String> tail = tail(args, 1);
     Volume volume = parseVolume(tail, sender);
 
     // Need a valid volume and a final argument for the name.
     if (volume == null || tail.size() != 1)
     {
       showUsage(sender, "kill");
     }
     else
     {
       ArrayList<LivingEntity> doppelgangers = findDoppelgangers(tail.get(0), volume);
       String message = formatVolumeMessage(volume, "Killing", doppelgangers.size());
       ChatColor colour = (doppelgangers.size() == 0) ? _failureColour : _successColour;
       sender.sendMessage(colour + message);
 
       for (int i = 0; i < doppelgangers.size(); ++i)
       {
         LivingEntity living = doppelgangers.get(i);
         Location loc = living.getLocation();
         String description = String.format("%s(%d) %s %s (%d, %d, %d)",
           _successColour, i + 1, living.getCustomName(), CreatureFactory.getLivingEntityType(living),
           loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
         sender.sendMessage(description);
         _plugin.getLogger().info("Killing " + description);
         living.remove();
       }
     }
   } // onCommandDoppelKill
 
   // --------------------------------------------------------------------------
   /**
    * Handle /doppel spawn:
    * 
    * <ul>
    * <li>/doppel spawn here type [name]</li>
    * <li>/doppel spawn at [world] x y z type [name]</li>
    * </ul>
    * 
    * @param sender the issuer of the command.
    * @param args command arguments after the initial /doppel.
    */
   @SubCommandHandler(parent = "doppel", name = "spawn", permission = "doppel.spawn")
   public void onCommandDoppelSpawn(CommandSender sender, String[] args)
   {
     ArrayList<String> tail = tail(args, 1);
     Location loc = parseLocation(tail, sender);
     String type = (tail.size() > 0) ? tail.remove(0) : null;
     String name = (tail.size() > 0) ? tail.remove(0) : null;
 
     // name is allowed to be null.
     if (loc == null || type == null || tail.size() > 0)
     {
       showUsage(sender, "spawn");
     }
     else
     {
       spawnAndLog(sender, type, name, loc);
     }
   } // onCommandDoppelSpawn
 
   // --------------------------------------------------------------------------
   /**
    * Handle /doppel maintain:
    * 
    * <ul>
    * <li>/doppel maintain at [world] x y z box [world] x1 y1 z1 x2 y2 z2 type
    * name</li>
    * <li>/doppel maintain at [world] x y z sphere [world] xc yc zc radius type
    * name</li>
    * </pre>
    * 
    * Technically, here is also allowed as a spawn location, but it's not much
    * use since this command is really just for command blocks, and mobs spawned
    * into a command block will be stuck.
    * 
    * The name parameter is required so that we can find the doppelganger (by
    * name) in the specified volume.
    * 
    * @param sender the issuer of the command.
    * @param args command arguments after the initial /doppel.
    */
   @SubCommandHandler(parent = "doppel", name = "maintain", permission = "doppel.maintain")
   public void onCommandDoppelMaintain(CommandSender sender, String[] args)
   {
     ArrayList<String> tail = tail(args, 1);
     Location loc = parseLocation(tail, sender);
     Volume volume = parseVolume(tail, sender);
     String type = (tail.size() > 0) ? tail.remove(0) : null;
     String name = (tail.size() > 0) ? tail.remove(0) : null;
     if (loc == null || volume == null || type == null || name == null || tail.size() > 0)
     {
      showUsage(sender, "maintain");
     }
     else
     {
      if (!volume.contains(loc))
      {
        sender.sendMessage(_failureColour + "The checked volume must contain the spawn location.");
        return;
      }

       ArrayList<LivingEntity> doppelgangers = findDoppelgangers(name, volume);
       String message = formatVolumeMessage(volume, "There are", doppelgangers.size());
       sender.sendMessage(_successColour + message);
 
       if (doppelgangers.size() < 1)
       {
         spawnAndLog(sender, type, name, loc);
       }
       else if (doppelgangers.size() > 1)
       {
         // Find the oldest creature.
         LivingEntity oldest = null;
         int ticksLived = -1;
         for (LivingEntity living : doppelgangers)
         {
           if (living.getTicksLived() > ticksLived)
           {
             ticksLived = living.getTicksLived();
             oldest = living;
           }
         }
 
         // Kill all but the oldest.
         sender.sendMessage(String.format("%sKilling %d extra doppelganger(s).",
           _successColour, (doppelgangers.size() - 1)));
         for (LivingEntity living : doppelgangers)
         {
           if (living != oldest)
           {
             living.remove();
           }
         }
       }
     }
   } // onCommandDoppelMaintain
   // --------------------------------------------------------------------------
   /**
    * Return a list of all LivingEntity instances with the specified visible
    * custom name within the specified volume.
    * 
    * I tried setting the name "<anonymous>" when spawning escorts, but not
    * showing the name. The client still shows it when at very short range and
    * for certain view angles only. The name "." is less noticeable, but still
    * noticeable enough that using a hidden custom name is not a viable way of
    * marking Doppelganger-spawned mobs.
    * 
    * @param name the custom name, which must be visible; if this is null, any
    *          name will do.
    * @param volume the volume to be searched for doppelgangers.
    * @return a list of the matching LivingEntity instances.
    */
   protected ArrayList<LivingEntity> findDoppelgangers(String name, Volume volume)
   {
     ArrayList<LivingEntity> doppelgangers = new ArrayList<LivingEntity>();
     for (LivingEntity living : volume.getWorld().getLivingEntities())
     {
       if (living.isCustomNameVisible() &&
           (name == null || name.equals(living.getCustomName())) &&
           volume.contains(living.getLocation()))
       {
         doppelgangers.add(living);
       }
     }
     return doppelgangers;
   }
 
   // --------------------------------------------------------------------------
   /**
    * Spawn a doppelganger with the specified type and optional name at the
    * Location.
    * 
    * Give feedback to the command sender and log successful spawns and
    * unexpected failures.
    * 
    * @param sender the command sender.
    * @param type the creature type name.
    * @param name the name of the creature; if null, the creature is anonymous.
    * @param loc the Location where the creature will spawn.
    */
   protected void spawnAndLog(CommandSender sender, String type, String name, Location loc)
   {
     if (_plugin.getCreatureFactory().isValidCreatureType(type))
     {
       LivingEntity doppelganger = _plugin.spawnDoppelganger(type, name, loc);
       if (doppelganger == null)
       {
         sender.sendMessage(String.format(
           "%sSpawning a %s named %s at (%d,%d,%d) in %s failed unexpectedly.",
           _failureColour, type, name, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName()));
         _plugin.getLogger().info(String.format(
           "%s tried to spawn a %s named %s at (%d,%d,%d) in %s but it failed unexpectedly.",
           sender.getName(), type, name, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName()));
       }
       else
       {
         sender.sendMessage(String.format(
           "%sSpawned a %s named %s at (%d,%d,%d) in %s.",
           _successColour, type, name, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName()));
         _plugin.getLogger().info(String.format(
           "%s spawned a %s named %s at (%d,%d,%d) in %s.",
           sender.getName(), type, name, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName()));
       }
     }
     else
     {
       sender.sendMessage(String.format("%s\"%s\" is not a valid creature type.", _failureColour, type));
     }
   } // spawnAndLog
 
   // --------------------------------------------------------------------------
   /**
    * Show the correct usage syntax of the subcommand corresponding to topic.
    * 
    * @param sender the command sender.
    * @param topic the help topic and subcommand name.
    */
   protected void showUsage(CommandSender sender, String topic)
   {
     sender.sendMessage(_failureColour + "Incorrect arguments.");
     sender.sendMessage(ChatColor.YELLOW + "Usage:");
     _help.showHelp(sender, topic, false);
   }
 
   // --------------------------------------------------------------------------
   /**
    * Return the Location of the specified CommandSender, or null if the sender
    * has no Location (e.g. the console).
    * 
    * @return the Location of the specified CommandSender, or null if the sender
    *         has no Location (e.g. the console).
    */
   protected static Location getLocation(CommandSender sender)
   {
     if (sender instanceof Player)
     {
       return ((Player) sender).getLocation();
     }
     else if (sender instanceof BlockCommandSender)
     {
       return ((BlockCommandSender) sender).getBlock().getLocation();
     }
     else
     {
       return null;
     }
   }
 
   // --------------------------------------------------------------------------
   /**
    * Return a list containing the tail of the args array, from startIndex
    * onwards.
    * 
    * Exclude arguments that are just the empty string. They happen when a user
    * inadvertently adds extra spaces in the command and are probably never
    * actually desired.
    * 
    * @param args an array of Strings.
    * @param startIndex the first element of args that will appear in the
    *          returned list; all previous elements of args are skipped.
    * @return a list containing the tail of the args array, from startIndex
    *         onwards.
    */
   protected static ArrayList<String> tail(String[] args, int startIndex)
   {
     ArrayList<String> list = new ArrayList<String>();
     for (int i = startIndex; i < args.length; ++i)
     {
       if (args[i].length() > 0)
       {
         list.add(args[i]);
       }
     }
     return list;
   }
 
   // --------------------------------------------------------------------------
   /**
    * Dump the current arguments list to the specified Logger for debugging.
    * 
    * @param logger the Logger.
    * @param args the argument list.
    */
   protected static void dumpArgs(Logger logger, ArrayList<String> args)
   {
     StringBuilder message = new StringBuilder();
     message.append("args (");
     message.append(args.size());
     message.append("):");
     for (String arg : args)
     {
       message.append(' ');
       message.append(arg);
     }
     logger.info(message.toString());
   }
 
   // --------------------------------------------------------------------------
   /**
    * Parse a World from the first argument of the argument list and consume it
    * from the arguments if successful.
    * 
    * @param args the argument list.
    * @param sender the command sender, used to infer the default world if the
    *          sender is a player or command block.
    * @return a World if it can be parsed from args, or failing that, inferred
    *         from the sender's Location, or null if not possible to determine a
    *         world. If the World was parsed from the args, the first argument is
    *         removed from the list.
    */
   protected static World parseWorld(ArrayList<String> args, CommandSender sender)
   {
     Server server = sender.getServer();
     World world = (args.size() >= 1) ? server.getWorld(args.get(0)) : null;
     if (world == null)
     {
       Location senderLoc = getLocation(sender);
       if (senderLoc == null)
       {
         return null;
       }
       else
       {
         world = senderLoc.getWorld();
       }
     }
     else
     {
       // Consume the (valid) world name.
       args.remove(0);
     }
     return world;
   } // parseWorld
 
   // --------------------------------------------------------------------------
   /**
    * Parse x, y and z coordinates from the first three arguments in args (if
    * present).
    * 
    * @param args the argument list.
    * @param world the world in which the coordinates apply.
    * @return the corresponding Location, or null on error. If a valid Location
    *         is returned, the first three arguments from args are consumed;
    *         otherwise, no arguments are consumed.
    */
   protected static Location parseCoordinates(ArrayList<String> args, World world)
   {
     if (args.size() >= 3)
     {
       Double x = parseDouble(args, 0);
       Double y = parseDouble(args, 1);
       Double z = parseDouble(args, 2);
       if (x != null && y != null && z != null)
       {
         Location loc = new Location(world, x, y, z);
         args.remove(0);
         args.remove(0);
         args.remove(0);
         return loc;
       }
     }
 
     // All other cases.
     return null;
   } // parseCoordinates
 
   // --------------------------------------------------------------------------
   /**
    * Parse an x, y, z Location with optional World out of the command line
    * arguments and remove those arguments from the list if successful.
    * 
    * Valid argument syntax:
    * 
    * <pre>
    * here
    * at [world] x y z
    * </pre>
    * 
    * If the World is not specified, the World of the CommandSender will be used
    * (if the sender has a Location).
    * 
    * @param args the argument list; successfully parsed arguments are removed
    *          from the list.
    * @param sender the command sender. If the command sender has a Location,
    *          this may be used when required.
    * @return the parsed Location or null on error.
    */
   protected static Location parseLocation(ArrayList<String> args, CommandSender sender)
   {
     if (args.size() >= 1 && args.get(0).equals("here"))
     {
       args.remove(0);
       Location senderLoc = getLocation(sender);
       if (senderLoc == null)
       {
         sender.sendMessage(_failureColour + "This command cannot be run from the console.");
       }
       return senderLoc;
     }
     else if (args.size() >= 4 && args.get(0).equals("at"))
     {
       args.remove(0);
       World world = parseWorld(args, sender);
       if (world == null)
       {
         sender.sendMessage(_failureColour + "You must specify the world.");
         return null;
       }
       else
       {
         Location loc = parseCoordinates(args, world);
         if (loc == null)
         {
           sender.sendMessage(_failureColour + "Invalid coordinates specified.");
         }
         return loc;
       }
     }
 
     // Fall through case.
     return null;
   } // parseLocation
 
   // --------------------------------------------------------------------------
   /**
    * Attempt to parse a double value from args.get(index).
    * 
    * @param args the command line arguments in a List<>.
    * @param index the index into args.
    * @return the parsed double value, or null on error.
    */
   protected static Double parseDouble(ArrayList<String> args, int index)
   {
     if (index >= 0 && index < args.size())
     {
       try
       {
         return Double.parseDouble(args.get(index));
       }
       catch (Exception ex)
       {
         // Silent.
       }
     }
     return null;
   }
 
   // --------------------------------------------------------------------------
   /**
    * Parse a volume from the command line arguments and return a Volume
    * instance, or null on error.
    * 
    * Valid argument syntax:
    * 
    * <pre>
    * sphere here radius
    * sphere [world] x y z radius
    * box [world] x1 y1 z1 x2 y2 z2
    * </pre>
    * 
    * @param sender the command sender. If the command sender has a Location,
    *          this may be used when required.
    * @param args the command line arguments, starting at the first keyword that
    *          introduces a volume description, possibly containing arguments
    *          after the description.
    * @return the corresponding Volume, or null on error. If a non-null Volume is
    *         returned, args will have all all volume parameters removed. If the
    *         the specified volume is invalid (null), all bets are off and args
    *         shouldn't be used again.
    */
   protected static Volume parseVolume(ArrayList<String> args, CommandSender sender)
   {
     try
     {
       if (args.size() >= 3 && args.get(0).equals("sphere"))
       {
         // /doppel coords sphere here radius [name]
         args.remove(0);
         if (args.get(0).equals("here"))
         {
           args.remove(0);
           Location senderLoc = getLocation(sender);
           if (senderLoc == null)
           {
             sender.sendMessage(_failureColour + "This command cannot be run from the console.");
             return null;
           }
 
           Double radius = parseDouble(args, 0);
           if (radius == null || radius < 0)
           {
             sender.sendMessage(_failureColour + "Invalid radius specified.");
             return null;
           }
 
           args.remove(0);
           return new Volume.Sphere(senderLoc, radius);
         }
         else
         {
           // sphere [world] x y z radius
           World world = parseWorld(args, sender);
           if (world == null)
           {
             sender.sendMessage(_failureColour + "You must specify the world.");
             return null;
           }
 
           // If we can parse coordinates and radius then we have *enough*
           // arguments of the correct types.
           Location centre = parseCoordinates(args, world);
           if (centre == null)
           {
             sender.sendMessage(_failureColour + "Invalid coordinates specified.");
             return null;
           }
 
           Double radius = parseDouble(args, 0);
           if (radius == null || radius < 0)
           {
             sender.sendMessage(_failureColour + "Invalid radius specified.");
             return null;
           }
           else
           {
             args.remove(0);
           }
           return new Volume.Sphere(centre, radius);
         }
       }
       else if (args.size() >= 7 && args.get(0).equals("box"))
       {
         // box [world] x1 y1 z1 x2 y2 z2
         args.remove(0);
         World world = parseWorld(args, sender);
         if (world == null)
         {
           sender.sendMessage(_failureColour + "You must specify the world.");
           return null;
         }
 
         Location loc1 = parseCoordinates(args, world);
         if (loc1 == null)
         {
           sender.sendMessage(_failureColour + "Invalid first point specified.");
           return null;
         }
 
         Location loc2 = parseCoordinates(args, world);
         if (loc2 == null)
         {
           sender.sendMessage(_failureColour + "Invalid second point specified.");
           return null;
         }
 
         // Box deals with loc1 > loc2 by sorting/swapping the coords.
         return new Volume.Box(loc1, loc2);
       }
     }
     catch (Exception ex)
     {
     }
     return null;
   } // parseVolume
 
   // --------------------------------------------------------------------------
   /**
    * Format a message about the shape of the volume, the number of matching
    * doppelgangers in it and what will happen to them.
    * 
    * @param volume the Volume.
    * @param messageStart the start of the message.
    * @param doppelgangerCount the number of matching doppelgangers found in the
    *          Volume.
    */
   protected static String formatVolumeMessage(Volume volume, String messageStart, int doppelgangerCount)
   {
     if (volume instanceof Volume.Sphere)
     {
       Volume.Sphere sphere = (Volume.Sphere) volume;
       double radius = sphere.getRadius();
       if (radius > 1e9)
       {
         return String.format("%s %d matching doppelgangers loaded in this world.",
           messageStart, doppelgangerCount);
       }
       else
       {
         Location centre = sphere.getCentre();
         return String.format("%s %d matching doppelgangers loaded within %5g blocks of (%d, %d, %d) in %s.",
           messageStart, doppelgangerCount, radius, centre.getBlockX(), centre.getBlockY(), centre.getBlockZ(),
           centre.getWorld().getName());
       }
     }
     else
     {
       // Box is currently the only other option.
       return String.format("%s %d matching doppelgangers loaded in the specified box.",
         messageStart, doppelgangerCount);
     }
   } // formatVolumeMessage
 
   // --------------------------------------------------------------------------
   /**
    * Default colour of messages on success.
    */
   protected static final ChatColor _successColour = ChatColor.GOLD;
 
   /**
    * Default colour of messages on failure.
    */
   protected static final ChatColor _failureColour = ChatColor.DARK_RED;
 
   /**
    * Reference to the Doppelganger plugin.
    */
   protected Doppelganger           _plugin;
 
   /**
    * Handles help messages.
    */
   protected Help                   _help;
 } // class Commands
