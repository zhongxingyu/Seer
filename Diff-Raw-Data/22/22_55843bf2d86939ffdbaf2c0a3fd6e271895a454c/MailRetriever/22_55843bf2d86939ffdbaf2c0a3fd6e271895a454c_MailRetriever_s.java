 package com.myrontuttle.fin.trade.alertreceivers;
 
 import java.io.*;
 import java.util.*;
 
 import javax.mail.Address;
 import javax.mail.FetchProfile;
 import javax.mail.Flags;
 import javax.mail.Flags.Flag;
 import javax.mail.Folder;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.NoSuchProviderException;
 import javax.mail.Part;
 import javax.mail.Session;
 import javax.mail.Store;
 import javax.mail.search.FlagTerm;
 
 import com.myrontuttle.fin.trade.api.AlertReceiver;
 
 /**
  * Code for reading unread mails from an email account (including Google Mail).
  * @author Myron Tuttle
  */
 public class MailRetriever implements Runnable {
 	
 	private final AlertReceiver alertReceiver;
 	private final String host;
 	private final int port;
 	private final String user;
 	private final String password;
 	
 	private final Properties props;
 
 	// Constructor of the class.
 	public MailRetriever(AlertReceiver alertReceiver, String host, 
 					int port, String user, String password) {
 		this.alertReceiver = alertReceiver;
 		this.host = host;
 		this.port = port;
 		this.user = user;
 		this.password = password;
 
 		/* Set the mail properties */
 		this.props = System.getProperties();
 		props.setProperty("mail.store.protocol", "imaps");
 	}
 	
 	public String getHost() {
 		return host;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public String getUser() {
 		return user;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void run() {
 
 		if (host == null || user == null || password == null) {
 			System.out.println("Please enter host, email, and password to use");
 			return;
 		}
		System.out.println("Retrieving Mail");
 		try {
 			/* Create the session and get the store for read the mail. */
 			Session session = Session.getDefaultInstance(props, null);
 			Store store = session.getStore("imaps");
 			store.connect(host, port, user, password);
 
 			/* Mention the folder name which you want to read. */
 			Folder inbox = store.getFolder("Inbox");
			System.out.println("No of Unread Messages : "
					+ inbox.getUnreadMessageCount());
 
 			/* Open the inbox using store. */
 			inbox.open(Folder.READ_WRITE);
 
 			/* Get the messages which are unread in the Inbox */
 			Message messages[] = inbox.search(new FlagTerm(
 					new Flags(Flag.SEEN), false));
 
 			/* Use a suitable FetchProfile */
 			FetchProfile fp = new FetchProfile();
 			fp.add(FetchProfile.Item.FLAGS);
 			fp.add(FetchProfile.Item.ENVELOPE);
 			fp.add(FetchProfile.Item.CONTENT_INFO);
 			inbox.fetch(messages, fp);
 
 			String subject = null;
 			for (Message message : messages) {
 				try {
 					subject = message.getSubject();
 				} catch (MessagingException me) {
 					System.out.println("Problem reading mail: " + me.getMessage());
 					subject = message.getHeader("Subject")[0];
 				}
 
 				System.out.println("Retrieved email: " + subject);
 				if (alertReceiver.matchAlert(subject) > 0) {
 					System.out.println("Email matched!");
 					inbox.setFlags(new Message[] {message}, new Flags(Flags.Flag.SEEN), true);
 				} else {
 					System.out.println("Message doesn't match any alerts yet.");
 					inbox.setFlags(new Message[] {message}, new Flags(Flags.Flag.SEEN), false);
 				}
 			}
 
 			inbox.close(true);
 			store.close();
 		} catch (NoSuchProviderException e) {
 			e.printStackTrace();
 			System.exit(1);
 		} catch (MessagingException e) {
 			e.printStackTrace();
 			System.exit(2);
 		}
 	}
 
 	void printAllMessages(Message[] msgs) throws Exception {
 		for (int i = 0; i < msgs.length; i++) {
 			System.out.println("MESSAGE #" + (i + 1) + ":");
 			printEnvelope(msgs[i]);
 		}
 	}
 
 	/* Print the envelope(FromAddress,ReceivedDate,Subject) */
 	void printEnvelope(Message message) throws Exception {
 		Address[] a;
 		// FROM
 		if ((a = message.getFrom()) != null) {
 			for (int j = 0; j < a.length; j++) {
 				System.out.println("FROM: " + a[j].toString());
 			}
 		}
 		// TO
 		if ((a = message.getRecipients(Message.RecipientType.TO)) != null) {
 			for (int j = 0; j < a.length; j++) {
 				System.out.println("TO: " + a[j].toString());
 			}
 		}
 		String subject = message.getSubject();
 		Date receivedDate = message.getReceivedDate();
 		String content = message.getContent().toString();
 		System.out.println("Subject : " + subject);
 		System.out.println("Received Date : " + receivedDate.toString());
 		System.out.println("Content : " + content);
 		getContent(message);
 	}
 
 	void getContent(Message msg) {
 		try {
 			String contentType = msg.getContentType();
 			System.out.println("Content Type : " + contentType);
 			Multipart mp = (Multipart) msg.getContent();
 			int count = mp.getCount();
 			for (int i = 0; i < count; i++) {
 				dumpPart(mp.getBodyPart(i));
 			}
 		} catch (Exception ex) {
 			System.out.println("Exception arose during getContent");
 			ex.printStackTrace();
 		}
 	}
 
 	void dumpPart(Part p) throws Exception {
 		// Dump input stream ..
 		InputStream is = p.getInputStream();
 		// If InputStream is not already buffered, wrap a BufferedInputStream
 		// around it.
 		if (!(is instanceof BufferedInputStream)) {
 			is = new BufferedInputStream(is);
 		}
 		int c;
 		System.out.println("Message : ");
 		while ((c = is.read()) != -1) {
 			System.out.write(c);
 		}
 	}
 }
