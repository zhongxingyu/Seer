 package epam.ph.sg.tab.chat;
 
 /**
  * @author Talash Pavlo
  */
 import java.util.Date;
 
 import epam.ph.sg.models.User;
 
 public class Message {
 	public static final int MAX_LENGTH = 20;
 
 	private String time;
 	private User sender;
 	private String text;
 
 	@SuppressWarnings("deprecation")
 	public Message(User sender, String text) {
 		Date date = new Date();
 		int hh = date.getHours();
 		int mm = date.getMinutes();
 		if (mm < 10) {
 			this.time = hh + ":0" + mm;
 		} else {
 			this.time = hh + ":" + mm;
 		}
 
 		this.sender = sender;
 		this.text = msgPrepare(text);
 	}
 
 	/**
 	 * Prepare text to add it to the Chat (split to MAX_LENGTH)
 	 * 
 	 * @param text
 	 *            - text to split
 	 * @return prepared text
 	 */
 	private static String msgPrepare(String text) {

 		String preparedText = "";
 		String[] words = text.split(" ");
 		for (int i = 0; i < words.length; i++) {
 			if (words[i].length() > MAX_LENGTH) {
 				String newWord = words[i].substring(0, MAX_LENGTH);
 				newWord = newWord.concat("...");
 				preparedText += " " + newWord;
 			} else {
 				preparedText += " " + words[i];
 			}
 		}
 		return preparedText.trim();
 	}
 
 	public String getTime() {
 		return time;
 	}
 
 	public User getSender() {
 		return sender;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	@Override
 	public String toString() {
 		return "Sender: " + getSender().getName() + "; Time: " + getTime();
 	}
 }
