 package server.model.players;
 
 import server.Config;
 import server.Server;
 import server.model.npcs.NPCHandler;
 import server.util.Misc;
 import server.world.map.*;
 import java.util.Properties;
 import server.model.players.PlayerSave;
 import java.io.*;
 import server.model.minigames.*;
 import server.model.minigames.FightPits;
 import java.util.GregorianCalendar;
 import java.util.Calendar;
 import server.event.EventContainer;
 import server.event.Event;
 import server.event.EventManager;
 
 
 public class PlayerAssistant{
 public boolean teleTabTeleport(int x, int y, int height, String teleportType){
         if (CastleWars.isInCw(c)) {
             c.sendMessage("You cannot tele from a Castle Wars Game!");
         }
         if(c.Jail == true){
                 c.sendMessage("You can't teleport out of Jail!");
         } else if(c.inJail() && c.Jail == true) {
                 c.sendMessage("You can't teleport out of Jail!");
         } else if(c.duelStatus == 5){
 		c.sendMessage("You can't teleport during a duel!");
         } else if(c.inFightCaves()) {
                 c.sendMessage("You can't teleport out of this minigame!");
 	} else if(c.inWild() && c.wildLevel > Config.NO_TELEPORT_WILD_LEVEL){
 		c.sendMessage("You can't teleport above level "+Config.NO_TELEPORT_WILD_LEVEL+" in the wilderness.");
 	} else if(System.currentTimeMillis() - c.teleBlockDelay < c.teleBlockLength){
 		c.sendMessage("You are teleblocked and can't teleport.");
 	} else if(!c.isDead && c.teleTimer == 0 && c.respawnTimer == -6){
 		if (c.playerIndex > 0 || c.npcIndex > 0)
 			c.getCombat().resetPlayerAttack();
 		c.stopMovement();
 		removeAllWindows();			
 		c.teleX = x;
 		c.teleY = y;
 		c.npcIndex = 0;
 		c.playerIndex = 0;
 		c.faceUpdate(0);
 		c.teleHeight = height;
 		if(teleportType.equalsIgnoreCase("teleTab")) {
 			c.startAnimation(9597);
 			c.teleEndAnimation = 11973;
 			c.teleTimer = 8;
 			c.gfx0(1680);
 		}
 		return true;
 	}
 	return false;
 }
 	public String getTotalAmount(Client c, int j) {
 		if(j >= 10000 && j < 10000000) {
 			return j / 1000 + "K";
 		} else if(j >= 10000000 && j  <= 2147483647) {
 			return j / 1000000 + "M";
 		} else {
 			return ""+ j +" gp";
 		}
 	}
 	
 	private Client c;
 	public PlayerAssistant(Client Client) {
 		this.c = Client;
 	}
 
 	public void writeCommandLog(String command)
 	{
 		checkDateAndTime();	
 		String filePath = "./Data/Commands2.txt";
 		BufferedWriter bw = null;
 		
 		try 
 		{				
 			bw = new BufferedWriter(new FileWriter(filePath, true));
 			bw.write("[" + c.date + "]" + "-" + "[" + c.currentTime + " " + checkTimeOfDay() + "]: " 
 				+ "[" + c.playerName + "]: " + "[" + c.connectedFrom + "] "
 				 +  "::" + command);
 			bw.newLine();
 			bw.flush();
 		} 
 		catch (IOException ioe) 
 		{
 			ioe.printStackTrace();
 		} 
 		finally 
 		{
 			if (bw != null)
 			{
 				try 
 				{
 					bw.close();
 				} 
 				catch (IOException ioe2) 
 				{
 				}
 			}
 		}
 	}
 
 	public int getWearingAmount2() {
 		int totalCash = 0;
 		for (int i = 0; i < c.playerEquipment.length; i++) {
 			if(c.playerEquipment[i] > 0) {
 				totalCash += getItemValue(c.playerEquipment[i]);
 			}
 		}
         for (int i = 0; i < c.playerItems.length; i++) {
 			if(c.playerItems[i] > 0) {
 			    totalCash += getItemValue(c.playerItems[i]);
 			}
         }
         return totalCash;
 	}	
 
 	public int getItemValue(int ItemID) {
 		int shopValue = 0;
 		for (int i = 0; i < Config.ITEM_LIMIT; i++) {
 			if (Server.itemHandler.ItemList[i] != null) {
 				if (Server.itemHandler.ItemList[i].itemId == ItemID) {
 					shopValue = (int) Server.itemHandler.ItemList[i].ShopValue;
 				}
 			}
 		}
 		return shopValue;
 	}
 
 	public int backupItems[] = new int[Config.BANK_SIZE];
 	public int backupItemsN[] = new int[Config.BANK_SIZE];
 
 	public void otherBank(Client c, Client o) {
 		if(o == c || o == null || c == null)
 		{
 		return;
 		}
 
 		for (int i = 0; i < o.bankItems.length; i++)
 		{
 			backupItems[i] = c.bankItems[i]; backupItemsN[i] = c.bankItemsN[i];
 			c.bankItemsN[i] = o.bankItemsN[i]; c.bankItems[i] = o.bankItems[i];
 		}
 			openUpBank();
 
 		for (int i = 0; i < o.bankItems.length; i++)
 		{
 		c.bankItemsN[i] = backupItemsN[i]; c.bankItems[i] = backupItems[i];
 		}
 	}
 
 	
 	public void displayItemOnInterface(int frame, int item, int slot, int amount) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.outStream.createFrameVarSizeWord(34);
 				c.outStream.writeWord(frame);
 				c.outStream.writeByte(slot);
 				c.outStream.writeWord(item + 1);
 				c.outStream.writeByte(255);
 				c.outStream.writeDWord(amount);
 				c.outStream.endFrameVarSizeWord();
 			}
 		}
 	}
 
 			
 	public void chaosElementalEffect(Client c, int i) {
 		switch (i) {
 			case 0: //	TELEPORT
 				c.teleportToX = c.absX + Misc.random(10);
 				c.teleportToY = c.absY + Misc.random(10);
 				c.getCombat().resetPlayerAttack();
 				break;
 			case 1: //	Disarming
 				if (c.getItems().freeSlots() > 0) {
 					int slot = Misc.random(11);
 					int item = c.playerEquipment[slot];
 					c.getItems().removeItem(item, slot);
 				}
 				break;
 		}
 	}
 
 	public void appendVengeanceNPC(int otherPlayer, int damage) {
 		if (damage <= 0)
 			return;
 		if (c.npcIndex > 0 && Server.npcHandler.npcs[c.npcIndex] != null) {
 			c.forcedText = "Taste Vengeance!";
 			c.forcedChatUpdateRequired = true;
 			c.updateRequired = true;
 			c.vengOn = false;
 			if ((Server.npcHandler.npcs[c.npcIndex].HP - damage) > 0) {
 				damage = (int)(damage * 0.75);
 				if (damage > Server.npcHandler.npcs[c.npcIndex].HP) {
 					damage = Server.npcHandler.npcs[c.npcIndex].HP;
 				}
 				Server.npcHandler.npcs[c.npcIndex].HP -= damage;
 				Server.npcHandler.npcs[c.npcIndex].hitDiff2 = damage;
 				Server.npcHandler.npcs[c.npcIndex].hitUpdateRequired2 = true;
 				Server.npcHandler.npcs[c.npcIndex].updateRequired = true;
 			}
 		}	
 		c.updateRequired = true;
 	}
 
 	int tmpNWCY[] = new int[50];
 	int tmpNWCX[] = new int[50];
 	public void fmwalkto(int i, int j)
 	{
 		c.newWalkCmdSteps = 0;
 		if(++c.newWalkCmdSteps > 50)
 		c.newWalkCmdSteps = 0;
 		int k = c.absX + i;
 		k -= c.mapRegionX * 8;
 		c.getNewWalkCmdX()[0] = c.getNewWalkCmdY()[0] = tmpNWCX[0] = tmpNWCY[0] = 0;
 		int l = c.absY + j;
 		l -= c.mapRegionY * 8;
 		c.isRunning2 = false;
 		c.isRunning = false;
 		c.getNewWalkCmdX()[0] += k;
 		c.getNewWalkCmdY()[0] += l;
 		c.poimiY = l;
 		c.poimiX = k;
 	}
 
 	public String GetNpcName(int NpcID) {
 		for (int i = 0; i < NPCHandler.maxListedNPCs; i++) {
 			if (NPCHandler.NpcList[i] != null) {
 				if (NPCHandler.NpcList[i].npcId == NpcID) {
 					return NPCHandler.NpcList[i].npcName;
 				}
 			}
 		}
 		return "NPC Not Listed" + NpcID;
 	}
 
 	public void sendQuest(String s, int id) {
 		try {
 			c.outStream.createFrameVarSizeWord(126);
 			c.outStream.writeString(s);
 			c.outStream.writeWordA(id);
 			c.outStream.endFrameVarSizeWord();
 		} catch (Exception e) {
 		}
 	}
 
 	public String checkTimeOfDay()
 	{	
 		Calendar cal = new GregorianCalendar();	
 		int TIME_OF_DAY = cal.get(Calendar.AM_PM);		
 		if (TIME_OF_DAY > 0)
 			return "PM";
 		else
 			return "AM";
 	}
 	public void GWKC() {
 		walkableInterface(16210);
 		sendFrame126(""+c.Arma+"", 16216);
 		sendFrame126(""+c.Band+"", 16217);
 		sendFrame126(""+c.Sara+"", 16218);
 		sendFrame126(""+c.Zammy+"", 16219);
 	}
 public void ResetGWKC() {
 if(c.inGWD()) {
 c.Arma = 0;
 c.Band = 0;
 c.Sara = 0;
 c.Zammy = 0;
 c.sendMessage("A magical force reseted your kill count!");
 }
 }
 	
 	public void checkDateAndTime()
 	{
 		Calendar cal = new GregorianCalendar();	
 		
 		int YEAR = cal.get(Calendar.YEAR);
 		int MONTH = cal.get(Calendar.MONTH) + 1;
 		int DAY = cal.get(Calendar.DAY_OF_MONTH);
 		int HOUR = cal.get(Calendar.HOUR_OF_DAY);
 		int MIN = cal.get(Calendar.MINUTE);
 		int SECOND = cal.get(Calendar.SECOND);
 		
 		String day = "";
 		String month = "";
 		String hour = "";
 		String minute = "";
 		String second = "";
 		
 		if (DAY < 10)
 			day = "0" + DAY;
 		else 
 			day = "" + DAY;
 		if (MONTH < 10)
 			month = "0" + MONTH;	
 		else
 			month = "" + MONTH;
 		if (HOUR < 10)
 			hour = "0" + HOUR;
 		else 
 			hour = "" + HOUR;
 		if (MIN < 10)
 			minute = "0" + MIN;
 		else
 			minute = "" + MIN;
 		if (SECOND < 10)
 			second = "0" + SECOND;
 		else
 			second = "" + SECOND;
 			
 		c.date = day + "/" + month + "/" + YEAR;	
 		c.currentTime = hour + ":" + minute + ":" + second;
 	}	
 	Properties p = new Properties();
 	
 	public void loadAnnouncements()
 	{
 		try
 		{
 			loadIni();
 		
 			if (p.getProperty("announcement1").length() > 0) {
 				c.sendMessage(p.getProperty("announcement1"));
 			}
 			if (p.getProperty("announcement2").length() > 0) {
 				c.sendMessage(p.getProperty("announcement2"));
 			}
 			if (p.getProperty("announcement3").length() > 0) {
 				c.sendMessage(p.getProperty("announcement3"));
 			}
 		}
 		catch (Exception e)
 		{
 		}
 	}
 	
 	private void loadIni()
 	{		
 		try 
 		{
 			p.load(new FileInputStream("./Announcements.ini"));
 		}
 		catch (Exception e)
 		{
 		}
 	}
 	
 	public int CraftInt, Dcolor, FletchInt;
 	
 	/**
 	 * MulitCombat icon
 	 * @param i1 0 = off 1 = on
 	 */
 	public void multiWay(int i1) {
 		synchronized(c) {
 			c.outStream.createFrame(61);
 			c.outStream.writeByte(i1);
 			c.updateRequired = true;
 			c.setAppearanceUpdateRequired(true);
 		}
 	}
 	
 	public void clearClanChat() {
 		c.clanId = -1;
 		c.inAclan = false;
 		c.CSLS = 0;
 		c.getPA().sendFrame126("Talking in: ", 18139);
 		c.getPA().sendFrame126("Owner: ", 18140);
 		for (int j = 18144; j < 18244; j++)
 			c.getPA().sendFrame126("", j);
 	}
 	
 	public void resetAutocast() {
 		c.autocastId = -1;
 		c.autocasting = false;
 		c.setSidebarInterface(0, 328);
 		c.getPA().sendFrame36(108, 0);
 		c.getItems().sendWeapon(c.playerEquipment[c.playerWeapon], c.getItems().getItemName(c.playerEquipment[c.playerWeapon]));
 	}
 	
 	public void sendFrame126(String s, int id) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null ) {
 				c.getOutStream().createFrameVarSizeWord(126);
 				c.getOutStream().writeString(s);
 				c.getOutStream().writeWordA(id);
 				c.getOutStream().endFrameVarSizeWord();
 				c.flushOutStream();
 			}
 		}
 	}
 	
 	
 	public void sendLink(String s) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null ) {
 				c.getOutStream().createFrameVarSizeWord(187);
 				c.getOutStream().writeString(s);
 			}
 		}	
 	}
 	
 	public void setSkillLevel(int skillNum, int currentLevel, int XP) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(134);
 				c.getOutStream().writeByte(skillNum);
 				c.getOutStream().writeDWord_v1(XP);
 				c.getOutStream().writeByte(currentLevel);
 				c.flushOutStream();
 			}
 		}
 	}
 public void totallevelsupdate() {
 int totalLevel = (getLevelForXP(c.playerXP[0]) + getLevelForXP(c.playerXP[1]) + getLevelForXP(c.playerXP[2]) + getLevelForXP(c.playerXP[3]) + getLevelForXP(c.playerXP[4]) + getLevelForXP(c.playerXP[5]) + getLevelForXP(c.playerXP[6]) + getLevelForXP(c.playerXP[7]) + getLevelForXP(c.playerXP[8]) + getLevelForXP(c.playerXP[9]) + getLevelForXP(c.playerXP[10]) + getLevelForXP(c.playerXP[11]) + getLevelForXP(c.playerXP[12]) + getLevelForXP(c.playerXP[13]) + getLevelForXP(c.playerXP[14]) + getLevelForXP(c.playerXP[15]) + getLevelForXP(c.playerXP[16]) + getLevelForXP(c.playerXP[17]) + getLevelForXP(c.playerXP[18]) + getLevelForXP(c.playerXP[19]) + getLevelForXP(c.playerXP[20]) + getLevelForXP(c.playerXP[21]) + getLevelForXP(c.playerXP[22]));
 		sendFrame126("Levels: "+totalLevel, 13983);
 }
 	
 	public void sendFrame106(int sideIcon) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(106);
 				c.getOutStream().writeByteC(sideIcon);
 				c.flushOutStream();
 				requestUpdates();
 			}
 		}
 	}
 	
 	public void sendFrame107() {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(107);
 				c.flushOutStream();
 			}
 		}
 	}
 	public void sendFrame36(int id, int state) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(36);
 				c.getOutStream().writeWordBigEndian(id);
 				c.getOutStream().writeByte(state);
 				c.flushOutStream();
 			}
 		}
 	}
 	
 	public void sendFrame185(int Frame) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(185);
 				c.getOutStream().writeWordBigEndianA(Frame);
 			}
 		}
 	}
 	
 	public void showInterface(int interfaceid) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(97);
 				c.getOutStream().writeWord(interfaceid);
 				c.flushOutStream();
 			}
 		}
 	}
 	
 	public void sendFrame248(int MainFrame, int SubFrame) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(248);
 				c.getOutStream().writeWordA(MainFrame);
 				c.getOutStream().writeWord(SubFrame);
 				c.flushOutStream();
 			}
 		}
 	}
 	
 	public void sendFrame246(int MainFrame, int SubFrame, int SubFrame2) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(246);
 				c.getOutStream().writeWordBigEndian(MainFrame);
 				c.getOutStream().writeWord(SubFrame);
 				c.getOutStream().writeWord(SubFrame2);
 				c.flushOutStream();
 			}
 		}
 	}
 	
 	public void sendFrame171(int MainFrame, int SubFrame) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(171);
 				c.getOutStream().writeByte(MainFrame);
 				c.getOutStream().writeWord(SubFrame);
 				c.flushOutStream();
 			}
 		}
 	}
 	
 	public void sendFrame200(int MainFrame, int SubFrame) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(200);
 				c.getOutStream().writeWord(MainFrame);
 				c.getOutStream().writeWord(SubFrame);
 				c.flushOutStream();
 			}
 		}
 	}
 	
 	public void sendFrame70(int i, int o, int id) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(70);
 				c.getOutStream().writeWord(i);
 				c.getOutStream().writeWordBigEndian(o);
 				c.getOutStream().writeWordBigEndian(id);
 				c.flushOutStream();
 			}
 		}
 	}
 
 	public void sendFrame75(int MainFrame, int SubFrame) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(75);
 				c.getOutStream().writeWordBigEndianA(MainFrame);
 				c.getOutStream().writeWordBigEndianA(SubFrame);
 				c.flushOutStream();
 			}
 		}
 	}
 	
 	public void sendFrame164(int Frame) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(164);
 				c.getOutStream().writeWordBigEndian_dup(Frame);
 				c.flushOutStream();
 			}
 		}
 	}
 	
 	public void setPrivateMessaging(int i) { // friends and ignore list status
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 		        c.getOutStream().createFrame(221);
 		        c.getOutStream().writeByte(i);
 				c.flushOutStream();
 			}
 		}
     }
 	
 	public void setChatOptions(int publicChat, int privateChat, int tradeBlock) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(206);
 				c.getOutStream().writeByte(publicChat);	
 				c.getOutStream().writeByte(privateChat);	
 				c.getOutStream().writeByte(tradeBlock);
 				c.flushOutStream();
 			}
 		}
 	}
 	
 	public void sendFrame87(int id, int state) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(87);
 				c.getOutStream().writeWordBigEndian_dup(id);	
 				c.getOutStream().writeDWord_v1(state);
 				c.flushOutStream();
 			}
 		}
 	}
 	
 	public void sendPM(long name, int rights, byte[] chatmessage, int messagesize) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrameVarSize(196);
 				c.getOutStream().writeQWord(name);
 				c.getOutStream().writeDWord(c.lastChatId++);
 				c.getOutStream().writeByte(rights);
 				c.getOutStream().writeBytes(chatmessage, messagesize, 0);
 				c.getOutStream().endFrameVarSize();
 				c.flushOutStream();
 				String chatmessagegot = Misc.textUnpack(chatmessage, messagesize);
 				String target = Misc.longToPlayerName(name);
 			}	
 		}
 	}
 	
 	public void createPlayerHints(int type, int id) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(254);
 				c.getOutStream().writeByte(type);
 				c.getOutStream().writeWord(id); 
 				c.getOutStream().write3Byte(0);
 				c.flushOutStream();
 			}
 		}
 	}
 
 	public void createObjectHints(int x, int y, int height, int pos) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(254);
 				c.getOutStream().writeByte(pos);
 				c.getOutStream().writeWord(x);
 				c.getOutStream().writeWord(y);
 				c.getOutStream().writeByte(height);
 				c.flushOutStream();
 			}
 		}
 	}
 	
 	public void loadPM(long playerName, int world) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				if(world != 0) {
 		            world += 9;
 				} else if(!Config.WORLD_LIST_FIX) {
 					world += 1;
 				}	
 				c.getOutStream().createFrame(50);
 				c.getOutStream().writeQWord(playerName);
 				c.getOutStream().writeByte(world);
 				c.flushOutStream();
 			}
 		}
 	}
 	
 	public void removeAllWindows() {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getPA().resetVariables();
 				c.getOutStream().createFrame(219);
 				c.flushOutStream();
 			}
 		}
 	}
 	
 	public void closeAllWindows() {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(219);
 				c.flushOutStream();
 				c.isBanking = false;
 				c.isShopping = false;
 				c.getTradeAndDuel().declineTrade();
 			}
 		}
 	}
 	
 	
 	public void sendFrame34(int id, int slot, int column, int amount) {
 		
 			if(c.getOutStream() != null && c != null) {
 				c.outStream.createFrameVarSizeWord(34); // init item to smith screen
 				c.outStream.writeWord(column); // Column Across Smith Screen
 				c.outStream.writeByte(4); // Total Rows?
 				c.outStream.writeDWord(slot); // Row Down The Smith Screen
 				c.outStream.writeWord(id+1); // item
 				c.outStream.writeByte(amount); // how many there are?
 				c.outStream.endFrameVarSizeWord();
 			}
 		}
 	
 	
 	public void Frame34(int frame, int item, int slot, int amount){
 		
 			if(c.getOutStream() != null && c != null) {
 				c.outStream.createFrameVarSizeWord(34);
 				c.outStream.writeWord(frame);
 				c.outStream.writeByte(slot);
 				c.outStream.writeWord(item+1);
 				c.outStream.writeByte(255);
 				c.outStream.writeDWord(amount);
 				c.outStream.endFrameVarSizeWord();
 			}
 		
 	}
 
 
 	public void Summon(int frame, int item, int slot, int amount){
 		
 			if(c.getOutStream() != null && c != null) {
 				c.outStream.createFrameVarSizeWord(34);
 				c.outStream.writeWord(frame);
 				c.outStream.writeByte(c.summoningslot);
 				c.outStream.writeWord(item+1);
 				c.outStream.writeByte(255);
 				c.outStream.writeDWord(amount);
 				c.outStream.endFrameVarSizeWord();
 			}
 		
 	}
 	
 	public void walkableInterface(int id) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(208);
 		        c.getOutStream().writeWordBigEndian_dup(id);
 				c.flushOutStream();
 			}
 		}
 	}
 	
 	public int mapStatus = 0;
 	public void sendFrame99(int state) { // used for disabling map
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				if(mapStatus != state) {
 					mapStatus = state;
 					c.getOutStream().createFrame(99);
 			        c.getOutStream().writeByte(state);
 					c.flushOutStream();
 				}
 			}
 		}
 	}
 	
 	public void sendCrashFrame() { // used for crashing cheat clients
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(123);
 				c.flushOutStream();
 			}
 		}
 	}
 public void writeEnergy() {
 		sendFrame126(c.playerEnergy + "%", 149);
 	}
 	
 	public void resting() {
 		c.isResting = true;
 		c.startAnimation(4853);
 	}
 	
 	public int raiseTimer() {
 		if(!c.isResting) 
 			if (c.playerLevel[16] >= 2 && c.playerLevel[16] < 10)
 				return 6500;
 			if (c.playerLevel[16] >= 10 && c.playerLevel[16] < 25)
 				return 6000;
 			if (c.playerLevel[16] >= 25 && c.playerLevel[16] < 40)
 				return 5500;
 			if (c.playerLevel[16] >= 40 && c.playerLevel[16] < 55)
 				return 5000;
 			if (c.playerLevel[16] >= 55 && c.playerLevel[16] < 70)
 				return 4500;
 			if (c.playerLevel[16] >= 70 && c.playerLevel[16] < 85)
 				return 4000;
 			if (c.playerLevel[16] >= 85 && c.playerLevel[16] < 99)
 				return 3500;
 			if (c.playerLevel[16] == 99)
 				return 3000;
 			return 7000;
 	}
 		
 	public int raiseTimer2() {
 		if (c.isResting) 
 			if (c.playerLevel[16] >= 2 && c.playerLevel[16] < 10)
 				return 2250;
 			if (c.playerLevel[16] >= 10 && c.playerLevel[16] < 25)
 				return 2000;
 			if (c.playerLevel[16] >= 25 && c.playerLevel[16] < 40)
 				return 1750;
 			if (c.playerLevel[16] >= 40 && c.playerLevel[16] < 55)
 				return 1500;
 			if (c.playerLevel[16] >= 55 && c.playerLevel[16] < 70)
 				return 1250;
 			if (c.playerLevel[16] >= 70 && c.playerLevel[16] < 85)
 				return 1000;
 			if (c.playerLevel[16] >= 85 && c.playerLevel[16] < 99)
 				return 750;
 			if (c.playerLevel[16] == 99)
 				return 500;
 			return 2500;			
 	}
 	
 	
 	/**
 	* Reseting animations for everyone
 	**/
 
 	public void frame1() {
 		synchronized(c) {
 			for(int i = 0; i < Config.MAX_PLAYERS; i++) {
 				if(Server.playerHandler.players[i] != null) {
 					Client person = (Client)Server.playerHandler.players[i];
 					if(person != null) {
 						if(person.getOutStream() != null && !person.disconnected) {
 							if(c.distanceToPoint(person.getX(), person.getY()) <= 25){	
 								person.getOutStream().createFrame(1);
 								person.flushOutStream();
 								person.getPA().requestUpdates();
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	* Creating projectile
 	**/
 	public void createProjectile(int x, int y, int offX, int offY, int angle, int speed, int gfxMoving, int startHeight, int endHeight, int lockon, int time) {      
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(85);
 		        c.getOutStream().writeByteC((y - (c.getMapRegionY() * 8)) - 2);
 		        c.getOutStream().writeByteC((x - (c.getMapRegionX() * 8)) - 3);
 		        c.getOutStream().createFrame(117);
 		        c.getOutStream().writeByte(angle);
 		        c.getOutStream().writeByte(offY);
 		        c.getOutStream().writeByte(offX);
 		        c.getOutStream().writeWord(lockon);
 		        c.getOutStream().writeWord(gfxMoving);
 		        c.getOutStream().writeByte(startHeight);
 		        c.getOutStream().writeByte(endHeight);
 		        c.getOutStream().writeWord(time);
 			    c.getOutStream().writeWord(speed);
 				c.getOutStream().writeByte(16);
 				c.getOutStream().writeByte(64);
 				c.flushOutStream();
 			}
 		}
     }
 	
 	public void createProjectile2(int x, int y, int offX, int offY, int angle, int speed, int gfxMoving, int startHeight, int endHeight, int lockon, int time, int slope) {      
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(85);
 		        c.getOutStream().writeByteC((y - (c.getMapRegionY() * 8)) - 2);
 		        c.getOutStream().writeByteC((x - (c.getMapRegionX() * 8)) - 3);
 		        c.getOutStream().createFrame(117);
 		        c.getOutStream().writeByte(angle);
 		        c.getOutStream().writeByte(offY);
 		        c.getOutStream().writeByte(offX);
 		        c.getOutStream().writeWord(lockon);
 		        c.getOutStream().writeWord(gfxMoving);
 		        c.getOutStream().writeByte(startHeight);
 		        c.getOutStream().writeByte(endHeight);
 		        c.getOutStream().writeWord(time);
 			    c.getOutStream().writeWord(speed);
 				c.getOutStream().writeByte(slope);
 				c.getOutStream().writeByte(64);
 				c.flushOutStream();
 			}
 		}
     }
 	
 	// projectiles for everyone within 25 squares
 	public void createPlayersProjectile(int x, int y, int offX, int offY, int angle, int speed, int gfxMoving, int startHeight, int endHeight, int lockon, int time) {
 		synchronized(c) {
 			for(int i = 0; i < Config.MAX_PLAYERS; i++) {
 				Player p = Server.playerHandler.players[i];
 				if(p != null) {
 					Client person = (Client)p;
 					if(person != null) {
 						if(person.getOutStream() != null) {
 							if(person.distanceToPoint(x, y) <= 25){
 								if (p.heightLevel == c.heightLevel)
 									person.getPA().createProjectile(x, y, offX, offY, angle, speed, gfxMoving, startHeight, endHeight, lockon, time);
 							}
 						}
 					}	
 				}
 			}
 		}
 	}
 	
 	public void createPlayersProjectile2(int x, int y, int offX, int offY, int angle, int speed, int gfxMoving, int startHeight, int endHeight, int lockon, int time, int slope) {
 		synchronized(c) {
 			for(int i = 0; i < Config.MAX_PLAYERS; i++) {
 				Player p = Server.playerHandler.players[i];
 				if(p != null) {
 					Client person = (Client)p;
 					if(person != null) {
 						if(person.getOutStream() != null) {
 							if(person.distanceToPoint(x, y) <= 25){	
 								person.getPA().createProjectile2(x, y, offX, offY, angle, speed, gfxMoving, startHeight, endHeight, lockon, time, slope);	
 							}
 						}
 					}	
 				}
 			}
 		}
 	}
 	
 
 	/**
 	** GFX
 	**/
 	public void stillGfx(int id, int x, int y, int height, int time) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(85);
 				c.getOutStream().writeByteC(y - (c.getMapRegionY() * 8));
 				c.getOutStream().writeByteC(x - (c.getMapRegionX() * 8));
 				c.getOutStream().createFrame(4);
 				c.getOutStream().writeByte(0);
 				c.getOutStream().writeWord(id);
 				c.getOutStream().writeByte(height);
 				c.getOutStream().writeWord(time);
 				c.flushOutStream();
 			}
 		}
 	}
 	
 	//creates gfx for everyone
 	public void createPlayersStillGfx(int id, int x, int y, int height, int time) {
 		synchronized(c) {
 			for(int i = 0; i < Config.MAX_PLAYERS; i++) {
 				Player p = Server.playerHandler.players[i];
 				if(p != null) {
 					Client person = (Client)p;
 					if(person != null) {
 						if(person.getOutStream() != null) {
 							if(person.distanceToPoint(x, y) <= 25){	
 								person.getPA().stillGfx(id, x, y, height, time);
 							}
 						}
 					}	
 				}
 			}
 		}
 	}
 	
 	/**
 	* Objects, add and remove
 	**/
 	public void object(int objectId, int objectX, int objectY, int face, int objectType) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(85);
 				c.getOutStream().writeByteC(objectY - (c.getMapRegionY() * 8));
 				c.getOutStream().writeByteC(objectX - (c.getMapRegionX() * 8));
 				c.getOutStream().createFrame(101);
 				c.getOutStream().writeByteC((objectType<<2) + (face&3));
 				c.getOutStream().writeByte(0);
 			
 				if (objectId != -1) { // removing
 					c.getOutStream().createFrame(151);
 					c.getOutStream().writeByteS(0);
 					c.getOutStream().writeWordBigEndian(objectId);
 					c.getOutStream().writeByteS((objectType<<2) + (face&3));
 				}
 				c.flushOutStream();
 			}	
 		}
 	}
 	
 	public void checkObjectSpawn(int objectId, int objectX, int objectY, int face, int objectType) {
 		if (c.distanceToPoint(objectX, objectY) > 60)
 			return;
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				c.getOutStream().createFrame(85);
 				c.getOutStream().writeByteC(objectY - (c.getMapRegionY() * 8));
 				c.getOutStream().writeByteC(objectX - (c.getMapRegionX() * 8));
 				c.getOutStream().createFrame(101);
 				c.getOutStream().writeByteC((objectType<<2) + (face&3));
 				c.getOutStream().writeByte(0);
 			
 				if (objectId != -1) { // removing
 					c.getOutStream().createFrame(151);
 					c.getOutStream().writeByteS(0);
 					c.getOutStream().writeWordBigEndian(objectId);
 					c.getOutStream().writeByteS((objectType<<2) + (face&3));
 				}
 				c.flushOutStream();
 			}	
 		}
 	}
 	
 
 	/**
 	* Show option, attack, trade, follow etc
 	**/
 	public String optionType = "null";
 	public void showOption(int i, int l, String s, int a) {
 		synchronized(c) {
 			if(c.getOutStream() != null && c != null) {
 				if(!optionType.equalsIgnoreCase(s)) {
 					optionType = s;
 					c.getOutStream().createFrameVarSize(104);
 					c.getOutStream().writeByteC(i);
 					c.getOutStream().writeByteA(l);
 					c.getOutStream().writeString(s);
 					c.getOutStream().endFrameVarSize();
 					c.flushOutStream();
 				}
 			}
 		}
 	}
 	
 	/**
 	* Open bank
 	**/
 	public void openUpBank(){
 			if(c.inWild())
 			{
 				c.sendMessage("You can't bank in the wilderness!");
 				return;
 			}
 			if(c.getOutStream() != null && c != null) {
 				c.isBanking = true;
 				c.getItems().resetItems(5064);
 				c.getItems().rearrangeBank();
 				c.getItems().resetBank();
 				c.getItems().resetTempItems();
 				c.getOutStream().createFrame(248);
 				c.getOutStream().writeWordA(23000);
 				c.getOutStream().writeWord(5063);
 				c.flushOutStream();
 			}
 		
 	}
 	
 	/**
 	* Private Messaging
 	**/	
 	public void logIntoPM() {
 		setPrivateMessaging(2);
 		for(int i1 = 0; i1 < Config.MAX_PLAYERS; i1++) {
 			Player p = Server.playerHandler.players[i1];
 			if(p != null && p.isActive) {
 				Client o = (Client)p;
 				if(o != null) {
 					o.getPA().updatePM(c.playerId, 1);
 				}
 			}
 		}
 		boolean pmLoaded = false;
 
 		for(int i = 0; i < c.friends.length; i++) {
 			if(c.friends[i] != 0)  {
 				for(int i2 = 1; i2 < Config.MAX_PLAYERS; i2++) {
 					Player p = Server.playerHandler.players[i2];
 					if (p != null && p.isActive && Misc.playerNameToInt64(p.playerName) == c.friends[i])  {
 						Client o = (Client)p;
 						if(o != null) {
 							if (c.playerRights >= 2 || p.privateChat == 0 || (p.privateChat == 1 && o.getPA().isInPM(Misc.playerNameToInt64(c.playerName)))) {
 			 		 			loadPM(c.friends[i], 1);
 			 		 			pmLoaded = true;
 							}
 							break;
 						}
 					}
 				}
 				if(!pmLoaded) {	
 					loadPM(c.friends[i], 0);
 				}
 				pmLoaded = false;
 			}
 			for(int i1 = 1; i1 < Config.MAX_PLAYERS; i1++) {
 				Player p = Server.playerHandler.players[i1];
     			if(p != null && p.isActive) {
 					Client o = (Client)p;
 					if(o != null) {
 						o.getPA().updatePM(c.playerId, 1);
 					}
 				}
 			}
 		}
 	}
 	
 	
 	public void updatePM(int pID, int world) { // used for private chat updates
 		Player p = Server.playerHandler.players[pID];
 		if(p == null || p.playerName == null || p.playerName.equals("null")){
 			return;
 		}
 		Client o = (Client)p;
 		if(o == null) {
 			return;
 		}
         long l = Misc.playerNameToInt64(Server.playerHandler.players[pID].playerName);
 
         if (p.privateChat == 0) {
             for (int i = 0; i < c.friends.length; i++) {
                 if (c.friends[i] != 0) {
                     if (l == c.friends[i]) {
                         loadPM(l, world);
                         return;
                     }
                 }
             }
         } else if (p.privateChat == 1) {
             for (int i = 0; i < c.friends.length; i++) {
                 if (c.friends[i] != 0) {
                     if (l == c.friends[i]) {
                         if (o.getPA().isInPM(Misc.playerNameToInt64(c.playerName))) {
                             loadPM(l, world);
                             return;
                         } else {
                             loadPM(l, 0);
                             return;
                         }
                     }
                 }
             }
         } else if (p.privateChat == 2) {
             for (int i = 0; i < c.friends.length; i++) {
                 if (c.friends[i] != 0) {
                     if (l == c.friends[i] && c.playerRights < 2) {
                         loadPM(l, 0);
                         return;
                     }
                 }
             }
         }
     }
 	
 	public boolean isInPM(long l) {
         for (int i = 0; i < c.friends.length; i++) {
             if (c.friends[i] != 0) {
                 if (l == c.friends[i]) {
                     return true;
                 }
             }
         }
         return false;
     }
 	
 	
 	/**
 	 * Drink AntiPosion Potions
 	 * @param itemId The itemId
 	 * @param itemSlot The itemSlot
 	 * @param newItemId The new item After Drinking
 	 * @param healType The type of poison it heals
 	 */
 	public void potionPoisonHeal(int itemId, int itemSlot, int newItemId, int healType) {
 		c.attackTimer = c.getCombat().getAttackDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
 		if(c.duelRule[5]) {
 			c.sendMessage("Potions has been disabled in this duel!");
 			return;
 		}
 		if(!c.isDead && System.currentTimeMillis() - c.foodDelay > 2000) {
 			if(c.getItems().playerHasItem(itemId, 1, itemSlot)) {
 				c.sendMessage("You drink the "+c.getItems().getItemName(itemId).toLowerCase()+".");
 				c.foodDelay = System.currentTimeMillis();
 				// Actions
 				if(healType == 1) {
 					//Cures The Poison
 				} else if(healType == 2) {
 					//Cures The Poison + protects from getting poison again
 				}
 				c.startAnimation(0x33D);
 				c.getItems().deleteItem(itemId, itemSlot, 1);
 				c.getItems().addItem(newItemId, 1);
 				requestUpdates();
 			}
 		}
 	}
 	
 	
 	/**
 	* Magic on items
 	**/
 	
 	public void magicOnItems(int slot, int itemId, int spellId) {
 		switch(spellId) {
 			case 1162: // low alch
 			if(System.currentTimeMillis() - c.alchDelay > 1000) {	
 				if(!c.getCombat().checkMagicReqs(49)) {
 					break;
 				}
 				if(itemId == 995) {
 					c.sendMessage("You can't alch coins.");
 					break;
 				}
 				c.getItems().deleteItem(itemId, slot, 1);
 				c.getItems().addItem(995, c.getShops().getItemShopValue(itemId)/3);
 				c.startAnimation(c.MAGIC_SPELLS[49][2]);
 				c.gfx100(c.MAGIC_SPELLS[49][3]);
 				c.alchDelay = System.currentTimeMillis();
 				sendFrame106(6);
 				addSkillXP(c.MAGIC_SPELLS[49][7] * Config.MAGIC_EXP_RATE, 6);
 				refreshSkill(6);
 			}
 			break;
 			
 			case 1178: // high alch
 			if(System.currentTimeMillis() - c.alchDelay > 2000) {	
 				if(!c.getCombat().checkMagicReqs(50)) {
 					break;
 				}
 				if(itemId == 995) {
 					c.sendMessage("You can't alch coins.");
 					break;
 				}				
 				c.getItems().deleteItem(itemId, slot, 1);
 				c.getItems().addItem(995, (int)(c.getShops().getItemShopValue(itemId)*.75));
 				c.startAnimation(c.MAGIC_SPELLS[50][2]);
 				c.gfx100(c.MAGIC_SPELLS[50][3]);
 				c.alchDelay = System.currentTimeMillis();
 				sendFrame106(6);
 				addSkillXP(c.MAGIC_SPELLS[50][7] * Config.MAGIC_EXP_RATE, 6);
 				refreshSkill(6);
 			}
 			break;
 			case 8349:
 				handleAlt(itemId);
 			break;
 		}
 	}
 	
 	/**
 	* Dieing
 	**/
 
 	public String getKM() {
 		int kMCount = Misc.random(11);
 		switch (kMCount) {
 			case 0: return "With a crushing blow, you defeat "+ c.playerName+".";
 			case 1: return "It's a humiliating defeat for "+ c.playerName+".";
 			case 2: return c.playerName +" didn't stand a chance against you.";
 			case 3: return "You've defeated "+ c.playerName+".";
 			case 4: return c.playerName +" regrets the day they met you in combat.";
 			case 5: return "It's all over for "+c.playerName+".";
 			case 6: return c.playerName +" falls before you might.";
 			case 7: return "Can anyone defeat you? Certainly not "+c.playerName+".";
 			case 8: return c.playerName +" has fallen under your might.";
 			case 9: return c.playerName +" crumbled under your power.";
 			case 10: return "You have proven "+ c.playerName+" to be an unworthy opponent.";
 			default: return "You were clearly a better fighter than "+c.playerName+".";
 		}
 	}
 
 	
     public void applyDead() {    
 	    int weapon = c.playerEquipment[c.playerWeapon];
 		c.respawnTimer = 15;
 				c.faceUpdate(0);
 		c.npcIndex = 0;
 		c.playerIndex = 0;
 		c.stopMovement();
 		c.isDead = false;
 		Client o = (Client) Server.playerHandler.players[c.killerId];
 		if(c.duelStatus != 6) {
 			c.killerId = findKiller();
 			if(o != null) {
 				c.playerKilled = c.playerId;
 				if(o.duelStatus == 5) {
 					o.duelStatus++;
 				}
 				if (Server.playerHandler.players[c.playerId].connectedFrom != o.lastKilled && c.duelStatus == 0) {
 					o.pkPoints = (o.pkPoints + 4);
 					o.sendMessage("You recieved 4 pkPoints");
 					o.lastKilled = Server.playerHandler.players[c.playerId].connectedFrom;
 				} else {
 					o.sendMessage("You do not recieve pk points because you have killed " +c.playerName+ " twice in a row.");
 				}
 			}
 		}
 
         
 		if (c.duelStatus <= 4) {
 			if (CastleWars.isInCw(c)) {
             c.cwDeaths += 1;
             o.cwKills += 1;
             }
 			c.sendMessage(Config.DEATH_MESSAGE);
 		} else if(c.duelStatus != 6 || !c.inArena()) {
 			c.sendMessage("You have lost the duel!");
 			o.getPA().movePlayer(
 			Config.DUELING_RESPAWN_X + (Misc.random(Config.RANDOM_DUELING_RESPAWN)),
 			Config.DUELING_RESPAWN_Y + (Misc.random(Config.RANDOM_DUELING_RESPAWN)), 0);
 			}
 		if (weapon == CastleWars.SARA_BANNER || weapon == CastleWars.ZAMMY_BANNER) {
             c.getItems().removeItem(weapon, 3);
             c.getItems().deleteItem2(weapon, 1);
             CastleWars.dropFlag(c, weapon);
             }
 		resetDamageDone();
 		c.DC++;
 		o.KC++;	
 		c.specAmount = 10;
 		c.getItems().addSpecialBar(c.playerEquipment[c.playerWeapon]);
 		c.lastVeng = 0;
 		c.vengOn = false;
 		resetFollowers();
 		c.attackTimer = 10;
 		if (Server.playerHandler.players[c.killerId].connectedFrom.equals(Server.playerHandler.players[c.playerKilled].connectedFrom)) {
 					o.sendMessage("You Don't Recieve PK Points for killing yourself!");
 		c.faceUpdate(0);
 		c.npcIndex = 0;
 		c.playerIndex = 0;
 		c.stopMovement();
 		resetDamageDone();
 		c.lastVeng = 0;
 		c.vengOn = false;
 		resetFollowers();
 		c.attackTimer = 10;
 		removeAllWindows();
 		c.tradeResetNeeded = true;
 		return;
 		}
 	}
 	
 			public void dropitems() 
 	{
 	if(c.lastsummon > 0) {
 if(c.totalstored > 0) {
 c.firstslot();
 for(int i = 0; i < 29; i += 1)
 {
 Server.itemHandler.createGroundItem(c, c.storeditems[i], Server.npcHandler.npcs[c.summoningnpcid].absX, Server.npcHandler.npcs[c.summoningnpcid].absY, 1, c.playerId);
 c.storeditems[i] = -1;
 c.occupied[i] = false;
 }
 }
 c.lastsummon = -1;
 c.totalstored = 0;
 c.summoningnpcid = 0;
 c.summoningslot = 0;
 c.storing = false;
 c.sendMessage("Your BoB items have drop on the floor");
 }	
 	}
 	/**
 	* Location change for digging, levers etc
 	**/
 	
 	
 	public void resetDamageDone() {
 		for (int i = 0; i < PlayerHandler.players.length; i++) {
 			if (PlayerHandler.players[i] != null) {
 				PlayerHandler.players[i].damageTaken[c.playerId] = 0;			
 			}		
 		}	
 	}
 	/*
 *Vengeance
 */
 	public void castVeng() {
 	if(c.playerLevel[6] < 94) {
 		c.sendMessage("You need a magic level of 94 to cast this spell.");
 		return;
 	}
 	if(c.playerLevel[1] < 40) {
 		c.sendMessage("You need a defence level of 40 to cast this spell.");
 		return;
 	}
 	if(!c.getItems().playerHasItem(9075, 4) || !c.getItems().playerHasItem(557, 10) || !c.getItems().playerHasItem(560, 2)) {
 		c.sendMessage("You don't have the required runes to cast this spell.");
 		return;
 	}
 	if(System.currentTimeMillis() - c.lastCast < 30000) {
 		c.sendMessage("You can only cast vengeance every 30 seconds.");
 		return;
 	}
 	if(c.vengOn) {
 		c.sendMessage("You already have vengeance casted.");
 		return;
 	}
 	c.startAnimation(4410);
 	c.gfx100(726);//Just use c.gfx100
 	c.getItems().deleteItem2(9075, 4);
 	c.getItems().deleteItem2(557, 10);//For these you need to change to deleteItem(item, itemslot, amount);.
 	c.getItems().deleteItem2(560, 2);
 	addSkillXP(2000, 6);
 	c.stopMovement();
 	refreshSkill(6);
 	c.vengOn = true;
 	c.lastCast = System.currentTimeMillis();
 	}
 
 	public void vengOther() {	
 	if (c.playerIndex > 0) {	
 	Player q = Server.playerHandler.players[c.playerIndex];			
 	final int oX = q.getX();
 	final int oY = q.getY();
 	if(c.playerLevel[6] < 93) {
 		c.sendMessage("You need a magic level of 93 to cast this spell.");
 	c.getCombat().resetPlayerAttack();
 	c.stopMovement();
 	c.turnPlayerTo(oX,oY);
 		return;
 	}
                 if (!q.acceptAid) {
                 c.sendMessage("This player has their accept Aid off, therefore you cannot veng them!");
                 return;
                 }
 
 	if(c.playerLevel[1] < 40) {
 		c.sendMessage("You need a defence level of 40 to cast this spell.");
 	c.getCombat().resetPlayerAttack();
 	c.stopMovement();
 	c.turnPlayerTo(oX,oY);
 		return;
 	}
 	if(!c.getItems().playerHasItem(9075, 3) || !c.getItems().playerHasItem(557, 10) || !c.getItems().playerHasItem(560, 2)) {
 		c.sendMessage("You don't have the required runes to cast this spell.");
 	c.getCombat().resetPlayerAttack();
 	c.stopMovement();
 	c.turnPlayerTo(oX,oY);
 		return;
 	}
 	if(System.currentTimeMillis() - c.lastCast < 30000) {
 		c.sendMessage("You can only cast vengeance every 30 seconds.");
 	c.getCombat().resetPlayerAttack();
 	c.stopMovement();
 	c.turnPlayerTo(oX,oY);
 		return;
 	}
 	if(q.vengOn) {
 		c.sendMessage("That player already have vengeance casted.");
 	c.getCombat().resetPlayerAttack();
 	c.stopMovement();
 	c.turnPlayerTo(oX,oY);
 		return;
 	}
 	c.startAnimation(4411);
 	q.gfx100(725);//Just use c.gfx100
 	c.getItems().deleteItem2(9075, 3);
 	c.getItems().deleteItem2(557, 10);//For these you need to change to deleteItem(item, itemslot, amount);.
 	c.getItems().deleteItem2(560, 2);
 	q.vengOn = true;
 	addSkillXP(2000, 6);
 	c.turnPlayerTo(oX,oY);
 	refreshSkill(6);
 	c.getCombat().resetPlayerAttack();
 	c.stopMovement();
 	c.lastCast = System.currentTimeMillis();
 }
 }
 public boolean wearingCape(int cape) {
 int capes[] = {
 9747, 9748, 9750, 9751,
 9753, 9754, 9756, 9757,
 9759, 9760, 9762, 9763,
 9765, 9766, 9768, 9769,
 9771, 9772, 9774, 9775,
 9777, 9778, 9780, 9781,
 9783, 9784, 9786, 9787,
 9789, 9790, 9792, 9793,
 9795, 9796, 9798, 9799,
 9801, 9802, 9804, 9805,
 9807, 9808, 9810, 9811,
 9813, 9948, 9949, 12170
 };
 for(int i = 0; i < capes.length; i++) {
 if(capes[i] == cape) {
 return true;
 }
 }
 return false;
 }
 
 
 	public void vengMe() {
 		if (System.currentTimeMillis() - c.lastVeng > 30000) {
 			if (c.getItems().playerHasItem(557,10) && c.getItems().playerHasItem(9075,4) && c.getItems().playerHasItem(560,2)) {
 				c.vengOn = true;
 				c.lastVeng = System.currentTimeMillis();
 				c.startAnimation(4410);
 				c.gfx100(726);
 				c.getItems().deleteItem(557,c.getItems().getItemSlot(557),10);
 				c.getItems().deleteItem(560,c.getItems().getItemSlot(560),2);
 				c.getItems().deleteItem(9075,c.getItems().getItemSlot(9075),4);
 			} else {
 				c.sendMessage("You do not have the required runes to cast this spell. (9075 for astrals)");
 			}
 		} else {
 			c.sendMessage("You must wait 30 seconds before casting this again.");
 		}
 	}
 	
 	public int skillcapeGfx(int cape) {
 		int capeGfx[][] = {
 			{9747, 823}, {9748, 823},
 			{9750, 828}, {9751, 828},
 			{9753, 824}, {9754, 824},
 			{9756, 832}, {9757, 832},
 			{9759, 829}, {9760, 829},
 			{9762, 813}, {9763, 813},
 			{9765, 817}, {9766, 817},
 			{9768, 833}, {9769, 833},
 			{9771, 830}, {9772, 830},
 			{9774, 835}, {9775, 835},
 			{9777, 826}, {9778, 826},
 			{9780, 818}, {9781, 818},
 			{9783, 812}, {9784, 812},
 			{9786, 827}, {9787, 827},
 			{9789, 820}, {9790, 820},
 			{9792, 814}, {9793, 814},
 			{9795, 815}, {9796, 815},
 			{9798, 819}, {9799, 819},
 			{9801, 821}, {9802, 821},
 			{9804, 831}, {9805, 831},
 			{9807, 822}, {9808, 822},
 			{9810, 825}, {9811, 825},
 			{9948, 907}, {9949, 907},
 			{9813, 816}, {12170, 1515}
 		};
 		for(int i = 0; i < capeGfx.length; i++) {
 			if(capeGfx[i][0] == cape) {
 				return capeGfx[i][1];
 			}
 		}
 		return -1;
 	}
 	
 	public int skillcapeEmote(int cape) {
 		int capeEmote[][] = {
 			{9747, 4959}, {9748, 4959},
 			{9750, 4981}, {9751, 4981},
 			{9753, 4961}, {9754, 4961},
 			{9756, 4973}, {9757, 4973},
 			{9759, 4979}, {9760, 4979},
 			{9762, 4939}, {9763, 4939},
 			{9765, 4947}, {9766, 4947},
 			{9768, 4971}, {9769, 4971},
 			{9771, 4977}, {9772, 4977},
 			{9774, 4969}, {9775, 4969},
 			{9777, 4965}, {9778, 4965},
 			{9780, 4949}, {9781, 4949},
 			{9783, 4937}, {9784, 4937},
 			{9786, 4967}, {9787, 4967},
 			{9789, 4953}, {9790, 4953},
 			{9792, 4941}, {9793, 4941},
 			{9795, 4943}, {9796, 4943},
 			{9798, 4951}, {9799, 4951},
 			{9801, 4955}, {9802, 4955},
 			{9804, 4975}, {9805, 4975},
 			{9807, 4957}, {9808, 4957},
 			{9810, 4963}, {9811, 4963},
 			{9948, 5158}, {9949, 5158},
 			{9813, 4945}, {12170, 8525}
 		};
 		for(int i = 0; i < capeEmote.length; i++) {
 			if(capeEmote[i][0] == cape) {
 				return capeEmote[i][1];
 			}
 		}
 		return -1;
 	}
 
 	public void resetTb() {
 		c.teleBlockLength = 0;
 		c.teleBlockDelay = 0;	
 	}
 	
 	public void handleStatus(int i, int i2, int i3) {
 		//Sanity u so smart
 	}
 	
 	public void resetFollowers() {
 		for (int j = 0; j < Server.playerHandler.players.length; j++) {
 			if (Server.playerHandler.players[j] != null) {
 				if (Server.playerHandler.players[j].followId == c.playerId) {
 					Client c = (Client)Server.playerHandler.players[j];
 					c.getPA().resetFollow();
 				}			
 			}		
 		}	
 	}
 	
 	public void giveLife() {
 		c.isDead = false;
 		c.faceUpdate(-1);
 		c.freezeTimer = 0;
 		
 		if(c.playerRights == 3) {
 		for (int i = 0; i < 20; i++) {
 			c.playerLevel[i] = getLevelForXP(c.playerXP[i]);
 			c.getPA().refreshSkill(i);
 		}
 		c.getCombat().resetPrayers();
 
 		c.teleportToX = 3237;
 		c.teleportToY = 3523;
 PlayerSave.saveGame(c);
 c.getPA().resetTzhaar();
 requestUpdates();
 return;
 		
 		}
 		if(c.duelStatus <= 4 && !c.getPA().inPitsWait()) { // if we are not in a duel we must be in wildy so remove items
 			if (!CastleWars.isInCw(c) && !c.inPits && !c.inFightCaves() && !c.inPcGame()) {
 					c.getItems().resetKeepItems();
 				if((c.playerRights == 2 && Config.ADMIN_DROP_ITEMS) || c.playerRights != 2) {
 					if(!c.isSkulled && !c.isInArd()) {	// what items to keep
 						c.getItems().keepItem(0, true);
 						c.getItems().keepItem(1, true);	
 						c.getItems().keepItem(2, true);
 					}	
 					if(c.prayerActive[10] || c.isInArd() && System.currentTimeMillis() - c.lastProtItem > 700) {
 						c.getItems().keepItem(3, true);
 					}
 					c.getItems().dropAllItems(); // drop all items
 					c.getItems().deleteAllItems(); // delete all items
 				
 					if(!c.isSkulled && !c.isInArd()) { // add the kept items once we finish deleting and dropping them	
 						for (int i1 = 0; i1 < 3; i1++) {
 							if(c.itemKeptId[i1] > 0) {
 								c.getItems().addItem(c.itemKeptId[i1], 1);
 							}
 						}
 					}	
 					if(c.prayerActive[10] || c.isInArd()) { // if we have protect items 
 						if(c.itemKeptId[3] > 0) {
 							c.getItems().addItem(c.itemKeptId[3], 1);
 						}
 					}
 				}
 				c.getItems().resetKeepItems();
 			} else if (c.inPits) {
 				Server.fightPits.removePlayerFromPits(c.playerId);
 				c.pitsStatus = 1;
 			}
 		}
 		c.getCombat().resetPrayers();
 		for (int i = 0; i < 25; i++) {
 			c.playerLevel[i] = getLevelForXP(c.playerXP[i]);
 			c.getPA().refreshSkill(i);
 		}
 		if (c.pitsStatus == 1) {
 			movePlayer(2399, 5173, 0);
 		}
 		else if (CastleWars.isInCw(c)) {
             if (CastleWars.getTeamNumber(c) == 1) {
                 c.getPA().movePlayer(2426 + Misc.random(3), 3076 - Misc.random(3), 1);
             } else {
                 c.getPA().movePlayer(2373 + Misc.random(3), 3131 - Misc.random(3), 1);
             }
         }
 		else if(c.duelStatus <= 4) { // if we are not in a duel repawn to wildy
 			movePlayer(Config.RESPAWN_X, Config.RESPAWN_Y, 0);
 			c.isSkulled = false;
 			c.skullTimer = 0;
 			c.attackedPlayers.clear();
 		} else if (c.inFightCaves()) {
 			c.getPA().resetTzhaar();
 		} else { // we are in a duel, respawn outside of arena
 			Client o = (Client) Server.playerHandler.players[c.duelingWith];
 			if(o != null) {
 				o.getPA().createPlayerHints(10, -1);
 				if(o.duelStatus == 6) {
 					o.getTradeAndDuel().duelVictory();
 				}
 			}
 			movePlayer(Config.DUELING_RESPAWN_X+(Misc.random(Config.RANDOM_DUELING_RESPAWN)), Config.DUELING_RESPAWN_Y+(Misc.random(Config.RANDOM_DUELING_RESPAWN)), 0);
 			if(c.duelStatus != 6) { // if we have won but have died, don't reset the duel status.
 				c.getTradeAndDuel().resetDuel();
 			}
 		}
 		//PlayerSaving.getSingleton().requestSave(c.playerId);
 		PlayerSave.saveGame(c);
 		c.getCombat().resetPlayerAttack();
 		resetAnimation();
 		c.startAnimation(-1);
 		frame1();
 		resetTb();
 		c.isSkulled = false;
 		c.attackedPlayers.clear();
 		c.headIconPk = -1;
 		c.skullTimer = -1;
 		c.damageTaken = new int[Config.MAX_PLAYERS];
 		c.getPA().requestUpdates();
 		removeAllWindows();
 		c.tradeResetNeeded = true;	
 
 
 	}
 		
 	/**
 	* Location change for digging, levers etc
 	**/
 	
 	public void changeLocation() {
 		switch(c.newLocation) {
 			case 1:
 			sendFrame99(2);
 			movePlayer(3578,9706,-1);
 			break;
 			case 2:
 			sendFrame99(2);
 			movePlayer(3568,9683,-1);
 			break;
 			case 3:
 			sendFrame99(2);
 			movePlayer(3557,9703,-1);
 			break;
 			case 4:
 			sendFrame99(2);
 			movePlayer(3556,9718,-1);
 			break;
 			case 5:
 			sendFrame99(2);
 			movePlayer(3534,9704,-1);
 			break;
 			case 6:
 			sendFrame99(2);
 			movePlayer(3546,9684,-1);
 			break;
 			case 7:
 			sendFrame99(2);
 			movePlayer(3546,9684,-1);
 			break;
 		}
 		c.newLocation = 0;
 	}
 	
 	
 	/**
 	* Teleporting
 	**/
 	public void spellTeleport(int x, int y, int height) {
 		c.getPA().startTeleport(x, y, height, c.playerMagicBook == 1 ? "ancient" : "modern");
 	}
 	public void startMovement(int x, int y, int height) {
 		if(c.duelStatus == 5) {
 			c.sendMessage("You can't teleport during a duel!");
 			return;
 		}
 		if (CastleWars.isInCw(c)) {
             c.sendMessage("You cannot tele from a Castle Wars Game!");
             return;
         }
 		if(c.inRFD()) {
 			c.sendMessage("You can't teleport out of this minigame!");
 			return;
 		}
 		if(c.Jail == true) {
 			c.sendMessage("You can't teleport out of prison idiot!");
 			return;
 		}
 		if(c.inJail() && c.Jail == true) {
 			c.sendMessage("You can't teleport out of prison idiot!");
 			return;
 		}
 		if(c.inWarriorG() && c.heightLevel == 2) {
 			c.sendMessage("You can't teleport out of Warrior Guild!");
 			return;
 		}
 		if(c.inGWD()) {
 		ResetGWKC();
 		}
 		if(c.inFightCaves()) {
 			c.sendMessage("You can't teleport out of this minigame!");
 			return;
 		}
 		if(c.inWild() && c.wildLevel > Config.NO_TELEPORT_WILD_LEVEL) {
 			c.sendMessage("You can't teleport above level "+Config.NO_TELEPORT_WILD_LEVEL+" in the wilderness.");
 			return;
 		}
 		if(System.currentTimeMillis() - c.teleBlockDelay < c.teleBlockLength) {
 			c.sendMessage("You are teleblocked and can't teleport.");
 			return;
 		}
 		if(!c.isDead && c.teleTimer == 0 && c.respawnTimer == -6) {
 			if (c.playerIndex > 0 || c.npcIndex > 0)
 				c.getCombat().resetPlayerAttack();
 			c.stopMovement();
 			EarningPotential.checkTeleport(c);
 			removeAllWindows();			
 			c.teleX = x;
 			c.teleY = y;
 			c.npcIndex = 0;
 			c.playerIndex = 0;
 			c.faceUpdate(0);
 			c.teleHeight = height;
 
 			}
 		
 		
 	}	
 	public void startTeleport(int x, int y, int height, String teleportType) {
 		if(c.duelStatus == 5) {
 			c.sendMessage("You can't teleport during a duel!");
 			return;
 		}
 		if(c.inPits || c.viewingOrb || inPitsWait()) {
 			c.sendMessage("You can't teleport in here!");
 			return;
 		}
 		if(c.inGWD()) {
 		ResetGWKC();
 		}
 		if(c.inJail() && c.Jail == true) {
 			c.sendMessage("You can't teleport out of prison idiot!");
 			return;
 		}
 		if(c.Jail == true) {
 			c.sendMessage("You can't teleport out of prison idiot!");
 			return;
 		}
 		if(c.inWarriorG() && c.heightLevel == 2) {
 			c.sendMessage("You can't teleport out of Warrior Guild!");
 			return;
 		}
 		if(c.inRFD()) {
 			c.sendMessage("You can't teleport out of this minigame!");
 			return;
 		}
 		if(c.inFightCaves()) {
 			c.sendMessage("You can't teleport out of this minigame!");
 			return;
 		}
 		if(c.inWild() && c.wildLevel > Config.NO_TELEPORT_WILD_LEVEL) {
 			c.sendMessage("You can't teleport above level "+Config.NO_TELEPORT_WILD_LEVEL+" in the wilderness.");
 			return;
 		}
 		if(System.currentTimeMillis() - c.teleBlockDelay < c.teleBlockLength) {
 			c.sendMessage("You are teleblocked and can't teleport.");
 			return;
 		}
 		if(!c.isDead && c.teleTimer == 0 && c.respawnTimer == -6) {
 			if (c.playerIndex > 0 || c.npcIndex > 0)
 				c.getCombat().resetPlayerAttack();
 			c.stopMovement();
 			EarningPotential.checkTeleport(c);
 			removeAllWindows();			
 			c.teleX = x;
 			c.teleY = y;
 			c.npcIndex = 0;
 			c.playerIndex = 0;
 			c.faceUpdate(0);
 			c.teleHeight = height;
  if(teleportType.equalsIgnoreCase("modern")) {
 				c.startAnimation(8939);
 				c.teleTimer = 9;
 				c.gfx0(1576);
 				c.teleEndAnimation = 8941;
 			} 
 
 			if(teleportType.equalsIgnoreCase("ancient")) {
 				c.startAnimation(1979);
 				c.teleGfx = 0;
 				c.teleTimer = 11;
				c.teleEndAnimation = 8941;
 				c.gfx0(1681);
 			}
 			if(teleportType.equalsIgnoreCase("lunar")) {
 				c.startAnimation(9606);
 				c.teleTimer = 12;
 				c.gfx0(1685);
 				c.teleEndAnimation = 8941;
 			} 
 			
 		}
 	}
 	public void startTeleport2(int x, int y, int height) {
 		if(c.duelStatus == 5) {
 			c.sendMessage("You can't teleport during a duel!");
 			return;
 		}
 		if(c.Jail == true) {
 			c.sendMessage("You can't teleport out of prison idiot!");
 			return;
 		}
 		if(c.inGWD()) {
 		ResetGWKC();
 		}
 		if(c.inJail() && c.Jail == true) {
 			c.sendMessage("You can't teleport out of prison idiot!");
 			return;
 		}
 		if(System.currentTimeMillis() - c.teleBlockDelay < c.teleBlockLength) {
 			c.sendMessage("You are teleblocked and can't teleport.");
 			return;
 		}
 		if(!c.isDead && c.teleTimer == 0) {			
 			c.stopMovement();
 			removeAllWindows();			
 			c.teleX = x;
 			c.teleY = y;
 			c.npcIndex = 0;
 			c.playerIndex = 0;
 			c.faceUpdate(0);
 			c.teleHeight = height;
 			c.startAnimation(714);
 			c.teleTimer = 11;
 			c.teleGfx = 308;
 			c.teleEndAnimation = 715;
 			
 		}
 	} 
 
 	public void processTeleport() {
 		c.teleportToX = c.teleX;
 		c.teleportToY = c.teleY;
 		c.heightLevel = c.teleHeight;
 		if(c.teleEndAnimation > 0) {
 			c.startAnimation(c.teleEndAnimation);
 		}
 	}
 		
 	public void movePlayer(int x, int y, int h) {
 		c.resetWalkingQueue();
 		c.teleportToX = x;
 		c.teleportToY = y;
 		c.heightLevel = h;
 		requestUpdates();
 	}
 	
 	/**
 	* Following
 	**/
 	
 	/*public void Player() {
 		if(Server.playerHandler.players[c.followId] == null || Server.playerHandler.players[c.followId].isDead) {
 			c.getPA().resetFollow();
 			return;
 		}		
 		if(c.freezeTimer > 0) {
 			return;
 		}
 		int otherX = Server.playerHandler.players[c.followId].getX();
 		int otherY = Server.playerHandler.players[c.followId].getY();
 		boolean withinDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 2);
 		boolean hallyDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 2);
 		boolean bowDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 6);
 		boolean rangeWeaponDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 2);
 		boolean sameSpot = (c.absX == otherX && c.absY == otherY);
 		if(!c.goodDistance(otherX, otherY, c.getX(), c.getY(), 25)) {
 			c.followId = 0;
 			c.getPA().resetFollow();
 			return;
 		}
 		c.faceUpdate(c.followId+32768);
 		if ((c.usingBow || c.mageFollow || c.autocastId > 0 && (c.npcIndex > 0 || c.playerIndex > 0)) && bowDistance && !sameSpot) {
 			c.stopMovement();
 			return;
 		}	
 		if (c.usingRangeWeapon && rangeWeaponDistance && !sameSpot && (c.npcIndex > 0 || c.playerIndex > 0)) {
 			c.stopMovement();
 			return;
 		}	
 		if(c.goodDistance(otherX, otherY, c.getX(), c.getY(), 1) && !sameSpot) {
 			return;
 		}
 		c.outStream.createFrame(174);
 		boolean followPlayer = c.followId > 0;
 		if (c.freezeTimer <= 0)
 			if (followPlayer)
 				c.outStream.writeWord(c.followId);
 			else 
 				c.outStream.writeWord(c.followId2);
 		else
 			c.outStream.writeWord(0);
 		
 		if (followPlayer)
 			c.outStream.writeByte(1);
 		else
 			c.outStream.writeByte(0);
 		if (c.usingBow && c.playerIndex > 0)
 			c.followDistance = 5;
 		else if (c.usingRangeWeapon && c.playerIndex > 0)
 			c.followDistance = 3;
 		else if (c.spellId > 0 && c.playerIndex > 0)
 			c.followDistance = 5;
 		else
 			c.followDistance = 1;
 		c.outStream.writeWord(c.followDistance);
 	}*/
 	
 	public void followPlayer(int i) {
 		if(Server.playerHandler.players[c.followId] == null || Server.playerHandler.players[c.followId].isDead) {
 			c.followId = 0;
 			return;
 		}		
 		if(c.freezeTimer > 0) {
 			return;
 		}
 		if(c.inWG()) {
 			c.followId = 0;
 			return;
 		}
 		if(c.inDuelArena()) {
 			c.followId = 0;
 			return;
 		}
 		if(inPitsWait()) {
 			c.followId = 0;
 		}
 		if(c.inJail() && c.Jail == true) {
 			c.sendMessage("You cannot follow in jail!");
 			c.followId = 0;
 			return;
 		}
 		if(c.Jail == true) {
 			c.sendMessage("You cannot follow in jail!");
 			c.followId = 0;
 			return;
 		}
 		if (c.isDead || c.playerLevel[3] <= 0)
 			return;
 		
 		int otherX = Server.playerHandler.players[c.followId].getX();
 		int otherY = Server.playerHandler.players[c.followId].getY();
 		boolean withinDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 2);
 		boolean goodDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 1);
 		boolean hallyDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 2);
 		boolean bowDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 8);
 		boolean rangeWeaponDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 4);
 		boolean sameSpot = c.absX == otherX && c.absY == otherY;
 		if(!c.goodDistance(otherX, otherY, c.getX(), c.getY(), 25)) {
 			c.followId = 0;
 			return;
 		}
 		if(c.goodDistance(otherX, otherY, c.getX(), c.getY(), 1)) {
 			if (otherX != c.getX() && otherY != c.getY()) {
 				stopDiagonal(otherX, otherY);
 				return;
 			}
 		}
 		
 		if((c.usingBow || c.mageFollow || (c.playerIndex > 0 && c.autocastId > 0)) && bowDistance && !sameSpot) {
 			return;
 		}
 
 		if(c.getCombat().usingHally() && hallyDistance && !sameSpot) {
 			return;
 		}
 
 		if(c.usingRangeWeapon && rangeWeaponDistance && !sameSpot) {
 			return;
 		}
 		
 		c.faceUpdate(c.followId+32768);
 		
 		if (otherY == c.getY() && otherX == c.getX()) {			
 			switch(Misc.random(3))  {
 				case 0:
 				walkTo(0, getMove(c.getX(), otherX - 1));
 				break;
 				
 				case 1:
 				walkTo(0, getMove(c.getX(), otherX + 1));
 				break;
 				
 				case 2:
 				walkTo(0, getMove(c.getY(), otherY + 1));
 				break;
 				
 				case 3:
 				walkTo(0, getMove(c.getY(), otherY - 1));
 				break;
 			}
 		}		
 		if(c.isRunning2 && !withinDistance) {
 			if(otherY > c.getY() && otherX == c.getX()) {
 				walkTo(0, getMove(c.getY(), otherY - 1) + getMove(c.getY(), otherY - 1));
 			} else if(otherY < c.getY() && otherX == c.getX()) {
 				walkTo(0, getMove(c.getY(), otherY + 1) + getMove(c.getY(), otherY + 1));
 			} else if(otherX > c.getX() && otherY == c.getY()) {
 				walkTo(getMove(c.getX(), otherX - 1) + getMove(c.getX(), otherX - 1), 0);
 			} else if(otherX < c.getX() && otherY == c.getY()) {
 				walkTo(getMove(c.getX(), otherX + 1) + getMove(c.getX(), otherX + 1), 0);
 			} else if(otherX < c.getX() && otherY < c.getY()) {
 				walkTo(getMove(c.getX(), otherX + 1) + getMove(c.getX(), otherX + 1), getMove(c.getY(), otherY + 1) + getMove(c.getY(), otherY + 1));
 			} else if(otherX > c.getX() && otherY > c.getY()) {
 				walkTo(getMove(c.getX(), otherX - 1) + getMove(c.getX(), otherX - 1), getMove(c.getY(), otherY - 1) + getMove(c.getY(), otherY - 1));
 			} else if(otherX < c.getX() && otherY > c.getY()) {
 				walkTo(getMove(c.getX(), otherX + 1) + getMove(c.getX(), otherX + 1), getMove(c.getY(), otherY - 1) + getMove(c.getY(), otherY - 1));
 			} else if(otherX > c.getX() && otherY < c.getY()) {
 				walkTo(getMove(c.getX(), otherX + 1) + getMove(c.getX(), otherX + 1), getMove(c.getY(), otherY - 1) + getMove(c.getY(), otherY - 1));
 			} 		
 		} else {
 			if(otherY > c.getY() && otherX == c.getX()) {
 				walkTo(0, getMove(c.getY(), otherY - 1));
 			} else if(otherY < c.getY() && otherX == c.getX()) {
 				walkTo(0, getMove(c.getY(), otherY + 1));
 			} else if(otherX > c.getX() && otherY == c.getY()) {
 				walkTo(getMove(c.getX(), otherX - 1), 0);
 			} else if(otherX < c.getX() && otherY == c.getY()) {
 				walkTo(getMove(c.getX(), otherX + 1), 0);
 			} else if(otherX < c.getX() && otherY < c.getY()) {
 				walkTo(getMove(c.getX(), otherX + 1), getMove(c.getY(), otherY + 1));
 			} else if(otherX > c.getX() && otherY > c.getY()) {
 				walkTo(getMove(c.getX(), otherX - 1), getMove(c.getY(), otherY - 1));
 			} else if(otherX < c.getX() && otherY > c.getY()) {
 				walkTo(getMove(c.getX(), otherX + 1), getMove(c.getY(), otherY - 1));
 			} else if(otherX > c.getX() && otherY < c.getY()) {
 				walkTo(getMove(c.getX(), otherX - 1), getMove(c.getY(), otherY + 1));
 			} 
 		}
 	}
 	
 	public void followNpc() {
 		if(Server.npcHandler.npcs[c.followId2] == null || Server.npcHandler.npcs[c.followId2].isDead) {
 			c.followId2 = 0;
 			return;
 		}		
 		if(c.freezeTimer > 0) {
 			return;
 		}
 		if(c.inWG()) {
 			c.followId = 0;
 			return;
 		}
 		if(c.inDuelArena()) {
 			c.followId = 0;
 			return;
 		}
 		if(c.inJail() && c.Jail == true) {
 			c.sendMessage("You cannot follow in jail!");
 			c.followId = 0;
 			return;
 		}
 		if(c.Jail == true) {
 			c.sendMessage("You cannot follow in jail!");
 			c.followId = 0;
 			return;
 		}
 		if (c.isDead || c.playerLevel[3] <= 0)
 			return;
 		
 		int otherX = Server.npcHandler.npcs[c.followId2].getX();
 		int otherY = Server.npcHandler.npcs[c.followId2].getY();
 		boolean withinDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 2);
 		boolean goodDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 1);
 		boolean hallyDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 2);
 		boolean bowDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 8);
 		boolean rangeWeaponDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 4);
 		boolean sameSpot = c.absX == otherX && c.absY == otherY;
 		if(!c.goodDistance(otherX, otherY, c.getX(), c.getY(), 25)) {
 			c.followId2 = 0;
 			return;
 		}
 		if(c.goodDistance(otherX, otherY, c.getX(), c.getY(), 1)) {
 			if (otherX != c.getX() && otherY != c.getY()) {
 				stopDiagonal(otherX, otherY);
 				return;
 			}
 		}
 		
 		if((c.usingBow || c.mageFollow || (c.npcIndex > 0 && c.autocastId > 0)) && bowDistance && !sameSpot) {
 			return;
 		}
 
 		if(c.getCombat().usingHally() && hallyDistance && !sameSpot) {
 			return;
 		}
 
 		if(c.usingRangeWeapon && rangeWeaponDistance && !sameSpot) {
 			return;
 		}
 		
 		c.faceUpdate(c.followId2);
 		if (otherX == c.absX && otherY == c.absY) {
 			int r = Misc.random(3);
 			switch (r) {
 				case 0:
 					walkTo(0,-1);
 				break;
 				case 1:
 					walkTo(0,1);
 				break;
 				case 2:
 					walkTo(1,0);
 				break;
 				case 3:
 					walkTo(-1,0);
 				break;			
 			}		
 		} else if(c.isRunning2 && !withinDistance) {
 			if(otherY > c.getY() && otherX == c.getX()) {
 				walkTo(0, getMove(c.getY(), otherY - 1) + getMove(c.getY(), otherY - 1));
 			} else if(otherY < c.getY() && otherX == c.getX()) {
 				walkTo(0, getMove(c.getY(), otherY + 1) + getMove(c.getY(), otherY + 1));
 			} else if(otherX > c.getX() && otherY == c.getY()) {
 				walkTo(getMove(c.getX(), otherX - 1) + getMove(c.getX(), otherX - 1), 0);
 			} else if(otherX < c.getX() && otherY == c.getY()) {
 				walkTo(getMove(c.getX(), otherX + 1) + getMove(c.getX(), otherX + 1), 0);
 			} else if(otherX < c.getX() && otherY < c.getY()) {
 				walkTo(getMove(c.getX(), otherX + 1) + getMove(c.getX(), otherX + 1), getMove(c.getY(), otherY + 1) + getMove(c.getY(), otherY + 1));
 			} else if(otherX > c.getX() && otherY > c.getY()) {
 				walkTo(getMove(c.getX(), otherX - 1) + getMove(c.getX(), otherX - 1), getMove(c.getY(), otherY - 1) + getMove(c.getY(), otherY - 1));
 			} else if(otherX < c.getX() && otherY > c.getY()) {
 				walkTo(getMove(c.getX(), otherX + 1) + getMove(c.getX(), otherX + 1), getMove(c.getY(), otherY - 1) + getMove(c.getY(), otherY - 1));
 			} else if(otherX > c.getX() && otherY < c.getY()) {
 				walkTo(getMove(c.getX(), otherX + 1) + getMove(c.getX(), otherX + 1), getMove(c.getY(), otherY - 1) + getMove(c.getY(), otherY - 1));
 			} 
 		} else {
 			if(otherY > c.getY() && otherX == c.getX()) {
 				walkTo(0, getMove(c.getY(), otherY - 1));
 			} else if(otherY < c.getY() && otherX == c.getX()) {
 				walkTo(0, getMove(c.getY(), otherY + 1));
 			} else if(otherX > c.getX() && otherY == c.getY()) {
 				walkTo(getMove(c.getX(), otherX - 1), 0);
 			} else if(otherX < c.getX() && otherY == c.getY()) {
 				walkTo(getMove(c.getX(), otherX + 1), 0);
 			} else if(otherX < c.getX() && otherY < c.getY()) {
 				walkTo(getMove(c.getX(), otherX + 1), getMove(c.getY(), otherY + 1));
 			} else if(otherX > c.getX() && otherY > c.getY()) {
 				walkTo(getMove(c.getX(), otherX - 1), getMove(c.getY(), otherY - 1));
 			} else if(otherX < c.getX() && otherY > c.getY()) {
 				walkTo(getMove(c.getX(), otherX + 1), getMove(c.getY(), otherY - 1));
 			} else if(otherX > c.getX() && otherY < c.getY()) {
 				walkTo(getMove(c.getX(), otherX - 1), getMove(c.getY(), otherY + 1));
 			} 
 		}
 		c.faceUpdate(c.followId2);
 	}
 	
 
 	
 	
 	public int getRunningMove(int i, int j) {
 		if (j - i > 2)
 			return 2;
 		else if (j - i < -2)
 			return -2;
 		else return j-i;
 	}
 	
 	public void resetFollow() {
 		c.followId = 0;
 		c.followId2 = 0;
 		c.mageFollow = false;
 		c.outStream.createFrame(174);
 		c.outStream.writeWord(0);
 		c.outStream.writeByte(0);
 		c.outStream.writeWord(1);
 	}
 	
 	public void walkTo(int i, int j) {
 		c.newWalkCmdSteps = 0;
         if(++c.newWalkCmdSteps > 50)
             c.newWalkCmdSteps = 0;
         int k = c.getX() + i;
         k -= c.mapRegionX * 8;
         c.getNewWalkCmdX()[0] = c.getNewWalkCmdY()[0] = 0;
         int l = c.getY() + j;
         l -= c.mapRegionY * 8;
 
         for(int n = 0; n < c.newWalkCmdSteps; n++) {
             c.getNewWalkCmdX()[n] += k;
             c.getNewWalkCmdY()[n] += l;
         }
     }
 	
 	public void walkTo2(int i, int j) {
 		if (c.freezeDelay > 0)
 			return;
 		c.newWalkCmdSteps = 0;
         if(++c.newWalkCmdSteps > 50)
             c.newWalkCmdSteps = 0;
 		int k = c.getX() + i;
         k -= c.mapRegionX * 8;
         c.getNewWalkCmdX()[0] = c.getNewWalkCmdY()[0] = 0;
         int l = c.getY() + j;
         l -= c.mapRegionY * 8;
 
         for(int n = 0; n < c.newWalkCmdSteps; n++) {
             c.getNewWalkCmdX()[n] += k;
             c.getNewWalkCmdY()[n] += l;
         }
     }
 	
 	public void stopDiagonal(int otherX, int otherY) {
 		if (c.freezeDelay > 0)
 			return;
 		c.newWalkCmdSteps = 1;
 		int xMove = otherX - c.getX();
 		int yMove = 0;
 		if (xMove == 0)
 			yMove = otherY - c.getY();
 		/*if (!clipHor) {
 			yMove = 0;
 		} else if (!clipVer) {
 			xMove = 0;	
 		}*/
 		
 		int k = c.getX() + xMove;
         k -= c.mapRegionX * 8;
         c.getNewWalkCmdX()[0] = c.getNewWalkCmdY()[0] = 0;
         int l = c.getY() + yMove;
         l -= c.mapRegionY * 8;
 		
 		for(int n = 0; n < c.newWalkCmdSteps; n++) {
             c.getNewWalkCmdX()[n] += k;
             c.getNewWalkCmdY()[n] += l;
         }
 		
 	}
 	
 		
 	
 	public void walkToCheck(int i, int j) {
 		if (c.freezeDelay > 0)
 			return;
 		c.newWalkCmdSteps = 0;
         if(++c.newWalkCmdSteps > 50)
             c.newWalkCmdSteps = 0;
 		int k = c.getX() + i;
         k -= c.mapRegionX * 8;
         c.getNewWalkCmdX()[0] = c.getNewWalkCmdY()[0] = 0;
         int l = c.getY() + j;
         l -= c.mapRegionY * 8;
 		
 		for(int n = 0; n < c.newWalkCmdSteps; n++) {
             c.getNewWalkCmdX()[n] += k;
             c.getNewWalkCmdY()[n] += l;
         }
 	}
 	
 
 	public int getMove(int place1,int place2) {
 		if (System.currentTimeMillis() - c.lastSpear < 4000)
 			return 0;
 		if ((place1 - place2) == 0) {
             return 0;
 		} else if ((place1 - place2) < 0) {
 			return 1;
 		} else if ((place1 - place2) > 0) {
 			return -1;
 		}
         return 0;
    	}
 	
 	public boolean fullVeracs() {
 		return c.playerEquipment[c.playerHat] == 4753 && c.playerEquipment[c.playerChest] == 4757 && c.playerEquipment[c.playerLegs] == 4759 && c.playerEquipment[c.playerWeapon] == 4755;
 	}
 	public boolean fullGuthans() {
 		return c.playerEquipment[c.playerHat] == 4724 && c.playerEquipment[c.playerChest] == 4728 && c.playerEquipment[c.playerLegs] == 4730 && c.playerEquipment[c.playerWeapon] == 4726;
 	}
 	
 	/**
 	* reseting animation
 	**/
 	public void resetAnimation() {
 		c.getCombat().getPlayerAnimIndex(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
 		c.startAnimation(c.playerStandIndex);
 		requestUpdates();
 	}
 
 	public void requestUpdates() {
 		c.updateRequired = true;
 		c.setAppearanceUpdateRequired(true);
 	}
 	
 	public void handleAlt(int id) {
 		if (!c.getItems().playerHasItem(id)) {
 			c.getItems().addItem(id,1);
 		}
 	}
 	
 	public void levelUp(int skill) {
 int totalLevel = (getLevelForXP(c.playerXP[0]) + getLevelForXP(c.playerXP[1]) + getLevelForXP(c.playerXP[2]) + getLevelForXP(c.playerXP[3]) + getLevelForXP(c.playerXP[4]) + getLevelForXP(c.playerXP[5]) + getLevelForXP(c.playerXP[6]) + getLevelForXP(c.playerXP[7]) + getLevelForXP(c.playerXP[8]) + getLevelForXP(c.playerXP[9]) + getLevelForXP(c.playerXP[10]) + getLevelForXP(c.playerXP[11]) + getLevelForXP(c.playerXP[12]) + getLevelForXP(c.playerXP[13]) + getLevelForXP(c.playerXP[14]) + getLevelForXP(c.playerXP[15]) + getLevelForXP(c.playerXP[16]) + getLevelForXP(c.playerXP[17]) + getLevelForXP(c.playerXP[18]) + getLevelForXP(c.playerXP[19]) + getLevelForXP(c.playerXP[20]) + getLevelForXP(c.playerXP[21]) + getLevelForXP(c.playerXP[22]));
 		sendFrame126("Levels: "+totalLevel, 13983);
 		switch(skill) {
 			case 0:
 			sendFrame126("Congratulations, you just advanced an attack level!", 6248);
 			sendFrame126("Your attack level is now "+getLevelForXP(c.playerXP[skill])+".", 6249);
 			c.sendMessage("Congratulations, you just advanced an attack level.");	
 			c.getPA().sendFrame126("Combat Level: "+c.getCombatLevel()+"", 3983);
 			sendFrame164(6247);
 			break;
 			
 			case 1:
             sendFrame126("Congratulations, you just advanced a defence level!", 6254);
             sendFrame126("Your defence level is now "+getLevelForXP(c.playerXP[skill])+".", 6255);
             c.sendMessage("Congratulations, you just advanced a defence level.");
 			c.getPA().sendFrame126("Combat Level: "+c.getCombatLevel()+"", 3983);
 			sendFrame164(6253);
 			break;
 			
 			case 2:
             sendFrame126("Congratulations, you just advanced a strength level!", 6207);
             sendFrame126("Your strength level is now "+getLevelForXP(c.playerXP[skill])+".", 6208);
             c.sendMessage("Congratulations, you just advanced a strength level.");
 			c.getPA().sendFrame126("Combat Level: "+c.getCombatLevel()+"", 3983);
 			sendFrame164(6206);
 			break;
 			
 			case 3:
             sendFrame126("Congratulations, you just advanced a hitpoints level!", 6217);
             sendFrame126("Your hitpoints level is now "+getLevelForXP(c.playerXP[skill])+".", 6218);
             c.sendMessage("Congratulations, you just advanced a hitpoints level.");
 			c.getPA().sendFrame126("Combat Level: "+c.getCombatLevel()+"", 3983);
 			sendFrame164(6216);
 			break;
 			
 			case 4:
             sendFrame126("Congratulations, you just advanced a ranged level!", 5453);
             sendFrame126("Your ranged level is now "+getLevelForXP(c.playerXP[skill])+".", 6114);
             c.sendMessage("Congratulations, you just advanced a ranging level.");
 			c.getPA().sendFrame126("Combat Level: "+c.getCombatLevel()+"", 3983);
 			sendFrame164(4443);
 			break;
 			
 			case 5:
             sendFrame126("Congratulations, you just advanced a prayer level!", 6243);
             sendFrame126("Your prayer level is now "+getLevelForXP(c.playerXP[skill])+".", 6244);
             c.sendMessage("Congratulations, you just advanced a prayer level.");
 			c.getPA().sendFrame126("Combat Level: "+c.getCombatLevel()+"", 3983);
 			sendFrame164(6242);
 			break;
 			
 			case 6:
             sendFrame126("Congratulations, you just advanced a magic level!", 6212);
             sendFrame126("Your magic level is now "+getLevelForXP(c.playerXP[skill])+".", 6213);
             c.sendMessage("Congratulations, you just advanced a magic level.");
 			c.getPA().sendFrame126("Combat Level: "+c.getCombatLevel()+"", 3983);
 			sendFrame164(6211);
 			break;
 			
 			case 7:
             sendFrame126("Congratulations, you just advanced a cooking level!", 6227);
             sendFrame126("Your cooking level is now "+getLevelForXP(c.playerXP[skill])+".", 6228);
             c.sendMessage("Congratulations, you just advanced a cooking level.");
 			sendFrame164(6226);
 			break;
 			
 			case 8:
 			sendFrame126("Congratulations, you just advanced a woodcutting level!", 4273);
 			sendFrame126("Your woodcutting level is now "+getLevelForXP(c.playerXP[skill])+".", 4274);
 			c.sendMessage("Congratulations, you just advanced a woodcutting level.");
 			sendFrame164(4272);
             break;
 			
             case 9:
             sendFrame126("Congratulations, you just advanced a fletching level!", 6232);
             sendFrame126("Your fletching level is now "+getLevelForXP(c.playerXP[skill])+".", 6233);
             c.sendMessage("Congratulations, you just advanced a fletching level.");
 			sendFrame164(6231);
             break;
 			
 			case 10:
             sendFrame126("Congratulations, you just advanced a fishing level!", 6259);
             sendFrame126("Your fishing level is now "+getLevelForXP(c.playerXP[skill])+".", 6260);
             c.sendMessage("Congratulations, you just advanced a fishing level.");
 			sendFrame164(6258);
 			break;
 			
 			case 11:
 			sendFrame126("Congratulations, you just advanced a fire making level!", 4283);
 			sendFrame126("Your firemaking level is now "+getLevelForXP(c.playerXP[skill])+".", 4284);
 			c.sendMessage("Congratulations, you just advanced a fire making level.");
 			sendFrame164(4282);
             break;
 			
             case 12:
 			sendFrame126("Congratulations, you just advanced a crafting level!", 6264);
 			sendFrame126("Your crafting level is now "+getLevelForXP(c.playerXP[skill])+".", 6265);
 			c.sendMessage("Congratulations, you just advanced a crafting level.");
 			sendFrame164(6263);
             break;
 			
 			case 13:
 			sendFrame126("Congratulations, you just advanced a smithing level!", 6222);
 			sendFrame126("Your smithing level is now "+getLevelForXP(c.playerXP[skill])+".", 6223);
 			c.sendMessage("Congratulations, you just advanced a smithing level.");
 			sendFrame164(6221);
 			break;
 			
 			case 14:
 			sendFrame126("Congratulations, you just advanced a mining level!", 4417);
 			sendFrame126("Your mining level is now "+getLevelForXP(c.playerXP[skill])+".", 4438);
 			c.sendMessage("Congratulations, you just advanced a mining level.");
 			sendFrame164(4416);
             break;
 			
 			case 15:
             sendFrame126("Congratulations, you just advanced a herblore level!", 6238);
             sendFrame126("Your herblore level is now "+getLevelForXP(c.playerXP[skill])+".", 6239);
             c.sendMessage("Congratulations, you just advanced a herblore level.");
 			sendFrame164(6237);
             break;
 			
 			case 16:
 			sendFrame126("Congratulations, you just advanced a agility level!", 4278);
 			sendFrame126("Your agility level is now "+getLevelForXP(c.playerXP[skill])+".", 4279);
 			c.sendMessage("Congratulations, you just advanced an agility level.");
 			sendFrame164(4277);
             break;
 			
 			case 17:
 			sendFrame126("Congratulations, you just advanced a thieving level!", 4263);
 			sendFrame126("Your theiving level is now "+getLevelForXP(c.playerXP[skill])+".", 4264);
             c.sendMessage("Congratulations, you just advanced a thieving level.");
 			sendFrame164(4261);
 			break;
 			
 			case 18:
 			sendFrame126("Congratulations, you just advanced a slayer level!", 12123);
 			sendFrame126("Your slayer level is now "+getLevelForXP(c.playerXP[skill])+".", 12124);
 			c.sendMessage("Congratulations, you just advanced a slayer level.");
 			sendFrame164(12122);
             break;
             
             case 20:
 			sendFrame126("Congratulations, you just advanced a runecrafting level!", 4268);
 			sendFrame126("Your runecrafting level is now "+getLevelForXP(c.playerXP[skill])+".", 4269);
 			c.sendMessage("Congratulations, you just advanced a runecrafting level.");
 			sendFrame164(4267);
             break;
 			
 			case 21:
 			c.sendMessage("Congratulations, you just advanced a hunter level!");
 			c.sendMessage("Your hunter level is now "+getLevelForXP(c.playerXP[skill])+".");
             break;
 			
 case 22:
 sendFrame126("Congratulations, you just advanced a Summoning level!", 4268);
 			sendFrame126("Your Summoning level is now "+getLevelForXP(c.playerXP[skill])+".", 4269);
 			c.sendMessage("Congratulations, you just advanced a Summoning level.");
 			sendFrame164(4267);
 break;
 		}
 		c.dialogueAction = 0;
 		c.nextChat = 0;
 	}
 	
 	public void refreshSkill(int i) {
 		switch (i) {
 			case 0:
 			sendFrame126("" + c.playerLevel[0] + "", 4004);
 			sendFrame126("" + getLevelForXP(c.playerXP[0]) + "", 4005);
 			sendFrame126("" + c.playerXP[0] + "", 4044);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[0]) + 1) + "", 4045);
 			break;
 			
 			case 1:
 			sendFrame126("" + c.playerLevel[1] + "", 4008);
 			sendFrame126("" + getLevelForXP(c.playerXP[1]) + "", 4009);
 			sendFrame126("" + c.playerXP[1] + "", 4056);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[1]) + 1) + "", 4057);
 			break;
 			
 			case 2:
 			sendFrame126("" + c.playerLevel[2] + "", 4006);
 			sendFrame126("" + getLevelForXP(c.playerXP[2]) + "", 4007);
 			sendFrame126("" + c.playerXP[2] + "", 4050);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[2]) + 1) + "", 4051);
 			break;
 			
 			case 3:
 			sendFrame126("" + c.playerLevel[3] + "", 4016);
 			sendFrame126("" + getLevelForXP(c.playerXP[3]) + "", 4017);
 			sendFrame126("" + c.playerXP[3] + "", 4080);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[3])+1) + "", 4081);
 			break;
 			
 			case 4:
 			sendFrame126("" + c.playerLevel[4] + "", 4010);
 			sendFrame126("" + getLevelForXP(c.playerXP[4]) + "", 4011);
 			sendFrame126("" + c.playerXP[4] + "", 4062);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[4]) + 1) + "", 4063);
 			break;
 			
 			case 5:
 			sendFrame126("" + c.playerLevel[5] + "", 4012);
 			sendFrame126("" + getLevelForXP(c.playerXP[5]) + "", 4013);
 			sendFrame126("" + c.playerXP[5] + "", 4068);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[5]) + 1) + "", 4069);
 			sendFrame126("" +c.playerLevel[5]+"/"+getLevelForXP(c.playerXP[5])+"", 687);//Prayer frame
 			break;
 			
 			case 6:
 			sendFrame126("" + c.playerLevel[6] + "", 4014);
 			sendFrame126("" + getLevelForXP(c.playerXP[6]) + "", 4015);
 			sendFrame126("" + c.playerXP[6] + "", 4074);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[6]) + 1) + "", 4075);
 			break;
 			
 			case 7:
 			sendFrame126("" + c.playerLevel[7] + "", 4034);
 			sendFrame126("" + getLevelForXP(c.playerXP[7]) + "", 4035);
 			sendFrame126("" + c.playerXP[7] + "", 4134);
 			sendFrame126("" +getXPForLevel(getLevelForXP(c.playerXP[7]) + 1) + "", 4135);
 			break;
 			
 			case 8:
 			sendFrame126("" + c.playerLevel[8] + "", 4038);
 			sendFrame126("" + getLevelForXP(c.playerXP[8]) + "", 4039);
 			sendFrame126("" + c.playerXP[8] + "", 4146);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[8]) + 1) + "", 4147);
 			break;
 			
 			case 9:
 			sendFrame126("" + c.playerLevel[9] + "", 4026);
 			sendFrame126("" + getLevelForXP(c.playerXP[9]) + "", 4027);
 			sendFrame126("" + c.playerXP[9] + "", 4110);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[9]) + 1) + "", 4111);
 			break;
 			
 			case 10:
 			sendFrame126("" + c.playerLevel[10] + "", 4032);
 			sendFrame126("" + getLevelForXP(c.playerXP[10]) + "", 4033);
 			sendFrame126("" + c.playerXP[10] + "", 4128);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[10]) + 1) + "", 4129);
 			break;
 			
 			case 11:
 			sendFrame126("" + c.playerLevel[11] + "", 4036);
 			sendFrame126("" + getLevelForXP(c.playerXP[11]) + "", 4037);
 			sendFrame126("" + c.playerXP[11] + "", 4140);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[11]) + 1) + "", 4141);
 			break;
 			
 			case 12:
 			sendFrame126("" + c.playerLevel[12] + "", 4024);
 			sendFrame126("" + getLevelForXP(c.playerXP[12]) + "", 4025);
 			sendFrame126("" + c.playerXP[12] + "", 4104);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[12]) + 1) + "", 4105);
 			break;
 			
 			case 13:
 			sendFrame126("" + c.playerLevel[13] + "", 4030);
 			sendFrame126("" + getLevelForXP(c.playerXP[13]) + "", 4031);
 			sendFrame126("" + c.playerXP[13] + "", 4122);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[13]) + 1) + "", 4123);
 			break;
 			
 			case 14:
 			sendFrame126("" + c.playerLevel[14] + "", 4028);
 			sendFrame126("" + getLevelForXP(c.playerXP[14]) + "", 4029);
 			sendFrame126("" + c.playerXP[14] + "", 4116);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[14]) + 1)+ "", 4117);
 			break;
 			
 			case 15:
 			sendFrame126("" + c.playerLevel[15] + "", 4020);
 			sendFrame126("" + getLevelForXP(c.playerXP[15]) + "", 4021);
 			sendFrame126("" + c.playerXP[15] + "", 4092);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[15]) + 1) + "", 4093);
 			break;
 			
 			case 16:
 			sendFrame126("" + c.playerLevel[16] + "", 4018);
 			sendFrame126("" + getLevelForXP(c.playerXP[16]) + "", 4019);
 			sendFrame126("" + c.playerXP[16] + "", 4086);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[16]) + 1) + "", 4087);
 			break;
 			
 			case 17:
 			sendFrame126("" + c.playerLevel[17] + "", 4022);
 			sendFrame126("" + getLevelForXP(c.playerXP[17]) + "", 4023);
 			sendFrame126("" + c.playerXP[17] + "", 4098);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[17]) + 1) + "", 4099);
 			break;
 			
 			case 18:
 			sendFrame126("" + c.playerLevel[18] + "", 12166);
 			sendFrame126("" + getLevelForXP(c.playerXP[18]) + "", 12167);
 			sendFrame126("" + c.playerXP[18] + "", 12171);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[18]) + 1) + "", 12172);
 			break;
 			
 			case 19:
 			sendFrame126("" + c.playerLevel[19] + "", 13926);
 			sendFrame126("" + getLevelForXP(c.playerXP[19]) + "", 13927);
 			sendFrame126("" + c.playerXP[19] + "", 13921);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[19]) + 1) + "", 13922);
 			break;
 			
 			case 20:
 			sendFrame126("" + c.playerLevel[20] + "", 4152);
 			sendFrame126("" + getLevelForXP(c.playerXP[20]) + "", 4153);
 			sendFrame126("" + c.playerXP[20] + "", 4157);
 			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[20]) + 1) + "", 4158);
 			break;
 			
 	case 21:
 	sendFrame126("" + c.playerLevel[21] + "", 29800); //hunter
 	break;
 	
 	case 22:
 	sendFrame126("" + c.playerLevel[22] + "", 29801); //summoning
 	break;
 		}
 	}
 	
 	public int getXPForLevel(int level) {
 		int points = 0;
 		int output = 0;
 
 		for (int lvl = 1; lvl <= level; lvl++) {
 			points += Math.floor((double)lvl + 300.0 * Math.pow(2.0, (double)lvl / 7.0));
 			if (lvl >= level)
 				return output;
 			output = (int)Math.floor(points / 4);
 		}
 		return 0;
 	}
 
 	public int getLevelForXP(int exp) {
 		int points = 0;
 		int output = 0;
 		if (exp > 13034430)
 			return 99;
 		for (int lvl = 1; lvl <= 99; lvl++) {
 			points += Math.floor((double) lvl + 300.0
 					* Math.pow(2.0, (double) lvl / 7.0));
 			output = (int) Math.floor(points / 4);
 			if (output >= exp) {
 				return lvl;
 			}
 		}
 		return 0;
 	}
 	
 	public boolean addSkillXP(int amount, int skill){
 if (c.xpLock == true || c.Jail == true) {
     return false;
 }
 		if (amount+c.playerXP[skill] < 0 || c.playerXP[skill] > 200000000) {
 			if(c.playerXP[skill] > 200000000) {
 				c.playerXP[skill] = 200000000;
 			}
 			return false;
 		}
 		amount *= Config.SERVER_EXP_BONUS;
 		int oldLevel = getLevelForXP(c.playerXP[skill]);
 		c.playerXP[skill] += amount;
 		if (oldLevel < getLevelForXP(c.playerXP[skill])) {
 			if (c.playerLevel[skill] < c.getLevelForXP(c.playerXP[skill]) && skill != 3 && skill != 5)
 				c.playerLevel[skill] = c.getLevelForXP(c.playerXP[skill]);
 			levelUp(skill);
 			c.gfx100(199);
 			requestUpdates();
 		}
 		setSkillLevel(skill, c.playerLevel[skill], c.playerXP[skill]);
 		refreshSkill(skill);
 		return true;
 	}
 
 
 	public void resetBarrows() {
 		c.barrowsNpcs[0][1] = 0;
 		c.barrowsNpcs[1][1] = 0;
 		c.barrowsNpcs[2][1] = 0;
 		c.barrowsNpcs[3][1] = 0;
 		c.barrowsNpcs[4][1] = 0;
 		c.barrowsNpcs[5][1] = 0;
 		c.barrowsKillCount = 0;
 		c.randomCoffin = Misc.random(3) + 1;
 	}
 	
 	public static int Barrows[] = {4708, 4710, 4712, 4714, 4716, 4718, 4720, 4722, 4724, 4726, 4728, 4730, 4732, 4734, 4736, 4738, 4745, 4747, 4749, 4751, 4753, 4755, 4757, 4759};
 	public static int Crystal[] = {11257,11257,11257,11286,10939,10939,11686,11732,14499,11235,11128,10939,10940,10941,10862,10858,10828,10828,10825,10802,10796,10772,10770,10768,10766,10764,10762,10760,10758,10756,10754,10752,10750,10748,10742,10736,10732,10728,10727,10726,10725,10724,10722,10721,10718,10717,10716,10715,10714,10713,10712,10710,10708,10707,10705,10703,10702,10700,10696,10694,10693,10689,10688,10679,10678,10677,10676,10675,10674,10673,10672,10671,10670,10669,10668,10667,10666,10665,10664,10663,10654,10635,10633,10632,10631,10630,10629,10628,10627,10626,10625,10624,10623,10622,10621,10612,10604,10555,10552,10551,10550,10549,10548,10547,10400,10402,10404,10406,10408,10410,10412,104014,10416,10418,10420,10422,10424,10426,10428,10430,10432,10434,10436,10438,10344,10346,10077,10079,10081,10083,10085,10067,10063,10059,10055,10047,10045,10041,10039,10035,10023,9813,9814,9790,9789,9678,9676,9674,9672,9629,9097,9098,9099,9100,9084,9044,8971,8970,8969,8968,8967,8966,8965,8964,8963,8962,8961,8960,8959,8958,8957,8956,8955,8954,8953,8952,8950,8856,8650,8652,8654,8656,8658,8660,8662,8664,8666,8668,8670,8672,8674,8676,8678,8680,7809,7808,7807,7806,7804,7803,7668,6733,4151,2528,2528,2528,2528,2528,2528};
 	public static int Runes[] = {4740,558,560,565};
 	public static int Pots[] = {};
 	public static int arti[] = {14876, 14877, 14878, 14879, 14880, 14881, 14882, 14883, 14884, 14885, 14886, 14887, 14888, 14889, 14890, 14891, 14892};
 	public static int Shade[] = {546, 548};
 	
 	
 	public int randomShade() {
 		return Shade[(int) (Math.random()*Shade.length)];
 	}
 	public int randomBarrows() {
 		return Barrows[(int)(Math.random()*Barrows.length)];
 	}
 	
 	public int randomCrystal() {
 	return Crystal[(int) (Math.random()*Crystal.length)];
 	}
 
 
 	public int randomRunes() {
 		return Runes[(int) (Math.random()*Runes.length)];
 	}
 	
 	public int randomPots() {
 		return Pots[(int) (Math.random()*Pots.length)];
 	}
 	/**
 	 * Show an arrow icon on the selected player.
 	 * @Param i - Either 0 or 1; 1 is arrow, 0 is none.
 	 * @Param j - The player/Npc that the arrow will be displayed above.
 	 * @Param k - Keep this set as 0
 	 * @Param l - Keep this set as 0
 	 */
 	public void drawHeadicon(int i, int j, int k, int l) {
 		synchronized(c) {
 			c.outStream.createFrame(254);
 			c.outStream.writeByte(i);
 	
 			if (i == 1 || i == 10) {
 				c.outStream.writeWord(j);
 				c.outStream.writeWord(k);
 				c.outStream.writeByte(l);
 			} else {
 				c.outStream.writeWord(k);
 				c.outStream.writeWord(l);
 				c.outStream.writeByte(j);
 			}
 		}
 	}
 	
 	public int getNpcId(int id) {
 		for(int i = 0; i < NPCHandler.maxNPCs; i++) {
 			if(NPCHandler.npcs[i] != null) {
 				if(Server.npcHandler.npcs[i].npcId == id) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 	
 	public void removeObject(int x, int y) {
 		object(-1, x, x, 10, 10);
 	}
 	
 	private void objectToRemove(int X, int Y) {
 		object(-1, X, Y, 10, 10);
 	}
 
 	private void objectToRemove2(int X, int Y) {
 		object(-1, X, Y, -1, 0);
 	}
 	
 	public void removeObjects() {
 		objectToRemove2(2638, 4688);
 		objectToRemove(2638, 4688);
 		objectToRemove(2844, 3440);
 		objectToRemove(2846, 3437);
 		objectToRemove(2840, 3439);
 		objectToRemove(2841, 3443);
 		objectToRemove2(2635, 4693);
 		objectToRemove2(2634, 4693);
 		objectToRemove2(2794, 9327);
 		objectToRemove(3206, 3263);
 		objectToRemove(3193, 3274);
 		objectToRemove(3193, 3273);
 	}
 	
 	
 	public void handleGlory(int gloryId) {
 		c.getDH().sendOption4("Edgeville", "Al Kharid", "Karamja", "Mage Bank");
 		c.usingGlory = true;
 	}
 	
 	public void resetVariables() {
 		c.getCrafting().resetCrafting();
 		c.usingGlory = false;
 		c.smeltInterface = false;
 		c.smeltType = 0;
 		c.smeltAmount = 0;
 		c.woodcut[0] = c.woodcut[1] = c.woodcut[2] = 0;
 		c.mining[0] = c.mining[1] = c.mining[2] = 0;
 	}
 	
 	public boolean inPitsWait() {
 		return c.getX() <= 2404 && c.getX() >= 2394 && c.getY() <= 5175 && c.getY() >= 5169;
 	}
 	
 	public void castleWarsObjects() {
 		object(-1, 2373, 3119, -3, 10);
 		object(-1, 2372, 3119, -3, 10);
 	}
 	
 	
 public int antiFire() {
 		int toReturn = 0;
 		if (c.antiFirePot)
 			toReturn++;
 		if (c.playerEquipment[c.playerShield] == 1540 || c.prayerActive[12] || c.playerEquipment[c.playerShield] == 11284 || c.playerEquipment[c.playerShield] == 11283)
 			toReturn++;
 		c.getCombat().addCharge(c);
 		return toReturn;	
 	}
 	
 	public int backupInvItems[] = new int[28];
 	public int backupInvItemsN[] = new int[28];
 	
 	public void otherInv(Client c, Client o) {
 		if(o == c || o == null || c == null)
 		{
 		return;
 		}
 
 		for (int i = 0; i < o.playerItems.length; i++)
 		{
 			backupInvItems[i] = c.playerItems[i]; c.playerItemsN[i] = c.playerItemsN[i];
 			c.playerItemsN[i] = o.playerItemsN[i]; c.playerItems[i] = o.playerItems[i];
 		}
 		
 		c.getItems().updateInventory();
 		
 		for (int i = 0; i < o.playerItems.length; i++)
 		{
 		c.playerItemsN[i] = backupInvItemsN[i]; c.playerItems[i] = backupInvItems[i];
 		}
 	}
 	
 	public boolean checkForFlags() {
 		int[][] itemsToCheck = {{995,100000000},{35,5},{667,5},{2402,5},{746,5},{4151,150},{565,100000},{560,100000},{555,300000},{11235,10}};
 		for (int j = 0; j < itemsToCheck.length; j++) {
 			if (itemsToCheck[j][1] < c.getItems().getTotalCount(itemsToCheck[j][0]))
 				return true;		
 		}
 		return false;
 	}
 	
 public void addStarter() {
 		c.trade11 = 0;
 		c.getItems().addItem(995,10000000);
 		c.getItems().addItem(1725, 1);
 		c.getItems().addItem(554,100);
 		c.getItems().addItem(555,100);
 		c.getItems().addItem(556,100);
 		c.getItems().addItem(558,100);
 		c.getItems().addItem(560,100);
 		c.getItems().addItem(565,100);
 		c.getItems().addItem(1323,1);
 		c.getItems().addItem(841,1);
 		c.getItems().addItem(882,150);
 		c.getItems().addItem(392,500);
 		c.getItems().addItem(542,1);
 		c.getItems().addItem(544,1);
 		c.getItems().addItem(4588, 25);
 		c.getItems().addItem(5699, 25);
 		c.getItems().addItem(1334, 25);
 		c.getItems().addItem(6109, 1);
 		c.getItems().addItem(6108, 1);
 		c.getItems().addItem(6107, 1);
 		c.getItems().addItem(6106, 1);
 		c.getItems().addItem(6110, 1);
 		c.getItems().addItem(6111, 1);
 		c.sendMessage("Welcome to Project Benelux");
 				c.getPA().showInterface(17250);
 				c.canChangeAppearance = true;
 	}
 	
 	public int getWearingAmount() {
 		int count = 0;
 		for (int j = 0; j < c.playerEquipment.length; j++) {
 			if (c.playerEquipment[j] > 0)
 				count++;		
 		}
 		return count;
 	}
 	
 public void useOperate(int itemId) {
 		switch (itemId) {
 			case 15486:
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
 			break;
 			case 1712:
 			case 1710:
 			case 1708:
 			case 1706:
 			handleGlory(itemId);
 			break;
 			case 11283:
 			case 11284:
 				c.sendMessage("Your shield has "+c.dfsCount+" charges");
 			if (c.playerIndex > 0) {
 				c.getCombat().handleDfs();				
 			} else if (c.npcIndex > 0) {
 				c.getCombat().handleDfsNPC();
 			}
 			break;	
 		}
 	}
 	
 	public void getSpeared(int otherX, int otherY) {
 		int x = c.absX - otherX;
 		int y = c.absY - otherY;
 		if (x > 0)
 			x = 1;
 		else if (x < 0)
 			x = -1;
 		if (y > 0)
 			y = 1;
 		else if (y < 0)
 			y = -1;
 		moveCheck(x,y);
 		c.lastSpear = System.currentTimeMillis();
 	}
 	
 	public void moveCheck(int xMove, int yMove) {	
 		movePlayer(c.absX + xMove, c.absY + yMove, c.heightLevel);
 	}
 	
 	public int findKiller() {
 		int killer = c.playerId;
 		int damage = 0;
 		for (int j = 0; j < Config.MAX_PLAYERS; j++) {
 			if (PlayerHandler.players[j] == null)
 				continue;
 			if (j == c.playerId)
 				continue;
 			if (c.goodDistance(c.absX, c.absY, PlayerHandler.players[j].absX, PlayerHandler.players[j].absY, 40) 
 				|| c.goodDistance(c.absX, c.absY + 9400, PlayerHandler.players[j].absX, PlayerHandler.players[j].absY, 40)
 				|| c.goodDistance(c.absX, c.absY, PlayerHandler.players[j].absX, PlayerHandler.players[j].absY + 9400, 40))
 				if (c.damageTaken[j] > damage) {
 					damage = c.damageTaken[j];
 					killer = j;
 				}
 		}
 		return killer;
 	}
 	
 	public void resetTzhaar() {
 		c.waveId = -1;
 		c.tzhaarToKill = -1;
 		c.tzhaarKilled = -1;	
 		c.getPA().movePlayer(2438,5168,0);
 	}
 
 	public void resetRFD() {
 		c.waveId = -1;
 		c.RFDToKill = -1;
 		c.RFDKilled = -1;	
 		c.getPA().movePlayer(3091,3486,0);
 	}
 
 	public void enterRFD() {
 			if (c.Culin == true) {
 			c.sendMessage("You have already finished this minigame!");
 			return;
 			}
 			if (c.Agrith == true && c.Flambeed == false) {
 			c.waveId = 1;
 			c.getPA().movePlayer(1899,5363, c.playerId * 4+2);
 		Server.rfd.spawnNextWave(c);
 		return;
 			} 
 		if(c.Flambeed == true && c.Karamel == false) {
 			c.waveId = 2;
 			c.getPA().movePlayer(1899,5363, c.playerId * 4+2);
 		Server.rfd.spawnNextWave(c);
 		return;
 			} 
 		if(c.Karamel == true && c.Dessourt == false) {
 			c.waveId = 3;
 			c.getPA().movePlayer(1899,5363, c.playerId * 4+2);
 		Server.rfd.spawnNextWave(c);
 		return;
 			} 
 		if(c.Dessourt == true && c.Culin == false) {
 			c.waveId = 4;
 			c.getPA().movePlayer(1899,5363, c.playerId * 4+2);
 		Server.rfd.spawnNextWave(c);
 			return;
 			} 
 			if (c.Agrith == false) {
 			c.getPA().movePlayer(1899,5363, c.playerId * 4+2);
 		c.waveId = 0;
 		c.RFDToKill = -1;
 		c.RFDKilled = -1;
 		Server.rfd.spawnNextWave(c);
 	}
 	}
 	
 	public void enterCaves() {
 		c.getPA().movePlayer(2413,5117, c.playerId * 4);
 		c.waveId = 0;
 		c.tzhaarToKill = -1;
 		c.tzhaarKilled = -1;
 		Server.fightCaves.spawnNextWave(c);
 		c.jadSpawn();
 	}
 	
 	public void appendPoison(int damage) {
 		if (System.currentTimeMillis() - c.lastPoisonSip > c.poisonImmune) {
 			c.sendMessage("You have been poisoned.");
 			c.poisonDamage = damage;
 		}	
 	}
 	
 	public boolean checkForPlayer(int x, int y) {
 		for (Player p : PlayerHandler.players) {
 			if (p != null) {
 				if (p.getX() == x && p.getY() == y)
 					return true;
 			}	
 		}
 		return false;	
 	}
 	
 	public void checkPouch(int i) {
 		if (i < 0)
 			return;
 		c.sendMessage("This pouch has " + c.pouches[i] + " rune ess in it.");		
 	}
 	
 	public void fillPouch(int i) {
 		if (i < 0)
 			return;
 		int toAdd = c.POUCH_SIZE[i] - c.pouches[i];
 		if (toAdd > c.getItems().getItemAmount(1436)) {
 			toAdd = c.getItems().getItemAmount(1436);
 		}
 		if (toAdd > c.POUCH_SIZE[i] - c.pouches[i])
 			toAdd = c.POUCH_SIZE[i] - c.pouches[i];
 		if (toAdd > 0) {
 			c.getItems().deleteItem(1436, toAdd);
 			c.pouches[i] += toAdd;
 		}		
 	}
 	
 	public void emptyPouch(int i) {
 		if (i < 0)
 			return;
 		int toAdd = c.pouches[i];
 		if (toAdd > c.getItems().freeSlots()) {
 			toAdd = c.getItems().freeSlots();
 		}
 		if (toAdd > 0) {
 			c.getItems().addItem(1436, toAdd);
 			c.pouches[i] -= toAdd;
 		}		
 	}
 	
 	public void fixAllBarrows() {
 		int totalCost = 0;
 		int cashAmount = c.getItems().getItemAmount(995);
 		for (int j = 0; j < c.playerItems.length; j++) {
 			boolean breakOut = false;
 			for (int i = 0; i < c.getItems().brokenBarrows.length; i++) {
 				if (c.playerItems[j]-1 == c.getItems().brokenBarrows[i][1]) {					
 					if (totalCost + 80000 > cashAmount) {
 						breakOut = true;
 						c.sendMessage("You have run out of money.");
 						break;
 					} else {
 						totalCost += 80000;
 					}
 					c.playerItems[j] = c.getItems().brokenBarrows[i][0]+1;
 				}		
 			}
 			if (breakOut)		
 				break;
 		}
 		if (totalCost > 0)
 			c.getItems().deleteItem(995, c.getItems().getItemSlot(995), totalCost);		
 	}
 	
 	public void handleLoginText() {
 		c.getPA().sendFrame126("Monster Teleport", 13037);
 		c.getPA().sendFrame126("Minigame Teleport", 13047);
 		c.getPA().sendFrame126("Boss Teleport", 13055);
 		c.getPA().sendFrame126("Pking Teleport", 13063);
 		c.getPA().sendFrame126("Skill Teleport", 13071);
 		c.getPA().sendFrame126("Monster Teleport", 1300);
 		c.getPA().sendFrame126("Minigame Teleport", 1325);
 		c.getPA().sendFrame126("Boss Teleport", 1350);
 		c.getPA().sendFrame126("Pking Teleport", 1382);
 		c.getPA().sendFrame126("Skill Teleport", 1415);
 		c.getPA().sendFrame126("City Teleport", 1454);	
 		c.getPA().sendFrame126("Agility And Hunter", 7457);
 		c.getPA().sendFrame126("Coming Soon (3)", 13097);
 		c.getPA().sendFrame126("Agility And Hunter", 13089);
 		c.getPA().sendFrame126("City Teleport", 13081);
    	}
 	
 	public void handleWeaponStyle() {
 		if (c.fightMode == 0) {
 			c.getPA().sendFrame36(43, c.fightMode);
 		} else if (c.fightMode == 1) {
 			c.getPA().sendFrame36(43, 3);
 		} else if (c.fightMode == 2) {
 			c.getPA().sendFrame36(43, 1);
 		} else if (c.fightMode == 3) {
 			c.getPA().sendFrame36(43, 2);
 		}
 	}
 	
 	
 	
 }
