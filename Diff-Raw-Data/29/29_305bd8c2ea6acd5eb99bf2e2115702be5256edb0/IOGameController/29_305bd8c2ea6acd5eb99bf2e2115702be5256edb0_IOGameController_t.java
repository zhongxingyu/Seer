 package ioGame;
 
 import game.Game;
 import game.Movement;
 import game.Player;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 import ai.LMinimax;
 import ai.Minimax;
 
 public class IOGameController {
 	File file;
 	Game game;
 	
 	public IOGameController(String fileName, String playerNumber){
 		char[] charBoard = openCharBoard(fileName);
 		
 		Player player;
 		this.game = new Game(charBoard);
 	
 		if (playerNumber.equals("1")){
 			player = game.getHuman();
 		} else {
 			player = game.getComputer();
 		}
 	
 		start(player);
 
 		System.out.println(this.game.getBoard());
 	
 	}
 
 
 	
 	private void start(Player player){
 		System.out.println(this.game.getBoard());
 		//TODO: delete magic number
 		Minimax ai = new LMinimax(4, game.getStrategy(), game.getBoard(), player, game.getOpponent(player));
 
 		Movement move = ai.getBestMove();
 		
 		game.move(move); //borrar
 		
 		
 		if (move == null){
 			System.out.println("PASS");
 		} else {
			System.out.println("Play:" + move);
 		}
 	}
 	
 	private char[] openCharBoard(String path){
 		try {
 			BufferedReader buffer = new BufferedReader(new FileReader(path));
 			
 			char[] charBoard = new char[Game.getBoardHeight() * Game.getBoardWidth()];
 			
 			buffer.read(charBoard);
 			buffer.close();
 		System.out.println(charBoard);
 			return charBoard;
 			
 		} catch (FileNotFoundException e) {
 			return null;
 		} catch (IOException e) {
 			return null;
 		}
 		
 	}
 
 }
