 package Monopoly;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.util.ArrayList;
 
 
 public class Dice{
	public int value;
 	ArrayList<Integer> rolls = new ArrayList<Integer>();
 	
 	public Dice() {
 		
 	}
 	
 	public void reset() {
 		rolls.clear();
 	}
 	
 	public int roll(int n)
 	  {
 		for(int i=1;i<=n;i++) {
 			int roll;
 			double number = Math.random();
 			roll = (int) (number * 6) + 1;
 			rolls.add(roll);
 			value = value + roll;
 			System.out.println("Dice "+i+":"+roll);
 		}
 		return value;
 	  }
 		  
 	public void draw(Graphics g) {
 		  int left = 0;
 		  g.setFont(new Font("Verdana", Font.PLAIN, 60));
 		  for(int i=0;i<rolls.size();i++) {
 			  g.setColor(Color.WHITE);
 			  g.fillRoundRect(768+left, 9, 100, 100, 30, 30);
 			  g.setColor(Color.BLACK);
 			  g.drawRoundRect(768+left, 9, 100, 100, 30, 30);
 			  g.drawString(""+rolls.get(i), 768+left+30, 9+70);
 			  left = left + 130;
 			}
 	}
 }
 
