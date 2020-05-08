 package mozeq.irc.bot.plugins;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 
 import mozeq.irc.bot.IrcBotPlugin;
 import mozeq.irc.bot.IrcMessage;
 
 public class KudosPlugin extends IrcBotPlugin {
 
 	private Map<String, Integer> kudosTable = new Hashtable<String, Integer>();
 
 	@Override
 	public void init() {
 		this.commands = new ArrayList<String>();
 		commands.add("kudos");
		commands.add("\\w+\\+\\+");
		commands.add("\\w+\\-\\-");
 
 		loadKudos();
 	}
 
 	@Override
 	public ArrayList<String> run(IrcMessage message, String command) {
 
 		clearResponses();
 		if (command.equals("kudos")) {
 			System.out.println("aaaaaa");
 
 			Iterator<Map.Entry<String, Integer>> it = kudosTable.entrySet().iterator();
 
 
 		    while(it.hasNext()) {
 		    	Map.Entry<String, Integer> entry = it.next();
 
 		    	addResponse(entry.getKey() + ":" + entry.getValue());
 		    }
 			return responses;
 		}
 
		else if (command.endsWith("++") || command.endsWith("--")) {

			String kudoNick = command.substring(0, command.length() - 2);
 
 
 			if (kudoNick.length() < 1)
 				return responses;
 
 			if (kudoNick.contains("obama")) {
 				addResponse(kudoNick + " is a ni..a");
 				return responses;
 			}
 
 			Integer kudos = kudosTable.get(kudoNick);
 			if (kudos == null)
 				kudos = 0;
 
			if (command.endsWith("++"))
 				kudos++;
 			else
 				kudos--;
 
 			kudosTable.put(kudoNick, kudos);
 
 			saveKudos();
 
 			addResponse(kudoNick +" now has: " + kudos +" kudos");
 		}
 
 		return responses;
 	}
 
 	void saveKudos() {
 		try {
 		    BufferedWriter out = new BufferedWriter(new FileWriter("kudos"));
 
 		    Iterator<Map.Entry<String, Integer>> it = kudosTable.entrySet().iterator();
 
 		    while(it.hasNext()) {
 		    	Map.Entry<String, Integer> entry = it.next();
 		    	out.write(entry.getKey() + " " + entry.getValue() + "\n");
 		    }
 
 		    out.close();
 		} catch (IOException e) {
 
 		}
 	}
 
 	void loadKudos() {
 		kudosTable.clear();
 
 		try {
 		    BufferedReader in = new BufferedReader(new FileReader("kudos"));
 		    String str;
 		    while ((str = in.readLine()) != null) {
 		    	int delim = str.indexOf(' ');
 		    	if (delim > -1) {
 		    		String nick = str.substring(0, delim);
 		    		Integer kudos = Integer.valueOf(str.substring(delim+1)).intValue();
 		    		kudosTable.put(nick, kudos);
 		    	}
 		    }
 		    in.close();
 		} catch (IOException e) {
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
