 package in.nikitapek.chestempty;
 
import com.amshulman.mbapi.MbapiPlugin;
import in.nikitapek.chestempty.commands.CommandChestEmpty;
 import in.nikitapek.chestempty.events.ChestEmptyListener;
 import in.nikitapek.chestempty.util.ChestEmptyConfigurationContext;
 
 public final class ChestEmptyPlugin extends MbapiPlugin {
     @Override
     public void onEnable() {
        final ChestEmptyConfigurationContext configurationContext = new ChestEmptyConfigurationContext(this);
 
        registerCommandExecutor(new CommandChestEmpty(configurationContext));
        registerEventHandler(new ChestEmptyListener(configurationContext));
 
        super.onEnable();
     }
 }
