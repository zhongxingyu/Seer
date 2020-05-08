 
 package com.quartercode.basiccommands;
 
 import com.quartercode.basiccommands.command.EjectCommand;
 import com.quartercode.basiccommands.command.GetVersionsCommand;
 import com.quartercode.basiccommands.command.HelpCommand;
 import com.quartercode.basiccommands.command.InfoCommand;
 import com.quartercode.basiccommands.command.PatchCommand;
 import com.quartercode.basiccommands.command.RemovecartsCommand;
 import com.quartercode.basiccommands.command.StopcartsCommand;
 import com.quartercode.basiccommands.command.UpdateCommand;
 import com.quartercode.basiccommands.command.VersioncheckCommand;
 import com.quartercode.basiccommands.listener.PlayerListener;
 import com.quartercode.basiccommands.util.MinecartRevolutionRecodePatcher;
 import com.quartercode.basiccommands.util.Patcher;
 import com.quartercode.minecartrevolution.plugin.JavaMinecartRevolutionPlugin;
 import com.quartercode.minecartrevolution.plugin.PluginInfo;
 import com.quartercode.qcutil.version.Version;
 
 public class BasicCommandsPlugin extends JavaMinecartRevolutionPlugin {
 
     public BasicCommandsPlugin() {
 
         super();
     }
 
     @Override
     public PluginInfo getInfo() {
 
        return new PluginInfo("BasicCommands", new Version("Alpha 1.0"));
     }
 
     @Override
     public void enable() {
 
         new PlayerListener(getMinecartRevolution());
 
         addCommandHandler(new HelpCommand());
         addCommandHandler(new InfoCommand());
         addCommandHandler(new GetVersionsCommand());
         addCommandHandler(new VersioncheckCommand());
         addCommandHandler(new UpdateCommand());
         addCommandHandler(new RemovecartsCommand());
         addCommandHandler(new StopcartsCommand());
         addCommandHandler(new EjectCommand());
         addCommandHandler(new PatchCommand());
 
         Patcher.addPatcher(new MinecartRevolutionRecodePatcher(getMinecartRevolution()));
     }
 
 }
