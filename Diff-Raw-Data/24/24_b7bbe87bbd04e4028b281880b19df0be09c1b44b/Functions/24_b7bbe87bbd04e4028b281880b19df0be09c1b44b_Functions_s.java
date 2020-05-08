 /**
  * Functions.java
  * Part of Javascool proglet "game"
  * File creation : 20110704 at revision 48 by gmatheron (INRIA)
  * Last modification 20110704 by gmatheron (INRIA)
  * 
  * gmatheron : guillaume.matheron.06@gmail.com
  */
 package org.javascool.proglets.game;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.javascool.tools.Console;
 import org.javascool.tools.Macros;
 import org.javascool.proglets.game.MouseState;
 import org.javascool.proglets.game.MouseWheelState;
 
 /* To use these event listeners, use this syntax : 
  * void toto(MouseState s) {
  *     ...
  * }
  * void main() {
  *     onClick("toto");
  * }
  */
 
 
 /**
  * 
  * @author gmatheron
  */
 public class Functions {
     private static Functions m_singleton;
     private boolean m_mouseDown[] = {false, false, false};
     private int m_mouseWheelPosition=0;
     /* These arrays are designed to store the functions the user assigned a listener
      * A convenience type is used : CallbackFunction
      */
     private java.util.ArrayList<String> m_onClick;
     private java.util.ArrayList<String> m_onMouseEntered;
     private java.util.ArrayList<String> m_onMouseExited;
     private java.util.ArrayList<String> m_onMousePressed;
     private java.util.ArrayList<String> m_onMouseReleased;
     private java.util.ArrayList<String> m_onMouseDown;
     private java.util.ArrayList<String> m_onMouseUp;
     private java.util.ArrayList<String> m_onMouseMoved;
     private java.util.ArrayList<String> m_onMouseDragged;
     private java.util.ArrayList<String> m_onMouseWheelUp;
     private java.util.ArrayList<String> m_onMouseWheelDown;
     private java.util.ArrayList<String> m_onMouseWheelMoved;
 
     public static void test(){
         try {
             for(Method m:Macros.getProgram().getClass().getDeclaredMethods()){
                System.err.println("Method : "+m.getName());
             }
         } catch (Exception ex) {
             Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onClick(String s) {
         m_singleton.m_onClick.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseEntered(String s) {
         m_singleton.m_onMouseEntered.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseExited(String s) {
         m_singleton.m_onMouseExited.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMousePressed(String s) {
         m_singleton.m_onMousePressed.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseReleased(String s) {
         m_singleton.m_onMouseReleased.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseDown(String s) {
         m_singleton.m_onMouseDown.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseUp(String s) {
         m_singleton.m_onMouseUp.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseMoved(String s) {
         m_singleton.m_onMouseMoved.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseDragged(String s) {
         m_singleton.m_onMouseDragged.add(s);
     }
     
     public static void onMouseWheelUp(String s) {
         m_singleton.m_onMouseWheelUp.add(s);
     }
     
     public static void onMouseWheelDown(String s) {
         m_singleton.m_onMouseWheelDown.add(s);
     }
     
     public static void onMouseWheelMoved(String s) {
         m_singleton.m_onMouseWheelMoved.add(s);
     }
 
     /**
      * Calls the specified end-user-defined method, passing as a parameter the specified state.
      * If the function is undefined with one argument, it will be called with no arguments.
      * @param method The end-user-defined method to call
      * @param s The state to pass
      */
     private static void call(String method, State s) {
         try {
            Console.getProgram().getClass().getMethod(method, State.class).invoke(Console.getProgram(), s);
         } catch (IllegalAccessException ex) {
             Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalArgumentException ex) {
             Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InvocationTargetException ex) {
             Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
         } catch (NoSuchMethodException ex) {
             try {
                Console.getProgram().getClass().getMethod(method).invoke(Console.getProgram());
            } catch (IllegalAccessException ex2) {
                 Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex2);
            } catch (IllegalArgumentException ex2) {
                 Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex2);
            } catch (InvocationTargetException ex2) {
                 Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex2);
            } catch (NoSuchMethodException ex2) {
                 Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex2);
             } catch (SecurityException ex2) {
                 Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex2);
             }
         } catch (SecurityException ex) {
             Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     //TODO
     /**
      * This method should be called during init (see bug report #005)
      * It creates the listeners, the clock and the singleton
      */
     public static void start() {
         /* A singleton is used to be able to define non-static classes inside Functions
          * The functions the end-user can call are all static, but they refer
          * to non-static attributes using this singleton static attribute
          */
         m_singleton=new Functions();
         
         /* These arrays store the listeners that should be called when an event occurs
          */
         m_singleton.m_onClick=new java.util.ArrayList<String>();
         m_singleton.m_onMouseDown=new java.util.ArrayList<String>();
         m_singleton.m_onMouseDragged=new java.util.ArrayList<String>();
         m_singleton.m_onMouseEntered=new java.util.ArrayList<String>();
         m_singleton.m_onMouseExited=new java.util.ArrayList<String>();
         m_singleton.m_onMouseMoved=new java.util.ArrayList<String>();
         m_singleton.m_onMousePressed=new java.util.ArrayList<String>();
         m_singleton.m_onMousePressed=new java.util.ArrayList<String>();
         m_singleton.m_onMouseReleased=new java.util.ArrayList<String>();
         m_singleton.m_onMouseUp=new java.util.ArrayList<String>();
         
         /* The clock object will 'tick' each 1/30s. it will then call the callback
          * functions for onMouseDown, onMouseUp, etc if needed
          */
         Clock c = new Clock();
         c.setFps(30);
         new Thread(c).start();
 
         /* Define a few anonymous classes that will define the proglet's behavior
          * when an event is performed. Uually the proglet will call all the callback
          * functions for this event.
          * Possible optimization : only register a listener if an event is defined
          */
         /********* START ANONYMOUS CLASSES ***************/
         Macros.getProgletPanel().addMouseListener(
             new java.awt.event.MouseListener() {
                 @Override
                 public void mouseClicked(java.awt.event.MouseEvent evt) {
                     callback(m_singleton.m_onClick, (new MouseState(evt)));
                 }
 
                 @Override
                 public void mouseEntered(java.awt.event.MouseEvent evt) {
                     callback(m_singleton.m_onMouseEntered, (new MouseState(evt)));
                 }
 
                 @Override
                 public void mouseExited(java.awt.event.MouseEvent evt) {
                     callback(m_singleton.m_onMouseExited, (new MouseState(evt)));
                 }
 
                 @Override
                 public void mousePressed(java.awt.event.MouseEvent evt) {
                     callback(m_singleton.m_onMousePressed, (new MouseState(evt)));
                     if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                         m_singleton.m_mouseDown[0] = true;
                     } else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON2) {
                        m_singleton. m_mouseDown[1] = true;
                     } else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                         m_singleton.m_mouseDown[2] = true;
                     }
                 }
 
                 @Override
                 public void mouseReleased(java.awt.event.MouseEvent evt) {
                     callback(m_singleton.m_onMouseReleased, (new MouseState(evt)));
                     if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                         m_singleton.m_mouseDown[0] = false;
                     } else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON2) {
                         m_singleton.m_mouseDown[1] = false;
                     } else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                         m_singleton.m_mouseDown[2] = false;
                     }
                 }
             }
         );
         Macros.getProgletPanel().addMouseMotionListener(
             new java.awt.event.MouseMotionListener() {
 
                 @Override
                 public void mouseDragged(MouseEvent e) {
                     callback(m_singleton.m_onMouseDragged, (new MouseState(e)));
                 }
 
                 @Override
                 public void mouseMoved(MouseEvent e) {
                     callback(m_singleton.m_onMouseMoved, (new MouseState(e)));
                 }
             }
         );
         Macros.getProgletPanel().addMouseWheelListener(
             new java.awt.event.MouseWheelListener() {
                 public void mouseWheelMoved(MouseWheelEvent e) {
                     int copy=m_singleton.m_mouseWheelPosition;
                     m_singleton.m_mouseWheelPosition+=e.getScrollAmount();
                     if (copy>m_singleton.m_mouseWheelPosition)
                         callback(m_singleton.m_onMouseWheelDown,(new MouseWheelState(e)));
                     else
                         callback(m_singleton.m_onMouseWheelUp,(new MouseWheelState(e)));
                 }
             }
         );
         /********* END ANONYMOUS CLASSES ***************/
     }
     
     private static void callback(java.util.ArrayList<String> functions, State s) {
         for (int i=0; i<functions.size(); i++) {
             call(functions.get(i),s);
         }
     }
     
     /**
      * TODO
      */
     class Sprite {
         private javax.swing.JPanel m_panel;
     }
 
     public static class Clock implements Runnable {
 
         private int m_fps;
 
         @Override
         public void run() {
             while (true) {
                 try {
                     Thread.sleep(1000 / m_fps);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 tick();
             }
         }
 
         public void setFps(int fps) {
             m_fps = fps;
         }
 
         private void tick() {
             for (int j = 0; j < 3; j++) {
                 if (m_singleton.m_mouseDown[j]) {
                     for (int i = 0; i < m_singleton.m_onMouseDown.size(); i++) {
                         call(m_singleton.m_onMouseDown.get(i), new MouseState(j));
                     }
                 } else {
                     for (int i = 0; i < m_singleton.m_onMouseUp.size(); i++) {
                         call(m_singleton.m_onMouseUp.get(i), new MouseState(j));
                     }
                 }
             }
         }
     }
 
     
 }
 
 /**
  * Superclass of all the states.
  * 
  * If an abstract (State) class can't be declared into a non-abstract class (Functions)
  * maybe we could switch it to an interface or leave it non-abstract (ugly)
  */
 abstract class State {
 }
