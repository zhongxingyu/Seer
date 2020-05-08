 import java.util.Random;
 
 import org.lwjgl.input.Keyboard;
 
 
 public class EntityPlayer extends Entity {
 	
 	public Direction rotation = Direction.DOWN;
 	float speed = 0.05f;
 	boolean Moving = false;
 	public EntityPlayer(MapSection mapSection) {
 		super(mapSection);
 		Random rand = new Random();
 		x = 16*rand.nextInt(20);
 		y = 16*rand.nextInt(18);
 
 	}
 	boolean nextStep = true;
 	@Override 
 	public void update(int delta)
 	{
 		if(x%16 == 0&&y%16 == 0)
 		{
 			Moving = false;
 			if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {rotation = Direction.LEFT; if (objMapSection.MoveOver((int)x/16-1,(int)y/16)) Moving = true;}
 			else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {rotation = Direction.RIGHT; if (objMapSection.MoveOver((int)x/16+1,(int)y/16)) Moving = true;}
 			else if (Keyboard.isKeyDown(Keyboard.KEY_UP))  {rotation = Direction.UP;if (objMapSection.MoveOver((int)x/16,(int)y/16-1)) Moving = true;}
 			else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {rotation = Direction.DOWN;if (objMapSection.MoveOver((int)x/16,(int)y/16+1))Moving = true;}
 			if(nextStep)
 			{
 			objMapSection.stepOn(this,(int)x/16,(int)y/16);
 			nextStep = false;
 			}
 			if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
 				if (TileLongGrass.available = true){
					TileLongGrass.catchWildMonster(TileLongGrass.currentWild);
 				}
 			}
 		}
 
 		if(Moving)
 		{
 			nextStep = true;
 			if(rotation == Direction.RIGHT)
 			{
 				
 				if(x%16 > 16f - (speed * delta))
 					x += (16 - x%16);
 				else
 				{
 					x += speed * delta;
 					
 				}
 				
 			}
 			if(rotation == Direction.LEFT)
 			{
 				
 				if(x%16 < speed * delta&&x%16 != 0 )
 					x -= x%16;
 				else
 				{
 					x -= speed * delta;
 				
 				}
 				
 				
 			}
 			if(rotation == Direction.UP)
 			{
 				
 				if(y%16 < speed * delta&&y%16 != 0)
 					y -= y%16;
 				else
 				{
 					y -= speed * delta;
 					
 				}
 				
 			}
 			if(rotation == Direction.DOWN)
 			{
 				
 				if(y%16 > 16f - (speed * delta))
 					y += (16 - y%16);
 				
 				else
 				{
 					y += speed * delta;
 					
 				}
 				
 			}
 		}
 
 
 
 
 
 		// keep quad on the screen
 		
 	}
 
 }
