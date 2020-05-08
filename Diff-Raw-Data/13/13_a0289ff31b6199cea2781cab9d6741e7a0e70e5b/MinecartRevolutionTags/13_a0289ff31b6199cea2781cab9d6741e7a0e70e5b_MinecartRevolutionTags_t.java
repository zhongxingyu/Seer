 
 package com.quartercode.minecartrevolutiontags;
 
 import com.quartercode.minecartrevolution.plugin.BukkitMinecartRevolutionPlugin;
 import com.quartercode.minecartrevolution.plugin.PluginInfo;
 import com.quartercode.minecartrevolutiontags.util.TagManager;
 import com.quartercode.qcutil.io.File;
 import com.quartercode.qcutil.version.Version;
 
 public class MinecartRevolutionTags extends BukkitMinecartRevolutionPlugin {
 
     private File       tagFile;
     private TagManager tagManager;
 
     public MinecartRevolutionTags() {
 
         super();
     }
 
     @Override
     public PluginInfo getInfo() {
 
         return new PluginInfo("MinecartRevolutionTags", new Version("1.0"));
     }
 
     public File getTagFile() {
 
         return tagFile;
     }
 
     public TagManager getTagManager() {
 
         return tagManager;
     }
 
     @Override
     public void enable() {
 
         new MinecartListener(this);
 
         tagFile = new File(getPluginFolder(), "tags.properties");
         tagManager = new TagManager(this);
 
         addCommandHandler(new TagUpdateCommand());
 
         addExpressionCommand(new TagCommand(this));
         addControlSign(new TagSign());
 
        addMinecartTerm(new TagMinecartTerm(this));
     }
 
 }
