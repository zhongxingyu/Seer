 package model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import model.Card.SUIT;
 import model.Card.VALUE;
 import view.View;
 
 public class Game implements Serializable
 {
     private static final long serialVersionUID = 1L;
 
     private final List<Player> players;
 
     private GameProperties properties;
 
     /* A map from playerID to total game score */
     private final Map<Integer, Integer> playerScores;
 
     /* index of current master (which determines round score) */
     private int masterIndex;
 
     /* state of the game */
     public enum State
     {
         AWAITING_SHOW, AWAITING_FRIEND_CARDS, AWAITING_KITTY, AWAITING_PLAY, AWAITING_RESTART
     }
 
     private State state;
 
     private List<Card> deck;
 
     /* index of current card drawer / current card player */
     private int playerIndex;
 
     /* declared cards */
     private Play shownCards;
 
     /* hidden cards by master */
     private Play kitty;
 
     /* A map from playerID to hand */
     private final Map<Integer, Hand> hands;
 
     /* A map from playerID to team */
     private final Map<Integer, Integer> teams;
 
     /* Selected friend cards with index; used in 'find a friend' version */
     private FriendCards friendCards;
 
     /* A map from playerID to current round score */
     private final Map<Integer, Integer> currentScores;
 
     /* A list of tricks, up to the current (possibly unfinished) */
     private final List<Trick> tricks;
 
     /* Reference to the view */
     private transient View view;
 
     public Game(GameProperties properties)
     {
         this.players = new ArrayList<Player>();
         this.properties = properties;
         this.playerIndex = 0;
         this.playerScores = new HashMap<Integer, Integer>();
         this.masterIndex = 0;
         this.hands = new HashMap<Integer, Hand>();
         this.teams = new HashMap<Integer, Integer>();
         this.friendCards = new FriendCards();
         this.currentScores = new HashMap<Integer, Integer>();
         this.tricks = new ArrayList<Trick>();
     }
 
     public void setView(View view)
     {
         this.view = view;
         view.startGame(this);
     }
 
     public List<Player> getPlayers()
     {
         return new ArrayList<Player>(players);
     }
 
     public int numPlayers()
     {
         return players.size();
     }
 
     public void addPlayer(Player player)
     {
         players.add(player);
         Collections.sort(players);
         playerScores.put(player.ID, 0);
     }
 
     public void addPlayers(List<Player> players)
     {
         for (Player player : players)
             addPlayer(player);
     }
 
     public void removePlayer(Player player)
     {
         players.remove(player);
         playerScores.remove(player.ID);
         currentScores.remove(player.ID);
         if (masterIndex > 0 && masterIndex == players.size())
             masterIndex--;
     }
 
     public GameProperties getProperties()
     {
         return properties;
     }
 
     public Map<Integer, Integer> getPlayerScores()
     {
         return new HashMap<Integer, Integer>(playerScores);
     }
 
     public Player getMaster()
     {
         return players.get(masterIndex);
     }
 
     public Card.VALUE getTrumpValue()
     {
         return Card.values[playerScores.get(players.get(masterIndex).ID)];
     }
 
     public void startRound(long randomSeed)
     {
         /* make deck */
         deck = new ArrayList<Card>();
         int cardID = 101;
         for (int deckNum = 0; deckNum < properties.numDecks; deckNum++)
         {
             for (VALUE value : Card.values)
                 for (SUIT suit : Card.suits)
                     deck.add(new Card(value, suit, cardID++));
             deck.add(new Card(Card.VALUE.SMALL_JOKER, Card.SUIT.TRUMP, cardID++));
             deck.add(new Card(Card.VALUE.BIG_JOKER, Card.SUIT.TRUMP, cardID++));
         }
         Collections.shuffle(deck, new Random(randomSeed));
 
         /* initialize other variables */
         playerIndex = masterIndex;
         shownCards = null;
         state = State.AWAITING_SHOW;
         kitty = null;
         hands.clear();
         teams.clear();
         friendCards.clear();
         currentScores.clear();
         tricks.clear();
         tricks.add(new Trick());
 
         for (Player player : players)
         {
             hands.put(player.ID, new Hand());
             currentScores.put(player.ID, 0);
 
             if (properties.find_a_friend)
             {
                 /* everyone is initially on his/her own team */
                 for (int i = 0; i < players.size(); i++)
                     teams.put(players.get(i).ID, i);
             }
             else
             {
                 /* every other player is on one team */
                 for (int i = 0; i < players.size(); i++)
                     teams.put(players.get(i).ID, i % 2);
             }
         }
 
         view.startRound();
     }
 
     public Player getCurrentPlayer()
     {
         return players.get(playerIndex);
     }
 
     public Player getPlayerWithID(int playerID)
     {
         for (Player player : players)
             if (player.ID == playerID)
                 return player;
 
         return null;
     }
 
     public boolean started()
     {
         return deck != null;
     }
 
     public Game.State getState()
     {
         return state;
     }
 
     public boolean canDrawFromDeck(int playerID)
     {
         return deck.size() > kittySize() && getCurrentPlayer().ID == playerID;
     }
 
     public boolean deckHasCards()
     {
         return deck != null && !deck.isEmpty();
     }
 
     public void drawFromDeck(int playerID)
     {
         Card card = deck.remove(deck.size() - 1);
         hands.get(playerID).addCard(card);
         playerIndex = (playerIndex + 1) % players.size();
         view.drawCard(card, playerID);
     }
 
     public void takeKittyCards()
     {
         /* At some point, give the remaining cards to the master */
         while (!deck.isEmpty())
             hands.get(players.get(masterIndex).ID).addCard(
                     deck.remove(deck.size() - 1));
 
         if (properties.find_a_friend)
         {
             state = State.AWAITING_FRIEND_CARDS;
             view.requestFriendCards(numFriends());
         }
         else
         {
             state = State.AWAITING_KITTY;
             view.notifyCanMakeKitty(kittySize());
         }
     }
 
     public Play getShownCards()
     {
         return shownCards;
     }
 
     public boolean canShowCards(Play cards)
     {
         if (state != State.AWAITING_SHOW)
             return false;
 
         Card firstCard = cards.getCards().get(0);
         for (Card card : cards.getCards())
         {
             if (card.value != getTrumpValue()
                     && card.value != Card.VALUE.BIG_JOKER)
                 return false;
             else if (!card.dataEquals(firstCard))
                 return false;
         }
         if (isShownCardsStrengthening(cards))
             return true;
         if (firstCard.value == Card.VALUE.BIG_JOKER && shownCards != null
                 && cards.numCards() < shownCards.numCards())
             return false;
         else if (firstCard.value != Card.VALUE.BIG_JOKER && shownCards != null
                 && cards.numCards() <= shownCards.numCards())
             return false;
         else
             return true;
     }
 
     public void showCards(Play cards)
     {
         if (isShownCardsStrengthening(cards))
         {
             List<Card> strengthenedCards = new ArrayList<Card>(
                     shownCards.getCards());
             strengthenedCards.addAll(cards.getCards());
             shownCards = new Play(shownCards.getPlayerID(), strengthenedCards);
         }
         else
         {
             returnShownCards();
             shownCards = cards;
         }
         hands.get(cards.getPlayerID()).playCards(cards.getCards());
         view.showCards(cards);
     }
 
     public Card.SUIT getTrumpSuit()
     {
         if (shownCards == null)
             return Card.SUIT.TRUMP;
 
         return shownCards.getPrimarySuit();
     }
 
     public int getTeam(int playerID)
     {
         return teams.containsKey(playerID) ? teams.get(playerID) : -1;
     }
 
     public FriendCards getFriendCards()
     {
         return friendCards;
     }
 
     public boolean canSelectFriendCards(int playerID, FriendCards friendCards)
     {
         return state == State.AWAITING_FRIEND_CARDS && properties.find_a_friend
                 && playerID == players.get(masterIndex).ID
                 && friendCards.size() == numFriends();
     }
 
     public void selectFriendCards(int playerID, FriendCards friendCards)
     {
         state = State.AWAITING_KITTY;
         this.friendCards = friendCards;
         view.selectFriendCards(friendCards);
         view.notifyCanMakeKitty(kittySize());
     }
 
     public Play getKitty()
     {
         return kitty;
     }
 
     public boolean canMakeKitty(Play cards)
     {
         return state == State.AWAITING_KITTY && cards.getPlayerID() == players.get(masterIndex).ID
                 && cards.numCards() == kittySize();
     }
 
     public void makeKitty(Play cards)
     {
         returnShownCards();
         state = State.AWAITING_PLAY;
         kitty = cards;
         hands.get(cards.getPlayerID()).playCards(cards.getCards());
         view.makeKitty(cards);
     }
 
     public Hand getHand(int playerID)
     {
         if (hands.get(playerID) == null)
             return null;
         return hands.get(playerID);
     }
 
     public Map<String, Integer> getTeamScores()
     {
         int masterTeam = teams.get(players.get(masterIndex).ID);
 
         Map<String, Integer> teamScores = new HashMap<String, Integer>();
         for (Player player : players)
             if (teams.get(player.ID) != masterTeam)
             {
                 String team = (friendCards.isEmpty() ? "defenders"
                         : player.name);
                 if (!teamScores.containsKey(team))
                     teamScores.put(team, 0);
                 teamScores.put(team,
                         teamScores.get(team) + currentScores.get(player.ID));
             }
 
         return teamScores;
 
     }
 
     public Trick getCurrentTrick()
     {
         return tricks.get(tricks.size() - 1);
     }
 
     public Trick getPreviousTrick()
     {
         return tricks.size() == 1 ? new Trick() : tricks.get(tricks.size() - 2);
     }
 
     public boolean canPlay(Play play)
     {
         /* Must be current player */
         if (play.getPlayerID() != getCurrentPlayer().ID)
             return false;
 
         /* Must not be awaiting a draw or making the kitty */
         if (state != State.AWAITING_PLAY)
             return false;
 
         Trick currentTrick = tricks.get(tricks.size() - 1);
         if (currentTrick.getPlays().isEmpty())
         {
             /* All cards must be same suit */
             return suit(play) != null;
         }
         else
         {
             /* Must have same number of cards */
             Play startingPlay = currentTrick.getInitialPlay();
             if (play.numCards() != startingPlay.numCards())
                 return false;
 
             /* Must follow along starting suit, if possible */
             Card.SUIT startingSuit = suit(startingPlay);
             List<Card> cards = play.getCards();
             boolean hasAnotherSuit = false;
             for (Card card : cards)
                 if (suit(card) != startingSuit)
                     hasAnotherSuit = true;
             if (hasAnotherSuit)
                 for (Card card : hands.get(play.getPlayerID())
                         .getCardsAfterPlay(cards))
                     if (suit(card) == startingSuit && !cards.contains(card))
                         return false;
 
             /* If starting play has double/triple, must follow if possible */
             int maxFrequency = maxFrequency(startingPlay.getCards());
             if (numCardsNotInFrequency(play.getCards(), maxFrequency) >= maxFrequency)
             {
                 /*
                  * Room for more doubles/triples; check that none are in hand
                  */
                 List<Card> handCards = hands.get(play.getPlayerID()).getCards();
                 for (Card card : handCards)
                     if (suit(card) == startingSuit)
                     {
                         int frequency = card.frequencyIn(handCards);
                         if (frequency > 1 && frequency <= maxFrequency
                                 && !play.getCards().contains(card))
                             return false;
                     }
             }
 
             return true;
         }
     }
 
     public boolean isSpecialPlay(Play play)
     {
         if (play.numCards() == 1)
             return false;
 
         List<int[]> profile = getProfile(play.getCards());
 
         /* perform union find on profile to ensure that all cards are one group */
         int[] groups = new int[play.numCards()];
         for (int i = 0; i < groups.length; i++)
             groups[i] = i;
         for (int[] constraint : profile)
         {
             int group = groups[constraint[1]];
             for (int i = 0; i < groups.length; i++)
                 if (groups[i] == group)
                     groups[i] = groups[constraint[0]];
         }
         for (int group : groups)
             if (group != groups[0])
                 return true;
 
         return false;
     }
 
     public boolean allowedSpecialPlay(Play play)
     {
         Card minCard = minCard(play);
         for (int playerID : hands.keySet())
             if (playerID != play.getPlayerID())
                 for (Card card : hands.get(playerID).getCards())
                    if (cardRank(card) > cardRank(minCard))
                         return false;
 
         return true;
     }
 
     public Card minCard(Play play)
     {
         Card minCard = play.getPrimaryCard();
         for (Card card : play.getCards())
             if (cardRank(card) < cardRank(minCard))
                 minCard = card;
         return minCard;
     }
 
     public void sortCards(List<Card> cards)
     {
         Collections.sort(cards, new Comparator<Card>()
         {
             public int compare(Card card1, Card card2)
             {
                 int score1 = (isTrump(card1) ? 100 : card1.suit.ordinal() * 20)
                         + cardRank(card1);
                 int score2 = (isTrump(card2) ? 100 : card2.suit.ordinal() * 20)
                         + cardRank(card2);
                 /* for big trumps, group by suit */
                 if (score1 == score2 && card1.value == getTrumpValue())
                     return card1.suit.ordinal() - card2.suit.ordinal();
                 return score1 - score2;
             }
         });
     }
 
     public void play(Play play)
     {
         Trick currentTrick = tricks.get(tricks.size() - 1);
         currentTrick.addPlay(play);
         hands.get(play.getPlayerID()).playCards(play.getCards());
         playerIndex = (playerIndex + 1) % players.size();
 
         if (properties.find_a_friend)
             for (Card card : play.getCards())
                 if (friendCards.update(card))
                     teams.put(play.getPlayerID(),
                             teams.get(players.get(masterIndex).ID));
 
         if (currentTrick.numPlays() == players.size())
         {
             /* Finish trick */
             Play winningPlay = winningPlay(currentTrick);
             playerIndex = players.indexOf(getPlayerWithID(winningPlay
                     .getPlayerID()));
             update(currentScores, winningPlay.getPlayerID(),
                     currentTrick.numPoints());
             currentTrick.setWinningPlay(winningPlay);
             tricks.add(new Trick());
             if (canStartNewRound())
                 endRound();
             view.finishTrick(currentTrick, winningPlay.getPlayerID());
         }
 
         view.playCards(play);
     }
 
     public boolean canStartNewRound()
     {
         for (Hand hand : hands.values())
             if (!hand.isEmpty())
                 return false;
 
         return true;
     }
 
     public void endRound()
     {
         state = State.AWAITING_RESTART;
 
         /* Add points from kitty, doubled */
         update(currentScores,
                 getPreviousTrick().getWinningPlay().getPlayerID(),
                 2 * kitty.numPoints());
 
         /* Increment scores of players on winning team */
         int totalScore = 0;
         for (Player player : players)
             if (teams.get(player.ID) == 1)
                 totalScore += currentScores.get(player.ID);
         if (totalScore >= 40 * properties.numDecks)
             incrementPlayerScores(1, 1);
         else
             incrementPlayerScores(0, 1);
 
         view.endRound();
     }
 
     public List<Player> getWinners()
     {
         List<Player> winners = new ArrayList<Player>();
         for (Player player : players)
             if (playerScores.get(player.ID) > Card.VALUE.ACE.ordinal())
                 winners.add(player);
         return winners;
     }
 
     private int numFriends()
     {
         if (properties.find_a_friend)
             return players.size() / 2 - 1;
         else
             return 0;
     }
 
     private int kittySize()
     {
         int totalNumCards = properties.numDecks * 54;
         int kittySize = totalNumCards
                 - Math.round((float) (totalNumCards - 7) / players.size())
                 * players.size();
         return (kittySize <= 4 ? kittySize + players.size() : kittySize);
     }
 
     private void incrementPlayerScores(int winningTeam, int dScore)
     {
         /* Move the master to the next player on the winning team */
         do
         {
             masterIndex = (masterIndex + 1) % players.size();
         }
         while (teams.get(players.get(masterIndex).ID) != winningTeam);
 
         /* Increment scores */
         for (Player player : players)
             if (teams.get(player.ID) == winningTeam)
                 update(playerScores, player.ID, 1);
     }
 
     private boolean isShownCardsStrengthening(Play cards)
     {
         return shownCards != null
                 && shownCards.getPlayerID() == cards.getPlayerID()
                 && shownCards.getPrimaryCard().dataEquals(
                         cards.getPrimaryCard());
     }
 
     private void returnShownCards()
     {
         if (shownCards != null)
             for (Card card : shownCards.getCards())
                 hands.get(shownCards.getPlayerID()).addCard(card);
     }
 
     private void update(Map<Integer, Integer> map, int key, int dValue)
     {
         map.put(key, map.get(key) + dValue);
     }
 
     private Play winningPlay(Trick trick)
     {
         Play startingPlay = trick.getInitialPlay();
         Card.SUIT startingSuit = suit(startingPlay);
         List<int[]> profile = getProfile(startingPlay.getCards());
 
         /*
          * For each play, if it matches the profile, compare against initial
          * play
          */
         Play bestPlay = startingPlay;
         for (Play play : trick.getPlays().subList(1, trick.numPlays()))
             if (suit(play) == startingSuit || suit(play) == Card.SUIT.TRUMP)
             {
                 List<List<Card>> permutations = new ArrayList<List<Card>>();
                 fillPermutations(new ArrayList<Card>(play.getCards()),
                         new ArrayList<Card>(), permutations);
                 for (List<Card> permutation : permutations)
                     if (matchesProfile(permutation, profile)
                             && beats(permutation, bestPlay))
                         bestPlay = play;
             }
 
         return bestPlay;
     }
 
     private Card.SUIT suit(Play play)
     {
         Card.SUIT suit = suit(play.getPrimaryCard());
         for (Card card : play.getCards())
             if (suit(card) != suit)
                 return null;
 
         return suit;
     }
 
     private List<int[]> getProfile(List<Card> cards)
     {
         List<int[]> profile = new ArrayList<int[]>();
         for (int i = 0; i < cards.size(); i++)
         {
             Card card = cards.get(i);
             for (int j = 0; j < i; j++)
             {
                 Card otherCard = cards.get(j);
                 if (card.dataEquals(otherCard))
                     profile.add(new int[]
                     { i, j, 0 });
                 else if (cardRank(card) == cardRank(otherCard) + 1)
                 {
                     /* Check if there are two occurrences of card and otherCard */
                     int cardCount = 0, otherCardCount = 0;
                     for (Card card_ : cards)
                     {
                         if (card_.dataEquals(card))
                             cardCount++;
                         if (card_.dataEquals(otherCard))
                             otherCardCount++;
                     }
                     if (cardCount >= 2 && otherCardCount >= 2)
                     {
                         profile.add(new int[]
                         { i, j, 1 });
                     }
                 }
             }
         }
         return profile;
     }
 
     private void fillPermutations(List<Card> cards, List<Card> current,
             List<List<Card>> permutations)
     {
         if (cards.isEmpty())
             permutations.add(new ArrayList<Card>(current));
 
         for (int i = 0; i < cards.size(); i++)
         {
             Card card = cards.remove(i);
             current.add(card);
             fillPermutations(cards, current, permutations);
             cards.add(i, card);
             current.remove(current.size() - 1);
         }
     }
 
     private boolean matchesProfile(List<Card> cards, List<int[]> profile)
     {
         for (int[] constraint : profile)
         {
             Card card = cards.get(constraint[0]), otherCard = cards
                     .get(constraint[1]);
             if (constraint[2] == 0 && !card.dataEquals(otherCard))
                 return false;
             else if (constraint[2] == 1
                     && !(suit(card) == suit(otherCard) && cardRank(card) == cardRank(otherCard) + 1))
                 return false;
         }
         return true;
     }
 
     private boolean beats(List<Card> cards, Play bestPlay)
     {
         /* A mixture of suits never beats anything */
         if (suit(new Play(-1, cards)) == null)
             return false;
 
         /* Check that each corresponding card is better */
         boolean strictlyBeat = false;
         for (int i = 0; i < cards.size(); i++)
         {
             Card card1 = cards.get(i);
             Card card2 = bestPlay.getCards().get(i);
             int score1 = (isTrump(card1) ? 100 : 0) + cardRank(card1);
             int score2 = (isTrump(card2) ? 100 : 0) + cardRank(card2);
             if (score1 < score2)
                 return false;
             else if (score1 > score2)
                 strictlyBeat = true;
         }
         return strictlyBeat;
     }
 
     private int maxFrequency(List<Card> cards)
     {
         int maxFrequency = 0;
         for (Card card : cards)
             maxFrequency = Math.max(maxFrequency, card.frequencyIn(cards));
         return maxFrequency;
     }
 
     private int numCardsNotInFrequency(List<Card> cards, int frequency)
     {
         List<Card> badCards = new ArrayList<Card>(cards);
         Iterator<Card> it = badCards.iterator();
         while (it.hasNext())
             if (it.next().frequencyIn(cards) >= frequency)
                 it.remove();
         return badCards.size();
     }
 
     private int cardRank(Card card)
     {
         if (card.value == Card.VALUE.BIG_JOKER)
             return 15;
         else if (card.value == Card.VALUE.SMALL_JOKER)
             return 14;
         else if (card.value == getTrumpValue())
             return (card.suit == getTrumpSuit() ? 13 : 12);
         else if (card.value.ordinal() > getTrumpValue().ordinal())
             return card.value.ordinal() - 1;
         else
             return card.value.ordinal();
     }
 
     private Card.SUIT suit(Card card)
     {
         return (isTrump(card) ? Card.SUIT.TRUMP : card.suit);
     }
 
     private boolean isTrump(Card card)
     {
         return card.value == Card.VALUE.BIG_JOKER
                 || card.value == Card.VALUE.SMALL_JOKER
                 || card.value == getTrumpValue() || card.suit == getTrumpSuit();
     }
 }
