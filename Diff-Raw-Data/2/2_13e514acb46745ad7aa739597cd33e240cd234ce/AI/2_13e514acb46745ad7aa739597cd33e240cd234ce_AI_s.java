 package com.jpii.navalbattle.game.turn;
 
 import java.util.ArrayList;
 
 import com.jpii.navalbattle.game.NavalGame;
 import com.jpii.navalbattle.game.NavalManager;
 import com.jpii.navalbattle.pavo.grid.Entity;
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
 				if(currentEntity.getHandle()==11){
 					//Sub
 					determineCurrentEnemies(currentEntity);
 					pickEnemy(1);
 				}
 				if(currentEntity.getHandle()==21){
 					//AC
 				determineCurrentEnemies(currentEntity);
 				pickEnemy(2);
 				}
 				if(currentEntity.getHandle()==31){
 					//BS
 					determineCurrentEnemies(currentEntity);
 					pickEnemy(3);
 				}
 			}
 			
 		}
 		turnOver=true;
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
 	
 	public void determineCurrentEnemies(MoveableEntity e)
 	{
 		for (int x = 0; x < (e.getMovementLeft() * 2) + 1; x++) {
 			for (int y = 0; y < (e.getMovementLeft() * 2) + 1; y++) {
				Entity location = e.getManager().findEntity(e.getRLR(y), e.getCLR(x));
 				if(location!=null){
 					Player temp = NavalGame.getManager().getTurnManager().findPlayer(location); 
 					if(!(temp.equals(this))&&!enemies.contains(location)){
 						//entity at spot is not owned by this AI
 						addEnemyEntity(location);
 					}
 				}
 			}
 		}
 	}
 	
 	public void endTurn(){
 		super.endTurn();
 	}
 }
