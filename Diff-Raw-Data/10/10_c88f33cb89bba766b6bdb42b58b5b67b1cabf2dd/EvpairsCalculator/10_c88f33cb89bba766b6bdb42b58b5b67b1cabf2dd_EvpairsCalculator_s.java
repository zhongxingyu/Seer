 package ru.neverdark.phototools.evcalculator;
 
 import java.util.Arrays;
 
 public class EvpairsCalculator {
 
     private int mCurrentAperturePosition;
     private int mCurrentIsoPosition;
     private int mCurrentShutterSpeedPosition;
     private int mNewAperturePosition;
     private int mNewIsoPostion;
     private int mNewShutterSpeedPosition;
     private int mIndex;
     
     /** For error handling. Must not equ with any of exposition pairs. */
     public static final int INVALID_INDEX = -100;
     
     public static final String ISO_LIST[] = { "", "25", "50", "100", "200", "400", "800",
             "1600", "3200", "6400", "12800" };
     
     public static final String SHUTTER_SPEED_LIST[]  = { "", "512 min",
         "256 min", "128 min", "64 min", "32 min", "16 min", "8 min", "4 min", "2 min",
         "1 min", "30 sec", "15 sec", "8 sec", "4 sec", "2 sec", "1 sec",
         "1/2 sec", "1/4 sec", "1/8 sec", "1/15 sec", "1/30 sec",
         "1/60 sec", "1/125 sec", "1/250 sec", "1/500 sec", "1/1000 sec",
         "1/2000 sec", "1/4000 sec", "1/8000 sec" };
         
     public static final String APERTURE_LIST[] = { "", "1.4", "2.0", "2.8",
         "4", "5.6", "8", "11", "16", "22", "32" };
     
     private final int EV_TABLE[][] = {
         { -16, -17, -18, -19, -20, -21, -22, -23, -24, -25 },
         { -15, -16, -17, -18, -19, -20, -21, -22, -23, -24 },
         { -14, -15, -16, -17, -18, -19, -20, -21, -22, -23 },
         { -13, -14, -15, -16, -17, -18, -19, -20, -21, -22 },
         { -12, -13, -14, -15, -16, -17, -18, -19, -20, -21 },
         { -11, -12, -13, -14, -15, -16, -17, -18, -19, -20 },
         { -10, -11, -12, -13, -14, -15, -16, -17, -18, -19 },
         { -9, -10, -11, -12, -13, -14, -15, -16, -17, -18 },
         { -8, -9, -10, -11, -12, -13, -14, -15, -16, -17 },
         { -7, -8, -9, -10, -11, -12, -13, -14, -15, -16 },
         { -6, -7, -8, -9, -10, -11, -12, -13, -14, -15 },
         { -5, -6, -7, -8, -9, -10, -11, -12, -13, -14 },
         { -4, -5, -6, -7, -8, -9, -10, -11, -12, -13 },
         { -3, -4, -5, -6, -7, -8, -9, -10, -11, -12 },
         { -2, -3, -4, -5, -6, -7, -8, -9, -10, -11 },
         { -1, -2, -3, -4, -5, -6, -7, -8, -9, -10 },
         { 0, -1, -2, -3, -4, -5, -6, -7, -8, -9 },
         { 1, 0, -1, -2, -3, -4, -5, -6, -7, -8 },
         { 2, 1, 0, -1, -2, -3, -4, -5, -6, -7 },
         { 3, 2, 1, 0, -1, -2, -3, -4, -5, -6 },
         { 4, 3, 2, 1, 0, -1, -2, -3, -4, -5 },
         { 5, 4, 3, 2, 1, 0, -1, -2, -3, -4 },
         { 6, 5, 4, 3, 2, 1, 0, -1, -2, -3 },
         { 7, 6, 5, 4, 3, 2, 1, 0, -1, -2 },
         { 8, 7, 6, 5, 4, 3, 2, 1, 0, -1 },
         { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 },
         { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 },
         { 11, 10, 9, 8, 7, 6, 5, 4, 3, 2 },
         { 12, 11, 10, 9, 8, 7, 6, 5, 4, 3 },
         { 13, 12, 11, 10, 9, 8, 7, 6, 5, 4 },
         { 14, 13, 12, 11, 10, 9, 8, 7, 6, 5 },
         { 15, 14, 13, 12, 11, 10, 9, 8, 7, 6 },
         { 16, 15, 14, 13, 12, 11, 10, 9, 8, 7 },
         { 17, 16, 15, 14, 13, 12, 11, 10, 9, 8 },
         { 18, 17, 16, 15, 14, 13, 12, 11, 10, 9 },
         { 19, 18, 17, 16, 15, 14, 13, 12, 11, 10 },
         { 20, 19, 18, 17, 16, 15, 14, 13, 12, 11 },
         { 21, 20, 19, 18, 17, 16, 15, 14, 13, 12 },
         { 22, 21, 20, 19, 18, 17, 16, 15, 14, 13 },
         { 23, 22, 21, 20, 19, 18, 17, 16, 15, 14 } };
     
     private final String SHUTTERS_TABLE[][] = {
         { "0", "0", "0", "0", "0", "0", "0", "0", "0", "0" },
         { "0", "0", "0", "0", "0", "0", "0", "0", "0", "0" },
         { "0", "0", "0", "0", "0", "0", "0", "0", "0", "0" },
         { "0", "0", "0", "0", "0", "0", "0", "0", "0", "0" },
         { "512 min", "0", "0", "0", "0", "0", "0", "0", "0", "0" },
         { "256 min", "512 min", "0", "0", "0", "0", "0", "0", "0", "0" },
         { "128 min", "256 min", "512 min", "0", "0", "0", "0", "0", "0", "0" },
         { "64 min", "128 min", "256 min", "512 min", "0", "0", "0", "0", "0", "0" },
         { "32 min", "64 min", "128 min", "256 min", "512 min", "0", "0", "0", "0", "0" },
         { "16 min", "32 min", "64 min", "128 min", "256 min", "512 min", "0", "0", "0", "0" },
         { "8 min", "16 min", "32 min", "64 min", "128 min", "256 min", "512 min", "0", "0", "0" },
         { "4 min", "8 min", "16 min", "32 min", "64 min", "128 min", "256 min", "512 min", "0", "0" },
         { "2 min", "4 min", "8 min", "16 min", "32 min", "64 min", "128 min", "256 min", "512 min", "0" },
         { "1 min", "2 min", "4 min", "8 min", "16 min", "32 min", "64 min", "128 min", "256 min", "512 min" },
         { "30 sec", "1 min", "2 min", "4 min", "8 min", "16 min", "32 min", "64 min", "128 min", "256 min" },
         { "15 sec", "30 sec", "1 min", "2 min", "4 min", "8 min", "16 min", "32 min", "64 min", "128 min" },
         { "8 sec", "15 sec", "30 sec", "1 min", "2 min", "4 min", "8 min", "16 min", "32 min", "64 min" },
         { "4 sec", "8 sec", "15 sec", "30 sec", "1 min", "2 min", "4 min", "8 min", "16 min", "32 min" },
         { "2 sec", "4 sec", "8 sec", "15 sec", "30 sec", "1 min", "2 min", "4 min", "8 min", "16 min" },
         { "1 sec", "2 sec", "4 sec", "8 sec", "15 sec", "30 sec", "1 min", "2 min", "4 min", "8 min" },
         { "1/2 sec", "1 sec", "2 sec", "4 sec", "8 sec", "15 sec", "30 sec", "1 min", "2 min", "4 min" },
         { "1/4 sec", "1/2 sec", "1 sec", "2 sec", "4 sec", "8 sec", "15 sec", "30 sec", "1 min", "2 min" },
         { "1/8 sec", "1/4 sec", "1/2 sec", "1 sec", "2 sec", "4 sec", "8 sec", "15 sec", "30 sec", "1 min" },
         { "1/15 sec", "1/8 sec", "1/4 sec", "1/2 sec", "1 sec", "2 sec", "4 sec", "8 sec", "15 sec", "30 sec" },
         { "1/30 sec", "1/15 sec", "1/8 sec", "1/4 sec", "1/2 sec", "1 sec", "2 sec", "4 sec", "8 sec", "15 sec" },
         { "1/60 sec", "1/30 sec", "1/15 sec", "1/8 sec", "1/4 sec", "1/2 sec", "1 sec", "2 sec", "4 sec", "8 sec" },
         { "1/125 sec", "1/60 sec", "1/30 sec", "1/15 sec", "1/8 sec", "1/4 sec", "1/2 sec", "1 sec", "2 sec", "4 sec" },
         { "1/250 sec", "1/125 sec", "1/60 sec", "1/30 sec", "1/15 sec", "1/8 sec", "1/4 sec", "1/2 sec", "1 sec", "2 sec" },
         { "1/500 sec", "1/250 sec", "1/125 sec", "1/60 sec", "1/30 sec", "1/15 sec", "1/8 sec", "1/4 sec", "1/2 sec", "1 sec" },
         { "1/1000 sec", "1/500 sec", "1/250 sec", "1/125 sec", "1/60 sec", "1/30 sec", "1/15 sec", "1/8 sec", "1/4 sec", "1/2 sec" },
         { "1/2000 sec", "1/1000 sec", "1/500 sec", "1/250 sec", "1/125 sec", "1/60 sec", "1/30 sec", "1/15 sec", "1/8 sec", "1/4 sec" },
         { "1/4000 sec", "1/2000 sec", "1/1000 sec", "1/500 sec", "1/250 sec", "1/125 sec", "1/60 sec", "1/30 sec", "1/15 sec", "1/8 sec" },
         { "1/8000 sec", "1/4000 sec", "1/2000 sec", "1/1000 sec", "1/500 sec", "1/250 sec", "1/125 sec", "1/60 sec", "1/30 sec", "1/15 sec" },
         { "0", "1/8000 sec", "1/4000 sec", "1/2000 sec", "1/1000 sec", "1/500 sec", "1/250 sec", "1/125 sec", "1/60 sec", "1/30 sec" },
         { "0", "0", "1/8000 sec", "1/4000 sec", "1/2000 sec", "1/1000 sec", "1/500 sec", "1/250 sec", "1/125 sec", "1/60 sec" },
         { "0", "0", "0", "1/8000 sec", "1/4000 sec", "1/2000 sec", "1/1000 sec", "1/500 sec", "1/250 sec", "1/125 sec" },
         { "0", "0", "0", "0", "1/8000 sec", "1/4000 sec", "1/2000 sec", "1/1000 sec", "1/500 sec", "1/250 sec" },
         { "0", "0", "0", "0", "0", "1/8000 sec", "1/4000 sec", "1/2000 sec", "1/1000 sec", "1/500 sec" },
         { "0", "0", "0", "0", "0", "0", "1/8000 sec", "1/4000 sec", "1/2000 sec", "1/1000 sec" },
         { "0", "0", "0", "0", "0", "0", "0", "1/8000 sec", "1/4000 sec", "1/2000 sec" } };
     
     
     public EvpairsCalculator(int currentAperturePosition,
             int currentIsoPosition, int currentShutterSpeedPosition,
             int newAperturePosition, int newIsoPostion,
             int newShutterSpeedPosition) {
 
         mCurrentAperturePosition = currentAperturePosition;
         mCurrentIsoPosition = currentIsoPosition;
         mCurrentShutterSpeedPosition = currentShutterSpeedPosition;
         
         mNewAperturePosition = newAperturePosition;
         mNewIsoPostion = newIsoPostion;
         mNewShutterSpeedPosition = newShutterSpeedPosition;
 
         mIndex = INVALID_INDEX;
     }
     
     /**
      * Function calculates the required value based on indices obtained in the class constructor.
      * @return index for the empty spinner or INVALID_INDEX on error
      */
     public int calculate() {                
         if (mNewAperturePosition == 0) {
             calculateAperture();
         } else if (mNewIsoPostion == 0) {
             calculateIso();
         } else {
             calculateShutterSpeed();
         }
                 
         return mIndex;
     }
     
     /**
      * Function calculates the aperture
      */
     private void calculateAperture() {
         int isoNewIndex = getIsoNewIndex();
         String shutter = SHUTTER_SPEED_LIST[mNewShutterSpeedPosition];
 
         if (isoNewIndex != INVALID_INDEX) {
            for (int i = 0; i < SHUTTER_SPEED_LIST.length - 1; i++) {
                 if (shutter.equals(SHUTTERS_TABLE[isoNewIndex][i])) {
                     mIndex = i + 1;
                     break;
                 }
             }
         }
     }
     
     /**
      * Function calculates the shutter speed
      */
     private void calculateShutterSpeed() {
         int apertureNewColumnNumber = mNewAperturePosition - 1;
         int isoNewIndex = getIsoNewIndex();                
         
         if (isoNewIndex != INVALID_INDEX) {
             String shutter = SHUTTERS_TABLE[isoNewIndex][apertureNewColumnNumber];                    
             mIndex = Arrays.asList(SHUTTER_SPEED_LIST).indexOf(shutter);
         }
     }
 
     /**
      * Function calculates the ISO
      */
     private void calculateIso() {
         int i;
         int ev = getEv();
         int isoLine = INVALID_INDEX;
         int apertureNewColumnIndex = mNewAperturePosition - 1;
         String shutter = SHUTTER_SPEED_LIST[mNewShutterSpeedPosition];
 
         if (ev != INVALID_INDEX) {
             for (i = 0; i < SHUTTERS_TABLE.length; i++) {
                 if (shutter.equals(SHUTTERS_TABLE[i][apertureNewColumnIndex])) {
                     isoLine = i;
                     break;
                 }
             }
 
             if (isoLine != INVALID_INDEX) {
                 for (i = 0; i < ISO_LIST.length - 1; i++) {
                     if (EV_TABLE[isoLine][i] == ev) {
                         mIndex = i + 1;
                         break;
                     }
                 }
             }
         }
     }
 
     /**
      * Function gets the number of exposure pairs that matches the specified parameters
      * 
      * @return number pf exposure pair or INVLAID_INDEX if EV not found
      */
     private int getEv() {
         int ev = INVALID_INDEX;
         int isoCurrentColumnNumber = mCurrentIsoPosition - 1;
         int apertureCurrentColumnNumber = mCurrentAperturePosition - 1;
         String currentShutterSpeed = SHUTTER_SPEED_LIST[mCurrentShutterSpeedPosition];
 
         for (int i = 0; i < SHUTTERS_TABLE.length; i++) {
             if (currentShutterSpeed
                     .equals(SHUTTERS_TABLE[i][apertureCurrentColumnNumber])) {
                 ev = EV_TABLE[i][isoCurrentColumnNumber];
                 break;
             }
         }
 
         return ev;
     }
     
     /**
      * Function determines the index of the ISO
      * 
      * @return ISO index or INVALID_INDEX if ISO not found
      */
     private int getIsoNewIndex() {
         int ev = getEv();
         int index = INVALID_INDEX;
         int isoNewColumnNumber = mNewIsoPostion - 1;
 
         if (ev != INVALID_INDEX) {
             for (int i = 0; i < SHUTTERS_TABLE.length; i++) {
                 if (ev == EV_TABLE[i][isoNewColumnNumber]) {
                     index = i;
                     break;
                 }
             }
         }
 
         return index;
     }
     
 }
