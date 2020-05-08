 package to.joe.j2mc.core.visibility;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.kitteh.vanish.VanishPerms;
 import org.kitteh.vanish.staticaccess.VanishNoPacket;
 import org.kitteh.vanish.staticaccess.VanishNotLoadedException;
 
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.core.exceptions.BadPlayerMatchException;
 import to.joe.j2mc.core.exceptions.NoPlayersException;
 import to.joe.j2mc.core.exceptions.TooManyPlayersException;
 
 public class Visibility {
 
     /**
      * @param searcher
      *            set as null for accessing all players on server
      * @return
      */
     public List<Player> getOnlinePlayers(Player searcher) {
        final List<Player> players = Arrays.asList(J2MC_Manager.getCore().getServer().getOnlinePlayers());
         if ((searcher != null) && !VanishPerms.canSeeAll(searcher)) {
             for (final Player player : J2MC_Manager.getCore().getServer().getOnlinePlayers()) {
                 try {
                     if ((player != null) && VanishNoPacket.isVanished(player.getName())) {
                         players.remove(player);
                     }
                 } catch (final VanishNotLoadedException e) {
                     J2MC_Manager.getCore().buggerAll("VanishNoPacket DIED");
                 }
             }
         }
         return players;
     }
     
     /**
      * @param target
      * @param searcher
      *            set as null for accessing all players on server
      * @return player
      * @throws TooManyPlayersException
      * @throws NoPlayersException
      */
     public Player getPlayer(String target, CommandSender searcher) throws BadPlayerMatchException {
         return this.getPlayer(target, searcher, (String) null);
     }
 
     /**
      * @param target
      * @param searcher
      *            set as null for accessing all players on server
      * @param toIgnore
      *            users to ignore
      * @return player
      * @throws TooManyPlayersException
      * @throws NoPlayersException
      */
     public Player getPlayer(String target, CommandSender searcher, String... toIgnore) throws BadPlayerMatchException {
 
         final List<Player> players = new ArrayList<Player>();
         final Set<String> toIgnoreSet = new HashSet<String>();
         if (toIgnore != null) {
             for (int i = 0; i < toIgnore.length; i++) {
                 if (toIgnore[i] != null) {
                     toIgnoreSet.add(toIgnore[i].toLowerCase());
                 }
             }
         }
         final boolean hidingVanished = (searcher != null) && (searcher instanceof Player) && !VanishPerms.canSeeAll((Player) searcher);
         for (final Player player : J2MC_Manager.getCore().getServer().getOnlinePlayers()) {
             try {
                 if (!toIgnoreSet.contains(player.getName()) && (!hidingVanished || !VanishNoPacket.isVanished(player.getName()))) {
                     if (player.getName().toLowerCase().contains(target.toLowerCase())) {
                         players.add(player);
                     }
                     if (player.getName().equalsIgnoreCase(target)) {
                         return player;
                     }
                 }
             } catch (final VanishNotLoadedException e) {
                 J2MC_Manager.getCore().buggerAll("VanishNoPacket DIED");
             }
         }
         if (players.size() > 1) {
             StringBuilder sb = new StringBuilder();
             for (Player player : players) {
                 sb.append(player.getName());
                 sb.append(", ");
             }
             sb.setLength(sb.length() - 2);
             throw new TooManyPlayersException(sb.toString());
         }
         if (players.size() == 0) {
             throw new NoPlayersException();
         }
         return players.get(0);
     }
     
     /**
      * Get a alphabetically sorted list of potential players from an incomplete beginning of a name
      * 
      * @param incompleteString The incomplete string
      * @param searcher The searcher
      * @return The sorted list
      * @throws BadPlayerMatchException If no players were matched
      */
     public List<String> getPotentialMatches(String incompleteString, CommandSender searcher) throws BadPlayerMatchException {
         
         List<String> result = new ArrayList<String>();
         final boolean hidingVanished = (searcher != null) && (searcher instanceof Player) && !VanishPerms.canSeeAll((Player) searcher);
         
         for (final Player player : J2MC_Manager.getCore().getServer().getOnlinePlayers()) {
             try {
                 if (hidingVanished && VanishNoPacket.isVanished(player.getName())) {
                     continue;
                 }
                 if (player.getName().startsWith(incompleteString)) {
                     result.add(player.getName());
                 }
             } catch (final VanishNotLoadedException e) {
                 J2MC_Manager.getCore().buggerAll("VanishNoPacket DIED");
             }
         }
         
         if (result.size() == 0) {
             throw new NoPlayersException();
         }
         
         Collections.sort(result);
         return result;
     }
 
     /**
      * Is the player vanished?
      * 
      * @param player
      * @return
      */
     public boolean isVanished(Player player) {
         try {
             return VanishNoPacket.isVanished(player.getName());
         } catch (final VanishNotLoadedException e) {
             J2MC_Manager.getCore().buggerAll("VanishNoPacket DIED");
         }
         return false;
     }
 
 }
