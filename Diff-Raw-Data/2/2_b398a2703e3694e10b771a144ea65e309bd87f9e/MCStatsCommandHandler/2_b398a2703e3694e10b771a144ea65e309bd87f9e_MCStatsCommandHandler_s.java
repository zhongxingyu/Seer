 package com.drtshock.willie.command.minecraft;
 
 import com.drtshock.willie.Willie;
 import com.drtshock.willie.command.CommandHandler;
 import com.drtshock.willie.util.WebHelper;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.User;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 public class MCStatsCommandHandler implements CommandHandler {
 
     @Override
     public void handle(Willie bot, Channel channel, User sender, String[] args) throws Exception {
         if (args.length != 1) {
             channel.sendMessage(Colors.RED + "Look up a plugin with !plugin <name>");
             return;
         }
 
         String mcStatsURL = "http://mcstats.org/plugin/";
         String pluginStatsURL = mcStatsURL + args[0];
 
         try {
             Document doc = getPage(pluginStatsURL);
 
             PluginStats stats = PluginStats.get(doc);
 
             channel.sendMessage(Colors.BOLD + "MCStats" + Colors.NORMAL + " informations for plugin " + Colors.DARK_GREEN + stats.name);
             channel.sendMessage("Rank: " + Colors.BOLD + stats.rank + Colors.NORMAL + " (" + colorizeDiff(stats.rankDiff) +
                                 ") | Servers: " + Colors.BOLD + stats.servers + Colors.NORMAL + " (" + colorizeDiff(stats.serversDiff) +
                                 ") | Players: " + Colors.BOLD + stats.players + Colors.NORMAL + " (" + colorizeDiff(stats.playersDiff));
             channel.sendMessage("For more informations: " + WebHelper.shortenURL(pluginStatsURL));
         } catch (FileNotFoundException | MalformedURLException | IndexOutOfBoundsException e) {
            channel.sendMessage(Colors.RED + "Plugin unknown by MCStats");
         } catch (IOException e) {
             channel.sendMessage(Colors.RED + "Failed: " + e.getMessage());
             throw e; // Gist
         }
     }
 
     private Document getPage(String urlString) throws IOException {
         URL url = new URL(urlString);
 
         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 
         connection.setConnectTimeout(10000);
         connection.setReadTimeout(10000);
         connection.setUseCaches(false);
 
         BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
         StringBuilder buffer = new StringBuilder();
         String line;
 
         while ((line = input.readLine()) != null) {
             buffer.append(line);
             buffer.append('\n');
         }
 
         String page = buffer.toString();
 
         input.close();
 
         return Jsoup.parse(page);
     }
 
     private static class PluginStats {
 
         public String name;
         public String rank;
         public String rankDiff;
         public String servers;
         public String serversDiff;
         public String players;
         public String playersDiff;
 
         private PluginStats() {
         }
 
         public static PluginStats get(Document doc) {
             PluginStats res = new PluginStats();
             res.name = doc.getElementsByClass("open").get(0).getElementsByTag("strong").get(0).ownText().trim();
 
             Element statBoxes = doc.getElementsByClass("stat-boxes").get(0);
 
             Element rankLi = statBoxes.child(0);
             Element serversLi = statBoxes.child(1);
             Element playersLi = statBoxes.child(2);
 
             Element rankLiStrong = rankLi.getElementsByClass("right").get(0).getElementsByTag("strong").get(0);
             if (rankLiStrong.children().size() == 0) {
                 res.rank = rankLiStrong.ownText().trim();
             } else {
                 res.rank = rankLiStrong.child(0).ownText().trim();
             }
             res.rankDiff = rankLi.getElementsByClass("left").get(0).ownText().trim();
             res.rankDiff = res.rankDiff.replace("&plusmn;", "±");
 
             Element serversLiStrong = serversLi.getElementsByClass("right").get(0).getElementsByTag("strong").get(0);
             if (serversLiStrong.children().size() == 0) {
                 res.servers = serversLiStrong.ownText().trim();
             } else {
                 res.servers = serversLiStrong.child(0).ownText().trim();
             }
             res.serversDiff = serversLi.getElementsByClass("left").get(0).ownText().trim();
             res.serversDiff = res.serversDiff.replace("&plusmn;", "±");
 
             Element playersLiStrong = playersLi.getElementsByClass("right").get(0).getElementsByTag("strong").get(0);
             if (playersLiStrong.children().size() == 0) {
                 res.players = playersLiStrong.ownText().trim();
             } else {
                 res.players = playersLiStrong.child(0).ownText().trim();
             }
             res.playersDiff = playersLi.getElementsByClass("left").get(0).ownText().trim();
             res.playersDiff = res.playersDiff.replace("&plusmn;", "±");
 
             return res;
         }
     }
 
     private String colorizeDiff(String diff) {
         if (diff.contains("+")) {
             return Colors.BOLD + Colors.DARK_GREEN + diff + Colors.NORMAL;
         } else if (diff.contains("-")) {
             return Colors.BOLD + Colors.RED + diff + Colors.NORMAL;
         } else {
             return Colors.BOLD + Colors.DARK_GRAY + diff + Colors.NORMAL;
         }
     }
 
 }
