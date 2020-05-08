 package main;
 
 import org.jibble.pircbot.User;
 
 /*
  * This class processes IRC messages and events to generate statistics.
  */
 
 public class StatsHandler {
 	private static StatsHandler instance;
 	
 	private String[] profanities =  { "fuck","cock", "dick", "cunt", "bitch", "shit", "piss", "nigger", "asshole", "faggot", "wank" };
 	private String[] conjunctions = { "and", "but", "or", "yet", "for", "nor", "so" };
	private String[] snagMessages = { "Snagged the shit outta that one!", "What a lame quote. Snagged!", "Imma stash those words for you.", "Snagged, motherfucker!", "Everything looks great out of context. Snagged!", "Yoink!", ""};
 
 	public static StatsHandler getInstance() {
 		if (instance == null) {
 			instance = new StatsHandler();
 		}
 		return instance;
 	}
 
 	public void processMessage(String channel, String sender, String login, String hostname, String message) {
 		incrementLineCount(login);
 		SqlConnector.getInstance().tryIncrementVaria("global_line_count");
 		processQE(login, message);
 		processAlts(login, sender);
 		String[] words = message.split(" ");
 		processRandomQuote(channel, login, message, words);
 		processEmoticons(channel, sender, login, hostname, message, words);
 		processWords(channel, sender, login, hostname, message, words);
 	}
	private void sendMessage(String target, String message){
		SimpleBot.instance.sendMessage(target, message);
	}
 	private void processRandomQuote(String channel, String login, String message, String[] words){
 		if(words.length > 6){
			if(Math.random() < 1){
 				double rand = Math.random();
 				for(int i = 0; i <= snagMessages.length; i++){
					double treshold = (1.0 / ((double) snagMessages.length*2.5)*((double)i+1.0));
					if( rand < treshold && i < snagMessages.length){
 						SimpleBot.instance.sendMessage(channel, snagMessages[i]);
 						break;
 					}
 					else if(i == snagMessages.length){
 						SimpleBot.instance.sendMessage(channel, "Snagged!");
 					}
 				}
 				setRandomQuote(login, message);
 			}
 		}
 	}
 	private void processQE(String login, String message){
 		if(message.endsWith("!")){
 			SqlConnector.getInstance().tryIncrement("users", "nick", login, "exclamations", "'" + login + "', 0, 0, 0, 1, 0, 0, 0, 0, 0, ''");
 		}else if(message.endsWith("?")){
 			SqlConnector.getInstance().tryIncrement("users", "nick", login, "questions", "'" + login + "', 0, 0, 1, 0, 0, 0, 0, 0, 0, ''");
 		}
 	}
 	private void setRandomQuote(String login, String message) {
 		SqlConnector.getInstance().sendQuery("UPDATE users SET random_quote = '" + message.replaceAll("'", "''") + "' WHERE nick = '" + login + "'");
 	}
 
 	private void processAlts(String login, String sender) {
 		String dbLogin = SqlConnector.getInstance().sendSelectQuery("SELECT login FROM alts WHERE `login` = '" + login + "'");
 		if(dbLogin.equals("") || dbLogin == null){
 			System.out.println("Creating new alt for " + login + ". Primary: " + sender);
 			SqlConnector.getInstance().sendQuery("INSERT INTO alts VALUES ('" + login + "', '" + sender + "', '')");
 		}else{
 			String primaryNick = SqlConnector.getInstance().sendSelectQuery("SELECT `primary` FROM alts WHERE login = '" + login + "'");
 			if(!sender.equals(primaryNick)){
 				String altNicks = SqlConnector.getInstance().sendSelectQuery("SELECT `additional` FROM alts WHERE login = '" + login + "'");
 				if(!altNicks.contains(sender.toLowerCase()) && !(login.equals("webchat") || login.equals("~Quassel"))){
 					SqlConnector.getInstance().sendQuery("UPDATE alts SET additional = CONCAT_WS(',', additional, '" + sender.toLowerCase() + "') WHERE login = '" + login + "'");
 				}
 			}
 		}
 	}
 
 	private void processEmoticons(String channel, String sender, String login, String hostname, String message, String[] words) {
 		if(login.equals("~Cadbury")) return;
 		for(String word : words){
 			for(int i = 0; i < Emoticons.emoticons.length; i++){
 				String emoticon = Emoticons.emoticons[i];
 				if(word.startsWith("http://")) continue;
 				if(word.equals(emoticon)){
 					SqlConnector.getInstance().tryIncrementLastUsedBy("emoticons", "emoticon", emoticon, "frequency", login, "'"+ emoticon + "', 1, '" + login + "'");
 				}
 			}
 		}
 	}
 
 	private void processWords(String channel, String sender, String login, String hostname, String message, String[] words) {
 		User[] users = SimpleBot.instance.getUsers(channel);
 		
 		SqlConnector.getInstance().tryIncrement("users", "nick", login, "words", words.length, "'"+ sender + "', 0, 0, 0, 0, 0, 0, 0, 0, " + words.length + ", ''");
 		
 		int nickCount = 0;
 		for (String word : words) {
 			word = word.toLowerCase();
 			for (User user : users) {
 				String nick = user.getNick().toLowerCase();
 				if (!sender.equals(word) && !sender.equals("Cadbury") && word.replaceAll("$|,|:", "").equalsIgnoreCase(nick)) {
 					nickCount++;
 					if(nickCount < 3){
 						String pingedLogin = SqlConnector.getInstance().sendSelectQuery("SELECT login FROM alts WHERE (`primary` = '" + nick + "' OR `additional` LIKE '%" + nick + "%')");
 						if(pingedLogin.equals("") || pingedLogin == null){
 							//Unable to retrieve the login of the person who was pinged
 						}else{
 							SqlConnector.getInstance().tryIncrement("users", "nick", pingedLogin, "pings", "'" + nick + "', 1, 0, 0, 0, 0, 0, 0, 0, ''");
 						}
 						
 					}
 				}
 			}
 			if (!word.startsWith("http://")) {
 				word = word.replaceAll("[^A-Za-z0-9]", "");
 			} else {
 				// Add support for URLs here later
 				continue;
 			}
 			if (word.equals("")) {
 				continue;
 			}
 			if (word.length() > 32 || word.length() < 3) {
 				continue;
 			}
 			if (isArticle(word)) {
 				continue;
 			}
 			if (isConjunction(word)) {
 				continue;
 			}
 			if (isProfanity(word)) {
 				SqlConnector.getInstance().increment("users", "nick", login, "profanities");
 			}
 			SqlConnector.getInstance().tryIncrement("words", "word", word, "frequency", "'" + word+ "', 1");
 		}
 	}
 
 	private boolean isProfanity(String word) {
 		for (int i = 0; i < profanities.length; i++) {
 			String profanity = profanities[i];
 			if (word.contains(profanity)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void incrementLineCount(String nick) {
 		SqlConnector.getInstance().tryIncrement("users", "nick", nick, "lines", "'" + nick + "', 0, 0, 0, 0, 0, 0, 0, 1, 0, ''");
 	}
 
 	private boolean isConjunction(String word) {
 		for (String conjunction : conjunctions) {
 			if (word.equals(conjunction))
 				return true;
 		}
 		return false;
 	}
 
 	private boolean isArticle(String word) {
 		return (word.equals("the") || word.equals("an") || word.equals("a"));
 	}
 
 	public void processAction(String sender, String login, String hostname, String target, String action) {
 		SqlConnector.getInstance().tryIncrementVaria("global_action_count");
 		SqlConnector.getInstance().tryIncrement("users", "nick", login, "actions", "'" + login + "', 0, 1, 0, 0, 0, 0, 0, 0, 0, ''");
 	}
 }
