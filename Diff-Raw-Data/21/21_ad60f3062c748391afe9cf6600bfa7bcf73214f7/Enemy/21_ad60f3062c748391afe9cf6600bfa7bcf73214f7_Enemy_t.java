 package characterSprites;
 
 import characterSprites.enemyAI.AttackState;
 import characterSprites.enemyAI.DeadState;
 import characterSprites.enemyAI.EnemyState;
 import characterSprites.enemyAI.PatrolState;
 import characterSprites.enemyAI.StationaryState;
 import com.golden.gamedev.Game;
 
 @SuppressWarnings("serial")
 public class Enemy extends Character
 {
     private EnemyState standState;
     private EnemyState patrolState;
     private EnemyState attackState;
     private EnemyState deadState;
     
     private Game myGame;
     private EnemyState myState;
     private EnemyState myPreviousState;
     private Double myStartX;
     private double myPatrolRadius;
 
     public Enemy (Game game, double patrolRadius)
     {
        super(game);
         
         standState = new StationaryState(this);
         patrolState = new PatrolState(this);
         attackState = new AttackState(this);
         deadState = new DeadState(this);
         
         myState = (patrolRadius != 0) ? patrolState : standState;
         myStartX = null;
         myPatrolRadius = patrolRadius;
     }
     
     public void update (long milliSec)
     {       
         if (myStartX == null)       myStartX = getX();
         
         myState.update(milliSec);
         super.update(milliSec);
     }
     
     public void setState (EnemyState state)
     {
         myPreviousState = myState;
         myState = state;
     }
     
     public double getStartX ()
     {
         return myStartX;
     }
     
     public double getPatrolRadius ()
     {
         return myPatrolRadius;
     }
     
     protected EnemyState getState ()
     {
         return myState;
     }
     
     protected EnemyState getPreviousState ()
     {
         return myPreviousState;
     }
 }
