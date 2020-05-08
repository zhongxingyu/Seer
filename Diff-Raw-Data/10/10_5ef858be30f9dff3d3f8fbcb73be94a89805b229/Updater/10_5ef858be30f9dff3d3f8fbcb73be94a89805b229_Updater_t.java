 package net.worldoftomorrow.nala.ni;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 
 public class Updater {
 
 	private int current = 0;
 	private int latest = 0;
	private static final String recurl = "http://ci.worldoftomorrow.net/job/NoItem/RecommendedBuild/buildNumber";
	private static final String devurl = "http://ci.worldoftomorrow.net/job/NoItem/lastSuccessfulBuild/buildNumber";
 
 	public Updater(NoItem plugin) {
 		if (!Config.getBoolean("CheckForUpdates")) {
 			return;
 		}
 		
 		String[] parts = plugin.getDescription().getVersion().split("-");
 		if(parts.length == 2) {
 			//Release version
 			this.current = Integer.parseInt(parts[1]);
 		} else if (parts.length == 3) {
 			//Snapshot or beta build
 			this.current = Integer.parseInt(parts[2]);
 		}
 		
 		try {
 			URL site;
 			final String channel = Config.getString("PluginChannel");
 			if (channel.equalsIgnoreCase("main")) {
 				site = new URL(recurl);
 			} else if (channel.equalsIgnoreCase("dev")) {
 				site = new URL(devurl);
 		    } else {
 				Log.warn("The configuration has an invalid value for \"PluginChannel\", assuming main.");
 				site = new URL(recurl);
 			}
 			URLConnection conn = site.openConnection();
 			//conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
 			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
 			String line;
 			while ((line = in.readLine()) != null) {
 				this.latest = Integer.parseInt(line);
 				break;
 			}
 			in.close();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (UnknownHostException e) {
 			Log.warn("[NoItem] Could not check for update.");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public boolean isLatest() {
 		return this.latest == this.current;
 	}
 }
