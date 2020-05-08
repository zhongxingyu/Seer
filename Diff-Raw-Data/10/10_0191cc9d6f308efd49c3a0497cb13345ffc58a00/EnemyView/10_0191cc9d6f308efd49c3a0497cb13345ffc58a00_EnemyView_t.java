 package com.secondhand.view.entities;
 
 import java.beans.PropertyChangeEvent;
 
 import com.secondhand.model.Enemy;
 
 public class EnemyView extends BlackHoleView{
 
	public EnemyView(final Enemy enemy) {
 		super(enemy);
 
 		getBody().setLinearDamping(1.2f);
 	}
 
 	@Override
	public void propertyChange(final PropertyChangeEvent event) { }
 
 }
