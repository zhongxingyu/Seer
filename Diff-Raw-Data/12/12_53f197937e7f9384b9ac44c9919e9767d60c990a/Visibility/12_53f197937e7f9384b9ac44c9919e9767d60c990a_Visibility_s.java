 package to.joe.j2mc.core.visibility;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
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
     public Player getPlayer(String target, Player searcher) throws BadPlayerMatchException {
 
         final List<Player> players = new ArrayList<Player>();
         final boolean hidingVanished = (searcher != null) && VanishPerms.canSeeAll(searcher);
        for (final Player p : J2MC_Manager.getCore().getServer().getOnlinePlayers()) {
             try {
                if (!hidingVanished || !VanishNoPacket.isVanished(p.getName())) {
                    if (p.getName().toLowerCase().contains(target.toLowerCase())) {
                        players.add(p);
                     }
                 }
             } catch (final VanishNotLoadedException e) {
                 J2MC_Manager.getCore().buggerAll("VanishNoPacket DIED");
             }
         }
         if (players.size() > 1) {
             throw new TooManyPlayersException(players.size());
         }
         if (players.size() == 0) {
             throw new NoPlayersException();
         }
         return players.get(0);
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
