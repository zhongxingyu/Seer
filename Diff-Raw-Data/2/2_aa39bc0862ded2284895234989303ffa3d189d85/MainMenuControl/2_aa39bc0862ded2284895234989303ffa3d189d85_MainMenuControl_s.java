 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package memory;
 
 /**
  *
  * @author Karalee Foster
  */
 public class MainMenuControl {
 
     public void startGame(long noPlayers) {
 
         if (noPlayers != 1 && noPlayers != 2) {
             new MemoryError().displayError("startGame - invalid number of players specified.");
             return;
         }
 
         Game game;
         if (noPlayers == 1) {
             game = this.create("ONE_PLAYER");
         } else {
             game = this.create("TWO_PLAYER");
         }
 
         GameMenuView gameMenu = new GameMenuView(game);
         gameMenu.getInput(game);
     }
 
     public Game create(String gameType) {
         Game game = null;
         Player playerA = null;
         Player playerB = null;
 
         if (gameType == null) {
             new MemoryError().displayError("MainCommands - create: gameType is null");
             return null;
         }
 
         if (gameType.equals(Game.ONE_PLAYER)) {
             game = new Game(Game.ONE_PLAYER);
             playerA = new Player(Player.REGULAR_PLAYER);
             playerA.setName("Player 1");
             playerB = new Player(Player.COMPUTER_PLAYER);
             playerB.setName("Computer");
         } else if (gameType.equals(Game.TWO_PLAYER)) {
             game = new Game(Game.TWO_PLAYER);
             playerA = new Player(Player.REGULAR_PLAYER);
             playerA.setName("Player 1");
             playerB = new Player(Player.REGULAR_PLAYER);
             playerB.setName("Player 2");
 
         }
 


         game.setPlayerA(playerA);
         game.setPlayerB(playerB);
 
         return game;
     }
 }
