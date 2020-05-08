 package ucbang.core;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 public class Card {
     public Card() {
     }
     
     enum play{DAMAGE, HEAL, MISS, DRAW, STEAL, DISCARD}; //played cards
     enum field{DAMAGE, HEAL, MISS, DRAW, STEAL, DISCARD, GUN, HORSE}; //field cards
     
     public Card(Enum e) {
         this.e = e;
         ordinal = e.ordinal();
         name = e.toString();
         if(e instanceof Bang.Characters){
             type = 1;
            int[] threehp = new int[]{3, 6, 8, 16, 21, 27, 28, 30};
            if(Arrays.binarySearch(threehp, ordinal)>=0 && ordinal == threehp[Arrays.binarySearch(threehp, ordinal)]){ //awkward way of doing contains
                 special = 3;
             }
             else
                 special = 4;
         }
         else{
             //TODO: find out what kind of card it is
          
             switch((Bang.CardName)e){
                 //put all direct damage cards here
                 case BANG: special = 1;
                 case PUNCH: 
                 case KNIFE: type = 2; effect = play.DAMAGE.ordinal(); break;
                 case BUFFALO_RIFLE: type = 3;  break;
                 
                 //all cards with miss effect
                 
                 //all cards with beer effect
                 case BEER: special = 1; effect = play.HEAL.ordinal(); break;
                 default: special = 1; type = 2; effect = play.DAMAGE.ordinal(); break; //all cards left untreated are treated as bangs
             }
         }
     }
     
     public Enum e;
         public String name; 
         public int ordinal;
     
     public int type; //1 = char, 2 = play, 3 = field
     public int effect; //1 = deal damage, 2 = heal, 3 = miss, 4 = draw
     public int special; //HP for char cards, ???? for other cards, 1 for beer and bangs, 1 for miss, 2 for dodge
     public boolean discardToPlay; //cards that need a discard to play
     public int range; //used for guns
 }
