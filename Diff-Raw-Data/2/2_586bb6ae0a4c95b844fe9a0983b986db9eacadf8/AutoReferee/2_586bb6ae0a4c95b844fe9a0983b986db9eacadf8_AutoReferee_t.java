 //START CODE
 package net.minecraft.src;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 import org.apache.commons.lang3.StringUtils;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 public class AutoReferee {
 	public static final String CHANNEL = "autoref:referee";
 	public static final String DELIMITER = "\\|";
 	public static final char DELIMITER_SEND = '|';
 	public static final String PLUGIN_CHANNEL_ENC = "UTF-8";
 	
 	public static final int MAX_NUMBER_OF_PLAYERS_ON_SCREEN = 4;
 
 	public static final int AUTOREFEREE_ICON_NUMBER_IN_WIDTH = 4;
 	public static final int AUTOREFEREE_ICON_NUMBER_IN_HEIGHT = 4;
 	public static final int AUTOREFEREE_ICON_TEXTURE_SIZE = 4;
 	public static final int AUTOREFEREE_WINNERS_ICON = 0;
 	public static final int AUTOREFEREE_SKULL_ICON = 1;
 	public static final int AUTOREFEREE_ARROW_ICON = 2;
 	public static final int AUTOREFEREE_KILL_STREAK_ICON = 4;
 	public static final int AUTOREFEREE_DOMINATION_ICON = 5;
 	public static final int AUTOREFEREE_ICON_SIZE = 13;
 
 	public static final int PLAYER_NAMETAG_HEARTS_X_OFFSET = -40;
 	public static final int PLAYER_NAMETAG_HEARTS_Y_OFFSET = -12;
 	public static final int PLAYER_NAMETAG_ARMOR_X_OFFSET = PLAYER_NAMETAG_HEARTS_X_OFFSET;
 	public static final int PLAYER_NAMETAG_ARMOR_Y_OFFSET = -22;
 	public static final int PLAYER_NAMETAG_WOOL_X_OFFSET = -20;
 	public static final int PLAYER_NAMETAG_WOOL_Y_OFFSET = -42;
 	
 	public static final ResourceLocation GUI_ICONS = new ResourceLocation("textures/gui/icons.png");
 	public static final ResourceLocation AUTOREFEREE_ICONS = new ResourceLocation("textures/AutoReferee.png");
 
 	// public static final int LIST_DISPLAY_TICKS = 50;
 	public static final int MESSAGE_DISPLAY_TICKS = 60;
 	public static final int TEAM_LIST_CYCLE = 150;
 
 	private static AutoReferee instance = new AutoReferee();
 	private RenderItem renderItem;
 	private Minecraft mc;
 	private NetClientHandler netClientHandler;
 
 	public HashMap<String, AutoRefereePlayer> players = new HashMap<String, AutoRefereePlayer>();
 	public HashMap<String, AutoRefereeTeam> teams = new HashMap<String, AutoRefereeTeam>();
 
 	public ArrayList<AutoRefereeMessage> messages = new ArrayList<AutoRefereeMessage>();
 	public AutoRefereeMessage lastMessage = null;
 
 	// match fields
 	private int lastTick;
 	public int tickInit = 0;
 	public boolean startOfMatch = false; // is true for a short amount of time after the match|init message.
 	public boolean gameRunning;
 	public boolean countingDown;
 	private Calendar countdownEnd;
 	private Calendar matchStart;
 	private String mapName;
 	private AutoRefereeTeam winners;
 	private int lastTickPlayerList;
 	private int lastTickTeamList;
 	private String gameType;
 	public boolean nightVision;
 	public boolean swapTeams;
 	public boolean registeredChannel;
 	private List<AutoRefereePlayer> closestPlayers;
 	private int closestPlayersUpdateTick;
 	private boolean ffa;
 
 	public AutoReferee() {
 		renderItem = new RenderItem();
 		resetValues();
 	}
 
 	public void resetValues() {
 		this.mapName = "";
 		this.gameRunning = false;
 		this.players.clear();
 		this.teams.clear();
 		this.countingDown = false;
 		this.winners = null;
 		this.lastTickPlayerList = 0;
 		this.lastTickTeamList = 0;
 		this.messages.clear();
 		this.lastMessage = null;
 		this.gameType = "RFW";
 		this.nightVision = true;
 		this.swapTeams = false;
 		this.registeredChannel = false;
 		this.closestPlayers = null;
 		this.ffa = false;
 		this.closestPlayersUpdateTick = 0;
 	}
 
 	public static AutoReferee get() {
 		return instance;
 	}
 
 	public EntityPlayer getPlayer() {
 		return (EntityPlayer) this.mc.thePlayer;
 	}
 
 	public void set(Minecraft mc, NetClientHandler netClientHandler) {
 		this.mc = mc;
 		this.netClientHandler = netClientHandler;
 	}
 
 	public void registerAutoRefereeChannel() {
 		Packet250CustomPayload p = new Packet250CustomPayload();
 		p.channel = "REGISTER";
 		p.data = CHANNEL.getBytes(Charset.forName("UTF-8"));
 		p.length = p.data.length;
 		if (this.netClientHandler != null)
 			this.netClientHandler.addToSendQueue(p);
 
 		p = new Packet250CustomPayload();
 		p.channel = CHANNEL;
 		p.data = "REGISTER".getBytes(Charset.forName("UTF-8"));
 		p.length = p.data.length;
 		if (this.netClientHandler != null)
 			this.netClientHandler.addToSendQueue(p);
 	}
 
 	public void handleCustomPayload(Packet250CustomPayload customPayloadPackage) {
 		String s = new String(customPayloadPackage.data, Charset.forName(PLUGIN_CHANNEL_ENC));
 		Logger.getLogger("Minecraft").info("Received package: '" + s + "'.");
 		String[] command = s.split(DELIMITER);
 		if ("player".equals(command[0])) {
 			// handle player events
 			String playerName = command[1];
 			if (!players.containsKey(playerName))
 				return;
 
 			if ("goal".equals(command[2])) {
 				// player object events
 				String[] blockInfo = (command[3].substring(1)).split(",");
 				int blockId = Integer.parseInt(blockInfo[0]);
 				int blockData = 0;
 				if (blockInfo.length >= 2)
 					blockData = Integer.parseInt(blockInfo[1]);
 
 				if (command[3].startsWith("+")) {
 					// player object retrieval
 					retrieveObjectiveForPlayer(playerName, blockId, blockData);
 				} else if (command[3].startsWith("-")) {
 					// player object loss
 					loseObjectiveForPlayer(playerName, blockId, blockData);
 				}
 			} else if ("hp".equals(command[2])) {
 				this.changeHealthOfPlayer(playerName, Integer.parseInt(command[3]));
 			} else if ("armor".equals(command[2])) {
 				this.changeArmorOfPlayer(playerName, Integer.parseInt(command[3]));
 			} else if ("kills".equals(command[2])) {
 				this.changeKillsOfPlayer(playerName, Integer.parseInt(command[3]));
 			} else if ("deaths".equals(command[2])) {
 				this.changeDeathsOfPlayer(playerName, Integer.parseInt(command[3]));
 			} else if ("accuracy".equals(command[2])) {
 				this.changeAccuracyOfPlayer(playerName, Integer.parseInt(command[3]));
 			} else if ("streak".equals(command[2])) {
 				this.changeKillStreakOfPlayer(playerName, Integer.parseInt(command[3]));
 			} else if ("dominate".equals(command[2])) {
 				this.addDominationOfPlayer(playerName, command[3]);
 			} else if ("revenge".equals(command[2])) {
 				this.addRevengeOfPlayer(playerName, command[3]);
 			} else if ("cape".equals(command[2])) {
 				if (command.length >= 4 && command[3] != null)
 					this.setCapeUrlForPlayer(playerName, command[3]);
 				else
 					this.removeCapeUrlForPlayer(playerName);
 			} else if ("login".equals(command[2])) {
 				this.setLoggedIn(playerName, true);
 			} else if ("logout".equals(command[2])) {
 				this.setLoggedIn(playerName, false);
 			} else if ("itemcount".equals(command[2])) {
 				String[] blockInfo = (command[3].substring(0)).split(",");
 				int blockId = Integer.parseInt(blockInfo[0]);
 				int blockData = 0;
 				if (blockInfo.length >= 2)
 					blockData = Integer.parseInt(blockInfo[1]);
 				this.changeItemAmountOfPlayer(playerName, blockId, blockData, Integer.parseInt(command[4]));
 			} else if ("dimension".equals(command[2])) {
 				this.changeDimensionOfPlayer(playerName, command[3]);
 			}
 		} else if ("team".equals(command[0])) {
 			// handle team events
 			String teamName = command[1];
 			if ("init".equals(command[2]) && !teams.containsKey(teamName))
 				addTeam(teamName);
 			else if ("destroy".equals(command[2]))
 				removeTeam(teamName);
 			else if ("name".equals(command[2])) {
 				// team name event
 				changeTeamName(teamName, command[3]);
 			} else if ("color".equals(command[2])) {
 				// team color event
 				changeTeamColor(teamName, command[3]);
 			} else if ("player".equals(command[2])) {
 				// team player event
 				String playerName = command[3].substring(1);
 				if (command[3].startsWith("+")) {
 					// team player set event
 					this.changeTeamOfPlayer(playerName, command[1]);
 				} else if (command[3].startsWith("-")) {
 					// team player remove event
 					this.removePlayerFromTeam(playerName, command[1]);
 				}
 			} else if ("goal".equals(command[2])) {
 				// team object events
				if("survive".equalsIgnoreCase(command[3])){
 					//TODO add survive goal stuff
 				}else if("core".equalsIgnoreCase(command[3])){
 					//TODO add core goal stuff
 				}else if("time".equalsIgnoreCase(command[3])){
 					//TODO add time goal stuff
 				}else{//else it's just a blockid and blockdata.
 					String[] blockInfo = (command[3].substring(1)).split(",");
 					int blockId = Integer.parseInt(blockInfo[0]);
 					int blockData = 0;
 					if(blockInfo.length >= 2)
 						blockData = Integer.parseInt(blockInfo[1]);
 					
 					if (command[3].startsWith("+")) {
 						// player object retrieval
 						addObjectiveForTeam(teamName, blockId, blockData);
 					} else if (command[3].startsWith("-")) {
 						// player object loss
 						removeObjectiveForTeam(teamName, blockId, blockData);
 					}
 				}
 			} else if ("state".equals(command[2])) {
 				// team objective state event
 				AutoRefereeTeam at = teams.get(teamName);
 				String[] blockInfo = (command[3]).split(",");
 				int blockId = Integer.parseInt(blockInfo[0]);
 				int blockData = 0;
 				if(blockInfo.length >= 2)
 					blockData = Integer.parseInt(blockInfo[1]);
 				if (at != null) {
 					AutoRefereeObjective obj = at.getObjective(blockId, blockData);
 					if (obj != null) {
 						AutoRefereeObjectiveStatus oldStatus = obj.getStatus();
 						if ("none".equalsIgnoreCase(command[4]))
 							obj.setStatus(AutoRefereeObjectiveStatus.FLEECY_BOX);
 						else if ("found".equalsIgnoreCase(command[4]))
 							obj.setStatus(AutoRefereeObjectiveStatus.SAFE);
 						else if ("carry".equalsIgnoreCase(command[4]))
 							obj.setStatus(AutoRefereeObjectiveStatus.PLAYER);
 						else if ("target".equalsIgnoreCase(command[4]))
 							obj.setStatus(AutoRefereeObjectiveStatus.VICTORY_MONUMENT);
 						if (oldStatus == AutoRefereeObjectiveStatus.FLEECY_BOX && obj.getStatus() != AutoRefereeObjectiveStatus.FLEECY_BOX && !this.startOfMatch)
 							// not start of match because it means that the message is sent within 1 second after the match|init message.
 							this.mc.sndManager.playSoundFX("portal.travel", 1.0F, 1.0F);
 						// TODO remove
 						// addMessage("Caske33 is blowing a cannon", "authorblues");
 					}
 				}
 			}
 		} else if ("match".equals(command[0])) {
 			if ("map".equals(command[2])) {
 				this.mapName = command[3];
 			} else if ("init".equals(command[2])) {
 				this.resetValues();
 				this.tickInit = -1;
 				this.startOfMatch = true;
 				this.registeredChannel = true;
 			} else if ("time".equals(command[2])) {
 				this.setTime(command[3]);
 			} else if ("countdown".equals(command[2])) {
 				this.setCountdown(Integer.parseInt(command[3]));
 			} else if ("start".equals(command[2])) {
 				this.startMatch();
 			} else if ("end".equals(command[2])) {
 				this.endMatch(command[3]);
 			} else if ("champions".equals(command[2])) {
 				this.setChampions(command[3]);
 			} else if ("swap".equals(command[2])) {
 				swapTeams = !swapTeams;
 			} else if ("nightvis".equals(command[2])){
 				if(command.length >= 4){
 					if("1".equalsIgnoreCase(command[3]))
 						nightVision = true;
 					else if("0".equalsIgnoreCase(command[3]))
 						nightVision = false;
 				} else {
 					nightVision = !nightVision;
 				}
 			} else if ("gametype".equals(command[2])) {
 				gameType = command[3].toUpperCase();
 			} else if("gameplay".equals(command[2])){
 				if("FFA".equals(command[3])){
 					if(command.length >= 4){
 						this.ffa = (command[4] == "on");
 					}else
 						this.ffa = !ffa;
 				}else if("respawn".equals(command[3])){
 					//TODO add respawn gameplay behaviour.
 				}
 			}
 		} else if ("plugin".equals(command[0])) {
 			if ("ucp".equals(command[2])) {
 				updateClosestPlayers();
 			}
 		}
 	}
 	
 	public void messageServer(String ...parts) {
 		String msg = StringUtils.join(parts, DELIMITER_SEND);
 		Packet250CustomPayload packet;
 		try {
 			packet = new Packet250CustomPayload(CHANNEL, msg.getBytes(PLUGIN_CHANNEL_ENC));
 			this.netClientHandler.addToSendQueue(packet);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public Minecraft getMinecraft(){
 		return mc;
 	}
 
 	public boolean hasMessage(int tick) {
 		if (lastMessage != null && lastMessage.tick(tick) != 0)
 			return true;
 		else if (lastMessage != null)
 			lastMessage = null;
 		if (messages.size() > 0) {
 			lastMessage = messages.get(0);
 			messages.remove(0);
 			return true;
 		}
 		return false;
 	}
 
 	public void addMessage(String message, String author) {
 		AutoRefereeMessage am = new AutoRefereeMessage(message, author);
 		messages.add(am);
 	}
 
 	public boolean showPlayerList(int tick) {
 		if (this.mc.gameSettings.keyBindPlayerList.pressed) {
 			/*
 			 * if(lastTickPlayerList == 0) lastTickPlayerList = tick;
 			 */
 			return true;
 		}/*
 		 * if(lastTickPlayerList != 0){ if((tick - lastTickPlayerList) <= AutoReferee.LIST_DISPLAY_TICKS) return true; lastTickPlayerList = 0; }
 		 */
 		return false;
 	}
 
 	public boolean showTeamList(int tick) {
 		if (showPlayerList(tick))
 			return false;
 		if (this.mc.gameSettings.keyBindDrop.pressed) {
 			/*
 			 * if(lastTickTeamList == 0) lastTickTeamList = tick;
 			 */
 			return true;
 		} /*
 		 * if(lastTickTeamList != 0){ if((tick - lastTickTeamList) <= AutoReferee.LIST_DISPLAY_TICKS) return true; lastTickTeamList = 0; }
 		 */
 		return false;
 	}
 
 	public void setCountdown(int seconds) {
 		this.countingDown = (seconds != 0);
 		this.countdownEnd = Calendar.getInstance();
 		this.countdownEnd.add(Calendar.SECOND, seconds+1);
 	}
 	
 	public String getTime(){
 		if(!gameRunning && !countingDown)
 			return "00:00:00";
 		int seconds = 0;
 		if(gameRunning){
 			Calendar now = Calendar.getInstance();
 			seconds = (int)((now.getTimeInMillis() - matchStart.getTimeInMillis())/1000);
 		} else{
 			Calendar now = Calendar.getInstance();
 			seconds = (int)(( countdownEnd.getTimeInMillis() - now.getTimeInMillis())/1000);
 			if(seconds == 0)
 				countingDown = false;
 		}
 		int numberOfSeconds = seconds % 60;
 		int numberOfMinutes = (seconds / 60) % 60;
 		int numberOfHours = seconds / 60 / 60;
 		String s = "";
 		s += (numberOfHours >= 10) ? "" : "0";
 		s += numberOfHours;
 		s += ":";
 		s += (numberOfMinutes >= 10) ? "" : "0";
 		s += numberOfMinutes;
 		s += ":";
 		s += (numberOfSeconds >= 10) ? "" : "0";
 		s += numberOfSeconds;
 		return s;
 	}
 	
 	public void startMatch() {
 		this.countingDown = false;
 		this.gameRunning = true;
 		this.matchStart = Calendar.getInstance();
 	}
 
 	public void endMatch(String teamNameWinners) {
 		this.gameRunning = false;
 		setChampions(teamNameWinners);
 	}
 
 	public void setChampions(String teamNameWinners) {
 		if (teams.containsKey(teamNameWinners))
 			this.winners = teams.get(teamNameWinners);
 	}
 
 	public AutoRefereeTeam getWinners() {
 		return winners;
 	}
 
 	public void setTime(String timestamp) {
 		String[] times;
 		times = timestamp.split(",");
 		if(timestamp.contains(":"))
 			times = timestamp.split(":");
 		int numberOfHours = Integer.parseInt(times[0]);
 		int numberOfMinutes = Integer.parseInt(times[1]);
 		int numberOfSeconds = Integer.parseInt(times[2]);
 		if (numberOfHours != 0 || numberOfMinutes != 0 || numberOfSeconds != 0){
 			gameRunning = true;
 			matchStart = Calendar.getInstance();
 			matchStart.add(Calendar.SECOND, -numberOfSeconds);
 			matchStart.add(Calendar.MINUTE, -numberOfMinutes);
 			matchStart.add(Calendar.HOUR, -numberOfHours);
 		}
 	}
 
 	public AutoRefereePlayer addPlayer(String name) {
 		AutoRefereePlayer apl = new AutoRefereePlayer(name.replaceAll("\u00A7.", ""));
 		players.put(name, apl);
 		return apl;
 	}
 
 	public AutoRefereePlayer getPlayer(String name) {
 		return players.get(name.replaceAll("\u00A7.", ""));
 	}
 
 	public AutoRefereeTeam addTeam(String name) {
 		AutoRefereeTeam at = new AutoRefereeTeam(name);
 		teams.put(name, at);
 		return at;
 	}
 	
 	public void removeTeam(String name) {
 		if (teams.containsKey(name))
 			teams.remove(name);
 	}
 
 	public void changeTeamName(String oldName, String newName) {
 		AutoRefereeTeam at = teams.get(oldName);
 		if (at != null) {
 			at.setName(newName);
 			teams.remove(oldName);
 			teams.put(newName, at);
 		}
 	}
 
 	public void changeTeamColor(String teamName, String colorString) {
 		AutoRefereeTeam at = teams.get(teamName);
 		if (at != null) {
 			at.setColor(colorString);
 		}
 	}
 
 	public void changeHealthOfPlayer(String name, int hp) {
 		AutoRefereePlayer apl = players.get(name);
 		if (apl == null)
 			return;
 		
 		apl.setHealth(hp);
 		
 		if(hp == 0 && "UHC".equalsIgnoreCase(this.gameType))
 			players.remove(name);
 	}
 
 	public void changeArmorOfPlayer(String name, int armor) {
 		AutoRefereePlayer apl = players.get(name);
 		if (apl != null)
 			apl.setArmor(armor);
 	}
 
 	public void changeTeamOfPlayer(String playerName, String teamName) {
 		AutoRefereePlayer apl = players.get(playerName);
 		AutoRefereeTeam at = teams.get(teamName);
 		if (at == null)
 			return;
 		if (apl == null)
 			apl = addPlayer(playerName);
 
 		AutoRefereeTeam atOld = apl.getTeam();
 		apl.setTeam(at);
 	}
 
 	public void removePlayerFromTeam(String playerName, String teamName) {
 		if (players.containsKey(playerName)){
 			AutoRefereePlayer apl = players.get(playerName);
 			AutoRefereeTeam atOld = apl.getTeam();
 			apl.setTeam(null);
 			players.remove(playerName);
 			if(closestPlayers != null && closestPlayers.contains(apl)){
 				closestPlayers.remove(apl);
 			}
 		}
 	}
 
 	public void changeKillsOfPlayer(String name, int kills) {
 		AutoRefereePlayer apl = players.get(name);
 		if (apl != null)
 			apl.setKills(kills);
 	}
 
 	public void changeDeathsOfPlayer(String name, int deaths) {
 		AutoRefereePlayer apl = players.get(name);
 		if (apl != null)
 			apl.setDeaths(deaths);
 	}
 
 	public void changeAccuracyOfPlayer(String name, int acc) {
 		AutoRefereePlayer apl = players.get(name);
 		if (apl != null)
 			apl.setAccuracy(acc);
 	}
 
 	public void changeKillStreakOfPlayer(String name, int streak) {
 		AutoRefereePlayer apl = players.get(name);
 		if (apl != null)
 			apl.setKillStreak(streak);
 	}
 
 	public void addDominationOfPlayer(String name, String name2) {
 		AutoRefereePlayer apl = players.get(name);
 		if (apl != null)
 			apl.addDomination(name2);
 	}
 
 	public void addRevengeOfPlayer(String name, String name2) {
 		AutoRefereePlayer apl = players.get(name2);
 		if (apl != null)
 			apl.removeDomination(name);
 	}
 
 	public void retrieveObjectiveForPlayer(String name, int id, int dataValue) {
 		AutoRefereePlayer apl = players.get(name);
 		if (apl != null) {
 			AutoRefereeTeam at = apl.getTeam();
 			if (at != null) {
 				AutoRefereeObjective obj = at.getObjective(id, dataValue);
 				if (obj != null)
 					apl.addObjective(obj);
 			}
 		}
 	}
 
 	public void loseObjectiveForPlayer(String name, int id, int dataValue) {
 		AutoRefereePlayer apl = players.get(name);
 		if (apl != null) {
 			AutoRefereeTeam at = apl.getTeam();
 			if (at != null) {
 				AutoRefereeObjective obj = at.getObjective(id, dataValue);
 				if (obj != null)
 					apl.removeObjective(obj);
 			}
 		}
 	}
 
 	public void addObjectiveForTeam(String name, int id, int dataValue) {
 		AutoRefereeTeam at = teams.get(name);
 		if (at != null)
 			at.addObjective(new AutoRefereeObjective(id, dataValue, at));
 	}
 
 	public void removeObjectiveForTeam(String name, int id, int dataValue) {
 		AutoRefereeTeam at = teams.get(name);
 		if (at != null)
 			at.removeObjective(id, dataValue);
 	}
 
 	public ArrayList<AutoRefereeObjective> objectivePlayerHas(String name) {
 		AutoRefereePlayer apl = players.get(name);
 		if (apl != null)
 			return apl.getObjectives();
 		return null;
 	}
 
 	public int getHealthOfPlayer(String name) {
 		AutoRefereePlayer apl = players.get(name);
 		if (apl != null)
 			return apl.getHealth();
 		else
 			return 0;
 	}
 
 	public int getArmorOfPlayer(String name) {
 		AutoRefereePlayer apl = players.get(name);
 		if (apl != null)
 			return apl.getArmor();
 		else
 			return 0;
 	}
 
 	public String getColorOfPlayer(String name) {
 		AutoRefereePlayer apl = players.get(name);
 		if (apl != null) {
 			AutoRefereeTeam at = apl.getTeam();
 			if (at != null) {
 				return at.getColorString();
 			} else {
 				return "";
 			}
 		} else
 			return "";
 	}
 
 	public void setCapeUrlForPlayer(String playerName, String url) {
 		if (url == null)
 			return;
 		url = "http://" + url;
 		AutoRefereePlayer apl = getPlayer(playerName);
 		if (apl != null) {
 			apl.setCapeUrl(url);
 		}
 	}
 
 	public void removeCapeUrlForPlayer(String playerName) {
 		AutoRefereePlayer apl = getPlayer(playerName);
 		if (apl != null) {
 			apl.setCapeUrl("");
 		}
 	}
 	
 	public boolean isFFA(){
 		return this.ffa;
 	}
 
 	public void setLoggedIn(String playerName, boolean loggedIn) {
 		AutoRefereePlayer apl = getPlayer(playerName);
 		if (apl != null) {
 			apl.setLoggedIn(loggedIn);
 		}
 	}
 
 	public void changeItemAmountOfPlayer(String name, int id, int data, int amount) {
 		AutoRefereePlayer apl = players.get(name);
 		if (apl != null)
 			apl.setItemAmount(id, data, amount);
 	}
 
 	public void changeDimensionOfPlayer(String name, String dimension) {
 		AutoRefereePlayer apl = players.get(name);
 		if (apl != null) {
 			if ("NETHER".equalsIgnoreCase(dimension) || "OVERWORLD".equalsIgnoreCase(dimension) || "END".equalsIgnoreCase(dimension))
 				apl.setDimension(dimension);
 		}
 	}
 	
 	public String getNumberOfPlayersRemainingString(){
 		/*int totalPlayers = 0;
 		int playersAlive = 0;
 		for(AutoRefereePlayer apl : players.values()){
 			++totalPlayers;
 			if (apl.getHealth() != 0)
 				++playersAlive;
 		}
 		return playersAlive + " of " + totalPlayers + " remaining";*/
 		return players.size() + (players.size() == 1 ?  " player remaining" : " players remaining");
 	}
 
 	public ArrayList<AutoRefereePlayer> getPlayersOfTeam(AutoRefereeTeam at) {
 		ArrayList<AutoRefereePlayer> returnList = new ArrayList<AutoRefereePlayer>();
 		for (AutoRefereePlayer apl : players.values()) {
 			if (apl.getTeam() == at)
 				returnList.add(apl);
 		}
 		return returnList;
 	}
 
 	public AutoRefereeTeam getLeftTeam(int tick) {
 		Iterator<AutoRefereeTeam> iterator = teams.values().iterator();
 		if ("RFW".equalsIgnoreCase(gameType)) {
 			if (!iterator.hasNext())
 				return null;
 			if (this.swapTeams)
 				return iterator.next();
 			iterator.next();
 			if (iterator.hasNext())
 				return iterator.next();
 		} /*else if ("UHC".equalsIgnoreCase(gameType)) {
 			int teamNumber = (int) ((int) (tick / TEAM_LIST_CYCLE) % Math.ceil(((double) teams.size() / 2)));
 			for (int i = 0; i < teamNumber * 2; ++i)
 				if (iterator.hasNext())
 					iterator.next();
 			if (this.swapTeams && iterator.hasNext())
 				return iterator.next();
 			if (iterator.hasNext())
 				iterator.next();
 			if (iterator.hasNext())
 				return iterator.next();
 		}*/
 		return null;
 	}
 
 	public AutoRefereeTeam getRightTeam(int tick) {
 		Iterator<AutoRefereeTeam> iterator = teams.values().iterator();
 		if ("RFW".equalsIgnoreCase(gameType)) {
 			if (!iterator.hasNext())
 				return null;
 			if (!this.swapTeams)
 				return iterator.next();
 			iterator.next();
 			if (iterator.hasNext())
 				return iterator.next();
 		} /*else if ("UHC".equalsIgnoreCase(gameType)) {
 			int teamNumber = (int) ((int) (tick / TEAM_LIST_CYCLE) % Math.ceil(((double) teams.size() / 2)));
 			for (int i = 0; i < teamNumber * 2; ++i)
 				if (iterator.hasNext())
 					iterator.next();
 			if (!this.swapTeams && iterator.hasNext())
 				return iterator.next();
 			if (iterator.hasNext())
 				iterator.next();
 			if (iterator.hasNext())
 				return iterator.next();
 		}*/
 		return null;
 	}
 	
 	public ArrayList<AutoRefereePlayer> getCyclingPlayers(int tick, boolean FromTickBefore){
 		if(closestPlayers == null){
 			updateClosestPlayers();
 			closestPlayersUpdateTick = tick;
 		}
 		
 		//RETURN PLAYERS DEPENDING ON TIME.
 		if(closestPlayers.size() == 0 && players.size() > MAX_NUMBER_OF_PLAYERS_ON_SCREEN && players.size() <= 2 * MAX_NUMBER_OF_PLAYERS_ON_SCREEN){
 			if (FromTickBefore)
 				tick = 0;
 			else
 				tick = TEAM_LIST_CYCLE;
 		}
 		if(players.size() > 0 && (FromTickBefore || players.size() > MAX_NUMBER_OF_PLAYERS_ON_SCREEN)){
 			ArrayList<AutoRefereePlayer> rightPlayers = new ArrayList<AutoRefereePlayer>();
 			
 			int firstPlayerNumber = (int) ((int) (tick / TEAM_LIST_CYCLE) % Math.ceil((double)players.size()/MAX_NUMBER_OF_PLAYERS_ON_SCREEN)) * MAX_NUMBER_OF_PLAYERS_ON_SCREEN;
 			firstPlayerNumber -= closestPlayers.size();
 			if(firstPlayerNumber < 0)
 				firstPlayerNumber = 0;
 			
 			int lastPlayerNumber = (firstPlayerNumber + MAX_NUMBER_OF_PLAYERS_ON_SCREEN-1);
 			
 			int i = 0;
 			for(AutoRefereePlayer apl : players.values()){
 				if(closestPlayers.contains(apl))
 					continue;
 				if(firstPlayerNumber <= i && i <= lastPlayerNumber)
 					rightPlayers.add(apl);
 				if(i >= lastPlayerNumber)
 					break;
 				++i;
 			}
 			return rightPlayers;
 		}else
 			return new ArrayList<AutoRefereePlayer>();
 	}
 	
 	public List<AutoRefereePlayer> getClosestPlayers(int tick){
 		// RETURN CLOSEST PLAYERS
 		if(closestPlayers == null || tick % (TEAM_LIST_CYCLE) == 0){
 			updateClosestPlayers();
 			closestPlayersUpdateTick = tick;
 		}else if(tick - closestPlayersUpdateTick > TEAM_LIST_CYCLE){
 			updateClosestPlayers();
 			closestPlayersUpdateTick = tick - (tick % TEAM_LIST_CYCLE);
 		}
 		return closestPlayers;
 	}
 	
 	public void updateClosestPlayers(){
 		HashMap<AutoRefereePlayer,Double> distances = new HashMap<AutoRefereePlayer,Double>();
 		Iterator<List> allPlayersIterator = this.mc.theWorld.playerEntities.iterator();
 		while(allPlayersIterator.hasNext()){
 			EntityPlayer pl = (EntityPlayer) allPlayersIterator.next();
 			if(pl != this.mc.thePlayer && players.containsKey(pl.username)){
 				double dist = pl.getDistanceSqToEntity(this.mc.thePlayer);
 				AutoRefereePlayer apl = players.get(pl.username);
 				distances.put(apl, dist);
 			}
 		}
 		closestPlayers = getClosestKPlayers(MAX_NUMBER_OF_PLAYERS_ON_SCREEN,distances);
 	}
 	
 	public List<AutoRefereePlayer> getClosestKPlayers(int k, HashMap<AutoRefereePlayer,Double> distances){
 		List<AutoRefereePlayer> players = new ArrayList<AutoRefereePlayer>(); 
 		players.addAll(distances.keySet());
 		Collections.sort(players, new DistanceComparator(distances));
 		if(distances.size() < k)
 			k = distances.size();
 		players = players.subList(0, k);
 		return players;
 	}
 	
 	private static class DistanceComparator implements Comparator<AutoRefereePlayer>{
 		HashMap<AutoRefereePlayer,Double> distances = new HashMap<AutoRefereePlayer,Double>();
 
 		public DistanceComparator(HashMap<AutoRefereePlayer,Double> distances){ 
 			this.distances = distances; 
 		}
 
 		public int compare(AutoRefereePlayer apl1, AutoRefereePlayer apl2)
 		{
 			return distances.get(apl1) < distances.get(apl2) ? -1 : 1;
 		}
 	};
 
 	public String getGameType() {
 		return gameType;
 	}
 
 	public void renderHearts(int health, int x, int y, float scale, boolean renderIfNone) {
 		if (!renderIfNone && health == 0)
 			return;
 		float offset = 8;
 		int offsetYInTexture = 0;
 		if("UHC".equalsIgnoreCase(gameType))
 			offsetYInTexture = 5*9;
 		for (int k = 0; k < 10; k++) {
 			if (k * 2 + 1 < health) {
 				drawTexturedModalRect(k * offset + x, y, 16, offsetYInTexture, 9, 9, scale);
 				drawTexturedModalRect(k * offset + x, y, 52, offsetYInTexture, 9, 9, scale);
 			}
 			if (k * 2 + 1 == health) {
 				drawTexturedModalRect(k * offset + x, y, 16, offsetYInTexture, 9, 9, scale);
 				drawTexturedModalRect(k * offset + x, y, 61, offsetYInTexture, 9, 9, scale);
 			}
 			if (k * 2 + 1 > health) {
 				drawTexturedModalRect(k * offset + x, y, 16, offsetYInTexture, 9, 9, scale);
 			}
 
 		}
 	}
 
 	public void renderArmor(int armor, int x, int y, float scale) {
 		if (armor == 0)
 			return;
 		float offset = 8;
 		for (int k = 0; k < 10; k++) {
 			if (k * 2 + 1 < armor) {
 				drawTexturedModalRect(k * offset + x, y, 34, 9, 9, 9, scale);
 			}
 			if (k * 2 + 1 == armor) {
 				drawTexturedModalRect((int) k * offset + x, y, 25, 9, 9, 9, scale);
 			}
 			if (k * 2 + 1 > armor) {
 				drawTexturedModalRect((int) k * offset + x, y, 16, 9, 9, 9, scale);
 			}
 		}
 	}
 
 	private void drawTexturedModalRect(float x, float y, int u, int v, int width, int height, float scale) {
 		GuiIngame guiIngame = this.mc.ingameGUI;
 		renderSettingsStart(x, y, -guiIngame.zLevel, scale);
 		// Bind the gui/icons file
 		this.mc.func_110434_K().func_110577_a(GUI_ICONS);
 		guiIngame.drawTexturedModalRect(0, 0, u, v, width, height);
 		renderSettingsEnd();
 	}
 
 	/* Render a right-aligned string */
 	public int renderString(String text, float x, float y, float scale, int color, boolean shadow) {
 		renderSettingsStart(x, y, 0, scale);
 		int returnValue = 0;
 		if (shadow)
 			returnValue = this.mc.fontRenderer.drawStringWithShadow(text, 0, 0, color);
 		else
 			this.mc.fontRenderer.drawString(text, 0, 0, color);
 		renderSettingsEnd();
 		return returnValue;
 	}
 
 	/* Render a center-aligned string */
 	public float renderCenteredString(String text, float x, float y, float scale, int color, boolean shadow) {
 		int width = this.mc.fontRenderer.getStringWidth(text);
 		x -= width / 2 * scale;
 		this.renderString(text, x, y, scale, color, shadow);
 		return x;
 	}
 
 	/* Render a left-aligned string */
 	public float renderLeftString(String text, float x, float y, float scale, int color, boolean shadow) {
 		int width = this.mc.fontRenderer.getStringWidth(text);
 		x -= width * scale;
 		this.renderString(text, x, y, scale, color, shadow);
 		return x;
 	}
 
 	/* This method is used for rendering an objective above a player's head and in the player list */
 	public void renderItem(int itemId, int itemDataValue, int x, int y, float scale) {
 		if(itemId != 0)
 		    renderItem(itemId, itemDataValue, 1, x, y, scale);
 	}
 
 	public void renderItem(int itemId, int itemDataValue, int itemAmount, int x, int y, float scale) {
 		renderSettingsStart(x, y, 0, scale);
 		ItemStack is = new ItemStack(itemId, itemAmount, itemDataValue);
 		renderItem.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.func_110434_K(), is, 0, 0);
 		renderItem.renderItemOverlayIntoGUI(mc.fontRenderer, mc.func_110434_K(), is, 0, 0);
 		renderSettingsEnd();
 		GL11.glDisable(GL11.GL_LIGHTING);
 	}
 
 	public void renderSkinHead(AutoRefereePlayer apl, float x, float y, float scale) {
 		this.mc.func_110434_K().func_110577_a(apl.getSkin());
 		renderSettingsStart(x, y, 0, scale);
 		int size = 20;
 		float width = (float) 8 / 64;
 		float height = (float) 8 / 32;
 		float xOffset = (float) 8 / 64;
 		float yOffset = (float) 8 / 32;
 		Tessellator var11 = Tessellator.instance;
 		var11.startDrawingQuads();
 		var11.addVertexWithUV(0, 0, 0, xOffset, yOffset);
 		var11.addVertexWithUV(0, size, 0, xOffset, yOffset + height);
 		var11.addVertexWithUV(size, size, 0, xOffset + width, yOffset + height);
 		var11.addVertexWithUV(size, 0, 0, xOffset + width, yOffset);
 		var11.draw();
 		renderSettingsEnd();
 	}
 
 	public float renderAutoRefereeIcon(int id, int amount, float x, float y, float scale, int color) {
 		// Bind the AutoReferee icons file
 		this.mc.func_110434_K().func_110577_a(AUTOREFEREE_ICONS);
 		renderSettingsStart(x, y, 0, scale);
 		int u = id % AUTOREFEREE_ICON_NUMBER_IN_WIDTH;
 		int v = id / AUTOREFEREE_ICON_NUMBER_IN_WIDTH;
 		float width = (float) 1 / AUTOREFEREE_ICON_NUMBER_IN_WIDTH;
 		float height = (float) 1 / AUTOREFEREE_ICON_NUMBER_IN_HEIGHT;
 		float xOffset = u * width;
 		float yOffset = v * height;
 		Tessellator var11 = Tessellator.instance;
 		var11.startDrawingQuads();
 		var11.addVertexWithUV(0, 0, 0, xOffset, yOffset);
 		var11.addVertexWithUV(0, AUTOREFEREE_ICON_SIZE, 0, xOffset, yOffset + height);
 		var11.addVertexWithUV(AUTOREFEREE_ICON_SIZE, AUTOREFEREE_ICON_SIZE, 0, xOffset + width, yOffset + height);
 		var11.addVertexWithUV(AUTOREFEREE_ICON_SIZE, 0, 0, xOffset + width, yOffset);
 		var11.draw();
 		if (amount > 0)
 			this.renderString("" + amount, AUTOREFEREE_ICON_SIZE * 0.65F, AUTOREFEREE_ICON_SIZE * 0.75F, scale * 0.8F, color, true);
 		renderSettingsEnd();
 		return scale * AUTOREFEREE_ICON_TEXTURE_SIZE;
 	}
 
 	public float renderAutoRefereeIcon(int id, int amount, float x, float y, float scale) {
 		return renderAutoRefereeIcon(id, amount, x, y, scale, 16777215);
 	}
 
 	public void renderSettingsStart(float x, float y, float z, float scale) {
 		GL11.glPushMatrix();
 		GL11.glTranslatef(x, y, z);
 		GL11.glScalef(scale, scale, scale);
 	}
 
 	public void renderSettingsEnd() {
 		GL11.glPopMatrix();
 	}
 }
 // END CODE
