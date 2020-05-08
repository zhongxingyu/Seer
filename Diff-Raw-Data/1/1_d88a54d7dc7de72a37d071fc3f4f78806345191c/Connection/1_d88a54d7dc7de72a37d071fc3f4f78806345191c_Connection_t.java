 package game;
 
 import org.zeromq.ZMQ;
 
 
 public class Connection {
 
 	private String connectmsg = "{ \n\"comm_type\" : \"MatchConnect\",\n\"match_token\" : ";
 	private String endmsg = ",\n\"team_name\" : \"Team 112\"\n\"password\" : \"castlemountain\" }";
 	
 	/**
 	 * TODO: generate initialization
 	 */
 	public Connection(){}
 	
 	/**
 	 * Method to manually connect to test server using 
 	 * Match token and server url.
 	 * @return True if connection was successful.
 	 */
 	public boolean ConnectManual(String matchtoken, String url){
 		ZMQ.Context context = ZMQ.context(1);
 		
 		ZMQ.Socket test = context.socket(ZMQ.REQ);
 		ZMQ.Socket testrep = context.socket(ZMQ.REP);
 		test.bind("tcp://"+url+":5557");
 		testrep.bind("tcp://"+url+":5557");
 		
 		String conmsg = connectmsg+matchtoken+endmsg;
 		test.send(conmsg.getBytes(), 0);
 		byte[] recsig = testrep.recv(0);
 		
 		System.out.println(new String(recsig));
 		
 		return true;
 	}
 	
 	/**
 	 * Method which will automatically connect to test server
 	 * using an http post.
 	 *
 	 * @return True if connection was successful.
 	 */
 	public boolean ConnectAuto(){
 		
 		return true;
 	}
 	
 	
 }
