 /*
  * MyResidence, Bukkit plugin for managing your towns and residences
  * Copyright (C) 2011, Michael Hohl
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
  */
 
 package at.co.hohl.myresidence.commands;
 
 import at.co.hohl.mcutils.chat.Chat;
 import at.co.hohl.myresidence.MyResidence;
 import at.co.hohl.myresidence.Nation;
 import at.co.hohl.myresidence.ResidenceManager;
 import at.co.hohl.myresidence.event.ResidenceChangedEvent;
 import at.co.hohl.myresidence.event.ResidenceCreatedEvent;
 import at.co.hohl.myresidence.event.ResidenceRemovedEvent;
 import at.co.hohl.myresidence.exceptions.*;
 import at.co.hohl.myresidence.storage.Session;
 import at.co.hohl.myresidence.storage.persistent.Inhabitant;
 import at.co.hohl.myresidence.storage.persistent.Residence;
 import at.co.hohl.myresidence.storage.persistent.Town;
 import com.nijikokun.register.payment.Method;
 import com.sk89q.minecraft.util.commands.Command;
 import com.sk89q.minecraft.util.commands.CommandContext;
 import com.sk89q.minecraft.util.commands.CommandPermissions;
 import com.sk89q.minecraft.util.commands.NestedCommand;
 import com.sk89q.worldedit.IncompleteRegionException;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 import org.bukkit.entity.Player;
 
 /**
  * Command for managing residences.
  *
  * @author Michael Hohl
  */
 public class ResidenceCommands {
   @Command(
           aliases = {"create", "c"},
           usage = "<name>",
           desc = "Creates a new residence with passed name",
           min = 1,
           flags = "w"
   )
   @CommandPermissions({"myresidence.major", "myresidence.residence.wildness"})
   public static void create(final CommandContext args,
                             final MyResidence plugin,
                             final Nation nation,
                             final Player player,
                             final Session session)
           throws IncompleteRegionException, MyResidenceException {
 
     // Can not make a Residence without a selection.
     final Selection selection = plugin.getWorldEdit().getSelection(player);
     if (selection == null) {
       throw new IncompleteRegionException();
     }
 
     // Check if player has enough right.
     final boolean buildInWildness = args.hasFlag('w');
     if (buildInWildness && !nation.getPermissionsResolver().hasPermission(player, "myresidence.residence.wildness")) {
       throw new PermissionsDeniedException("You are not allowed to create a residence in wildness!");
     }
     if (!buildInWildness && !nation.getPermissionsResolver().hasPermission(player, "myresidence.major")) {
       throw new PermissionsDeniedException("Your are not allowed to create residences inside towns!");
     }
 
     // If player wants to create a residence in town, he must be inside a town too!
     final Town town = nation.getTown(player.getLocation());
     if (!buildInWildness) {
       if (town == null) {
         throw new MyResidenceException("You can not create a residence outside the town!");
       } else if (!nation.getChunkManager().hasChunks(town, selection.getWorld(),
               selection.getRegionSelector().getRegion().getChunks())) {
         throw new MyResidenceException("Town does not own the chunks, where you want to create the residence!");
       }
     }
 
     // Create task, which gets executed after selecting a sign.
     session.setTask(new Runnable() {
       public void run() {
         final Residence residence = new Residence();
         residence.setName(args.getJoinedStrings(0));
         residence.setOwnerId(session.getPlayerId());
         if (!buildInWildness) {
           residence.setTownId(town.getId());
         }
         nation.save(residence);
 
         ResidenceManager manager = nation.getResidenceManager(residence);
         manager.setSign(session.getSelectedSignBlock());
         manager.setArea(selection);
 
         Chat.sendMessage(player, "&2Residence {0} created!", residence);
 
         plugin.info("Residence %s (ID:%d) created by %s.", residence, residence.getId(), player.getName());
 
         plugin.getEventManager().callEvent(new ResidenceCreatedEvent(session, residence));
       }
     });
     session.setTaskActivator(Session.Activator.SELECT_SIGN);
 
     // Notify user about he has to select a sign.
     Chat.sendMessage(player, "&dPlease select a sign, to link it to the new Residence!");
   }
 
   @Command(
           aliases = {"redefine"},
           desc = "Changes a residences area",
           max = 0
   )
   @CommandPermissions({"myresidence.major"})
   public static void redefine(final CommandContext args,
                               final MyResidence plugin,
                               final Nation nation,
                               final Player player,
                               final Session session)
           throws NotOwnException, NoResidenceSelectedException, IncompleteRegionException {
     Residence residence = session.getSelectedResidence();
    if (!session.hasMajorRights(nation.getTown(residence.getTownId()))) {
       throw new NotOwnException();
     }
 
     // Can not make a Residence without a selection.
     final Selection selection = plugin.getWorldEdit().getSelection(player);
     if (selection == null) {
       throw new IncompleteRegionException();
     }
 
     ResidenceManager manager = nation.getResidenceManager(residence);
     manager.setArea(selection);
 
     Chat.sendMessage(player, "&2Residence {0} area changed!", residence);
   }
 
   @Command(
           aliases = {"remove", "r"},
           desc = "Removes a residence",
           max = 0
   )
   @CommandPermissions({"myresidence.major"})
   public static void remove(final CommandContext args,
                             final MyResidence plugin,
                             final Nation nation,
                             final Player player,
                             final Session session)
           throws NoResidenceSelectedException, PermissionsDeniedException {
 
     final Residence residenceToRemove = session.getSelectedResidence();
     if (!session.hasResidenceOwnerRights(residenceToRemove)) {
       throw new PermissionsDeniedException("Only the owner of the residence can remove it!");
     }
 
     // Create task to confirm.
     session.setTask(new Runnable() {
       public void run() {
         plugin.getEventManager().callEvent(new ResidenceRemovedEvent(session, residenceToRemove));
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
           public void run() {
             nation.remove(residenceToRemove);
             Chat.sendMessage(player, "&2Residence {0} removed!", residenceToRemove);
           }
         });
       }
     });
     session.setTaskActivator(Session.Activator.CONFIRM_COMMAND);
 
     // Notify user about need confirmation.
     Chat.sendMessage(player, "&dDo you really want to remove &5{0}&d?", residenceToRemove);
     Chat.sendMessage(player, "&dUse &5/task confirm&d to confirm this task!");
 
   }
 
   @Command(
           aliases = {"buy", "b"},
           desc = "Buys the residence",
           max = 0
   )
   @CommandPermissions({"myresidence.residence.own"})
   public static void buy(final CommandContext args,
                          final MyResidence plugin,
                          final Nation nation,
                          final Player player,
                          final Session session)
           throws MyResidenceException {
     Residence residence = session.getSelectedResidence();
     Method payment = plugin.getPaymentMethods().getMethod();
 
     if (!residence.isForSale()) {
       throw new MyResidenceException("The owner does not sell this residence!");
     }
 
     if (payment == null) {
       throw new EconomyMissingException();
     }
 
     Method.MethodAccount playerAccount = payment.getAccount(player.getName());
     double price = residence.getPrice();
     if (!playerAccount.hasEnough(price)) {
       throw new NotEnoughMoneyException(price);
     }
     Method.MethodAccount ownerAccount = payment.getAccount(nation.getInhabitant(residence.getOwnerId()).getName());
     playerAccount.subtract(price);
     ownerAccount.add(price);
 
     Player oldOwner = plugin.getServer().getPlayer(nation.getInhabitant(residence.getOwnerId()).getName());
     residence.setOwnerId(nation.getInhabitant(player.getName()).getId());
     residence.setForSale(false);
     nation.getDatabase().save(residence);
 
     Chat.sendMessage(player, "&2You have successfully bought the residence!");
     if (oldOwner != null && oldOwner.isOnline()) {
       Chat.sendMessage(player, "&2Your residence was sold to &a{0}&2 for &a{1}&2.",
               residence, player.getName(), plugin.format(price));
     }
 
     plugin.getEventManager().callEvent(new ResidenceChangedEvent(session, residence));
   }
 
   @Command(
           aliases = {"sell", "sale", "s"},
           usage = "[price]",
           desc = "Makes your residence available for sale",
           min = 0,
           max = 1
   )
   @CommandPermissions({"myresidence.residence.own"})
   public static void sell(final CommandContext args,
                           final MyResidence plugin,
                           final Nation nation,
                           final Player player,
                           final Session session)
           throws MyResidenceException {
     Residence residence = session.getSelectedResidence();
 
     if (!session.hasResidenceOwnerRights(residence)) {
       throw new NotOwnException();
     }
 
     double price = args.getDouble(0, residence.getValue());
     residence.setForSale(true);
     residence.setPrice(price);
     residence.setOwnerId(session.getPlayerId());
     if (residence.getValue() <= 0.0) {
       residence.setValue(price);
     }
     nation.getDatabase().save(residence);
 
     Chat.sendMessage(player, "&3Your residence is available for sale now!");
 
     plugin.getEventManager().callEvent(new ResidenceChangedEvent(session, residence));
   }
 
   @Command(
           aliases = {"give", "transfer", "grant"},
           usage = "<player>",
           desc = "Transfers the residence to an owner",
           min = 1,
           max = 1
   )
   @CommandPermissions({"myresidence.residence.own"})
   public static void give(final CommandContext args,
                           final MyResidence plugin,
                           final Nation nation,
                           final Player player,
                           final Session session)
           throws MyResidenceException {
     Residence residence = session.getSelectedResidence();
     if (!session.hasResidenceOwnerRights(residence)) {
       throw new NotOwnException();
     }
 
     Inhabitant inhabitant = nation.getInhabitant(args.getString(0));
     if (inhabitant == null) {
       throw new PlayerNotFoundException();
     }
 
     residence.setOwnerId(inhabitant.getId());
     nation.save(residence);
 
     Chat.sendMessage(player, "&a{0}&2 transferred to &a{1}&2.", residence, inhabitant);
 
     Player receiver = player.getServer().getPlayer(inhabitant.getName());
     if (receiver != null && receiver.isOnline()) {
       Chat.sendMessage(receiver, "&a{0}&2 transferred you the residence &a{1}&2!", residence, inhabitant);
     }
 
     plugin.getEventManager().callEvent(new ResidenceChangedEvent(session, residence));
   }
 
   @Command(
           aliases = {"area"},
           desc = "Selects the residence area with WorldEdit",
           max = 0
   )
   public static void area(final CommandContext args,
                           final MyResidence plugin,
                           final Nation nation,
                           final Player player,
                           final Session session)
           throws MyResidenceException {
     Selection selection = nation.getResidenceManager(session.getSelectedResidence()).getArea();
     plugin.getWorldEdit().setSelection(player, selection);
 
     Chat.sendMessage(player, "&2Residence area selected with WorldEdit!");
   }
 
   @Command(
           aliases = {"value"},
           usage = "<amount>",
           desc = "Sets the value of a residence",
           min = 1,
           max = 1
   )
   @CommandPermissions({"myresidence.residence.value"})
   public static void value(final CommandContext args,
                            final MyResidence plugin,
                            final Nation nation,
                            final Player player,
                            final Session session)
           throws MyResidenceException {
 
     Residence residence = session.getSelectedResidence();
 
     if (!(session.hasResidenceOwnerRights(residence))) {
       throw new NotOwnException();
     }
 
     residence.setValue(args.getDouble(0));
     nation.getDatabase().save(residence);
 
     Chat.sendMessage(player, "&2Value has been set to &a{0}&2!", plugin.format(residence.getValue()));
 
     plugin.getEventManager().callEvent(new ResidenceChangedEvent(session, residence));
   }
 
   @Command(
           aliases = {"rename"},
           usage = "<name>",
           desc = "Change the name of a residence",
           min = 1
   )
   @CommandPermissions({"myresidence.residence.rename"})
   public static void rename(final CommandContext args,
                             final MyResidence plugin,
                             final Nation nation,
                             final Player player,
                             final Session session)
           throws MyResidenceException {
 
     Residence residence = session.getSelectedResidence();
 
     if (!(session.hasResidenceOwnerRights(residence))) {
       throw new NotOwnException();
     }
 
     residence.setName(args.getJoinedStrings(0));
     nation.getDatabase().save(residence);
 
     Chat.sendMessage(player, "&2Residence renamed to &a{0}&2!", residence.getName());
 
     plugin.getEventManager().callEvent(new ResidenceChangedEvent(session, residence));
 
   }
 
   @Command(
           aliases = {"info", "here", "i"},
           desc = "Shows information about residences",
           usage = "[residence]"
   )
   public static void info(final CommandContext args,
                           final MyResidence plugin,
                           final Nation nation,
                           final Player player,
                           final Session session)
           throws MyResidenceException {
     Residence residence;
 
     if (args.argsLength() == 0) {
       residence = session.getSelectedResidence();
     } else {
       residence = nation.getResidence(args.getJoinedStrings(0));
     }
 
     nation.sendInformation(player, residence);
   }
 
   @Command(
           aliases = {"list", "l"},
           desc = "List & Search Residences"
   )
   @NestedCommand({ResidenceListCommands.class})
   public static void list(final CommandContext args,
                           final MyResidence plugin,
                           final Nation nation,
                           final Player player,
                           final Session session) {
   }
 
   @Command(
           aliases = {"member", "members"},
           desc = "Manage Members of Residences"
   )
   @NestedCommand({ResidenceMemberCommands.class})
   @CommandPermissions({"myresidence.residence.members"})
   public static void members(final CommandContext args,
                              final MyResidence plugin,
                              final Nation nation,
                              final Player player,
                              final Session session) {
   }
 
   @Command(
           aliases = {"flag", "f"},
           desc = "Manage Flags of Residences"
   )
   @NestedCommand({ResidenceFlagCommands.class})
   @CommandPermissions({"myresidence.major"})
   public static void flags(final CommandContext args,
                            final MyResidence plugin,
                            final Nation nation,
                            final Player player,
                            final Session session) {
   }
 }
