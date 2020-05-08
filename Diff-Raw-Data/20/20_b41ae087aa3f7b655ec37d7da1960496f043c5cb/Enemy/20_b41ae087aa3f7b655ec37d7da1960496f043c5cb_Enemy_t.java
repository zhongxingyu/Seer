 package com.game.jetpack;
 
 import com.bag.lib.DynamicGameObject;
 
 public class Enemy extends DynamicGameObject {
 
 	public static final int ENEMY_TYPE_ZOMBIE 		= 0;
 	public static final int ENEMY_TYPE_BOSS 		= 1;
 
 	public static final int ENEMY_STATE_ALIVE 		= 2;
 	public static final int ENEMY_STATE_DEAD 		= 3;
 	
     public static final float ENEMY_MOVE_VELOCITY 	= 3.2f;
     public static final float ENEMY_BASIC_WIDTH 	= 1.0f;
     public static final float ENEMY_BASIC_HEIGHT 	= 1.0f;
     public static final float ENEMY_BOSS_WIDTH 		= 4.0f;
     public static final float ENEMY_BOSS_HEIGHT 	= 4.0f;
     
     public static final int ENEMY_SCORE = 50;
     
     public int type;
     public int state;
     public int life;
     
     private float speed;
     private int   difficulty;
     public float rotationAngle;
     public int   score;
     
     public float stateTime;
 	
 	public Enemy(float x, float y, int type, int difficulty) {
 		super(x, y, ENEMY_BASIC_WIDTH, ENEMY_BASIC_HEIGHT); 
 		
 		this.state = ENEMY_STATE_ALIVE;
 		this.rotationAngle = 0;
 		this.life = 1;
 		this.difficulty = difficulty;
 		this.type = type;
 		this.stateTime = 0.0f;
 		
 		initialize();
 	}
 	
     public void initialize()
     {
     	if(type == ENEMY_TYPE_ZOMBIE)
     	{
     		this.score = ENEMY_SCORE;
     		this.speed = ENEMY_MOVE_VELOCITY + (difficulty / 4);
 	        
     	} 
     	else if (type == ENEMY_TYPE_BOSS)
     	{
     		this.score = (int) (500 * (difficulty * 2));
     		this.life = (int) (100 + (difficulty * 50));
     		this.speed = ENEMY_MOVE_VELOCITY + 2 + (difficulty / 3) ; 
     		this.bounds.width += ENEMY_BOSS_WIDTH/2;
     		this.bounds.height += ENEMY_BOSS_WIDTH/2;
     	}	
     }
     
     public void update(float deltaTime) {    
     	bounds.lowerLeft.set(position).sub(bounds.width / 2, bounds.height / 2);
     	stateTime += deltaTime;
     	
     	updateVelocity(); 
          
         position.add(velocity.x * deltaTime, velocity.y * deltaTime);
         
         //stateTime += deltaTime;
         
         if(life <= 0)
         {
         	state = ENEMY_STATE_DEAD;
         	
         	if(type == ENEMY_TYPE_BOSS)
         	{
         		//DO SOMETHING?
         	}
         }
     }
     
     private void updateVelocity() 
     {
     	velocity.x = (float) (speed*Math.cos(rotationAngle));
     	velocity.y = (float) (speed*Math.sin(rotationAngle));
     }
 }
