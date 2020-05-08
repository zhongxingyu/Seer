 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package storm.utility;
 
 /**
  *
  * @author Developer
  */
 public class Queue {
 
     double[][] queue;
     int qPart = 0;
     int qNum = 0;
     int type = 0;
     double distance = 0;
     double speed = 0.0;
     boolean qRunning = false;
 
     public Queue() {
 
          queue = new double[3][10];
 
     }
 
    public void Start() {
         qRunning = true;
     }
    public void Stop() {
         qRunning = false;
     }
 
     public void add(int t, double distance, double speed) {
 
         queue[0][qNum] = t;
         queue[1][qNum] = distance;
         queue[2][qNum] = speed;
         
 
         qNum++;
 
     }
 
     public double getSpeed() {
         return queue[2][qPart];
     }
     public double getDistance() {
         return queue[1][qPart];
     }
     public double getType() {
         return queue[0][qPart];
     }
     public int getPart() {
         return qPart;
     }
     public boolean isRunning() {
         return qRunning;
     }
 
     public void next() {
         qPart++;
         if (queue[0][qPart] == 0) qRunning = false;
     }
 
     public void clear() {
 
         for (int i = 0; i < 10; i++) {
             for (int j = 0; j < 3; j++) {
                 queue[j][i] = 0;
             }
         }
         qNum = 0;
         qPart = 0;
 
     }
 
 }
