 package ccm.deathTimer.commands;
 
 import java.util.List;
 
 import net.minecraft.command.CommandBase;
 import net.minecraft.command.ICommandSender;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.util.EnumChatFormatting;
 
 import ccm.deathTimer.api.HardTimerAPI;
 import ccm.deathTimer.server.ServerTimer;
 import ccm.deathTimer.timerTypes.DeathTimer;
 import ccm.deathTimer.timerTypes.ITimerBase;
 import ccm.deathTimer.timerTypes.PointTimer;
 import ccm.nucleum_omnium.helper.JavaHelper;
 
 /**
  * The main (and only) command. You need OP to change server wide settings, Cient only changes are send back via packets and are saved in the client config. TODO: Make command.
  * 
  * @author Dries007
  */
 public class Timer extends CommandBase
 {
 
     public final static String ListSyntax  = "list";
 
     public final static String AddSyntax   = "add <name> <time> [X] [Y] [Z] [dim]";
 
     public final static String StopSyntax  = "stop <name>";
 
     public final static String SoundSyntax = "sound <name> [volume] [pitch]";
 
     public final static String PointSyntax = "point <name> [X] [Y] [Z] [dim]";
 
     public final static String OpArgs      = "[" + ListSyntax + "|" + AddSyntax + "|" + StopSyntax + "|" + SoundSyntax + "|" + PointSyntax + "]";
 
     public final static String NonOpArgs   = "[" + ListSyntax + "|" + StopSyntax + "|" + SoundSyntax + "]";
 
     @Override
     public String getCommandName()
     {
         return "timer";
     }
 
     /**
      * Main command thing
      */
     @Override
     public void processCommand(final ICommandSender sender, final String[] args)
     {
         final boolean op = sender instanceof EntityPlayer ? MinecraftServer.getServer().isDedicatedServer() ? MinecraftServer.getServer().getConfigurationManager().getOps().contains(sender.getCommandSenderName()) : true : true;
 
         if (args.length == 1 && args[0].equalsIgnoreCase("help"))
         {
             sender.sendChatToPlayer(EnumChatFormatting.GREEN + "Useage: /" + this.getCommandName() + " " + (op ? OpArgs : NonOpArgs));
             if (!op)
             {
                 sender.sendChatToPlayer(EnumChatFormatting.GREEN + "Note that you can only change your own timers!");
             }
             return;
         }
         
         if ((args.length == 0) || args[0].equalsIgnoreCase("timers") || args[0].equalsIgnoreCase("list"))
         {
             sender.sendChatToPlayer("--- Timers ---");
             for (final ITimerBase timer : ServerTimer.getInstance().timerList.values())
             {
                 final StringBuilder sb = new StringBuilder();
                 for (final String s : timer.getTimerString(sender))
                 {
                     sb.append(EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString() + s);
                 }
                 sender.sendChatToPlayer(sb.toString());
             }
             sender.sendChatToPlayer("--------------");
         }
         else if (op && args[0].equalsIgnoreCase("add"))
         {
             this.processCommandAdd(sender, args, op);
         }
         else if (op && args[0].equalsIgnoreCase("point"))
         {
             this.processCommandPoint(sender, args, op);
         }
         else if (args[0].equalsIgnoreCase("stop"))
         {
             this.processCommandStop(sender, args, op);
         }
         else if (args[0].equalsIgnoreCase("sound"))
         {
             this.processCommandSound(sender, args, op);
         }
         else
         {
             sender.sendChatToPlayer(EnumChatFormatting.RED + "Syntax error. Use: /" + this.getCommandName() + " " + (op ? OpArgs : NonOpArgs));
             if (!op)
             {
                 sender.sendChatToPlayer(EnumChatFormatting.RED + "Note that you can only change your own timers!");
             }
             return;
         }
     }
 
     /**
      * Stop part
      */
     public void processCommandStop(final ICommandSender sender, final String[] args, final boolean op)
     {
         if (args.length < 1)
         {
             if (ServerTimer.getInstance().timerList.containsKey(DeathTimer.PACKETID + sender.getCommandSenderName()))
                 HardTimerAPI.stopTimer(DeathTimer.PACKETID + sender.getCommandSenderName());
             else
                 sender.sendChatToPlayer(EnumChatFormatting.RED + "Syntax error. Use: /" + this.getCommandName() + " " + StopSyntax);
         }
         else if (ServerTimer.getInstance().timerList.containsKey(args[1]))
         {
             final ITimerBase timer = ServerTimer.getInstance().timerList.get(args[1]);
 
             if ((timer.isRelevantFor(sender) && timer.isPersonal()) || op)
             {
                 HardTimerAPI.stopTimer(args[1]);
                 sender.sendChatToPlayer(EnumChatFormatting.GREEN + "Done.");
             }
             else
             {
                 sender.sendChatToPlayer(EnumChatFormatting.RED + "You can't edit this timer.");
             }
         }
         else
         {
             sender.sendChatToPlayer(EnumChatFormatting.RED + "This timer doesn't exists.");
         }
     }
 
     /**
      * Sound part
      */
     public void processCommandSound(final ICommandSender sender, final String[] args, final boolean op)
     {
         if (args.length < 1)
         {
             sender.sendChatToPlayer(EnumChatFormatting.RED + "Syntax error. Use: /" + this.getCommandName() + " " + SoundSyntax);
         }
         else if (ServerTimer.getInstance().timerList.containsKey(args[1]))
         {
             final ITimerBase timer = ServerTimer.getInstance().timerList.get(args[1]);
 
             if (args.length < 5)
             {
                 if ((timer.isRelevantFor(sender) && timer.isPersonal()) || op)
                 {
                     if (JavaHelper.isNumeric(args[3]) || JavaHelper.isNumeric(args[3]))
                     {
                         HardTimerAPI.setSound(args[1], args[2], Float.valueOf(args[3]), Float.valueOf(args[4]));
                         sender.sendChatToPlayer(EnumChatFormatting.GREEN + "Done.");
                     }
                     else
                     {
                         if (!JavaHelper.isNumeric(args[3]))
                         {
                             sender.sendChatToPlayer(EnumChatFormatting.RED + " " + args[3] + " is not a number!");
                         }
                         else if (!JavaHelper.isNumeric(args[4]))
                         {
                             sender.sendChatToPlayer(EnumChatFormatting.RED + " " + args[4] + " is not a number!");
                         }
                         else
                         {
                             sender.sendChatToPlayer("?????????? < Please report to modmaker, idk how you got here...");
                         }
                     }
                 }
                 else
                 {
                     sender.sendChatToPlayer(EnumChatFormatting.RED + "You can't edit this timer.");
                 }
             }
             else
             {
                 sender.sendChatToPlayer("Current sound: " + timer.getSoundName() + " Volume:" + timer.getSoundVolume() + " Pitch: " + timer.getSoundPitch());
             }
         }
         else
         {
             sender.sendChatToPlayer(EnumChatFormatting.RED + "This timer doesn't exists.");
         }
     }
 
     /**
      * Point part
      */
     public void processCommandPoint(final ICommandSender sender, final String[] args, final boolean op)
     {
         if (args.length < 1)
         {
             sender.sendChatToPlayer(EnumChatFormatting.RED + "Syntax error. Use: /" + this.getCommandName() + " " + PointSyntax);
         }
         else if (ServerTimer.getInstance().timerList.containsKey(args[1]) && (ServerTimer.getInstance().timerList.get(args[1]) instanceof PointTimer))
         {
             final PointTimer timer = (PointTimer) ServerTimer.getInstance().timerList.get(args[1]);
 
             if (args.length < 5)
             {
                 if ((timer.isRelevantFor(sender) && timer.isPersonal()) || op)
                 {
                     if (JavaHelper.isNumeric(args[2]) || JavaHelper.isNumeric(args[3]) || JavaHelper.isNumeric(args[4]))
                     {
                         timer.X = Integer.parseInt(args[2]);
                         timer.Y = Integer.parseInt(args[3]);
                         timer.Z = Integer.parseInt(args[4]);
                         if ((args.length == 6) && JavaHelper.isNumeric(args[5]))
                         {
                             timer.dim = Integer.parseInt(args[5]);
                         }
                         sender.sendChatToPlayer(EnumChatFormatting.GREEN + "Done.");
                         timer.sendAutoUpdate();
                     }
                     else
                     {
                         if (!JavaHelper.isNumeric(args[3]))
                         {
                             sender.sendChatToPlayer(EnumChatFormatting.RED + " " + args[3] + " is not a number!");
                         }
                         else if (!JavaHelper.isNumeric(args[4]))
                         {
                             sender.sendChatToPlayer(EnumChatFormatting.RED + " " + args[4] + " is not a number!");
                         }
                         else if (!JavaHelper.isNumeric(args[5]))
                         {
                             sender.sendChatToPlayer(EnumChatFormatting.RED + " " + args[5] + " is not a number!");
                         }
                         else if ((args.length < 5) && !JavaHelper.isNumeric(args[6]))
                         {
                             sender.sendChatToPlayer(EnumChatFormatting.RED + " " + args[6] + " is not a number!");
                         }
                         else
                         {
                             sender.sendChatToPlayer("?????????? < Please report to modmaker, idk how you got here...");
                         }
                     }
                 }
                 else
                 {
                     sender.sendChatToPlayer(EnumChatFormatting.RED + "You can't edit this timer.");
                 }
             }
             else
             {
                 sender.sendChatToPlayer("Current point: [" + timer.X + ";" + timer.Y + ";" + timer.Z + "] in world " + timer.dim);
             }
         }
         else
         {
             sender.sendChatToPlayer(EnumChatFormatting.RED + "This timer doesn't exists or is not a point timer.");
         }
     }
 
     /**
      * Add part
      */
     public void processCommandAdd(final ICommandSender sender, final String[] args, final boolean op)
     {
         if (args.length < 3)
         {
             sender.sendChatToPlayer(EnumChatFormatting.RED + "Syntax error. Use: /" + this.getCommandName() + " " + AddSyntax);
         }
         else if (ServerTimer.getInstance().timerList.containsKey(args[1]))
         {
             sender.sendChatToPlayer(EnumChatFormatting.RED + "This timer already exists.");
         }
         else
         {
             if (!JavaHelper.isNumeric(args[2]))
             {
                 sender.sendChatToPlayer(EnumChatFormatting.RED + " " + args[2] + " is not a number!");
             }
             else
             {
                 if ((args.length == 6) || (args.length == 7))
                 {
                     if (JavaHelper.isNumeric(args[3]) || JavaHelper.isNumeric(args[4]) || JavaHelper.isNumeric(args[5]))
                     {
                         if (!(sender instanceof EntityPlayer) && (args.length == 6))
                         {
                             sender.sendChatToPlayer("You need to specify a dimention!");
                         }
                         else if ((args.length == 7) && !JavaHelper.isNumeric(args[6]))
                         {
                             sender.sendChatToPlayer(EnumChatFormatting.RED + " " + args[2] + " is not a number!");
                         }
                         else
                         {
                             HardTimerAPI.newPointTimer(args[1], Integer.valueOf(args[2]), Integer.valueOf(args[3]), Integer.valueOf(args[4]), Integer.valueOf(args[5]), (args.length == 7) ? Integer.valueOf(args[6]) : ((EntityPlayer) sender).dimension);
                         }
                     }
                     else
                     {
                         if (!JavaHelper.isNumeric(args[3]))
                         {
                             sender.sendChatToPlayer(EnumChatFormatting.RED + " " + args[3] + " is not a number!");
                         }
                         else if (!JavaHelper.isNumeric(args[4]))
                         {
                             sender.sendChatToPlayer(EnumChatFormatting.RED + " " + args[4] + " is not a number!");
                         }
                         else if (!JavaHelper.isNumeric(args[5]))
                         {
                             sender.sendChatToPlayer(EnumChatFormatting.RED + " " + args[5] + " is not a number!");
                         }
                         else
                         {
                             sender.sendChatToPlayer("?????????? < Please report to modmaker, idk how you got here...");
                         }
                     }
                 }
                 else
                 {
                     HardTimerAPI.newBasicTimer(args[1], Integer.valueOf(args[2]));
                 }
             }
         }
     }
 
     @Override
     @SuppressWarnings({ "unchecked" })
     public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args)
     {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "help", "list", "add", "stop", "sound", "point");
        else if ((args.length == 2) && (args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("sound") || args[0].equalsIgnoreCase("stop")))
             return getListOfStringsFromIterableMatchingLastWord(args, ServerTimer.getInstance().timerList.keySet());
         else
             return null;
     }

    public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender)
    {
        return true;
    }

 }
