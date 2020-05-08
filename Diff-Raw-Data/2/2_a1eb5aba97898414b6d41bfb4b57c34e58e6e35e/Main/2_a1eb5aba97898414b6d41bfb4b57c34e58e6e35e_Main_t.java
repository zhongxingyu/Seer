 package edgruberman.bukkit.simplelocks;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.event.HandlerList;
 
 import edgruberman.bukkit.simplelocks.commands.Break;
 import edgruberman.bukkit.simplelocks.commands.Describe;
 import edgruberman.bukkit.simplelocks.commands.Grant;
 import edgruberman.bukkit.simplelocks.commands.Reload;
 import edgruberman.bukkit.simplelocks.commands.Revoke;
 import edgruberman.bukkit.simplelocks.messaging.ConfigurationCourier;
 import edgruberman.bukkit.simplelocks.util.CustomPlugin;
 
 public class Main extends CustomPlugin {
 
     public static ConfigurationCourier courier;
 
     @Override
    public void onLoad() { this.putConfigMinimum("3.3.2"); }
 
     @Override
     public void onEnable() {
         this.reloadConfig();
         Main.courier = ConfigurationCourier.Factory.create(this).setPath("messages").build();
 
         final String title = this.getConfig().getString("title");
         this.getLogger().config("Lock title: " + title);
         if (title.length() < 1 || title.length() > Locksmith.MAXIMUM_SIGN_LINE_LENGTH)
             throw new IllegalArgumentException("Lock title must be between 1 and " + Locksmith.MAXIMUM_SIGN_LINE_LENGTH + " characters");
 
         final Locksmith locksmith = new Locksmith(this, title);
         final ConfigurationSection substitutions = this.getConfig().getConfigurationSection("substitutions");
         if (substitutions != null)
             for (final String name : substitutions.getKeys(false))
                 locksmith.substitutions.put(name, substitutions.getString(name));
 
         if (this.getConfig().getBoolean("explosion-protection")) new ExplosiveOrdnanceDisposal(this, locksmith);
 
         this.getCommand("simplelocks:describe").setExecutor(new Describe(locksmith));
         this.getCommand("simplelocks:grant").setExecutor(new Grant(locksmith));
         this.getCommand("simplelocks:revoke").setExecutor(new Revoke(locksmith));
         this.getCommand("simplelocks:break").setExecutor(new Break(locksmith));
         this.getCommand("simplelocks:reload").setExecutor(new Reload(this));
     }
 
     @Override
     public void onDisable() {
         HandlerList.unregisterAll(this);
         Main.courier = null;
     }
 
 }
