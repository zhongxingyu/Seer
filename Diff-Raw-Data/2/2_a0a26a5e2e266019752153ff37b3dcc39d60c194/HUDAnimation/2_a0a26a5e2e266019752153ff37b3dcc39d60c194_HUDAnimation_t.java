 /*
  * Project Wonderland
  * 
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  * 
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  * 
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  * 
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied
  * this code.
  */
 package org.jdesktop.wonderland.client.hud;
 
 /**
  * A HUDAnimation applies an animation to a HUDComponent.
  * Examples of animations include animating the motion of a HUDComponent
  * from on position to another on the HUD.
  *
  * @author nsimpson
  */
 public interface HUDAnimation {
 
     /**
      * Sets the object to animate
      * @param component the HUDComponent to animate
      */
     public void setAnimated(HUDComponent component);
 
     /**
      * Gets the animated object
      * @return the object to be animated
      */
     public HUDComponent getAnimated();
 
     /**
      * Trigger an animation
      */
     public void startAnimation();
 
     /**
      * Stops an in progress animation
      */
     public void stopAnimation();
 
     /**
      * Gets whether the animation is in progress
     * @return true if the animation is in progress, false if not
      */
     public boolean isAnimating();
 
     /**
      * Sets the duration of the animation
      * @param duration the duration of the animation in milliseconds
      */
     public void setDuration(long duration);
 
     /**
      * Gets the duration of the animation
      * @return the duration of the animation in milliseconds
      */
     public long getDuration();
 }
