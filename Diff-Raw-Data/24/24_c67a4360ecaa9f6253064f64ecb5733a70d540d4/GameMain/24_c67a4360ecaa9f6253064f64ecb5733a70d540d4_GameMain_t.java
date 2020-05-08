 import javax.swing.*;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.awt.*;
 import java.util.ArrayList;
 
 /*
  * 
  */
 public class GameMain extends JFrame{
 	private static Player currPlayer;
 	private static int currRounds;
 	private static JLabel currTurnLabel; //change
 	private static JLabel currPlayerLabel;
 	public static void main(String[] args){
 		ArrayList<Player> players = new ArrayList<Player>();
 		GameMain game = new GameMain();
 		Initial initial;
 		boolean back;
 		
 		do{
 			back = false;
 			initial = new Initial(game);
 			while(!initial.getContinue()){
 				game.repaint();
 			}
 			for(int i = initial.getNumPlayers(); i>0; i--){
 				PlayerConfig pConfig = new PlayerConfig(game);
 				while(!pConfig.getContinue()){
 					game.repaint();
 					if(pConfig.getBack()){
 						i++;
 						if(i>initial.getNumPlayers())
 							back = true;
 						else
 							players.remove(initial.getNumPlayers() - i);
 						break;
 					}
 				}
 				if(!pConfig.getBack())
 					players.add(pConfig.getPlayer());
 				else
 					i++;
 				if(back){
 					game.setContentPane(initial);
 					break;
 				}
 			}
 		}
 		while(back);
 		
 		
 		MapPanel map = new MapPanel(false, game); //CHANGE FALSE TO BOOL RANDOM
 		game.validate();
 		game.repaint();
 		
 		do{
 			currRounds++;
 			
 			//This will reorder the players from lowest score to highest
 			for(int i=0; i<players.size(); i++){ 
 				for(int j=i-1; j>=0; j--){
					if(players.get(i).getScore()<players.get(j).getScore()){
						players.add(j, players.get(i));
						players.remove(i+1);
 					}
					else
						break;
 				}
 			}
 			for(Player p : players){
 				currPlayer = p;
 				map.setCurrRound(currRounds);
 				map.setCurrPlayerLabel(currPlayer.getName());
 				
 				System.out.println("Current Player " + currPlayer.getName());
 				//Cycle through playerlist, while!done\
 				map.setCurPlayer(p);
 				
 				game.repaint();
 				//p.takeTurn();
 
 				//}
 				while(!p.isDone()){
					//(new GameMain()).turnTimer();
 					game.repaint();
 				}
 				p.setDone(false);
 				game.repaint();
 				if(map.gameOver(currRounds))
 					break;
 				
 			}
 
			map.nextTurn();
 		}while(!map.gameOver(currRounds ));
 		
 	}
 	
 	public void turnTimer() {
     		int turnTime = currPlayer.getTurnTime(currRounds);
     		Timer timer = new Timer();
     		timer.schedule(new TimerTask() {
     			public void run() {
     				currPlayer.setDone(true);
     			}
     		}, turnTime);
     	}
 	
 	public static Player getCurrPlayer(){
 		return currPlayer;
 	}
 	
 	public static int getCurrTurns(){
 		return currRounds;
 	}
 	
 }
