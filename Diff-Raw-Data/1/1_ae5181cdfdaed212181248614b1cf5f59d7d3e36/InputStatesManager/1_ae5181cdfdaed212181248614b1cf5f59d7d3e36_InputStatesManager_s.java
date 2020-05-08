 /**
  * Copyright (c) 2007-2008, JAGaToo Project Group all rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * 
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * 
  * Neither the name of the 'Xith3D Project Group' nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
  * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE
  */
 package org.jagatoo.input.managers;
 
 import org.jagatoo.input.InputSystem;
 import org.jagatoo.input.InputSystemException;
 import org.jagatoo.input.actions.InputAction;
 import org.jagatoo.input.actions.InvokableInputAction;
 import org.jagatoo.input.devices.InputDevice;
 import org.jagatoo.input.devices.Mouse;
 import org.jagatoo.input.devices.components.DeviceComponent;
 import org.jagatoo.input.devices.components.InputState;
 import org.jagatoo.input.devices.components.MouseWheel;
 import org.jagatoo.logging.Log;
 
 /**
  * Manages state-changes on any kind of {@link InputDevice}.
  * 
  * @author Marvin Froehlich (aka Qudus)
  */
 public class InputStatesManager
 {
     private final InputBindingsManager< ? extends InputAction > bindingsManager;
     
     private InputStatesManipulator manipulator = null;
     
     private final int numStates;
     
     private final short[] states1;
     private final short[] states2;
     
     private short[] prevStates;
     private short[] currStates;
     private final short[] tmpStates;
     
     private boolean swapper = false;
     
     /**
      * @return the {@link InputStatesManipulator} for this manager.
      */
     public InputStatesManipulator getKeyStatesManipulator()
     {
         if ( manipulator == null )
         {
             manipulator = new InputStatesManipulator( this );
         }
         
         return( manipulator );
     }
     
     /**
      * @return the number of key-states (or commands).
      */
     public final int getNumStates()
     {
         return( numStates );
     }
     
     /**
      * @param action
      * 
      * @return the current key-state for the given action.
      */
     public final InputState getInputState( InputAction action )
     {
         final int ordinal = action.ordinal();
         
         if ( states1[ ordinal ] == states2[ ordinal ] )
         {
             if ( states1[ ordinal ] > 0 )
                 return( InputState.POSITIVE );
             else
                 return( InputState.NEGATIVE );
         }
         else if ( swapper )
         {
             if ( states1[ ordinal ] > states2[ ordinal ] )
                 return( InputState.MADE_POSITIVE );
             else
                 return( InputState.MADE_NEGATIVE );
         }
         else
         {
             if ( states2[ ordinal ] > states1[ ordinal ] )
                 return( InputState.MADE_POSITIVE );
             else
                 return( InputState.MADE_NEGATIVE );
         }
     }
     
     /**
      * @param action
      * 
      * @return the current key-state for the given action.
      */
     public final int getSimpleInputState( InputAction action )
     {
         return( currStates[ action.ordinal() ] );
     }
     
     /**
      * @param action
      * 
      * @return whether the state for the given action's state has changed.
      */
     public final boolean hasInputStateChanged( InputAction action )
     {
         return( states1[ action.ordinal() ] != states2[ action.ordinal() ] );
     }
     
     private final void invokeAction( final InputDevice device, final DeviceComponent comp, final int ordinal, final InputAction action, long nanoTime )
     {
         if ( action instanceof InvokableInputAction )
         {
             final InvokableInputAction invAction = (InvokableInputAction)action;
             
             try
             {
                 invAction.invokeAction( device, comp, ( tmpStates[ ordinal ] - prevStates[ ordinal ] ), tmpStates[ ordinal ], nanoTime );
             }
             catch ( InputSystemException ex )
             {
                 Log.print( InputSystem.LOG_CHANNEL, ex );
                 ex.printStackTrace();
             }
         }
     }
     
     private final int updateState_( final InputDevice device, final DeviceComponent comp, final int state, long nanoTime )
     {
         final InputAction action = bindingsManager.getBoundAction( comp );
         
         if ( action == null )
             return( -1 );
         
         final int ordinal = action.ordinal();
         
         tmpStates[ ordinal ] = (short)state;
         
         if ( tmpStates[ ordinal ] != prevStates[ ordinal ] )
         {
             invokeAction( device, comp, ordinal, action, nanoTime );
         }
         
         return( ordinal );
     }
     
     private final void updateWheelStates( final Mouse mouse, final MouseWheel wheel, final int state, final int delta, long nanoTime )
     {
         if ( wheel != MouseWheel.GLOBAL_WHEEL )
             updateWheelStates( mouse, MouseWheel.GLOBAL_WHEEL, state, delta, nanoTime );
         
         updateState_( mouse, wheel, state, nanoTime );
         
         final int result;
         if ( delta > 0 )
             result = updateState_( mouse, wheel.getUp(), 1, nanoTime );
         else
             result = updateState_( mouse, wheel.getDown(), 1, nanoTime );
         
         if ( result >= 0 )
         {
             currStates[ result ] = 0;
         }
     }
     
     public final void internalUpdateState( final InputDevice device, final DeviceComponent comp, final int state, final int delta, long nanoTime )
     {
         if ( comp.getType() == DeviceComponent.Type.MOUSE_WHEEL )
         {
             updateWheelStates( (Mouse)device, (MouseWheel)comp, state, delta, nanoTime );
         }
         else
         {
             updateState_( device, comp, state, nanoTime );
         }
     }
     
     /**
      * Updates the key-states array.
      * 
      * @param nanoTime
      */
     public void update( long nanoTime )
     {
         swapper = !swapper;
         
         if ( swapper )
         {
             currStates = states1;
             prevStates = states2;
         }
         else
         {
             currStates = states2;
             prevStates = states1;
         }
         
         System.arraycopy( tmpStates, 0, currStates, 0, numStates );
         
         //System.out.println( prevStates[ 0 ] + ", " + currStates[ 0 ] );
         
         if ( manipulator != null )
         {
             manipulator.apply( currStates );
         }
     }
     
     /**
      * Creates a new {@link InputStatesManager}.
      * 
      * @param numActions the number of available (bindeable) actions.
      */
     public InputStatesManager( InputBindingsManager< ? extends InputAction > bindingsManager )
     {
         this.bindingsManager = bindingsManager;
         
         final int numActions = bindingsManager.getNumActions();
         
         this.numStates = numActions;
         
         this.states1 = new short[ numActions ];
         this.states2 = new short[ numActions ];
         this.tmpStates = new short[ numActions ];
         
         for ( int i = 0; i < numActions; i++ )
         {
             states1[ i ] = 0;
             states2[ i ] = 0;
             tmpStates[ i ] = 0;
         }
         
         currStates = states2;
         
         swapper = false;
     }
 }
