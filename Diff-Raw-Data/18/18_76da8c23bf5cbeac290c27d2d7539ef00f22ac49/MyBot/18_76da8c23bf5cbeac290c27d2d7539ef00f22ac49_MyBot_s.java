 import org.jibble.pircbot.*;
 import org.jsoup.*;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import java.io.IOException;
 
 public class MyBot extends PircBot {
 
     public MyBot() {
         this.setName("Java_Bot");
     }
 
     public void onMessage(String channel, String sender,
                        String login, String hostname, String message) {
 		if (message.matches("^Java_Bot\\sdate")){
 			String time = new java.util.Date().toString();
 			sendMessage(channel, sender + ": The date and time is: " + time);
 		}
 		else if (message.matches("Java_Bot\\shelp")){
 			sendMessage(channel, sender + ": The only thing i can do right now is:");
 			sendMessage(channel, "'Java_Bot date' and 'Java_Bot help' and 'Java_Bot metallo <summonername>' sorry!!");
 		}
 		else if (message.matches("^Java_Bot metallo (.*)")){
 			String[] names = message.substring(16).split("\\s");
 			String summonerName = "", sendName = "";
 			for(int i = 1;i < names.length;i++) {
					sendName += names[i];
					summonerName += names[i];
					if(i != names.length-1)
					{
						summonerName += " ";
					}
 			}
 			try {
 				Connection con = Jsoup.connect("http://www.lolking.net/search?name="+sendName);
 				con.timeout(0);
 				con.userAgent("Mozilla");
 				Document doc = con.get();
 				Elements euneBox = doc.select("div[onclick^=window.location='/summoner/eune/]");
 				String rank = euneBox.select("div[style=font-size: 14px; color: #FFF; text-shadow: 0 0 1px #000;]").text();
 				if (rank.isEmpty()) {
 					sendMessage(channel, sender + ": Summoner " + summonerName + " is unranked or not exist!");
 				}
 				else {
					sendMessage(channel, sender + ": Summoner " + summonerName + " is " + rank);
 				}
 			}
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else if (message.matches("^Java_Bot.*")) {
 			sendMessage(channel, sender + ": I'm under construction. Leave me alone! Type Java_Bot help for more info!!");
 		}
     }
 }
