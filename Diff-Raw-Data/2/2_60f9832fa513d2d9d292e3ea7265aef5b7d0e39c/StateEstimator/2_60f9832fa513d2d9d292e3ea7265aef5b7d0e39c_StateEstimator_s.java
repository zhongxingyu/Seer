 package core;
 
 import java.util.ArrayList;
 
 import map.Map;
 import map.MapBlock;
 
 public class StateEstimator {
     private DataCollection dc;
 
     //private Map worldMap;
     //private Map localMap;
     
     private double botX;
     private double botY;
     private double botTheta;
     
     private Map map;
     private Block closestBlock;
 
     public static final double WHEELBASE = .4;
     public static final double TICKS_PER_REV = 65500;
     public static final double WHEEL_RADIUS = .0625;
     //0.0000059954
     public static final double METERS_PER_TICK = WHEEL_RADIUS*Math.PI*2/TICKS_PER_REV;
 
     public StateEstimator(DataCollection dc) {
         this.dc = dc;    
     }
     
     public void step() {
         computePose();        
     }
     
     public void computePose() {
         double dl = dc.encoders.dLeft * METERS_PER_TICK;
         double dr = dc.encoders.dRight * METERS_PER_TICK;
 
         if (dr == 0 && dl == 0) return; // we haven't moved at all
         
         double dTheta = (dl - dr)/WHEELBASE;
 
         botTheta += dTheta;
         botX += (dl+dr)*Math.cos(botTheta)/2.0;
         botY += (dl+dr)*Math.sin(botTheta)/2.0;
     }
 
     public void updateBlocks() {
         for (Block b: dc.BlocksInVision){
 
            b.setPosition(dc.botX, dc.botY, dc,botTheta);
 
         }      
     }
 
 }
