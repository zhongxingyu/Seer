 package net.daboross.bukkitdev.wildwest;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 class Task_Interest
   implements Runnable
 {
   public WildWestBukkit plugin;
 
   public Task_Interest(WildWestBukkit plugin)
   {
     this.plugin = plugin;
   }
 
   public void run()
   {
     MoneyAPI money;
     if (this.plugin.getConfig().getBoolean("Interest.Enabled")) {
       money = MoneyAPI.getInstance();
       FileConfiguration conf = money.getMoneyConfig();
       if (!conf.getKeys(false).isEmpty())
         for (String keys : conf.getKeys(false)) {
          double rate = this.plugin.getConfig().getDouble("Interest.Rate") / 100.0D + 1.0D;
           int time = this.plugin.getConfig().getInt("Interest.Time");
           double cmoney = money.getMoney(keys);
           double p = cmoney * rate;
           double a = Math.pow(p, time);
           money.setMoney(keys, a);
         }
     }
   }
 }
