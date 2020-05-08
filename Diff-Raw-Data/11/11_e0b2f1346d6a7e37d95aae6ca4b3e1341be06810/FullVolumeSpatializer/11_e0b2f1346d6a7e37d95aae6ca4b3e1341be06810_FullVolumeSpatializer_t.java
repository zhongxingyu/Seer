 /*
  * Copyright 2007 Sun Microsystems, Inc.
  *
  * This file is part of jVoiceBridge.
  *
  * jVoiceBridge is free software: you can redistribute it and/or modify 
  * it under the terms of the GNU General Public License version 2 as 
  * published by the Free Software Foundation and distributed hereunder 
  * to you.
  *
  * jVoiceBridge is distributed in the hope that it will be useful, 
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied this 
  * code. 
  */
 
 package com.sun.mpk20.voicelib.app;
 
 import java.io.Serializable;
 
 import java.util.logging.Logger;
 
 public class FullVolumeSpatializer implements Spatializer, Serializable {
 
     private static final long serialVersionUID = 1;
 
     private double fullVolumeRadius = 0;
 
     private double attenuator = DefaultSpatializer.DEFAULT_MAXIMUM_VOLUME;
 
     public FullVolumeSpatializer() {
 	this(0);
     }
 
     public FullVolumeSpatializer(double fullVolumeRadius) {
 	this.fullVolumeRadius = fullVolumeRadius;
     }
 
     public double[] spatialize(double sourceX, double sourceY, 
                                double sourceZ, double sourceOrientation, 
                                double destX, double destY, 
                                double destZ, double destOrientation) {
 
 	double[] parameters = new double[4];
 
 	parameters[0] = 1;	 	 // front/back
 	parameters[1] = 0;  		 // left/right
 	parameters[2] = 0;  		 // up/down
 	parameters[3] = 1 * attenuator;  // volume
 
	double distance = Util.getDistance(sourceX, sourceY,
	    sourceZ, destX, destY, destZ);
 
	if (distance > fullVolumeRadius) {
	    parameters[3] = 0;
 	}
 
 	return parameters; 
     }
 
     public void setAttenuator(double attenuator) {
 	this.attenuator = attenuator;
     }
 
     public double getAttenuator() {
 	return attenuator;
     }
 
     public Object clone() {
 	FullVolumeSpatializer f = new FullVolumeSpatializer();
 
 	f.fullVolumeRadius = fullVolumeRadius;
 	f.attenuator = attenuator;
 	return f;
     }
 
     public String toString() {
 	return "FullVolumeSpatializer(fvr=" + fullVolumeRadius
 	    + " attenuator=" + attenuator + ")";
     }
 
 }
