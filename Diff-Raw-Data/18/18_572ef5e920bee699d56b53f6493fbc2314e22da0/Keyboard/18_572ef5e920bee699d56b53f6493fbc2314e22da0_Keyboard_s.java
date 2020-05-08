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
 package org.jagatoo.input.devices;
 
 import java.util.ArrayList;
 
 import org.jagatoo.input.InputSystemException;
 import org.jagatoo.input.devices.components.InputState;
 import org.jagatoo.input.devices.components.Key;
 import org.jagatoo.input.devices.components.Keys;
 import org.jagatoo.input.events.EventQueue;
 import org.jagatoo.input.events.KeyPressedEvent;
 import org.jagatoo.input.events.KeyReleasedEvent;
 import org.jagatoo.input.events.KeyTypedEvent;
 import org.jagatoo.input.events.KeyboardEvent;
 import org.jagatoo.input.events.KeyboardEventPool;
 import org.jagatoo.input.listeners.KeyboardListener;
 import org.jagatoo.input.localization.KeyboardLocalizer;
 import org.jagatoo.input.render.InputSourceWindow;
 
 /**
  * This is the base-class for all Keyboard implementations.<br>
  * Applications should always use instances as Keyboard, but never cast them to
  * the concrete implementation.<br>
  * Instances can only be created/retrieved through an {@link InputDeviceFactory}.
  * 
  * @author Marvin Froehlich (aka Qudus)
  */
 public abstract class Keyboard extends InputDevice
 {
     private final KeyboardFactory sourceFactory;
     
     private final boolean[] keyStates;
     
     private int modifierMask = 0;
     
     private final ArrayList< KeyboardListener > listeners = new ArrayList< KeyboardListener >();
     
     /**
      * @return the {@link KeyboardFactory}, that created this instance.
      */
     public final KeyboardFactory getSourceFactory()
     {
         return( sourceFactory );
     }
     
     /**
      * @return true, of at least one {@link KeyboardListener} is currently registered.
      */
     public final boolean hasKeyboardListener()
     {
         return( listeners.size() > 0 );
     }
     
     /**
      * @return true, if one of {@link #hasInputStateListener()} or {@link #hasKeyboardListener()} return true.
      */
     public final boolean hasListener()
     {
         return( hasInputStateListener() || hasKeyboardListener() );
     }
     
     /**
      * Adds a {@link KeyboardListener} to the list of notified instances.
      * 
      * @param l
      */
     public void addKeyboardListener( KeyboardListener l )
     {
         for ( int i = 0; i < listeners.size(); i++ )
         {
             if ( listeners.get( i ) == l )
                 return;
         }
         
         listeners.add( l );
     }
     
     /**
      * Removes a {@link KeyboardListener} from the list of notified instances.
      * 
      * @param l
      */
     public void removeKeyboardListener( KeyboardListener l )
     {
         listeners.remove( l );
     }
     
     /**
      * Combines the given key's maks-value to the modifier-mask,
      * if the Key is a modifier.
      * 
      * @param key
      * @param isKeyDown
      * 
      * @return the new modifier-mask.
      */
     protected int applyModifier( Key key, boolean isKeyDown )
     {
         if ( key == null )
             return( modifierMask );
         
         switch ( key.getKeyID() )
         {
             case LEFT_SHIFT:
             case RIGHT_SHIFT:
                 if ( isKeyDown )
                     modifierMask |= Keys.MODIFIER_SHIFT;
                 else
                     modifierMask &= ~Keys.MODIFIER_SHIFT;
                 break;
             case LEFT_CONTROL:
             case RIGHT_CONTROL:
                 if ( isKeyDown )
                     modifierMask |= Keys.MODIFIER_CONTROL;
                 else
                     modifierMask &= ~Keys.MODIFIER_CONTROL;
                 break;
             case ALT:
                 if ( isKeyDown )
                     modifierMask |= Keys.MODIFIER_ALT;
                 else
                     modifierMask &= ~Keys.MODIFIER_ALT;
                 break;
             case ALT_GRAPH:
                 if ( isKeyDown )
                     modifierMask |= Keys.MODIFIER_ALT_GRAPH;
                 else
                     modifierMask &= ~Keys.MODIFIER_ALT_GRAPH;
                 break;
             case LEFT_META:
                 if ( isKeyDown )
                     modifierMask |= Keys.MODIFIER_LEFT_META;
                 else
                     modifierMask &= ~Keys.MODIFIER_LEFT_META;
                 break;
             case RIGHT_META:
                 if ( isKeyDown )
                     modifierMask |= Keys.MODIFIER_RIGHT_META;
                 else
                     modifierMask &= ~Keys.MODIFIER_RIGHT_META;
                 break;
         }
         
         return( modifierMask );
     }
     
     /**
      * @return the current modifier-mask.
      */
     public final int getModifierMask()
     {
         return( modifierMask );
     }
     
     /**
      * Prepares a {@link KeyPressedEvent} for being fired.<br>
      * The event is not fired from this method.<br>
      * This method cares about the current key-state.
      * 
      * @param key
      * @param modifierMask
      * @param when
      * @param lastWhen
      * 
      * @return the new event from the pool or <code>null</code>, if no events are currently accepted.
      */
     protected final KeyPressedEvent prepareKeyPressedEvent( Key key, int modifierMask, long when, long lastWhen )
     {
        if ( !isEnabled() || !hasListener() )
             return( null );
         
         keyStates[ key.getKeyCode() - 1 ] = true;
         
         KeyPressedEvent e = KeyboardEventPool.allocPressed( this, key, modifierMask, when, lastWhen );
         
         return( e );
     }
     
     /**
      * This method is asked by the {@link #fireOnKeyPressed(KeyPressedEvent, boolean)}
      * and {@link #fireOnKeyReleased(KeyReleasedEvent, boolean)} methods
      * and listeners are not notified, if this method returns false.
      * 
      * @param key
      * @param keyState
      */
     protected abstract boolean hasKeyStateChanged( Key key, boolean keyState );
     
     /**
      * Fires a {@link KeyPressedEvent} and pushes it back to the pool,
      * if consumeEvent is true.
      * 
      * @param e
      * @param consumeEvent
      */
     public final void fireOnKeyPressed( KeyPressedEvent e, boolean consumeEvent )
     {
         if ( !isEnabled() || !hasListener() )
         {
             if ( consumeEvent )
                 KeyboardEventPool.freePressed( e );
             return;
         }
         
         if ( hasKeyStateChanged( e.getKey(), keyStates[ e.getKeyCode() - 1 ] ) )
         {
             for ( int i = 0; i < listeners.size(); i++ )
             {
                 listeners.get( i ).onKeyStateChanged( e, e.getKey(), e.getKeyBooleanState() );
                 listeners.get( i ).onKeyPressed( e, e.getKey() );
             }
         }
         
         fireStateEventsAndDoActions( e, +1, 1 );
         
         if ( consumeEvent )
             KeyboardEventPool.freePressed( e );
     }
     
     /**
      * Prepares a {@link KeyReleasedEvent} for being fired.<br>
      * The event is not fired from this method.<br>
      * This method cares about the current key-state.
      * 
      * @param key
      * @param modifierMask
      * @param when
      * @param lastWhen
      * 
      * @return the new event from the pool or <code>null</code>, if no events are currently accepted.
      */
     protected final KeyReleasedEvent prepareKeyReleasedEvent( Key key, int modifierMask, long when, long lastWhen )
     {
        if ( !isEnabled() || !hasListener() )
             return( null );
         
         keyStates[ key.getKeyCode() - 1 ] = false;
         
         KeyReleasedEvent e = KeyboardEventPool.allocReleased( this, key, modifierMask, when, lastWhen );
         
         return( e );
     }
     
     /**
      * Fires a {@link KeyReleasedEvent} and pushes it back to the pool,
      * if consumeEvent is true.
      * 
      * @param e
      * @param consumeEvent
      */
     public final void fireOnKeyReleased( KeyReleasedEvent e, boolean consumeEvent )
     {
         if ( !isEnabled() || !hasListener() )
         {
             if ( consumeEvent )
                 KeyboardEventPool.freeReleased( e );
             return;
         }
         
         if ( hasKeyStateChanged( e.getKey(), keyStates[ e.getKeyCode() - 1 ] ) )
         {
             for ( int i = 0; i < listeners.size(); i++ )
             {
                 listeners.get( i ).onKeyStateChanged( e, e.getKey(), e.getKeyBooleanState() );
                 listeners.get( i ).onKeyReleased( e, e.getKey() );
             }
         }
         
         fireStateEventsAndDoActions( e, -1, 0 );
         
         if ( consumeEvent )
             KeyboardEventPool.freeReleased( e );
     }
     
     /**
      * Prepares a {@link KeyTypedEvent} for being fired.<br>
      * The event is not fired from this method.<br>
      * 
      * @param keyChar
      * @param modifierMask
      * @param when
      * @param lastWhen
      * 
      * @return the new event from the pool or <code>null</code>, if no events are currently accepted.
      */
     protected final KeyTypedEvent prepareKeyTypedEvent( char keyChar, int modifierMask, long when, long lastWhen )
     {
         if ( !isEnabled() || !hasListener() )
             return( null );
         
         KeyTypedEvent e = KeyboardEventPool.allocTyped( this, keyChar, modifierMask, when, lastWhen );
         
         return( e );
     }
     
     /**
      * Fires a {@link KeyTypedEvent} and pushes it back to the pool,
      * if consumeEvent is true.
      * 
      * @param e
      * @param consumeEvent
      */
     public final void fireOnKeyTyped( KeyTypedEvent e, boolean consumeEvent )
     {
         if ( !isEnabled() || !hasListener() )
         {
             if ( consumeEvent )
                 KeyboardEventPool.freeTyped( e );
             return;
         }
         
         for ( int i = 0; i < listeners.size(); i++ )
         {
             listeners.get( i ).onKeyTyped( e, e.getKeyChar() );
         }
         
         if ( consumeEvent )
             KeyboardEventPool.freeTyped( e );
     }
     
     /**
      * this method simply forwards to the concrete fire* methods.
      * 
      * @param e
      * @param consumeEvent
      */
     public final void fireKeyboardEvent( KeyboardEvent e, boolean consumeEvent )
     {
         switch ( e.getSubType() )
         {
             case PRESSED:
                 fireOnKeyPressed( (KeyPressedEvent)e, consumeEvent );
                 break;
             case RELEASED:
                 fireOnKeyReleased( (KeyReleasedEvent)e, consumeEvent );
                 break;
             case TYPED:
                 fireOnKeyTyped( (KeyTypedEvent)e, consumeEvent );
                 break;
         }
     }
     
     /**
      * @param key
      * 
      * @return true, if the given Key is currently pressed on this Keyboard.
      */
     public final boolean isKeyPressed( Key key )
     {
         return( keyStates[ key.getKeyCode() - 1 ] );
     }
     
     /**
      * @param key
      * 
      * @return The given Key's state on this Keyboard.
      */
     public final InputState getKeyState( Key key )
     {
         if ( isKeyPressed( key ) )
             return( InputState.POSITIVE );
         else
             return( InputState.NEGATIVE );
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString()
     {
         return( this.getClass().getSimpleName() + " { name = \"" + getName() + "\", localization-mapping = \"" + ( ( KeyboardLocalizer.getMapping() != null ) ? KeyboardLocalizer.getMapping().getClass().getName() : null ) + "\" }" );
     }
     
     /**
      * Destroys the Keyboard.
      */
     protected abstract void destroyImpl() throws InputSystemException;
     
     /**
      * {@inheritDoc}
      */
     @Override
     public final void destroy() throws InputSystemException
     {
         destroyImpl();
     }
     
     protected Keyboard( KeyboardFactory sourceFactory, InputSourceWindow sourceWindow, EventQueue eventQueue, String name ) throws InputSystemException
     {
         super( sourceWindow, eventQueue, name );
         
         this.sourceFactory = sourceFactory;
         
         this.keyStates = new boolean[ Keys.getNumKeys() ];
         java.util.Arrays.fill( keyStates, false );
     }
 }
