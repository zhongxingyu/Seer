 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import model.*;
 import org.springframework.boot.SpringApplication;
 import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.RestController;
 import util.IdGenerator;
 
 //TODO: limit player adding
 //TODO: return different status if it's not player's turn
 
 @RestController
 @EnableAutoConfiguration
 public class DealerController {
 	
 	private final static int STARTING_CHIPS = 1000;
     private final static int MINIMUM_WAGER = 10;
 	
 	private Deck deck;
 	private Hand dealerHand;
 	private Map<String, Player> players;
     private Card dealerUpCard;
     private List<Card> revealedCards;
     private int deckNum = 0;
 	private int playerCount = 0;
     private final int NUM_ROUNDS;
     private int NUM_PLAYERS;
     private int round = 0;
     private boolean done = false;
     private int currentPosition = 0;
     private int dealRequests = 0;
     private IdGenerator idGen;
 
     private final Lock dealerLock;
 
     public DealerController() {
        this(1000, 2);
     }
 	public DealerController(int numRounds, int numPlayers) {
         deck = new Deck();
 		players = new HashMap<String, Player>();
         revealedCards = new ArrayList<Card>();
 		dealerLock = new ReentrantLock();
         idGen = new IdGenerator();
         NUM_ROUNDS = numRounds;
         NUM_PLAYERS = numPlayers;
 	}
 	
 	//creates new player and gives them 1000 chips
 	//returns id of player for use in requests
     @RequestMapping("/addPlayer")
 	public String addPlayer(@RequestParam(value = "name", required = true) String name) {
 
 		dealerLock.lock();
         Player newPlayer = new Player();
         newPlayer.setPosition(playerCount);
         playerCount++;
         String playerId = idGen.nextId();
         newPlayer.setId(playerId);
         newPlayer.setName(name);
         newPlayer.giveChips(STARTING_CHIPS);
         players.put(playerId, newPlayer);
         System.out.println("added player with id: " + playerId);
 
         dealerLock.unlock();
 		return playerId;
 	}
 
     //deals cards in standard order
     //returns a list of hands and dealer up card
     @RequestMapping("/start")
 	public ResponseEntity<StartInfo> start(@RequestParam(value = "playerId", required = true) String playerId, @RequestParam(value = "wager", required = true) int wager) {
         if (playerCount < NUM_PLAYERS) {
             System.out.println("waiting for players to join");
             return new ResponseEntity<StartInfo>(HttpStatus.FORBIDDEN);
         }
         //playerId error check
         dealerLock.lock();
         ResponseEntity<StartInfo> resp;
         Player thisPlayer = players.get(playerId);
 
         if (thisPlayer == null) {
             System.out.println("invalid player id");
             resp = new ResponseEntity<StartInfo>(HttpStatus.BAD_REQUEST);
         }
         else if (thisPlayer.getPosition() != currentPosition || thisPlayer.isActive()) {
             System.out.println("player must wait his/her turn");
             resp = new ResponseEntity<StartInfo>(HttpStatus.FORBIDDEN);
         }
         //wager error check
         else if (wager < MINIMUM_WAGER || wager > thisPlayer.getStack()) {
             System.err.println("invalid wager amount for " + thisPlayer.getName() + ": " + wager);
             resp = new ResponseEntity<StartInfo>(HttpStatus.BAD_REQUEST);
         }
         //see if game is over
         else if (round == NUM_ROUNDS) {
             if (!done) {
                 System.out.println("GAME OVER!");
                 done = true;
             }
             resp = new ResponseEntity<StartInfo>(HttpStatus.BAD_REQUEST);
         }
         else {
             System.out.println("deal requested: setting " + thisPlayer.getName() + "'s wager to " + wager);
             dealRequests++;
             thisPlayer.setCurrentWager(wager);
             thisPlayer.setActive(true);
 
             //if this is the first request for a deal, deal cards to begin new round
             if (dealRequests == 1) {
                 dealCards();
             }
             StartInfo startInfo = new StartInfo();
             startInfo.setYourHand(thisPlayer.getHand());
             startInfo.setDealerUpCard(dealerUpCard);
             resp = new ResponseEntity<StartInfo>(startInfo, HttpStatus.OK);
         }
 
         dealerLock.unlock();
         return resp;
 //        return new ResponseEntity<StartInfo>(startInfo, HttpStatus.OK);
     }
 
     @RequestMapping("/hit")
     public ResponseEntity<Card> hit(@RequestParam(value = "playerId", required = true) String playerId) {
         dealerLock.lock();
         ResponseEntity<Card> resp;
         Player thisPlayer = players.get(playerId);
 
         if (thisPlayer == null) {
             System.out.println("invalid player id");
             resp = new ResponseEntity<Card>(HttpStatus.BAD_REQUEST);
         }
         else if (thisPlayer.getPosition() != currentPosition || !thisPlayer.isActive()) {
             System.out.println("player must wait his/her turn");
             resp = new ResponseEntity<Card>(HttpStatus.FORBIDDEN);
         }
         else {
             checkDeck();
             Card newCard = deck.drawCard();
             revealedCards.add(newCard);
             thisPlayer.giveCard(newCard);
             System.out.println(thisPlayer.getName() + " hits: " + newCard.toString());
 
             if (thisPlayer.getHand().getValue() > 21) {
                 thisPlayer.setActive(false);
                 currentPosition++;
                 System.out.println("moving on to position " + currentPosition);
                 if (currentPosition == NUM_PLAYERS) {
                     System.out.println("all players done, dealer's turn");
                     determineDealerHand();
                 }
             }
             resp = new ResponseEntity<Card>(newCard, HttpStatus.OK);
         }
 
         dealerLock.unlock();
         return resp;
     }
 
     @RequestMapping("/doubleDown")
 	public ResponseEntity<Card> doubleDown(@RequestParam(value = "playerId", required = true) String playerId) {
 		dealerLock.lock();
         ResponseEntity<Card> resp;
 		Player thisPlayer = players.get(playerId);
         if (thisPlayer == null) {
             System.out.println("invalid player id");
             resp = new ResponseEntity<Card>(HttpStatus.BAD_REQUEST);
         }
         else if (thisPlayer.getPosition() != currentPosition || !thisPlayer.isActive()) {
             System.out.println("player must wait his/her turn");
             resp = new ResponseEntity<Card>(HttpStatus.FORBIDDEN);
         }
         else if (thisPlayer.getHand().getCards().size() > 2) {
             System.out.println("can't double down after hitting");
             resp = new ResponseEntity<Card>(HttpStatus.FORBIDDEN);
         }
         else {
             checkDeck();
             Card newCard = deck.drawCard();
             revealedCards.add(newCard);
             thisPlayer.giveCard(newCard);
             System.out.println(thisPlayer.getName() + " doubled down: " + newCard.toString());
             thisPlayer.setCurrentWager(thisPlayer.getCurrentWager() * 2);
             thisPlayer.setActive(false);
             currentPosition++;
             System.out.println("moving on to position " + currentPosition);
             if (currentPosition == NUM_PLAYERS) {
                 System.out.println("all players done, dealer's turn");
                 determineDealerHand();
             }
             resp = new ResponseEntity<Card>(newCard, HttpStatus.OK);
         }
 
         dealerLock.unlock();
         return resp;
 
 
 	}
 
     @RequestMapping("/stand")
     public ResponseEntity stand(@RequestParam(value = "playerId", required = true) String playerId) {
         dealerLock.lock();
         ResponseEntity resp;
         Player thisPlayer = players.get(playerId);
         if (thisPlayer == null) {
             System.out.println("invalid player id");
             resp = new ResponseEntity<Card>(HttpStatus.BAD_REQUEST);
         }
        else if (thisPlayer.getPosition() != currentPosition) {
             System.out.println("player must wait his/her turn");
             resp = new ResponseEntity<Card>(HttpStatus.FORBIDDEN);
         }
        else if (!thisPlayer.isActive()) {
            resp = new ResponseEntity(HttpStatus.OK);
        }
         else {
             thisPlayer.setActive(false);
             System.out.println(thisPlayer.getName() + " stands");
             currentPosition++;
             System.out.println("moving on to position " + currentPosition);
             if (currentPosition == NUM_PLAYERS) {
                 System.out.println("all players done, dealer's turn");
                 determineDealerHand();
             }
             resp = new ResponseEntity(HttpStatus.OK);
         }
         dealerLock.unlock();
         return resp;
 
     }
 
     @RequestMapping("/getRevealedCards")
     public Map<String, Object> getRevealedCards() {
         Map<String, Object> resp = new HashMap<String, Object>();
         resp.put("deckNumber", deckNum);
         resp.put("revealedCards", revealedCards);
         return resp;
     }
 
     @RequestMapping("/getStack")
     public ResponseEntity<Integer> getStack(@RequestParam(value = "playerId", required = true) String playerId) {
         Player thisPlayer = players.get(playerId);
         if (thisPlayer == null) {
             System.out.println("invalid player id");
             return new ResponseEntity<Integer>(HttpStatus.BAD_REQUEST);
         }
         else {
             return new ResponseEntity<Integer>(thisPlayer.getStack(), HttpStatus.OK);
         }
     }
 
     @RequestMapping("/done")
     public boolean done() {
         return done;
     }
 
     private void dealCards() {
         System.out.println("discarding all hands");
         System.out.println("round " + round);
         for (Player player : players.values()) {
             player.discardHand();
         }
         dealerHand = new Hand();
         //deal cards in standard order and update players map
         for (int i = 0; i < 2; i++) {
             for (Player player : players.values()) {
                 checkDeck();
                 Card newCard = deck.drawCard();
                 revealedCards.add(newCard);
                 player.giveCard(newCard);
                 players.put(player.getId(), player);
                 System.out.println("dealing " + newCard.toString() + " to " + player.getName());
             }
             Card dealerCard = deck.drawCard();
             dealerHand.addCard(dealerCard);
             System.out.println("dealing " + dealerCard.toString() + " to dealer");
 
             if (i == 1) {
                 revealedCards.add(dealerCard);
                 dealerUpCard = dealerCard;
             }
         }
     }
 
     private void determineDealerHand() {
         //reveal the dealer down card
         Card dealerDownCard = dealerHand.getCards().get(0);
         System.out.println("revealing dealer down card: " + dealerDownCard.toString());
         revealedCards.add(dealerDownCard);
         if (dealerHand.isSoft()) {
             while(dealerHand.getValue() < 8) {
                 checkDeck();
                 Card newCard = deck.drawCard();
                 System.out.println("dealer hits: " + newCard.toString());
                 revealedCards.add(newCard);
                 dealerHand.addCard(newCard);
             }
         }
         else {
             while (dealerHand.getValue() < 17) {
                 checkDeck();
                 Card newCard = deck.drawCard();
                 System.out.println("dealer hits: " + newCard.toString());
                 revealedCards.add(newCard);
                 dealerHand.addCard(newCard);
             }
 
         }
 
         int dealer = dealerHand.getValue();
         //if dealer stays with a soft hand, make sure higher value is used
         if (dealerHand.isSoft()) {
             dealer += 10;
         }
         System.out.println("dealer hand value: " + dealer);
         //dealer busts, everyone currently in the round wins
         if (dealer > 21) {
             System.out.println("dealer busted");
             for (Player p : players.values()) {
                 if (p.getHand().getValue() > 21) {
                     System.out.println(p.getName() + " busted and loses " + p.getCurrentWager() + " chips");
                     p.takeChips(p.getCurrentWager());
                 }
                 else {
                     System.out.println(p.getName() + " is awarded " + p.getCurrentWager() + " chips");
                     p.giveChips(p.getCurrentWager());
                 }
                 p.setCurrentWager(0);
             }
         }
         else {
             //go through each player and award chips accordingly
             //note: player wager is not subtracted from stack at time of bet
             for (Player p : players.values()) {
                 Hand playerHand = p.getHand();
                 int handValue = playerHand.getValue();
                 int wager = p.getCurrentWager();
                 if (playerHand.isSoft()) {
                     handValue += 10;
                 }
                 //blackjack
                 if (handValue > 21) {
                     System.out.println(p.getName() + " busted and loses " + wager + " chips");
                     p.takeChips(wager);
                 }
                 else if (handValue == 21 && playerHand.getCards().size() == 2) {
                     System.out.println(p.getName() + " got a blackjack and is awarded " +
                                        (int)Math.ceil((double)wager * 1.5) + " chips");
                     p.giveChips((int) Math.ceil((double) wager * 1.5));
                 }
                 //player hand beats dealer
                 else if (handValue > dealer) {
                     System.out.println(p.getName() + " beat dealer with a " + handValue +
                                        " and is awarded " + wager + " chips");
                     p.giveChips(wager);
                 }
                 //push
                 else if (handValue == dealer) {
                     System.out.println(p.getName() + " pushes");
                 }
                 else {
                     System.out.println(p.getName() + " lost to dealer and loses " + wager + " chips");
                     p.takeChips(wager);
                 }
                 p.setCurrentWager(0);
            }
        }
        //check for broke players and remove them
        synchronized (dealerLock) {
            for (Player p : players.values()) {
                 if (p.getStack() < MINIMUM_WAGER) {
                     System.out.println(p.getName() + " ran out of chips");
                     int playerPosition = p.getPosition();
                     players.remove(p.getId());
                     for (Player pl : players.values()) {
                         int position = pl.getPosition();
                         if (position > playerPosition) {
                             pl.setPosition(position - 1);
                             players.put(pl.getId(), pl);
                         }
                     }
                     NUM_PLAYERS--;
                 }
             }
         }
         printTotals();
         round++;
         dealRequests = 0;
         currentPosition = 0;
     }
 
     private void checkDeck() {
         if (deck.cardsLeft() < 5) {
             System.out.println("shuffling deck");
             deck = new Deck();
             revealedCards.clear();
             deckNum++;
         }
     }
 
     private void printTotals() {
         System.out.println("****************** chip totals ******************\n");
         for (Player p : players.values()) {
             System.out.println("\t" + p.getName() + ": " + p.getStack());
         }
         System.out.println("");
     }
 
     public static void main(String[] args) throws Exception {
         SpringApplication.run(DealerController.class, args);
     }
 	
 	
 }
