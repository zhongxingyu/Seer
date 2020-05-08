 package org.touchirc.irc;
 
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Set;
 
 import org.pircbotx.Channel;
 import org.pircbotx.exception.IrcException;
 import org.pircbotx.exception.NickAlreadyInUseException;
 import org.touchirc.R;
 import org.touchirc.TouchIrc;
 import org.touchirc.activity.ConversationActivity;
 import org.touchirc.activity.ExistingServersActivity;
 import org.touchirc.model.Profile;
 import org.touchirc.model.Server;
 
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.IBinder;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.NotificationCompat.Builder;
 import android.util.SparseArray;
 
 public class IrcService extends Service {
 	
 	private final IrcBinder ircBinder;
 	
 	// Map of Server, IrcBot for connected Bots
 	private HashMap<Server, IrcBot> botsConnected;
 	
 	private Server currentServer;
 	private Channel currentChannel;
 	
 	// Map of idServer, Server for available servers 
 	private SparseArray<Server> availableServers; // SparseArray = Map<Integer, Object>
 	
 	private NotificationManager notificationManager;
 	private Builder builder;
 	
 	public IrcService(){
 		super();
 		
 		this.ircBinder = new IrcBinder(this);
 		this.botsConnected = new HashMap<Server, IrcBot>();
 		TouchIrc.getInstance().load(this);
 		this.availableServers = TouchIrc.getInstance().getAvailableServers();
 	}
 	
 	@Override
 	public void onCreate(){
 		builder = new NotificationCompat.Builder(this);
 		builder.setSmallIcon(R.drawable.ic_launcher);
 		builder.setContentTitle("TouchIrc");
 		builder.setContentText("No connected server");
 		Intent intent = new Intent().setClass(getApplicationContext(), ExistingServersActivity.class);
 		builder.setContentIntent(PendingIntent.getActivity(getApplication(), 0, intent, 0));
 		
 		startForeground(1, builder.build());
 		
 		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 
 		for(int i = 0 ; i < availableServers.size() ; i++)
 			if(availableServers.valueAt(i).isAutoConnect())
 				getBot(availableServers.valueAt(i));
 		System.out.println("Service Created");
 	}
 	
 	@Override
 	public void onDestroy(){
 		stopForeground(true);
 		notificationManager.cancel(1);
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
 	
 	public synchronized IrcBot connect(int idServer){
 		final Server server = availableServers.get(idServer);
 		final IrcBot bot = new IrcBot(server, this);
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
 						bot.setAutoReconnect(true);
 						bot.connect(server.getHost(),server.getPort(),server.getPassword());
 						if(server.getAutoConnectedChannels() != null)
 							for(String s : server.getAutoConnectedChannels())
 								bot.joinChannel(s);
 						
 						bot.joinChannel("#Boulet2"); // TODO Remove it when the tests will be done
 						connected = 0;
 						currentServer = server;
 						botsConnected.put(currentServer, bot);
 					} catch (NickAlreadyInUseException e) {
 					} catch (IrcException e) {
 						// TODO Auto-generated catch block
 						botsConnected.remove(server);
 						e.printStackTrace();
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
 								botsConnected.remove(server);
 								e.printStackTrace();
 							}
 						}else{
 							botsConnected.remove(server);
 							e.printStackTrace();
 						}
 					}
 				}
 				updateNotification();
 			}
 		}.start();
 		
 		
 		return bot;
 	}
 	
 	private void updateNotification() {
 		String message = "Connected to : ";
 		for(Server s : this.botsConnected.keySet())
 			message += s.getName() + ", ";
 		builder.setContentText(message.substring(0, message.length()-2));
 		Intent intent = new Intent().setClass(getApplicationContext(), ConversationActivity.class);
 		builder.setContentIntent(PendingIntent.getActivity(getApplication(), 0, intent, 0));
 		notificationManager.notify(1, builder.build());		
 	}
 
 	/**
 	 * Get the Bot/Connection of the server with this idServer
 	 * @param idServer of the server (= id of the Bot)
 	 * @return the bot
 	 */
 	// If the Bot doesn't exist it will be created and launched !
 	public synchronized IrcBot getBot(Server server){
 		IrcBot ircBot = this.botsConnected.get(server);
 		if(ircBot == null)
 			ircBot = connect(availableServers.keyAt(availableServers.indexOfValue(server)));
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
