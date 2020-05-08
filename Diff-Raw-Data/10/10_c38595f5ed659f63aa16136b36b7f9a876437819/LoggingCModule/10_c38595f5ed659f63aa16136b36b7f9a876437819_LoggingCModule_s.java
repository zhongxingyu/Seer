 package commands;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 import org.pircbotx.hooks.events.MessageEvent;
 
 
 import backend.Bot;
 import backend.Database;
 import backend.Util;
 
 public class LoggingCModule extends Command {
 
 	@Override
 	public void execute(final Bot bot, final Channel chan, final User user, String message) {
 		//TODO generate stats for a channel
 		if(chan == null) return;
 		
 		if(Util.checkArgs(message, 2)) {
 			String[] args = Util.getArgs(message, 2);
 			switch(args[1]) {
 			case "quote":
 				try {
 					displayRandomQuote(bot, chan, user, getDataForChannel(chan));
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			default:
 					
 			}
 			return;
 		}
 		
 		
 		passMessage(bot, chan, user, "I will begin generating statistics for "+chan.getName()+ " now.");
 		new Thread(new Runnable(){
 
 			@Override
 			public void run() {
 				try {
 					List<HashMap<String, Object>> returned = getDataForChannel(chan);
 					passMessage(bot, chan, user, "I have "+returned.size()+" recorded messages from "+chan.getName()+".");
 					
 					displayRandomQuote(bot, chan, user, returned);
 					
 					HashMap<String, Integer> httpcount = new HashMap<>();
 					HashMap<String, Integer> cmdcount = new HashMap<>();
 					
 					for(HashMap<String, Object> column : returned) {
 						String message = column.get("MESSAGE").toString();
 						String sender = column.get("USER_NAME").toString().trim();
 						if(message.contains("http://") || message.contains("https://")) {
 							if(httpcount.containsKey(sender)) httpcount.put(sender, httpcount.get(sender)+1);
 							else httpcount.put(sender, 1);
 						} else if(message.startsWith("!")) {
 							if(cmdcount.containsKey(sender)) cmdcount.put(sender, cmdcount.get(sender)+1);
 							else cmdcount.put(sender, 1);
 						}
 					}
 					
 					String maxHttp = getMax(httpcount);
 					String maxCmd = getMax(cmdcount);
 					
 					passMessage(bot, chan, user, "Most links posted: "+maxHttp + ", with "+httpcount.get(maxHttp)+ " links.");
 					passMessage(bot, chan, user, "Most commands used: "+maxCmd + ", with "+cmdcount.get(maxCmd)+ " commands.");
 					
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 				
 			}}).start();
 	}
 
 	private String getMax(HashMap<String, Integer> httpcount) {
 		String curMax = null;
 		
 		for(String s : httpcount.keySet()) {
 			if(curMax == null || httpcount.get(s) > httpcount.get(curMax)) curMax = s;
 		}
 		return curMax;
 	}
 
 	private void displayRandomQuote(final Bot bot, final Channel chan,
 			final User user, List<HashMap<String, Object>> returned) {
 			HashMap<String, Object> rand;
 			do {
 				rand = returned.get((int) (Math.random() * returned.size()));
 			} while(rand.get("MESSAGE").toString().startsWith("!") || rand.get("MESSAGE").toString().split(" ").length < 3);
 			passMessage(bot, chan, user, "Random quote: <"+((String)rand.get("USER_NAME")).trim() + "> " + rand.get("MESSAGE"));
 	}
 	
 	private List<HashMap<String, Object>> getDataForChannel(
 			final Channel chan) throws SQLException {
 		List<HashMap<String,Object>> returned = Database.select("select * from "+getFormattedTableName()+" where channel="+Database.getEnclosedString(chan.getName()));
 		return returned;
 	}
 
 	@Override
 	protected void initialize() {
 		addAlias("generate-stats");
 		this.setAccessLevel(LEVEL_OPERATOR);
 		this.setPriorityLevel(PRIORITY_MODULE);
 		this.setHelpText("Wheeeee, retrieve those logs!");
 		this.setName("LoggingStats");
 		this.setTableName("logs"); 
 
 		try {
 			Database.createTable(
 					this.getFormattedTableName(),
 					"server char(20), channel char(30), user_name char(25), message varchar(600), time timestamp not null default current timestamp");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onMessage(MessageEvent<Bot> event) throws Exception {
 		Database.insert(getFormattedTableName(),
 				"server, channel, user_name, message", Database
 						.getEnclosedString(event.getBot().getServer())
 								+ ","
 								+ Database.getEnclosedString(event.getChannel()
 										.getName())
 										+ ","
 										+ Database.getEnclosedString(event
 												.getUser().getNick())
 										+ ","
 										+ Database.getEnclosedString(event
												.getMessage().replaceAll("'", "''"))
 												);
 		super.onMessage(event);
 	}
 
 }
