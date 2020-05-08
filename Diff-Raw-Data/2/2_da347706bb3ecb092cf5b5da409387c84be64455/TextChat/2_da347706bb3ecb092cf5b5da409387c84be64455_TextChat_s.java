 
 package axirassa.webapp.ajax;
 
 import java.util.ArrayList;
 
import org.testng.log4testng.Logger;
 
 public class TextChat {
 	protected static final Logger log = Logger.getLogger(TextChat.class);
 
 	private final ArrayList<TextChatMessage> messages = new ArrayList<TextChatMessage>();
 
 
 	public void addMessage(String text) {
 		if (text != null && text.trim().length() > 0)
 			messages.add(0, new TextChatMessage(text));
 
 	}
 
 }
