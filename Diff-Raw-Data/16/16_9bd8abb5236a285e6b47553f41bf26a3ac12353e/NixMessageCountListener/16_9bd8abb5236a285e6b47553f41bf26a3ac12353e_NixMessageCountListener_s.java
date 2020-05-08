 package com.brif.nix.listeners;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.event.MessageCountEvent;
 import javax.mail.event.MessageCountListener;
 
 import com.brif.nix.model.DataAccess;
 import com.brif.nix.model.User;
 import com.brif.nix.parser.MessageParser;
 import com.sun.mail.gimap.GmailMessage;
 
 public class NixMessageCountListener implements MessageCountListener {
 
 	private final User currentUser;
 	private DataAccess dataAccess;
 
 	public NixMessageCountListener(User currentUser, DataAccess dataAccess) {
 		this.currentUser = currentUser;
 		this.dataAccess = dataAccess;
 	}
 
 	@Override
 	public void messagesRemoved(MessageCountEvent arg0) {
 		Message[] messages = arg0.getMessages();
 		for (Message message : messages) {
			System.out.println(getTime() + "message removed (" + message.getMessageNumber() + ")");			System.out.println(message.getMessageNumber());
			long uid = 0;
			dataAccess.removeMessage(uid);
 			System.out.println(getTime() + message.getMessageNumber());
 		}
 	}
 
 	@Override
 	public void messagesAdded(MessageCountEvent arg0) {
 		Message[] messages = arg0.getMessages();
 		for (Message message : messages) {
 			try {
 				if (isDraft(message)) {
 					break;
 				}
 				final int messageNumber = message.getMessageNumber();
				System.out.println(getTime() + "adding message (" + messageNumber + ")");
 				MessageParser mp = new MessageParser(message);
 				dataAccess.addMessage(currentUser, mp);
 
 				// TODO better way to acquire / store the next uid?
 				currentUser.next_uid = mp.getFolder().getUIDNext();
 				dataAccess.updateUserNextUID(currentUser);
				
				System.out.println(getTime() + "message added (" + messageNumber + ")");
 
 			} catch (MessagingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private boolean isDraft(Message message) throws MessagingException {
 		GmailMessage gm = (GmailMessage) message;
 		final String[] labels = gm.getLabels();
 		return labels.length > 0 && "\\Draft".equals(labels[0]);
 	}
 
 	public static String getTime() {
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		Date date = new Date();
 		return "[" + dateFormat.format(date) + "] ";
 	}
 
 }
