 import javax.swing.*;
 
 import java.util.Calendar;
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
 	private static int startSeconds,currSeconds;
 	
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
 
 				Calendar calendar = Calendar.getInstance();
 				startSeconds = calendar.get(Calendar.SECOND);
 				//System.out.println("start time: " + startSeconds);
 				(new GameMain()).turnTimer();
 				
 
 				System.out.println("Current Player " + currPlayer.getName());
 				//Cycle through playerlist, while!done\
 				map.setCurPlayer(p);
 				
 				game.repaint();
 				//p.takeTurn();
 
 				//}
 				while(!p.isDone()){
					//(new GameMain()).turnTimer();
					int remainTime = p.getTurnTime(currRounds)-(new GameMain()).getTime();
					if (remainTime < 0){
						remainTime = p.getTurnTime(currRounds);
					}
					map.setRemainTime(remainTime);
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
     		}, turnTime*1000);
     	}
 	
 	public int getTime(){
		int remainTime;
 		Calendar calendar = Calendar.getInstance();
 		currSeconds = calendar.get(Calendar.SECOND);
 
 		if (currSeconds > startSeconds){
			remainTime = currSeconds-startSeconds;
 		}
 		else
			remainTime = (60-startSeconds)+currSeconds;
 		return remainTime;
 	}
 	
 	public static Player getCurrPlayer(){
 		return currPlayer;
 	}
 	
 	public static int getCurrTurns(){
 		return currRounds;
 	}
 	
 }
