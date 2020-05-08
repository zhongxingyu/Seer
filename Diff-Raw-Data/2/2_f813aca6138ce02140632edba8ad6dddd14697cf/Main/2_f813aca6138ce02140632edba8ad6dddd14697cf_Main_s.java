 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Vector;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 public class Main extends JPanel implements ActionListener {
 
     private static final int DEALER_YIELD_AT = 17;
     private static final int DEALER_CARD_INCREMENT_X = 50;
     private static final int DEALER_CARD_INCREMENT_Y = 0;
     private static final int DEALER_CARD_Y = 60;
     private static final int DEALER_CARD_X = 115;
     private static final int PLAYER_CARD_Y = 220;
     private static final int PLAYER_CARD_X = 195;
     private static final int PLAYER_CARD_INCREMENT_Y = -20;
     private static final int PLAYER_CARD_INCREMENT_X = 20;
     private static final int WINDOW_HEIGHT = 368;
     private static final int WINDOW_WIDTH = 470;
     private static final int MENU_HEIGHT = 40;
 
     private Image table = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);
     private Dimension size = new Dimension();
     private Kortlek deck = new Kortlek();
     private JButton drawButton = new JButton();
     private JButton passButton = new JButton();
     private JButton resetButton = new JButton();
     private Vector<Kort> myCards = new Vector<Kort>();
     private Vector<Kort> dealersCards = new Vector<Kort>();
 
     public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Welcome to Black Jack!");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
         frame.add(new Main());
         frame.pack();
         frame.setLocationRelativeTo(null);
         frame.setVisible(true);
     }
 
     public Main() throws IOException {
         size.width = table.getWidth(null);
         size.height = table.getHeight(null);
         setPreferredSize(size);
 
         drawButton.setText("Draw!");
         drawButton.addActionListener(this);
         add(drawButton);
 
         passButton.setText("Pass!");
         passButton.addActionListener(this);
         add(passButton);
 
         resetButton.setText("New game!");
         resetButton.addActionListener(this);
         add(resetButton);
 
         newGame();
     }
 
     private void newGame() throws IOException {
         drawButton.setEnabled(true);
         passButton.setEnabled(true);
 
         table = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);
         table = ImageIO.read(new File("images/table.png"));
 
         deck = new Kortlek();
         deck.shuffle();
 
         myCards.clear();
         dealersCards.clear();
 
         drawCard();
         drawCard();
 
         repaint();
     }
 
     public void paint(Graphics g) {
         super.paint(g);
 
         Graphics2D g2d = (Graphics2D) g;
         g2d.drawImage(table, 0, MENU_HEIGHT, WINDOW_WIDTH, WINDOW_HEIGHT, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT - MENU_HEIGHT, null);
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         try {
             if (e.getSource().equals(drawButton)) {
                 drawCard();
             } else if (e.getSource().equals(passButton)){
                 done();
             } else {
                 newGame();
             }
         } catch (IOException ioe) {
         }
     }
 
     private void done() throws IOException {
         int sum = 0;
 
         while (sum < DEALER_YIELD_AT) {
             Kort draw = deck.draw();
             dealersCards.add(draw);
 
             sum = hand(dealersCards);
 
             BufferedImage cardImage = draw.getCardImage();
             Graphics graphics = table.getGraphics();
             int nextCardX = DEALER_CARD_X + DEALER_CARD_INCREMENT_X * dealersCards.size();
             int nextCardY = DEALER_CARD_Y + DEALER_CARD_INCREMENT_Y * dealersCards.size();;
             graphics.drawImage(cardImage, nextCardX, nextCardY, null);
             graphics.dispose();
 
             repaint();
         }
 
         gameOver();
     }
 
     private void gameOver() {
         int myPoints = hand(myCards);
         int dealerPoints = hand(dealersCards);
 
         String message;
         if (myPoints > 21) {
             message = "You lost!";
         } else if (dealerPoints > 21 || dealerPoints < myPoints){
             message = "You won!";
         } else {
             message = "You lost!";
         }
 
         message += " (" + myPoints + " vs " + dealerPoints + ")";
 
         JOptionPane.showMessageDialog(null, message);
 
         drawButton.setEnabled(false);
         passButton.setEnabled(false);
     }
 
     /**
      * This method will add the value of the cards, treating aces as either 1 or 14,
      * whichever gets the highest sum still below or equal to 21. All dressed cards
      * are considered being worth 10 points
      * @param cards
      * @return the value of the hand of cards
      */
     private int hand(Vector<Kort> cards) {
         int numAces = 0;
         int sum = 0;
 
         for (Kort kort : cards) {
             int value = kort.getValue();
             sum += value;
             if (kort.isAce()) {
                 numAces++;
             }
         }
 
         while(numAces > 0 && sum + 13 <= 21) {
             numAces--;
             sum += 13;
         }
 
         return sum;
     }
 
     private void drawCard() throws IOException {
         Kort draw = deck.draw();
         myCards.add(draw);
 
         BufferedImage cardImage = draw.getCardImage();
         Graphics graphics = table.getGraphics();
         int nextCardX = PLAYER_CARD_X + PLAYER_CARD_INCREMENT_X * myCards.size();
         int nextCardY = PLAYER_CARD_Y + PLAYER_CARD_INCREMENT_Y * myCards.size();
         graphics.drawImage(cardImage, nextCardX, nextCardY, null);
         graphics.dispose();
 
         repaint();
 
         if (hand(myCards) > 21) {
             gameOver();
         }
     }
 }
