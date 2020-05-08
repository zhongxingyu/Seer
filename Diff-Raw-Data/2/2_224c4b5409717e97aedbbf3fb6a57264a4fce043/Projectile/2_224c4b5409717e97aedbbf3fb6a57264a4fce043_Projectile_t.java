 package com.github.inside;
 
 import com.github.inside.Config;
 import com.github.inside.Vector;
 import com.github.inside.Collision;
 import java.lang.Math;
 
 public class Projectile extends Equipement
 {
     long creationTime, // milliseconds timestamp
          diesIn   = 0,
          lifeTime = Config.PROJECTILES_DEFAULT_LIFETIME;
     double vX,
            vY;
     boolean diesNow = false;
 
     public Projectile(Board board)
     {
         super(board);
         this.creationTime = System.currentTimeMillis();
         this.diesIn = this.creationTime + this.lifeTime;
     }
 
     public void resetPosition()
     {
         this.x = (Config.BOARD_WIDTH / 2) - (this.width / 2);
         this.y = (Config.BOARD_HEIGHT / 2) - (this.height / 2);
         this.setVelocity(this.getInitialUnitVector());
     }
 
     public void setVelocity(Vector vector)
     {
         this.vX = vector.cartesian(0);
         this.vY = vector.cartesian(1);
     }
 
     public Vector getInitialUnitVector()
     {
         // random between 0.0 and 1.0 with one digit after the decimal point
         double y = (double) (Math.round(Math.random() * 10)) / 10.0,
                x = 1;
 
         // 50% of the time, go downwards
         if (Math.round(Math.random()) == 0)
         {
             y *= -1;
         }
         if (Math.round(Math.random()) == 0)
         {
             x *= -1;
         }
 
         return new Vector(new double[] {x, y}).direction();
     }
 
     public Vector getUnitVector(double x, double y)
     {
         return new Vector(new double[] {x, y}).direction();
     }
 
    public boolean isLiving(long currentTime)
     {
         if (this.diesNow)
         {
             return false;
         }
         else if (this.lifeTime <= 0)
         {
             return true;
         }
 
         return this.diesIn >= currentTime;
     }
 
     public Boolean hitsLeftWall()
     {
         return this.x <= 0;
     }
 
     public Boolean hitsRightWall()
     {
         return this.x >= Config.BOARD_WIDTH - this.width;
     }
 
     public Boolean hitsFloor()
     {
         return this.y >= Config.BOARD_HEIGHT - this.height;
     }
 
     public Boolean hitsCeiling()
     {
         return this.y <= 0;
     }
 
     public Boolean hitsLeftPaddle()
     {
         return Collision.overlap(this, this.board.leftPaddle);
     }
 
     public Boolean hitsRightPaddle()
     {
         return Collision.overlap(this, this.board.rightPaddle);
     }
 
     public void updateForNewFrame()
     {
         this.x += this.vX * this.speed * this.board.time;
         this.y += this.vY * this.speed * this.board.time;
 
         if (this.hitsLeftWall())
         {
             this.vX *= -1;
             this.x = 0;
         }
         else if (this.hitsRightWall())
         {
             this.vX *= -1;
             this.x = Config.BOARD_WIDTH - this.width;
         }
 
         if (this.hitsCeiling())
         {
             this.vY *= -1;
             this.y = 0;
             this.setVelocity(this.getUnitVector(this.vX, this.vY));
         }
         else if (this.hitsFloor())
         {
             this.vY *= -1;
             this.y = Config.BOARD_HEIGHT - this.height;
             this.setVelocity(this.getUnitVector(this.vX, this.vY));
         }
     }
 }
