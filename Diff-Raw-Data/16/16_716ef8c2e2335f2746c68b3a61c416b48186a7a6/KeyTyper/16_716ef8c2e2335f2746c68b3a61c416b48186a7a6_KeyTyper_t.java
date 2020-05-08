 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.common.driver;
 
 import java.awt.AWTEvent;
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Set;
 
 import javax.swing.KeyStroke;
 
 import org.apache.commons.lang.Validate;
 import org.eclipse.jubula.rc.common.exception.RobotException;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.common.util.MatchUtil;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
import org.eclipse.jubula.tools.utils.EnvironmentUtils;
 
 
 /**
  * Presses keys. Supports "native" keypresses, in the sense that one can 
  * optionally choose not to wait for event confirmation.
  *
  * @author BREDEX GmbH
  * @created Oct 24, 2007
  */
 public class KeyTyper {
 
     /** Key codes for keys that also appear on the number pad */
     public static final int [] NUMPAD_KEYCODES = {
         KeyEvent.VK_PAGE_UP,
         KeyEvent.VK_PAGE_DOWN,
         KeyEvent.VK_HOME,
         KeyEvent.VK_END,
         KeyEvent.VK_DELETE,
         KeyEvent.VK_INSERT,
         KeyEvent.VK_RIGHT,
         KeyEvent.VK_LEFT,
         KeyEvent.VK_UP,
         KeyEvent.VK_DOWN,
         KeyEvent.VK_KP_RIGHT,
         KeyEvent.VK_KP_LEFT,
         KeyEvent.VK_KP_UP,
         KeyEvent.VK_KP_DOWN
     };
 
     /** regexp specifying the legal formatting of native input strings */
     private static final String VALID_INPUT = "[a-zA-z0-9]*"; //$NON-NLS-1$
     
     /** the logger */
     private static AutServerLogger log = 
         new AutServerLogger(KeyTyper.class);
 
     /** single instance */
     private static KeyTyper instance = null;
     
     /** robot used for native key presses */
     private Robot m_robot;
 
     /**
      * Private constructor
      * 
      * @throws AWTException if there is a problem creating the Robot.
      */
     private KeyTyper() throws AWTException {
         m_robot = new Robot();
     }
     
     /**
      * 
      * @return the single instance.
      */
     public static KeyTyper getInstance() throws AWTException {
         if (instance == null) {
             instance = new KeyTyper();
         }
         return instance;
     }
 
     /**
      * Types the given keystroke.
      * If any of the intercepting and event matching arguments are 
      * <code>null</code>, this method will not wait for event confirmation. It 
      * will simply assume that the events were received correctly. Otherwise,
      * this method will use the given interceptor and event matcher arguments to
      * handle event confirmation.
      * 
      * @param keyStroke The key stroke. May not be null.
      * @param interceptor The interceptor that will be used to wait for event
      *                    confirmation.
      * @param keyDownMatcher The event matcher to be used for key press event
      *                       confirmation.
      * @param keyUpMatcher The event matcher to be used for key release event
      *                     confirmation.
      */
     public void type(KeyStroke keyStroke, IRobotEventInterceptor interceptor, 
         IEventMatcher keyDownMatcher, IEventMatcher keyUpMatcher) {
         
         try {
             Validate.notNull(keyStroke);
             boolean waitForConfirm = interceptor != null 
                 && keyDownMatcher != null && keyUpMatcher != null;
             InterceptorOptions options = new InterceptorOptions(new long[]{
                 AWTEvent.KEY_EVENT_MASK});
             List keycodes = modifierKeyCodes(keyStroke);
             keycodes.add(new Integer(keyStroke.getKeyCode()));
             if (log.isDebugEnabled()) {
                 String keyModifierText = KeyEvent.getKeyModifiersText(keyStroke
                     .getModifiers());
                 String keyText = KeyEvent.getKeyText(keyStroke.getKeyCode());
                 log.debug("Key stroke: " + keyStroke); //$NON-NLS-1$
                 log.debug("Modifiers, Key: " + keyModifierText + ", " + keyText);  //$NON-NLS-1$//$NON-NLS-2$
                 log.debug("number of keycodes: " + keycodes.size()); //$NON-NLS-1$
             }
             m_robot.setAutoWaitForIdle(true);
 
             // FIXME Hack for MS Windows for keys that also appear on the numpad.
             //       Turns NumLock off. Does nothing if locking key functionality
             //       isn't implemented for the operating system.
             boolean isNumLockToggled = hackWindowsNumpadKeys1(
                 keyStroke.getKeyCode());
 
             // first press all keys, then release all keys, but
             // avoid to press and release any key twice (even if perhaps alt
             // and meta should have the same keycode(??)
             Set alreadyDown = new HashSet();
             ListIterator i = keycodes.listIterator();
             try {
                 while (i.hasNext()) {
                     Integer keycode = (Integer)i.next();
                     if (log.isDebugEnabled()) {
                         log.debug("trying to press: " + keycode); //$NON-NLS-1$
                     }
                     if (!alreadyDown.contains(keycode)) {
                         IRobotEventConfirmer confirmer = null;
                         if (waitForConfirm) {
                             confirmer = interceptor.intercept(options);
                         }
                         if (log.isDebugEnabled()) {
                             log.debug("pressing: " + keycode); //$NON-NLS-1$
                         }
                         alreadyDown.add(keycode);
                         m_robot.keyPress(keycode.intValue());
                         if (waitForConfirm) {
                             confirmer.waitToConfirm(null, keyDownMatcher);
                         }
                     }
                 }
             } finally {
                 releaseKeys(options, alreadyDown, i, interceptor, keyUpMatcher);
                 // FIXME Hack for MS Windows for keys that also appear on the numpad.
                 //       Turns NumLock back on, if necessary.
                 if (isNumLockToggled) {
                     hackWindowsNumpadKeys2();
                 }
             }
         } catch (IllegalArgumentException e) {
             throw new RobotException(e);
         }
     }
     
     /**
      * Types the given keystroke. The arguments must adhere to the specification
      * at <a
      * href=http://java.sun.com/j2se/1.4.2/docs/api/javax/swing/KeyStroke.html#getKeyStroke(java.lang.String)>
      * If any of the intercepting and event matching arguments are 
      * <code>null</code>, this method will not wait for event confirmation. It 
      * will simply assume that the events were received correctly. Otherwise,
      * this method will use the given interceptor and event matcher arguments to
      * handle event confirmation.
      * 
      * @param keyStrokeSpec The key code.
      * @param interceptor The interceptor that will be used to wait for event
      *                    confirmation.
      * @param keyDownMatcher The event matcher to be used for key press event
      *                       confirmation.
      * @param keyUpMatcher The event matcher to be used for key release event
      *                     confirmation.
      */
     public void type(String keyStrokeSpec, IRobotEventInterceptor interceptor,
             IEventMatcher keyDownMatcher, IEventMatcher keyUpMatcher) {
         KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeSpec);
         if (keyStroke == null) {
             String trimmedKeyStrokeSpec = keyStrokeSpec.trim();
             int indexOfKeySpec = trimmedKeyStrokeSpec.lastIndexOf(" "); //$NON-NLS-1$
             if (indexOfKeySpec != -1) {
                 String keySpec = trimmedKeyStrokeSpec.substring(
                         indexOfKeySpec + 1,
                         trimmedKeyStrokeSpec.length());
                 KeyStroke checkKeyStroke = KeyStroke.getKeyStroke(keySpec);
                 if (checkKeyStroke == null) {
                     throw new StepExecutionException(
                             "invalid key spec", //$NON-NLS-1$
                             EventFactory.createActionError(
                                     "TestErrorEvent.InvalidKeySpec",  //$NON-NLS-1$
                                     new String [] {keySpec}));
                 }
                 String invalidModifier = trimmedKeyStrokeSpec.substring(0,
                         indexOfKeySpec).trim();
                 throw new StepExecutionException(
                         "invalid modifier", //$NON-NLS-1$
                         EventFactory.createActionError(
                                 "TestErrorEvent.InvalidModifier", //$NON-NLS-1$
                                 new String [] {invalidModifier}));
             }
         }
         type(keyStroke, interceptor, keyDownMatcher, keyUpMatcher);
     }
 
     /**
      * @param keyStroke KeyStroke whose modifiers are requested
      * @return a List of KeyCodes (hopefully) realising the ModifierMask contained in the KeyStroke
      */
     private List modifierKeyCodes(KeyStroke keyStroke) {
         List l = new LinkedList();
         int modifiers = keyStroke.getModifiers();
         // this is jdk 1.3 - code.
         // use ALT_DOWN_MASK instead etc. with jdk 1.4 !
         if ((modifiers & InputEvent.ALT_MASK) != 0) {
             l.add(new Integer(KeyEvent.VK_ALT));
         }
         if ((modifiers & InputEvent.ALT_GRAPH_MASK) != 0) {
             l.add(new Integer(KeyEvent.VK_ALT_GRAPH));
         }
         if ((modifiers & InputEvent.CTRL_MASK) != 0) {
             l.add(new Integer(KeyEvent.VK_CONTROL));
         }
         if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
             l.add(new Integer(KeyEvent.VK_SHIFT));
         }
         if ((modifiers & InputEvent.META_MASK) != 0) {
             l.add(new Integer(KeyEvent.VK_META));
         }
         return l;
     }
     
     /**
      * Fix for MS Windows for keys that also appear on the numpad. Turns
      * NumLock off if it is on.
      * First method called of a two-part fix.
      * @param keyCode keycode to check
      * @return <code>True</code>, if the NumLock status was toggled. Otherwise
      *         <code>false</code>. Basically, a value of true indicates that 
      *         second part of this fix must also be used.
      */
     private boolean hackWindowsNumpadKeys1(int keyCode) {
        if (!EnvironmentUtils.isWindowsOS()) {
            return false;
        }
         // FIXME Fix for MS Windows for keys that also appear on the numpad.
         //       Turns NumLock off.
         boolean isNumpadKey = false;
         for (int i = 0; i < NUMPAD_KEYCODES.length; ++i) {
             if (NUMPAD_KEYCODES[i] == keyCode) {
                 isNumpadKey = true;
                 break;
             }
         }
         boolean wasNumLockToggled = false;
         if (isNumpadKey) {
             try {
                 // FIXME Extra-ugly hack to get the CURRENT status of 
                 // NumLock. 
                 /* 
                  * See:
                  * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6446890
                  * Using NumLock as the key to press because it seems
                  * least likely to affect the AUT. We also type it twice, 
                  * obviously, so as not to change the real status of NumLock.
                  */
                 m_robot.keyPress(KeyEvent.VK_NUM_LOCK);
                 m_robot.keyRelease(KeyEvent.VK_NUM_LOCK);
                 m_robot.keyPress(KeyEvent.VK_NUM_LOCK);
                 m_robot.keyRelease(KeyEvent.VK_NUM_LOCK);
                 
                final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
                if (defaultToolkit.getLockingKeyState(KeyEvent.VK_NUM_LOCK)) {
                    defaultToolkit.setLockingKeyState(KeyEvent.VK_NUM_LOCK,
                        false);
                     wasNumLockToggled = true;
                 }
             } catch (UnsupportedOperationException usoe) {
                 // OS does not support locking key operations
                 // Do nothing -> Leave NumLock alone
                 log.info("UnsupportedOperationException thrown by NumPad "  //$NON-NLS-1$
                     + "workaround. NumLock will not be toggled."); //$NON-NLS-1$
             }
         }
         return wasNumLockToggled;
     }
     
     /**
      * Fix for MS Windows for keys that also appear on the numpad. Turns
      * NumLock on.
      * Second method called of a two-part fix.
      */
     private void hackWindowsNumpadKeys2() {
         Toolkit.getDefaultToolkit().setLockingKeyState(
             KeyEvent.VK_NUM_LOCK, true);
     }
 
     /**
      * @param options options
      * @param alreadyDown alreadyDown
      * @param i i
      * @param interceptor The interceptor that will be used to wait for event
      *                    confirmation.
      * @param keyUpMatcher The event matcher to be used for key release event
      *                     confirmation.
      */
     private void releaseKeys(InterceptorOptions options, Set alreadyDown, 
         ListIterator i, IRobotEventInterceptor interceptor, 
         IEventMatcher keyUpMatcher) {
         
         boolean waitForConfirm = interceptor != null && keyUpMatcher != null;
         // Release all keys in reverse order.
         Set alreadyUp = new HashSet();
         while (i.hasPrevious()) {
             Integer keycode = (Integer)i.previous();
             if (log.isDebugEnabled()) {
                 log.debug("trying to release: " + keycode.intValue()); //$NON-NLS-1$
             }
             if (!alreadyUp.contains(keycode)
                 && alreadyDown.contains(keycode)) {
                 try {
                     IRobotEventConfirmer confirmer = null;
                     if (waitForConfirm) {
                         confirmer = interceptor.intercept(options);
                     }
                     if (log.isDebugEnabled()) {
                         log.debug("releasing: " + keycode.intValue()); //$NON-NLS-1$
                     }
                     alreadyUp.add(keycode);
                     m_robot.keyRelease(keycode.intValue());
                     if (waitForConfirm) {
                         confirmer.waitToConfirm(null, keyUpMatcher);
                     }
                 } catch (RobotException e) {
                     log.error("error releasing keys", e); //$NON-NLS-1$
                     if (!i.hasPrevious()) {
                         throw e;
                     }
                 }
             }
         }
     }
 
     /**
      * Types the given string without checking for event confirmation. Note that
      * that only alphanumeric characters can be typed using this method.
      * 
      * @param text The text to type.
      */
     public void nativeTypeString(String text) {
         // Verify that the string consists of only valid characters
         // (any change in verification will probably require a change in 
         //  how the text is processed)
         if (MatchUtil.getInstance().match(
             text, VALID_INPUT, MatchUtil.MATCHES_REGEXP)) {
 
             boolean isCapsLockOn = false;
             try {
                 isCapsLockOn = Toolkit.getDefaultToolkit().getLockingKeyState(
                     KeyEvent.VK_CAPS_LOCK);
             } catch (UnsupportedOperationException uoe) {
                 // Do nothing.
                 // Querying the status of the Caps Lock key is not possible on
                 // certain platforms (ex. Linux). In this case, we will just 
                 // assume that Caps Lock is not active.
             }
             for (int i = 0; i < text.length(); i++) {
                 char c = text.charAt(i);
                 boolean holdShift = Character.isUpperCase(c) 
                     ^ isCapsLockOn;
                 StringBuffer sb = new StringBuffer();
                 if (holdShift) {
                     sb.append("shift "); //$NON-NLS-1$
                 }
                 sb.append(Character.toUpperCase(c));
                 type(sb.toString(), null, null, null);
             }
         } else {
             throw new StepExecutionException(
                 "Invalid input string (only ASCII alphanumeric strings are allowed)", //$NON-NLS-1$ 
                 EventFactory.createActionError(
                         TestErrorEvent.INVALID_PARAM_VALUE));
         }
     }
 
 }
