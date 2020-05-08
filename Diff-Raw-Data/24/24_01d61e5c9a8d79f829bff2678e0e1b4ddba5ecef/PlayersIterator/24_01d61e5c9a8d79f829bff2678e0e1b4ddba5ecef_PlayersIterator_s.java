 package fr.aumgn.bukkitutils.playerid.iterator;
 
 import java.util.Iterator;
 
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.playerid.PlayerId;
 
 public class PlayersIterator implements Iterator<Player> {
 
     private Iterator<PlayerId> it;
     private PlayerId next;
 
     public PlayersIterator(Iterator<PlayerId> iterator) {
         this.it = iterator;
         forward();
     }
 
    public void forward() {
        while (next.isOffline()) {
             next = it.next();
         }
     }
 
     @Override
     public boolean hasNext() {
        return it.hasNext();
     }
 
     @Override
     public Player next() {
         PlayerId current = next;
 
        next = it.next();
         forward();
 
         return current.getPlayer();
     }
 
     @Override
     public void remove() {
         throw new UnsupportedOperationException();
     }
 
 }
