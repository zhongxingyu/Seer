 package com.example.reboxed;
 
 public class SensorDataHistory {
     private final int mHistoryCapacity;
     private final SensorData[] mSensorDataArray;
     private int mCurrentSensorDataIndex = 0;
     private int mHistoryCount;
     
     public SensorDataHistory(int historySize) {
         mHistoryCapacity = historySize;
         mSensorDataArray = new SensorData[historySize];
         
         clearSensorDataHistory();
     }
     
     public void clearSensorDataHistory() {
         
         mCurrentSensorDataIndex = 0;
         mHistoryCount = 0;
     }
     
     public SensorData getAverage() {
         if (mHistoryCount == 0) return null;
         
         SensorData averageData = new SensorData();
         averageData.accel = 0;
         averageData.motion = 0;
         averageData.smoke = 0;
         
         for(int i =0; i < mHistoryCount; i ++){
             averageData.accel += mSensorDataArray[i].accel;
             averageData.motion += mSensorDataArray[i].motion;
             averageData.smoke += mSensorDataArray[i].smoke;
         }
         
         averageData.accel = averageData.accel/mHistoryCount;
         averageData.motion = averageData.motion/mHistoryCount;
         averageData.smoke = averageData.smoke/mHistoryCount;        
 
         return averageData;
     }
     
     public void addSensorData(SensorData data) {
        if(data.accel != Float.NaN){
             mSensorDataArray[mCurrentSensorDataIndex++] = data;
             if (mCurrentSensorDataIndex >= mHistoryCapacity) mCurrentSensorDataIndex = 0;
             
             if (mHistoryCount != mHistoryCapacity) mHistoryCount++;
         }
     } 
 
 }
