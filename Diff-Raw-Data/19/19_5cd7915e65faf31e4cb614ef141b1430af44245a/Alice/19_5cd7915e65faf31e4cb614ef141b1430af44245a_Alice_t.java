 import java.io.File;
import java.io.FileOutputStream;
 import java.io.IOException;
import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Scanner;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
import ec.util.MersenneTwisterFast;
 
 /**
  * Alice bot.
  * 
  * @author yappy
  */
 public class Alice {
 
 	private static final String TWEET_FILE_NAME = "list.txt";
 
 	private static PrintWriter logOut;
 
 	private static void saySomething() {
 		try {
 			Scanner in = new Scanner(new File(TWEET_FILE_NAME), "UTF-8");
 			List<String> list = new ArrayList<String>();
 			while (in.hasNextLine()) {
 				String line = in.nextLine();
 				if (!line.equals("") && !line.startsWith("#")) {
 					list.add(line);
 				}
 			}
 			logOut.printf("List loaded (%d items)%n", list.size());
 
 			MersenneTwisterFast mt = new MersenneTwisterFast();
 			int first = mt.nextChar();
 			for (int i = 0; i < first; i++) {
 				mt.nextInt();
 			}
 			int ind = mt.nextInt(list.size());
 
 			String msg = list.get(ind);
 			Twitter twitter = new TwitterFactory().getInstance();
 			twitter.updateStatus(msg);
 			logOut.println("tweet: " + msg);
 		} catch (IOException e) {
 			e.printStackTrace(logOut);
 		} catch (TwitterException e) {
 			e.printStackTrace(logOut);
 		}
 	}
 
 	public static void main(String[] args) {
 		Date nowDate = new Date();
 		String nowStr = String.format("%1$tY%1$tm%1$td", nowDate);
 
 		File logDir = new File("log");
 		logDir.mkdir();
 		String logFileName = nowStr + ".log";
 		try {
			Writer w = new OutputStreamWriter(new FileOutputStream(new File(
					logDir, logFileName), true), "UTF-8");
			logOut = new PrintWriter(w);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 		logOut.printf("Start %1$tF %1$tT%n", nowDate);
 
 		saySomething();
 
 		logOut.println("Exit.");
 		logOut.println();
 		logOut.close();
 	}
 }
