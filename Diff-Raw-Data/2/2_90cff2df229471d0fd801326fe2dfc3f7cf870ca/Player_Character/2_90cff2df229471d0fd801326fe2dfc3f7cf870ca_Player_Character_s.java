 
 package zed;
 
 import org.newdawn.slick.*;
 
 /**
  *
  * @author Richard Barella Jr
  * @author Adam Bennett
  * @author Ryan Slyter
  */
 public class Player_Character extends GCharacter {
     
 	private boolean Sword_Drawn; // holds whether the sword is currently drawn
 	                             // character is currently swinging the sword
 	
 	// default initialization for Player_Character does nothing
     public Player_Character() throws SlickException{
     }
     
     // Initialize with animations defined
     public Player_Character(int tile_x, int tile_y, boolean visible, boolean solid,
     		int[] sprite_shift_x, int[] sprite_shift_y, int tilesize,
     		Animation[] animation_list, int current_animation,
     		int health, float speed,
     		int x_movement, int y_movement) throws SlickException{
     	
     	super(tile_x, tile_y, 16, 16, visible, solid, 0, sprite_shift_x, sprite_shift_y,
     			tilesize, animation_list, current_animation,
     			health, speed, x_movement, y_movement, -1);
     	
     	Sword_Drawn = false;
 
 		Hurt_Sound = new Sound("soundtrack/effects/punch.wav");
     }
 
     // The character's update function that is called every time slick updates
     public void Update(GObject[] objects, GCharacter[] npcs){
     	
     	if (Health > 0)
     	{
     		GObject x_col_object = null;
     		GObject x_col_npc = null;
     		GObject y_col_object = null;
     		GObject y_col_npc = null;
     		
 	        Update_Frame_State(); // update the current animation being played
 	                              // based on movement and attacking
 		    
 	        // check for collision based on movement values
 	        if ((x_col_object = X_Collision(objects)) == null
 	        		&& (x_col_npc = X_Collision(npcs)) == null
 	        		&& !X_Out_Of_Bounds()) // TODO: maybe make Collision_Up, Collision_Left,
 	        	                         //       Collision_Down, Collision_Right functions
 	        	                         //       to allow character to slide against walls
 	        	                         //       or we can just return what object the
 	        	                         //       character collided with and tell which
 	        	                         //       direction the character collided based on that.
 		    {
 		    	// update the character's position based on movement values
 		        Update_X_Position();
 		    }
 	        if ((y_col_object = Y_Collision(objects)) == null
 	        		&& (y_col_npc = Y_Collision(npcs)) == null
 	        		&& !Y_Out_Of_Bounds())
 	        {
 	        	Update_Y_Position();
 	        }
 	        // check for collision with npc based on movement values
 	        if ((x_col_object != null && x_col_object.Get_Damage() != 0)
 	        		|| (x_col_npc != null && x_col_npc.Get_Damage() != 0)
 	        		|| (y_col_object != null && y_col_object.Get_Damage() != 0)
 	        		|| (y_col_npc != null && y_col_npc.Get_Damage() != 0))
 	        {
 	        	// damage the character if hits an npc
 	        	if (x_col_object != null
 	        			&& (x_col_npc == null || x_col_object.Get_Damage() < x_col_npc.Get_Damage())
 	        			&& (y_col_object == null || x_col_object.Get_Damage() < y_col_object.Get_Damage())
 	        			&& (y_col_npc == null || x_col_object.Get_Damage() < y_col_npc.Get_Damage()))
 	        		Decrease_Health(x_col_object.Get_Damage());
 	        	else if (x_col_npc != null
 	        			&& (y_col_object == null || x_col_npc.Get_Damage() < y_col_object.Get_Damage())
 	        			&& (y_col_npc == null || x_col_npc.Get_Damage() < y_col_npc.Get_Damage()))
 	        		Decrease_Health(x_col_npc.Get_Damage());
 	        	else if (y_col_object != null
 	        			&& (y_col_npc == null || y_col_object.Get_Damage() < y_col_npc.Get_Damage()))
 	        		Decrease_Health(y_col_object.Get_Damage());
 	        	else
 	        		Decrease_Health(y_col_npc.Get_Damage());
 	        }
     	}
     	else
     	{
     		Visible = false;
     		Solid = false;
     	}
     }
     
     // change the current movement values (-1, 0, 1)
     public void New_Movement(int new_x_mov, int new_y_mov){
         
         X_Movement = new_x_mov;
         Y_Movement = new_y_mov;
     }
     
     // starts the sword attack
     // and sets the correct animation to play
     public void Start_Sword_Attack(){
     	
     	if (Current_Animation == Animation_List[0] || Current_Animation == Animation_List[4]) // attack up
     	{
     		Change_Animation(8);
     		Current_Animation.start();
     		Sword_Drawn = true;
     	}
     	else if (Current_Animation == Animation_List[1] || Current_Animation == Animation_List[5]) // Attack left
     	{
     		Change_Animation(9);
     		Current_Animation.start();
     		Sword_Drawn = true;
     	}
     	else if (Current_Animation == Animation_List[2] || Current_Animation == Animation_List[6]) // Attack down
     	{
     		Change_Animation(10);
     		Current_Animation.start();
     		Sword_Drawn = true;
     	}
     	else if (Current_Animation == Animation_List[3] || Current_Animation == Animation_List[7])
     	{
     		Change_Animation(11);
     		Sword_Drawn = true;
     	}
     }
     
     // ends the sword attack
     // and selects the correct animation to play
     public void End_Sword_Attack(){
     	
     	Sword_Drawn = false;
     	
     	if (Current_Animation.isStopped())
     	{
 	    	if (Current_Animation == Animation_List[8]) // Stop attacking up
 	    	{
 	    		Current_Animation.restart();
 	    		Change_Animation(0);
 	    	}
 	    	else if (Current_Animation == Animation_List[9]) // Stop attacking left
 	    	{
 	    		Current_Animation.restart();
 	    		Change_Animation(1);
 	    	}
 	    	else if (Current_Animation == Animation_List[10]) // Stop attacking down
 	    	{
 	    		Current_Animation.restart();
 	    		Change_Animation(2);
 	    	}
 	    	else if (Current_Animation == Animation_List[11]) // Stop attacking right
 	    	{
 	    		Current_Animation.restart();
 	    		Change_Animation(3);
 	    	}
     	}
     }
     
     // updates the current frame being displayed based on movement values
     void Update_Frame_State(){
     	
    	if (Sword_Drawn == false)
     	{
     		super.Update_Frame_State();
     	}
     }
     
     // gets the position of the point of the sword.
     // If sword isn't drawn, returns -1
     public int Get_Sword_Pos_X(){
     	
     	if (Sword_Drawn && Current_Animation.getFrame() == 1)
     	{
     		if (Current_Animation == Animation_List[8]) // attack up
     		{
     			return X_Position + 7;
     		}
     		else if (Current_Animation == Animation_List[9]) // attack left
     		{
     			return X_Position - 15;
     		}
     		else if (Current_Animation == Animation_List[10]) // attack down
     		{
     			return X_Position + 7;
     		}
     		else if (Current_Animation == Animation_List[11]) // attack right
     		{
     			return X_Position + 15 + 15;
     		}
     	}
     	return -1;
     }
     
     // gets the position of the point of the sword.
     // If the sword isn't drawn, returns -1
     public int Get_Sword_Pos_Y(){
     	
     	if (Sword_Drawn && Current_Animation.getFrame() == 1)
     	{
     		if (Current_Animation == Animation_List[8]) // attack up
     		{
     			return Y_Position - 15;
     		}
     		else if (Current_Animation == Animation_List[9]) // attack left
     		{
     			return Y_Position + 7;
     		}
     		else if (Current_Animation == Animation_List[10]) // attack down
     		{
     			return Y_Position + 15 + 15;
     		}
     		else if (Current_Animation == Animation_List[11]) // attack right
     		{
     			return Y_Position + 7;
     		}
     	}
     	return -1;
     }
     
     public int Get_Type(){
     	
     	return -1;
     }
 }
