 package com.edinarobotics.zed.vision;
 
 import com.sun.squawk.util.MathUtils;
 import com.sun.squawk.util.StringTokenizer;
 import java.util.Vector;
 
 public class TargetCollection {
     private Vector targets;
     
     /**
      * Creates a new TargetCollection with Target objects created from the String,
      * {@code targetData}. The string has the format
      * "[x(double)],[y(double)],[distance(double)],[isCenter(1/0)]:[repeat]:[...]:".
      * @param targetData The Target data string to be parsed into a new TargetCollection.
      */
     public TargetCollection(String targetData) {
         targets = new Vector();
         String processedTargetData = targetData.trim();
         if(!processedTargetData.equals("")) {
             StringTokenizer targetTokenizer = new StringTokenizer(processedTargetData, ":");
             try {
                 while(targetTokenizer.hasMoreTokens()) {
                     String dataBlock = targetTokenizer.nextToken();
                     if(dataBlock.equals("")){
                         continue;
                     }
                     StringTokenizer targetDataTokenizer = new StringTokenizer(dataBlock, ",");
                     double x = Double.parseDouble(targetDataTokenizer.nextToken());
                     double y = Double.parseDouble(targetDataTokenizer.nextToken());
                     double distance = Double.parseDouble(targetDataTokenizer.nextToken());
                     boolean isCenter = targetDataTokenizer.nextToken().equals("1");
 
                     targets.addElement(new Target(x, y, distance, isCenter));
                 }
             }
             catch(Exception e) {
                 e.printStackTrace();
             }
         }
     }
     
     /**
      * Constructs a new TargetCollection containing the Target
      * objects given in {@code targets}.
      * @param targets The Target objects to be stored in the new TargetCollection.
      */
     public TargetCollection(Target[] targets){
         for(int i = 0;i < targets.length;++i) {
             this.targets.addElement(targets[i]);
         }
     }
     
     /**
      * Clones a given TargetCollection.
      * @param targetCollection The TargetCollection instance to be cloned.
      */
     public TargetCollection(TargetCollection targetCollection){
         targets = targetCollection.getTargets();
     }
     
     /**
      * Returns a copy of the internal Target vector used by this TargetCollection.
      * @return A Target vector equivalent to that used internally by TargetCollection.
      */
     public Vector getTargets(){
         return targets;
     }
     
     /**
      * Returns an array of all targets of the specified type.
      * If {@code centerTarget} is {@code true}, this method returns all center
      * (high) goals. If {@code centerTarget} is {@code false}, this method
      * returns all non-center (middle) goals.
      * @param centerTarget A boolean value indicating whether to return
      * high or medium Target objects.
      * @return An array of all targets meeting the specified criteria.
      */
     public Target[] filterByType(boolean centerTarget){
         int count = 0;
         for(int i = 0; i < targets.size(); i++){
             Target target = (Target)targets.elementAt(i);
             if(target.isCenter() == centerTarget){
                 count++;
             }
         }
         Target[] foundTargets = new Target[count];
         int index = 0;
         for(int i = 0; i < targets.size(); i++){
             Target target = (Target)targets.elementAt(i);
             if(target.isCenter() == centerTarget){
                 foundTargets[index] = target;
                 index++;
             }
         }
         return foundTargets;
     }
     
     /**
      * Returns the closest Target object to the given point. The distance
      * from the point to the center of all Target objects in this TargetCollection
      * is computed and the target with the smallest such distance is returned.
      * @param x The x-coordinate of the point to which the distance is computed.
      * @param y The y-coordinate of the point to which the distance is computed.
      * @return The target with the smallest distance to the specified point
      * or {@code null} if this TargetCollection is empty.
      */
     public Target getClosestTarget(double x, double y){
         double minDistance = Double.MAX_VALUE;
         Target foundTarget = null;
         for(int i = 0; i < targets.size(); i++){
             Target target = (Target)targets.elementAt(i);
             double sqDistance = MathUtils.pow(target.getX() - x, 2)+MathUtils.pow(target.getY() - y, 2);
             if(sqDistance < minDistance){
                 minDistance = sqDistance;
                 foundTarget = target;
             }
         }
         return foundTarget;
     }
     
     /**
      * Returns the closest Target object to the given point and with the given
      * type. The distance from the point to the center of all Target objects of the proper
      * type is computed and the target with the smallest such distance is returned.
      * @param x The x-coordinate of the point to which the distance is computed.
      * @param y The y-coordinate of the point to which the distance is computed.
      * @param centerTarget The type of target to return. {@code true} indicates
      * a high, center target and {@code false} indicates a low, non-center target.
      * @return The target with the smallest distance to the specified point
      * or {@code null} if no such target exists.
      */
     public Target getClosestTarget(double x, double y, boolean centerTarget){
         double minDistance = Double.MAX_VALUE;
         Target foundTarget = null;
         for(int i = 0; i < targets.size(); i++){
             Target target = (Target)targets.elementAt(i);
             if(target.isCenter() == centerTarget){
                 double sqDistance = MathUtils.pow(target.getX() - x, 2)+MathUtils.pow(target.getY() - y, 2);
                 if(sqDistance < minDistance){
                     minDistance = sqDistance;
                     foundTarget = target;
                 }
             }
         }
         return foundTarget;
     }
     
     public String toString() {
         String collection = "<TargetCollection ";
        for(int i = 0;i < targets.size();++i) {
            if(i == (targets.size() - 1)) {
                collection += ((Target)(targets.elementAt(i))).toString() + ">";
            } else {
                collection += ((Target)(targets.elementAt(i))).toString() + ", ";
            }
         }
         return collection;
     }
 }
