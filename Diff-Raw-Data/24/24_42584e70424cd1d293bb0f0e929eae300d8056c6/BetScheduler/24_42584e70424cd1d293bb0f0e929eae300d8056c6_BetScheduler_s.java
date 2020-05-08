 package server;
 import java.util.Iterator;
 import java.util.Vector;
 
 import pt.uc.dei.sd.BetManager;
 import pt.uc.dei.sd.IBetManager;
 import pt.uc.dei.sd.IMatch;
 
 
 public class BetScheduler extends Thread{
 	int TIME_BETWEEN_ROUNDS = 60000;
 	String message, lastMatches = "";
 	ActiveClients activeClients;
     IBetManager man;
     int gamesPerRound;
     int [] gameResults;
     Vector<Bet> betList;
 	
 	public BetScheduler(ActiveClients activeClients, int gamesPerRound){
 		this.activeClients = activeClients;
		man = new BetManager();
 		this.gamesPerRound=gamesPerRound;
 		gameResults = new int [gamesPerRound];
 		betList = new Vector<Bet>(0);
 		
 		this.start();
 	}
 	
 	public void run(){
         int nGame;
         while (true){
         	message = "";
 	        message += "\n========= Batch of Matches =========\n";        
 	        for (IMatch m : man.getMatches()) {
 	            message += m.getCode()+" - "+m.getHomeTeam() + " vs " + m.getAwayTeam() + "\n";
 	        }
 	        synchronized(lastMatches){
 	        	lastMatches=new String(message);
 	        }
 	        //System.out.println(message);
 	        try {
 				Thread.sleep(TIME_BETWEEN_ROUNDS);
 			} catch (InterruptedException e) {
 				System.out.println("BetScheduler has been interrupted while sleeping.");
 			}
 			
 			synchronized(man){
 				message = "";
 				nGame=0;
 		        message += "========= Results =========\n";
 		        for (IMatch m : man.getMatches()) {
 		        	message += (m.getCode() + " - " + m + ": ");
 		            switch (man.getResult(m)) {
 		                case HOME: 
 		                	gameResults[nGame]=1;
 		                	message +=  "1\n";
 		                    break;
 		                case AWAY: 
 		                	gameResults[nGame]=2;
 		                	message +=  "2\n";
 		                    break;
 		                default: 
 		                	gameResults[nGame]=0;
 		                	message += "X\n";
 		                    break;
 		            }
 		            nGame++;
 		        }
 		        
		        /*Check all the bets and sends the result to the respectiv user*/
 		        notifyBets();
 		        
 		        betList.clear();
 		
 		        /*Send the results to all the active clients.*/
 		        activeClients.sendMessageAll(message, null);
 		        /* Creates a new batch of games. */
 		        
 		        man.refreshMatches();
 			}
         }
 	}
 	
 	public void notifyBets(){
 		Iterator<Bet> it = betList.iterator();
 		Bet bet;
 		
 		while(it.hasNext()){
 			bet=it.next();
 			
 			if(gameResults[(bet.getGameNumber()-1)%gamesPerRound]==0 && bet.bet.compareToIgnoreCase("X")==0){
 				activeClients.sendMessageUser("Congratulations, it looks like your guess was right about game " +
 						bet.gameNumber+". You won: "+bet.credits*3+" Credits!", bet.getUser());
 			}
 			else if(gameResults[(bet.getGameNumber()-1)%gamesPerRound]==1 && bet.bet.compareTo("1")==0){
 				activeClients.sendMessageUser("Congratulations, it looks like your guess was right about game " +
 						bet.gameNumber+". You won: "+bet.credits*3+" Credits!", bet.getUser());
 			}
 			else if(gameResults[(bet.getGameNumber()-1)%gamesPerRound]==2 && bet.bet.compareTo("2")==0){
 				activeClients.sendMessageUser("Congratulations, it looks like your guess was right about game " +
 						bet.gameNumber+". You won: "+bet.credits*3+" Credits!", bet.getUser());
 			}
 			else {
 				activeClients.sendMessageUser("Sorry, it looks like your guess wans't right about game " +
 						+bet.gameNumber+"... Please try again!", bet.getUser());
 			}
 		}
 	}
 	
 	public boolean isValidGame(int nGame){
 		 for (IMatch m : man.getMatches()) {
 			 if(m.getMatchCode()==nGame){
 				 return true;
 			 }
 		 }
 		 return false;
 	}
 	
 	public String getMatches(){
 		return lastMatches;
 	}
 	
 	public IBetManager getManager(){
 		return man;
 	}
 	
 	public void addBet(Bet bet){
 		betList.add(bet);
 	}
 }
