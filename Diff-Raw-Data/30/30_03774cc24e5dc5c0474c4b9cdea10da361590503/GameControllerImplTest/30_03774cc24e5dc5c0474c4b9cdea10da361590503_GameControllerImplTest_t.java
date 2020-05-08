 package com.randombit.uskoci.game;
 
 import com.randombit.uskoci.card.dao.CardDAO;
 import com.randombit.uskoci.card.dao.CardDAOSimple;
 import com.randombit.uskoci.card.model.Card;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.easymock.EasyMock;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 public class GameControllerImplTest {
 
     private static final int STARTING_NUMBER_OF_CARDS = 4;
     GameController gameController;
     CardDAO cardDAO;
     private static final int INITIAL_NUMBER_OF_CARDS_IN_THE_DECK = 60;
 
     int testNumberOfPlayers = 4;
 
     @Before
     public void setUp() throws Exception {
         gameController = new GameControllerImpl(new CardDAOSimple());
         gameController.startGame(testNumberOfPlayers);
     }
 
     @Test
     public void testGetCardsInTheDeck() throws Exception {
         Assert.assertTrue("Deck is not empty", !gameController.getCardsInTheDeck().isEmpty());
     }
 
     //    Na početku igre, promiješa se špil karata (ili špilovi karata)
     //    Svakom igraču se podijeli n karata (za Uskoke (USK) n=4)
     @Test
     public void testCardShuffle() throws Exception {
 
         List<Card> cardsInTheDeck = gameController.getCardsInTheDeck();
         int expectedNumberOfCards = INITIAL_NUMBER_OF_CARDS_IN_THE_DECK - (testNumberOfPlayers * STARTING_NUMBER_OF_CARDS);
         Assert.assertEquals("Remaining number of cards is smaller after dealing", expectedNumberOfCards, cardsInTheDeck.size());
 
         List<Card> allPlayersHands = new ArrayList<Card>();
 
         for (int i = 1; i < testNumberOfPlayers + 1; i++) {
             allPlayersHands.addAll(gameController.getPlayerCards(i));
         }
 
         cardsAreNotDuplicatedDuringShuffling(cardsInTheDeck, allPlayersHands);
 
         gameController.startGame(testNumberOfPlayers);
 
         cardsAreShuffledProperly(cardsInTheDeck, allPlayersHands);
         eachPlayerHasEqualAmountOfCards();
         gameResourcesAreInitialized();
         discardPileIsInitialized();
     }
 
     private void cardsAreNotDuplicatedDuringShuffling(List<Card> cardsInTheDeck, List<Card> allPlayersHands) {
         Assert.assertFalse("Cards in players' hands are not the same as ones in the deck",
                 cardsInTheDeck.containsAll(allPlayersHands));
         Assert.assertFalse("Deck does not contain a card in players hand",
                 cardsInTheDeck.contains(allPlayersHands.get(0)));
     }
 
     private void cardsAreShuffledProperly(List<Card> cardsInTheDeck, List<Card> allPlayersHands) {
         Assert.assertFalse("After restart of the game, different cards are remaining in the deck", cardsInTheDeck.containsAll(gameController.getCardsInTheDeck()));
         Assert.assertFalse("After restart of the game, players have different cards in their hands",
                 allPlayersHands.containsAll(gameController.getPlayerCards(1))
                         && allPlayersHands.containsAll(gameController.getPlayerCards(2))
                         && allPlayersHands.containsAll(gameController.getPlayerCards(3))
                         && allPlayersHands.containsAll(gameController.getPlayerCards(4)));
     }
 
     private void eachPlayerHasEqualAmountOfCards() {
         Assert.assertEquals("Each player is dealt an equal amount of cards",
                 (gameController.getPlayerCards(1).size() == gameController.getPlayerCards(2).size()),
                 (gameController.getPlayerCards(3).size() == gameController.getPlayerCards(4).size()));
     }
 
     private void discardPileIsInitialized() {
         Assert.assertTrue("After restart of the game discard pile is initialized", gameController.getDiscardPile() != Collections.<Card>emptyList());
     }
 
     private void gameResourcesAreInitialized() {
         Assert.assertTrue("After restart of the game resource zones are initialized", gameController.getResources(1) != Collections.<Card>emptyList());
         Assert.assertTrue("After restart of the game resource zones are empty", gameController.getResources(1) != Collections.<Card>emptyList());
         Assert.assertTrue("After restart of the game resource zones are empty", gameController.getResources(2) != Collections.<Card>emptyList());
         Assert.assertTrue("After restart of the game resource zones are empty", gameController.getResources(3) != Collections.<Card>emptyList());
         Assert.assertTrue("After restart of the game resource zones are empty", gameController.getResources(4) != Collections.<Card>emptyList());
     }
 
     //    Početni igrač se odredi nasumično
     @Test
     public void testFirstPlayer() throws Exception {
         int firstPlayer = gameController.getCurrentPlayerId();
 
         Assert.assertTrue("First player is set", firstPlayer > 0);
 
         gameController.startGame(testNumberOfPlayers);
         Assert.assertTrue("First player is re-set", firstPlayer > 0);
     }
 
     // Opcionalno: Na početku poteza vuče se n karata (USK: da, n=1)
     @Test
     public void testDrawCard() throws Exception {
        int testPlayerId = gameController.getCurrentPlayerId();
         Card cardDrawn = gameController.drawCard(testPlayerId);
 
         Assert.assertNotNull("Card has been drawn",
                 cardDrawn);
         Assert.assertFalse("Deck does not contain a card after it has been drawn",
                 gameController.getCardsInTheDeck().contains(cardDrawn));
         Assert.assertTrue("Players hand contains a card after it has been drawn",
                 gameController.getPlayerCards(testPlayerId).contains(cardDrawn));
     }
 
    @Test(expected = ActionNotAllowedException.class)
    public void testPlayerNotOnMoveDrawCard() throws Exception {
        int playerNotOnTheMoveId = gameController.getNextPlayerId();
        Card cardDrawn = gameController.drawCard(playerNotOnTheMoveId);
    }

     //    Igrač klikom na gumb prelazi iz faze u fazu, prelaskom iz završne faze,
 //    započinje potez sljedećeg igrača (USK: 1 faza, završetkom faze, pokreće se pravilo 6.)
 
     @Test
     public void testNextPlayer() throws Exception {
 
         int currentPlayer = gameController.getCurrentPlayerId();
         int expectedPlayer;
 
         if (currentPlayer == 4) {
             expectedPlayer = 1;
         } else {
             expectedPlayer = currentPlayer + 1;
         }
         Assert.assertEquals("When previous player was " + currentPlayer + " next one is " + expectedPlayer, expectedPlayer, gameController.getNextPlayerId());
     }
 
     //    Opcionalno: Igrač na kraju poteza smije imati maksimalno n karata u ruci (USK: da, 5); modifikacija ovog pravila
 //    je „u svakom trenutku“ gdje će se svaki trenutak provjeravati na kraju svake faze/stepa igre (USK: ne)
     @Test
     public void testSetNextPlayersTurn() throws Exception {
         int currentPlayerId = gameController.getCurrentPlayerId();
         gameController.drawCard(currentPlayerId);
 
         int expectedPlayerOnTheMove = gameController.getNextPlayerId();
         gameController.setNextPlayersTurn(currentPlayerId);
 
         Assert.assertEquals("next player is on the move", expectedPlayerOnTheMove, gameController.getCurrentPlayerId());
         Assert.assertFalse("At the beginning of next players turn card has not yet been drawn", gameController.getBeginningCardDrawn());
     }
 
     @Test(expected = ActionNotAllowedException.class)
     public void testSetNextPlayerTurnNoCardDrawn() throws Exception {
         int currentPlayerId = gameController.getCurrentPlayerId();
         gameController.setNextPlayersTurn(currentPlayerId);
     }
 
     @Test(expected = ActionNotAllowedException.class)
     public void testSetNextPlayerTurnPlayerNotOnTheMove() throws Exception {
         int currentPlayerId = gameController.getCurrentPlayerId();
         gameController.drawCard(currentPlayerId);
 
         int playerNotOnTheMove = gameController.getNextPlayerId();
         gameController.setNextPlayersTurn(playerNotOnTheMove);
     }
 
     @Test(expected = ActionNotAllowedException.class)
     public void testNextPlayerButTooMuchCardsInHandAtEndOfTurn() throws Exception {
         // given
         int currentPlayerId = gameController.getCurrentPlayerId();
         gameController.drawCard(currentPlayerId);
         gameController.drawCard(currentPlayerId);
 
         // when
         gameController.setNextPlayersTurn(currentPlayerId);
     }
     
     // Opcionalno: nakon što igrač potroši špil, karte koje se nalaze u groblju se zamiješaju (USK: da)
     @Test
     public void testCardsReshuffle() throws Exception {
         
         List<Card> cardsInTheDeck = gameController.getCardsInTheDeck();
         List<Card> discardedCards;
         List<Card> allPlayersHands = new ArrayList<Card>();
         
        int testPlayerId = gameController.getCurrentPlayerId();
         Card cardDrawn;
         int expectedNumberOfCards = INITIAL_NUMBER_OF_CARDS_IN_THE_DECK - (testNumberOfPlayers * STARTING_NUMBER_OF_CARDS) - 1;
         
         while (cardsInTheDeck.size() != 1) {
             cardDrawn = gameController.drawCard(testPlayerId);
             gameController.discardCardFromPlayersHand(Integer.valueOf(cardDrawn.getId()), testPlayerId);
         }   
         
         cardDrawn = gameController.drawCard(testPlayerId); // Draw last card from the deck.
         discardedCards = gameController.getDiscardPile();
         
         Assert.assertEquals("Number of cards in the deck is smaller then discard pile after reshuffling the pile", expectedNumberOfCards, cardsInTheDeck.size());
         Assert.assertEquals("Discard pile is not empty", 0, discardedCards.size());
         
         
         for (int i = 1; i < testNumberOfPlayers + 1; i++) {
             allPlayersHands.addAll(gameController.getPlayerCards(i));
         }
         
         cardsAreNotDuplicatedDuringShuffling(cardsInTheDeck, allPlayersHands);
         
     }    
 
     /*  5.  Opcionalno: Odigravanje karte – Igrač (opcionalno) mora platiti neke resurse ili se karta vraća u hand.
             Nakon plaćanja, karta iz handa se odigrava licem prema gore tako da ju vide svi igrači.
             Igrači imaju (opcionalno) mogućnost igranja drugih karata (samo određenog tipa, ako su odabrani svi tipovi,
             onda se sve karte mogu igrati „u response“).
             Nakon odigravanja, karat odlazi u određenu zonu (USK: nema cijene; da jedino za eventove vrijedi
             „u response“ i „bilo kada“; eventovi – odbačene karte (discard pile), plijen - flota).
     */
 
     // TODO: event cards go to discard pile
     @Test
     public void testPlayCard() throws Exception {
         int playerOnTheMove = gameController.getCurrentPlayerId();
         Card testCard = gameController.getPlayerCards(playerOnTheMove).get(0);
         String testCardId = testCard.getId();
 
         playerOnTheMovePlaysACard(playerOnTheMove, testCardId);
 
         List<Card> resources = gameController.getResources(playerOnTheMove);
         Assert.assertTrue("Card is in the players resource zone", resources.contains(testCard));
         List<Card> playerCards = gameController.getPlayerCards(playerOnTheMove);
         Assert.assertFalse("Card is no longer in players hand", testCardId.equals(playerCards.get(0).getId()));
     }
 
     private void playerOnTheMovePlaysACard(int playerOnTheMove, String testCardId) throws ActionNotAllowedException {
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
     }
 
     @Test(expected = ActionNotAllowedException.class)
     public void testPlayerNotOnTheMovePlayResourceCard() throws Exception {
         int playerNotOnTheMove = gameController.getNextPlayerId();
         String testCardId = "1";
         Card testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard);
         EasyMock.expect(testCard.getType()).andReturn("resource");
         EasyMock.replay(cardDAO, testCard);
 
         gameController.playCard(playerNotOnTheMove, Integer.valueOf(testCardId));
     }
 
     @Test
     public void testPlayerNotOnTheMovePlayEventCard() throws Exception {
         int playerNotOnTheMove = gameController.getNextPlayerId();
         String testCardId = "1";
         Card testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard);
         EasyMock.expect(testCard.getType()).andReturn("event").times(4);
         EasyMock.expect(testCard.getSummary()).andReturn("");
         EasyMock.replay(cardDAO, testCard);
 
         gameController.playCard(playerNotOnTheMove, Integer.valueOf(testCardId));
 
         Assert.assertFalse("Card is in the players resource zone because it is an event card", gameController.getResources(playerNotOnTheMove).contains(testCard));
     }
 
     // TODO: check if necessary rule
     @Ignore
     @Test(expected = ActionNotAllowedException.class)
     public void testPlayResourceCardTwiceInSameTurn() throws Exception {
         // Given
     	   int playerOnTheMove = gameController.getCurrentPlayerId();
 
         // When
         playerOnTheMovePlaysACard(playerOnTheMove, "1");
         playerOnTheMovePlaysACard(playerOnTheMove, "1");
     }
 
     @Ignore
     @Test
     public void testResourceCardPlayedResetEachTurn() throws Exception {
         // given
         String testCardId = "1";
     	int currentPlayerId = gameController.getCurrentPlayerId();
         playerOnTheMovePlaysACard(currentPlayerId, testCardId);
      
         // When
         gameController.setNextPlayersTurn(currentPlayerId);
 
         Assert.assertFalse("Resource Card played is reset at beginning of new turn", gameController.isResourceCardPlayed());
     }
 
     // TODO: check if necessary rule
     @Ignore
     @Test(expected = ActionNotAllowedException.class)
     public void testPlayCardBeginningCardNotDrawn() throws Exception {
         // Given player is on the move
         int currentPlayer = gameController.getCurrentPlayerId();
         // beginning card has not been drawn
 
         // when he tries to play a card he does not have in his hand exception is thrown
         gameController.playCard(currentPlayer, 1);
 
     }
 
     @Test
     public void testInitialBeginningCardDrawnStatus() throws Exception {
         gameController.startGame(testNumberOfPlayers);
         Assert.assertFalse("At the beginning of the game beginning card has not yet been drawn", gameController.getBeginningCardDrawn());
     }
 
     /*   
 
         15.  Igrači (samo pod utjecajem eventa) mogu manipulirati špilom (tražiti karte, okrenuti gornjih n karata
              (nakon čega se te karte ili vrate u istom redoslijedu ili vrate u promijenjenom (odlučuje vlasnik eventa)
              ili premjeste u neku zonu) (USK: okretanje gornje karte rezultira premještanjem karte u discard pile)
     */
     @Test
     public void testFlipCardFaceUp() {
     	List<Card> discardedCards;
         int playerOnTheMove = gameController.getCurrentPlayerId();
         Card testCard = gameController.flipCardFaceUp();
         Assert.assertFalse("Flipped card cannot be in players resource", gameController.getResources(playerOnTheMove).contains(testCard));
         discardedCards = gameController.getDiscardPile();
         Assert.assertEquals("Flipped card not discarded", 1, discardedCards.size());
 
     }
     /*    Specijalna pravila 1
     Kraj igre se događa kad igrač skupi 25 bodova (nevezano uz tip plijena) igrači imaju pravo odigrati eventove.
     Ukoliko niti jedan od igrača ne odigra kartu događaja tijekom 5 sekundi (prikazan tajmer svim igračima),
     igra završava. Igrač može zaustaviti tajmer da razmisli što bi igrao (maksimalno 15s?).
     Ukoliko nitko ne odigra ništa, igra se završava. Ukoliko netko odigra nešto, karta se resolva
     (vidi općenito pravilo 5) nakon čega se ponovo utvrđuje uvjet kraja igre.
     Ukoliko više igrača skupi 25 bodova u istom trenutku, zajedno pobjeđuju.
     */
     // TODO: ostatak ovog pravila
 
     @Test
     public void testGetPlayersPoints() throws Exception {
         int playerOnTheMove = gameController.getCurrentPlayerId();
         String testCardId = "1";
         Card testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard);
         EasyMock.expect(testCard.getType()).andReturn("resource").times(4);
         EasyMock.expect(testCard.getValue()).andReturn("5").times(2);
         EasyMock.replay(cardDAO, testCard);
 
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
 
         Assert.assertTrue("Card is in the players resource zone because it is an event card", gameController.getResources(playerOnTheMove).contains(testCard));
         int playersPoints = gameController.getPlayersPoints(gameController.getCurrentPlayerId());
         Assert.assertTrue("Players points are increased when he plays an resource card", playersPoints == 5);
     }
     
     /*  Specificno pravilo 3
      Ograničavanje igranja nekih tipova karata : Igrač ne smije odigrati kartu plijena ili eventa 
      ako bi prešao 25 bodova njezinim odigravanjem (ili za event izvršavanjem)
      */
     
     @Test (expected = ActionNotAllowedException.class)    
     public void testMaximumPlayerPoints() throws Exception {
     	int playerOnTheMove = gameController.getCurrentPlayerId();
     	String testCardId = "1";
     	Card testCard;
      
     	testCard = EasyMock.createMock(Card.class);
     	cardDAO = EasyMock.createMock(CardDAO.class);
     	gameController.setCardDAO(cardDAO);
     	EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard).times(3);
     	EasyMock.expect(testCard.getType()).andReturn("resource").times(10);
     	EasyMock.expect(testCard.getValue()).andReturn("9").times(6);
     	EasyMock.replay(cardDAO, testCard);
     
     	gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
     	gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
     	gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
   
     } 
 
     /*  Specificno pravilo 4
     Ograničavanje igranja nekih tipova karata: Nakon što je odigran event, niti jedna druga karta se ne može odigrati (iznimka su karte: „Božja volja“ i „Utvrda nehaj“)
     */
     @Test (expected = ActionNotAllowedException.class) 
     public void testResourceCardPlayedAfterEvent() throws Exception {
         int playerOnTheMove = gameController.getCurrentPlayerId();
         String testCardId = "1";
         Card testCard;
      
         testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard).times(2);
         EasyMock.expect(testCard.getType()).andReturn("event").times(4);
         EasyMock.expect(testCard.getType()).andReturn("resource").times(4);
         EasyMock.expect(testCard.getSummary()).andReturn("").times(2);
         EasyMock.expect(testCard.getValue()).andReturn("1").times(4);
         EasyMock.replay(cardDAO, testCard);
 
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
 
     }
     @Test (expected = ActionNotAllowedException.class) 
     public void testEventCardPlayedAfterEvent() throws Exception {
         int playerOnTheMove = gameController.getCurrentPlayerId();
         String testCardId = "1";
         Card testCard;
      
         testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard).times(2);
         EasyMock.expect(testCard.getType()).andReturn("event").times(8);
         EasyMock.expect(testCard.getValue()).andReturn("1").times(4);
         EasyMock.expect(testCard.getSummary()).andReturn("").times(2);
         EasyMock.replay(cardDAO, testCard);
 
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
 
     }
     @Test  
     public void testEventResponseToEvent() throws Exception{
     	int playerOnTheMove = gameController.getCurrentPlayerId();
         String testCardId = "1";
         Card testCard;
      
         testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard).times(2);
         EasyMock.expect(testCard.getType()).andReturn("event").times(8);
         EasyMock.expect(testCard.getValue()).andReturn("1").times(4);
         EasyMock.expect(testCard.getSummary()).andReturn("");
         EasyMock.expect(testCard.getSummary()).andReturn("Bozja volja");
         EasyMock.replay(cardDAO, testCard);
         
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
         
         Assert.assertTrue("Event response on event not on stack", gameController.getCardStack().contains(testCard));
     }
 
     /*  Specificno pravilo 5
       Karta obavijesti (x2 karte) mogu se odbaciti s flote u discard pile u bilo kojem trenutku
     */
     @Test
     public void testMultiplierRemovedFromPile() throws Exception {
         int playerOnTheMove = gameController.getCurrentPlayerId();
         String testCardId = "1";
         Card testCard;
         List<Card> discardedCards;
      
         testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
 
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard).times(3);
         EasyMock.expect(testCard.getType()).andReturn("multiplier").times(4);
         EasyMock.replay(cardDAO, testCard);
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
         Assert.assertTrue("Multiplier card not in resource pile after playing it.", gameController.getResources(playerOnTheMove).contains(testCard));
         gameController.removeMultiplierFromResourcePile(playerOnTheMove, Integer.valueOf(testCardId));
         discardedCards = gameController.getDiscardPile();
         Assert.assertEquals("Multiplier card is zero", 1, discardedCards.size());
         Assert.assertFalse("Multiplier card not removed from resource pile.", gameController.getResources(playerOnTheMove).contains(testCard));
     }
 }
