 /*
  * Copyright (c) 2003-2006 jMonkeyEngine
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  * * Redistributions of source code must retain the above copyright
  *   notice, this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary form must reproduce the above copyright
  *   notice, this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  *
  * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
  *   may be used to endorse or promote products derived from this software 
  *   without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.jme.input;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import com.jme.input.action.InputAction;
 import com.jme.input.action.InputActionEvent;
 import com.jme.input.action.KeyInputAction;
 import com.jme.input.action.MouseInputAction;
 import com.jme.input.joystick.Joystick;
 import com.jme.input.joystick.JoystickInput;
 import com.jme.input.joystick.JoystickInputHandlerDevice;
 import com.jme.input.keyboard.KeyboardInputHandlerDevice;
 import com.jme.input.mouse.MouseInputHandlerDevice;
 import com.jme.util.LoggingSystem;
 
 /**
  * <code>InputHandler</code> handles mouse, key and other inputs. Actions can be subscribed for specific event triggers.
  * {@link InputAction#performAction(InputActionEvent)} is invoked within the {@link #update} method whenever the
  * trigger criterias match. For a usage example see TestInputHandler. InputHandler is also used to decouple event
  * occurence and action invocation (see {@link ActionTrigger}) - an event may occur in another thread (e.g. polling
  * thread) but the action is still invoked in the thread that is calling the {@link #update} method.
  * <br>
  * You can add custom devices via the {@link #addDevice(InputHandlerDevice)} method.
  *
  * @author Mark Powell
  * @author Jack Lindamood - (javadoc only)
  * @author Irrisor - revamp
 * @version $Id: InputHandler.java,v 1.30 2006-01-13 19:39:27 renanse Exp $
  */
 public class InputHandler extends AbstractInputHandler {
     /**
      * Stores first active trigger that is invoked upon next update. Other active triggers are reachable via
      * {@link ActionTrigger#getNext()} (linked list).
      */
     ActionTrigger activeTriggers;
 
     /**
      * list of all {@link ActionTrigger}s of this input handler (triggers add themselves).
      */
     ArrayList allTriggers;
 
     /**
      * Device name of the mouse.
      *
      * @see #addAction(com.jme.input.action.InputAction, String, int, int, boolean)
      */
     public static final String DEVICE_MOUSE = "mouse";
     /**
      * Device name of the keyboard.
      *
      * @see #addAction(com.jme.input.action.InputAction, String, int, int, boolean)
      */
     public static final String DEVICE_KEYBOARD = "keyboard";
 
     /**
      * Wildcard device name for all devices.
      *
      * @see #addAction(com.jme.input.action.InputAction, String, int, int, boolean)
      */
     public static final String DEVICE_ALL = "ALL DEVICES";
 
     /**
      * int value for representing no button/key.
      *
      * @see #addAction(com.jme.input.action.InputAction, String, int, int, boolean)
      */
     public static final int BUTTON_NONE = -1;
     /**
      * int value for representing all buttons/keys (wildcard).
      *
      * @see #addAction(com.jme.input.action.InputAction, String, int, int, boolean)
      */
     public static final int BUTTON_ALL = Integer.MIN_VALUE;
     /**
      * int value for representing no axis.
      *
      * @see #addAction(com.jme.input.action.InputAction, String, int, int, boolean)
      */
     public static final int AXIS_NONE = -1;
     /**
      * int value for representing all axes (wildcard).
      *
      * @see #addAction(com.jme.input.action.InputAction, String, int, int, boolean)
      */
     public static final int AXIS_ALL = Integer.MIN_VALUE;
 
     /** Not used any more. */
     protected Mouse mouse; //todo: remove this field in .11
 
     /**
      * event that will be used to call each action this frame.
      */
     protected InputActionEvent event = new InputActionEvent();
 
     /**
      * Creates a new input handler. By default, there are no keyboard actions or
      * mouse actions defined.
      */
     public InputHandler() {
         initializeDefaultDevices();
     }
 
     /**
      * @return keyboard manager.
      * @deprecated use {@link KeyBindingManager#getKeyBindingManager()}
      */
     public KeyBindingManager getKeyBindingManager() {
         //todo: remove this method in .11
         return KeyBindingManager.getKeyBindingManager();
     }
 
     /**
      * Sets the mouse to receive mouse inputs from.
      *
      * @param mouse This handler's new mouse.
      * @deprecated store your mouse somewhere else (e.g. in your game class or menu state) and use
      *             {@link Mouse#registerWithInputHandler} to update the mouse automatically
      */
     public void setMouse( final Mouse mouse ) {
         //todo: remove this method in .11
         //noinspection deprecation
         Mouse oldValue = this.mouse;
         if ( oldValue != mouse ) {
             if ( oldValue != null ) {
                 oldValue.registerWithInputHandler( null );
             }
             if ( mouse != null ) {
                 mouse.registerWithInputHandler( this );
             }
             //noinspection deprecation
             this.mouse = mouse;
         }
     }
 
     /**
      * Returns the mouse that was set by setMouse before - not used any more.
      *
      * @return mouse
      * @deprecated store your mouse somewhere else (e.g. in your game class or menu state)
      */
     public Mouse getMouse() {
         //todo: remove this method in .11
         // noinspection deprecation
         return mouse;
     }
 
     /**
      * @see com.jme.input.action.InputAction#setSpeed(float)
      * @deprecated InputHander does not distinguish between key and mouse actions any more
      *             - use {@link #setActionSpeed} to change speed of all actions
      */
     public void setKeySpeed( float speed ) {
         //todo: remove this method in .11
         synchronized ( this ) {
             if ( allTriggers != null ) {
                 for ( int i = allTriggers.size() - 1; i >= 0; i-- ) {
                     ActionTrigger actionTrigger = ( (ActionTrigger) allTriggers.get( i ) );
                     if ( DEVICE_KEYBOARD.equals( actionTrigger.getDeviceName() ) ) {
                         actionTrigger.action.setSpeed( speed );
                     }
                 }
             }
         }
     }
 
     /**
      * @see com.jme.input.action.InputAction#setSpeed(float)
      * @deprecated InputHander does not distinguish between key and mouse actions any more
      *             - use {@link #setActionSpeed} to change speed of all actions
      */
     public void setMouseSpeed( float speed ) {
         //todo: remove this method in .11
         synchronized ( this ) {
             if ( allTriggers != null ) {
                 for ( int i = allTriggers.size() - 1; i >= 0; i-- ) {
                     ActionTrigger actionTrigger = ( (ActionTrigger) allTriggers.get( i ) );
                     if ( DEVICE_MOUSE.equals( actionTrigger.getDeviceName() ) ) {
                         actionTrigger.action.setSpeed( speed );
                     }
                 }
             }
         }
     }
 
     /**
      * Sets the speed of all actions currently registered with this handler to
      * the given value.
      *
      * @param speed The new speed for all currently registered actions.
      * @see com.jme.input.action.InputAction#setSpeed(float)
      */
     public void setActionSpeed( float speed ) {
         synchronized ( this ) {
             if ( allTriggers != null ) {
                 for ( int i = allTriggers.size() - 1; i >= 0; i-- ) {
                     ( (ActionTrigger) allTriggers.get( i ) ).action.setSpeed( speed );
                 }
             }
         }
     }
 
     /**
      * @deprecated use {@link #addAction(com.jme.input.action.InputAction, String, boolean)} to specify needed parameters
      */
     public void addAction( KeyInputAction inputAction ) {
         // noinspection deprecation
         addAction( inputAction, inputAction.getKey(), inputAction.allowsRepeats() );
     }
 
     /**
      * Adds an input action to be invoked by this handler during update.
      *
      * @param inputAction    the input action to be added
      * @param triggerCommand the command to trigger this action (registered with {@link KeyBindingManager}), if null
      *                       the action is invoked on each call of {@link #update}
      * @param allowRepeats   true to invoke the action every call of update the trigger is lit, false to invoke
      *                       the action only once every time the trigger is lit
      */
     public void addAction( InputAction inputAction, String triggerCommand, boolean allowRepeats ) {
         // noinspection deprecation
         new ActionTrigger.CommandTrigger( this, triggerCommand, inputAction, allowRepeats );
     }
 
     /**
      * Registeres a single key as command in {@link KeyBindingManager} and adds an input
      * action to be invoked by this handler during update.
      *
      * @param inputAction    the input action to be added
      * @param triggerCommand the command to trigger this action, may not be null (unlike in
      *                       {@link #addAction(com.jme.input.action.InputAction, String, boolean)})
      * @param keyCode        the keyCode to register at {@link KeyBindingManager} for the command
      * @param allowRepeats   true to invoke the action every call of update the trigger is lit, false to invoke
      *                       the action only once every time the trigger is lit
      */
     public void addAction( InputAction inputAction, String triggerCommand, int keyCode, boolean allowRepeats ) {
         if ( triggerCommand == null ) {
             throw new NullPointerException( "triggerCommand may not be null" );
         }
         KeyBindingManager.getKeyBindingManager().add( triggerCommand, keyCode );
         addAction( inputAction, triggerCommand, allowRepeats );
     }
 
     /**
      * @deprecated use {@link #addAction(com.jme.input.action.InputAction, String, boolean)} to specify needed parameters
      */
     public void addKeyboardAction( String command, int keyInputValue, KeyInputAction action ) {
         KeyBindingManager.getKeyBindingManager().set( command, keyInputValue );
         // noinspection deprecation
         addAction( action, command, action.allowsRepeats() );
     }
 
     /**
      * Adds a mouse input action to be invoked each frame.
      * Use {@link #addAction(InputAction, String, int, int, boolean)} to add actions that
      * are invoked on mouse events.
      *
      * @param mouseAction The input action to be added
      */
     public void addAction( MouseInputAction mouseAction ) {
         addAction( mouseAction, DEVICE_MOUSE, BUTTON_NONE, 0, true );
     }
 
     /**
      * Devices for all handlers.
      * TODO: we could decide to have one device per handler to reduce amount of triggers that are checked on each event
      */
     private static Map devices;
 
     /**
      * create mouse, keyboard and joystick devices (if not yet created).
      */
     private static void initializeDefaultDevices() {
         //TODO: synchronize if multithreaded creation of handlers should be supported
         if ( devices == null ) {
             devices = new HashMap();
             addDevice( new MouseInputHandlerDevice() );
             addDevice( new KeyboardInputHandlerDevice() );
             for ( int i = JoystickInput.get().getJoystickCount() - 1; i >= 0; i-- ) {
                 Joystick joystick = JoystickInput.get().getJoystick( i );
                 addDevice( new JoystickInputHandlerDevice( joystick ) );
             }
         }
     }
 
     /**
      * Add a device to the InputHandlers. Note: only newly added actions regard the added devices (actions added
      * with {@link #DEVICE_ALL} before device was added will not receive device events).
      *
      * @param device new device
      * @see InputHandlerDevice
      */
     public static void addDevice( InputHandlerDevice device ) {
         if ( device != null ) {
             InputHandlerDevice oldDevice = (InputHandlerDevice) devices.put( device.getName(), device );
             if ( oldDevice != null && oldDevice != device ) {
                 LoggingSystem.getLogger().warning( "InputHandlerDevice name '" + device.getName() + "' used twice!" );
             }
         }
     }
 
     /**
      * Adds an input action to be invoked on deviceName button or axis events.
      *
      * @param action       the input action to be added
      * @param deviceName   name of the deviceName: {@link #DEVICE_MOUSE}, {@link #DEVICE_KEYBOARD},
      *                     a joystick name or {@link #DEVICE_ALL}
      * @param button       index of the button that triggers this event, {@link #BUTTON_NONE} for no button,
      *                     {@link #BUTTON_ALL} for all buttons. (for keyboad deviceName this is a key code).
      *                     If DEVICE_ALL is specified button will not be interpreted as key code, thus
      *                     keyboard input is only regarded if BUTTON_ALL is specified inthis case.
      * @param axis         index of the axis that triggers this event, {@link #AXIS_NONE} for no axis,
      *                     {@link #AXIS_ALL} for all axes
      * @param allowRepeats false to invoke action once for each button down, true to invoke each frame while the
      *                     button is pressed
      */
     public void addAction( InputAction action, String deviceName, int button, int axis, boolean allowRepeats ) {
         if ( DEVICE_ALL.equals( deviceName ) ) {
             for ( Iterator it = devices.values().iterator(); it.hasNext(); ) {
                 InputHandlerDevice device = (InputHandlerDevice) it.next();
                 device.createTriggers( action, axis, button, allowRepeats, this );
             }
         }
         else {
             InputHandlerDevice device = (InputHandlerDevice) devices.get( deviceName );
             if ( device != null ) {
                 device.createTriggers( action, axis, button, allowRepeats, this );
             }
             else {
                 throw new UnsupportedOperationException( "Device '" + deviceName + "' is unknown!" );
             }
         }
     }
 
     /**
      * Removes a keyboard input action from the list of keyActions that are
      * polled during update.
      *
      * @param inputAction The action to remove.
      */
     public void removeAction( InputAction inputAction ) {
         synchronized ( this ) {
             for ( int i = allTriggers.size() - 1; i >= 0; i-- ) {
                 ActionTrigger trigger = (ActionTrigger) allTriggers.get( i );
                 if ( trigger.action == inputAction ) {
                     trigger.remove();
                     //go on, action could be in more triggers
                 }
             }
         }
     }
 
     /**
      * Clears all keyboard actions currently stored.
      *
      * @deprecated InputHander does not distinguish between key and mouse actions any more
      *             - use {@link #clearActions} to remove all actions
      */
     public void clearKeyboardActions() {
         //todo: remove this method in .11
         synchronized ( this ) {
             for ( int i = allTriggers.size() - 1; i >= 0; i-- ) {
                 ActionTrigger trigger = (ActionTrigger) allTriggers.get( i );
                 if ( DEVICE_KEYBOARD.equals( trigger.getDeviceName() ) ) {
                     trigger.remove();
                     //go on, action could be in more triggers
                 }
             }
         }
     }
 
     /**
      * Clears all mouse actions currently stored.
      *
      * @deprecated InputHander does not distinguish between key and mouse actions any more
      *             - use {@link #clearActions} to remove all actions
      */
     public void clearMouseActions() {
         //todo: remove this method in .11
         synchronized ( this ) {
             for ( int i = allTriggers.size() - 1; i >= 0; i-- ) {
                 ActionTrigger trigger = (ActionTrigger) allTriggers.get( i );
                 if ( DEVICE_MOUSE.equals( trigger.getDeviceName() ) ) {
                     trigger.remove();
                     //go on, action could be in more triggers
                 }
             }
         }
     }
 
     /**
      * Clears all actions currently registered.
      */
     public void clearActions() {
         synchronized ( this ) {
             for ( int i = allTriggers.size() - 1; i >= 0; i-- ) {
                 ActionTrigger trigger = (ActionTrigger) allTriggers.get( i );
                 trigger.remove();
             }
         }
     }
 
     /**
      * Checks all actions to see if they should be invoked. If
      * so, {@link InputAction#performAction} is called on the action with the given time.
      * <br>
      * This method can be invoked while the handler is disabled. Thus the method should
      * check {@link #isEnabled()} and return immediately if it evaluates to false.
      * <br>
      * This method should normally not be overwritten by subclasses. If an InputHandler needs to
      * execute something in each update register an action with triggerCommand = null. Exception to this
      * is an InputHandler that checks additional input types, that cannot be handled via {@link InputHandlerDevice}s.
      *
      * @param time The time to pass to every action that is active.
      * @see #addAction(com.jme.input.action.InputAction, String, boolean)
      */
     public void update( float time ) {
         if ( !isEnabled() ) {
             return;
         }
 
         event.setTime( time );
 
         synchronized ( this ) {
             for ( ActionTrigger trigger = activeTriggers; trigger != null; ) {
                 ActionTrigger nextTrigger = trigger.getNext(); //perform action might deactivate the action
                 // -> getNext() would return null then
                 trigger.performAction( event );
                 trigger = nextTrigger;
             }
         }
 
         for ( int i = this.sizeOfAttachedHandlers() - 1; i >= 0; i-- ) {
             InputHandler handler = this.getFromAttachedHandlers( i );
             if ( handler.isEnabled() ) {
                 handler.update( time );
             }
         }
         //TODO: provide a list of events that occur this frame?
     }
 
 
     public static float getFloatProp( HashMap props, String key, float defaultVal ) {
         if ( props == null || props.get( key ) == null ) {
             return defaultVal;
         }
         else {
             return Float.parseFloat( props.get( key ).toString() );
         }
     }
 
     public static int getIntProp( HashMap props, String key, int defaultVal ) {
         if ( props == null || props.get( key ) == null ) {
             return defaultVal;
         }
         else {
             return Integer.parseInt( props.get( key ).toString() );
         }
     }
 
     public static boolean getBooleanProp( HashMap props, String key, boolean defaultVal ) {
         if ( props == null || props.get( key ) == null ) {
             return defaultVal;
         }
         else {
             return "true".equalsIgnoreCase( props.get( key ).toString() );
         }
     }
 
     public static Object getObjectProp( HashMap props, String key, Object defaultVal ) {
         if ( props == null || props.get( key ) == null ) {
             return defaultVal;
         }
         else {
             return props.get( key );
         }
     }
 
 
     /**
      * @return true if this handler is currently enabled
      */
     public boolean isEnabled() {
         return this.enabled;
     }
 
     /**
      * store the value for field enabled
      */
     private boolean enabled = true;
 
     /**
      * Enable/disable the handler: disabled handler do not invoke actions and do not update attached handlers.
      *
      * @param value true to enable the handler, false to disable the handler
      */
     public synchronized void setEnabled( final boolean value ) {
         final boolean oldValue = this.enabled;
         if ( oldValue != value ) {
             this.enabled = value;
             // todo: we could decide to have one device per handler to reduce amount of triggers that are checked on
             // each event, devices would then be notified here about disabling the handler
         }
     }
 
     /**
      * enabled/disables all attached handlers but this handler keeps its status.
      *
      * @param enabled true to enable all attached handlers, false to disable them
      */
     public void setEnabledOfAttachedHandlers( boolean enabled ) {
         for ( int i = this.sizeOfAttachedHandlers() - 1; i >= 0; i-- ) {
             InputHandler handler = this.getFromAttachedHandlers( i );
             handler.setEnabled( false );
         }
     }
 
     /**
      * List of InputHandlers
      */
     private ArrayList attachedHandlers;
 
     /**
      * Attach a handler which should be updated in this handlers update method.
      *
      * @param value handler to attach
      * @return true if handler was not attached before
      */
     public boolean addToAttachedHandlers( InputHandler value ) {
         if ( value != null ) {
             return value.setParent( this );
         }
         else {
             return false;
         }
     }
 
     /**
      * Attach a handler which should be updated in this handlers update method.
      *
      * @param value handler to attach
      * @return true if handler was not attached before
      */
     private boolean addToAttachedHandlers_internal( InputHandler value ) {
         boolean changed = false;
         if ( value != null ) {
             if ( this.attachedHandlers == null ) {
                 this.attachedHandlers = new ArrayList();
             }
             else if ( this.attachedHandlers.contains( value ) ) {
                 return false;
             }
             changed = this.attachedHandlers.add( value );
             if ( changed ) {
                 value.setParent( this );
             }
         }
         return changed;
     }
 
     /**
      * Get an element from the attachedHandlers association.
      *
      * @param index index of element to be retrieved
      * @return the element, null if index out of range
      */
     public InputHandler getFromAttachedHandlers( int index ) {
         if ( attachedHandlers != null && index >= 0 && index < attachedHandlers.size() ) {
             return (InputHandler) attachedHandlers.get( index );
         }
         else {
             return null;
         }
     }
 
     public void removeAllFromAttachedHandlers() {
         for ( int i = this.sizeOfAttachedHandlers() - 1; i >= 0; i-- ) {
             InputHandler handler = this.getFromAttachedHandlers( i );
             this.removeFromAttachedHandlers( handler );
         }
     }
 
     public boolean removeFromAttachedHandlers( InputHandler value ) {
         boolean changed = false;
         if ( ( this.attachedHandlers != null ) && ( value != null ) ) {
             changed = this.attachedHandlers.remove( value );
             if ( changed ) {
                 value.setParent( null );
             }
         }
         return changed;
     }
 
     /**
      * @return number of attached handlers
      */
     public int sizeOfAttachedHandlers() {
         return ( ( this.attachedHandlers == null )
                 ? 0
                 : this.attachedHandlers.size() );
     }
 
     /**
      * store value for field parent
      */
     private InputHandler parent;
 
     /**
      * Query parent handler.
      *
      * @return current parent
      */
     public InputHandler getParent() {
         return this.parent;
     }
 
     /**
      * @param value new value for field parent
      * @return true if parent was changed
      */
     protected boolean setParent( InputHandler value ) {
         boolean changed = false;
         final InputHandler oldValue = this.parent;
         if ( oldValue != value ) {
             if ( oldValue != null ) {
                 this.parent = null;
                 oldValue.removeFromAttachedHandlers( this );
             }
             this.parent = value;
             if ( value != null ) {
                 value.addToAttachedHandlers_internal( this );
             }
             changed = true;
         }
         return changed;
     }
 
 }
