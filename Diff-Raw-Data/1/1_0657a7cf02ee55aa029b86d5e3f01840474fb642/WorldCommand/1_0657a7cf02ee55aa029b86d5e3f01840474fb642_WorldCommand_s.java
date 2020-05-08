 /*
  * Copyright 2011 frdfsnlght <frdfsnlght@gmail.com>.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.bennedum.transporter.command;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import org.bennedum.transporter.Context;
 import org.bennedum.transporter.Global;
 import org.bennedum.transporter.TransporterException;
 import org.bennedum.transporter.Utils;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.command.Command;
 
 /**
  *
  * @author frdfsnlght <frdfsnlght@gmail.com>
  */
 public class WorldCommand extends TrpCommandProcessor {
 
     @Override
     protected String[] getSubCommands() { return new String[] {"world"}; }
 
     @Override
     public String getUsage(Context ctx) {
         return
                 super.getUsage(ctx) + " list\n" +
                 super.getUsage(ctx) + " create <world> [<env>] [<seed>]\n" +
                 super.getUsage(ctx) + " load <world>\n" +
                 super.getUsage(ctx) + " unload <world>\n" +
                 super.getUsage(ctx) + " go [<coords>] [<world>]\n" +
                 super.getUsage(ctx) + " spawn [<coords>] [<world>]";
     }
 
     @Override
     public void process(final Context ctx, Command cmd, List<String> args) throws TransporterException {
         super.process(ctx, cmd, args);
         if (args.isEmpty())
             throw new CommandException("do what with a world?");
         String subCmd = args.remove(0).toLowerCase();
 
         if ("list".startsWith(subCmd)) {
             ctx.requireAllPermissions("trp.world.list");
             List<World> worlds = new ArrayList<World>(Global.plugin.getServer().getWorlds());
             Collections.sort(worlds, new Comparator<World>() {
                 @Override
                 public int compare(World a, World b) {
                     return a.getName().compareToIgnoreCase(b.getName());
                 }
             });
             ctx.send("%d worlds:", worlds.size());
             for (World world : worlds)
                 ctx.send("  %s (%s)", world.getName(), world.getEnvironment());
             return;
         }
 
         if ("create".startsWith(subCmd)) {
             if (args.isEmpty())
                 throw new CommandException("new name required");
             final String newName = args.remove(0);
             Environment env = Environment.NORMAL;
             Long seed = null;
             if (! args.isEmpty()) {
                 String arg = args.remove(0);
                 if (arg.matches("^\\d+$"))
                     try {
                         seed = Long.parseLong(arg);
                     } catch (NumberFormatException e) {
                         throw new CommandException("illegal seed value");
                     }
                 else
                     try {
                         env = Environment.valueOf(arg.toUpperCase());
                     } catch (IllegalArgumentException e) {
                         throw new CommandException("unknown environment");
                     }
             }
             ctx.requireAllPermissions("trp.world.create");
 
             ctx.sendLog("creating world '%s'...", newName);
             final Environment fEnv = env;
             final Long fSeed = seed;
             Utils.worker(new Runnable() {
                 @Override
                 public void run() {
                     if (fSeed == null)
                         Global.plugin.getServer().createWorld(newName, fEnv);
                     else
                         Global.plugin.getServer().createWorld(newName, fEnv, fSeed);
                     Utils.fire(new Runnable() {
                         @Override
                         public void run() {
                             ctx.sendLog("created world '%s'", newName);
                         }
                     });
                 }
             });
             return;
         }
 
         if ("load".startsWith(subCmd)) {
             if (args.isEmpty())
                 throw new CommandException("world name required");
             final String name = args.remove(0);
 
             ctx.requireAllPermissions("trp.world.load");
 
             if (Global.plugin.getServer().getWorld(name) != null)
                 throw new CommandException("world '%s' is already loaded", name);
             File worldFolder = Utils.worldFolder(name);
             if (! worldFolder.isDirectory())
                 throw new CommandException("world '%s' doesn't exist", name);
             ctx.sendLog("loading world '%s'...", name);
             Utils.worker(new Runnable() {
                 @Override
                 public void run() {
                     final World world = Global.plugin.getServer().createWorld(name, Environment.NORMAL);
                     Utils.fire(new Runnable() {
                         @Override
                         public void run() {
                             ctx.sendLog("loaded world '%s'", world.getName());
                         }
                     });
                 }
             });
             return;
         }
 
         if ("unload".startsWith(subCmd)) {
             if (args.isEmpty())
                 throw new CommandException("world name required");
             String name = args.remove(0);
 
             ctx.requireAllPermissions("trp.world.unload");
 
             final World world = Utils.getWorld(name);
             if (world == null)
                 throw new CommandException("world '%s' is ambiguous or not loaded", name);
             Utils.worker(new Runnable() {
                 @Override
                 public void run() {
                     Global.plugin.getServer().unloadWorld(world, true);
                     Utils.fire(new Runnable() {
                         @Override
                         public void run() {
                             ctx.sendLog("unloaded world '%s'", world.getName());
                             // TODO: remove this once bukkit can send onWorldUnload events
                             Global.gates.remove(world);
                         }
                     });
                 }
             });
             return;
         }
 
         if ("go".startsWith(subCmd)) {
             if (! ctx.isPlayer())
                 throw new CommandException("must be a player to use this command");
 
             World world = ctx.getPlayer().getWorld();
             Location location = world.getSpawnLocation();
             String locationString = null;
 
             if ((! args.isEmpty()) && (args.get(0).indexOf(',') != -1))
                 locationString = args.remove(0);
             if (! args.isEmpty()) {
                 String name = args.remove(0);
                 world = Utils.getWorld(name);
                 if (world == null)
                     throw new CommandException("world '%s' is ambiguous or not loaded", name);
             }
 
             if (locationString != null) {
                 String ordStrings[] = locationString.split(",");
                 double ords[] = new double[ordStrings.length];
                 for (int i = 0; i < ordStrings.length; i++)
                     try {
                         ords[i] = Double.parseDouble(ordStrings[i]);
                     } catch (NumberFormatException e) {
                         throw new CommandException("invalid ordinate '%s'", ordStrings[i]);
                     }
                 if (ords.length == 2) {
                     // given x,z, so figure out sensible y
                     int y = world.getHighestBlockYAt((int)ords[0], (int)ords[1]) + 1;
                     while (y > 1) {
                         if ((world.getBlockTypeIdAt((int)ords[0], y, (int)ords[1]) == 0) &&
                             (world.getBlockTypeIdAt((int)ords[0], y, (int)ords[1]) == 0)) break;
                         y--;
                     }
                     if (y == 1)
                         throw new CommandException("unable to locate a space big enough for you");
                     location = new Location(world, ords[0], y, ords[1]);
                 } else if (ords.length == 3)
                     location = new Location(world, ords[0], ords[1], ords[2]);
                 else
                     throw new CommandException("expected 2 or 3 ordinates");
             }
 
             ctx.requireAllPermissions("trp.world.go");
 
             ctx.getPlayer().teleport(location);
             ctx.sendLog("teleported to world '%s'", world.getName());
 
             return;
         }
 
         if ("spawn".startsWith(subCmd)) {
             World world = ctx.isPlayer() ? ctx.getPlayer().getWorld() : null;
             Location location = ctx.isPlayer() ? ctx.getPlayer().getLocation() : null;
             String locationString = null;
 
             if ((! args.isEmpty()) && (args.get(0).indexOf(',') != -1))
                 locationString = args.remove(0);
             if (! args.isEmpty()) {
                 String name = args.remove(0);
                 world = Utils.getWorld(name);
                 if (world == null)
                     throw new CommandException("world '%s' is ambiguous or not loaded", name);
             }
 
             if ((world != null) && (locationString != null)) {
                 String ordStrings[] = locationString.split(",");
                 double ords[] = new double[ordStrings.length];
                 for (int i = 0; i < ordStrings.length; i++)
                     try {
                         ords[i] = Double.parseDouble(ordStrings[i]);
                     } catch (NumberFormatException e) {
                         throw new CommandException("invalid ordinate '%s'", ordStrings[i]);
                     }
                 if (ords.length == 2) {
                     // given x,z, so figure out sensible y
                     int y = world.getHighestBlockYAt((int)ords[0], (int)ords[1]) + 1;
                     while (y > 1) {
                         if ((world.getBlockTypeIdAt((int)ords[0], y, (int)ords[1]) == 0) &&
                             (world.getBlockTypeIdAt((int)ords[0], y, (int)ords[1]) == 0)) break;
                         y--;
                     }
                     if (y == 1)
                         throw new CommandException("unable to locate a space big enough for a player");
                     location = new Location(world, ords[0], y, ords[1]);
                 } else if (ords.length == 3)
                     location = new Location(world, ords[0], ords[1], ords[2]);
                 else
                     throw new CommandException("expected 2 or 3 ordinates");
             }
             if ((world != null) && (location == null))
                 throw new CommandException("location required");
             if (world == null)
                 throw new CommandException("world name required");
 
             ctx.requireAllPermissions("trp.world.spawn");
 
             world.setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ());
             ctx.sendLog("set spawn location for world '%s'", world.getName());
 
             return;
         }
 
         throw new CommandException("do what with a world?");
     }
 
 }
