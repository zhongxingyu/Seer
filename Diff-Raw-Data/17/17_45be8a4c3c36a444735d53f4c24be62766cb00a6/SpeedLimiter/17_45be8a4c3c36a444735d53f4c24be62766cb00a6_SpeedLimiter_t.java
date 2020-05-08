 package org.housered.simul.model.location;
 
 public class SpeedLimiter
 {
     private float maxDistancePerSecond;
     private float distanceMovedThisTick;
     private float dt;
 
     public void setSpeedLimit(float maxDistancePerSecond)
     {
         this.maxDistancePerSecond = maxDistancePerSecond;
     }
 
     public void startNewTick(float dt)
     {
         distanceMovedThisTick = 0;
         this.dt = dt;
     }
 
     public Vector incrementPosition(Vector delta)
     {
         float wantedMag = Math.min(maxDistancePerSecond * dt - distanceMovedThisTick, delta.magnitude());
        wantedMag = Math.max(wantedMag, 0);
         distanceMovedThisTick += wantedMag;
         return delta.scaleToMagnitudeCopy(wantedMag);
     }
 }
