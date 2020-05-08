 /**
  * Copyright 2012 Kevin J. Jones (http://www.kevinjjones.co.uk)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package uk.co.kevinjjones.vehicle;
 
 import uk.co.kevinjjones.Log;
 import uk.co.kevinjjones.model.*;
 
 /**
  * Speed virtual stream.
  */
 public class SpeedStream extends StreamBase {
 
     public static String NAME = "Speed";
     private View _view;
     private ROStream _lusp;
     private ROStream _rusp;
     private ROStream _ldsp;
     private ROStream _rdsp;
     private ROStream _lowt;
 
     public SpeedStream() {
     }
 
     @Override
     public void setView(View view, Object arg, ParamHandler handler, WithError<Boolean, BasicError> ok) {
         super.setView(view, arg, handler, ok);
         _view = view;
         _lusp = _view.getStream(WheelStream.name(WheelStream.LUSP));
         _rusp = _view.getStream(WheelStream.name(WheelStream.RUSP));
         _ldsp = _view.getStream(WheelStream.name(WheelStream.LDSP));
         _rdsp = _view.getStream(WheelStream.name(WheelStream.RDSP));
         _lowt = _view.getStream(LowThrottleStream.NAME);
 
         if (_lusp != null && _rusp != null && _ldsp != null && _rdsp != null) {
             ok.addError(new BasicError(BasicError.WARN, "Using four wheel speeds to calculate speed"));
         } else if (_lusp != null && _rusp != null) {
             ok.addError(new BasicError(BasicError.WARN, "Using two undriven wheel sensors to calculate speed"));
         } else if (_lusp != null) {
             ok.addError(new BasicError(BasicError.WARN, "Using left undriven wheel sensors to calculate speed"));
         } else if (_rusp != null) {
             ok.addError(new BasicError(BasicError.WARN, "Using right undriven wheel sensors to calculate speed"));
         } else if (_ldsp != null && _rdsp != null) {
             ok.addError(new BasicError(BasicError.WARN, "Using two driven wheel sensors to calculate speed"));
         } else if (_ldsp != null) {
             ok.addError(new BasicError(BasicError.WARN, "Using left driven wheel sensors to calculate speed"));
         } else if (_rdsp != null) {
             ok.addError(new BasicError(BasicError.WARN, "Using right driven wheel sensors to calculate speed"));
         } else {
             ok.addError(new BasicError(BasicError.WARN, "No wheel speeds available"));
             ok.setValue(false);
         }
     }
 
     @Override
     public String name() {
         return NAME;
     }
 
     @Override
     public String description() {
         return "Speed";
     }
 
     @Override
     public String axis() {
         return "Speed";
     }
 
     @Override
     public String units() {
         switch (isKPH(false)) {
             case 1:
                 return "kph";
             case 2:
                 return "mph";
         }
         return null;
     }
 
     @Override
     public int size() {
         return _view.getStream(Log.TIME_STREAM).size();
     }
    
    private double safeAverage(double v1, double v2) {
        if (v1 != 0 && v2 != 0) {
            return (v1+v2)/2;
        } else if (v1 != 0) {
            return v1;
        } else {
            return v2;
        }
    }
 
     @Override
     public double getNumeric(int position) throws NumberFormatException {
 
         if (_lusp != null && _rusp != null && _ldsp != null && _rdsp != null) {
            // With four wheel sensors we track undriven under accel
            // and driven under braking by looking at tps 
             // at tps
            double avgDriven = safeAverage(_ldsp.getNumeric(position), _rdsp.getNumeric(position));
            double avgUndriven = safeAverage(_lusp.getNumeric(position), _rusp.getNumeric(position));
 
            if (_lowt != null && _lowt.getNumeric(position) == 1) {
                return avgDriven;
             } else {
                return avgUndriven;
             }
         } else if (_lusp != null && _rusp != null) {
             // With only 2 undriven go for max to remove lock ups
             return Math.max(_lusp.getNumeric(position), _rusp.getNumeric(position));
         } else if (_lusp != null) {
             return _lusp.getNumeric(position);
         } else if (_rusp != null) {
             return _rusp.getNumeric(position);
         } else if (_ldsp != null && _rdsp != null) {
             // With only 2 undriven go for min to remove spin
             return Math.min(_ldsp.getNumeric(position), _rdsp.getNumeric(position));
         } else if (_ldsp != null) {
             return _ldsp.getNumeric(position);
         } else if (_rdsp != null) {
             return _rdsp.getNumeric(position);
         }
         return 0;
     }
 }
