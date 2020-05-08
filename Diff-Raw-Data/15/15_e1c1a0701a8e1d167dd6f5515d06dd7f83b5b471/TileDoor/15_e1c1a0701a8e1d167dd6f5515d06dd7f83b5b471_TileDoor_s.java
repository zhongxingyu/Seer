 package game.tile;
 
 import game.entity.Player;
 import game.screen.ScreenGame;
 
 public class TileDoor extends Tile
 {
 	int animStage = 0;
 	boolean active, vertical;
 	public String tileName = "Door";
 	int startSprite;
 	public TileDoor(boolean active, boolean vertical){
 		sprite = vertical ? 50 : 51;
 		startSprite = sprite + 16;
 		this.active = active;
 		this.vertical = vertical;
 
 	}
 
 	@Override
 	public void tick() {
 		if(active)
 		{
			animStage++;
			if(animStage >= 224)
 			{
 				sprite = 34;
 			}else
			sprite = startSprite +(16 * (animStage/16));
		}else
		{
			animStage = 0;
 		}
 	}
 }
