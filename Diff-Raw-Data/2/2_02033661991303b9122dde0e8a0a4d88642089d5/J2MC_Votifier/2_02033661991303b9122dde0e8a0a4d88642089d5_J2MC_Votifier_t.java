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
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.redeem.PackageBuilder;
 import to.joe.redeem.exception.CouponCodeAlreadyExistsException;
 
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
 
        PackageBuilder builder = new PackageBuilder().forPlayer(v.getUsername()).withExpiry(System.currentTimeMillis() / 1000L + 86400).withName(v.getServiceName() + " Voting Reward").withDescription("Thanks for voting!").withCreator("J2 Senior Staff").onServer("MC3");
         int[] prizes = { 2256, 2257, 2258, 2259, 2260, 2261, 2262, 2263, 2264, 2265, 2266, 2267, 84 };
         builder.withItemStack(new ItemStack(prizes[new Random().nextInt(prizes.length)]));
         builder.withItemStack(new ItemStack(Material.EMERALD));
 
         try {
             builder.build();
         } catch (SQLException | CouponCodeAlreadyExistsException e) {
             getLogger().log(Level.SEVERE, "Error granting voting reward", e);
         }
 
         l.info(v.getAddress());
         l.info(v.getServiceName());
         l.info(v.getTimeStamp());
         l.info(v.getUsername());
     }
 }
