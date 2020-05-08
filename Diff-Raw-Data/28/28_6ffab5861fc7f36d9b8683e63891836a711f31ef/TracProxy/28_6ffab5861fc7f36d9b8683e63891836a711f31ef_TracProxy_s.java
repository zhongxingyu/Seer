 package org.mozeq.Trac;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
 
 public class TracProxy {
 	XmlRpcClient client = null;
 	String projectURL = null;
 
 	public TracProxy(String serverURL, String project) {
 		this.projectURL = serverURL + ((serverURL.endsWith("/") || project == null) ? "" : "/") + (project != null ? project: "");
 	}
 
 	public void connect(String name, String pass) throws MalformedURLException, XmlRpcException {
 		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    	String loginURL = projectURL+"/login/xmlrpc";
 		config.setServerURL(new URL(loginURL));
 		config.setBasicUserName(name);
 		config.setBasicPassword(pass);
	    client = new XmlRpcClient();
	    client.setConfig(config);
 	}
 
 	public String getProjectURL() {
 		return projectURL;
 	}
 
 	public String[] getApiVersion() throws XmlRpcException {
	    Object[] params = new Object[]{};
 		Object[] result = (Object[])client.execute("system.getAPIVersion", params);
 
	    if (result != null)
	    	for(Object o: result){
	    		System.out.println(o);
	    	}
 
	    return (String[])result;
 
 	}
 
 	public ArrayList<Ticket> getTickets(String query) {
    	System.out.println("getTickets");
 		return null;
    }
 
 	public Ticket getTicket(int ticketID) throws XmlRpcException {
 		Object[] params = { ticketID };
 		//Returns [id, time_created, time_changed, attributes].
 		Object[] result = (Object[])client.execute("ticket.get", params);
 
 		return new Ticket(result);
 	}
 
 }
