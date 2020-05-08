 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of MineStarWarp.
  * 
  * MineStarWarp is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * MineStarWarp is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with MineStarWarp.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.minestar.MineStarWarp.commands;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.minestar.MineStarWarp.Main;
 import com.minestar.MineStarWarp.commands.back.BackCommand;
 import com.minestar.MineStarWarp.commands.bank.BankCommand;
 import com.minestar.MineStarWarp.commands.bank.BankListCommand;
 import com.minestar.MineStarWarp.commands.bank.SetBankCommand;
 import com.minestar.MineStarWarp.commands.home.HomeCommand;
 import com.minestar.MineStarWarp.commands.home.SetHomeCommand;
 import com.minestar.MineStarWarp.commands.spawn.SetSpawnCommand;
 import com.minestar.MineStarWarp.commands.spawn.SpawnCommand;
 import com.minestar.MineStarWarp.commands.teleport.TeleportHereCommand;
 import com.minestar.MineStarWarp.commands.teleport.TeleportToCommand;
 import com.minestar.MineStarWarp.commands.warp.CreateCommand;
 import com.minestar.MineStarWarp.commands.warp.DeleteCommand;
 import com.minestar.MineStarWarp.commands.warp.GuestListCommand;
 import com.minestar.MineStarWarp.commands.warp.InviteCommand;
 import com.minestar.MineStarWarp.commands.warp.ListCommand;
 import com.minestar.MineStarWarp.commands.warp.MoveCommand;
 import com.minestar.MineStarWarp.commands.warp.PrivateCommand;
 import com.minestar.MineStarWarp.commands.warp.PublicCommand;
 import com.minestar.MineStarWarp.commands.warp.RenameCommand;
 import com.minestar.MineStarWarp.commands.warp.SearchCommand;
 import com.minestar.MineStarWarp.commands.warp.UninviteCommand;
 import com.minestar.MineStarWarp.commands.warp.WarpToCommand;
 
 public class CommandList {
 
     // The commands are stored in this list. The key indicates the
     // commandssyntax and the argument counter
     private HashMap<String, Command> commandList;
 
     /**
      * Creates an array where the commands are stored in and add them all to the
      * HashMap
      * 
      * @param server
      */
     public CommandList(Server server) {
 
         // Add an command to this list to register it in the plugin
         Command[] commands = new Command[] {
                 // Teleport Commands
                 new TeleportHereCommand("/tphere", "<Player>", "tphere", server),
                 new TeleportToCommand("/tp", "<Player>", "tpTo", server),
 
                 // Home
                 new SetHomeCommand("/sethome", "", "sethome", server),
                 new HomeCommand("/home", "", "home", server),
 
                 // Spawn
                 new SpawnCommand("/spawn", "", "spawn", server),
                 new SpawnCommand("/spawn", "<Worldname>", "spawnSpecific",
                         server),
                 new SetSpawnCommand("/setspawn", "", "setSpawn", server),
 
                 // Bank
                 new BankCommand("/bank", "", "bank", server,
                         new BankListCommand("list", "", "bankList", server)),
                 new SetBankCommand("/setbank", "<Player>", "setBank", server),
 
                 // Back
                 new BackCommand("/back", "", "back", server),
 
                 // Warp Command
                 new WarpToCommand(
                         "/warp",
                         "<Name>",
                         "warpTo",
                         server,
 
                         new Command[] {
                                 // Warp Creation, Removing, Moving and
                                 // Renameing.
                                 new CreateCommand("create", "<Name>", "create",
                                         server),
                                 new CreateCommand("pcreate", "<Name>",
                                         "create", server),
                                 new DeleteCommand("delete", "<Name>", "delete",
                                         server),
                                new MoveCommand("move", "<Name>", ",move",
                                         server),
                                 new RenameCommand("rename",
                                         "<Oldname> <Newname>", "rename", server),
 
                                 // Searching Warps
                                 new ListCommand("list", "", "list", server),
                                 new SearchCommand("search", "<Name>", "search",
                                         server),
 
                                 // Modifiers
                                 new PrivateCommand("private", "<Name>",
                                         "private", server),
                                 new PublicCommand("public", "<Name>", "public",
                                         server),
 
                                 // Guests
                                 new InviteCommand("invite",
                                         "<PlayerName> <Warpname>", "invite",
                                         server),
                                 new UninviteCommand("uninvite",
                                         "<PlayerName> <Warpname>", "uninvite",
                                         server),
                                 new GuestListCommand("guestlist", "<WarpName>",
                                         "guestlist", server) }) };
 
         // store the commands in the hash map
         initCommandList(commands);
     }
 
     public void handleCommand(CommandSender sender, String label, String[] args) {
 
         if (!(sender instanceof Player))
             return;
 
         Player player = (Player) sender;
 
         if (!label.startsWith("/"))
             label = "/" + label;
 
         // looking for
         Command cmd = commandList.get(label + "_" + args.length);
         if (cmd != null)
             cmd.run(args, player);
         else {
             cmd = commandList.get(label);
             if (cmd != null)
                 cmd.run(args, player);
             else {
                 player.sendMessage(ChatColor.RED
                         + Main.localization.get("commandList.wrongSyntax",
                                 label));
 
                 // FIND RELATED COMMANDS
                 LinkedList<Command> cmdList = new LinkedList<Command>();
                 for (Entry<String, Command> entry : commandList.entrySet()) {
                     if (entry.getKey().startsWith(label))
                         cmdList.add(entry.getValue());
                 }
 
                 // PRINT SYNTAX
                 while (!cmdList.isEmpty()) {
                     cmd = cmdList.removeFirst();
                     player.sendMessage(ChatColor.GRAY + cmd.getSyntax() + " "
                             + cmd.getArguments());
                 }
             }
         }
     }
 
     /**
      * Stores the commands from the array to a HashMap. The key is generated by
      * the followning: <br>
      * <code>syntax_numberOfArguments</code> <br>
      * Example: /warp create_1 (because create has one argument)
      * 
      * @param cmds
      *            The array list for commands
      */
     private void initCommandList(Command[] cmds) {
 
         commandList = new HashMap<String, Command>(cmds.length, 1.0f);
         for (Command cmd : cmds) {
             String key = "";
             // when the command has a variable count of arguments or
             // when the command has a function and sub commands
             if (cmd instanceof ExtendedCommand || cmd instanceof SuperCommand)
                 key = cmd.getSyntax();
             // a normal command(no subcommands/fix argument count)
             else
                 key = cmd.getSyntax() + "_"
                         + (cmd.getArguments().split("<").length - 1);
 
             commandList.put(key, cmd);
         }
     }
 }
