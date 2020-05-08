 package de.schelklingen2008.canasta.client.view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 import de.schelklingen2008.canasta.client.Constants;
 import de.schelklingen2008.canasta.client.controller.Controller;
 import de.schelklingen2008.canasta.client.controller.GameChangeListener;
 import de.schelklingen2008.canasta.client.model.GameContext;
 import de.schelklingen2008.canasta.model.Card;
 import de.schelklingen2008.canasta.model.CardStack;
 import de.schelklingen2008.canasta.model.Discard;
 import de.schelklingen2008.canasta.model.GameModel;
 import de.schelklingen2008.canasta.model.Hand;
 import de.schelklingen2008.canasta.model.Player;
 import de.schelklingen2008.canasta.model.Rank;
 import de.schelklingen2008.canasta.model.Talon;
 import de.schelklingen2008.util.LoggerFactory;
 
 /**
  * Displays the main game interface (the board).
  */
 public class BoardView extends JPanel implements GameChangeListener
 {
 
     private static final Logger sLogger = LoggerFactory.create();
 
     private Controller          controller;
 
     private List<SensitiveArea> areas   = new ArrayList<SensitiveArea>();
 
     private boolean[]           isCardSelected;
     private boolean             isDiscardSelected;
 
    public boolean isDiscardSelected()
    {
        return isDiscardSelected;
    }

     /**
      * Constructs a view which will initialize itself and prepare to display the game board.
      */
     public BoardView(Controller controller)
     {
         this.controller = controller;
         controller.addChangeListener(this);
 
         addMouseMotionListener(new MouseMotionAdapter()
         {
 
             @Override
             public void mouseMoved(MouseEvent e)
             {
                 moved(e);
             }
         });
 
         addMouseListener(new MouseAdapter()
         {
 
             @Override
             public void mousePressed(MouseEvent e)
             {
                 pressed(e);
             }
         });
     }
 
     private void moved(MouseEvent e)
     {
         // not neccessary
     }
 
     private void pressed(MouseEvent e)
     {
         for (SensitiveArea area : areas)
         {
             if (area.contains(e.getX(), e.getY()))
             {
                 if (area.getName().equals("Talon"))
                 {
                     sLogger.info("pressed Talon");
                     controller.talonClicked();
                 }
                 else if (area.getName().equals("HandCard"))
                 {
                     sLogger.info("pressed HandCard " + area.getAreaNumber());
                     handCardClicked(area.getAreaNumber());
                 }
                 else if (area.getName().equals("CardStack"))
                 {
                     sLogger.info("pressed CardStack " + area.getAreaNumber());
                     controller.cardStackClicked(getSelectedCardNumbers(), area.getAreaNumber());
                 }
                 else if (area.getName().equals("NewOutlay"))
                 {
                     sLogger.info("pressed NewOutlay");
                     controller.makeOutlay(getSelectedCardNumbers(), isDiscardSelected);
                 }
                 else if (area.getName().equals("Discard"))
                 {
                     /**
                      * TODO the discard pile cannot be picked up
                      */
                     sLogger.info("pressed Discard");
                     if (getGameModel().hasDrawn())
                     {
                         controller.discardClicked(getSelectedCardNumbers(), isDiscardSelected);
                     }
                     else
                     {
                         isDiscardSelected = !isDiscardSelected;
                         repaint();
                     }
                     sLogger.info(((Boolean) isDiscardSelected).toString());
                 }
                 else
                 {
                     sLogger.info("pressed unknown area '" + area.getName() + "'");
                 }
             }
         }
     }
 
     @Override
     public Dimension getPreferredSize()
     {
         return new Dimension(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT);
     }
 
     @Override
     protected void paintComponent(Graphics g)
     {
         areas.clear();
 
         Graphics2D gfx = (Graphics2D) g;
         paintBackground(gfx);
         paintBoard(gfx);
         if (getGameModel() == null) return;
         paintCards(gfx);
     }
 
     private void paintBackground(Graphics2D gfx)
     {
         gfx.setColor(new Color(Constants.COLOR_HANDSPACE));
         gfx.fillRect(0, 0, Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT);
     }
 
     private void paintBoard(Graphics2D gfx)
     {
 
         gfx.setColor(new Color(Constants.COLOR_OUTLAY));
         gfx.fillRect(Constants.HAND_BORDER, Constants.HAND_BORDER, Constants.BOARD_WIDTH - 2 * Constants.HAND_BORDER,
                      Constants.BOARD_HEIGHT - 2 * Constants.HAND_BORDER);
     }
 
     private void paintCards(Graphics2D gfx)
     {
         paintTalon(gfx);
         paintDiscard(gfx);
         Player[] players = getGameModel().getPlayers();
 
         int localPlayerNumber = controller.getGameContext().getLocalPlayerNumber();
 
         switch (players.length)
         {
             case 2:
                 paintHandBottom(gfx, localPlayerNumber);
                 paintHandTop(gfx, (localPlayerNumber + 1) % 2);
                 break;
 
             case 4:
                 paintHandBottom(gfx, localPlayerNumber);
                 paintHandRight(gfx, (localPlayerNumber + 1) % 4);
                 paintHandTop(gfx, (localPlayerNumber + 2) % 4);
                 paintHandLeft(gfx, (localPlayerNumber + 3) % 4);
         }
 
         paintOutlays(gfx);
     }
 
     private void paintHandBottom(Graphics2D gfx, int playerNumber)
     {
         Player[] players = getGameModel().getPlayers();
 
         Player player = players[playerNumber];
 
         Hand hand = player.getHand();
 
         BufferedImage cardImage = getCardImage(null, 50, true);
         final int border = (Constants.HAND_BORDER - cardImage.getHeight()) / 2;
 
         final int cardCount = hand.size();
         final int hand_space = Constants.BOARD_WIDTH - Constants.HAND_BORDER - border * 2;
 
         double cardSpace = (double) hand_space / (double) cardCount;
         int maxSpace = cardImage.getWidth() - Constants.MIN_CARD_OVERLAY;
 
         if (cardSpace > maxSpace)
         {
             cardSpace = maxSpace;
         }
 
         cardSpace += (cardSpace - cardImage.getWidth()) / (cardCount - 1);
 
         int i = 0;
 
         for (Card card : hand)
         {
             cardImage = getCardImage(card, 50, false);
 
             int x = border + (int) (i * cardSpace);
             int y = Constants.BOARD_HEIGHT - Constants.HAND_BORDER + border;
             if (isCardSelected[i])
             {
                 y -= 10;
             }
             gfx.drawImage(cardImage, x, y, null);
             areas.add(new SensitiveArea("HandCard", x, y, (int) ((i + 1) * cardSpace) - (int) (i * cardSpace), 71, i));
 
             i++;
         }
 
     }
 
     private void paintHandRight(Graphics2D gfx, int playerNumber)
     {
         Player[] players = getGameModel().getPlayers();
         Player player = players[playerNumber];
         Hand hand = player.getHand();
         BufferedImage cardImage = getCardImage(null, 40, true);
         final int border = (Constants.HAND_BORDER - cardImage.getHeight()) / 2;
 
         final int cardCount = hand.size();
         final int hand_space = Constants.BOARD_WIDTH - Constants.HAND_BORDER - border * 2;
 
         double cardSpace = (double) hand_space / (double) cardCount;
 
         cardSpace += (cardSpace - cardImage.getWidth()) / (cardCount - 1);
         int maxSpace = cardImage.getWidth() - Constants.MIN_CARD_OVERLAY;
 
         if (cardSpace > maxSpace)
         {
             cardSpace = maxSpace;
         }
 
         gfx.translate((border + cardImage.getHeight()), border);
         gfx.rotate(Math.PI / 2);
         int i = 0;
         for (Card card : hand)
         {
             cardImage = getCardImage(card, 40, true);
 
             gfx.drawImage(cardImage, (int) (i * cardSpace), 0, null);
 
             i++;
         }
         gfx.rotate(-Math.PI / 2);
         gfx.translate(-(border + cardImage.getHeight()), -border);
 
     }
 
     private void paintHandTop(Graphics2D gfx, int playerNumber)
     {
         Player[] players = getGameModel().getPlayers();
         Player player = players[playerNumber];
         Hand hand = player.getHand();
         BufferedImage cardImage = getCardImage(null, 40, true);
         final int border = (Constants.HAND_BORDER - cardImage.getHeight()) / 2;
 
         final int cardCount = hand.size();
         final int hand_space = Constants.BOARD_WIDTH - Constants.HAND_BORDER - border * 2;
 
         double cardSpace = (double) hand_space / (double) cardCount;
 
         cardSpace += (cardSpace - cardImage.getWidth()) / (cardCount - 1);
         int maxSpace = cardImage.getWidth() - Constants.MIN_CARD_OVERLAY;
 
         if (cardSpace > maxSpace)
         {
             cardSpace = maxSpace;
         }
 
         int i = 0;
         for (Card card : hand)
         {
             cardImage = getCardImage(card, 40, true);
 
             int x = Constants.BOARD_WIDTH - border - (int) (i * cardSpace) - cardImage.getWidth();
             int y = border;
             gfx.drawImage(cardImage, x, y, null);
 
             i++;
         }
     }
 
     private void paintHandLeft(Graphics2D gfx, int playerNumber)
     {
         Player[] players = getGameModel().getPlayers();
         Player player = players[playerNumber];
         Hand hand = player.getHand();
         BufferedImage cardImage = getCardImage(null, 40, true);
         final int border = (Constants.HAND_BORDER - cardImage.getHeight()) / 2;
 
         final int cardCount = hand.size();
         final int hand_space = Constants.BOARD_WIDTH - Constants.HAND_BORDER - border * 2;
 
         double cardSpace = (double) hand_space / (double) cardCount;
 
         cardSpace += (cardSpace - cardImage.getWidth()) / (cardCount - 1);
         int maxSpace = cardImage.getWidth() - Constants.MIN_CARD_OVERLAY;
 
         if (cardSpace > maxSpace)
         {
             cardSpace = maxSpace;
         }
 
         gfx.translate((Constants.BOARD_WIDTH - border - cardImage.getHeight()), (Constants.BOARD_HEIGHT - border));
         gfx.rotate(-Math.PI / 2);
         int i = 0;
         for (Card card : hand)
         {
             cardImage = getCardImage(card, 40, true);
 
             gfx.drawImage(cardImage, (int) (i * cardSpace), 0, null);
 
             i++;
         }
         gfx.rotate(Math.PI / 2);
         gfx.translate(-(Constants.BOARD_WIDTH - border - cardImage.getHeight()), -(Constants.BOARD_HEIGHT - border));
 
     }
 
     private void paintOutlays(Graphics2D gfx)
     {
         Player[] players = getGameModel().getPlayers();
 
         int outlayWidth;
         int outlayX;
         int outlayHeight;
         int outlayY;
 
         int localPlayerNumber = controller.getGameContext().getLocalPlayerNumber();
 
         switch (players.length)
         {
             case 2:
                 // Player 0
                 outlayWidth = (Constants.BOARD_WIDTH - 2 * Constants.HAND_BORDER - Constants.SHARED_CARDS_SPACE)
                               / 2
                               + Constants.SHARED_CARDS_SPACE;
                 outlayX = Constants.HAND_BORDER;
                 outlayHeight = (Constants.BOARD_HEIGHT - 2 * Constants.HAND_BORDER - Constants.SHARED_CARDS_SPACE) / 2;
                 outlayY = Constants.BOARD_HEIGHT - Constants.HAND_BORDER - outlayHeight;
 
                 paintOutlay(gfx, players[localPlayerNumber], outlayX, outlayY, outlayWidth, outlayHeight);
 
                 // Player 1
                 outlayWidth = (Constants.BOARD_WIDTH - 2 * Constants.HAND_BORDER - Constants.SHARED_CARDS_SPACE)
                               / 2
                               + Constants.SHARED_CARDS_SPACE;
                 outlayX = Constants.HAND_BORDER
                           + (Constants.BOARD_WIDTH - 2 * Constants.HAND_BORDER - Constants.SHARED_CARDS_SPACE)
                           / 2;
                 outlayHeight = (Constants.BOARD_HEIGHT - 2 * Constants.HAND_BORDER - Constants.SHARED_CARDS_SPACE) / 2;
                 outlayY = Constants.HAND_BORDER;
 
                 paintOutlay(gfx, players[(localPlayerNumber + 1) % 2], outlayX, outlayY, outlayWidth, outlayHeight);
 
                 break;
             case 4:
                 // Player 0
                 outlayWidth = (Constants.BOARD_WIDTH - 2 * Constants.HAND_BORDER - Constants.SHARED_CARDS_SPACE)
                               / 2
                               + Constants.SHARED_CARDS_SPACE;
                 outlayX = Constants.HAND_BORDER;
                 outlayHeight = (Constants.BOARD_HEIGHT - 2 * Constants.HAND_BORDER - Constants.SHARED_CARDS_SPACE) / 2;
                 outlayY = Constants.BOARD_HEIGHT - Constants.HAND_BORDER - outlayHeight;
 
                 paintOutlay(gfx, players[localPlayerNumber], outlayX, outlayY, outlayWidth, outlayHeight);
 
                 // Player 1
                 outlayWidth = (Constants.BOARD_WIDTH - 2 * Constants.HAND_BORDER - Constants.SHARED_CARDS_SPACE) / 2;
                 outlayX = Constants.HAND_BORDER;
                 outlayHeight = (Constants.BOARD_HEIGHT - 2 * Constants.HAND_BORDER - Constants.SHARED_CARDS_SPACE)
                                / 2
                                + Constants.SHARED_CARDS_SPACE;
                 outlayY = Constants.HAND_BORDER;
 
                 paintOutlay(gfx, players[(localPlayerNumber + 1) % 4], outlayX, outlayY, outlayWidth, outlayHeight);
 
                 // Player 2
                 outlayWidth = (Constants.BOARD_WIDTH - 2 * Constants.HAND_BORDER - Constants.SHARED_CARDS_SPACE)
                               / 2
                               + Constants.SHARED_CARDS_SPACE;
                 outlayX = Constants.HAND_BORDER
                           + (Constants.BOARD_WIDTH - 2 * Constants.HAND_BORDER - Constants.SHARED_CARDS_SPACE)
                           / 2;
                 outlayHeight = (Constants.BOARD_HEIGHT - 2 * Constants.HAND_BORDER - Constants.SHARED_CARDS_SPACE) / 2;
                 outlayY = Constants.HAND_BORDER;
 
                 paintOutlay(gfx, players[(localPlayerNumber + 2) % 4], outlayX, outlayY, outlayWidth, outlayHeight);
 
                 // Player 3
                 outlayWidth = (Constants.BOARD_WIDTH - 2 * Constants.HAND_BORDER - Constants.SHARED_CARDS_SPACE) / 2;
                 outlayX = Constants.HAND_BORDER
                           + (Constants.BOARD_WIDTH - 2 * Constants.HAND_BORDER - Constants.SHARED_CARDS_SPACE)
                           / 2
                           + Constants.SHARED_CARDS_SPACE;
                 outlayHeight = (Constants.BOARD_HEIGHT - 2 * Constants.HAND_BORDER - Constants.SHARED_CARDS_SPACE)
                                / 2
                                + Constants.SHARED_CARDS_SPACE;
                 outlayY = Constants.BOARD_HEIGHT - Constants.HAND_BORDER - outlayHeight;
 
                 paintOutlay(gfx, players[(localPlayerNumber + 3) % 4], outlayX, outlayY, outlayWidth, outlayHeight);
 
                 break;
             default:
                 throw new RuntimeException(new IllegalStateException(""
                                                                      + players.length
                                                                      + " players not allowed at the moment."));
         }
     }
 
     private void paintOutlay(Graphics2D gfx, Player player, int x, int y, int width, int height)
     {
         // negative values will throw exeptions, redicular small outlay spaces will produce errors!
         if (width <= 0 || height <= 0) throw new IllegalArgumentException("outlay space too small");
 
         // dummy image, we get the card dimensions from this
         BufferedImage cardImage = getCardImage(null, 40, true);
 
         // some pixel calculation
         int stackCountX = width / (int) (3.0 * cardImage.getWidth());
         int stackCountY = height / cardImage.getHeight();
 
         int stackSpaceX = (int) ((width - 3.0 * cardImage.getWidth() * stackCountX) / stackCountX + 1);
         int stackSpaceY = (height - cardImage.getHeight() * stackCountY) / stackCountY + 1;
 
         // paintCardStack(gfx, player.getOutlay().get(0));
 
         // draw the stacks
         int i = 0;
         for (CardStack cardStack : player.getOutlay())
         {
             int whichStackX = i % stackCountX;
             int whichStackY = i / stackCountX;
 
             int translateX = x + (int) ((whichStackX + 1) * stackSpaceX + whichStackX * 3.0 * cardImage.getWidth());
             int translateY = y + (whichStackY + 1) * stackSpaceY + whichStackY * cardImage.getHeight();
 
             for (int i1 = 0; i1 < cardStack.size(); i1++)
             {
                 gfx.drawImage(getCardImage(cardStack.get(i1), 40, false), translateX + i1 * 6 + 20, translateY, null);
             }
 
             areas.add(new SensitiveArea("CardStack",
                                         translateX + (cardStack.size() - 1) * 6,
                                         translateY,
                                         cardStack.size() * 6 + 40,
                                         57,
                                         i));
             // gfx.setPaint(new Color(0xFF00FF));
             // gfx.drawRect(translateX + (cardStack.size() + 5) * 2, translateY, cardStack.size() * 5 + 40,
             // 57);
             // gfx.setFont(Font.)
 
             gfx.setPaint(new Color(0xFFFF00));
             gfx.drawString(((Integer) cardStack.getJokerCount()).toString(), translateX, translateY + 30);
 
             gfx.setPaint(new Color(0xFFFFFF));
             gfx.drawString(((Integer) cardStack.size()).toString(), translateX, translateY + 15);
 
             gfx.setPaint(new Color(0xFFFFFF));
             gfx.drawString(cardStack.getRank().getSmallString(), translateX, translateY + 45);
 
             i++;
 
             /**
              * TODO if the outlay space is too small, missing card stacks should be painted somehow
              */
             if (i > stackCountX * stackCountY)
             {
                 return;
             }
         }
 
         if (getGameContext().getMyPlayer() == player)
         {
             // TODO irgendwas
 
             // areas.add(new SensitiveArea("NewOutlay", x, y, 5, 5));
             // gfx.setPaint(new Color(0xFF00FF));
             // gfx.drawRect(x, y, 5, 5);
 
         }
 
         // some debugging
         // gfx.setPaint(new Color(0xFF0000));
         // gfx.drawRect(x, y, width, height);
         // gfx.drawString(((Integer) stackCountX).toString(), width / 2 - 10, height / 2);
         // gfx.drawString(((Integer) stackCountY).toString(), width / 2 + 10, height / 2);
         // gfx.drawString(((Integer) width).toString(), width / 2 - 30, height / 2 + 20);
         // gfx.drawString(((Integer) height).toString(), width / 2 + 30, height / 2 + 20);
     }
 
     private void paintTalon(Graphics2D gfx)
     {
         Talon talon = getGameModel().getTalon();
         Card topCard = talon.peek();
 
         if (topCard == null)
         {
             gfx.setColor(new Color(0xFFFFFF));
             gfx.drawRoundRect(410, 380, 50, 71, 3, 3);
         }
         else
         {
             gfx.drawImage(getCardImage(topCard, 50, true), 410, 380, null);
         }
         areas.add(new SensitiveArea("Talon", 410, 380, 50, 71));
         gfx.setPaint(new Color(0xFFFFFF));
 
         if (talon.size() > 9)
             gfx.drawString(((Integer) talon.size()).toString(), 427, 370);
         else
             gfx.drawString(((Integer) talon.size()).toString(), 433, 370);
 
     }
 
     private void paintDiscard(Graphics2D gfx)
     {
         Discard discard = getGameModel().getDiscard();
         Card topCard = discard.peek();
 
         if (topCard == null)
         {
             gfx.setColor(new Color(0xFFFFFF));
             gfx.drawRoundRect(350, 380, 50, 71, 3, 3);
         }
         else
         {
             BufferedImage cardImage = getCardImage(topCard, 50, false);
             if (isDiscardSelected)
             {
                 gfx.setColor(new Color(0xFFFF00));
                 gfx.fillRect(345, 375, cardImage.getWidth() + 10, cardImage.getHeight() + 10);
             }
             gfx.drawImage(cardImage, 350, 380, null);
         }
 
         areas.add(new SensitiveArea("Discard", 350, 380, 50, 71));
 
         if (discard.size() > 9)
             gfx.drawString(((Integer) discard.size()).toString(), 367, 370);
         else
             gfx.drawString(((Integer) discard.size()).toString(), 373, 370);
     }
 
     private BufferedImage getCardImage(Card card, int version, boolean faceDown)
     {
         String imagePath = "./src/main/resources/cards/" + version + "/";
         String imageName;
         if (faceDown)
         {
             imageName = "back-red-" + version + "-2.png";
         }
         else
         {
             if (card.getRank() == Rank.JOKER)
             {
                 imageName = "joker-b" + "-" + version + ".png";
             }
             else
             {
                 String rank = card.getRank().toString().toLowerCase();
                 String suit = card.getSuit().toString().toLowerCase();
 
                 imageName = suit + "-" + rank + "-" + version + ".png";
             }
         }
         try
         {
             return ImageIO.read(new File(imagePath + imageName));
         }
         catch (IOException e)
         {
             e.printStackTrace();
             return null;
         }
     }
 
     public void gameChanged()
     {
         isCardSelected = new boolean[getGameContext().getMyPlayer().getHand().size()];
 
         repaint();
     }
 
     public boolean isCardSelected(int cardNumber)
     {
         return isCardSelected[cardNumber];
     }
 
     public int[] getSelectedCardNumbers()
     {
         List<Integer> list = new LinkedList<Integer>();
         for (int i = 0; i < isCardSelected.length; i++)
         {
             if (isCardSelected[i]) list.add(i);
         }
 
         int[] cardNumbers = new int[list.size()];
 
         int i = 0;
         for (int cardNumber : list)
         {
             cardNumbers[i] = cardNumber;
             i++;
         }
 
         return cardNumbers;
     }
 
     public Card[] getSelectedCards()
     {
         List<Card> list = new LinkedList<Card>();
         for (int i = 0; i < isCardSelected.length; i++)
         {
             list.add(controller.getGameContext().getMyPlayer().getHand().get(i));
         }
 
         Card[] cards = new Card[list.size()];
 
         int i = 0;
         for (Card card : list)
         {
             cards[i] = card;
             i++;
         }
 
         return cards;
     }
 
     public void handCardClicked(int cardNumber)
     {
         isCardSelected[cardNumber] = !isCardSelected[cardNumber];
 
         repaint();
     }
 
     private GameModel getGameModel()
     {
         return getGameContext().getGameModel();
     }
 
     private GameContext getGameContext()
     {
         return controller.getGameContext();
     }
 
     private class SensitiveArea
     {
 
         Rectangle r;
         String    name;
         int       areaNumber;
 
         public SensitiveArea(String name, int x, int y, int w, int h)
         {
             this(name, x, y, w, h, 0);
         }
 
         public SensitiveArea(String name, int x, int y, int w, int h, int areaNumber)
         {
             r = new Rectangle(x, y, w, h);
             this.name = name;
             this.areaNumber = areaNumber;
 
             // if (name == "CardStack")
             // {
             // sLogger.info("cardStack " + x + " " + y + " " + w + " " + h);
             // }
         }
 
         public Rectangle getRectangle()
         {
             return r;
         }
 
         public int getAreaNumber()
         {
             return areaNumber;
         }
 
         public boolean contains(int x, int y)
         {
             return r.contains(x, y);
         }
 
         public String getName()
         {
             return name;
         }
     }
 
 }
