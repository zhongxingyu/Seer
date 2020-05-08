 /*
  * @author Raymond Hammarling
  * @description This command lets you locate where in the world a hostname or IP address leads to, or even locate a user.
  * @basecmd geolocate
  * @category misc
  */
 package commands;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 
 import backend.Bot;
 
 public class GeolocateCommand extends Command {
 	@Override
 	protected void initialize() {
 		setName("GeoLocator");
 		setHelpText("Where in the world does that address point to? Let's find out!");
 		addAlias("geolocate");
 		addAlias("geolocate-nick");
 	}
 
 	@Override
 	protected String format() {
 		return super.format() + " {hostname | ip} | [nick]";
 	}
 
 	@Override
 	public void execute(Bot bot, Channel chan, User user, String message) {
		if(message.startsWith(getCmdSequence())) message = message.substring(1);
 		
 		String targetHost = user.getHostmask();
 		String[] args = message.trim().split(" ", 2);
 		
 		if(args.length > 1) {
 			if(args[0].equalsIgnoreCase("geolocate-nick")) {
 				User targetUser = bot.getUser(args[1]);
 				if(targetUser.getChannels().size() == 0) {
 					passMessage(bot, chan, targetUser, "Sorry, I don't know about anyone called \"" + args[1] + "\"!");
 					return;
 				}
 				else targetHost = targetUser.getHostmask();
 			}
 			else targetHost = args[1];
 		}
 		
 		try {
 			InetAddress addr = InetAddress.getByName(targetHost);
 			
 			IpInfoDb requester = new IpInfoDb("07b8b76fe1f6146cd24f83e290ebf16e15c5a1a5834e93ec5baa4294eada0c64");
 			Map<String, String> response = requester.lookUp(addr);
 			
 			DateFormat hostFormat = new SimpleDateFormat();
 			hostFormat.setTimeZone(TimeZone.getTimeZone("GMT"+response.get("timeZone")));
 			
 			passMessage(bot, chan, user, "The address " + addr.getHostName() + " (" + addr.getHostAddress() + ") points to " +
 					response.get("latitude") + ", " + response.get("longitude") + ", in " + response.get("cityName") + ", " +
 					response.get("regionName") + ", " + response.get("countryName") + " (" + response.get("countryCode") + ")" +
 					". Time is " + hostFormat.format(new Date()) + " (UTC" + response.get("timeZone") + ").");
 			
 		} catch (IOException e) {
 			passMessage(bot, chan, user, "There was a problem when looking up! " +
 					e.getClass().getSimpleName() + ": " + e.getMessage());
 			e.printStackTrace();
 			return;
 		}
 	}
 	
 	public static class IpInfoDb {
 		private static Pattern entryRegex = Pattern.compile("\"(.+)\"\\s*:\\s*\"(.+)\"");
 		
 		private String apiKey = "";
 		
 		public IpInfoDb(String apiKey) {
 			this.apiKey = apiKey;
 		}
 		
 		public Map<String, String> lookUp(InetAddress address) throws IOException {
 			Map<String, String> returnValues = new HashMap<>();
 			
 			String result = getResponse(address);
 			
 			Matcher matcher = entryRegex.matcher(result);
 			
 			while(matcher.find()) {
 				returnValues.put(matcher.group(1), matcher.group(2));
 			}
 			
 			return returnValues;
 		}
 		
 		private String getResponse(InetAddress address) throws IOException {
 			String result = "";
 			String requestUrl = "http://api.ipinfodb.com/v3/ip-city/?key=" + apiKey + "&ip=" + address.getHostAddress() + "&format=json";
 			URL url = new URL(requestUrl);
 			BufferedReader stream = new BufferedReader(new InputStreamReader(url.openStream()));
 			
 			String inputLine;
 			while((inputLine = stream.readLine()) != null) {
 				result += inputLine + System.getProperty("line.separator");
 			}
 			return result;
 		}
 	}
 }
