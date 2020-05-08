 package view;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 import model.Card;
 import model.FriendCards;
 import model.Game;
 import model.GameProperties;
 import model.Play;
 import model.Player;
 import model.Trick;
 
 public class GamePanel extends JPanel
 {
     private static final long serialVersionUID = 7889326310244251698L;
 
     private BufferedImage[][] CARD_IMAGES;
     private BufferedImage BIG_JOKER_IMAGE, SMALL_JOKER_IMAGE;
     private BufferedImage CARD_BACK_IMAGE;
 
     private HumanView view;
     private Game game;
 
     private Map<Card, CardPosition> cardPositions;
     private boolean showPreviousTrick;
 
     public GamePanel(HumanView view)
     {
         setBackground(Color.GREEN);
         this.view = view;
     }
 
     public void loadImages() throws IOException
     {
         CARD_IMAGES = new BufferedImage[13][4];
         for (int i = 0; i < CARD_IMAGES.length; i++)
             for (int j = 0; j < CARD_IMAGES[i].length; j++)
             {
                 String resource = String.format("/images/%c%s.gif",
                         "shdc".charAt(j),
                         "2 3 4 5 6 7 8 9 10 j q k 1".split(" ")[i]);
                 CARD_IMAGES[i][j] = ImageIO.read(getClass().getResource(resource));
             }
         BIG_JOKER_IMAGE = ImageIO.read(getClass().getResource("/images/jr.gif"));
         SMALL_JOKER_IMAGE = ImageIO.read(getClass().getResource("/images/jb.gif"));
         CARD_BACK_IMAGE = ImageIO.read(getClass().getResource("/images/b1fv.gif"));
     }
 
     public void setGame(Game game)
     {
         this.game = game;
         this.cardPositions = new HashMap<Card, CardPosition>();
         for (MouseListener listener : getMouseListeners())
             removeMouseListener(listener);
         addMouseListener(new CardSelectListener());
         addMouseListener(new ShowPreviousTrickListener());
         new Timer().schedule(new TimerTask()
         {
             public void run()
             {
                 synchronized (cardPositions)
                 {
                     for (CardPosition position : cardPositions.values())
                         position.snap();
                 }
                 repaint();
             }
         }, 50, 50);
     }
 
     public List<Card> getSelected()
     {
         List<Card> selectedCards = new ArrayList<Card>();
         synchronized (cardPositions)
         {
             for (Card card : cardPositions.keySet())
                 if (cardPositions.get(card).selected())
                     selectedCards.add(card);
         }
         return selectedCards;
     }
 
     public void resetSelected()
     {
         synchronized (cardPositions)
         {
             for (Card card : cardPositions.keySet())
                 cardPositions.get(card).setSelected(false);
         }
         repaint();
     }
 
     public void moveCardsToDeck()
     {
         cardPositions.clear();
     }
 
     public void moveCardToHand(Card card, int playerID)
     {
         moveCard(card, handLocation(playerID, card), !view.joinedGame()
                 || playerID == view.getPlayerID(), 0.5);
     }
 
     public void moveCardToTable(Card card, int playerID)
     {
         moveCard(card, tableLocation(playerID, card), true, 0.3);
     }
 
     public void moveCardAway(Card card, int playerID)
     {
         moveCard(card, awayLocation(playerID), true, 0.2);
     }
 
     public void showPreviousTrick(boolean flag)
     {
         showPreviousTrick = flag;
     }
 
     public void paintComponent(Graphics g)
     {
         super.paintComponent(g);
 
         synchronized (view)
         {
             if (game == null)
                 return;
 
             drawGameInformation(g);
             drawGameScores(g);
             drawSpecialInformation(g);
 
             if (!game.started())
                 return;
 
             drawRoundScores(g);
 
             /* Draw deck */
             if (game.deckHasCards())
                 drawDeck(g);
             else if (game.getState() == Game.State.AWAITING_PLAY)
                 drawShowPreviousTrickButton(g);
 
             drawCards(g);
         }
     }
 
     private void moveCard(Card card, Point point, boolean faceUp,
             double snapRatio)
     {
         synchronized (cardPositions)
         {
             if (!cardPositions.containsKey(card))
                 cardPositions
                         .put(card, new CardPosition(deckLocation(), false));
             cardPositions.get(card).setDest(point, faceUp, snapRatio);
         }
     }
 
     private void drawGameInformation(Graphics g)
     {
         Font font = new Font("Times New Roman", 0, 14);
         g.setFont(font);
         g.setColor(Color.BLACK);
         FontMetrics fm = g.getFontMetrics();
         int lineDiff = fm.getHeight() + 4;
 
         /* Draw game information */
         int y = 0;
         g.drawString("Trump value: " + game.getTrumpValue(), 10, y += lineDiff);
         g.drawString("Trump suit: "
                 + (game.getTrumpSuit() == Card.SUIT.TRUMP ? '\u2668'
                         : (char) (game.getTrumpSuit().ordinal() + '\u2660')),
                 10, y += lineDiff);
         g.drawString("Starter: " + game.getMaster().name, 10, y += lineDiff);
 
         /* Draw player names */
         List<Player> players = game.getPlayers();
         for (Player player : players)
         {
             double angle = getAngle(player.ID);
             int startX = (int) (450 * (1 + 0.9 * Math.sin(angle)));
             int startY = (int) (350 * (1 + 0.9 * Math.cos(angle)));
             String s = player.name;
             AffineTransform at = new AffineTransform();
             double transformAngle = (Math.cos(angle) < 1e-10 ? Math.PI - angle
                     : -angle);
             at.rotate(transformAngle);
             g.setFont(font.deriveFont(at));
             int width = fm.stringWidth(s);
             if (game.getTeam(player.ID) == game.getTeam(game.getMaster().ID))
                 g.setColor(Color.BLACK);
             else
                 g.setColor(Color.BLUE);
             g.drawString(s, (int) (startX - Math.cos(transformAngle) * width
                     / 2), (int) (startY - Math.sin(transformAngle) * width / 2));
         }
     }
 
     private void drawGameScores(Graphics g)
     {
         g.setFont(new Font("Times New Roman", 0, 14));
         g.setColor(Color.BLACK);
         FontMetrics fm = g.getFontMetrics();
         int lineDiff = fm.getHeight() + 4;
 
         int y = 0;
         String s = "Scores";
         g.drawString(s, 890 - fm.stringWidth(s), y += lineDiff);
         Map<Integer, Integer> playerScores = game.getPlayerScores();
         for (int playerID : playerScores.keySet())
         {
             s = game.getPlayerWithID(playerID).name + ": "
                     + Card.VALUE.values()[playerScores.get(playerID)];
             g.drawString(s, 890 - fm.stringWidth(s), y += lineDiff);
         }
     }
 
     private void drawRoundScores(Graphics g)
     {
         g.setFont(new Font("Times New Roman", 0, 14));
         g.setColor(Color.BLUE);
         FontMetrics fm = g.getFontMetrics();
         int lineDiff = fm.getHeight() + 4;
 
         Map<String, Integer> teamScores = game.getTeamScores();
         int y = 640 - lineDiff * teamScores.size();
         for (String teamName : teamScores.keySet())
         {
             String s = teamName + ": " + teamScores.get(teamName);
             g.drawString(s, 10, y += lineDiff);
         }
     }
 
     private void drawSpecialInformation(Graphics g)
     {
         g.setFont(new Font("Times New Roman", 0, 14));
         g.setColor(Color.BLACK);
         FontMetrics fm = g.getFontMetrics();
         int lineDiff = fm.getHeight() + 4;
 
         GameProperties properties = game.getProperties();
         FriendCards friendCards = game.getFriendCards();
         int y = 640 - lineDiff
                 * (1 + (properties.find_a_friend ? 1 : 0) + friendCards.size());
         String s = properties.numDecks + " decks";
         g.drawString(s, 890 - fm.stringWidth(s), y += lineDiff);
         if (properties.find_a_friend)
         {
             s = "Find a friend!"
                     + (friendCards.isEmpty() ? "" : " Looking for:");
             g.drawString(s, 890 - fm.stringWidth(s), y += lineDiff);
         }
         Map<Card, Integer> friendCardsMap = friendCards.getFriendCards();
         for (Card card : friendCardsMap.keySet())
         {
             int index = friendCardsMap.get(card);
             String indexStr;
             if (index == 1)
                 indexStr = "next";
             else if (index == 2)
                 indexStr = "second";
             else if (index == 3)
                 indexStr = "third";
             else
                 indexStr = index + "th";
             s = "the "
                     + indexStr
                     + " "
                     + card.value.toString().toLowerCase().replace("_", " ")
                     + (card.value == Card.VALUE.SMALL_JOKER
                             || card.value == Card.VALUE.BIG_JOKER ? "" : " of "
                             + card.suit.toString().toLowerCase() + "s");
             g.drawString(s, 890 - fm.stringWidth(s), y += lineDiff);
         }
     }
 
     private void drawDeck(Graphics g)
     {
         g.drawImage(CARD_BACK_IMAGE, 415, 300, null);
     }
 
     private void drawShowPreviousTrickButton(Graphics g)
     {
         // TODO make fancier button.
         g.setFont(new Font("Times New Roman", 0, 14));
         g.setColor(Color.BLACK);
         FontMetrics fm = g.getFontMetrics();
         String s = "Show prev";
         g.drawString(s, 450 - fm.stringWidth(s) / 2, 350 - fm.getHeight());
     }
 
     private void drawCards(Graphics g)
     {
         synchronized (cardPositions)
         {
             Set<Card> denotedCards = new HashSet<Card>();
             for (Player player : game.getPlayers())
             {
                 for (Card card : memoizeSortedHandCards(player.ID))
                 {
                     moveCardToHand(card, player.ID);
                     denotedCards.add(card);
                 }
                 for (Card card : memoizeTableCards(player.ID))
                 {
                     moveCardToTable(card, player.ID);
                     denotedCards.add(card);
                 }
             }
             Trick[] tricks =
             { game.getCurrentTrick(), game.getPreviousTrick() };
             for (Trick trick : tricks)
                 if (trick.getWinningPlay() != null)
                     for (Play play : trick.getPlays())
                         for (Card card : play.getCards())
                             if (!denotedCards.contains(card))
                             {
                                 moveCardAway(card, trick.getWinningPlay()
                                         .getPlayerID());
                                 denotedCards.add(card);
                             }
             List<Card> cardsToDraw = new ArrayList<Card>(denotedCards);
             Collections.sort(cardsToDraw, new Comparator<Card>()
             {
                 public int compare(Card card1, Card card2)
                 {
                     CardPosition position1 = cardPositions.get(card1);
                     CardPosition position2 = cardPositions.get(card2);
                     int score1 = position1.currX() + 5 * position1.currY();
                     int score2 = position2.currX() + 5 * position2.currY();
                     return score1 - score2;
                 }
             });
 
             for (Card card : cardsToDraw)
                 drawCard(card, g);
         }
     }
 
     private Point deckLocation()
     {
         return new Point(415, 300);
     }
 
     private Point handLocation(int playerID, Card card)
     {
         double angle = getAngle(playerID);
         int startX = (int) (450 * (1 + 0.7 * Math.sin(angle)));
         int startY = (int) (350 * (1 + 0.7 * Math.cos(angle)));
 
         List<Card> cards = memoizeSortedHandCards(playerID);
         int cardIndex = cards.indexOf(card);
         int cardDiff = (!view.joinedGame() || playerID == view.getPlayerID()) ? 14
                 : 9;
         return new Point((int) (startX + cardDiff * Math.cos(angle)
                 * (cardIndex - cards.size() / 2.0) - 35),
                 (int) (startY - cardDiff * Math.sin(angle)
                         * (cardIndex - cards.size() / 2.0) - 48));
     }
 
     private Point tableLocation(int playerID, Card card)
     {
         double angle = getAngle(playerID);
         int startX = (int) (450 * (1 + 0.4 * Math.sin(angle)));
         int startY = (int) (350 * (1 + 0.4 * Math.cos(angle)));
 
         List<Card> cards = memoizeTableCards(playerID);
         int cardIndex = cards.indexOf(card);
         return new Point((int) (startX + 24 * Math.cos(angle)
                 * (cardIndex - cards.size() / 2.0) - 35), (int) (startY - 24
                 * Math.sin(angle) * (cardIndex - cards.size() / 2.0) - 48));
     }
 
     private Point awayLocation(int playerID)
     {
         double angle = getAngle(playerID);
         int startX = (int) (450 * (1 + 2 * Math.sin(angle)));
         int startY = (int) (350 * (1 + 2 * Math.cos(angle)));
         return new Point(startX, startY);
     }
 
     private double getAngle(int playerID)
     {
         List<Player> players = game.getPlayers();
         int index = players.indexOf(game.getPlayerWithID(playerID));
         if (view.joinedGame())
             index -= players.indexOf(game.getPlayerWithID(view.getPlayerID()));
         return 2 * Math.PI / players.size() * index;
     }
 
     private void drawCard(Card card, Graphics g)
     {
         CardPosition position = cardPositions.get(card);
         BufferedImage image;
         if (!position.faceUp())
             image = CARD_BACK_IMAGE;
         else if (card.value == Card.VALUE.BIG_JOKER)
             image = BIG_JOKER_IMAGE;
         else if (card.value == Card.VALUE.SMALL_JOKER)
             image = SMALL_JOKER_IMAGE;
         else
             image = CARD_IMAGES[card.value.ordinal()][card.suit.ordinal()];
         int y = position.selected() ? position.currY() - 20 : position.currY();
         g.drawImage(image, position.currX(), y, null);
     }
 
     private Map<Integer, List<Card>> sortedHandCards = new HashMap<Integer, List<Card>>();
     private Map<Integer, List<Card>> prevHandCards = new HashMap<Integer, List<Card>>();
 
     private List<Card> memoizeSortedHandCards(int playerID)
     {
         List<Card> cards = game.getHand(playerID).getCards();
         if (!cards.equals(prevHandCards.get(playerID)))
         {
             List<Card> sortedCards = new ArrayList<Card>(cards);
             game.sortCards(sortedCards);
             sortedHandCards.put(playerID, sortedCards);
             prevHandCards.put(playerID, cards);
         }
         return sortedHandCards.get(playerID);
     }
 
     private Map<Integer, List<Card>> tableCards = new HashMap<Integer, List<Card>>();
     private Map<Integer, List<Card>> prevTableCards = new HashMap<Integer, List<Card>>();
 
     private List<Card> memoizeTableCards(int playerID)
     {
         List<Card> cards = Collections.emptyList();
         if (game.getState() == Game.State.AWAITING_PLAY)
         {
             Play play = (showPreviousTrick ? game.getPreviousTrick() : game
                     .getCurrentTrick()).getPlayByID(playerID);
             if (play != null)
                 cards = play.getCards();
         }
         else if (game.getState() == Game.State.AWAITING_RESTART
                 && game.getKitty().getPlayerID() == playerID)
             cards = game.getKitty().getCards();
         else if (game.getShownCards() != null
                 && game.getShownCards().getPlayerID() == playerID)
             cards = game.getShownCards().getCards();
         if (!cards.equals(prevTableCards.get(playerID)))
         {
             List<Card> sortedCards = new ArrayList<Card>(cards);
             game.sortCards(sortedCards);
 
             tableCards.put(playerID, sortedCards);
             prevTableCards.put(playerID, cards);
         }
         return tableCards.get(playerID);
     }
 
     private class CardSelectListener extends MouseAdapter
     {
         @Override
         public void mouseClicked(MouseEvent e)
         {
             if (!view.joinedGame())
                 return;
 
             List<Card> cards = new ArrayList<Card>(
                     memoizeSortedHandCards(view.getPlayerID()));
             Collections.reverse(cards);
             for (Card card : cards)
             {
                 CardPosition position = cardPositions.get(card);
                 if (e.getX() >= position.currX()
                         && e.getX() < position.currX() + 71
                         && e.getY() >= position.currY()
                         && e.getY() < position.currY() + 96)
                 {
                     position.setSelected(!position.selected());
                     break;
                 }
             }
             repaint();
         }
     }
 
     private class ShowPreviousTrickListener extends MouseAdapter
     {
         @Override
         public void mousePressed(MouseEvent e)
         {
             if (Math.hypot(450 - e.getX(), 350 - e.getY()) < 50)
                 showPreviousTrick = true;
             repaint();
         }
 
         @Override
         public void mouseReleased(MouseEvent e)
         {
             showPreviousTrick = false;
             repaint();
         }
     }
 }
