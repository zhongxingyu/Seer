 import java.net.*;
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class MultipleSocketServer implements Runnable {
 
 	static Runnable[] feeds = new Runnable[2];
 	static Runnable[] parsers = new Runnable[2];
 	private Socket connection;
 	public static void main(String[] args) {
 		feeds[0] = new FeedReader("oil");
 		feeds[1] = new FeedReader("currency");
 		parsers[0] = new Parse("oil");
 		parsers[1] = new Parse("currency");
 
 		Thread oilt = new Thread(feeds[0]);
 		oilt.start();
 		Thread currt = new Thread(feeds[1]);
 		currt.start();
 		Thread oiltopt = new Thread(parsers[0]);
 		oiltopt.start();
 		Thread currtopt = new Thread(parsers[1]);
 		currtopt.start();
 		
 		// Start main server on this port
 		int port = 8009;
 		try {
 			ServerSocket socket1 = new ServerSocket(port);
 			System.out.println("Server starting..");
 			while (true) {
 				Socket connection = socket1.accept();
 				Runnable runnable = new MultipleSocketServer(connection);
 				Thread thread = new Thread(runnable);
 				thread.start();
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	MultipleSocketServer(Socket s) {
 		this.connection = s;
 	}
 
 	public void run() {
 		try {
 			// Read input
 			BufferedInputStream is = new BufferedInputStream(connection.getInputStream());
 			InputStreamReader isr = new InputStreamReader(is);
 			int character;
 			StringBuffer process = new StringBuffer();
 			while((character = isr.read()) != '\n') {
 				process.append((char)character);
 			}
 			String processStr = process.toString();
 			
 			// Process input
 			// Format: topic;topic;;datetime
 			// let convertedDate be input date
 			String[] processArr = processStr.split(";;");
 			String[] topicArr = processArr[0].split(";");
 			BufferedOutputStream os = new BufferedOutputStream(connection.getOutputStream());
 			OutputStreamWriter osw = new OutputStreamWriter(os, "US-ASCII");
 			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); 
 			Date convertedDate = dateFormat.parse(processArr[1]); 
 			System.out.println(processArr[1]);
 			
 			// Send news - Feedreader
 			for (String topic : topicArr) {
 				for (int i = 0; i < feeds.length; i++) {
 					if (((FeedReader) feeds[i]).getSector().compareTo(topic) == 0) {
 						FeedReader thefeed = ((FeedReader) feeds[i]);
 						for (Story S: thefeed.getStories()) {
 							if (S == null) {
 								continue;
 							}
 							if (convertedDate.compareTo(S.getTimestamp()) < 0) {
 								String title = S.getTitle();
 								String returnTitle = title ;
 								returnTitle = returnTitle + " ;@; " + S.getDate() + "\n";
 								osw.write(returnTitle);
 								osw.write("KEYWORDSTART\n");
 								String returnWords = S.top5keyWords();
 								osw.write(returnWords);
 								osw.write("KEYWORDEND\n");
 							}
 						}
 					}
 				}
 				osw.write("NEWSTOP\n");
 			}
 			osw.write("SPLITINFO\n");
 			
 			// Send topics - Parse
 			for (String topic : topicArr) {
 				for (int i = 0; i < parsers.length; i++) {
 					if (((Parse) parsers[i]).getSector().compareTo(topic) == 0) {
 						Parse theparse = ((Parse) parsers[i]);
 						for (Topic T: theparse.getTopics()) {
 							if (T == null) {
 								continue;
 							}
 							if (convertedDate.compareTo(T.getTimestamp()) < 0) {
 								String returnTitle = ((Integer) T.getUid()).toString() + ";;\n";
 								returnTitle += T.getRecentTitle() + ";;\n"; 
 								returnTitle += T.getDate() + ";;\n";
								returnTitle += T.artsLastHour() + ";;\n";
 								returnTitle += T.topLinks() + ";;\n";
 								returnTitle += T.topWords()+ ";;\n";
 								osw.write(returnTitle);
 								osw.write("SPECTOPS\n");
 							}
 						}
 
 					}
 				}
 				osw.write("TOPSTOP\n");
 			}
 			osw.write("\t");
 			osw.flush();
 
 	}
 	catch (Exception e) {
 		e.printStackTrace();
 		System.out.println(e);
 	}
 	finally {
 		try {
 			connection.close();
 		}
 		catch (IOException e){}
 	}
 }
 }
