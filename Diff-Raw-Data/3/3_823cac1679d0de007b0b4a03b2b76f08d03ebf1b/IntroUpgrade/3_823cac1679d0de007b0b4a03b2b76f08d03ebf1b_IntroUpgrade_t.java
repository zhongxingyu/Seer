 package de.autoit4you.bankaccount.internal.upgrade;
 
 import de.autoit4you.bankaccount.BankAccount;
 import de.autoit4you.bankaccount.internal.Upgrade;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.Statement;
 
 public class IntroUpgrade extends Upgrade {
     private BankAccount plugin;
 
     public IntroUpgrade(BankAccount plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public void performUpgrade() throws Throwable {
         upgradeDb();
         plugin.getConfig().set("version", 1);
 
         Upgrade upgrade = Upgrade.getUpgrade(1, plugin);
         if(upgrade != null) {
             upgrade.performUpgrade();
         }
     }
 
     private void upgradeDb() throws Throwable {
         if(plugin.getConfig().getString("database.type").equalsIgnoreCase("mysql")) {
            Class.forName("com.mysql.jdbc.Driver");
             Connection conn = DriverManager.getConnection("jdbc:mysql://" +
                     plugin.getConfig().getString("database.server") +
                     "/" + plugin.getConfig().getString("database.database"),
                     plugin.getConfig().getString("database.username"),
                     plugin.getConfig().getString("database.password"));
             if(conn == null)
                 throw new Exception("Can't access MySQL database!");
 
             Statement stmt = conn.createStatement();
             stmt.executeUpdate("ALTER TABLE `accounts`  ADD `password` VARCHAR(200) NOT NULL");
             conn.close();
         } else if (plugin.getConfig().getString("database.type").equalsIgnoreCase("sqlite")) {
            Class.forName("org.sqlite.JDBC");
             Connection conn = DriverManager.getConnection("jdbc:sqlite:" + plugin.getConfig().getString("database.sqlitefile"));
             if(conn == null)
                 throw new Exception("Can't access SQlite database!");
 
             Statement stmt = conn.createStatement();
             stmt.executeUpdate("ALTER TABLE `accounts`  ADD `password` VARCHAR(200) NOT NULL");
             conn.close();
         } else {
             throw new UnsupportedOperationException("This database type is not supported in this version!");
         }
     }
 }
