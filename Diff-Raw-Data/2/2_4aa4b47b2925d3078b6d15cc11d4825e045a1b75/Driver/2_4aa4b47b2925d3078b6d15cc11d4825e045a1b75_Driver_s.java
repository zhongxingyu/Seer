 package com.forrestpruitt.texas;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import com.forrestpruitt.texas.Card.Rank;
 import com.forrestpruitt.texas.Card.Suit;
 
 import java.util.Collections;
 
 public class Driver
 {
 	public static ArrayList<Player> players = new ArrayList<Player>();
 	
 	public static void main(String args[])
 	{
 		/*
 		Card aCard = new Card(Rank.TWO, Suit.SPADE, 0);
 		Card bCard = new Card(Rank.ACE, Suit.HEART, 1);
 		Card cCard = new Card(Rank.FOUR, Suit.CLUB, 2);
 		Card dCard = new Card(Rank.FIVE, Suit.SPADE, 3);
 		Card eCard = new Card(Rank.SIX, Suit.SPADE, 4);
 		Card fCard = new Card(Rank.SEVEN, Suit.CLUB, 5);
 		Card gCard = new Card(Rank.ACE, Suit.DIAMOND, 6);
 
 		ArrayList<Card> aCardList = new ArrayList<Card>();
 		aCardList.add(aCard); aCardList.add(bCard); aCardList.add(cCard); 
 		aCardList.add(dCard); aCardList.add(eCard); aCardList.add(fCard); 
 		aCardList.add(gCard); 
 
 		PokerHandEvaluator evaluator = new PokerHandEvaluator(aCardList);
 
 		System.out.println(evaluator);
 		*/
 		
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(TexasGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(TexasGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(TexasGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(TexasGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Creates and displays the Texas GUI and sets the window to center screen */
         /*java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                TexasGUI newGUI = new TexasGUI();
                newGUI.setVisible(true);
                //Sets the Texas GUI to center screen on open
                newGUI.setLocationRelativeTo(null);    
             }
         });*/
 		
 		
 		
 		Scanner in = new Scanner(System.in);
 		System.out.println("Enter your name: ");
 		String playerName = in.nextLine();
 		System.out.println("Enter number of starting chips: ");
 		int numOfStartingChips = in.nextInt();
 		Player player1 = new Player(numOfStartingChips,playerName,0);
 		Player player2 = new Player(numOfStartingChips,"Computer",1);
 		
 		int gamesPlayed = 0;
 		
 		boolean again = true;
 
 		while(again)
 		{
 			Game testGame = new Game(player1, player2);
 		
 			//If StartGameLoop returns -1, the human player ran out of chips.
 			//If StartGameLoop returns -2, the computer player ran out of chips.
 			//If it returns 1, the human player won.
 			//If it returns 2, the computer player won.
 			int returnValue = testGame.StartGameLoop();
 			gamesPlayed++;
 			
 			//Handle endgame conditions
 			if(returnValue == -1)
 			{
 				gamesPlayed--;
 				System.out.println("You have run out of chips. ");
 				System.out.println("Press 1 to add more chips, 2 to quit");
 				int ans = in.nextInt();
 				if(ans == 1)
 				{
 					System.out.println("Enter the number of chips you would like to add: ");
 					int chips = in.nextInt();
 					player1.winChips(chips);
 				}
 				else if(ans == 2)
 				{
 					System.out.println("Your final stats for this game were: ");
 					printStats(player1, gamesPlayed);
 					System.out.println("Exiting...");
 					again = false;
 				}
 			}
 			else if(returnValue == -2)
 			{
 				gamesPlayed--;
 				System.out.println("Press 1 to give it more chips, or 2 to quit.");
 				int ans = in.nextInt();
 				if(ans == 1)
 				{
 					System.out.println("Enter the number of chips you would like to add: ");
 					int chips = in.nextInt();
 					player2.winChips(chips);
 				}
 				else if(ans == 2)
 				{
 					System.out.println("Your final stats for this game were: ");
 					printStats(player1, gamesPlayed);
 					System.out.println("Exiting...");
 					again = false;
 				}
 			}
 			else if(returnValue == 1)
 			{
 				SoundPlayer.playSound(SoundPlayer.sound_win);
 
 				player1.winGame();
 				System.out.println("You won!");
 				System.out.println("Your current stats are: ");
 				printStats(player1, gamesPlayed);
 				System.out.println("Would you like to play again? 1 for yes, 0 for no: ");
 				int ans = in.nextInt();
 				if(ans == 0)
 					again = false;
 			}
 			else if(returnValue == 2)
 			{
 				SoundPlayer.playSound(SoundPlayer.sound_lose);
 
 				player2.winGame();
 				System.out.println("The computer won the game.");
 				System.out.println("Your current stats are: ");
 				printStats(player1, gamesPlayed);
 				System.out.println("Would you like to play again? 1 for yes, 0 for no: ");
 				int ans = in.nextInt();
 				if(ans == 0)
 					again = false;
 			}
 			else if(returnValue == 3)
 			{
 				SoundPlayer.playSound(SoundPlayer.sound_win);
 
				System.out.println("The computer won the game.");
 				System.out.println("Your current stats are: ");
 				printStats(player1, gamesPlayed);
 				System.out.println("Would you like to play again? 1 for yes, 0 for no: ");
 				int ans = in.nextInt();
 				if(ans == 0)
 					again = false;
 			}
 			
 			
 			//Reset static variables
 			Game.chipsInPot = 0;
 
 			player1.clearTotalBetThisRound();
 			player2.clearTotalBetThisRound();
 
 			player1.setIsAllIn(false);
 			player2.setIsAllIn(false);
 			//Game.betToCall = 0;
 		}
 		
 	}
 	
 	private static void printStats(Player player, int gamesPlayed)
 	{
 		System.out.println("> " + "Wins: "+ player.getWins());
 		System.out.println("> " + "Losses: "+ (gamesPlayed - player.getWins()));
 		//Rounds to nearest int
 		System.out.println("> " + "Win Percentage: "+Math.round(((double)player.getWins()/gamesPlayed)*100)+"%");
 	}
 	
 }
