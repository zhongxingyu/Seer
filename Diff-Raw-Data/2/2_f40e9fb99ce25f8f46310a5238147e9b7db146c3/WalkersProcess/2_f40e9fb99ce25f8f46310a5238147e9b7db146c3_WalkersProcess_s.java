 package com.mel.wallpaper.starWars.process;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.andengine.util.debug.Debug;
 
 import com.mel.entityframework.Game;
 import com.mel.entityframework.Process;
 import com.mel.util.Point;
 import com.mel.wallpaper.starWars.entity.Jumper;
 import com.mel.wallpaper.starWars.entity.Map;
 import com.mel.wallpaper.starWars.entity.Walker;
 import com.mel.wallpaper.starWars.entity.commands.Command;
 import com.mel.wallpaper.starWars.entity.commands.MoveCommand;
 import com.mel.wallpaper.starWars.entity.commands.ShootLaserCommand;
 
 
 
 public class WalkersProcess extends Process
 {
 	private Map map;
 	private List<Walker> jedis;
 	
 	
 	public WalkersProcess(Map partido){
 	}
 	
 	@Override
 	public void onAddToGame(Game game){
 		this.map = (Map)game.getEntity(Map.class);
 
 		this.jedis = (List<Walker>) game.getEntities(Jumper.class);
 		this.jedis.addAll(game.getEntities(Walker.class));
 	}
 	
 	@Override
 	public void onRemoveFromGame(Game game){
 		if(jedis != null){
 			jedis.clear();
 			jedis = null;
 		}
 		
 		this.map = null;
 	}
 	
 	@Override
 	public void update(){
 		//TODO:add commands to each player
 	
 		for(Walker jedi : jedis) {
 			
 			if(jedi.hasDestination())
 				continue;
 			
 			MoveCommand move = new MoveCommand(jedi);
 			move.setMovable(jedi);
 			move.destination = map.walls.getRandomPoint();
 		}
 	
 		Walker jedi = jedis.get(0);
 		
		ShootLaserCommand laser = new ShootLaserCommand(jedi);
 		
 		executeCommandsByRandomPlayer();	
 	}
 
 	private void executeCommandsByRandomPlayer(){
 		Collections.shuffle(this.jedis);
 		for(Walker player:this.jedis){
 			for(Command c:player.pendingCommands){
 				c.execute(this.map);
 			}
 			player.clearPendingCommands();
 		}
 	}
 	
 	
 	
 }
