 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package client;
 
 import java.util.logging.Logger;
 import org.apache.xmlrpc.XmlRpcException;
 import org.apache.xmlrpc.client.XmlRpcClient;
 
 /**
  *
  * @author Alexandru Popescu
  */
 public class Session {
 
 	Logger log = Logger.getLogger(Session.class.getName());
 	XmlRpcClient client;
 	int id;
 
 	public Session(XmlRpcClient rpcClient) {
 		client = rpcClient;
 	}
 
	public int start(String name) throws XmlRpcException {
 		Object[] params = new Object[]{name};
 		try {
 			id = (Integer) client.execute("Session.start", params);
 		} catch (XmlRpcException ex) {
 			log.severe(ex.getMessage());
			throw ex;
 		}
 		return id;
 	}
 
	public boolean end()  {
 		Object[] params = new Object[]{id};
 		boolean ret;
 		try {
 			ret = (Boolean) client.execute("Session.end", params);
 			if (ret) {
 				id = 0;
 			}
 		} catch (XmlRpcException ex) {
 			log.severe(ex.getMessage());
			return false;
 		}
 		return ret;
 	}
 
 	public int getId() {
 		return id;
 	}
 }
