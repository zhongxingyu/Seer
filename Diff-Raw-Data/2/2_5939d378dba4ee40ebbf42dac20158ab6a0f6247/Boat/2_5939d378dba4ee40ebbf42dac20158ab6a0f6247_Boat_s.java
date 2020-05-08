 package org.darkquest.gs.plugins.npcs;
 
 import org.darkquest.gs.event.ShortEvent;
 import org.darkquest.gs.model.ChatMessage;
 import org.darkquest.gs.model.MenuHandler;
 import org.darkquest.gs.model.Npc;
 import org.darkquest.gs.model.Player;
 import org.darkquest.gs.model.Point;
 import org.darkquest.gs.plugins.listeners.action.TalkToNpcListener;
 import org.darkquest.gs.plugins.listeners.executive.TalkToNpcExecutiveListener;
 import org.darkquest.gs.tools.DataConversions;
 import org.darkquest.gs.world.World;
 
 public final class Boat implements TalkToNpcListener, TalkToNpcExecutiveListener {
 	/**
 	 * World instance
 	 */
 	public static final World world = World.getWorld();
 	
 	/*
 	private static final String[] destinationNames = {
 		"Karamja", "Brimhaven", "Port Sarim", "Ardougne",
 		"Port Khazard", "Catherby", "Shilo"
 	}; */
 	
 	private static final String[] destinationNames = {
 		"Yes please", "No thanks"
 	};
 	
 	private static final Point[] destinationCoords = {
 		Point.location(324, 713), Point.location(467, 649), Point.location(268, 650), Point.location(538, 616),
 		Point.location(541, 702), Point.location(439, 506), Point.location(471, 853)
 	};
 	
 
 	int[] boatMen = new int[]{ 166, 170, 171, 163, 317, 316, 280, 764 };
 
 	@Override
 	public void onTalkToNpc(Player player, final Npc npc) {
 		if(!DataConversions.inArray(boatMen, npc.getID())) {
 			return;
 		}
 		if(npc.getID() == 163) {
 			player.informOfNpcMessage(new ChatMessage(npc, "G'day sailor, would you like a free trip to Port Sarim", player));
 			player.setBusy(true);
 			world.getDelayedEventHandler().add(new ShortEvent(player) {
 				public void action() {
 					owner.setBusy(false);
 					owner.setMenuHandler(new MenuHandler(destinationNames) {
 						public void handleReply(final int option, final String reply) {
 							if(owner.isBusy() || option < 0 || option >= destinationNames.length) {
 								npc.unblock();
 								return;
 							}
 
 							owner.informOfChatMessage(new ChatMessage(owner, reply, npc));
 							if(option == 1) {
 								owner.setBusy(false);
 								npc.unblock();
 								return;
 							}
 							owner.setBusy(true);
 							world.getDelayedEventHandler().add(new ShortEvent(owner) {
 							public void action() {
 								owner.getActionSender().sendMessage("You board the ship");
 								world.getDelayedEventHandler().add(new ShortEvent(owner) {
 									public void action() {
 										Point p = Point.location(268, 650); // port sarim
 										owner.teleport(p.getX(), p.getY(), false);
 										owner.getActionSender().sendMessage("The ship arrives at Port Sarim");
 										owner.setBusy(false);
 										npc.unblock();
 									}
 								});
 							}
 						});
 					}
 				});
 				owner.getActionSender().sendMenu(destinationNames);
 			}
 		});
 		} else {
 			player.informOfNpcMessage(new ChatMessage(npc, "G'day sailor, would you like a free trip to Karamja", player));
 			player.setBusy(true);
 			world.getDelayedEventHandler().add(new ShortEvent(player) {
 				public void action() {
 					owner.setBusy(false);
 					owner.setMenuHandler(new MenuHandler(destinationNames) {
 						public void handleReply(final int option, final String reply) {
 							if(owner.isBusy() || option < 0 || option >= destinationNames.length) {
 								npc.unblock();
 								return;
 							}
 
 							owner.informOfChatMessage(new ChatMessage(owner, reply, npc));
 							if(option == 1) {
 								owner.setBusy(false);
 								npc.unblock();
 								return;
 							}
 							owner.setBusy(true);
 							world.getDelayedEventHandler().add(new ShortEvent(owner) {
 							public void action() {
 								owner.getActionSender().sendMessage("You board the ship");
 								world.getDelayedEventHandler().add(new ShortEvent(owner) {
 									public void action() {
 										Point p = Point.location(324, 713); // karamja
 										owner.teleport(p.getX(), p.getY(), false);
										owner.getActionSender().sendMessage("The ship arrives at Karamja" + reply);
 										owner.setBusy(false);
 										npc.unblock();
 									}
 								});
 							}
 						});
 					}
 				});
 				owner.getActionSender().sendMenu(destinationNames);
 			}
 		});
 		}
 		npc.blockedBy(player);
 	}
 
 	@Override
 	public boolean blockTalkToNpc(Player p, Npc n) {
 		return DataConversions.inArray(boatMen, n.getID()); 
 	}
 }
