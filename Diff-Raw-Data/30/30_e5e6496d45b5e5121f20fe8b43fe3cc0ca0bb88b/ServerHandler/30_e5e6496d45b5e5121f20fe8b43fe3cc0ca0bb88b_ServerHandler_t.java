 package ch.bfh.monopoly.net;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.mina.core.service.IoHandler;
 import org.apache.mina.core.session.IdleStatus;
 import org.apache.mina.core.session.IoSession;
 
 /**
  * This class is used to handle the operations of the server
  * over the network
  * @author snake
  *
  */
 public class ServerHandler implements IoHandler{
 
 	private List<String> usernames = new ArrayList<String>();
 	private List<PlayerWrapper> plWrap = new ArrayList<PlayerWrapper>();
 	private int userindex = 1;
 
 	//used to send the token to the user in position userTokenIndex in plWrap
 	private int userTokenIndex = 0;
 
 	/**
 	 * This inner class is used to wrap the username 
 	 * and the roll value for order into one single 
 	 * object
 	 * @author snake
 	 *
 	 */
 	class PlayerWrapper {
 
 		private String username;
 		private int rollValue;
 		private IoSession session;
 		private boolean turnToken = false;
 
 		public PlayerWrapper(String username, int rollValue, IoSession session){
 			this.username = username;
 			this.rollValue = rollValue;
 			this.session = session;
 		}
 
 
 		public IoSession getSession(){
 			return this.session;
 		}
 
 		public String getUsername() {
 			return username;
 		}
 
 		public int getRollValue() {
 			return rollValue;
 		}
 
 		public void setTurnToken(boolean t){
 			turnToken = t;
 		}
 
 		public boolean hasTurnToken(){
 			return turnToken;
 		}
 
 	}
 
 	/**
 	 * Get the number of opened sessions on this server
 	 * @return an int representing the number of sessions opened
 	 */
 	public int getOpenedSessions(){
 		return this.plWrap.size();
 	}
 
 	/**
 	 * Get the usernames of the players 
 	 * @return a List that contains the player's names
 	 */
 	public List<String> getUsernames(){
 		return Collections.unmodifiableList(usernames);
 	}
 
 	@Override
 	public void exceptionCaught(IoSession arg0, Throwable arg1)
 	throws Exception {
 		System.out.println(arg1.getMessage());
 	}
 
 	/**
 	 * Send a broadcast message to the session of this server
 	 * @param n NetMessage
 	 * 		the NetMessage to broadcast
 	 * @param notBroadcast IoSession
 	 * 		the session where the message should not be sent
 	 */
 	private void sendBroadcast(NetMessage n, IoSession notBroadcast){
 		for(PlayerWrapper pW : plWrap){
 			if(pW.getSession() != notBroadcast)
 				pW.getSession().write(n);
 		}
 	}
 
 	/**
 	 * Send the turn token to the next player
 	 */
 	public void sendTurnToken(){
 		//reset turn token
 		for(PlayerWrapper p : plWrap){
 			p.setTurnToken(false);
 		}
 
 		NetMessage nm = new NetMessage(plWrap.get(userTokenIndex).getUsername(), Messages.TURN_TOKEN);
 		plWrap.get(userTokenIndex).setTurnToken(true);
 
 		System.out.println("===  USER TOKEN INDEX : " + userTokenIndex + " TURN TO PLAYER " + plWrap.get(userTokenIndex).getUsername());
 
 		//if we arrive at the least position reset the counter
 		if((userTokenIndex == plWrap.size()-1))
 			userTokenIndex = 0;
 		else if(userTokenIndex < plWrap.size()-1)
 			userTokenIndex++;
 		else if(userTokenIndex > plWrap.size()-1)
 			userTokenIndex--;
 
 		//broadcast the message to the other players
 		sendBroadcast(nm, null);	
 	}
 
 	/**
 	 * Send the message GAME_START to the client
 	 */
 	public void sendStartGame(Locale loc){
 		NetMessage nm = new NetMessage(this.usernames, loc, Messages.GAME_START);
 		sendBroadcast(nm, null);	
 	}
 
 	/**
 	 * Build the list of user, by ordering the list looking at the
 	 * roll order value and check if there are equals username
 	 * @param n
 	 */
 	private void buildUserList(NetMessage n, IoSession session){
 		String checkedName = n.getString1();
 
 		//if the username sended is equal to one in the wrap list, add a number to it
 		for(int j = 0 ; j < plWrap.size() ; j++){
 			if(n.getString1().equals(plWrap.get(j).getUsername())){
 				checkedName = n.getString1().concat(Integer.toString(userindex));
 				this.userindex++;
 			}
 		}
 
 		this.plWrap.add(new PlayerWrapper(checkedName, n.getInt(), session));
 
 		//bubble sort for sorting players
 		int x = plWrap.size()-1;
 		PlayerWrapper tmp;
 
 		while(x > 0){
 			for (int i = 0; i < x; i++) {                
 				//sort player from the biggest to the lowest roll value
 				if (plWrap.get(i).getRollValue() < plWrap.get(i+1).getRollValue()) {                          
 					tmp = plWrap.get(i);
 
 					plWrap.set(i, plWrap.get(i+1));
 
 					plWrap.set(i+1, tmp);
 				}
 			} 
 
 			x--;
 		}
 
 		this.usernames.clear();
 
 		for(int q = 0 ; q < plWrap.size() ; q++){
 			//TODO remove sysout
 			this.usernames.add(plWrap.get(q).getUsername());
 			System.out.println("USER " + plWrap.get(q).getUsername() + " " + plWrap.get(q).getRollValue());
 		}
 
 		System.out.println("========");
 	}
 
 	/**
 	 * If a user went down, update the username list (for broadcast) 
 	 * turn counter
 	 * @param session
 	 */
 	private void playerQuit(IoSession session){
 		//find the user
 
 
 		for(int j = 0 ; j < usernames.size() ; j++){
 			if(plWrap.get(j).getSession() == session){
 				System.out.println("USER QUIT: " + plWrap.get(j).getUsername());
 
 				//send token the quit game message
 				NetMessage turn = new NetMessage(usernames.get(j), Messages.QUIT_GAME);
 				sendBroadcast(turn, null);
 
 				//wait before sending turn token
 				try {
					Thread.sleep(1500);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 				//if the user who quit has the turn token update it and send a new turn token
 				System.out.println("THE TOKEN IS: " + userTokenIndex);
 				System.out.println("THE J COUNTER IS: " + j);
 
 				if(plWrap.get(j).hasTurnToken()){
 					System.out.println("THE USER HAS THE TOKEN");
 					System.out.println(plWrap.get(j).getUsername());
 
 					System.out.println("WE HAVE TO SEND THE TURN TOKEN TO: ");
 					System.out.println(usernames.get(userTokenIndex));
 
 					//send new turn token
 					sendTurnToken();
 					userTokenIndex--;
 				}
 
 				usernames.remove(j);
 				plWrap.remove(j);
 				
 				//there is only one player, so the game must ends
 				if(usernames.size() == 1){
 					NetMessage winner = new NetMessage(usernames.get(0), Messages.GAME_WINNER);
 					sendBroadcast(winner, null);
 				}
 				
 				break;
 			}
 
 		}
 
 		System.out.println("======================================000");
 		System.out.println("======================================000");
 		System.out.println("======================================000");
 		System.out.println("======================================000");
 		System.out.println("======================================000");
 	}
 
 	@Override
 	public void messageReceived(IoSession arg0, Object arg1) throws Exception {
 		NetMessage n = (NetMessage)arg1;
 
 		System.out.println("ServerHandler Received Message TYPE: " + n.getMessageType());
 
 		if(n.getMessageType() == Messages.SEND_USERNAME)
 			buildUserList(n, arg0);
 		else if(n.getMessageType() == Messages.END_TURN){
 			System.out.println("END TURN RECEIVED");
 			sendTurnToken();
 		}
 		else
 			//when we receive a message, we must broadcast it
 			sendBroadcast(n, arg0);
 
 	}
 
 	@Override
 	public void messageSent(IoSession arg0, Object arg1) throws Exception {
 
 	}
 
 	@Override
 	public void sessionClosed(IoSession arg0) throws Exception {
 		System.out.println("QUIT GAME RECEIVED");
 		playerQuit(arg0);
 		
 		
 		System.out.println("A player went down. Taking his properties and money.");
 
 	}
 
 	@Override
 	public void sessionCreated(IoSession arg0) throws Exception {
 
 	}
 
 	@Override
 	public void sessionIdle(IoSession arg0, IdleStatus arg1) throws Exception {
 
 	}
 
 	@Override
 	public void sessionOpened(IoSession arg0) throws Exception {
 
 	}
 }
