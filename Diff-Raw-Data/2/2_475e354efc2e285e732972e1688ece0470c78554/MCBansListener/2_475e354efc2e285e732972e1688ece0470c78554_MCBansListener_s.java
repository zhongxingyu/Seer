 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.listener.feature
  * Created: 2013/01/25 0:27:36
  */
 package net.syamn.sakuracmd.listener.feature;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import net.syamn.sakuracmd.SakuraCmd;
 import net.syamn.utils.LogUtil;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
 import com.mcbans.firestar.mcbans.events.PlayerBannedEvent;
 
 /**
  * MCBansListener (MCBansListener.java)
  * @author syam(syamn)
  */
 public class MCBansListener implements Listener{
     private SakuraCmd plugin;
     public MCBansListener (final SakuraCmd plugin){
         this.plugin = plugin;
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerBanned(final PlayerBannedEvent event){
         String banType = "(undefined)";
         if (event.isGlobalBan()) banType = "Global";
         else if (event.isLocalBan()) banType = "Local";
         else if (event.isTempBan()) banType = "Temp";
 
         // Banned player Ip address if available
         String ip = event.getPlayerIP();
         ip = (ip != null && ip.length() > 0) ? ip : "";
 
         // Ban issued player
         String senderName = event.getSenderName();
         if (senderName.equalsIgnoreCase("console")){
                 senderName = "CONSOLE";
         }else{
                 Player player = Bukkit.getPlayer(senderName);
                 if (player != null && player.isOnline()){
                         senderName = player.getName();
                 }
         }
 
         // Build reasons
         Map<String, String> reasons = getReasons(event.getReason());
         if (reasons.size() <= 0){
             reasons.put("(未設定)", event.getReason());
         }
         
         String target = event.getPlayerName();
         // * Build posting data
         // Title
         String title = banType + ": " + target + " - Banned by: " + senderName;
         
         // Body
         String body = "";
         final String mcblink = "http://mcbans.com/player/" + target;
         
         // japanese
         body += "[u]このプレイヤーは以下の通りBANされています[/u]\n\n";
         body += "[b]プレイヤー名[/b]: " + target + " ([url=" + mcblink + "]MCBansで確認[/url])\n";
         body += "[b]BANを行ったスタッフ[/b]: " + senderName + "\n";
         body += "[b]BAN種類[/b]: " + banType + "\n";
         body += "[b]BAN理由[/b]:\n";
         if (reasons.size() > 0){
             body += "[list]\n";
             for (final String key : reasons.keySet()){
                 body += "[*]" + key + "\n";
             }
             body += "[/list]";
         }
         
         // english
         body += "\n\n[u]This player has been banned from this server[/u]\n\n";
         body += "[b]Banned player[/b]: " + target + " ([url=" + mcblink + "]Check on MCBans[/url])\n";
         body += "[b]Ban issued by[/b]: " + senderName + "\n";
         body += "[b]Ban type[/b]: " + banType + "\n";
         body += "[b]Reason(s)[/b]:\n";
         if (reasons.size() > 0){
             body += "[list]\n";
             for (final String key : reasons.values()){
                 body += "[*]" + key + "\n";
             }
             body += "[/list]\n";
         }
         
         body += "\n[b]証拠 / Proof[/b]:\n";
         if (Pattern.compile("(honeypot)").matcher(event.getReason().toLowerCase(Locale.ENGLISH)).find()){
            body += "[list][*]HoneyPot Logger: [url]http://sakura-server.net/hp/[/url][/list]";
         }
         
         try{
             title = URLEncoder.encode(title, "UTF-8");
             body = URLEncoder.encode(body, "UTF-8");
         }catch (UnsupportedEncodingException ex){
             ex.printStackTrace();
         }
         postForum(title, body);
     }
     
     private Map<String, String> getReasons(final String reason){
         Map<String, String> ret = new HashMap<String, String>();
         
         final String s = reason.toLowerCase(Locale.ENGLISH);
         
         // japanese - english //fly|hack|nodus|glitch|exploit|NC|cheat|nuker|x-ray|xray
         if (Pattern.compile("(grief|broke)").matcher(s).find()){
             ret.put("破壊", "Griefing");
         }
         
         if (Pattern.compile("(steal|theft)").matcher(s).find()){
             ret.put("窃盗", "Stealing");
         }
         
         if (Pattern.compile("(xray|x-ray)").matcher(s).find()){
             ret.put("鉱石検知クライアント", "X-ray related modificaiton");
         }
         
         if (Pattern.compile("(racism)").matcher(s).find()){
             ret.put("人種差別発言", "Racism");
         }
         
         if (Pattern.compile("(sexism|homophobia)").matcher(s).find()){
             ret.put("性差別発言", "Sexism/Homophobia");
         }
         
         if (Pattern.compile("(nazi (symbol|skin))").matcher(s).find()){
             ret.put("ナチスシンボル/スキン", "Nazi symbols/skins");
         }
         
         if (Pattern.compile("(spambot|spam bot)").matcher(s).find()){
             ret.put("スパムボット", "Spambot");
         }
         else if (Pattern.compile("(spam)").matcher(s).find()){
             ret.put("スパム行為", "Spamming");
         }
         
         if (Pattern.compile("(honeypot)").matcher(s).find()){
             ret.put("ハニーポット破壊", "Destroyed HoneyPot(s)");
         }
         if (Pattern.compile("(fly|hack|nodus|cheat|nuker)").matcher(s).find()){
             ret.put("不正クライアント", "Hacked Client");
         }
         
         if (Pattern.compile("(advertis)").matcher(s).find()){
             ret.put("広告/宣伝", "Advertising");
         }
         
         return ret;
     }
     
     private void postForum(final String title, final String body){
         Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable(){
             public void run(){
                 InputStream is = null;
                 BufferedReader br = null;
                 String res = "Error,error";
                 try{ 
                     URL url = new URL("http://forum.sakura-server.net/api/ban.php?title=" + title + "&body=" + body);
                     is = ((InputStream)url.getContent());
                     br = new BufferedReader(new InputStreamReader(is));
                     res = br.readLine();
                 }
                 catch (Exception ex){
                     ex.printStackTrace();
                 }
                 finally{
                     if (br != null){
                         try { br.close(); }
                         catch (IOException ignore) {}
                     }
                     if (is != null){
                         try { is.close(); }
                         catch (IOException ignore) {}
                     }
                 }
                 
                 String[] s = res.split(",");
                 if (s.length < 2){
                     LogUtil.warning("Could not post to ban forum, Invalid response(len=" + s.length + "): " + res);
                 }
                 else if ("Error".equals(s[0])){
                     LogUtil.warning("Could not post to ban forum: " + s[1]);
                 }
                 else if ("OK".equals(s[0])){
                     LogUtil.info("Posted ban data to forum! PostID: " + s[1]);
                 }
                 else{
                     LogUtil.warning("Could not post to ban forum, undefined error: " + res);
                 }
             }
         });
     }
 }
