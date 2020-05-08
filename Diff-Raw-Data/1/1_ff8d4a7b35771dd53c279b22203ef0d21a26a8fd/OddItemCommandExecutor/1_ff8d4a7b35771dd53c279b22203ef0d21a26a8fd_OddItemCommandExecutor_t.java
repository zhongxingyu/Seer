 /* This program is free software: you can redistribute it and/or modify
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
  */
 package info.somethingodd.OddItem;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * @author Gordon Pettey (petteyg359@gmail.com)
  */
 public class OddItemCommandExecutor implements CommandExecutor {
     private OddItemBase oddItemBase;
 
     /**
      * Constructor
      * @param oddItemBase Base plugin
      */
     public OddItemCommandExecutor(OddItemBase oddItemBase) {
         this.oddItemBase = oddItemBase;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (command.getName().equals("odditem")) {
             if (sender.hasPermission("odditem.alias")) {
                 switch (args.length) {
                     case 0:
                         if (sender instanceof Player) {
                             ItemStack itemStack = ((Player) sender).getItemInHand();
                             if (itemStack.getTypeId() > 255)
                                itemStack = new ItemStack(itemStack.getTypeId());
                                 itemStack.setDurability((short) 0);
                             sender.sendMessage(OddItem.getAliases(itemStack).toString());
                             return true;
                         }
                         break;
                     case 1:
                         try {
                             sender.sendMessage(OddItem.getAliases(args[0]).toString());
                         } catch (IllegalArgumentException e) {
                             sender.sendMessage("[OddItem] No such alias. Similar: " + e.getMessage());
                         }
                         return true;
                 }
             } else {
                 sender.sendMessage("DENIED");
             }
         } else if (command.getName().equals("odditeminfo")) {
             if (sender.hasPermission("odditem.info")) {
                 sender.sendMessage("[OddItem] " + OddItem.items.itemCount() + " items with " + OddItem.items.aliasCount() + " aliases");
                 sender.sendMessage("[OddItem] " + OddItem.groups.groupCount() + " groups with " + OddItem.groups.aliasCount() + " aliases");
             } else {
                 sender.sendMessage("DENIED");
             }
             return true;
         } else if (command.getName().equals("odditemreload")) {
             if (sender.hasPermission("odditem.reload")) {
                 sender.sendMessage("[OddItem] Reloading...");
                 OddItem.clear();
                 new OddItemConfiguration(oddItemBase).configure();
             } else {
                 sender.sendMessage("DENIED");
             }
             return true;
         }
         return false;
     }
 }
