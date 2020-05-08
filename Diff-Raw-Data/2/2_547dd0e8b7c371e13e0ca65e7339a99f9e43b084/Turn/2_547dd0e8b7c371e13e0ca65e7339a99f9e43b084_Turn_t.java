 package com.forrestpruitt.texas;
 
 import java.util.Scanner;
 
 public class Turn {
 	
 	Player player;
 	Player opponent;
 	public Turn(Player player, Player opponent)
 	{
 		this.player = player;
 		this.opponent = opponent;
 	}
 	
 	//If this method returns:
 	// 0, betting continues
 	// -1, the opponent wins
 	public int takeTurn()
 	{
 		//Determine the amount needed to call
 		int amountToCall = Game.betToCall;
 		
 		Scanner in = new Scanner(System.in);
 		
 		System.out.println(player.getName()+", Do you wish to call, bet, or fold?");
 		
 		int answer;
 		//Look for input until input an amount in the correct range.
 		//If they want to check, make sure the amountToCall is not 0, or they can't check.
 		do
 		{
 			System.out.println("0 to call, 1 to bet, 2 to check, 3 to fold");
 			answer = in.nextInt();
 			if(answer ==2 && amountToCall != 0)
 			{
 				System.out.println("You can't check, you must call the current bet, bet higher, or fold.");
 			}
 			if(answer == 0 && amountToCall == 0)
 			{
 				System.out.println("There isn't anything to call. Did you mean check?");
 			}
 		}while(answer < 0 || answer > 3 || (answer == 2 && amountToCall != 0) || (answer == 0 && amountToCall == 0));
 		
 		
 		
 		if(answer == 0)
 		{
 			//Code for calling.
			System.out.println(player.getName()+" Is calling. This adds "+amountToCall+" to the pot.");
 			player.betChips(amountToCall);
 			SoundPlayer.playSound(SoundPlayer.sound_betting);
 			System.out.println("The pot has "+Game.chipsInPot+" chips.");
 			Game.betToCall = 0;
 			return 0;
 		}
 		else if(answer == 1)
 		{
 			//Code for placing a bet.
 			int minBet = amountToCall + Game.SMALL_BLIND;
 			
 			//Find out how much user wants to bet.
 			int betAmount;
 			do
 			{
 				System.out.println("How much would you like to bet? (Min. Bet "+minBet+")");
 				System.out.println("(Your chip count: "+player.getNumOfChips()+")");
 				betAmount = in.nextInt();
 			}while(betAmount < 0 || betAmount > player.getNumOfChips());
 			
 			//Place Bet
 			System.out.println(player.getName()+" is betting "+betAmount);
 			if(betAmount == player.getNumOfChips())
 			{
 				System.out.println(player.getName()+" is going all in!");
 			}
 			player.betChips(betAmount);
 			SoundPlayer.playSound(SoundPlayer.sound_betting);
 			
 			//Adjust the new amount the next player needs to call.
 			Game.betToCall = betAmount - amountToCall;
 			
 		}
 		else if(answer == 2)
 		{
 			//ADD CHECKING OPTION HERE.
 			
 			//If you can get here, you SHOULD be able to always pass this assertation.
 			assert(Game.betToCall == 0);
 			return 0;
 			
 		}
 		else if(answer == 3)
 		{
 			opponent.winChips(Game.chipsInPot);
 			SoundPlayer.playSound(SoundPlayer.sound_fold);
 			return -1;
 		}
 		
 		return 0; //Game precedes as normal.
 		
 		
 	}
 		
 		
 }
