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
  * A controller that provides an interface for controlling a fixed-wing 
  * aircraft such as an F-18.  Provides methods for setting desired speed,
  * bearing, heading, and altitude. The rate of change of the entity's heading
  * and altitude are limited by the desiredTurnRate and flight path angle 
  * (or altitude rate). This controller may be added directly
  * to a vehicle or may be nested in another controller such as a Soar agent.
  *
  * @author piegdon
  */
 public class FixedWingFlightController extends AbstractEntityCapability implements FlightController
 {
     /**
      * The names of various properties used by this controller.
      */
     public static final String PROPERTY_USE_FULL_ORIENTATION = "use-full-orientation";
     public static final String PROPERTY_DESIRED_VELOCITY = "desired-velocity";
     public static final String PROPERTY_DESIRED_TURN_RATE = "desired-turn-rate";
     public static final String PROPERTY_DESIRED_ALTITUDE = "desired-altitude";
     public static final String PROPERTY_DESIRED_FPA = "desired-fpa";
     public static final String PROPERTY_DESIRED_ALTITUDE_RATE = "desired-altitude-rate";
     public static final String PROPERTY_USE_DESIRED_FPA = "use-desired-fpa";
     public static final String PROPERTY_DESIRED_HEADING = "desired-heading";
     public static final String PROPERTY_DESIRED_SPEED = "desired-speed";
    public static final String PROPERTY_DESIRED_TURN_DIR = "desired-turn-dir";
     
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
      * Desired altitude rate in meters/second
      * This class should ensure that FPA overrides altitude rate if useDesiredFpa
      * is true.
      */
     private double desiredAltitudeRate = 0.0;  // Okay because default useDesiredFpa is true
     
     /**
      * Are we using desiredFpa or desiredAltitudeRate to adjust altitude?
      */
     private boolean useDesiredFpa = true;
     
     /**
      * Maximum turning rate in radians per second
      */
     private double desiredTurnRate = Math.toRadians(15.0);
 
     /**
      * Desired turn direction to achieve the current desired heading.  Must be "left" or "right".
      * If any other value, the flight controller will choose the shortest direction to achieve the turn.
      */
     private String desiredTurnDir = null;
 
     /**
      * Basic constructor, does nothing but initialize member variables to default values.
      */
     public FixedWingFlightController()
     {
     }
 
     /**
      * Sets the target ground speed of the controlled entity. Currently acceleration is instantaneous 
      * so the ground speed of the entity should be this value after the next tick.
      * 
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
      * Sets the target heading of the controlled entity. The rate of change of the heading 
      * is limited by the desiredTurnRate.
      * 
      * @param desiredHeading The new desired bearing in radians
      */
     public void setDesiredHeading(double desiredHeading)
     {
         this.desiredHeading = desiredHeading;
         if(getEntity() != null)
         {
             getEntity().setProperty(PROPERTY_DESIRED_HEADING, Math.toDegrees(desiredHeading));
         }
     }
 
     /**
      * Sets the maximum flight path angle the entity will use to achieve the 
      * desired target altitude. If set this will override any previous desired
      * altitude rate settings.
      * 
      * @param desiredFpa The flight path angle in radians
      */
     public void setDesiredFpa(Double desiredFpa)
     {
         this.desiredFpa = Math.abs(desiredFpa);
         this.useDesiredFpa = true;
         if(getEntity() != null)
         {
             getEntity().setProperty(PROPERTY_DESIRED_FPA, Math.toDegrees(desiredFpa));
             getEntity().setProperty(PROPERTY_USE_DESIRED_FPA, useDesiredFpa);
         }
     }
 
     /**
      * Sets the maximum vertical velocity the entity will use to achieve the
      * desired target altitude. If set this will override any previous flight path
      * angle settings.
      * 
      * @param rate The maximum vertical velocity in meters/sec
      */
     public void setDesiredAltitudeRate(double rate)
     {
         this.desiredAltitudeRate = Math.abs(rate);
         this.useDesiredFpa = false;
         if(getEntity() != null)
         {
             getEntity().setProperty(PROPERTY_DESIRED_ALTITUDE_RATE, desiredAltitudeRate);
             getEntity().setProperty(PROPERTY_USE_DESIRED_FPA, useDesiredFpa);
         }
     }
 
     /**
      * Sets the target altitude of the entity. The number of ticks it will take to reach
      * this altitude is determined by the flight path angle or the altitude rate (and the
      * entity's current deviation from the target).
      * 
      * @param altitude The target altitude for the entity in meters above ground level
      */
     public void setDesiredAltitude(double altitude)
     {
         this.desiredAltitude = altitude;
         if(getEntity() != null)
         {
             getEntity().setProperty(PROPERTY_DESIRED_ALTITUDE, desiredAltitude);
         }
     }
 
     /**
      * Sets the maximum turn rate the entity will use to reach the desired target heading.
      * 
      * @param turnRate in radians/second
      */
     public void setDesiredTurnRate(Double turnRate)
     {
         this.desiredTurnRate = turnRate;
         if(getEntity() != null)
         {
             getEntity().setProperty(PROPERTY_DESIRED_TURN_RATE, Math.toDegrees(desiredTurnRate));
         }
     }
 
     /**
      * Sets the desired turn direction to reach the desired target heading.  If not "left" or "right",
      * the flight controller will pick the shortest turn.
      * 
      * @param turnDir the String "left", "right", or anything else for no specified turn direction
      */
     public void setDesiredTurnDir(String turnDir)
     {
         this.desiredTurnDir = turnDir;
         if(getEntity() != null)
         {
             getEntity().setProperty(PROPERTY_DESIRED_TURN_DIR, desiredTurnDir);
         }
         
     }
 
     /**
      * Simple accessor for the desired heading setting.
      * 
      * @return the desired target heading in radians CW from due north
      */
     public double getDesiredHeading() { return desiredHeading; }
     
     /**
      * Simple accessor for the desired altitude setting.
      * 
      * @return the desired target heading in meters above ground level
      */
     public double getDesiredAltitude() { return desiredAltitude; }
     
     /**
      * Simple accessor for the desired ground speed.
      * 
      * @return the desired target ground speed in meters/sec
      */
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
         getEntity().setProperty(PROPERTY_USE_DESIRED_FPA, useDesiredFpa);
         getEntity().setProperty(PROPERTY_DESIRED_FPA, Math.toDegrees(desiredFpa));
         getEntity().setProperty(PROPERTY_DESIRED_ALTITUDE_RATE, desiredAltitudeRate);
         getEntity().setProperty(PROPERTY_DESIRED_ALTITUDE, desiredAltitude);
         getEntity().setProperty(PROPERTY_DESIRED_TURN_RATE, Math.toDegrees(desiredTurnRate));
         getEntity().setProperty(PROPERTY_DESIRED_TURN_DIR, desiredTurnDir);
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
         getEntity().setProperty(PROPERTY_USE_DESIRED_FPA, null);
         getEntity().setProperty(PROPERTY_DESIRED_FPA, null);
         getEntity().setProperty(PROPERTY_DESIRED_ALTITUDE_RATE, null);
         getEntity().setProperty(PROPERTY_DESIRED_ALTITUDE, null);
         getEntity().setProperty(PROPERTY_DESIRED_TURN_RATE, null);
         getEntity().setProperty(PROPERTY_DESIRED_TURN_DIR, null);
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
         double desiredVelocityZ = altitudeError/dt;
 
         // Decide how fast we're allowed to change altitude
         if (useDesiredFpa) {
             desiredAltitudeRate = Math.abs(desiredVelocity.length() * Math.sin(desiredFpa));
         }
 
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
 
         double angleDiff = Angles.angleDifference(desiredOrientation, currentOrientation, desiredTurnDir);
         double maxDeltaAngle = desiredTurnRate * dt;
         if (Math.abs(angleDiff) < maxDeltaAngle) {
             maxDeltaAngle = Math.abs(angleDiff);
             setDesiredTurnDir(null);
         }
         double desiredDeltaAngle = maxDeltaAngle * Math.signum(angleDiff);
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
