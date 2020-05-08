 package madsdf.shimmer.gui;
 
 import java.util.LinkedList;
 
 /**
  * A sample received from the shimmer containing accel / gyro measurements
  * You can choose if you want calibrated or uncalibrated data by listening
  * for the correct class on the eventbus (that's why we use two different
  * classes for calibrated/uncalibrated).
  */
 public class AccelGyro {
     // Base class for samples
     public static abstract class Sample {
         public final long receivedTimestampMillis;
         public final float accel[] = new float[3];
         public final float gyro[] = new float[3];
 
         public Sample(long time, float[] accel, float[] gyro) {
             this.receivedTimestampMillis = time;
             System.arraycopy(accel, 0, this.accel, 0, 3);
             System.arraycopy(gyro, 0, this.gyro, 0, 3);
         }
         
         @Override
         public String toString() {
             return "sample @ " + receivedTimestampMillis + ", accel = ("
                     + accel[0] + ", " + accel[1] + ", " + accel[2] + "), " +
                     "gyro = (" + gyro[0] + ", " + gyro[1] + ", " + gyro[2] + ")";
         }
         
     }
     public static class CalibratedSample extends Sample {
         public CalibratedSample(long time, float[] accel, float[] gyro) {
             super(time, accel, gyro);
         }
     }
     
     public static class UncalibratedSample extends Sample {
         public UncalibratedSample(long time, float[] accel, float[] gyro) {
             super(time, accel, gyro);
         }
         
         // TODO: Remove : this is only for backward-compatibility
         public float getVal(int i) {
             if (i < 1 || i > 6) {
                 throw new IllegalArgumentException("Invaild i : " + i);
             }
             if (i <= 3) {
                 return accel[i - 1];
             } else {
                return accel[i - 4];
             }
             
         }
     }
 }
