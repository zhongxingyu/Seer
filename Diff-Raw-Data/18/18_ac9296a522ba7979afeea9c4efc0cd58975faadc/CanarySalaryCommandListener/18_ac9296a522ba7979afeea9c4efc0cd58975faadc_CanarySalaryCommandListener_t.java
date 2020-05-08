 /*
  * This file is part of MySalary.
  *
  * Copyright © 2011-2013 Visual Illusions Entertainment
  *
  * MySalary is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * MySalary is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with MySalary.
  * If not, see http://www.gnu.org/licenses/gpl.html.
  */
 package net.visualillusionsent.mysalary.canary;
 
 import net.canarymod.Canary;
 import net.canarymod.api.entity.living.humanoid.Player;
 import net.canarymod.chat.Colors;
 import net.canarymod.chat.MessageReceiver;
 import net.canarymod.commandsys.Command;
 import net.canarymod.commandsys.CommandDependencyException;
 import net.visualillusionsent.dconomy.dCoBase;
 import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPluginInformationCommand;
 import net.visualillusionsent.utils.VersionChecker;
 
 import java.text.MessageFormat;
 
 /**
  * Canary MySalary Command Listener
  *
  * @author Jason (darkdiplomat)
  */
 public class CanarySalaryCommandListener extends VisualIllusionsCanaryPluginInformationCommand {
     private static final String salary_msg = Colors.LIGHT_GREEN + "Your salary is " + Colors.ORANGE + " {0,number,#.##} " + dCoBase.getProperties().getString("money.name"),
             salary_pay = Colors.LIGHT_GREEN + "You have received " + Colors.ORANGE + " {0,number,#.##} " + dCoBase.getProperties().getString("money.name");
 
     public CanarySalaryCommandListener(CanarySalary plugin) throws CommandDependencyException {
         super(plugin);
         Canary.commands().registerCommands(this, plugin, false);
     }
 
     @Command(aliases = { "mysalary", "mys" },
             description = "MySalary information/main Command",
             permissions = { "mysalary.getpaid" },
             toolTip = "/mysalary [subcommand]")
     public final void information(MessageReceiver msgrec, String[] args) {
         for (String msg : about) {
             if (msg.equals("$VERSION_CHECK$")) {
                 VersionChecker vc = plugin.getVersionChecker();
                 Boolean isLatest = vc.isLatest();
                 if (isLatest == null) {
                     msgrec.message(center(Colors.GRAY + "VersionCheckerError: " + vc.getErrorMessage()));
                 }
                 else if (!isLatest) {
                     msgrec.message(center(Colors.GRAY + vc.getUpdateAvailibleMessage()));
                 }
                 else {
                     msgrec.message(center(Colors.LIGHT_GREEN + "Latest Version Installed"));
                 }
 
                 //Inject MySalary messages
                 msgrec.message(Colors.LIGHT_GREEN + "Next PayCheck in: " + Colors.ORANGE + getCS().getFinance().getTimeUntil());
                 if (getCS().getCfg().isGroupSpecificEnabled()) {
                     if (msgrec instanceof Player) {
                         double salary = getCS().getCfg().getGroupPay(((Player) msgrec).getGroup().getName());
                         if (salary > 0) {
                             msgrec.message(MessageFormat.format(salary_msg, salary));
                         }
                         else {
                             msgrec.notice("You do not have a salary.");
                         }
                     }
                     else {
                         if (getCS().getCfg().payServer()) {
                             msgrec.message(MessageFormat.format(salary_msg, getCS().getCfg().getGroupPay("SERVER")));
                         }
                     }
                 }
                 else {
                     msgrec.message(MessageFormat.format(salary_msg, getCS().getCfg().getDefaultPayAmount()));
                 }
             }
             else {
                 msgrec.message(msg);
             }
         }
     }
 
     @Command(aliases = { "claim" },
             description = "Used to claim pending checks",
             permissions = { "mysalary.getpaid" },
             parent = "mysalary",
             toolTip = "/mysalary claim")
     public final void claimcheck(MessageReceiver msgrec, String[] args) {
         if (getCS().getCfg().isRequireClaimEnabled()) {
             double result = getCS().getFinance().checkPendingAndPay(msgrec.getName());
             if (result > 0) {
                 msgrec.message(MessageFormat.format(salary_pay, result));
             }
             else {
                 msgrec.notice("You do not have any pending checks.");
             }
         }
         else {
             msgrec.notice("Checks are auto-deposited. Claiming is not required.");
         }
     }
 
     @Command(aliases = { "broadcast" },
             description = "Broadcasts time until next paycheck",
             permissions = { "mysalary.admin" },
             parent = "mysalary",
             toolTip = "/mysalary broadcast")
     public final void broadcast(MessageReceiver msgrec, String[] args) {
         Canary.getServer().broadcastMessage("[§AMySalary§F]§A Next PayCheck in: " + Colors.ORANGE + getCS().getFinance().getTimeUntil());
     }
 
     @Command(aliases = { "forcepay" },
             description = "Forces a pay out of checks",
             permissions = { "mysalary.admin" },
             parent = "mysalary",
             toolTip = "/mysalary forcepay [reset]")
     public final void forcepay(MessageReceiver msgrec, String[] args) {
        if (args.length == 2 && args[1].toLowerCase().equals("reset")) {
             getCS().getFinance().reset(false);
         }
         getCS().getFinance().payout();
     }
 
     @Command(aliases = { "setprop" },
             description = "Sets/changes a property value",
             permissions = { "mysalary.admin" },
             parent = "mysalary",
             toolTip = "/mysalary setprop <key> <value>",
             min = 2
     )
     public final void setProp(MessageReceiver msgrec, String[] args) {
         try {
             getCS().getCfg().setProperty(args[1], args[2]);
             msgrec.message(Colors.ORANGE + args[1] + Colors.LIGHT_GREEN + " is now set to " + Colors.YELLOW + args[2]);
         }
         catch (IllegalArgumentException iaex) {
             msgrec.notice(iaex.getMessage());
         }
     }
 
     private final CanarySalary getCS() {
         return (CanarySalary) plugin;
     }
 }
