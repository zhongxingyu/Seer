 package com.randombit.uskoci.game.control;
 
 import com.randombit.uskoci.card.dao.CardDAO;
 import com.randombit.uskoci.card.dao.CardDAOSimple;
 import com.randombit.uskoci.card.model.Card;
 import com.randombit.uskoci.game.ActionNotAllowedException;
 import com.randombit.uskoci.game.control.eventmessage.Action;
 import com.randombit.uskoci.game.control.eventmessage.Response;
 import org.easymock.EasyMock;
 import org.junit.*;
 import org.junit.rules.ExpectedException;
 
 import java.util.*;
 
 import static com.randombit.uskoci.game.control.GameConstants.*;
 
 public class GameControllerImplTest {
 
     GameController gameController;
     CardDAO cardDAO;
     GameStatus gameStatus;
     int testNumberOfPlayers = 4;
 
     @Rule
     public ExpectedException thrown = ExpectedException.none();
 
     @Before
     public void setUp() throws Exception {
 
         cardDAO = new CardDAOSimple();
         gameController = new GameControllerImpl(cardDAO);
         gameStatus = intializeGameStatus();
         gameController.setGameStatus(gameStatus);
     }
 
     private GameStatus intializeGameStatus() {
         gameStatus = new GameStatus();
         gameStatus.setNumberOfPlayersJoined(testNumberOfPlayers);
         gameStatus.setCardDeck(new ArrayList<Card>(cardDAO.getAllCards()));
         gameStatus.setPlayerCardMap(new HashMap<String, List<Card>>());
         gameStatus.setResourceCardPlayed(false);
         gameStatus.setBeginningCardDrawn(false);
         gameStatus.setGameStarted(true);
         gameStatus.setDiscardedCards(new ArrayList<Card>());
         gameStatus.setPlayersResources(generateEmptyPlayersResources());
         gameStatus.setCardStack(new LinkedList<Card>());
         gameStatus.setCurrentPlayerId(1);
         giveCardsToPlayers(testNumberOfPlayers);
         return gameStatus;
     }
 
     private HashMap<String, ResourcePile> generateEmptyPlayersResources() {
         HashMap<String, ResourcePile> playersResourcesHashMap = new HashMap<String, ResourcePile>();
         for (int playerId = 1; playerId <= gameStatus.getNumberOfPlayersJoined(); playerId++) {
             playersResourcesHashMap.put(String.valueOf(playerId), new ResourcePile(playerId));
         }
         return playersResourcesHashMap;
     }
 
     private void giveCardsToPlayers(int numberOfPlayers) {
         for (int i = 1; i < numberOfPlayers + 1; i++) {
             List<Card> cardsDealtToPlayer = gameStatus.getCardDeck().subList(0, BEGINNING_NUMBER_OF_CARDS);
             gameStatus.getPlayerCardMap().put(String.valueOf(i), new ArrayList<Card>(cardsDealtToPlayer));
             gameStatus.getCardDeck().removeAll(gameStatus.getPlayerCardMap().get(String.valueOf(i)));
         }
     }
 
     @Test
     public void testGetCardsInTheDeck() throws Exception {
         Assert.assertTrue("Deck is not empty", !gameController.getCardsInTheDeck().isEmpty());
     }
 
     //    Na početku igre, promiješa se špil karata (ili špilovi karata)
     //    Svakom igraču se podijeli n karata (za Uskoke (USK) n=4)
     @Test
     public void testCardShuffle() throws Exception {
 
         List<Card> cardsInTheDeck = gameStatus.getCardDeck();
         int expectedNumberOfCards = INITIAL_NUMBER_OF_CARDS_IN_THE_DECK - (gameStatus.getNumberOfPlayersJoined() * BEGINNING_NUMBER_OF_CARDS);
         Assert.assertEquals("Remaining number of cards is smaller after dealing", expectedNumberOfCards, cardsInTheDeck.size());
 
         List<Card> allPlayersHands = new ArrayList<Card>();
 
         for (int i = 1; i < gameStatus.getNumberOfPlayersJoined() + 1; i++) {
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
         int firstPlayer = gameStatus.getCurrentPlayerId();
 
         Assert.assertTrue("First player is set", firstPlayer > 0);
 
         gameController.startGame(gameStatus.getNumberOfPlayersJoined());
         Assert.assertTrue("First player is re-set", firstPlayer > 0);
     }
 
     // Opcionalno: Na početku poteza vuče se n karata (USK: da, n=1)
     @Test
     public void testDrawCard() throws Exception {
         int testPlayerId = gameStatus.getCurrentPlayerId();
         Card cardDrawn = gameController.drawCard(testPlayerId);
 
         Assert.assertNotNull("Card has been drawn",
                 cardDrawn);
         Assert.assertFalse("Deck does not contain a card after it has been drawn",
                 gameController.getCardsInTheDeck().contains(cardDrawn));
         Assert.assertTrue("Players hand contains a card after it has been drawn",
                 gameController.getPlayerCards(testPlayerId).contains(cardDrawn));
     }
 
     @Test
     public void testPlayerNotOnMoveDrawCard() throws Exception {
         thrown.expect(ActionNotAllowedException.class);
         thrown.expectMessage(EXCEPTION_DRAW_CARD_NOT_ON_THE_MOVE);
         int playerNotOnTheMoveId = gameController.getNextPlayerId();
         Card cardDrawn = gameController.drawCard(playerNotOnTheMoveId);
     }
 
     @Test
     public void testPlayerOnMoveDrawMoreThanOneCard() throws Exception {
         thrown.expect(ActionNotAllowedException.class);
         thrown.expectMessage(EXCEPTION_DRAW_MORE_THAN_ONE_CARD);
         int playerOnTheMoveId = gameStatus.getCurrentPlayerId();
         gameStatus.setBeginningCardDrawn(true);
         gameController.drawCard(playerOnTheMoveId);
     }
 
     @Test
     public void testPlayerNotOnTheMOveDrawACardThenPlayerOnTheMoveDrawACard() throws Exception {
         int playerOnTheMove = gameStatus.getCurrentPlayerId();
         int playerNotOnTheMove = gameController.getNextPlayerId();
 
         try {
             gameController.drawCard(playerNotOnTheMove);
         } catch (ActionNotAllowedException e) {
 
         }
         gameController.drawCard(playerOnTheMove);
 
         Assert.assertEquals("Player not on the move does not have the card in his hand", BEGINNING_NUMBER_OF_CARDS, gameController.getPlayerCards(playerNotOnTheMove).size());
     }
 
     //    Igrač klikom na gumb prelazi iz faze u fazu, prelaskom iz završne faze,
 //    započinje potez sljedećeg igrača (USK: 1 faza, završetkom faze, pokreće se pravilo 6.)
 
     @Test
     public void testNextPlayer() throws Exception {
 
         int currentPlayer = gameStatus.getCurrentPlayerId();
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
         gameStatus.setBeginningCardDrawn(true);
 
         int expectedPlayerOnTheMove = gameController.getNextPlayerId();
         gameController.setNextPlayersTurn(currentPlayerId);
 
         Assert.assertEquals("next player is on the move", expectedPlayerOnTheMove, gameController.getCurrentPlayerId());
         Assert.assertFalse("At the beginning of next players turn card has not yet been drawn", gameController.getBeginningCardDrawn());
     }
 
     @Test
     public void testSetNextPlayerTurnNoCardDrawn() throws Exception {
         thrown.expect(ActionNotAllowedException.class);
         thrown.expectMessage(EXCEPTION_NEXT_TURN_NO_BEGINNING_CARD_DRAWN);
         int currentPlayerId = gameStatus.getCurrentPlayerId();
         gameController.setNextPlayersTurn(currentPlayerId);
     }
 
     @Test
     public void testSetNextPlayerTurnPlayerNotOnTheMove() throws Exception {
         thrown.expect(ActionNotAllowedException.class);
         thrown.expectMessage(EXCEPTION_NEXT_PLAYER_BY_PLAYER_NOT_ON_THE_MOVE);
         int currentPlayerId = gameStatus.getCurrentPlayerId();
         gameStatus.setBeginningCardDrawn(true);
         addANumberofCardsToPlayersHand(1, currentPlayerId);
 
         int playerNotOnTheMove = gameController.getNextPlayerId();
         gameController.setNextPlayersTurn(playerNotOnTheMove);
     }
 
     @Test
     public void testNextPlayerButTooMuchCardsInHandAtEndOfTurn() throws Exception {
         thrown.expect(ActionNotAllowedException.class);
         thrown.expectMessage(EXCEPTION_NEXT_PLAYER_TOO_MUCH_CARDS_IN_HAND);
         // given
         int currentPlayerId = gameStatus.getCurrentPlayerId();
         gameStatus.setBeginningCardDrawn(true);
         addANumberofCardsToPlayersHand(2, currentPlayerId);
 
         // when
         gameController.setNextPlayersTurn(currentPlayerId);
     }
 
     private void addANumberofCardsToPlayersHand(int numberOfCardsToAdd, int currentPlayerId) {
         List<Card> playersCards = gameStatus.getPlayerCardMap().get(String.valueOf(currentPlayerId));
         for (int i = 0; i < numberOfCardsToAdd; i++) {
             playersCards.add(new Card());
         }
     }
 
     // Opcionalno: nakon što igrač potroši špil, karte koje se nalaze u groblju se zamiješaju (USK: da)
 
     @Test
     public void testCardsReshuffle() throws Exception {
 
         List<Card> discardedCards;
         List<Card> allPlayersHands = new ArrayList<Card>();
         List<Card> cards = new ArrayList<Card>();
         cards.add(new Card());
 
         int testPlayerId = gameStatus.getCurrentPlayerId();
         int expectedNumberOfCards = INITIAL_NUMBER_OF_CARDS_IN_THE_DECK - 1;
 
         discardedCards = new ArrayList<Card>(gameStatus.getCardDeck());
         gameStatus.setDiscardedCards(discardedCards);
         gameStatus.setCardDeck(cards);
         gameController.drawCard(testPlayerId); // Draw last card from the deck.
 
         Assert.assertTrue("Number of cards in the deck is bigger then discard pile after reshuffling the pile", expectedNumberOfCards > gameStatus.getCardDeck().size());
         Assert.assertEquals("Discard pile is empty", 0, gameStatus.getDiscardedCards().size());
 
         for (int i = 1; i < testNumberOfPlayers + 1; i++) {
             allPlayersHands.addAll(gameController.getPlayerCards(i));
         }
 
         cardsAreNotDuplicatedDuringShuffling(gameStatus.getCardDeck(), allPlayersHands);
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
         gameStatus.setBeginningCardDrawn(true);
         int playerOnTheMove = gameStatus.getCurrentPlayerId();
         Card testCard = gameStatus.getPlayerCardMap().get(String.valueOf(playerOnTheMove)).get(0);
         String testCardId = testCard.getId();
 
         playerPlaysACard(playerOnTheMove, testCardId);
 
         List<Card> resources = gameController.getResources(playerOnTheMove);
         Assert.assertTrue("Card is in the players resource zone", resources.contains(testCard));
         List<Card> playerCards = gameController.getPlayerCards(playerOnTheMove);
         Assert.assertFalse("Card is no longer in players hand", testCardId.equals(playerCards.get(0).getId()));
     }
 
     private void playerPlaysACard(int playerOnTheMove, String testCardId) throws ActionNotAllowedException {
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
     }
 
     @Test
     public void testPlayerNotOnTheMovePlayResourceCard() throws Exception {
         gameStatus.setBeginningCardDrawn(true);
         thrown.expect(ActionNotAllowedException.class);
         thrown.expectMessage(EXCEPTION_PLAYER_NOT_ON_MOVE_PLAYS_RESOURCE);
         int playerNotOnTheMove = gameController.getNextPlayerId();
         String testCardId = "1";
         Card testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard);
         EasyMock.expect(testCard.getType()).andReturn(RESOURCE).times(4);
         EasyMock.expect(testCard.getValue()).andReturn("1").times(2);
         EasyMock.replay(cardDAO, testCard);
 
         gameController.playCard(playerNotOnTheMove, Integer.valueOf(testCardId));
     }
 
     // TODO: enable even if beginning card has not been drawn
     @Ignore
     @Test
     public void testPlayerNotOnTheMovePlayEventCard() throws Exception {
         int playerNotOnTheMove = gameController.getNextPlayerId();
         String testCardId = "1";
         Card testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard);
         EasyMock.expect(testCard.getType()).andReturn(EVENT).times(4);
         EasyMock.expect(testCard.getSummary()).andReturn("");
         EasyMock.replay(cardDAO, testCard);
 
         gameController.playCard(playerNotOnTheMove, Integer.valueOf(testCardId));
 
         Assert.assertFalse("Card is in the players resource zone because it is an event card", gameController.getResources(playerNotOnTheMove).contains(testCard));
     }
 
     @Test
     public void testPlayResourceCardTwiceInSameTurn() throws Exception {
         gameStatus.setResourceCardPlayed(true);
         gameStatus.setBeginningCardDrawn(true);
         thrown.expect(ActionNotAllowedException.class);
         thrown.expectMessage(EXCEPTION_PLAY_RESOURCE_TWICE);
         int playerOnTheMove = gameController.getCurrentPlayerId();
         String testCardId = "1";
         Card testCard;
 
 
         testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard).times(1);
         EasyMock.expect(testCard.getType()).andReturn(RESOURCE).times(4);
         EasyMock.expect(testCard.getValue()).andReturn("1").times(2);
         EasyMock.expect(testCard.getSummary()).andReturn("").times(1);
         EasyMock.replay(cardDAO, testCard);
 
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
     }
 
     @Test
     public void testResourceCardPlayedResetEachTurn() throws Exception {
         int currentPlayerId = gameController.getCurrentPlayerId();
 
        gameStatus.setResourceCardPlayed(true);
        gameStatus.setBeginningCardDrawn(true);
         // When
         gameController.setNextPlayersTurn(currentPlayerId);
         Assert.assertFalse("Resource Card played is reset at beginning of new turn", gameController.isResourceCardPlayed());
     }
 
     @Test
     public void testPlayCardBeginningCardNotDrawn() throws Exception {
         thrown.expect(ActionNotAllowedException.class);
         thrown.expectMessage(EXCEPTION_PLAY_CARD_BEGINNING_CARD_NOT_DRAWN);
         // Given player is on the move
         int currentPlayer = gameController.getCurrentPlayerId();
         // beginning card has not been drawn
 
         gameController.playCard(currentPlayer, 1);
 
     }
 
     @Test
     public void testPlayMultiplier() throws Exception {
         int currentPlayerId = gameController.getCurrentPlayerId();
         ResourcePile playersResourcePile = GetResourcePileAfterPlayerPlaysAMultiplierCard(currentPlayerId);
         int resourcePileValue = playersResourcePile.getValue();
         Assert.assertEquals("Players Resource pile is multiplied by two", 27*2, resourcePileValue);
     }
 
     private ResourcePile GetResourcePileAfterPlayerPlaysAMultiplierCard(int currentPlayerId) throws ActionNotAllowedException {
         String testCardId = "1";
         Card testCard = new Card();
         gameStatus.setResourceCardPlayed(true);
         gameStatus.setBeginningCardDrawn(true);
 
         testCard.setId(testCardId);
         testCard.setType(MULTIPLIER);
         testCard.setSummary(RESOURCE_TYPE.WOOD.toString());
         testCard.setValue("0");
 
         addThreeCardsToPlayersResourcePile(currentPlayerId);
 
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard);
         EasyMock.replay(cardDAO);
 
         gameController.playCard(currentPlayerId, Integer.valueOf(testCardId));
         return gameController.getPlayersResourcePile(currentPlayerId);
     }
 
     @Test
     public void testMultiplierSetFirstInResourcesPile() throws Exception {
         int currentPlayerId = gameController.getCurrentPlayerId();
         ResourcePile playersResourcePile = GetResourcePileAfterPlayerPlaysAMultiplierCard(currentPlayerId);
         List<Card> resourcesList = gameController.getPlayersResourcePile(currentPlayerId).getResourcesList();
         Card firstResource = resourcesList.get(0);
         Assert.assertTrue("Multiplier is first in players Resource pile", firstResource.getType().equals(MULTIPLIER));
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
         int playerOnTheMove = gameStatus.getCurrentPlayerId();
 
         Card testCard = new Card();
         testCard.setSummary("weapon1");
         testCard.setValue("5");
 
         Map<String, ResourcePile> stringResourcePileMap = gameStatus.getPlayersResources();
         ResourcePile resourcePile = new ResourcePile(playerOnTheMove);
         resourcePile.put(testCard);
 
         stringResourcePileMap.put(String.valueOf(playerOnTheMove), resourcePile);
 
         int playersPoints = gameController.getPlayersPoints(gameController.getCurrentPlayerId());
         Assert.assertTrue("Players points are increased when he plays an resource card", playersPoints == 5);
     }
 
     /*  Specificno pravilo 3
     Ograničavanje igranja nekih tipova karata : Igrač ne smije odigrati kartu plijena ili eventa
     ako bi prešao 25 bodova njezinim odigravanjem (ili za event izvršavanjem)
     */
 
     @Test
     public void testMaximumPlayerPoints() throws Exception {
         thrown.expect(ActionNotAllowedException.class);
         thrown.expectMessage(EXCEPTION_PLAY_TOO_MUCH_RESOURCES);
         gameStatus.setBeginningCardDrawn(true);
         int playerOnTheMove = gameController.getCurrentPlayerId();
 
         addThreeCardsToPlayersResourcePile(playerOnTheMove);
 
         gameController.playCard(playerOnTheMove, 1);
    }

    @Test
    public void testPlayerNotOver25PlaysACard() throws Exception {
        int playerOnTheMove = gameController.getCurrentPlayerId();
        int playerNotOnTheMove = gameController.getNextPlayerId();
        gameStatus.setBeginningCardDrawn(true);

        addThreeCardsToPlayersResourcePile(playerNotOnTheMove);
        gameController.playCard(playerOnTheMove, 1);
    }
 
    @Test
    public void testPlayerOver25PlaysNonResource() throws Exception {
        int playerOnTheMove = gameController.getCurrentPlayerId();
        String testCardId = "1";
        gameStatus.setBeginningCardDrawn(true);
        addThreeCardsToPlayersResourcePile(playerOnTheMove);
        cardDAO = EasyMock.createMock(CardDAO.class);
        EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(new Card(testCardId, "", EVENT, "", "", ""));
        EasyMock.replay(cardDAO);
        gameController.setCardDAO(cardDAO);
        gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
     }
 
     private void addThreeCardsToPlayersResourcePile(int playerOnTheMove) {
         Card testCard1 = new Card();
         testCard1.setSummary(RESOURCE_TYPE.WOOD.toString());
         testCard1.setValue("9");
         Card testCard2 = new Card();
         testCard2.setSummary(RESOURCE_TYPE.WOOD.toString());
         testCard2.setValue("9");
         Card testCard3 = new Card();
         testCard3.setSummary(RESOURCE_TYPE.WOOD.toString());
         testCard3.setValue("9");
 
         Map<String, ResourcePile> playersResources = gameStatus.getPlayersResources();
         ResourcePile resourcePile = new ResourcePile(playerOnTheMove);
         resourcePile.put(testCard1);
         resourcePile.put(testCard2);
         resourcePile.put(testCard3);
 
         playersResources.put(String.valueOf(playerOnTheMove), resourcePile);
     }
 
     /*  Specificno pravilo 4
     Ograničavanje igranja nekih tipova karata: Nakon što je odigran event, niti jedna druga karta se ne može odigrati (iznimka su karte: „Božja volja“ i „Utvrda nehaj“)
     */
     @Test
     public void testResourceCardPlayedAfterEvent() throws Exception {
         thrown.expect(ActionNotAllowedException.class);
         thrown.expectMessage(EXCEPTION_RESOURCE_AFTER_EVENT);
         gameStatus.setBeginningCardDrawn(true);
         int playerOnTheMove = gameController.getCurrentPlayerId();
         String testCardId = "1";
         Card testCard;
 
         testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard).times(2);
 //        EasyMock.expect(testCard.getType()).andReturn(GameConstants.EVENT).times(4);
         EasyMock.expect(testCard.getType()).andReturn(RESOURCE).times(4);
         EasyMock.expect(testCard.getSummary()).andReturn("").times(2);
         EasyMock.expect(testCard.getValue()).andReturn("1").times(4);
         EasyMock.replay(cardDAO, testCard);
 
         List<Card> stack = gameStatus.getCardStack();
         stack.add(testCard);
         gameStatus.setCardStack(stack);
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
     }
 
     @Test
     public void testEventCardPlayedAfterEvent() throws Exception {
         thrown.expect(ActionNotAllowedException.class);
         thrown.expectMessage(EXCEPTION_EVENT_AFTER_EVENT);
         gameStatus.setBeginningCardDrawn(true);
         int playerOnTheMove = gameController.getCurrentPlayerId();
         String testCardId = "1";
         Card testCard;
 
         testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard);
         EasyMock.expect(testCard.getType()).andReturn(EVENT).times(4);
         EasyMock.expect(testCard.getValue()).andReturn("1").times(2);
         EasyMock.expect(testCard.getSummary()).andReturn("");
         EasyMock.replay(cardDAO, testCard);
 
         List<Card> stack = gameStatus.getCardStack();
         stack.add(testCard);
         gameStatus.setCardStack(stack);
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
 
     }
 
     @Test
     public void testEventResponseToEvent() throws Exception {
         gameStatus.setBeginningCardDrawn(true);
         int playerOnTheMove = gameController.getCurrentPlayerId();
         String testCardId = "1";
         Card testCard;
 
         testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard).times(2);
         EasyMock.expect(testCard.getType()).andReturn(EVENT).times(8);
         EasyMock.expect(testCard.getValue()).andReturn("1").times(4);
         EasyMock.expect(testCard.getSummary()).andReturn(WILL);
         EasyMock.replay(cardDAO, testCard);
 
         List<Card> stack = gameStatus.getCardStack();
         stack.add(testCard);
         gameStatus.setCardStack(stack);
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
 
         Assert.assertTrue("Event response on event not on stack", gameController.getCardStack().contains(testCard));
     }
 
     @Test
     public void testStackEmptyAfterNextTurn() throws Exception {
         int playerOnTheMove = gameController.getCurrentPlayerId();
 
         LinkedList<Card> cardStack = new LinkedList<Card>();
         cardStack.add(new Card());
         gameStatus.setCardStack(cardStack);
         gameStatus.setBeginningCardDrawn(true);
         gameController.setNextPlayersTurn(playerOnTheMove);
         Assert.assertTrue("Card stack is emptied after next turn set", gameController.getCardStack().isEmpty());
     }
 
     /*  Specificno pravilo 5
       Karta obavijesti (x2 karte) mogu se odbaciti s flote u discard pile u bilo kojem trenutku
     */
     @Test
     public void testMultiplierRemovedFromPile() throws Exception {
         gameStatus.setBeginningCardDrawn(true);
         int playerOnTheMove = gameController.getCurrentPlayerId();
         String testCardId = "1";
         Card testCard;
         List<Card> discardedCards;
 
         testCard = new Card();
         testCard.setType(MULTIPLIER);
         testCard.setSummary(RESOURCE_TYPE.FOOD.toString());
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
 
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard).times(3);
         EasyMock.replay(cardDAO);
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
         Assert.assertTrue("Multiplier card not in resource pile after playing it.", gameController.getResources(playerOnTheMove).contains(testCard));
         gameController.removeMultiplierFromResourcePile(playerOnTheMove, Integer.valueOf(testCardId));
         discardedCards = gameController.getDiscardPile();
         Assert.assertEquals("Multiplier card is added to resource pile", 1, discardedCards.size());
         Assert.assertFalse("Multiplier card not removed from resource pile.", gameController.getResources(playerOnTheMove).contains(testCard));
     }
 
     /* EVENT: Will of God: Cancels any card that has just been played, regardless of the car type. 
     * The cancelled card has no effect and is discarded.
     */
     // TODO : modify test
     @Ignore
     @Test
     public void testEventWillOfGod() throws Exception {
         int playerOnTheMove = gameController.getCurrentPlayerId();
         String testCardId = "1";
         Card testCard;
 
         testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard).times(2);
         EasyMock.expect(testCard.getType()).andReturn(EVENT).times(8);
         EasyMock.expect(testCard.getValue()).andReturn("1").times(4);
         EasyMock.expect(testCard.getSummary()).andReturn("Storm");
         EasyMock.expect(testCard.getSummary()).andReturn("Will of God").times(3);
         EasyMock.replay(cardDAO, testCard);
 
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
         gameController.resolveCardsOnStack();
 
 
         Assert.assertTrue("Event response on event not on stack", gameController.getCardStack().isEmpty());
 
     }
 
     // EVENT: Storm : Destroy all cards and resource piles in play. The cards are discarded.
     // TODO : modify test
     @Ignore
     @Test
     public void testEventStorm() throws Exception {
         int playerOnTheMove = gameController.getCurrentPlayerId();
         String testCardId = "1";
         Card testCard;
 
         testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard).times(5);
         EasyMock.expect(testCard.getType()).andReturn(RESOURCE).times(16);
         EasyMock.expect(testCard.getType()).andReturn(EVENT).times(4);
         EasyMock.expect(testCard.getValue()).andReturn("1").times(12);
         EasyMock.expect(testCard.getSummary()).andReturn("Storm").times(2);
         EasyMock.replay(cardDAO, testCard);
 
         gameController.drawCard(playerOnTheMove);
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
         gameController.setNextPlayersTurn(playerOnTheMove);
         playerOnTheMove = gameController.getCurrentPlayerId();
         gameController.drawCard(playerOnTheMove);
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
         gameController.setNextPlayersTurn(playerOnTheMove);
         playerOnTheMove = gameController.getCurrentPlayerId();
         gameController.drawCard(playerOnTheMove);
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
         gameController.setNextPlayersTurn(playerOnTheMove);
         playerOnTheMove = gameController.getCurrentPlayerId();
         gameController.drawCard(playerOnTheMove);
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
         gameController.resolveCardsOnStack();
 
         Assert.assertTrue("Event on stack", gameController.getCardStack().isEmpty());
         Assert.assertTrue("Resource for player1 not empty", gameController.getResources(1).isEmpty());
         Assert.assertTrue("Resource for player2 not empty", gameController.getResources(2).isEmpty());
         Assert.assertTrue("Resource for player3 not empty", gameController.getResources(3).isEmpty());
         Assert.assertTrue("Resource for player4 not empty", gameController.getResources(4).isEmpty());
 
         Assert.assertFalse("Cards are not in the discard pile", gameController.getDiscardPile().isEmpty());
 
     }
 
     // EVENT: Spyglass: Look at the hands of all players.
     @Test
     public void testEventSpyGlass() throws Exception {
         gameStatus.setBeginningCardDrawn(true);
         int playerOnTheMove = gameStatus.getCurrentPlayerId();
         String testCardId = "1";
         int playerId = 1;
         Card testCard;
         List<Action> listOfActions = new ArrayList<Action>();
         List<Card> cardsInPlayersHand = new ArrayList<Card>();
         testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard).times(2);
         EasyMock.expect(testCard.getType()).andReturn(EVENT).times(4);
         EasyMock.expect(testCard.getValue()).andReturn("1").times(2);
         EasyMock.expect(testCard.getSummary()).andReturn(SPYGLASS).times(2);
         EasyMock.replay(cardDAO, testCard);
 
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
         listOfActions = gameController.responseToEvent(testCard, playerOnTheMove, Collections.<Response>emptyList());
         for (Action action : listOfActions) {
             cardsInPlayersHand = gameController.getPlayerCards(playerId++);
             Assert.assertTrue("Revealed cards are not in player hand", cardsInPlayersHand.containsAll(action.getArea()));
         }
     }
 
     // EVENT: Spy: Look at all the cards in an opponents hand. You may play up to two of them.
     @Test
     public void testEventSpy() throws Exception {
         gameStatus.setBeginningCardDrawn(true);
         int playerOnTheMove = gameStatus.getCurrentPlayerId();
         String testCardId = "1";
         Card testCard, pickedCard;
         Action action;
         List<Action> listOfActions = new ArrayList<Action>();
         List<Response> responseList = new ArrayList<Response>();
         List<Card> listOfCards = new ArrayList<Card>();
         List<Integer> listOfPlayers = new ArrayList<Integer>();
         testCard = EasyMock.createMock(Card.class);
         cardDAO = EasyMock.createMock(CardDAO.class);
         gameController.setCardDAO(cardDAO);
         EasyMock.expect(cardDAO.getCard(Integer.valueOf(testCardId))).andReturn(testCard).times(2);
         EasyMock.expect(testCard.getType()).andReturn(EVENT).times(4);
         EasyMock.expect(testCard.getValue()).andReturn("1").times(2);
         EasyMock.expect(testCard.getSummary()).andReturn(SPY).times(4);
         EasyMock.replay(cardDAO, testCard);
         gameController.playCard(playerOnTheMove, Integer.valueOf(testCardId));
         listOfActions = gameController.responseToEvent(testCard, playerOnTheMove, Collections.<Response>emptyList());
         listOfPlayers.add(2);
         Response resp = new Response(playerOnTheMove, "Players", Collections.<Card>emptyList(), listOfPlayers);
         responseList.add(resp);
         listOfActions = gameController.responseToEvent(testCard, playerOnTheMove, responseList);
         action = listOfActions.remove(0);
         pickedCard = action.getArea().remove(0);
         listOfCards.add(pickedCard);
         resp = new Response(playerOnTheMove, "Cards", listOfCards, listOfPlayers);
         responseList.clear();
         responseList.add(resp);
         listOfActions = gameController.responseToEvent(testCard, playerOnTheMove, responseList);
         action = listOfActions.remove(0);
         Assert.assertFalse("No action", action.getArea().isEmpty());
         Assert.assertTrue("Action play cards is missing", action.getActionType() == "Play cards");
     }
 
     @Test
     public void testGetPlayersResourcesByType() throws Exception {
         Map<String, ResourcePile> playersResources = new HashMap<String, ResourcePile>();
         List<Card> cards = getListOfCardsWithEachTypeOfResource();
 
         int testPlayerId = 1;
         ResourcePile testPlayersResourcePile = new ResourcePile(testPlayerId);
 
         for (Card card:cards) {
             testPlayersResourcePile.put(card);
         }
 
         playersResources.put(String.valueOf(testPlayerId), testPlayersResourcePile);
         gameStatus.setPlayersResources(playersResources);
 
         Assert.assertTrue(gameController.getResources(testPlayerId).containsAll(cards));
         List<Card> expectedWoodResources = gameController.getPlayersResourcesByType(testPlayerId, RESOURCE_TYPE.WOOD.toString());
         Assert.assertTrue("List contains only one item", expectedWoodResources.size() == 1);
         Assert.assertTrue("The list contains wood resources", expectedWoodResources.get(0).getSummary().contains(RESOURCE_TYPE.WOOD.toString()));
 
         List<Card> expectedFoodResources = gameController.getPlayersResourcesByType(testPlayerId, RESOURCE_TYPE.FOOD.toString());
         Assert.assertTrue("List contains only one item", expectedFoodResources.size() == 1);
         Assert.assertTrue("The list contains food resources", expectedFoodResources.get(0).getSummary().contains(RESOURCE_TYPE.FOOD.toString()));
 
         List<Card> expectedMoneyResources = gameController.getPlayersResourcesByType(testPlayerId, RESOURCE_TYPE.MONEY.toString());
         Assert.assertTrue("List contains only one item", expectedMoneyResources.size() == 1);
         Assert.assertTrue("The list contains money resources", expectedMoneyResources.get(0).getSummary().contains(RESOURCE_TYPE.MONEY.toString()));
 
         List<Card> expectedWeaponResources = gameController.getPlayersResourcesByType(testPlayerId, RESOURCE_TYPE.WEAPON.toString());
         Assert.assertTrue("List contains only one item", expectedWeaponResources.size() == 1);
         Assert.assertTrue("The list contains weapon resources", expectedWeaponResources.get(0).getSummary().contains(RESOURCE_TYPE.WEAPON.toString()));
     }
 
     private List<Card> getListOfCardsWithEachTypeOfResource() {
         List<Card> cards = new ArrayList<Card>();
         Card moneyCard = new Card();
         moneyCard.setSummary("money1");
         Card woodCard = new Card();
         woodCard.setSummary("wood1");
         Card weaponCard = new Card();
         weaponCard.setSummary("weapon1");
         Card foodCard = new Card();
         foodCard.setSummary("food1");
         cards.add(moneyCard);
         cards.add(woodCard);
         cards.add(weaponCard);
         cards.add(foodCard);
         return cards;
     }
 }
