 package in.nikitapek.chestempty;
 
 import in.nikitapek.chestempty.events.ChestEmptyListener;
 import in.nikitapek.chestempty.util.ChestEmptyConfigurationContext;
 
import org.bukkit.Bukkit;

import com.amshulman.mbapi.MbapiPlugin;

 public final class ChestEmptyPlugin extends MbapiPlugin {
     @Override
     public void onEnable() {
        registerEventHandler(new ChestEmptyListener(new ChestEmptyConfigurationContext(this)));
 
        super.onEnable();
    }
 
    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
     }
 }
