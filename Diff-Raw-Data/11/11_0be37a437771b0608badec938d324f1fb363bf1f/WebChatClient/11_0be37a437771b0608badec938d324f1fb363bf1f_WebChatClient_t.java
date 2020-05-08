 package com.github.tux2323.demo.chat.server;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.osgi.framework.ServiceReference;
 
 import com.github.tux2323.demo.chatserver.ChatClient;
 import com.github.tux2323.demo.chatserver.Session;
 
 public class WebChatClient implements ChatClient {
 	
 	private Session session;
 	
 	private List<String> messages = new ArrayList<String>();
 	
 	private ServiceReference serviceReference;
 	
 	public List<String> pollMessages(){
 		List<String> result = messages;
 		messages = new ArrayList<String>();
 		return result;
 	}
 
 	public void setServiceReference(ServiceReference serviceReference) {
 		this.serviceReference = serviceReference;
 	}
 
 	public ServiceReference getServiceReference() {
 		return serviceReference;
 	}
 
 	public void setSession(Session session) {
 		this.session = session;
 	}
 
 	public Session getSession() {
 		return session;
 	}

	@Override
	public void receiveMessage(String from, String msg) {
		messages.add(msg);
	}
 	
 }
