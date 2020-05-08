 package com.tictactoe;
 
 import com.tictactoe.Board.Board;
 import com.tictactoe.Board.BoardLogic;
 import com.tictactoe.Player.ComputerPlayer;
 import com.tictactoe.Player.HumanPlayer;
 import com.tictactoe.Player.Player;
 import com.tictactoe.UserInterface.UserInterface;
 
 import java.util.Hashtable;
 
 public class Game {
     private Board board;
     private Player playerOne, playerTwo;
     private UserInterface ui;
     private BoardLogic stateOfGame;
 
     public Game(UserInterface ui) {
         this.ui = ui;
     }
 
     public Game(UserInterface ui, Hashtable<String, String> settings) {
         this.ui = ui;
         board = new Board(Integer.parseInt(settings.get("boardSize")));
         board.setSlots(settings.get("board"));
         stateOfGame = new BoardLogic(board);
         playerOne = determinePlayer(settings.get("playerOne"), 'X');
         playerTwo = determinePlayer(settings.get("playerTwo"), 'O');
     }
 
     public Board getBoard() {
         return board;
     }
 
     public Player getPlayerOne() {
         return playerOne;
     }
 
     public Player getPlayerTwo() {
         return playerTwo;
     }
 
     public UserInterface getUi() {
         return ui;
     }
 
     public void askPlayerOne() {
         String pOne = ui.askPlayerOneOption();
         playerOne = determinePlayer(pOne, 'X');
     }
 
     public void askPlayerTwo() {
         String pTwo = ui.askPlayerTwoOption();
         playerTwo = determinePlayer(pTwo, 'O');
     }
 
     public Player determinePlayer(String input, char mark) {
         Player player;
         if(input.equals("human")) player = new HumanPlayer(mark);
         else player = new ComputerPlayer(mark);
         return player;
     }
 
     public void askBoardLength() {
         int boardLength = ui.askBoardOption();
         board = new Board(boardLength);
         stateOfGame = new BoardLogic(board);
     }
 
     public void startGame() {
         ui.welcomeMessage();
         askOptions();
         ui.printBoard(board);
     }
 
     public void askOptions() {
         askPlayerOne();
         askPlayerTwo();
         askBoardLength();
     }
 
     public void endGame() {
         ui.displayResult(result());
         ui.printBoard(board);
         if(ui.askContinue()){
             playGame();
         }
         ui.endGame();
     }
 
     public String result() {
         if(stateOfGame.tie()) return "tie";
         else return String.valueOf(stateOfGame.winner());
     }
 
     public void playGame() {
         startGame();
         while(!stateOfGame.isOver()) makePlayerMoves();
         endGame();
     }
 
     public void makePlayerMoves() {
         if(makePlayerMove(playerOne) && !stateOfGame.isOver()) {
             ui.printBoard(board);
             makePlayerMove(playerTwo);
         }
         if(!stateOfGame.isOver()) ui.printBoard(board);
     }
 
     public boolean makePlayerMove(Player player) {
         if(player.isHuman()) {
             board = player.move(board, ui.askPlayerMove());
         } else {
             board = player.move(board, 0);
         }
         return true;
     }
 
     public BoardLogic getBoardLogic() {
         return stateOfGame;
     }
 
     private void makeMove(Player player, String move) {
        if(player.isHuman() && !(move == ""))
            board = player.move(board, Integer.parseInt(move));
        else if(!player.isHuman())
            board = player.move(board, 0);
     }
 
     public static Board playGame(UserInterface ui, Hashtable<String, String> settings, String move) {
         Game game = new Game(ui, settings);
         Player player = findPlayer(game);
         if(game.board.isEmpty() && player.isHuman()) {
             game.board.setMove(player.getMark(), move);
         } else if(game.getBoardLogic().isOver()) {
             game.ui.displayResult(game.result());
         } else {
             game.makeMove(player, move);
             if(game.getBoardLogic().isOver()) {
                 game.ui.displayResult(game.result());
             }
         }
         return game.getBoard();
     }
 
     private static Player findPlayer(Game game) {
         char mark = game.getBoardLogic().findPlayerTurn(game.board.getSlots());
         if(mark == 'X') return game.playerOne;
         else return game.playerTwo;
     }
 }
