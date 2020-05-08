 package me.iaccidentally.mailbox;
 
import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.milkbowl.vault.economy.Economy;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MailBoxPlugin extends JavaPlugin
 {
   private final MailBoxPlayerListener playerListener = new MailBoxPlayerListener(this);
   private final HashMap<Player, Boolean> debugees = new HashMap();
   public final HashMap<Player, String> akcia = new HashMap();
   public final ArrayList<Record> cooldown = new ArrayList();
   private static String db_file = "db_file.db";
   private String mysql_database = "";
   private String mysql_user = "";
   private String mysql_pass = "";
   private boolean use_mysql = false;
   private int cooldown_limit = 20;
   private double delivery_fee = 5.0D;
   private double creating_fee = 5.0D;
   private double fee_per_1000m = 0.0D;
   private double foreign_fee = 0.0D;
   private double postman_profit = 0.0D;
   private boolean only_op = false;
   private static final Logger log = Logger.getLogger("Minecraft");
   private boolean economy_on = false;
   private static Economy economy = null;
   private boolean is_running = true;
 
   public boolean isRunning() {
     return this.is_running;
   }
 
   @Override
   public void onEnable()
   {
     File file = getDataFolder();
     db_file = getDataFolder() + "//" + db_file;
 
     if (!file.exists()) {
       file.mkdirs();
     }
 
     File settings = new File(file, "settings.txt");
     if (!settings.exists()) {
       try {
         settings.createNewFile();
         FileWriter fstream = new FileWriter(settings);
         BufferedWriter out = new BufferedWriter(fstream);
 
         out.write("cooldown_limit = 50\r\n");
         out.write("delivery_fee = 5.0\r\n");
         out.write("creating_fee = 0.0\r\n");
         out.write("fee_per_1000m = 4.0\r\n");
         out.write("foreign_fee = 10.0\r\n");
         out.write("postman_profit = 0\r\n");
         out.write("only_op = false\r\n");
         out.write("use_mysql = false\r\n");
         out.write("mysql_database = jdbc:mysql://localhost:3306/mailbox\r\n");
         out.write("mysql_user = root\r\n");
         out.write("mysql_pass = password");
         out.close();
       }
       catch (IOException e)
       {
         e.printStackTrace();
       }
     }
 
     Thread thread1 = new Thread(new Timer(this), "mailbox_timer");
     thread1.start();
 
     prepareSettings();
 
     this.economy_on = setupEconomy().booleanValue();
     if (!this.economy_on)
     {
 	log.info("[MailBox] Economy plugin not found.");
     }
     else 
     {
         log.log(Level.INFO, "[MailBox] Hooked into {0}", economy.getName());
     }
 
     prepareTables();
 
    checkVersion();
 
     getServer().getPluginManager().registerEvents(new MailBoxPlayerListener(this), this);
 
     PluginDescriptionFile pdfFile = getDescription();
     log.log(Level.INFO, "{0} version {1} is enabled! (MySQL is {2}.)", new Object[]{pdfFile.getName(), pdfFile.getVersion(), this.use_mysql ? "enabled" : "disabled"});
   }
 
   private Boolean setupEconomy()
   {
     if (getServer().getPluginManager().getPlugin("Vault") == null) 
     {
         return Boolean.valueOf(false);
     }
 
     RegisteredServiceProvider rsp = getServer().getServicesManager().getRegistration(Economy.class);
 
     if (rsp == null)
     {
 	return Boolean.valueOf(false);
     }
 
     economy = (Economy)rsp.getProvider();
     if (economy != null)
     {
 	return Boolean.valueOf(true);
     } 
     return Boolean.valueOf(false);
   }
 
   public boolean checkpermissions(Player player, String string)
   {
     return (player.hasPermission(string)) || (player.isOp());
   }
 
   public void prepareSettings() {
     File file = getDataFolder();
     File settings = new File(file, "settings.txt");
     try
     {
       Scanner sc = new Scanner(settings);
       this.cooldown_limit = Integer.parseInt(sc.nextLine().split(" = ")[1]);
       this.delivery_fee = Double.parseDouble(sc.nextLine().split(" = ")[1]);
       this.creating_fee = Double.parseDouble(sc.nextLine().split(" = ")[1]);
       this.fee_per_1000m = Double.parseDouble(sc.nextLine().split(" = ")[1]);
       this.foreign_fee = Double.parseDouble(sc.nextLine().split(" = ")[1]);
       this.postman_profit = Double.parseDouble(sc.nextLine().split(" = ")[1]);
       this.only_op = sc.nextLine().split(" = ")[1].equals("true");
       this.use_mysql = sc.nextLine().split(" = ")[1].equals("true");
       this.mysql_database = sc.nextLine().split(" = ")[1];
       this.mysql_user = sc.nextLine().split(" = ")[1];
       this.mysql_pass = sc.nextLine().split(" = ")[1];
     }
     catch (Exception e)
     {
       log.warning("Problem with config file. Check file: plugin/MailBox/settings.txt");
     }
   }
 
   @Override
   public void onDisable()
   {
     this.is_running = false;
     System.out.println("Plugin MailBox shutting down...");
   }
 
   public Connection prepareSQL() {
     Connection conn = null;
     try
     {
       if (this.use_mysql) 
       {
         conn = DriverManager.getConnection(this.mysql_database + "?autoReconnect=true&user=" + this.mysql_user + "&password=" + this.mysql_pass);
       } 
       else 
       {
         Class.forName("org.sqlite.JDBC");
         conn = DriverManager.getConnection("jdbc:sqlite:" + db_file);
       }
     }
     catch (Exception e) {
         log.warning("Problem with connection to SQL database. Check settings in plugin/MailBox/settings.txt file");
     }
     return conn;
   }
 
  public void checkVersion()
   {
     InputStream is = null;
 
     PluginDescriptionFile pdfFile = getDescription();
     int verzia = (int)Math.round(Double.parseDouble(pdfFile.getVersion()) * 100.0D);
     try
     {
       URL u = new URL("http://mcplugins.pro-webdesign.cz/query.php?v=" + verzia + "&p=MailBox");
 
       is = u.openStream();
 
       InputStreamReader isr = new InputStreamReader(is);
 
       BufferedReader br = new BufferedReader(isr);
       String s1;
       while ((s1 = br.readLine()) != null)
       {
         System.out.println("[MailBox]: " + s1);
       }
 
       is.close();
     }
     catch (Exception e)
     {
       log.info("[MailBox]: Can't find out information about newest plugin version.");
     }
  }
 
   public void prepareTables()
   {
     Connection conn = prepareSQL();
 
     boolean alter = false;
     try 
     {
       Statement stmt = conn.createStatement();
 
       String QueryString = "CREATE TABLE IF NOT EXISTS mailboxes (playername VARCHAR(255) PRIMARY KEY, coordinates VARCHAR(20), world VARCHAR(30), date BIGINT)";
 
       stmt.executeUpdate(QueryString);
 
       QueryString = "CREATE TABLE IF NOT EXISTS logs (sender VARCHAR(255), receiver VARCHAR(255), material VARCHAR(50), amount SMALLINT, date BIGINT)";
 
       stmt.executeUpdate(QueryString);
 
       alter = true;
       QueryString = "ALTER TABLE mailboxes ADD createdby VARCHAR(255)";
       stmt.executeUpdate(QueryString);
 
       conn.close();
     }
     catch (SQLException e)
     {
       if (!alter)
       {
 	log.warning("Problem with SQL. Check config file /plugin/MailBox/settings.txt");
       }
     }
   }
 
   public String removeMailBox(Player player, String owner)
   {
     Connection conn = prepareSQL();
     try
     {
       Statement stmt = conn.createStatement();
       String replace = owner.replace('\'', 'x');
 
       boolean remove_self = true;
       if (!player.getName().equals(owner)) 
       {
         remove_self = false;
       }
 
       String QueryString = "SELECT * FROM mailboxes WHERE LOWER(playername) = '" + 
         owner.toLowerCase() + "'";
 
       ResultSet rs = stmt.executeQuery(QueryString);
 
       boolean remove = false;
       if (!rs.next()) 
       {
         if (remove_self) 
 	{
           return "You don't have a mailbox yet! Use §c/mailbox create§f to create it.";
         }
         return "Player " + owner + " don't have a mailbox!";
       }
 
       if (remove_self)
       {
 	remove = true;
       }
       if (checkpermissions(player, "mailbox.admin.removeany"))
       {
 	remove = true;
       }
       if ((checkpermissions(player, "mailbox.postman.removeother")) && (rs.getString("createdby").equals(player.getName()))) 
       {
         remove = true;
       }
 
       if (!remove) 
       {
         return "§cYou don't have permission for remove this mailbox!";
       }
 
       String DeleteQuery = "DELETE FROM mailboxes WHERE LOWER(playername) = '" + owner.toLowerCase() + "'";
 
       stmt.execute(DeleteQuery);
 
       log.log(Level.INFO, "MailBox: {0} has removed {1}''s mailbox.", new Object[]{player.getName(), owner});
 
       if (remove_self) 
       {
         return "§aMailbox removed successfully.";
       }
       Player pl = Bukkit.getServer().getPlayer(owner);
       if (pl != null)
       {
 	pl.sendMessage("§cYour mailbox was removed by " + player.getName() + "!");
       }
       return "§a" + owner + "'s mailbox removed successfully.";
     }
     catch (SQLException e)
     {
       log.warning("Problem with SQL. Check config file /plugin/MailBox/settings.txt");
     }
 
     return "§4Error!";
   }
 
   public String createMailBox(Player player, String owner, Block block)
   {
     Connection conn = prepareSQL();
     String replace = owner.replace('\'', 'x');
 
     boolean self_creating = true;
     Player pl = null;
 
     if (!owner.equals(player.getName()))
     {
       self_creating = false;
       pl = Bukkit.getServer().getPlayer(owner);
       if (pl == null)
       {
 	return "Player " + owner + " has to be online during mailbox creating!";
       }
       owner = pl.getName();
     }
     try
     {
       Statement stmt = conn.createStatement();
 
       String QueryString = "SELECT COUNT(*) AS pocet FROM mailboxes WHERE LOWER(playername) = '" + 
         owner.toLowerCase() + "'";
 
       ResultSet rs = stmt.executeQuery(QueryString);
       rs.next();
 
       if (rs.getInt("pocet") > 0) {
         conn.close();
         if (self_creating) 
 	{
           return "You already have a mailbox! Use §c/mailbox remove§f to remove it.";
         }
         return pl.getName() + " already has a mailbox!";
       }
 
       if (block.getType() != Material.CHEST) 
       {
         return "§cBlock is not a chest!";
       }
 
       String loc = block.getX() + ";" + block.getY() + ";" + block.getZ();
 
       String coordinatesQuery = "SELECT COUNT(*) AS pocet FROM mailboxes WHERE coordinates = '" + 
         loc + "'";
 
       ResultSet rs2 = stmt.executeQuery(coordinatesQuery);
       rs2.next();
 
       if (rs2.getInt("pocet") > 0) 
       {
         conn.close();
         return "§cOn this position is already created mailbox. Choose another one.";
       }
 
       if (this.economy_on)
       {
         if (!economy.has(player.getName(), this.creating_fee)) {
           player.sendMessage("§cYou do not have enough money for create a mailbox!");
           return "Fee is §b" + economy.format(this.creating_fee) + "§f.";
         }
         economy.depositPlayer(player.getName(), -this.creating_fee);
       }
 
       String InsertQuery = "INSERT INTO mailboxes (playername, createdby, coordinates, world, date) VALUES ('" + 
         owner + "', '" + 
         player.getName() + "', '" + 
         loc + "', '" + 
         player.getWorld().getName() + "', " + 
         System.currentTimeMillis() + ")";
 
       stmt.execute(InsertQuery);
       log.log(Level.INFO, "MailBox: {0} has created the mailbox for {1}. [{2}]", new Object[]{player.getName(), owner, loc});
 
       if (self_creating) 
       {
         return "§aMailbox created successfully.";
       }
       pl.sendMessage("§aYour mailbox was created successfully by " + player.getName() + "!");
       return "§a" + pl.getName() + "'s mailbox created successfully.";
     }
     catch (SQLException e)
     {
       log.warning("Problem with SQL. Check config file /plugin/MailBox/settings.txt");
     }
 
     return "§4Error!";
   }
 
   public void viewLog(Player player, int page)
   {
     Connection conn = prepareSQL();
     try
     {
       Statement stmt = conn.createStatement();
 
       int limit = 5;
       int from = page * limit;
       String query = "SELECT * FROM logs ORDER BY date DESC LIMIT " + from + ", " + limit;
 
       ResultSet rs = stmt.executeQuery(query);
       ArrayList ar = new ArrayList();
 
       if (!rs.next()) 
       {
         player.sendMessage("§cNo records on this page!");
         return;
       }
 
       player.sendMessage("§cPage: " + (page + 1));
       do
       {
         String line = "§a" + rs.getString("sender") + "§f --> §b" + 
           rs.getString("receiver") + "§f :  " + rs.getString("material") + " §c" + rs.getInt("amount");
         ar.add(line);
       }
       while (rs.next());
         for (int i = ar.size() - 1; i >= 0; i--) {
         player.sendMessage((String)ar.get(i));
       }
     }
     catch (SQLException e) 
     {
       log.warning("Problem with SQL. Check config file /plugin/MailBox/settings.txt");
 
       player.sendMessage("§4Error!");
     }
   }
 
   public String check(Player sender, String receiver) 
   {
     receiver = receiver.replace('\'', 'x');
 
     Connection conn = prepareSQL();
 
     Server server = Bukkit.getServer();
     try
     {
       Statement stmt = conn.createStatement();
 
       String QueryString = "SELECT * FROM mailboxes WHERE LOWER(playername) = '" + receiver.toLowerCase() + "'";
 
       ResultSet rs = stmt.executeQuery(QueryString);
       boolean naslo = rs.next();
 
       if (!naslo) 
       {
         return "§cPlayer " + receiver + " doesn't have a mailbox!";
       }
       receiver = rs.getString("playername");
       String world = rs.getString("world");
       String[] suradnice = rs.getString("coordinates").split(";");
       int x = Integer.parseInt(suradnice[0]);
       int y = Integer.parseInt(suradnice[1]);
       int z = Integer.parseInt(suradnice[2]);
 
       Location l = new Location(server.getWorld(world), x, y, z);
 
       if (l.getBlock().getType() != Material.CHEST) 
       {
         return "§c" + receiver + "'s mailbox is damaged!";
       }
 
       double fee = 0.0D;
       double distance = 0.0D;
       double distance_fee = 0.0D;
 
       DecimalFormat df = new DecimalFormat("0.00");
       boolean foreign = false;
 
       if (!sender.getWorld().equals(l.getWorld())) {
         fee = this.delivery_fee + this.foreign_fee;
         foreign = true;
       } else {
         distance = sender.getLocation().distance(l);
         distance_fee = distance / 1000.0D * this.fee_per_1000m;
         fee = this.delivery_fee + distance_fee;
       }
 
       if (this.economy_on)
       {
         sender.sendMessage("Fee for sending package to " + receiver + " is §b" + economy.format(fee) + "§f.");
         if (foreign)
 	{
 	  sender.sendMessage("§b" + df.format(this.delivery_fee) + "§f is fixed fee + §b" + df.format(this.foreign_fee) + "§f is fee for sending package to another world.");
 	}
         else
 	{
 	  sender.sendMessage("§b" + df.format(this.delivery_fee) + "§f is fixed fee + §b" + df.format(distance_fee) + "§f is fee for distance. (" + (int)distance + " m)");
 	}
       }
       else 
       {
         sender.sendMessage("economy is off!");
       }
 
       return "";
     }
     catch (SQLException e) 
     {
       log.warning("Problem with SQL. Check config file /plugin/MailBox/settings.txt");
     }
 
     return "§4Error!";
   }
 
   public String sendPackage(Player sender, String receiver)
   {
     receiver = receiver.replace('\'', 'x');
 
     if ((!checkpermissions(sender, "mailbox.sendtoself")) && (sender.getName().toLowerCase().equals(receiver.toLowerCase()))) {
       return "§cYou can't send package to yourself!";
     }
 
     int cas = 0;
     try
     {
       for (int i = 0; i < this.cooldown.size(); i++)
       {
 	if (((Record)this.cooldown.get(i)).getPl().equalsIgnoreCase(sender.getName())) 
 	{
 	  cas = ((Record)this.cooldown.get(i)).getTime();
 	  return "§fYou have to wait §b" + cas + "§f seconds for sending new package.";
 	}
       }
     }
     catch (Exception e)
     {
       log.warning("Problem with cooldown thread!");
     }
 
     Connection conn = prepareSQL();
 
     Server server = Bukkit.getServer();
     try
     {
       Statement stmt = conn.createStatement();
 
       String QueryString = "SELECT * FROM mailboxes WHERE LOWER(playername) = '" + receiver.toLowerCase() + "'";
 
       ResultSet rs = stmt.executeQuery(QueryString);
       boolean naslo = rs.next();
 
       if (!naslo) 
       {
         return "§cPlayer " + receiver + " doesn't have a mailbox!";
       }
       receiver = rs.getString("playername");
       String postman = rs.getString("createdby");
 
       String world = rs.getString("world");
       String[] suradnice = rs.getString("coordinates").split(";");
       int x = Integer.parseInt(suradnice[0]);
       int y = Integer.parseInt(suradnice[1]);
       int z = Integer.parseInt(suradnice[2]);
 
       Location l = new Location(server.getWorld(world), x, y, z);
 
       double distance = 0.0D;
       double distance_fee = 0.0D;
       double fee = 0.0D;
 
       DecimalFormat df = new DecimalFormat("0.00");
       boolean foreign = false;
 
       if (!sender.getWorld().equals(l.getWorld())) {
         fee = this.delivery_fee + this.foreign_fee;
         foreign = true;
       } else {
         distance = sender.getLocation().distance(l);
         distance_fee = distance / 1000.0D * this.fee_per_1000m;
         fee = this.delivery_fee + distance_fee;
       }
 
       if (this.economy_on)
       {
         if (!economy.has(sender.getName(), fee)) {
           sender.sendMessage("§cYou don't have enough money for send a package!");
           sender.sendMessage("Fee is §b" + economy.format(fee) + "§f.");
 
           if (foreign)
 	  {
 	    sender.sendMessage("§b" + df.format(this.delivery_fee) + "§f is fixed fee + §b" + df.format(this.foreign_fee) + "§f is fee for sending package to another world.");
 	  }
           else 
           {
             sender.sendMessage("§b" + df.format(this.delivery_fee) + "§f is fixed fee + §b" + df.format(distance_fee) + "§f is fee for distance. (" + (int)distance + " m)");
           }
           return "";
         }
 
         economy.depositPlayer(sender.getName(), -fee);
       }
 
       Chest chest = (Chest)l.getBlock().getState();
 
       ItemStack _package = sender.getInventory().getItemInHand();
 
       if (_package.getType() == Material.AIR)
       {
         if (this.economy_on) 
 	{
           economy.depositPlayer(sender.getName(), fee);
         }
         return "§cYou have nothing in your hand!";
       }
 
       ItemStack package_copy = new ItemStack(_package.getType(), _package.getAmount());
       package_copy.setDurability(_package.getDurability());
 
       HashMap nevoslo = chest.getInventory().addItem(new ItemStack[] { _package });
 
       if (nevoslo.size() > 0) 
       {
         chest.getInventory().removeItem(new ItemStack[] { new ItemStack(_package.getType(), _package.getAmount() - _package.getAmount()) });
         sender.setItemInHand(package_copy);
         Player pl = server.getPlayer(receiver);
         if (pl != null)
 	{
 	  pl.sendMessage("§cYour mailbox is full!");
 	}
         if (this.economy_on) 
 	{
           economy.depositPlayer(sender.getName(), fee);
         }
         return "§c" + receiver + "'s mailbox is full!";
       }
 	  
       sender.getInventory().clear(sender.getInventory().getHeldItemSlot());
       Player pl = server.getPlayer(receiver);
 
       if (pl != null)
       {
 	pl.sendMessage("§aYou have new package in your mailbox!");
       }
 
       if ((this.economy_on) && (!postman.equalsIgnoreCase(receiver))) 
       {
         economy.depositPlayer(postman, fee * this.postman_profit);
       }
 
       if (!checkpermissions(sender, "mailbox.nocooldown")) 
       {
         this.cooldown.add(new Record(sender.getName(), this.cooldown_limit));
       }
       log.log(Level.INFO, "MailBox: {0} has sent the package to {1}. [{2}:{3}]", new Object[]{sender.getName(), receiver, _package.getType(), package_copy.getAmount()});
 
       String InsertQuery = "INSERT INTO logs (sender, receiver, material, amount, date) VALUES ('" + 
         sender.getName() + "', '" + 
         receiver + "', '" + 
         package_copy.getType() + "', " + 
         package_copy.getAmount() + ", " + 
         System.currentTimeMillis() + ")";
 
       stmt.execute(InsertQuery);
 
       return "§aPackage was successfully received.";
     }
     catch (SQLException e)
     {
       log.warning("Problem with SQL. Check config file /plugin/MailBox/settings.txt");
     }
     return "§4Error!";
   }
 
   @Override
   public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
   {
     if ((!command.getName().equalsIgnoreCase("mailbox")) && (!command.getName().equalsIgnoreCase("mbox")) && (!command.getName().equalsIgnoreCase("msend")))
     {
       return false;
     }
     if (!(sender instanceof Player))
     {
       if ((args.length == 1) && (args[0].equals("-reload"))) 
       {
         prepareSettings();
         log.info("Settings reloaded");
         return true;
       }
       log.info("You can't use this command in console!");
       return true;
     }
 
     Player player = (Player)sender;
 
     if ((command.getName().equalsIgnoreCase("msend")) && 
       (args.length == 0)) 
     {
       player.sendMessage("For sending §c/msend <player_name>");
       return true;
     }
 
     if ((args.length == 0) || (args[0].equals("help"))) 
     {
       player.sendMessage("§bMailBox Plugin help:");
       player.sendMessage("§c/mailbox fees §f- Check the fees.");
       player.sendMessage("§c/mailbox help §f- Plugin help.");
       player.sendMessage("§c/mailbox create §f- Create a mailbox.");
       player.sendMessage("§c/mailbox remove §f- Remove a mailbox.");
       player.sendMessage("§c/mailbox check <player_name> §f- Check the delivery fee for sending the package to <player_name>.");
       player.sendMessage("§c/mailbox send <player_name> §f- Send a package to a player.");
       //TODO: remove op references
       if ((checkpermissions(player, "mailbox.postman.createother")) || (player.isOp())) 
       {
         player.sendMessage("§c/mailbox create <player_name> §f- Create a mailbox for someone else.");
       }
 
       if ((checkpermissions(player, "mailbox.postman.removeother")) || (checkpermissions(player, "mailbox.admin.removeany")) || (player.isOp())) 
       {
         player.sendMessage("§c/mailbox remove <player_name> §f- Remove <player_name>'s mailbox.");
       }
 
       if ((checkpermissions(player, "mailbox.admin.viewlog")) || (player.isOp())) 
       {
         player.sendMessage("§c/mailbox view [page] §f- View a log.");
       }
       return true;
     }
 
     if (args[0].equals("create"))
     {
       if ((!checkpermissions(player, "mailbox.user.create")) && (!checkpermissions(player, "mailbox.postman.createother")) && (this.only_op)) 
       {
         player.sendMessage("§cYou don't have a permission to create a mailbox!");
         return true;
       }
 
       String createfor = player.getName();
 
       if ((checkpermissions(player, "mailbox.postman.createother")) && (args.length == 2)) 
       {
         createfor = args[1];
         player.sendMessage("§2Please left click chest to create " + createfor + "'s mailbox.");
       } else {
         player.sendMessage("§2Please left click your chest to create your mailbox.");
       }
 
       if (this.akcia.get(player) != null) 
       {
         this.akcia.remove(player);
       }
       this.akcia.put(player, createfor);
       return true;
     }
 
     if (args[0].equals("remove"))
     {
       if ((!checkpermissions(player, "mailbox.user.remove")) && (!checkpermissions(player, "mailbox.postman.removeother")) && (!checkpermissions(player, "mailbox.admin.removeany")) && (this.only_op)) 
       {
         player.sendMessage("§cYou don't have a permission to remove a mailbox!");
         return true;
       }
 
       String owner = player.getName();
       if (args.length == 2) 
       {
         owner = args[1];
       }
       String r = removeMailBox(player, owner);
       player.sendMessage(r);
       return true;
     }
 
     if ((args[0].equals("send")) || (command.getName().equalsIgnoreCase("msend")))
     {
       if ((!checkpermissions(player, "mailbox.user.send")) && (this.only_op)) 
       {
         player.sendMessage("§cYou don't have a permission to send a package!");
         return true;
       }
 
       String playername = "";
 
       if (command.getName().equalsIgnoreCase("msend")) 
       {
         if (args.length == 0) 
 	{
           player.sendMessage("For send a package write: §c/msend <player_name>");
           return true;
         }
         playername = args[0];
       }
       else 
       {
         if (args.length == 1) 
 	{
           player.sendMessage("For send a package write: §c/mailbox send <player_name>");
           return true;
         }
         playername = args[1];
       }
 
       String r = sendPackage(player, playername);
       player.sendMessage(r);
       return true;
     }
 
     if (args[0].equals("check"))
     {
       String playername = "";
       if (args.length == 1) 
       {
         player.sendMessage("For check a fee for delivery a package, write: §c/mailbox check <player_name>");
         return true;
       }
       playername = args[1];
       String r = check(player, playername);
       player.sendMessage(r);
       return true;
     }
 
     if ((args[0].equals("view")) || (args[0].equals("viewlog")))
     {
       if (!checkpermissions(player, "mailbox.admin.viewlog")) 
       {
         player.sendMessage("§cYou don't have a permission to view a log!");
         return true;
       }
 
       int page = 0;
 
       if (args.length == 2) 
       {
         try 
 	{
           page = Integer.parseInt(args[1]);
           page--;
         } 
 	catch (Exception e) 
 	{
           player.sendMessage("§cWrong page number!");
         }
       }
 
       viewLog(player, page);
     }
 
     if (args[0].equals("fees"))
     {
       if (!this.economy_on) 
       {
         player.sendMessage("§ceconomy is off!");
       }
       else 
       {
         player.sendMessage("The fee for creating the mailbox is §b" + economy.format(this.creating_fee) + "§f.");
         player.sendMessage("Fixed fee for delivery is §b" + economy.format(this.delivery_fee) + "§f");
         player.sendMessage("+  §b" + economy.format(this.fee_per_1000m) + "§f per 1000 meters. (In the same World)");
         player.sendMessage("OR §b" + economy.format(this.foreign_fee) + "§f when you send package to another World.");
       }
     }
 
     return true;
   }
 
   public boolean isDebugging(Player player) 
   {
     if (this.debugees.containsKey(player)) 
     {
       return ((Boolean)this.debugees.get(player)).booleanValue();
     }
     return false;
   }
 
   public void setDebugging(Player player, boolean value)
   {
     this.debugees.put(player, Boolean.valueOf(value));
   }
 }
