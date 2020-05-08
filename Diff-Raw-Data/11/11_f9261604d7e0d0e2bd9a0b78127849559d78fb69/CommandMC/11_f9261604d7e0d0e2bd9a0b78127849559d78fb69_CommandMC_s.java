 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.command;
 
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.server.FMLServerHandler;
 import monnef.core.Config;
 import monnef.core.MonnefCorePlugin;
 import monnef.core.Reference;
 import monnef.core.client.CustomCloaksHandler;
 import monnef.core.common.GuiHandler;
 import monnef.core.common.ScheduledTicker;
 import monnef.core.mod.MonnefCoreNormalMod;
 import monnef.core.utils.DyeHelper;
 import monnef.core.utils.GameObjectsDumper;
 import net.minecraft.command.CommandBase;
 import net.minecraft.command.ICommandSender;
 import net.minecraft.entity.player.EntityPlayer;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import static monnef.core.client.CustomCloaksHandler.DEBUG_FORCE_SPECIAL_CLOAK;
 import static monnef.core.utils.PlayerHelper.addMessage;
 
 public class CommandMC extends CommandBase {
     @Override
     public String getCommandName() {
         return "mc";
     }
 
     @Override
     public String getCommandUsage(ICommandSender icommandsender) {
         return "commands.mc.usage";
     }
 
     @Override
     public void processCommand(ICommandSender commandsender, String[] parameters) {
         if (parameters.length <= 0) {
            addMessage(commandsender, "\u00A7dmonnef core§r created by §e" + Reference.MONNEF + "§r.");
         } else if (parameters.length == 1 && parameters[0].equals("exporter")) {
             if (Config.isExporterEnabled()) {
                 if (commandsender instanceof EntityPlayer) {
                     EntityPlayer player = (EntityPlayer) commandsender;
                     player.openGui(MonnefCoreNormalMod.instance, GuiHandler.GuiId.EXPORTER.ordinal(), player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
                 } else {
                     addMessage(commandsender, "You're not an entity, aborting.");
                 }
             } else {
                 addMessage(commandsender, "Exporter is disabled in a config file.");
             }
         } else if (parameters.length == 1 && parameters[0].equals("dumpGameObjects")) {
             GameObjectsDumper.dump("gameObjects.csv");
         } else if (parameters.length == 2 && parameters[0].equals("debug")) {
             if (parameters[1].equals("dumpColors")) {
                 addMessage(commandsender, DyeHelper.compileColorList());
             } else if (parameters[1].equals("scheduledTicker")) {
                 constructScheduledTickerAndRun();
             } else if (parameters[1].equals("forceCloak")) {
                 if (MonnefCorePlugin.debugEnv) DEBUG_FORCE_SPECIAL_CLOAK = !DEBUG_FORCE_SPECIAL_CLOAK;
                 addMessage(commandsender, "Cloak forcing: " + DEBUG_FORCE_SPECIAL_CLOAK);
             } else {
                 addMessage(commandsender, "Unknown debug sub-command.");
             }
         } else {
             addMessage(commandsender, "Unknown sub-command.");
         }
     }
 
     private void constructScheduledTickerAndRun() {
         new ScheduledTicker() {
             private DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SS");
 
             private int remaining = 6;
 
             private String time() {
                 return formatter.format(new Date());
             }
 
             @Override
             public void onClientTickStart() {
                 addMessage(FMLClientHandler.instance().getClientPlayerEntity(), "C: " + time());
                 onTick();
             }
 
             @Override
             public void onServerTickStart() {
                 String s = "S: " + time();
                 System.out.println(s);
                 //FMLServerHandler.instance().getServer().logInfo(s);
                 onTick();
             }
 
             private void onTick() {
                 remaining--;
                 if (remaining < 0) unregister();
             }
 
             @Override
             public int nextTickSpacing(Side side) {
                 return 20;
             }
         }.register();
     }
 }
