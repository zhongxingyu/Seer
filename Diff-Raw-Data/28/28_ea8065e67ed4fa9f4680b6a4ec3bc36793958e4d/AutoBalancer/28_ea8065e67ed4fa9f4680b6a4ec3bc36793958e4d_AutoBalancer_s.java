 package mods.learncraft.common;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Random;

 import javax.swing.Timer;

 import net.minecraft.entity.player.EntityPlayer;
 
 public class AutoBalancer
 {
 	private Timer timer, runner;
 	private Random random;
 	private int orangeTeamSize, blueTeamSize, total, take;
 	private Team largerTeam, smallerTeam;
 	private String teleportLocation, message;
 	private boolean balanced = true;
 	
 	public AutoBalancer()
 	{
 		timer = new Timer(0, new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				 orangeTeamSize = Common.getTeam("orange").getOnline();
 				 blueTeamSize = Common.getTeam("blue").getOnline(); 
 				 total = orangeTeamSize + blueTeamSize;
 				 take = 0; //the amount of players taken from the larger team
 				 largerTeam = null;
 				 smallerTeam = null;
 				 teleportLocation = "";
 				 message = "";
 				 
 				 //Determine which team is larger, and set some corresponding attributes
 				 if(orangeTeamSize > blueTeamSize)
 				 {
 					 largerTeam = Common.getTeam("orange");
 					 smallerTeam = Common.getTeam("blue");
 					 take = orangeTeamSize - total/2;
 					 teleportLocation = "blue_spawn";
 					 message = "You have been switched to the blue team.";
 				 }
 				 
 				 if(blueTeamSize > orangeTeamSize)
 				 {
 					 largerTeam = Common.getTeam("blue");
 					 smallerTeam = Common.getTeam("orange");
 					 take = blueTeamSize - total/2;
 					 teleportLocation = "gold_spawn";
 					 message = "You have been switched to the orange team.";
 				 }
 				 
 				 //If by some random chance the teams are equal, don't autobalance
 				 if(orangeTeamSize == blueTeamSize)
 				 {
 					 Common.announce("Autobalance has detected equality where before there was none.");
 					 return;
 				 }
 				 
 				 for(int i = 0; i < take; i++)
 				 {
 					 //Due to the way the roster works, players are added at the spot of whatever the current numroster
 					 //happens to be. This returns a list of all those indices, and one of them is randomly chosen.
 					 ArrayList<Integer> indices = largerTeam.getCurrentIndices(); 
 					 int index = random.nextInt(indices.size());
 					 EntityPlayer player = largerTeam.roster[indices.get(index)];
 					 
 					 if(player != null)
 					 {
 						 //Empty the player's inventory
 						 for(int j = 0; j < player.inventory.getSizeInventory(); j++)
 						 {
 							 player.inventory.setInventorySlotContents(j, null);
 						 }
 						 
 						 largerTeam.removePlayer(player); //remove from larger team
 						 smallerTeam.addPlayer(player); //add to smaller team	
 						 Common.teleportPlayerTo(player, teleportLocation); //teleport to the right team's base
 						 player.addChatMessage(message); //Notify the player they were switched
 					 }
 				 }
 				 
 				 //Assume the game to be balanced again
 				 balanced = true;
 				 timer.stop();
 			}
 		});
 		timer.setInitialDelay(10 * 1000);
 		random = new Random();
 		
 		runner = new Timer(1000, new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				autoBalance();
 			}
 			
 		});
 		
 		runner.start();
 	}
 	
 	public void autoBalance()
 	{
 	    orangeTeamSize = Common.getTeam("orange").getOnline();
 		blueTeamSize = Common.getTeam("blue").getOnline();
 		
 		if(balanced)
 		{
 			//Used to disable switching teams
 			Common.disableTeamSwitch = false;
 			
 			//Determine if there is an imbalance
 			if((orangeTeamSize - blueTeamSize) >= 2)
 			{
 				balanced = false;
 				Common.disableTeamSwitch = true;
 				Common.announce("Blue team is at a disadvantage, teams will be autobalanced in 10 seconds.");
 			}
 			
 			if((blueTeamSize - orangeTeamSize) >= 2)
 			{
 				balanced = false;
 				Common.disableTeamSwitch = true;
 				Common.announce("Orange team is at a disadvantage, teams will be autobalanced in 10 seconds.");
 			}
 		}
 		
 		//If the not balanced flag has been set, then start the timer which does the autobalancing
 		if(!balanced)
 		{
 			timer.start();
 		}
 	}
 	
 	
 	
 }
