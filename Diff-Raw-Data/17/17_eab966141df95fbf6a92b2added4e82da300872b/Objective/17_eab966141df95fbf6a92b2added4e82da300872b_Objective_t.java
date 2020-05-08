 package com.gozdy.HookIT;
 
 import com.badlogic.gdx.Gdx;
 
 
 public class Objective {
 
 	int match;
 	int[] candies;
 	int[] candyNumber;
 	
 	public Objective(int a, int b, int c, int d)
 	{
 		
 		candies = new int[4];
		candyNumber = new int[4];
 		candyNumber[0] = a;
 		candyNumber[1] = b;
 		candyNumber[2] = c;
 		candyNumber[3] = d;
 		match = 0;
 	}
 	
 	public boolean checkWon()
 	{
		match = 0;
 		for (int i = 0; i < candies.length; i++) {
 			if (candies[i] == candyNumber[i])
 			{
 				match++;
 			}
 		}
 		if (match == 4) {
 			Gdx.app.log("WON", "YOU WON");
 			return true;
		}else{
			Gdx.app.log("MATCH", Integer.toString(match));
			Gdx.app.log("candy1", Integer.toString(candies[0]));
			Gdx.app.log("candy2", Integer.toString(candies[1]));
			Gdx.app.log("candy3", Integer.toString(candies[2]));
			Gdx.app.log("candy4", Integer.toString(candies[3]));
			Gdx.app.log("candy1", Integer.toString(candyNumber[0]));
			Gdx.app.log("candy2", Integer.toString(candyNumber[1]));
			Gdx.app.log("candy3", Integer.toString(candyNumber[2]));
			Gdx.app.log("candy4", Integer.toString(candyNumber[3]));
			return false;
		}
 		
 			
 	}
 	
 	
 	public void reset()
 	{
 		candyNumber[0] = 0;
 		candyNumber[1] = 0;
 		candyNumber[2] = 0;
 		candyNumber[3] = 0;
 		match = 0;
 	}
 	
 }
