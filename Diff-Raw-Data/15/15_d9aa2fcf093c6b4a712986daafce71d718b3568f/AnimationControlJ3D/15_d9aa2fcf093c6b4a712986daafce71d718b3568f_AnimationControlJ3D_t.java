 //
 // AnimationControlJ3D.java
 //
 
 /*
 VisAD system for interactive analysis and visualization of numerical
 data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
 Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
 Tommy Jasmin.
  
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Library General Public
 License as published by the Free Software Foundation; either
 version 2 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Library General Public License for more details.
 
 You should have received a copy of the GNU Library General Public
 License along with this library; if not, write to the Free
 Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 MA 02111-1307, USA
 */
 
 package visad.java3d;
  
 import visad.*;
 
 import java.rmi.*;
 
 /**
    AnimationControlJ3D is the VisAD class for controlling Animation
    display scalars under Java3D.<P>
 
    WLH - manipulate a list of Switch nodes in scene graph.<P>
 */
 public class AnimationControlJ3D extends AVControlJ3D
        implements Runnable, AnimationControl {
 
  private int current = 0;
   private boolean direction; // true = forward
   private long step; // time in milleseconds between animation steps
  private long[] stepValues = {500}; // times in milleseconds between animation steps
   private AnimationSetControl animationSet;
   private ToggleControl animate;
   private RealType real;
 
   /** AnimationControlJ3D is Serializable, mark as transient */
   private transient Thread animationThread;
 
   public AnimationControlJ3D(DisplayImplJ3D d, RealType r) {
     super(d);
     real = r;
     current = 0;
     direction = true;
     step = 500;
     stepValues = new long[] {step};
     animationSet = new AnimationSetControl(d, this);
     // initialize the stepValues array
     try
     {
         Set set = animationSet.getSet();
         if (set != null) stepValues = new long[set.getLength()];
     }
     catch (VisADException v) {;} 
     for (int i = 0; i<stepValues.length; i++)
     {
         stepValues[i] = step;
     }
     d.addControl(animationSet);
     animate = new ToggleControl(d, this);
     d.addControl(animate);
     if (d != null) {
       animationThread = new Thread(this);
       animationThread.start();
     }
   }
 
   AnimationControlJ3D() {
     this(null, null);
   }
 
   public void stop() {
     animationThread = null;
   }
  
   public void run() {
     Thread me = Thread.currentThread();
     while (animationThread == me) {
       try {
         if (animate != null && animate.getOn()) {
           takeStep();
         }
       }
       catch (VisADException v) {
         v.printStackTrace();
         throw new VisADError("AnimationControlJ3D.run: " + v.toString());
       }
       catch (RemoteException v) {
         v.printStackTrace();
         throw new VisADError("AnimationControlJ3D.run: " + v.toString());
       }
       try {
         synchronized (this) {
           wait(stepValues[current]);
         }
       }
       catch(InterruptedException e) {
         // control doesn't normally come here
       }
     }
   }
 
   public int getCurrent() {
     return current;
   }
 
   /** set the current ordinal step number = c */
   public void setCurrent(int c)
          throws VisADException, RemoteException {
     if (animationSet != null) {
       current = animationSet.clipCurrent(c);
       init();
     }
     else {
       current = 0;
     }
     changeControl(true);
   }
  
   /** set the current step by the value of the RealType
       mapped to Display.Animation */
   public void setCurrent(double value)
          throws VisADException, RemoteException {
     if (animationSet != null) {
       current = animationSet.getIndex(value);
       init();
     }
     else {
       current = 0;
     }
     changeControl(true);
   }
 
   /** 
    * Set the animation direction.
    *
    * @param    dir     true for forward, false for backward
    *
    * @throws  VisADException   Couldn't create necessary VisAD object.  The
    *                           direction remains unchanged.
    * @throws  RemoteException  Java RMI exception
    */
   public void setDirection(boolean dir)
          throws VisADException, RemoteException {
     direction = dir;
     changeControl(true);
   }
 
   /** Get the animation direction.
    *
    *  @return	true for forward, false for backward
    */
   public boolean getDirection()
   {
     return direction;
   }
 
   /**
    * Return the dwell time for the current step
    */
   public long getStep() {
     return stepValues[current];
   }
 
   /**
    * return an array of the dwell times for all the steps.
    */
   public long[] getSteps()
   {
       return stepValues;
   }
 
   /** 
    * set the dwell time for all steps
    *
    * @param  st   dwell time in milliseconds
    *
    * @throws  VisADException   Couldn't create necessary VisAD object.  The
    *                           dwell time remains unchanged.
    * @throws  RemoteException  Java RMI exception
    */
   public void setStep(int st) throws VisADException, RemoteException {
     if (st <= 0) {
       throw new DisplayException("AnimationControlJ3D.setStep: " +
                                  "step must be > 0");
     }
     step = st;
     for (int i=0; i < stepValues.length; i++)
     {
         stepValues[i] = st;
     }
     changeControl(true);
   }
 
   /** 
    * set the dwell time for individual steps.
    *
    * @param   steps   an array of dwell rates for each step in the animation
    *                  If the length of the array is less than the number of 
    *                  frames in the animation, the subsequent step values will 
    *                  be set to the value of the last step.
    *
    * @throws  VisADException   Couldn't create necessary VisAD object.  The
    *                           dwell times remain unchanged.
    * @throws  RemoteException  Java RMI exception
    */
   public void setSteps(int[] steps) 
   throws VisADException, RemoteException
   {
     // verify that the values are valid
     for (int i = 0; i < stepValues.length; i++)
     {
         stepValues[i] = 
 	    (i < steps.length) ? steps[i] : steps[steps.length-1];
         if (stepValues[i] <= 0) 
             throw new DisplayException("AnimationControlJ3D.setSteps: " +
                                  "step " + i + " must be > 0");
     }
     changeControl(true);
   }
 
   /** 
    * advance one step (forward or backward) 
    *
    * @throws  VisADException   Couldn't create necessary VisAD object.  No
    *                           step is taken.
    * @throws  RemoteException  Java RMI exception
    */
   public void takeStep() throws VisADException, RemoteException {
     if (direction) current++;
     else current--;
     if (animationSet != null) {
       current = animationSet.clipCurrent(current);
       init();
     }
     changeControl(true);
   }
 
   public void init() throws VisADException {
     if (animationSet != null &&
         animationSet.getSet() != null) {
       double value = animationSet.getValue(current);
       Set set = animationSet.getSet();
 
       animation_string(real, set, value, current);
       selectSwitches(value, set);
     }
   }
 
   public Set getSet() {
     if (animationSet != null) {
       return animationSet.getSet();
     }
     else {
       return null;
     }
   }
 
   public void setSet(Set s)
          throws VisADException, RemoteException {
     setSet(s, false);
     if (s.getLength() != stepValues.length)
     {
         stepValues = new long[s.getLength()];
         for (int i = 0; i < stepValues.length; i++)
         {
             stepValues[i] = step;
         }
     }
   }
  
   /** changeControl(!noChange) to not trigger re-transform,
       used by ScalarMap.setRange */
   public void setSet(Set s, boolean noChange)
          throws VisADException, RemoteException {
     if (animationSet != null) {
       animationSet.setSet(s, noChange);
       if (s.getLength() != stepValues.length)
       {
           stepValues = new long[s.getLength()];
           for (int i = 0; i < stepValues.length; i++)
           {
               stepValues[i] = step;
           }
       }
     }
   }
 
   /** return true if automatic stepping is on */
   public boolean getOn() {
     if (animate != null) {
       return animate.getOn();
     }
     else {
       return false;
     }
   }
 
   /** 
    * Set automatic stepping on or off.
    *
    * @param  o  true = turn stepping on, false = turn stepping off
    *
    * @throws  VisADException   Couldn't create necessary VisAD object.  No
    *                           change in automatic stepping occurs.
    * @throws  RemoteException  Java RMI exception
    */
   public void setOn(boolean o)
          throws VisADException, RemoteException {
     if (animate != null) {
       animate.setOn(o);
     }
   }
 
   /** 
    * toggle automatic stepping between off and on 
    *
    * @throws  VisADException   Couldn't create necessary VisAD object.  No
    *                           change in automatic stepping occurs.
    * @throws  RemoteException  Java RMI exception
    */
   public void toggle()
          throws VisADException, RemoteException {
     if (animate != null) {
       animate.setOn(!animate.getOn());
     }
   }
 
   public void subSetTicks() {
     if (animationSet != null) {
       animationSet.setTicks();
     }
     if (animate != null) {
       animate.setTicks();
     }
   }
 
   public boolean subCheckTicks(DataRenderer r, DataDisplayLink link) {
     boolean flag = false;
     if (animationSet != null) {
       flag |= animationSet.checkTicks(r, link);
     }
     if (animate != null) {
       flag |= animate.checkTicks(r, link);
     }
     return flag;
   }
 
   public boolean subPeekTicks(DataRenderer r, DataDisplayLink link) {
     boolean flag = false;
     if (animationSet != null) {
       flag |= animationSet.peekTicks(r, link);
     }
     if (animate != null) {
       flag |= animate.peekTicks(r, link);
     }
     return flag;
   }
 
   public void subResetTicks() {
     if (animationSet != null) {
       animationSet.resetTicks();
     }
     if (animate != null) {
       animate.resetTicks();
     }
   }
 
 }
 
