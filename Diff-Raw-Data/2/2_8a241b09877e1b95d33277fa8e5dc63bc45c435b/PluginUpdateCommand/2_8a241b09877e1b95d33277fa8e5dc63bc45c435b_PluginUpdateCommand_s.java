 package com.cloudbees.sdk.commands;
 
 import com.cloudbees.sdk.CommandServiceImpl;
 import com.cloudbees.sdk.GAV;
 import com.cloudbees.sdk.Plugin;
 import com.cloudbees.sdk.cli.BeesCommand;
 import com.cloudbees.sdk.cli.CLICommand;
 
 import java.io.IOException;
 
 /**
  * @author Fabian Donze
  */
 @CLICommand("plugin:update")
@BeesCommand(group="SDK", description = "CLI plugin upadte")
 public class PluginUpdateCommand extends PluginVersionCommand {
 
     public PluginUpdateCommand() {
         setArgumentExpected(1);
     }
 
     @Override
     protected boolean preParseCommandLine() {
         if (super.preParseCommandLine()) {
             addOption("v", "verbose", false, "verbose output");
             return true;
         }
         return false;
     }
     @Override
     protected String getUsageMessage() {
         return "PLUGIN_NAME";
     }
 
     private String getPluginName() {
         String name = getParameters().get(0);
         if (name.indexOf(':') > -1) {
             String[] parts = name.split(":");
             name = parts[1];
         }
         return name;
     }
 
     @Override
     protected boolean execute() throws Exception {
         CommandServiceImpl service = (CommandServiceImpl) commandService;
         String name = getPluginName();
         Plugin plugin = service.getPlugin(name);
         if (plugin != null) {
             System.out.println();
             System.out.println("Plugin: " + plugin.getArtifact());
             GAV gav = new GAV(plugin.getArtifact());
             return checkVersion(gav);
         } else {
             throw new IOException("Plugin not found: " + name);
         }
     }
 
 }
 
