 package zed;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.SpriteSheet;
 
 public class Zombie extends GCharacter {
 
 	public Zombie(){
 		
 		
 	}
 	// Zombie constructor
 	public Zombie(int tile_x, int tile_y, SpriteSheet sprites) {
 
 		Animation[] zombie_animations = new Animation[4];
 		
 		int i0 = 10;
 		int[] spritesheet_index = {i0, i0 + 1, i0 + 2, i0 + 3};
 		int[] animation_length = {3, 3, 3, 3};
 		boolean[] looping = {true, true, true, true};
 		int[] sprite_shift_x = {0, 0, 0, 0};
 		int[] sprite_shift_y = {16, 16, 16, 16};
 		
         for (int i = 0; i < 4; i++) // initialize walking and standing animations
         {
 	        zombie_animations[i] = new Animation(sprites, // spritesheet to use
 	        		// location of first sprite in spritesheet
 	                0,                       spritesheet_index[i],
 	                // location of last sprite in spritesheet
 	                animation_length[i] - 1, spritesheet_index[i],
 	                false, // horizontalScan value
 	                200, // speed value for animation
 	                true // autoupdate value
 	                );
 	        zombie_animations[i].setLooping(true); // intialize whether animation loops
 	        zombie_animations[i].setPingPong(true); // initialize whether animation ping-pongs between last and first frame
         }
         
         FRAME_STATE_UP = 3; FRAME_STATE_DOWN = 0;
         FRAME_STATE_LEFT = 1; FRAME_STATE_RIGHT = 2;
         FRAME_STATE_UP_WALK = 3; FRAME_STATE_DOWN_WALK = 0;
         FRAME_STATE_LEFT_WALK = 1; FRAME_STATE_RIGHT_WALK = 2;
 		
         // Initialize zombie based on constructor
 		super.Init(tile_x, tile_y, true, true,
 				sprite_shift_x, sprite_shift_y, 16, zombie_animations,
 				0, 3, 1.0f, 0, 0);
 		
 		AI_State_Change_Time = 500;
 	}
 	
 	public void Artificial_Intelligence(boolean collision, Player_Character player)
 	{
 		int x_distance = player.Get_X_Position() - X_Position;
 		int y_distance = player.Get_Y_Position() - Y_Position;
		if (x_distance*x_distance + y_distance*y_distance > 16*5*16*5
				|| !player.Visible)
 		{
 			super.Artificial_Intelligence(collision, player);
 		}
 		else
 		{
 			if (x_distance*x_distance > y_distance*y_distance)
 			{
 				if (x_distance > 0) {X_Movement = 1;}
 				else                {X_Movement = -1;}
 				Y_Movement = 0;
 			}
 			else // y_distance >= x_distance
 			{
 				if (y_distance > 0) {Y_Movement = 1;}
 				else                {Y_Movement = -1;}
 				X_Movement = 0;
 			}
 		}
 	}
 }
