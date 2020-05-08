 package ch.sreng.schedule.procedure;
 
 import ch.sreng.schedule.components.mobile.Train;
 import ch.sreng.schedule.components.stationary.TrackComponent;
 import ch.sreng.schedule.simulation.Time;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.ListIterator;
 
 /**
  * @author koenigst
  *
  */
 public class DriveStrategyBangBang implements DriveStrategy {
 
     final protected List<MaxVelocity> getMaxVelocityList(Train requester, Time timer)
     {
         //Compute how far in front of the train we need to search for velocity changes
         double maxAchievableVelocity=Math.min(requester.getMaxVelocity(),
                 requester.getVelocity()+requester.getMaxAcceleration()*timer.getDeltaTime());
         double maxCheckDistance=1.2*(0.5*requester.getMaxAcceleration()*timer.getDeltaTime()*timer.getDeltaTime()
                 +requester.getSafetyStrategy().brickWallDistance(requester, maxAchievableVelocity)
                 +requester.getLength());
         //Keep track of the distance to the back of the train
         double tmpDistance=0;
         //Generate a list of maximum velocities and at what distance they begin
         ArrayList<MaxVelocity> maxVelocityTrack=new ArrayList<MaxVelocity>();
         MaxVelocity maxVelocityTrain=null;
         //Distance from the beginning of the TrackComponent to the back of the train,
         //only important for first TrackComponent
         TrackComponent currentTrack=requester.getCurrentTrack();
         double distanceBeginTrain=currentTrack.getTrainEndPosition(requester);
         //Go through all tracks in front until we searched far enough
         while(tmpDistance<maxCheckDistance)
         {
             if(currentTrack==null)
             {
                 maxVelocityTrack.add(new MaxVelocity(tmpDistance, 0));
                 tmpDistance=maxCheckDistance;
             }
             else
             {
                 //Add velocity contraint of currentTrack
                 maxVelocityTrack.add(new MaxVelocity(tmpDistance,
                         Math.min(currentTrack.getMaxVelocity(requester),requester.getMaxVelocity())));
                 //Get all trains on current track and get their velocity constraint
                 for(Train otherTrain: currentTrack.getOtherTrains(requester))
                 {
                    if(otherTrain.getVelocity()<=requester.getVelocity())
                     {
                         double tmpTrainBrakePosition=tmpDistance+currentTrack.getTrainEndPosition(otherTrain)
                                 -requester.getSafetyStrategy().brickWallDistance(requester, otherTrain.getVelocity());
                         if(tmpTrainBrakePosition>0) {
                             tmpTrainBrakePosition=requester.getVelocity()*tmpTrainBrakePosition
                                     /(requester.getVelocity()-otherTrain.getVelocity());
                         }
                         if(maxVelocityTrain==null || maxVelocityTrain.distance>tmpTrainBrakePosition)
                         {
                            if(tmpTrainBrakePosition<0) {
                                maxVelocityTrain=new MaxVelocity(tmpTrainBrakePosition,0);
                            } else {
                                maxVelocityTrain=new MaxVelocity(tmpTrainBrakePosition,
                                        Math.min(otherTrain.getVelocity(),requester.getMaxVelocity()));
                            }
                         }
                     }
                 }
 
                 //Go to next track segment
                 tmpDistance+=currentTrack.getLength(requester)-distanceBeginTrain;
                 currentTrack=currentTrack.getNextTrack(requester);
                 distanceBeginTrain=0;
             }
         }
 
         //Sort for distances
         Collections.sort(maxVelocityTrack);
 
         ArrayList<MaxVelocity> maxVelocityReturn;
         if(maxVelocityTrain==null)
         {
             maxVelocityReturn=maxVelocityTrack;
         } else {
             maxVelocityReturn=new ArrayList<MaxVelocity>();
             ListIterator<MaxVelocity> cleanIterator=maxVelocityTrack.listIterator();
             double lastAddedVelocity=Double.POSITIVE_INFINITY;
             while(cleanIterator.hasNext())
             {
                 MaxVelocity currentElement=cleanIterator.next();
                if(currentElement.distance<maxVelocityTrain.distance
                        || maxVelocityTrain.maxVelocity>currentElement.maxVelocity)
                 {
                     lastAddedVelocity=currentElement.maxVelocity;
                     maxVelocityReturn.add(currentElement);
                 }
             }
             if(maxVelocityTrain.maxVelocity<lastAddedVelocity)
             {
                 if(maxVelocityReturn.isEmpty()) { //If no other speed limit, add current
                     maxVelocityReturn.add(new MaxVelocity(0, requester.getCurrentTrack().getMaxVelocity(requester)));
                 }
                 maxVelocityReturn.add(maxVelocityTrain);
             }
         }
         
         return maxVelocityReturn;
     }
 
     @Override
     public List<AccelerationAtTime> getAccelerationProfile(Train requester, Time timer) {
         //Get a list of distances at which the max velocity changes
         List<MaxVelocity> maxVelocity=this.getMaxVelocityList(requester, timer);
 
         ArrayList<AccelerationAtTime> returnList=new ArrayList<AccelerationAtTime>();
         double currentVelocity=requester.getVelocity();
         double currentTime=0;
         double currentPosition=0;
         ListIterator<MaxVelocity> limitIterator=maxVelocity.listIterator();
         while(limitIterator.hasNext())
         {
             MaxVelocity currentLimit=limitIterator.next();
             ListIterator<MaxVelocity> secondIterator=maxVelocity.listIterator(limitIterator.nextIndex());
             double currentMinBrakeDistance=Double.POSITIVE_INFINITY;
             double currentBrakeTime=0;
             double nextDistance=Double.POSITIVE_INFINITY;
             int currentLimitAdd=0;
             int targetLimitAdd=0;
             while(secondIterator.hasNext())
             {
                 MaxVelocity nextLimit=secondIterator.next();
                 if(nextDistance>nextLimit.distance)
                 {
                     nextDistance=nextLimit.distance;
                 }
                 if(nextLimit.maxVelocity<currentLimit.maxVelocity)
                 {
                     double tmpBrakeTime=(currentLimit.maxVelocity-nextLimit.maxVelocity)
                             /requester.getMaxDeceleration();
                     double tmpBrakeDistance=nextLimit.distance-currentPosition
                             -requester.getLength()-nextLimit.maxVelocity*tmpBrakeTime
                             -requester.getMaxDeceleration()*tmpBrakeTime*tmpBrakeTime/2;
 
                     if(tmpBrakeDistance<currentMinBrakeDistance && tmpBrakeDistance<=nextDistance)
                     {
                         targetLimitAdd=currentLimitAdd;
                         currentMinBrakeDistance=tmpBrakeDistance;
                         currentBrakeTime=tmpBrakeTime;
                         nextDistance=nextLimit.distance;
                     }
                 }
                 ++currentLimitAdd;
             }
 
             if (currentVelocity<currentLimit.maxVelocity) { //Accelerate
                 returnList.add(new AccelerationAtTime(currentTime, requester.getMaxAcceleration()));
                 double accelerationTime=(currentLimit.maxVelocity-currentVelocity)
                         /requester.getMaxAcceleration();
                 double accelerationDistance=currentLimit.distance-currentPosition+currentVelocity*accelerationTime
                         +requester.getMaxAcceleration()*accelerationTime*accelerationTime/2;
                 
                 if(currentBrakeTime>0) {//If braking is required
                     if(accelerationDistance<currentMinBrakeDistance) { //Can accelerate fully
                         currentVelocity+=accelerationTime*requester.getMaxAcceleration();
                         currentTime+=accelerationTime;
                         returnList.add(new AccelerationAtTime(currentTime, 0));
                         currentTime+=(currentMinBrakeDistance-accelerationDistance)/currentVelocity;
                         returnList.add(new AccelerationAtTime(currentTime, -requester.getMaxDeceleration()));
                         currentTime+=currentBrakeTime;
                         currentVelocity-=currentBrakeTime*requester.getMaxDeceleration();
                         currentPosition=nextDistance-requester.getLength();
                     } else {
                         double accelCoeff=(requester.getMaxAcceleration()+requester.getMaxDeceleration())
                                 /requester.getMaxDeceleration();
                         double deltaAccelerationTime=(currentLimit.maxVelocity*accelCoeff
                                 -Math.sqrt(accelCoeff*(currentLimit.maxVelocity*currentLimit.maxVelocity
                                 *accelCoeff-2*requester.getMaxAcceleration()
                                 *(accelerationDistance-currentMinBrakeDistance))))
                                 /(accelCoeff*requester.getMaxAcceleration());
                         if(Double.isNaN(deltaAccelerationTime)) {
                             deltaAccelerationTime=accelerationTime;
                         }
                         accelerationTime=Math.max(accelerationTime-deltaAccelerationTime,0);
                         currentTime+=accelerationTime;
                         currentVelocity+=accelerationTime*requester.getMaxAcceleration();
                         returnList.add(new AccelerationAtTime(currentTime, -requester.getMaxDeceleration()));
                         double deltaDecelerationTime=deltaAccelerationTime*requester.getMaxAcceleration()
                                 /requester.getMaxDeceleration();
                         currentBrakeTime=Math.max(currentBrakeTime-deltaDecelerationTime,0);
                         currentTime+=currentBrakeTime;
                         currentVelocity-=currentBrakeTime*requester.getMaxDeceleration();
                         currentPosition=nextDistance-requester.getLength();
                     }
                 } else {//If no braking is required
                     if(accelerationDistance+currentPosition<nextDistance) {
                         returnList.add(new AccelerationAtTime(currentTime+accelerationTime, 0));
                         currentVelocity+=accelerationTime*requester.getMaxAcceleration();
                         currentTime+=accelerationTime+(nextDistance-currentPosition
                                 -accelerationDistance)/currentVelocity;
                         currentPosition=nextDistance;
                     } else {
                         accelerationTime=(-currentVelocity+Math.sqrt(currentVelocity*currentVelocity
                                 +2*requester.getMaxAcceleration()*(nextDistance-currentPosition)))
                                 /requester.getMaxAcceleration();
                         currentTime+=accelerationTime;
                         currentVelocity+=accelerationTime*requester.getMaxAcceleration();
                         currentPosition=nextDistance;
                     }
                 }
             } else {//Velocity is already at maximum
                 returnList.add(new AccelerationAtTime(currentTime, 0));
                 if(currentBrakeTime>0) { //If braking is required
                     currentTime+=Math.max(currentMinBrakeDistance/currentVelocity,0);
                     returnList.add(new AccelerationAtTime(currentTime,-requester.getMaxDeceleration()));
                     currentTime+=currentBrakeTime;
                     currentVelocity-=currentBrakeTime*requester.getMaxDeceleration();
                     currentPosition=nextDistance-requester.getLength();
                 } else { //If no braking is required
                     currentTime+=(nextDistance-currentPosition)/currentVelocity;
                     currentPosition=nextDistance;
                 }
             }
 
             //Jump over the already calculated parts
             for(int i=0;i<targetLimitAdd;++i)
             {
                 limitIterator.next();
             }

//            if(currentMinBrakeDistance<0 && currentLimit.maxVelocity<=currentVelocity) {
//                returnList=new ArrayList<AccelerationAtTime>();
//                returnList.add(new AccelerationAtTime(0, -requester.getMaxDeceleration()));
//                while(limitIterator.hasNext()) {limitIterator.next();}
//            }
         }

         return returnList;
     }
 
     final protected class MaxVelocity implements Comparable<MaxVelocity>{
         public final double distance;
         public final double maxVelocity;
         
         public MaxVelocity(double myDistance,double myMaxVelocity) {
             this.distance=myDistance;
             this.maxVelocity=myMaxVelocity;
         }
 
         @Override
         public String toString()
         {
             return "["+Double.toString(this.distance)+","+Double.toString(this.maxVelocity)+"]";
         }
 
         @Override
         public int compareTo(MaxVelocity other) {
             return Double.compare(this.distance, other.distance);
         }
     }
 }
