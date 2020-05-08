 package com.RoboMobo;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Роман
  * Date: 31.07.13
  * Time: 12:51
  */
 public class RMGR
 {
     public static Bitmap[] PICKUP_0;
     public static Bitmap[] PICKUP_1;
     public static Bitmap CHAR_0;
     public static Bitmap[] TILE_0;
     public static Bitmap[] TILE_1;
     public static short tile_0_iterator = 0;
     public static short tile_1_iterator = 0;
     public static short pickup_0_iterator = 0;
     public static short pickup_1_iterator = 0;
 
     public static void init(ActivityMain activityMain)
     {
         TILE_0 = new Bitmap[13];
        TILE_1 = new Bitmap[14];
         PICKUP_0 = new Bitmap[25];
         PICKUP_1 = new Bitmap[11];
 
         PICKUP_0[0] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_0);
         PICKUP_0[1] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_1);
         PICKUP_0[2] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_2);
         PICKUP_0[3] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_3);
         PICKUP_0[4] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_4);
         PICKUP_0[5] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_5);
         PICKUP_0[6] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_6);
         PICKUP_0[7] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_7);
         PICKUP_0[8] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_8);
         PICKUP_0[9] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_9);
         PICKUP_0[10] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_10);
         PICKUP_0[11] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_11);
         PICKUP_0[12] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_12);
         PICKUP_0[13] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_13);
         PICKUP_0[14] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_14);
         PICKUP_0[15] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_15);
         PICKUP_0[16] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_16);
         PICKUP_0[17] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_17);
         PICKUP_0[18] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_18);
         PICKUP_0[19] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_19);
         PICKUP_0[20] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_20);
         PICKUP_0[21] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_21);
         PICKUP_0[22] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_22);
         PICKUP_0[23] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_23);
         PICKUP_0[24] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_0_24);
 
         PICKUP_1[0] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_1_0);
         PICKUP_1[1] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_1_1);
         PICKUP_1[2] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_1_2);
         PICKUP_1[3] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_1_3);
         PICKUP_1[4] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_1_4);
         PICKUP_1[5] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_1_5);
         PICKUP_1[6] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_1_6);
         PICKUP_1[7] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_1_7);
         PICKUP_1[8] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_1_8);
         PICKUP_1[9] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_1_9);
         PICKUP_1[10] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.pickup_1_10);
 
         CHAR_0 = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.char_test);
 
         TILE_0[0] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_0_0);
         TILE_0[1] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_0_1);
         TILE_0[2] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_0_2);
         TILE_0[3] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_0_3);
         TILE_0[4] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_0_4);
         TILE_0[5] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_0_5);
         TILE_0[6] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_0_6);
         TILE_0[7] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_0_7);
         TILE_0[8] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_0_8);
         TILE_0[9] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_0_9);
         TILE_0[10] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_0_10);
         TILE_0[11] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_0_11);
         TILE_0[12] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_0_12);
 
         TILE_1[0] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_1_0);
         TILE_1[1] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_1_1);
         TILE_1[2] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_1_2);
         TILE_1[3] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_1_3);
         TILE_1[4] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_1_4);
         TILE_1[5] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_1_5);
         TILE_1[6] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_1_6);
         TILE_1[7] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_1_7);
         TILE_1[8] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_1_8);
         TILE_1[9] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_1_9);
         TILE_1[10] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_1_10);
         TILE_1[11] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_1_11);
         TILE_1[12] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_1_12);
         TILE_1[12] = BitmapFactory.decodeResource(activityMain.getResources(), R.drawable.tile_1_13);
     }
 }
