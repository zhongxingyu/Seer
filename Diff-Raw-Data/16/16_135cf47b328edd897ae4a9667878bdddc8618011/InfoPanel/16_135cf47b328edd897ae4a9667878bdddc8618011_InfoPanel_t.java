 package dominoes;
 
 import dominoes.Widgets.BoneYardWidget;
 import dominoes.players.DominoPlayer;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * Created with IntelliJ IDEA.
  * User: nick
  * Date: 10/03/13
  * Time: 20:08
  */
 public class InfoPanel extends JPanel {
 
     private final BoneYardWidget boneYardWidget;
     private boolean interactive;
     private TurnCoordinator turnCoordinator;
     private final ScoreBoard scoreBoard;
     private final JButton boneYardButton;
     private final JLabel warningText;
     private Color warningColour = Color.yellow;
     private Color gameWinColour = Color.blue;
     private Color roundWinColour = Color.green;
     private Color normalColor = Color.lightGray;
 
     public InfoPanel(TurnCoordinator turnCoordinator) {
         this.turnCoordinator = turnCoordinator;
         Font font = new Font("Arial", Font.BOLD, 18);
         this.setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         JLabel boneYardLabel = new JLabel(" Boneyard ");
         c.gridx=0;c.gridy=1; c.weightx=0.0;
         this.add(boneYardLabel,c);
         boneYardWidget = new BoneYardWidget();
         c.gridx=1; c.gridheight=3;c.gridy=0;  c.weightx=0.0;
         this.add(boneYardWidget,c);
         boneYardButton = new JButton("Draw");
         boneYardButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (interactive) {
                     buttonDraw();
                 }
             }
         });
         c.gridx=2;c.gridy=1;c.gridheight=1; c.weighty=0.8;
         this.add(boneYardButton,c);
         JPanel textPanel = new JPanel();
         warningText = new JLabel("",JLabel.CENTER);
         warningText.setFont(font);
         textPanel.add(warningText, JPanel.CENTER_ALIGNMENT);
         textPanel.setBackground(Color.lightGray);
         c.weightx=0.8;c.weighty=0.0;c.gridwidth=5; c.gridx=3;c.gridy=0;c.gridheight=2;
         this.add(textPanel,c);
         scoreBoard = new ScoreBoard();
         c.weightx=0.2; c.gridwidth=3;c.gridy=0;c.gridx=8;c.gridheight=3;c.weighty=0.8;
         this.add(scoreBoard,c);
     }
 
     private void buttonDraw() {
         turnCoordinator.drawOrPass();
     }
 
     public void setPlayers(DominoPlayer[] p) {
         this.scoreBoard.setPlayers(p);
     }
 
     public void updateInfoPanel(BoneYard boneYard) {
         this.boneYardWidget.setBoneYard(boneYard);
         this.warningText.setText("");
         if (boneYard.size() == 0) {
             boneYardButton.setText("Pass");
         }
         validate();
     }
 
     public void roundWinner(DominoPlayer dominoPlayer) {
         this.scoreBoard.incrementRound();
         this.warningText.setText(dominoPlayer.getName() + " wins the round!");
         this.warningText.getParent().setBackground(this.roundWinColour);
         JOptionPane.showMessageDialog(new JFrame(), dominoPlayer.getName() + " wins the round!",
                 "Round Over", JOptionPane.INFORMATION_MESSAGE);
         warningText.setText("");
         warningText.getParent().setBackground(this.normalColor);
     }
 
     public void gameWinner(DominoPlayer dominoPlayer) {
         //this.scoreBoard.finalScore();
         this.warningText.setText(dominoPlayer.getName() + " has won the game with " + dominoPlayer.getPoints() + " points!");
         this.warningText.setBackground(this.gameWinColour);
         JOptionPane.showMessageDialog(new JFrame(), dominoPlayer.getName() + " wins the game!",
                 "Game Over", JOptionPane.INFORMATION_MESSAGE);
 
     }
 
     public void invalidMove(DominoPlayer dominoPlayer) {
         this.warningText.setText("Sorry, " + dominoPlayer.getName() + ", that was not a valid move. Please try again.");
         this.warningText.setBackground(this.warningColour);
     }
 
     public boolean mouseUp(Event e, int x, int y) {
         // If click was on bone yard, and we are in a stage of play which allows interaction with the boneyard...
         if (interactive) {
             if (e.target == this.boneYardWidget || e.target == this.boneYardButton) {
                 turnCoordinator.drawOrPass();
             }
         }
         // Container should not see event
         return true;
     }
 
     public void allowBoneYard() {
         this.interactive = true;
     }
 
     public void denyBoneYard() {
         this.interactive = false;
     }
 }
