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
 package org.jagatoo.input;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import org.jagatoo.input.devices.Controller;
 import org.jagatoo.input.devices.ControllerFactory;
 import org.jagatoo.input.devices.InputDeviceFactory;
 import org.jagatoo.input.devices.Keyboard;
 import org.jagatoo.input.devices.KeyboardFactory;
 import org.jagatoo.input.devices.Mouse;
 import org.jagatoo.input.devices.MouseFactory;
 import org.jagatoo.input.events.EventQueue;
 import org.jagatoo.input.handlers.InputHandler;
 import org.jagatoo.input.listeners.InputListener;
 import org.jagatoo.input.listeners.InputStateListener;
 import org.jagatoo.input.misc.InputSourceWindow;
 
 public class InputSystem
 {
     private static InputSystem instance = null;
     
     private Keyboard[] keyboards = new Keyboard[ 0 ];
     private Mouse[] mouses = new Mouse[ 0 ];
     private Controller[] controllers = new Controller[ 0 ];
     
     private Keyboard[] keyboards_out = null;
     private Mouse[] mouses_out = null;
     private Controller[] controllers_out = null;
     
     private final EventQueue eventQueue;
     
     private final ArrayList< InputListener > inputListeners = new ArrayList< InputListener >();
     private final ArrayList< InputStateListener > inputStateListeners = new ArrayList< InputStateListener >();
     
     private final ArrayList< InputHandler< ? > > inputHandlers = new ArrayList< InputHandler< ? > >();
     
     public static final void setInstance( InputSystem inputSystem )
     {
         instance = inputSystem;
     }
     
     public static final boolean hasInstance()
     {
         return( instance != null );
     }
     
     public static final InputSystem getInstance()
     {
         if ( instance == null )
         {
             instance = new InputSystem();
         }
         
         return( instance );
     }
     
     public final EventQueue getEventQueue()
     {
         return( eventQueue );
     }
     
     public void addInputHandler( InputHandler< ? > inputHandler )
     {
         if ( !inputHandlers.contains( inputHandler ) )
             inputHandlers.add( inputHandler );
     }
     
     public void removeInputHandler( InputHandler< ? > inputHandler )
     {
         inputHandlers.remove( inputHandler );
     }
     
     public void addInputListener( InputListener l )
     {
         boolean contains = false;
         for ( int i = 0; i < inputListeners.size(); i++ )
         {
             if ( inputListeners.get( i ) == l )
             {
                 contains = true;
                 break;
             }
         }
         
         if ( !contains )
         {
             for ( int i = 0; i < keyboards.length; i++ )
             {
                 keyboards[ i ].addKeyboardListener( l );
             }
             
             for ( int i = 0; i < mouses.length; i++ )
             {
                 mouses[ i ].addMouseListener( l );
             }
             
             for ( int i = 0; i < controllers.length; i++ )
             {
                 controllers[ i ].addControllerListener( l );
             }
             
             inputListeners.add( l );
         }
     }
     
     public void removeInputListener( InputListener l )
     {
         if ( inputListeners.remove( l ) )
         {
             for ( int i = 0; i < keyboards.length; i++ )
             {
                 keyboards[ i ].removeKeyboardListener( l );
             }
             
             for ( int i = 0; i < mouses.length; i++ )
             {
                 mouses[ i ].removeMouseListener( l );
             }
             
             for ( int i = 0; i < controllers.length; i++ )
             {
                 controllers[ i ].removeControllerListener( l );
             }
         }
     }
     
     public void addInputStateListener( InputStateListener l )
     {
         boolean contains = false;
         for ( int i = 0; i < inputStateListeners.size(); i++ )
         {
             if ( inputStateListeners.get( i ) == l )
             {
                 contains = true;
                 break;
             }
         }
         
         if ( !contains )
         {
             for ( int i = 0; i < keyboards.length; i++ )
             {
                 keyboards[ i ].addInputStateListener( l );
             }
             
             for ( int i = 0; i < mouses.length; i++ )
             {
                 mouses[ i ].addInputStateListener( l );
             }
             
             for ( int i = 0; i < controllers.length; i++ )
             {
                 controllers[ i ].addInputStateListener( l );
             }
             
             inputStateListeners.add( l );
         }
     }
     
     public void removeInputStateListener( InputStateListener l )
     {
         if ( inputStateListeners.remove( l ) )
         {
             for ( int i = 0; i < keyboards.length; i++ )
             {
                 keyboards[ i ].removeInputStateListener( l );
             }
             
             for ( int i = 0; i < mouses.length; i++ )
             {
                 mouses[ i ].removeInputStateListener( l );
             }
             
             for ( int i = 0; i < controllers.length; i++ )
             {
                 controllers[ i ].removeInputStateListener( l );
             }
         }
     }
     
     /**
      * Registers the given Keyboard at this InputSystem.
      * 
      * @param keyboard
      * 
      * @throws InputSystemException
      */
     public void registerKeyboard( Keyboard keyboard ) throws InputSystemException
     {
         /*
         if ( !keyboard.getClass().isAssignableFrom( keyboard.getFactory().getExpectedKeyboardClass() ) )
             throw( new InputSystemException( "This Keyboard is not an instance of " + getDeviceFactory().getExpectedKeyboardClass() + "." ) );
         */
         
         for ( int i = 0; i < keyboards.length; i++ )
         {
             if ( keyboards[ i ] == keyboard )
                 throw( new InputSystemException( "This Keyboard is already registered at this InputSystem." ) );
         }
         
         Keyboard[] keyboards2 = new Keyboard[ keyboards.length + 1 ];
         System.arraycopy( keyboards, 0, keyboards2, 0, keyboards.length );
         keyboards2[ keyboards.length ] = keyboard;
         
         this.keyboards = keyboards2;
         
         keyboard.onDeviceRegistered( this );
         
         for ( int i = 0; i < inputListeners.size(); i++ )
         {
             keyboard.addKeyboardListener( inputListeners.get( i ) );
         }
         
         for ( int i = 0; i < inputStateListeners.size(); i++ )
         {
             keyboard.addInputStateListener( inputStateListeners.get( i ) );
         }
     }
     
     /**
      * Registers a new Keyboard.
      * 
      * @param factory
      * 
      * @return the registered Keyboard.
      * 
      * @throws InputSystemException
      */
     public final Keyboard registerNewKeyboard( KeyboardFactory factory ) throws InputSystemException
     {
         Keyboard[] allKeyboards = factory.getKeyboards( false );
         Keyboard[] newKeyboards = new Keyboard[ allKeyboards.length ];
         int numNewKeyboards = 0;
         
         for ( int i = 0; i < allKeyboards.length; i++ )
         {
             boolean b = false;
             for ( int j = 0; j < this.keyboards.length; j++ )
             {
                 if ( this.keyboards[ j ].equals( allKeyboards[ i ] ) )
                 {
                     b = true;
                     break;
                 }
             }
             
             if ( !b )
             {
                 newKeyboards[ numNewKeyboards++ ] = allKeyboards[ i ];
             }
         }
         
         if ( numNewKeyboards == 0 )
         {
             throw( new InputSystemException( "There's no available Keyboard to register." ) );
         }
         
         registerKeyboard( newKeyboards[ 0 ] );
         
         return( newKeyboards[ 0 ] );
     }
     
     /**
      * Registers a new Keyboard.
      * 
      * @param sourceWindow
      * 
      * @return the registered Keyboard.
      * 
      * @throws InputSystemException
      */
     public final Keyboard registerNewKeyboard( InputSourceWindow sourceWindow ) throws InputSystemException
     {
         return( registerNewKeyboard( sourceWindow.getInputDeviceFactory( this ) ) );
     }
     
     /**
      * Deregisters the given Keyboard from this InputSystem.
      * 
      * @param keyboard
      * 
      * @throws InputSystemException
      */
     public void deregisterKeyboard( Keyboard keyboard ) throws InputSystemException
     {
         if ( keyboards.length == 0 )
             throw( new InputSystemException( "This Keyboard is not registered at this InputSystem." ) );
         
         boolean found = false;
         Keyboard[] keyboards2 = new Keyboard[ keyboards.length - 1 ];
         int j = 0;
         for ( int i = 0; i < keyboards.length; i++ )
         {
             if ( keyboards[ i ] == keyboard )
                 found = true;
             else
                 keyboards2[ j++ ] = keyboards[ i ];
         }
         
         if ( !found )
             throw( new InputSystemException( "This Keyboard is not registered at this InputSystem." ) );
         
         for ( int i = 0; i < inputListeners.size(); i++ )
         {
             keyboard.removeKeyboardListener( inputListeners.get( i ) );
         }
         
         for ( int i = 0; i < inputStateListeners.size(); i++ )
         {
             keyboard.removeInputStateListener( inputStateListeners.get( i ) );
         }
         
         keyboard.destroy();
         
         this.keyboards = keyboards2;
         
         keyboard.onDeviceUnregistered( this );
     }
     
     /**
      * @return <code>true</code>, if at least one Keyboard is currently registered.
      */
     public final boolean hasKeyboard()
     {
         return( keyboards.length > 0 );
     }
     
     /**
      * @return the number of currently registered Keyboards.
      */
     public final int getKeyboardsCount()
     {
         return( keyboards.length );
     }
     
     /**
      * @return the first registered Keyboard.
      */
     public final Keyboard getKeyboard()
     {
         if ( keyboards.length >= 1 )
             return( keyboards[ 0 ] );
         
         return( null );
     }
     
     /**
      * @param index
      * 
      * @return the i-th registered Keyboard.
      */
     public final Keyboard getKeyboard( int index )
     {
         return( keyboards[ index ] );
     }
     
     /**
      * @return an array of all registered Keyboards in this InputSystem.
      */
     public final Keyboard[] getKeyboards()
     {
         if ( ( keyboards_out == null ) || ( keyboards_out.length != keyboards.length ) )
         {
             keyboards_out = new Keyboard[ keyboards.length ];
             System.arraycopy( keyboards, 0, keyboards_out, 0, keyboards.length );
         }
         
         return( keyboards_out );
     }
     
     /**
      * Checks, wether the given Keyboard is already registered at this InputSystem.
      * 
      * @param keyboard
      * 
      * @return true, if the Keyboard is registered.
      */
     public final boolean isKeyboardRegistered( Keyboard keyboard )
     {
         for ( int i = 0; i < keyboards.length; i++ )
         {
             if ( keyboards[ i ] == keyboard )
                 return( true );
         }
         
         return( false );
     }
     
     
     /**
      * Registers the given Mouse at this InputSystem.
      * 
      * @param mouse
      * 
      * @throws InputSystemException
      */
     public void registerMouse( Mouse mouse ) throws InputSystemException
     {
         /*
         if ( !mouse.getClass().isAssignableFrom( mouse.getFactory().getExpectedMouseClass() ) )
             throw( new InputSystemException( "This mouse is not an instance of " + getDeviceFactory().getExpectedMouseClass() + "." ) );
         */
         
         for ( int i = 0; i < mouses.length; i++ )
         {
             if ( mouses[ i ] == mouse )
                 throw( new InputSystemException( "This Mouse is already registered at this InputSystem." ) );
         }
         
         Mouse[] mouses2 = new Mouse[ mouses.length + 1 ];
         System.arraycopy( mouses, 0, mouses2, 0, mouses.length );
         mouses2[ mouses.length ] = mouse;
         
         this.mouses = mouses2;
         
         mouse.onDeviceRegistered( this );
         
         for ( int i = 0; i < inputListeners.size(); i++ )
         {
             mouse.addMouseListener( inputListeners.get( i ) );
         }
         
         for ( int i = 0; i < inputStateListeners.size(); i++ )
         {
             mouse.addInputStateListener( inputStateListeners.get( i ) );
         }
     }
     
     /**
      * Registers a new Mouse.
      * 
      * @param factory
      * 
      * @return the registered Mouse.
      * 
      * @throws InputSystemException
      */
     public final Mouse registerNewMouse( MouseFactory factory ) throws InputSystemException
     {
         Mouse[] allMouses = factory.getMouses( false );
         Mouse[] newMouses = new Mouse[ allMouses.length ];
         int numNewMouses = 0;
         
         for ( int i = 0; i < allMouses.length; i++ )
         {
             boolean b = false;
             for ( int j = 0; j < this.mouses.length; j++ )
             {
                 if ( this.mouses[ j ].equals( allMouses[ i ] ) )
                 {
                     b = true;
                     break;
                 }
             }
             
             if ( !b )
             {
                 newMouses[ numNewMouses++ ] = allMouses[ i ];
             }
         }
         
         if ( numNewMouses == 0 )
         {
             throw( new InputSystemException( "There's no available Mouse to register." ) );
         }
         
         registerMouse( newMouses[ 0 ] );
         
         return( newMouses[ 0 ] );
     }
     
     /**
      * Registers a new Mouse.
      * 
      * @param sourceWindow
      * 
      * @return the registered Mouse.
      * 
      * @throws InputSystemException
      */
     public final Mouse registerNewMouse( InputSourceWindow sourceWindow ) throws InputSystemException
     {
         return( registerNewMouse( sourceWindow.getInputDeviceFactory( this ) ) );
     }
     
     /**
      * Deregisters the given Mouse from this InputSystem.
      * 
      * @param mouse
      * 
      * @throws InputSystemException
      */
     public void deregisterMouse( Mouse mouse ) throws InputSystemException
     {
         if ( mouses.length == 0 )
             throw( new InputSystemException( "This Mouse is not registered at this InputSystem." ) );
         
         boolean found = false;
         Mouse[] mouses2 = new Mouse[ mouses.length - 1 ];
         int j = 0;
         for ( int i = 0; i < mouses.length; i++ )
         {
             if ( mouses[ i ] == mouse )
                 found = true;
             else
                 mouses2[ j++ ] = mouses[ i ];
         }
         
         if ( !found )
             throw( new InputSystemException( "This Mouse is not registered at this InputSystem." ) );
         
         for ( int i = 0; i < inputListeners.size(); i++ )
         {
             mouse.removeMouseListener( inputListeners.get( i ) );
         }
         
         for ( int i = 0; i < inputStateListeners.size(); i++ )
         {
             mouse.removeInputStateListener( inputStateListeners.get( i ) );
         }
         
         mouse.destroy();
         
         this.mouses = mouses2;
         
         mouse.onDeviceUnregistered( this );
     }
     
     /**
      * @return <code>true</code>, if at least one Mouse is currently registered at this InputSystem.
      */
     public final boolean hasMouse()
     {
         return( mouses.length > 0 );
     }
     
     /**
      * @return the number of currently registered Mouses.
      */
     public final int getMousesCount()
     {
         return( mouses.length );
     }
     
     /**
      * @return the first registered Mouse.
      */
     public final Mouse getMouse()
     {
         if ( mouses.length >= 1 )
             return( mouses[ 0 ] );
         
         return( null );
     }
     
     /**
      * @param index
      * 
      * @return the i-th registered Mouse.
      */
     public final Mouse getMouse( int index )
     {
         return( mouses[ index ] );
     }
     
     /**
      * @return an array of all currently registered Mouses.
      */
     public final Mouse[] getMouses()
     {
         if ( ( mouses_out == null ) || ( mouses_out.length != mouses.length ) )
         {
             mouses_out = new Mouse[ mouses.length ];
             System.arraycopy( mouses, 0, mouses_out, 0, mouses.length );
         }
         
         return( mouses_out );
     }
     
     /**
      * Checks, wether the given Mouse is already registered at this InputSystem.
      * 
      * @param mouse
      * 
      * @return true, if the Mouse is registered.
      */
     public final boolean isMouseRegistered( Mouse mouse )
     {
         for ( int i = 0; i < mouses.length; i++ )
         {
             if ( mouses[ i ] == mouse )
                 return( true );
         }
         
         return( false );
     }
     
     
     /**
      * Registers a new Keyboard and Mouse.
      * 
      * @param factory
      * 
      * @throws InputSystemException
      */
     public final void registerNewKeyboardAndMouse( InputDeviceFactory factory ) throws InputSystemException
     {
         registerNewKeyboard( factory );
         registerNewMouse( factory );
     }
     
     /**
      * Registers a new Keyboard and Mouse.
      * 
      * @param sourceWindow
      * 
      * @throws InputSystemException
      */
    public final void registerNewKeyboardAndMouse( InputSourceWindow sourceWindow ) throws InputSystemException
     {
         registerNewKeyboardAndMouse( sourceWindow.getInputDeviceFactory( this ) );
     }
     
     
     /**
      * Registers the given Controller at this InputSystem.
      * 
      * @param controller
      * 
      * @throws InputSystemException
      */
     public void registerController( Controller controller ) throws InputSystemException
     {
         /*
         if ( !controller.getClass().isAssignableFrom( controller.getFactory().getExpectedControllerClass() ) )
             throw( new InputSystemException( "This controller is not an instance of " + getDeviceFactory().getExpectedControllerClass() + "." ) );
         */
         
         for ( int i = 0; i < controllers.length; i++ )
         {
             if ( controllers[ i ] == controller )
                 throw( new InputSystemException( "This Controller is already registered at this InputSystem." ) );
         }
         
         Controller[] controllers2 = new Controller[ controllers.length + 1 ];
         System.arraycopy( controllers, 0, controllers2, 0, controllers.length );
         controllers2[ controllers.length ] = controller;
         
         this.controllers = controllers2;
         
         controller.onDeviceRegistered( this );
         
         for ( int i = 0; i < inputListeners.size(); i++ )
         {
             controller.addControllerListener( inputListeners.get( i ) );
         }
         
         for ( int i = 0; i < inputStateListeners.size(); i++ )
         {
             controller.addInputStateListener( inputStateListeners.get( i ) );
         }
     }
     
     /**
      * Registers a new Controller.
      * 
      * @param factory
      * 
      * @return the registered Controller.
      * 
      * @throws InputSystemException
      */
     public final Controller registerNewController( ControllerFactory factory ) throws InputSystemException
     {
         Controller[] allControllers = factory.getControllers( false );
         Controller[] newControllers = new Controller[ allControllers.length ];
         int numNewControllers = 0;
         
         for ( int i = 0; i < allControllers.length; i++ )
         {
             boolean b = false;
             for ( int j = 0; j < this.controllers.length; j++ )
             {
                 if ( this.controllers[ j ].equals( allControllers[ i ] ) )
                 {
                     b = true;
                     break;
                 }
             }
             
             if ( !b )
             {
                 newControllers[ numNewControllers++ ] = allControllers[ i ];
             }
         }
         
         if ( numNewControllers == 0 )
         {
             throw( new InputSystemException( "There's no available Controller to register." ) );
         }
         
         registerController( newControllers[ 0 ] );
         
         return( newControllers[ 0 ] );
     }
     
     /**
      * Registers a new Controller.
      * 
      * @param sourceWindow
      * 
      * @return the registered Controller.
      * 
      * @throws InputSystemException
      */
     public final Controller registerNewController( InputSourceWindow sourceWindow ) throws InputSystemException
     {
         return( registerNewController( sourceWindow.getInputDeviceFactory( this ) ) );
     }
     
     /**
      * Deregisters the given Controller from this InputSystem.
      * 
      * @param controller
      * 
      * @throws InputSystemException
      */
     public void deregisterController( Controller controller ) throws InputSystemException
     {
         if ( controllers.length == 0 )
             throw( new InputSystemException( "This Controller is not registered at this InputSystem." ) );
         
         boolean found = false;
         Controller[] controllers2 = new Controller[ controllers.length - 1 ];
         int j = 0;
         for ( int i = 0; i < controllers.length; i++ )
         {
             if ( controllers[ i ] == controller )
                 found = true;
             else
                 controllers2[ j++ ] = controllers[ i ];
         }
         
         if ( !found )
             throw( new InputSystemException( "This Controller is not registered at this InputSystem." ) );
         
         for ( int i = 0; i < inputListeners.size(); i++ )
         {
             controller.removeControllerListener( inputListeners.get( i ) );
         }
         
         for ( int i = 0; i < inputStateListeners.size(); i++ )
         {
             controller.removeInputStateListener( inputStateListeners.get( i ) );
         }
         
         controller.destroy();
         
         this.controllers = controllers2;
         
         controller.onDeviceUnregistered( this );
     }
     
     /**
      * @return <code>true</code>, if at least one Controller is currently registered.
      */
     public final boolean hasController()
     {
         return( controllers.length > 0 );
     }
     
     /**
      * @return the number of currently registered Controllers.
      */
     public final int getControllersCount()
     {
         return( controllers.length );
     }
     
     /**
      * @return the first registered Controller.
      */
     public final Controller getController()
     {
         if ( controllers.length >= 1 )
             return( controllers[ 0 ] );
         
         return( null );
     }
     
     /**
      * @param index
      * 
      * @return the i-th registered Controller.
      */
     public final Controller getController( int index )
     {
         return( controllers[ index ] );
     }
     
     /**
      * @return an array of all currently registered Controllers.
      */
     public final Controller[] getControllers()
     {
         if ( ( controllers_out == null ) || ( controllers_out.length != controllers.length ) )
         {
             controllers_out = new Controller[ controllers.length ];
             System.arraycopy( controllers, 0, controllers_out, 0, controllers.length );
         }
         
         return( controllers_out );
     }
     
     /**
      * Checks, wether the given Controller is already registered at this InputSystem.
      * 
      * @param controller
      * 
      * @return true, if the Controller is registered.
      */
     public final boolean isControllerRegistered( Controller controller )
     {
         for ( int i = 0; i < controllers.length; i++ )
         {
             if ( controllers[ i ] == controller )
                 return( true );
         }
         
         return( false );
     }
     
     protected void collectKeyboardEvents( long nanoTime ) throws InputSystemException
     {
         for ( int i = 0; i < keyboards.length; i++ )
         {
             if ( keyboards[ i ].isEnabled() )
                 keyboards[ i ].collectEvents( this, eventQueue, nanoTime );
         }
     }
     
     protected void collectMouseEvents( long nanoTime ) throws InputSystemException
     {
         for ( int i = 0; i < mouses.length; i++ )
         {
             if ( mouses[ i ].isEnabled() )
                 mouses[ i ].collectEvents( this, eventQueue, nanoTime );
         }
     }
     
     protected void collectControllerEvents( EventQueue eventQueue, long nanoTime ) throws InputSystemException
     {
         for ( int i = 0; i < controllers.length; i++ )
         {
             if ( controllers[ i ].isEnabled() )
                 controllers[ i ].collectEvents( this, eventQueue, nanoTime );
         }
     }
     
     /**
      * Processes pending events from the system
      * and places them into the EventQueue.<br>
      * The events are not fired (listeners are not notified).
      * They are fired when the {@link #update(InputSystem, EventQueue, long)}
      * method is invoked.
      * 
      * @param nanoTime
      * 
      * @throws InputSystemException
      */
     public void collectEvents( long nanoTime ) throws InputSystemException
     {
         collectKeyboardEvents( nanoTime );
         collectMouseEvents( nanoTime );
         collectControllerEvents( eventQueue, nanoTime );
     }
     
     protected void updateKeyboards( long nanoTime ) throws InputSystemException
     {
         for ( int i = 0; i < keyboards.length; i++ )
         {
             if ( keyboards[ i ].isEnabled() )
                 keyboards[ i ].update( this, eventQueue, nanoTime );
         }
     }
     
     protected void updateMouses( long nanoTime ) throws InputSystemException
     {
         for ( int i = 0; i < mouses.length; i++ )
         {
             if ( mouses[ i ].isEnabled() )
                 mouses[ i ].update( this, eventQueue, nanoTime );
         }
     }
     
     protected void updateControllers( EventQueue eventQueue, long nanoTime ) throws InputSystemException
     {
         for ( int i = 0; i < controllers.length; i++ )
         {
             if ( controllers[ i ].isEnabled() )
                 controllers[ i ].update( this, eventQueue, nanoTime );
         }
     }
     
     protected void updateInputHandlers( long nanoTime ) throws InputSystemException
     {
         for ( int i = 0; i < inputHandlers.size(); i++ )
         {
             inputHandlers.get( i ).update( nanoTime );
         }
     }
     
     /**
      * Processes pending events from the system
      * and directly fires them (notifes the listeners).<br>
      * This method also flushes the EventQueue, if the previously
      * called {@link #collectEvents(InputSystem, EventQueue, long)}
      * method placed events into it.
      * 
      * @param nanoTime
      * 
      * @throws InputSystemException
      */
     public void update( long nanoTime ) throws InputSystemException
     {
         updateKeyboards( nanoTime );
         updateMouses( nanoTime );
         updateControllers( eventQueue, nanoTime );
         
         updateInputHandlers( nanoTime );
     }
     
     public void destroy() throws InputSystemException
     {
         HashSet< Object > factories = new HashSet< Object >();
         
         for ( int i = keyboards.length - 1; i >= 0; i-- )
         {
             factories.add( keyboards[ i ].getSourceFactory() );
             
             deregisterKeyboard( keyboards[ i ] );
         }
         
         for ( int i = mouses.length - 1; i >= 0; i-- )
         {
             factories.add( mouses[ i ].getSourceFactory() );
             
             deregisterMouse( mouses[ i ] );
         }
         
         for ( int i = controllers.length - 1; i >= 0; i-- )
         {
             factories.add( controllers[ i ].getSourceFactory() );
             
             deregisterController( controllers[ i ] );
         }
         
         for ( Object factory: factories )
         {
             if ( factory instanceof KeyboardFactory )
                 ((KeyboardFactory)factory).destroy( this );
             else if ( factory instanceof MouseFactory )
                 ((MouseFactory)factory).destroy( this );
             else if ( factory instanceof ControllerFactory )
                 ((ControllerFactory)factory).destroy( this );
         }
     }
     
     public void destroy( KeyboardFactory deviceFactory ) throws InputSystemException
     {
         for ( int i = keyboards.length - 1; i >= 0; i-- )
         {
             if ( keyboards[ i ].getSourceFactory() == deviceFactory )
                 deregisterKeyboard( keyboards[ i ] );
         }
         
         deviceFactory.destroy( this );
     }
     
     public void destroy( MouseFactory deviceFactory ) throws InputSystemException
     {
         for ( int i = mouses.length - 1; i >= 0; i-- )
         {
             if ( mouses[ i ].getSourceFactory() == deviceFactory )
                 deregisterMouse( mouses[ i ] );
         }
         
         deviceFactory.destroy( this );
     }
     
     public void destroy( ControllerFactory deviceFactory ) throws InputSystemException
     {
         for ( int i = controllers.length - 1; i >= 0; i-- )
         {
             if ( controllers[ i ].getSourceFactory() == deviceFactory )
                 deregisterController( controllers[ i ] );
         }
         
         deviceFactory.destroy( this );
     }
     
     public void destroy( InputDeviceFactory deviceFactory ) throws InputSystemException
     {
         destroy( (KeyboardFactory)deviceFactory );
         destroy( (MouseFactory)deviceFactory );
         destroy( (ControllerFactory)deviceFactory );
     }
     
     public void destroy( InputSourceWindow sourceWindow ) throws InputSystemException
     {
         destroy( sourceWindow.getInputDeviceFactory( this ) );
     }
     
     public InputSystem()
     {
         this.eventQueue = new EventQueue();
     }
 }
