 package com.flat502.rox.demo;
 
 import java.net.InetAddress;
import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import com.flat502.rox.marshal.RpcCall;
 import com.flat502.rox.marshal.RpcResponse;
 import com.flat502.rox.marshal.xmlrpc.XmlRpcMethodResponse;
 import com.flat502.rox.server.RpcCallContext;
 import com.flat502.rox.server.SynchronousRequestHandler;
 import com.flat502.rox.server.XmlRpcServer;
 
 /**
  * A demo synchronous server illustrating the {@link com.flat502.rox.server.SynchronousRequestHandler}
  * interface.
  */
 public class SyncServerDemo implements SynchronousRequestHandler {
 	public RpcResponse handleRequest(RpcCall call, RpcCallContext context) throws Exception {
 		Object[] params = call.getParameters();
 		System.out.println("Method [" + call.getName() + "] called with "
 				+ params.length + " parameters");
 		for (int i = 0; i < params.length; i++) {
 			System.out.println("   Param " + (i + 1) + " [" + params[i] + "]");
 		}
 
 		if (call.getName().equals("example.getDate")) {
			TimeInfo timeInfo = new TimeInfo();
			timeInfo.info = "Hello World";
			timeInfo.today = new Date();
			
			TimeInfo a1 = new TimeInfo();
			a1.info = "Hi There";
			timeInfo.array = new TimeInfo[]{a1};
			return new XmlRpcMethodResponse(timeInfo);
 		} else if (call.getName().equals("example.sum")) {
 			Integer sum = new Integer(sum((List)call.getParameters()[0]));
 			return new XmlRpcMethodResponse(sum);
 		} else if (call.getName().equals("example.getInfo")) {
 			throw new NoSuchMethodException();
 		}
 		throw new NoSuchMethodException();
 	}
 	
 	private int sum(List list) {
 		int total = 0;
 		Iterator i = list.iterator();
 		while (i.hasNext()) {
 			total += ((Integer) i.next()).intValue();
 		}
 		return total;
 	}
 
 	/**
 	 * Start an instance of this demo server.
 	 * <p>
 	 * The following XML-RPC methods are supported by
 	 * this server:
 	 * <ul>
 	 * <li>{@link RMIServerInterface#sum(int[]) rmi.sum}</li>
 	 * <li>{@link RMIServerInterface#getDate() rmi.getDate}</li>
 	 * <li>{@link RMIServerInterface#getVersionInfo(boolean) rmi.getVersion}</li>
 	 * </ul>
 	 * @param args
 	 * 	A list of parameters indicating
 	 * 	the <code>host/address</code> and
 	 * 	<code>port</code> to bind to. These default to 
 	 * 	<code>localhost</code> and <code>8080</code> if
 	 * 	not specified.
 	 */
 	public static void main(String[] args) {
 		try {
 			String host = "localhost";
 			int port = 8080;
 
 			if (args != null && args.length > 0) {
 				host = args[0];
 				if (args.length > 1) {
 					port = Integer.parseInt(args[1]);
 				}
 			}
 			System.out.println("Starting server on " + host + ":" + port);
 
 			XmlRpcServer server = new XmlRpcServer(InetAddress.getByName(host), port);
 			server.registerHandler(null, "^example\\.", new SyncServerDemo());
 			server.start();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
