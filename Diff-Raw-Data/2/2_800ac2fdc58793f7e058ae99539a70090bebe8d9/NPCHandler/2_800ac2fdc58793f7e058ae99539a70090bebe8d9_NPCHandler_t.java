 package server.model.npcs;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import server.Config;
 import server.Server;
 import server.model.players.Client;
 import server.util.Misc;
 import server.world.map.VirtualWorld;
 import server.event.EventManager;
 import server.event.Event;
 import server.event.EventContainer;
 
 public class NPCHandler {
 	public static int maxNPCs = 10000;
 	public static int maxListedNPCs = 10000;
 	public static int random;
 	public static int maxNPCDrops = 10000;
 	public static NPC npcs[] = new NPC[maxNPCs];
 	public static NPCList NpcList[] = new NPCList[maxListedNPCs];
 	public NPCHandler() {
 		for(int i = 0; i < maxNPCs; i++) {
 			npcs[i] = null;
 		}
 		for(int i = 0; i < maxListedNPCs; i++) {
 			NpcList[i] = null;
 		}
 		loadNPCList("./Data/CFG/npc.cfg");
 		loadAutoSpawn("./Data/CFG/spawn-config.cfg");
 	}
 	
 
 	/**
 	 *	Sends Graardor's Message using forceChat.
 	 *	@param random How many messages there are he can choose from to say.
 	 */
 	public String sGraardorM() {
 		int random = Misc.random(8);
 		switch (random) {
 			case 0: return "Death to our enemies!";
 			case 1: return "Brargh!";
 			case 2: return "Break their bones!";
 			case 3: return "For the glory of the Big High War God!";
 			case 4: return "Split their skulls!";
 			case 5: return "We feast on the bones of our enemies tonight!";
 			case 6: return "CHAAARGE!";
 			case 7: return "All glory to Bandos!";
 			default: return "GRAAAAAAAAAR!";
 
 
 
 		}
 	}
 	
 	/**
 	 *	Sends K'ril's Message using forceChat.
 	 *	@param random How many messages there are he can choose from to say.
 	 */
 	public String sKrilM() {
 		int random = Misc.random(8);
 		switch (random) {
 			case 0: return "Attack them, you dogs!";
 			case 1: return "Forward!";
 			case 2: return "Death to Saradomin's dogs!";
 			case 3: return "Kill them, you cowards!";
 			case 4: return "The Dark One will have their souls!";
 			case 5: return "Zamorak curse them!";
 			case 6: return "Rend them limb from limb!";
 			case 7: return "No retreat!";
 			default: return "Flay them all!";
 
 
 		}
 
 
 	}
 	
 	/**
 	 *	Sends Zilyana's Message using forceChat.
 	 *	@param random How many messages there are she can choose from to say.
 	 */
 	public String sZilyanaM() {
 		int random = Misc.random(9);
 		switch (random) {
 			case 0: return "Death to the enemies of the light!";
 			case 1: return "Slay the evil ones!";
 			case 2: return "Saradomin lend me strength!";
 			case 3: return "By the power of Saradomin!";
 			case 4: return "May Saradomin be my sword!";
 			case 5: return "Good will always triumph!";
 			case 6: return "Forward! Our allies are with us!";
 			case 7: return "Saradomin is with us!";
 			case 8: return "In the name of Saradomin!";
 			default: return "Atack! Find the Godsword!";
 		}
 	}
 
 	/**
 	 *	Sends Evil Tree's Message using forceChat.
 	 *	@param random How many messages there are he can choose from to say.
 	 */
 	public String sTreeM() {
 		int random = Misc.random(3);
 		switch (random) {
 			case 0: return "RAWWRRRRRRR!";
 			case 1: return "Thou Shall Not harm us Trees!";
 			case 2: return "You'll bow to my branches!";
 			default: return "GRAAAAAAAAAR!";
 		}
 	}
 	
 public void Summon(Client c, int npcType, int x, int y, int heightLevel, int WalkingType, int HP, int maxHit, boolean attackPlayer, int attack, int defence) {
 	
 		
 		// first, search for a free slot
 		int slot = -1;
 		for (int i = 1; i < maxNPCs; i++) {
 			if (npcs[i] == null) {
 				slot = i;
 				break;
 			}
 		}
 		if(slot == -1) {
 			//Misc.println("No Free Slot");
 			return;		// no free slot found
 		}
 		NPC newNPC = new NPC(slot, npcType);
 		newNPC.absX = x;
 		newNPC.absY = y;
 		newNPC.makeX = x;
 		newNPC.makeY = y;
 		newNPC.heightLevel = heightLevel;
 		newNPC.walkingType = WalkingType;
 		newNPC.HP = HP;
 		newNPC.MaxHP = HP;
 		newNPC.maxHit = maxHit;
 				newNPC.attack = attack;
 		newNPC.defence = defence;
 	
 		newNPC.spawnedBy = c.getId();
 		
 		 newNPC.followPlayer = c.getId();
 //			followPlayer(npcType, c.getId());
 			newNPC.summon = true;
 c.lastsummon = npcType;
 c.summon = true;
 newNPC.gfx0(1315);
 c.summoningnpcid = slot;
 newNPC.npcslot = slot;
 
 
 		if(attackPlayer) {
 			newNPC.underAttack = true;
 			if(c != null) {
 				if(server.model.minigames.Barrows.COFFIN_AND_BROTHERS[c.randomCoffin][1] != newNPC.npcType) {
 					if(newNPC.npcType == 2025 || newNPC.npcType == 2026 || newNPC.npcType == 2027 || newNPC.npcType == 2028 || newNPC.npcType == 2029 || newNPC.npcType == 2030) {
 						newNPC.forceChat("You dare disturb my rest!");
 					}
 				}
 				if(server.model.minigames.Barrows.COFFIN_AND_BROTHERS[c.randomCoffin][1] == newNPC.npcType) {
 					newNPC.forceChat("You dare steal from us!");
 				}
 				
 				newNPC.killerId = c.playerId;
 			}
 		}
 		npcs[slot] = newNPC;
 	}
 			
 	
 /*public void spawnNpc3(Client c, int npcType, int x, int y, int heightLevel, int WalkingType, int HP, int maxHit, int attack, int defence, boolean attackPlayer, boolean headIcon, boolean summonFollow) {
 		// first, search for a free slot
 		int slot = -1;
 		for (int i = 1; i < maxNPCs; i++) {
 			if (npcs[i] == null) {
 				slot = i;
 				break;
 			}
 		}
 		if(slot == -1) {
 			//Misc.println("No Free Slot");
 			return;		// no free slot found
 		}
 		NPC newNPC = new NPC(slot, npcType);
 		newNPC.absX = x;
 		newNPC.absY = y;
 		newNPC.makeX = x;
 		newNPC.makeY = y;
 		newNPC.heightLevel = heightLevel;
 		newNPC.walkingType = WalkingType;
 		newNPC.HP = HP;
 		newNPC.MaxHP = HP;
 		newNPC.maxHit = maxHit;
 		newNPC.attack = attack;
 		newNPC.defence = defence;
 		newNPC.spawnedBy = c.getId();
 		newNPC.underAttack = true;
 		newNPC.facePlayer(c.playerId);
 		if(headIcon) 
 			c.getPA().drawHeadicon(1, slot, 0, 0);
 		if (summonFollow) {
 			newNPC.summoner = true;
 			newNPC.summonedBy = c.playerId;
 			c.summonId = npcType;
 			c.hasNpc = true;
 		}
 		if(attackPlayer) {
 			newNPC.underAttack = true;
 			if(c != null) {			
 				newNPC.killerId = c.playerId;
 			}
 		}
 		npcs[slot] = newNPC;
 	}*/
 
 	public void appendJailKc(int i) {
 		Client c = (Client)Server.playerHandler.players[npcs[i].killedBy];
 		if(c != null) {
 			int[] Jail = {
 				132
 			};
 			for (int j : Jail) {
 				if (npcs[i].npcType == j) {
 					c.monkeyk0ed++;
 					c.sendMessage("You now have "+c.monkeyk0ed+" Monkey kills!");
 					} else {
 						c.sendMessage("Woah man slow down.. you already have 20 monkey kills..");
 					break;
 				}
 			}
 		}	
 	}
 	
 	public void multiAttackGfx(int i, int gfx) {
 		if (npcs[i].projectileId < 0)
 			return;
 		for (int j = 0; j < Server.playerHandler.players.length; j++) {
 			if (Server.playerHandler.players[j] != null) {
 				Client c = (Client)Server.playerHandler.players[j];
 				if (c.heightLevel != npcs[i].heightLevel)
 					continue;
 				if (Server.playerHandler.players[j].goodDistance(c.absX, c.absY, npcs[i].absX, npcs[i].absY, 15)) {
 					int nX = Server.npcHandler.npcs[i].getX() + offset(i);
 					int nY = Server.npcHandler.npcs[i].getY() + offset(i);
 					int pX = c.getX();
 					int pY = c.getY();
 					int offX = (nY - pY)* -1;
 					int offY = (nX - pX)* -1;
 					c.getPA().createPlayersProjectile(nX, nY, offX, offY, 50, getProjectileSpeed(i), npcs[i].projectileId, 43, 31, -c.getId() - 1, 65);					
 				}
 			}		
 		}
 	}
 	
 	public boolean switchesAttackers(int i) {
 		switch(npcs[i].npcType) {
 			case 6261:
 			case 6263:
 			case 6265:
 			case 6223:
 			case 6225:
 			case 6227:
 			case 6248:
 			case 6250:
 			case 8133:
 			case 6252:
 			case 2892:
 			case 2894:
 			case 50:
 			case 6206:
 			case 6208:
 			case 6204:
 			return true;
 		
 		}
 	
 		return false;
 	}
 		public boolean ArmadylKC(int i) {
 		switch(npcs[i].npcType) {
 			case 6222:
 			case 6223:
 			case 6225:
 			case 6230:
 			case 6239: // Aviansie
 			case 6227:
 			case 6232:
 			case 6229:
 			case 6233:
 			case 6231:
 			return true;
 		
 		}
 	
 		return false;
 	}
 		public boolean BandosKC(int i) {
 		switch(npcs[i].npcType) {
 			case 6260:
 			case 6261:
 			case 6263:
 			case 6265:
 			case 6277:
 			case 6269:
 			case 6270:
 			case 3247:
 			case 6276:
 			case 6272:
 			case 6274:
 			case 6278:
 			return true;
 		
 		}
 	
 		return false;
 	}
 		public boolean ZammyKC(int i) {
 		switch(npcs[i].npcType) {
 			case 6203:
 			case 6204:
 			case 6206:
 			case 6208:
 			case 6219:
 			case 6218:
 			case 6212:
 			case 3248:
 			case 6220:
 			case 6221:
 			return true;
 		
 		}
 	
 		return false;
 	}
 		public boolean SaraKC(int i) {
 		switch(npcs[i].npcType) {
 			case 6247:
 			case 6248:
 			case 6250:
 			case 6254:
 			case 6252:
 			case 6257:
 			case 6255:
 			case 6256:
 			case 6258:
 			return true;
 		
 		}
 	
 		return false;
 	}
 		public int getNpcDeleteTime(int i) {
 		switch(npcs[i].npcType) {
 			case 1265:
 			case 90:
 			case 1648:
 			case 1341:
 			case 1851:
 			case 1857:
 			case 1854:
 		        return 2; 
 			case 82:
                         return 3;			
 			case 103:
 			
                         return 0;
                         case 117:
                         return 6; 			
 			default:
 			return 4;
 		}
 	}
 	
 	public void multiAttackDamage(int i) {
 		int max = getMaxHit(i);
 		for (int j = 0; j < Server.playerHandler.players.length; j++) {
 			if (Server.playerHandler.players[j] != null) {
 				Client c = (Client)Server.playerHandler.players[j];
 				if (c.isDead || c.heightLevel != npcs[i].heightLevel)
 					continue;
 				if (Server.playerHandler.players[j].goodDistance(c.absX, c.absY, npcs[i].absX, npcs[i].absY, 15)) {
 					if (npcs[i].attackType == 2) {
 						if (!c.prayerActive[16] && !c.curseActive[7]) {
 							if (Misc.random(500) + 200 > Misc.random(c.getCombat().mageDef())) {
 								int dam = Misc.random(max);
 								c.dealDamage(dam);
 								c.handleHitMask(dam);							
 							} else {
 								c.dealDamage(0);
 								c.handleHitMask(0);							
 							}
 						} else {
 							c.dealDamage(0);
 							c.handleHitMask(0);
 						}
 					} else if (npcs[i].attackType == 1) {
 						if (!c.prayerActive[17] && !c.curseActive[8]) {
 							int dam = Misc.random(max);
 							if (Misc.random(500) + 200 > Misc.random(c.getCombat().calculateRangeDefence())) {
 								c.dealDamage(dam);
 								c.handleHitMask(dam);							
 							} else {
 								c.dealDamage(0);
 								c.handleHitMask(0);
 							}
 						} else {
 							c.dealDamage(0);
 							c.handleHitMask(0);							
 						}
 					}
 					if (npcs[i].endGfx > 0) {
 						c.gfx0(npcs[i].endGfx);					
 					}
 				}
 				c.getPA().refreshSkill(3);
 			}		
 		}
 	}
 	
 	public int getClosePlayer(int i) {
 		for (int j = 0; j < Server.playerHandler.players.length; j++) {
 			if (Server.playerHandler.players[j] != null) {
 				if (j == npcs[i].spawnedBy)
 					return j;
 				if (goodDistance(Server.playerHandler.players[j].absX, Server.playerHandler.players[j].absY, npcs[i].absX, npcs[i].absY, 2 + distanceRequired(i) + followDistance(i))) {
 					if ((Server.playerHandler.players[j].underAttackBy <= 0 && Server.playerHandler.players[j].underAttackBy2 <= 0) || Server.playerHandler.players[j].inMulti())
 						if (Server.playerHandler.players[j].heightLevel == npcs[i].heightLevel)
 							return j;
 				}
 			}	
 		}
 		return 0;
 	}
 	
 	public int getCloseRandomPlayer(int i) {
 		ArrayList<Integer> players = new ArrayList<Integer>();
 		for (int j = 0; j < Server.playerHandler.players.length; j++) {
 			if (Server.playerHandler.players[j] != null) {
 				if (goodDistance(Server.playerHandler.players[j].absX, Server.playerHandler.players[j].absY, npcs[i].absX, npcs[i].absY, 2 + distanceRequired(i) + followDistance(i))) {
 					if ((Server.playerHandler.players[j].underAttackBy <= 0 && Server.playerHandler.players[j].underAttackBy2 <= 0) || Server.playerHandler.players[j].inMulti())
 						 if (npcs[i].npcType == 6260 || npcs[i].npcType == 6261 || npcs[i].npcType == 6263 || npcs[i].npcType == 6265 && Server.playerHandler.players[j].inBandos() == false)
 							continue;
 						 if (npcs[i].npcType == 6222 || npcs[i].npcType == 6223 || npcs[i].npcType == 6225|| npcs[i].npcType == 6227 && Server.playerHandler.players[j].inArma() == false)
 							continue;
 						 if (npcs[i].npcType == 6247 || npcs[i].npcType == 6248 || npcs[i].npcType == 6250 || npcs[i].npcType == 6252 && Server.playerHandler.players[j].inSara() == false)
 							continue;
 						 if (npcs[i].npcType == 6203 || npcs[i].npcType == 6204 || npcs[i].npcType == 6206 || npcs[i].npcType == 6208 && Server.playerHandler.players[j].inZammy() == false)
 							continue;
 						if (Server.playerHandler.players[j].heightLevel == npcs[i].heightLevel)
 							players.add(j);
 				}
 			}	
 		}
 		if (players.size() > 0)
 			return players.get(Misc.random(players.size() -1));
 		else
 			return 0;
 	}
 	
 	public int npcSize(int i) {
 		switch (npcs[i].npcType) {
 		case 2883:
 		case 2882:
 		case 2881:
 		case 3493:
 			return 3;
 		case 3494:
 			return 5;
 		}
 		return 0;
 	}
 	
 	public boolean isAggressive(int i) {
 		switch (npcs[i].npcType) {
 
 			case 6260:
 			case 6261:
 			case 6263:
 			case 6265:
 			case 6222:
 			case 6223:
 			case 6225:
 			case 6227:
 			case 6247:
 			case 6248:
 			case 6250:
 			case 6252:
 			case 1158:
 			case 1160:
 			case 1154:
 			case 1157:
 			case 1156:
 			case 795:
 			case 8133:
 			case 3101:
 			case 3102:
 			case 5666:
 			case 3103:
 			case 2892:
 			case 2894:
 			case 2881:
 			case 50:
 			case 2882:
 			case 2883:
 			case 6203:
 			case 6206:
 			case 6208:
 			case 6204:
 		return true;	
 		}
 		return false;
 	}	
 
 	/**
 	* Summon npc, barrows, etc
 	**/
 	public void spawnNpc(Client c, int npcType, int x, int y, int heightLevel, int WalkingType, int HP, int maxHit, int attack, int defence, boolean attackPlayer, boolean headIcon) {
 		int slot = -1;
 		for (int i = 1; i < maxNPCs; i++) {
 			if (npcs[i] == null) {
 				slot = i;
 				break;
 			}
 		}
 		if(slot == -1) {
 			return;
 		}
 		NPC newNPC = new NPC(slot, npcType);
 		newNPC.absX = x;
 		newNPC.absY = y;
 		newNPC.makeX = x;
 		newNPC.makeY = y;
 		newNPC.heightLevel = heightLevel;
 		newNPC.walkingType = WalkingType;
 		newNPC.HP = HP;
 		newNPC.MaxHP = HP;
 		newNPC.maxHit = maxHit;
 		newNPC.attack = attack;
 		newNPC.defence = defence;
 		newNPC.spawnedBy = c.getId();
 		if(headIcon) 
 			c.getPA().drawHeadicon(1, slot, 0, 0);
 		if(attackPlayer) {
 			newNPC.underAttack = true;
 			if(c != null) {
 				if(server.model.minigames.Barrows.COFFIN_AND_BROTHERS[c.randomCoffin][1] != newNPC.npcType) {
 					if(newNPC.npcType == 2025 || newNPC.npcType == 2026 || newNPC.npcType == 2027 || newNPC.npcType == 2028 || newNPC.npcType == 2029 || newNPC.npcType == 2030) {
 						newNPC.forceChat("You dare disturb my rest!");
 					}
 				}
 				if (newNPC.npcType >= 4278 && newNPC.npcType <= 4284) {
 					newNPC.forceAnim(4410);
 					newNPC.forceChat("I'M ALIVE!");
 				}
 				if(server.model.minigames.Barrows.COFFIN_AND_BROTHERS[c.randomCoffin][1] == newNPC.npcType) {
 					newNPC.forceChat("You dare steal from us!");
 				}
 				
 				newNPC.killerId = c.playerId;
 			}
 		}
 		npcs[slot] = newNPC;
 	}
 	public void getDtLastKill(int i) {
 		int dtNpcs[] = {
			1974, 1914, 1977, 1913
 		};
 		for(int dtNpc : dtNpcs) {
 			if(npcs[i].npcType == dtNpc) {
 				Client p = (Client) Server.playerHandler.players[npcs[i].killedBy];
 				if(p != null) {
 					p.lastDtKill = dtNpc;
 				}
 			}
 		}
 	}
 	
 	public void checkDt(int i) {
 		if(npcs[i] != null) {
 			Client c = (Client) Server.playerHandler.players[npcs[i].spawnedBy];
 			if(c != null) {
 				c.getDT().handleDtKills(i);
 			}
 		}
 	}
 	
 	public void spawnNpc2(int npcType, int x, int y, int heightLevel, int WalkingType, int HP, int maxHit, int attack, int defence) {
 		// first, search for a free slot
 		int slot = -1;
 		for (int i = 1; i < maxNPCs; i++) {
 			if (npcs[i] == null) {
 				slot = i;
 				break;
 			}
 		}
 		if(slot == -1) {
 			//Misc.println("No Free Slot");
 			return;		// no free slot found
 		}
 		NPC newNPC = new NPC(slot, npcType);
 		newNPC.absX = x;
 		newNPC.absY = y;
 		newNPC.makeX = x;
 		newNPC.makeY = y;
 		newNPC.heightLevel = heightLevel;
 		newNPC.walkingType = WalkingType;
 		newNPC.HP = HP;
 		newNPC.MaxHP = HP;
 		newNPC.maxHit = maxHit;
 		newNPC.attack = attack;
 		newNPC.defence = defence;
 		npcs[slot] = newNPC;
 	}
 	
 		public static void spawnNewNPC(int npcType, int x, int y, int heightLevel, int WalkingType, int HP, int maxHit, int attack, int defence, boolean needSpawn) {
 		int slot = -1;
 		for (int i = 1; i < maxNPCs; i++) {
 			if (npcs[i] == null) {
 				slot = i;
 			break;
 			}
 		}
 		if(slot == -1) {
 			return;
 		}
 		NPC newNPC = new NPC(slot, npcType);
 		newNPC.absX = x;
 		newNPC.absY = y;
 		newNPC.makeX = x;
 		newNPC.makeY = y;
 		newNPC.heightLevel = heightLevel;
 		newNPC.walkingType = WalkingType;
 		newNPC.HP = HP;
 		newNPC.MaxHP = HP;
 		newNPC.maxHit = maxHit;
 		newNPC.attack = attack;
 		newNPC.defence = defence;
 		newNPC.NeedsRespawn = needSpawn;
 		npcs[slot] = newNPC;
 		if (newNPC.npcType == 1160) {
 			newNPC.startAnim(1181);//spawn anim
 		}
 	}
 
 		public void handleKalpite(int i) {
 		spawnNewNPC(1160, npcs[i].absX, npcs[i].absY, npcs[i].heightLevel, 1, 20, 0, 0, 0, true);
 		int form1 = 0, form2 = 0;
 		for (int index = 0; index < maxNPCs; index++){
 		    if (npcs[index] != null) {
 				if (npcs[index].npcType == 1158) {
 					form1 = index;
 				break;
 				}
 		    }
 		}
 		for (int index2 = 0; index2 < maxNPCs; index2++) {
 		    if (npcs[index2] != null){
 				if (npcs[index2].npcType == 1160) {
 					form2 = index2;
 				break;
 				}
 		    }
 		}
 		npcs[form2].killedBy = npcs[form1].killedBy;
 	}
 	
 	
 	/**
 	* Emotes
 	**/
 	public int getBlockEmote(int id) {
 		switch(Server.npcHandler.npcs[id].npcType) {		
 			case 1158:
 			case 3835:
 				return 6232;
 				case 2037:
 				return 5489;
 
 case 10127:
 return 13170;
 case 10057:
 				
 						return 10818;
 
 
 					
 			case 1160:
 			case 3836:
 				return 6237;
 			case 2783:
 				return 2732;
 			case 8133: // corp beast
 				return 10058;
 			case 10141: // corp beast
 				return 13601;
 			case 8349: // torm demon
 				return 10923;
 case 9947:
 return 13771;
 				/**
 				* God Wars List
 				**/
 			//bandos
 			case 6260: //boss
 				return 7061;
 			case 6261:
 			case 6263:
 			case 6265:
 				return 6155;
 			//armadyl
 			case 6222: //boss
 				return 6974;
 			case 6223:
 			case 6225:
 			case 6227:
 				return 6952;
 			//zammy
 			case 6203:
 				return 6944;
 			case 6204:
 			case 6206:
 			case 6208:
 				return 65;
 			//Sara
 			case 6247:
 				return 6966;
 			case 6248:
 				return 6375;
 			case 6250:
 				return 7017;
 			case 6252:
 				return 4311;
 				
 			//armadyl npcs
 			//spiritual mages
 			case 6229:
 			case 6230:
 			case 6231:
 			//aviansies
 			case 6232:
 			case 6233:
 			case 6234:
 			case 6235:
 			case 6236:
 			case 6237:
 			case 6238:
 			case 6239:
 			case 6240:
 			case 6241:
 			case 6242:
 			case 6243:
 			case 6244:
 			case 6245:
 			case 6246:
 				return 6952;
 				
 			//bandos npcs
 			case 6267:
 				return 360;
 			case 6268:
 				return 2933;
 			case 6269:
 			case 6270:
 				return 4651;
 			case 6271:
 			case 6272:
 			case 6273:
 			case 6274:
 				return 4322;
 			case 6275:
 				return 165;
 			case 6276:
 			case 6277:
 			case 6278:
 				return 4322;
 			case 6279:
 			case 6280:
 				return 6183;
 			case 6281:
 				return 6136;
 			case 6282:
 				return 6189;
 			case 6283:
 				return 6183;
 				
 			//zamorak npcs
 			case 6210:
 				return 6578;
 			case 6211:
 				return 170;
 			case 6212:
 			case 6213:
 				return 6538;
 			case 6215:
 				return 1550;
 			case 6216:
 			case 6217:
 				return 1581;
 			case 6218:
 				return 4301;
 				
 			//sara npcs
 			case 6258:
 				return 2561;
 			
 			case 655:
 			return 129;
 case 10775:
 return 13154;
 			
 			default:
                 return 1834;//1834
 				
 		}
 	}
 	
 			public static int getAttackEmote(int i) {
 		switch(Server.npcHandler.npcs[i].npcType) {
 	
 case 6795:
 return 1010;
 
 
 
 case 10775:
 return 13151;
 
 case 2037:
 				return 5485;
 				
 case 6797:
 return 8104;
 	
 case 6799:
 return 8069;
 
 case 6801:
 return 7853;
 
 case 6803:
 return 8159;
 
 case 6805:
 return 7786;
 
 case 6807:
 return 8148;
 
 case 6810:
 return 7970;
 
 case 6812:
 return 7935;
 
 case 6814:
 return 7741;
 
 case 6816:
 return 8288;
 
 case 6819:
 return 7667;
 
 case 6821:
 return 7680;
 
 
 case 6823:
 return 6376;
 
 
 case 6826:
 return 5387;
 
 
 case 6828:
 return 8208;
 
 
 case 6830:
 return 8292;
 case 6832:
 return 7795;
 case 6834:
 return 8248;
 case 6836:
 return 8275;
 case 6838:
 return 6254;
 case 6856:
 return 4921;
 case 6858:
 return 5327;
 
 case 6860:
 case 6862:
 case 6864:
 return 7656;
 
 case 6868:
 return 7896;
 
 case 6870:
 return 8303;
 
 
 case 6872:
 return 7769;
 
 case 6874:
 return 5782;
 
 case 6890:
 return 7260;
 
 case 7330:
 return 8223;
 
 case 7332:
 return 8032;
 
 case 7338:
 return 5228;
 
 case 7352:
 return 8234;
 
 case 7354:
 return 7755;
 
 case 7355:
 return 7834;
 
 case 7358:
 return 7844;
 
 case 7359:
 return 8183;
 
 case 7362:
 return 8257;
 
 case 7364:
 case 7366:
 return 5228;
 
 case 7368:
 case 7369:
 
 return 8130;
 
 case 7371:
 return 8093;
 
 case 7374: 
 return 7994;
 
 case 7376:
 return 7946;
 
 
 			case 6260:
 				if (npcs[i].attackType == 0)
 					return 7060;
 				else
 					return 7063;
 			///Kq
 			case 1158:
 			case 3835:
 				if (npcs[i].attackType == 0)
 						return 6241;
 					else
 						return 6240;
 			case 1160:
 			case 3836:
 				if (npcs[i].attackType == 0)
 						return 6235;
 					else
 						return 6234;
 			
 			case 2892:
 
 			case 2894:
 			return 2868;
 			case 2627:
 			return 2621;
 			case 2630:
 			return 2625;
 			case 2631:
 			return 2633;
 			case 2741:
 			return 2637;
 			case 2746:
 			return 2637;
 			case 2607:
 			return 2611;
 			case 2743://360
 			return 2647;
 			
 			//bandos gwd
 			case 6261:
 			case 6263:
 			case 6265:
 			return 6154;
 			case 6267:
 			return 361;
 			case 6268:
 			return 2930;
 			case 6269:
 			case 6270:
 			return 4652;
 			case 6271:
 			case 6272:
 			case 6273:
 			case 6274:
 			return 4320;
 			case 6275:
 			return 164;
 			case 6276:
 			case 6277:
 			case 6278:
 			return 4320;
 			case 6279:
 			case 6280:
 			return 6184;
 			case 6281:
 			return 6134;
 			case 6282:
 			return 6188;
 			case 6283:
 			return 6184;
 			
 			//end of gwd
 			//zammy gwd
 			case 6203:
 			return 6945;
 			case 6204:
 			case 6206:
 			case 6208:
 			return 64;
 			case 6210:
 			return 6581;
 			case 6211:
 			return 169;
 			case 6212:
 			case 6213:
 			return 6536;
 			case 6215:
 			return 1552;
 			case 6216:
 			case 6217:
 			return 1582;
 			case 6218:
 			return 4300;
 			//end of zammy gwd
 			//arma gwd
 			case 6222:
 			return 6973;
 			case 6225:
 			return 6953;
 			case 6223:
 			return 6954;
 			case 6227:
 			return 6953;
 			//spiritual mages
 			case 6229:
 			case 6230:
 			case 6231:
 				return 6954;
 			//aviansies
 			case 6232:
 			case 6233:
 			case 6234:
 			case 6235:
 			case 6236:
 			case 6237:
 			case 6238:
 			case 6239:
 			case 6240:
 			case 6241:
 			case 6242:
 			case 6243:
 			case 6244:
 			case 6245:
 			case 6246:
 				return 6953;
 			//end of arma gwd
 			
 			//sara gwd
 			case 6247:
 				if (npcs[i].attackType == 2)
 						return 6967;
 					else
 						return 6964;
 
 
 			case 10057:
 				if (npcs[i].attackType == 1)
 						return 10817;
 					else
 						return 10816;
 
 					
 			case 6248:
 			return 6376;
 			case 6250:
 			return 7018;
 			case 6252:
 			return 7009;
 			//end of sara gwd
 			
 			case 13: //wizards
 			return 711;
 			
 			case 103:
 			return 123;
 			
 			case 1624:
 			return 1557;
 			
 			case 1648:
 			return 1590;
 			
 			case 2783: //dark beast
 			return 2731;
 			
 			case 1615: //abby demon
 			return 1537;
 			
 			case 1613: //nech
 			return 1528;
 			
 			case 1610: case 1611: //garg
 			return 1519;
 			
 			case 1616: //basilisk
 			return 1546;
 			
 			case 90: //skele
 			return 260;
 			
 			case 50://drags
 			case 53:
 
 			case 54:
 			case 55:
 			case 941:
 			case 1590:
 			case 1591:
 			case 1592:
 			return 80;
 			
 			case 124: //earth warrior
 			return 390;
 			
 			case 803: //monk
 			return 422;
 			
 			case 52: //baby drag
 			return 25;			
 
 			case 58: //Shadow Spider
             case 59: //Giant Spider
             case 60: //Giant Spider
             case 61: //Spider
             case 62: //Jungle Spider
             case 63: //Deadly Red Spider
             case 64: //Ice Spider
             case 134:
 			return 143;	
 			
 			case 105: //Bear
             case 106:  //Bear
 			return 41;
 			
 			case 412:
 			case 78:
 			return 30;
 			
 			case 2033: //rat
 			return 138;	
 			
 			case 2031: // bloodworm
 			return 2070;
 			
 			case 101: // goblin
 			return 309;	
 			
 			case 81: // cow
 			return 0x03B;
 			
 			case 21: // hero
 			return 451;	
 			
 			case 41: // chicken
 			return 55;	
 			
 			case 9: // guard
 			case 32: // guard
 			case 20: // paladin
 			return 451;	
 			
 			case 1338: // dagannoth
 			case 1340:
 			case 1342:
 			return 1341;
 		
 			case 19: // white knight
 			return 406;
 			
 			case 110:
 			case 111: // ice giant
 			case 112:
 			case 117:
 			return 128;
 			
 			case 2452:
 			return 1312;
 			
 			case 2889:
 			return 2859;
 			
 			case 118:
 			case 119:
 			return 99;
 			
 			case 82://Lesser Demon
             case 83://Greater Demon
             case 84://Black Demon
             case 1472://jungle demon
 			return 64;
 			
 			case 1267:
 			case 1265:
 			return 1312;
 			
 			case 125: // ice warrior
 			case 178:
 			return 451;
 			
 			case 1153: //Kalphite Worker
             case 1154: //Kalphite Soldier
             case 1155: //Kalphite guardian
             case 1156: //Kalphite worker
             case 1157: //Kalphite guardian
 			return 1184;
 			
 			case 123:
 			case 122:
 			return 164;
 
 
 
 
 case 7334:
 return 8172;
 case 7336:
 return 7871;
 case 5228:
 return 5228;
 
 case 7340:
 return 7879;
 
 case 7342:
 return 7879;
 
 case 7344:
 return 8183;
 
 case 7346:
 return 8048;
 
 case 7348:
 return 5989;
 
 
 case 7350:
 return 7693;
 
 			
 			case 2028: // karil
 			return 2075;
 					
 			case 2025: // ahrim
 			return 729;
 			
 			case 2026: // dharok
 			return 2067;
 			
 			case 2027: // guthan
 			return 2080;
 			
 			case 2029: // torag
 			return 0x814;
 			
 			case 2030: // verac
 			return 2062;
 			
 			case 2881: //supreme
 			return 2855;
 			
 			case 2882: //prime
 			return 2854;
 			
 			case 2883: //rex
 			return 2851;
 			
 			case 3340: // giant mole test
 			return 3312;
 			
 			case 3200:
 			return 3146;
 			
 			case 3847:
 			if (npcs[i].attackType == 2)
 			return 3992;
 			if (npcs[i].attackType == 1)
 			return 3992;
 			
 			case 8349://tormented demon
 				if (npcs[i].attackType == 2)
 					return 10917;
 				else if (npcs[i].attackType == 1)
 					return 10918;
 				else if (npcs[i].attackType == 0)
 					return 10922;
 
 
 
 			
 					
 			case 8133://corp beast
 				if (npcs[i].attackType == 2)
 					return 10053;
 				else if (npcs[i].attackType == 1)
 					return 10059;
 				else if (npcs[i].attackType == 0)
 					return 10057;
 case 10127:
 if (npcs[i].attackType == 2)
 					return 13176;
 				else if (npcs[i].attackType == 0)
 					return 13169;
 
 case 9947:
 if (npcs[i].attackType == 2)
 					return 13770;
 				else if (npcs[i].attackType == 0)
 					return 13771;
 					
 case 10141://corp beast
 				if (npcs[i].attackType == 2)
 					return 10053;
 				else if (npcs[i].attackType == 0)
 					return 13599;
 				else if (npcs[i].attackType == 1)
 					return 13603;
 
 			
 			case 2745:
 			if (npcs[i].attackType == 2)
 			return 9300;
 			else if (npcs[i].attackType == 1)
 			return 9276;
 			else if (npcs[i].attackType == 0)
 			return 9277;
 			case 655:
 			return 129;
 			
 			default:
 			return 0x326;		
 		}
 	}
 
 	
 	public int getDeadEmote(int i) {
 		switch(npcs[i].npcType) {
 			case 8133: // corp beast
 				return 10059;
 case 10141:
 return 13602;
 case 10127:
 return 13171;
 case 10057:
 return 10815;
 			case 8349: // torm demon
 				return 10924;
 			//sara gwd
 			case 6247:
 			return 6965;
 case 10775:
 return 13153;
 case 9947:
 return 13772;
 			case 6248:
 			return 6377;
 			case 6250:
 			return 7016;
 			case 6252:
 			return 7011;
 			//bandos gwd
 			case 6261:
 			case 6263:
 			case 6265:
 			return 6156;
 			case 6260:
 			return 7062;
 			case 2892:
 			case 2894:
 			return 2865;
 			case 1612: //banshee
 			return 1524;
 			case 6203: //zammy gwd
 			return 6946;
 			case 6204:
 			case 6206:
 			case 6208:
 			return 67;
 			case 6222:
 			return 6975;
 			case 6223:
 			case 6225:
 			case 6227:
 			return 6956;
 			case 2607:
 			return 2607;
 			case 2627:
 			return 2620;
 			case 2630:
 			return 2627;
 			case 2631:
 			return 2630;
 			case 2738:
 			return 2627;
 			case 2741:
 			return 2638;
 			case 2746:
 			return 2638;
 			case 2743:
 			return 2646;
 			case 2745:
 			return 2654;
 			
 			case 6142:
 			case 6143:
 			case 6144:
 			case 6145:
 			return -1;
 			
 			case 3200:
 			return 3147;
 			
 			case 3847:
 			return 3993;
 			
 			case 2035: //spider
 			return 146;
 			
 			case 2033: //rat
 			return 141;
 			
 			case 2031: // bloodvel
 			return 2073;
 			
 			case 101: //goblin
 			return 313;
 			
 			case 81: // cow
 			return 0x03E;
 			
 			case 41: // chicken
 			return 57;
 			
 			case 1338: // dagannoth
 			case 1340:
 			case 1342:
 			return 1342;
 			
 			case 2881:
 			case 2882:
 			case 2883:
 			return 2856;
 			
 			case 111: // ice giant
 			return 131;
 			
 			case 125: // ice warrior
 			return 843;
 			
 			case 751://Zombies!!
 			return 302;
 			
 			case 1626:
             case 1627:
             case 1628:
             case 1629:
             case 1630:
             case 1631:
             case 1632: //turoth!
             return 1597;
 			
 			case 1616: //basilisk
             return 1548;
 			
 			case 1653: //hand
             return 1590;
 			
 			case 82://demons
 			case 83:
 			case 84:
 			return 67;
 			
 			case 1605://abby spec
 			return 1508;
 			
 			case 51://baby drags
 			case 52:
 			case 1589:
 			case 3376:
 			return 28;
 			
 			case 1610:
 			case 1611:
 			return 1518;
 			
 			case 1618:
 			case 1619:
 			return 1553;
 			
 			case 1620: case 1621:
 			return 1563;
 			
 			case 2783:
 			return 2733;
 			
 			case 1615:
 			return 1538;
 			
 			case 1624:
 			return 1558;
 			
 			case 1613:
 			return 1530;
 			
 			case 1633: case 1634: case 1635: case 1636:
 			return 1580;
 			
 			case 1648: case 1649: case 1650: case 1651: case 1652: case 1654: case 1655: case 1656: case 1657:
 			return 1590;
 			
 			case 100: case 102:
 			return 313;
 			
 			case 105:
 			case 106:
 			return 44;
 			
 			case 412:
 			case 78:
 			return 36;
 			
 			case 122:
 			case 123:
 			return 167;
 			
 			case 58: case 59: case 60: case 61: case 62: case 63: case 64: case 134:
 			return 146;
 			
 			case 1153: case 1154: case 1155: case 1156: case 1157:
 			return 1190;
 			
 			case 103: case 104:
 			return 123;
 			
 			case 118: case 119:
 			return 102;
 			
 			case 3340:
 			return 3310;
 			
 			
 			case 50://drags
 			case 53:
 			case 54:
 
 			case 55:
 			case 941:
 			case 1590:
 			case 1591:
 			case 1592:
 			return 92;
 			
 			
 			case 1158:
 			case 3835:
 				return 6242;
 			case 1160:
 			case 3836:
 				return 6233;
 			
 			
 			default:
 			return 2304;
 		}
 	}
 	public boolean AttackNPC(int NPCID) {
 		if (Server.npcHandler.npcs[npcs[NPCID].attacknpc] != null) {
 			int EnemyX = Server.npcHandler.npcs[npcs[NPCID].attacknpc].absX;
 			int EnemyY = Server.npcHandler.npcs[npcs[NPCID].attacknpc].absY;
 			int EnemyHP = Server.npcHandler.npcs[npcs[NPCID].attacknpc].HP;
 			int hitDiff = 0;
 
 			hitDiff = Misc.random(npcs[NPCID].maxHit);
 			if (goodDistance(EnemyX, EnemyY, npcs[NPCID].absX,npcs[NPCID].absY, 1) == true) {
 				if (Server.npcHandler.npcs[npcs[NPCID].attacknpc].isDead == true) {
 					//ResetAttackNPC(NPCID);
 					// npcs[NPCID].textUpdate = "Oh yeah I win!";
 					// npcs[NPCID].textUpdateRequired = true;
 					npcs[NPCID].animNumber = 2103;
 					npcs[NPCID].animUpdateRequired = true;
 					npcs[NPCID].updateRequired = true;
 				} else {
 					if ((EnemyHP - hitDiff) < 0) {
 						hitDiff = EnemyHP;
 					}
 					if (npcs[NPCID].npcType == 9)
 						npcs[NPCID].animNumber = 386;
 					if (npcs[NPCID].npcType == 3200)
 						npcs[NPCID].animNumber = 0x326; // drags: chaos ele
 					// emote ( YESSS )
 					if ((npcs[NPCID].npcType == 1605)
 							|| (npcs[NPCID].npcType == 1472)) {
 						npcs[NPCID].animNumber = 386; // drags: abberant
 						// spector death ( YAY )
 					}
 					npcs[NPCID].animUpdateRequired = true;
 					npcs[NPCID].updateRequired = true;
 					Server.npcHandler.npcs[npcs[NPCID].attacknpc].hitDiff = hitDiff;
 					Server.npcHandler.npcs[npcs[NPCID].attacknpc].attacknpc = NPCID;
 					Server.npcHandler.npcs[npcs[NPCID].attacknpc].updateRequired = true;
 					Server.npcHandler.npcs[npcs[NPCID].attacknpc].hitUpdateRequired = true;
 					npcs[NPCID].actionTimer = 7;
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 public void attackNPC(int c, int i) {
 		if(npcs[i] != null) {
 			if (npcs[i].isDead)
 				return;
 			if (!npcs[i].inMulti() && npcs[i].underAttackBy > 0 && npcs[i].underAttackBy != npcs[c].npcId) {
 				npcs[i].killerId = 0;
 				return;
 			}
 			if (!npcs[i].inMulti() && (npcs[c].underAttackBy > 0 || (npcs[c].underAttackBy2 > 0 && npcs[c].underAttackBy2 != i))) {
 				npcs[i].killerId = 0;
 				return;
 			}
 			if (npcs[i].heightLevel != npcs[c].heightLevel) {
 				npcs[i].killerId = 0;
 				return;
 			}
 follownpc(i, c);
 			npcs[i].facePlayer(npcs[c].npcId);
 npcs[i].facenpc(npcs[c].npcId);
 			boolean special = false;//specialCase(c,i);
 			if(goodDistance(npcs[i].getX(), npcs[i].getY(), npcs[c].getX(), npcs[c].getY(), distanceRequired(i)) || special) {
 				if(npcs[c].actionTimer <= 0) {
 					npcs[i].facePlayer(npcs[c].npcId);
 					npcs[i].attackTimer = getNpcDelay(i);
 					npcs[i].hitDelayTimer = getHitDelay(i);
 					npcs[i].attackType = 0;
 
 					if (special)
 						loadSpell2(i);
 					else
 
 						loadSpell(i);
 
 
 
 
 					npcs[c].underAttackBy2 = i;
 					npcs[c].actionTimer = 7;
 npcs[i].actionTimer = 5;
 int damg = Misc.random(npcs[i].maxHit);
 npcs[c].handleHitMask(damg);
 npcs[c].HP -= damg;
 npcs[c].hitUpdateRequired2 = true;
 npcs[c].hitUpdateRequired = true;
 
 					npcs[i].oldIndexNPC = npcs[c].npcId;
 					startAnimation(getAttackEmote(i), i);
 					//c.getPA().removeAllWindows();
 				} 
 			} 
 		}
 	}
 	public void attacknpc(int i) {
 		if(npcs[i] != null) {
 			if (npcs[i].isDead)
 				return;
 			if (!npcs[i].inMulti() && npcs[i].underAttackBy > 0) {
 				npcs[i].killerId = 0;
 				return;
 			}
 
 
 			
 
 			boolean special = false;//specialCase(c,i);
 			if(goodDistance(npcs[i].getX(), npcs[i].getY(), Server.npcHandler.npcs[npcs[i].attacknpc].getX(), Server.npcHandler.npcs[npcs[i].attacknpc].getY(), 1) || special) {
 			if(npcs[i].actionTimer <= 0 && npcs[i].isDead == false && Server.npcHandler.npcs[npcs[i].attacknpc].isDead == false)
 {
 
 					npcs[i].facePlayer(Server.npcHandler.npcs[npcs[i].attacknpc].npcId);
 Server.npcHandler.npcs[npcs[i].attacknpc].facePlayer(npcs[i].npcId);
 					npcs[i].attackTimer = getNpcDelay(i);
 					npcs[i].hitDelayTimer = getHitDelay(i);
 					npcs[i].attackType = 0;
 					if (special)
 						loadSpell2(i);
 					else
 						loadSpell(i);
 					if (npcs[i].attackType == 3)
 						npcs[i].hitDelayTimer += 2;
 					if (multiAttacks(i)) {
 						multiAttackGfx(i, npcs[i].projectileId);
 						startAnimation(getAttackEmote(i), i);
 						npcs[i].oldIndex = Server.npcHandler.npcs[npcs[i].attacknpc].npcId;
 						return;
 					}
 					//if(npcs[i].projectileId > 0) {
 						//int nX = Server.npcHandler.npcs[i].getX() + offset(i);
 						//int nY = Server.npcHandler.npcs[i].getY() + offset(i);
 						//int pX = npcs[othernpc].getX();
 						//int pY = npcs[othernpc].getY();
 						//int offX = (nY - pY)* -1;
 						//int offY = (nX - pX)* -1;
 						//npcs[othernpc].createPlayersProjectile(nX, nY, offX, offY, 50, getProjectileSpeed(i), npcs[i].projectileId, 43, 31, -c.getId() - 1, 65);
 					//}
 					Server.npcHandler.npcs[npcs[i].attacknpc].handleHitMask(Misc.random(npcs[i].maxHit));
 if(Server.npcHandler.npcs[npcs[i].attacknpc].actionTimer <= 0 && npcs[i].isDead == false && Server.npcHandler.npcs[npcs[i].attacknpc].isDead == false)
 {
 
 Server.npcHandler.npcs[npcs[i].attacknpc].actionTimer = 7;
 Server.npcHandler.npcs[npcs[i].attacknpc].handleHitMask(Misc.random(Server.npcHandler.npcs[npcs[i].attacknpc].maxHit));
 				startAnimation(getAttackEmote(npcs[i].attacknpc), npcs[i].attacknpc);
 }
 					npcs[i].actionTimer = 7;
 					npcs[i].npcIndex = Server.npcHandler.npcs[npcs[i].attacknpc].npcId;
 					//npcs[i].facenpc(Server.npcHandler.npcs[npcs[i].attacknpc].npcId);
 					//Server.npcHandler.npcs[npcs[i].attacknpc].facenpc(npcs[i].npcId);
 					startAnimation(getAttackEmote(i), i);
 Server.npcHandler.npcs[npcs[i].attacknpc].attacknpc = i;
 	
 					
 				} 
 			}			
 		}
 	}
 	/**
 	* Attack delays
 	**/
 	public int getNpcDelay(int i) {
 		switch(npcs[i].npcType) {
 			case 2025:
 			case 2028:
 			return 7;
 						case 1158:
 			case 1160:
 				return 6;
 			case 8133: // Corporeal beast
 			case 3101: // Melee
 			case 3102: // Range
 			case 3103: // Mage
 			return 7;
 			case 8349: case 8350: case 8351:
 			if (npcs[i].attackType == 2)
 			return 4;
 			else if (npcs[i].attackType == 1)
 			return 6;
 			else if (npcs[i].attackType == 0)
 			return 7;
 			case 3495:
 			return 3;
 			case 2745:
 			return 8;
 			case 50:
 			case 53:
 			case 54:
 			case 55:
 			case 941:
 			case 1590:
 			case 1591:
 			case 1592:
 			return 8;
 
 			case 6222:
 			case 6223:
 			case 6206:
 			case 6208:
 			case 6204:
 			case 6225:
 			case 6227:
 			case 6260:
 			return 6;
 			//saradomin gw boss
 			case 6247:
 			return 3;
 			
 			default:
 			return 5;
 		}
 	}
 	
 	/**
 	* Hit delays
 	**/
 	public int getHitDelay(int i) {
 		switch(npcs[i].npcType) {
 			case 2881:
 			case 2882:
 			case 3200:
 			case 2892:
 			case 2894:
 			case 6208:
 			case 6206:
 			case 6203:
 			return 3;
 						case 1158:
 			case 1160:
 				if (npcs[i].attackType == 1 || npcs[i].attackType == 2)
 					return 3;
 				else
 					return 2;
 			case 2743:
 			case 2631:
 			case 6222:
 			case 6223:
 			case 6225:
 
 			case 6239: // Aviansie
 			case 6230:
 			case 6233:
 			case 6232:
 			case 6276:
 			case 6257:
 			case 6221:
 
 			return 3;
 			
 			case 2745:
 			if (npcs[i].attackType == 1 || npcs[i].attackType == 2) {
 			return 5;
 			} else {
 			return 2;
 			} 			
 			case 2025:
 			return 4;
 			case 2028:
 			return 3;
 			case 3495:
 			return 2;
 
 			default:
 			return 2;
 		}
 	}
 	
 	public int getSummSpecialAnim(int id) {
 		switch(Server.npcHandler.npcs[id].npcType) {
 		case 6795:
 			return 8229;
 
 			default:
 			return -1;
 		}
 	}
 		
 	/**
 	* Npc respawn time
 	**/
 	public int getRespawnTime(int i) {
 		switch(npcs[i].npcType) {
 			case 6261:
 			case 6263:
 			case 6265:
 			case 8349: case 8350: case 8351:
 			return 100;
 						case 1158:
 				return 200;
 			case 6203:
 			case 6204:
 			case 6206:
 			case 2881:
 			case 2882:
 			case 2883:
 			case 6222:
 			case 6223:
 			case 6225:
 			case 6227:
 			case 6247:
 			case 6248:
 			case 6250:
 			case 6260:
 			case 6208:
 			return 125;
 			case 8133: // Corporeal beast
 			case 3101: // Melee
 			case 3102: // Range
 			case 3103: // Mage
 			return 140;
 
 			case 3247: // Godwars
 			case 6270:
 			case 6219:
 			case 6255:
 			case 6229:
 			case 6277:
 			case 6233:
 			case 6232:
 			case 6218:
 			case 6269:
 			case 3248:
 			case 6212:
 			case 6220:
 			case 6276:
 			case 6256:
 			case 6239: // Aviansie
 			case 6230:
 			case 6221:
 			case 6231:
 			case 6257:
 			case 6278:
 			case 6272:
 			case 6274:
 			case 6254:
 			case 6258:
 			return 80;
 			case 50://drags
 			case 53:
 			case 54:
 			case 55:
 			case 941:
 			case 1590:
 			case 1591:
 			case 4291: // Cyclops
 			case 4292: // Ice cyclops
 			case 1592:
 			return 110;
 			case 6142:
 			case 6143:
 			case 6144:
 			case 6145:
 			return 500;
 			default:
 			return 25;
 		}
 	}
 	
 	
 	
 	
 	public void newNPC(int npcType, int x, int y, int heightLevel, int WalkingType, int HP, int maxHit, int attack, int defence) {
 		int slot = -1;
 		for (int i = 1; i < maxNPCs; i++) {
 			if (npcs[i] == null) {
 				slot = i;
 				break;
 			}
 		}
 		if(slot == -1)
 			return;
 		NPC newNPC = new NPC(slot, npcType);
 		newNPC.absX = x;
 		newNPC.absY = y;
 		newNPC.makeX = x;
 		newNPC.makeY = y;
 		newNPC.heightLevel = heightLevel;
 		newNPC.walkingType = WalkingType;
 		newNPC.HP = HP;
 		newNPC.MaxHP = HP;
 		newNPC.maxHit = maxHit;
 		newNPC.attack = attack;
 		newNPC.defence = defence;
 		npcs[slot] = newNPC;
 	}
 
 	public void newNPCList(int npcType, String npcName, int combat, int HP) {
 		int slot = -1;
 		for (int i = 0; i < maxListedNPCs; i++) {
 			if (NpcList[i] == null) {
 				slot = i;
 				break;
 			}
 		}
 		if(slot == -1)
 			return;
 		NPCList newNPCList = new NPCList(npcType);
 		newNPCList.npcName = npcName;
 		newNPCList.npcCombat = combat;
 		newNPCList.npcHealth = HP;
 		NpcList[slot] = newNPCList;
 	}
 
 	
 
 	public void process() {
 
 		for (int i = 0; i < maxNPCs; i++) {
 			if (npcs[i] == null) continue;
 			npcs[i].clearUpdateFlags();
 			
 		}
 		
 		for (int i = 0; i < maxNPCs; i++) {
 			if (npcs[i] != null) {
 
 
 
 
 
 if(npcs[i].summon == true) {
 							Client c = (Client)Server.playerHandler.players[npcs[i].spawnedBy];	
 						
 
 if(c != null && c.npcIndex > 0) {
 
 follownpc(i, c.npcIndex);
 }
 
 if(c != null && c.playerIndex < 1 && npcs[i].summon == true) {
 if(!npcs[i].underAttack) {
 if(!Server.playerHandler.players[npcs[i].spawnedBy].goodDistance(npcs[i].getX(), npcs[i].getY(), Server.playerHandler.players[npcs[i].spawnedBy].getX(), Server.playerHandler.players[npcs[i].spawnedBy].getY(), 2) && c.npcIndex < 1)
 followPlayer(i, c.playerId);
 }
 } else {
 if(c != null && npcs[i].summon == true) {
 if(!Server.playerHandler.players[npcs[i].spawnedBy].goodDistance(npcs[i].getX(), npcs[i].getY(), Server.playerHandler.players[npcs[i].spawnedBy].getX(), Server.playerHandler.players[npcs[i].spawnedBy].getY(), 5) && c.playerIndex < 1 || c.npcIndex < 1)
 {
 followPlayer(i, c.playerId);
 }
 }
 	
 }			
 
 
 				if(c != null && c.lastsummon > 0 && !Server.playerHandler.players[npcs[i].spawnedBy].goodDistance(npcs[i].getX(), npcs[i].getY(), Server.playerHandler.players[npcs[i].spawnedBy].getX(), Server.playerHandler.players[npcs[i].spawnedBy].getY(), 10) && npcs[i].summon == true && !npcs[i].isDead)
 				{
 
 npcs[i].isDead = true;
 npcs[i].applyDead = true;
 c.Summoning.SummonNewNPC(c.lastsummon);
 npcs[i].gfx0(1315);
 npcs[i].underAttackBy2 = -1;
 npcs[i].updateRequired = true;
 npcs[i].dirUpdateRequired = true;
 npcs[i].getNextWalkingDirection();
 				}
 						if (c != null && c.canStartSpecialAnim == true) {
 					startAnimation(getSummSpecialAnim(i), i);
 					c.canStartSpecialAnim = false;
 				}
 
 
 if(c != null && c.lastsummon < 0 || c == null)
 {
 npcs[i].isDead = true;
 npcs[i].applyDead = true;
 npcs[i].summon = false;
 npcs[i].underAttackBy2 = -1;
 }
 
 
 if(c != null && npcs[i].actionTimer < 1 && npcs[i].summon == true)
 {
 if(c.playerIndex > 0)
 {
 	Client o = (Client)Server.playerHandler.players[c.playerIndex];
 if(o != null) {
 if(npcs[i].IsAttackingPerson = true && o.inMulti())
 {
 followPlayer(i, o.playerId);
 attackPlayer(o, i);
 npcs[i].index = o.playerId;
 npcs[i].actionTimer = 7;
 }
 }
 }
 }
 				
 				}
 				if (npcs[i].npcType == 6260) {
 					if (Misc.random(10) <= 1) {
 						npcs[i].forceChat(sGraardorM());
 					}
 				}
 				if (npcs[i].npcType == 6203) {
 					if (Misc.random(10) <= 1) {
 						npcs[i].forceChat(sKrilM());
 					}
 				}
 				if (npcs[i].npcType == 6247) {
 					if (Misc.random(10) <= 1) {
 						npcs[i].forceChat(sZilyanaM());
 					}
 				}
 				if (npcs[i].npcType == 443) {
 					if (Misc.random(10) <= 1) {
 						npcs[i].forceChat(sTreeM());
 					}
 				}
 				
 				if (npcs[i].actionTimer > 0) {
 					npcs[i].actionTimer--;
 				}
 				
 				if (npcs[i].freezeTimer > 0) {
 					npcs[i].freezeTimer--;
 				}
 				
 				if (npcs[i].hitDelayTimer > 0) {
 					npcs[i].hitDelayTimer--;
 				}
 				
 				if (npcs[i].hitDelayTimer == 1) {
 					npcs[i].hitDelayTimer = 0;
 					applyDamage(i);
 				}
 				
 				if(npcs[i].attackTimer > 0) {
 					npcs[i].attackTimer--;
 				}
 			
 				if(npcs[i].spawnedBy > 0) { // delete summons npc
 					if(Server.playerHandler.players[npcs[i].spawnedBy] == null
 					|| Server.playerHandler.players[npcs[i].spawnedBy].heightLevel != npcs[i].heightLevel	
 					|| Server.playerHandler.players[npcs[i].spawnedBy].respawnTimer > 0 
 					|| !Server.playerHandler.players[npcs[i].spawnedBy].goodDistance(npcs[i].getX(), npcs[i].getY(), Server.playerHandler.players[npcs[i].spawnedBy].getX(), Server.playerHandler.players[npcs[i].spawnedBy].getY(), 10)) {
 							
 						if(Server.playerHandler.players[npcs[i].spawnedBy] != null) {
 							for(int o = 0; o < Server.playerHandler.players[npcs[i].spawnedBy].barrowsNpcs.length; o++){
 								if(npcs[i].npcType == Server.playerHandler.players[npcs[i].spawnedBy].barrowsNpcs[o][0]) {
 									if (Server.playerHandler.players[npcs[i].spawnedBy].barrowsNpcs[o][1] == 1)
 										Server.playerHandler.players[npcs[i].spawnedBy].barrowsNpcs[o][1] = 0;
 								}
 							}
 						}
 						npcs[i] = null;
 					}
 				}
 				if (npcs[i] == null) continue;
 				
 				/**
 				* Attacking player
 				**/
 				if (isAggressive(i) && !npcs[i].underAttack && !npcs[i].isDead && !switchesAttackers(i)) {
 					npcs[i].killerId = getCloseRandomPlayer(i);
 				} else if (isAggressive(i) && !npcs[i].underAttack && !npcs[i].isDead && switchesAttackers(i)) {
 					npcs[i].killerId = getCloseRandomPlayer(i);
 				}
 				
 				if (System.currentTimeMillis() - npcs[i].lastDamageTaken > 5000)
 					npcs[i].underAttackBy = 0;
 				
 				if((npcs[i].killerId > 0 || npcs[i].underAttack) && !npcs[i].walkingHome && retaliates(npcs[i].npcType)) {
 					if(!npcs[i].isDead) {
 						int p = npcs[i].killerId;
 						if(Server.playerHandler.players[p] != null) {
 							Client c = (Client) Server.playerHandler.players[p];					
 							followPlayer(i, c.playerId);
 							if (npcs[i] == null) continue;
 							if(npcs[i].attackTimer == 0) {
 								if(c != null) {
 									attackPlayer(c, i);
 								} else {
 									npcs[i].killerId = 0;
 									npcs[i].underAttack = false;
 									npcs[i].facePlayer(0);
 								}
 							}
 						} else {
 							npcs[i].killerId = 0;
 							npcs[i].underAttack = false;
 							npcs[i].facePlayer(0);
 						}
 					}
 				}
 				/**
 				* Random walking and walking home
 				**/
 				if (npcs[i] == null) continue;
 				if((!npcs[i].underAttack || npcs[i].walkingHome) && npcs[i].randomWalk && !npcs[i].isDead) {
 					npcs[i].facePlayer(0);
 					npcs[i].killerId = 0;	
 					if(npcs[i].spawnedBy == 0) {
 						if((npcs[i].absX > npcs[i].makeX + Config.NPC_RANDOM_WALK_DISTANCE) || (npcs[i].absX < npcs[i].makeX - Config.NPC_RANDOM_WALK_DISTANCE) || (npcs[i].absY > npcs[i].makeY + Config.NPC_RANDOM_WALK_DISTANCE) || (npcs[i].absY < npcs[i].makeY - Config.NPC_RANDOM_WALK_DISTANCE)) {
 							npcs[i].walkingHome = true;
 						}
 					}
 
 					if (npcs[i].walkingHome && npcs[i].absX == npcs[i].makeX && npcs[i].absY == npcs[i].makeY) {
 						npcs[i].walkingHome = false;
 					} else if(npcs[i].walkingHome) {
 						npcs[i].moveX = GetMove(npcs[i].absX, npcs[i].makeX);
 			      		npcs[i].moveY = GetMove(npcs[i].absY, npcs[i].makeY);
 						npcs[i].getNextNPCMovement(i); 
 						npcs[i].updateRequired = true;
 					}
 					if(npcs[i].walkingType == 1) {
 						if(Misc.random(3)== 1 && !npcs[i].walkingHome) {
 							int MoveX = 0;
 							int MoveY = 0;			
 							int Rnd = Misc.random(9);
 							if (Rnd == 1) {
 								MoveX = 1;
 								MoveY = 1;
 							} else if (Rnd == 2) {
 								MoveX = -1;
 							} else if (Rnd == 3) {
 								MoveY = -1;
 							} else if (Rnd == 4) {
 								MoveX = 1;
 							} else if (Rnd == 5) {
 								MoveY = 1;
 							} else if (Rnd == 6) {
 								MoveX = -1;
 								MoveY = -1;
 							} else if (Rnd == 7) {
 								MoveX = -1;
 								MoveY = 1;
 							} else if (Rnd == 8) {
 								MoveX = 1;
 								MoveY = -1;
 							}
 										
 							if (MoveX == 1) {
 								if (npcs[i].absX + MoveX < npcs[i].makeX + 1) {
 									npcs[i].moveX = MoveX;
 								} else {
 									npcs[i].moveX = 0;
 								}
 							}
 							
 							if (MoveX == -1) {
 								if (npcs[i].absX - MoveX > npcs[i].makeX - 1)  {
 									npcs[i].moveX = MoveX;
 								} else {
 									npcs[i].moveX = 0;
 								}
 							}
 							
 							if(MoveY == 1) {
 								if(npcs[i].absY + MoveY < npcs[i].makeY + 1) {
 									npcs[i].moveY = MoveY;
 								} else {
 									npcs[i].moveY = 0;
 								}
 							}
 							
 							if(MoveY == -1) {
 								if(npcs[i].absY - MoveY > npcs[i].makeY - 1)  {
 									npcs[i].moveY = MoveY;
 								} else {
 									npcs[i].moveY = 0;
 								}
 							}
 								
 
 							int x = (npcs[i].absX + npcs[i].moveX);
 							int y = (npcs[i].absY + npcs[i].moveY);
 							if (VirtualWorld.I(npcs[i].heightLevel, npcs[i].absX, npcs[i].absY, x, y, 0))
 								npcs[i].getNextNPCMovement(i);
 							else
 							{
 								npcs[i].moveX = 0;
 								npcs[i].moveY = 0;
 							} 
 							npcs[i].updateRequired = true;
 						}
 					}
 				}
 				
 				if (npcs[i].isDead == true) {
 					if (npcs[i].actionTimer == 0 && npcs[i].applyDead == false && npcs[i].needRespawn == false) {
 						npcs[i].updateRequired = true;
 						npcs[i].facePlayer(0);
 						npcs[i].killedBy = getNpcKillerId(i);
 						npcs[i].animNumber = getDeadEmote(i); // dead emote
 						npcs[i].animUpdateRequired = true;
 						npcs[i].freezeTimer = 0;
 						npcs[i].applyDead = true;
 						killedBarrow(i);
 						getDtLastKill(i);
 						//if (isFightCaveNpc(i))
 							//killedTzhaar(i);
 if(npcs[i].summon == true)
 npcs[i].summon = false;
 						npcs[i].actionTimer = 4; // delete time
 						resetPlayersInCombat(i);
 						if (npcs[i].npcType == 1158){
 							handleKalpite(i);
 						}
 						
 						
 					} else if (npcs[i] != null && npcs[i].actionTimer == 0 && npcs[i].applyDead == true &&  npcs[i].needRespawn == false) {
 						//if (npcs[i] != null) {
 						npcs[i].needRespawn = true;
 						npcs[i].actionTimer = getRespawnTime(i); // respawn time
 						dropItems(i); // npc drops items!
 						appendSlayerExperience(i);
 						//appendKillCount(i);
 						npcs[i].absX = npcs[i].makeX;
 						npcs[i].absY = npcs[i].makeY;				
 						npcs[i].HP = npcs[i].MaxHP;
 						npcs[i].animNumber = 0x328;
 						npcs[i].updateRequired = true;
 						npcs[i].animUpdateRequired = true;
 						if (npcs[i].npcType >= 2440 && npcs[i].npcType <= 2446) {
 							Server.objectManager.removeObject(npcs[i].absX, npcs[i].absY);
 						}
 						if (npcs[i].npcType == 2745) {
 							handleJadDeath(i);
 						}
 						if (npcs[i].npcType == 49) {
 							handleShade(i);
 						}
 						if (npcs[i].npcType == 3491) {
 							handleRFDDeath(i);
 						}
 						
 						if (npcs[i] != null && npcs[i].npcType != 1158){
 							if (!npcs[i].needRespawn){
 								npcs[i] = null;
 								for (int j = 1; j < Config.MAX_PLAYERS; j++) {
 									if (Server.playerHandler.players[j] != null) {
 										Server.playerHandler.players[j].RebuildNPCList = true;
 									}
 								}
 								continue;
 							}
 							npcs[i].needRespawn = true;
 							npcs[i].actionTimer = 30;
 							npcs[i].absX = npcs[i].makeX;
 							npcs[i].absY = npcs[i].makeY;
 						} else if (npcs[i] != null && npcs[i].npcType == 1158) {
 							npcs[i] = null;
 							for (int j = 1; j < Config.MAX_PLAYERS; j++) {
 								if (Server.playerHandler.players[j] != null){
 									Server.playerHandler.players[j].RebuildNPCList = true;
 								}
 							}
 						}
 						
 					} else if (npcs[i].actionTimer == 0 && npcs[i].needRespawn == true) {					
 						if(npcs[i].spawnedBy > 0) {
 							npcs[i] = null;
 						} else {
 							/*if (npcs[i].actionTimer <= 0 && npcs[i].needRespawn && npcs[i].NeedsRespawn) {
 							for (int j = 1; j < Config.MAX_PLAYERS; j++) {
 								if (Server.playerHandler.players[j] != null) {
 									Server.playerHandler.players[j].RebuildNPCList = true;
 								}
 							}*/
 								int type = npcs[i].npcType;
 								if (type == 1160)
 									type = 1158;
 								int x = npcs[i].makeX;
 								int y = npcs[i].makeY;
 								int height = npcs[i].heightLevel;
 								int walk = npcs[i].walkingType;
 								int mHp = npcs[i].MaxHP;
 								int mHit = npcs[i].maxHit;
 								int attack = npcs[i].attack;	
 								int defence = npcs[i].defence;
 								
 								npcs[i] = null;
 								newNPC(type, x, y, height, walk, mHp, mHit, attack, defence);
 						}
 					}
 				}
 			}
 		}
 	}
        
 	public boolean getsPulled(int i) {
 		switch (npcs[i].npcType) {
 			case 6260:
 				if (npcs[i].firstAttacker > 0)
 					return false;
 			break;
 		}
 		return true;
 	}
 	   
 	public boolean multiAttacks(int i) {
 		switch (npcs[i].npcType) {
 			case 6222://bandos?
 			return true;
 
 			case 8133: // Corporeal beast
 			if (npcs[i].attackType == 2)
 				return true;
 			
 			case 6247://saradomin?
 			if (npcs[i].attackType == 2)
 				return true;
 				
 			case 6260://armadyl?
 			if (npcs[i].attackType == 1)
 				return true;	
 				
 			default:
 			return false;
 		}
 	
 	
 	}
 	
 	/**
 	* Npc killer id?
 	**/
 	
 	public int getNpcKillerId(int npcId) {
 		int oldDamage = 0;
 		int count = 0;
 		int killerId = 0;
 		for (int p = 1; p < Config.MAX_PLAYERS; p++)  {	
 			if (Server.playerHandler.players[p] != null) {
 				if(Server.playerHandler.players[p].lastNpcAttacked == npcId) {
 					if(Server.playerHandler.players[p].totalDamageDealt > oldDamage) {
 						oldDamage = Server.playerHandler.players[p].totalDamageDealt;
 						killerId = p;
 					}
 					Server.playerHandler.players[p].totalDamageDealt = 0;
 				}	
 			}
 		}				
 		return killerId;
 	}
 		
 	/**
 	 * 
 	 */
 	private void killedBarrow(int i) {
 		Client c = (Client)Server.playerHandler.players[npcs[i].killedBy];
 		if(c != null) {
 			for(int o = 0; o < c.barrowsNpcs.length; o++){
 				if(npcs[i].npcType == c.barrowsNpcs[o][0]) {
 					c.barrowsNpcs[o][1] = 2; // 2 for dead
 					c.barrowsKillCount++;	
 
 				}
 			}
 		}
 	}
 
 	
 	
 	private void killedTzhaar(int i) {
 		final Client c2 = (Client)Server.playerHandler.players[npcs[i].spawnedBy];
 		c2.tzhaarKilled++;
 		//System.out.println("To kill: " + c2.tzhaarToKill + " killed: " + c2.tzhaarKilled);
 		if (c2.tzhaarKilled == c2.tzhaarToKill) {
 			//c2.sendMessage("STARTING EVENT");
 			c2.waveId++;
 			EventManager.getSingleton().addEvent(new Event() {
 				public void execute(EventContainer c) {
 					if (c2 != null) {
 						Server.fightCaves.spawnNextWave(c2);
 					}	
 					c.stop();
 				}
 			}, 7500);
 			
 		}
 	}
 
 	private void killedRFD(int i) {
 		final Client c2 = (Client)Server.playerHandler.players[npcs[i].spawnedBy];
 		c2.RFDKilled++;
 		//System.out.println("To kill: " + c2.RFDToKill + " killed: " + c2.RFDKilled);
 		if (c2.RFDKilled == c2.RFDToKill) {
 			//c2.sendMessage("STARTING EVENT");
 			c2.waveId++;
 			EventManager.getSingleton().addEvent(new Event() {
 				public void execute(EventContainer c) {
 					if (c2 != null) {
 						Server.rfd.spawnNextWave(c2);
 					}	
 					c.stop();
 				}
 			}, 7500);
 			
 		}
 	}
 	
 		public void handleShade(int i) {
 		Client c = (Client)Server.playerHandler.players[npcs[i].spawnedBy];
 		int random = Misc.random(50);
 		if(random == 50) {
 			if(c.combatLevel >= 3 && c.combatLevel <= 25) {
 				Server.npcHandler.spawnNpc(c, 425, c.getX(), c.getY()-1, 0, 0, 120, 5, 50, 50, true, true);
 			} else if(c.combatLevel >= 26 && c.combatLevel <= 50) {
 				Server.npcHandler.spawnNpc(c, 427, c.getX(), c.getY()-1, 0, 0, 120, 8, 75, 75, true, true);
 			} else if(c.combatLevel >= 51 && c.combatLevel <= 99) {
 				Server.npcHandler.spawnNpc(c, 428, c.getX(), c.getY()-1, 0, 0, 120, 13, 120, 120, true, true);
 			} else if(c.combatLevel >= 100 && c.combatLevel <= 126) {
 				Server.npcHandler.spawnNpc(c, 430, c.getX(), c.getY()-1, 0, 0, 120, 18, 175, 175, true, true);
 			}
 		} else if(random > 45 && random <= 49) {
 			c.sendMessage("You hear the shade get closer to you");
 		}
 	}
 	
 	public void handleJadDeath(int i) {
 		Client c = (Client)Server.playerHandler.players[npcs[i].spawnedBy];
 		c.getItems().addItem(6570,1);
 		c.sendMessage("Congratulations on completing the fight caves minigame!");
 		c.getPA().resetTzhaar();
 		c.waveId = 300;
 	}
 
 	public void handleRFDDeath(int i) {
 		Client c = (Client)Server.playerHandler.players[npcs[i].spawnedBy];
 		c.sendMessage("Congratulations you have completed the RFD minigame!");
 		c.getPA().resetRFD();
 		c.waveId = 300;
 	}
 	
 	
 	/**
 	* Dropping Items!
 	**/
 	public boolean rareDrops(int i) {
 		return Misc.random(NPCDrops.dropRarity.get(npcs[i].npcType)) == 0;
 	}
 	
 	
 	public void dropItems(int i) {
 		int npc = 0;
 		//long start = System.currentTimeMillis();
 		Client c = (Client)Server.playerHandler.players[npcs[i].killedBy];
 		if(c != null) {
 			if (npcs[i].npcType == 912 || npcs[i].npcType == 913 || npcs[i].npcType == 914)
 				c.magePoints += 1;
 			if (NPCDrops.constantDrops.get(npcs[i].npcType) != null) {
 				for (int item : NPCDrops.constantDrops.get(npcs[i].npcType)) {
 					Server.itemHandler.createGroundItem(c, item, npcs[i].absX, npcs[i].absY, 1, c.playerId);
 					//if (c.clanId >= 0)
 						//Server.clanChat.handleLootShare(c, item, 1);
 				}	
 			}
 						if(npcs[i].npcType > 0) {
 int random2 = Misc.random(8);
 				if(random2 == 4) {
 					Server.itemHandler.createGroundItem(c, 12158, npcs[i].absX, npcs[i].absY, 1, c.playerId);
 				}
 	if(random2 == 3) {
 					Server.itemHandler.createGroundItem(c, 12159, npcs[i].absX, npcs[i].absY, 1, c.playerId);
 				}
 	if(random2 == 2) {
 					Server.itemHandler.createGroundItem(c, 12160, npcs[i].absX, npcs[i].absY, 1, c.playerId);
 				}
 
 	if(random2 == 1) {
 					Server.itemHandler.createGroundItem(c, 12163, npcs[i].absX, npcs[i].absY, 1, c.playerId);
 				}
 				}
 
 if ((npcs[i].npcType == 4278 || npcs[i].npcType == 4279 || npcs[i].npcType == 4280 || npcs[i].npcType == 4281 || npcs[i].npcType == 4282 || npcs[i].npcType == 4283 || npcs[i].npcType == 4284)) {
 c.sendMessage("You gain some tokens.");
 }
 			if(npcs[i].npcType == 4291 || npcs[i].npcType == 4292 && c.inCyclops) {
 				int random2 = Misc.random(25);
 				if(random2 == 1) {
 					Server.itemHandler.createGroundItem(c, c.getWarriorsGuild().getCyclopsDrop(c), npcs[i].absX, npcs[i].absY, 1, c.playerId);
 				}
 			}
 			int[] tokens = {10, 20, 30, 40, 50, 60, 70};
 			if (npcs[i].npcType >= 4278 && npcs[i].npcType <= 4284) {
 				c.getWarriorsGuild().setSpawned(false);
 				//Server.itemHandler.createGroundItem(c, tokens[npcs[i].npcType - 4278], npcs[i].absX, npcs[i].absY, 1, c.playerId);
 			}
 if (npcs[i].npcType == 4278) {
 c.getItems().addItem(8851, 5);
 }
 if (npcs[i].npcType == 425 || npcs[i].npcType == 427 || npcs[i].npcType == 428 || npcs[i].npcType == 430) {
 	if(c.getItems().freeSlots() > 0) {
 		c.getItems().addItem(c.getPA().randomShade(), 1);
 			} else if(c.getItems().freeSlots() == 0) {	
 				c.sendMessage("Not enough inventory space, sorry.");
 	}
 }
 if (ArmadylKC(i)) {
 c.Arma += 1;
 //c.getPA().sendFrame126(""+c.Arma+"", 16216);
 }
 if (npcs[i].npcType == 132) {
 appendJailKc(i);
 }
 if (BandosKC(i)) {
 c.Band += 1;
 //c.getPA().sendFrame126(""+c.Bandos+"", 16217);
 }
 if (SaraKC(i)) {
 c.Sara += 1;
 //c.getPA().sendFrame126(""+c.Sara+"", 16218);
 }
 if (ZammyKC(i)) {
   c.Zammy += 1;
 //c.getPA().sendFrame126(""+c.Zammy+"", 16219);
 }
 if (npcs[i].npcType == 3493) {
 c.Agrith = true;
 }
 if (npcs[i].npcType == 3494) {
 c.Flambeed = true;
 }
 if (npcs[i].npcType == 3495) {
 c.Karamel = true;
 }
 if (npcs[i].npcType == 3496) {
 c.Dessourt = true;
 }
 if (npcs[i].npcType == 3491) {
 c.Culin = true;
 }
 if (npcs[i].npcType == 4279) {
 c.getItems().addItem(8851, 10);
 }
 if (npcs[i].npcType == 4280) {
 c.getItems().addItem(8851, 15);
 }
 if (npcs[i].npcType == 4281) {
 c.getItems().addItem(8851, 20);
 }
 if (npcs[i].npcType == 4282) {
 c.getItems().addItem(8851, 25);
 }
 if (npcs[i].npcType == 4283) {
 c.getItems().addItem(8851, 30);
 }
 if (npcs[i].npcType == 4284) {
 c.getItems().addItem(8851, 40);
 }
 
 			if (NPCDrops.dropRarity.get(npcs[i].npcType) != null) {
 				if (rareDrops(i)) {
 try {
 					int random = Misc.random(NPCDrops.rareDrops.get(npcs[i].npcType).length-1);
 					if (c.CSLS == 3) {
 					if (c.clanId >= 0)
 					Server.clanChat.handleCoinShare(c, NPCDrops.rareDrops.get(npcs[i].npcType)[random][0], NPCDrops.rareDrops.get(npcs[i].npcType)[random][1]);
 					return;
 					}
 					if (c.CSLS == 1) {
 					if (c.clanId >= 0)
 					Server.clanChat.handleLootShare(c, NPCDrops.rareDrops.get(npcs[i].npcType)[random][0], NPCDrops.rareDrops.get(npcs[i].npcType)[random][1]);
 					Server.itemHandler.createGroundItem(c, NPCDrops.rareDrops.get(npcs[i].npcType)[random][0], npcs[i].absX, npcs[i].absY, NPCDrops.rareDrops.get(npcs[i].npcType)[random][1], c.playerId);
 					return;
 					}
 					Server.itemHandler.createGroundItem(c, NPCDrops.rareDrops.get(npcs[i].npcType)[random][0], npcs[i].absX, npcs[i].absY, NPCDrops.rareDrops.get(npcs[i].npcType)[random][1], c.playerId);
 		} catch(Exception ex) {
 		ex.printStackTrace();
 		}
 				} else {
 					int random = Misc.random(NPCDrops.normalDrops.get(npcs[i].npcType).length-1);
 						
 					Server.itemHandler.createGroundItem(c, NPCDrops.normalDrops.get(npcs[i].npcType)[random][0], npcs[i].absX, npcs[i].absY, NPCDrops.normalDrops.get(npcs[i].npcType)[random][1], c.playerId);
 					//Server.clanChat.handleLootShare(c, NPCDrops.normalDrops.get(npcs[i].npcType)[random][0], NPCDrops.normalDrops.get(npcs[i].npcType)[random][1]);
 				}
 			}	
 			
 		}
 
 		//System.out.println("Took: " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	
 	
 	
 	//id of bones dropped by npcs
 	public int boneDrop(int type) {
 		switch (type) {
 			case 1://normal bones
 			case 9:
 			case 100:
 			case 12:
 			case 17:
 			case 803:
 			case 18:
 			case 81:
 			case 101:
 			case 41:
 			case 19:
 			case 90:
 			case 75:
 			case 86:
 			case 78:
 			case 912:
 			case 913:
 			case 914:
 			case 1648:
 			case 1643:
 			case 1618:
 			case 1624:
 			case 181:
 			case 119:
 			case 49:
 			case 26:
 			case 1341:
 
 
 			case 3247:
 			case 6233:
 			case 6232:
 			case 3248:
 			case 6212:
 			case 6254:
 			case 6258:
 			return 526;
 			case 117:
 			case 6270: // Cyclops
 			case 6269: // Ice cyclops
 			case 4291: // Cyclops
 			case 4292: // Ice cyclops
 			return 532;//big bones
 			case 50://drags
 			case 53:
 			case 54:
 			case 55:
 			case 941:
 			case 1590:
 			case 1591:
 			case 1592:
 			case 6218:
 			case 6272:
 			case 6274:
 			return 536;
 			case 84:
 			case 1615:
 			case 1613:
 			case 82:
 			case 3200:
 			case 6208:
 			case 6206:
 			case 6204:
 			case 6203:
 			return 592;
 			case 2881:
 			case 2882:
 			case 2883:
 			return 6729;
 			default:
 			return -1;
 		}	
 	}
 
 	public int getStackedDropAmount(int itemId, int npcId) {
 		switch (itemId) {
 			case 995:
 				switch (npcId) {
 					case 1:
 					return 50+ Misc.random(50);
 					case 9:
 					return 133 + Misc.random(100);
 					case 1624:
 					return 1000 + Misc.random(300);
 					case 1618:
 					return 1000 + Misc.random(300);
 					case 1643:
 					return 1000 + Misc.random(300);
 					case 1610:
 					return 1000 + Misc.random(1000);
 					case 1613:
 					return 1500 + Misc.random(1250);
 					case 1615:
 					return 3000;
 					case 18:
 					return 500;
 					case 101:
 					return 60;
 					case 913:
 					case 912:
 					case 914:
 					return 750 + Misc.random(500);
 					case 1612:
 					return 250 + Misc.random(500);
 					case 1648:
 					return 250 + Misc.random(250);
 					case 90:
 					return 200;
 					case 82:
 					return 1000 + Misc.random(455);
 					case 52:
 					return 400 + Misc.random(200);
 					case 49:
 					return 1500 + Misc.random(2000);
 					case 1341:
 					return 1500 + Misc.random(500);
 					case 26:
 					return 500 + Misc.random(100);
 					case 20:
 					return 750 + Misc.random(100);
 					case 21: 
 					return 890 + Misc.random(125);
 					case 117:
 					return 500 + Misc.random(250);
 					case 2607:
 					return 500 + Misc.random(350);
 				}			
 			break;
 			case 11212:
 			return 10 + Misc.random(4);
 			case 565:
 			case 561:
 			return 10;
 			case 560:
 			case 563:
 			case 562:
 			return 15;
 			case 555:
 			case 554:
 			case 556:
 			case 557:
 			return 20;
 			case 892:
 			return 40;
 			case 886:
 			return 100;
 			case 6522:
 			return 6 + Misc.random(5);
 			
 		}
 	
 		return 1;
 	}
 	
 	/**
 	* Slayer Experience
 	**/	
 
 	public void appendSlayerExperience(int i) {
 		int npc = 0;
 		Client c = (Client)Server.playerHandler.players[npcs[i].killedBy];
 		if(c != null) {
 			if (c.slayerTask == npcs[i].npcType ){
 				c.taskAmount--;
 				c.getPA().addSkillXP(npcs[i].MaxHP * Config.SLAYER_EXPERIENCE, 18);
 				if (c.taskAmount <= 0 ) {
 				if (npcs[i].npcType == 1645 || npcs[i].npcType == 1591 || npcs[i].npcType == 1618 || npcs[i].npcType == 1643 || npcs[i].npcType == 941 || npcs[i].npcType == 119 || npcs[i].npcType == 82 || npcs[i].npcType == 52 || npcs[i].npcType == 1612 || npcs[i].npcType == 117 || npcs[i].npcType == 1265 || npcs[i].npcType == 112 || npcs[i].npcType == 125) {
 					c.getPA().addSkillXP((npcs[i].MaxHP * 10) * Config.SLAYER_EXPERIENCE, 18);
 					c.slayerPoints += 10;
 					c.slayerTask = -1;
 					c.sendMessage("You completed your MEDIUM slayer task. Please see a slayer master to get a new one.");
 					c.sendMessage("You receive 10 EPP.");
 				}
 				if (npcs[i].npcType == 1624 || npcs[i].npcType == 1610 || npcs[i].npcType == 1592 || npcs[i].npcType == 1613 || npcs[i].npcType == 1615 || npcs[i].npcType == 55 || npcs[i].npcType == 84 || npcs[i].npcType == 49 || npcs[i].npcType == 1618 || npcs[i].npcType == 941 || npcs[i].npcType == 82 || npcs[i].npcType == 2783 || npcs[i].npcType == 1341) {
 					c.getPA().addSkillXP((npcs[i].MaxHP * 12) * Config.SLAYER_EXPERIENCE, 18);
 					c.slayerPoints += 20;
 					c.slayerTask = -1;
 					c.sendMessage("You completed your HARD slayer task. Please see a slayer master to get a new one.");
 					c.sendMessage("You receive 15 EPP.");
 				}
 				if (npcs[i].npcType == 1648 || npcs[i].npcType == 117 || npcs[i].npcType == 1265 || npcs[i].npcType == 90 || npcs[i].npcType == 103 || npcs[i].npcType == 78 || npcs[i].npcType == 119 || npcs[i].npcType == 18 || npcs[i].npcType == 101 || npcs[i].npcType == 1265 || npcs[i].npcType == 181) {
 					c.getPA().addSkillXP((npcs[i].MaxHP * 8) * Config.SLAYER_EXPERIENCE, 18);
 					c.pkPoints += 5;
 					c.slayerTask = -1;
 					c.sendMessage("You completed your EASY slayer task. Please see a slayer master to get a new one.");
 					c.sendMessage("You receive 5 EPP");
 				}
 
 				}
 
 			}
 		}
 	}
 
 		
 		/**
 	* Npc names
 	**/
 	
 	public String getNpcName(int npcId) {
 		for (int i = 0; i < maxNPCs; i++) {
 			if (Server.npcHandler.NpcList[i] != null) {
 				if (Server.npcHandler.NpcList[i].npcId == npcId) {
 					return Server.npcHandler.NpcList[i].npcName;
 				}
 			}
 		}
 		return "-1";
 	}
 
 	
 	/**
 	 *	Resets players in combat
 	 */
 	
 	public void resetPlayersInCombat(int i) {
 		for (int j = 0; j < Server.playerHandler.players.length; j++) {
 			if (Server.playerHandler.players[j] != null)
 				if (Server.playerHandler.players[j].underAttackBy2 == i)
 					Server.playerHandler.players[j].underAttackBy2 = 0;
 		}
 	}
 	
 	
 	/**
 	* Npc Follow Player
 	**/
 	
 	public int GetMove(int Place1,int Place2) { 
 		if ((Place1 - Place2) == 0) {
             return 0;
 		} else if ((Place1 - Place2) < 0) {
 			return 1;
 		} else if ((Place1 - Place2) > 0) {
 			return -1;
 		}
         	return 0;
    	 }
 	
 	public boolean followPlayer(int i) {
 		switch (npcs[i].npcType) {
 			
 			case 2892:
 			case 2894:
 			return false;
 		}
 		return true;
 	}
 	
 	public void followPlayer(int i, int playerId) {
 		if (Server.playerHandler.players[playerId] == null) {
 			return;
 		}
 		if (Server.playerHandler.players[playerId].respawnTimer > 0) {
 			npcs[i].facePlayer(0);
 			npcs[i].randomWalk = true; 
 	      	npcs[i].underAttack = false;	
 			return;
 		}
 		
 		if (!followPlayer(i)) {
 			npcs[i].facePlayer(playerId);
 			return;
 		}
 		
 if(!goodDistance(npcs[i].getX(), npcs[i].getY(), Server.playerHandler.players[playerId].getX(), Server.playerHandler.players[playerId].getY(), 1) && npcs[i].npcType == 10127 && npcs[i].attackType == 0) {
 npcs[i].attackType = 2;
 return;
 }
 		
 		int playerX = Server.playerHandler.players[playerId].absX;
 		int playerY = Server.playerHandler.players[playerId].absY;
 		npcs[i].randomWalk = false;
 		if (goodDistance(npcs[i].getX(), npcs[i].getY(), playerX, playerY, distanceRequired(i)))
 			return;
 
 		
 
 		if((npcs[i].spawnedBy > 0) || ((npcs[i].absX < npcs[i].makeX + Config.NPC_FOLLOW_DISTANCE) && (npcs[i].absX > npcs[i].makeX - Config.NPC_FOLLOW_DISTANCE) && (npcs[i].absY < npcs[i].makeY + Config.NPC_FOLLOW_DISTANCE) && (npcs[i].absY > npcs[i].makeY - Config.NPC_FOLLOW_DISTANCE))) {
 			if(npcs[i].heightLevel == Server.playerHandler.players[playerId].heightLevel) {
 				if(Server.playerHandler.players[playerId] != null && npcs[i] != null) {
 					if(playerY < npcs[i].absY) {
 						npcs[i].moveX = GetMove(npcs[i].absX, playerX);
 						npcs[i].moveY = GetMove(npcs[i].absY, playerY);
 					} else if(playerY > npcs[i].absY) {
 						npcs[i].moveX = GetMove(npcs[i].absX, playerX);
 						npcs[i].moveY = GetMove(npcs[i].absY, playerY);
 					} else if(playerX < npcs[i].absX) {
 						npcs[i].moveX = GetMove(npcs[i].absX, playerX);
 						npcs[i].moveY = GetMove(npcs[i].absY, playerY);
 					} else if(playerX > npcs[i].absX)  {
 						npcs[i].moveX = GetMove(npcs[i].absX, playerX);
 						npcs[i].moveY = GetMove(npcs[i].absY, playerY);
 					} else if(playerX == npcs[i].absX || playerY == npcs[i].absY) {
 						int o = Misc.random(3);
 						switch(o) {
 							case 0:
 							npcs[i].moveX = GetMove(npcs[i].absX, playerX);
 							npcs[i].moveY = GetMove(npcs[i].absY, playerY+1);
 							break;
 							
 							case 1:
 							npcs[i].moveX = GetMove(npcs[i].absX, playerX);
 							npcs[i].moveY = GetMove(npcs[i].absY, playerY-1);
 							break;
 							
 							case 2:
 							npcs[i].moveX = GetMove(npcs[i].absX, playerX+1);
 							npcs[i].moveY = GetMove(npcs[i].absY, playerY);
 							break;
 							
 							case 3:
 							npcs[i].moveX = GetMove(npcs[i].absX, playerX-1);
 							npcs[i].moveY = GetMove(npcs[i].absY, playerY);
 							break;
 						}	
 					}
 					int x = (npcs[i].absX + npcs[i].moveX);
 					int y = (npcs[i].absY + npcs[i].moveY);
 					npcs[i].facePlayer(playerId);
 					if (checkClipping(i))
 						npcs[i].getNextNPCMovement(i);
 					else {
 						npcs[i].moveX = 0;
 						npcs[i].moveY = 0;
 					}
 					npcs[i].facePlayer(playerId);
 			      	npcs[i].updateRequired = true;
 				}	
 			}
 		} else {
 			npcs[i].facePlayer(0);
 			npcs[i].randomWalk = true; 
 		   	npcs[i].underAttack = false;	
 		}
 	}
 	
 public void follownpc(int i, int playerId) {
 		if (npcs[playerId] == null) {
 			return;
 		}
 
 		
 		if (!followPlayer(i)) {
 			npcs[i].facePlayer(playerId);
 			return;
 		}
 
 if(!goodDistance(npcs[i].getX(), npcs[i].getY(), npcs[playerId].getX(), npcs[playerId].getY(), 1) && npcs[i].npcType == 10127 && npcs[i].attackType == 0) {
 npcs[i].attackType = 2;
 return;
 }
 		
 
 		npcs[i].randomWalk = false;
 
 		if (goodDistance(npcs[i].getX(), npcs[i].getY(), npcs[playerId].absX, npcs[playerId].absY, distanceRequired(i)))
 			return;
 
 		
 
 		if((npcs[i].spawnedBy > 0) || ((npcs[i].absX < npcs[i].makeX + Config.NPC_FOLLOW_DISTANCE) && (npcs[i].absX > npcs[i].makeX - Config.NPC_FOLLOW_DISTANCE) && (npcs[i].absY < npcs[i].makeY + Config.NPC_FOLLOW_DISTANCE) && (npcs[i].absY > npcs[i].makeY - Config.NPC_FOLLOW_DISTANCE))) {
 			if(npcs[i].heightLevel == npcs[playerId].heightLevel) {
 				if(npcs[playerId] != null && npcs[i] != null) {
 					if(npcs[playerId].absY < npcs[i].absY) {
 						npcs[i].moveX = GetMove(npcs[i].absX, npcs[playerId].absX);
 						npcs[i].moveY = GetMove(npcs[i].absY, npcs[playerId].absY);
 					} else if(npcs[playerId].absY > npcs[i].absY) {
 						npcs[i].moveX = GetMove(npcs[i].absX, npcs[playerId].absX);
 						npcs[i].moveY = GetMove(npcs[i].absY, npcs[playerId].absY);
 					} else if(npcs[playerId].absX < npcs[i].absX) {
 						npcs[i].moveX = GetMove(npcs[i].absX, npcs[playerId].absX);
 						npcs[i].moveY = GetMove(npcs[i].absY, npcs[playerId].absY);
 					} else if(npcs[playerId].absX > npcs[i].absX)  {
 						npcs[i].moveX = GetMove(npcs[i].absX, npcs[playerId].absX);
 						npcs[i].moveY = GetMove(npcs[i].absY, npcs[playerId].absY);
 					} else if(npcs[playerId].absX == npcs[i].absX || npcs[playerId].absY == npcs[i].absY) {
 						int o = Misc.random(3);
 						switch(o) {
 							case 0:
 							npcs[i].moveX = GetMove(npcs[i].absX, npcs[playerId].absX);
 							npcs[i].moveY = GetMove(npcs[i].absY, npcs[playerId].absY+1);
 							break;
 							
 							case 1:
 							npcs[i].moveX = GetMove(npcs[i].absX, npcs[playerId].absX);
 							npcs[i].moveY = GetMove(npcs[i].absY, npcs[playerId].absY-1);
 							break;
 							
 							case 2:
 							npcs[i].moveX = GetMove(npcs[i].absX, npcs[playerId].absX+1);
 							npcs[i].moveY = GetMove(npcs[i].absY, npcs[playerId].absY);
 							break;
 							
 							case 3:
 							npcs[i].moveX = GetMove(npcs[i].absX, npcs[playerId].absX-1);
 							npcs[i].moveY = GetMove(npcs[i].absY, npcs[playerId].absY);
 							break;
 						}	
 					}
 					int x = (npcs[i].absX + npcs[i].moveX);
 					int y = (npcs[i].absY + npcs[i].moveY);
 					npcs[i].facePlayer(playerId);
 					if (checkClipping(i))
 						npcs[i].getNextNPCMovement(i);
 					else {
 						npcs[i].moveX = 0;
 						npcs[i].moveY = 0;
 					}
 					npcs[i].facePlayer(playerId);
 			      	npcs[i].updateRequired = true;
 				}	
 			}
 		} else {
 			npcs[i].facePlayer(0);
 			npcs[i].randomWalk = true; 
 		   	npcs[i].underAttack = false;	
 		}
 	}
 	
 	
 	public boolean checkClipping(int i) {
 		NPC npc = npcs[i];
 		int size = npcSize(i);
 		
 		for (int x = 0; x < size; x++) {
 			for (int y = 0; y < size; y++) {
 				if (!VirtualWorld.I(npc.heightLevel, npc.absX + x, npc.absY + y, npc.absX + npc.moveX, npc.absY + npc.moveY, 0))
 					return false;				
 			}
 		}
 		return true;
 	}
 
 	private Client v;
 	public NPCHandler(Client Client) {
 		this.v = Client;
 	}
 	
 	/**
 	* load spell
 	**/
 	public void loadSpell2(int i) {
 		Client c = (Client)Server.playerHandler.players[npcs[i].killedBy];
 		npcs[i].attackType = 3;
 		int random = Misc.random(3);
 		if (random == 0) {
 			npcs[i].projectileId = 393; //red
 			npcs[i].endGfx = 430;
 		} else if (random == 1) {
 			npcs[i].projectileId = 394; //green
 			npcs[i].endGfx = 429;
 		} else if (random == 2) {
 			npcs[i].projectileId = 395; //white
 			npcs[i].endGfx = 431;
 		} else if (random == 3) {
 			npcs[i].projectileId = 396; //blue
 			npcs[i].endGfx = 428;
 		}
 	}
 	
 	public void loadSpell(int i) {
 		Client c = (Client) Server.playerHandler.players[npcs[i].killerId];
 		switch(npcs[i].npcType) {
 			case 2892:
 			npcs[i].projectileId = 94;
 			npcs[i].attackType = 2;
 			npcs[i].endGfx = 95;
 			break;
 						//kalphite queen form 1
 			case 1158:
 				for (int j = 0; j < Server.playerHandler.players.length; j++) {
 					if (Server.playerHandler.players[j] != null) {
 				
 						int kq1 = 0;
 				if (goodDistance(npcs[i].absX, npcs[i].absY, c.absX, c.absY, 2))
 				kq1 = Misc.random(2);
 					else 
 						kq1 = Misc.random(1);
 				if (kq1 == 0) {					
 				npcs[i].projectileId = 280; //mage
 				npcs[i].endGfx = 281;
 				npcs[i].attackType = 2;
 			} else if (kq1 == 1) {
 				npcs[i].attackType = 1; // range
 				npcs[i].endGfx = 281;
 				npcs[i].projectileId = 473;
 			} else if (kq1 == 2) {
 				npcs[i].attackType = 0; // melee
 				npcs[i].projectileId = -1;
 			}
 					}
 				}
 					break;
 			
 					//kalphite queen form 2
 			case 1160:
 				for (int j = 0; j < Server.playerHandler.players.length; j++) {
 					if (Server.playerHandler.players[j] != null) {
 				
 						int kq1 = 0;
 				if (goodDistance(npcs[i].absX, npcs[i].absY, c.absX, c.absY, 2))
 				kq1 = Misc.random(2);
 					else 
 						kq1 = Misc.random(1);
 				if (kq1 == 0) {					
 				npcs[i].projectileId = 280; //mage
 				npcs[i].endGfx = 281;
 				npcs[i].attackType = 2;
 			} else if (kq1 == 1) {
 				npcs[i].attackType = 1; // range
 				npcs[i].endGfx = 281;
 				npcs[i].projectileId = 473;
 			} else if (kq1 == 2) {
 				npcs[i].attackType = 0; // melee
 				npcs[i].projectileId = -1;
 			}
 					}
 				}
 
 					break;
 			case 2894:
 			npcs[i].projectileId = 298;
 			npcs[i].attackType = 1;
 			break;
 case 6203:
 				random = Misc.random(2);
 				if (random == 0 || random == 1) {
 					npcs[i].attackType = 0;
 					npcs[i].projectileId = -1;
 				} else {
 					npcs[i].attackType = 2;
 					npcs[i].projectileId = 1211;
 				}
 			break;
 				case 5666:
 				random = Misc.random(1);
 				if (random == 0) {
 					npcs[i].attackType = 1;
 				} else {
 					npcs[i].attackType = 2;
 
 				}
 			break;
 			case 6206:				
 					npcs[i].attackType = 1;
 					npcs[i].projectileId = 1209;	
 			break;
 			case 6208:				
 					npcs[i].attackType = 2;
 					npcs[i].projectileId = 1213;	
 			break;
 			case 6256:				
 					npcs[i].attackType = 1;
 					npcs[i].projectileId = 16;	
 			break;
 			case 6220:				
 					npcs[i].attackType = 1;
 					npcs[i].projectileId = 17;	
 			break;
 			case 50:
 			int r5 = 0;
 			if (goodDistance(npcs[i].absX, npcs[i].absY, Server.playerHandler.players[npcs[i].killerId].absX, Server.playerHandler.players[npcs[i].killerId].absY, 2))
 				r5 = Misc.random(5);
 			else
 				r5 = Misc.random(3);
 			if (r5 == 0) {
 				npcs[i].projectileId = 393; //red
 				npcs[i].attackType = 3;
 			} else if (r5 == 1) {
 				npcs[i].projectileId = 394; //green
 				npcs[i].attackType = 2;
 				if(c.poisonDamage <= 0) {
 					c.getPA().appendPoison(8);
 				}
 			} else if (r5 == 2) {
 				npcs[i].projectileId = 395; //white
 				npcs[i].attackType = 2;
 				if(c.freezeTimer <= 0) {
 					c.freezeTimer = 19;
 					c.sendMessage("You have been Frozen!");
 				}
 			} else if (r5 == 3) {
 				npcs[i].projectileId = 396; //blue
 				npcs[i].attackType = 2;
 			} else if (r5 == 4) {
 				npcs[i].projectileId = -1; //melee
 				npcs[i].attackType = 0;	
 			} else if (r5 == 5) {
 				npcs[i].projectileId = -1; //melee
 				npcs[i].attackType = 0;	
 			}			
 			break;
 			case 53:
 			case 54:
 			case 55:
 			case 941:
 			case 1590:
 			case 1591:
 			case 1592:
 			int r6 = 0;	
 			if (goodDistance(npcs[i].absX, npcs[i].absY, Server.playerHandler.players[npcs[i].killerId].absX, Server.playerHandler.players[npcs[i].killerId].absY, 2))
 			r6 = Misc.random(2);
 			else
 			r6 = Misc.random(1);
 			if (r6 == 0) {
 				npcs[i].projectileId = 393; //red
 				npcs[i].attackType = 3;
 			} else if (r6 == 1) {
 				npcs[i].projectileId = 393; //red
 				npcs[i].attackType = 3;
 			} else if (r6 == 2) {
 				npcs[i].projectileId = -1; //melee
 				npcs[i].attackType = 0;	
 			}				
 			break;
 			//arma npcs
 			case 6227://kilisa
 				npcs[i].attackType = 0;
 			break;
 			case 6225://geerin
 			case 6233:
 			case 6230:
 				npcs[i].attackType = 1;
 				npcs[i].projectileId = 1190;
 			break;
 			case 6239:
 				npcs[i].attackType = 1;
 				npcs[i].projectileId = 1191;
 			break;
 			case 6232:
 				npcs[i].attackType = 1;
 				npcs[i].projectileId = 1191;
 			break;
 			case 6276:
 				npcs[i].attackType = 1;
 				npcs[i].projectileId = 1195;
 			break;
 			case 6223://skree
 				npcs[i].attackType = 2;
 				npcs[i].projectileId = 1199;
 			break;
 			case 6257://saradomin strike
 				npcs[i].attackType = 2;
 					npcs[i].endGfx = 76;
 			break;
 			case 6221://zamorak strike
 				npcs[i].attackType = 2;
 			npcs[i].endGfx = 78;
 			break;
 			case 6231://arma
 				npcs[i].attackType = 2;
 				npcs[i].projectileId = 1199;
 			break;
 			case 6222://kree
 				random = Misc.random(1);
 				npcs[i].attackType = 1 + random;
 				if (npcs[i].attackType == 1) {
 					npcs[i].projectileId = 1197;				
 				} else {
 					npcs[i].attackType = 2;
 					npcs[i].projectileId = 1198;
 				}	
 			break;
 			//sara npcs
 			case 6247: //sara
 				random = Misc.random(1);
 				if (random == 0) {
 					npcs[i].attackType = 2;
 					npcs[i].endGfx = 1224;
 					npcs[i].projectileId = -1;
 				} else if (random == 1)
 					npcs[i].attackType = 0;
 			break;
 			case 6248: //star
 				npcs[i].attackType = 0;
 			break;
 			case 6250: //growler
 				npcs[i].attackType = 2;
 				npcs[i].projectileId = 1203;
 			break;
 			case 6252: //bree
 				npcs[i].attackType = 1;
 				npcs[i].projectileId = 9;
 			break;
 			//bandos npcs
 			case 6260://bandos
 				random = Misc.random(2);
 				if (random == 0 || random == 1) {
 					npcs[i].attackType = 0;
 				} else {
 					npcs[i].attackType = 1;
 					//npcs[i].projectileId = 1200;
 				}
 			break;
 			case 9463:
 				random = Misc.random(2);
 				if (random == 0 || random == 1)
 					npcs[i].attackType = 0;
 				else {
 				c.freezeTimer = 20;
 				npcs[i].attackType = 2;
 				c.sendMessage("The Strykewyrm Used His Ice Bite And Froze You!");
 				}
 				break;
 			case 9467:
 				random = Misc.random(2);
 				if (random == 0 || random == 1)
 					npcs[i].attackType = 0;
 				else {
 				if(c.poisonDamage <= 0) {
 				c.getPA().appendPoison(12);
 				npcs[i].attackType = 2;
 				c.sendMessage("The Strykewyrm Used His Poison Bite, And Poisend You!");
 				}
 				}
 			case 9465:
 				random = Misc.random(2);
 				if (random == 0 || random == 1)
 					npcs[i].attackType = 0;
 				else {
 				c.playerLevel[5] -= (c.playerLevel[5] * .22);
 				npcs[i].attackType = 2;
 				c.sendMessage("The Strykewyrm Drained Your Prayer Points!");
 				c.getPA().refreshSkill(5);
 				}
 				break;
 			case 795:
 				random = Misc.random(2);
 				if (random == 0 || random == 1)
 					npcs[i].attackType = 0;
 				else {
 			c.gfx0(369);
 			npcs[i].forceChat("Muhahaha");
 					c.freezeTimer = 15;
 					npcs[i].attackType = 2;
 				}
 			break;
 			case 3495:
 				random = Misc.random(2);
 				if (random == 0 || random == 1) 
 					npcs[i].attackType = 0;
 				 else {
 			c.gfx0(369);
 			npcs[i].forceChat("Semolina-Go!");
 					c.freezeTimer = 10;
 					npcs[i].attackType = 2;
 				}
 			break;
 			case 3493:
 				random = Misc.random(2);
 				if (random == 0 || random == 1) {
 					npcs[i].attackType = 0;
 				npcs[i].projectileId = -1;
 			} else {
 				npcs[i].gfx100(129);
 				npcs[i].projectileId = 130;
 				npcs[i].endGfx = 131;
 					npcs[i].attackType = 2;
 				}
 			break;
 			case 3496:
 				random = Misc.random(2);
 				if (random == 0 || random == 1) {
 					npcs[i].attackType = 0;
 				npcs[i].projectileId = -1;
 			} else {
 			npcs[i].forceChat("Hssssssssssss");
 				npcs[i].gfx100(550);
 				npcs[i].projectileId = 551;
 				npcs[i].endGfx = 552;
 					npcs[i].attackType = 2;
 				}
 			break;
 			case 3491:
 				npcs[i].projectileId = 106;
 					npcs[i].attackType = 2;
 			break;
 			case 6261://strongstack
 				npcs[i].attackType = 0;
 			break;
 			case 6263://steelwill
 				npcs[i].attackType = 2;
 				npcs[i].projectileId = 1203;
 			break;
 			case 6265://grimspike
 				npcs[i].attackType = 1;
 				npcs[i].projectileId = 1206;
 			break;
 			case 2025:
 			npcs[i].attackType = 2;
 			int r = Misc.random(3);
 			if(r == 0) {
 				npcs[i].gfx100(158);
 				npcs[i].projectileId = 159;
 				npcs[i].endGfx = 160;
 			}
 			if(r == 1) {
 				npcs[i].gfx100(161);
 				npcs[i].projectileId = 162;
 				npcs[i].endGfx = 163;
 			}
 			if(r == 2) {
 				npcs[i].gfx100(164);
 				npcs[i].projectileId = 165;
 				npcs[i].endGfx = 166;
 			}
 			if(r == 3) {
 				npcs[i].gfx100(155);
 				npcs[i].projectileId = 156;
 			}
 			break;
 			case 2881://supreme
 				npcs[i].attackType = 1;
 				npcs[i].projectileId = 298;
 			break;
 			
 			case 2882://prime
 				npcs[i].attackType = 2;
 				npcs[i].projectileId = 162;
 				npcs[i].endGfx = 477;
 			break;
 			
 			case 2028:
 				npcs[i].attackType = 1;
 				npcs[i].projectileId = 27;
 			break;
 			
 			case 3200:
 			int r2 = Misc.random(5);
 			if (r2 == 0) {
 				npcs[i].attackType = 1;
 				npcs[i].gfx100(550);
 				npcs[i].projectileId = 551;
 				npcs[i].endGfx = 552;
 				c.getPA().chaosElementalEffect(c, 1);
 			} else if(r2 == 1) {
 				npcs[i].attackType = 2;
 				npcs[i].gfx100(553);
 				npcs[i].projectileId = 554;
 				npcs[i].endGfx = 555;
    				c.getPA().chaosElementalEffect(c, 0);
 			} else {
 				npcs[i].attackType = 0;
 				npcs[i].gfx100(556);
 				npcs[i].projectileId = 557;
 				npcs[i].endGfx = 558;
 			}
 			break;
 			case 2745:
 			int r3 = 0;
 			if (goodDistance(npcs[i].absX, npcs[i].absY, Server.playerHandler.players[npcs[i].spawnedBy].absX, Server.playerHandler.players[npcs[i].spawnedBy].absY, 1))
 				r3 = Misc.random(2);
 			else
 				r3 = Misc.random(1);
 			if (r3 == 0) {
 				npcs[i].attackType = 2;
 				npcs[i].endGfx = 157;
 				npcs[i].projectileId = 1627;
 			} else if (r3 == 1) {
 				npcs[i].attackType = 1;
 				npcs[i].endGfx = 451;
 				npcs[i].gfx100(1625);
 				npcs[i].projectileId = -1;
 			} else if (r3 == 2) {
 				npcs[i].attackType = 0;
 				npcs[i].projectileId = -1;
 			}			
 			break;
 			case 8133:
 			if (goodDistance(npcs[i].absX, npcs[i].absY, Server.playerHandler.players[npcs[i].killerId].absX, Server.playerHandler.players[npcs[i].killerId].absY, 3))
 				r3 = Misc.random(2);
 			else
 				r3 = Misc.random(1);
 			if (r3 == 0) {
 				npcs[i].attackType = 2;
 				npcs[i].endGfx = -1;
 				npcs[i].projectileId = 1828;
 			} else if (r3 == 1) {
 				npcs[i].attackType = 1;
 				npcs[i].endGfx = -1;
 				npcs[i].projectileId = 1839;
 			} else if (r3 == 2) {
 				npcs[i].attackType = 0;
 				npcs[i].gfx100(1834);
 				npcs[i].projectileId = -1;
 			}			
 			break;
 			case 3102:
 				npcs[i].attackType = 1;
 				npcs[i].projectileId = 1839;
 			break;
 			case 3103:
 				npcs[i].attackType = 2;
 				npcs[i].projectileId = 1828;
 			break;
 			case 8349: case 8350: case 8351:
 			if (goodDistance(npcs[i].absX, npcs[i].absY, Server.playerHandler.players[npcs[i].killerId].absX, Server.playerHandler.players[npcs[i].killerId].absY, 2))
 				r3 = Misc.random(2);
 			else
 				r3 = Misc.random(1);
 			if (r3 == 0) {
 				npcs[i].attackType = 2;
 				npcs[i].gfx100(1885);
 				npcs[i].projectileId = 1884;
 			} else if (r3 == 1) {
 				npcs[i].attackType = 1;
 				npcs[i].projectileId = 1889;
 			} else if (r3 == 2) {
 				npcs[i].attackType = 0;
 				npcs[i].gfx100(1886);
 				npcs[i].projectileId = -1;
 			}			
 			break;
 			case 2743:
 				npcs[i].attackType = 2;
 				npcs[i].projectileId = 445;
 				npcs[i].endGfx = 446;
 			break;
 			
 			case 2631:
 				npcs[i].attackType = 1;
 				npcs[i].projectileId = 443;
 			break;
 		}
 	}
 		
 	/**
 	* Distanced required to attack
 	**/	
 	public int distanceRequired(int i) {
 		switch(npcs[i].npcType) {
 			case 2025:
 			case 2028:
 			return 6;
 			case 53:
 			case 54:
 			case 55:
 			case 941:
 			case 1590:
 			case 1591:
 			case 1592:
 			return 2;
 			case 1158:
 			case 1160:
 				return 8;
 			case 6247:
 			return 2;
 			case 2881://dag kings
 			case 2882:
 			case 3200://chaos ele
 			case 2743:
 			case 2631:
 			case 2745:
 			case 50:
 			return 8;
 			case 6220:
 			case 6276:
 			case 6256:
 			case 6230:
 			case 6239: // Aviansie
 			case 6221:
 			case 6231:
 			case 6257:
 			case 6278:
 			case 8133:
 			case 6233:
 			case 6232:
 			return 5;
 			case 3102:
 			case 3103:
 			return 2;
 			case 8349: case 8350: case 8351:
 			return 1;
 			case 2883://rex
 			case 4291: // Cyclops
 			case 4292: // Ice cyclops
 			return 2;
 			case 6263:
 			case 6265:
 			case 6206:
 			case 6208:
 			case 6222:
 			case 6223:
 			case 6225:
 			case 6250:
 			case 6252:
 			return 15;
 			//things around dags
 			case 2892:
 			case 2894:
 			return 10;
 			default:
 			return 1;
 		}
 	}
 
 	
 	
 	public int followDistance(int i) {
 		switch (npcs[i].npcType) {
 			case 6260:
 			case 6261:
 			return 5;
 			case 6247:
 			case 6248:
 			case 6223:
 			case 6225:
 			case 6227:
 			case 6203:
 			case 6204:
 			case 6206:
 			case 6208:
 			case 6250:
 			case 6252:
 			case 6263:
 			case 8133:
 			case 6265:
 			return 15;
 			case 3247:
 			case 6270:
 			case 6219:
 			case 6255:
 			case 6229:
 			case 6277:
 			case 6233:
 			case 6232:
 			case 6218:
 			case 6269:
 			case 3248:
 			case 6212:
 			case 6220:
 			case 6276:
 			case 6256:
 			case 6230:
 			case 6239:
 			case 6221:
 			case 6231:
 			case 6257:
 			case 6278:
 			case 6272:
 			case 6274:
 			case 6254:
 			case 4291: // Cyclops
 			case 4292: // Ice cyclops
 			case 6258:
 			case 8349: case 8350: case 8351:
 			return 7;
 			case 50:
 			return 18;
 			case 2883:
 			return 4;
 			case 2881:
 			case 2882:
 			return 1;
 		
 		}
 		return 0;
 		
 	
 	}
 	
 	public int getProjectileSpeed(int i) {
 		switch(npcs[i].npcType) {
 			case 2881:
 			case 2882:
 			case 3200:
 			return 85;
 			
 			case 2745:
 			return 115;
 			case 1158:
 			case 1160:
 			return 90;
 			
 			case 50:
 			case 53:
 			case 54:
 			case 55:
 			case 941:
 			case 1590:
 			case 1591:
 			case 1592:
 			return 85;
 			
 			case 2025:
 			return 85;
 
 			case 3493:
 			return 85;
 			
 			case 2028:
 			return 80;
 			
 			default:
 			return 85;
 		}
 	}
 	
 	/**
 	*NPC Attacking Player
 	**/
 	
 	public void attackPlayer(Client c, int i) {
 		if(npcs[i] != null) {
 			if (npcs[i].isDead)
 				return;
 			if (!npcs[i].inMulti() && npcs[i].underAttackBy > 0 && npcs[i].underAttackBy != c.playerId) {
 				npcs[i].killerId = 0;
 				return;
 			}
 			if (!npcs[i].inMulti() && (c.underAttackBy > 0 || (c.underAttackBy2 > 0 && c.underAttackBy2 != i))) {
 				npcs[i].killerId = 0;
 				return;
 			}
 			if (npcs[i].heightLevel != c.heightLevel) {
 				npcs[i].killerId = 0;
 				return;
 			}
 if(!c.inSara() && npcs[i].npcType == 6247|| npcs[i].npcType == 6248 || npcs[i].npcType == 6250 || npcs[i].npcType == 6252)
 {
 npcs[i].killerId = 0;
 npcs[i].underAttack = false;
 npcs[i].IsAttackingPerson = false;
 return;
 }
 
 if(!c.inArma() && npcs[i].npcType == 6222|| npcs[i].npcType == 6223 || npcs[i].npcType == 6225 || npcs[i].npcType == 6227)
 {
 npcs[i].killerId = 0;
 npcs[i].underAttack = false;
 npcs[i].IsAttackingPerson = false;
 return;
 }
 
 if(!c.inBandos() && npcs[i].npcType == 6260 || npcs[i].npcType == 6261 || npcs[i].npcType == 6263 || npcs[i].npcType == 6265)
 {
 npcs[i].killerId = 0;
 npcs[i].underAttack = false;
 npcs[i].IsAttackingPerson = false;
 return;
 }
 if(!c.inZammy() && npcs[i].npcType == 6203 || npcs[i].npcType == 6204 || npcs[i].npcType == 6206 || npcs[i].npcType == 6208) {
 npcs[i].killerId = 0;
 npcs[i].underAttack = false;
 npcs[i].IsAttackingPerson = false;
 return;
 }
 		if (!goodDistance(npcs[i].getX(), npcs[i].getY(), c.getY(), c.getX(), 1) && npcs[i].npcType == 8133 && npcs[i].attackType == 0)
 {
 npcs[i].attackType = 1+Misc.random(1);
 			return;
 }
 
 
 
 		if (!goodDistance(npcs[i].getX(), npcs[i].getY(), c.getY(), c.getX(), 1) && npcs[i].npcType == 10127 && npcs[i].attackType == 0)
 {
 npcs[i].attackType = 2;
 			return;
 }
 
 
 		if (!goodDistance(npcs[i].getX(), npcs[i].getY(), c.getX(), c.getY(), 1) && npcs[i].npcType == 8349 && npcs[i].attackType == 0)
 {
 npcs[i].attackType = 1+Misc.random(1);
 			return;
 } 
 			npcs[i].facePlayer(c.playerId);
 			boolean special = false;//specialCase(c,i);
 			if(goodDistance(npcs[i].getX(), npcs[i].getY(), c.getX(), c.getY(), distanceRequired(i)) || special) {
 				if(c.respawnTimer <= 0) {
 					npcs[i].facePlayer(c.playerId);
 					npcs[i].attackTimer = getNpcDelay(i);
 					npcs[i].hitDelayTimer = getHitDelay(i);
 					npcs[i].attackType = 0;
 					if (special)
 						loadSpell2(i);
 				else
 						loadSpell(i);
 					if (npcs[i].attackType == 3)
 						npcs[i].hitDelayTimer += 2;
 					if (multiAttacks(i)) {
 						multiAttackGfx(i, npcs[i].projectileId);
 						startAnimation(getAttackEmote(i), i);
 						npcs[i].oldIndex = c.playerId;
 						return;
 					}
 					if(npcs[i].projectileId > 0) {
 						int nX = Server.npcHandler.npcs[i].getX() + offset(i);
 						int nY = Server.npcHandler.npcs[i].getY() + offset(i);
 						int pX = c.getX();
 						int pY = c.getY();
 						int offX = (nY - pY)* -1;
 						int offY = (nX - pX)* -1;
 						c.getPA().createPlayersProjectile(nX, nY, offX, offY, 50, getProjectileSpeed(i), npcs[i].projectileId, 43, 31, -c.getId() - 1, 65);
 					}
 					c.underAttackBy2 = i;
 					c.singleCombatDelay2 = System.currentTimeMillis();
 					npcs[i].oldIndex = c.playerId;
 					startAnimation(getAttackEmote(i), i);
 					c.getPA().removeAllWindows();
 				} 
 			}			
 		}
 	}
 	
 	public int offset(int i) {
 		switch (npcs[i].npcType) {
 			case 2881:
 			case 2882:
 			return 1;
 			case 2745:
 			case 8349: case 8350: case 8351:
 			case 2743:
 			case 8133:
 			case 50:
 			return 1;	
 case 1158:
 			case 1160:
 				return 2;			
 		}
 		return 0;
 	}
 	
 	public boolean specialCase(Client c, int i) { //responsible for npcs that much 
 		if (goodDistance(npcs[i].getX(), npcs[i].getY(), c.getX(), c.getY(), 8) && !goodDistance(npcs[i].getX(), npcs[i].getY(), c.getX(), c.getY(), distanceRequired(i)))
 			return true;
 		return false;
 	}
 	
 	public boolean retaliates(int npcType) {
 		return npcType < 6142 || npcType > 6145 && !(npcType >= 2440 && npcType <= 2446);
 	}
 	
 	public void applyDamage(int i) {
 		if(npcs[i] != null) {
 			if(Server.playerHandler.players[npcs[i].oldIndex] == null) {
 				return;
 			}
 			if (npcs[i].isDead)
 				return;
 			Client c = (Client) Server.playerHandler.players[npcs[i].oldIndex];
 			if (multiAttacks(i)) {
 				multiAttackDamage(i);
 				return;
 			}
 			if (c.playerIndex <= 0 && c.npcIndex <= 0)
 				if (c.autoRet == 1)
 					c.npcIndex = i;
 			if(c.attackTimer <= 3 || c.attackTimer == 0 && c.npcIndex == 0 && c.oldNpcIndex == 0) {
 				c.startAnimation(c.getCombat().getBlockEmote());
 			}
 			if(c.respawnTimer <= 0) {	
 				int damage = 0;
 				if(npcs[i].attackType == 0) {
 					damage = Misc.random(npcs[i].maxHit);
 					if (10 + Misc.random(c.getCombat().calculateMeleeDefence()) > Misc.random(Server.npcHandler.npcs[i].attack)) {
 						damage = 0;
 					}				
 					if(c.prayerActive[18] || c.curseActive[9]) { // protect from melee
 						damage = 0;
 					}	
 					if(c.SolProtect >= 1) { // protect from melee
 						damage = (int)damage / 2;
 					}			
 					if (c.playerEquipment[c.playerShield] == 13740) {
 						damage = (int)damage * 70 / 100;
 					}
 					if (c.playerEquipment[c.playerShield] == 13742) {
 					if(Misc.random(4) == 3) {
 					damage = (int)damage * 65 / 100;
 					}
 					}	
 					if (c.playerLevel[3] - damage < 0) { 
 						damage = c.playerLevel[3];
 					}
 				}
 				
 				if(npcs[i].attackType == 1) { // range
 					damage = Misc.random(npcs[i].maxHit);
 					if (10 + Misc.random(c.getCombat().calculateRangeDefence()) > Misc.random(Server.npcHandler.npcs[i].attack)) {
 						damage = 0;
 					}					
 					if(c.prayerActive[17] || c.curseActive[8]) { // protect from range
 						damage = 0;
 					}		
 					if(c.SolProtect >= 1) { // protect from melee
 						damage = (int)damage / 2;
 					}	
 					if (c.playerEquipment[c.playerShield] == 13740) {
 						damage = (int)damage * 70 / 100;
 					}
 					if (c.playerEquipment[c.playerShield] == 13742) {
 					if(Misc.random(4) == 3) {
 					damage = (int)damage * 65 / 100;
 					}
 					}
 					if (c.playerLevel[3] - damage < 0) { 
 						damage = c.playerLevel[3];
 					}
 				}
 				
 				if(npcs[i].attackType == 2) { // magic
 					damage = Misc.random(npcs[i].maxHit);
 					boolean magicFailed = false;
 					if (10 + Misc.random(c.getCombat().mageDef()) > Misc.random(Server.npcHandler.npcs[i].attack)) {
 						damage = 0;
 						magicFailed = true;
 					}				
 					if(c.prayerActive[16] || c.curseActive[7]) { // protect from magic
 						damage = 0;
 						magicFailed = true;
 					}		
 					if(c.SolProtect >= 1) { // protect from melee
 						damage = (int)damage / 2;
 					}		
 					if (c.playerEquipment[c.playerShield] == 13740) {
 						damage = (int)damage * 70 / 100;
 					}
 					if (c.playerEquipment[c.playerShield] == 13742) {
 					if(Misc.random(4) == 3) {
 					damage = (int)damage * 65 / 100;
 					}
 					}
 					if (c.playerLevel[3] - damage < 0) { 
 						damage = c.playerLevel[3];
 					}
 					if (damage == 0) { 
 						c.gfx100(85);
 					}
 					if(npcs[i].endGfx > 0 && (!magicFailed )) {
 					c.gfx100(npcs[i].endGfx);
 					} else {
 						//c.gfx100(85);
 					}
 				}
 				
 if (npcs[i].attackType == 3) { //fire breath
 					int anti = c.getPA().antiFire();
 					if (anti == 0) {
 						damage = Misc.random(30) + 10;
 						c.sendMessage("You are badly burnt by the dragon fire!");
 						c.getCombat().addCharge(c);
 				} else if (anti == 1)					
 					damage = Misc.random(12);
 					
 					else if (anti == 2)
 						damage = Misc.random(6);
 					
 					if (c.playerLevel[3] - damage < 0)
 						damage = c.playerLevel[3];
 					//c.gfx100(npcs[i].endGfx);
 				}
 				handleSpecialEffects(c, i, damage);
 				if (c.vengOn && damage > 0) {
 					c.getCombat().appendVengeanceNPC(i, damage);
 				}
 				c.logoutDelay = System.currentTimeMillis(); // logout delay
 				//c.setHitDiff(damage);
 				c.handleHitMask(damage);
 				c.playerLevel[3] -= damage;
 				c.getPA().refreshSkill(3);
 				c.updateRequired = true;
 				//c.setHitUpdateRequired(true);	
 			}
 		}
 	}
 	
 	public void handleSpecialEffects(Client c, int i, int damage) {
 		if (npcs[i].npcType == 2892 || npcs[i].npcType == 2894) {
 			if (damage > 0) {
 				if (c != null) {
 					if (c.playerLevel[5] > 0) {
 						c.playerLevel[5]--;
 						c.getPA().refreshSkill(5);
 						c.getPA().appendPoison(12);
 					}
 				}			
 			}	
 		}
 	
 	}
 		
 		
 
 	public void startAnimation(int animId, int i) {
 		npcs[i].animNumber = animId;
 		npcs[i].animUpdateRequired = true;
 		npcs[i].updateRequired = true;
 	}
 	
 	public boolean goodDistance(int objectX, int objectY, int playerX, int playerY, int distance) {
 		for (int i = 0; i <= distance; i++) {
 		  for (int j = 0; j <= distance; j++) {
 			if ((objectX + i) == playerX && ((objectY + j) == playerY || (objectY - j) == playerY || objectY == playerY)) {
 				return true;
 			} else if ((objectX - i) == playerX && ((objectY + j) == playerY || (objectY - j) == playerY || objectY == playerY)) {
 				return true;
 			} else if (objectX == playerX && ((objectY + j) == playerY || (objectY - j) == playerY || objectY == playerY)) {
 				return true;
 			}
 		  }
 		}
 		return false;
 	}
 	
       
 	public int getMaxHit(int i) {
 		switch (npcs[i].npcType) {
 			case 6222:
 				if (npcs[i].attackType == 2)
 					return 28;
 				else
 					return 68;
 			case 8133:
 				if (npcs[i].attackType == 0)
 					return 48;
 				else
 				if (npcs[i].attackType == 1)
 					return 48;
 				else
 				if (npcs[i].attackType == 2)
 					return 60;
 			case 6203:
 				if (npcs[i].attackType == 0)
 					return 40;
 				else
 					return 35;
 			case 8349: case 8350: case 8351:
 				if (npcs[i].attackType == 0)
 					return 20;
 				else
 					return 27;
 			case 6247:
 				return 31;
 			case 6260:
 				return 36;
 		}
 		return 1;
 	}
 	
 	
 	public boolean loadAutoSpawn(String FileName) {
 		String line = "";
 		String token = "";
 		String token2 = "";
 		String token2_2 = "";
 		String[] token3 = new String[10];
 		boolean EndOfFile = false;
 		int ReadMode = 0;
 		BufferedReader characterfile = null;
 		try {
 			characterfile = new BufferedReader(new FileReader("./"+FileName));
 		} catch(FileNotFoundException fileex) {
 			Misc.println(FileName+": file not found.");
 			return false;
 		}
 		try {
 			line = characterfile.readLine();
 		} catch(IOException ioexception) {
 			Misc.println(FileName+": error loading file.");
 			return false;
 		}
 		while(EndOfFile == false && line != null) {
 			line = line.trim();
 			int spot = line.indexOf("=");
 			if (spot > -1) {
 				token = line.substring(0, spot);
 				token = token.trim();
 				token2 = line.substring(spot + 1);
 				token2 = token2.trim();
 				token2_2 = token2.replaceAll("\t\t", "\t");
 				token2_2 = token2_2.replaceAll("\t\t", "\t");
 				token2_2 = token2_2.replaceAll("\t\t", "\t");
 				token2_2 = token2_2.replaceAll("\t\t", "\t");
 				token2_2 = token2_2.replaceAll("\t\t", "\t");
 				token3 = token2_2.split("\t");
 				if (token.equals("spawn")) {
 					newNPC(Integer.parseInt(token3[0]), Integer.parseInt(token3[1]), Integer.parseInt(token3[2]), Integer.parseInt(token3[3]), Integer.parseInt(token3[4]), getNpcListHP(Integer.parseInt(token3[0])), Integer.parseInt(token3[5]), Integer.parseInt(token3[6]), Integer.parseInt(token3[7]));
 				
 				}
 			} else {
 				if (line.equals("[ENDOFSPAWNLIST]")) {
 					try { characterfile.close(); } catch(IOException ioexception) { }
 					return true;
 				}
 			}
 			try {
 				line = characterfile.readLine();
 			} catch(IOException ioexception1) { EndOfFile = true; }
 		}
 		try { characterfile.close(); } catch(IOException ioexception) { }
 		return false;
 	}
 
 	public int getNpcListHP(int npcId) {
 		for (int i = 0; i < maxListedNPCs; i++) {
 			if (NpcList[i] != null) {
 				if (NpcList[i].npcId == npcId) {
 					return NpcList[i].npcHealth;
 				}
 			}
 		}
 		return 0;
 	}
 	
 	public String getNpcListName(int npcId) {
 		for (int i = 0; i < maxListedNPCs; i++) {
 			if (NpcList[i] != null) {
 				if (NpcList[i].npcId == npcId) {
 					return NpcList[i].npcName;
 				}
 			}
 		}
 		return "nothing";
 	}
 
 	public boolean loadNPCList(String FileName) {
 		String line = "";
 		String token = "";
 		String token2 = "";
 		String token2_2 = "";
 		String[] token3 = new String[10];
 		boolean EndOfFile = false;
 		int ReadMode = 0;
 		BufferedReader characterfile = null;
 		try {
 			characterfile = new BufferedReader(new FileReader("./"+FileName));
 		} catch(FileNotFoundException fileex) {
 			Misc.println(FileName+": file not found.");
 			return false;
 		}
 		try {
 			line = characterfile.readLine();
 		} catch(IOException ioexception) {
 			Misc.println(FileName+": error loading file.");
 			return false;
 		}
 		while(EndOfFile == false && line != null) {
 			line = line.trim();
 			int spot = line.indexOf("=");
 			if (spot > -1) {
 				token = line.substring(0, spot);
 				token = token.trim();
 				token2 = line.substring(spot + 1);
 				token2 = token2.trim();
 				token2_2 = token2.replaceAll("\t\t", "\t");
 				token2_2 = token2_2.replaceAll("\t\t", "\t");
 				token2_2 = token2_2.replaceAll("\t\t", "\t");
 				token2_2 = token2_2.replaceAll("\t\t", "\t");
 				token2_2 = token2_2.replaceAll("\t\t", "\t");
 				token3 = token2_2.split("\t");
 				if (token.equals("npc")) {
 					newNPCList(Integer.parseInt(token3[0]), token3[1], Integer.parseInt(token3[2]), Integer.parseInt(token3[3]));
 				}
 			} else {
 				if (line.equals("[ENDOFNPCLIST]")) {
 					try { characterfile.close(); } catch(IOException ioexception) { }
 					return true;
 				}
 			}
 			try {
 				line = characterfile.readLine();
 			} catch(IOException ioexception1) { EndOfFile = true; }
 		}
 		try { characterfile.close(); } catch(IOException ioexception) { }
 		return false;
 	}
 	
 
 }
