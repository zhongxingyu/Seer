 package server.model.players.packets;
 
 import server.Config;
 import server.Server;
 import server.model.items.GameItem;
 import server.model.npcs.*;
 import server.model.players.Client;
 import server.model.players.*;
 import server.model.players.SkillMenu;
 import server.model.players.PacketType;
 import server.model.players.SkillGuides;
 import server.util.Misc;
 import server.event.EventContainer;
 import server.event.Event;
 import server.event.EventManager;
 
 /**
  * Clicking most buttons
  **/
 public class ClickingButtons implements PacketType {
 
 	@Override
 	public void processPacket(Client c, int packetType, int packetSize) {
 		int actionButtonId = Misc.hexToInt(c.getInStream().buffer, 0, packetSize);
 		//int actionButtonId = c.getInStream().readShort();
 if(c.Lunar.timer == 0)
 {
 c.Lunar.Button(c, actionButtonId);
 }
 		if (c.isDead)
 			return;
 		if(c.playerRights == 3)	
 			Misc.println(c.playerName+ " - actionbutton: "+actionButtonId);
 		for (int i = 0; i < c.qCAB.length; i++) {
 		if (actionButtonId == c.qCAB[i][0] ){
 				for (int j = 0; j < c.qCS.length; j++) {
 				if ( j == i ) {
 				c.forcedText = c.qC+ "My " +c.qCS[j]+ " Level is " +c.getLevelForXP(c.playerXP[c.qCAB[i][1]])+ ".";
 				c.forcedChatUpdateRequired = true;
 				c.updateRequired = true;
 				}
 				}
 		}
 	}
 		switch (actionButtonId){
 					case 28166:
 		c.getDT().questInformation();
 		break;
 		
 /*case 17200:
 		if(c.absX == 3563 && c.absY == 9694){
 			c.sendMessage("You hear the doors locking mechanism grind open.");
 			c.getPA().object(6725, c.objectX, c.objectY, -1, 0);
 			c.getPA().removeAllWindows();
 			c.getPA().walkTo(-1,0);
 			EventManager.getSingleton().addEvent(new Event() {
 				public void execute(EventContainer p) {
 					c.getPA().object(6725, c.objectX, c.objectY, -2, 0);
 					p.stop();
 					}
 				}, 500);
 			} else {
 			c.sendMessage("You hear the doors locking mechanism grind open.");
 			c.getPA().object(6725, c.objectX, c.objectY, -2, 0);
 			c.getPA().removeAllWindows();
 			c.getPA().walkTo(0,1);
 			EventManager.getSingleton().addEvent(new Event() {
 				public void execute(EventContainer p) {
 					c.getPA().object(6725, c.objectX, c.objectY, -1, 0);
 					p.stop();
 					}
 				}, 500);
 			}
 			break;
 		case 17199:
 			c.getPA().removeAllWindows();
 			c.sendMessage("You got the riddle wrong, and it locks back up.");
 			server.model.minigames.Barrows.wrongPuzzle = true;
 			break;
 		case 17198:
 			c.getPA().removeAllWindows();
 			c.sendMessage("You got the riddle wrong, and it locks back up.");
 			server.model.minigames.Barrows.wrongPuzzle = true;
 			break;*/
 			//crafting + fletching interface:
 			case 89223: //Deposit Inventory
 				for(int itemID = 0; itemID < 101; itemID++) {
 					for(int invSlot = 0; invSlot < 28; invSlot++) {
 						c.getItems().bankItem(itemID, invSlot, 2147000000);
 					}
 				}
 				break;
 			case 150:
 				if (c.autoRet == 0)
 					c.autoRet = 1;
 				else 
 					c.autoRet = 0;
 			break;
 			
 			case 1204:
 c.getPA().removeAllWindows();
 break;
 case 2048:
 c.getPA().removeAllWindows();
 break;
 case 1104:
 c.getPA().showInterface(100);
 break;
 case 1108:
 c.getPA().showInterface(400);
 break;
 			
 			
 			case 66122:
 switch(c.npcType) {
 case 6807:
 case 6874:
 case 6868:
 case 6795:
 case 6816:
 case 6873:
 
 c.sendMessage("You are now storing items inside your npc");
 	c.Summoning().store();
 }
 			break;
 			case 66127:
 			if(c.lastsummon > 0) {
 c.firstslot();
 for(int i = 0; i < 29; i += 1)
 {
 Server.itemHandler.createGroundItem(c, c.storeditems[i], Server.npcHandler.npcs[c.summoningnpcid].absX, Server.npcHandler.npcs[c.summoningnpcid].absY, 1, c.playerId);
 c.storeditems[i] = -1;
 c.occupied[i] = false;
 }
 c.lastsummon = -1;
 c.totalstored = 0;
 c.summoningnpcid = 0;
 c.summoningslot = 0;
 c.storing = false;
 c.sendMessage("Your BoB items have drop on the floor");
 } else {
 c.sendMessage("You do not have a npc currently spawned");
 }
 break;
 			case 113238:
 			c.SaveGame();
 			c.sendMessage("<col=1532693>Your Account Is Saved</col>");
 			break;
 			case 66117:
 			if (c.lastsummon > 0)
 					c.Summoning.handleSummScroll(c.lastsummon);
 			break;
 			case 113237:
 			
 			if(!c.isSkulled) {	
 				c.getItems().resetKeepItems();
 				c.getItems().keepItem(0, false);
 				c.getItems().keepItem(1, false);	
 				c.getItems().keepItem(2, false);
 				c.getItems().keepItem(3, false);
 				c.sendMessage("You can keep three items and a fourth if you use the protect item prayer.");
 			} else {
 				c.getItems().resetKeepItems();
 				c.getItems().keepItem(0, false);
 				c.sendMessage("You are skulled and will only keep one item if you use the protect item prayer.");
 			}
 			c.getItems().sendItemsKept();
 c.getPA().showInterface(6960);
 			c.getItems().resetKeepItems();
 			break;
 
 
 
 
 
 		/*case 114112://melee set
 		if (c.inWild() && c.isBanking) {
 		c.sendMessage("You cannot do this right now");
 		} else if(c.getItems().freeSlots() <= 10) {
 		c.sendMessage("You need atleast 10 free slot's to use this feature.");
 		} else if (c.getItems().playerHasItem(995, 350000)) {
 		c.getItems().deleteItem2(995, 350000);
 		c.getItems().addItem(10828, 1);
 		c.getItems().addItem(1127, 1);
 		c.getItems().addItem(1079, 1);
 		c.getItems().addItem(3842, 1);
 		c.getItems().addItem(4587, 1);
 		c.getItems().addItem(1231, 1);
 		c.getItems().addItem(1725, 1);
 		c.getItems().addItem(3105, 1);
 		c.getItems().addItem(2550, 1);
 		} else {
 		c.sendMessage("You need atleast 350,000 coins to use this feature.");
 		}
 		break;
 			case 46230:
 		c.getItems().addItem(10828, 1);
 		c.getItems().addItem(10551, 1);
 		c.getItems().addItem(4087, 1);
 		c.getItems().addItem(11732, 1);
 		c.getItems().addItem(13006, 1);
 		c.getItems().addItem(1725, 1);
 		c.getItems().addItem(6737, 1);
 		c.getItems().addItem(8850, 1);
 		c.getItems().addItem(4151, 1);
 		c.getItems().addItem(995, 50000000);
                 c.getPA().showInterface(3559);
 				c.getPA().addSkillXP((15000000), 0);
 				c.getPA().addSkillXP((15000000), 1);
 				c.getPA().addSkillXP((15000000), 2);
 				c.getPA().addSkillXP((15000000), 3);
 				c.getPA().addSkillXP((15000000), 4);
 				c.getPA().addSkillXP((15000000), 5);
 				c.getPA().addSkillXP((15000000), 6);
 				c.playerXP[3] = c.getPA().getXPForLevel(99)+5;
 				c.playerLevel[3] = c.getPA().getLevelForXP(c.playerXP[3]);
 				c.getPA().refreshSkill(3);
 				c.puremaster = 1;
 			break;
 			case 46234:
 		c.getItems().addItem(10941, 1);
 		c.getItems().addItem(10939, 1);
 		c.getItems().addItem(10940, 1);
 		c.getItems().addItem(10933, 1);
 		c.getItems().addItem(18508, 1);
 		c.getItems().addItem(2462, 1);
 		c.getItems().addItem(995, 50000000);
                 c.getPA().showInterface(3559);
 			break;
 			case 46227:
 		c.getItems().addItem(12222, 1);
 		c.getItems().addItem(6107, 1);
 		c.getItems().addItem(2497, 1);
 		c.getItems().addItem(3105, 1);
 		c.getItems().addItem(12988, 1);
 		c.getItems().addItem(10498, 1);
 		c.getItems().addItem(1725, 1);
 		c.getItems().addItem(861, 1);
 		c.getItems().addItem(4151, 1);
 		c.getItems().addItem(892, 1000);
 		c.getItems().addItem(995, 50000000);
                 c.getPA().showInterface(3559);
 				c.getPA().addSkillXP((15000000), 0);
 				c.getPA().addSkillXP((15000000), 2);
 				c.getPA().addSkillXP((15000000), 3);
 				c.getPA().addSkillXP((15000000), 4);
 				c.getPA().addSkillXP((15000000), 6);
 				c.playerXP[3] = c.getPA().getXPForLevel(99)+5;
 				c.playerLevel[3] = c.getPA().getLevelForXP(c.playerXP[3]);
 				c.getPA().refreshSkill(3);
 				c.puremaster = 1;
 			break;
 			
 					case 114113://mage set
 			 if (c.inWild() && c.isBanking) {
 				c.sendMessage("You cannot do this right now");
 			} else if(c.getItems().freeSlots() <= 7) {
 				c.sendMessage("You need atleast 7 free slot's to use this feature.");
 			} else if (c.getItems().playerHasItem(995, 300000)) {
 				c.getItems().deleteItem2(995, 300000);
 				c.getItems().addItem(4091, 1);
 				c.getItems().addItem(4093, 1);
 				c.getItems().addItem(3755, 1);
 				c.getItems().addItem(2550, 1);
 				c.getItems().addItem(1704, 1);
 				c.getItems().addItem(3842, 1);
 				c.getItems().addItem(4675, 1);
 			} else {
 				c.sendMessage("You need atleast 300,000 coins to use this feature.");
 			}
 			break;
 			
 								case 114114://range set
 			 if (c.inWild() && c.isBanking) {
 				c.sendMessage("You cannot do this right now");
 			} else if(c.getItems().freeSlots() <= 13) {
 				c.sendMessage("You need atleast 13 free slot's to use this feature.");
 			} else if (c.getItems().playerHasItem(995, 450000)) {
 				c.getItems().deleteItem2(995, 450000);
 				c.getItems().addItem(3749, 1);
 				c.getItems().addItem(1704, 1);
 				c.getItems().addItem(2503, 1);
 				c.getItems().addItem(2497, 1);
 				c.getItems().addItem(2491, 1);
 				c.getItems().addItem(6328, 1);
 				c.getItems().addItem(2550, 1);
 				c.getItems().addItem(9185, 1);
 				c.getItems().addItem(9243, 100);
 				c.getItems().addItem(10499, 1);
 				c.getItems().addItem(861, 1);
 				c.getItems().addItem(892, 100);
 			} else {
 				c.sendMessage("You need atleast 450,000 coins to use this feature.");
 			}
 			break;
 			
 			case 114115://hybrid set
 			if (c.inWild() && c.isBanking) {
 				c.sendMessage("You cannot do this right now");
 			} else if(c.getItems().freeSlots() <= 14) {
 				c.sendMessage("You need atleast 14 free slot's to use this feature.");
 			} else if (c.getItems().playerHasItem(995, 450000)) {
 				c.getItems().deleteItem2(995, 450000);
 				c.getItems().addItem(555, 300);
 				c.getItems().addItem(560, 200);
 				c.getItems().addItem(565, 100);
 				c.getItems().addItem(4675, 1);
 				c.getItems().addItem(2497, 1);
 				c.getItems().addItem(2415, 1);
 				c.getItems().addItem(10828, 1);
 				c.getItems().addItem(3841, 1);
 				c.getItems().addItem(2503, 1);
 				c.getItems().addItem(7460, 1);
 				c.getItems().addItem(1704, 1);
 				c.getItems().addItem(2550, 1);
 				c.getItems().addItem(4091, 1);
 				c.getItems().addItem(4093, 1);
 				c.getItems().addItem(3105, 1);
 			} else {
 				c.sendMessage("You need atleast 450,000 coins to use this feature.");
 			}
 			break;
 			
 						case 114118://runes set
 			if (c.inWild() && c.isBanking) {
 				c.sendMessage("You cannot do this right now");
 			} else if(c.getItems().freeSlots() <= 10) {
 				c.sendMessage("You need atleast 10 free slot's to use this feature.");
 			} else if (c.getItems().playerHasItem(995, 300000)) {
 				c.getItems().deleteItem2(995, 300000);
 				c.getItems().addItem(560,1000);
 				c.getItems().addItem(555,1000);
 				c.getItems().addItem(565,1000);
 				c.getItems().addItem(9075,1000);
 				c.getItems().addItem(557,1000);
 				c.getItems().addItem(556,1000);
 				c.getItems().addItem(554,1000);
 				c.getItems().addItem(562,1000);
 				c.getItems().addItem(561,1000);
 				c.getItems().addItem(563,1000);
 			} else {
 				c.sendMessage("You need atleast 300,000 coins to use this feature.");
 			}
 			break;
 			
 									case 114119://barrage set
 			if (c.inWild() && c.isBanking) {
 				c.sendMessage("You cannot do this right now");
 			} else if(c.getItems().freeSlots() <= 3) {
 				c.sendMessage("You need atleast 3 free slot's to use this feature.");
 			} else if (c.getItems().playerHasItem(995, 2000000)) {
 				c.getItems().deleteItem2(995, 2000000);
 				c.getItems().addItem(555,6000);
 				c.getItems().addItem(560,4000);
 				c.getItems().addItem(565,2000);
 			} else {
 				c.sendMessage("You need atleast 2,000,000 coins to use this feature.");
 			}
 			break;
 			
 			case 114120://veng set
 			if (c.inWild() && c.isBanking) {
 				c.sendMessage("You cannot do this right now");
 			} else if(c.getItems().freeSlots() <= 3) {
 				c.sendMessage("You need atleast 3 free slot's to use this feature.");
 			} else if (c.getItems().playerHasItem(995, 100000)) {
 				c.getItems().deleteItem2(995, 100000);
 				c.getItems().addItem(557,1000);
 				c.getItems().addItem(560,200);
 				c.getItems().addItem(9075,400);
 			} else {
 				c.sendMessage("You need atleast 100,000 coins to use this feature.");
 			}
 			break;
 			
 			case 114123://shark set
 			if (c.inWild() && c.isBanking) {
 				c.sendMessage("You cannot do this right now");
 			} else if(c.getItems().freeSlots() <= 1) {
 				c.sendMessage("You need atleast 1 free slot's to use this feature.");
 			} else if (c.getItems().playerHasItem(995, 100000)) {
 				c.getItems().deleteItem2(995, 100000);
 				c.getItems().addItem(385,1000);
 			} else {
 				c.sendMessage("You need atleast 100,000 coins to use this feature.");
 			}
 			break;
 			
 						case 114124://tuna pot set
 			if (c.inWild() && c.isBanking) {
 				c.sendMessage("You cannot do this right now");
 			} else if(c.getItems().freeSlots() <= 1) {
 				c.sendMessage("You need atleast 1 free slot's to use this feature.");
 			} else if (c.getItems().playerHasItem(995, 150000)) {
 				c.getItems().deleteItem2(995, 150000);
 				c.getItems().addItem(7060,1000);
 			} else {
 				c.sendMessage("You need atleast 150,000 coins to use this feature.");
 			}
 			break;
 			
 			case 114125://super set
 			if (c.inWild() && c.isBanking) {
 				c.sendMessage("You cannot do this right now");
 			} else if(c.getItems().freeSlots() <= 1) {
 				c.sendMessage("You need atleast 1 free slot's to use this feature.");
 			} else if (c.getItems().playerHasItem(995, 80000)) {
 				c.getItems().deleteItem2(995, 80000);
 				c.getItems().addItem(146,100);
 				c.getItems().addItem(158,100);
 				c.getItems().addItem(164,100);
 			} else {
 				c.sendMessage("You need atleast 80,000 coins to use this feature.");
 			}
 			break;
 			
 						case 114126://super restores biatch
 			if (c.inWild() && c.isBanking) {
 				c.sendMessage("You cannot do this right now");
 			} else if(c.getItems().freeSlots() <= 1) {
 				c.sendMessage("You need atleast 1 free slot's to use this feature.");
 			} else if (c.getItems().playerHasItem(995, 30000)) {
 				c.getItems().deleteItem2(995, 30000);
 				c.getItems().addItem(3025,100);
 			} else {
 				c.sendMessage("You need atleast 30,000 coins to use this feature.");
 			}
 			break;
 			
 									case 114127://mage pots
 			if (c.inWild() && c.isBanking) {
 				c.sendMessage("You cannot do this right now");
 			} else if(c.getItems().freeSlots() <= 1) {
 				c.sendMessage("You need atleast 1 free slot's to use this feature.");
 			} else if (c.getItems().playerHasItem(995, 30000)) {
 				c.getItems().deleteItem2(995, 30000);
 				c.getItems().addItem(3041,100);
 			} else {
 				c.sendMessage("You need atleast 30,000 coins to use this feature.");
 			}
 			break;
 			
 									case 114128://range pots
 			if (c.inWild() && c.isBanking) {
 				c.sendMessage("You cannot do this right now");
 			} else if(c.getItems().freeSlots() <= 1) {
 				c.sendMessage("You need atleast 1 free slot's to use this feature.");
 			} else if (c.getItems().playerHasItem(995, 36000)) {
 				c.getItems().deleteItem2(995, 36000);
 				c.getItems().addItem(2445,100);
 			} else {
 				c.sendMessage("You need atleast 36,000 coins to use this feature.");
 			}
 			break;*/
 			
 			
 			
 
 
 		
 			
 		case 17111://stop viewing viewing orb
                 c.setSidebarInterface(10, 2449);
                 c.viewingOrb = false;
                 c.teleportToX = 2399;
                 c.teleportToY = 5171;
                 c.appearanceUpdateRequired = true;
                 c.updateRequired = true;
                 break;
 
             case 59139://viewing orb southwest
                 c.viewingOrb = true;
                 c.teleportToX = 2388;
                 c.teleportToY = 5138;
                 c.appearanceUpdateRequired = true;
                 c.updateRequired = true;
                 break;
 
             case 59138://viewing orb southeast
                 c.viewingOrb = true;
                 c.teleportToX = 2411;
                 c.teleportToY = 5137;
                 c.appearanceUpdateRequired = true;
                 c.updateRequired = true;
                 break;
 
             case 59137://viewing orb northeast
                 c.viewingOrb = true;
                 c.teleportToX = 2409;
                 c.teleportToY = 5158;
                 c.appearanceUpdateRequired = true;
                 c.updateRequired = true;
                 break;
 
             case 59136://viewing orb northwest
                 c.viewingOrb = true;
                 c.teleportToX = 2384;
                 c.teleportToY = 5157;
                 c.appearanceUpdateRequired = true;
                 c.updateRequired = true;
                 break;
 
             case 59135://viewing orb middle
                 c.viewingOrb = true;
                 c.teleportToX = 2398;
                 c.teleportToY = 5150;
                 c.appearanceUpdateRequired = true;
                 c.updateRequired = true;
                 break;
 			case 107229:
 				if (c.isDonator == 1 && c.inGWD()) {
 				c.Arma = 15;
 				c.Band = 15;
 				c.Sara = 15;
 				c.Zammy = 15;
 					c.sendMessage("Your magical donator rank forces your KC to raise to 15!");
 				} else {
 					c.sendMessage("You must be a donator and be in godwars dungeon to use this!");
 				} 
 			break;
 
 				case 108003:
 				if (c.isDonator == 1) {
 				c.setSidebarInterface(4, 27620);
 				} else {
 				c.sendMessage("You must be an donator to view this tab!");
 				return;				
 				}
 				break;
 
 				case 107231:
 				if (c.isDonator == 1) {
 				c.getPA().spellTeleport(2036, 4525, 0);
 				c.sendMessage("You teleported to donator-zone a place to chill/relax, theres also alot of benefits.");
 				} else {
 				c.sendMessage("You must be an donator to teleport to the donator-zone!");
 				return;				
 				}
 				break;
 			case 108006:
 				//if (c.xpLock == false) {
 					//c.xpLock = true;
 					//c.sendMessage("Your XP are now LOCKED!");
 				//} else {
 				//	c.xpLock = false;
 				//	c.sendMessage("Your XP are now UNLOCKED!");
 				//} 
 						c.StartBestItemScan();
 		c.EquipStatus = 0;
 		for (int k = 0; k < 4; k++)
 			c.getPA().sendFrame34a(10494, -1, k, 1);
 		for (int k = 0; k < 39; k++)
 			c.getPA().sendFrame34a(10600, -1, k, 1);
 		if(c.WillKeepItem1 > 0)
 			c.getPA().sendFrame34a(10494, c.WillKeepItem1, 0, c.WillKeepAmt1);
 		if(c.WillKeepItem2 > 0)
 			c.getPA().sendFrame34a(10494, c.WillKeepItem2, 1, c.WillKeepAmt2);
 		if(c.WillKeepItem3 > 0)
 			c.getPA().sendFrame34a(10494, c.WillKeepItem3, 2, c.WillKeepAmt3);
 		if(c.WillKeepItem4 > 0)
 			c.getPA().sendFrame34a(10494, c.WillKeepItem4, 3, 1);
 		for(int ITEM = 0; ITEM < 28; ITEM++){
 			if(c.playerItems[ITEM]-1 > 0 && !(c.playerItems[ITEM]-1 == c.WillKeepItem1 && ITEM == c.WillKeepItem1Slot)
  			&& !(c.playerItems[ITEM]-1 == c.WillKeepItem2 && ITEM == c.WillKeepItem2Slot)
  			&& !(c.playerItems[ITEM]-1 == c.WillKeepItem3 && ITEM == c.WillKeepItem3Slot)
  			&& !(c.playerItems[ITEM]-1 == c.WillKeepItem4 && ITEM == c.WillKeepItem4Slot)){
 				c.getPA().sendFrame34a(10600, c.playerItems[ITEM]-1, c.EquipStatus, c.playerItemsN[ITEM]);
 				c.EquipStatus += 1;
 			} else if(c.playerItems[ITEM]-1 > 0 && (c.playerItems[ITEM]-1 == c.WillKeepItem1 && ITEM == c.WillKeepItem1Slot) && c.playerItemsN[ITEM] > c.WillKeepAmt1){
 				c.getPA().sendFrame34a(10600, c.playerItems[ITEM]-1, c.EquipStatus, c.playerItemsN[ITEM]-c.WillKeepAmt1);
 				c.EquipStatus += 1;
 			} else if(c.playerItems[ITEM]-1 > 0 && (c.playerItems[ITEM]-1 == c.WillKeepItem2 && ITEM == c.WillKeepItem2Slot) && c.playerItemsN[ITEM] > c.WillKeepAmt2){
 				c.getPA().sendFrame34a(10600, c.playerItems[ITEM]-1, c.EquipStatus, c.playerItemsN[ITEM]-c.WillKeepAmt2);
 				c.EquipStatus += 1;
 			} else if(c.playerItems[ITEM]-1 > 0 && (c.playerItems[ITEM]-1 == c.WillKeepItem3 && ITEM == c.WillKeepItem3Slot) && c.playerItemsN[ITEM] > c.WillKeepAmt3){
 				c.getPA().sendFrame34a(10600, c.playerItems[ITEM]-1, c.EquipStatus, c.playerItemsN[ITEM]-c.WillKeepAmt3);
 				c.EquipStatus += 1;
 			} else if(c.playerItems[ITEM]-1 > 0 && (c.playerItems[ITEM]-1 == c.WillKeepItem4 && ITEM == c.WillKeepItem4Slot) && c.playerItemsN[ITEM] > 1){
 				c.getPA().sendFrame34a(10600, c.playerItems[ITEM]-1, c.EquipStatus, c.playerItemsN[ITEM]-1);
 				c.EquipStatus += 1;
 			}
 		}
 		for(int EQUIP = 0; EQUIP < 14; EQUIP++){
 			if(c.playerEquipment[EQUIP] > 0 && !(c.playerEquipment[EQUIP] == c.WillKeepItem1 && EQUIP+28 == c.WillKeepItem1Slot)
 			&& !(c.playerEquipment[EQUIP] == c.WillKeepItem2 && EQUIP+28 == c.WillKeepItem2Slot)
 			&& !(c.playerEquipment[EQUIP] == c.WillKeepItem3 && EQUIP+28 == c.WillKeepItem3Slot)
 			&& !(c.playerEquipment[EQUIP] == c.WillKeepItem4 && EQUIP+28 == c.WillKeepItem4Slot)){
 				c.getPA().sendFrame34a(10600, c.playerEquipment[EQUIP], c.EquipStatus, c.playerEquipmentN[EQUIP]);
 				c.EquipStatus += 1;
 			} else if(c.playerEquipment[EQUIP] > 0 && (c.playerEquipment[EQUIP] == c.WillKeepItem1 && EQUIP+28 == c.WillKeepItem1Slot) && c.playerEquipmentN[EQUIP] > 1 && c.playerEquipmentN[EQUIP]-c.WillKeepAmt1 > 0){
 				c.getPA().sendFrame34a(10600, c.playerEquipment[EQUIP], c.EquipStatus, c.playerEquipmentN[EQUIP]-c.WillKeepAmt1);
 				c.EquipStatus += 1;
 			} else if(c.playerEquipment[EQUIP] > 0 && (c.playerEquipment[EQUIP] == c.WillKeepItem2 && EQUIP+28 == c.WillKeepItem2Slot) && c.playerEquipmentN[EQUIP] > 1 && c.playerEquipmentN[EQUIP]-c.WillKeepAmt2 > 0){
 				c.getPA().sendFrame34a(10600, c.playerEquipment[EQUIP], c.EquipStatus, c.playerEquipmentN[EQUIP]-c.WillKeepAmt2);
 				c.EquipStatus += 1;
 			} else if(c.playerEquipment[EQUIP] > 0 && (c.playerEquipment[EQUIP] == c.WillKeepItem3 && EQUIP+28 == c.WillKeepItem3Slot) && c.playerEquipmentN[EQUIP] > 1 && c.playerEquipmentN[EQUIP]-c.WillKeepAmt3 > 0){
 				c.getPA().sendFrame34a(10600, c.playerEquipment[EQUIP], c.EquipStatus, c.playerEquipmentN[EQUIP]-c.WillKeepAmt3);
 				c.EquipStatus += 1;
 			} else if(c.playerEquipment[EQUIP] > 0 && (c.playerEquipment[EQUIP] == c.WillKeepItem4 && EQUIP+28 == c.WillKeepItem4Slot) && c.playerEquipmentN[EQUIP] > 1 && c.playerEquipmentN[EQUIP]-1 > 0){
 				c.getPA().sendFrame34a(10600, c.playerEquipment[EQUIP], c.EquipStatus, c.playerEquipmentN[EQUIP]-1);
 				c.EquipStatus += 1;
 			}
 		}
 
 	          	c.ResetKeepItems();
 				c.getPA().showInterface(17100);
 			break;
 
 
 			//SUMMONING INTERFACE
 			case 122114://next
 			c.getPA().showInterface(31550);
 			break;
 			case 123058://previous
 			c.getPA().showInterface(31350);
 			break;
 
 
 		case 122119://Spirit Wolf
 		if (c.playerLevel[22] < 1) {
 				c.sendMessage("You need a level 1 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 7) && c.getItems().playerHasItem(12158, 1) && c.getItems().playerHasItem(2859, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 7);
 			c.getItems().deleteItem2(12158, 1);
 			c.getItems().deleteItem2(2859, 1);
 			c.getPA().closeAllWindows();
 			c.getPA().addSkillXP(5 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(12047, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122122://spirit spider
 		if (c.playerLevel[22] < 10) {
 				c.sendMessage("You need a level 10 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 8) && c.getItems().playerHasItem(12158, 1) && c.getItems().playerHasItem(6291, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 8);
 			c.getItems().deleteItem2(12158, 1);
 			c.getItems().deleteItem2(6291, 1);
 			c.getPA().addSkillXP(12 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8379, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122125://Thorny snail
 		if (c.playerLevel[22] < 13) {
 				c.sendMessage("You need a level 13 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 9) && c.getItems().playerHasItem(8341, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 9);
 			c.getItems().deleteItem2(8341, 1);
 			c.getPA().addSkillXP(23 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8360, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122128://granite crab
 		if (c.playerLevel[22] < 13) {
 				c.sendMessage("You need a level 13 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 7) && c.getItems().playerHasItem(8341, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 7);
 			c.getItems().deleteItem2(8341, 1);
 			c.getPA().addSkillXP(21 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8355, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122131://spirit mosquito
 		if (c.playerLevel[22] < 17) {
 				c.sendMessage("You need a level 17 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 1) && c.getItems().playerHasItem(8341, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 1);
 			c.getItems().deleteItem2(8341, 1);
 			c.getPA().addSkillXP(46 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8481, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122134://desert wyrm
 		if (c.playerLevel[22] < 18) {
 				c.sendMessage("You need a level 18 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 45) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 45);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(31 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8374, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122137://spirit scorpion
 		if (c.playerLevel[22] < 19) {
 				c.sendMessage("You need a level 19 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 57) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 57);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(82 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8377, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122140://Spirit Tz-Kih
 		if (c.playerLevel[22] < 22) {
 				c.sendMessage("You need a level 22 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 64) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 64);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(96 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8496, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122143://Compost mound
 		if (c.playerLevel[22] < 28) {
 				c.sendMessage("You need a level 28 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 47) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 47);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(46 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8395, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122146://vampire bat
 		if (c.playerLevel[22] < 31) {
 				c.sendMessage("You need a level 31 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 81) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 81);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(136 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8376, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122149://honey badger
 		if (c.playerLevel[22] < 32) {
 				c.sendMessage("You need a level 32 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 84) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 84);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(140 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8382, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122152://beaver
 		if (c.playerLevel[22] < 33) {
 				c.sendMessage("You need a level 33 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 72) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 72);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(57 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8361, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122155://Void shifter
 		if (c.playerLevel[22] < 34) {
 				c.sendMessage("You need a level 34 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 74) && c.getItems().playerHasItem(8346, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 74);
 			c.getItems().deleteItem2(8346, 1);
 			c.getPA().addSkillXP(59 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8598, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122158://Void torcher
 		if (c.playerLevel[22] < 34) {
 				c.sendMessage("You need a level 34 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 74) && c.getItems().playerHasItem(8346, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 74);
 			c.getItems().deleteItem2(8346, 1);
 			c.getPA().addSkillXP(59 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8491, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122161://Void spinner
 		if (c.playerLevel[22] < 34) {
 				c.sendMessage("You need a level 34 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 74) && c.getItems().playerHasItem(8346, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 74);
 			c.getItems().deleteItem2(8346, 1);
 			c.getPA().addSkillXP(59 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8482, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122164://bull ant
 		if (c.playerLevel[22] < 40) {
 				c.sendMessage("You need a level 40 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 11) && c.getItems().playerHasItem(8341, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 11);
 			c.getItems().deleteItem2(8341, 1);
 			c.getPA().addSkillXP(52 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8393, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122167://Void ravager
 		if (c.playerLevel[22] < 34) {
 				c.sendMessage("You need a level 34 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 74) && c.getItems().playerHasItem(8346, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 74);
 			c.getItems().deleteItem2(8346, 1);
 			c.getPA().addSkillXP(59 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8600, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122170://evil turnip
 		if (c.playerLevel[22] < 42) {
 				c.sendMessage("You need a level 42 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 104) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 104);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(184 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8375, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122173://spirit cockatrice
 		if (c.playerLevel[22] < 43) {
 				c.sendMessage("You need a level 43 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 88) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 88);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(75 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8397, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122176://spirit guthatrice
 		if (c.playerLevel[22] < 43) {
 				c.sendMessage("You need a level 43 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 88) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 88);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(75 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8398, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122179://spirit saratrice
 		if (c.playerLevel[22] < 43) {
 				c.sendMessage("You need a level 43 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 88) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 88);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(75 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8399, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122182://spirit zamatrice
 		if (c.playerLevel[22] < 43) {
 				c.sendMessage("You need a level 43 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 88) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 88);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(75 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8400, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122185://spirit pengatrice
 		if (c.playerLevel[22] < 43) {
 				c.sendMessage("You need a level 43 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 88) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 88);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(75 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8401, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122188://spirit coraxatrice
 		if (c.playerLevel[22] < 43) {
 				c.sendMessage("You need a level 43 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 88) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 88);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(75 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8402, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 
 		case 122191://spirit vulatrice
 		if (c.playerLevel[22] < 43) {
 				c.sendMessage("You need a level 43 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 88) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 88);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(75 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8403, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122194://bloated leech
 		if (c.playerLevel[22] < 49) {
 				c.sendMessage("You need a level 49 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 117) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 117);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(215 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8380, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122197://spirit terrorbird
 		if (c.playerLevel[22] < 52) {
 				c.sendMessage("You need a level 52 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 12) && c.getItems().playerHasItem(8341, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 12);
 			c.getItems().deleteItem2(8341, 1);
 			c.getPA().addSkillXP(68 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8354, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122200://abyssal parasite
 		if (c.playerLevel[22] < 54) {
 				c.sendMessage("You need a level 54 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 106) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 106);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(94 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8368, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122203://spirit jelly
 		if (c.playerLevel[22] < 55) {
 				c.sendMessage("You need a level 55 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 151) && c.getItems().playerHasItem(8346, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 151);
 			c.getItems().deleteItem2(8346, 1);
 			c.getPA().addSkillXP(484 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8364, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 122206://ibis
 		if (c.playerLevel[22] < 56) {
 				c.sendMessage("You need a level 56 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 109) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 109);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(98 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8477, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123063://spirit graahk
 		if (c.playerLevel[22] < 57) {
 				c.sendMessage("You need a level 57 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 154) && c.getItems().playerHasItem(8346, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 154);
 			c.getItems().deleteItem2(8346, 1);
 			c.getPA().addSkillXP(501 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8596, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 
 		case 123066://spirit kyatt
 		if (c.playerLevel[22] < 57) {
 				c.sendMessage("You need a level 57 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 153) && c.getItems().playerHasItem(8346, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 153);
 			c.getItems().deleteItem2(8346, 1);
 			c.getPA().addSkillXP(501 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8498, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123069://spirit larupia
 		if (c.playerLevel[22] < 57) {
 				c.sendMessage("You need a level 57 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 155) && c.getItems().playerHasItem(8346, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 155);
 			c.getItems().deleteItem2(8346, 1);
 			c.getPA().addSkillXP(501 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8484, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 
 		case 123072://Karamthulhu overlord
 		if (c.playerLevel[22] < 58) {
 				c.sendMessage("You need a level 58 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 144) && c.getItems().playerHasItem(8346, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 144);
 			c.getItems().deleteItem2(8346, 1);
 			c.getPA().addSkillXP(510 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8362, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123075://Smoke devil
 		if (c.playerLevel[22] < 61) {
 				c.sendMessage("You need a level 61 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 141) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 141);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(268 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8392, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123078://stranger plant
 		if (c.playerLevel[22] < 64) {
 				c.sendMessage("You need a level 64 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 128) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 128);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(281 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8373, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123081://war tortoise
 		if (c.playerLevel[22] < 67) {
 				c.sendMessage("You need a level 67 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 1) && c.getItems().playerHasItem(8341, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 1);
 			c.getItems().deleteItem2(8341, 1);
 			c.getPA().addSkillXP(58 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8366, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123084://fruit bat
 		if (c.playerLevel[22] < 69) {
 				c.sendMessage("You need a level 69 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 130) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 130);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(121 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8367, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123087://ravenous locust
 		if (c.playerLevel[22] < 70) {
 				c.sendMessage("You need a level 70 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 79) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 79);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(132 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8502, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123090://arctic bear
 		if (c.playerLevel[22] < 71) {
 				c.sendMessage("You need a level 71 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 14) && c.getItems().playerHasItem(8341, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 14);
 			c.getItems().deleteItem2(8341, 1);
 			c.getPA().addSkillXP(93 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8378, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123093://obsidian golem
 		if (c.playerLevel[22] < 73) {
 				c.sendMessage("You need a level 73 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 195) && c.getItems().playerHasItem(8346, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 195);
 			c.getItems().deleteItem2(8346, 1);
 			c.getPA().addSkillXP(642 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8488, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123096://praying mantis
 		if (c.playerLevel[22] < 75) {
 				c.sendMessage("You need a level 75 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 168) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 168);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(329 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8356, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123099://forge regent
 		if (c.playerLevel[22] < 76) {
 				c.sendMessage("You need a level 76 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 141) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 141);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(134 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8483, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123102://talon beast
 		if (c.playerLevel[22] < 77) {
 				c.sendMessage("You need a level 77 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 174) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 174);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(1015 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8489, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123105://giant ent
 		if (c.playerLevel[22] < 78) {
 				c.sendMessage("You need a level 78 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 124) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 124);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(136 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8357, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123108://fire titan
 		if (c.playerLevel[22] < 79) {
 				c.sendMessage("You need a level 79 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 198) && c.getItems().playerHasItem(8346, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 198);
 			c.getItems().deleteItem2(8346, 1);
 			c.getPA().addSkillXP(695 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8493, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123111://moss titan
 		if (c.playerLevel[22] < 79) {
 				c.sendMessage("You need a level 79 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 202) && c.getItems().playerHasItem(8346, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 202);
 			c.getItems().deleteItem2(8346, 1);
 			c.getPA().addSkillXP(695 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8494, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123114://hydra
 		if (c.playerLevel[22] < 80) {
 				c.sendMessage("You need a level 80 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 128) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 128);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(140 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8363, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123117://spirit dagannoth
 		if (c.playerLevel[22] < 83) {
 				c.sendMessage("You need a level 83 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 1) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 1);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(164 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8359, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123120://lava titan
 		if (c.playerLevel[22] < 83) {
 				c.sendMessage("You need a level 83 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 219) && c.getItems().playerHasItem(8346, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 219);
 			c.getItems().deleteItem2(8346, 1);
 			c.getPA().addSkillXP(730 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8486, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123123://swamp titan
 		if (c.playerLevel[22] < 85) {
 				c.sendMessage("You need a level 85 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 1) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 150);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(373 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8480, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123126://rune minotaur
 		if (c.playerLevel[22] < 86) {
 				c.sendMessage("You need a level 86 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 1) && c.getItems().playerHasItem(8346, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 1);
 			c.getItems().deleteItem2(8346, 1);
 			c.getPA().addSkillXP(756 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8391, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123129://unicorn stallion
 		if (c.playerLevel[22] < 88) {
 				c.sendMessage("You need a level 88 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 140) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 140);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(154 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8370, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123132://geyser titan
 		if (c.playerLevel[22] < 89) {
 				c.sendMessage("You need a level 89 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 222) && c.getItems().playerHasItem(8346, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 222);
 			c.getItems().deleteItem2(8346, 1);
 			c.getPA().addSkillXP(783 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8485, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123135://wolpertinger
 		if (c.playerLevel[22] < 92) {
 				c.sendMessage("You need a level 92 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 203) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 203);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(404 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8394, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123138://abyssal titan
 		if (c.playerLevel[22] < 93) {
 				c.sendMessage("You need a level 93 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 112) && c.getItems().playerHasItem(8342, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 112);
 			c.getItems().deleteItem2(8342, 1);
 			c.getPA().addSkillXP(163 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8490, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123141://iron titan
 		if (c.playerLevel[22] < 95) {
 				c.sendMessage("You need a level 95 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 198) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 198);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(417 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8602, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123144://pack yak
 		if (c.playerLevel[22] < 96) {
 				c.sendMessage("You need a level 96 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 211) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 211);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(422 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8396, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 		
 		case 123147://steel titan
 		if (c.playerLevel[22] < 98) {
 				c.sendMessage("You need a level 98 Summoning to do this.");
 				return;
 				}
 		if (c.getItems().playerHasItem(12155, 1) && c.getItems().playerHasItem(18016, 178) && c.getItems().playerHasItem(8343, 1)) {
 			c.getItems().deleteItem2(12155, 1);
 			c.getItems().deleteItem2(18016, 178);
 			c.getItems().deleteItem2(8343, 1);
 			c.getPA().addSkillXP(435 * Config.SUMMONING_EXPERIENCE, 23);
 			c.getItems().addItem(8487, 1);
 			c.sendMessage("You created a Summoning Pouch.");
 			c.startAnimation(10530);
 			c.gfx0(1549);
 		} else {
 			c.sendMessage("You don't have the required items.");
 		}
 		break;
 
 		case 123150://unknown
 		if (c.playerLevel[22] < 99) {
 			c.sendMessage("You need a level 99 Summoning to do this.");
 			return;
 			}
 		c.sendMessage("This is still unknown...");
 		c.startAnimation(10530);
 		c.gfx0(1549);
 		break;
 
 		//END OF SUMMONING
 			case 107230:
 			if(c.isDonator == 0 || c.inWild()) {
 			c.sendMessage("You must be outside wilderness and be a donator to use this!");
 			return;
 			}
 			if (c.playerMagicBook == 0 && c.isDonator == 1 && !c.inWild()) {
 				c.playerMagicBook = 1;
 				c.setSidebarInterface(6, 12855);
 				c.setSidebarInterface(0, 328);
 				c.sendMessage("An ancient wisdomin fills your mind.");
 				c.getPA().resetAutocast();
 				return;
 			}	
 			if (c.playerMagicBook == 1 && c.isDonator == 1 && !c.inWild()) {
 				c.playerMagicBook = 2;
 				c.setSidebarInterface(0, 328);
 				c.setSidebarInterface(6, 16640);
 				c.sendMessage("Your mind becomes stirred with thoughs of dreams.");
 				c.getPA().resetAutocast();
 				return;
 			}
 			if (c.playerMagicBook == 2 && c.isDonator == 1 && !c.inWild()) {
 				c.setSidebarInterface(6, 1151); //modern
 				c.playerMagicBook = 0;
 				c.setSidebarInterface(0, 328);
 				c.sendMessage("You feel a drain on your memory.");
 				c.autocastId = -1;
 				c.getPA().resetAutocast();
 				return;
 			}
 			break;
 					case 94142:
 if(c.lastsummon > 0) {
 c.firstslot();
 for(int i = 0; i < 29; i += 1)
 {
 Server.itemHandler.createGroundItem(c, c.storeditems[i], Server.npcHandler.npcs[c.summoningnpcid].absX, Server.npcHandler.npcs[c.summoningnpcid].absY, 1, c.playerId);
 c.storeditems[i] = -1;
 c.occupied[i] = false;
 }
 c.lastsummon = -1;
 c.totalstored = 0;
 c.summoningnpcid = 0;
 c.summoningslot = 0;
 c.sendMessage("Your BoB items have drop on the floor");
 } else {
 c.sendMessage("You do not have a npc currently spawned");
 }
 			//1st tele option
 			case 9190:
 				if (c.teleAction == 1) {
 					//rock crabs
 					c.getPA().spellTeleport(2676, 3715, 0);
 				} else if (c.teleAction == 2) {
 					//barrows
 					c.getPA().spellTeleport(3565, 3314, 0);
 				} else if (c.teleAction == 3) {
 					//godwars
 					c.sendMessage("You teleported to Godwars... If everything is black Logout then back in!");
 					c.getPA().spellTeleport(2882, 5310, 2);
 				} else if (c.teleAction == 4) {
 					//varrock wildy
 					c.getPA().spellTeleport(2539, 4716, 0);
 				} else if (c.teleAction == 5) {
 					c.getPA().spellTeleport(3046,9779,0);
 				} else if (c.teleAction == 20) {
 					//lum
 					c.getPA().spellTeleport(3222, 3218, 0);//3222 3218 
 				}
 				if (c.dialogueAction == 10) {
 					c.getPA().spellTeleport(2845, 4832, 0);
 					c.dialogueAction = -1;
 				} else if (c.dialogueAction == 11) {
 					c.getPA().spellTeleport(2786, 4839, 0);
 					c.dialogueAction = -1;
 				} else if (c.dialogueAction == 12) {
 					c.getPA().spellTeleport(2398, 4841, 0);
 					c.dialogueAction = -1;
 				}
 				break;
 				//mining - 3046,9779,0
 			//smithing - 3079,9502,0
 case 154:
 			if(System.currentTimeMillis() - c.logoutDelay < 8000) {
 c.sendMessage("You cannot do skillcape emotes in combat!");
 return;
 }
 		if(System.currentTimeMillis() - c.lastEmote >= 7000) {
 	if(c.getPA().wearingCape(c.playerEquipment[c.playerCape])) {
 		c.stopMovement();
 		c.gfx0(c.getPA().skillcapeGfx(c.playerEquipment[c.playerCape]));
 		c.startAnimation(c.getPA().skillcapeEmote(c.playerEquipment[c.playerCape]));
 	} else {
 		c.sendMessage("You must be wearing a Skillcape to do this emote.");
 }
 			c.lastEmote = System.currentTimeMillis();
 }
 break;
 			//2nd tele option
 			case 9191:
 				if (c.teleAction == 1) {
 					//tav dungeon
 					c.getPA().spellTeleport(2884, 9798, 0);
 				} else if (c.teleAction == 2) {
 					//pest control
 					c.getPA().spellTeleport(2662, 2650, 0);
 				} else if (c.teleAction == 3) {
 					//kbd
 					c.getPA().spellTeleport(3007, 3849, 0);
 				} else if (c.teleAction == 4) {
 					//graveyard
 					c.getPA().spellTeleport(2981, 3595, 0);
 				} else if (c.teleAction == 5) {
 					c.getPA().spellTeleport(3079,9502,0);
 				
 				} else if (c.teleAction == 20) {
 					c.getPA().spellTeleport(3210,3424,0);//3210 3424
 				}
 				if (c.dialogueAction == 10) {
 					c.getPA().spellTeleport(2796, 4818, 0);
 					c.dialogueAction = -1;
 				} else if (c.dialogueAction == 11) {
 					c.getPA().spellTeleport(2527, 4833, 0);
 					c.dialogueAction = -1;
 				} else if (c.dialogueAction == 12) {
 					c.getPA().spellTeleport(2464, 4834, 0);
 					c.dialogueAction = -1;
 				}
 				break;
 			//3rd tele option	
 
 			case 9192:
 				if (c.teleAction == 1) {
 					//slayer tower
 					c.getPA().spellTeleport(3428, 3537, 0);
 				} else if (c.teleAction == 2) {
 					//tzhaar
 					c.getPA().spellTeleport(2438, 5168, 0);
 					c.sendMessage("To fight Jad, enter the cave.");
 				} else if (c.teleAction == 3) {
 					//dag kings
 					c.getPA().spellTeleport(1910, 4367, 0);
 					c.sendMessage("Climb down the ladder to get into the lair.");
 				} else if (c.teleAction == 4) {
 					//Hillz
 						c.dialogueId = 51;
 					c.getDH().sendDialogues(c.dialogueId, 0);
 									
 				} else if (c.teleAction == 5) {
 					c.getPA().spellTeleport(2597,3408,0);
 				}
 				 else if (c.teleAction == 20) {
 					c.getPA().spellTeleport(2757,3477,0);
 				}
 
 				if (c.dialogueAction == 10) {
 					c.getPA().spellTeleport(2713, 4836, 0);
 					c.dialogueAction = -1;
 				} else if (c.dialogueAction == 11) {
 					c.getPA().spellTeleport(2162, 4833, 0);
 					c.dialogueAction = -1;
 				} else if (c.dialogueAction == 12) {
 					c.getPA().spellTeleport(2207, 4836, 0);
 					c.dialogueAction = -1;
 				}
 				break;
 			//4th tele option
 			case 9193:
 				if (c.teleAction == 1) {
 					//brimhaven dungeon
 					c.getPA().spellTeleport(2710, 9466, 0);
 					c.sendMessage("You teleported to brimhaven dungeon, be sure to bring antifire-shield.");
 				} else if (c.teleAction == 2) {
 					//duel arena
 					c.getPA().spellTeleport(3366, 3266, 0);
 				} else if (c.teleAction == 3) {
 					//chaos elemental
 					c.getPA().spellTeleport(2717, 9805, 0);
 				} else if (c.teleAction == 4) {
 					//Fala
 				c.getPA().spellTeleport(3086, 3516, 0);
 
 				} else if (c.teleAction == 5) {
 					c.getPA().spellTeleport(2724,3484,0);
 					c.sendMessage("For magic logs, try north of the duel arena.");
 				}
 				if (c.dialogueAction == 10) {
 					c.getPA().spellTeleport(2660, 4839, 0);
 					c.dialogueAction = -1;
 				} else if (c.dialogueAction == 11) {
 					//c.getPA().spellTeleport(2527, 4833, 0); astrals here
 					c.getRunecrafting().craftRunes(2489);
 					c.dialogueAction = -1;
 				} else if (c.dialogueAction == 12) {
 					//c.getPA().spellTeleport(2464, 4834, 0); bloods here
 					c.getRunecrafting().craftRunes(2489);
 					c.dialogueAction = -1;
 				
 				} else if (c.teleAction == 20) {
 					c.getPA().spellTeleport(2964,3378,0);
 				}
 				break;
 			case 9194:
 				if (c.teleAction == 1) {
 					//island
 					c.getPA().spellTeleport(3117, 9847, 0);
 				} else if (c.teleAction == 2) {
 					//last minigame spot
 					c.getPA().spellTeleport(2865,3546,0);
 					//c.getPA().closeAllWindows();
 				} else if (c.teleAction == 3) {
 					c.getPA().spellTeleport(3302,9372,0);
 					c.sendMessage("Enter the gate to fight the mighty Corporeal Beast!");
 					c.sendMessage("Note: Magic protect, Ruby bolts (e) and Diamond bolts (e) are recommended!");
 					c.getPA().closeAllWindows();
 				} else if (c.teleAction == 4) {
 					c.dialogueId = 50;
 					c.getDH().sendDialogues(c.dialogueId, 0);
 				} else if (c.teleAction == 5) {
 					c.getPA().spellTeleport(2812,3463,0);
 				}
 				if (c.dialogueAction == 10 || c.dialogueAction == 11) {
 					c.dialogueId++;
 					c.getDH().sendDialogues(c.dialogueId, 0);
 				} else if (c.dialogueAction == 12) {
 					c.dialogueId = 17;
 					c.getDH().sendDialogues(c.dialogueId, 0);
 				
 				} else if (c.teleAction == 20) {
 					c.getPA().spellTeleport(3506,3496,0);
 				}
 				break;
 			
 			case 71074:
 				if (c.clanId >= 0 && Server.clanChat.clans[c.clanId].owner.equalsIgnoreCase(c.playerName)) {
 					if (c.CSLS == 0) {
 		if(System.currentTimeMillis() - c.lastEmote >= 1500) {
 						Server.clanChat.clans[c.clanId].CS = 1;
 						Server.clanChat.sendLootShareMessage(c.clanId, "LootShare has been toggled to " + (!Server.clanChat.clans[c.clanId].lootshare ? "ON" : "OFF") + " by the clan leader.");
 						Server.clanChat.clans[c.clanId].lootshare = !Server.clanChat.clans[c.clanId].lootshare;
 						c.CSLS = 1;
 						Server.clanChat.updateClanChat(c.clanId);
 			c.lastEmote = System.currentTimeMillis();
 						return;
 				}	
 				}	
 					if (c.CSLS == 1) {
 		if(System.currentTimeMillis() - c.lastEmote >= 1500) {
 						c.CSLS = 2;
 						Server.clanChat.clans[c.clanId].CS = 2;
 						Server.clanChat.updateClanChat(c.clanId);
 						Server.clanChat.sendLootShareMessage(c.clanId, "LootShare has been toggled to " + (!Server.clanChat.clans[c.clanId].lootshare ? "ON" : "OFF") + " by the clan leader.");
 						Server.clanChat.clans[c.clanId].lootshare = !Server.clanChat.clans[c.clanId].lootshare;
 			c.lastEmote = System.currentTimeMillis();
 						return;
 
 				}	
 				}	
 					if (c.CSLS == 2) {
 		if(System.currentTimeMillis() - c.lastEmote >= 1500) {
 						if(Server.clanChat.clans[c.clanId].playerz == 1) {
 						c.sendMessage("There must be atleast 2 members in the clan chat to toggle Coinshare ON.");
 						c.CSLS = 0;
 						Server.clanChat.clans[c.clanId].CS = 0;
 						Server.clanChat.updateClanChat(c.clanId);
 			c.lastEmote = System.currentTimeMillis();
 						return;
 						}
 						c.CSLS = 3;
 						Server.clanChat.clans[c.clanId].CS = 3;
 						Server.clanChat.updateClanChat(c.clanId);
 						Server.clanChat.sendCoinShareMessage(c.clanId, "CoinShare has been toggled to " + (!Server.clanChat.clans[c.clanId].coinshare ? "ON" : "OFF") + " by the clan leader.");
 						Server.clanChat.clans[c.clanId].coinshare = !Server.clanChat.clans[c.clanId].coinshare;
 						return;
 
 				}	
 				}	
 					if (c.CSLS == 3) {
 		if(System.currentTimeMillis() - c.lastEmote >= 1500) {
 						c.CSLS = 0;
 						Server.clanChat.clans[c.clanId].CS = 0;
 						Server.clanChat.updateClanChat(c.clanId);
 						Server.clanChat.sendCoinShareMessage(c.clanId, "CoinShare has been toggled to " + (!Server.clanChat.clans[c.clanId].coinshare ? "ON" : "OFF") + " by the clan leader.");
 						Server.clanChat.clans[c.clanId].coinshare = !Server.clanChat.clans[c.clanId].coinshare;
 			c.lastEmote = System.currentTimeMillis();
 						return;
 				}	
 				}	
 					} else {
 						c.sendMessage("Only the owner of the clan has the power to do that.");
 				}	
 			break;
 			case 34185: case 34184: case 34183: case 34182: case 34189: case 34188: case 34187: case 34186: case 34193: case 34192: case 34191: case 34190:
 				if (c.craftingLeather)
 					c.getCrafting().handleCraftingClick(actionButtonId);
 				if (c.getFletching().fletching)
 					c.getFletching().handleFletchingClick(actionButtonId);
 			break;
 			
 			case 15147:
 				if (c.smeltInterface) {
 					c.smeltType = 2349;
 					c.smeltAmount = 1;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 						case 15146:
 				if (c.smeltInterface) {
 					c.smeltType = 2349;
 					c.smeltAmount = 5;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 									case 10247:
 				if (c.smeltInterface) {
 					c.smeltType = 2349;
 					c.smeltAmount = 10;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 												case 9110:
 				if (c.smeltInterface) {
 					c.smeltType = 2349;
 					c.smeltAmount = 28;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 			
 			case 15151:
 				if (c.smeltInterface) {
 					c.smeltType = 2351;
 					c.smeltAmount = 1;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 						case 15150:
 				if (c.smeltInterface) {
 					c.smeltType = 2351;
 					c.smeltAmount = 5;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 						case 15149:
 				if (c.smeltInterface) {
 					c.smeltType = 2351;
 					c.smeltAmount = 10;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 						case 15148:
 				if (c.smeltInterface) {
 					c.smeltType = 2351;
 					c.smeltAmount = 28;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 			
 			
 			case 15159:
 				if (c.smeltInterface) {
 					c.smeltType = 2353;
 					c.smeltAmount = 1;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 						case 15158:
 				if (c.smeltInterface) {
 					c.smeltType = 2353;
 					c.smeltAmount = 5;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 						case 15157:
 				if (c.smeltInterface) {
 					c.smeltType = 2353;
 					c.smeltAmount = 10;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 						case 15156:
 				if (c.smeltInterface) {
 					c.smeltType = 2353;
 					c.smeltAmount = 28;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 			
 			case 29017:
 				if (c.smeltInterface) {
 					c.smeltType = 2359;
 					c.smeltAmount = 1;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 			case 29016:
 				if (c.smeltInterface) {
 					c.smeltType = 2359;
 					c.smeltAmount = 5;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 						case 24253:
 				if (c.smeltInterface) {
 					c.smeltType = 2359;
 					c.smeltAmount = 10;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 						case 16062:
 				if (c.smeltInterface) {
 					c.smeltType = 2359;
 					c.smeltAmount = 28;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 			
 			case 29022:
 				if (c.smeltInterface) {
 					c.smeltType = 2361;
 					c.smeltAmount = 1;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 						case 29020:
 				if (c.smeltInterface) {
 					c.smeltType = 2361;
 					c.smeltAmount = 5;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 						case 29019:
 				if (c.smeltInterface) {
 					c.smeltType = 2361;
 					c.smeltAmount = 10;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 						case 29018:
 				if (c.smeltInterface) {
 					c.smeltType = 2361;
 					c.smeltAmount = 28;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 			case 29026:
 				if (c.smeltInterface) {
 					c.smeltType = 2363;
 					c.smeltAmount = 1;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 				
 			break;
 			case 29025://smelt 5
 				if (c.smeltInterface) {
 					c.smeltType = 2363;
 					c.smeltAmount = 5;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 			case 29024://smelt 10
 				if (c.smeltInterface) {
 					c.smeltType = 2363;
 					c.smeltAmount = 10;
 					c.getSmithing().startSmelting(c.smeltType);
 				}
 			break;
 			case 108005:
 			c.getPA().showInterface(19148);
 			break;
 			
 			case 59004:
 			c.getPA().removeAllWindows();
 			break;
 			
 			case 70212:
 				if (c.clanId > -1)
 					Server.clanChat.leaveClan(c.playerId, c.clanId);
 				else
 					c.sendMessage("You are not in a clan.");
 			break;
 			case 62137:
 				if (c.clanId >= 0) {
 					c.sendMessage("You are already in a clan.");
 					break;
 				}
 				if (c.getOutStream() != null) {
 					c.getOutStream().createFrame(187);
 					c.flushOutStream();
 				}	
 			break;
 			
 			case 9178:
 				if (c.usingGlory)
 					c.getPA().startTeleport(Config.EDGEVILLE_X, Config.EDGEVILLE_Y, 0, "modern");
 				if (c.dialogueAction == 2)
 					c.getPA().startTeleport(3428, 3538, 0, "modern");
 				if (c.dialogueAction == 3)		
 					c.getPA().startTeleport(Config.EDGEVILLE_X, Config.EDGEVILLE_Y, 0, "modern");
 				if (c.dialogueAction == 4)
 					c.getPA().startTeleport(3565, 3314, 0, "modern");
 				if (c.dialogueAction == 20) {
 					c.getPA().startTeleport(2897, 3618, 4, "modern");
 				}
 				if(c.dialogueAction == 100) {
 					c.getDH().sendDialogues(25, 946);
 				}
 					
 			break;
 			
 			case 9179:
 				if (c.usingGlory)
 					c.getPA().startTeleport(Config.AL_KHARID_X, Config.AL_KHARID_Y, 0, "modern");
 				if (c.dialogueAction == 2)
 					c.getPA().startTeleport(2884, 3395, 0, "modern");
 				if (c.dialogueAction == 3)
 					c.getPA().startTeleport(3243, 3513, 0, "modern");
 				if (c.dialogueAction == 4)
 					c.getPA().startTeleport(2444, 5170, 0, "modern");
 				if (c.dialogueAction == 20) {
 					c.getPA().startTeleport(2897, 3618, 12, "modern");
 				}
 				if(c.dialogueAction == 101) {
 					c.getDH().sendDialogues(21, 946);
 				}
 				if(c.dialogueAction == 100) {
 					c.getGamble().gambleBlackJack(c);
 				}	
 			break;
 			
 			case 9180:
 				if (c.usingGlory)
 					c.getPA().startTeleport(Config.KARAMJA_X, Config.KARAMJA_Y, 0, "modern");
 				if (c.dialogueAction == 2)
 					c.getPA().startTeleport(2471,10137, 0, "modern");	
 				if (c.dialogueAction == 3)
 					c.getPA().startTeleport(3363, 3676, 0, "modern");
 				if (c.dialogueAction == 4)
 					c.getPA().startTeleport(2659, 2676, 0, "modern");
 				if (c.dialogueAction == 20) {
 					c.getPA().startTeleport(2897, 3618, 8, "modern");
 				}
 				if(c.dialogueAction == 101) {
 					c.getDH().sendDialogues(23, 946);
 				}
 				if(c.dialogueAction == 100) {
 					if(!c.getItems().playerHasItem(995, 1000000)) {
 						c.sendMessage("You need at least 1M coins to play this game!");
 						c.getPA().removeAllWindows();
 						break;
 					}
 					c.getGamble().playGame(c);
 				}
 			break;
 			
 			case 9181:
 				if (c.usingGlory)
 					c.getPA().startTeleport(Config.MAGEBANK_X, Config.MAGEBANK_Y, 0, "modern");
 				if (c.dialogueAction == 2)
 					c.getPA().startTeleport(2669,3714, 0, "modern");
 				if (c.dialogueAction == 3)	
 					c.getPA().startTeleport(2540, 4716, 0, "modern");
 				if (c.dialogueAction == 4) {
 					c.getPA().startTeleport(3366, 3266, 0, "modern");
 					c.sendMessage("Dueling is at your own risk. Refunds will not be given for items lost due to glitches.");
 				}
 				if (c.dialogueAction == 20) {
 					//c.getPA().startTeleport(3366, 3266, 0, "modern");
 					//c.killCount = 0;
 					c.sendMessage("This will be added shortly");
 				} else if (c.dialogueAction == 10 || c.dialogueAction == 101) {
 					c.dialogueAction = 0;
 					c.getPA().removeAllWindows();
 				} else {
 					c.getPA().removeAllWindows();
 				}
 				c.dialogueAction = 0;
 				break;
 			
 			case 1093:
 			case 1094:
 			case 1097:
 				if (c.autocastId > 0) {
 					c.getPA().resetAutocast();
 				} else {
 					if (c.playerMagicBook == 1) {
 						if (c.playerEquipment[c.playerWeapon] == 4675 || c.playerEquipment[c.playerWeapon] == 15486 || c.playerEquipment[c.playerWeapon] == 18355)
 							c.setSidebarInterface(0, 1689);
 						else
 							c.sendMessage("You can't autocast ancients without an ancient, chaotic staff or a SOL.");
 					} else if (c.playerMagicBook == 0) {
 						if (c.playerEquipment[c.playerWeapon] == 4170 || c.playerEquipment[c.playerWeapon] == 15486 || c.playerEquipment[c.playerWeapon] == 15040) {
 							c.setSidebarInterface(0, 12050);
 						} else {
 							c.setSidebarInterface(0, 1829);
 						}	
 					}
 						
 				}		
 			break;
 	//barrows door puzzles
 
 
 			case 9157://barrows tele to tunnels
 				//Desert Treasure
 				if(c.dtOption) {
 					c.getDH().sendDialogues(501, -1);
 					c.dtOption = false;
 				}
 				if(c.dtOption2) {
 					if(c.lastDtKill == 0) {
 						c.getDH().sendDialogues(65, -1);
 					} else {
 						c.getDH().sendDialogues(502, -1);
 					}
 					c.dtOption2 = false;
 				}
 				//end Desert Treasure
 				if(c.dialogueAction == 1) {
 					//int r = 4;
 					int r = Misc.random(5);
 					switch(r) {
 						case 0:
 							c.getPA().movePlayer(3534, 9677, 0);
 							break;
 						
 						case 1:
 							c.getPA().movePlayer(3534, 9712, 0);
 							break;
 						
 						case 2:
 							c.getPA().movePlayer(3568, 9712, 0);
 							break;
 						
 						case 3:
 							c.getPA().movePlayer(3568, 9677, 0);
 							break;
 						//case 4:
 							//c.getPA().movePlayer(3551, 9694, 0);
 							//break;
 					}
 				} else if (c.dialogueAction == 2) {
 					c.getPA().movePlayer(2507, 4717, 0);		
 				} else if (c.dialogueAction == 5) {
 					c.getSlayer().giveTask();
 				} else if (c.dialogueAction == 403) {
 							c.desertT++;
 			c.height = (c.playerId * 4);
 			c.getPA().movePlayer(3310, 9376, c.height);
 			Server.npcHandler.spawnNpc(c, 1977, 3318, 9376, c.height, 0, 130, 40, 70, 90, true, true);
 			c.getPA().removeAllWindows();
 			c.getPA().loadQuests();
 			c.inDt = true;
 				} else if (c.dialogueAction == 408) {
 			c.height = (c.lastDtKill == 1914 ? (c.playerId * 4) + 2 : (c.playerId * 4));
 			c.getPA().movePlayer(c.getDT().playerX(c.lastDtKill), c.getDT().playerY(c.lastDtKill), c.height);
 			c.getDT().getDtNpcInfo(c.getDT().nextDtNpcId(Server.npcHandler.getNpcName(c.lastDtKill)));
 			Server.npcHandler.spawnNpc(c, c.getDT().nextDtNpcId(Server.npcHandler.getNpcName(c.lastDtKill)), c.getDT().getDtX(c.lastDtKill), c.getDT().getDtY(c.lastDtKill), c.height, 0, c.dtHp, c.dtMax, c.dtAtk, c.dtDef, true, true);
 			c.getPA().removeAllWindows();
 			c.inDt = true;
 				} else if (c.dialogueAction == 6) {
 					c.getSlayer().giveTask2();
 				} else if (c.dialogueAction == 7) {
 					c.getPA().startTeleport(3088,3933,0,"modern");
 					c.sendMessage("NOTE: You are now in the wilderness...");
 				} else if (c.dialogueAction == 50) {
 					c.getPA().startTeleport(2661,3307,0,"modern");
 					c.sendMessage("This is PVP");
 			} else if (c.dialogueAction == 51) {
 					c.getPA().startTeleport(3007,3631,0,"modern");
 				} else if (c.dialogueAction == 8) {
 					c.getPA().resetBarrows();
 					c.sendMessage("Your barrows have been reset.");
 				} else if (c.dialogueAction == 13) {
 					c.getPA().spellTeleport(1762, 5180, 0);
 					c.dialogueAction = -1;
  } else if (c.dialogueAction == 27) {
 					c.getPA().movePlayer(3210, 3424, 0);
 					c.monkeyk0ed = 0;
 					c.Jail = false;
 		c.forcedText = "I swear to god that i will never break the rules anymore!";
 			c.forcedChatUpdateRequired = true;
 		c.updateRequired = true;
 				}
 				c.dialogueAction = 0;
 				c.getPA().removeAllWindows();
 				break;
 			
 			case 9158:
 				if (c.dialogueAction == 50) {
 					c.getPA().startTeleport(2559,3089,0,"modern");
 					c.sendMessage("This is PVP!");
 			} else if (c.dialogueAction == 51) {
 					c.getPA().startTeleport(3243,3790,0,"modern");
 
 				} else if (c.dialogueAction == 13) {
 					c.getPA().spellTeleport(3505, 9494, 0);
 					c.dialogueAction = -1;
 									} else if (c.dialogueAction == 34) {
 					c.getPA().removeAllWindows();
 					c.dialogueAction = -1;
 					}
 
 				if (c.dialogueAction == 8) {
 					c.getPA().fixAllBarrows();
 				} else {
 				c.dialogueAction = 0;
 				c.getPA().removeAllWindows();
 				}
 				break;
 			case 9159:
 				if (c.dialogueAction == 51) {
 					c.getPA().startTeleport(3351,3659,0,"modern");
 					}
 				break;
 			case 107243:
 				c.setSidebarInterface(4, 1644);
 				break;
 
 			case 107215:
 				c.setSidebarInterface(11, 904);
 				break;
 			
 			/**Specials**/
 			case 29188:
 			c.specBarId = 7636; // the special attack text - sendframe126(S P E C I A L  A T T A C K, c.specBarId);
 			c.usingSpecial = !c.usingSpecial;
 			c.getItems().updateSpecialBar();
 			break;
 			
 			case 29163:
 			c.specBarId = 7611;
 			c.usingSpecial = !c.usingSpecial;
 			c.getItems().updateSpecialBar();
 			break;
 			
 			case 33033:
 			c.specBarId = 8505;
 			c.usingSpecial = !c.usingSpecial;
 			c.getItems().updateSpecialBar();
 			break;
 			
 			case 29038:
			if(c.playerEquipment[c.playerWeapon] == 13902) {
 			c.specBarId = 7486;
 			c.usingSpecial = !c.usingSpecial;
 			c.getItems().updateSpecialBar();
 			} else {
 			c.specBarId = 7486;
 			/*if (c.specAmount >= 5) {
 				c.attackTimer = 0;
 				c.getCombat().attackPlayer(c.playerIndex);
 				c.usingSpecial = true;
 				c.specAmount -= 5;
 			}*/
			c.getCombat().handleGmaulPlayer();
 			c.getItems().updateSpecialBar();
 			}
 			break;
 			
 			case 29063:
 			if(c.getCombat().checkSpecAmount(c.playerEquipment[c.playerWeapon])) {
 				c.gfx0(246);
 				c.forcedChat("Raarrrrrgggggghhhhhhh!");
 				c.startAnimation(1056);
 				c.playerLevel[2] = c.getLevelForXP(c.playerXP[2]) + (c.getLevelForXP(c.playerXP[2]) * 15 / 100);
 				c.getPA().refreshSkill(2);
 				c.getItems().updateSpecialBar();
 			} else {
 				c.sendMessage("You don't have the required special energy to use this attack.");
 			}
 			break;
 			
 			case 48023:
 			c.specBarId = 12335;
 			c.usingSpecial = !c.usingSpecial;
 			c.getItems().updateSpecialBar();
 			break;
 
 			case 30108:
 			c.specBarId = 7812;
 			c.usingSpecial = !c.usingSpecial;
 			c.getItems().updateSpecialBar();
 			break;
 			
 			case 29138:
 			if(c.playerEquipment[c.playerWeapon] == 15486) {
 			if(c.getCombat().checkSpecAmount(c.playerEquipment[c.playerWeapon])) {
 				c.gfx0(1958);
 				c.SolProtect = 120;
 				c.startAnimation(10518);
 				c.getItems().updateSpecialBar();
 			c.usingSpecial = !c.usingSpecial;
 			c.sendMessage("All damage will be split into half for 1 minute.");
 			c.getPA().sendFrame126("@bla@S P E C I A L  A T T A C K", 7562);
 			} else {
 				c.sendMessage("You don't have the required special energy to use this attack.");
 			}	
 			}			
 			c.specBarId = 7586;
 			c.usingSpecial = !c.usingSpecial;
 			c.getItems().updateSpecialBar();
 			break;
 			
 			case 29113:
 			c.specBarId = 7561;
 			c.usingSpecial = !c.usingSpecial;
 			c.getItems().updateSpecialBar();
 			break;
 			
 			case 29238:
 			c.specBarId = 7686;
 			c.usingSpecial = !c.usingSpecial;
 			c.getItems().updateSpecialBar();
 			break;
 			
 			/**Dueling**/			
 			case 26065: // no forfeit
 			case 26040:
 			c.duelSlot = -1;
 			c.getTradeAndDuel().selectRule(0);
 			break;
 			
 			case 26066: // no movement
 			case 26048:
 			c.duelSlot = -1;
 			c.getTradeAndDuel().selectRule(1);
 			break;
 			
 			case 26069: // no range
 			case 26042:
 			c.duelSlot = -1;
 			c.getTradeAndDuel().selectRule(2);
 			break;
 			
 			case 26070: // no melee
 			case 26043:
 			c.duelSlot = -1;
 			c.getTradeAndDuel().selectRule(3);
 			break;				
 			
 			case 26071: // no mage
 			case 26041:
 			c.duelSlot = -1;
 			c.getTradeAndDuel().selectRule(4);
 			break;
 				
 			case 26072: // no drinks
 			case 26045:
 			c.duelSlot = -1;
 			c.getTradeAndDuel().selectRule(5);
 			break;
 			
 			case 26073: // no food
 			case 26046:
 			c.duelSlot = -1;
 			c.getTradeAndDuel().selectRule(6);
 			break;
 			
 			case 26074: // no prayer
 			case 26047:	
 			c.duelSlot = -1;
 			c.getTradeAndDuel().selectRule(7);
 			break;
 			
 			case 26076: // obsticals
 			case 26075:
 			c.duelSlot = -1;
 			c.getTradeAndDuel().selectRule(8);
 			break;
 			
 			case 2158: // fun weapons
 			case 2157:
 			c.duelSlot = -1;
 			c.getTradeAndDuel().selectRule(9);
 			break;
 			
 			case 30136: // sp attack
 			case 30137:
 			c.duelSlot = -1;
 			c.getTradeAndDuel().selectRule(10);
 			break;	
 
 			case 53245: //no helm
 			c.duelSlot = 0;
 			c.getTradeAndDuel().selectRule(11);
 			break;
 			
 			case 53246: // no cape
 			c.duelSlot = 1;
 			c.getTradeAndDuel().selectRule(12);
 			break;
 			
 			case 53247: // no ammy
 			c.duelSlot = 2;
 			c.getTradeAndDuel().selectRule(13);
 			break;
 			
 			case 53249: // no weapon.
 			c.duelSlot = 3;
 			c.getTradeAndDuel().selectRule(14);
 			break;
 			
 			case 53250: // no body
 			c.duelSlot = 4;
 			c.getTradeAndDuel().selectRule(15);
 			break;
 			
 			case 53251: // no shield
 			c.duelSlot = 5;
 			c.getTradeAndDuel().selectRule(16);
 			break;
 			
 			case 53252: // no legs
 			c.duelSlot = 7;
 			c.getTradeAndDuel().selectRule(17);
 			break;
 			
 			case 53255: // no gloves
 			c.duelSlot = 9;
 			c.getTradeAndDuel().selectRule(18);
 			break;
 			
 			case 53254: // no boots
 			c.duelSlot = 10;
 			c.getTradeAndDuel().selectRule(19);
 			break;
 			
 			case 53253: // no rings
 			c.duelSlot = 12;
 			c.getTradeAndDuel().selectRule(20);
 			break;
 			
 			case 53248: // no arrows
 			c.duelSlot = 13;
 			c.getTradeAndDuel().selectRule(21);
 			break;
 			
 			case 26018:	
 			Client o = (Client) Server.playerHandler.players[c.duelingWith];
 			if(o == null) {
 				c.getTradeAndDuel().declineDuel();
 				return;
 			}
 			
 			if(c.duelRule[2] && c.duelRule[3] && c.duelRule[4]) {
 				c.sendMessage("You won't be able to attack the player with the rules you have set.");
 				break;
 			}
 			c.duelStatus = 2;
 			if(c.duelStatus == 2) {
 				c.getPA().sendFrame126("Waiting for other player...", 6684);
 				o.getPA().sendFrame126("Other player has accepted.", 6684);
 			}
 			if(o.duelStatus == 2) {
 				o.getPA().sendFrame126("Waiting for other player...", 6684);
 				c.getPA().sendFrame126("Other player has accepted.", 6684);
 			}
 			
 			if(c.duelStatus == 2 && o.duelStatus == 2) {
 				c.canOffer = false;
 				o.canOffer = false;
 				c.duelStatus = 3;
 				o.duelStatus = 3;
 				c.getTradeAndDuel().confirmDuel();
 				o.getTradeAndDuel().confirmDuel();
 			}
 			break;
 			
 			case 25120:
 			if(c.duelStatus == 5) {
 				break;
 			}
 			Client o1 = (Client) Server.playerHandler.players[c.duelingWith];
 			if(o1 == null) {
 				c.getTradeAndDuel().declineDuel();
 				return;
 			}
 
 			c.duelStatus = 4;
 			if(o1.duelStatus == 4 && c.duelStatus == 4) {				
 				c.getTradeAndDuel().startDuel();
 				o1.getTradeAndDuel().startDuel();
 				o1.duelCount = 4;
 				c.duelCount = 4;
 				c.duelDelay = System.currentTimeMillis();
 				o1.duelDelay = System.currentTimeMillis();
 			} else {
 				c.getPA().sendFrame126("Waiting for other player...", 6571);
 				o1.getPA().sendFrame126("Other player has accepted", 6571);
 			}
 			break;
 	
 			
 			case 4169: // god spell charge
 			c.usingMagic = true;
 			if(!c.getCombat().checkMagicReqs(48)) {
 				break;
 			}
 				
 			if(System.currentTimeMillis() - c.godSpellDelay < Config.GOD_SPELL_CHARGE) {
 				c.sendMessage("You still feel the charge in your body!");
 				break;
 			}
 			c.godSpellDelay	= System.currentTimeMillis();
 			c.sendMessage("You feel charged with a magical power!");
 			c.gfx100(c.MAGIC_SPELLS[48][3]);
 			c.startAnimation(c.MAGIC_SPELLS[48][2]);
 			c.usingMagic = false;
 	        break;
 			
 			
 			case 28164: // item kept on death 
 			break;
 			
 			
 case 153:
 case 152:
 c.isRunning2 = !c.isRunning2;
 int frame = c.isRunning2 == true ? 1 : 0;
 c.getPA().sendFrame36(173,frame);
 break;
 			
 			case 9154:
 			c.logout();
 			break;
 			
 			case 21010:
 			c.takeAsNote = true;
 			break;
 
 			case 21011:
 			c.takeAsNote = false;
 			break;
 			
 				
 
 			//home teleports
 			case 4171:
 			c.getPA().startTeleport(Config.START_LOCATION_X+Misc.random(2), Config.START_LOCATION_Y+Misc.random(2), 0, "modern");	
 			break;		
 
 			//home teleports
 			case 50056:
 			c.getPA().startTeleport(Config.EDGEVILLE_X+Misc.random(2), Config.EDGEVILLE_Y+Misc.random(2), 0, "ancient");	
 			break;
 			
 			case 117048:
 			c.getPA().startTeleport(Config.EDGEVILLE_X+Misc.random(2), Config.EDGEVILLE_Y+Misc.random(2), 0, "lunar");
 			break;
 			
 			case 50235:
 			case 4140:
 			case 117112:
 			//c.getPA().startTeleport(Config.LUMBY_X, Config.LUMBY_Y, 0, "modern");
 			c.getDH().sendOption5("Rock Crabs", "Taverly Dungeon", "Slayer Tower", "Brimhaven Dungeon", "Hill Giants");
 
 			c.teleAction = 1;
 			break;
 
 			
 			case 4143:
 			case 50245:
 			case 117123:
 			c.getDH().sendOption5("Barrows", "Pest Control", "TzHaar Cave", "Duel Arena", "Warrior Guild");
 			c.teleAction = 2;
 			break;
 			
 			case 50253:
 			case 117131:
 			case 4146:
 			c.getDH().sendOption5("Godwars", "King Black Dragon (Wild)", "Dagannoth Kings", "Tormented Demons", "Corporeal Beast");
 			c.teleAction = 3;
 			break;
 			
 
 			case 51005:
 			case 117154:
 			case 4150:
 			c.getDH().sendOption5("Mage Bank", "Green Dragons(13 Wild))", "Multi Pk Zones", "Edgeville", "PvP Zones");
 			c.teleAction = 4;
 			break;			
 			
 			case 51013:
 			case 6004:	
 			case 117162:	
 			c.getPA().startTeleport(2852, 3432, 0, "modern");
 			//c.getDH().sendOption5("Mining", "Smithing", "Fishing/Cooking", "Woodcutting", "Farming");
 			//c.teleAction = 5;
 			break; 
 			
 			
 			case 51023:
 			case 6005:
 						c.getDH().sendOption5("Lumbridge", "Varrock", "Camelot", "Falador", "Canifis");
 			c.teleAction = 20;
 			break; 
 			
 			
 			case 51031:
 			case 29031:
 			c.getPA().startTeleport(2602, 4779, 0, "modern");
 			break; 		
 
 			case 72038:
 			case 51039:
 			//c.getDH().sendOption5("Option 18", "Option 2", "Option 3", "Option 4", "Option 5");
 			//c.teleAction = 8;
 			break;
 			
       			case 9125: //Accurate
 			case 6221: // range accurate
 			case 22230: //kick (unarmed)
 			case 48010: //flick (whip)
 			case 21200: //spike (pickaxe)
 			case 1080: //bash (staff)
 			case 6168: //chop (axe)
 			case 6236: //accurate (long bow)
 			case 17102: //accurate (darts)
 			case 8234: //stab (dagger)
 
 			case 30088: //claws
 			case 1177: //hammer
 			c.fightMode = 0;
 			if (c.autocasting)
 				c.getPA().resetAutocast();
 			break;
 			
 			case 9126: //Defensive
 			case 48008: //deflect (whip)
 			case 22228: //punch (unarmed)
 			case 21201: //block (pickaxe)
 			case 1078: //focus - block (staff)
 			case 6169: //block (axe)
 			case 33019: //fend (hally)
 			case 18078: //block (spear)
 			case 8235: //block (dagger)
 			case 1175: //accurate (darts)
 			case 30089: //stab (dagger)
 			c.fightMode = 1;
 			if (c.autocasting)
 				c.getPA().resetAutocast();
 			break;
 			
 			case 9127: // Controlled
 			case 48009: //lash (whip)
 			case 33018: //jab (hally)
 			case 6234: //longrange (long bow)
 			case 6219: //longrange
 			case 18077: //lunge (spear)
 			case 18080: //swipe (spear)
 			case 18079: //pound (spear)
 			case 17100: //longrange (darts)
 			c.fightMode = 3;
 			if (c.autocasting)
 				c.getPA().resetAutocast();
 			break;
 			
 			case 9128: //Aggressive
 			case 6220: // range rapid
 			case 22229: //block (unarmed)
 			case 21203: //impale (pickaxe)
 			case 21202: //smash (pickaxe)
 			case 1079: //pound (staff)
 			case 6171: //hack (axe)
 			case 6170: //smash (axe)
 			case 33020: //swipe (hally)
 			case 6235: //rapid (long bow)
 			case 17101: //repid (darts)
 			case 8237: //lunge (dagger)
 			case 30091: //claws
 			case 1176: //stat hammer
 			case 8236: //slash (dagger)
 
 			case 30090: //claws
 			c.fightMode = 2;
 			if (c.autocasting)
 				c.getPA().resetAutocast();
 			break;
 
 			/**Prayers**/
 			case 87231: // thick skin
 			if(c.trade11 > 1) {
 			for(int p = 0; p < c.PRAYER.length; p++) { // reset prayer glows 
 				c.prayerActive[p] = false;
 				c.getPA().sendFrame36(c.PRAYER_GLOW[p], 0);	
 			}
 			c.sendMessage("You must wait 15 minutes before using this!");
 			return;
 			}
 			c.getCurse().activateCurse(0);
 			break;	
 			case 87233: // burst of str
 			c.getCurse().activateCurse(1);
 			break;	
 			case 87235: // charity of thought
 			c.getCurse().activateCurse(2);
 			break;	
 			case 87237: // range
 			c.getCurse().activateCurse(3);
 			break;
 			case 87239: // mage
 			c.getCurse().activateCurse(4);
 			break;
 case 87241: // berserker
 			if(c.altarPrayed == 0) {
 				return;
 			}
 			c.getCurse().activateCurse(5);
 			break;
 			case 87243: // super human
 			c.getCurse().activateCurse(6);
 			break;
 			case 87245:	// improved reflexes
 			c.getCurse().activateCurse(7);
 			break;
 			case 87247: //hawk eye
 			c.getCurse().activateCurse(8);
 			break;
 			case 87249:
 			c.getCurse().activateCurse(9);
 			break;
 			case 87251: // protect Item
 			c.getCurse().activateCurse(10);
 			break;			
 			case 87253: // 26 range
 			c.getCurse().activateCurse(11);
 			break;
 			case 87255: // 27 mage
 			c.getCurse().activateCurse(12);
 			break;	
 			case 88001: // steel skin
 			c.getCurse().activateCurse(13);
 			break;
 			case 88003: // ultimate str
 			c.getCurse().activateCurse(14);
 			break;
 			case 88005: // incredible reflex
 			c.getCurse().activateCurse(15);
 			break;	
 			case 88007: // protect from magic
 			c.getCurse().activateCurse(16);
 			break;					
 			case 88009: // protect from range
 			c.getCurse().activateCurse(17);
 			break;
 			case 88011: // protect from melee
 			c.getCurse().activateCurse(18);
 			break;
 			case 88013: // 44 range
 			c.getCurse().activateCurse(19);
 			break;	
 			/**End of curse prayers**/
 			
 			
 			/**Prayers**/
 			case 97168: // thick skin
 			c.getCombat().activatePrayer(0);
 			break;	
 			case 97170: // burst of str
 			c.getCombat().activatePrayer(1);
 			break;	
 			case 97172: // charity of thought
 			c.getCombat().activatePrayer(2);
 			break;	
 			case 97174: // range
 			c.getCombat().activatePrayer(3);
 			break;
 			case 97176: // mage
 			c.getCombat().activatePrayer(4);
 			break;
 			case 97178: // rockskin
 			c.getCombat().activatePrayer(5);
 			break;
 			case 97180: // super human
 			c.getCombat().activatePrayer(6);
 			break;
 			case 97182:	// improved reflexes
 			c.getCombat().activatePrayer(7);
 			break;
 			case 97184: //hawk eye
 			c.getCombat().activatePrayer(8);
 			break;
 			case 97186:
 			c.getCombat().activatePrayer(9);
 			break;
 			case 97188: // protect Item
 			/*if(c.trade11 > 1) {
 			for(int p = 0; p < c.PRAYER.length; p++) { // reset prayer glows 
 				c.prayerActive[p] = false;
 				c.getPA().sendFrame36(c.PRAYER_GLOW[p], 0);	
 			}
 			c.sendMessage("You must wait 15 minutes before using this!");
 			return;
 			}*/
 			c.getCombat().activatePrayer(10);
 			break;			
 			case 97190: // 26 range
 			c.getCombat().activatePrayer(11);
 			break;
 			case 97192: // 27 mage
 			c.getCombat().activatePrayer(12);
 			break;	
 			case 97194: // steel skin
 			c.getCombat().activatePrayer(13);
 			break;
 			case 97196: // ultimate str
 			c.getCombat().activatePrayer(14);
 			break;
 			case 97198: // incredible reflex
 			c.getCombat().activatePrayer(15);
 			break;	
 			case 97200: // protect from magic
 			c.getCombat().activatePrayer(16);
 			break;					
 			case 97202: // protect from range
 			c.getCombat().activatePrayer(17);
 			break;
 			case 97204: // protect from melee
 			c.getCombat().activatePrayer(18);
 			break;
 			case 97206: // 44 range
 			c.getCombat().activatePrayer(19);
 			break;	
 			case 97208: // 45 mystic
 			c.getCombat().activatePrayer(20);
 			break;				
 			case 97210: // retrui
 			c.getCombat().activatePrayer(21);
 			break;					
 			case 97212: // redem
 			c.getCombat().activatePrayer(22);
 			break;					
 			case 97214: // smite
 			c.getCombat().activatePrayer(23);
 			break;
 			case 97216: // chiv
 			c.getCombat().activatePrayer(24);
 			break;
 			case 97218: // piety
 			c.getCombat().activatePrayer(25);
 			break;
 
 					
 			case 13092:
                         if (System.currentTimeMillis() - c.lastButton < 400) {
 
 					c.lastButton = System.currentTimeMillis();
 
 					break;
 
 				} else {
 
 					c.lastButton = System.currentTimeMillis();
 
 				}
 			Client ot = (Client) Server.playerHandler.players[c.tradeWith];
 			if(ot == null) {
 				c.getTradeAndDuel().declineTrade();
 				c.sendMessage("Trade declined as the other player has disconnected.");
 				break;
 			}
 			c.getPA().sendFrame126("Waiting for other player...", 3431);
 			ot.getPA().sendFrame126("Other player has accepted", 3431);	
 			c.goodTrade= true;
 			ot.goodTrade= true;
 			
 			for (GameItem item : c.getTradeAndDuel().offeredItems) {
 				if (item.id > 0) {
 					if(ot.getItems().freeSlots() < c.getTradeAndDuel().offeredItems.size()) {					
 						c.sendMessage(ot.playerName +" only has "+ot.getItems().freeSlots()+" free slots, please remove "+(c.getTradeAndDuel().offeredItems.size() - ot.getItems().freeSlots())+" items.");
 						ot.sendMessage(c.playerName +" has to remove "+(c.getTradeAndDuel().offeredItems.size() - ot.getItems().freeSlots())+" items or you could offer them "+(c.getTradeAndDuel().offeredItems.size() - ot.getItems().freeSlots())+" items.");
 						c.goodTrade= false;
 						ot.goodTrade= false;
 						c.getPA().sendFrame126("Not enough inventory space...", 3431);
 						ot.getPA().sendFrame126("Not enough inventory space...", 3431);
 							break;
 					} else {
 						c.getPA().sendFrame126("Waiting for other player...", 3431);				
 						ot.getPA().sendFrame126("Other player has accepted", 3431);
 						c.goodTrade= true;
 						ot.goodTrade= true;
 						}
 					}	
 				}	
 				if (c.inTrade && !c.tradeConfirmed && ot.goodTrade && c.goodTrade) {
 					c.tradeConfirmed = true;
 					if(ot.tradeConfirmed) {
 						c.getTradeAndDuel().confirmScreen();
 						ot.getTradeAndDuel().confirmScreen();
 						break;
 					}
 							  
 				}
 
 		
 			break;
 					
 			case 13218:
                          if (System.currentTimeMillis() - c.lastButton < 400) {
 
 					c.lastButton = System.currentTimeMillis();
 
 					break;
 
 				} else {
 
 					c.lastButton = System.currentTimeMillis();
 
 				}
 			c.tradeAccepted = true;
 			Client ot1 = (Client) Server.playerHandler.players[c.tradeWith];
 				if (ot1 == null) {
 					c.getTradeAndDuel().declineTrade();
 					c.sendMessage("Trade declined as the other player has disconnected.");
 					break;
 				}
 				
 				if (c.inTrade && c.tradeConfirmed && ot1.tradeConfirmed && !c.tradeConfirmed2) {
 					c.tradeConfirmed2 = true;
 					if(ot1.tradeConfirmed2) {	
 						c.acceptedTrade = true;
 						ot1.acceptedTrade = true;
 						c.getTradeAndDuel().giveItems();
 						ot1.getTradeAndDuel().giveItems();
 						c.sendMessage("Trade accepted.");
 						c.SaveGame();
 						ot1.SaveGame();
 						ot1.sendMessage("Trade accepted.");
 						break;
 					}
 				ot1.getPA().sendFrame126("Other player has accepted.", 3535);
 				c.getPA().sendFrame126("Waiting for other player...", 3535);
 				}
 				
 			break;			
 			/* Rules Interface Buttons */
 			case 125011: //Click agree
 				if(!c.ruleAgreeButton) {
 					c.ruleAgreeButton = true;
 					c.getPA().sendFrame36(701, 1);
 				} else {
 					c.ruleAgreeButton = false;
 					c.getPA().sendFrame36(701, 0);
 				}
 				break;
 			case 67100://Accept
 					c.getPA().showInterface(3559);
 					c.newPlayer = false;
 					c.sendMessage("You need to click on you agree before you can continue on.");
 				break;
 			case 67103://Decline
 				c.sendMessage("You have chosen to decline, Client will be disconnected from the server.");
 				break;
 			/* End Rules Interface Buttons */
 			/* Player Options */
 			case 74176:
 				if(!c.mouseButton) {
 					c.mouseButton = true;
 					c.getPA().sendFrame36(500, 1);
 					c.getPA().sendFrame36(170,1);
 				} else if(c.mouseButton) {
 					c.mouseButton = false;
 					c.getPA().sendFrame36(500, 0);
 					c.getPA().sendFrame36(170,0);					
 				}
 				break;
 			case 74184:
 				if(!c.splitChat) {
 					c.splitChat = true;
 					c.getPA().sendFrame36(502, 1);
 					c.getPA().sendFrame36(287, 1);
 				} else {
 					c.splitChat = false;
 					c.getPA().sendFrame36(502, 0);
 					c.getPA().sendFrame36(287, 0);
 				}
 				break;
 			case 100231:
 				if(!c.chatEffects) {
 					c.chatEffects = true;
 					c.getPA().sendFrame36(501, 1);
 					c.getPA().sendFrame36(171, 0);
 				} else {
 					c.chatEffects = false;
 					c.getPA().sendFrame36(501, 0);
 					c.getPA().sendFrame36(171, 1);
 				}
 				break;
 			case 100237:
 				if(!c.acceptAid) {
 					c.acceptAid = true;
 					c.getPA().sendFrame36(503, 1);
 					c.getPA().sendFrame36(427, 1);
 				} else {
 					c.acceptAid = false;
 					c.getPA().sendFrame36(503, 0);
 					c.getPA().sendFrame36(427, 0);
 				}
 				break;
 			case 74201://brightness1
 				c.getPA().sendFrame36(505, 1);
 				c.getPA().sendFrame36(506, 0);
 				c.getPA().sendFrame36(507, 0);
 				c.getPA().sendFrame36(508, 0);
 				c.getPA().sendFrame36(166, 1);
 				break;
 			case 74203://brightness2
 				c.getPA().sendFrame36(505, 0);
 				c.getPA().sendFrame36(506, 1);
 				c.getPA().sendFrame36(507, 0);
 				c.getPA().sendFrame36(508, 0);
 				c.getPA().sendFrame36(166,2);
 				break;
 
 			case 74204://brightness3
 				c.getPA().sendFrame36(505, 0);
 				c.getPA().sendFrame36(506, 0);
 				c.getPA().sendFrame36(507, 1);
 				c.getPA().sendFrame36(508, 0);
 				c.getPA().sendFrame36(166,3);
 				break;
 
 			case 74205://brightness4
 				c.getPA().sendFrame36(505, 0);
 				c.getPA().sendFrame36(506, 0);
 				c.getPA().sendFrame36(507, 0);
 				c.getPA().sendFrame36(508, 1);
 				c.getPA().sendFrame36(166,4);
 				break;
 			case 74206://area1
 				c.getPA().sendFrame36(509, 1);
 				c.getPA().sendFrame36(510, 0);
 				c.getPA().sendFrame36(511, 0);
 				c.getPA().sendFrame36(512, 0);
 				break;
 			case 74207://area2
 				c.getPA().sendFrame36(509, 0);
 				c.getPA().sendFrame36(510, 1);
 				c.getPA().sendFrame36(511, 0);
 				c.getPA().sendFrame36(512, 0);
 				break;
 			case 74208://area3
 				c.getPA().sendFrame36(509, 0);
 				c.getPA().sendFrame36(510, 0);
 				c.getPA().sendFrame36(511, 1);
 				c.getPA().sendFrame36(512, 0);
 				break;
 			case 74209://area4
 				c.getPA().sendFrame36(509, 0);
 				c.getPA().sendFrame36(510, 0);
 				c.getPA().sendFrame36(511, 0);
 				c.getPA().sendFrame36(512, 1);
 				break;
 			case 168:
                 c.startAnimation(855);		c.stopMovement();
             break;
             case 169:
                 c.startAnimation(856);		c.stopMovement();
             break;
             case 162:
                 c.startAnimation(857);		c.stopMovement();
             break;
             case 164:
                 c.startAnimation(858);		c.stopMovement();
             break;
             case 165:
                 c.startAnimation(859);		c.stopMovement();
             break;
             case 161:
                 c.startAnimation(860);		c.stopMovement();
             break;
             case 170:
                 c.startAnimation(861);		c.stopMovement();
             break;
             case 171:
                 c.startAnimation(862);		c.stopMovement();
             break;
             case 163:
                 c.startAnimation(863);		c.stopMovement();
             break;
             case 167:
                 c.startAnimation(864);		c.stopMovement();
             break;
             case 172:
                 c.startAnimation(865);		c.stopMovement();
             break;
             case 166:
                 c.startAnimation(866);		c.stopMovement();
             break;
             case 52050:
                 c.startAnimation(2105);		c.stopMovement();
             break;
             case 52051:
                 c.startAnimation(2106);		c.stopMovement();
             break;
             case 52052:
                 c.startAnimation(2107);		c.stopMovement();
             break;
             case 52053:
                 c.startAnimation(2108);		c.stopMovement();
             break;
             case 52054:
                 c.startAnimation(2109);		c.stopMovement();
             break;
             case 52055:
                 c.startAnimation(2110);		c.stopMovement();
             break;
             case 52056:
                 c.startAnimation(2111);		c.stopMovement();
             break;
             case 52057:
                 c.startAnimation(2112);		c.stopMovement();
             break;
             case 52058:
                 c.startAnimation(2113);		c.stopMovement();
             break;
             case 43092:
                 c.startAnimation(0x558);		c.stopMovement();
 				c.gfx0(574);
             break;
             case 2155:
                 c.startAnimation(11044);		c.stopMovement();
 				c.gfx0(1973);
             break;
             case 25103:
                 c.startAnimation(10530);		c.stopMovement();
 				c.gfx0(1864);
             break;
             case 25106:
 				c.startAnimation(8770);
 				c.gfx0(1553);		c.stopMovement();
             break;
             case 2154:
                 c.startAnimation(7531);		c.stopMovement();
             break;
             case 52071:
                 c.startAnimation(0x84F);		c.stopMovement();
             break;
             case 52072:
                 c.startAnimation(0x850);		c.stopMovement();
             break;
             case 73003:
 		c.startAnimation(6111);	c.stopMovement();
             break;
             case 73001:
                 c.startAnimation(3544);		c.stopMovement();
             break;
             case 73000:
 			if(System.currentTimeMillis() - c.logoutDelay < 8000) {
 c.sendMessage("You cannot do skillcape emotes in combat!");
 return;
 }
                 c.startAnimation(3543);		c.stopMovement();
             break;
 			case 72032:
 				c.startAnimation(9990);		c.stopMovement();
 				c.gfx0(1734);
             break;
 			case 72033:
 				c.startAnimation(4278);		c.stopMovement();
             break;
 			case 59062:
 				c.startAnimation(4280);		c.stopMovement();
             break;
 			case 72254:
 				c.startAnimation(4275);		c.stopMovement();
             break;
 			case 73004:
 				c.startAnimation(7272);		c.stopMovement();
 				c.gfx0(1244);
             break;
 			case 72255:
 			if(System.currentTimeMillis() - c.logoutDelay < 8000) {
 c.sendMessage("You cannot do skillcape emotes in combat!");		c.stopMovement();
 return;
 }
 				c.startAnimation(2414);
 				c.gfx0(1537);
 			break;
 			/* END OF EMOTES */
 //case 118098:
 //c.getPA().castVeng();
 //break; 
 			
 			case 47130:			
 				c.forcedText = "I must slay another " + c.taskAmount + " " + Server.npcHandler.getNpcListName(c.slayerTask) + ".";
 				c.forcedChatUpdateRequired = true;
 				c.updateRequired = true;
 			break;
 			
 				case 77035:
 		c.forcedText = "[QC] My Hunter level is  " + c.getPA().getLevelForXP(c.playerXP[21]) + ".";
 		c.forcedChatUpdateRequired = true;
 		c.updateRequired = true;
 	break;
 	case 77036:
 if(c.lastsummon > 0) {
 c.firstslot();
 for(int i = 0; i < 29; i += 1)
 {
 Server.itemHandler.createGroundItem(c, c.storeditems[i], Server.npcHandler.npcs[c.summoningnpcid].absX, Server.npcHandler.npcs[c.summoningnpcid].absY, 1, c.playerId);
 c.storeditems[i] = -1;
 c.occupied[i] = false;
 }
 c.lastsummon = -1;
 c.totalstored = 0;
 c.summoningnpcid = 0;
 c.summoningslot = 0;
 c.sendMessage("Your BoB items have drop on the floor");
 } else {
 c.sendMessage("You do not have a npc currently spawned");
 }
 			case 55095:
 				c.getPA().destroyItem(c.destroyItem);
 			case 55096:
 				c.getPA().closeAllWindows();
 			break;
 			
 			case 24017:
 				c.getPA().resetAutocast();
 				//c.sendFrame246(329, 200, c.playerEquipment[c.playerWeapon]);
 				c.getItems().sendWeapon(c.playerEquipment[c.playerWeapon], c.getItems().getItemName(c.playerEquipment[c.playerWeapon]));
 				//c.setSidebarInterface(0, 328);
 				//c.setSidebarInterface(6, c.playerMagicBook == 0 ? 1151 : c.playerMagicBook == 1 ? 12855 : 1151);
 			break;
 		}
 		if (c.isAutoButton(actionButtonId))
 			c.assignAutocast(actionButtonId);
 	}
 
 }
