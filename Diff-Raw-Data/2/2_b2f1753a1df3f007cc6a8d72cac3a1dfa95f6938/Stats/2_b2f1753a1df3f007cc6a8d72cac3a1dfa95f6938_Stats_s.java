 package com.starlon.starvisuals;
 
 class FrameStats {
     public double renderTime;
     public double frameTime;
     public FrameStats() 
     {
     }
 }
 
 public class Stats {
     public int MAX_FRAME_STATS = 200;
     public int MAX_PERIOD_MS = 1500;
     public double firstTime = 0.0;
     public double lastTime = 0.0;
     public double frameTime = 0.0;
     public int firstFrame = 0;
     public int numFrames = 0;
     public FrameStats frames[];
     public double mAvgFrame = 0.0;
     public double mMaxFrame = 0.0;
     public double mMinFrame = 0.0;
     public double mAvgRender = 0.0;
     public double mMinRender = 0.0;
     public double mMaxRender = 0.0;
     
 
     public Stats()
     {
     }
 
     public String getText()
     {
         return String.format("%.1f fps", mAvgFrame);
     }
 
    private double nowMil()
     {
         return System.currentTimeMillis();
     }
     
     private void
     formatStats(double avgFrame, double maxFrame, double minFrame, double avgRender, double minRender, double maxRender)
     {
         mAvgFrame = avgFrame;
         mMaxFrame = maxFrame;
         mMinFrame = minFrame;
         mAvgRender = avgRender;
         mMinRender = minRender;
         mMaxRender = maxRender;
     }
     public void
     statsInit()
     {
         int index;
         frames = new FrameStats[MAX_FRAME_STATS];
         for(index = 0; index < MAX_FRAME_STATS; index++)
         {
             frames[index] = new FrameStats();
         }
         lastTime = nowMil();
         firstTime = 0.;
         firstFrame = 0;
         numFrames  = 0;
     }
     
     public void
     startFrame()
     {
         frameTime = nowMil();
     }
     
     public void
     endFrame()
     {
         double now = nowMil();
         double renderTime = now - frameTime;
         double frameTime  = now - lastTime;
         int nn;
     
         if (now - firstTime >= MAX_PERIOD_MS) {
             if (numFrames > 0) {
                 double minRender, maxRender, avgRender;
                 double minFrame, maxFrame, avgFrame;
                 int count;
     
                 nn = firstFrame;
                 minRender = maxRender = avgRender = frames[nn].renderTime;
                 minFrame  = maxFrame  = avgFrame  = frames[nn].frameTime;
                 for (count = numFrames; count > 0; count-- ) {
                     nn += 1;
                     if (nn >= MAX_FRAME_STATS)
                         nn -= MAX_FRAME_STATS;
                     double render = frames[nn].renderTime;
                     if (render < minRender) minRender = render;
                     if (render > maxRender) maxRender = render;
                     double frame = frames[nn].frameTime;
                     if (frame < minFrame) minFrame = frame;
                     if (frame > maxFrame) maxFrame = frame;
                     avgRender += render;
                     avgFrame  += frame;
                 }
                 avgRender /= numFrames;
                 avgFrame  /= numFrames;
     
     
                 formatStats(1000.0/avgFrame, 1000.0/maxFrame, 1000.0/minFrame,
                 avgRender, minRender, maxRender);
             }
             numFrames  = 0;
             firstFrame = 0;
             firstTime  = now;
         }
     
         nn = firstFrame + numFrames;
         if (nn >= MAX_FRAME_STATS)
             nn -= MAX_FRAME_STATS;
     
         frames[nn].renderTime = renderTime;
         frames[nn].frameTime  = frameTime;
     
         if (numFrames < MAX_FRAME_STATS) {
             numFrames += 1;
         } else {
             firstFrame += 1;
             if (firstFrame >= MAX_FRAME_STATS)
                 firstFrame -= MAX_FRAME_STATS;
         }
     
         lastTime = now;
     }
 
 }
 
 
