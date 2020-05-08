 package com.secondhand.view.entities;
 
 import java.beans.PropertyChangeEvent;
 
 import com.secondhand.model.Enemy;
 
 public class EnemyView extends BlackHoleView{
 
	public EnemyView(Enemy enemy) {
 		super(enemy);
 
 		getBody().setLinearDamping(1.2f);
		// TODO Auto-generated constructor stub
 	}
 
 	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// TODO Auto-generated method stub
		
	}
 
 }
