 package edu.chl.codenameg.model;
 
 import edu.chl.codenameg.model.entity.Block;
 import edu.chl.codenameg.model.entity.MovingBlock;
 import edu.chl.codenameg.model.entity.PlayerCharacter;
 
 public class GameModel {
 	
 	private World activeWorld;
 	private World selectedWorld;
 	private boolean running;
 	
 	public GameModel() {
 		
 		//TODO Default world?
 		World tempWorld = new World();
 		
 		Block block = new Block();
 		block.setPosition(new Position(100,200));
 		block.setHitbox(new Hitbox(200,20));
 		
 //		MovingBlock movingblock = new MovingBlock(new Hitbox(20,20),new Position(50,50), new Position(100,100));
 		
 		PlayerCharacter pc = new PlayerCharacter();
 		pc.setPosition(new Position(200,50));
 		
 		tempWorld.add(block);
 		tempWorld.add(pc);
 //		world.add(movingblock);
 		
 		this.setWorld(tempWorld);
 	}
 	
 	public void setWorld(World w) {
 		this.selectedWorld = w;
 	}
 	
 	public void startGame() {
 		this.running = true;
 		this.activeWorld = new World(this.selectedWorld);
 	}
 	
 	public void restartGame() {
 		this.activeWorld = new World(this.selectedWorld);
 	}
 	
 	public void pauseGame(World w) {
 		this.running = false;
 	}
 	
 	public World getWorld() {
		return activeWorld;
 	}
 	
 	public void update(int elapsedTime) {
 		if(activeWorld != null && running) {
 			activeWorld.update(elapsedTime);
 		}
 	}
 	
 }
