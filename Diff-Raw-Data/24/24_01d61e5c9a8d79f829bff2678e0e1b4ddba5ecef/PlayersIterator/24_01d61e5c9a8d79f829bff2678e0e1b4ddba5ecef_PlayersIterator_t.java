 package fr.aumgn.bukkitutils.playerid.iterator;
 
 import java.util.Iterator;
 
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.playerid.PlayerId;
 
 public class PlayersIterator implements Iterator<Player> {
 
     private Iterator<PlayerId> it;
     private PlayerId next;
 
     public PlayersIterator(Iterator<PlayerId> iterator) {
         this.it = iterator;

        forwardOne();
         forward();
     }
 
    private void forward() {
        while (next != null && next.isOffline()) {
            forwardOne();
        }
    }

    private void forwardOne() {
        if (it.hasNext()) {
             next = it.next();
        } else {
            next = null;
         }
     }
 
     @Override
     public boolean hasNext() {
        return next != null;
     }
 
     @Override
     public Player next() {
         PlayerId current = next;
 
        forwardOne();
         forward();
 
        System.out.println(current.getName() + " : " + current.isOffline());
         return current.getPlayer();
     }
 
     @Override
     public void remove() {
         throw new UnsupportedOperationException();
     }
 
 }
