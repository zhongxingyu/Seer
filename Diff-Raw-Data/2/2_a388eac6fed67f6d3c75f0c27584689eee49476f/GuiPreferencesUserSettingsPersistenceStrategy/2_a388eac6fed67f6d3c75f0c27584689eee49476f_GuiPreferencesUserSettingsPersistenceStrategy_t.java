 package info.mikaelsvensson.ftpbackup.gui.conf;
 
 import info.mikaelsvensson.ftpbackup.conf.PreferencesUserSettingsPersistenceStrategy;
 import info.mikaelsvensson.ftpbackup.conf.UserSetting;
 import info.mikaelsvensson.ftpbackup.conf.UserSettingsPersistenceStrategyException;
 
 import java.awt.*;
 
 public class GuiPreferencesUserSettingsPersistenceStrategy extends PreferencesUserSettingsPersistenceStrategy {
 // ------------------------------ FIELDS ------------------------------
 
     private static final GuiPreferencesUserSettingsPersistenceStrategy instance = new GuiPreferencesUserSettingsPersistenceStrategy();
 
 // -------------------------- STATIC METHODS --------------------------
 
     public static GuiPreferencesUserSettingsPersistenceStrategy getInstance() {
         return instance;
     }
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     private GuiPreferencesUserSettingsPersistenceStrategy() {
         super();
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     @Override
     protected void loadValue(final UserSetting<?> setting) throws UserSettingsPersistenceStrategyException {
         if (setting instanceof WindowPlacementSetting) {
             WindowPlacementSetting windowPlacementSetting = (WindowPlacementSetting) setting;
             WindowPlacement placement = new WindowPlacement(
                     getInteger(setting.getId() + ".x", -1),
                     getInteger(setting.getId() + ".y", -1),
                     getInteger(setting.getId() + ".width", 0),
                     getInteger(setting.getId() + ".height", 0),
                     getInteger(setting.getId() + ".extendedState", Frame.NORMAL));
             windowPlacementSetting.setValue(placement);
         } else {
             super.loadValue(setting);
         }
     }
 
     @Override
     protected void storeValue(final UserSetting<?> setting) throws UserSettingsPersistenceStrategyException {
         if (setting instanceof WindowPlacementSetting) {
             WindowPlacementSetting windowPlacementSetting = (WindowPlacementSetting) setting;
             putInteger(setting.getId() + ".x", windowPlacementSetting.getValue().getX());
             putInteger(setting.getId() + ".y", windowPlacementSetting.getValue().getY());
             putInteger(setting.getId() + ".width", windowPlacementSetting.getValue().getWidth(0));
             putInteger(setting.getId() + ".height", windowPlacementSetting.getValue().getHeight(0));
             putInteger(setting.getId() + ".extendedState", windowPlacementSetting.getValue().getExtendedState());
         } else {
            super.storeValue(setting);
         }
     }
 }
