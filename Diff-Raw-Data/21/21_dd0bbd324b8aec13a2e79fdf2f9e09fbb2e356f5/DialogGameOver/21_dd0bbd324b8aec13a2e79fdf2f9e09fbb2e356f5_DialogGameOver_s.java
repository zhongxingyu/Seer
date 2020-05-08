 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * User: FeliciousX
  * Date: 5/23/13
  * Time: 8:45 PM
  *
  * DialogGameOver is a JDialog that implements ActionListener to handle the close button
  * after the game is over.
  * It displays Win or Lose depending on the result of the game.
  */
 public class DialogGameOver extends JDialog implements ActionListener {
     public static final int MAX_WIDTH = 400;
     public static final int MAX_HEIGHT = 400;
 
     private Image smiley;
 
     public DialogGameOver(Player player) {
         this.setPreferredSize(new Dimension(MAX_WIDTH, MAX_HEIGHT));
         this.setTitle("Game Over!");
 
         JLabel label = new JLabel();
 
         if (player.isWin()) {
             label.setText("You WIN!");
            smiley = Toolkit.getDefaultToolkit().getImage("src/img/happy.jpg");
         }
         else {
             label.setText("You LOSE");
            smiley = Toolkit.getDefaultToolkit().getImage("src/img/sad.png");
         }
         label.setFont(new Font("Serif", Font.BOLD, 25));
         label.setPreferredSize(new Dimension(MAX_WIDTH, 40));
         label.setHorizontalAlignment(JLabel.CENTER);
         label.setHorizontalTextPosition(JLabel.CENTER);
         JPanel dialogPanel = new JPanel();
         dialogPanel.setLayout(new BorderLayout());
 
 
         FacePanel facePanel = new FacePanel();
         facePanel.repaint();
 
         JButton btnClose = new JButton("Close");
         btnClose.addActionListener(this);
         btnClose.setHorizontalAlignment(JButton.CENTER);
         btnClose.setHorizontalTextPosition(JButton.CENTER);
         btnClose.setPreferredSize(new Dimension(50, 30));
 
         dialogPanel.add(label, BorderLayout.NORTH);
         dialogPanel.add(facePanel, BorderLayout.CENTER);
         dialogPanel.add(btnClose, BorderLayout.SOUTH);
 
         this.add(dialogPanel);
         this.pack();
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         dispose();
     }
 
     private class FacePanel extends JPanel {
         public FacePanel() {
             super();
         }
 
         @Override
         protected void paintComponent(Graphics g) {
             super.paintComponent(g);
 
             Graphics2D g2d = (Graphics2D) g;
             double img_width = smiley.getWidth(this);
             double img_height = smiley.getHeight(this);
             double centerX = img_width / 2;
 
             g2d.translate(MAX_WIDTH / 2 - centerX, MAX_HEIGHT / 2 - img_height);
             g2d.drawImage(smiley, 0, 0, this);
         }
     }
 }
