 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates.commands;
 
 //import edu.team2035.meta.MetaTCPVariables;
 import edu.wpi.first.wpilibj.templates.OI;
 import edu.wpi.first.wpilibj.templates.RobotMap;
 import edu.wpi.first.wpilibj.templates.ScraperBike;
 
 /**
  *
  * @author jrusso
  */
 public class TargetSorting extends CommandBase {
 
     private double[] X_values = new double[4];
     private double[] Y_values = new double[4];
     private double[][] targetArray = new double [4][2];
     private boolean isSorting;
     private boolean finished;
     private int validTargets;
     
     public TargetSorting(){
         
         super("Target Sorting");
     }
     
     protected void initialize() {
                 
         validTargets = 0;
         targetArray[0][0] = 0;
         targetArray[0][1] = 0;
         targetArray[1][0] = 0;
         targetArray[1][1] = 0;
         targetArray[2][0] = 0;
         targetArray[2][1] = 0;
         targetArray[3][0] = 0;
         targetArray[3][1] = 0;
         isSorting = false;
         finished = false;
         fillXArray();
         fillYArray();
     }
 
     protected void execute()
     {
         
         if(isSorting == false){
             //System.out.println("Initiating sort");
             validTargets = 0;
 
             X_values = fillXArray();
             Y_values = fillYArray();
 
 //            if (MetaTCPVariables.dataMessage[0] < 5300)
 //                RobotMap.range = truncate(MetaTCPVariables.dataMessage[0]);
 //            else
 //                RobotMap.range = 0;
             
            if (ScraperBike.nt.getNumber("Range") < 5300)
                RobotMap.range = truncate(ScraperBike.nt.getNumber("Range"));
             else
                 RobotMap.range = 0;
 
 
             if ((X_values[0] > 0) || (Y_values[0] > 0)){
 
                 targetArray[validTargets][0] = truncate(X_values[validTargets]);
                 targetArray[validTargets][1] = truncate(Y_values[validTargets]);
                 validTargets++;
             } 
             if ((X_values[1] > 0) || (Y_values[1] > 0)){
 
                 targetArray[validTargets][0] = truncate(X_values[validTargets]);
                 targetArray[validTargets][1] = truncate(Y_values[validTargets]);
                 validTargets++;
             } 
             if ((X_values[2] > 0) || (Y_values[2] > 0)){
 
                 targetArray[validTargets][0] = truncate(X_values[validTargets]);
                 targetArray[validTargets][1] = truncate(Y_values[validTargets]);
                 validTargets++;
             } 
             if ((X_values[3] > 0) || (Y_values[3] > 0)){
 
                 targetArray[validTargets][0] = truncate(X_values[validTargets]);
                 targetArray[validTargets][1] = truncate(Y_values[validTargets]);
                 validTargets++;
             }
 
             //System.out.println("Unsorted array: " + arrayToString(targetArray));
             //System.out.println("About to sort!");
             sortTargets();
             //System.out.println("Sorted.");
             finished = true;
         }
         
         //RobotMap.ultrasonicRange = ScraperBike.getShooterController().getUltrasonicRange();
     }
 
     protected boolean isFinished() {
         if(finished)
             return true;
         else
             return false;
     }
 
     protected void end() {
     }
     
     protected double[] fillXArray(){
         
         for(int i = 1; i<=4; i++){
             
 //            X_values[(i-1)] = MetaTCPVariables.dataMessage[((i*2)-1)];
            X_values[0] = ScraperBike.nt.getNumber("X1");
            X_values[1] = ScraperBike.nt.getNumber("X2");
            X_values[2] = ScraperBike.nt.getNumber("X3");
            X_values[3] = ScraperBike.nt.getNumber("X4");
             //System.out.println("X values stored");
         }
         return X_values;
     }
     
     protected double[] fillYArray(){
         
         for(int i = 1; i<=4; i++){
             
 //            Y_values[(i-1)] = MetaTCPVariables.dataMessage[(i*2)];
            Y_values[0] = ScraperBike.nt.getNumber("Y1");
            Y_values[1] = ScraperBike.nt.getNumber("Y2");
            Y_values[2] = ScraperBike.nt.getNumber("Y3");
            Y_values[3] = ScraperBike.nt.getNumber("Y4");
             //System.out.println("Y values stored");
         }
         return Y_values;
     }
     
     protected synchronized double[][] sortTargets(){
 
         isSorting = true;
         //System.out.println("sortTargets begun");
         double[][] sortedArray = new double[4][2];
 
         double[] x_sort = new double[]{targetArray[0][0], targetArray[1][0], targetArray[2][0], targetArray[3][0]};
         double[] y_sort = new double[]{targetArray[0][1], targetArray[1][1], targetArray[2][1], targetArray[3][1]};
 
         double[] sortedX = arraySort(x_sort);
 
         for(int i = 0; i < 4; i++)
         {
 
             sortedArray[i][0] = sortedX[i];
         }
         x_sort = sortedX;
 
         double[] sortedY = arraySort(y_sort);
         for(int i = 0; i < 4; i++)
         {
 
             sortedArray[i][1] = sortedY[i];
         }
         y_sort = sortedY;
 
         for (int i = 0; i<validTargets; i++){
 
             if (targetArray[i][1] == y_sort[0]){ //find top
                 sortedArray[0][0] = targetArray[i][0];
                 sortedArray[0][1] = targetArray[i][1];
             }
         }
 
         for (int i = 0; i<validTargets; i++){
 
             if (targetArray[i][1] == y_sort[validTargets-1]){ //find bottom
                 sortedArray[1][0] = targetArray[i][0];
                 sortedArray[1][1] = targetArray[i][1];
             }
         }
 
         for (int i = 0; i<validTargets; i++){
 
             if (targetArray[i][0] == x_sort[0]){ //find left
                 sortedArray[2][0] = targetArray[i][0];
                 sortedArray[2][1] = targetArray[i][1];
             }
         }
 
         for (int i = 0; i<validTargets; i++){
 
             if (targetArray[i][0] == x_sort[validTargets-1]){ //find right
                 sortedArray[3][0] = targetArray[i][0];
                 sortedArray[3][1] = targetArray[i][1];
             }
         }
 
         //System.out.println("Targets sorted! " + truncate(sortedArray[0][0]) + ", " + validTargets);
         RobotMap.top[0] = sortedArray[0][0];
         RobotMap.top[1] = sortedArray[0][1];
         RobotMap.bottom[0] = sortedArray[1][0];
         RobotMap.bottom[1] = sortedArray[1][0];
         RobotMap.left[0] = sortedArray[2][0];                 
         RobotMap.left[1] = sortedArray[2][1];                 
         RobotMap.right[0] = sortedArray[3][0];
         RobotMap.right[1] = sortedArray[3][1];
         isSorting = false;
         return sortedArray;
     }
 
     protected void interrupted() {
     }   
     
     public double truncate(double d){
 
         int temp = (int)(d*1000);
         double result = (double)temp/1000;
         return result;
     }
 
     public synchronized double[] arraySort(double[] unsorted)
     {
         //System.out.println("ArraySort running");
         double[] sorted;
 
         for(int i=0; i<unsorted.length; i++)
         {
             for(int j=1; j < unsorted.length-1; j++)
             {
 
                 if(unsorted[j] > unsorted[i])
                 {
 
                     double temp = unsorted[i];
                     unsorted[i] = unsorted[j];
                     unsorted[j] = temp;
                 }
             }
         }
 
         sorted = unsorted;
 
         //System.out.println("ArraySort finished.");
         return sorted;
     }
     
     public String arrayToString(double[][] arr){
         String s = (arr[0][0] +", " + arr[0][1] +", " + arr[1][0] +", " 
                 + arr[1][1] +", " + arr[2][0] +", " + arr[2][1] +", " +
                 arr[3][0] +", " + arr[3][1]);
         
         return s;
     }
 }
