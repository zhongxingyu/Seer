 package httpserv;
 
 import graph.PersistentGraph;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 
 import utils.Time;
 
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 import computer.TweetRankComputer;
 
 /** This class handles the STATUS requests. */
 public class StatusHandler implements HttpHandler {
 	private static final Logger logger = Logger.getLogger("ranker.logger");
 	private PersistentGraph pgraph;
 	private TweetRankComputer trcomputer;
 
 	public StatusHandler(PersistentGraph pgraph, TweetRankComputer trcomputer) {
 		super();
 		this.pgraph = pgraph;
 		this.trcomputer = trcomputer;
 	}
 
 	@Override
 	public void handle(HttpExchange t) throws IOException {
 		try {
 			String response = "Persistent graph info:\n======================\n" +
 			"Number of tweets: " + pgraph.getNumberOfTweets() + "\n" + 
 			"Number of users: " + pgraph.getNumberOfUsers() + "\n" +
 			"Number of hashtags: " + pgraph.getNumberOfHashtags() + "\n" +
 			"Average tweets per user: " + pgraph.getAverageTweetsPerUser() + "\n" +
 			"Average friends per user: " + pgraph.getAverageFriendsPerUser() + "\n" +
 			"Average references per tweet: " + pgraph.getAverageReferencePerTweet() + "\n" +
 			"Average mentions per tweet: " + pgraph.getAverageMentionsPerTweet() + "\n\n";
 
 			response += "TweetRank computation:\n======================\n";
 			TweetRankComputer.State state = trcomputer.getState();
 			long NTweets = trcomputer.getNumberOfTweets();
 			Date enddate = trcomputer.getEndDate();
 			Time elapsed = trcomputer.getElapsedTime();
 
 			if ( state == TweetRankComputer.State.WORKING ) {
 				response += "State: WORKING\n";
 				response += "Number of tweets: " + NTweets + "\n";
 				response += "Last computation: " + (enddate == null ? "Never" : Time.formatDate("yyyy/MM/dd HH:mm:ss", enddate)) + "\n";
 				response += "Elapsed time: " + elapsed + "\n";
 				response += "Completed: " + trcomputer.getPercentageOfCompletion()*100.0 + "%\n";
 				response +=	"Expected remaining time: " + trcomputer.getRemainingTime(); 
 			} else {
 				response += "State: IDLE\n";
 				response += "Number of tweets: " + NTweets + "\n";
 				response += "Last computation: " + (enddate == null ? "Never" : Time.formatDate("yyyy/MM/dd HH:mm:ss", enddate)) + "\n";
 				if ( elapsed != null )	response += "Elapsed time: " + elapsed;
 			}
 
 			t.sendResponseHeaders(200, response.length());
 			OutputStream os = t.getResponseBody();
 			os.write(response.getBytes());
 			os.close();
		} catch ( Throwable th ) {
			logger.error("Error during status recopilation.", th);
 			String response = "Error during status recopilation.";
 			t.sendResponseHeaders(400, response.length());
 			OutputStream os = t.getResponseBody();
 			os.write(response.getBytes());
 			os.close();
 		}
 	}
 }
