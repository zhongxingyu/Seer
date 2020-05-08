 package com.theminequest.bukkit.frontend.inventory;
 
 import static com.theminequest.common.util.I18NMessage._;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 import org.bukkit.plugin.Plugin;
 
 import com.theminequest.api.ManagerException;
 import com.theminequest.api.Managers;
 import com.theminequest.api.group.Group;
 import com.theminequest.api.group.GroupException;
 import com.theminequest.api.platform.MQPlayer;
 import com.theminequest.bukkit.frontend.cmd.PartyCommandFrontend;
 import com.theminequest.bukkit.frontend.inventory.IconMenu.OptionClickEvent;
 import com.theminequest.bukkit.frontend.inventory.TemporaryIconMenu.Action;
 
 public class MineQuestMenu {
 	
 	private static final IconMenu MENU = new IconMenu(ChatColor.RESET + "MineQuest v" + Managers.getVersion(), 9, new IconMenu.OptionClickEventHandler() {
 		@Override
 		public void onOptionClick(final IconMenu.OptionClickEvent event) {
 			event.setWillClose(false);
 			
 			switch (event.getPosition()) {
 			case 3:
 				partyMenu(event.getPlayer());
 				break;
 			case 5:
 				questMenu(event.getPlayer());
 				break;
 			}
 			
 		}
 	}, (Plugin) Managers.getPlatform().getPlatformObject()).setOption(3, new MaterialData(Material.SKULL_ITEM, (byte) 3).toItemStack(1), ChatColor.RESET + "" + ChatColor.AQUA + _("Party"), ChatColor.RESET + _("Play with others!")).setOption(5, new ItemStack(Material.IRON_SWORD, 1), ChatColor.RESET + "" + ChatColor.GREEN + _("Quest"), ChatColor.RESET + _("Manage your adventure!"));
 	
 	public static void showMenu(Player sender) {
 		MENU.open(sender);
 	}
 	
 	private static void partyMenu(final Player pl) {
 		
 		final MQPlayer p = Managers.getPlatform().toPlayer(pl);
 		
 		Group g = null;
 		boolean invite = Managers.getGroupManager().hasInvite(p);
 		boolean isLeader = false;
 		if (Managers.getGroupManager().indexOf(p) != -1) {
 			g = Managers.getGroupManager().get(p);
 			isLeader = g.getLeader().equals(p);
 		}
 		
 		TemporaryIconMenu partyMenu = new TemporaryIconMenu(_("Party Management"), 18, (Plugin) Managers.getPlatform().getPlatformObject());
 		
 		/*
 		 * accept
 		 * create
 		 * 
 		 * info
 		 * invite
 		 * kick
 		 * leave
 		 * list
 		 * promote
 		 */
 		
 		if (g == null) {
 			int createLoc = 4;
 			if (invite) {
 				createLoc = 5;
 				partyMenu.setOption(3, Material.POTION.getNewData((byte) 8260).toItemStack(1), new Action() {
 					
 					@Override
 					public void run(OptionClickEvent event) {
 						
 						if (Managers.getGroupManager().indexOf(p) != -1) {
 							p.sendMessage(PartyCommandFrontend.IINPARTY);
 							return;
 						}
 						if (!Managers.getGroupManager().hasInvite(p)) {
 							p.sendMessage(PartyCommandFrontend.INOINVITE);
 							return;
 						}
 						try {
 							Managers.getGroupManager().acceptInvite(p);
 							p.sendMessage(PartyCommandFrontend.IACCEPT);
 						} catch (ManagerException e) {
 							throw new RuntimeException(e); // toss this to
 															// CommandFrontend
 						}
 					}
 					
 				}, ChatColor.RESET + "" + ChatColor.BOLD + ChatColor.GREEN + _("Accept Invite"), ChatColor.RESET + _("Accept invite to party."));
 			}
 			
 			partyMenu.setOption(createLoc, new ItemStack(Material.BOOK_AND_QUILL, 1), new Action() {
 				
 				@Override
 				public void run(OptionClickEvent event) {
 					
 					if (Managers.getGroupManager().indexOf(p) != -1) {
 						p.sendMessage(PartyCommandFrontend.IINPARTY);
 						return;
 					}
 					if (Managers.getGroupManager().hasInvite(p))
 						p.sendMessage(PartyCommandFrontend.IDISCARD);
 					Managers.getGroupManager().createNewGroup(p);
 					p.sendMessage(PartyCommandFrontend.ICREATE);
 				}
 				
 			}, ChatColor.RESET + "" + ChatColor.GOLD + _("Create Party"), ChatColor.RESET + _("Create a new party."));
 		} else {
 			// Top row, do individual players
 			List<MQPlayer> members = g.getMembers();
 			
 			for (int i = 0; i < members.size(); i++) {
 				final MQPlayer mqplayer = members.get(i);
 				Action act = null;
 				
 				if (isLeader) {
 					act = new Action() {
 						
 						@Override
 						public void run(OptionClickEvent event) {
 							event.setWillClose(false);
 							String header = String.format("%s: %s/%s", mqplayer.getDisplayName(), mqplayer.getHealth(), mqplayer.getMaxHealth());
 							
 							final TemporaryIconMenu playerMenu = new TemporaryIconMenu(header, 9, (Plugin) Managers.getPlatform().getPlatformObject());
 							playerMenu.setOption(0, new ItemStack(Material.ARROW, 1), new Action() {
 								
 								@Override
 								public void run(OptionClickEvent event) {
 									event.setWillClose(false);
 									partyMenu(event.getPlayer());
 								}
 								
							}, ChatColor.RESET + "" + ChatColor.AQUA + _("Back"), ChatColor.RESET + _("Back to the previous menu."));
 							
 							playerMenu.setOption(4, new ItemStack(Material.LAVA_BUCKET, 1), new Action() {
 								
 								@Override
 								public void run(OptionClickEvent event) {
 									
 									if (Managers.getGroupManager().indexOf(p) == -1) {
 										p.sendMessage(PartyCommandFrontend.IOUTPARTY);
 										return;
 									}
 									
 									Group g = Managers.getGroupManager().get(p);
 									if (!g.getLeader().equals(p)) {
 										p.sendMessage(PartyCommandFrontend.INOTLEADER);
 										return;
 									}
 									
 									if (!g.contains(mqplayer)) {
 										p.sendMessage(PartyCommandFrontend.ITARGETOUTPARTY);
 										return;
 									}
 									
 									try {
 										g.remove(mqplayer);
 										p.sendMessage(PartyCommandFrontend.IKICKED.replace("%n%", mqplayer.getDisplayName()));
 										mqplayer.sendMessage(PartyCommandFrontend.ITARGETKICKED.replace("%n%", p.getDisplayName()));
 									} catch (GroupException e) {
 										throw new RuntimeException(e); // toss
 																		// to
 																		// CommandFrontend
 									}
 									
 								}
 								
							}, ChatColor.RESET + "" + ChatColor.RED + _("Kick"), ChatColor.RESET + _("Kick {0} from the party.", mqplayer.getDisplayName()));
 							
 							playerMenu.setOption(8, new ItemStack(Material.NETHER_STAR, 1), new Action() {
 								
 								@Override
 								public void run(OptionClickEvent event) {
 									
 									if (Managers.getGroupManager().indexOf(p) == -1) {
 										p.sendMessage(PartyCommandFrontend.IOUTPARTY);
 										return;
 									}
 									
 									Group g = Managers.getGroupManager().get(p);
 									if (!g.getLeader().equals(p)) {
 										p.sendMessage(PartyCommandFrontend.INOTLEADER);
 										return;
 									}
 									
 									if (!g.contains(mqplayer)) {
 										p.sendMessage(PartyCommandFrontend.ITARGETOUTPARTY);
 										return;
 									}
 									
 									try {
 										g.setLeader(mqplayer);
 										for (MQPlayer member : g.getMembers())
 											member.sendMessage(PartyCommandFrontend.IPROMOTE);
 									} catch (GroupException e) {
 										throw new RuntimeException(e); // throw
 																		// to
 																		// CommandFrontend
 									}
 								}
 								
							}, ChatColor.RESET + "" + ChatColor.GOLD + _("Promote"), ChatColor.RESET + _("Promote {0} as the leader of the party.", mqplayer.getDisplayName()), _("You will no longer be leader."));
 							
 							Managers.getPlatform().scheduleSyncTask(new Runnable() {
 								
 								@Override
 								public void run() {
 									playerMenu.open(Bukkit.getPlayerExact(pl.getName()));
 								}
 								
 							});
 						}
 						
 					};
 				}
 				
 				if (i == 0)
 					partyMenu.setOption(0, new ItemStack(Material.GOLD_CHESTPLATE, 1), act, ChatColor.RESET + "" + ChatColor.GOLD + mqplayer.getDisplayName(), ChatColor.RESET + _("Health: {0}/{1}", mqplayer.getHealth(), mqplayer.getMaxHealth()));
 				else
 					partyMenu.setOption(i + 1, new ItemStack(Material.IRON_CHESTPLATE, 1), act, ChatColor.RESET + "" + ChatColor.AQUA + mqplayer.getDisplayName(), ChatColor.RESET + _("Health: {0}/{1}", mqplayer.getHealth(), mqplayer.getMaxHealth()));
 			}
 			
 			// Bottom row, handle leaving the party or inviting
 			int leaveLoc = 13;
 			if (isLeader) {
 				leaveLoc = 12;
 				partyMenu.setOption(14, new ItemStack(Material.PAPER, 1), new Action() {
 					
 					@Override
 					public void run(OptionClickEvent event) {
 						event.getPlayer().sendMessage(_("You will need to do /party invite [player]."));
 					}
 					
 				}, ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + _("Invite"), ChatColor.RESET + _("Invite a player to your party."));
 			}
 			partyMenu.setOption(leaveLoc, new ItemStack(Material.IRON_DOOR, 1), new Action() {
 				
 				@Override
 				public void run(OptionClickEvent event) {
 					if (Managers.getGroupManager().indexOf(p) == -1) {
 						p.sendMessage(PartyCommandFrontend.IOUTPARTY);
 						return;
 					}
 					Group g = Managers.getGroupManager().get(p);
 					boolean leader = g.getLeader().equals(p);
 					
 					try {
 						if (leader && (g.getMembers().size() != 1))
 							g.setLeader(g.getMembers().get(1));
 						g.remove(p);
 						p.sendMessage(PartyCommandFrontend.ILEAVE);
 					} catch (GroupException e) {
 						throw new RuntimeException(e); // toss to
 														// CommandFrontend
 					}
 				}
 				
 			}, ChatColor.RESET + "" + ChatColor.RED + _("Leave"), ChatColor.RESET +_("Leave the party."));
 			
 		}
 		
 		partyMenu.open(pl);
 	}
 	
 	private static void questMenu(Player p) {
 		p.sendMessage("Not implemented yet! Use /quest!");
 	}
 	
 }
