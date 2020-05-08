  package ucbang.core;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class Deck {
     public static enum CardName {BACK, BULLETBACK, BANG, MISS, BEER, BARREL, DUEL, INDIANS, GATLING, DYNAMITE, SALOON, WELLS_FARGO, STAGECOACH, GENERAL_STORE, CAT_BALLOU, PANIC, JAIL, APPALOOSA, MUSTANG, VOLCANIC, SCHOFIELD, REMINGTON, REV_CARBINE, WINCHESTER, HIDEOUT, SILVER, BRAWL, DODGE, PUNCH, RAG_TIME, SPRINGFIELD, TEQUILA, WHISKY, BIBLE, BUFFALO_RIFLE, CAN_CAN, CANTEEN, CONESTOGA, DERRINGER, HOWITZER, IRON_PLATE, KNIFE, PEPPERBOX, PONY_EXPRESS, SOMBRERO, TEN_GALLON_HAT};
     public static enum Characters {BART_CASSIDY, BLACK_JACK, CALAMITY_JANET, EL_GRINGO, JESSE_JONES, JOURDONNAIS, KIT_CARLSON, LUCKY_DUKE, PAUL_REGRET, PEDRO_RAMIREZ, ROSE_DOOLAN, SID_KETCHUM, SLAB_THE_KILLER, SUZY_LAFAYETTE, VULTURE_SAM, WILLY_THE_KID, APACHE_KID, BELLE_STAR, BILL_NOFACE, CHUCK_WENGAM, DOC_HOLYDAY, ELENA_FUENTE, GREG_DIGGER, HERB_HUNTER, JOSE_DELGADO, MOLLY_STARK, PAT_BRENNAN, PIXIE_PETE, SEAN_MALLORY, TEQUILA_JOE, VERA_CUSTER};
     public static enum Role {SHERIFF, DEPUTY, OUTLAW, RENEGADE};
     public ArrayDeque<Card> drawPile = new ArrayDeque<Card>();
     public ArrayList<Card> discardPile = new ArrayList<Card>();
     public Deck(){
     }
     public void fillCharacterCards(int numPlayers){
         //Assign character cards
         ArrayList<Enum> charList = new ArrayList<Enum>();
         for(Enum e: Characters.values()){
             charList.add(e);
         }
         for(int n = 0; n<numPlayers; n++){
             drawPile.add(new Card(charList.remove((int)(Math.random()*charList.size()))));
             drawPile.add(new Card(charList.remove((int)(Math.random()*charList.size()))));
             drawPile.add(new Card(charList.remove((int)(Math.random()*charList.size()))));
             drawPile.add(new Card(charList.remove((int)(Math.random()*charList.size()))));
             drawPile.add(new Card(charList.remove((int)(Math.random()*charList.size()))));
         }
     }
     public void fillGameCards(){
         //Create a drawPile
         Enum[] cards = new Enum[120];
         /*Arrays.fill(cards, 0, 1, CardName.APPALOOSA);
         Arrays.fill(cards, 1, 30, CardName.BANG);
         Arrays.fill(cards, 30, 33, CardName.BARREL);
         Arrays.fill(cards, 33, 41, CardName.BEER);
         Arrays.fill(cards, 41, 42, CardName.BIBLE);
         Arrays.fill(cards, 42, 43, CardName.BRAWL);
         Arrays.fill(cards, 43, 44, CardName.BUFFALO_RIFLE);
         Arrays.fill(cards, 44, 45, CardName.CAN_CAN);
         Arrays.fill(cards, 45, 51, CardName.CAT_BALLOU);
         Arrays.fill(cards, 51, 52, CardName.CONESTOGA);
         Arrays.fill(cards, 52, 53, CardName.DERRINGER);
         Arrays.fill(cards, 53, 55, CardName.DODGE);
         Arrays.fill(cards, 55, 58, CardName.DUEL);
         Arrays.fill(cards, 58, 60, CardName.DYNAMITE);
         Arrays.fill(cards, 60, 61, CardName.GATLING);
         Arrays.fill(cards, 61, 64, CardName.GENERAL_STORE);
         Arrays.fill(cards, 64, 66, CardName.HOWITZER);
         Arrays.fill(cards, 66, 69, CardName.INDIANS);
         Arrays.fill(cards, 69, 71, CardName.IRON_PLATE);
         Arrays.fill(cards, 71, 74, CardName.JAIL);
         Arrays.fill(cards, 74, 75, CardName.KNIFE);
         Arrays.fill(cards, 75, 88, CardName.MISS);
         Arrays.fill(cards, 88, 91, CardName.MUSTANG);
         Arrays.fill(cards, 91, 96, CardName.PANIC);
         Arrays.fill(cards, 96, 97, CardName.PEPPERBOX);
         Arrays.fill(cards, 97, 98, CardName.PONY_EXPRESS);
         Arrays.fill(cards, 98, 99, CardName.PUNCH);
         Arrays.fill(cards, 99, 100, CardName.RAG_TIME);
         Arrays.fill(cards, 100, 102, CardName.REMINGTON);
         Arrays.fill(cards, 102, 104, CardName.REV_CARBINE);
         Arrays.fill(cards, 104, 105, CardName.SALOON);
         Arrays.fill(cards, 105, 108, CardName.SCHOFIELD);
         Arrays.fill(cards, 108, 109, CardName.SILVER);
         Arrays.fill(cards, 109, 110, CardName.SOMBRERO);
         Arrays.fill(cards, 110, 111, CardName.SPRINGFIELD);
         Arrays.fill(cards, 111, 113, CardName.STAGECOACH);
         Arrays.fill(cards, 113, 114, CardName.TEN_GALLON_HAT);
         Arrays.fill(cards, 114, 115, CardName.TEQUILA);
         Arrays.fill(cards, 115, 117, CardName.VOLCANIC);
         Arrays.fill(cards, 117, 118, CardName.WELLS_FARGO);
         Arrays.fill(cards, 118, 119, CardName.WHISKY);
         Arrays.fill(cards, 119, 120, CardName.WINCHESTER);*/
         
         Arrays.fill(cards, 0, 60, CardName.BANG);
        Arrays.fill(cards, 60, 90, CardName.MUSTANG);
        Arrays.fill(cards, 90, 120, CardName.SCHOFIELD);
         
         ArrayList<Enum> allCards = new ArrayList<Enum>();
         for(Enum e: cards)
             allCards.add(e);
         while(allCards.size()>0){
             drawPile.add(new Card(allCards.remove((int)(Math.random()*allCards.size()))));
         }
     }
     
     public void sendCardToDiscard(){
         //TODO: Use this function for something, or delete it.
     }
     
     public Card draw() throws Exception{
     	if(drawPile.size()>0)
     		return drawPile.pop();
     	else
     		throw new Exception("The deck is empty.");
     }
     public int size(){
     	return drawPile.size();
     }
 }
