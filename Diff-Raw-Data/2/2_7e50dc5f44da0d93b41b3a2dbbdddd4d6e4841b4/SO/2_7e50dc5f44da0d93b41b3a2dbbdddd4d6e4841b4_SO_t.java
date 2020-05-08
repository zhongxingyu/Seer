 package modules;
 
 import static org.jibble.pircbot.Colors.*;
 
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 import java.util.zip.GZIPInputStream;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import main.Message;
 import main.NoiseModule;
 
 import static panacea.Panacea.*;
 
 /**
  * SO
  *
  * @author Michael Mrozek
  *         Created Aug 29, 2010.
  */
 public class SO extends NoiseModule {
 	private static final String SO_URL_PATTERN = "http://stackoverflow.com/questions/([0-9]+)(?:/.*/([0-9]+))?";
 	
 	private static final String COLOR_INFO = PURPLE;
 	private static final String COLOR_ERROR = RED + REVERSE;
 	
 	private static JSONObject getJSON(String url) throws IOException, JSONException {
 		final URLConnection c = new URL(url).openConnection();
 		final GZIPInputStream s = new GZIPInputStream(c.getInputStream());
 		final StringBuffer b = new StringBuffer();
 		
 		byte[] buffer = new byte[1024];
 		int size;
 		while((size = s.read(buffer)) >= 0)
 			b.append(new String(buffer, 0, size));
 
 //		this.bot.sendMessage(COLOR_INFO +  title + " (posted by " + author + ", " + duration + " seconds, " + viewCount + " views)");
 		return new JSONObject(b.toString());
 	}
 
 	@Command(".*" + SO_URL_PATTERN + ".*")
 	public void so(Message message, String questionID, String answerID) {
 		try {
 			final boolean isAnswer = (answerID != null);
 			final JSONObject j = getJSON("http://api.stackoverflow.com/1.0/" + (isAnswer ? ("answers/" + answerID) : ("questions/" + questionID)));
 			if(j.getInt("total") == 0) {
 				this.bot.sendMessage(COLOR_INFO + "No SO question with ID " + questionID);
 				return;
 			}
 			final JSONObject post = j.getJSONArray(isAnswer ? "answers" : "questions").getJSONObject(0);
 			final String created;
 			{
 				final Calendar c = new GregorianCalendar();
 				c.setTime(new Date(post.getInt("creation_date") * 1000L));
 				created = c.get(Calendar.DAY_OF_MONTH) + " " + c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + " " + c.get(Calendar.YEAR);
 			}
			this.bot.sendMessage(COLOR_INFO + post.getString("title") + " (" + (isAnswer ? "answered" : "asked") + " by " + post.getJSONObject("owner").getString("display_name") + " on " + created + ", +" + post.getInt("up_vote_count") + "/-" + post.getInt("down_vote_count") + ", " + pluralize(post.getInt("view_count"), "view", "views") + (isAnswer ? "" : ", " + pluralize(post.getInt("answer_count"), "answer", "answers")) + ")");
 		} catch(Exception e) {
 			this.bot.sendMessage(COLOR_ERROR + "Problem parsing Stack Overflow data");
 		}
 	}
 
 	@Override public String getFriendlyName() {return "SO";}
 	@Override public String getDescription() {return "Outputs information about any Stack Overflow URLs posted";}
 	@Override public String[] getExamples() {
 		return new String[] {
 			"http://stackoverflow.com/questions/3041249/when-are-temporaries-created-as-part-of-a-function-call-destroyed"
 		};
 	}
 	@Override public String getOwner() {return "Morasique";}
 }
