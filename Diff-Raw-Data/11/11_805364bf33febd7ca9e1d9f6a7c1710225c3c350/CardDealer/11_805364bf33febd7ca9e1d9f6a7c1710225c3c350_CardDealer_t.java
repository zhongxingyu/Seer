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
 
     private final Iterator<Card> dealer;
 
     /**
      * Creates a new instance. This instance should only be used while {@link
     * #hasMore()} returns {@code true}.
      */
     public CardDealer()
     {
         List<SuspectCard> suspects = Arrays.asList(SuspectCard.values());
         List<WeaponCard> weapons = Arrays.asList(WeaponCard.values());
         List<RoomCard> rooms = Arrays.asList(RoomCard.values());
 
         Collections.shuffle(suspects);
         Collections.shuffle(weapons);
         Collections.shuffle(rooms);
 
         // pop the first card off and put into the solution "envelope"
         solution = new Solution(suspects.remove(0),
                                 weapons.remove(0),
                                 rooms.remove(0));
 
         // Place the rest into one big list, shuffle again, and make a "dealer"
 
         List<Card> allCards = new ArrayList<Card>();
         
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
      * Randomly deals (returns) the next {@link Card}, or throws an exception if
      * there are no more cards to deal.
      *
      * @return the next random {@link Card}, never {@code null}.
      *
      * @throws NoSuchElementException if there is no next card (see {@link
      *     #hasMore()})
      */
     public Card deal()
     {
         return dealer.next();
     }
 }
