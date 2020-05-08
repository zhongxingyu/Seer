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
 package org.jagatoo.input.impl.lwjgl;
 
 import org.jagatoo.input.InputSystem;
 import org.jagatoo.input.InputSystemException;
 import org.jagatoo.input.devices.Mouse;
 import org.jagatoo.input.devices.MouseFactory;
 import org.jagatoo.input.devices.components.MouseButton;
 import org.jagatoo.input.devices.components.MouseButtons;
 import org.jagatoo.input.events.EventQueue;
 import org.jagatoo.input.events.MouseButtonPressedEvent;
 import org.jagatoo.input.events.MouseButtonReleasedEvent;
 import org.jagatoo.input.events.MouseMovedEvent;
 import org.jagatoo.input.events.MouseWheelEvent;
 import org.jagatoo.input.misc.InputSourceWindow;
 
 /**
  * LWJGL implementation of the Mouse class.
  * 
  * @author Marvin Froehlich (aka Qudus)
  */
 public class LWJGLMouse extends Mouse
 {
     private static final MouseButton[] buttonMap = new MouseButton[ 12 ];
     static
     {
         buttonMap[ 0 ] = MouseButtons.LEFT_BUTTON;
         buttonMap[ 1 ] = MouseButtons.RIGHT_BUTTON;
         buttonMap[ 2 ] = MouseButtons.MIDDLE_BUTTON;
         buttonMap[ 3 ] = MouseButtons.EXT_BUTTON_1;
         buttonMap[ 4 ] = MouseButtons.EXT_BUTTON_2;
         buttonMap[ 5 ] = MouseButtons.EXT_BUTTON_3;
         buttonMap[ 6 ] = MouseButtons.EXT_BUTTON_4;
         buttonMap[ 7 ] = MouseButtons.EXT_BUTTON_5;
         buttonMap[ 8 ] = MouseButtons.EXT_BUTTON_6;
         buttonMap[ 9 ] = MouseButtons.EXT_BUTTON_7;
         buttonMap[ 10 ] = MouseButtons.EXT_BUTTON_8;
         buttonMap[ 11 ] = MouseButtons.EXT_BUTTON_9;
     }
     
     public static final MouseButton convertButton( int lwjglButton )
     {
         return( buttonMap[ lwjglButton ] );
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void setPosition( int x, int y ) throws InputSystemException
     {
         super.setPosition( x, y );
         
         try
         {
             if ( isAbsolute() )
             {
                 org.lwjgl.input.Mouse.setCursorPosition( x, getSourceWindow().getHeight() - y );
             }
             else
             {
                 storePosition( x, y );
             }
         }
         catch ( Throwable t )
         {
             if ( t instanceof InputSystemException )
                 throw( (InputSystemException)t );
             
             if ( t instanceof Error )
                 throw( (Error)t );
             
             if ( t instanceof RuntimeException )
                 throw( (RuntimeException)t );
             
             throw( new InputSystemException( t ) );
         }
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void centerMouse() throws InputSystemException
     {
         int centerX = 0;
         int centerY = 0;
         try
         {
             centerX = getSourceWindow().getWidth() / 2;
             centerY = getSourceWindow().getHeight() / 2;
         }
         catch ( Throwable t )
         {
             throw( new InputSystemException( t ) );
         }
         
         setPosition( centerX, centerY );
     }
     
     protected final void collectOrFireEvents( InputSystem is, EventQueue eventQueue, long nanoTime, boolean acceptEvents ) throws InputSystemException
     {
         final boolean isQueued = ( eventQueue != null );
         
         if ( !org.lwjgl.opengl.Display.isCreated() )
             throw( new InputSystemException( "Display is not created." ) );
         
         if ( !org.lwjgl.input.Mouse.isCreated() )
             throw( new InputSystemException( "Mouse is not created." ) );
         
         try
         {
             LWJGLInputDeviceFactory.processMessages( nanoTime );
             
             org.lwjgl.input.Mouse.poll();
             
             while ( org.lwjgl.input.Mouse.next() )
             {
                 if ( !acceptEvents )
                     continue;
                 
                 final int x = org.lwjgl.input.Mouse.getEventX();
                 final int y = getSourceWindow().getHeight() - org.lwjgl.input.Mouse.getEventY();
                 
                 final int dx = org.lwjgl.input.Mouse.getEventDX();
                final int dy = -org.lwjgl.input.Mouse.getEventDY();
                 
                 final int buttonIdx = org.lwjgl.input.Mouse.getEventButton();
                 final boolean buttonState = org.lwjgl.input.Mouse.getEventButtonState();
                 
                 final int wheelDelta = org.lwjgl.input.Mouse.getEventDWheel() / 120;
                 
                 if ( isAbsolute() )
                     this.storePosition( x, y );
                 
                 if ( ( dx != 0 ) || ( dy != 0 ) )
                 {
                     final MouseMovedEvent movEv = prepareMouseMovedEvent( getCurrentX(), getCurrentY(), dx, dy, nanoTime );
                     
                     if ( dx != 0 )
                         is.notifyInputStatesManagers( this, getXAxis(), x, dx, nanoTime );
                     if ( dy != 0 )
                         is.notifyInputStatesManagers( this, getYAxis(), y, dy, nanoTime );
                     
                     if ( movEv == null )
                         continue;
                     
                     if ( isQueued )
                         eventQueue.enqueue( movEv );
                     else
                         fireOnMouseMoved( movEv, true );
                 }
                 
                 if ( buttonIdx != -1 )
                 {
                     final MouseButton button = convertButton( buttonIdx );
                     
                     if ( buttonState )
                     {
                         final MouseButtonPressedEvent pressedEv = prepareMouseButtonPressedEvent( button, nanoTime );
                         
                         is.notifyInputStatesManagers( this, button, 1, +1, nanoTime );
                         
                         if ( pressedEv == null )
                             continue;
                         
                         if ( isQueued )
                             eventQueue.enqueue( pressedEv );
                         else
                             fireOnMouseButtonPressed( pressedEv, true );
                     }
                     else
                     {
                         final MouseButtonReleasedEvent releasedEv = prepareMouseButtonReleasedEvent( button, nanoTime );
                         
                         is.notifyInputStatesManagers( this, button, 0, -1, nanoTime );
                         
                         if ( releasedEv == null )
                             continue;
                         
                         if ( isQueued )
                             eventQueue.enqueue( releasedEv );
                         else
                             fireOnMouseButtonReleased( releasedEv, true );
                     }
                 }
                 
                 if ( wheelDelta != 0 )
                 {
                     final MouseWheelEvent wheelEv = prepareMouseWheelMovedEvent( wheelDelta, false, nanoTime );
                     
                     is.notifyInputStatesManagers( this, getWheel(), getWheel().getIntValue(), wheelDelta, nanoTime );
                     
                     if ( wheelEv == null )
                         continue;
                     
                     if ( isQueued )
                         eventQueue.enqueue( wheelEv );
                     else
                         fireOnMouseWheelMoved( wheelEv, true );
                 }
             }
         }
         catch ( Throwable t )
         {
             if ( t instanceof InputSystemException )
                 throw( (InputSystemException)t );
             
             if ( t instanceof Error )
                 throw( (Error)t );
             
             if ( t instanceof RuntimeException )
                 throw( (RuntimeException)t );
             
             throw( new InputSystemException( t ) );
         }
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void consumePendingEvents( InputSystem is, EventQueue eventQueue, long nanoTime ) throws InputSystemException
     {
         collectOrFireEvents( is, null, nanoTime, false );
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void collectEvents( InputSystem is, EventQueue eventQueue, long nanoTime ) throws InputSystemException
     {
         if ( eventQueue == null )
             throw( new InputSystemException( "EventQueue must not be null here!" ) );
         
         final boolean acceptEvents = ( isEnabled() && getSourceWindow().receivesInputEvents() );
         
         collectOrFireEvents( is, eventQueue, nanoTime, acceptEvents );
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void update( InputSystem is, EventQueue eventQueue, long nanoTime ) throws InputSystemException
     {
         collectOrFireEvents( is, null, nanoTime, true );
         
         getEventQueue().dequeueAndFire( is );
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     protected void setAbsoluteImpl( boolean absolute ) throws InputSystemException
     {
         try
         {
             org.lwjgl.input.Mouse.setGrabbed( !absolute );
             
             if ( absolute )
             {
                 org.lwjgl.input.Mouse.setCursorPosition( getCurrentX(), getSourceWindow().getHeight() - getCurrentY() );
             }
         }
         catch ( Throwable t )
         {
             if ( t instanceof InputSystemException )
                 throw( (InputSystemException)t );
             
             if ( t instanceof Error )
                 throw( (Error)t );
             
             if ( t instanceof RuntimeException )
                 throw( (RuntimeException)t );
             
             throw( new InputSystemException( t ) );
         }
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void destroyImpl() throws InputSystemException
     {
         try
         {
             if ( org.lwjgl.input.Mouse.isCreated() )
             {
                 org.lwjgl.input.Mouse.destroy();
             }
         }
         catch ( Throwable t )
         {
             throw( new InputSystemException( t ) );
         }
     }
     
     private static int init_getNumButtons() throws InputSystemException
     {
         try
         {
             if ( !org.lwjgl.input.Mouse.isCreated() )
                 org.lwjgl.input.Mouse.create();
         }
         catch ( org.lwjgl.LWJGLException e )
         {
             throw( new InputSystemException( e ) );
         }
         
         return( org.lwjgl.input.Mouse.getButtonCount() );
     }
     
     private static boolean init_hasWheel() throws InputSystemException
     {
         try
         {
             if ( !org.lwjgl.input.Mouse.isCreated() )
                 org.lwjgl.input.Mouse.create();
         }
         catch ( org.lwjgl.LWJGLException e )
         {
             throw( new InputSystemException( e ) );
         }
         
         return( org.lwjgl.input.Mouse.hasWheel() );
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void onDeviceRegistered( InputSystem inputSystem ) throws InputSystemException
     {
         consumePendingEvents( inputSystem, null, -1L );
     }
     
     protected LWJGLMouse( MouseFactory factory, InputSourceWindow sourceWindow, EventQueue eventQueue ) throws InputSystemException
     {
         super( factory, sourceWindow, eventQueue, "Primary Mouse", init_getNumButtons(), init_hasWheel() );
     }
 }
