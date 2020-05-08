 package org.touchirc.irc;
 
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Set;
 
 import org.pircbotx.Channel;
 import org.pircbotx.exception.IrcException;
 import org.pircbotx.exception.NickAlreadyInUseException;
 import org.touchirc.TouchIrc;
 import org.touchirc.model.Profile;
 import org.touchirc.model.Server;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.SparseArray;
 
 public class IrcService extends Service {
 	
 	private final IrcBinder ircBinder;
 	
 	// Map of Server, IrcBot for connected Bots
 	private HashMap<Server, IrcBot> botsConnected;
 	
 	private Server currentServer;
 	private Channel currentChannel;
 	
 	// Map of idServer, Server for available servers 
 	private SparseArray<Server> availableServers; // SparseArray = Map<Integer, Object>
 	
 	public IrcService(){
 		super();
 		
 		this.ircBinder = new IrcBinder(this);
 		this.botsConnected = new HashMap<Server, IrcBot>();
 		TouchIrc.getInstance().load(this);
 		this.availableServers = TouchIrc.getInstance().getAvailableServers();
 	}
 	
 	@Override
 	public void onCreate(){
 	}
 	
 	@Override
 	public void onDestroy(){
 		for(Server s : this.botsConnected.keySet())
 			getBot(s).shutdown(true);
 		System.out.println("[Irc Service] Destroyed !");
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return this.ircBinder;
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId){
 		System.out.println("TouchIrcService Started");
 		return START_STICKY;
 	
 	}
 	
 	public void reloadAvailableServers(){
 		this.availableServers = TouchIrc.getInstance().getAvailableServers();
 	}
 	
 	// return null if the idServer isn't in the Hashmap servers
 	public Server getServerById(int idServer){
 		return this.availableServers.get(idServer);
 	}
 	
 	
 	public Set<Server> getConnectedServers(){
 		return this.botsConnected.keySet();
 	}
 	
 	public synchronized void connect(int idServer){
 		final Server server = availableServers.get(idServer);
 		final IrcBot bot = getBot(server);
 		new Thread("Thread for the server : " + server.getName()){
 			@Override
 			public void run(){
 				System.out.println(" Thread for the server : " + server.getName());
 				Profile profile = (server.hasAssociatedProfile() ? server.getProfile() : TouchIrc.getInstance().getDefaultProfile());
 				bot.setNickName(profile.getFirstNick());
 				bot.setIdent(profile.getUsername());
 				bot.setRealName(profile.getRealname());
 				
 				int connected = -1;
 				while(connected != 0){
 					try {
 						bot.setEncoding(server.getEncoding());
 						bot.connect(server.getHost(),server.getPort(),server.getPassword());
 						if(server.getAutoConnectedChannels() != null)
 							for(String s : server.getAutoConnectedChannels())
 								bot.joinChannel(s);
 						
 						bot.joinChannel("#Boulet2"); // TODO Remove it when the tests will be done
 					} catch (NickAlreadyInUseException e) {
 					} catch (IrcException e) {
 						// TODO Auto-generated catch block
 						
 					} catch (IOException e) {
 						if(e.getCause().toString().startsWith("org.pircbotx.exception.NickAlreadyInUseException")){
 							if(connected == -1){ // First Time so go test with secondNick
 								bot.setNickName(profile.getSecondNick());
 								connected = 1;
 								continue;
 							}else if(connected == 1){ // second error so go to thirdNick
 								bot.setNickName(profile.getThirdNick());
 								connected = 2;
 								continue;
 							}else {
 								e.printStackTrace();
 							}
 						}else{
 							e.printStackTrace();
 						}
 					}
 					connected = 0;
 					currentServer = server;
 					
 				}
 			}
 		}.start();
 		
 		
 		
 	}
 	
 	/**
 	 * Get the Bot/Connection of the server with this idServer
 	 * @param idServer of the server (= id of the Bot)
 	 * @return the bot
 	 */
	// If the Bot don't existe it will be created !
 	public synchronized IrcBot getBot(Server server){
 		IrcBot ircBot = this.botsConnected.get(server);
 		if(ircBot == null){
 			ircBot = new IrcBot(server,this);
 			this.botsConnected.put(server, ircBot);
 		}
 		return ircBot;
 	}
 	
 	public Server getCurrentServer(){
 		return currentServer;
 	}
 	
 	public void setCurrentServer(Server server){
 		if(botsConnected.containsKey(server))
 			currentServer = server;
 	}
 	
 	public Channel getCurrentChannel(){
 		return currentChannel;
 	}
 	
 	public void setCurrentChannel(Channel channel){
 		Set<Channel> channels = getBot(currentServer).getChannels();
 		for(Channel chan : channels){
 			if(chan.equals(channel)){
 				currentChannel = chan;
 				return;
 			}
 		}
 	}
 }
