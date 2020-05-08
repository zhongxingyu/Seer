 // ------------------------------------------------------------------------ //
 //                              File Reference                              //
 // Author: Patrick le Duc                                                   //
 // Filename: CommandEdit.java                                                   //
 // License: GNU GPLv2
 // Description:                                                             //
 // Base Class of the MyWorld Plugin                                         //
 //                                                                          //
 // ------------------------------------------------------------------------ //
 
 package nl.pleduc.mc.CommandEdit;
 
import org.bukkit.event.EventPriority;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CommandEdit extends JavaPlugin
 {
     private boolean m_Debugging = true;
     private String pluginName = "CommandEdit";
     private String pluginVersion = "v1.0";
     
    private CommandEditFileLoader m_Filesystem;
    
     @Override
     public void onEnable()
     {
         // Catch all commands with the command edit executor for clean code purposes
         getCommand( "commandedit" ).setExecutor( new CommandEditCommandExecutor( this ) );
        
        // Register the Listeners
        this.getServer().getPluginManager().registerEvents( new CommandEditListener( this ), this );
        
        m_Filesystem = new CommandEditFileLoader( this );
        m_Filesystem.Reload();
     }
     
     @Override
     public void onDisable()
     {
         
     }
     
     public String getPluginName()
     {
         return pluginName;
     }
     
     public String getPluginVersion()
     {
         return pluginVersion;
     }
     
}
