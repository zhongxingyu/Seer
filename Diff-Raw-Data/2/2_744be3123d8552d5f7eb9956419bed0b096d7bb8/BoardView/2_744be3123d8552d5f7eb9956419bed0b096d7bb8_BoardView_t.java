 package de.schelklingen2008.canasta.client.view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 import de.schelklingen2008.canasta.client.controller.Controller;
 import de.schelklingen2008.canasta.client.controller.GameChangeListener;
 import de.schelklingen2008.canasta.client.model.GameContext;
 import de.schelklingen2008.canasta.model.Card;
 import de.schelklingen2008.canasta.model.Discard;
 import de.schelklingen2008.canasta.model.GameModel;
 import de.schelklingen2008.canasta.model.Hand;
 import de.schelklingen2008.canasta.model.Player;
 import de.schelklingen2008.canasta.model.Rank;
 import de.schelklingen2008.canasta.model.Talon;
 
 /**
  * Displays the main game interface (the board).
  */
 public class BoardView extends JPanel implements GameChangeListener
 {
 
     private final int  HAND_BORDER  = 70;
     private final int  BOARD_WIDTH  = 800;
     private final int  BOARD_HEIGHT = 800;
 
     private Controller controller;
 
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
         // TODO respond to players mouse movements
     }
 
     private void pressed(MouseEvent e)
     {
         // TODO respond to players mouse clicks
     }
 
     @Override
     public Dimension getPreferredSize()
     {
         // TODO calculate correct dimensions for the board view
         return new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
     }
 
     @Override
     protected void paintComponent(Graphics g)
     {
         Graphics2D gfx = (Graphics2D) g;
         // TODO do proper painting of game state
         paintBackground(gfx);
         paintBoard(gfx);
         paintCards(gfx);
     }
 
     private void paintBackground(Graphics2D gfx)
     {
         gfx.setColor(new Color(0x003300));
         gfx.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
     }
 
     private void paintBoard(Graphics2D gfx)
     {
 
         gfx.setColor(new Color(0x336600));
         gfx.fillRect(HAND_BORDER, HAND_BORDER, BOARD_WIDTH - 2 * HAND_BORDER, BOARD_HEIGHT - 2 * HAND_BORDER);
     }
 
     private void paintCards(Graphics2D gfx)
     {
         paintTalon(gfx);
         paintDiscard(gfx);
         paintHand0(gfx);
         paintHand1(gfx);
         paintHand2(gfx);
     }
 
     private void paintHand0(Graphics2D gfx)
     {
         Player[] players = controller.getGameContext().getGameModel().getPlayers();
 
         Player player = players[0];
 
         Hand hand = player.getHand();
 
         BufferedImage cardImage = getCardImage(null, true);
         final int border = (HAND_BORDER - cardImage.getHeight()) / 2; // (70-57)/2 = 6,5
 
         final int cardCount = hand.size();
         final int hand_space = BOARD_WIDTH - HAND_BORDER - border * 2; // 800-70-13 = 717
 
         double cardSpace = (double) hand_space / (double) cardCount;
 
         cardSpace += (cardSpace - cardImage.getWidth()) / (cardCount - 1);
 
         int i = 0;
 
         for (Card card : hand)
         {
             cardImage = getCardImage(card, false);
 
             int x = border + (int) (i * cardSpace);
             int y = BOARD_HEIGHT - HAND_BORDER + border;
             gfx.drawImage(cardImage, x, y, null);
 
             i++;
         }
 
     }
 
     private void paintHand1(Graphics2D gfx)
     {
         Player[] players = controller.getGameContext().getGameModel().getPlayers();
         Player player = players[1];
         Hand hand = player.getHand();
         BufferedImage cardImage = getCardImage(null, true);
         final int border = (HAND_BORDER - cardImage.getHeight()) / 2;
 
         final int cardCount = hand.size();
         final int hand_space = BOARD_WIDTH - HAND_BORDER - border * 2;
 
         double cardSpace = (double) hand_space / (double) cardCount;
 
         cardSpace += (cardSpace - cardImage.getWidth()) / (cardCount - 1);
 
         gfx.translate((border + cardImage.getHeight()), border);
         gfx.rotate(Math.PI / 2);
         int i = 0;
         for (Card card : hand)
         {
             cardImage = getCardImage(card, true);
 
             // int x = 0;
             // int y = 0;
 
            gfx.drawImage(cardImage, (int) (i * cardSpace), 0, null);
 
             i++;
         }
         gfx.rotate(-Math.PI / 2);
         gfx.translate(-(border + cardImage.getHeight()), -border);
 
     }
 
     private void paintHand2(Graphics2D gfx)
     {
         Player[] players = controller.getGameContext().getGameModel().getPlayers();
         Player player = players[2];
         Hand hand = player.getHand();
         BufferedImage cardImage = getCardImage(null, true);
         final int border = (HAND_BORDER - cardImage.getHeight()) / 2;
 
         final int cardCount = hand.size();
         final int hand_space = BOARD_WIDTH - HAND_BORDER - border * 2;
 
         double cardSpace = (double) hand_space / (double) cardCount;
 
         cardSpace += (cardSpace - cardImage.getWidth()) / (cardCount - 1);
 
         int i = 0;
         for (Card card : hand)
         {
             cardImage = getCardImage(card, true);
 
             int x = BOARD_WIDTH - border - (int) (i * cardSpace) - cardImage.getWidth();
             int y = border;
             gfx.drawImage(cardImage, x, y, null);
 
             i++;
         }
     }
 
     private void paintHand3(Graphics2D gfx)
     {
         // TODO Auto-generated method stub
 
     }
 
     private void paintTalon(Graphics2D gfx)
     {
         Talon talon = controller.getGameContext().getGameModel().getTalon();
         gfx.drawImage(getCardImage(talon.peek(), true), 410, 380, null);
     }
 
     private void paintDiscard(Graphics2D gfx)
     {
         Discard discard = controller.getGameContext().getGameModel().getDiscard();
         gfx.drawImage(getCardImage(discard.peek(), false), 350, 380, null);
     }
 
     private BufferedImage getCardImage(Card card, boolean faceDown)
     {
         String imagePath = "./src/main/resources/cards/";
         String imageName;
         if (faceDown)
         {
             imageName = "back-red-40-2.png";
         }
         else
         {
             if (card.getRank() == Rank.JOKER)
             {
                 imageName = "joker-b" + "-40.png";
             }
             else
             {
                 String rank = card.getRank().toString().toLowerCase();
                 String suit = card.getSuit().toString().toLowerCase();
 
                 imageName = suit + "-" + rank + "-40.png";
             }
         }
         try
         {
             return ImageIO.read(new File(imagePath + imageName));
         }
         catch (IOException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
             return null;
         }
     }
 
     public void gameChanged()
     {
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
 }
