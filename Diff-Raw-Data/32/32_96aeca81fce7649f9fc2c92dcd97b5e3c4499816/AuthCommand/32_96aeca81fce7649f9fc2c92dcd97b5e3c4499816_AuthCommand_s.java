 /*
  * ExtraAuth - Extra authentication for bukkit, for accessing account or other plugins (which uses my API)
  * Copyright (C) 2013 Dan Printzell
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package me.wildn00b.extraauth.command;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import me.wildn00b.extraauth.ExtraAuth;
 import me.wildn00b.extraauth.api.AuthManager;
 import me.wildn00b.extraauth.api.AuthMethod;
 import me.wildn00b.extraauth.api.PlayerInformation;
 import me.wildn00b.extraauth.api.event.AuthenticateFailedEvent;
 import me.wildn00b.extraauth.api.event.AuthenticateSuccessfullEvent;
 import me.wildn00b.extraauth.api.event.FailedReason;
 import me.wildn00b.extraauth.api.event.PreAuthenticateEvent;
 import me.wildn00b.extraauth.api.event.PreRegistrationEvent;
 import me.wildn00b.extraauth.api.event.PreUnregistrationEvent;
 import me.wildn00b.extraauth.api.event.RegistrationFailedEvent;
 import me.wildn00b.extraauth.api.event.RegistrationSuccessfullEvent;
 import me.wildn00b.extraauth.api.event.UnregistrationFailedEvent;
 import me.wildn00b.extraauth.api.event.UnregistrationSuccessfullEvent;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class AuthCommand implements CommandExecutor {
 
   private final ExtraAuth extraauth;
 
   public AuthCommand(ExtraAuth extraauth) {
     this.extraauth = extraauth;
   }
 
   private void Auth(CommandSender sender, String[] args) {
     final Player player = (Player) sender;
     final PreAuthenticateEvent event = new PreAuthenticateEvent(
         new PlayerInformation(player.getName()));
     extraauth.getServer().getPluginManager().callEvent(event);
     if (event.isCancelled()) {
       extraauth
           .getServer()
           .getPluginManager()
           .callEvent(
               new AuthenticateFailedEvent(new PlayerInformation(player
                   .getName()), FailedReason.CANCELED));
       send(sender, extraauth.Lang._("Command.Auth.Event.Failed"));
     } else {
       final FailedReason ret = extraauth.DB.Auth(player, (Object[]) args);
       if (ret == FailedReason.SUCCESSFULL) {
         extraauth
             .getServer()
             .getPluginManager()
             .callEvent(
                 new AuthenticateSuccessfullEvent(new PlayerInformation(player
                     .getName())));
         send(sender, extraauth.Lang._("Command.Auth.Success"));
       } else {
         extraauth
             .getServer()
             .getPluginManager()
             .callEvent(
                 new AuthenticateFailedEvent(new PlayerInformation(player
                     .getName()), ret));
 
         if (ret == FailedReason.NOT_REGISTERED)
           send(sender, extraauth.Lang._("Command.Auth.NotRegistered.Failed"));
         else if (ret == FailedReason.ALREADY_AUTHED)
           send(sender, extraauth.Lang._("Command.Auth.AlreadyAuthed.Failed"));
         else if (ret == FailedReason.WRONG_KEY)
           send(sender, extraauth.Lang._("Command.Auth.WrongKey.Failed"));
         else if (ret == FailedReason.INVALID_METHOD)
           send(sender, extraauth.Lang._("Command.Auth.InvalidMethod.Failed"));
         else if (ret == FailedReason.INVALID_ARGS)
           send(sender, extraauth.Lang._("Command.InvalidArgs"));
         else
           send(sender, extraauth.Lang._("Command.Auth.Unknown.Failed"));
       }
     }
 
   }
 
   private boolean canUseCommand(CommandSender sender,
       CommandAccountPermission needAccount) {
    if (sender instanceof Player)
      if (extraauth.DB.Contains(((Player) sender).getName())
          && needAccount.isNeedAccount())
        return extraauth.DB.IsAuth((Player) sender)
            || needAccount.isNeedLoggedIn();
      else
        return true;
     else
       return true;
   }
 
   private void Disable(CommandSender sender) {
     final Player player = (Player) sender;
     final PreUnregistrationEvent event = new PreUnregistrationEvent(
         new PlayerInformation(player.getName()));
     extraauth.getServer().getPluginManager().callEvent(event);
 
     if (event.isCancelled()) {
       extraauth
           .getServer()
           .getPluginManager()
           .callEvent(
               new UnregistrationFailedEvent(new PlayerInformation(player
                   .getName()), FailedReason.CANCELED));
       send(sender, extraauth.Lang._("Command.Disable.Event.Failed"));
     } else if (!extraauth.DB.Contains(player.getName())) {
       extraauth
           .getServer()
           .getPluginManager()
           .callEvent(
               new UnregistrationFailedEvent(new PlayerInformation(player
                   .getName()), FailedReason.NOT_REGISTERED));
       send(sender, extraauth.Lang._("Command.Disable.NotRegistered.Failed"));
     } else if (!extraauth.DB.Get(player.getName()).Authed) {
       extraauth
           .getServer()
           .getPluginManager()
           .callEvent(
               new UnregistrationFailedEvent(new PlayerInformation(player
                   .getName()), FailedReason.NEED_TO_AUTHENTICATE));
       send(sender, extraauth.Lang._("NeedToAuthenticate"));
 
     } else if (extraauth.DB.Remove(player.getName()) == FailedReason.SUCCESSFULL) {
       extraauth.getServer().getPluginManager()
           .callEvent(new UnregistrationSuccessfullEvent(player.getName()));
       send(sender, extraauth.Lang._("Command.Disable.Success"));
     } else {
       extraauth
           .getServer()
           .getPluginManager()
           .callEvent(
               new UnregistrationFailedEvent(new PlayerInformation(player
                   .getName()), FailedReason.UNKNOWN));
       send(sender, extraauth.Lang._("Command.Disable.Unknown.Failed"));
     }
   }
 
   private void DisableOther(CommandSender sender, String[] args) {
     if (args.length < 2 && extraauth.DB.Contains(args[1])) {
       send(sender, extraauth.Lang._("Command.NoPlayer"));
       return;
     }
     final PreUnregistrationEvent event = new PreUnregistrationEvent(
         new PlayerInformation(args[1]));
     extraauth.getServer().getPluginManager().callEvent(event);
 
     if (event.isCancelled()) {
       extraauth
           .getServer()
           .getPluginManager()
           .callEvent(
               new UnregistrationFailedEvent(new PlayerInformation(args[1]),
                   FailedReason.CANCELED));
       send(sender, extraauth.Lang._("Command.Disable.Event.Failed"));
     } else if (!extraauth.DB.Contains(args[1])
         && extraauth.DB.Get(args[1]) != null) {
       extraauth
           .getServer()
           .getPluginManager()
           .callEvent(
               new UnregistrationFailedEvent(new PlayerInformation(args[1]),
                   FailedReason.NOT_REGISTERED));
       send(sender,
           extraauth.Lang._("Command.Disable.NotRegistered.Other.Failed"));
     } else if (extraauth.DB.Remove(args[1], false) == FailedReason.SUCCESSFULL) {
       extraauth.getServer().getPluginManager()
           .callEvent(new UnregistrationSuccessfullEvent(args[1]));
       send(sender, extraauth.Lang._("Command.Disable.Other.Success"));
     } else {
       extraauth
           .getServer()
           .getPluginManager()
           .callEvent(
               new UnregistrationFailedEvent(new PlayerInformation(args[1]),
                   FailedReason.UNKNOWN));
       send(sender, extraauth.Lang._("Command.Disable.Unknown.Failed"));
 
     }
   }
 
   private void Enable(CommandSender sender, String label, String[] args) {
 
     final Player player = (Player) sender;
     if (args.length < 2) {
       ShowHelp(sender, label, 1);
       return;
     }
     if (extraauth.DB.Get(player.getName()) != null
         && !extraauth.DB.Get(player.getName()).Authed) {
       extraauth
           .getServer()
           .getPluginManager()
           .callEvent(
               new UnregistrationFailedEvent(new PlayerInformation(player
                   .getName()), FailedReason.NEED_TO_AUTHENTICATE));
       send(sender, extraauth.Lang._("NeedToAuthenticate"));
     } else {
       final PreRegistrationEvent event = new PreRegistrationEvent(
           player.getName());
       extraauth.getServer().getPluginManager().callEvent(event);
 
       if (event.isCancelled()) {
         extraauth
             .getServer()
             .getPluginManager()
             .callEvent(
                 new RegistrationFailedEvent(player.getName(),
                     FailedReason.CANCELED, AuthManager.GetAuthMethod("Unknown")));
         send(sender, extraauth.Lang._("Command.Enable.Event.Failed"));
       } else if (p(player, "auth.enable." + args[1].toLowerCase())) {
         if (args.length > 2) {
           final Object arg[] = Arrays.copyOfRange(args, 2, args.length);
 
           final FailedReason reason = extraauth.DB.Add(player.getName(),
               AuthManager.GetAuthMethod(args[1].toLowerCase()), arg);
           if (reason == FailedReason.SUCCESSFULL) {
             extraauth
                 .getServer()
                 .getPluginManager()
                 .callEvent(
                     new RegistrationSuccessfullEvent(new PlayerInformation(
                         player.getName())));
             send(sender, extraauth.Lang._("Command.Enable.General.Success"));
           } else {
             extraauth
                 .getServer()
                 .getPluginManager()
                 .callEvent(
                     new RegistrationFailedEvent(player.getName(), reason,
                         AuthManager.GetAuthMethod(args[1].toLowerCase())));
             if (reason == FailedReason.ALREADY_REGISTERED)
               send(sender,
                   extraauth.Lang._("Command.Enable.AlreadyRegistered.Failed"));
             else if (reason == FailedReason.INVALID_METHOD)
               send(sender,
                   extraauth.Lang._("Command.Enable.InvalidMethod.Failed"));
             else if (reason == FailedReason.INVALID_ARGS)
               ShowHelp(sender, label, 1);
             else
               send(sender, extraauth.Lang._("Command.Enable.Unknown.Failed"));
           }
         } else
           ShowHelp(sender, label, 1);
       } else
         ShowHelp(sender, label, 1);
     }
   }
 
   private void EnableOther(CommandSender sender, String label, String[] args) {
     if (args.length < 4) {
       ShowHelp(sender, label, 1);
       return;
     }
 
     final PreRegistrationEvent event = new PreRegistrationEvent(args[2]);
     extraauth.getServer().getPluginManager().callEvent(event);
 
     if (event.isCancelled()) {
       extraauth
           .getServer()
           .getPluginManager()
           .callEvent(
               new RegistrationFailedEvent(args[2], FailedReason.CANCELED,
                   AuthManager.GetAuthMethod("Unknown")));
       send(sender, extraauth.Lang._("Command.Enable.Event.Failed"));
     } else if (p(sender, "auth.enableother." + args[1].toLowerCase())
         && AuthManager.GetAuthMethod(args[1]).AllowOtherToEnable()) {
       if (args.length > 3) {
         final Object arg[] = Arrays.copyOfRange(args, 3, args.length);
 
         final FailedReason reason = extraauth.DB.Add(args[2],
             AuthManager.GetAuthMethod(args[1]), arg);
         if (reason == FailedReason.SUCCESSFULL) {
           extraauth
               .getServer()
               .getPluginManager()
               .callEvent(
                   new RegistrationSuccessfullEvent(new PlayerInformation(
                       args[2])));
           send(sender, extraauth.Lang._("Command.Enable.Other.General.Success"));
         } else {
           extraauth
               .getServer()
               .getPluginManager()
               .callEvent(
                   new RegistrationFailedEvent(args[2], reason, AuthManager
                       .GetAuthMethod(args[1].toLowerCase())));
           if (reason == FailedReason.ALREADY_REGISTERED)
             send(sender,
                 extraauth.Lang
                     ._("Command.Enable.Other.AlreadyRegistered.Failed"));
           else if (reason == FailedReason.INVALID_METHOD)
             send(sender,
                 extraauth.Lang._("Command.Enable.InvalidMethod.Failed"));
           else if (reason == FailedReason.INVALID_ARGS)
             ShowHelp(sender, label, 1);
           else
             send(sender, extraauth.Lang._("Command.Enable.Unknown.Failed"));
         }
       } else
         ShowHelp(sender, label, 1);
     } else
       ShowHelp(sender, label, 1);
   }
 
   private void Help(CommandSender sender, String label, String[] args) {
 
     if (args.length > 1)
       ShowHelp(sender, label, Integer.parseInt(args[1]));
     else
       ShowHelp(sender, label, 1);
 
   }
 
   @Override
   public boolean onCommand(CommandSender sender, Command command, String label,
       String[] args) {
     try {
       if (args.length > 0) {
         if (args[0].equalsIgnoreCase("reload") && p(sender, "auth.reload")
             && canUseCommand(sender, CommandAccountPermission.NO_ACCOUNT))
           extraauth.Reload();
         else if (args[0].equalsIgnoreCase("disable")
             && p(sender, "auth.disable", false)
             && canUseCommand(sender, CommandAccountPermission.NEED_LOGGEDIN))
           Disable(sender);
         else if (args[0].equalsIgnoreCase("disableother")
             && p(sender, "auth.disableother", false)
             && canUseCommand(sender, CommandAccountPermission.NEED_LOGGEDIN))
           DisableOther(sender, args);
         else if (args[0].equalsIgnoreCase("help"))
           Help(sender, label, args);
         else if (args[0].equalsIgnoreCase("enable") && sender instanceof Player
            && canUseCommand(sender, CommandAccountPermission.NEED_LOGGEDIN))
           Enable(sender, label, args);
         else if (args[0].equalsIgnoreCase("enableother")
             && canUseCommand(sender, CommandAccountPermission.NEED_LOGGEDIN))
           EnableOther(sender, label, args);
        else if (sender instanceof Player)
           Auth(sender, args);
         else
           ShowHelp(sender, label, 1);
       } else
         ShowHelp(sender, label, 1);
     } catch (final ArrayIndexOutOfBoundsException e) {
       ShowHelp(sender, label, 1);
     } catch (final Exception e) {
       e.printStackTrace();
       send(sender, extraauth.Lang._("Command.Exception"));
     }
 
     return true;
   }
 
   private boolean p(CommandSender sender, String permissions) {
     return p(sender, permissions, true);
   }
 
   private boolean p(CommandSender sender, String permissions,
       boolean consoleDefault) {
     if (sender instanceof Player)
       return extraauth.Vault.HasPermissions((Player) sender, permissions);
     else
       return consoleDefault;
   }
 
   private void send(CommandSender sender, String msg) {
     sender
         .sendMessage(ChatColor.YELLOW + "[ExtraAuth] " + ChatColor.GOLD + msg);
   }
 
   private void ShowHelp(CommandSender sender, String label, int page) {
     final ArrayList<String> cmds = new ArrayList<String>();
 
    cmds.add(ChatColor.YELLOW
        + "/"
        + label
        + " help "
        + extraauth.Lang._("Command.Help").replaceFirst("- ",
            ChatColor.DARK_AQUA + "-" + ChatColor.GOLD + " "));
 
     if (p(sender, "tgym.reload")
         && canUseCommand(sender, CommandAccountPermission.NO_ACCOUNT))
       cmds.add(ChatColor.YELLOW
           + "/"
           + label
           + " reload "
           + extraauth.Lang._("Command.Reload").replaceFirst("- ",
               ChatColor.DARK_AQUA + "-" + ChatColor.GOLD + " "));
 
     if (sender instanceof Player
         && new PlayerInformation(sender.getName()).getExist())
       cmds.add(ChatColor.YELLOW
           + "/"
           + label
           + " "
           + extraauth.Lang._("Command.Auth.Help").replaceFirst("- ",
               ChatColor.DARK_AQUA + "-" + ChatColor.GOLD + " "));
 
     for (final Class<? extends AuthMethod> clazz : AuthManager.Methods)
       try {
         final AuthMethod method = clazz.newInstance();
 
         if (p(sender, "tgym.enable." + method.GetName().toLowerCase(), false)
             && canUseCommand(sender, CommandAccountPermission.NEED_LOGGEDIN))
           cmds.add(ChatColor.YELLOW
               + "/"
               + label
               + " enable "
               + method.GetName().toLowerCase()
               + " "
               + method.GetHelpLine(
                   (String) extraauth.Settings._("Language", "en-US"))
                   .replaceFirst("- ",
                       ChatColor.DARK_AQUA + "-" + ChatColor.GOLD + " "));
         if (p(sender, "tgym.enableother." + method.GetName().toLowerCase(),
             true)
             && method.AllowOtherToEnable()
             && canUseCommand(sender, CommandAccountPermission.NEED_LOGGEDIN))
           cmds.add(ChatColor.YELLOW
               + "/"
               + label
               + " enableother "
               + method.GetName().toLowerCase()
               + " "
               + method.GetOtherHelpLine(
                   (String) extraauth.Settings._("Language", "en-US"))
                   .replaceFirst("- ",
                       ChatColor.DARK_AQUA + "-" + ChatColor.GOLD + " "));
       } catch (final Exception e) {
       }
 
     if (p(sender, "tgym.disable", false)
         && canUseCommand(sender, CommandAccountPermission.NEED_LOGGEDIN))
       cmds.add(ChatColor.YELLOW
           + "/"
           + label
           + " disable "
           + extraauth.Lang._("Command.Disable.Help").replaceFirst("- ",
               ChatColor.DARK_AQUA + "-" + ChatColor.GOLD + " "));
 
     if (p(sender, "tgym.disableother")
         && canUseCommand(sender, CommandAccountPermission.NEED_LOGGEDIN))
       cmds.add(ChatColor.YELLOW
           + "/"
           + label
           + " disableother "
           + extraauth.Lang._("Command.Disable.Other.Help").replaceFirst("- ",
               ChatColor.DARK_AQUA + "-" + ChatColor.GOLD + " "));
 
     final int maxpage = 1 + cmds.size() / 6;
     if (page < 1)
       page = 1;
     else if (page > maxpage)
       page = maxpage;
     sender.sendMessage(""
         + ChatColor.RED
         + ChatColor.BOLD
         + extraauth.Lang
             ._("Command.Title")
             .replaceAll("%VERSION%", extraauth.Version)
             .replaceAll("%PAGE%", "" + ChatColor.RED + page + ChatColor.AQUA)
             .replaceAll("%MAXPAGE%",
                 "" + ChatColor.BLUE + maxpage + ChatColor.GOLD)
             .replaceAll("%AUTHOR%", ChatColor.YELLOW + "WildN00b"));
     try {
       for (int i = (page - 1) * 6; i < ((page - 1) * 6) + 6; i++)
         sender.sendMessage(cmds.get(i));
 
     } catch (final Exception e) {
     }
   }
 
 }
