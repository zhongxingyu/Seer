 package net.acomputerdog.BlazeLoader.proxy;
 
 import net.acomputerdog.BlazeLoader.api.base.ApiBase;
 import net.acomputerdog.BlazeLoader.fix.FixManager;
 import net.acomputerdog.BlazeLoader.main.BlazeLoader;
 import net.acomputerdog.BlazeLoader.mod.ModList;
 import net.minecraft.src.*;
 
 import java.io.File;
 import java.lang.reflect.Field;
 import java.net.Proxy;
 
 /**
  * Acts as a proxy to intercept method calls to Minecraft.class.  Loaded in place of normal Minecraft.class
  */
 public class MinecraftProxy extends Minecraft {
     protected IntegratedServerProxy theServer = null;
 
     public MinecraftProxy(Session par1Session, int par2, int par3, boolean par4, boolean par5, File par6File, File par7File, File par8File, Proxy par9Proxy, String par10Str) {
         super(par1Session, par2, par3, par4, par5, par6File, par7File, par8File, par9Proxy, par10Str);
         ApiBase.theMinecraft = this;
         injectProfilerProxy();
         ApiBase.globalLogger = getLogAgent();
         BlazeLoader.init(new File(System.getProperty("user.dir")));
     }
 
     private void injectProfilerProxy(){
         try{
             Field[] fields = getClass().getDeclaredFields();
             for(Field f : fields){
                 if(Profiler.class.isAssignableFrom(f.getType())){
                     f.setAccessible(true);
                     f.set(this, new ProfilerProxy());
                 }
             }
         }catch(ReflectiveOperationException e){
             BlazeLoader.log("[ERROR] Unable to inject ProfilerProxy!  Some components may not function!");
             e.printStackTrace();
         }
     }
 
     @Override
     public void setDimensionAndSpawnPlayer(int par1) {
         super.setDimensionAndSpawnPlayer(par1);
         ApiBase.localPlayer = this.thePlayer;
     }
 
     @Override
     public void loadWorld(WorldClient par1WorldClient, String par2Str) {
         super.loadWorld(par1WorldClient, par2Str);
        theServer = (IntegratedServerProxy)getIntegratedServer();
         ApiBase.localPlayer = this.thePlayer;
     }
 
     @Override
     public void launchIntegratedServer(String par1Str, String par2Str, WorldSettings par3WorldSettings) {
         super.launchIntegratedServer(par1Str, par2Str, par3WorldSettings);
         IntegratedServerProxy server = new IntegratedServerProxy(this, getIntegratedServer());
         theServer = server;
         setIntegratedServer(server);
     }
 
     protected void setIntegratedServer(IntegratedServer server){
         for(Field f : Minecraft.class.getDeclaredFields()){
             if(IntegratedServer.class.isAssignableFrom(f.getType())){
                 f.setAccessible(true);
                 try {
                     f.set(this, server);
                 } catch (IllegalAccessException e) {
                     throw new RuntimeException("Could not inject IntegratedServerProxy!", e);
                 }
             }
         }
     }
 
     public IntegratedServerProxy getServerProxy(){
         return theServer;
     }
 
     @Override
     public void run() {
         //Event here?
         super.run();
     }
 
     @Override
     public void refreshResources() {
         //Event here?
         super.refreshResources();
     }
 
     @Override
     public void shutdown() {
         FixManager.onStop();
         ModList.stop();
         BlazeLoader.saveSettings();
         super.shutdown();
     }
 
     @Override
     public void runTick() {
         BlazeLoader.isInTick = true;
         super.runTick();
         ModList.tick(false);
         BlazeLoader.isInTick = false;
     }
 
     @Override
     public void displayGuiScreen(GuiScreen par1GuiScreen) {
         if(par1GuiScreen != null){
             GuiScreen gui = ModList.onGui(par1GuiScreen);
             if(gui != null)super.displayGuiScreen(par1GuiScreen);
         }else{
             super.displayGuiScreen(null);
         }
     }
 }
