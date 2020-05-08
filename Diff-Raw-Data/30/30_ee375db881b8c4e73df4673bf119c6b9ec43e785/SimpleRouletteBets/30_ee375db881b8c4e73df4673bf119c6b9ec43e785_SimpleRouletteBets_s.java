
 
 
 /**
  * Basic program for Roulette Bets
  * Methods for new game, making bets, spin wheel
  * @author yeotaJMU
  * last update: 4/18/2013
  */
 public class SimpleRouletteBets 
 {
 	public double bankroll;
 	public double betAmount;
 	public int result;
 	public int finalBet;
 	
 	
 	//initialize bankroll
 	public double startBankroll ()
 	{
 		bankroll = (bankroll + 1000.00);
 		return bankroll;
 	}
 	//takes in bet from GUI
 	public void placeBet()
 	 {
 		 if (betAmount <= 0)
 		 {
			 System.err.println("You must bet a positive number!");
			 System.exit(0);
//			 placeBet();
 		 }
 		 else if (betAmount > bankroll)
 		 {
			 System.err.println("You can't bet more than you have!");
			 System.exit(0);
//			 placeBet();
 		 }
 		 else
		 bankroll = bankroll - betAmount;
		 spin();
 	 }
 	//generate rand, calculate results of spin
 	 public void spin()
 	 {
 		 result = (int) (Math.random()*37);
 		 if (finalBet <= 36)
 		 {
 			 if (result == finalBet)
 			 {
 				 bankroll = bankroll + (betAmount + (35 * betAmount));
 			 }
 			 else
 			 {
 				 bankroll = bankroll;
 			 }	 
 		 }
 		 else if (finalBet == 37)
 		 {
 			 black();
 		 }
 		 else if (finalBet == 38)
 		 {
 			 red();
 		 }
 	 }
 	 //considers all black numbers
 	 public void black()
 	 {
 		 if ((result==2)||(result==4)||(result==6)||(result==8)||(result==10)
 					||(result==11)||(result==13)||(result==15)||(result==17)||(result==20)
 					||(result==22)||(result==24)||(result==26)||(result==28)||(result==29)
 					||(result==31)||(result==33)||(result==35))
 			{
 				bankroll = bankroll + (betAmount*2);
 			}
 			else
 			{
 				bankroll = bankroll;
 			}			
 		 
 	 }
 	 //considers all red numbers
 	 public void red()
 	 {
 		 if ((result==1)||(result==3)||(result==5)||(result==7)||(result==9)
 					||(result==12)||(result==14)||(result==16)||(result==18)||(result==19)
 					||(result==21)||(result==23)||(result==25)||(result==27)||(result==30)
 					||(result==32)||(result==34)||(result==36))
 		 {
 			 bankroll = bankroll + (betAmount * 2);
 		 }
 		 else
 		 {
 			 bankroll = bankroll;
 		 }
 	 }	
 
 	 
 	 
 	//getters and setters
 	double getBetAmount() 
 	{
 		return betAmount;
 	}
 
 	public void setBetAmount(int betAmount) 
 	{
 		this.betAmount = betAmount;
 	}
 
 	
 	double getBankroll(){
 		return bankroll;
 	}
 	
 	public void setBankroll(double bankroll){
 		this.bankroll = bankroll;
 	}
 
 
 	public int getResult() {
 		return result;
 	}
 
 
 	public void setResult(int result) {
 		this.result = result;
 	}
 	
 	
 	
 
 }
