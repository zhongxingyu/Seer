 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.cnaude.plugin;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author cnaude
  */
 public class tx0 extends JavaPlugin {
 
     @Override
     public void onEnable() {
         registerCommand("tx0");
     }
 
     private void registerCommand(String command) {
         try {
             getCommand(command).setExecutor(new tx0Command(this));
         } catch (Exception ex) {
             System.out.println("Failed to register command '" + command + "'! Is it allready used by some other Plugin? " + ex.getMessage());
         }
     }
 
     public void translateURL(String s, String sender) {
         try {
             // Construct data            
            if (!s.toLowerCase().startsWith("http://")) {                
                 s = "http://" + s;
             }         
             getServer().broadcastMessage(sender + ChatColor.WHITE + ": " + ChatColor.YELLOW + s);
             String data = URLEncoder.encode("url", "UTF-8") + "=" + URLEncoder.encode(s, "UTF-8");            
 
             // Send data
             URL url = new URL("http://tx0.org");
             URLConnection conn = url.openConnection();
             conn.setDoOutput(true);
             OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
             wr.write(data);
             wr.flush();
 
             // Get the response
             BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             String line;
             while ((line = rd.readLine()) != null) {
                 Pattern p = Pattern.compile(">(http://.*?)<");
                 //>http://tx0.org/435<
                 Matcher m = p.matcher(line);
                 while(m.find()) {		
                    getServer().broadcastMessage(sender + ChatColor.WHITE + ": " + ChatColor.YELLOW + m.group(1));                   
                 }
             }
             wr.close();
             rd.close();
         } catch (Exception e) {
             System.out.println(e.getMessage());
         }
     }
 }
