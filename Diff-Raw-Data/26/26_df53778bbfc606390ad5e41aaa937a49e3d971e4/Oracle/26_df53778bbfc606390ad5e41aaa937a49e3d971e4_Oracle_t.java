 package experiments.current;
 
 import core.protocols.p2p.Node;
 import core.protocols.transport.ITransport;
 import core.protocols.transport.socket.request.RequestHandler;
 import core.protocols.transport.socket.server.SocketImpl;
 
 public class Oracle {
 
 	public final static int PUT = 10;
 	public final static int GET = 20;
 
 	/**
 	 * main Oracle
 	 * @param args
 	 */
 	public static void main (String args[]){
 		ITransport transport = new SocketImpl(0, 10, RequestHandler.class.getName(),
 				10, 1, 50, null);
		try {
			if(args[0].equals("put")){
				transport.sendRequest(args[3] + "," + PUT + "," + args[1] + "," + args[2],
						new Node(args[4], Integer.parseInt(args[5])));
				System.out.println("put(" + args[1] + ", " + args[2] + ")");
			} else if(args[0].equals("get")){
				String res = transport.sendRequest(args[2] + "," + GET + "," + args[1],
						new Node(args[3], Integer.parseInt(args[4])));
				if(res != null && res.split("\\.").length == 1 && !res.equals("null")){
					for(String arg : res.split("\\*\\*\\*\\*")){
						if(!arg.equals("null") && !arg.equals("") && arg != null){
 							System.out.println(arg);
 							break;
						}
 					}
 				}
 			}
		} catch(Exception e) {
 			System.out.println("Oracle put <key> <value> <networkID> <address> <port>");
 			System.out.println("Oracle get <key> <networkID> <address> <port>");
 		}
 		System.exit(0);
 	}
 }
