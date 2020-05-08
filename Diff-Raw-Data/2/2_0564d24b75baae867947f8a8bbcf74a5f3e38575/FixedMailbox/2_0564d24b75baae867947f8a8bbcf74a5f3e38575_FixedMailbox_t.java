 package org.osgi.mailboxes.fixed;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.osgi.mailboxes.api.Mailbox;
 import org.osgi.mailboxes.api.Message;
 
 public class FixedMailbox implements Mailbox {
 	private final List<Message> messages;
 	
 	public FixedMailbox() {
 		messages = Arrays.<Message>asList(
 				new StringMessage(0, "Hi there!", "Welcome to OSGi world!"), 
 				new StringMessage(1, "Modulatiry", "Modularity is esential for developing flexible and maintainable  software!")
 			);
 	}
 	
 	@Override
 	public long[] getAllMessages() {
 		long[] ids = new long[messages.size()];
 		for(int i = 0; i < messages.size(); ++i) {
 			ids[i] = i;
 		}
		return ids;
 	}
 
 	@Override
 	public Message[] getMessages(long[] ids) {
 		Message[] result = new Message[ids.length];
 		for(int i = 0; i < ids.length; ++i) {
 			long id = ids[i];
 			if (id < 0 || id >= messages.size()) {
 				throw new IllegalArgumentException("Invalid id: " + id);
 			}
 			
 			result[i] = messages.get((int) id);
 		}
 		
 		return result;
 	}
 	
 	
 	@Override
 	public long[] getMesagesSince(long id) {
 		if (id < 0 || id >= messages.size()) {
 			throw new IllegalArgumentException("Invalid id: " + id);
 		}
 		
 		int first = (int) id;
 		
 		long[] ids = new long[messages.size() - first];
 		for(int i = 0; i < ids.length; ++i) {
 			ids[i] = i + first;
 		}
 		return ids;
 	}
 }
