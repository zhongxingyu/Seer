 /*******************************************************************************
  * Copyright (C) 2013-2014 Artem Yankovskiy (artemyankovskiy@gmail.com).
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package ru.neverdark.phototools.utils.evcalculator;
 
 import java.util.Arrays;
 
 import ru.neverdark.phototools.utils.Log;
 
 
 public class EvCalculator {
 
     private int mCurrentAperturePosition;
     private int mCurrentIsoPosition;
     private int mCurrentShutterSpeedPosition;
     private int mNewAperturePosition;
     private int mNewIsoPostion;
     private int mNewShutterSpeedPosition;
     private int mIndex;
     private EvTable mEvTable;
     
     
     public static final int FULL_STOP = 0;
     public static final int HALF_STOP = 1;
     public static final int THIRD_STOP = 2;
     
     /** For error handling. Must not equ with any of exposition pairs. */
     public static final int INVALID_INDEX = -100;
     
     private String ISO_LIST[];
     
     private String SHUTTER_SPEED_LIST[];
         
     private String APERTURE_LIST[];
     
     private int EV_TABLE[][];
     
     private String SHUTTERS_TABLE[][];
     
     
     public void prepare(int currentAperturePosition,
             int currentIsoPosition, int currentShutterSpeedPosition,
             int newAperturePosition, int newIsoPostion,
             int newShutterSpeedPosition) {
 
         mCurrentAperturePosition = currentAperturePosition;
         mCurrentIsoPosition = currentIsoPosition;
         mCurrentShutterSpeedPosition = currentShutterSpeedPosition;
 
         mNewAperturePosition = newAperturePosition - 1;
         mNewIsoPostion = newIsoPostion -1;
         mNewShutterSpeedPosition = newShutterSpeedPosition  - 1;
 
         Log.variable("mCurrentAperturePosition", String.valueOf(mCurrentAperturePosition));
         Log.variable("mCurrentIsoPosition", String.valueOf(mCurrentIsoPosition));
         Log.variable("mCurrentShutterSpeedPosition", String.valueOf(mCurrentShutterSpeedPosition));
         
         Log.variable("mNewAperturePosition", String.valueOf(mNewAperturePosition));
         Log.variable("mNewIsoPostion", String.valueOf(mNewIsoPostion));
         Log.variable("mNewShutterSpeedPosition", String.valueOf(mNewShutterSpeedPosition));
         
         mIndex = INVALID_INDEX;
     }
     
     /**
      * Inits local arrays by EV step
      * @param evStep ev step
      */
     public void initArrays(final int evStep) {
         switch (evStep) {
         case FULL_STOP:
             mEvTable = new EvFullTable();
             Log.message("EV FULL");
             break;
         case HALF_STOP:
             mEvTable = new EvHalfTable();
             Log.message("Ev HALF");
             break;
         case THIRD_STOP:
             mEvTable = new  EvThirdTable();
             Log.message("Ev THIRD");
             break;
         }
         
         ISO_LIST = mEvTable.getIsoList();
         SHUTTER_SPEED_LIST = mEvTable.getShutterList();
         APERTURE_LIST = mEvTable.getApertureList();
         EV_TABLE = mEvTable.getEvTable();
         SHUTTERS_TABLE = mEvTable.getShutterTable();
     }
     
     /**
      * Gets ISO list
      * @return array contains possible ISO
      */
     public String[] getIsoList() {
         return ISO_LIST;
     }
     
     /**
      * Gets aperture list 
      * @return array contains possible apertures
      */
     public String[] getApertureList() {
         return APERTURE_LIST;
     }
     
     /**
      * Gets shutter list
      * @return array contains possible shutter speed
      */
     public String[] getShutterList() {
         return SHUTTER_SPEED_LIST;
     }
     
     
     
     /**
      * Function calculates the required value based on indices obtained in the class constructor.
      * @return index for the empty spinner or INVALID_INDEX on error
      */
     public int calculate() {
         if (mNewAperturePosition == -1) {
             calculateAperture();
         } else if (mNewIsoPostion == -1) {
             calculateIso();
         } else {
             calculateShutterSpeed();
         }
                
        mIndex++;
        Log.variable("mIndex", String.valueOf("mIndex"));
         
         return mIndex;
     }
     
     /**
      * Function calculates the aperture
      */
     private void calculateAperture() {
         int isoNewIndex = getIsoNewIndex();
         String shutter = SHUTTER_SPEED_LIST[mNewShutterSpeedPosition];
         
         Log.variable("shutter", String.valueOf(shutter));
         Log.variable("isoNewIndex", String.valueOf(isoNewIndex));
 
         if (isoNewIndex != INVALID_INDEX) {
             for (int i = 0; i < SHUTTERS_TABLE[isoNewIndex].length; i++) {
                 if (shutter.equals(SHUTTERS_TABLE[isoNewIndex][i])) {
                     mIndex = i;
                     Log.variable("mIndex", String.valueOf(mIndex));
                     break;
                 }
             }
         }
     }
     
     /**
      * Function calculates the shutter speed
      */
     private void calculateShutterSpeed() {
         int apertureNewColumnNumber = mNewAperturePosition;
         int isoNewIndex = getIsoNewIndex();
         Log.variable("isoNewIndex", String.valueOf(isoNewIndex));
         
         if (isoNewIndex != INVALID_INDEX) {
             String shutter = SHUTTERS_TABLE[isoNewIndex][apertureNewColumnNumber];
             mIndex = Arrays.asList(SHUTTER_SPEED_LIST).indexOf(shutter);
             
             Log.variable("shutter", shutter);
             Log.variable("mIndex", String.valueOf(mIndex));
         }
     }
 
     /**
      * Function calculates the ISO
      */
     private void calculateIso() {
         int i;
         int ev = getEv();
         int isoLine = INVALID_INDEX;
         int apertureNewColumnIndex = mNewAperturePosition;
         String shutter = SHUTTER_SPEED_LIST[mNewShutterSpeedPosition];
 
         Log.variable("ev", String.valueOf(ev));
         Log.variable("apertureNewColumnIndex", String.valueOf(apertureNewColumnIndex));
         Log.variable("shutter", shutter);
         
         if (ev != INVALID_INDEX) {
             for (i = 0; i < SHUTTERS_TABLE.length; i++) {
                 if (shutter.equals(SHUTTERS_TABLE[i][apertureNewColumnIndex])) {
                     isoLine = i;
                     break;
                 }
             }
             
             Log.variable("isoLine", String.valueOf(isoLine));
             
             if (isoLine != INVALID_INDEX) {
                 for (i = 0; i < ISO_LIST.length - 1; i++) {
                     if (EV_TABLE[isoLine][i] == ev) {
                         mIndex = i;
                         Log.variable("mIndex", String.valueOf(mIndex));
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
         int isoCurrentColumnNumber = mCurrentIsoPosition;
         int apertureCurrentColumnNumber = mCurrentAperturePosition;
         String currentShutterSpeed = SHUTTER_SPEED_LIST[mCurrentShutterSpeedPosition];
 
         Log.variable("currentShutterSpeed", currentShutterSpeed);
         
         for (int i = 0; i < SHUTTERS_TABLE.length; i++) {
             if (currentShutterSpeed
                     .equals(SHUTTERS_TABLE[i][apertureCurrentColumnNumber])) {
                 ev = EV_TABLE[i][isoCurrentColumnNumber];
                 Log.variable("ev", String.valueOf(ev));
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
         int isoNewColumnNumber = mNewIsoPostion;
 
         Log.variable("ev", String.valueOf(ev));
         
         if (ev != INVALID_INDEX) {
             for (int i = 0; i < SHUTTERS_TABLE.length; i++) {
                 if (ev == EV_TABLE[i][isoNewColumnNumber]) {
                     index = i;
                     Log.variable("index", String.valueOf(index));
                     break;
                 }
             }
         }
 
         return index;
     }
     
 }
