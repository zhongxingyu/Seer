 package game.tournament;
 
 import com.biotools.meerkat.GameObserver;
 import game.*;
 import game.deck.DeckFactory;
 import game.stats.BankrollObserver;
 import game.tournament.blinds.Level;
 import org.apache.log4j.Logger;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * GameRunner running a Tournament<br>
  */
 public class TournamentGameRunner implements GameRunner {
     private static final Logger log = Logger.getLogger(TournmentTableSeater.class);
 
     private TournamentGameDescription gameDescription;
     private Map<PublicGameInfo, Dealer> dealers;
     private int totalHands = 0;
 
     public TournamentGameRunner(TournamentGameDescription gameDescription) {
         this.gameDescription = gameDescription;
     }
 
     @Override
     public void runGame(DeckFactory deckFactory, TableSeater tableSeater, GameIDGenerator gameIDGenerator, List<? extends GameObserver> gameObservers) {
         PublicGameInfo gameInfos[] = initTables(deckFactory, tableSeater, gameObservers);
 
         while (gameInfos[0].getNumPlayers() > 1) {
             totalHands++;
             log.debug("------------ Hand: " + totalHands);
             Level blindsLevel = gameDescription.getBlindsStructure().getCurrentLevel(totalHands);
             for (PublicGameInfo gameInfo : gameInfos) {
                 runHand(gameIDGenerator, gameInfo, blindsLevel);
             }
             gameInfos = ((TournmentTableSeater) tableSeater).rearrangeTables(gameInfos);
         }
     }
 
     private PublicGameInfo[] initTables(DeckFactory deckFactory, TableSeater tableSeater, List<? extends GameObserver> gameObservers) {
         PublicGameInfo gameInfos[] = tableSeater.createTables(gameDescription);
 
         dealers = new HashMap<PublicGameInfo, Dealer>(gameInfos.length, 1);
 
         for (PublicGameInfo gameInfo : gameInfos) {
             for (GameObserver gameObserver : gameObservers) {
                 gameInfo.addGameObserver(gameObserver);
             }
             gameInfo.setLimit(gameDescription.isNolimit() ? PublicGameInfo.NO_LIMIT : PublicGameInfo.FIXED_LIMIT);
             Dealer dealer = new Dealer(deckFactory.createDeck(), gameInfo);
 
             dealers.put(gameInfo, dealer);
         }
 
         return gameInfos;
     }
 
     private void runHand(GameIDGenerator gameIDGenerator, PublicGameInfo gameInfo, Level blindsLevel) {
         gameInfo.setBlinds(blindsLevel.getSmallBlindAmount(), blindsLevel.getBigBlindAmount());
         gameInfo.setGameID(gameIDGenerator.getNextGameID());
 
         Dealer dealer = dealers.get(gameInfo);
         dealer.playHand();
 
         removeSittingOutPlayers(gameInfo);
        dealer.moveButton();
     }
 
     private void removeSittingOutPlayers(PublicGameInfo gameInfo) {
         for (int seat = 0; seat < gameInfo.getNumSeats(); seat++) {
             PublicPlayerInfo player = gameInfo.getPlayer(seat);
             if (player != null && player.isSittingOut()) {
                 gameInfo.removePlayer(player);
             }
         }
     }
 
     @Override
     public PublicGameInfo asyncRunGame(DeckFactory deckFactory, TableSeater tableSeater, GameIDGenerator gameIDGenerator, List<? extends GameObserver> gameObservers) {
         return null;
     }
 
     @Override
     public void addBankrollObserver(BankrollObserver bankrollgraph) {
         // TODO
     }
 }
