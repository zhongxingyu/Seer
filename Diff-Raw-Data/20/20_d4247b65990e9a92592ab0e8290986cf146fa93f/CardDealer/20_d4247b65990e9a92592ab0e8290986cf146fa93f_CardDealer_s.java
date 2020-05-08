 package keyf.clueless;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import keyf.clueless.data.card.Card;
 import keyf.clueless.data.card.RoomCard;
 import keyf.clueless.data.card.SuspectCard;
 import keyf.clueless.data.card.WeaponCard;
 import keyf.clueless.data.Item;
 import keyf.clueless.data.Suspect;
 import keyf.clueless.data.Weapon;
 import keyf.clueless.data.location.Room;
 
 /**
 * Sets aside one {@link Card} each of the {@link SuspectCard}s, {@link 
  * WeaponCard}s, and {@link RoomCard}s for the {@link #getSolution() solution},
  * and iterates over the rest of the {@link SuspectCard}s, {@link WeaponCard}s,
  * and {@link RoomCard}s, returning a random {@link Card} on each call to {@link
  * #deal}.
 * 
  * @author justin
  */
 public class CardDealer
 {
     private final Solution solution;
 
     private final Iterator<? extends Item> dealer;
 
     /**
      * Creates a new instance. This instance should only be used while {@link
      * #hasMore()} returns {@code true}.
      */
     public CardDealer()
     {
        List<Suspect> suspects = Arrays.asList(Suspect.values());
        List<Weapon> weapons = Arrays.asList(Weapon.values());
        List<Room> rooms = Arrays.asList(Room.values());
 
         Collections.shuffle(suspects);
         Collections.shuffle(weapons);
         Collections.shuffle(rooms);
 
         // pop the first card off and put into the solution "envelope"
         solution = new Solution(suspects.remove(0),
                                 weapons.remove(0),
                                 rooms.remove(0));
 
         // Place the rest into one big list, shuffle again, and make a "dealer"
 
         List<Item> allCards = new ArrayList<Item>();
        
         allCards.addAll(suspects);
         allCards.addAll(weapons);
         allCards.addAll(rooms);
 
         Collections.shuffle(allCards);
 
         dealer = allCards.iterator();
     }
 
     /**
      * Returns a {@link Solution}.
      *
      * @return never {@code null}.
      */
     public Solution getSolution()
     {
         return solution;
     }
 
     /**
      * Returns {@code true} if this dealer has more cards to deal. If {@code
      * false} is returned, this instance should no longer be used.
      *
      * @return {@code true}  if this dealer has more cards to deal, {@code
      *     false} otherwise.
      */
     public boolean hasMore()
     {
         return dealer.hasNext();
     }
 
     /**
      * Randomly deals (returns) the next {@link Item}, or throws an exception if
      * there are no more cards to deal.
      *
      * @return the next random {@link Item}, never {@code null}.
      *
      * @throws NoSuchElementException if there is no next card (see {@link
      *     #hasMore()})
      */
     public Item deal()
     {
         return dealer.next();
     }
 }
