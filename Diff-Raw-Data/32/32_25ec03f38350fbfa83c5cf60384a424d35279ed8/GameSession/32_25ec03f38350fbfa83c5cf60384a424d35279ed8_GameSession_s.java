 package fr.frozen.iron.serverSessions;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInput;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.nio.channels.SocketChannel;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Set;
 
 import fr.frozen.iron.common.GameContext;
 import fr.frozen.iron.common.IronMap;
 import fr.frozen.iron.common.IronWorld;
 import fr.frozen.iron.common.PlayerGameInfo;
 import fr.frozen.iron.common.entities.IronUnit;
 import fr.frozen.iron.common.skills.Skill;
 import fr.frozen.iron.net.IronServer;
 import fr.frozen.iron.protocol.Protocol;
 import fr.frozen.iron.util.IronConfig;
 import fr.frozen.iron.util.IronConst;
 import fr.frozen.iron.util.IronUtil;
 import fr.frozen.network.common.Message;
 import fr.frozen.network.common.MessageToSend;
 import fr.frozen.network.server.Client;
 import fr.frozen.util.XMLParser;
 import fr.frozen.util.pathfinding.Path;
 
 public class GameSession extends BaseGameController implements GameContext {
 
 	protected int hostColor = 0xff0000;
 	protected int otherColor = 0x0070b7;//0xff3333;
 	
 	
 	protected Hashtable<Client, PlayerGameInfo> playerInfo;
 	protected IronWorld world;
 	protected int nextEntityId = 0;
 	
 	protected int lastReadyId = -1;
 	protected int nbReady = 0;
 	protected boolean gameStarted = false;
 	protected boolean gameOver = false;
 	
 	
 	protected float timeLeftForTurn = 0;
 	protected int turnPlayerId = -1;
 	protected int turnIndex = 0; //switches from 0 to 1 to make it easier to switch turns
 	
 	public GameSession(Client host, int hostRace, Client other, int otherRace, IronServer server) {
 		super("Game session", server, Protocol.SESSION_GAME);
 		clients.add(host);
 		clients.add(other);
 		
 		playerInfo = new Hashtable<Client, PlayerGameInfo>();
 		playerInfo.put(host, new PlayerGameInfo(Protocol.get(hostRace), hostColor));
 		playerInfo.put(other, new PlayerGameInfo(Protocol.get(otherRace), otherColor));
 		
		logger.info("New game : "+host+"("+Protocol.get(hostRace)+") vs "+
 				other+"("+Protocol.get(otherRace)+")");
 		
 		
 		world = new IronWorld(this);
 		
 		world.setMap(new IronMap());
 		world.getMap().generateMap();
 		
 		world.setUnits(createGameUnitsList());
 	}
 	
 	@Override
 	public synchronized void removeClient(Client c, String reason) {
 		
 		if (!gameOver) {
 			int winnerIndex = c.getId() == turnPlayerId ? 1 ^ turnIndex : turnIndex;
 			onGameOver(clients.get(winnerIndex), clients.get(1 ^ winnerIndex));
 		}
 		
 		super.removeClient(c, reason);
 		if (clients.size() == 0) {
 			server.removeGameSession(this);
 			logger.debug(this+" removed");
 		}
 	}
 
 	public synchronized void onGameOver(Client winner, Client loser) {
 		int nbWinnerUnits = 0;
 		int nbLoserUnits = 0;
 		
 		for (IronUnit unit : world.getUnits()) {
 			if (!unit.isDead()) {
 				if (unit.getOwnerId() == winner.getId()) {
 					nbWinnerUnits++;
 				} else {
 					nbLoserUnits++;
 				}
 			}
 		}
 		
 		logger.info("[GameOver]"+winner+"("+playerInfo.get(winner).getRace()+
 				") wins against "+loser+"("+playerInfo.get(loser).getRace()+
 				") -- Units left : ["+nbWinnerUnits+"]vs["+nbLoserUnits+"]");
 		gameOver = true;
 		notifyGameEnded(winner.getId());
 	}
 
 	public synchronized void setTurn(int playerId) {
 		if (turnPlayerId != -1) {
 			getPlayerInfo(turnPlayerId).setTurnToPlay(false);
 			world.endTurn(playerId);
 		}
 		getPlayerInfo(playerId).setTurnToPlay(true);
 		turnPlayerId = playerId;
 		world.initTurn(playerId, false);
 		
 		timeLeftForTurn = IronConst.TURN_DURATION;
 		
 		List<SocketChannel> channels = getAllChannels();
 		if (channels == null){
 			return;//TODO handle this type of error ?
 		}
 		server.getWriter().addMsg(new MessageToSend(channels, Protocol.GAME_TURN.getValue(), IronUtil.intToByteArray(playerId)));
 	}
 	
 	public void startGame() {
 		turnIndex = (int)(System.currentTimeMillis() % 2);
 		Client player = clients.get(turnIndex);
 		
 		setTurn(player.getId());
 		gameStarted = true;
 	}
 	
 	public void notifyGameEnded(int winnerId) {
 		List<SocketChannel> channels = getAllChannels();
 		if (channels == null) return;
 		
 		try {
 			byteArray.reset();
 			byteArray.write(IronUtil.intToByteArray(winnerId));
 
 			MessageToSend msgToSend = new MessageToSend(channels,
 					Protocol.GAME_OVER.ordinal(),
 					byteArray.toByteArray());
 			server.getWriter().addMsg(msgToSend);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public synchronized void update(float deltaTime) {
 		super.update(deltaTime);
 		
 		if (!gameStarted) return;
 		boolean turnEnded = false;
 		
 		timeLeftForTurn -= deltaTime;
 		if (timeLeftForTurn <= 0) {
 			turnEnded = true;
 		}
 		
 		if (!gameOver) {
 			if (world.areAllUnitsDead(clients.get(turnIndex ^ 1).getId())) {
 				onGameOver(clients.get(turnIndex), clients.get(1 ^ turnIndex));
 			}
 		}
 		
 		turnEnded |= world.haveAllUnitsPlayed(turnPlayerId);
 		
 		if (turnEnded && !gameOver) {
 			switchTurns();
 		}
 	}
 	
 	protected void switchTurns() {
 		turnIndex ^= 1;
 		if (clients.size() == 2)
 			setTurn(clients.get(turnIndex).getId());
 	}
 	
 	protected List<IronUnit> createGameUnitsList() {
 		List<IronUnit> list = new ArrayList<IronUnit>();
 		IronUnit unit = null;
 		XMLParser parser = IronConfig.getIronXMLParser();
 		int nblines = 2;
 		
 		String []line = new String[nblines];
 		
 		int nb = 0;//0 is when its top player, and 1 bottom player
 		int []x = new int[nblines];
 		int []y = new int[nblines];
 		
 		main : for (Client client : clients) {
 			
 			String race = null;
 			if (playerInfo.get(client) == null) continue; //TODO error to handle here
 			race = IronUtil.getRaceStr(playerInfo.get(client).getRace());
 			
 			if (race == null) continue; //TODO error to handle here
 			
 			
 			for (int i = 0; i < nblines; i++) {
 				line[i] = parser.getAttributeValue("deployement/"+race, "line"+(i+1));
 				if (line[i] == null) {
 					logger.error("probs at parsing deployement line "+(i+1));
 					continue main;
 				}
 			}
 
 			for (int i = 0; i < nblines; i++) {
 				x[i] = IronConst.MAP_WIDTH / 2 - line[i].length() / 2;
 				y[i] = nb == 0 ? i : IronConst.MAP_HEIGHT - (1 + i);
 			}
 			
 			for (int i = 0; i < nblines; i++) {
 				for (int j = 0; j < line[i].length(); j++) {
 					unit = IronUnit.getUnit(line[i].charAt(j), world, nextEntityId, client.getId(), x[i], y[i]);
 					x[i]++;
 					if (unit != null) {
 						list.add(unit);
 						nextEntityId++;
 						unit = null;
 					}
 				}
 			}
 			nb++;
 		}
 		return list;
 	}
 	
 	@Override
 	public void handle(Message msg) {
 		
 		//TODO : add security everywhere in case there is false data sending
 		switch (Protocol.get(msg.getType())) {
 		case GAME_END_TURN_REQUEST :
 			synchronized (this) {
 				if (!gameOver && turnPlayerId == msg.getClientId()) {
 					switchTurns();
 				}
 			}
 			break;
 		
 		case GAME_UNDO_REQUEST :
 			IronUnit unit = world.getUnitFromId(IronUtil.byteArrayToInt(msg.getData()));
 			if (unit != null && msg.getClientId() == turnPlayerId && !unit.isDead() 
 					&& unit.getOwnerId() == turnPlayerId && unit.canUndo() && !gameOver) {
 				
 				unit.undoMove();
 				
 				List<SocketChannel> channels = getAllChannels();
 				if (channels == null){
 					return;//TODO handle this type of error ?
 				}
 				MessageToSend msgToSend = new MessageToSend(channels,
 						Protocol.GAME_UNDO.ordinal(),
 						msg.getData());
 				server.getWriter().addMsg(msgToSend);
 			}
 			break;
 		
 		case GAME_ACTION_REQUEST : 
 			try {
 				if (!gameOver && turnPlayerId == msg.getClientId()) {
 					handleActionRequest(msg.getData());
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			break;
 		case  GAME_MAP_REQUEST :
 			sendMap(msg.getClientId());
 			break;
 		case GAME_PLAYER_INFO_REQUEST :
 			sendPlayerInfo(msg.getClientId());
 			break;
 		case GAME_UNIT_LIST_REQUEST :
 			sendGameUnitList(msg.getClientId());
 			break;
 		case GAME_READY :
 			if (msg.getClientId() != lastReadyId) {
 				lastReadyId = msg.getClientId();
 				nbReady++;
 				
 				if (nbReady == 2) {
 					startGame();
 				}
 			}
 			break;
 		default :
 			super.handle(msg);
 			break;
 		}
 	}
 	
 	public void handleActionRequest(byte [] data) throws IOException {
 		DataInput is = new DataInputStream(new ByteArrayInputStream(data));
 		
 		int unitSrcId = is.readInt();
 		int actionType = is.readInt();
 		int x, y;
 		
 		if (world.getUnitFromId(unitSrcId).hasPlayed()) return;
 		
 		switch (actionType) {
 		case IronUnit.ACTION_MOVE :
 			x = is.readInt();
 			y = is.readInt();
 			handleMove(unitSrcId, x, y, data);
 			break;
 		case IronUnit.ACTION_SKILL:
 			int skillType = is.readInt();
 			x = is.readInt();
 			y = is.readInt();
 			Skill skill = Skill.getSkill(skillType);
 			IronUnit unitSrc = world.getUnitFromId(unitSrcId);
 			if (unitSrc.getSkills().contains(skill)) {
 				handleSkill(unitSrcId, skill, x, y);
 			}
 			break;
 		default :
 			logger.error("ACTION NOT SUPPORTED :"+ actionType);
 		}
 	}
 
 	protected void handleSkill(int unitId, Skill skill, int x, int y) throws IOException {
 		if (skill == null) return;
 		List<int[]> res = skill.executeSkill(world, unitId, x, y);
 		
 		if (res == null) return;
 		
 		List<SocketChannel> channels = getAllChannels();
 		if (channels == null){
 			return;//TODO handle this type of error ?
 		}
 		
 		//goes as follows : 
 		//unitID - actionType - skillType - x - y - list of [dstId - value] ended by -1 
 		
 		byteArray.reset();
 		byteArray.write(IronUtil.intToByteArray(unitId));
 		byteArray.write(IronUtil.intToByteArray(IronUnit.ACTION_SKILL));
 		byteArray.write(IronUtil.intToByteArray(skill.getSkillType()));
 		byteArray.write(IronUtil.intToByteArray(x));
 		byteArray.write(IronUtil.intToByteArray(y));
 		
 		for (int [] couple : res) {
 			byteArray.write(IronUtil.intToByteArray(couple[0]));//targetId
 			byteArray.write(IronUtil.intToByteArray(couple[1]));//value of heal/damage
 		}
 		
 		byteArray.write(IronUtil.intToByteArray(-1));
 		
 		MessageToSend msgToSend = new MessageToSend(channels,
 				Protocol.GAME_ACTION.ordinal(),
 				byteArray.toByteArray());
 		server.getWriter().addMsg(msgToSend);
 	}
 	
 	protected void handleMove(int unitId, int x, int y, byte []data) {
 		IronUnit unit = world.getUnitFromId(unitId);
 		if (unit == null || unit.isDead()) return;
 		Path path = world.getPath(unitId, x, y);
 		
 		if (path != null) {
 			List<SocketChannel> channels = getAllChannels();
 			if (channels == null){
 				return;//TODO handle this type of error ?
 			}
 			world.getUnitFromId(unitId).move(x, y, path.getTotalMoveCost());
 			byte [] dataToSend = new byte[data.length + 4];
 			System.arraycopy(data, 0, dataToSend, 0, data.length);
 			System.arraycopy(IronUtil.intToByteArray(path.getTotalMoveCost()), 0, dataToSend, data.length, 4);
 			
 			server.getWriter().addMsg(new MessageToSend(channels, Protocol.GAME_ACTION.getValue(), dataToSend));
 		}
 	}
 	
 	
 	public void sendMap(int clientId) {
 		Client dest = server.getClient(clientId);
 
 		if (dest == null) {
 			logger.error("problem, client is null (in sendMap");
 			return;
 		}
 		try {
 			byteArray.reset();
 			byteArray.write(world.getMap().serialize());
 
 			MessageToSend msgToSend = new MessageToSend(dest.getChannel(),
 					Protocol.GAME_MAP_SEND.ordinal(),
 					byteArray.toByteArray());
 			
 			server.getWriter().addMsg(msgToSend);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void sendPlayerInfo(int clientId) {
 		Client dest = server.getClient(clientId);
 		Set<Client> keySet = playerInfo.keySet();
 
 		if (dest == null || keySet.size() != 2) {
 			logger.error("problem, in send playerInfo");
 			return;
 		}
 		try {
 			byteArray.reset();
 			for (Client c : keySet) {
 				byteArray.write(IronUtil.intToByteArray(c.getId()));
 				byteArray.write(playerInfo.get(c).serialize());
 			}
 			MessageToSend msgToSend = new MessageToSend(dest.getChannel(),
 					Protocol.GAME_PLAYER_INFO_SEND.ordinal(),
 					byteArray.toByteArray());
 
 			server.getWriter().addMsg(msgToSend);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void sendGameUnitList(int clientId) {
 		Client dest = server.getClient(clientId);
 
 		if (dest == null) {
 			logger.error("problem, client is null (in sendGameUnit");
 			return;
 		}
 		try {
 			byteArray.reset();
 			for (IronUnit unit : world.getUnits()) {
 				byteArray.write(unit.serialize());
 			}
 			byteArray.write(IronUtil.intToByteArray(-1));//end of list
 			
 			MessageToSend msgToSend = new MessageToSend(dest.getChannel(),
 					Protocol.GAME_UNIT_LIST_SEND.ordinal(),
 					byteArray.toByteArray());
 			
 			server.getWriter().addMsg(msgToSend);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public int getClientId() {
 		return -1;
 	}
 
 	@Override
 	public PlayerGameInfo getPlayerInfo(int clientId) {
 		return playerInfo.get(server.getClient(clientId));
 	}
 }
