 
 package zed;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.SpriteSheet;
 
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /*
  * @author Richard Barella Jr.
  * @author Adam Bennett
  * @author Ryan Slyter
  */
 public class GCharacter extends GObject {
     
     // index locations for each walking state within Animation_List are defined for Character
 	// Animation_List might be created before or after initialization
     int FRAME_STATE_UP = 0;
     int FRAME_STATE_LEFT = 1;
     int FRAME_STATE_DOWN = 2;
     int FRAME_STATE_RIGHT = 3;
     int FRAME_STATE_UP_WALK = 4;
     int FRAME_STATE_LEFT_WALK = 5;
     int FRAME_STATE_DOWN_WALK = 6;
     int FRAME_STATE_RIGHT_WALK = 7;
     
     final int INVINCIBILITY_TIME = 1000;
     int STUN_TIME = 200;
     
     int Health; // current health for Character
     int Max_Health; // maximum health for Character
     float Speed; // tiles per second
     int X_Movement; // {-1, 0, 1} tells movement direction in x-axis
     int Y_Movement; // {-1, 0, 1} tells movement direction in y-axis
     
     long x_last_move; // time in nanoseconds of last movement
     long y_last_move;
     long last_damage; // time in milliseconds of last time was damaged
     
     int AI_State; // For use in Artificial_Intelligence function
     long Last_AI_State_Change; // For use in Artificial_Intelligence function
     long AI_State_Change_Time;
     Random rnd = new Random();
     
     Sound Hurt_Sound = null;
     
     // Constructor for character that makes use of SpriteSheet to construct its
     // Animation array
     public GCharacter(
     		int tile_x, // initial x position of the character w.r.t. the game tiles
     		int tile_y, // initial y position of the character w.r.t. the game tiles
     		int width, // initial width in pixels
     		int height, // initial height in pixels
     		boolean visible, // initialize whether the character is visible
     		boolean solid, // initializes whether its solid
     		int damage, // initialize damage to the player
             int[] sprite_shift_x, // by how many pixels each animation is shifted in x direction
             int[] sprite_shift_y, // by how many pixels each animation is shifted in y direction
             int tilesize, // Give the character how large each tile is
             SpriteSheet sprites, // Give the character the spritesheet file for fetching its animation frames
             int[] spritesheet_index, // Give the character the indexes for the rows of the SpriteSheet to fetch each animation from
                                      // index are defined {up,left,down,right,upwalk,leftwalk,downwalk,rightwalk}
             int[] animation_length, // Give the character the length of each animation
             boolean[] looping, // tell which animations are looping
             int current_animation, // intialize which animation to start out with
             int health, // intialize the character's max_health and health
             float speed, // Give the character its speed in tiles per second 
             int x_movement, // Give the character its initial x_movement value (-1, 0, 1)
             int y_movement, // Give the character its initial y_movement value (-1, 0, 1)
             int type
             ) throws SlickException {
         
         // Constructs the "Object" part of Character
         super(tile_x, tile_y, width, height, visible, solid, damage,
         		sprite_shift_x, sprite_shift_y, tilesize, sprites,
                 spritesheet_index, animation_length, looping, current_animation);
         
         // Initialze health
         Health = health;
         Max_Health = health;
         
         // Initialize movement parameters
         Speed = speed;
         X_Movement = x_movement;
         Y_Movement = y_movement;
         
         // Initialize movement
         x_last_move = System.nanoTime();
         y_last_move = System.nanoTime();
         // Initialize damage timer
         last_damage = System.currentTimeMillis();
         
         // Initialize Artificial Intelligence
         AI_State = 0;
         Last_AI_State_Change = System.currentTimeMillis();
         AI_State_Change_Time = 200;
     }
     
     // Constructor for Character that takes an already defined Animation array
     public GCharacter(
     		int tile_x, // initial x position of the character w.r.t. the game tiles
     		int tile_y, // initial y position of the character w.r.t. the game tiles
     		int width, // initial width in pixels
     		int height, // initial height in pixels
     		boolean visible, // initialize whether the character is visible
     		boolean solid, // initialize solidity for collision
     		int damage, // initialize whether it damages the player
             int[] sprite_shift_x, // by how many pixels each animation is shifted in x direction
             int[] sprite_shift_y, // by how many pixels each animation is shifted in y direction
             int tilesize, // Give the character how large each tile is
     		Animation[] animation_list, // Give the character the animations it will be using
             int current_animation, // initialize which animation to start out with
             int health, // initialize the character's max_health and health
             float speed, // Give the character its speed in tiles per second 
             int x_movement, // Give the character its initial x_movement value (-1, 0, 1)
             int y_movement, // Give the character its initial y_movement value (-1, 0, 1)
             int type
     		) throws SlickException{
     	
     	super(tile_x, tile_y, width, height, visible, solid, damage, sprite_shift_x, sprite_shift_y,
     			tilesize, animation_list, current_animation);
     	
         // Initialze health
         Health = health;
         Max_Health = health;
         
         // Initialize movement parameters
         Speed = speed;
         X_Movement = x_movement;
         Y_Movement = y_movement;
         
         // Initialize movement
         x_last_move = System.nanoTime();
         y_last_move = System.nanoTime();
         // Initialize damage timer
         last_damage = System.currentTimeMillis();
         
         // Initialize Artificial Intelligence
         AI_State = 0;
         Last_AI_State_Change = System.currentTimeMillis();
     }
 
     // Default Constructor sets everything to equal either 0 or null
     public GCharacter() throws SlickException {
         x_last_move = System.nanoTime();
         y_last_move = System.nanoTime();
         last_damage = System.currentTimeMillis();
     }
     
     // frames cannot have array size less than 4x5 because these
     // are the frames that Character is using.
     
     // Initialization given SpriteSheet
     public void Init(
     		int tile_x, // initial x position of the character w.r.t. the game tiles
     		int tile_y, // initial y position of the character w.r.t. the game tiles
     		int width, // initial width in pixels
     		int height, // initial height in pixels
     		boolean visible, // initialize whether the character is visible
     		boolean solid, // initialize solidity for collision
     		int damage, // initialize whether it damages player
             int[] sprite_shift_x, // by how many pixels each animation is shifted in x direction
             int[] sprite_shift_y, // by how many pixels each animation is shifted in y direction
             int tilesize, // Give the character how large each tile is
             SpriteSheet sprites, // Give the character the spritesheet file for fetching its animation frames
             int[] spritesheet_index, // Give the character the indexes for the rows of the SpriteSheet to fetch each animation from
                                      // index are defined {up,left,down,right,upwalk,leftwalk,downwalk,rightwalk}
             int[] animation_length, // Give the character the length of each animation
             boolean[] looping, // tell which animations are looping
             int current_animation, // intialize which animation to start out with
             int health, // intialize the character's health
             int max_health, // intialize the character's max_health
             float speed, // Give the character its speed in tiles per second 
             int x_movement, // Give the character its initial x_movement value (-1, 0, 1)
             int y_movement // Give the character its initial y_movement value (-1, 0, 1)
             ) throws SlickException{
         
     	// Initialize object part of character
         Init(tile_x, tile_y, width, height, visible, solid, damage,
         		sprite_shift_x, sprite_shift_y, tilesize, sprites,
                 spritesheet_index, animation_length, looping, current_animation);
         
         // initialize character part of character
         Health = health;
         Max_Health = max_health;
         Speed = speed;
         X_Movement = x_movement;
         Y_Movement = y_movement;
 
         x_last_move = System.nanoTime();
         y_last_move = System.nanoTime();
     }
     
     public void Init(
     		int tile_x, // initial x position of the character w.r.t. the game tiles
     		int tile_y, // initial y position of the character w.r.t. the game tiles
     		int width, // initial width in pixels
     		int height, // initial height in pixels
     		boolean visible, // initialize whether the character is visible
     		boolean solid, // initialize solidity for collision
     		int damage, // initialize whether it damages the player
             int[] sprite_shift_x, // by how many pixels each animation is shifted in x direction
             int[] sprite_shift_y, // by how many pixels each animation is shifted in y direction
             int tilesize, // Give the character how large each tile is
     		Animation[] animation_list, // Give the character the animations it will be using
             int current_animation, // initialize which animation to start out with
             int health, // initialize the character's max_health and health
             float speed, // Give the character its speed in tiles per second 
             int x_movement, // Give the character its initial x_movement value (-1, 0, 1)
             int y_movement // Give the character its initial y_movement value (-1, 0, 1)
             ) throws SlickException{
     
 	    super.Init(tile_x, tile_y, width, height, visible, solid, damage, 
 	    		sprite_shift_x, sprite_shift_y,
 				tilesize, animation_list, current_animation);
 		
 	    // Initialze health
 	    Health = health;
 	    Max_Health = health;
 	    
 	    // Initialize movement parameters
 	    Speed = speed;
 	    X_Movement = x_movement;
 	    Y_Movement = y_movement;
 	    
 	    // Initialize movement
         x_last_move = System.nanoTime();
         y_last_move = System.nanoTime();
 	    // Initialize damage timer
 	    last_damage = System.currentTimeMillis();
 	    
 	    // Initialize Artificial Intelligence
 	    AI_State = 0;
 	    Last_AI_State_Change = System.currentTimeMillis();
     }
     
     // Updates the Character's position based on artificial intelligence and collision
     public void Update(GObject[] collision_objects, GCharacter[] npcs, Player_Character player){
     	
     	if (Health > 0)
     	{
 	        Update_Frame_State();
 	        GObject[] cplayer = {player};
 	        boolean x_col_obj = (X_Collision(collision_objects) != null); // tell whether character has collided with an object
 	        boolean x_col_npc = (X_Collision(npcs) != null); // tell whether character has collided with another collidable npc
 	        boolean x_col_bnd = (X_Out_Of_Bounds());
 	        boolean x_col_plr = (X_Collision(cplayer) != null);
 	        boolean x_col = x_col_obj | x_col_npc | x_col_bnd | x_col_plr;
 	        boolean y_col_obj = (Y_Collision(collision_objects) != null); // tell whether character has collided with an object
 	        boolean y_col_npc = (Y_Collision(npcs) != null); // tell whether character has collided with another collidable npc
 	        boolean y_col_bnd = (Y_Out_Of_Bounds());
 	        boolean y_col_plr = (Y_Collision(cplayer) != null);
 	        boolean y_col = y_col_obj | y_col_npc | y_col_bnd | y_col_plr;
 	        boolean col = x_col | y_col;
 	        
 	        if ((x_col_plr | y_col_plr) && !(x_col_obj | y_col_obj) && Damage != 0)
 	        {
 	        	player.Decrease_Health(Damage);
 	        }
 	        if (System.currentTimeMillis() > last_damage + STUN_TIME
 	        		&& (!X_Out_Of_Bounds() || !Y_Out_Of_Bounds()))
 	        {
 	        	if (!x_col)
 	        		Update_X_Position(); // can move if there is something to collide with
 	        	if (!y_col)
 	        		Update_Y_Position();
 	        }
	        if (Pixel_Contained(player.Get_Sword_Pos_X(), player.Get_Sword_Pos_Y()))
 	        {
 	        	Decriment_Health(); // kills this character with a sword
 	        }
 	        
 	        Artificial_Intelligence(col, player); // Proceed with AI code to update the character's movement values
     	}
     	else
     	{
     		Solid = false;
     		Visible = false;
     		// TODO: check back later if next line should be uncommented
     		//Damage = 0;
     	}
     }
     
     // can tell if GCharacter will collide with another object or not
     // and returns the object GCharacter collides with
     GObject X_Collision(GObject collision_objects[]){
         
     	if (collision_objects != null)
     	{
 	        for (int i = 0; i < collision_objects.length; i++)
 	        {
 		        if (collision_objects[i] != null && collision_objects[i] != this && collision_objects[i].Solid)
 		        {
 		        	if (X_Collision(collision_objects[i]))
 		        		return collision_objects[i];
 		        }
 	        }
     	}
         return null; // didn't collide
     }
     // can tell if GCharacter will collide with another object or not
     // and returns the object GCharacter collides with
     GObject Y_Collision(GObject collision_objects[]){
         
     	if (collision_objects != null)
     	{
 	        for (int i = 0; i < collision_objects.length; i++)
 	        {
 		        if (collision_objects[i] != null && collision_objects[i] != this && collision_objects[i].Solid)
 		        {
 		        	if (Y_Collision(collision_objects[i]))
 		        		return collision_objects[i];
 		        }
 	        }
     	}
         return null; // didn't collide
     }
     
     // can tell if pixel is within GCharacter or not in x direction of movement
     boolean X_Collision(int x, int y){
     	
     	if (x >= X_Position + X_Movement
     			&& x < X_Position + X_Movement + Width
     			&& y >= Y_Position && y < Y_Position + Height)
     	{
     		return true;
     	}
     	return false;
     }
     // can tell if pixel is within GCharacter or not in y direction of movement
     boolean Y_Collision(int x, int y){
     	
     	if (x >= X_Position && x < X_Position + Width
     			&& y >= Y_Position + Y_Movement
     			&& y < Y_Position + Y_Movement + Height)
     	{
     		return true;
     	}
     	return false;
     }
     
     boolean X_Collision(GObject col){
     	
     	if (X_Movement > 0)
     	{
     		if (X_Collision(col.Get_X_Position(), col.Get_Y_Position()))
     			return true;
     		if (X_Collision(col.Get_X_Position(), col.Get_Y_Position() + col.Width - 1))
     			return true;
     		if (col.Pixel_Contained(X_Position + Width, Y_Position))
     			return true;
     		if (col.Pixel_Contained(X_Position + Width, Y_Position + Height - 1))
     			return true;
     	}
     	if (X_Movement < 0)
     	{
     		if (X_Collision(col.Get_X_Position() + col.Width - 1, col.Get_Y_Position()))
     			return true;
     		if (X_Collision(col.Get_X_Position() + col.Width - 1, col.Get_Y_Position() + col.Height - 1))
     			return true;
     		if (col.Pixel_Contained(X_Position - 1, Y_Position))
     			return true;
     		if (col.Pixel_Contained(X_Position - 1, Y_Position + Height - 1))
     			return true;
     	}
     	return false;
     }
     boolean Y_Collision(GObject col){
     	
     	if (Y_Movement > 0)
     	{
     		if (Y_Collision(col.Get_X_Position(), col.Get_Y_Position()))
     			return true;
     		if (Y_Collision(col.Get_X_Position() + col.Width - 1, col.Get_Y_Position()))
     			return true;
     		if (col.Pixel_Contained(X_Position, Y_Position + Height))
     			return true;
     		if (col.Pixel_Contained(X_Position + Width - 1, Y_Position + Height))
     			return true;
     	}
     	if (Y_Movement < 0)
     	{
     		if (Y_Collision(col.Get_X_Position(), col.Get_Y_Position() + col.Height - 1))
     			return true;
     		if (Y_Collision(col.Get_X_Position() + col.Width - 1, col.Get_Y_Position() + col.Height - 1))
     			return true;
     		if (col.Pixel_Contained(X_Position, Y_Position - 1))
     			return true;
     		if (col.Pixel_Contained(X_Position + Width - 1, Y_Position - 1))
     			return true;
     	}
     	return false;
     }
     
     // check to see if GCharacter goes out of screen in x direction
     boolean X_Out_Of_Bounds(){
 
         if (X_Position + Width + X_Movement > Tilesize*20 || X_Position + X_Movement < 0)
         {
         	return true; // can't go out of bounds
         }
         return false;
     }
     // check to see if GCharacter goes out of screen in y direction
     boolean Y_Out_Of_Bounds(){
 
         if (Y_Position + Width + Y_Movement > Tilesize*15 || Y_Position + Y_Movement < 0)
         {
         	return true; // can't go out of bounds
         }
         return false;
     }    
     
     // updates the current animation being played based on current movement
     void Update_Frame_State(){
         
     	// if walking up or down
         if (Y_Movement > 0) {
             Change_Animation(FRAME_STATE_DOWN_WALK); // walk down
         }
         
         else if (Y_Movement < 0) {
         	Change_Animation(FRAME_STATE_UP_WALK); // walk up
         }
         
         else // if not walking up or down
         {
             if (X_Movement > 0) {
                 Change_Animation(FRAME_STATE_RIGHT_WALK); // walk right
             }
             
             else if (X_Movement < 0) {
             	Change_Animation(FRAME_STATE_LEFT_WALK); // walk left
             }
             
             else // if not walking
             {
                 if (Current_Animation == Animation_List[FRAME_STATE_UP_WALK]) {
                 	Change_Animation(FRAME_STATE_UP); // face up
                 }
                 else if (Current_Animation == Animation_List[FRAME_STATE_LEFT_WALK]) {
                     Change_Animation(FRAME_STATE_LEFT); // face left
                 }
                 else if (Current_Animation == Animation_List[FRAME_STATE_DOWN_WALK]) {
                     Change_Animation(FRAME_STATE_DOWN); // face down
                 }
                 else if (Current_Animation == Animation_List[FRAME_STATE_RIGHT_WALK]) {
                     Change_Animation(FRAME_STATE_RIGHT); // face right
                 }
             }
         }
     }
     
     // Updates current x position based on movement
     // (do not call this function without checking for collision first)
     void Update_X_Position(){
         
         // Move Horizontally
         if (X_Movement != 0)
         {
             if (System.nanoTime() >= x_last_move
                 + (long)(1000000000.0/(Speed*0.70710678118*Tilesize))) // wait until right time
             {
                 X_Position += X_Movement; // update X_Position
             
                 x_last_move = System.nanoTime(); // record the time of last movement
             }
         }
     }
     
     // Updates current y position based on movement
     // (do not call this function without checking for collision first)
     void Update_Y_Position(){
         
         // Move Horizontally
         if (Y_Movement != 0)
         {
             if (System.nanoTime() >= y_last_move
                 + (long)(1000000000.0/(Speed*0.70710678118*Tilesize))) // wait until right time
             {
                 Y_Position += Y_Movement; // update X_Position
             
                 y_last_move = System.nanoTime(); // record the time of last movement
             }
         }
     }
     
     // Get the current health of the character
     public int Get_Health(){
         
         return Health;
     }
     
     // Decriment the current health
     public void Decriment_Health(){
         
     	if (System.currentTimeMillis() > last_damage + INVINCIBILITY_TIME)
     	{
 	    	if (Health > 0) // no decrimenting past 0
 	    	{
 	    		Health--; // decriment health
 	    		if (Hurt_Sound != null) {Hurt_Sound.play();}
 	    	}
 	    	else
 	    	{
 	    		Health = 0; // safeguard in case health is below 0
 	    	}
 	    	last_damage = System.currentTimeMillis();
     	}
     }
     
     // decrease health in chunks to save performance
     // if damage dealt needs more decriments
     public void Decrease_Health(int health_dec){
     	
     	if (System.currentTimeMillis() > last_damage + INVINCIBILITY_TIME)
     	{
 	    	if (Health - health_dec < 0) // don't decrease below 0
 	    	{
 	    		Health = 0; // safeguard in case decreased below 0
 	    	}
 	    	else if (Health - health_dec > Max_Health)
 	    	{
 	    		Health = Max_Health;
 	    	}
 	    	else
 	    	{
 	    		Health -= health_dec; // decrease health
 	    		if (Hurt_Sound != null && health_dec > 0)
 	    		{
 	    			Hurt_Sound.play();
 	    		}
 	    	}
 	    	last_damage = System.currentTimeMillis();
     	}
     }
     
     // Reset character's health back to full health
     public void Reset_Health(){
     	
     	Health = Max_Health;
     	last_damage = System.currentTimeMillis();
     }
     
     // Incrase the character's health by a certain amount
     public void Increase_Health(int health_inc){
     	
     	if (Health + health_inc >= Max_Health) // don't increase more than maximum
     	{
     		Health = Max_Health; // safeguard to ensure health doesn't exceed maximum
     	}
     	else
     	{
     		Health = Health + health_inc; // increase health
     	}
     }
     
     // Get the maximum health of the character
     public int Get_Max_Health(){
         
         return Max_Health;
     }
     
     // Get the speed (in tiles per second) of the character
     public float Get_Speed(){
         
         return Speed;
     }
     
     // Get the current movement on the x axis of the character
     // can be {-1, 0, 1}
     public int Get_X_Movement(){
         
         return X_Movement;
     }
     // Get the current movement on the y axis of the character
     // can be {-1, 0, 1}
     public int Get_Y_Movement(){
         
         return Y_Movement;
     }
     
     public void Set_Movement(int x_movement, int y_movement){
     	
     	X_Movement = x_movement;
     	Y_Movement = y_movement;
     }
     
     // The default artificial intelligence code for the character
     // that updates the character's movement values
     public void Artificial_Intelligence(
     		boolean collision, // so the character knows if it collided with something
     		Player_Character player){ 
         
     	if (System.currentTimeMillis() > Last_AI_State_Change + AI_State_Change_Time)
     	{
     		if (rnd.nextBoolean())
     		{
     			Y_Movement = 0;
 		    	if (rnd.nextBoolean())
 		    	{
 		    		X_Movement = 1;
 		    	}
 		    	else
 		    	{
 		    		X_Movement = -1;
 		    	}
     		}
     		else
     		{
     			X_Movement = 0;
 		    	if (rnd.nextBoolean())
 		    	{
 		    		Y_Movement = 1;
 		    	}
 		    	else
 		    	{
 		    		Y_Movement = -1;
 		    	}
     		}
 	    	Last_AI_State_Change = System.currentTimeMillis();
     	}
     }
     
     public void Render(int zoom,
             int current_tile_x, int current_tile_y, // position of tiles
             GameContainer gc, Graphics g){
     	
     	super.Render(zoom, current_tile_x, current_tile_y, gc, g);
     }
     
     public int Get_Type(){
     	
     	return -1;
     }
     
     public static void main(String[] args) throws SlickException{
 
 		// Initialize values for GCharacter test
 		GCharacter t = new GCharacter();
 		t.X_Position = 32; t.Y_Position = 32; t.Width = 16; t.Height = 16;
 		t.Visible = true; t.Solid = true; t.Damage = 1;
 		t.Sprite_Shift_X = null; t.Sprite_Shift_Y = null;
 		t.Current_Animation = null; t.Animation_List = null;
 		t.Tilesize = 16; t.Current_Animation_Index = 0;
 		t.Health = 4; t.Max_Health = 5; t.Speed = 3.0f;
 		t.X_Movement = 0; t.Y_Movement = 0;
 		
 		// ====================================================================
 		// Conduct X_Collision test with input=null
 		// ====================================================================
 		GObject obj = null;
 		if (t.X_Collision(obj) == false)
 			System.out.print("X_Collision(null) success\n");
 		else
 			System.out.print("X_Collision(null fail\n");
 		
 		// ====================================================================
 		// Conduct Y_Collision test with input=null
 		// ====================================================================
 		if (t.Y_Collision(obj) == false)
 			System.out.print("Y_Collision(null) success\n");
 		else
 			System.out.print("Y_Collision(null) fail\n");
 		
 		// ====================================================================
 		// Conduct X_Collision test with input=obj where obj is far away
 		// ====================================================================
 		obj = new GObject();
 		obj.X_Position = 100; obj.Y_Position = 100;
 		obj.Width = 16; obj.Height = 16;
 		obj.Tilesize = 16;
 		
 		if (t.X_Collision(obj) == false)
 			System.out.print("X_Collision(far away obj) success\n");
 		else
 			System.out.print("X_Collision(far away obj) fail\n");
 		
 		// ====================================================================
 		// Conduct Y_Collision test with input=obj where obj is far away
 		// ====================================================================
 		if (t.Y_Collision(obj) == false)
 			System.out.print("Y_Collision(far away obj) success\n");
 		else
 			System.out.print("Y_Collision(far away obj) fail\n");
 		
 		// ====================================================================
 		// Conduct X_Collision test with input=obj where obj is one tile away
 		// ====================================================================
 		obj.X_Position = t.X_Position + 16;
 		obj.Y_Position = t.Y_Position;
 		obj.Solid = true;
 		t.X_Movement = 1; t.Y_Movement = 0;
 		if (t.X_Collision(obj) == true)
 			System.out.print("X_Collision(close obj) success 1\n");
 		else
 			System.out.print("X_Collision(close obj) fail 1\n");
 		t.X_Movement = 0;
 		if (t.X_Collision(obj) == false)
 			System.out.print("X_Collision(close obj) success 2\n");
 		else
 			System.out.print("X_Collision(close obj) fail 2\n");
 
 		// ====================================================================
 		// Conduct Y_Collision test with input=obj where obj is one tile away
 		// ====================================================================
 		obj.X_Position = t.X_Position;
 		obj.Y_Position = t.Y_Position + 16;
 		obj.Solid = true;
 		t.X_Movement = 0; t.Y_Movement = 1;
 		if (t.Y_Collision(obj) == true)
 			System.out.print("Y_Collision(close obj) success 1\n");
 		else
 			System.out.print("Y_Collision(close obj) fail 1\n");
 		t.Y_Movement = 0;
 		if (t.Y_Collision(obj) == false)
 			System.out.print("Y_Collision(close obj) success 2\n");
 		else
 			System.out.print("Y_Collision(close obj) fail 2\n");
 		
 		// ====================================================================
 		// Conduct X_Collision test where input is pixel within GCharacter
 		// ====================================================================
 		t.X_Movement = 0; t.Y_Movement = 0;
 		if (t.X_Collision(t.X_Position + 8, t.Y_Position + 8) == true)
 			System.out.print("X_Collision(pixel inside) not moving success 1\n");
 		else
 			System.out.print("X_Collision(pixel inside) not moving fail 1\n");
 		t.X_Movement = 1;
 		if (t.X_Collision(t.X_Position + 8, t.Y_Position + 8) == true)
 			System.out.print("X_Collision(pixel inside) moving success 2\n");
 		else
 			System.out.print("X_Collision(pixel inside) moving fail 2\n");
 		
 		// ====================================================================
 		// Conduct Y_Collision test where input is pixel within GCharacter
 		// ====================================================================
 		t.X_Movement = 0; t.Y_Movement = 0;
 		if (t.Y_Collision(t.X_Position + 8, t.Y_Position + 8) == true)
 			System.out.print("Y_Collision(pixel inside) not moving success 1\n");
 		else
 			System.out.print("Y_Collision(pixel inside) not moving fail 1\n");
 		t.X_Movement = 1;
 		if (t.Y_Collision(t.X_Position + 8, t.Y_Position + 8) == true)
 			System.out.print("Y_Collision(pixel inside) moving success 2\n");
 		else
 			System.out.print("Y_Collision(pixel inside) moving fail 2\n");
 
 		// ====================================================================
 		// Conduct X_Collision test where input is pixel outside of GCharacter
 		// ====================================================================
 		t.X_Movement = 0; t.Y_Movement = 0;
 		if (t.X_Collision(t.X_Position + 100, t.Y_Position + 100) == false)
 			System.out.print("X_Collision(pixel outside) success 1\n");
 		else
 			System.out.print("X_Collision(pixel outside) fail 1\n");
 		t.X_Movement = 1;
 		if (t.X_Collision(t.X_Position + 100, t.Y_Position + 100) == false)
 			System.out.print("X_Collision(pixel outside) success 2\n");
 		else
 			System.out.print("X_Collision(pixel outside) fail 2\n");
 
 		// ====================================================================
 		// Conduct Y_Collision test where input is pixel outside of GCharacter
 		// ====================================================================
 		t.X_Movement = 0; t.Y_Movement = 0;
 		if (t.Y_Collision(t.X_Position + 100, t.Y_Position + 100) == false)
 			System.out.print("Y_Collision(pixel outside) success 1\n");
 		else
 			System.out.print("Y_Collision(pixel outside) fail 1\n");
 		t.X_Movement = 1;
 		if (t.Y_Collision(t.X_Position + 100, t.Y_Position + 100) == false)
 			System.out.print("Y_Collision(pixel outside) success 2\n");
 		else
 			System.out.print("Y_Collision(pixel outside) fail 2\n");
 
 		// ====================================================================
 		// Conduct X_Collision test with input={obj1,obj2} where both are far away
 		// ====================================================================
 		GObject obj1 = new GObject();
 		GObject obj2 = new GObject();
 		obj1.Width = 16; obj1.Height = 16;
 		obj2.Width = 16; obj2.Height = 16;
 		obj1.Solid = true; obj2.Solid = true;
 		obj1.X_Position = 100; obj2.X_Position = 100;
 		GObject[] objs = {obj1, obj2};
 		if (t.X_Collision(objs) == null)
 			System.out.print("X_Collision(objs faraway) success\n");
 		else
 			System.out.print("X_Collision(objs faraway) fail\n");
 		
 		// ====================================================================
 		// Conduct Y_Collision test with input={obj1,obj2} where both are far away
 		// ====================================================================
 		if (t.Y_Collision(objs) == null)
 			System.out.print("Y_Collision(objs faraway) success\n");
 		else
 			System.out.print("Y_Collision(objs faraway) fail\n");
 
 		// ====================================================================
 		// Conduct X_Collision test with input={obj1,obj2} where one is one tile away
 		// ====================================================================
 		obj1.X_Position = t.X_Position + 16;
 		obj1.Y_Position = t.Y_Position;
 		t.X_Movement = 1;
 		t.Y_Movement = 0;
 		if (t.X_Collision(objs) == null)
 			System.out.print("X_Collision(objs 1 close) fail\n");
 		else
 			System.out.print("X_Collision(objs 1 close) success\n"); 
 		
 		// ====================================================================
 		// Conduct Y_Collision test with input={obj1,obj2} where one is one tile away
 		// ====================================================================
 		obj1.X_Position = t.X_Position;
 		obj1.Y_Position = t.Y_Position + 16;
 		t.X_Movement = 0;
 		t.Y_Movement = 1;
 		if (t.Y_Collision(objs) == null)
 			System.out.print("Y_Collision(objs 1 close) fail\n");
 		else
 			System.out.print("Y_Collision(objs 1 close) success\n");
 
 		// ====================================================================
 		// Conduct out of bounds test if out of bounds
 		// ====================================================================
 		t.X_Position = -100;
 		t.Y_Position = -100;
 		if (t.X_Out_Of_Bounds() && t.Y_Out_Of_Bounds())
 			System.out.print("Out of bounds success\n");
 		else
 			System.out.print("Out of bounds fail\n");
 		
 		// ====================================================================
 		// Conduct out of bounds test if not out of bounds
 		// ====================================================================
 		t.X_Position = 100;
 		t.Y_Position = 100;
 		if (t.X_Out_Of_Bounds() || t.Y_Out_Of_Bounds())
 			System.out.print("In bounds fail\n");
 		else
 			System.out.print("In bounds success\n");
 		
 		// ====================================================================
 		// Conduct Update_X_Position test and Update_Y_Position test for all movement values
 		// ====================================================================
 		t.X_Position = 0; t.Y_Position = 0;
 		t.X_Movement = 0; t.Y_Movement = 0;
 		t.x_last_move = -99999;
 		t.y_last_move = -99999;
 		t.Update_X_Position();
 		if (t.X_Position == 0 && t.Y_Position == 0)
 			System.out.print("Update_X_Position neutral success\n");
 		else
 			System.out.print("Update_X_Position neutral fail\n");
 		t.X_Position = 0; t.Y_Position = 0;
 		t.X_Movement = -1;
 		t.x_last_move = -99999;
 		t.y_last_move = -99999;
 		t.Update_X_Position();
 		if (t.X_Position == -1 && t.Y_Position == 0)
 			System.out.print("Update_X_Position left success\n");
 		else
 			System.out.print("Update_X_Position left fail\n");
 		t.X_Position = 0; t.Y_Position = 0;
 		t.X_Movement = 1;
 		t.x_last_move = -99999;
 		t.y_last_move = -99999;
 		t.Update_X_Position();
 		if (t.X_Position == 1 && t.Y_Position == 0)
 			System.out.print("Update_X_Position right success\n");
 		else
 			System.out.print("Update_X_Position right fail\n");
 		t.X_Position = 0; t.Y_Position = 0;
 		t.X_Movement = 0; t.Y_Movement = -1;
 		t.x_last_move = -99999;
 		t.y_last_move = -99999;
 		t.Update_Y_Position();
 		if (t.X_Position == 0 && t.Y_Position == -1)
 			System.out.print("Update_Y_Position up success\n");
 		else
 			System.out.print("Update_Y_Position up fail\n");
 		t.X_Position = 0; t.Y_Position = 0;
 		t.X_Movement = 0; t.Y_Movement = 1;
 		t.x_last_move = -99999;
 		t.y_last_move = -99999;
 		t.Update_Y_Position();
 		if (t.X_Position == 0 && t.Y_Position == 1)
 			System.out.print("Update_Y_Position down success\n");
 		else
 			System.out.print("Update_Y_Position down fail\n");
 		
 		// ====================================================================
 		// Conduct Get_Health test
 		// ====================================================================
 		t.Health = 4;
 		if (t.Get_Health() == 4)
 			System.out.print("Get_Health success\n");
 		else
 			System.out.print("Get_Health fail\n");
 		
 		// ====================================================================
 		// Conduct Decriment_Health test
 		// ====================================================================
 		t.Health = 4;
 		t.last_damage = -999999999;
 		t.Decriment_Health();
 		if (t.Health == 3)
 			System.out.print("Decriment_Health success\n");
 		else
 			System.out.print("Decriment_Health fail\n");
 		
 		// ====================================================================
 		// Conduct Decrease_Health test
 		// ====================================================================
 		t.Health = 4;
 		t.last_damage = -999999;
 		t.Decrease_Health(3);
 		if (t.Health == 1)
 			System.out.print("Decrease_Health success\n");
 		else
 			System.out.print("Decrease_Health fail\n");
 		
 		// ====================================================================
 		// Conduct Reset_Health test
 		// ====================================================================
 		t.Max_Health = 5;
 		t.Health = 1;
 		t.Reset_Health();
 		if (t.Health == 5)
 			System.out.print("Reset_Health success\n");
 		else
 			System.out.print("Reset_Health fail\n");
 		
 		// ====================================================================
 		// Conduct Increase_Health test
 		// ====================================================================
 		t.Health = 5;
 		t.Max_Health = 5;
 		t.Increase_Health(1);
 		if (t.Health == 5)
 			System.out.print("Increase_Health when maxed already success\n");
 		else
 			System.out.print("Increase_Health when maxed already fail\n");
 		t.Health = 2;
 		t.Max_Health = 5;
 		t.Increase_Health(1);
 		if (t.Health == 3)
 			System.out.print("Increase_Health when not maxed success\n");
 		else
 			System.out.print("Increase_Health when not maxed fail\n");
 		
 		// ====================================================================
 		// Conduct Get_Max_Health test
 		// ====================================================================
 		t.Max_Health = 5;
 		if (t.Get_Max_Health() == 5)
 			System.out.print("Get_Max_Health success\n");
 		else
 			System.out.print("Get_Max_Health fail\n");
 		
 		// ====================================================================
 		// Conduct Get_Speed test
 		// ====================================================================
 		t.Speed = 3.0f;
 		if (t.Get_Speed() == 3.0f)
 			System.out.print("Get_Speed success\n");
 		else
 			System.out.print("Get_Speed fail\n");
 		
 		// ====================================================================
 		// Conduct Get_X_Movement test
 		// ====================================================================
 		t.X_Movement = -1;
 		if (t.Get_X_Movement() == -1)
 			System.out.print("Get_X_Movement success\n");
 		else
 			System.out.print("Get_X_Movement fail\n");
 		
 		// ====================================================================
 		// Conduct Get_Y_Movement test for Y_Movement=-1,0,1
 		// ====================================================================
 		t.Y_Movement = 1;
 		if (t.Get_Y_Movement() == 1)
 			System.out.print("Get_Y_Movement success\n");
 		else
 			System.out.print("Get_Y_Movement fail\n");
 		
 		// ====================================================================
 		// Conduct Set_Movement test for combinations of movement values
 		// ====================================================================
 		t.Set_Movement(1, -1);
 		if (t.X_Movement == 1 && t.Y_Movement == -1)
 			System.out.print("Set_Movement sucess\n");
 		else
 			System.out.print("Set_Movement fail\n");
 		
 		// ====================================================================
 		// Conduct Move test
 		// ====================================================================
 		t.Move(30, 40);
 		if (t.X_Position == 30 && t.Y_Position == 40)
 			System.out.print("Move sucess\n");
 		else
 			System.out.print("Move fail\n");
 		
 		// ====================================================================
 		// Conduct Get_X_Position test
 		// ====================================================================
 		t.X_Position = 50;
 		if (t.Get_X_Position() == 50)
 			System.out.print("Get_X_Position success\n");
 		else
 			System.out.print("Get_X_Position fail\n");
 		
 		// ====================================================================
 		// Conduct Get_Y_Position test
 		// ====================================================================
 		t.Y_Position = 80;
 		if (t.Get_Y_Position() == 80)
 			System.out.print("Get_Y_Position success\n");
 		else
 			System.out.print("Get_Y_Position fail\n");
 		
 		// ====================================================================
 		// Conduct Get_Width test
 		// ====================================================================
 		if (t.Get_Width() == 16)
 			System.out.print("Get_Width success\n");
 		else
 			System.out.print("Get_Width fail\n");
 		
 		// ====================================================================
 		// Conduct Get_Height test
 		// ====================================================================
 		if (t.Get_Height() == 16)
 			System.out.print("Get_Height success\n");
 		else
 			System.out.print("Get_Height fail\n");
 		
 		// ====================================================================
 		// Conduct Pixel_Contained test
 		// ====================================================================
 		if (t.Pixel_Contained(t.X_Position + 8, t.Y_Position + 8))
 			System.out.print("Pixel_Contained(pixel inside) success\n");
 		else
 			System.out.print("Pixel_Contained(pixel inside) fail\n");
 		if (t.Pixel_Contained(t.X_Position + 100, t.Y_Position + 100))
 			System.out.print("Pixel_Contained(pixel outside) fail\n");
 		else
 			System.out.print("Pixel_Cointained(pixel inside) success\n");
 		
 		// ====================================================================
 		// Conduct Alligned_With_Tiles test for when alligned with tiles and when not
 		// ====================================================================
 		t.X_Position = 16; t.Y_Position = 16;
 		if (t.Alligned_With_Tiles())
 			System.out.print("Alligned_With_Tiles when alligned success\n");
 		else
 			System.out.print("Alligned_With_Tiles when alligned fail\n");
 		t.X_Position = 17;
 		if (t.Alligned_With_Tiles())
 			System.out.print("Alligned_With_Tiles when not alligned fail\n");
 		else
 			System.out.print("Alligned_With_Tiles when not alligned success\n");
 		
 		// ====================================================================
 		// Conduct Get_Damage test
 		// ====================================================================
 		t.Damage = 1;
 		if (t.Get_Damage() == 1)
 			System.out.print("Get_Damage success\n");
 		else
 			System.out.print("Get_Damage fail\n");
 	}
 }
