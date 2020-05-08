 package uw.cse403.nonogramfun;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.widget.Button;
 
 public class Cell extends Button{
 	private boolean select;
 	private int state;  
 	private int origin; 
 	private int current;
 	
 	public Cell(Context context) {
 		super(context);
 		select = false;
 		state = 0; 
 		current = Color.WHITE;
 	}
 	public void setSelectVal(){
 		if(select)
 			select = false;
 		else
 			select = true;
 	}
 	
 	public boolean getSelectVal(){
 		return select;
 	}
 	
 	//set the state after a click
 	public void setState(){
 		if(state < 2){
     		state++;
 		} else {
     		state = 0;
 		}
 	}
 	
 	public int getState(){
 		return state;
 	}
 	
 	public int getColor(){
 		return current;
 	}
 	
 	//store the origin cell color before any action
 	public void setOriginColor(int color){
 		origin = color;
 	}
 	
 	public void setStateColor(){
 		if (state == 0) {
 			//0 : set to the original cell color (unmark)
 			this.setText("");
 			this.setColor(origin);
 		} else if(state == 1) {
 			//1 : mark the cell black
 			this.setText("");
 			this.setColor(Color.BLACK);
 		} else {
 			//2 : leave a question mark on the cell
 			this.setColor(origin);
 			this.setText("?");
 			this.setTextColor(Color.BLUE);
 		}
 	}
 	
 	public void setColor(int color){
 		this.setBackgroundColor(color);
 		current = color;
 	}
 }
 
