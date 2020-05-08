 package dominoes;
 
 import dominoes.players.ComputerPlayer;
 import dominoes.players.DominoPlayer;
 import dominoes.players.Player;
 import dominoes.players.PlayerType;
 
 import javax.swing.*;
 import javax.swing.border.EtchedBorder;
 import java.awt.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: nick
  * Date: 16/02/13
  * Time: 22:57
  */
 
 public class UI extends JPanel implements DominoUI, TurnCoordinator {
     PlayerHandPanel player1Hand;
     PlayerHandPanel player2Hand;
     InfoPanel infoPanel;
     TablePanel tableArea;
     UIFrame parent;
     int boneSize = 120;
     int maxpips = 6;  //graphics output currently can not cope with higher than 6
 
     private PlayerType player1Type = PlayerType.None;
     private PlayerType player2Type = PlayerType.None;
     private int targetScore = 50;
     private Player player1;
     private Player player2;
     private PlayWrapperCubbyHole nextMove;
     private Bone nextMoveBone;
     private final Dominoes dominoesGame;
     private final Thread dominoesThread;
 
     private Player createPlayer(PlayerType type, String name) {
         if (type == PlayerType.Computer) {
             return new ComputerPlayer(name, this);
         } else {
             return new Player(name, this);
         }
     }
 
     public UI(PlayerType player1Type, String player1Name, PlayerType player2Type, String player2Name, int targetScore,UIFrame parent) {
         super();
         this.player1 = this.createPlayer(player1Type, player1Name);
         this.player2 = this.createPlayer(player2Type, player2Name);
         this.targetScore = targetScore;
         this.player1Type = player1Type;
         this.player2Type = player2Type;
         this.parent = parent;
         this.setSize(parent.getWidth(), parent.getHeight());
 
         this.dominoesGame = new Dominoes(this, this.player1, this.player2, this.targetScore, this.maxpips);
         this.dominoesThread = new Thread(new DominoesThread(this.dominoesGame, this));
 
         EtchedBorder eb1 = new EtchedBorder(EtchedBorder.RAISED);
         this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
         infoPanel = new InfoPanel(this);
         setupScorePanel(infoPanel, eb1);
         player1Hand = new PlayerHandPanel(player1Type, this);
         setupPlayerHand(player1Hand, eb1);
 
         tableArea = new TablePanel(this);
         setupTableArea(eb1);
 
         player2Hand = new PlayerHandPanel(player2Type, this);
         player2Hand.setMinimumSize(new Dimension(parent.getWidth(), 200));
         setupPlayerHand(player2Hand, eb1);
 
         this.validate();
 
         this.dominoesThread.start();
     }
 
     //region sets and gets
     public void setPlayer1Type(PlayerType type) {
         this.player1Type = type;
         this.player1Hand.setPlayerType(type);
     }
 
     public void setPlayer2Type(PlayerType type) {
         this.player2Type = type;
         this.player2Hand.setPlayerType(type);
     }
 
     public PlayerType getPlayer1Type() {
         return this.player1Type;
     }
 
     public PlayerType getPlayer2Type() {
         return this.player2Type;
     }
 
     public void setTargetScore(int targetScore) {
         this.targetScore = targetScore;
     }
 
     public int getTargetScore() {
         return targetScore;
     }
 
     public int getMaxpips() {
         return maxpips;
     }
 
     public void setPlayer1(Player player1) {
         this.player1 = player1;
     }
 
     public Player getPlayer1() {
         return player1;
     }
 
     public void setPlayer2(Player player2) {
         this.player2 = player2;
     }
 
     public Player getPlayer2() {
         return player2;
     }
 
     private void setDominoPlayers(DominoPlayer[] dominoPlayers) {
         player1Hand.setPlayer(dominoPlayers[0]);
         player2Hand.setPlayer(dominoPlayers[1]);
         infoPanel.setPlayers(dominoPlayers);
     }
     //endregion
 
     //region setups
     private void setupTableArea(EtchedBorder eb1) {
         tableArea.setBackground(Color.orange);
         tableArea.setBorder(eb1);
         tableArea.validate();
         add(tableArea);
     }
 
     private void setupPlayerHand(PlayerHandPanel panel, EtchedBorder eb1) {
         panel.setBackground(Color.lightGray);
         panel.setBorder(eb1);
         add(panel);
     }
 
     private void setupScorePanel(InfoPanel panel, EtchedBorder eb1) {
         panel.setBackground(Color.lightGray);
         panel.setBorder(eb1);
         add(panel);
     }
     //endregion
 
     // DominoUI implementation
     public void display(DominoPlayer[] dominoPlayers, Table table, BoneYard boneYard) {
         this.tableArea.setTable(table);
         this.setDominoPlayers(dominoPlayers);
         this.infoPanel.updateInfoPanel(boneYard);
     }
 
     public void displayRoundWinner(DominoPlayer dominoPlayer) {
         if (dominoPlayer == null) {
            //draw round condition
             this.infoPanel.roundWinner(dominoPlayer);
         }  else {
             // Check if target score has been met, if yes, then it's a game win, if not, round win
             if (dominoPlayer.getPoints() >= this.targetScore) {
                 this.infoPanel.gameWinner(dominoPlayer);
             } else {
                 this.infoPanel.roundWinner(dominoPlayer);
             }
         }
     }
 
     public void displayInvalidPlay(DominoPlayer dominoPlayer) {
         this.infoPanel.invalidMove(dominoPlayer);
     }
 
 
     // TurnCoordinator implementation
     // Called by PlayerHandPanel when player selects a bone to play
     public void nextMoveBoneSelected(Bone bone) {
         this.nextMoveBone = bone;
         this.tableArea.showPlayIndicators();
     }
 
     // Called by TablePanel when player selects play position
     public void nextMovePosition(int position) {
         this.tableArea.hidePlayIndicators();
         this.nextMove.put(new PlayWrapper(true, new Play(this.nextMoveBone, position)));
         this.player1Hand.notYourMove();
         this.player2Hand.notYourMove();
         this.infoPanel.denyBoneYard();
     }
 
     public void aiMoveBegins(Player player) {
         if (player == this.player1) {
             player1Hand.yourMove();
         } else if (player == this.player2) {
             player2Hand.yourMove();
         }
     }
 
     public void aiMoveEnds() {
         this.player1Hand.notYourMove();
         this.player2Hand.notYourMove();
     }
 
     public void drawOrPass() {
         this.tableArea.hidePlayIndicators();
         if (this.nextMove != null) {
             this.nextMove.put(new PlayWrapper(false, null));
         }
         this.player1Hand.notYourMove();
         this.player2Hand.notYourMove();
     }
 
     public void updateBoneYard(BoneYard boneYard) {
         this.infoPanel.updateInfoPanel(boneYard);
     }
 
     // Called by Player when it requires a move from the UI
     public void getPlayerMove(Player player, PlayWrapperCubbyHole nextMove) {
         this.nextMove = nextMove;
         this.infoPanel.allowBoneYard();
         if (player == this.player1) {
             player1Hand.yourMove();
         } else if (player == this.player2) {
             player2Hand.yourMove();
         }
     }
 
     public void handleWinner(DominoPlayer pWinner) {
         displayRoundWinner(pWinner);
     }
 
     public void nextGame() {
         parent.showNewGameDialog();
     }
 
     public boolean gameIsActive() {
         return this.dominoesThread.isAlive();
     }
 
     //Test-use Constructor
     public UI() {
         //for test case
         this.setSize(1400,800);
         this.player1 = this.createPlayer(PlayerType.Human, "Player 1");
         this.player1Type = PlayerType.Human;
         this.player2 = this.createPlayer(PlayerType.Computer, "Player 2");
         this.player2Type = PlayerType.Computer;
 
         EtchedBorder eb1 = new EtchedBorder(EtchedBorder.RAISED);
         Dimension notTooTall = new Dimension(2000,200);
         this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
         infoPanel = new InfoPanel(this);
         infoPanel.setMaximumSize(notTooTall);
         setupScorePanel(infoPanel, eb1);
         player1Hand = new PlayerHandPanel(player1Type, this);
         player1Hand.setMaximumSize(notTooTall);
         setupPlayerHand(player1Hand, eb1);
 
         tableArea = new TablePanel(this);
         tableArea.setMaximumSize(notTooTall);
         setupTableArea(eb1);
 
         player2Hand = new PlayerHandPanel(player2Type, this);
         player2Hand.setMaximumSize(notTooTall);
         setupPlayerHand(player2Hand, eb1);
 
         this.validate();
         dominoesGame = null;
         dominoesThread = null;
     }
 
     public void endGame() {
         this.dominoesThread.stop();
     }
 }
