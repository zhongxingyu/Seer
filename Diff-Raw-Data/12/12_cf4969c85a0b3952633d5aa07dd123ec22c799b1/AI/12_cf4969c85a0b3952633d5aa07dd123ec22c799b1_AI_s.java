 package com.jpii.navalbattle.game.turn;
 
 import java.util.ArrayList;
 
 import com.jpii.navalbattle.game.NavalGame;
 import com.jpii.navalbattle.game.NavalManager;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.GridHelper;
 import com.jpii.navalbattle.pavo.grid.Location;
 import com.jpii.navalbattle.game.entity.MoveableEntity;
 
 public class AI extends Player{
 	
 	NavalManager nm;
 	ArrayList<Entity> enemies;
 	
 	public AI(NavalManager nm,String name) {
 		super(name);
 		enemies = new ArrayList<Entity>();
 		this.nm = nm;
 	}
 	
 	public void addEnemyEntity(Entity e){
 		enemies.add(e);
 	}
 
 	public void takeTurn(){
 		for(int k = 0; k < getTotalEntities(); k++)
 		{
 			Entity ent = getEntity(k);
 			MoveableEntity currentEntity;
 			if(ent.getHandle()%10 == 1){
 				currentEntity = (MoveableEntity)ent;
 				System.out.println("my Location is... "+currentEntity.getLocation());
 				if(currentEntity.getHandle()==11){
 					//Sub
 					System.out.println("I am a submarine");
 					moveAIShip(currentEntity);
 					determineCurrentEnemies(currentEntity);
 	
 				/*	System.out.println("Ships next to me: " + pickEnemy(1));
 					if(pickEnemy(1)!=-1){
 					Entity ene = enemies.get(pickEnemy(1));
 					MoveableEntity enemyEntity;
 					enemyEntity = (MoveableEntity)ene;
 					DamageCalculator.doPrimaryDamage(currentEntity, enemyEntity);
 					}*/
 				}
 				if(currentEntity.getHandle()==21){
 					//AC
 					System.out.println("I am a aircraft");
 					moveAIShip(currentEntity);
 				determineCurrentEnemies(currentEntity);
 				/*if(pickEnemy(2)!=-1){
 				Entity ene = enemies.get(pickEnemy(2));
 				MoveableEntity enemyEntity;
 				enemyEntity = (MoveableEntity)ene;
 				DamageCalculator.doPrimaryDamage(currentEntity, enemyEntity);
 				}*/
 				}
 				if(currentEntity.getHandle()==31){
 					//BS
 					System.out.println("I am a battleship");
 					moveAIShip(currentEntity);
 					determineCurrentEnemies(currentEntity);
 			/*		if(pickEnemy(3)!=-1){
 					Entity ene = enemies.get(pickEnemy(3));
 					MoveableEntity enemyEntity;
 					enemyEntity = (MoveableEntity)ene;
 					DamageCalculator.doPrimaryDamage(currentEntity, enemyEntity);
 					}*/
 				}
 			}
 		}
 		
 		turnOver=true;
 		enemies.clear();
 	}
 	
 	public int pickEnemy(int currentShip)
 	{
 		if(!enemies.isEmpty()){
 		switch (currentShip) {
 	      case 1:	for(int k = 0; k < enemies.size(); k++){
 						if(enemies.get(k).getHandle()==21)
 							return k;
 	      				}
 	      			for(int k = 0; k < enemies.size(); k++){
 	      				if(enemies.get(k).getHandle()==31)
 	      					return k;
 	      				}
 	      			for(int k = 0; k < enemies.size(); k++){
 	      				if(enemies.get(k).getHandle()==11)
 	      					return k;
 	      				}
 	      			
 	      case 2:	for(int k = 0; k < enemies.size(); k++){
 						if(enemies.get(k).getHandle()==31)
 							return k;
 						}
 					for(int k = 0; k < enemies.size(); k++){
 						if(enemies.get(k).getHandle()==21)
 							return k;
 						}
 					for(int k = 0; k < enemies.size(); k++){
 						if(enemies.get(k).getHandle()==11)
 							return k;
 						}
 						
 	      case 3:	for(int k = 0; k < enemies.size(); k++){
 						if(enemies.get(k).getHandle()==11)
 							return k;
 						}
 					for(int k = 0; k < enemies.size(); k++){
 						if(enemies.get(k).getHandle()==31)
 							return k;
 						}
 					for(int k = 0; k < enemies.size(); k++){
 						if(enemies.get(k).getHandle()==21)
 							return k;
 						}
      			
 		}
 	}
 		return -1;
 	}
 	public void moveAIShip(MoveableEntity e){
 		int topX = e.getLocation().getCol()-e.getMovementLeft()+1;	   
 		int topY = e.getLocation().getRow()-e.getMovementLeft()+1;
 		int currentX=topX;
 		int currentY=topY;
		int count = 0;
 		e.toggleMoveable();
 		//delay!
		e.toggleMoveable();
 	do
 		{	
 			currentX=topX;
 			currentY=topY;
 			currentX += (int) (Math.random()*((e.getMovementLeft() * 2) + 1));
 			currentY += (int) (Math.random()*((e.getMovementLeft() * 2) + 1));
			count++;
 		}
		while(!GridHelper.canMoveTo(e.getManager(), e, e.getCurrentOrientation(), currentY, currentX,e.getWidth())&&count <= 10);
 		System.out.println("I am moving to ..."+new Location(currentY,currentX));
 		e.moveTo(new Location(currentY,currentX));
 	}
 	public void determineCurrentEnemies(MoveableEntity e){
 		int topX = (e.getLocation().getCol()-e.getMovementLeft())+1;	   
 		int topY = (e.getLocation().getRow()-e.getMovementLeft())+1;	 		
 		for (int x = topX; x < (e.getLocation().getCol()+e.getMovementLeft())+1; x++) {
 			for (int y = topY; y < (e.getLocation().getRow()+e.getMovementLeft())+1; y++) {
 				Entity location = e.getManager().findEntity(y,x);
 				if(location!=null){
 					Player temp = NavalGame.getManager().getTurnManager().findPlayer(location); 
 					if (temp!=null){
 					if(!(temp.equals(this))&&!enemies.contains(location)){
 						//entity at spot is not owned by this AI
 						System.out.println("Enemies that are next to me with Location: "+ location.getLocation() );
 						addEnemyEntity(location);
 					}
 					}
 				}
 			}
 		}
 	}
 	
 	public void endTurn(){
 		super.endTurn();
 	}
 }
