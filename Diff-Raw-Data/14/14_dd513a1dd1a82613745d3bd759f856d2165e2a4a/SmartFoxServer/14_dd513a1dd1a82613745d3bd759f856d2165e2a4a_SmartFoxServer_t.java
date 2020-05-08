 package Server;
 
 import java.net.InetAddress;
 import java.net.URL;
 import java.util.Map;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import Application.GameSettings;
 import Application.MatchManager;
 import Application.MatchManager.Direction;
 import Screens.AcceptScreen;
 import Screens.ConfirmScreen;
 import Screens.FriendsListScreen;
 import Screens.InviteScreen;
 import Screens.MultiplayerScreen;
 
 import com.badlogic.gdx.math.Vector3;
 import com.smartfoxserver.v2.entities.data.ISFSArray;
 import com.smartfoxserver.v2.entities.data.ISFSObject;
 import com.smartfoxserver.v2.entities.data.SFSArray;
 import com.smartfoxserver.v2.entities.data.SFSObject;
 import com.smartfoxserver.v2.exceptions.SFSException;
 import sfs2x.client.SmartFox;
 import sfs2x.client.core.BaseEvent;
 import sfs2x.client.core.IEventListener;
 import sfs2x.client.core.SFSEvent;
 import sfs2x.client.requests.ExtensionRequest;
 import sfs2x.client.requests.LoginRequest;
 import sfs2x.client.requests.LogoutRequest;
 
 public class SmartFoxServer implements IEventListener {
 
 	private static final String SFS_ZONE = "FrozenWars";
 	private static final String SFS_RegisterZone = "Register";
 	private SmartFox sfsClient;
 	private MatchManager manager;
 	private static SmartFoxServer instance;
 	private long delayTime;
 	private int myPlayerId;
 	private String lastUserName;
 	private String lastPass;
 	private boolean loggedIn;
 	
 	public static SmartFoxServer getInstance() {
 		if (instance == null) instance = new SmartFoxServer();
 		return instance;
 	}
 	
 	public static boolean isInstanced(){
 		return instance!=null;
 	}
 	
 	public boolean isLoggedIn() {
 		return loggedIn;
 	}
 	
 	public void getTimeResponse(ISFSObject response){
 		long time = response.getLong("time");
 		this.delayTime = System.currentTimeMillis() - time;
 	}
 	
 	public void getTimeRequest(){
 		ExtensionRequest request = new ExtensionRequest("GetTime",null);
 		sfsClient.send(request);
 	}
 	
 	public SmartFoxServer(){
 		loggedIn = false;
 		instance = this;
 		myPlayerId = -999;
 		String ip = getServerIP();
 		sfsClient = new SmartFox(false);
 		while(!sfsClient.isConnected()){
 			sfsClient.connect(ip,9933);
 		}
 		addEventListeners();
 	}
 	
 	public void addManager(MatchManager manager){
 		this.manager = manager;
 	}
 	
 	
 	public int getMyPlayerId(){
 		return myPlayerId;
 	}
 	
 	private void addEventListeners() {
 		sfsClient.addEventListener(SFSEvent.CONNECTION, this);
 		sfsClient.addEventListener(SFSEvent.LOGIN, new IEventListener(){
 
 			public void dispatch(BaseEvent arg0) throws SFSException {
 				getTimeRequest();
 			}
 			
 		});
 		
 		sfsClient.addEventListener(SFSEvent.LOGIN_ERROR,new IEventListener(){
 
 			public void dispatch(BaseEvent event) throws SFSException {
 				Map<String,Object>args=event.getArguments();
 				Short err=(Short) args.get("errorCode");
 				Short error=6;
 				if (!(error==err))
 					AcceptScreen.getInstance().setNewAcceptScreen("NamePassNotValid", "");
 				else
 					AcceptScreen.getInstance().setNewAcceptScreen("AlreadyLogged", "");
 			}
 			
 		});
 		sfsClient.addEventListener(SFSEvent.USER_ENTER_ROOM, this);
 		sfsClient.addEventListener(SFSEvent.USER_EXIT_ROOM, this);
 		sfsClient.addEventListener(SFSEvent.EXTENSION_RESPONSE, new IEventListener(){
 
 			@Override
 			public void dispatch(BaseEvent event) throws SFSException {
 				Map<String, Object> r = event.getArguments();
 				ISFSObject response = (ISFSObject)r.get("params"); //Gets the ISFSObject from the BaseEvent
 				String cmd = (String) r.get("cmd"); 
 				//Executes the response methods according to the requests sent.
 				if(cmd.equals("GetConnectedFriends"))
 					getConnectedFriendsResponse(response);
 				else if (cmd.equals("Invite"))
 					inviteResponse(response);
 				else if (cmd.equals("InviteFail"))
 					inviteFail(response);
 				else if (cmd.equals("AcceptedWaiting"))
 					acceptedWaiting(response);
 				else if (cmd.equals("RefusedWaiting"))
 					refusedWaiting(response);
 				else if (cmd.equals("AcceptedRefused"))
 					acceptedRefused(response);
 				else if (cmd.equals("ModExternalPlayers"))
 					modExternalPlayersResponse(response);
 				else if (cmd.equals("ModeChange"))
 					modeChangeResponse(response);	
 				else if (cmd.equals("GameFull"))
 					gameFullResponse(response);	
 				else if (cmd.equals("LeaderLeft"))
 					leaderLeftResponse(response);	
 				else if (cmd.equals("startGame"))
 					insertInQueuesResponse(response);
 				else if (cmd.equals("GameMessage"))
 					gameMessage(response);
 				else if (cmd.equals("modInQueue"))
 					modInQueueResponse(response);
 				else if (cmd.equals("GameNotFound"))
 					gameNotFound(response);
 				else if (cmd.equals("DisconectedOnGame"))
 					DisconectedOnGame(response);
 				else if (cmd.equals("putHarpoon"))
 					getHarpoon(response);
 				else if (cmd.equals("getTime"))
 					getTimeResponse(response);
 				else if (cmd.equals("dbRegister"))
 					registerResponse(response);
 				else if(cmd.equals("FriendRequestRes"))
 					AcceptScreen.getInstance().setNewAcceptScreen("AddFriendAdder", ((ISFSObject)r.get("params")).getUtfString("res"));
 				else if(cmd.equals("BeFriends?"))
 					beFriends(response);
 				else if(cmd.equals("AddFriendRes"))
 					AcceptScreen.getInstance().setNewAcceptScreen("AddFriendAdded", ((ISFSObject)r.get("params")).getUtfString("res"));
 				else if(cmd.equals("ConnectRes"))
 					connectRes(response);
 				else if(cmd.equals("asignaMejoras"))
 					asignaMejoras(response);
 				else if(cmd.equals("getMyFriendsRequestRes"))
 					getMyFriendsRequestRes(response);
 				else if(cmd.equals("ExitGameRes"))
 					exitGameRes(response);
 				else if (cmd.equals("OutOfQueue"))
 					userOutOfQueue(response);
 				else if(cmd.equals("NamesGame"))
 					NamesGame(response);
 			}
 
 		});
 	}
 
 	private String getServerIP() {
 		String ip = "";
 		try {
 			InetAddress address = InetAddress.getByName(new URL("http://boomwars-server.no-ip.org").getHost());
 			ip = address.getHostAddress();
			//ip="192.168.1.38";
 		} catch (Exception e){
 			
 		}
 		return ip;
 	}
 
 	public void conectaSala(String user,String pword){
 		try {
 			Thread.sleep(500);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		sfsClient.send(new LoginRequest(user,pword, SFS_ZONE));
 		lastUserName = user;
 		lastPass = pword;
 
 	}
 	
 	public void connectRes(ISFSObject response) {
 		if (response.getUtfString("Response").equals("Success")){
 			MultiplayerScreen.getInstance().setMyName(sfsClient.getMySelf().getName());
 			loggedIn = true;
   			GameSettings.getInstance().setUserName(lastUserName);
   			GameSettings.getInstance().setUserPassword(lastPass);
   			MultiplayerScreen.getInstance().setChangeToThis(true);
         	ExtensionRequest request2 = new ExtensionRequest("GetFriendsRequests",new SFSObject());
 			sfsClient.send(request2);
 		}
 	}
 
 	public void sendMove(Direction dir, int myPlayerId,Vector3 position) {
 		int dirCod = -1;
 		if (dir.equals(Direction.left)) dirCod = 0;
 		else if (dir.equals(Direction.right)) dirCod = 1;
 		else if (dir.equals(Direction.up)) dirCod = 2;
 		else if (dir.equals(Direction.down)) dirCod = 3;
 		ISFSObject params = new SFSObject();
 		params.putUtfString("message", "M"+Integer.toString(dirCod)+Integer.toString(myPlayerId)+"X"+ position.x +"Y" + position.y);
 		ExtensionRequest request = new ExtensionRequest("GameMessage",params);
 		sfsClient.send(request);
 			
 	}
 	
 	public void sendLance(int x,int y) {
 		ISFSObject params = new SFSObject();
 		params.putUtfString("message", "H"+"X"+x+"Y"+y);
 		ExtensionRequest request = new ExtensionRequest("GameMessage",params);
 		sfsClient.send(request);
 	}
 	
 	public void getConnectedFriendsResponse(ISFSObject params){
 		Vector<String> friends = new Vector<String>();
 		for(int i= 0;i<params.getSFSArray("ConnectedFriends").size();i++)
 			friends.add((String) params.getSFSArray("ConnectedFriends").getElementAt(i));//Builds the friends array.
 		InviteScreen.getInstance().setNotInvited(friends); //Sends the array to the InviteScreen instance.
 	}
 	
 	public void getConnectedFriendsRequest(){
 		 ExtensionRequest request = new ExtensionRequest("GetConnectedFriends",null);
 		 sfsClient.send(request);//Executes the request.
 	}
 	
 	public void inviteRequest(String name){
 		 ISFSObject params = new SFSObject();
 		 params.putUtfString("Invited", name);
		 params.putInt("mode",MultiplayerScreen.getInstance().getGameMode());
 		 ExtensionRequest request = new ExtensionRequest("Invite",params);
 		 sfsClient.send(request);
 	}
 	
 	public void inviteResponse(ISFSObject response){
 		String inviter = response.getUtfString("Inviter");
 		ConfirmScreen.getInstance().setNewConfirmScreen("InviteGame", inviter);
 	}
 
 
 	public void inviteFail(ISFSObject response){
 		String invited = response.getUtfString("Invited");
 		ConfirmScreen.getInstance().setNewConfirmScreen("InvitedDisconnected", invited);
 	}
 	
 	public void acceptRequest(String inviter) {
 		this.groupExitRequest(MultiplayerScreen.getInstance().getGameAdmin());
 		
 		ISFSObject params = new SFSObject();
 		params.putUtfString("Inviter", inviter);
 		ExtensionRequest request = new ExtensionRequest("Accept",params);
 		sfsClient.send(request);
 	}
 	
 	
 	private void acceptedWaiting(ISFSObject params) {
 	
 		Vector<String> accepted = new Vector<String>();
 		for(int i= 0;i<params.getSFSArray("acceptedPlayers").size();i++)
 			accepted.add((String) params.getSFSArray("acceptedPlayers").getElementAt(i));//Builds the friends array.
 		
 		Vector<String> waiting = new Vector<String>();
 		for(int i= 0;i<params.getSFSArray("waitingPlayers").size();i++)
 			waiting.add((String) params.getSFSArray("waitingPlayers").getElementAt(i));//Builds the friends array.
 		
 		MultiplayerScreen.getInstance().setAcceptedPlayers(accepted);
 		MultiplayerScreen.getInstance().setWaitingPlayers(waiting);
 		
 	}
 	
 	public void acceptedRefused(ISFSObject params) {
 		Vector<String> accepted = new Vector<String>();
 		for(int i= 0;i<params.getSFSArray("acceptedPlayers").size();i++)
 			accepted.add((String) params.getSFSArray("acceptedPlayers").getElementAt(i));//Builds the friends array.
 		
 		Vector<String> refused = new Vector<String>();
 		for(int i= 0;i<params.getSFSArray("refusedPlayers").size();i++)
 			refused.add((String) params.getSFSArray("refusedPlayers").getElementAt(i));//Builds the friends array.
 		
 		MultiplayerScreen.getInstance().setAcceptedPlayers(accepted);
 		MultiplayerScreen.getInstance().setRefusedPlayers(refused);
 		
 	}
 	
 	private void refusedWaiting(ISFSObject response) {
 		
 		Vector<String> refused = new Vector<String>();
 		for(int i= 0;i<response.getSFSArray("refusedPlayers").size();i++)
 			refused.add((String) response.getSFSArray("refusedPlayers").getElementAt(i));//Builds the friends array.
 		
 		Vector<String> waiting = new Vector<String>();
 		for(int i= 0;i<response.getSFSArray("waitingPlayers").size();i++)
 			waiting.add((String) response.getSFSArray("waitingPlayers").getElementAt(i));//Builds the friends array.
 		
 		MultiplayerScreen.getInstance().setRefusedPlayers(refused);
 		MultiplayerScreen.getInstance().setWaitingPlayers(waiting);
 		
 	}
 		
 	public void insertInQueuesResponse(ISFSObject response){
 		myPlayerId = response.getInt("id");
 		MultiplayerScreen.getInstance().setEmpiezaPartida(true);
 	} 
 
 	public void insertInQueuesRequest(Vector<String> names, boolean externalPlayers){
 		if(names.size()==1){		  //El que lanza la partida debe estar en la posicion 0 del vector.
			  SFSObject params = new SFSObject();
			  params.putInt("mode",MultiplayerScreen.getInstance().getGameMode());  
			  ExtensionRequest request2 = new ExtensionRequest("meter1",params);
 			  sfsClient.send(request2);
 		}
 		else if(names.size()==2 && externalPlayers==true){
 			  String friend=names.get(1);
 			  SFSObject params = new SFSObject();
 			  params.putUtfString("pfriend1",friend);
 			  ExtensionRequest request2 = new ExtensionRequest("meter2",params);
 			  sfsClient.send(request2);
 		}
 		else if(names.size()==3 && externalPlayers==true){
 			  String friend1=names.get(1);
 			  String friend2=names.get(2);
 			  SFSObject params = new SFSObject();
 			  params.putUtfString("pfriend1", friend1);
 			  params.putUtfString("pfriend2", friend2);
 			  ExtensionRequest request2 = new ExtensionRequest("meter3",params);
 			  sfsClient.send(request2);
 		}
 		else if(names.size()==4 || externalPlayers==false){
 			String friend1;
 			String friend2;
 			String friend3;
 			SFSObject params = new SFSObject();
 			if(names.size()>1){
 				friend1=names.get(1);
 				params.putUtfString("pfriend1", friend1);
 			}
 			if(names.size()>2){
 				friend2=names.get(2);
 				params.putUtfString("pfriend2", friend2);
 			}
 			if(names.size()>3){
 				friend3=names.get(3);
 				params.putUtfString("pfriend3", friend3);
 			}
 			  ExtensionRequest request2 = new ExtensionRequest("lanzarPartida",params);
 			  sfsClient.send(request2);
 		}
 	}
 	
 	public void register(String user,String email, String pword, String confpword){ //method that sends the user that wants to regist
 		if (!user.equals("")){
 			String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 			Pattern pattern = Pattern.compile(EMAIL_PATTERN);
 			Matcher matcher = pattern.matcher(email);
 			if (matcher.matches()){
 				if (pword.length()>=4 && pword.length()<=8){
 					if (pword.equals(confpword)){
 						sfsClient.send(new LoginRequest("","", SFS_RegisterZone));
 						SFSObject params = new SFSObject();
 						params.putUtfString("name", user);
 						params.putUtfString("pword", pword);
 						params.putUtfString("email", email);
 						lastUserName = user;
 						lastPass = pword;
 						sfsClient.send(new ExtensionRequest("register",params));
 					}else{
 						AcceptScreen.getInstance().setNewAcceptScreen("DiffPasswords", "");
 					}
 				}else{
 					AcceptScreen.getInstance().setNewAcceptScreen("PasswordChars", "");
 				}
 			}else{
 				AcceptScreen.getInstance().setNewAcceptScreen("Email", "");
 			}
 		}else{
 			AcceptScreen.getInstance().setNewAcceptScreen("Username", "");
 		}
 	}
 	
 	public void registerResponse(ISFSObject response){ //response of the regist call
 		String str=response.getUtfString("res");
 		AcceptScreen.getInstance().setNewAcceptScreen("RegisterSuccess", str);
 		if (str.equals("Registered")){
 			GameSettings.getInstance().setUserName(lastUserName);
   			GameSettings.getInstance().setUserPassword(lastPass);
 		}
 		
 		sfsClient.send(new LogoutRequest());
 	}
 	
 	public void modInQueueResponse(ISFSObject response){
 		MultiplayerScreen.getInstance().setInQueue(true);
 	} 
 	
	public void removeOfQueue(int playersNumber, int mode){//Funcion invocada por el usuario que quiere salirse de cola.
 		SFSObject params = new SFSObject();
 		params.putInt("numJugadores", playersNumber);//playersNumber es el numero de amigos con los que metiste cola(incluido el que lanza).
		params.putInt("mode", mode);
 		ExtensionRequest request2 = new ExtensionRequest("sacardecola",params);
 		sfsClient.send(request2);
 	}
 	
 	public void refuseRequest(String inviter) {
 		ISFSObject params = new SFSObject();
 		params.putUtfString("Inviter", inviter);
 		ExtensionRequest request = new ExtensionRequest("Refuse",params);
 		sfsClient.send(request);
 	}
 	
 	public void modeChangeRequest(int mode){
 		ISFSObject params = new SFSObject();
 		params.putInt("mode", mode);
 		ExtensionRequest request = new ExtensionRequest("ModeChange",params);
 		sfsClient.send(request);
 	}
 	
 	public void modeChangeResponse(ISFSObject response){
 		int mode = response.getInt("mode");
 		MultiplayerScreen.getInstance().setGameMode(mode);	
 	}
 	
 	public void modExternalPlayersRequest(boolean external){
 		ISFSObject params = new SFSObject();
 		params.putBool("externalPlayers", external);
 		ExtensionRequest request = new ExtensionRequest("ModExternalPlayers",params);
 		sfsClient.send(request);
 	}
 	
 	public void modExternalPlayersResponse(ISFSObject response){
 		Boolean external = response.getBool("externalPlayers");
 		MultiplayerScreen.getInstance().setExternalPlayers(external);
 	}
 	
 	public void gameFullResponse(ISFSObject response) {
 		AcceptScreen.getInstance().setNewAcceptScreen("FullTeam", "");
 	}
 	
 	public void groupExitRequest(String name){
 		//The name contains the leaders name.
 		ISFSObject params = new SFSObject();
 		params.putUtfString("Inviter", name);
 		ExtensionRequest request = new ExtensionRequest("ExitGroup",params);
 		sfsClient.send(request);
 		 MultiplayerScreen.getInstance().setDefault();
 		
 	}
 	
 	public void gameMessage(ISFSObject response){
 		String message = response.getUtfString("message").toString();
 		if (message.charAt(0)=='M'){
 			Direction dir = null;
 			if (message.charAt(1)=='0') dir = Direction.left;
 			else if (message.charAt(1)=='1') dir = Direction.right;
 			else if (message.charAt(1)=='2') dir = Direction.up;
 			else if (message.charAt(1)=='3') dir = Direction.down;
 			String text = (message.substring(2,3));
 			int jugador = Integer.parseInt(text);
 			int xPosition = 0;
 			int yPosition = 0;
 			boolean found = false;
 			int i = 0;
 			while (i<message.length() & !found){
 				found = (message.charAt(i)=='X');
 				if (found) xPosition = i;
 				else i++;
 			}
 			i = 0;
 			found = false;
 			while (i<message.length() & !found){
 				found = (message.charAt(i)=='Y');
 				if (found) yPosition = i;
 				else i++;
 			}
 			xPosition++;
 			float xPlayerPosition = Float.parseFloat(message.substring(xPosition, yPosition));
 			yPosition++;
 			float yPlayerPosition = Float.parseFloat(message.substring(yPosition));
 			manager.movePlayerEvent(dir,jugador,xPlayerPosition,yPlayerPosition);
 		}		
 	}
 	
 	private void DisconectedOnGame(ISFSObject response){
 		System.out.println(response.getInt("id"));
 	}
 	
 	private void leaderLeftResponse(ISFSObject response) {
 		AcceptScreen.getInstance().setNewAcceptScreen("LeaderLeft", "");
 	}
 	
 	private void gameNotFound(ISFSObject response) {
 		AcceptScreen.getInstance().setNewAcceptScreen("GameNotFound", "");
 	} 
 	
 	
 	public String getMyName() {
 		return sfsClient.getMySelf().getName();		
 	}
 	
 	public void putHarpoon(int x,int y,int range, int myPlayerId){
 		SFSObject params = new SFSObject();
 		params.putInt("range", range);
 		params.putInt("x", x);
 		params.putInt("y", y);
 		params.putInt("playerId",myPlayerId);
 		sfsClient.send(new ExtensionRequest("putHarpoon",params));
 	}
 	
 	public void getHarpoon(ISFSObject response){
 		long time=response.getLong("time");		
 		int x=response.getInt("x");
 		int y=response.getInt("y");
 		int range=response.getInt("range");
 		int playerId = response.getInt("playerId"); 
 		manager.putHarpoonEvent(x,y,range,playerId,time+delayTime);
 	}
 	
 	public void beFriends(ISFSObject params){
 		SFSObject params2 = new SFSObject();
 		if (params.getSFSArray("Friends")!=null){
 			for(int i= 0;i<params.getSFSArray("Friends").size();i++){
 				ConfirmScreen.getInstance().setNewConfirmScreen("BeFriends?", ((String) params.getSFSArray("Friends").getElementAt(i)));
 			}
 		}
 		if (params.getSFSArray("Friends2")!=null){
 			for(int i=0;i<params.getSFSArray("Friends2").size();i++){
 				AcceptScreen.getInstance().setNewAcceptScreen("AcceptedFriendRequest", ((String) params.getSFSArray("Friends2").getElementAt(i)));
 				params2.putUtfString("friend", ((String) params.getSFSArray("Friends2").getElementAt(i)));
 				ExtensionRequest request2 = new ExtensionRequest("ViewedConfFriend",params2);
 				sfsClient.send(request2);
 			}
 		}
 	}
 	
 	public void sendBeFriends(String conf,String user){
 		SFSObject params2 = new SFSObject();
 		params2.putUtfString("friend", user);
 		params2.putUtfString("res", conf);
 		ExtensionRequest request2 = new ExtensionRequest("AddFriend",params2);
 		sfsClient.send(request2);
 	}
 	
 	public void sendFriendRequest(String friend){
 		 if(!sfsClient.getMySelf().getName().equals(friend)){
 			  SFSObject params = new SFSObject();
 			  params.putUtfString("friend", friend);
 			  ExtensionRequest request2 = new ExtensionRequest("FriendRequest",params);
 			  sfsClient.send(request2);
 		 }else{
 			  AcceptScreen.getInstance().setNewAcceptScreen("AddFriendAdder", "CantAddYourself");
 		 }
 	}
 	
 	public void  sendAsign(int numBarriles,int maxBootUpgrades,int maxRangeUpgrades,int maxNumHarpoonsUpgrades, int maxThrowUpgrades){
 		SFSObject params = new SFSObject();
 	     ISFSArray array = new SFSArray();
 	     array.addInt(maxBootUpgrades);
 	     array.addInt(maxRangeUpgrades);
 	     array.addInt(maxNumHarpoonsUpgrades);
 	     array.addInt(maxThrowUpgrades);      
 	     params.putInt("numBarriles", numBarriles);
 	     params.putSFSArray("arraymejoras", array);
 	     ExtensionRequest request = new ExtensionRequest("AsignaMejoras",params);
 		 sfsClient.send(request);
 	}
 
 	public void asignaMejoras(ISFSObject params) {
 		while ((manager==null) || (manager.getLoadingScreen()==null));
 		 int numBarriles=params.getInt("nBarriles");
 		 int upgrades[] = new int[numBarriles];
 		 for(int i=0;i<numBarriles;i++){
 			 upgrades[i]= (params.getSFSArray("arrayBarriles").getInt(i));
 		 }
 		 int numPlayers = params.getInt("numPlayers");
 		 manager.startGame(upgrades,numPlayers);
 	}
 	
 	private void NamesGame(ISFSObject response) {
 		int numPlayers = response.getInt("n");
 		String[] usersName = new String[numPlayers];
 		for(int i=1;i<=numPlayers;i++){
 			String name=response.getUtfString("name"+i);
 			int id=response.getInt("id"+i);
 			usersName[id-1] = name;
 		}
 		Application.MatchManager.setUserName(usersName);
 	}
 
 	public void getMyFriendsRequest() {
 		 ExtensionRequest request = new ExtensionRequest("GetFriends",null);
 		 sfsClient.send(request);
 	}
 	
 	private void getMyFriendsRequestRes(ISFSObject params) {
 		Vector<String> connectedFriends = new Vector<String>();
 		Vector<String> disconnectedFriends = new Vector<String>();
 		Vector<String> playingFriends = new Vector<String>();
 		
 		//TODO GONZALO. PLAYING AND CONNECTED FRIENDS ARE SAVED SWITCHING THE NAMES OF THE ARRAYS.
 		//connected friends
 		for(int i= 0;i<params.getSFSArray("ConnectedFriends").size();i++){
 			playingFriends.add((String) params.getSFSArray("ConnectedFriends").getElementAt(i));
 		}
 		//disconnected friends
 		for(int i= 0;i<params.getSFSArray("DisconnectedFriends").size();i++){
 			disconnectedFriends.add((String)params.getSFSArray("DisconnectedFriends").getElementAt(i));
 		}
 		//friends gaming
 		for(int i= 0;i<params.getSFSArray("PlayingFriends").size();i++){
 			connectedFriends.add((String)params.getSFSArray("PlayingFriends").getElementAt(i));
 		}
 		FriendsListScreen.getInstance().updateFriends(playingFriends, connectedFriends, disconnectedFriends);
 	}
 	
 	/**method when you want to exit from the game*/
 	public void exitGame(){
 		ExtensionRequest request2 = new ExtensionRequest("ExitGame",null);
 		sfsClient.send(request2);
 	}
 	
 	/**response of the server when you want to exit from the game*/
 	private void exitGameRes(ISFSObject response) {
 		if (response.getUtfString("res")=="Success"){
 			//TODO fede when you have exit from the game are you are now in the lobby show a message? and go to the multiplayer screen?
 		}else if (response.getUtfString("res")=="Error"){
 			//TODO show a error message (Like the ones of adding friends)
 		}	
 	}
 	
 	private void userOutOfQueue(ISFSObject response) {
 		AcceptScreen.getInstance().setNewAcceptScreen("UserOutOfQueue", response.getUtfString("playerName"));
 		
 	}
 	
 	public void dispatch(BaseEvent event) throws SFSException {
 	}
 	
 	public void disconnect(){
 		if (sfsClient.isConnected()) sfsClient.disconnect();
 		sfsClient = null;
 	}
 
 	public void dispose() {
 		disconnect();
 		instance = null;
 	}
 
 	
 	
 }
