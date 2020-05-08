 /*
  * Copyright 2012 Marius Volkhart
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
//package lab2;
 
 import java.util.Observable;
 
 /**
  * Maintains the current state of the application. Allows both push and pull
  * 
  * @author volkhartm
  * @version 1.0
  * @created 05-Dec-2012 9:54:35 AM
  */
 public class GpsSubject extends Observable {
 
     private GPSCoordinate mGPSCoordinate;
 
     /**
      * Sets the most recently acquired corrdinates
      * 
      * @param newCoordinate The most recently acquired coordinates.
      */
     public void setCoordinates(GPSCoordinate newCoordinate) {
         mGPSCoordinate = newCoordinate;
         coordinatesChanged();
     }
 
     private void coordinatesChanged() {
         setChanged();
         notifyObservers(mGPSCoordinate);
     }
 
     public GPSCoordinate getCoordinates() {
         return mGPSCoordinate;
     }
 
 }
