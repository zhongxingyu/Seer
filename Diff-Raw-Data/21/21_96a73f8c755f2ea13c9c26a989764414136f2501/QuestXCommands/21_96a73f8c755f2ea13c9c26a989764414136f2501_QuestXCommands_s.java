 package com.adamki11s.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.adamki11s.ai.RandomMovement;
 import com.adamki11s.ai.dataset.Reputation;
 import com.adamki11s.data.ItemStackDrop;
 import com.adamki11s.data.ItemStackProbability;
 import com.adamki11s.npcs.NPCHandler;
 import com.adamki11s.npcs.SimpleNPC;
 import com.adamki11s.npcs.UniqueNameRegister;
 import com.adamki11s.npcs.io.CreateNPC;
 import com.adamki11s.npcs.loading.FixedLoadingTable;
 import com.adamki11s.npcs.tasks.Fireworks;
 import com.adamki11s.questx.QuestX;
 import com.topcat.npclib.entity.HumanNPC;
 
 public class QuestXCommands implements CommandExecutor {
 
 	QuestX plugin;
 	NPCHandler handle;
 
 	public QuestXCommands(QuestX main) {
 		this.plugin = main;
 		this.handle = main.getNPCHandler();
 	}
 
 	HumanNPC test;
 	RandomMovement rm;
 
 	Reputation r = new Reputation("Adamki11s", 100);
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (label.equalsIgnoreCase("questx")) {
 			if (!(sender instanceof Player)) {
 				System.out.println("QuestX Commands must be issued in-game.");
 				return true;
 			} else {
 				Player p = (Player) sender;
 
 				ItemStack[] gear = new ItemStack[] { null, null, null, null, new ItemStack(Material.WOOD_AXE) };
 
 				if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
 					if (UniqueNameRegister.isNameUnique(args[1])) {
 						CreateNPC create = new CreateNPC(args[1], ChatColor.RED);
 
 						// Format id:data:quantity:chance(out of 10,000)/
 						String invDrops = "1,0,5,6000#2,0,3,3000", gr = "0,0,0,0,0";
 						create.setProperties(true, true, false, true, (20 * 5), (20 * 20), 15, (20 * 30), 30, 2, 2, invDrops, gr);
 						create.createNPCFiles();
 					} else {
 						p.sendMessage("NPC with this name already exists");
 					}
 					return true;
 				}
 
 				if (args.length == 2 && args[0].equalsIgnoreCase("find")) {
 					String npcName = args[1];
 					SimpleNPC npc = this.handle.getSimpleNPCByName(npcName);
 					if (npc == null) {
 						p.sendMessage("NPC with this name is not spawned");
 						return true;
 					} else {
 						Fireworks f = new Fireworks(npc.getHumanNPC().getBukkitEntity().getLocation(), 6, 20);
 						f.fireLocatorBeacons();
 						p.sendMessage("Launching locator beacons!");
 						return true;
 					}
 				}
 
 				if (args.length == 2 && args[0].equalsIgnoreCase("tele")) {
 					String npcName = args[1];
 					SimpleNPC npc = this.handle.getSimpleNPCByName(npcName);
 					if (npc == null) {
 						p.sendMessage("NPC with this name is not spawned");
 						return true;
 					} else {
 						p.teleport(npc.getHumanNPC().getBukkitEntity().getLocation());
 						return true;
 					}
 				}
 
 				if (args.length == 2 && args[0].equalsIgnoreCase("setfixedspawn")) {
 					String npcName = args[1];
					FixedLoadingTable.addFixedNPCSpawn(p, npcName, p.getLocation());
					this.handle.getSimpleNPCByName(npcName).spawnNPC();
 					return true;
 				}
 
 				if (args.length == 2 && args[0].equalsIgnoreCase("stressspawn")) {
 					int max = Integer.parseInt(args[1]);
 					for (int i = 0; i < max; i++) { // 1/10 chance of dropping
 						SimpleNPC snpc = new SimpleNPC(this.handle, ("a" + i), ChatColor.BLUE, true, true, false, 60, 200, 20, 100, 200, new ItemStackDrop(
 								new ItemStackProbability[] { new ItemStackProbability(new ItemStack(Material.GOLD_AXE, 1), 6000) }), gear, 1, 1.5);
 						snpc.spawnNPC();
 						// p.sendMessage("NPC Spawned!");
 					}
 					return true;
 				}
 
 				if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
 					String npcName = args[1];
 					if (!UniqueNameRegister.isNameUnique(npcName)) {
 						p.sendMessage(ChatColor.RED + "Name is not unique!");
 						return true;
 					} else {
 						SimpleNPC snpc = new SimpleNPC(this.handle, npcName, ChatColor.BLUE, true, true, false, 60, 200, 10, 100, 200, new ItemStackDrop(
 								new ItemStackProbability[] { new ItemStackProbability(new ItemStack(Material.GOLD_AXE, 1), 6000) }), gear, 1, 1.5);
 						snpc.spawnNPC();
 
 						p.sendMessage("NPC Spawned!");
 						return true;
 					}
 
 				}
 			}
 		}
 		return true;
 	}
 }
