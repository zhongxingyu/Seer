 package com.bobacadodl.redditFeed;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class RedditFeed extends JavaPlugin{
 	int x=0;
 	public RedditFeedConfig cfg;
 	Random r = new Random();
	
	String lastPost = "";
 	public void onEnable(){
 		cfg= new RedditFeedConfig(this);
 		cfg.load();
 		
		
 		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
 			@Override
 			public void run() {
 				try {
 					URL url = new URL("http://www.reddit.com/r/"+cfg.subReddit+"/"+cfg.section+".json");
 					InputStream is = url.openStream();
 					BufferedReader rd = new BufferedReader(new InputStreamReader(is,Charset.forName("UTF-8")));
 					JsonObject o = JsonObject.readFrom(rd);
 					JsonObject data = o.get("data").asObject();					
 					JsonArray posts = data.get("children").asArray();
 					
					JsonObject post=null;
 					if(cfg.random){
 						post = posts.get(r.nextInt(posts.size()-1)).asObject();
 					}
 					else{
						post = posts.get(0).asObject();
						if(post.toString().equals(lastPost)){
							return;	
						}
						else{
							lastPost=post.toString();
						}
 					}
 					JsonObject postData = post.get("data").asObject();	
					
 					String author = postData.get("author").asString();
 					
 					JsonValue flairValue = postData.get("author_flair_text");
 					String flairText = null;
 					if(flairValue.isString())  flairText=flairValue.asString();
 					
 					
 					String title = postData.get("title").asString();
 					String ups = Integer.toString(postData.get("ups").asInt());
 					String downs = Integer.toString(postData.get("downs").asInt());
 					String urlstring = postData.get("url").asString();
 					
 					
 					getServer().broadcastMessage(" ");
 					if(flairText!=null){
 						getServer().broadcastMessage(ChatColor.AQUA+"["+author+"] ("+flairText+")- \""+ChatColor.GOLD+title+"\"");
 					}
 					else{
 						getServer().broadcastMessage(ChatColor.GOLD+"["+author+"]- \""+title+"\"");
 					}
 					
 					getServer().broadcastMessage(ChatColor.GREEN+ups+"▲" + ChatColor.BLACK+" | "+ChatColor.RED+downs+"▼");
 					getServer().broadcastMessage(ChatColor.BLUE+""+ChatColor.UNDERLINE+urlstring);
 					
 					is.close();
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			
 		}, cfg.interval*10, cfg.interval*20);
 	}
 }
