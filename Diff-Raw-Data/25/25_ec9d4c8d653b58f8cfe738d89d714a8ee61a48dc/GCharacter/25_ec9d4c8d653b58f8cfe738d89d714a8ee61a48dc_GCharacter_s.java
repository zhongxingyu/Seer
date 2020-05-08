 
 package zed;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SpriteSheet;
 
 import java.util.Random;
 
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
     final int STUN_TIME = 200;
     
     int Health; // current health for Character
     int Max_Health; // maximum health for Character
     float Speed; // tiles per second
     int X_Movement; // {-1, 0, 1} tells movement direction in x-axis
     int Y_Movement; // {-1, 0, 1} tells movement direction in y-axis
     
     long last_move; // time in nanoseconds of last movement
     long last_damage; // time in milliseconds of last time was damaged
     
     int AI_State; // For use in Artificial_Intelligence function
     long Last_AI_State_Change; // For use in Artificial_Intelligence function
     long AI_State_Change_Time;
     Random rnd = new Random();
     
     // Constructor for character that makes use of SpriteSheet to construct its
     // Animation array
     public GCharacter(
     		int tile_x, // initial x position of the character w.r.t. the game tiles
     		int tile_y, // initial y position of the character w.r.t. the game tiles
     		boolean visible, // initialize whether the character is visible
     		boolean solid,
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
             int y_movement // Give the character its initial y_movement value (-1, 0, 1)
             ) {
         
         // Constructs the "Object" part of Character
         super(tile_x, tile_y, visible, solid, sprite_shift_x, sprite_shift_y, tilesize, sprites,
                 spritesheet_index, animation_length, looping, current_animation);
         
         // Initialze health
         Health = health;
         Max_Health = health;
         
         // Initialize movement parameters
         Speed = speed;
         X_Movement = x_movement;
         Y_Movement = y_movement;
         
         // Initialize movement
         last_move = System.nanoTime();
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
     		boolean visible, // initialize whether the character is visible
     		boolean solid, // initialize solidity for collision
             int[] sprite_shift_x, // by how many pixels each animation is shifted in x direction
             int[] sprite_shift_y, // by how many pixels each animation is shifted in y direction
             int tilesize, // Give the character how large each tile is
     		Animation[] animation_list, // Give the character the animations it will be using
             int current_animation, // initialize which animation to start out with
             int health, // initialize the character's max_health and health
             float speed, // Give the character its speed in tiles per second 
             int x_movement, // Give the character its initial x_movement value (-1, 0, 1)
             int y_movement // Give the character its initial y_movement value (-1, 0, 1)
     		){
     	
     	super(tile_x, tile_y, visible, solid, sprite_shift_x, sprite_shift_y,
     			tilesize, animation_list, current_animation);
     	
         // Initialze health
         Health = health;
         Max_Health = health;
         
         // Initialize movement parameters
         Speed = speed;
         X_Movement = x_movement;
         Y_Movement = y_movement;
         
         // Initialize movement
         last_move = System.nanoTime();
         // Initialize damage timer
         last_damage = System.currentTimeMillis();
         
         // Initialize Artificial Intelligence
         AI_State = 0;
         Last_AI_State_Change = System.currentTimeMillis();
     }
 
     // Default Constructor sets everything to equal either 0 or null
     public GCharacter() {
         last_move = System.nanoTime();
         last_damage = System.currentTimeMillis();
     }
     
     // frames cannot have array size less than 4x5 because these
     // are the frames that Character is using.
     
     // Initialization given SpriteSheet
     public void Init(
     		int tile_x, // initial x position of the character w.r.t. the game tiles
     		int tile_y, // initial y position of the character w.r.t. the game tiles
     		boolean visible, // initialize wheter the character is visible
     		boolean solid, // initialize solidity for collision
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
             ){
         
     	// Initialize object part of character
         Init(tile_x, tile_y, visible, solid, sprite_shift_x, sprite_shift_y, tilesize, sprites,
                 spritesheet_index, animation_length, looping, current_animation);
         
         // initialize character part of character
         Health = health;
         Max_Health = max_health;
         Speed = speed;
         X_Movement = x_movement;
         Y_Movement = y_movement;
         
         last_move = System.nanoTime();
     }
     
     public void Init(
     		int tile_x, // initial x position of the character w.r.t. the game tiles
     		int tile_y, // initial y position of the character w.r.t. the game tiles
     		boolean visible, // initialize whether the character is visible
     		boolean solid, // initialize solidity for collision
             int[] sprite_shift_x, // by how many pixels each animation is shifted in x direction
             int[] sprite_shift_y, // by how many pixels each animation is shifted in y direction
             int tilesize, // Give the character how large each tile is
     		Animation[] animation_list, // Give the character the animations it will be using
             int current_animation, // initialize which animation to start out with
             int health, // initialize the character's max_health and health
             float speed, // Give the character its speed in tiles per second 
             int x_movement, // Give the character its initial x_movement value (-1, 0, 1)
             int y_movement // Give the character its initial y_movement value (-1, 0, 1)
             ){
     
 	    super.Init(tile_x, tile_y, visible, solid, sprite_shift_x, sprite_shift_y,
 				tilesize, animation_list, current_animation);
 		
 	    // Initialze health
 	    Health = health;
 	    Max_Health = health;
 	    
 	    // Initialize movement parameters
 	    Speed = speed;
 	    X_Movement = x_movement;
 	    Y_Movement = y_movement;
 	    
 	    // Initialize movement
 	    last_move = System.nanoTime();
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
 	        boolean collided = false;
 	        
 	        Update_Frame_State();
 	        GObject[] cplayer = {player};
 	        collided |= (Collision(collision_objects) != null); // tell whether character has collided with an object
 	        collided |= (Collision(npcs) != null); // tell whether character has collided with another collidable npc
	        collided |= (Collision(cplayer) != null);
 	        
 	        if (!collided && System.currentTimeMillis() > last_damage + STUN_TIME
 	        		&& !Out_Of_Bounds())
 	        {
 	            Update_Position(); // can move if there is something to collide with
 	        }
 	        if (Collision(player.Get_Sword_Pos_X(), player.Get_Sword_Pos_Y()))
 	        {
 	        	Decriment_Health(); // kills this character with a sword
 	        }
 	        
 	        Artificial_Intelligence(collided, player); // Proceed with AI code to update the character's movement values
     	}
     	else
     	{
     		Solid = false;
     		Visible = false;
     	}
     }
     
     // can tell if GCharacter will collide with another object or not
     // and returns the object GCharacter collides with
     GObject Collision(GObject collision_objects[]){
         
     	if (collision_objects != null)
     	{
 	        for (int i = 0; i < collision_objects.length; i++)
 	        {
 		        if (collision_objects[i] != this && collision_objects[i].Solid)
 		        {
 		        	if (Collision(collision_objects[i].Get_X_Position(),
 		        			collision_objects[i].Get_Y_Position()))
 		        		return collision_objects[i];
 		        	if (Collision(collision_objects[i].Get_X_Position() + 15,
 		        			collision_objects[i].Get_Y_Position()))
 		        		return collision_objects[i];
 		        	if (Collision(collision_objects[i].Get_X_Position() + 15,
 		        			collision_objects[i].Get_Y_Position() + 15))
 		        		return collision_objects[i];
 		        	if (Collision(collision_objects[i].Get_X_Position(),
 		        			collision_objects[i].Get_Y_Position() + 15))
 		        		return collision_objects[i];
 		        }
 	        }
     	}
         return null; // didn't collide
     }
     
     // can tell if pixel is within GCharacter or not
     boolean Collision(int x, int y){
     	
     	if (x >= X_Position + X_Movement && x < X_Position + X_Movement + 16
     	 && y >= Y_Position + Y_Movement && y < Y_Position + Y_Movement + 16)
     	{
     		return true;
     	}
     	return false;
     }
     
     // check to see if GCharacter goes out of screen
     boolean Out_Of_Bounds(){
 
         if (  X_Position + 16 + X_Movement > 16*20 || X_Position + X_Movement < 0
     			|| Y_Position + 16 + Y_Movement > 16*15 || Y_Position + Y_Movement < 0)
         {
         	return true; // can't go out of bounds
         }
         return false;
     }
     
     // updates the current animation being played based on current movement
     void Update_Frame_State(){
         
     	// if walking up or down
         if (Y_Movement == 1) {
             Change_Animation(FRAME_STATE_DOWN_WALK); // walk down
         }
         
         else if (Y_Movement == -1) {
         	Change_Animation(FRAME_STATE_UP_WALK); // walk up
         }
         
         else // if not walking up or down
         {
             if (X_Movement == 1) {
                 Change_Animation(FRAME_STATE_RIGHT_WALK); // walk right
             }
             
             else if (X_Movement == -1) {
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
     
     // Updates current position based on movement
     // (do not call this function without checking for collision first)
     void Update_Position(){
         
         // Move Diagonally
         if (X_Position != 0 && Y_Position != 0)
         {
             if (System.nanoTime() >= last_move
                 + (long)(1000000000.0/(Speed*0.70710678118*16.0))) // wait until right time
             {
                 X_Position += X_Movement; // update X_Position
                 Y_Position += Y_Movement; // update Y_Position
             
                 last_move = System.nanoTime(); // record the time of last movement
             }
         }
         
         // Move vertically or horizontally
         else 
         {
             if (System.nanoTime() >= last_move
                     + (long)(1000000000.0/(Speed*16.0))) // wait until right time
             {
                 X_Position += X_Movement; // update X_Position
                 Y_Position += Y_Movement; // update Y_Position
 
                 last_move = System.nanoTime(); // record the time of last movement
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
 	    	if (Health - health_dec <= 0) // don't decrease below 0
 	    	{
 	    		Health = 0; // safeguard in case decreased below 0
 	    	}
 	    	else
 	    	{
 	    		Health -= health_dec; // decrease health
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
 }
