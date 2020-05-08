 package autobot.behavior;
 
 import org.powerbot.core.script.job.Task;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.input.Keyboard;
 import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.node.SceneEntities;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.widget.Bank;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.wrappers.Locatable;
 import org.powerbot.game.api.wrappers.Tile;
 import org.powerbot.game.api.wrappers.map.TilePath;
 
 public class Fisher extends Behavior {
 
 	private boolean canAccomplish;
 	public int errorCount;
 
 	//public static final int;
 	private TilePath tp;
 	
 	
 	/** this assumes starting from the Catherby Bank/**/
 	public Fisher(){
 		// TODO assumes at fishing spot
 		try {
 			Tile[] t = {new Tile(2838,3433, 0),new Tile(2824,3437, 0),new Tile(2810,3435, 0),new Tile(2808,3440, 0)};
 			tp = new TilePath(t);
 			canAccomplish = true;
 			System.out.println("Prepare Completed");
 
 		} catch (Exception e) {
 			canAccomplish = false;
 		}
 	}
 
 	@Override
 	public boolean canAct() {
 		if (errorCount > 5) // arbitrary number of acceptable errors
 		{
 			System.out.println("Shutting down");
 			canAccomplish = false;
 		}
 		// TODO add more checking
 		return canAccomplish;
 	}
 
 	@Override
 	public void act() {
 		try {
 			moveFromBank();
 			while(!Inventory.isFull())
 				fish();
 			moveToBank();
 			bank();
 		} catch (Exception e) {
 			errorCount++;
 			System.out.println("Caught an error: "+e);
 		}
 	}
 
 	private void fish() throws Exception {
		Walking.walk(NPCs.getNearest(320).getLocation());
		setCameraRelativeTo(NPCs.getNearest(320).getLocation());
 		NPCs.getNearest(320).click(true);
 		Task.sleep(10000,10500);
 	}
	
	public void setCameraRelativeTo(final Locatable reference) {
        final Tile playerTile = Players.getLocal().getLocation();
        final Tile referenceTile = reference.getLocation();
        if (playerTile.getX() < referenceTile.getX()) {
            Camera.setAngle(Random.nextInt(250, 270));
        } else if (playerTile.getX() > referenceTile.getX()) {
            Camera.setAngle(Random.nextInt(80, 100));
        } else if (playerTile.getY() > referenceTile.getY()) {
            Camera.setAngle(Random.nextInt(170, 180));
        } else if (playerTile.getY() < referenceTile.getY()) {
            Camera.setAngle(Random.nextInt(350, 360));
        }
}
 
 	private void moveToBank() throws Exception{
 		while(tp.validate()){
 			tp.traverse();
 			sleep(Times.SHORT);
 		}
 	}
 	
 	private void moveFromBank() throws Exception{
 		tp.reverse();
 		while(tp.validate()){
 			tp.traverse();
 			sleep(Times.SHORT);
 		}
 		tp.reverse();
 	}
 	private void bank() throws Exception {
 		if(Bank.open())
 			System.out.println("opened bank");
 		else {
 			sleep(Times.SHORT);
 			Bank.open();
 		}
 		sleep(Times.SHORT);
 		Bank.depositInventory();
 		sleep(Times.SHORT);
 		Bank.close();
 		sleep(Times.BANK);
 		
 	}
 }
