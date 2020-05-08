 package com.me.tft_02.assassin.config;
 
 import java.util.List;
 
 public class Config extends AutoUpdateConfigLoader {
    private static Config instance;
 
     private Config() {
         super("config.yml");
     }
 
     public static Config getInstance() {
         if (instance == null) {
             instance = new Config();
         }
 
         return instance;
     }
 
     @Override
     protected void loadKeys() {}
 
     /* @formatter:off */
     /* GENERAL SETTINGS */
     public String getLocale() { return config.getString("General.Locale", "en_us"); }
     public int getSaveInterval() { return config.getInt("General.Save_Interval", 15); }
     public boolean getStatsTrackingEnabled() { return config.getBoolean("General.Stats_Tracking", true); }
     public boolean getUpdateCheckEnabled() { return config.getBoolean("General.Update_Check", true); }
     public boolean getPreferBeta() { return config.getBoolean("General.Prefer_Beta", false); }
     public boolean getVerboseLoggingEnabled() { return config.getBoolean("General.Verbose_Logging", false); }
     public boolean getConfigOverwriteEnabled() { return config.getBoolean("General.Config_Update_Overwrite", true); }
 
     /* ASSASSIN SETTINGS */
     public boolean getPreventPVP() { return config.getBoolean("Assassin.Prevent_Neutral_PVP", true); }
     public boolean getOverridePVP() { return config.getBoolean("Assassin.Override_PVP", true); }
     public int getActiveLength() { return config.getInt("Assassin.Assassin_Mode_Duration", 3600); }
     public List<String> getBlockedCommands() { return config.getStringList("Assassin.Blocked_Commands"); }
 
     public int getWarningTimeDeactivate() { return config.getInt("Assassin.Warn_Time_Almost_Up", 10); }
     public boolean getTeleportOnDeactivate() { return config.getBoolean("Assassin.Teleport_On_Deactivate", true); }
     public boolean getReturnMask() { return config.getBoolean("Assassin.Return_Mask", false); }
 
     public int getCooldownLength() { return config.getInt("Assassin.Cooldown_Length", 600); }
     public double getActivationCost() { return config.getDouble("Assassin.Activation_Cost", 0); }
 
     public boolean getWarnOnActivate() { return config.getBoolean("Assassin.Warn_Others_On_Activation", true); }
     public boolean getWarnWhenNear() { return config.getBoolean("Assassin.Warn_Others_When_Near", true); }
     public int getMessageDistance() { return config.getInt("Assassin.Message_Distance", 250); }
 
     public boolean getParticleEffectsEnabled() { return config.getBoolean("Assassin.Particle_Effects_Enabled", true); }
     public boolean getPotionEffectsEnabled() { return config.getBoolean("Assassin.Potion_Effects_Enabled", true); }
 
     public int getMaxAllowed() { return config.getInt("Assassin.Max_Allowed", 5); }
 
     public int getBountyAmount() { return config.getInt("Assassin.Bounty_Increase_Amount", 10); }
     public String getCurrencyIcon() { return config.getString("Assassin.Bounty_Currency", "$"); }
     /* @formatter:on */
 }
