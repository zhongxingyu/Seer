 /*
  * Copyright (c) 2010, Soar Technology, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * * Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  *
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  *
  * * Neither the name of Soar Technology, Inc. nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without the specific prior written permission of Soar Technology, Inc.
  *
  * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.soartech.simjr.controllers;
 
 import com.soartech.math.Angles;
 import com.soartech.math.Vector3;
 import com.soartech.simjr.sim.AbstractEntityCapability;
 import com.soartech.simjr.sim.Entity;
 import com.soartech.simjr.sim.EntityTools;
 
 /**
  * A controller that provides an interface for controlling a fixed-wing-
  * aircraft such as an F-18.  Provides methods for setting desired speed,
  * bearing, heading, and altitude.  This controller may be added directly
  * to a vehicle or may be nested in another controller such as a Soar agent.
  *
  * @author piegdon
  */
 public class FixedWingFlightController extends AbstractEntityCapability implements FlightController
 {
     public static final String PROPERTY_USE_FULL_ORIENTATION = "use-full-orientation";
     public static final String PROPERTY_DESIRED_VELOCITY = "desired-velocity";
     public static final String PROPERTY_DESIRED_TURN_RATE = "desired-turn-rate";
     public static final String PROPERTY_DESIRED_ALTITUDE = "desired-altitude";
     public static final String PROPERTY_DESIRED_FPA = "desired-fpa";
     public static final String PROPERTY_DESIRED_HEADING = "desired-heading";
     public static final String PROPERTY_DESIRED_SPEED = "desired-speed";
     
     /**
      * Desired ground speed in m/s
      */
     private double desiredSpeed = 0.0;
     /**
      * Desired heading in nav radians (0 north)
      */
     private double desiredHeading = 0.0;
     /**
      * Desired altitude in meters
      */
     private double desiredAltitude = 0.0;
     /**
      * Desired flight path angle in radians
      */
     private double desiredFpa = Math.toRadians(20.0);
     /**
      * Desired turning rate in radians per second
      */
     private double desiredTurnRate = Math.toRadians(15.0);
 
     public FixedWingFlightController()
     {
     }
 
     /**
      * @param speed The new desired speed in m/s.
      */
     public void setDesiredSpeed(double speed)
     {
         this.desiredSpeed = speed;
         if(getEntity() != null)
         {
             getEntity().setProperty(PROPERTY_DESIRED_SPEED, desiredSpeed);
         }
     }
 
     /**
      * @param desiredBearing The new desired bearing in radians
      */
     public void setDesiredHeading(double desiredHeading)
     {
         this.desiredHeading = desiredHeading;
         if(getEntity() != null)
         {
             getEntity().setProperty(PROPERTY_DESIRED_HEADING, Math.toDegrees(desiredHeading));
         }
     }
 
     public void setDesiredFpa(Double desiredFpa)
     {
         this.desiredFpa = desiredFpa;
         if(getEntity() != null)
         {
             getEntity().setProperty(PROPERTY_DESIRED_FPA, Math.toDegrees(desiredFpa));
         }
     }
 
     public void setDesiredAltitude(double altitude)
     {
         this.desiredAltitude = altitude;
         if(getEntity() != null)
         {
             getEntity().setProperty(PROPERTY_DESIRED_ALTITUDE, desiredAltitude);
         }
     }
 
     public void setDesiredTurnRate(Double turnRate)
     {
         this.desiredTurnRate = turnRate;
         if(getEntity() != null)
         {
             getEntity().setProperty(PROPERTY_DESIRED_TURN_RATE, Math.toDegrees(desiredTurnRate));
         }
     }
 
     public double getDesiredHeading() { return desiredHeading; }
     public double getDesiredAltitude() { return desiredAltitude; }
     public double getDesiredSpeed() { return desiredSpeed; }
 
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.sim.AbstractEntityCapability#attach(com.soartech.simjr.sim.Entity)
      */
     @Override
     public void attach(Entity entity)
     {
         super.attach(entity);
         getEntity().setProperty(PROPERTY_DESIRED_SPEED, desiredSpeed);
         getEntity().setProperty(PROPERTY_DESIRED_HEADING, Math.toDegrees(desiredHeading));
         getEntity().setProperty(PROPERTY_DESIRED_FPA, Math.toDegrees(desiredFpa));
         getEntity().setProperty(PROPERTY_DESIRED_ALTITUDE, desiredAltitude);
         getEntity().setProperty(PROPERTY_DESIRED_TURN_RATE, Math.toDegrees(desiredTurnRate));
         getEntity().setProperty(PROPERTY_USE_FULL_ORIENTATION, true);
     }
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.sim.AbstractEntityCapability#detach()
      */
     @Override
     public void detach()
     {
         getEntity().setProperty(PROPERTY_DESIRED_SPEED, null);
         getEntity().setProperty(PROPERTY_DESIRED_HEADING, null);
         getEntity().setProperty(PROPERTY_DESIRED_FPA, null);
         getEntity().setProperty(PROPERTY_DESIRED_ALTITUDE, null);
         getEntity().setProperty(PROPERTY_DESIRED_TURN_RATE, null);
         getEntity().setProperty(PROPERTY_USE_FULL_ORIENTATION, null);
         super.detach();
     }
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.sim.EntityController#openDebugger()
      */
     public void openDebugger()
     {
     }
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.sim.Tickable#tick(double)
      */
     @Override
     public void tick(double dt)
     {
         // Convert desired speed, bearing and altitude into a desired velocity
 
         // Note that desiredHeading is CW with 0 north, orientation is CCW with 0 east
 
         // Get X/Y part of desired velocity - sin/cos switched to account for 0 North.
         double x = Math.sin(desiredHeading);
         double y = Math.cos(desiredHeading);
         double currentOrientation = getEntity().getHeading();
         double desiredOrientation = Angles.navRadiansToMathRadians(desiredHeading);
         Vector3 desiredVelocity = new Vector3(x, y, 0);
 
         // Scale by speed to get the right ground speed
         desiredVelocity = desiredVelocity.multiply(desiredSpeed);
 
         // Calculate desired Z velocity
         double currentAltitude = EntityTools.getAltitude(getEntity());
         double altitudeError = desiredAltitude - currentAltitude;
        double desiredVelocityZ = altitudeError * dt;
 
         // Decide how fast we're allowed to change altitude
         double desiredAltitudeRate = Math.abs(desiredVelocity.length() * Math.sin(desiredFpa));
 
         // Clamp Z velocity to desired altitude rate
         if(desiredVelocityZ > desiredAltitudeRate)
         {
             desiredVelocityZ = desiredAltitudeRate;
         }
         else if(desiredVelocityZ < -desiredAltitudeRate)
         {
             desiredVelocityZ = -desiredAltitudeRate;
         }
         
         // Setting the pitch based simply on the velocity vector
         boolean useFullOrientation = true;
         Object ufoObj = getEntity().getProperty(PROPERTY_USE_FULL_ORIENTATION);
         if ( ufoObj != null && ufoObj instanceof Boolean ) {
             useFullOrientation = ((Boolean) ufoObj).booleanValue();
         }
         
         if ( useFullOrientation ) {
             getEntity().setPitch( Math.atan2(desiredVelocityZ, desiredSpeed) );
         }
 
         // Create final desired velocity
         desiredVelocity = new Vector3(desiredVelocity.x, desiredVelocity.y, desiredVelocityZ);
         if(desiredVelocity.epsilonEquals(Vector3.ZERO))
         {
             desiredVelocity = Vector3.ZERO;
         }
 
         // Store in properties so it's displayed in UI.
         getEntity().setProperty(PROPERTY_DESIRED_VELOCITY, desiredVelocity);
 
         double angleDiff = Angles.angleDifference(desiredOrientation, currentOrientation);
         double maxDeltaAngle = desiredTurnRate * dt;
         double desiredDeltaAngle = Math.min(Math.abs(angleDiff), maxDeltaAngle) * Math.signum(angleDiff);
         double newOrientation = currentOrientation + desiredDeltaAngle;
         getEntity().setHeading(newOrientation);
         
         if ( useFullOrientation ) {
             getEntity().setRoll(-Math.PI/2. * desiredDeltaAngle/maxDeltaAngle);
         }
 
         Vector3 newVelocity = new Vector3(Math.cos(newOrientation), Math.sin(newOrientation), 0.0);
         newVelocity = newVelocity.multiply(desiredSpeed);
         newVelocity = new Vector3(newVelocity.x, newVelocity.y, desiredVelocityZ);
 
         if(newVelocity.epsilonEquals(Vector3.ZERO))
         {
             newVelocity = Vector3.ZERO;
         }
         getEntity().setVelocity(newVelocity);
     }
 
 }
