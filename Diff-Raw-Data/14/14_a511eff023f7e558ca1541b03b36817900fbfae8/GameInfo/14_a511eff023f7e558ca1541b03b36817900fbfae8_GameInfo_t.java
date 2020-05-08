 package kaban.iklan;
 
 class GameInfo {
 
     private final static int FPS_HISTORY_SIZE = 10;
     private double 	fpsStore[] = new double[FPS_HISTORY_SIZE];
 
     // number of frames skipped in a store cycle (1 sec)
     private int framesSkippedPerStatCycle;
     // number of frames skipped since the game started
     private int totalFramesSkipped = 0;
     private int avgFramesSkipped;
 
     // number of rendered frames in an interval
     private int frameCountPerStatCycle = 0;
     private long totalFrameCount = 0l;
 
     // the number of times the stat has been read
     private long 	statsCount = 0;
     // the average FPS since the game started
     private double 	averageFps = 0.0;
     // we'll be reading the stats every second
     private final static int STAT_INTERVAL = 1000; //ms
     // last time the status was stored
     private long lastStatusStore = 0;
     // the status time counter
     private long statusIntervalTimer = 0l;
 
 
     // public methods
     public long getTotalFrames() {
         return totalFrameCount;
     }
 
     public int getAvgFramesSkipped() {
         return avgFramesSkipped;
     }
 
     public double getAverageFps(){
         return averageFps;
     }
 
     public void update(int framesSkipped) {
 
         framesSkippedPerStatCycle += framesSkipped;
 
         frameCountPerStatCycle++;
         totalFrameCount++;
 
         // check the actual time
         statusIntervalTimer += (System.currentTimeMillis() - statusIntervalTimer);
 
         if (statusIntervalTimer >= lastStatusStore + STAT_INTERVAL) {
             // calculate the actual frames per status check interval
             double actualFps = (double)(frameCountPerStatCycle / (STAT_INTERVAL / 1000));
 
             // stores the latest fps in the array
             fpsStore[(int) statsCount % FPS_HISTORY_SIZE] = actualFps;
 
             // increase the number of times statistics was calculated
             statsCount++;
 
             double totalFps = 0.0;
             // sum up the stored fps values
             for (int i = 0; i < FPS_HISTORY_SIZE; i++) {
                 totalFps += fpsStore[i];
             }
 
             // obtain the average
             if (statsCount < FPS_HISTORY_SIZE) {
                 // in case of the first 10 triggers
                 averageFps = totalFps / statsCount;
             } else {
                 averageFps = totalFps / FPS_HISTORY_SIZE;
             }
             // saving the number of total frames skipped
             totalFramesSkipped += framesSkippedPerStatCycle;
 
             // resetting the counters after a status record (1 sec)
             framesSkippedPerStatCycle = 0;
             statusIntervalTimer = 0;
             frameCountPerStatCycle = 0;
 
             statusIntervalTimer = System.currentTimeMillis();
             lastStatusStore = statusIntervalTimer;
            if( totalFramesSkipped > 0) {
                avgFramesSkipped = (int) totalFrameCount / totalFramesSkipped;
            }
         }
     }
 }
