 package com.voxeo.ozone.web.client;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.time.DateFormatUtils;
 
 import com.voxeo.ozone.client.listener.StanzaAdapter;
 import com.voxeo.servlet.xmpp.ozone.stanza.Error;
 import com.voxeo.servlet.xmpp.ozone.stanza.IQ;
 import com.voxeo.servlet.xmpp.ozone.stanza.Presence;
 
 public class ClientStanzaListener extends StanzaAdapter {
 
 	private Set<String> callIds = new HashSet<String>();
 	
 	public ClientStanzaListener() {
 
 	}
 	
 	@Override
 	public void onIQ(IQ iq) {
 
 		MessagesQueue.publish(new Message(generateMessage(iq.toString()), Message.Type.IN));
 		if (iq.hasExtension()) {
 			if (iq.getExtension().getStanzaName().equalsIgnoreCase("offer")) {
 				callIds.add(getCallId(iq));
 			}
 		}
 	}
 	
 	private String getCallId(IQ iq) {
 
 		return iq.getFrom().substring(0, iq.getFrom().indexOf('@'));
 	}
 
 	@Override
 	public void onError(Error error) {
 
		MessagesQueue.publish(new Message(generateMessage(error.toString()), Message.Type.ERROR));
 	}
 	
 	@Override
 	public void onMessage(com.voxeo.servlet.xmpp.ozone.stanza.Message message) {
 
 		MessagesQueue.publish(new Message(generateMessage(message.toString()), Message.Type.IN));
 	}
 	
 	@Override
 	public void onPresence(Presence presence) {
 
 		super.onPresence(presence);
 		MessagesQueue.publish(new Message(generateMessage(presence.toString()), Message.Type.IN));
 	}
 	
 	public List<String> getAvailableCalls() {
 		
 		return new ArrayList<String>(callIds);
 	}
 	
 	private String generateMessage(String message) {
 		
 		return DateFormatUtils.format(new Date(), "dd/MM/yyyy hh:mm:ss") + ": " + StringEscapeUtils.escapeXml(message);		
 	}
 }
