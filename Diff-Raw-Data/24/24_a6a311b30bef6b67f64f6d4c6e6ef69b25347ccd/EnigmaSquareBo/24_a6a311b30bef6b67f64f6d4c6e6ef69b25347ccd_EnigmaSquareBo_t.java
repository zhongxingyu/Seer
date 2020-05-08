 package model.bo;
 
 //<editor-fold defaultstate="collapsed" desc=" import ">
 import cococare.common.CCFormat;
 import cococare.common.CCLogic;
 import cococare.common.CCSprite;
 import cococare.common.CCThread;
 import static cococare.common.CCThread.threadSleep;
 import cococare.swing.CCSwing;
 import cococare.swing.component.CCImage;
 import controller.form.DlgGameOverCtrl;
 import controller.form.DlgQuestionCtrl;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.util.*;
 import static model.mdl.EnigmaSquareMain.gameOption;
 import static model.mdl.EnigmaSquareMain.questions;
 import model.obj.Enum.GameMode;
 import model.obj.Enum.QuestionOrder;
 import model.obj.Player;
 import model.obj.Question;
 import view.form.PnlSquare;
 //</editor-fold>
 
 public class EnigmaSquareBo {
 
     private CCImage imgBoard;
     private CCSprite sprDice;
     //
     private Random random = new Random();
     //
     private List<Player> playingPlayers = new ArrayList();
     private List<Player> unfinishedPlayers = new ArrayList();
     private List<Player> finishedPlayers = new ArrayList();
     private HashMap<Player, CCImage> playerPawn = new HashMap();
     private HashMap<Integer, PnlSquare> numberSquare = new HashMap();
     //
     private List<Question> playingQuestions = new ArrayList();
     //
     private Date gameStart;
     private int gameTurn;
     private boolean gameEnd = true;
     //
     private Player currentPlayer;
     private int currentDiceEye;
     private int step;
 
 //<editor-fold defaultstate="collapsed" desc=" private method ">
     private CCImage _newPlayerPawn(Player player) {
         CCImage imgPawn = new CCImage();
         imgPawn.setBounds(0, 0, 30, 30);
         imgPawn.setIcon(player.getIcon());
         imgPawn.setIconAutoFit(true);
         imgPawn.setIconProportion(true);
         imgPawn.setOpaque(false);
         imgPawn.setToolTipText(player.getName());
         return imgPawn;
     }
 
     private List<Integer> _newRandomIndexes(int size) {
         List<Integer> indexes = new ArrayList();
         while (indexes.size() < size) {
             int index = random.nextInt(size);
             if (!indexes.contains(index)) {
                 indexes.add(index);
             }
         }
         return indexes;
     }
 
     private List<Integer> _newRandomQuestionBoards(int size) {
         List<Integer> indexes = new ArrayList();
         while (indexes.size() < size) {
             int index = random.nextInt(25) + 1;
             if (!indexes.contains(index)) {
                 indexes.add(index);
             }
         }
         return indexes;
     }
 
     private PnlSquare _boardAddNewSquare(CCImage imgBoard, int number, int x, int y, boolean hasQuestion) {
         PnlSquare square = new PnlSquare(CCFormat.getString(number));
         square.setBounds(110 * x, 110 * y, 110, 110);
         if (number == 1) {
             square.setIcon(getClass().getResource("/resource/start.jpg"));
         } else if (number == 25) {
             square.setIcon(getClass().getResource("/resource/finish.png"));
         }
         square.setHasQuestion(hasQuestion);
         imgBoard.add(square);
         return square;
     }
 
     private Integer[] _newRollDice() {
         List<Integer> diceEyes = new ArrayList();
         while (diceEyes.size() < 10) {
             diceEyes.add(random.nextInt(6));
         }
         currentDiceEye = diceEyes.get(diceEyes.size() - 1) + 1;
         return diceEyes.toArray(new Integer[0]);
     }
 
     private void _showPlayerPawnOnNextSquare(Player player, boolean forward) {
        if (player.getSquareNumber() >= 25 || (!forward && player.getSquareNumber() <= 1)) {
            player.setReverse(forward);
         }
         player.setSquareNumber(player.getSquareNumber() + ((forward ? 1 : -1) * (player.isReverse() ? -1 : +1)));
         int i = gameOption.getPlayers().indexOf(player);
         CCSwing.getCCImage(numberSquare.get(player.getSquareNumber()), "imgIcon" + (i + 1)).add(playerPawn.get(player));
     }
 
     private Question _drawQuestion() {
         if (playingQuestions.isEmpty()) {
             initQuestions();
         }
         return playingQuestions.remove(0);
     }
 
     private Player _nextPlayer() {
         if (CCLogic.isNull(getCurrentPlayer())) {
             currentPlayer = getUnfinishedPlayers().get(0);
         } else {
             int index = getUnfinishedPlayers().indexOf(getCurrentPlayer());
             if (++index >= getUnfinishedPlayers().size()) {
                 index = 0;
             }
             currentPlayer = getUnfinishedPlayers().get(index);
         }
         getCurrentPlayer().setReverse(false);
         return getCurrentPlayer();
     }
 
     private void _sortUnfinishedPlayersByScore() {
         for (int i = 0; i < unfinishedPlayers.size() - 1; i++) {
             for (int j = 0; j < unfinishedPlayers.size() - 1; j++) {
                 Player player0 = unfinishedPlayers.get(j);
                 Player player1 = unfinishedPlayers.get(j + 1);
                 if (player1.getScore() > player0.getScore()) {
                     unfinishedPlayers.set(j, player1);
                     unfinishedPlayers.set(j + 1, player0);
                 }
             }
         }
     }
 //</editor-fold>
 
     /**
      * initial board.
      */
     public EnigmaSquareBo(CCImage imgBoard) {
         this.imgBoard = imgBoard;
     }
 
     /**
      * initial dice sprite.
      */
     public void initSpriteDice(CCImage imgDice) {
         if (CCLogic.isNull(sprDice)) {
             BufferedImage bufferedImage = cococare.datafile.CCImage.getImage(getClass().getResource("/resource/dice.png"));
             bufferedImage = cococare.datafile.CCImage.resizeImage(bufferedImage, 300, 200);
             BufferedImage[] bufferedImages = cococare.datafile.CCImage.splitImages(bufferedImage, 3, 2);
             sprDice = new CCSprite(bufferedImages, imgDice, 0, 0);
         }
     }
 
     /**
      * initPlayers();<br/>
      * initBoard();<br/>
      * initPlayerPawnOnBoard();<br/>
      * initQuestions();
      */
     public void newGame() {
         initPlayers();
         initBoard();
         initPlayerPawnOnBoard();
         initQuestions();
         gameStart = new Date();
         gameTurn = 1;
         gameEnd = false;
     }
 
     /**
      * call in newGame().
      */
     protected void initPlayers() {
         getPlayingPlayers().clear();
         getUnfinishedPlayers().clear();
         getFinishedPlayers().clear();
         playerPawn.clear();
         for (int i = 0; i < gameOption.getPlayerNumber(); i++) {
             Player player = gameOption.getPlayers().get(i);
             player.resetScore();
             getPlayingPlayers().add(player);
             getUnfinishedPlayers().add(player);
             playerPawn.put(player, _newPlayerPawn(player));
         }
         currentPlayer = null;
         _nextPlayer();
     }
 
     /**
      * call in newGame().
      */
     protected void initBoard() {
         imgBoard.removeAll();
         List<Integer> questionBoards = _newRandomQuestionBoards(gameOption.getQuestionNumber());
         numberSquare.clear();
         int number = 1;
         for (int y = 4; y > -1; y--) {
             if (y % 2 == 0) {
                 for (int x = 0; x < 5; x++) {
                     numberSquare.put(number, _boardAddNewSquare(imgBoard, number, x, y, questionBoards.contains(number)));
                     number++;
                 }
             } else if (y % 2 == 1) {
                 for (int x = 4; x > -1; x--) {
                     numberSquare.put(number, _boardAddNewSquare(imgBoard, number, x, y, questionBoards.contains(number)));
                     number++;
                 }
             }
         }
         imgBoard.repaint();
     }
 
     /**
      * call in newGame().
      */
     protected void initPlayerPawnOnBoard() {
         for (int i = 0; i < getPlayingPlayers().size(); i++) {
             Player player = getPlayingPlayers().get(i);
             CCSwing.getCCImage(numberSquare.get(player.getSquareNumber()), "imgIcon" + (i + 1)).add(playerPawn.get(player));
         }
     }
 
     /**
      * call in newGame().
      */
     protected void initQuestions() {
         if (gameOption.getQuestionNumber() > 0) {
             playingQuestions.clear();
             QuestionOrder questionOrder = QuestionOrder.values()[gameOption.getQuestionOrderIndex()];
             if (QuestionOrder.SEQUENCE.equals(questionOrder)) {
                 //register questions
                 for (int i = 0; i < questions.size(); i++) {
                     playingQuestions.add(questions.get(i));
                 }
             } else if (QuestionOrder.RANDOM.equals(questionOrder)) {
                 //random indexes
                 List<Integer> indexes = _newRandomIndexes(questions.size());
                 //register questions
                 for (int i = 0; i < indexes.size(); i++) {
                     playingQuestions.add(questions.get(indexes.get(i)));
                 }
             }
         }
     }
 
     /**
      * roll dice
      */
     public void rollDice(final ActionListener postActionListener) {
         //roll dice animation
         sprDice.addScene("RollDice", _newRollDice());
         sprDice.playOneLoop();
         //player pawn animation
         step = currentDiceEye;
         new CCThread() {
             @Override
             public void run() {
                 //player pawn animation
                 while (step > 0) {
                     _showPlayerPawnOnNextSquare(getCurrentPlayer(), true);
                     imgBoard.repaint();
                     step--;
                     threadSleep();
                     threadSleep();
                 }
                 //show question dialog
                 if (numberSquare.get(getCurrentPlayer().getSquareNumber()).isHasQuestion()) {
                     Question question = _drawQuestion();
                     //show question dialog
                     if (new DlgQuestionCtrl().init(question)) {
                         System.out.println("answerTrue");
                         step = question.getGift();
                         getCurrentPlayer().answerTrue();
                     } else {
                         System.out.println("answerFalse");
                         step = question.getPenalty();
                         if (step > 0) {
                             step = -currentDiceEye;
                         }
                         getCurrentPlayer().answerFalse();
                     }
                     while (step != 0) {
                         _showPlayerPawnOnNextSquare(getCurrentPlayer(), step > 0);
                         imgBoard.repaint();
                         if (step > 0) {
                             step--;
                         } else {
                             step++;
                         }
                         threadSleep();
                         threadSleep();
                     }
                 }
                 //end turn
                 endTurn();
                 postActionListener.actionPerformed(null);
                 interrupt();
             }
         }.start();
     }
 
     /**
      * call in rollDice().
      */
     protected void endTurn() {
         //end turn
         getCurrentPlayer().incTurnNumber();
         getCurrentPlayer().countScore();
         if (getCurrentPlayer().getSquareNumber() >= 25) {
             getFinishedPlayers().add(getUnfinishedPlayers().remove(getUnfinishedPlayers().indexOf(getCurrentPlayer())));
         }
         gameTurn++;
         //check game end or not
         GameMode gameMode = GameMode.values()[gameOption.getGameModeIndex()];
         if (GameMode.ONE_LOSSER.equals(gameMode)) {
             gameEnd = getUnfinishedPlayers().size() == 1;
         } else if (GameMode.ONE_WINNER.equals(gameMode)) {
             gameEnd = getFinishedPlayers().size() == 1;
         } else if (GameMode.TEN_TURN.equals(gameMode)) {
             gameEnd = getGameTurn() > 10;
         } else {
            gameEnd = getUnfinishedPlayers().isEmpty();
         }
         //next player
         if (isGameEnd()) {
             _sortUnfinishedPlayersByScore();
             new DlgGameOverCtrl().init(this);
         } else {
             _nextPlayer();
         }
     }
 
 //<editor-fold defaultstate="collapsed" desc=" getter ">
     public List<Player> getPlayingPlayers() {
         return playingPlayers;
     }
 
     public List<Player> getUnfinishedPlayers() {
         return unfinishedPlayers;
     }
 
     public List<Player> getFinishedPlayers() {
         return finishedPlayers;
     }
 
     public Player getCurrentPlayer() {
         return currentPlayer;
     }
 
     public Date getGameStart() {
         return gameStart;
     }
 
     public int getGameTurn() {
         return gameTurn;
     }
 
     public boolean isGameEnd() {
         return gameEnd;
     }
 //</editor-fold>
 }
