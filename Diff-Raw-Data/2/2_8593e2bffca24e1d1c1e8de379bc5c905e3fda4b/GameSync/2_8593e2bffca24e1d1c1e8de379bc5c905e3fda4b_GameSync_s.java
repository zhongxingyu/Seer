 package cargame.sync;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Map;
 import java.util.TimeZone;
 
 import cargame.CarGame;
 import cargame.core.Client;
 import cargame.core.GameInfo;
 import cargame.core.Player;
 import cargame.core.messaging.UdpMessage;
 import cargame.core.messaging.utils.UdpMessageUtils;
 
 public class GameSync extends Thread implements Client {
 
 	private static final int MESSAGE_LENGTH = 700;
 	private static final int PERMITED_MESSAGE_LOST = 150 ;
 	
 	private InetAddress peerAddress;
 	private int serverPort;
 	private int clientPort;
 	
 	private CarGame game;
 	private boolean running;
 	private boolean server;
 	
 	private long lastReceivedPlayerTime;
 	
 	public GameSync(CarGame game,boolean server,String serverIp) {
 		super();
 		this.game = game;
 		this.server = server;
 		this.running = true; 
 		this.serverPort = 12343;
 		this.clientPort = 12353;
		this.peerAddress = getInetAddress((serverIp == null)?null:serverIp);
 		this.lastReceivedPlayerTime = Long.MIN_VALUE;
 	}
 	
 	private InetAddress getInetAddress(String url){
 		try {
 			return InetAddress.getByName(url);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	@Override
 	public void run(){
 		
 		int lost_packets = 0;
 		
 		while(running){
 			if(game.getStatus() == CarGame.STATUS_WAITING){
 				long latency = getLatency();
 				System.out.println("Latency:"+latency);
 			}else{
 				if(receiveData()){
 					lost_packets = 0;
 					game.setConnectionLost(false);
 				}else{
 					lost_packets++;
 				}
 				
 				if(!game.isGameOver() && !game.isWaiting() && lost_packets >= PERMITED_MESSAGE_LOST){
 					game.setConnectionLost(true);
 				}
 				sendData();
 			}
 		}
 	}
 
 	public boolean isRunning() {
 		return running;
 	}
 
 	public void setRunning(boolean running) {
 		this.running = running;
 	}
 	
 	private void sendData() {
 		UdpMessage outMessage = new UdpMessage(UdpMessage.TYPE_PLAYER_DATA, game.getMyPlayer(), System.currentTimeMillis());
 		outMessage.setAddress(this.peerAddress);
 		UdpMessageUtils.sendMessage(outMessage, (server)?this.clientPort:this.serverPort);
 
 	}
 	
 	private boolean receiveData(){
 		UdpMessage inMessage = UdpMessageUtils.receiveMessage((server)?this.serverPort:this.clientPort, MESSAGE_LENGTH, 20);
 		
 		if(inMessage == null) return false; // No new message
 		if(inMessage.getTime() <= this.lastReceivedPlayerTime) return true; // Ignore old packet
 		this.lastReceivedPlayerTime = inMessage.getTime(); 
 		
 		if(this.server && this.peerAddress == null){
 			this.peerAddress = inMessage.getAddress();
 		}
 		
 		switch(inMessage.getType()){
 			case UdpMessage.TYPE_PLAYER_DATA:
 				Player receivedPlayer = (Player)inMessage.getData();
 				syncPlayerInfo(receivedPlayer);
 			break;
 		}
 		return true;
 	}
 	
 	private long getLatency(){
 		long latency = 0l;
 		if(this.server){
 			UdpMessage inMsg = UdpMessageUtils.receiveMessage((server)?this.serverPort:this.clientPort, MESSAGE_LENGTH, 0);
 //			String timeZone = (String) inMsg.getData();
 			if(inMsg.getType() == UdpMessage.TYPE_SYNC_MESSAGE){
 				latency = System.currentTimeMillis();
 				UdpMessage outMsg = new UdpMessage(UdpMessage.TYPE_SYNC_MESSAGE, TimeZone.getDefault().getDisplayName(), System.currentTimeMillis());
 				while(outMsg != null){
 					outMsg.setAddress(this.peerAddress);
 					UdpMessageUtils.sendMessage(outMsg, (server)?this.clientPort:this.serverPort);
 					inMsg = UdpMessageUtils.receiveMessage((server)?this.serverPort:this.clientPort, MESSAGE_LENGTH, 0);
 					if(inMsg != null){
 						latency = System.currentTimeMillis() - latency;
 						System.out.println("Latency:"+latency);
 					}
 				}
 			}
 		}else{
 			UdpMessage outMsg = new UdpMessage(UdpMessage.TYPE_SYNC_MESSAGE, TimeZone.getDefault().getDisplayName(), System.currentTimeMillis());
 			while(outMsg != null){
 				outMsg.setAddress(this.peerAddress);
 				latency = System.currentTimeMillis();
 				UdpMessageUtils.sendMessage(outMsg, (server)?this.clientPort:this.serverPort);
 				UdpMessage inMsg = UdpMessageUtils.receiveMessage((server)?this.serverPort:this.clientPort, MESSAGE_LENGTH, 0);
 				if(inMsg != null){
 					latency = System.currentTimeMillis() - latency;
 					System.out.println("Latency:"+latency);
 				}
 			}
 		}
 		return latency;
 	}
 	
 	@Override
 	public cargame.core.ServerStatus ServerStatus() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public GameInfo currentGame() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void setCurrentGame(int gameInfoID, Player player) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void sendMyPlayerInfo(Player player) {
 
 	}
 	
 //	private void syncPlayersInfo(Map<Integer, Player> newPlayerList){
 //		Map<Integer, Player> playerList = game.getPlayers();
 //		for(Integer playerId : newPlayerList.keySet()){
 //			Player newPlayerInfo = newPlayerList.get(playerId);
 //			Player actualPlayerInfo = playerList.put(playerId, newPlayerInfo);
 //			if(actualPlayerInfo != null){
 //				if(newPlayerInfo.time <= actualPlayerInfo.time){
 //					playerList.put(playerId, actualPlayerInfo);
 //				}
 //			}
 //		}
 //	}
 //	
 //	private void syncPlayerInfo(int playerId, float[] values){
 //		Map<Integer, Player> playerList = game.getPlayers();
 //		if(!playerList.containsKey(playerId)){
 //			Player newPlayer = new Player();
 //			newPlayer.id = playerId;
 //			newPlayer.movingPosition = new MovingPosition();
 //			playerList.put(playerId, newPlayer);
 //		}
 //		Player player = playerList.get(playerId);
 //		player.time = (new Date()).getTime();
 //		player.movingPosition.setValues(values);
 //	}
 	
 	private void syncPlayerInfo(Player player){
 		Map<Integer, Player> playerList = game.getPlayers();
 		playerList.put(player.id, player);
 	}
 }
