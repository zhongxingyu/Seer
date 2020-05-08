 /**
  * 
  */
 package uk.ac.horizon.ug.exploding.client;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.UUID;
 
 import uk.ac.horizon.ug.exploding.client.Client.Log;
 import uk.ac.horizon.ug.exploding.client.Client.LoggingUtils;
 import uk.ac.horizon.ug.exploding.client.model.Game;
 import uk.ac.horizon.ug.exploding.client.model.Member;
 import uk.ac.horizon.ug.exploding.client.model.Player;
 import uk.ac.horizon.ug.exploding.client.model.Position;
 import uk.ac.horizon.ug.exploding.client.model.Zone;
 
 import org.apache.http.client.*;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.*;
 
 import com.thoughtworks.xstream.XStream;
 /**
  * @author cmg
  *
  */
 public class Main implements Runnable {
 
 	static String SERVER_URL = "http://localhost:8080/exploding/rpc/";
 	static String CLIENT_PREFIX = "Testing";
 	static String PLAYER_NAME = "Testing";
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		for (int i=0; i<9; i++) {
 			System.out.println("Creating client "+i);
 			Main main = new Main(CLIENT_PREFIX+i, SERVER_URL, PLAYER_NAME+i);
 			new Thread(main).start();
 			mains.add(main);
 			try {
 				Thread.sleep(1000);
 			} 
 			catch(InterruptedException e) {}
 		}
 		try {
 			while(true) {
 				Thread.sleep(15000);
 				System.out.println("Player,players,zones,games,members,messages");
 				for (Main m : mains) {					
 					
 					System.out.println(m.playerName+","+m.client.getFacts(Player.class.getName()).size()+
 							","+m.client.getFacts(Zone.class.getName()).size()+
 							","+m.client.getFacts(Game.class.getName()).size()+
 							","+m.client.getFacts(Member.class.getName()).size()+
							","+m.client.getFacts(uk.ac.horizon.ug.exploding.client.model.Message.class.getName()).size());
 				}
 			}
 		}  
 		catch (Exception e) {
 			System.err.println("Error in dump thread: "+e);
 			e.printStackTrace(System.err);
 		}
 	}
 	static LinkedList<Main> mains = new LinkedList<Main>();
 	
 	public void run() {
 		try {
 			System.out.println("login "+playerName);
 			doLogin();
 			Thread.sleep(10000);
 			System.out.println("getting state "+playerName);
 			doGetState();
 			while(true) {
 				Thread.sleep(15000);
 				createMemberIfPossible();
 				doPoll();
 			}
 		}
 		catch (Exception e) {
 			System.err.println("Error for "+playerName+": "+e);
 			e.printStackTrace(System.err);
 		}
 	}
 	
 
 	Main(String clientId, String serverUrl, String playerName) {
 		this.clientId = clientId;
 		this.serverUrl = serverUrl;
 		this.playerName = playerName;
 	}
 	
 	private String clientId;
 	private String serverUrl;
 	private String conversationId;
 	private HttpClient httpClient;
 	private String playerName;
 	private static final String LOGIN_PATH = "login";
 	private static final String MESSAGES_PATH = "messages";
 	private static final String TAG = "Test";
 	public static final int CLIENT_VERSION = 1;
 	private static final String CLIENT_TYPE = "AndroidDevclient";
	private static final int TO_FOLLOW = 30;
 	private GameStatus gameStatus;
 	private LoginReplyMessage.Status loginStatus;
 	private String loginMessage;
 	private boolean loginOk;
 	private Random random = new Random(System.currentTimeMillis());
 	
 	/** attempt login - called from background thread, unsync. */
 	private void doLogin() {
         // get device unique ID(s)
         conversationId = UUID.randomUUID().toString().substring(0,20);
         
 		httpClient = new DefaultHttpClient();
 		HttpPost request = null;
 		try {
 			String serverUrl = this.serverUrl+LOGIN_PATH;
 			request = new HttpPost(new URI(serverUrl));
 		} catch (Exception e) {
 			Log.e(TAG, "parsing serverUrl "+serverUrl, e);
 			setClientStatus(ClientStatus.ERROR_IN_SERVER_URL, "There is a problem with the Server URL\n("+e.getMessage()+")");
 			return;
 		}
 		try {
 			LoginMessage login = new LoginMessage();
 			login.setClientId(clientId);
 			login.setPlayerName(playerName);
 			login.setConversationId(conversationId);
 			login.setClientVersion(CLIENT_VERSION);
 			login.setClientType(CLIENT_TYPE);
 			// TODO XPP3 driver?
 			XStream xs = new XStream(/*new DomDriver()*/);
 			xs.alias("login", LoginMessage.class);
 			xs.alias("reply", LoginReplyMessage.class);
 			String xmlText = xs.toXML(login);
 			// name?
 			Log.d(TAG,"Login: "+xmlText);
 			log("login", "request", xmlText);
 			//request.setHeader("Content-Type", )
 			request.setEntity(new StringEntity(xmlText));
 			HttpResponse response = httpClient.execute(request);
 			StatusLine statusLine = response.getStatusLine();
 			Log.d(TAG, "Http status on login: "+statusLine);
 			if (statusLine.getStatusCode()!=200) {
 				Log.e(TAG, "Error - Http status on login: "+statusLine);
 				setClientStatus(ClientStatus.ERROR_DOING_LOGIN, "Error logging in!\n("+statusLine.getReasonPhrase()+")");				
 				if (response.getEntity()!=null)
 					response.getEntity().consumeContent();
 				return;
 			}
 			LoginReplyMessage reply = (LoginReplyMessage )xs.fromXML(response.getEntity().getContent());
 			response.getEntity().consumeContent();
 			Log.d(TAG,"Reply: "+reply);
 			log("loginReply", "reply", reply.toString());
 
 			gameStatus = GameStatus.valueOf(reply.getGameStatus());
 			loginStatus = LoginReplyMessage.Status.valueOf(reply.getStatus());
 			loginMessage = reply.getMessage();
 
 			if (loginStatus==LoginReplyMessage.Status.OK && 
 					(gameStatus==GameStatus.ACTIVE ||
 							gameStatus==GameStatus.NOT_STARTED ||
 							gameStatus==GameStatus.ENDING)) {
 				loginOk = true;
 				// currentClientState.setClientStatus(ClientStatus.GETTING_STATE);
 			} else if (loginStatus==LoginReplyMessage.Status.OK && 
 					(gameStatus==GameStatus.ENDED)||
 					loginStatus==LoginReplyMessage.Status.GAME_NOT_FOUND) {
 				// game is over - shouldn't ever actually be returned to login
 				//currentClientState.setClientStatus(ClientStatus.STOPPED);
 			}
 			else
 				;//currentClientState.setClientStatus(ClientStatus.ERROR_DOING_LOGIN);
 
 		} catch (Exception e) {
 			Log.e(TAG, "Attempting post to serverUrl "+serverUrl, e);
 			setClientStatus(ClientStatus.ERROR_DOING_LOGIN, "Error logging in!\n("+e.getMessage()+")");
 			return;
 		}
 	}
 
 	private Client client;
 	private long lastPollTime;
 	
 	private void doGetState() {
 		try {
 			client = new Client(httpClient, serverUrl+MESSAGES_PATH+"?conversationID="+conversationId, clientId);
 //			currentClientState.setCache(client);
 		}
 		catch (Exception e) {
 			Log.e(TAG, "Creating message client", e);
 			setClientStatus(ClientStatus.ERROR_IN_SERVER_URL, "There is a problem with the Server messages URL\n("+e.getMessage()+")");
 			return;			
 		}
 		try {
 			updatePlayer();
 			client.sendQueuedMessages();
 			client.poll(getToFollow());
 			lastPollTime = System.currentTimeMillis();
 			// success = good
 			setClientStatus(ClientStatus.POLLING, "Ready to play");
 		}
 		catch (Exception e) {
 			Log.e(TAG, "Doing first poll", e);
 			setClientStatus(ClientStatus.ERROR_GETTING_STATE, "Could not join the game\n("+e.getMessage()+")");
 			return;						
 		}
 	}
 	private int getToFollow() {
 		return TO_FOLLOW;
 	}
 
 
 	private void doPoll() {
 		try {
 			setClientStatus(ClientStatus.POLLING, "Trying to get updates");	
 			updatePlayer();
 			client.sendQueuedMessages();
 			client.poll(getToFollow());
 			// success = good
 			setClientStatus(ClientStatus.IDLE, "Ready to play");			
 		}
 		catch (Exception e) {
 			Log.e(TAG, "Doing (later) poll", e);
 			setClientStatus(ClientStatus.ERROR_AFTER_STATE, "Could not get updates\n("+e.getMessage()+")");
 			return;						
 		}
 	}
 	private Position lastPosition;
	static double INITIAL_LATITUDE = 52.951419;//51.49083;
	static double INITIAL_LONGITUDE = -1.183117;//0.064266;
 	static double STEP_SIZE = 30*360/(Math.PI*2*6000000);
 	private Position getPosition() {
 		if (lastPosition==null) {
 			lastPosition = new Position();
 			lastPosition.setLatitude(INITIAL_LATITUDE);
 			lastPosition.setLongitude(INITIAL_LONGITUDE);
 			lastPosition.setElevation(0.0);
 		}
 		lastPosition.setLatitude(lastPosition.getLatitude()+(random.nextDouble()-0.5)*STEP_SIZE);
 		lastPosition.setLongitude(lastPosition.getLongitude()+(random.nextDouble()-0.5)*STEP_SIZE);
 		Position pos = new Position();
 		pos.setLatitude(lastPosition.getLatitude());
 		pos.setLongitude(lastPosition.getLongitude());
 		pos.setElevation(0.0);
 		return pos;
 	}
 	private void updatePlayer() throws IOException {
 		Player player = new Player();
 		Position pos = getPosition();
 		player.setPosition(pos);
 		log("updatePlayer()");
 		// relying on this being handled as a special case - no old value, no ID!
 		client.queueMessage(client.updateFactMessage(null, player), null);
 	}
 	
 	private Player getPlayer() {
 		List<Object> ps = client.getFacts(Player.class.getName());
 		if (ps.size()==0)
 			return null;
 		return (Player)ps.get(0);
 	}
 	private void createMemberIfPossible() throws IOException {
 		Player player = getPlayer();	
 		if (player==null || player.getNewMemberQuota()<=0)
 			return;
 		System.out.println("Creating member");
 		Member m = new Member();
		m.setPlayerID(player.getID());
 		m.setAction(random.nextInt() % 8);
 		m.setBrains(random.nextInt() % 8);
 		m.setWealth(random.nextInt() % 8);
 		m.setHealth(2 + (random.nextInt() % 3));
 		m.setName(playerName);
 		m.setPosition(getPosition());
 		Zone zone = ZoneService.getZone(client, m.getPosition().getLatitude(), m.getPosition().getLongitude());
 		m.setZone(zone!=null ? zone.getOrgId() : 0);
 		m.setCarried(false);
 		m.setColourRef(player.getColourRef());
 		StringBuilder b = new StringBuilder();
 		for (int l=0; l<5; l++) {
 			for (int j=0; j<2; j++) {
 				b.append((random.nextDouble()-0.5)*400);
 				b.append(",");
 			}
 			for (int j=0; j<2; j++) {
 				b.append((random.nextDouble()-0.5)*100*(l==0 ? 2 : 1));
 				b.append(",");
 			}
 		}
		m.setLimbData(b.toString());
 		
 		client.queueMessage(client.addFactMessage(m), null);
 	}
 	
 	private void log(String string) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	private void doSendQueuedMessages() {
 		// TODO Auto-generated method stub
 		try {
 			if (client.isQueuedMessage())
 			{
 				setClientStatus(ClientStatus.POLLING, "Trying to send queued updates");	
 				client.sendQueuedMessages();
 				// success = good
 				setClientStatus(ClientStatus.IDLE, "Ready to play");			
 			}
 		}
 		catch (Exception e) {
 			Log.e(TAG, "Sending queued messages", e);
 			setClientStatus(ClientStatus.ERROR_AFTER_STATE, "Could not send updates\n("+e.getMessage()+")");
 			return;						
 		}
 		
 	}
 
 	private void log(String string, String string2, String xmlText) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private void setClientStatus(ClientStatus errorInServerUrl, String string) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
