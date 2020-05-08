 package server.cooproject.itk.hu;
 
 import java.util.HashMap;
 
 import org.apache.log4j.Logger;
 import org.jwebsocket.api.WebSocketPacket;
 import org.jwebsocket.factory.JWebSocketFactory;
 import org.jwebsocket.kit.WebSocketServerEvent;
 import org.jwebsocket.listener.WebSocketServerTokenEvent;
 import org.jwebsocket.listener.WebSocketServerTokenListener;
 import org.jwebsocket.server.TokenServer;
 import org.jwebsocket.token.Token;
 import org.jwebsocket.token.TokenFactory;
 
 
 public class coopMessageListener implements WebSocketServerTokenListener{
 
 	private static TokenServer _tServer = (TokenServer) JWebSocketFactory.getServer("ts0");
 	private static Logger log = Logger.getLogger(coopMessageListener.class.getName());
 	private HashMap<String, String> _users;
 	
 
 	public coopMessageListener() {
 		super();
 		log.info("Coop Server listener successfully loaded");
 		_users = new HashMap<String, String>();
 	}
 
 	@Override
 	public void processClosed(WebSocketServerEvent aEvent) {
 		//Broadcastolj, ha lelep valaki
 		
 		
 	}
 
 	@Override
 	public void processOpened(WebSocketServerEvent aEvent) {
 		//Broadcastolj, ha belep valaki
 		/* A szerzo kommentje:
 		* Mivel semmi authunk nincs, igy fogalmunk sincs hogy mi az uj user neve
 		* Tehat, a kovetkezo lehetosegeink vannak
 		* 1, Hagyjuk igy, hogy csak jelezzuk ha valaki joinol
 		* 2, Lesz egy join type, amire kesobb lehet szurni, es broadcastolni
 		* 3, Az csinaljuk, hogy ha ujkent kerul be hashmapbe akkor kuldunk rol
 		*/
 		//Token dResponse = new Token();
 		//_tServer.broadcastToken(aToken);
 		
 	}
 
 	@Override
 	public void processPacket(WebSocketServerEvent aEvent, WebSocketPacket arg1) {
 		// TODO Auto-generated method stub
 		//keressuk elo user mapbol
 		if(!_users.containsKey(aEvent.getSessionId())){
 			String username = _users.get(aEvent.getSessionId());
 			_users.remove(aEvent.getSessionId());
 			log.info("Deleting record from _users map : "+aEvent.getSessionId()+" - "+username);
 			Token dResponse = TokenFactory.createToken("response");
 			dResponse.setString("type","1000");//chat message
 			dResponse.setString("sender","CooProjectServer");
 			dResponse.setString("message",username+" left the server");//chat message
 			_tServer.broadcastToken(dResponse);
 		}
 	}
 
 	@Override
 	public void processToken(WebSocketServerTokenEvent aEvent, Token aToken) {
 		// Dolgozzuk fel a letezo fieldeket
 		int cType = 0;
 		if(aToken.getString("type") != null){
 			cType = Integer.parseInt(aToken.getString("type"));
 		}
 		String cSenderName = aToken.getString("sender");
 		String cMessage = aToken.getString("message");
 
 		//Loggoljuk
 		log.info("New token received from "+cSenderName+" and the message is "+cMessage);
 		updateUsername(aEvent,cSenderName);//updateljuk a sessionId - nev parost
 		boolean should_be_broadcasted = true;//mindent broadcastolunk, kiveve amit nem :(
 		// dolgozzuk fel type alapjan
 		switch(cType){
 			//Egy chat message. Egyelore csak broadcastoljuk
 			case 1000: should_be_broadcasted = true;
 					   break;
 		
 			//Nem jot kuldott, biztos elnezte. Hat adjuk a tudtara asszertiv kommunikacioval
 			default: handleUnknowTypeField(aEvent,aToken,cType);
 					 break;
 		}
 		//Ha broadcastolni kell
 		if(should_be_broadcasted){
 			_tServer.broadcastToken(aToken);
 		}
 		
 	}
 	
 	
 	/**
 	 * Ismeretlen type field a jsonben valaszoljuk a feladonak.
 	 * @param aEvent A websocketservertokeEvent
 	 * @param aToken Maga a token
 	 * @param cType A hibasnak/ismeretlennek itelt type mezo tartalma
 	 */
 private void handleUnknowTypeField(WebSocketServerTokenEvent aEvent, Token aToken, int cType){
 	log.warn("Message with invalid type field "+cType);
 	Token dResponse = aEvent.createResponse(aToken);
 	//REMOVEME: transition phase miatt
 	dResponse.setString("sender","CooProjectServer");
 	dResponse.setString("message", "Ne haragudj, de elrontottad a type("+cType+") mezo erteket!");
 	aEvent.sendToken(dResponse);
 }
 
 /**
  * Arra szolgal, hogy updatelje a usernevet ha valtozik, vagy hozzaadja a maphez ha meg nincs benne
  * @param aEvent Az event object
  * @param username A jsonbol kinyert username
  */
 private void updateUsername(WebSocketServerTokenEvent aEvent, String username){
 	//REMOVEME: csak ameddig kliens nem kul usernevet
 	if(username == null) username = aEvent.getSessionId();
 	//Ha nincs benne, rakjuk bele es broadcast
 	if(!_users.containsKey(aEvent.getSessionId())){
 		_users.put(aEvent.getSessionId(),username);
 		log.info("New record in _users map : "+aEvent.getSessionId()+" - "+username);
 		Token dResponse = TokenFactory.createToken("response");
 		dResponse.setString("type","1000");//chat message
 		dResponse.setString("sender","CooProjectServer");
 		dResponse.setString("message",username+" joined");//chat message
 		_tServer.broadcastToken(dResponse);
 	}else{
		if(_users.get(aEvent.getSessionId()) != username){
 			log.info("_users map updated with "+aEvent.getSessionId()+" - "+username);
 
 			Token dResponse = TokenFactory.createToken("response");
 			dResponse.setString("type","1000");
 			dResponse.setString("sender","CooProjectServer");
 			dResponse.setString("message",_users.get(aEvent.getSessionId())+" is now known as "+username);
 
 			_tServer.broadcastToken(dResponse);
 
 			_users.put(aEvent.getSessionId(),username);
 		}
 	}
 	
 }
 	
 	
 }
