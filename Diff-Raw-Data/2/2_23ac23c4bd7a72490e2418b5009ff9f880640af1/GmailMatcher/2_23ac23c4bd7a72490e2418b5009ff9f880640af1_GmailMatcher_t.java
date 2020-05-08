 package com.azusa;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.mail.Flags;
 import javax.mail.Folder;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Store;
 import javax.mail.internet.MimeMultipart;
 import javax.mail.search.FlagTerm;
 
 // 1. Use Shell to call : GmailMatcher user password labelName matchRegex > outputFile.html
 // 2. Use chrome to open: chrome ouputFile.html
 public class GmailMatcher {
 	private static Pattern p;
 	private final static int MAX_MAIL = 20;
 	private static final boolean IS_OUT_DEBUG = true;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		if (args.length < 4) {
 			System.out
 					.println("Usage: GmailReceiver user pass folder matchRegex");
 			return;
 		}
 		debug(args[0], args[1], args[2], args[3]);
 		/*
 		 * System.out.println(receiveAndOutputMail("xxxx@gmail.com", "xxxx",
 		 * "メールdeポイント"
 		 * ,"\\bhttp[s]*://?pmrd.rakuten.co.jp/\\?r=.+?p=.+?u=.+?\\b"));
 		 */
 		p = Pattern.compile(args[3]);
 		Map<String, String> mails = new HashMap<String, String>();
 		try {
 			receiveMails(mails, args[0], args[1], args[2]);
 		} catch (Exception e) {
 			// Any exception
 			e.printStackTrace();
 		} finally {
 			System.out.println(createOutputHtml(mails));
 		}
 
 	}
 
 	private static Map<String, String> receiveMails(final Map<String,String> mapper,
 			String user, String password, String folderName) throws Exception {
 
 		new GmailMatcher().new MyGMailBox() {
 			@Override
 			public void doEachMail(int no, Message m)
 					throws MessagingException, IOException {
 				if (!m.isSet(Flags.Flag.SEEN)) {
 					String mailTitle = m.getReceivedDate() + m.getSubject();
 					String contentText = "";
 					Object content = m.getContent();
 					if (content instanceof MimeMultipart)  {
 						StringBuilder sb = new StringBuilder();
 						int linesNum = ((MimeMultipart)content).getCount();
 						for (int i=0;i<linesNum;i++){
 							sb.append(((MimeMultipart)content).getBodyPart(i).getContent());
 						}
 						contentText = sb.toString();
 					} else {
 						contentText = content.toString();
 					}
 					String url = getPointURL(contentText);
 					if (url != null) {
 						validCount++;
 						debug("[INFO] " + validCount +"/"+MAX_MAIL +  " Ponit url :" + url);
 						mapper.put(mailTitle, url);
 					} else {
 						debug("[WARN] No url mail. ");
 					}
 				} else {
 					debug("[WARN] Readed mail. ");
 				}
 			}
 
 			@Override
 			public boolean isStop() {
 				return mapper.size() >= MAX_MAIL;
 			}
 		}.receive(user, password, folderName);
 
 		return mapper;
 		// return createOutputHtml(mapper);
 	}
 
 	private static String getPointURL(String content) {
 		Matcher m = p.matcher(content);
 		return m.find() ? m.group() : null;
 	}
 
 	// System.out.println(m.getSubject() + " - " + m.getSize());
 	// System.out.println("------------ Message " + (i + 1) +
 	// " ------------");
 	// System.out.println("SentDate : " +
 	// messages[i].getSentDate());
 	// System.out.println("From : " + messages[i].getFrom()[0]);
 	// System.out.println("Subject : " + messages[i].getSubject());
 	// System.out.print("Message : " + messages[i].getContent());
 	//private static final String htmlTemplate = "<!DOCTYPE html><html><head><meta http-equiv='content-type' content='text/html;charset=UTF-8'><title>Latest Mail De Point</title><script type='text/javascript' src='http://code.jquery.com/jquery-1.8.3.js'></script><script type='text/javascript'>win=null;$(function(){$('#OPEN_LIST a').each(function(i){var href=$(this).attr('href');setTimeout(function(){win=window.open(href);},i*10000);setTimeout(function(){if(win!=null)win.close();},10000+i*9990);});});</script></head><body>#REPLACE_HERE#</body></html>";
	private static final String htmlTemplate = "<!DOCTYPE html><html><head><meta http-equiv='content-type' content='text/html;charset=UTF-8'><title>Latest Mail De Point</title><script type='text/javascript' src='http://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js'></script><script type='text/javascript'>$(function(){$('#OPEN_LIST a').each(function(i){var href=$(this).attr('href');setTimeout(function(){$('#mywin').attr('src', href);},i*10000)});});</script></head><body>#REPLACE_HERE#<iframe id='mywin' src='http://www.bing.com' width=1024 height=768></iframe></body></html>";
 
 	private static String createOutputHtml(Map<String, String> map) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<div id='OPEN_LIST'>");
 		if (map.size() > 0) {
 			for (String s : map.keySet()) {
 				sb.append("<a href='").append(map.get(s)).append("'>").append(s)
 						.append("</a><br/>");
 			}
 		} else {
 			sb.append("No result.");
 		}
 		sb.append("</div>");
 		return htmlTemplate.replace("#REPLACE_HERE#", sb.toString());
 	}
 
 	protected static void debug(String... strings) {
 		if (IS_OUT_DEBUG) {
 			StringBuilder sb = new StringBuilder();
 			// Comment of html
 			sb.append(" <!--");
 			for (String s : strings)
 				sb.append(s + " ");
 			sb.append(" -->");
 			System.out.println(sb.toString());
 		}
 	}
 
 	abstract class MyGMailBox {
 		final String host = "imap.gmail.com";
 		final int port = 993;
 		int validCount = 0;
 
 		public abstract void doEachMail(int no, Message m)
 				throws MessagingException, IOException;
 
 		public abstract boolean isStop();
 
 		public void receive(String user, String password, String folderName)
 				throws Exception {
 			debug("User:" + user + ", Folder: " + folderName);
 
 			Properties props = System.getProperties();
 			Session sess = Session.getInstance(props, null);
 			// sess.setDebug(true);
 
 			Store st = sess.getStore("imaps");
 			st.connect(host, port, user, password);
 			Folder fol = st.getFolder(folderName);
 			if (fol.exists()) {
 				// for (Folder f : fol.list()) {
 				// System.out.println(f.getName());
 				// }
 				fol.open(Folder.READ_WRITE);
 
 				// (1) create a search term for all "unseen" messages
 				Flags seen = new Flags(Flags.Flag.SEEN);
 				FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
 
 				// (2) create a search term for all recent messages
 				// Flags recent = new Flags(Flags.Flag.RECENT);
 				// FlagTerm recentFlagTerm = new FlagTerm(recent, true);
 
 				// (3) combine the search terms with a JavaMail AndTerm:
 				// http://java.sun.com/developer/onlineTraining/JavaMail/contents.html#Java...
 				// SearchTerm searchTerm = new AndTerm(unseenFlagTerm,
 				// recentFlagTerm);
 
 				// From old to latest mail
 				Message[] messages = fol.search(unseenFlagTerm);
 				// Message[] messages = fol.getMessages();
 
 				// To get the latest 20 mails use:
 				// int n= fol.getMessageCount();
 				// Message[] messages= fol.getMessages(n-20,n);
 
 				for (int i = messages.length - 1; i > 0; i--) {
 					int count = messages.length - i;
 					Message message = messages[i];
 					debug("Receiving " + count + " mail. Title: "
 							+ message.getReceivedDate() + message.getSubject());
 					doEachMail(count, message);
 
 					if (isStop())
 						break;
 					// getPointURL(m.getSubject().toString(),
 					// m.getContent().toString());
 					// InputStream stream = messages[i].getInputStream();
 					// while (stream.available() != 0) {
 					// System.out.print((char) stream.read());
 					// }
 					// System.out.println();
 				}
 				fol.close(false);
 			} else {
 				System.out.printf("%s is not exist.", folderName);
 			}
 			st.close();
 		}
 
 	}
 }
