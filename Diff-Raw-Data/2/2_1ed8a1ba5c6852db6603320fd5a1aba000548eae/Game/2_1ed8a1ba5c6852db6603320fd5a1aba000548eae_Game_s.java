 package de.thm.ateam.memory.game;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.view.View;
 import android.widget.TextView;
 
 public class Game {
 	
 	ArrayList<Player> list;
 	Integer current = -1;
 	Context ctx;
 	
 	public Game(Context ctx, GameAttributes attr){
 		this.ctx = ctx;
 	}
 	
 	public Player next(){
		return list.get((current++)%list.size());
 	}
 	
 	public int add(Player player){
 		return list.add(player)?list.indexOf(player):-1;
 	}
 	
 	public void turn(){
 		next().myTurn();
 	}
 	
 	public View assembleLayout(){
 		TextView tv = new TextView(ctx);
 		tv.setText("You are using the method of "+this.getClass().getSimpleName()+" and you might want to override this.");
 		return tv;
 	}
 	
 	private void newGame(){
 		
 	}
 
 }
