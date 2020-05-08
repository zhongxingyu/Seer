 /**
  * Copyright (c) 2007-2009, JAGaToo Project Group all rights reserved.
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
 package org.jagatoo.input.devices.components;
 
 import org.jagatoo.input.InputSystem;
 import org.jagatoo.input.InputSystemException;
 import org.jagatoo.input.actions.InvokableInputAction;
 import org.jagatoo.input.devices.InputDevice;
 import org.jagatoo.input.localization.KeyboardLocalizer;
 import org.jagatoo.logging.Log;
 
 /**
  * {@link DeviceComponent}s can be keys, buttons, axes, etc.
  * 
  * @author Marvin Froehlich (aka Qudus)
  */
 public abstract class DeviceComponent
 {
     public enum Type
     {
         KEY,
         MOUSE_AXIS,
         MOUSE_BUTTON,
         MOUSE_WHEEL,
         CONTROLLER_BUTTON,
         CONTROLLER_AXIS,
         ;
     }
     
     protected static int nextID = 1;
     
     private final int id = nextID++;
     
     private final String name;
     
     private final Type type;
     
     private InvokableInputAction[] actions = null;
     private InputDevice[] actionDevices = null;
     
     /**
      * @return this component's ID.
      */
     public final int getDeviceComponentID()
     {
         return ( id );
     }
     
     /**
      * @return this component's name.
      */
     public final String getName()
     {
         return ( name );
     }
     
     /**
      * @return this component's localized (translated) name.
      */
     public String getLocalizedName()
     {
         return ( KeyboardLocalizer.getMapping().getLocalizedKeyName( this ) );
     }
     
     /**
      * @return the DeviceComponent's Type.
      */
     public final Type getType()
     {
         return ( type );
     }
     
     /**
      * @return whether this {@link DeviceComponent} is an instance of {@link DigitalDeviceComponent}.
      */
     public final boolean isDigital()
     {
         return ( this instanceof DigitalDeviceComponent );
     }
     
     /**
      * @return whether this {@link DeviceComponent} is an instance of {@link AnalogDeviceComponent}.
      */
     public final boolean isAnalog()
     {
         return ( this instanceof AnalogDeviceComponent );
     }
     
     /**
      * Binds an InputAction to this DeviceComponent, which is executed on a
      * state change.
      * 
      * @param action the action to bind
      * @param device the InputDevice to use
      */
     public void bindAction( InvokableInputAction action, InputDevice device )
     {
         if ( actions == null )
         {
             actions = new InvokableInputAction[ 1 ];
             actionDevices = new InputDevice[ 1 ];
         }
         else
         {
             InvokableInputAction[] actions2 = new InvokableInputAction[ actions.length + 1 ];
             InputDevice[] actionDevices2 = new InputDevice[ actionDevices.length + 1 ];
             
             System.arraycopy( actions, 0, actions2, 0, actions.length );
             System.arraycopy( actionDevices, 0, actionDevices2, 0, actionDevices.length );
             
             actions = actions2;
             actionDevices = actionDevices2;
         }
         
         actions[ actions.length - 1 ] = action;
         actionDevices[ actionDevices.length - 1 ] = device;
     }
     
     /**
      * Binds an InputAction to this DeviceComponent, which is executed on a
      * state change.
      * 
      * @param action the action to bind
      */
     public void bindAction( InvokableInputAction action )
     {
         bindAction( action, null );
     }
     
     private void unbindAction( int index )
     {
         if ( ( index == 0 ) && ( actions.length == 1 ) )
         {
             actions = null;
             actionDevices = null;
             return;
         }
         
         InvokableInputAction[] actions2 = new InvokableInputAction[ actions.length - 1 ];
         InputDevice[] actionDevices2 = new InputDevice[ actionDevices.length - 1 ];
         
         if ( index > 0 )
         {
             System.arraycopy( actions, 0, actions2, 0, index );
             System.arraycopy( actionDevices, 0, actionDevices2, 0, index );
         }
         if ( index < actions.length - 1 )
         {
             System.arraycopy( actions, index + 1, actions2, index, actions.length - index - 1 );
             System.arraycopy( actionDevices, index + 1, actionDevices2, index, actionDevices.length - index - 1 );
         }
         
         actions = actions2;
         actionDevices = actionDevices2;
     }
     
     private final int findActionIndex( InvokableInputAction action, InputDevice device )
     {
         if ( actions == null )
             return ( -1 );
         
         for ( int i = 0; i < actions.length; i++ )
         {
             if ( ( actions[ i ].equals( action ) ) && ( ( device == null ) || ( device == actionDevices[ i ] ) ) )
                 return ( i );
         }
         
         return ( -1 );
     }
     
     /**
      * Unbinds an InputAction from this DeviceComponent.
      * 
      * @param action the action to bind
      * @param device the InputDevice to use
      */
     public void unbindAction( InvokableInputAction action, InputDevice device )
     {
         int index;
         while ( ( index = findActionIndex( action, device ) ) != -1 )
         {
             unbindAction( index );
         }
     }
     
     /**
      * Unbinds an InputAction from this DeviceComponent.
      * 
      * @param action the action to bind
      */
     public void unbindAction( InvokableInputAction action )
     {
         unbindAction( action, null );
     }
     
     /**
      * Notifies all bound {@link InvokableInputAction}s.
      * 
      * @param device
      * @param delta
      * @param state
      */
     public void notifyBoundActions( InputDevice device, int delta, int state, long nanoTime )
     {
         if ( actions == null )
             return;
         
         for ( int i = 0; i < actions.length; i++ )
         {
             if ( ( actionDevices[ i ] == null ) || ( actionDevices[ i ] == device ) )
             {
                 try
                 {
                     actions[ i ].invokeAction( device, this, delta, state, nanoTime );
                 }
                 catch ( InputSystemException ex )
                 {
                     Log.print( InputSystem.LOG_CHANNEL, ex );
                     ex.printStackTrace();
                 }
             }
         }
     }
     
     protected DeviceComponent( Type type, String name )
     {
         this.type = type;
         this.name = name;
     }
 }
