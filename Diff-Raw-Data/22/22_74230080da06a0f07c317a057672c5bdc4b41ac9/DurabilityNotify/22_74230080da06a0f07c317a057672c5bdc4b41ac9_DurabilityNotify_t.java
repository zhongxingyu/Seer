 /*
  * DurabilityNotify - a bukkit plugin
  *
  * Copyright (C) 2013 Blake Bartenbach
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * FIXME: Known issues:
 *   1) Improper tool notifications - working better...still needs work though.
  *   2) That god damn carrot stick
  *
  *   Doing:
  *   1) watching for helmet break notification.
  *   2) testing improper notification
  */
 
 package co.proxa.durabilitynotify;
 
 import co.proxa.durabilitynotify.file.Paths;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import co.proxa.durabilitynotify.file.FileHandler;
 import co.proxa.durabilitynotify.listeners.*;
 import co.proxa.durabilitynotify.threads.ReminderThread;
 
 public class DurabilityNotify extends JavaPlugin {
 
     private final LiveNotify ln = new LiveNotify();
     private Armor a;
     private ReminderThread rt;
 
     @Override
     public void onEnable() {
         final FileHandler fh = new FileHandler(this);
         final ListManager lm = new ListManager(this);
         final Permissions p = new Permissions(this);
         final Tool t = new Tool(lm);
         a = new Armor(lm);
         final BlockBreakListener bbl = new BlockBreakListener(lm);
         final BowListener bl = new BowListener(lm);
         final FishingListener fl = new FishingListener(lm);
         final CombatListener cl = new CombatListener(lm, a);
         final HoeListener hl = new HoeListener(lm);
         final FlintAndSteelListener fasl = new FlintAndSteelListener(lm);
         final ShearListener sl = new ShearListener(lm);
         final PluginManager pm = getServer().getPluginManager();
         final Notify n = new Notify();
 
         this.checkReminderThread(a);
         fh.checkFiles();
         lm.initializeLists();
 
         pm.registerEvents(bbl, this);
         pm.registerEvents(bl, this);
         pm.registerEvents(fl, this);
         pm.registerEvents(hl, this);
         pm.registerEvents(cl, this);
         pm.registerEvents(fasl, this);
         pm.registerEvents(sl, this);
     }
 
     private void checkReminderThread(Armor a) {
         if (this.getConfig().getBoolean(Paths.reminderEnabled)) {
             int minutes = this.getConfig().getInt(Paths.reminderMinutes);
             rt = new ReminderThread(this, a, minutes);
             rt.startThread();
         }
     }
 
     @Override
     public void onDisable() {
         rt.stopThread();
         ln.clearMap();
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (sender instanceof Player && Permissions.hasToolPerms((Player) sender) && label.equalsIgnoreCase("dura")) {
             if (ln.onMap((Player)sender)) {
                 ln.toggleNotify((Player)sender);
             } else {
                 LiveNotify.putPlayerOnMap((Player)sender);
             }
             ln.sendMessage((Player)sender);
             return true;
         } else if (sender instanceof Player && Permissions.hasArmorPerms((Player) sender) && label.startsWith("arm")) {
             a.checkArmorCommand((Player) sender);
             return true;
         }
         return false;
     }
 
 }
