 package net.minecraft.src;
 
 import org.jruby.embed.ScriptingContainer;
 import org.jruby.embed.LocalVariableBehavior;
 
 public class CommandRuby extends CommandBase
 {
     private ScriptingContainer container;
 
     public CommandRuby() {
       this.container = new ScriptingContainer(LocalVariableBehavior.PERSISTENT);
       this.container.runScriptlet("require 'endertromb/lib/endertromb'; include Endertromb");
     }
 
     public String getCommandName()
     {
         return "ruby";
     }
 
     /**
      * Return the required permission level for this command.
      */
     public int getRequiredPermissionLevel()
     {
         return 0;
     }
 
     public void processCommand(ICommandSender par1ICommandSender, String[] par2ArrayOfStr)
     {
         EntityPlayerMP player = getCommandSenderAsPlayer(par1ICommandSender);
         World world = player.worldObj;
 
         this.container.put("$player", player);
         this.container.put("$world", world);
 
         String script = CommandRuby.joinArgs(par2ArrayOfStr);
        String output = this.container.runScriptlet("(" + script + ").inspect").toString();
         par1ICommandSender.sendChatToPlayer("> " + script + "\n= " + output);
     }
     
     static private String joinArgs(String[] args) {
         if (args.length == 0) return "";
         StringBuilder sb = new StringBuilder();
         int i;
         for (i = 0; i < args.length - 1; i++) {
           sb.append(args[i] + " ");
         }
         return sb.toString() + args[i];
     }
 }
