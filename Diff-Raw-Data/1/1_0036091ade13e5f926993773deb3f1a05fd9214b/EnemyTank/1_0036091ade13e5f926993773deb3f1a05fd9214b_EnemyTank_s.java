 package com.tank;
 
 import java.awt.Color;
 
 import com.main.TankPanel;
 
 public class EnemyTank extends Tank{
 	{
 		this.setColor(Color.RED);
 		this.setGood(false);
 	}
 	public EnemyTank(int x, int y) {
 		super(x, y);

 	}
 		
 	public EnemyTank(int x, int y, TankPanel tankPanel) {
 		super(x, y, tankPanel);
 	}
 }
