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
 	
 	private static final int STATUS_RUNNING = 0;
 	private static final int STATUS_WAITING = 1;
 	
 	private InetAddress peerAddress;
 	private int serverPort;
 	private int clientPort;
 	
 	private int state;
 	private boolean server;
 	
 	private long lastReceivedPlayerTime;
 	
 	public GameSync() {
 		super();
 		this.serverPort = 12343;
 		this.clientPort = 12353;
 		this.state = STATUS_WAITING;
 	}
 	
 	public void start(boolean server,String serverIp){
 		this.state = STATUS_RUNNING; 
 		this.server = server;
 		this.peerAddress = (server)?null:getInetAddress(serverIp);
 		System.out.println("Received address:"+this.peerAddress);
 		this.lastReceivedPlayerTime = Long.MIN_VALUE;
 		if(!this.isAlive()){
 			super.start();
 		}
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
 		while(checkState(STATUS_RUNNING) || checkState(STATUS_WAITING)){
 			if(checkState(STATUS_RUNNING)){
 				if(receiveData()){
 					lost_packets = 0;
 					CarGame.getInstance().setConnectionLost(false);
 				}else{
 					lost_packets++;
 				}
 				if(CarGame.getInstance().checkStatus(CarGame.STATUS_PLAYING) && lost_packets >= PERMITED_MESSAGE_LOST){
 					CarGame.getInstance().setConnectionLost(true);
 				}
				if(this.peerAddress != null && (CarGame.getInstance().checkStatus(CarGame.STATUS_PLAYING) || CarGame.getInstance().checkStatus(CarGame.STATUS_WAITING))){
 					sendData();
 				}
 			}
 		}
 	}
 
 	public void setSetState(int state) {
 		this.state = state;
 	}
 	
 	private void sendData() {
 		UdpMessage outMessage = new UdpMessage(UdpMessage.TYPE_PLAYER_DATA, CarGame.getInstance().getMyPlayer(), System.currentTimeMillis());
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
 			System.out.println("Client address:"+this.peerAddress.getHostAddress());
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
 	
 	private boolean checkState(int state){
 		return state == this.state;
 	}
 	
 	private void syncPlayerInfo(Player player){
 		Map<Integer, Player> playerList = CarGame.getInstance().getPlayers();
 		playerList.put(player.id, player);
 	}
 }
