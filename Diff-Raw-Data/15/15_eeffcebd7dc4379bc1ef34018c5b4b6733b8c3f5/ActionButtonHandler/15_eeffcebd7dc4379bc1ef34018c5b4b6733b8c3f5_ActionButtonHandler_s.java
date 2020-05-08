 package org.dementhium.net.packethandlers;
 
 import java.text.NumberFormat;
 import java.util.Currency;
 
 import org.dementhium.content.DialogueManager;
 import org.dementhium.content.activity.impl.DuelActivity;
 import org.dementhium.content.activity.impl.duel.DuelConfigurations.Rules;
 import org.dementhium.content.clans.ClanChatUtils;
 import org.dementhium.content.interfaces.Emotes;
 import org.dementhium.content.skills.magic.TeleportHandler;
 import org.dementhium.event.EventManager;
 import org.dementhium.model.Item;
 import org.dementhium.model.SpecialAttack;
 import org.dementhium.model.SpecialAttackContainer;
 import org.dementhium.model.World;
 import org.dementhium.model.combat.impl.specs.QuickSmash;
 import org.dementhium.model.definition.ItemDefinition;
 import org.dementhium.model.mask.Animation;
 import org.dementhium.model.player.Bank;
 import org.dementhium.model.player.Equipment;
 import org.dementhium.model.player.Player;
 import org.dementhium.model.player.Shop;
 import org.dementhium.model.player.Skills;
 import org.dementhium.net.ActionSender;
 import org.dementhium.net.PacketHandler;
 import org.dementhium.net.message.Message;
 import org.dementhium.tickable.Tick;
 import org.dementhium.tickable.impl.WGuildTick;
 import org.dementhium.util.InputHandler;
 import org.dementhium.util.InterfaceSettings;
 import org.dementhium.util.Misc;
 
 
 /**
  * @author 'Mystic Flow
  * @author `Discardedx2
  * @auhtor Steve
  */
 public class ActionButtonHandler extends PacketHandler {
 
 	private static final int[] BUTTON_PACKET_IDS = { 6, 13, 0, 15, 46, 67, 82,
 			39, 73 };
 	private EventManager eventManager = EventManager.getEventManager();
 
 	/*
 	 * case 6: bob.removeItem(slot, 1); break; case 13: bob.removeItem(slot, 5);
 	 * break; case 0: bob.removeItem(slot, 10); break; case 15:
 	 * bob.removeItem(slot, bob.numberOf(bob.getContainer().get(slot).getId()));
 	 * break; case 46: InputHandler.requestIntegerInput(player, 9,
 	 * "Please enter an amount:"); player.setAttribute("slotId", slot); break;
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.dementhium.net.PacketHandler#handlePacket(org.dementhium.model.player
 	 * .Player, org.dementhium.net.message.Message)
 	 */
 	@Override
 	public void handlePacket(Player player, Message packet) {
 		if (!player.hasStarter()) {
 			return;
 		}
 		try {
 			handleButtons(player, packet,
 					getMenuOptionIndex(packet.getOpcode()));
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 	}
 
 	private int getMenuOptionIndex(int opcode) {
 		for (int i = 0; i < BUTTON_PACKET_IDS.length; i++) {
 			if (BUTTON_PACKET_IDS[i] == opcode) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	private void handleButtons(final Player player, Message packet,
 			int menuIndex) {
 		int interfaceId = packet.readShort();
 		int buttonId = packet.readShort();
 		int slot = packet.readLEShortA();
 		int itemId = packet.readShort();
 		System.out.println("Packety "+interfaceId+","+buttonId+","+slot+","+itemId+","+packet.getOpcode());
 		if (slot == 65535) {
 			slot = -1;
 		}
 		System.out.println("interfaceId=" + interfaceId + " buttonId=" + buttonId + " slot=" + slot + " itemId=" + itemId + "PacketID" +packet.getOpcode());
 		if (eventManager.handleInterfaceOption(player, interfaceId, buttonId,
 				slot, itemId, packet.getOpcode())) {
 			return;
 		}
 		switch (interfaceId) {
 		case 672:
 		case 206:
 			if (buttonId == 13) {
 				player.getPriceCheck().close();
 			}
 			break;
 		case 411:
 			if (System.currentTimeMillis() - WGuildTick.getLastLaunch() < 3500)
 				player.setAttribute("shieldStyle", buttonId);
 			break;
 		case 652:
 			if (buttonId == 34) {
 				if (slot < 0) {
 					slot = 0;
 				}
 				player.setAttribute("graveSelection", slot);
 				player.setAttribute("gravePrice", -1);
 				if (slot == player.getSettings().getGraveStone()) {
 					player.sendMessage("You're already using this gravestone.");
 					return;
 				}
 				if (slot == 1) {
 					player.setAttribute("gravePrice", 50);
 				} else if (slot == 2) {
 					player.setAttribute("gravePrice", 500);
 				} else if (slot == 3) {
 					player.setAttribute("gravePrice", 5000);
 				} else if (slot > 3 && slot < 12) {
 					player.setAttribute("gravePrice", 50000);
 				} else if (slot > 11) {
 					player.setAttribute("gravePrice", 500000);
 				}
 				ActionSender.sendCloseInterface(player);
 				int price = player.getAttribute("gravePrice", -1);
 				if (price > 0) {
 					DialogueManager.sendDialogue(player,
 							DialogueManager.CALM_TALK, 456, 400,
 							"That's a fine selection,",
 							"the gods will be pleased with your choice.",
 							"Though they're still handcrafted so it'll cost",
 							"a small fee of " + price + " coins.");
 				} else {
 					player.getSettings().setGraveStone(slot);
 					DialogueManager.sendDialogue(player,
 							DialogueManager.CALM_TALK, 456, -1,
 							"That's a fine selection,",
 							"the gods will be pleased with your choice.");
 				}
 			}
 			break;
 		case 749:
 			switch (packet.getOpcode()) {
 			case 6:
 				player.getPrayer().switchQuickPrayers();
 				break;
 			case 13:
 				player.getPrayer().switchSettingQuickPrayer();
 				break;
 			}
 			break;
 		case 916:
 			switch (buttonId) {
 			case 19:
 				player.getSettings().increaseAmountToProduce();
 				break;
 			case 20:
 				player.getSettings().decreaseAmountToProduce();
 				break;
 			case 5:
 				player.getSettings().setAmountToProduce(1);
 				break;
 			case 6:
 				player.getSettings().setAmountToProduce(5);
 				break;
 			case 7:
 				player.getSettings().setAmountToProduce(10);
 				break;
 
 			}
 			break;
 		// case 1028:
 		// CharacterDesign.handleButton(player, buttonId, slot, itemId);
 		case 464:
 			Emotes.handleButton(player, buttonId, slot, itemId);
 			break;
 		case 763:
 			if (buttonId == 0) {
 				System.out.println("duh"+packet.getOpcode());
 				switch (packet.getOpcode()) {
 				case 6:
 					player.getBank().addItem(slot, 1);
 					break;
 				case 13:
 					player.getBank().addItem(slot, 5);
 					break;
 				case 0:
 					player.getBank().addItem(slot, 10);
 					break;
 				case 15:
 					player.getBank().addItem(slot,
 							player.getSettings().getLastXAmount());
 					break;
 				case 67:
 					Item item = player.getInventory().getContainer().get(slot);
 					player.getBank().addItem(
 							slot,
 							player.getInventory().getContainer()
 									.getNumberOf(item));// getContainer(slot).getAmount());
 					break;
 				case 46:
 					InputHandler.requestIntegerInput(player, 2,
 							"Please enter an amount:");
 					player.setAttribute("inputId", 4);
 					player.setAttribute("slotId", slot);
 					break;
 				case 58:
 					player.sendMessage(player.getInventory().getContainer()
 							.get(slot).getDefinition().getExamine());
 					break;
 				}
 			}
 			break;
 		case 762:
 			System.out.println("BAnk");
 			if (buttonId >= 44 && buttonId <= 62) {
 				player.setAttribute("currentTab", Bank.getArrayIndex(buttonId));
 			}
 			switch (buttonId) {
 			case 117:
 				player.setAttribute("fromBank", Boolean.TRUE);
 				ActionSender.sendInterfaceConfig(player, 667, 49, true);
 				ActionSender.sendInterfaceConfig(player, 667, 50, true);
 				player.getBonuses().refreshEquipScreen();
 				ActionSender.sendInterface(player, 667);
 				break;
 			case 33:
 				player.getBank().bankInv();
 				break;
 			case 35:
 				player.getBank().bankEquip();
 				break;
 			case 37:
 				player.getBank().bankBob();
 				break;
 			case 19:
 				player.setAttribute("noting", player.getAttribute("noting",
 						Boolean.FALSE) == Boolean.FALSE ? Boolean.TRUE
 						: Boolean.FALSE);
 				break;
 			case 15:
 				player.setAttribute(
 						"inserting",
 						player.getAttribute("inserting", Boolean.FALSE) == Boolean.FALSE ? Boolean.TRUE
 								: Boolean.FALSE);
 				break;
 			case 18:
 				if (player.getAttribute("inBank", Boolean.FALSE) == Boolean.TRUE) {
 					player.removeAttribute("inBank");
 				}
 				break;
 			case 62:
 			case 60:
 			case 58:
 			case 56:
 			case 54:
 			case 52:
 			case 50:
 			case 48:
 			case 46:
 				switch (packet.getOpcode()) {
 				case 6:
 					player.setAttribute("currentTab",
 							Bank.getArrayIndex(buttonId));
 					player.setAttribute("currentTabConfig",
 							Bank.getViewedTabConfig(buttonId));
 					break;
 				case 13:
 					player.getBank().collapseTab(Bank.getArrayIndex(buttonId));
 					player.getBank().refresh();
 					break;
 				}
 				break;
 
 			case 93:
 				switch (packet.getOpcode()) {
 				case 6:
 					player.getBank().removeItem(slot, 1);
 					break;
 				case 13:
 					player.getBank().removeItem(slot, 5);
 					break;
 				case 0:
 					player.getBank().removeItem(slot, 10);
 					break;
 				case 15:
 					player.getBank().removeItem(slot,
 							player.getSettings().getLastXAmount());
 					break;
 				case 67:
 					Item item = player.getBank().getContainer().get(slot);
 					player.getBank().removeItem(slot,
 							player.getBank().getContainer().getNumberOf(item));
 					break;
 				case 46:
 					InputHandler.requestIntegerInput(player, 2,
 							"Please enter an amount:");
 					player.setAttribute("inputId", 3);
 					player.setAttribute("slotId", slot);
 					break;
 				case 82:
 					Item item2 = player.getBank().getContainer().get(slot);
 					int itemAmt = player.getBank().getContainer()
 							.getNumberOf(item2);
 					player.getBank().removeItem(slot, itemAmt - 1);
 					break;
 				case 58:
 					player.sendMessage(player.getBank().getContainer()
 							.get(slot).getDefinition().getExamine());
 					break;
 				}
 				break;
 
 			default:
 				System.out.println(buttonId);
 			}
 			break;
 
 		case 665:
 			/*
 			 * if (player.getFamiliar() instanceof BeastOfBurden) {
 			 * BeastOfBurden bob = (BeastOfBurden) player.getFamiliar(); switch
 			 * (packet.getOpcode()) { case 6: bob.putItem(slot, 1); break; case
 			 * 13: bob.putItem(slot, 5); break; case 0: bob.putItem(slot, 10);
 			 * break; case 15: bob.putItem(slot,
 			 * player.getInventory().numberOf(player
 			 * .getInventory().get(slot).getId())); break; case 46:
 			 * InputHandler.requestIntegerInput(player, 8,
 			 * "Please enter an amount:"); player.setAttribute("slotId", slot);
 			 * break; } }
 			 */
 			break;
 		case 670:
 			/*
 			 * int equipSlot3 = Equipment.getItemType(itemId); //Item item3 =
 			 * player.getEquipment().get(equipSlot3); if(packet.getOpcode() ==
 			 * 6){ switch(buttonId){ case 0:
 			 * 
 			 * if (player.getActivity().getActivityId() == 0 && (equipSlot3 ==
 			 * Equipment.SLOT_CAPE || equipSlot3 == Equipment.SLOT_HAT)) {
 			 * player
 			 * .sendMessage("You can't equip a cape or hat in this activity.");
 			 * return; } if (itemId == 8856) { if
 			 * (!World.getWorld().getAreaManager
 			 * ().getAreaByName("WGuildCatapult"
 			 * ).contains(player.getLocation())) { player.sendMessage(
 			 * "You may not equip this shield outside the catapult room in the Warriors' Guild."
 			 * ); return; } if (player.getEquipment().get(Equipment.SLOT_WEAPON)
 			 * != null) { DialogueManager.sendInfoDialogue(player,
 			 * "You will need to make sure your sword hand is free",
 			 * "to equip this shield."); return; }
 			 * 
 			 * } player.getEquipment().equip(player, buttonId, slot, itemId);
 			 * break; } }
 			 */
 			player.sendMessage("This feature has been disabled until further notice.");
 			break;
 		case 667:
 			int equipSlot2 = Equipment.getItemType(itemId);
 			Item item2 = player.getEquipment().get(equipSlot2);
 			if (packet.getOpcode() == 58) {
 				if (item2 != null) {
 					player.sendMessage(item2.getDefinition().getExamine());
 				}
 				return;
 			} else if (packet.getOpcode() == 73) {
 				switch (buttonId) {
 				case 7:
 					ActionSender.sendString(player, 667, 63, item2
 							.getDefinition().getName());
 					ActionSender.sendString(player, 667, 65, "Back");
 					ActionSender.sendInterfaceConfig(player, 667, 51, true);
 					break;
 				}
 			} else if (packet.getOpcode() == 6) {
 				switch (buttonId) {
 				case 64:
 					ActionSender.sendInterfaceConfig(player, 667, 51, false);
 					break;
 				case 7:
 					ItemDefinition definition = ItemDefinition.forId(itemId);
 					if (item2 == null) {
 						return;
 					}
 					if (item2.getId() != itemId) {
 						return;
 					}
 					if (itemId >= 5527 && itemId <= 5545 || itemId == 5547
 							|| itemId == 5549 || itemId == 5551
 							|| itemId == 9106) {
 						ActionSender.sendConfig(player, 491, 0);
 					}
 					if (player.getActivity().getActivityId() == 0
 							&& (equipSlot2 == Equipment.SLOT_CAPE || equipSlot2 == Equipment.SLOT_HAT)) {
 						player.sendMessage("You can't remove your cape or hat in this activity.");
 						return;
 					}
 					if (player.getEquipment().checkUnequip(equipSlot2)) {
 						return;
 					}
 					if (player.getInventory().hasRoomFor(item2.getId(),
 							item2.getAmount())) {
 						player.getEquipment().set(equipSlot2, null);
 						player.getInventory().getContainer().add(item2);
 						if (player.getEquipment().hpModifier(definition)) {
 							player.getSkills().lowerTotalHp(
 									player.getEquipment().getModifier(
 											definition));
 						}
 						player.getInventory().refresh();
 					} else {
 						ActionSender.sendMessage(player,
 								"Not enough space in your inventory.");
 					}
 					break;
 				case 48:
 					if (player.getAttribute("fromBank") != null) {
 						player.setAttribute("inBank", Boolean.TRUE);
 						player.getBank().openBank();
 						player.removeAttribute("fromBank");
 					}
 					break;
 				case 74:
 					if (player.getAttribute("fromBank") != null) {
 						player.removeAttribute("fromBank");
 					}
 					break;
 				}
 			}
 			break;
 		case 671:
 			/*
 			 * if (player.getFamiliar() instanceof BeastOfBurden) {
 			 * BeastOfBurden bob = (BeastOfBurden) player.getFamiliar(); if
 			 * (buttonId == 29) { bob.take(); return; } switch
 			 * (packet.getOpcode()) { case 6: bob.removeItem(slot, 1); break;
 			 * case 13: bob.removeItem(slot, 5); break; case 0:
 			 * bob.removeItem(slot, 10); break; case 15: bob.removeItem(slot,
 			 * bob.numberOf(bob.getContainer().get(slot).getId())); break; case
 			 * 46: InputHandler.requestIntegerInput(player, 9,
 			 * "Please enter an amount:"); player.setAttribute("slotId", slot);
 			 * break; } }
 			 */
 			break;
 		case 620:
 		case 621:
 			World.getWorld()
 					.getShopManager()
 					.getShop((Integer) player.getAttribute("shopId"))
 					.handleOption(player, interfaceId, buttonId, slot,
 							packet.getOpcode(), itemId);
 			break;
 		case 449:
 			if (buttonId == 1) {
 				Shop.sendInventory(player);
 				player.removeAttribute("itemInfoSlot");
 			} else if (buttonId == 21) {
 				World.getWorld()
 						.getShopManager()
 						.getShop((Integer) player.getAttribute("shopId"))
 						.handleOption(player, 620, 25,
 								(Integer) player.getAttribute("itemInfoSlot"),
 								packet.getOpcode(), 0);
 				break;
 			}
 			break;
 		case 589: // Clan chat tab
 			if (buttonId == 16) {
 				ActionSender.sendInterface(player, 590);
 				if (World
 						.getWorld()
 						.getClanManager()
 						.getClan(
 								Misc.formatPlayerNameForProtocol(player
 										.getUsername())) != null) {
 					ActionSender
 							.sendConfig(
 									player,
 									1083,
 									(World.getWorld()
 											.getClanManager()
 											.getClan(
 													Misc.formatPlayerNameForProtocol(player
 															.getUsername()))
 											.isCoinSharing() ? 1 : 0) << 18
 											| (World.getWorld()
 													.getClanManager()
 													.getClan(
 															Misc.formatPlayerNameForProtocol(player
 																	.getUsername()))
 													.isLootsharing() ? 1 : 0));
 
 					ActionSender
 							.sendString(player,
 									World.getWorld().getClanManager()
 											.getClanName(player.getUsername()),
 									590, 22);
 					World.getWorld()
 							.getClanManager()
 							.handleOption(player, 27 - buttonId,
 									buttonId == 25 ? menuIndex + 3 : menuIndex);
 					ActionSender
 							.sendString(
 									player,
 									590,
 									23,
 									ClanChatUtils.RANK_INDEX[World
 											.getWorld()
 											.getClanManager()
 											.getClan(
 													Misc.formatPlayerNameForProtocol(player
 															.getUsername()))
 											.getJoinReq()]);
 					ActionSender
 							.sendString(
 									player,
 									590,
 									24,
 									ClanChatUtils.RANK_INDEX[World
 											.getWorld()
 											.getClanManager()
 											.getClan(
 													Misc.formatPlayerNameForProtocol(player
 															.getUsername()))
 											.getTalkReq()]);
 					ActionSender
 							.sendString(
 									player,
 									590,
 									25,
 									ClanChatUtils.RANK_INDEX[World
 											.getWorld()
 											.getClanManager()
 											.getClan(
 													Misc.formatPlayerNameForProtocol(player
 															.getUsername()))
 											.getKickReq() + 3]);
 					ActionSender
 							.sendString(
 									player,
 									590,
 									26,
 									ClanChatUtils.RANK_INDEX[World
 											.getWorld()
 											.getClanManager()
 											.getClan(
 													Misc.formatPlayerNameForProtocol(player
 															.getUsername()))
 											.getLootReq()]);
 				}
 			}
 			if (buttonId == 0) {
 				World.getWorld().getClanManager().toggleLootshare(player);
 			}
 			if (buttonId == 1) {
 				// World.getWorld().getClanManager().banMember(player,
 				// InputHandler.)
 			}
 			if (buttonId == 15) {
 				World.getWorld().getClanManager().leaveClan(player, false);
 				player.getSettings().setCurrentClan(null);
 			}
 			break;
 		case 590: // Clan chat interface
 			if (buttonId > 22 && buttonId < 27) {
 				World.getWorld()
 						.getClanManager()
 						.handleOption(player, 27 - buttonId,
 								buttonId == 25 ? menuIndex + 3 : menuIndex);
 				String rank = ClanChatUtils.RANK_INDEX[buttonId == 25 ? menuIndex + 3
 						: menuIndex];
 				if (buttonId == 26) {
 					if (menuIndex == 0) {
 						rank = "No-one";
 					}
 				}
 				ActionSender.sendString(player, 590, buttonId, rank);
 			}
 
 			if (buttonId == 22) {
 				switch (packet.getOpcode()) {
 				case 6: // prefix
 					InputHandler.requestStringInput(player, 0,
 							"Enter clan prefix:");
 					break;
 				case 13: // disable
 					break;
 				}
 			}
 			if (buttonId == 33) {
 				World.getWorld().getClanManager().toggleCoinshare(player);
 				ActionSender
 						.sendConfig(
 								player,
 								1083,
 								(World.getWorld()
 										.getClanManager()
 										.getClan(
 												Misc.formatPlayerNameForProtocol(player
 														.getUsername()))
 										.isCoinSharing() ? 1 : 0) << 18
 										| (World.getWorld()
 												.getClanManager()
 												.getClan(
 														Misc.formatPlayerNameForProtocol(player
 																.getUsername()))
 												.isLootsharing() ? 1 : 0));
 
 			}
 			break;
 
 		case 335: // trade
 			switch (buttonId) {
 			/**
 			 * Close button.
 			 */
 			case 18:
 			case 12:
 				if (player.getTradeSession() != null) {
 					player.getTradeSession().tradeFailed();
 				}
 				break;
 			case 16:
 				if (player.getTradeSession() != null) {
 					player.getTradeSession().acceptPressed(player);
 				}
 				break;
 			case 56:
 				player.getTradeSession().removeLoanedItem(player, slot, 1);
 				break;
 			case 57:
 				InputHandler.requestStringInput(player, 2, "Set the loan duration in hours: (1-24)");
 				break;
 			case 34:
 				Item item = player.getTradeSession().partnerItemsOffered.get(slot);
 				if (item == null) return;
 				int itemprice = ItemDefinition.getDefinitions()[itemId].getExchangePrice();
 				String itemname = ItemDefinition.getDefinitions()[itemId].getName();
 				NumberFormat nf1 = NumberFormat.getInstance();
 				if (item.getAmount() == 1)
 					player.sendMessage(itemname+": market price is "+nf1.format(itemprice).replaceAll("\\.", ",")+" coin" + (itemprice == 1 ? "" : "s") + ".");
 				else
 					player.sendMessage(itemname+": market price is "+nf1.format(itemprice).replaceAll("\\.", ",")+" coin" + (itemprice == 1 ? "" : "s") + " each ("+(nf1.format(itemprice*item.getAmount())).replaceAll("\\.", ",")+" coins for "+nf1.format(item.getAmount()).replaceAll("\\.", ",")+").");
 				break;
 			/*case 34:
 				ActionSender.sendMessage(player,player.getItemDef().getId()
 						+ ": is worth "
 						+ player.getItemDef().getExchangePrice());
 				break;*/
 			case 31:
 				if (player.getTradeSession() != null) {
 					switch (packet.getOpcode()) {// 6. 13. 15. 67. 58
 					case 0:
 						player.getTradeSession().removeItem(player, slot, 10);
 						break;
 					case 6:
 						player.getTradeSession().removeItem(player, slot, 1);
 						break;
 					case 13:
 						player.getTradeSession().removeItem(player, slot, 5);
 						break;
 					case 15:
 						player.getTradeSession().removeItem(
 								player,
 								slot,
 								player.getTradeSession()
 										.getPlayerItemsOffered(player)
 										.getNumberOf(
 												player.getTradeSession()
 														.getPlayerItemsOffered(
 																player)
 														.get(slot)));
 						break;
 					case 67:
						Item item3 = player.getTradeSession().traderItemsOffered.get(slot);
						if (item3 == null) return;
						if (item3.getAmount() == 1)
							player.sendMessage(ItemDefinition.getDefinitions()[itemId].getName()+": market price is "+ItemDefinition.getDefinitions()[itemId].getExchangePrice()+" coin" + (ItemDefinition.getDefinitions()[itemId].getExchangePrice() == 1 ? "" : "s") + ".");
 						else
							player.sendMessage(ItemDefinition.getDefinitions()[itemId].getName()+": market price is "+ItemDefinition.getDefinitions()[itemId].getExchangePrice()+" coin" + (ItemDefinition.getDefinitions()[itemId].getExchangePrice() == 1 ? "" : "s") + 
									" each ("+(ItemDefinition.getDefinitions()[itemId].getExchangePrice()*item3.getAmount())+" coins for "+item3.getAmount()+").");
 						break;
 					case 46:
 						InputHandler.requestIntegerInput(player, 2,
 								"Please enter an amount:");
 						player.getTradeSession().resetAccept();
 						player.setAttribute("inputId", 2);
 						player.setAttribute("slotId", slot);
 						break;
 					case 58:
 						player.sendMessage(player.getTradeSession()
 								.getPlayerItemsOffered(player).get(slot)
 								.getDefinition().getName()
 								+ ": is worth "
 								+ player.getTradeSession()
 										.getPlayerItemsOffered(player)
 										.get(slot).getDefinition()
 										.getExchangePrice()+"gp.");
 						break;
 					default:
 					}
 				}
 			default:
 			}
 			break;
 		case 334:
 			switch (buttonId) {
 			case 21:
 				if (player.getTradeSession() != null) {
 					player.getTradeSession().acceptPressed(player);
 				}
 				break;
 			case 22:
 			case 6:
 				if (player.getTradeSession() != null) {
 					player.getTradeSession().tradeFailed();
 				}
 				break;
 			}
 			break;
 		case 336:
 			if (player.getTradeSession() != null) {
 				switch (packet.getOpcode()) {
 				case 6:// 6. 13. 15. 67. 58
 					player.getTradeSession().offerItem(player, slot, 1);
 					break;
 				case 13:
 					player.getTradeSession().offerItem(player, slot, 5);
 					break;
 				case 0:
 					player.getTradeSession().offerItem(player, slot, 10);
 					break;
 				case 15:
 					player.getTradeSession().offerItem(player, slot, player.getInventory().numberOf(player.getInventory().get(slot).getId()));
 					break;
 				/*case 67:
 					ActionSender.sendMessage(player,player.getItemDef().getId()
 						+ ": is worth "
 						+ player.getItemDef().getExchangePrice());
 				break;*/
 				case 46:
 					InputHandler.requestIntegerInput(player, 2,
 							"Please enter an amount:");
 					player.setAttribute("inputId", 1);
 					player.setAttribute("slotId", slot);
 					break;
 				case 82:
 					player.getTradeSession().lendItem(player, slot, 1);
 					break;
 				case 58:
 					ActionSender.sendMessage(
 							player,
 							player.getTradeSession()
 									.getPlayerItemsOffered(player).get(slot)
 									.getDefinition().getName()
 									+ " is valued at "
 									+ player.getTradeSession()
 											.getPlayerItemsOffered(player)
 											.get(slot).getDefinition()
 											.getExchangePrice());
 					break;
 				}
 			}
 			break;
 		case 387:
 			System.out.println("OPCODE: " + packet.getOpcode() + " BUTTON "
 					+ buttonId);
 			int equipSlot = Equipment.getItemType(itemId);
 			Item item = player.getEquipment().get(equipSlot);
 			if (packet.getOpcode() == 58) {
 				if (item != null) {
 					player.sendMessage(item.getDefinition().getExamine());
 				}
 				return;
 			} else if (packet.getOpcode() == 6) {
 				switch (buttonId) {
 				case 17:
 				case 20:
 				case 8:
 				case 11:
 				case 14:
 				case 26:
 				case 32:
 				case 29:
 				case 35:
 				case 23:
 				case 38:
 					ItemDefinition definition = ItemDefinition.forId(itemId);
 					if (item == null) {
 						return;
 					}
 					if (item.getId() != itemId) {
 						return;
 					}
 					if (itemId >= 5527 && itemId <= 5545 || itemId == 5547
 							|| itemId == 5549 || itemId == 5551
 							|| itemId == 9106) {
 						ActionSender.sendConfig(player, 491, 0);
 					}
 					if (player.getActivity().getActivityId() == 0
 							&& (equipSlot == Equipment.SLOT_CAPE || equipSlot == Equipment.SLOT_HAT)) {
 						player.sendMessage("You can't remove your cape or hat in this activity.");
 						return;
 					}
 					if (player.getEquipment().checkUnequip(equipSlot)) {
 						return;
 					}
 					if (player.getInventory().hasRoomFor(item.getId(),
 							item.getAmount())) {
 						player.getEquipment().set(equipSlot, null);
 						player.getInventory().getContainer().add(item);
 						if (player.getEquipment().hpModifier(definition)) {
 							player.getSkills().lowerTotalHp(
 									player.getEquipment().getModifier(
 											definition));
 						}
 						player.getInventory().refresh();
 					} else {
 						ActionSender.sendMessage(player,
 								"Not enough space in your inventory.");
 					}
 					return;
 				case 39:
 					/**
 					 * Config: ID: 1248 Value: 268435464 BCONFIG ID: 199 VALUE:
 					 * -1 Send interface - show id: 0, window id: 548,
 					 * interfaceId: 18, child id: 667. Send interface - show id:
 					 * 0, window id: 548, interfaceId: 198, child id: 670.
 					 * Accessmask set: 1538, interface: 667 child: 7 start 65,
 					 * length: 0 Accessmask set: 1538, interface: 667 child: 7
 					 * start 65, length: 0 Accessmask set: 1538, interface: 667
 					 * child: 7 start 65, length: 0 Accessmask set: 1538,
 					 * interface: 667 child: 7 start 65, length: 0 Accessmask
 					 * set: 1538, interface: 667 child: 7 start 65, length: 0
 					 * Accessmask set: 1538, interface: 667 child: 7 start 65,
 					 * length: 0 Accessmask set: 1538, interface: 667 child: 7
 					 * start 65, length: 0 Accessmask set: 1538, interface: 667
 					 * child: 7 start 65, length: 0 Accessmask set: 1538,
 					 * interface: 667 child: 7 start 65, length: 0 Accessmask
 					 * set: 1538, interface: 667 child: 7 start 65, length: 0
 					 * Accessmask set: 1538, interface: 667 child: 7 start 65,
 					 * length: 0 Accessmask set: 1538, interface: 667 child: 7
 					 * start 65, length: 0 Accessmask set: 1538, interface: 667
 					 * child: 7 start 65, length: 0 Accessmask set: 1538,
 					 * interface: 667 child: 7 start 65, length: 0 Accessmask
 					 * set: 1538, interface: 667 child: 7 start 65, length: 0
 					 * Accessmask set: 1538, interface: 670 child: 0 start 65,
 					 * length: 0 Accessmask set: 1538, interface: 670 child: 0
 					 * start 65, length: 0 Accessmask set: 1538, interface: 670
 					 * child: 0 start 65, length: 0 Accessmask set: 1538,
 					 * interface: 670 child: 0 start 65, length: 0 Accessmask
 					 * set: 1538, interface: 670 child: 0 start 65, length: 0
 					 * Accessmask set: 1538, interface: 670 child: 0 start 65,
 					 * length: 0 Accessmask set: 1538, interface: 670 child: 0
 					 * start 65, length: 0 Accessmask set: 1538, interface: 670
 					 * child: 0 start 65, length: 0 Accessmask set: 1538,
 					 * interface: 670 child: 0 start 65, length: 0 Accessmask
 					 * set: 1538, interface: 670 child: 0 start 65, length: 0
 					 * Accessmask set: 1538, interface: 670 child: 0 start 65,
 					 * length: 0 Accessmask set: 1538, interface: 670 child: 0
 					 * start 65, length: 0 Accessmask set: 1538, interface: 670
 					 * child: 0 start 65, length: 0 Accessmask set: 1538,
 					 * interface: 670 child: 0 start 65, length: 0 Accessmask
 					 * set: 1538, interface: 670 child: 0 start 65, length: 0
 					 * Accessmask set: 1538, interface: 670 child: 0 start 65,
 					 * length: 0 Accessmask set: 1538, interface: 670 child: 0
 					 * start 65, length: 0 Accessmask set: 1538, interface: 670
 					 * child: 0 start 65, length: 0 Accessmask set: 1538,
 					 * interface: 670 child: 0 start 65, length: 0 Accessmask
 					 * set: 1538, interface: 670 child: 0 start 65, length: 0
 					 * Accessmask set: 1538, interface: 670 child: 0 start 65,
 					 * length: 0 Accessmask set: 1538, interface: 670 child: 0
 					 * start 65, length: 0 Accessmask set: 1538, interface: 670
 					 * child: 0 start 65, length: 0 Accessmask set: 1538,
 					 * interface: 670 child: 0 start 65, length: 0 Accessmask
 					 * set: 1538, interface: 670 child: 0 start 65, length: 0
 					 * Accessmask set: 1538, interface: 670 child: 0 start 65,
 					 * length: 0 Accessmask set: 1538, interface: 670 child: 0
 					 * start 65, length: 0 Accessmask set: 1538, interface: 670
 					 * child: 0 start 65, length: 0 BCONFIG ID: 779 VALUE: 28
 					 */
 
 					ActionSender.sendConfig(player, 1248, 268435464);
 					ActionSender.sendBConfig(player, 199, -1);
 					ActionSender.sendInterfaceConfig(player, 667, 49, false);
 					ActionSender.sendInterfaceConfig(player, 667, 50, false);
 					// ActionSender.sendAMask(player, 4, 667, 7, 0, 65);
 					ActionSender.sendAMask(player, 1538, 667, 7, 0, 15);
 					ActionSender.sendAMask(player, 1538, 670, 0, 0, 28);
 					ActionSender.sendBConfig(player, 779, 28);
 					player.getBonuses().refreshEquipScreen();
 					ActionSender.sendInterface(player, 667);
 					ActionSender.sendInventoryInterface(player, 670);
 					break;
 				case 42:
 					player.getPriceCheck().open();
 					break;
 				default:
 					ActionSender.sendChatMessage(player, 0,
 							"Equip button slot " + buttonId + " not handled.");
 					break;
 				}
 			} else if (packet.getOpcode() == 13) {
 				/*
 				 * Operate options. TODO: Do this better, this is just for
 				 * testing DFS spec.
 				 */
 				if (item != null) {
 					if (item.getId() == 11283 || item.getId() == 11284) {
 						if (player.getAttribute("dischargeDelay", 0) > World
 								.getTicks()) {
 							player.sendMessage("Your dragonfire shield is recharging.");
 							return;
 						}
 						if (!player.getAttribute("dragonfireShieldActivated",
 								false)) {
 							player.setAttribute("dragonfireShieldActivated",
 									true);
 							ActionSender.sendConfig(player, 301, 1);
 							return;
 						}
 						player.setAttribute("dragonfireShieldActivated", false);
 						if (!player.usingSpecial()) {
 							ActionSender.sendConfig(player, 301, 0);
 						}
 						return;
 					}
 				}
 
 				switch (buttonId) {
 				case 17:
 				case 20:
 				case 8:
 				case 11:
 				case 14:
 				case 26:
 				case 32:
 				case 29:
 				case 35:
 				case 23:
 				case 38:
 					ItemDefinition definition = ItemDefinition.forId(itemId);
 					if (item == null) {
 						return;
 					}
 					if (item.getId() != itemId) {
 						return;
 					}
 					if (item.getId() == 1712) {
 						player.sendMessage("You rub the amulet...");
 						TeleportHandler.telePlayer(player, 3087, 3496, 0, 0, 0,
 								false, 1712);
 						player.getEquipment().set(Equipment.SLOT_AMULET,
 								new Item(1710, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your amulet has three charges left.");
 						return;
 					}
 					if (item.getId() == 1710) {
 						player.sendMessage("You rub the amulet...");
 						TeleportHandler.telePlayer(player, 3087, 3496, 0, 0, 0,
 								false, 1710);
 						player.getEquipment().set(Equipment.SLOT_AMULET,
 								new Item(1708, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your amulet has two charges left.");
 						return;
 					}
 					if (item.getId() == 1708) {
 						player.sendMessage("You rub the amulet...");
 						TeleportHandler.telePlayer(player, 3087, 3496, 0, 0, 0,
 								false, 1708);
 						player.getEquipment().set(Equipment.SLOT_AMULET,
 								new Item(1706, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your amulet has one charge left.");
 						return;
 					}
 					if (item.getId() == 1706) {
 						player.sendMessage("You rub the amulet...");
 						TeleportHandler.telePlayer(player, 3087, 3496, 0, 0, 0,
 								false, 1706);
 						player.getEquipment().set(Equipment.SLOT_AMULET,
 								new Item(1704, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("You use the amulet's last charge.");
 						return;
 					}
 					if (item.getId() == 1704) {
 						player.sendMessage("You rub the amulet...");
 						player.sendMessage("The amulet has lost its charge.");
 						player.sendMessage("It will need to be recharged before you can use it again.");
 						return;
 					}
 					// Games Necklace
 					if (item.getId() == 3853) {
 						player.sendMessage("You rub the necklace...");
 						TeleportHandler.telePlayer(player, 2876, 3557, 0, 0, 0,
 								false, 3853);
 						player.getEquipment().set(Equipment.SLOT_AMULET,
 								new Item(3855, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your amulet has seven charges left.");
 						return;
 					}
 					if (item.getId() == 3855) {
 						player.sendMessage("You rub the necklace...");
 						TeleportHandler.telePlayer(player, 2876, 3557, 0, 0, 0,
 								false, 3855);
 						player.getEquipment().set(Equipment.SLOT_AMULET,
 								new Item(3857, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your amulet has six charges left.");
 						return;
 					}
 					if (item.getId() == 3857) {
 						player.sendMessage("You rub the necklace...");
 						TeleportHandler.telePlayer(player, 2876, 3557, 0, 0, 0,
 								false, 3857);
 						player.getEquipment().set(Equipment.SLOT_AMULET,
 								new Item(3859, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your amulet has five charges left.");
 						return;
 					}
 					if (item.getId() == 3859) {
 						player.sendMessage("You rub the necklace...");
 						TeleportHandler.telePlayer(player, 2876, 3557, 0, 0, 0,
 								false, 3859);
 						player.getEquipment().set(Equipment.SLOT_AMULET,
 								new Item(3861, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your amulet has four charges left.");
 						return;
 					}
 					if (item.getId() == 3861) {
 						player.sendMessage("You rub the necklace...");
 						TeleportHandler.telePlayer(player, 2876, 3557, 0, 0, 0,
 								false, 3861);
 						player.getEquipment().set(Equipment.SLOT_AMULET,
 								new Item(3863, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your amulet has three charges left.");
 						return;
 					}
 					if (item.getId() == 3863) {
 						player.sendMessage("You rub the necklace...");
 						TeleportHandler.telePlayer(player, 2876, 3557, 0, 0, 0,
 								false, 3863);
 						player.getEquipment().set(Equipment.SLOT_AMULET,
 								new Item(3865, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your amulet has two charges left.");
 						return;
 					}
 					if (item.getId() == 3865) {
 						player.sendMessage("You rub the necklace...");
 						TeleportHandler.telePlayer(player, 2876, 3557, 0, 0, 0,
 								false, 3865);
 						player.getEquipment().set(Equipment.SLOT_AMULET,
 								new Item(3867, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your amulet has one charge left.");
 						return;
 					}
 					if (item.getId() == 3867) {
 						TeleportHandler.telePlayer(player, 2876, 3557, 0, 0, 0,
 								false, 3867);
 						player.getEquipment().refresh();
 						player.sendMessage("Your games necklace crumbles to dust.");
 						return;
 					}
 					// Ring Of Duelling
 					if (item.getId() == 2552) {
 						player.sendMessage("You rub the ring...");
 						TeleportHandler.telePlayer(player, 3313, 3234, 0, 0, 0,
 								false, 2552);
 						player.getEquipment().set(Equipment.SLOT_RING,
 								new Item(2554, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your ring has seven charges left.");
 						return;
 					}
 					if (item.getId() == 2554) {
 						player.sendMessage("You rub the ring...");
 						TeleportHandler.telePlayer(player, 3313, 3234, 0, 0, 0,
 								false, 2554);
 						player.getEquipment().set(Equipment.SLOT_RING,
 								new Item(2556, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your ring has six charges left.");
 						return;
 					}
 					if (item.getId() == 2556) {
 						player.sendMessage("You rub the ring...");
 						TeleportHandler.telePlayer(player, 3313, 3234, 0, 0, 0,
 								false, 2556);
 						player.getEquipment().set(Equipment.SLOT_RING,
 								new Item(2558, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your ring has five charges left.");
 						return;
 					}
 					if (item.getId() == 2558) {
 						player.sendMessage("You rub the ring...");
 						TeleportHandler.telePlayer(player, 3313, 3234, 0, 0, 0,
 								false, 2558);
 						player.getEquipment().set(Equipment.SLOT_RING,
 								new Item(2560, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your ring has four charges left.");
 						return;
 					}
 					if (item.getId() == 2560) {
 						player.sendMessage("You rub the ring...");
 						TeleportHandler.telePlayer(player, 3313, 3234, 0, 0, 0,
 								false, 2560);
 						player.getEquipment().set(Equipment.SLOT_RING,
 								new Item(2562, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your ring has three charges left.");
 						return;
 					}
 					if (item.getId() == 2562) {
 						player.sendMessage("You rub the ring...");
 						TeleportHandler.telePlayer(player, 3313, 3234, 0, 0, 0,
 								false, 2562);
 						player.getEquipment().set(Equipment.SLOT_RING,
 								new Item(2564, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your ring has two charges left.");
 						return;
 					}
 					if (item.getId() == 2564) {
 						player.sendMessage("You rub the ring...");
 						TeleportHandler.telePlayer(player, 3313, 3234, 0, 0, 0,
 								false, 2564);
 						player.getEquipment().set(Equipment.SLOT_RING,
 								new Item(2566, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your ring has one charge left.");
 						return;
 					}
 					if (item.getId() == 2566) {
 						player.sendMessage("You rub the ring...");
 						TeleportHandler.telePlayer(player, 3313, 3234, 0, 0, 0,
 								false, 2566);
 						player.getEquipment().refresh();
 						player.sendMessage("Your ring of duelling crumbles to dust.");
 						return;
 					}
 					if (item.getId() == 15398) {
 						player.sendMessage("You rub the ring...");
 						TeleportHandler.telePlayer(player, 1690, 5287, 1, 0, 0,
 								false, 15398);
 						player.getEquipment().set(Equipment.SLOT_RING,
 								new Item(15399, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your ring has four charges left.");
 						return;
 					}
 					if (item.getId() == 15399) {
 						player.sendMessage("You rub the ring...");
 						TeleportHandler.telePlayer(player, 1690, 5287, 1, 0, 0,
 								false, 15399);
 						player.getEquipment().set(Equipment.SLOT_RING,
 								new Item(15400, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your ring has three charges left.");
 						return;
 					}
 					if (item.getId() == 15400) {
 						player.sendMessage("You rub the ring...");
 						TeleportHandler.telePlayer(player, 1690, 5287, 1, 0, 0,
 								false, 15400);
 						player.getEquipment().set(Equipment.SLOT_RING,
 								new Item(15401, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your ring has two charges left.");
 						return;
 					}
 					if (item.getId() == 15401) {
 						player.sendMessage("You rub the ring...");
 						TeleportHandler.telePlayer(player, 1690, 5287, 1, 0, 0,
 								false, 15401);
 						player.getEquipment().set(Equipment.SLOT_RING,
 								new Item(15402, 1));
 						player.getEquipment().refresh();
 						player.sendMessage("Your ring has one charges left.");
 						return;
 					}
 					if (item.getId() == 15402) {
 						player.sendMessage("You rub the ring...");
 						TeleportHandler.telePlayer(player, 1690, 5287, 1, 0, 0,
 								false, 15402);
 						player.getEquipment().refresh();
 						player.sendMessage("Your ferocious ring crumbles to dust.");
 						return;
 					}
 					if (player.getActivity().getActivityId() == 0
 							&& (equipSlot == Equipment.SLOT_CAPE || equipSlot == Equipment.SLOT_HAT)) {
 						player.sendMessage("You can't remove your cape or hat in this activity.");
 						return;
 					}
 					if (player.getEquipment().checkUnequip(equipSlot)) {
 						return;
 					}
 					if (player.getInventory().hasRoomFor(item.getId(),
 							item.getAmount())) {
 						player.getEquipment().set(equipSlot, null);
 						player.getInventory().getContainer().add(item);
 						if (player.getEquipment().hpModifier(definition)) {
 							player.getSkills().lowerTotalHp(
 									player.getEquipment().getModifier(
 											definition));
 						}
 						player.getInventory().refresh();
 					} else {
 						ActionSender.sendMessage(player,
 								"Not enough space in your inventory.");
 					}
 					return;
 				case 39:
 					ActionSender.sendInterfaceConfig(player, 667, 49, false);
 					ActionSender.sendInterfaceConfig(player, 667, 50, false);
 					player.getBonuses().refreshEquipScreen();
 					ActionSender.sendInterface(player, 667);
 					break;
 				case 42:
 					player.getPriceCheck().open();
 					break;
 				default:
 					ActionSender.sendChatMessage(player, 0,
 							"Equip button slot " + buttonId + " not handled.");
 					break;
 				}
 			} else if (packet.getOpcode() == 0) {// Third option
 				if (item.getId() == 1712) {
 					player.sendMessage("You rub the amulet...");
 					TeleportHandler.telePlayer(player, 2918, 3176, 0, 0, 0,
 							false, 1712);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(1710, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your amulet has three charges left.");
 					return;
 				}
 				if (item.getId() == 1710) {
 					player.sendMessage("You rub the amulet...");
 					TeleportHandler.telePlayer(player, 2918, 3176, 0, 0, 0,
 							false, 1710);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(1708, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your amulet has two charges left.");
 					return;
 				}
 				if (item.getId() == 1708) {
 					player.sendMessage("You rub the amulet...");
 					TeleportHandler.telePlayer(player, 2918, 3176, 0, 0, 0,
 							false, 1708);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(1706, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your amulet has one charge left.");
 					return;
 				}
 				if (item.getId() == 1706) {
 					player.sendMessage("You rub the amulet...");
 					TeleportHandler.telePlayer(player, 2918, 3176, 0, 0, 0,
 							false, 1706);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(1704, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("You use the amulet's last charge.");
 					return;
 				}
 				if (item.getId() == 1704) {
 					player.sendMessage("You rub the amulet...");
 					player.sendMessage("The amulet has lost its charge.");
 					player.sendMessage("It will need to be recharged before you can use it again.");
 				}
 				if (item.getId() == 3853) {
 					player.sendMessage("You rub the necklace...");
 					TeleportHandler.telePlayer(player, 2518, 3570, 0, 0, 0,
 							false, 3853);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(3855, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your amulet has seven charges left.");
 					return;
 				}
 				if (item.getId() == 3855) {
 					player.sendMessage("You rub the necklace...");
 					TeleportHandler.telePlayer(player, 2518, 3570, 0, 0, 0,
 							false, 3855);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(3857, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your amulet has six charges left.");
 					return;
 				}
 				if (item.getId() == 3857) {
 					player.sendMessage("You rub the necklace...");
 					TeleportHandler.telePlayer(player, 2518, 3570, 0, 0, 0,
 							false, 3857);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(3859, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your amulet has five charges left.");
 					return;
 				}
 				if (item.getId() == 3859) {
 					player.sendMessage("You rub the necklace...");
 					TeleportHandler.telePlayer(player, 2518, 3570, 0, 0, 0,
 							false, 3859);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(3861, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your amulet has four charges left.");
 					return;
 				}
 				if (item.getId() == 3861) {
 					player.sendMessage("You rub the necklace...");
 					TeleportHandler.telePlayer(player, 2518, 3570, 0, 0, 0,
 							false, 3861);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(3863, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your amulet has three charges left.");
 					return;
 				}
 				if (item.getId() == 3863) {
 					player.sendMessage("You rub the necklace...");
 					TeleportHandler.telePlayer(player, 2518, 3570, 0, 0, 0,
 							false, 3863);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(3865, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your amulet has two charges left.");
 					return;
 				}
 				if (item.getId() == 3865) {
 					player.sendMessage("You rub the necklace...");
 					TeleportHandler.telePlayer(player, 2518, 3570, 0, 0, 0,
 							false, 3865);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(3867, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your amulet has one charges left.");
 					return;
 				}
 				if (item.getId() == 3867) {
 					TeleportHandler.telePlayer(player, 2518, 3570, 0, 0, 0,
 							false, 3867);
 					player.getEquipment().refresh();
 					player.sendMessage("Your games necklace crumbles to dust.");
 					return;
 				}
 				if (item.getId() == 2552) {
 					player.sendMessage("You rub the ring...");
 					TeleportHandler.telePlayer(player, 2441, 3089, 0, 0, 0,
 							false, 2552);
 					player.getEquipment().set(Equipment.SLOT_RING,
 							new Item(2554, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your ring has seven charges left.");
 					return;
 				}
 				if (item.getId() == 2554) {
 					player.sendMessage("You rub the ring...");
 					TeleportHandler.telePlayer(player, 2441, 3089, 0, 0, 0,
 							false, 2554);
 					player.getEquipment().set(Equipment.SLOT_RING,
 							new Item(2556, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your ring has six charges left.");
 					return;
 				}
 				if (item.getId() == 2556) {
 					player.sendMessage("You rub the ring...");
 					TeleportHandler.telePlayer(player, 2441, 3089, 0, 0, 0,
 							false, 2556);
 					player.getEquipment().set(Equipment.SLOT_RING,
 							new Item(2558, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your ring has five charges left.");
 					return;
 				}
 				if (item.getId() == 2558) {
 					player.sendMessage("You rub the ring...");
 					TeleportHandler.telePlayer(player, 2441, 3089, 0, 0, 0,
 							false, 2558);
 					player.getEquipment().set(Equipment.SLOT_RING,
 							new Item(2560, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your ring has four charges left.");
 					return;
 				}
 				if (item.getId() == 2560) {
 					player.sendMessage("You rub the ring...");
 					TeleportHandler.telePlayer(player, 2441, 3089, 0, 0, 0,
 							false, 2560);
 					player.getEquipment().set(Equipment.SLOT_RING,
 							new Item(2562, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your ring has three charges left.");
 					return;
 				}
 				if (item.getId() == 2562) {
 					player.sendMessage("You rub the ring...");
 					TeleportHandler.telePlayer(player, 2441, 3089, 0, 0, 0,
 							false, 2562);
 					player.getEquipment().set(Equipment.SLOT_RING,
 							new Item(2564, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your ring has two charges left.");
 					return;
 				}
 				if (item.getId() == 2564) {
 					player.sendMessage("You rub the ring...");
 					TeleportHandler.telePlayer(player, 2441, 3089, 0, 0, 0,
 							false, 2564);
 					player.getEquipment().set(Equipment.SLOT_RING,
 							new Item(2566, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your ring has one charge left.");
 					return;
 				}
 				if (item.getId() == 2566) {
 					player.sendMessage("You rub the ring...");
 					TeleportHandler.telePlayer(player, 2441, 3089, 0, 0, 0,
 							false, 2566);
 					player.getEquipment().refresh();
 					player.sendMessage("Your ring of duelling crumbles to dust.");
 					return;
 				}
 			} else if (packet.getOpcode() == 46) {// Fifth option
 				if (item.getId() == 1712) {
 					player.sendMessage("You rub the amulet...");
 					TeleportHandler.telePlayer(player, 3293, 3163, 0, 0, 0,
 							false, 1712);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(1710, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your amulet has three charges left.");
 					return;
 				}
 				if (item.getId() == 1710) {
 					player.sendMessage("You rub the amulet...");
 					TeleportHandler.telePlayer(player, 3293, 3163, 0, 0, 0,
 							false, 1710);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(1708, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your amulet has two charges left.");
 					return;
 				}
 				if (item.getId() == 1708) {
 					player.sendMessage("You rub the amulet...");
 					TeleportHandler.telePlayer(player, 3293, 3163, 0, 0, 0,
 							false, 1708);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(1706, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your amulet has one charge left.");
 					return;
 				}
 				if (item.getId() == 1706) {
 					player.sendMessage("You rub the amulet...");
 					TeleportHandler.telePlayer(player, 3293, 3163, 0, 0, 0,
 							false, 1706);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(1704, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("You use the amulet's last charge.");
 					return;
 				}
 			} else if (packet.getOpcode() == 15) {// Fourth option
 				if (item.getId() == 1712) {
 					player.sendMessage("You rub the amulet...");
 					TeleportHandler.telePlayer(player, 3105, 3251, 0, 0, 0,
 							false, 1712);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(1710, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your amulet has three charges left.");
 					return;
 				}
 				if (item.getId() == 1710) {
 					player.sendMessage("You rub the amulet...");
 					TeleportHandler.telePlayer(player, 3105, 3251, 0, 0, 0,
 							false, 1710);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(1708, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your amulet has two charges left.");
 					return;
 				}
 				if (item.getId() == 1708) {
 					player.sendMessage("You rub the amulet...");
 					TeleportHandler.telePlayer(player, 3105, 3251, 0, 0, 0,
 							false, 1708);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(1706, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("Your amulet has one charge left.");
 					return;
 				}
 				if (item.getId() == 1706) {
 					player.sendMessage("You rub the amulet...");
 					TeleportHandler.telePlayer(player, 3105, 3251, 0, 0, 0,
 							false, 1706);
 					player.getEquipment().set(Equipment.SLOT_AMULET,
 							new Item(1704, 1));
 					player.getEquipment().refresh();
 					player.sendMessage("You use the amulet's last charge.");
 					return;
 				}
 				if (item.getId() == 1704) {
 					player.sendMessage("You rub the amulet...");
 					player.sendMessage("The amulet has lost its charge.");
 					player.sendMessage("It will need to be recharged before you can use it again.");
 				}
 
 			}
 
 			break;
 		case 750:
 			switch (buttonId) {
 			case 1:
 				if (packet.getOpcode() == 6) {
 					player.getWalkingQueue().setRunToggled(
 							!player.getWalkingQueue().isRunToggled());
 				} else if (packet.getOpcode() == 13) {
 					if (player.getSettings().isResting()) {
 						player.sendMessage("You're already resting!");
 						return;
 					}
 					player.getWalkingQueue().reset();
 					player.getSettings().setResting(true);
 					ActionSender.sendConfig(player, 1433, 1);
 					ActionSender.sendConfig(player, 1189, 3833973);
 					ActionSender.sendBConfig(player, 119, 3);
 					if (player.getRandom().nextBoolean()) {
 						player.animate(5713);
 					} else {
 						player.animate(11786);
 					}
 					player.setAttribute("restAnimation", player.getMask()
 							.getLastAnimation());
 				}
 				break;
 			}
 			break;
 		case 884:
 			switch (buttonId) {
 			case 4:// special bar
 				if (player.getActivity().toString().equals("DuelActivity")) {
 					DuelActivity arena = (DuelActivity) player.getActivity();
 					if (arena.getDuelConfigurations().getRule(
 							Rules.SPECIAL_ATTACKS)) {
 						player.sendMessage("You cannot use special attacks during this duel!");
 						return;
 					}
 				}
 				if (player.getSpecialAmount() < 1
 						&& !player.getSettings().isUsingSpecial()) {
 					player.sendMessage("You do not have enough power left.");
 					return;
 				}
 				player.reverseSpecialActive();
 				SpecialAttack spec = SpecialAttackContainer.get(player
 						.getEquipment().getSlot(3));
 				if (spec instanceof QuickSmash) {
 					if (player.getCombatExecutor().getVictim() == null
 							|| Math.floor(player.getLocation().distance(
 									player.getCombatExecutor().getVictim()
 											.getLocation())) > 1) {
 						player.sendMessage("Warning: Since the maul's special is an instant attack, it will be wasted when used ");
 						player.sendMessage("on a first strike.");
 						player.reverseSpecialActive();
 						return;
 					}
 					player.getCombatExecutor().setTicks(0);
 					player.getCombatExecutor().tick();
 				} else if (player.getEquipment().get(3).getDefinition()
 						.getName().contains("Staff of light")) {
 					if (player.getSpecialAmount() < 1000) {
 						player.sendMessage("You do not have enough power left.");
 						return;
 					}
 					player.setAttribute("staffOfLightEffect",
 							World.getTicks() + 100);
 					player.getCombatExecutor().setTicks(
 							player.getCombatExecutor().getTicks() + 3);
 					player.animate(12804);
 					player.graphics(2319);
 					player.setSpecialAmount(player.getSpecialAmount() - 1000);
 					player.reverseSpecialActive();
 				} else if (player.getEquipment().get(3).getDefinition()
 						.getName().contains("Dragon battle")) {
 					if (player.getSpecialAmount() < 1000) {
 						player.sendMessage("You do not have enough power left.");
 						return;
 					}
 					player.forceText("Raarrrrrgggggghhhhhhh!");
 					player.animate(1056);
 					player.graphics(246);
 					player.setSpecialAmount(player.getSpecialAmount() - 1000);
 					player.reverseSpecialActive();
 					int attackLevel = (int) (0.1 * player.getSkills().getLevel(
 							Skills.ATTACK));
 					int defenceLevel = (int) (0.1 * player.getSkills()
 							.getLevel(Skills.DEFENCE));
 					int rangeLevel = (int) (0.1 * player.getSkills().getLevel(
 							Skills.RANGE));
 					int magicLevel = (int) (0.1 * player.getSkills().getLevel(
 							Skills.MAGIC));
 					int strengthLevel = (int) (10 + (0.25 * (magicLevel
 							+ rangeLevel + defenceLevel + attackLevel)));
 					player.getSkills().set(
 							Skills.ATTACK,
 							player.getSkills().getLevel(Skills.ATTACK)
 									- attackLevel);
 					player.getSkills().set(
 							Skills.DEFENCE,
 							player.getSkills().getLevel(Skills.DEFENCE)
 									- defenceLevel);
 					player.getSkills().set(
 							Skills.RANGE,
 							player.getSkills().getLevel(Skills.RANGE)
 									- rangeLevel);
 					player.getSkills().set(
 							Skills.MAGIC,
 							player.getSkills().getLevel(Skills.MAGIC)
 									- magicLevel);
 					player.getSkills().set(
 							Skills.STRENGTH,
 							player.getSkills().getLevelForExperience(
 									Skills.STRENGTH)
 									+ strengthLevel);
 				} else if (player.getEquipment().get(3).getDefinition()
 						.getName().contains("xcalibur")) {
 					if (player.getSpecialAmount() < 1000) {
 						player.sendMessage("You do not have enough power left.");
 						return;
 					}
 					final boolean enhanced = player.getEquipment().get(3)
 							.getDefinition().getName().contains("Enhanced");
 					player.forceText("For Dementhium!");
 					player.animate(1168);
 					player.graphics(247);
 					player.setSpecialAmount(player.getSpecialAmount() - 1000);
 					player.reverseSpecialActive();
 					int defenceLevel = enhanced ? (int) (0.15 * player
 							.getSkills().getLevelForExperience(Skills.DEFENCE))
 							: 8;
 					player.getSkills().set(
 							Skills.DEFENCE,
 							player.getSkills().getLevelForExperience(
 									Skills.DEFENCE)
 									+ defenceLevel);
 					if (enhanced) {
 						World.getWorld().submit(new Tick(3) {
 							int count = 5;
 
 							@Override
 							public void execute() {
 								player.getSkills().heal(40);
 								if (--count == 0) {
 									this.stop();
 								}
 							}
 						});
 					}
 				}
 				break;
 			case 15:// auto retaliate
 				player.reverseAutoRetaliate();
 				break;
 			case 11:
 			case 12:
 			case 13:
 			case 14:
 				player.getEquipment().toggleStyle(player, buttonId);
 				break;
 			}
 			break;
 		case 271:
 			if (player.getActivity().toString().equals("DuelActivity")) {
 				DuelActivity arena = (DuelActivity) player.getActivity();
 				if (arena.getDuelConfigurations().getRule(Rules.PRAYER)) {
 					player.sendMessage("You cannot use prayer during this duel!");
 					return;
 				}
 			}
 			switch (buttonId) {
 			case 8:
 			case 42:
 				player.getPrayer().switchPrayer(slot,
 						player.getPrayer().isAncientCurses());
 				break;
 			case 43:
 				player.getPrayer().setQuickPrayers();
 				break;
 			default:
 			}
 			break;
 		case 182:
 			switch (buttonId) {
 			case 5: // lobby
 			case 10: // out
 				if (player.getAttribute("isInDuelArena") != null) {
 					player.sendMessage("You cannot logout while in a duel!");
 					return;
 				}
 				ActionSender.sendLogout(player, buttonId);
 				break;
 			}
 			break;
 		case 982: // split chat settings
 			if (buttonId == 5) {
 				int winId = player.getConnection().getDisplayMode() < 2 ? 548
 						: 746;
 				int slotId = player.getConnection().getDisplayMode() < 2 ? 214
 						: 99;
 				ActionSender.sendInterface(player, 1, winId, slotId, 261);
 			} else {
 				if (buttonId >= 45 && buttonId <= 62) {
 					int color = buttonId - 44;
 					player.getSettings().setPrivateTextColor(color);
 					ActionSender.sendConfig(player, 287, player.getSettings()
 							.getPrivateTextColor());
 				}
 			}
 			break;
 		case 755:
 			switch (packet.getOpcode()) {
 			case 6:
 				switch (buttonId) {
 				case 44:// Close Button
 					player.removeAttribute("worldmap");
 					break;
 				}
 				break;
 			}
 			break;
 		case 746:
 			switch (buttonId) {
 			case 179:
 				ActionSender.sendWindowsPane(player, 755, 0);
 				player.setAttribute("worldmap", true);
 				ActionSender.sendBConfig(player, 622, player.getLocation()
 						.getX() << 14
 						| player.getLocation().getY()
 						| player.getLocation().getZ() << 28);
 				World.getWorld().submit(new Tick(3) {
 					@Override
 					public void execute() {
 						if (player.getAttribute("worldmap") != null) {
 							player.animate(840);
 							ActionSender.sendBConfig(player, 674, player
 									.getLocation().getX() << 14
 									| player.getLocation().getY()
 									| player.getLocation().getZ() << 28);
 						} else {
 							player.animate(Animation.RESET);
 							player.setAttribute("resetCanvasAndRegionalData",
 									true);
 							InterfaceSettings.switchWindow(player, player
 									.getConnection().getDisplayMode());
 							stop();
 						}
 					}
 				});
 				break;
 			}
 			break;
 		case 548:
 			switch (packet.getOpcode()) {
 			case 39:
 				player.getSkills().setExperienceCounter(0);
 				ActionSender.sendConfig(player, 1801, player.getSkills()
 						.getExperienceCounter() * 10);
 				break;
 			case 6:
 				switch (buttonId) {
 				case 179:
 					ActionSender.sendWindowsPane(player, 755, 0);
 					player.setAttribute("worldmap", true);
 					ActionSender.sendBConfig(player, 622, player.getLocation()
 							.getX() << 14
 							| player.getLocation().getY()
 							| player.getLocation().getZ() << 28);
 					World.getWorld().submit(new Tick(3) {
 						@Override
 						public void execute() {
 							if (player.getAttribute("worldmap") != null) {
 								player.animate(840);
 								ActionSender.sendBConfig(player, 674, player
 										.getLocation().getX() << 14
 										| player.getLocation().getY()
 										| player.getLocation().getZ() << 28);
 							} else {
 								player.setAttribute(
 										"resetCanvasAndRegionalData", true);
 								player.animate(Animation.RESET);
 								InterfaceSettings.switchWindow(player, player
 										.getConnection().getDisplayMode());
 								stop();
 							}
 						}
 					});
 					break;
 				}
 				break;
 			}
 			if (buttonId > 127) {
 				player.setAttribute("viewTab", buttonId - 128);
 			} else {
 				player.setAttribute("viewTab", 8 + buttonId - 98);
 			}
 			// ActionSender.closeSideInterface(p);
 			/*
 			 * switch (buttonId) { case 124: ActionSender.sendInterface(p, 1,
 			 * 548, 199, 320); break; case 123: ActionSender.sendInterface(p, 1,
 			 * 548, 198, 884); break; case 125: ActionSender.sendInterface(p, 1,
 			 * 548, 200, 190); break; case 126: ActionSender.sendInterface(p, 1,
 			 * 548, 201, 259); break; case 127: ActionSender.sendInterface(p, 1,
 			 * 548, 202, 149); break; case 128: ActionSender.sendInterface(p, 1,
 			 * 548, 203, 387); break; case 129: ActionSender.sendInterface(p, 1,
 			 * 548, 204, 271); break; case 130: ActionSender.sendInterface(p, 1,
 			 * 548, 205, p.getSettings().getSpellBook());// BOOK break; case 93:
 			 * ActionSender.sendInterface(p, 1, 548, 206, 891); break; case 94:
 			 * ActionSender.sendInterface(p, 1, 548, 207, 550); break; case 95:
 			 * ActionSender.sendInterface(p, 1, 548, 208, 551); break; case 96:
 			 * ActionSender.sendInterface(p, 1, 548, 209, 589); break; case 97:
 			 * ActionSender.sendInterface(p, 1, 548, 210, 261); break; case 98:
 			 * ActionSender.sendInterface(p, 1, 548, 211, 464); break; case 99:
 			 * ActionSender.sendInterface(p, 1, 548, 212, 187); break; case 100:
 			 * ActionSender.sendInterface(p, 1, 548, 213, 34); break; default:
 			 * System.out.println(buttonId); }
 			 */
 
 			break;
 		case 853:
 			System.out.println(buttonId);
 			break;
 		case 261:
 			switch (buttonId) {
 			case 3:
 				ActionSender.sendConfig(player, 173, player.getWalkingQueue()
 						.isRunToggled() ? 0 : 1);
 				player.getWalkingQueue().setRunToggled(
 						!player.getWalkingQueue().isRunToggled());
 				break;
 			case 5:
 				if (player.getConnection().getDisplayMode() == 1) {
 					ActionSender.sendInterface(player, 1, 548, 214, 982);
 					// ActionSender.sendAccessMask(player, -1, -1, 548, 97, 0,
 					// 2);
 				} else {
 					ActionSender.sendInterface(player, 1, 746, 99, 982);
 					// ActionSender.sendAccessMask(player, -1, -1, 746, 48, 0,
 					// 2);
 				}
 				break;
 			case 6: // Mouse Button config
 				player.setAttribute("mouseButtons", (Integer) player
 						.getAttribute("mouseButtons", 0) == 0 ? 1 : 0);
 				ActionSender.sendConfig(player, 170,
 						(Integer) player.getAttribute("mouseButtons"));
 				break;
 			case 7: // Accept aid config
 				break;
 			case 8: // House Building Options
 				ActionSender.sendInventoryInterface(player, 398);
 				break;
 			case 14:
 				ActionSender.sendInterface(player, 742);
 				break;
 			case 16:
 				ActionSender.sendInterface(player, 743);
 				break;
 			default:
 				break;
 			}
 			break;
 		case 662: {
 			/*
 			 * Familiar familiar = player.getFamiliar(); if (familiar != null) {
 			 * switch (buttonId) { case 49: familiar.callToPlayer(false); break;
 			 * case 51: familiar.dismiss(); break; } }
 			 */}
 			break;
 		case 747:
 			/*
 			 * Familiar familiar = player.getFamiliar(); if (familiar != null) {
 			 * switch (buttonId) { case 18: familiar.showDetails(); break; case
 			 * 12: if (familiar instanceof BeastOfBurden) { ((BeastOfBurden)
 			 * player.getFamiliar()).take(); } break; case 10:
 			 * familiar.callToPlayer(false); break; case 11: familiar.dismiss();
 			 * break; } }
 			 */
 			break;
 		default:
 			// remove this before you put it on the main server
 			// System.out.println("interfaceId: " + interfaceId + ", buttonId: "
 			// + buttonId);
 		}
 	}
 }
