 package lxx.services;
 
 import lxx.events.BulletDetectedEventListener;
 import lxx.events.WaveGoneEventListener;
 import lxx.model.LxxBullet;
 import lxx.model.LxxBulletState;
 import lxx.model.LxxWave;
 import lxx.utils.HitRate;
 
 public class StatisticsService implements WaveGoneEventListener, BulletDetectedEventListener {
 
     private final HitRate hitRate;
     private final String robotName;
 
     public StatisticsService(StaticDataStorage dataStorage, String robotName) {
         this.robotName = robotName;
         final String id = this.robotName + ".hitRate";
         if (!dataStorage.containsData(id)) {
             dataStorage.saveData(id, new HitRate());
             MonitoringService.setRobotHitRate(robotName, "unknown");
         }
         this.hitRate = dataStorage.getData(id);
     }
 
     @Override
     public void onBulletDetected(LxxBullet bullet) {
         if (bullet.state == LxxBulletState.HIT_ROBOT) {
             hitRate.hit();
         }
 
         MonitoringService.setRobotHitRate(robotName, hitRate.toString());
     }
 
     @Override
     public void onWaveGone(LxxWave wave) {
         hitRate.miss();
     }
 
 }
