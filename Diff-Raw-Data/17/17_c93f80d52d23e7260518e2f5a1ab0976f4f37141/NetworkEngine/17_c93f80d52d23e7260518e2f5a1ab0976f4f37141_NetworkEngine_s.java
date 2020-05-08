 package server;
 
 import net.Protocol;
 
 public class NetworkEngine {
 	public static final int BCAST_BUF_SIZE = 16384;
 	public static final int TCP_BUF_SIZE   = 1024;
 	
 	public static final int BCAST_PORT        = 49725;
 	public static final int TCP_PORT          = 49726;
 	public static final int TCP_CLIENT_PORT   = 49727;
 	
 	private Broadcaster bcaster;
 	private Server gameserver;
 	private Protocol prot;
 	private TcpListener tcpControl;
 	
 	public NetworkEngine(Server gs) {
 		gameserver = gs;
 		prot = new Protocol();
 		bcaster = new Broadcaster(BCAST_PORT);
 		
 		tcpControl = new TcpListener(TCP_PORT, gameserver);
 		tcpControl.start();
 	}
 	
 	public void addPlayer(int id, String ip) {
 		bcaster.addIP(id, ip);
 	}
 	
 	/**
 	 * Returns IP address of the client specified by id, if there is no such client returns null
 	 * @param id
 	 * @return
 	 */
 	public String getIPbyID(int id) {
		System.out.println(bcaster.ipList.values());
 		if (bcaster.ipList.containsKey(id))
 			return bcaster.ipList.get(id);
 		return null;
 	}
 	
 	public void update() {
 		bcaster.spam(prot.encode(gameserver.gameState.getNetState()));
 		
 		/* Action events such as explosions are sent only once */
 		gameserver.gameState.getNetState().clearActions();
 	}
 }
