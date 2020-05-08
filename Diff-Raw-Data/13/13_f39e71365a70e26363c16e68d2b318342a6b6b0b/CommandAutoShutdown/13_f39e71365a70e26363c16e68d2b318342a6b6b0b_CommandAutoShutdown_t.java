 package monnef.autoshutdown;
 
 import com.google.common.base.Joiner;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.CommandBase;
 import net.minecraft.src.Entity;
 import net.minecraft.src.ICommandSender;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class CommandAutoShutdown extends CommandBase {
     @Override
     public String getCommandName() {
         return "autoshutdown";
     }
 
     @Override
     public String getCommandUsage(ICommandSender var1) {
        return "/" + getCommandName() + " [postpone=pp|on|off|status=s]";
     }
 
     @Override
     public List getCommandAliases() {
         List res = new ArrayList();
         res.add("asd");
         return res;
     }
 
     @Override
     public void processCommand(ICommandSender var1, String[] var2) {
         boolean printStatus = false;
         boolean ok = false;
         boolean skipLog = false;
 
         if (var2.length >= 1) {
             String t = var2[0].toLowerCase();
             if (t.equals("postpone") || t.equals("pp")) {
                 mod_autoshutdown.minutesServerIsDead = 0;
                 ok = printStatus = true;
             } else if (t.equals("on")) {
                 mod_autoshutdown.active = true;
                 ok = printStatus = true;
             } else if (t.equals("off")) {
                 mod_autoshutdown.active = false;
                 ok = printStatus = true;
             } else if (t.equals("status") || t.equals("s")) {
                 // this branch is just to skip error message
                 ok = printStatus = skipLog = true;
             }
         }
 
         if (printStatus) var1.sendChatToPlayer(mod_autoshutdown.getStatus());
 
         if (!ok) {
             var1.sendChatToPlayer("Unknown parameters. See /help for more info.");
         }
 
         if (ok && !skipLog && var1 instanceof Entity) {
             MinecraftServer s = MinecraftServer.getServer();
             Entity p = (Entity) var1;
             s.logInfo("[" + mod_autoshutdown.Name + "] " + "\"" + p.getEntityName() + "\" used command: \"" + Joiner.on(" ").join(var2) + "\"");
         }
     }
 
 }
