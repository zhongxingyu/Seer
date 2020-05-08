 package to.joe.j2mc.votifier;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.redemption.J2MC_Redemption;
 
 import com.vexsoftware.votifier.model.Vote;
 import com.vexsoftware.votifier.model.VotifierEvent;
 
 public class J2MC_Votifier extends JavaPlugin implements Listener {
 
     @Override
     public void onEnable() {
         this.getServer().getPluginManager().registerEvents(this, this);
     }
 
     @EventHandler
     public void onVote(VotifierEvent event) {
         Vote v = event.getVote();
         Logger l = getLogger();
 
         try {
             PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("INSERT INTO votes (address, service, timestamp, username) VALUES (?,?,?,?)");
             ps.setString(1, v.getAddress());
             ps.setString(2, v.getServiceName());
             if (v.getServiceName().equals("Minestatus")) {
                 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
                 ps.setTimestamp(3, new Timestamp(sdf.parse(v.getTimeStamp()).getTime()));
             } else {
                 ps.setTimestamp(3, new Timestamp(Long.parseLong(v.getTimeStamp()) * 1000));
             }
             ps.setString(4, v.getUsername());
             ps.execute();
         } catch (SQLException e) {
             getLogger().log(Level.SEVERE, "Something went wrong logging the vote in the database", e);
         } catch (ParseException e) {
             getLogger().log(Level.SEVERE, "Date parse error. Votifier must have fed us garbage", e);
         }
 
         getServer().broadcastMessage(ChatColor.RED + v.getUsername() + ChatColor.AQUA + " has just voted for the server!");
         getServer().broadcastMessage(ChatColor.RED + "Visit http://joe.to/vote for details on how to vote and claim rewards");
 
         try {
            int id = J2MC_Redemption.newCoupon(v.getUsername(), false, v.getServiceName(), System.currentTimeMillis() / 1000L + 86400, 3);
             if (id != -1) {
                 int[] prizes = { 2256, 2257, 2258, 2259, 2260, 2261, 2262, 2263, 2264, 2265, 2266, 2267, 84 };
                 J2MC_Redemption.addItem(id, prizes[new Random().nextInt(prizes.length)]);
             }
             J2MC_Redemption.addItem(id, 388, 3);
         } catch (SQLException e) {
             l.log(Level.SEVERE, "Error adding voting rewards", e);
         }
 
         l.info(v.getAddress());
         l.info(v.getServiceName());
         l.info(v.getTimeStamp());
         l.info(v.getUsername());
     }
 
 }
