 package net.dracolair.games.applestoapples;
 
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import net.dracolair.games.applestoapples.card.Card;
 import net.dracolair.games.applestoapples.card.CardRenderer;
 import net.dracolair.games.applestoapples.commands.CmdLimit;
 import net.dracolair.games.applestoapples.commands.MgrCmdChoose;
 import net.dracolair.games.applestoapples.commands.MgrCmdCleanup;
 import net.dracolair.games.applestoapples.commands.MgrCmdCreateGame;
 import net.dracolair.games.applestoapples.commands.MgrCmdDeal7;
 import net.dracolair.games.applestoapples.commands.MgrCmdEndGame;
 import net.dracolair.games.applestoapples.commands.MgrCmdPlay;
 import net.dracolair.games.applestoapples.commands.CmdChoose;
 import net.dracolair.games.applestoapples.commands.CmdJoin;
 import net.dracolair.games.applestoapples.commands.CmdList;
 import net.dracolair.games.applestoapples.commands.CmdPlay;
 import net.dracolair.games.applestoapples.commands.CmdStart;
 import net.dracolair.games.applestoapples.commands.Command;
 
 import static net.dracolair.games.applestoapples.FileParser.*;
 import static net.dracolair.games.applestoapples.Factories.*;
 
 public class GameManager {
 
 	public static List<Card>	RED = null;
 	public static List<Card>	GREEN = null;
 	
 	public Map<String, Game>	m_roomToGameMap = new LinkedHashMap<String, Game>();
 	public Map<Name, Game>		m_nameToGameMap = new LinkedHashMap<Name, Game>();
 	public Map<String, Command> m_commands = new LinkedHashMap<String, Command>();
 	public Map<String, Name> 	m_nickToNameMap = new LinkedHashMap<String, Name>();
 	public String				m_name;
 	
 	public GameManager(String name, CardRenderer redCardRenderer, CardRenderer greenCardRenderer) {
 		m_name = name;
 		
 		
 		
 		if(RED == null) {
			RED = loadCardsFromFile(this.getClass().getResourceAsStream("/red.txt"), redCardRenderer);
 		}
 		if(GREEN == null) {
			GREEN = loadCardsFromFile(this.getClass().getResourceAsStream("/green.txt"), greenCardRenderer);
 		}
 		
 		m_commands.put("join", new CmdJoin());
 		m_commands.put("list", new CmdList());
 		m_commands.put("start", new CmdStart());
 		m_commands.put("play", new CmdPlay());
 		m_commands.put("choose", new CmdChoose());
 		m_commands.put("limit", new CmdLimit());
 		m_commands.put("botplay", new MgrCmdPlay());
 		m_commands.put("botchoose", new MgrCmdChoose());
 		m_commands.put("botdeal7", new MgrCmdDeal7());
 		m_commands.put("botcleanup", new MgrCmdCleanup());
 		m_commands.put("botendgame", new MgrCmdEndGame());
 		m_commands.put("botcreategame", new MgrCmdCreateGame());
 	}
 	
 	public List<Message> processRoomMessage(MessageInfo msgInfo) {
 		List<Message> responses = new LinkedList<Message>();
 		System.out.println("processRoomMessage: " + msgInfo.MESSAGE);
 		if(msgInfo.MESSAGE.charAt(0) == '!') {
 			String[] parsedMessage = msgInfo.MESSAGE.split(" ", 2);
 			String cmdKey = parsedMessage[0].substring(1);
 			MessageInfo modMsgInfo = msgInfo.clone();
 			if (parsedMessage.length < 2) {
 				modMsgInfo.MESSAGE = "";
 			} else {
 				modMsgInfo.MESSAGE = parsedMessage[1];
 			}
 			
 			Command cmd = m_commands.get(cmdKey);
 			Game ata = getGameByChan(modMsgInfo.ROOM);
 			
 			if (cmd != null) {
 				responses = cmd.execute(this, ata, modMsgInfo);
 			}
 		}
 		return responses;
 	}
 	
 	public List<Message> processPrivMessage(MessageInfo msgInfo) {
 		List<Message> responses = new LinkedList<Message>();
 		System.out.println("processPrivMessage: " + msgInfo.MESSAGE);
 		String[] parsedMessage = msgInfo.MESSAGE.split(" ", 3);
 		String cmdKey = parsedMessage[0].substring(1);
 		MessageInfo modMsgInfo = msgInfo.clone();
 		if (parsedMessage.length < 2) {
 			modMsgInfo.MESSAGE = "";
 		} else {
 			modMsgInfo.MESSAGE = parsedMessage[1];
 		}
 		
 		Command cmd = m_commands.get(cmdKey);
 		Game ata = getGameByChan(modMsgInfo.MESSAGE);
 		if(ata == null) {
 			ata = getGameByNick(modMsgInfo.MESSAGE);
 		}
 		
 		if (cmd != null) {
 			responses = cmd.execute(this, ata, modMsgInfo);
 		}
 
 		return responses;
 	}
 
 	public Game getGameByChan(String channel) {
 		return m_roomToGameMap.get(channel);
 	}
 	
 	public Game getGameByNick(String nick) {
 		return m_nameToGameMap.get(m_nickToNameMap.get(nick));
 	}
 	
 	public String getName() {
 		return m_name;
 	}
 
 	public void reset(String room) {
 		Game ata = getGameByChan(room);
 		for(Name n : ata.m_players.keySet()) {
 			m_nameToGameMap.remove(n);
 			m_nickToNameMap.remove(n.toString());
 		}
 		m_roomToGameMap.put(room, GAME(RED, GREEN, ata.m_isRandom));
 	}
 
 	public void changeNick(String oldNick, String newNick) {
 		Name n = m_nickToNameMap.get(oldNick);
 		if(n != null) {
 			n.m_name = newNick;
 			m_nickToNameMap.put(newNick, n);
 			m_nickToNameMap.remove(oldNick);
 		}
 	}
 }
